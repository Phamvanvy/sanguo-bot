package peony.auction;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameObjectRef;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.levellimit.LevelLimitService;

/**
 * 发布拍卖物品 serial int itemId int 物品ID itemInstanceId int 物品实例ID count short 物品数量
 * (如果是有实例ID的物品count需为1) startPrice int 初始价格 endPrice int 一口价 (一口价可以为0,表示不设置一口价)
 * public static final short AUCTION_CREATEAUCTION_CLIENT = 477;
 * 
 * 发布成功 serial int fees int 扣除手续费 public static final short
 * AUCTION_CREATEAUCTION_SERVER = 478;
 */
public class AuctionCreateCall extends ClientSessionAsyncCall {

	protected final Logger log = Logger.getLogger(AuctionCreateCall.class);
	protected GameObjectRef playerRef;
	protected int serial;
	protected int startPrice;
	protected int endPrice;
	protected int itemId;
	protected int itemInstanceId;
	protected int count;
	protected int faction;
	int fees = 0;

	public AuctionCreateCall(ClientSession session, Packet packet, Player player) {

		super(session);
		this.playerRef = player.ref();
		this.serial = packet.getInt();
		this.itemId = packet.getInt();
		this.itemInstanceId = packet.getInt();
		this.count = packet.getInt();
		this.startPrice = packet.getInt();
		this.endPrice = packet.getInt();
	}

	public void callFinish() throws Exception {
		if (success) {
			Packet pt = new Packet(OpCode.AUCTION_CREATEAUCTION_SERVER);
			pt.putInt(serial);
			pt.putInt(fees);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.AUCTION_CREATEAUCTION_SERVER, errorMessage);
		}
	}

	public void run() {

		AuctionService auctionService = null;
		try {
			auctionService = Server.server.getServiceRegistry()
					.getAuctionService();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Player p = ObjectAccessor.getPlayer(playerRef.id);
		if (p != null) {
			if (count <= 0) {
				log.error("[AUCTIONERROR]" + LogUtil.getPlayerLogString(p)
						+ "COUNT[" + count + "]");
				return;
			}
			if (startPrice < 0) {
				log.error("[AUCTIONERROR]" + LogUtil.getPlayerLogString(p)
						+ "STARTPRICE[" + startPrice + "]");
				return;
			}
			if (endPrice != 0 && endPrice < startPrice) {
				log.error("[AUCTIONERROR]" + LogUtil.getPlayerLogString(p)
						+ "ENDPRICE[" + endPrice + "]");
				return;
			}
			PlayerTransaction tx = p.newTransaction("CAU");
			this.faction = p.faction;
			GameItem item = p.bag.removeGameItem(itemId, itemInstanceId, count,
					tx, false);
			try {
				if (item != null) {
					if (p.level < 30) {
						LevelLimitService service = Server.server
								.getServiceRegistry().getLevelLimitService();
						if (service.check(item.template.id)) {
							tx.rollback();
							error(null, "Trước đẳng cấp 30 bạn không thể giao dịch đạo cụ này!");
							addToClientSession();
							return;
						}
					}
					if(item.isBound()){
						tx.rollback();
						error("Vật phẩm đã khóa, không thể đấu giá");
						addToClientSession();
						return;
					}
					fees = auctionService.createAuction(p, startPrice,
							endPrice, item, count, tx);
				} else {
					tx.rollback();
					error(null, "Trong túi đeo không có vật phẩm đó hoặc số lượng vật phẩm đó không đủ");
				}
			} catch (AuctionException e) {
				error(e, e.getMessage());
			}
		}
		addToClientSession();
	}
}
