package com.pip.server.account.dao;

import org.hibernate.Session;

import com.pip.db.hibernateDAO.GenericHibernateDAO;
import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.server.account.bean.Sequence;

public class SequenceDAO extends GenericHibernateDAO<Sequence,Integer> {
	
	public Sequence getSequence(int id){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		return (Sequence)session.get(Sequence.class, new Integer(id));
	}
}
