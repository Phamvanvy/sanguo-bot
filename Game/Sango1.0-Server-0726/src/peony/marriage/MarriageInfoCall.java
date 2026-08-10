package peony.marriage;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.friend.PlayerRelation;

public class MarriageInfoCall extends ClientSessionAsyncCall {
	
	protected int serial;
	
	public MarriageInfoCall(ClientSession session, Packet packet){
		super(session);
		this.serial = packet.getInt();
	}
	
	public void callFinish() throws Exception {

	}

	public void run() {
		Player p = (Player)session.getClient();
		if(p!=null){
			PlayerRelation rel = Server.server.getServiceRegistry().getRelationService().get(p.id);
			Actor mate = null;
			if(rel != null){
				int mateId = rel.mateId;
				if (mateId != -1) {
					mate = Server.server.getServiceRegistry().getActorCacheService().find(mateId);
				}
			}
			Packet pt = new Packet(OpCode.MARRIAGE_INFO_SERVER);
			pt.putInt(serial);
			if(mate != null){
				pt.putInt(mate.id);
				pt.putString(mate.name);
				pt.put(mate.online?1:0);
			}else{
				pt.putInt(-1);
				pt.putString("");
				pt.put(0);
			}
			p.send(pt);
		}
	}

}
