package com.pip.server.billing.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;

public class ValidSecurityServlet extends HttpServlet {
	
	protected AccountSecurityService service;
	
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	protected static Logger log = Logger.getLogger(ValidSecurityServlet.class);

	public ValidSecurityServlet(AccountSecurityService service){
		this.service = service;
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setCharacterEncoding("utf-8");
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try {
			int id = SecurityUtil.getId(req);
			AccountSecurity as = service.findAndCreateAccountSecurity(id);
			String phone = SecurityUtil.getPhone(req);
			if (phone != null) {
				if(phone.equals(as.getPhone()))
					SecurityUtil.ok(resp, null);
				else
					SecurityUtil.error(resp, null);
				return;
			}
			String mail = SecurityUtil.getMail(req);
			if (mail != null) {
				if(mail.equals(as.getMail()))
					SecurityUtil.ok(resp, null);
				else
					SecurityUtil.error(resp, null);
				return;
			}
			String question = SecurityUtil.getQuestion(req);
			String answer = SecurityUtil.getAnswer(req);
			if (question != null && answer != null) {
				if(question.equals(as.getQuestion())&&answer.equals(as.getAnswer()))
					SecurityUtil.ok(resp, null);
				else
					SecurityUtil.error(resp, null);
				return;
			}
			String idcard = SecurityUtil.getIdcard(req);
			if (idcard != null) {
				if(idcard.equals(as.getIdcard()))
					SecurityUtil.ok(resp, null);
				else
					SecurityUtil.error(resp, null);
				return;
			}
			SecurityUtil.error(resp, SecurityUtil.ERROR_INVALIDREQUEST);
		} catch (IllegalIdException e) {
			SecurityUtil.error(resp, SecurityUtil.ERROR_ILLEGALID);
			tx.rollback();
		} catch (AccountNotFoundException e) {
			SecurityUtil.error(resp, SecurityUtil.ERROR_ILLEGALID);
			tx.rollback();
		} catch (Exception e){
			log.error(e,e);
			tx.rollback();
		} finally{
			if(tx.isActive()){
				tx.commit();
			}
		}
	}
}
