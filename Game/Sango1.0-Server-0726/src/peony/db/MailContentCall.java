package peony.db;

import peony.common.ClientSessionAsyncCall;
import peony.game.CommonUtil;
import peony.game.ErrorHandler;
import peony.game.Mail;
import peony.game.OpCode;
import peony.game.Server;
import peony.game.mail.MailService;
import peony.net.ClientSession;
import peony.net.Packet;

public class MailContentCall extends ClientSessionAsyncCall{
	
	private int serial;
	private int mailId;
	private int playerId;
	private Mail mail;
	
	public MailContentCall(ClientSession session,int serial,int mailId,int playerId){
		super(session);
		this.serial = serial;
		this.mailId = mailId;
		this.playerId = playerId;
	}
	
	public void callFinish() throws Exception {
		if(success){
//			 * serial							int
//			 * mailId							int
//			 * sourceId							int
//			 * sourceName						string
//			 * title							string
//			 * content							string
//			 * data					            string
//			 * expirationTime				    string
//			 * price							int
//			 * attachment						byte[]如果是物品{1(byte),itemId(int),instanceId(int),count(byte),name(string)}，如果是金钱{2(byte),count(int)}
//			 */
			Packet pt = new Packet(OpCode.MAIL_CONTENT_SERVER);
			pt.putInt(serial);
			pt.putInt(mail.getId());
			pt.putInt(mail.getSourceId());
			pt.putString(mail.getSourceName());
			pt.putString(mail.getTitle());
			pt.putString(mail.getContent());
			pt.putString(CommonUtil.getDateString(mail.getPostTime()));
			pt.putString(CommonUtil.getRemainTimeString(mail.getExpirationTime()));
			pt.putInt(mail.getPrice());
			pt.put(mail.getAttachmentClientBytes());
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.MAIL_CONTENT_SERVER, errorMessage);
		}
	}

	public void run() {
		DBService dbService = Server.server.getServiceRegistry().getDbService();
		MailService mailService = Server.server.getServiceRegistry()
				.getMailService();
		synchronized (mailService) {
			mail = dbService.mailDAO.getMailById(mailId);
			if (mail != null && mail.getStatus() == Mail.UNREADED) {
				mail.setStatus(Mail.READED);
				dbService.mailDAO.updateEntity(mail);
			}
			if (mail != null) {
				if (mail.getDestId() != playerId && mail.getSourceId() != playerId) {
					mail = null;
					error(null, "取邮件错误");
				}
			} else {
				error(null, "该邮件已被发件人索回");
			}
		}
		addToClientSession();
	}

}
