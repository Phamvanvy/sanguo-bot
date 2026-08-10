package peony.service.shop;

import java.text.DecimalFormat;
import java.util.List;

import peony.common.ClientSessionAsyncCall;
import peony.game.DataService;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.Account;
import peony.service.account.cmcc.CmccAccountService;
import peony.service.account.cmcc.CmccCheckDownloadMessage;

import com.pip.sanguo.data.Rank;
import com.pip.sanguo.data.Shop;

public class CmccShopListCall extends ClientSessionAsyncCall {

	int serial;
	String cmccUserId;
	String cmccUserKey;

	public CmccShopListCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.cmccUserId = packet.getString();
		this.cmccUserKey = packet.getString();
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		Player p = (Player) session.getClient();
		if (p != null) {
			ShopService service = Server.server.getServiceRegistry()
					.getShopService();
			DataService ds = Server.server.getServiceRegistry().getDataService();
			Shop shop = service.findShop(ShopService.CMCC_SHOP);
			Packet pt = new Packet(OpCode.SHOP_LIST_SERVER);
			pt.putInt(serial);
			pt.put(1);
			pt.putShort(shop.id);
			pt.putString(shop.title);
			List<Shop.ShopItem> items = service.getShopItems(shop);
			pt.put(items.size());
			for (Shop.ShopItem item : items) {
				pt.putInt(item.item.id);
				pt.putString(item.item.title);
				pt.put(item.item.quality);
				pt.put(item.item.iconImage);
				pt.put(item.item.iconIndex);
				pt.putShort(item.remain);
				pt.put(item.buyLimit);
				pt.put(item.requirements.size());
				for (Shop.BuyRequirement req : item.requirements) {
					pt.put(req.type);
//					pt.putInt(req.amount);
					pt.putString(String.valueOf(req.amount));
					pt.put(req.deduct ? 1 : 0);
					if (req.type == Shop.TYPE_RANK) {
						Rank rank = (Rank) ds.data.findDictObject(Rank.class,
								req.amount);
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
			pt.putInt(0);
			session.send(pt);
			Account a = (Account)p.session.getIdentity();
			if(a != null){
				a.setCmccUserId(this.cmccUserId);
				a.setCmccUserKey(this.cmccUserKey);
				if(!a.isCheckDownloaded()&&this.cmccUserId.length()!=0&&this.cmccUserKey.length()!=0){
					CmccAccountService slaveAccountService = (CmccAccountService)Server.server.getServiceRegistry().getSlaveAccountService();
					slaveAccountService.postMessage(new CmccCheckDownloadMessage(a.getCmccUserId(), a.getId(), p.id, a.getJvmCode()));
					a.setCheckDownloaded(true);
				}
			}
		}
	}

}
