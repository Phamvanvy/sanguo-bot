package com.pip.gameaccount.qq;

import org.hibernate.Query;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class QQGameAccountDAO extends GenericHibernateDAO<QQGameAccount,Integer> {
	private static final String GETBYACCOUNTID = "from QQGameAccount g where g.name=:name";
	private static final String GETGAMEACCOUNTNAME = "select g.name from QQGameAccount g where g.id=:id";
	
	public QQGameAccount getGameAccountByAccountId(String accountId){
		Query query = getSession().createQuery(GETBYACCOUNTID);
		query.setString("name", accountId);
		return (QQGameAccount)query.uniqueResult();
	}
	
	public void create(QQGameAccount account){
		getSession().save(account);
	}
	
	public void update(QQGameAccount account){
		getSession().update(account);
	}	
	
	public String getGameAccountName(int id){
		Query query = getSession().createQuery(GETGAMEACCOUNTNAME);
		query.setInteger("id", id);
		return (String)query.uniqueResult();
	}
}
