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

public class QQBuyOkHandler implements IMessageHandler {

	protected QQLoginService loginService;
	protected QQBillingService billingService;
	protected ISessionService sessionService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	static Logger log = Logger.getLogger(QQBuyOkHandler.class);
	
	public static String CHANNEL_Q = "QQ_Q";
	public static String CHANNEL_CHINARUN = "QQ_CHINARUN";
	public static HashMap<Integer, Integer> chinarunAmountMap = new HashMap<Integer, Integer>();
//			30卡 3000元宝+30元宝
//			50卡 5000元宝+100元宝
//			100卡 10000元宝+400元宝
//			300卡 30000元宝+600元宝
//			500卡 50000元宝+1000元宝
	static {
		chinarunAmountMap.put(3000, 3000);
		chinarunAmountMap.put(5000, 5000);
		chinarunAmountMap.put(10000, 10000);
		chinarunAmountMap.put(30000, 30000);
		chinarunAmountMap.put(50000, 50000);
//		chinarunAmountMap.put(3000, 3030);
//		chinarunAmountMap.put(5000, 5100);
//		chinarunAmountMap.put(10000, 10400);
//		chinarunAmountMap.put(30000, 32400);
//		chinarunAmountMap.put(50000, 55000);
	}
	
	public QQBuyOkHandler(QQLoginService loginService,QQBillingService billingService,ISessionService sessionService){
		this.loginService = loginService;
		this.billingService = billingService;
		this.sessionService = sessionService;
	}
	
	public void handle(IMessage message) throws Exception {
		QQBuyMessage msg = (QQBuyMessage)message;
		log.info("QQBuyOk Uin["+msg.getUin()+"]LinkId["+msg.getLinkId()+"]GoodId["+msg.getObjectId()+"]Count["+msg.getCount()+"]");
		QQBilling billing = billingService.removeBilling(msg.getLinkId());
		if(billing==null){
			QQBuyResultMessage newMsg = new QQBuyResultMessage(msg.getCmd(),msg.getLinkId(),
					msg.getBId(), msg.getUin(), msg.getObjectId(), (short) msg
							.getCount(), (short) 2);//linkid不匹配
			msg.getSource().send(newMsg);
			log.info("ChargeNotFound Uin[" + msg.getUin() + "]LinkId["
					+ msg.getLinkId() + "]Count[" + msg.getCount() + "]BuyType["+msg.getCmd()+"]");
			return;
		}
		QQBuyResultMessage newMsg = new QQBuyResultMessage(msg.getCmd(),msg.getLinkId(),
				msg.getBId(), msg.getUin(), msg.getObjectId(), (short) msg
						.getCount(), (short) 0);
		msg.getSource().send(newMsg);
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try{
			int count = msg.getCount();
			if (msg.getCmd()==QQMessageType.QQ_BUY_CHINARUN_RESULT){ //如果是神州行充值那么送钱
				if (chinarunAmountMap.containsKey(count)) {
					count = chinarunAmountMap.get(count);
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
			if (msg.getCmd() == QQMessageType.QQ_BUY_RESULT) {
				fee.setChannel(CHANNEL_Q);
			}else{
				fee.setChannel(CHANNEL_CHINARUN);
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
