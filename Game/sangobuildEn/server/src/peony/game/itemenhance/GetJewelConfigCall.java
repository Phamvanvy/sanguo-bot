package peony.game.itemenhance;

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
	protected final Logger log = Logger.getLogger(GetJewelConfigCall.class);
	protected Player player;
	
	private static Packet pt = new Packet(OpCode.DECORATE_GET_CONFIG_SERVER);
	public static int ITEM_STAR_ENHANCE_LEVEL1_PRICE;
	public static int ITEM_STAR_ENHANCE_LEVEL2_PRICE;
	public static int ITEM_STAR_ENHANCE_LEVEL3_PRICE;
	public static int ITEM_NATURAL_ENHANCE_PRICE;
	static {
	    JewelService js = Server.server.getServiceRegistry().getJewelService();
	    ShopService ss = Server.server.getServiceRegistry().getShopService();
	    //装备相关商店
	    Shop shop;
	    if("CMCC".equals(Server.server.revision)){//移动
	    	shop = ss.findShop(49);
	    } else if("CHINATEL".equals(Server.server.revision)){//电信
	    	shop = ss.findShop(25);
	    } else if("TAIWAN".equals(Server.server.revision)){//台湾
	    	shop = ss.findShop(54);
	    } else {//PiP
	    	shop = ss.findShop(20);
	    }
	    
        // 宝石各级别的合成、镶嵌、移除配置
        pt.put(JewelService.JEWEL_LEVELS);
        for (int i = 0; i < JewelService.JEWEL_LEVELS; i++) {
            pt.put(JewelService.MERGE_3_SUCC);
            pt.put(JewelService.MERGE_4_SUCC);
            pt.put(JewelService.MERGE_5_SUCC);
            pt.putInt(js.getMergePrice(i + 1));
            ItemTemplate it = js.mergeItems[i];
            pt.putInt(it.id);
            pt.put(it.showType);
            pt.putString(it.name);
            pt.putInt(getItemPrice(shop, it.id));
            pt.putInt(js.getDecoratePrice(i + 1));
            pt.putInt(js.getRemovePrice(i + 1));
            it = js.removeItems[i];
            pt.putInt(it.id);
            pt.put(it.showType);
            pt.putString(it.name);
            pt.putInt(getItemPrice(shop, it.id));
        }
        
        // 镶嵌配置
        pt.put(JewelService.DECO_SUCC_RATE1);
        pt.putInt(js.deocrateItem1.id);
        pt.put(js.deocrateItem1.showType);
        pt.putString(js.deocrateItem1.name);
        pt.putInt(getItemPrice(shop, js.deocrateItem1.id));
        pt.put(JewelService.DECO_SUCC_RATE2);
        pt.putInt(js.deocrateItem2.id);
        pt.put(js.deocrateItem2.showType);
        pt.putString(js.deocrateItem2.name);
        pt.putInt(getItemPrice(shop, js.deocrateItem2.id));
        pt.put(JewelService.DECO_SUCC_RATE3);
        
        // 打孔配置
        pt.put(js.addHoleItemIDs.length);
        for (int i = 0; i < js.addHoleItemIDs.length; i++) {
            pt.put(js.addHoleItemIDs[i][0]);
            ItemTemplate it = js.addHoleItems[i];
            pt.putInt(it.id);
            pt.put(it.showType);
            pt.putString(it.name);
            pt.putInt(getItemPrice(shop, it.id));
        }
        
        //星级鉴定符的价格
    	ITEM_STAR_ENHANCE_LEVEL1_PRICE = getItemPrice(shop, ItemUtil.ITEM_STAR_ENHANCE_LEVEL1);
    	ITEM_STAR_ENHANCE_LEVEL2_PRICE = getItemPrice(shop, ItemUtil.ITEM_STAR_ENHANCE_LEVEL2);
    	ITEM_STAR_ENHANCE_LEVEL3_PRICE = getItemPrice(shop, ItemUtil.ITEM_STAR_ENHANCE_LEVEL3);
    	
    	//资质鉴定符的价格
	    if("CMCC".equals(Server.server.revision)){//移动
	    	shop = ss.findShop(51);
	    } else if("CHINATEL".equals(Server.server.revision)){//电信
	    	shop = ss.findShop(46);
	    } else if("TAIWAN".equals(Server.server.revision)){//台湾
	    	shop = ss.findShop(56);
	    } else {//PiP
	    	shop = ss.findShop(21);
	    }
    	ITEM_NATURAL_ENHANCE_PRICE = getItemPrice(shop, ItemUtil.ITEM_NATURAL_ENHANCE);
	}

	public GetJewelConfigCall(ClientSession session, Packet packet) {
		super(session);
		this.player = (Player)session.getClient();
	}
	
	private static int getItemPrice(Shop shop,int itemId){
		int price = 1;
		Shop.ShopItem item = null;
		for (Shop.ShopItem it : shop.items) {
			if(it.item.id == itemId){
				item = it;
				break;
			}
		}
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
	
	public void callFinish() {
        session.send(pt);
	}

	public void run() {
		addToClientSession();
	}
}
