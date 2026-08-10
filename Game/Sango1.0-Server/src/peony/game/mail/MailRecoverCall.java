package peony.game.mail;

import java.text.MessageFormat;
import java.util.Date;
import peony.common.ClientSessionAsyncCall;
import peony.db.MailDAO;
import peony.game.ErrorHandler;
import peony.game.Mail;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class MailRecoverCall  extends ClientSessionAsyncCall{

	int serial;
	int mailId;
	Player player;
	
	public MailRecoverCall(ClientSession session,Packet packet) {
		super(session);
		serial = packet.getInt();
		mailId = packet.getInt();
		player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if (success) {
			Packet pt = new Packet(OpCode.MAIL_RECOVER_SERVER);
			pt.putInt(serial);
			pt.putInt(mailId);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.MAIL_RECOVER_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(player != null){
			MailDAO mailDAO = Server.server.getServiceRegistry().getDbService().mailDAO;
			 MailService mailService = Server.server.getServiceRegistry()
				.getMailService();
			 synchronized(mailService){
				 Mail mail = mailDAO.getMailById(mailId);
				 if (mail!=null && mail.getSourceId() == player.id) {
						RecoverMail(player, mailDAO, mail);
					} else {
						error(null, peony.Messages.STRING_01059);
					}
			 }
		}
		addToClientSession();
	}
	
	protected void RecoverMail(Player p, MailDAO dao, Mail m) {
		dao.makeTransient(m);
		if (m.getAttachment() != null
				&& m.getPrice() > 0) {
			Mail newMail = new Mail();
			newMail.setDestId(m.getSourceId());
			newMail.setSourceId(-1);
			newMail.setSourceName(peony.Messages.STRING_00004);
			newMail.setTitle(MessageFormat.format(peony.Messages.STRING_01060, m.getTitle()));
			newMail.setContent("");
			newMail.setPrice(0);
			newMail.setStatus(Mail.UNREADED);
			newMail.setPostTime(new Date());
			newMail.setValidTime(new Date());
			newMail.setExpirationTime(new Date(System.currentTimeMillis()+MailService.EXPIRATION_TIME));
			newMail.setAttachment(m.getAttachment());
			dao.newEntity(newMail);
		} 
	}
}
