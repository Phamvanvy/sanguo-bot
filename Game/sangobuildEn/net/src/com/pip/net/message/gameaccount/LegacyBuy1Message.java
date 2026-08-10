package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class LegacyBuy1Message extends AbstractMessage {
	
	protected int accountId;
	protected String key;
	protected int value;
	
	public LegacyBuy1Message(int serial,int accountId,String key,int value){
		super(GameAccountMessageType.LEGACY_BUY1,serial);
		this.accountId = accountId;
		this.key = key;
		this.value = value;
	}
	
	public LegacyBuy1Message(int accountId,String key,int value){
		super(GameAccountMessageType.LEGACY_BUY1);
		this.accountId = accountId;
		this.value = value;
		this.key = key;
	}

	public int getAccountId() {
		return accountId;
	}

	public int getValue() {
		return value;
	}

	public String getKey() {
		return key;
	}
	
}
