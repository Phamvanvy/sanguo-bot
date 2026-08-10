package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class LegacyBuyResultMessage extends AbstractMessage{
	
	protected boolean success;
	protected int balance;
	protected int cost;
	protected String cause;

	public LegacyBuyResultMessage(int serial,boolean success,int balance,int cost,String cause){
		super(GameAccountMessageType.LEGACY_BUY_RESULT,serial,true);
		this.success = success;
		this.balance = balance;
		this.cost = cost;
		this.cause = cause;
	}

	public boolean isSuccess() {
		return success;
	}

	public int getBalance() {
		return balance;
	}

	public int getCost() {
		return cost;
	}

	public String getCause() {
		return cause;
	}
	

}
