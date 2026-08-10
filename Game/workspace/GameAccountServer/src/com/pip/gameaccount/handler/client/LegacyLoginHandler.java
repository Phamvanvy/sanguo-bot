package com.pip.gameaccount.handler.client;

import com.pip.gameaccount.request.LegacyLoginRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.LegacyLoginMessage;

public class LegacyLoginHandler implements IMessageHandler {
	
	protected ISession accountSkeleton;
	protected IRequestService requestService;

	public LegacyLoginHandler(ISession accountSkeleton,IRequestService requestService){
		this.accountSkeleton = accountSkeleton;
		this.requestService = requestService;
	}
	
	public void handle(IMessage message){
		LegacyLoginMessage msg = (LegacyLoginMessage)message;
		LegacyLoginMessage newMsg = new LegacyLoginMessage(msg.getName(),msg.getPassword(),msg.getPhone());
		LegacyLoginRequest request = new LegacyLoginRequest(msg.getSerial(),msg.getSource().getId());
		requestService.add(newMsg.getSerial(), request);
		accountSkeleton.send(newMsg);
	}

}
