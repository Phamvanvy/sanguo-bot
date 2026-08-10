package com.pip.gameaccount.handler.client;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.GameAccount;
import com.pip.gameaccount.ILoginService;
import com.pip.gameaccount.request.AccountInfoRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.AccountInfoMessage;
import com.pip.net.message.gameaccount._AccountInfoMessage;

public class AccountInfoHandler implements IMessageHandler {
	
	protected ISession accountSkeleton;
	protected IRequestService requestService;
	protected ILoginService loginService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	
	public AccountInfoHandler(ILoginService loginService,ISession accountSkeleton,IRequestService requestService){
		this.loginService = loginService;
		this.accountSkeleton = accountSkeleton;
		this.requestService = requestService;
	}
	
	public void handle(IMessage message) throws Exception {
		AccountInfoMessage msg = (AccountInfoMessage)message;
		if(msg.getAccountId()!=-1){
			Transaction tx = sf.getCurrentSession().beginTransaction();
			try {
				GameAccount ga = loginService
						.getGameAccount(msg.getAccountId());
				tx.commit();
				if (ga != null) {
					_AccountInfoMessage newMsg = new _AccountInfoMessage(ga
							.getName());
					AccountInfoRequest request = new AccountInfoRequest(msg
							.getSerial(), msg.getSource().getId());
					requestService.add(newMsg.getSerial(), request);
					accountSkeleton.send(newMsg);
				}
			} catch (Exception e) {
				tx.rollback();
			} 
		}else{
			_AccountInfoMessage newMsg = new _AccountInfoMessage(msg.getName());
			AccountInfoRequest request = new AccountInfoRequest(msg.getSerial(),msg.getSource().getId());
			requestService.add(newMsg.getSerial(),request);
			accountSkeleton.send(newMsg);			
		}

	}

}
