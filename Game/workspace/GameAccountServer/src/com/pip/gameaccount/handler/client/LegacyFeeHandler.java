package com.pip.gameaccount.handler.client;

import java.util.Date;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.GameAccount;
import com.pip.gameaccount.ILoginService;
import com.pip.gameaccount.request.LegacyFeeRequest;
import com.pip.gameaccount.util.Util;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.ISession;
import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.LegacyFeeMessage;
import com.pip.net.message.gameaccount.SyncBalanceMessage;
import com.pip.net.message.gameaccount._DecBalanceMessage;

public class LegacyFeeHandler implements IMessageHandler {
	
	protected ILoginService loginService;
	protected ISession accountSkeleton;
	protected IRequestService requestService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	
	public LegacyFeeHandler(ILoginService loginService,ISession accountSkeleton,IRequestService requestService){
		this.loginService = loginService;
		this.accountSkeleton = accountSkeleton;
		this.requestService = requestService;
	}

	public void handle(IMessage message) throws Exception {
		LegacyFeeMessage msg = (LegacyFeeMessage) message;
		Transaction tx = sf.getCurrentSession().beginTransaction();
		GameAccount ga = null;
		try {
			ga = loginService.getGameAccount(msg.getName());
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} 
		if (ga != null) {
			String key = ga.getKey();
			if (key != null&&msg.getKey().equals(key)) {
				if (ga.isSubscribe()) // 如果包月返回
					return;
				if (Util.isMonth(ga.getMonthFee())) { // 如果达到了包月上限
					if (Util.inLaterMonth(ga.getLastFeeTime(), new Date())) { // 如果没有到下个月,需要同步一次当前的余额
						SyncBalanceMessage newMsg = new SyncBalanceMessage(ga.getId(), msg.getBalance()
								+ msg.getFee(), true, ga.isSubscribe(), msg.getBalance());
						msg.getSource().send(newMsg);
						return;
					}
				}
				int fee = Math.min(Util.MAX_MONTHFEE - ga.getMonthFee(), msg
						.getFee());
				_DecBalanceMessage newMsg = new _DecBalanceMessage(ga
						.getName(), key, fee);
				LegacyFeeRequest request = new LegacyFeeRequest(
						msg.getSerial(), msg.getSource().getId(), ga.getName(), ga
								.getKey(), ga.getId(), fee, msg.getBalance());
				requestService.add(newMsg.getSerial(), request);
				accountSkeleton.send(newMsg);
				return;
			}
		}
		ErrorMessage newMsg = new ErrorMessage(msg.getSerial(), 110);
		message.getSource().send(newMsg);
	}

}
