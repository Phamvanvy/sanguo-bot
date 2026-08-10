package peony.service.gamble;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

public class GambleService implements Service, ServiceEventListener{
	
	private static Logger log = Logger.getLogger(GambleService.class);
	
	List<GambleDef> gambles = new ArrayList<GambleDef>();
	
	public static String PROPERTY_VALUE_FULI = "consumefulizhi"; //玩家消费的福利值
	
	public static String PROPERTY_GAMBLE_DAYCOUNT = "gambledaycount"; //玩家每日抽奖次数
	
	public static int DAY_LIMIT = 30;//每日抽奖上限
	
    public synchronized void initGambleCount(Player player){
    	player.pool.remove(PROPERTY_GAMBLE_DAYCOUNT);
    }
	
	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
		
	}

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
		.findFile("gamble.xml");
        Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
        parse(doc);
        Server.server.getEventManager().registerListener(this);
	}
	
	@SuppressWarnings("unchecked")
	public void parse(Document doc){
		Element root = doc.getRootElement();
		List<GambleDef> gd = new ArrayList<GambleDef>();
		if (root != null) {
			List gamble = root.elements("gamble");
			for (int i = 0; i < gamble.size(); i++) {
				int gambleId = Integer.parseInt(((Element) gamble.get(i))
						.attributeValue("id"));
				int consumMoney = Integer.parseInt(((Element) gamble.get(i))
						.attributeValue("consumemoney"));
				int rewardItemId = Integer.parseInt(((Element) gamble.get(i))
						.attributeValue("rewarditem"));
				int count = Integer.parseInt(((Element) gamble.get(i))
						.attributeValue("count"));
				GambleDef def = new GambleDef(gambleId,consumMoney,rewardItemId,count);
				gd.add(def);
				List types = ((Element) gamble.get(i)).elements("type");
				for (int j = 0; j < types.size(); j++) {
					int typeId = Integer.parseInt(((Element) types.get(j))
							.attributeValue("typeid"));
					int rate = Integer.parseInt(((Element) types.get(j))
							.attributeValue("rate"));
					int decMoney = Integer.parseInt(((Element) types.get(j))
							.attributeValue("decmoney"));
					GambleItem type = new GambleItem(typeId,rate,decMoney);
					def.addGameItem(type);
				}
				gambles.add(def);
			}
		}
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_IBUY
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_IBUY:
			playerBuyOK(((Integer)event.param1).intValue(), ((Integer)event.param2).intValue());
			break;
		}
	}
	
	/*
	 * 玩家消费通知(1元宝=10积分)。
	 */
	protected void playerBuyOK(int playerId, int money) {
		Player player = ObjectAccessor.getPlayer(playerId);
		if(player!=null){
			int addMoney = Math.round(money/3600f);
			int total = player.pool.getInt(PROPERTY_VALUE_FULI,0);
			int oldValue = total;
			total += addMoney*10;
			player.pool.setInt(PROPERTY_VALUE_FULI, total);
			player.addIntPropertyChangedItem(ChangedItem.FULIVALUE,total,false,true);
			LogUtil.logAddFuliValue(player, oldValue, total);
		}
	}
	
	public void decFuli(Player player,int decValue,int gambleId,int typeId) throws Exception{
		int value = player.pool.getInt(PROPERTY_VALUE_FULI,0);;
		if(value>=decValue){
			value -= decValue;
			player.pool.setInt(PROPERTY_VALUE_FULI, value);
			player.addIntPropertyChangedItem(ChangedItem.FULIVALUE,value,false,true);
			LogUtil.logDecFuliValue(player, value+decValue, value, gambleId, typeId);
		}else{
			throw new Exception("您的消费积分不足");
		}
	}
	
	/**
	 * 抽奖列表
	 * @param session
	 * @param packet
	 */
	public void gambleList(ClientSession session,Packet packet){
		int serial = packet.getInt();
		Player player = (Player)session.getClient();
		if(player!=null){
			Packet pt = new Packet(OpCode.GAMBLE_LIST_SERVICE);
			pt.putInt(serial);
			if(gambles!=null && gambles.size()>0){
			   pt.put(gambles.size());
			   for(GambleDef gd : gambles){
				   pt.put(gd.gambleId);
				   pt.putInt(gd.consumMoney);
				   pt.putInt(gd.rewardItemId);
				   GameItem gi = ObjectAccessor.createGameItem(gd.rewardItemId);
				   pt.putString(gi.template.name);
				   pt.putInt(gi.template.showType);
				   pt.putInt(gi.template.showImage);
			   }
			}else{
				pt.put(0);
			}
			int fuliValue = player.pool.getInt(PROPERTY_VALUE_FULI,0);
		    pt.putInt(fuliValue);
		    player.send(pt);
		}
	}
	
	/**
	 * 抽奖类型列表
	 * @param session
	 * @param packet
	 */
	public void gambleDetailList(ClientSession session,Packet packet){
		int serial = packet.getInt();
		int gambleId = packet.get();
		Player player = (Player)session.getClient();
		if(player!=null){
			GambleDef def = getGambleById(gambleId);
			if(def!=null){
				if(player.pool.getInt(PROPERTY_VALUE_FULI,0)<def.consumMoney){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GAMBLE_DETAILLIST_CLIENT, MessageFormat.format("抽奖条件不足！您的消费积分尚未达到{0}", def.consumMoney));
				    return;
				}
			    Packet pt = new Packet(OpCode.GAMBLE_DETAILLIST_SERVICE);
			    pt.putInt(serial);
			    List<GambleItem> gis = def.getGambleItem();
				if(gis!=null && gis.size()>0){
				   pt.put(gis.size());
				   for(GambleItem gi : gis){
					   pt.put(gi.typeId);
					   pt.putInt(gi.rate);
					   pt.putInt(gi.decMoney);
				   }
				}else{
					pt.put(0);
				}
				player.send(pt);
			}
		}
	}
	
	/**
	 * 抽奖
	 * @param player
	 * @param gambleId
	 * @param typeId
	 * @throws Exception
	 */
	public void processGamble(Player player,int gambleId,int typeId)throws Exception{
		if(player!=null){
			int gambleCount = player.pool.getInt(PROPERTY_GAMBLE_DAYCOUNT, 0);
			if(gambleCount>=DAY_LIMIT){
				throw new Exception("您今天的抽奖次数已用完，请明天再来抽奖");
			}
			GambleDef def = getGambleById(gambleId);
			if(player.pool.getInt(PROPERTY_VALUE_FULI,0)<def.consumMoney){
				throw new Exception(MessageFormat.format("抽奖条件不足！您的消费积分尚未达到{0}", def.consumMoney));
			}
			if(def!=null){
				GambleItem gi = def.getGambleItemByid(typeId);
				if(gi!=null){
					decFuli(player,gi.decMoney,gambleId,typeId);
					int rnd = ItemUtil.rnd.nextInt(100);
					if(rnd<=gi.rate){
						GameItem item = ObjectAccessor.createGameItem(def.rewardItemId);
						if(item != null){
							PlayerTransaction tx = player.newTransaction("GAMBLEREWARD");
							try {
								player.bag.addGameItemComplete(item, def.count, tx, true);
								tx.commit();
								player.message(-1, "恭喜您，奖品已放入背包", -1, -1);
							} catch (Exception e) {
								tx.rollback();
								player.message(-1, "背包已满，奖品将通过飞鸽发放，请注意查收！", -1, -1);
								String content = MessageFormat.format(
										"恭喜您抽奖抽中了{0}个{1}",def.count,item.template.name);
								Server.server.getServiceRegistry().getMailService()
								.sendSystemMail(player.id, peony.Messages.STRING_00004, "商城积分抽奖奖励", content, 0,
										item, def.count, "GAMBLEREWARD");
								
							}
							if(gambleId == 2){//抽中5级宝石发世界公告
								String msg = MessageFormat.format("{0}在积分商城只花费了极少的积分就抽到了一个5级宝石如意袋，太厉害了！", player.name);
							    Server.server.getServiceRegistry().getChatService().sendWorldMessage(msg);
							}
							LogUtil.logFuliResult(player, "SUCCESS");
						} else {
							player.message(-1, "很遗憾，抽奖失败了", -1, -1);
							LogUtil.logFuliResult(player, "FAILD");
						}
					}else{
						player.message(-1, "很遗憾，抽奖失败了", -1, -1);
						LogUtil.logFuliResult(player, "FAILD");
					}
					gambleCount++;
					player.pool.setInt(PROPERTY_GAMBLE_DAYCOUNT, gambleCount);
				}
			}
		}
	}
	
	
	public GambleDef  getGambleById(int gambleId){
		if(gambles!=null && gambles.size()>0){
			for(GambleDef d:gambles){
				if(d.gambleId == gambleId){
					return d;
				}
			}
		}
		return null;
	}

}

class GambleDef{
	public int gambleId;
	public int consumMoney;
	public int rewardItemId;
	public int count;
	List<GambleItem> itemList = new ArrayList<GambleItem>();
	public GambleDef(int gambleId,int consumeMoney,int rewardItemId,int count){
		this.gambleId = gambleId;
		this.consumMoney = consumeMoney;
		this.rewardItemId = rewardItemId;
		this.count = count;
	}
	
	public void addGameItem(GambleItem gItem){
		itemList.add(gItem);
	}
	
	public List<GambleItem> getGambleItem(){
		return itemList;
	}
	
	public GambleItem getGambleItemByid(int typeId){
		if(itemList!=null && itemList.size()>0){
			for(GambleItem gi : itemList){
				if(gi.typeId == typeId)
					return gi;
			}
		}
		return null;
	}
	
}

class GambleItem{
	public int typeId;
	public int rate;
	public int decMoney;
	public GambleItem(int typeId,int rate,int decMoney){
		this.typeId = typeId;
		this.rate = rate;
		this.decMoney = decMoney;
	}
}


