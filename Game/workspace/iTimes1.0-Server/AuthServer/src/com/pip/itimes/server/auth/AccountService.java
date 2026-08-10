package com.pip.itimes.server.auth;

import java.util.*;

import com.pip.itimes.server.bean.Account;
import com.pip.itimes.server.dao.AccountDao;
import com.pip.itimes.server.dao.DataAccessException;
import org.apache.log4j.Logger;
import org.apache.mina.common.IoAcceptor;
import java.util.concurrent.ConcurrentHashMap;


public class AccountService implements Runnable {

    private static final Logger log = Logger.getLogger(AccountService.class);
    private ConcurrentHashMap clients = new ConcurrentHashMap();
    private AccountDao accountDao;
    private IoAcceptor acceptor;

    private BillingService billingService = null;


    public AccountService(AccountDao accountDao) {
        this.accountDao = accountDao;
//        new Thread(this).start();
    }

    public void setAcceptor(IoAcceptor acceptor){
        this.acceptor = acceptor;
    }

    public void stop(){
        acceptor.unbindAll();
    }

    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public Account loadAccountById(int accountId){
        try {
            return accountDao.getAccountById(accountId);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public Account loadAccountByName(String name){
        try {
            return accountDao.getAccountByName(name);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public Account loadAccountByNameAndPassword(String accountName,String password){
        try {
            return accountDao.getAccountByNameAndPassword(accountName, password);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public String getAccountName(int accountId){
        try {
            return accountDao.getAccountName(accountId);
        } catch (DataAccessException ex) {
            return null;
        }
    }


    public Account createNewAccount(String name, String password,
                                    String guardpass, String phone,
                                    String recommend, int iMoney, String cause,
                                    boolean valid, String channel,
                                    int modifyNameTimes,
                                    int modifyPasswordTimes,String model,String gameCode) {
        try {
            Account account = new Account();
            account.setUserName(name);
            account.setPassword(password);
            account.setGuardpass(guardpass);
            account.setBalance(0);
            account.setPhone(phone);
            account.setCreateTime(new Date());
            account.setValid(true);
            account.setRecommend(recommend);
            account.setiMoney(iMoney);
            account.setMonthFee(0);
            account.setCause(cause);
            account.setValid(valid);
            account.setChannel(channel);
            account.setModifyNameTimes(modifyNameTimes);
            account.setModifyPasswordTimes(modifyPasswordTimes);
            account.setModel(model);
            account.setGameCode(gameCode);
//            account.setiMoney(iMoney);
            accountDao.makePersistent(account);
            return account;
        } catch (DataAccessException ex) {
            log.error(ex, ex);
            return null;
        }
    }

//    public Account createNewAccount(String name, String password,
//                                    String guardpass) {
//        try {
//            Account account = new Account();
//            account.setUserName(name);
//            account.setPassword(password);
//            account.setGuardpass(guardpass);
//            account.setBalance(0);
//            account.setPhone("");
//            account.setCreateTime(new Date());
//            account.setValid(true);
//            account.setRecommend("");
//            account.setiMoney(0);
//            account.setMonthFee(0);
//            accountDao.makePersistent(account);
//            return account;
//        } catch (DataAccessException ex) {
//            return null;
//        }
//    }

    public void logout(String name) {

    }

    public void checkBalance() {
        Collection c = clients.values();
        Iterator ite = c.iterator();
        while (ite.hasNext()) {
            ConnectSession stub = (ConnectSession) ite.next();
            stub.checkBalance();
        }
    }


    public void saveAccount(Account account){
        try {
            accountDao.makePersistent(account);
        } catch (DataAccessException ex) {
        }
    }

    public void run() {
        while(true){
            try {
                Thread.sleep(2 * 1000 * 60L);
            } catch (InterruptedException ex1) {
            }
            try {
                checkTimeOut();
            } catch (Exception ex) {
            }
        }
    }

    private void checkTimeOut(){
        synchronized(this){
            Iterator ite = clients.values().iterator();
            while (ite.hasNext()) {
                AccountState account = (AccountState) ite.next();
                if ((account.lastLiveTime() + 10 * 1000 * 60L) <
                    System.currentTimeMillis()) {
//            if((account.lastLiveTime()+3*1000*60L)<System.currentTimeMillis()){
                    unRegistry(account);
                    log.info("ACCOUNTID[" + account.getId() + "]TIMEOUT");
                }
            }
        }
    }

    public boolean contains(int accountId){
        return clients.containsKey(new Integer(accountId));
    }

    public void registry(AccountState account) {
        synchronized(this){
            clients.put(new Integer(account.getId()), account);
        }
    }

    public void unRegistry(AccountState account) {
        synchronized(this){
            clients.remove(new Integer(account.getId()));
        }
    }

    public AccountState getAccount(int accountId){
        return (AccountState)clients.get(new Integer(accountId));
    }

    public int getAccountCountByPhone(String phone){
        try {
            return accountDao.getAccountCountByPhone(phone);
        } catch (DataAccessException ex) {
            return -1;
        }
    }

    public int getAccountId(String name){
        try {
            return accountDao.getAccountId(name);
        } catch (DataAccessException ex) {
            return -1;
        }
    }

    public int getAccountIdBySubscribePhone(String phone) {
        try {
            return accountDao.getAccountIdBySubscribePhone(phone);
        } catch (DataAccessException ex) {
            return -1;
        }
    }

    public Account getFirstValidAccountByPhone(String phone){
        try {
            return accountDao.getFirstValidAccountByPhone(phone);
        } catch (DataAccessException ex) {
            return null;
        }
    }
}
