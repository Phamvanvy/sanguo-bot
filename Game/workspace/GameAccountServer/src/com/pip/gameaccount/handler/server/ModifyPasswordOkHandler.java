package com.pip.gameaccount.handler.server;

import com.pip.gameaccount.ISessionService;
import com.pip.gameaccount.request.ModifyPasswordRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.ModifyPasswordOkMessage;

public class ModifyPasswordOkHandler implements IMessageHandler {

	protected IRequestService requestService;
	protected ISessionService sessionService;
	
	public ModifyPasswordOkHandler(IRequestService requestService,ISessionService sessionService){
		this.requestService = requestService;
		this.sessionService = sessionService;
	}
	
	public void handle(IMessage message) throws Exception {
		ModifyPasswordOkMessage msg = (ModifyPasswordOkMessage)message;
		ModifyPasswordRequest rq = (ModifyPasswordRequest)requestService.remove(msg.getSerial());
		if(rq!=null){
			ISession session = sessionService.getSession(rq.getSessionId());
			if(session!=null){
				ModifyPasswordOkMessage newMsg = new ModifyPasswordOkMessage(rq.getId());
				session.send(newMsg);
			}
		}
	}

}
