package com.pip.datatransfer.dao;

import java.util.List;

import org.hibernate.Query;

import com.pip.datatransfer.bean.OldAccount;
import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class OldAccountDAO extends GenericHibernateDAO<OldAccount,Integer> {
	private static final String GETALLID = "select a.id from OldAccount a order by a.id";
	private static final String GETACCOUNTBYID = "from OldAccount a where a.id=:id";
	
	public List getAllId(){
		Query query = getSession().createQuery(GETALLID);

		return (List)query.list();
	}

	public OldAccount getAccountById(int id){
		Query query = getSession().createQuery(GETACCOUNTBYID);
		query.setInteger("id", id);
		
		return (OldAccount)query.uniqueResult();
	}
	
	public void create(OldAccount account){
		getSession().save(account);
	}
	
	public void update(OldAccount account){
		getSession().update(account);
	}
	
	
}
