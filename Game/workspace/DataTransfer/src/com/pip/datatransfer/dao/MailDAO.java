package com.pip.datatransfer.dao;

import java.util.List;

import org.hibernate.Query;

import com.pip.datatransfer.bean.Mail;
import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class MailDAO extends GenericHibernateDAO<Mail,Integer> {
	private static final String GETALLID = "select a.id from Mail a order by a.id";
	private static final String GETMAILBYID = "from Mail a where a.id=:id";
	
	public List getAllId(){
		Query query = getSession().createQuery(GETALLID);

		return (List)query.list();
	}

	public Mail getMailById(int id){
		Query query = getSession().createQuery(GETMAILBYID);
		query.setInteger("id", id);
		
		return (Mail)query.uniqueResult();
	}
	
	public void create(Mail mail){
		getSession().save(mail);
	}
	
	public void update(Mail mail){
		getSession().update(mail);
	}
	
	
}
