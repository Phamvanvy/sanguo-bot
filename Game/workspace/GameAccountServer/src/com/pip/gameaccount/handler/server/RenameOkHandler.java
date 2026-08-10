package com.pip.gameaccount.handler.server;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.ILoginService;
import com.pip.gameaccount.ISessionService;
import com.pip.gameaccount.request.RenameRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.RenameOkMessage;

public class RenameOkHandler implements IMessageHandler {

	protected IRequestService requestService;
	protected ISessionService sessionService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	protected ILoginService loginService;
	private static final Logger log = Logger.getLogger(RenameOkHandler.class);
	
	public RenameOkHandler(IRequestService requestService,ISessionService sessionService,ILoginService loginService){
		this.requestService = requestService;
		this.sessionService = sessionService;
		this.loginService = loginService;
	}
	
	public void handle(IMessage message) throws Exception {
		RenameOkMessage msg = (RenameOkMessage)message;
		RenameRequest rq = (RenameRequest)requestService.remove(msg.getSerial());
		if(rq!=null){
			Transaction tx = sf.getCurrentSession().beginTransaction();
			try{
				loginService.rename(rq.getOldName(), rq.getNewName());
				tx.commit();
			}catch(Exception ex){
				tx.rollback();
				log.error(ex,ex);
			}
			ISession session = sessionService.getSession(rq.getSessionId());
			if(session!=null){
				RenameOkMessage newMsg = new RenameOkMessage(rq.getId());
				session.send(newMsg);
			}
		}
	}

}
