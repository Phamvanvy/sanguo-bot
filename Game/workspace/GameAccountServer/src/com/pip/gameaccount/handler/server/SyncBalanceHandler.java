package com.pip.gameaccount.handler.server;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.GameAccount;
import com.pip.gameaccount.ILoginService;
import com.pip.gameaccount.ISessionService;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.GetAccountNameOkMessage;
import com.pip.net.message.gameaccount.SyncBalanceMessage;

public class SyncBalanceHandler implements IMessageHandler {
	protected ISessionService sessionService;
	protected ILoginService loginService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	
	public SyncBalanceHandler(ISessionService sessionService, ILoginService loginService) {
		this.sessionService = sessionService;
		this.loginService = loginService;
	}
	
	public void handle(IMessage message) throws Exception {
	    SyncBalanceMessage msg = (SyncBalanceMessage)message;
	    Transaction tx = sf.getCurrentSession().beginTransaction();
		try {
			GameAccount ga = loginService.getGameAccount(msg.getAccountId());
			tx.commit();
			if (ga != null) {
		    	ISession session = sessionService.getSession(ga.getServerId());
		    	if (session != null) {
		    		session.send(msg);
		    	}
		    }
		} catch (Exception e) {
			tx.rollback();
		} 
	}
}
