package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class ModifyPhoneMessage extends AbstractMessage {
	protected String name;
	protected String key;
	protected String phone;
	
	public ModifyPhoneMessage(int serial,String name,String key,String phone){
		super(GameAccountMessageType.MODIFY_PHONE,serial);
		this.name = name;
		this.key = key;
		this.phone = phone;
	}
	
	public ModifyPhoneMessage(String name,String key,String phone){
		super(GameAccountMessageType.MODIFY_PHONE);
		this.name = name;
		this.key = key;
		this.phone = phone;
	}

	public String getName() {
		return name;
	}

	public String getKey() {
		return key;
	}

	public String getPhone() {
		return phone;
	}
}
