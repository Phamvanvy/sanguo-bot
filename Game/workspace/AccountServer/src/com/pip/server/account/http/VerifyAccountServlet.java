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
import com.pip.server.account.util.Util;

public class VerifyAccountServlet extends HttpServlet {
	
	protected static final Logger log = Logger.getLogger(VerifyAccountServlet.class);
	
	protected AccountService accountService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	
	public VerifyAccountServlet(AccountService accountService){
		this.accountService = accountService;
	}
	
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setCharacterEncoding("GBK");
		String name = req.getParameter("name");
		String password = req.getParameter("password");
		log.info("Verify Name[" + name + "] Password[" + password + "]");
		if(name==null||name.length()==0){
			sendError(resp,"参数错误");
			return;
		}
        Transaction tx = sf.getCurrentSession().beginTransaction();
        AccountEntity ae = accountService.getAccountEntity(name);
        tx.commit();
		if(ae==null){
			sendError(resp, "用户名或者密码错误");
		}else{
			if(Util.verifyPassword(ae.getPassWord(), password)){
				sendOk(resp,"");
			}else{
				sendError(resp,"原始密码错误");
			}
		}
	}
	
	private void sendError(HttpServletResponse response, String error)
			throws IOException {
		response.getWriter().println("2");
		response.getWriter().print(error);
	}
	
    private void sendOk(HttpServletResponse response, String password)
			throws IOException {
		response.getWriter().println("1");
		response.getWriter().print(password);
	}	
}
