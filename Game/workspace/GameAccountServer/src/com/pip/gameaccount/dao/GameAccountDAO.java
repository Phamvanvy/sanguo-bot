package com.pip.gameaccount.dao;

import org.hibernate.Query;

import com.pip.db.hibernateDAO.GenericHibernateDAO;
import com.pip.gameaccount.GameAccount;

public class GameAccountDAO extends GenericHibernateDAO<GameAccount, Integer>{
	
	private static final String GETBYNAME = "from GameAccount g where g.name=:name";
	private static final String GETGAMEACCOUNTNAME = "select g.name from GameAccount g where g.id=:id";
	
	public GameAccount getGameAccountByAccountId(String accountId){
		Query query = getSession().createQuery(GETBYNAME);
		query.setString("name", accountId);
		return (GameAccount)query.uniqueResult();
	}
	
	public void create(GameAccount account){
		getSession().save(account);
	}
	
	public void update(GameAccount account){
		getSession().update(account);
	}
	
	public String getGameAccountName(int id){
		Query query = getSession().createQuery(GETGAMEACCOUNTNAME);
		query.setInteger("id", id);
		return (String)query.uniqueResult();
	}
}
