package peony.service.shop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import peony.game.DataService;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.service.Service;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Shop;

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
	
	public void startup() throws Exception {
		active = true;
		new Thread(this).start();
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
            buyObject.notifyClient(false, "系統錯誤");
            throw new ShopException("系統錯誤");
        }
    }
	
	/**
	 * 开始一个异步i币支付。
	 * @param amount i币金额
	 * @param callback 请求对象
	 */
	void asyncPay(int accountId, int amount, String consumeCode, BuyRequest callback, int level) {
		// 向认证服务器请求扣费
		Server.server.getServiceRegistry().getAccountService().schedule(new IMoneyBuyCall(accountId, amount, consumeCode, callback, level));
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
			req.buyObject.notifyClient(false, cause == null ? "扣費失敗" : cause);
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
					processBuyResult(id, false, "請求超時");
				}
			} catch (Exception e) {
				log.error(e, e);
			}
		}
	}
}
