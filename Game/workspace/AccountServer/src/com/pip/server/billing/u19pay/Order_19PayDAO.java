package com.pip.server.billing.u19pay;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class Order_19PayDAO extends GenericHibernateDAO<Order_19Pay,Integer>{
	private static final String GETRECENTRECORDS = "from Order_19Pay p where p.accountID = :accountID and p.createTime > :createTime";
	
	public void create(Order_19Pay data){
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
	
	public Order_19Pay getByID(int id) {
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
    	    Query query = getSession().createQuery("from Order_19Pay d where d.id = :id");
    	    query.setParameter("id", id);
    	    Order_19Pay ret = (Order_19Pay)query.uniqueResult();
    	    tx.commit();
    	    return ret;
        }  catch (Exception ex) {
            tx.rollback();
            return null;
        }
	}
	
	public void update(Order_19Pay data){
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
            getSession().update(data);
            tx.commit();
        }  catch (Exception ex) {
            tx.rollback();
        }
	}
	
	public List<Order_19Pay> getRecentRecords(int accountID) {
		Date d = new Date(System.currentTimeMillis() - 3600000L);
		Query query = getSession().createQuery(GETRECENTRECORDS);
		query.setInteger("accountID", accountID);
		query.setDate("createTime", d);
		return query.list();
	}
}
