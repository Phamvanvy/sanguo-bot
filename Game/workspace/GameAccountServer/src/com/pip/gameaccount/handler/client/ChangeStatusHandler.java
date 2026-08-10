package com.pip.gameaccount.handler.client;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.GameAccount;
import com.pip.gameaccount.ILoginService;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.ChangeStatusMessage;
import com.pip.net.message.gameaccount._ChangeStatusMessage;

public class ChangeStatusHandler implements IMessageHandler {

	protected ISession accountSkeleton;
	protected ILoginService loginService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	
	public ChangeStatusHandler(ILoginService loginService,ISession accountSkeleton){
		this.loginService = loginService;
		this.accountSkeleton = accountSkeleton;
	}
	
	public void handle(IMessage message) throws Exception {
		ChangeStatusMessage msg = (ChangeStatusMessage)message;
		Transaction tx = sf.getCurrentSession().beginTransaction();
		
		try {
			GameAccount ga = loginService.getGameAccount(msg.getAccountId());
			tx.commit();
			if(ga!=null){
				_ChangeStatusMessage newMsg = new  _ChangeStatusMessage(ga.getName(),msg.getStatus(),msg.getMessage());
				accountSkeleton.send(newMsg);	
			}
		} catch (Exception e) {
			tx.rollback();
		} 
	}

}
