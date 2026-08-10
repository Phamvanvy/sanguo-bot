package peony.service.ranking;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import peony.db.RankingDAO;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.cards.CardService;
import peony.service.shop.ShopService;
import peony.service.stat.StatService;

/**
 * 排名服务
 * @author pmeng
 */
public class RankingService implements Service,ServiceEventListener{
	
	public static String TYPE_RONGYUTA = "YONGYUTA";
	
	protected List<Ranking> topTwenty = new ArrayList<Ranking>();//缓存前二十名
	
	public Map<Integer,Ranking> playerRankings = new ConcurrentHashMap<Integer,Ranking>();//查询过的玩家缓存十分钟
	
	protected Map<Integer,Long> playerid2Time = new ConcurrentHashMap<Integer,Long>();//缓存玩家查询时间
	
	public static int MINUTE_TEN = 30 * 60 * 1000;//十分钟
	
	public static int STATE_NO_GRANKING = 0;//没有排名
	
	public static int STATE_NOT_TWENTY = 1;//有排名但不在前二十
	
	public static int STATE_IN_TWENTY = 2;//在前二十名
	
	public static int TOP_NUMBER = 20; //取荣誉塔排名数
	
	public Long lastChectTime = 0L;
	
	//摇卡排行榜
	public static String TYPE_ROCKCARD = "ROCKCARDEXP";
	public List<Ranking> oldCardExpTopTen = new ArrayList<Ranking>();//缓存上一天前十名
	public List<Ranking> newCardExpTopTen = new ArrayList<Ranking>();//缓存当天前十名
	public int BASE_ROCKCOUNT = 0;
	public int[] ROCKCOUNT_REWARD = null;
	protected int day = Time.day;
	public int[] rewards = new int[20];
	
	//祈福排行榜
	public static String TYPE_PRAY = "PAYPRAY";
	protected List<Ranking> oldPrayTopTen = new ArrayList<Ranking>();//缓存上一天前十名
	protected List<Ranking> newPrayTopTen = new ArrayList<Ranking>();//缓存当天前十名
	public static String PROPERTY_ADDPRAY_EVERYDAY = "addprayday";  //玩家每日获取祈福经验
	public static String PROPERTY_PRAYCOUNT_EVERYDAY = "praycountday";  //玩家每日的祈福次数
	public int BASE_PRAYCOUNT = 0;
	public int[] PRAYCOUNT_REWARD = null;
//    public static Map<Integer,List<Integer>> topPray = new HashMap<Integer,List<Integer>>();
	public int[] prayRewards = new int[20];
	
	public void shutdown() {
		
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
		topTwenty = Server.server.getServiceRegistry().getDbService().rankingDAO.findTopTwenty();
		initCardExpRanking();
	}
	

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_CYCLEINSTANCE_FINISH
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
			case ServiceEvent.EVENT_CYCLEINSTANCE_FINISH:
				processCycleInstance(((Player)event.param1),(Integer)event.param2);
				break;

		}
	}
	
	public void update(){
//		if(day!=Time.day){
//			day = Time.day;
//			updateCardExp();
//			topRockCard.clear();
//		}
		if(System.currentTimeMillis() - lastChectTime > 60 * 1000 * 5){
			lastChectTime = System.currentTimeMillis();
			Iterator<Ranking> it = playerRankings.values().iterator();
			if(it.hasNext()){
				Ranking r = it.next();
				if(System.currentTimeMillis() - playerid2Time.get(r.playerId) > MINUTE_TEN){
					//删除查询时间的缓存
					playerid2Time.remove(r.playerId);
					//删除排名缓存
					it.remove();
				}
			}
		}
	}
	
	public void processCycleInstance(Player player,int value){
//		Ranking rk = playerRankings.get(player.id);
//		RankingDAO dao = Server.server.getServiceRegistry().getDbService().rankingDAO;
//		Ranking rank = isInTopTen(player);
//		if(rank != null){//在前十名中
//			//比以前成绩好  更新数据库并重新排序
//			if(rank.value < value){
//				rank.value = value;
//				dao.updateEntity(rank);
//				updateGranking1(rank);
//			}
//		}else{
//			if(rk == null){
//				//是否有这个人记录 若有更新层数   若没有插入数据
//				rk = dao.findRankingByPlayerId(player.id);
//				if(rk == null){
//					Ranking newRk = new Ranking();
//					newRk.playerId = player.id;
//					newRk.playerName = player.name;
//					newRk.type = RankingService.TYPE_RONGYUTA;
//					newRk.value = value;
//					newRk.time = new Date();
//					dao.newEntity(newRk);
//					rk = newRk;
//				}else{
//					if(rk.value < value){
//						rk.value = value;
//						dao.updateEntity(rk);
//					}
//				}
//			}else{
//				if(rk.value < value){
//					rk.value = value;
//					dao.updateEntity(rk);
//				}
//			}
//			if(rk!=null){
//				updateGranking2(rk);
//			}
//		}
		Server.server.getServiceRegistry().getDbService().
        schedule(new PlayerCycleRackingRecordCall(player==null ? null : player.session, player,value));
//		Server.server.getWorld().schedule(new PlayerCycleRackingRecordCall(player.session, player, value));
	}
	
	/**
	 * 以前成绩在前二十 成绩提升后更新top20
	 */
	public void updateGranking1(Ranking rank){
		int index = topTwenty.indexOf(rank);
		for(int i = index - 1;i >= 0;i--){
			Ranking nextRank = topTwenty.get(i);
			if(nextRank.value < rank.value){
				topTwenty.set(i, rank);
				topTwenty.set(i + 1, nextRank);
			}else{
				break;
			}
		}
	}
	
	/**
	 * 以前成绩不在前二十 成绩更新后更新top20
	 */
	public void updateGranking2(Ranking rank){
		if(topTwenty.size() == 0){
			topTwenty.add(rank);
		}else{
			Ranking lastRank = topTwenty.get(topTwenty.size() - 1);
			if(rank.value > lastRank.value){
				if(topTwenty.size() >= TOP_NUMBER){
					topTwenty.set(TOP_NUMBER-1, rank);
				}else{
					topTwenty.add(rank);
				}
				//最后一个元素排序
				for(int i = topTwenty.size() -1;i > 0;i--){
					Ranking nextRank = topTwenty.get(i - 1);
					if(nextRank.value < rank.value){
						topTwenty.set(i, nextRank);
						topTwenty.set(i - 1, rank);
					}
				}
			}else{
				if(topTwenty.size() < TOP_NUMBER){
					topTwenty.add(rank);
				}
			}
		}
	}
	
	/**
	 * 判断一个玩家是否在前十名中
	 */
	public Ranking isInTopTwenty(Player player){
		for(int i = 0;i < topTwenty.size();i++){
			Ranking r = topTwenty.get(i);
			if(r.playerId == player.id)
				return r;
		}
		return null;
	}
	
	/**
	 * 得到前十名
	 */
	public List<Ranking> getTopTwenty(){
		return topTwenty;
	}
	
	/**
	 * 在缓存中查找某个玩家
	 */
	public Ranking getPlayerRankInCache(int playerId){
		return playerRankings.get(playerId);
	}
	
	/**
	 * 将从DB中查到的数据放入cache中
	 */
	public void putRankToCache(Ranking rank){
		playerRankings.put(rank.playerId, rank);
		playerid2Time.put(rank.playerId, System.currentTimeMillis());
	}
	
	/**
	 * 上一天摇卡排行榜
	 * @return
	 */
	public List<Ranking> getOldCardRanking(){
		return oldCardExpTopTen;
	}
	
	/**
	 * 当天摇卡记录
	 * @return
	 */
	public List<Ranking> getNewCardRanking(){
		return newCardExpTopTen;
	}
	
	/**
	 * 以前成绩在前十 成绩提升后更新top10
	 */
	public void updateRanking1(Ranking rank,List<Ranking> rankingTopTen){
		int index = rankingTopTen.indexOf(rank);
		for(int i = index - 1;i >= 0;i--){
			Ranking nextRank = rankingTopTen.get(i);
			if(nextRank.value < rank.value){
				rankingTopTen.set(i, rank);
				rankingTopTen.set(i + 1, nextRank);
			}else{
				break;
			}
		}
	}
	
	/**
	 * 以前成绩不在前十 成绩更新后更新top10
	 */
	public void updateRanking2(Ranking rank,List<Ranking> rankingTopTen){
		if(rankingTopTen.size() == 0){
			rankingTopTen.add(rank);
		}else{
			Ranking lastRank = rankingTopTen.get(rankingTopTen.size() - 1);
			if(rank.value > lastRank.value){
				if(rankingTopTen.size() >= 10){
					rankingTopTen.set(9, rank);
				}else{
					rankingTopTen.add(rank);
				}
				//最后一个元素排序
				for(int i = rankingTopTen.size() -1;i > 0;i--){
					Ranking nextRank = rankingTopTen.get(i - 1);
					if(nextRank.value < rank.value){
						rankingTopTen.set(i, nextRank);
						rankingTopTen.set(i - 1, rank);
					}
				}
			}else{
				if(rankingTopTen.size() < 10){
					rankingTopTen.add(rank);
				}
			}
		}
	}
	
	/**
	 * 判断一个玩家是否在前十名中
	 */
	public Ranking isInTopTen(Player player,List<Ranking> rankingTopTen){
		for(int i = 0;i < rankingTopTen.size();i++){
			Ranking r = rankingTopTen.get(i);
			if(r.playerId == player.id)
				return r;
		}
		return null;
	}
	
	/**
	 * 换天时处理卡片经验排行榜
	 */
	public void updateCardExp(){
		oldCardExpTopTen.clear();
		if(newCardExpTopTen!=null && newCardExpTopTen.size()>0){
			for(int i=0;i<newCardExpTopTen.size();i++){
				if(i>9)
					break;
				Ranking rank = newCardExpTopTen.get(i);
				oldCardExpTopTen.add(rank);
				try{
					if(rewards[2*i]!=0){
						int itemId = rewards[2*i];
						int count = rewards[2*i+1];
						GameItem item = ObjectAccessor.createGameItem(itemId);
						if(item!=null){
							Server.server.getServiceRegistry().getMailService()
							.sendSystemMail(rank.playerId, peony.Messages.STRING_00004,"摇卡排行榜", MessageFormat.format("恭喜您获得排行榜第{0}名", i+1), 0,
									item, count, "CARDEXPRANKING");
						}
					}
				}catch(Exception e){
					
				}
			}
		}
		newCardExpTopTen.clear();
	}
	
	/**
	 * 服务器启动时删除过期排行榜数据
	 */
	public void processObsoleteCardRank(){
		try{
			Date date = Time.getDateLastDay(new Date());
			RankingDAO dao = Server.server.getServiceRegistry().getDbService().rankingDAO;
			List<Ranking> ob = dao.getObsoleteCardRanking(date);
			if(ob!=null){
				for(Ranking o:ob){
					dao.makeTransient(o);
				}
			}
		}catch(Exception e){
			
		}
	}
	
	/**
	 * 服务器启动时从db里取出排行榜数据
	 */
	public void initCardExpRanking(){
		try{
			Date time = Time.getDateToday(new Date());
			Date oldTime = Time.getDateLastDay(new Date());
			processObsoleteCardRank();  //维护时移除过期卡片经验记录
			//取得前一天卡片经验记录
			oldCardExpTopTen = Server.server.getServiceRegistry().getDbService().rankingDAO.findCardTopTenOld(oldTime,time);  
			//取得当天卡片经验记录
			newCardExpTopTen = Server.server.getServiceRegistry().getDbService().rankingDAO.findCardTopTenNew(time);
			
			processObsoletePrayRank();  //维护时移除过期祈福记录 
			//取得前一天卡片经验记录
			oldPrayTopTen = Server.server.getServiceRegistry().getDbService().rankingDAO.findPrayTopTenOld(oldTime,time);  
			//取得当天卡片经验记录
			newPrayTopTen = Server.server.getServiceRegistry().getDbService().rankingDAO.findPrayTopTenNew(time);
		}catch(Exception e){
			
		}
	}
	
	/**
	 * 摇卡次数奖励
	 * @param player
	 */
	public void rockCountReward(Player player){
		if(BASE_ROCKCOUNT!=0){
			while(player.rockCardCount/BASE_ROCKCOUNT>0){
				if(ROCKCOUNT_REWARD!=null){
					GameItem item = ObjectAccessor.createGameItem(ROCKCOUNT_REWARD[0]);
					int count = ROCKCOUNT_REWARD[1];
					if(item!=null){
						Server.server.getServiceRegistry().getMailService()
						.sendSystemMail(player.id, peony.Messages.STRING_00004,"摇卡次数奖励", MessageFormat.format("摇卡次数满{0}次获得奖励", BASE_ROCKCOUNT), 0,
								item, count, "CARDROCKCOUNT");
						player.rockCardCount-=BASE_ROCKCOUNT;
					}
				}
			}
		}
	}
	
	/**
	 * 玩家登录时初始化摇卡次数
	 * @param player
	 */
	public void playerLoadRockCount(Player player){
		player.rockCardCount = player.pool.getInt(CardService.PROPERTY_ROCKCARDCOUNT_EVERYDAY, 0);
//		if(BASE_ROCKCOUNT!=null&&BASE_ROCKCOUNT.length>0){
//			for(int i=0;i<BASE_ROCKCOUNT.length;i++){
//				if(player.rockCardCount>=BASE_ROCKCOUNT[i]){
//					List<Integer> list = topRockCard.get(i);
//					if(list == null){
//						list = new ArrayList<Integer>();
//					}
//					if(list!=null && !list.contains(player.id) && ROCKCOUNT_REWARD!=null && 2*i<ROCKCOUNT_REWARD.length){
//						list.add(player.id);
//						topRockCard.put(i, list);
//					}
//				}
//			}
//		}
	}
	
	/**
	 * 玩家登录时初始化祈福次数
	 * @param player
	 */
	public void playerLoadPrayCount(Player player){
		player.prayCount = player.pool.getInt(PROPERTY_PRAYCOUNT_EVERYDAY, 0);
//		if(BASE_PRAYCOUNT!=null&&BASE_PRAYCOUNT.length>0){
//			for(int i=0;i<BASE_PRAYCOUNT.length;i++){
//				if(player.prayCount>=BASE_PRAYCOUNT[i]){
//					List<Integer> list = topPray.get(i);
//					if(list == null){
//						list = new ArrayList<Integer>();
//					}
//					if(list!=null && !list.contains(player.id) && PRAYCOUNT_REWARD!=null && 2*i<PRAYCOUNT_REWARD.length){
//						list.add(player.id);
//                        
//					}
//				}
//			}
//		}
	}
	
	/**
	 * 祈福次数奖励
	 * @param player
	 */
	public synchronized void prayCountReward(Player player){
		if(BASE_PRAYCOUNT!=0 && player.prayCount>0){
		    while(player.prayCount/BASE_PRAYCOUNT>0){
		    	if(PRAYCOUNT_REWARD!=null){
					GameItem item = ObjectAccessor.createGameItem(PRAYCOUNT_REWARD[0]);
					int count = PRAYCOUNT_REWARD[1];
					if(item!=null){
						Server.server.getServiceRegistry().getMailService()
						.sendSystemMail(player.id, peony.Messages.STRING_00004,"祈福次数奖励", MessageFormat.format("祈福次数满{0}次获得奖励", BASE_PRAYCOUNT), 0,
								item, count, "PRAYCOUNT");
						player.prayCount-=BASE_PRAYCOUNT;
					}
				}
		    }
		}
	}
	
	public List<Ranking> getNewPrayRanking(){
		return newPrayTopTen;
	}
	
	public List<Ranking> getOldPrayRanking(){
		return oldPrayTopTen;
	}
	
	/**
	 * 换天处理祈福排行榜
	 */
	public void updatePray(){
		oldPrayTopTen.clear();
		if(newPrayTopTen!=null && newPrayTopTen.size()>0){
			for(int i=0;i<newPrayTopTen.size();i++){
				Ranking rank = newPrayTopTen.get(i);
				oldPrayTopTen.add(rank);
				if(prayRewards[2*i]!=0){
					int itemId = prayRewards[2*i];
					int count = prayRewards[2*i+1];
					GameItem item = ObjectAccessor.createGameItem(itemId);
					if(item!=null){
						Server.server.getServiceRegistry().getMailService()
						.sendSystemMail(rank.playerId, peony.Messages.STRING_00004,"祈福排行榜", MessageFormat.format("恭喜您获得排行榜第{0}名", i+1), 0,
								item, count, "PRAYRANKING");
					}
				}
			}
		}
		newPrayTopTen.clear();
	}
	
	/**
	 * 处理玩家祈福逻辑
	 * @param player
	 * @param limit
	 * @param itemId
	 */
	public synchronized void processPrayRanking(Player player,int limit,int itemId){
//		synchronized(newPrayTopTen){
			try{
				ShopService shopService = Server.server.getServiceRegistry().getShopService();
				int addPay = (int)shopService.getItemPrice(itemId);
				player.prayCount+=limit;
				player.payForPray+=limit*addPay;
				List<Ranking> topTen = getNewPrayRanking();
				RankingDAO dao = Server.server.getServiceRegistry().getDbService().rankingDAO;
				if(topTen==null){
					topTen = new ArrayList<Ranking>();
				}
				Ranking rank = isInTopTen(player,topTen);
				if(rank!=null){
					if(rank.value < player.payForPray){
						rank.setValue(player.payForPray);
						rank.setValue2(player.prayCount);
						rank.setTime(new Date());
						dao.updateEntity(rank);
						updateRanking1(rank,topTen);
					}
				}else{
					if(topTen.size()<10){
						Ranking ranking = new Ranking();
						ranking.setPlayerId(player.id);
						ranking.setPlayerName(player.name);
						ranking.setTime(new Date());
						ranking.setType(RankingService.TYPE_PRAY);
						ranking.setValue(player.payForPray);
						ranking.setValue2(player.prayCount);
						ranking.setFaction(player.faction);
						dao.newEntity(ranking);
						topTen.add(ranking);
						if(topTen.size()>1){
							updateRanking1(ranking, topTen);
						}
					}else{
						Ranking lastRanking = topTen.get(topTen.size()-1);
						if(lastRanking.value<player.payForPray){
							dao.makeTransient(lastRanking);
						    topTen.remove(topTen.size()-1);
						    Ranking ranking = new Ranking();
							ranking.setPlayerId(player.id);
							ranking.setPlayerName(player.name);
							ranking.setTime(new Date());
							ranking.setType(RankingService.TYPE_PRAY);
							ranking.setValue(player.payForPray);
							ranking.setValue2(player.prayCount);
							ranking.setFaction(player.faction);
							dao.newEntity(ranking);
							topTen.add(ranking);
							updateRanking1(ranking, topTen);
						}
					}
				}
			}catch(Exception e){
				
			}
//		}
	}
	
	/**
	 * 服务器启动时删除过期祈福排行榜数据
	 */
	public void processObsoletePrayRank(){
		try{
			Date date = Time.getDateLastDay(new Date());
			RankingDAO dao = Server.server.getServiceRegistry().getDbService().rankingDAO;
			List<Ranking> ob = dao.getObsoletePrayRanking(date);
			if(ob!=null){
				for(Ranking o:ob){
					dao.makeTransient(o);
				}
			}
		}catch(Exception e){
			
		}
	}
	
	/**
	 * 处理各排行榜换天逻辑
	 */
	public void resetRanking(){
		updateCardExp();
		updatePray();
	}

}
