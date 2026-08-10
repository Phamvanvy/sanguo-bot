package com.pip.gameaccount;



public interface ILoginService {
	
	public GameAccount login(String name,String key,String serverId);
	
	//添加一个新的LoginKey(accountId,key),返回前一个LoginKey，如果没有返回null
	public LoginDetail addLoginKey(int accountId,String name,String key,String serverId);
	
	//根据帐号名取Login记录
	public GameAccount getGameAccount(String name);
	
	//根据帐号id取Login记录
	public GameAccount getGameAccount(int id);
	
	//返回值不为null时认为是退出成功
	public LoginDetail logout(int id,String key,String serverId);
	public LoginDetail logout(String name,String key,String serverId);
	
	public void rename(String oldName,String newName);
}
