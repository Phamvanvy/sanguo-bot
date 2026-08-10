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

public class AccountRegOkHandler implements IMessageHandler {

	protected IGameAccountService service;
	protected IRequestService requestService;
	protected ISessionService sessionService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	
	public AccountRegOkHandler(IGameAccountService service,IRequestService requestService,ISessionService sessionService){
		this.service = service;
		this.requestService = requestService;
		this.sessionService = sessionService;
	}
	
	public void handle(IMessage message) throws Exception {
		AccountRegRequest rq = (AccountRegRequest) requestService
				.remove(message.getSerial());
		if (rq != null) {
			AccountRegOkMessage msg = (AccountRegOkMessage) message;
			String name = msg.getName();
			Transaction tx = sf.getCurrentSession().beginTransaction();
			service.createGameAccount(msg.getAccountId(),name, new Date());
			tx.commit();
			AccountRegOkMessage newMsg = new AccountRegOkMessage(rq.getId(),msg.getAccountId(),
					name, msg.getPassword());
			ISession session = sessionService.getSession(rq.getSessionId());
			if(session!=null)
				session.send(newMsg);

		}

	}

}
