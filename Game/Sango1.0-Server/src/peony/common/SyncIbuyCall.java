package peony.common;

import java.util.ArrayList;
import java.util.List;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.ShopException;
import peony.service.shop.SyncShopItemBuy;

/**
 * 同步i币购买call。调用waitBuy（）方法后线程会等待，直到购买结果返回。购买不成功会抛出异常。
 * @author dchen
 */
public abstract class SyncIbuyCall extends ClientSessionAsyncCall {

	protected List<SyncShopItemBuy> ibuys = new ArrayList<SyncShopItemBuy>();
	
	public SyncIbuyCall(ClientSession session, Packet packet) {
		super(session);
	}
	
	private SyncShopItemBuy sendSyncShopItemBuy(Player player, int serial, int shopID, int itemID, 
			int count, SyncIbuyCall call) throws ShopException {
		SyncShopItemBuy ibuy = new SyncShopItemBuy(player, serial, shopID, itemID, count, call);
		ibuys.add(ibuy);
		Server.server.getServiceRegistry().getShopService().buy(player, ibuy);
		return ibuy;
	}
	
	protected void ibuyRollBack(){
		for(SyncShopItemBuy ibuy : ibuys){
			ibuy.addGameItemToPlayer();
		}
	}
	
	/** 同步购买一个商品。
	 * @param player,购买者
	 * @param serial,购买call的序列号
	 * @param shopID,商店ID
	 * @param itemID,购买物品ID
	 * @param count,购买物品数量
	 * @param call,同步执行call，回调用
	 * @throws Exception
	 */
	protected void waitBuy(Player player, int serial, int shopID, int itemID, 
			int count, SyncIbuyCall call) throws Exception{
		SyncShopItemBuy ibuy = sendSyncShopItemBuy(player, serial, shopID, itemID, count, call);
		if(!ibuy.received){
			synchronized (this) {
				try {
					wait(20000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if (!ibuy.received) {
			// 如果15秒超时没有收到返回信息，认为扣费失败
			call.error(peony.Messages.STRING_00405);
		}
		if(!success){
			ibuyRollBack();
			throw new Exception("");
		}
	}

}
