package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class _DecBalanceOkMessage extends AbstractMessage {
	
	protected int value;
	protected int balance;
	protected long longBalance;
	protected long bBalance;
	
	public _DecBalanceOkMessage(int serial,int value,int balance,long longBalance,long bBalance){
		super(GameAccountMessageType.DEC_BALANCE_OK,serial,true);
		this.balance = balance;
		this.value = value;
		this.longBalance = longBalance;
		this.bBalance = bBalance;
	}
	
	public int getBalance(){
		return balance;
	}

	public int getValue() {
		return value;
	}
	
	public long getLongBalance() {
		return longBalance;
	}

	public long getBBalance() {
		return bBalance;
	}
}
