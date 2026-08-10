package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class LegacyFee1Message extends AbstractMessage {
	
	protected int accountId;
	protected String key;
	protected int fee;
	protected int balance;
	
	public LegacyFee1Message(int serial,int accountId,String key,int fee,int balance){
		super(GameAccountMessageType.LEGACY_FEE1,serial);
		this.accountId = accountId;
		this.key = key;
		this.fee = fee;
		this.balance = balance;		
	}
	
	public LegacyFee1Message(int accountId,String key,int fee,int balance){
		super(GameAccountMessageType.LEGACY_FEE1);
		this.accountId = accountId;
		this.key = key;
		this.fee = fee;
		this.balance = balance;
	}

	public int getAccountId() {
		return accountId;
	}

	public int getFee() {
		return fee;
	}

	public int getBalance() {
		return balance;
	}

	public String getKey() {
		return key;
	}	
}
