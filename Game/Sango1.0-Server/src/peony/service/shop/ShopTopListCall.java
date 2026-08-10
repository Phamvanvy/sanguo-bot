package peony.service.shop;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import peony.common.ClientSessionAsyncCall;
import peony.game.DataService;
import peony.game.ErrorHandler;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.itemeffect.AddItemEffect;
import peony.game.itemeffect.AttendantItemEffect;
import peony.game.itemeffect.DropItemEffect;
import peony.game.itemeffect.HorseItemEffect;
import peony.game.nation.Nation;
import peony.net.ClientSession;
import peony.net.Packet;
import com.pip.sanguo.data.AttendantType;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Rank;
import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.item.DropGroup;
import com.pip.sanguo.data.item.DropItem;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.item.SubDropGroup;

public class ShopTopListCall extends ClientSessionAsyncCall {

	protected final Logger log = Logger.getLogger(ShopListCall.class);
	protected int serial;
	protected int[] shopIDs;
	protected Shop[] shops;
	protected int taxRate;
	public static final byte TYPE_HORSE_IMAGE = 100;
	public static final byte TYPE_ATTENDANT_IMAGE = 101;
	public static final Map<Integer,Integer> itemId2HorseImageId = new HashMap<Integer,Integer>();

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
			//商店及商品
			for (int i = 0; i < shops.length; i++) {
				pt.putShort(shops[i].id);
				pt.putString(shops[i].title);
				List<Shop.ShopItem> items = service.getShopItems(shops[i]);
				pt.put(items.size());
				for (Shop.ShopItem item : items) {
					writeShopItem(pt, item,ds.data,shops[i].id);
				}
			}
			pt.put(service.topShopItems.size());
			//热门推荐
			for (Shop.ShopItem item : service.topShopItems) {
				int shopId = 0;
				try {
					shopId = service.getShopByItemId(item.item.id).id;
				} catch (Exception e) {
				}
				writeShopItem(pt, item, ds.data,shopId);
			}
			pt.putInt(taxRate);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.SHOP_LIST_CLIENT, errorMessage);
		}
	}
	
	/**
	 * 写入一个商品信息
	 * @param pt
	 * @param item
	 */
	private static void writeShopItem(Packet pt,Shop.ShopItem item,ProjectData pd, int shopId){
		pt.putInt(item.item.id);
		pt.putString(item.item.title);
		pt.put(item.item.quality);
		pt.put(item.item.iconImage);
		pt.put(item.item.iconIndex);
		pt.putShort(item.remain);
		pt.put(item.buyLimit);
		if(item.item.type == Item.TYPE_HORSE || item.item.type == Item.TYPE_HORSE_PACKAGE || item.item.type == Item.TYPE_ATTENDANT){
			//坐骑商品、随从商品由于要预览，所以多发一个requirement来记录坐骑图片id
			pt.put((byte)(item.requirements.size() + 1));
		} else {
			pt.put((byte)item.requirements.size());
		}
		for (Shop.BuyRequirement req : item.requirements) {
			pt.put(req.type);
			if(req.type==Shop.TYPE_HONOR || req.type==Shop.TYPE_IMONEY || req.type==Shop.TYPE_MONEY || req.type==Shop.TYPE_SALARY || req.type==Shop.TYPE_REPUTATION){
				float totleDiscount = Server.server.getServiceRegistry().getShopService().getShopItemDiscount(shopId, req.type)/100f;
				float price = req.amount * totleDiscount;
				DecimalFormat df = new DecimalFormat("0.00");
				String showPrice = df.format(price);
//				pt.putInt(Math.round(req.amount * totleDiscount));
				pt.putString(showPrice);
			}else{
//				pt.putInt(req.amount);
				pt.putString(String.valueOf(req.amount));
			}
			pt.put(req.deduct ? 1 : 0);
			if (req.type == Shop.TYPE_RANK) {
			    Rank rank = (Rank)pd.findDictObject(Rank.class, req.amount);
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
		//查找并下发坐骑图片id，以便预览之
		if(item.item.type == Item.TYPE_HORSE || item.item.type == Item.TYPE_HORSE_PACKAGE){
			int horseImage = -1;
			Integer horseImageId = itemId2HorseImageId.get(item.item.id);
			if(horseImageId != null){
				horseImage = horseImageId.intValue();
			} else {
				horseImage = getHorseImageId(item.item,pd);
				itemId2HorseImageId.put(item.item.id, horseImage);
			}
			pt.put(TYPE_HORSE_IMAGE);
//			pt.putInt(1);
			pt.putString("1");
			pt.put(0);
			pt.putInt(horseImage);
		} else if(item.item.type == Item.TYPE_ATTENDANT){
			int attImage = -1;
			Integer attImageId = itemId2HorseImageId.get(item.item.id);
			if(attImageId != null){
				attImage = attImageId.intValue();
			} else {
				attImage = getAttendantImageId(item.item,pd);
				itemId2HorseImageId.put(item.item.id, attImage);
			}
			pt.put(TYPE_ATTENDANT_IMAGE);
//			pt.putInt(1);
			pt.putString("1");
			pt.put(0);
			pt.putInt(attImage);
		}
	}
	
	/**
	 * 查找坐骑图片id
	 * @param item
	 * @return
	 */
	private static int getHorseImageId(Item item,ProjectData pd){
		int ret = -1;
		ItemEffect[] effects = ItemUtil.translateEffects(item.effects);
		for(int i=0;i<effects.length;i++){
			ItemEffect eff = effects[i];
			if(eff instanceof HorseItemEffect){//直接就是坐骑物品
				ret = ((HorseItemEffect)eff).getHorseImageId();
				break;
			} else if(eff instanceof AddItemEffect){//获得是坐骑物品
				int[] itemIds = ((AddItemEffect)eff).getItemIds();
				for(int j=0;j<itemIds.length;j++){
					int itemId = itemIds[j];
					Item it = pd.findItem(itemId);
					if(it.type == Item.TYPE_HORSE || it.type == Item.TYPE_HORSE_PACKAGE){
						ret = getHorseImageId(it,pd);
						break;
					}
				}
			} else if(eff instanceof DropItemEffect){//掉落组
				int groupId = ((DropItemEffect)eff).getGroupIds();
				DropGroup dg = (DropGroup)pd.findObject(DropGroup.class, groupId);
				if(dg != null){
					for (SubDropGroup sdg : dg.subGroup) {
						for (DropItem di:sdg.dropGroup) {
							if(di.dropType == 0){
								Item it = pd.findItem(di.dropID);
								if(it.type == Item.TYPE_HORSE){
									ret = getHorseImageId(it,pd);
									break;
								}
							}
						}
					}
				}
			}
		}
		return ret;
	}
	
	/**
	 * 查找随从图片id
	 * @param item
	 * @return
	 */
	private static int getAttendantImageId(Item item,ProjectData pd){
		int ret = -1;
		ItemEffect[] effects = ItemUtil.translateEffects(item.effects);
		for(int i=0;i<effects.length;i++){
			ItemEffect eff = effects[i];
			if(eff instanceof AttendantItemEffect){//直接就是随从召唤令物品
				ret = ((AttendantItemEffect)eff).attendantId;
				break;
			}
		}
		if(ret != -1){//找到了随从id
			AttendantType att = (AttendantType)pd.findObject(AttendantType.class, ret);
			if(att != null){
				ret = att.image.id;
			}
		}
		return ret;
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
