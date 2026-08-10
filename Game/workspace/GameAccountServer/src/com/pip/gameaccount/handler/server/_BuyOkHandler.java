package com.pip.gameaccount.handler.server;

import java.util.Date;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.GameAccount;
import com.pip.gameaccount.IGameAccountService;
import com.pip.gameaccount.ILoginService;
import com.pip.gameaccount.ISessionService;
import com.pip.gameaccount.request.LegacyBuyRequest;
import com.pip.gameaccount.request.RequestType;
import com.pip.gameaccount.util.Util;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequest;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.LegacyBuyResultMessage;
import com.pip.net.message.gameaccount._BuyOkMessage;

public class _BuyOkHandler implements IMessageHandler {

	public static final int MAX_MONTHFEE = 100000;
	
	protected IRequestService requestService;
	protected ILoginService loginService;
	protected IGameAccountService gameAccountService;
	protected ISessionService sessionService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	
	public _BuyOkHandler(ILoginService loginService,
			IRequestService requestService,IGameAccountService gameAccountService,ISessionService sessionService) {
		this.loginService = loginService;
		this.requestService = requestService;
		this.gameAccountService = gameAccountService;
		this.sessionService = sessionService;
	}

	public void handle(IMessage message) throws Exception {
		_BuyOkMessage msg = (_BuyOkMessage) message;
		IRequest request = requestService.remove(message.getSerial());
		if (request != null) {
			int type = request.getType();
			if (type == RequestType.LEGACY_BUY_RESULT) {
				LegacyBuyRequest rq = (LegacyBuyRequest) request;
				LegacyBuyResultMessage sendMessage = new LegacyBuyResultMessage(
						rq.getId(), true, msg.getBalance(), msg.getCost(), "", msg.getLongBalance());
				ISession session = sessionService.getSession(rq.getSessionId());
				if(session!=null)
					session.send(sendMessage);
				GameAccount ga = loginService.getGameAccount(rq.getName());
				if(ga!=null){
					Transaction tx = sf.getCurrentSession().beginTransaction();
					updateMonthPay(ga,msg.getCost());
					tx.commit();
				}
			}
		}
	}
	
	protected void updateMonthPay(GameAccount ga,int value){
		Date current = new Date();
		if(ga.getLastPayTime()!=null){
			if(!Util.inLaterMonth(ga.getLastPayTime(), current)){
				ga.setLastmonthpay(ga.getMonthPay());
				ga.setMonthPay(0);
			}
			ga.setMonthPay(ga.getMonthPay()+value);
			ga.setLastPayTime(current);
		}else{
			ga.setLastPayTime(current);
			ga.setMonthPay(value);
		}
		gameAccountService.save(ga);
	}
}
