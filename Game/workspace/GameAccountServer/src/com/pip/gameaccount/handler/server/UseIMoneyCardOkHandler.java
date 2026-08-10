package com.pip.gameaccount.handler.server;

import com.pip.gameaccount.ISessionService;
import com.pip.gameaccount.request.UseIMoneyCardRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.UseIMoneyCardOkMessage;

public class UseIMoneyCardOkHandler implements IMessageHandler {
	protected IRequestService requestService;
	protected ISessionService sessionService;
	
	public UseIMoneyCardOkHandler(IRequestService requestService,ISessionService sessionService) {
		this.requestService = requestService;
		this.sessionService = sessionService;
	}
	
	public void handle(IMessage message) throws Exception {
		UseIMoneyCardOkMessage msg = (UseIMoneyCardOkMessage)message;
		UseIMoneyCardRequest rq = (UseIMoneyCardRequest)requestService.remove(msg.getSerial());
		if (rq != null) {
			ISession session = sessionService.getSession(rq.getSessionId());
			if (session != null) {
				UseIMoneyCardOkMessage newMsg = new UseIMoneyCardOkMessage(rq.getId(), msg.getAccountID(),
						msg.getAmount(), msg.getBalance(), msg.getLongBalance());
				session.send(newMsg);
			}
		}
	}
}
