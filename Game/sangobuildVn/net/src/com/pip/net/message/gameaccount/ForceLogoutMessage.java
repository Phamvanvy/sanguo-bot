package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class ForceLogoutMessage extends AbstractMessage {
	
	protected String name;
	protected String key;
	protected int id;
	
	public ForceLogoutMessage(int serial,int id,String name,String key){
		super(GameAccountMessageType.FORCE_LOGOUT,serial);
		this.id = id;
		this.name = name;
		this.key = key;		
	}
	
	public ForceLogoutMessage(int id,String name,String key){
		super(GameAccountMessageType.FORCE_LOGOUT);
		this.id = id;
		this.name = name;
		this.key = key;
	}
	
	public int getId(){
		return id;
	}

	public String getName() {
		return name;
	}

	public String getKey() {
		return key;
	}
	
	
}
