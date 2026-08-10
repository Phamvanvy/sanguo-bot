package peony.service.tong;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * 请求军团改名。
 * serial	int
 * name		String		新名称
 */
public class RenameTongCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(RenameTongCall.class);
	protected int serial;
	protected Player player;
	protected String name;
	protected Tong tong;
	protected TongService tongService;

	public RenameTongCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.name = packet.getString();
		player = (Player)session.getClient();
		tongService = Server.server.getServiceRegistry().getTongService();
	}

	public void callFinish() throws Exception {
		if (success) {
			// 下发改名成功消息
			player.message(-1, "軍團改名成功", -1, -1);
		} else {
			// 错误
			player.message(-1, errorMessage, -1, -1);
		}
	}

	public void run() {
		// 尝试扣除一个军团改名符并改名
		PlayerTransaction tx = player.newTransaction("ITE");
		try {
		    if (player.bag.removeGameItem(ItemUtil.ITEM_CHANGE_TONG_NAME, GameItem.GENERAL_INSTANCEID, 1, tx, true) == null) {
		        tx.rollback();
		        throw new Exception("您沒有軍團改名符");
		    }
		    tongService.rename(player.id, name);
		    tx.commit();
		} catch (TongException te) {
			tx.rollback();
			error(null, te.getMessage());
		} catch (Exception e) {
			tx.rollback();
			error(null, "改名時發生不明錯誤,請聯系GM解決.");
		}
		addToClientSession();
	}
}
