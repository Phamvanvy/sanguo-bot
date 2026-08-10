package com.pip.gameaccount.handler.client;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.GameAccount;
import com.pip.gameaccount.ILoginService;
import com.pip.gameaccount.request.ModifyPhoneRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.ModifyPhoneMessage;

public class ModifyPhoneHandler implements IMessageHandler {

	protected IRequestService requestService;
	protected ILoginService loginService;
	protected ISession accountSkeleton;
	
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	
	public ModifyPhoneHandler(ISession accountSkeleton,ILoginService loginService,IRequestService requestService){
		this.accountSkeleton = accountSkeleton;
		this.loginService = loginService;
		this.requestService = requestService;
	}
	
	public void handle(IMessage message) throws Exception {
		ModifyPhoneMessage msg = (ModifyPhoneMessage)message;
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
				ModifyPhoneMessage newMsg = new ModifyPhoneMessage(msg.getName(),msg.getKey(),msg.getPhone());
				ModifyPhoneRequest rq = new ModifyPhoneRequest(msg.getSerial(),msg.getSource().getId(),msg.getKey(),msg.getName());
				requestService.add(newMsg.getSerial(), rq);
				accountSkeleton.send(newMsg);
			}
		}
	}

}
