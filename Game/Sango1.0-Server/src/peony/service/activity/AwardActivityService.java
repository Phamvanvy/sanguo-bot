package peony.service.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import peony.game.DayListener;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.util.IntHashMap;

public class AwardActivityService implements Service,DayListener,ServiceEventListener{
	
	protected Map<Integer,Award> playerId2Award = new ConcurrentHashMap<Integer,Award>();
	protected IntHashMap<Integer> playerId2Count = new IntHashMap<Integer>();
	
	public static int decItem = 4627;
	public static int count = 5;
	public static int REWARD_ITEM1 = 4629;	//末日狂欢至尊礼包,占两个
	public static int REWARD_ITEM2 = 4628;	//末日狂欢惊喜礼包
	public static int DAY_LIMIT = 1;
	
	public Random rnd = new Random();

	public void shutdown() {
		
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}
	
	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_CHANGEDAY_THREE
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
			case ServiceEvent.EVENT_CHANGEDAY_THREE:
				dayChanged();
				break;
		}
	}
	
	public void dayChanged() {
		playerId2Count.clear();
	}
	
	/** 抽奖请求列表 */
	public synchronized void getAwardItems(Player player) throws Exception{
		if(player!=null){
			if(playerId2Count.containsKey(player.id)){
				int count = playerId2Count.get(player.id);
	    		if(count>=DAY_LIMIT){
	    			throw new Exception("您今天抽奖次数已用完，请明天再来抽奖。");
	    		}
			}
		    Award award = playerId2Award.get(player.id);
		    if(award==null){
				award = new Award(player.id);
			    if(award!=null){
			    	playerId2Award.put(player.id, award);
			    }else{
			    	throw new Exception("数据错误");
			    }
		    }
		    if(award!=null){
				//发送物品列表协议
				Packet pt = new Packet(OpCode.GET_AWARD_ITEMS_SERVER); 
				pt.putInt(OpCode.GET_AWARD_ITEMS_SERVER);
				pt.putInt(award.awardItemId);
				for(GameItem item:award.items){
					pt.put(1);
					pt.putInt(item.template.id);
					pt.putString(item.template.name);
					pt.putInt(item.template.showType);
				}
				player.send(pt);
			}
		}
	}
	
	/**
	 * 抽奖(3个惊喜礼包得总概率为99.2%，2个至尊礼包得总概率为：0.8%)
	 */
	public synchronized int getAward(Player p) throws Exception{
		Award award = playerId2Award.get(p.id);
		if(award == null)
			throw new Exception(peony.Messages.STRING_01659);
		if(award.awardItemId==0){
	        List<GameItem> list = award.items;
	        
	        float index = rnd.nextFloat()*100;
			if(index >= 99.2){
				award.awardItemId = REWARD_ITEM1;
			}else{
				award.awardItemId = REWARD_ITEM2;
			}
		}
		return award.awardItemId;
	}
	
	/** 抽奖结果 */
	public void awardGet(ClientSession session,Packet packet){
		int serial = packet.getInt();
		Player player = (Player)session.getClient();
		if(player!=null){
			PlayerTransaction tx = player.newTransaction("AWARDACTIVITY");
			GameItem it = player.bag.removeGameItemIngoreInstanceId(decItem, count, tx, false);
			if(it != null){
				tx.commit();
			}else{
				tx.rollback();
				ErrorHandler.sendErrorMessage(session, serial, OpCode.AWARDACTIVITY_RESULT_CLIENT, "您身上不足5个红包，无法进行抽奖");
				return;
			}
			
			int awardId;
			try {
				awardId = getAward(player);
			} catch (Exception e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.AWARDACTIVITY_RESULT_CLIENT, e.getMessage());
				return;
			}
			Packet pt = new Packet(OpCode.AWARDACTIVITY_RESULT_SERVER); 
			pt.putInt(serial);
			pt.putInt(awardId);
			player.send(pt);
		}
	}
	
	/**
	 * 抽奖后对角色缓存的处理
	 */
	public synchronized void processGetAwardOver(Player p){
		playerId2Award.remove(p.id);
		if(playerId2Count.containsKey(p.id)){
			int count = playerId2Count.get(p.id);
			count++;
			playerId2Count.put(p.id, count);
		}else{
			playerId2Count.put(p.id, 1);
		}
	}
	
	//获取玩家抽奖物品
	public synchronized GameItem getAwardByPlayerId(int id) throws Exception{
		Award award = playerId2Award.get(id);
		if(award == null)
			throw new Exception(peony.Messages.STRING_01658);
		for(GameItem gi : award.items){
			if(gi.template.id == award.awardItemId){
				return gi;
			}
		}
		return null;
	}
	
	
	/**
	 * 根据人物条件获得奖品列表
	 */
	public void getItemsByPlayer(Award award){
		int indexA = rnd.nextInt(5);
		int indexB = rnd.nextInt(5);
		while(indexA == indexB){
			indexB = rnd.nextInt(5);
		}
		for(int i=0; i<5; i++){
			GameItem item;
			if(i==indexA || i== indexB){
				item = ObjectAccessor.createGameItem(REWARD_ITEM1);
			}else{
				item = ObjectAccessor.createGameItem(REWARD_ITEM2);
			}
			award.items.add(item);
		}
	}
	
	//一次抽奖的结构
	class Award{
		int playerId;
		int awardItemId = 0;//抽中物品
		List<GameItem> items = new ArrayList<GameItem>();
		public Award(int playerId){
			this.playerId = playerId;
			getItemsByPlayer(this);
		}
	}

}

