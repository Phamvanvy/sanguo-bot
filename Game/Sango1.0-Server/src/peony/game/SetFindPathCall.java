package peony.game;

import peony.common.SyncIbuyCall;
import peony.net.ClientSession;
import peony.net.Packet;

public class SetFindPathCall extends SyncIbuyCall {
	protected Player player;
	
	public SetFindPathCall(ClientSession session, Packet packet, Player player) {
		super(session, null);
		this.player = player;
	}

	public void callFinish() throws Exception {
		if(success){
			
		}else{
			
		}
	}

	public void run() {
		player.broadcast(player.getMovePacket(GameObject.MOVE_DETAIL),player, null, false, false, true);
	}

}
