package com.pip.gameaccount.qq;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.message.gameaccount.ChangeStatusMessage;

public class ChangeStatusHandler implements IMessageHandler {

	protected QQLoginService loginService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	private static final Logger log = Logger.getLogger(ChangeStatusHandler.class);
	
	public ChangeStatusHandler(QQLoginService loginService){
		this.loginService = loginService;
	}
	
	public void handle(IMessage message) throws Exception {
		ChangeStatusMessage msg = (ChangeStatusMessage)message;
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try{
			QQGameAccount account = loginService.changeStatus(msg.getAccountId(), msg.getStatus());
			tx.commit();
		}catch(Exception ex){
			tx.rollback();
			log.error(ex,ex);
		}
	}

}
