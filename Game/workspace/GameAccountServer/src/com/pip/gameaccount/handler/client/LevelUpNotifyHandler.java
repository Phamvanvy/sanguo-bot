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
import com.pip.net.message.gameaccount.OnlineTimeNotifyMessage;
import com.pip.net.message.gameaccount.RecommendRequestMessage;
import com.pip.net.message.gameaccount.RenameMessage;

public class LevelUpNotifyHandler implements IMessageHandler {
	protected ISession accountSkeleton;
	
	public LevelUpNotifyHandler(ISession accountSkeleton) {
		this.accountSkeleton = accountSkeleton;
	}
	
	public void handle(IMessage message) throws Exception {
		accountSkeleton.send(message);
	}
}
