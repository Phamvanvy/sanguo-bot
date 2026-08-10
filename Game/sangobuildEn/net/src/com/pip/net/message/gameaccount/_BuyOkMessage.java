package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class _BuyOkMessage extends AbstractMessage{
	
	protected int balance;
	protected int cost;
	
	public _BuyOkMessage(int serial,int cost,int balance){
		super(GameAccountMessageType.BUY_OK,serial,true);
		this.cost = cost;
		this.balance = balance;
	}
	
	public int getCost(){
		return cost;
	}
	
	public int getBalance(){
		return balance;
	}
	
	
}
