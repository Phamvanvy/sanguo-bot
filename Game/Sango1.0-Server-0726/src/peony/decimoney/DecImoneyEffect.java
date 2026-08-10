package peony.decimoney;

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

public class DecImoneyEffect implements ItemEffect, NoItemShopBuyI{
	
    protected int itemId;
    protected Player player;
    protected boolean buyOk;
    protected String failMessage;
    private static final Logger log = Logger.getLogger(DecImoneyEffect.class);
    public long beginTime;
    
    public DecImoneyEffect(int itemId){
    	this.itemId = itemId;
    }
    
	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException{
		synchronized (this) {
			if(!ItemUtil.checkUseTarget(source, item, target))
				throw new UseItemException("错误的目标");
			if(!(target instanceof Player))
				throw new UseItemException("错误的目标");
			Player p = (Player)source;
			this.player = p;
			if(p!=null){
				ShopService shopService = Server.server.getServiceRegistry().getShopService();
				int price = Math.round(shopService.getItemPrice(itemId));
				if(price<=0)
					throw new UseItemException("物品配置错误，请稍后再使用");
				if(SyncExecutorService.autoMergerAndRemoveFlag==0){
					try {
						DecImoneyBuy dib = new DecImoneyBuy(p,price);
						shopService.buy(p, dib);
					} catch (ShopException e) {
						throw new UseItemException("您的元宝余额不足");
					}
				}else{
					try {
						int shopId = shopService.getShopByItemId(itemId).id;
						shopService.buy(p, new NoItemShopBuy(p,0,shopId,itemId,1,this,null));
						beginTime = System.currentTimeMillis();
						while(true){
							if(buyOk)
								break;
							if(failMessage!=null && !failMessage.equals("")){
								String mess = failMessage;
								clear();
								throw new UseItemException(mess);
							}
							if(System.currentTimeMillis()-beginTime>240000){
								log.info("[DECIMONEYOUT]"+LogUtil.getPlayerLogString(p)+"PRICEITEM["+itemId+"]");
								throw new UseItemException("扣费失败，请稍后再试");
							}
						}
						clear();
					} catch (Exception e) {
						throw new UseItemException("扣费失败，请稍后再试");
					}
				}
			}
		}
	}
	
	protected void clear(){
		buyOk = false;
		failMessage = null;
		beginTime = 0;
	}
	
	public boolean isAsync() {
		return true;
	}

	public void process(Object[] o) {
		this.buyOk = true;
	}

	public void procssFail(Object[] o) {
		failMessage = "您的元宝余额不足";
	}

}
