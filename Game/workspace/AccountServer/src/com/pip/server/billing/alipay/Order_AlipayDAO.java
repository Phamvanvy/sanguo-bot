package com.pip.server.billing.alipay;

import org.hibernate.Query;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class Order_AlipayDAO extends GenericHibernateDAO<Order_Alipay,Integer>{
	public void create(Order_Alipay data){
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
	
	public Order_Alipay getByID(int id) {
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
    	    Query query = getSession().createQuery("from Order_Alipay d where d.id = :id");
    	    query.setParameter("id", id);
    	    Order_Alipay ret = (Order_Alipay)query.uniqueResult();
    	    tx.commit();
    	    return ret;
        }  catch (Exception ex) {
            tx.rollback();
            return null;
        }
	}
	
	public Order_Alipay getBySeqID(String seqid) {
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
    	    Query query = getSession().createQuery("from Order_Alipay d where d.paySeq = :paySeq");
    	    query.setParameter("paySeq", seqid);
    	    Order_Alipay ret = (Order_Alipay)query.uniqueResult();
    	    tx.commit();
    	    return ret;
        }  catch (Exception ex) {
            tx.rollback();
            return null;
        }
	}
	public void update(Order_Alipay data){
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
