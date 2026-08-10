package com.pip.server.billing.f3g;

import org.hibernate.Query;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class ServerInfoDAO extends GenericHibernateDAO<ServerInfo,Integer> {
	public ServerInfo getByRegionID(int regionID) {
        Transaction tx = null;
        try {
            tx = getSession().beginTransaction();
    	    Query query = getSession().createQuery("from ServerInfo s where s.regionID = :regionid");
    	    query.setParameter("regionid", regionID);
    	    ServerInfo ret = (ServerInfo)query.uniqueResult();
    	    tx.commit();
    	    return ret;
        }  catch (Exception ex) {
            tx.rollback();
            return null;
        }
	}
}
