package peony.game.stepserver;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;
import peony.channel.ChannelService;
import peony.game.Actor;
import peony.game.CreatureDieCallback;
import peony.game.DayListener;
import peony.game.DieCallback;
import peony.game.GameMapDefinition;
import peony.game.GameObject;
import peony.game.Horse;
import peony.game.MoveCallback;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Skills;
import peony.game.Time;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.game.attendant.Attendant;
import peony.game.attendant.AttendantFixService;
import peony.game.nation.CandidateService;
import peony.net.DispatchClientSessionService;
import peony.net.DispatchPacket;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.util.TimeUtil;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.OperationStatus;

/**
 * 跨服战场服务
 * @author dchen
 */
public class StepBattleService implements Service, VMapManager, DayListener {
	
	//普通跨服战(老跨服战场)
	private static final Logger log = Logger.getLogger(StepBattleService.class);
	public static int mapId = 2208; //战场场景ID
	public static int minPlayerLevel = 65; //允许报名跨服战场的最低玩家级别
	public static int minEnterPlayers = 7; //每场参战人数
	public static int startHour = 12; //报名开始时间-小时
	public static int startMin = 30; //报名开始时间-分钟
	public static int endHour = 13; //报名结束时间-小时
	public static int endMin = 0; //报名结束时间-分钟
	public static int instanceEndHour = 13; //战场结束时间-小时
	public static int instanceEndMin = 30; //战场结束时间-分钟
	public static int chatStartHour = 12; //报名开始公告时间-小时
	public static int chatStartMin = 25; //报名开始公告时间-分钟
	
	//战场队伍(除方士之外)
	public Queue<Integer> queues = new LinkedList<Integer>();
	//方士专用
	public Queue<Integer> queues_Fangshi = new LinkedList<Integer>();
	
	//战场所有开启的副本
	public List<StepBattleInstance> instances = new ArrayList<StepBattleInstance>();
	//玩家对应的副本
	public Map<Integer, StepBattleInstance> player2Instance = new HashMap<Integer, StepBattleInstance>();
	
	public int lastCheckTime = 0; //上一次check时间
	public boolean canSignUp = false; //是否允许报名
	
	public StepBattleDieCallBack stepBattleDieCallBack = new StepBattleDieCallBack(); //死亡回调
	
	public List<Actor> winners = new ArrayList<Actor>(); //战胜者列表
	public List<Integer> winnerScore = new ArrayList<Integer>(); //战胜者列表
	public StepBattleMoveCallBack moveCallBack = new StepBattleMoveCallBack(); //移动回调
	public List<Integer> todaySigns = new ArrayList<Integer>();
	
	
	//常规赛16强
	
	
	public static int delayTime=10*60*1000;//10分钟
	
	//活动时间修改为17:00-22：00
	public static int minEnterPlayers_16 = 2; //常规赛参战人数
	/**每天报名开始时间*/
	public static int sign_everyday_StartHour=17;
	public static int sign_everyday_StartMin=00;
	
	/**每天报名结束时间*/
	public int sign_everyday_EndHour=22;
	public int sign_everyday_EndMin=0;
	
	/**每天跨服战结束的时间*/
	public int endPvpBattle_everyday_Hour=22;
	public int endPvpBattle_everyday_Min=15;
	
	/**每天跨服数据存档时间*/
	public int endPvpBattle_persist_Hour=22;
	public int endPvpBattle_persist_Min=20;
	
	public boolean hasPersisData = true; //是否已经存储过
	public boolean hasSendGift = true; //是否已经发送过奖励
	
	/**常规赛数据清除，第三周周日争霸赛结束后*/
	public int routineDataClear_Hour=23;
	public int routineDataClear_Min=0;
	public boolean hadClearRoutineData=false;//是否已经清除过常规赛数据
	
	/**每天查看内存中玩家的数据信息*/
	public int viewMemoryData_Hour=22;
	public int viewMemoryData_Min=13;
	
	
	
	public static int chatStartHour_PVP = 16; //报名开始公告时间-小时
	public static int chatStartMin_PVP = 55; //报名开始公告时间-分钟
	
	/**每天报名次数，第二天清除*/
	public static Map<Integer,Integer> todaySignedTimes=new HashMap<Integer,Integer>();//当天玩家报名的次数
	
	/**每天报名最多次数*/
	public static int MAXSIGNEDTIMES=3;//每天最多报名跨服战3次
	
	//战场队伍
	public Queue<Integer> queues_EveryDay = new LinkedList<Integer>();
	
	public int currMinorFaction = 0; //当前分配的minorFaction
	public int lastCheckTime_EveryDay = 0; //上一次check时间
	public boolean canSignUp_EveryDay = false; //是否允许报名
	
	public List<Integer> todaySigns_EveryDay = new ArrayList<Integer>();//每天报名次数，超过3次不让报名
	public List<Integer> players = new ArrayList<Integer>();//所有报名玩家
	public Map<Integer, Integer> playerScore = new HashMap<Integer, Integer>();//玩家获胜场数
	public Map<Integer, Integer> playerTimer = new HashMap<Integer, Integer>();//玩家获胜总时间(每次获胜后累加)
	public Map<Long, StepBattleScore> scores = new HashMap<Long, StepBattleScore>(); //玩家战斗分数缓存,报名后从数据库中获取
	public StepBattleScore[] top16 = new StepBattleScore[16]; //排行榜前16
	
	
	/**3轮结束时的提示,为了与之前的前7名区别，从100开始*/
	public static String STEPBATTLE_HINT_MORETHEN3ROUNDS="100";
	/**发送奖励*/
	public static String STEPBATTLEEND_SENDGIFT="101";	
	
	//争霸赛
	public List<StepBattleScoreTop16> finalsPlayers=new ArrayList<StepBattleScoreTop16>();//争霸赛玩家分数及信息

	public Queue<Integer> queues_Finals = new LinkedList<Integer>();//争霸赛报名
	
	public List<Integer> finalsPlayers16=new ArrayList<Integer>();//16强名单(数据库中取出)
	
	public List<StepBattleInstanceFinals> instancesFinal = new ArrayList<StepBattleInstanceFinals>();
	
	/**是否进入了副本*/
	public List<String> todayEnteredInstancePlayers = new ArrayList<String>();
	
	/**没有参加比赛的玩家*/
	public List<String> notEnteredInstancePlayers = new ArrayList<String>();
	
	/**争霸赛玩家报名信息*/
	public List<Integer> todaySigns_Finlas = new ArrayList<Integer>();
	
	/**比赛过程中等待下一轮或轮空时掉线玩家*/
	public List<String> disConnetionPlayers=new ArrayList<String>();
	
	/**等待冷却时间*/
	public int waitCoolDownTime=5*60*1000;
	/**最后一次分配副本时间*/
	public int lastEnterFinalsBattleTime;
	
	public static int finalMaxPlayers=2;//每场最多的人数
	
	
	public static boolean allInstanceEnd=false;//所有副本都已经结束
	
	public boolean canSignUp_Finals = false; //是否允许争霸赛报名
	
	
	public boolean chatFinalsBattle=false;//世界公告
//
//	
	public static int signFinals_Hour=18;//报名开始时间
	public static int signFInals_Min=50;
	
	public static int signFinals_EndHour=19;//报名结束时间,不报名视为放弃
	public static int signFinals_EndMin=0;
	
	public static int chatHour_Finals=18;//公告时间,同时弹出提示（报名，放弃）
	public static int chatMin_Finals=50;
	public static boolean hadSendChat=false;//是否已经发送过公告(每三周发一次)
	
	public static int maxBetCoins=1000000;//最大押注数(累加100万)
	
	public static float returnBetPercentage=0.9f;//返还金币百分比
	
	
	/**没有玩家报名参加比赛*/
	public static boolean noPlayerEnterBattle=false;
	
	public static boolean firstCheckQueuesFinals=true;//第一次检测报名
	
	
	public static int[] titleIds_Finals = {181,182,183};//争霸赛称号
	public static int[] titleIds_Top16 = {175,176,178};//常规赛称号
	
	//争霸赛称号
	public int RANKING_FINALS_1=0;
	public int RANKING_FINALS_2=1;
	public int RANKING_FINALS_OTHER=2;
	//16强
	public int RANKING_TOP16_1=0;
	public int RANKING_TOP16_2=1;
	public int RANKING_TOP16_OTHER=2;
	
	
	public static Map<Integer, Integer> titleItem = new HashMap<Integer, Integer>();
	
	protected Random  rd=new Random();
	
	
	//跨服共用部分(出生点，传出位置，）
	/**
	 * 跨服类型
	 * 0普通跨服，1常规16强赛，2争霸赛
	 * */
	public static int StepInstanceType;
	//进入战场出生点
	public static int[][] initPosition = new int[][]{
		{447,167},{200,240},{80,395},{180,542},{478,660},{715,593},{824,426},{740,277}
	}; 
	//登录时如果在战场里面的传出位置
	public static int[][] outPosition = new int[][]{{848,300,300},{848,300,300},{848,300,300}};
	public static Map<String, String> serverNames = new HashMap<String, String>();
	public static Map<String, String> serverNamesOfTwaiwan = new HashMap<String, String>();
	
	public static int currentWeek=1;
	public int currentFaction = 1;
	
	public Random ran=new Random();
	
	public static int rewards_16=4640;//常规奖励
	public static int rewards_16_End=2708;//常规赛结束奖励
	
	public HashMap<Integer, Integer> enterIndtanceRecord = new HashMap<Integer, Integer>();
	public HashMap<Integer, StepBattleInstanceFinals> currentInstances = new HashMap<Integer, StepBattleInstanceFinals>();
	public HashMap<Integer, StepBattleInstanceFinals> futureEnterInstances = new HashMap<Integer, StepBattleInstanceFinals>();

	static{
		serverNames.put("sanguo_01", "群雄争霸");
		serverNames.put("sanguo_02", "逐鹿中原");
		serverNames.put("sanguo_03", "问鼎天下");
		serverNames.put("sanguo_04", "血战河东");
		serverNames.put("sanguo_05", "纵横九州");
		serverNames.put("sanguo_06", "赤壁怀古");
		serverNames.put("sanguo_07", "金戈铁马");
		serverNames.put("sanguo_08", "长坂坡(华东)");
		serverNames.put("sanguo_09", "华容道(华东)");
		serverNames.put("sanguo_10", "青梅煮酒");
		serverNames.put("sanguo_11", "草船借箭");
		serverNames.put("sanguo_12", "桃园结义");
		serverNames.put("sanguo_13", "火烧连营");
		serverNames.put("sanguo_14", "霸王降世");
		serverNames.put("sanguo_15", "义结金兰");
		serverNames.put("sanguo_16", "单骑救主");
		serverNames.put("sanguo_17", "御驾亲征");
		serverNames.put("sanguo_18", "十八诸侯");
		serverNames.put("sanguo_19", "一骑当千");
		serverNames.put("sanguo_20", "卧虎藏龙");
		serverNames.put("sanguo_21", "烽烟再起");
		serverNames.put("sanguo_22", "绝世无双");
		serverNames.put("sanguo_23", "南征北战");
		serverNames.put("sanguo_24", "天下纷争");
		serverNames.put("sanguo_25", "势如破竹");
		serverNames.put("sanguo_26", "乱世枭雄");
		serverNames.put("sanguo_27", "横扫千军");
		serverNames.put("sanguo_28", "叱咤风云");
		serverNames.put("sanguo_29", "血战沙场");
		serverNames.put("sanguo_30", "威震八方");
		serverNames.put("sanguo_31", "天命之战");
		serverNames.put("sanguo_32", "对酒当歌");
		serverNames.put("sanguo_33", "烽火连天");
		serverNames.put("sanguo_34", "雷霆万钧");
		serverNames.put("sanguo_35", "众志成城");
		serverNames.put("sanguo_36", "国士无双");
		serverNames.put("sanguo_37", "忠肝义胆");
		serverNames.put("sanguo_38", "威凌天下");
		serverNames.put("sanguo_39", "雷动九天");
		serverNames.put("sanguo_40", "九龙诛心");
		serverNames.put("sanguo_41", "嗜血无痕");
		serverNames.put("sanguo_42", "战鼓雷鸣");
		serverNames.put("sanguo_43", "过关斩将");
		serverNames.put("sanguo_44", "锦绣帅旗");
		serverNames.put("sanguo_45", "君临天下");
		serverNames.put("sanguo_46", "风云再起");
		serverNames.put("sanguo_47", "单刀赴会");
		serverNames.put("sanguo_48", "一战成名");
		
		serverNamesOfTwaiwan.put("sanguo_01", "群雄爭霸");
		serverNamesOfTwaiwan.put("sanguo_02", "天命之戰");
		
		titleItem.put(181, 4642);
		titleItem.put(182, 4643);
		titleItem.put(183, 4644);
		titleItem.put(175, 4600);
		titleItem.put(176, 4601);
		titleItem.put(178, 4603);
		
	}
	
	public void startup() throws Exception {
		Server.server.getWorld().addVMapManager(this);
		Server.server.getWorld().registerVMapManager(mapId, this);
		Time.addDayListener(this);
		if(!Server.isStepServer && (Server.server.revision.equals(Server.REVISION_TYPE_PIP) 
				|| Server.server.revision.equals(Server.REVISION_TYPE_TW)) && !Server.isAppSection){
			Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
				public void run() {
							Server.server.getServiceRegistry().getChatService()
							.sendWorldMessage("跨服战场将在5分钟后开启,请广大英雄通过主城内【郭大师】报名参加,一战成名机不可失.");
				}
			}, TimeUtil.getScheduleTimeMills(new Date(), chatStartHour, chatStartMin), 24*60*60*1000l, TimeUnit.MILLISECONDS);
		}
		if(Server.isStepServer){
			Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
				public void run() {
					if(currentWeek==1||currentWeek==2){
						log.info("[VIEWMEMORYDATASTART]");
						for(StepBattleScore sbs:scores.values()){
							log.info("[VIEWMEMORYDATA_ROUTINPLAYERSSCORES]PLAYERACC["+sbs.accountId+"]PLAYERID["+sbs.playerid+"]PLAYERNAME["+sbs.name+"]GAMECODE["+sbs.gameCode+"]WINCOUNT["+sbs.winCount+"]TIMER["+sbs.time+"]");
						}
						log.info("[VIEWMEMORYDATAEND]");
					}
				}
			}, TimeUtil.getScheduleTimeMills(new Date(), viewMemoryData_Hour, viewMemoryData_Min), 24*60*60*1000l, TimeUnit.MILLISECONDS);
		}
		if(Server.isStepServer){
//			modify();
			loadTopList();
			loadStepBattleBDB();
			loadFinalsPlayers();
			log.info("[STEPBATTLESTARTUP_CURRENTWEEK]"+currentWeek);
		}
	}
	
//	public static String[] score={
//		"1762249,sanguo_22,100000,2343266,15745033",
//		"1059681,sanguo_33,500000,2343266,15745033",
//		"3103788,sanguo_22,300000,2343266,15745033",
//	
//		
//		"1762249,sanguo_22,100000,2449490,9205438",	
//		"1724499,sanguo_31,100000,2449490,9205438",	
//		"1059681,sanguo_33,300000,2449490,9205438",	
//		"3103788,sanguo_22,200000,2449490,9205438",	
//		
//		"1762249,sanguo_22,100000,1866338,9886897",	
//		"6525656,sanguo_15,100000,1866338,9886897",	
//		"1059681,sanguo_33,600000,1866338,9886897",	
//		"1724499,sanguo_31,200000,1866338,9886897",	
//		"3775025,sanguo_15,200000,1866338,9886897",
//		
//		"1762249,sanguo_22,100000,4736705,10078239",
//		"1059681,sanguo_33,1000000,4736705,10078239",
//		
//		"544314,sanguo_40,100000,6404227,20972405",	
//		"6525656,sanguo_15,100000,6404227,20972405",	
//		"1059681,sanguo_33,800000,6404227,20972405",	
//		"201648,sanguo_22,1000000,6404227,20972405",	
//		
//		"16244668,sanguo_07,100000,16244668,16053073",	
//		
//};
//	
//	protected void modify(){
//		StepBattleScore_FinalsDao dao=Server.server.getServiceRegistry().getDbService().stepbattlescore_FinalsDAO;
//		List<StepBattleScoreTop16> finalsPlayers=new ArrayList<StepBattleScoreTop16>();
//		finalsPlayers = dao.getTop16Players();
//		
//		for(StepBattleScoreTop16 sbs16:finalsPlayers){
//			for(int i=0;i<score.length;i++){
//				String[] playerScoreInfo=score[i].split(",");
//				int targetPlayerId=Integer.parseInt(playerScoreInfo[3]);
//				int targetPlayerAcc=Integer.parseInt(playerScoreInfo[4]);
//				int betPlayaerId=Integer.parseInt(playerScoreInfo[0]);
//				String betPlayerGameCode=playerScoreInfo[1];
//				int betCoins=Integer.parseInt(playerScoreInfo[2]);
//				if(sbs16.playerid==targetPlayerId&&sbs16.accountId==targetPlayerAcc){
//					sbs16.addBet(betPlayaerId, betPlayerGameCode, betCoins);
//				}
//			}
//		}
//		
//		
//		for(StepBattleScoreTop16 sbs : finalsPlayers){
//			if(sbs==null)
//				continue;
//			StepBattleScoreTop16 score = dao.getPlayerStepBattleScoreInfo(sbs.playerid, sbs.accountId);
//			if(score==null){
//				dao.newEntity(sbs);
//			}else{
//				dao.updateEntity(sbs);
//			}
//		}
//	}
	
	/** 补发胜场数和用时 */
//	public void modify(){
//		int[] arr = {186392,4,1668141,1,1829106,2,1566760,6,462487,2,9769720,1,151982,6,4360,2,673930,2,3233018,1,8796,3,17538,3,16372100,4,3770467,2,361278,4,17566,2,9413928,4,3016436,5,1295078,2,504884,1,8706,2,16120706,6,1066430,3,424874,2,1356676,5,533091,3,147263,3,2184537,6,3436010,5,75870,6,485554,2,1068536,4,3393727,3,332796,5,1768267,3,10805,3,1427040,3,15960202,5,2142,1,352194,2,214,4,13247,1,757687,1,935945,6,5268312,5,1044712,4,220106,2,1995464,4,750011,3,539231,6,9732545,3,483730,5,6633076,5,499702,1,16321787,1,539142,3,1107913,3,650472,3,1010140,5,1703134,5,1699315,3,10145797,3,585264,5,72205,3,1222534,6,1228335,1,1570948,1,5378517,5,5417780,5,16187442,4,2150133,2,233071,3,936284,5,1172780,4,440255,2,2182584,6,682782,1,567978,5,449,4,396609,4,444568,1,58967,5,2628289,3,879915,2,964357,3,9771116,4,4033110,3,132675,3,362974,3,724084,1,15916475,5,320971,4,16359340,4,6574474,5,8284,6,167891,5,384013,4,186894,6,5384589,1,16299836,6,2809,5,604953,4,102509,1,16244668,6,1329098,1,3955719,3,501707,1,5056048,2,1633307,2,371491,3,1849533,5,676753,2,2827316,2,399255,6,492928,1,1332781,5,461848,3,4364864,6,1814234,6,1358195,3,2680,4,10278720,4,860774,2,753,6,470573,2,546737,5,483943,4,1400014,4,4282474,1,990223,1,750196,3,5636622,3,4447495,1,2946906,6,5254374,1,62861,5,1424041,1,1699741,2,1214334,3,1125052,2,16213076,3,880562,6,284379,2,1684113,3,173133,5,1294789,5,1122845,4,4244118,2,203963,3,1088015,1,5636755,3,1181398,3,510184,4,301914,1,245800,4,165597,3,3557600,2,10207519,4,1662291,6,31873,2,1012470,1,2286122,3,776538,6,492786,3,9577589,3,10057564,5,9228023,4,95855,3,974,4,641951,1,2914735,1,983696,6,1598251,6,496177,4,461443,3,3719744,2,302303,5,3789209,5,1003008,3,16179395,3,670711,6,381506,3,201648,6,16292676,4,9586131,3,159416,2,81170,4,2718550,6,10208889,5,3182,2,521312,3,234122,2,14249,4,257533,4,16286343,5,16298436,4,106010,2,75004,3,46821,1,809240,1,1888669,1,857982,3,534458,2,3840534,1,300332,6,10308223,4,1275317,6,534423,6,106484,6,1434017,1,168234,4,735357,5,546945,5,549707,3,408194,5,1370993,3,519220,2,502004,2,2889471,2,4905368,6,140045,2,1282676,6,540285,4,81099,1,16385624,2,586542,5,10272420,4,4835003,5,1009130,1,3103788,2,1512428,6,3539681,5,3260709,5,1190502,3,3701109,6,3813720,5,2378746,3,1507,3,62312,4,333483,5,10160701,4,3442,6,908053,6,144037,5,2413365,5,389473,4,638439,2,3203343,6,131541,6,1487580,6,793811,4,760781,6,1739201,1,2209745,2,559090,6,592973,5,861938,1,1621293,4,1999558,3,654034,3,9370723,6,9751261,3,830816,4,16034419,6,1869443,4,2179746,1,3452552,6,736054,4,1227138,3,942073,2,1026891,3,2222374,3,5949,1,127670,3,403665,5,591025,3,474060,1,79740,6,537904,5,4430858,3,16332815,5,184259,2,1392210,6,714151,4,114973,2,2962773,4,2358459,3,1511633,6,16216292,5,1714650,6,1595962,6,2455975,2,220561,6,13804,2,4736705,6,2449490,6,112380,5,15929310,2,1374248,5,9747566,6,2921862,3,289049,1,2137554,5,4418307,3,924256,4,493820,5,13336,2,16307388,3,16292065,4,623745,4,1370491,1,1743439,4,539726,1,1015444,4,1017882,6,9955121,3,679541,3,681391,3,1379337,6,1221113,2,3300816,2,649807,2,16291965,3,6012613,1,3837961,2,59523,1,413654,5,578496,4,2064135,6,708144,6,392184,5,1756971,2,1194286,2,226882,2};
//		StepBattleScoreDao dao = Server.server.getServiceRegistry().getDbService().stepbattlescoreDAO;
//		for(int i=0;i<arr.length-1;i+=2){
//			int playerId = arr[i];
//			int c = arr[i+1];
//			try {
//				StepBattleScore score = (StepBattleScore) dao.uniqueResult("from StepBattleScore c where c.playerid=?", playerId);
//				if(score!=null){
//					float speed = (score.time/score.winCount);
//					int addTime = (int) (speed * c);
//					if(score.winCount+c>24){
//						score.winCount = 24;
//					}else{
//						score.winCount += c;
//						score.time += addTime;
//					}
//					dao.updateEntity(score);
//					System.out.println("————————"+score.playerid+"	"+score.gameCode+"	"+score.winCount+"	"+score.time);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}
	
	/**给每周16强发送称号*/
	public void sendTitle_16(){
		//再次排序(防止出现问题)
		for(int i=0;i<top16.length;i++){
			for(int j=i+1;j<top16.length;j++){
				if(top16[i]!=null&&top16[j]!=null){
					if(top16[i].winCount<top16[j].winCount||
							(top16[i].winCount==top16[j].winCount&&top16[i].time>top16[j].time)){
						StepBattleScore temp=top16[i];
						top16[i]=top16[j];
						top16[j]=temp;
					}
				}
			}
		}
		//发送称号奖励
		for(int i=0;i<top16.length;i++){
			StepBattleScore sbs=top16[i];
			int index=0;
			if(sbs!=null){
				if((i==0||i==1)){//第一名和第二名称号
					index=i;
					sendTitle(sbs.playerid, sbs.gameCode, titleItem.get(titleIds_Top16[index]),"常规赛奖励");
					log.info("[SENDTITLE16]PLAYERID["+sbs.playerid+"]GAMECODE["+sbs.gameCode+"]RANK01["+i+"]");
				}else{//参加比赛玩家给强者称号
					index=2;
					sendTitle(sbs.playerid, sbs.gameCode, titleItem.get(titleIds_Top16[index]),"常规赛奖励");
					log.info("[SENDTITLE16]PLAYERID["+sbs.playerid+"]GAMECODE["+sbs.gameCode+"]RANK2["+i+"]");
				}
			}
		}
	}
	
	
	/**
	 * 发送称号给玩家
	 * @param playerId
	 * @param gameCode
	 * @param itemId
	 */
	public void sendTitle(int playerId,String gameCode,int itemId,String messageTitle){
		Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
		pt.putInt(StepServer.TYPE_SENDTITLE);
		pt.putString(messageTitle);
		pt.putInt(playerId);
		pt.putUTF(gameCode);
		pt.putInt(itemId);
		DispatchPacket dp = new DispatchPacket(0, pt);
		for(IoSession session : Server.server.getServiceRegistry().getStepServer().sessions){
			session.write(dp);
		}
	}
	
	
	public void loadFinalsPlayers(){//争霸赛玩家
		StepBattleScore_FinalsDao dao = Server.server.getServiceRegistry().getDbService().stepbattlescore_FinalsDAO;
		finalsPlayers = dao.getTop16Players();
		for(int i=0;i<finalsPlayers.size();i++){//按名次排序
			for(int j=i+1;j<finalsPlayers.size();j++){
				StepBattleScoreTop16 sbs1=finalsPlayers.get(i);
				StepBattleScoreTop16 sbs2=finalsPlayers.get(j);
				if(sbs2.getBet()>sbs1.getBet()){
					finalsPlayers.set(i, sbs2);
					finalsPlayers.set(j, sbs1);
					}
				}
		}
	}
	/***
	 * 是否是争霸赛选手
	 * @param playerId
	 * @return
	 */
	public boolean isFinalsPlayer(int playerId,String GameCode){
		if(finalsPlayers!=null){
			for(StepBattleScoreTop16 sbs16:finalsPlayers){
				if(sbs16.playerid==playerId&&sbs16.gameCode.equals(GameCode)){//id和gamecode
					return true;
				}
			}
		}
		return false;
	}
	
	public void loadStepBattleBDB(){
		try {
			Database db = Server.server.getServiceRegistry().getSleepyCatService().stepBattleDB;
			Cursor cursor = null;
            DatabaseEntry keyEntry = new DatabaseEntry();
            DatabaseEntry dataEntry = new DatabaseEntry();
            try {
	            cursor = db.openCursor(null, new CursorConfig());
	            while (cursor.getNext(keyEntry, dataEntry, null) != OperationStatus.NOTFOUND) {
	            	StringBinding.entryToString(keyEntry);
	            	int value = IntegerBinding.entryToInt(dataEntry);
	            	currentWeek = (value==0?1:value);
	            }
	        } finally {
	            if (cursor != null) {
	                try {
	                    cursor.close();
	                } catch (Exception e) {
	                }
	            }
	        }
		} catch (Exception e) {
			currentWeek=1;
		} 
	}
	
	public void saveCurrentCWeek(int currentWeek){
		Database db = Server.server.getServiceRegistry().getSleepyCatService().stepBattleDB;
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		StringBinding.stringToEntry("CURRENTWEEK", key);
		IntegerBinding.intToEntry(currentWeek, data);
		try {
			db.put(null, key, data);
		} catch (DatabaseException e) {
			
		}
	}
	
	public void loadTopList(){
		StepBattleScoreDao dao = Server.server.getServiceRegistry().getDbService().stepbattlescoreDAO;
		List<StepBattleScore> list = dao.getTop16Players();
		list.toArray(top16);
	}
	
	public void addWinner(Actor actor){
		int index = -1;
		for(int i = 0;i<winners.size();i++){
			Actor act = winners.get(i);
			if(act!=null){
				if(act.id==actor.id){
					index = i;
					break;
				}
			}
		}
		if(index==-1){
			winners.add(actor);
			winnerScore.add(1);
		}else{
			if(winnerScore.get(index)!=null)
				winnerScore.set(index, winnerScore.get(index).intValue()+1);
			else
				winnerScore.set(index, 1);
		}
	}
	
	/** 报名*/
	public void signUp(int playerId){
		if(getStepBattleType()==StepServer.STEPBATTLE_TYPE_NORMAL){//普通
			//方士
			if(ObjectAccessor.getPlayer(playerId)!=null&&ObjectAccessor.getPlayer(playerId).clazz==Player.CLASS_4){
				if(!queues_Fangshi.contains(new Integer(playerId))){
					queues_Fangshi.add(playerId);
				}
			}else{//除方士之外的职业
				if(!queues.contains(new Integer(playerId)))
					queues.add(playerId);
			}
			if(!todaySigns.contains(new Integer(playerId))){
				todaySigns.add(playerId);
			}
		}else if(getStepBattleType()==StepServer.STEPBATTLE_TYPE_16){//常规16强
			if(!queues_EveryDay.contains(new Integer(playerId)))
				queues_EveryDay.add(playerId);
			if(!todaySigns_EveryDay.contains(new Integer(playerId))){
				todaySigns_EveryDay.add(playerId);
			}
			//统计每个玩家的报名次数
			if(!todaySignedTimes.containsKey(playerId)){//如果没报过名
				todaySignedTimes.put(playerId, 1);
			}else{
				if(todaySignedTimes.get(playerId)<MAXSIGNEDTIMES){//已经报过名少于3次也可报名
					int times=todaySignedTimes.get(playerId)+1;
					todaySignedTimes.put(playerId, times);
				}else{
					Player p1=ObjectAccessor.getPlayer(playerId);
					p1.message(-1, "每日仅可参加三次跨服常规赛！", -1, -1);
				}
			}
			log.info("[STEPBATTLETODAYSIGNEDTIMES]PLAYERID["+playerId+"]");
		}else if(getStepBattleType()==StepServer.STEPBATTLE_TYPE_TOURNAMENT){//争霸赛
			if(!queues_Finals.contains(new Integer(playerId)))
				queues_Finals.add(playerId);
			if(!todaySigns_Finlas.contains(new Integer(playerId))){
				todaySigns_Finlas.add(playerId);
			}
			log.info("[STEPBATTLEFINALSSIGNED]PLAYERID["+playerId+"]");
		}
	}
	
	public void removeFromQueue(int playerId){
		queues.remove(new Integer(playerId));
		queues_Fangshi.remove(new Integer(playerId));
		queues_EveryDay.remove(new Integer(playerId));
//		queues_Finals.remove(new Integer(playerId));
		log.info("[STEPREMOVEQUEUES_EVERYDAY]PLAYERID["+playerId+"]");
		player2Instance.remove(playerId);
	}
	
	public VMap addToMap(Player player, int mapId, int x, int y, boolean check) throws VMapException {
		if(check){
			int faction = player.faction;
			int[] position = outPosition[faction-1];
			int outMapId = position[0];
			int outX = position[1];
			int outY = position[2];
			return Server.server.getWorld().addPlayerToMap(player, outMapId, outX, outY, true);
		}else{
			if(getStepBattleType()==StepServer.STEPBATTLE_TYPE_TOURNAMENT){
				StepBattleInstanceFinals instancef = (StepBattleInstanceFinals)player2Instance.get(player.id);
				if(enterIndtanceRecord.get(player.id)==null){
					player.removeFromMap();
					player.addToMap(instancef.map, x, y);
					if(player!=null && player.attendant!=null){
						player.attendant.follow();
					}
					ChannelService channelService = Server.server.getServiceRegistry().getChannelService();
					channelService.addSessionToChannel("chat_system", player.session);
					enterIndtanceRecord.put(player.id, 1);
					return instancef.map;
				}else{
					StepBattleInstanceFinals instanceOther=futureEnterInstances.get(player.id);
					if(instanceOther==null)
						return null;
					if(instancesFinal.contains(instanceOther)){
						player.removeFromMap();
						player.addToMap(instanceOther.map, x, y);
						if(player!=null && player.attendant!=null){
							player.attendant.follow();
						}
						ChannelService channelService = Server.server.getServiceRegistry().getChannelService();
						channelService.addSessionToChannel("chat_system", player.session);
						instanceOther.refreshStartTime();
						futureEnterInstances.remove(player.id);
						StepBattleInstanceFinals ins = currentInstances.remove(player.id);
						ins.isDispatch = false;
						ins.dispatchOk = true;
						instanceOther.waitDispatch=false;
						instanceOther.dispatchOk = false;
						StepBattleInstanceFinals.isTrans = false;
						log.info("[STEPGOOTHERINSTANCEOK]PLAYERID["+player.id+"]PLAYERNAME["+player.name+"]INSID["+ins.instanceId+"]OTHER["+instanceOther.instanceId+"]");
						return instanceOther.map;
					}else{
						StepBattleInstanceFinals ins = currentInstances.remove(player.id);
						ins.isDispatch = false;
						log.info("[STEPGOOTHERINSTANCEFAIL]PLAYERID["+player.id+"]PLAYERNAME["+player.name+"]");
					}
					return null;
				}
			}else{
				player.removeFromMap();
				StepBattleInstance instance = player2Instance.get(player.id);
				player.addToMap(instance.map, x, y);
				//初始化聊天频道
				ChannelService channelService = Server.server.getServiceRegistry().getChannelService();
				channelService.addSessionToChannel("chat_system", player.session);
				return instance.map;
			}
		}
	}
	
	/** 定时检查报名人数并在人满情况下开启战场副本 */
	public void checkAndOpenInstance(){
		if(getStepBattleType()==StepServer.STEPBATTLE_TYPE_NORMAL){//普通
			int currentQueues = queues.size();
			if(currentQueues>=minEnterPlayers || (currentQueues>=minEnterPlayers-1&&queues_Fangshi.size()>0)){
				VMap map = VMapUtil.create(this, Server.server.getWorld(), mapId, Server.REVISION_TYPE_PIP);
				if(Server.server.revision.equals(Server.REVISION_TYPE_TW))
					map = VMapUtil.create(this, Server.server.getWorld(), mapId, Server.REVISION_TYPE_TW);
				StepBattleInstance instance = new StepBattleInstance(map, this);
				instance.setInstanceEndTime(instanceEndHour, instanceEndMin-1);
				instances.add(instance);
				for(int i=0;i<minEnterPlayers;i++){
					int playerId = -1;
					if(queues_Fangshi.size()>0){
						if(i<minEnterPlayers-1){
							playerId= queues.remove();
						}else if(i == minEnterPlayers-1){
							playerId = queues_Fangshi.remove();
						}
					}else{
						playerId= queues.remove();
					}
					log.info("[STEPTRANS]PLAYERID["+playerId+"]");
					Player player = ObjectAccessor.getPlayer(playerId);
					player2Instance.put(playerId, instance);
					try {
						if(player!=null){
							player.isInStep = true;
							player.minorFaction = getMinorFaction();
							instance.addPlayer(player);
							int[] positions = getInitPosition(player.minorFaction);
							Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
							pt.putInt(StepServer.TYPE_BATTLE_START);
							pt.putString("");
							player.send(pt);
							player.buffs.clearAllBuffs();
							//特殊处理坐骑天命套装效果579BUFF
							player.buffs.removeBuff(Horse.jewelBuffId);
							if(player.horse!=null){
								player.horse.processRideBuff(player);
							}
							CandidateService candidateService = Server.server.getServiceRegistry().getCandidateService();
							player.skills.removeSkill(candidateService.getKingSkillGroupId(player.clazz), 1);
							player.skills.removeSkill(candidateService.getKingSkillGroupId(player.clazz), 0);
							player.buffs.removeBuff(Skills.getSkillId(candidateService.getKingSkillGroupId(player.clazz), 1));
							player.initBuffs();
							player.initPlayerBooks();
							int attendantInstanceId = player.pool.getInt(Player.PROPERTY_LAST_ATTENDANT_INSTANCEID);
							if(attendantInstanceId!=0){
								Attendant attendant = player.attendantBag.getAttendant(attendantInstanceId);
								if(attendant!=null){
									attendant.follow();
									if(!(attendant.loyal<=0 || attendant.hp<=0)){
										AttendantFixService attFixService = Server.server.getServiceRegistry().getAttendantFixService();
										attFixService.addBuffOnFollow(player, attendant);
									}
								}
							}
							player.loadFinished();
							player.refreshProperties(false);
							player.setHp(player.maxhp, true);
							player.setMp(player.maxmp, true);
							player.goMap(mapId, positions[0], positions[1]);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}else if(getStepBattleType()==StepServer.STEPBATTLE_TYPE_16){//常规16强
			int currentQueues = queues_EveryDay.size();
			if(currentQueues>=minEnterPlayers_16){
				VMap map = VMapUtil.create(this, Server.server.getWorld(), mapId, Server.REVISION_TYPE_PIP);
				StepBattleInstance instance = new StepBattleInstance(map, this);
				instance.setInstanceEndTime(endPvpBattle_everyday_Hour, endPvpBattle_everyday_Min);
				instances.add(instance);
				for(int i=0;i<minEnterPlayers_16;i++){
					int playerId = queues_EveryDay.remove();
					log.info("[STEPTRANS]PLAYERID["+playerId+"]");
					Player player = ObjectAccessor.getPlayer(playerId);
					player2Instance.put(playerId, instance);
					try {
						if(player!=null){
							player.isInStep = true;
							player.minorFaction = getMinorFaction();
							instance.addPlayer(player);
							int[] positions = getInitPosition(player.minorFaction);
							Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
							pt.putInt(StepServer.TYPE_BATTLE_START);
							pt.putString("");
							player.send(pt);
							player.buffs.clearAllBuffs();
							//特殊处理坐骑天命套装效果579BUFF
							player.buffs.removeBuff(Horse.jewelBuffId);
							if(player.horse!=null){
								player.horse.processRideBuff(player);
							}
							CandidateService candidateService = Server.server.getServiceRegistry().getCandidateService();
							player.skills.removeSkill(candidateService.getKingSkillGroupId(player.clazz), 1);
							player.skills.removeSkill(candidateService.getKingSkillGroupId(player.clazz), 0);
							player.buffs.removeBuff(Skills.getSkillId(candidateService.getKingSkillGroupId(player.clazz), 1));
							player.initBuffs();
							player.initPlayerBooks();
							int attendantInstanceId = player.pool.getInt(Player.PROPERTY_LAST_ATTENDANT_INSTANCEID);
							if(attendantInstanceId!=0){
								Attendant attendant = player.attendantBag.getAttendant(attendantInstanceId);
								if(attendant!=null){
									attendant.follow();
									if(!(attendant.loyal<=0 || attendant.hp<=0)){
										AttendantFixService attFixService = Server.server.getServiceRegistry().getAttendantFixService();
										attFixService.addBuffOnFollow(player, attendant);
									}
								}
							}
							player.loadFinished();
							player.refreshProperties(false);
							player.setHp(player.maxhp, true);
							player.setMp(player.maxmp, true);
							player.goMap(mapId, positions[0], positions[1]);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}else if(getStepBattleType()==StepServer.STEPBATTLE_TYPE_TOURNAMENT){//争霸赛
			int currHour=Time.currentHour;
			int currMin=Time.currentMin;
			if(currHour>signFinals_EndHour || (currHour==signFinals_EndHour && currMin>signFinals_EndMin)){//报名结束后才开始战场
				if(finalsEnd){//争霸赛结束
					if(noPlayerEnterBattle){//没有玩家参加比赛时返还所有人的本金
						if(!hadSendTitle){
							for(int i=0;i<finalsPlayers.size();i++){
								returnBetPlayerCoins(i,true);
							}
							hadSendTitle=true;
						}
					}else{
						if(instancesFinal.size()>0){//结束所有副本
							for(StepBattleInstanceFinals in:instancesFinal){
								if(in!=null){
									for(int i=0;i<in.players.size();i++){
										Player player=ObjectAccessor.getPlayer(in.players.get(i));
										if(player!=null){
											in.playerBattleEnd(player);
										}
									}
									in.state=StepBattleInstanceFinals.STATE_END;
								}
							}
						}
						//排序
						if(!hadSendTitle){
							for(int i=0;i<finalsPlayers.size();i++){//按名次排序
								for(int j=i+1;j<finalsPlayers.size();j++){
									StepBattleScoreTop16 sbs1=finalsPlayers.get(i);
									StepBattleScoreTop16 sbs2=finalsPlayers.get(j);
									if(sbs2!=null&&sbs1!=null&&sbs2.ranking>sbs1.ranking){
										finalsPlayers.set(i, sbs2);
										finalsPlayers.set(j, sbs1);
										}
									}
							}
							for(StepBattleScoreTop16 sbs16:finalsPlayers){
								if(sbs16!=null){
									log.info("[FINALSPLAYERSRANKING_WHENFINALSBATTLEEND]PLAYERID["+sbs16.playerid+"]ACC["+sbs16.accountId+"]GAMECODE["+sbs16.gameCode+"]WINCOUNT["+sbs16.winCount+"]TIMER["+sbs16.time+"]RANKING["+sbs16.ranking+"]");
								}
							}
							//保存更新争霸比赛名次数据
							Server.server.getServiceRegistry().getDbService().schedule(new StepBattleScoreFinalsSaveCall(null, finalsPlayers,StepBattleScoreFinalsSaveCall.TYPE_UPDATEFINALSRANKING));
							log.info("[FINALSPLAYERSRANKING_SAVEOK]");
							//返还押注冠军的本金
							returnBetPlayerCoins(0,false);
							//把没有参加争霸赛玩家的金钱反还给各自押注的玩家
							returnOtherNoSignUpFinlasPlayersBet();
							//把战场中一个人时掉线的玩家押注滚动到冠军头上
							returnOtherDisConnetionFinlasPlayersBet(finalsPlayers.get(0).playerid, finalsPlayers.get(0).gameCode);
							for(int i=0;i<finalsPlayers.size();i++){
								StepBattleScoreTop16 sbs=finalsPlayers.get(i);
								int rankingIndex=0;
								if((i==0||i==1)&&sbs.ranking>0){//冠亚军称号
									rankingIndex=i;
									sendTitle(sbs.playerid, sbs.gameCode, titleItem.get(titleIds_Finals[rankingIndex]),"争霸赛奖励");
									log.info("[FINALSPLAYERSRANKING_WINNERANDSECOND]PLAYERID["+sbs.playerid+"]GAMECODE["+sbs.gameCode+"]");
								}else if(sbs.ranking>0){//参加比赛玩家给强者称号
									rankingIndex=2;
									sendTitle(sbs.playerid, sbs.gameCode, titleItem.get(titleIds_Finals[rankingIndex]),"争霸赛奖励");
									log.info("[FINALSPLAYERSRANKING_POWERHOUSE]PLAYERID["+sbs.playerid+"]GAMECODE["+sbs.gameCode+"]");
								}
							}
							hadSendTitle=true;
						}
					}
				}else{//争霸赛进行中
					if(firstEnterFinlas){//争霸赛第一轮
						checkAndCreateFinalsInstances();
					}else{
						//检测副本及玩家状态，确定冠亚军
						updateAllFinalsInstancesState();
						if(lastEnterFinalsBattleTime==0){
							if(isCurrRoundInstancesFinalsEnd()){
								if(!finalsEnd){
									lastEnterFinalsBattleTime=Time.currTime;
//									allInstanceEnd=false;
									//提醒胜利玩家耐心等待
									for(StepBattleInstanceFinals in:instancesFinal){
										for(int i=0;i<in.players.size();i++){
											int playerId=in.players.get(i);
											Player player=ObjectAccessor.getPlayer(playerId);
											if(player!=null){
												Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
												pt.putInt(StepClientPacketCall.TYPE_NOTIFYFINALSPLAYER);
												pt.putString("本轮争霸赛已经结束，下一轮比赛开始前您将有5分钟的准备时间。");
												player.send(pt);
											}
										}
									}
								}
							}
						}else{
							if(Time.currTime-lastEnterFinalsBattleTime>waitCoolDownTime){
								allInstanceEnd=true;
							}
							if(checkAllInsIsDisPatchOK()){//所有副本分配完成
								lastEnterFinalsBattleTime=0;
								allInstanceEnd=false;
								//遍历所有副本查找轮空的玩家给提示
								for(StepBattleInstanceFinals in:instancesFinal){
									if(in.players.size()==1){
										Player player=ObjectAccessor.getPlayer(in.players.get(0));
										Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
										pt.putInt(StepClientPacketCall.TYPE_NOTIFYFINALSPLAYER);
										pt.putString("由于您的对手未能出战，恭喜您直接晋级一轮比赛。请耐心等候其他选手的比赛结果后为您分配对手。");
										if(player!=null){
											player.send(pt);
										}
									}
								}
							}
						}
					}					
				}
			}
		}
	}
	/**检测所有副本是否已经分配完成**/
	public boolean checkAllInsIsDisPatchOK(){
		int count=0;
		for(StepBattleInstanceFinals in:instancesFinal){
			if(in.players.size()==1){
				count++;
			}
		}
		if(count<2){
			return true;
		}else{
			return false;
		}
	}
	
	/**检测所有争霸副本的状态*/
	public synchronized void updateAllFinalsInstancesState(){
		if(/*instancesFinal.size()==1&&*/getAllInstancesFinalsPlayers()==2&&queues_Finals.size()==0){//只剩下2个人时
//			StepBattleInstanceFinals in=instancesFinal.get(0);
			for(StepBattleInstanceFinals in:instancesFinal){
				if(in!=null){
					for(int j=0;j<in.players.size();j++){
						int playerId=in.players.get(j);
						if(!winnerAndSecondId.contains(playerId)){
							winnerAndSecondId.add(playerId);
							log.info("[SAVEWINNERWHEREONLYHADTWOPLAYERS]PLAYERID["+playerId+"]");
						}
					}
				}
			}
		}else
		if(/*instancesFinal.size()==1&&*/getAllInstancesFinalsPlayers()==1&&queues_Finals.size()==0){//只剩下一个副本并且副本中只剩一个人时为冠军
			int playerId=-1;
			for(StepBattleInstanceFinals in:instancesFinal){
				if(in.players.size()>0){
					for(int i=0;i<in.players.size();i++){
						playerId=in.players.get(i);
						log.info("[WHEREALLINSTANCEHADONEPLAYER]PLAYERID["+playerId+"]");
						break;
					}
				}
			}
//			int playerId=instancesFinal.get(0).players.get(0);
			Player player=ObjectAccessor.getPlayer(playerId);
			if(player!=null){
				player.message(-1, "恭喜您获得冠军", -1, -1);
				finalsEnd=true;
				allInstanceEnd=true;//报告所有副本结束
				for(StepBattleScoreTop16 sbs16:finalsPlayers){
					if(sbs16.playerid==playerId&&sbs16.gameCode.equals(player.gameCode)){
						sbs16.ranking=99;
						log.info("[SETFINALSWINNER]PLAYERID["+sbs16.playerid+"]ACC["+sbs16.accountId+"]RANKING["+sbs16.ranking+"]WINCOUNT["+sbs16.winCount+"]");
						break;
					}
				}
				sendDPacketToClientServer(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER, StepClientPacketCall.TYPE_BATTLE_FINALS_CHAT,
						MessageFormat.format("经过激烈的厮杀，{0}荣获本届争霸赛的冠军以及争霸赛冠军称号！",player.name));
			}
			int tempPlayerId=-1;
			for(int i=0;i<winnerAndSecondId.size();i++){//亚军
				tempPlayerId=winnerAndSecondId.get(i);
				if(tempPlayerId!=playerId){
					for(StepBattleScoreTop16 sbs16:finalsPlayers){
						if(sbs16.playerid==tempPlayerId){
							sbs16.ranking=98;
							break;
						}
					}
				}
			}
			log.info("[STEPFINALSEND]WINNERPLAYERID["+playerId+"]SECONDPLAYERID["+tempPlayerId+"]");
		}
	}
	
	/**统计没有参加比赛的玩家*/
	public void  processNotEnteredInstancePlayers(){
		for(int i=0;i<finalsPlayers.size();i++){
			StepBattleScoreTop16 sbs16=finalsPlayers.get(i);
			if(sbs16!=null){
				String sbs16PlayerInfo=sbs16.playerid+sbs16.gameCode;
				int count=0;
				for(int j=0;j<todayEnteredInstancePlayers.size();j++){
					String enteredPlayerInfo=todayEnteredInstancePlayers.get(j);
					if(enteredPlayerInfo.equals(sbs16PlayerInfo)){//用id和gamecode区分玩家
						count++;
						break;
					}
				}
				if(count==0){//没有进入战场的玩家
					if(!notEnteredInstancePlayers.contains(sbs16PlayerInfo)){
						notEnteredInstancePlayers.add(sbs16PlayerInfo);
						log.info("[PROCESSNOTENTEREDINSTANCEPLAYERS]PLAYERINFO["+sbs16PlayerInfo+"]");
					}
				}
			}
		}
	}
	
	/**给冠军押注玩家的本金返还*/
	public void returnBetPlayerCoins(int playerIndex,boolean noPlayerEnterBattle){
		StepBattleScoreTop16 sbs=finalsPlayers.get(playerIndex);
		String allPlayerBetsString=sbs.getPoolbet().getString(StepBattleScoreTop16.BETFLAG);
		if(allPlayerBetsString.startsWith("|")){
			allPlayerBetsString=allPlayerBetsString.substring(1);
		}
		String[] allPlayersBets=allPlayerBetsString.split("\\|");
		if(allPlayersBets!=null&&allPlayersBets.length>0){
			Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
			pt.putInt(StepServer.TYPE_FINALBATTLE_SENDREWARD);
			String noPlayerFlag=noPlayerEnterBattle?"NOPLAYERENTER":"";
			pt.putUTF(noPlayerFlag);
			pt.putUTF(sbs==null?"":sbs.name);
			pt.putInt(allPlayersBets.length);
			for(int i=0;i<allPlayersBets.length;i++){
				if(allPlayersBets[i]!=null&&!allPlayersBets[i].equals("")){
					String[] everyPlayerInfo=allPlayersBets[i].split(",");
					if(everyPlayerInfo!=null){
						pt.putInt(Integer.parseInt(everyPlayerInfo[0]));//playerid
						pt.putUTF(everyPlayerInfo[1]);//gamecode
						pt.putInt(Integer.parseInt(everyPlayerInfo[2]));//betcoins
						int wincoinsPer=0;
						pt.putInt(wincoinsPer);//赢得的金币数量
						log.info("[RETURNWINNERBETCOINS]PLAYERID["+allPlayersBets[i]+"]PLAYERGAMECODE["+everyPlayerInfo[1]+"]");
					}
				}
			}
			DispatchPacket dp = new DispatchPacket(0, pt);
			for(IoSession session : Server.server.getServiceRegistry().getStepServer().sessions){
				session.write(dp);
			}
		}
	}
	/***没有报名的玩家押注原封返还*/
	public void returnOtherNoSignUpFinlasPlayersBet(){
		List<StepBattleScoreTop16> noEnterPlayersSBS=new ArrayList<StepBattleScoreTop16>();
		for(int j=1;j<finalsPlayers.size();j++){//当只有一个人报名时，除了冠军之外 的玩家返还押注
			StepBattleScoreTop16 sbs=finalsPlayers.get(j);
			for(int i=0;i<notEnteredInstancePlayers.size();i++){
				if((sbs.playerid+sbs.gameCode).equals(notEnteredInstancePlayers.get(i))){
					if(!noEnterPlayersSBS.contains(sbs)){
						noEnterPlayersSBS.add(sbs);
					}
				}
			}
		}
		if(noEnterPlayersSBS.size()>0){
			for(StepBattleScoreTop16 sbs:noEnterPlayersSBS){
				String[] allPlayers=sbs.getAllPlayersBetCoins();
				Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
				pt.putInt(StepServer.TYPE_FINALBATTLE_SENDREWARD);
				pt.putUTF("NOPLAYERENTER");
				pt.putUTF(sbs.name);
				//每个押注玩家的信息统计
				List<String[]> allPlayerInfo=new ArrayList<String[]>();
				for(int k=0;k<allPlayers.length;k++){
					if(allPlayers[k]!=null&&allPlayers[k].length()>0){
						String[] everyPlayerInfo=allPlayers[k].split(",");
						if(everyPlayerInfo!=null){
							allPlayerInfo.add(everyPlayerInfo);
						}
					}
				}
				pt.putInt(allPlayerInfo.size());
				for(String[] onePlayer:allPlayerInfo){
					pt.putInt(Integer.parseInt(onePlayer[0]));//playerid
					pt.putUTF(onePlayer[1]);//gamecode
					pt.putInt(Integer.parseInt(onePlayer[2]));//betcoins
					pt.putInt(0);//赢得的金币数量
					log.info("[SENDNOTENTENBATTLEINSTANCE]FINALSPLAYERID["+sbs.playerid+"]TARGETPLAYERINFO["+onePlayer[0]+","+onePlayer[1]+","+onePlayer[2]+"]GAMECODE["+sbs.gameCode+"]BETS["+Integer.parseInt(onePlayer[2])+"]");
				}
				DispatchPacket dp = new DispatchPacket(0, pt);
				for(IoSession session : Server.server.getServiceRegistry().getStepServer().sessions){
					session.write(dp);
				}
			}
		}
	}
	/***
	 * 把比赛过程中（一个人轮空时，进入下一轮等待时）掉线玩家的押注按比例发送给冠军押注的玩家
	 */
	public void returnOtherDisConnetionFinlasPlayersBet(int playerId,String gameCode){
		long allBet=0;
		List<StepBattleScoreTop16> disConnPlayers=new ArrayList<StepBattleScoreTop16>();//进入战场并一个人掉线的玩家
		for(int i=1;i<finalsPlayers.size();i++){
			StepBattleScoreTop16 sbs=finalsPlayers.get(i);
			for(String playerInfo1:disConnetionPlayers){
				if((sbs.playerid+sbs.gameCode).equals(playerInfo1)){
					if(!disConnPlayers.contains(sbs)){
						disConnPlayers.add(sbs);
					}
				}
			}
		}
		for(StepBattleScoreTop16 sbs:disConnPlayers){//统计没参赛玩家的总押注数
			allBet+=sbs.getBet();
		}
		//统计给冠军押注玩家的押注比例
		List<String> everyPlayerBet=getWinnerBetPlayersPer(playerId,gameCode);
		Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
		pt.putInt(StepServer.TYPE_FINALBATTLE_SENDREWARD);
		pt.putUTF("DISCONNPLAYERS");//掉线玩家的奖金
		pt.putUTF("");
		pt.putInt(everyPlayerBet.size());
		for(int i=0;i<everyPlayerBet.size();i++){
			String[] everyPlayerInfo=everyPlayerBet.get(i).split(",");
			if(everyPlayerInfo!=null){
				pt.putInt(Integer.parseInt(everyPlayerInfo[0]));//playerid
				pt.putUTF(everyPlayerInfo[1]);//gamecode
				pt.putInt(0);//betcoins
				int wincoinsPer=(int)(allBet*Float.parseFloat(everyPlayerInfo[3]));
				pt.putInt(wincoinsPer);//赢得的金币数量
				log.info("[SENDWINNNERBETCOINSNOENTERBATTLE]WINNERPLAYERID["+playerId+"]TARGETPLAYERINFO["+everyPlayerBet.get(i)+"]GAMECODE["+gameCode+"]ALLBETS["+allBet+"]");
			}
		}
		DispatchPacket dp = new DispatchPacket(0, pt);
		for(IoSession session : Server.server.getServiceRegistry().getStepServer().sessions){
			session.write(dp);
		}
	}
	
	public List<String> getWinnerBetPlayersPer(int playerId,String gameCode){
		NumberFormat numberFormat = NumberFormat.getInstance();
		numberFormat.setMaximumFractionDigits(2);
		List<String> allBetPlayers=new ArrayList<String>();
		StepBattleScoreTop16 winnerSbs=getPlayerBets(playerId, gameCode);
		if(winnerSbs!=null){
			long allbet=winnerSbs.getBet();
			String[] allPlayerBet=winnerSbs.getAllPlayersBetCoins();
			if(allPlayerBet!=null){
				for(int i=0;i<allPlayerBet.length;i++){
					if(allPlayerBet[i]!=null&&!allPlayerBet[i].equals("")){
						int betcoins=Integer.parseInt(allPlayerBet[i].split(",")[2]);
						String result = numberFormat.format((float)betcoins/(float)allbet);
						allBetPlayers.add(allPlayerBet[i]+","+new Float(result));
					}
				}
			}
		}
		return allBetPlayers;
	}
	
	public StepBattleScoreTop16 getPlayerBets(int playerId,String gameCode){
		for(StepBattleScoreTop16 sbsTemp:finalsPlayers){
			if(sbsTemp!=null&&sbsTemp.playerid==playerId&&sbsTemp.gameCode.equals(gameCode)){
				return sbsTemp;
			}
		}
		return null;
	}
	
	/**是否已经发送过称号*/
	public boolean hadSendTitle=true;
	/**冠亚军ID*/
	public List<Integer> winnerAndSecondId=new ArrayList<Integer>();
	/**
	 * 获取所有争霸副本中的玩家数量
	 * @return
	 */
	public int getAllInstancesFinalsPlayers(){
		int count=0;
		for(StepBattleInstanceFinals in:instancesFinal){
			count+=in.players.size();
		}
		count+=this.queues_Finals.size();
		return count;
	}
	
	
	public StepBattleInstanceFinals getUsableInstanceExcept(int instanceId){
		//从随机位置开始遍历所有副本状态
		int randomIns=(rd.nextInt()>>>3)%instancesFinal.size();
		for(int i=randomIns;i<instancesFinal.size()+randomIns;i++){
			int index=i%instancesFinal.size();
			StepBattleInstanceFinals ins = instancesFinal.get(index);
			if(ins.players.size()==1&& ins.instanceId!=instanceId && !futureEnterInstances.containsValue(ins) && !ins.isDispatch){
				return ins;
			}
		}
//		for(int i=0;i<instancesFinal.size();i++){
//			StepBattleInstanceFinals ins = instancesFinal.get(i);
//			if(ins.players.size()==1&& ins.instanceId!=instanceId && !futureEnterInstances.containsValue(ins) && !ins.isDispatch){
//				return ins;
//			}
//		}
		return null;
	}
	
	
	/**争霸赛结束*/
	public static boolean finalsEnd=false;
	
	/**第一次开始争霸赛*/
	public static boolean firstEnterFinlas=true;
	/***
	 * 所有副本是否已经结束
	 * @return
	 */
	public boolean isCurrRoundInstancesFinalsEnd(){
		for(StepBattleInstanceFinals in:instancesFinal){
			if(in.players.size()==2){
				return false;
			}
		}
		return true;
	}
	/**是否已经检测完所有争霸赛玩家的在线状态*/
	public static boolean hadCheckAllFinalPlayersOk=false;
	//是否已经请求过玩家状态(只发送一次)
	public static boolean hadSendAllFinalPlayersInfos=false;
	/**报名的玩家检测状态*/
	List<StepBattleScoreTop16> signUpPlayers=new ArrayList<StepBattleScoreTop16>();
	
	public void checkAndCreateFinalsInstances(){
		if(!hadCheckAllFinalPlayersOk){//没有检测完所有玩家状态时不开战场
			if(!hadSendAllFinalPlayersInfos){
				//检测报名的玩家
				for(StepBattleScoreTop16 sbs16:finalsPlayers){
					if(queues_Finals.contains(new Integer(sbs16.playerid))){
						if(!signUpPlayers.contains(sbs16)){
							signUpPlayers.add(sbs16);
						}
					}
				}
				Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
				pt.putInt(StepClientPacketCall.TYPE_CHECKPLAYERONLINE_STEPSERVER);
				pt.putInt(signUpPlayers.size());
				for(int i=0;i<signUpPlayers.size();i++){
					StepBattleScoreTop16 sbs16=signUpPlayers.get(i);
					pt.putInt(sbs16.playerid);
					pt.putUTF(sbs16.gameCode);
				}
				DispatchPacket dp = new DispatchPacket(0, pt);
				for(IoSession session : Server.server.getServiceRegistry().getStepServer().sessions){
					session.write(dp);
				}
				hadSendAllFinalPlayersInfos=true;
			}
			int count=0;
			for(StepBattleScoreTop16 sbs16:signUpPlayers){
				if(sbs16.onLineState==0){
					count++;
				}
			}
			log.info("[CHECKONLINEPLAYEROVER]COUNT["+count+"]");
			if(count==0){
				hadCheckAllFinalPlayersOk=true;
				for(StepBattleScoreTop16 sbs16:finalsPlayers){
					if(sbs16.onLineState==-1){//不在线玩家
						queues_Finals.remove(new Integer(sbs16.playerid));
					}
				}
				log.info("[HADCHECKALLFINALPLAYERSOK]");
			}
		}else{
			if(firstCheckQueuesFinals){//如果是第一次分配战场
				firstCheckQueuesFinals=false;
				int signUpNums=queues_Finals.size();
				log.info("[CHECKANDCREATEFINALSINSTANCES]SIGNUPNUMS["+signUpNums+"]");
				if(signUpNums==1){//1人时直接胜利晋级为冠军
					int signupPlayerId=queues_Finals.remove();
					for(int i=0;i<finalsPlayers.size();i++){
						StepBattleScoreTop16 sbs=finalsPlayers.get(i);
						if(sbs.playerid==signupPlayerId){
							sbs.ranking=99;
						}
					}
					Player player=ObjectAccessor.getPlayer(signupPlayerId);
					Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
					pt.putInt(StepClientPacketCall.TYPE_NOTIFYFINALSPLAYER);
					pt.putString("本届比赛其他选手因被您气势所摄纷纷退出比赛，您直接获得冠军。");
					if(player!=null){
						player.send(pt);
					}
					finalsEnd=true;
				}else if(signUpNums==0){//没人报名时不处理
					noPlayerEnterBattle=true;
					finalsEnd=true;
					log.info("[NOPLAYERENTERBATTLE]");
				}
			}else
			{
				//2人以上开战场副本
				//分配战场副本
				int size=queues_Finals.size();
				if(size==0){
					return;
				}
				VMap map = VMapUtil.create(this, Server.server.getWorld(), mapId, Server.REVISION_TYPE_PIP);
				StepBattleInstanceFinals instance = new StepBattleInstanceFinals(map, this);
				instancesFinal.add(instance);
				for(int i=0;i<2;i++){
					try {
						int playerId = queues_Finals.remove();
						Player player = ObjectAccessor.getPlayer(playerId);
						log.info("[STEPTRANSFINALSTOINSTANCE]PLAYERID["+playerId+"]"+player.name);
						player2Instance.put(playerId, instance);
						if(player!=null){
							String saveFlag=new String(player.id+player.gameCode);
							if(!todayEnteredInstancePlayers.contains(saveFlag)){
								todayEnteredInstancePlayers.add(saveFlag);//记录进入副本的玩家(用来判断掉线时的奖励的发送问题)
							}
							player.isInStep = true;
							player.minorFaction = getMinorFaction_Finals();
							instance.addPlayer(player);
							int[] positions = getInitPosition(player.minorFaction);
							Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
							pt.putInt(StepServer.TYPE_BATTLE_START);
							pt.putString("");
							player.send(pt);
							player.buffs.clearAllBuffs();
							//特殊处理坐骑天命套装效果579BUFF
							player.buffs.removeBuff(Horse.jewelBuffId);
							if(player.horse!=null){
								player.horse.processRideBuff(player);
							}
							CandidateService candidateService = Server.server.getServiceRegistry().getCandidateService();
							player.skills.removeSkill(candidateService.getKingSkillGroupId(player.clazz), 1);
							player.skills.removeSkill(candidateService.getKingSkillGroupId(player.clazz), 0);
							player.buffs.removeBuff(Skills.getSkillId(candidateService.getKingSkillGroupId(player.clazz), 1));
							player.initBuffs();
							player.initPlayerBooks();
							int attendantInstanceId = player.pool.getInt(Player.PROPERTY_LAST_ATTENDANT_INSTANCEID);
							if(attendantInstanceId!=0){
								Attendant attendant = player.attendantBag.getAttendant(attendantInstanceId);
								if(attendant!=null){
									attendant.follow();
									if(!(attendant.loyal<=0 || attendant.hp<=0)){
										AttendantFixService attFixService = Server.server.getServiceRegistry().getAttendantFixService();
										attFixService.addBuffOnFollow(player, attendant);
									}
								}
							}
							player.loadFinished();
							player.refreshProperties(false);
							player.setHp(player.maxhp, true);
							player.setMp(player.maxmp, true);
							player.goMap(mapId, positions[0], positions[1]);
						}
						if(queues_Finals.size()==0&&i==0){//最后一个人的时候发送直接晋级
							instance.hadSendMessage=true;
							Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
							pt.putInt(StepClientPacketCall.TYPE_NOTIFYFINALSPLAYER);
							pt.putString("由于您的对手未能出战，恭喜您直接晋级一轮比赛。请耐心等候其他选手的比赛结果后为您分配对手。");
							if(player!=null){
								player.send(pt);
							}
							log.info("[STEPTRANSFINALSTOINSTANCE_LASTPLAYERONLY]PLAYERID["+playerId+"]");
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			if(queues_Finals.size()==0){//战场分配完成后开始计算没有参加比赛的玩家
				processNotEnteredInstancePlayers();
				firstEnterFinlas=false;
				hadSendTitle=false;
				log.info("[FIRSTENTERFINLAS]");
			}
		}
	}
	
	
	
	/** 取得本场战争所分配的minorFaction */
	public int getMinorFaction(){
		if(getStepBattleType()==StepServer.STEPBATTLE_TYPE_NORMAL){
			if(currMinorFaction==7)
				this.currMinorFaction = 1;
			else
				this.currMinorFaction += 1;
		}else if(getStepBattleType()==StepServer.STEPBATTLE_TYPE_16){
			if(currentFaction==1)
				currentFaction = 2;
			else
				currentFaction = 1;
			return currentFaction;
		}
		return currMinorFaction;
	}
	
	/***
	 * 取得本场争霸赛所分配的minorFaction
	 */
	public int getMinorFaction_Finals(){
		if(currentFaction==1)
			currentFaction = 2;
		else
			currentFaction = 1;
		return currentFaction;
	}
	
	/**
	 * 发送给客户端服务器协议
	 * @param opcode
	 * @param type
	 */
	public void sendDPacketToClientServer(short opcode,int type,String hint){
		Packet pt = new Packet(opcode);
		pt.putInt(type);
		pt.putString(hint);
		DispatchPacket dp = new DispatchPacket(0, pt);
		for(IoSession session : Server.server.getServiceRegistry().getStepServer().sessions){
			session.write(dp);
		}
		log.info("[SENDDPACKETTOCLIENTSERVER]OPCODE["+opcode+"]TYPE["+type+"]HINT["+hint+"]");
	}
	
	public void removeAndUpdateAllInstancesFinals(int diff){
		Iterator<StepBattleInstanceFinals> itFinals = instancesFinal.iterator();
		while(itFinals.hasNext()){
			StepBattleInstanceFinals instance = itFinals.next();
			if(instance!=null){
				instance.update(diff);
				if(!instance.isDispatch && (instance.state==StepBattleInstanceFinals.STATE_END /*|| instance.players.size()==0*/)&&!instance.waitDispatch){
					instance.endInstance();
					log.info("[REMOVEANDUPDATEALLINSTANCESFINALS]INSTANCEID["+instance.instanceId+"]PLAYERSSIZE["+instance.players.size()+"]");
					Server.server.getEventManager().unregisterListener(instance);
					itFinals.remove();
				}
			}
		}
	}
	public void update(int diff) {
		if(!Server.isStepServer)
			return;
		int currHour = Time.currentHour;
		int currMin = Time.currentMin;
		
		//发送常规赛公告(前两周每天一次)
		if(!chatFinalsBattle&&currentWeek<3){
			if((currHour>chatStartHour_PVP || (currHour==chatStartHour_PVP && currMin>=chatStartMin_PVP))){
				if(Server.isStepServer){
					//常规赛开始前5分钟提示
					sendDPacketToClientServer(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER, StepClientPacketCall.TYPE_BATTLE_FINALS_CHAT,"五分钟后单人跨服对抗赛开启，大家快去主城易大师处报名！");
				}
				chatFinalsBattle=true;
			}
		}
		
		if(getStepBattleType()==StepServer.STEPBATTLE_TYPE_NORMAL){//普通
			if(!canSignUp){
				if((currHour>startHour || currHour==startHour && currMin>=startMin) 
						&& (currHour<endHour || currHour==endHour && currMin<=endMin)){
					canSignUp = true;
					if(Server.isStepServer){
						Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
						pt.putInt(StepServer.TYPE_BATTLE_CANSIGN);
						pt.putString("");
						DispatchPacket dp = new DispatchPacket(0, pt);
						for(IoSession session : Server.server.getServiceRegistry().getStepServer().sessions){
							session.write(dp);
						}
					}
				}
			}else{
				if((currHour<startHour || currHour==startHour && currMin<startMin) 
						|| (currHour>endHour || currHour==endHour && currMin>endMin)){
					canSignUp = false;
				}
			}
		}else if(getStepBattleType()==StepServer.STEPBATTLE_TYPE_16){//16强
			if(!canSignUp_EveryDay){
				if((currHour>sign_everyday_StartHour || currHour==sign_everyday_StartHour && currMin>=sign_everyday_StartMin) 
						&& (currHour<sign_everyday_EndHour || currHour==sign_everyday_EndHour && currMin<=sign_everyday_EndMin)){
					canSignUp_EveryDay = true;
					hasPersisData = false;
					hasSendGift = false;
					if(Server.isStepServer){
						//pvp跨服提示报名
						sendDPacketToClientServer(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER, StepServer.TYPE_BATTLE_1V1_CANSIGN,"");
					}
				}
			}else{
				if((currHour<sign_everyday_StartHour || currHour==sign_everyday_StartHour && currMin<sign_everyday_StartMin) 
						|| (currHour>sign_everyday_EndHour || currHour==sign_everyday_EndHour && currMin>sign_everyday_EndMin)){
					canSignUp_EveryDay = false;
				}
			}
		}else if(getStepBattleType()==StepServer.STEPBATTLE_TYPE_TOURNAMENT){//争霸
			if(!chatFinalsBattle){
				if((currHour>signFinals_Hour || currHour==signFinals_Hour && currMin>=signFInals_Min+5) 
						&& (currHour<signFinals_EndHour || currHour==signFinals_EndHour && currMin<=signFinals_EndMin)){
					if(Server.isStepServer){
						//争霸赛开始前5分钟提示
						sendDPacketToClientServer(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER, StepClientPacketCall.TYPE_BATTLE_FINALS_CHAT,"单人跨服争霸赛将在5分钟后开始，参赛选手请前往主城易大师处报名！");
					}
					chatFinalsBattle=true;
				}
			}
			
			if(!canSignUp_Finals){
				if((currHour>signFinals_Hour || currHour==signFinals_Hour && currMin>=signFInals_Min) 
						&& (currHour<signFinals_EndHour || currHour==signFinals_EndHour && currMin<=signFinals_EndMin)){
					canSignUp_Finals = true;
				}
			}else{
				if((currHour>signFinals_EndHour || currHour==signFinals_EndHour && currMin>signFinals_EndMin)){
					canSignUp_Finals = false;
				}
			}
		}else if(getStepBattleType()==-1){
			canSignUp_EveryDay=false;
			canSignUp=false;
			canSignUp_Finals=false;
		}
		Iterator<StepBattleInstance> it = instances.iterator();
		while(it.hasNext()){
			StepBattleInstance instance = it.next();
			if(instance!=null){
				instance.update(diff);
				if(instance.state==StepBattleInstance.STATE_END || instance.players.size()==0){
					Server.server.getEventManager().unregisterListener(instance);
					it.remove();
				}
			}
		}
		
		//发公告给16强玩家参加争霸赛(暂时屏蔽常规赛第二周时再打开)
		if(Time.currentHour==chatHour_Finals&&Time.currentMin>=chatMin_Finals&&!hadSendChat&&currentWeek==3&&Time.currentWeekDay==1)
		{
			Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
			pt.putInt(StepServer.TYPE_NOTIFYFINALPLAYERS);
			pt.putString("");
			int size=finalsPlayers.size();
			pt.putInt(size);
			if(size>0){
				for(StepBattleScoreTop16 sbs:finalsPlayers){
					pt.putInt(sbs.playerid);
					pt.putUTF(sbs.gameCode);
				}
			}
			DispatchPacket dp = new DispatchPacket(0, pt);
			for(IoSession session : Server.server.getServiceRegistry().getStepServer().sessions){
				session.write(dp);
			}
			hadSendChat=true;
		}
		if(Time.currTime-lastCheckTime>500){
			checkAndOpenInstance();
			lastCheckTime = Time.currTime;
		}
		removeAndUpdateAllInstancesFinals(diff);
		if((Time.currentHour>endPvpBattle_everyday_Hour || Time.currentHour==endPvpBattle_everyday_Hour 
				&& Time.currentMin>=endPvpBattle_everyday_Min) && !hasPersisData){
			log.info("[ROUTININSTANCESEND]TIME["+Time.currentHour+":"+Time.currentMin+"]CURRENTWEEK["+currentWeek+"]");
			for(StepBattleInstance in:instances){
				if(in!=null){
					log.info("[CHECKALLINSTANCESSTATE]INSTANCEID["+in.instanceId+"]INSTANCESTARTTIME["+in.startTime+"]INSTATE["+in.state+"]PLAYERSIZE["+in.players.size()+"]");
					if(in.players.size()>0){
						for(int i=0;i<in.players.size();i++){
							log.info("[CHECKALLINSTANCESSTATE_PLAYERS]PLAYERID["+in.players.get(i)+"]");
						}
					}
				}
			}
			
			if(currentWeek==2&&Time.currentWeekDay==1){//第二周周日常规赛结束后更新争霸赛数据
				finalsPlayers.clear();
				//更新争霸榜
				for(StepBattleScore sbs : top16){
					if(sbs!=null){
						StepBattleScoreTop16 sbs16=new StepBattleScoreTop16();
						sbs16.playerid=sbs.playerid;
						sbs16.accountId=sbs.accountId;
						sbs16.faction=sbs.faction;
						sbs16.gameCode=sbs.gameCode;
						sbs16.name=sbs.name;
						sbs16.winCount=sbs.winCount;
						sbs16.time=sbs.time;
						finalsPlayers.add(sbs16);
						log.info("[STEPBATTLEFINALSPRODUCE]PLAYERID["+sbs.playerid+"]ACC["+sbs.accountId+"]GAMECODE["+sbs.gameCode+"]WINCOUNT["+sbs.winCount+"]TIME["+sbs.time+"]");
					}
				}
				//清空争霸赛表并保存16强到争霸赛表
				Server.server.getServiceRegistry().getDbService().schedule(new StepBattleScoreFinalsSaveCall(null, finalsPlayers,StepBattleScoreFinalsSaveCall.TYPE_FINALS));
			}
			if(currentWeek==1||currentWeek==2){//只有常规赛时才会每天存储常规赛的数据
				for(StepBattleScore score:scores.values()){
					if(score!=null){
						log.info("[SAVEROUTINEPLAYERSCORES]SCORES["+score.playerid+"]ACC["+score.accountId+"]GAMECODE["+score.gameCode+"]WINCOUNT["+score.winCount+"]TIME["+score.time+"]");
					}
				}
				Server.server.getServiceRegistry().getDbService().schedule(new StepBattleScoreCall(null, scores));
			}
			DispatchClientSessionService dispatchClientSessionService = (DispatchClientSessionService) Server.server.getServiceRegistry().getService(DispatchClientSessionService.class);
			dispatchClientSessionService.removeAllClientSessions();
			hasPersisData = true;
		}
		if(Time.currentWeekDay==1){//星期天
			//统计前16名发送奖励,如果今天是周五要发送奖励给前16名玩家
			if((currentWeek == 1 || currentWeek == 2)&&(Time.currentHour>endPvpBattle_everyday_Hour || Time.currentHour==endPvpBattle_everyday_Hour 
					&& Time.currentMin>=endPvpBattle_everyday_Min)){
				if(!hasSendGift){
					StepBattleScoreDao dao = Server.server.getServiceRegistry().getDbService().stepbattlescoreDAO;
					List<StepBattleScore> list = null;
					synchronized (Server.server.getServiceRegistry().getStepBattleService()) {
						list = dao.getTop16Players();
					}
					if(currentWeek==2){
						sendGift(list, 1);
					}else if(currentWeek==1){
						sendGift(list, 0);
					}
					sendTitle_16();
					hasSendGift = true;
				}
			}
			//第三周周日争霸赛完之后清除常规赛数据
			if(!hadClearRoutineData&&currentWeek==3&&(Time.currentHour>routineDataClear_Hour || (Time.currentHour==routineDataClear_Hour
					&&Time.currentMin>=routineDataClear_Min))){
				//清空16强数据库
				Server.server.getServiceRegistry().getDbService().schedule(new StepBattleScoreFinalsSaveCall(null, null,StepBattleScoreFinalsSaveCall.TYPE_TOP16));
				//清空16强缓存
				clearTop16();
				hadClearRoutineData=true;
				log.info("[ROUTINEDATACLEAROK]");
			}
		}
	}
	/**
	 * 更新掉线玩家的排名（掉线即为失败+1)
	 * */
	public void updateDisconnectedPlayerRanking(int playerId,int accountId){
		for(StepBattleScoreTop16 sbs16:finalsPlayers){//称号升级用于排序
			if(playerId==sbs16.playerid&&accountId==sbs16.accountId&&player2Instance.containsKey(playerId)){
				sbs16.ranking+=1;
				log.info("[UPDATEDISCONNECTEDPLAYERRANKING]PLAYERID["+playerId+"]ACCOUNTID["+accountId+"]");
					String disConnetioPlayerInfo=sbs16.playerid+sbs16.gameCode;
					if(!disConnetionPlayers.contains(disConnetioPlayerInfo)){
						disConnetionPlayers.add(disConnetioPlayerInfo);
						log.info("[COUNTISCONNETIONPLAYERS]PLAYERID["+playerId+"]ACCOUNTID["+accountId+"]gamecode["+sbs16.gameCode+"]wincount["+sbs16.winCount+"]ranking["+sbs16.ranking+"]");
					}
				break;
			}
		}
	}
	/**是否是等待下一轮或轮空时掉线*/
	public boolean isPlayerInFinalsInstance(int playerId,int accountId){
		if(instancesFinal.size()>0){
			for(int i=0;i<instancesFinal.size();i++){
				if(instancesFinal.get(i)!=null&&instancesFinal.get(i).players.size()==1){
					if(playerId==players.get(0)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
//	/***
//	 * 所有副本都结束
//	 * @return
//	 */
//	public boolean allInstanceHadEnd(){
//		if(instances.size()==0){
//			return true;
//		}
//		return false;
//	}
	
	/***
	 * 发送奖励
	 * @param giftType 0-周奖励  1-常规赛结束奖励
	 */
	public void sendGift(List<StepBattleScore> list,int giftType){
		Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
		pt.putInt(StepServer.TYPE_BATTLE_SENDGIFT);
		pt.putString("");
		pt.putInt(giftType);
		pt.put(list.size());
		for(StepBattleScore sbs:list){
			pt.putInt(sbs.playerid);
			pt.putInt(sbs.accountId);
			pt.putUTF(sbs.gameCode);
			log.info("[STEPBATTLESENDGIFT]PLAYERID["+sbs.playerid+"]GIFTTYPE["+giftType+"]");
		}
		DispatchPacket dp = new DispatchPacket(0, pt);
		for(IoSession session : Server.server.getServiceRegistry().getStepServer().sessions){
			session.write(dp);
		}
	}
	
	
	/** 移除副本 */
	public void removeInstance(int instanceId){
		Iterator<StepBattleInstance> it = instances.iterator();
		while(it.hasNext()){
			StepBattleInstance instance = it.next();
			if(instance!=null && instance.instanceId==instanceId)
				it.remove();
		}
		Iterator<StepBattleInstanceFinals> it1 = instancesFinal.iterator();
		while(it1.hasNext()){
			StepBattleInstanceFinals instance = it1.next();
			if(instance!=null && instance.instanceId==instanceId)
				it1.remove();
		}
	}
	
	/** 根据顺序获取出生点 */
	public int[] getInitPosition(int order){
		if(getStepBattleType()==StepServer.STEPBATTLE_TYPE_16||getStepBattleType()==StepServer.STEPBATTLE_TYPE_TOURNAMENT){
			if(order%2==0)
				return new int[]{440, 300};
			return new int[]{490, 535};
		}
		return initPosition[order]; 
	}
	
	public CreatureDieCallback creatureDieCallback() {
		return null;
	}

	public DieCallback dieCallback() {
		return stepBattleDieCallBack;
	}

	public void mapChanged(GameMapDefinition mapDef) {
		
	}

	public MoveCallback moveCallback() {
		return this.moveCallBack;
	}

	public void outPrison(Player p) {
		if(p==null)
			return;
		if(p.map.map!=null){
		    int[] pos = p.map.map.mapDef.mapInfo.getPathFinder().tryOutPrison(p.x, p.y);
		    if(pos==null){
				int[] relivePoint = p.map.map.getRelivePoint(p.faction);
				try{
					int oldMapId = p.map.map.getId();
					int oldX = p.x;
					int oldY = p.y;
					p.goMap(relivePoint[0], relivePoint[1], relivePoint[2]);
					Server.server.getEventManager().fireEvent(
						new ServiceEvent(ServiceEvent.EVENT_PLAYER_OUTPRISON_RELIVEPOINT,
						p,oldMapId,oldX,oldY));
				}catch(VMapException e) {
				}
			}else{
			    try{
					p.goMap(p.map.map.getId(), pos[0], pos[1]);
				}catch (VMapException e) {
				}
			}
		}
	}

	public void removeFromMap(Player player) {
		if(player!=null)
			player.getVMap().instance.removePlayer(player);
		log.info("[STEPTREMOVEPLAYER]PLAYERID["+player.id+"]");
	}

	public void shutdown() {
		saveCurrentCWeek(currentWeek);
		Server.server.getServiceRegistry().getDbService().schedule(new StepBattleScoreCall(null, scores));
		Server.server.getServiceRegistry().getDbService().schedule(new StepBattleScoreFinalsSaveCall(null, finalsPlayers,StepBattleScoreFinalsSaveCall.TYPE_SAVE));
	}
	
	/**根据当前时间获取跨服战类型*/
	public int getStepBattleType(){
		int currHour = Time.currentHour;
		int currMin = Time.currentMin;
		if((currHour>sign_everyday_StartHour || currHour==sign_everyday_StartHour && currMin>=sign_everyday_StartMin) 
				&& (currHour<endPvpBattle_everyday_Hour || currHour==endPvpBattle_everyday_Hour && currMin<=endPvpBattle_everyday_Min)&&currentWeek<3){
			return StepServer.STEPBATTLE_TYPE_16;
		}else if((currHour>startHour || currHour==startHour && currMin>=startMin) 
				&& (currHour<instanceEndHour || currHour==instanceEndHour && currMin<=instanceEndMin)){
			return StepServer.STEPBATTLE_TYPE_NORMAL;
		}else if(currentWeek==3&&Time.currentWeekDay==1&&(currHour>signFinals_Hour||currHour==signFinals_Hour&&currMin>=signFInals_Min)){
			return StepServer.STEPBATTLE_TYPE_TOURNAMENT;
		}
		return -1;
	}

	public void dayChanged() {
		todaySigns.clear();
		queues.clear();
		queues_Fangshi.clear();
		queues_EveryDay.clear();
		todaySigns_EveryDay.clear();
		todaySigns_Finlas.clear();
		todaySignedTimes.clear();//常规赛当天报名清除
		hadSendChat=false;
		firstCheckQueuesFinals=true;
		enterIndtanceRecord.clear();
		hasPersisData = false;
		hasSendGift = false;
		winnerAndSecondId.clear();//清空冠亚军
		noPlayerEnterBattle=false;
		todayEnteredInstancePlayers.clear();
		finalsEnd=false;
		chatFinalsBattle=false;//是否发送通知
		hadClearRoutineData=false;
		hadCheckAllFinalPlayersOk=false;//没有检测争霸赛玩家状态
		signUpPlayers.clear();
		if(scores!=null){
			scores.clear();
		}
		log.info("[DAYCHANGED0]CURRENTWEEK["+currentWeek+"]");
		if(Time.currentWeekDay==2){//每周周一才对currentWeek操作，
			++currentWeek;
			if(currentWeek>3){
				currentWeek=1;
			}
			saveCurrentCWeek(currentWeek);
		}
		log.info("[DAYCHANGED1]CURRENTWEEK["+currentWeek+"]");
	}
	//清空16强缓存
	public void clearTop16(){
		for(int i=0;i<top16.length;i++){
			if(top16[i]!=null){
				log.info("[CLEARTOP16]PLAYERID["+top16[i].playerid+"]ACCOUNTID["+top16[i].accountId+"]GAMECODE["+top16[i].gameCode+"]WINCOUNT["+top16[i].winCount+"]TIME["+top16[i].time+"]");
				top16[i]=null;
			}
		}
	}
	
	public static String getServerName(String gameCode){
		if(Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_PIP) && serverNames.containsKey(gameCode))
			return serverNames.get(gameCode);
		else if(Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_TW) && serverNamesOfTwaiwan.containsKey(gameCode))
			return serverNamesOfTwaiwan.get(gameCode);
		return "测试服务器";
	}
}
