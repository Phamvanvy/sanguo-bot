package peony.db;

import peony.common.ClientSessionAsyncCall;
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

public class MailFavoriteCall extends ClientSessionAsyncCall {

	protected int serial;
	protected GameObjectRef ref;
	protected int mailId;
	
	public MailFavoriteCall(ClientSession session,int serial,GameObjectRef ref,int mailId){
		super(session);
		this.serial = serial;
		this.ref = ref;
		this.mailId = mailId;
	}
	
	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.MAIL_FAVORITE_SERVER);
			pt.putInt(serial);
			pt.putInt(mailId);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.MAIL_DELETE_CLIENT, errorMessage);
		}
	}

	public void run() {
		Player p = (Player) ObjectAccessor.getGameObject(ref);
		if (p != null) {
			MailService mailService = Server.server.getServiceRegistry()
					.getMailService();
			synchronized (mailService) {
				DBService dbService = Server.server.getServiceRegistry()
						.getDbService();
				Mail m = dbService.mailDAO.getMailById(mailId);
				if (m != null && m.getDestId() == p.id) {
					if (m.getStatus() == Mail.UNREADED) {
						m.setStatus(Mail.UNREADED_FAVORITE);
					} else if (m.getStatus() == Mail.READED) {
						m.setStatus(Mail.READED_FAVORITE);
					}
					dbService.mailDAO.updateEntity(m);
				} else {
					error(null, peony.Messages.STRING_01059);
				}
			}
			addToClientSession();
		}
	}

}
