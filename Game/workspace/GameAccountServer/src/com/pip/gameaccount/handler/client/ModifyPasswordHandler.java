package com.pip.gameaccount.handler.client;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.GameAccount;
import com.pip.gameaccount.ILoginService;
import com.pip.gameaccount.request.ModifyPasswordRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.ModifyPasswordMessage;

public class ModifyPasswordHandler implements IMessageHandler {

	protected IRequestService requestService;
	protected ILoginService loginService;
	protected ISession accountSkeleton;
	
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	
	public ModifyPasswordHandler(ISession accountSkeleton,ILoginService loginService,IRequestService requestService){
		this.accountSkeleton = accountSkeleton;
		this.loginService = loginService;
		this.requestService = requestService;
	}
	
	public void handle(IMessage message) throws Exception {
		ModifyPasswordMessage msg = (ModifyPasswordMessage)message;
		GameAccount ga = null;
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try{
			ga = loginService.getGameAccount(msg.getName());
			tx.commit();
		}catch(Exception e){
			tx.rollback();
		}
		if(ga!=null){
			if(msg.getKey().equals(ga.getKey())){
				ModifyPasswordMessage newMsg = new ModifyPasswordMessage(msg.getName(),msg.getKey(),msg.getOldPassword(),msg.getPassword());
				ModifyPasswordRequest rq = new ModifyPasswordRequest(msg.getSerial(),msg.getSource().getId(),msg.getKey(),msg.getName());
				requestService.add(newMsg.getSerial(), rq);
				accountSkeleton.send(newMsg);
			}
		}
	}

}
