package peony.game.map;

import peony.common.AsyncCall;
import peony.game.ErrorHandler;
import peony.game.MapCell;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapException;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * 触碰传送点后传送到新场景的异步调用。因为场景管理器的多线程改造，所以传送地图的操作必须用异步调用
 * 的方式来完成。
 * @author lighthu
 */
public class TouchExitTransferCall implements AsyncCall {
	private Player player;
	private ClientSession session;
	private int serial;
	private int exitID;
	private int mapId;
	private int x;
	private int y;
	
	public TouchExitTransferCall(Player p, ClientSession session, int serial, 
			int exitid, int mid, int x, int y) {
		player = p;
		this.session = session;
		this.serial = serial;
		this.exitID = exitid;
		mapId = mid;
		this.x = x;
		this.y = y;
	}
	
	public void run() {
	}
	
	public void callFinish() throws Exception {
		try {
			// 广播离开消息
			player.map.map.notifyExit(player, exitID);
			
			VMap map = Server.server.getWorld().addPlayerToMap(player,
					mapId, x, y, false);
			//处理随从
			if(player!=null && player.attendant!=null){
				player.attendant.follow();
			}
			Packet pt = new Packet(OpCode.GOMAP_ALLOW_SERVER);
			pt.putInt(map.getId());
			pt.putInt(map.getInstanceId());
			pt.putInt(player.x);
			pt.putInt(player.y);
			pt.put(map.allowFollow() ? 1 : 0);
			session.send(pt);
		} catch (VMapException e) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.TOUCHEXIT_CLIENT, e.getMessage());
		}
	}
}
