package com.pip.server.account.dao;

import org.hibernate.Query;

import com.pip.db.hibernateDAO.GenericHibernateDAO;
import com.pip.server.account.bean.AccountCredit;

public class AccountCreditDAO extends GenericHibernateDAO<AccountCredit,Integer> {
    public AccountCredit getAccountCredit(int id) {
        Query query = getSession().createQuery("from AccountCredit ac where ac.id = :id");
        query.setParameter("id", id);
        AccountCredit ret = (AccountCredit)query.uniqueResult();
        if (ret == null) {
            ret = new AccountCredit();
            ret.setId(id);
            ret.setCredit(0);
            ret.setLogoutTime(null);
            ret.setDayCredit(0);
            getSession().save(ret);
        }
        return ret;
    }

    public void update(AccountCredit account){
		getSession().update(account);
	}
}
