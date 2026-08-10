package com.pip.server.billing.card;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class CardDAO extends GenericHibernateDAO<Card,Integer> {
	public void create(Card data) {
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
    		getSession().save(data);
    		tx.commit();
        }  catch (Exception ex) {
            tx.rollback();
        }
	}
	
	public Card getByCardNo(String cardno) {
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
    	    Query query = getSession().createQuery("from Card d where d.cardno = :cardno");
    	    query.setParameter("cardno", cardno);
    	    Card ret = (Card)query.uniqueResult();
    	    tx.commit();
    	    return ret;
        }  catch (Exception ex) {
            tx.rollback();
            return null;
        }
	}
	
	public void update(Card data){
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
            getSession().update(data);
            tx.commit();
        }  catch (Exception ex) {
            tx.rollback();
        }
	}
	
	public boolean findCardByAccount(int gameCode, int cardType, int accountID) {
	    Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
            Query query = getSession().createQuery("from Card d where d.accountID = " + accountID + 
                    " and d.used = true and d.gameCode = " + gameCode + " and d.cardType = " + cardType);
            List list = query.list();
            tx.commit();
            if (list == null || list.size() == 0) {
                return false;
            } else {
                return true;
            }
        }  catch (Exception ex) {
            tx.rollback();
            return false;
        }
	}
	
	public int getMaxID() throws Exception {
		Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
            Query query = getSession().createQuery("select max(id) from Card");
            int ret = Integer.parseInt(query.uniqueResult().toString());
            tx.commit();
            return ret;
        }  catch (Exception ex) {
            tx.rollback();
            throw ex;
        }
	}
}
