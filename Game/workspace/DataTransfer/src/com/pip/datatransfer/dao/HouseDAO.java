package com.pip.datatransfer.dao;

import java.util.List;

import org.hibernate.Query;

import com.pip.datatransfer.bean.House;
import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class HouseDAO extends GenericHibernateDAO<House,Integer> {
	private static final String GETALLID = "select a.id from House a order by a.id";
	private static final String GETHOUSEBYID = "from House a where a.id=:id";
	
	public List getAllId(){
		Query query = getSession().createQuery(GETALLID);

		return (List)query.list();
	}

	public House getHouseById(int id){
		Query query = getSession().createQuery(GETHOUSEBYID);
		query.setInteger("id", id);
		
		return (House)query.uniqueResult();
	}
	
	public void create(House house){
		getSession().save(house);
	}
	
	public void update(House house){
		getSession().update(house);
	}
	
	
}
