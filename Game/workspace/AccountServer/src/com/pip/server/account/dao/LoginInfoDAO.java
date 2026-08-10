package com.pip.server.account.dao;

import java.util.List;

import org.hibernate.Query;

import com.pip.db.hibernateDAO.GenericHibernateDAO;
import com.pip.server.account.bean.LoginInfo;

public class LoginInfoDAO extends GenericHibernateDAO<LoginInfo,Integer> {
	
	private static final String FINDLOGININFOBYACCOUNTID = "from LoginInfo l where l.accountId=:accountId";
	
	public List<LoginInfo> findLoginInfoByAccountId(int accountId){
		Query query = getSession().createQuery(FINDLOGININFOBYACCOUNTID);
		query.setInteger("accountId", accountId);
		return query.list();
	}
}
