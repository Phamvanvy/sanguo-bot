package com.pip.gameaccount.handler.server;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.GameAccount;
import com.pip.gameaccount.ILoginService;
import com.pip.gameaccount.ISessionService;
import com.pip.gameaccount.LoginDetail;
import com.pip.gameaccount.request.LegacyFeeRequest;
import com.pip.gameaccount.request.RequestType;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequest;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.SessionRequest;
import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.ForceLogoutMessage;

public class ErrorHandler implements IMessageHandler {

	protected IRequestService requestService;
	protected ILoginService loginService;
	protected ISessionService sessionService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	
	public ErrorHandler(ILoginService loginService,IRequestService requestService,ISessionService sessionService){
		this.requestService = requestService;
		this.loginService = loginService;
		this.sessionService = sessionService;
	}
	
	public void handle(IMessage message) throws Exception {
		ErrorMessage msg = (ErrorMessage) message;
		IRequest request = requestService.remove(msg.getSerial());
		if (request != null) {
			if (request.getType() == RequestType.LEGACY_FEE) {
				LegacyFeeRequest rq = (LegacyFeeRequest)request;
				Transaction tx = sf.getCurrentSession().beginTransaction();
				LoginDetail ld = null;
				GameAccount ga = null;
				try {
					ld = loginService.logout(rq.getAccountId(),rq.getKey(),rq.getSessionId());
					ga = loginService.getGameAccount(rq.getAccountId());
					tx.commit();
				} catch (Exception e) {
					e.printStackTrace();
				} 
				if(ld!=null){
					ForceLogoutMessage newMsg = new ForceLogoutMessage(ga.getId(),ld.getName(),ld.getKey());
					ISession session = sessionService.getSession(rq.getSessionId());
					if(session!=null)
						session.send(newMsg);				}
			} else {
				ErrorMessage newMsg = new ErrorMessage(request.getId(), msg
						.getCode());
				ISession session = sessionService.getSession(((SessionRequest)request).getSessionId());
				if(session!=null)
					session.send(newMsg);
			}
		}
	}
	

}
