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

public class QQLoginHandler implements IMessageHandler {
	
	protected QQLoginService loginService;
	protected SuperQQService superQQService;
	protected ISessionService sessionService;
	
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	
	private static final Logger log = Logger.getLogger(QQLoginHandler.class);
	
	public QQLoginHandler(QQLoginService loginService,SuperQQService sqqser, ISessionService sessionService){
		this.loginService = loginService;
		this.superQQService = sqqser;
		this.sessionService = sessionService;
	}
	
	public void handle(IMessage message) throws Exception {
		QQLoginMessage msg = (QQLoginMessage) message;
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try {
			QQLoginResultMessage resultMessage = new QQLoginResultMessage("ok");
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
			QQLoginResultMessage resultMessage = new QQLoginResultMessage("fail");
			msg.getSource().send(resultMessage);			
		}
	}

}
