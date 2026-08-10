package com.pip.gameaccount.qq;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.message.gameaccount.AccountInfoMessage;
import com.pip.net.message.gameaccount.AccountInfoOkMessage;

public class AccountInfoHandler implements IMessageHandler {

	
	protected QQLoginService loginService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	private static final Logger log = Logger.getLogger(AccountInfoHandler.class);
	
	public AccountInfoHandler(QQLoginService loginService){
		this.loginService = loginService;
	}
	
	public void handle(IMessage message) throws Exception {
		AccountInfoMessage msg = (AccountInfoMessage) message;
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try {
			QQGameAccount account = loginService.getGameAccount(msg
					.getAccountId());
			tx.commit();
			AccountInfoOkMessage newMsg = new AccountInfoOkMessage(msg
					.getSerial(), account.getId(), account.getName(), account
					.getKey(), "");
			message.getSource().send(newMsg);
		} catch (Exception e) {
			tx.rollback();
			log.error(e, e);
		}
	}

}
