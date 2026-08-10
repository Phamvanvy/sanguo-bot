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

public class SecurityStatusServlet extends HttpServlet {

	protected AccountSecurityService service;

	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	protected static Logger log = Logger.getLogger(SecurityStatusServlet.class);
	
	public SecurityStatusServlet(AccountSecurityService service){
		this.service = service;
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setCharacterEncoding("utf-8");
		String type = SecurityUtil.getType(req);
		if(SecurityUtil.TYPE_AWARD.equals(type)){
			resp.getWriter().println("0");
			resp.getWriter().println("0"/*"9900"*/);   //绑定手机
			resp.getWriter().println("0");  //绑定身份证
			resp.getWriter().println("0"); //绑定问题
			resp.getWriter().println("0"/*"9900"*/); //绑定邮箱
			return;
		}
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try {
			int id = SecurityUtil.getId(req);
			AccountSecurity as = service.findAndCreateAccountSecurity(id);
			if(SecurityUtil.TYPE_SIMPLE.equals(type)){
				char[] cs = new char[4];
				cs[0] = as.isPhoneBound()?'1':'0';
				cs[1] = as.isIdcardBound()?'1':'0';
				cs[2] = as.isQnaBound()?'1':'0';
				cs[3] = as.isMailBound()?'1':'0';
				SecurityUtil.ok(resp, new String(cs));
				return;
			}
			if(SecurityUtil.TYPE_BLUR.equals(type)){
				String phone = as.getBindPhone();
				String idcard = as.getIdcard();
				String question = as.getQuestion();
				String mail = as.getMail();
				resp.getWriter().println("0");
				phone = phone==null?"":CommonUtil.blurString(phone, 2, 8);
				idcard = idcard==null?"":CommonUtil.blurString(idcard, 6, idcard.length()-1);
				question = question==null?"":question;
				if(mail==null){
					mail = "";
				}else{
					int index = mail.indexOf('@');
					if(index>1){
						mail = CommonUtil.blurString(mail, 1, index-1);
					}
				}
				resp.getWriter().println(phone);
				resp.getWriter().println(idcard);
				resp.getWriter().println(question);
				resp.getWriter().println(mail);
				return;
			}

			SecurityUtil.error(resp, SecurityUtil.ERROR_REQUEST);
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
			if(tx.isActive())
				tx.commit();
		}
	}
}
