package peony.db;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameObjectRef;
import peony.game.LogUtil;
import peony.game.Mail;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.mail.MailService;
import peony.net.ClientSession;
import peony.net.Packet;

public class MailDeleteCall extends ClientSessionAsyncCall {
    private static Logger log = Logger.getLogger(MailDeleteCall.class);
	private int serial;
	private int mailId;
	private GameObjectRef ref;

	public MailDeleteCall(ClientSession session, int serial, GameObjectRef ref,
			int mailId) {
		super(session);
		this.serial = serial;
		this.mailId = mailId;
		this.ref = ref;
	}

	public void callFinish() throws Exception {
		if (success) {
			Packet pt = new Packet(OpCode.MAIL_DELETE_SERVER);
			pt.putInt(serial);
			pt.putInt(mailId);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.MAIL_DELETE_CLIENT, errorMessage);
		}
	}

	public void run() {
		Player p = (Player) ObjectAccessor.getGameObject(ref);
		if (p != null) {
            MailService mailService = Server.server.getServiceRegistry()
					.getMailService();
			if (mailService.isForbid(p.id)) {
				error(null, "已經被封,請聯系遊戲管理員");
				addToClientSession();
				return;
			}
			DBService dbService = Server.server.getServiceRegistry()
					.getDbService();
			synchronized (mailService) {  //需要跟周期性的检查邮件同步
				if (mailId != -1) {
					Mail m = dbService.mailDAO.getMailById(mailId);
					if (m != null && m.getDestId() == p.id) {
						deleteMail(p, dbService.mailDAO, m);
					} else {
						error(null, "沒有找到指定郵件");
					}
				} else { // 如果mailId为-1，那么代表删除一个小时之前的所有未收藏的邮件
					Date time = new Date(
							System.currentTimeMillis() - 3600 * 1000L);
					List<Mail> l = dbService.mailDAO
							.getUnFavoriteNoAttachmentMails(p.id, time);
					dbService.mailDAO.deleteUnFavoriteMails(p.id, time);
					for (Mail m : l) {
						deleteMail(p, dbService.mailDAO, m);
					}
				}
			}
			addToClientSession();
		}
	}

	protected void deleteMail(Player p, MailDAO dao, Mail m) {
			dao.makeTransient(m);
		if (m.getSourceId() != -1 && m.getAttachment() != null
				&& m.getPrice() > 0) {
			Mail newMail = new Mail();
			newMail.setDestId(m.getSourceId());
			newMail.setSourceId(-1);
			newMail.setSourceName("系統");
			newMail.setTitle(MessageFormat.format("被刪除:{0}", m.getTitle()));
			newMail.setContent("");
			newMail.setPrice(0);
			newMail.setStatus(Mail.UNREADED);
			newMail.setPostTime(new Date());
			newMail.setValidTime(new Date());
			newMail.setExpirationTime(new Date(System.currentTimeMillis()+MailService.EXPIRATION_TIME));
			newMail.setAttachment(m.getAttachment());
			dao.newEntity(newMail);
			LogUtil.logDelAttach(p, m, newMail.getId());
		} else if (m.getAttachment() != null) {
			LogUtil.logDelAttach(p, m, -1);
		}
	}

}
