package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class SyncBalanceMessage extends AbstractMessage {
	
	protected int accountId;
	protected int balance;
	protected boolean isMonth;
	protected boolean isSubscribe;
	
	public SyncBalanceMessage(int serial,int accountId,int balance,boolean isMonth,boolean isSubscribe){
		super(GameAccountMessageType.SYNC_BALANCE,serial);
		this.accountId = accountId;
		this.balance = balance;
		this.isMonth = isMonth;
		this.isSubscribe = isSubscribe;
	}

	public SyncBalanceMessage(int accountId,int balance,boolean isMonth,boolean isSubscribe){
		super(GameAccountMessageType.SYNC_BALANCE);
		this.accountId = accountId;
		this.balance = balance;
		this.isMonth = isMonth;
		this.isSubscribe = isSubscribe;	
	}

	public int getAccountId() {
		return accountId;
	}

	public int getBalance() {
		return balance;
	}

	public boolean isMonth() {
		return isMonth;
	}

	public boolean isSubscribe() {
		return isSubscribe;
	}
	
}
