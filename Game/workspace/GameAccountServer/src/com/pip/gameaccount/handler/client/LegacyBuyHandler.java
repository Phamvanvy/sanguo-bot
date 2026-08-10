package com.pip.gameaccount.handler.client;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.GameAccount;
import com.pip.gameaccount.ILoginService;
import com.pip.gameaccount.request.LegacyBuyRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.LegacyBuyMessage;
import com.pip.net.message.gameaccount._BuyMessage;

public class LegacyBuyHandler implements IMessageHandler {

	protected ISession accountSkeleton;
	protected IRequestService requestService;
	protected ILoginService loginService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();

	public LegacyBuyHandler(ISession accountSkeleton,
			ILoginService loginService, IRequestService requestService) {
		this.accountSkeleton = accountSkeleton;
		this.requestService = requestService;
		this.loginService = loginService;
	}

	public void handle(IMessage message) throws Exception {
		LegacyBuyMessage msg = (LegacyBuyMessage) message;
		Transaction tx = sf.getCurrentSession().beginTransaction();
		GameAccount ga = null;
		try {
			ga = loginService.getGameAccount(msg.getName());
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} 
		if (ga != null) {
			String key = ga.getKey();
			if (key != null&&msg.getKey().equals(key)) {
				_BuyMessage newMsg = new _BuyMessage(ga.getName(), key,
						msg.getValue());
				LegacyBuyRequest request = new LegacyBuyRequest(
						msg.getSerial(), msg.getSource().getId(), ga
								.getName(), msg.getValue());
				requestService.add(newMsg.getSerial(), request);
				accountSkeleton.send(newMsg);
				return;
			}
		}
		ErrorMessage newMsg = new ErrorMessage(msg.getSerial(), 110);
		message.getSource().send(newMsg);

	}

}
