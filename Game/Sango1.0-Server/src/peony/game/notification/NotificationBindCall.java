package peony.game.notification;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.db.MailAttachmentCall;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.Account;

public class NotificationBindCall extends ClientSessionAsyncCall{
	
	private static final Logger log = Logger.getLogger(NotificationBindCall.class);
	
	protected String appId;
	protected String provider;
	protected String token;
	protected Account a;
	protected Player player;
	

	public NotificationBindCall(ClientSession session,Packet packet) {
		super(session);
		this.token = packet.getString();
		this.appId = NotificationService.APP_ID;
		this.provider = NotificationService.PROVIDER;
		this.a = (Account)session.getIdentity();
		player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		log.info("[NOTIFICATIONBIND]");
		if(a!=null){
			log.info("[NOTIFICATIONBINDTRY]ACCOUNT["+a.getId()+"]TOKEN["+token+"]");
			NotificationService nfService = Server.server.getServiceRegistry().getNotificationService();
			if(token.startsWith("<")){
				String temp = token.replaceAll(" ", "");
				String temp2 = temp.substring(1, temp.length()-1);
				token = temp2;
			}
			nfService.bind(appId, provider, a.getId(), token);
			if(player!=null){
				player.pushToken = token;
			}
		}
	}
}
