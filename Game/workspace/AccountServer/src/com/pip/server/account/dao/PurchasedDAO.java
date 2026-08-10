package com.pip.server.account.dao;

import java.util.List;

import org.hibernate.Query;

import com.pip.db.hibernateDAO.GenericHibernateDAO;
import com.pip.server.account.bean.Purchased;

public class PurchasedDAO extends GenericHibernateDAO<Purchased,Integer> {

	private static final String GETPURCAHSEDBYACCOUNTID = "from Purchased p where p.accountId=:accountId and status=1";
	private static final String GETPURCHASEDBYCODEANDPHONE = "from Purchased p where p.code=:code and p.phone=:phone and status=1";
	
	public List<Purchased> getPurchased(int accountId){
		Query query = getSession().createQuery(GETPURCAHSEDBYACCOUNTID);
		query.setInteger("accountId", accountId);
		return query.list();
	}
	
	public Purchased getPurchased(int code,String phone){
		Query query = getSession().createQuery(GETPURCHASEDBYCODEANDPHONE);
		query.setInteger("code", code);
		query.setString("phone", phone);
		return (Purchased)query.uniqueResult();
	}
	
	public void save(Purchased purchased){
		getSession().save(purchased);
	}
	
	public void update(Purchased purchased){
		getSession().update(purchased);
	}
}
