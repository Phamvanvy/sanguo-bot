package peony.service.apprentice;

import java.text.MessageFormat;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.chat.ChatService;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * 解除师徒关系
 *
 */
public class RemoveApprenticeCall extends ClientSessionAsyncCall{
	protected int serial;
	protected int targetId;
	protected Player p;

	public RemoveApprenticeCall(ClientSession session,Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.targetId = packet.getInt();
		this.p = (Player) session.getClient();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.PLAYER_REMOVEAPPRENTICE_SERVER);
			pt.putInt(serial);
			p.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.PLAYER_REMOVEAPPRENTICE_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(p!=null){
			int apprenticeId = p.id;
			int teacherId = p.id;
			ApprenticeService service = Server.server.getServiceRegistry().getApprenticeService();
			ChatService chatService = Server.server.getServiceRegistry().getChatService();
			try {
				if (p.level >= 70) {
					apprenticeId = targetId;
				} else {
					teacherId = targetId;
				}
				if (apprenticeId != -1 && teacherId != -1) {
					service.saveRemove(apprenticeId, teacherId);
					Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(targetId);
					if(p.level>=70){
						p.pool.setLong(Player.PROPERTY_TEACHER_LASTTIME,System.currentTimeMillis());
						String msg1 = MessageFormat.format(peony.Messages.STRING_01039,
								actor.name);
						chatService.sendPrivateMessage(p.id, msg1);
						Player app = ObjectAccessor.getPlayer(targetId);
						if(app != null){
							String msg2 = MessageFormat.format(peony.Messages.STRING_01040,
									p.name);
							chatService.sendPrivateMessage(targetId, msg2);
						} else {
							Server.server.getServiceRegistry().getMailService().sendSystemMail(targetId, peony.Messages.STRING_00004,peony.Messages.STRING_00087,MessageFormat.format(peony.Messages.STRING_01041,
									p.name), 0,null, 0,"APPRENTICEREMOVERELATION");
						}
					} else {
						p.pool.setLong(Player.PROPERTY_APPRENTICE_LASTTIME, System.currentTimeMillis());
						String msg1 = MessageFormat.format(peony.Messages.STRING_01042,
								actor.name);
						chatService.sendPrivateMessage(p.id, msg1);
						Player teacher = ObjectAccessor.getPlayer(targetId);
						if(teacher != null){
							String msg2 = MessageFormat.format(peony.Messages.STRING_01043,
									p.name);
							chatService.sendPrivateMessage(targetId, msg2);
						} else {
							Server.server.getServiceRegistry().getMailService().sendSystemMail(targetId, peony.Messages.STRING_00004,peony.Messages.STRING_00087,MessageFormat.format(peony.Messages.STRING_01044,
									p.name), 0,null, 0,"APPRENTICEREMOVERELATION");
						}
					}
					LogUtil.removeApprentice(p, targetId);
				}
			}catch(Exception e){
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.PLAYER_REMOVEAPPRENTICE_CLIENT, e.getMessage());
			}
		}
		addToClientSession();
	}

}
