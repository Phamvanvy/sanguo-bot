package com.pip.gameaccount.handler.client;

import com.pip.gameaccount.request.CreateIMoneyCardRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.CreateIMoneyCardMessage;

public class CreateIMoneyCardHandler implements IMessageHandler {
	protected IRequestService requestService;
	protected ISession accountSkeleton;
	
	public CreateIMoneyCardHandler(ISession accountSkeleton,IRequestService requestService){
		this.accountSkeleton = accountSkeleton;
		this.requestService = requestService;
	}
	
	public void handle(IMessage message) throws Exception {
		CreateIMoneyCardMessage msg = (CreateIMoneyCardMessage)message;
		CreateIMoneyCardMessage newMsg = new CreateIMoneyCardMessage(msg.getGameCode(), msg.getAccountID(), msg.getKey(), msg.getAmount());
		CreateIMoneyCardRequest rq = new CreateIMoneyCardRequest(msg.getSerial(), msg.getSource().getId());
		requestService.add(newMsg.getSerial(), rq);
		accountSkeleton.send(newMsg);
	}
}
