package com.pip.gameaccount.request;

import com.pip.net.SessionRequest;

public class AccountRegRequest extends SessionRequest {
	public AccountRegRequest(int id,String serverId){
		super(id,RequestType.ACCOUNT_REG,serverId);
	}
}
