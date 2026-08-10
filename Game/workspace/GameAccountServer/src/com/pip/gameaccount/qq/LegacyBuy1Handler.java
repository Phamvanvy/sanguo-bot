package com.pip.gameaccount.qq;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.message.gameaccount.LegacyBuy1Message;
import com.pip.net.message.gameaccount.LegacyBuyResultMessage;

public class LegacyBuy1Handler implements IMessageHandler {

	protected QQLoginService loginService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	static Logger log = Logger.getLogger(LegacyBuy1Handler.class);
	
	public LegacyBuy1Handler(QQLoginService loginService){
		this.loginService = loginService;
	}
	
	public void handle(IMessage message) throws Exception {
		LegacyBuy1Message msg = (LegacyBuy1Message)message;
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try{
			log.info("ID["+msg.getAccountId()+"]BuyPrice["+msg.getValue()+"]Key["+msg.getKey()+"]TRY");
			QQGameAccount ga = loginService.getGameAccount(msg.getAccountId());
			QQGameAccount ga1 = loginService.decBalance(ga.getName(), msg.getKey(), msg.getValue());
			tx.commit();
			log.info("ID["+msg.getAccountId()+"]BuyPrice["+msg.getValue()+"]Key["+msg.getKey()+"]OK");
			LegacyBuyResultMessage newMsg = new LegacyBuyResultMessage(msg.getSerial(),true,ga1.getBalance(),msg.getValue(),"",ga1.getBalance());
			msg.getSource().send(newMsg);
		}catch(Exception e){
			LegacyBuyResultMessage newMsg = new LegacyBuyResultMessage(msg.getSerial(),false,0,0,"",0);
			msg.getSource().send(newMsg);
		}
	}

}
