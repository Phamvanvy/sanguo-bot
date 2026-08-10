package com.pip.gameaccount;

public class LoginKey {
	private String accountId;
	private String key;
	
	public LoginKey(String accountId,String key){
		this.accountId = accountId;
		this.key = key;
	}
	
	public String getAccountId(){
		return accountId;
	}
	
	public String getKey(){
		return key;
	}
}
