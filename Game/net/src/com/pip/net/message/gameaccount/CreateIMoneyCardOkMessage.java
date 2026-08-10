package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class CreateIMoneyCardOkMessage extends AbstractMessage {
	private int accountID;
	private int cost;
	private int balance;
	private String cardno;
	private String password;
	private long longBalance;
	private long bBalance;
	
	public CreateIMoneyCardOkMessage(int serial, int accountID, int cost, int balance, String cardno, String password, long longBalance, long bBalance) {
		super(GameAccountMessageType.CREATE_IMONEY_CARD_OK, serial);
		this.accountID = accountID;
		this.cost = cost;
		this.balance = balance;
		this.cardno = cardno;
		this.password = password;
		this.longBalance = longBalance;
		this.bBalance = bBalance;
	}
	
	public CreateIMoneyCardOkMessage(int accountID, int cost, int balance, String cardno, String password, long longBalance, long bBalance) {
		super(GameAccountMessageType.CREATE_IMONEY_CARD_OK);
		this.accountID = accountID;
		this.cost = cost;
		this.balance = balance;
		this.cardno = cardno;
		this.password = password;
		this.longBalance = longBalance;
		this.bBalance = bBalance;
	}

	public int getAccountID() {
		return accountID;
	}

	public int getCost() {
		return cost;
	}
	
	public long getLongBalance() {
		return longBalance;
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
	
	public long getBBalance() {
		return bBalance;
	}
}
