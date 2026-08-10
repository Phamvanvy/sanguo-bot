package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class ModifyPasswordMessage extends AbstractMessage {
	
	protected String name;
	protected String key;
	protected String password;
	protected String oldPassword;
	
	public ModifyPasswordMessage(int serial,String name,String key,String oldPassword,String password){
		super(GameAccountMessageType.MODIFY_PASSWORD,serial);
		this.name = name;
		this.key = key;
		this.password = password;
		this.oldPassword = oldPassword;
	}
	
	public ModifyPasswordMessage(String name,String key,String oldPassword,String password){
		super(GameAccountMessageType.MODIFY_PASSWORD);
		this.name = name;
		this.key = key;
		this.password = password;
		this.oldPassword = oldPassword;
	}

	public String getName() {
		return name;
	}

	public String getKey() {
		return key;
	}

	public String getPassword() {
		return password;
	}

	public String getOldPassword() {
		return oldPassword;
	}
}
