package com.pip.gameaccount.request;

import com.pip.net.SessionRequest;

public class AddRecommendBalanceRequest extends SessionRequest {
	public AddRecommendBalanceRequest(int id,String sessionId) {
		super(id, RequestType.ADD_RECOMMEND_BALANCE, sessionId);
	}
}
