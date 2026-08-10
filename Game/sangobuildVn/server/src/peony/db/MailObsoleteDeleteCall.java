package peony.db;

import java.util.Date;
import java.util.List;
import peony.common.ClientSessionAsyncCall;
import peony.db.DBService;
import peony.game.ErrorHandler;
import peony.game.GameObjectRef;
import peony.game.Mail;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.mail.MailService;
import peony.net.ClientSession;
import peony.net.Packet;

public class MailObsoleteDeleteCall extends ClientSessionAsyncCall {
	private int serial;
	private int day;
	private GameObjectRef ref;

	public MailObsoleteDeleteCall(ClientSession session, int serial,
			GameObjectRef ref,int day) {
		super(session);
		this.serial = serial;
		this.ref = ref;
		this.day = day;
	}

	public void callFinish() throws Exception {
		if (success) {
			Packet pt = new Packet(OpCode.MAIL_OBSOLETE_DELETE_SERVER);
			pt.putInt(serial);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.MAIL_OBSOLETE_DELETE_CLIENT, errorMessage);
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
			if(day!=15 && day!=30){
				error(null, "不支持的天数");
				addToClientSession();
				return;
			}
			DBService dbService = Server.server.getServiceRegistry()
					.getDbService();
			synchronized (mailService) { // 需要跟周期性的检查邮件同步
				//testmodify
//				Date time = new Date(System.currentTimeMillis() - 3 * 60 * 1000L);
				Date time = new Date(System.currentTimeMillis() - day * 24
						* 3600 * 1000l);
				List<Mail> mails = dbService.mailDAO
						.getObsoleteNoAttachmentMails(p.id, time);
				if (mails != null) {
					for (Mail mail : mails) {
						dbService.mailDAO.makeTransient(mail);
					}
				} else {
					error(null, "没有找到邮件");
				}
			}
			addToClientSession();
		}
	}
}
