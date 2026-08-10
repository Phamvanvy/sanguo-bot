package com.pip.server.billing.appstore;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class Order_AppStoreDAO extends GenericHibernateDAO<Order_AppStore,Integer>{
	private static final String GETRECENTRECORDS = "from Order_AppStore p where p.accountID = :accountID and p.createTime > :createTime";
	
	public void create(Order_AppStore data){
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
    		getSession().save(data);
    		tx.commit();
        }  catch (Exception ex) {
            ex.printStackTrace();
            tx.rollback();
        }
	}
	
	public Order_AppStore getByID(int id) {
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
    	    Query query = getSession().createQuery("from Order_AppStore d where d.id = :id");
    	    query.setParameter("id", id);
    	    Order_AppStore ret = (Order_AppStore)query.uniqueResult();
    	    tx.commit();
    	    return ret;
        }  catch (Exception ex) {
            tx.rollback();
            return null;
        }
	}
	
	public Order_AppStore getByOrderID(String oid) {
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
            Query query = getSession().createQuery("from Order_AppStore d where d.orderID = :id");
            query.setParameter("id", oid);
            Order_AppStore ret = (Order_AppStore)query.uniqueResult();
            tx.commit();
            return ret;
        }  catch (Exception ex) {
            tx.rollback();
            return null;
        }
    }
	
	public void update(Order_AppStore data){
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
            getSession().update(data);
            tx.commit();
        }  catch (Exception ex) {
            tx.rollback();
        }
	}
	
	public List<Order_AppStore> getRecentRecords(int accountID) {
		Date d = new Date(System.currentTimeMillis() - 3600000L);
		Query query = getSession().createQuery(GETRECENTRECORDS);
		query.setInteger("accountID", accountID);
		query.setDate("createTime", d);
		return query.list();
	}
}
