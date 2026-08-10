package peony.service.apprentice;

import java.util.ArrayList;
import java.util.List;
import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.friend.PlayerRelation;
import peony.service.friend.RelationList;
import peony.service.friend.RelationService;


public class ApprenticeListCall extends ClientSessionAsyncCall{
	
	private int serial;
	private List<Actor> actorList;
	private Player p;

	public ApprenticeListCall(ClientSession session,Packet packet) {
		super(session);
		this.serial = packet.getInt();
		actorList = new ArrayList<Actor>();
		this.p = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(success){
			RelationService rs = Server.server.getServiceRegistry().getRelationService();
			PlayerRelation relation = rs.get(p.id);
			if (relation == null) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.PLAYER_APPRENTICELIST_CLIENT, peony.Messages.STRING_00436);
				return;
			}
			if(relation.apprenticeList == null){
				relation.apprenticeList = new RelationList();
			}
			Packet pt = new Packet(OpCode.PLAYER_APPRENTICELIST_SERVER);
			pt.putInt(serial);
			if(p.level<70){
				int teacherId = relation.teacherId;
				if(teacherId != -1){
					Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(teacherId);
					actorList.add(actor);
				}
				
			} else {
				relation.apprenticeList.refreshPlayers();
				int count = relation.apprenticeList.getCount();
				for(int i=0;i<count;i++){
					Actor actor = relation.apprenticeList.getPlayerAt(i);
					if(actor != null){
						actorList.add(actor);
					}
				}
			}
			if(actorList!=null && actorList.size()>0){
				pt.putInt(actorList.size());
				for(Actor a : actorList){
					pt.putInt(a.id);
					pt.putString(a.name);
					pt.put(a.online ? 1 : 0);
					pt.putShort(a.level);
					pt.put(a.sex);
					pt.put(a.clazz);
					pt.put(a.faction);
					pt.put(a.level>=70 ? 0:1);
				}
			} else {
				pt.putInt(0);
			}
			p.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.PLAYER_APPRENTICELIST_CLIENT, errorMessage);
		}
	}

	public void run() {
		addToClientSession();
	}
}
