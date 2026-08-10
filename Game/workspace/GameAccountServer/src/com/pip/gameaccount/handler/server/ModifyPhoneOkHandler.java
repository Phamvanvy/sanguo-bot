package com.pip.gameaccount.handler.server;

import com.pip.gameaccount.ISessionService;
import com.pip.gameaccount.request.ModifyPhoneRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.ModifyPhoneOkMessage;

public class ModifyPhoneOkHandler implements IMessageHandler {

	protected IRequestService requestService;
	protected ISessionService sessionService;
	
	public ModifyPhoneOkHandler(IRequestService requestService,ISessionService sessionService){
		this.requestService = requestService;
		this.sessionService = sessionService;
	}
	
	public void handle(IMessage message) throws Exception {
		ModifyPhoneOkMessage msg = (ModifyPhoneOkMessage)message;
		ModifyPhoneRequest rq = (ModifyPhoneRequest)requestService.remove(msg.getSerial());
		if(rq!=null){
			ISession session = sessionService.getSession(rq.getSessionId());
			if(session!=null){
				ModifyPhoneOkMessage newMsg = new ModifyPhoneOkMessage(rq.getId());
				session.send(newMsg);
			}
		}
	}

}
