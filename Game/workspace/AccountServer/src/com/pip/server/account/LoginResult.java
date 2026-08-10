package com.pip.server.account;

import com.pip.server.account.bean.LoginInfo;

public class LoginResult {
	private AccountEntity ae;
	private LoginInfo info;
	
	public LoginResult(AccountEntity ae,LoginInfo info){
		this.ae = ae;
		this.info = info;
	}
	
	public AccountEntity getAccountEntity(){
		return ae;
	}
	
	public LoginInfo getLoginInfo(){
		return info;
	}
	
}
