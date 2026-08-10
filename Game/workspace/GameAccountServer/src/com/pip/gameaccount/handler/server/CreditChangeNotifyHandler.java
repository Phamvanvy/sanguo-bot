package com.pip.gameaccount.handler.server;

import java.util.Date;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.IGameAccountService;
import com.pip.gameaccount.ISessionService;
import com.pip.gameaccount.request.AccountRegRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.AccountRegOkMessage;
import com.pip.net.message.gameaccount.CreditChangeNotifyMessage;

public class CreditChangeNotifyHandler implements IMessageHandler {
	protected ISessionService sessionService;
	
	public CreditChangeNotifyHandler(ISessionService sessionService) {
		this.sessionService = sessionService;
	}
	
	public void handle(IMessage message) throws Exception {
	    CreditChangeNotifyMessage msg = (CreditChangeNotifyMessage)message;
	    CreditChangeNotifyMessage newMsg = new CreditChangeNotifyMessage(msg.getAccountID(), msg.getCredit());
	    for (ISession session : sessionService.getSessions()) {
	        session.send(newMsg);
	    }
	}
}
