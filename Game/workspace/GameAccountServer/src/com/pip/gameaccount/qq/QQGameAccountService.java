package com.pip.gameaccount.qq;

import java.util.Date;

public class QQGameAccountService {
	
	protected QQGameAccountDAO dao;
	
	public QQGameAccountService(QQGameAccountDAO dao){
		this.dao = dao;
	}
	
	public QQGameAccount createGameAccount(String name,int balance,Date createTime){
		return createGameAccount(name,null,null,balance,createTime);
	}
	
	public QQGameAccount createGameAccount(String name,String key,String serverId,int balance,Date creaTime){
		QQGameAccount account = new QQGameAccount();
		account.setName(name);
		account.setCreateTime(creaTime);
		account.setStatus(1);
		account.setBalance(balance);
		account.setServerId(serverId);
		account.setKey(key);
		dao.create(account);
		return account;
	}
	
	public QQGameAccount getGameAccount(String name){
		return dao.getGameAccountByAccountId(name);
	}
	
	public QQGameAccount getGameAccount(int id){
		return dao.findById(id, false);
	}
	
	public void save(QQGameAccount account){
		dao.update(account);
	}
	
	public String getGameAccountName(int id){
		return dao.getGameAccountName(id);
	}
}
