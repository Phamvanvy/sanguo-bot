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
	private GameObjectRef ref;
	
	private int type;//类型
	private int playerId;
	
	public static int MAIL_SYSTEM = 0;//系统邮件
	public static int MAIL_PLAYER = 1;//玩家邮件
	public static int MAIL_PAY = 2;//需要对方付费的邮件
	
	private boolean hasDelete=false;//是否有删除的邮件。
	
	public static int MAIL_LIST_TYPE = 0;
	private Player p;
	public MailObsoleteDeleteCall(ClientSession session, int serial,
			GameObjectRef ref,int playerId,int type) {
		super(session);
		this.serial = serial;
		this.ref = ref;
		this.type=type;
		this.playerId=playerId;
		p = (Player) ObjectAccessor.getGameObject(ref);
	}

	public void callFinish() throws Exception {
		if (success) {
			Packet pt = new Packet(OpCode.MAIL_OBSOLETE_DELETE_SERVER);
			pt.putInt(serial);
			session.send(pt);
			p.message(-1, hasDelete?"删除成功！":"未给您删除任何信件，因为您飞鸽中的信件都尚存有附件，请您提取后再进行一键删除的操作", -1,-1);
			hasDelete=false;
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.MAIL_OBSOLETE_DELETE_CLIENT, errorMessage);
		}
	}

	public void run() {
		if (p != null) {
			MailService mailService = Server.server.getServiceRegistry()
					.getMailService();
			if (mailService.isForbid(p.id)) {
				error(null, peony.Messages.STRING_01019);
				addToClientSession();
				return;
			}
			/*if(pageIndex!=15 && pageIndex!=30){
				error(null, "索引不能小于0!"peony.Messages.STRING_01020);
				addToClientSession();
				return;
			}*/
			DBService dbService = Server.server.getServiceRegistry()
					.getDbService();
			synchronized (mailService) { // 需要跟周期性的检查邮件同步
				//testmodify
//				Date time = new Date(System.currentTimeMillis() - 3 * 60 * 1000L);
//				Date time = new Date(System.currentTimeMillis() - pageIndex * 24
//						* 3600 * 1000l);
//				List<Mail> mails = dbService.mailDAO
//						.getObsoleteNoAttachmentMails(p.id, time);
//				if (mails != null) {
//					for (Mail mail : mails) {
//						dbService.mailDAO.makeTransient(mail);
//					}
//				} else {
//					error(null, peony.Messages.STRING_01021);
//				}
				List<Mail> list=null;
				if(type == MAIL_PLAYER){
					int count=dbService.mailDAO.getMailCount(playerId, new Date());
					list = dbService.mailDAO.getPlayerMailListByDestId(playerId, 0, count,new Date());
				}else if(type == MAIL_PAY){
					int payCount=dbService.mailDAO.getRequestPayMailCount(playerId, new Date());
					list = dbService.mailDAO.getMailListByRequestPay(playerId, 0, payCount,new Date());
				}else if(type == MAIL_SYSTEM){
					int sysCount=dbService.mailDAO.getSystemMailCount(playerId, new Date());
					list = dbService.mailDAO.getSystemMailList(playerId, 0, sysCount,new Date());
				}
				if(list!=null){
					for(Mail templist:list){
						if(templist.getAttachment()==null){
							dbService.mailDAO.makeTransient(templist);
							hasDelete=true;
						}
					}
				}
			}
			addToClientSession();
		}
	}
}
