package peony.service.shop;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

import com.pip.sanguo.data.Shop;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.OpCode;
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
public class QuickBuyCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(QuickBuyCall.class);
	protected int serial;
	protected int shopID;
	protected int itemID;
	protected short count;
	
	private static final Map<Integer,Integer> cache = new ConcurrentHashMap<Integer,Integer>();

	public QuickBuyCall(ClientSession session, Packet packet) {
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
            	ErrorHandler.sendErrorMessage(session, serial,
                        OpCode.SHOP_QUICK_BUY_CLIENT, "不合法的购买数量");
                log.error("[SHOPATTACK]" + LogUtil.getPlayerLogString(player));
                return;
            }
            ShopService service = Server.server.getServiceRegistry()
			.getShopService();
            //修正商店ID(商店ID可能因商品转移到另一个商店而与客户端又没有及时更正而不正确)
            Integer cacheId = cache.get(itemID);
            if(cacheId != null){
            	shopID = cacheId.intValue();
            } else {
                Shop shop = service.findShop(shopID);
                int shopId = -1;
                if(shop != null){
                	if(service.hasItemInShop(shop, itemID)){
                		shopId = shop.id;
                	} else {
                		shopId = (service.getShopByItemId(itemID)==null) ? -1 : (service.getShopByItemId(itemID).id);
                	}
                } else {
                	shopId = (service.getShopByItemId(itemID)==null) ? -1 : (service.getShopByItemId(itemID).id);
                }
            		
        		if(shopId == -1){
        			ErrorHandler.sendErrorMessage(session, serial,
                            OpCode.SHOP_QUICK_BUY_CLIENT, peony.Messages.STRING_01735);
        			return;
        		} else {
        			cache.put(itemID, shopId);
        			shopID = shopId;
        		}
            }
            
            LogUtil.logShopBuyTry(player, shopID, itemID, count);
			
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
	
//	public static boolean hasItemInShop(Shop shop,int itemId){
//		boolean found = false;
//        for (Shop.ShopItem item : shop.items) {
//			if(item.item.id == itemId){
//				found = true;
//				break;
//			}
//		}
//		return found;
//	}
//	
//	public static int hasItemInShop(int itemId){
//		DataService ds = Server.server.getServiceRegistry().getDataService();
//		List<DataObject> list = ds.data.getDataListByType(Shop.class);
//		int shopId = -1;
//		for (DataObject sobj : list) {
//			Shop sp = (Shop)sobj;
//			if(hasItemInShop(sp, itemId)){
//				shopId = sp.id;
//				break;
//			}
//		}
//		return shopId;
//	}
}
