package com.pip.itimes.server.stage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.primitives.ArrayShortList;
import org.apache.commons.collections.primitives.ShortList;
import org.apache.log4j.Logger;

import com.pip.gtl.etf.ETFFile;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.server.bean.Master;
import com.pip.itimes.server.bean.Mercenary;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.bean.TaskData;
import com.pip.itimes.server.suit.SuitEffect;
import com.pip.itimes.server.suit.Suits;
import com.pip.itimes.server.util.PropertyPool;
import com.pip.itimes.server.util.Utils;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class
        PlayerData {

    private static final Logger log = Logger.getLogger(PlayerData.class);

    private Player player;
	
    protected List abilities = new ArrayList();
    protected List equipments = new ArrayList();
    protected List basicItems = new ArrayList();
    protected List extendedItems = new ArrayList();
    protected List taskItems = new ArrayList();
    protected Grid[] usedEquipments = new Grid[9];
    protected List pets = new ArrayList();
    protected List image = new ArrayList<RoleFaceData>();				//人物的形象
    protected List roleTitle = new ArrayList();				//人物的称号
    
    protected short options[] = new short[20];
    
    protected int key9_options[] = new int[12];
    
    protected short skillPoint[] = new short[8];
    protected List recipes = new ArrayList();
    protected List playerPrescriptions = new ArrayList();				//新的打造配方
    protected int skillPoint2;								//新的生活熟练度点数

	//mengjie modify
//    protected ShortList currentTasks = new ArrayShortList();
//    protected ShortList completedTasks = new ArrayShortList();
    protected Map<Short,LoopTasks> currentTasks = new HashMap<Short,LoopTasks>();
    protected Map<Short,LoopTasks> completedTasks = new HashMap<Short,LoopTasks>();
    
    protected int favoriteId;
    protected ChatOption[] chatOptions = new ChatOption[8];
    protected Map friends = MapUtils.orderedMap(new HashMap(25));
    protected List blackList = new ArrayList();
    protected List channels = new ArrayList();
    protected List bufs = new ArrayList(3);
    protected List extendedBufs = new ArrayList(3);
    protected List campBattleBuffs = new ArrayList(3);
    protected List diamondShineBufs = new ArrayList();		//宝辉效果产生Buf
    protected List suitEffect = new ArrayList();
    
    protected PropertyPool playerPool = new PropertyPool();
    
    protected int maxHp;
    protected int maxMp;
    private int siderealTime = 0;

    protected Pet pet;

    protected int maxLevel = 100;

    protected int creditIndex = 0;
    //mengjie add 刷怪
    private long lastkillmg = 0;
    private long killmgtime = 0;
    private int killmgcount = 0;
    private int killmgpasscount = 0;
    private long killmgpasstime = 0;

    private int killmgtotalcount = 0;
    private int killmgerrorpro = 0;
    
    private int lastmgid = 0;
    private int positiontimes = 0;
    private int killmgidcount = 0;
    public static final String[] CREDIT_NAME = {"新兵","列兵", "下士", "中士", "军士长", "士官长",
                                               "骑士", "骑士中尉", "骑士队长", "少校", "司令",
                                               "统帅", "元帅", "大元帅"};
    //mengjie add 上限增加到20亿
    public static final int[] CREDIT = {0,100, 300, 500, 1000, 2000, 4000, 8000,
                                       20000, 50000, 100000, 200000, 500000,
                                       900000};
    
    private static final String VIP_LEVEL = "vipLevel";
    
    private static final String VIP_NEW_LEVEL = "vipNewLevel";
    
    private static final String LAST_GET_VIP_GIFT_TIME = "lastGetVipGiftTime";
    
    private static final String VIP_VALID_TIME = "vipValidTime";
    
    private static final String CHR_ITEM_COUNT = "chrItemCount";
    
    private static final String LAST_WORLD_COMPLETE_TIME = "lastWorldCompleteTime";
    
    private static final String LAST_CAMP_COMPLETE_TIME = "lastCampCompleteTime";
    
    private static final String LAST_ROLLCALL_TIME = "lastRollcallTime";
    
    private static final String ROLLCALL_DAYS = "rollcallDays";
    
    private static final String CHECK_EXPBAG = "checkExpBag";		//玩家获取经验
    
    
    /**
     * ohterPool相关
     */
    //离线经验
    public static final String OTHER_POOL_LIFEVALUE = "lifeValue";		//当前记录的活力值
    public static final String OTHER_POOL_UNLINEEXP = "unlineExp";		//离线经验值
    public static final String OTHER_POOL_UNLINEDATE = "unlineDate";	//离线日期
    private static final String OTHER_POOL_UNLINEONLINELIFE = "unlineOnlineLife";	//在线活力积累 每天积累
    private static final String TWELFTH_LUNAR_CONSUMER = "twelfthLunarConsumer";	// 腊八消费金额（I币）
    private static final String TWELFTH_LUNAR_LAST_RECEIVE_TIME = "twelfthLunarLastReceiveTime";	// 最后一次领取腊八粥的时间
    private static final String TWELFTH_LUNAR_BEANS_COUNT = "twelfthLunarBeansCount";	// 捐助腊八豆的总个数
    private static final String ACTIVITY_CONSUMER = "activityConsumer";				// 消费额度
    private static final String NEW_ACTIVITY_CONSUMER = "NewActivityConsumer";		// 新消费额度
    private static final String CAMP_BATTLEFIELD_KILLING_POINTS = "killPoints";		// 阵营战场杀戮点数
    private static final String LAST_RESET_KILLING_POINTS_TIME = "lastResetTime";	// 重置杀戮点数时间
    private static final String KILL_POINT_CONSUME = "killPointConsume";			// 杀戮点数消费
    private static final String KILL_POINT_CONSUME_TIME = "killPointConsumeTime";	// 杀戮点数消费时间
    private static final String VIANYTYPE = "viany";		//属性攻属性
    private static final String PHIZTITLE_INDEX = "phizTitleIndex"; //表情称号
    private static final String ONLINE_TIMER = "onlineTimer";	//在线时间
    private static final String MERCENTARY = "mercentary";	//佣兵数据
    private static final String LEADERSHIP = "leadership";	//统御值
    private static final String GETLEADERSHIPTIME = "getleadershiptime";	//获取统御值的时间
    private static final String SELLNEXTTIME = "sellnexttime";	//下次可以出售佣兵的时间    
    private static final String LANDLASTTIME = "landlasttime";  //上次登陆的时间
    private static final String LANDTIMES = "ltimes"; //登陆的次数
    private static final String MAGICMONEY = "magicimoney"; //魔法i币
    private static final String CLOCK = "clock";           //记录时间
    private static final String CLOCKUSE = "clockTime";    //
    private static final String CLOCK_TONGSHOP="tongShopClock"; //公会商店记录时间
    private static final String CLOCKUSE_TONGSHOP="tongShopClockTime";
    private static final String LUCK_TIME = "lucktime";	//幸运沙漏
    private static final String FARM_MONEY = "farmmoney";	//家园金币
    private static final String BOSSRUSH_STAGE = "bossRushStage";//多层BOSS挑战的关卡记录
    private static final String BOSSRUSH_STAGE_BEST = "bossRushStage_best";//多层BOSS挑战记录最高的关卡
    private static final String BOSSRUSH_BOUTCOUNT = "bossRush_BoutCount"; //多层BOSS挑战每层用的回合数
    private static final String BOSSRUSH_TIME = "bossRushTime"; //多层BOSS记录每天挑战的次数
    private static final String BOSSRUSH_LASTTIME = "bossRushLastTime";//多层BOSS挑战上一次进行的时间
    private static final String ADDATTRIBUTE_VALUE = "addAttribute"; //保存永久增加的属性 
    private static final String AWARD_BOX_ITEMID_ARRAY = "awardBoxItemIdArray"; //保存奥德赛之旅产生的十个itemId
    private static final String CHRISTMAS_WISHING_NORMAL_COUNT = "christmasWishing_normal_count";//圣诞许愿：每天普通许愿的次数
    private static final String DRAGON_BOAT_FESTIVAL_REPLACE_COUNT = "dragonBoatFestivalReplaceCount";//圣诞许愿：每天普通许愿的次数
    private static final String CHRISTMAS_WISHING_NORMAL_LASTTIME = "christmasWishing_normal_lastTime";//圣诞许愿：最后一次普通许愿的时间
    private static final String DRAGON_BOAT_FESTIVAL_REPLACE_LASTTIME = "dragonBoatFestivalReplaceLastTime";//圣诞许愿：最后一次普通许愿的时间
	private static final String REPLACE_DIAMOND_TIME = "replace_diamond_time";
	private static final String DOWNLOAD_POINTS = "download_points";//保存安卓客户端下载积分
    private static final String DOWNLOADPOINT_USEVALUE = "downloadPoint_usevalue";//每天使用积分的数额
    private static final String DOWNLOADPOINT_LASTTIME = "downloadPoint_lasttime";//最后一次使用积分的时间
    private static final String TRAIN_POINTS = "training";//修心点数
    private static final String ATTACK_LEVEL = "attcklevel";//攻击等级
    private static final String PDEFEN_LEVEL = "pdeflevel";//防御等级
    private static final String MATTACK_LEVEL = "mattcklevel";//魔攻等级
    private static final String MDEFEN_LEVEL = "mdeflevel";//魔防等级
    private static final String HIT_LEVEL = "hitlevel";//命中等级
    private static final String NOCRI_LEVEL = "nocrilevel";//免爆等级
    private static final String CURRENT_ATTACK_POINT = "currentattckpoint";//当前灵力值
    private static final String CURRENT_PDEF_POINT = "currentpdefpoint";
    private static final String CURRENT_MATTACK_POINT = "currentmattckpoint";
    private static final String CURRENT_MDEF_POINT = "currentmdefpoint";
    private static final String CURRENT_HIT_POINT = "currenthitpoint";
    private static final String CURRENT_NOCRI_POINT = "currentnocripoint";
    private static final String REPLACE_TIMES = "replacetimes";//当前兑换次数
    private static final String REPLACE_CLOCK = "replaceclock";//兑换时间
    private static final String GET_DIAMONDGIFTBAG_TIMES = "GetDiamondGiftBagTimes";//获得超级宝石大礼包次数
    private static final String	GET_SEED_TIME = "getseedtime";//得到劳动种子时间 
    private static final String USE_TRAINPOINT = "usetrainpoint";//玩家使用的聚灵点
    private static final String GET_SUPER_IDENTIFY_TIMES = "get_super_identify_times";//获得超级鉴定符道具次数
    private static final String GET_SEVEN_LEVEL_TIMES = "get_seven_level_times";//获得7级定向道具次数
    private static final String GET_FORCE_PK_TIMES = "get_force_pk_times";//获得强制pk道具次数
    private static final String WEEK_USE_TRAINPOINT_CLOCK = "week_use_trainpoint_clock";//每周使用聚灵点时间
    private static final String BOSSRUSH_AUTO_TIMES = "bossrush_auto_times";//自动爬塔次数
    private static final String BOSSRUSH_AUTO_TIMES_VIP4 = "bossrush_auto_times_vip4";//vip4自动爬塔次数
    private static final String BOSSRUSH_AUTO_TIMES_VIP5 = "bossrush_auto_times_vip5";//vip5自动爬塔次数
    private static final String GET_VIP3_FACE = "get_vip3_face";//获得vip3级形象次数
    private static final String GET_VIP4_FACE = "get_vip4_face";//获得vip4级形象次数
    private static final String GET_VIP5_FACE = "get_vip5_face";//获得vip5级形象次数
    private static final String AUTO_PASS_STAGE_CLOCK = "auto_pass_stage_clock";// 自动爬塔时间
    private static final String FORCE_PK_CLOCK = "force_pk_clock";//强制pk时间
    private static final String VIP5_MESSAGE_UP = "vip5_message_up";//vip5上线公告时间
    private static final String VIP5_MESSAGE_DOWN = "vip5_message_down";//vip5下线公告时间
    private static final String PetDevelopTime = "petDevelopTime";//宠物培养的时间
    private static final String PetDevelopAddValue = "petDevelopValue";	//宠物培养增加的数值和次数
    private static final String PetEvolutionTime = "petEvolutionTime";	//宠物进化时间
    private static final String PetEvolutionCount = "petEvolutionCount";	//宠物进化次数
    
    //(每个阵眼当前经验)
    private static final String WATER_MAGIC_POSITION_EXP = "water_exp";
    private static final String SOIL_MAGIC_POSITION_EXP = "soil_exp";
    private static final String FIRE_MAGIC_POSITION_EXP = "fire_exp";
	private static final String WIND_MAGIC_POSITION_EXP = "wind_exp";
	private static final String MIND_MAGIC_POSITION_EXP = "mind_exp";
	//(每个阵眼当前级别)
	private static final String WATER_MAGIC_POSITION_LEVEL = "water_level";
    private static final String SOIL_MAGIC_POSITION_LEVEL = "soil_level";
    private static final String FIRE_MAGIC_POSITION_LEVEL = "fire_level";
	private static final String WIND_MAGIC_POSITION_LEVEL = "wind_level";
	private static final String MIND_MAGIC_POSITION_LEVEL = "mind_level";
	//(每个级别当前阶层)
	private static final String WATER_CURRENT_FLOOR = "water_current_floor";
	private static final String SOIL_CURRENT_FLOOR = "soil_current_floor";
	private static final String FIRE_CURRENT_FLOOR = "fire_current_floor";
	private static final String WIND_CURRENT_FLOOR = "wind_current_floor";
	private static final String MIND_CURRENT_FLOOR = "mind_current_floor";
//	//兔子赛跑下注的类型
//	private static final String RABBIT_RACE_JETTON_FIR = "jettonNumFir";
//	private static final String RABBIT_RACE_JETTON_SEC = "jettonNumSec";
//	private static final String RABBIT_RACE_JETTON_THI = "jettonNumThi";
//	private static final String RABBIT_RACE_JETTON_FOU = "jettonNumFou";
//	private static final String RABBIT_RACE_JETTON_FIF = "jettonNumFif";
	
	//花钱开宝箱宝箱序号
	private static final String AWARD_BOX_ITEM_ID = "awardBoxItemId";
    
    protected PropertyPool otherPool = new PropertyPool();
    public int tmpLifeValue = 0;			//临时记录活力值
    public int tmpLifeValueGet = 0;			//临时使用的活力值
    private int lifeValue = 0;
    private int unlineExp = 0;
    private Date unlineDateTime = null;		//离线时间
    private int unlineOnlineLife = 0;		//在线活力值
    //师徒系统
    private static final String OTHER_POOL_FAME = "fame";				//声望
    private static final String OTHER_POOL_CALLCOUNT = "callCount";		//被呼叫次数
    private int fame = 0;					//当前声望值
    private int callCount = 0;				//被呼叫的次数
    private Master[] apprentices = null;			//爱徒列表
    private long apprenticeFindMasterTime;					//拜师时发送请求的时间 一分钟只能请求一次
    //获取经验包
    private int checkExpBag = 0;
    private int farmMoney = 0;		//家园金币
    private int trainpoint = 0;		//修心灵力点
    private int trainattacklevel = 0;//修心攻击等级
    private int trainpdeflevel = 0;	//修心防御等级
    private int trainmattacklevel = 0;//修心魔攻等级
    private int trainmdeflevel = 0;//修心魔防等级
    private int trainhitlevel = 0;//修心命中等级
    private int trainnocrilevel = 0;//修心免爆等级
    //当前属性聚灵灵力值
    private int currentattpoint = 0;
    private int currentpdefpoint = 0;
    private int currentmattpoint = 0;
    private int currentmdefpoint = 0;
    private int currenthitpoint = 0;
    private int currentnocripoint = 0;
    //当前兑换经验次数
    private int replacetimes = 0;
    private Date Replaceclock;
    private int diamondGiftTimes = 0;//领取礼包次数
    private Date getSeedDate;	
    private int playerUseTrainpoint = 0;//玩家使用的聚灵点
    private int getIdentifyTimes = 0;//领取超级鉴定符次数
    private int getSevenLevelFixTimes = 0;//领取7级定向包次数
    private int getforcepkTimes = 0;//领取强制pk药水次数
    private int bossRushAutoTimes = 0;//爬塔次数
    private int bossRushAutoTimesvip4 = 0;//vip4自动爬塔次数
    private int bossRushAutoTimesvip5 = 0;//vip5自动爬塔次数
    private Date UseTrainpointDate;
    private int getvip3facetimes = 0;//vip各等级形象领取次数
    private int getvip4facetimes = 0;
    private int getvip5facetimes = 0;
    private Date AutoStageDate;
    private Date forcePkDate;
    private Date vip5messageUpDate;
    private Date vip5messageDownDate;
    //阵眼经验
    private int waterExp;
    private int soilExp;
    private int fireExp;
	private int windExp;
	private int mindExp;
	//阵眼级别
	private int waterlevel;
    private int soillevel;
    private int firelevel;
	private int windlevel;
	private int mindlevel;
	//阵眼阶层
	private int waterfloor;
	private int soilfloor;
	private int firefloor;
	private int windfloor;
	private int mindfloor;
	
	//花钱开宝箱开出的宝箱类别
	private int awardBoxItemId;
	private int[] awardBoxItemIdArray;
	
//	//兔子赛跑下注注数
//	private int jettonNumFir;
//	private int jettonNumSec;
//	private int jettonNumThi;
//	private int jettonNumFou;
//	private int jettonNumFif;
    
	//玩家在兔子赛跑中的下注注数
//	private int[] jettonNum = {0, 0, 0, 0, 0};
	
	/**
	 * 宠物培养相关
	 */
	private Date petDevelopTime;	//宠物培养时间记录
	private int petDevelopValue[] = new int[8];	//宠物的培养记录数值和当天次数:力敏体智,普通,高级,皇家,宠物ID

	private Date petEvolutionTime;	//宠物进化时间记录
	private int petEvolutionCount;	//宠物进化次数
	
	/**
	 * 格式化时间
	 */
    private static SimpleDateFormat format = new SimpleDateFormat ("yyyy-MM-dd HH:mm");
    
    protected List enemys = new ArrayList(20);
    
    public int clientDataVersion = 0;
           
    // 聊天频道     同 ISendMessage
    public static final int WORLD = -1;
    public static final int MAP = -2;
    public static final int GUILD = -3;
    public static final int GROUP = -4;
    public static final int TEAM = -5;
    public static final int FAVORITE = -6;
    public static final int SYSTEM = -7;
    public static final int GM = -8;
    public static final int	CAMP = -9;		 //阵营
    public static final int NEW = -10;
    public static final int ROAR = -12;	 // 狮子吼
    
    // 性别
    public static final int MALE = 0;
    public static final int FEMALE = 1;
    
    // 背包中的装备
    public static final byte BAG_EQUIPMENT = 0;
    // 玩家身上的装备
    public static final byte PLAYER_USE_EQUIPMENT = 1;
    // 宠物身上的装备
    public static final byte PET_USE_EQUIPMENT = 2;
    
    // vip等级
    public int vipLevel;
    
    // new vip等级
    public int vipNewLevel;
    
    // 最后领取奖励的时间
    public Date lastGetVipGiftTime;
    // vip有效期时间
    public Date validTime;
    public int chrItemCount;
    // 最后一次完成世界喊话活动的时间
    public Date lastWorldCompleteTime;
    // 最后一次完成阵营喊话活动的时间
    public Date lastCampCompleteTime;
    // 最后一次完成点名活动的时间
    public Date lastRollcallCompleteTime;
    // 连续完成点名活动的天数
    public int rollcallDays;
    // 腊八活动消费的金额（RMB:分）
    public int twelfthLunarConsumer;
    // 最后一次领取腊八粥的时间
    public Date twelfthLunarLastReceiveTime;
    // 捐献腊八豆总的个数
    public int twelfthLunarBeansCount;
    // 消费额度
    public int activityConsumer;
    // 新消费额度
    public int NewActivityConsumer;
    // 阵营战场杀戮点数
    public int campBattlefieldKillPoints;
    // 最后重置杀戮点数时间
    public Date lastResetKillPointsTime;
    // 杀戮点数消费值
    private int killPointConsume;
    // 杀戮点数
    private Date killPointConsumeTime;
    // 属性攻属性
    private int vianyType;
//    登陆的次数
    private int iTimes;
    //表情称号
    protected List<PhizTitleData> phizTitleList = new ArrayList<PhizTitleData>();
    //当前选择的表情称号
    private short phizTitleIndex;
    // 在线时间
    private long onlineTimer;
    //统御力
    private int leadership;
    //魔法i币
    private int magicimoney;
    
    private Date getLeadershipDate;
    //统御力最大值
    public final static int LEADERSHIP_MAX = 1200;
    //最大雇佣兵个数
    public final static int MERCENARY_COUNT_MAX = 2;
    private Date sellNextDate;	//下次可以出售佣兵的时间
    private Date landLastDate;	//上次上线的时间
    private Date clock;          //记录时间
    private int clockUse;        //设置能使用的点数
    private Date clock_tongShop; //公会商店记录时间
    private int clockUse_tongShop;
    private long lucktime;		//幸运沙漏
    //多层BOSS挑战
    private int bossRushStage; // 多层BOSS挑战记录关卡
    private int bossRushStage_best; //多层BOSS挑战记录最高的关卡
    private List<Integer> bossRushBoutList = new ArrayList<Integer>(); // 多层BOSS挑战回合数(记录每层最小值)
    private Date bossRushLastTime;
    private int bossRushTime; //多层boss挑战记录每日的次数
    
    private int[] addAttributes;//保存永久增加的属性 [0]:力,[1]:敏,[2]:体,[3]:智
    public final byte ADDATTR_STRENGTH = 0;
    public final byte ADDATTR_AGILITY = 1;
    public final byte ADDATTR_VITALITY = 2;
    public final byte ADDATTR_INTELLIGENCE = 3;
    //佣兵
    private HashMap<Integer, Integer> mercenaryid = new HashMap<Integer, Integer>();
    
    public final static long LeaveLineSecond = 24 * 60 * 60 * 1000L * 90;  //90天
    
    public int[][] autoMixData; //临时保存宝石升级需要的宝石和合成符id、个数、已经买了的个数的数组 ，[0][]:3级宝石[1][]：初级合成符，[2][]：高级合成符 ， 
    public int autoMixDiamondID;//临时保存要升级的宝石ID
    public int autoMixMoney_J;//临时保存宝石升级所需的J币
    
    private byte gemLightLevel_Holy; //临时保存神圣宝辉等级
    private byte gemLightLevel_Fantasy; //临时保存梦幻宝辉等级
    
    private final static int CHRISTMAS_WISHING_NORMAL_MAX_COUNT=3;//每天最多进行3次普通圣诞许愿
    private int christmasWishing_normal_count;//每天进行的普通圣诞许愿次数
    private Date christmasWishing_normal_lastTime; //最后一次普通圣诞许愿的时间
    
    private final static int DRAGON_BOAT_FESTIVAL_REPLACE_MAX_COUNT=5;//每天最多进行5次免费兑换
    private int dragonBoatFestivalReplaceCount;//每天进行的免费兑换次数
	private Date dragonBoatFestivalReplaceLastTime; //最后一次免费兑换的时间
    
    private Date replace_diamond_time;//宝石碎块兑换时间
    private int downloadPoint;//安卓版下载积分
    private Date downloadPoint_lasttime; //
    private int downloadPoint_usevalue;
    //jwp add start
    /*private  Vector<Mail> attchmentList;//用于邮件附件里白装和绿色装备信件
    public Vector<Mail> getAttchmentList() {
    	if(attchmentList == null){
    		attchmentList = new Vector<Mail>();
    	}
		return attchmentList;
	}
	public void addAttchmentListMail(Mail mail){
    	attchmentList.add(mail);
    }
	/*public Mail getAttchmentListMail(int id){
		Mail mail = null;
		Mail mail2 = null;
		for(int i = 0; i < attchmentList.size(); i++){
			mail = attchmentList.get(i);
			if(mail.getId() == id){
				mail2 = mail;
				break;
			}
		}
		return mail2;
	}
    public void deleteAttchmentListMail(Mail mail) throws MailException{
    	if(attchmentList.contains(mail)){
    		attchmentList.remove(mail);
    	}else{
    		throw new MailException("delete " + player.getId() + mail.getId() + "fail");
    	}

    }*/
    //jwp add end
    public PlayerData(Player player) throws Exception {
        this.player = player;
        initProperties();
//        maxHp = calculateMaxHp();
//        maxMp = calculateMaxMp();
        siderealTime = Utils.getSiderealTime(getCreateTime());
        creditIndex = getCreditIndex();
    }

    private void initProperties() throws Exception {
        byte[] bytes = player.getAbilities();
        if (bytes != null && bytes.length > 2) {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            short size = dis.readShort();
            for (int i = 0; i < size; i++) {
                short abilityId = dis.readShort();
                Ability ability = Ability.getAbility(abilityId);
                if (ability == null)
                    throw new PlayerDataException("ID["+player.getId()+"]Abilities数据错误abilityId["+abilityId+"]");
                abilities.add(ability);
            }
        }
        bytes = player.getBasicItems();
        if (bytes != null && bytes.length > 2) {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            short size = dis.readShort();
            for (int i = 0; i < size; i++) {
                int id = dis.readInt();
                byte count = dis.readByte();
                Grid grid = new Grid();
                IItemTemplate template = Items.getTemplate(id);
                if (template == null)
                    throw new PlayerDataException("ID["+player.getId()+"]BasicItems数据错误item["+id+"]");
                grid.item = template.newInstance();
                if(grid.item.getBindType()==IItem.BIND_GET)
                    grid.item.setBinded(true);
                grid.count = count;
                basicItems.add(grid);
            }
        }
        bytes = player.getMetaItems();
        if (bytes != null && bytes.length > 2) {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            short size = dis.readShort();
            for (int i = 0; i < size; i++) {
                int id = dis.readInt();
                byte count = dis.readByte();
                Grid grid = new Grid();
                // 2011年2月9日 修复情人抽奖券可以喂养宠物的BUG。
                if (id == 580011) {
                	id = 201139;
                }
                if (id == 201139) {
                	boolean mark = true;
                	for (int j = 0; j < extendedItems.size(); j ++) {
                		Grid tmpGrid = (Grid) extendedItems.get(j);
                		if (tmpGrid.item.getItemId() == id) {
                			mark = false;
                			if (tmpGrid.count >= 99) {
                				break;
                			} else {
                				int total = tmpGrid.count + count;
                				if (total > 99) {
                					tmpGrid.count = 99;
                				} else {
                					tmpGrid.count = (short) total;
                				}
                				break;
                			}
                		}
                	}
                	if (mark) {
                		IItemTemplate template = Items.getTemplate(id);
                    	if (template == null)
                    		throw new PlayerDataException("ID["+player.getId()+"]MetaItems数据错误item["+id+"]");
                    	grid.item = template.newInstance();
                    	if(grid.item.getBindType()==IItem.BIND_GET)
                    		grid.item.setBinded(true);
                    	grid.count = count;
                    	extendedItems.add(grid);
                	}
                } else {
                	IItemTemplate template = Items.getTemplate(id);
                	if (template == null)
                		throw new PlayerDataException("ID["+player.getId()+"]MetaItems数据错误item["+id+"]");
                	grid.item = template.newInstance();
                	if(grid.item.getBindType()==IItem.BIND_GET)
                		grid.item.setBinded(true);
                	if (id == 201112) {
                		if (count < 1) {
                			continue;
                		}
                	}
                	grid.count = count;
                	extendedItems.add(grid);
                }
            }
        }
        bytes = player.getTaskItems();
        if (bytes != null && bytes.length > 2) {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            short size = dis.readShort();
            for (int i = 0; i < size; i++) {
                int id = dis.readInt();
                byte count = dis.readByte();
                //mengjie add 2010年3月29日11:24:49 对100218特殊处理，预计2周后去除
                if(id == 100218){
                	continue;
                }
                Grid grid = new Grid();
                IItemTemplate template = Items.getTemplate(id);
                if (template == null)
                    throw new PlayerDataException("ID["+player.getId()+"]TaskItems数据错误item["+id+"]");
                grid.item = template.newInstance();
                grid.count = count;
                taskItems.add(grid);
            }
        }
        bytes = player.getEquipments();
        if (bytes != null && bytes.length > 2) {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            byte version = dis.readByte();
            short size = dis.readShort();
            for (int i = 0; i < size; i++) {
                IEquipment equ = EquipmentHelper.createFromDbBytes(version,dis);
                if (equ == null)
                    throw new PlayerDataException("ID["+player.getId()+"]Equipments数据错误");
                Grid grid = new Grid();
                grid.item = equ;
                grid.count = 1;
                equipments.add(grid);
            }
        }
        bytes = player.getUsedEquipments();
        if (bytes != null && bytes.length > 2) {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            byte version = dis.readByte();
            short size = dis.readShort();
            for (int i = 0; i < size; i++) {
                IEquipment equ = EquipmentHelper.createFromDbBytes(version,dis);
                if (equ == null)
                    throw new PlayerDataException("ID["+player.getId()+"]UsedEquipments数据错误");
                Grid grid = new Grid();
                grid.item = equ;
                grid.count = 1;
                usedEquipments[equ.getPart()] = grid;
            }
        }
        bytes = player.getOptions();
        if (bytes != null && bytes.length != 0) {
            for (int i = 0; i < options.length && i * 2 + 1 < bytes.length; i++) {
            	int b1 = (bytes[i * 2] & 0xFF) << 8;
            	int b2 = bytes[i * 2 + 1] & 0xFF;
                options[i] = (short)(b1 | b2);
            }
        }
        //mengjie add key9_options
        bytes = player.getKey9_options();
        if (bytes != null && bytes.length != 0) {
        	ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            for (int i = 0; i < 12 ; i++) {
            	try{
            		key9_options[i] = dis.readInt();
            	}catch (Exception ex) {
                    log.info(ex, ex);
                }
            }
        }
        
        bytes = player.getChatOptions();
        if (bytes != null && bytes.length > 0) {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            for (int i = 0; i < 8; i++) {
                ChatOption option = new ChatOption();
                option.pri = dis.readByte(); ;
                option.color = dis.readByte();
                chatOptions[i] = option;
            }
            favoriteId = dis.readInt();
        } else {
            chatOptions = ChatOption.getDefaltChatOptions();
            favoriteId = -1;
        }
        bytes = player.getTechSkills();
        if (bytes != null && bytes.length > 2) {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            for (int i = 0; i < 8; i++) {
                skillPoint[i] = dis.readShort();
            }
        } else {
            for (int i = 0; i < 8; i++) {
                skillPoint[i] = -1;
            }
        }
        bytes = player.getRecipes();
        if (bytes != null && bytes.length > 0) {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            short count = dis.readShort();
            for (int i = 0; i < count; i++) {
                Recipe recipe = Recipes.getRecipe(dis.readInt());
                if (recipe == null)
                    throw new PlayerDataException("数据错误");
                recipes.add(recipe);
            }
        }
        
        
        bytes = player.getFriends();
        if (bytes != null && bytes.length > 0) {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            byte count = dis.readByte();
            for (int i = 0; i < count; i++) {
                int id = dis.readInt();
                String name = dis.readUTF();
//                int favorite = 1;
                int favorite = dis.readInt();
                if(id == getId()){
                	continue;
                }
                Friend friend = new Friend(id, name,favorite, 0);
                friends.put(new Integer(id),friend);
            }
        }
        
        bytes = player.getBlackList();
        if(bytes!=null&&bytes.length>0){
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            byte count = dis.readByte();
            for(int i=0;i<count;i++){
                int id = dis.readInt();
                String name = dis.readUTF();
                if(name.length()==0){ 
                	log.info("ID[" + player.getId() +"] blackList error：name.length == 0 playerLinkID[" + id +"] ");
                	continue;
                }
                PlayerLink p = new PlayerLink(id,name);
                blackList.add(p);
            }
        }
        if(player.getTaskData() == null){
        	//2011年12月27日10:45:05 服务器发现有些玩家没有任务数据
        	TaskData taskData = new TaskData();
            taskData.setCurrent(new byte[0]);
            taskData.setFinished(new byte[0]);
            taskData.setSaveData(new byte[0]);
            taskData.setPlayer(player);
            player.setTaskData(taskData);
            log.info("PlayerID[" + getId() + "] no TaskData");
        }
        if (player.getTaskData() != null) {
	        bytes = player.getTaskData().getCurrent();
	        if (bytes != null && bytes.length > 0) {
	            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
	            DataInputStream dis = new DataInputStream(bis);
	            //mengjie modify 解析任务部分，增加最后完成任务时间，一个周期内完成数量。并保留原来解析方式 20091013
	            short id = dis.readShort();
	            if (id == 0){//new
	            	byte version = dis.readByte();
	            	if (version == 1){//版本1
	            		int taskcount = dis.readInt();
	            		for (int i = 0; i < taskcount; i++) {
	            			short taskid = dis.readShort();
	            			Long lastfinishdatelong = dis.readLong();
	            			Date lastfinishdate = null;
	            			if (lastfinishdatelong > 0){
	            				lastfinishdate = new Date(lastfinishdatelong);
	            			}
	            			byte finishcount = dis.readByte();
	            			LoopTasks loopTasks = null;
	            			if (LoopTasks.LoopTaskbyTaskid.containsKey(taskid)){
	            				LoopTasks loopTaskstmp = LoopTasks.LoopTaskbyTaskid.get(taskid);
	            				
	            				loopTasks = new LoopTasks(loopTaskstmp.getTaskid(),
	            						loopTaskstmp.getLoops(),
	            						loopTaskstmp.getTime(),
	            						loopTaskstmp.getGroup(), loopTaskstmp.getCampId());
	            				loopTasks.setFinishcount(finishcount);
	            				loopTasks.setLastfinishtime(lastfinishdate);
	            			}else{
	            				loopTasks = new LoopTasks(taskid,
	            						-1,
	            						-1,
	            						-1, -1);
	            				loopTasks.setFinishcount(finishcount);
	            				loopTasks.setLastfinishtime(lastfinishdate);
	            			}
	            			if (!currentTasks.containsKey(taskid)) {
	                            currentTasks.put(taskid, loopTasks);
	                        }else{
	                        	currentTasks.remove(taskid);
	                        	currentTasks.put(taskid, loopTasks);
	                        }
	            		}
	            	}
	            }else{//old
	            	for (int i = 0; i < bytes.length / 2; i++) {
	                    if (i > 0){
	                    	id = dis.readShort();
	                    }
	                    LoopTasks loopTasks = new LoopTasks(id,
	    						-1,
	    						-1,
	    						-1, -1);
	    				loopTasks.setFinishcount(-1);
	    				loopTasks.setLastfinishtime(null);
	    				if (!currentTasks.containsKey(id)) {
	                        currentTasks.put(id, loopTasks);
	                    }else{
	                    	currentTasks.remove(id);
	                    	currentTasks.put(id, loopTasks);
	                    }
	                }
	            }
	        }
	        
	        bytes = player.getTaskData().getFinished();
	        if (bytes != null && bytes.length > 0) {
	            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
	            DataInputStream dis = new DataInputStream(bis);
	          //mengjie modify 解析任务部分，增加最后完成任务时间，一个周期内完成数量。并保留原来解析方式 20091013
	            short id = dis.readShort();
	            if (id == 0){//new
	            	byte version = dis.readByte();
	            	if (version == 1){//版本1
	            		int taskcount = dis.readInt();
	            		for (int i = 0; i < taskcount; i++) {
	            			short taskid = dis.readShort();
	            			Long lastfinishdatelong = dis.readLong();
	            			Date lastfinishdate = null;
	            			if (lastfinishdatelong > 0){
	            				lastfinishdate = new Date(lastfinishdatelong);
	            			}
	            			byte finishcount = dis.readByte();
	            			LoopTasks loopTasks = null;
	            			if (LoopTasks.LoopTaskbyTaskid.containsKey(taskid)){
	            				LoopTasks loopTaskstmp = LoopTasks.LoopTaskbyTaskid.get(taskid);
	            				
	            				loopTasks = new LoopTasks(loopTaskstmp.getTaskid(),
	            						loopTaskstmp.getLoops(),
	            						loopTaskstmp.getTime(),
	            						loopTaskstmp.getGroup(), loopTaskstmp.getCampId());
	            				loopTasks.setFinishcount(finishcount);
	            				loopTasks.setLastfinishtime(lastfinishdate);
	            			}else{
	            				loopTasks = new LoopTasks(taskid,
	            						-1,
	            						-1,
	            						-1, -1);
	            				loopTasks.setFinishcount(finishcount);
	            				loopTasks.setLastfinishtime(lastfinishdate);
	            			}
	            			//TODO delete
	            			if(taskid >= 12001 && taskid <= 12005){
	            				Calendar cal = Calendar.getInstance();
	            				cal.set(Calendar.YEAR, 2011);
	            				cal.set(Calendar.MONTH, 5);
	            				cal.set(Calendar.DAY_OF_MONTH, 16);
	            				cal.set(Calendar.HOUR, 0);
	            				cal.set(Calendar.MINUTE, 0);
	            				cal.set(Calendar.SECOND, 0);
	            				cal.set(Calendar.MILLISECOND, 0);
	            				if(lastfinishdatelong <= cal.getTimeInMillis()){
	            					log.info("playerID[" + player.getId() + "] delete completedTask id[" + taskid + "]");
	            					continue;
	            				}
	            			}
	            			//TODO delete end
	            			if (!completedTasks.containsKey(taskid)) {
	            				completedTasks.put(taskid, loopTasks);
	                        }else{
	                        	completedTasks.remove(taskid);
	                        	completedTasks.put(taskid, loopTasks);
	                        }
	            		}
	            	}
	            }else{//old
	            	for (int i = 0; i < bytes.length / 2; i++) {
	                    if (i > 0){
	                    	id = dis.readShort();
	                    }
	                    LoopTasks loopTasks = new LoopTasks(id,
	    						-1,
	    						-1,
	    						-1, -1);
	    				loopTasks.setFinishcount(-1);
	    				loopTasks.setLastfinishtime(null);
	    				
	    				if (!completedTasks.containsKey(id)) {
	        				completedTasks.put(id, loopTasks);
	                    }else{
	                    	completedTasks.remove(id);
	                    	completedTasks.put(id, loopTasks);
	                    }
	                }
	            }
	        }
        }
        bytes = player.getPets();
        if (bytes != null && bytes.length > 0) {
            Pet[] ps = Pet.getPetsFromDb(bytes);
            for (int i = 0; i < ps.length; i++) {
                //mengjie add level
            	if(ps[i].getLevel()>100){
            		Utils.log(log, player.getId(), 9999,
                            "loadPet Error[" + Utils.getHexdump(ps[i].toClientBytesWithLevel(-1)) +
                            "]");
            		int pointtmp = (ps[i].getLevel()-100)*4;
            		//登录时，把所有可兑换属性，变成可分配属性
            		if(ps[i].getPoint() > 0){
            			ps[i].setCurrentPoint(ps[i].getCurrentPoint()+ps[i].getPoint());
            			ps[i].setPoint(0);
            		}
            		int p = ps[i].getPoint();
            		int cp = ps[i].getCurrentPoint();
            		
            		if(p >= pointtmp){
            			p -= pointtmp;
            		}else{
            			p -= pointtmp;
            			if((cp + p) >= 0){
            				cp = cp + p;
            				p = 0;
            			}else{
            				cp =0;
            				p=0;
            			}
            		}
            		ps[i].setLevel(100);
            		ps[i].setPoint(p);
            		ps[i].setCurrentPoint(cp);
            		Utils.log(log, player.getId(), 9999,
                            "loadPet Error[" + Utils.getHexdump(ps[i].toClientBytesWithLevel(-1)) +
                            "] changed");
            	}
            	//毒瘤 增加对原先的2代宠物符的修正 当技能个数不对时 给宠物添加匹配的技能
            	int skillcount = 5;
            	int pl = ps[i].getPerceptionLevel();
            	if(pl >= 3 && pl < 6){
            		skillcount++;
            	}else if(pl >= 6 && pl < 8){
            		skillcount += 2;
            	}else if(pl == 8){
            		skillcount += 3;
            	}
            	if(ps[i].getEvolutionLevel() >= 4){
            		skillcount += 1;
            	}
            	if(ps[i].getAbilities().length < skillcount){
            		//宠物技能数不对 增加技能
            		int addcount = skillcount - ps[i].getAbilities().length;
            		if(ps[i].getEvolutionLevel() >= 4){
            			addcount -= 1;
            		}
            		if(addcount > 0){
	            		Ability[] ability = Utils.getAddPetAbilities(ps[i].getAbilityId(), addcount);
	            		for (int j = 0; j < ability.length; j++) {
	            			ps[i].addAbility(ability[j]);
	            		}
            		}
            		String perceptionLevelName = Utils.getPerceptionLevelName(ps[i].getPerceptionLevel());
            		if(ps[i].getEvolutionLevel() >= 4){
            			Ability[] abilityCommon  = ps[i].getAbilities();
  						int abiliyCommonId[] = new int[abilityCommon.length];
   						for(int j = 0; j < abilityCommon.length ;j++){
   							abiliyCommonId[j] = abilityCommon[j].getId();
    					}
            			ps[i].addEvoAbilities(abiliyCommonId);
            		}
            		ps[i].setEnhanceName(perceptionLevelName);
            		log.info("initProperties Pet SkillCount error PlayerID[" + this.getId() + "] PetID[" + ps[i].getId() + "] add SkillCount[" + addcount + "]");
            	}else if(ps[i].getAbilities().length > skillcount){
            		//修改技能大于技能数时 删除最后的技能 直到技能数正常
            		int removeCount = ps[i].getAbilities().length - skillcount;
            		log.info("initProperties Pet SkillCount error PlayerID[" + this.getId() + "] PetID[" + ps[i].getId() + "] remove SkillCount[" + removeCount + "] try");
            		for(int r=0; r<removeCount; r++){
            			Ability ability = ps[i].removeAbilityEnd();
            			if(ability != null){
            				log.info("initProperties Pet SkillCount error PlayerID[" + this.getId() + "] PetID[" + ps[i].getId() + "] remove SkillName[" + ability.getName() + "]");
            			}else{
            				log.info("initProperties Pet SkillCount error PlayerID[" + this.getId() + "] PetID[" + ps[i].getId() + "] remove Skill is NULL");
            			}
            		}
            	}
            	pets.add(ps[i]);
            }
        }
        int petId = player.getPetId();
        if (petId != -1) {
            for (int i = 0; i < pets.size(); i++) {
                Pet p = (Pet) pets.get(i);
                if (p.getId() == petId) {
                    pet = p;
                    break;
                }
            }
        }
        //宠物栏容量改成10
        if(player.getPetSize() < 10){
        	player.setPetSize(10);
        }
        bytes = player.getBufs();
        if(bytes!=null&&bytes.length>0){
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            short len = dis.readShort();
            long current = System.currentTimeMillis();
            for(int i=0;i<len;i++){
                int id = dis.readInt();
                byte pro = dis.readByte();
                int value = dis.readInt();
                int time = dis.readInt();
                byte unit = dis.readByte();
                long timestamp = dis.readLong();
                Buf buf = Buf.getBufCheckTime(id,pro,value,time,unit,timestamp,current);
                if(buf!=null) {
                	addBufOnce(buf,null);
                }
            }
        }
        bytes = player.getEnemys();
        if(bytes!=null&&bytes.length>0){
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            short len = dis.readShort();
            for(int i=0;i<len;i++){
                int id = dis.readInt();
                String name = dis.readUTF();
                int times = dis.readInt();
                long lastTime = dis.readLong();
                Enemy enemy = new Enemy(id,name,times,lastTime);
                enemys.add(enemy);
            }
        }
        /**
         * 橱窗里的人物形象
         */
        bytes = player.getImage();			//玩家橱窗里的形象和物品
        if(bytes != null && bytes.length > 0){
        	ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            short len = dis.readShort();
            if(len == 0){
            	int version = dis.read();			//读出version
            	if(version == 1){		// 形象永久生效
            		int type = dis.readByte();		//1: 为形象
        			int lenImage = dis.read();		//形象的个数
            		for(int i= 0; i < lenImage; i ++){
                    	int faceID = dis.readInt();
                    	RoleFaceData selfFaceTmp = RoleFaces.getRoleFace(faceID);
                    	RoleFaceData selfFace = new RoleFaceData(selfFaceTmp.getFace(), selfFaceTmp.getName(), selfFaceTmp.getPrice());
                        long expiration = dis.readLong();		// 由于之前服务器储存的是购买时间，所以在强制写成-1
                        expiration = -1;
                        selfFace.setExpiration(expiration);
                        image.add(selfFace);				//保存形象物品
                    }
            		type = dis.readByte();//称号
        			int lenTitle = dis.read();
            		for(int i= 0; i < lenTitle; i ++){
                    	String curRoleTitle = dis.readUTF();
                        roleTitle.add(curRoleTitle);				//保存称号
                    }
            	} else if (version == 2) {		// 增加形象过期
            		int type = dis.readByte();		//1: 为形象
        			int lenImage = dis.read();		//形象的个数
            		for(int i= 0; i < lenImage; i ++){
                    	int faceID = dis.readInt();
                    	RoleFaceData selfFaceTmp = RoleFaces.getRoleFace(faceID);
                    	RoleFaceData selfFace = new RoleFaceData(selfFaceTmp.getFace(), selfFaceTmp.getName(), selfFaceTmp.getPrice());
                        long expiration = dis.readLong();		// 过期时间
                        selfFace.setExpiration(expiration);
                        image.add(selfFace);				//保存形象物品
                    }
            		type = dis.readByte();//称号
        			int lenTitle = dis.read();
            		for(int i= 0; i < lenTitle; i ++){
                    	String curRoleTitle = dis.readUTF();
                        roleTitle.add(curRoleTitle);				//保存称号
                    }
            	} else if(version == 3){ //增加表情称号
            		int type = dis.readByte();		//1: 为形象
        			int lenImage = dis.read();		//形象的个数
            		for(int i= 0; i < lenImage; i ++){
                    	int faceID = dis.readInt();
                    	RoleFaceData selfFaceTmp = RoleFaces.getRoleFace(faceID);
                    	RoleFaceData selfFace = new RoleFaceData(selfFaceTmp.getFace(), selfFaceTmp.getName(), selfFaceTmp.getPrice());
                        long expiration = dis.readLong();		// 过期时间
                        selfFace.setExpiration(expiration);
                        image.add(selfFace);				//保存形象物品
                    }
            		type = dis.readByte();//称号
        			int lenTitle = dis.read();
            		for(int i= 0; i < lenTitle; i ++){
                    	String curRoleTitle = dis.readUTF();
                        roleTitle.add(curRoleTitle);				//保存称号
                    }
            		type = dis.readByte();//表情称号
            		int lenPhiz = dis.read();
            		for(int i=0;i<lenPhiz;i++){
            			short phizIndex = dis.readShort();
            			PhizTitleData tmpPhiz = PhizTitleData.getPhizTitle(phizIndex);
            			PhizTitleData playerPhiz = new PhizTitleData(tmpPhiz.getIndex(),tmpPhiz.getType(),tmpPhiz.getName());
            			phizTitleList.add(playerPhiz);
            		}
            	}
            }else{
            	for(int i= 0; i < len; i ++){
                	int faceID = dis.readInt();
                	RoleFaceData selfFaceTmp = RoleFaces.getRoleFace(faceID);
                	RoleFaceData selfFace = new RoleFaceData(selfFaceTmp.getFace(), selfFaceTmp.getName(), selfFaceTmp.getPrice());
                    long expiration = dis.readLong();		// 过期时间
                    selfFace.setExpiration(expiration);
                    image.add(selfFace);				//保存形象物品
                }
            }
        }
        
        /**新的打造配方*/
        bytes = player.getPrescription();							//新的打造配方
        if (bytes != null && bytes.length > 0) {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            byte version = dis.readByte();						//读出版本号
            if(version == 1){
            	skillPoint2 = dis.readInt();					//打造技能点数
            	short count = dis.readShort();
                for (int i = 0; i < count; i++) {
                    Prescription prescription = PrescriptionsAll.getPrescription(dis.readInt());
                    if (prescription == null)
                        throw new PlayerDataException("数据错误");
                    playerPrescriptions.add(prescription);
                }
            }
        }
        initPlayerPool();
        initOtherPool();
       /* adjustProperty();*/
    }
    
    private void initOtherPool() throws Exception {
    	if (player.getOtherPool() == null) {
    		//设置当前活力值
    		setLifeValue(otherPool.getInt(OTHER_POOL_LIFEVALUE));
    		//设置当前离线经验值
    		setUnlineExp(otherPool.getInt(OTHER_POOL_UNLINEEXP));
    		// 设置离线的时间
    		String strUnlineDate = otherPool.getString(OTHER_POOL_UNLINEDATE);
    		if (strUnlineDate != null && strUnlineDate.equals("") == false) {
    			setUnlineDate(format.parse(strUnlineDate));
    		}
    		//设置在线活力积累值
    		setUnlineOnlineLife(otherPool.getInt(OTHER_POOL_UNLINEONLINELIFE));
    		// 设置腊八消费金额I币
    		setTwelfthLunarConsumer(otherPool.getInt(TWELFTH_LUNAR_CONSUMER));
    		// 设置最后一次领取腊八粥的时间
    		String lastReceiveTime = otherPool.getString(TWELFTH_LUNAR_LAST_RECEIVE_TIME);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setTwelfthLunarLastReceiveTime(new Date(0));
    		} else {
    			setTwelfthLunarLastReceiveTime(format.parse(lastReceiveTime));
    		}
    		// 设置腊八豆的总个数
    		setTwelfthLunarBeansCount(otherPool.getInt(TWELFTH_LUNAR_BEANS_COUNT));
    		//设置声望
    		setFame(otherPool.getInt(OTHER_POOL_FAME));
    		//设置被呼叫次数
    		setCallCount(otherPool.getInt(OTHER_POOL_CALLCOUNT));
    		// 设置消费额度
    		setActivityConsumer(otherPool.getInt(ACTIVITY_CONSUMER));
    		// 设置新消费额度
    		setNewActivityConsumer(otherPool.getInt(NEW_ACTIVITY_CONSUMER));
    		// 设置新vip等级
    		setVipNewLevel(otherPool.getInt(VIP_NEW_LEVEL));
    		// 设置杀戮点数
    		setCampBattlefieldKillingPoints(otherPool.getInt(CAMP_BATTLEFIELD_KILLING_POINTS));
    		// 设置重置杀戮点数时间
    		String lastTime = otherPool.getString(LAST_RESET_KILLING_POINTS_TIME);
    		if (lastTime == null || lastTime.equals("")) {
    			setLastResetKillPointsTime(new Date(0));
    		} else {
    			setLastResetKillPointsTime(format.parse(lastTime));
    		}
    		//设置消费杀戮点数
    		setKillPointConsume(otherPool.getInt(KILL_POINT_CONSUME));
    		lastTime = otherPool.getString(KILL_POINT_CONSUME_TIME);
    		if(lastTime == null || lastTime.equals("")){
    			setKillPointConsumeTime(new Date(Utils.getTodayStart()));
    		}else{
    			setKillPointConsumeTime(format.parse(lastTime));
    		}
    		//设置属性攻属性
    		setVianyType(otherPool.getInt(VIANYTYPE));
    		//设置表情称号
    		setPhizTitleIndex((short)otherPool.getInt(PHIZTITLE_INDEX));
    		//设置在线时间
    		setOnlineTimer(otherPool.getLong(ONLINE_TIMER, 0));
    		//设置统御力
    		setLeaderShip(otherPool.getInt(LEADERSHIP));
    		//设置魔法i币
    		setMagicIMoney(otherPool.getInt(MAGICMONEY));
    		//设置佣兵
    		setMercentaryData(otherPool.getString(MERCENTARY));
    		// 设置最后一次领取统御值的时间
    		lastReceiveTime = otherPool.getString(GETLEADERSHIPTIME);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setGetLeadershipDate(new Date(0));
    		} else {
    			setGetLeadershipDate(format.parse(lastReceiveTime));
    		}
    		// 设置下一次可以出售佣兵的时间
    		lastReceiveTime = otherPool.getString(SELLNEXTTIME);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setSellNextDate(new Date(0));
    		} else {
    			setSellNextDate(format.parse(lastReceiveTime));
    		}
    		// 设置上一次登陆的时间
    		lastReceiveTime = otherPool.getString(LANDLASTTIME);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setLandLastDate(new Date(0));
    		} else {
    			setLandLastDate(format.parse(lastReceiveTime));
    		}
    		// 设置登陆的次数
    		
    		setLandTimes(otherPool.getInt(LANDTIMES));
    		
    		//  add zjl
    		lastReceiveTime = otherPool.getString(CLOCK);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setClock(new Date(0));
    		} else {
    			setClock(format.parse(lastReceiveTime));
    		}
    		setClockUse(otherPool.getInt(CLOCKUSE));
    		
    		lastReceiveTime = otherPool.getString(CLOCK_TONGSHOP);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setClock_TongShop(new Date(0));
    		} else {
    			setClock_TongShop(format.parse(lastReceiveTime));
    		}
    		lucktime = otherPool.getLong(LUCK_TIME, 0);
    		farmMoney = otherPool.getInt(FARM_MONEY);
    		trainpoint = otherPool.getInt(TRAIN_POINTS);
    		trainattacklevel = otherPool.getInt(ATTACK_LEVEL);
    		trainpdeflevel = otherPool.getInt(PDEFEN_LEVEL);
    		trainmattacklevel = otherPool.getInt(MATTACK_LEVEL);
    		trainmdeflevel = otherPool.getInt(MDEFEN_LEVEL);
    		trainhitlevel = otherPool.getInt(HIT_LEVEL);
    		trainnocrilevel = otherPool.getInt(NOCRI_LEVEL);
    		currentattpoint = otherPool.getInt(CURRENT_ATTACK_POINT);
    		currentpdefpoint = otherPool.getInt(CURRENT_PDEF_POINT);
    		currentmattpoint = otherPool.getInt(CURRENT_MATTACK_POINT);
    		currentmdefpoint = otherPool.getInt(CURRENT_MDEF_POINT);
    		currenthitpoint = otherPool.getInt(CURRENT_HIT_POINT);
    		currentnocripoint = otherPool.getInt(CURRENT_NOCRI_POINT);
    		
//    		//兔子赛跑
//    		jettonNumFir = otherPool.getInt(RABBIT_RACE_JETTON_FIR);
//    		jettonNumSec = otherPool.getInt(RABBIT_RACE_JETTON_SEC);
//    		jettonNumThi = otherPool.getInt(RABBIT_RACE_JETTON_THI);
//    		jettonNumFou = otherPool.getInt(RABBIT_RACE_JETTON_FOU);
//    		jettonNumFif = otherPool.getInt(RABBIT_RACE_JETTON_FIF);
    		
    		replacetimes = otherPool.getInt(REPLACE_TIMES);
    		diamondGiftTimes = otherPool.getInt(GET_DIAMONDGIFTBAG_TIMES);
    		getIdentifyTimes = otherPool.getInt(GET_SUPER_IDENTIFY_TIMES);
    		getSevenLevelFixTimes = otherPool.getInt(GET_SEVEN_LEVEL_TIMES);
    		getforcepkTimes = otherPool.getInt(GET_FORCE_PK_TIMES);
    		bossRushAutoTimes = otherPool.getInt(BOSSRUSH_AUTO_TIMES);
    		bossRushAutoTimesvip4 = otherPool.getInt(BOSSRUSH_AUTO_TIMES_VIP4);
    		bossRushAutoTimesvip5 = otherPool.getInt(BOSSRUSH_AUTO_TIMES_VIP5);
    		getvip3facetimes = otherPool.getInt(GET_VIP3_FACE);
    		getvip4facetimes = otherPool.getInt(GET_VIP4_FACE);
    		getvip5facetimes = otherPool.getInt(GET_VIP5_FACE);
    		
    		//封印法阵
    		waterExp = otherPool.getInt(WATER_MAGIC_POSITION_EXP);
    		soilExp = otherPool.getInt(SOIL_MAGIC_POSITION_EXP);
    		fireExp = otherPool.getInt(FIRE_MAGIC_POSITION_EXP);
    		windExp = otherPool.getInt(WIND_MAGIC_POSITION_EXP);
    		mindExp = otherPool.getInt(MIND_MAGIC_POSITION_EXP);
    		
    		waterlevel = otherPool.getInt(WATER_MAGIC_POSITION_LEVEL);
    		soillevel = otherPool.getInt(SOIL_MAGIC_POSITION_LEVEL);
    		firelevel = otherPool.getInt(FIRE_MAGIC_POSITION_LEVEL);
    		windlevel = otherPool.getInt(WIND_MAGIC_POSITION_LEVEL);
    		mindlevel = otherPool.getInt(MIND_MAGIC_POSITION_LEVEL);
    		
    		waterfloor = otherPool.getInt(WATER_CURRENT_FLOOR);
    		soilfloor = otherPool.getInt(SOIL_CURRENT_FLOOR);
    		firefloor = otherPool.getInt(FIRE_CURRENT_FLOOR);
    		windfloor = otherPool.getInt(WIND_CURRENT_FLOOR);
    		mindfloor = otherPool.getInt(MIND_CURRENT_FLOOR);
    		//end
    		
    		//花钱开宝箱
    		awardBoxItemId = otherPool.getInt(AWARD_BOX_ITEM_ID);
    		
    		lastReceiveTime = otherPool.getString(REPLACE_CLOCK);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setReplaceclock(new Date(0));
    		}else{
    			setReplaceclock(format.parse(lastReceiveTime));
    		}
    		
    		//使用聚灵点时间
    		lastReceiveTime = otherPool.getString(WEEK_USE_TRAINPOINT_CLOCK);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setUseTrainpointClock(new Date(0));
    		}else{
    			setUseTrainpointClock(format.parse(lastReceiveTime));
    		}
    		
    		playerUseTrainpoint = otherPool.getInt(USE_TRAINPOINT);
    		
    		//自动爬塔时间
    		lastReceiveTime = otherPool.getString(AUTO_PASS_STAGE_CLOCK);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setAutoStageClock(new Date(0));
    		}else{
    			setAutoStageClock(format.parse(lastReceiveTime));
    		}
    		//强制pk时间
    		lastReceiveTime = otherPool.getString(FORCE_PK_CLOCK);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setForcePkClock(new Date(0));
    		}else{
    			setForcePkClock(format.parse(lastReceiveTime));
    		}
    		
    		//vip5发公告时间
    		lastReceiveTime = otherPool.getString(VIP5_MESSAGE_UP);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setVip5MessageUpClock(new Date(0));
    		}else{
    			setVip5MessageUpClock(format.parse(lastReceiveTime));
    		}
    		
    		lastReceiveTime = otherPool.getString(VIP5_MESSAGE_DOWN);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setVip5MessageDownClock(new Date(0));
    		}else{
    			setVip5MessageDownClock(format.parse(lastReceiveTime));
    		}
    		
    		setBossRushStageBest(otherPool.getInt(BOSSRUSH_STAGE_BEST));
    		setBossRushStage(otherPool.getInt(BOSSRUSH_STAGE));
    		otherPool.deleteByName("bossRushBoutCount");
    		setBossRushBoutList(otherPool.getString(BOSSRUSH_BOUTCOUNT));
    		setBossRushTime(otherPool.getInt(BOSSRUSH_TIME));
    		lastReceiveTime = otherPool.getString(BOSSRUSH_LASTTIME);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setBossRushLastTime(new Date(0));
    		}else{
    			setBossRushLastTime(format.parse(lastReceiveTime));
    		}
    		initAddAttributes(otherPool.getString(ADDATTRIBUTE_VALUE));
    		initAwardBoxItemIdArray(otherPool.getString(AWARD_BOX_ITEMID_ARRAY));
    		//圣诞许愿
    		setChristmasWishingNormal_Count(otherPool.getInt(CHRISTMAS_WISHING_NORMAL_COUNT));
    		lastReceiveTime = otherPool.getString(CHRISTMAS_WISHING_NORMAL_LASTTIME);
    		if(lastReceiveTime == null ||lastReceiveTime.equals("")) {
    			setChristmasWishingNormal_LastTime(new Date(0));
    		}else{
    			setChristmasWishingNormal_LastTime(format.parse(lastReceiveTime));
    		}
    		//端午节兑换
    		setDragonBoatFestivalReplaceCount(otherPool.getInt(DRAGON_BOAT_FESTIVAL_REPLACE_COUNT));
    		lastReceiveTime = otherPool.getString(DRAGON_BOAT_FESTIVAL_REPLACE_LASTTIME);
    		if(lastReceiveTime == null ||lastReceiveTime.equals("")) {
    			setDragonBoastFestivalReplaceLastTime(new Date(0));
    		}else{
    			setDragonBoastFestivalReplaceLastTime(format.parse(lastReceiveTime));
    		}
    		
    		//宝石碎块兑换时间
    		lastReceiveTime = otherPool.getString(REPLACE_DIAMOND_TIME);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")){
    			setPieceReplaceTime(new Date(0));
    		}else{
    			setPieceReplaceTime(format.parse(lastReceiveTime));
    		}
    		//安卓版下载积分
    		setDownloadPoint(otherPool.getInt(DOWNLOAD_POINTS));
    		setDownloadPoint_useValue(otherPool.getInt(DOWNLOADPOINT_USEVALUE));
    		lastReceiveTime = otherPool.getString(DOWNLOADPOINT_LASTTIME);
    		if(lastReceiveTime == null || lastReceiveTime.equals("")){
    			setDownloadPoint_LastTime(new Date(0));
    		}else{
    			setDownloadPoint_LastTime(format.parse(lastReceiveTime));
    		}
    		
    		//领取劳动种子时间
    		lastReceiveTime = otherPool.getString(GET_SEED_TIME);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setSeedclock(new Date(0));
    		}else{
    			setSeedclock(format.parse(lastReceiveTime));
    		}
    		
    		//宠物培养时间记录
    		lastReceiveTime = otherPool.getString(PetDevelopTime);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setPetDevelopTime(new Date(0));
    		}else{
    			setPetDevelopTime(format.parse(lastReceiveTime));
    		}
    		
    		//宠物培养过程数值记录
    		lastReceiveTime = otherPool.getString(PetDevelopAddValue);
    		if(lastReceiveTime != null){
    			String[] value = lastReceiveTime.split(",");
    			if(value.length == petDevelopValue.length){
	    			for(int i=0; i<petDevelopValue.length; i++){
	    				petDevelopValue[i] = Integer.parseInt(value[i]);
	    			}
    			}
    		}
    		
    		//宠物进化时间记录
    		lastReceiveTime = otherPool.getString(PetEvolutionTime);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setPetEvolutionTime(new Date(0));
    		}else{
    			setPetEvolutionTime(format.parse(lastReceiveTime));
    		}
    		
    		setPetEvolutionCount(otherPool.getInt(PetEvolutionCount));
    	} else {
    		//设置当前活力值
    		setLifeValue(player.getOtherPool().getInt(OTHER_POOL_LIFEVALUE));
    		//设置当前离线经验值
    		setUnlineExp(player.getOtherPool().getInt(OTHER_POOL_UNLINEEXP));
    		// 设置离线的时间
    		String strUnlineDate = player.getOtherPool().getString(OTHER_POOL_UNLINEDATE);
    		if (strUnlineDate != null && strUnlineDate.equals("") == false) {
    			setUnlineDate(format.parse(strUnlineDate));
    		}
    		//设置在线活力积累值
    		setUnlineOnlineLife(player.getOtherPool().getInt(OTHER_POOL_UNLINEONLINELIFE));
    		// 设置腊八消费金额I币
    		setTwelfthLunarConsumer(player.getOtherPool().getInt(TWELFTH_LUNAR_CONSUMER));
    		// 设置最后一次领取腊八粥的时间
    		String lastReceiveTime = player.getOtherPool().getString(TWELFTH_LUNAR_LAST_RECEIVE_TIME);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setTwelfthLunarLastReceiveTime(new Date(0));
    		} else {
    			setTwelfthLunarLastReceiveTime(format.parse(lastReceiveTime));
    		}
    		// 设置腊八豆的总个数
    		setTwelfthLunarBeansCount(player.getOtherPool().getInt(TWELFTH_LUNAR_BEANS_COUNT));
    		//设置声望
    		setFame(player.getOtherPool().getInt(OTHER_POOL_FAME));
    		//设置被呼叫次数
    		setCallCount(player.getOtherPool().getInt(OTHER_POOL_CALLCOUNT));
    		// 设置消费额度
    		setActivityConsumer(player.getOtherPool().getInt(ACTIVITY_CONSUMER));
    		// 设置新消费额度
    		setNewActivityConsumer(player.getOtherPool().getInt(NEW_ACTIVITY_CONSUMER));
    		// 设置新vip等级
    		setVipNewLevel(player.getOtherPool().getInt(VIP_NEW_LEVEL));
    		// 设置杀戮点数
    		setCampBattlefieldKillingPoints(player.getOtherPool().getInt(CAMP_BATTLEFIELD_KILLING_POINTS));
    		// 设置重置杀戮点数时间
    		String lastTime = player.getOtherPool().getString(LAST_RESET_KILLING_POINTS_TIME);
    		if (lastTime == null || lastTime.equals("")) {
    			setLastResetKillPointsTime(new Date(0));
    		} else {
    			setLastResetKillPointsTime(format.parse(lastTime));
    		}
    		//设置消费杀戮点数
    		setKillPointConsume(player.getOtherPool().getInt(KILL_POINT_CONSUME));
    		lastTime = player.getOtherPool().getString(KILL_POINT_CONSUME_TIME);
    		if(lastTime == null || lastTime.equals("")){
    			setKillPointConsumeTime(new Date(Utils.getTodayStart()));
    		}else{
    			setKillPointConsumeTime(format.parse(lastTime));
    		}
    		//设置属性攻属性
    		setVianyType(player.getOtherPool().getInt(VIANYTYPE));
    		//设置表情称号
    		setPhizTitleIndex((short)player.getOtherPool().getInt(PHIZTITLE_INDEX));
    		//设置在线时间
    		setOnlineTimer(player.getOtherPool().getLong(ONLINE_TIMER, 0));
    		//设置统御力
    		setLeaderShip(player.getOtherPool().getInt(LEADERSHIP));
    		//设置魔法i币
    		setMagicIMoney(player.getOtherPool().getInt(MAGICMONEY));
    		//设置佣兵
    		setMercentaryData(player.getOtherPool().getString(MERCENTARY));
    		// 设置最后一次领取统御值的时间
    		lastReceiveTime = player.getOtherPool().getString(GETLEADERSHIPTIME);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setGetLeadershipDate(new Date(0));
    		} else {
    			setGetLeadershipDate(format.parse(lastReceiveTime));
    		}
    		// 设置下一次可以出售佣兵的时间
    		lastReceiveTime = player.getOtherPool().getString(SELLNEXTTIME);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setSellNextDate(new Date(0));
    		} else {
    			setSellNextDate(format.parse(lastReceiveTime));
    		}
    		// 设置上一次登陆的时间
    		lastReceiveTime = player.getOtherPool().getString(LANDLASTTIME);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setLandLastDate(new Date(0));
    		} else {
    			setLandLastDate(format.parse(lastReceiveTime));
    		}
    		setLandTimes(player.getOtherPool().getInt(LANDTIMES));
    		//  add zjl
    		lastReceiveTime = player.getOtherPool().getString(CLOCK);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setClock(new Date(0));
    		} else {
    			setClock(format.parse(lastReceiveTime));
    		}
    		setClockUse(player.getOtherPool().getInt(CLOCKUSE));
    		
    		lastReceiveTime = player.getOtherPool().getString(CLOCK_TONGSHOP);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setClock_TongShop(new Date(0));
    		} else {
    			setClock_TongShop(format.parse(lastReceiveTime));
    		}
    		setClockUse_TongShop(player.getOtherPool().getInt(CLOCKUSE_TONGSHOP));
    		
    		lucktime = player.getOtherPool().getLong(LUCK_TIME, 0);
    		farmMoney = player.getOtherPool().getInt(FARM_MONEY, 0);
    		trainpoint = player.getOtherPool().getInt(TRAIN_POINTS, 0);
    		trainattacklevel =  player.getOtherPool().getInt(ATTACK_LEVEL, 0);
    		trainpdeflevel = player.getOtherPool().getInt(PDEFEN_LEVEL,0);
    		trainmattacklevel = player.getOtherPool().getInt(MATTACK_LEVEL,0);
    		trainmdeflevel = player.getOtherPool().getInt(MDEFEN_LEVEL,0);
    		trainhitlevel = player.getOtherPool().getInt(HIT_LEVEL,0);
    		trainnocrilevel = player.getOtherPool().getInt(NOCRI_LEVEL,0);
    		currentattpoint = player.getOtherPool().getInt(CURRENT_ATTACK_POINT,0);
    		currentpdefpoint = player.getOtherPool().getInt(CURRENT_PDEF_POINT,0);
    		currentmattpoint = player.getOtherPool().getInt(CURRENT_MATTACK_POINT,0);
    		currentmdefpoint = player.getOtherPool().getInt(CURRENT_MDEF_POINT,0);
    		currenthitpoint = player.getOtherPool().getInt(CURRENT_HIT_POINT,0);
    		currentnocripoint = player.getOtherPool().getInt(CURRENT_NOCRI_POINT,0);
    		replacetimes = player.getOtherPool().getInt(REPLACE_TIMES,0);
    		diamondGiftTimes = player.getOtherPool().getInt(GET_DIAMONDGIFTBAG_TIMES,0);
    		getIdentifyTimes = player.getOtherPool().getInt(GET_SUPER_IDENTIFY_TIMES, 0);
    		getSevenLevelFixTimes = player.getOtherPool().getInt(GET_SEVEN_LEVEL_TIMES, 0);
    		getforcepkTimes = player.getOtherPool().getInt(GET_FORCE_PK_TIMES,0);
    		bossRushAutoTimes = player.getOtherPool().getInt(BOSSRUSH_AUTO_TIMES, 0);
    		bossRushAutoTimesvip4 = player.getOtherPool().getInt(BOSSRUSH_AUTO_TIMES_VIP4, 0);
    		bossRushAutoTimesvip5 = player.getOtherPool().getInt(BOSSRUSH_AUTO_TIMES_VIP5, 0);
    		getvip3facetimes = player.getOtherPool().getInt(GET_VIP3_FACE, 0);
    		getvip4facetimes = player.getOtherPool().getInt(GET_VIP4_FACE, 0);
    		getvip5facetimes = player.getOtherPool().getInt(GET_VIP5_FACE, 0);
    		
//    		//兔子赛跑
//    		jettonNumFir = player.getOtherPool().getInt(RABBIT_RACE_JETTON_FIR, 0);
//    		jettonNumSec = player.getOtherPool().getInt(RABBIT_RACE_JETTON_SEC, 0);
//    		jettonNumThi = player.getOtherPool().getInt(RABBIT_RACE_JETTON_THI, 0);
//    		jettonNumFou = player.getOtherPool().getInt(RABBIT_RACE_JETTON_FOU, 0);
//    		jettonNumFif = player.getOtherPool().getInt(RABBIT_RACE_JETTON_FIF, 0);
    		
    		//花钱开宝箱
    		awardBoxItemId = player.getOtherPool().getInt(AWARD_BOX_ITEM_ID);
    		
    		//封印法阵
    		waterExp = player.getOtherPool().getInt(WATER_MAGIC_POSITION_EXP,0);
    		soilExp = player.getOtherPool().getInt(SOIL_MAGIC_POSITION_EXP, 0);
    		fireExp = player.getOtherPool().getInt(FIRE_MAGIC_POSITION_EXP, 0);
    		windExp = player.getOtherPool().getInt(WIND_MAGIC_POSITION_EXP, 0);
    		mindExp = player.getOtherPool().getInt(MIND_MAGIC_POSITION_EXP, 0);
    		
    		waterlevel = player.getOtherPool().getInt(WATER_MAGIC_POSITION_LEVEL, 0);
    		soillevel = player.getOtherPool().getInt(SOIL_MAGIC_POSITION_LEVEL, 0);
    		firelevel = player.getOtherPool().getInt(FIRE_MAGIC_POSITION_LEVEL, 0);
    		windlevel = player.getOtherPool().getInt(WIND_MAGIC_POSITION_LEVEL, 0);
    		mindlevel = player.getOtherPool().getInt(MIND_MAGIC_POSITION_LEVEL, 0);
    		
    		waterfloor = player.getOtherPool().getInt(WATER_CURRENT_FLOOR, 0);
    		soilfloor = player.getOtherPool().getInt(SOIL_CURRENT_FLOOR, 0);
    		firefloor = player.getOtherPool().getInt(FIRE_CURRENT_FLOOR, 0);
    		windfloor = player.getOtherPool().getInt(WIND_CURRENT_FLOOR, 0);
    		mindfloor = player.getOtherPool().getInt(MIND_CURRENT_FLOOR, 0);
    		//end
    		
    		
    		lastReceiveTime = player.getOtherPool().getString(REPLACE_CLOCK);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setReplaceclock(new Date(0));
    		}else{
    			setReplaceclock(format.parse(lastReceiveTime));
    		}
    		
    		//使用聚灵点时间
    		lastReceiveTime = player.getOtherPool().getString(WEEK_USE_TRAINPOINT_CLOCK);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setUseTrainpointClock(new Date(0));
    		}else{
    			setUseTrainpointClock(format.parse(lastReceiveTime));
    		}
    		
    		playerUseTrainpoint = player.getOtherPool().getInt(USE_TRAINPOINT,0);
    		
    		lastReceiveTime = player.getOtherPool().getString(AUTO_PASS_STAGE_CLOCK);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setAutoStageClock(new Date(0));
    		}else{
    			setAutoStageClock(format.parse(lastReceiveTime));
    		}
    		
    		lastReceiveTime = player.getOtherPool().getString(FORCE_PK_CLOCK);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setForcePkClock(new Date(0));
    		}else{
    			setForcePkClock(format.parse(lastReceiveTime));
    		}
    		
    		lastReceiveTime = player.getOtherPool().getString(VIP5_MESSAGE_UP);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setVip5MessageUpClock(new Date(0));
    		}else{
    			setVip5MessageUpClock(format.parse(lastReceiveTime));
    		}
    		
    		lastReceiveTime = player.getOtherPool().getString(VIP5_MESSAGE_DOWN);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setVip5MessageDownClock(new Date(0));
    		}else{
    			setVip5MessageDownClock(format.parse(lastReceiveTime));
    		}
    		
    		setBossRushStageBest(player.getOtherPool().getInt(BOSSRUSH_STAGE_BEST,0));
    		setBossRushStage(player.getOtherPool().getInt(BOSSRUSH_STAGE,0));
    		player.getOtherPool().deleteByName("bossRushBoutCount");
    		setBossRushBoutList(player.getOtherPool().getString(BOSSRUSH_BOUTCOUNT));
    		setBossRushTime(player.getOtherPool().getInt(BOSSRUSH_TIME));
    		lastReceiveTime = player.getOtherPool().getString(BOSSRUSH_LASTTIME);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setBossRushLastTime(new Date(0));
    		}else{
    			setBossRushLastTime(format.parse(lastReceiveTime));
    		}
    		initAddAttributes(player.getOtherPool().getString(ADDATTRIBUTE_VALUE));
    		initAwardBoxItemIdArray(player.getOtherPool().getString(AWARD_BOX_ITEMID_ARRAY));
    		//圣诞许愿
    		setChristmasWishingNormal_Count(player.getOtherPool().getInt(CHRISTMAS_WISHING_NORMAL_COUNT));
    		lastReceiveTime = player.getOtherPool().getString(CHRISTMAS_WISHING_NORMAL_LASTTIME);
    		if(lastReceiveTime == null ||lastReceiveTime.equals("")) {
    			setChristmasWishingNormal_LastTime(new Date(0));
    		}else{
    			setChristmasWishingNormal_LastTime(format.parse(lastReceiveTime));
    		}
    		//端午节兑换
    		setDragonBoatFestivalReplaceCount(player.getOtherPool().getInt(DRAGON_BOAT_FESTIVAL_REPLACE_COUNT));
    		lastReceiveTime = player.getOtherPool().getString(DRAGON_BOAT_FESTIVAL_REPLACE_LASTTIME);
    		if(lastReceiveTime == null ||lastReceiveTime.equals("")) {
    			setDragonBoastFestivalReplaceLastTime(new Date(0));
    		}else{
    			setDragonBoastFestivalReplaceLastTime(format.parse(lastReceiveTime));
    		}
    		//宝石碎块兑换时间
    		lastReceiveTime = player.getOtherPool().getString(REPLACE_DIAMOND_TIME);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setPieceReplaceTime(new Date(0));
    		} else {
    			setPieceReplaceTime(format.parse(lastReceiveTime));
    		}
    		//安卓版下载积分
    		setDownloadPoint(player.getOtherPool().getInt(DOWNLOAD_POINTS));
    		setDownloadPoint_useValue(player.getOtherPool().getInt(DOWNLOADPOINT_USEVALUE));
    		lastReceiveTime = player.getOtherPool().getString(DOWNLOADPOINT_LASTTIME);
    		if(lastReceiveTime == null || lastReceiveTime.equals("")){
    			setDownloadPoint_LastTime(new Date(0));
    		}else{
    			setDownloadPoint_LastTime(format.parse(lastReceiveTime));
    		}
    		
    		lastReceiveTime = player.getOtherPool().getString(GET_SEED_TIME);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setSeedclock(new Date(0));
    		} else {
    			setSeedclock(format.parse(lastReceiveTime));
    		}
    		
    		//宠物培养时间记录
    		lastReceiveTime = player.getOtherPool().getString(PetDevelopTime);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setPetDevelopTime(new Date(0));
    		} else {
    			setPetDevelopTime(format.parse(lastReceiveTime));
    		}
    		
    		//宠物培养过程数值记录
    		lastReceiveTime = player.getOtherPool().getString(PetDevelopAddValue);
    		if(lastReceiveTime != null){
    			String[] value = lastReceiveTime.split(",");
    			if(value.length == petDevelopValue.length){
	    			for(int i=0; i<petDevelopValue.length; i++){
	    				petDevelopValue[i] = Integer.parseInt(value[i]);
	    			}
    			}
    		}
    		
    		//宠物进化时间记录
    		lastReceiveTime = player.getOtherPool().getString(PetEvolutionTime);
    		if (lastReceiveTime == null || lastReceiveTime.equals("")) {
    			setPetEvolutionTime(new Date(0));
    		} else {
    			setPetEvolutionTime(format.parse(lastReceiveTime));
    		}
    		
    		setPetEvolutionCount(player.getOtherPool().getInt(PetEvolutionCount));
    		
    		otherPool = player.getOtherPool().clone();
    	}
    	
    	if(getPetEvolutionTime() != null){
    		long time = getPetEvolutionTime().getTime();
    		Calendar cal = Calendar.getInstance();
    		cal.set(Calendar.YEAR, 2012);
    		cal.set(Calendar.MONTH, Calendar.DECEMBER);
    		cal.set(Calendar.DAY_OF_MONTH, 4);
    		cal.set(Calendar.HOUR_OF_DAY, 11);
    		cal.set(Calendar.MINUTE, 0);
    		cal.set(Calendar.SECOND, 0);
    		if(time < cal.getTimeInMillis()){
    			log.info("Pet evolution time < 2012-12-04 11:00");
    			if(pets != null && pets.size() > 0){
    				for(int i=0; i<pets.size(); i++){
    					Pet pet = (Pet)pets.get(i);
    					if(pet != null){
    						if(pet.getEvolutionLevel() > 0 || pet.getEvolutionPoint() > 0){
    							log.info("PlayerID[" + getId() + "] petID[" + pet.getId() + "] evolutionLevel[" + pet.getEvolutionLevel() + "] evolutionPoint[" + pet.getEvolutionPoint() + "] evolutionType[" + pet.getEvolutionType() + "]");
    							pet.setEvolutionLevel(0);
    							pet.setEvolutionPoint(0);
    							setPetEvolutionTime(new Date());
    							setPetEvolutionCount(0);
    						}
    					}
    				}
    			}
    		}
    	}
    	
    }
    private void resetOtherPool() {
    	resetLifeValue();
    	resetUnlineExp();
    	resetUnlineDate();
    	resetUnlineOnlineLife();
    	resetTwelfthLunarConsumer();
    	resetTwelfthLunarLastReceiveTime();
    	resetTwelfthLunarBeansCount();
    	resetFame();
    	resetCallCount();
    	resetActivityConsumer();
    	resetVipNewLevel();
    	resetNewActivityConsumer();
    	resetCampBattlefieldKillPoints();
    	resetLastResetKillPointsTime();
    	resetKillPointConsume();
    	resetKillPointConsumeDate();
    	resetVianyType();
    	resetLandTimes();
    	resetPhizTitle();
    	resetOnlineTimer();
    	resetLeaderShip();
    	resetMercentaryData();
    	resetGetLeadershipDate();
    	resetSellNextDate();
    	resetLandLastDate();
    	resetMagicIMoney();
    	resetClock();
    	resetClockUse();
    	resetClock_TongShop();
    	resetClockUse_TongShop();
    	resetLuckTime();
    	resetFarmMoney();
    	resetTrainPoint();
    	resetTrainAttackLevel();
    	resetTrainPdefLevel();
    	resetTrainMattackLevel();
    	resetTrainMdefLevel();
    	resetTrainHitLevel();
    	resetTrainNocriLevel();
//    	resetJettonNumFif();
//    	resetJettonNumFir();
//    	resetJettonNumFou();
//    	resetJettonNumSec();
//    	resetJettonNumThi();
    	resetCurrentAttPoint();
    	resetCurrentPdefPoint();
    	resetCurrentMattPoint();
    	resetCurrentMdefPoint();
    	resetCurrentHitPoint();
    	resetCurrentNocriPoint();
    	resetReplaceTimes();
    	resetDiamondGiftTimes();
    	resetIdentifyTimes();
    	resetSevenLevelFixTimes();
    	resetForcePkTimes();
    	resetBossRushAutoTimes();
    	resetBossRushAutoTimesVip4();
    	resetBossRushAutoTimesVip5();
    	resetVip3FaceTimes();
    	resetVip4FaceTimes();
    	resetVip5FaceTimes();
    	resetReplaceclock();
    	resetUseTrainpointClock();
    	resetAutoStageClock();
    	resetForcePkClock();
    	resetVip5MessageUpClock();
    	resetVip5MessageDownClock();
    	resetUseTrainPoint();
    	resetBossRushStageBest();
    	resetBossRushStage();
    	resetBossRushBoutCount();
    	resetBossRushTime();
    	resetBossRushLastTime();
    	resetAddAttributes();
    	resetAwardBoxItemIdArray();
    	resetChristmasWishingNormal_Count();
    	resetDragonBoatFestivalReplaceCount();
    	resetChristmasWishingNormal_LastTime();
    	resetDragonBoatFestivalReplaceLastTime();
    	resetPieceReplaceTime();
    	resetDownloadPoint();
    	resetDownloadPoint_useTime();
    	resetDownloadPoint_lastTime();
    	resetGetSeedclock();
    	resetWaterExp();
    	resetWaterLevel();
    	resetWaterFloor();
    	resetSoilExp();
    	resetSoilLevel();
    	resetSoilFloor();
    	resetFireExp();
    	resetFireLevel();
    	resetFireFloor();
    	resetWindExp();
    	resetWindLevel();
    	resetWindFloor();
    	resetMindExp();
    	resetMindLevel();
    	resetMindFloor();
    	resetAwardBoxItemId();
    	resetPetDevelopTime();
    	resetPetDevelopValue();
    	resetPetEvolutionTime();
    	resetPetEvolutionCount();
    	setOtherPool(otherPool);
    }
    public PropertyPool getOtherPool () {
    	return otherPool;
    }
    public void setOtherPool (PropertyPool otherPool) {
    	player.setOtherPool(otherPool);
    }
    public void setLifeValue(int lifeValue){
    	this.lifeValue = lifeValue;
    }
    public int getLifeValue(){
    	return lifeValue;
    }
    public void resetLifeValue() {
    	getOtherPool().setInt(OTHER_POOL_LIFEVALUE, getLifeValue());
    }
    public void setUnlineExp(int unlineExp){
    	this.unlineExp = unlineExp;
    }
    public int getUnlineExp(){
    	return unlineExp;
    }
    protected void resetUnlineExp(){
    	getOtherPool().setInt(OTHER_POOL_UNLINEEXP, getUnlineExp());
    }
    public void setUnlineDate(Date date){
    	unlineDateTime = date;
    }
    public Date getUnlineDate(){
    	return unlineDateTime;
    }
    protected void resetUnlineDate(){
    	if (unlineDateTime != null) {
    		getOtherPool().setString(OTHER_POOL_UNLINEDATE, format.format(unlineDateTime));
    	} else {
    		getOtherPool().setString(OTHER_POOL_UNLINEDATE, "");
    	}
    }
    public void setUnlineOnlineLife(int life){
    	unlineOnlineLife = life;
    }
    public int getUnlineOnlineLife(){
    	return unlineOnlineLife;
    }
    public void resetUnlineOnlineLife(){
    	getOtherPool().setInt(OTHER_POOL_UNLINEONLINELIFE, unlineOnlineLife);
    }
    public void setFame(int fame){
    	this.fame = fame;
    }
    public int getFame(){
    	return fame;
    }
    public void resetFame(){
    	getOtherPool().setInt(OTHER_POOL_FAME, getFame());
    }
    public void setCallCount(int callCount){
    	this.callCount = callCount;
    }
    public int getCallCount(){
    	return callCount;
    }
    public void resetCallCount(){
    	getOtherPool().setInt(OTHER_POOL_CALLCOUNT, getCallCount());
    }
    public void setApprentices(Master[] apprentices){
    	this.apprentices = apprentices;
    }
    public Master[] getApprentices(){
    	return apprentices;
    }
    public long getApprenticeFindMasterTime(){
    	return apprenticeFindMasterTime;
    }
    public void setApprenticeFindMasterTime(long apprenticeFindMasterTime){
    	this.apprenticeFindMasterTime = apprenticeFindMasterTime;
    }
    
    protected void resetTwelfthLunarConsumer () {
    	getOtherPool().setInt(TWELFTH_LUNAR_CONSUMER, getTwelfthLunarConsumer ());
    }
    protected void resetTwelfthLunarLastReceiveTime () {
    	getOtherPool().setString(TWELFTH_LUNAR_LAST_RECEIVE_TIME, format.format(getTwelfthLunarLastReceiveTime()));
    }
    protected void resetTwelfthLunarBeansCount () {
    	getOtherPool().setInt(TWELFTH_LUNAR_BEANS_COUNT, getTwelfthLunarBeansCount());
    }
    protected void resetActivityConsumer () {
    	getOtherPool().setInt(ACTIVITY_CONSUMER, getActivityConsumer());
    }
    protected void resetVipNewLevel (){
    	getOtherPool().setInt(VIP_NEW_LEVEL, getVipNewLevel());
    }
    protected void resetNewActivityConsumer () {
    	getOtherPool().setInt(NEW_ACTIVITY_CONSUMER, getNewActivityConsumer());
    }
    protected void resetCampBattlefieldKillPoints () {
    	getOtherPool().setInt(CAMP_BATTLEFIELD_KILLING_POINTS, getCampBattlefieldKillPoints());
    }
    protected void resetLastResetKillPointsTime () {
    	getOtherPool().setString(LAST_RESET_KILLING_POINTS_TIME, format.format(getLastResetKillPointsTime()));
    }
    
    public void resetKillPointConsume(){
    	getOtherPool().setInt(KILL_POINT_CONSUME, killPointConsume);
    }
    
    public void resetKillPointConsumeDate(){
    	getOtherPool().setString(KILL_POINT_CONSUME_TIME, format.format(killPointConsumeTime));
    }
    
    public void resetVianyType(){
    	getOtherPool().setInt(VIANYTYPE, vianyType);
    }
    
    public void resetPhizTitle(){
    	getOtherPool().setInt(PHIZTITLE_INDEX, phizTitleIndex);
    }
    
    public void resetOnlineTimer(){
    	getOtherPool().setLong(ONLINE_TIMER, onlineTimer);
    }
    
    public void resetLeaderShip(){
    	getOtherPool().setInt(LEADERSHIP, leadership);
    }
    
    public void resetMagicIMoney(){
    	getOtherPool().setInt(MAGICMONEY, magicimoney); 	
    }
    
    public void resetClockUse(){
    	getOtherPool().setInt(CLOCKUSE, clockUse);
    }
    
    public void resetClockUse_TongShop(){
    	getOtherPool().setInt(CLOCKUSE_TONGSHOP, clockUse_tongShop);
    }
    
    public void resetLuckTime(){
    	getOtherPool().setLong(LUCK_TIME, lucktime);
    }
    
    public long getLuckTime(){
    	return lucktime;
    }
    
    public void setLuckTime(long lucktime){
    	this.lucktime = lucktime;
    }
    
    public void resetPieceReplaceTime(){
    	getOtherPool().setString(REPLACE_DIAMOND_TIME, format.format(replace_diamond_time));
    }
    
    public Date getPieceReplaceTime(){
    	return replace_diamond_time;
    }
    
    public void setPieceReplaceTime(Date time){
    	this.replace_diamond_time = time;
    }
    
    
    public void resetFarmMoney(){
    	getOtherPool().setInt(FARM_MONEY, farmMoney);
    }
    
    public void setFarmMoney(int farmMoney){
    	this.farmMoney = farmMoney;
    }
    
    public int getFarmMoney(){
    	return farmMoney;
    }
    
    public void resetTrainPoint(){
    	getOtherPool().setInt(TRAIN_POINTS, trainpoint);
    }
    
    public void setTrainPoint(int point){
    	this.trainpoint = point;
    }
    
    public int getTrainPoint(){
    	return this.trainpoint;
    }
    
    //当前物攻聚灵点
    public void resetCurrentAttPoint(){
    	getOtherPool().setInt(CURRENT_ATTACK_POINT, currentattpoint);
    }
    
    public void setCurrentAttPoint(int point){
    	this.currentattpoint = point;
    }
    
    public int getCurrentAttPoint(){
    	return this.currentattpoint;
    }
    
    //物防
    public void resetCurrentPdefPoint(){
    	getOtherPool().setInt(CURRENT_PDEF_POINT, currentpdefpoint);
    }
    
    public void setCurrentPdefPoint(int point){
    	this.currentpdefpoint = point;
    }
    
    public int getCurrentPdefPoint(){
    	return this.currentpdefpoint;
    }
    
    //魔攻
    public void resetCurrentMattPoint(){
    	getOtherPool().setInt(CURRENT_MATTACK_POINT, currentmattpoint);
    }
    
    public void setCurrentMattPoint(int point){
    	this.currentmattpoint = point;
    }
    
    public int getCurrentMattPoint(){
    	return this.currentmattpoint;
    }
    
    //魔防
    public void resetCurrentMdefPoint(){
    	getOtherPool().setInt(CURRENT_MDEF_POINT, currentmdefpoint);
    }
    
    public void setCurrentMdefPoint(int point){
    	this.currentmdefpoint = point;
    }
    
    public int getCurrentMdefPoint(){
    	return this.currentmdefpoint;
    }
    
    //命中
    public void resetCurrentHitPoint(){
    	getOtherPool().setInt(CURRENT_HIT_POINT, currenthitpoint);
    }
    
    public void setCurrentHitPoint(int point){
    	this.currenthitpoint = point;
    }
    
    public int getCurrentHitPoint(){
    	return this.currenthitpoint;
    }
    
    //免爆
    public void resetCurrentNocriPoint(){
    	getOtherPool().setInt(CURRENT_NOCRI_POINT, currentnocripoint);
    }
    
    public void setCurrentNocriPoint(int point){
    	this.currentnocripoint = point;
    }
    
    public int getCurrentNocriPoint(){
    	return this.currentnocripoint;
    }
    
    
    //物攻等级
    public void resetTrainAttackLevel(){
    	getOtherPool().setInt(ATTACK_LEVEL, trainattacklevel);
    }
    
    public void setTrainAttackLevel(int level){
    	this.trainattacklevel = level;
    }
    
    public int getTrainAttackLevel(){
    	return this.trainattacklevel;
    }
    
    //魔攻
    public void resetTrainMattackLevel(){
    	getOtherPool().setInt(MATTACK_LEVEL, trainmattacklevel);
    }
    
    public void setTrainMattackLevel(int level){
    	this.trainmattacklevel = level;
    }
    
    public int getTrainMattackLevel(){
    	return this.trainmattacklevel;
    }

    //物防
    public void resetTrainPdefLevel(){
    	getOtherPool().setInt(PDEFEN_LEVEL, trainpdeflevel);
    }
    
    public void setTrainPdefLevel(int level){
    	this.trainpdeflevel = level;
    }
    
    public int getTrainPdefLevel(){
    	return this.trainpdeflevel;
    }
    
    //魔防
    public void resetTrainMdefLevel(){
    	getOtherPool().setInt(MDEFEN_LEVEL, trainmdeflevel);
    }
    
    public void setTrainMdefLevel(int level){
    	this.trainmdeflevel = level;
    }
    
    public int getTrainMdefLevel(){
    	return this.trainmdeflevel;
    }
    
    //命中
    public void resetTrainHitLevel(){
    	getOtherPool().setInt(HIT_LEVEL, trainhitlevel);
    }
    
    public void setTrainHitLevel(int level){
    	this.trainhitlevel = level;
    }
    
    public int getTrainHitLevel(){
    	return this.trainhitlevel;
    }
    
    //免爆
    public void resetTrainNocriLevel(){
    	getOtherPool().setInt(NOCRI_LEVEL, trainnocrilevel);
    }
    
    public void setTrainNocriLevel(int level){
    	this.trainnocrilevel = level;
    }
    
    public int getTrainNocriLevel(){
    	return this.trainnocrilevel;
    }
    
    //领取宝石礼包次数
    public void resetDiamondGiftTimes(){
    	getOtherPool().setInt(GET_DIAMONDGIFTBAG_TIMES, diamondGiftTimes);
    }
    
    public void setDiamondGiftTimes(int times){
    	this.diamondGiftTimes = times;
    }
    
    public int getDiamondGiftTimes(){
    	return this.diamondGiftTimes;
    }
    
    //领取超级鉴定符次数
    public void resetIdentifyTimes(){
    	getOtherPool().setInt(GET_SUPER_IDENTIFY_TIMES, getIdentifyTimes);
    }
    
    public void setIdentifyTimes(int times){
    	this.getIdentifyTimes = times;
    }
    
    public int getIdentifyTimes(){
    	return this.getIdentifyTimes;
    }
    
    //领取7级定向包次数
    public void resetSevenLevelFixTimes(){
    	getOtherPool().setInt(GET_SEVEN_LEVEL_TIMES, getSevenLevelFixTimes);
    }
    
    public void setSevenLevelFixTimes(int times){
    	this.getSevenLevelFixTimes = times;
    }
    
    public int getSevenLevelFixTimes(){
    	return this.getSevenLevelFixTimes;
    }
    
    //领取强制pk药水次数
    public void resetForcePkTimes(){
    	getOtherPool().setInt(GET_FORCE_PK_TIMES, getforcepkTimes);
    }
    
    public void setForcePkTimes(int times){
    	this.getforcepkTimes = times;
    }
    
    public int getForcePkTimes(){
    	return this.getforcepkTimes;
    }
    
    //自动爬塔次数
    public void resetBossRushAutoTimes(){
    	getOtherPool().setInt(BOSSRUSH_AUTO_TIMES, bossRushAutoTimes);
    }
    
    public void setBossRushAutoTimes(int times){
    	this.bossRushAutoTimes = times;
    }
    
    public int getBossRushAutoTimes(){
    	return this.bossRushAutoTimes;
    }
    
    //vip4自动爬塔次数
    public void resetBossRushAutoTimesVip4(){
    	getOtherPool().setInt(BOSSRUSH_AUTO_TIMES_VIP4, bossRushAutoTimesvip4);
    }
    
    public void setBossRushAutoTimesVip4(int times){
    	this.bossRushAutoTimesvip4 = times;
    }
    
    public int getBossRushAutoTimesVip4(){
    	return this.bossRushAutoTimesvip4;
    }
    
    //vip5自动爬塔次数
    public void resetBossRushAutoTimesVip5(){
    	getOtherPool().setInt(BOSSRUSH_AUTO_TIMES_VIP5, bossRushAutoTimesvip5);
    }
    
    public void setBossRushAutoTimesVip5(int times){
    	this.bossRushAutoTimesvip5 = times;
    }
    
    public int getBossRushAutoTimesVip5(){
    	return this.bossRushAutoTimesvip5;
    }
    
    //领取vip3形象
    public void resetVip3FaceTimes(){
    	getOtherPool().setInt(GET_VIP3_FACE, getvip3facetimes);
    }
    
    public void setVip3FaceTimes(int times){
    	this.getvip3facetimes = times;
    }
    
    public int getVip3FaceTimes(){
    	return this.getvip3facetimes;
    }
    
    //领取vip4形象
    public void resetVip4FaceTimes(){
    	getOtherPool().setInt(GET_VIP4_FACE, getvip4facetimes);
    }
    
    public void setVip4FaceTimes(int times){
    	this.getvip4facetimes = times;
    }
    
    public int getVip4FaceTimes(){
    	return this.getvip4facetimes;
    }
    
    //领取vip5形象
    public void resetVip5FaceTimes(){
    	getOtherPool().setInt(GET_VIP5_FACE, getvip5facetimes);
    }
    
    public void setVip5FaceTimes(int times){
    	this.getvip5facetimes = times;
    }
    
    public int getVip5FaceTimes(){
    	return this.getvip5facetimes;
    }
    
    //封印法阵，各阵眼当前经验
    public void resetWaterExp(){
    	getOtherPool().setInt(WATER_MAGIC_POSITION_EXP, waterExp);
    }
    
    public void setWaterExp(int exp){
    	this.waterExp = exp;
    }
    
    public int getWaterExp(){
    	return this.waterExp;
    }
    
    public void resetSoilExp(){
    	getOtherPool().setInt(SOIL_MAGIC_POSITION_EXP, soilExp);
    }
    
    public void setSoilExp(int exp){
    	this.soilExp = exp;
    }
    
    public int getSoilExp(){
    	return this.soilExp;
    }
    
    public void resetFireExp(){
    	getOtherPool().setInt(FIRE_MAGIC_POSITION_EXP, fireExp);
    }
    
    public void setFireExp(int exp){
    	this.fireExp = exp;
    }
    
    public int getFireExp(){
    	return this.fireExp;
    }
    
    public void resetWindExp(){
    	getOtherPool().setInt(WIND_MAGIC_POSITION_EXP, windExp);
    }
    
    public void setWindExp(int exp){
    	this.windExp = exp;
    }
    
    public int getWindExp(){
    	return this.windExp;
    }
    
    public void resetMindExp(){
    	getOtherPool().setInt(MIND_MAGIC_POSITION_EXP, mindExp);
    }
    
    public void setMindExp(int exp){
    	this.mindExp = exp;
    }
    
    public int getMindExp(){
    	return this.mindExp;
    }
    
    //封印法阵，各阵眼等级
    public void resetWaterLevel(){
    	getOtherPool().setInt(WATER_MAGIC_POSITION_LEVEL, waterlevel);
    }
    
    public void setWaterLevel(int level){
    	this.waterlevel = level;
    }
    
    public int getWaterLevel(){
    	return this.waterlevel;
    }
    
    public void resetSoilLevel(){
    	getOtherPool().setInt(SOIL_MAGIC_POSITION_LEVEL, soillevel);
    }
    
    public void setSoilLevel(int level){
    	this.soillevel = level;
    }
    
    public int getSoilLevel(){
    	return this.soillevel;
    }
    
    public void resetFireLevel(){
    	getOtherPool().setInt(FIRE_MAGIC_POSITION_LEVEL, firelevel);
    }
    
    public void setFireLevel(int level){
    	this.firelevel = level;
    }
    
    public int getFireLevel(){
    	return this.firelevel;
    }
    
    public void resetWindLevel(){
    	getOtherPool().setInt(WIND_MAGIC_POSITION_LEVEL, windlevel);
    }
    
    public void setWindLevel(int level){
    	this.windlevel = level;
    }
    
    public int getWindLevel(){
    	return this.windlevel;
    }
    
    public void resetMindLevel(){
    	getOtherPool().setInt(MIND_MAGIC_POSITION_LEVEL, mindlevel);
    }
    
    public void setMindLevel(int level){
    	this.mindlevel = level;
    }
    
    public int getMindLevel(){
    	return this.mindlevel;
    }
    
    //封印法阵，各阵眼等级中阶层
    public void resetWaterFloor(){
    	getOtherPool().setInt(WATER_CURRENT_FLOOR, waterfloor);
    }
    
    public void setWaterFloor(int floor){
    	this.waterfloor = floor;
    }
    
    public int getWaterFloor(){
    	return this.waterfloor;
    }
    
    public void resetSoilFloor(){
    	getOtherPool().setInt(SOIL_CURRENT_FLOOR, soilfloor);
    }
    
    public void setSoilFloor(int floor){
    	this.soilfloor = floor;
    }
    
    public int getSoilFloor(){
    	return this.soilfloor;
    }
    
    public void resetFireFloor(){
    	getOtherPool().setInt(FIRE_CURRENT_FLOOR, firefloor);
    }
    
    public void setFireFloor(int floor){
    	this.firefloor = floor;
    }
    
    public int getFireFloor(){
    	return this.firefloor;
    }
    
    public void resetWindFloor(){
    	getOtherPool().setInt(WIND_CURRENT_FLOOR, windfloor);
    }
    
    public void setWindFloor(int floor){
    	this.windfloor = floor;
    }
    
    public int getWindFloor(){
    	return this.windfloor;
    }
    
    public void resetMindFloor(){
    	getOtherPool().setInt(MIND_CURRENT_FLOOR, mindfloor);
    }
    
    public void resetAwardBoxItemId(){
		getOtherPool().setInt(AWARD_BOX_ITEM_ID, awardBoxItemId);
    }
    
    public void resetPetDevelopTime(){
    	getOtherPool().setString(PetDevelopTime, format.format(petDevelopTime));
    }
    
    public void resetPetEvolutionTime(){
    	getOtherPool().setString(PetEvolutionTime, format.format(petEvolutionTime));
    }
    
    public void resetPetDevelopValue(){
    	StringBuilder sb = new StringBuilder();
    	for(int i=0; i<petDevelopValue.length; i++){
    		sb.append(petDevelopValue[i]);
    		if(i != petDevelopValue.length - 1){
    			sb.append(",");
    		}
    	}
    	getOtherPool().setString(PetDevelopAddValue, sb.toString());
    }
    
    public int getAwardBoxItemId() {
		return awardBoxItemId;
	}

	public void setAwardBoxItemId(int awardBoxItemId) {
		this.awardBoxItemId = awardBoxItemId;
	}
	
	public void resetAwardBoxItemIdArrayAfterGivePrize() {
		this.awardBoxItemIdArray = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	}
    
    public void setMindFloor(int floor){
    	this.mindfloor = floor;
    }
    
    public int getMindFloor(){
    	return this.mindfloor;
    }
    //封印法阵相关end
    
    
    //兑换经验次数
    public void resetReplaceTimes(){
    	getOtherPool().setInt(REPLACE_TIMES, replacetimes);
    }
    
    public void setReplaceTimes(int count){
    	this.replacetimes = count;
    }
    
    public int getReplaceTimes(){
    	return this.replacetimes;
    }
    
    public void resetReplaceclock(){
    	getOtherPool().setString(REPLACE_CLOCK, format.format(Replaceclock));
    }
    
    public Date getReplaceclock(){
    	return Replaceclock;
    }
    
    public void setReplaceclock(Date time){
    	this.Replaceclock = time;
    }
    
    public void resetUseTrainpointClock(){
    	getOtherPool().setString(WEEK_USE_TRAINPOINT_CLOCK, format.format(UseTrainpointDate));
    }
    
    public Date getUseTrainpointClock(){
    	return UseTrainpointDate;
    }
    
    public void setUseTrainpointClock(Date time){
    	this.UseTrainpointDate = time;
    }
    
    public void resetAutoStageClock(){
    	getOtherPool().setString(AUTO_PASS_STAGE_CLOCK, format.format(AutoStageDate));
    }
    
    public Date getAutoStageClock(){
    	return AutoStageDate;
    }
    
    public void setAutoStageClock(Date time){
    	this.AutoStageDate = time;
    }
    
    public void resetForcePkClock(){
    	getOtherPool().setString(FORCE_PK_CLOCK, format.format(forcePkDate));
    }
    
    public Date getForcePkClock(){
    	return forcePkDate;
    }
    
    public void setForcePkClock(Date time){
    	this.forcePkDate = time;
    }
    
    
    public void resetVip5MessageUpClock(){
    	getOtherPool().setString(VIP5_MESSAGE_UP, format.format(vip5messageUpDate));
    }
    
    public Date getVip5MessageUpClock(){
    	return vip5messageUpDate;
    }
    
    public void setVip5MessageUpClock(Date time){
    	this.vip5messageUpDate = time;
    }
    
    public void resetVip5MessageDownClock(){
    	getOtherPool().setString(VIP5_MESSAGE_DOWN, format.format(vip5messageDownDate));
    }
    
    public Date getVip5MessageDownClock(){
    	return vip5messageDownDate;
    }
    
    public void setVip5MessageDownClock(Date time){
    	this.vip5messageDownDate = time;
    }
    
    public void resetGetSeedclock(){
    	getOtherPool().setString(GET_SEED_TIME, format.format(getSeedDate));
    }
    
    public Date getSeedclock(){
    	return getSeedDate;
    }
    
    public void setSeedclock(Date time){
    	this.getSeedDate = time;
    }
    
    public void setPetDevelopTime(Date time){
    	this.petDevelopTime = time;
    }
    
    public Date getPetDevelopTime(){
    	return petDevelopTime;
    }
    
    public void setPetEvolutionTime(Date time){
    	this.petEvolutionTime = time;
    }
    
    public Date getPetEvolutionTime(){
    	return petEvolutionTime;
    }
    
    public void setPetEvolutionCount(int count){
    	this.petEvolutionCount = count;
    }
    
    public int getPetEvolutionCount(){
    	return petEvolutionCount;
    }
    
    public void resetPetEvolutionCount(){
    	getOtherPool().setInt(PetEvolutionCount, petEvolutionCount);
    }
    
    public void setPetDevelopValue(int[] value){
    	this.petDevelopValue = value;
    }
    
    public int[] getPetDevelopValue(){
    	return petDevelopValue;
    }
    
    public void resetUseTrainPoint(){
    	getOtherPool().setInt(USE_TRAINPOINT, playerUseTrainpoint);
    }
    
    public void setUseTrainPoint(int point){
    	this.playerUseTrainpoint = point;
    }
    
    public int getUseTrainPoint(){
    	return playerUseTrainpoint;
    }
    
    
    public int getBossRushStage(){
    	return bossRushStage;
    }
    
    public void setBossRushStage(int bossRushStage){
    	this.bossRushStage = bossRushStage;
    	if(bossRushStage > bossRushStage_best){
    		setBossRushStageBest(bossRushStage); 
    	}
    }
    
    public void resetBossRushStage(){
    	getOtherPool().setInt(BOSSRUSH_STAGE, bossRushStage);
    }
    
    public int getBossRushStageBest(){
    	return bossRushStage_best;
    }
    
    public void setBossRushStageBest(int bossRushStageBest){
    	this.bossRushStage_best = bossRushStageBest;
    }
    
    public void resetBossRushStageBest(){
    	getOtherPool().setInt(BOSSRUSH_STAGE_BEST, bossRushStage_best);
    }
    
    public List getBossRushBoutList(){
    	return bossRushBoutList;
    }
    
    public int getBossRushBoutListSize(){
    	if(bossRushBoutList!=null){
    		return bossRushBoutList.size();
    	}
    	return 0;
    }
    public void setBossRushBoutList(String str){
    	bossRushBoutList.clear();
    	if(str==null || str.length()==0){
    		bossRushBoutList.add(0);
    	}else{
	    	String[] tmp = str.split(":");
			for(int i=0;i<tmp.length;i++){
				addBossRushBout(Integer.parseInt(tmp[i]));
			}
    	}
    }
    
    public void resetBossRushBoutCount(){
    	if(bossRushBoutList.size()>0){
	    	StringBuilder sb = new StringBuilder();
	    	for(int i = 0;i<bossRushBoutList.size();i++ ){
	    		sb.append(bossRushBoutList.get(i).toString() + ":");
	    	}
    		sb.deleteCharAt(sb.length()-1);
    		getOtherPool().setString(BOSSRUSH_BOUTCOUNT, sb.toString());
    	}else{
    		getOtherPool().setString(BOSSRUSH_BOUTCOUNT, "0");
    	}
    }
    
    public void addBossRushBout(int bout){
    	bossRushBoutList.add(bout);
    }
    
    public void addBossRushBout(int bout, int index){
    	if(index == bossRushBoutList.size()){
    		bossRushBoutList.add(bout);
    	}else if(index < bossRushBoutList.size()){
    		setBossRushBout(bout,index);
    	}
    }
    
    public void setBossRushBout(int bout, int index){
    	if(index >= 0 && index < bossRushBoutList.size()){
    		int tmpValue = bossRushBoutList.get(index).intValue();
    		if(bout < tmpValue || tmpValue <=0){
    			bossRushBoutList.set(index, bout);
    		}
    	}
    }
    
    public int getBossRushBout(int index){
    	if(index > 0 && index < bossRushBoutList.size()){
    		return bossRushBoutList.get(index).intValue();
    	}
    	return 0;
    }
    
    public int getBossRushTotalBout(){
    	int value = 0;
    	int size = bossRushBoutList.size();
    	for(int i=0;i<size;i++){
    		value += bossRushBoutList.get(i).intValue();
    	}
    	return value;
    }
    
    public void setBossRushTime(int bossRushTime){
    	this.bossRushTime = bossRushTime;
    }
    
    public int getBossRushTime(){
    	return bossRushTime;
    }
    
    public void resetBossRushTime(){
    	getOtherPool().setInt(BOSSRUSH_TIME,bossRushTime);
    }
    
    public void setBossRushLastTime(Date bossRushLastTime){
    	this.bossRushLastTime = bossRushLastTime;
    }
    
    public Date getBossRushLastTime(){
    	return bossRushLastTime;
    }
    public void resetBossRushLastTime () {
    	getOtherPool().setString(BOSSRUSH_LASTTIME, format.format(bossRushLastTime));
    }
    
//    //兔子赛跑押注信息设置
//	public int getJettonNumFir() {
//		return jettonNumFir;
//	}
//
//	public void setJettonNumFir(int jettonNumFir) {
//		this.jettonNumFir = jettonNumFir;
//	}
//	
//	public void resetJettonNumFir() {
//		getOtherPool().setInt(RABBIT_RACE_JETTON_FIR, jettonNumFir);
//	}
//	public void resetJettonNumSec() {
//		getOtherPool().setInt(RABBIT_RACE_JETTON_SEC, jettonNumSec);
//	}
//	public void resetJettonNumThi() {
//		getOtherPool().setInt(RABBIT_RACE_JETTON_THI, jettonNumThi);
//	}
//	public void resetJettonNumFou() {
//		getOtherPool().setInt(RABBIT_RACE_JETTON_FOU, jettonNumFou);
//	}
//	public void resetJettonNumFif() {
//		getOtherPool().setInt(RABBIT_RACE_JETTON_FIF, jettonNumFif);
//	}
//
//	public void resetJettonNums(){
//		getOtherPool().setInt(RABBIT_RACE_JETTON_FIR, 0);
//		getOtherPool().setInt(RABBIT_RACE_JETTON_SEC, 0);
//		getOtherPool().setInt(RABBIT_RACE_JETTON_THI, 0);
//		getOtherPool().setInt(RABBIT_RACE_JETTON_FOU, 0);
//		getOtherPool().setInt(RABBIT_RACE_JETTON_FIF, 0);
//	}
	
//	public int getJettonNumSec() {
//		return jettonNumSec;
//	}
//
//	public void setJettonNumSec(int jettonNumSec) {
//		this.jettonNumSec = jettonNumSec;
//	}
//
//	public int getJettonNumThi() {
//		return jettonNumThi;
//	}
//
//	public void setJettonNumThi(int jettonNumThi) {
//		this.jettonNumThi = jettonNumThi;
//	}
//
//	public int getJettonNumFou() {
//		return jettonNumFou;
//	}
//
//	public void setJettonNumFou(int jettonNumFou) {
//		this.jettonNumFou = jettonNumFou;
//	}
//
//	public int getJettonNumFif() {
//		return jettonNumFif;
//	}
//
//	public void setJettonNumFif(int jettonNumFif) {
//		this.jettonNumFif = jettonNumFif;
//	}
    
    /**
     * 检测上次打多层BOSS的挑战的时间是否超过1天
     * @return
     */
    public boolean checkBossRushLastTime(){
    	Date dateLast = getBossRushLastTime();
    	int lastDay = -1;
    	if(dateLast!= null){
    		Calendar calLast = Calendar.getInstance();
    		calLast.setTime(dateLast);
    		lastDay = calLast.get(Calendar.DAY_OF_MONTH);
    	}
    	Calendar calNow = Calendar.getInstance();
    	calNow.setTimeInMillis(System.currentTimeMillis());
    	int day = calNow.get(Calendar.DAY_OF_MONTH);
    	if(day!=lastDay){
    		return true;
    	}
    	return false;
    }
    
    public void checkBossRushStageBest(){
    	int size = getBossRushBoutListSize();
    	if(bossRushStage_best > size){
    		this.setBossRushStageBest(size);
    	}
    }
    
    public int[] getAddAttributes(){
    	return addAttributes;
    }

    
    public void setAddAttributes(int value,int index,boolean overwrite,Changed changed){
    	if(index >= 0 && index < addAttributes.length){
    		int ret = 0;
    		if(overwrite){
    			ret = value - addAttributes[index];
    			addAttributes[index] = value;
    		}else{
    			addAttributes[index]+=value;
    			ret = value;
    		}
    		if(changed!=null){
    			byte pro = (byte)(Changed.ADDATTR_STRENGTH + index);
    			changed.setProperty(pro, ret);
    		}
    	}
    }
    
    public void initAddAttributes(String str){
    	addAttributes = new int[4];
    	if(str!=null && str.length() > 0){
    	String tmp[] = str.split(":");
	    	for(int i =0;i<tmp.length;i++){
	    		addAttributes[i]=Integer.parseInt(tmp[i]);
	    	}
    	}
    }
    
    public void resetAddAttributes(){
    	StringBuilder sb = new StringBuilder();
    	for(int i = 0;i < addAttributes.length;i++){
    		sb.append(addAttributes[i] + ":");
    	}
    	if(sb.length() > 0){
    		sb.deleteCharAt(sb.length()-1);
    	}else{
    		sb.append("0:0:0:0");
    	}
    	getOtherPool().setString(ADDATTRIBUTE_VALUE,sb.toString());
    }
    
    public void initAwardBoxItemIdArray(String str){
    	awardBoxItemIdArray = new int[10];
    	if(str!=null && str.length() > 0){
    		String tmp[] = str.split(":");
    		for(int i =0;i<tmp.length;i++){
    			awardBoxItemIdArray[i]=Integer.parseInt(tmp[i]);
    		}
    	}
    }
    
    public void resetAwardBoxItemIdArray(){
    	StringBuilder sb = new StringBuilder();
    	for(int i = 0;i < awardBoxItemIdArray.length;i++){
    		sb.append(awardBoxItemIdArray[i] + ":");
    	}
    	if(sb.length() > 0){
    		sb.deleteCharAt(sb.length()-1);
    	}else{
    		sb.append("0:0:0:0:0:0:0:0:0:0");
    	}
    	getOtherPool().setString(AWARD_BOX_ITEMID_ARRAY,sb.toString());
    }
    
	public int[] getAwardBoxItemIdArray() {
		return awardBoxItemIdArray;
	}

	public void setAwardBoxItemIdArray(int[] awardBoxItemIdArray) {
		this.awardBoxItemIdArray = awardBoxItemIdArray;
	}
    
    public void resetChristmasWishingNormal_Count(){
    	getOtherPool().setInt(CHRISTMAS_WISHING_NORMAL_COUNT,christmasWishing_normal_count);
    }
    
    public void resetDragonBoatFestivalReplaceCount(){
    	getOtherPool().setInt(DRAGON_BOAT_FESTIVAL_REPLACE_COUNT,dragonBoatFestivalReplaceCount);
    }
    
    public void setChristmasWishingNormal_Count(int count){
    	christmasWishing_normal_count = count;
    }
    
    public int getChristmasWishingNormal_Count(){
    	return christmasWishing_normal_count;
    }
    
    public boolean addChristmasWishingNormalCount(){
//    	if(christmasWishing_normal_count < CHRISTMAS_WISHING_NORMAL_MAX_COUNT){
//    		christmasWishing_normal_count++;
//    		setChristmasWishingNormal_LastTime(new Date());
//    		return true;
//    	}else{
    		Date dateLast = getChristmasWishingNormal_LastTime();
        	int lastDay = -1;
        	if(dateLast!= null){
        		Calendar calLast = Calendar.getInstance();
        		calLast.setTime(dateLast);
        		lastDay = calLast.get(Calendar.DAY_OF_MONTH);
        	}
        	Calendar calNow = Calendar.getInstance();
        	calNow.setTimeInMillis(System.currentTimeMillis());
        	int day = calNow.get(Calendar.DAY_OF_MONTH);
        	if(day!=lastDay){
        		christmasWishing_normal_count = 1;
        		setChristmasWishingNormal_LastTime(new Date());
        		return true;
        	}else{//同一天内
        		if(christmasWishing_normal_count < CHRISTMAS_WISHING_NORMAL_MAX_COUNT){
        			christmasWishing_normal_count++;
        			return true;
        		}
        	}
    	//}
    	return false;
    }
    
    public boolean addDragonBoatFestivalReplaceCount(){
    	Date dateLast = getDragonBoatFestivalReplaceLastTime();
    	int lastDay = -1;
    	if(dateLast!= null){
    		Calendar calLast = Calendar.getInstance();
    		calLast.setTime(dateLast);
    		lastDay = calLast.get(Calendar.DAY_OF_MONTH);
    	}
    	Calendar calNow = Calendar.getInstance();
    	calNow.setTimeInMillis(System.currentTimeMillis());
    	int day = calNow.get(Calendar.DAY_OF_MONTH);
    	if(day!=lastDay){
    		dragonBoatFestivalReplaceCount = 1;
    		setDragonBoastFestivalReplaceLastTime(new Date());
    		return true;
    	}else{//同一天内
    		if(dragonBoatFestivalReplaceCount < DRAGON_BOAT_FESTIVAL_REPLACE_MAX_COUNT){
    			dragonBoatFestivalReplaceCount++;
    			return true;
    		}
    	}
    	//}
    	return false;
    }
    
    public int checkChristmasWishingNormalCount(){
    	return CHRISTMAS_WISHING_NORMAL_MAX_COUNT - christmasWishing_normal_count;
    }
    
    public int checkDragonBoatFestivalReplaceCount(){
    	return DRAGON_BOAT_FESTIVAL_REPLACE_MAX_COUNT - dragonBoatFestivalReplaceCount;
    }
    
    public void resetChristmasWishingNormal_LastTime(){
    	getOtherPool().setString(CHRISTMAS_WISHING_NORMAL_LASTTIME, format.format(christmasWishing_normal_lastTime));
    }
    
    public void resetDragonBoatFestivalReplaceLastTime(){
    	getOtherPool().setString(DRAGON_BOAT_FESTIVAL_REPLACE_LASTTIME, format.format(dragonBoatFestivalReplaceLastTime));
    }

    
    public Date getChristmasWishingNormal_LastTime(){
    	return christmasWishing_normal_lastTime;
    }
    
    public void setChristmasWishingNormal_LastTime(Date lastTime){
    	christmasWishing_normal_lastTime = lastTime;
    }
    
    public int getDragonBoatFestivalReplaceCount() {
		return dragonBoatFestivalReplaceCount;
	}

	public void setDragonBoatFestivalReplaceCount(int dragonBoatFestivalReplaceCount) {
		this.dragonBoatFestivalReplaceCount = dragonBoatFestivalReplaceCount;
	}

	public Date getDragonBoatFestivalReplaceLastTime() {
		return dragonBoatFestivalReplaceLastTime;
	}

	public void setDragonBoastFestivalReplaceLastTime(
			Date dragonBoastFestivalReplaceLastTime) {
		this.dragonBoatFestivalReplaceLastTime = dragonBoastFestivalReplaceLastTime;
	}
    
    public Date getDownloadPoint_LastTime(){
    	return downloadPoint_lasttime;
    }
    
    public void setDownloadPoint_LastTime(Date lastTime){
    	downloadPoint_lasttime = lastTime;
    }
    
    public void resetDownloadPoint_lastTime(){
    	getOtherPool().setString(DOWNLOADPOINT_LASTTIME, format.format(downloadPoint_lasttime));
    }
    
    public int getDownloadPoint_useValue(){
    	return downloadPoint_usevalue;
    }
    
    public void setDownloadPoint_useValue(int usevalue){
    	this.downloadPoint_usevalue = usevalue;
    }
    
    public void resetDownloadPoint_useTime(){
    	getOtherPool().setInt(DOWNLOADPOINT_USEVALUE, downloadPoint_usevalue);
    }
    
    public int getDownloadPoint(){
    	return downloadPoint;
    }
    
    public void setDownloadPoint(int point){
    	if(point < 0){
    		log.error("ID[" + this.getId() + "] setDownloadPoint error  newPoint[" + point + "] curPoint[" + downloadPoint +"] ");
    	}else{
    		downloadPoint = point;
    	}
    }
    
    public void addDownloadPoint(int point, Changed changed){
    	if(point <= 0){
    		return;
    	}
    	setDownloadPoint(getDownloadPoint() + point);
    	if(changed!=null){
    		changed.addProperty(Changed.DOWNLOAD_POINT, point);
    	}
    }
    
    public void decDownloadPoint(int point, Changed changed){
    	if(point <= 0){
    		return;
    	}
    	setDownloadPoint(getDownloadPoint() - point);
    	if(changed!=null){
    		changed.addProperty(Changed.DOWNLOAD_POINT, -point);
    	}
    }
    
    public void resetDownloadPoint(){
    	getOtherPool().setInt(DOWNLOAD_POINTS, downloadPoint);
    }
    
    private void initPlayerPool () throws Exception {
    	playerPool.parse(player.getPlayerPool());
    	
    	// 设置VIP等级
    	setVipLevel(playerPool.getInt(VIP_LEVEL));
    	// 设置最后一次领去VIP奖品的时间
        String getTimeStr = playerPool.getString(LAST_GET_VIP_GIFT_TIME);
        if (getTimeStr == null || getTimeStr.equals("")) {
        	setLastGetVipGiftTime(new Date(Utils.getTodayStart() - Utils.MILLS_OF_DAY));
    	} else {
    		setLastGetVipGiftTime(format.parse(getTimeStr));
    	}
        // 设置VIP生效的时间
        String vipValidTimeStr = playerPool.getString(VIP_VALID_TIME);
        if (vipValidTimeStr != null && vipValidTimeStr.equals("") == false) {
        	setVipValidTime(format.parse(vipValidTimeStr));
        }
        // 设置资源争夺战捐赠的个数
        setChrItemCount(playerPool.getInt(CHR_ITEM_COUNT));
        // 设置最近完成世界喊话活动的时间
        String getLastWorldTime = playerPool.getString(LAST_WORLD_COMPLETE_TIME);
        if (getLastWorldTime == null || getLastWorldTime.equals("")) {
        	setLastWorldCompleteTime(new Date(0));
        } else {
        	setLastWorldCompleteTime(format.parse(getLastWorldTime));
        }
        // 设置最近完成阵营喊话活动的时间
        String getLastCampTime = playerPool.getString(LAST_CAMP_COMPLETE_TIME);
        if (getLastCampTime == null || getLastCampTime.equals("")) {
        	setLastCampCompleteTime(new Date(0));
        } else {
        	setLastCampCompleteTime(format.parse(getLastCampTime));
        }
        // 设置最近完成点名活动的时间
        String getLastRollcallTime = playerPool.getString(LAST_ROLLCALL_TIME);
        if (getLastRollcallTime == null || getLastRollcallTime.equals("")) {
        	setLastRollcallCompleteTime(new Date(0));
        } else {
        	setLastRollcallCompleteTime(format.parse(getLastRollcallTime));
        }
        // 设置点名活动完成天数
        setRollcallDays(playerPool.getInt(ROLLCALL_DAYS));
        //设置是否获取经验
        setCheckExpBag(playerPool.getInt(CHECK_EXPBAG));
    }

    public Changed addFallResult(FallResult fallResult, int dataVersion) {
        Changed ret = new Changed();
        if (fallResult.getMoney() > 0) {
            setMoeny(getMoeny() + fallResult.getMoney());
            ret.setProperty(Changed.MONEY, fallResult.getMoney());
        }
        if (fallResult.getExp() > 0) {
        	//星辉套装增加经验效果
        	int exp = fallResult.getExp();
        	int[] diamondShineLevel = Suits.getActualPointSuitEffect2(getUsedEquipments());{
        		if(diamondShineLevel[0] >= 2){
        			exp =exp + exp * 4 / 100;
        		} else if(diamondShineLevel[0] == 1){
        			exp =exp + exp * 2 / 100;
        		}
        	}
            addExp(exp, ret);
        }
        TemplateGrid[] items = fallResult.getItems();
        addItems(items, ret, dataVersion);
        return ret;
    }

//    public boolean addItems(Changed ret,Grid[] items){
//        boolean added = false;
//        for (int i = 0; i < items.length; i++) {
//            if(addItem(items[i].item, items[i].count, ret)!=0){
//                added = true;
//            }
//        }
//        return added;
//    }

    public boolean addItems(TemplateGrid[] grids, Changed changed, int dataVersion) {
        boolean added = false;
        for (int i = 0; i < grids.length; i++) {
        	if(grids[i].count < 0){	//count = -1,兑换全部
        		added = false;
        	}else if(addItem(grids[i].template, grids[i].count, changed, dataVersion) != 0) {
                added = true;
            }
        }
        return added;
    }

/*    public int addItem(IItem item, int count, Changed changed) {
        byte type = item.getType();
        if (type == IItem.TYPE_BASIC) {
            for (int i = 0; i < basicItems.size(); i++) {
                Grid grid = (Grid) basicItems.get(i);
                if (grid.item.getItemId() == item.getItemId()) {
                    if (grid.count >= 99) {
                        if (changed != null)
                            changed.setProperty(Changed.GRIDFULL, 1);
                        return 0;
                    }
                    int oldCount = grid.count;
                    int total = grid.count + count;
                    if (total > 99) {
                        int ret = 99 - grid.count;
                        grid.count = 99;
                        if (changed != null) {
                            changed.addItem(grid.item, grid.count - oldCount);
                        }
                        changed.setProperty(Changed.GRIDFULL, 1);
                        return ret;
                    } else {
                        grid.count = (short) total;
                        if (changed != null) {
                            changed.addItem(grid.item, grid.count - oldCount);
                        }
                        return count;
                    }
                }
            }
            if (isFull()) {
                if (changed != null)
                    changed.setProperty(Changed.GRIDFULL, 1);
                return 0;
            }
            Grid grid = new Grid();
            grid.item = item;
            grid.count = (short) count;
            basicItems.add(grid);
            if (grid.item.getBindType() == IItem.BIND_GET) {
                grid.item.setBinded(true);
            }

            if (changed != null) {
                changed.addItem(grid.item, count);
            }
            return count;
        } else if (type == IItem.TYPE_TASK) {
            for (int i = 0; i < taskItems.size(); i++) {
                Grid grid = (Grid) taskItems.get(i);
                if (grid.item.getItemId() == item.getItemId()) {
                    if (grid.count >= ((TaskItem) item).getMax())
                        return 0;
                    int total = grid.count + count;
                    int oldCount = grid.count;
                    if (total > ((TaskItem) item).getMax()) {
                        int ret = ((TaskItem) item).getMax() - grid.count;
                        grid.count = ((TaskItem) item).getMax();
                        if (changed != null) {
                            changed.addItem(grid.item, grid.count - oldCount);
                        }
                        return ret;
                    } else {
                        grid.count = (short) total;
                        if (changed != null) {
                            changed.addItem(grid.item, grid.count - oldCount);
                        }
                        return count;
                    }
                }
            }
            if (isFull()) {
                if (changed != null)
                    changed.setProperty(Changed.GRIDFULL, 1);
                return 0;
            }
            Grid grid = new Grid();
            grid.item = item;
            short nCount = (short) count;
            if (nCount > ((TaskItem) item).getMax()) {
                nCount = (short)((TaskItemTemplate) item).getMax();
            }
            grid.count = nCount;
            taskItems.add(grid);
            if (changed != null) {
                changed.addItem(grid.item, count);
            }
            return nCount;
        } else if (type == IItem.TYPE_EXTENDED) {
            for (int i = 0; i < extendedItems.size(); i++) {
                Grid grid = (Grid) extendedItems.get(i);
                if (grid.item.getItemId() == item.getItemId()) {
                    if (grid.count >= 99) {
                        if (changed != null)
                            changed.setProperty(Changed.GRIDFULL, 1);
                        return 0;
                    }
                    int oldCount = grid.count;
                    int total = grid.count + count;
                    if (total > 99) {
                        int ret = 99 - grid.count;
                        grid.count = 99;
                        if (changed != null) {
                            changed.addItem(grid.item, grid.count - oldCount);
                        }
                        changed.setProperty(Changed.GRIDFULL, 1);
                        return ret;
                    } else {
                        grid.count = (short) total;
                        if (changed != null) {
                            changed.addItem(grid.item, grid.count - oldCount);
                        }
                        return count;
                    }
                }
            }
            if (isFull()) {
                if (changed != null)
                    changed.setProperty(Changed.GRIDFULL, 1);
                return 0;
            }
            Grid grid = new Grid();
            grid.item = item;
            grid.count = (short) count;
            extendedItems.add(grid);
            if (grid.item.getBindType() == IItem.BIND_GET) {
                grid.item.setBinded(true);
            }
            if (changed != null) {
                changed.addItem(grid.item, count);
            }
            return count;
        } else if (type == IItem.TYPE_EQU) {
            if (isFull()) {
                if (changed != null)
                    changed.setProperty(Changed.GRIDFULL, 1);
                return 0;
            }
            Grid grid = new Grid();
            grid.item = item;
            grid.count = 1;
            equipments.add(grid);
            if (grid.item.getBindType() == IItem.BIND_GET) {
                grid.item.setBinded(true);
            }
            if (changed != null) {
                changed.addItem(grid.item, 1);
            }
            return 1;
        }
        return 0;
    }*/

    public void descItems(Grid[] grids) {
        for (int i = 0; i < grids.length; i++) {
            descItem(grids[i].item, grids[i].count);
        }
    }

    public boolean descItem(IItem item, int count) {
        byte type = item.getType();
        List items = null;
        if (type == IItem.TYPE_BASIC) {
            items = basicItems;
        } else if (type == IItem.TYPE_EXTENDED) {
            items = extendedItems;
        } else if (type == IItem.TYPE_TASK) {
            items = taskItems;
        }
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                Grid grid = (Grid) items.get(i);
                if (grid.item.getItemId() == item.getItemId()) {
                    int newCount = grid.count - count;
                    if (newCount > 0) {
                        grid.count = (short) newCount;
                        return true;
                    } else if (newCount == 0) {
                        items.remove(i);
                        return true;
                    } else
                        return false;
                }
            }
        }
        return false;
    }


    public IEquipment deleteEquipment(int itemId, int id) {
        for (int i = 0; i < equipments.size(); i++) {
            Grid grid = (Grid) equipments.get(i);
            IEquipment equ = (IEquipment) grid.item;
            if (equ.getItemId() == itemId && equ.getId() == id) {
                equipments.remove(i);
                return equ;
            }
        }
        return null;
    }


    public int addItem(IItemTemplate template, int count, Changed changed,int dataVersion) {
        byte type = template.getType();
        if (type == IItem.TYPE_BASIC) {
            for (int i = 0; i < basicItems.size(); i++) {
                Grid grid = (Grid) basicItems.get(i);
                if (grid.item.getItemId() == template.getItemId()) {
                    if (grid.count >= 99) {
                        if (changed != null)
                            changed.setProperty(Changed.GRIDFULL, 1);
                        return 0;
                    }
                    int oldCount = grid.count;
                    int total = grid.count + count;
                    if (total > 99) {
                        int ret = 99 - grid.count;
                        grid.count = 99;
                        if (changed != null) {
                            changed.addItem(grid.item, grid.count - oldCount);
                        }
                        changed.setProperty(Changed.GRIDFULL, 1);
                        return ret;
                    } else {
                        grid.count = (short) total;
                        if (changed != null) {
                            changed.addItem(grid.item, grid.count - oldCount);
                        }
                        return count;
                    }
                }
            }
            if (isFull()) {
                if (changed != null)
                    changed.setProperty(Changed.GRIDFULL, 1);
                return 0;
            }
            Grid grid = new Grid();
            grid.item = template.newInstance();
            grid.count = (short) count;
            basicItems.add(grid);
            if (grid.item.getBindType() == IItem.BIND_GET) {
                grid.item.setBinded(true);
            }

            if (changed != null) {
                changed.addItem(grid.item, count);
            }
            return count;
        } else if (type == IItem.TYPE_TASK) {
            for (int i = 0; i < taskItems.size(); i++) {
                Grid grid = (Grid) taskItems.get(i);
                if (grid.item.getItemId() == template.getItemId()) {
                    if (grid.count >= ((TaskItemTemplate) template).getMax())
                        return 0;
                    int total = grid.count + count;
                    int oldCount = grid.count;
                    if (total > ((TaskItemTemplate) template).getMax()) {
                        int ret = ((TaskItemTemplate) template).getMax() -
                                  grid.count;
                        grid.count = ((TaskItemTemplate) template).getMax();
                        if (changed != null) {
                            changed.addItem(grid.item, grid.count - oldCount);
                        }
                        return ret;
                    } else {
                        grid.count = (short) total;
                        if (changed != null) {
                            changed.addItem(grid.item, grid.count - oldCount);
                        }
                        return count;
                    }
                }
            }
            if (isFull()) {
                if (changed != null)
                    changed.setProperty(Changed.GRIDFULL, 1);
                return 0;
            }
            Grid grid = new Grid();
            grid.item = template.newInstance();
            short nCount = (short) count;
            if (nCount > ((TaskItemTemplate) template).getMax()) {
                nCount = (short)((TaskItemTemplate) template).getMax();
            }
            grid.count = nCount;
            taskItems.add(grid);
            if (changed != null) {
                changed.addItem(grid.item, nCount);
            }
            return nCount;
        } else if (type == IItem.TYPE_EXTENDED) {
            for (int i = 0; i < extendedItems.size(); i++) {
                Grid grid = (Grid) extendedItems.get(i);
                if (grid.item.getItemId() == template.getItemId()) {
                    if (grid.count >= 99) {
                        if (changed != null)
                            changed.setProperty(Changed.GRIDFULL, 1);
                        return 0;
                    }
                    int oldCount = grid.count;
                    int total = grid.count + count;
                    if (total > 99) {
                        int ret = 99 - grid.count;
                        grid.count = 99;
                        if (changed != null) {
                            changed.addItem(grid.item, grid.count - oldCount);
                        }
                        if(changed!=null)
                            changed.setProperty(Changed.GRIDFULL, 1);
                        return ret;
                    } else {
                        grid.count = (short) total;
                        if (changed != null) {
                            changed.addItem(grid.item, grid.count - oldCount);
                        }
                        return count;
                    }
                }
            }
            if (isFull()) {
                if (changed != null)
                    changed.setProperty(Changed.GRIDFULL, 1);
                return 0;
            }
            Grid grid = new Grid();
            grid.item = template.newInstance();
            grid.count = (short) count;
            extendedItems.add(grid);
            if (grid.item.getBindType() == IItem.BIND_GET) {
                grid.item.setBinded(true);
            }

            if (changed != null) {
                changed.addItem(grid.item, count);
            }
            return count;
        } else if (type == IItem.TYPE_EQU) {
            if (isFull()) {
                if (changed != null)
                    changed.setProperty(Changed.GRIDFULL, 1);
                return 0;
            }
            Grid grid = new Grid();
            grid.item = template.newInstance(); 
            if(grid.item instanceof NormalEquipment){
            	((NormalEquipment)grid.item).setDataVersion(dataVersion);
            }else if(grid.item instanceof DynamicEquipment){
            	((DynamicEquipment)grid.item).setDataVersion(dataVersion);
            }
            grid.count = 1;
            equipments.add(grid);
            if (grid.item.getBindType() == IItem.BIND_GET) {
                grid.item.setBinded(true);
            }
            if (changed != null) {
                changed.addItem(grid.item, 1);
            }
            return 1;
        }
        return 0;
    }

    public int addPet(Pet pet, Changed changed) {
        if (isPetFull()) {
            if (changed != null) {
                changed.setProperty(Changed.PETFULL, 1);
            }
            return 0;
        }
        pets.add(pet);
        if (changed != null) {
            changed.addItem(pet, 1);

        }
        return 1;
    }
    
    public void removePet (int id) {
    	for (int i = 0; i < pets.size(); i++) {
            Pet pet = (Pet) pets.get(i);
            if (pet.getId() == id) {
                pets.remove(i);
            }
        }
    }

    public int addItem(int itemId, int count, Changed changed, int dataVersion) {
        IItemTemplate template = Items.getTemplate(itemId);
        return addItem(template, count, changed, dataVersion);
    }


//    public int addItem(IItem item,int count,Changed changed){
//        byte type = item.getType();
//        if(item.getBindType()==IItem.BIND_GET){
//            item.setBinded(true);
//        }
//        if(type==IItem.TYPE_BASIC){
//            for(int i=0;i<basicItems.size();i++){
//                Grid grid = (Grid)basicItems.get(i);
//                if(grid.item.getItemId()==item.getItemId()){
//                    if(grid.count>=99){
//                        changed.setProperty(Changed.GRIDFULL,1);
//                        return 0;
//                    }
//                    int oldCount = grid.count;
//                    int total = grid.count+count;
//                    if(total>99){
//                        int ret = 99-grid.count;
//                        grid.count = 99;
//                        if(changed!=null){
//                            changed.addItem(item,grid.count-oldCount);
//                        }
//                        changed.setProperty(Changed.GRIDFULL,1);
//                        return ret;
//                    }else{
//                        grid.count = (short)total;
//                        if(changed!=null){
//                            changed.addItem(item,grid.count-oldCount);
//                        }
//                        return count;
//                    }
//                }
//            }
//            if(isFull()){
//                changed.setProperty(Changed.GRIDFULL,1);
//                return 0;
//            }
//            Grid grid = new Grid();
//            grid.item = item;
//            grid.count = (short)count;
//            basicItems.add(grid);
//            if(changed!=null){
//                changed.addItem(item,count);
//            }
//            return count;
//        }
//        else if(type==IItem.TYPE_TASK){
//            TaskItem taskItem = (TaskItem)item;
//            for(int i=0;i<taskItems.size();i++){
//                Grid grid = (Grid)taskItems.get(i);
//                if(grid.item.getItemId()==item.getItemId()){
//                    if(grid.count>=taskItem.getMax())
//                        return 0;
//                    int total = grid.count + count;
//                    int oldCount = grid.count;
//                    if(total>taskItem.getMax()){
//                        int ret = taskItem.getMax()-grid.count;
//                        grid.count = taskItem.getMax();
//                        if(changed!=null){
//                            changed.addItem(item,grid.count-oldCount);
//                        }
//                        return ret;
//                    }else{
//                        grid.count = (short)total;
//                        if (changed != null) {
//                            changed.addItem(item, grid.count - oldCount);
//                        }
//                        return count;
//                    }
//                }
//            }
//            if(isFull()){
//                changed.setProperty(Changed.GRIDFULL,1);
//                return 0;
//            }
//            Grid grid = new Grid();
//            grid.item = item;
//            short nCount = (short)count;
//            if(nCount>taskItem.getMax()){
//                nCount = taskItem.getMax();
//            }
//            grid.count = nCount;
//            taskItems.add(grid);
//            if(changed!=null){
//                changed.addItem(item,count);
//            }
//            return nCount;
//        }
//        else if(type==IItem.TYPE_EXTENDED){
//            for(int i=0;i<extendedItems.size();i++){
//                Grid grid = (Grid)extendedItems.get(i);
//                if(grid.item.getItemId()==item.getItemId()){
//                    if(grid.count>=99){
//                        changed.setProperty(Changed.GRIDFULL,1);
//                        return 0;
//                    }
//                    int oldCount = grid.count;
//                    int total = grid.count+count;
//                    if(total>99){
//                        int ret = 99-grid.count;
//                        grid.count = 99;
//                        if(changed!=null){
//                            changed.addItem(item,grid.count-oldCount);
//                        }
//                        changed.setProperty(Changed.GRIDFULL,1);
//                        return ret;
//                    }else{
//                        grid.count = (short)total;
//                        if (changed != null) {
//                            changed.addItem(item, grid.count - oldCount);
//                        }
//                        return count;
//                    }
//                }
//            }
//            if(isFull()){
//                changed.setProperty(Changed.GRIDFULL,1);
//                return 0;
//            }
//            Grid grid = new Grid();
//            grid.item = item;
//            grid.count = (short)count;
//            extendedItems.add(grid);
//            if(changed!=null){
//                changed.addItem(item,count);
//            }
//            return count;
//        }
//        else if(type==IItem.TYPE_EQU){
//            if(isFull()){
//                changed.setProperty(Changed.GRIDFULL,1);
//                return 0;
//            }
//            Equipment equ = (Equipment)item;
//            if(equ.isTemplate())
//                equ = equ.newInstance();
//            Grid grid = new Grid();
//            grid.item = equ;
//            grid.count = 1;
//            equipments.add(grid);
//            if(changed!=null){
//                changed.addItem(grid.item,1);
//            }
//            return 1;
//        }
//        else if(type==IItem.TYPE_PET){
//            if(isPetFull()){
//                changed.setProperty(Changed.PETFULL,1);
//                return 0;
//            }
//            pets.add(item);
//            if(changed!=null){
//                changed.addItem(item,1);
//            }
//        }
//        return 0;
//    }



    public IItem completeAddItem(IItem item, int count, Changed changed, int dataVersion) {
        if (item.getType() == IItem.TYPE_PET) {
            if (isPetFull())
                return null;
            pets.add(item);
            if (changed != null)
                changed.addItem(item, 1);
            return item;
        } else if (item.getType() == IItem.TYPE_EQU) {
            if (isFull())
                return null;
            IEquipment equ = (IEquipment) item;
            equ.setDataVersion(dataVersion);
            Grid grid = new Grid();
            grid.item = equ;
            grid.count = 1;  
            equipments.add(grid);
            if (grid.item.getBindType() == IItem.BIND_GET) {
                grid.item.setBinded(true);
            }
            if (changed != null)
                changed.addEquipment((IEquipment) grid.item);
            return grid.item;
        } else {
        	if (count > 99) {
        		return null;
        	}
            if (item.getType() == IItem.TYPE_BASIC) {
                for (int i = 0; i < basicItems.size(); i++) {
                    Grid grid = (Grid) basicItems.get(i);
                    if (grid.item.getItemId() == item.getItemId()) {
                        if (grid.count >= 99)
                            return null;
                        int total = grid.count + count;
                        if (total <= 99) {
                            grid.count = (short) total;
                            if (changed != null)
                                changed.addItem(item.getItemId(), count);
                            return grid.item;
                        } else {
                            return null;
                        }
                    }
                }
                if (isFull())
                    return null;
                Grid grid = new Grid();
                grid.item = item;
                grid.count = (short) count;
                basicItems.add(grid);
                if (grid.item.getBindType() == IItem.BIND_GET) {
                    grid.item.setBinded(true);
                }
                if (changed != null)
                    changed.addItem(item.getItemId(), count);
                return grid.item;
            } else if (item.getType() == IItem.TYPE_EXTENDED) {
                for (int i = 0; i < extendedItems.size(); i++) {
                    Grid grid = (Grid) extendedItems.get(i);
                    if (grid.item.getItemId() == item.getItemId()) {
                        int total = grid.count + count;
                        if (total <= 99) {
                            grid.count = (short) total;
                            if (changed != null)
                                changed.addItem(item.getItemId(), count);
                            return grid.item;
                        } else {
                            return null;
                        }
                    }
                }
                if (isFull())
                    return null;
                Grid grid = new Grid();
                grid.item = item;
                grid.count = (short) count;
                extendedItems.add(grid);
                if (grid.item.getBindType() == IItem.BIND_GET) {
                    grid.item.setBinded(true);
                }
                if (changed != null)
                    changed.addItem(item.getItemId(), count);
                return grid.item;
            } else if (item.getType() == IItem.TYPE_TASK) {
                TaskItem taskItem = (TaskItem) item;
                for (int i = 0; i < taskItems.size(); i++) {
                    Grid grid = (Grid) taskItems.get(i);
                    if (grid.item.getItemId() == item.getItemId()) {
                        int total = grid.count + count;
                        if (total <= taskItem.getMax()) {
                            grid.count = (short) total;
                            if (changed != null)
                                changed.addItem(item.getItemId(), count);
                            return grid.item;
                        } else {
                            return null;
                        }
                    }
                }
                if (isFull())
                    return null;
                Grid grid = new Grid();
                grid.item = item;
                short nCount = (short) count;
                if (nCount <= taskItem.getMax()) {
                    nCount = (short)taskItem.getMax();
                    grid.count = nCount;
                    taskItems.add(grid);
                    if (changed != null)
                        changed.addItem(item.getItemId(), count);
                    return grid.item;
                }

            }
            return null;
        }
    }

    /**
     * 将玩家的形象添加到橱窗里
     * @param face
     * @param count
     * @param changed
     * @return
     */
    public RoleFaceData completeAddRoleFace(int face, int count, Changed changed, long time) {
    	int ret = isCanBuyFace(face);
    	if (ret == 0) {
    		RoleFaceData roleFaceTmp = RoleFaces.getRoleFace(face);
    		RoleFaceData roleFace = new RoleFaceData(roleFaceTmp.getFace(), roleFaceTmp.getName(), roleFaceTmp.getPrice());
    		if (time > 0) {
    			long expiration = time;
    			if (changed != null) {
    				Date date = new Date ();
    				expiration += date.getTime();
    			}
    			roleFace.setExpiration(expiration);	// 设置过期时间 使用时间 + 持续时间
    		} else {
    			roleFace.setExpiration(time);
    		}
    		log.info("PlayerData CompleteAddRoleFace to playerId [" + player.getId() + "] getFace [" + roleFace.getFace() + "] getExpiration [" + roleFace.getExpiration() + "] ms");
    		if(image.size() >= 1){
    			RoleFaceData selfFace = (RoleFaceData)image.get(0);
    			if(selfFace.getFace() >= 28 && selfFace.getFace() <=31){			//阵营形象		
    				if(image.size() > 2){
    					selfFace = (RoleFaceData)image.get(1);
    					if(selfFace.getFace() >= 0 && selfFace.getFace() <= 3){		//勇士形象
    						if(image.size() >3){
    							selfFace = (RoleFaceData)image.get(2);
    							if(selfFace.getFace() >= 34 && selfFace.getFace() <= 37){
    								image.add(3,roleFace);									//新形象放到第三位
        						}else{
        							image.add(2,roleFace);									//新形象放到第三位
        						}
    						}else{
    							image.add(2,roleFace);									//新形象放到第三位
    						}
    					}else{
    						if(selfFace.getFace() >= 34 && selfFace.getFace() <= 37){
    							image.add(2,roleFace);
    						}else{
    							image.add(1,roleFace);
    						}
    					}
    				}else{
    					image.add(1,roleFace);	
    				}
    			}else{
    				if(roleFace.getFace() >= 28 && roleFace.getFace() <= 31){
    					image.add(0,roleFace);
    				} else {
    					if (selfFace.getFace() >= 0 && selfFace.getFace() <= 3) {					//勇士形象
    						if (image.size() > 2) {
    							selfFace = (RoleFaceData)image.get(1);
    							if(selfFace.getFace() >= 34 && selfFace.getFace() <= 37){		//领袖形象
    								image.add(2,roleFace);
    							}else{
    								image.add(1,roleFace);
    							}
    						}else{
    							image.add(1,roleFace);
    						}
    					} else {
    						if (roleFace.getFace() >= 0 && roleFace.getFace() <= 3) {
    							image.add(0,roleFace);
    						} else {
    							if (selfFace.getFace() >= 34 && selfFace.getFace() <= 37) {
    								image.add(1,roleFace);
    							}else{
    								image.add(0,roleFace);
    							}
    						}
    					}
    				}
    			}
    		}else{
    			image.add(roleFace);
    		}
    		return roleFace;
    	} else if (ret == 2) {
    		for (int i = 0; i < image.size(); i ++) {
    			RoleFaceData selfFace = (RoleFaceData)image.get(i);
    			if (face == selfFace.getFace()) {
    				if (time > 0) {
    	    			long expiration = time;
    	    			if (changed != null) {
    	    				Date date = new Date ();
    	    				expiration += date.getTime();
    	    			}
    	    			selfFace.setExpiration(expiration);	// 设置过期时间 使用时间 + 持续时间
    	    		} else {
    	    			selfFace.setExpiration(time);
    	    		}
    				return selfFace;
    			}
    		}
    		return null;
    	} else {
    		return null;
    	}
    	
    }
    /**
     * 将玩家的称号添加到称号橱窗里
     * @param roleTitle
     * @param changed
     * @return
     */
    public List completeAddRoleTitle(String curRoleTitle) {
    	if("".equals(curRoleTitle)){
    		return null;
    	}
    	if(curRoleTitle.equals(Utils.CAMP_TEAM_BRIGHT) || curRoleTitle.equals(Utils.CAMP_TEAM_DARK)){
    		return null;
    	}
    	int ret = isCanChangeRoleTitle(curRoleTitle);
    	if(ret == 0){	//可以更换
    		if(roleTitle != null){
    			if(roleTitle.size() > 0){
    				roleTitle.add(0, curRoleTitle);		//存时排好序
    			} else {
    				roleTitle.add(curRoleTitle);
    			}
    		}
    		return this.roleTitle;
    	}else{
    		return null;
    	}
    	
    }
    /**
     * 判断能否购买形象物品
     * @param face
     * @return		0:可以购买使用；1:已经购买了； 2:需要延期
     */
    public int isCanBuyFace(int face){
        for (int i = 0; i < image.size(); i++) {
        	RoleFaceData selfFace = (RoleFaceData)image.get(i);
            if (selfFace.getExpiration() == RoleFaceData.UNLIMIT && selfFace.getFace() == face) {							//已经加入到形象橱窗里了
            	return 1;
            } else if (selfFace.getExpiration() != RoleFaceData.UNLIMIT && selfFace.getFace() == face) {
            	return 2;
            }
        }
        return 0;
    }
    
    /**
     * 检查玩家是否有此形象（防止刷包）
     * @param face
     * @return true 有 false 无
     */
    public boolean checkImage (int face) {
    	for (int i = 0; i < image.size(); i ++) {
    		RoleFaceData selfFace = (RoleFaceData)image.get(i);
    		if (selfFace.getFace() == face) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * 获得玩家形象橱窗中的形象(不同于RoleFaces.getRoleFace(faceId))
     * @param face
     * @return selfFace 有 null 无
     */
    public RoleFaceData getShowcaseRoleFace (int face) {
    	for (int i = 0; i < image.size(); i ++) {
    		RoleFaceData selfFace = (RoleFaceData)image.get(i);
    		if (selfFace.getFace() == face) {
    			return selfFace;
    		}
    	}
    	return null;
    }
    
    /**
     * 判断能否更换称号
     * @param face
     * @return		1:已经有此称号
     */
    public int isCanChangeRoleTitle(String title){
        for (int i = 0; i < roleTitle.size(); i++) {
        	String roleTitleStr = (String)roleTitle.get(i);
            if (roleTitleStr.equals(title)) {							//已经加入到称号橱窗里了
            	return 1;
            }
        }
        return 0;
    }
    /**
     * 在橱窗中移出形象
     * @param face
     * @return
     */
    public boolean removeRoleFace(int face){
    	for(int i = 0; i < image.size(); i ++){
    		RoleFaceData selfFace = (RoleFaceData)image.get(i);
            if (selfFace.getFace() == face) {							//已经加入到形象橱窗里了
            	image.remove(i);
            	return true;
            }
    	}
    	return false;
    }
    /**
     * 在橱窗中移出称号
     * @param face
     * @return
     */
    public boolean removeRoleTitle(String title){
    	for(int i = 0; i < roleTitle.size(); i ++){
    		String curRoleTitle = (String)roleTitle.get(i);
            if (curRoleTitle.equals(title)) {							//已经加入到称号橱窗里了
            	roleTitle.remove(i);
            	return true;
            }
    	}
    	return false;
    }
    /**
     * 改变性别的时候，删除其他的形象，只保留基本的形象
     */
    public void changeRoleFace(){
    	image.clear();				//清空原先的形象
    	RoleFaceData roleFace;
    	if (player.getSex() == 0){		//改为男性
    		// 阵营
	    	if(player.getCamp() == 1){	//黑暗
	    		roleFace = RoleFaces.getRoleFace(30);
	    	}else if(player.getCamp() == 2){
	    		roleFace = RoleFaces.getRoleFace(28);
	    	}else{
	    		roleFace = RoleFaces.getRoleFace(0);
	    	}
    	}else{//改为女性
    		// 阵营
	    	if(player.getCamp() == 1){
	    		roleFace = RoleFaces.getRoleFace(31);
	    	}else if(player.getCamp() == 2){
	    		roleFace = RoleFaces.getRoleFace(29);
	    	}else{
	    		roleFace = RoleFaces.getRoleFace(1);
	    	}
        }
    	image.add(roleFace);
    }
    public Player getPlayer() {
        return player;
    }

    public void resetAbilities() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeShort(abilities.size());
            for (int i = 0; i < abilities.size(); i++) {
                Ability ability = (Ability) abilities.get(i);
                dos.writeShort(ability.getId());
            }
            player.setAbilities(bos.toByteArray());
        } catch (Exception ex) {
            log.info(ex, ex);
        }
    }
    /**
     * 保存形象橱窗里的形象
     */
    public void resetImage() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeShort(0);	//无用字段
            dos.write(3);		//version (1形象永久，2增加形象过期，3增加表情称号)
            dos.writeByte(1);		//形象
            dos.write(image.size());
            for (int i = 0; i < image.size(); i++) {
                RoleFaceData roleFace = (RoleFaceData)image.get(i);
                dos.write(roleFace.toDbBytes());
            }
            dos.writeByte(2);		//称号
            dos.write(roleTitle.size());
            for (int i = 0; i < roleTitle.size(); i++) {
                String curRoleTitle = (String)roleTitle.get(i);
                dos.writeUTF(curRoleTitle);
            }
            dos.writeByte(3);	//表情
            dos.write(phizTitleList.size());
            for(int i=0;i<phizTitleList.size();i++){
            	PhizTitleData tmpPhiz = (PhizTitleData)phizTitleList.get(i);
            	dos.write(tmpPhiz.toDbBytes());
            }
            player.setImage(bos.toByteArray());
        } catch (Exception ex) {
            log.info(ex, ex);
        }
    }

    public void resetRecipes() {
        try {
            if (recipes.size() == 0)
                return;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeShort(recipes.size());
            for (int i = 0; i < recipes.size(); i++) {
                Recipe recipe = (Recipe) recipes.get(i);
                dos.writeInt(recipe.getId());
            }
            player.setRecipes(bos.toByteArray());
        } catch (Exception ex) {
            log.info(ex, ex);
        }
    }
    /**
     * 新的配方
     */
    private void resetPrescription() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.write((byte)1);												//版本号
            dos.writeInt(skillPoint2);									//打造技能熟练度
            dos.writeShort(playerPrescriptions.size());
            for (int i = 0; i < playerPrescriptions.size(); i++) {
            	Prescription rescription = (Prescription) playerPrescriptions.get(i);
                dos.writeInt(rescription.getId());
            }
            player.setPrescription(bos.toByteArray());
        } catch (Exception ex) {
            log.info(ex, ex);
        }
    }

    public void reset() {
        resetUsedEquipments();
        resetBasicItems();
        resetTaskItems();
        resetExtendedItems();
        resetEquipments();
        resetAbilities();
        resetCurrentTasks();
        resetChatOptions();
        resetCompletedTasks();
        resetRecipes();
        resetSkills();
        resetFriends();
        resetBlackList();
        resetPets();
        resetOption();
        resetKey9_Option();
        resetBufs();
        resetEnemys();
        resetImage();
        resetPrescription();
        resetPlayerPool();
        resetOtherPool();
    }

    private void resetBufs(){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            Object[] oBufs = bufs.toArray();
            Object[] eBufs = extendedBufs.toArray();
            Object[] cBufs = campBattleBuffs.toArray();
            dos.writeShort(oBufs.length + eBufs.length + cBufs.length);
            for (int i = 0; i < oBufs.length; i++) {
                dos.write(((Buf)oBufs[i]).toDbBytes());
            }
            for(int i=0;i<eBufs.length;i++){
                dos.write(((Buf)eBufs[i]).toDbBytes());
            }
            for (int i = 0; i < cBufs.length; i++) {
            	dos.write(((Buf)cBufs[i]).toDbBytes());
            }
            player.setBufs(bos.toByteArray());
        } catch (Exception ex) {
            log.error(ex, ex);
        }
    }

    private void resetOption() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            for (int i = 0; i < options.length; i++) {
                dos.writeShort(options[i]);
            }
            player.setOptions(bos.toByteArray());
        } catch (Exception ex) {
            log.error(ex, ex);
        }
    }

    private void resetKey9_Option() {
        try {
        	ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            for (int i = 0; i < key9_options.length; i++) {
                dos.writeInt(key9_options[i]);
            }
            player.setKey9_options(bos.toByteArray());
        } catch (Exception ex) {
            log.error(ex, ex);
        }
    }
    private void resetBasicItems() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeShort(basicItems.size());
            for (int i = 0; i < basicItems.size(); i++) {
                Grid grid = (Grid) basicItems.get(i);
                dos.writeInt(grid.item.getItemId());
                dos.writeByte(grid.count);
            }
            player.setBasicItems(bos.toByteArray());
        } catch (Exception ex) {
            log.error(ex, ex);
        }
    }

    private void resetTaskItems() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeShort(taskItems.size());
            for (int i = 0; i < taskItems.size(); i++) {
                Grid grid = (Grid) taskItems.get(i);
                dos.writeInt(grid.item.getItemId());
                dos.writeByte(grid.count);
            }
            player.setTaskItems(bos.toByteArray());
        } catch (Exception ex) {
            log.error(ex, ex);
        }
    }

    private void resetExtendedItems() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeShort(extendedItems.size());
            for (int i = 0; i < extendedItems.size(); i++) {
                Grid grid = (Grid) extendedItems.get(i);
                dos.writeInt(grid.item.getItemId());
                dos.writeByte(grid.count);
            }
            player.setMetaItems(bos.toByteArray());
        } catch (IOException ex) {
            log.error(ex, ex);
        }
    }

    private void resetUsedEquipments() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            //dos.write((byte)2);//items version
            //dos.write((byte)3);//items version
            //dos.write((byte)4);				//items version 4  增加鉴定
            //dos.write((byte)5);				//items version 5 装备刻字
//            dos.write((byte)6);                 //items version 6 增加宝石系统
//          dos.write((byte)7);					//items version 7增加附魔系统
//            dos.write((byte)8);					//items version 8调整附魔数值
//            dos.write((byte)9);					//items version 9增加属性攻
            dos.write((byte)10);					//items version 10宝石养成
            dos.writeShort(getUsedEquipmentsCount());
            for (int i = 0; i < usedEquipments.length; i++) {
                Grid grid = (Grid) usedEquipments[i];
                if (grid != null) {
                    IEquipment equ = (IEquipment) grid.item;
                    dos.write(equ.toDbBytes());
                }
            }
            player.setUsedEquipments(bos.toByteArray());
        } catch (Exception ex) {
            log.error(ex, ex);
        }
    }

    private void resetEquipments() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            //dos.write((byte)2);//items version
            //dos.write((byte)3);//items version
            
            //dos.write((byte)4);				//items version 4  增加鉴定
            //dos.write((byte)5);				//items version 5 装备刻字
//            dos.write((byte)6);                 //items version 6 增加宝石系统	
//          dos.write((byte)7);					//items version 7增加附魔系统
//            dos.write((byte)8);					//items version 8调整附魔数值
//            dos.write((byte)9);					//items version 9增加属性攻
            dos.write((byte)10);					//items version 10宝石养成
            dos.writeShort(equipments.size());
            for (int i = 0; i < equipments.size(); i++) {
                Grid grid = (Grid) equipments.get(i);
                IEquipment equ = (IEquipment) grid.item;
                dos.write(equ.toDbBytes());
            }
            player.setEquipments(bos.toByteArray());
        } catch (Exception ex) {
            log.error(ex, ex);
        }
    }

    public void resetPets() {
        try {
            Pet[] ps = new Pet[pets.size()];
            pets.toArray(ps);
            player.setPets(Pet.toDbBytes_version6(ps));
        } catch (Exception ex) {
            log.error(ex, ex);
        }
    }

    protected void resetEnemys(){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeShort(enemys.size());
            for (int i = 0; i < enemys.size(); i++) {
                Enemy enemy = (Enemy) enemys.get(i);
                dos.writeInt(enemy.id);
                dos.writeUTF(enemy.name);
                dos.writeInt(enemy.times);
                dos.writeLong(enemy.lastTime);
            }
            player.setEnemys(bos.toByteArray());
        } catch (Exception ex) {
            log.error(ex, ex);
        }
    }
    
    protected void resetPlayerPool () {
    	resetVipLevel();
    	resetLastGetVipGiftTime();
    	resetVipValidTime();
    	resetChrItemCount();
    	resetLastWorldCompleteTime();
    	resetLastCampCompleteTime();
    	resetLastRollcallTime();
    	resetRollcallDays();
    	resetCheckExpBag();
        setPlayerPool(getPlayerPool().toString());
    }
    
    protected void resetVipLevel () {
    	getPlayerPool().setInt(VIP_LEVEL, getVipLevel());
    }
    
    protected void resetChrItemCount(){
    	getPlayerPool().setInt(CHR_ITEM_COUNT, getChrItemCount());
    }
    
    protected void resetLastWorldCompleteTime () {
    	getPlayerPool().setString(LAST_WORLD_COMPLETE_TIME, format.format(getLastWorldCompleteTime()));
    }
    
    protected void resetLastCampCompleteTime () {
    	getPlayerPool().setString(LAST_CAMP_COMPLETE_TIME, format.format(getLastCampCompleteTime()));
    }
    
    protected void resetLastGetVipGiftTime () {
    	getPlayerPool().setString(LAST_GET_VIP_GIFT_TIME, format.format(getLastGetVipGiftTime()));
    }
    
    protected void resetVipValidTime () {
    	if (getVipValidTime() != null && getVipValidTime().equals("") == false) {
    		getPlayerPool().setString(VIP_VALID_TIME, format.format(getVipValidTime()));
    	} else {
    		getPlayerPool().setString(VIP_VALID_TIME, "");
    	}
    }
    
    protected void resetLastRollcallTime () {
    	getPlayerPool().setString(LAST_ROLLCALL_TIME, format.format(getLastRollcallCompleteTime()));
    }
    
    protected void resetRollcallDays () {
    	getPlayerPool().setInt(ROLLCALL_DAYS, getRollcallDays());
    }
    
    public int getUsedEquipmentsCount() {
        int ret = 0;
        for (int i = 0; i < usedEquipments.length; i++) {
            if (usedEquipments[i] != null) {
                ret++;
            }
        }
        return ret;
    }

    public IEquipment[] getUsedEquipments() {
        IEquipment[] ret = new IEquipment[usedEquipments.length];
        for (int i = 0; i < usedEquipments.length; i++) {
            if (usedEquipments[i] != null)
                ret[i] = (IEquipment) usedEquipments[i].item;
        }
        return ret;
    }
    
    public Grid[] getUseEquipments(){
    	return usedEquipments;
    }

    public IEquipment getUsedEquipment(int itemId, int id) {
        for (int i = 0; i < usedEquipments.length; i++) {
            if (usedEquipments[i] != null) {
                if (usedEquipments[i].item.getItemId() == itemId &&
                    usedEquipments[i].item.getId() == id) {
                    return (IEquipment) usedEquipments[i].item;
                }
            }
        }
        return null;
    }
    
    // 获得玩家已装备的宝石级别（用于判断同级别宝石是否>=30个；是发光【返回发光级别】，否不发光；目前只有3-7种发光级别）
    // getDiamondMosiacRoleInfo获得数值解析0是未打孔，1是打孔未镶嵌，以上获得数值-1就是宝石等级
    public int getGemEffectLevel () {
    	List<Integer> gemLevelInfo = new ArrayList <Integer> ();
    	for (int i = 0; i < usedEquipments.length; i++) {
    		Grid grid = usedEquipments[i];
            if (grid != null) {
                IEquipment item = (IEquipment) grid.item;
                byte[] gemInfo = item.getDiamondMosiacRoleInfo();
                for (int j = 0; j < gemInfo.length; j ++) {
                	int size = gemLevelInfo.size();
                	if (size < gemInfo[j]) {
                		for (int k = 0; k < gemInfo[j] - size; k ++) {
                			gemLevelInfo.add(0);
                		}
                	}
                }
            }
    	}
    	int ret = -1;
    	int count = 0;
    	for (int i = 0; i < usedEquipments.length; i++) {
            Grid grid = usedEquipments[i];
            if (grid != null) {
                IEquipment item = (IEquipment) grid.item;
                byte[] gemInfo = item.getDiamondMosiacRoleInfo();
                for (int j = 0; j < gemInfo.length; j ++) {
                	if (gemInfo[j] > 1) {
            			count = gemLevelInfo.get(gemInfo[j] - 1);
                		count ++;
                		gemLevelInfo.set(gemInfo[j] - 1, count);
                	}
                }
            }
        }
    	int tmpHoly = 0;
    	int tmpFantasy = 0;
    	for (int i = gemLevelInfo.size() - 1; i >= 0; i --) {
    		int sum = 0;
    		for (int j = i + 1; j < gemLevelInfo.size(); j ++) {
    			int tmp = gemLevelInfo.get(j);
    			sum += tmp;
    		}
    		count = gemLevelInfo.get(i) + sum;
    		if (count >= Utils.gemEffectCount && ret < i) {
    			ret = i;
//    			player.setLightLevel((byte) (i));
//    			return i;
    		}
    		if(i > 2 && count >= Utils.gemEffectCount_Holy && tmpHoly < i){
    			tmpHoly = i;
    		}
    		if(i > 2 && count >= Utils.gemEffectCount_Fantasy && tmpFantasy < i){
    			tmpFantasy = i;
    		}
    	}
    	player.setLightLevel((byte) (ret));
    	setHolyGemLightLevel((byte)tmpHoly);
    	setFantasyGemLightLevel((byte)tmpFantasy);
    	return ret;
    }
    
    public void setHolyGemLightLevel(byte level){
    	gemLightLevel_Holy = level;
    }
    
    public byte getHolyGemLightLevel(){
    	return gemLightLevel_Holy;
    }
    
    public void setFantasyGemLightLevel(byte level){
    	gemLightLevel_Fantasy = level;
    }
    
    public byte getFantasyGemLightLevel(){
    	return gemLightLevel_Fantasy;
    }
    
    /**
     * 设置VIP等级
     * @param vipLevel
     */
    public void setVipLevel (int vipLevel) {
    	this.vipLevel = vipLevel;
    }
    
    /**
     * 设置VIP新等级
     * @param Newlevel
     */
    public void setVipNewLevel(int Newlevel){
    	this.vipNewLevel = Newlevel;
    }
    
    
    /**
     * 设置捐的物品的个数
     */
    public void setChrItemCount(int itemCount){
    	this.chrItemCount = itemCount;
    }
    /**
     * 设置最后一次完成世界喊话活动的时间
     * @param lastWorldCompleteTime
     */
    public void setLastWorldCompleteTime (Date lastWorldCompleteTime) {
    	this.lastWorldCompleteTime = lastWorldCompleteTime;
    }
    /**
     * 设置最后一次完成阵营喊话活动的时间
     * @param lastCampCompleteTime
     */
    public void setLastCampCompleteTime (Date lastCampCompleteTime) {
    	this.lastCampCompleteTime = lastCampCompleteTime;
    }
    /**
     * 设置最后一次完成点名活动的时间
     * @param lastRollcallCompleteTime
     */
    public void setLastRollcallCompleteTime (Date lastRollcallCompleteTime) {
    	this.lastRollcallCompleteTime = lastRollcallCompleteTime;
    }
    /**
     * 连续完成点名活动的天数
     * @param lastRollcallCompleteTime
     */
    public void setRollcallDays (int rollcallDays) {
    	this.rollcallDays = rollcallDays;
    }
    /**
     * 腊八活动消费金额（RMB）
     * @param twelfthLunarConsumer
     */
    public void setTwelfthLunarConsumer (int twelfthLunarConsumer) {
    	this.twelfthLunarConsumer = twelfthLunarConsumer;
    }
    /**
     * 最后一次领取腊八粥的时间
     * @param twelfthLunarBeans
     */
    public void setTwelfthLunarLastReceiveTime (Date twelfthLunarLastReceiveTime) {
    	this.twelfthLunarLastReceiveTime = twelfthLunarLastReceiveTime;
    }
    /**
     * 捐献腊八豆总的个数
     * @param twelfthLunarBeansCount
     */
    public void setTwelfthLunarBeansCount (int twelfthLunarBeansCount) {
    	this.twelfthLunarBeansCount = twelfthLunarBeansCount;
    }
    /**
     * 设置消费额度
     * @param activityConsumer
     */
    public void setActivityConsumer (int activityConsumer) {
    	this.activityConsumer = activityConsumer;
    }
    
    /**
     * 设置新消费额度
     * @param NewConsumer
     */
    public void setNewActivityConsumer (int NewConsumer) {
    	this.NewActivityConsumer = NewConsumer;
    }
    
    /**
     * 设置杀戮点数
     * @param campBattlefieldKillPoints
     */
    public void setCampBattlefieldKillingPoints (int campBattlefieldKillPoints) {
    	this.campBattlefieldKillPoints = campBattlefieldKillPoints;
    }
    
    public void setCheckExpBag(int getExp){
    	this.checkExpBag = getExp;
    }
    public int getCheckExpBag(){
    	return checkExpBag;
    }
    public void resetCheckExpBag(){
    	getPlayerPool().setInt(CHECK_EXPBAG, getCheckExpBag());
    }
    
    
//    /**
//     * 设置最后一次完成世界喊话活动的时间
//     * @param lastShoutCompleteTime
//     */
//    public void setLastShoutCompleteTime (Date lastShoutCompleteTime) {
//    	this.lastShoutCompleteTime = lastShoutCompleteTime;
//    }
    
    /**
     * 设置最后领取VIP工资卡的时间
     * @param lastGetVipGiftTime
     */
    public void setLastGetVipGiftTime (Date lastGetVipGiftTime) {
		this.lastGetVipGiftTime = lastGetVipGiftTime;
    }
    
    /**
     * 设置最后重置杀戮点数时间
     * @param lastResetKillPointsTime
     */
    public void setLastResetKillPointsTime (Date lastResetKillPointsTime) {
    	this.lastResetKillPointsTime = lastResetKillPointsTime;
    }
    
    public void setKillPointConsume(int killPointConsume){
    	this.killPointConsume = killPointConsume;
    }
    public void setKillPointConsumeTime (Date killPointConsumeTime) {
    	this.killPointConsumeTime = killPointConsumeTime;
    }
    
    public void setVianyType(int vianyType){
    	this.vianyType = vianyType;
    }
    
    public void setOnlineTimer(long onlineTimer){
    	this.onlineTimer = onlineTimer;
    }
    
    public void setLeaderShip(int leadership){
    	this.leadership = leadership;
    }
    
    public void setMagicIMoney(int money){
    	this.magicimoney = money;
    }
    
    /**
     * 设置VIP有效时间
     */
    public void setVipValidTime (Date validTime) {
		this.validTime = validTime;
    }
    
    /**
     * 获得VIP等级
     * 注：为玩家体验更好；内存操作，数据库存储都是从0开始，显示以及LOG日志都是从1开始
     * @return vipLevel
     */
    public int getVipLevel () {
    	return vipLevel;
    }
    
    /**
     * 获得VIP新等级
     * @return vipNewLevel
     */
    
    public int getVipNewLevel(){
    	return vipNewLevel;
    }
    
    /**
     * 获得捐的物品的个数
     * @return
     */
    public int getChrItemCount(){
    	return chrItemCount;
    }
    /**
     * 获取最后一次完成喊话活动的时间
     * @return
     */
    public Date getLastWorldCompleteTime () {
    	return lastWorldCompleteTime;
    }
    /**
     * 获取最后一次完成喊话活动的时间
     * @return
     */
    public Date getLastCampCompleteTime () {
    	return lastCampCompleteTime;
    }
    
    /**
     * 获得最后领取VIP工资卡的时间
     * @return lastGetVipGiftTime
     */
    public Date getLastGetVipGiftTime () {
    	return lastGetVipGiftTime;
    }
    
    public Date getLastResetKillPointsTime () {
    	return lastResetKillPointsTime;
    }
    
    public int getKillPointConsume(){
    	return killPointConsume;
    }
    public Date getKillPointConsumeTime () {
    	return killPointConsumeTime;
    }
    
    public int getVianyType(){
    	return vianyType;
    }
    
    public long getOnlineTimer(){
    	return onlineTimer;
    }
    
    public int getLeaderShip(){
    	return leadership;
    }
    
    public int getMagicIMoney(){
    	return magicimoney;
    }
    
    /**
     * 获得有效期时间
     * @return validTime
     */
    public Date getVipValidTime () {
    	return validTime;
    }
    
    /**
     * 获得最后一次完成点名活动的时间
     * @return
     */
    public Date getLastRollcallCompleteTime () {
    	return lastRollcallCompleteTime;
    }
    /**
     * 获得连续完成点名活动的天数
     * @return
     */
    public int getRollcallDays () {
    	return rollcallDays;
    }
    /**
     * 获得腊八活动消费金额（RMB:分）
     * @return
     */
    public int getTwelfthLunarConsumer () {
    	return twelfthLunarConsumer;
    }
    /**
     * 获得最后一次领取腊八粥的时间
     * @return
     */
    public Date getTwelfthLunarLastReceiveTime () {
    	return twelfthLunarLastReceiveTime;
    }
    /**
     * 获得捐献腊八豆总个数
     * @return
     */
    public int getTwelfthLunarBeansCount () {
    	return twelfthLunarBeansCount;
    }
    /**
     * 获得消费额度
     * @return
     */
    public int getActivityConsumer () {
    	return activityConsumer;
    }
    
    /**
     * 获得新消费额度
     * @return
     */
    public int getNewActivityConsumer () {
    	return NewActivityConsumer;
    }
    
    /**
     * 获得杀戮点数
     * @return
     */
    public int getCampBattlefieldKillPoints () {
    	return campBattlefieldKillPoints;
    }
    
    public PropertyPool getPlayerPool () {
    	return playerPool;
    }
    
    public void setPlayerPool (String poolText) {
    	player.setPlayerPool(poolText);
    }

    public boolean hasEquipmented(int itemId){
        for (int i = 0; i < usedEquipments.length; i++) {
            if (usedEquipments[i] != null) {
                if (usedEquipments[i].item.getItemId() == itemId) {
                    return true;
                }
            }
        }
        return false;
    }

    public void resetFriends() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeByte(friends.size());
            Iterator ite = friends.values().iterator();
            while(ite.hasNext()){
                Friend friend = (Friend)ite.next();
                dos.writeInt(friend.getId());
                dos.writeUTF(friend.getName());
                dos.writeInt(friend.getFavorite());
            }
            player.setFriends(bos.toByteArray());
        } catch (Exception ex) {
            log.info(ex, ex);
        }
    }

    public void resetBlackList(){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeByte(blackList.size());
            for (int i = 0; i < blackList.size(); i++) {
                PlayerLink p = (PlayerLink) blackList.get(i);
                dos.writeInt(p.id);
                dos.writeUTF(p.name);
            }
            player.setBlackList(bos.toByteArray());
        } catch (Exception ex) {
            log.info(ex, ex);
        }
    }
    
    /**
     * 修正老版本血蓝问题
     */
    public void repairOldVersionHpMp(int version){
    	if(version < 1){
    		int maxHp = getRealVitality() * 6 * ((int)Utils.sqrt(player.getLevel() * 100) + 30) / 40 + 50;
    		setMaxHp(maxHp);
    		if(player.getHp() > maxHp){
    			player.setHp(maxHp);
    			
    		}
    		int maxMp = getRealIntelligence() * 3 * ((int)Utils.sqrt(player.getLevel() * 100) + 30) / 40 + 50;
    		setMaxMp(maxMp);
    		if(player.getMp() > maxMp){
    			player.setMp(maxMp);
    		}
    	}
    }
    /**
     * 转换为客户端可以识别的格式。
     * @param version 0 - 表示幻想i时代版本，1 - 表示明珠幻想版本
     * @return
     * @throws Exception
     */
    public byte[] toClientBytes(int version) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(player.getSex());
        dos.writeShort(player.getFace());
        dos.writeByte(player.getReturnTimes());
        dos.writeShort(player.getLevel());
        dos.writeInt(player.getExp());
        dos.writeInt(Utils.getUpLevelExp(player.getLevel()));
        dos.writeInt(player.getMoeny());
        dos.writeInt(player.getCredit());
        dos.writeShort(player.getStrength());
        dos.writeShort(player.getAgility());
        dos.writeShort(player.getVitality());
        dos.writeShort(player.getIntelligence());
        dos.writeLong(player.getLuck());
        if(getPropertyBufs().length>0){  //保存buf的问题，客户端的问题需要特殊处理
            dos.writeInt(0);
            dos.writeInt(0);
        }
        else{
        	repairOldVersionHpMp(version);
        	dos.writeInt(player.getHp());
            dos.writeInt(player.getMp());
        }
        dos.writeShort(getAllGridSize());
        dos.writeByte(player.getLeavePoints());
        long l = (long) player.getPoint() << 32 | player.getAbilityPoints();
        dos.writeLong(l);
        dos.writeShort(abilities.size());
        for (int i = 0; i < abilities.size(); i++) {
            Ability ability = (Ability) abilities.get(i);
            dos.writeShort(ability.getId());
        }
        for (int i = 0; i < skillPoint.length; i++) {
            dos.writeShort(skillPoint[i]);
        }
        int count = getUsedEquipmentsCount();
        dos.writeShort(count);
        for (int i = 0; i < usedEquipments.length; i++) {
        	int level = getGemEffectLevel ();
            Grid grid = usedEquipments[i];
            if (grid != null) {
                IEquipment item = (IEquipment) grid.item;
                item.setDataVersion(version);
                dos.write(item.toClientBytesWithLevel(player.getLevel()));
            }
        }
        dos.writeShort(basicItems.size());
        for (int i = 0; i < basicItems.size(); i++) {
            Grid grid = (Grid) basicItems.get(i);
            BasicItem item = (BasicItem) grid.item;
            dos.write(item.toClientBytes(version));
            dos.writeByte(grid.count);
        }
        dos.writeShort(extendedItems.size());
        for (int i = 0; i < extendedItems.size(); i++) {
            Grid grid = (Grid) extendedItems.get(i);
            ExtendedItem item = (ExtendedItem) grid.item;
            dos.write(item.toClientBytes(version));
            dos.writeByte(grid.count);
        }
        dos.writeShort(taskItems.size());
        for (int i = 0; i < taskItems.size(); i++) {
            Grid grid = (Grid) taskItems.get(i);
            TaskItem item = (TaskItem) grid.item;
            dos.write(item.toClientBytes(version));
            dos.writeByte(grid.count);
        }
        dos.writeShort(equipments.size());
        for (int i = 0; i < equipments.size(); i++) {
            Grid grid = (Grid) equipments.get(i);
            IEquipment item = (IEquipment) grid.item;
            item.setDataVersion(version);
            dos.write(item.toClientBytesWithLevel(-1));
        }
        dos.writeByte(friends.size());
        Iterator ite = friends.values().iterator();
        long now = new Date().getTime(); 
        Random rnd = new Random();
        
        //  zjl
        while(ite.hasNext()) {    
        	Friend friend = (Friend)ite.next();
            dos.writeInt(friend.getId());
            dos.writeUTF(friend.getName());
            dos.writeShort((short)friend.getFavorite());
            if((now - friend.getLoginTime()) > LeaveLineSecond){       
            	if(FriendLogin.isHave(friend.getId())){   //表中存在记录id 
            		friend.setLoginTime(FriendLogin.getFrindLoginTime(friend.getId()));	
            	}else{                                    //表中无记录随机一个day
            		int days = Utils.getRandom(rnd, 1, 10);
            		Calendar cal = Calendar.getInstance();
            		cal.setTimeInMillis(System.currentTimeMillis());
            		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - days);
            		friend.setLoginTime(cal.getTimeInMillis());
            		FriendLogin.setFriendLoginTime(friend.getId(), cal.getTimeInMillis());
            	}
            }
            dos.writeInt(Utils.getLoginTimeSecond(now, friend.getLoginTime()));
            
        }
        
        dos.writeByte(blackList.size());
        for(int i=0;i<blackList.size();i++){
            PlayerLink p = (PlayerLink) blackList.get(i);
            dos.writeInt(p.id);
            dos.writeUTF(p.name);
        }
        
        // 登录时下发。按版本号判断
        if (version > 0) {
        	dos.writeShort(options.length);
    		for (int i = 0; i < options.length; i++) {
                dos.writeShort(options[i]);
            }
    		if ((key9_options == null) && (key9_options.length == 0)){
    			dos.writeShort(0);
    		}else{
    			
    			dos.writeShort(key9_options.length);
    			String[] itemname = new String[key9_options.length];  
        		for (int i = 0; i < key9_options.length; i++) {
                    dos.writeInt(key9_options[i]);
                    if (key9_options[i] > 0){
                    	IItemTemplate itemtemplate = Items.getTemplate(key9_options[i]-1);
                    	if (itemtemplate != null){
                    		itemname[i]=itemtemplate.getName();
                    	}else{
                    		itemname[i]="";
                    	}
                    }else{
                    	itemname[i]="";
                    }
                }
        		for (int i = 0; i < key9_options.length; i++) {
                    dos.writeUTF(itemname[i]);
                }
    		}
    		
        } else {
        	// 旧版本只发前十位
        	for (int i = 0; i < 10; i++) {
        		dos.writeShort(options[i]);
        	}
        }
        
        for (int i = 0; i < chatOptions.length; i++) {
            dos.writeByte(chatOptions[i].pri);
            dos.writeByte(chatOptions[i].color);
        }
        ChatFavorite favorite = ChatFavorites.getFavorite(favoriteId);
        if (favorite == null) {
            dos.writeUTF("");
        } else {
            dos.writeUTF(favorite.name);
        }
        dos.writeUTF(getTongName() == null ? "" : getTongName());
        dos.writeByte(getTongDuty());
        dos.writeByte(getPetSize());
        dos.writeByte(pets.size());
        for (int i = 0; i < pets.size(); i++) {
            dos.write(((Pet) pets.get(i)).toClientBytesWithLevel(-1));
        }
        dos.writeInt(pet != null ? pet.getId() : -1);
        dos.writeUTF(getTitle());
        dos.writeUTF(getCreditName());
        
        // 下发套装属性加成
        int[] propertyPoint = getSuitEffectPropertyPoint();
        if (propertyPoint == null) {
        	 dos.writeInt(0);
        	 dos.writeInt(0);
        	 dos.writeInt(0);
        	 dos.writeInt(0);
        } else {
        	// 力量
        	dos.writeInt(propertyPoint[0]);
        	// 敏捷
        	dos.writeInt(propertyPoint[1]);
        	// 体力
        	dos.writeInt(propertyPoint[2]);
        	// 智力
        	dos.writeInt(propertyPoint[3]);
        }
       
        // 下发玩家VIP等级服务器存储是从0开始的，下发到客户端是从1开始，方便体验
        int toClientVip = 1;
        if (getVipValidTime() != null && getVipValidTime().equals("") == false) {
			long validTime = getVipValidTime().getTime();
			long timeNow = new Date().getTime();
			if (validTime >= timeNow) {
				toClientVip = getVipLevel() + 1;
			}
		}
        dos.writeInt(toClientVip);
        dos.writeInt(getVipNewLevel());
        dos.writeInt(getNewActivityConsumer());
        dos.writeInt(getCampBattlefieldKillPoints());
        
        //属性攻属性值 数值  @see Viany 中的
        dos.writeInt(getVianyType());
        dos.writeShort(getPhizTitleIndex());
        dos.writeByte(5);
        return bos.toByteArray();
    }
    //取得玩家下一级能学的技能 lfzuo add，1.5版本技能列表专用
    //返回一个map并返回且是否可以学习的标志，里面存放id, type
    public Map getNextSkill(byte skillType){
    	Map<Integer, Byte> nextSkillMap = new TreeMap<Integer, Byte>();
    	String last = "null";
    	 for(int i = 1;i < 1000;i ++){
        	 Ability temp = Ability.getAbility(i);
        	 if(temp != null && temp.getType() == skillType){
	        	 boolean contain = containsAbility(temp);
	        	 // 顶级技能也下发，但是id为负数
	        	 if(contain && temp.getMaxLevel() == temp.getLevel()) {
	        		 //l.add(new Integer(-temp.getId()));
	        		 nextSkillMap.put(temp.getId() , (byte)0);
	        		 last = temp.getName();
	        	 }
	        	 //没有学会的第一个技能
	             if(!contain && !last.equals(temp.getName())){
	            	 //l.add(new Integer(temp.getId()));
	            	 nextSkillMap.put(temp.getId() , (byte)1);
	        		 last = temp.getName();
	             }
        	 }
         }
    	return nextSkillMap;
    }

   //为玩家自动学会该级能及以下等级所可以学的技能           mengjie add，1.5版本技能列表专用
   //返回一个map并返回且是否可以学习的标志，里面存放id, type
    public Map learnAllSkill(byte skillType,byte level){
    	Map<Integer, Byte> nextSkillMap = new TreeMap<Integer, Byte>();
    	String last = "null";
    	 for(int i = 1;i < 1000;i ++){
        	 Ability temp = Ability.getAbility(i);
        	 if(temp != null && temp.getType() == skillType){
	        	 boolean contain = containsAbility(temp);
	        	 // 顶级技能也下发，但是id为负数
	        	 if(contain && temp.getMaxLevel() == temp.getLevel()) {
	        		 //l.add(new Integer(-temp.getId()));
	        		 nextSkillMap.put(temp.getId() , (byte)0);
	        		 last = temp.getName();
	        	 }
	        	 //没有学会的第一个技能
	             if(!contain && !last.equals(temp.getName())){
	            	 //l.add(new Integer(temp.getId()));
	            	 nextSkillMap.put(temp.getId() , (byte)1);
	        		 last = temp.getName();
	             }
        	 }
         }
    	return nextSkillMap;
    }


    /**
     * @param skillId
     * @return获取技能列表里面最高级别的技能
     */
    public int[] getViewSkill(){
    	//存放key为级别，value为等级
    	Map<Integer, Ability> skillMap = new TreeMap<Integer, Ability>();
    	for(int i = 0; i < abilities.size(); i++){
    		Ability temp = (Ability) abilities.get(i);
    		if(!skillMap.containsKey(temp.getEffect())){
    			skillMap.put(temp.getEffect(), temp);
    		}else{
    			Ability destTemp = skillMap.get(temp.getEffect());
    			if(temp.getLevel() > destTemp.getLevel()){
    				skillMap.put(temp.getEffect(), temp);
    			}
    		}
    	}
    	int[] skillId = new int[skillMap.size()];
    	int i = 0;
    	for(Map.Entry<Integer, Ability> ability: skillMap.entrySet()){
    		skillId[i] = ability.getValue().getId();
    		i++;
    	}
    	return skillId;
    }
    
    public byte[] getTaskData() {
        return player.getTaskData().getSaveData();
    }

    public void setTaskData(byte[] saveData) {
        player.getTaskData().setSaveData(saveData);
    }

    public int getAccountId() {
        return player.getAccountId();
    }

    public int getAgility() {
        return player.getAgility();
    }

    public Grid[] getBasicItems() {
        Grid[] ret = new Grid[basicItems.size()];
        basicItems.toArray(ret);
        return ret;
    }

    public Grid[] getExtendedItems() {
        Grid[] ret = new Grid[extendedItems.size()];
        extendedItems.toArray(ret);
        return ret;
    }

    public Grid[] getTaskItems() {
        Grid[] ret = new Grid[taskItems.size()];
        taskItems.toArray(ret);
        return ret;
    }

    public Grid[] getEquipments() {
        Grid[] ret = new Grid[equipments.size()];
        equipments.toArray(ret);
        return ret;
    }
    
    public Grid getLimitUsedEquipments(int i) {
        Grid ret = usedEquipments[i];
        return ret;
    }
    
    public Date getCreateTime() {
        return player.getCreateTime();
    }

    public int getCredit() {
        return player.getCredit();
    }

    public byte[] getData() {
        return player.getData();
    }


    public int getExp() {
        return player.getExp();
    }

    public short getFace() {
        return player.getFace();
    }
    
    public byte getLightLevel() {
    	return player.getLightLevel();
    }
    
    public long getPetPracticeMaxTime () {
    	return player.getPetPracticeMaxTime();
    }

    public int getHouseLevel() {
        return player.getHouseLevel();
    }

    public int getHp() {
        return player.getHp();
    }

    public int getId() {
        return player.getId();
    }

    public int getIntelligence() {
        return player.getIntelligence();
    }

    public Date getLastLoginTime() {
        return player.getLastLoginTime();
    }

    //可以分配的属性点
    public int getLeavePoints() {
        return player.getLeavePoints();
    }

    public int getLevel() {
        return player.getLevel();
    }

    public int getLuck() {
        return player.getLuck();
    }

    public short getMapId() {
        return player.getMapId();
    }

    public byte[] getMetaItems() {
        return player.getMetaItems();
    }


    public int getMp() {
        return player.getMp();
    }

    
    public String getPlayerCampName(){
    	String playerName = player.getPlayerName();
    	if(player.getCamp() == 0){
    		playerName = playerName + "(无阵营)";
    	}else if(player.getCamp() == 1){
    		playerName = playerName + "(黑暗阵营)";
    	}else if(player.getCamp() == 2){
    		playerName = playerName + "(光明阵营)";
    	}
    	return playerName;
    }
    public String getPlayerName() {
        return player.getPlayerName();
    }

    public byte getReturnTimes() {
        return player.getReturnTimes();
    }

    public byte getSex() {
        return player.getSex();
    }

    public int getStrength() {
        return player.getStrength();
    }


    public int getTongDuty() {
        return player.getTongDuty();
    }


    public int getTongId() {
        return player.getTongId();
    }

    public String getTongName() {
        return player.getTongName();
    }


    public int getVitality() {
        return player.getVitality();
    }

    public short getX() {
        return player.getX();
    }

    public short getY() {
        return player.getY();
    }

    public Date getQuestionTime() {
    	return player.getQuestionTime();
    }

    public int getQuestionState() {
    	return player.getQuestionState();
    }

    public void setQuestionState(int c) {
    	player.setQuestionState(c);
    }

    public void setQuestionTime(Date t) {
    	player.setQuestionTime(t);
    }

    public void setAccountId(int accountId) {
        player.setAccountId(accountId);
    }

    public void setAgility(int agility) {
        player.setAgility(agility);
    }

    public void setBasicItems(byte[] basicItems) {
        player.setBasicItems(basicItems);
    }

    public void setCreateTime(Date createTime) {
        player.setCreateTime(createTime);
    }

    public void setCredit(int credit) {
        player.setCredit(credit);
    }

    public void addCredit(int credit,Changed changed){
    	int newCredit = 0;
    	if (player.getCredit()+credit>CREDIT[CREDIT.length-1]){
    		newCredit = Math.min(player.getCredit()+credit,2000000000);
    	}else{
    		newCredit = Math.min(player.getCredit()+credit,CREDIT[CREDIT.length-1]);
    	}
        player.setCredit(newCredit);
        if(changed!=null){
            changed.addProperty(Changed.CREDIT, credit);
        }
        int newCreditIndex = getCreditIndex();
        if(newCreditIndex!=creditIndex){
            creditIndex = newCreditIndex;
            if(changed!=null){
                changed.setProperty(Changed.CREDIT_STRING, getCreditName());
            }
        }
    }

    public void setData(byte[] data) {
        player.setData(data);
    }

    public void setEquipments(byte[] equipments) {
        player.setEquipments(equipments);
    }


    public void setExp(int exp) {
        player.setExp(exp);
    }

    public void setFace(short face) {
        player.setFace(face);
    }

    public void setLightLevel(byte gemLightLevel) {
    	player.setLightLevel(gemLightLevel);
    }
    
    public void setPetPracticeMaxTime (long petPracticeMaxTime) {
    	player.setPetPracticeMaxTime(petPracticeMaxTime);
    }
    
    public void setHouseLevel(int houseLevel) {
        player.setHouseLevel(houseLevel);
    }

    public void setHp(int hp) {
        player.setHp(hp);
    }

    public void addHp(int hp) {
        int h = hp + getHp();
        setHp(Math.min(h, getMaxHp() + getBufProperty(Changed.HP) + getSuitEffectProperty(Changed.HP)));
    }

    public void addMp(int mp) {
        int m = mp + getMp();
        setMp(Math.min(m, getMaxMp()+ getBufProperty(Changed.MP) + getSuitEffectProperty(Changed.MP)));
    }

    public void setId(int id) {
        player.setId(id);
    }


    public void setIntelligence(int intelligence) {
        player.setIntelligence(intelligence);
    }

    public void setLastLoginTime(Date lastLoginTime) {
        player.setLastLoginTime(lastLoginTime);
    }


    public void setLeavePoints(int leavePoints) {
        player.setLeavePoints(leavePoints);
    }


    public void setLevel(int level) {
        player.setLevel(level);
    }

    public void setLuck(int luck) {
        player.setLuck(luck);
    }

    public void setMapId(short mapId) {
        player.setMapId(mapId);
    }


    public void setMoeny(int money) {
    	if(money < 0){
    		log.error("setMoney error playerID[" + this.getId() + "] money[" + money + "]");
    	}else if(money > Integer.MAX_VALUE){
    		player.setMoeny(Integer.MAX_VALUE);
    		log.error("setMoney error playerID[" + this.getId() + "] money[" + money + "]");
    	}else{
    		player.setMoeny(money);
    	}
    }

    public int getMoeny() {
        return player.getMoeny();
    }


    public void setMp(int mp) {
        player.setMp(mp);
    }


    public void setPlayerName(String playerName) {
        player.setPlayerName(playerName);
    }

    public void setReturnTimes(byte returnTimes) {
        player.setReturnTimes(returnTimes);
    }

    public void setSex(byte sex) {
        player.setSex(sex);
    }

    public void setStrength(int strength) {
        player.setStrength(strength);
    }


    public void setTongDuty(int tongDuty) {
        player.setTongDuty(tongDuty);
    }

    public void setTongId(int tongId) {
        player.setTongId(tongId);
    }

    public void setTongName(String tongName) {
        player.setTongName(tongName);
    }

    public void setTongTitle(String tongTitle) {
        player.setTongTitle(tongTitle);
    }

    public String getTongTitle() {
        return player.getTongTitle();
    }

    public void setVitality(int vitality) {
        player.setVitality(vitality);
    }

    public void setX(short x) {
        player.setX(x);
    }

    public void setY(short y) {
        player.setY(y);
    }

    public List getImage() {
		return image;
	}
    
    public List getRoleTitle() {
    	return roleTitle;
    }
    
    public void setRoleTitle(List roleTitle){
    	this.roleTitle = roleTitle;
    }

	public void setImage(List image) {
		this.image = image;
	}
	
	public List getPhizTitleList(){
		return phizTitleList;
	}
	
	public void setPhizTitleList(List phizTitle){
		this.phizTitleList = phizTitle;
	}
	
	public short getPhizTitleIndex(){
		return phizTitleIndex;
	}
	
	public void setPhizTitleIndex(short phizTitleIndex){
		this.phizTitleIndex = phizTitleIndex;
	}
	
	public String getPhizTitleName(){
		return PhizTitleData.getPhizTitleName(phizTitleIndex);
	}
	
	public byte getPhizTitleType(){
		return PhizTitleData.getPhizTitleType(phizTitleIndex);
	}
	
	public boolean hasPhizTitle(short index){
		for(int i = 0;i<phizTitleList.size();i++){
			PhizTitleData tmpPhiz = (PhizTitleData)phizTitleList.get(i);
			if(tmpPhiz.getIndex() == index){
				return true;
			}
		}
		return false;
	}
	
	public void addPhizTitle(short phizIndex){
		if(phizIndex < 0){
			return;
		}
		if(!PhizTitleData.checkPhizTitle(phizIndex)){
			return;
		}
		if(hasPhizTitle(phizIndex)){
			return; //已经有该表情了
		}
		PhizTitleData tmpPhiz = PhizTitleData.getPhizTitle(phizIndex);
		PhizTitleData newPhiz = new PhizTitleData(tmpPhiz.getIndex(),tmpPhiz.getType(),tmpPhiz.getName());
		if(phizTitleList!=null){
			if(phizTitleList.size()>0){
				phizTitleList.add(0, newPhiz);
			}else{
				phizTitleList.add(newPhiz);
			}
		}
	}
	
	public HashMap getPhizTitleMap(){
		HashMap<Short,Byte> phizMap = new HashMap<Short,Byte>();
		for(int i=0;i<phizTitleList.size();i++){
			PhizTitleData tmpPhiz = phizTitleList.get(i);
			phizMap.put(tmpPhiz.getIndex(), tmpPhiz.getType());
		}
		return phizMap;
	}
	
	public int[] getAbilitiesId() {
        int[] ret = new int[abilities.size()];
        for (int i = 0; i < abilities.size(); i++) {
            Ability ability = (Ability) abilities.get(i);
            ret[i] = ability.getId();
        }
        return ret;
    }

    public boolean containsAbility(Ability ability) {
        for (int i = 0; i < abilities.size(); i++) {
            Ability a = (Ability) abilities.get(i);
            if (a.getId() == ability.getId())
                return true;
        }
        return false;
    }


    public boolean containsRecipe(Recipe recipe) {
        for (int i = 0; i < recipes.size(); i++) {
            Recipe r = (Recipe) recipes.get(i);
            if (r.getId() == recipe.getId()) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(List items, int item, int count) {
        for (int i = 0; i < items.size(); i++) {
            Grid g = (Grid) items.get(i);
            if (g.item.getItemId() == item) {
                if (g.count >= count)
                    return true;
                return false;
            }
        }
        return false;
    }

    public boolean containsOR(List<TemplateGrid[]> grids){
    	for(TemplateGrid[] tgrid : grids){
	    	int c = 0;
	    	int total = tgrid[0].count;
	    	boolean found = false;
	    	
	    	for(int i = 0; i < tgrid.length; i++){
	    		int itemId = tgrid[i].template.getItemId();
	    		byte type = tgrid[i].template.getType();
	    		
	    		switch(type){
		    		case IItem.TYPE_BASIC:{
		    			for (int j = 0; j < basicItems.size(); j++) {
		    	            Grid g = (Grid) basicItems.get(j);
		    	            
		    	            if (g.item.getItemId() == itemId) {
		    	            	c += g.count;
		    	            	
		    	            	if(c >= total){
		    	            		found = true;
		    	            		break;
		    	            	}
		    	            }
		    	        }
		    		}
		    			break;
		    		case IItem.TYPE_EXTENDED:{
		    			for (int j = 0; j < extendedItems.size(); j++) {
		    	            Grid g = (Grid) extendedItems.get(j);
		    	            
		    	            if (g.item.getItemId() == itemId) {
		    	            	c += g.count;
		    	            	
		    	            	if(c >= total){
		    	            		found = true;
		    	            		break;
		    	            	}
		    	            }
		    	        }
		    		}
		    			break;
		    		case IItem.TYPE_EQU:{
		    			for (int j = 0; j < equipments.size(); j++) {
		    	            Grid g = (Grid) equipments.get(j);
		    	            
		    	            if (g.item.getItemId() == itemId) {
		    	            	c++;
		    	            	
		    	            	if(c >= total){
		    	            		found = true;
		    	            		break;
		    	            	}
		    	            }
		    	        }
		    		}
		    			break;
		    		case IItem.TYPE_TASK:{
		    			for (int j = 0; j < taskItems.size(); j++) {
		    	            Grid g = (Grid) taskItems.get(j);
		    	            
		    	            if (g.item.getItemId() == itemId) {
		    	            	c += g.count;
		    	            	
		    	            	if(c >= total){
		    	            		found = true;
		    	            		break;
		    	            	}
		    	            }
		    	        }
		    		}
		    			break;
	    		}
	    	}
	    	
	    	if(!found){
	    		return false;
	    	}
    	}
    	
    	return true;
    }

    public boolean contains(Grid grid) {
        IItem item = grid.item;
        int count = grid.count;
        byte type = item.getType();
        if (type == IItem.TYPE_BASIC) {
            return contains(basicItems, item.getItemId(), count);
        } else if (type == IItem.TYPE_EXTENDED) {
            return contains(extendedItems, item.getItemId(), count);
        } else if (type == IItem.TYPE_TASK) {
            return contains(taskItems, item.getItemId(), count);
        } else if (type == IItem.TYPE_EQU) {
            for (int i = 0; i < equipments.size(); i++) {
                Grid g = (Grid) equipments.get(i);
                if (g.item.getItemId() == item.getItemId()) {
                    if (g.item.getId() == item.getId())
                        return true;
                    continue;
                }
            }
        }
        return false;
    }

    public boolean contains(TemplateGrid grid) {
        int itemId = grid.template.getItemId();
        byte type = grid.template.getType();
        int count = grid.count;
        if (type == IItem.TYPE_BASIC) {
            return contains(basicItems, itemId, count);
        } else if (type == IItem.TYPE_EXTENDED) {
            return contains(extendedItems, itemId, count);
        } else if (type == IItem.TYPE_TASK) {
            return contains(taskItems, itemId, count);
        } else if (type == IItem.TYPE_EQU) {
            int c = 0;
            for (int i = 0; i < equipments.size(); i++) {
                Grid g = (Grid) equipments.get(i);
                if (g.item.getItemId() == itemId) {
                    c++;
                    if(c==count)
                        return true;
                }
            }
        }
        return false;
    }

    public boolean contains(TemplateGrid[] grids) {
        for (int i = 0; i < grids.length; i++) {
            if (!contains(grids[i]))
                return false;
        }
        return true;
    }


    public boolean contains(Grid[] grids) {
        for (int i = 0; i < grids.length; i++) {
            if (!contains(grids[i]))
                return false;
        }
        return true;
    }

    private int getReleaseGrid(byte type, int itemId, int count) {
        if (type == IItem.TYPE_BASIC) {
            for (int i = 0; i < basicItems.size(); i++) {
                Grid grid = (Grid) basicItems.get(i);
                if (grid.item.getItemId() == itemId) {
                    if ((grid.count - count) <= 0)
                        return 1;
                    else
                        return 0;
                }
            }
            return 0;
        } else if (type == IItem.TYPE_EXTENDED) {
            for (int i = 0; i < extendedItems.size(); i++) {
                Grid grid = (Grid) extendedItems.get(i);
                if (grid.item.getItemId() == itemId) {
                    if ((grid.count - count) <= 0)
                        return 1;
                    else
                        return 0;
                }
            }
            return 0;
        } else if (type == IItem.TYPE_TASK) {
            for (int i = 0; i < taskItems.size(); i++) {
                Grid grid = (Grid) taskItems.get(i);
                if (grid.item.getItemId() == itemId) {
                    if ((grid.count - count) <= 0)
                        return 1;
                    else
                        return 0;
                }
            }
            return 0;
        } else if (type == IItem.TYPE_EQU)
            return 1;
        return 0;
    }

    private int getNeedGrid(byte type, int itemId, int count) {
        if (type == IItem.TYPE_BASIC) {
            for (int i = 0; i < basicItems.size(); i++) {
                Grid grid = (Grid) basicItems.get(i);
                if (grid.item.getItemId() == itemId) {
                    if ((grid.count + count) <= 99)
                        return 0;
                    else
                        return -1;
                }
            }
            return 1;
        } else if (type == IItem.TYPE_EXTENDED) {
            for (int i = 0; i < extendedItems.size(); i++) {
                Grid grid = (Grid) extendedItems.get(i);
                if (grid.item.getItemId() == itemId) {
                    if ((grid.count + count) <= 99)
                        return 0;
                    else
                        return -1;
                }
            }
            return 1;
        } else if (type == IItem.TYPE_TASK) {
            for (int i = 0; i < taskItems.size(); i++) {
                Grid grid = (Grid) taskItems.get(i);
                if (grid.item.getItemId() == itemId) {
                    if ((grid.count + count) <= ((TaskItem) grid.item).getMax())
                        return 0;
                    else
                        return -1;
                }
            }
            return 1;
        } else if (type == IItem.TYPE_EQU)
            return 1;
        return 0;
    }

    private int getNeedGrid(Grid g) {
        return getNeedGrid(g.item.getType(), g.item.getItemId(), g.count);
    }


    public boolean isOver(Grid[] grids) {
        int total = 0;
        for (int i = 0; i < grids.length; i++) {
            int n = getNeedGrid(grids[i]);
            if (n == -1)
                return true;
            else
                total += n;
        }
        return total > (getAllGridSize() - getCurrentGridSize());
    }

    public boolean isReallyOver(TemplateGrid[] addGrids, TemplateGrid[] removeGrids) {
        int total = 0;
        for(int i = 0; i < removeGrids.length; i++){
            total -= getReleaseGrid(removeGrids[i].template.getType(),
                            removeGrids[i].template.getItemId(), removeGrids[i].count);
        }

        for (int i = 0; i < addGrids.length; i++) {
            int n = getNeedGrid(addGrids[i].template.getType(),
                            addGrids[i].template.getItemId(), addGrids[i].count);
            if (n == -1)
                return true;
            else
                total += n;
        }
        return total > (getAllGridSize() - getCurrentGridSize());
    }

    public boolean isOver(TemplateGrid[] grids) {
        int total = 0;
        for (int i = 0; i < grids.length; i++) {
            int n = getNeedGrid(grids[i].template.getType(),
                                grids[i].template.getItemId(), grids[i].count);
            if (n == -1)
                return true;
            else
                total += n;
        }
        return total > (getAllGridSize() - getCurrentGridSize());
    }

    public void addRecipe(Recipe recipe) {
        recipes.add(recipe);
    }

    public Recipe[] getRecipes(byte type) {
        List l = new ArrayList();
        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = (Recipe) recipes.get(i);
            if (recipe.getType() == type) {
                l.add(recipe);
            }
        }
        Recipe[] ret = new Recipe[l.size()];
        l.toArray(ret);
        return ret;
    }
    
    public void addPrescription(Prescription prescription) {
        playerPrescriptions.add(prescription);
    }

    /**
     * 玩家身上某类型所有的配方
     * @return
     */
    public Prescription[] getPlayerPrescription(int equType){
    	 List l = new ArrayList();
         for (int i = playerPrescriptions.size() - 1; i >= 0; i--) {
        	 Prescription prescription = (Prescription) playerPrescriptions.get(i);
             if (prescription.getEquType() == equType) {
                 l.add(prescription);
             }
         }
         Prescription[] ret = new Prescription[l.size()];
         l.toArray(ret);
         return ret;
    }
    
    /**
     * 玩家身上所有的配方
     * @return
     */
    public Prescription[] getPlayerPrescriptionAll(){
   	 List l = new ArrayList();
        for (int i = 0; i < playerPrescriptions.size(); i++) {
   	 		Prescription prescription = (Prescription) playerPrescriptions.get(i);
            l.add(prescription);
        }
        Prescription[] ret = new Prescription[l.size()];
        l.toArray(ret);
        return ret;
   }
    
    public boolean changedEquipment (int[] equs, Changed changed) {
        if (equs.length != 18) {
        	return false;
        }
        Grid[] oldUsedEquipments = new Grid[usedEquipments.length];
        System.arraycopy(usedEquipments, 0, oldUsedEquipments, 0,
                         usedEquipments.length);
        List oldEquipments = new ArrayList(equipments);
        boolean ret = true;
        for (int i = 0; i < 9; i++) {
            int itemID = equs[i * 2];
            int id = equs[i * 2 + 1];
            if (itemID != -1) {
                IEquipment e = getEquipment(itemID,id);
                  //暂时屏蔽用于vip系统
                if(getVipNewLevel() > 0){
                }else{
                	if (e != null && e.getRequiredLevel() > getLevel()) {	//如果是新换的，并且等级超出了就返回错误
                		return false;
                	}
                }
                if ((usedEquipments[i] != null 
                		&& (usedEquipments[i].item.getItemId() != itemID || usedEquipments[i].item.getId() != id))
                			&& getEquipment(itemID, id) == null
                				&& (itemID == -1 && usedEquipments[i] != null && isFull())) {	//因为装备可能是已经装备上的，所以getEquipment可能取不到
                    return false;
                }
            }
        }
        for (int i = 0; i < 9; i++) {
            int itemID = equs[i * 2];
            int id = equs[i * 2 + 1];
            if (itemID == -1) {
                if (usedEquipments[i] == null) {
                	continue;
                } else {
            		equipments.add(usedEquipments[i]);
            		IEquipment e = (IEquipment) usedEquipments[i].item;
            		usedEquipments[i] = null;
            		if(changed != null && e.isGrow()){
            			changed.updatEquipmentProperty(e, (byte)-1, -1);
            		}
                }
            } else {
                if (usedEquipments[i] == null) {
                    Grid grid = removeEquipment(itemID, id);
                    if(grid == null){
                    	log.info("changedEquipment removeEquipment return null playerID[" + this.getId() + "] itemID[" + itemID + "] id[" + id + "]");
                    	break;
                    }
                    IEquipment e = (IEquipment) grid.item;
                      //暂时屏蔽用于vip系统
                    if(getVipNewLevel() > 0){
                    }else{
                    	if (e.getRequiredLevel() > getLevel()) {
                    		ret = false;
                    		break;
                    	}
                    }
                    if (e.getBindType() == 1) {
                        if (!e.isBinded()) {
                            e.setBinded(true);
                            if (changed != null) {
                            	changed.addBinded(e);
                            }
                        }
                    }
                    usedEquipments[i] = grid;
                    if(changed != null && e.isGrow()){
                    	changed.updatEquipmentProperty(e, (byte)-1, player.getLevel());
                    }
                } else {
                    Grid old = usedEquipments[i];
                    IEquipment oldEqu = (IEquipment) old.item;
                    if (oldEqu.getItemId() == itemID && oldEqu.getId() == id) { //没有更换
                        continue;
                    } else {
                        Grid grid = removeEquipment(itemID, id);
                        if (grid != null) {
                        	equipments.add(usedEquipments[i]);
                        	usedEquipments[i] = grid;
                        	IEquipment e = (IEquipment) grid.item;
                        	if (e.getBindType() == 1) {
                        		if (!e.isBinded()) {
                        			e.setBinded(true);
                        			if (changed != null) {
                        				changed.addBinded(e);
                        			}
                        		}
                        	}
                        	if(changed != null && oldEqu.isGrow()){
                        		changed.updatEquipmentProperty(oldEqu, (byte)-1, -1);
                        	}
                        	if(changed != null && e.isGrow()){
                        		changed.updatEquipmentProperty(e, (byte)-1, player.getLevel());
                        	}
                        } else {
                        	return false;
                        }
                    }
                }
            }
        }
        if (ret == false) {
            usedEquipments = oldUsedEquipments;
            equipments = oldEquipments;
        }
        return ret;
    }

    public boolean changedPetEquipment (int[] equs, int petId, Changed changed) {
        if (equs.length != 18) {
        	return false;
        }
        Pet mypet = getPet(petId);
        if (mypet == null) return false;
        Grid[] oldpetUsedEquipments = new Grid[mypet.getUsedEquipments().length];
        Grid[] petusedEquipments = mypet.getUsedEquipments();
        
        System.arraycopy(petusedEquipments, 0, oldpetUsedEquipments, 0,
        		petusedEquipments.length);
       
        List oldEquipments = new ArrayList(equipments);
        boolean ret = true;
        for (int i = 0; i < 9; i++) {
            int itemID = equs[i * 2];
            int id = equs[i * 2 + 1];
            if (itemID != -1) {
                IEquipment e = getEquipment(itemID,id);
                if (e != null && e.getRequiredLevel() > mypet.getLevel()) {	//如果是新换的，并且等级超出了就返回错误
                	return false;
                } 
                if ((petusedEquipments[i] != null
                		&& (petusedEquipments[i].item.getItemId() != itemID || petusedEquipments[i].item.getId() != id))
                			&& getEquipment(itemID, id) == null
                				&& (itemID == -1 && petusedEquipments[i] != null && isFull())) { //因为装备可能是已经装备上的，所以getEquipment可能取不到
                	return false;
                }
                if (mypet.getUsedEquipmentinfo()[i] == 2) {
                	return false;
                }
            }
        }
        for (int i = 0; i < 9; i++) {
            int itemID = equs[i * 2];
            int id = equs[i * 2 + 1];
            if (itemID == -1) {
                if (petusedEquipments[i] == null) {
                	continue;
                } else {
                    equipments.add(petusedEquipments[i]);
                    IEquipment oldEqu = (IEquipment)petusedEquipments[i].item;
                    petusedEquipments[i] = null;
                    mypet.setUsedEquipmentsinfo(i, (byte)0);
                    if(oldEqu.isGrow()){
                    	changed.updatEquipmentProperty(oldEqu, (byte)-1, -1);
                    }
                }
            } else {
                if (petusedEquipments[i] == null) {
                    Grid grid = removeEquipment(itemID, id);
                    IEquipment e = (IEquipment) grid.item;
                    if (e.getRequiredLevel() > mypet.getLevel()) {
                        ret = false;
                        break;
                    }
                    if (e.getBindType() == 1) {
                        if (!e.isBinded()) {
                            e.setBinded(true);
                            if (changed!=null) {
                            	changed.addBinded(e);
                            }
                        }
                    }
                    petusedEquipments[i] = grid;
                    mypet.setUsedEquipmentsinfo(i, (byte)1);
                    if(e.isGrow()){
                    	changed.updatEquipmentProperty(e, (byte)-1, mypet.getLevel());
                    }
                } else {
                    Grid old = petusedEquipments[i];
                    IEquipment oldEqu = (IEquipment) old.item;
                    if (oldEqu.getItemId() == itemID && oldEqu.getId() == id) { //没有更换
                        continue;
                    } else {
                        Grid grid = removeEquipment(itemID, id);
                        if (grid != null) {
                        	equipments.add(petusedEquipments[i]);
                        	IEquipment e = (IEquipment) grid.item;
                        	if (e.getBindType() == 1) {
                        		if (!e.isBinded()) {
                        			e.setBinded(true);
                        			if (changed!=null) {
                        				changed.addBinded(e);
                        			}
                        		}
                        	}
                        	IEquipment equ = (IEquipment)petusedEquipments[i].item;
                        	petusedEquipments[i] = grid;
                        	mypet.setUsedEquipmentsinfo(i, (byte)1);
                        	if(e.isGrow()){
                        		changed.updatEquipmentProperty(e, (byte)-1, mypet.getLevel());
                        	}
                        	if(equ.isGrow()){
                        		changed.updatEquipmentProperty(equ, (byte)-1, -1);
                        	}
                        } else {
                        	return false;
                        }
                    }
                }
            }
        }
        mypet.setUsedEquipments(petusedEquipments);
        if (ret) {
	        if (mypet.getHp() > mypet.getMaxHp()){
	        	changed.addPetProperty(mypet, Changed.PET_HP, mypet.getMaxHp());
//	        			mypet.getMaxHp() - mypet.getHp());
	        	mypet.setHp(mypet.getMaxHp());
	        }
	        if (mypet.getMp() > mypet.getMaxMp()){
	        	changed.addPetProperty(mypet, Changed.PET_MP, mypet.getMaxMp());
//	        			mypet.getMaxMp() - mypet.getMp());
	        	mypet.setMp(mypet.getMaxMp());
	        }
    	} else {
    		mypet.setUsedEquipments(oldpetUsedEquipments);
            equipments = oldEquipments;
        }
        return ret;
    }
    
    public Grid removeEquipment(int itemId, int id) {
        for (int i = 0; i < equipments.size(); i++) {
            Grid grid = (Grid) equipments.get(i);
            IEquipment equ = (IEquipment) grid.item;
            if (equ.getItemId() == itemId && equ.getId() == id) {
                equipments.remove(i);
                return grid;
            }
        }
        return null;
    }

    public Grid removeUsedEquipment(int itemId, int id) {
        for (int i = 0; i < usedEquipments.length; i++) {
            Grid grid = usedEquipments[i];
            if (grid != null) {
                if (grid.item.getItemId() == itemId && grid.item.getId() == id) {
                    usedEquipments[i] = null;
                    return grid;
                }

            }
        }
        return null;
    }

    /**
     * @return  阵营任务的物品id
     */
    public int hasCampLoopTask(){
    	int campItemId = 0;
    	ArrayList<LoopTasks> loopTasksList = LoopTasks.getCampLoopTaskIds();
    	if(loopTasksList != null){
    		for(int i = 0; i < loopTasksList.size(); i++){
    			int taskId = loopTasksList.get(i).getTaskid();
    			if(currentTasks.containsKey((short)taskId)){
    				campItemId = loopTasksList.get(i).getCampId();
    				break;
    			}
    		}
    	}
    	return campItemId;
    }
    
  //mengjie modify 重复任务特殊处理
    public boolean hasTask(short taskId) {
        return currentTasks.containsKey(taskId);
    }

    public void taskCompleted(short taskId) {
    	//add 完成时间和周期内完成次数
    	LoopTasks loopTasks = null;
    	Date now = new Date();
    	if (currentTasks.containsKey(taskId)){
    		loopTasks = currentTasks.get(taskId);
    		int loopscount = loopTasks.getLoops();
        	if (loopscount > -1){
        		Date lastfinishdate = loopTasks.getLastfinishtime();
        		Calendar callast = Calendar.getInstance();
        		if (lastfinishdate == null){
        			loopTasks.setLastfinishtime(now);
        			loopTasks.setFinishcount(1);
        		}else{
        			callast.setTime(lastfinishdate);
        			Calendar calnow = Calendar.getInstance();
            		calnow.setTime(now);
            		
            		if ((callast.get(Calendar.DAY_OF_MONTH) == calnow.get(Calendar.DAY_OF_MONTH)) && 
            				(callast.get(Calendar.YEAR) == calnow.get(Calendar.YEAR)) && 
            				(callast.get(Calendar.MONTH) == calnow.get(Calendar.MONTH)) ){
            			//上次完成就是当天。
            			loopTasks.setLastfinishtime(now);
            			loopTasks.setFinishcount(loopTasks.getFinishcount()+1);
            		}else{
            			loopTasks.setLastfinishtime(now);
            			loopTasks.setFinishcount(1);
            		}
        		}
        	}else{
        		loopTasks.setLastfinishtime(now);
        		loopTasks.setFinishcount(1);
        	}
            currentTasks.remove(taskId);
    	}else{
    		//特殊处理，无接到的任务交任务
    		if (LoopTasks.LoopTaskbyTaskid.containsKey(taskId)){//是重复任务
    			LoopTasks loopTaskstmp = LoopTasks.LoopTaskbyTaskid.get(taskId);
    			loopTasks = new LoopTasks(loopTaskstmp.getTaskid(),
						loopTaskstmp.getLoops(),
						loopTaskstmp.getTime(),
						loopTaskstmp.getGroup(), loopTaskstmp.getCampId());
    			loopTasks.setLastfinishtime(now);
    			loopTasks.setFinishcount(1);
    			loopTasks.setPreTask(loopTaskstmp.getPreTask());
    		}else{//不是重复任务
    			loopTasks = new LoopTasks(taskId,
						-1,-1,-1, -1);
    			loopTasks.setLastfinishtime(null);
    			loopTasks.setFinishcount(-1);
    			loopTasks.setPreTask(-1);
    		}
    	}
    	if (!completedTasks.containsKey(taskId)){
    		completedTasks.put(taskId, loopTasks);
    	}else{
    		completedTasks.remove(taskId);
    		completedTasks.put(taskId, loopTasks);
    	}
        removeTaskSave(taskId);
    }

    //mengjie add
    public void removecompletedTask(short taskId) {
    	if (completedTasks.containsKey(taskId)){//完成过
    		completedTasks.remove(taskId);
    	}
    }
    
    public void addTask(short taskId) {
    	
        if (!currentTasks.containsKey(taskId)) {
        	LoopTasks loopTasks = null;
        	Date now = new Date();
        	if (completedTasks.containsKey(taskId)){//完成过
        		LoopTasks loopTaskstmp = LoopTasks.LoopTaskbyTaskid.get(taskId);
        		loopTasks = new LoopTasks(loopTaskstmp.getTaskid(),
						loopTaskstmp.getLoops(),
						loopTaskstmp.getTime(),
						loopTaskstmp.getGroup(), loopTaskstmp.getCampId());
    			loopTasks.setFinishcount(loopTaskstmp.getFinishcount());
        		loopTasks.setLastfinishtime(loopTaskstmp.getLastfinishtime());
        		loopTasks.setPreTask(loopTaskstmp.getPreTask());
        	}else{
        		if (LoopTasks.LoopTaskbyTaskid.containsKey(taskId)){//是重复任务
        			LoopTasks loopTaskstmp = LoopTasks.LoopTaskbyTaskid.get(taskId);
        			loopTasks = new LoopTasks(loopTaskstmp.getTaskid(),
    						loopTaskstmp.getLoops(),
    						loopTaskstmp.getTime(),
    						loopTaskstmp.getGroup(), loopTaskstmp.getCampId());
        			loopTasks.setLastfinishtime(now);
        			loopTasks.setFinishcount(1);
        			loopTasks.setPreTask(loopTaskstmp.getPreTask());
        		}else{//不是重复任务
        			loopTasks = new LoopTasks(taskId,
    						-1,-1,-1, -1);
        			loopTasks.setLastfinishtime(null);
        			loopTasks.setFinishcount(-1);
        			loopTasks.setPreTask(-1);
        		}
        	}
            currentTasks.put(taskId, loopTasks);
        }
    }

    public void resetCurrentTasks() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
          //mengjie modify 20091014 new version 1
            dos.writeShort(0);
            //dos.writeByte(1);//version
            dos.writeByte(taskVersion);
            dos.writeInt(currentTasks.size());
            for (short s:currentTasks.keySet()){
            	dos.writeShort(s);
            	if (currentTasks.get(s).getLastfinishtime() == null){
            		dos.writeLong(-1);
            	}else{
            		dos.writeLong(currentTasks.get(s).getLastfinishtime().getTime());
            	}            	
            	dos.writeByte(currentTasks.get(s).getFinishcount());
        	}
//            for (int i = 0; i < currentTasks.size(); i++) {
//                dos.writeShort(currentTasks.get(i));
//            }
            player.getTaskData().setCurrent(bos.toByteArray());
        } catch (Exception ex) {
            log.error(ex, ex);
        }
    }

    public void resetCompletedTasks() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            //mengjie modify 20091014 new version 1
            dos.writeShort(0);
            //dos.writeByte(1);//version
            dos.writeByte(taskVersion);
            dos.writeInt(completedTasks.size());
            for (short s:completedTasks.keySet()){
            	dos.writeShort(s);
            	if (completedTasks.get(s).getLastfinishtime() == null){
            		dos.writeLong(-1);
            	}else{
            		dos.writeLong(completedTasks.get(s).getLastfinishtime().getTime());
            	}
            	dos.writeByte(completedTasks.get(s).getFinishcount());
        	}
//            for (int i = 0; i < completedTasks.size(); i++) {
//                dos.writeShort(completedTasks.get(i));
//            }
            player.getTaskData().setFinished(bos.toByteArray());
        } catch (Exception ex) {
            log.error(ex, ex);
        }
    }

    public void resetTasks() {
        resetCurrentTasks();
        resetCompletedTasks();
    }

    public short[] getCurrentTasksId() {
    	short[] result = new short[currentTasks.size()];
    	int i = 0;
    	for (short s:currentTasks.keySet()){
    		result[i] = s;
    		i++;
    	}
        return result;
    }

    public short[] getCompletedTasksId() {
    	short[] result = new short[completedTasks.size()];
    	int i = 0;
    	for (short s:completedTasks.keySet()){
    		result[i] = s;
    		i++;
    	}
        return result;
    }

    public short[] getCurrentTasksId(short taskId) {
        short[] ids = TaskDefinitions.getDefinitions(taskId);
        ShortList ret = new ArrayShortList(3);
        for (int i = 0; i < ids.length; i++) {
            if (currentTasks.containsKey(ids[i])) {
                ret.add(ids[i]);
            }
        }
        return ret.toArray();
    }

    public LoopTasks getCurrentTask(short taskId){
    	return currentTasks.get(taskId);
    }
    public boolean removeTask(short taskId) {
        if (currentTasks.containsKey(taskId)) {
        	currentTasks.remove(taskId);
            TaskSaveDataBean taskSaveBean = new TaskSaveDataBean();
            taskSaveBean.updateData(getTaskData());
            taskSaveBean.removeTaskSave(taskId);
            setTaskData(taskSaveBean.getData());
            return true;
        }
        return false;
    }

    public void removeTaskSave(short taskId) {
        TaskSaveDataBean taskSaveBean = new TaskSaveDataBean();
        taskSaveBean.updateData(getTaskData());
        taskSaveBean.removeTaskSave(taskId);
        setTaskData(taskSaveBean.getData());
    }
    
    public boolean hasCompleteTask(short taskId){
    	return completedTasks.containsKey(taskId);
    }
    public boolean hasCurrentTasks(short taskId){
    	return currentTasks.containsKey(taskId);
    }
    public short[] getCompletedTasksId(short taskId) {
        short[] ids = TaskDefinitions.getDefinitions(taskId);
        ShortList ret = new ArrayShortList(3);
        for (int i = 0; i < ids.length; i++) {
            if (completedTasks.containsKey(ids[i])) {
                ret.add(ids[i]);
            }
        }
        return ret.toArray();
    }

    public short[] getCompletedTasksId(short[] tasksId) {
        short[] ids = TaskDefinitions.getDefinitions(tasksId);
        ShortList ret = new ArrayShortList(3);
        for (int i = 0; i < ids.length; i++) {
            if (completedTasks.containsKey(ids[i])) {
                ret.add(ids[i]);
            }
        }
        return ret.toArray();
    }
    public LoopTasks getCompletedLoopTask(short tasksId) {
    	return completedTasks.get(tasksId);
    }
    
    
    //mengjie modify end
    public void removeItem(byte type, int id, int count) {
        if (type == IItem.TYPE_BASIC) {
            for (int i = 0; i < basicItems.size(); i++) {
                Grid grid = (Grid) basicItems.get(i);
                if (grid.item.getItemId() == id) {
                    grid.count -= count;
                    if (grid.count <= 0) {
                        basicItems.remove(i);
                        break;
                    }
                }
            }
        } else if (type == IItem.TYPE_EXTENDED) {
            for (int i = 0; i < extendedItems.size(); i++) {
                Grid grid = (Grid) extendedItems.get(i);
                if (grid.item.getItemId() == id) {
                    grid.count -= count;
                    if (grid.count <= 0) {
                        extendedItems.remove(i);
                        break;
                    }
                }
            }
        } else if (type == IItem.TYPE_EQU) {
            for (int i = 0; i < equipments.size(); i++) {
                Grid grid = (Grid) equipments.get(i);
                if (grid.item.getItemId() == id && grid.item.getId() == count) {
                    equipments.remove(i);
                    break;
                }
            }
        } else if (type == IItem.TYPE_PET) {
            for (int i = 0; i < pets.size(); i++) {
                Pet p = (Pet) pets.get(i);
                if (p.getId() == count) {
                    pets.remove(i);
                    break;
                }
            }
        }
    }

    public void removeTaskItem(String name, int count) {
        for (int i = 0; i < taskItems.size(); i++) {
            Grid grid = (Grid) taskItems.get(i);
            if (grid.item.getName().equals(name)) {
                grid.count -= count;
                if (grid.count <= 0) {
                    taskItems.remove(i);
                    break;
                }
            }
        }
    }


    public IItem completeRemoveItem(int itemId, int count, Changed changed) {
        IItemTemplate item = Items.getTemplate(itemId);
        byte type = item.getType();
        if (type == IItem.TYPE_BASIC) {
            if(count <= 0){
                return null;
            }
            
            for (int i = 0; i < basicItems.size(); i++) {
                Grid grid = (Grid) basicItems.get(i);
                if (grid.item.getItemId() == itemId) {
                    if (grid.count >= count) {
                        grid.count -= count;
                        if (grid.count <= 0) {
                            basicItems.remove(i);
                        }
                        if (changed != null) {
                            changed.addItem(grid.item, -count);
                        }
                        return grid.item;
                    }
                }
            }
        } else if (type == IItem.TYPE_EXTENDED) {
            if(count <= 0){
                return null;
            }
            
            for (int i = 0; i < extendedItems.size(); i++) {
                Grid grid = (Grid) extendedItems.get(i);
                if (grid.item.getItemId() == itemId) {
                    if (grid.count >= count) {
                        grid.count -= count;
                        if (grid.count <= 0) {
                            extendedItems.remove(i);
                        }
                        if (changed != null) {
                            changed.addItem(grid.item, -count);
                        }
                        return grid.item;
                    }
                }
            }
        } else if (type == IItem.TYPE_TASK) {
            if(count <= 0){
                return null;
            }
            
            for (int i = 0; i < taskItems.size(); i++) {
                Grid grid = (Grid) taskItems.get(i);
                if (grid.item.getItemId() == itemId) {
                    if (grid.count >= count) {
                        grid.count -= count;
                        if (grid.count <= 0) {
                            taskItems.remove(i);
                        }
                        if (changed != null) {
                            changed.addItem(grid.item, -count);
                        }
                        return grid.item;
                    }
                }
            }
        } else if (type == IItem.TYPE_EQU) {
            for (int i = 0; i < equipments.size(); i++) {
                Grid grid = (Grid) equipments.get(i);
                if (grid.item.getItemId() == itemId&&(grid.item.getId()==count||count==-1) ) { //生成id相等或者传入的生成id为-1的时候(-1在扣除任务物品的时候需要用到)
                	IEquipment equ = (IEquipment)grid.item;
            		if(equ.getDataVesion() > 0){ 
            			for (byte d = 0; d < Utils.maxHolesEqu; d ++) {
            				DiamondMosaic diamondMosaic = equ.getDiamondMosaicRole(d);
            				if (diamondMosaic != null) {
            					if(DiamondMosaic.getDiamondMosaicLevel(diamondMosaic.getItemId()) >= 3){
            						log.info("playerID[" + getId() + "] Remove EQU has Diamond ID[" + grid.item.getId() + "] name[" + grid.item.getName() + "]" +
            	                			"current version[" + IEquipment.CURRENT_EQU_VERSION + "] ItemData[" + Utils.getHexdump(grid.item.toDbBytes()) + "]"
            	                			);
            						return null;
            					}
            				}
            			}
            		}
                	log.info("playerID[" + getId() + "] Remove EQU ID[" + grid.item.getId() + "] name[" + grid.item.getName() + "]" +
                			"current version[" + IEquipment.CURRENT_EQU_VERSION + "] ItemData[" + Utils.getHexdump(grid.item.toDbBytes()) + "]"
                			);
                    equipments.remove(i);
                    if (changed != null) {
                        changed.addItem(grid.item, -1);
                    }
                    return grid.item;
                }
            }
        } else if (type == IItem.TYPE_PET) {
            for (int i = 0; i < pets.size(); i++) {
                Pet pet = (Pet) pets.get(i);
                if (pet.getId() == itemId) {
                    pets.remove(i);
                    if (changed != null) {
                        changed.addItem(pet, -1);
                    }
                }
            }
        }
        return null;
    }

//    public boolean completeRemoveItem(Grid[] grids, Changed changed) {
//        if (!contains(grids))
//            return true;
//        for (int i = 0; i < grids.length; i++) {
//            completeRemoveItem(grids[i].item, grids[i].count, changed);
//        }
//        return true;
//    }

    public int completeRemoveItemOR(List<TemplateGrid[]> grids, Changed changed){
    	if(!containsOR(grids)){
    		return 0;
    	}
    	boolean delAll = false;
    	int delCount = 0;
    	for(TemplateGrid[] tgrid : grids){
    		if(tgrid[0].count >= 0){
	    		int c = tgrid[0].count;
	    		delCount += c; 
	    		for(int i = 0; i < tgrid.length; i++){
	    			int itemId = tgrid[i].template.getItemId();
	    			int itemType = tgrid[i].template.getType();
	    			
	    			while(c > 0){
		    			if(itemType == IItem.TYPE_EQU){
		    				if(completeRemoveItem(itemId, -1, changed) != null){ //因为取不到生成id，所以传入-1，保证能扣到一个
		    					c--;
		    				}else{
		    					break;
		    				}
		    			}else{
		    				if(completeRemoveItem(itemId, 1, changed) != null){
		    					c--;
		    				}else{
		    					break;
		    				}
		    			}
	    			}
	    			
	    			if(c == 0){
	    				break;
	    			}
	    		}
    		}else{	//兑换全部
    			delAll = true;
    			break;
    		}
    	}
    	if(delAll){
    		int delcount = -1;
    		for(TemplateGrid[] tgrid : grids){
				for(int i = 0; i < tgrid.length; i++){//获取物品个数以及除以百分比后应删个数
					int itemCount = getItemCount(tgrid[i].template.getItemId());
					int hasCount = itemCount / tgrid[i].getPercent();
					if(delcount == -1){
						delcount = hasCount;
					}else{
						if(delcount > hasCount){
							delcount = hasCount;
						}
					}
				}
    		}
    		if(delcount <=0){
    			return -1;
    		}
    		for(TemplateGrid[] tgrid : grids){
				for(int i = 0; i < tgrid.length; i++){
	    			int itemId = tgrid[i].template.getItemId();
	    			int itemDelCount = delcount * tgrid[i].getPercent();
					completeRemoveItem(itemId, itemDelCount, changed);
				}
    		}
    		return delcount;
    	}
    	return 0;
    }

    public boolean completeRemoveItem(TemplateGrid[] grids, Changed changed) {
        if (!contains(grids))
            return true;
        for (int i = 0; i < grids.length; i++) {
            if(grids[i].template.getType()==IItem.TYPE_EQU){
                for (int j = 0; j < grids[i].count; j++)
                    completeRemoveItem(grids[i].template.getItemId(), -1,
                                       changed); //因为取不到生成id，所以传入-1，保证能扣到一个
            }else{
                completeRemoveItem(grids[i].template.getItemId(), grids[i].count,
                                   changed);
            }
        }
        return true;
    }

    public boolean hasItem(int itemId){
        Grid grid = getItem(itemId,0);
        if(grid==null)
            return false;
        return grid.count>0;
    }

    public boolean hasItem(int itemId,int count){
        Grid grid = getItem(itemId,0);
        if(grid==null)
            return false;
        return grid.count>=count;
    }
    //jwp add 添加宠我一生
    public int  getItemCount(int itemId){
        Grid grid = getItem(itemId,0);
        if(grid==null)
            return 0;
        return grid.count;
    }
    //这里的包括装备
    public int  getItemCount2(int itemId){
        Grid grid = getItem(itemId,-1);
        if(grid==null)
            return 0;
        return grid.count;
    }
    //套装升级使用
    
    /**
     * @param needGrids
     * @return 返回是否有可升级的装备
     */
    public boolean hasUpgradeEqu(int itemId){
    	boolean flag = false;
    	for (int k = 0; k < equipments.size(); k++) {
    		Grid grid = (Grid) equipments.get(k);
            if (grid.item.getItemId() == itemId){
            	flag = true;
  			  	break;
            }
        }

    	return flag;
    }
    
    /**
     * @param instanceId
     * @return根据实例id返回装备栏，包括背包内和身上装备的，只有装备有效
     */
    public Grid getEquipmentByInstanceid(int instanceId){
    	Grid iequ = null;
    	for (int i = 0; i < equipments.size(); i++) {
    		Grid grid = (Grid) equipments.get(i);
            if (grid.item.getId() == instanceId){
            	return grid;
            }
        }
    	
    	for (int i = 0; i < usedEquipments.length; i++) {
    		Grid grid = usedEquipments[i];
    		if(grid != null){
    			if (grid.item.getId() == instanceId){
                	return grid;
                }
    		}
        }
    	return iequ;
    }
    //jwp add end
    //instanceId只有在物品位装备时才有效
    public Grid getItem(int itemId, int instanceId) {
        IItemTemplate template = Items.getTemplate(itemId);
        return getItem(template, instanceId);
    }
    
    public Grid getItem(IItemTemplate template, int instanceId) {
        byte type = template.getType();
        if (type == IItem.TYPE_BASIC) {
            for (int i = 0; i < basicItems.size(); i++) {
                Grid grid = (Grid) basicItems.get(i);
                if (grid.item.getItemId() == template.getItemId())
                    return grid;
            }
        } else if (type == IItem.TYPE_EXTENDED) {
            for (int i = 0; i < extendedItems.size(); i++) {
                Grid grid = (Grid) extendedItems.get(i);
                if (grid.item.getItemId() == template.getItemId())
                    return grid;
            }
        } else if (type == IItem.TYPE_TASK) {
            for (int i = 0; i < taskItems.size(); i++) {
                Grid grid = (Grid) taskItems.get(i);
                if (grid.item.getItemId() == template.getItemId())
                    return grid;
            }
        } else if (type == IItem.TYPE_EQU) {
            for (int i = 0; i < equipments.size(); i++) {
                Grid grid = (Grid) equipments.get(i);
                if (grid.item.getItemId() == template.getItemId() &&
                    (grid.item.getId() == instanceId||instanceId==-1))
                    return grid;
            }
        }
        return null;
    }

//    public Grid getItem(IItem item){
//        byte type = item.getType();
//        if(type==IItem.TYPE_BASIC){
//            for(int i=0;i<basicItems.size();i++){
//                Grid grid = (Grid)basicItems.get(i);
//                if(grid.item.getItemId()==item.getItemId())
//                    return grid;
//            }
//        }
//        else if(type==IItem.TYPE_EXTENDED){
//            for(int i=0;i<extendedItems.size();i++){
//                Grid grid = (Grid)extendedItems.get(i);
//                if(grid.item.getItemId()==item.getItemId())
//                    return grid;
//            }
//        }
//        else if(type==IItem.TYPE_TASK){
//            for(int i=0;i<taskItems.size();i++){
//                Grid grid = (Grid)taskItems.get(i);
//                if(grid.item.getItemId()==item.getItemId())
//                    return grid;
//            }
//        }
//        else if(type==IItem.TYPE_EQU){
//            for(int i=0;i<equipments.size();i++){
//                Grid grid = (Grid)equipments.get(i);
//                if(grid.item.getItemId()==item.getItemId())
//                    return grid;
//            }
//        }
//        return null;
//    }

    /**
     * @param item
     * @param count  如果是装备的话，这里代表装备的实例id
     * @param changed
     * @return 
     */
    public IItem completeRemoveItem(IItem item, int count, Changed changed) {
        return completeRemoveItem(item.getItemId(), count, changed);
    }

    public boolean hasItem(IItem item, int count) {
        byte type = item.getType();
        if (type == IItem.TYPE_BASIC) {
            for (int i = 0; i < basicItems.size(); i++) {
                Grid grid = (Grid) basicItems.get(i);
                if (grid.item.getItemId() == item.getItemId()) {
                    if (grid.count >= count) {
                        return true;
                    }
                }
            }
        } else if (type == IItem.TYPE_EXTENDED) {
            for (int i = 0; i < extendedItems.size(); i++) {
                Grid grid = (Grid) extendedItems.get(i);
                if (grid.item.getItemId() == item.getItemId()) {
                    if (grid.count >= count) {
                        return true;
                    }
                }
            }
        } else if (type == IItem.TYPE_TASK) {
            for (int i = 0; i < taskItems.size(); i++) {
                Grid grid = (Grid) taskItems.get(i);
                if (grid.item.getItemId() == item.getItemId()) {
                    if (grid.count >= count) {
                        return true;
                    }
                }
            }
        } else if (type == IItem.TYPE_EQU) {
            for (int i = 0; i < equipments.size(); i++) {
                Grid grid = (Grid) equipments.get(i);
                if (grid.item.getItemId() == item.getItemId() &&
                    grid.item.getId() == count) {
                    return true;
                }
            }
        } else if (type == IItem.TYPE_PET) {
            for (int i = 0; i < pets.size(); i++) {
                Pet p = (Pet) pets.get(i);
                if (p.getId() == count)
                    return true;
            }
        }
        return false;
    }

//    public IEquipment getEquipmentInBag(int itemId,int id){
//        for (int i = 0; i < equipments.size(); i++) {
//            Grid grid = (Grid) equipments.get(i);
//            if (grid.item.getItemId() == itemId &&
//                grid.item.getId() == id) {
//                return (IEquipment)grid.item;
//            }
//        }
//        return null;
//    }



    public void resetChatOptions() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            for (int i = 0; i < chatOptions.length; i++) {
                dos.writeByte(chatOptions[i].pri);
                dos.writeByte(chatOptions[i].color);
            }
            dos.writeInt(favoriteId);
            player.setChatOptions(bos.toByteArray());
        } catch (Exception ex) {
            log.error(ex, ex);
        }
    }

    public void setChatOptions(ChatOption[] chatOptions) {
        this.chatOptions = chatOptions;
    }

    public ChatOption[] getChatOptions() {
        return chatOptions;
    }

    public int getChatFavoriteId() {
        return favoriteId;
    }

    public void setChatFavoriteId(int favoriteId) {
        this.favoriteId = favoriteId;
    }

    public boolean inMapChannel() {
        return chatOptions[ChatOption.MAP].pri != 0;
    }

    public boolean inWorldChannel() {
        return chatOptions[ChatOption.WORLD].pri != 0;
    }

    public boolean inFavoriteChannel() {
        return chatOptions[ChatOption.FAVORITE].pri != 0;
    }

    private static final Buf[] EMPTY_BUFS = new Buf[0];

    public Buf[] getBufs() {
        if (bufs.size() == 0 && extendedBufs.size() == 0 && campBattleBuffs.size() == 0)
            return EMPTY_BUFS;
        Buf[] ret = new Buf[bufs.size() + extendedBufs.size() + campBattleBuffs.size()];
        List l = new ArrayList(bufs.size() + extendedBufs.size() + campBattleBuffs.size());
        l.addAll(bufs);
        l.addAll(extendedBufs);
        l.addAll(campBattleBuffs);
        l.toArray(ret);
        return ret;
    }

    public Buf[] getPropertyBufs() {
        if (bufs.size() == 0 && campBattleBuffs.size() == 0) {
        	return new Buf[0];
        }
        List l = new ArrayList(bufs.size() + campBattleBuffs.size());
        for (int i = 0; i < bufs.size(); i ++) {
            Buf buf = (Buf)bufs.get(i);
            if (buf.getProperty() > 0)
                l.add(buf);
        }
        for (int i = 0; i < campBattleBuffs.size(); i ++) {
        	Buf buf = (Buf) campBattleBuffs.get(i);
        	if (buf.getProperty() > 0) {
        		l.add(buf);
        	}
        }
        Buf[] ret = new Buf[l.size()];
        l.toArray(ret);
        return ret;
    }

    
    /**
     * 只在第一次登陆的时候调用，修复登陆后血蓝不满的BUG
     * @param buf
     * @param change
     */
    public void addBufOnce (Buf buf, Changed change) {
        if (buf.getTime() == 0) {
            return;
        }
        boolean b = false;
        if (buf.getProperty() < 0) {  //扩展buf,相同的替换，不同的加到末尾，没有个数限制，并且不会通知客户端
        	if (buf.getUnit() >= Buf.CAMP_BATTLE_BUFF_MARK) {
        		addTypeExtendBuff(campBattleBuffs, buf);
        	} else {
        		addTypeExtendBuff(extendedBufs, buf);
        		if(getPet() != null){
        			getPet().setPetExtendBuff(extendedBufs);
        		}
        	}
        } else {  //属性buf,相同的替换，不同的加到末尾，但是有个数限制，会通知客户端buf状态改变
        	if (buf.getUnit() >= Buf.CAMP_BATTLE_BUFF_MARK) {
        		addTypeBasicBuff(campBattleBuffs, buf, change);
        	} else {
        		addTypeBasicBuff(bufs, buf, change);
        	}
        }
    }
    
    public void addBuf(Buf buf, Changed change) {
        if (buf.getTime() == 0) {
            return;
        }
        boolean b = false;
        if (buf.getProperty() < 0) {  //扩展buf,相同的替换，不同的加到末尾，没有个数限制，并且不会通知客户端
        	if (buf.getUnit() >= Buf.CAMP_BATTLE_BUFF_MARK) {
        		addTypeExtendBuff(campBattleBuffs, buf);
        	} else {
        		addTypeExtendBuff(extendedBufs, buf);
        		if(getPet() != null){
        			getPet().setPetExtendBuff(extendedBufs);
        		}
        	}
        } else {  //属性buf,相同的替换，不同的加到末尾，但是有个数限制，会通知客户端buf状态改变
        	if (buf.getUnit() >= Buf.CAMP_BATTLE_BUFF_MARK) {
        		addTypeBasicBuff(campBattleBuffs, buf, change);
        	} else {
        		addTypeBasicBuff(bufs, buf, change);
        	}
        }
        if (buf.getProperty() > 0)
            adjustProperty();
    }
    
    /**
     * 添加不同类型的扩展BUFF
     * @param list
     * @param buff
     */
    public void addTypeExtendBuff (List list, Buf buff) {
    	boolean mark = false;
    	for (int i = 0;i < list.size(); i++) {
            Buf aBuf = (Buf)list.get(i);
            if (aBuf.getProperty() == buff.getProperty()) {
            	list.set(i,buff);
                mark = true;
                break;
            }
        }
        if(!mark){
        	list.add(buff);
        }
    }
    
    public void addTypeBasicBuff (List list, Buf buff, Changed change) {
    	boolean mark = false;
    	for (int i = 0; i < list.size(); i++) {
            Buf aBuf = (Buf) list.get(i);
            if (aBuf.getProperty() == buff.getProperty()) {
                Buf tmp = aBuf;
                list.set(i, buff);
                if (change != null ) {
                    change.addBuf(buff);
                    change.addRemovedBuff(tmp);
                }
                mark = true;
                break;
            }
        }
        if (!mark) {
        	if (buff.getUnit() >= Buf.CAMP_BATTLE_BUFF_MARK) {
        		list.add(buff);
    			if (change != null) {
    				change.addBuf(buff);
    			}
        	} else {
        		//if (list.size() < 3) {
        			list.add(buff);
        			if (change != null)
        				change.addBuf(buff);
//        		} else {
//        			for (int i = 0; i < list.size(); i++) {
//        				Buf tmp = (Buf) list.get(i);
//        				if (tmp.getProperty() > 0) {
//        					list.set(i, buff);
//        					if (change != null) {
//        						if (buff.getProperty() > 0)
//        							change.addBuf(buff);
//        						if (tmp.getProperty() > 0)
//        							change.addRemovedBuff(tmp);
//        					}
//        					break;
//        				}
//        			}
//        		}
        	}
        }
    }
    
    public void addPropertySuitEffect (SuitEffect se) {
    	if (se.getType() >= SuitEffect.EFFECT_TYPE_ADD_INTE && se.getType() <= SuitEffect.EFFECT_TYPE_ADD_STR) {
    		suitEffect.add(se);
    	}else if(se.getType() == SuitEffect.EFFECT_TYPE_ADD_DIAMOND){
    		suitEffect.add(se);
    	}
    }
    
    public List getPropertySuitEffect () {
    	return suitEffect;
    }
    
    // 获得套装带来的属性加成
    public int[] getSuitEffectPropertyPoint () {
    	ArrayList tmpSuitEffect = (ArrayList) getPropertySuitEffect();
    	if (tmpSuitEffect != null && tmpSuitEffect.size() > 0) {
    		int[] ret = new int[4];
    		for (int i = 0; i < tmpSuitEffect.size(); i++) {
    			SuitEffect se = (SuitEffect) tmpSuitEffect.get(i);
    			switch (-se.getType()) {
    			case Changed.STRENGTH:
    				ret[0] += se.getValue();
    				break;
    			case Changed.AGILITY:
    				ret[1] += se.getValue();
    				break;
    			case Changed.VITALITY:
    				ret[2] += se.getValue();
    				break;
    			case Changed.INTELLIGENCE:
    				ret[3] += se.getValue();
    				break;
    			}
    		}
    		return ret;
    	} else {
    		return null;
    	}
    }
    
    
    public void addDiamondShineBuf(int[] level){
    	if(diamondShineBufs != null){
    		diamondShineBufs.clear();
    	}
        for(int i =5;i<= level[0];i++){
        	switch(i){
        		case 5:		//提高物攻魔攻
        			DiamondShineBuf dsBuf5_1 = new DiamondShineBuf(-1, DiamondShineBuf.PHYSIC_ATTC, 2, -1, DiamondShineBuf.UNIT_DIAMONDSHINE);
        			calcDiamondShineBuf(dsBuf5_1);
        			DiamondShineBuf dsBuf5_2 = new DiamondShineBuf(-1, DiamondShineBuf.MAGIC_ATTC, 2, -1, DiamondShineBuf.UNIT_DIAMONDSHINE);
        			calcDiamondShineBuf(dsBuf5_2);
        			break;
        		case 6:		//提高物攻魔攻
        			DiamondShineBuf dsBuf6_1 = new DiamondShineBuf(-1, DiamondShineBuf.PHYSIC_ATTC, 2, -1, DiamondShineBuf.UNIT_DIAMONDSHINE);
        			calcDiamondShineBuf(dsBuf6_1);
        			DiamondShineBuf dsBuf6_2 = new DiamondShineBuf(-1, DiamondShineBuf.MAGIC_ATTC, 2, -1, DiamondShineBuf.UNIT_DIAMONDSHINE);
        			calcDiamondShineBuf(dsBuf6_2);
        			break;
        		case 7:		//提高免爆率
        			DiamondShineBuf dsBuf7 = new DiamondShineBuf(-1, DiamondShineBuf.NOCRI, 2, -1, DiamondShineBuf.UNIT_DIAMONDSHINE);
        			calcDiamondShineBuf(dsBuf7);
        			break;
        		case 8:		//提高免爆率
        			DiamondShineBuf dsBuf8 = new DiamondShineBuf(-1, DiamondShineBuf.NOCRI, 2, -1, DiamondShineBuf.UNIT_DIAMONDSHINE);
        			calcDiamondShineBuf(dsBuf8);
        			break;
        		case 9:		//提高暴击率
        			DiamondShineBuf dsBuf9_1 = new DiamondShineBuf(-1, DiamondShineBuf.PHYSIC_CRI, 2, -1, DiamondShineBuf.UNIT_DIAMONDSHINE);
        			calcDiamondShineBuf(dsBuf9_1);
        			DiamondShineBuf dsBuf9_2 = new DiamondShineBuf(-1, DiamondShineBuf.MAGIC_CRI, 2, -1, DiamondShineBuf.UNIT_DIAMONDSHINE);
        			calcDiamondShineBuf(dsBuf9_2);
        			break;
        		case 10:	//提高敏捷
        			DiamondShineBuf dsBuf10 = new DiamondShineBuf(-1, DiamondShineBuf.AGI, 5, -1, DiamondShineBuf.UNIT_DIAMONDSHINE);
        			calcDiamondShineBuf(dsBuf10);
        			break;
        		case 11:	//提高力量和智力
        			DiamondShineBuf dsBuf11_1 = new DiamondShineBuf(-1, DiamondShineBuf.STR, 4, -1, DiamondShineBuf.UNIT_DIAMONDSHINE);
        			calcDiamondShineBuf(dsBuf11_1);
        			DiamondShineBuf dsBuf11_2 = new DiamondShineBuf(-1, DiamondShineBuf.INT, 4, -1, DiamondShineBuf.UNIT_DIAMONDSHINE);
        			calcDiamondShineBuf(dsBuf11_2);
        			break;
        		case 12:		//提高hp/mp和暴击
        			DiamondShineBuf dsBuf12_1 = new DiamondShineBuf(-1, DiamondShineBuf.ADD_MPMAX, 10, -1, DiamondShineBuf.UNIT_DIAMONDSHINE);
        			calcDiamondShineBuf(dsBuf12_1);
        			DiamondShineBuf dsBuf12_2 = new DiamondShineBuf(-1, DiamondShineBuf.ADD_HPMAX, 10, -1, DiamondShineBuf.UNIT_DIAMONDSHINE);
        			calcDiamondShineBuf(dsBuf12_2);
        			DiamondShineBuf dsBuf12_3 = new DiamondShineBuf(-1, DiamondShineBuf.SERVER_PHYSIC_CRI, 10, -1, DiamondShineBuf.UNIT_DIAMONDSHINE);
        			calcDiamondShineBuf(dsBuf12_3);
        			DiamondShineBuf dsBuf12_4 = new DiamondShineBuf(-1, DiamondShineBuf.SERVER_MAGIC_CRI, 10, -1, DiamondShineBuf.UNIT_DIAMONDSHINE);
        			calcDiamondShineBuf(dsBuf12_4);
        			break;
        	}
        }
        if(level.length > 2 && level[2] >= 3){
        	int value = level[2] * level[2] * 5;
        	for(int i = DiamondShineBuf.STR_VALUE; i <= DiamondShineBuf.INT_VALUE; i++){
        		DiamondShineBuf dsBuf = new DiamondShineBuf(-1, (byte)i, value, -1, DiamondShineBuf.UNIT_DIAMONDSHINE);
    			calcDiamondShineBuf(dsBuf);
        	}
        }
    }
    
    public void calcDiamondShineBuf(DiamondShineBuf buf){
        boolean b = false;
        //宝辉套装buf,相同的叠加，不同的加到末尾，没有个数限制，并且不会通知客户端
        if(diamondShineBufs.size() > 0){
            for(int i=0;i<diamondShineBufs.size();i++){
            	DiamondShineBuf tmpBuf = (DiamondShineBuf)diamondShineBufs.get(i);
                if(tmpBuf.getProperty()==buf.getProperty()){
                	buf.setValue(tmpBuf.getValue() + buf.getValue());
                	diamondShineBufs.set(i,buf);
                    b = true;
                    break;
                }
            }
            if(!b){
            	diamondShineBufs.add(buf);
            }
        } else {
        	diamondShineBufs.add(buf);
        }
    }
    
    public int getDiamondShineBufAttri(int pro){
        for (int i = 0; i < diamondShineBufs.size(); i++) {
        	DiamondShineBuf buf = (DiamondShineBuf) diamondShineBufs.get(i);
            if (buf.getProperty() == pro)
                return buf.getValue();
        }
        return 0;
    }
    
    public List getDiamondShineList(){
    	return diamondShineBufs;
    }
    
    public void removeDiamondShineList(){
    	diamondShineBufs.clear();
    }
    
    public void removeBuf(Buf buf, Changed changed) {
        if (buf.getProperty() > 0) {
        	if (buf.getUnit() >= Buf.CAMP_BATTLE_BUFF_MARK) {
        		if (campBattleBuffs.remove(buf)) {
        			adjustProperty();
        			if (changed != null) {
        				changed.addRemovedBuff(buf);
        			}
        		}
        	} else {
        		if (bufs.remove(buf)) {
        			adjustProperty();
        			if (changed != null)
        				changed.addRemovedBuff(buf);
        		}
        	}
        } else {
        	if (buf.getUnit() >= Buf.CAMP_BATTLE_BUFF_MARK) {
        		campBattleBuffs.remove(buf);
        	} else {
        		if(buf != null){
	        		if(buf.getProperty() == Buf.CAMP_EVA || buf.getProperty() == buf.CAMP_STONE){
	        			buf.setValue(0);
	        			changed.addBuf(buf);
	        		}
        		}
        		extendedBufs.remove(buf);
        	}
        }
    }
    
    public void removeSuitEffect (SuitEffect se) {
    	suitEffect.remove(se);
    }
    
    public void removeAllSuitEffect () {
    	suitEffect.clear();
    }

    public boolean removeBuf(int pro,Changed changed){
        if (pro > 0) {
            Iterator ite = bufs.iterator();
            while(ite.hasNext()){
                Buf buf = (Buf)ite.next();
                if(buf.getProperty()==pro){
                    ite.remove();
                    if(changed!=null){
                        changed.addRemovedBuff(buf);
                    }
                    return true;
                }
            }
        } else {
            Iterator ite = extendedBufs.iterator();
            while(ite.hasNext()){
                Buf buf = (Buf)ite.next();
                if(buf.getProperty()==pro){
                    ite.remove();
                    return true;
                }

            }
        }
        return false;
    }
    
    public boolean removeCampBattleBuff (int pro) {
    	Iterator ite = campBattleBuffs.iterator();
    	while (ite.hasNext()) {
    		Buf buf = (Buf)ite.next();
    		if (buf.getProperty() == pro) {
    			ite.remove();
    			return true;
    		}
    	}
    	return false;
    }

    public boolean hasBuf(int pro){
        if (pro > 0) {
            for (int i = 0; i < bufs.size(); i++) {
                Buf buf = (Buf) bufs.get(i);
                if (buf.getProperty() == pro)
                    return true;
            }
        } else {
            for(int i=0;i<extendedBufs.size();i++){
                Buf buf = (Buf) extendedBufs.get(i);
                if(buf.getProperty()==pro)
                    return true;
            }
        }
        for (int i = 0; i < campBattleBuffs.size(); i ++) {
        	Buf buf = (Buf) campBattleBuffs.get(i);
        	if (buf.getProperty() == pro) {
        		return true;
        	}
        }
        return false;
    }
    
    public boolean hasCampBattleBuff (int pro) {
    	for (int i = 0; i < campBattleBuffs.size(); i++) {
    		Buf buf = (Buf) campBattleBuffs.get(i);
    		if (buf.getProperty() == pro) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public Buf getItemBuf(byte pro){
        if(pro>0){
            for (int i = 0; i < bufs.size(); i++) {
                Buf buf = (Buf) bufs.get(i);
                if (buf.getProperty() == pro)
                    return buf;
            }
        }else{
            for(int i=0;i<extendedBufs.size();i++){
                Buf buf = (Buf)extendedBufs.get(i);
                if(buf.getProperty()==pro)
                    return buf;
            }
        }
        return null;
    }

    public Buf getBuf(int pro) {
        if (pro > 0) {
        	int value = 0;
            for (int i = 0; i < bufs.size(); i++) {
                Buf buf = (Buf) bufs.get(i);
				if (buf.getProperty() == pro) {
					value = buf.getValue();
					break;
				}
            }
            for (int i = 0; i < campBattleBuffs.size(); i++) {
            	Buf buf = (Buf) campBattleBuffs.get(i);
            	if (buf.getProperty() == pro) {
            		value += buf.getValue();
            		break;
            	}
            }
            if (value > 0) {
            	Buf ret = new Buf(0, (byte)0, value, 0, (byte)0);
            	return ret;
            }
        } else {
        	int value = 0;
            for(int i=0;i<extendedBufs.size();i++){
                Buf buf = (Buf)extendedBufs.get(i);
                if(buf.getProperty()==pro) {
                	value = buf.getValue();
                	break;
                }
            }
            for (int i = 0; i < campBattleBuffs.size(); i++) {
            	Buf buf = (Buf) campBattleBuffs.get(i);
            	if (buf.getProperty() == pro) {
            		value += buf.getValue();
            		break;
            	}
            }
            if (value > 0) {
            	Buf ret = new Buf(0, (byte)0, value, 0, (byte)0);
            	return ret;
            }
        }
        return null;
    }
    
    public Buf getCampBuf(int pro) {
        for(int i=0;i<extendedBufs.size();i++){
            Buf buf = (Buf)extendedBufs.get(i);
            if(buf.getProperty()==pro) {
            	return buf;
            }
        }
        return null;
    }
    
//    public boolean hasTimerBuf(){
//        for(int i=0;i<bufs.size();i++){
//            Buf buf = (Buf)bufs.get(i);
//            if(buf.getUnit()==1){
//                return true;
//            }
//        }
//        return false;
//    }

//    public int getBufSize() {
//        return bufs.size();
//    }

    public short getSkillPoint(int type) {
        return skillPoint[type];
    }

    public void setSkillPoint(int type, short point) {
        skillPoint[type] = point;
    }


    public void setGridSize(short gridSize) {
        player.setGridSize(gridSize);
    }

    public short getGridSize() {
        return player.getGridSize();
    }

    public void setAddedGridSize(int addedGridSize) {
        player.setAddedGridSize(addedGridSize);
    }

    public int getAddedGridSize() {
        return player.getAddedGridSize();
    }

    public int getAllGridSize() {
        return player.getGridSize() + player.getAddedGridSize();
    }

    public boolean isFull() {
        return getCurrentGridSize() >= getAllGridSize();
    }

    public boolean isPetFull() {
        return getPetCount() >= getPetSize();
    }

    public short getCurrentGridSize() {
        return (short) (basicItems.size() + extendedItems.size() +
                        taskItems.size() + equipments.size());
    }

    public void addMoney(int money, Changed changed) {
        if (money <= 0)
            return;
        setMoeny(getMoeny() + money);
        if (changed != null)
            changed.addProperty(Changed.MONEY, money);
    }

    public int decCredit(int credit,Changed changed){
        if(credit<=0)
            return 0;
        int oldCredit = player.getCredit();
        player.setCredit(Math.max(0,oldCredit-credit));
        if(changed!=null){
            changed.addProperty(Changed.CREDIT, player.getCredit() - oldCredit);
        }
        int newCreditIndex = getCreditIndex();
        if (newCreditIndex != creditIndex) {
            creditIndex = newCreditIndex;
            if(changed!=null){
                changed.setProperty(Changed.CREDIT_STRING, getCreditName());
            }
        }
        return oldCredit - player.getCredit();
    }

//    public void incCredit(int credit,Changed changed){
//        if(credit<=0)
//            return;
//        player.setCredit(player.getCredit()+credit);
//        changed.setProperty(Changed.CREDIT,credit);
//    }

    public void decMoney(int money, Changed changed) {
        if (money <= 0)
            return;
        setMoeny(getMoeny() - money);
        if (changed != null) {
            changed.addProperty(Changed.MONEY, -money);
        }
    }

    public void addExp(int e, Changed changed) {
        if (e < 0)
            return;
        
        if(player.getExp() < 0){
        	player.setExp(Integer.MAX_VALUE);
        }
        
        int exp = player.getExp() + e;
        if(exp < 0){
        	exp = Integer.MAX_VALUE;
        }
        if (changed != null)
            changed.addProperty(Changed.GAINEXP, e);
        
        if(player.getLevel() >= 100){
        	//经验转换
        	int explimit = 2000000;	//经验上限
        	int replacetimeslimit = 80;	//兑换次数上限
        	int rate = exp / explimit;
        	
        	if(rate + getReplaceTimes() > replacetimeslimit){
        		rate = replacetimeslimit - getReplaceTimes();
        	}
        	
        	int limitexp = 160000000;
        	if(rate > 0){
        		boolean isreplace = true;
        		long today = Utils.getTodayStart();    //一天起始时间
        		if(getReplaceclock().getTime() == today){//一天之内
        			if(getReplaceTimes() >= replacetimeslimit){
        				isreplace = false;
        				log.info("PlayerID[" + getId() + "] REPLACE_TIMES [" + getReplaceTimes() + "] LIMIT ");
        			}
        		}else{
        			setReplaceclock(new Date(today));
        			setReplaceTimes(0);
        		}
        		if(isreplace){
        			exp = exp - explimit * rate;
        			setTrainPoint(getTrainPoint() + 10 * rate);
        			setReplaceTimes(getReplaceTimes() + 1 * rate);
        		}
        	}
        }else{
	        int upLevel = Utils.getUpLevel(getLevel(), exp);
	        if (upLevel > 0) {
	            int oldLevel = player.getLevel();
	            player.setLevel(oldLevel + upLevel);
	            
	            //玩家到了100级给100灵力
	        	if(player.getLevel() >= 100){
	        		setTrainPoint(100);
	        	}
	        	
	            exp -= Utils.getUpLevelExp(oldLevel, oldLevel + upLevel);
	            int point = (getLevel() - oldLevel) / 2;
	            if ((getLevel() % 2 == 0) && oldLevel % 2 != 0)
	                point++;
	            player.setPoint(getPoint() + point);
	            player.setVitality(player.getVitality() + upLevel);
	            player.setStrength(player.getStrength() + upLevel);
	            player.setAgility(player.getAgility() + upLevel);
	            player.setIntelligence(player.getIntelligence() + upLevel);
	            player.setLeavePoints(player.getLeavePoints() + upLevel);
	            int gridSize = getGridSize(player.getLevel());
	            player.setGridSize((short) gridSize);
	            int oldPetSize = player.getPetSize();
	            int petSize = getPetSize(player.getLevel());
	            if (oldPetSize != petSize) {
	                setPetSize(petSize);
	                changed.addProperty(Changed.PET_GRID, petSize);
	            }
	            adjustProperty();
	            setHp(getMaxHp());
	            setMp(getMaxMp());
	            if (changed != null) {
	            	IEquipment[] equs = getUsedEquipments();
	                if(equs != null){
		                for(int i=0; i<equs.length; i++){
		                	if(equs[i] != null && equs[i].isGrow()){
		                		changed.updatEquipmentProperty(equs[i], PLAYER_USE_EQUIPMENT, getLevel());
		                	}
		                }
	                }
	                changed.setProperty(Changed.LEVEL, player.getLevel());
	                changed.setProperty(Changed.UPLEVELEXP,
	                                    Utils.getUpLevelExp(player.getLevel()));
	                changed.setProperty(Changed.VITALITY,
	                                    player.getVitality() +
	                                    getBufProperty(Changed.VITALITY));
	                changed.setProperty(Changed.STRENGTH,
	                                    player.getStrength() +
	                                    getBufProperty(Changed.STRENGTH));
	                changed.setProperty(Changed.AGILITY,
	                                    player.getAgility() +
	                                    getBufProperty(Changed.AGILITY));
	                changed.setProperty(Changed.INTELLIGENCE,
	                                    player.getIntelligence() +
	                                    getBufProperty(Changed.INTELLIGENCE));
	                changed.setProperty(Changed.LEAVEPOINTS, player.getLeavePoints());
	                changed.setProperty(Changed.POINT, getPoint());
	                changed.setProperty(Changed.GRIDSIZE, getAllGridSize());
	                changed.setProperty(Changed.HP, getHp());
	                changed.setProperty(Changed.MP, getMp());
	            }
	            log.info("Add Exp[" + e + "] PlayerID[" + getId() + "] OLDLEVEL[" + oldLevel + "] NEWLEVEL[" + player.getLevel() + "]");
	        }
        }
        player.setExp(exp);
        if (changed != null)
        	changed.setProperty(Changed.EXP, exp);
    }
    
    public void downGrade(int level, Changed changed) {
        if (level <= 0)
            return;
        int exp = 1;
        if (changed != null)
            changed.addProperty(Changed.GAINEXP, 1);
        int downLevel = getLevel() - level;
        int point = level / 2;
        if ((getLevel() % 2 == 0) && downLevel % 2 != 0)
            point++;
        player.setPoint(getPoint() - point);
        
        player.setVitality(player.getVitality() - level);
        player.setStrength(player.getStrength() - level);
        player.setAgility(player.getAgility() - level);
        player.setIntelligence(player.getIntelligence() - level);
        player.setLeavePoints(player.getLeavePoints() - level);
        
        int gridSize = getGridSize(downLevel);
        player.setGridSize((short) gridSize);
        
//        int oldPetSize = player.getPetSize();
//        int petSize = getPetSize(downLevel);
//        if (oldPetSize != petSize) {
//            setPetSize(petSize);
//            changed.addProperty(Changed.PET_GRID, petSize);
//        }
        player.setLevel(downLevel);
        adjustProperty();
        setHp(getMaxHp());
        setMp(getMaxMp());
        
        if (changed != null) {
            changed.setProperty(Changed.LEVEL, player.getLevel());
            changed.setProperty(Changed.UPLEVELEXP,
                                Utils.getUpLevelExp(player.getLevel()));
            changed.setProperty(Changed.VITALITY,
                                player.getVitality() +
                                getBufProperty(Changed.VITALITY));
            changed.setProperty(Changed.STRENGTH,
                                player.getStrength() +
                                getBufProperty(Changed.STRENGTH));
            changed.setProperty(Changed.AGILITY,
                                player.getAgility() +
                                getBufProperty(Changed.AGILITY));
            changed.setProperty(Changed.INTELLIGENCE,
                                player.getIntelligence() +
                                getBufProperty(Changed.INTELLIGENCE));
            changed.setProperty(Changed.LEAVEPOINTS, player.getLeavePoints());
            changed.setProperty(Changed.POINT, getPoint());
            changed.setProperty(Changed.GRIDSIZE, getAllGridSize());
            changed.setProperty(Changed.HP, getHp());
            changed.setProperty(Changed.MP, getMp());
        }
        
        player.setExp(exp);
        if (changed != null)
            changed.setProperty(Changed.EXP, exp);
    }

    public void resetProperties(Changed changed){
        int level = getLevel();
        setAgility(level + addAttributes[ADDATTR_AGILITY]);
        setStrength(level + addAttributes[ADDATTR_STRENGTH]);
        setIntelligence(level + addAttributes[ADDATTR_INTELLIGENCE]);
        setVitality(level + addAttributes[ADDATTR_VITALITY]);
        setLeavePoints(level-1);
        adjustProperty();
        setHp(getMaxHp());
        setMp(getMaxMp());
        if(changed!=null){
            changed.setProperty(Changed.VITALITY,level + addAttributes[ADDATTR_VITALITY]);
            changed.setProperty(Changed.STRENGTH,level + addAttributes[ADDATTR_STRENGTH]);
            changed.setProperty(Changed.INTELLIGENCE,level + addAttributes[ADDATTR_INTELLIGENCE]);
            changed.setProperty(Changed.AGILITY,level + addAttributes[ADDATTR_AGILITY]);
            changed.setProperty(Changed.HP,getHp());
            changed.setProperty(Changed.MP,getMp());
            changed.setProperty(Changed.LEAVEPOINTS,getLeavePoints());
        }
    }

    public void resetAgility(Changed changed){
        int level = getLevel();
        int agility = getAgility();
        int v = agility - level - addAttributes[ADDATTR_AGILITY];
        setAgility(level + addAttributes[ADDATTR_AGILITY]);
        setLeavePoints(getLeavePoints()+v);
        adjustProperty();
        setHp(getMaxHp());
        setMp(getMaxMp());
        if(changed!=null){
            changed.setProperty(Changed.AGILITY,level + addAttributes[ADDATTR_AGILITY]);
            changed.setProperty(Changed.HP,getHp());
            changed.setProperty(Changed.MP,getMp());
            changed.setProperty(Changed.LEAVEPOINTS,getLeavePoints());
        }
    }

    public void resetStrength(Changed changed){
        int level = getLevel();
        int strength = getStrength();
        int v = strength - level - addAttributes[ADDATTR_STRENGTH];
        setStrength(level + addAttributes[ADDATTR_STRENGTH]);
        setLeavePoints(getLeavePoints()+v);
        adjustProperty();
        setHp(getMaxHp());
        setMp(getMaxMp());
        if(changed!=null){
            changed.setProperty(Changed.STRENGTH,level + addAttributes[ADDATTR_STRENGTH]);
            changed.setProperty(Changed.HP,getHp());
            changed.setProperty(Changed.MP,getMp());
            changed.setProperty(Changed.LEAVEPOINTS,getLeavePoints());
        }
    }

    public void resetVitality(Changed changed){
        int level = getLevel();
        int vitality = getVitality();
        int v = vitality - level - addAttributes[ADDATTR_VITALITY];
        setVitality(level + addAttributes[ADDATTR_VITALITY]);
        setLeavePoints(getLeavePoints()+v);
        adjustProperty();
        setHp(getMaxHp());
        setMp(getMaxMp());
        if(changed!=null){
            changed.setProperty(Changed.VITALITY,level + addAttributes[ADDATTR_VITALITY]);
            changed.setProperty(Changed.HP,getHp());
            changed.setProperty(Changed.MP,getMp());
            changed.setProperty(Changed.LEAVEPOINTS,getLeavePoints());
        }
    }

    public void resetIntelligence(Changed changed){
        int level = getLevel();
        int intelligence = getIntelligence();
        int v = intelligence - level - addAttributes[ADDATTR_INTELLIGENCE];
        setIntelligence(level + addAttributes[ADDATTR_INTELLIGENCE]);
        setLeavePoints(getLeavePoints()+v);
        adjustProperty();
        setHp(getMaxHp());
        setMp(getMaxMp());
        if(changed!=null){
            changed.setProperty(Changed.INTELLIGENCE,level + addAttributes[ADDATTR_INTELLIGENCE]);
            changed.setProperty(Changed.HP,getHp());
            changed.setProperty(Changed.MP,getMp());
            changed.setProperty(Changed.LEAVEPOINTS,getLeavePoints());
        }
    }

    /**
     * 超过最大数量返回1，重复返回2，成功返回0
     * @param player PlayerData
     * @return int
     */
    public int addBlackList(int id,String name){
        if(blackList.size() >= 10){
            return 1;
        }
        for(int i=0;i<blackList.size();i++){
            PlayerLink p = (PlayerLink)blackList.get(i);
            if(p.id==id)
                return 2;
        }
        PlayerLink p = new PlayerLink(id,name);
        blackList.add(p);
        return 0;
    }

    public int removeBlackList(String name, int id){
        for (int i = 0; i < blackList.size(); i++) {
            PlayerLink friend = (PlayerLink) blackList.get(i);
            if (friend.name.equals(name) || friend.id == id) {
                blackList.remove(i);
                return friend.id;
            }
        }
        return -1;
    }

    public int[] getBlackListIds(){
        if(blackList.size()==0)
            return new int[0];
        int[] ret = new int[blackList.size()];
        for(int i=0;i<blackList.size();i++){
            PlayerLink p = (PlayerLink)blackList.get(i);
            ret[i] = p.id;
        }
        return ret;
    }

    public boolean inBlackList(int id){
        for(int i=0;i<blackList.size();i++){
            PlayerLink p = (PlayerLink)blackList.get(i);
            if(p.id==id)
                return true;
        }
        return false;
    }

    /**
     * 超过最大数量返回1，重复返回2，成功返回0
     * @param player PlayerData
     * @return int
     */
    public int addFriend(int id, String name, long loginTime) {
        if (friends.size() >= 25)
            return 1;
        if(friends.containsKey(new Integer(id))){
            return 2;
        }
        Friend friend = new Friend(id,name,1, loginTime);
        friends.put(new Integer(id),friend);
//        for (int i = 0; i < friends.size(); i++) {
//            PlayerLink friend = (PlayerLink) friends.get(i);
//            if (friend.id == id)
//                return 2;
//        }
//        PlayerLink friend = new PlayerLink(id, name);
//        friends.add(friend);
        return 0;
    }

    public int removeFriend(String name) {
//        for (int i = 0; i < friends.size(); i++) {
//            PlayerLink friend = (PlayerLink) friends.get(i);
//            if (friend.name.equals(name)) {
//                friends.remove(i);
//                return friend.id;
//            }
//        }
        Iterator ite = friends.values().iterator();
        while(ite.hasNext()){
            Friend friend = (Friend)ite.next();
            if(friend.getName().equals(name)){
                ite.remove();
                return friend.getId();
            }
        }
        return -1;
//        for(int i=0;i<friends.size();i++){
//            Friend friend = (Friend)friends.get(i);
//            if(friend.getName().equals(name)){
//                friends.remove(i);
//                return friend.getId();
//            }
//        }
//        return -1;
    }

    public int hashCode() {
        return player.getId();
    }


    /**
     * 返回1不能学习此系技能,2已经学会此技能,3没学会低等级技能,4等级不到,5钱不够,0成功
     * @param ability Ability
     * @return int
     */

    public int learnAbility(Ability ability,boolean moneyflag) {
        int level = 0;
        int moneyOld =ability.getPrice();			//原先需要的金钱
        if (moneyflag){
            if(hasBuf(Buf.HOPEOBJECT)){
            	Buf buf = getBuf(Buf.HOPEOBJECT);
            	int rate = buf.getValue();
            	moneyOld = moneyOld * (100 - rate)/100;
            }
	        if (getMoeny() < moneyOld) {
	            return 5;
	        }
        }
        for (int i = 0; i < abilities.size(); i++) {
            Ability a = (Ability) abilities.get(i);
            if (a.getName().equals(ability.getName())) {
                level = a.getLevel();
            }
        }
        if (level >= ability.getLevel())
            return 2;
        if ((level + 1) != ability.getLevel())
            return 3;
//        if (getAbilityPoint(ability.getType()) < ability.getRequiredLevel())
//            return 4;
        abilities.add(ability);
        if (moneyflag){
        	setMoeny(getMoeny() - moneyOld);
        }
        return 0;
    }

    public boolean canLearnSkill(byte type) {
        if (skillPoint[type] != -1)
            return false;
        if (type == Player.SKILL_FISHING || type == Player.SKILL_COOKING)
            return true;
        //mengjie modify 20091207
        /*
        if (type <= 2) {
            return skillPoint[0] == -1 && skillPoint[1] == -1 &&
                    skillPoint[2] == -1;
        } else if (type > 2 && type <= 5) {
            return skillPoint[3] == -1 && skillPoint[4] == -1 &&
                    skillPoint[5] == -1;
        }
        return false;
        */
        if (type < 6) {
        	int skillcount = 0;
            for(int i = 0;i<6;i++){
            	if (skillPoint[i] > -1){
            		skillcount++;
            	}
            }
            if (skillcount < 3){
            	return true;
            }
        }
        return false;
    }


    public void resetSkills() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            for (int i = 0; i < 8; i++) {
                dos.writeShort(skillPoint[i]);
            }
            player.setTechSkills(bos.toByteArray());
        } catch (Exception ex) {
            log.error(ex, ex);
        }
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public void setMaxMp(int maxMp) {
        this.maxMp = maxMp;
    }

    public int getMaxHp() {
        return maxHp + getBufProperty(Changed.HP) + getSuitEffectProperty(Changed.HP);
    }

    public int getMaxMp() {
        return maxMp + getBufProperty(Changed.MP) + getSuitEffectProperty(Changed.MP);
    }
    
    public int calculateMaxHp(){
    	int ret_ADD_HPMAX = getDiamondShineBufAttri(DiamondShineBuf.ADD_HPMAX);
    	return calculateMaxHpWithoutDiamondShine() * (100 + ret_ADD_HPMAX) / 100;
    }
    
    //生命HP= 8 * (体力 +　力量/6) * ((int)(sqrt(level * 100) + 30) / 40) + 50 + 血宝石
    public int calculateMaxHpWithoutDiamondShine() {
    	int ret_ADD_HPMAX = getDiamondShineBufAttri(DiamondShineBuf.ADD_HPMAX);
//    		int ret_AGI = getDiamondShineBufAttri(DiamondShineBuf.AGI);
//    		int agi = getRealAgility() * (100 + ret_AGI) / 100;
//    		int ret_STR = getDiamondShineBufAttri(DiamondShineBuf.STR);
//    		int str = getRealStrength() * (100 + ret_STR) / 100;
//    		int ret_INT = getDiamondShineBufAttri(DiamondShineBuf.INT);
//    		int inte = getRealIntelligence() * (100 + ret_INT) / 100;
    	int vit = getRealVitality();
    	vit += getDiamondShineBufAttri(DiamondShineBuf.VIT_VALUE);
    	int buf_stone = getUsedEquipmentPropertyStoneBuf(IEquipment.EQUIP_ADD_HPMAX);
    	int []magicpositionHpOrMp = MagicPosMessage.getMagicPosAttr(mindMagicPos, getMindLevel(), getMindFloor());
    	int magicposHp = 0;
    	if(magicpositionHpOrMp != null){
    		magicposHp = magicpositionHpOrMp[0];
    	}
    	return Utils.calculateMaxHp(vit, 0, 0, 0, getLevel(), buf_stone + getUsedEquipmentProperty(IEquipment.EQUIP_ADD_HPMAX) + getSuitEffectPropertyStone(HpStone) + getTrainLevelPropertyStone(HpStone) + magicposHp);
    }
    
    public int calculateMaxMp(){
    	int ret_ADD_MPMAX = getDiamondShineBufAttri(DiamondShineBuf.ADD_MPMAX);
    	return calculateMaxMpWithoutDiamondShine() * (100 + ret_ADD_MPMAX) / 100;
    }
    
    //魔法MP = 3 * (智力 +　力量/4) * (((int)(sqrt(level * 100) + 30)/40)) + 50 + 蓝宝石
    public int calculateMaxMpWithoutDiamondShine() {
    	int ret_ADD_STR = getDiamondShineBufAttri(DiamondShineBuf.STR);
    	int ret_ADD_INT = getDiamondShineBufAttri(DiamondShineBuf.INT);
    	int str = getRealStrength();
    	str = str * (100 + ret_ADD_STR) / 100;
    	str += getDiamondShineBufAttri(DiamondShineBuf.STR_VALUE);
    	int inte = getRealIntelligence();
    	inte = inte * (100 + ret_ADD_INT) / 100;
    	inte += getDiamondShineBufAttri(DiamondShineBuf.INT_VALUE);
    	int buf_stone = getUsedEquipmentPropertyStoneBuf(IEquipment.EQUIP_ADD_MPMAX);
    	int []magicpositionHpOrMp = MagicPosMessage.getMagicPosAttr(mindMagicPos, getMindLevel(), getMindFloor());
    	int magicposMp = 0;
    	if(magicpositionHpOrMp != null){
    		magicposMp = magicpositionHpOrMp[1];
    	}
		return Utils.calculateMaxMp(0, 0, str, inte, getLevel(),  buf_stone + getUsedEquipmentProperty(IEquipment.EQUIP_ADD_MPMAX) + getSuitEffectPropertyStone(MpStone) + getTrainLevelPropertyStone(MpStone) + magicposMp);
    	
    }

    /**
     * petversion >= 4 新增：宠物灵性给玩家带来的奖励
     * 规则：sum = （宠物基本属性 + 宠物装备属性）* 悟性提升比例） * 灵性提升比例
     * 玩家基本属性 + 装备属性 + sum + buf
     * @return
     */
    public int getRealVitality() {
        int vitality = player.getVitality() +
                       getUsedEquipmentProperty(IEquipment.EQUIP_ADD_VIT) +
                       getBufProperty(Changed.VITALITY) + getSuitEffectProperty(Changed.VITALITY) +
                       getSuitEffectPropertyStone(VitStone) + getTrainLevelPropertyStone(VitStone);
        //加上附魔属性
        vitality += getUsedEquipmentEnchantingProperty(IEquipment.EQUIP_ADD_VIT);
        
        if (getPet() != null) {
        	getPet().setPetExtendBuff(extendedBufs);
        	vitality += getPet().getRealVitality() * Utils.SPRITE_PROPERTIES_AWARD[getPet().getSpiritualityLevel()] / 10000;
        }
        
        //加上阵营鉴定BUF加成
        vitality += getUsedEquipmentPropertyEvaBuf(IEquipment.EQUIP_ADD_VIT);
        
        //加上阵营宝石BUF加成
        vitality += getUsedEquipmentPropertyStoneBuf(IEquipment.EQUIP_ADD_VIT);
        
        //加全身9钻效果
        if(IsEquIdentifyEffcet()){
        	vitality += 90;
        }
        
        return vitality;
    }

    public int getRealStrength() {
        int strength = player.getStrength() +
                       getUsedEquipmentProperty(IEquipment.EQUIP_ADD_STR) +
                       getBufProperty(Changed.STRENGTH) + getSuitEffectProperty(Changed.STRENGTH) + 
                       getSuitEffectPropertyStone(StrStone) + getTrainLevelPropertyStone(StrStone);
        
        //加上附魔属性
        strength += getUsedEquipmentEnchantingProperty(IEquipment.EQUIP_ADD_STR);
        
        if (getPet() != null) {
        	getPet().setPetExtendBuff(extendedBufs);
        	strength += getPet().getRealStrength() * Utils.SPRITE_PROPERTIES_AWARD[getPet().getSpiritualityLevel()] / 10000;
        }
        
        //加上阵营鉴定BUF加成
        strength += getUsedEquipmentPropertyEvaBuf(IEquipment.EQUIP_ADD_STR);
        
        //加上阵营宝石BUF加成
        strength += getUsedEquipmentPropertyStoneBuf(IEquipment.EQUIP_ADD_STR);
        
        //加全身9钻效果
        if(IsEquIdentifyEffcet()){
        	strength += 90;
        }
        
        return strength;
    }

    public int getRealIntelligence() {
        int intelligence = player.getIntelligence() +
                           getUsedEquipmentProperty(IEquipment.EQUIP_ADD_INT) +
                           getBufProperty(Changed.INTELLIGENCE) + getSuitEffectProperty(Changed.INTELLIGENCE) +
                           getSuitEffectPropertyStone(IneStone) + getTrainLevelPropertyStone(IneStone);
        
        //加上附魔属性
        intelligence += getUsedEquipmentEnchantingProperty(IEquipment.EQUIP_ADD_INT);
        
        if (getPet() != null) {
        	getPet().setPetExtendBuff(extendedBufs);
        	intelligence += getPet().getRealIntelligence() * Utils.SPRITE_PROPERTIES_AWARD[getPet().getSpiritualityLevel()] / 10000;
        }
        
        //加上阵营鉴定BUF加成
        intelligence += getUsedEquipmentPropertyEvaBuf(IEquipment.EQUIP_ADD_INT);
        
        //加上阵营宝石BUF加成
        intelligence += getUsedEquipmentPropertyStoneBuf(IEquipment.EQUIP_ADD_INT);
        
        //加全身9钻效果
        if(IsEquIdentifyEffcet()){
        	intelligence += 90;
        }
        
        return intelligence;
    }

    public int getRealAgility() {
        int agility = player.getAgility() +
                      getUsedEquipmentProperty(IEquipment.EQUIP_ADD_AGI) +
                      getBufProperty(Changed.AGILITY) + getSuitEffectProperty(Changed.AGILITY) +
                      getSuitEffectPropertyStone(AgiStone) + getTrainLevelPropertyStone(AgiStone);
        
        //加上附魔属性
        agility += getUsedEquipmentEnchantingProperty(IEquipment.EQUIP_ADD_AGI);
        
        if (getPet() != null) {
        	getPet().setPetExtendBuff(extendedBufs);
        	agility += getPet().getRealAgility() * Utils.SPRITE_PROPERTIES_AWARD[getPet().getSpiritualityLevel()] / 10000;
        }
        
        //加上阵营鉴定BUF加成
        agility += getUsedEquipmentPropertyEvaBuf(IEquipment.EQUIP_ADD_AGI);
        
        //加上阵营宝石BUF加成
        agility += getUsedEquipmentPropertyStoneBuf(IEquipment.EQUIP_ADD_AGI);
        
        //加全身9钻效果
        if(IsEquIdentifyEffcet()){
        	agility += 90;
        }
        
        return agility;
    }

    public int getAddedBufVitality() {
        int vitality = player.getVitality() +
                       getBufProperty(Changed.VITALITY);
        return vitality;
    }

    public int getAddedBufStrength() {
        int strength = player.getStrength() +
                       getBufProperty(Changed.STRENGTH);
        return strength;
    }

    public int getAddedBufIntelligence() {
        int intelligence = player.getIntelligence() +
                           getBufProperty(Changed.INTELLIGENCE);
        return intelligence;
    }

    public int getAddedBufAgility() {
        int agility = player.getAgility() +
                      getBufProperty(Changed.AGILITY);
        return agility;
    }

    public int getBufProperty(int pro) {
        int ret = 0;
        for (int i = 0; i < bufs.size(); i++) {
            Buf buf = (Buf) bufs.get(i);
            if (buf.getProperty() == pro) {
                ret += buf.getValue();
            }
        }
        for (int i = 0; i < campBattleBuffs.size(); i++) {
        	Buf buf = (Buf) campBattleBuffs.get(i);
        	if (buf.getProperty() == pro) {
        		ret += buf.getValue();
        	}
        }
        
        Buf buf = getCampBuf(Buf.CAMP_EVA);
        if(buf != null){
        	//阵营BUFF 除力智体敏  提供其它属性的增加
        	int equPro = -1;
        	switch(pro){
        	case Changed.PATTACK:
        		equPro = IEquipment.EQUIP_ADD_PATTACK;
        		break;
        	case Changed.PDEFENSE:
        		equPro = IEquipment.EQUIP_ADD_DEFENCE;
        		break;
        	}
        	if(equPro != -1){
        		ret += getUsedEquipmentPropertyEvaBuf(equPro) * buf.getValue() / 100;
        	}
        }
        buf = getCampBuf(Buf.CAMP_STONE);
        if(buf != null){
        	int equPro = -1;
        	//阵营BUFF 除力智体敏  提供其它属性的增加
        	switch(pro){
        	case Changed.PATTACK:
        		equPro = IEquipment.EQUIP_ADD_PATTACK;
        		break;
        	case Changed.MATTACK:
        		equPro = IEquipment.EQUIP_ADD_MATTACK;
        		break;
        	case Changed.PDEFENSE:
        		equPro = IEquipment.EQUIP_ADD_PDEFENCE;
        		break;
        	case Changed.MDEFENSE:
        		equPro = IEquipment.EQUIP_ADD_MDEFENCE;
        		break;
        	case Changed.HIT:
        		equPro = IEquipment.EQUIP_ADD_HIT;
        		break;
        	case Changed.PARRY:
        		equPro = IEquipment.EQUIP_ADD_FLEE;
        		break;
        	}
        	if(equPro != -1){
        		ret += getUsedEquipmentPropertyStoneBuf(equPro) * buf.getValue() / 100;
        	}
        }
        
        return ret;
    }
    
    /**
     * 新增全身9钻效果
     */
    public static final int NineEquDiamond = 81;
    public Boolean IsEquIdentifyEffcet(){
    	boolean flag = false;
    	int ret = 0;
    	for(int i= 0;i<usedEquipments.length;i++){
    		if(usedEquipments[i]!=null){
    			IEquipment equ = (IEquipment) usedEquipments[i].item;//获取当前装备
    			if(equ.isValid()){
    				int EquDiamond = equ.getDiamond();//获得当前装备钻数
    				ret += EquDiamond;
    			}
    		}
    	}
    	if(ret == NineEquDiamond){//达到一身9钻
    		flag = true;
    	}
    	return flag;
    }
    
    // 获得套装效果带来的属性加成
    public int getSuitEffectProperty (int pro) {
    	int ret = 0;
    	for (int i = 0; i < suitEffect.size(); i ++) {
    		SuitEffect bs = (SuitEffect) suitEffect.get(i);
    		if (- bs.getType() == pro) {
    			ret += bs.getValue();
    		}
    	}
    	return ret;
    }
    
    // 获得套装效果带来的属性加成
    public int getSuitEffectPropertyStone (int pro) {
    	int[] value = getSuitEffectDiamondAddValue();
    	return value[pro];
    }
    
    //获得聚灵效果带来宝石加成
    public int getTrainLevelPropertyStone(int pro){
    	int []value = getTrainAttributeAddValue();
    	return value[pro];
    }
    
    public static final int VitStone = 1;//体力宝石
    public static final int IneStone = 2;//智力
    public static final int StrStone = 3;//力量
    public static final int AgiStone = 4;//敏捷
    public static final int PattackStone = 5;//物攻
    public static final int MattackStone = 6;//魔攻
    public static final int PdefStone = 7;//物防
    public static final int MdefStone = 8;//魔防
    public static final int HitStone = 9;//命中
    public static final int ParryStone = 10;//闪避
    public static final int HpStone = 15;//血量
    public static final int MpStone = 16;//魔法
    public static final int NocriStone = 17;//免暴
    
    //获得装备上不同属性宝石总属性值
    public int []getDiamondAddValue(){
    	int []stoneValue = new int[18];//各个宝石总属性值
    			for (int j = 0; j < usedEquipments.length; j++) {
    				if (usedEquipments[j] != null) {
    					IEquipment equ = (IEquipment) usedEquipments[j].item;
    					if(equ.isValid()){//装备有效
    						byte count = equ.getDiamondcount();
    						if(count > 0){//宝石数
    							for(byte p = 0; p < count;p++){
    								DiamondMosaic diamosaic = equ.getDiamondMosaicRole(p);//获得当前宝石
    								if(diamosaic != null){
    									byte diamondproperty = diamosaic.getProperty();//属性
    									short[] developAddPoint = equ.getDevelopAddPoint();
    									int addPoint = 0;
										addPoint = developAddPoint[p];
    									stoneValue[diamondproperty] += diamosaic.getAddPoint() + addPoint;
    								}
    							}
    						}
    					}
    				}
    			}
    	return stoneValue;
    }
    
    public static final byte minLevelIndex = 0;
    //将玩家聚灵等级从小到大排序
    public int [] SortTrainLevel(){
    	int [] playerTrainLevel = getTrainLevel();
    	for(int i = 0;i<playerTrainLevel.length;i++){
    		for(int j=0;j<playerTrainLevel.length -1;j++){
    			int tempLevel = playerTrainLevel[j];
    			if(playerTrainLevel[j] > playerTrainLevel[j+1]){
    				tempLevel = playerTrainLevel[j];
    				playerTrainLevel[j] = playerTrainLevel[j+1];
    				playerTrainLevel[j+1] = tempLevel;
    			}
    		}
    	}
    	return playerTrainLevel;
    }
    
    //获取聚灵等级宝石加成
    public int [] getTrainAttributeAddValue(){
    	int []vaule = getDiamondAddValue();//13种宝石总属性值
    	int[] addpoint = new int[18];//存增加属性值
    	int [] sortLevel = SortTrainLevel();
    	int MinTrainLevel = sortLevel[minLevelIndex];//数组最前面为最小
    	int rate = MinTrainLevel / 10;
    	if(rate > 0){
    		for(int k= 0;k < vaule.length;k++){
    			addpoint[k] += vaule[k] * (0.02 * rate);
    		}
    	}	
    	return addpoint;
    }
    
    
    //获得加成后各属性宝石值
    public int []getSuitEffectDiamondAddValue(){
    	int []addpoint = new int[18];//存13种属性宝石增加的属性值
    	int []vaule = getDiamondAddValue();
    	for (int i = 0; i < suitEffect.size(); i ++){
    		SuitEffect bs = (SuitEffect) suitEffect.get(i);
    		if (bs.getType() == 36){//新套装效果
    			for(int j = 0; j < vaule.length; j++){
    				addpoint[j] += vaule[j] * bs.getValue() / 100;
    			}
    		}
    	}
    	return addpoint;
    }
    
    public static final byte train_attack = 0;
	public static final byte train_pdef = 1;
	public static final byte train_mattack = 2;
	public static final byte train_mdef = 3;
	public static final byte train_hit = 4;
	public static final byte train_nocri = 5;
    
    //取玩家聚灵总等级
	public int getPlayerTrainLevel(){
		int ret = 0;
		int []trainlevel = getTrainLevel();
		for(int i = 0;i<trainlevel.length;i++){
			ret += trainlevel[i];
		}
		return ret;
	}
	
	
    //聚灵属性相关等级
    public int[] getTrainLevel(){
    	int[] trainLevel = new int[6];
    	trainLevel[train_attack] = getTrainAttackLevel();
    	trainLevel[train_pdef] = getTrainPdefLevel();
    	trainLevel[train_mattack] = getTrainMattackLevel();
    	trainLevel[train_mdef] = getTrainMdefLevel();
    	trainLevel[train_hit] = getTrainHitLevel();
    	trainLevel[train_nocri] = getTrainNocriLevel();
    	return trainLevel;
    }
    
    //聚灵等级升级需达到的灵力点
    public int[] getUpLevelTrainPoint(){
    	int []trainlevel = getTrainLevel();
    	int []uplevelpoint = new int[6];
    	
    	for(int i = 0;i<trainlevel.length;i++){
    		trainlevel[i] ++;
    		if(trainlevel[i] > 0 && trainlevel[i] <= 40){
    			uplevelpoint[i] = trainlevel[i] * 7;
    		}else{
    			uplevelpoint[i] = trainlevel[i] * 34;
    		}
    	}
    	
    	return uplevelpoint;		
    }
    
    //取不同聚灵属性加成值
    public int [] getTrainLevelattributepoint(){
    	int [] attributepoint = new int [6];	//存6项聚灵属性加成值
    	int [] trainlevel = getTrainLevel();
    	
		if(trainlevel[train_attack] > 0){
			for(int i=1; i<=trainlevel[train_attack]; i++){
				attributepoint[train_attack] += 1 + (i - 1) * 1;
			}
		}
		if(trainlevel[train_pdef] > 0){
			for(int i=1; i<=trainlevel[train_pdef]; i++){
				attributepoint[train_pdef] += 2 + (i - 1) * 3;
			}
		}	
		if(trainlevel[train_mattack] > 0){
			for(int i=1; i<=trainlevel[train_mattack]; i++){
				attributepoint[train_mattack] += 1 + (i - 1) * 1;
			}
		}
		if(trainlevel[train_mdef] > 0){
			for(int i=1; i<=trainlevel[train_mdef]; i++){
				attributepoint[train_mdef] += 2 + (i - 1) * 3;
			}
		}
		if(trainlevel[train_hit] > 0){
			for(int i=1; i<=trainlevel[train_hit]; i++){
				attributepoint[train_hit] += 7 + (i - 1) * 1;
			}
		}
		if(trainlevel[train_nocri] > 0){
			for(int i=1; i<=trainlevel[train_nocri]; i++){
				attributepoint[train_nocri] += 1 + (i - 1) * 1;
			}
		}
    	
    	return attributepoint;
    }
    
    public static final int waterMagicPos = 0;
    public static final int soilMagicPos = 1;
    public static final int fireMagicPos = 2;
    public static final int windMagicPos = 3;
    public static final int mindMagicPos = 4;
    
    
    //获得玩家各法阵阵眼总等级
    public int getTotalMagicPosLevel(){
    	int ret = 0;
		int []magiclevel = getMagicPosLevel();
		for(int i = 0;i<magiclevel.length;i++){
			ret += magiclevel[i];
		}
		return ret;
    }
    
    //获得玩家各法阵阵眼总经验
    public int getTotalMagicPosExp(){
    	int ret = 0;
		int []magicexp = getMagicPosExp();
		for(int i = 0;i<magicexp.length;i++){
			ret += magicexp[i];
		}
		return ret;
    }
    
    
    //获得玩家当前所有阵眼经验
    public int [] getMagicPosExp(){
    	int [] playerMagicPosExp = new int[5];
    	playerMagicPosExp[waterMagicPos] = getWaterExp();
    	playerMagicPosExp[soilMagicPos] = getSoilExp();
    	playerMagicPosExp[fireMagicPos] = getFireExp();
    	playerMagicPosExp[windMagicPos] = getWindExp();
    	playerMagicPosExp[mindMagicPos] = getMindExp();
    	return playerMagicPosExp;
    }
    
    //获得玩家当前所有阵眼级别
    public int []getMagicPosLevel(){
    	int []playerMagicPosLevel = new int[5];
    	playerMagicPosLevel[waterMagicPos] = getWaterLevel();
    	playerMagicPosLevel[soilMagicPos] = getSoilLevel();
    	playerMagicPosLevel[fireMagicPos] = getFireLevel();
    	playerMagicPosLevel[windMagicPos] = getWindLevel();
    	playerMagicPosLevel[mindMagicPos] = getMindLevel();
    	return playerMagicPosLevel;
    }
    
    //获得玩家当前所有阵眼阶层
    public int []getMagicPosFloor(){
    	int [] playerMagPosFr = new int [5];
    	playerMagPosFr[waterMagicPos] = getWaterFloor();
    	playerMagPosFr[soilMagicPos] = getSoilFloor();
    	playerMagPosFr[fireMagicPos] = getFireFloor();
    	playerMagPosFr[windMagicPos] = getWindFloor();
    	playerMagPosFr[mindMagicPos] = getMindFloor();
    	return playerMagPosFr;
    }
    
    // 判断是否是增加属性的套装效果，如果是加入属性池
    public void setSuitEffProperty () {
    	SuitEffect[] propertySuitEffects = Suits.getSpritePropertiesSuitEffect(getUsedEquipments());
    	if (propertySuitEffects != null) {
    		for (int i = 0; i < propertySuitEffects.length; i++) {
        		addPropertySuitEffect(propertySuitEffects[i]);
        	}
    	}
    }

    public int getUsedEquipmentProperty(int pro) {
        int ret = 0;
        for (int i = 0; i < usedEquipments.length; i++) {
            if (usedEquipments[i] != null) {
                IEquipment equ = (IEquipment) usedEquipments[i].item;
                if (equ.isValid())
                    ret += equ.getProperty(pro, getLevel());
            }
        }
        return ret;
    }
    
    public int getUsedEquipmentPropertyStoneBuf(int pro) {
    	int ret = 0;
    	Buf buf = getBuf(Buf.CAMP_STONE);		//宝石加成
    	if(buf == null) return 0;
    	for (int i = 0; i < usedEquipments.length; i++) {
    		if (usedEquipments[i] != null) {
    			IEquipment equ = (IEquipment) usedEquipments[i].item;
    			if(equ.isValid()){
    				ret += equ.getDiamondMosiacProperty(pro);
    			}
    		}
    	}
    	return ret * buf.getValue() / 100;
    }
    
    public int getUsedEquipmentPropertyEvaBuf(int pro) {
    	int ret = 0;
    	Buf buf = getBuf(Buf.CAMP_EVA);		//鉴定加成
    	if(buf == null) return 0;
    	for (int i = 0; i < usedEquipments.length; i++) {
    		if (usedEquipments[i] != null) {
    			IEquipment equ = (IEquipment)usedEquipments[i].item;
    			if(equ.isValid()){
    				ret += equ.getDiamondProperty(pro, getLevel());
    			}
    		}
    	}
    	return ret * buf.getValue() / 100;
    }
    
    /**
     * 获得当前装备的附魔属性
     * @param pro
     * @return
     */
    public int getUsedEquipmentEnchantingProperty(int pro){
    	int ret = 0;
        for (int i = 0; i < usedEquipments.length; i++) {
            if (usedEquipments[i] != null) {
                IEquipment equ = (IEquipment) usedEquipments[i].item;
                if (equ.isValid())
                    ret += equ.getEnchanting().getProperty(pro);
            }
        }
        return ret;
    }

    public void adjustProperty() {
        int maxHp = calculateMaxHp();
        int maxMp = calculateMaxMp();
        setMaxHp(maxHp);
        setMaxMp(maxMp);
        if (getHp() > getMaxHp())
            setHp(getMaxHp());
        if (getMp() > getMaxMp())
            setMp(getMaxMp());
        //repairOldVersionHpMp(clientDataVersion);
    }
	
    public IEquipment getEquipment(int itemId, int id) {
        for (int i = 0; i < equipments.size(); i++) {
            Grid grid = (Grid) equipments.get(i);
            if (grid.item.getItemId() == itemId && grid.item.getId() == id)
                return (IEquipment) grid.item;
        }
        return null;
    }

    public int getGridSize(int level) {
        return level / 3 + 40;
    }

    //剩余技能点
    public int getPoint() {
        return player.getPoint();
    }

    public void setPoint(int point) {
        player.setPoint(point);
    }

    //技能点,升级以后需要手动增加此属性
    public int getAbilityPoints() {
        return player.getAbilityPoints();
    }

    public void setAbilityPoints(int abilityPoints) {
        player.setAbilityPoints(abilityPoints);
    }

    public void addAbilityPoint(int type) {
        int p[] = new int[4];
        for (int i = 0; i < 4; i++) {
            p[i] = getAbilityPoint(i);
        }
        p[type]++;
        player.setAbilityPoints(p[3] << 24 | p[2] << 16 | p[1] << 8 | p[0]);
        setPoint(getPoint() - 1);
    }

    
    public void addAbilityMorePoint(int type, int Count) {
        int p[] = new int[4];
        for (int i = 0; i < 4; i++) {
            p[i] = getAbilityPoint(i);
        }
        p[type] = p[type] + Count;
        player.setAbilityPoints(p[3] << 24 | p[2] << 16 | p[1] << 8 | p[0]);
        setPoint(getPoint() - Count);
    }
    public int getAbilityPoint(int type) {
        int points = getAbilityPoints();
        return (points >> (8 * type)) & 0x00000FF;
    }

    public byte[] getTaskSaveData(short[] taskIds, ETFFile[] taskFiles) {
        TaskSaveDataBean taskSaveBean = new TaskSaveDataBean();
        taskSaveBean.updateData(getTaskData());

        TaskSaveDataBean cutSaveBean = new TaskSaveDataBean();
        for (int i = 0; i < taskIds.length; i++) {
            byte[] save = taskSaveBean.getTaskSaveData(taskIds[i]);
            if (save != null) {
                cutSaveBean.addTaskSave(taskIds[i], save);
                cutSaveBean.normalize(taskIds[i], taskFiles[i]);
            }
        }
        return cutSaveBean.getData();
    }

    public void updateTaskSaveData(byte[] taskSave) {
        TaskSaveDataBean tbean = new TaskSaveDataBean();
        tbean.updateData(getTaskData());
        tbean.updateData(taskSave);
        setTaskData(tbean.getData());
    }

//    public void deleteTaskSaveData(short taskId){
//        TaskSaveDataBean tbean = new TaskSaveDataBean();
//        tbean.updateData(getTaskData());
//        tbean.removeTaskSave(taskId);
//        setTaskData(tbean.getData());
//    }

    public Friend[] getFriends() {
        Friend[] ret = new Friend[friends.size()];
        friends.values().toArray(ret);
        return ret;
    }

    public int getFriendFavorite(PlayerData player){
        Friend friend = (Friend)friends.get(player.getId());
        if(friend==null)
            return 0;
        return friend.getFavorite();
    }
    
    public boolean hasFriend(PlayerData player){
        return friends.containsKey(new Integer(player.getId()));
    }

    //如果互为好友就加友好度，否则就不加
    public void tryAddFriendFavorite(PlayerData player,int favorite){
        Friend friend = (Friend)friends.get(new Integer(player.getId()));
        if(friend!=null&&player.hasFriend(this)){
            int oldFavorite = friend.getFavorite();
            friend.setFavorite(Math.min(oldFavorite+favorite,30000));
        }
    }

    public void tryAddFriendFavorite(PlayerData[] players,int favorite){
        for(int i=0;i<players.length;i++){
            if(players[i]!=null&&players[i].getId()!=getId()){
                tryAddFriendFavorite(players[i],favorite);
            }
        }
    }

    public void setFriendFavorite(PlayerData player,int favorite){
        Friend friend = (Friend)friends.get(new Integer(player.getId()));
        if(friend!=null&&favorite>0){
            friend.setFavorite(favorite);
        }
    }

//    public int getFriendFavorite(PlayerData player){
//        Friend friend = (Friend)friends.get(new Integer(player.getId()));
//        if(friend==null)
//            return 0;
//        return friend.getFavorite();
//    }


    public void setPet(Pet pet) {
        if (pet == null) {
            this.pet = null;
            player.setPetId( -1);
        }
        if (this.pet == pet) {
            return;
        } else {
            this.pet = pet;
            player.setPetId(pet.getId());
        }
    }

    public Pet getPet() {
        return pet;
    }

    public int getPetSize() {
        return player.getPetSize();
    }

    public void setPetSize(int petSize) {
        player.setPetSize(petSize);
    }

    public int getPetSize(int level) {
//        if (level > 0 && level <= 7)
//            return 0;
//        if (level >= 8 && level <= 20)
//            return 2;
//        if (level >= 21 && level <= 40) {
//            return 3;
//        }
//        if (level >= 41 && level <= 60) {
//            return 4;
//        }
//        if (level >= 61 && level <= 100)
//            return 5;
        return 10;//0;
    }

    public List getPetsList () {
    	return pets;
    }
    
    public int getPetCount() {
        return pets.size();
    }

    public Pet getPet(int id) {
        for (int i = 0; i < pets.size(); i++) {
            Pet p = (Pet) pets.get(i);
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    public boolean removePet(Pet p) {
        if (p == pet) {
            pet = null;
            player.setPetId( -1);
        }
        return pets.remove(p);
    }

    public Pet[] getPets() {
        Pet[] ret = new Pet[pets.size()];
        pets.toArray(ret);
        return ret;
    }
    
    public Pet[] getOetherPet(){
    	Pet[] ret = new Pet[pets.size() - (pet != null ? 1 : 0)];
    	for(int index=0, i=0; i<pets.size(); i++){
    		Pet p = (Pet)pets.get(i);
    		if(pet != null && p.getId() == pet.getId()){
    		}else{
    			ret[index++] = p;
    		}
    	}
    	return ret;
    }

    public void setValid(boolean value){
        player.setValid(value);
    }
    //进行限制性的减忠诚度并返回应该跑的宠物
    public Pet[] removeLimitPetFavor(int value, Changed changed, Random rnd) {
    	Pet[] tempPets = new Pet[5];
    	int count = 0;
        for (int i = 0; i < pets.size(); i++, count++) {
            Pet pet = (Pet) pets.get(i);
            if(pet.getFavor() <= 17){						
            	if (Utils.hit(rnd, 5000, 10000)) {//
            		tempPets[count] = pet;
            	}
            	continue;
            }else{
            	removeFavor(pet, value, changed, rnd);
            }
        }
        return tempPets;
    }
    /**
     * 雇佣了管家的时候，宠物的忠诚度加1
     * @param value
     * @return
     */
    public void addPetFavor(int value,Changed changed,Random rnd) {
        for (int i = 0; i < pets.size(); i++) {
            Pet pet = (Pet) pets.get(i);
            removeFavor(pet, value, changed, rnd);
        }
    }

    /**
     * 取消宠物可以逃跑的设定，此方法返回所有能逃跑的宠物
     * @param value
     * @param changed
     * @param rnd
     * @return
     */
    public String removeCurPetFavor(int value, Changed changed, Random rnd) {
    	if(getVipNewLevel() > 0){
    		return null;
    	}
    	String petName = null;
    	int petId = player.getPetId();
    	if(petId != -1){
            for (int i = 0; i < pets.size(); i++) {
                Pet pet = (Pet) pets.get(i);
                if(pet.getId() == petId){
                	removeFavor(pet, value, changed, rnd);
                    if(pet.getFavor() <= 15 ){
                    	petName = pet.getName();
                    }
                }
            }
    	}
        return petName;
    }

    public void removeFavor(Pet pet, int value, Changed changed, Random rnd) {
        int oldFavor = pet.getFavor();
        int newFavor = Math.max(oldFavor - value, 0);
        if(newFavor > 100){				//忠诚度最高为100
        	newFavor = 100;
        }
        pet.setFavor(newFavor);
        if (changed != null) {
            changed.addPetProperty(pet, Changed.PET_FAVOR, newFavor - oldFavor);
        }
        /**取消宠物逃跑的限制*/
//        if (newFavor <= 15) {
//            if (Utils.hit(rnd, 5000, 10000)) {
//                removePet(pet);
//                log.info("Player [" + this.getId() + "] Pet [" + pet.toDbBytes_version3() + "] RunAway");
//                if (changed != null)
//                    changed.addItem(pet, -1);
//            }
//        }
    }

    public void removeUsedEquipmentDurability(IEquipment equ, int value,
                                              Changed changed) {
        int oldValue = equ.getCurrentDurability();
        if (oldValue > 0) {
            int durability = Math.max(0, (oldValue - value));
            equ.setCurrentDurability((short) durability);
            if (changed != null) {
                changed.addDurability(equ, equ.getCurrentDurability());
            }
            if (durability == 0)
                adjustProperty();
        }
    }
    
    public byte updateEquipmentProperty (IEquipment equ, Changed changed) {
    	byte recalculate = -1;
        int length = usedEquipments.length;
    	for (int i = 0; i < length; i++) {
            if (usedEquipments[i] != null && usedEquipments[i].item.getId() == equ.getId() && usedEquipments[i].item.getItemId() == equ.getItemId()) {
            	recalculate = PLAYER_USE_EQUIPMENT;
            	break;
            }
        }
    	Pet p = getPet();
    	if (p != null) {
    		Grid[] petUsedEquipments = p.getUsedEquipments();
    		length = petUsedEquipments.length;
    		for (int i = 0; i < length; i++) {
    			if (petUsedEquipments[i] != null && petUsedEquipments[i].item.getId() == equ.getId() && petUsedEquipments[i].item.getItemId() == equ.getItemId()) {
    				recalculate = PET_USE_EQUIPMENT;
    				break;
    			}
    		}
    	}
    	if (changed != null) {
    		changed.updatEquipmentProperty(equ, recalculate, recalculate == -1 ? -1 : getLevel());
    	}
    	return recalculate;
    }

    public void addUsedEquipmentDurability(IEquipment equ) {
        int oldValue = equ.getCurrentDurability();
        equ.setCurrentDurability(equ.getDurability());
        if (oldValue == 0)
            adjustProperty();
    }

    public boolean tryAddPetExp(int exp, Changed changed) {
        Pet p = getPet();
        if (p != null) {
            int nExp = p.getExp() + exp;
            int upExp = Utils.getPetUpLevelExp(p.getLevel());
            
            int nExp1 = nExp;
            int upExp1 = upExp;
            int upLevel = 0;
            
            while(nExp1 >= upExp1 && pet.getLevel() + upLevel < getLevel()){
            	nExp1 -= upExp1;
            	upLevel++;
            	upExp1 = Utils.getPetUpLevelExp(p.getLevel() + upLevel);
            }
            
            if(upLevel > 1){
            	int tmpLevel = p.getLevel() + upLevel - 1;
            	nExp = nExp1 + Utils.getPetUpLevelExp(tmpLevel);
            	upExp = Utils.getPetUpLevelExp(tmpLevel);
            	p.setLevel(tmpLevel);
            }

            if (nExp >= upExp && ((pet.getLevel() + 1) <= getLevel())) {
                pet.setLevel(pet.getLevel() + 1);
                pet.setExp(nExp - upExp);
                int oldHp = pet.getHp();
                int oldMp = pet.getMp();
                pet.setMp(pet.getMaxMp());
                pet.setHp(pet.getMaxHp());
//                pet.setPoint(pet.getPoint() + 4);
                pet.setCurrentPoint(pet.getCurrentPoint()+4*upLevel);
                pet.setNextExp(Utils.getPetUpLevelExp(pet.getLevel()));
                if (changed != null) {
                	IEquipment[] equs = pet.getUsedEquipments2();
                	if(equs != null){
                		for(int i=0; i<equs.length; i++){
                			if(equs[i] != null && equs[i].isGrow()){
                				changed.updatEquipmentProperty(equs[i], PET_USE_EQUIPMENT, pet.getLevel());
                			}
                		}
                	}
                    changed.addPetProperty(p, Changed.PET_LEVEL, upLevel);
                    changed.addPetProperty(p, Changed.PET_EXP, exp);
                    changed.addPetProperty(p, Changed.PET_UPLEVELEXP,
                                           (nExp - upExp));
                    changed.addPetProperty(p, Changed.PET_HP, pet.getHp());
//                                           (pet.getHp() - oldHp));
                    changed.addPetProperty(p, Changed.PET_MP, pet.getMp());
//                                           (pet.getMp() - oldMp));
//                    changed.addPetProperty(p, changed.PET_POINT, 4);
                    changed.addPetProperty(p, changed.PET_CURRENTPOINT, 4*upLevel);
                    changed.addPetProperty(p, Changed.PET_NEXT_LEVEL_EXP, Utils.getPetUpLevelExp(pet.getLevel()));
                }
                return true;
            } else {
                int oldExp = pet.getExp();
                nExp = Math.min(upExp-1,nExp);  //不能超过最高升级点数
                int Exptmp = nExp - pet.getExp();
                pet.setExp(nExp);
                if (changed != null)
                    changed.addPetProperty(p, Changed.PET_EXP, Exptmp);
                if(nExp>oldExp)
                    return true;
                return false;
            }
        }
        return false;
    }

    public boolean tryAddPetExp(Pet p, int exp, Changed changed) {
        if (p != null) {
            int nExp = p.getExp() + exp;
            int upExp = Utils.getPetUpLevelExp(p.getLevel());
            
            int nExp1 = nExp;
            int upExp1 = upExp;
            int upLevel = 0;
            
            while(nExp1 >= upExp1 && p.getLevel() + upLevel < getLevel()){
            	nExp1 -= upExp1;
            	upLevel++;
            	upExp1 = Utils.getPetUpLevelExp(p.getLevel() + upLevel);
            }
            
            if(upLevel > 1){
            	int tmpLevel = p.getLevel() + upLevel - 1;
            	nExp = nExp1 + Utils.getPetUpLevelExp(tmpLevel);
            	upExp = Utils.getPetUpLevelExp(tmpLevel);
            	p.setLevel(tmpLevel);
            }

            if (nExp >= upExp && ((p.getLevel() + 1) <= getLevel())) {
                p.setLevel(p.getLevel() + 1);
                p.setExp(nExp - upExp);
                int oldHp = p.getHp();
                int oldMp = p.getMp();
                p.setMp(p.getMaxMp());
                p.setHp(p.getMaxHp());
//                pet.setPoint(pet.getPoint() + 4);
                p.setCurrentPoint(p.getCurrentPoint()+4*upLevel);
                p.setNextExp(Utils.getPetUpLevelExp(p.getLevel()));
                if (changed != null) {
                    changed.addPetProperty(p, Changed.PET_LEVEL, upLevel);
                    changed.addPetProperty(p, Changed.PET_EXP, exp);
                    changed.addPetProperty(p, Changed.PET_UPLEVELEXP,
                                           (nExp - upExp));
                    changed.addPetProperty(p, Changed.PET_HP, p.getHp());
//                                           (pet.getHp() - oldHp));
                    changed.addPetProperty(p, Changed.PET_MP, p.getMp());
//                                           (pet.getMp() - oldMp));
//                    changed.addPetProperty(p, changed.PET_POINT, 4);
                    changed.addPetProperty(p, changed.PET_CURRENTPOINT, 4*upLevel);
                    changed.addPetProperty(p, Changed.PET_NEXT_LEVEL_EXP, Utils.getPetUpLevelExp(p.getLevel()));
                }
                return true;
            } else {
                int oldExp = p.getExp();
                nExp = Math.min(upExp-1,nExp);  //不能超过最高升级点数
                int Exptmp = nExp - p.getExp();
                p.setExp(nExp);
                if (changed != null)
                    changed.addPetProperty(p, Changed.PET_EXP, Exptmp);
                if(nExp>oldExp)
                    return true;
                return false;
            }
        }
        return false;
    }
    
    public IEquipment getWeapon() {
        if (usedEquipments[7] == null) {
            return null;
        } else {
            return (IEquipment) (usedEquipments[7].item);
        }
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getAbilityTimes() {
        return player.getAbilityTimes();
    }

    public void setAbilityTimes(int abilityTimes) {
        player.setAbilityTimes(abilityTimes);
    }

    public int getUsedAbilityPoint() {
        int points = 0;
        for (int i = 0; i < 4; i++) {
            int point = getAbilityPoint(i);
            if (point > 0) {
                points += point;
            }
        }
        return points;
    }

    public void clearAbilities() {
        abilities.clear();
    }

    public void clearSkills(int type) {
        Iterator ite = recipes.iterator();
        while (ite.hasNext()) {
            Recipe r = (Recipe) ite.next();
            if (r.getType() == type) {
                ite.remove();
            }
        }
    }

    public void setOption(short[] option) {
        this.options = option;
    }

    public short[] getOption() {
        return options;
    }
    
    /**
     * 
     * @param chatChannel 聊天频道 ISendMessage
     * @return 
     */
    public int getMessageCount(int chatChannel) {
    	int tmpCount = player.getMessageCount();
    	switch(chatChannel){
    	case WORLD://世界聊    最后两位保存世界聊
    		return tmpCount%100;
    	case CAMP://阵营聊   前两位保存阵营聊count
    		return tmpCount/100;
    	}
    	return 0;
    }
    
    /**
     * 
     * @param chatChannel 聊天频道 ISendMessage
     */
    public void increaseMessageCount(int chatChannel) {
    	switch(chatChannel){
    	case WORLD://世界聊
    		player.setMessageCount(player.getMessageCount() + 1);
    		break;
    	case CAMP://阵营聊
    		player.setMessageCount(player.getMessageCount()+100);
    		break;
    	}
    }

    public void setMessageCount(int count) {
        player.setMessageCount(count);
    }

    public Date getLastMessageTime() {
        return player.getLastMessageTime();
    }

    public void setLastMessageTime(Date time) {
        player.setLastMessageTime(time);
    }

    public boolean isLuckyTime(){
    	long now = System.currentTimeMillis();
    	if(lucktime > now){
    		return true;
    	}
        return Utils.isLuckyTime(siderealTime);
    }

    public int getSiderealTime(){
        return siderealTime;
    }

    public Date getBathHouseTime(){
        return player.getBathHouseTime();
    }

    public void setBathHouseTime(Date time){
        player.setBathHouseTime(time);
    }


    public Date getVipBathHouseTime(){
        return player.getVipBathHouseTime();
    }

    public void setVipBathHouseTime(Date time){
        player.setVipBathHouseTime(time);
    }
//    public void setLuckyBuf(LuckyBuf luckyBuf) {
//        this.luckyBuf = luckyBuf;
//    }
//
//    public LuckyBuf getLuckyBuf() {
//        if (luckyBuf != null &&
//            luckyBuf.getValidTime() < System.currentTimeMillis()) {
//            luckyBuf = null;
//        }
//        return luckyBuf;
//    }


//    public boolean isLuckyTime(){
//        return false;
//    }

    public int getCreditIndex(){
        int credit = getCredit();
        for(int i=0;i<CREDIT.length-1;i++){
            if(credit>=CREDIT[i]&&credit<CREDIT[i+1])
                return i;
        }
        if(credit>=CREDIT[CREDIT.length-1])
            return CREDIT.length-1;
        return 0;
    }

    public long getLastkillmg() {
		return lastkillmg;
	}

	public void setLastkillmg(long lastkillmg) {
		this.lastkillmg = lastkillmg;
	}

	public int getKillmgcount() {
		return killmgcount;
	}

	public void setKillmgcount(int killmgcount) {
		this.killmgcount = killmgcount;
	}

	public long getKillmgtime() {
		return killmgtime;
	}

	public void setKillmgtime(long killmgtime) {
		this.killmgtime = killmgtime;
	}

	public int getKillmgpasscount() {
		return killmgpasscount;
	}

	public void setKillmgpasscount(int killmgpasscount) {
		this.killmgpasscount = killmgpasscount;
	}

	public long getKillmgpasstime() {
		return killmgpasstime;
	}

	public int getKillmgtotalcount() {
		return this.killmgtotalcount;
	}

	public void setKillmgtotalcount(int killmgtotalcount) {
		this.killmgtotalcount = killmgtotalcount;
	}

	public void setKillmgpasstime(long killmgpasstime) {
		this.killmgpasstime = killmgpasstime;
	}

	public int getKillmgerrorpro() {
		return killmgerrorpro;
	}

	public void setKillmgerrorpro(int killmgerrorpro) {
		this.killmgerrorpro = killmgerrorpro;
	}

	public int getPositiontimes() {
		return positiontimes;
	}

	public void setPositiontimes(int positiontimes) {
		this.positiontimes = positiontimes;
	}

	public String getCreditName(){
        return CREDIT_NAME[creditIndex];
    }

    public void setTitle(String title){
        player.setTitle(title);
    }

    public String getTitle(){
        return player.getTitle();
    }

    public short getJumpMapId(){
        return player.getJumpMapId();
    }

    public void setJumpMapId(short jumpMapId){
        player.setJumpMapId(jumpMapId);
    }

    public short getJumpX(){
        return player.getJumpX();
    }

    public void setJumpX(short jumpX){
        player.setJumpX(jumpX);
    }

    public short getJumpY(){
        return player.getJumpY();
    }

    public void setJumpY(short jumpY){
        player.setJumpY(jumpY);
    }

    public void setConsumePoint(int point){
        player.setConsumePoint(point);
    }

    public int getConsumePoint(){
        return player.getConsumePoint();
    }

    public void setContribution(int contribution){
        player.setContribution(contribution);
    }

    public int getContribution(){
        return player.getContribution();
    }

    public String toString(){
        StringBuffer buff = new StringBuffer(400);
        buff.append("ID[");
        buff.append(player.getId());
        buff.append("]Level[");
        buff.append(player.getLevel());
        buff.append("]Money[");
        buff.append(player.getMoeny());
        buff.append("]CREDIT[");
        buff.append(player.getCredit());
        buff.append("]Exp[");
        buff.append(player.getExp());
        buff.append("]Agility[");
        buff.append(player.getAgility());
        buff.append("]Strength[");
        buff.append(player.getStrength());
        buff.append("]Vitality[");
        buff.append(player.getVitality());
        buff.append("]Intelligence[");
        buff.append(player.getIntelligence());
        buff.append("]");
        buff.append("StageID[");
        buff.append(player.getMapId() >> 4);
        buff.append("]MapID[");
        buff.append(player.getMapId());
        buff.append("]x[");
        buff.append(player.getX());
        buff.append("]y[");
        buff.append(player.getY());
        buff.append("]Tasks[");
        for (int i = 0; i < currentTasks.size(); i++) {
        	buff.append(currentTasks.get(i));
        	if(i < currentTasks.size() - 1){
        		buff.append(",");
        	}
        }
        buff.append("]");
        return buff.toString();
    }

    public int getModifyNameTimes() {
        return player.getModifyNameTimes();
    }

    public void setModifyNameTimes(int modifyNameTimes) {
        player.setModifyNameTimes(modifyNameTimes);
    }

    public void addKills(int num){
        player.setKills(player.getKills() + num);
    }

    public void addSneaks(int num){
        player.setSneaks(player.getSneaks() + num);
    }

    public void killDayEnd(){
        player.setLastKills(player.getKills());
        player.setKills(0);
    }

    public void sneakDayEnd(){
        player.setLastSneaks(player.getSneaks());
        player.setSneaks(0);
    }

    public void addEnemy(int id,String name,long lastTime){
        Enemy oldest = null;
        for(int i=0;i<enemys.size();i++){
            Enemy enemy = (Enemy)enemys.get(i);
            if(enemy.id==id){
                enemy.name = name;
                enemy.times++;
                enemy.lastTime = lastTime;
                return;
            }else{
                if(oldest==null){
                    oldest = enemy;
                }else{
                    if(oldest.lastTime < enemy.lastTime){
                        oldest = enemy;
                    }
                }
            }
        }
        if(enemys.size() < 50){
            Enemy e = new Enemy(id,name,1,lastTime);
            enemys.add(e);
        }else{
            if(oldest!=null){
                oldest.id = id;
                oldest.name = name;
                oldest.times = 1;
                oldest.lastTime = lastTime;
            }
        }
    }

    public Enemy[] getEnemys(){
        Enemy[] ret = new Enemy[enemys.size()];
        enemys.toArray(ret);
        return ret;
    }
    
    public boolean deleteEnemy(int playerid){
    	for(int i=0; i<enemys.size(); i++){
    		Enemy enemy = (Enemy)enemys.get(i);
    		if(enemy.id == playerid){
    			enemys.remove(i);
    			return true;
    		}
    	}
    	return false;
    }

    public int getBoxCount(){
        return player.getBoxCount();
    }

    public void addBoxCount(int value){
        player.setBoxCount(player.getBoxCount()+value);
    }

    public Date getIslandItemTime(){
        return player.getIslandItemTime();
    }

    public void setIslandItemTime(Date time){
        player.setIslandItemTime(time);
    }
    public Date getIbuylastTime(){
        return player.getIbuylastTime();
    }
    public void setIbuylastTime(Date time){
        player.setIbuylastTime(time);
    }
    //mengjie add
    public Date getTonginTime(){
        return player.getTonginTime();
    }
    public void setTonginTime(Date time){
        player.setTonginTime(time);
    }
    public byte[] getAbilities() {
        return player.getAbilities();
    }
    public int getArenaV1Id() {
		return player.getArenaV1Id();
	}
    public void setArenaV1Id(int arenaV1Id) {
    	player.setArenaV1Id(arenaV1Id);
	}
    public int getArenaV2Id() {
		return player.getArenaV2Id();
	}
    public void setArenaV2Id(int arenaV2Id) {
    	player.setArenaV2Id(arenaV2Id);
	}
    public int getArenaV3Id() {
		return player.getArenaV3Id();
	}
    public void setArenaV3Id(int arenaV3Id) {
    	player.setArenaV3Id(arenaV3Id);
	}
    public int getArenaLevel() {
		return player.getArenaLevel();
	}
    public void setArenaLevel(int arenaLevel) {
    	player.setArenaLevel(arenaLevel);
	}
    public int getArenaLevel2() {
		return player.getArenaLevel2();
	}
    public void setArenaLevel2(int arenaLevel2) {
    	player.setArenaLevel2(arenaLevel2);
	}
    public int getArenaLevel3() {
		return player.getArenaLevel3();
	}
    public void setArenaLevel3(int arenaLevel3) {
    	player.setArenaLevel3(arenaLevel3);
	}
    public int getArenaPoint() {
		return player.getArenaPoint();
	}
    public void setArenaPoint(int arenaPoint) {
    	player.setArenaPoint(arenaPoint);
	}
    public Date getLastlogoutTime() {
		return player.getLastlogoutTime();
	}
	public void setLastlogoutTime(Date lastlogoutTime) {
		player.setLastlogoutTime(lastlogoutTime);
	}
	
	public byte[] getUseskill() {
		return player.getUseskill();
	}
	public void setUseskill(byte[] useskill) {
		player.setUseskill(useskill);
	}
	public byte getCamp() {
		return player.getCamp();
	}
	public void setCamp(byte camp) {
		player.setCamp(camp);
	}
	public int getCampwin() {
		return player.getCampwin();
	}
	public void setCampwin(int campwin) {
		player.setCampwin(campwin);
	}
	
	public void addCampWin(int win){
		player.setCampwin(player.getCampwin() + win);
	}
	
	public void addCampLost(int lost){
		player.setCamplost(player.getCamplost() + lost);
	}
	
	public int getCamplost() {
		return player.getCamplost();
	}
	public void setCamplost(int camplost) {
		player.setCamplost(camplost);
	}
	public int getCampcredit() {
		return player.getCampcredit();
	}
	public void setCampcredit(int campcredit) {
		player.setCamplost(campcredit);
	}
	
	public Date getEndVoteTime() {
		return player.getEndVoteTime();
	}
	
	public void setEndVoteTime(Date endVoteTime) {
		player.setEndVoteTime(endVoteTime);
	}
//    public void removeAttachment(byte[] bytes){
//        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
//        DataInputStream dis = new DataInputStream(bis);
//        byte type = dis.readByte();
//        Changed change = new Changed();
//        if(type==1){ //money
//            int money = dis.readInt();
//            int oldMoney = player.getMoeny();
//            if(oldMoney>=money){
//                player.setMoeny(oldMoney-money);
//                change.addProperty(Changed.MONEY,money);
//            }else
//                throw new Exception("钱不够");
//        }
//        else if(type==2){ //basicItem
//            int id = dis.readInt();
//            byte count = dis.readByte();
//            IItem item = Items.getItem(id);
//            boolean b = player.descItem(item,count);
//            if(b){
//                change.addItem(id,count);
//            }else{
//                throw new Exception("物品不够");
//            }
//        }
//        else if(type==3){ //taskItem
//            String s = dis.readUTF();
//            byte count = dis.readByte();
//            IItem item = Items.getTaskItem(s);
//            boolean b= player.descItem(item,count);
//            if(b){
//                change.addItem(item.getItemId(),count);
//            }else{
//                throw new Exception("物品不够");
//            }
//        }
//        else if(type==4){ //extendedItem
//            int id = dis.readInt();
//            byte count = dis.readByte();
//            IItem item = Items.getItem(id);
//            boolean b = player.descItem(item,count);
//            if(b){
//                change.addItem(id,count);
//            }else{
//                throw new Exception("物品不够");
//            }
//
//        }
//        else if(type==5){ //equip
//            int itemId = dis.readInt();
//            int id = dis.readInt();
//            Equipment equ = player.deleteEquipment(itemId,id);
//            if(equ!=null){
//                change.addEquipment(equ);
//            }else{
//                throw new Exception("物品不够");
//            }
//        }
//
//        Object[] o = change.toClientBytes();
//        if(o.length==1){
//            player.reset();
//            playerService.savePlayer(player.getPlayer());
//            return (byte[])o[0];
//        }
//
//        return null;
//    }

	public int getLastmgid() {
		return lastmgid;
	}

	public void setLastmgid(int lastmgid) {
		this.lastmgid = lastmgid;
	}

	public int getKillmgidcount() {
		return killmgidcount;
	}

	public void setKillmgidcount(int killmgidcount) {
		this.killmgidcount = killmgidcount;
	}

	public int[] getKey9_options() {
		return key9_options;
	}

	public void setKey9_options(int[] key9Options) {
		this.key9_options = key9Options;
	}
	
    public int getSkillPoint2() {
		return skillPoint2;
	}

	public void setSkillPoint2(int skillPoint2) {
		this.skillPoint2 = skillPoint2;
	}

	public final static byte taskVersion = 1;
	
	//lisen add 
	public List getOldBufPropertyList(){
		List tmpList=new ArrayList(3);
		for(int i=0;i<bufs.size();i++){
			Buf tmp=(Buf)bufs.get(i);
			tmpList.add(tmp.getProperty());
		}
		return tmpList;
	}
	public byte getOldBufProperty(List tmpList,byte pro){
		if(pro ==-1 && tmpList.size()>0){
			byte tmpPro=((Byte)tmpList.get(0)).byteValue();
			tmpList.remove(0);
			return tmpPro;
		}
		for(int i=0;i<tmpList.size();i++){
			byte tmpPro=((Byte)tmpList.get(i)).byteValue();
			if(tmpPro==pro){
				tmpList.remove(i);
				return tmpPro;
			}
		}
		return -1;
	}
	public int[] getNewBuf(Effect[] effects){
		int buffPro[]=new int[3];
		int j=0;
		int tmpIndex=0;
		List tmpBufPro=getOldBufPropertyList();
		for(int i=0;i<effects.length;i++){
			switch(effects[i].getType()){
			case 1:
				PropertyEffect effect = (PropertyEffect) effects[i];
                if (effect.getTime() != 0 && effect.getProperty()>0) {
                	buffPro[j++]=getOldBufProperty(tmpBufPro,effect.getProperty());
                }
				break;
			case 46:
				SaveShieldEffect effectSS = (SaveShieldEffect)effects[i];
				if(effectSS.getTime()!=0 && effectSS.getProperty()>0){
					buffPro[j++]=getOldBufProperty(tmpBufPro,effectSS.getProperty());
				}
				break;
			}
			if(j>=buffPro.length){
                break;
            }
		}
		int tmp=buffPro.length-bufs.size();
		for(int i=0;i<buffPro.length;i++){
			if(buffPro[i]==-1){
				if(tmp<=0){
					buffPro[i]=getOldBufProperty(tmpBufPro,(byte)-1);
				}else{
					tmp--; //控制添加个数
				}
			}
		}
		return buffPro;
	}
	
	public int addBufNew(Buf buf, Changed change, int buffPro[], int index) {
		if (buf.getTime() == 0) {
			return index;
		}
		boolean b = false;
		if (buf.getProperty() < 0) { // 扩展buf,相同的替换，不同的加到末尾，没有个数限制，并且不会通知客户端
			for (int i = 0; i < extendedBufs.size(); i++) {
				Buf aBuf = (Buf) extendedBufs.get(i);
				if (aBuf.getProperty() == buf.getProperty()) {
					extendedBufs.set(i, buf);
					b = true;
					break;
				}
			}
			if (!b) {
				extendedBufs.add(buf);
			}
			return index;
		} else { // 属性buf,相同的替换，不同的加到末尾，但是有个数限制，会通知客户端buf状态改变
			if(index>=buffPro.length){
				return index;
			}
			if (buffPro[index] == -1) {
				bufs.add(buf);
				if (change != null)
					change.addBuf(buf);
			} else {
				if(buffPro[index]==0)
					return index;
				if (bufs.size() > 0) {
					boolean hassame = false;
					int qsize = bufs.size();
					for(int q=0; q<qsize; q++){
						Buf tmpbuf = (Buf)bufs.get(q);
						if(tmpbuf.getProperty() == buf.getProperty()){
							hassame = true;
							if(tmpbuf.getValue() > buf.getValue()){//本身存在buff值大于新加buff值,不替换原有buff
							}else{
								bufs.remove(tmpbuf);//删旧buff
								bufs.add(buf);//加新buff
								if (change != null) {
									change.addBuf(buf);
									change.addRemovedBuff(tmpbuf);
								}
							}
							break;
						}
					}
					if(!hassame){//不存在相同类型buff
						bufs.add(buf);
						if (change != null) {
							change.addBuf(buf);
						}
					}
				} else {
					bufs.add(buf);
					if (change != null)
						change.addBuf(buf);
				}
			}
		}
		if (buf.getProperty() > 0)
			adjustProperty();
		return index+1;
	}
	//lisen add end
	
	/**
     * 玩家形象的时效形象到期时，设置玩家为默认形象(无阵营的玩家设置成默认勇士装，有阵营的玩家设置成阵营默认形象)
     * @param player
     */
    public void setDefaultFace () {
    	if (getSex() == MALE){		// 男性
	    	if (getCamp() == Utils.CAMP_DARK) {
	    		setFace((short)Utils.MALE_DARK_CAMP_FACE_ID);
	    	} else if (getCamp() == Utils.CAMP_BRIGHT) {
	    		setFace((short)Utils.MALE_BRIGHT_CAMP_FACE_ID);
	    	} else if (getCamp() == Utils.NO_CAMP) {
	    		setFace((short)Utils.MALE_WARRIORS_DEFAULT_FACE_ID);
	    	}
    	} else {					// 女性
	    	if (getCamp() == Utils.CAMP_DARK) {
	    		setFace((short)Utils.FEMALE_DARK_CAMP_FACE_ID);
	    	} else if (getCamp() == Utils.CAMP_BRIGHT) {
	    		setFace((short)Utils.FEMALE_BRIGHT_CAMP_FACE_ID);
	    	} else if (getCamp() == Utils.NO_CAMP) {
	    		setFace((short)Utils.FEMALE_WARRIORS_DEFAULT_FACE_ID);
	    	}
        }
    }
    
    // 给宠物加悟性
    public boolean addPetPerceptionPoint (int petId, int exp, Changed changed) {
    	Pet p = getPet(petId);
        if (p != null) {
            int nPoint = p.getPerceptionPoint() + exp;
            int upPoint = Utils.getPetUpLevelPerceptionPoint(p.getPerceptionLevel());
            
            int nPoint1 = nPoint;
            int upPoint1 = upPoint;
            int upPerceptionLevel = 0;
            
            while (nPoint1 >= upPoint1) {
            	nPoint1 -= upPoint1;
            	upPerceptionLevel++;
        		upPoint1 = Utils.getPetUpLevelPerceptionPoint(p.getPerceptionLevel() + upPerceptionLevel);
            }
            
            if (upPerceptionLevel > 1) {
            	int tmpPerceptionLevel = p.getPerceptionLevel() + upPerceptionLevel - 1;
            	nPoint = nPoint1 + Utils.getPetUpLevelPerceptionPoint(tmpPerceptionLevel);
            	upPoint = Utils.getPetUpLevelPerceptionPoint(tmpPerceptionLevel);
            	p.setPerceptionLevel(tmpPerceptionLevel);
            }

            if (nPoint >= upPoint) {
            	if (p.getPerceptionLevel() + 1 >= Utils.PET_MAX_PERCEPTION_LEVEL) {
            		p.setPerceptionLevel(p.getPerceptionLevel() + 1);
	        		p.setPerceptionPoint(0);
	        		p.setNextPerceptionPoint(0);
	                if (changed != null) {
	                    changed.addPetProperty(p, Changed.PET_PERCETPION_LEVEL, upPerceptionLevel);
	                    changed.addPetProperty(p, Changed.PET_PERCETPION_POINT, 0);
	                    changed.addPetProperty(p, Changed.PET_UPLEVELPERCETPIONPOINT,
	                                           0);
	                    changed.addPetProperty(p, Changed.PET_NEXT_PERCEPTION_POINT, 0);
	                }
            	} else {
	        		p.setPerceptionLevel(p.getPerceptionLevel() + 1);
	        		p.setPerceptionPoint(nPoint - upPoint);
	        		p.setNextPerceptionPoint(Utils.getPetUpLevelPerceptionPoint(p.getPerceptionLevel()));
	                if (changed != null) {
	                    changed.addPetProperty(p, Changed.PET_PERCETPION_LEVEL, upPerceptionLevel);
	                    changed.addPetProperty(p, Changed.PET_PERCETPION_POINT, exp);
	                    changed.addPetProperty(p, Changed.PET_UPLEVELPERCETPIONPOINT,
	                                           (nPoint - upPoint));
	                    changed.addPetProperty(p, Changed.PET_NEXT_PERCEPTION_POINT, Utils.getPetUpLevelPerceptionPoint(p.getPerceptionLevel()));
	                }
            	}
                return true;
            } else {
                int oldExp = p.getPerceptionPoint();
                nPoint = Math.min(upPoint-1, nPoint);  //不能超过最高升级点数
                int Exptmp = nPoint - p.getPerceptionPoint();
                p.setPerceptionPoint(nPoint);
                if (changed != null) {
                	changed.addPetProperty(p, Changed.PET_PERCETPION_POINT, Exptmp);
                }
                if (nPoint > oldExp) {
                	return true;
                }
                return false;
            }
        }
        return false;
    }
    
    public void setPetSkillAndEnhanceName (int petId, int lastPetPerceptionLevel, Changed changed) {
    	Pet p = getPet(petId);
    	if (p != null) {
    		int count = Utils.getAddSkillCount(p.getPerceptionLevel(), lastPetPerceptionLevel);
        	if (count > 0) {
        		Ability[] abs = Utils.getAddPetAbilities(p.getAbilityId(), count);
        		for (int j = 0; j < abs.length; j++) {
        			p.addAbility(abs[j]);
        		}
        		changed.addPetAbility(p, Changed.PET_ADD_SKILL, abs);
        	}
        	if (p.getPerceptionLevel() - lastPetPerceptionLevel > 0) {
        		p.setEnhanceName(Utils.getPerceptionLevelName(p.getPerceptionLevel()));
        		String newName = p.getName();
        		if(p.getBindType() > 0){
					newName = newName.concat("(" + (p.getBindType() + 1) + "代)");
				}
        		if (p.getEnhanceName().equals("") && p.getEnhanceName().length() == 0) {
        			changed.addPetProperty(p, Changed.PET_NAME,
        					newName);
        		} else {
        			changed.addPetProperty(p, Changed.PET_NAME,
        					newName + p.getEnhanceName());
        		}
        	}
    	}
    }
    
    /**
     * 设置佣兵数据
     * @param mercentaryData
     */
    public void setMercentaryData(String mercentaryData){
    	if(mercentaryData == null || mercentaryData.equals("")){
    		return;
    	}
    	String[] mdata = mercentaryData.split(",");
    	//购买的佣兵个数
    	int mcount = Integer.parseInt(mdata[0]);
    	for(int i=1; i<=mcount; i++){
    		int id = Integer.parseInt(mdata[i]);
    		mercenaryid.put(id, null);
    	}
    }
    
    /**
     * 重置佣兵数据
     */
    public void resetMercentaryData(){
    	StringBuilder sb = new StringBuilder();
    	int mcount = mercenaryid.size();
    	if(mcount == 0){
    		sb.append("");
    	}else{
	    	sb.append(mcount);
	    	Iterator<Integer> iter = mercenaryid.keySet().iterator();
	    	while(iter.hasNext()){
	    		int id = iter.next();
	    		sb.append(",");
	    		sb.append(id);
	    	}
    	}
    	getOtherPool().setString(MERCENTARY, sb.toString());
    }
    
    public HashMap<Integer, Integer> getMercenaryId(){
    	return mercenaryid;
    }
    
    public void addMercenaryId(int id){
    	mercenaryid.put(id, null);
    }
    
    public void removeMercenaryId(int id){
    	if(mercenaryid.containsKey(id)){
    		mercenaryid.remove(id);
    	}
    }
    
    /**
     * 设置获取最后获取统御值的时间 
     * @param getLeadershipDate
     */
    public void setGetLeadershipDate (Date getLeadershipDate) {
    	this.getLeadershipDate = getLeadershipDate;
    }
    
    /**
     * 设置下一次可以出售佣兵的时间
     * @param sellNextDate
     */
    public void setSellNextDate (Date sellNextDate) {
    	this.sellNextDate = sellNextDate;
    }
    
    public void setLandTimes(int iTimes){
    	this.iTimes = iTimes;
    }
    
    public void setClockUse(int clockUse){
    	this.clockUse = clockUse;
    }
    
    public void resetLandTimes(){
    	getOtherPool().setInt(LANDTIMES, iTimes);
    }
    public int getLandTimes(){
    	return iTimes; 
    	
    }
    
    
    public void setLandLastDate (Date landLastDate) {
    	this.landLastDate = landLastDate;
    }
    
    public void setClock(Date clock){
    	this.clock = clock;
    }
    
    public Date getClock(){
    	return clock;
    }
    
    public int getClockUse(){
    	return clockUse;
    }
    
    public Date getClock_TongShop(){
    	return clock_tongShop;
    }
    
    public int getClockUse_TongShop(){
    	return clockUse_tongShop;
    }
    
    public void setClock_TongShop(Date clock){
    	this.clock_tongShop = clock;
    }
    
    public void setClockUse_TongShop(int clockUse){
    	this.clockUse_tongShop = clockUse;
    }
    
    public void resetLandLastDate () {
    	getOtherPool().setString(LANDLASTTIME, format.format(landLastDate));
    }
    
    public void resetClock(){
    	getOtherPool().setString(CLOCK, format.format(clock));
    }
    
    public void resetClock_TongShop(){
    	getOtherPool().setString(CLOCK_TONGSHOP, format.format(clock_tongShop));
    }
    
    public Date getLandLastDate(){
    	return landLastDate;
    }
    
    public Date getGetLeadershipDate(){
    	return getLeadershipDate;
    }
    
    public void resetGetLeadershipDate () {
    	getOtherPool().setString(GETLEADERSHIPTIME, format.format(getLeadershipDate));
    }
    
    public void resetSellNextDate () {
    	getOtherPool().setString(SELLNEXTTIME, format.format(sellNextDate));
    }
    
    public Date getSellNextDate(){
    	return sellNextDate;
    }
    
    public final static byte AUTOMIX_TYPE = 3;
    public final static byte AUTOMIX_LENGTH = 3;
   
    
    public boolean setAutoMixData(int[][] arrData, int itemID, int money){
    	autoMixData = null;
    	autoMixDiamondID = 0;
    	autoMixMoney_J = 0;
    	if(arrData==null || arrData.length != AUTOMIX_TYPE){
    		return false;
    	}
    	int[][] tmpData = new int[AUTOMIX_TYPE][AUTOMIX_LENGTH];
    	for(int i=0;i<AUTOMIX_TYPE;i++){
    		if(arrData[i].length != AUTOMIX_LENGTH){
    			return false;
    		}
    		System.arraycopy(arrData[i], 0, tmpData[i], 0, AUTOMIX_LENGTH);
    	}
    	autoMixData = tmpData;
    	autoMixDiamondID = itemID;
    	autoMixMoney_J = money;
    	return true;
    }
    
    public void updateAutoMixData(int itemID,int count){
    	if(autoMixData== null){
    		return;
    	}
    	for(int i = 0; i<autoMixData.length;i++){
    		if(autoMixData[i][0]==itemID){
    			autoMixData[i][2] +=count;
    			break;
    		}
    	}
    }

//	public int getJettonNum(int index) {
//		int jettonNum = 0;
//		switch (index) {
//		case 0:
//			jettonNum = jettonNumFir;
//			break;
//		case 1:
//			jettonNum = jettonNumSec;
//			break;
//		case 2:
//			jettonNum = jettonNumThi;
//			break;
//		case 3:
//			jettonNum = jettonNumFou;
//			break;
//		case 4:
//			jettonNum = jettonNumFif;
//			break;
//		default:
//			break;
//		}
//		return jettonNum;
//	}
	
//	public int[] getJettonNums() {
//		int[] jettonNum = new int[5];
//		jettonNum[0] = jettonNumFir;
//		jettonNum[1] = jettonNumSec;
//		jettonNum[2] = jettonNumThi;
//		jettonNum[3] = jettonNumFou;
//		jettonNum[4] = jettonNumFif;
//		return jettonNum;
//	}
	
//	public void setJettonNums(int[] jetton) {
//		this.jettonNum = jetton;
//	}

//	public void setJettonNumByIndex(int index, int jettonNum) {
//		switch (index) {
//		case 0:
//			jettonNumFir = jettonNum;
//			break;
//		case 1:
//			jettonNumSec = jettonNum;
//			break;
//		case 2:
//			jettonNumThi = jettonNum;
//			break;
//		case 3:
//			jettonNumFou = jettonNum;
//			break;
//		case 4:
//			jettonNumFif = jettonNum;
//			break;
//		default:
//			break;
//		}
//	}
	
	
//	public void resetJettonNumsAfterRace() {
//		jettonNumFir = 0;
//		jettonNumSec = 0;
//		jettonNumThi = 0;
//		jettonNumFou = 0;
//		jettonNumFif = 0;
//	}
	
//	//本保存为一数组，但是由于数组无法进行永久保存，就单独拆开了，但是还是保留了类似于索引的选择信息
//	public void addJettonNum(int index, int jettonNum){
//		switch (index) {
//		case 0:
//			jettonNumFir += jettonNum;
//			break;
//		case 1:
//			jettonNumSec += jettonNum;
//			break;
//		case 2:
//			jettonNumThi += jettonNum;
//			break;
//		case 3:
//			jettonNumFou += jettonNum;
//			break;
//		case 4:
//			jettonNumFif += jettonNum;
//			break;
//		default:
//			break;
//		}
//	}
}