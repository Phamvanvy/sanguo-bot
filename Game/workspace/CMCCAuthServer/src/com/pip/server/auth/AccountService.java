package com.pip.server.auth;

import java.util.*;

import com.pip.server.auth.bean.Account;
import com.pip.server.auth.dao.AccountDao;
import com.pip.server.auth.dao.DataAccessException;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoAcceptor;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线帐号管理服务。
 */
public class AccountService implements Runnable {
    private static final Logger log = Logger.getLogger(AccountService.class);

    /*
     * 在线用户表
     */
    private Map<Integer, AccountState> clients = new ConcurrentHashMap<Integer, AccountState>();

    private AccountDao accountDao;
    private IoAcceptor acceptor;

    public AccountService(AccountDao accountDao) {
        this.accountDao = accountDao;
        // 未开启帐号超时检查功能
        // new Thread(this).start();
    }

    public void setAcceptor(IoAcceptor acceptor) {
        this.acceptor = acceptor;
    }

    public void stop() {
        acceptor.unbindAll();
    }

    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    /**
     * 根据帐号ID载入帐号。
     * @param accountId
     * @return
     */
    public Account loadAccountById(int accountId) {
        try {
            return accountDao.getAccountById(accountId);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    /**
     * 根据帐号名称查找帐号。
     * @param name
     * @return
     */
    public Account loadAccountByName(String name) {
        try {
            return accountDao.getAccountByName(name);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    /**
     * 根据帐号名称和密码查找帐号。
     * @param accountName
     * @param password
     * @return
     */
    public Account loadAccountByNameAndPassword(String accountName, String password) {
        try {
            return accountDao.getAccountByNameAndPassword(accountName, password);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    /**
     * 根据帐号ID查找帐号名称。
     * @param accountId
     * @return
     */
    public String getAccountName(int accountId) {
        try {
            return accountDao.getAccountName(accountId);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    /**
     * 创建新帐号。
     * @param name 名称
     * @param password 密码
     * @param guardpass 保护密码
     * @param phone 注册手机号 
     * @param recommend 推荐人
     * @param iMoney 初始i币
     * @param cause 注册额外信息
     * @param valid 是否有效
     * @param channel 版本+渠道
     * @param modifyNameTimes 允许修改名称次数
     * @param modifyPasswordTimes 允许修改密码次数
     * @param model 机型
     * @param gameCode 游戏区ID
     * @return
     */
    public Account createNewAccount(String name, String password, String guardpass, String phone, String recommend,
            int iMoney, String cause, boolean valid, String channel, int modifyNameTimes, int modifyPasswordTimes,
            String model, String gameCode) {
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
            account.setValid(valid);
            
            // 渠道号（版本）的格式为：2.2-15001000-下载序列号（最后的下载序列号可能为空），
            // 这里把下载序列号保存到cause字段中去。
            String[] secs = channel.split("-");
            if (secs.length == 3) {
                account.setChannel(secs[0] + "-" + secs[1]);
                account.setCause(cause + secs[2]);
            } else {
                account.setChannel(channel);
                account.setCause(cause);
            }
            
            account.setModifyNameTimes(modifyNameTimes);
            account.setModifyPasswordTimes(modifyPasswordTimes);
            account.setModel(model);
            account.setGameCode(gameCode);
            account.setActivePhone(phone);
            accountDao.makePersistent(account);
            return account;
        } catch (DataAccessException ex) {
            log.error(ex, ex);
            return null;
        }
    }

    /**
     * 把一个帐号的当前数据保存到数据库。
     * @param account
     */
    public void saveAccount(Account account) {
        try {
            accountDao.makePersistent(account);
        } catch (DataAccessException ex) {
        }
    }

    /**
     * 登录超时检查线程。
     */
    public void run() {
        while (true) {
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

    /*
     * 检查哪些用户超时了。
     */
    private void checkTimeOut() {
        synchronized (this) {
            Iterator<AccountState> ite = clients.values().iterator();
            while (ite.hasNext()) {
                AccountState account = ite.next();
                if ((account.lastLiveTime() + 10 * 1000 * 60L) < System.currentTimeMillis()) {
                    unRegistry(account);
                    log.info("ACCOUNTID[" + account.getId() + "]TIMEOUT");
                }
            }
        }
    }

    /**
     * 判断一个用户是否在线。
     * @param accountId 用户ID
     * @return
     */
    public boolean contains(int accountId) {
        return clients.containsKey(new Integer(accountId));
    }

    /**
     * 注册一个在线用户。
     * @param account
     */
    public void registry(AccountState account) {
        synchronized (this) {
            clients.put(new Integer(account.getId()), account);
        }
    }

    /**
     * 注销一个用户。
     * @param account
     */
    public void unRegistry(AccountState account) {
        synchronized (this) {
            clients.remove(new Integer(account.getId()));
        }
    }

    /**
     * 根据ID查找在线用户信息。
     * @param accountId
     * @return
     */
    public AccountState getAccount(int accountId) {
        return (AccountState) clients.get(new Integer(accountId));
    }

    /**
     * 查找同一手机号注册的用户的数量。
     * @param phone
     * @return
     */
    public int getAccountCountByPhone(String phone) {
        try {
            return accountDao.getAccountCountByPhone(phone);
        } catch (DataAccessException ex) {
            return -1;
        }
    }

    /**
     * 根据名字查询帐号ID。
     * @param name
     * @return 如果帐号不存在，返回-1
     */
    public int getAccountId(String name) {
        try {
            return accountDao.getAccountId(name);
        } catch (DataAccessException ex) {
            return -1;
        }
    }

    /**
     * 根据包月手机号查找帐号ID。
     * @param phone
     * @return
     */
    public int getAccountIdBySubscribePhone(String phone) {
        try {
            return accountDao.getAccountIdBySubscribePhone(phone);
        } catch (DataAccessException ex) {
            return -1;
        }
    }

    /**
     * 查找一个手机号注册的第一个合法帐号。
     * @param phone
     * @return
     */
    public Account getFirstValidAccountByPhone(String phone) {
        try {
            return accountDao.getFirstValidAccountByPhone(phone);
        } catch (DataAccessException ex) {
            return null;
        }
    }
}
