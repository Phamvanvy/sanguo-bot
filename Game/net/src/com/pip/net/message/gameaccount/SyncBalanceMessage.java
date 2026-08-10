package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class SyncBalanceMessage extends AbstractMessage {
	
	protected int accountId;
	protected int balance;
	protected boolean isMonth;
	protected boolean isSubscribe;
	protected long longBalance;
	protected long bBalance;
	
	public SyncBalanceMessage(int serial,int accountId,int balance,boolean isMonth,boolean isSubscribe,long longBalance,long bBalance){
		super(GameAccountMessageType.SYNC_BALANCE,serial);
		this.accountId = accountId;
		this.balance = balance;
		this.isMonth = isMonth;
		this.isSubscribe = isSubscribe;
		this.longBalance = longBalance;
		this.bBalance = bBalance;
	}

	public SyncBalanceMessage(int accountId,int balance,boolean isMonth,boolean isSubscribe,long longBalance,long bBalance){
		super(GameAccountMessageType.SYNC_BALANCE);
		this.accountId = accountId;
		this.balance = balance;
		this.isMonth = isMonth;
		this.isSubscribe = isSubscribe;
		this.longBalance = longBalance;
		this.bBalance = bBalance;
	}

	public int getAccountId() {
		return accountId;
	}

	public int getBalance() {
		return balance;
	}
	
	public long getLongBalance(){
		return longBalance;
	}

	public boolean isMonth() {
		return isMonth;
	}

	public boolean isSubscribe() {
		return isSubscribe;
	}
	
	public long getBBalance() {
		return bBalance;
	}
}
