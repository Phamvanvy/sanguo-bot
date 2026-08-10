package com.pip.gameaccount.handler.server;

import java.util.Date;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.GameAccount;
import com.pip.gameaccount.IGameAccountService;
import com.pip.gameaccount.ISessionService;
import com.pip.gameaccount.request.LegacyQuickRegRequest;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.LegacyQuickRegResultMessage;
import com.pip.net.message.gameaccount._LegacyQuickRegResultMessage;

public class _LegacyQuickRegResultHandler implements IMessageHandler {

	protected IGameAccountService service;
	protected IRequestService requestService;
	protected ISessionService sessionService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();

	public _LegacyQuickRegResultHandler(IGameAccountService service,
			IRequestService requestService, ISessionService sessionService) {
		this.service = service;
		this.requestService = requestService;
		this.sessionService = sessionService;
	}

	public void handle(IMessage message) throws Exception {
		LegacyQuickRegRequest rq = (LegacyQuickRegRequest) requestService
				.remove(message.getSerial());
		if (rq != null) {
			_LegacyQuickRegResultMessage msg = (_LegacyQuickRegResultMessage) message;
			String name = msg.getName();
			Transaction tx = sf.getCurrentSession().beginTransaction();
			try {
				if (msg.getResult() == 0) { // 认证服务器新建了帐号
					GameAccount ga = service.createGameAccount(msg
							.getAccountId(), name, new Date());
					tx.commit();
					LegacyQuickRegResultMessage newMsg = new LegacyQuickRegResultMessage(
							rq.getId(), ga.getId(), name, msg.getPassword(),
							msg.getResult());
					ISession session = sessionService.getSession(rq
							.getSessionId());
					if (session != null)
						session.send(newMsg);
				} else if (msg.getResult() == 1) { // 认证服务器用了原始帐号
					GameAccount ga = service.getGameAccount(name);
					if (ga == null)
						ga = service.createGameAccount(msg.getAccountId(),
								name, new Date());
					tx.commit();
					LegacyQuickRegResultMessage newMsg = new LegacyQuickRegResultMessage(
							rq.getId(), ga.getId(), ga.getName(), msg
									.getPassword(), msg.getResult());
					ISession session = sessionService.getSession(rq
							.getSessionId());
					if (session != null)
						session.send(newMsg);
				}
			}catch(Exception e){
				tx.rollback();
			}
		}

	}

}
