package com.pip.gameaccount.request;

import com.pip.net.SessionRequest;

public class AddBalanceRequest extends SessionRequest {
	public AddBalanceRequest(int id,String sessionId) {
		super(id, RequestType.ADD_BALANCE, sessionId);
	}
}
