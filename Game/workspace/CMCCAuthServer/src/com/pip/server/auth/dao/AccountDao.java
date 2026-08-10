package com.pip.server.auth.dao;

import java.util.List;

import com.pip.server.auth.bean.Account;

public class AccountDao extends BaseDao {


    public AccountDao() {
        super();
    }

    public Account getAccountByNameAndPassword(String name, String password) throws
            DataAccessException {
        String sql = "from Account b where b.userName='" + name +
                     "' and b.password='" + password + "'";
        Account account = (Account) uniqueResult(sql);
        return account;
    }

    public Account getAccountById(int id) throws DataAccessException {
        return (Account)super.getObject(Account.class, new Integer(id));
    }

    public Account getAccountByName(String name) throws DataAccessException {
        String sql = "from Account b where b.userName='" + name + "'";
        return (Account) uniqueResult(sql);
    }

    public void addAccount(Account account) throws DataAccessException {
        makePersistent(account);
    }


    public void deleteAccount(Account account) throws DataAccessException {
        makeTransient(account);
    }

    public int getAccountCountByPhone(String phone) throws DataAccessException{
        return getCount("from Account b where b.phone='"+phone+"'");
    }

    public Account getFirstValidAccountByPhone(String phone) throws DataAccessException{
        List l = getList("from Account b where b.phone='"+phone+"' and b.valid=true order by b.createTime desc");
        if(l.size()==0)
            return null;
        return (Account)l.get(0);
    }

    public int getAccountId(String name) throws DataAccessException{
        String sql = "select b.id from Account b where b.userName='"+name+"'";
        Integer ret = (Integer)uniqueResult(sql);
        if(ret==null)
            return -1;
        return ret.intValue();
    }


    public String getAccountName(int accountId) throws DataAccessException {
        String sql = "select b.userName from Account b where b.id=" +accountId;
        String ret = (String) uniqueResult(sql);
        return ret;
    }

    public int getAccountIdBySubscribePhone(String phone) throws DataAccessException {
        String sql = "select b.id from Account b where b.subscribePhone='" + phone
        	+ "' and b.subscribeStatus = " + Account.SUBSCRIBED;
        java.util.List l = getLimitedList(sql, 0, 1);
        if (l == null || l.size() == 0) {
        	return -1;
        } else {
        	return ((Integer)l.get(0)).intValue();
        }
    }


}
