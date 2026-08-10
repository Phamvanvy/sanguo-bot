package peony.service.shop;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import peony.db.IBuyDAO;
import peony.game.DataService;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.service.Service;
import peony.util.TimeUtil;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.Shop.ShopItem;

/**
 * 商店服务，包括普通金钱商店、i币商店和荣誉商店。
 * @author lighthu
 */
public class ShopService implements Service, Runnable {
	
	public static int CMCC_SHOP = 61;
	
	private static Logger log = Logger.getLogger(ShopService.class);
	protected boolean active = false;
	protected AtomicInteger buyIDGen = new AtomicInteger(1);
	/*
	 * 需要i币交易的商品
	 */
	protected Map<Integer, BuyRequest> pendingRequests = new ConcurrentHashMap<Integer, BuyRequest>();
	protected static final int TIMEOUT = 10000000;
	
	public List<ShopItem> topShopItems = new ArrayList<ShopItem>(); //最畅销购买项
	public static int[] initShopItems = {1341,1582,1578,821,1340,1343,1339,1579,1183,1346,1580,1395,857,1458,1374,1338,1165,1430,1367,1134}; //初始化最畅销购买项
	List<Integer> IshopList = new ArrayList<Integer>();
	private List<Integer> effectIshopList = new ArrayList<Integer>(); //取价格用的商店
	
	/** 商店整体打折数据 */
	public List<ShopDiscount> shopDiscounts = new ArrayList<ShopDiscount>();
	
	public void startup() throws Exception {
		active = true;
		new Thread(this).start();
		initIshops();
		loadTopShopItems();
	}
	
	protected void initIshops(){
		List<Integer> list = new ArrayList<Integer>();
		if (Server.server.REVISION_TYPE_CMCC.equals(Server.server.revision)||Server.server.REVISION_TYPE_TEL.equals(Server.server.revision)) {
			list.add(47);
			list.add(48);
			list.add(49);
			list.add(50);
			list.add(51);
			list.add(110);
			effectIshopList.add(110);
		}else if(Server.server.REVISION_TYPE_TW.equals(Server.server.revision)){
			list.add(52);
			list.add(53);
			list.add(54);
			list.add(55);
			list.add(56);
			list.add(111);
			effectIshopList.add(111);
		} else if(Server.server.REVISION_TYPE_KO.equals(Server.server.revision)){
			list.add(186);
			list.add(139);
			list.add(140);
			list.add(141);
			list.add(142);
			list.add(187);
			list.add(109);
			effectIshopList.add(109);  // 韩国价格和国服相同，用国服自动购买商店
		} else if(Server.server.REVISION_TYPE_JAPAN.equals(Server.server.revision)){
			list.add(184);
			list.add(180);
			list.add(181);
			list.add(182);
			list.add(183);
			list.add(188);
			list.add(109);
			effectIshopList.add(109);  // 日本价格和国服相同，用国服自动购买商店
		}else {
			list.add(21);
			list.add(18);
			list.add(19);
			list.add(20);
			list.add(17);
			list.add(69);
			list.add(109);
			effectIshopList.add(109);
		}
		IshopList = list;
	}
	
	protected void loadTopShopItems(){
		try {
			IBuyDAO dao = Server.server.getServiceRegistry().getDbService().ibuyDAO;
			List<Integer> topItemIds = dao.getTopItemConsume(TimeUtil.getDate(new Date(), 7*24*3600, false),new Date() , 20);
			for(int itemId : topItemIds){
				if(itemId==1163 || getShopItem(itemId, effectIshopList)!=null)
					continue;
				Shop shop = getShopByItemId(itemId);
				ShopItem shopItem = null;
				if(shop!=null && (shopItem=getShopItemByItemId(shop, itemId))!=null){
					topShopItems.add(shopItem);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		try {
			if(topShopItems.size()==0){
				for(int itemId : initShopItems){
					Shop shop = getShopByItemId(itemId);
					ShopItem shopItem = null;
					if(shop!=null && (shopItem=getShopItemByItemId(shop, itemId))!=null){
						topShopItems.add(shopItem);
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public void shutdown() {
		active = false;
	}
	
	/**
	 * 根据ID查找一个商店。
	 */
	public Shop findShop(int id) {
		DataService ds = Server.server.getServiceRegistry().getDataService();
		return (Shop)ds.data.findObject(Shop.class, id);
	}
	
	/**
	 * 取得一个商店中剩余的物品。
	 * @param shop
	 * @return
	 */
	public List<Shop.ShopItem> getShopItems(Shop shop) {
		List<Shop.ShopItem> ret = new ArrayList<Shop.ShopItem>();
		for (Shop.ShopItem item : shop.items) {
			if (item.count > 0 && item.remain == 0) {
				continue;
			}
			ret.add(item);
		}
		return ret;
	}
	
	/**
	 * 购买一个商品。如果购买成功，返回true；如果购买需要异步i币支付，返回false。
	 */
	public boolean buy(Player player, int serial, int shopID, int itemID, 
			int count) throws ShopException {
	    return buy(player, new ShopItemBuy(player, serial, shopID, itemID, count));
	}
	
	/**
     * 购买一个自定义的服务。例如驿站。
     */
    public boolean buy(Player player, IBuyObject buyObject) throws ShopException {
        BuyRequest req = new BuyRequest(this, buyIDGen.getAndIncrement(), player, buyObject);
        try {
            req.lock();
            if (!req.pay()) {
                // 需要i币支付，已加入支付队列
                return false;
            }
            req.receive(false);
            req.commit();
            buyObject.notifyClient(true, null);
            buyObject.log();
            return true;
        } catch (ShopException e) {
            req.rollback();
            buyObject.notifyClient(false, e.getMessage());
            throw e;
        } catch (Exception ee) {
            log.error(ee, ee);
            req.rollback();
            buyObject.notifyClient(false, "系统错误");
            throw new ShopException("系统错误");
        }
    }
	
	/**
	 * 开始一个异步i币支付。
	 * @param amount i币金额
	 * @param callback 请求对象
	 */
	void asyncPay(int accountId, int amount, String consumeCode, BuyRequest callback, int level, int faction) {
		// 向认证服务器请求扣费
		Server.server.getServiceRegistry().getAccountService().schedule(new IMoneyBuyCall(accountId, amount, consumeCode, callback, level, faction));
		pendingRequests.put(callback.buyID, callback);
	}
	
	/**
	 * 处理购买结果。
	 * @param id 购买ID
	 * @param succ 是否成功标志
	 */
	void processBuyResult(int id, boolean succ, String cause) {
		BuyRequest req = pendingRequests.remove(id);
		if (req == null) {
			return;
		}
		if (succ) {
			try {
				// 发货
				req.receive(true);
				req.commit();
				req.buyObject.notifyClient(true, null);
				req.buyObject.log();
			} catch (ShopException e) {
				log.error(e,e);
				// 发货失败
				req.rollback();
				req.buyObject.notifyClient(false, e.getMessage());
			}
		} else {
			// 购买失败
			req.rollback();
			req.buyObject.notifyClient(false, cause == null ? "扣费失败" : cause);
		}
	}
	
	/**
	 * 此线程定时刷新商品，检查购买超时。
	 */
	public void run() {
		while (active) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
			
			// 尝试刷新所有物品
			try {
				DataService ds = Server.server.getServiceRegistry().getDataService();
				List<DataObject> shops = ds.data.getDataListByType(Shop.class);
				for (DataObject dobj : shops) {
					Shop shop = (Shop)dobj;
					for (Shop.ShopItem item : shop.items) {
						item.update(Time.currTime);
					}
				}
			} catch (Exception e) {
				log.error(e, e);
			}
			
			// 检查所有i币购买请求是否超时
			try {
				Iterator<BuyRequest> itor = pendingRequests.values().iterator();
				List<Integer> timeouts = new ArrayList<Integer>();
				while (itor.hasNext()) {
					BuyRequest req = itor.next();
					if (Time.currTime - req.createTime > TIMEOUT) {
						timeouts.add(req.buyID);
					}
				}
				for (int id : timeouts) {
					processBuyResult(id, false, "请求超时");
				}
			} catch (Exception e) {
				log.error(e, e);
			}
		}
	}
	
	public ShopItem getShopItemByItemId(Shop shop, int itemId){
		for (Shop.ShopItem item : shop.items) {
			if(item.item.id == itemId){
				return item;
			}
		}
		return null;
	}
	
	public Shop getShopByItemId(int itemId){
		DataService ds = Server.server.getServiceRegistry().getDataService();
		List<DataObject> list = ds.data.getDataListByType(Shop.class);
		for (DataObject sobj : list) {
			Shop sp = (Shop)sobj;
			if(getIshops().contains(sp.id) && hasItemInShop(sp, itemId)){
				return sp;
			}
		}
		return null;
	}
	
	public boolean hasItemInShop(Shop shop,int itemId){
		boolean found = false;
        for (Shop.ShopItem item : shop.items) {
			if(item.item.id == itemId){
				found = true;
				break;
			}
		}
		return found;
	}
	
	public List<Integer> getIshops(){
		return IshopList;
	}
	
	/**
	 * 获取当前商店物品价格
	 * @param itemId
	 * @return
	 */
	public float getItemPrice(int itemId){
		List<Integer> ishops = effectIshopList;
		Shop.ShopItem item = null;
		item = getShopItem(itemId, ishops);
		if(item==null)
			item = getShopItem(itemId, IshopList);
		float price = 1f;
		if(item != null){
			for (Shop.BuyRequirement req : item.requirements) {
				if(req.type == Shop.TYPE_IMONEY){
					price = req.amount * item.discount / 100f;
					break;
				}
			}
		}
		return price;
	}
	
	protected ShopItem getShopItem(int itemId, List<Integer> ishops){
		for (int i = 0; i < ishops.size(); i++) {
			Shop is = findShop(ishops.get(i).intValue());
			if(is == null){
				continue;
			}
			
			for (int j = 0; j < is.items.size(); j++) {
				Shop.ShopItem item0 = is.items.get(j);
				if(item0!=null && item0.item.id == itemId){
					j = is.items.size();
					i = ishops.size();
					return item0;
				}
			}
		}
		return null;
	}
	
	/**
	 * 获取当前revision的商店的折扣率（乘100后的）
	 */
	public int getCurrentShopDiscount(){
		List<Integer> ishops = effectIshopList;
		Shop.ShopItem item = null;
		for (int i = 0; i < ishops.size(); i++) {
			Shop is = findShop(ishops.get(i).intValue());
			if(is == null){
				continue;
			}
			
			for (int j = 0; j < is.items.size(); j++) {
				item = is.items.get(j);
				if(item!=null){
					for (Shop.BuyRequirement req : item.requirements) {
						if(req.type == Shop.TYPE_IMONEY){
							return item.discount;
						}
					}
				}
			}
		}
		return 100;
	}
	
	public void addShopDiscount(int shopId, int buyObjectType, int discount){
		ShopDiscount shopDiscount = new ShopDiscount(shopId, buyObjectType, discount);
		shopDiscounts.add(shopDiscount);
	}
	
	public void removeShopDiscount(int shopId, int buyObjectType){
		Iterator<ShopDiscount> it = shopDiscounts.iterator();
		while(it.hasNext()){
			ShopDiscount sd = it.next();
			if(sd.shopId==shopId && sd.buyObjectType==buyObjectType)
				it.remove();
		}
	}
	
	public int getShopItemDiscount(int shopId, int buyObjectType){
		for(ShopDiscount sd : shopDiscounts){
			if(sd.shopId==shopId && sd.buyObjectType==buyObjectType)
				return sd.discount;
		}
		return 100;
	}
	
}

class ShopDiscount{
	
	public int shopId;
	public int buyObjectType;
	public int discount;
	
	public ShopDiscount(int shopId, int buyObjectType, int discount){
		this.shopId = shopId;
		this.buyObjectType = buyObjectType;
		this.discount = discount;
	}
	
}
