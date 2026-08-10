package peony.service.tong;

import org.apache.log4j.Logger;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * 请求退出军团。
 * serial	int
 */
public class TongShopItemBuy extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(TongShopItemBuy.class);
	protected int serial;
	protected int itemId;
	protected Player player;
	protected TongService tongService;

	public TongShopItemBuy(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.itemId = packet.getInt();
		player = (Player)session.getClient();
		tongService = Server.server.getServiceRegistry().getTongService();
	}

	public void callFinish() throws Exception {
		if (success) {
			// 购买成功，下发确认包
			Packet pt = new Packet(OpCode.TONG_SHOP_BUY_SERVER);
			pt.putInt(serial);
			pt.putInt(player.contribute);
			pt.putInt(player.bag.getGameItemCount(TongService.suiPianIds[player.clazz]));
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.TONG_SHOP_BUY_CLIENT, errorMessage);
		}
	}

	public void run() {
		// 请求退出
		if(player == null)
			return;
		try {
			tongService.tongShopBuy(player, itemId);
		} catch (TongException e) {
			error(null, e.getMessage());
		}
		addToClientSession();
	}
}
