package com.pip.gameaccount.qq;

import org.apache.log4j.Logger;

import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.message.gameaccount.Logout1Message;

public class LogoutHandler implements IMessageHandler {

	static final Logger log = Logger.getLogger(LogoutHandler.class);
	
	public void handle(IMessage message) throws Exception {
		Logout1Message msg = (Logout1Message)message;
		log.info("AccountId["+msg.getAccountId()+"]Logouted");
	}

}
