package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class UseIMoneyCardMessage extends AbstractMessage {
	protected String gameCode;
	protected int accountID;
	protected String key;
	protected String cardno;
	protected String password;
	
	public UseIMoneyCardMessage(int serial, String gameCode, int accountID, String key, String cardno, String password) {
		super(GameAccountMessageType.USE_IMONEY_CARD, serial);
		this.gameCode = gameCode;
		this.accountID = accountID;
		this.key = key;
		this.cardno = cardno;
		this.password = password;
	}
	
	public UseIMoneyCardMessage(String gameCode, int accountID, String key, String cardno, String password) {
		super(GameAccountMessageType.USE_IMONEY_CARD);
		this.gameCode = gameCode;
		this.accountID = accountID;
		this.key = key;
		this.cardno = cardno;
		this.password = password;
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

	public String getCardno() {
		return cardno;
	}

	public String getPassword() {
		return password;
	}
}
