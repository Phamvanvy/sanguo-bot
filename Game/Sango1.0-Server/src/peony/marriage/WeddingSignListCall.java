package peony.marriage;

import java.util.List;
import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.friend.PlayerRelation;
import peony.service.friend.RelationService;
import peony.service.tong.Tong;
import peony.service.tong.TongService;

public class WeddingSignListCall extends ClientSessionAsyncCall{
	protected int serial;
	protected Player player;

	
	public WeddingSignListCall(Packet packet,ClientSession session) {
		super(session);
		this.serial = packet.getInt();
		player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
	
	}

	public void run() {
		if(player!=null){
		    WeddingService service = Server.server.getServiceRegistry().getWeddingService();
	    	List<Actor> actors = service.getSignIns(player);
	    	RelationService rs = Server.server.getServiceRegistry().getRelationService();
	    	TongService tongService = Server.server.getServiceRegistry().getTongService();
	    	WeddingInstance instance = (WeddingInstance)player.map.map.instance;
	    	PlayerRelation relationMan = rs.get(instance.man.id);
	    	PlayerRelation relationWoman = rs.get(instance.woman.id);
	    	Tong tmMan = tongService.getPlayerTong(instance.man.id,true);
	    	Tong tmWoman = tongService.getPlayerTong(instance.woman.id,true);
	    	byte type;
	    	Packet pt = new Packet(OpCode.WEDDING_SIGNINLIST_SERVER);
	    	pt.putInt(serial);
	    	pt.putInt(actors == null?0:actors.size());
	    	if(actors != null){
		    	for(Actor actor:actors){
		    		byte isFetch = 0;
		    		if(instance.getgift.contains(new Integer(actor.id))){
		    			isFetch = 1;
		    		}
		    		Tong tm2 = tongService.getPlayerTong(actor.id,true);
					type = 0;
					
		    		if((relationMan != null && relationMan.friends.exists(actor.id)) || (relationWoman != null && relationWoman.friends.exists(actor.id))){
		    			type |= 1;
		    		}
		    		if(tm2!=null && ((tmMan!=null && tmMan.id == tm2.id) || (tmWoman!=null && tmWoman.id == tm2.id))){
		    			type |= 2;
		    		} 
		    		pt.putInt(actor.id);
		    		pt.putString(actor.name);
		    		pt.put(type);
		    		pt.put(isFetch);
		    	}
	    	}
	    	player.send(pt);
	   }
		addToClientSession();
	}

}
