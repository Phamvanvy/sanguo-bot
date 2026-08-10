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
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import com.pip.sanguo.data.item.Item;
import peony.game.Actor;
import peony.game.CommonUtil;
import peony.game.DayListener;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameItemObject;
import peony.game.GameObject;
import peony.game.Horse;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.PropertyPool;
import peony.game.Server;
import peony.game.TransactionBagGrid;
import peony.game.Unit;
import peony.game.beautyparade.Beauty;
import peony.game.itemenhance.ItemEnhance;
import peony.game.itemenhance.NaturalEnhance;
import peony.game.nation.Nation;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.produce.ProduceService;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.cards.CardGroup;
import peony.service.cards.CardService;
import peony.service.fame.Fame;
import peony.service.fame.FameService;
import peony.service.friend.PlayerRelation;
import peony.service.tong.TongMember;
import peony.service.tong.TongService;
import peony.util.TimeUtil;
import peony.vm.ASMQuest;

public class StatService implements Service,DayListener,ServiceEventListener{
	
	private static final Logger log = Logger.getLogger(StatService.class);
	
	public static int TOP_COUNT = 20;
	
	protected Map<Integer,PvpInfo> pvpInfos = new HashMap<Integer,PvpInfo>(); 
	protected BlockingQueue<Object> kills = new LinkedBlockingQueue<Object>();
	protected Map<Integer, List<Achievement>> achievement = new HashMap<Integer, List<Achievement>>();
	public Map<Integer,List<Achievement>> subAchievement = new HashMap<Integer,List<Achievement>>();
	protected Map<Integer,List<Integer>> achieveType = new HashMap<Integer,List<Integer>>();

	@SuppressWarnings("unchecked")
	protected List[] topPvpInfos = new List[4];
	
	@SuppressWarnings("unchecked")
	protected List[] topWeekRanks = new List[4];
	
	@SuppressWarnings("unchecked")
	protected List[] topLevelRanks = new List[4];
	
	protected static long ONEDAY = 24 * 3600 * 1000L;
	
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
	public static String PROPERTY_PRODUCE_EQUIPCOUNT = "produceequipcount";//打造装备的数目
	public static String PROPERTY_BEAUTY_TOPTEN = "beautytopten";//选美进入前十
	public static String PROPERTY_KILLENEMY_FIRSTTIME = "killenemyfirsttime";//第一次击杀帝国人
	public static String PROPERTY_CANDIDATE_KING = "candidateking";//当选国公
	public static String PROPERTY_KILLCREATRUE_COUNT = "killcreaturecount";//杀死怪物数量
	public static String PROPERTY_MERGE_JEWEL = "mergejewel";//合成宝石
	public static String PROPERTY_EQUIP_ADDHOLE = "equipaddhole";//打孔
	public static String PROPERTY_EQUIP_ADDJEWEL = "equipaddjewel";//镶嵌宝石
	public static String PROPERTY_EQUIP_REMOVEJEWEL ="equipremovejewel";//摘除宝石
	public static String PROPERTY_ONLINE_TIME = "onlinetime"; //玩家累积在线时间
	public static String PROPERTY_FINISHTIME_NATIONQUEST="opennationquest";//国公发布一次国家任务
	public static String PROPERTY_FINISHTIME_TONGQUEST="opentongquest";//发布一次军团任务
	public static String PROPERTY_PKWIN_COUNT = "pkwincount";//切磋胜利次数
	public static String PROPERTY_FINISHTIME_BIGBOX = "getbigbox";//在西域获得大宝箱
	public static String PROPERTY_FINISHTIME_SMALLBOX = "getsmallbox";//在西域获得小宝箱
	public static String PROPERTY_FINISHTIME_EXPANSIONBATTLE = "expansionbattlewin";//获得司隶战役胜利
	public static String PROPERTY_FINISHTIME_USEYIHESU = "useyihesu";//品尝一盒酥
	public static String PROPERTY_FINISHTIME_USESIFANGCAI = "usesifangcai";//品尝貂蝉私房菜
	public static String PROPERTY_USECOUNT_FOOD = "usefoodcount";//品尝美食的数值
	
	public static final int NORMAL_QUEST_ACHIEVETYPE = 0; //常规任务类型
	public static final int FAME_QUEST_ACHIEVETYPE = 1; //荣誉任务类型
	public static final int KILL_ENEMY_ACHIEVETYPE = 2; //杀死敌国玩家类型
	public static final int NATION_COLLECT_ACHIEVETYPE = 3; //国家捐款类型
	public static final int BATTLE_WIN_ACHIEVETYPE = 4; //战争类型
	public static final int MONEY_UP_ACHIEVETYPE = 5; //金钱类型
	public static final int FOOD_CONSUME_ACHIEVETYPE = 6; //美食类型
	public static final int LIFE_LEFT_ACHIEVETYPE = 7; //生活其它类型
	public static final int PLAYER_LEVELUP_ACHIEVETYPE = 8;//成长历程类型
	public static final int ONLINE_TIME_ACHIEVETYPE = 9; //累积在线类型
	public static final int WARE_JEWEL_ACHIEVETYPE = 10; //穿戴宝石类型
	public static final int WARE_JEWELCOUNT_ACHIEVETYPE = 11; //穿戴宝石类型
	public static final int AUCTION_SELL_ACHIEVETYPE = 12; //拍卖类型
	public static final int AUCTION_BUY_ACHIEVETYPE = 13; //拍买类型
	public static final int KILL_KINT_ACHIEVETYPE = 14; //拍卖类型
	public static final int HORSE_LEVELUP_ACHIEVETYPE = 15; //坐骑升级类型
	public static final int HORSE_EQUCOUNT_ACHIEVETYPE = 16; //拥有马装数量类型
	public static final int HORSE_CATOCOUNT_ACHIEVETYPE = 17; //拥有坐骑种类类型
	public static final int IMONEY_CONSUME_ACHIEVETYPE = 18; //消费I币类型
	public static final int PRODUCE_PRACTICE_ACHIEVETYPE = 19; //打造熟练度类型
	public static final int PRODUCE_EQUIPMENT_ACHIEVETYPE = 20; //打造马装或装备类型
	public static final int PRODUCE_NUMBER_ACHIEVETYPE = 21; //打造数量类型
	public static final int EQUIPMENT_QUALITY_ACHIEVETYPE = 22; //拥有装备质量类型
	public static final int EQUIPMENT_ADDHOLE_ACHIEVETYPE = 23; //装备打孔镶嵌类型
	public static final int EQUIPMENT_ENHANCE_ACHIEVETYPE = 24; //五孔数量类型
	public static final int EQUIPMENT_RESULT_ACHIEVETYPE = 25; //装备效果类型
	public static final int TITLE_COUNT_ACHIEVETYPE = 26; //称号种类类型
	public static final int CARD_COLLECT_ACHIEVETYPE = 27; //卡片数量类型
	public static final int CARD_GROUP_ACHIEVETYPE = 28; //卡片数量类型
	public static final int KILL_CREATURE_ACHIEVETYPE = 29; //击杀怪物类型
	public static final int QUEST_REPEAT_ACHIEVETYPE = 30; //任务重复类型
	public static final int QUEST_ONCE_ACHIEVETYPE = 31; //一次任务类型
	public static final int OTHERTYPE_ONCE_ACHIEVETYPE= 32; //其它散类型
	public static final int OTHERTYPE_REPEAT_ACHIEVETYPE = 33; //其它重复类型
	
	
	

	public boolean runStat = true;
	
	public int[] questIds = {1895,1896,1897,1690,1699,1700,1751,161,382,1652}; //被监听的任务ID
	
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
	
	public String[] questName = {"Tình khiên thất tịch","Tình khiên thất tịch","Tình khiên thất tịch","Ngày lành trung thu","Cảm ơn đái nghĩa","Cảm ơn đái nghĩa","Cảm ơn đái nghĩa","为国效力","Mô Kim hiệu ủy","大破天龙阵"};
	
	public int[] clazzs = {0,1,2,3}; // 代表四种职业
	
	public int[] faction = {GameObject.FACTION_WEI,GameObject.FACTION_SHU,GameObject.FACTION_WU};

	public int[] kkgIds = {1503,1504,1502};//暗杀国君的三个国家的任务id,按魏，蜀，吴排列
	
	public int[] questType = {0,1,2,3,4};//分别为以下五种任务
	//任务类成就所需要监听的任务id
	public int[] ydzQuestIds = {901,902,903,904,905,906,907,908,909,910,945,946,947};  //衣代诏任务
	public int[] dhmlQuestIds = {567,568,569,570,571,572,573,574,575,576,577,578,579,580,581}; //大汉密令任务
	
	public int[] nationQuestIds = {1489,1454,1490,1453,1491,1452}; //国战任务
	public int[] battleQuestIds = {632,633,634}; //战场任务
	public int[] tongbattleQuestIds = {1176,1177,1178}; //团战任务
	
	
	public int[] foodIds = {1945,632,633,634,635,641,642,646,647,648,649,664};  //玩家品尝美食ID
	
	
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
				ServiceEvent.EVENT_PRODUCE,
				ServiceEvent.EVENT_BATTLE_WIN,
				ServiceEvent.EVENT_ADD_TITLE,
				ServiceEvent.EVENT_ADD_HORSE,
		        ServiceEvent.EVENT_COLLECT_CARD,
		        ServiceEvent.EVENT_CHANGE_EQUIP,
		        ServiceEvent.EVENT_MERGEJEWEL,
		        ServiceEvent.EVENT_DIG_SUCCESS,
		        ServiceEvent.EVENT_EXTIRPADE,
		        ServiceEvent.EVENT_ENHANCE,
		        ServiceEvent.EVENT_USEITEM,
		        ServiceEvent.EVENT_BEAUTY_END,
		        ServiceEvent.EVENT_FINISH_QUEST,
		        ServiceEvent.EVENT_PK_END,
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

		      //统计常规任务
			 for (int i = 0; i < questIds.length; i++) {
				int questId = questIds[i];
				if (p.asmVm.taskFinished(questId) == 1) {
					long time = p.asmVm.getFinishTime(questId);
					String timeStr = getTimeProperty(questId);
					if(pvpInfo.pool.getString(timeStr)==""){
					   pvpInfo.pool.setString(timeStr, getFinishTime(time));
					}
				}
			}
		    //完美任务
		    int finishCount = p.asmVm.getFinishedQuest(p);
			if(finishCount >= 300 && pvpInfo.pool.getString(StatService.PROPERTY_FINISHTIME_PERFECT) == ""){
				pvpInfo.pool.setString(StatService.PROPERTY_FINISHTIME_PERFECT,getFinishTime(System.currentTimeMillis()));
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
			//为国为民成就
			List<Achievement> list = getAchievementList(NATION_COLLECT_ACHIEVETYPE);
			if(list!=null){
				Achievement ach = list.get(0);
				if(oldCollect + money >= Integer.parseInt(ach.param1) && pvpInfo.pool.getString(PROPERTY_FINISHTIME_COLLECT) == ""){
					pvpInfo.pool.setString(PROPERTY_FINISHTIME_COLLECT, getFinishTime(System.currentTimeMillis()));
					setMessage(p,ach,true);
				}
			}
		}
	}
	
	protected void playerMarriaged(Player p, Player mate){
		if(p!=null){
			PvpInfo pvpInfo1 = getPvpInfo(p.id, p.faction);
			PvpInfo pvpInfo2 = getPvpInfo(mate.id, mate.faction);
			List<Achievement> list = getAchievementList(LIFE_LEFT_ACHIEVETYPE);
			for(Achievement a : list){
				if(a.param1.equals("marrage")){
					//洞房花烛成就
					if(pvpInfo1.pool.getString(PROPERTY_FINISHTIME_MARRIAGE)==""){
						pvpInfo1.pool.setString(PROPERTY_FINISHTIME_MARRIAGE, getFinishTime(System.currentTimeMillis()));
						setMessage(p,a,true);
					}
					if(pvpInfo2.pool.getString(PROPERTY_FINISHTIME_MARRIAGE)==""){
					    pvpInfo2.pool.setString(PROPERTY_FINISHTIME_MARRIAGE, getFinishTime(System.currentTimeMillis()));
					    setMessage(mate,a,true);
				   }
				}
			}
			
		}
	}
	
	protected void playerMoneyUp(Player p, int oldMoney, int value){
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(p.id, p.faction);
			List<Achievement> list = getAchievementList(MONEY_UP_ACHIEVETYPE);
			if(list != null){
				for(Achievement a : list){
					int money = Integer.parseInt(a.param1);
					if(money == 10000 && (oldMoney + value) >= money && pvpInfo.pool.getString(PROPERTY_FINISHTIME_TENTHOU) == "") {//万元户成就
						pvpInfo.pool.setString(PROPERTY_FINISHTIME_TENTHOU, getFinishTime(System.currentTimeMillis()));
						setMessage(p,a,false);
					} else if(money == 1000000 && (oldMoney + value) >= money && pvpInfo.pool.getString(PROPERTY_FINISHTIME_MILLIONARE) == "") {//百万富翁成就
						pvpInfo.pool.setString(PROPERTY_FINISHTIME_MILLIONARE, getFinishTime(System.currentTimeMillis()));
						setMessage(p,a,false);
				    }
			    }
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
		
		//统计击杀怪物数量
		if(u1.type == GameObject.TYPE_CREATURE && u2 instanceof Player){
			Player p = (Player)u2;
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			int killCount = p.pool.getInt(PROPERTY_KILLCREATRUE_COUNT, 0);
			killCount ++;
			p.pool.setInt(PROPERTY_KILLCREATRUE_COUNT, killCount);
			List<Achievement> listKillCreature = getAchievementList(KILL_CREATURE_ACHIEVETYPE);
			if(listKillCreature!=null){
				int maxCount = Integer.parseInt(listKillCreature.get(listKillCreature.size()-1).param1);
				if(pvpInfo.pool.getString(getKillCreatureProperty(maxCount)) == ""){
					for(Achievement a : listKillCreature){
				        int count = Integer.parseInt(a.param1);
						if(killCount>=count && pvpInfo.pool.getString(getKillCreatureProperty(count)) == ""){
						   pvpInfo.pool.setString(getKillCreatureProperty(count), getFinishTime(System.currentTimeMillis()));
						   setMessage(p,a,false);
						}
					}
				}
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
			List<Achievement> achs = getAchievementList(KILL_ENEMY_ACHIEVETYPE);
			if(achs!=null){
				for(Achievement a : achs){
					int count = Integer.parseInt(a.param1);
					if(killCount>=count){
						if(count==1 && killInfo.pool.getString(PROPERTY_KILLENEMY_FIRSTTIME) == ""){//手刃敌人成就
							killInfo.pool.setString(PROPERTY_KILLENEMY_FIRSTTIME, getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,false);
						} else if(count == 100 && killInfo.pool.getString(PROPERTY_FINISHTIME_KILL) == ""){//英勇杀敌成就
							killInfo.pool.setString(PROPERTY_FINISHTIME_KILL, getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,true);
						} else if(count == 500 && killInfo.pool.getString(PROPERTY_FINISHTIME_KILLMORE) == ""){//所向披靡成就
							killInfo.pool.setString(PROPERTY_FINISHTIME_KILLMORE, getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,true);
						}
					}
				}
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
	
	@SuppressWarnings("unchecked")
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
		case ServiceEvent.EVENT_PRODUCE:
			playerProduce((Integer)event.param1,(Integer)event.param2,(Integer)event.param3,(Integer)event.param4);
			break;
		case ServiceEvent.EVENT_BATTLE_WIN:
			playerBattleWin((Integer)event.param1,(Integer)event.param2,(Integer)event.param3);
			break;
		case ServiceEvent.EVENT_ADD_TITLE:
			playerAddTitle((Player)event.param1);
			break;
		case ServiceEvent.EVENT_ADD_HORSE:
			playerAddHorse((Player)event.param1);
			break;
		case ServiceEvent.EVENT_BEAUTY_END:
			beautyEnd((Integer)event.param1);
			break;
		case ServiceEvent.EVENT_COLLECT_CARD:
			playerCollectCard((Player)event.param1,(Integer)event.param2);
			break;
		case ServiceEvent.EVENT_CHANGE_EQUIP:
			playerChangeEquip((Player)event.param1);
			break;
		case ServiceEvent.EVENT_MERGEJEWEL:
			playerMergeJewel((Player)event.param1);
			break;
		case ServiceEvent.EVENT_DIG_SUCCESS:
			playerAddHole((Player)event.param1);
			break;
		case ServiceEvent.EVENT_EXTIRPADE:
			playerExtirpade((Player)event.param1);
			break;
		case ServiceEvent.EVENT_ENHANCE:
			processEnhance((Player)event.param1, (Integer)event.param2);
			break;
		case ServiceEvent.EVENT_USEITEM:
			processUseItem((Player)event.param1, (Integer)event.param2,(Integer)event.param3);
			break;
		case ServiceEvent.EVENT_FINISH_QUEST:
			playerFinishQuest((Player)event.param1, (Integer)event.param2,(Integer)event.param3);
			break;
		case ServiceEvent.EVENT_PK_END:
			playerPKEnd((Player)event.param1);
			break;
		}
	}
	
	public void playerPKEnd(Player winner){
		if(winner!=null){
			PvpInfo pvpInfo = getPvpInfo(winner.id, winner.faction);
			List<Achievement> listPk = getAchievementList(OTHERTYPE_REPEAT_ACHIEVETYPE);
			int totalCount = winner.pool.getInt(StatService.PROPERTY_PKWIN_COUNT,0);
			totalCount++;
			if(listPk != null){
				for(Achievement a:listPk){
					int type = Integer.parseInt(a.param1);
					if(type == 1){//PK
						int count = Integer.parseInt(a.param2);
						if(pvpInfo.pool.getString(getPropertyOfPkWin(count)) == ""){
							if(totalCount>=count){
								pvpInfo.pool.setString(getPropertyOfPkWin(count), getFinishTime(System.currentTimeMillis()));
								setMessage(winner, a, false);
							}
							winner.pool.setInt(PROPERTY_PKWIN_COUNT, totalCount);
						}
					}
				}
			}
		}
	}
	
	public void playerFinishQuest(Player p,int questId,int branch){
		if(p!=null){
			long time = p.asmVm.getFinishTime(questId);
			PvpInfo pvpInfo = getPvpInfo(p.id, p.faction);
			//常规任务类成就
			List<Achievement> achs = subAchievement.get(NORMAL_QUEST_ACHIEVETYPE);
			List<Achievement> ach2 = subAchievement.get(FAME_QUEST_ACHIEVETYPE);
			achs.addAll(ach2);
			if(achs!=null && achs.size()>0){
				for(int i=0;i<achs.size();i++){
					Achievement ach = achs.get(i);
					String strs = ach.param1;
					if(strs!=null){
						if(strs.equals("perfect")){ //完美任务
							int finishCount = p.asmVm.getFinishedQuest(p);
							if(finishCount >= 300 && pvpInfo.pool.getString(StatService.PROPERTY_FINISHTIME_PERFECT) == ""){
								pvpInfo.pool.setString(StatService.PROPERTY_FINISHTIME_PERFECT,getFinishTime(time));
								setMessage(p,ach,false);
							}
						}else{ //常规任务
							String[] str = strs.split(",");
							for(int j=0;j<str.length;j++){
								if(questId == Integer.parseInt(str[j])){
									String timeStr = getTimeProperty(questId);
									if(pvpInfo.pool.getString(timeStr)==""){
										 pvpInfo.pool.setString(timeStr, getFinishTime(time));
										 setMessage(p,ach,false);
									 }
								}
							}
						}
					}
				}
			}
			
			
			//任务类成就
			List<Achievement> listQuestRepeat = getAchievementList(QUEST_REPEAT_ACHIEVETYPE);
			if(listQuestRepeat!=null){
				int ty = -1;
				for(Achievement a:listQuestRepeat){
					int type = Integer.parseInt(a.param1);
					int count = p.pool.getInt(getPropertyOfQuestCount(type), 0);
					if(ty!=type){
						count++;
						ty=type;
					}
					int index = -1;
					if(type == 0){
						index = isInArray(ydzQuestIds,questId);
					}else if(type==1){
						index = isInArray(dhmlQuestIds,questId);
					}
					if(index!=-1){
						int num = Integer.parseInt(a.param2);
						if(pvpInfo.pool.getString(getPropertyOfFinishQuest(type,num)) == ""){
							if(count>=num){
								pvpInfo.pool.setString(getPropertyOfFinishQuest(type,num),getFinishTime(System.currentTimeMillis()));
								setMessage(p,a,false);
							}
						}
						p.pool.setInt(getPropertyOfQuestCount(type), count);
					}
				}	
			}
			
			List<Achievement> listOnceRepeat = getAchievementList(QUEST_ONCE_ACHIEVETYPE);
			if(listOnceRepeat!=null){
				for(Achievement a:listOnceRepeat){
					int type = Integer.parseInt(a.param1);
					String[] str = a.param2.split(",");
					for(int i=0;i<str.length;i++){
						if(questId == Integer.parseInt(str[i])&& pvpInfo.pool.getString(getPropertyOfFinishQuest(type,1)) == ""){
							pvpInfo.pool.setString(getPropertyOfFinishQuest(type,1),getFinishTime(System.currentTimeMillis()));
						    setMessage(p,a,false);
						}
					}
				}
			}
	
		}
	}
				
	
	public void processUseItem(Player p,int itemId,int cnt){
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
            List<Achievement> list = getAchievementList(FOOD_CONSUME_ACHIEVETYPE);
            if(list!=null){
    			int count = p.pool.getInt(PROPERTY_USECOUNT_FOOD, 0);
    			int maxCount = Integer.parseInt(list.get(list.size()-1).param2);
    			count += cnt;
            	for(Achievement a : list){
            		if(!a.param1.equals("") && a.param2.equals("")){
            			int itId = Integer.parseInt(a.param1);
            			if(itId == itemId){
	            			if(itemId == 1183 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_USEYIHESU) == ""){
	            				pvpInfo.pool.setString(PROPERTY_FINISHTIME_USEYIHESU, getFinishTime(System.currentTimeMillis()));
	        					setMessage(p,a,false);
	            			} else if(itemId == 1945 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_USESIFANGCAI) == ""){
	            				pvpInfo.pool.setString(PROPERTY_FINISHTIME_USESIFANGCAI, getFinishTime(System.currentTimeMillis()));
	        					setMessage(p,a,false);
	            			}
            			}
            		}else if(!a.param2.equals("") && a.param1.equals("")&& pvpInfo.pool.getString(getPropertyOfUseFood(maxCount)) == ""){
        				int num = Integer.parseInt(a.param2);
            			if(pvpInfo.pool.getString(getPropertyOfUseFood(num)) == ""){
            				if(count>=num){
	            				pvpInfo.pool.setString(getPropertyOfUseFood(num), getFinishTime(System.currentTimeMillis()));
	    						setMessage(p,a,false);
            				}
            			}
            			p.pool.setInt(PROPERTY_USECOUNT_FOOD, count);
            		}
            	}
            }
		}
	}
	
	
	/**
	 * 星级和资质鉴定事件
	 * @param p
	 * @param type
	 */
	public void processEnhance(Player p,int type){
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			if(type == 0){
				//拥有双完美装备
//				if(pvpInfo.pool.getString(getEquipmentProperty1(-1,-1,10)) == ""){
//					processEquipCount(p,3,false,true);
//				}
			} else if(type == 1){
				//拥有装备的星级
				List<Achievement> listStar = getAchievementList(EQUIPMENT_RESULT_ACHIEVETYPE);
				if(listStar!=null){
					int totalStar = p.getAveStar();
					for(Achievement a:listStar){
						int t = Integer.parseInt(a.param1);
						int num = Integer.parseInt(a.param2);
						if(t==0){
							if(totalStar >= num && pvpInfo.pool.getString(getEquipmentProperty2(num,-1)) == ""){
								pvpInfo.pool.setString(getEquipmentProperty2(num,-1), getFinishTime(System.currentTimeMillis()));
								setMessage(p,a,false);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 玩家摘除宝石事件
	 * @param p
	 */
	public void playerExtirpade(Player p){
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			List<Achievement> listAddHole = getAchievementList(EQUIPMENT_ADDHOLE_ACHIEVETYPE);
			if(listAddHole!=null){
				for(Achievement a:listAddHole){
					int type = Integer.parseInt(a.param1);
					if(type == 4){
						if(pvpInfo.pool.getString(PROPERTY_EQUIP_REMOVEJEWEL) == ""){
							pvpInfo.pool.setString(PROPERTY_EQUIP_REMOVEJEWEL, getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,false);
						}
					}
				}
			}		
		}
	}
	
	/**
	 * 玩家打孔事件
	 * @param p
	 */
    public void playerAddHole(Player p){
    	if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			List<Achievement> listAddHole = getAchievementList(EQUIPMENT_ADDHOLE_ACHIEVETYPE);
			if(listAddHole!=null){
				for(Achievement a:listAddHole){
					int type = Integer.parseInt(a.param1);
					if(type == 1){
						if(pvpInfo.pool.getString(PROPERTY_EQUIP_ADDHOLE) == ""){
							pvpInfo.pool.setString(PROPERTY_EQUIP_ADDHOLE, getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,false);
						}
					}
				}
			}
			
			//拥有五孔的情况
			if(pvpInfo.pool.getString(getEquipmentProperty1(-1,10,-1)) == ""){
				processEquipCount(p,2,false,true);
			}	
		}
    }
	
	/**
	 * 玩家合成宝石事件
	 * @param p
	 */
	public void playerMergeJewel(Player p){
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			List<Achievement> listMerge= getAchievementList(EQUIPMENT_ADDHOLE_ACHIEVETYPE);
			if(listMerge!=null){
				for(Achievement a:listMerge){
					int type = Integer.parseInt(a.param1);
					if(type == 2){
						if(pvpInfo.pool.getString(PROPERTY_MERGE_JEWEL) == ""){
							pvpInfo.pool.setString(PROPERTY_MERGE_JEWEL, getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,false);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 玩家换装事件
	 * @param p
	 * @param equ
	 */
	public void playerChangeEquip(Player p){
		if(p!=null){
			processEquipCount(p,1,true,true);
		}	
	}
	
	/**
	 * 
	 * @param p
	 * @param type 类型(1蓝装或紫装,2五孔装备,3双完美装备,4星级装备,5宝石光效)
	 * @param wholeOn 是否全部开放
	 */
	public void processEquipCount(Player p,int type,boolean wholeOn,boolean broadcast){
		if(p!=null && p.id>0){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			List<GameItem> equipments = new ArrayList<GameItem>();
			int countBlue = 0;
		    int countPurple = 0;
		    int count5Holes = 0;
		    int countPerfect = 0;
			for(GameItem item : p.equipments.equs){
				if(item != null){
				   equipments.add(item);
				}
			}
			if(equipments != null && equipments.size() > 0){
				for(GameItem item : equipments){
//					if(item.template.isEquipment()){
						if(item.template.quality == Item.QUALITY_BLUE){
							countBlue ++ ;
						} else if(item.template.quality == Item.QUALITY_PURPLE){
							countPurple ++;
						}
						GameItemObject itemObject = item.object;
						if(itemObject instanceof ItemEnhance){
							ItemEnhance enhance = (ItemEnhance)itemObject;
							int holes = enhance.addHole;
							if(holes >= 4){
								count5Holes ++;
							}
							NaturalEnhance[] naturalEnhance = enhance.getNaturals();
					        if(naturalEnhance != null && naturalEnhance.length>0){
					        	if(naturalEnhance[0].percent >= 29 && naturalEnhance[1].percent >= 29){
					        		countPerfect ++;
					        	}
					        }
						}
//					}
				}
			}
	        
			if(wholeOn || type == 1){//拥有蓝装或紫装
				List<Achievement> listQuality = getAchievementList(EQUIPMENT_QUALITY_ACHIEVETYPE);
		        if(listQuality!=null){
		        	for(Achievement a:listQuality){
		        		int count = Integer.parseInt(a.param1);
		        		int quality = Integer.parseInt(a.param2);
		        		if(quality == Item.QUALITY_BLUE && countBlue >= count && pvpInfo.pool.getString(getEquipmentProperty1(Item.QUALITY_BLUE,-1,-1)) == ""){
							pvpInfo.pool.setString(getEquipmentProperty1(Item.QUALITY_BLUE,-1,-1), getFinishTime(System.currentTimeMillis()));
							if(broadcast){
							   setMessage(p,a,false);
							}
						} else if(quality == Item.QUALITY_PURPLE && countPurple >= count && pvpInfo.pool.getString(getEquipmentProperty1(Item.QUALITY_PURPLE,-1,-1)) == ""){
							pvpInfo.pool.setString(getEquipmentProperty1(Item.QUALITY_PURPLE,-1,-1), getFinishTime(System.currentTimeMillis()));
							if(broadcast){
							   setMessage(p,a,false);
							}
						}
		        	}
				}
	        }
			
			if(wholeOn || type == 2){//拥有的五孔装备
				List<Achievement> listHoles = getAchievementList(EQUIPMENT_ENHANCE_ACHIEVETYPE);
		        if(listHoles!=null){
		        	for(Achievement a:listHoles){
		        		int holeCount = Integer.parseInt(a.param1);
						if(count5Holes >= holeCount && pvpInfo.pool.getString(getEquipmentProperty1(-1,holeCount,-1)) == ""){
							pvpInfo.pool.setString(getEquipmentProperty1(-1,holeCount,-1), getFinishTime(System.currentTimeMillis()));
							if(broadcast){
							    setMessage(p,a,false);
							}
						}
		        	}
				}
			}
//			if(wholeOn || type == 3){//拥有双完美装备
//				int[] perfectArr = {1,5,10};
//				int[] perfectSubArr = {17,18,19};
//				for(int i=0;i<perfectArr.length;i++){
//					if(countPerfect >= perfectArr[i] && pvpInfo.pool.getString(getEquipmentProperty1(-1,-1,perfectArr[i])) == ""){
//						pvpInfo.pool.setString(getEquipmentProperty1(-1,-1,perfectArr[i]), getFinishTime(System.currentTimeMillis()));
//						if(broadcast){
//						    setMessage(p,achieveType,perfectSubArr[i],false);
//						}
//					}
//				}
//			}
			if(wholeOn || type == 4){//拥有装备的星级
				List<Achievement> listStar = getAchievementList(EQUIPMENT_RESULT_ACHIEVETYPE);
				if(listStar!=null){
					int totalStar = p.getAveStar();
					for(Achievement a:listStar){
						int t = Integer.parseInt(a.param1);
						int num = Integer.parseInt(a.param2);
						if(t==0){
							if(totalStar >= num && pvpInfo.pool.getString(getEquipmentProperty2(num,-1)) == ""){
								pvpInfo.pool.setString(getEquipmentProperty2(num,-1), getFinishTime(System.currentTimeMillis()));
								if(broadcast){
								   setMessage(p,a,false);
								}
							}
						}
					}
				}
			}
			if(wholeOn || type == 5){//宝石光效
				List<Achievement> listFlash = getAchievementList(EQUIPMENT_RESULT_ACHIEVETYPE);
				if(listFlash!=null){
					int flashLevel = p.equipments.getFlashLevel();
					for(Achievement a : listFlash){
						int t = Integer.parseInt(a.param1);
						int level = Integer.parseInt(a.param2);
						if(t==1){
							if(flashLevel == level && pvpInfo.pool.getString(getEquipmentProperty2(-1,level)) == ""){
								pvpInfo.pool.setString(getEquipmentProperty2(-1,level), getFinishTime(System.currentTimeMillis()));
								if(broadcast){
								    setMessage(p,a,false);
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 收藏卡片事件
	 * @param p
	 * @param groupId 卡片所属套装Id
	 */
	public void playerCollectCard(GameObject player,int groupId){
		Player p = (Player) ObjectAccessor.getGameObject(player.id);
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			List<Achievement> listCard = getAchievementList(CARD_COLLECT_ACHIEVETYPE);
			if(listCard != null){
				int maxCount = Integer.parseInt(listCard.get(listCard.size()-1).param1);
				int cardCount = p.pool.getInt(CardService.PROPERTY_HAVECARD, 0);
				if(pvpInfo.pool.getString(getCardCollectProperty(maxCount,-1))==""){
					for(Achievement a:listCard){
						int count = Integer.parseInt(a.param1);
						if(cardCount >= count && pvpInfo.pool.getString(getCardCollectProperty(count,-1)) == ""){
							pvpInfo.pool.setString(getCardCollectProperty(count,-1), getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,false);
						}
					}
				}
			}
			
			CardService cardService = Server.server.getServiceRegistry().getCardService();
			CardGroup group = cardService.getCardGroup(groupId);
			List<Achievement> listCardGroup = getAchievementList(CARD_GROUP_ACHIEVETYPE);
			if(listCardGroup!=null){
				for(Achievement a:listCardGroup){
				   int grouId = Integer.parseInt(a.param1);
				   if(grouId == groupId){
						if(p.pool.getInt(cardService.getPropertyOfPlayerSuit(grouId), 0) == group.cards.size() && pvpInfo.pool.getString(getCardCollectProperty(-1,grouId)) == ""){
							pvpInfo.pool.setString(getCardCollectProperty(-1,grouId), getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,false);
						}
				   }
				}
			}
		}
	}
	
	/**
	 * 选美结束事件
	 * @param beautys 选美TOP10集合
	 */
	public void beautyEnd(int playerId){
		Player p = ObjectAccessor.getPlayer(playerId);
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			List<Achievement> list = getAchievementList(LIFE_LEFT_ACHIEVETYPE);
			if(list!=null){
				for(Achievement a : list){
					if(a.param1.equals("beauty") && pvpInfo.pool.getString(PROPERTY_BEAUTY_TOPTEN) == ""){
						pvpInfo.pool.setString(PROPERTY_BEAUTY_TOPTEN, getFinishTime(System.currentTimeMillis()));
						setMessage(p,a,false);
					}
				}
			}
		}
	}
	
	/**
	 * 玩家获得坐骑事件
	 * @param p
	 */
	public void playerAddHorse(Player p){
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			List<Achievement> list = getAchievementList(HORSE_CATOCOUNT_ACHIEVETYPE);
			if(list!=null){
				int count = getHorseCategory(p);
				int maxCount = Integer.parseInt(list.get(list.size()-1).param1);
				if(count <= maxCount){
					for(int i=0;i<list.size();i++){
						Achievement a = list.get(i);
						if(a!=null){
							int cnt = Integer.parseInt(a.param1);
							if(count >= cnt && pvpInfo.pool.getString(getHorseCountProperty(cnt)) == ""){
								pvpInfo.pool.setString(getHorseCountProperty(cnt), getFinishTime(System.currentTimeMillis()));
								setMessage(p,a,false);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 统计玩家拥有坐骑的种类
	 * @param p
	 * @return
	 */
	public int getHorseCategory(Player p){
		int count = 0;
		if(p!=null && p.id>0){
			List<Horse> horses = p.horseBag.horses;
			if(horses!=null && horses.size()>0){
				List<Integer> tempList = new ArrayList<Integer>();
				for(int i=0;i<horses.size();i++){
					if(!tempList.contains(horses.get(i).imageId)){
						count++;
					}
					tempList.add(horses.get(i).imageId);
				}
			}
		}
		return count;
	}
	
	/**
	 * 玩家获得称号事件
	 * @param p
	 */
    public void playerAddTitle(Player p){
    	if(p!=null){
    		PvpInfo pvpInfo = getPvpInfo(p.id, p.faction);
    		List<Achievement> listAddTitle =getAchievementList(TITLE_COUNT_ACHIEVETYPE);
    		if(listAddTitle!=null){
	    		int titleCount = p.titles.titles.size();
	    		int maxNum = Integer.parseInt(listAddTitle.get(listAddTitle.size()-1).param1);
	    		if(titleCount <= maxNum){
	    			for(Achievement a:listAddTitle){
	    				int num = Integer.parseInt(a.param1);
	    				if(titleCount >= num && pvpInfo.pool.getString(getTitleCountProperty(num)) == ""){
	    					pvpInfo.pool.setString(getTitleCountProperty(num), getFinishTime(System.currentTimeMillis()));
	    	    			setMessage(p,a,false);
	    				}
	    			}
	    		}
    		}
    	}
    }
	
	/**
	 *  国战胜利事件
	 * @param p
	 * @param type
	 * @param victorySide
	 */
	public void playerBattleWin(int playerId,int type,int victorySide){
		Player p = ObjectAccessor.getPlayer(playerId);
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			List<Achievement> achs = getAchievementList(BATTLE_WIN_ACHIEVETYPE);
			if(achs != null){
				for(Achievement a : achs){
					int t = Integer.parseInt(a.param1); //成就战争类型 0国战
					int v = Integer.parseInt(a.param2); //胜利方0防守方，1进攻方
					if(type == t && victorySide == v){ 
						if(pvpInfo.pool.getString(getBattleWinProperty(type,victorySide)) == ""){
							pvpInfo.pool.setString(getBattleWinProperty(type,victorySide), getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,false);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 玩家打造事件
	 * @param p
	 * @param outType 产出装备类型 0为角色装备，1为坐骑装备
	 * @param formulaLevel 配方等级
	 */
	
	public void playerProduce(int playerId,int outType,int practiceLevel,int formulaLevel){
		Player p = ObjectAccessor.getPlayer(playerId);
		PvpInfo pvpInfo = getPvpInfo(playerId, p.faction);
		if(p!=null){
			List<Achievement> listProduce = getAchievementList(PRODUCE_EQUIPMENT_ACHIEVETYPE);
			if(listProduce!=null){
				for(Achievement a:listProduce){
					if(a.param2.equals("")){
						int oType = Integer.parseInt(a.param1);
						if(oType == outType && pvpInfo.pool.getString(getProduceProperty(-1,-1,outType,-1)) == ""){
							pvpInfo.pool.setString(getProduceProperty(-1,-1,outType,-1), getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,false);
						}
					} else {
						int oType = Integer.parseInt(a.param1);
						int forLevel = Integer.parseInt(a.param2);
						if(outType!=-1 && oType == outType && forLevel == formulaLevel && pvpInfo.pool.getString(getProduceProperty(-1,formulaLevel,outType,-1)) == ""){
							pvpInfo.pool.setString(getProduceProperty(-1,formulaLevel,outType,-1), getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,false);	
					    }	
				    }
				}
			}
			
			List<Achievement> listPractice = getAchievementList(PRODUCE_PRACTICE_ACHIEVETYPE);
			if(listPractice!=null){
				for(Achievement a : listPractice){
					int practice = Integer.parseInt(a.param1);
					if(practiceLevel>=practice && pvpInfo.pool.getString(getProduceProperty(practice,-1,-1,-1))==""){
						pvpInfo.pool.setString(getProduceProperty(practice,-1,-1,-1), getFinishTime(System.currentTimeMillis()));
						setMessage(p,a,false);
					}
				}
			}
				
			List<Achievement> listCount = getAchievementList(PRODUCE_NUMBER_ACHIEVETYPE);
			if(listPractice!=null){
				int maxNum = Integer.parseInt(listCount.get(listCount.size()-1).param1);
				int count = p.pool.getInt(PROPERTY_PRODUCE_EQUIPCOUNT, 0);
				if(count < maxNum){
					count++;
					p.pool.setInt(PROPERTY_PRODUCE_EQUIPCOUNT, count);
				    for(Achievement a : listCount){
				    	int num = Integer.parseInt(a.param1);
						if(count >= num && pvpInfo.pool.getString(getProduceProperty(-1,-1,-1,num)) == ""){
								pvpInfo.pool.setString(getProduceProperty(-1,-1,-1,num),getFinishTime(System.currentTimeMillis()));
								setMessage(p,a,false);
						}
					}
				}
			}
		}
	}
	
	/** 马换装时统计马装成就 */
	public void horseEquip(Player p){
		if(p!=null && p.id>0){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			List<Achievement> list = getAchievementList(HORSE_EQUCOUNT_ACHIEVETYPE);
			if(list!=null){
				int maxNum = Integer.parseInt(list.get(list.size()-1).param1);
				if(pvpInfo.pool.getString(getPropertyOfHorseEqu(maxNum))==""){
					List<GameItem> items = new ArrayList<GameItem>();
					if(p.horse!=null){
						for(GameItem it : p.horse.equs.equs){
							if(it!=null){
								items.add(it);
							}
						}
						int num = items.size();
						for(int i=0;i<list.size();i++){
						   Achievement a = list.get(i);
						   if(a!=null){
							   int number = Integer.parseInt(a.param1);
							   if(num>=number && pvpInfo.pool.getString(getPropertyOfHorseEqu(number))==""){
								   pvpInfo.pool.setString(getPropertyOfHorseEqu(number), getFinishTime(System.currentTimeMillis()));
								   setMessage(p,a,false);
							   }
							   if(pvpInfo.pool.getString(getPropertyOfHorseEqu(number))==""){
								   break;
							   }
						   }
						}
					}
				}
			}
		}
	}
	
	/** 马升级时统计马的等级成就 */
	public void horseLevelUp(Player p){
		if(p!=null && p.id>0){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			List<Achievement> list = getAchievementList(HORSE_LEVELUP_ACHIEVETYPE);
			if(list!=null){
				int maxNum = Integer.parseInt(list.get(list.size()-1).param1);
				if(pvpInfo.pool.getString(getPropertyOfHorseLevel(maxNum)) == ""){
				     for(int i=0;i<list.size();i++){
				    	 Achievement a = list.get(i);
				    	 if(a!=null){
					    	 int level = Integer.parseInt(a.param1);
							 if(p.horse!=null && p.horse.level>=level && pvpInfo.pool.getString(getPropertyOfHorseLevel(level)) == ""){
								pvpInfo.pool.setString(getPropertyOfHorseLevel(level), getFinishTime(System.currentTimeMillis()));
								setMessage(p,a,false);
							 }
							 if(pvpInfo.pool.getString(getPropertyOfHorseLevel(level)) == ""){
								 break;
							 }
				    	 }
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
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(playerId,p.faction);
			List<Achievement> list = getAchievementList(IMONEY_CONSUME_ACHIEVETYPE);
			if(list!=null){
				int maxNum = Integer.parseInt(list.get(list.size()-1).param1);
				int total = 0;
				if(pvpInfo.pool.getString(getPropertyOfIMoney(maxNum,true)) == ""){
					if(pvpInfo.pool.getInt(PROPERTY_IMONEYUSE_COUNT, 0)==0){
					    total = Server.server.getServiceRegistry().getDbService().ibuyDAO.getTotalConsumeTillNow(playerId);
						pvpInfo.pool.setInt(PROPERTY_IMONEYUSE_COUNT,total);
					} else if(pvpInfo.pool.getInt(PROPERTY_IMONEYUSE_COUNT, 0) > 0){
						total = pvpInfo.pool.getInt(PROPERTY_IMONEYUSE_COUNT,0)+money;
						pvpInfo.pool.setInt(PROPERTY_IMONEYUSE_COUNT, total);
					}
					for(int i=0;i<list.size();i++){
						Achievement a = list.get(i);
						if(a!=null){
							int num = Integer.parseInt(a.param1);
							if(pvpInfo.pool.getString(getPropertyOfIMoney(num,true)) == ""){
								if(total >=num*3600 || pvpInfo.pool.getInt(PROPERTY_IMONEYUSE_COUNT, 0)<0){
									pvpInfo.pool.setString(getPropertyOfIMoney(num,true), getFinishTime(System.currentTimeMillis()));
									if(num==maxNum){
										setMessage(p,a,true);
									} else if(num<maxNum && pvpInfo.pool.getInt(PROPERTY_IMONEYUSE_COUNT, 0)>0){
										setMessage(p,a,false);
									}
								}
							} 
							if(pvpInfo.pool.getString(getPropertyOfIMoney(num,true)) == ""){
									break;
							}
						}
					}
				}
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
			
			//上线统计在线累积成就
			long time = p.pool.getLong(StatService.PROPERTY_ONLINE_TIME,0l);
			if(subAchievement!=null && subAchievement.size()>0){
				List<Achievement> achs = subAchievement.get(ONLINE_TIME_ACHIEVETYPE);
				for(int i=0;i<achs.size();i++){
					Achievement ach = achs.get(i);
					int t = Integer.parseInt(ach.param1);
					if(time>=t*60*60*1000l && pvpInfo.pool.getString(getPropertyOfOnlineTime(t)) == ""){
						pvpInfo.pool.setString(getPropertyOfOnlineTime(t), getFinishTime(System.currentTimeMillis()));
						setMessage(p,ach,false);
					}
				}
			}
		}
	}
	
	public void playerLevelUp(Player p){
	   if(p!=null && p.id>0){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			List<Achievement> achs = subAchievement.get(PLAYER_LEVELUP_ACHIEVETYPE);
			if(achs!=null && achs.size()>0){
				for(int i=0;i<achs.size();i++){
					Achievement ach = achs.get(i);
					int level = Integer.parseInt(ach.param1);
					if(p.level >= level && pvpInfo.pool.getString(getPropertyOfLevel(level)) == ""){
						pvpInfo.pool.setString(getPropertyOfLevel(level),getFinishTime(System.currentTimeMillis()));
						setMessage(p,ach,false);
					}
				}
			}
	    }
	}
	
	public void playerAddJewel(Player p,GameItem gameItem){
	   if(p!=null && p.id>0){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			int jewelLevel = ObjectAccessor.getItemTemplate(gameItem.template.id).useLevel;
			List<Achievement> list = getAchievementList(WARE_JEWEL_ACHIEVETYPE);
			if(list!=null){
				for(Achievement a : list){
					int count = Integer.parseInt(a.param1);
					int jeLevel = Integer.parseInt(a.param2);
					if(count==1 && jeLevel == jewelLevel){
						//得佩圣宝成就
						if(jewelLevel == 7 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_SEV) == ""){
							pvpInfo.pool.setString(PROPERTY_FINISHTIME_SEV,getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,false);
						}
						if(jewelLevel != 7 && pvpInfo.pool.getString(getPropertyByLevel(jeLevel)) == ""){
							pvpInfo.pool.setString(getPropertyByLevel(jeLevel), getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,false);
						}
					}
				}
			}
			countJewelOnEquipment(p,true);
			
			//成功完成一次宝石镶嵌成就
			
			List<Achievement> listAddJewel= getAchievementList(EQUIPMENT_ADDHOLE_ACHIEVETYPE);
			if(listAddJewel!=null){
				for(Achievement a:listAddJewel){
					int type = Integer.parseInt(a.param1);
					if(type == 3){
						if(pvpInfo.pool.getString(PROPERTY_EQUIP_ADDJEWEL) == ""){
							pvpInfo.pool.setString(PROPERTY_EQUIP_ADDJEWEL, getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,false);
						}
					}
				}
			}
			//拥有宝石光效成就
			processEquipCount(p,5,false,true);
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
    
    public String getPropertyOfGetGift(String achieveName){
    	return "PROPERTY_FINISHTIEM_GETGIFT" + achieveName;
    }
    
    /** 记录打造类属性 */
    public String getProduceProperty(int practiceLevel,int level,int equipment,int count){
    	return "PROPERTY_FINISHTIME_PRODUCE"+practiceLevel+level+equipment+count;
    }
    
    /**
     * 战场胜利属性
     * @param type 战争类型 0国战，1战场，2城战
     * @param victorySide 胜利方 0防守方胜利，1进攻方胜利
     * @return
     */
    public String getBattleWinProperty(int type,int victorySide){
    	return "PROPERTY_FINISHTIME_BATTLEWIN"+type+victorySide;
    }
    
    /**
     * 完成成就领取奖励属性
     * @param type 成就类型Id
     * @param achievement 成就子类型Id
     * @return
     */
    public String getRewardProperty(int type,int achievement) {
    	return "PROPERTY_FINISHTIME_GETREWARD"+type+achievement;
    }
    
    /**
     * 获得称号个数属性
     * @param count 获得称号总数
     * @return
     */
    public String getTitleCountProperty(int count){
    	return "PROPERTY_FINISHTIME_TITLECOUNT"+count;
    }
    
    /** 坐骑种类属性 */
    public String getHorseCountProperty(int count){
    	return "PROPERTY_FINISHTIME_HORSECOUNT"+count;
    }
    
    /** 收藏卡片属性 */
    public String getCardCollectProperty(int count,int groupId){
    	return "PROPERTY_FINISHTIME_CARDCOLLECT"+count+groupId;
    }
    
    /**
     * 击杀怪物属性
     * @param count 击杀怪物数量
     * @return
     */
    public String getKillCreatureProperty(int count){
    	return "PROPERTY_FINISHTIME_KILLCREATURE"+count;
    }
    
    /**
     * 完成任务属性
     * @param type
     * @param count
     * @return
     */
    public String getPropertyOfFinishQuest(int type,int count){
    	return "PROPERTY_FINISHTIME_FINISHQUEST"+type+count;
    }
    
    /**
     * 完成指定任务的数值
     * @param questType 任务类型
     * @return
     */
    public String getPropertyOfQuestCount(int questType){
    	return "PROPERTY_FINISHQUEST_COUNT" + questType;
    }
    
    /**
     * 玩家拥有的装备属性
     * @param quality 装备的品质
     * @param fiveHole 拥有五孔装备的件数
     * @param perfect 拥有双完美装备的件数
     * @return
     */
    public String getEquipmentProperty1(int quality,int fiveHole,int perfect){
    	return "PROPERTY_FINISHTIME_EQUIPTYPE"+quality+fiveHole+perfect;
    }
    
    /**
     * 玩家拥有的装备属性2
     * @param starLevel 星级装备
     * @param freshLevel 宝石光效
     * @return
     */
    public String getEquipmentProperty2(int starLevel,int freshLevel){
    	return "PROPERTY_FINISHTIME_EQUIPTYPE2"+starLevel+freshLevel;
    }
    
    /** 记录玩家在线时间的属性 */
    public String getPropertyOfOnlineTime(int hour){
    	return "PROPERTY_FINISHTIME_ONLINE" + hour;
    }
    
    /** 切磋胜利的属性 */
    public String getPropertyOfPkWin(int count){
    	return "PROPERTY_FINISHTIME_PKWIN"+count;
    }
    
    public String getPropertyOfUseFood(int count){
    	return "PROPERTY_FINISHTIME_USEFOOD"+count;
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
	protected void countJewelOnEquipment(Player p,boolean broadcast){
	  if(p!=null && p.id>0){
		PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
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
							pvpInfo.pool.setString(PROPERTY_FINISHTIME_SEV, getFinishTime(System.currentTimeMillis()));
						}
						if(jewelLevel != 7 && pvpInfo.pool.getString(getPropertyByLevel(jewelLevel)) == ""){
							pvpInfo.pool.setString(getPropertyByLevel(jewelLevel), getFinishTime(System.currentTimeMillis()));
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
		
		List<Achievement> list = getAchievementList(WARE_JEWELCOUNT_ACHIEVETYPE);
		if(list != null){
			int totalCount = pvpInfo.pool.getInt(PROPERTY_JEWEL_COUNT, 0);
			for(Achievement a : list){
				int jewelCount = Integer.parseInt(a.param1);
				if(totalCount >= jewelCount){
					if(a.param2==""){
					    if(jewelCount == 30 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_COUNT30) == ""){
					    	pvpInfo.pool.setString(PROPERTY_FINISHTIME_COUNT30, getFinishTime(System.currentTimeMillis()));
							if(broadcast){
								setMessage(p,a,false);
							}
					    } else if(jewelCount == 40 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_COUNT40)==""){
					    	pvpInfo.pool.setString(PROPERTY_FINISHTIME_COUNT40, getFinishTime(System.currentTimeMillis()));
							if(broadcast){
								setMessage(p,a,false);
							}
					    } else if(jewelCount == 50 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_COUNT50)==""){
					    	pvpInfo.pool.setString(PROPERTY_FINISHTIME_COUNT50, getFinishTime(System.currentTimeMillis()));
							if(broadcast){
								setMessage(p,a,false);
							}
					    }
					}else{
						int total = Integer.parseInt(a.param1);
						int level = Integer.parseInt(a.param2);
						if(level == 6){
							int jewSixCount = pvpInfo.pool.getInt(PROPERTY_JEWELSIX_COUNT, 0);
							if(jewSixCount>=total && pvpInfo.pool.getString(PROPERTY_FINISHTIME_6COUNT50)==""){
								pvpInfo.pool.setString(PROPERTY_FINISHTIME_6COUNT50, getFinishTime(System.currentTimeMillis()));
							    if(broadcast){
							    	setMessage(p,a,true);
							    }
							}
						}else if(level == 7){
							int jewSevCount = pvpInfo.pool.getInt(PROPERTY_JEWELSEVEN_COUNT, 0);
							if(jewSevCount >= total && pvpInfo.pool.getString(PROPERTY_FINISHTIME_7COUNT50)=="" ){
								pvpInfo.pool.setString(PROPERTY_FINISHTIME_7COUNT50, getFinishTime(System.currentTimeMillis()));
							    if(broadcast){
							    	setMessage(p,a,true);
							    }
							}
						}
					}
				 } 
			 }
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
       Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
		public void run() {
			try {
				synchronized (pvpInfos) {
					rebuildPvpInfos();
				}
				rebuildWeekCredits();
				rebuildLevelRanks();
			} catch (Exception e) {
				log.error(e, e);
			}
		}
       }, TimeUtil.getScheduleTimeMills(new Date(), 3, 0), ONEDAY, TimeUnit.MILLISECONDS);
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
//		new Thread("RebuildPvpInfo"){
//			@Override
//			public void run(){
//				synchronized (pvpInfos) {
//					rebuildPvpInfos();
//				}
//				rebuildWeekCredits();
//				rebuildLevelRanks();
//			}
//		}.start();
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
			Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(id);
			topWeekRanks[faction].add(actor);
			LogUtil.logWeekRankScore(actor);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void getTopLevelRanks(int count,int faction){
		List<Integer> l = Server.server.getServiceRegistry().getDbService().playerDAO.getTopLevelRanks(count,faction);
		topLevelRanks[faction] = new ArrayList<Actor>();
		for(int id:l){
			Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(id);
			topLevelRanks[faction].add(actor);
			LogUtil.logLevelRankScore(actor);
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
	
	public int isInArray(int[] arr,int a){
		if(arr != null && arr.length>0){
			for(int i=0;i<arr.length;i++){
				if(arr[i] == a)
					return i;
			}
		}
		return -1;
	}
	
	public void parse(Document doc) {
		Element root = doc.getRootElement();
		if (root != null) {
			List ach = root.elements("achievement");
			for (int i = 0; i < ach.size(); i++) {
				List<Achievement> achieve = new ArrayList<Achievement>();
				List<Integer> achType = new ArrayList<Integer>();
				int type = Integer.parseInt(((Element) ach.get(i))
						.attributeValue("type"));
				List achTypes = ((Element) ach.get(i)).elements("achievetype");
				for(int j=0;j<achTypes.size();j++){
					int achievementType = Integer.parseInt(((Element) achTypes.get(j))
							.attributeValue("id"));
					List achItems = ((Element) achTypes.get(j)).elements("item");
					List<Achievement> achie = new ArrayList<Achievement>();
					achType.add(achievementType);
					for(int k=0;k<achItems.size();k++){
						int achieveId = Integer.parseInt(((Element) achItems.get(k))
								.attributeValue("achieveid"));
				        String achievementName = ((Element) achItems.get(k))
						.attributeValue("name");
						String dec = ((Element) achItems.get(k))
								.attributeValue("dec");
						String param1 = ((Element) achItems.get(k))
						.attributeValue("param1");
						String param2 = ((Element) achItems.get(k))
						.attributeValue("param2");
						int point = Integer.parseInt(((Element) achItems.get(k))
								.attributeValue("point"));
						String items = ((Element) achItems.get(k))
						.attributeValue("reward");
						Achievement perAchieve = new Achievement(achieveId, type,achievementType,
								achievementName, dec,param1,param2, point);
						if(items != ""){
							String[] strs = items.split(",");
							for(int m=0;m<strs.length;m++){
							   perAchieve.addRewardItem(Integer.parseInt(strs[m]));
							}
						}
						achie.add(perAchieve);
						achieve.add(perAchieve);	
				    }
				    subAchievement.put(achievementType, achie);
				}
				achievement.put(type, achieve);
				achieveType.put(type, achType);
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
				if(type == 16){
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
				} else if(type == 17){
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
						countJewelOnEquipment(person,false);
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
					Map<Achievement,Byte> list = new HashMap<Achievement,Byte>();
					for(Achievement ach : achievements){
						getAccomplishAndFiniTime(ach,player);
						list.put(ach, ach.acomplish);
					}
					for (int i=0;i<achievements.size();i++) {
						Achievement ach = achievements.get(i);
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
						Byte b = list.get(ach);
						pt.put(b.byteValue());
						List<Integer> rewardItems = ach.getRewardItem();
						if(rewardItems != null && rewardItems.size()>0){
							int sz = rewardItems.size();
							if(person.pool.getString(getPropertyOfGetGift(ach.achievementName))!=""){
								sz = 0;
							}
							if(sz > 0){
								pt.putShort((rewardItems.size() >> 1));
								for(int i=0;i<rewardItems.size();i+=2){
									GameItem gameItem = ObjectAccessor.createGameItem(rewardItems.get(i));
									pt.putInt(rewardItems.get(i));
									pt.putInt(gameItem.template.showType);
									pt.putInt(rewardItems.get(i+1));
									pt.putString(gameItem.getDesc());
									pt.putString(gameItem.template.name);
								}
							} else {
								pt.putShort((short)0);
							}
						} else {
							pt.putShort((short)0);
						}
					}
				}
				player.send(pt);
			} else {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.PERSONAL_ACHIEVEMENT_DETAIL_CLIENT, "Người chơi không trên mạng");
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
			String[] catagoryNames = { "Lệ thường", "荣誉", "Cuộc sống" ,"Lịch trình trưởng thành","Xuyên kim đái ngân","Kinh thương hữu đạo","刺杀国君","Bá lạc chi tài","Phúc giáp thiên hạ","打造","装备","Tên gọi","卡片","击杀怪物","Nhiệm vụ","Khác","Thống kê kích sát","Thống kê bị truy sát"};
			Player person = (Player)ObjectAccessor.getPlayer(personId);
			if(person == null){
				person =  Server.server.getServiceRegistry().getFameService().getStatue(personId);
			}
			if(person == null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.PERSONAL_ACHIEVEMENT_CLIENT, "Người chơi đã rời mạng");
				return;
			}
			for (int i=0;i<achievement.size();i++) {
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
					countJewelOnEquipment(person,false);
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
			pt.putString(catagoryNames[16]);
			pt.putInt(2);
			pt.putString(catagoryNames[17]);
			pt.putInt(2);
			pt.putInt(count);
			pt.putInt(pointCount);
			player.send(pt);
			catagoryNames = null;
		}
	}
	
	/**
	 * 完成成就领取奖励
	 * @param p
	 * @param type
	 * @param achieveId
	 * @throws Exception
	 */
	public void getReward(Player p,int type,int achieveId) throws Exception{
		if(p!=null){
			List<Achievement> achs = achievement.get(type);
			if(achs!=null && achs.size()>0){
				Achievement ach = achs.get(achieveId);
				if(ach != null){
					if(ach.acomplish == 0){
						throw new Exception("Xin lỗi,bạn chưa hoàn thành thành tựu");
					}
					if(p.pool.getString(getPropertyOfGetGift(ach.achievementName))!=""){
						throw new Exception("Xin lỗi,bạn đã nhận qua phần thưởng này rồi.");
					}
					List<Integer> itemIds = ach.getRewardItem();
					if(itemIds != null && itemIds.size()>0){
						for(int i = 0;i<itemIds.size();i+=2){
							GameItem item = ObjectAccessor.createGameItem(itemIds.get(i));
					        int count = itemIds.get(i+1);
							PlayerTransaction tx = p.newTransaction("ACHIEVEMENTREWARD");
							try {
								p.bag.addGameItemComplete(item, count, tx, true);
								tx.commit();
							} catch (Exception e) {
								tx.rollback();
								String content = MessageFormat.format(
										"Chúc mừng bạn đã hoàn thành{0}thành tựu bạn nhận được{1}{2}phần thưởng.", ach.achievementName,count,
										item.template.name);
								Server.server.getServiceRegistry().getMailService()
								.sendSystemMail(p.id, "<cFF0000>[系统]</c>\n<cFF0000>[hệ thống]</c>", "Phần thưởng hoàn thành thành tựu", content, 0,
										item, count, "ACHIEVEMENTREWARD");
							}
							p.pool.setString(getPropertyOfGetGift(ach.achievementName), getFinishTime(System.currentTimeMillis()));
						}
					} else {
						throw new Exception("Xin lỗi，hoàn thành thành tựu này ko có phần thưởng.");
					}
				}
			}
		}
	}
	
	public Achievement getAchievement(List<Achievement> list,int subType){
		if(list!=null && list.size()>0){
			for(Achievement a : list){
				if(a.achieveId == subType){
					return a;
				}
			}
		}
		return null;
	}
	
	public List<Achievement> getAchievementList(int achieveTypeId){
		if(subAchievement!=null && subAchievement.size()>0){
			List<Achievement> list = subAchievement.get(achieveTypeId);
			if(list!=null && list.size()>0){
				return list;
			}
		}
		return null;
	}
	
	/**
	 * 完成成就发送消息
	 * @param p
	 * @param type 成就类型
	 * @param subType 成就子类型
	 * @param worldMessage 是否需要发世界聊
	 * @return
	 */
	public String setMessage(Player p,Achievement achieve,boolean worldMessage){
		if(p!=null){
			if(achieve != null){
				Server.server.getServiceRegistry().getChatService()
				.sendAreaSystemMessage(MessageFormat.format("Chúc mừng anh hùng <cff0000>{0}</c> đạt được thành tựu <cff0000>{1}</c>", p.name,achieve.achievementName),p.map.id);
				if(worldMessage){
					Server.server.getServiceRegistry().getChatService()
					.sendWorldMessage(MessageFormat.format("Chúc mừng anh hùng <cff0000>{0}</c> đạt được thành tựu <cff0000>{1}</c>", p.name,achieve.achievementName));
				}
			}
		}
		return "";
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
		List<Integer> list = achieveType.get(ach.type);
		if(list!=null && list.size()>0){
			for(int index=0;index<list.size();index++){
			    int achieveTypeId = list.get(index);
				List<Achievement> achs = subAchievement.get(achieveTypeId);
				if(achs!=null && achs.size()>0){
					switch(ach.type){
					case 0: //常规
						   switch(index){
						   case 0:
							      for(Achievement a : achs){
							    	  if(a.param2.equals("qixi")){
							    		  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_QUESTQIXI) == "") ? 0 : 1);
										  a.finiTime = po.getString(PROPERTY_FINISHTIME_QUESTQIXI);
							    	  } else if(a.param2.equals("midautom")){
							    		  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_QUESTMID) == "") ? 0 : 1);
										  a.finiTime = po.getString(PROPERTY_FINISHTIME_QUESTMID);
							    	  } else if(a.param2.equals("owe")){
							    		  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_QUESTOWE) == "") ? 0 : 1);
										  a.finiTime = po.getString(PROPERTY_FINISHTIME_QUESTOWE);
							    	  }
							      }
							      break;
						   }
						   break;
					case 1:
						   switch(index){
						   case 0: //荣誉类任务
								   for(Achievement a:achs){
									   if(a.param2.equals("zusha")){
							    		  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_ZHUSHA) == "") ? 0 : 1);
										  a.finiTime = po.getString(PROPERTY_FINISHTIME_ZHUSHA);
								       } else if(a.param2.equals("yuxi")){
							    		  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_YUXI) == "") ? 0 : 1);
										  a.finiTime = po.getString(PROPERTY_FINISHTIME_YUXI);
								       } else if(a.param2.equals("tianlong")){
							    		  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_QUESTTIANLONG) == "") ? 0 : 1);
										  a.finiTime = po.getString(PROPERTY_FINISHTIME_QUESTTIANLONG);
								       } else if(a.param1.equals("perfect")){
							    		  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_PERFECT) == "") ? 0 : 1);
										  a.finiTime = po.getString(PROPERTY_FINISHTIME_PERFECT);
								       }
								   }
							       break;
						   case 1: //杀死敌国玩家
							       for(Achievement a:achs){ 
							    	   int killCount = Integer.parseInt(a.param1);
							    	   if(killCount == 1){
							    		   a.acomplish = (byte) ((po.getString(PROPERTY_KILLENEMY_FIRSTTIME) == "") ? 0 : 1);
										   a.finiTime = po.getString(PROPERTY_KILLENEMY_FIRSTTIME);
							    	   } else if(killCount == 100){
							    		   a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_KILL) == "") ? 0 : 1);
										   a.finiTime = po.getString(PROPERTY_FINISHTIME_KILL);
							    	   } else if(killCount == 500){
							    		   a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_KILLMORE) == "") ? 0 : 1);
										   a.finiTime = po.getString(PROPERTY_FINISHTIME_KILLMORE);
							    	   }
							       }
							       break;
						   case 2:
							      Achievement ac = achs.get(0);//国家捐款
							      int collectNum = Integer.parseInt(ac.param1);
							      if(person.id>0 && po.getString(PROPERTY_FINISHTIME_COLLECT) == "" && po.getInt(
											PROPERTY_NATION_COLLECT, 0) >= collectNum){
										po.setString(PROPERTY_FINISHTIME_COLLECT,getFinishTime(System.currentTimeMillis()));
									}
							      ac.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_COLLECT) == "" ? 0 : 1);
							      ac.finiTime = po.getString(PROPERTY_FINISHTIME_COLLECT);
							      break;
						   case 3: //战争类
							      for(Achievement a : achs){ 
							    	  int battleType = Integer.parseInt(a.param1);
							    	  int victory = Integer.parseInt(a.param2);
							    	  a.acomplish = (byte)((po.getString(getBattleWinProperty(battleType,victory)) == "") ? 0 : 1);
									  a.finiTime = po.getString(getBattleWinProperty(battleType,victory));
							      }
							      break;
						   }
						   break;
					case 2:
						   switch(index){
						   case 0: //拥有的金钱
							      for(Achievement a : achs){
							    	  int moneyCount = Integer.parseInt(a.param1);
							    	  if(person.id>0 && person.money >= moneyCount){
								    	  if(moneyCount == 10000 && po.getString(PROPERTY_FINISHTIME_TENTHOU) == ""){
											po.setString(PROPERTY_FINISHTIME_TENTHOU,getFinishTime(System.currentTimeMillis()));
										  }else if(moneyCount == 1000000 && po.getString(PROPERTY_FINISHTIME_MILLIONARE) == ""){
												po.setString(PROPERTY_FINISHTIME_MILLIONARE,getFinishTime(System.currentTimeMillis()));
										  }
							          }
							    	  if(moneyCount == 10000){
							    		  a.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_TENTHOU) == "" ? 0 : 1);
										  a.finiTime=po.getString(PROPERTY_FINISHTIME_TENTHOU);
							    	  } else if(moneyCount == 1000000){
							    		  a.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_MILLIONARE) == "" ? 0 : 1);
										  a.finiTime=po.getString(PROPERTY_FINISHTIME_MILLIONARE);
							    	  }
							      }
							      break;
						   case 1://美食相关
							      for(Achievement a:achs){
							    	  if(!a.param1.equals("") && a.param2.equals("")){
							    		  int itemId = Integer.parseInt(a.param1);
							    		  if(itemId == 1183){
							    			a.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_USEYIHESU) == "" ? 0 : 1);
							  				a.finiTime = po.getString(PROPERTY_FINISHTIME_USEYIHESU);
							    		  }else if(itemId == 1945){
							    			a.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_USESIFANGCAI) == "" ? 0 : 1);
								  			a.finiTime = po.getString(PROPERTY_FINISHTIME_USESIFANGCAI);
							    		  }
							    	  }else if(!a.param2.equals("") && a.param1.equals("")){
							    		  int num = Integer.parseInt(a.param2);
							    		  a.acomplish = (byte) (po.getString(getPropertyOfUseFood(num)) == "" ? 0 : 1);
										  a.finiTime = po.getString(getPropertyOfUseFood(num));
							    	  }
							      }
							      break;
						   case 2://生活其它
								   for(Achievement a:achs){
									   if(a.param1.equals("marrage")){
										   if(person.id>0){
											   PlayerRelation rel = Server.server.getServiceRegistry()
												.getRelationService().get(person.id);
												if ((rel != null && rel.mateId != -1)
															&& po.getString(PROPERTY_FINISHTIME_MARRIAGE) == ""){
													po.setString(PROPERTY_FINISHTIME_MARRIAGE,getFinishTime(System.currentTimeMillis()));
													}
										   }
										   a.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_MARRIAGE) == "" ? 0 : 1);
										   a.finiTime = po.getString(PROPERTY_FINISHTIME_MARRIAGE);
									   }else if(a.param2.equals("beauty")){
										   a.acomplish = (byte) (po.getString(PROPERTY_BEAUTY_TOPTEN) == "" ? 0 : 1);
										   a.finiTime = po.getString(PROPERTY_BEAUTY_TOPTEN);
									   }
								   }
								   break;
						   }
						   break;   
					case 3:
						  switch(index){
						  case 0:
								  for(Achievement a : achs){
									  int level = (Integer.parseInt(a.param1));
									  if(person.id>0){
										  if(person.level >=level && po.getString(getPropertyOfLevel(level)) == ""){
												po.setString(getPropertyOfLevel(level), getFinishTime(System.currentTimeMillis()));
										  }
									  }
									  a.acomplish = (byte) ((po.getString(getPropertyOfLevel(level)) == "") ? 0 : 1);
									  a.finiTime = po.getString(getPropertyOfLevel(level));
								  } break;
						  case 1:
								  for(Achievement a : achs){
									  int count = (Integer.parseInt(a.param1));
									  a.acomplish = (byte) ((po.getString(getPropertyOfOnlineTime(count)) == "") ? 0 : 1);
									  a.finiTime = po.getString(getPropertyOfOnlineTime(count));
								  } break;
						   }
						   break;
					case 4:
						   switch(index){
						   case 0:
							     for(Achievement a:achs){
							    	 int level = Integer.parseInt(a.param2);
							    	 if(level == 7){
							    		 a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_SEV) == "") ? 0 : 1);
										 a.finiTime = po.getString(PROPERTY_FINISHTIME_SEV);
							    	 }else {
							    		 a.acomplish = (byte) ((po.getString(getPropertyByLevel(level)) == "") ? 0 : 1);
										 a.finiTime = po.getString(getPropertyByLevel(level));
							    	 }
							     }
							     break;
						   case 1:
							      for(Achievement a:achs){
							    	  int total = Integer.parseInt(a.param1);
							    	  if(a.param2.equals("")){
							    		  if(total == 30){
								    		  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_COUNT30) == "") ? 0 : 1);
								  			  a.finiTime = po.getString(PROPERTY_FINISHTIME_COUNT30);
							    		  }else if(total == 40){
							    			  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_COUNT40) == "") ? 0 : 1);
								  			  a.finiTime = po.getString(PROPERTY_FINISHTIME_COUNT40);
							    		  }else if(total == 50){
							    			  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_COUNT50) == "") ? 0 : 1);
								  			  a.finiTime = po.getString(PROPERTY_FINISHTIME_COUNT50);
							    		  }
							    	  }else {
							    		  int level = Integer.parseInt(a.param2);
							    		  if(level == 6){
							    			  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_6COUNT50) == "") ? 0 : 1);
							  				  a.finiTime = po.getString(PROPERTY_FINISHTIME_6COUNT50);
							    		  }else if(level == 7){
							    			  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_7COUNT50) == "") ? 0 : 1);
							  				  a.finiTime = po.getString(PROPERTY_FINISHTIME_7COUNT50);
							    		  }
							    	  }
							      }
							     break;
						   }
						   break;
					case 5:
						   switch(index){
						   case 0:
							      for(Achievement a:achs){
							    	  if(!a.param1.equals("")){
							    		  int count = Integer.parseInt(a.param1);
							    		  if(person.id>0 && count == 1){
							    			  if(po.getString(PROPERTY_FINISHTIME_AUCTION)!="" &&
							  					po.getString(getPropertyOfAuction(count,0,true))!=po.getString(PROPERTY_FINISHTIME_AUCTION)){
							  					po.setString(getPropertyOfAuction(count,0,true),po.getString(PROPERTY_FINISHTIME_AUCTION));
							  				  }
							    		  }
							    		  a.acomplish = (byte) ((po.getString(getPropertyOfAuction(count,0,true)) == "") ? 0 : 1);
										  a.finiTime = po.getString(getPropertyOfAuction(count,0,true));
							    	  }else{
							    		  int money = Integer.parseInt(a.param2);
							    		  a.acomplish = (byte) ((po.getString(getPropertyOfAuction(0,money,true)) == "") ? 0 : 1);
										  a.finiTime = po.getString(getPropertyOfAuction(0,money,true));
							    	  }
							      }
							      break;
						   case 1:
							      for(Achievement a :achs){
							    	  if(!a.param1.equals("")){
							    		  int count = Integer.parseInt(a.param1);
							    		  a.acomplish = (byte) ((po.getString(getPropertyOfAuction(count,0,false)) == "") ? 0 : 1);
										  a.finiTime = po.getString(getPropertyOfAuction(count,0,false));
							    	  }else {
							    		  int money = Integer.parseInt(a.param2);
							    		  a.acomplish = (byte) ((po.getString(getPropertyOfAuction(0,money,false)) == "") ? 0 : 1);
										  a.finiTime = po.getString(getPropertyOfAuction(0,money,false));
							    	  }
							      }
							      break;
						   }
						   break;
					case 6:
							int[] ind = {0,0,1,2};
							List<Integer> faList = getFaction(person.faction);
							if(ach.achieveId == ind[faList.get(0)]){
							    ach.acomplish = (byte) ((po.getString(getPropertyOfKillKing(1)) == "") ? 0 : 1);
								ach.finiTime = po.getString(getPropertyOfKillKing(1));
							}
							
							if(ach.achieveId == ind[faList.get(1)]){
							    ach.acomplish = (byte) ((po.getString(getPropertyOfKillKing(2)) == "") ? 0 : 1);
								ach.finiTime = po.getString(getPropertyOfKillKing(2));
							}
						   break;
					case 7:
						  switch(index){
						  case 0:
							     for(Achievement a:achs){
							    	 int level = Integer.parseInt(a.param1);
							    	 a.acomplish = (byte) ((po.getString(getPropertyOfHorseLevel(level)) == "") ? 0 : 1);
									 a.finiTime = po.getString(getPropertyOfHorseLevel(level));
							     }
							     break;
						  case 1:
							    for(Achievement a:achs){
							    	int count = Integer.parseInt(a.param1);
							    	a.acomplish = (byte) ((po.getString(getPropertyOfHorseEqu(count)) == "") ? 0 : 1);
									a.finiTime = po.getString(getPropertyOfHorseEqu(count));
							    }
							    break;
						  case 2:
							    for(Achievement a:achs){
							    	int count = Integer.parseInt(a.param1);
							    	if(person.id>0){
							    		int category = getHorseCategory(person);
							    		if(category>=count && po.getString(getHorseCountProperty(count))==""){
							    			po.setString(getHorseCountProperty(count),getFinishTime(System.currentTimeMillis()));
							    		}
							    	}
							    	a.acomplish = (byte)((po.getString(getHorseCountProperty(count)) == "") ? 0 : 1);
									a.finiTime = po.getString(getHorseCountProperty(count));
							    }
							    break;
						  }
						  break;
					case 8:
							switch(index){
							case 0:
								  for(Achievement a:achs){
									  int num = Integer.parseInt(a.param1);
									  a.acomplish = (byte) ((po.getString(getPropertyOfIMoney(num,true)) == "") ? 0 : 1);
									  a.finiTime = po.getString(getPropertyOfIMoney(num,true));
								  }
								   break;
							}
						    break;
						    
					case 9:
						   switch(index){
						   case 0:
							      int practice = person.pool.getInt(Player.PROPERTY_PRODUCE_ABILITY);
								  int practiceLevel = ProduceService.getPracticeLevel(person.level, practice);
							      for(Achievement a:achs){
							    	 int level = Integer.parseInt(a.param1);
							    	 if(person.id>0){
							    	    if(practiceLevel>=level && po.getString(getProduceProperty(level,-1,-1,-1)) == ""){
							    	    	po.setString(getProduceProperty(level,-1,-1,-1), getFinishTime(System.currentTimeMillis()));
							    	    }
							    	 }
							    	 a.acomplish = (byte)((po.getString(getProduceProperty(level,-1,-1,-1)) == "") ? 0 : 1);
									 a.finiTime = po.getString(getProduceProperty(level,-1,-1,-1));
							      }
							      break;
						   case 1:
							      for(Achievement a:achs){
							    	  if(a.param2.equals("")){
							    		  int oType = Integer.parseInt(a.param1);
							    		  a.acomplish = (byte)((po.getString(getProduceProperty(-1,-1,oType,-1)) == "") ? 0 : 1);
										  a.finiTime = po.getString(getProduceProperty(-1,-1,oType,-1));
							    	  }else{
							    		  int oType = Integer.parseInt(a.param1);
							    		  int forLevel = Integer.parseInt(a.param2);
							    		  a.acomplish = (byte)((po.getString(getProduceProperty(-1,forLevel,oType,-1)) == "") ? 0 : 1);
										  a.finiTime = po.getString(getProduceProperty(-1,forLevel,oType,-1));
							    	  }
							      }
							      break;
						   case 2:
							      for(Achievement a:achs){
							    	  int count = Integer.parseInt(a.param1);
							    	  a.acomplish = (byte)((po.getString(getProduceProperty(-1,-1,-1,count)) == "") ? 0 : 1);
									  a.finiTime = po.getString(getProduceProperty(-1,-1,-1,count));
							      }
							      break;
						   }
						   break;
					case 10:
						    processEquipCount(person,1,true,false);
						    switch(index){
						    case 0:
						    	   for(Achievement a:achs){
						    		  int quality = Integer.parseInt(a.param2);
						    		  a.acomplish = (byte)((po.getString(getEquipmentProperty1(quality,-1,-1)) == "") ? 0 : 1);
									  a.finiTime = po.getString(getEquipmentProperty1(quality,-1,-1));
						    	   }
						    	   break;
						    case 1:
						    	   for(Achievement a:achs){
						    		   int type = Integer.parseInt(a.param1);
						    		   if(type == 1){
						    			   a.acomplish = (byte)((po.getString(PROPERTY_EQUIP_ADDHOLE) == "") ? 0 : 1);
						   				   a.finiTime = po.getString(PROPERTY_EQUIP_ADDHOLE);
						    		   }else if(type == 2){
						    			   a.acomplish = (byte)((po.getString(PROPERTY_MERGE_JEWEL) == "") ? 0 : 1);
						   				   a.finiTime = po.getString(PROPERTY_MERGE_JEWEL);
						    		   }else if(type == 3){
						    			   a.acomplish = (byte)((po.getString(PROPERTY_EQUIP_ADDJEWEL) == "") ? 0 : 1);
						   				   a.finiTime = po.getString(PROPERTY_EQUIP_ADDJEWEL);
						    		   }else if(type == 4){
						    			   a.acomplish = (byte)((po.getString(PROPERTY_EQUIP_REMOVEJEWEL) == "") ? 0 : 1);
						   				   a.finiTime = po.getString(PROPERTY_EQUIP_REMOVEJEWEL);
						    		   }
						    	   }
						    	   break;
						    case 2:
						    	   for(Achievement a:achs){
						    		   if(!a.param1.equals("")){
						    		      int count = Integer.parseInt(a.param1);
						    		      a.acomplish = (byte)((po.getString(getEquipmentProperty1(-1,count,-1)) == "") ? 0 : 1);
						  				  a.finiTime = po.getString(getEquipmentProperty1(-1,count,-1));
						    		   } 
						    	   }
						    	   break;
						    case 3:
						    	   for(Achievement a:achs){
						    		   int t = Integer.parseInt(a.param1);
						    		   int level = Integer.parseInt(a.param2);
						    		   if(t == 0){
						    			   a.acomplish = (byte)((po.getString(getEquipmentProperty2(level,-1)) == "") ? 0 : 1);
						   				   a.finiTime = po.getString(getEquipmentProperty2(level,-1));
						    		   }else if(t==1){
						    			   a.acomplish = (byte)((po.getString(getEquipmentProperty2(-1,level)) == "") ? 0 : 1);
						   				   a.finiTime = po.getString(getEquipmentProperty2(-1,level));
						    		   }
						    	   }
						    	   break;
						    }
						    break;
					case 11:
						    switch(index){
						    case 0:
						    	   for(Achievement a:achs){
						    		   int count = Integer.parseInt(a.param1);
						    		   if(person.id > 0){
							   				int titleCount = person.titles.titles.size();
						   					if(titleCount >= count && po.getString(getTitleCountProperty(count)) == ""){
						   						po.setString(getTitleCountProperty(count), getFinishTime(System.currentTimeMillis()));
						   					}
						    		   }
						    		   a.acomplish = (byte)((po.getString(getTitleCountProperty(count)) == "") ? 0 : 1);
									   a.finiTime = po.getString(getTitleCountProperty(count));
						    	   }
						    	   break;
						    }
						    break;
					case 12:
						   switch(index){
						   case 0:
							      for(Achievement a:achs){
							    	  int count = Integer.parseInt(a.param1);
							    	  if(person.id>0){
											int cardCount = person.pool.getInt(CardService.PROPERTY_HAVECARD, 0);
											if(cardCount >= count && po.getString(getCardCollectProperty(count,-1))==""){
												po.setString(getCardCollectProperty(count,-1), getFinishTime(System.currentTimeMillis()));
											}
							    	  }
							    	  a.acomplish = (byte)((po.getString(getCardCollectProperty(count,-1)) == "") ? 0 : 1);
									  a.finiTime = po.getString(getCardCollectProperty(count,-1));
							      }
							      break;
						   case 1:
							      for(Achievement a:achs){
							    	  int groupId = Integer.parseInt(a.param1);
							    	  if(person.id>0){
											CardService cardService = Server.server.getServiceRegistry().getCardService();
											CardGroup group = cardService.getCardGroup(groupId);
											if(person.pool.getInt(cardService.getPropertyOfPlayerSuit(groupId), 0) == group.cards.size() && po.getString(getCardCollectProperty(-1,groupId)) == ""){
												po.setString(getCardCollectProperty(-1,groupId), getFinishTime(System.currentTimeMillis()));
											}
							    	   }
							    	   a.acomplish = (byte)((po.getString(getCardCollectProperty(-1,groupId)) == "") ? 0 : 1);
									   a.finiTime = po.getString(getCardCollectProperty(-1,groupId));
							      }
							      break;
						   }
						    break;
					case 13:
						    switch(index){
						    case 0:
						    	   for(Achievement a:achs){
						    		   int count = Integer.parseInt(a.param1);
						    		   a.acomplish = (byte)((po.getString(getKillCreatureProperty(count)) == "") ? 0 : 1);
									   a.finiTime = po.getString(getKillCreatureProperty(count));
						    	   }
						    	   break;
						    }
						    break;
					case 14:
						    switch(index){
						    case 0:
						    	   for(Achievement a:achs){
						    		   int type = Integer.parseInt(a.param1);
						    		   int count = Integer.parseInt(a.param2);
						    		   a.acomplish = (byte) ((po.getString(getPropertyOfFinishQuest(type,count)) == "") ? 0 : 1);
									   a.finiTime = po.getString(getPropertyOfFinishQuest(type,count));
						    	   }
						    	   break;
						    case 1:
						    	   for(Achievement a:achs){
						    		   int type = Integer.parseInt(a.param1);
						    		   a.acomplish = (byte) ((po.getString(getPropertyOfFinishQuest(type,1)) == "") ? 0 : 1);
									   a.finiTime = po.getString(getPropertyOfFinishQuest(type,1));
						    	   }
						    	   break;
						    }
						    break;
					case 15:
						    switch(index){
						    case 0:
						    	   for(Achievement a:achs){
						    		   int type = Integer.parseInt(a.param1);
						    		   if(type == 1){
						    			   if(person.id>0){
						    				   Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(person.faction);
							   				   if(nation.getKingId()!=-1 && nation.getKingId() == person.id && po.getString(PROPERTY_CANDIDATE_KING) == ""){
							   					  po.setString(PROPERTY_CANDIDATE_KING, getFinishTime(System.currentTimeMillis()));
							   				   }
						    			   }
						    			   a.acomplish = (byte) ((po.getString(PROPERTY_CANDIDATE_KING) == "") ? 0 : 1);
						   				   a.finiTime = po.getString(PROPERTY_CANDIDATE_KING);
						    		   }else if(type == 2){
						    			   a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_NATIONQUEST) == "") ? 0 : 1);
						   				   a.finiTime = po.getString(PROPERTY_FINISHTIME_NATIONQUEST);
						    		   }else if(type == 3){
						    			   a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_TONGQUEST) == "") ? 0 : 1);
						   				   a.finiTime = po.getString(PROPERTY_FINISHTIME_TONGQUEST);
						    		   }else if(type == 4){
						    			   a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_BIGBOX) == "") ? 0 : 1);
						   				   a.finiTime = po.getString(PROPERTY_FINISHTIME_BIGBOX);
						    		   }else if(type == 5){
						    			   a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_SMALLBOX) == "") ? 0 : 1);
						   				   a.finiTime = po.getString(PROPERTY_FINISHTIME_SMALLBOX);
						    		   }else if(type == 6){
						    			   a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_EXPANSIONBATTLE) == "") ? 0 : 1);
						   				   a.finiTime = po.getString(PROPERTY_FINISHTIME_EXPANSIONBATTLE);
						    		   }
						    	   }
						    	   break;
						    case 1:
						    	   for(Achievement a:achs){
						    		   int type = Integer.parseInt(a.param1);
						    		   int count = Integer.parseInt(a.param2);
						    		   if(type == 1){
						    			   a.acomplish = (byte) ((po.getString(getPropertyOfPkWin(count)) == "") ? 0 : 1);
						   				   a.finiTime = po.getString(getPropertyOfPkWin(count));
						    		   }
						    	   }
						    	   break;
						    }
						    break;
				    }  
					
				}
			}
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

