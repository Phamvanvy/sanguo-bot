package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class ChangeStatusMessage extends AbstractMessage {
	
	protected int accountId;
	protected int status;
	protected String message;
	
	public ChangeStatusMessage(int serial,int accountId,int status,String message){
		super(GameAccountMessageType.CHANGE_STATUS,serial);
		this.accountId = accountId;
		this.status = status;
		this.message = message;
	}
	
	public ChangeStatusMessage(int accountId,int status,String message){
		super(GameAccountMessageType.CHANGE_STATUS);
		this.accountId = accountId;
		this.status = status;
		this.message = message;
	}

	public int getAccountId() {
		return accountId;
	}


	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}
}
