package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class RecommendRequestMessage extends AbstractMessage {
    protected int accountID;
    protected String phone;
    protected String gamecode;
	
	public RecommendRequestMessage(int accountID, String phone, String gamecode) {
		super(GameAccountMessageType.RECOMMEND_REQUEST);
		this.accountID = accountID;
		this.phone = phone;
		this.gamecode = gamecode;
	}

	public int getAccountID() {
	    return accountID;
	}
	
	public String getPhone() {
	    return phone;
	}
	
	public String getGameCode() {
	    return gamecode;
	}
}
