package com.pip.gameaccount.request;

import com.pip.net.SessionRequest;

public class ModifyPhoneRequest extends SessionRequest {
	protected String name;
	protected String key;
	
	public ModifyPhoneRequest(int id,String sessionId,String name,String key){
		super(id,RequestType.MODIFY_PHONE,sessionId);
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
