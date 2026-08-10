package com.pip.server.billing.umpay;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class GamePayDAO extends GenericHibernateDAO<GamePay,Integer>
{
    protected static Logger log = Logger.getLogger(GamePayDAO.class);
    
    public GamePay create(String mobile, String msg, String gameCode, String payType, String spNumber){
        Transaction tx = null;
        try {
        	GamePay gp = new GamePay();
            gp.setMobile(mobile);
            gp.setMsg(msg);
            gp.setGameCode(gameCode);
            gp.setPayType(payType);
            gp.setSpNumber(spNumber);
            gp.setCreateTime(new Date());
            
            tx = getSession().beginTransaction();
    		getSession().save(gp);
    		tx.commit();
    		return gp;
        }  catch (Exception ex) {
            ex.printStackTrace();
            tx.rollback();
        }
        return null;
	}
}
