package com.pip.gameaccount.request;

import com.pip.net.SessionRequest;

public class ModifyPasswordRequest extends SessionRequest {
	
	protected String name;
	protected String key;
	
	public ModifyPasswordRequest(int id,String sessionId,String name,String key){
		super(id,RequestType.MODIFY_PASSWORD,sessionId);
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
