package com.pip.server.billing.umpay;

import java.util.Date;

import org.hibernate.Query;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class UMPayDataDAO extends GenericHibernateDAO<UMPayData,Integer>{
	public void create(UMPayData data){
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
    		getSession().save(data);
    		tx.commit();
        }  catch (Exception ex) {
            tx.rollback();
        }
	}
	
	public UMPayData getOrCreate(int id, String name) {
	    Transaction tx = null;
	    try {
    	    tx = getSession().beginTransaction();
    	    Query query = getSession().createQuery("from UMPayData d where d.id = :id");
            query.setParameter("id", id);
            UMPayData data = (UMPayData)query.uniqueResult();
            if (data == null) {
                data = new UMPayData();
                data.setId(id);
                data.setUserName(name);
                data.setPhones("");
                data.setLastModifyTime(new Date());
                getSession().save(data);
            }
            tx.commit();
            return data;
	    }  catch (Exception ex) {
            tx.rollback();
            return null;
        }
	}
	
	public UMPayData getByAccountID(int id) {
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
    	    Query query = getSession().createQuery("from UMPayData d where d.id = :id");
    	    query.setParameter("id", id);
    	    UMPayData ret = (UMPayData)query.uniqueResult();
    	    tx.commit();
    	    return ret;
        }  catch (Exception ex) {
            tx.rollback();
            return null;
        }
	}
	
	public UMPayData getByAccountName(String name) {
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
    	    Query query = getSession().createQuery("from UMPayData d where d.userName = :userName");
            query.setParameter("userName", name);
            UMPayData ret = (UMPayData)query.uniqueResult();
            tx.commit();
            return ret;
        }  catch (Exception ex) {
            tx.rollback();
            return null;
        }
	}
	
	public void update(UMPayData data){
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
