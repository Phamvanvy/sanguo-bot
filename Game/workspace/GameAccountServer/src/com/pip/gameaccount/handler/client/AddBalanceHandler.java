package com.pip.gameaccount.handler.client;

import com.pip.gameaccount.request.AddBalanceRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.AddBalanceMessage;

public class AddBalanceHandler implements IMessageHandler {
	protected ISession accountSkeleton;
	protected IRequestService requestService;
	
	public AddBalanceHandler(ISession accountSkeleton,IRequestService requestService){
		this.accountSkeleton = accountSkeleton;
		this.requestService = requestService;
	}
	
	public void handle(IMessage message) throws Exception {
	    AddBalanceMessage msg = (AddBalanceMessage) message;
        AddBalanceMessage newMsg = new AddBalanceMessage(msg.getAccountID(), msg.getValue(), msg.getReason());
        AddBalanceRequest rq = new AddBalanceRequest(msg.getSerial(), msg.getSource().getId());
        requestService.add(newMsg.getSerial(), rq);
        accountSkeleton.send(newMsg);
	}
}
