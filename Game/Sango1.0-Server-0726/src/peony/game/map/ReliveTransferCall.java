package peony.game.map;

import peony.common.AsyncCall;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.ReliveOption;
import peony.game.Server;
import peony.game.VMap;
import peony.net.Packet;

/**
 * 复活并传送到新场景的异步调用。因为场景管理器的多线程改造，所以传送地图的操作必须用异步调用
 * 的方式来完成。
 * @author lighthu
 */
public class ReliveTransferCall implements AsyncCall {
	private Player player;
	private ReliveOption option;
	
	public ReliveTransferCall(Player p, ReliveOption option) {
		player = p;
		this.option = option;
	}
	
	public void run() {
	}
	
	public void callFinish() throws Exception {
		option.relive(player);
	}
}
