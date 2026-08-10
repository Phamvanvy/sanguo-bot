package com.pip.server.account.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.server.account.AccountEntity;
import com.pip.server.account.AccountService;
import com.pip.server.account.CreateAccountException;
import com.pip.server.account.Errors;
import com.pip.server.account.IStringValidator;
import com.pip.server.account.NumberStringValidator;

public class CreateAccountServlet extends HttpServlet {
	
	protected AccountService accountService;
	protected IStringValidator numberStringValidator = new NumberStringValidator();
	protected IStringValidator phoneValidator;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	private static final Logger log = Logger.getLogger(CreateAccountServlet.class);
	
	public CreateAccountServlet(AccountService accountService,IStringValidator phoneValidator){
		this.accountService = accountService;
		this.phoneValidator = phoneValidator;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String name = req.getParameter("name");
		String password = req.getParameter("password");
		String phone = req.getParameter("phone");
		String gameCode = req.getParameter("gamecode");
		name = name.trim();
		req.setCharacterEncoding("GBK");
		resp.setCharacterEncoding("GBK");
		if(name==null||password==null||phone==null||gameCode==null){
			sendError(resp, "参数错误");
			return;			
		}
		if(name.startsWith("游客")){
			sendError(resp, "参数错误");
			return;				
		}
		if (numberStringValidator.valid(name) == IStringValidator.OK) { //不允许除了手机号码以外的纯数字
			if (phoneValidator.valid(name) != IStringValidator.OK)
				sendError(resp, "帐号名出现非法字符");
		}
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try {
			AccountEntity account = accountService.register(name, "", "", 10000,
					"CCCCCWEB", phone, "", "", gameCode, "opera",null,"");
			tx.commit();
			log.info(account.getName() + "Registered  model web");
			sendOk(resp, password);
		} catch (CreateAccountException e) {
			tx.rollback();
			int code = e.getCode();
			if (code == Errors.ILLEGAL_LENGTH) {
				sendError(resp, "帐号名长度不正确");
				return;
			} else if (code == Errors.ILLEGAL_NAME_CHAR) {
				sendError(resp, "帐号名出现非法字符");
				return;
			} else if (code == Errors.ILLEGAL_PHONE) {
				sendError(resp, "手机号有误");
				return;
			} else if (code == Errors.DUPLICATE_NAME) {
				sendError(resp, "已经存在同名帐号");
				return;
			} else {
				sendError(resp, "创建帐号错误");
				return;
			}
		}

	}

    private void sendError(HttpServletResponse response, String error) throws IOException{
        response.getWriter().println("2");
        response.getWriter().print(error);
    }

    private void sendOk(HttpServletResponse response, String password) throws IOException{
        response.getWriter().println("1");
        response.getWriter().print(password);
    }
}
