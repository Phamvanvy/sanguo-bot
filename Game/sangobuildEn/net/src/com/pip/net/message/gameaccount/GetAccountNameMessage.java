package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class GetAccountNameMessage extends AbstractMessage {
	
	protected int accountId;
	
	public GetAccountNameMessage(int serial,int accountId){
		super(GameAccountMessageType.GET_ACCOUNTNAME,serial);
		this.accountId = accountId;
	}
	
	public GetAccountNameMessage(int accountId){
		super(GameAccountMessageType.GET_ACCOUNTNAME);
		this.accountId = accountId;
	}

	public int getAccountId() {
		return accountId;
	}
}
