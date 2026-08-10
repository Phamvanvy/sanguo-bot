package com.pip.gameaccount.handler.server;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.GameAccount;
import com.pip.gameaccount.IGameAccountService;
import com.pip.gameaccount.ILoginService;
import com.pip.gameaccount.ISessionService;
import com.pip.gameaccount.request.LegacyFeeRequest;
import com.pip.gameaccount.request.RequestType;
import com.pip.gameaccount.util.Util;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequest;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.SyncBalanceMessage;
import com.pip.net.message.gameaccount._DecBalanceOkMessage;

public class _DecBalanceOkHandler implements IMessageHandler {

	private static final Logger log = Logger
			.getLogger(_DecBalanceOkHandler.class);

	protected IRequestService requestService;
	protected ILoginService loginService;
	protected ISession accountSkeleton;
	protected IGameAccountService gameAccountService;
	protected ISessionService sessionService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();

	public _DecBalanceOkHandler(ILoginService loginService,
			IRequestService requestService,
			IGameAccountService gameAccountService, ISession accountSkeleton) {
		this.loginService = loginService;
		this.requestService = requestService;
		this.gameAccountService = gameAccountService;
		this.accountSkeleton = accountSkeleton;
	}

	public void handle(IMessage message) throws Exception {
		_DecBalanceOkMessage msg = (_DecBalanceOkMessage) message;
		IRequest request = requestService.remove(message.getSerial());
		if (request != null) {
			int type = request.getType();
			if (type == RequestType.LEGACY_FEE) {
				LegacyFeeRequest rq = (LegacyFeeRequest) request;
				GameAccount ga = null;
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try {
					ga = loginService.getGameAccount(rq.getAccountId());
					if (ga != null) {
						String key = ga.getKey();
						boolean oldIsMonth = Util.isMonth(ga.getMonthFee());
						Date current = new Date();
						if (ga.getLastFeeTime() != null) {
							if (!Util
									.inLaterMonth(ga.getLastFeeTime(), current)) {
								ga.setMonthFee(0);
							}
						}
						ga.setMonthFee(ga.getMonthFee() + msg.getValue());
						ga.setLastFeeTime(new Date());
						boolean isMonth = Util.isMonth(ga.getMonthFee());

						gameAccountService.save(ga);
						tx.commit();
						log.info("FeeOk Balance[" + msg.getBalance()
								+ "]Value[" + msg.getValue() + "]");
						if (msg.getBalance() != rq.getBalance()
								|| (oldIsMonth != isMonth)) {
							SyncBalanceMessage newMsg = new SyncBalanceMessage(
									rq.getAccountId(), msg.getBalance(),
									isMonth, ga.isSubscribe(), msg.getLongBalance());
							ISession session = sessionService.getSession(rq
									.getSessionId());
							if (session != null)
								session.send(newMsg);
						}
					}
				} catch (Exception e) {
					tx.rollback();
					e.printStackTrace();
				}
			}
		}
	}

}
