package com.pip.gameaccount.handler.client;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.GameAccount;
import com.pip.gameaccount.ILoginService;
import com.pip.gameaccount.request.RenameRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.RenameMessage;

public class RenameHandler implements IMessageHandler {

	protected IRequestService requestService;
	protected ILoginService loginService;
	protected ISession accountSkeleton;
	
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	
	public RenameHandler(ISession accountSkeleton,ILoginService loginService,IRequestService requestService){
		this.accountSkeleton = accountSkeleton;
		this.loginService = loginService;
		this.requestService = requestService;
	}
	
	public void handle(IMessage message) throws Exception {
		RenameMessage msg = (RenameMessage) message;
		GameAccount ga = null;
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try {
			ga = loginService.getGameAccount(msg.getOldName());
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		}
		if (ga != null) {
			RenameMessage newMsg = new RenameMessage(msg.getOldName(),msg.getNewName());
			RenameRequest rq = new RenameRequest(msg
					.getSerial(), msg.getSource().getId(),  msg.getOldName(),msg.getNewName());
			requestService.add(newMsg.getSerial(), rq);
			accountSkeleton.send(newMsg);
		}
	}

}
