package peony.game.asyncbattle;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import peony.game.CoolDownList;
import peony.game.CreatureDieCallback;
import peony.game.DayListener;
import peony.game.DieCallback;
import peony.game.GameMapDefinition;
import peony.game.GameObject;
import peony.game.Horse;
import peony.game.MoveCallback;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapManager;
import peony.game.VMapReference;
import peony.game.VMapUtil;
import peony.game.attendant.Attendant;
import peony.game.attendant.AttendantFixService;
import peony.game.chat.ChatService;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.account.Account;
import peony.service.cards.Cards;
import peony.service.player.PlayerService;
import peony.util.IntHashMap;

/**
 * 异步竞技场服务
 * @author dchen
 */
public class AsyncBattleService implements Service, Runnable, ServiceEventListener, DayListener, VMapManager {
	private static final Logger log = Logger.getLogger(AsyncBattleService.class);
	
	protected AtomicInteger IDS = new AtomicInteger(0); //ID生成器
	public static int MINLEVEL = 40; //进榜的最低级别
	public static int battleMap = 2240; //挑战场景
	
	
	public static int HERO_RANK_TOP10=10;//前10名
	
	//积分规则{0}
	public static int BATTLECOUNT=5;//每天免费可挑战次数
	
	public static int BATTLECOUNT_MAX=10;//总次数
	
	public static int SCORE_WIN=10;//胜利获得积分
	
	public static int SCORE_LOSE=5;//失败获得积分
	
	public static int SCORE_LIMIT=100;//每天积分上限
	
	//奖励规则
	public static int TOTAL_REWARD_COUNT=5;//累计奖励次数
	public static int SCORES=4;//积分
	
	public static int REWARD_TYPE_COUNT=7;//奖励类型数量目前为7可加到20
	
	
	public static int IMONEY_BATTLE=5;//挑战花费元宝数量
	
	public static int BATTLEINFOS_COUNT=10;//玩家挑战信息存储数量
	
	public static int CHANGEGROUPNEEDCREDIT=50;//换一批需要的战功数
	
	
	//积分和奖励统一格式为{0,0,0,0,0}
	public static final String ASYNCBATTLEPROPERTY="ASYNCBATTLEPROP";
	
	public static int OFFICERS_NEEDSCORE[]={//官职需要达到的积分
		0,
		28785,
		26990,
		25245,
		23555,
		21925,
		20345,
		18825,
		17360,
		15950,
		14600,
		13305,
		12065,
		10885,
		9760,
		8695,
		7685,
		6735,
		5845,
		5015,
		4245,
		3535,
		2890,
		2300,
		1780,
		1315,
		920,
		590,
		300,
		125,
		25,
	};
	
	public static int[][] OFFICER_REWARD={//官职奖励
		{659,4,660,4,4880,3},
		{659,4,660,4,4880,3},
		{659,4,660,4,4880,3},
		{659,4,660,4,4880,2},
		{659,4,660,4,4880,2},
		{659,4,660,4,4880,2},
		{659,4,660,4,4880,1},
		{659,4,660,4,4880,1},
		{659,4,660,4,4880,1},
		{659,4,660,4,4863,3},
		{659,4,660,4,4863,3},
		{659,4,660,4,4863,3},
		{659,4,660,4,4863,2},
		{659,4,660,4,4863,2},
		{659,4,660,4,4863,2},
		{659,4,660,4,4863,1},
		{659,4,660,4,4863,1},
		{659,4,660,4,4863,1},
		{659,4,660,4},
		{659,4,660,4},
		{659,4,660,4},
		{659,3,660,3},
		{659,3,660,3},
		{659,3,660,3},
		{659,2,660,2},
		{659,2,660,2},
		{659,2,660,2},
		{659,1,660,1},
		{659,1,660,1},
		{659,1,660,1},
		{}
	};
	
	public static String[] OFFICER_NAME={
		"丞相",
		"御史大夫",
		"太尉",
		"太常",
		"光禄勋",
		"卫尉",
		"太仆",
		"廷尉",
		"大鸿胪",
		"宗正",
		"大司农",
		"少府",
		"京北尹",
		"右扶风",
		"左冯翊",
		"郡守",
		"都尉",
		"郡丞",
		"长史",
		"功曹史",
		"县令",
		"县尉",
		"县丞",
		"主簿",
		"廷掾",
		"乡长",
		"里长",
		"亭长",
		"什长",
		"伍长",
		"新兵"
	};
	/***
	 * 首进奖励
	 */
	public static int[][] REWARD_FIRSTENTER={
		{4118,1,4900,1},//首次进入500
		{4138,1,4787,1,4901,1},// 首次进入200
		{4140,1,4787,2,4902,1},// 首次进入100
		{4140,2,4787,3,4903,1,4899,1},// 首次进入 50
		{},
		{},
	};
	/***
	 * 累计奖励
	 */
	public static int[][] REWARD_TOTAL={
		{4742,25,4893,10,4898,6,4905,1}, //累计5天第1名
		{4742,15,4893,4,4898,3}, //累计5天前10名
		{4742,10,4893,1,4898,1}, //累计5天前50名
	};
	
	public static String rewardFirstEnter[]={
			//标题
			"首进前500",
			"首进前200",
			"首进前100",
			"首进前50",
			"首进前10",
			"首次成为第1",
			//说明
			"首次进入前500名。",
			"首次进入前200名。",
			"首次进入前100名。",
			"首次进入前50名。",
			"首次进入前10名。",
			"首次成为第1名。",
	};
	public static String rewardTotal[]={
			"累计5天第1名",
			"累计5天前10名",
			"累计5天前50名",
			"累计5天每天0点结算时名次都为第1名。",
			"累计5天每天0点结算时名次都为前10名内。",
			"累计5天每天0点结算时名次都为前50名内。",
	};
	
	/**
	 * vip每天额外可购买次数
	 */
	public static int[] vipBuyCount={
		0,
		0,
		0,
		5,
		5,
		10,
		10,
		15,
		15,
		20,
		20,
		25,
	};
	
	protected HashMap<Integer, Integer> cacheTimeInfo = new HashMap<Integer, Integer>(); //缓存10分钟
	protected IntHashMap<Player> playerInfoCache = new IntHashMap<Player>(); //缓存10分钟
	
	protected HashMap<Integer, AsyncNormalBoard> rank2boards = new HashMap<Integer, AsyncNormalBoard>();
	protected HashMap<Integer, AsyncNormalBoard> id2boards = new HashMap<Integer, AsyncNormalBoard>();
	
	public static final String PROPERTY_ENTER_MAP = "ASYNCENTERMAP";
	public static final String PROPERTY_ENTER_X = "ASYNCENTERX";
	public static final String PROPERTY_ENTER_Y = "ASYNCENTERY";
	
	public static int[] removeBuffs = new int[]{287,288,289,290,291,292,293,294,295,296,297,298,299,300,
		301,302,303,304,305,306,307,308,309,310,311,312,313,314,315,316,317,318,319,320,321,322,323,324,
		325,326,327,328,329,330,331,332,333,334,335,164,119,120,121,122,205,206,207,208,415,394,131,132,
		133,134};
	
	public static int[] canNotUseItem = new int[]{856,678,1100,1102,1101,1197,1198,1199,1200,1948,1949,
		1950,1951,4258,4259,4260,4261,4868,4869,4870,4871,4872,4873,4874,4875,4876,4877,4878,4879,4880,
		4881,4882,4883,4884,2070,2071,2072,2073,2074,2075,2076,2077,2078,2079,2080,2081,2082,2083,2084,
		2085,2086,2087,2088,2089,2090,2091,2092,2093,2094,2095,2096,2097,2098,2099,2100,2101,20102,2103,
		2104,2105,2106,2107,2108,2109,2110,2111,2112,2113,2114,2115,2116,2117,4202,4203,4204,4205,4206,
		4207,4208,4209,4210,4211,4212,2653,2655,2656,2657,2658,2653,3500,2285,1109,4704,1242,4904};
	
	public HashMap<Integer, AsyncNormalBoard> getRank2boards() {
		return rank2boards;
	}
	
	/**上升最快排名限制数量*/
	public static final int TOPLISTMAX=3;
	
	protected List<AsyncNormalBoard> tops=new ArrayList<AsyncNormalBoard>();
	
	public List<AsyncNormalBoard> getTops() {
		return tops;
	}

	protected IntHashMap<VMap> maps = new IntHashMap<VMap>();
	protected HashMap<Integer, Integer> map2starttime = new HashMap<Integer, Integer>();
	
	public int checkSaveNormalBoardTime;
	
	private DieCallback dieCallBack = new AsyncBattleDieCallBack();
	
	public void startup() throws Exception {
		new Thread(this).start();
		Server.server.getEventManager().registerListener(this);
		Time.addDayListener(this);
		Server.server.getWorld().addVMapManager(this);
		Server.server.getWorld().registerVMapManager(battleMap, this);
		loadAsyncNormalBoards();
	}
	
	protected void loadAsyncNormalBoards(){
		AsyncNormalBoardDao dao=Server.server.getServiceRegistry().getDbService().asyncnormalboardDao;
		List<AsyncNormalBoard> list=dao.getAsyncNormalBoardList();
		for(AsyncNormalBoard board:list){
			board.transToNewAchieve();
			id2boards.put(board.playerId, board);
			if(!rank2boards.containsKey(board.rank)){
				rank2boards.put(board.rank, board);
			}else{
				try{
					StringBuffer sb=new StringBuffer();
					sb.append("ACHIEVEMENTSTATE[");
					for(int i=0;i<board.achievementStateNew.length;i++){
						String flag=",";
						if(i==0){
							flag="";
						}
						sb.append(flag+board.achievementStateNew[i]);
					}
					sb.append("]");
					log.info("[LOADASYNCNORMALBOARDS_DUPLICATE]PLAYERID["+board.playerId+"]RANK["+board.rank+"]UPRANK["+board.upRank+"]FACTION["+board.faction+"]UPRANKTIME["+board.upRankTime+"]LOGINDAY["+board.loginDay+"]CLAZZ["+board.clazz+"]LEVEL["+board.level+"]BATTLECOUNT["+board.battleCount+"]OFFICERINDEX["+board.officerIndex+"]DAYFLAG["+board.dayFlag+"]DAYFLAG_GETREWARDTIME["+board.dayFlag_GetRewardTime+"]OFFICERSCORE["+board.officerScore+"]"+sb.toString());
				}catch(Exception e){
				}
			}
		}
		if(list.size()>0){
			for(int i=0;i<list.size();i++){
				if(i>=TOPLISTMAX){
					break;
				}
				AsyncNormalBoard ab=list.get(i);
				if(ab!=null&&ab.upRank>0){
					tops.add(ab);
				}
			}
		}
	}
	
	//判断是否已在榜单中
	public boolean hasInBoard(int playerId){
		return id2boards.containsKey(playerId);
	}
	
	public AsyncNormalBoard getAsyncNormalBoardByPlayerId(int playerId){
		synchronized (this) {
			return id2boards.get(playerId);
		}
	}
	
	public AsyncNormalBoard getAsyncNormalBoardByRank(int rank){
		synchronized (this) {
			return rank2boards.get(rank);
		}
	}
	
	//可能操作数据库，所以必须要在非主线程中调用
	public synchronized Player getPlayerInfo(int playerId){
		PlayerService playerService = Server.server.getServiceRegistry().getPlayerService();
		Player player = null;
		if(ObjectAccessor.getPlayer(playerId)==null && playerService.getFromCache(playerId)==null){
			player = playerInfoCache.get(playerId);
		}
		try {
			if(player==null){
				player = createBody(playerId);
				if(player!=null)
					addPlayerInfoCache(player);
			}
		} catch (Exception e) {
			
		}
		return player;
	}
	
	private synchronized void addPlayerInfoCache(Player player){
		playerInfoCache.put(player.id, player);
		cacheTimeInfo.put(player.id, Time.currTime);
	}
	
	//用于实时更新前三名上升最快玩家
	public void checkTops(AsyncNormalBoard anb){
		if(tops.size()<TOPLISTMAX){
			int flag=0;
			for(int i=0;i<tops.size();i++){
				AsyncNormalBoard ab=tops.get(i);
				if(ab.playerId==anb.playerId){
					flag++;
					if(anb.upRank>ab.upRank){
						ab.upRank=anb.upRank;
						ab.level=anb.level;
					}
				}
			}
			if(flag==0){
				tops.add(anb);
			}
		}else{
			int flag=0;
			for(int i=tops.size()-1;i>=0;i--){
				AsyncNormalBoard c=tops.get(i);
				if(anb.playerId==c.playerId){
					flag++;
					if(anb.getUpRank()>c.getUpRank()){
						tops.set(i, anb);
						break;
					}
				}
			}
			if(flag==0){
				for(int i=tops.size()-1;i>=0;i--){
					AsyncNormalBoard c=tops.get(i);
					if(anb.getUpRank()>c.getUpRank()||(anb.upRank==c.upRank&&anb.upRankTime<c.upRankTime)){
						tops.set(i, anb);
						break;
					}
				}	
			}
		}
		for(int i=0;i<tops.size();i++){
			for(int j=0;j<tops.size();j++){
				AsyncNormalBoard a=tops.get(i);
				AsyncNormalBoard b=tops.get(j);
				if(a.upRank>b.upRank||(a.upRank==b.upRank&&a.upRankTime<b.upRankTime)){
					tops.set(i, b);
					tops.set(j,a);
				}
			}
		}
	}
	
	//新进榜单
	public void addBoard(Player player){
		synchronized (this) {
			if(player!=null){
				int maxRank = getMaxRank();
				AsyncNormalBoard board = new AsyncNormalBoard();
				board.setName(player.name);
				board.level=player.level;
				board.clazz=(byte)player.clazz;
				board.faction=player.faction;
				board.setPlayerId(player.id);
				board.setRank(maxRank+1);
				board.loginDay = Time.day;
				board.checkFirstEnterReward();
				id2boards.put(board.playerId, board);
				rank2boards.put(board.getRank(), board);
			}
		}
	}
	
	//更新榜单
	public void updateBoard(Player winner,Player loser,boolean winnerIsAttacker){
		synchronized (this) {
			if(winner!=null&&loser!=null){
				//int role, boolean win, int rank, Date date, int targetId
				List<AsyncNormalBoard> changeList=new ArrayList<AsyncNormalBoard>();
				AsyncNormalBoard aWin=id2boards.get(winner.id);
				AsyncNormalBoard sLose=id2boards.get(loser.id);
				log.info("[ASYNCBATTLESERVICEUPDATEBOARD_BEFORE]WINNER["+winner.id+"]RANK["+aWin.rank+"]LOSER["+loser.id+"]RANK["+sLose.rank+"]");
//				System.out.println("======================:"+winner.battleType+" "+loser.battleType);
//				System.out.println("----------------------:"+aWin.rank+"    "+sLose.rank);
				sLose.lastBattleResult=AsyncNormalBoard.BATTLERESULT_LOSE;
				aWin.lastBattleResult=AsyncNormalBoard.BATTLERESULT_WIN;
				if(winner.battleType==Player.TYPE_ASYNC_PLAYER){//如果胜利者是机器人
					AsyncBattleInfo bInfo=new AsyncBattleInfo(winnerIsAttacker?AsyncBattleInfo.ROLE_SOURCE:AsyncBattleInfo.ROLE_TARGET,true,AsyncBattleInfo.BATTLERESULT_NORMAL,aWin.rank,Time.currDate,sLose.playerId);
					aWin.battleInfos.add(bInfo);
					if(aWin.battleInfos.size()>10){
						aWin.battleInfos.remove(0);
					}
					AsyncBattleInfo bInfo1=new AsyncBattleInfo((!winnerIsAttacker)?AsyncBattleInfo.ROLE_SOURCE:AsyncBattleInfo.ROLE_TARGET,false,AsyncBattleInfo.BATTLERESULT_NORMAL,sLose.rank,Time.currDate,aWin.playerId);
					sLose.battleInfos.add(bInfo1);
					if(sLose.battleInfos.size()>10){
						sLose.battleInfos.remove(0);
					}
//					if(sLose.officerScore_Day<SCORE_LIMIT){
						sLose.officerScore_Day+=SCORE_LOSE;
						sLose.officerScore+=SCORE_LOSE;
//					}
					return;
				}
				if(aWin.rank<sLose.rank){
					AsyncBattleInfo bInfo=new AsyncBattleInfo(winnerIsAttacker?AsyncBattleInfo.ROLE_SOURCE:AsyncBattleInfo.ROLE_TARGET,true,AsyncBattleInfo.BATTLERESULT_NORMAL,aWin.rank,Time.currDate,sLose.playerId);
					aWin.battleInfos.add(bInfo);
					if(aWin.battleInfos.size()>10){
						aWin.battleInfos.remove(0);
					}
					AsyncBattleInfo bInfo1=new AsyncBattleInfo((!winnerIsAttacker)?AsyncBattleInfo.ROLE_SOURCE:AsyncBattleInfo.ROLE_TARGET,false,AsyncBattleInfo.BATTLERESULT_NORMAL,sLose.rank,Time.currDate,aWin.playerId);
					sLose.battleInfos.add(bInfo1);
					if(sLose.battleInfos.size()>10){
						sLose.battleInfos.remove(0);
					}
//					if(aWin.officerScore_Day<SCORE_LIMIT){
						aWin.officerScore_Day+=SCORE_WIN;
						aWin.officerScore+=SCORE_WIN;
//					}
					return;
				}
//				if(aWin.officerScore_Day<SCORE_LIMIT){
					aWin.officerScore_Day+=SCORE_WIN;
					aWin.officerScore+=SCORE_WIN;
//				}
				int rankLose=sLose.rank;
				int rankWin=aWin.rank;
				int upRank=rankWin-rankLose;
				AsyncNormalBoard aWin_Rank=rank2boards.get(rankWin);
				AsyncNormalBoard sLose_Rank=rank2boards.get(rankLose);
				aWin_Rank.setRank(sLose_Rank.rank);
				for(int i=rankWin-1;i>=rankLose;i--){
					AsyncNormalBoard anbTemp=rank2boards.get(i);
					anbTemp.setRank(anbTemp.getRank()+1);
					rank2boards.put(anbTemp.rank,anbTemp);
					if(!changeList.contains(anbTemp)){
						changeList.add(anbTemp);
					}
				}
				rank2boards.put(aWin_Rank.rank, aWin_Rank);
				aWin_Rank.setUpRank(aWin_Rank.getUpRank()+upRank);
//				System.out.println("-------------------uprank:"+aWin_Rank.name+"  "+aWin_Rank.getUpRank());
				if(!changeList.contains(aWin_Rank)){
					changeList.add(aWin_Rank);
				}
				checkTops(aWin_Rank);//是否进入前3
				for(AsyncNormalBoard ab:changeList){
					id2boards.put(ab.playerId, ab);
				}
				AsyncBattleInfo bInfo=new AsyncBattleInfo(winnerIsAttacker?AsyncBattleInfo.ROLE_SOURCE:AsyncBattleInfo.ROLE_TARGET,true,AsyncBattleInfo.BATTLERESULT_WIN,aWin_Rank.rank,Time.currDate,sLose.playerId);
				aWin_Rank.battleInfos.add(bInfo);
				if(aWin_Rank.battleInfos.size()>10){
					aWin_Rank.battleInfos.remove(0);
				}
				AsyncBattleInfo bInfo1=new AsyncBattleInfo((!winnerIsAttacker)?AsyncBattleInfo.ROLE_SOURCE:AsyncBattleInfo.ROLE_TARGET,false,AsyncBattleInfo.BATTLERESULT_LOSE,sLose_Rank.rank,Time.currDate,aWin_Rank.playerId);
				sLose_Rank.battleInfos.add(bInfo1);
				if(sLose_Rank.battleInfos.size()>10){
					sLose_Rank.battleInfos.remove(0);
				}
				log.info("[ASYNCBATTLESERVICEUPDATEBOARD_AFTER]WINNER["+winner.id+"]RANK["+aWin_Rank.rank+"]LOSER["+loser.id+"]RANK["+sLose_Rank.rank+"]");
				aWin_Rank.checkFirstEnterReward();
//				if(aWin_Rank.rank<=10&&aWin_Rank.rank!=aWin_Rank.oldRank){//进入前10名发送世界聊
//					ChatService chat=Server.server.getServiceRegistry().getChatService();
//					chat.sendWorldMessage(MessageFormat.format("{0}在擂台中所向披靡，成功击败了对手晋升为擂台的第{1}名。", aWin_Rank.name,aWin_Rank.rank));
//				}
			}
		}
//		for(AsyncNormalBoard sb:id2boards.values()){
//			System.out.println("id2boards:"+sb.name+" "+sb.rank);
//		}
//		System.out.println("=====================================");
//		for(AsyncNormalBoard sb:rank2boards.values()){
//			System.out.println("rank2boards:"+sb.name+" "+sb.rank);
//		}
//		System.out.println("=====================================");
	}
	
	//获取榜单目前最大人数
	public int getMaxRank(){
		synchronized (this) {
			return rank2boards.size();
		}
	}
	
	//获取可挑战者名单(返回可挑战的名次)
	public int[] getChallenges(int selfRank){
		synchronized (this) {
			if(selfRank>5){
				int[] arr = new int[5];
				arr[0] = (int) (selfRank - 5d * Math.pow(Math.max(selfRank/100d, 1d), 1.7d));
				arr[1] = (int) (selfRank - 4d * Math.pow(Math.max(selfRank/100d, 1d), 1.5d));
				arr[2] = (int) (selfRank - 3d * Math.pow(Math.max(selfRank/100d, 1d), 1.3d));
				arr[3] = selfRank - 2;
				arr[4] = selfRank - 1;
				return arr;
			}else{
//			for(AsyncNormalBoard ab:rank2boards.values()){
//				System.out.println("=========================:"+ab.name+" "+ab.rank);
//			}
				List<Integer> list=new ArrayList<Integer>();
				int count=rank2boards.size();
				if(count>5){
					count=6;
				}
				int[] arr = null;
				for(int i=0;i<count;i++){
					if(selfRank==i+1){
						continue;
					}
					int rank= i + 1;
//				System.out.println("----------------------------:"+selfRank+"   "+rank);
					list.add(rank);
				}
				arr=new int[list.size()];
				for(int i=0;i<arr.length;i++){
					arr[i]=list.get(i);
				}
				return arr;
			}
		}
	}
	
	//创建机器人
	public static Player createBody(int playerId) throws Exception {
		Player source = Server.server.getServiceRegistry().getPlayerService().loadAsyncPlayerSilent(playerId);
		if(source==null)
			throw new Exception("挑战者不存在");
		Player body = new Player();
		body.id = source.id;
		body.battleIngoPlayer = true;
		body.instanceId = source.instanceId;
		body.moveType = source.moveType;
		body.type = GameObject.TYPE_PLAYER;
		body.name = source.name;
		body.sex = source.sex;
		body.level = source.level;
		body.clazz = source.clazz;
		body.faction = source.faction;
		body.maxhp = source.maxhp;
		body.maxmp = source.maxmp;
		body.hp = source.hp;
		body.mp = source.mp;
		body.strength = source.strength;
		body.agility = source.agility;
		body.stamina = source.stamina;
		body.intellect = source.intellect;
		body.attackpowerup = source.attackpowerup;
		body.attackpowerdown = source.attackpowerdown;
		body.spellpower = source.spellpower;
		body.spellheal = source.spellheal;
		body.defense = source.defense;
		body.spelldefense = source.spelldefense;
		body.critical = source.critical;
		body.spellcritical = source.spellcritical;
		body.hit = source.hit;
		body.spellhit = source.spellhit;
		body.dodge = source.dodge;
		body.spelldodge = source.spelldodge;
		body.anticrit = source.anticrit;
		body.defensePercent = source.defensePercent;
		body.healthrestore = source.healthrestore;
		body.manarestore = source.manarestore;
		body.skillPoint = source.skillPoint;
		body.propertyPoint = source.propertyPoint;
		body.exp = source.exp;
		body.state = source.state;
		body.setGuildName(source.getGuildName());
		body.strengthAdded = source.strengthAdded;
		body.agilityAdded = source.agilityAdded;
		body.staminaAdded = source.staminaAdded;
		body.intellectAdded = source.intellectAdded;
		body.equipments = source.equipments.clone();
		body.buffs = source.buffs.clone();
		body.buffs.owner = body;
		body.titles = source.titles.clone();
		body.skills = source.skills.clone();
		body.cards = (Cards) source.cards.clone();
		body.books = source.books.clone();
		body.pool = source.pool.clone();
		body.warState = source.warState;
//		body.attendantBag=source.attendantBag.clone();
		body.attendantBag.owner = body;
		int attendantInstanceId = source.pool.getInt(Player.PROPERTY_LAST_ATTENDANT_INSTANCEID);
		if(attendantInstanceId!=0&&source.attendantBag!=null&&source.attendantBag.getAttendant(attendantInstanceId)!=null){
			Attendant attendant = source.attendantBag.getAttendant(attendantInstanceId).clone();
			if(attendant!=null){
				attendant.owner=body;
				body.attendantView=attendant;
				body.attendantBag.addAttendant(attendant);
			}
		}
		body.horseBag=source.horseBag.clone();
		body.horseBag.owner = body;
		int horseInstanceId=source.pool.getInt(Player.PROPERTY_LAST_HORSE_INSTANCEID);
		if(horseInstanceId!=0&&source.horseBag!=null&&source.horseBag.getHorse(horseInstanceId)!=null){
			Horse horse=source.horseBag.getHorse(horseInstanceId).clone();
			body.horse=horse;
			body.horse.ride(body);
			body.ride();
		}
		body.coolDowns = new CoolDownList();
		body.initBuffs();
		if(!source.isAlive()){
			body.moveType = Player.MOVE_ALL;
			body.state &= Player.MASK_CLEAR;
		}
		body.setWeekCredit(source.getWeekCredit());
		body.setCredit(source.getCredit(),"CREATEBODY");
		body.activePower=source.activePower;
		body.setRank(source.getRank());
		return body;
	}
	
	public void shutdown() {
		AsyncNormalBoardDao dao=Server.server.getServiceRegistry().getDbService().asyncnormalboardDao;
		for(AsyncNormalBoard board:id2boards.values()){
			if(board==null)
				continue;
			AsyncNormalBoard boardTemp=dao.getAsyncNormalBoardById(board.playerId);
			if(boardTemp==null){
				dao.newEntity(board);
			}else{
				dao.updateEntity(board);
			}
		}
		Server.server.getEventManager().unregisterListener(this);
	}

	public void run() {
		while(true){
			synchronized (this) {
				List<Integer> list = new ArrayList<Integer>();
				for(int id : cacheTimeInfo.keySet()){
					int time = cacheTimeInfo.get(id);
					if(Time.currTime > time + 600000)
						list.add(id);
				}
				for(int id : list){
					cacheTimeInfo.remove(id);
					playerInfoCache.remove(id);
				}
			}
			try {
				Thread.sleep(600000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD,
				ServiceEvent.EVENT_PLAYER_LEVELUP,
				ServiceEvent.EVENT_PLAYER_LOGOUTED,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			processPlayerFirstLoaded((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_LEVELUP:
			processPlayerLevelUp((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_LOGOUTED:
			processPlayerLogOut((Player)event.param1);
			clearPlayerState((Player)event.param1);
			break;
		}
	}
	
	protected void processPlayerLogOut(Player player){
		if(player!=null&&player.map.id==battleMap){
			//处理掉线玩家
			int playerId=player.id;
			AsyncNormalBoard sLose=getAsyncNormalBoardByPlayerId(playerId);
			AsyncNormalBoard aWin=getAsyncNormalBoardByPlayerId(player.asyncTargetId);
			if(player.battleType!=Player.TYPE_ASYNC_PLAYER){//不是机器人
				if(aWin!=null&&sLose!=null){
					sLose.lastBattleResult=AsyncNormalBoard.BATTLERESULT_LOSE;
					AsyncBattleInfo bInfo=new AsyncBattleInfo(AsyncBattleInfo.ROLE_SOURCE,true,AsyncBattleInfo.BATTLERESULT_NORMAL,aWin.rank,Time.currDate,sLose.playerId);
					aWin.battleInfos.add(bInfo);
					if(aWin.battleInfos.size()>10){
						aWin.battleInfos.remove(0);
					}
					AsyncBattleInfo bInfo1=new AsyncBattleInfo(AsyncBattleInfo.ROLE_TARGET,false,AsyncBattleInfo.BATTLERESULT_NORMAL,sLose.rank,Time.currDate,aWin.playerId);
					sLose.battleInfos.add(bInfo1);
					if(sLose.battleInfos.size()>10){
						sLose.battleInfos.remove(0);
					}
//					if(sLose.officerScore_Day<SCORE_LIMIT){
						sLose.officerScore_Day+=SCORE_LOSE;
						sLose.officerScore+=SCORE_LOSE;
//					}
				}
				int asyncTarget = player.asyncTargetId;
				Player target = null;
				if(ObjectAccessor.getAsyncGameObject(asyncTarget, player.asyncMapInstanceId)!=null){
					target = (Player) ObjectAccessor.getAsyncGameObject(asyncTarget, player.asyncMapInstanceId);
					if(target.attendant!=null){
						target.attendant.removeFromWorld();
					}
					target.removeFromMap();
					ObjectAccessor.asyncPlayers.remove(AsyncPlayer.getSearchKey(asyncTarget, player.asyncMapInstanceId));
				}
			}
		}
	}
	
	private void processPlayerFirstLoaded(Player player){
		synchronized (this) {
			if(player!=null){
				if(player.level>=MINLEVEL && !hasInBoard(player.id)){
					addBoard(player);
				}else if(hasInBoard(player.id)){
					AsyncNormalBoard board=getAsyncNormalBoardByPlayerId(player.id);
					if(board!=null){
						board.level=player.level;
						board.name=player.name;
					}
				}
			}
		}
	}
	
	private void processPlayerLevelUp(Player player){
		synchronized (this) {
			if(player!=null){
				if(player.level>=MINLEVEL && !hasInBoard(player.id)){
					addBoard(player);
				}
			}
		}
	}

	public void dayChanged() {
		//结算每天晋升最快榜单人员名单，并发放奖励
		ChatService service=Server.server.getServiceRegistry().getChatService();
		StringBuffer sb=new StringBuffer();
		for(AsyncNormalBoard anb:tops){
			if(sb.toString().equals("")){
				sb.append(anb.getName());
			}else{
				sb.append("、"+anb.getName());
			}
		}
		tops.clear();
		if(!sb.toString().equals("")){
			service.sendWorldMessage(MessageFormat.format("{0}在擂台中努力拼搏，成为了今日的晋级之星", sb.toString()));
		}
		synchronized (this) {
			for(AsyncNormalBoard board:id2boards.values()){
				if(board==null)
					continue;
				board.checkTotalRewardState();
				board.resetParaForDay();
			}
		}
		savePlayerScore();
		
	}
	
	public HashMap<Integer, AsyncNormalBoard> getId2boards() {
		synchronized (this) {
			return id2boards;
		}
	}

	public void setId2boards(HashMap<Integer, AsyncNormalBoard> id2boards) {
		synchronized (this) {
			this.id2boards = id2boards;
		}
	}

	public void savePlayerScore(){
		Server.server.getServiceRegistry().getDbService().schedule(new AsyncBattleScoreSaveCall(null));
	}

	public VMap addToMap(Player player, int mapId, int x, int y, boolean check)
			throws VMapException {
		if(check){
			int outM = player.pool.getInt(PROPERTY_ENTER_MAP);
			int outX = player.pool.getInt(PROPERTY_ENTER_X);
			int outY = player.pool.getInt(PROPERTY_ENTER_Y);
			if(outM==0){
//				魏国：地图id:272  x=411 y=849
//				蜀国：地图id：240 x=565 y=388
//				吴国：地图id：352 x=762 y=785
				if(player.faction==Player.FACTION_WEI){
					outM=272;
					outX=411;
					outY=849;
				}else if(player.faction==Player.FACTION_SHU){
					outM=240;
					outX=565;
					outY=388;
				}else if(player.faction==Player.FACTION_WU){
					outM=352;
					outX=762;
					outY=785;
				}
				return Server.server.getWorld().addPlayerToMap(player, outM, outX, outY, check);
			}
			player.pool.remove(PROPERTY_ENTER_MAP);
			player.pool.remove(PROPERTY_ENTER_X);
			player.pool.remove(PROPERTY_ENTER_Y);
			return Server.server.getWorld().addPlayerToMap(player, outM, outX, outY, check);
		}else{
			VMap map = VMapUtil.create(this, Server.server.getWorld(), mapId, Server.server.revision);
			map.asyncbattleInstanceId = IDS.incrementAndGet();
			player.asyncEnterMapId = player.map.getId();
			player.asyncEnterX = player.x;
			player.asyncEnterY = player.y;
			player.pool.setInt(PROPERTY_ENTER_MAP, player.asyncEnterMapId);
			player.pool.setInt(PROPERTY_ENTER_X, player.asyncEnterX);
			player.pool.setInt(PROPERTY_ENTER_Y, player.asyncEnterY);
			player.removeFromMap();
			map.addPlayer(player, x, y);
			player.buffs.allocationTempBuffs(removeBuffs);
			int targetId = player.asyncTargetId;
			try {
				Player p = AsyncBattleService.createBody(targetId);
				p.buffs.removeSpecialBuff();
				p.battleType = Player.TYPE_ASYNC_PLAYER;
				p.map = new VMapReference();
				p.map.id = player.map.id;
				p.map.setMap(player.map.map);
				map.addPlayer(p, 288, 452);
				int attendantInstanceId = p.pool.getInt(Player.PROPERTY_LAST_ATTENDANT_INSTANCEID);
				if(attendantInstanceId!=0&&p.attendantBag!=null&&p.attendantBag.getAttendant(attendantInstanceId)!=null){
					Attendant attendant = p.attendantBag.getAttendant(attendantInstanceId).clone();
					if(attendant!=null){
						attendant.battleType = Player.TYPE_ASYNC_PLAYER;
						attendant.asyncMapInstanceId = map.asyncbattleInstanceId;
						attendant.owner=p;
						attendant.setHp(attendant.maxhp, false);
						attendant.setMp(attendant.maxmp, false);
						attendant.follow();
						if(!(attendant.loyal<=0 || attendant.hp<=0)){
							AttendantFixService attFixService = Server.server.getServiceRegistry().getAttendantFixService();
							attFixService.addBuffOnFollow(p, attendant);
						}
					}
				}
				int horseInstanceId=p.pool.getInt(Player.PROPERTY_LAST_HORSE_INSTANCEID);
				if(horseInstanceId!=0&&p!=null&&p.horseBag!=null&&p.horseBag.getHorse(horseInstanceId)!=null){
					Horse horse=p.horseBag.getHorse(horseInstanceId).clone();
					p.horse=horse;
					p.horse.ride(p);
					p.ride();
				}
				p.ai = new PlayerBodyAi(p, player);
				p.ai.init();
				player.minorFaction = 1;
				p.minorFaction = 2;
				player.asyncMapInstanceId = map.asyncbattleInstanceId;
				p.asyncMapInstanceId = map.asyncbattleInstanceId;
				p.setSystemState(Player.SYSTEMSTATE_READY);
				AsyncPlayer asyncPlayer = new AsyncPlayer(p.instanceId, map.asyncbattleInstanceId, p, player.id);
				ObjectAccessor.asyncPlayers.put(AsyncPlayer.getSearchKey(targetId, map.asyncbattleInstanceId), asyncPlayer);
				p.refreshProperties(false);
				getAsyncNormalBoardByPlayerId(player.id).battlePlayers.clear();
			} catch (Exception e) {
				throw new VMapException(e.getMessage());
			}
			maps.put(map.asyncbattleInstanceId, map);
			map2starttime.put(map.asyncbattleInstanceId, Time.currTime);
			return map;
		}
	}

	public CreatureDieCallback creatureDieCallback() {
		return null;
	}

	public DieCallback dieCallback() {
		return dieCallBack;
	}

	public void mapChanged(GameMapDefinition mapDef) {
		
	}

	public MoveCallback moveCallback() {
		return null;
	}

	public void outPrison(Player p) {
		
	}

	public void removeFromMap(Player player) {
		if(player!=null && player.battleType!=Player.TYPE_ASYNC_PLAYER){
			player.buffs.restoreTempBuffs();
		}
	}

	public void update(int diff) {
		try {
			for(VMap m : maps.values()){
				m.update(diff);
			}
			List<Integer> endMaps = new ArrayList<Integer>();
			for(int mapInsId : map2starttime.keySet()){
				int startTime = map2starttime.get(mapInsId);
				if(Time.currTime > startTime + 10*60*1000){
					end(mapInsId);
					endMaps.add(mapInsId);
				}
			}
			for(int mapInsId : endMaps){
				removeMap(mapInsId);
			}
			if(Time.currTime-checkSaveNormalBoardTime>60*60*1000){
				checkSaveNormalBoardTime=Time.currTime;
				savePlayerScore();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void removeMap(int asyncMapInstanceId){
		maps.remove(asyncMapInstanceId);
		map2starttime.remove(asyncMapInstanceId);
	}
	
	public void end(int asyncMapInstanceId){
		VMap map = maps.get(asyncMapInstanceId);
		List<GameObject> list = new ArrayList<GameObject>();
		for(GameObject o : map.instanceid2objects.values()){
			if(o!=null && o.type==GameObject.TYPE_PLAYER){
				list.add(o);
			}
		}
		for(GameObject o : list){
			Player p = (Player)o;
			if(p.battleType!=Player.TYPE_ASYNC_PLAYER){
				if(p!=null){
					Account account = p.getAccount();
					if(account!=null){
						String mod = null;
						if(account.getUiModel()!=null)
							mod = account.getUiModel().trim();
						if(mod!=null){
							if(mod.equals("AndroidNew") || mod.equals("AndroidLargeNew") || mod.equals("iOSNewUI") || mod.equals("iOSNewUILarge")){
							}else if(mod.equals("NewUI_AndroidLarge") || mod.equals("NewUI_Android") || mod.equals("NewUI_iOS") || mod.equals("NewUI_iOSLarge")){
							}else{
								Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, "在刚刚的擂台战中您不幸落败了，获得5点积分");
							}
						}else{
						}
					}
				
				}
				processPlayerLogOut(p);
				try {
					if(p.attendant!=null && p.attendant.hp==0)
						p.attendant.setHp(p.attendant.maxhp, false);
					p.goMap(p.asyncEnterMapId, p.asyncEnterX, p.asyncEnterY);
					p.buffs.clearAllBuffs();
					p.initBuffs();
					p.buffs.restoreTempBuffs();
				} catch (VMapException e) {
					e.printStackTrace();
				}
			}else{
				p.removeFromMap();
				ObjectAccessor.asyncPlayers.remove(AsyncPlayer.getSearchKey(p.id, p.asyncMapInstanceId));
				if(p.attendant!=null){
					p.attendant.removeFromWorld();
				}
			}
			clearPlayerState(p);
		}
	}
	
	public void clearPlayerState(Player player){
		if(player!=null){
			player.battleType = 0;
			player.asyncMapInstanceId = 0;
			player.asyncEnterMapId = 0;
			player.asyncEnterX = 0;
			player.asyncEnterY = 0;
			player.minorFaction=0;
			player.asyncLoadFinish = false;
		}
	}

	/**获取积分对应的官职*/
	public synchronized int getJobIndex(int score){
		for(int i=0;i<OFFICERS_NEEDSCORE.length;i++){
			if(OFFICERS_NEEDSCORE[i]>=score){
				return i;
			}
		}
		return -1;
	}
	
	public static boolean canUse(int itemId){
		for(int id : canNotUseItem){
			if(itemId==id)
				return false;
		}
		return true;
	}
	
	/**根据官职索引获取属性值*/
//	public synchronized int[] getEnhanceProperties(int index){
//		if(index<0 || index>OFFICER_ENHANCE_PROPERTIES.length-1){
//			return new int[]{0,0};
//		}
//		int[] props=new int[2];
//		props[0]=OFFICER_ENHANCE_PROPERTIES[index*2];
//		props[1]=OFFICER_ENHANCE_PROPERTIES[index*2+1];
//		return props;
//	}
}
