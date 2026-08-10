package com.pip.server.account.dao;

import org.hibernate.Query;

import com.pip.db.hibernateDAO.GenericHibernateDAO;
import com.pip.server.account.bean.Account;

public class AccountDAO extends GenericHibernateDAO<Account,Integer> {
	
	private static final String GETACCOUNTBYNAME = "from Account a where a.name=:name";
	private static final String GETACCOUNTBYNAMEANDPASSWORD = "from Account a where a.name=:name and a.password=:password";
	private static final String GETACCOUNTBYNAMEANDPASSWORDANDSTATUS = "from Account a where a.name=:name and a.password=:password and a.status=:status";
	private static final String GETACCOUNTNAMEBYID = "select a.name from Account a where a.id=:id";
	
	public Account getAccountByName(String name){
		Query query = getSession().createQuery(GETACCOUNTBYNAME);
		query.setParameter("name", name);
		return (Account)query.uniqueResult();
	}
	
	public String getAccountNameById(int id){
		Query query = getSession().createQuery(GETACCOUNTNAMEBYID);
		query.setInteger("id", id);
		return (String)query.uniqueResult();
	}
	
	public void create(Account account){
		getSession().save(account);
	}
	
	public void update(Account account){
		getSession().update(account);
	}
	
	
}
