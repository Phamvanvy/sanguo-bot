package com.pip.datatransfer.dao;

import java.util.List;

import org.hibernate.Query;

import com.pip.datatransfer.bean.Shop;
import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class ShopDAO extends GenericHibernateDAO<Shop,Integer> {
	private static final String GETALLID = "select a.id from Shop a order by a.id";
	private static final String GETMAILBYID = "from Shop a where a.id=:id";
	
	public List getAllId(){
		Query query = getSession().createQuery(GETALLID);

		return (List)query.list();
	}

	public Shop getShopById(int id){
		Query query = getSession().createQuery(GETMAILBYID);
		query.setInteger("id", id);
		
		return (Shop)query.uniqueResult();
	}
	
	public void create(Shop shop){
		getSession().save(shop);
	}
	
	public void update(Shop shop){
		getSession().update(shop);
	}
	
	
}
