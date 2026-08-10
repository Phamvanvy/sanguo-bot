package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class _BuyOkMessage extends AbstractMessage{
	public long dataCreateTime;
	public long messageCreateTime;
	
	protected int balance;
	protected int cost;
	protected long longBalance;
	private long bBalance;
	
	public _BuyOkMessage(int serial,int cost,int balance, long longBalance, long bBalance){
		super(GameAccountMessageType.BUY_OK,serial,true);
		this.cost = cost;
		this.balance = balance;
		this.longBalance = longBalance;
		this.bBalance = bBalance;
	}
	
	public int getCost(){
		return cost;
	}
	
	public int getBalance(){
		return balance;
	}
	
	public long getLongBalance() {
		return longBalance;
	}
	
	public long getBBalance() {
		return bBalance;
	}
}
