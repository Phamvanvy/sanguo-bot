package peony.game.admin;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class RenewPlayerCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int id;
	
	public RenewPlayerCall(Packet packet, ClientSession session) {
		super(session);
		this.serial = packet.getInt();
		this.id = packet.getInt();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.ADMIN_RENEWPLAYER_SERVER);
			pt.putInt(serial);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.ADMIN_RENEWPLAYER_CLIENT, errorMessage);
		}
	}

	public void run() {
		Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(id);
		if(actor==null){
			error("沒有找到此角色");
			return;
		}
		Player p = Server.server.getServiceRegistry().getDbService().playerDAO.getPlayerById(actor.id);
		p.exist = 1;
		Server.server.getServiceRegistry().getDbService().playerDAO.updateEntity(p);
		addToClientSession();
	}

}
