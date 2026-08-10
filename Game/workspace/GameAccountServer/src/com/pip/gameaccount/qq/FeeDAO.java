package com.pip.gameaccount.qq;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class FeeDAO extends GenericHibernateDAO<Fee, Integer> {
	
	public void createFee(Fee fee){
		getSession().save(fee);
	}
	
}
