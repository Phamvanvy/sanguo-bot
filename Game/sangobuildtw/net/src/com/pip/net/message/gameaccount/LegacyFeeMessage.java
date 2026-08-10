package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class LegacyFeeMessage extends AbstractMessage {
	
	protected String name;
	protected String key;
	protected int fee;
	protected int balance;
	
	public LegacyFeeMessage(int serial,String name,String key,int fee,int iMoney){
		super(GameAccountMessageType.LEGACY_FEE,serial);
		this.name = name;
		this.key = key;
		this.fee = fee;
		this.balance = iMoney;		
	}
	
	public LegacyFeeMessage(String name,String key,int fee,int iMoney){
		super(GameAccountMessageType.LEGACY_FEE);
		this.name = name;
		this.key = key;
		this.fee = fee;
		this.balance = iMoney;
	}

	public String getName() {
		return name;
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
