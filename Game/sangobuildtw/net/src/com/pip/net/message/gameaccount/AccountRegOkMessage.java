package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class AccountRegOkMessage extends AbstractMessage{
	
	protected int accountId;
	protected String name;
	protected String password;
	
	public AccountRegOkMessage(int serial,int accountId,String name,String password){
		super(GameAccountMessageType.ACCOUNT_REG_OK,serial,true);
		this.accountId = accountId;
		this.name = name;
		this.password = password;
	}
	
	public String getName(){
		return name;
	}
	
	public String getPassword(){
		return password;
	}

	public int getAccountId() {
		return accountId;
	}
	
}
