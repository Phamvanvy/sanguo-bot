package peony.game.itemenhance;

import java.util.List;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.ShopService;

import com.pip.sanguo.data.Shop;

/**
 * 镶嵌：取镶嵌系统相关配置数据。
 * public static final short DECORATE_GET_CONFIG_CLIENT = 503;
 * 镶嵌：返回镶嵌系统相关配置数据。
 * levelcount           byte                宝石级别总数（循环变量）
 *    mergesuccrate1    byte                3颗成功概率（百分比）
 *    mergesuccrate2    byte                4颗成功概率（百分比）
 *    mergesuccrate3    byte                5颗成功概率（百分比）
 *    mergeprice        int                 合成需求金钱
 *    mergeitem         int                 需求合成符ID
 *    mergeitemicon     byte                需求合成符图标
 *    mergeitemname     String              合成符名称
 *    addprice          int                 镶嵌需求金钱
 *    removeprice       int                 取下需求金钱
 *    removeitem        int                 取下需要摘除符ID
 *    removeitemicon    byte                摘除符图标
 *    removeitemname    String              摘除符名称
 *  addsuccrate         byte                无镶嵌符时的镶嵌成功率
 *  additem1            int                 低级镶嵌符ID
 *  additemicon1        byte                低级镶嵌符图标
 *  additemname1        String              低级镶嵌符名称
 *  addsuccrate1        byte                低级镶嵌符成功率
 *  additem2            int                 高级镶嵌符ID
 *  additemicon2        byte                高级镶嵌符图标
 *  additemname2        String              高级镶嵌符名称
 *  addsuccrate2        byte                高级镶嵌符成功率
 *  holeconfigs         byte                打孔符配置数（循环变量）
 *    holelvl           byte                装备级别上限（含）
 *    holeitem          int                 打孔符ID
 *    holeitemicon      byte                打孔符图标
 *    holeitemname      String              打孔符名称
 * public static final short DECORATE_GET_CONFIG_SERVER = 504;
 */
public class GetJewelConfigCall extends ClientSessionAsyncCall {
	protected final static Logger log = Logger.getLogger(GetJewelConfigCall.class);
	protected Player player;
	
	private static Packet pt = null;
	public static int ITEM_STAR_ENHANCE_LEVEL1_PRICE;
	public static int ITEM_STAR_ENHANCE_LEVEL2_PRICE;
	public static int ITEM_STAR_ENHANCE_LEVEL3_PRICE;
	public static int ITEM_NATURAL_ENHANCE_PRICE;
	public static long lastBuildConfigTime = 0;

	public GetJewelConfigCall(ClientSession session, Packet packet) {
		super(session);
		this.player = (Player)session.getClient();
	}
	
	private static int getItemPrice(int itemId){
		ShopService ss = Server.server.getServiceRegistry().getShopService();
		List<Integer> ishops = ss.getIshops();
		Shop.ShopItem item = null;
		for (int i = 0; i < ishops.size(); i++) {
			Shop is = ss.findShop(ishops.get(i).intValue());
			if(is == null){
				continue;
			}
			
			for (int j = 0; j < is.items.size(); j++) {
				item = is.items.get(j);
				if(item.item.id == itemId){
					j = is.items.size();
					i = ishops.size();
				}
			}
		}
		int price = 1;
		if(item != null){
			for (Shop.BuyRequirement req : item.requirements) {
				if(req.type == Shop.TYPE_IMONEY){
					price = req.amount;
					break;
				}
			}
		}
		return price;
	}
	
	public synchronized static void checkConfigPacket() {
		if (pt == null || (System.currentTimeMillis() - lastBuildConfigTime > 60000L)) {
		    lastBuildConfigTime = System.currentTimeMillis();
			loadPacket();
		}
	}
	
	public static void loadPacket(){
		try {
		    pt = null;
		    Packet packet = new Packet(OpCode.DECORATE_GET_CONFIG_SERVER);
			
			JewelService js = Server.server.getServiceRegistry().getJewelService();
	        // 宝石各级别的合成、镶嵌、移除配置
			packet.put(JewelService.JEWEL_LEVELS);
	        for (int i = 0; i < JewelService.JEWEL_LEVELS; i++) {
	            packet.put(JewelService.MERGE_3_SUCC);
	            packet.put(JewelService.MERGE_4_SUCC);
	            packet.put(JewelService.MERGE_5_SUCC);
	            packet.putInt(js.getMergePrice(i + 1));
	            ItemTemplate it = js.mergeItems[i];
	            packet.putInt(it.id);
	            packet.put(it.showType);
	            packet.putString(it.name);
	            packet.putInt(getItemPrice(it.id));
	            packet.putInt(js.getDecoratePrice(i + 1));
	            packet.putInt(js.getRemovePrice(i + 1));
	            it = js.removeItems[i];
	            packet.putInt(it.id);
	            packet.put(it.showType);
	            packet.putString(it.name);
	            packet.putInt(getItemPrice(it.id));
	        }
	        
	        // 镶嵌配置
	        packet.put(JewelService.DECO_SUCC_RATE1);
	        packet.putInt(js.deocrateItem1.id);
	        packet.put(js.deocrateItem1.showType);
	        packet.putString(js.deocrateItem1.name);
	        packet.putInt(getItemPrice(js.deocrateItem1.id));
	        packet.put(JewelService.DECO_SUCC_RATE2);
	        packet.putInt(js.deocrateItem2.id);
	        packet.put(js.deocrateItem2.showType);
	        packet.putString(js.deocrateItem2.name);
	        packet.putInt(getItemPrice(js.deocrateItem2.id));
	        packet.put(JewelService.DECO_SUCC_RATE3);
	        
	        // 打孔配置
	        packet.put(js.addHoleItemIDs.length);
	        for (int i = 0; i < js.addHoleItemIDs.length; i++) {
	            packet.put(js.addHoleItemIDs[i][0]);
	            ItemTemplate it = js.addHoleItems[i];
	            packet.putInt(it.id);
	            packet.put(it.showType);
	            packet.putString(it.name);
	            packet.putInt(getItemPrice(it.id));
	        }
	        
	        //星级鉴定符的价格
	    	ITEM_STAR_ENHANCE_LEVEL1_PRICE = getItemPrice(ItemUtil.ITEM_STAR_ENHANCE_LEVEL1);
	    	ITEM_STAR_ENHANCE_LEVEL2_PRICE = getItemPrice(ItemUtil.ITEM_STAR_ENHANCE_LEVEL2);
	    	ITEM_STAR_ENHANCE_LEVEL3_PRICE = getItemPrice(ItemUtil.ITEM_STAR_ENHANCE_LEVEL3);
	    	
	    	ITEM_NATURAL_ENHANCE_PRICE = getItemPrice(ItemUtil.ITEM_NATURAL_ENHANCE);
	    	
	    	pt = packet;
		} catch (Exception e) {
		    pt = null;
			log.error(e, e);
		}
	}
	
	public void callFinish() {
        session.send(pt);
	}

	public void run() {
		checkConfigPacket();
		addToClientSession();
	}
}
