package peony.common;

import java.util.ArrayList;
import java.util.List;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.ShopException;
import peony.service.shop.SyncShopItemBuy;

public abstract class SyncIbuyCall extends ClientSessionAsyncCall {

	protected List<SyncShopItemBuy> ibuys = new ArrayList<SyncShopItemBuy>();
	
	public SyncIbuyCall(ClientSession session, Packet packet) {
		super(session);
	}
	
	protected SyncShopItemBuy syncShopItemBuy(Player player, int serial, int shopID, int itemID, 
			int count, ClientSessionAsyncCall call) throws ShopException {
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
	
	/** 同步购买一个商品。*/
	protected void waitBuy(SyncShopItemBuy ibuy) throws Exception{
		while(!ibuy.received){
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if(!success){
			ibuyRollBack();
			throw new Exception("");
		}
	}

}
