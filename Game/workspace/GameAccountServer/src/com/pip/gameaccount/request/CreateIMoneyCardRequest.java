package com.pip.gameaccount.request;

import com.pip.net.SessionRequest;

public class CreateIMoneyCardRequest extends SessionRequest {
	public CreateIMoneyCardRequest(int id, String sessionId) {
		super(id, RequestType.CREATE_IMONEY_CARD, sessionId);
	}
}
