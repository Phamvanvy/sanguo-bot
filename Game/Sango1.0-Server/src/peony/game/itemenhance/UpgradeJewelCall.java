package peony.game.itemenhance;

import java.text.DecimalFormat;

import org.apache.log4j.Logger;

import peony.common.SyncIbuyCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.Horse;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.attendant.Attendant;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.ShopService;

import com.pip.sanguo.data.item.Item;

/**
 * 镶嵌宝石升级
 * @author dchen
 */
public class UpgradeJewelCall extends SyncIbuyCall {

	protected final Logger log = Logger.getLogger(UpgradeJewelCall.class);
	protected int serial;
	protected Player player;
    protected int equItemID;
	protected int equInstanceID;
	protected int hole;
	protected int reqType;
	protected ItemEnhance itemEnh;
	public static int RESITEM = 1336;	 //低级宝石合成符
	protected static int DECITEM = 1337; //高级宝石合成符
	protected static int ITEM_JEWEL_LV3 = 1353; //3级力量宝石
	
	public UpgradeJewelCall(ClientSession session, Packet packet) {
		super(session, packet);
		this.serial = packet.getInt();
		this.player = (Player)session.getClient();
		this.equItemID = packet.getInt();
		this.equInstanceID = packet.getInt();
		this.hole = packet.getByte() & 0xFF;
		this.reqType = packet.getByte() & 0xFF;
	}

	public void callFinish() throws Exception {
		try {
			if(reqType == 1){	//请求价格
				getShopItemPrice();
				return;
			}else{
				go();
			}
		} catch (Exception e) {
			error(e.getMessage());
		}
		if(player!=null){
			if(success){
				Packet pt = new Packet(OpCode.DECORATE_UPGRADE_JEWEL_SERVER);
				pt.putInt(serial);
				pt.put(0);
				pt.put(itemEnh.toClientBytes());
				player.send(pt);
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.DECORATE_UPGRADE_JEWEL_CLIENT, errorMessage);
			}
		}
	}

	public void run() {
		addToClientSession();
	}
	
	protected void go() throws Exception {
		if(player!=null){
			// 在背包中查找目标装备
		    Object[] obj = ItemUtil.findPlayerEquipment(player, equItemID, equInstanceID);
		    if (obj == null)
		        throw new Exception(peony.Messages.STRING_00015);
		    GameItem gi = (GameItem)obj[0];
		    Object giOwner = obj[1];
		    if (gi.template.equipment == null)
		        throw new Exception(peony.Messages.STRING_01949);
		    if (gi.object == null)
		        gi.object = new ItemEnhance();
		    if (!(gi.object instanceof ItemEnhance))
		        throw new Exception(peony.Messages.STRING_00017);
		    
		 // 检查孔位是否合法，宝石是否正确（同一类的宝石只能镶1个），坐骑宝石不能镶嵌给人物装备
		    itemEnh = (ItemEnhance)gi.object;
		    ItemTemplate jewelTemp = ObjectAccessor.getItemTemplate(itemEnh.getJewel(hole));
		    if (jewelTemp == null || jewelTemp.itemType != Item.TYPE_JEWEL)
		        throw new Exception(peony.Messages.STRING_00060);
		    if (hole < 0 || hole >= itemEnh.addHole + gi.template.equipment.initHole)
		        throw new Exception(peony.Messages.STRING_01082);
		    if (itemEnh.getJewel(hole) == -1)
		        throw new Exception(peony.Messages.STRING_01950);
		    if(jewelTemp.useLevel<5)
		    	throw new Exception(peony.Messages.STRING_01951);
		    if(jewelTemp.useLevel>=7)
		    	throw new Exception(peony.Messages.STRING_01952);
		    
		    int decJewelId = jewelTemp.id;
		    try {
				LogUtil.logUpgradeJewelTry(player, gi, decJewelId, hole, 0);
			} catch (Exception e) {
			}
		    PlayerTransaction tx = player.newTransaction("JEWELUPGRADE");
		    GameItem decItem = player.bag.removeGameItemIngoreInstanceId(decJewelId, 1, tx, false);
		    GameItem decItem1 = player.bag.removeGameItemIngoreInstanceId(DECITEM, 1, tx, false);
		    
		    if(decItem!=null && decItem1!=null){
		    	tx.commit();
		    }else{
		    	float price = getNeedPrice(jewelTemp);
		    	if(price*100>player.getAccount().getLongIMoney()){
		    		tx.rollback();
		    		try {
						LogUtil.logUpgradeJewelOK(player, gi, decJewelId, hole, 0, false);
					} catch (Exception e1) {
					}
		    		throw new Exception(peony.Messages.STRING_00924);
		    	}else{
		    		ShopService service = Server.server.getServiceRegistry().getShopService();
		    		if(decItem1 == null){	//缺少宝石 合成符
				    	try {
							waitBuy(player, 0, service.getShopByItemId(DECITEM).id, DECITEM, 1, this);
						} catch (Exception e) {
							tx.rollback();
							return;
						}
				    }
		    		
		    		if(decItem == null){	//缺少宝石
		    			int itemNums = 1;	//3级宝石数量
		    			if(jewelTemp.useLevel > 3){
		    				int len = jewelTemp.useLevel - 3;
		    				for(int i=0; i<len; i++){
		    					itemNums = itemNums*5;
		    				}
		    			}
		    			
			    		//3级合成4级*5 --低级宝石合成符
		    			int resItemCnt = itemNums/5;
		    			try {
							waitBuy(player, 0, service.getShopByItemId(RESITEM).id, RESITEM, resItemCnt, this);
						} catch (Exception e) {
							tx.rollback();
							return;
						}
						
						//4级合成5级 --高级宝石合成符
						
						int jewelLv4Cnt = itemNums/25;
						while(jewelLv4Cnt > 0){
							try {
								waitBuy(player, 0, service.getShopByItemId(DECITEM).id, DECITEM, jewelLv4Cnt, this);
							} catch (Exception e) {
								tx.rollback();
								return;
							}
							jewelLv4Cnt = jewelLv4Cnt/5;
						}
						
				    	try {	//3级力量宝石
							waitBuy(player, 0, service.getShopByItemId(ITEM_JEWEL_LV3).id, ITEM_JEWEL_LV3, itemNums, this);
						} catch (Exception e) {
							tx.rollback();
							return;
						}
				    }
		    		tx.commit();
		    	}
		    }
		    
		    itemEnh.upgradeJewel(player,gi,hole);
		    
		 // 如果当前装备的物品被修改，刷新人物属性
	        if (giOwner instanceof Player) {
	            player.refreshProperties(false);
	        } else if (giOwner instanceof Horse) {
	            // 如果马的装备物品被修改，刷新马的属性；如果这个马当前被装备，还需要刷新人的属性
	            Horse h = (Horse)giOwner;
	            h.refreshProperties(false, player);
	            if (h == player.horse) {
	                player.refreshProperties(false);
	            }
	        } else if(giOwner instanceof Attendant){
	        	((Attendant) giOwner).refreshProperties(false);
	        }
	        try {
				LogUtil.logUpgradeJewelOK(player, gi, decJewelId, hole, 0, true);
			} catch (Exception e) {
			}
		}
	}
	
	protected void getShopItemPrice() throws Exception {
		if(player != null){
			// 在背包中查找目标装备
		    Object[] obj = ItemUtil.findPlayerEquipment(player, equItemID, equInstanceID);
		    if (obj == null)
		        throw new Exception(peony.Messages.STRING_00015);
		    GameItem gi = (GameItem)obj[0];
		    Object giOwner = obj[1];
		    if (gi.template.equipment == null)
		        throw new Exception(peony.Messages.STRING_01949);
		    if (gi.object == null)
		        gi.object = new ItemEnhance();
		    if (!(gi.object instanceof ItemEnhance))
		        throw new Exception(peony.Messages.STRING_00017);
		    
		 // 检查孔位是否合法，宝石是否正确（同一类的宝石只能镶1个），坐骑宝石不能镶嵌给人物装备
		    itemEnh = (ItemEnhance)gi.object;
		    ItemTemplate jewelTemp = ObjectAccessor.getItemTemplate(itemEnh.getJewel(hole));
		    if (jewelTemp == null || jewelTemp.itemType != Item.TYPE_JEWEL)
		        throw new Exception(peony.Messages.STRING_00060);
		    if (hole < 0 || hole >= itemEnh.addHole + gi.template.equipment.initHole)
		        throw new Exception(peony.Messages.STRING_01082);
		    if (itemEnh.getJewel(hole) == -1)
		        throw new Exception(peony.Messages.STRING_01950);
		    if(jewelTemp.useLevel<5)
		    	throw new Exception(peony.Messages.STRING_01951);
		    if(jewelTemp.useLevel>=7)
		    	throw new Exception(peony.Messages.STRING_01952);
		    
		    float price = getNeedPrice(jewelTemp);

			DecimalFormat df = new DecimalFormat("0.00");
			String showPrice = df.format(price);
			Packet pt = new Packet(OpCode.DECORATE_UPGRADE_JEWEL_SERVER);
			pt.putInt(serial);
			pt.put(1);
			pt.putString(showPrice);
			player.send(pt);
		}
	}
	
	public float getNeedPrice(ItemTemplate jewelTemp){
		int decJewelId = jewelTemp.id;
		GameItem itemFu = player.bag.getGameItem(DECITEM);	//高级宝石合成符
	    GameItem itemJewel = player.bag.getGameItem(decJewelId); //宝石
	    ShopService service = Server.server.getServiceRegistry().getShopService();
		float price = 0;
	    
    	float priceDecItem = 0;
    	try {
    		priceDecItem = service.getItemPriceInAppointShop(DECITEM, -1);
		} catch (Exception e) {
			priceDecItem = service.getFilterItemPrice(DECITEM);
		}
		if(itemFu == null){	//高级宝石合成符
			price += priceDecItem;
	    }
	    
	    if(itemJewel == null){	//宝石价格 3级宝石为准
	    	int itemNums = 1;//物品数量
			if(jewelTemp.useLevel > 3){
				int len = jewelTemp.useLevel - 3;
				for(int i=0; i<len; i++){
					itemNums = itemNums*5;
				}
				
				float priceTemp = 0;
		    	try {
		    		priceTemp = itemNums * service.getItemPriceInAppointShop(ITEM_JEWEL_LV3, -1); //3级力量宝石
				} catch (Exception e) {
					priceTemp = itemNums * service.getFilterItemPrice(ITEM_JEWEL_LV3);
				}
				price += priceTemp;
				
				//3级合成4级*5 --低级宝石合成符
				float priceResItem = 0;
		    	try {
		    		priceResItem = service.getItemPriceInAppointShop(RESITEM, -1);
				} catch (Exception e) {
					priceResItem = service.getFilterItemPrice(RESITEM);
				}
				price += itemNums/5 * priceResItem;
				
				//4级以上合成 --高级宝石合成符
				int jewelLv4Cnt = itemNums/25;
				while(jewelLv4Cnt > 0){
					price += jewelLv4Cnt * priceDecItem;
					jewelLv4Cnt = jewelLv4Cnt/5;
				}
			}
	    }
	    return price;
	}

}
