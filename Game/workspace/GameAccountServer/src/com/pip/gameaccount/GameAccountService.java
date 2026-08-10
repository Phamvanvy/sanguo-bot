package com.pip.gameaccount;

import java.util.Date;

import com.pip.gameaccount.dao.GameAccountDAO;

public class GameAccountService implements IGameAccountService {
	
	private GameAccountDAO dao;
	

	public GameAccountService(GameAccountDAO dao){
		this.dao = dao;
	}
	
	public GameAccount createGameAccount(GameAccount account){
		dao.create(account);
		return account;
	}
	
	public GameAccount createGameAccount(int accountId,String name, String key,String serverId,Date time) {
		GameAccount account = new GameAccount();
		account.setId(accountId);
		account.setName(name);
		account.setCreateTime(time);
		account.setLoginTime(time);
		account.setKey(key);
		account.setServerId(serverId);
		account.setLastmonthpay(0);
		account.setMonthFee(0);
		account.setMonthPay(0);
		account.setSubscribe(false);
		dao.create(account);
		return account;
	}
	
	public GameAccount createGameAccount(int accountId,String name,Date time){
		return createGameAccount(accountId,name,null,null,time);
	}
	

	public GameAccount getGameAccount(String accountId) {
		GameAccount result = dao.getGameAccountByAccountId(accountId);
		return result;
	}

	public GameAccount getGameAccount(int id) {
		GameAccount result = dao.findById(id, false);
		return result;
	}
	
	
	public void save(GameAccount ga){
		dao.makePersistent(ga);
	}
	
	public String getGameAccountName(int id){
		return dao.getGameAccountName(id);
	}

}
