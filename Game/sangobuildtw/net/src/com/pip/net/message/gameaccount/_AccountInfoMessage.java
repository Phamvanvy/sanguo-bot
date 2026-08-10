package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class _AccountInfoMessage extends AbstractMessage {
	protected String name;
	
	public _AccountInfoMessage(int serial,String name){
		super(GameAccountMessageType._ACCOUNT_INFO,serial);
		this.name = name;
	}
	
	public _AccountInfoMessage(String name){
		super(GameAccountMessageType._ACCOUNT_INFO);
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
