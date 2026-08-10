package com.pip.gameaccount.qq;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.LegacyLoginMessage;
import com.pip.net.message.gameaccount.LegacyLoginOkMessage;

public class LegacyLoginHandler implements IMessageHandler {

	protected QQLoginService loginService;
	protected SuperQQService superQQService;
	protected SessionFactory sf = HibernateUtil.getSessionFactory();
	protected static int[] EMPTY = new int[0];
	protected static int[] SUPERQQ = {1};
	private static final Logger log = Logger.getLogger(LegacyLoginHandler.class);
	
	public LegacyLoginHandler(QQLoginService loginService,SuperQQService superQQService){
		this.loginService = loginService;
		this.superQQService = superQQService;
	}

	public void handle(IMessage message) throws Exception {
		LegacyLoginMessage msg = (LegacyLoginMessage) message;
		Transaction tx = sf.getCurrentSession().beginTransaction();
		try {
		    log.info("Uin[" + msg.getName() + "]Key[" + msg.getPassword() + "]Phone[" + msg.getPhone() + "]Try Login");
			QQGameAccount account = loginService.login(msg.getName(), msg
					.getPassword(), msg.getSource().getId(), msg.getPhone());
			boolean isSuperQQ = superQQService.isSuperQQ(msg.getName());
			int[] purchased = EMPTY;
			if(isSuperQQ){
				purchased = SUPERQQ;
			}
			log.info("Uin[" + msg.getName() + "]SuperQQ[" + (isSuperQQ ? "true" : "false") + "]");
			if (account == null) {
				ErrorMessage newMsg = new ErrorMessage(msg.getSerial(), 4);
				msg.getSource().send(newMsg);
				log.info("Uin[" + msg.getName() + "]Key[" + msg.getPassword()
						+ "]Login FAIL");
			} else {
				LegacyLoginOkMessage newMsg = new LegacyLoginOkMessage(msg
						.getSerial(), account.getId(), account.getName(),
						account.getKey(), "", 0, account.getBalance(), false,
						false, 0, purchased, account.getBalance());
				msg.getSource().send(newMsg);
				log.info("Uin[" + msg.getName() + "]Key[" + msg.getPassword()
						+ "] Super["+isSuperQQ+"]Login OK");
			}
			tx.commit();

		} catch (Exception e) {
			ErrorMessage newMsg = new ErrorMessage(msg.getSerial(), 4);
			msg.getSource().send(newMsg);
			log.error(e, e);
			tx.rollback();
		}
	}
}
