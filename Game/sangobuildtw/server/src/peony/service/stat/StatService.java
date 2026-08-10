package peony.service.stat;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import peony.game.Actor;
import peony.game.CommonUtil;
import peony.game.DayListener;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.Horse;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PropertyPool;
import peony.game.Server;
import peony.game.TransactionBagGrid;
import peony.game.Unit;
import peony.game.itemenhance.ItemEnhance;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.fame.Fame;
import peony.service.fame.FameService;
import peony.service.friend.PlayerRelation;
import peony.service.tong.TongMember;
import peony.service.tong.TongService;
import peony.vm.ASMQuest;

public class StatService implements Service,DayListener,ServiceEventListener{
	
	private static final Logger log = Logger.getLogger(StatService.class);
	
	public static int TOP_COUNT = 20;
	
	public static long TIMENOW = System.currentTimeMillis();
	
	protected Map<Integer,PvpInfo> pvpInfos = new HashMap<Integer,PvpInfo>(); 
	protected BlockingQueue<Object> kills = new LinkedBlockingQueue<Object>();
	protected Map<Integer, List<Achievement>> achievement = new HashMap<Integer, List<Achievement>>();

	@SuppressWarnings("unchecked")
	protected List[] topPvpInfos = new List[4];
	
	@SuppressWarnings("unchecked")
	protected List[] topWeekRanks = new List[4];
	
	@SuppressWarnings("unchecked")
	protected List[] topLevelRanks = new List[4];
	
//	protected List<Actor> topWeekRanks = null;
	
	public static String PROPERTY_PLAYER_LEVEL = "playerlevel"; // 玩家级别
	public static String PROPERTY_EQUIPHASJEWEL = "equiphasjewel"; // 装备镶嵌宝石
	public static String PROPERTY_RANK = "playerrank"; // 玩家军衔
	public static String PROPERTY_NATION_COLLECT = "nationcollect"; // 向国库捐献金钱
	public static String PROPERTY_PLAYER_MONEY = "playermoney"; // 玩家金钱数量
	public static String PROPERTY_MONEY_WAN = "wanyuanhu"; //玩家成为万元户
	public static String PROPERTY_MONEY_MILLIONARE="baiwanfuwen";//玩家为百万富翁
	public static String PROPERTY_MARRIAGE = "playermarriage"; // 玩家结婚
	public static String PROPERTY_MONEY_MOST = "moneymost"; // 金钱数量达到服务器最多
	public static String PROPERTY_JEWELON_EQUIPMENT = "jewelonequipment"; //装备上镶嵌有7级宝石
	public static String PROPERTY_QUEST_77 = "quest77"; // 完成七夕任务
	public static String PROPERTY_QUEST_MIDAUTUMN = "questmidautumn"; // 完成中秋任务
	public static String PROPERTY_QUEST_OWE = "questowe"; // 完成感恩任务
	public static String PROPERTY_QUEST_ZHUSHASHOUE = "zhushashoue"; // 完成诛杀首恶任务
	public static String PROPERTY_QUEST_YUXI = "yuxi"; // 完成搜寻玉玺任务
	public static String PROPERTY_QUEST_TIANLONG = "tianlong"; // 完成大破天龙阵任务
	public static String PROPERTY_QUESTS_COUNT = "questscount"; // 完成任务数量
	public static String PROPERTY_KILL_WEI = "killwei"; // 杀死魏国人总数
	public static String PROPERTY_KILL_SHU = "killshu"; // 杀死蜀国人总数
	public static String PROPERTY_KILL_WU = "killwu"; // 杀死吴国人总数
	public static String PROPERTY_KILLEDBY_WEI = "killedbywei"; // 被魏国杀死总次数
	public static String PROPERTY_KILLEDBY_SHU = "killedbyshu"; // 被蜀国杀死总次数
	public static String PROPERTY_KILLEDBY_WU = "killedbywu"; // 被吴国杀死总次数
	public static String PROPERTY_AUCTION_SUCCESS = "auctionsuccess";//玩家拍卖成功记录
	public static String PROPERTY_ADDJWEL_SUCCESS = "addjewel";//玩家镶嵌宝石成功
	public static String PROPERTY_FINISHTIME_LEVELTHIRTY = "levelthirty";//玩家到达等级30的时间
	public static String PROPERTY_FINISHTIME_LEVELFIFTY = "levelfifty";//玩家到达等级50的时间
	public static String PROPERTY_FINISHTIME_LEVELSEVENTY = "levelseventy";//玩家到达等级70的时间
	public static String PROPERTY_FINISHTIME_QUESTQIXI = "quesqixi";//完成七夕任务的时间
	public static String PROPERTY_FINISHTIME_QUESTMID = "questmid";//完成中秋任务的时间
	public static String PROPERTY_FINISHTIME_QUESTOWE = "quesowe";//完成感恩任务的时间
	public static String PROPERTY_FINISHTIME_ADDJEWEL = "jeweltime";//镶了七级宝石的时间
	public static String PROPERTY_FINISHTIME_ZHUSHA = "zhusha";//完成诛杀首恶任务的时间
	public static String PROPERTY_FINISHTIME_YUXI = "yux";//完成玉玺任务的时间
	public static String PROPERTY_FINISHTIME_QUESTTIANLONG = "questtianlong";//完成天龙阵任务的时间
	public static String PROPERTY_FINISHTIME_PERFECT = "perfect";//完成任务数量为500的时间
	public static String PROPERTY_FINISHTIME_KILL = "kill";//击杀敌国100人的时间
	public static String PROPERTY_FINISHTIME_KILLMORE = "killmore";//击杀敌国500人的时间
	public static String PROPERTY_FINISHTIME_COLLECT = "collect";//向国家捐献1000000的时间
	public static String PROPERTY_FINISHTIME_TENTHOU = "tenthousand";//玩家有10000金钱的时间
	public static String PROPERTY_FINISHTIME_MILLIONARE = "milliontime";//玩家有1000000金钱的时间
	public static String PROPERTY_FINISHTIME_MARRIAGE = "marriagetime";//玩家结过一次婚的时间
	public static String PROPERTY_FINISHTIME_AUCTION = "business";//玩家成功进行一次拍卖的时间
	public static String PROPERTY_FINISHTIME_COUNT30 = "jewelcnt30";//玩家身上有30颗宝石
	public static String PROPERTY_FINISHTIME_COUNT40 = "jewelcnt40";//玩家身上有40颗宝石
	public static String PROPERTY_FINISHTIME_COUNT50 = "jewelcnt50";//玩家身上有50颗宝石
	public static String PROPERTY_FINISHTIME_7COUNT50 = "jewel7cnt50";//玩家身上有50颗七级宝石
	public static String PROPERTY_FINISHTIME_6COUNT50 = "jewel6cnt50";//玩家身上有50颗六级宝石
	public static String PROPERTY_JEWEL_COUNT = "jewelcnt";//玩家身上的宝石数
	public static String PROPERTY_FINISHTIME_SEV = "jewel7ji";
	public static String PROPERTY_JEWELSIX_COUNT = "jewel6cnt";//玩家身上6级宝石数
	public static String PROPERTY_JEWELSEVEN_COUNT = "jewel7cnt";//玩家身上7级宝石数
	public static String PROPERTY_AUCSELL_COUNT = "aucsellcnt";//玩家成功售出物品的次数
	public static String PROPERTY_AUCBUY_COUNT = "aucbuycnt";//玩家成功售出物品的次数
	public static String PROPERTY_AUCSELLMONEY_COUNT = "aucsellmoney";//玩家成功售出物品获得的金钱
	public static String PROPERTY_AUCBUYMONEY_COUNT = "aucbuymoney";//玩家成功售出物品获得的金钱
	public static String PROPERTY_IMONEY_COUNT = "haveimoney";//玩家随身携带的i币数
	public static String PROPERTY_IMONEYUSE_COUNT = "useimoney";//玩家消费的i币数
	
	
	
	
	
	public boolean runStat = true;
	
	public int[] questIds = {1895,1896,1897,1690,1699,1670,1751,161,382,1652}; //被监听的任务ID
	
	public String[] questProperty = {
			PROPERTY_QUEST_77,PROPERTY_QUEST_77,PROPERTY_QUEST_77,PROPERTY_QUEST_MIDAUTUMN,
			PROPERTY_QUEST_OWE,PROPERTY_QUEST_OWE,PROPERTY_QUEST_OWE,
			PROPERTY_QUEST_ZHUSHASHOUE,PROPERTY_QUEST_YUXI,PROPERTY_QUEST_TIANLONG
			}; //对应于被监听任务
	public String[] timeProperty = {
			PROPERTY_FINISHTIME_QUESTQIXI,PROPERTY_FINISHTIME_QUESTQIXI,PROPERTY_FINISHTIME_QUESTQIXI,
			PROPERTY_FINISHTIME_QUESTMID,PROPERTY_FINISHTIME_QUESTOWE,PROPERTY_FINISHTIME_QUESTOWE,PROPERTY_FINISHTIME_QUESTOWE,
			PROPERTY_FINISHTIME_ZHUSHA,PROPERTY_FINISHTIME_YUXI,PROPERTY_FINISHTIME_QUESTTIANLONG
	        };//对应于被监听任务的完成时间
	
	public String[] questName = {"情牽七夕","情牽七夕","情牽七夕","中秋佳節","感恩戴義","感恩戴義","感恩戴義","為國效力","摸金校尉","大破天龍陣"};
	
	public int[] clazzs = {0,1,2,3}; // 代表四种职业
	
	public int[] faction = {GameObject.FACTION_WEI,GameObject.FACTION_SHU,GameObject.FACTION_WU};
	
	int[] imoney = {1,100,10000,58888,588888};//消费的i币数
	
	int[] horseEqu = {1,3,7};//统计的马装的件数
	
	public int[] kkgIds = {1503,1504,1502};//暗杀国君的三个国家的任务id,按魏，蜀，吴排列
	
	/** 获取暗杀敌君的序号 */
	public int index(int questId){
		for(int i=0;i<kkgIds.length;i++){
			if(questId == kkgIds[i])
				return i;
		}
		return 0;
	}

	public int[] getEventTypes() {
		return new int[] {
				ServiceEvent.EVENT_UNIT_DIE,
				ServiceEvent.EVENT_PLAYER_CHANGE_FACTION,
				ServiceEvent.EVENT_PLAYER_MONEY_UP,
				ServiceEvent.EVENT_PLAYER_MARRIAGE,
				ServiceEvent.EVENT_NATIONCOLLECT,
				ServiceEvent.EVENT_RANK_UP,
				ServiceEvent.EVENT_PLAYER_LEVELUP,
				ServiceEvent.EVENT_ADDJEWEL_SUCCESS,
				ServiceEvent.EVENT_PLAYER_LOGINED,
				ServiceEvent.EVENT_IBUY,
				ServiceEvent.EVENT_HORSE_LEVELUP,
				ServiceEvent.EVENT_HORSE_RIDE,
				ServiceEvent.EVENT_HORSE_EQUIP,
		
		};
	}

	public void handleEvent(ServiceEvent event) {
		kills.add(event);
	}
	
	protected void playerRankUp(Player p, int rank){
		
	}
	
	/**
	 * 历遍检查玩家是否完成被监听的任务
	 * @param p
	 */
	protected void processQuest(Player p){
	  PvpInfo pvpInfo = getPvpInfo(p.id, p.faction);
	  if(p!=null){
		 for(int i=0;i<kkgIds.length;i++){
			int questId = kkgIds[i];
			if (p.asmVm.taskFinished(questId) == 1) {
				long time = p.asmVm.getFinishTime(questId);
					int ide = index(questId);
						int index = getIndex(p.faction,faction[ide]);
						if(pvpInfo.pool.getString(getPropertyOfKillKing(index)) == ""){
							 pvpInfo.pool.setString(getPropertyOfKillKing(index), getFinishTime(time));
						}
			}
		  }
		  for (int i = 0; i < questIds.length; i++) {
			int questId = questIds[i];
			if (p.asmVm.taskFinished(questId) == 1) {
			  long time = p.asmVm.getFinishTime(questId);
			  String timeStr = getTimeProperty(questId);
			  TongService ser = Server.server.getServiceRegistry().getTongService();
			  TongMember tm = ser.getPlayerInfo(p.id);
			  if(pvpInfo.pool.getString(timeStr)==""){
				 pvpInfo.pool.setString(timeStr, getFinishTime(time));
				 String msg = MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>{1}</c>成就", p.name,questName[i]);
				 if(tm!=null){
					Server.server.getServiceRegistry().getChatService()
					.sendGuildSystemMessage(msg,tm.tongID);
				 }
				 Server.server.getServiceRegistry().getChatService().sendWorldMessage(msg);
			   }
			 }
		  }
	   }
	}
	
	public String getFinishTime(long time){
		StringBuilder sb = new StringBuilder();
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(time));
		int year = cal.get(Calendar.YEAR);
		int date = cal.get(Calendar.MONTH)+1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		sb.append(year);
		sb.append(".");
		sb.append(date);
		sb.append(".");
		sb.append(day);
		return sb.toString();
	}
	
	/** 获取除跑环任务和每日任务之外玩家完成任务的总数量 **/
	protected int getFinishedQuestsSize(Player p){
		if(p!=null){
			int finished = p.asmVm.getQuests().size();
			for(ASMQuest quest : p.asmVm.getQuests()){
				if(quest.getGameQuest().getCycleInfo()!=null){
					finished--;
				}
				if(quest.getGameQuest().getRepeatType()==3){
					finished--;
				}
			}
			return finished;
		}
		return 0;
	}
	
	protected void playerCollect(Player p, int oldCollect, int money){
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(p.id, p.faction);
			pvpInfo.pool.setLong(PROPERTY_NATION_COLLECT, (oldCollect+money));
			if(oldCollect + money >= 1000000 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_COLLECT) == ""){
				pvpInfo.pool.setString(PROPERTY_FINISHTIME_COLLECT, getFinishTime(TIMENOW));
				TongService service = Server.server.getServiceRegistry().getTongService();
				TongMember tm = service.getPlayerInfo(p.id);
				String msg = MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>向國庫捐款達到1000000</c>成就", p.name);
				if(tm!=null){
				Server.server.getServiceRegistry().getChatService()
				.sendGuildSystemMessage(msg,tm.tongID);
				}
				Server.server.getServiceRegistry().getChatService().sendWorldMessage(msg);
			}
		}
	}
	
	protected void playerMarriaged(Player p, Player mate){
		if(p!=null){
			PvpInfo pvpInfo1 = getPvpInfo(p.id, p.faction);
			PvpInfo pvpInfo2 = getPvpInfo(mate.id, mate.faction);
			if(pvpInfo1.pool.getString(PROPERTY_FINISHTIME_MARRIAGE)==""){
				pvpInfo1.pool.setString(PROPERTY_FINISHTIME_MARRIAGE, getFinishTime(TIMENOW));
				TongService service = Server.server.getServiceRegistry().getTongService();
				TongMember tm = service.getPlayerInfo(p.id);
				String msg = MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>洞房花燭</c>成就", p.name);
				if(tm!=null){
					Server.server.getServiceRegistry().getChatService()
					.sendGuildSystemMessage(msg,tm.tongID);
				}
				Server.server.getServiceRegistry().getChatService().sendWorldMessage(msg);
			}
			if(pvpInfo2.pool.getString(PROPERTY_FINISHTIME_MARRIAGE)==""){
			    pvpInfo2.pool.setString(PROPERTY_FINISHTIME_MARRIAGE, getFinishTime(TIMENOW));
			    TongService service = Server.server.getServiceRegistry().getTongService();
				TongMember tm = service.getPlayerInfo(mate.id);
				String msg = MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>洞房花燭</c>成就", mate.name);
				if(tm!=null){
					Server.server.getServiceRegistry().getChatService()
					.sendGuildSystemMessage(msg,tm.tongID);
				}
				Server.server.getServiceRegistry().getChatService().sendWorldMessage(msg);
		   }
		}
	}
	
	protected void playerMoneyUp(Player p, int oldMoney, int value){
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(p.id, p.faction);
			TongService service = Server.server.getServiceRegistry().getTongService();
			TongMember tm = service.getPlayerInfo(p.id);
			if ((oldMoney + value) >= 10000 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_TENTHOU) == "") {
				pvpInfo.pool.setString(PROPERTY_FINISHTIME_TENTHOU, getFinishTime(TIMENOW));
				String msg = MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>万元戶</c>成就", p.name);
				if(tm!=null){
					Server.server.getServiceRegistry().getChatService()
					.sendGuildSystemMessage(msg,tm.tongID);
			    }
			    Server.server.getServiceRegistry().getChatService().sendAreaSystemMessage(msg,p.map.id);
			} 
			if ((oldMoney + value) >= 1000000 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_MILLIONARE)==""){
				pvpInfo.pool.setString(PROPERTY_FINISHTIME_MILLIONARE, getFinishTime(TIMENOW));
				String msg = MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>百万富翁</c>成就", p.name);
				if(tm!=null){
					Server.server.getServiceRegistry().getChatService()
					.sendGuildSystemMessage(msg,tm.tongID);
				}
				Server.server.getServiceRegistry().getChatService().sendWorldMessage(msg);
			} 
		}
	}
	
	protected void playerChangeFaction(Player player){
		
	}
	
	
	protected void unitDie(Unit u1,Unit u2){
		if(u1.type==GameObject.TYPE_PLAYER&&u2.type==GameObject.TYPE_PLAYER){
			if(u1.faction!=u2.faction){
				addPvpInfo(u1.id, u2.id,u1.faction,u2.faction,u2.clazz,u1.clazz,u2.level,u1.level);
			}
		}
	}
	
	/** 根据任务ID获取变量池中记录的变量类型 **/
	public String getQuestProperty(int questId){
		for(int i=0;i<questIds.length;i++){
			if(questIds[i]==questId)
				return questProperty[i];
		}
		return null;
	}
	/** 根据任务ID获取变量池中记录的变量类型 **/
	public String getTimeProperty(int questId){
		for(int i=0;i<questIds.length;i++){
			if(questIds[i]==questId)
				return timeProperty[i];
		}
		return null;
	}
	
	//统计不同国家PK的人数的增加
	protected void processFactionKill(FactionKill kill){
		PvpInfo killInfo = getPvpInfo(kill.killId, kill.killFaction);
		//杀死敌国总人数统计
		killInfo.pool.setInt(getPropertyByFaction(kill.killedFaction,true)
				, killInfo.pool.getInt(getPropertyByFaction(kill.killedFaction,true),0)+1);
		Player p = (Player)ObjectAccessor.getPlayer(kill.killId);
		if(p!=null){
			int killCount = getKillCount(p);
			TongService service = Server.server.getServiceRegistry().getTongService();
			TongMember tm = service.getPlayerInfo(p.id);
			if(killCount >= 100 && killInfo.pool.getString(PROPERTY_FINISHTIME_KILL) == ""){
				killInfo.pool.setString(PROPERTY_FINISHTIME_KILL, getFinishTime(TIMENOW));
				String msg = MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>英勇殺敵</c>成就", p.name);
				if(tm!=null){
					Server.server.getServiceRegistry().getChatService()
					.sendGuildSystemMessage(msg,tm.tongID);
				}
				Server.server.getServiceRegistry().getChatService().sendWorldMessage(msg);
			}
			if(killCount >= 500 && killInfo.pool.getString(PROPERTY_FINISHTIME_KILLMORE) == ""){
				killInfo.pool.setString(PROPERTY_FINISHTIME_KILLMORE, getFinishTime(TIMENOW));
				String msg = MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>所向披靡</c>成就", p.name);
				if(tm!=null){
					Server.server.getServiceRegistry().getChatService()
					.sendGuildSystemMessage(msg,tm.tongID);
				}
				Server.server.getServiceRegistry().getChatService().sendWorldMessage(msg);
			}
		}
		//杀死敌国不同职业的人数统计
		killInfo.pool.setInt(getPropertyByFactionAndClazz(kill.killedFaction,kill.killedClazz,true)
				, killInfo.pool.getInt(getPropertyByFactionAndClazz(kill.killedFaction,kill.killedClazz,true),0)+1);
		PvpInfo killedInfo = getPvpInfo(kill.killedId, kill.killedFaction);
		//被敌国杀死总次数的统计
		killedInfo.pool.setInt(getPropertyByFaction(kill.killFaction, false)
				, killedInfo.pool.getInt(getPropertyByFaction(kill.killFaction, false),0)+1);
		//被敌国不同职业杀死次数的统计
		killedInfo.pool.setInt(getPropertyByFactionAndClazz(kill.killFaction, kill.killClazz, false)
				, killedInfo.pool.getInt(getPropertyByFactionAndClazz(kill.killFaction, kill.killClazz, false),0)+1);
	}
	
	protected void processEvent(ServiceEvent event){
		switch (event.type) {
		case ServiceEvent.EVENT_UNIT_DIE:
			unitDie((Unit) event.param1, (Unit) event.param2);
			break;
		case ServiceEvent.EVENT_PLAYER_CHANGE_FACTION:
			playerChangeFaction((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_MONEY_UP:
			playerMoneyUp((Player)event.param1, (Integer)event.param2, (Integer)event.param3);
			break;
		case ServiceEvent.EVENT_PLAYER_MARRIAGE:
			playerMarriaged((Player)event.param1, (Player)event.param2);
			break;
		case ServiceEvent.EVENT_NATIONCOLLECT:
			playerCollect((Player)event.param1, (Integer)event.param2, (Integer)event.param3);
			break;
		case ServiceEvent.EVENT_RANK_UP:
			playerRankUp((Player)event.param1, (Integer)event.param2);
			break;
		case ServiceEvent.EVENT_ADDJEWEL_SUCCESS:
			playerAddJewel((Player)event.param1,(GameItem)event.param2);
			break;
		case ServiceEvent.EVENT_PLAYER_LEVELUP:
			playerLevelUp((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_LOGINED:
			playerAchieveDataDel((Player)event.param1);
			break;
		case ServiceEvent.EVENT_IBUY:
			playerIBuyOk(((Integer)event.param1).intValue(), ((Integer)event.param2).intValue());
			break;
		case ServiceEvent.EVENT_HORSE_LEVELUP:
			horseLevelUp((Player)event.param1);
			break;
		case ServiceEvent.EVENT_HORSE_RIDE:
			horseRide((Player)event.param1);
			break;
		case ServiceEvent.EVENT_HORSE_EQUIP:
		    horseEquip((Player)event.param1);
		    break;
		}
	}
	
	/** 马换装时统计马装成就 */
	public void horseEquip(Player p){
		if(p.id>0){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			TongService service = Server.server.getServiceRegistry().getTongService();
			TongMember tm = service.getPlayerInfo(p.id);
			if(pvpInfo.pool.getString(getPropertyOfHorseEqu(horseEqu[2]))==""){
				List<GameItem> items = new ArrayList<GameItem>();
				for(GameItem it : p.horse.equs.equs){
					if(it!=null){
						items.add(it);
					}
				}
				int num = items.size();
				for(int i=0;i<horseEqu.length;i++){
				   if(num>=horseEqu[i] && pvpInfo.pool.getString(getPropertyOfHorseEqu(horseEqu[i]))==""){
					   pvpInfo.pool.setString(getPropertyOfHorseEqu(horseEqu[i]), getFinishTime(TIMENOW));
					   String msg = MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>坐騎裝備{1}件馬裝</c>成就", p.name,horseEqu[i]);
						if(i == 2){
							Server.server.getServiceRegistry().getChatService()
							.sendWorldMessage(MessageFormat.format(msg, p.name));
							if(tm!=null){
								Server.server.getServiceRegistry().getChatService()
								.sendGuildSystemMessage(msg,tm.tongID);
							}
						} else {
							Server.server.getServiceRegistry().getChatService()
							.sendAreaSystemMessage(msg, p.map.id);
						}
				   }
				   if(pvpInfo.pool.getString(getPropertyOfHorseEqu(horseEqu[i]))==""){
					   break;
				   }
				}
			}
		}
	}
	
	/** 马升级时统计马的等级成就 */
	public void horseLevelUp(Player p){
		if(p.id>0){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			TongService service = Server.server.getServiceRegistry().getTongService();
			TongMember tm = service.getPlayerInfo(p.id);
			if(pvpInfo.pool.getString(getPropertyOfHorseLevel(70)) == ""){
				for(int i=10;i<=70;i+=10){
					if(p.horse!=null && p.horse.level>=i && pvpInfo.pool.getString(getPropertyOfHorseLevel(i)) == ""){
						pvpInfo.pool.setString(getPropertyOfHorseLevel(i), getFinishTime(TIMENOW));
						String msg = MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>培養坐騎達到{1}級</c>成就", p.name,i);
						if(i == 70){
							Server.server.getServiceRegistry().getChatService()
							.sendWorldMessage(MessageFormat.format(msg, p.name));
							if(tm!=null){
								Server.server.getServiceRegistry().getChatService()
								.sendGuildSystemMessage(msg,tm.tongID);
							}
						} else {
							Server.server.getServiceRegistry().getChatService()
							.sendAreaSystemMessage(msg, p.map.id);
						}
					} 
					if(pvpInfo.pool.getString(getPropertyOfHorseLevel(i))==""){
						break;
					}
				}
			}
		}
	}
	
	/** 上骑时统计马的等级和马装成就 */
	public void horseRide(Player p){
		if(p!=null && p.id>0){
			horseLevelUp(p);
			horseEquip(p);
		}
	}
	
	/** 统计玩家元宝消费成就 */
	public void playerIBuyOk(int playerId, int money){
		Player p = (Player)ObjectAccessor.getPlayer(playerId);
		PvpInfo pvpInfo = getPvpInfo(playerId,p.faction);
		int total;
		if(p!=null && pvpInfo.pool.getString(getPropertyOfIMoney(imoney[4],true)) == ""){
			if(pvpInfo.pool.getInt(PROPERTY_IMONEYUSE_COUNT, 0)==0){
				total = Server.server.getServiceRegistry().getDbService().ibuyDAO.getTotalConsumeTillNow(playerId);
				pvpInfo.pool.setInt(PROPERTY_IMONEYUSE_COUNT,total);
			} else {
				total = pvpInfo.pool.getInt(PROPERTY_IMONEYUSE_COUNT,0)+money;
				pvpInfo.pool.setInt(PROPERTY_IMONEYUSE_COUNT, total);
			}
			TongService service = Server.server.getServiceRegistry().getTongService();
			TongMember tm = service.getPlayerInfo(playerId);
			for(int i=0;i<imoney.length;i++){
				if(pvpInfo.pool.getString(getPropertyOfIMoney(imoney[i],true)) == ""){
					if(total >=imoney[i]*3600){
						pvpInfo.pool.setString(getPropertyOfIMoney(imoney[i],true), getFinishTime(TIMENOW));
						String msg = MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>玩家共消耗{1}元寶</c>成就", p.name,imoney[i]);
						if(i==4||i==5){
							Server.server.getServiceRegistry().getChatService()
							.sendWorldMessage(MessageFormat.format(msg, p.name));
							if(tm!=null){
								Server.server.getServiceRegistry().getChatService()
								.sendGuildSystemMessage(msg,tm.tongID);
							}
						} else {
							Server.server.getServiceRegistry().getChatService()
								.sendAreaSystemMessage(msg, p.map.id);
						}
					}
				} 
				if(pvpInfo.pool.getString(getPropertyOfIMoney(imoney[i],true)) == "")
						break;
			}
		}
	}
	
	public long getCancelTime(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR,2010);
		cal.set(Calendar.MONTH, 5);
		cal.set(Calendar.DAY_OF_MONTH, 29);
		cal.set(Calendar.HOUR_OF_DAY,8);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date time = cal.getTime();
		return time.getTime();
	}
	
	public void playerAchieveDataDel(Player p){
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			if((p.lastLogoutTime).getTime() < getCancelTime()){
				pvpInfo.pool = new PropertyPool();
				Server.server.getServiceRegistry().getDbService().pvpInfoDAO.updateEntity(pvpInfo);
			}
		}
	}
	
	public void playerLevelUp(Player p){
	  if(p.id>0){
		PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
		TongService service = Server.server.getServiceRegistry().getTongService();
		TongMember tm = service.getPlayerInfo(p.id);
		if(p.level >= 75 && pvpInfo.pool.getString(getPropertyOfLevel(75)) == ""){
			pvpInfo.pool.setString(getPropertyOfLevel(75),getFinishTime(TIMENOW));
			String msg = MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>等級達到75級</c>成就", p.name);
			if(tm!=null){
				Server.server.getServiceRegistry().getChatService()
				.sendGuildSystemMessage(msg,tm.tongID);
			}
			Server.server.getServiceRegistry().getChatService()
			.sendWorldMessage(msg);
		}
		for(int i=30;i<=80;i+=10){
			if(i==40 || i==60)
				continue;	
			if(p.level>=i && pvpInfo.pool.getString(getPropertyOfLevel(i)) == ""){
				pvpInfo.pool.setString(getPropertyOfLevel(i), getFinishTime(TIMENOW));
				String msg = MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>等級達到{1}級</c>成就", p.name,i);
				if(i == 80){
					Server.server.getServiceRegistry().getChatService()
					.sendWorldMessage(msg);
					if(tm!=null){
						Server.server.getServiceRegistry().getChatService()
						.sendGuildSystemMessage(msg,tm.tongID);
					}
				} else {
					Server.server.getServiceRegistry().getChatService()
					.sendAreaSystemMessage(msg,p.map.id);
				}
			}
			    if(pvpInfo.pool.getString(getPropertyOfLevel(i))==""){
				   break;
			}
		}
	  }
	}
	
	public void playerAddJewel(Player p,GameItem gameItem){
	  if(p!=null && p.id>0){
		PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
		int jewelLevel = ObjectAccessor.getItemTemplate(gameItem.template.id).useLevel;
		if(jewelLevel == 7 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_SEV) == ""){
			pvpInfo.pool.setString(PROPERTY_FINISHTIME_SEV,getFinishTime(TIMENOW));
			Server.server.getServiceRegistry().getChatService()
			.sendAreaSystemMessage(MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>裝備上擁有1顆7級寶石</c>成就", p.name),p.map.id);
		}
		if(jewelLevel != 7 && pvpInfo.pool.getString(getPropertyByLevel(jewelLevel)) == ""){
			pvpInfo.pool.setString(getPropertyByLevel(jewelLevel), getFinishTime(TIMENOW));
			Server.server.getServiceRegistry().getChatService()
			.sendAreaSystemMessage(MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>裝備上擁有1顆{1}級寶石</c>成就", p.name,jewelLevel),p.map.id);
		}
		countJewelOnEquipment(p);
		}
	 }

	
	/** 根据国别获取杀死敌国总人数或者被敌国杀死总次数的属性池属性 **/
	protected String getPropertyByFaction(int faction, boolean kill){
		if(kill){
			switch(faction){
			case GameObject.FACTION_WEI:
				return PROPERTY_KILL_WEI;
			case GameObject.FACTION_SHU:
				return PROPERTY_KILL_SHU;
			case GameObject.FACTION_WU:
				return PROPERTY_KILL_WU;
			}
		}else{
			switch(faction){
			case GameObject.FACTION_WEI:
				return PROPERTY_KILLEDBY_WEI;
			case GameObject.FACTION_SHU:
				return PROPERTY_KILLEDBY_SHU;
			case GameObject.FACTION_WU:
				return PROPERTY_KILLEDBY_WU;
			}
		}
		return null;
	}
	
	/**根据宝石等级获得完成宝石属性的时间 **/
	protected String getPropertyByLevel(int level){
		return "PROPERTY_FINISHTIME_JEWEL"+level;
	}
	
	/**获得拍卖成就完成时间属性 **/
	public String getPropertyOfAuction(int cnt,int money,boolean sell){
		if(money == 0){
			if(sell){
				return "PROPERTY_FINISHTIME_AUCTIONSELL"+cnt;
			} else {
				return "PROPERTY_FINISHTIME_AUCTIONBUY"+cnt;
			}
		}
		if(cnt == 0){
			if(sell){
				return "PROPERTY_FINISHTIME_AUCTIONSELLMONEY"+money;
			} else {
				return "PROPERTY_FINISHTIME_AUCTIONBUYMONEY"+money;
			}
		}
		return null;	
	}
	
	/**记录玩家随身携带和消费的i币的属性 **/
    public String getPropertyOfIMoney(int imoney,boolean use){
    	if(use){
    		return "PROPERTY_FINISHTIME_USEIMONEY"+imoney;
    	} else {
    		return "PROPERTY_FINISHTIME_HAVEIMONEY"+imoney;
    	}
    }
    
    /**记录马的等级属性 **/
    public String getPropertyOfHorseLevel(int level){
    		return "PROPERTY_FINISHTIME_HORSELEVEL"+level;
    }
    
    /**记录马的装备属性 **/
    public String getPropertyOfHorseEqu(int num){
    		return "PROPERTY_FINISHTIME_HORSEEQU"+num;
    }
    
    /**记录玩家升级属性**/
    protected String getPropertyOfLevel(int level){
    	return "PROPERTY_FINISHTIME_LEVEL"+level;
    }
    
    /** 记录玩家杀死敌国国王的属性 */
    public String getPropertyOfKillKing(int index){
    	return "PROPERTY_FINISHTIME_KKG"+index;
    }
    
    public int getIndex(int fa,int kingFa){
    	List<Integer> factions = getFaction(fa);
    	int temp = 0;
		int a = fa - factions.get(0);
		int b = fa - factions.get(1);
		if(a>b){
			temp = factions.get(0);
		} else {
			temp = factions.get(1);
		}
		if(temp == kingFa){
			return 1;
		}
		return 2;
    }
    
    public List<Integer> getFaction(int fa){
    	List<Integer> factions = new ArrayList<Integer>();
		for(int i=0;i<faction.length;i++){
			if(fa!=faction[i]){
				factions.add(new Integer(faction[i]));
			}
		}
		return factions;
    }
	
	/** 根据国别和职业获取杀死敌国某职业的总人数或者被敌国某职业杀死的属性池属性 **/
	protected String getPropertyByFactionAndClazz(int faction, int clazz, boolean kill){
		return getPropertyByFaction(faction,kill)+clazzs[clazz];
	}
	
	/** 统计玩家装备中镶有各级宝石的数量 **/
	protected void countJewelOnEquipment(Player p){
	  if(p.id>0){
		PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
		TongService service = Server.server.getServiceRegistry().getTongService();
		TongMember tm = service.getPlayerInfo(p.id);
		List<GameItem> equipments = new ArrayList<GameItem>();
		for(GameItem item : p.equipments.equs){
			equipments.add(item);
		}
		for(TransactionBagGrid grid : p.bag.getGrids()){
			GameItem item = grid.getItem();
			equipments.add(item);
		}
		List<Horse> horses = new ArrayList<Horse>(p.horseBag.horses);
		if(p.horse!=null)
			horses.add(p.horse);
		for(Horse horse : horses){
			for(GameItem item : horse.equs.equs){
				equipments.add(item);
			}
		}
		int count = 0;
		int countsev = 0;
		int countsix = 0;
		for(GameItem gameItem : equipments){
			if(gameItem != null && gameItem.object!=null && gameItem.object instanceof ItemEnhance){
				ItemEnhance enhance = (ItemEnhance) gameItem.object;
				if(enhance.getJewelCount()>0){
					for(int i=0;i<enhance.getJewelCount();i++){
						int jewelId = enhance.getJewel(i);
						if(ObjectAccessor.getItemTemplate(jewelId)==null)
							continue;
						int jewelLevel = ObjectAccessor.getItemTemplate(jewelId).useLevel;
						if(jewelLevel == 7 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_SEV) == ""){
							pvpInfo.pool.setString(PROPERTY_FINISHTIME_SEV, getFinishTime(TIMENOW));
						}
						if(jewelLevel != 7 && pvpInfo.pool.getString(getPropertyByLevel(jewelLevel)) == ""){
							pvpInfo.pool.setString(getPropertyByLevel(jewelLevel), getFinishTime(TIMENOW));
						}
						if(jewelLevel == 6 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_6COUNT50) == ""){
							countsix++;
						}
						if(jewelLevel == 7 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_7COUNT50) == ""){
							countsev++;
						}
					}
					if(pvpInfo.pool.getString(PROPERTY_FINISHTIME_COUNT50)==""){
					     count+=enhance.getJewelCount();
					}
				}
			}
		}
		if(count != pvpInfo.pool.getInt(PROPERTY_JEWEL_COUNT, 0) && pvpInfo.pool.getString(PROPERTY_FINISHTIME_COUNT50) == ""){
			pvpInfo.pool.setInt(PROPERTY_JEWEL_COUNT, count);
		}
		if(countsev != pvpInfo.pool.getInt(PROPERTY_JEWELSEVEN_COUNT,0) && pvpInfo.pool.getString(PROPERTY_FINISHTIME_7COUNT50) == ""){
			pvpInfo.pool.setInt(PROPERTY_JEWELSEVEN_COUNT, countsev);
		}
		if(countsix != pvpInfo.pool.getInt(PROPERTY_JEWELSIX_COUNT,0) && pvpInfo.pool.getString(PROPERTY_FINISHTIME_6COUNT50) == ""){
			pvpInfo.pool.setInt(PROPERTY_JEWELSIX_COUNT, countsix);
		}
		if(pvpInfo.pool.getInt(PROPERTY_JEWEL_COUNT, 0) >= 30 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_COUNT30) == ""){
			pvpInfo.pool.setString(PROPERTY_FINISHTIME_COUNT30, getFinishTime(TIMENOW));
			Server.server.getServiceRegistry().getChatService()
			.sendAreaSystemMessage(MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>裝備上擁有30顆寶石</c>成就", p.name),p.map.id);
		}
		if(pvpInfo.pool.getInt(PROPERTY_JEWEL_COUNT, 0) >= 40 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_COUNT40)==""){
			pvpInfo.pool.setString(PROPERTY_FINISHTIME_COUNT40, getFinishTime(TIMENOW));
			Server.server.getServiceRegistry().getChatService()
			.sendAreaSystemMessage(MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>裝備上擁有40顆寶石</c>成就", p.name),p.map.id);
		}
		if(pvpInfo.pool.getInt(PROPERTY_JEWEL_COUNT, 0) >= 50 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_COUNT50)==""){
			pvpInfo.pool.setString(PROPERTY_FINISHTIME_COUNT50, getFinishTime(TIMENOW));
			Server.server.getServiceRegistry().getChatService()
			.sendAreaSystemMessage(MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>裝備上擁有50顆寶石</c>成就", p.name),p.map.id);
		}
		if(pvpInfo.pool.getInt(PROPERTY_JEWELSIX_COUNT, 0)>= 50 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_6COUNT50)=="" ) {
		    pvpInfo.pool.setString(PROPERTY_FINISHTIME_6COUNT50, getFinishTime(TIMENOW));
		    String msg = MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>裝備上擁有50顆6級寶石</c>成就", p.name);
		    if(tm!=null){
				Server.server.getServiceRegistry().getChatService()
				.sendGuildSystemMessage(msg,tm.tongID);
			}
			Server.server.getServiceRegistry().getChatService()
			.sendWorldMessage(msg);
		}
		if(pvpInfo.pool.getInt(PROPERTY_JEWELSEVEN_COUNT, 0) >= 50 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_7COUNT50)=="" ) {
		    pvpInfo.pool.setString(PROPERTY_FINISHTIME_7COUNT50, getFinishTime(TIMENOW));
		    String msg = MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>裝備上擁有50顆7級寶石</c>成就", p.name);
		    if(tm!=null){
				Server.server.getServiceRegistry().getChatService()
				.sendGuildSystemMessage(msg,tm.tongID);
			}
			Server.server.getServiceRegistry().getChatService()
			.sendWorldMessage(msg);
		}
	  }
	}


	public void shutdown() {
		synchronized(pvpInfos){
			log.info("UPDATE PVPINFO");
			saveAllPvpInfos();
			kills.clear();
			log.info("UPDATE PVPINFO OK");
		}
		
	}
	
	protected void saveAllPvpInfos(){
		for(PvpInfo info:pvpInfos.values()){
			Server.server.getServiceRegistry().getDbService().pvpInfoDAO.updateEntity(info);
		}
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
		getTopPvpInfos(TOP_COUNT,GameObject.FACTION_WEI);
		getTopPvpInfos(TOP_COUNT,GameObject.FACTION_SHU);
		getTopPvpInfos(TOP_COUNT,GameObject.FACTION_WU);
		getTopWeekRanks(TOP_COUNT,GameObject.FACTION_WEI);
		getTopWeekRanks(TOP_COUNT,GameObject.FACTION_SHU);
		getTopWeekRanks(TOP_COUNT,GameObject.FACTION_WU);
		getTopLevelRanks(TOP_COUNT, 0);
		getTopLevelRanks(TOP_COUNT,GameObject.FACTION_WEI);
		getTopLevelRanks(TOP_COUNT,GameObject.FACTION_SHU);
		getTopLevelRanks(TOP_COUNT,GameObject.FACTION_WU);
		new Thread(new UpdatePvpInfo()).start();
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
		.findFile("achievement.xml");
       Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
       parse(doc);
	}
	
	@SuppressWarnings("unchecked")
	public List<PvpInfo> topPvpInfos(int faction){
		return topPvpInfos[faction];
	}
	
	@SuppressWarnings("unchecked")
	public List<Actor> topWeekRanks(int faction){
		return topWeekRanks[faction];
	}
	
	@SuppressWarnings("unchecked")
	public List<Actor> topLevelRanks(int faction){
		return topLevelRanks[faction];
	}
	
	/**
	 * 
	 * @param killedId 被杀的人的ID
	 * @param killId 杀人者的ID
	 */
	public void addPvpInfo(int killedId,int killId,int killedFaction,int killFaction
			,int killClazz,int killedClazz,int killLevel,int killedLevel){
		Kill kill = new Kill();
		kill.killedId = killedId;
		kill.killId = killId;
		kill.killedFaction = killedFaction;
		kill.killFaction = killFaction;
		kills.add(kill);
		
		FactionKill factionKill = new FactionKill();
		factionKill.killedId = killedId;
		factionKill.killId = killId;
		factionKill.killedFaction = killedFaction;
		factionKill.killFaction = killFaction;
		factionKill.killClazz = killClazz;
		factionKill.killedClazz = killedClazz;
		//高于对方10级以上就不算
		if(killLevel<=(killedLevel+10))
			kills.add(factionKill);
	}
	
	protected void processPvpInfo(Object o){
		if(o instanceof Kill){
			Kill kill = (Kill)o;
			PvpInfo info = getPvpInfo(kill.killedId,kill.killedFaction);
			info.todayDieCount++;
			info.totalDieCount++;
			info = getPvpInfo(kill.killId,kill.killFaction);
			info.todayKillCount++;
			info.totalKillCount++;
		}else if(o instanceof FactionKill){
			processFactionKill((FactionKill)o);
		}else if(o instanceof ServiceEvent){
			processEvent((ServiceEvent)o);
		}
	}

	public PvpInfo getPvpInfo(int id,int faction){
		PvpInfo info = pvpInfos.get(id);
		if(info==null){
			info = Server.server.getServiceRegistry().getDbService().pvpInfoDAO.getPvpInfoById(id);
			if(info==null){
				info = new PvpInfo(id,faction);
				Server.server.getServiceRegistry().getDbService().pvpInfoDAO.newEntity(info);
			}
			pvpInfos.put(id, info);
		}
		return info;
	}
    
	public void dayChanged() {
		new Thread("RebuildPvpInfo"){
			@Override
			public void run(){
				synchronized (pvpInfos) {
					rebuildPvpInfos();
				}
				rebuildWeekCredits();
				rebuildLevelRanks();
			}
		}.start();
	}
	
	public void rebuildLevelRanks() {
		getTopLevelRanks(TOP_COUNT, 0);
		getTopLevelRanks(TOP_COUNT,GameObject.FACTION_WEI);
		getTopLevelRanks(TOP_COUNT,GameObject.FACTION_SHU);
		getTopLevelRanks(TOP_COUNT,GameObject.FACTION_WU);
	}
	
	public void rebuildWeekCredits(){
		getTopWeekRanks(TOP_COUNT,GameObject.FACTION_WEI);
		getTopWeekRanks(TOP_COUNT,GameObject.FACTION_SHU);
		getTopWeekRanks(TOP_COUNT,GameObject.FACTION_WU);
	}
	
	public void rebuildPvpInfos(){
		saveAllPvpInfos();
		Server.server.getServiceRegistry().getDbService().pvpInfoDAO.updatePvpInfos();
		pvpInfos.clear();
		getTopPvpInfos(TOP_COUNT,GameObject.FACTION_WEI);
		getTopPvpInfos(TOP_COUNT,GameObject.FACTION_SHU);
		getTopPvpInfos(TOP_COUNT,GameObject.FACTION_WU);
	}
	
	public void deleteActor(int id){
		PvpInfo info = pvpInfos.remove(id);
		if(info==null){
			info = Server.server.getServiceRegistry().getDbService().pvpInfoDAO.getPvpInfoById(id);
		}
		if(info!=null){
			Server.server.getServiceRegistry().getDbService().pvpInfoDAO.makeTransient(info);
		}
	}
	
	public void removePvpInfo(int id){
		pvpInfos.remove(id);
	}
	
	protected void getTopPvpInfos(int count,int faction){
		topPvpInfos[faction] = Server.server.getServiceRegistry().getDbService().pvpInfoDAO.getTopPvpInfos(TOP_COUNT,faction);
		for(Object o:topPvpInfos[faction]){
			PvpInfo info = (PvpInfo)o;
			info.actor = Server.server.getServiceRegistry().getActorCacheService().find(info.id);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void getTopWeekRanks(int count,int faction){
		List<Integer> l = Server.server.getServiceRegistry().getDbService().playerDAO.getTopWeekRanks(count,faction);
		topWeekRanks[faction] = new ArrayList<Actor>();
		for(int id:l){
			topWeekRanks[faction].add(Server.server.getServiceRegistry().getActorCacheService().find(id));
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void getTopLevelRanks(int count,int faction){
		List<Integer> l = Server.server.getServiceRegistry().getDbService().playerDAO.getTopLevelRanks(count,faction);
		topLevelRanks[faction] = new ArrayList<Actor>();
		for(int id:l){
			topLevelRanks[faction].add(Server.server.getServiceRegistry().getActorCacheService().find(id));
		}
	}
	
	class UpdatePvpInfo implements Runnable{
		public void run(){
			while(runStat){
				try {
					Object o = kills.take();
					synchronized(pvpInfos){
						processPvpInfo(o);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 杀死敌国总人数
	 * @param p
	 */
	public int getKillCount(Player p){
		int total = 0;
		PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
	   for(int i = 0;i < faction.length;i++){
		   if(p.faction != faction[i]){
			   total+=pvpInfo.pool.getInt(getPropertyByFaction(
						faction[i], true),0);
		   }
	   }
	   return total;
	}
	

	public void parse(Document doc) {
		Element root = doc.getRootElement();
		if (root != null) {
			List ach = root.elements("achievement");
			for (int i = 0; i < ach.size(); i++) {
				List<Achievement> achieve = new ArrayList<Achievement>();
				int type = Integer.parseInt(((Element) ach.get(i))
						.attributeValue("type"));
				List achItems = ((Element) ach.get(i)).elements("item");
				for (int j = 0; j < achItems.size(); j++) {
					int achieveId = Integer
							.parseInt(((Element) achItems.get(j))
									.attributeValue("achieveid"));
					String achievementName = ((Element) achItems.get(j))
							.attributeValue("name");
					String dec = ((Element) achItems.get(j))
							.attributeValue("dec");
					int point = Integer.parseInt(((Element) achItems.get(j))
							.attributeValue("point"));
					Achievement perAchieve = new Achievement(achieveId, type,
							achievementName, dec, point);
					achieve.add(perAchieve);
				}
				achievement.put(type, achieve);
			}	
		  }	
		}
	
	
	/*
	 * 个人成就详细列表
	 */
	@SuppressWarnings("unchecked")
	public void detailAchieveList(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		int serial = packet.getInt();
		int personId = packet.getInt();
		int type = packet.getInt();
		if (player != null) {
			Packet pt = new Packet(OpCode.PERSONAL_ACHIEVEMENT_DETAIL_SERVER);
			pt.putInt(serial);
			Player person = (Player)ObjectAccessor.getPlayer(personId);
			if(person == null){
				person =  Server.server.getServiceRegistry().getFameService().getStatue(personId);
			}
			if(person != null){
				PropertyPool po = null;
				if(person.id>0){
				     PvpInfo pvpInfo = getPvpInfo(person.id, person.faction);
				     po = pvpInfo.pool;
				} else {
				    Fame fame = Server.server.getServiceRegistry().getFameService().getFame(person.id);
					po = fame.pool;
				}
				if(type == 9){
					pt.putShort(2);
					List<Integer> list=new ArrayList<Integer>();
					for(int i=0;i<faction.length;i++){
						if(person.faction!=faction[i])
							list.add(faction[i]);
					}
					for(int i=0;i<list.size();i++){
						pt.putInt(list.get(i));//faction
						pt.putInt(po.getInt(getPropertyByFaction(
								list.get(i), true),0));//totalcnt
						pt.putShort(clazzs.length);
						for(int j=0;j< clazzs.length;j++){
							pt.putInt(clazzs[j]);
							pt.putInt(po.
									getInt(getPropertyByFactionAndClazz(list.get(i),clazzs[j],true),0));
						}
							
					}
				} else if(type == 10){
					pt.putShort(2);
					List<Integer> list=new ArrayList<Integer>();
					for(int i=0;i<faction.length;i++){
						if(person.faction!=faction[i])
							list.add(faction[i]);
					}
					for(int i=0;i<list.size();i++){
						pt.putInt(list.get(i));//faction
						pt.putInt(po.getInt(getPropertyByFaction(
								list.get(i), false),0));//totalcnt
						pt.putShort(clazzs.length);
						for(int j=0;j< clazzs.length;j++){
							pt.putInt(clazzs[j]);
							pt.putInt(po.
									getInt(getPropertyByFactionAndClazz(list.get(i),clazzs[j],false),0));
						}
							
					}
				} else {
					if(person.id>0){
						processQuest(person);
						countJewelOnEquipment(person);
					}
					List<Achievement> achievements = achievement.get(type);
					if(type == 6){
						List<Achievement> achs = new ArrayList<Achievement>();
						for(int i=0;i<faction.length;i++){
							if(person.faction!=faction[i]){
								achs.add(achievements.get(i));
							}
						}
						achievements = new ArrayList<Achievement>(achs);
					}
					for (Achievement ach : achievements) {
						getAccomplishAndFiniTime(ach, person);
					}
					Collections.sort(achievements, new ConverSort());
					int size = achievements.size();
					pt.putShort(size);
					for (Achievement ach : achievements) {
						pt.putString(ach.achievementName);
						pt.put(ach.acomplish);
						pt.putString(ach.dec);
						pt.putInt(ach.point);
						pt.putString(ach.finiTime);
					}
				}
				player.send(pt);
			} else {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.PERSONAL_ACHIEVEMENT_DETAIL_CLIENT, "玩家不在線");
			}
		}
	}

	/*
	 * 个人成就分类列表
	 */
	public void achievementList(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		int count = 0;
		int pointCount = 0;
		int serial = packet.getInt();
		int personId = packet.getInt();
		int size = achievement.size();
		if (player != null) {
			Packet pt = new Packet(OpCode.PERSONAL_ACHIEVEMENT_SERVER);
			pt.putInt(serial);
			pt.putShort(size);
			String[] catagoryNames = { "常規", "榮譽", "生活" ,"成長歷程","穿金戴銀","經商有道","刺殺國君","伯樂之才","富甲天下","擊殺統計","被擊殺統計"};
			Player person = (Player)ObjectAccessor.getPlayer(personId);
			if(person == null){
				person =  Server.server.getServiceRegistry().getFameService().getStatue(personId);
			}
			if(person == null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.PERSONAL_ACHIEVEMENT_CLIENT, "玩家已下線");
				return;
			}
			for (int i : achievement.keySet()) {
				int finished = 0;
				int point = 0;
				List<Achievement> list = achievement.get(i);
				if(i == 6){
					List<Achievement> achs = new ArrayList<Achievement>();
					for(int j=0;j<faction.length;j++){
						if(person.faction!=faction[j]){
							achs.add(list.get(j));
						}
					}
					list = new ArrayList<Achievement>(achs);
				}
				pt.putString(catagoryNames[i]);
				pt.putInt(list.size());
				if(person.id>0){
					processQuest(person);
					countJewelOnEquipment(person);
				}
				for (Achievement ach : list) {
					getAccomplishAndFiniTime(ach, person);
					if (ach.acomplish == 1){
						count++;
						finished++;
						point += ach.point;
					    pointCount += ach.point;
					}
				}
				pt.putInt(finished);
				pt.putInt(point);
			}
			pt.putString(catagoryNames[9]);
			pt.putInt(2);
			pt.putString(catagoryNames[10]);
			pt.putInt(2);
			pt.putInt(count);
			pt.putInt(pointCount);
			player.send(pt);
			catagoryNames = null;
		}
	}

	public void getAccomplishAndFiniTime(Achievement ach, Player person) {
		PropertyPool po = null;
		if(person.id>0){
	      PvpInfo pvpInfo = getPvpInfo(person.id, person.faction);
	      po = pvpInfo.pool;
		} else {
			Fame fame = FameService.fames.get(person.id);
			po = fame.pool;
		}
		
		// 完成七夕任务
		if (ach.achieveId == 1) {
			ach.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_QUESTQIXI) == "") ? 0
					: 1);
			ach.finiTime = po.getString(PROPERTY_FINISHTIME_QUESTQIXI);
		}
		
		// 完成中秋任务
		if (ach.achieveId == 2) {
			ach.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_QUESTMID) == "") ? 0 : 1);
			ach.finiTime = po.getString(PROPERTY_FINISHTIME_QUESTMID);
		}
		
		// 完成感恩任务
		if (ach.achieveId == 3) {
			ach.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_QUESTOWE) == "") ? 0
					: 1);
			ach.finiTime = po.getString(PROPERTY_FINISHTIME_QUESTOWE);
		}
        
		// 完成河东副本任务[副]诛杀首恶
		if (ach.achieveId == 4) {
			ach.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_ZHUSHA) == "") ? 0 : 1);
			ach.finiTime = po.getString(PROPERTY_FINISHTIME_ZHUSHA);
		}
		
		// 完成古墓副本任务[副]搜寻玉玺
		if (ach.achieveId == 5) {
			ach.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_YUXI) == "") ? 0 : 1);
			ach.finiTime = po.getString(PROPERTY_FINISHTIME_YUXI);
		}
		
		// 完成天龙阵副本[副]大破天龙阵
		if (ach.achieveId == 6) {
			ach.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_QUESTTIANLONG) == "") ? 0 : 1);
			ach.finiTime = po.getString(PROPERTY_FINISHTIME_QUESTTIANLONG);
		}
		
		// 已经完成300个任务，不包含每日和跑环任务
		if (ach.achieveId == 7) {
			if(person.asmVm.getFinishedQuest(person) >= 300 && po.getString(PROPERTY_FINISHTIME_PERFECT) == ""  && person.id>0){
				po.setString(PROPERTY_FINISHTIME_PERFECT,getFinishTime(TIMENOW));
			}
			ach.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_PERFECT) == "") ? 0
					: 1);
			ach.finiTime = po.getString(PROPERTY_FINISHTIME_PERFECT);
		}
		
		// 杀死敌方阵营玩家数达到100
		if (ach.achieveId == 8) {
			ach.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_KILL) == "" ? 0 : 1);
			ach.finiTime = po.getString(PROPERTY_FINISHTIME_KILL);
		}
		
		// 杀死敌方阵营玩家数达到500
		if (ach.achieveId == 9) {
			ach.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_KILLMORE) == "" ? 0 : 1);
			ach.finiTime = po.getString(PROPERTY_FINISHTIME_KILLMORE);
		}
		
		// 向国库捐赠达到1000000
		if (ach.achieveId == 10) {
			if(po.getString(PROPERTY_FINISHTIME_COLLECT) == "" && po.getInt(
					PROPERTY_NATION_COLLECT, 0) >= 1000000 && person.id>0){
				po.setString(PROPERTY_FINISHTIME_COLLECT,getFinishTime(TIMENOW));
			}
			ach.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_COLLECT) == "" ? 0 : 1);
			ach.finiTime = po.getString(PROPERTY_FINISHTIME_COLLECT);
	    }
		
		// 金钱达到10000 
		if (ach.achieveId == 11) {
			if(person.money >= 10000 && po.getString(PROPERTY_FINISHTIME_TENTHOU) == "" && person.id>0){
				po.setString(PROPERTY_FINISHTIME_TENTHOU,getFinishTime(TIMENOW));
			}
			ach.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_TENTHOU)=="" ? 0 : 1);
			ach.finiTime=po.getString(PROPERTY_FINISHTIME_TENTHOU);
		}
		
		// 金钱达到1000000 
		if (ach.achieveId == 12) {
			if(person.money>=1000000 && po.getString(PROPERTY_FINISHTIME_MILLIONARE) == "" && person.id>0){
				po.setString(PROPERTY_FINISHTIME_MILLIONARE,getFinishTime(TIMENOW));
			}
			ach.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_MILLIONARE) == "" ? 0 : 1);
			ach.finiTime=po.getString(PROPERTY_FINISHTIME_MILLIONARE);
		}
		
		// 结过一次婚
		if (ach.achieveId == 13) {
			PlayerRelation rel = Server.server.getServiceRegistry()
					.getRelationService().get(person.id);
			if ((rel != null && rel.mateId != -1)
						&& po.getString(PROPERTY_FINISHTIME_MARRIAGE) == "" && person.id>0){
				po.setString(PROPERTY_FINISHTIME_MARRIAGE,getFinishTime(TIMENOW));
				}
			    ach.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_MARRIAGE) == "" ? 0 : 1);
				ach.finiTime = po.getString(PROPERTY_FINISHTIME_MARRIAGE);
	    }
		
		// 等级达到30级
		if (ach.achieveId == 16) {
			if(po.getString(PROPERTY_FINISHTIME_LEVELTHIRTY)!= "" 
				&& po.getString(getPropertyOfLevel(30))!= po.getString(PROPERTY_FINISHTIME_LEVELTHIRTY) && person.id>0){
				po.setString(getPropertyOfLevel(30), po.getString(PROPERTY_FINISHTIME_LEVELTHIRTY));
			}
			if(person.level >=30 && po.getString(getPropertyOfLevel(30)) == "" && person.id>0){
				po.setString(getPropertyOfLevel(30), getFinishTime(TIMENOW));
			}
			ach.acomplish = (byte) ((po.getString(getPropertyOfLevel(30)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfLevel(30));
		}
		
		// 等级达到50级
		if (ach.achieveId == 17) {
			if(po.getString(PROPERTY_FINISHTIME_LEVELFIFTY)!= "" 
				&& po.getString(getPropertyOfLevel(50))!= po.getString(PROPERTY_FINISHTIME_LEVELFIFTY) && person.id>0){
				po.setString(getPropertyOfLevel(50), po.getString(PROPERTY_FINISHTIME_LEVELFIFTY));
			}
			if(person.level >=50 && po.getString(getPropertyOfLevel(50))=="" && person.id>0){
				po.setString(getPropertyOfLevel(50), getFinishTime(TIMENOW));
			}
			ach.acomplish = (byte) ((po.getString(getPropertyOfLevel(50)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfLevel(50));
		}
		
		// 等级达到70级
		if (ach.achieveId == 18) {
			if(po.getString(PROPERTY_FINISHTIME_LEVELSEVENTY)!= "" 
				&& po.getString(getPropertyOfLevel(70))!= po.getString(PROPERTY_FINISHTIME_LEVELSEVENTY) && person.id>0){
				po.setString(getPropertyOfLevel(70),po.getString(PROPERTY_FINISHTIME_LEVELSEVENTY));
			}
			if(person.level >= 70 && po.getString(getPropertyOfLevel(70)) == "" && person.id>0){
				po.setString(getPropertyOfLevel(70), getFinishTime(TIMENOW));
			}
			ach.acomplish = (byte) ((po.getString(getPropertyOfLevel(70)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfLevel(70));
	   }
		
		// 等级达到75级
		if (ach.achieveId == 19) {
			if(person.level >=75 && po.getString(getPropertyOfLevel(75)) == "" && person.id>0){
				po.setString(getPropertyOfLevel(75), getFinishTime(TIMENOW));
			}
			ach.acomplish = (byte) ((po.getString(getPropertyOfLevel(75)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfLevel(75));
		}
		
		// 等级达到80级
		if (ach.achieveId == 20) {
			if(person.level >=80 && po.getString(getPropertyOfLevel(80)) == "" && person.id>0){
				po.setString(getPropertyOfLevel(80), getFinishTime(TIMENOW));
			}
			ach.acomplish = (byte) ((po.getString(getPropertyOfLevel(80)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfLevel(80));
		}
		
		// 装备上镶有一颗1级宝石
		if (ach.achieveId == 21) {
		ach.acomplish = (byte) ((po.getString(getPropertyByLevel(1)) == "") ? 0 : 1);
		ach.finiTime = po.getString(getPropertyByLevel(1));
	    }
		
		// 装备上镶有一颗2级宝石
		if (ach.achieveId == 22) {
			ach.acomplish = (byte) ((po.getString(getPropertyByLevel(2)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyByLevel(2));
		}
		
		// 装备上镶有一颗3级宝石
		if (ach.achieveId == 23) {
			ach.acomplish = (byte) ((po.getString(getPropertyByLevel(3)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyByLevel(3));
		}
		
		// 装备上镶有一颗4级宝石
		if (ach.achieveId == 24) {
			ach.acomplish = (byte) ((po.getString(getPropertyByLevel(4)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyByLevel(4));
		}
		
		// 装备上镶有一颗5级宝石
		if (ach.achieveId == 25) {
			ach.acomplish = (byte) ((po.getString(getPropertyByLevel(5)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyByLevel(5));
		}
		
		// 装备上镶有一颗6级宝石
		if (ach.achieveId == 26) {
			ach.acomplish = (byte) ((po.getString(getPropertyByLevel(6)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyByLevel(6));
		}
		
		// 装备上镶有一颗7级宝石
		if (ach.achieveId == 27) {
			ach.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_SEV) == "") ? 0 : 1);
			ach.finiTime = po.getString(PROPERTY_FINISHTIME_SEV);
		}
		
		// 装备上镶有30颗宝石
		if(ach.achieveId == 28){
			ach.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_COUNT30) == "") ? 0 : 1);
			ach.finiTime = po.getString(PROPERTY_FINISHTIME_COUNT30);
		}
		
		// 装备上镶有40颗宝石
		if(ach.achieveId == 29){
			ach.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_COUNT40) == "") ? 0 : 1);
			ach.finiTime = po.getString(PROPERTY_FINISHTIME_COUNT40);
		}
		
		// 装备上镶有50颗宝石
		if(ach.achieveId == 30){
			ach.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_COUNT50) == "") ? 0 : 1);
			ach.finiTime = po.getString(PROPERTY_FINISHTIME_COUNT50);
		}
		
		// 装备上镶有50颗6级宝石
		if(ach.achieveId == 31){
			ach.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_6COUNT50) == "") ? 0 : 1);
			ach.finiTime = po.getString(PROPERTY_FINISHTIME_6COUNT50);
		}		
		
		// 装备上镶有50颗7级宝石
		if(ach.achieveId == 32){
			ach.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_7COUNT50) == "") ? 0 : 1);
			ach.finiTime = po.getString(PROPERTY_FINISHTIME_7COUNT50);
		}		
		
		// 在拍卖行成功拍出1件物品
		if(ach.achieveId == 33) {
			if(po.getString(PROPERTY_FINISHTIME_AUCTION)!="" &&
					po.getString(getPropertyOfAuction(1,0,true))!=po.getString(PROPERTY_FINISHTIME_AUCTION) && person.id>0){
				po.setString(getPropertyOfAuction(1,0,true),po.getString(PROPERTY_FINISHTIME_AUCTION));
			}
			ach.acomplish = (byte) ((po.getString(getPropertyOfAuction(1,0,true)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfAuction(1,0,true));
		}
		
		// 在拍卖行成功拍出10件物品
		if(ach.achieveId == 34) {
			ach.acomplish = (byte) ((po.getString(getPropertyOfAuction(10,0,true)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfAuction(10,0,true));
		}
		
		// 在拍卖行成功拍出100件物品
		if(ach.achieveId == 35) {
			ach.acomplish = (byte) ((po.getString(getPropertyOfAuction(100,0,true)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfAuction(100,0,true));
		}
		
		// 在拍卖行成功买到1件物品
		if(ach.achieveId == 36) {
			ach.acomplish = (byte) ((po.getString(getPropertyOfAuction(1,0,false)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfAuction(1,0,false));
		}
		
		// 在拍卖行成功买到10件物品
		if(ach.achieveId == 37) {
			ach.acomplish = (byte) ((po.getString(getPropertyOfAuction(10,0,false)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfAuction(10,0,false));
		}
		
		// 在拍卖行成功买到100件物品
		if(ach.achieveId == 38) {
			ach.acomplish = (byte) ((po.getString(getPropertyOfAuction(100,0,false)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfAuction(100,0,false));
		}
		
		if(ach.achieveId == 39) {
			ach.acomplish = (byte) ((po.getString(getPropertyOfAuction(0,100,true)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfAuction(0,100,true));
		}
		
		if(ach.achieveId == 40) {
			ach.acomplish = (byte) ((po.getString(getPropertyOfAuction(0,10000,true)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfAuction(0,10000,true));
		}
		
		if(ach.achieveId == 41) {
			ach.acomplish = (byte) ((po.getString(getPropertyOfAuction(0,1000000,true)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfAuction(0,1000000,true));
		}
		
		if(ach.achieveId == 42) {
			ach.acomplish = (byte) ((po.getString(getPropertyOfAuction(0,100000000,true)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfAuction(0,100000000,true));
		}
		
		if(ach.achieveId == 43) {
			ach.acomplish = (byte) ((po.getString(getPropertyOfAuction(0,100,false)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfAuction(0,100,false));
		}
		
		if(ach.achieveId == 44) {
			ach.acomplish = (byte) ((po.getString(getPropertyOfAuction(0,10000,false)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfAuction(0,10000,false));
		}
		
		if(ach.achieveId == 45) {
			ach.acomplish = (byte) ((po.getString(getPropertyOfAuction(0,1000000,false)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfAuction(0,1000000,false));
		}
		
		if(ach.achieveId == 46) {
			ach.acomplish = (byte) ((po.getString(getPropertyOfAuction(0,100000000,false)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfAuction(0,100000000,false));
		}
		//47,48,49为分别击杀魏蜀吴三个国家的国君
		if(ach.type == 6){
			int[] index = {0,47,48,49};
			List<Integer> list = getFaction(person.faction);
			if(ach.achieveId == index[list.get(0)]){
			    ach.acomplish = (byte) ((po.getString(getPropertyOfKillKing(1)) == "") ? 0 : 1);
				ach.finiTime = po.getString(getPropertyOfKillKing(1));
			}
			
			if(ach.achieveId == index[list.get(1)]){
			    ach.acomplish = (byte) ((po.getString(getPropertyOfKillKing(2)) == "") ? 0 : 1);
				ach.finiTime = po.getString(getPropertyOfKillKing(2));
			}
		}
		
		if(ach.achieveId == 50){
			ach.acomplish = (byte) ((po.getString(getPropertyOfHorseLevel(10)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfHorseLevel(10));
		}
		
		if(ach.achieveId == 51){
			ach.acomplish = (byte) ((po.getString(getPropertyOfHorseLevel(20)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfHorseLevel(20));
		}
		
		if(ach.achieveId == 52){
			ach.acomplish = (byte) ((po.getString(getPropertyOfHorseLevel(30)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfHorseLevel(30));
		}
		
		if(ach.achieveId == 53){
			ach.acomplish = (byte) ((po.getString(getPropertyOfHorseLevel(40)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfHorseLevel(40));
		}
		
		if(ach.achieveId == 54){
			ach.acomplish = (byte) ((po.getString(getPropertyOfHorseLevel(50)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfHorseLevel(50));
		}
		
		if(ach.achieveId == 55){
			ach.acomplish = (byte) ((po.getString(getPropertyOfHorseLevel(60)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfHorseLevel(60));
		}
		
		if(ach.achieveId == 56){
			ach.acomplish = (byte) ((po.getString(getPropertyOfHorseLevel(70)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfHorseLevel(70));
		}
		
		if(ach.achieveId == 57) {
			ach.acomplish = (byte) ((po.getString(getPropertyOfHorseEqu(1)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfHorseEqu(1));
		}
		
		if(ach.achieveId == 58) {
			ach.acomplish = (byte) ((po.getString(getPropertyOfHorseEqu(3)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfHorseEqu(3));
		}
		
		if(ach.achieveId == 59) {
			ach.acomplish = (byte) ((po.getString(getPropertyOfHorseEqu(7)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfHorseEqu(7));
		}
		
		if(ach.achieveId == 60){
			ach.acomplish = (byte) ((po.getString(getPropertyOfIMoney(imoney[0],true)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfIMoney(imoney[0],true));
		}
		
		if(ach.achieveId == 61){
			ach.acomplish = (byte) ((po.getString(getPropertyOfIMoney(imoney[1],true)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfIMoney(imoney[1],true));
		}
		
		if(ach.achieveId == 62){
			ach.acomplish = (byte) ((po.getString(getPropertyOfIMoney(imoney[2],true)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfIMoney(imoney[2],true));
		}
		
		if(ach.achieveId == 63){
			ach.acomplish = (byte) ((po.getString(getPropertyOfIMoney(imoney[3],true)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfIMoney(imoney[3],true));
		}
		
		if(ach.achieveId == 64){
			ach.acomplish = (byte) ((po.getString(getPropertyOfIMoney(imoney[4],true)) == "") ? 0 : 1);
			ach.finiTime = po.getString(getPropertyOfIMoney(imoney[4],true));
		}
	}
}

class Kill{
	public int killId;
	public int killFaction;
	public int killedId;
	public int killedFaction;
	public int killClazz;
	public int killedClazz;
}

class FactionKill{
	public int killId;
	public int killFaction;
	public int killedId;
	public int killedFaction;
	public int killClazz;
	public int killedClazz;
}

class Achievement {
	public int achieveId;
	public int type;
	public String achievementName;
	public String dec;
	public byte acomplish;
	public int point;
	public String finiTime = "";

	public Achievement(int achieveId, int type, String achievementName,
			String dec, int point) {
		this.achieveId = achieveId;
		this.type = type;
		this.achievementName = achievementName;
		this.dec = dec;
		this.point = point;
	}
}

@SuppressWarnings("unchecked")
class ConverSort implements Comparator {
	public int compare(Object o1, Object o2) {
		Achievement p1 = (Achievement) o1;
		Achievement p2 = (Achievement) o2;
		if (p1.acomplish < p2.acomplish)
			return 1;
		else 
			return 0;
	}
}

