package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class GetAccountNameOkMessage extends AbstractMessage {
	
	protected String name;
	
	public GetAccountNameOkMessage(int serial,String name){
		super(GameAccountMessageType.GET_ACCOUNTNAME_OK,serial);
		this.name = name;
	}
	
	public GetAccountNameOkMessage(String name){
		super(GameAccountMessageType.GET_ACCOUNTNAME_OK);
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
