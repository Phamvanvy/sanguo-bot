package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class LegacyBuyResultMessage extends AbstractMessage{
	
	protected boolean success;
	protected int balance;
	protected int cost;
	protected String cause;
	protected long longBalance;
	protected long bBalance;

	public LegacyBuyResultMessage(int serial,boolean success,int balance,int cost,String cause,long longBalance,long bBalance){
		super(GameAccountMessageType.LEGACY_BUY_RESULT,serial,true);
		this.success = success;
		this.balance = balance;
		this.cost = cost;
		this.cause = cause;
		this.longBalance = longBalance;
		this.bBalance = bBalance;
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
	
	public long getLongBalance() {
		return longBalance;
	}

	public long getBBalance() {
		return bBalance;
	}
}
