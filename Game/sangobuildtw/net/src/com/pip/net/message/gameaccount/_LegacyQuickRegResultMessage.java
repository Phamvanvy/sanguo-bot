package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class _LegacyQuickRegResultMessage extends AbstractMessage {
	
	protected int accountId;
	protected String name;
	protected String password;
	protected byte result;
	
	public _LegacyQuickRegResultMessage(int serial,int accountId,String name,String password,byte result){
		super(GameAccountMessageType._LEGACY_QUICKREG_RESULT,serial,true);
		this.accountId = accountId;
		this.name = name;
		this.password = password;
		this.result = result;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}


	public byte getResult() {
		return result;
	}

	public int getAccountId() {
		return accountId;
	}	
}
