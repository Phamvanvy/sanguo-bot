package com.pip.datatransfer.dao;

import java.util.List;

import org.hibernate.Query;

import com.pip.datatransfer.bean.Petmanager;
import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class PetmanagerDAO extends GenericHibernateDAO<Petmanager,Integer> {
	private static final String GETALLID = "select a.id from Petmanager a order by a.id";
	private static final String GETPETMANAGERBYID = "from Petmanager a where a.id=:id";
	
	public List getAllId(){
		Query query = getSession().createQuery(GETALLID);

		return (List)query.list();
	}

	public Petmanager getPetmanagerById(int id){
		Query query = getSession().createQuery(GETPETMANAGERBYID);
		query.setInteger("id", id);
		
		return (Petmanager)query.uniqueResult();
	}
	
	public void create(Petmanager petmanager){
		getSession().save(petmanager);
	}
	
	public void update(Petmanager petmanager){
		getSession().update(petmanager);
	}
	
	
}
