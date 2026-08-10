package com.pip.gameaccount.request;

import com.pip.net.SessionRequest;

public class LegacyBuyRequest extends SessionRequest {
	
	protected String name;
	protected int value;
	
	public LegacyBuyRequest(int id,String sessionId,String name,int value){
		super(id,RequestType.LEGACY_BUY_RESULT,sessionId);
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}
	
}
