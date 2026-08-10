package peony.decimoney;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.service.shop.ShopException;
import peony.service.shop.ShopService;

public class DecImoneyEffect implements ItemEffect{
	
    protected int price;
    
    public DecImoneyEffect(int value){
    	this.price = value;
    }
    
	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException{
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("错误的目标");
		if(!(target instanceof Player))
			throw new UseItemException("错误的目标");
		Player p = (Player)source;
		if(p!=null){
			ShopService shopService = Server.server.getServiceRegistry().getShopService();
			try {
				DecImoneyBuy dib = new DecImoneyBuy(p,price);
				shopService.buy(p, dib);
			} catch (ShopException e) {
				throw new UseItemException("Số dư xu của bạn không đủ");
			}
		}
	}
	
	public boolean isAsync() {
		return false;
	}

}
