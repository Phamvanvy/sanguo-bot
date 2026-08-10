package peony.game.itemenhance;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Logger;
import com.pip.sanguo.data.item.Item;
import peony.common.SyncIbuyCall;
import peony.game.ChatOption;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemTemplate;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.shop.ShopService;

public class AutoMergeJewelCall1 extends SyncIbuyCall {

	protected final Logger log = Logger.getLogger(MergeJewelCall.class);
	protected int serial;
	protected Player player;
	protected int jewelId;
	protected ItemTemplate mergeItem;
	protected static Random rand = new Random();
	public static int lowItemId = 1336;
	public static int highItemId = 1337;

	public AutoMergeJewelCall1(ClientSession session, Packet packet) {
		super(session,packet);
		this.serial = packet.getInt();
		this.player = (Player)session.getClient();
		this.jewelId = packet.getInt();
	}
	
	public static List<Map<Integer, String>> getRequest(Player player, int jewelId){
		if(player!=null){
			List<Map<Integer, String>> list = new ArrayList<Map<Integer,String>>();
			String currentJewelName = ObjectAccessor.getItemTemplate(jewelId).name.substring(2);
			int decImoney = getDecImoney(player, jewelId); //扣除i币
			int decMoney = getDecMoney(player, jewelId); //扣除金币
			int decItemCount = getItemCount(player, jewelId)[2]; //扣除低级宝石数量
			int lowItemCount = getItemCount(player, jewelId)[0]; //低级合成符数量
			int highItemCount = getItemCount(player, jewelId)[1]; //高级合成符数量
			String lowItemName = "低级宝石合成符";
			String highItemName = "高级宝石合成符";
			String name = MessageFormat.format("三级{0}", currentJewelName);
			if(decMoney>0){
				Map<Integer, String> request = new HashMap<Integer, String>();
				request.put(0, new Integer(decMoney).toString());
				list.add(request);
			}
			GameItem gi = player.bag.getGameItem(jewelId);
			if(decItemCount>0){
				int count = player.bag.getGameItemCount(Server.server.getServiceRegistry()
						.getJewelService().jewels[gi.template.jewelAttrType][2].id);
				if(count<decItemCount){
					Map<Integer, String> request = new HashMap<Integer, String>();
					request.put(1, MessageFormat.format("{0}{1}个", name, new Integer(decItemCount-count).toString()));
					list.add(request);
				}
			}
			if(lowItemCount>0 && player.bag.getGameItemCount(lowItemId)<lowItemCount){
				int count0 = player.bag.getGameItemCount(lowItemId);
				Map<Integer, String> request = new HashMap<Integer, String>();
				request.put(1, MessageFormat.format("{0}{1}个", lowItemName, new Integer(lowItemCount-count0).toString()));
				list.add(request);
			}
			if(highItemCount>0 && player.bag.getGameItemCount(highItemId)<highItemCount){
				int count0 = player.bag.getGameItemCount(highItemId);
				Map<Integer, String> request = new HashMap<Integer, String>();
				request.put(1, MessageFormat.format("{0}{1}个", highItemName, new Integer(highItemCount-count0).toString()));
				list.add(request);
			}
			if(decImoney>0){
				Map<Integer, String> request = new HashMap<Integer, String>();
				request.put(2, new Integer(decImoney).toString());
				list.add(request);
			}
			return list;
		}
		return null;
	}
	
	protected static int[] getItemCount(Player player, int jewelId){
		int[] counts = new int[3]; //依次为低级宝石合成符、高级宝石合成符、3级宝石
		int count = player.bag.getGameItemCount(jewelId);
		int jewelLevel = ObjectAccessor.getItemTemplate(jewelId).useLevel;
		int lowItemCount = 0; //低级合成符数量
		int highItemCount = 0; //高级合成符数量
		int decItemCount = 1; //最终需要3级宝石的数量
		int amount = 5 - count; //差本等级宝石数
		if(count<5){
			int levelBalance = jewelLevel - 3; //合成级别与3级的级别差
			for(int i=0;i<levelBalance;i++){
				decItemCount *= 5;
				if(i==levelBalance-1)
					lowItemCount += decItemCount/5;
				else
					highItemCount += decItemCount/5;
			}
			if(levelBalance==0)
				decItemCount = 5;
			else
				decItemCount *= amount;
			lowItemCount *= amount;
		}
		if(lowItemCount==0)
			lowItemCount = 1;
		highItemCount *= amount;
		if(jewelLevel>=4)
			highItemCount++;
		counts[0] = lowItemCount;
		counts[1] = highItemCount;
		counts[2] = decItemCount;
		return counts;
	}
	
	protected static int getDecImoney(Player player, int jewelId){
		ShopService service = Server.server.getServiceRegistry().getShopService();
		JewelService js = Server.server.getServiceRegistry().getJewelService();
		int[] itemCount = getItemCount(player, jewelId);
		int decImoney = 0; //扣除i币
		int lowItemCount = itemCount[0];
		int highItemCount = itemCount[1];
		int decItemCount = itemCount[2];
		GameItem gi = player.bag.getGameItem(jewelId);
		int decItemPrice = Math.round(service.getItemPrice(js.jewels[gi.template.jewelAttrType][2].id));
		int hasCount = player.bag.getGameItemCount(js.jewels[gi.template.jewelAttrType][2].id);
		if(hasCount<decItemCount)
			decImoney = (int) ((decItemCount-hasCount) * decItemPrice);
		if(lowItemCount>0 && player.bag.getGameItemCount(lowItemId)<lowItemCount){
			int count0 = player.bag.getGameItemCount(lowItemId);
			if(count0<lowItemCount){
				decImoney += service.getItemPrice(lowItemId)*(lowItemCount-count0);
			}
		}
		if(highItemCount>0 && player.bag.getGameItemCount(highItemId)<highItemCount){
			int count0 = player.bag.getGameItemCount(highItemId);
			if(count0<highItemCount){
				decImoney += service.getItemPrice(highItemId)*(highItemCount-count0);
			}
		}
//		decImoney *= service.getCurrentShopDiscount()/100f;
		return decImoney>=0? decImoney : 0;
	}
	
	protected static int getDecMoney(Player player, int jewelId){
		int decMoney = 0; //扣除金币
		int count = player.bag.getGameItemCount(jewelId);
		int jewelLevel = ObjectAccessor.getItemTemplate(jewelId).useLevel;
		if(count<5){
			if(jewelLevel==3)
				decMoney += 4050;
			else if(jewelLevel==4)
				decMoney += 4050*count+9600;
			else if(jewelLevel==5)
				decMoney += 9600*count+4050*5*count+18750;
			else if(jewelLevel==6)
				decMoney += 4050*25*count+9600*5*count+18750*count+32400;
		}else{
			decMoney = Server.server.getServiceRegistry().getJewelService().getMergePrice(jewelLevel);
		}
		return decMoney;
	}

	public void callFinish() {
	    try {
	        Packet pt = new Packet(OpCode.AUTO_MERGER_JEWEL_SERVER);
            pt.putInt(serial);
            pt.putInt(mergeItem.id);
            pt.put(mergeItem.showType);
            pt.putString(mergeItem.name);
            session.send(pt);
	    } catch (Exception e) {
	    	if(!success){
	    		ErrorHandler.sendErrorMessage(session, serial,
	                    OpCode.AUTO_MERGE_JEWEL_CLIENT, errorMessage);
	    	}else{
	    		ErrorHandler.sendErrorMessage(session, serial,
                    OpCode.AUTO_MERGE_JEWEL_CLIENT, "合成失败");
	    	}
	    }
	}

	public void run() {
		ibuys.clear();
		if(player!=null){
			ShopService service = Server.server.getServiceRegistry().getShopService();
			JewelService js = Server.server.getServiceRegistry().getJewelService();
		    int currentJewelCount = player.bag.getGameItemCount(jewelId);
		    PlayerTransaction tx = player.newTransaction("AMJE");
		    int jewelLevel;
			try {
				jewelLevel = ObjectAccessor.getItemTemplate(jewelId).useLevel;
			} catch (Exception e1) {
				tx.rollback();
				error("宝石数据错误");
				addToClientSession();
				return;
			}
		    if(jewelLevel<=2){
		    	tx.rollback();
		    	error("达到3级才能自动合成");
		    	addToClientSession();
				return;
		    }
		    if(jewelLevel>=6){
		    	tx.rollback();
		    	error("此等级的宝石不能自动合成");
		    	addToClientSession();
				return;
		    }
		    LogUtil.logAutoMergeJewelTry(player, jewelId, currentJewelCount);
		    if (currentJewelCount <=0 ) {
		    	tx.rollback();
		    	error("宝石数量错误");
		    	addToClientSession();
				return;
		    }
		    GameItem gi = player.bag.removeGameItem(jewelId, GameItem.GENERAL_INSTANCEID, currentJewelCount, tx, true);
		    if (gi == null) {
		        tx.rollback();
		        error("没有足够的宝石");
		        addToClientSession();
				return;
		    }
		    if(gi.template.isFlaw){
		    	tx.rollback();
		    	error("有瑕疵的宝石没有合成功能");
		    	addToClientSession();
				return;
		    }
		    if (gi.template.itemType != Item.TYPE_JEWEL) {
		        tx.rollback();
		        error("选择物品错误");
		        addToClientSession();
				return;
		    }
		    if (gi.template.useLevel >= JewelService.JEWEL_LEVELS) {
		        tx.rollback();
		        error("你选择的宝石已经是最高级的了");
		        addToClientSession();
				return;
		    }
			int decImoney = getDecImoney(player, jewelId); //扣除i币
			if(decImoney*100>player.getAccount().getLongIMoney()){
				tx.rollback();
				error("i币不足");
		    	addToClientSession();
				return;
			}
			int decMoney = getDecMoney(player, jewelId); //扣除金币
			try {
				player.decMoney(decMoney, tx, true);
			} catch (NoEnoughValueException e) {
				tx.rollback();
				ibuyRollBack();
				error("金钱不足");
				addToClientSession();
				return;
			}
			int decItemCount = getItemCount(player,jewelId)[2];
			int hasCount = player.bag.getGameItemCount(js.jewels[gi.template.jewelAttrType][2].id);
			if(hasCount<decItemCount){
				if(hasCount>0)
					player.bag.removeGameItemIngoreInstanceId(js.jewels[gi.template.jewelAttrType][2].id, hasCount, tx, false);
				try {
					waitBuy(syncShopItemBuy(player, 0, service.getShopByItemId(lowItemId).id, js.jewels[gi.template.jewelAttrType][2].id, decItemCount-hasCount, this));
				} catch (Exception e) {
					tx.rollback();
					callFinish();
					return;
				}
			}else{
				player.bag.removeGameItemIngoreInstanceId(js.jewels[gi.template.jewelAttrType][2].id, decItemCount, tx, false);
			}
			int lowItemCount = getItemCount(player,jewelId)[0];
			if(lowItemCount>0){
				int count = player.bag.getGameItemCount(lowItemId);
				if(count>=lowItemCount){
					player.bag.removeGameItemIngoreInstanceId(lowItemId, lowItemCount, tx, false);
				}else if(count<lowItemCount){
					player.bag.removeGameItemIngoreInstanceId(lowItemId, count, tx, false);
					try {
						waitBuy(syncShopItemBuy(player, 0, service.getShopByItemId(lowItemId).id, lowItemId, lowItemCount-count, this));
					} catch (Exception e) {
						tx.rollback();
						callFinish();
						return;
					}
				}
			}
			int highItemCount = getItemCount(player,jewelId)[1];
			if(highItemCount>0){
				int count = player.bag.getGameItemCount(highItemId);
				if(count>=highItemCount){
					player.bag.removeGameItemIngoreInstanceId(highItemId, highItemCount, tx, false);
				}else if(count<highItemCount){
					player.bag.removeGameItemIngoreInstanceId(highItemId, count, tx, false);
					try {
						waitBuy(syncShopItemBuy(player, 0, service.getShopByItemId(highItemId).id, highItemId, highItemCount-count, this));
					} catch (Exception e) {
						callFinish();
						tx.rollback();
						return;
					}
				}
			}
		    mergeItem = js.jewels[gi.template.jewelAttrType][gi.template.useLevel];
		    GameItem gi3 = ObjectAccessor.createGameItem(mergeItem, -1);
	        if (!player.bag.addGameItem(gi3, 1, tx, true)) {
	            tx.rollback();
	            ibuyRollBack();
	            error("背包已满");
	            addToClientSession();
	    		return;
	        }
	        tx.commit();
	        LogUtil.logAutoMergeJewelOK(player, jewelId, currentJewelCount, true);
	        Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MERGEJEWEL, player));
	        sendMessage(player, gi3);
		}
		addToClientSession();
	}
	
	protected void sendMessage(Player player, GameItem gi3){
		if(gi3.template.useLevel==5){
        	String s = MessageFormat.format("{0}成功合成出了一颗闪闪发光的{1}", player.name,gi3.template.name);
        	Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(player.faction, s);
        }
        else if(gi3.template.useLevel==6){
        	String s = MessageFormat.format("{0}竟然成功的合成出了一颗完美无瑕的{1}", player.name,gi3.template.name);
        	Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(player.faction, s);
        }
        else if(gi3.template.useLevel==7){
        	String s = MessageFormat.format("{0}的{1}让绝世神石{2}奇迹般的诞生在这个世界上！", player.getFactionName(),player.name,gi3.template.name);
        	Server.server.getServiceRegistry().getChatService().sendSystemMessage(ChatOption.WORLD, "系统", s);
        }
	}

}
