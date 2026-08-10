package peony.service.duel;

import peony.common.AsyncCall;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.VMap;
import peony.net.Packet;

public class DuelGoMapCall implements AsyncCall {

	private Player player;
	private DuelService manager;
	private DuelInstance instance;
	private DuelInstance currentInstance;
	
	public DuelGoMapCall(Player player, DuelService manager, DuelInstance instance, DuelInstance currentInstance) {
		super();
		this.player = player;
		this.manager = manager;
		this.instance = instance;
		this.currentInstance = currentInstance;
	}
	
	public void run() {
		
	}

	public void callFinish() throws Exception {
		VMap map1 = Server.server.getWorld().addPlayerToMap(player, manager.mapInfo[0], 
				manager.mapInfo[1], manager.mapInfo[2], false);
		player.unMoving();
		Packet pt = new Packet(OpCode.FORCE_GOMAP_SERVER);
		pt.putInt(map1.getId());
		pt.putInt(map1.getInstanceId());
		pt.putInt(manager.mapInfo[1]);
		pt.putInt(manager.mapInfo[2]);
		pt.put(map1.allowFollow() ? 1 : 0);
		player.send(pt);
		instance.addPlayer(player);
		currentInstance.players.clear();
	}

}
