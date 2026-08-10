package peony.service.shop;

import org.apache.log4j.Logger;
import peony.common.SyncIbuyCall;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;

/**
 * 同步购买项(需要跟一个SyncIbuyCall联合使用)
 * @author dchen
 */
public class SyncShopItemBuy extends ShopItemBuy {

	private static final Logger log = Logger.getLogger(ShopItemBuy.class);
	
    Player player;
    
    protected SyncIbuyCall call;
    
    protected GameItem item;
    
    public boolean received;
    
    public SyncShopItemBuy(Player player, int serial, int shopID, int itemID, int count, SyncIbuyCall call) throws ShopException {
    	super(player, serial, shopID, itemID, count);
    	this.player = player;
        this.call = call;
        log.info("[DEBUG][SYNCBUY]REQ["+this+"]CREATE");
    }
    
    public void receive(PlayerTransaction tx, boolean supportMail) throws ShopException {
    	log.info("[DEBUG][SYNCBUY]REQ["+this+"]RECEIVE");
    	received = true;
    	item = ObjectAccessor.createGameItem(buyItem.item.id);
    	synchronized (call) {
			call.notify();
		}
    }
    
    public void rollback() {
    	log.info("[DEBUG][SYNCBUY]REQ["+this+"]ROLLBACK");
        super.rollback();
        synchronized (call) {
        	call.error(peony.Messages.STRING_00405);
        	received = true;
            call.notify();
		}
    }
    
    public void addGameItemToPlayer(){
    	try {
    		if(call.success)
    			super.receive(player.newTransaction("BUY"), true);
		} catch (ShopException e) {
			log.error(e, e);
		}
    }

}
