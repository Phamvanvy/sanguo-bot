package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class _LogoutMessage extends AbstractMessage {
	
	protected String name;
	protected String key;
	
	public _LogoutMessage(String name,String key){
		super(GameAccountMessageType._LOGOUT);
		this.name = name;
		this.key = key;
	}

	public _LogoutMessage(int serial,String name,String key){
		super(GameAccountMessageType._LOGOUT,serial);
		this.name = name;
		this.key = key;
	}	
	
	public String getName() {
		return name;
	}

	public String getKey() {
		return key;
	}
	
}
