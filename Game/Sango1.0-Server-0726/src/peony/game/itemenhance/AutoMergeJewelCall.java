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
			String lowItemName = "低级宝石合成符";
			String highItemName = "高级宝石合成符";
			String name = MessageFormat.format("三级{0}", currentJewelName);
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
				Map<Integer, String> request = new HashMap<Integer, String>();
				request.put(0, new Integer(decMoney).toString());
				list.add(request);
			}
			if(decItemCount>0){
				Map<Integer, String> request = new HashMap<Integer, String>();
				request.put(1, MessageFormat.format("{0}{1}个", name, new Integer(decItemCount).toString()));
				list.add(request);
			}
			if(lowItemCount>0 && player.bag.getGameItemCount(lowItemId)<lowItemCount){
				int count0 = player.bag.getGameItemCount(lowItemId);
				if(count0<lowItemCount){
					decImoney += 36*(lowItemCount-count0);
				}
				Map<Integer, String> request = new HashMap<Integer, String>();
				request.put(1, MessageFormat.format("{0}{1}个", lowItemName, new Integer(lowItemCount-count0).toString()));
				list.add(request);
			}
			if(highItemCount>0 && player.bag.getGameItemCount(highItemId)<highItemCount){
				int count0 = player.bag.getGameItemCount(highItemId);
				if(count0<highItemCount){
					decImoney += 216*(highItemCount-count0);
				}
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
	
	protected void go() throws Exception {
		if(player!=null){
			ShopService service = Server.server.getServiceRegistry().getShopService();
			JewelService js = Server.server.getServiceRegistry().getJewelService();
		    int jewelCount = player.bag.getGameItemCount(jewelId);
		    int jewelLevel;
			try {
				jewelLevel = ObjectAccessor.getItemTemplate(jewelId).useLevel;
			} catch (Exception e1) {
				throw new Exception("宝石数据错误");
			}
		    if(jewelLevel<=2){
		    	throw new Exception("达到3级才能自动合成");
		    }
		    if(jewelLevel>=6){
		    	throw new Exception("此等级的宝石不能自动合成");
		    }
		    LogUtil.logAutoMergeJewelTry(player, jewelId, jewelCount);
		    if (jewelCount <=0 ) {
		        throw new Exception("宝石数量错误");
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
		        throw new Exception("没有足够的宝石");
		    }
		    if(gi.template.isFlaw){
		    	throw new Exception("有瑕疵的宝石没有合成功能");
		    }
		    if (gi.template.itemType != Item.TYPE_JEWEL) {
		        tx.rollback();
		        throw new Exception("选择物品错误");
		    }
		    if (gi.template.useLevel >= JewelService.JEWEL_LEVELS) {
		        tx.rollback();
		        throw new Exception("你选择的宝石已经是最高级的了");
		    }
		    if(decMoney<=player.money){
				player.decMoney(decMoney, tx, true);
			}else{
				tx.rollback();
				throw new Exception("金钱不足");
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
					throw new Exception("i币不足");
				}
			}
		    mergeItem = js.jewels[gi.template.jewelAttrType][gi.template.useLevel];
		    GameItem gi3 = ObjectAccessor.createGameItem(mergeItem, -1);
	        if (!player.bag.addGameItem(gi3, 1, tx, true)) {
	            tx.rollback();
	            throw new Exception("背包已满");
	        }
	        ShopService shopService = Server.server.getServiceRegistry().getShopService();
			try {
				DecImoneyBuy dib = new DecImoneyBuy(player,decImoney,"AMJE");
				shopService.buy(player, dib);
			} catch (ShopException e) {
				tx.rollback();
				throw new Exception("i币不足");
			}
	        tx.commit();
	        LogUtil.logAutoMergeJewelOK(player, jewelId, jewelCount, true);
	        
	        Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MERGEJEWEL, player));
	        
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
