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

public class GetBackSecurityServlet extends HttpServlet {
	
	protected AccountSecurityService service;
	
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	protected static Logger log = Logger.getLogger(GetBackSecurityServlet.class);
	
	public GetBackSecurityServlet(AccountSecurityService service){
		this.service = service;
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setCharacterEncoding("utf-8");
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try {
			int id = SecurityUtil.getId(req);
			String type = SecurityUtil.getType(req);
//			if(SecurityUtil.TYPE_PHONE.equals(type)){
//				service.getbackByPhone(id);
//				SecurityUtil.ok(resp, null);
//				return;
//			}
			if(SecurityUtil.TYPE_MAIL.equals(type)){
				service.getbackByMail(id);
				SecurityUtil.ok(resp, null);
				return;
			}
			if(SecurityUtil.TYPE_QNA.equals(type)){
				String question = SecurityUtil.getQuestion(req);
				String answer = SecurityUtil.getAnswer(req);
				if(question==null){
					SecurityUtil.error(resp, SecurityUtil.ERROR_ILLEGALQUESTION);
					return;
				}
				if(answer==null){
					SecurityUtil.error(resp, SecurityUtil.ERROR_ILLEGALANSWER);
					return;
				}
				String password = service.getbackByQnA(id, question, answer);
				SecurityUtil.ok(resp, password);
				return;
			}
		} catch (IllegalIdException e) {
			SecurityUtil.error(resp, SecurityUtil.ERROR_ILLEGALID);
			tx.rollback();
		} catch (AccountNotFoundException e) {
			SecurityUtil.error(resp, SecurityUtil.ERROR_ILLEGALID);
			tx.rollback();
		} catch (BindStatusException e) {
			SecurityUtil.error(resp, SecurityUtil.ERROR_BINDSTATUS);
			tx.rollback();
		} catch (NotEnoughPaymentException e) {
			SecurityUtil.error(resp, SecurityUtil.ERROR_NOTENOUGHPAYMENT);
			tx.rollback();
		} catch (VerificationException e){
			SecurityUtil.error(resp, SecurityUtil.ERROR_VERIFICATION_QNA);
			tx.rollback();			
		}
		catch (Exception e){
			tx.rollback();
		} finally{
			if(tx.isActive())
				tx.commit();
		}
		
	}
}
