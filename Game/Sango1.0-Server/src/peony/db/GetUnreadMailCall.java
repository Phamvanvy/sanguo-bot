package peony.db;

import java.text.MessageFormat;

import peony.common.ClientSessionAsyncCall;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class GetUnreadMailCall extends ClientSessionAsyncCall{
	
	/**
	 * 玩家登陆未读邮件提醒
	 */
	
	protected Player player;

	public GetUnreadMailCall(ClientSession session,Player player) {
		super(session);
		this.player = player;
	}

	public void callFinish() throws Exception {
		
		
	}

	public void run() {
		if(player!=null){
			MailDAO mailDAO = Server.server.getServiceRegistry().getDbService().mailDAO;
			long mailUnreadCount = mailDAO.countUnreadMail(player.id);
			if(mailUnreadCount > 0){
				String message = "您有未读信件";
				boolean isNewUI = player.isNewUI();
				if(isNewUI){
					message = "您有未读信件，请点击屏幕上闪烁的信封来收取邮件。";
				}
				Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, message);
				Packet pt = new Packet(OpCode.MAIL_NEW_SERVER);
				player.send(pt);
			}
		}
	}

}
