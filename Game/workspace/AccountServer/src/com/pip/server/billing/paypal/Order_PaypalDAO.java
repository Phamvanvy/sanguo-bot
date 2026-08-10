package com.pip.server.billing.paypal;

import org.hibernate.Query;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class Order_PaypalDAO extends GenericHibernateDAO<Order_Paypal,Integer>{
	public void create(Order_Paypal data){
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
	
	public Order_Paypal getByID(int id) {
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
    	    Query query = getSession().createQuery("from Order_Paypal d where d.id = :id");
    	    query.setParameter("id", id);
    	    Order_Paypal ret = (Order_Paypal)query.uniqueResult();
    	    tx.commit();
    	    return ret;
        }  catch (Exception ex) {
            tx.rollback();
            return null;
        }
	}
	
	public Order_Paypal getBySeqID(String seqid) {
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
    	    Query query = getSession().createQuery("from Order_Paypal d where d.paySeq = :paySeq");
    	    query.setParameter("paySeq", seqid);
    	    Order_Paypal ret = (Order_Paypal)query.uniqueResult();
    	    tx.commit();
    	    return ret;
        }  catch (Exception ex) {
            tx.rollback();
            return null;
        }
	}
	public Order_Paypal getByPaypalID(String paypalID) {
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
    	    Query query = getSession().createQuery("from Order_Paypal d where d.paypalID = :paypalid");
    	    query.setParameter("paypalid", paypalID);
    	    Order_Paypal ret = (Order_Paypal)query.uniqueResult();
    	    tx.commit();
    	    return ret;
        }  catch (Exception ex) {
            tx.rollback();
            return null;
        }
	}
	
	public void update(Order_Paypal data){
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
            getSession().update(data);
            tx.commit();
        }  catch (Exception ex) {
            tx.rollback();
        }
	}
}
