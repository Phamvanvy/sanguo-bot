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

public class CreatePhoneAccountServlet extends HttpServlet {
	
	protected AccountService accountService;
	protected IStringValidator numberStringValidator = new NumberStringValidator();
	protected IStringValidator phoneValidator;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	private static final Logger log = Logger.getLogger(CreateAccountServlet.class);
	
	public CreatePhoneAccountServlet(AccountService accountService,IStringValidator phoneValidator){
		this.accountService = accountService;
		this.phoneValidator = phoneValidator;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String name = req.getParameter("name");
		String password = req.getParameter("password");
		int regType = Integer.parseInt(req.getParameter("type"));
		int cbalance = Integer.parseInt(req.getParameter("cbalance"));
		name = name.trim();
		req.setCharacterEncoding("GBK");
		resp.setCharacterEncoding("GBK");
		if(name==null||password==null){
			sendError(resp, 3);
			return;			
		}
		if(name.startsWith("сн©м")){
			sendError(resp, 3);
			return;				
		}
		if (phoneValidator.valid(name) != IStringValidator.OK){
			sendError(resp, 3);
			return;
		}
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try {
			AccountEntity account = accountService.registerWithPassword(name,
					password, 0, cbalance, "CCCCCCFG", "", "", "opera", null,
					regType);
			tx.commit();
			log.info(account.getName() + "Registered  model web1");
			sendOk(resp, 1);
		} catch (CreateAccountException e) {
			tx.rollback();
			int code = e.getCode();
			if (code == Errors.ILLEGAL_LENGTH) {
				sendError(resp, 3);
				return;
			} else if (code == Errors.ILLEGAL_NAME_CHAR) {
				sendError(resp, 3);
				return;
			} else if (code == Errors.ILLEGAL_PHONE) {
				sendError(resp, 3);
				return;
			} else if (code == Errors.DUPLICATE_NAME) {
				sendOk(resp,2);
				return;
			} else {
				sendError(resp, 3);
				return;
			}
		}

	}

    private void sendError(HttpServletResponse response, int code) throws IOException{
        response.getWriter().println(code);
    }

    private void sendOk(HttpServletResponse response,int code) throws IOException{
        response.getWriter().println(code);
    }
}

