package com.pip.gameaccount.handler.server;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.ILoginService;
import com.pip.gameaccount.ISessionService;
import com.pip.gameaccount.request.AddBalanceRequest;
import com.pip.gameaccount.request.AddRecommendBalanceRequest;
import com.pip.gameaccount.request.RenameRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.AddBalanceOkMessage;
import com.pip.net.message.gameaccount.AddRecommendBalanceOkMessage;
import com.pip.net.message.gameaccount.RenameOkMessage;

public class AddBalanceOkHandler implements IMessageHandler {
	protected IRequestService requestService;
	protected ISessionService sessionService;
	private static final Logger log = Logger.getLogger(AddBalanceOkHandler.class);
	
	public AddBalanceOkHandler(IRequestService requestService,ISessionService sessionService){
		this.requestService = requestService;
		this.sessionService = sessionService;
	}
	
	public void handle(IMessage message) throws Exception {
		AddBalanceOkMessage msg = (AddBalanceOkMessage)message;
		AddBalanceRequest rq = (AddBalanceRequest)requestService.remove(msg.getSerial());
		if (rq!=null) {
			ISession session = sessionService.getSession(rq.getSessionId());
			if (session!=null) {
				AddBalanceOkMessage newMsg = new AddBalanceOkMessage(rq.getId(), msg.getAccountID(), msg.getValue());
				session.send(newMsg);
			}
		}
	}
}
