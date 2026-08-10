package com.pip.server.billing.security;

import org.hibernate.Query;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class AccountSecurityDAO extends GenericHibernateDAO<AccountSecurity, Integer> {

	private static final String GETACCOUNTBYNAME = "from AccountSecurity a where a.name=:name";
	private static final String GETACCOUNTNAMEBYID = "select a.name from AccountSecurity a where a.id=:id";
	private static final String GETACCOUNTBYID = "from AccountSecurity a where a.id=:id";
	
	public AccountSecurity getAccountSecurityByName(String name){
		Query query = getSession().createQuery(GETACCOUNTBYNAME);
		query.setParameter("name", name);
		return (AccountSecurity)query.uniqueResult();		
	}
	
	public AccountSecurity getAccountSecurity(int id){
		Query query = getSession().createQuery(GETACCOUNTBYID);
		query.setInteger("id", id);
		return (AccountSecurity)query.uniqueResult();
	}
	
	public String getAccountNameById(int id){
		Query query = getSession().createQuery(GETACCOUNTNAMEBYID);
		query.setInteger("id", id);
		return (String)query.uniqueResult();
	}
	
	public void create(AccountSecurity account){
		getSession().save(account);
	}
	
	public void update(AccountSecurity account){
		getSession().update(account);
	}

}
