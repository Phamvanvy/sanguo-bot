package com.pip.gameaccount.handler.client;

import com.pip.gameaccount.request.AccountRegRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.AccountRegMessage;

public class AccountRegHandler implements IMessageHandler {

	protected IRequestService requestService;
	protected ISession accountSkeleton;

	public AccountRegHandler(ISession accountSkeleton,
			IRequestService requestService) {
		this.accountSkeleton = accountSkeleton;
		this.requestService = requestService;
	}

	public void handle(IMessage message) throws Exception {
		AccountRegMessage msg = (AccountRegMessage) message;
		AccountRegMessage newMsg = new AccountRegMessage(msg.getName(), 
		        msg.getPhone(), msg.getRecommend(), msg.getRecommendId(), 
		        msg.getModel(), msg.getService(), msg.getVersion(), 
		        msg.getRealPhone(), msg.getInitPassword());
		AccountRegRequest request = new AccountRegRequest(msg.getSerial(),msg.getSource().getId());
		requestService.add(newMsg.getSerial(),request);
		accountSkeleton.send(newMsg);
	}

}
