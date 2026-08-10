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
import com.pip.sanguo.data.quest.QuestRewardItem;
import com.pip.sanguo.data.quest.QuestRewardSet;
import peony.game.Actor;
import peony.game.ChatOption;
import peony.game.CommonUtil;
import peony.game.DayListener;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameItemObject;
import peony.game.GameObject;
import peony.game.GetConsumnCall;
import peony.game.Horse;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.PropertyPool;
import peony.game.Server;
import peony.game.Title;
import peony.game.TitleUtil;
import peony.game.Unit;
import peony.game.attendant.Attendant;
import peony.game.beautyparade.Beauty;
import peony.game.beautyparade.BeautyParadeService;
import peony.game.chat.ChatMessage;
import peony.game.itemenhance.ItemEnhance;
import peony.game.itemenhance.NaturalEnhance;
import peony.game.nation.NationBattleFieldInstance;
import peony.game.skill.Skill;
import peony.marriage.WeddingService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.cards.CardGroup;
import peony.service.cards.CardService;
import peony.service.duel.DuelService;
import peony.service.fame.Fame;
import peony.service.tong.TongMember;
import peony.service.tong.TongService;
import peony.util.TimeUtil;
import peony.vm.ASMQuest;
import peony.vm.ASMQuestUtil;

public class StatService implements Service,DayListener,ServiceEventListener{
	
	private static final Logger log = Logger.getLogger(StatService.class);
	
	public static int TOP_COUNT = 20;
	
	public Map<Integer,PvpInfo> pvpInfos = new HashMap<Integer,PvpInfo>(); 
	protected BlockingQueue<Object> kills = new LinkedBlockingQueue<Object>();
//	public Map<Integer, List<Achievement>> achievement = new HashMap<Integer, List<Achievement>>();
//	public Map<Integer,List<Achievement>> subAchievement = new HashMap<Integer,List<Achievement>>();
//	protected Map<Integer,List<Integer>> achieveType = new HashMap<Integer,List<Integer>>();
	Map<Integer,List<Integer>> type2AchiveId = new HashMap<Integer,List<Integer>>(); //key type,
	public static Map<Integer,Achievement> achieveId2Achieve = new HashMap<Integer,Achievement>();
    
	public static List<Integer> trackLogPlayer = new ArrayList<Integer>();

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
	public static String PROPERTY_CANDIDATE_KING_AGAIN = "candidatekingagain";//连任国公
	public static String PROPERTY_KILLCREATRUE_COUNT = "killcreaturecount";//杀死怪物数量
	public static String PROPERTY_MERGE_JEWEL = "mergejewel";//合成宝石
	public static String PROPERTY_EQUIP_ADDHOLE = "equipaddhole";//打孔
	public static String PROPERTY_EQUIP_ADDJEWEL = "equipaddjewel";//镶嵌宝石
	public static String PROPERTY_EQUIP_REMOVEJEWEL ="equipremovejewel";//摘除宝石
	public static String PROPERTY_ONLINE_TIME = "onlinetimeachieve"; //玩家累积在线时间
	public static String PROPERTY_FINISHTIME_NATIONQUEST="opennationquest";//国公发布一次国家任务
	public static String PROPERTY_FINISHTIME_TONGQUEST="opentongquest";//发布一次军团任务
	public static String PROPERTY_FINISHTIME_CREATETONG="createtong";//创建一个军团
	public static String PROPERTY_PKWIN_COUNT = "pkwincount";//切磋胜利次数
	public static String PROPERTY_FINISHTIME_BIGBOX = "getbigbox";//在西域获得大宝箱
	public static String PROPERTY_FINISHTIME_SMALLBOX = "getsmallbox";//在西域获得小宝箱
	public static String PROPERTY_FINISHTIME_EXPANSIONBATTLE = "expansionbattlewin";//获得司隶战役胜利
	public static String PROPERTY_FINISHTIME_USEYIHESU = "useyihesu";//品尝一盒酥
	public static String PROPERTY_FINISHTIME_USESIFANGCAI = "usesifangcai";//品尝貂蝉私房菜
	public static String PROPERTY_USECOUNT_FOOD = "usefoodcount";//品尝美食的数值
	public static String PROPERTY_FINISHTIME_APPRENTICE_GRADUATE = "apprenticegraduate";//玩家徒弟出师
	public static String PROPERTY_FINISHTIME_ENAIDU = "fuqienaidu";//恩爱度
	public static String PROPERTY_FINISHTIME_HORSEBOOK500 = "horsebook500";//使用坐骑遗忘书
	public static String PROPERTY_DONE_CHRISMAS = "donechrismasquest";//已完成圣诞任务
	public static String PROPERTY_FINISHTIME_CHRISMAS = "chrismasquest";//完成圣诞任务
	public static String PROPERTY_FINISHTIME_KILLDONGZHUO = "killdongzhuo";//击杀董卓
	public static String PROPERTY_IDS_KILLWORLDBOSS = "worldbossids";//已击杀世界boss的id
	public static String PROPERTY_FINISHTIME_KILLALLWORLDBOSS = "killallworldboss";//击杀所有世界boss
	public static String PROPERTY_FINISHTIME_ALLFUNINSTANCE = "allfuninstance";//通关所有趣味副本
	public static String PROPERTY_DONE_CYCLEQUEST = "cyclequest";//已完成的跑环任务
	public static String PROPERTY_FINISHTIME_ALLCYCLEQUEST = "allcyclequest";//完成所有跑环任务
	public static String PROPERTY_FINISHTIME_NATIONBATTLEINTEN = "nationbattleinten";//十分钟之内取得国战胜利
	public static String PROPERTY_FINISHTIME_KILLENMEMYONENBATTLE = "killenemyonenbattle";//单场国战中击杀20敌人
	public static String PROPERTY_KILLENEMY_NATIONBATTLE = "killcountnbattle";//国战中击杀敌人数
	public static String PROPERTY_FINISHTIME_KILLENMEMYNBATTLE = "killenemynbattle";//国战中击杀521敌人
	public static String PROPERTY_FINISHTIME_KILL4BOSSNBATTLE = "kille4bossnbattle";//国战中击杀521敌人
	public static String PROPERTY_FINISHTIME_TIANXIADIYITITLE = "tianxiadiyititle";//拥有天下第一称号
	public static String PROPERTY_FINISHTIME_PRODUCEDONGZHUOLING = "producedongzhuoling";//成功打造董卓令
	public static String PROPERTY_FINISHTIME_STUDYFORMULARBOOK = "studyformularbook";//学习100本配方书
	public static String PROPERTY_FINISHTIME_QICAIGUANGMANG = "qicaiguangmang";//身上拥有7彩光效
	public static String PROPERTY_FINISHTIME_BAQIWAILOU = "baqiwailou";//拥有85颗七级宝石（人物+坐骑)
	public static String PROPERTY_GETCREDIT_DAYQUEST = "getcreditdayquest";//完成每日任务获得的战功
	public static String PROPERTY_FINISHTIME_LOCKHORSESKILL = "lockhorseskill";//成功锁定一个坐骑技能
	public static String PROPERTY_GET_ATTENDANT = "getattendantachieve";//获得随从
	public static String PROPERTY_GET_ACHIEVEREWARD = "achrewards";//已领取的成就奖励
	
	
	public static final int NORMAL_QUEST_ACHIEVETYPE = 0; //常规任务类型
	public static final int FAME_QUEST_ACHIEVETYPE = 1; //荣誉任务类型
	public static final int KILL_ENEMY_ACHIEVETYPE = 2; //杀死敌国玩家类型
	public static final int NATION_COLLECT_ACHIEVETYPE = 3; //国家捐款类型
	public static final int KILL_BOSS_ACHIEVETYPE = 4; //国家捐款类型
	public static final int BATTLE_WIN_ACHIEVETYPE = 5; //战争类型
	public static final int BATTLE_REFER_ACHIEVETYPE = 6; //战争相关类型
	public static final int MONEY_UP_ACHIEVETYPE = 7; //金钱类型
	public static final int FOOD_CONSUME_ACHIEVETYPE = 8; //美食类型
	public static final int LIFE_LEFT_ACHIEVETYPE = 9; //生活其它类型
	public static final int PLAYER_LEVELUP_ACHIEVETYPE = 10;//成长历程类型
	public static final int ONLINE_TIME_ACHIEVETYPE = 11; //累积在线类型
	public static final int WARE_JEWEL_ACHIEVETYPE = 12; //穿戴宝石类型
	public static final int WARE_JEWELCOUNT_ACHIEVETYPE = 13; //穿戴宝石类型
	public static final int AUCTION_SELL_ACHIEVETYPE = 14; //拍卖类型
	public static final int AUCTION_BUY_ACHIEVETYPE = 15; //拍买类型
	public static final int KILL_KINT_ACHIEVETYPE = 16; //拍卖类型
	public static final int HORSE_LEVELUP_ACHIEVETYPE = 17; //坐骑升级类型
	public static final int HORSE_EQUCOUNT_ACHIEVETYPE = 18; //拥有马装数量类型
	public static final int HORSE_CATOCOUNT_ACHIEVETYPE = 19; //拥有坐骑种类类型
	public static final int HORSE_OTHER_ACHIEVETYPE = 20; //坐骑其它类型
	public static final int IMONEY_CONSUME_ACHIEVETYPE = 21; //消费I币类型
	public static final int PRODUCE_PRACTICE_ACHIEVETYPE = 22; //打造熟练度类型
	public static final int PRODUCE_EQUIPMENT_ACHIEVETYPE = 23; //打造马装或装备类型
	public static final int PRODUCE_NUMBER_ACHIEVETYPE = 24; //打造数量类型
	public static final int PRODUCE_OTHER_ACHIEVETYPE = 25; //打造其它类型
	public static final int EQUIPMENT_QUALITY_ACHIEVETYPE = 26; //拥有装备质量类型
	public static final int EQUIPMENT_ADDHOLE_ACHIEVETYPE = 27; //装备打孔镶嵌类型
	public static final int EQUIPMENT_ENHANCE_ACHIEVETYPE = 28; //五孔数量类型
	public static final int EQUIPMENT_RESULT_ACHIEVETYPE = 29; //装备效果类型
	public static final int TITLE_COUNT_ACHIEVETYPE = 30; //称号种类类型
	public static final int CARD_COLLECT_ACHIEVETYPE = 31; //卡片数量类型
	public static final int CARD_GROUP_ACHIEVETYPE = 32; //卡片数量类型
	public static final int KILL_CREATURE_ACHIEVETYPE = 33; //击杀怪物类型
	public static final int QUEST_REPEAT_ACHIEVETYPE = 34; //任务重复类型
	public static final int QUEST_ONCE_ACHIEVETYPE = 35; //一次任务类型
	public static final int HORSE_MERGE_ACHIEVETYPE = 36;//坐骑合成
	public static final int HORSE_CHANGE_ACHIEVETYPE = 37;//坐骑幻化
	public static final int OTHERTYPE_ONCE_ACHIEVETYPE= 38; //其它散类型
	public static final int OTHERTYPE_REPEAT_ACHIEVETYPE = 39; //其它重复类型
	public static final int FINISH_ACHIEVE_POINT = 40;//完成成就点数
	
	
	
	

	public boolean runStat = true;
	
//	public static String[] catagoryNames = { peony.Messages.STRING_00828, peony.Messages.STRING_00829, peony.Messages.STRING_00830 ,peony.Messages.STRING_00831,peony.Messages.STRING_00832,peony.Messages.STRING_00833,peony.Messages.STRING_00834,peony.Messages.STRING_00835,peony.Messages.STRING_00836,peony.Messages.STRING_00837,peony.Messages.STRING_00838,peony.Messages.STRING_00839,peony.Messages.STRING_00840,peony.Messages.STRING_00841,peony.Messages.STRING_00842,"坐骑合成","坐骑幻化",peony.Messages.STRING_00843,peony.Messages.STRING_00844,peony.Messages.STRING_00845};
	
	public static String[] catagoryNames = { "战斗", "装备宝石", "财富" ,"坐骑随从","任务","事件","综合","打造",peony.Messages.STRING_00844,peony.Messages.STRING_00845};
	
	
	public int[] questIds = {2824,2825,2826,1690,1699,1700,1751,161,382,1652,2029,2030,2031}; //被监听的任务ID
	
	public String[] questProperty = {
			PROPERTY_QUEST_77,PROPERTY_QUEST_77,PROPERTY_QUEST_77,PROPERTY_QUEST_MIDAUTUMN,
			PROPERTY_QUEST_OWE,PROPERTY_QUEST_OWE,PROPERTY_QUEST_OWE,
			PROPERTY_QUEST_ZHUSHASHOUE,PROPERTY_QUEST_YUXI,PROPERTY_QUEST_TIANLONG,PROPERTY_QUEST_TIANLONG,PROPERTY_QUEST_TIANLONG,PROPERTY_QUEST_TIANLONG
			}; //对应于被监听任务
	public String[] timeProperty = {
			PROPERTY_FINISHTIME_QUESTQIXI,PROPERTY_FINISHTIME_QUESTQIXI,PROPERTY_FINISHTIME_QUESTQIXI,
			PROPERTY_FINISHTIME_QUESTMID,PROPERTY_FINISHTIME_QUESTOWE,PROPERTY_FINISHTIME_QUESTOWE,PROPERTY_FINISHTIME_QUESTOWE,
			PROPERTY_FINISHTIME_ZHUSHA,PROPERTY_FINISHTIME_YUXI,PROPERTY_FINISHTIME_QUESTTIANLONG,PROPERTY_FINISHTIME_QUESTTIANLONG,PROPERTY_FINISHTIME_QUESTTIANLONG,PROPERTY_FINISHTIME_QUESTTIANLONG
	        };//对应于被监听任务的完成时间
	
	public String[] questName = {peony.Messages.STRING_00822,peony.Messages.STRING_00822,peony.Messages.STRING_00822,peony.Messages.STRING_00823,peony.Messages.STRING_00824,peony.Messages.STRING_00824,peony.Messages.STRING_00824,peony.Messages.STRING_00825,peony.Messages.STRING_00826,peony.Messages.STRING_00827,peony.Messages.STRING_00827,peony.Messages.STRING_00827,peony.Messages.STRING_00827};
	
	public int[] clazzs = {0,1,2,3}; // 代表四种职业
	
	public int[] faction = {GameObject.FACTION_WEI,GameObject.FACTION_SHU,GameObject.FACTION_WU};
	
	public int[] factionIndex = {138,139,140};

	public int[] kkgIds = {1503,1504,1502};//暗杀国君的三个国家的任务id,按魏，蜀，吴排列
	
	public int[] questType = {0,1,2,3,4};//分别为以下五种任务
	//任务类成就所需要监听的任务id
	public int[] ydzQuestIds = {901,902,903,904,905,906,907,908,909,910,945,946,947};  //衣代诏任务
	public int[] dhmlQuestIds = {567,568,569,570,571,572,573,574,575,576,577,578,579,580,581}; //大汉密令任务
	
	public int[] nationQuestIds = {1489,1454,1490,1453,1491,1452}; //国战任务
	public int[] battleQuestIds = {632,633,634}; //战场任务
	public int[] tongbattleQuestIds = {1176,1177,1178}; //团战任务
	
	public static int[] foodIds = {1945,632,633,634,635,641,642,646,647,648,649,664};  //玩家品尝美食ID
	
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
//				ServiceEvent.EVENT_PLAYER_CHANGE_FACTION,
				ServiceEvent.EVENT_PLAYER_MONEY_UP,
				ServiceEvent.EVENT_PLAYER_MARRIAGE,
				ServiceEvent.EVENT_NATIONCOLLECT,
//				ServiceEvent.EVENT_RANK_UP,
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
		        ServiceEvent.EVENT_CYCLEINSTANCE_FINISH,
		};
	}

	public void handleEvent(ServiceEvent event) {
		if(Server.isStepServer){
			return;
		}
		if(event.type == ServiceEvent.EVENT_UNIT_DIE){
			Unit u1=(Unit) event.param1;
			Unit u2=(Unit) event.param2;
			if(u1 == null || u2 == null){
				return;
			}
			if(u1.type!=GameObject.TYPE_CREATURE&&u1.type!=GameObject.TYPE_PLAYER){
				return;
			}
			if(u2.type == GameObject.TYPE_PLAYER){
				Player p2=(Player)u2;
				if(p2.battleType == Player.TYPE_ASYNC_PLAYER || p2.battleIngoPlayer){
					return;
				}
			}
			if(u2.type == GameObject.TYPE_ATTENDANT){
				Player p2=((Attendant)u2).owner;
				if(p2.battleType == Player.TYPE_ASYNC_PLAYER || p2.battleIngoPlayer){
					return;
				}
			}
			if(u1.type==GameObject.TYPE_PLAYER){
				Player p1=(Player)u1;
				if(p1.isInStep || p1.battleType == Player.TYPE_ASYNC_PLAYER || p1.battleIngoPlayer){
					return;
				}
			}
		}
		kills.add(event);
	}
	
//	protected void playerRankUp(Player p, int rank){
//		
//	}
	
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
					if(pvpInfo.pool.getString(getPropertyOfKillKing(index)).equals("")){
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
			if(finishCount >= 300 && pvpInfo.pool.getString(StatService.PROPERTY_FINISHTIME_PERFECT).equals("")){
				pvpInfo.pool.setString(StatService.PROPERTY_FINISHTIME_PERFECT,getFinishTime(System.currentTimeMillis()));
			}
			
			//处理军团
			TongService tongService = Server.server.getServiceRegistry().getTongService();
			TongMember tm = tongService.getPlayerInfo(p.id);
			if(tm!=null){
				if(tm.duty == TongService.CHAIRMAN && pvpInfo.pool.getString(PROPERTY_FINISHTIME_TONGQUEST).equals("")){
					pvpInfo.pool.setString(PROPERTY_FINISHTIME_TONGQUEST, getFinishTime(System.currentTimeMillis()));
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
			//为国为民成就
//			List<Achievement> list = getAchievementList(NATION_COLLECT_ACHIEVETYPE);
//			if(list!=null){
//				for(Achievement a:list){
			Achievement a = getAchievementById(74);
			if(a!=null){
				if(a.param2.equals("collect") && oldCollect + money >= Integer.parseInt(a.param1) && pvpInfo.pool.getString(PROPERTY_FINISHTIME_COLLECT).equals("")){
					pvpInfo.pool.setString(PROPERTY_FINISHTIME_COLLECT, getFinishTime(System.currentTimeMillis()));
					setMessage(p,a,true,true);
				}
			}
		}
	}
	
	protected void playerMarriaged(Player p, Player mate){
		if(p!=null){
			PvpInfo pvpInfo1 = getPvpInfo(p.id, p.faction);
			PvpInfo pvpInfo2 = getPvpInfo(mate.id, mate.faction);
			Achievement a = getAchievementById(142);
			if(a!=null){
				if(a.param1.equals("marrage")){
					//洞房花烛成就
					if(pvpInfo1.pool.getString(PROPERTY_FINISHTIME_MARRIAGE).equals("")){
						pvpInfo1.pool.setString(PROPERTY_FINISHTIME_MARRIAGE, getFinishTime(System.currentTimeMillis()));
						setMessage(p,a,true,true);
					}
					if(pvpInfo2.pool.getString(PROPERTY_FINISHTIME_MARRIAGE).equals("")){
					    pvpInfo2.pool.setString(PROPERTY_FINISHTIME_MARRIAGE, getFinishTime(System.currentTimeMillis()));
					    setMessage(mate,a,true,true);
				   }
				}
			}
		}
	}
	
	protected void playerMoneyUp(Player p, int oldMoney, int value){
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			for(int i=53;i<55;i++){
			   Achievement a = getAchievementById(i);
			   if(a!=null){
					int money = Integer.parseInt(a.param1);
					if(money == 10000 && (oldMoney + value) >= money && pvpInfo.pool.getString(PROPERTY_FINISHTIME_TENTHOU) == "") {//万元户成就
						pvpInfo.pool.setString(PROPERTY_FINISHTIME_TENTHOU, getFinishTime(System.currentTimeMillis()));
						setMessage(p,a,false,true);
					} else if(money == 1000000 && (oldMoney + value) >= money && pvpInfo.pool.getString(PROPERTY_FINISHTIME_MILLIONARE) == "") {//百万富翁成就
						pvpInfo.pool.setString(PROPERTY_FINISHTIME_MILLIONARE, getFinishTime(System.currentTimeMillis()));
						setMessage(p,a,false,true);
				    }
			    }
			}
		}
	}
	
//	protected void playerChangeFaction(Player player){
//		
//	}
	
	
	protected void unitDie(Unit u1,Unit u3){
		Player u2=null;
		if(u3.type==GameObject.TYPE_ATTENDANT){
			Attendant att=(Attendant)u3;
			u2=att.owner;
		}else if(u3.type==GameObject.TYPE_PLAYER){
			u2=(Player)u3;
		}else{
			return;
		}
		if(u1.type==GameObject.TYPE_PLAYER&&(u2.type==GameObject.TYPE_PLAYER||u2.type==GameObject.TYPE_ATTENDANT)){
			if(u1.faction!=u2.faction){
				addPvpInfo(u1.id, u2.id,u1.faction,u2.faction,u2.clazz,u1.clazz,u2.level,u1.level);
				if(u1.map.map.instance!=null && u2.map.map.instance!=null && u1.map.map.instance instanceof NationBattleFieldInstance && u2.map.map.instance instanceof NationBattleFieldInstance) {
					PvpInfo pvpInfo = getPvpInfo(u2.id,u2.faction);
					Player p = (Player)u2;
					for(int i=16;i<19;i++){
						Achievement a = getAchievementById(i);
						if(a!=null){
							if(a.param1.equals("huguoyougong") && pvpInfo.pool.getString(PROPERTY_FINISHTIME_KILLENMEMYONENBATTLE).equals("")){
								int totleCount = Integer.parseInt(a.param2);
								int count = p.pool.getInt(Player.PROPERTY_KILLENEMY_ONENBATTLE, 0);
								count++;
								p.pool.setInt(Player.PROPERTY_KILLENEMY_ONENBATTLE, count);
								if(count>=totleCount){
									pvpInfo.pool.setString(PROPERTY_FINISHTIME_KILLENMEMYONENBATTLE, getFinishTime(System.currentTimeMillis()));
									setMessage(p,a,false,true);
								}
							} else if(a.param1.equals("qijinqichu") && pvpInfo.pool.getString(PROPERTY_FINISHTIME_KILLENMEMYNBATTLE).equals("")){
								int totalCount = Integer.parseInt(a.param2);
								int count = pvpInfo.pool.getInt(PROPERTY_KILLENEMY_NATIONBATTLE, 0);
								count++;
								pvpInfo.pool.setInt(PROPERTY_KILLENEMY_NATIONBATTLE, count);
								if(count>=totalCount){
									pvpInfo.pool.setString(PROPERTY_FINISHTIME_KILLENMEMYNBATTLE, getFinishTime(System.currentTimeMillis()));
									setMessage(p,a,false,true);
								}
							}
						}
					}
				}
			}
		}
		

		//统计击杀怪物数量
		if(u1.type == GameObject.TYPE_CREATURE && (u3 instanceof Player||u3 instanceof Attendant)){
			Player p = null;
			if(u3 instanceof Player){
				p=(Player)u3;
			}else if(u3 instanceof Attendant){
				Attendant att=(Attendant)u3;
				p=att.owner;
			}else{
				return;
			}
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
					//击杀董卓
			Achievement a = getAchievementById(125);
			if(a!=null){
				if(a.param1.equals("dongzhuo") && pvpInfo.pool.getString(PROPERTY_FINISHTIME_KILLDONGZHUO).equals("")){
					String[] bossIds = a.param2.split(",");
					if(bossIds != null && isInArray2(bossIds,u1.id)!=-1){
					   pvpInfo.pool.setString(PROPERTY_FINISHTIME_KILLDONGZHUO, getFinishTime(System.currentTimeMillis()));
					   setMessage(p,a,false,true);
					}
				}
			}
			Achievement a2 = getAchievementById(135);
			if(a2!=null){
				if(a2.param1.equals("worldboss")){//击杀所有世界boss
					if(p.party!=null&&p.party.members!=null&&p.party.members.size()>0){
						for(int i=0;i<p.party.members.size();i++){
							Player pTemp=p.party.members.get(i).player;
							if(pTemp.inRange(p, 320)){
								 PvpInfo pvpInfo1 = getPvpInfo(pTemp.id,pTemp.faction);
								doneTask(pTemp,u1.id,pvpInfo1,a2,PROPERTY_IDS_KILLWORLDBOSS,PROPERTY_FINISHTIME_KILLALLWORLDBOSS);
							}
						}
					}else{
						doneTask(p,u1.id,pvpInfo,a2,PROPERTY_IDS_KILLWORLDBOSS,PROPERTY_FINISHTIME_KILLALLWORLDBOSS);
					}
			    }
			}
			
			//击杀怪物
			int killCount = p.pool.getInt(PROPERTY_KILLCREATRUE_COUNT, 0);
			killCount ++;
			p.pool.setInt(PROPERTY_KILLCREATRUE_COUNT, killCount);
			for(int i=1;i<5;i++){
				Achievement achievement = getAchievementById(i);
				if(achievement!=null){
					int count = Integer.parseInt(achievement.param1);
					if(killCount>=count && pvpInfo.pool.getString(getKillCreatureProperty(count)).equals("")){
					   pvpInfo.pool.setString(getKillCreatureProperty(count), getFinishTime(System.currentTimeMillis()));
					   setMessage(p,achievement,false,true);
					}
				}
			}
//			List<Achievement> listKillCreature = getAchievementList(KILL_CREATURE_ACHIEVETYPE);
//			if(listKillCreature!=null){
//				int maxCount = Integer.parseInt(listKillCreature.get(listKillCreature.size()-1).param1);
//				if(pvpInfo.pool.getString(getKillCreatureProperty(maxCount)).equals("")){
//					for(Achievement a : listKillCreature){
//				        int count = Integer.parseInt(a.param1);
//						if(killCount>=count && pvpInfo.pool.getString(getKillCreatureProperty(count)).equals("")){
//						   pvpInfo.pool.setString(getKillCreatureProperty(count), getFinishTime(System.currentTimeMillis()));
//						   setMessage(p,a,false,true);
//						}
//					}
//				}
//			}
		}
	}
	
	public String getNationBattleBoss(int faction){
		if(faction == 1){
			return "4390923,4390922,4390924,4390925";
		} else if(faction == 2){
			return "4456449,4456458,4456459,4456460";
		} else if(faction == 3){
			return "4521997,4521994,4521995,4521996";
		}
		return "";
	}
	

	/** 统计玩家创建一个军团成就 */
	public void createTong(Player p){
		if(p!=null){
			try{
				PvpInfo pvpInfo = getPvpInfo(p.id, p.faction);
				Achievement a = getAchievementById(141);
				if(a!=null){
					int type = Integer.parseInt(a.param1);
					if(type == 8){
						if(pvpInfo.pool.getString(PROPERTY_FINISHTIME_CREATETONG).equals("")){
							 pvpInfo.pool.setString(PROPERTY_FINISHTIME_CREATETONG, getFinishTime(System.currentTimeMillis()));
							 setMessage(p,a,false,true);
						}
					}
				}
			}catch(Exception e){
				
			}
		}
	}
	
	/** 统计玩家徒弟出师成就 */
	public void apprenticeGraduate(Player p){
		if(p!=null){
			try{
				PvpInfo pvpInfo = getPvpInfo(p.id, p.faction);
				Achievement a = getAchievementById(143);
				if(a!=null){
					String type = a.param1;
					if(type.equals("graduate")){
						if(pvpInfo.pool.getString(PROPERTY_FINISHTIME_APPRENTICE_GRADUATE).equals("")){
							 pvpInfo.pool.setString(PROPERTY_FINISHTIME_APPRENTICE_GRADUATE, getFinishTime(System.currentTimeMillis()));
							   setMessage(p,a,false,true);
						}
					}
				}
			}catch(Exception e){
				
			}
		}
	}
	
	/** 统计完成成就点数的成就 */
	public int countAchievePoint(Player p){
		int pointCount = 0;
		for (int i:type2AchiveId.keySet()) {
			List<Integer> list = type2AchiveId.get(i);
//			if(i == 6){
//				List<Achievement> achs = new ArrayList<Achievement>();
//				for(int j=0;j<faction.length;j++){
//					if(p.faction!=faction[j]){
//						achs.add(list.get(j));
//					}
//				}
//				list = new ArrayList<Achievement>(achs);
//			}
			for (Integer achId : list) {
				Achievement ach = achieveId2Achieve.get(achId);
				ach.acomplish = (byte)(getTime(p,ach).equals("")?0:1);
				if (ach.acomplish == 1){
				    pointCount += ach.point;
				}
			}
		}
		return pointCount;
	}
	
	public String getTime(Player p,Achievement achievement){
		String finishTime = "";
		PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
		if(achievement!=null){
			String property = achievement.property;
			if(!property.equals("")){
				if(p.id<0){
					Fame fame = Server.server.getServiceRegistry().getFameService().getFame(p.id);
				    if(fame != null){
				    	finishTime = fame.pool.getString(property);
				    }else{
				    	finishTime = pvpInfo.pool.getString(property);
				    }
				}else
				    finishTime = pvpInfo.pool.getString(property);
			}
		}
		return finishTime;
	}
	
	/** 统计完成成就点数*/
	public void finishAchievePoint(Player p,int cnt){
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(p.id, p.faction);
			for(int i=130;i<132;i++){
				Achievement a =getAchievementById(i);
				if(a!=null){
					int count = Integer.parseInt(a.param1);
					if(cnt>=count){
						if(pvpInfo.pool.getString(getPropertyOfFinishAchieve(count)).equals("")){
							 pvpInfo.pool.setString(getPropertyOfFinishAchieve(count), getFinishTime(System.currentTimeMillis()));
							 setMessage(p,a,false,true);
						}
					}
				}
			}
		}
	}
	
	/** 统计恩爱度的成就 */
	public void enaiduAchieve(Player p){
		if(p!=null){
			try{
				PvpInfo pvpInfo = getPvpInfo(p.id, p.faction);
				int value = p.pool.getInt(WeddingService.PROPERTY_ENAIDU,0);
				Achievement a = getAchievementById(144);
				if(a!=null){
					String param1 = a.param1;
					if(param1.equals("marragedegree")){
						int count = Integer.parseInt(a.param2);
						if(value >= count && pvpInfo.pool.getString(PROPERTY_FINISHTIME_ENAIDU).equals("")){
							 pvpInfo.pool.setString(PROPERTY_FINISHTIME_ENAIDU, getFinishTime(System.currentTimeMillis()));
							   setMessage(p,a,false,true);
						}
					}
				}
			}catch(Exception e){
				
			}
		}
	}
	
	/** 统计卡片附魔 */
//	public void addCardAchieve(Player p,Card c){
//		if(p!=null){
//			try{
//				PvpInfo pvpInfo = getPvpInfo(p.id, p.faction);
//				List<Achievement> list = getAchievementList(CARD_GROUP_ACHIEVETYPE);
//				if(list != null){
//					for(Achievement a : list){
//						if(a.param1.equals("")){
//							int t = Integer.parseInt(a.param2);
//							CardService service = Server.server.getServiceRegistry().getCardService();
//							String qualityPool = service.getPropertyOfCardQuality(c.id);
//						    int qulity = p.pool.getInt(qualityPool, 0);
//							if(t == 0 || (t==1&&qulity==1) || (t==2 && c.star == 4)){
//								if(pvpInfo.pool.getString(getPropertyOfAddCard(t)).equals("")){
//									 pvpInfo.pool.setString(getPropertyOfAddCard(t), getFinishTime(System.currentTimeMillis()));
//									 setMessage(p,a,false,true);
//								}
//							} 
//						}
//					}
//				}
//			}catch(Exception e){
//				
//			}
//		}
//	}
	
	/** 统计拥有随从成就*/
	public void playerAddAttendant(Player p){
		if(p!=null){
			try{
				PvpInfo pvpInfo = getPvpInfo(p.id, p.faction);
				int cnt = pvpInfo.pool.getInt(PROPERTY_GET_ATTENDANT,0);
				cnt++;
				pvpInfo.pool.setInt(PROPERTY_GET_ATTENDANT,cnt);
				for(int i=99;i<102;i++){
					Achievement a = getAchievementById(i);
					if(a!=null){
						if(a.param1.equals("attendant") && !a.param2.equals("")){
							int count = Integer.parseInt(a.param2);
							int totalAtt = p.attendantBag.attendants.size();
							if(cnt<totalAtt){
								cnt = totalAtt;
								pvpInfo.pool.setInt(PROPERTY_GET_ATTENDANT,cnt);
							}
							if(cnt>=count && pvpInfo.pool.getString(getPropertyOfGetAttendant(count)).equals("")){
								pvpInfo.pool.setString(getPropertyOfGetAttendant(count),getFinishTime(System.currentTimeMillis()));
								setMessage(p,a,false,true);
							}
						}
					}
				}
			}catch(Exception e){
				
			}
		} 
	}
	
	/** 玩家成功锁定一个坐骑经验 */
	public void playerLockHorseSkill(Player p){
		if(p!=null){
			try{
				PvpInfo pvpInfo = getPvpInfo(p.id, p.faction);
				Achievement a = getAchievementById(96);
				if(a!=null){
					if(a.param1.equals("locksill") && pvpInfo.pool.getString(PROPERTY_FINISHTIME_LOCKHORSESKILL).equals("")){
						pvpInfo.pool.setString(PROPERTY_FINISHTIME_LOCKHORSESKILL,getFinishTime(System.currentTimeMillis()));
						setMessage(p,a,false,true);
					}
				}
			}catch(Exception e){
				
			}
		} 
	}
	
	public boolean lockSkill(Player p){
		try{
			if(p.horseBag.horses!=null){
				for(Horse h : p.horseBag.horses){
					if(h.lockSkillId>0){
						return true;
					}
				}
			}
		}catch(Exception e){
			
		}
		return false;
	}
	
	/** 统计坐骑技能等级 */
	public void playerHorseSkillLevel(Player p){
		if(p!=null){
			try{
				PvpInfo pvpInfo = getPvpInfo(p.id, p.faction);
				Achievement a = getAchievementById(97);
				if(a!=null){
					if(a.param1.equals("skilllevel")){
						int level = Integer.parseInt(a.param2);
						if(pvpInfo.pool.getString(getPropertyOfHorseSkillLevel(level)).equals("")){
							List<Horse> horses = p.horseBag.horses;
							if(horses!=null&&horses.size()>0){
								for(Horse h : horses){
									if(checkHorseSkill(h,level)){
										pvpInfo.pool.setString(getPropertyOfHorseSkillLevel(level),getFinishTime(System.currentTimeMillis()));
										setMessage(p,a,false,true);
										break;
									}
								}
							}
						}
					}
				}
			}catch(Exception e){
				
			}
		} 
	}
	
	/** 检测坐骑技能是否都达到某一等级*/
	public boolean checkHorseSkill(Horse h,int level){
		if(h!=null){
			List<Skill> skills = h.skills;
			if(skills!=null && skills.size()>0){
				int count = 0;
				for(Skill skill:skills){
					if(skill.getLevel() == level){
						count++;
					}
				}
				if(count == h.skillSize){
					return true;
				}
			}
		}
		return false;
	}
	
	/** 玩家成功幻化坐骑 */
	public void playerChangeHorse(Player p){
		if(p!=null){
			try{
				PvpInfo pvpInfo = getPvpInfo(p.id, p.faction);
				int time = p.pool.getInt(Player.PROPERTY_HORSECHANGE_TIME,0);
				time++;
				p.pool.setInt(Player.PROPERTY_HORSECHANGE_TIME,time);
				for(int i=78;i<81;i++){
					Achievement a = getAchievementById(i);
					if(a!=null){
						if(a.param1.equals("change")){
							int t = Integer.parseInt(a.param2);
							if(time>=t && pvpInfo.pool.getString(getPropertyOfHorseChange(t)).equals("")){
								pvpInfo.pool.setString(getPropertyOfHorseChange(t),getFinishTime(System.currentTimeMillis()));
								setMessage(p,a,false,true);
							}
						}
					}
				}
			}catch(Exception e){
				
			}
		} 
	}
	
	/** 玩家成功合成坐骑 */
	public void playerMergeHorse(Player p,Horse h){
		if(p!=null){
			try{
				PvpInfo pvpInfo = getPvpInfo(p.id, p.faction);
				for(int i=75;i<78;i++){
					Achievement a = getAchievementById(i);
					if(a!=null){
						if(a.param1.equals("merge")){
							int t = Integer.parseInt(a.param2);
							if(h.fixCount>=t && pvpInfo.pool.getString(getPropertyOfHorseMergeLevel(t)).equals("")){
								pvpInfo.pool.setString(getPropertyOfHorseMergeLevel(t),getFinishTime(System.currentTimeMillis()));
								setMessage(p,a,false,true);
							}
						}
					}
				}
			}catch(Exception e){
				
			}
		} 
	}
	
	/** 检测坐骑合成等级*/
	public int checkHorseMerge(Player p){
		int count = 0;
		if(p!=null){
			List<Horse> horses = p.horseBag.horses;
			if(horses!=null && horses.size()>0){
				for(Horse h:horses){
					if(h!=null && h.fixCount > count){
						count=h.fixCount;
					}
				}
			}
		}
		return count;
	}
	
	/** 检测坐骑合成等级*/
	public int checkHorseChange(Player p){
		int count = 0;
		if(p!=null){
			List<Horse> horses = p.horseBag.horses;
			if(horses!=null && horses.size()>0){
				for(Horse h:horses){
					if(h!=null && h.imageIdChange>=0){
						count++;
					}
				}
				int cnt = p.pool.getInt(Player.PROPERTY_HORSECHANGE_TIME,0);
				if(count>=cnt){
					p.pool.getInt(Player.PROPERTY_HORSECHANGE_TIME,count);
				}else{
					count = cnt;
				}
			}
		}
		return count;
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
//			List<Achievement> achs = getAchievementList(KILL_ENEMY_ACHIEVETYPE);
//			if(achs!=null){
				for(int i=7;i<13;i++){
					Achievement a = getAchievementById(i);
					if(a!=null){
						int count = Integer.parseInt(a.param1);
						if(killCount>=count){
							if(count==1 && killInfo.pool.getString(PROPERTY_KILLENEMY_FIRSTTIME).equals("")){//手刃敌人成就
								killInfo.pool.setString(PROPERTY_KILLENEMY_FIRSTTIME, getFinishTime(System.currentTimeMillis()));
								setMessage(p,a,false,true);
							} else if(count == 100 && killInfo.pool.getString(PROPERTY_FINISHTIME_KILL).equals("")){//英勇杀敌成就
								killInfo.pool.setString(PROPERTY_FINISHTIME_KILL, getFinishTime(System.currentTimeMillis()));
								setMessage(p,a,false,true);
							} else if(count == 500 && killInfo.pool.getString(PROPERTY_FINISHTIME_KILLMORE).equals("")){//所向披靡成就
								killInfo.pool.setString(PROPERTY_FINISHTIME_KILLMORE, getFinishTime(System.currentTimeMillis()));
								setMessage(p,a,false,true);
							} else if(count == 1000 && killInfo.pool.getString(getPropertyOfKillEnemy(count)).equals("")){
								killInfo.pool.setString(getPropertyOfKillEnemy(count), getFinishTime(System.currentTimeMillis()));
								setMessage(p,a,false,true);
							} else if(count == 10000 && killInfo.pool.getString(getPropertyOfKillEnemy(count)).equals("")){
								killInfo.pool.setString(getPropertyOfKillEnemy(count), getFinishTime(System.currentTimeMillis()));
								setMessage(p,a,true,true);
							} else if(count == 60000 && killInfo.pool.getString(getPropertyOfKillEnemy(count)).equals("")){
								killInfo.pool.setString(getPropertyOfKillEnemy(count), getFinishTime(System.currentTimeMillis()));
								setMessage(p,a,true,true);
							} 
						}
					}
				}
//			}	
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
		if(Server.isStepServer){
			return;
		}
		switch (event.type) {
		case ServiceEvent.EVENT_UNIT_DIE:
			unitDie((Unit) event.param1, (Unit) event.param2);
			break;
//		case ServiceEvent.EVENT_PLAYER_CHANGE_FACTION:
//			playerChangeFaction((Player)event.param1);
//			break;
		case ServiceEvent.EVENT_PLAYER_MONEY_UP:
			playerMoneyUp((Player)event.param1, (Integer)event.param2, (Integer)event.param3);
			break;
		case ServiceEvent.EVENT_PLAYER_MARRIAGE:
			playerMarriaged((Player)event.param1, (Player)event.param2);
			break;
		case ServiceEvent.EVENT_NATIONCOLLECT:
			playerCollect((Player)event.param1, (Integer)event.param2, (Integer)event.param3);
			break;
//		case ServiceEvent.EVENT_RANK_UP:
//			playerRankUp((Player)event.param1, (Integer)event.param2);
//			break;
		case ServiceEvent.EVENT_ADDJEWEL_SUCCESS:
			playerAddJewel((Player)event.param1,(GameItem)event.param2);
			break;
		case ServiceEvent.EVENT_PLAYER_LEVELUP:
			playerLevelUp((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_LOGINED:
			processAwardProperty((Player)event.param1);
			playerAchieveDataDel((Player)event.param1);
			beautyCheckWhenLogined((Player)event.param1);//登陆时检测选美结果
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
			produceOutPut((Integer)event.param1,(Object)event.param5);
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
			getCreditFromDayQuest((Player)event.param1, (Integer)event.param2);
			break;
		case ServiceEvent.EVENT_PK_END:
			playerPKEnd((Player)event.param1);
			break;
		case ServiceEvent.EVENT_CYCLEINSTANCE_FINISH:
			processCycleInstance(((Player)event.param1),(Integer)event.param2);
			break;
		}
	}
	
	/** 荣誉塔层数成就*/
	public void processCycleInstance(Player p,int maxCycle){
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			for(int i=128;i<130;i++){
				Achievement a = getAchievementById(i);
				if(a!=null){
					if(!a.param1.equals("") && !a.param2.equals("")){
						int t = Integer.parseInt(a.param1); 
						int v = Integer.parseInt(a.param2); 
						if(t==9 && maxCycle>=v && pvpInfo.pool.getString(getPropertyOfCycleInstance(v)).equals("")){
							pvpInfo.pool.setString(getPropertyOfCycleInstance(v), getFinishTime(System.currentTimeMillis()));
							setMessage(p, a, false,true);
						}
					}
				}
			}
		}
	}
	
	public void playerPKEnd(Player winner){
		if(winner!=null){
			PvpInfo pvpInfo = getPvpInfo(winner.id, winner.faction);
			int totalCount = winner.pool.getInt(StatService.PROPERTY_PKWIN_COUNT,0);
			totalCount++;
			for(int i=5;i<7;i++){
				Achievement achievement = getAchievementById(i);
				if(achievement!=null){
					int count = Integer.parseInt(achievement.param2);
					if(pvpInfo.pool.getString(getPropertyOfPkWin(count)).equals("")){
						if(totalCount>=count){
							pvpInfo.pool.setString(getPropertyOfPkWin(count), getFinishTime(System.currentTimeMillis()));
							setMessage(winner, achievement, false,true);
						}
						winner.pool.setInt(PROPERTY_PKWIN_COUNT, totalCount);
					}
				}
			}
		}
	}
	
	public void playerFinishQuest(Player p,int questId,int branch){
		if(p!=null){
			try{
				long time = p.asmVm.getFinishTime(questId);
				PvpInfo pvpInfo = getPvpInfo(p.id, p.faction);
				//常规任务类成就
//				List<Achievement> achs = subAchievement.get(NORMAL_QUEST_ACHIEVETYPE);
//				List<Achievement> ach2 = subAchievement.get(FAME_QUEST_ACHIEVETYPE);
				// achs.addAll(ach2);
				for(int i=111;i<125;i++){
					Achievement ach = getAchievementById(i);
					if(ach!=null){
						String strs = ach.param1;
						if(strs!=null && !strs.equals("dayquestcredit")){//刨去埋头苦干和废寝忘食成就
							if(strs.equals("perfect")){//完美任务
								try{
									int finishCount = p.asmVm.getFinishedQuest(p);
									if(finishCount >= 300 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_PERFECT).equals("")){
										pvpInfo.pool.setString(PROPERTY_FINISHTIME_PERFECT,getFinishTime(time));
										setMessage(p,ach,false,true);
									}
								}catch(Exception e){
									
								}
							} else if(strs.equals("allcyclequest")){//完成所有跑环任务
								doneTask(p,questId,pvpInfo,ach,PROPERTY_DONE_CYCLEQUEST,PROPERTY_FINISHTIME_ALLCYCLEQUEST);
								log.info("[STATSERVICEPLAYERFINISHQUEST]PLAYERID["+p.id+"]QUESTID["+questId+"]ACHID["+ach.achieveId+"]");
							}else if(strs.equals("allfuninstance") && pvpInfo.pool.getString(PROPERTY_FINISHTIME_ALLFUNINSTANCE).equals("")){
								try{//趣味副本
									int qId = Integer.parseInt(ach.param2);
									if(questId == qId){
										pvpInfo.pool.setString(PROPERTY_FINISHTIME_ALLFUNINSTANCE, getFinishTime(time));
									    setMessage(p,ach,false,true);
									}
								}catch(Exception e){
									
								}
							} else{ 
								String[] str = strs.split(",");
								if(isInArray2(str,questId)!=-1){
									String timeStr = getTimeProperty(questId);
									if(timeStr!=null){//常规任务
										if(pvpInfo.pool.getString(timeStr).equals("")){
											 pvpInfo.pool.setString(timeStr, getFinishTime(time));
											 setMessage(p,ach,false,true);
										}
								    } else{
								    	try{
											String str2 = ach.param2;
											if(str2.equals("chrismas")){//圣诞任务
												doneTask(p,questId,pvpInfo,ach,PROPERTY_DONE_CHRISMAS,PROPERTY_FINISHTIME_CHRISMAS);
											}
								    	}catch(Exception e){
								    		
								    	}
									}
								}
							}
						}
					}
				}
				
				
				//朝廷公告和衣带诏
				try{
					int ty = -1;
					for(int i=105;i<111;i++){
						Achievement a = getAchievementById(i);
						if(a!=null){
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
								if(pvpInfo.pool.getString(getPropertyOfFinishQuest(type,num)).equals("")){
									if(count>=num){
										pvpInfo.pool.setString(getPropertyOfFinishQuest(type,num),getFinishTime(System.currentTimeMillis()));
										setMessage(p,a,false,true);
									}
								}
								p.pool.setInt(getPropertyOfQuestCount(type), count);
							}
						}	
					}
				} catch(Exception e){
					
				}
				
				try{
					for(int i=102;i<105;i++){
						Achievement a = getAchievementById(i);
						if(a!=null){
							int type = Integer.parseInt(a.param1);
							String[] str = a.param2.split(",");
							if(isInArray2(str,questId) != -1 && pvpInfo.pool.getString(getPropertyOfFinishQuest(type,1)).equals("")){
								pvpInfo.pool.setString(getPropertyOfFinishQuest(type,1),getFinishTime(System.currentTimeMillis()));
							    setMessage(p,a,false,true);
							}
						}
					}
				} catch(Exception e){
				
			    }
			}catch(Exception e){
				
			}
		}
	}
	
	/** 已完成规定的任务*/
	public void doneTask(Player p,int questId,PvpInfo pvpInfo,Achievement a,String property1,String property2){
		try{
			if(pvpInfo.pool.getString(property2).equals("")){
				String[] strs = null;
				if(a.param1.equals("worldboss")){//击杀所有世界boss
					strs = a.param2.split(",");
				} else if(a.param1.equals("allcyclequest")){//完成所有跑环
					String temp = getCycleQuest(p.faction);
					if(!temp.equals("")){
						strs = temp.split(",");
					}
				} else {
					strs = a.param1.split(",");
				}
				if(strs!=null && isInArray2(strs,questId)!=-1){
				String allreadyDone = pvpInfo.pool.getString(property1);
				    if(!allreadyDone.equals("")){
						String[] str = allreadyDone.split(",");
						if(isInArray2(str,questId)==-1){
							String newStr = allreadyDone+","+questId;
							pvpInfo.pool.setString(property1,newStr);
						}
					} else {
						pvpInfo.pool.setString(property1,String.valueOf(questId));
					}
					allreadyDone = pvpInfo.pool.getString(property1);
					log.info("[STATSERVICEDONETASK]PLAYERID["+p.id+"]QUESTID["+questId+"]ACHID["+a.achieveId+"]ALLREADYDONE["+allreadyDone+"]SOURCEACH["+strs.length+"]");
					String[] str = allreadyDone.split(",");
					if(str.length == strs.length){
						pvpInfo.pool.setString(property2, getFinishTime(System.currentTimeMillis()));
						setMessage(p,a,false,true);
					}
				}
			}
		} catch (Exception e){
			log.error("[DONETASK]PLAYERID["+p.id+"]QUESTID["+questId+"]ACHID["+a.achieveId+"]", e);
		}
	}
	
	public void getCreditFromDayQuest(Player p,int questId){
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			ASMQuest quest=ASMQuestUtil.getQuest(questId);
			int value = 0;
			int originValue = 0;
			if(quest.getGameQuest().getRepeatType() == 3){
				List<QuestRewardSet> ls = quest.getGameQuest().getRewardSets();
				for(QuestRewardSet rewardSet : ls){
					List<QuestRewardItem> items = rewardSet.rewardItems;
					for(QuestRewardItem item:items){
						if(item.rewardType==QuestRewardItem.REWARD_HONOR){
							value = item.rewardValue;
							originValue = pvpInfo.pool.getInt(PROPERTY_GETCREDIT_DAYQUEST,0);
							pvpInfo.pool.setInt(PROPERTY_GETCREDIT_DAYQUEST, value+originValue);
						}
					}
				}
				for(int i=118;i<120;i++){//埋头苦干和废寝忘食
					Achievement a = getAchievementById(i);
					if(a!=null){
						if(a.param1.equals("dayquestcredit")){
							int num = Integer.parseInt(a.param2);
							if(value+originValue>=num && pvpInfo.pool.getString(getPropertyOfGetCreditDayQuest(num)).equals("")){
								pvpInfo.pool.setString(getPropertyOfGetCreditDayQuest(num),getFinishTime(System.currentTimeMillis()));
								setMessage(p,a,false,true);
							}
						}
					}
				}
			}
		}
	}
	
	public String getCycleQuest(int faction){
		if(faction == 1){
			return "1359,1379,1399,1289,1369,1389,1279";
		} else if(faction == 2){
			return "1409,1429,1299,1269,1419,1439,1259";
		} else if(faction ==3){
			return "1309,1329,1349,1249,1319,1339,1239";
		}
		return "";
	}
	
	public void processUseItem(Player p,int itemId,int cnt){
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			if(itemId == 821){
				Achievement a = getAchievementById(98);
				if(a!=null){
					if(!a.param1.equals("") && !a.param2.equals("")){
						int type = Integer.parseInt(a.param1);
						int num = Integer.parseInt(a.param2);
						int count = p.pool.getInt(Player.PROPERTY_HORSE_BOOK,0)+cnt;
						p.pool.setInt(Player.PROPERTY_HORSE_BOOK, count);
						if(type == 1 && count>=num && pvpInfo.pool.getString(PROPERTY_FINISHTIME_HORSEBOOK500).equals("")){
							pvpInfo.pool.setString(PROPERTY_FINISHTIME_HORSEBOOK500, getFinishTime(System.currentTimeMillis()));
        					setMessage(p,a,false,true);
						}
					}
				}
			} else {
    			int count = p.pool.getInt(PROPERTY_USECOUNT_FOOD, 0);
    			Achievement ach = getAchievementById(155);
    			int maxCount = Integer.parseInt(ach.param2);
    			count += cnt;
    			if(pvpInfo.pool.getString(getPropertyOfUseFood(maxCount)).equals("")){
    				p.pool.setInt(PROPERTY_USECOUNT_FOOD, count);
    			}
            	for(int i=154;i<158;i++){
            		Achievement a = getAchievementById(i);
            		if(a!=null){
	            		if(!a.param1.equals("") && a.param2.equals("")){
	            			int itId = Integer.parseInt(a.param1);
	            			if(itId == itemId){
		            			if(itemId == 1183 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_USEYIHESU) == ""){
		            				pvpInfo.pool.setString(PROPERTY_FINISHTIME_USEYIHESU, getFinishTime(System.currentTimeMillis()));
		        					setMessage(p,a,false,true);
		            			} else if(itemId == 1945 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_USESIFANGCAI) == ""){
		            				pvpInfo.pool.setString(PROPERTY_FINISHTIME_USESIFANGCAI, getFinishTime(System.currentTimeMillis()));
		        					setMessage(p,a,false,true);
		            			}
	            			}
	            		}else if(!a.param2.equals("") && a.param1.equals("")){
	        				int num = Integer.parseInt(a.param2);
	            			if(pvpInfo.pool.getString(getPropertyOfUseFood(num)).equals("")&&pvpInfo.pool.getString(getPropertyOfUseFood(maxCount)).equals("")){
	            				if(count>=num){
		            				pvpInfo.pool.setString(getPropertyOfUseFood(num), getFinishTime(System.currentTimeMillis()));
		    						setMessage(p,a,false,true);
	            				}
	            			}
	            		}
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
				for(int i=25;i<28;i++){
					Achievement a = getAchievementById(i);
					if(a!=null){
						int t = Integer.parseInt(a.param1);
						int num = Integer.parseInt(a.param2);
						if(t==0){
							int totalStar = p.getAveStar(p.equipments.equs,0);
							if(totalStar >= num && pvpInfo.pool.getString(getEquipmentProperty2(num,-1)) == ""){
								pvpInfo.pool.setString(getEquipmentProperty2(num,-1), getFinishTime(System.currentTimeMillis()));
								setMessage(p,a,false,true);
							}
						} 
					}
				}
				checkHorseEquip(p,pvpInfo,true);
				
			}
		}
	}
	
	public void checkHorseEquip(Player p,PvpInfo pvpInfo,boolean broadcast){
		for(int i=28;i<31;i++){
			Achievement a = getAchievementById(i);
			if(a!=null){
				int type = Integer.parseInt(a.param1);
				int num = Integer.parseInt(a.param2);
				if(type == 2 && p.horseBag.horses!=null){
					for(Horse h : p.horseBag.horses){
						int totalStar = p.getAveStar(h.equs.equs,1);
						if(totalStar>=num && pvpInfo.pool.getString(getHorseEquipmentStar(num)).equals("")){
							pvpInfo.pool.setString(getHorseEquipmentStar(num), getFinishTime(System.currentTimeMillis()));
							if(broadcast)
							   setMessage(p,a,false,true);
							break;
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
			Achievement a = getAchievementById(32);
			if(a!=null){
				if(pvpInfo.pool.getString(PROPERTY_EQUIP_REMOVEJEWEL) == ""){
					pvpInfo.pool.setString(PROPERTY_EQUIP_REMOVEJEWEL, getFinishTime(System.currentTimeMillis()));
					setMessage(p,a,false,true);
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
			Achievement a = getAchievementById(19);
			if(a!=null){
				if(pvpInfo.pool.getString(PROPERTY_EQUIP_ADDHOLE).equals("")){
					pvpInfo.pool.setString(PROPERTY_EQUIP_ADDHOLE, getFinishTime(System.currentTimeMillis()));
					setMessage(p,a,false,true);
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
			Achievement a = getAchievementById(31);
			if(a!=null){
				if(pvpInfo.pool.getString(PROPERTY_MERGE_JEWEL) == ""){
					pvpInfo.pool.setString(PROPERTY_MERGE_JEWEL, getFinishTime(System.currentTimeMillis()));
					setMessage(p,a,false,true);
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
			countJewelOnEquipment(p,true);
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
							int initHoles = 0;
							if(item.template.equipment!=null && item.template.equipment.initHole>1){
								initHoles = item.template.equipment.initHole;
								holes += initHoles-1;
							}
							if(holes >= 4){
								count5Holes ++;
							}
							NaturalEnhance[] naturalEnhance = enhance.getNaturals();
					        if(naturalEnhance != null && naturalEnhance.length>0){
					        	if(naturalEnhance[0].percent >= 29 && naturalEnhance[1].percent >= 29){
					        		countPerfect ++;
					        	}
					        }
						}else{
							if(item.template.equipment!=null&&item.template.equipment.initHole>=4){
								count5Holes ++;
							}
						}
//					}
				}
			}
	        
			if(wholeOn || type == 1){//拥有蓝装或紫装
	        	for(int i=23;i<25;i++){
	        		Achievement a = getAchievementById(i);
	    			if(a!=null){
		        		int count = Integer.parseInt(a.param1);
		        		int quality = Integer.parseInt(a.param2);
		        		if(quality == Item.QUALITY_BLUE && countBlue >= count && pvpInfo.pool.getString(getEquipmentProperty1(Item.QUALITY_BLUE,-1,-1)) == ""){
							pvpInfo.pool.setString(getEquipmentProperty1(Item.QUALITY_BLUE,-1,-1), getFinishTime(System.currentTimeMillis()));
							if(broadcast){
							   setMessage(p,a,false,true);
							}
						} else if(quality == Item.QUALITY_PURPLE && countPurple >= count && pvpInfo.pool.getString(getEquipmentProperty1(Item.QUALITY_PURPLE,-1,-1)) == ""){
							pvpInfo.pool.setString(getEquipmentProperty1(Item.QUALITY_PURPLE,-1,-1), getFinishTime(System.currentTimeMillis()));
							if(broadcast){
							   setMessage(p,a,false,true);
							}
						}
	        	    }
				}
	        }
			
			if(wholeOn || type == 2){//拥有的五孔装备
	        	for(int i=20;i<23;i++){
	        		Achievement a = getAchievementById(i);
	    			if(a!=null){
		        		int holeCount = Integer.parseInt(a.param1);
						if(count5Holes >= holeCount && pvpInfo.pool.getString(getEquipmentProperty1(-1,holeCount,-1)) == ""){
							pvpInfo.pool.setString(getEquipmentProperty1(-1,holeCount,-1), getFinishTime(System.currentTimeMillis()));
							if(broadcast){
							    setMessage(p,a,false,true);
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
				int totalStar = p.getAveStar(p.equipments.equs,0);
				for(int i=25;i<28;i++){
					Achievement a = getAchievementById(i);
					if(a!=null){
						int t = Integer.parseInt(a.param1);
						int num = Integer.parseInt(a.param2);
						if(t==0){
							if(totalStar >= num && pvpInfo.pool.getString(getEquipmentProperty2(num,-1)) == ""){
								pvpInfo.pool.setString(getEquipmentProperty2(num,-1), getFinishTime(System.currentTimeMillis()));
								if(broadcast){
								   setMessage(p,a,false,true);
								}
							}
						}
				    }
			    }
			}
			if(wholeOn || type == 5){//宝石光效
				int flashLevel = p.equipments.getFlashLevel();
				for(int i=47;i<52;i++){
					Achievement a = getAchievementById(i);
					if(a!=null){
						int t = Integer.parseInt(a.param1);
						int level = Integer.parseInt(a.param2);
						if(t==1){
							if(flashLevel == level && pvpInfo.pool.getString(getEquipmentProperty2(-1,level)) == ""){
								pvpInfo.pool.setString(getEquipmentProperty2(-1,level), getFinishTime(System.currentTimeMillis()));
								if(broadcast){
								    setMessage(p,a,false,true);
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
			int cardCount = p.pool.getInt(CardService.PROPERTY_HAVECARD, 0);
			for(int i=148;i<152;i++){//拥有卡片数量
				Achievement a = getAchievementById(i);
				if(a!=null){
					int count = Integer.parseInt(a.param1);
					if(cardCount >= count && pvpInfo.pool.getString(getCardCollectProperty(count,-1)).equals("")){
						pvpInfo.pool.setString(getCardCollectProperty(count,-1), getFinishTime(System.currentTimeMillis()));
						setMessage(p,a,false,true);
					}
				}
			}
			CardService cardService = Server.server.getServiceRegistry().getCardService();
			CardGroup group = cardService.getCardGroup(groupId);
			for(int i=152;i<154;i++){
				Achievement a = getAchievementById(i);
				if(a!=null){
				    if(!a.param1.equals("")){
					    int grouId = Integer.parseInt(a.param1);
					    if(grouId == groupId){
							if(p.pool.getInt(cardService.getPropertyOfPlayerSuit(grouId), 0) == group.cards.size() && pvpInfo.pool.getString(getCardCollectProperty(-1,grouId)).equals("")){
								pvpInfo.pool.setString(getCardCollectProperty(-1,grouId), getFinishTime(System.currentTimeMillis()));
								setMessage(p,a,false,true);
							}
					    }
				    }
				}
			}
		}
	}
	
	
	public void beautyCheckWhenLogined(Player p){
		if(p==null){
			return;
		}
		BeautyParadeService service=Server.server.getServiceRegistry().getBeautyParadeService();
		for(Beauty beauty:service.lastBeautys){
			if(beauty.playerId==p.id&&beauty.faction==p.faction&&beauty.name.equals(p.name)){
				PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
				Achievement a = getAchievementById(137);
				if(a!=null){
					if(a.param1.equals("beauty") && pvpInfo.pool.getString(PROPERTY_BEAUTY_TOPTEN).equals("")){
						pvpInfo.pool.setString(PROPERTY_BEAUTY_TOPTEN, getFinishTime(System.currentTimeMillis()));
						setMessage(p,a,false,true);
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
			Achievement a = getAchievementById(137);
			if(a!=null){
				if(a.param1.equals("beauty") && pvpInfo.pool.getString(PROPERTY_BEAUTY_TOPTEN).equals("")){
					pvpInfo.pool.setString(PROPERTY_BEAUTY_TOPTEN, getFinishTime(System.currentTimeMillis()));
					setMessage(p,a,false,true);
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
			int count = getHorseCategory(p);
			Achievement ach = getAchievementById(84);
			int maxCount = Integer.parseInt(ach.param1);
			if(count <= maxCount){
				for(int i=81;i<85;i++){
					Achievement a = getAchievementById(i);
					if(a!=null && a.param2.equals("")){
						int cnt = Integer.parseInt(a.param1);
						if(count >= cnt && pvpInfo.pool.getString(getHorseCountProperty(cnt)).equals("")){
							pvpInfo.pool.setString(getHorseCountProperty(cnt), getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,false,true);
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
    		int titleCount = p.titles.titles.size();
			for(int i=145;i<148;i++){
				Achievement a = getAchievementById(i);
				if(a!=null){
    				int num = Integer.parseInt(a.param1);
    				if(titleCount >= num && pvpInfo.pool.getString(getTitleCountProperty(num)) == ""){
    					pvpInfo.pool.setString(getTitleCountProperty(num), getFinishTime(System.currentTimeMillis()));
    	    			setMessage(p,a,false,true);
    				}
    			}	
    		}
    		//拥有天下第一称号
    		Achievement a = getAchievementById(136);
			if(a!=null){
				if(a.param2.equals("title") && pvpInfo.pool.getString(PROPERTY_FINISHTIME_TIANXIADIYITITLE).equals("")){
					int tId = Integer.parseInt(a.param1);
					Title t = TitleUtil.getTitle(tId);
					if(t!=null && p.titles.titles.containsValue(t)){
						pvpInfo.pool.setString(PROPERTY_FINISHTIME_TIANXIADIYITITLE, getFinishTime(System.currentTimeMillis()));
    	    			setMessage(p,a,false,true);
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
			for(int i=13;i<18;i++){
				if(i==14||i==16){
					continue;
				}
				Achievement a = getAchievementById(i);
				if(a!=null){
					int t = Integer.parseInt(a.param1); //成就战争类型 0国战
					int v = Integer.parseInt(a.param2); //胜利方0防守方，1进攻方
					if(type == t && victorySide == v){ 
						if(pvpInfo.pool.getString(getBattleWinProperty(type,victorySide)).equals("")){
							pvpInfo.pool.setString(getBattleWinProperty(type,victorySide), getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,false,true);
						}
					}
				}
			}
		}
	}
	
	/** 统计10分钟内取得国战胜利*/
	public void getNationWinInTen(int pId,Date startTime){
		Player p = ObjectAccessor.getPlayer(pId);
		if(p!=null){
			PvpInfo pvpInfo = getPvpInfo(pId,p.faction);
			Achievement a = getAchievementById(14);
			if(a!=null){
				if(a.param1.equals("hengchongzhizhuang") && pvpInfo.pool.getString(PROPERTY_FINISHTIME_NATIONBATTLEINTEN).equals("")){
					if(nBattleCostTime(startTime,Integer.parseInt(a.param2))){
						pvpInfo.pool.setString(PROPERTY_FINISHTIME_NATIONBATTLEINTEN, getFinishTime(System.currentTimeMillis()));
						setMessage(p,a,false,true);
					}
				}
			}
		}
	}
	
	/** 国战是否在规定时间里胜利*/
	public boolean nBattleCostTime(Date startTime,int time){
		if(System.currentTimeMillis()-startTime.getTime()<=time*60000l){
			return true;
		}
		return false;
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
			for(int i=168;i<176;i++){//打造一件装备或坐骑装备，以及配方书装备
				Achievement a = getAchievementById(i);
				if(a!=null){
					if(a.param2.equals("")){
						int oType = Integer.parseInt(a.param1);
						if(oType == outType && pvpInfo.pool.getString(getProduceProperty(-1,-1,outType,-1)) == ""){
							pvpInfo.pool.setString(getProduceProperty(-1,-1,outType,-1), getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,false,true);
						}
					} else {
						int oType = Integer.parseInt(a.param1);
						int forLevel = Integer.parseInt(a.param2);
						if(outType!=-1 && oType == outType && forLevel == formulaLevel && pvpInfo.pool.getString(getProduceProperty(-1,formulaLevel,outType,-1)) == ""){
							pvpInfo.pool.setString(getProduceProperty(-1,formulaLevel,outType,-1), getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,false,true);	
					    }	
				    }
				}
			}
			
			for(int i=176;i<179;i++){//打造熟练度
				Achievement a = getAchievementById(i);
				if(a!=null){
					int practice = Integer.parseInt(a.param1);
					if(practiceLevel>=practice && pvpInfo.pool.getString(getProduceProperty(practice,-1,-1,-1)).equals("")){
						pvpInfo.pool.setString(getProduceProperty(practice,-1,-1,-1), getFinishTime(System.currentTimeMillis()));
						setMessage(p,a,false,true);
					}
				}
			}
			//打造装备数量
			Achievement ach = getAchievementById(182);
			int maxNum = Integer.parseInt(ach.param1);
			int count = p.pool.getInt(PROPERTY_PRODUCE_EQUIPCOUNT, 0);
			if(count < maxNum){
				count++;
				p.pool.setInt(PROPERTY_PRODUCE_EQUIPCOUNT, count);
			}
			for(int i=179;i<183;i++){//打造数量
				Achievement a = getAchievementById(i);
				if(a!=null){
			    	int num = Integer.parseInt(a.param1);
					if(count >= num && pvpInfo.pool.getString(getProduceProperty(-1,-1,-1,num)).equals("")){
						pvpInfo.pool.setString(getProduceProperty(-1,-1,-1,num),getFinishTime(System.currentTimeMillis()));
						setMessage(p,a,false,true);
					}
				}
			}
		}
	}
	
	/** 打造产出 */
	public void produceOutPut(int playerId,Object output){
		if(output instanceof GameItem){
			Player p = ObjectAccessor.getPlayer(playerId);
			PvpInfo pvpInfo = getPvpInfo(playerId,p.faction);
			Achievement a = getAchievementById(183);
			if(a!=null){
				if(a.param1.equals("dazao")&& pvpInfo.pool.getString(PROPERTY_FINISHTIME_PRODUCEDONGZHUOLING).equals("")){
					int itemId = Integer.parseInt(a.param2);
					GameItem item = (GameItem)output;
					if(itemId == item.template.id){
						pvpInfo.pool.setString(PROPERTY_FINISHTIME_PRODUCEDONGZHUOLING,getFinishTime(System.currentTimeMillis()));
						setMessage(p,a,false,true);
					}
				}
			}
		}
	}
	
	/**  统计学习打造配方书成就*/
	public void studyFormularAchievement(Player p){
		try{
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			Achievement a = getAchievementById(184);
			if(a!=null){
				if(a.param1.equals("study") && pvpInfo.pool.getString(PROPERTY_FINISHTIME_STUDYFORMULARBOOK).equals("")){
					int count = p.pool.getInt(Player.PROPERTY_FORMULAR_BOOK, 0);
					int totleCount = Integer.parseInt(a.param2);
					if(count>=totleCount){
						pvpInfo.pool.setString(PROPERTY_FINISHTIME_STUDYFORMULARBOOK,getFinishTime(System.currentTimeMillis()));
						setMessage(p,a,false,true);
					}
				}
			}
		}catch(Exception e){
			
		}
	}
	
	/** 马换装时统计马装成就 */
	public void horseEquip(Player p){
		if(p!=null && p.id>0){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			List<GameItem> items = new ArrayList<GameItem>();
			if(p.horse!=null){
				for(GameItem it : p.horse.equs.equs){
					if(it!=null){
						items.add(it);
					}
				}
				int num = items.size();
				for(int i=93;i<96;i++){
				   Achievement a = getAchievementById(i);
				   if(a!=null){
					   int number = Integer.parseInt(a.param1);
					   if(num>=number && pvpInfo.pool.getString(getPropertyOfHorseEqu(number)).equals("")){
						   pvpInfo.pool.setString(getPropertyOfHorseEqu(number), getFinishTime(System.currentTimeMillis()));
						   setMessage(p,a,false,true);
					   }
					   if(pvpInfo.pool.getString(getPropertyOfHorseEqu(number)).equals("")){
						   break;
					   }
				   }
				}
			}
			checkHorseEquip(p,pvpInfo,true);
			countJewelOnEquipment(p,true);
		}
	}
	
	/** 马升级时统计马的等级成就 */
	public void horseLevelUp(Player p){
		if(p!=null && p.id>0){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			Achievement ach = getAchievementById(92);
				int maxNum = Integer.parseInt(ach.param1);
				if(pvpInfo.pool.getString(getPropertyOfHorseLevel(maxNum)).equals("")){
				     for(int i=85;i<93;i++){
				    	 Achievement a = getAchievementById(i);
				    	 if(a!=null){
					    	 int level = Integer.parseInt(a.param1);
							 if(p.horse!=null && p.horse.level>=level && pvpInfo.pool.getString(getPropertyOfHorseLevel(level)).equals("")){
								pvpInfo.pool.setString(getPropertyOfHorseLevel(level), getFinishTime(System.currentTimeMillis()));
								setMessage(p,a,false,true);
							 }
							 if(pvpInfo.pool.getString(getPropertyOfHorseLevel(level)).equals("")){
								 break;
							 }
				    	 }
					}
				}
			
			checkHorseEquip(p,pvpInfo,true);
		}
	}
	
	/** 上骑时统计马的等级和马装成就 */
	public void horseRide(Player p){
		if(p!=null && p.id>0){
			horseLevelUp(p);
			horseEquip(p);
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			checkHorseEquip(p,pvpInfo,true);
			countJewelOnEquipment(p,true);
		}
	}
	
	/** 统计玩家元宝消费成就 */
	public void playerIBuyOk(int playerId, int money){
		Player player = (Player)ObjectAccessor.getPlayer(playerId);
		if(player!=null){
			Server.server.getServiceRegistry().getDbService().
            schedule(new GetConsumnCall(player==null ? null : player.session, player,money));
//			PvpInfo pvpInfo = getPvpInfo(playerId,p.faction);
//			List<Achievement> list = getAchievementList(IMONEY_CONSUME_ACHIEVETYPE);
//			if(list!=null){
//				int maxNum = Integer.parseInt(list.get(list.size()-1).param1);
//				int total = 0;
//				if(pvpInfo.pool.getString(getPropertyOfIMoney(maxNum,true)).equals("")){
//					if(pvpInfo.pool.getInt(PROPERTY_IMONEYUSE_COUNT, 0) >= 0){
//						total = pvpInfo.pool.getInt(PROPERTY_IMONEYUSE_COUNT,0)+money;
//						pvpInfo.pool.setInt(PROPERTY_IMONEYUSE_COUNT, total);
//					}
//					for(int i=0;i<list.size();i++){
//						Achievement a = list.get(i);
//						if(a!=null){
//							int num = Integer.parseInt(a.param1);
//							if(pvpInfo.pool.getString(getPropertyOfIMoney(num,true)).equals("")){
//								if(total >=num*3600 || pvpInfo.pool.getInt(PROPERTY_IMONEYUSE_COUNT, 0)<0){
//									pvpInfo.pool.setString(getPropertyOfIMoney(num,true), getFinishTime(System.currentTimeMillis()));
//									if(num==maxNum){
//										setMessage(p,a,true,true);
//									} else if(num<maxNum && pvpInfo.pool.getInt(PROPERTY_IMONEYUSE_COUNT, 0)>0){
//										setMessage(p,a,false,true);
//									}
//								}
//							} 
//							if(pvpInfo.pool.getString(getPropertyOfIMoney(num,true)) == ""){
//									break;
//							}
//						}
//					}
//				}
//			}
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
//			if((p.lastLogoutTime).getTime() < getCancelTime()){
//				pvpInfo.pool = new PropertyPool();
//				Server.server.getServiceRegistry().getDbService().pvpInfoDAO.updateEntity(pvpInfo);
//			}
			
			//单场国战中玩家杀敌国玩家个数中途下线时下次登录时清零
			if(p.map.map!=null && p.map.map.instance == null){
				p.pool.setInt(Player.PROPERTY_KILLENEMY_ONENBATTLE, 0);
			}
			
			//上线统计在线累积成就
			long time = p.pool.getLong(StatService.PROPERTY_ONLINE_TIME,0l);
			for(int i=164;i<168;i++){
				Achievement ach = getAchievementById(i);
				if(ach!=null){
					int t = Integer.parseInt(ach.param1);
					if(time>=t*60*60*1000l && pvpInfo.pool.getString(getPropertyOfOnlineTime(t)).equals("")){
						pvpInfo.pool.setString(getPropertyOfOnlineTime(t), getFinishTime(System.currentTimeMillis()));
						setMessage(p,ach,false,false);
					}
				}
			}
		}
	}
	
	/** 优化已获取成就奖励属性*/
	public void processAwardProperty(Player p){
		if(p.pool.getString(PROPERTY_GET_ACHIEVEREWARD).equals("")){
			String str = "";
			for(Achievement ach : achieveId2Achieve.values()){
				if(ach!=null){
					if(!p.pool.getString(getPropertyOfGetGift(ach.achievementName)).equals("")){
						if(!str.equals("")){
							str +=","+ach.achievementName;
						}else{
						    str +=ach.achievementName;
						}
						log.info("[REMOVEACHIEVEREWARD]ID["+p.id+"]ACHIEVENAME["+ach.achievementName+"]");
					}
					p.pool.remove(getPropertyOfGetGift(ach.achievementName));
				}
			}
			p.pool.setString(PROPERTY_GET_ACHIEVEREWARD, str);
		}
	}
	
	/** 获取已领取成就奖励*/
	public List<String> getAwarded(Player p){
		List<String> strList = new ArrayList<String>();
		String str = p.pool.getString(PROPERTY_GET_ACHIEVEREWARD);
		if(!str.equals("")){
			String[] strs = str.split(",");
			for(int i=0;i<strs.length;i++){
				if(strs[i]!=null)
				    strList.add(strs[i]);
			}
			return strList;
		}
		return strList;
	}
	
	/** 记录领取成就奖励*/
	public void setAwarded(Player p,String achiveName){
		String str = p.pool.getString(PROPERTY_GET_ACHIEVEREWARD);
		if(!str.equals("")){
		    str += ","+achiveName;
		}else{
			str = achiveName;
		}
		p.pool.setString(PROPERTY_GET_ACHIEVEREWARD, str);
	}
	
	public void playerLevelUp(Player p){
	   if(p!=null && p.id>0){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			for(int i=158;i<164;i++){
				Achievement ach = getAchievementById(i);
				if(ach!=null){
					int level = Integer.parseInt(ach.param1);
					if(p.level >= level && pvpInfo.pool.getString(getPropertyOfLevel(level)).equals("")){
						pvpInfo.pool.setString(getPropertyOfLevel(level),getFinishTime(System.currentTimeMillis()));
						setMessage(p,ach,false,true);
					}
				}
			}
	    }
	}
	
	public void playerAddJewel(Player p,GameItem gameItem){
	   if(p!=null && p.id>0){
			PvpInfo pvpInfo = getPvpInfo(p.id,p.faction);
			int jewelLevel = ObjectAccessor.getItemTemplate(gameItem.template.id).useLevel;
			for(int i=34;i<41;i++){
				Achievement a = getAchievementById(i);
				if(a!=null){
					int count = Integer.parseInt(a.param1);
					int jeLevel = Integer.parseInt(a.param2);
					if(count==1 && jeLevel == jewelLevel){
						//得佩圣宝成就
						if(jewelLevel == 7 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_SEV) == ""){
							pvpInfo.pool.setString(PROPERTY_FINISHTIME_SEV,getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,false,true);
						}
						if(jewelLevel != 7 && pvpInfo.pool.getString(getPropertyByLevel(jeLevel)) == ""){
							pvpInfo.pool.setString(getPropertyByLevel(jeLevel), getFinishTime(System.currentTimeMillis()));
							setMessage(p,a,false,true);
						}
					}
				}
			}
			countJewelOnEquipment(p,true);
			
			//成功完成一次宝石镶嵌成就
			

			Achievement a = getAchievementById(33);
			if(a!=null){
				if(pvpInfo.pool.getString(PROPERTY_EQUIP_ADDJEWEL).equals("")){
					pvpInfo.pool.setString(PROPERTY_EQUIP_ADDJEWEL, getFinishTime(System.currentTimeMillis()));
					setMessage(p,a,false,true);
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
    
    /**
     * 坐骑拥有的装备属性2
     * @param starLevel 星级装备
     * @return
     */
    public String getHorseEquipmentStar(int starLevel){
    	return "PROPERTY_FINISHTIME_HORSEEQUSTAR"+starLevel;
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
    
    /** 击杀敌国玩家个数的属性 */
    public String getPropertyOfKillEnemy(int count){
    	return "PROPERTY_FINISHTIME_KILLENEMY"+count;
    }
    
    /** 成就点数属性*/
    public String getPropertyOfFinishAchieve(int point){
    	return "PROPERTY_FINISHTIME_FINISHACHIEVE"+point;
    }
    
    /** 卡片附魔属性*/
    public String getPropertyOfAddCard(int type){
    	return "PROPERTY_FINISHTIME_ADDCARD"+type;
    }
    
    /** 通过荣誉塔层数*/
    public String getPropertyOfCycleInstance(int count){
    	return "PROPERTY_FINISHTIME_CYCLE"+count;
    }
    
    /** 每日任务获取战功属性*/
    public String getPropertyOfGetCreditDayQuest(int num){
    	return "PROPERTY_FINISHTIME_GETCREDITDAYQUEST"+num;
    }
    
    /** 拥有随从属性*/
    public String getPropertyOfGetAttendant(int num){
    	return "PROPERTY_FINISHTIME_GETATTENDANT"+num;
    }
    
    /** 坐骑技能等级属性*/
    public String getPropertyOfHorseSkillLevel(int level){
    	return "PROPERTY_FINISHTIME_HORSESKILLLEVEL"+level;
    }
    
    /** 幻化坐骑次数属性*/
    public String getPropertyOfHorseChange(int num){
    	return "PROPERTY_FINISHTIME_HORSECHANGE"+num;
    }
    
    /** 坐骑合成等级属性*/
    public String getPropertyOfHorseMergeLevel(int level){
    	return "PROPERTY_FINISHTIME_HORSEMERGELEVEL"+level;
    }
    
    public int getIndex(int fa,int kingFa){
    	int[] f = getFactionArr(fa);
        for(int i=0;i<f.length;i++){
        	if(kingFa == f[i]){
        		return i+1;
        	}
        }
        return 1;
    }
    
    public int[] getFactionArr(int fa){
    	int k=0;
    	int[] f = new int[2];
 		for(int i=0;i<faction.length;i++){
 			if(fa!=faction[i]){
 				f[k]=faction[i];
 				k++;
 			}
 		}
		return f;
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
			if(item == null)
				continue;
			equipments.add(item);
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
						if(jewelLevel == 7 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_SEV).equals("")){
							pvpInfo.pool.setString(PROPERTY_FINISHTIME_SEV, getFinishTime(System.currentTimeMillis()));
						}
						if(jewelLevel != 7 && pvpInfo.pool.getString(getPropertyByLevel(jewelLevel)).equals("")){
							pvpInfo.pool.setString(getPropertyByLevel(jewelLevel), getFinishTime(System.currentTimeMillis()));
						}
						if(jewelLevel == 6 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_6COUNT50).equals("")){
							countsix++;
						}
						if(jewelLevel == 7){
							countsev++;
						}
					}
					if(pvpInfo.pool.getString(PROPERTY_FINISHTIME_COUNT50).equals("")){
					     count+=enhance.getJewelCount();
					}
				}
			}
		}

		for(int i=41;i<47;i++){
			Achievement a = getAchievementById(i);
			if(a!=null){
				int jewelCount = Integer.parseInt(a.param1);
				if(a.param2.equals("")){
				    if(jewelCount == 30 && count>=jewelCount && pvpInfo.pool.getString(PROPERTY_FINISHTIME_COUNT30).equals("")){
				    	pvpInfo.pool.setString(PROPERTY_FINISHTIME_COUNT30, getFinishTime(System.currentTimeMillis()));
						if(broadcast){
							setMessage(p,a,false,true);
						}
				    } else if(jewelCount == 40 && count>=jewelCount && pvpInfo.pool.getString(PROPERTY_FINISHTIME_COUNT40).equals("")){
				    	pvpInfo.pool.setString(PROPERTY_FINISHTIME_COUNT40, getFinishTime(System.currentTimeMillis()));
						if(broadcast){
							setMessage(p,a,false,true);
						}
				    } else if(jewelCount == 50 && count>=jewelCount && pvpInfo.pool.getString(PROPERTY_FINISHTIME_COUNT50).equals("")){
				    	pvpInfo.pool.setString(PROPERTY_FINISHTIME_COUNT50, getFinishTime(System.currentTimeMillis()));
						if(broadcast){
							setMessage(p,a,false,true);
						}
				    }
				}else{
					int total = Integer.parseInt(a.param1);
					int level = Integer.parseInt(a.param2);
					if(level == 6){
						if(countsix>=total && pvpInfo.pool.getString(PROPERTY_FINISHTIME_6COUNT50).equals("")){
							pvpInfo.pool.setString(PROPERTY_FINISHTIME_6COUNT50, getFinishTime(System.currentTimeMillis()));
						    if(broadcast){
						    	setMessage(p,a,true,true);
						    }
						}
					}else if(level == 7){
						if(total == 50){//璀璨如星成就
							if(countsev >= total && pvpInfo.pool.getString(PROPERTY_FINISHTIME_7COUNT50).equals("")){
								pvpInfo.pool.setString(PROPERTY_FINISHTIME_7COUNT50, getFinishTime(System.currentTimeMillis()));
							    if(broadcast){
							    	setMessage(p,a,true,true);
							    }
							}
						} else if(total == 85 && pvpInfo.pool.getString(PROPERTY_FINISHTIME_BAQIWAILOU).equals("")){
							int countHorse = countHorseJewel(p);
							int totalJewel = countsev+countHorse;
							if(totalJewel>=total){
								pvpInfo.pool.setString(PROPERTY_FINISHTIME_BAQIWAILOU, getFinishTime(System.currentTimeMillis()));
								if(broadcast){
							    	setMessage(p,a,true,true);
							    }
							}
						}
					}
				}
			 }
		  }
		Achievement a = getAchievementById(52);
		int total = Integer.parseInt(a.param1);
		int level = Integer.parseInt(a.param2);
		if(total ==51&&level==7){//七彩光效成就
			if(countsev >= total && pvpInfo.pool.getString(PROPERTY_FINISHTIME_QICAIGUANGMANG).equals("")){
				pvpInfo.pool.setString(PROPERTY_FINISHTIME_QICAIGUANGMANG, getFinishTime(System.currentTimeMillis()));
			    if(broadcast){
			    	setMessage(p,a,true,true);
			    }
			}
		}
	   }
	}
	
	protected int countHorseJewel(Player p){
		int count = 0;
		try{
			if(p.horse!=null){
				List<GameItem> list = new ArrayList<GameItem>();
				for(GameItem item : p.horse.equs.equs){
					if(item!=null){
						list.add(item);
					}
				}
				for(GameItem item : list){
					if(item!=null){
						ItemEnhance enhance = (ItemEnhance) item.object;
						if(enhance.getJewelCount()>0){
							for(int i=0;i<enhance.getJewelCount();i++){
								int jewelId = enhance.getJewel(i);
								if(ObjectAccessor.getItemTemplate(jewelId)==null)
									continue;
								int jewelLevel = ObjectAccessor.getItemTemplate(jewelId).useLevel;
								if(jewelLevel == 7)
									count++;
						    }
						}
					}
				}
			}
		}catch(Exception e){
			
		}
		return count;
	}
	
	
//	public Achievement getAchievement(int index,int achieveId){
//		List<Achievement> list = getAchievementList(index);
//		if(list!=null && list.size()>0){
//			for(Achievement a : list){
//				if(a.achieveId == achieveId){
//					return a;
//				}
//			}
//		}
//		return null;
//	}

	public void shutdown() {
		synchronized(pvpInfos){
			log.info("UPDATE PVPINFO");
			saveAllOnlinePvpInfos();
			kills.clear();
			log.info("UPDATE PVPINFO OK");
		}
		
	}
	
	protected void saveAllOnlinePvpInfos(){
	    int total = pvpInfos.size();
	    int c = 0;
		for(PvpInfo info:pvpInfos.values()){
			if(info!=null){
				Player player = ObjectAccessor.getPlayer(info.id);
				if(player!=null){
					Server.server.getServiceRegistry().getDbService().pvpInfoDAO.updateEntity(info);
				    c++;
			        if(c % 100 == 0){
			            log.info("UPDATE PVPINFO : " + c + "/" + total);
			        }
				}
			}
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
		if(killLevel<=(killedLevel+10)||killedLevel>=70)
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
		saveAllOnlinePvpInfos();
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
	
	public static int isInArray(int[] arr,int a){
		if(arr != null && arr.length>0){
			for(int i=0;i<arr.length;i++){
				if(arr[i] == a)
					return i;
			}
		}
		return -1;
	}
	
	public static int isInArray2(String[] arr,int a){
		if(arr != null && arr.length>0){
			for(int i=0;i<arr.length;i++){
				if(Integer.parseInt(arr[i]) == a)
					return i;
			}
		}
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	public void parse(Document doc) {
		Element root = doc.getRootElement();
		if (root != null) {
			List<Element> subRoot = root.elements("achievement");
			for(Element el : subRoot){
				int type = Integer.parseInt(el.attributeValue("type"));
			    List<Element> items = el.elements("item");
			    List<Integer> achiveIdList = new ArrayList<Integer>();
			    for(Element it : items){
			    	int achieveId = Integer.parseInt(it.attributeValue("achieveid"));
			    	String catogory = it.attributeValue("catogory");
			    	String name = it.attributeValue("name");
			    	String dec = it.attributeValue("dec");
			    	String param1 = it.attributeValue("param1");
					String param2 = it.attributeValue("param2");
					int point = Integer.parseInt(it.attributeValue("point"));
					String rewardItems = it.attributeValue("reward");
					String property = it.attributeValue("poolkey");
					Achievement perAchieve = new Achievement(achieveId, type,
							name, dec,param1,param2, point,property);
					
					if(rewardItems != ""){
						String[] strs = rewardItems.split(",");
						for(int m=0;m<strs.length;m++){
						    try{
						        perAchieve.addRewardItem(Integer.parseInt(strs[m]));
						    }catch(Exception e){
						        log.error(type + " , "+ " , " + achieveId + items, e);
						    }
						}
					}
					achiveIdList.add(achieveId);
					achieveId2Achieve.put(achieveId, perAchieve);
			    }
			    type2AchiveId.put(type, achiveIdList);
			}
	    }	
	}
	
	public Achievement getAchievementById(int achieveId){
		return achieveId2Achieve.get(achieveId);
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
			DuelService duelService = Server.server.getServiceRegistry().getDuelService();
			if(person == null && duelService!=null)
				person = duelService.getStatue(personId);
			if(person != null){
				PropertyPool po = null;
				if(person.id>0){
				     PvpInfo pvpInfo = getPvpInfo(person.id, person.faction);
				     po = pvpInfo.pool;
				} else {
				    Fame fame = Server.server.getServiceRegistry().getFameService().getFame(person.id);
				    if(fame != null){
					   po = fame.pool;
				    } else {
				    	PvpInfo pvpInfo = getPvpInfo(person.id, person.faction);
					    po = pvpInfo.pool;
				    }
				}
				if(type == catagoryNames.length-2){
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
				} else if(type == catagoryNames.length-1){
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
					List<Integer> achievements = type2AchiveId.get(type);
				    if(type == 5){
					List<Integer> achs = new ArrayList<Integer>();
					for(int id : achievements){
						if(id == 138 || id == 139 || id == 140){
							continue;
						}
						achs.add(id);
					}
					for(int j=0;j<factionIndex.length;j++){
						if(person.faction!=faction[j]){
							achs.add(factionIndex[j]);
						}
					}
					achievements = new ArrayList<Integer>(achs);
				}
				List<Achievement> list = new ArrayList<Achievement>();
				for(int ty : achievements){
					Achievement achieve = achieveId2Achieve.get(ty);
					achieve.finiTime = getTime(person, achieve);
					achieve.acomplish = (byte)(achieve.finiTime.equals("")?0:1);
					list.add(achieve);
				}
//					Map<Achievement,Byte> list = new HashMap<Achievement,Byte>();
//					for(Achievement ach : achievements){
//						getAccomplishAndFiniTime(ach,player);
//						list.put(ach, ach.acomplish);
//					}
//					for (int i=0;i<achievements.size();i++) {
//						Achievement ach = achievements.get(i);
//						getAccomplishAndFiniTime(ach, person);
//					}
//					if(type == 6){//击杀国君需按魏蜀吴排序
//						Collections.sort(achievements, new IdSort());
//					} else {
						Collections.sort(list, new ConverSort());
//					}
					
					int size = list.size();
					pt.putShort(size);
					for (Achievement ach : list) {
						pt.putInt(ach.achieveId);
						pt.putString(ach.achievementName);
						pt.put(ach.acomplish);
						pt.putString(ach.dec);
						pt.putInt(ach.point);
						pt.putString(ach.finiTime);
//						Byte b = list.get(ach);
						String b = getTime(player,ach);
						pt.put(b.equals("")?0:1);
						List<Integer> rewardItems = ach.getRewardItem();
						if(rewardItems != null && rewardItems.size()>0){
							int sz = rewardItems.size();
							List<String> awarded = getAwarded(person);
							if(awarded.contains(ach.achievementName)){
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
				ErrorHandler.sendErrorMessage(session, serial, OpCode.PERSONAL_ACHIEVEMENT_DETAIL_CLIENT, peony.Messages.STRING_00116);
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
		int size = type2AchiveId.size();
		if (player != null) {
			Packet pt = new Packet(OpCode.PERSONAL_ACHIEVEMENT_SERVER);
			pt.putInt(serial);
			pt.putShort(size);
			Player person = (Player)ObjectAccessor.getPlayer(personId);
			if(person == null){
				person =  Server.server.getServiceRegistry().getFameService().getStatue(personId);
			}
			DuelService duelService = Server.server.getServiceRegistry().getDuelService();
			if(person == null && duelService!=null)
				person = duelService.getStatue(personId);
			if(person == null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.PERSONAL_ACHIEVEMENT_CLIENT, peony.Messages.STRING_00088);
				return;
			}
			for (int i=0;i<type2AchiveId.size();i++) {
				int finished = 0;
				int point = 0;
				List<Integer> list = type2AchiveId.get(i);
				if(i == 5){
					List<Integer> achs = new ArrayList<Integer>();
					for(int j=0;j<factionIndex.length;j++){
						if(person.faction!=faction[j]){
							achs.add(factionIndex[j]);
						}
					}
					list = new ArrayList<Integer>(achs);
				}
				pt.putString(catagoryNames[i]);
				pt.putInt(list.size());
				if(person.id>0){
					processQuest(person);
					countJewelOnEquipment(person,false);
				}
				for (int achId : list) {
					Achievement ach = getAchievementById(achId);
					String finishTime = getTime(person,ach);
					ach.acomplish = (byte)(finishTime.equals("")?0:1);
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
			if(person.id>0){
				//完成成就点数的成就
				finishAchievePoint(person,pointCount);
				person.pool.setInt(Player.PROPERTY_ACHIEVEMENT_POINT, pointCount);
			}
			pt.putString(catagoryNames[catagoryNames.length-2]);
			pt.putInt(2);
			pt.putString(catagoryNames[catagoryNames.length-1]);
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
			Achievement ach = achieveId2Achieve.get(achieveId);
			if(ach != null){
				String finishTime = getTime(p,ach);
				ach.acomplish = (byte)(finishTime.equals("")?0:1);
				if(ach.acomplish == 0){
					throw new Exception(peony.Messages.STRING_00846);
				}
				List<String> awarded = getAwarded(p);
				if(awarded.contains(ach.achievementName)){
					throw new Exception(peony.Messages.STRING_00847);
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
									peony.Messages.STRING_00848, ach.achievementName,count,
									item.template.name);
							Server.server.getServiceRegistry().getMailService()
							.sendSystemMail(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_00849, content, 0,
									item, count, "ACHIEVEMENTREWARD");
						}
//						p.pool.setString(getPropertyOfGetGift(ach.achievementName), getFinishTime(System.currentTimeMillis()));
						setAwarded(p,ach.achievementName);
					}
				} else {
					throw new Exception(peony.Messages.STRING_00850);
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
	
//	public List<Achievement> getAchievementList(int achieveTypeId){
//		if(subAchievement!=null && subAchievement.size()>0){
//			List<Achievement> list = subAchievement.get(achieveTypeId);
//			if(list!=null && list.size()>0){
//				return list;
//			}
//		}
//		return null;
//	}
	
	/**
	 * 完成成就发送消息
	 * @param p
	 * @param type 成就类型
	 * @param subType 成就子类型
	 * @param worldMessage 是否需要发世界聊
	 * @return
	 */
	public String setMessage(Player p,Achievement achieve,boolean worldMessage,boolean openWeibo){
		if(p!=null){
			if(achieve != null){
//				Server.server.getServiceRegistry().getChatService()
//				.sendAreaSystemMessage(MessageFormat.format(peony.Messages.STRING_00851, p.name,achieve.achievementName),p.map.id);
				ChatMessage cm = new ChatMessage(ChatOption.AREA,p.id,-1,peony.Messages.STRING_00004+"(成就)",p.map.id,MessageFormat.format(peony.Messages.STRING_00851, p.name,achieve.achievementName)+"#"+achieve.dec,null);
				Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
				if(worldMessage){
//					Server.server.getServiceRegistry().getChatService()
//					.sendWorldMessage(MessageFormat.format(peony.Messages.STRING_00851, p.name,achieve.achievementName));
					ChatMessage cm2 = new ChatMessage(ChatOption.WORLD, p.id, -1,peony.Messages.STRING_00004+"(成就)", MessageFormat.format(peony.Messages.STRING_00851, p.name,achieve.achievementName)+"#"+achieve.dec, null);
					Server.server.getServiceRegistry().getChatService().addChatMessage(cm2);
				}
				int achievePoint = p.pool.getInt(Player.PROPERTY_ACHIEVEMENT_POINT, 0);
				if(achievePoint == 0){
					achievePoint = countAchievePoint(p);
					p.pool.setInt(Player.PROPERTY_ACHIEVEMENT_POINT, achievePoint);
				} else {
					achievePoint += achieve.point;
				}
				Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_ACHIEVE_FINISH,p));
				//统计成就点数的成就
				finishAchievePoint(p,achievePoint);
				LogUtil.logFinishAchievement(p.id, achieve.achievementName);
				//完成成就触发发送微博
//				if(openWeibo){
//					if(p.pool.getInt(Player.PROPERTY_WEIBO_ACTIVE, 0)==0 || (p.pool.getInt(Player.PROPERTY_WEIBO_ACTIVE, 0)==1 && p.pool.getString(Player.PROPERTY_WEIBO_TOKEN)!="")){
//						WeiboService weiboService = Server.server.getServiceRegistry().getWeiboService();
//						String msg = MessageFormat.format(peony.Messages.STRING_00852,
//								weiboService.getDitrict(),p.name, achieve.dec,achieve.achievementName);
//						if(p.pool.getInt(Player.PROPERTY_WEIBO_ACTIVE, 0)==0){
//						   p.pool.setInt(Player.PROPERTY_WEIBO_ACTIVE, 1);
//						}
//						weiboService.showWeiboUI(p, msg);
//					}
//				}
			}
		}
		return "";
	}

//	public void getAccomplishAndFiniTime(Achievement ach, Player person) {
//		PropertyPool po = null;
//		if(person.id>0){
//	      PvpInfo pvpInfo = getPvpInfo(person.id, person.faction);
//	      po = pvpInfo.pool;
//		} else {
//			Fame fame = FameService.fames.get(person.id);
//			if(fame != null){
//			   po = fame.pool;
//			} else {
//				 PvpInfo pvpInfo = getPvpInfo(person.id, person.faction);
//			     po = pvpInfo.pool;
//			}
//		}
//		List<Integer> list = achieveType.get(ach.type);
//		if(list!=null && list.size()>0){
//			for(int index=0;index<list.size();index++){
//			    int achieveTypeId = list.get(index);
//				List<Achievement> achs = subAchievement.get(achieveTypeId);
//				if(achs!=null && achs.size()>0){
//					switch(ach.type){
//					case 0: //常规
//						   switch(index){
//						   case 0:
//							      for(Achievement a : achs){
//							    	  if(a.param2.equals("qixi")){
//							    		  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_QUESTQIXI).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(PROPERTY_FINISHTIME_QUESTQIXI);
//							    	  } else if(a.param2.equals("midautom")){
//							    		  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_QUESTMID).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(PROPERTY_FINISHTIME_QUESTMID);
//							    	  } else if(a.param2.equals("owe")){
//							    		  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_QUESTOWE).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(PROPERTY_FINISHTIME_QUESTOWE);
//							    	  } else if(a.param2.equals("chrismas")){//圣诞任务
//							    		  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_CHRISMAS).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(PROPERTY_FINISHTIME_CHRISMAS);
//							    	  }
//							      }
//							      break;
//						   }
//						   break;
//					case 1:
//						   switch(index){
//						   case 0: //荣誉类任务
//								   for(Achievement a:achs){
//									   if(!a.param2.equals("") && a.param2.equals("zusha")){
//							    		  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_ZHUSHA).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(PROPERTY_FINISHTIME_ZHUSHA);
//								       } else if(!a.param2.equals("") && a.param2.equals("yuxi")){
//							    		  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_YUXI).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(PROPERTY_FINISHTIME_YUXI);
//								       } else if(!a.param2.equals("") && a.param2.equals("tianlong")){
//							    		  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_QUESTTIANLONG).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(PROPERTY_FINISHTIME_QUESTTIANLONG);
//								       } else if(!a.param2.equals("") && a.param1.equals("allfuninstance")){//有趣的人成就
//								    	   if(person.id>0){//趣味副本
//								    		   int qId = Integer.parseInt(a.param2);
//								    		   long time = person.asmVm.getFinishTime(qId);
//								    		   if(time!=0 && po.getString(PROPERTY_FINISHTIME_ALLFUNINSTANCE).equals("")){
//								    			   po.setString(PROPERTY_FINISHTIME_ALLFUNINSTANCE,getFinishTime(time));
//								    		   }
//								    	   }
//								    	   a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_ALLFUNINSTANCE).equals("")) ? 0 : 1);
//										   a.finiTime = po.getString(PROPERTY_FINISHTIME_ALLFUNINSTANCE);
//								       } else if(a.param1.equals("allcyclequest")){//完成所有跑环任务
//								    	   if(person.id>0){
//									    	   String questIds = getCycleQuest(person.faction);
//									    	   if(!questIds.equals("")){
//									    		   String[] strs = questIds.split(",");
//									    		   int count = 0;
//									    		   for(int i=0;i<strs.length;i++){
//									    			   long time = person.asmVm.getFinishTime(Integer.parseInt(strs[i]));
//									    			   if(time!=0){
//									    				   count ++;
//									    			   }
//									    		   }
//									    		   if(count == strs.length){
//									    			   po.setString(PROPERTY_FINISHTIME_ALLCYCLEQUEST,getFinishTime(System.currentTimeMillis()));
//									    		   }
//									    	   }
//								    	   }
//								    	   a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_ALLCYCLEQUEST).equals("")) ? 0 : 1);
//										   a.finiTime = po.getString(PROPERTY_FINISHTIME_ALLCYCLEQUEST);
//								       } else if(a.param1.equals("dayquestcredit")){//从每日任务处获得战功
//								    	   int num = Integer.parseInt(a.param2);
//								    	   a.acomplish = (byte) ((po.getString(getPropertyOfGetCreditDayQuest(num)).equals("")) ? 0 : 1);
//										   a.finiTime = po.getString(getPropertyOfGetCreditDayQuest(num));
//								       } else if(a.param1.equals("perfect")){//完美任务
//							    		  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_PERFECT).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(PROPERTY_FINISHTIME_PERFECT);
//								       }
//								   }
//							       break;
//						   case 1: //杀死敌国玩家
//							       for(Achievement a:achs){ 
//							    	   int killCount = Integer.parseInt(a.param1);
//							    	   if(killCount == 1){
//							    		   a.acomplish = (byte) ((po.getString(PROPERTY_KILLENEMY_FIRSTTIME).equals("")) ? 0 : 1);
//										   a.finiTime = po.getString(PROPERTY_KILLENEMY_FIRSTTIME);
//							    	   } else if(killCount == 100){
//							    		   a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_KILL).equals("")) ? 0 : 1);
//										   a.finiTime = po.getString(PROPERTY_FINISHTIME_KILL);
//							    	   } else if(killCount == 500){
//							    		   a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_KILLMORE).equals("")) ? 0 : 1);
//										   a.finiTime = po.getString(PROPERTY_FINISHTIME_KILLMORE);
//							    	   } else if(killCount == 1000){
//							    		   a.acomplish = (byte) ((po.getString(getPropertyOfKillEnemy(1000)).equals("")) ? 0 : 1);
//										   a.finiTime = po.getString(getPropertyOfKillEnemy(1000));
//							    	   } else if(killCount == 10000){
//							    		   a.acomplish = (byte) ((po.getString(getPropertyOfKillEnemy(10000)).equals("")) ? 0 : 1);
//										   a.finiTime = po.getString(getPropertyOfKillEnemy(10000));
//							    	   } else if(killCount == 60000){
//							    		   a.acomplish = (byte) ((po.getString(getPropertyOfKillEnemy(60000)).equals("")) ? 0 : 1);
//										   a.finiTime = po.getString(getPropertyOfKillEnemy(60000));
//							    	   }
//							       }
//							       break;
//						   case 2:
//								  for(Achievement a : achs){
//								      String type = a.param2;
//								      if(type.equals("collect")){     //国家捐款
//									      int collectNum = Integer.parseInt(a.param1);
//									      if(person.id>0 && po.getString(PROPERTY_FINISHTIME_COLLECT).equals("") && po.getInt(
//													PROPERTY_NATION_COLLECT, 0) >= collectNum){
//												po.setString(PROPERTY_FINISHTIME_COLLECT,getFinishTime(System.currentTimeMillis()));
//										  }
//									      a.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_COLLECT).equals("") ? 0 : 1);
//									      a.finiTime = po.getString(PROPERTY_FINISHTIME_COLLECT);
//								      } else if(type.equals("title")){ //拥有天下第一称号
//								    	  if(person.id>0){
//									    	  int titleId = Integer.parseInt(a.param1);
//									    	  Title t = TitleUtil.getTitle(titleId);
//									    	  if(t!=null && person.titles.titles.values().contains(t) && po.getString(PROPERTY_FINISHTIME_TIANXIADIYITITLE).equals("")){
//									    		  po.setString(PROPERTY_FINISHTIME_TIANXIADIYITITLE,getFinishTime(System.currentTimeMillis()));
//									    	  }
//								    	  }
//								    	  a.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_TIANXIADIYITITLE).equals("") ? 0 : 1);
//									      a.finiTime = po.getString(PROPERTY_FINISHTIME_TIANXIADIYITITLE);
//								      }
//								  }
//							      break;
//						   case 3://击杀boss
//							      for(Achievement a : achs){
//							    	  String type = a.param1;
//							    	  if(type.equals("dongzhuo")){//击杀董卓
//								    	  a.acomplish = (byte)((po.getString(PROPERTY_FINISHTIME_KILLDONGZHUO).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(PROPERTY_FINISHTIME_KILLDONGZHUO);
//							    	  } else if(type.equals("worldboss")){//击杀所有世界boss
//							    		  a.acomplish = (byte)((po.getString(PROPERTY_FINISHTIME_KILLALLWORLDBOSS).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(PROPERTY_FINISHTIME_KILLALLWORLDBOSS);
//							    	  }
//							      }
//							      break;
//							      
//						   case 4: //战争类
//							      for(Achievement a : achs){
//							    	  int battleType = Integer.parseInt(a.param1);
//							    	  int victory = Integer.parseInt(a.param2);
//							    	  a.acomplish = (byte)((po.getString(getBattleWinProperty(battleType,victory)).equals("")) ? 0 : 1);
//									  a.finiTime = po.getString(getBattleWinProperty(battleType,victory));
//							      }
//							      break;
//						   case 5: //战争相关类
//							      for(Achievement a : achs){
//							    	  String type = a.param1;
//							    	  if(type.equals("hengchongzhizhuang")){
//								    	  a.acomplish = (byte)((po.getString(PROPERTY_FINISHTIME_NATIONBATTLEINTEN).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(PROPERTY_FINISHTIME_NATIONBATTLEINTEN);
//							    	  } else if(type.equals("huguoyougong")){
//							    		  a.acomplish = (byte)((po.getString(PROPERTY_FINISHTIME_KILLENMEMYONENBATTLE).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(PROPERTY_FINISHTIME_KILLENMEMYONENBATTLE); 
//							    	  } else if(type.equals("qijinqichu")){
//							    		  a.acomplish = (byte)((po.getString(PROPERTY_FINISHTIME_KILLENMEMYNBATTLE).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(PROPERTY_FINISHTIME_KILLENMEMYNBATTLE); 
//							    	  } 
//							      }
//							      break;
//						   }
//						   break;
//					case 2:
//						   switch(index){
//						   case 0: //拥有的金钱
//							      for(Achievement a : achs){
//							    	  int moneyCount = Integer.parseInt(a.param1);
//							    	  if(person.id>0 && person.money >= moneyCount){
//								    	  if(moneyCount == 10000 && po.getString(PROPERTY_FINISHTIME_TENTHOU).equals("")){
//											po.setString(PROPERTY_FINISHTIME_TENTHOU,getFinishTime(System.currentTimeMillis()));
//										  }else if(moneyCount == 1000000 && po.getString(PROPERTY_FINISHTIME_MILLIONARE).equals("")){
//												po.setString(PROPERTY_FINISHTIME_MILLIONARE,getFinishTime(System.currentTimeMillis()));
//										  }
//							          }
//							    	  if(moneyCount == 10000){
//							    		  a.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_TENTHOU).equals("") ? 0 : 1);
//										  a.finiTime=po.getString(PROPERTY_FINISHTIME_TENTHOU);
//							    	  } else if(moneyCount == 1000000){
//							    		  a.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_MILLIONARE).equals("") ? 0 : 1);
//										  a.finiTime=po.getString(PROPERTY_FINISHTIME_MILLIONARE);
//							    	  }
//							      }
//							      break;
//						   case 1://美食相关
//							      for(Achievement a:achs){
//							    	  if(!a.param1.equals("") && a.param2.equals("")){
//							    		  int itemId = Integer.parseInt(a.param1);
//							    		  if(itemId == 1183){
//							    			a.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_USEYIHESU).equals("") ? 0 : 1);
//							  				a.finiTime = po.getString(PROPERTY_FINISHTIME_USEYIHESU);
//							    		  }else if(itemId == 1945){
//							    			a.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_USESIFANGCAI).equals("") ? 0 : 1);
//								  			a.finiTime = po.getString(PROPERTY_FINISHTIME_USESIFANGCAI);
//							    		  }
//							    	  }else if(!a.param2.equals("") && a.param1.equals("")){
//							    		  int num = Integer.parseInt(a.param2);
//							    		  a.acomplish = (byte) (po.getString(getPropertyOfUseFood(num)).equals("") ? 0 : 1);
//										  a.finiTime = po.getString(getPropertyOfUseFood(num));
//							    	  }
//							      }
//							      break;
//						   case 2://生活其它
//								   for(Achievement a:achs){
//									   //洞房花烛
//									   if(a.param1.equals("marrage")){
//										   if(person.id>0){
//											   PlayerRelation rel = Server.server.getServiceRegistry()
//												.getRelationService().get(person.id);
//												if ((rel != null && rel.mateId != -1)
//															&& po.getString(PROPERTY_FINISHTIME_MARRIAGE).equals("")){
//													po.setString(PROPERTY_FINISHTIME_MARRIAGE,getFinishTime(System.currentTimeMillis()));
//													}
//										   }
//										   a.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_MARRIAGE).equals("") ? 0 : 1);
//										   a.finiTime = po.getString(PROPERTY_FINISHTIME_MARRIAGE);
//									   }else if(a.param1.equals("beauty")){ //选美
//										   a.acomplish = (byte) (po.getString(PROPERTY_BEAUTY_TOPTEN).equals("") ? 0 : 1);
//										   a.finiTime = po.getString(PROPERTY_BEAUTY_TOPTEN);
//									   }else if(a.param1.equals("graduate")){//师徒
//										   a.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_APPRENTICE_GRADUATE).equals("") ? 0 : 1);
//										   a.finiTime = po.getString(PROPERTY_FINISHTIME_APPRENTICE_GRADUATE);
//									   }else if(a.param1.equals("marragedegree")){//恩爱度
//										   a.acomplish = (byte) (po.getString(PROPERTY_FINISHTIME_ENAIDU).equals("") ? 0 : 1);
//										   a.finiTime = po.getString(PROPERTY_FINISHTIME_ENAIDU);
//									   }else if(a.param1.equals("attendant")){//拥有随从
//										   int count = Integer.parseInt(a.param2);
//										   if(person.id>0){
//											   int max = po.getInt(PROPERTY_GET_ATTENDANT,0);
//											   int totalAtt = person.attendantBag.attendants.size();
//											   if(max<totalAtt){
//												   max = totalAtt;
//												   po.setInt(PROPERTY_GET_ATTENDANT, max);
//											   }
//											   if(max>=count && po.getString(getPropertyOfGetAttendant(count)).equals("")){
//												   po.setString(getPropertyOfGetAttendant(count),getFinishTime(System.currentTimeMillis()));
//											   }
//										   }
//										   a.acomplish = (byte) (po.getString(getPropertyOfGetAttendant(count)).equals("") ? 0 : 1);
//										   a.finiTime = po.getString(getPropertyOfGetAttendant(count));
//									   }
//								   }
//								   break;
//						   }
//						   break;   
//					case 3:
//						  switch(index){
//						  case 0:
//								  for(Achievement a : achs){//玩家等级
//									  int level = (Integer.parseInt(a.param1));
//									  if(person.id>0){
//										  if(person.level >=level && po.getString(getPropertyOfLevel(level)).equals("")){
//												po.setString(getPropertyOfLevel(level), getFinishTime(System.currentTimeMillis()));
//										  }
//									  }
//									  a.acomplish = (byte) ((po.getString(getPropertyOfLevel(level)).equals("")) ? 0 : 1);
//									  a.finiTime = po.getString(getPropertyOfLevel(level));
//								  } break;
//						  case 1:
//								  for(Achievement a : achs){//在线时间
//									  int count = (Integer.parseInt(a.param1));
//									  a.acomplish = (byte) ((po.getString(getPropertyOfOnlineTime(count)).equals("")) ? 0 : 1);
//									  a.finiTime = po.getString(getPropertyOfOnlineTime(count));
//								  } break;
//						   }
//						   break;
//					case 4:
//						   switch(index){
//						   case 0:
//							     for(Achievement a:achs){
//							    	 int level = Integer.parseInt(a.param2);
//							    	 if(level == 7){
//							    		 a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_SEV).equals("")) ? 0 : 1);
//										 a.finiTime = po.getString(PROPERTY_FINISHTIME_SEV);
//							    	 }else {
//							    		 a.acomplish = (byte) ((po.getString(getPropertyByLevel(level)).equals("")) ? 0 : 1);
//										 a.finiTime = po.getString(getPropertyByLevel(level));
//							    	 }
//							     }
//							     break;
//						   case 1:
//							      for(Achievement a:achs){
//							    	  int total = Integer.parseInt(a.param1);
//							    	  if(a.param2.equals("")){
//							    		  if(total == 30){
//								    		  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_COUNT30).equals("")) ? 0 : 1);
//								  			  a.finiTime = po.getString(PROPERTY_FINISHTIME_COUNT30);
//							    		  }else if(total == 40){
//							    			  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_COUNT40).equals("")) ? 0 : 1);
//								  			  a.finiTime = po.getString(PROPERTY_FINISHTIME_COUNT40);
//							    		  }else if(total == 50){
//							    			  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_COUNT50).equals("")) ? 0 : 1);
//								  			  a.finiTime = po.getString(PROPERTY_FINISHTIME_COUNT50);
//							    		  }
//							    	  }else {
//							    		  int level = Integer.parseInt(a.param2);
//							    		  int count =Integer.parseInt(a.param1);
//							    		  if(level == 6){//珠光宝气
//							    			  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_6COUNT50).equals("")) ? 0 : 1);
//							  				  a.finiTime = po.getString(PROPERTY_FINISHTIME_6COUNT50);
//							    		  }else if(level == 7){
//							    			  if(count == 50){//璀璨如星
//								    			  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_7COUNT50).equals("")) ? 0 : 1);
//								  				  a.finiTime = po.getString(PROPERTY_FINISHTIME_7COUNT50);
//							    			  } else if(count == 51){//七彩光芒
//								    			  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_QICAIGUANGMANG).equals("")) ? 0 : 1);
//									  			  a.finiTime = po.getString(PROPERTY_FINISHTIME_QICAIGUANGMANG);
//									    	  } else if(count == 85){
//									    		  a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_BAQIWAILOU).equals("")) ? 0 : 1);
//									  			  a.finiTime = po.getString(PROPERTY_FINISHTIME_BAQIWAILOU);
//									    	  }
//							    		  }
//							    	  }
//							      }
//							     break;
//						   }
//						   break;
//					case 5:
//						   switch(index){
//						   case 0:
//							      for(Achievement a:achs){
//							    	  if(!a.param1.equals("")){
//							    		  int count = Integer.parseInt(a.param1);
//							    		  if(person.id>0 && count == 1){
//							    			  if(po.getString(PROPERTY_FINISHTIME_AUCTION)!="" &&
//							  					po.getString(getPropertyOfAuction(count,0,true))!=po.getString(PROPERTY_FINISHTIME_AUCTION)){
//							  					po.setString(getPropertyOfAuction(count,0,true),po.getString(PROPERTY_FINISHTIME_AUCTION));
//							  				  }
//							    		  }
//							    		  a.acomplish = (byte) ((po.getString(getPropertyOfAuction(count,0,true)).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(getPropertyOfAuction(count,0,true));
//							    	  }else{
//							    		  int money = Integer.parseInt(a.param2);
//							    		  a.acomplish = (byte) ((po.getString(getPropertyOfAuction(0,money,true)).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(getPropertyOfAuction(0,money,true));
//							    	  }
//							      }
//							      break;
//						   case 1:
//							      for(Achievement a :achs){
//							    	  if(!a.param1.equals("")){
//							    		  int count = Integer.parseInt(a.param1);
//							    		  a.acomplish = (byte) ((po.getString(getPropertyOfAuction(count,0,false)).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(getPropertyOfAuction(count,0,false));
//							    	  }else {
//							    		  int money = Integer.parseInt(a.param2);
//							    		  a.acomplish = (byte) ((po.getString(getPropertyOfAuction(0,money,false)).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(getPropertyOfAuction(0,money,false));
//							    	  }
//							      }
//							      break;
//						   }
//						   break;
//					case 6:
//							int[] f = getFactionArr(person.faction);
//							if(ach.achieveId == f[0]-1){
//							    ach.acomplish = (byte) ((po.getString(getPropertyOfKillKing(1)).equals("")) ? 0 : 1);
//								ach.finiTime = po.getString(getPropertyOfKillKing(1));
//							}
//							
//							if(ach.achieveId == f[1]-1){
//							    ach.acomplish = (byte) ((po.getString(getPropertyOfKillKing(2)).equals("")) ? 0 : 1);
//								ach.finiTime = po.getString(getPropertyOfKillKing(2));
//							}
//						   break;
//					case 7:
//						  switch(index){
//						  case 0:
//							     for(Achievement a:achs){
//							    	 int level = Integer.parseInt(a.param1);
//							    	 a.acomplish = (byte) ((po.getString(getPropertyOfHorseLevel(level)).equals("")) ? 0 : 1);
//									 a.finiTime = po.getString(getPropertyOfHorseLevel(level));
//							     }
//							     break;
//						  case 1:
//							    for(Achievement a:achs){
//							    	int count = Integer.parseInt(a.param1);
//							    	a.acomplish = (byte) ((po.getString(getPropertyOfHorseEqu(count)).equals("")) ? 0 : 1);
//									a.finiTime = po.getString(getPropertyOfHorseEqu(count));
//							    }
//							    break;
//						  case 2:
//							    for(Achievement a:achs){
//							    	int count = Integer.parseInt(a.param1);
//							    	if(a.param2.equals("")){
//								    	if(person.id>0){
//								    		int category = getHorseCategory(person);
//								    		if(category>=count && po.getString(getHorseCountProperty(count)).equals("")){
//								    			po.setString(getHorseCountProperty(count),getFinishTime(System.currentTimeMillis()));
//								    		}
//								    	}
//								    	a.acomplish = (byte)((po.getString(getHorseCountProperty(count)).equals("")) ? 0 : 1);
//										a.finiTime = po.getString(getHorseCountProperty(count));
//							    	} else {
//							    		int cnt = Integer.parseInt(a.param2);
//							    		if(count == 1){
//							    			if(cnt == 500){
//							    				a.acomplish = (byte)((po.getString(PROPERTY_FINISHTIME_HORSEBOOK500).equals("")) ? 0 : 1);
//												a.finiTime = po.getString(PROPERTY_FINISHTIME_HORSEBOOK500);
//							    			}
//							    		}
//							    	}
//							    }
//							    break;
//						  case 3:
//							  for(Achievement a:achs){
//								  if(a.param1.equals("locksill")){//锁定坐骑技能
//									  if(person.id>0 && lockSkill(person)){
//										  po.setString(PROPERTY_FINISHTIME_LOCKHORSESKILL,getFinishTime(System.currentTimeMillis()));
//									  }
//									  a.acomplish = (byte)((po.getString(PROPERTY_FINISHTIME_LOCKHORSESKILL).equals("")) ? 0 : 1);
//									  a.finiTime = po.getString(PROPERTY_FINISHTIME_LOCKHORSESKILL);
//								  } else if(a.param1.equals("skilllevel")){//坐骑技能等级
//									  int level = Integer.parseInt(a.param2);
//									  if(person.id>0){
//										  if(person.horseBag.horses!=null && person.horseBag.horses.size()>0){
//											  for(Horse h : person.horseBag.horses){
//												  if(checkHorseSkill(h,level)){
//													  po.setString(getPropertyOfHorseSkillLevel(level),getFinishTime(System.currentTimeMillis()));
//												  }
//											  }
//										  }
//									  }
//									  a.acomplish = (byte)((po.getString(getPropertyOfHorseSkillLevel(level)).equals("")) ? 0 : 1);
//									  a.finiTime = po.getString(getPropertyOfHorseSkillLevel(level));
//								  }
//							  }
//							  break;
//						  }
//						  break;
//					case 8:
//							switch(index){
//							case 0:
//								  for(Achievement a:achs){
//									  int num = Integer.parseInt(a.param1);
//									  a.acomplish = (byte) ((po.getString(getPropertyOfIMoney(num,true)).equals("")) ? 0 : 1);
//									  a.finiTime = po.getString(getPropertyOfIMoney(num,true));
//								  }
//								   break;
//							}
//						    break;
//						    
//					case 9:
//						   switch(index){
//						   case 0:
//							      int practice = person.pool.getInt(Player.PROPERTY_PRODUCE_ABILITY);
//								  int practiceLevel = ProduceService.getPracticeLevel(person.level, practice);
//							      for(Achievement a:achs){
//							    	 int level = Integer.parseInt(a.param1);
//							    	 if(person.id>0){
//							    	    if(practiceLevel>=level && po.getString(getProduceProperty(level,-1,-1,-1)).equals("")){
//							    	    	po.setString(getProduceProperty(level,-1,-1,-1), getFinishTime(System.currentTimeMillis()));
//							    	    }
//							    	 }
//							    	 a.acomplish = (byte)((po.getString(getProduceProperty(level,-1,-1,-1)).equals("")) ? 0 : 1);
//									 a.finiTime = po.getString(getProduceProperty(level,-1,-1,-1));
//							      }
//							      break;
//						   case 1:
//							      for(Achievement a:achs){
//							    	  if(a.param2.equals("")){
//							    		  int oType = Integer.parseInt(a.param1);
//							    		  a.acomplish = (byte)((po.getString(getProduceProperty(-1,-1,oType,-1)).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(getProduceProperty(-1,-1,oType,-1));
//							    	  }else{
//							    		  int oType = Integer.parseInt(a.param1);
//							    		  int forLevel = Integer.parseInt(a.param2);
//							    		  a.acomplish = (byte)((po.getString(getProduceProperty(-1,forLevel,oType,-1)).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(getProduceProperty(-1,forLevel,oType,-1));
//							    	  }
//							      }
//							      break;
//						   case 2:
//							      for(Achievement a:achs){
//							    	  int count = Integer.parseInt(a.param1);
//							    	  a.acomplish = (byte)((po.getString(getProduceProperty(-1,-1,-1,count)).equals("")) ? 0 : 1);
//									  a.finiTime = po.getString(getProduceProperty(-1,-1,-1,count));
//							      }
//							      break;
//						   case 3:
//							      for(Achievement a:achs){
//							    	  String type = a.param1;
//							    	  if(type.equals("study")){//学习500打造技能书成就
//							    		  if(person.id>0 && po.getString(PROPERTY_FINISHTIME_STUDYFORMULARBOOK).equals("")){
//							    			  int count = person.formulaList.getFormulaCount();
//							    			  int totleCount = Integer.parseInt(a.param2);
//							    			  if(count>=totleCount){
//							    				  po.setString(PROPERTY_FINISHTIME_STUDYFORMULARBOOK,getFinishTime(System.currentTimeMillis()));
//							    			  }
//							    		  }
//								    	  a.acomplish = (byte)((po.getString(PROPERTY_FINISHTIME_STUDYFORMULARBOOK).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(PROPERTY_FINISHTIME_STUDYFORMULARBOOK);
//							    	  } else if(type.equals("dazao")){
//							    		  a.acomplish = (byte)((po.getString(PROPERTY_FINISHTIME_PRODUCEDONGZHUOLING).equals("")) ? 0 : 1);
//										  a.finiTime = po.getString(PROPERTY_FINISHTIME_PRODUCEDONGZHUOLING);
//							    	  }
//							      }  
//						   }
//						   break;
//					case 10:
//						    processEquipCount(person,1,true,false);
//						    switch(index){
//						    case 0:
//						    	   for(Achievement a:achs){
//						    		  int quality = Integer.parseInt(a.param2);
//						    		  a.acomplish = (byte)((po.getString(getEquipmentProperty1(quality,-1,-1)).equals("")) ? 0 : 1);
//									  a.finiTime = po.getString(getEquipmentProperty1(quality,-1,-1));
//						    	   }
//						    	   break;
//						    case 1:
//						    	   for(Achievement a:achs){
//						    		   int type = Integer.parseInt(a.param1);
//						    		   if(type == 1){
//						    			   a.acomplish = (byte)((po.getString(PROPERTY_EQUIP_ADDHOLE).equals("")) ? 0 : 1);
//						   				   a.finiTime = po.getString(PROPERTY_EQUIP_ADDHOLE);
//						    		   }else if(type == 2){
//						    			   a.acomplish = (byte)((po.getString(PROPERTY_MERGE_JEWEL).equals("")) ? 0 : 1);
//						   				   a.finiTime = po.getString(PROPERTY_MERGE_JEWEL);
//						    		   }else if(type == 3){
//						    			   a.acomplish = (byte)((po.getString(PROPERTY_EQUIP_ADDJEWEL).equals("")) ? 0 : 1);
//						   				   a.finiTime = po.getString(PROPERTY_EQUIP_ADDJEWEL);
//						    		   }else if(type == 4){
//						    			   a.acomplish = (byte)((po.getString(PROPERTY_EQUIP_REMOVEJEWEL).equals("")) ? 0 : 1);
//						   				   a.finiTime = po.getString(PROPERTY_EQUIP_REMOVEJEWEL);
//						    		   }
//						    	   }
//						    	   break;
//						    case 2:
//						    	   for(Achievement a:achs){
//						    		   if(!a.param1.equals("")){
//						    		      int count = Integer.parseInt(a.param1);
//						    		      a.acomplish = (byte)((po.getString(getEquipmentProperty1(-1,count,-1)).equals("")) ? 0 : 1);
//						  				  a.finiTime = po.getString(getEquipmentProperty1(-1,count,-1));
//						    		   } 
//						    	   }
//						    	   break;
//						    case 3:
//						    	   if(person.id>0){
//							    	   PvpInfo pvpInfo = getPvpInfo(person.id,person.faction);
//					    			   checkHorseEquip(person,pvpInfo,false);
//						    	   }
//						    	   for(Achievement a:achs){
//						    		   int t = Integer.parseInt(a.param1);
//						    		   int level = Integer.parseInt(a.param2);
//						    		   if(t == 0){
//						    			   a.acomplish = (byte)((po.getString(getEquipmentProperty2(level,-1)).equals("")) ? 0 : 1);
//						   				   a.finiTime = po.getString(getEquipmentProperty2(level,-1));
//						    		   }else if(t==1){
//						    			   a.acomplish = (byte)((po.getString(getEquipmentProperty2(-1,level)).equals("")) ? 0 : 1);
//						   				   a.finiTime = po.getString(getEquipmentProperty2(-1,level));
//						    		   } else if(t==2){//坐骑星级
//						    			   a.acomplish = (byte)((po.getString(getHorseEquipmentStar(level)).equals("")) ? 0 : 1);
//						   				   a.finiTime = po.getString(getHorseEquipmentStar(level));
//						    		   }
//						    	   }
//						    	   break;
//						    }
//						    break;
//					case 11:
//						    switch(index){
//						    case 0:
//						    	   for(Achievement a:achs){
//						    		   int count = Integer.parseInt(a.param1);
//						    		   if(person.id > 0){
//							   				int titleCount = person.titles.titles.size();
//						   					if(titleCount >= count && po.getString(getTitleCountProperty(count)).equals("")){
//						   						po.setString(getTitleCountProperty(count), getFinishTime(System.currentTimeMillis()));
//						   					}
//						    		   }
//						    		   a.acomplish = (byte)((po.getString(getTitleCountProperty(count)).equals("")) ? 0 : 1);
//									   a.finiTime = po.getString(getTitleCountProperty(count));
//						    	   }
//						    	   break;
//						    }
//						    break;
//					case 12:
//						   switch(index){
//						   case 0:
//							      for(Achievement a:achs){
//							    	  int count = Integer.parseInt(a.param1);
//							    	  if(person.id>0){
//											int cardCount = person.pool.getInt(CardService.PROPERTY_HAVECARD, 0);
//											if(cardCount >= count && po.getString(getCardCollectProperty(count,-1)).equals("")){
//												po.setString(getCardCollectProperty(count,-1), getFinishTime(System.currentTimeMillis()));
//											}
//							    	  }
//							    	  a.acomplish = (byte)((po.getString(getCardCollectProperty(count,-1)).equals("")) ? 0 : 1);
//									  a.finiTime = po.getString(getCardCollectProperty(count,-1));
//							      }
//							      break;
//						   case 1:
//							      for(Achievement a:achs){
//							    	  if(!a.param1.equals("")){
//								    	  int groupId = Integer.parseInt(a.param1);
//								    	  if(person.id>0){
//												CardService cardService = Server.server.getServiceRegistry().getCardService();
//												CardGroup group = cardService.getCardGroup(groupId);
//												if(person.pool.getInt(cardService.getPropertyOfPlayerSuit(groupId), 0) == group.cards.size() && po.getString(getCardCollectProperty(-1,groupId)) == ""){
//													po.setString(getCardCollectProperty(-1,groupId), getFinishTime(System.currentTimeMillis()));
//												}
//								    	   }
//								    	   a.acomplish = (byte)((po.getString(getCardCollectProperty(-1,groupId)).equals("")) ? 0 : 1);
//										   a.finiTime = po.getString(getCardCollectProperty(-1,groupId));
//							    	  } 
//							      }
//							      break;
//						   }
//						    break;
//					case 13:
//						    switch(index){
//						    case 0: //击杀怪物
//						    	   for(Achievement a:achs){
//						    		   int count = Integer.parseInt(a.param1);
//						    		   a.acomplish = (byte)((po.getString(getKillCreatureProperty(count)).equals("")) ? 0 : 1);
//									   a.finiTime = po.getString(getKillCreatureProperty(count));
//						    	   }
//						    	   break;
//						    }
//						    break;
//					case 14:
//						    switch(index){
//						    case 0:
//						    	   for(Achievement a:achs){
//						    		   int type = Integer.parseInt(a.param1);
//						    		   int count = Integer.parseInt(a.param2);
//						    		   a.acomplish = (byte) ((po.getString(getPropertyOfFinishQuest(type,count)).equals("")) ? 0 : 1);
//									   a.finiTime = po.getString(getPropertyOfFinishQuest(type,count));
//						    	   }
//						    	   break;
//						    case 1:
//						    	   for(Achievement a:achs){
//						    		   int type = Integer.parseInt(a.param1);
//						    		   a.acomplish = (byte) ((po.getString(getPropertyOfFinishQuest(type,1)).equals("")) ? 0 : 1);
//									   a.finiTime = po.getString(getPropertyOfFinishQuest(type,1));
//						    	   }
//						    	   break;
//						    }
//						    break;
//					case 15:
//							switch(index){
//						    case 0: //坐骑合成
//						    	   int count=0;
//						    	   if(person.id>0)
//						    	       count = checkHorseMerge(person);
//						    	   for(Achievement a:achs){
//						    		   if(a.param1.equals("merge")){
//						    			   int level = Integer.parseInt(a.param2);
//						    			   if(person.id>0){
//						    				   if(count>=level && po.getString(getPropertyOfHorseMergeLevel(level)).equals("")){
//						    					   po.setString(getPropertyOfHorseMergeLevel(level),getFinishTime(System.currentTimeMillis()));
//						    				   }
//						    			   }
//							    		   a.acomplish = (byte) ((po.getString(getPropertyOfHorseMergeLevel(level)).equals("")) ? 0 : 1);
//						   				   a.finiTime = po.getString(getPropertyOfHorseMergeLevel(level));
//						    		   }
//						    	   }
//						    	   break;
//							}
//						    break;
//					case 16:
//						    switch(index){
//						    case 0://坐骑幻化
//						    	int count = 0;
//						    	 if(person.id>0)
//						    		 count = checkHorseChange(person);
//						    	 for(Achievement a:achs){
//						    		   if(a.param1.equals("change")){
//						    			   int level = Integer.parseInt(a.param2);
//						    			   if(person.id>0){
//						    				   if(count>=level && po.getString(getPropertyOfHorseChange(level)).equals("")){
//						    					   po.setString(getPropertyOfHorseChange(level),getFinishTime(System.currentTimeMillis()));
//						    				   }
//						    			   }
//							    		   a.acomplish = (byte) ((po.getString(getPropertyOfHorseChange(level)).equals("")) ? 0 : 1);
//						   				   a.finiTime = po.getString(getPropertyOfHorseChange(level));
//						    		   }
//						    	   }
//						    	  break;
//						    }
//						    break;
//					case 17:
//					    switch(index){
//					    case 0:
//					    	   for(Achievement a:achs){
//					    		   int type = Integer.parseInt(a.param1);
//					    		   if(type == 1){
//					    			   if(person.id>0){
//					    				   Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(person.faction);
//						   				   if(nation.getKingId()!=-1 && nation.getKingId() == person.id && po.getString(PROPERTY_CANDIDATE_KING).equals("")){
//						   					  po.setString(PROPERTY_CANDIDATE_KING, getFinishTime(System.currentTimeMillis()));
//						   				   }
//					    			   }
//					    			   a.acomplish = (byte) ((po.getString(PROPERTY_CANDIDATE_KING).equals("")) ? 0 : 1);
//					   				   a.finiTime = po.getString(PROPERTY_CANDIDATE_KING);
//					    		   }else if(type == 2){
//					    			   a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_NATIONQUEST).equals("")) ? 0 : 1);
//					   				   a.finiTime = po.getString(PROPERTY_FINISHTIME_NATIONQUEST);
//					    		   }else if(type == 3){
//					    			   a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_TONGQUEST).equals("")) ? 0 : 1);
//					   				   a.finiTime = po.getString(PROPERTY_FINISHTIME_TONGQUEST);
//					    		   }else if(type == 4){
//					    			   a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_BIGBOX).equals("")) ? 0 : 1);
//					   				   a.finiTime = po.getString(PROPERTY_FINISHTIME_BIGBOX);
//					    		   }else if(type == 5){
//					    			   a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_SMALLBOX).equals("")) ? 0 : 1);
//					   				   a.finiTime = po.getString(PROPERTY_FINISHTIME_SMALLBOX);
//					    		   }else if(type == 6){
//					    			   a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_EXPANSIONBATTLE).equals("")) ? 0 : 1);
//					   				   a.finiTime = po.getString(PROPERTY_FINISHTIME_EXPANSIONBATTLE);
//					    		   }else if(type == 7){
//					    			   a.acomplish = (byte) ((po.getString(PROPERTY_CANDIDATE_KING_AGAIN).equals("")) ? 0 : 1);
//					   				   a.finiTime = po.getString(PROPERTY_CANDIDATE_KING_AGAIN);
//					    		   }else if(type == 8){
//					    			   a.acomplish = (byte) ((po.getString(PROPERTY_FINISHTIME_CREATETONG).equals("")) ? 0 : 1);
//					   				   a.finiTime = po.getString(PROPERTY_FINISHTIME_CREATETONG);
//					    		   }else if(type == 9){
//					    			   int lev = Integer.parseInt(a.param2);
//					    			   if(person.id>0){
//					    				   int level = person.pool.getInt(CycleInstanceMapManager.propertyOfCycleLevel, 0);
//					    				   if(level >= lev && po.getString(getPropertyOfCycleInstance(lev)).equals("")){
//					   						po.setString(getPropertyOfCycleInstance(lev), getFinishTime(System.currentTimeMillis()));
//											setMessage(person, a, false,true);
//					    				   }
//					    			   }
//					    			   a.acomplish = (byte) ((po.getString(getPropertyOfCycleInstance(lev)).equals("")) ? 0 : 1);
//					   				   a.finiTime = po.getString(getPropertyOfCycleInstance(lev));
//					    		   }
//					    	   }
//					    	   break;
//					    case 1:
//					    	   for(Achievement a:achs){
//					    		   int type = Integer.parseInt(a.param1);
//					    		   int count = Integer.parseInt(a.param2);
//					    		   if(type == 1){
//					    			   a.acomplish = (byte) ((po.getString(getPropertyOfPkWin(count)).equals("")) ? 0 : 1);
//					   				   a.finiTime = po.getString(getPropertyOfPkWin(count));
//					    		   }
//					    	   }
//					    	   break;
//					    case 2:
//					    	   for(Achievement a:achs){
//					    		   int count = Integer.parseInt(a.param1);
//					    		   a.acomplish = (byte) ((po.getString(getPropertyOfFinishAchieve(count)).equals("")) ? 0 : 1);
//				   				   a.finiTime = po.getString(getPropertyOfFinishAchieve(count));
//					    	   }
//					    	   break;
//					    }
//					    break;
//				    }  
//				}
//			}
//		}
//	}
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
		return p2.getacomplist().compareTo(p1.getacomplist());
//		return p1.getacomplist().compareTo(p2.getacomplist());
//		if (p1.acomplish < p2.acomplish)
//			return 1;
//		
//		else
//			return -1;
	}
}

class IdSort implements Comparator {
	public int compare(Object o1, Object o2) {
		Achievement p1 = (Achievement) o1;
		Achievement p2 = (Achievement) o2;
		if (p1.achieveId < p2.achieveId)
			return -1;
		else 
			return 1;
	}
}

