package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class AccountInfoOkMessage extends AbstractMessage {
	
	protected int accountId;
	protected String name;
	protected String password;
	protected String phone;
	
	public AccountInfoOkMessage(int serial,int accountId,String name,String password,String phone){
		super(GameAccountMessageType.ACCOUNT_INFO_OK,serial);
		this.accountId = accountId;
		this.name = name;
		this.password = password;
		this.phone = phone;
	}
	
	public AccountInfoOkMessage(int accountId,String name,String password,String phone){
		super(GameAccountMessageType.ACCOUNT_INFO_OK);
		this.accountId = accountId;
		this.name = name;
		this.password = password;
		this.phone = phone;
	}

	public int getAccountId() {
		return accountId;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public String getPhone() {
		return phone;
	}
}
