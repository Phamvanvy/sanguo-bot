package peony.service.shop;

import org.apache.log4j.Logger;
import peony.game.Player;
import peony.game.PlayerTransaction;

/**
 * 通过购买物品扣ib
 * @author pmeng
 */

public class NoItemShopBuy extends ShopItemBuy{
	
	private static final Logger log = Logger.getLogger(NoItemShopBuy.class);
	
	/*
	 *需要传入NoItemIbuy接口实现process方法特殊处理消费逻辑 
	 */
	protected NoItemShopBuyI ibuy;
    
	/** 一元宝物品Id */
    public static int YIYUANBAO = 3476;
    /** 二元宝物品Id */
    public static int LIANGYUANBAO = 3477;
    /** 五元宝物品Id */
    public static int WUYUANBAO = 3478;
    /** 十元宝物品Id */
    public static int SHIYUANBAO = 3479;
    
    Object[] obj;
	
	public NoItemShopBuy(Player player, int serial, int shopID, int itemID,
			int count, NoItemShopBuyI ibuy, Object[] objects) throws ShopException {
		super(player, serial, shopID, itemID, count);
		this.obj = objects;
		this.ibuy = ibuy;
	}

	public void receive(PlayerTransaction tx, boolean supportMail)
			throws ShopException {
		if(ibuy!=null)
			ibuy.process(obj);
	}

	public void rollback() {
		super.rollback();
		if(ibuy!=null)
			ibuy.procssFail(null);
	}
	
}
