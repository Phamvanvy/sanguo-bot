package com.pip.gameaccount.request;

import com.pip.net.SessionRequest;

public class AccountInfoRequest extends SessionRequest {
	public AccountInfoRequest(int id,String sessionId){
		super(id,RequestType.ACCOUNT_INFO,sessionId);
	}
}
