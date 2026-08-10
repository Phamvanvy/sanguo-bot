package com.pip.gameaccount.handler.client;

import org.apache.log4j.Logger;

import com.pip.gameaccount.ClientDetail;
import com.pip.gameaccount.ClientListManager;
import com.pip.gameaccount.ISessionService;
import com.pip.net.AcceptSession;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.ISession;
import com.pip.net.message.ServerLoginMessage;
import com.pip.net.message.ServerLoginOkMessage;

public class ServerLoginHandler implements IMessageHandler {

	protected ClientListManager manager;
	protected ISessionService sessionService;
	
	private static final Logger log = Logger.getLogger(ServerLoginHandler.class);
	
	public ServerLoginHandler(ClientListManager manager,ISessionService sessionService){
		this.manager = manager;
		this.sessionService = sessionService;
	}
	
	public void handle(IMessage message) throws Exception {
		ServerLoginMessage msg = (ServerLoginMessage)message;
		log.info("ServerId[" + msg.getServerId() + "]Password[" + msg.getPassword() + "]Try Login");
		ClientDetail cd = manager.getClientDetail(msg.getServerId());
		if(cd!=null&&cd.getPassWord().equals(msg.getPassword())){
			AcceptSession session = (AcceptSession)message.getSource();
			ISession oldSession = sessionService.getSession(msg.getServerId());
			if(oldSession!=null){
				// 如果重复的session来自同一个IP，则顶掉先前的session
				if (oldSession.getRemoteAddress().equals(session.getRemoteAddress()) && oldSession instanceof AcceptSession &&
						((AcceptSession)oldSession).timeout()) {
					oldSession.close();
					sessionService.removeSession(oldSession);
				} else {
					message.getSource().close();
					log.info("ClientServer["+session.getRemoteAddress()+"]["+msg.getServerId()+"] already exists.");
					return;
				}
			}
			session.setId(msg.getServerId());
			sessionService.addSession(session);
			ServerLoginOkMessage okMessage = new ServerLoginOkMessage(msg.getSerial());
			session.send(okMessage);
			log.info("ClientServer["+session.getRemoteAddress()+"]["+msg.getServerId()+"]Connected");
		}else{
			log.info("ClientServer["+message.getSource().getRemoteAddress()+"]LoginError");
			message.getSource().close();
		}		
	}

}
