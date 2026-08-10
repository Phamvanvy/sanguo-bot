package peony.game.itemenhance;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import peony.common.ClientSessionAsyncCall;
import peony.decimoney.DecImoneyBuy;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.Horse;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.attendant.Attendant;
import peony.game.mail.MailService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.account.Account;
import peony.service.shop.ShopException;
import peony.service.shop.ShopService;

public class RemoveAllJewelCall extends ClientSessionAsyncCall{

	protected int serial;
    protected int equItemID;
	protected int equInstanceID;
	protected Player player;
	protected ItemEnhance enhance;
	protected ItemTemplate removeItem;//购买的删除符
	
	public RemoveAllJewelCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.equItemID = packet.getInt(); 
		this.equInstanceID = packet.getInt();
	}

	public void callFinish() throws Exception {
		ShopService service = Server.server.getServiceRegistry().getShopService();
		if(success){
			player = (Player)session.getClient();
			if(player != null){
				Object[] obj = ItemUtil.findPlayerEquipment(player, equItemID,equInstanceID);
				GameItem gi = (GameItem) obj[0];
				LogUtil.logAutoRemoveJewelTry(player, gi);
				Object giOwner = obj[1];
				if (gi.template.equipment == null) {
			        throw new Exception(peony.Messages.STRING_00016);
			    }
			    if (gi.object == null) {
			        gi.object = new ItemEnhance();
			    }
			    if (!(gi.object instanceof ItemEnhance)) {
			        throw new Exception(peony.Messages.STRING_00017);
			    }
			    this.enhance = (ItemEnhance)gi.object;
				int jewelNum = enhance.getJewelCount();//宝石个数
				int[] jewelsId = new int[jewelNum]; 
				int[] holes = new int[jewelNum];
				JewelService js = Server.server.getServiceRegistry().getJewelService();
				for(int jj = 0; jj < jewelNum;jj ++){
					jewelsId[jj] = enhance.getJewelID(jj);
					holes[jj] = enhance.getJewelHole(jj);
				}
				PlayerTransaction tx = player.newTransaction("AUTORJE");
				int needImoney = 0;
				for(int j = 0; j < jewelNum; j ++){
					 // 扣除金钱、宝石摘除符
					ItemTemplate jewelTemp = ObjectAccessor.getItemTemplate(jewelsId[j]);
				    int lvl = jewelTemp.useLevel;
				    int needMoney = js.getRemovePrice(lvl);
				    try {
				        player.decMoney(needMoney, tx, true);
				    } catch (NoEnoughValueException ex) {
				        tx.rollback();
				        ErrorHandler.sendErrorMessage(session,  serial,OpCode.REMOVE_ALLJEWEL_RESOURCE_CLIENT,  peony.Messages.STRING_00158);
				        throw new Exception(peony.Messages.STRING_00158);
				    }
		    	    int canRemove = 0;//包中是否有相应删除符
				    int removeSuccess = 0;
			    	if(player.bag.getGameItemCount(js.removeItemIDs[jewelTemp.useLevel -1])!= 0){
			    		canRemove = 1;
			    	}
				    //若有 直接删了完事
				    if(canRemove == 1){
		    		    if (player.bag.removeGameItem(js.removeItemIDs[lvl - 1], GameItem.GENERAL_INSTANCEID, 1, tx, true) != null) {
		    			    removeSuccess = 1;
		    		    }
				    }
				    //若没有   进行购买
				    if(removeSuccess==0){
				    	//买摘除符
					    needImoney += service.getItemPrice(js.removeItemIDs[lvl - 1<2?2:lvl-1]);
					    if(needImoney>0){
							Account account = player.getAccount();
							long imoney = account.getLongIMoney();
							if(needImoney>imoney){
								tx.rollback();
								ErrorHandler.sendErrorMessage(session, serial,OpCode.DECORATE_REMOVE_ALLJEWEL_CLIENT, peony.Messages.STRING_00554);
								throw new Exception(peony.Messages.STRING_00554);
							}
						}
				    }
			       
				}
				try {
					DecImoneyBuy dib = new DecImoneyBuy(player,needImoney,"AUTORJE");
					service.buy(player, dib);
				} catch (ShopException e) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial, OpCode.DECORATE_REMOVE_ALLJEWEL_CLIENT, peony.Messages.STRING_00554);
					throw new Exception(peony.Messages.STRING_00554);
				}
				// 把摘除的宝石加入背包
				for(int jewelTemp : jewelsId){
					GameItem jewel = ObjectAccessor.createGameItem(jewelTemp);
				    if (!player.bag.addGameItem(jewel, 1, tx, true)) {
				    	//背包已满发飞鸽
				    	MailService ms = Server.server.getServiceRegistry().getMailService();
				    	if(ms.sendSystemMail(player.id, player.name, peony.Messages.STRING_01721, peony.Messages.STRING_01721, 0, jewel, 1, peony.Messages.STRING_00555) == -1){
				    		tx.rollback();
				    		throw new Exception(peony.Messages.STRING_01722);
				    	}
				    	ErrorHandler.sendErrorMessage(session, serial, OpCode.DECORATE_REMOVE_ALLJEWEL_CLIENT, peony.Messages.STRING_01723);
				    }
				}
			    tx.commit();
		        
		        // 删除镶嵌信息
			    for(int hole : holes){
			    	enhance.removeJewel(hole);
			    }
			    LogUtil.logAutoRemoveJewelOK(player, gi, true);
			    
			    //摘除成功事件
		        Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_EXTIRPADE, player));
				
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
				Packet pt = new Packet(OpCode.DECORATE_REMOVE_ALLJEWEL_SERVER);
	            pt.putInt(serial);
	            pt.putInt(equItemID);
	            pt.putInt(equInstanceID);
	            pt.put(enhance.toClientBytes());
	            session.send(pt);
			}
		}else{
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.DECORATE_REMOVE_ALLJEWEL_CLIENT, errorMessage);
		}
	}
	
	public static Packet getRequest(ItemEnhance enhance, ClientSession session ,int serial) throws Exception{
		ShopService service = Server.server.getServiceRegistry().getShopService();
		Player player = (Player)session.getClient();
		Packet pt = new Packet(OpCode.REMOVE_ALLJEWEL_RESOURCE_SERVER);
		int jewelNum = enhance.getJewelCount();//宝石个数
		int jewelLevelNum = 0;//宝石等级的种类
		int needMoney = 0;//需要金钱
		int needImoney = 0;//需要ib
		int needItemType = 0;//需要的摘除符种类
		Hashtable<Integer,Integer> jeLevel = new Hashtable<Integer, Integer>();//等级    个数
		Hashtable<Integer,Integer> jeLevelBag = new Hashtable<Integer, Integer>();//等级    个数  背包中
		JewelService js = Server.server.getServiceRegistry().getJewelService();
		for(int i = 0; i < jewelNum; i ++){
			ItemTemplate jewelTemp = ObjectAccessor.getItemTemplate(enhance.getJewelID(i));
			if(jewelTemp == null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.REMOVE_ALLJEWEL_RESOURCE_CLIENT, peony.Messages.STRING_00060);
				throw new Exception(peony.Messages.STRING_00060);
			}
			if(jeLevel.containsKey(jewelTemp.useLevel)){
				int num = jeLevel.get(jewelTemp.useLevel) + 1;
				jeLevel.remove(jewelTemp.useLevel);
				jeLevel.put(jewelTemp.useLevel, num);
			}else{
				jeLevel.put(jewelTemp.useLevel, 1);
			}
			//若背包中有  记录背包中卷轴数
			if(!jeLevelBag.containsKey(jewelTemp.useLevel)&& player.bag.getGameItemCount(js.removeItemIDs[jewelTemp.useLevel -1])!= 0){//包里没有  且身上也没有
				jeLevelBag.put(jewelTemp.useLevel,player.bag.getGameItemCount(js.removeItemIDs[jewelTemp.useLevel - 1]));
			}
			//若包中没有
			if(player.bag.getGameItemCount(js.removeItemIDs[jewelTemp.useLevel -1])== 0){
				if(jeLevel.get(jewelTemp.useLevel)<=1)
					needItemType ++;
				needImoney += Math.round(service.getItemPrice(js.removeItemIDs[jewelTemp.useLevel -1<2?2:jewelTemp.useLevel -1]));
			}
			needMoney += js.getRemovePrice(jewelTemp.useLevel);
		}
		jewelLevelNum = jeLevel.size();
		int[] jewelLevels = new int[jewelLevelNum];//都有哪个等级的石头
		Set<Integer> ee = jeLevel.keySet();
		Iterator<Integer> it = ee.iterator();
		for(int i = 0; i < jewelLevelNum; i++){
			jewelLevels[i] = it.next();
		}
		//包中有 但数量不够
		for(int i = 0; i < jewelLevelNum; i++){
			if(jeLevelBag.containsKey(jewelLevels[i]) && jeLevelBag.get(jewelLevels[i]) < jeLevel.get(jewelLevels[i])){
					needItemType++;
					int level = jewelLevels[i];
					int addOneMoney = Math.round(service.getItemPrice(js.removeItemIDs[level-1]));
					needImoney += (jeLevel.get(jewelLevels[i]) - jeLevelBag.get(jewelLevels[i])) * addOneMoney;
			}
		}
		int size = jeLevelBag.size();//背包中摘除符的种类
		pt.putInt(serial);
		pt.put(size);
		for(int i = 0; i < jewelLevelNum; i++){
			if(jeLevelBag.containsKey(jewelLevels[i])){
				pt.put(jewelLevels[i]);
				pt.put(jeLevelBag.get(jewelLevels[i]));
			}
		}
		pt.putInt(needMoney);
		pt.putInt(needImoney);
		pt.put(needItemType);
		for(int i = 0; i < jewelLevelNum; i ++){
				if(!jeLevelBag.containsKey(jewelLevels[i])){//不存在本等级拆除符
					pt.put(jewelLevels[i]);
					pt.put(jeLevel.get(jewelLevels[i]));
				}else{
					if(jeLevelBag.get(jewelLevels[i]) < jeLevel.get(jewelLevels[i])){//存在但数量不够
						int num = jeLevel.get(jewelLevels[i]) - jeLevelBag.get(jewelLevels[i]);
						pt.put(jewelLevels[i]);
						pt.put(num);
					}
				}
		}
		return pt;
	}
	
	public void run() {
		addToClientSession();
	}

}
