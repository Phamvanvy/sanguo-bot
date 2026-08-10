package com.pip.datatransfer.dao;

import java.util.List;

import org.hibernate.Query;

import com.pip.datatransfer.bean.Acution;
import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class AcutionDAO extends GenericHibernateDAO<Acution,Integer> {
	private static final String GETALLID = "select a.id from Acution a order by a.id";
	private static final String GETACUTIONBYID = "from Acution a where a.id=:id";
	
	public List getAllId(){
		Query query = getSession().createQuery(GETALLID);

		return (List)query.list();
	}

	public Acution getAcutionById(int id){
		Query query = getSession().createQuery(GETACUTIONBYID);
		query.setInteger("id", id);
		
		return (Acution)query.uniqueResult();
	}
	
	public void create(Acution acution){
		getSession().save(acution);
	}
	
	public void update(Acution acution){
		getSession().update(acution);
	}
	
	
}
