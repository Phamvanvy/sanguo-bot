package com.pip.server.account;

import com.pip.server.account.bean.Account;

public interface IAccountService {
	
	public void createAccount(Account account);
	
	public Account getAccount(String name);
	
	public Account getAccount(String name,String password);
	
	
}
