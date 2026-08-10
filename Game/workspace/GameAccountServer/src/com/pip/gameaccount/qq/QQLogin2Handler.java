package com.pip.gameaccount.qq;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.gameaccount.ISessionService;
import com.pip.gameaccount.LoginDetail;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.ISession;
import com.pip.net.message.gameaccount.ForceLogoutMessage;

public class QQLogin2Handler implements IMessageHandler {
	
	protected QQLoginService loginService;
	protected SuperQQService superQQService;
	protected ISessionService sessionService;
	
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	
	private static final Logger log = Logger.getLogger(QQLogin2Handler.class);
	
	public QQLogin2Handler(QQLoginService loginService,SuperQQService sqqser, ISessionService sessionService){
		this.loginService = loginService;
		this.superQQService = sqqser;
		this.sessionService = sessionService;
	}
	
	public void handle(IMessage message) throws Exception {
		QQLogin2Message msg = (QQLogin2Message) message;
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try {
			QQLogin2ResultMessage resultMessage = new QQLogin2ResultMessage(msg.getVersion(), msg.getSeqNo(), true);
			msg.getSource().send(resultMessage);
			log.info("Uin["+msg.getUin()+"]Key["+msg.getSessionKey()+"]Logined");
			LoginDetail ld = loginService.addLoginKey(msg.getUin(), msg.getSessionKey());
			QQGameAccount ga = null;
			if(ld!=null){
				ga = loginService.getGameAccount(ld.getName());
			}
			tx.commit();
			log.info("Uin["+msg.getUin()+"]Key["+msg.getSessionKey()+"]DbComplete");
			superQQService.addCheckRequest(msg.getUin());
			if (ga != null) {
				ISession session = sessionService.getSession("itimes_qq");
				if (session != null) {
					ForceLogoutMessage newMsg = new ForceLogoutMessage(ga
							.getId(), ld.getName(), ld.getKey());
					session.send(newMsg);
				}
			}
		} catch (Exception ex) {
			tx.rollback();
			ex.printStackTrace();
			QQLogin2ResultMessage resultMessage = new QQLogin2ResultMessage(msg.getVersion(), msg.getSeqNo(), false);
			msg.getSource().send(resultMessage);			
		}
	}

}
