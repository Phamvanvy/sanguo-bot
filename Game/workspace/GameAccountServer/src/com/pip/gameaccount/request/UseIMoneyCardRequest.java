package com.pip.gameaccount.request;

import com.pip.net.SessionRequest;

public class UseIMoneyCardRequest extends SessionRequest {
	public UseIMoneyCardRequest(int id, String sessionId) {
		super(id, RequestType.USE_IMONEY_CARD, sessionId);
	}
}
