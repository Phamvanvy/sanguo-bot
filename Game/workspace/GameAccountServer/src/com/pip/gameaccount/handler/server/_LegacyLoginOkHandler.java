package com.pip.gameaccount.handler.server;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.GameAccount;
import com.pip.gameaccount.ILoginService;
import com.pip.gameaccount.ISessionService;
import com.pip.gameaccount.LoginDetail;
import com.pip.gameaccount.request.LegacyLoginRequest;
import com.pip.gameaccount.util.Util;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.ForceLogoutMessage;
import com.pip.net.message.gameaccount.LegacyLoginOkMessage;
import com.pip.net.message.gameaccount._LegacyLoginOkMessage;

public class _LegacyLoginOkHandler implements IMessageHandler {

	protected ILoginService service;
	protected IRequestService requestService;
	protected ISessionService sessionService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	private static final Logger log = Logger.getLogger(_LegacyLoginOkHandler.class);
//	protected ISession accountSkeleton;
	
	public _LegacyLoginOkHandler(ILoginService service,IRequestService requestService,ISessionService sessionService){
		this.service = service;
		this.requestService = requestService;
		this.sessionService = sessionService;
//		this.accountSkeleton = accountSkeleton;
	}	
	
	public void handle(IMessage message) throws Exception {
		_LegacyLoginOkMessage msg = (_LegacyLoginOkMessage) message;
		LegacyLoginRequest rq = (LegacyLoginRequest) requestService.remove(msg
				.getSerial());
		if (rq != null) {
			Transaction tx = sf.getCurrentSession().beginTransaction();
			try {
				LoginDetail ld = service.addLoginKey(msg.getAccountId(),msg.getName(),msg.getKey(),rq.getSessionId());
				GameAccount ga = service.login(msg.getName(),msg.getKey(),rq.getSessionId());
				tx.commit();
				if(ld!=null){
					ForceLogoutMessage fm = new ForceLogoutMessage(ga.getId(),ga.getName(),ld.getKey());
					ISession session = sessionService.getSession(ld.getServerId());
					if(session!=null)
						session.send(fm);
				}
				LegacyLoginOkMessage newMsg = new LegacyLoginOkMessage(rq.getId(),
						ga.getId(), msg.getName(), msg.getKey(), msg
								.getPhone(), msg.getModifiedNameTimes(), msg
								.getIMoney(), Util.isMonth(ga.getMonthFee()), ga.isSubscribe(), msg
								.getLoginErrorTimes(),msg.getPuchasedCodes(),msg.getLongBalance());
				ISession session = sessionService.getSession(rq.getSessionId());
				if(session!=null)
					session.send(newMsg);
			} catch (Exception e) {
				tx.rollback();
				log.error(e,e);
			}
		}
	}

}
