package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class CreateIMoneyCardMessage extends AbstractMessage {
	protected String gameCode;
	protected int accountID;
	protected String key;
	protected int amount;
	
	public CreateIMoneyCardMessage(int serial, String gameCode, int accountID, String key, int amount) {
		super(GameAccountMessageType.CREATE_IMONEY_CARD, serial);
		this.gameCode = gameCode;
		this.accountID = accountID;
		this.key = key;
		this.amount = amount;
	}
	
	public CreateIMoneyCardMessage(String gameCode, int accountID, String key, int amount) {
		super(GameAccountMessageType.CREATE_IMONEY_CARD);
		this.gameCode = gameCode;
		this.accountID = accountID;
		this.key = key;
		this.amount = amount;
	}

	public String getGameCode() {
		return gameCode;
	}

	public int getAccountID() {
		return accountID;
	}
	
	public String getKey() {
		return key;
	}

	public int getAmount() {
		return amount;
	}
}
