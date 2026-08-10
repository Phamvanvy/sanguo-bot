package peony.db;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.Gain;
import peony.game.GameObjectRef;
import peony.game.LogUtil;
import peony.game.Mail;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.mail.ItemMailAttachment;
import peony.game.mail.MailAttachment;
import peony.game.mail.MailService;
import peony.game.mail.MoneyMailAttachment;
import peony.game.nation.CandidateService;
import peony.net.ClientSession;
import peony.net.Packet;

public class MailAttachmentCall extends ClientSessionAsyncCall {
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger
			.getLogger(MailAttachmentCall.class);

	private int serial;
	private GameObjectRef ref;
	private int mailId;
	private boolean deleted;

	public MailAttachmentCall(ClientSession session, int serial,
			GameObjectRef ref, int mailId) {
		super(session);
		this.serial = serial;
		this.ref = ref;
		this.mailId = mailId;
		LogUtil.logGetAttachTry((Player) session.getClient(), mailId);
	}

	public void callFinish() throws Exception {
		if (success) {
			Packet pt = new Packet(OpCode.MAIL_ATTACHMENT_SERVER);
			pt.putInt(serial);
			pt.putInt(mailId);
			pt.put(deleted ? 1 : 0);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.MAIL_ATTACHMENT_CLIENT, errorMessage);
		}
	}

	public void run() {
		Player p = (Player) ObjectAccessor.getGameObject(ref);
		if (p != null) {
			MailService mailService = Server.server.getServiceRegistry()
					.getMailService();
			if (mailService.isForbid(p.id)) {
				error(null, "已经被封，请联系游戏管理员");
				addToClientSession();
				return;
			}
			synchronized (mailService) {
				DBService dbService = Server.server.getServiceRegistry()
						.getDbService();
				Mail m = dbService.mailDAO.getMailById(mailId);
				if (m != null && m.getDestId() == p.id) {
					MailAttachment attachment = m.getAttachment();
					if (attachment != null) {
						if (attachment instanceof ItemMailAttachment) {
							ItemMailAttachment att = (ItemMailAttachment) attachment;
							if (m.getPrice() > 0 && p.getMoney() < m.getPrice()) {
								error(null, "没有足够的金钱");
							} else {
								Gain gain = new Gain(p);
								if((att.getGameItem().template.id==CandidateService.KING_WEAL_BAG || att.getGameItem().template.id==CandidateService.KING_TOKEN) 
										&& p.isKing()!=1){
									error(null, "此邮件已过期不能提取");
									addToClientSession();
									return;
								}
								gain.addGainItem(att.getGameItem(), att.getCount());
								PlayerTransaction tx = p.newTransaction("ATT");
								try {
									p.bag.addGainComplete(gain, tx, true);
									p.decMoney(m.getPrice(), tx, true);
									if (m.getSourceId()==-1 && (m.getContent().length() == 0 || m.getSourceName().equals("拍卖行"))) {
										dbService.mailDAO.makeTransient(m);
										deleted = true;
									} else {
										m.setAttachment(null);
										dbService.mailDAO.updateEntity(m);
									}
									tx.commit();
									if (m.getPrice() > 0) {
										Mail returnMail = new Mail();
										returnMail.setDestId(m.getSourceId());
										returnMail.setPrice(0);
										returnMail.setSourceId(-1);
										returnMail.setSourceName("系统");
										returnMail.setStatus(Mail.UNREADED);
										returnMail.setTitle(MessageFormat.format("回复:{0}", m.getTitle()));
										returnMail.setPostTime(new Date());
										returnMail.setValidTime(new Date());
										returnMail.setExpirationTime(new Date(System.currentTimeMillis()
							+ MailService.EXPIRATION_TIME));
										returnMail.setContent("");
										returnMail
												.setAttachment(new MoneyMailAttachment(
														m.getPrice()));
										dbService.mailDAO.newEntity(returnMail);

										LogUtil.logGetAttachOK(p, mailId, m
												.getSourceId(), 0, att
												.getGameItem(), att.getCount(),
												m.getPrice(), returnMail
														.getId());

										Player dest = ObjectAccessor
												.getPlayer(m.getSourceId());
										if (dest != null
												&& dest.systemState == Player.SYSTEMSTATE_READY) {
											Packet pt = new Packet(
													OpCode.MAIL_NEW_SERVER);
											dest.send(pt);
										}
									} else {
										LogUtil.logGetAttachOK(p, mailId, m
												.getSourceId(), 0, att
												.getGameItem(), att.getCount(),
												0, -1);
									}
								} catch (Exception e1) {
									log.debug(e1, e1);
									tx.rollback();
									error(null, "没有足够的包格");
								}
							}
						} else if (attachment instanceof MoneyMailAttachment) {
							MoneyMailAttachment att = (MoneyMailAttachment) attachment;
							PlayerTransaction tx = p.newTransaction("ATT");
							try {
								p.addMoney(att.getCount(), tx, true);
								if (m.getSourceId()==-1 && (m.getContent().length() == 0 || m.getSourceName().equals("拍卖行"))) {
									dbService.mailDAO.makeTransient(m);
									deleted = true;
								} else {
									m.setAttachment(null);
									dbService.mailDAO.updateEntity(m);
								}
								tx.commit();
								LogUtil.logGetAttachOK(p, mailId, m
										.getSourceId(), att.getCount(), null,
										0, 0, -1);
							} catch (Exception e) {
								log.debug(e, e);
								error(null, "取金钱错误");
								tx.rollback();
							}
						}
					} else {
						error(null, "没有附件");
					}
				} else {
					error(null, "该邮件已被发件人索回");
				}
			}
			addToClientSession();
		}
	}
}
