package com.pip.gameaccount.handler.client;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.ILoginService;
import com.pip.gameaccount.LoginDetail;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.Logout1Message;
import com.pip.net.message.gameaccount._LogoutMessage;

public class Logout1Handler implements IMessageHandler {

	protected ILoginService loginService;
	protected ISession accountSkeleton;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();

	public Logout1Handler(ISession accountSkeleton, ILoginService loginService) {
		this.accountSkeleton = accountSkeleton;
		this.loginService = loginService;
	}

	public void handle(IMessage message) throws Exception {
		Logout1Message msg = (Logout1Message) message;
		Transaction tx = sf.getCurrentSession().beginTransaction();
		LoginDetail ld = null;
		try {
			ld = loginService.logout(msg.getAccountId(), msg.getKey(), msg
					.getSource().getId());
			tx.commit();
		} catch (Exception ex) {
			tx.rollback();
			ex.printStackTrace();
		} 
		if (ld != null) {
			_LogoutMessage newMsg = new _LogoutMessage(ld.getName(), ld.getKey());
			accountSkeleton.send(newMsg);
		}
	}

}
