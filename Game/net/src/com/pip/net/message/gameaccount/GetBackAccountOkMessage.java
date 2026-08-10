package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class GetBackAccountOkMessage extends AbstractMessage {
	protected int accountID;
	protected String accountName;
	protected String password;
	protected String partition;
	
	public GetBackAccountOkMessage(int serial, int id, String name, String pass, String par) {
		super(GameAccountMessageType.GET_BACK_ACCOUNT_OK,serial);
		this.accountID = id;
		this.accountName = name;
		this.password = pass;
		this.partition = par;
	}
	
	public int getAccountID() {
		return accountID;
	}
	
	public String getAccountName() {
		return accountName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getPartition() {
		return partition;
	}
}
