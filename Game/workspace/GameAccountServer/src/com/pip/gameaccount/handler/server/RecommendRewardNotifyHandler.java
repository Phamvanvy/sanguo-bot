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
import com.pip.net.message.gameaccount.RecommendRewardNotifyMessage;

public class RecommendRewardNotifyHandler implements IMessageHandler {
	protected ISessionService sessionService;
	
	public RecommendRewardNotifyHandler(ISessionService sessionService) {
		this.sessionService = sessionService;
	}
	
	public void handle(IMessage message) throws Exception {
	    RecommendRewardNotifyMessage msg = (RecommendRewardNotifyMessage)message; 
	    for (ISession session : sessionService.getSessions()) {
	        if (session.getId().equals(msg.getGameCode())) {
	            session.send(msg);
	        }
	    }
	}
}
