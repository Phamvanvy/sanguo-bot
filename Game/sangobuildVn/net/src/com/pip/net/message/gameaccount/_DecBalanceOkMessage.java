package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class _DecBalanceOkMessage extends AbstractMessage {
	
	protected int value;
	protected int balance;
	
	public _DecBalanceOkMessage(int serial,int value,int balance){
		super(GameAccountMessageType.DEC_BALANCE_OK,serial,true);
		this.balance = balance;
		this.value = value;
	}
	
	public int getBalance(){
		return balance;
	}

	public int getValue() {
		return value;
	}
	
}
