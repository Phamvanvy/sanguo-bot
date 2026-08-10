package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class CreateIMoneyCardOkMessage extends AbstractMessage {
	private int accountID;
	private int cost;
	private int balance;
	private String cardno;
	private String password;
	
	public CreateIMoneyCardOkMessage(int serial, int accountID, int cost, int balance, String cardno, String password) {
		super(GameAccountMessageType.CREATE_IMONEY_CARD_OK, serial);
		this.accountID = accountID;
		this.cost = cost;
		this.balance = balance;
		this.cardno = cardno;
		this.password = password;
	}
	
	public CreateIMoneyCardOkMessage(int accountID, int cost, int balance, String cardno, String password) {
		super(GameAccountMessageType.CREATE_IMONEY_CARD_OK);
		this.accountID = accountID;
		this.cost = cost;
		this.balance = balance;
		this.cardno = cardno;
		this.password = password;
	}

	public int getAccountID() {
		return accountID;
	}

	public int getCost() {
		return cost;
	}

	public int getBalance() {
		return balance;
	}

	public String getCardno() {
		return cardno;
	}

	public String getPassword() {
		return password;
	}
}
