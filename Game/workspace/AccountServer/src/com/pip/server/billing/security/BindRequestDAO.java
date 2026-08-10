package com.pip.server.billing.security;

import java.util.Date;

import org.hibernate.Query;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class BindRequestDAO extends GenericHibernateDAO<BindRequest, Integer> {

	private static final String FINDBYRANDOMSTRING = "from BindRequest a where a.randomString=:randomString and a.createTime>:createtime";
	
	public static final long ONEDAY = 24*3600*1000L;
	public static final long THREEDAY = 3*24*3600*1000L;
	
	public void create(BindRequest request){
		getSession().save(request);
	}
	
	public void update(BindRequest request){
		getSession().update(request);
	}
	
	public BindRequest findPhoneRequestByRandomString(String randomString){
		Query query = getSession().createQuery(FINDBYRANDOMSTRING);
		query.setParameter("randomString", randomString);
		long t = System.currentTimeMillis()-ONEDAY;
		query.setParameter("createtime", new Date(t));
		return (BindRequest)query.uniqueResult();			
	}
	
	public BindRequest findMailRequestByRandomString(String randomString){
		Query query = getSession().createQuery(FINDBYRANDOMSTRING);
		query.setParameter("randomString", randomString);
		long t = System.currentTimeMillis()-THREEDAY;
		query.setParameter("createtime", new Date(t));
		return (BindRequest)query.uniqueResult();			
	}
	
}
