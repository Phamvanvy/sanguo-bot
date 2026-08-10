package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class GetBackAccountMessage extends AbstractMessage {
	protected int gameCode;
	protected String clientID;
	
	public GetBackAccountMessage(int serial, int gc, String cid) {
		super(GameAccountMessageType.GET_BACK_ACCOUNT,serial);
		this.gameCode = gc;
		this.clientID = cid;
	}
	
	public GetBackAccountMessage(int gc, String cid) {
        super(GameAccountMessageType.GET_BACK_ACCOUNT);
        this.gameCode = gc;
        this.clientID = cid;
    }
	
	public int getGameCode() {
		return gameCode;
	}
	
	public String getClientID() {
		return clientID;
	}
}
