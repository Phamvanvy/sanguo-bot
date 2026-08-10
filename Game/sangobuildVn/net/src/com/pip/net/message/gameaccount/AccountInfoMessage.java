package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class AccountInfoMessage extends AbstractMessage {
	
	protected int accountId;
	protected String name;
	
	public AccountInfoMessage(int serial,int accountId,String name){
		super(GameAccountMessageType.ACCOUNT_INFO,serial);
		this.accountId = accountId;
		this.name = name;
	}
	
	public AccountInfoMessage(int accountId,String name){
		super(GameAccountMessageType.ACCOUNT_INFO);
		this.accountId = accountId;
		this.name = name;
	}

	public int getAccountId() {
		return accountId;
	}

	public String getName() {
		return name;
	}
}
