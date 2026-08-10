package peony.service.shop;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemTemplate;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.TransactionBagGrid;
import peony.game.nation.Nation;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.tong.Tong;
import peony.service.tong.TongService;
import peony.service.tong.apply.TongBattleApplyService;

/**
 * 请求出售物品 serial int gridId byte 包格ID itemID int 物品ID instanceID int 实例ID count
 * short 物品数量 public static final short SHOP_SELL_CLIENT = 304; 出售物品成功 serial
 * int amount int 获得金钱数量 public static final short SHOP_SELL_SERVER = 305;
 */
public class ShopSellCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(ShopSellCall.class);
	protected int serial;
	protected int gridId;
	protected int itemID;
	protected int instanceID;
	protected short count;
	protected int sellMoney;

	public ShopSellCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.gridId = packet.get();
		this.itemID = packet.getInt();
		this.instanceID = packet.getInt();
		this.count = packet.getShort();
	}

	public void callFinish() throws Exception {
		if (success) {
			// 购买成功，下发确认包
			Packet pt = new Packet(OpCode.SHOP_SELL_SERVER);
			pt.putInt(serial);
			pt.putInt(sellMoney);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.SHOP_SELL_CLIENT, errorMessage);
		}
	}

	public void run() {
		// 出售
		Player player = (Player) session.getClient();
		if (player != null) {
			LogUtil.logShopSellTry(player, itemID, instanceID, count);
			if (count > 0) {
				ItemTemplate template = ObjectAccessor.getItemTemplate(itemID);
				if (template != null) {
					if (!template.canSale) {
						error(null, "此物品不能出售");
					} else {
						PlayerTransaction tx = player.newTransaction("SELL");
						TransactionBagGrid grid = player.bag.removeGridGameItem(gridId, itemID,
								instanceID, count, tx, true);
						if (grid != null) {
							Nation winNation = Server.server.getServiceRegistry().getNationService().getWinNation(player.faction);
							Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(player.faction);
							sellMoney = template.price * count;
							int tp = sellMoney;
							int v = (int)(sellMoney * nation.taxRate);
							int tax = v;
							if (v > 0) {
								nation.addMoney(v);
								if(winNation!=null){
									int v1 = (int)(sellMoney * Nation.FAILURE_TAX); //战败被征收的税
									if (v1 > 0) {
										sellMoney -= v1;
										winNation.addMoney(v1);
										tax += v1;
									}
								}
								sellMoney -= v;
							}
							//处理城战税率
							int mapId = player.map.getId();
							TongBattleApplyService applyService = Server.server.getServiceRegistry().getTongBattleApplyService();
							TongService tongService = Server.server.getServiceRegistry().getTongService();
							Tong tong = applyService.getWinnerTong(mapId);
							if(tong!=null && tong.taxRate>0 && tongService.getPlayerTong(player.id)!=tong){
								int ta = (int) (sellMoney * tong.taxRate);
								sellMoney = sellMoney - ta;
								tong.addMoney(ta);
							}
							player.addMoney(sellMoney, tx, true);
							GameItem gitem = grid.getItem();
							tx.commit();
							LogUtil.logShopSellOK(player, gitem, count, tp, tax);
						} else {
							tx.rollback();
							error(null, "物品不存在或数量不足");
						}
					}
				} else {
					error(null, "物品不存在或数量不足");
				}

			} else {
				error(null, "错误");
			}
			addToClientSession();
		}
	}
}
