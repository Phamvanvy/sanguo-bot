package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class UseIMoneyCardOkMessage extends AbstractMessage {
	private int accountID;
	private int amount;
	private int balance;
	private long longBalance;
	private long bBalance;
	
	public UseIMoneyCardOkMessage(int serial, int accountID, int amount, int balance, long longBalance, long bBalance) {
		super(GameAccountMessageType.USE_IMONEY_CARD_OK, serial);
		this.accountID = accountID;
		this.amount = amount;
		this.balance = balance;
		this.longBalance = longBalance;
		this.bBalance = bBalance;
	}
	
	public UseIMoneyCardOkMessage(int accountID, int amount, int balance, long longBalance, long bBalance) {
		super(GameAccountMessageType.USE_IMONEY_CARD_OK);
		this.accountID = accountID;
		this.amount = amount;
		this.balance = balance;
		this.longBalance = longBalance;
		this.bBalance = bBalance;
	}

	public int getAccountID() {
		return accountID;
	}

	public int getAmount() {
		return amount;
	}
	
	public long getLongBalance() {
		return longBalance;
	}

	public int getBalance() {
		return balance;
	}
	
	public long getBBalance() {
		return bBalance;
	}
}
