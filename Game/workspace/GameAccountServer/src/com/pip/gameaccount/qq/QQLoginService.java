package com.pip.gameaccount.qq;

import java.util.Date;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.pip.gameaccount.LoginDetail;

public class QQLoginService {
	
	private Cache name2account = null;
	private Cache id2name = null;
	
	private QQGameAccountService gameAccountService;
	
	
	public QQLoginService(QQGameAccountService gameAccountService){
		this.gameAccountService = gameAccountService;
		CacheManager manager = CacheManager.create();
		name2account = new Cache("name2account",3000,false,true,0,0);
		id2name = new Cache("id2name",8000,false,true,0,0);
		manager.addCache(id2name);
		manager.addCache(name2account);
	}
	
	public QQGameAccount addBalance(String name,int value){
		synchronized(this){
			QQGameAccount account = getGameAccount(name);
			if(account==null)
				return null;
			account.setBalance(account.getBalance()+value);
			gameAccountService.save(account);
			return account;
		}
	}
	
	public QQGameAccount changeStatus(int accountId,int status){
		synchronized (this) {
			QQGameAccount account = getGameAccount(accountId);
			if(account==null)
				return null;
			if(status==0){ //forbid
				account.setKey("-1");
				account.setStatus(status);
			}else{
				account.setKey(".valid");
				account.setStatus(status);
			}
			gameAccountService.save(account);
			return account;
		}
	}
	
	public QQGameAccount decBalance(String name,String key,int value) throws BalanceException{
		if(value<0)
			throw new IllegalArgumentException("value can not be:"+value);
		synchronized(this){
			QQGameAccount account = getGameAccount(name);
			if(account==null)
				return null;
			if(account.getBalance()<value)
				throw new BalanceException(Errors.NOT_ENOUGH_BALANCE);
			account.setBalance(account.getBalance()-value);
			gameAccountService.save(account);
			return account;
		}
	}
	
	public QQGameAccount login(String name,String key,String serverId,String phone) throws LoginException{
		synchronized (this) {
			QQGameAccount account = getGameAccount(name);
			if (account == null)
				throw new LoginException(2);
			if (account.getStatus()!=1)
				throw new LoginException(206);
			if (account.getKey()!=null&&account.getKey().equals(key)) {
				account.setServerId(serverId);
				
				// 如果传了手机号上来，则记录
				if (phone != null && phone.length() > 0) {
				    boolean needSave = false;
				    if (!phone.equals(account.getActivePhone())) {
				        account.setActivePhone(phone);
				        needSave = true;
				    }
				    if (account.getRegPhone() == null || account.getRegPhone().length() == 0) {
				        account.setRegPhone(phone);
				        needSave = true;
				    }
				    if (needSave) {
				        gameAccountService.save(account);
				    }
				}
				return account;
			}
			if(account.getKey()!=null&&account.getKey().equals("-1"))
				throw new LoginException(206); //封号
			return null;
		}
	}
	
	public LoginDetail addLoginKey(String name,String key) {
		synchronized (this) {
			QQGameAccount account = getGameAccount(name);
			if (account != null) {
				if(account.getKey()!=null&&account.getKey().equals("-1")){
					return null;
				}
				if (account.getKey() != null && account.getServerId() != null) {
					LoginDetail ld = new LoginDetail(name, account.getKey(),
							account.getServerId(), account.getLoginTime());// todo
					account.setKey(key);
					account.setServerId(null);
					account.setLoginTime(new Date());
					gameAccountService.save(account);
					return ld;
				}else{
					account.setKey(key);
					account.setServerId(null);
					account.setLoginTime(new Date());
					gameAccountService.save(account);
					return null;
				}
			} else {
				QQGameAccount ga = createGameAccount(name, key, null,0, new Date());
				return null;
			}
		}
	}
	

	public LoginDetail logout(String accountId, String key, String serverId) {
		synchronized (this) {
			QQGameAccount account = getGameAccount(accountId);
			if (account.getKey() != null && account.getKey().equals(key)) {
				LoginDetail ld = new LoginDetail(accountId, key, account
						.getServerId(), account.getLoginTime());
				account.setKey(null);
				gameAccountService.save(account);
				return ld;
			}
			return null;
		}
	}
	
	public LoginDetail logout(int id, String key, String serverId) {
		synchronized (this) {
			QQGameAccount account = getGameAccount(id);
			if (account.getKey() != null && account.getKey().equals(key)) {
				LoginDetail ld = new LoginDetail(account.getName(), key,
						account.getServerId(), account.getLoginTime());
				account.setKey(null);
				gameAccountService.save(account);
				return ld;
			}
			return null;
		}
	}
	
	public QQGameAccount getGameAccount(int id){
		synchronized (name2account) {
			String name = getGameAccountNameFromCache(id);
			if (name == null) {
				name = gameAccountService.getGameAccountName(id);
				id2name.put(new Element(id,name.toUpperCase()));
				if(name==null)
					return null;
			}
			QQGameAccount account = getGameAccountFromCache(name);
			if(account==null){
				account = gameAccountService.getGameAccount(name);
				if(account!=null)
					name2account.put(new Element(name.toUpperCase(),account));
			}
			return account;
		}
	}
	
	public QQGameAccount getGameAccount(String name){
		synchronized(name2account){
			QQGameAccount account = getGameAccountFromCache(name);
			if(account==null){
				account = gameAccountService.getGameAccount(name);
				if(account!=null)
					addGameAccountToCache(account);
			}
			return account;
		}
	}
	
	public QQGameAccount createGameAccount(String name,String key,String serverId,int balance,Date time){
		synchronized(name2account){
			QQGameAccount account = gameAccountService.createGameAccount(name, key, serverId, balance, time);
			addGameAccountToCache(account);
			return account;
		}
	}
	
	protected String getGameAccountNameFromCache(int id){
		Element e = id2name.get(id);
		if(e!=null)
			return (String)e.getValue();
		return null;
	}
	
	protected QQGameAccount getGameAccountFromCache(String name){
		Element e = name2account.get(name.toUpperCase());
		if(e!=null)
			return (QQGameAccount)e.getObjectValue();
		return null;
	}
	
	protected void addGameAccountToCache(QQGameAccount account){
		name2account.put(new Element(account.getName().toUpperCase(),account));
		id2name.put(new Element(account.getId(),account.getName().toUpperCase()));
	}
}