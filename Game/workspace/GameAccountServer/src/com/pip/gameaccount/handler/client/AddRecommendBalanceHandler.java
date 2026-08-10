package com.pip.gameaccount.handler.client;

import org.hibernate.Transaction;

import com.pip.gameaccount.GameAccount;
import com.pip.gameaccount.request.AddRecommendBalanceRequest;
import com.pip.gameaccount.request.RenameRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.AddRecommendBalanceMessage;
import com.pip.net.message.gameaccount.RenameMessage;

public class AddRecommendBalanceHandler implements IMessageHandler {
	protected ISession accountSkeleton;
	protected IRequestService requestService;
	
	public AddRecommendBalanceHandler(ISession accountSkeleton,IRequestService requestService){
		this.accountSkeleton = accountSkeleton;
		this.requestService = requestService;
	}
	
	public void handle(IMessage message) throws Exception {
	    AddRecommendBalanceMessage msg = (AddRecommendBalanceMessage) message;
        AddRecommendBalanceMessage newMsg = new AddRecommendBalanceMessage(msg.getAccountID(), msg.getValue(), msg.getValue2());
        AddRecommendBalanceRequest rq = new AddRecommendBalanceRequest(msg.getSerial(), msg.getSource().getId());
        requestService.add(newMsg.getSerial(), rq);
        accountSkeleton.send(newMsg);
	}
}
