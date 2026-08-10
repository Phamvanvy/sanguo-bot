package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class BindAccountMessage extends AbstractMessage {
	protected int accountID;
	protected int gameCode;
	protected String clientID;
	
	public BindAccountMessage(int serial, int id, int gc, String cid) {
		super(GameAccountMessageType.BIND_ACCOUNT,serial);
		this.accountID = id;
		this.gameCode = gc;
		this.clientID = cid;
	}
	
	public BindAccountMessage(int id, int gc, String cid) {
        super(GameAccountMessageType.BIND_ACCOUNT);
        this.accountID = id;
        this.gameCode = gc;
        this.clientID = cid;
    }
	
	public int getAccountID() {
		return accountID;
	}

	public int getGameCode() {
		return gameCode;
	}
	
	public String getClientID() {
		return clientID;
	}
}
