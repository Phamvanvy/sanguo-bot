package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class UseIMoneyCardOkMessage extends AbstractMessage {
	private int accountID;
	private int amount;
	private int balance;
	
	public UseIMoneyCardOkMessage(int serial, int accountID, int amount, int balance) {
		super(GameAccountMessageType.USE_IMONEY_CARD_OK, serial);
		this.accountID = accountID;
		this.amount = amount;
		this.balance = balance;
	}
	
	public UseIMoneyCardOkMessage(int accountID, int amount, int balance) {
		super(GameAccountMessageType.USE_IMONEY_CARD_OK);
		this.accountID = accountID;
		this.amount = amount;
		this.balance = balance;
	}

	public int getAccountID() {
		return accountID;
	}

	public int getAmount() {
		return amount;
	}

	public int getBalance() {
		return balance;
	}
}
