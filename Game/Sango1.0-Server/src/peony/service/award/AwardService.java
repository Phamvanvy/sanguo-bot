package peony.service.award;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.account.AccountProperty;

/**
 * 抽奖
 * @author pmeng
 */
public class AwardService implements Service,ServiceEventListener{
	
	protected Map<Integer,Award> playerId2Award = new ConcurrentHashMap<Integer,Award>();
	
	protected List<Integer> getAwardPlayers = new ArrayList<Integer>();//已领取过奖励的玩家
	
	protected List<Integer> canGetAwardPlayers = new ArrayList<Integer>();//在线一小时尚未领奖的玩家
	
//	protected List<GameItem> highA = new ArrayList<GameItem>();
//	
//	protected List<GameItem> highB = new ArrayList<GameItem>();
//	
//	protected List<GameItem> middleA = new ArrayList<GameItem>();
//	
//	protected List<GameItem> middleB = new ArrayList<GameItem>();
	public static String PROPERTY_GETAWARD_NUM = "getawardnum";
	
	protected List<ItemCount> high = new ArrayList<ItemCount>();
	protected List<ItemCount> middle = new ArrayList<ItemCount>();
	protected List<ItemCount> low = new ArrayList<ItemCount>();
	
	private static Random random = new Random();
	
	public static int FIRST_CLOSE = 1000;//第一次关闭所扣j币
	
	public static int SECONED_CLOSE = 2;//第二次关闭多扣元宝
	
	public void shutdown() {
		
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("award.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
	}
	
	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_ONLINE_HOUR,
				ServiceEvent.EVENT_CHANGEDAY_THREE,
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
			case ServiceEvent.EVENT_ONLINE_HOUR:
				processOnlineHour((Player)event.param1);
				break;
			case ServiceEvent.EVENT_CHANGEDAY_THREE:
				processChangeDay();
				break;
			case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
				processPlayerLoad((Player)event.param1);
				break;
		}
	}
	
	public void processPlayerLoad(Player player){
		if(Server.server.getServiceRegistry().getPlayerOnlineTimeService().players.contains(player.id)){
			if(isOnLineOneHour(player.id)){
				if(!Server.isStepServer && !player.isInStep){
					Packet pt = new Packet(OpCode.ONLINE_ONEHOUR_SERVER); 
					pt.putInt(OpCode.ONLINE_ONEHOUR_SERVER);
					int totalTime = 1;
					int getAwardCount = player.pool.getInt(PROPERTY_GETAWARD_NUM, 0);
					AccountProperty ap = Server.server.getServiceRegistry().getVipPrivilegeService().getAccountProperty(player.accountId);
					if(ap!=null){
						if(player.vipLevel>=3){
							totalTime = 2;
						}
					}
					pt.putInt(totalTime-getAwardCount);
					player.send(pt);
				}
			}
		}
	}
	
	public void processChangeDay(){
		getAwardPlayers.clear();
		canGetAwardPlayers.clear();
	}
	
	/**
	 * 抽奖后对角色缓存的处理
	 */
	public synchronized void processGetAwardOver(Player p){
		playerId2Award.remove(p.id);
		canGetAwardPlayers.remove(new Integer(p.id));
		int count = p.pool.getInt(PROPERTY_GETAWARD_NUM, 0);
		count++;
		if(p.vipLevel<3 || (p.vipLevel>=3 && count>=2)){
		    getAwardPlayers.add(p.id);
		}
		p.pool.setInt(PROPERTY_GETAWARD_NUM, count);
		if(p.vipLevel>=3 && count <2){
			canGetAwardPlayers.add(new Integer(p.id));
			//发送在线一小时
			if(!Server.isStepServer && !p.isInStep){
				Packet pt = new Packet(OpCode.ONLINE_ONEHOUR_SERVER); 
				pt.putInt(OpCode.ONLINE_ONEHOUR_SERVER);
				int totalTime = 1;
				if(p.vipLevel>=3){
					totalTime = 2;
				}
				pt.putInt(totalTime-count);
				p.send(pt);
			}
		}
	}
	
	public void processOnlineHour(Player p){
		if(p == null)
			return;
		Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, peony.Messages.STRING_01657);
		canGetAwardPlayers.add(p.id);
		//发送在线一小时
		if(!Server.isStepServer && !p.isInStep){
			Packet pt = new Packet(OpCode.ONLINE_ONEHOUR_SERVER); 
			pt.putInt(OpCode.ONLINE_ONEHOUR_SERVER);
			int totalTime = 1;
			int getAwardCount = p.pool.getInt(PROPERTY_GETAWARD_NUM, 0);
			if(p.vipLevel>=3){
				totalTime = 2;
			}
			pt.putInt(totalTime-getAwardCount);
			p.send(pt);
		}
	}
	
	//获取玩家抽奖对象
	public synchronized int[] getAwardByPlayerId(int id) throws AwardException{
		Award award = playerId2Award.get(id);
		if(award == null)
			throw new AwardException(peony.Messages.STRING_01658);
		int[] reward = new int[2];
		reward[0]=award.awardItemId;
		reward[1] = 1;
		for(ItemCount ic : award.items){
			if(ic.item.template.id == award.awardItemId){
				reward[1] = ic.count;
				break;
			}
		}
		return reward;
	}
	
	@SuppressWarnings("unchecked")
	public void parse(Document doc){
		Element root = doc.getRootElement();
		if (root != null) {
			Element h = root.element("high");
			List<Element> highas = h.elements("item");
			for (int i = 0; i < highas.size(); i++) {
				int id = Integer.parseInt(((Element) highas.get(i)).attributeValue("itemId"));
				int count  = Integer.parseInt(((Element) highas.get(i)).attributeValue("count"));
				int rate = Integer.parseInt(((Element) highas.get(i)).attributeValue("rate"));
				GameItem item = ObjectAccessor.createGameItem(id);
				if(item != null){
					ItemCount it = new ItemCount(item,count,rate);
					high.add(it);
				}
			}
			Element m = root.element("middle");
			List<Element> middleas = m.elements("item");
			for (int i = 0; i < middleas.size(); i++) {
				int id = Integer.parseInt(((Element) middleas.get(i)).attributeValue("itemId"));
				int count  = Integer.parseInt(((Element) middleas.get(i)).attributeValue("count"));
				int rate = Integer.parseInt(((Element) middleas.get(i)).attributeValue("rate"));
				GameItem item = ObjectAccessor.createGameItem(id);
				if(item != null){
					ItemCount it = new ItemCount(item,count,rate);
				    middle.add(it);
				}
			}
			Element l = root.element("low");
			List<Element> lows = l.elements("item");
			for (int i = 0; i < lows.size(); i++) {
				int id = Integer.parseInt(((Element) lows.get(i)).attributeValue("itemId"));
				int count  = Integer.parseInt(((Element) lows.get(i)).attributeValue("count"));
				int rate = Integer.parseInt(((Element) lows.get(i)).attributeValue("rate"));
				GameItem item = ObjectAccessor.createGameItem(id);
				if(item != null){
					ItemCount it = new ItemCount(item,count,rate);
			        low.add(it);
				}
			}
	    }
	}
	
	/**
	 * 抽奖
	 */
	public synchronized int getAward(Player p,int closeId1,int closeId2) throws AwardException{
		Award award = playerId2Award.get(p.id);
		if(award == null)
			throw new AwardException(peony.Messages.STRING_01659);
		int seed = random.nextInt(100);
		List<ItemCount> list = award.items;
		List<ItemCount> tempList = new ArrayList<ItemCount>();
		int totalNum = 0;
		for(int i=0;i<list.size();i++){
			ItemCount ic = list.get(i);
			if(ic.item.template.id == award.closeId1 || ic.item.template.id == award.closeId2){
//				list.remove(i);
//				i--;
				continue;
			}
		    totalNum+=ic.rate;
		    tempList.add(ic);
		}
		int base = 0;
		for(int i=0;i<tempList.size();i++){
			ItemCount ic = tempList.get(i);
			double tempRate = (double)(ic.rate*100)/totalNum;
			int num = base+(int)Math.ceil(tempRate);
			if(seed<num+1 && seed>base){
				award.awardItemId = ic.item.template.id;
				break;
			}
			base = num;
		}
//		if(closeId1 == award.lowId && closeId2 == -1){//只关闭一个低级物品    中A  80%   中B  20%
//			if(seed > 20){
//				award.awardItemId = award.middleAId;
//			}else{
//				award.awardItemId = award.middleBId;
//			}
//			return award.awardItemId;
//		}else if((closeId1 == award.lowId && (closeId2 == award.middleAId||closeId2 == award.middleBId))||
//				(closeId2 == award.lowId && (closeId1 == award.middleAId||closeId1 == award.middleBId))){
//			//关闭一个低级一个中级物品     剩余中级50%  高A 40%  高B 10%
//			if(seed<50){
//				if(closeId1 == award.lowId){
//					if(closeId2 == award.middleAId){
//						award.awardItemId = award.middleBId;
//					}else{
//						award.awardItemId = award.middleAId;
//					}
//				}else{
//					if(closeId1 == award.middleAId){
//						award.awardItemId = award.middleBId;
//					}else{
//						award.awardItemId = award.middleAId;
//					}
//				}
//			}else if(50 <= seed && seed < 90){
//				award.awardItemId = award.highAId;
//			}else{
//				award.awardItemId = award.highBId;
//			}
//			return award.awardItemId;
//		}else if((closeId1 == award.lowId && (closeId2 == award.highAId||closeId2 == award.highBId))||
//				(closeId2 == award.lowId && (closeId1 == award.highAId||closeId1 == award.highBId))){
//			//关闭一个低级一个高级物品  中A 60% 中B 30%  高 10%
//			if(seed<60){
//				award.awardItemId = award.middleAId;
//			}else if(60 <= seed && seed < 90){
//				award.awardItemId = award.middleBId;
//			}else{
//				if(closeId1 == award.lowId){
//					if(closeId2 == award.highAId){
//						award.awardItemId = award.highBId;
//					}else{
//						award.awardItemId = award.highAId;
//					}
//				}else{
//					if(closeId1 == award.highAId){
//						award.awardItemId = award.highBId;
//					}else{
//						award.awardItemId = award.highAId;
//					}
//				}
//				award.awardItemId = award.highBId;
//			}
//			return award.awardItemId;
//		}else{
//			award.awardItemId = award.lowId;
			return award.awardItemId;
//		}
	}
	
	public synchronized void setCloseId(Player p,int closeId1,int closeId2){
		Award award = playerId2Award.get(p.id);
		if(award!=null){
			award.closeId1 = closeId1;
			award.closeId2 = closeId2;
		}
	}
	
	public synchronized void resetAwardItemId(Player p){
		Award award = playerId2Award.get(p.id);
		if(award==null)
			award = new Award(p.id);
		playerId2Award.put(p.id,award);
		award.awardItemId=0;
	}
	
	/**
	 * 打开界面请求物品列表
	 */
	public synchronized void  requestAward(Player p)throws AwardException{
		Award award = playerId2Award.get(p.id);
		if(award==null)
			award = new Award(p.id);
		playerId2Award.put(p.id,award);
		//发送物品列表协议
		Packet pt = new Packet(OpCode.GET_AWARD_ITEMS_SERVER); 
		pt.putInt(OpCode.GET_AWARD_ITEMS_SERVER);
		pt.putInt(award.awardItemId);
		for(ItemCount ic:award.items){
			GameItem item = ic.item;
			int state = (item.template.id == award.closeId1||item.template.id == award.closeId2)?0:1;
			pt.put(state);
			if(state!=0){
				pt.putInt(item.template.id);
				pt.putString(item.template.name);
				pt.putInt(item.template.showType);
			}
		}
		p.send(pt);
	}
	
	/**
	 * 根据人物条件获得奖品列表
	 */
	public void getItemsByPlayer(Award award){
		int low1 = random.nextInt(low.size());
		int middleA = random.nextInt(middle.size());
		int middleB = random.nextInt(middle.size());
		while(middleB == middleA){
			middleB = random.nextInt(middle.size());
		}
		int middleC = random.nextInt(middle.size());
		while(middleC == middleB||middleC==middleA){
			middleC = random.nextInt(middle.size());
		}
		int high1 = random.nextInt(high.size());
		award.lowId = this.low.get(low1).item.template.id;
		award.middleAId = this.middle.get(middleA).item.template.id;
		award.middleBId = this.middle.get(middleB).item.template.id;
		award.middleCId = this.middle.get(middleC).item.template.id;
		award.highId = this.high.get(high1).item.template.id;
		award.items.add(this.low.get(low1));
		award.items.add(this.middle.get(middleA));
		award.items.add(this.middle.get(middleB));
		award.items.add(this.middle.get(middleC));
		award.items.add(this.high.get(high1));
	}
	
	public boolean isGetAward(int playerId){
		return getAwardPlayers.contains(playerId);
	}
	
	public boolean hasGetAward(Player p){
		return (p.vipLevel<3&&p.pool.getInt(PROPERTY_GETAWARD_NUM, 0)>=1) ||((p.vipLevel>=3)&& p.pool.getInt(PROPERTY_GETAWARD_NUM, 0)>=2);
	}
	
	public synchronized void processCharge(Player player){
		if(player.pool.getInt(PROPERTY_GETAWARD_NUM, 0)==1){
			if(isGetAward(player.id)){
				Iterator<Integer> it = getAwardPlayers.iterator();
				while(it.hasNext()){
					if(it.next() == player.id){
						it.remove();
					}
				}
			}
			if(!isOnLineOneHour(player.id)){
				canGetAwardPlayers.add(player.id);
			}
			if(!Server.isStepServer && !player.isInStep){
				Packet pt = new Packet(OpCode.ONLINE_ONEHOUR_SERVER); 
				pt.putInt(OpCode.ONLINE_ONEHOUR_SERVER);
				int totalTime = 1;
				int getAwardCount = player.pool.getInt(PROPERTY_GETAWARD_NUM, 0);
				if(player.vipLevel>=3){
					totalTime = 2;
				}
				pt.putInt(totalTime-getAwardCount);
				player.send(pt);
			}
		}
	}
	public boolean isOnLineOneHour(int playerId){
		return canGetAwardPlayers.contains(playerId);
	}
	
	
	//一次抽奖的结构
	class Award{
		
		int playerId;
		
		int lowId;
		
		int middleAId;
		
		int middleBId;
		
		int middleCId;
		
		int highId;
		
		int awardItemId = 0;//抽中物品
		
		int closeId1=-1;
		
		int closeId2=-1;
		
		List<ItemCount> items = new ArrayList<ItemCount>();
		
		public Award(int playerId){
			this.playerId = playerId;
			getItemsByPlayer(this);
		}
	}
	
	class ItemCount{
		GameItem item;
		int count;
		int rate;
		public ItemCount(GameItem item,int count,int rate){
			this.item = item;
			this.count = count;
			this.rate = rate;
		}
	}
}
