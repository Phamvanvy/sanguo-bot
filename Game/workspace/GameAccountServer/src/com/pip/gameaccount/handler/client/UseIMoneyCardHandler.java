package com.pip.gameaccount.handler.client;

import com.pip.gameaccount.request.UseIMoneyCardRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.UseIMoneyCardMessage;

public class UseIMoneyCardHandler implements IMessageHandler {
	protected IRequestService requestService;
	protected ISession accountSkeleton;
	
	public UseIMoneyCardHandler(ISession accountSkeleton,IRequestService requestService){
		this.accountSkeleton = accountSkeleton;
		this.requestService = requestService;
	}
	
	public void handle(IMessage message) throws Exception {
		UseIMoneyCardMessage msg = (UseIMoneyCardMessage)message;
		UseIMoneyCardMessage newMsg = new UseIMoneyCardMessage(msg.getGameCode(), msg.getAccountID(), msg.getKey(), 
				msg.getCardno(), msg.getPassword());
		UseIMoneyCardRequest rq = new UseIMoneyCardRequest(msg.getSerial(), msg.getSource().getId());
		requestService.add(newMsg.getSerial(), rq);
		accountSkeleton.send(newMsg);
	}
}