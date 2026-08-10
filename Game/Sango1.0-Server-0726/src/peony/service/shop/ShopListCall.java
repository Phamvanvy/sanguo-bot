package peony.service.shop;

import java.util.List;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.DataService;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.nation.Nation;
import peony.net.ClientSession;
import peony.net.Packet;

import com.pip.sanguo.data.Rank;
import com.pip.sanguo.data.Shop;

/**
 * 取商店商品列表
 * serial		int
 * shopIDs		int[]		商店ID列表
public static final short SHOP_LIST_CLIENT = 300;
 * 返回商店商品列表
 * serial		int
 * count		byte		商店数量
 * 循环N次
 *   shopID		short		商店ID
 *   title		String		商店标题
 *   itemCount	byte		商品数量
 *   循环N次
 *     id		int			物品ID
 *     name		String		物品名称
 *     quality	byte		品质
 *     icon		byte		图标ID
 *     remain	short		剩余数量，0表示无限制
 *     limit	byte		购买上限，0表示无限制
 *     reqcnt	byte		购买需求项数量，0表示无限制
 *       循环N次
 *         reqtype	byte	需求类型，见Shop类中的常量
 *         amount	int		需求金钱/i币/荣誉，或物品数量，或军衔ID
 *         deduct	byte	是否扣除，1表示是，0表示否
 *         rank		String	需求荣誉名称，只有需求类型为TYPE_RANK时有效
 *         itemID	int		物品ID，只有需求类型为TYPE_ITEM时有效
 *         itemName	String	物品名称，只有需求类型为TYPE_ITEM时有效
 *         quality	byte	品质，只有需求类型为TYPE_ITEM时有效
 *         icon		byte	图标ID，只有需求类型为TYPE_ITEM时有效
 *         varDesc  String  额外条件描述，只有需求类型为TYPE_VARIABLE时有效
public static final short SHOP_LIST_SERVER = 301;
*/
public class ShopListCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(ShopListCall.class);
	protected int serial;
	protected int[] shopIDs;
	protected Shop[] shops;
	protected int taxRate;

	public ShopListCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.shopIDs = packet.getInts();
	}

	public void callFinish() throws Exception {
		ShopService service = Server.server.getServiceRegistry().getShopService();
		DataService ds = Server.server.getServiceRegistry().getDataService();
		if (success) {
			// 创建成功，下发确认包
			Packet pt = new Packet(OpCode.SHOP_LIST_SERVER);
			pt.putInt(serial);
			pt.put(shops.length);
			for (int i = 0; i < shops.length; i++) {
				pt.putShort(shops[i].id);
				pt.putString(shops[i].title);
				List<Shop.ShopItem> items = service.getShopItems(shops[i]);
				pt.put(items.size());
				for (Shop.ShopItem item : items) {
					pt.putInt(item.item.id);
					pt.putString(item.item.title);
					pt.put(item.item.quality);
					pt.put(item.item.iconIndex);
					pt.putShort(item.remain);
					pt.put(item.buyLimit);
					pt.put(item.requirements.size());
					for (Shop.BuyRequirement req : item.requirements) {
						pt.put(req.type);
						if(req.type==Shop.TYPE_HONOR || req.type==Shop.TYPE_IMONEY || req.type==Shop.TYPE_MONEY){
							float totleDiscount = service.getShopItemDiscount(shops[i].id, req.type)/100f;
							pt.putInt(Math.round(req.amount * totleDiscount));
						}else{
							pt.putInt(req.amount);
						}
						pt.put(req.deduct ? 1 : 0);
						if (req.type == Shop.TYPE_RANK) {
						    Rank rank = (Rank)ds.data.findDictObject(Rank.class, req.amount);
							pt.putString(rank.title);
						}
						if (req.type == Shop.TYPE_ITEM) {
							pt.putInt(req.item.id);
							pt.putString(req.item.title);
							pt.put(req.item.quality);
							pt.put(req.item.iconIndex);
						}
						if (req.type == Shop.TYPE_VARIABLE) {
						    pt.putString(req.varDesc);
						}
					}
				}
			}
			pt.putInt(taxRate);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.SHOP_LIST_CLIENT, errorMessage);
		}
	}

	public void run() {
		Player p = (Player) session.getClient();
		if (p != null) {
			shops = new Shop[shopIDs.length];
			ShopService service = Server.server.getServiceRegistry()
					.getShopService();
			for (int i = 0; i < shopIDs.length; i++) {
				shops[i] = service.findShop(shopIDs[i]);
			}
			Nation winNation = Server.server.getServiceRegistry()
					.getNationService().getWinNation(p.faction);
			Nation nation = Server.server.getServiceRegistry()
					.getNationService().getNationByFaction(p.faction);
			if(winNation==null){
				taxRate = (int)(100 * nation.taxRate);
			}else{
				taxRate = (int)((100 * nation.taxRate) + (100 * Nation.FAILURE_TAX));
			}
			addToClientSession();
		}
	}
}
