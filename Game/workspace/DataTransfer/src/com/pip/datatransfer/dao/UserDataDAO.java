package com.pip.datatransfer.dao;

import java.util.List;

import org.hibernate.Query;

import com.pip.datatransfer.bean.UserData;
import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class UserDataDAO extends GenericHibernateDAO<UserData,Integer> {
	private static final String GETALLID = "select a.id from UserData a order by a.id";
	private static final String GETUSERDATABYID = "from UserData a where a.id=:id";
	
	public List getAllId(){
		Query query = getSession().createQuery(GETALLID);

		return (List)query.list();
	}

	public UserData getUserDataById(int id){
		Query query = getSession().createQuery(GETUSERDATABYID);
		query.setInteger("id", id);
		
		return (UserData)query.uniqueResult();
	}
	
	public void create(UserData userdata){
		getSession().save(userdata);
	}
	
	public void update(UserData userdata){
		getSession().update(userdata);
	}
	
	
}
