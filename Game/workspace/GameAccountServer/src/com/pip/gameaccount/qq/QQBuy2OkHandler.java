package com.pip.gameaccount.qq;

import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.ISessionService;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.SyncBalanceMessage;

public class QQBuy2OkHandler implements IMessageHandler {

	protected QQLoginService loginService;
	protected QQBillingService billingService;
	protected ISessionService sessionService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	static Logger log = Logger.getLogger(QQBuy2OkHandler.class);
	
	public QQBuy2OkHandler(QQLoginService loginService,QQBillingService billingService,ISessionService sessionService){
		this.loginService = loginService;
		this.billingService = billingService;
		this.sessionService = sessionService;
	}
	
	public void handle(IMessage message) throws Exception {
		QQBuy2Message msg = (QQBuy2Message)message;
		log.info("QQBuyOk Uin["+msg.getUin()+"]LinkId["+msg.getLinkId()+"]GoodId["+msg.getObjectId()+"]Count["+msg.getCount()+"]");
		QQBilling billing = billingService.removeBilling(msg.getLinkId());
		if(billing==null){
			QQBuy2ResultMessage newMsg = new QQBuy2ResultMessage(msg.getCmd(), msg.getVersion(), msg.getSeqNo(),
					msg.getUin(), msg.getLinkId(), msg.getBId(), msg.getObjectId(), msg.getCount(), (byte)1);  // linkid不匹配
			msg.getSource().send(newMsg);
			log.info("ChargeNotFound Uin[" + msg.getUin() + "]LinkId["
					+ msg.getLinkId() + "]Count[" + msg.getCount() + "]BuyType["+msg.getCmd()+"]");
			return;
		}
		QQBuy2ResultMessage newMsg = new QQBuy2ResultMessage(msg.getCmd(), msg.getVersion(), msg.getSeqNo(),
				msg.getUin(), msg.getLinkId(), msg.getBId(), msg.getObjectId(), msg.getCount(), (byte)0);
		msg.getSource().send(newMsg);
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try{
			int count = msg.getCount();
			if (msg.getCmd()==QQMessageType.QQ_BUY_CHINARUN2){ //如果是神州行充值那么送钱
				if (QQBuyOkHandler.chinarunAmountMap.containsKey(count)) {
					count = QQBuyOkHandler.chinarunAmountMap.get(count);
				}
			}
			QQGameAccount account = loginService.addBalance(msg.getUin(), count*100);
			Fee fee = new Fee();
			fee.setAccountId(account.getId());
			fee.setCharged(true);
			Date now = new Date();
			fee.setCreateTime(now);
			fee.setFinishTime(now);
			fee.setAmount(msg.getCount());
			fee.setCharged(true);
			if (msg.getCmd() == QQMessageType.QQ_BUY2) {
				fee.setChannel(QQBuyOkHandler.CHANNEL_Q);
			}else{
				fee.setChannel(QQBuyOkHandler.CHANNEL_CHINARUN);
			}
			billingService.addNewFee(fee);
			tx.commit();

			ISession session = sessionService.getSession(billing.getSourceId());
			if(session!=null){
				SyncBalanceMessage newMsg1 = new SyncBalanceMessage(account.getId(),account.getBalance(),false,false,account.getBalance());
				session.send(newMsg1);
			}
			log.info("ChargeOk Uin[" + msg.getUin() + "]LinkId["
					+ msg.getLinkId() + "]Count[" + count + "]");
		}
		catch(Exception e){
			log.error(e,e);
			tx.rollback();
//			QQBuyResultMessage newMsg = new QQBuyResultMessage(msg.getCmd(),msg.getLinkId(),
//					msg.getBId(), msg.getUin(), msg.getObjectId(), (short) msg
//							.getCount(), (short) -1);//系统错误
//			msg.getSource().send(newMsg);
			log.info("ChargeError Uin[" + msg.getUin() + "]LinkId["
					+ msg.getLinkId() + "]Count[" + msg.getCount() + "]");			
		}
	}

}
