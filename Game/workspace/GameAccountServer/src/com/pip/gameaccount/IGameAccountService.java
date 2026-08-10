package com.pip.gameaccount;

import java.util.Date;

public interface IGameAccountService {
	
	public GameAccount createGameAccount(GameAccount account);
	
	public GameAccount createGameAccount(int accountId,String name, String key,String serverId,Date time);
	
	public GameAccount createGameAccount(int accountId,String name,Date time);

	public GameAccount getGameAccount(String accountId);

	public GameAccount getGameAccount(int id);
	
	public String getGameAccountName(int id);

	public void save(GameAccount ga);
}
