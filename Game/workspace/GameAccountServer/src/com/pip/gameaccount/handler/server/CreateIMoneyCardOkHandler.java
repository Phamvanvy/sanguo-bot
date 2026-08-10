package com.pip.gameaccount.handler.server;

import com.pip.gameaccount.ISessionService;
import com.pip.gameaccount.request.CreateIMoneyCardRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.CreateIMoneyCardOkMessage;

public class CreateIMoneyCardOkHandler implements IMessageHandler {
	protected IRequestService requestService;
	protected ISessionService sessionService;
	
	public CreateIMoneyCardOkHandler(IRequestService requestService,ISessionService sessionService) {
		this.requestService = requestService;
		this.sessionService = sessionService;
	}
	
	public void handle(IMessage message) throws Exception {
		CreateIMoneyCardOkMessage msg = (CreateIMoneyCardOkMessage)message;
		CreateIMoneyCardRequest rq = (CreateIMoneyCardRequest)requestService.remove(msg.getSerial());
		if (rq != null) {
			ISession session = sessionService.getSession(rq.getSessionId());
			if (session != null) {
				CreateIMoneyCardOkMessage newMsg = new CreateIMoneyCardOkMessage(rq.getId(), msg.getAccountID(), 
						msg.getCost(), msg.getBalance(), msg.getCardno(), msg.getPassword(), msg.getLongBalance());
				session.send(newMsg);
			}
		}
	}
}
