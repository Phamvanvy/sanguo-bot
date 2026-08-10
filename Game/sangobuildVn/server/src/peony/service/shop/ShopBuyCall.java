package peony.service.shop;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.LogUtil;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * 请求购买商品
 * serial		int
 * shopID		short		商店ID
 * itemID		int			物品ID
 * count		short		购买数量
public static final short SHOP_BUY_CLIENT = 302;
 * 购买商品成功
 * serial		int
 * itemID		int			物品ID
 * count		short		购买数量
 * itemName		String		物品名称
 * quality		byte		品质
 * icon			byte		图标ID
public static final short SHOP_BUY_SERVER = 303;
*/
public class ShopBuyCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(ShopBuyCall.class);
	protected int serial;
	protected int shopID;
	protected int itemID;
	protected short count;

	public ShopBuyCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.shopID = packet.getInt();
		this.itemID = packet.getInt();
		this.count = packet.getShort();
	}

	public void callFinish() throws Exception {
	}

	public void run() {
		// 申请购买
		Player player = (Player) session.getClient();
		if (player != null) {
            // 检查非法数据
            if (this.count < 1 || this.count > 99) {
                log.error("[SHOPATTACK]" + LogUtil.getPlayerLogString(player));
                return;
            }
            LogUtil.logShopBuyTry(player, shopID, itemID, count);
			ShopService service = Server.server.getServiceRegistry()
					.getShopService();
			try {
				if (!service.buy((Player) session.getClient(), serial,
						shopID, itemID, count)) {
					// 如果需要异步支付，则下发包在支付结果收到后发送
					return;
				}
			} catch (Exception e) {
				log.error(e, e);
			}
		}
	}
}
