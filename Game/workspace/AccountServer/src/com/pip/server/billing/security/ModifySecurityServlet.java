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

public class ModifySecurityServlet extends HttpServlet {

	protected AccountSecurityService service;
	
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	protected static Logger log = Logger.getLogger(ModifySecurityServlet.class);
	
	public ModifySecurityServlet(AccountSecurityService service){
		this.service = service;
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setCharacterEncoding("utf-8");
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try {
			int id = SecurityUtil.getId(req);
			String phone = SecurityUtil.getPhone(req);
			if (phone != null) {
				String s = service.modifyPhone(id, phone);
				SecurityUtil.ok(resp, s);
				return;
			}
			String mail = SecurityUtil.getMail(req);
			if (mail != null) {
				service.modifyMail(id, mail);
				SecurityUtil.ok(resp, null);
				return;
			}
			String question = SecurityUtil.getQuestion(req);
			String answer = SecurityUtil.getAnswer(req);
			if (question != null && answer != null) {
				service.modifyQnA(id, question, answer);
				SecurityUtil.ok(resp, null);
				return;
			}
			String idcard = SecurityUtil.getIdcard(req);
			if (idcard != null) {
				service.modifyIdcard(id, idcard);
				SecurityUtil.ok(resp, null);
				return;
			}
			SecurityUtil.error(resp, SecurityUtil.ERROR_INVALIDREQUEST);
		} catch (IllegalIdException e) {
			SecurityUtil.error(resp, SecurityUtil.ERROR_ILLEGALID);
			tx.rollback();
		} catch (IllegalPhoneException e) {
			SecurityUtil.error(resp, SecurityUtil.ERROR_ILLEGALPHONE);
			tx.rollback();
		} catch (AccountNotFoundException e) {
			SecurityUtil.error(resp, SecurityUtil.ERROR_ILLEGALID);
			tx.rollback();
		} catch (IllegalMailException e) {
			SecurityUtil.error(resp, SecurityUtil.ERROR_ILLEGALMAIL);
			tx.rollback();
		} catch (IllegalQuestionException e) {
			SecurityUtil.error(resp, SecurityUtil.ERROR_ILLEGALQUESTION);
			tx.rollback();
		} catch (IllegalAnswerException e) {
			SecurityUtil.error(resp, SecurityUtil.ERROR_ILLEGALANSWER);
			tx.rollback();
		} catch (IllegalIdcardException e) {
			SecurityUtil.error(resp, SecurityUtil.ERROR_ILLEGALIDCARD);
			tx.rollback();
		} catch (NoIdCardException e) {
		    SecurityUtil.error(resp, SecurityUtil.ERROR_NOIDCARD);
            tx.rollback();
		} catch (Exception e){
			log.error(e,e);
			tx.rollback();
		} finally{
			if(tx.isActive())
				tx.commit();
		}

	}
	
	
}
