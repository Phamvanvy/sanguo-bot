package com.pip.gameaccount.qq;

import org.apache.log4j.Logger;

import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.message.gameaccount.QQBillingMessage;

public class QQBillingHandler implements IMessageHandler {

	protected QQBillingService service;
	
	static Logger log = Logger.getLogger(QQBillingHandler.class);
	
	public QQBillingHandler(QQBillingService service){
		this.service = service;
	}
	

	public void handle(IMessage message) throws Exception {
		QQBillingMessage msg = (QQBillingMessage) message;
		QQBilling billing = new QQBilling(msg.getUin(), msg.getLinkId(), msg
				.getGoodId(), msg.getCount(), msg.getSource().getId(),System.currentTimeMillis());
		service.addBilling(billing);
		log.info("QQBillingCreated Uin[" + msg.getUin() + "]LinkId["
				+ msg.getLinkId() + "]GoodId[" + msg.getGoodId() + "]Count["
				+ msg.getCount() + "]");
	}

}
