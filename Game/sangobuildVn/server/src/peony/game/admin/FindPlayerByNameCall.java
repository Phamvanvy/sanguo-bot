package peony.game.admin;

import java.util.List;
import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.player.ActorCacheService;

public class FindPlayerByNameCall extends ClientSessionAsyncCall {

	protected int serial;
	protected String name;
	protected List<Actor> actors;
	
	public FindPlayerByNameCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.name = packet.getString();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.ADMIN_PLAYERINFO_SERVER);
			pt.putInt(serial);
			pt.putInt(actors.size());
			for(Actor actor : actors){
				pt.putInt(actor.id);
				pt.put(actor.exist);
				pt.put(actor.sex);
				pt.put(actor.clazz);
				pt.put(actor.faction);
			}
			session.send(pt);
		}else{
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_PLAYERINFO_CLIENT, errorMessage);
		}
	}

	public void run() {
		ActorCacheService service = Server.server.getServiceRegistry().getActorCacheService();
		actors = service.fingActorsByName(name);
		if(actors==null || actors.size()==0){
			error("没有符合此名字的角色信息");
		}
		addToClientSession();
 	}

}
