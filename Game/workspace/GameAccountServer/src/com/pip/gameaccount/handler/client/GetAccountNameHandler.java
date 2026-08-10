package com.pip.gameaccount.handler.client;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.GameAccount;
import com.pip.gameaccount.ILoginService;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.message.gameaccount.GetAccountNameMessage;
import com.pip.net.message.gameaccount.GetAccountNameOkMessage;

public class GetAccountNameHandler implements IMessageHandler {

	protected ILoginService loginService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	
	public GetAccountNameHandler(ILoginService loginService){
		this.loginService = loginService;
	}
	
	public void handle(IMessage message) throws Exception {
		GetAccountNameMessage msg = (GetAccountNameMessage)message;
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try {
			GameAccount ga = loginService.getGameAccount(msg.getAccountId());
			tx.commit();
			if(ga!=null){
				GetAccountNameOkMessage newMsg = new GetAccountNameOkMessage(msg.getSerial(),ga.getName());
				msg.getSource().send(newMsg);
			}
		} catch (Exception e) {
			tx.rollback();
		} 
	}

}
