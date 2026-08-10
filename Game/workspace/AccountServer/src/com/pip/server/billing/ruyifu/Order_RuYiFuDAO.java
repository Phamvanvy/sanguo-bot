package com.pip.server.billing.ruyifu;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class Order_RuYiFuDAO extends GenericHibernateDAO<Order_RuYiFu,Integer>{
	private static final String GETRECENTRECORDS = "from Order_RuYiFu p where p.accountID = :accountID and p.createTime > :createTime";
	
	public void create(Order_RuYiFu data){
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
	
	public Order_RuYiFu getByID(int id) {
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
    	    Query query = getSession().createQuery("from Order_RuYiFu d where d.id = :id");
    	    query.setParameter("id", id);
    	    Order_RuYiFu ret = (Order_RuYiFu)query.uniqueResult();
    	    tx.commit();
    	    return ret;
        }  catch (Exception ex) {
            tx.rollback();
            return null;
        }
	}
	
	public Order_RuYiFu getByOrderID(String oid) {
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
            Query query = getSession().createQuery("from Order_RuYiFu d where d.orderID = :id");
            query.setParameter("id", oid);
            Order_RuYiFu ret = (Order_RuYiFu)query.uniqueResult();
            tx.commit();
            return ret;
        }  catch (Exception ex) {
            tx.rollback();
            return null;
        }
    }
	
	public void update(Order_RuYiFu data){
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
            getSession().update(data);
            tx.commit();
        }  catch (Exception ex) {
            tx.rollback();
        }
	}
	
	public List<Order_RuYiFu> getRecentRecords(int accountID) {
		Date d = new Date(System.currentTimeMillis() - 3600000L);
		Query query = getSession().createQuery(GETRECENTRECORDS);
		query.setInteger("accountID", accountID);
		query.setDate("createTime", d);
		return query.list();
	}
}
