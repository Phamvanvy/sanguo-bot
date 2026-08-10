package peony.game.stepserver;

import peony.common.AsyncCall;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.VMap;
import peony.net.Packet;

public class StepBattleFinalsGoMapCall implements AsyncCall {

	private Player player;
	private StepBattleInstanceFinals instance;
	private StepBattleInstanceFinals currentInstance;
	public int mapInfo[];

	public StepBattleFinalsGoMapCall(Player player,int mapInfo[],
			StepBattleInstanceFinals instance, StepBattleInstanceFinals currentInstance) {
		super();
		this.player = player;
		this.instance = instance;
		this.currentInstance = currentInstance;
		this.mapInfo=mapInfo;
	}

	public void run() {

	}

	public void callFinish() throws Exception {
		VMap map1 = Server.server.getWorld().addPlayerToMap(player,
				mapInfo[0], mapInfo[1], mapInfo[2],
				false);
		player.unMoving();
		Packet pt = new Packet(OpCode.FORCE_GOMAP_SERVER);
		pt.putInt(map1.getId());
		pt.putInt(map1.getInstanceId());
		pt.putInt(mapInfo[1]);
		pt.putInt(mapInfo[2]);
		pt.put(map1.allowFollow() ? 1 : 0);
		player.send(pt);
		instance.addPlayer(player);
		currentInstance.players.clear();
//		currentInstance.state=StepBattleInstanceFinals.STATE_END;
	}

}
