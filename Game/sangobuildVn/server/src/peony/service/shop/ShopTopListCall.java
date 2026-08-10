package peony.service.shop;

import java.util.List;
import org.apache.log4j.Logger;
import com.pip.sanguo.data.Rank;
import com.pip.sanguo.data.Shop;
import peony.common.ClientSessionAsyncCall;
import peony.game.DataService;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.nation.Nation;
import peony.net.ClientSession;
import peony.net.Packet;

public class ShopTopListCall extends ClientSessionAsyncCall {

	protected final Logger log = Logger.getLogger(ShopListCall.class);
	protected int serial;
	protected int[] shopIDs;
	protected Shop[] shops;
	protected int taxRate;

	public ShopTopListCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.shopIDs = packet.getInts();
	}

	public void callFinish() throws Exception {
		ShopService service = Server.server.getServiceRegistry().getShopService();
		DataService ds = Server.server.getServiceRegistry().getDataService();
		if (success) {
			// 创建成功，下发确认包
			Packet pt = new Packet(OpCode.SHOP_TOPLIST_SERVER);
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
						pt.putInt(req.amount);
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
			pt.put(service.topShopItems.size());
			for (Shop.ShopItem item : service.topShopItems) {
				pt.putInt(item.item.id);
				pt.putString(item.item.title);
				pt.put(item.item.quality);
				pt.put(item.item.iconIndex);
				pt.putShort(item.remain);
				pt.put(item.buyLimit);
				pt.put(item.requirements.size());
				for (Shop.BuyRequirement req : item.requirements) {
					pt.put(req.type);
					pt.putInt(req.amount);
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
