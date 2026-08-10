package peony.game.admin;

import java.util.List;

import peony.common.ClientSessionAsyncCall;
import peony.game.ActorListActor;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class AdminPlayerListCall extends ClientSessionAsyncCall {
	
	protected int serial;
	protected int accountId;
	
	protected List<ActorListActor> actorList;
	
	public AdminPlayerListCall(ClientSession session, Packet pt){
		super(session);
		this.serial = pt.getInt();
		this.accountId = pt.getInt();
	}

	public void callFinish() throws Exception {
		if(success){
			 Packet pt = new Packet(OpCode.ADMIN_PLAYERLIST_SERVER);
			 pt.putInt(serial);
			 pt.putShort(actorList.size());
			 for(ActorListActor actor:actorList){
				 pt.putInt(actor.id);
				 pt.putString(actor.name);
				 pt.put(actor.sex);
				 pt.put(actor.level);
				 pt.put(actor.clazz);
				 pt.put(actor.faction);
				 pt.putShort(actor.mapId);
			 }
			 session.send(pt);
		}
	}

	public void run() {
		actorList = Server.server.getServiceRegistry().getDbService().playerDAO.getActorList(accountId);
		addToClientSession();
	}

}
