package peony.game.itemenhance;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Logger;
import com.pip.sanguo.data.item.Item;
import peony.common.ClientSessionAsyncCall;
import peony.decimoney.DecImoneyBuy;
import peony.game.ChatOption;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemTemplate;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.chat.ChatMessage;
import peony.game.chat.ItemChatAttachment;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.account.Account;
import peony.service.shop.ShopException;
import peony.service.shop.ShopService;

public class AutoMergeJewelCall extends ClientSessionAsyncCall {

	protected final Logger log = Logger.getLogger(MergeJewelCall.class);
	protected int serial;
	protected Player player;
	protected int jewelId;
	protected ItemTemplate mergeItem;
	protected static Random rand = new Random();
	public static int lowItemId = 1336;
	public static int highItemId = 1337;

	public AutoMergeJewelCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.player = (Player)session.getClient();
		this.jewelId = packet.getInt();
	}
	
	public static List<Map<Integer, String>> getRequest(Player player, int jewelId){
		if(player!=null){
			ShopService service = Server.server.getServiceRegistry().getShopService();
			List<Map<Integer, String>> list = new ArrayList<Map<Integer,String>>();
			String currentJewelName = ObjectAccessor.getItemTemplate(jewelId).name.substring(2);
			int count = player.bag.getGameItemCount(jewelId);
			int jewelLevel = ObjectAccessor.getItemTemplate(jewelId).useLevel;
			int decImoney = 0; //扣除i币
			int decMoney = 0; //扣除金币
			int decItemCount = 0; //扣除低级宝石数量
			int lowItemCount = 0; //低级合成符数量
			int highItemCount = 0; //高级合成符数量
			String lowItemName = peony.Messages.STRING_00543;
			String highItemName = peony.Messages.STRING_00544;
			String name = MessageFormat.format(peony.Messages.STRING_00545, currentJewelName);
			int ccc = 5 - count; //差本等级宝石数
			if(count<5){
				int c = jewelLevel - 3; //合成级别与3级的级别差
				int cc = 1; //最终需要3级宝石的数量
				for(int i=0;i<c;i++){
					cc *= 5;
					if(i==c-1)
						lowItemCount += cc/5;
					else
						highItemCount += cc/5;
				}
				cc *= ccc;
				decItemCount = cc;
				decImoney = (int) (cc * 900 * service.getCurrentShopDiscount()/100f);
				lowItemCount *= ccc;
				if(lowItemCount==0)
					lowItemCount = 1;
				highItemCount *= ccc;
				if(jewelLevel>=4)
					highItemCount++;
			}
			for(int i=0;jewelLevel+1-i>3;i++){
				int A = (int) (Math.pow(jewelLevel-i, 3)*150*Math.pow(5, (i-1)<0?0:(i-1))*(i==0?1:(5-count)));
				decMoney += A;
			}
			if(decMoney>0){
				int decMon = Math.round(decMoney * JewelService.mergeJewelRate);
				Map<Integer, String> request = new HashMap<Integer, String>();
				request.put(0, new Integer(decMon).toString());
				list.add(request);
			}
			if(decItemCount>0){
				Map<Integer, String> request = new HashMap<Integer, String>();
				request.put(1, MessageFormat.format(peony.Messages.STRING_00546, name, new Integer(decItemCount).toString()));
				list.add(request);
			}
			if(lowItemCount>0 && player.bag.getGameItemCount(lowItemId)<lowItemCount){
				int count0 = player.bag.getGameItemCount(lowItemId);
				if(count0<lowItemCount){
					decImoney += 36*(lowItemCount-count0);
				}
				Map<Integer, String> request = new HashMap<Integer, String>();
				request.put(1, MessageFormat.format(peony.Messages.STRING_00546, lowItemName, new Integer(lowItemCount-count0).toString()));
				list.add(request);
			}
			if(highItemCount>0 && player.bag.getGameItemCount(highItemId)<highItemCount){
				int count0 = player.bag.getGameItemCount(highItemId);
				if(count0<highItemCount){
					decImoney += 216*(highItemCount-count0);
				}
				Map<Integer, String> request = new HashMap<Integer, String>();
				request.put(1, MessageFormat.format(peony.Messages.STRING_00546, highItemName, new Integer(highItemCount-count0).toString()));
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
	
	protected void go() throws Exception {
		if(player!=null){
			ShopService service = Server.server.getServiceRegistry().getShopService();
			JewelService js = Server.server.getServiceRegistry().getJewelService();
		    int jewelCount = player.bag.getGameItemCount(jewelId);
		    int jewelLevel;
			try {
				jewelLevel = ObjectAccessor.getItemTemplate(jewelId).useLevel;
			} catch (Exception e1) {
				throw new Exception(peony.Messages.STRING_00060);
			}
		    if(jewelLevel<=2){
		    	throw new Exception(peony.Messages.STRING_00096);
		    }
		    if(jewelLevel>=6){
		    	throw new Exception(peony.Messages.STRING_00548);
		    }
		    LogUtil.logAutoMergeJewelTry(player, jewelId, jewelCount);
		    if (jewelCount <=0 ) {
		        throw new Exception(peony.Messages.STRING_00549);
		    }
			int decImoney = 0; //扣除i币
			int decMoney = 0; //扣除金币
			int lowItemCount = 0; //低级合成符数量
			int highItemCount = 0; //高级合成符数量
			int ccc = 5 - jewelCount; //差本等级宝石数
			if(jewelCount<5){
				int c = jewelLevel - 3; //合成级别与3级的级别差
				int cc = 1; //最终需要3级宝石的数量
				for(int i=0;i<c;i++){
					cc *= 5;
					if(i==c-1)
						lowItemCount += cc/5;
					else
						highItemCount += cc/5;
				}
				cc *= ccc;
				decImoney = (int) (cc * 900 * service.getCurrentShopDiscount()/100f);;
				lowItemCount *= ccc;
				if(lowItemCount==0)
					lowItemCount = 1;
				highItemCount *= ccc;
				if(jewelLevel>=4)
					highItemCount++;
			}
			for(int i=0;jewelLevel+1-i>3;i++){
				int A = (int) (Math.pow(jewelLevel-i, 3)*150*Math.pow(5, (i-1)<0?0:(i-1))*(i==0?1:(5-jewelCount)));
				decMoney += A;
			}
		    PlayerTransaction tx = player.newTransaction("AMJE");
		    GameItem gi = player.bag.removeGameItem(jewelId, GameItem.GENERAL_INSTANCEID, jewelCount, tx, true);
		    if (gi == null) {
		        tx.rollback();
		        throw new Exception(peony.Messages.STRING_00550);
		    }
		    if(gi.template.isFlaw){
		    	throw new Exception(peony.Messages.STRING_00551);
		    }
		    if (gi.template.itemType != Item.TYPE_JEWEL) {
		        tx.rollback();
		        throw new Exception(peony.Messages.STRING_00552);
		    }
		    if (gi.template.useLevel >= JewelService.JEWEL_LEVELS) {
		        tx.rollback();
		        throw new Exception(peony.Messages.STRING_00553);
		    }
		    if(decMoney<=player.money){
		    	int decMon = Math.round(decMoney*JewelService.mergeJewelRate); 
				player.decMoney(decMon, tx, true);
			}else{
				tx.rollback();
				throw new Exception(peony.Messages.STRING_00158);
			}
			if(lowItemCount>0){
				int count = player.bag.getGameItemCount(lowItemId);
				if(count>=lowItemCount){
					player.bag.removeGameItemIngoreInstanceId(lowItemId, lowItemCount, tx, false);
				}else if(count>0 && count<lowItemCount){
					player.bag.removeGameItemIngoreInstanceId(lowItemId, count, tx, false);
					decImoney += 36*(lowItemCount-count);
				}else if(count==0){
					decImoney += 36*lowItemCount;
				}
			}
			if(highItemCount>0){
				int count = player.bag.getGameItemCount(highItemId);
				if(count>=highItemCount){
					player.bag.removeGameItemIngoreInstanceId(highItemId, highItemCount, tx, false);
				}else if(count>0 && count<highItemCount){
					player.bag.removeGameItemIngoreInstanceId(highItemId, count, tx, false);
					decImoney += 216*(highItemCount-count);
				}else if(count==0){
					decImoney += 216*highItemCount;
				}
			}
			if(decImoney>0){
				Account account = player.getAccount();
				long imoney = account.getLongIMoney();
				if(decImoney>imoney){
					tx.rollback();
					throw new Exception(peony.Messages.STRING_00554);
				}
			}
		    mergeItem = js.jewels[gi.template.jewelAttrType][gi.template.useLevel];
		    GameItem gi3 = ObjectAccessor.createGameItem(mergeItem, -1);
	        if (!player.bag.addGameItem(gi3, 1, tx, true)) {
	            tx.rollback();
	            throw new Exception(peony.Messages.STRING_00555);
	        }
	        ShopService shopService = Server.server.getServiceRegistry().getShopService();
			try {
				DecImoneyBuy dib = new DecImoneyBuy(player,decImoney,"AMJE");
				shopService.buy(player, dib);
			} catch (ShopException e) {
				tx.rollback();
				throw new Exception(peony.Messages.STRING_00554);
			}
	        tx.commit();
	        LogUtil.logAutoMergeJewelOK(player, jewelId, jewelCount, true);
	        
	        Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MERGEJEWEL, player));
	        
	        ItemChatAttachment attItem = new ItemChatAttachment(gi3);
	        if(gi3.template.useLevel==5){
	        	String s = MessageFormat.format(peony.Messages.STRING_00556, player.name,gi3.template.name);
	        	ChatMessage cm = new ChatMessage(ChatOption.FACTION, player.id, player.faction,peony.Messages.STRING_00004,player.faction, s, null);
	    		Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
	        }
	        else if(gi3.template.useLevel==6){
	        	String s = MessageFormat.format(peony.Messages.STRING_00557, player.getFactionName(),player.name);
	        	ChatMessage cm = new ChatMessage(ChatOption.WORLD, player.id, -1,peony.Messages.STRING_00004, s, attItem);
	    		Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
	        }
	        else if(gi3.template.useLevel==7){
	        	String s = MessageFormat.format(peony.Messages.STRING_00558, player.getFactionName(),player.name);
	        	ChatMessage cm = new ChatMessage(ChatOption.WORLD, player.id, -1,peony.Messages.STRING_00004, s, attItem);
	    		Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
	        }
		}
	}

	public void callFinish() {
	    try {
	        go();
	        
	        // 回送合成成功的包
	        Packet pt = new Packet(OpCode.AUTO_MERGER_JEWEL_SERVER);
            pt.putInt(serial);
            pt.putInt(mergeItem.id);
            pt.put(mergeItem.showType);
            pt.putString(mergeItem.name);
            session.send(pt);
	    } catch (Exception e) {
	        ErrorHandler.sendErrorMessage(session, serial,
                    OpCode.AUTO_MERGE_JEWEL_CLIENT, e.getMessage());
	    }
	}

	public void run() {
		addToClientSession();
	}

}
