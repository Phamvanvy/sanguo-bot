package peony.service.shop;

import org.apache.log4j.Logger;

import peony.game.LogUtil;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.AccountAsyncCall;

public class CmccBuyCall extends AccountAsyncCall {
	
	protected static final Logger log = Logger.getLogger(CmccBuyCall.class);
	
	protected int serial;
	protected int shopID;
	protected int itemID;
	protected short count;
	
	public CmccBuyCall(ClientSession session,Packet packet){
		super(session);
		this.serial = packet.getInt();
		this.shopID = packet.getInt();
		this.itemID = packet.getInt();
		this.count = packet.getShort();
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		Player player = (Player) session.getClient();
		if (player != null) {
            // 检查非法数据
			if(shopID != 61){
                log.error("[SHOPATTACK]" + LogUtil.getPlayerLogString(player));
                return;
			}
            if (this.count != 1) {
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
			} catch (ShopException se) {
			} catch (Exception e) {
				log.error(e, e);
			}
		}
	}

}
