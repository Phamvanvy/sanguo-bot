package com.pip.server.account.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.server.account.bean.Sequence2;

public class Sequence2DAO {

	private static final String GETSEQUENCEBYNAME  = "from Sequence2 s where s.name=:name";
	
	public Sequence2 getSequence(int id){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		return (Sequence2)session.get(Sequence2.class, new Integer(id));
	}
	
	public Sequence2 getSequence(String name){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		Query query = session.createQuery(GETSEQUENCEBYNAME);
		query.setString("name", name);
		return (Sequence2)query.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public Sequence2[] getSequences(){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		Query query = session.createQuery("from Sequence2");
		List<Sequence2> l = query.list();
		Sequence2[] ret = new Sequence2[l.size()];
		l.toArray(ret);
		return ret;
	}
	
	public void save(Sequence2 sequence){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.update(sequence);
	}
}
