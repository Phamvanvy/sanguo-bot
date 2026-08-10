package peony.game.map;

import peony.common.AsyncCall;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapException;
import peony.net.Packet;

/**
 * 强制传送到新场景的异步调用。因为场景管理器的多线程改造，所以传送地图的操作必须用异步调用
 * 的方式来完成。
 * @author lighthu
 */
public class ForceGoMapCall implements AsyncCall {
	private Player player;
	private int mapId;
	private int x;
	private int y;
	
	public ForceGoMapCall(Player p, int mid, int x, int y) {
		player = p;
		mapId = mid;
		this.x = x;
		this.y = y;
	}
	
	public void run() {
	}
	
	public void callFinish() throws Exception {
		try {
			VMap newMap = Server.server.getWorld().addPlayerToMap(player, mapId,
					x, y, false);
			if(newMap == null){
				return;
			}
			player.unMoving();
			// acceptMoving = false;
			Packet pt = new Packet(OpCode.FORCE_GOMAP_SERVER);
			pt.putInt(newMap.getId());
			pt.putInt(newMap.getInstanceId());
			pt.putInt(x);
			pt.putInt(y);
			pt.put(newMap.allowFollow() ? 1 : 0);
			player.send(pt);
			if (player.party != null && player.party.leader.player == player) {
				Packet tranPt = new Packet(OpCode.LEADER_TRAN_SERVER);
				tranPt.putInt(newMap.getId());
				tranPt.putInt(newMap.getInstanceId());
				player.party.broadcast(tranPt, player);
			}
		} catch (VMapException e) {
			player.message(-1, e.getMessage(), -1, -1);
		}
	}
}
