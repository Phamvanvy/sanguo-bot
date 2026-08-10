package com.pip.server.account;

import java.util.Date;
import java.util.List;
import java.util.Random;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.exception.ConstraintViolationException;

import com.pip.server.account.bean.Account;
import com.pip.server.account.bean.Balance;
import com.pip.server.account.bean.Fee;
import com.pip.server.account.bean.LoginInfo;
import com.pip.server.account.bean.Purchased;
import com.pip.server.account.bean.RecommendRequest;
import com.pip.server.account.bean.RecommendReward;
import com.pip.server.account.dao.AccountDAO;
import com.pip.server.account.dao.LoginInfoDAO;
import com.pip.server.account.dao.PurchasedDAO;
import com.pip.server.account.dao.RecommendRequestDAO;
import com.pip.server.account.dao.RecommendRewardDAO;
import com.pip.server.account.stub.UWAPSocketStub;
import com.pip.server.account.util.Util;

public class AccountService {

	private final AccountDAO accountDao;
	private final LoginInfoDAO loginInfoDAO;
	private final PurchasedDAO purchasedDAO;
	private FeeService feeService;

	// private AccountEntityCache cache = new AccountEntityCache();

	private final Cache cache;

	private Object[] locks;
	private final int lockCount;

	private final ISessionIdGenerator idGenerator = new UUIDSessionIdGenerator();
	private final ISourceManager sourceManager = null;
	private final IStringValidator nameValidator;
	private final IStringValidator passwordValidator;
	private final IStringValidator phoneValidator;
	
	private IAccountServiceListener listener;

	private static final Logger log = Logger.getLogger(AccountService.class);

	public AccountService(AccountDAO dao, LoginInfoDAO loginInfoDAO,
			PurchasedDAO purchasedDAO, Cache cache, IStringValidator validator,
			IStringValidator passwordValidator,
			IStringValidator phoneValidator, int lockCount) {
		this.accountDao = dao;
		this.loginInfoDAO = loginInfoDAO;
		this.purchasedDAO = purchasedDAO;
		this.lockCount = lockCount;
		this.cache = cache;
		this.nameValidator = validator;
		this.passwordValidator = passwordValidator;
		this.phoneValidator = phoneValidator;
		createLocks(lockCount);
	}
	
	public void setListener(IAccountServiceListener l) {
		listener = l;
	}
	
	public void setFeeService(FeeService fs) {
	    feeService = fs;
	}

	protected void createLocks(int count) {
		locks = new Object[count];
		for (int i = 0; i < locks.length; i++) {
			locks[i] = new Object();
		}
	}

	protected Object getLock(int index) {
		return locks[index];
	}

	protected Object getLock(String s) {
		int index = Math.abs(s.trim().toUpperCase().hashCode()) % lockCount;
		return getLock(index);
	}

	protected void createAccount(Account account) throws CreateAccountException {
		int nameValid = nameValidator.valid(account.getName());
		if (nameValid == IStringValidator.NULL)
			throw new CreateAccountException(Errors.NULL_NAME);
		if (nameValid == IStringValidator.MIN_LIMIT
				|| nameValid == IStringValidator.MAX_LIMIT)
			throw new CreateAccountException(Errors.ILLEGAL_LENGTH);
		if (nameValid == IStringValidator.ERROR_PATTERN
				|| nameValid == IStringValidator.ILLEGAL_CHAR)
			throw new CreateAccountException(Errors.ILLEGAL_NAME_CHAR);
		if (account.getPhone() != null && account.getPhone().length() != 0) {
			int result = phoneValidator.valid(account.getPhone());
			if (result != IStringValidator.OK)
				throw new CreateAccountException(Errors.ILLEGAL_PHONE);
		}
		if (account.getPassword() == null)
			throw new CreateAccountException(Errors.NULL_PASSWORD);
		if (account.getCreateGameCode() == null)
			throw new CreateAccountException(Errors.NULL_GAMECODE);
		if (account.getCreateTime() == null)
			account.setCreateTime(new Date());

		try {
			accountDao.create(account);
		} catch (RuntimeException e) {
		    log.error(e, e);
			if (e instanceof ConstraintViolationException) {
				throw new CreateAccountException(Errors.DUPLICATE_NAME);
			}
			throw new CreateAccountException(Errors.UNKNOW);
		}

	}

	protected Account createAccount(String name, String password, int balance,
			int status, String gamecode) throws CreateAccountException {
		Account account = new Account();
		account.setName(name);
		account.setPasswordEnc(password);
		Balance b = new Balance(balance, 0);
		account.setBalance(b);
		account.setStatus(status);
		account.setCreateGameCode(gamecode);
		account.setCreateTime(new Date());
		createAccount(account);
		return account;
	}

	protected Account getAccount(String name) {
		return accountDao.getAccountByName(name);
	}

	protected Account getAccountById(int id) {
		return accountDao.findById(id, false);
	}

	protected void historyLoginInfo(LoginInfo loginInfo) {

	}

	protected void updateAccountEntity(AccountEntity ae) {
		accountDao.update(ae.getAccount());
	}

	protected void updateAccountEntity(AccountEntity ae, Date current, long value) {
		if (ae.getLastPayTime() == null) {
			ae.setLastPayTime(current);
			ae.setMonthFee(value);
		} else {
			if (!Util.inLaterMonth(ae.getLastPayTime(), current)) {
				ae.setLastMonthFee(ae.getMonthFee());
				ae.setMonthFee(0);
			}
			ae.setLastPayTime(current);
			ae.setMonthFee(ae.getMonthFee() + value);

		}
		accountDao.update(ae.getAccount());
	}
	
	public AccountEntity resetPassword(String name,String phone) throws ModifyException{
		Object lock = getLock(name);
		synchronized (lock) {
			AccountEntity ae = getAccountEntity(name);
			if(ae == null)
				throw new ModifyException(Errors.UNKNOW_ACCOUNT);
			if(ae.getPhone()!=null&&ae.getPhone().equals(phone)){
				String password = getPassword(rnd);
				ae.setPassword(password);
				updateAccountEntity(ae);
				return ae;
			}else
				throw new ModifyException(Errors.ERROR_OLD_PHONE);
			
		}
	}
	
	public AccountEntity resetPassword(String name,int pay) throws ModifyException,BalanceException{
		Object lock = getLock(name);
		synchronized (lock) {
			AccountEntity ae = getAccountEntity(name);
			if (ae == null)
				throw new ModifyException(Errors.UNKNOW_ACCOUNT);
			Balance oldBalance = ae.getBalance();
			Balance b = oldBalance.decABalance(pay);
			ae.setBalance(b);
			String password = getPassword(rnd);
			ae.setPassword(password);
			updateAccountEntity(ae, new Date(), oldBalance.getValue()
					- b.getValue());
			return ae;
		}
	}

	public LoginResult login(String name, String password, String phone, String serviceId)
			throws LoginException {
		Object lock = getLock(name);
		synchronized (lock) {
			try {
				AccountEntity ae = check0(name, password, phone);
				String sessionId = idGenerator.getSessionId();
				Date current = new Date();
				ae.setLastLoginTime(current);
				LoginInfo li = ae.getLoginInfo(serviceId);
				if (li != null) {
					if (li.isValid()
							&& ((current.getTime() - li.getLoginTime()
									.getTime()) < 30000L))
						throw new LoginException(Errors.TIME_LIMIT);
					li.setLoginTime(current);
					li.setSessionId(sessionId);
					li.setValid(true);
				} else {
					li = new LoginInfo(ae.getId(), current, serviceId,
							sessionId, true);
					ae.addLoginInfo(li);
				}
				loginInfoDAO.makePersistent(li);
				return new LoginResult(ae, li);
			} catch (HibernateException e) {
				e.printStackTrace();
				throw new LoginException(Errors.UNKNOW);
			}
		}
	}

	public AccountEntity logout(String name, String key, String serviceId)
			throws LogoutException {
		Object lock = getLock(name);
		synchronized (lock) {

			try {
				AccountEntity ae = getAccountEntity(name);
				if (ae == null)
					throw new LogoutException(Errors.UNKNOW_ACCOUNT);
				LoginInfo li = ae.getLoginInfo(serviceId);
				if (li == null)
					throw new LogoutException(Errors.UNKOWN_SOURCE);
				li.setValid(false);
				loginInfoDAO.makePersistent(li);
				if (!ae.isLogon()) {
					updateAccountEntity(ae);
				}
				return ae;
			} catch (RuntimeException e) {
				e.printStackTrace();
				throw new LogoutException(Errors.UNKNOW);
			}

		}
	}

	public void addABalance(String name, int value) {
		Object lock = getLock(name);
		synchronized (lock) {
			AccountEntity ae = getAccountEntity(name);
			if (ae != null) {
				Balance b = ae.getBalance().addABalance(value);
				ae.setBalance(b);
				updateAccountEntity(ae);
			}
		}
	}
	

	public void addBBalance(String name, int value) {
		Object lock = getLock(name);
		synchronized (lock) {
			AccountEntity ae = getAccountEntity(name);
			if (ae != null) {
				Balance b = ae.getBalance().addBBalance(value);
				ae.setBalance(b);
				updateAccountEntity(ae);
			}
		}
	}

	public int addCBalance(String name, int value) throws BalanceException{
		if(value<0)
			throw new BalanceException(Errors.ILLEGAL_VALUE);
		Object lock = getLock(name);
		synchronized (lock) {
			AccountEntity ae = getAccountEntity(name);
			if(ae == null)
				throw new BalanceException(Errors.UNKNOW_ACCOUNT);
			ae.setCbalance(ae.getCbalance() + value);
			updateAccountEntity(ae);
			return ae.getCbalance();
		}
	}
	
	public int decCbalance(String name, int value) throws BalanceException{
		if(value<0)
			throw new BalanceException(Errors.ILLEGAL_VALUE);
		Object lock = getLock(name);
		synchronized (lock) {
			AccountEntity ae = getAccountEntity(name);
			if(ae == null)
				throw new BalanceException(Errors.UNKNOW_ACCOUNT);
			if(ae.getCbalance()<value)
				throw new BalanceException(Errors.NOT_ENOUGH_BALANCE);
			ae.setCbalance(ae.getCbalance()-value);
			updateAccountEntity(ae);
			return ae.getCbalance();
		}
	}
	
	public Balance[] decABalance(String name, String sessionId,
			String serviceId, int value) throws BalanceException {
		if (value < 0)
			throw new BalanceException(Errors.ILLEGAL_VALUE);
		Object lock = getLock(name);
		Balance[] ret = new Balance[2];
		synchronized (lock) {
			AccountEntity ae = getAccountEntity(name);
			if (ae == null)
				throw new BalanceException(Errors.UNKNOW_ACCOUNT);
			LoginInfo li = ae.getLoginInfo(serviceId);
			if (li == null)
				throw new BalanceException(Errors.ILLEGAL_SESSIONID);
			if (!li.isValid())
				throw new BalanceException(Errors.NO_LOGON);
			if (!li.getSessionId().equals(sessionId)) {
				throw new BalanceException(Errors.ILLEGAL_SESSIONID);
			}
			Balance oldBalance = ae.getBalance();
			Balance b = oldBalance.decABalance(value);
			ae.setBalance(b);
			updateAccountEntity(ae, new Date(), oldBalance.getValue()
					- b.getValue());
			ret[0] = oldBalance;
			ret[1] = b;
			return ret;
		}
	}

	public Balance[] decABalance(String name, int value)
			throws BalanceException {
		Object lock = getLock(name);
		Balance[] ret = new Balance[2];
		synchronized (lock) {
			AccountEntity ae = getAccountEntity(name);
			if (ae == null)
				throw new BalanceException(Errors.UNKNOW_ACCOUNT);
			Balance oldBalance = ae.getBalance();
			Balance b = oldBalance.decABalance(value);
			ae.setBalance(b);
			updateAccountEntity(ae, new Date(), oldBalance.getValue()
					- b.getValue());
			ret[0] = oldBalance;
			ret[1] = b;
			return ret;
		}
	}

	public Balance[] decBBalance(String name, String sessionId,
			String serviceId, int value) throws BalanceException {
		if (value < 0)
			throw new BalanceException(Errors.ILLEGAL_VALUE);
		Object lock = getLock(name);
		Balance[] ret = new Balance[2];
		synchronized (lock) {
			AccountEntity ae = getAccountEntity(name);
			if (ae == null)
				throw new BalanceException(Errors.UNKNOW_ACCOUNT);
			LoginInfo li = ae.getLoginInfo(serviceId);
			if (li == null)
				throw new BalanceException(Errors.ILLEGAL_SESSIONID);
			if (!li.isValid())
				throw new BalanceException(Errors.NO_LOGON);
			if (!li.getSessionId().equals(sessionId)) {
				throw new BalanceException(Errors.ILLEGAL_SESSIONID);
			}
			Balance oldBalance = ae.getBalance();
			Balance b = oldBalance.decBBalance(value);
			ae.setBalance(b);
			updateAccountEntity(ae, new Date(), oldBalance.getValue()
					- b.getValue());
			ret[0] = oldBalance;
			ret[1] = ae.getBalance();
			return ret;
		}
	}

	public Balance[] decBBalance(String name, int value)
			throws BalanceException {
		if (value < 0)
			throw new BalanceException(Errors.ILLEGAL_VALUE);
		Object lock = getLock(name);
		Balance[] ret = new Balance[2];
		synchronized (lock) {
			AccountEntity ae = getAccountEntity(name);
			if (ae == null)
				throw new BalanceException(Errors.UNKNOW_ACCOUNT);
			Balance oldBalance = ae.getBalance();
			Balance b = oldBalance.decBBalance(value);
			ae.setBalance(b);
			updateAccountEntity(ae, new Date(), oldBalance.getValue()
					- b.getValue());
			ret[0] = oldBalance;
			ret[1] = ae.getBalance();
			return ret;
		}
	}

	public Balance[] decABalanceToZero(String name, String sessionId,
			String serviceId, int value) throws BalanceException {
		if (value < 0)
			throw new BalanceException(Errors.ILLEGAL_VALUE);
		Object lock = getLock(name);
		Balance[] ret = new Balance[2];
		synchronized (lock) {
			AccountEntity ae = getAccountEntity(name);
			if (ae == null)
				throw new BalanceException(Errors.UNKNOW_ACCOUNT);
			LoginInfo li = ae.getLoginInfo(serviceId);
			if (li == null)
				throw new BalanceException(Errors.ILLEGAL_SESSIONID);
			if (!li.isValid())
				throw new BalanceException(Errors.NO_LOGON);
			if (!li.getServiceId().equals(serviceId)) {
				throw new BalanceException(Errors.ILLEGAL_SESSIONID);
			}
			Balance oldBalance = ae.getBalance();
			Balance b = null;
			try {
				b = oldBalance.decABalance(value);
			} catch (BalanceException e) {
				if (oldBalance.getABalance() > 0
						&& oldBalance.getABalance() <= value) {
					b = new Balance(0, oldBalance.getBBalance());
				} else
					throw e;
			}
			ae.setBalance(b);
			updateAccountEntity(ae, new Date(), oldBalance.getValue()
					- b.getValue());
			ret[0] = oldBalance;
			ret[1] = b;
			return ret;
		}
	}

	// 扣i币，如果不够就扣到0,value必须是正数,返回值为实际扣除的i币以及剩余的balance,只扣除被确定了的i币
	public Balance[] decBBalanaceToZero(String name, String sessionId,
			String serviceId, int value) throws BalanceException {
		if (value < 0)
			throw new BalanceException(Errors.ILLEGAL_VALUE);
		Object lock = getLock(name);
		Balance[] ret = new Balance[2];
		synchronized (lock) {
			try {
				AccountEntity ae = getAccountEntity(name);
				if (ae == null)
					throw new BalanceException(Errors.UNKNOW_ACCOUNT);
				LoginInfo li = ae.getLoginInfo(serviceId);
				if (li == null)
					throw new BalanceException(Errors.ILLEGAL_SESSIONID);
				if (!li.isValid())
					throw new BalanceException(Errors.NO_LOGON);
				if (!li.getServiceId().equals(serviceId)) {
					throw new BalanceException(Errors.ILLEGAL_SESSIONID);
				}
				Balance oldBalance = ae.getBalance();
				Balance b = null;
				try {
					b = oldBalance.decBBalance(value);
				} catch (BalanceException e) {
					if (oldBalance.getBBalance() > 0
							&& oldBalance.getBBalance() < value) {
						b = new Balance(oldBalance.getABalance(), 0);
					} else
						throw e;
				}
				ae.setBalance(b);
				updateAccountEntity(ae, new Date(), oldBalance.getValue()
						- b.getValue());
				ret[0] = oldBalance;
				ret[1] = b;
				return ret;
			} catch (RuntimeException e) {
				e.printStackTrace();
				throw new BalanceException(Errors.UNKNOW);
			}
		}
	}

	// //修改i币，value可以为正或者为负，正数是添加i币，负数是减少i币，如果剩下的i币不够扣那么抛出BalanceException，只修改被确认了的i币
	// public int modifyConfirmedBalance(String name, String sessionId, String
	// serviceId,
	// int value, boolean allowNegative) throws BalanceException {
	// Object lock = getLock(name);
	// synchronized (lock) {
	// try {
	// AccountEntity ae = getAccountEntity(name);
	// if (ae == null)
	// throw new BalanceException(Errors.UNKNOW_ACCOUNT);
	// LoginInfo li = ae.getLoginInfo(serviceId);
	// if (li == null)
	// throw new BalanceException(Errors.ILLEGAL_SESSIONID);
	// if(!li.isValid())
	// throw new BalanceException(Errors.NO_LOGON);
	// if (!li.getServiceId().equals(serviceId)) {
	// throw new BalanceException(Errors.ILLEGAL_SESSIONID);
	// }
	// if (allowNegative) {
	// ae.setConfirmedBalance(ae.getConfirmedBalance() + value);
	// updateAccountEntity(ae);
	// return ae.getConfirmedBalance();
	// }else{
	// if(ae.getConfirmedBalance()>=0){
	// int m = ae.getConfirmedBalance()+value;
	// if(m<0)
	// throw new BalanceException(Errors.NOT_ENOUGH_BALANCE);
	// ae.setConfirmedBalance(m);
	// updateAccountEntity(ae);
	// return m;
	// }
	// else{
	// if(value<0)
	// throw new BalanceException(Errors.NOT_ENOUGH_BALANCE);
	// ae.setConfirmedBalance(ae.getConfirmedBalance()+value);
	// updateAccountEntity(ae);
	// return ae.getConfirmedBalance();
	// }
	// }
	// } catch (RuntimeException e) {
	// e.printStackTrace();
	// throw new BalanceException(Errors.UNKNOW);
	// }
	// }
	// }
	//	
	// public int modifyBalance(String name, String serviceId, int balance,
	// boolean allowNegative) throws BalanceException {
	// Object lock = getLock(name);
	// synchronized (lock) {
	// try {
	// AccountEntity ae = getAccountEntity(name);
	// if (ae == null)
	// throw new BalanceException(Errors.UNKNOW_ACCOUNT);
	// if (allowNegative) {
	// ae.setConfirmedBalance(ae.getConfirmedBalance() + balance);
	// updateAccountEntity(ae);
	// return ae.getConfirmedBalance();
	// } else {
	// if (balance >= 0) {
	// ae.setConfirmedBalance(ae.getConfirmedBalance() + balance);
	// return ae.getConfirmedBalance();
	// } else {
	// if (ae.getConfirmedBalance() > 0 && (ae.getConfirmedBalance() + balance)
	// < 0)
	// throw new BalanceException(Errors.NOT_ENOUGH_BALANCE);
	// ae.setConfirmedBalance(ae.getConfirmedBalance() + balance);
	// updateAccountEntity(ae);
	// return ae.getConfirmedBalance();
	// }
	// }
	// } catch (RuntimeException e) {
	// throw new BalanceException(Errors.UNKNOW);
	// }
	// }
	// }

	public AccountEntity charge(String name, int value, boolean confirmed)
			throws BalanceException {
		Object lock = getLock(name);
		synchronized (lock) {
			AccountEntity ae = getAccountEntity(name);
			if (ae == null)
				throw new BalanceException(Errors.UNKNOW_ACCOUNT);
			if (value <= 0)
				throw new IllegalArgumentException("charge value can not be "
						+ value);
			if (confirmed) {
				Balance b = ae.getBalance().addBBalance(value);
				ae.setBalance(b);
			} else {
				Balance b = ae.getBalance().addABalance(value);
				ae.setBalance(b);
			}
			updateAccountEntity(ae);
			return ae;
		}
	}

	public int[] addRecommendBalance(int accID, int value, int value2) throws BalanceException {
		AccountEntity ae = getAccountEntity(accID);
		if (ae == null) {
		    throw new BalanceException(Errors.UNKNOW_ACCOUNT);
		}
		AccountEntity ae2 = null;
		try {
		    ae2 = getAccountEntity(Integer.parseInt(ae.getAccount().getRecommend()));
		} catch (Exception e) {
		}
		if (ae2 == null) {
		    // 没有有效推荐人
		    return null;
		}
		
		// 给被推荐人加i币
	    Object lock = getLock(ae.getName());
	    synchronized (lock) {
	        Balance b = ae.getBalance().addBBalance(value);
            ae.setBalance(b);
            updateAccountEntity(ae);
	    }
	    feeService.addCompletedFee(ae.getId(), value, "gm_recommended");
	
	    // 给推荐人加i币
	    lock = getLock(ae2.getName());
	    synchronized (lock) {
	        Balance b = ae2.getBalance().addBBalance(value2);
	        ae2.setBalance(b);
	        updateAccountEntity(ae2);
	    }
	    feeService.addCompletedFee(ae2.getId(), value2, "gm_recommend");
	    
	    return new int[] { accID, value, ae2.getId(), value2 };
	}
	
	public long forceAddBalance(int accID, int value, String reason) throws BalanceException {
		AccountEntity ae = getAccountEntity(accID);
		if (ae == null) {
		    throw new BalanceException(Errors.UNKNOW_ACCOUNT);
		}
	    Object lock = getLock(ae.getName());
	    long ret;
	    synchronized (lock) {
	        Balance b = ae.getBalance().addBBalance(value);
            ae.setBalance(b);
            updateAccountEntity(ae);
            ret = b.getValue();
	    }
	    feeService.addCompletedFee(ae.getId(), value, reason);
	    return ret;
	}

	public AccountEntity changeStatus(String name, int status, String comment)
			throws ChangeStatusException {
		Object lock = getLock(name);
		synchronized (lock) {
			try {
				AccountEntity ae = getAccountEntity(name);
				if (ae == null)
					throw new ChangeStatusException(Errors.UNKNOW_ACCOUNT);
				ae.setStatus(status);
				if (comment != null && comment.length() != 0) {
					ae.setComment(comment);
				}
				updateAccountEntity(ae);
				return ae;
			} catch (RuntimeException e) {
				throw new ChangeStatusException(Errors.UNKNOW);
			}
		}
	}

	public void modifyPassword(String name, String key, String oldPassword,
			String password, String serviceId) throws ModifyException {
		Object lock = getLock(name);
		synchronized (lock) {
			AccountEntity ae = getAccountEntity(name);
			if (ae == null)
				throw new ModifyException(Errors.UNKNOW_ACCOUNT);
			LoginInfo li = ae.getLoginInfo(serviceId);
			if (li == null)
				throw new ModifyException(Errors.ILLEGAL_SESSIONID);
			if (!li.isValid())
				throw new ModifyException(Errors.NO_LOGON);
			if (!Util.verifyPassword(ae.getPassWord(), oldPassword))
				throw new ModifyException(Errors.ERROR_OLD_PASSWORD);
			int result = passwordValidator.valid(password);
			if (result == IStringValidator.MAX_LIMIT
					|| result == IStringValidator.MIN_LIMIT)
				throw new ModifyException(Errors.ILLEGAL_PASSWORD_LENGTH);
			if (result != IStringValidator.OK)
				throw new ModifyException(Errors.ILLEGAL_PASSWORD_CHAR);
			ae.getAccount().setPasswordEnc(password);
			ae.getAccount().setModifyPasswordTimes(
					ae.getAccount().getModifyPasswordTimes() + 1);
			updateAccountEntity(ae);
		}
	}

	public void modifyPassword(String name, String oldPassword, String password)
			throws ModifyException {
		AccountEntity ae = getAccountEntity(name);
		if (ae == null)
			throw new ModifyException(Errors.UNKNOW_ACCOUNT);
		if (!Util.verifyPassword(ae.getPassWord(), oldPassword))
			throw new ModifyException(Errors.ERROR_OLD_PASSWORD);
		int result = passwordValidator.valid(password);
		if (result == IStringValidator.MAX_LIMIT
				|| result == IStringValidator.MIN_LIMIT)
			throw new ModifyException(Errors.ILLEGAL_PASSWORD_LENGTH);
		if (result != IStringValidator.OK)
			throw new ModifyException(Errors.ILLEGAL_PASSWORD_CHAR);
		ae.getAccount().setPasswordEnc(password);
		ae.getAccount().setModifyPasswordTimes(
				ae.getAccount().getModifyPasswordTimes() + 1);
		updateAccountEntity(ae);
	}

	public void modifyPhone(String name, String key, String phone,
			String serviceId) throws ModifyException {
		Object lock = getLock(name);
		synchronized (lock) {
			AccountEntity ae = getAccountEntity(name);
			if (ae == null)
				throw new ModifyException(Errors.UNKNOW_ACCOUNT);
			LoginInfo li = ae.getLoginInfo(serviceId);
			if (li == null)
				throw new ModifyException(Errors.ILLEGAL_SESSIONID);
			if (!li.isValid())
				throw new ModifyException(Errors.NO_LOGON);
			if (phoneValidator.valid(phone) != IStringValidator.OK)
				throw new ModifyException(Errors.ILLEGAL_PHONE);
			ae.getAccount().setPhone(phone);
			updateAccountEntity(ae);
		}
	}
	
	public void modifyPhone(String name,String oldPhone,String phone) throws ModifyException{
		Object lock = getLock(name);
		synchronized (lock) {
			AccountEntity ae = getAccountEntity(name);
			if (ae == null)
				throw new ModifyException(Errors.UNKNOW_ACCOUNT);
			if (phoneValidator.valid(phone) != IStringValidator.OK)
				throw new ModifyException(Errors.ILLEGAL_PHONE);
			if(oldPhone.length()>0&&!oldPhone.equals(ae.getPhone()))
				throw new ModifyException(Errors.ERROR_OLD_PHONE);
			if(oldPhone.length()==0&&(ae.getPhone()!=null&&ae.getPhone().length()!=0))
				throw new ModifyException(Errors.ERROR_OLD_PHONE);
			ae.getAccount().setPhone(phone);
			updateAccountEntity(ae);
		}		
	}

	protected AccountEntity check0(String name, String password, String phone)
			throws LoginException {
		AccountEntity ae = getAccountEntity(name);
		if (ae == null) {
			throw new LoginException(Errors.ERROR_NAMEORPASSWORD);
		}
		
		// 检查是否是攻击
		if (!ae.checkLoginAttack()) {
			throw new LoginException(Errors.ACCOUNT_FREEZE_PASSATTACK);
		}
		
		if (ae.getPassWord().equals(password)) {
			if (ae.getStatus() == 0) {
				throw new LoginException(Errors.ACCOUNT_INVALID);
			} else if (ae.getStatus() == 2) {
				throw new LoginException(Errors.ACCOUNT_FREEZE);
			} else if (ae.getStatus() != 1) {
				throw new LoginException(Errors.UNKNOW);
			}
			
			// 如果登录时带上了手机号信息，则记录
			if (phone != null && phone.length() > 0 && !phone.equals(ae.getAccount().getActivePhone())) {
			    ae.getAccount().setActivePhone(phone);
			    updateAccountEntity(ae);
			}
			return ae;
		} else {
			// 密码错误，记录
			ae.recordLoginFail();
			throw new LoginException(Errors.ERROR_NAMEORPASSWORD);
		}
	}

	public AccountEntity check(String name, String password, String phone)
			throws LoginException {
		Object lock = getLock(name);
		synchronized (lock) {
			return check0(name, password, phone);
		}
	}

	public AccountEntity register(String name, String initPass, String guardPass, 
	        int balance, String serviceId, String phone, String recommend,
			String serviceVersion, String comment, String model,String versionPatch,
			String loginPhone)
			throws CreateAccountException {
		AccountEntity ae;
		try {
		    // 检查注册的手机号是否有对应的推荐记录，如果有推荐记录，修改推荐人ID
		    if (loginPhone != null && loginPhone.length() > 0) {
		        RecommendRequest rr = new RecommendRequestDAO().findByPhone(loginPhone);
		        if (rr != null) {
		            recommend = String.valueOf(rr.getAccount());
		        }
		    }
		    
			Account account = new Account();
			account.setName(name);
			
			// 如果设置了初始密码，检查是否合法，如果初始密码为空，自动生成密码
			if (initPass.length() > 0) {
			    int result = passwordValidator.valid(initPass);
                if (result == IStringValidator.MAX_LIMIT
                        || result == IStringValidator.MIN_LIMIT)
                    throw new CreateAccountException(Errors.ILLEGAL_PASSWORD_LENGTH);
                if (result != IStringValidator.OK)
                    throw new CreateAccountException(Errors.ILLEGAL_PASSWORD_CHAR);
                account.setPasswordEnc(initPass);
                account.setModifyPasswordTimes(1);
			} else {
    			account.setPasswordEnc(getPassword(rnd));
    			account.setModifyPasswordTimes(0);
			}
			account.setGuardPass(guardPass);
			Balance b = new Balance(0, balance);
			account.setBalance(b);
			account.setCreateGameCode(serviceId);
			account.setPhone(phone);
			account.setRecommend(recommend);
			account.setServiceVersion(serviceVersion);
			account.setComment(comment);
			account.setStatus(1);
			account.setModel(model);
			account.setVersionPatch(versionPatch);
			account.setCbalance(0);
			account.setRegType(0);
			account.setActivePhone(loginPhone);
			account.setRegPhone(loginPhone);
			createAccount(account);
			ae = new AccountEntity(account);
			return ae;
		} catch (RuntimeException e) {
			throw new CreateAccountException(Errors.UNKNOW);
		} catch (CreateAccountException e) {
			throw e;
		} catch (Throwable e) {
			throw new CreateAccountException(Errors.UNKNOW);
		}
	}
	
	/**
	 * 用来支持用手机号注册，需要传入密码以及C币以及注册方式
	 */
	public AccountEntity registerWithPassword(String name, String password,
			int balance, int cbalance, String serviceId, String serviceVersion,
			String comment, String model, String versionPatch, int regType)
			throws CreateAccountException {
		Object lock = getLock(name);
		synchronized (lock) {
			AccountEntity ae = getAccountEntity(name);
			if(ae!=null){
				ae.setPassword(password);
				updateAccountEntity(ae);
				throw new CreateAccountException(Errors.DUPLICATE_NAME);
			}
			try {
				Account account = new Account();
				account.setName(name);
				account.setPasswordEnc(password);
				account.setGuardPass("");
				Balance b = new Balance(0, balance);
				account.setBalance(b);
				account.setCreateGameCode(serviceId);
				account.setPhone(name);
				account.setRecommend("");
				account.setServiceVersion(serviceVersion);
				account.setComment(comment);
				account.setStatus(1);
				account.setModifyPasswordTimes(1);
				account.setModel(model);
				account.setVersionPatch(versionPatch);
				account.setCbalance(cbalance);
				account.setRegType(regType);
				createAccount(account);
				ae = new AccountEntity(account);
				return ae;
			} catch (RuntimeException e) {
				throw new CreateAccountException(Errors.UNKNOW);
			} catch (CreateAccountException e) {
				throw e;
			} catch (Throwable e) {
				throw new CreateAccountException(Errors.UNKNOW);
			}
		}
	}
	
	private final Random rnd = new Random();

	protected final static char[] NUM = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9' };

	public static String getPassword(Random rnd) {
		char[] ret = new char[6];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = NUM[rnd.nextInt(ret.length)];
		}
		return new String(ret);
	}

	public static void main(String[] args) {
		// AccountService service = new AccountService(new AccountDAO(),new
		// LoginInfoDAO(),new PurchasedDAO(),16);
		// Account a = new Account();
		// a.setName("test2");
		// Balance b = new Balance(200,0);
		// a.setBalance(b);
		// // a.setPassWord("2008");
		// a.setCreateGameCode("test");
		// a.setCreateTime(new Date());
		// a.setStatus(1);
		// try {
		// service.createAccount(a);
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	public AccountEntity getAccountEntity(String name) {
		synchronized (cache) {
			name = name.trim();
			AccountEntity ae = getAccountEntityFromCache(name);
			if (ae != null) {
				return ae;
			}
			Account account = getAccount(name);
			if (account != null) {
				ae = new AccountEntity(account);
				List<LoginInfo> l = loginInfoDAO.findLoginInfoByAccountId(ae
						.getId());
				if (l != null && !l.isEmpty()) {
					for (int i = 0, size = l.size(); i < size; i++) {
						ae.addLoginInfo(l.get(i));
					}
				}
				List<Purchased> lp = purchasedDAO.getPurchased(ae.getId());
				if (lp != null && !lp.isEmpty()) {
					for (Purchased purchased : lp) {
						ae.addPurchased(purchased);
					}
				}
				cache.put(new Element(name.toUpperCase(), ae));
			}
			return ae;
		}
	}
	
	public void rename(String oldName,String newName) throws CreateAccountException{
		if(!oldName.startsWith(UWAPSocketStub.QUICKREG_PREFIX)){
			throw new CreateAccountException(Errors.ILLEGAL_NAME);
		}
		AccountEntity tmp = getAccountEntity(newName);
		if(tmp!=null)
			throw new CreateAccountException(Errors.DUPLICATE_NAME);
		Object lock = getLock(oldName);
		synchronized(lock){
			AccountEntity ae = getAccountEntity(oldName);
			ae.setName(newName);
			try {
				updateAccountEntity(ae);
				cache.remove(oldName.toUpperCase());
			} catch (Exception e) {
				ae.setName(oldName);
				throw new CreateAccountException(Errors.DUPLICATE_NAME);
			}
		}
	}

	public AccountEntity getAccountEntity(int id) {
		String name = accountDao.getAccountNameById(id);
		if (name != null) {
			return getAccountEntity(name);
		}
		return null;
	}

	public void release(int id) {
		String name = accountDao.getAccountNameById(id);
		synchronized (cache) {
			cache.remove(name.toUpperCase());
		}
	}

	public AccountEntity getAccountEntityBySubscribePhone(int code, String phone) {
		Purchased p = purchasedDAO.getPurchased(code, phone);
		if (p != null)
			return getAccountEntity(p.getAccountId());
		return null;
	}

	protected AccountEntity getAccountEntityFromCache(String name) {
		Element e = cache.get(name.toUpperCase());
		if (e == null)
			return null;
		return (AccountEntity) e.getObjectValue();
	}

	public boolean unPurchase(String name, int productCode) {
		Object lock = getLock(name);
		synchronized (lock) {
			AccountEntity entity = getAccountEntity(name);
			if (entity == null) {
				return false;
			}
			Purchased p = entity.getPurchased(productCode);
			if (p == null || p.getStatus() != 1) {
				return false;
			}
			p.setStatus(0);
			entity.removePurchased(productCode);
			purchasedDAO.update(p);
			return true;
		}
	}

	public boolean purchaseProduct(Fee fee, int productCode, String phone) {
		AccountEntity entity = getAccountEntity(fee.getAccountId());
		if (entity == null) {
			return false;
		}
		Object lock = getLock(entity.getName());
		synchronized (lock) {
			Purchased p = entity.getPurchased(productCode);
			if (p != null && p.getStatus() == 1) {
				return false;
			}
			if (p == null) {
				p = new Purchased();
				p.setAccountId(fee.getAccountId());
				p.setCode(productCode);
				p.setPhone(phone);
				p.setCreateTime(new Date());
				p.setFeeId(fee.getId());
				p.setStatus(1);
				entity.addPurchased(p);
				purchasedDAO.save(p);
			} else {
				p.setPhone(phone);
				p.setCreateTime(new Date());
				p.setFeeId(fee.getId());
				p.setStatus(1);
				purchasedDAO.update(p);
			}
			return true;
		}
	}

	public boolean purchaseProduct(String name, int productCode, int feeId,
			String phone) {
		Object lock = getLock(name);
		synchronized (lock) {
			AccountEntity ae = getAccountEntity(name);
			if (ae != null) {
				Purchased p = ae.getPurchased(productCode);
				if (p != null && p.getStatus() == 1) {
					return false;
				}
				if (p == null) {
					p = new Purchased();
					p.setAccountId(ae.getId());
					p.setCode(productCode);
					p.setPhone(phone);
					p.setCreateTime(new Date());
					p.setFeeId(feeId);
					p.setStatus(1);
					ae.addPurchased(p);
					purchasedDAO.save(p);
				} else {
					p.setPhone(phone);
					p.setCreateTime(new Date());
					p.setFeeId(feeId);
					p.setStatus(1);
					purchasedDAO.update(p);
				}
				return true;
			}
			return false;
		}
	}

	public boolean fulfillOrder(Fee fee) {
		AccountEntity entity = getAccountEntity(fee.getAccountId());
		if (entity == null) {
			return false;
		}
		Object lock = getLock(entity.getName());
		synchronized (lock) {
			if (fee.getAmount() >= 0) {
				Balance newBalance = entity.getBalance().addABalance(
						fee.getAmount());
				entity.setBalance(newBalance);
				updateAccountEntity(entity);
			} else {
				Balance newBalance = entity.getBalance().forceDecABalance(
						-fee.getAmount());
				entity.setBalance(newBalance);
				updateAccountEntity(entity);
			}
			syncBalance(entity);
			return true;
		}
	}
	
	public boolean fulfillOrder2(Fee fee) {
		AccountEntity entity = getAccountEntity(fee.getAccountId());
		if (entity == null) {
			return false;
		}
		Object lock = getLock(entity.getName());
		synchronized (lock) {
			entity.setCbalance(entity.getCbalance() + fee.getAmount());
			updateAccountEntity(entity);
			syncBalance(entity);
			return true;
		}
	}

	public String resetPassword(Fee fee) {
		AccountEntity entity = this.getAccountEntity(fee.getAccountId());
		if (entity == null) {
			return null;
		}
		// TODO: random password
		return entity.getAccount().getPasswordDec();
	}

	public boolean changePhone(Fee fee, String newPhone) {
		AccountEntity entity = this.getAccountEntity(fee.getAccountId());
		if (entity == null) {
			return false;
		}
		Object lock = getLock(entity.getName());
		synchronized (lock) {
			entity.getAccount().setPhone(newPhone);
			updateAccountEntity(entity);
			return true;
		}
	}

	public boolean hasPurchased(String accountName, int productCode) {
		AccountEntity entity = this.getAccountEntity(accountName);
		if (entity == null) {
			return false;
		}
		Purchased p = entity.getPurchased(productCode);
		if (p == null) {
			return false;
		}
		return (p.getStatus() == 1);
	}
	
	/**
	 * 判断一个玩家升级是否会触发发放推荐人奖励。
	 * @param accID 帐号ID
	 * @param service 服务ID
	 * @param roleID 角色ID
	 * @param level 当前级别
	 * @return 如果没有触发发送奖励，返回null；否则返回RecommendReward对象
	 * @throws BalanceException
	 */
	public RecommendReward checkRecommend(int accID, String service, int roleID, int level) throws BaseAccountException {
	    log.info("[CHECKRECOMEND]ACCOUNTID[" + accID + "]GAMECODE[" + service + "]ROLEID[" +
	            roleID + "]LEVEL[" + level + "]TRY");
	    
        // 检查是否有这个级别对应的奖励
        int code = getRecommendRewardCode(service, level);
        int[] values = getRecommendValue(code);
        if (values == null) {
            log.info("[CHECKRECOMEND]NO REWARD CONFIG");
            return null;
        }

        // 检查是否有推荐人
        AccountEntity ae = getAccountEntity(accID);
        if (ae == null) {
            throw new BaseAccountException(Errors.UNKNOW_ACCOUNT);
        }
        AccountEntity ae2 = null;
        try {
            ae2 = getAccountEntity(Integer.parseInt(ae.getAccount().getRecommend()));
        } catch (Exception e) {
        }
        if (ae2 == null) {
            // 没有有效推荐人
            log.info("[CHECKRECOMEND]NO RECOMMEND");
            return null;
        }
        
        // 检查是否已经给这个玩家发送过本级别的奖励了
        RecommendRewardDAO dao = new RecommendRewardDAO();
        if (dao.hasRewarded(accID, code)) {
            log.info("[CHECKRECOMEND]DUPLICATE");
            return null;
        }
        if (ae.getAccount().getRegPhone() == null || ae.getAccount().getRegPhone().length() == 0) {
        	log.info("[CHECKRECOMEND]NO PHONE");
            return null;
        }
        if (ae.getAccount().getRegPhone() != null && ae.getAccount().getRegPhone().length() > 0 &&
                dao.hasRewarded(ae.getAccount().getRegPhone(), code)) {
            log.info("[CHECKRECOMEND]DUPLICATE");
            return null;
        }
        
        // 给被推荐人加i币
        if (values[0] > 0) {
            Object lock = getLock(ae.getName());
            synchronized (lock) {
                Balance b = ae.getBalance().addBBalance(values[0]);
                ae.setBalance(b);
                updateAccountEntity(ae);
            }
            feeService.addCompletedFee(ae.getId(), values[0], "gm_recommended");
        }
    
        // 给推荐人加i币
        if (values[1] > 0) {
            Object lock = getLock(ae2.getName());
            synchronized (lock) {
                Balance b = ae2.getBalance().addBBalance(values[1]);
                ae2.setBalance(b);
                updateAccountEntity(ae2);
            }
            feeService.addCompletedFee(ae2.getId(), values[1], "gm_recommend");
        }
        
        // 添加RecommendReward记录
        RecommendReward rr = new RecommendReward();
        rr.setRewardTime(new Date());
        rr.setGuestID(accID);
        rr.setGuestPhone(ae.getAccount().getRegPhone());
        rr.setGuestGameCode(service);
        rr.setRoleID(roleID);
        rr.setGuestLevel(level);
        rr.setGuestRewardValue(values[0]);
        rr.setOwnerID(ae2.getId());
        rr.setOwnerRewardValue(values[1]);
        rr.setRewardCode(code);
        dao.add(rr);

        log.info("[CHECKRECOMEND]RECORDID[" + rr.getId() + "]OK");
        return rr;
    }
	
	/*
	 * 取得某个游戏某个级别对应的推荐人奖励代码。如果没有对应的配置，返回-1.
	 */
	private int getRecommendRewardCode(String service, int level) {
	    // 目前暂时只有幻想
	    if (service.startsWith("itimes")) {
	        switch (level) {
	        case 50:
	            return 1;
	        case 65:
	            return 2;
	        case 80:
	            return 3;
	        case 90:
	            return 4;
            case 100:
                return 5;
	        }
	    }
	    return -1;
	}
	
	/*
	 * 取得某个推荐人奖励代码对应的奖励金额。
	 * @param code 奖励代码
	 * @return 两个元素，第一个是给被推荐人的奖励，第二个是给推荐人的奖励，null表示非法代码。
	 */
	private int[] getRecommendValue(int code) {
	    switch (code) {
	    case 1:
	        return new int[] { 36000, 72000 };
	    case 2:
            return new int[] { 72000, 144000 };
	    case 3:
            return new int[] { 108000, 216000 };
	    case 4:
            return new int[] { 144000, 288000 };
	    case 5:
	        return new int[] { 180000, 360000 };
	    }
	    return null;
	}
	
	public void updatePhone(int accountID, String phone) throws BalanceException {
		if (phone == null || phone.length() == 0) {
			return;
		}
		AccountEntity ae = getAccountEntity(accountID);
		if (ae == null) {
		    throw new BalanceException(Errors.UNKNOW_ACCOUNT);
		}
	    Object lock = getLock(ae.getName());
	    synchronized (lock) {
	    	Account acc = ae.getAccount();
	    	if (acc.getRegPhone() == null || acc.getRegPhone().length() == 0) {
	    		if (acc.getCreateTime().getTime() > System.currentTimeMillis() - 600000L) {
	    			// 注册10分钟算作注册手机号
	    			acc.setRegPhone(phone);
	    		}
	    	}
	    	acc.setActivePhone(phone);
	    	updateAccountEntity(ae);
	    }
	}
	
	/**
	 * 向世界服务器同步余额。
	 * @param entity
	 */
	public void syncBalance(AccountEntity entity) {
		if (listener != null) {
			listener.accountBalanceChanged(entity);
		}
	}
}
