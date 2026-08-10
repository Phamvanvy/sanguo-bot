package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class Logout1Message extends AbstractMessage {
	
	protected int accountId;
	protected String key;

	public Logout1Message(int serial,int accountId,String key){
		super(GameAccountMessageType.LOGOUT1,serial);
		this.accountId = accountId;
		this.key = key;
	}
	
	public Logout1Message(int accountId,String key){
		super(GameAccountMessageType.LOGOUT1);
		this.accountId = accountId;
		this.key = key;
	}

	public int getAccountId() {
		return accountId;
	}

	public String getKey() {
		return key;
	}
}
