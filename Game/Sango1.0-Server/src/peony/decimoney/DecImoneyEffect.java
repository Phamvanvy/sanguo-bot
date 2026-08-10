package peony.decimoney;

import java.util.HashMap;
import org.apache.log4j.Logger;
import peony.db.SyncExecutorService;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.NoItemShopBuyI;
import peony.service.shop.ShopException;
import peony.service.shop.ShopService;
import peony.util.IntHashMap;

public class DecImoneyEffect implements ItemEffect, NoItemShopBuyI{
	
    protected int itemId;
    protected Player player;
    protected IntHashMap<Integer> buyOks = new IntHashMap<Integer>();
    protected HashMap<Integer, String> failMessages = new HashMap<Integer, String>();
    public HashMap<Integer, Long> beginTimes = new HashMap<Integer, Long>();
    
    private static final Logger log = Logger.getLogger(DecImoneyEffect.class);
    
    public DecImoneyEffect(int itemId){
    	this.itemId = itemId;
    }
    
	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException{
		synchronized (this) {
			if(!ItemUtil.checkUseTarget(source, item, target))
				throw new UseItemException(peony.Messages.STRING_00014);
			if(!(target instanceof Player))
				throw new UseItemException(peony.Messages.STRING_00014);
			Player p = (Player)source;
			this.player = p;
			if(p!=null){
				ShopService shopService = Server.server.getServiceRegistry().getShopService();
				int price = Math.round(shopService.getItemPrice(itemId));
				if(price<=0)
					throw new UseItemException(peony.Messages.STRING_01909);
				if(SyncExecutorService.async==0){
					try {
						DecImoneyBuy dib = new DecImoneyBuy(p,price);
						shopService.buy(p, dib);
					} catch (ShopException e) {
						throw new UseItemException(peony.Messages.STRING_01665);
					}
				}else{
					try {
						int shopId = shopService.getShopByItemId(itemId).id;
						log.info("[DEBUG][DECIMONEYEFFECT]ITEM["+itemId+"]HASHCODE["+this.hashCode()+"]TRY");
						shopService.buy(p, new NoItemShopBuy(p,0,shopId,itemId,1,this,new Object[]{source}));
						long beginTime = System.currentTimeMillis();
						beginTimes.put(source.id, beginTime);
						while(true){
							boolean buyOk = false;
							try{buyOk = buyOks.get(source.id)==1 ? true : false;}catch(Exception e){}
							if(buyOk){
								buyOks.remove(source.id);
								break;
							}
							if(failMessages.get(source.id)!=null && !failMessages.get(source.id).equals("")){
								String mess = failMessages.get(source.id);
								failMessages.remove(source.id);
								throw new UseItemException(mess);
							}
							if(beginTimes.get(source.id)!=null && System.currentTimeMillis()-beginTimes.get(source.id)>5000){
								log.info("[DECIMONEYOUT]"+LogUtil.getPlayerLogString(p)+"PRICEITEM["+itemId+"]");
								beginTimes.remove(source.id);
								throw new UseItemException(peony.Messages.STRING_01091);
							}
							try {
								Thread.sleep(10);
							} catch (InterruptedException ex) {
							}
						}
						clear(source.id);
					} catch (Exception e) {
						throw new UseItemException(peony.Messages.STRING_01091);
					}
				}
			}
		}
	}
	
	protected void clear(int sourceId){
		buyOks.remove(sourceId);
		failMessages.remove(sourceId);
		beginTimes.remove(sourceId);
	}
	
	public boolean isAsync() {
		return true;
	}

	public void process(Object[] o) {
		log.info("[DEBUG][DECIMONEYEFFECT]ITEM["+itemId+"]HASHCODE["+this.hashCode()+"]PROCESSBUYOK");
		if(o!=null){
			for(Object oo : o){
				if(oo!=null && oo instanceof Unit){
					int sourceId = ((Unit)oo).id;
					buyOks.put(sourceId, 1);
				}
			}
		}
	}

	public void procssFail(Object[] o) {
		log.info("[DEBUG][DECIMONEYEFFECT]ITEM["+itemId+"]HASHCODE["+this.hashCode()+"]PROCESSBUYFAIL");
		if(o!=null){
			for(Object oo : o){
				if(oo!=null && oo instanceof Unit){
					int sourceId = ((Unit)oo).id;
					failMessages.put(sourceId, peony.Messages.STRING_01665);
				}
			}
		}
	}

	public boolean needRemove() {
		return false;
	}

}
