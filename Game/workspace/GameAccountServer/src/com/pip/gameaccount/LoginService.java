package com.pip.gameaccount;

import java.util.Date;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;




public class LoginService implements ILoginService {

	private Cache name2account = null;
	private Cache id2name = null;
	
	private IGameAccountService gameAccountService;
	
	
	public LoginService(IGameAccountService gameAccountService){
		this.gameAccountService = gameAccountService;
		CacheManager manager = CacheManager.create();
		name2account = new Cache("name2account",3000,false,true,0,0);
		id2name = new Cache("id2name",8000,false,true,0,0);
		manager.addCache(id2name);
		manager.addCache(name2account);
	}
	
	public GameAccount login(String name,String key,String serverId) {
		synchronized (this) {
			GameAccount account = getGameAccount(name);
			if (account == null)
				return null;
			if (account.getKey()!=null&&account.getKey().equals(key)) {
				//可以接受同一个服务器的再次登陆
				if ( account.getServerId()!=null || account.getServerId().equals(serverId)) {
					return account;
				}
			}
			return null;
		}
	}
	
	public LoginDetail addLoginKey(int accountId,String name,String key,String serverId) {
		synchronized (this) {
			GameAccount account = getGameAccount(name);
			if (account != null) {
				if (account.getKey() != null && account.getServerId() != null) {
					LoginDetail ld = new LoginDetail(name, account.getKey(),
							account.getServerId(), account.getLoginTime());// todo
					account.setKey(key);
					account.setLastServerId(serverId);
					account.setLoginTime(new Date());
					gameAccountService.save(account);
					return ld;
				}else{
					account.setKey(key);
					account.setLastServerId(serverId);
					account.setLoginTime(new Date());
					gameAccountService.save(account);
					return null;
				}
			} else {
				GameAccount ga = createGameAccount(accountId,name, key, serverId, new Date());
				return null;
			}
		}
	}
	

	public LoginDetail logout(String accountId, String key, String serverId) {
		synchronized (this) {
			GameAccount account = getGameAccount(accountId);
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
			GameAccount account = getGameAccount(id);
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
	
	public void rename(String oldName,String newName){
		synchronized(name2account){
			GameAccount ga = removeGameAccountFromCache(oldName);
			if(ga==null){
				ga = gameAccountService.getGameAccount(oldName);
			}
			if(ga!=null){
				ga.setName(newName);
				gameAccountService.save(ga);
				addGameAccountToCache(ga);
			}
		}
	}
	
	public GameAccount getGameAccount(int id){
		synchronized (name2account) {
			String name = getGameAccountNameFromCache(id);
			if (name == null) {
				name = gameAccountService.getGameAccountName(id);
				id2name.put(new Element(id,name.toUpperCase()));
				if(name==null)
					return null;
			}
			GameAccount account = getGameAccountFromCache(name);
			if(account==null){
				account = gameAccountService.getGameAccount(name);
				if(account!=null)
					name2account.put(new Element(name.toUpperCase(),account));
			}
			return account;
		}
	}
	
	public GameAccount getGameAccount(String name){
		synchronized(name2account){
			GameAccount account = getGameAccountFromCache(name);
			if(account==null){
				account = gameAccountService.getGameAccount(name);
				if(account!=null)
					addGameAccountToCache(account);
			}
			return account;
		}
	}
	
	public GameAccount createGameAccount(int accountId,String name,String key,String serverId,Date time){
		synchronized(name2account){
			GameAccount account = gameAccountService.createGameAccount(accountId,name, key, serverId, time);
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
	
	protected GameAccount getGameAccountFromCache(String name){
		Element e = name2account.get(name.toUpperCase());
		if(e!=null)
			return (GameAccount)e.getObjectValue();
		return null;
	}
	
	protected GameAccount removeGameAccountFromCache(String name){
		Element e = name2account.get(name.toUpperCase());
		if(e!=null){
			name2account.remove(name.toUpperCase());
			return (GameAccount)e.getObjectValue();
		}
		return null;
	}
	
	protected void addGameAccountToCache(GameAccount account){
		name2account.put(new Element(account.getName().toUpperCase(),account));
		id2name.put(new Element(account.getId(),account.getName().toUpperCase()));
	}
	
}
