package com.pip.gameaccount.handler.server;

import com.pip.gameaccount.ISessionService;
import com.pip.gameaccount.request.AccountInfoRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.AccountInfoOkMessage;

public class AccountInfoOkHandler implements IMessageHandler {
	
	protected ISessionService sessionService;
	protected IRequestService requestService;
	
	public AccountInfoOkHandler(ISessionService sessionService,IRequestService requestService){
		this.sessionService = sessionService;
		this.requestService = requestService;
	}
	
	public void handle(IMessage message) throws Exception {
		AccountInfoOkMessage msg = (AccountInfoOkMessage) message;
		AccountInfoRequest rq = (AccountInfoRequest) requestService
				.remove(message.getSerial());
		if (rq != null) {
			AccountInfoOkMessage newMsg = new AccountInfoOkMessage(rq.getId(),
					msg.getAccountId(), msg.getName(), msg.getPassword(), msg
							.getPhone());
			ISession session = sessionService.getSession(rq.getSessionId());
			if(session!=null){
				session.send(newMsg);
			}
		}
	}

}
