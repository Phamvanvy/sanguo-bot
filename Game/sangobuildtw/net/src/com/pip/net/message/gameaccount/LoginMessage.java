package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class LoginMessage extends AbstractMessage {
	
	protected String name;
	protected String key;
	
	public LoginMessage(String name,String key){
		super(GameAccountMessageType.LOGIN);
		this.name = name;
		this.key = key;
	}
	
	public LoginMessage(int serial,String name,String key){
		super(GameAccountMessageType.LOGIN,serial);
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
