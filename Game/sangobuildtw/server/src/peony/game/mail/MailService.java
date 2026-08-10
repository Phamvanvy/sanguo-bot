package peony.game.mail;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import peony.db.DBService;
import peony.db.MailDAO;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.Mail;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.chat.ChatMessage;
import peony.net.Packet;
import peony.service.Service;
import ch.javasoft.util.intcoll.IntHashMap;

public class MailService implements Service, Runnable {
	private static final Logger log = Logger.getLogger(MailService.class);

	private static final long OBSOLETE_TIME = 86400L * 1000L;
	// private static final long OBSOLETE_TIME = 60 * 10 * 1000L;
	public static final long EXPIRATION_TIME = 30 * 24 * 3600 * 1000L;
	//public static final long EXPIRATION_TIME = 2 * 60 * 1000L;
	private static final int HANDLE_ONETIME = 1000;
	private boolean sweep;

	protected boolean active;

	protected IntHashMap<MailForbid> forbids = new IntHashMap<MailForbid>();

	protected BlockingQueue<Mail> pendingMails = new LinkedBlockingQueue<Mail>();

	public void addForbid(int playerId, long time) {
		MailForbid forbid = new MailForbid(playerId, time);
		forbids.put(playerId, forbid);
	}

	public boolean isForbid(int playerId) {
		MailForbid forbid = forbids.get(playerId);
		if (forbid == null)
			return false;
		return forbid.time > System.currentTimeMillis();
	}

	public void removeForbid(int playerId) {
		forbids.remove(playerId);
	}

	public void startup() throws Exception {

		if (!Server.containsOption("hack")) {
			new Thread(this, "MailService").start();
		}
		if (!Server.containsOption("hack")) {
			new Thread(new DaemonMail(), "Daemon-Mail").start();
		}
		if (!Server.containsOption("hack")) {
			new Thread(new ProcessExpMail(), "Expiration-Mail").start();
		}
	}

	public void shutdown() {
		synchronized (this) {
			while (sweep) {
				try {
					Thread.sleep(10 * 1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			while (pendingMails.size() > 0) {
				try {
					Thread.sleep(10 * 1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			while (active)
				try {
					Thread.sleep(10 * 1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
	}

	public void run() {
		while (true) {
			sweep(new Date(System.currentTimeMillis() - OBSOLETE_TIME));
			try {
				Thread.sleep(3600*1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void sweep(Date date){
		synchronized(this){
			sweep = true;
			try{
				DBService dbService = Server.server.getServiceRegistry().getDbService();
				List<Mail> mails = dbService.mailDAO.getObsoletMails(date);
				for(Mail mail:mails){
					try {
						deleteOrBack(dbService.mailDAO,mail);
					} catch (Exception e) {
						log.error(e,e);
					}
				}
			}catch(Exception ex){
				log.error(ex,ex);
			}
			sweep = false;
		}
	}

	protected void deleteOrBack(MailDAO dao, Mail mail) {
		if (mail.getSourceId() > 0 && mail.getPrice() > 0
				&& mail.getAttachment() != null) {
			Mail rMail = new Mail();
			rMail.setSourceId(-1);
			rMail.setSourceName("系統");
			rMail.setTitle(MessageFormat.format("退回：{0}", mail.getTitle()));
			rMail.setDestId(mail.getSourceId());
			rMail.setPrice(0);
			rMail.setContent("");
			rMail.setPostTime(new Date());
			rMail.setValidTime(new Date());
			rMail.setExpirationTime(new Date(System.currentTimeMillis()
					+ EXPIRATION_TIME));
			rMail.setStatus(Mail.UNREADED);
			rMail.setAttachment(mail.getAttachment());
			dao.makeTransient(mail);
			dao.newEntity(rMail);
			LogUtil.logAutoDelAttach(mail, rMail.getId());
		}
	}

	/**
	 * 发送一封系统邮件给用户
	 * 
	 * @param target
	 *            目标用户
	 * @param title
	 *            标题
	 * @param content
	 *            内容
	 * @param attMoney
	 *            附件金钱，0表示没有
	 * @param attItem
	 *            附件物品，null表示没有
	 * @param attCount
	 *            附件物品数量
	 */
	public int sendSystemMail(int target, String sourceName, String title,
			String content, int attMoney, GameItem attItem, int attCount, String cause) {
		try {
			Mail mail = new Mail();
			mail.setDestId(target);
			mail.setSourceId(-1);
			mail.setSourceName(sourceName);
			mail.setTitle(title);
			mail.setContent(content);
			mail.setPrice(0);
			mail.setStatus(Mail.UNREADED);
			mail.setPostTime(new Date());
			mail.setValidTime(new Date());
			mail.setExpirationTime(new Date(System.currentTimeMillis()
					+ EXPIRATION_TIME));
			if (attMoney > 0) {
				mail.setAttachment(new MoneyMailAttachment(attMoney));
			} else if (attItem != null) {
				mail.setAttachment(new ItemMailAttachment(attItem, attCount));
			}
			sendMail(mail);
			if (mail.getAttachment() != null) {
				LogUtil.logSystemMail(target, attMoney, attItem, attCount, cause, mail.getId());
			}
	//		DBService dbService = Server.server.getServiceRegistry().getDbService();
	//		dbService.mailDAO.newEntity(mail);
	//		log.info("[NEWMAIL]ID"+"["+mail.getId()+"]TARGET["+target+"]");
	//		Player p = ObjectAccessor.getPlayer(target);
	//		if(p!=null&&p.systemState==Player.SYSTEMSTATE_READY){
	//			Packet pt = new Packet(OpCode.MAIL_NEW_SERVER);
	//			p.send(pt);
	//		}
			return mail.getId();
		} catch (Exception e) {
			log.error(e, e);
			return -1;
		}
	}

	/**
	 * 异步的形式发送一封系统邮件给用户,用在主线程程序中
	 * @param target 目标用户
	 * @param title 标题
	 * @param content 内容
	 * @param attMoney 附件金钱，0表示没有
	 * @param attItem 附件物品，null表示没有
	 * @param attCount 附件物品数量
	 */
	public boolean sendSystemMailAsync(int target, String sourceName, String title,
			String content, int attMoney, GameItem attItem, int attCount, String cause) {
		Mail mail = new Mail();
		mail.setDestId(target);
		mail.setSourceId(-1);
		mail.setSourceName(sourceName);
		mail.setTitle(title);
		mail.setContent(content);
		mail.setPrice(0);
		mail.setStatus(Mail.UNREADED);
		mail.setPostTime(new Date());
		mail.setValidTime(new Date());
		mail.setExpirationTime(new Date(System.currentTimeMillis()
				+ EXPIRATION_TIME));
		if (attMoney > 0) {
			mail.setAttachment(new MoneyMailAttachment(attMoney));
		} else if (attItem != null) {
			mail.setAttachment(new ItemMailAttachment(attItem, attCount));
		}
		mail.cause = cause;
		pendingMails.add(mail);
//		sendMail(mail);
//		DBService dbService = Server.server.getServiceRegistry().getDbService();
//		dbService.mailDAO.newEntity(mail);
//		log.info("[NEWMAIL]ID"+"["+mail.getId()+"]TARGET["+target+"]");
//		Player p = ObjectAccessor.getPlayer(target);
//		if(p!=null&&p.systemState==Player.SYSTEMSTATE_READY){
//			Packet pt = new Packet(OpCode.MAIL_NEW_SERVER);
//			p.send(pt);
//		}
		return true;
	}

	public void sendMail(Mail mail) {
		DBService dbService = Server.server.getServiceRegistry().getDbService();
		dbService.mailDAO.newEntity(mail);
		Player p = ObjectAccessor.getPlayer(mail.getDestId());
		if (p != null && p.systemState == Player.SYSTEMSTATE_READY) {
			Packet pt = new Packet(OpCode.MAIL_NEW_SERVER);
			p.send(pt);
		}
	}

	class DaemonMail implements Runnable {
		public void run() {
			while (true) {
				try {
					Mail mail = pendingMails.take();
					sendMail(mail);
					if (mail.getAttachment() != null) {
						if (mail.getAttachment() instanceof MoneyMailAttachment) {
							MoneyMailAttachment att = (MoneyMailAttachment)mail.getAttachment();
							LogUtil.logSystemMail(mail.getDestId(), att.getCount(), null, 0, mail.cause, mail.getId());
						} else if (mail.getAttachment() instanceof ItemMailAttachment) {
							ItemMailAttachment att = (ItemMailAttachment)mail.getAttachment();
							LogUtil.logSystemMail(mail.getDestId(), 0, att.getGameItem(), att.getCount(), mail.cause, mail.getId());
						}
					}
				} catch (Exception e) {
					log.error(e, e);
				}
			}
		}
	}

	class ProcessExpMail implements Runnable {
		public void processExpiMails() {
			synchronized (this) {
				active = true;
				try {
					List<Mail> expiMails = Server.server.getServiceRegistry().getDbService().mailDAO
							.getExpirationAttachmentMails(0, HANDLE_ONETIME);
					if (expiMails != null) {
						for (Mail mail : expiMails) {
							deAndBackMail(mail);
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				active = false;
			}

		}

		protected void deAndBackMail(Mail mail) {
			Server.server.getServiceRegistry().getDbService().mailDAO.makeTransient(mail);
			if (mail.getSourceId() > 0 && mail.getAttachment() != null) {
				Mail rMail = new Mail();
				rMail.setSourceId(-1);
				rMail.setSourceName("系統");
				rMail.setTitle("退回：" + mail.getTitle());
				rMail.setDestId(mail.getSourceId());
				rMail.setPrice(0);
				rMail.setContent("");
				rMail.setPostTime(new Date());
				rMail.setValidTime(new Date());
				rMail.setExpirationTime(new Date(System.currentTimeMillis()
						+ EXPIRATION_TIME));
				rMail.setStatus(Mail.UNREADED);
				rMail.setAttachment(mail.getAttachment());
				Server.server.getServiceRegistry().getDbService().mailDAO.newEntity(rMail);
				LogUtil.logAutoDelExpiAttach(mail, rMail.getId());
			} else if(mail.getAttachment() != null) {
					LogUtil.logAutoDelExpiAttach(mail, -1);
				}
			}

		

		public void run() {
			while (true) {
				processExpiMails();
				try {
					Thread.sleep(5 * 60 * 1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

}

class MailForbid {
	public int playerId;
	public long time;

	public MailForbid(int playerId, long time) {
		this.playerId = playerId;
		this.time = time;
	}

}
