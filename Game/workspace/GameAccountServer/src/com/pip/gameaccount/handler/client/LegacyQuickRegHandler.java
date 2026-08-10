package com.pip.gameaccount.handler.client;

import com.pip.gameaccount.request.LegacyQuickRegRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.LegacyQuickRegMessage;

public class LegacyQuickRegHandler implements IMessageHandler {
	
	protected ISession accountSkeleton;
	protected IRequestService requestService;
	
	public LegacyQuickRegHandler(ISession accountSkeleton,IRequestService requestService){
		this.accountSkeleton = accountSkeleton;
		this.requestService = requestService;
	}
	
	public void handle(IMessage message) throws Exception {
		LegacyQuickRegMessage msg = (LegacyQuickRegMessage)message;
		LegacyQuickRegMessage newMsg = new LegacyQuickRegMessage(msg.getPhone(),msg.getVersion(),msg.getModel(),msg.getServiceId(),msg.getRealPhone());
		LegacyQuickRegRequest request = new LegacyQuickRegRequest(msg.getSerial(),msg.getSource().getId());
		requestService.add(newMsg.getSerial(), request);
		accountSkeleton.send(newMsg);
	}

}
