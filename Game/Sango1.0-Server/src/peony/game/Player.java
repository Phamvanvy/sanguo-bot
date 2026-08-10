package peony.game;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.apache.log4j.Logger;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;
import org.joda.time.MutableDateTime;
import peony.alchemy.AlchemyLevelData;
import peony.alchemy.AlchemyService;
import peony.channel.Channel;
import peony.clientguid.ClientGuid;
import peony.db.DBService;
import peony.db.LogoutCall;
import peony.depot.AccountDepotSaveCall;
import peony.game.admin.GMRequest;
import peony.game.association.Association;
import peony.game.association.AssociationException;
import peony.game.association.AssociationInvite;
import peony.game.association.AssociationMember;
import peony.game.association.AssociationService;
import peony.game.asyncbattle.AsyncBattleService;
import peony.game.asyncbattle.AsyncPlayer;
import peony.game.asyncbattle.PlayerAi;
import peony.game.asyncbattle.PlayerBodyAi;
import peony.game.attendant.Attendant;
import peony.game.attendant.AttendantBag;
import peony.game.attendant.AttendantFixService;
import peony.game.battlefield.FlagBattleFieldVMapManager;
import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.game.changed.AddTitleChangedItem;
import peony.game.changed.BindChangedItem;
import peony.game.changed.ChangedItem;
import peony.game.changed.HorseFoodChange;
import peony.game.changed.InvalidItem;
import peony.game.changed.PacketChangedItemVisitor;
import peony.game.changed.SkillChangedItem;
import peony.game.chat.ChatMessage;
import peony.game.chat.ChatService;
import peony.game.directory.ClientDirectory;
import peony.game.exchange.Exchange;
import peony.game.exp.ExpService;
import peony.game.instance.InstanceSweep;
import peony.game.instance.InstanceSweepService;
import peony.game.itemeffect.ActivityItemEffect;
import peony.game.itemeffect.AddItemEffect;
import peony.game.itemeffect.GetExpEffect;
import peony.game.itemeffect.KingItemEffect;
import peony.game.itemenhance.AutoAddHole;
import peony.game.itemenhance.AutoEquipEnhance;
import peony.game.itemenhance.AutoNaturalEnhance;
import peony.game.itemenhance.ItemEnhance;
import peony.game.itemenhance.JewelService;
import peony.game.itemenhance.NaturalEnhance;
import peony.game.map.ForceGoMapCall;
import peony.game.map.ReliveTransferCall;
import peony.game.nation.CandidateService;
import peony.game.nation.Nation;
import peony.game.nation.NationSkill2;
import peony.game.nation.NationSkill3;
import peony.game.nation.NationSkill6;
import peony.game.nation.Officer;
import peony.game.party.Party;
import peony.game.pk.PkInfo;
import peony.game.salary.SalaryService;
import peony.game.skill.AbstractSkill;
import peony.game.skill.Skill;
import peony.game.stepserver.StepClient;
import peony.game.stepserver.StepServer;
import peony.game.suite.SuiteEffect;
import peony.game.suite.SuiteEffects;
import peony.game.touchaction.TouchAction;
import peony.marriage.AskForGiftService;
import peony.marriage.MarriageService;
import peony.net.AbstractClientSession;
import peony.net.ClientSession;
import peony.net.DispatchPacket;
import peony.net.Packet;
import peony.service.MonthlyPayService;
import peony.service.ServiceEvent;
import peony.service.VIP.VipPrivilegeService;
import peony.service.account.Account;
import peony.service.account.AccountProperty;
import peony.service.account.AccountService;
import peony.service.account.AccountStatService;
import peony.service.account.ChargeActivityService;
import peony.service.account.FirstCharge;
import peony.service.activity.Activity;
import peony.service.activity.ActivityService;
import peony.service.award.AwardService;
import peony.service.cards.CardGroup;
import peony.service.cards.CardInfo;
import peony.service.cards.CardService;
import peony.service.cards.CardUpGradeCall;
import peony.service.cards.Cards;
import peony.service.enhance.EnhanceService;
import peony.service.enhance.EquipLevelUpInfoCall;
import peony.service.feast.FeastInstanceService;
import peony.service.friend.PlayerRelation;
import peony.service.friend.RelationService;
import peony.service.gamble.GambleService;
import peony.service.onlinetime.PlayerOnlineTimeService;
import peony.service.player.PlayerService;
import peony.service.pluginstance.MayDayFestivalService;
import peony.service.read.Book;
import peony.service.read.BookUtil;
import peony.service.read.Books;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.ShopService;
import peony.service.stat.PvpInfo;
import peony.service.stat.StatService;
import peony.service.tong.Tong;
import peony.service.tong.TongService;
import peony.service.tong.TongSkill1;
import peony.service.welfare.WelfareService;
import peony.util.IntHashMap;
import peony.util.TimeUtil;
import peony.vm.ASMGameVM;
import peony.vm.ASMQuest;
import peony.vm.ASMQuestUtil;
import com.pip.net.message.gameaccount.OnlineTimeNotifyMessage;
import com.pip.sanguo.data.BookChapter;
import com.pip.sanguo.data.BookConfig;
import com.pip.sanguo.data.Card;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.equipment.AttributeCalculator;
import com.pip.sanguo.data.equipment.Equipment;
import com.pip.sanguo.data.quest.QuestTarget;
import com.pip.sanguo.data.skill.BuffConfig;

@Entity
@Table(name = "player")
@AccessType("field")
public class Player extends Unit implements Client {

	public static final WarState PVESTATE = new PveState();
	public static final WarState PVPSTATE = new PvpState();
	public static final WarState PVEPVPSTATE = new PvePvpState();
	public static final WarState PVPPVESTATE = new PvpPveState();
	
	private static final int AUTO_ATTACK_INTERVAL = 3000;
	public static final int MAX_PVE_LEVEL = 35;
	public static final int MAX_LEVEL = 110;

	private static final Logger log = Logger.getLogger(Player.class);

	public static final int NOTIFY_CHAT = 1;
	public static final int NOTIFY_MESSAGE = 2;
	public static final int NOTIFY_QUESTION = 3;

	public static final int PVP_TIME = 3 * 60 * 1000; // 3分钟
	
	public static final int EXP_UNLOCK = 0;  
	public static final int EXP_LOCK = 1;    

	@Column(name = "accountid", nullable = false)
	public int accountId;

	@Transient
	public ClientSession session;
	
	@Transient
	protected String lastSessionId = "0";

	@Transient
	public int[] attack_timer = new int[3];
	@Transient
	public GameObjectRef targetRef;
	public static final int ATTACK_NO = -2;
	public static final int ATTACK_PREPARE = -1;

	@Transient
	public Creature lastkillCreature;
	@Transient
	public Player lastkillPlayer;
	@Transient
	public String lastMessage;
	@Transient
	public List<TouchNpcInfo> touchedNpc = new LinkedList<TouchNpcInfo>();
	@Transient
	public int questToFinishQuestId = -1;

	@Transient
	public List<UINotify> uiNotifies = new LinkedList<UINotify>();

	@Type(type = "peony.game.ASMGameVMUserType")
	@Column(name = "vm")
	public ASMGameVM asmVm;

	@Transient
	public Random rnd = new Random();

	@Type(type = "peony.game.TransactionBagUseType")
	@Column(name = "bag")
	public TransactionBag bag;

	@Transient
	public KillCreatureRecorder kills = new KillCreatureRecorder();

	// 本cycle杀死的玩家ID集合
	@Transient
	public Set<Integer> killPlayers = new HashSet<Integer>();

	// 死亡原因，0 - 未死亡，1 - 被玩家杀死，2 - 被怪物杀死
	@Transient
	public int dieCause;
	
	@Transient
	public float[] timeRatio = new float[4];//(0,沉默，1，恐惧，2，麻痹，3，定身）

	@Column(name = "skillpoint", nullable = false)
	public int skillPoint;

	@Column(name = "exp", nullable = false)
	public int exp;

	@Column(name = "propertypoint", nullable = false)
	public int propertyPoint;

	@Column(name = "actionbar")
	public byte[] actionBarOptions;

	@Column(name = "config")
	public byte[] config;

	@Transient
	public PlayerService serivce;

	@Type(type = "peony.game.ChatOptionsUserType")
	@Column(name = "chatoptions")
	public ChatOptions chatOptions;

	@Transient
	volatile public int loadTimes; // 维护客户端发送LOADING_FINISHED_CLIENT的次数

	@Transient
	public Party party;

	@Transient
	public int systemState;
	@Transient
	public long changeStateStamp;

	@Column(name = "exist", nullable = false)
	public int exist; // 1 代表角色正常 0代表角色删除

	@Transient
	public long loginTime;// logout的时间，如果logout时间超过10分钟，那么将会从ObjectAccessor中删除

	@Transient
	public int ignoreMoveTime; // 复活以后5秒内忽略move信息

	@Column(name = "money", nullable = false)
	public int money;

	@Column(name = "honor", nullable = false)
	public int honor;

	public static final int SYSTEMSTATE_LOAD = 1;// 被加载到内存
	public static final int SYSTEMSTATE_LOGINED = 2; // 登录,在过地图的时候也转换到这种状态
	public static final int SYSTEMSTATE_READY = 3; // 到每个地图以后收到第一个move信息以后设置成这个状态
	public static final int SYSTEMSTATE_DISCONNECTED = 4; // 断连

	@Transient
	protected List<CoolDown> cds = null;

	@Transient
	public ReliveOptions reliveOptions = null;

	@Transient
	public AutoAttack autoAttack = null;

	@Transient
	protected List<PlayerTransaction> transactions = new LinkedList<PlayerTransaction>();
	@Transient
	protected Lock txLock = new ReentrantLock();

	// protected Ride ride = null;
	@Transient
	protected ItemUse itemUse = null;

	@Transient
	public int lastSkillId = -1;

	@Transient
	public int lastItemId = -1;

	@Column(name = "createtime", nullable = false)
	public Date createTime;

	@Transient
	protected String guildName;

	@Transient
	public int mapNumber;

	@Transient
	public boolean cheat;

	// 记录每个威胁自己的敌对玩家的最后威胁时间
	@Transient
	public Map<Integer, Integer> enemyPlayers = new HashMap<Integer, Integer>();

	@Transient
	public Exchange exchange;

	@Column(name = "titles")
	@Type(type = "peony.game.TitlesUserType")
	public Titles titles;

	@Transient
	public int pvpTime;

	@Transient
	public int pvpFactionTime;
	
	@Transient
	public int pvp2pveMapId;

	@Transient
	protected TransactionIntProperty moneyTx; // 管理Money的事务，Money字段不能直接访问，必须通过此字段间接访问

	@Transient
	protected TransactionIntProperty creditTx;

	@Transient
	protected TransactionIntProperty honorTx;

	@Transient
	protected TransactionIntProperty expTx;
	
	@Transient
	protected TransactionIntProperty salaryTx;

	// @Transient
	// protected List<ClientMove> moves = new LinkedList<ClientMove>();

	@Transient
	public Position lastPosition, lastCalcPosition = null;

	@Transient
	public int forbidScore, runDist, violationTime1 = Time.currTime - 300000, violationTime2 = Time.currTime;
	
	@Transient
	protected double totalCheatDistance;      // 通过外挂方式累积获益
	
	@Transient
	protected int[] recentMoveClientTime = new int[30];		// 最近30次move包的时间
	@Transient
	protected int[] recentMoveServerTime = new int[30];		// 最近30次move包的时间
	@Transient
	protected int moveCounter = 0;
	
	@Column(name = "horses")
	@Type(type = "peony.game.HorseBagUserType")
	public HorseBag horseBag;

	@Transient
	public HorseRide ride;

	@Column(name = "pool")
	@Type(type = "peony.game.PropertyPoolType")
	public PropertyPool pool;

	@Transient
	public Gather gather;

	@Column(name = "lastlogin", nullable = true)
	public Date lastLoginTime;
	
	@Transient
	public long lastLoginTimeMills;

	@Transient
	public boolean acceptMoving;

	@Transient
	public boolean logouted; // 是否是正常退出

	@Transient
	public Flag flag;
	
//	@Transient 
//	public ReliveOption relive; 

	@Column(name="activepower", nullable=false)
	public int activePower; // 行动力
	
	@Column(name="formulalist")
	@Type (type="peony.game.FormulaListUserType")
	public FormulaList formulaList;
	
	@Transient
	public int lastRestoreActivePowerTime;
	
	@Transient
	public WarState warState;
	
	//在pvp状态下，在特殊地区被杀的次数，如果次数大于2次，那么将会变成pve状态15分钟
	@Transient
	public int pvpKilledTimes;
	
	@Type(type = "peony.depot.DepotTransactionBagUseType")
	@Column(name = "depot")
//	@Transient
	public TransactionBag depot;
	
	@Column(name = "lastlogout", nullable = true)
	public Date lastLogoutTime;
	
	@Transient
	public long lastLogoutTimeMills; 
	
	@Transient
	public int onlineExpTime = 0; 
	
	@Transient
	public AutoNaturalEnhance autoNaturalEnhance;
	
	@Transient
	public AutoEquipEnhance autoEquipEnhance;
	
	@Transient
	public AutoAddHole autoAddHole;
	
	@Transient
	protected ConcurrentHashMap<Integer,Action> actions = new ConcurrentHashMap<Integer,Action>(10);
	
	@Transient
	public PacketChangedItemVisitor pv = new PacketChangedItemVisitor();
	
	@Column(name = "attendants")
	@Type(type = "peony.game.attendant.AttendantUserType")
	public AttendantBag attendantBag;
    
	// 像creature一样，player也模拟行走
	@Transient
    public int nextX,nextY,startX,startY,lastX,lastY;
	@Transient
    private int nextDistance;			// 到下一点的距离
	@Transient
    private int needRunTime;			// 到下一点的预计时间
	@Transient
    public int runToNextPointTime; //从一点开始到另外一点的时间
	@Transient
    private int cycle;
	@Transient
	private List<Packet> scheduledPacket = new ArrayList<Packet>();
	@Transient
	private List<ChatMessage> scheduledChat = new ArrayList<ChatMessage>();
	@Transient
	public float tirePercent = 1.0f;	// 防沉迷系统下的收益比例
	@Transient
	public boolean tireChecked = false;
	@Transient
	private int errorCount = 0;
	@Transient
	public PlayerRelation relations;   // 为提高效率在这里保存一份冗
	@Transient
	public long enterMarrigTime;
	@Transient
	public byte isFetch = 0;
	@Transient
	public boolean isKicked = false;
	@Transient
	public Report report = new Report();
	@Transient
	public AssociationInvite associationInvite;
	@Transient
	public static int[] STAR_BUFF = {0, 390, 391, 392, 582, 392}; //星辉BUFF(0, 4级，6级，8级，10级，台湾全7)
	//public static int[] STAR_BUFF = {0, 390, 391, 392, 392}; //星辉BUFF(0, 4级，6级，8级，台湾全7)
	@Transient
	public static Buff[] STAR_BUFFS = new Buff[6];
	@Transient
	public static int[] HORSE_STAR_BUFF = {0, 433, 434, 435, 435}; //坐骑星辉BUFF
	@Transient
	public static Buff[] HORSE_STAR_BUFFS = new Buff[5];
	@Transient
	public static int STAR_7_BUFF_ID = 419; //全七BUFF
	@Transient
	public static Buff STAR_7_BUFF = BuffUtil.createSuiteBuff(STAR_7_BUFF_ID, 1);
	@Transient
	public AntiPlug antiPlug = new AntiPlug(this);
	@Transient
	public AntiBot antiBot = new AntiBot(this);
	@Transient
	public static String[] antiBotModels = {"GenericMidp2","Nokia7610","Nokia6681","Nokia3250","NokiaN73",
		"Nokia7370","NokiaE62","SEK750","SEK790","MotoE2","Midp2Touch","Lenovo","NokiaS60V3","NokiaS60V2",
		"PocketPC","WindowsMobile",
		"LenovoU1","Nokia5800","Nokia5800II","Nokia5800New","Nokia5800NewC","Nokia5800Portrait"};
	@Transient
	public static int ANTIPLUG_MODEL_LOG = 0; //外挂处理方式--日志
	@Transient
	public static int ANTIPLUG_MODEL_NONBENEFIT = 1; //外挂处理方式--无收益
	@Transient
	public static int antiBotModel = ANTIPLUG_MODEL_LOG; //新防外挂处理方式
//	public static int antiBotModel = ANTIPLUG_MODEL_NONBENEFIT; //新防外挂处理方式
	@Transient
	public static int antiPlugModel = ANTIPLUG_MODEL_LOG; //外挂处理方式
	@Transient
	public static int maxSkillOffsetTime = 100000; //攻击补偿上限
	@Transient
	public static int maxBotScore = 10; 
	@Transient
	public Attendant attendant; //当前跟随
	@Transient
	public byte isFindPath = 0; //是否正在寻路(0是非寻路状态、1寻路状态)
	@Transient
	public long lastChatTime = 0;//最后一次发言的时间
	@Transient
	public Map<Integer, Integer> lastRemindFriendTime = new HashMap<Integer, Integer>();//最后一次提醒好友上线的时间
//	@Column(name = "clientguid")
//	@Type(type = "peony.clientguid.ClientGuidUserType")
	@Transient
	public ClientGuid guid;
	@Transient
	public ClientDirectory directory;
	@Transient
	public CardPunch cardPunch;
	@Transient
	public long lastProcessCardBuffTime;
	@Transient
	public static int checkCardBuffDis = 600000;
	@Transient
	public Long lastCheckTime2 = 0L;
	@Transient
	public Long enterMap = 0L;//进入新手村时间
	@Transient
	public int salaryDay = 0;//获取工资日
	@Transient
	public int bookDay = 0;//书籍换天
	@Transient
	public IntHashMap<Long> monthPay = new IntHashMap<Long>();//包月
	@Transient
	public NaturalEnhance[] tempEnHance;	//临时装备的资质鉴定信息（装备资质鉴定的替换确认）
	@Transient
	public int tempStar;	//临时装备的星级鉴定信息（星级鉴定的替换确认）
	@Transient
	public int lastCheckEquipTime;
	@Transient
	public int lastCheckTitlesTime;//称号过期检测
	

	/**
	 * 个人贡献度
	 */
	@Column(name="contribute",nullable=false)
	public int contribute;
	
	/**
	 * 个人每日贡献度
	 */
	@Column(name="contributeday",nullable=false)
	public int contributeDay;
	
	@Transient
	public Map<Integer,InstanceSweep> sweepList = new HashMap<Integer,InstanceSweep>(); //正在扫荡的副本
	
	@Transient
	public List<Integer> freeSweep = new ArrayList<Integer>(); //正在扫荡的副本
	
	@Column(name = "cards")
	@Type(type = "peony.service.cards.CardsUserType")
	public Cards cards;
	
	@Column(name = "alchemy")
	@Type(type = "peony.alchemy.AlchemyUserType")
	public AlchemyLevelData alchemy;
	
	@Transient
	public int mateenaidu=0; //配偶恩爱度
	
	@Transient
	public int cardExpAdd=0; //当天摇卡经验
	
	@Transient
	public int rockCardCount=0; //当天摇卡次数
	
	@Transient
	public int prayCount=0; //当天祈福次数
	
	@Transient
	public int payForPray=0; //当天祈福花的元宝
	
	@Transient
	public int failHorseInst = -1; //坐骑合成失败时，用于未手动确认直接掉线时扣除坐骑的ID
	
	@Transient
	public int dayQuest=0;  //完成的每日任务数
	
//	@Transient
//	public int midAutumnDayQuest=0;  //完成的每日中秋任务数

	@Transient
	public int dayInstance=0;  //完成/扫荡副本数
	
	@Transient
	public int readBookCount=0;  //每日读书次数
	
	@Transient
	public int daySalary=0;  //每日已获得工资数
	
	@Transient
	public boolean notifyReparEquipment = false; 
	
	@Transient
	public boolean notifyReparEquipment_1 = false; 
	
	@Transient
	public boolean notifyReparHorseEquipment = false; 
	
	@Transient
	public boolean notifyReparHorseEquipment_1 = false; 
	
	@Transient
	public int onlineTimeToday; //玩家当天累计在线时长 
	
	@Transient
	public int lastCheckOnlineTime;
	
	@Transient
	public static int lastCheckOnlineTimeCheckCycle = 500;
	
	@Transient
	protected MutableDateTime cachedCal = new MutableDateTime();
	
	@Transient
	public String pushToken = "";
	
	@Transient
	public long lastAttendantFollowTime;
	
	@Transient
	public static int attendantFollowCD = 30000;
	
	@Transient
	public int chessCount = 0;
	
	@Transient
	public int chessType = 0;
	
	/** 玩家是否处于跨服战场状态 */
	@Transient
	public boolean isInStep = false;
	
	@Transient
	public int stepSafeTime = 0;
	
	@Transient
	public int stepSessionId; //跨服会话sessionId
	
	@Transient
	public int tempMapId; //玩家进跨服战之前的地图
	
	@Transient
	public int stepType;//跨服报名的类型0-普通跨服战，1-常规跨服战，2-争霸赛
	
	@Transient
	public String gameCode; //服务器的gameCode,应用于跨服战场
	
	@Transient
	public byte canPK=0; //0为可以切磋，其他值为不可以
	
	@Transient
	public boolean loadFinshed = false;
	
	@Transient
	public String accountModel;
	
	@Transient
	public static boolean useNewLoadFinish = true;
	
	@Transient
	public int vipLevel;
	
	@Transient
	public int chargeValue;
	
	@Transient
	public int logoutHp, logoutMp;
	
	@Transient
	public boolean canRecordHpMp = true;
	
	@Transient
	public boolean attendantWaitRelive = false;
	
	@Transient
	public PlayerAi ai;
	
	@Transient
	public int battleType; //标记异步
	
	public static int TYPE_ASYNC_PLAYER = 1;	//标记为异步战场的玩家
	
	@Transient
	public int asyncTargetId; //异步战场玩家ID
	
	@Transient
	public int asyncMapInstanceId; //异步战场场景的实例ID
	
	@Transient
	public int asyncEnterMapId, asyncEnterX, asyncEnterY; //挑战异步战场的在线玩家进入战场前的位置信息
	
	@Transient
	public boolean asyncLoadFinish = false;
	
	@Transient
	public boolean battleIngoPlayer = false;
	
	
	public static final String  ISSHOWPIPCHARGEFLAG="ISSHOWPIPCHARGEFLAG";
	public static final int LIMITSHOWOTHERCHARGELEVEL=50;//限制玩家能永久看到其他充值方式的级别(50级以上充值失败过一次的)
	
	@Transient
	public Horse horseView;
	@Transient
	public Attendant attendantView;
	
	
	// 防外挂相关变量
	private static final int VIOLATION_SCORE = 20;				// 记录警告的计分上限
	private static final int FATAL_ERROR_SCORE = 100;			// 踢下线的单次计分上限
	public static int TIME_ERROR_SCORE_1 = 5;					// 初级时间错误扣分值
	public static int TIME_ERROR_SCORE_2 = 10;					// 中级时间错误扣分值
	public static int TIME_ERROR_SCORE_3 = 20;					// 高级时间错误扣分值
	public static int MAX_MOVE_DISTANCE = 5000;					// 单次MOVE最大允许发送间隔（毫秒）
	public static int EXCEED_DISTANCE_SCORE = 100;				// 超过单次MOVE最大允许移动距离扣分值
	public static int POSITION_ERROR_SCORE1 = 0;				// 移动速度过快初级错误扣分值（因ctime不准不扣分）
	public static int POSITION_ERROR_SCORE2 = 0;				// 移动速度过快高级错误扣分值（因ctime不准不扣分）
	public static int TOTAL_CHEAT_ERROR_SCORE = 20;				// 累计移动距离超过设定速度扣分值
	public static int TOO_MUCH_MOVE_ERROR_SCORE = 100;			// MOVE包发送过于频繁扣分值
	public static double MOVE_CHEAT_VALVE1 = 0.5;				// 移动速度过快初级错误阈值
	public static double MOVE_CHEAT_VALVE2 = 1.0;				// 移动速度过快高级错误阈值
	public static double TOTAL_CHEAT_VALVE = 5;					// 累计移动距离错误阈值
	public static int TOO_MUCH_MOVE_VALVE = 20000;				// 判断MOVE包发送过快的时间阈值

	public static final String PROPERTY_REFRESH_SKILL = "refreshskill";
	public static final String PROPERTY_GATHER_ABILITY = "gatherability";
	public static final String PROPERTY_PRODUCE_ABILITY = "produceability";
	public static final String PROPERTY_LAST_FATION_CHAT = "lastfactionchat";
	public static final String PROPERTY_TODAY_FACTION_CHAT_COUNT = "todayfactionchatcount";
	public static final String PROPERTY_RESTORE_ACTIVEPOWER_DAY = "restoreapday"; 
	public static final String PROPERTY_INSTANCE_DAY = "instanceday";
	public static final String PROPERTY_INSTANCE_TIMES = "instancetimes";
	public static final String PROPERTY_INSTANCE_DAY_NEW = "insday";
	public static final String PROPERTY_INSTANCE_TIMES_NEW = "instimes";
	public static final String PROPERTY_NATIONCOLLECT_MONEY = "nationcollect";
	public static final String PROPERTY_GETNATIONSKILL_ITEM_DAY = "getnationskillitemday";
	public static final String PROPERTY_FLAGBATTLE_SIGNUPMAP = "battlesignupmap";
	public static final String PROPERTY_CLICKEXP_START_TIME = "clickexps";
	public static final String PROPERTY_CLICKEXP_CUMULATE_TIME = "clickexpc";
	public static final String PROPERTY_CLICKEXP_DAY = "clickexpday";
	public static final String PROPERTY_CLICKEXP_TIMES = "clickexptimes";
	public static final String PROPERYY_CLICKEXPSUC_TIME = "lastclickexptime";
	public static final String PROPERTY_TONGBATTLE_EXPDAY = "tongbattleexoday";
	public static final String PROPERTY_TONGBATTLE_SIGNMAPID = "tongbattlesignmapid";
	public static final String PROPERTY_LEAVETONG_TIME = "leavetongtime"; //离开军团的时间
	public static final String PROPERTY_COPYEQUIP_COUNT = "copyequip"; // 杀人掉装备的数量
	public static final String PROPERTY_COPYEQUIP_DAY = "copyequipday"; // 杀人掉装备的时间
	public static final String PROPERTY_KILLED_COPYEQUIP_COUNT = "killedcopyequip";
	public static final String PROPERTY_KILLED_COPYEQUIP_DAY = "killedcopyequipday";
	public static final String PROPERTY_CLICKMONEY_START_TIME = "clickmoneys";
	public static final String PROPERTY_CLICKMONEY_CUMULATE_TIME = "clickmoneyc";
	public static final String PROPERTY_CLICKMONEY_DAY = "clickmoneyday";
	public static final String PROPERTY_CLICKMONEY_TIMES = "clickmoneytimes";
	public static final String PROPERTY_LAST_HORSE_INSTANCEID = "LAST_HORSE_INSTANCEID";
	public static final String PROPERTY_LAST_ATTENDANT_INSTANCEID = "LAST_ATTENDANT_INSTANCEID";
	public static final String PROPERTY_LAST_REMOVEFROMASSOCIATION_TIME = "LAST_REMFASS_TIME";
	public static final String PROPERTY_GET_LIBAO_TIMES = "PROPERTY_GET_LIBAO_TIMES";//领取过xx礼包的次数
	public static final String PROPERTY_FAME_GETITEM_DAY = "PROPERTY_FAME_GETITEM_DAY"; //演武场领取物品时间
	public static final String WELFARE_SUBMIT_LETTER = "WELFARE_SUBMIT_LETTER";//玩家提交密函
	public static final String WELFARE_JOIN_TOWER = "welfare_join_tower";//参加一次斗阵
	public static final String WELFARE_JOIN_SILI = "welfare_join_sili";//参加一次司隶战役
	public static final String WELFARE_JOIN_BIWU = "welfare_join_biwu";//参加一次司隶战役
	public static final String WELFARE_KILLONE_INDUEL = "welfare_killone_induel";//在战场内击杀一名玩家
	public static final String PROPERTY_LOCK_EXP ="lockexp";//玩家锁经验
	public static final String PROPERTY_TEACHER_TIMES = "teachertimes"; //师傅出师次数
	public static final String PROPERTY_APPRENTICE_LASTTIME = "apprenticelasttime"; //上次跳出师门的时间
	public static final String PROPERTY_TEACHER_LASTTIME = "teacher"; //上次逐出师门的时间
	public static final String PROPERTY_GRADUATE_TEACHER = "teaherid";//毕业时师傅的id
	public static final String PROPERTY_WEIBO_TOKEN="weibotoken"; //微博token
	public static final String PROPERTY_WEIBO_TOKENSECRET="tokensecret"; //微博tokensecret
	public static final String PROPERTY_WEIBO_NAME="weiboname"; //微博账号
	public static final String PROPERTY_WEIBO_PASSWORD="weibopassword"; //微博密码
	public static final String PROPERTY_WEIBO_ACTIVE="weiboactive"; //是否激活微博
	public static final String PROPERTY_WEIBO_ADDFRIENDSHIP="weiboaddfriendship";//微博关注他人
	public static final String PROPERTY_ACHIEVEMENT_POINT="achievepoint";//成就点数
	public static final String PROPERTY_HORSE_BOOK="horsebook";//使用坐骑遗忘书
	public static final String PROPERTY_FORMULAR_BOOK="formularbood";//学习技能书
	public static final String PROPERTY_KILLENEMY_ONENBATTLE = "killcountonenbattle";//单场国战中击杀敌人数
	public static final String PROPERTY_HORSECHANGE_TIME = "horsechangetime";//坐骑幻化次数
	public static final String PROPERTY_LANTERN_OUT = "lanternout";//元宵提示信息标志
	public static final String PROPERTY_DIECOUNTIN_LANTERN = "lanterndiecount";//元宵死亡次数
	public static final String PROPERTY_LANTERN_ENTERTIME = "lanternentertime";//元宵地图进入时间
	public static final String PROPERTY_PAYFORBOOK_LASTTIME = "payforbooklasttime";//读书
	public static final String PROPERTY_PAYFORBOOK_DAYTIME = "payforbookdaytime";//读书付费次数
	public static final String PROPERTY_READBOOK_COUNT = "readbookcount";//新读书次数
	
	public static final String PROPERTY_FIRST_ESCORT_DAY = "escortDay";		//打开镖车界面日期
	public static final String PROPERTY_ESCORT_ACCEPT = "escortAccept";		//是否领取过押镖任务(0-未领取, 1-已领取)
	public static final String PROPERTY_ESCORTCAR_LEVEL = "escortCarLv";	//镖车品质
	public static final String PROPERTY_ESCORTCAR_REFRESHCOUNT = "escortRefreshCount";	//刷新镖车品质次数
	public static final String PROPERTY_ESCORTCAR_ISPANMONEY = "escortIsPayMoney";	//领取镖车是否花费过元宝
	public static final String PROPERTY_ESCORTCAR_ISVIPDOUBLE = "escortIsVipDouble";//是否购买了VIP双倍奖励
	
	public static final String PROPERTY_SEND_NEWYEAR_DAY = "sendPrayDay";		//春节发送祝福次数日期
	public static final String PROPERTY_SEND_NEWYEAR_PRAY = "sendNewYearPrayCnt";//春节发送祝福次数
	
	public static final String PROPERTY_AUTOEXTENDBAG = "autoextendbag"; //已经自动扩展过背包处理了
	public static final String PROPERTY_ONLINETIMETODY = "onlinetimetody"; 
	public static final String PROPERTY_FRESH_ENTERMAP = "freshentermap";
	
	public static final String PROPERTY_NEWAREAACT_CREDIT = "newareaactcredit";	//新区活动累计战功
	public static final String PROPERTY_NEWAREAACT_ISGET = "newareaactisget";	//是否已经领取新区活动
	
	public static final String PROPERTY_XUANWUSHI_DAY = "xuanwushidaycount";    //每日获取玄武石个数
	public static final String PROPERTY_XUANWUSHI_SALARYDAY = "xuanwushisalarycount";
	public static final String PROPERTY_XUANWUSHI_BOSSDAY = "xuanwushibosscount";
	public static final String PROPERTY_XUANWUSHI_QUESTDAY = "xuanwushiquestcount";
	
	public static int extendBagItem_past = 674; //发送自动扩展背包物品(原价:25背包扩展符)
	public static int extendBagItem_now = 3479; //自动扩展背包物品(代扣)
	public static int autoExtendBagItem_pip = 4181; //发送自动扩展背包物品
	public static int autoExtendBagItem_tw = 4253; //台湾发送自动扩展背包物品
	
	
	public static long TRACE_UNIT_UPDATE_THRESHOLD_NANO = 5000000L;    // 监控player.update速度的阈值
	
	//用于随从战力属性
	public static int[] attendant_buffs = {435,436,437,438,439};
	public static int[][] buff_values ={{0,0,82,82,114,114,228,228,456,456,734,734,1418,1418,1842,1842,2348,2348,2886},
		                                {0,0,114,114,196,196,424,424,734,734,1190,1190,1696,1696,2316,2316,3000,3000,3816,3816,4696},
		                                {0,0,962,962,1842,1842,4190,4190,7468,7468,11658,11658,16778,16778,22810,22810,29822,29822,37746,37746,46550},
		                                {0,0,4696,4696,9310,9310,20968,20968,37240,37240,58208,58208,83790,83790,114068,114068,149042,149042,188630,188630,232832},
		                                {0,0,7044,7044,14006,14006,31468,31468,55860,55860,87328,87328,125726,125726,171118,171118,223604,223604,282970,282970,349248}};
	
	//星辉战力属性
	public static int[] star_buffs = {390,391,392,582,433,434,435};
	public static int[] starbuff_values = {10600,10500,39200,94300,754,9700,12000};
	public static int  XUANWUSHI_ITEM = 4742;
	public static int XUANWUSHI_DAYLIMIT = 12;//每日玄武石总数限制
	public static int XUANWUSHI_SALARYLIMIT = 4;//工资每日玄武石总数限制
	public static int XUANWUSHI_BOSSLIMIT = 3;//世界boss每日玄武石总数限制
	public static int XUANWUSHI_QSTLIMIT = 5;//任务每日玄武石总数限制
		
	/**
	 * 取得已经分配的属性点数量。
	 * @return
	 */
	public int getAddedPropertyPoint() {
		return strengthAdded + agilityAdded + staminaAdded + intellectAdded;
	}
	
	/**
	 * 取得已分配的技能点数量。
	 * @return
	 */
	public int getAddedSkillPoint() {
		return PlayerUtil.SKILL_POINT[level] - skillPoint;
	}
	
	public String getGuildName() {
		return guildName == null ? "" : guildName;
	}
	
	public int hasGuild() {
		return guildName != null ? 1 : 0;
	}

	public void setGuildName(String guildName) {
		this.guildName = guildName;
		addStringPropertyChangedItem(ChangedItem.GUILD, getGuildName(), false);
		moveExtended |= MOVEEXT_GUILD;
	}

	public int getKillCreatureCount(int templateId) {
		return kills.get(templateId);
	}

	public int getKillCreatureCount(int templateId, int mateId) {
		return kills.get(templateId, mateId);
	}
	
	public void addKillCreatureCount(int templateId, List<Player> owners) {
		kills.add(templateId, owners);
	}

	public void addKillPlayer(int playerID) {
		killPlayers.add(playerID);
	}

	public void setSystemState(int systemState) {
		this.systemState = systemState;
		this.changeStateStamp = System.currentTimeMillis();
	}

	public Player() {
		super(GameObject.TYPE_PLAYER);
		this.changed = new Changed();
		this.chatOptions = ChatOptions.newDefaultChatOptions();
		this.moneyTx = new MoneyIntProperty();
		this.expTx = new ExpIntProperty();
		this.creditTx = new CreditIntProperty();
		this.honorTx = new HonorIntProperty();
		this.exist = 1;
		this.autoAttack = new AutoAttack();
		this.titles = new Titles(this);
		this.horseBag = new HorseBag(this);
		this.attendantBag = new AttendantBag(this);
		this.pool = new PropertyPool();
		this.formulaList = new FormulaList();
		this.lastRestoreActivePowerTime = Time.currTime;
		this.guid = new ClientGuid(this);
		this.cardPunch = new CardPunch(this);
		this.directory = new ClientDirectory(this);
		this.salaryTx = new SalaryIntProperty();
		this.cards = new Cards(this);
		this.alchemy=new AlchemyLevelData(this);
	}

	public Player(int accountId, String name, int sex, int clazz) {
		super(GameObject.TYPE_PLAYER);
		this.accountId = accountId;
		this.setName(name);
		this.sex = sex;
		this.clazz = clazz;
		this.asmVm = new ASMGameVM(this);
		this.bag = new TransactionBag(this, 27 + level / 5,0);
		this.depot = new TransactionBag(this, 0, 0); // 初始化仓库
		this.changed = new Changed(); // 只有player才需要changed
		this.moneyTx = new MoneyIntProperty();
		this.expTx = new ExpIntProperty();
		this.creditTx = new CreditIntProperty();
		this.honorTx = new HonorIntProperty();
		this.exist = 1;
		Arrays.fill(attack_timer, -2);
		this.autoAttack = new AutoAttack();
		this.titles = new Titles(this);
		this.horseBag = new HorseBag(this);
		this.attendantBag = new AttendantBag(this);
		this.pool = new PropertyPool();
		this.formulaList = new FormulaList();
		this.lastRestoreActivePowerTime = Time.currTime;
		this.guid = new ClientGuid(this);
		this.cardPunch = new CardPunch(this);
		this.directory = new ClientDirectory(this);
		this.salaryTx = new SalaryIntProperty();
		this.cards = new Cards(this);
		this.alchemy=new AlchemyLevelData(this);
	}

	public void setSession(ClientSession session) {
		this.session = session;
	}
	
	public Account getAccount() {
		if (session == null) {
			return null;
		}
		return (Account)session.getIdentity();
	}

	public void cancelAutoAttack() {
		autoAttack.clear();
	}

	public void cancelAttack() {
		if (attack != null) {
			attack = null;
		}
	}

	public void cancelUseItem() {
		if (itemUse != null) {
			itemUse = null;
		}
	}
	
	/**
	 * 记录外挂检测分数。
	 * @param value 新增分值
	 */
	public void addForbidScore(int value) {
		if(battleType==TYPE_ASYNC_PLAYER)
			return;
		if(isInStep || (!Server.server.isStepServer && Time.currTime<stepSafeTime)){
			log.info("[FORBIDILLEGAL]" + LogUtil.getPlayerLogString(this));
			return;
		}
	    // 每记满20分认为违规1次，然后停半分钟不记分；如果连续3次违规之间的间隔都少于5分钟，则
	    // 踢下线10分钟。
		this.forbidScore += value;
		if (this.forbidScore >= FATAL_ERROR_SCORE) {
		    // 如果发生了严重错误，直接踢下线15分钟
		    log.info("[FORBID][FATALERROR]" + LogUtil.getPlayerLogString(this) + "SCORE[" + forbidScore + "]");
            forbidScore = 0;
		    if (!cheat && Server.antiCheat && !Server.isAppSection) {
                Server.server.getServiceRegistry().getPlayerService().mute(id, System.currentTimeMillis()+15*60*1000);
            }
		    return;
		}
		if (Time.currTime < violationTime2 + 30000L || Time.currTime < 1800000) {
		    // 上次普通违规半分钟内不记分，开服30分钟内不记分
		    this.forbidScore = 0;
		}
		if (this.forbidScore >= VIOLATION_SCORE) {
            forbidScore = 0;
		    if (violationTime2 - violationTime1 < 300000L && Time.currTime - violationTime2 < 300000L) {
		        log.info("[FORBID][3ERRORS]" + LogUtil.getPlayerLogString(this));
    			if (!cheat && Server.antiCheat) {
    			    Server.server.getServiceRegistry().getPlayerService().mute(id, System.currentTimeMillis()+15*60*1000);
    			}
		    }
		    violationTime1 = violationTime2;
		    violationTime2 = Time.currTime;
		}
	}

	public void setFaction(int faction, boolean notify, int serial) {
		int oldFaction = this.faction;
		if (oldFaction != faction) {
			super.setFaction(faction, notify);
			Server.server.getEventManager().fireEvent(
					new ServiceEvent(ServiceEvent.EVENT_PLAYER_CHANGE_FACTION,
							this, oldFaction));
		}
		Packet pt = new Packet(OpCode.CHANGE_FACTION_SERVER);
		pt.putInt(serial);
		pt.putInt(faction);
		send(pt);
	}

	public void setSex(int sex, boolean notify, int serial) {
		super.setSex(sex, notify);
		Packet pt = new Packet(OpCode.CHANGE_SEX_SERVER);
		pt.putInt(serial);
		pt.putInt(sex);
		send(pt);
	}

	public void setClazz(int clazz, boolean notify, int serial) {
		int oldClazz = this.clazz;
		super.setClazz(clazz, notify);
		if (oldClazz != clazz) {
			Gain gain = new Gain(this);
//			GameItem equ = ObjectAccessor
//					.createGameItem(PlayerUtil.INIT_EQUIPMENT[clazz]);
//			gain.addGainItem(equ, 1);
			PlayerTransaction tx = newTransaction("CCL");
			boolean ok = bag.addGain(gain, tx, false);
			tx.commit();
			if (ok) {
//				equip(equ.template.id, equ.instanceId, -1);
				GameItem item = bag
						.getGameItem(PlayerUtil.INIT_EQUIPMENT[oldClazz]);
				if (item != null) {
					tx = newTransaction("CCL");
					bag.removeGameItem(item.template.id, item.instanceId, 1,
							tx, true);
					tx.commit();
				}
			}
			skills.clear();
			List<Skill> initSkills = ObjectAccessor.getPlayerInitSkills(clazz);
			for (Skill skill : initSkills) {
				skills.addSkill(skill, null, false);
			}
			sendSkillList();
			refreshPropertiesWhenClazzChanged(false);
		}
		Packet pt = new Packet(OpCode.CHANGE_CLASS_SERVER);
		pt.putInt(serial);
		pt.putInt(this.clazz);
		send(pt);
	}
	
	public void refreshPropertiesWhenClazzChanged(boolean levelUp){
		PropertyCalculator calc = new PropertyCalculator(this);
		processPreBuff(calc);
		equipments.enhance(calc);
		if(horse!=null)
			horse.enhance(calc);
		buffs.enhance(calc);
		books.enhance(calc);
		setHeadScore(equipments.getHeadScore(level,clazz));
		setBodyScore(equipments.getBodyScore(level,clazz));
		setWeaponScore(equipments.getWeaponScore(level,clazz));
		setFlashLevel(equipments.getFlashLevel());
		calc.caculate();
		setStrength(calc.strength, false);
		setAgility(calc.agility,false);
		setStamina(calc.stamina,false);
		setIntellect(calc.intellect,false);
		setMaxhp(calc.hp,false);
		setMaxmp(calc.mp,false);
		if(levelUp){
			setHp(calc.hp,false);
			setMp(calc.mp,false);
		}else{
			if(hp != calc.hp){
				setHp(calc.hp,false);
			}
			if(mp != calc.mp){
				setMp(calc.mp,false);
			}
		}
		setAttackpowerup(calc.attackpowerup);
		setAttackpowerdown(calc.attackpowerdown);
		setSpellpower(calc.spellpower);
		setSpellheal(calc.spellheal);
		setDefense(calc.defense);
		setSpelldefense(calc.spelldefense);
		setCritical(calc.critical);
		setSpellCritical(calc.spellcritical);
		setHit(calc.hit);
		setSpellhit(calc.spellhit);
		setDodge(calc.dodge);
		setSpelldodge(calc.spelldodge);
		setAnticrit(calc.anticrit);
		setHealthrestore(calc.healthrestore);
		setManarestore(calc.manarestore);
		setDefensePercent(calc.defensePercent);
		setSpeedRatio(calc.getSpeed());
		expRatio = calc.expRatio;
		rewardRation = calc.rewardRation;
		horseExpRatio = calc.horseExpRatio;
		moneyRatio = calc.moneyRatio;
	}

	public void setMoney(int money, boolean notify, String cause) {
		if (money >= 0 && this.money != money) {
			int oldMoney = this.money;
			this.money = money;
			addIntPropertyChangedItem(ChangedItem.MONEY, oldMoney, this.money,
					notify);
			if (money > oldMoney) {
				LogUtil.logGetMoney(this, oldMoney, money, cause);
			} else {
				LogUtil.logRemoveMoney(this, oldMoney, money, cause);
			}
		}
	}

	public int getMoney() {
		return this.money;
	}
	
	public int getIMoney(){
		return Math.round(getAccount().getLongIMoney() / 3600);
	}

	public void setHonor(int honor, boolean notify, String cause) {
		if (honor >= 0 && this.honor != honor) {
			int oldHonor = this.honor;
			this.honor = honor;
			addIntPropertyChangedItem(ChangedItem.HONOR, oldHonor, this.honor,
					notify);
			if (honor > oldHonor) {
				LogUtil.logGetHonor(this, oldHonor, honor, cause);
			} else {
				LogUtil.logRemoveHonor(this, oldHonor, honor, cause);
			}
		}

	}

	@Override
	public void setLevel(int level, boolean notify) {
		super.setLevel(level, notify);
		List<Skill> autoLearns = skills.getAutoLearSkills(level); // 找到所有需要自动学习的Skill，依次学习
		for (Skill skill : autoLearns) {
			addSkill(skill.getNextLevel());
		}

	}

	// public void clientMove(int x,int y , byte direct, short state ,int time){
	// Position p = new Position(map.id,x,y,Time.currTime,time);
	// if(lastPosition!=null){
	// comparePosition(lastPosition,p);
	// }
	// lastPosition = p;
	// if(lastCalcPosition==null){
	// lastCalcPosition = p;
	// }
	// moves.add(new ClientMove(x,y,direct,state,time));
	// }

	/*
	 * 检查用户新到达的一个点是否合法的点，如果不合法，则视为作弊。
	 */
	protected void checkPosition(Position p1) {
//		if (lastPosition != null && lastPosition.mapId == p1.mapId && lastPosition.x == p1.x &&
//				lastPosition.y == p1.y) {
//			return;
//		}
//		if (map.map != null && !map.map.mapDef.mapInfo.getPathFinder().canReach(p1.x, p1.y)) {
//			log.info("[REACHERROR]" +
//                    LogUtil.getPlayerLogString(this) + 
//                    "]DEST[" + p1.mapId + "," + p1.x + "," + p1.y + "]");
//		}
	}
	
	/**
	 * 比较两次移动信息，用以检测客户端是否使用了外挂加速。
	 * @param p1 上次服务器收到的位置信息
	 * @param p2 本次服务器收到的位置信息
	 * @return 如果新的位置信息有错，需要忽略，则返回false。
	 */
	protected boolean comparePosition(Position p1, Position p2) {
	    if (p2.mapId != p1.mapId || p1.clientTime == -1 || p2.clientTime == -1) {
	    	return true;
	    }
	    
	    // 计算两个点之间的距离
        int distY = Math.abs(p2.y - p1.y);
        int distX = Math.abs(p2.x - p1.x);
        double d1 = Math.sqrt(distY * distY + distX * distX);
        
        // 计算玩家在这个时间差范围内，按正常速度移动，最大可移动的距离
        int speed = Math.max(p1.speed, p2.speed);
        if (speed < SPEED) {
        	speed = SPEED;
        }
        double d2 = speed * ((p2.clientTime - p1.clientTime) / 1000.0);
        if (d2 < 0) {
        	d2 = 0;
        }
        
        int distValve = MAX_MOVE_DISTANCE * speed / 1000;
        if (distY > distValve || distX > distValve) {
        	// 客户端要求x或y变化量超过30像素就要发送一个move包，换算成最大直线距离约为42；加上一点客户端帧率低导致的误差，如果
        	// 单次MOVE之间的距离超过70像素，则判定为外挂跳地图，直接踢掉。
        	log.info("[POSITIONERROR][JUMP]" + LogUtil.getPlayerLogString(this) + 
        			"SRC[" + p1.mapId + "," + p1.x + "," + p1.y + 
                    "]DEST[" + p2.mapId + "," + p2.x + "," + p2.y + 
                    "]TIME1[" + p1.clientTime + "]TIME2[" + p2.clientTime + 
                    "]SPEED[" + speed + "]");
        	
        	// 改为不踢而是拉回原地
        	try {
				goMap(lastPosition.mapId, lastPosition.x, lastPosition.y);
				unMoving();
			} catch (VMapException e) {
			}
			return false;
        	// addForbidScore(EXCEED_DISTANCE_SCORE);
        } else if (d1 - d2 > MOVE_CHEAT_VALVE2 * speed) {
        	// 单次收益超过速度的2倍的，扣20分
            log.info("[POSITIONERROR][20]" +
                    LogUtil.getPlayerLogString(this) + 
                    "SRC[" + p1.mapId + "," + p1.x + "," + p1.y + 
                    "]DEST[" + p2.mapId + "," + p2.x + "," + p2.y + 
                    "]TIME1[" + p1.clientTime + "]TIME2[" + p2.clientTime + 
                    "]SPEED[" + speed + "]");
            addForbidScore(POSITION_ERROR_SCORE2);
        } else if (d1 - d2 > MOVE_CHEAT_VALVE1 * speed) {
        	// 单次收益超过速度的1倍的，扣10分
        	log.info("[POSITIONERROR][10]" +
                    LogUtil.getPlayerLogString(this) + 
                    "SRC[" + p1.mapId + "," + p1.x + "," + p1.y + 
                    "]DEST[" + p2.mapId + "," + p2.x + "," + p2.y + 
                    "]TIME1[" + p1.clientTime + "]TIME2[" + p2.clientTime + 
                    "]SPEED[" + speed + "]");
            addForbidScore(POSITION_ERROR_SCORE1);
        }
        
        // 按服务器时间计算，两次MOVE间隔时间内玩家可移动的最大距离
        double d3 = speed * ((p2.time - p1.time) / 1000.0);
        if (d3 < 0) {
        	d3 = 0;
        }
        totalCheatDistance += (d1 - d3) / speed;
        if (totalCheatDistance < 0) {
        	totalCheatDistance = 0;
        }
        if (totalCheatDistance > TOTAL_CHEAT_VALVE) {
        	// 累计加速移动收益超过5秒，记录一次严重错误
        	log.info("[TOTALCHEATERROR]" + LogUtil.getPlayerLogString(this));
            addForbidScore(TOTAL_CHEAT_ERROR_SCORE);
            totalCheatDistance = 0;
        }
        
        // 记录最近收到的move包的时间
        int index = moveCounter % recentMoveClientTime.length;
        recentMoveClientTime[index] = p2.clientTime;
        recentMoveServerTime[index] = p2.time;
        moveCounter++;
        if (moveCounter > 30) {
        	// 比较本次move和30次之前的move之间的时间间隔，如果客户端时间间隔和服务器时间间隔都小于20秒，则踢下线
        	int index0 = moveCounter % recentMoveClientTime.length;
        	int clientGap = p2.clientTime - recentMoveClientTime[index0];
        	int serverGap = p2.time - recentMoveServerTime[index0];
        	if (serverGap < TOO_MUCH_MOVE_VALVE && clientGap < TOO_MUCH_MOVE_VALVE) {
        		log.info("[TOOMUCHMOVEERROR]" + LogUtil.getPlayerLogString(this));
        		moveCounter = 0;
        		addForbidScore(TOO_MUCH_MOVE_ERROR_SCORE);
        	} else if (clientGap < TOO_MUCH_MOVE_VALVE || serverGap < TOO_MUCH_MOVE_VALVE) {
        		log.info("[TOOMUCHMOVEWARN]" + LogUtil.getPlayerLogString(this) + "CGAP[" + clientGap + "]SGAP[" + serverGap + "]");
        	}
        }
        return true;
	}

	// public void move(int x, int y, byte direct, short state, int time) {
	// // if (ignoreMoveTime != 0 && ignoreMoveTime > Time.currTime) {
	// // return;
	// // }
	// super.move(x, y);
	// this.direct = direct;
	// this.state = (short) (((this.state & 0xFFFE) | (state & 0x1)) & 0xFFFF);
	// this.lastMoveTime = time;
	// if (pkInfo != null && pkInfo.state == PkInfo.STATE_STARTED) {
	// Server.server.eventManager.fireEvent(new ServiceEvent(
	// ServiceEvent.EVENT_PLAYER_PK_MOVE, this, x, y));
	// }
	// moveType |= MOVE_RUNNING_STATE;
	// }
	
	/**
	 * 当Player第一次被数据库载入的时候用来调整skills,因为skills字段中记录了所有学过的技能,其中包括0级的每职业的所有技能
	 * 一旦技能做出调整,比如说技能增加的时候,就需要往其中加入新的技能,因为Hibernate载入字段的顺序很可能是不固定的,所有不能
	 * 在skills被载入的时候做,当时的clazz字段未必能取到
	 */
	public void skillsPatch(){
		List<Skill> ss = ObjectAccessor.getPlayerInitSkills(clazz);
		for(Skill s:ss){
			if(skills.getSkillByGroupId(s.getGroupId())==null){
				skills.addSkillSlient(s);
			}
		}
		Iterator<Skill> ite = skills.skills.values().iterator();
		while(ite.hasNext()){
			Skill skill = ite.next();
			if(skill.getGroupId()==175){ //由于175号技能被策划弄错，导致有些角色也学习了这个NPC的技能，所以需要特别判断
				ite.remove();
				if(skill.getLevel() != 0){
					skillPoint += skill.getLevel();
				}
			}
		}

	}

	public int getNextPointX(){
		if (this.nextDistance == 0) {
			return -1;
		}
		return nextX;
	}
	
	public int getNextPointY(){
		if (this.nextDistance == 0) {
			return -1;
		}
		return nextY;
	}
	
	public void setNextPoint(int x,int y){
		if (x == -1 && y == -1) {
			this.nextDistance = 0;
			return;
		}
		if(nextX!=x||nextY!=y){
			this.startX = this.x;
			this.startY = this.y;
			this.nextX = x;
			this.nextY = y;
			this.nextDistance = (int)Math.sqrt((this.nextX - this.startX) * (this.nextX - this.startX) + 
					(this.nextY - this.startY) * (this.nextY - this.startY));
			this.runToNextPointTime = Time.currTime;
			if (getSpeed() == 0) {
				this.needRunTime = runToNextPointTime;
			} else {
				this.needRunTime = this.runToNextPointTime + (this.nextDistance * 1000) / getSpeed();
			}
		}
	}
	
	public void runToNextPoint() {
		if (this.nextDistance == 0) {
			return;
		}
		if (Time.currTime < runToNextPointTime || Time.currTime >= needRunTime) {
			// 时间错误或时间超出，都直接跳转到目标位置
			move(nextX, nextY);
			if(battleType==0)
				this.nextDistance = 0;
		} else {
			int distance = getSpeed() * (Time.currTime - runToNextPointTime) / 1000;
			int dx = distance * (this.nextX - this.startX) / this.nextDistance;
			int dy = distance * (this.nextY - this.startY) / this.nextDistance;
			move(startX + dx, startY + dy);
		}
		lastMoveTime = Time.currTime;
	}
	
	/*
	 * 为上下马协议服务的move方法。
	 */
	public void move(int x, int y, byte direct, int time) {
		Position currentPosition = new Position(map.id, x, y, Time.elapseTime(System.currentTimeMillis()), time, getSpeed());
		checkPosition(currentPosition);
		if (lastPosition != null) {
			if (!isInStep && !comparePosition(lastPosition, currentPosition)) {
				return;
			}
		}
		super.move(x, y);
		this.direct = direct;
		this.lastMoveTime = time;
		lastPosition = currentPosition;
	}
	
	public void move(int x, int y, byte direct, short state, int time, int diff, int nextx, int nexty) {
		// log.info("["+id+","+x+","+y+"]");
		Position currentPosition = new Position(map.id, x, y, Time.elapseTime(System.currentTimeMillis()),
				time, getSpeed());
		checkPosition(currentPosition);
		if (lastPosition != null) {
			if (!isInStep && !comparePosition(lastPosition, currentPosition)) {
				return;
			}
			if (!isInStep && (Time.currTime - lastPosition.time) > 10000 && diff <= 5000) { // 10秒
				try {
					goMap(lastPosition.mapId, lastPosition.x, lastPosition.y);
					unMoving();
				} catch (VMapException e) {
					//不应该被执行到
					log.error(e,e);
				}
				// acceptMoving = false;
			} else {
				super.move(x, y);
				this.direct = direct;
				this.state = (short) (((this.state & 0xFFFE) | (state & 0x1)) & 0xFFFF);
				this.lastMoveTime = time;
				if (pkInfo != null && pkInfo.state == PkInfo.STATE_STARTED) {
					Server.server.eventManager.fireEvent(new ServiceEvent(
							ServiceEvent.EVENT_PLAYER_PK_MOVE, this, x, y));
				}
				moveType |= MOVE_RUNNING_STATE;
				lastPosition = currentPosition;
				moved();
				setNextPoint(nextx, nexty);
			}
		} else {
			super.move(x, y);
			this.direct = direct;
			this.state = (short) (((this.state & 0xFFFE) | (state & 0x1)) & 0xFFFF);
			this.lastMoveTime = time;
			if (pkInfo != null && pkInfo.state == PkInfo.STATE_STARTED) {
				Server.server.eventManager.fireEvent(new ServiceEvent(
						ServiceEvent.EVENT_PLAYER_PK_MOVE, this, x, y));
			}
			moveType |= MOVE_RUNNING_STATE;
			lastPosition = currentPosition;
			moved();
			setNextPoint(nextx, nexty);
		}
	}

	protected void moved() {
		MoveCallback moveCallback = moveCallback();
		if (moveCallback != null) {
			moveCallback.moved(this);
		}
	}

	public void ride() {
		state |= STATE_RIDE; // 设置骑马状态
		addIntPropertyChangedItem(ChangedItem.RIDE, getHorseInt(), false, true);
		refreshProperties(false);
		moveType |= MOVE_POINT_STATE | MOVE_DETAIL | MOVE_HORSE;
		
		// 记录玩家动作
		addAction(Action.RIDE);
		processCardBuff();
		updateHorseCardBuff(0);
	}

	public void horseFeed(int gridId, int itemId, int instanceId,
			int horseInstanceId, int serial) {
		if(itemId!=ItemUtil.ITEM_HORSEFOOD&&itemId!=ItemUtil.ITEM_HORSEFOOD_ADDBUFF&&itemId!= ItemUtil.ITEM_HORSEFOODS){
			return;
		}
		Horse h = horseBag.getHorse(horseInstanceId);
		if (h != null) {
			if (h.degree == 100) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_FEED_CLIENT, peony.Messages.STRING_01560);
				return;
			}
			PlayerTransaction tx = newTransaction("FED");
			TransactionBagGrid grid = bag.removeGridGameItem(gridId, itemId,
					instanceId, 1, tx, true);
			if (grid != null) {
				h.feed(grid.item, this, serial);
				tx.commit();
				
				// 记录玩家动作
				addAction(Action.FEED_HORSE);
			} else {
				tx.rollback();
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_FEED_CLIENT, peony.Messages.STRING_01561);
			}
		}
	}

	public void horseUnride(int serial) {
		if (horse != null) {
			Horse h = horse;
			horse = null;
			h.unRide(this);
			unRide();
			Packet pt = new Packet(OpCode.HORSE_UNRIDE_SERVER);
			pt.putInt(serial);
			send(pt);
			updateLastOnHorse();
		}
	}

	public void prepareHorseRide(int horseInstanceId, int serial) {
		Horse h = horseBag.getHorse(horseInstanceId);
		if (h == horse) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.HORSE_RIDE_CLIENT, peony.Messages.STRING_01562);
			return;
		}
		ride = new HorseRide(h.instanceId, Time.currTime
				+ h.template.summonTime, serial);
	}

	/**
	 * 上骑
	 * @param horseInstanceId
	 * @param serial
	 * @param change 是否换装 0 换装，-1 不换
	 */
	public void horseRide(int horseInstanceId, int serial,int change) {
		Horse h = horseBag.getHorse(horseInstanceId);
		Horse oldHorse = horse;
		if (h != null) {
			if (h != horse) {
				if(!h.isActive()){
					String message = peony.Messages.STRING_01563;
					if(h.indexOfFreeHorses(h.itemId)!=-1){
						message = "主人，只要你拥有100元宝就可以激发我体内的小宇宙啦，去商城中看看怎么充值，然后再来激活我吧";
					}
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.HORSE_RIDE_CLIENT, message);
					return;
				}
				if(CandidateService.isKingHorse(h.itemId)){
					Nation nation = Server.server.getServiceRegistry().getNationService()
					.getNationByFaction(faction);
					if(nation.getKingId()!=id){
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.HORSE_RIDE_CLIENT, peony.Messages.STRING_01564);
						return;
					}
				}
				if (h.degree <= 0) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.HORSE_RIDE_CLIENT, peony.Messages.STRING_01565);
					return;
				}
				if (horse != null) {
					horse.unRide(this);
					horse = null;
					unRide();
				}
				horse = h;
				h.ride(this);
				ride();	
				try {
					horseExchangeEquip(oldHorse,horse,change);
				} catch (Exception e) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.HORSE_RIDE_CLIENT, e.getMessage());
				}
				if(oldHorse!=null && oldHorse.equs!=null && oldHorse.equs.equs!=null)
					removeSuiteEquipmentBuffs(oldHorse.equs.equs);
				addHorseSuiteEquipmentBuffs();
				Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_HORSE_RIDE, this));
				updateLastOnHorse();
				refreshHorseStarState();
				refreshHorseAndPlayerProperty();
			}
			Packet pt = new Packet(OpCode.HORSE_RIDE_SERVER);
			pt.putInt(serial);
			pt.putInt(horseInstanceId);
			send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.HORSE_RIDE_CLIENT, peony.Messages.STRING_00201);
		}
	}
	
	/**
	 * 坐骑装备换装
	 * @param oldHorse 原来的坐骑
	 * @param horse 目标坐骑
	 * @param change 是否换装 0 换装，-1不换
	 * @throws Exception
	 */
	protected void horseExchangeEquip(Horse oldHorse,Horse horse,int change) throws Exception{
		if(oldHorse != null && horse != null){
			if(change == 0){
				List<GameItem> equipments = new ArrayList<GameItem>();
				for(GameItem item : oldHorse.equs.equs){
					if(item != null){
						equipments.add(item);
						if(item.template.useLevel > horse.level){
							change = -1;
							break;
						}
					}
				}
				if(change == 0){
					for(GameItem item : equipments){
						boolean canChange = true;
						if(item!=null){
							int index = HorseEquipments.getIndex(item.template.equipment.minorType);
							if(horse.equs.equs[index]!=null){
								canChange = false;
								break;
							}
						}
						if(canChange){
							GameItem gameItem = oldHorse.equs.unequip(item.template.id, item.instanceId, this);
							horse.equs.equip(gameItem, this);
						}
					}
					oldHorse.refreshProperties(false, this);
//					horse.refreshProperties(false, this);
					checkFoodChange(this,oldHorse,horse);
					message(-1, peony.Messages.STRING_01566, -1, -1);
				} else {
					throw new Exception(peony.Messages.STRING_01567);
				}
			}
		}
	}
	
	/**
	 * 随从装备换装
	 * @param oldAttendant 原来的随从
	 * @param attendant 目标随从
	 * @param change 是否换装 0 换装，-1不换
	 * @throws Exception
	 */
	public void attendantExchangeEquip(Attendant oldAttendant,Attendant attendant,int change) throws Exception{
		if(oldAttendant != null && attendant != null){
			if(change == 0){
				List<GameItem> equipments = new ArrayList<GameItem>();
				for(GameItem item : oldAttendant.equs){
					if(item != null){
						equipments.add(item);
						if(item.template.useLevel > attendant.level){
							change = -1;
							break;
						}
					}
				}
				if(equipments.size()<=0){
					throw new Exception("原来的随从身上没有装备");
				}
				if(change == 0){
					for(GameItem item : equipments){
						oldAttendant.unEquip(item.template.id, item.instanceId);
						attendant.equip(item.template.id, item.instanceId);
					}
					oldAttendant.refreshProperties(false);
					attendant.refreshProperties(false);
					message(-1, peony.Messages.STRING_01566, -1, -1);
				} else {
					throw new Exception("您要换装的随从等级太低，无法穿上装备，快去升级吧！");
				}
			}
		}
	}
	
	public void checkFoodChange(Player p,Horse oldHorse,Horse newHorse){
		try{
			if(oldHorse.foodId!=-1 && newHorse.foodId==-1 && 
					(oldHorse.foodId == ItemUtil.ITEM_HORSEFOOD || oldHorse.foodId== ItemUtil.ITEM_HORSEFOOD_ADDBUFF || oldHorse.foodId == ItemUtil.ITEM_HORSEFOODS)){
				if(p.changed!=null){
					HorseFoodChange changedItem = new HorseFoodChange(newHorse,oldHorse.foodId);
					p.changed.addChangedItem(changedItem);
					HorseFoodChange changedItem2 = new HorseFoodChange(oldHorse,-1);
					p.changed.addChangedItem(changedItem2);
					newHorse.foodId = oldHorse.foodId;
					oldHorse.foodId = -1;
				}
			}
		}catch(Exception e){
			
		}
		
	}
	
	public void refreshHorseAndPlayerProperty(){
		if(horse!=null)
			horse.refreshProperties(false, this);
		refreshProperties(false);
	}
	
	private void updateLastOnHorse(){
		if(horse != null){
			pool.setInt(PROPERTY_LAST_HORSE_INSTANCEID, horse.instanceId);
		} else {
			pool.setInt(PROPERTY_LAST_HORSE_INSTANCEID, 0);
		}
	}

	protected void processRide() {
		if (ride != null) {
			if (ride.time < Time.currTime) {
				horseRide(ride.instanceId, ride.serial,-1);
				ride = null;
			}
		}
		if (horse != null) {
			horse.update(this);
		}
	}

	/**
	 * 修理装备
	 * 
	 * @param serial
	 * @param type
	 *            0 - 身上装备，1 - 背包装备，2 - 所有装备
	 */
	public void repair(int serial, int type) {
		int totalMoney = 0;
		List<GameItem> bagItems = new ArrayList<GameItem>();
		List<Horse> horses = new ArrayList<Horse>();
		if (type == 0 || type == 2) {
			for (int i = 0; i < equipments.equs.length; i++) {
				GameItem item = equipments.equs[i];
				if (item != null) {
					totalMoney += item.getRepairMoney();
				}
			}
		}
		if (type == 1 || type == 2) {
			for (TransactionBagGrid grid : bag.grids) {
				if (grid.item == null || grid.item.template.equipment == null) {
					continue;
				}
				int m = grid.item.getRepairMoney();
				if (m > 0) {
					totalMoney += m;
					bagItems.add(grid.item);
				}
			}
		}
		if(type == 0){
			if(horse!=null){
				totalMoney += horse.getRepairMoney();
				horses.add(horse);
			}
		}
		if(type == 2){
			for(Horse h:horseBag.horses){
				totalMoney += h.getRepairMoney();
				horses.add(h);
			}
		}
		if (totalMoney == 0) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.REPAIR_CLIENT, peony.Messages.STRING_01568);
			return;
		}

		PlayerTransaction tx = newTransaction("REP");
		try {
			decMoney(totalMoney, tx, true);
			tx.commit();
			equipments.repair();
			for (GameItem bitem : bagItems) {
				bitem.repair(this);
			}
			for (Horse h: horses){
				h.repair(this);
			}
			Packet pt = new Packet(OpCode.REPAIR_SERVER);
			pt.putInt(serial);
			pt.putInt(totalMoney);
			send(pt);
		} catch (NoEnoughValueException e) {
			tx.rollback();
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.REPAIR_CLIENT, peony.Messages.STRING_00020);
		}
	}

	public void unRide() {
		state &= 0xFFFB; // 设置成下马状态
		addIntPropertyChangedItem(ChangedItem.RIDE, getHorseInt(), false, true);
//		refreshProperties(false);
		moveType |= MOVE_POINT_STATE | MOVE_DETAIL | MOVE_HORSE;
		refreshHorseStarState();
		processCardBuff();
		updateHorseCardBuff(1);
	}

	protected void rollbackNotCommiteds() {
		txLock.lock();
		try {
			if (transactions.size() > 0) {
				log.info("[ROLLBACK]" + LogUtil.getPlayerLogString(this) + "SIZE[" + transactions.size() + "]");
				List<PlayerTransaction> l = new ArrayList<PlayerTransaction>(
						transactions);
				for (PlayerTransaction tx : l) {
					release(tx, false);
					log.info(tx);
				}
				log.info("[ROLLBACK]" + LogUtil.getPlayerLogString(this) + "OK");
			}
		} finally {
			txLock.unlock();
		}
	}

	public PlayerTransaction newTransaction(String cause) {
		txLock.lock();
		try {
			PlayerTransaction tx = new PlayerTransaction(this, cause);
			transactions.add(tx);
			return tx;
		} finally {
			txLock.unlock();
		}
	}

	void release(PlayerTransaction tx, boolean commit)
			throws TransactionException {
		txLock.lock();
		try {
			if (transactions.contains(tx)) {
				transactions.remove(tx);
				if (commit)
					tx.internalCommit();
				else
					tx.internalRollback();
			} else
				throw new TransactionException();
		} finally {
			txLock.unlock();
		}
	}

	@Override
	public void setAttack(Attack attack) {
		if(attack==null && battleType==TYPE_ASYNC_PLAYER && ai!=null){
			((PlayerBodyAi)ai).skill = null;
			((PlayerBodyAi)ai).nextSkill = null;
		}
		super.setAttack(attack);
		if (attack != null) {
			if (autoAttack != null && autoAttack.fired) {
				autoAttack.time = Time.currTime + attack.skill.getActTime(this)
						+ AUTO_ATTACK_INTERVAL;
				autoAttack.attack = null;
			}
		}
		// if (attack != null && autoAttack == null
		// && (attack.skill.getType() & Skill.TYPE_ATTACK) != 0
		// && attack.targetRef != null) { // 如果是攻击技能
		// autoAttack = new Attack(skills.getSkill(1), this, ObjectAccessor
		// .getGameObject(attack.targetRef), true);
		// }
	}

	public int prepareSkillAttack(int instanceId, int skillId, int offsetTime, int targetType) {
//		offsetTime = Math.min(offsetTime, maxSkillOffsetTime);
		checkPlug(offsetTime);
		int retCode = 0;
		if (attack == null) {
			Skill skill = skills.getSkill(skillId);
			if (skill != null) {
				if (skill.getLevel() == 0) {
					retCode = 5;
				} else {
					if (!coolDowns.atCoolDown(skill.getCDGroup())) { // 检查技能CD时间
						GameObject target = ObjectAccessor
								.getGameObject(instanceId);
						if(target==null)
							target = ObjectAccessor.getPlayer(instanceId);
						if(instanceId!=id && targetType==Attack.ATTACK_TYPE_ASYNC_TARGET && !(target!=null && target.type==GameObject.TYPE_CREATURE))
							target = ObjectAccessor.getAsyncGameObject(instanceId, map.map.asyncbattleInstanceId);
						if(targetType==Attack.ATTACK_TYPE_ASYNC_SOURCE && (skill.getType() & Skill.TYPE_AID)==Skill.TYPE_AID)
							target = ObjectAccessor.getAsyncGameObject(instanceId, map.map.asyncbattleInstanceId);
						if (!(target instanceof Unit)) {
						    if(target == null && ObjectAccessor.players.containsKey(instanceId)){
						        log.info("[OBJECTPLAYERERROR]"+LogUtil.getPlayerLogString(this)+"INSTANCEID["+instanceId+"]");
						    }
							retCode = 8;
						} else {
							Unit unit = (Unit) target;
							if (unit != this) {
								if(unit.isWorldboss||unit.id==8323136 || unit.id==8323137 || unit.id==8257717 || unit.id==8192136 || unit.id==8192148 || unit.id==8192147 || unit.id==8192026 || unit.id==8192025 || unit.id==8192027){//如果被攻击者是世界boss就把盾取消
									pvpFactionTime=0;
								}
								VMap map = getVMap();
								if (!map.mapDef.mapInfo.canSee(x, y, unit.x,
										unit.y)) {
									retCode = 14;
								} else {
									retCode = super.prepareSkillAttack(unit,
											skill, offsetTime);
								}
							} else {
								retCode = super.prepareSkillAttack(unit, skill,
										offsetTime);
							}

						}
					} else {
						retCode = 10;
					}
				}
			} else {
				retCode = 5;
			}
		} else {
			retCode = 2;
		}
		if (retCode != 0) {
			Packet pt = new Packet(OpCode.ATTACK_FAIL_SERVER);
			pt.put((byte) retCode);
			pt.putInt(this.instanceId);
			pt.putInt(instanceId);
			pt.putInt(skillId);
			send(pt);
		}else{
			coolDowns.setCommonCD(500);
		}
		return retCode;
	}
	
	private void checkPlug(int offsetTime){
		try {
			if(antiPlug!=null && !antiPlug.isBot && offsetTime>maxSkillOffsetTime){
				antiPlug.score1 += 1;
				if(antiPlug.score1>=maxBotScore){
					LogUtil.logAntiPlug(this, "SKILLOFFSET");
					antiPlug.isBot = true;
				}
			}
		} catch (Exception e) {
		}
	}

	@Override
	public int canAttack(GameObject unit) {
		if (unit.type == GameObject.TYPE_PLAYER && this.faction != unit.faction) {// 20级内的敌对玩家不允许攻击
			Player p = (Player) unit;
			if(!p.isPvpFaction())
				return 13;
		}
		return super.canAttack(unit);
	}

	public void breakAllActions() {
		itemUse = null;
		cancelExchange();
		autoAttack.clear();
		breakAutoNaturalEnhance();
		breakAutoAddHole();
		breakAutoEquipEnhance();
	}

//	public void removePvpFlag() {
//		if (isPvp()) {
//			unPvp();
//		}
//		if (isPvpFaction() && level <= MAX_PVE_LEVEL) {
//			unPvpFaction();
//		}
//	}

	protected DieCallback dieCallback() {
		if (map.map != null) {
			if (map.map.manager.dieCallback() != null) {
				return map.map.manager.dieCallback();
			}
		}
		return Server.server.world.dieCallback;
	}

	protected MoveCallback moveCallback() {
		if (map.map != null) {
			if (map.map.manager.moveCallback() != null) {
				return map.map.manager.moveCallback();
			}
		}
		return null;
	}
	

	
	
	protected void realDie(Unit source){
		super.realDie(source);
		moveType |= MOVE_POINT_STATE;
		moveType |= MOVE_HPMP;
		breakAllActions();
		if (source != null && source.type != GameObject.TYPE_PLAYER) { // 如果是被玩家杀死的，比如PK或者切磋状态是不掉耐久的
			equipments.decAllArmorRemainDuration(6);
		}
		if (horse != null) {
			horse.equs.decAllArmorDuration(6, 100, this);
		}
		if (!isAlive())
			dieCallback().die(this, source);
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
		if(map!=null){
			StringBuilder sb = new StringBuilder();
			sb.append("[PLAYERDIE]"+LogUtil.getPlayerLogString(this)+"LVL["+level+"]MAP["+map.id+"]X["+x+"]Y["+y+"]SOURCETYPE["+(source==null ? 0 : source.type)+"]");
			if(source!=null){
				if(source.type!=GameObject.TYPE_PLAYER){
					sb.append("SOURCEID[");
					sb.append(source.instanceId);
					sb.append("]");
				}else{
					Player sourcePlayer = ObjectAccessor.getPlayer(source.id);
					if(sourcePlayer!=null){
						sb.append("SOURCEID[");
						sb.append(sourcePlayer.id+"]SOURCEACC[");
						sb.append(sourcePlayer.accountId+"]SOURCELVL[");
						sb.append(sourcePlayer.level+"]");
					}
				}
			}
			log.info(sb.toString());
		}
	}

//	@Override
//	public void die(Unit source) {
//		super.die(source);
//	}

	public void relive(ReliveOption option) {
		ReliveTransferCall call = new ReliveTransferCall(this, option);
		Server.server.getWorld().schedule(call);
		unMoving();
		// acceptMoving = false;
		reliveOptions = null;
	}

	public void addReliveOption(ReliveOption option) {
		if (reliveOptions != null) {
			reliveOptions.addOption(option, true);
			send(reliveOptions.getRelivePacket());
		}
	}

	/**
	 * 根据reliveOptionId来进行复活
	 * 
	 * @param reliveId
	 */
	public void relive(int reliveId) {
		if (reliveOptions != null) {
			ReliveOption option = reliveOptions.get(reliveId);
			if (option != null) {
				relive(option);
			}
		}
	}

	@Override
	public void relive(int hp, int mp) {
		reliveOptions = null;
		setHp(hp, false);
		setMp(mp, false);
		
		//特殊处理坐骑天命套装效果579BUFF
		buffs.removeBuff(Horse.jewelBuffId);
		processHorseRideBuff(); 
		
		initBuffs();
		refreshProperties(false);
		moveType = MOVE_ALL;
		state &= MASK_CLEAR;
		// log.debug("relive");
		lastMoveTime = CommonUtil.currentMillis();
		ignoreMoveTime = Time.currTime + 5000;
		if(isKing()==1){
			setKing();
			buffs.addBuff(BuffUtil.createSuiteBuff(216, 1));
		}
	}
	
	public void relive2(int hp, int mp) {
		reliveOptions = null;
		setHp(hp, false);
		setMp(mp, false);
		
		//特殊处理坐骑天命套装效果579BUFF
		buffs.removeBuff(Horse.jewelBuffId);
		processHorseRideBuff(); 
		
		initBuffs();
		refreshProperties(false);
		moveType = MOVE_ALL;
		state &= MASK_CLEAR;
		// log.debug("relive");
		lastMoveTime = CommonUtil.currentMillis();
		ignoreMoveTime = Time.currTime + 5000;
		if(isKing()==1){
			setKing();
			buffs.addBuff(BuffUtil.createSuiteBuff(216, 1));
		}
		
		hp = this.maxhp;
		mp = this.maxmp;
		this.setHp(hp, false);
		this.setMp(mp, false);
	
	}
	
	/** 特殊处理坐骑合成的宝石效果*/
	protected void processHorseRideBuff(){
		if(horse!=null){
			horse.processRideBuff(this);
		}
	}

	protected void addTitleBuffs() {
		if (titles.currentEquipTitle != null) {
			Buff buff = titles.currentEquipTitle.newBuff(this);
			if (buff != null) {
				buffs.addBuff(buff);
			}
		}
	}
	
	public void addPlayerSuiteEquipmentBuffs(){
		addSuiteEquipmentBuffs(equipments.equs);
	}

	protected void addSuiteEquipmentBuffs(GameItem[] gameItems) {
		List<Buff> buffList = getSuiteBuffs(gameItems);
		for(Buff suiteBuff : buffList){
			buffs.addBuff(suiteBuff);
		}
		buffList = getSuiteSpecialBuffs(gameItems);
		for(Buff specialBuff : buffList){
			if(buffs.getBuffByID(specialBuff.getId())==null)
				buffs.addBuff(specialBuff);
		}
	}
	
	protected void removeSuiteEquipmentBuffs(GameItem[] gameItems){
		List<Buff> buffList = getSuiteBuffs(gameItems);
		for(Buff suiteBuff : buffList){
			buffs.removeBuff(suiteBuff);
			if(buffs.isNoSingleSuiteBuff(suiteBuff.getId()))
				buffs.removeBuff(suiteBuff.getId());
			
		}
		buffList = getSuiteSpecialBuffs(gameItems);
		for(Buff specialBuff : buffList){
			buffs.removeBuff(specialBuff);
		}
	}
	
	/** 获取套装效果 */
	protected List<Buff> getSuiteBuffs(GameItem[] gameItems){
		List<Buff> buffList = new ArrayList<Buff>();
		Map<SuiteEffects, Integer> map = new HashMap<SuiteEffects, Integer>();
		if (gameItems != null) {
			int flag = 0;
			for (GameItem gameItem : gameItems) {
				if (gameItem != null && gameItem.template != null
						&& gameItem.template.equipment != null
						&& gameItem.template.equipment.suiteEffects != null) {
					Set<SuiteEffects> keys = map.keySet();
					if (keys != null) {
						for (SuiteEffects key : keys) {
							if (key != null && gameItem.template.equipment.suiteEffects == key) {
								int c = map.get(key);
								c++;
								map.put(gameItem.template.equipment.suiteEffects, c);
								flag++;
							}
						}
						if (flag == 0) {
							map.put(gameItem.template.equipment.suiteEffects, 1);
						} else {
							flag = 0;
						}
					} else {
						map.put(gameItem.template.equipment.suiteEffects, 1);
					}
				}
			}
		}
		for (SuiteEffects key : map.keySet()) {
			if(key.type==SuiteEffects.TYPE_NORMAL){
				for (SuiteEffect effect : key.getEffects()) {
					if (map.get(key) >= effect.count) {
						Buff b = effect.buff;
						if(b==null){
							b = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel);
						}else
							if(buffs.isNoSingleSuiteBuff(effect.buff.getId())){
								b = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel);
						}
						buffList.add(b);
					}
				}
			}else if(key.type==SuiteEffects.TYPE_WEIGHT){
				for (SuiteEffect effect : key.getEffects()) {
					if (map.get(key) >= effect.count) {
						Buff b = effect.buff;
						int weight=0;
						for (GameItem gameItem : gameItems) {
							if (gameItem != null
									&& gameItem.template.isEquipment()) {
								if (gameItem != null
										&& gameItem.template.equipment.suiteEffects != null&&gameItem.template.equipment.suiteEffects==key) {
									int equipWeight=gameItem.template.equipment.suiteEffects.weights.get(gameItem.template.id)==null?0:gameItem.template.equipment.suiteEffects.weights.get(gameItem.template.id);
									weight+=(equipWeight);
								}
							}
						}
						if(b==null){
							b = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel,weight);
						}else if(buffs.isNoSingleSuiteBuff(effect.buff.getId())){
							b = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel,weight);
						}
						buffList.add(b);
					}
				}
			}
		}
		return buffList;
	}
	
	/** 获取套装特效 */
	protected List<Buff> getSuiteSpecialBuffs(GameItem[] gameItems){
		List<Buff> buffList = new ArrayList<Buff>();
		if (gameItems != null) {
			for (GameItem gameItem : gameItems) {
				if (gameItem != null && gameItem.template.isEquipment()
						&& gameItem.template.equipment.specialEffect != null) {
					if (buffs.getBuffByID(gameItem.template.equipment.specialEffect.getId()) == null) {
						buffList.add(gameItem.template.equipment.specialEffect);
					}
				}
			}
		}
		return buffList;
	}
	
	public void addHorseSuiteEquipmentBuffs() {
		if(horse!=null){
			GameItem[] gameItems = horse.equs.equs;
			if (gameItems != null) {
				addSuiteEquipmentBuffs(horse.equs.equs);
			}
		}
	}

	protected void addHorseBuffs() {
		if (horse != null) {
			for (Skill skill : horse.skills) {
				Buff buff = skill.newBuff();
				if (buff != null)
					buffs.addBuff(buff);
				else
					log.info("[SKILLERROR]SKILL[" + skill.getId()
							+ "]BUFFNOTFOUND");
			}
		}
	}
	
	/** 检测包月到期*/
	public void processMonthPayTimeout(){
		try{
			MonthlyPayService service = Server.server.getServiceRegistry().getMonthlyPayService();
			if(service.monthlyBuys!=null && service.monthlyBuys.size()>0){
				for(Integer key : service.monthlyBuys.keySet()){
					String property = service.getPoolByType(key);
					if(property!=null){
						if(!service.inService(this, key)&& this.pool.getLong(property, 0l)!=0){
							this.pool.setLong(property, 0l);
							monthPay.put(key,0l);
							GameItem item = ObjectAccessor.createGameItem(key);
							if(item!=null){
								if(key == MonthlyPayService.MONTHPAY_TYPE_TELEPORT){
									if(!ActivityItemEffect.hasTeleportEffect(this)){
									   this.addIntPropertyChangedItem(ChangedItem.TIMEOUT,item.template.id,false,true);
									}
								}else{
									this.addIntPropertyChangedItem(ChangedItem.TIMEOUT,item.template.id,false,true);
								}
							}
						}
					}
				}
			}
			ActivityItemEffect.updateWorldTeleport(this);
		}catch(Exception e){
			
		}
	}
	
	/** 检测工资换天*/
    public void processSalaryChangeDay(){
    	try{
	    	SalaryService service = Server.server.getServiceRegistry().getSalaryService();
			if(salaryDay !=0 && salaryDay != Time.day){
				salaryDay = Time.day;
				service.initSalary(this);
				initDaySalary();
				InstanceSweepService inService = Server.server.getServiceRegistry().getInstanceSweepService();
				inService.initInstanceTimes(this);
				MayDayFestivalService mdService = Server.server.getServiceRegistry().getMayDayFestivalService();
				mdService.initEnterTime(this);
				CardService cardService = Server.server.getServiceRegistry().getCardService();
				cardService.initExpAdded(this);
				GambleService gs = Server.server.getServiceRegistry().getGambleService();
				gs.initGambleCount(this);
				this.pool.setInt(FeastInstanceService.PROPERTY_FEAST_DAYCOUNT,0);
				this.pool.setInt(Player.PROPERTY_XUANWUSHI_DAY, 0);
				this.pool.setInt(Player.PROPERTY_XUANWUSHI_SALARYDAY, 0);
				this.pool.setInt(Player.PROPERTY_XUANWUSHI_BOSSDAY, 0);
				this.pool.setInt(Player.PROPERTY_XUANWUSHI_QUESTDAY, 0);
			}else{
				daySalary = this.pool.getInt(SalaryService.PPOPERTY_SALARY_DAY,0);
			}
    	} catch (Exception e){
    		
    	}
    }
    
    /** 检测并删除到期的物品和装备 */
    public void checkItemValidTime(){
    	if(Time.currTime-lastCheckEquipTime>1000*60*10){
	    	//判断玩家身上
    		List<GameItem> list = new ArrayList<GameItem>();
    		for(GameItem item:equipments.equs){
    			if(item!=null && item.template!=null ){
    				if(System.currentTimeMillis()/60000>item.validTime&& item.validTime>0){
    					item.validTime=-1;
    				}
    				if (item != null && item.template.equipment.specialEffect != null&&item.validTime==-1) {
						buffs.removeBuff(item.template.equipment.specialEffect);
						refreshStar7Buff();
						refreshStarState();
						refreshProperties(false);
						processCardBuff();
					}
					list.add(item);
    			}
    		}
    		GameItem[] equipsItems=null;
    		if(list.size()>0){
    			equipsItems=new GameItem[list.size()];
    			for(int i=0;i<equipsItems.length;i++){
    				equipsItems[i]=(GameItem)list.get(i);
    			}
    		}
	    	//背包
    		List<TransactionBagGrid> list1 = new ArrayList<TransactionBagGrid>();
	    	for(TransactionBagGrid grid : bag.getGrids()){
	    		if(grid==null){
	    			continue;
	    		}
				GameItem item = grid.getItem();
				if(item!=null && item.template!=null){
					int leaveTime = item.validTime;
					if(System.currentTimeMillis()/60000>leaveTime&& item.validTime>0){
	    				item.validTime=-1;
					}
					if(leaveTime==-1&&!item.template.isEquipment()){
						PlayerTransaction tx = newTransaction("DELINVALIDITEM");
						GameItem item0  = bag.removeGameItem(item.template.id, item.instanceId, 1, tx, false);
						if(item0!=null)
							tx.commit();
						else
							tx.rollback();
					}else if(leaveTime>0||item.template.isEquipment()){
						list1.add(grid);
					}
				}
			}
	    	TransactionBagGrid[] transactionBagGrid=null;
	    	if(list1.size()>0){
	    		transactionBagGrid=new TransactionBagGrid[list1.size()];
    			for(int i=0;i<transactionBagGrid.length;i++){
    				transactionBagGrid[i]=(TransactionBagGrid)list1.get(i);
    			}
    		}
	    	InvalidItem invalidItem=new InvalidItem(equipsItems,transactionBagGrid);
	    	this.changed.addChangedItem(invalidItem);
    		lastCheckEquipTime = Time.currTime;
    	}
    }
    
    public void checkTitleValidTime(){
    	if(Time.currTime-lastCheckTitlesTime>1000*60*10){
    		List<Integer> deleteTitles = new ArrayList<Integer>();
    		for(Title title : titles.titles.values()){
    			int validTime = titles.getVliadTime(title.id);
    			if(validTime>0&&validTime<System.currentTimeMillis()/60000){
    				deleteTitles.add(title.id);
    			}
    		}
    		for(int titleId : deleteTitles){
    			titles.removeTitle(titleId);
    		}
    		lastCheckTitlesTime=Time.currTime;
    	}
    }
    
    public void processAccumulateCharge(){
    	ChargeActivityService  service = Server.server.getServiceRegistry().getChargeActivityService();
    	FirstCharge firstCharge = service.getFirstCharge(accountId,false);
    	if(firstCharge!=null &&!service.hasGetMulGift(this.accountId)){    	
	    	long leftTime = System.currentTimeMillis() - firstCharge.pool.getLong(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATETIME, 0);
	    	if(leftTime<ChargeActivityService.FIFTEEN_DAY){
	    		int oldDay = firstCharge.pool.getInt(ChargeActivityService.PROPERTY_ACCUMULATECHARGE_LASTTIME,15);
	    		int newDay = (int)((ChargeActivityService.FIFTEEN_DAY-leftTime)/(24*60*60*1000))+1;
	    		if(oldDay!=newDay){
	    			this.addIntPropertyChangedItem(ChangedItem.MULCHARGE_DAYS,newDay,false,true);
	    			firstCharge.pool.setInt(ChargeActivityService.PROPERTY_ACCUMULATECHARGE_LASTTIME,newDay);
	    			service.addFirstCharge(this.accountId, firstCharge);
	    		}
	    	}else{
    			firstCharge.pool.remove(ChargeActivityService.PROPERTY_ACCUMULATECHARGE_LASTTIME);
    			//firstCharge.pool.remove(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATEED);
    			this.addIntPropertyChangedItem(ChangedItem.MULCHARGE_DAYS,0,false,true);
    			service.addFirstCharge(this.accountId, firstCharge);
    		}
    	}
    }
    
	@Override
	public void update(int diff) {
		if(!isInStep && battleType!=TYPE_ASYNC_PLAYER){
			try {
				cycle++;
				// lighthu: 多线程改造后，登录后的用户的请求包处理放到这里进行
				if (this.session != null) {
					this.session.update(diff);
				}
				
				if (systemState == SYSTEMSTATE_DISCONNECTED) {
					if (System.currentTimeMillis() - changeStateStamp >= 30000L) { // 如果大于等于30秒，那么终止所有行动
						removeFromWorld();
						return;
					}
				}
				long startTime = 0;
				if (TimeUtil.monitorPlayerUpdatePerformance) {
					startTime = System.nanoTime();
				}
				if (isAlive() && Time.currTime - lastRestoreTime > 5000) {
					lastRestoreTime = Time.currTime;
					setHp(this.hp + healthrestore, false);
					setMp(this.mp + manarestore, false);
				}
				if (itemUse != null && itemUse.time <= Time.currTime) {
					useItem();
					itemUse = null;
					recordLastAction();
				}
				processActivePower();
				processDie();
				processPvp();
				cds = coolDowns.update();
				try {
					buffs.update(diff);
				} catch (Exception e) {
					log.error(e, e);
				}
				// processClientMove();
				// syncBuffAndClear();
				processAttack(diff);
				processAutoAttack(diff);
				processRide();
				processGather();
				if (systemState == SYSTEMSTATE_READY)
					asmVm.update(diff);
				actions.clear();
				syncWithClient();
				processClientGuid(cycle);
				processClientDirectory(cycle);
				clearCycle();
				processThreats();
				processMove(this);
				processMoveExt();
				processRelive();
				processOnlineExp();
				processHorseOnlineExp();
				prosessAutoNaturalEnhance();
				processAutoEquipEnhance();
				processAutoAddHole();
				processTireState();
				if (cycle % 5 == 0) {
					runToNextPoint();
				}
				processScheduledPacket();
				try {
					processReport();
				} catch (Exception e) {
				}
				try {
					if(id>0)
						buffs.updateLogBuff();
				} catch (Exception e) {
				}
				processAntiPlug();
				errorCount = 0;
				if(System.currentTimeMillis()-lastProcessCardBuffTime>checkCardBuffDis){
					processCardBuff();
					lastProcessCardBuffTime = System.currentTimeMillis();
				}
	//			processWelfare();
				if(id>0)
				     processMonthPayTimeout();
				processOnlineTime();
				if(id>0){
					updateAlchemyByPlayerExpCount();
				}
				if(id>0)
				     processSalaryChangeDay();
				if (TimeUtil.monitorPlayerUpdatePerformance) {
					long used = System.nanoTime() - startTime;
					if (used > TRACE_UNIT_UPDATE_THRESHOLD_NANO) {
						// 如果单个player处理时间超过1毫秒，报警
						log.warn("[PLAYERTOOLONG]ID[" + id + "]TIME[" + (used / 1000000) + "]");
					}
				}
				if(id>0){
				   try{processBook();}catch(Exception e){}
				}
				if(id>0){
					try{processInstanceSweep();}catch(Exception e){}
				}
				if(id>0 && cycle%10 == 0){
					try{processBag();}catch(Exception e){}
				}
				if(id>0 && cycle%lastCheckOnlineTimeCheckCycle == 0){
					try{processOnlineTody();}catch(Exception e){}
				}
				if(id>0 && cycle%10==0){
					try{processAntiBot();}catch(Exception e){}
				}
				if(id>0 && systemState==Player.SYSTEMSTATE_READY){
					try{checkItemValidTime();}catch(Exception e){}
				}
				if(id>0 && systemState==Player.SYSTEMSTATE_READY){
					try{checkTitleValidTime();}catch(Exception e){}
				}
				if(id>0 && cycle%500 == 0){
					try{ processAccumulateCharge(); }catch(Exception e){}
				}
			} catch (Exception e) {
				log.error(e, e);
				log.info("[PLAYERERROR]" + LogUtil.getPlayerLogString(this));
				errorCount++;
				if (errorCount > 10) {
					// 帐号异常，踢出
					try {
						this.logout();
					} catch (Exception e1) {
						log.error(e1, e1);
					}
				}
			}
		}else if(isInStep && Server.isStepServer || battleType==TYPE_ASYNC_PLAYER){
			try {
				cycle++;
				if (this.session != null) {
					this.session.update(diff);
				}
				if (systemState == SYSTEMSTATE_DISCONNECTED) {
					if (System.currentTimeMillis() - changeStateStamp >= 30000L) { // 如果大于等于30秒，那么终止所有行动
						removeFromWorld();
						return;
					}
				}
				if (isAlive() && Time.currTime - lastRestoreTime > 5000) {
					lastRestoreTime = Time.currTime;
					setHp(this.hp + healthrestore, false);
					setMp(this.mp + manarestore, false);
				}
				if (itemUse != null && itemUse.time <= Time.currTime) {
					useItem();
					itemUse = null;
					recordLastAction();
				}
				processDie();
				cds = coolDowns.update();
				try {
					buffs.update(diff);
				} catch (Exception e) {
					log.error(e, e);
				}
				processAttack(diff);
				processAutoAttack(diff);
				processRide();
				processGather();
				if(!battleIngoPlayer)
					syncWithClient();
				clearCycle();
				processThreats();
				processMove(this);
				processMoveExt();
				processRelive();
				processTireState();
				if (cycle % 5 == 0) {
					runToNextPoint();
				}
				processScheduledPacket();
				if(System.currentTimeMillis()-lastProcessCardBuffTime>checkCardBuffDis){
					processCardBuff();
					lastProcessCardBuffTime = System.currentTimeMillis();
				}
				if(id>0){
					   try{processBook();}catch(Exception e){}
				}
				if(ai!=null){
					try {ai.update();} catch (Exception e) {}
				}
			} catch (Exception e) {
				log.error(e, e);
				log.info("[PLAYERERROR]" + LogUtil.getPlayerLogString(this));
				errorCount++;
			}
		}else if(isInStep && !Server.isStepServer){
			try {
				cycle++;
				if (this.session != null) {
					this.session.update(diff);
				}
				if (systemState == SYSTEMSTATE_DISCONNECTED) {
					if (System.currentTimeMillis() - changeStateStamp >= 30000L) { // 如果大于等于30秒，那么终止所有行动
						removeFromWorld();
						return;
					}
				}
				processScheduledPacket();
			} catch (Exception e) {
				
			}
		}
	}
	
	protected void processStepUiPacket(){
		StepClient client = Server.server.getServiceRegistry().getStepClient();
		if(client!=null){
			DispatchPacket dpt = client.uiPackets.get(id);
			if(dpt!=null){
				send(dpt.packet);
				client.uiPackets.remove(id);
			}
		}
	}
	
	protected void processAntiBot(){
		if(attack==null && autoAttack==null)
			return;
		String model = getAccount().getModel();
		if(isAntiBotModel(model)){
			if(antiBot.lastCycleTime==0 || Time.currTime-antiBot.lastCycleTime>=AntiBot.cycle){
				Packet firstRequest = antiBot.buildFistRequestPacket();
				send(firstRequest);
				antiBot.hasReceive = false;
				antiBot.firstReqTime = Time.currTime;
				antiBot.lastCycleTime = Time.currTime;
				antiBot.hasSendFirstReq = true;
				antiBot.secondReqTime = 0;
			}
			if(antiBot.hasSendFirstReq && antiBot.firstReqTime>0 && !antiBot.hasReceive && Time.currTime-antiBot.firstReqTime>=AntiBot.calcTime){
				Packet secondRequest = antiBot.buildSecondRequestPacket();
				send(secondRequest);
				antiBot.secondReqTime = Time.currTime;
				antiBot.hasSendFirstReq = false;
			}
			if(!antiBot.isBot && antiBot.secondReqTime>0 && !antiBot.hasReceive && Time.currTime-antiBot.secondReqTime>=AntiBot.bot_time){
				if(((AbstractClientSession)session).getIdlePacketTime()<AntiBot.calcTime){
					antiBot.isBot = true;
					LogUtil.logAntiBot(this, "[RABOT]", "OUTOFTIME");
					if(antiBotModel==ANTIPLUG_MODEL_NONBENEFIT){
//						Server.server.getServiceRegistry().getPlayerService().mute(id, System.currentTimeMillis()+15*60*1000);
						muteAccount();
					}
				}
			}
		}
	}
	
	public void muteAccount(){
		PlayerService playerService = Server.server.getServiceRegistry().getPlayerService();
		boolean isMuted = playerService.isAccountMuted(accountId);
		if(!isMuted){
			int muteAccountCount = playerService.getAccountMuteCount(accountId);
			long muteTime = getAccountMuteTime(muteAccountCount+1);
			playerService.muteAccount(this, System.currentTimeMillis()+muteTime, muteAccountCount+1);
		}
	}
	
	public boolean isAntiBotModel(String model){
		for(String m : antiBotModels){
			if(m!=null && model!=null && m.equals(model))
				return true;
		}
		return false;
	}
	
	public void checkAntiBot(Packet pt){
		if(attack==null && autoAttack==null)
			return;
		String model = getAccount().getModel();
		if(!isAntiBotModel(model))
			return;
		
		if(antiBot.isBot)
			return;
		
		int tickCount = pt.getInt();
		int exceptTickCount = pt.getInt();
		int moveDistance = pt.getInt();
		int CRC = pt.getInt();
		
		int serverCRC = antiBot.enCodeValue(tickCount, exceptTickCount, moveDistance);
		if(CRC!=serverCRC){
			antiBot.errCount += 1;
			LogUtil.logAntiBot(this, "[RABOTCNT]", MessageFormat.format(
					"TICKCNT[{0}]EXCEPTICKCNT[{1}]MOVEDIS[{2}]CRC[{3}]CRCS[{4}]", tickCount, 
					exceptTickCount, moveDistance, CRC, CRC));
		}
		int timeDis = (antiBot.secondReqTime - antiBot.firstReqTime) / 1000;
		float tickRatio = tickCount/timeDis*1f;
		if(tickRatio>13){
			antiBot.errCount += 1;
			LogUtil.logAntiBot(this, "[RABOTCNT]", MessageFormat.format(
					"TICKCNT[{0}]EXCEPTICKCNT[{1}]MOVEDIS[{2}]CRC[{3}]TICKRATIO[{4}]", tickCount, 
					exceptTickCount, moveDistance, CRC, tickRatio));
		}
		float exceptRatio = 0;
		if(tickCount>0)
			exceptRatio = exceptTickCount/tickCount*1f;
		if(exceptRatio>0.5){
			antiBot.errCount += 1;
			LogUtil.logAntiBot(this, "[RABOTCNT]", MessageFormat.format(
					"TICKCNT[{0}]EXCEPTICKCNT[{1}]MOVEDIS[{2}]CRC[{3}]EXCEPTRATIO[{4}]", tickCount, 
					exceptTickCount, moveDistance, CRC, exceptRatio));
		}
		float speedRatio = moveDistance/timeDis/getSpeed()*1f;
		if(speedRatio>0.3){
			antiBot.errCount += 1;
			LogUtil.logAntiBot(this, "[RABOTCNT]", MessageFormat.format(
					"TICKCNT[{0}]EXCEPTICKCNT[{1}]MOVEDIS[{2}]CRC[{3}]SPEEDRATIO[{4}]", tickCount, 
					exceptTickCount, moveDistance, CRC, speedRatio));
		}
		antiBot.hasReceive = true;
		if(antiBot.errCount>=3){
			antiBot.isBot = true;
			LogUtil.logAntiBot(this, "[RABOT]", MessageFormat.format("TICKCNT[{0}]EXCEPTICKCNT[{1}]MOVEDIS[{2}]CRC[{3}]", 
					tickCount, exceptTickCount, moveDistance, CRC));
			if(antiBotModel==ANTIPLUG_MODEL_NONBENEFIT){
//				Server.server.getServiceRegistry().getPlayerService().mute(id, System.currentTimeMillis()+15*60*1000);
				muteAccount();
			}
		}
	}
	
	protected long getAccountMuteTime(int muteCount){
		return muteCount * PlayerService.baseMuteAccountTime;
	}
	
	/** 统计玩家当天在线时长 */
	protected void processOnlineTody(){
		if(lastLoginTimeMills==0)
			lastLoginTimeMills = lastLoginTime.getTime(); //上次登录时间
		cachedCal.setMillis(lastLoginTimeMills);
		int lastLoginDay = cachedCal.getDayOfYear(); //上次登录的day
		int lastLoginElapseTime = Time.elapseTime(lastLoginTimeMills); //上次登录跟服务器重启时间间隔
		int currentDay = Time.currentDayOfYear; //系统的当前day
		if(currentDay==lastLoginDay){
			//如果是今天登录的，则通过每个cycle进行累计时间
			if(lastCheckOnlineTime<=0)
				this.onlineTimeToday += Math.max(0, Time.currTime-lastLoginElapseTime);
			else
				this.onlineTimeToday += (Time.currTime - lastCheckOnlineTime);
			lastCheckOnlineTime = Time.currTime;
		}else{
			//跨天的,统计当天0点到现在的时间
			cachedCal.setMillis(Time.currDateTimes);
			cachedCal.setHourOfDay(0);
			cachedCal.setMinuteOfHour(0);
			cachedCal.setSecondOfMinute(0);
			int elapseTime = Time.elapseTime(cachedCal.getMillis());
			this.onlineTimeToday = Math.max(0, Time.currTime-elapseTime);
		}
	}
	
	public void processBag(){
		int autoExtendBagItem = -1;
		if(Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_TW))
			autoExtendBagItem = autoExtendBagItem_tw;
		else
			autoExtendBagItem = autoExtendBagItem_pip;
		if(autoExtendBagItem<=0)
			return;
		int baseBagSize = 27 + level / 5;
		int currentBagSize = bag.size + bag.addedSize;
		if(currentBagSize==baseBagSize){
			if(bag.getFreeBagCount()<=3 && pool.getInt(PROPERTY_AUTOEXTENDBAG, 0)!=1){
				PlayerTransaction tx = newTransaction("AUTOEXTENDBAG");
				GameItem item = ObjectAccessor.createGameItem(autoExtendBagItem);
				ShopService service = Server.server.getServiceRegistry().getShopService();
				String pricePast = service.getShopItemYB(extendBagItem_past);//原价
				String priceNow = service.getShopItemYB(extendBagItem_now);	//现在的价格
				
				try {
					bag.addGameItemComplete(item, 1, tx, true);
					tx.commit();
					Packet pt = new Packet(OpCode.OPENUI_SERVER);
					pt.putString("ui_npc_dialog");
					pt.putString("OPENBAG|"+pricePast+"|"+priceNow);
					send(pt);
				} catch (NoEnoughSpaceException e) {
					tx.rollback();
					Server.server.getServiceRegistry().getMailService()
					.sendSystemMailAsync(id, "系统", "自动扩展背包", "自动扩展背包物品", 0, item, 1, "AUTOEXTENDBAG");
					Packet pt = new Packet(OpCode.OPENUI_SERVER);
					pt.putString("ui_npc_dialog");
					pt.putString("MAILLIST|"+pricePast+"|"+priceNow);
					send(pt);
				}
				pool.setInt(PROPERTY_AUTOEXTENDBAG, 1);
			}
		}
	}
	
	public void enhanceCards(PropertyCalculator pc){
		CardService service = Server.server.getServiceRegistry().getCardService();
		for(CardInfo info : cards.equipCards){
			if(info!=null){
				int cardId = info.cardId;
				int cardLevel = info.level;
				Card card = service.getCardByCardId(cardId);
				if(card!=null){
					int cardPropertyType = card.prorertyType;
					int baseValue = card.propertyBaseValue;
					int upLevelValue = card.propertyUpLevelValue;
					int quality = ObjectAccessor.createGameItem(card.itemId).template.quality;
					service.enhanceCardValue(pc, cardLevel, cardPropertyType, baseValue, upLevelValue, quality);
				}
			}
		}
	}
	
	public void enhanceAlchemyData(PropertyCalculator pc){
		AlchemyService service=Server.server.getServiceRegistry().getAlchemyService();
		service.enhanceAlchemyValue(pc, alchemy.practiceLevel, alchemy.pulseIndex, alchemy.acupointNum, alchemy.acupointLevel);
	}
	
	/** 取得角色总工资 */
	public int getPlayerSalary(){
		return pool.getInt(SalaryService.PROPERTY_SALARY, 0);
	}
	
	/** 角色日工资是否达到上限 */
	public boolean isReachDayLimit(){
		return daySalary >= SalaryService.SALARY_DAYLIMIT ? true : false;
	}
	
	/** 初始化日工资 */
	public void initDaySalary(){
		SalaryService salaryService = Server.server.getServiceRegistry().getSalaryService();
		synchronized(salaryService){
			pool.setInt(SalaryService.PPOPERTY_SALARY_DAY,0);
			daySalary = 0;
		}
	}
	
	/** 角色总工资是否达到上限 */
	public boolean isReachLimitTotal(){
		return pool.getInt(SalaryService.PROPERTY_SALARY,0) >= SalaryService.SALARY_LIMIT ? true : false;
	}
	
	/**  取角色日工资 */
	public int getPlayerDaySalary(){
		return pool.getInt(SalaryService.PPOPERTY_SALARY_DAY, 0);
	}
	
	public boolean topNumOfXuanwushi(){
		return (pool.getInt(PROPERTY_XUANWUSHI_DAY, 0)>=XUANWUSHI_DAYLIMIT);
	}
	
	public boolean topBossOfXuanwushi(){
		return (pool.getInt(PROPERTY_XUANWUSHI_DAY, 0)>=XUANWUSHI_DAYLIMIT || pool.getInt(PROPERTY_XUANWUSHI_BOSSDAY, 0)>=XUANWUSHI_BOSSLIMIT);
	}
	
	/**
	 * 增加玄武石数量
	 * @param count
	 * @param type（0为工资限制，1为世界boss限制，2为任务限制）
	 */
	public void addNumOfXuanwushi(int count,int type){
		pool.setInt(PROPERTY_XUANWUSHI_DAY, pool.getInt(PROPERTY_XUANWUSHI_DAY, 0) + count);
		if(type == 0){
			pool.setInt(PROPERTY_XUANWUSHI_SALARYDAY, pool.getInt(PROPERTY_XUANWUSHI_SALARYDAY, 0) + count);
		}else if(type == 1){
			pool.setInt(PROPERTY_XUANWUSHI_BOSSDAY, pool.getInt(PROPERTY_XUANWUSHI_BOSSDAY, 0) + count);
		}else if(type == 2){
			pool.setInt(PROPERTY_XUANWUSHI_QUESTDAY, pool.getInt(PROPERTY_XUANWUSHI_QUESTDAY, 0) + count);
		}
	}
	
	/**
	 * 获取可增加玄武石数量
	 * @param count
	 * @param type（0为工资限制，1为世界boss限制，2为任务限制）
	 * @return
	 */
	public int addXuanwuItem(int count,int type){
		int oldValue = pool.getInt(PROPERTY_XUANWUSHI_DAY, 0);
		int addValue = Math.min(oldValue+count, Player.XUANWUSHI_DAYLIMIT);
		int dayLimit = addValue - oldValue;
		int typeLimit = 0;
		if(type == 0){//工资玄武石类型
			oldValue = pool.getInt(PROPERTY_XUANWUSHI_SALARYDAY, 0);
			addValue = Math.min(oldValue+count, Player.XUANWUSHI_SALARYLIMIT);
			typeLimit = addValue - oldValue;
		}else if(type == 1){//野外boss
			oldValue = pool.getInt(PROPERTY_XUANWUSHI_BOSSDAY, 0);
			addValue = Math.min(oldValue+count, Player.XUANWUSHI_BOSSLIMIT);
			typeLimit = addValue - oldValue;
		}else if(type == 2){//每日任务限制
			oldValue = pool.getInt(PROPERTY_XUANWUSHI_QUESTDAY, 0);
			addValue = Math.min(oldValue+count, Player.XUANWUSHI_QSTLIMIT);
			typeLimit = addValue - oldValue;
		}
		
		return Math.min(dayLimit, typeLimit);
	}
	
	protected void processOnlineTime(){
		if((System.currentTimeMillis() - lastCheckTime2) >= PlayerOnlineTimeService.MINUTE){
			lastCheckTime2 = System.currentTimeMillis();
			if(Server.server.getServiceRegistry().getPlayerOnlineTimeService().players.contains(id)){
				return;
			}
			Long onlinetime = this.pool.getLong(PlayerOnlineTimeService.PROPERTY_ONLLINE_TIME, 0L);
			onlinetime += PlayerOnlineTimeService.MINUTE;
			pool.setLong(PlayerOnlineTimeService.PROPERTY_ONLLINE_TIME, onlinetime);
			if(onlinetime  >= PlayerOnlineTimeService.HOUR){
				if(pool.getInt(AwardService.PROPERTY_GETAWARD_NUM, 0)==0 || (this.vipLevel>=3&&pool.getInt(AwardService.PROPERTY_GETAWARD_NUM, 0)==1)){
				      Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_ONLINE_HOUR,this));
				}
				Server.server.getServiceRegistry().getPlayerOnlineTimeService().players.add(id);
			}
		}
	}
	
	protected void processWelfare(){
		WelfareService ws = Server.server.getServiceRegistry().getWelfareService();
		ws.proccessWelfareFinish(this);
	}
	
	protected void processClientGuid(int diff){
		if(guid!=null && id>0){
			guid.update(diff);
		}
	}
	
	protected void processClientDirectory(int diff){
		try {
			if(directory!=null && id>0){
				directory.update(diff);
			}
		} catch (Exception e) {
		}
	}
	
	/**
	 * 刷新上下马卡片buff
	 * @param ride 0上马 1下马
	 */
	public void updateHorseCardBuff(int ride){
		CardService service = Server.server.getServiceRegistry().getCardService();
		for(CardInfo info : cards.horseEquipCards){
			if(info!=null){
				int cardId = info.cardId;
				int cardLevel = info.level;
				Card card = service.getCardByCardId(cardId);
				if(card!=null){
					int buff2Id=card.buff2Id;
					if(buff2Id!=-1){
						buffs.removeBuff(buff2Id);
					}
				}
			}
		}
		for(CardInfo info : cards.horseEquipCards){
			if(info!=null){
				int cardId = info.cardId;
				int cardLevel = info.level;
				Card card = service.getCardByCardId(cardId);
				if(card!=null){
					int buff2Id=card.buff2Id;
					if(buff2Id!=-1){
						if(ride==0&&buffs.getBuffByID(buff2Id)==null){
							Buff skillBuff=BuffUtil.createBuff(buff2Id, cardLevel, this, this, 0);
							buffs.addBuff(skillBuff);
						}
					}
				}
			}
		}		
	}
	
	public void processCardBuff(){
		if(isKing()==1){
			KingItemEffect.isKing(this);
		}
//		CardService service = Server.server.getServiceRegistry().getCardService();
//		for(int buff : service.getCardBuffs())
//			buffs.removeBuff(buff);
//		for(GameItem item : equipments.equs){
//			if(item!=null && item.template!=null && item.template.isEquipment()){
//				Object obj = item.object;
//				if(obj!=null && obj instanceof ItemEnhance){
//					ItemEnhance enhance = (ItemEnhance)obj;
//					for(int i=0;i<enhance.cards.length;i+=2){
//			        	int cardId = enhance.cards[i + 1];
//			            service.addCardBuff(this, cardId, PropertyCalculator.TYPE_PLAYER, id, item.template.id, item.instanceId);
//			        }
//				}
//			}
//		}
//		if(horse!=null){
//			for(GameItem item : horse.equs.equs){
//				if(item!=null && item.template!=null && item.template.isHorseEquipment()){
//					Object obj = item.object;
//					if(obj!=null && obj instanceof ItemEnhance){
//						ItemEnhance enhance = (ItemEnhance)obj;
//						for(int i=0;i<enhance.cards.length;i+=2){
//				        	int cardId = enhance.cards[i + 1];
//				            service.addCardBuff(this, cardId, PropertyCalculator.TYPE_PLAYER, id, item.template.id, item.instanceId);
//				        }
//					}
//				}
//			}
//		}
	}
	
	protected void processAntiPlug(){
		try {
			if(id>0 && antiPlug!=null && !antiPlug.isBot){
				if(antiPlug.lastSendATime==0 || (Time.currTime-antiPlug.lastSendATime>=240000)){
					//下发A
					antiPlug.calc();
					send(antiPlug.getPacketA());
					antiPlug.lastSendATime = Time.currTime;
					antiPlug.sendA = true;
				}
				if(antiPlug.lastSendATime>0 
						&& (Time.currTime-antiPlug.lastSendATime>=120000) 
						&& antiPlug.lastSendBTime==0 
						&& antiPlug.sendA){
					//30秒后下发B
					send(antiPlug.getPacketB());
					antiPlug.lastSendBTime = Time.currTime;
				}
				if(antiPlug.lastSendBTime>0 && Time.currTime-antiPlug.lastSendBTime>240000){
					//时间同步发生,重置
					antiPlug.clear();
				}
				if(antiPlug.lastSendBTime>0 
						&& Time.currTime-antiPlug.lastSendBTime>120000){
					//30秒没有收到客户端校验包则判断为外挂
					antiPlug.clear();
					antiPlug.score += 1;
					if(antiPlug.score>=maxBotScore){
						antiPlug.isBot = true;
						LogUtil.logAntiPlug(this, "TIMEOUT");
					}
				}
				if(antiPlug.D1!=null && antiPlug.D!=null){
					//校验
					if(antiPlug.D1.intValue()!=antiPlug.D.intValue()){
						antiPlug.score += 1;
						if(antiPlug.score>=maxBotScore){
							antiPlug.isBot = true;
							LogUtil.logAntiPlug(this, "ERROR");
						}
					}
					antiPlug.clear();
				}
			}
		} catch (Exception e) {
		}
	}
	
	public void checkPlug(Packet pt){
		String uiModel = pt.getString();
		int value = pt.getInt();
		antiPlug.uiModel = uiModel;
		antiPlug.calc1();
		antiPlug.D1 = value;
		antiPlug.enCode();
	}
	
	protected void processReport(){
		if(map==null || map.map==null || map.map.manager==null)
			return;
		if(map.map.manager instanceof FlagBattleFieldVMapManager){
			// 战场举报挂机
			FlagBattleFieldVMapManager manager = Server.server.getServiceRegistry().getFlagBattleFieldVMapManager();
			if((report.lastActionTime>0 && (Time.currTime-report.lastActionTime>120000) && !manager.hasStarted(this)) || 
					(report.lastActionTime>0 && (Time.currTime-report.lastActionTime>120000) && manager.hasStarted(this)) || 
					(report.effectReport>=3)){
				try {
					goMap(manager.outs[faction-1][0], manager.outs[faction-1][1], manager.outs[faction-1][2]);
					LogUtil.logProcessReport(this, Time.currTime-report.lastActionTime, report.effectReport);
				} catch (VMapException e) {
					log.error(e, e);
				} finally{
					report.clear();
				}
			}
		}else{
			// 野外举报挂机,向GM发信
			if((report.reportPlayerId!=null && report.effectReport>=20)){
				try {
					Account a = (Account)session.getIdentity();
					GMRequest request = new GMRequest(1,id,peony.Messages.STRING_00004,MessageFormat.format(peony.Messages.STRING_01569, name, report.effectReport),map.id,x,y,a.getModel());
					Channel channel = Server.server.getServiceRegistry().getChannelService().getChannel("gm");
					if(channel!=null){
						Packet pt = new Packet(OpCode.ADMIN_GMREQUEST_ADDED_SERVER);
						pt.putInt(request.id);
						pt.put(request.getType());
						pt.putInt(request.getPlayerId());
						pt.putString(request.getPlayerName());
						pt.putString(request.getCause());
						pt.put(request.state);
						pt.putString(request.getSolvent());
						pt.putString(request.getModel());
						pt.putShort(request.getMapId());
						pt.putShort(request.getX());
						pt.putShort(request.getY());
						channel.broadcast(pt, null);
					}
				} catch (Exception e) {
					log.error(e, e);
				} finally{
					report.clear();
				}
			}
			if(report.reportStartTime!=0 && (Time.currTime-report.reportStartTime>=60*60*1000)){
				report.effectReport /= 2;
				report.reportStartTime = Time.currTime;
			}
		}
	}
	
	
	/*
	 * 发送延迟发送的所有包。
	 */
	protected void processScheduledPacket() {
		int size = scheduledPacket.size();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				send(scheduledPacket.get(i));
			}
			scheduledPacket.clear();
		}
		size = scheduledChat.size();
		if (size > 0) {
			ChatService cs = Server.server.getServiceRegistry().getChatService();
			for (int i = 0; i < scheduledChat.size(); i++) {
				cs.addChatMessage(scheduledChat.get(i));
			}
			scheduledChat.clear();
		}
	}
	
	/*
	 * 处理防沉迷系统，根据用户在线时间增加防沉迷BUFF。
	 */
	protected void processTireState() {
		// 每1分钟检查一次疲劳状态，如果是，向用户发出警告
		if ((cycle % 600) == 0 || (!tireChecked && this.systemState == SYSTEMSTATE_READY)) {
			AccountStatService ass = Server.server.getServiceRegistry().getAccountStatService();
			int tireState = ass.getTireState(accountId);
			switch (tireState) {
			case 0:
				tirePercent = 1.0f;
				break;
			case 1:
				if (tirePercent != 0.5f) {
					shout(peony.Messages.STRING_01570, 0xFF0000, 10000);
				} else {
					shout(peony.Messages.STRING_01571, 0xFF0000, 10000);
				}
				tirePercent = 0.5f;
				break;
			case 2:
				tirePercent = 0.0f;
				shout(peony.Messages.STRING_01572, 0xFF0000, 10000);
				break;
			}
			tireChecked = true;
		}
	}
	
	/**
	 * 向此用户发送喊话消息。
	 */
	public void shout(String msg, int color, int time) {
		Packet pt = new Packet(OpCode.SHOUT_SERVER);
		pt.putString(msg);
		pt.putInt(color);
		pt.putInt(time);
		send(pt);
	}
	
	/**
	 * 处理自动资质鉴定
	 */
	protected void prosessAutoNaturalEnhance(){
		if(autoNaturalEnhance!=null){
			boolean ok = false;
			for(int i=0;i<5;i++){
				PlayerTransaction tx = newTransaction("MNE");
				GameItem gameItem = bag.removeGameItem(ItemUtil.ITEM_NATURAL_ENHANCE, -1, 1, tx, true);
				if (gameItem == null) {
					tx.rollback();
					autoNaturalEnhance.cause = 0;
					ok = true;
					LogUtil.logAutoNaturalEnhance(this, autoNaturalEnhance.item, "NOKEY");
					break;
				}
				try {
					decMoney(autoNaturalEnhance.decMoney, tx, false);
					tx.commit();
					ItemUtil.naturalEnhance(autoNaturalEnhance.item,this);
					autoNaturalEnhance.money += autoNaturalEnhance.decMoney;
					autoNaturalEnhance.count++;
					NaturalEnhance[] enhances = ((ItemEnhance)autoNaturalEnhance.item.object).getNaturals();
					if(autoNaturalEnhance.specialAtt==-1){
						for(NaturalEnhance enhance : enhances){
							if(enhance.getLevel()>=autoNaturalEnhance.level){
								ok = true;
							}
						}
					}else{
						for(NaturalEnhance enhance : enhances){
							if(enhance.getAttType() == autoNaturalEnhance.specialAtt && enhance.getLevel()>=autoNaturalEnhance.level){
								ok = true;
							}
						}
					}
					if(ok){
						autoNaturalEnhance.cause = 2;
						LogUtil.logAutoNaturalEnhance(this, autoNaturalEnhance.item, "OK");
						break;
					}
				} catch (NoEnoughValueException e) {
					tx.rollback();
					autoNaturalEnhance.cause = 1;
					ok = true;
					LogUtil.logAutoNaturalEnhance(this, autoNaturalEnhance.item, "NOMONEY");
					break;
				}
			}
			if(ok){
				try {
					if (autoNaturalEnhance.owner instanceof Player) {
						refreshProperties(false);
					} else if (autoNaturalEnhance.owner instanceof Horse) {
						Horse h = (Horse) autoNaturalEnhance.owner;
						h.refreshProperties(false, this);
						if (h == horse) {
							refreshProperties(false);
						}
					}
					Packet pt = new Packet(OpCode.AUTO_NATURALENHANCE_SERVER);
					pt.putInt(autoNaturalEnhance.serial);
					pt.putInt(autoNaturalEnhance.count);
					pt.putInt(autoNaturalEnhance.money);
					pt.put(autoNaturalEnhance.item.toClientBytes());
					pt.put(autoNaturalEnhance.cause);
					send(pt);
					addAction(Action.NATURAL_ENHANCE);
				} catch (Exception e) {
					log.error(e, e);
				} finally{
					autoNaturalEnhance = null;
				}
			}
		}
	}
	
	protected void breakAutoNaturalEnhance(){
		if(autoNaturalEnhance!=null){
			try {
				if (autoNaturalEnhance.owner instanceof Player) {
					refreshProperties(false);
				} else if (autoNaturalEnhance.owner instanceof Horse) {
					Horse h = (Horse) autoNaturalEnhance.owner;
					h.refreshProperties(false, this);
					if (h == horse) {
						refreshProperties(false);
					}
				}
				Packet pt = new Packet(OpCode.AUTO_NATURALENHANCE_SERVER);
				pt.putInt(autoNaturalEnhance.serial);
				pt.putInt(autoNaturalEnhance.count);
				pt.putInt(autoNaturalEnhance.money);
				pt.put(autoNaturalEnhance.item.toClientBytes());
				pt.put(3);
				send(pt);
			} catch (Exception e) {
				log.error(e, e);
			} finally{
				autoNaturalEnhance = null;
			}
		}
	}
	
	/**
	 * 处理自动装备鉴定
	 */
	protected void processAutoEquipEnhance(){
		if(autoEquipEnhance!=null){
			boolean ok = false;
			int times = autoEquipEnhance.leftTimes;
			EnhanceService enhService = Server.server.getServiceRegistry().getEnhanceService();
			for(int i=0;i<times+1;i++){
				if(i>=times){
					autoEquipEnhance.cause = 0;
					ok = true;
					LogUtil.logAutoNaturalEnhance(this, autoEquipEnhance.item, "TIMEOUT");
					break;
				}
				PlayerTransaction tx = newTransaction("AUTOEQUIPENHANCE");
				try {
					decMoney(autoEquipEnhance.decMoney, tx, false);
					tx.commit();
					enhService.autoEquipEnhance(this, autoEquipEnhance.item, autoEquipEnhance.owner);
					autoEquipEnhance.money += autoEquipEnhance.decMoney;
					autoEquipEnhance.count++;
					if(autoEquipEnhance.specialAtt>=0){
						ItemEnhance ie = (ItemEnhance) autoEquipEnhance.item.object;
						if (ie == null) {
							ie = new ItemEnhance();
							autoEquipEnhance.item.object = ie;
						}
						if(autoEquipEnhance.specialAtt<=3){
							int tempNum = Math.round((ie.equipEnhanceData[autoEquipEnhance.specialAtt]*100)/EnhanceService.ENHANCE_MAX[autoEquipEnhance.specialAtt]);
							if(tempNum>=90){
								ok = true;
							}
						}else{
							int count = 0;
							for(int j=0;j<ie.equipEnhanceData.length;j++){
								int tempNum = Math.round((ie.equipEnhanceData[j]*100)/EnhanceService.ENHANCE_MAX[j]);
								if(tempNum>=autoEquipEnhance.level){
									count ++;
								}
							}
							if(autoEquipEnhance.specialAtt==4 && count>=2){
								ok = true;
							}else if(autoEquipEnhance.specialAtt==5 && count>=3){
								ok = true;
							}else if(autoEquipEnhance.specialAtt==6 && count>=4){
								ok = true;
							}
						}
						
					}
					if(ok){
						autoEquipEnhance.cause = 2;
						LogUtil.logAutoNaturalEnhance(this, autoEquipEnhance.item, "OK");
						break;
					}
				} catch (NoEnoughValueException e) {
					tx.rollback();
					autoEquipEnhance.cause = 1;
					ok = true;
					LogUtil.logAutoNaturalEnhance(this, autoEquipEnhance.item, "NOMONEY");
					break;
				}
			}
			if(ok){
				try {
					if (autoEquipEnhance.owner instanceof Player) {
						refreshProperties(false);
					} else if (autoEquipEnhance.owner instanceof Horse) {
						Horse h = (Horse) autoEquipEnhance.owner;
						h.refreshProperties(false, this);
						if (h == horse) {
							refreshProperties(false);
						}
					}
					Packet pt = new Packet(OpCode.AUTO_EQUIPENHANCE_SERVER);
					pt.putInt(autoEquipEnhance.serial);
					pt.putInt(autoEquipEnhance.money);
					pt.putInt(autoEquipEnhance.count);
					pt.put(autoEquipEnhance.item.toClientBytes());
					pt.put(autoEquipEnhance.cause);
					send(pt);
					addAction(Action.EQUIP_ENHANCE);
				} catch (Exception e) {
					log.error(e, e);
				} finally{
					autoEquipEnhance = null;
				}
			}
		}
	}
	
	protected void breakAutoEquipEnhance(){
		if(autoEquipEnhance!=null){
			try {
				if (autoEquipEnhance.owner instanceof Player) {
					refreshProperties(false);
				} else if (autoEquipEnhance.owner instanceof Horse) {
					Horse h = (Horse) autoEquipEnhance.owner;
					h.refreshProperties(false, this);
					if (h == horse) {
						refreshProperties(false);
					}
				}
				Packet pt = new Packet(OpCode.AUTO_EQUIPENHANCE_SERVER);
				pt.putInt(autoEquipEnhance.serial);
				pt.putInt(autoEquipEnhance.money);
				pt.put(autoEquipEnhance.item.toClientBytes());
				pt.put(3);
				send(pt);
			} catch (Exception e) {
				log.error(e, e);
			} finally{
				autoEquipEnhance = null;
			}
		}
	}
	
	protected void processAutoAddHole(){
		if(autoAddHole==null)
			return;
		Random rand = autoAddHole.rand;
		GameItem item = autoAddHole.gameItem;
		int destHole = autoAddHole.destHole;
		ItemEnhance itemEnhance = (ItemEnhance) item.object;
		JewelService js = Server.server.getServiceRegistry().getJewelService();
		List<ItemTemplate> l = js.getAddHoleItem(item.template.useLevel);
		for(int i=0;i<5;i++){
			PlayerTransaction tx = newTransaction("AUTOADDHOLE");
			int needMoney = js.getAddHolePrice(item.template.useLevel,
					itemEnhance.addHole + item.template.equipment.initHole);
			boolean ok = false;
			try {
				decMoney(needMoney, tx, true);
				autoAddHole.decMoney += needMoney;
				for (ItemTemplate it : l) {
					if (bag.removeGameItemIngoreInstanceId(it.id, 1, tx, true) != null) {
						ok = true;
						autoAddHole.useBannerAccount++;
						break;
					}
				}
			} catch (NoEnoughValueException e) {
				tx.rollback();
				Packet pt = new Packet(OpCode.AUTO_ADDHOLE_SERVER);
				pt.putInt(autoAddHole.serial);
				pt.put(autoAddHole.realHoles);
				pt.putInt(autoAddHole.useBannerAccount);
				pt.putInt(autoAddHole.decMoney);
				pt.put(1);
				session.send(pt);
				LogUtil.logAutoAddHole(this, autoAddHole.wantHole, autoAddHole.realHoles, 
						autoAddHole.useBannerAccount,autoAddHole.decMoney);
				autoAddHole = null;
				return;
			}
			if (ok) {
				tx.commit();
				int rate = js.getAddHoleSuccRate(itemEnhance.addHole
						+ item.template.equipment.initHole);
				Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(faction);
				NationSkill2 skill = (NationSkill2)nation.skills.get(2);
				if(skill != null){
					float v = skill.getAddHoleAdded();
					if(v != 0f){
						rate *= (1 + v);
						if(rate > 10000){
							rate = 10000;
						}
					}
				}
				if (rand.nextInt(10000) <= rate) {
					itemEnhance.addHole++;
					autoAddHole.realHoles++;
					if(itemEnhance.addHole==destHole){
						Packet pt = new Packet(OpCode.AUTO_ADDHOLE_SERVER);
						pt.putInt(autoAddHole.serial);
						pt.put(autoAddHole.realHoles);
						pt.putInt(autoAddHole.useBannerAccount);
						pt.putInt(autoAddHole.decMoney);
						pt.put(0);
						session.send(pt);
						//打孔成功事件
				        Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_DIG_SUCCESS, this));
						LogUtil.logAutoAddHole(this, autoAddHole.wantHole, autoAddHole.realHoles, 
								autoAddHole.useBannerAccount,autoAddHole.decMoney);
						autoAddHole = null;
						return;
					}
				}
			}else{
				tx.rollback();
				Packet pt = new Packet(OpCode.AUTO_ADDHOLE_SERVER);
				pt.putInt(autoAddHole.serial);
				pt.put(autoAddHole.realHoles);
				pt.putInt(autoAddHole.useBannerAccount);
				pt.putInt(autoAddHole.decMoney-needMoney);
				pt.put(2);
				session.send(pt);
				LogUtil.logAutoAddHole(this, autoAddHole.wantHole, autoAddHole.realHoles, 
						autoAddHole.useBannerAccount,autoAddHole.decMoney-needMoney);
				autoAddHole = null;
				return;
			}
		}
		
	}
	
	protected void breakAutoAddHole(){
		if(autoAddHole!=null){
			try {
				Packet pt = new Packet(OpCode.AUTO_ADDHOLE_SERVER);
				pt.putInt(autoAddHole.serial);
				pt.put(autoAddHole.realHoles);
				pt.putInt(autoAddHole.useBannerAccount);
				session.send(pt);
			} catch (Exception e) {
				log.error(e, e);
			} finally{
				autoAddHole = null;
			}
		}
	}
	
	protected void processOnlineExp(){
		if(onlineExpTime != 0){
			if(Time.currTime > onlineExpTime){
				Server.server.getServiceRegistry().getExpService().handleOnlineExp(this);
				onlineExpTime = (int) (Time.currTime + ExpService.onlineDis);
			}
		}
	}
	
	protected void processHorseOnlineExp(){
		List<Horse> horses = Server.server.getServiceRegistry().getExpService().getAgentHoeses(this);
		Horse horse1 = null;
		for(Horse h : horses){
			horse1 = h;
		}
		if(horse1!=null && horse1.notOnlineExpTime != 0){
			if(Time.currTime > horse1.notOnlineExpTime){
				Server.server.getServiceRegistry().getExpService().handleHorseExp(this);
				horse1.notOnlineExpTime = (int) (Time.currTime + ExpService.notonlineDis);
			}
		}
		autoActiveHorse(this);
	}
	
	public void autoActiveHorse(Player player){
		try{
			for(Horse h:player.horseBag.horses){
				if(h.itemId == Horse.freeHorse[player.clazz] && !h.isActive()){
					AccountService as = Server.server.getServiceRegistry()
					.getAccountService();
					Account account = as.getAccount(player.accountId);
					long iMoney = account.getLongIMoney() / 100;
					if(iMoney>=100*36){
						h.freeHorseEndTime = 0;
						h.setActive();
						h.addIntPropertyChangedItem(player.changed, ChangedItem.HORSE_STATE, h.state, false);
						h.addStringPropertyChangedItem(player.changed, ChangedItem.HORSE_NAME, h.name, false);
						player.message(-1, "由于赤炎感受到了您对三国的支持与热爱，它体内沉睡的小宇宙爆发了！它被激活了，它将永远忠诚的辅佐于您，快打开坐骑菜单骑上它吧！", -1, -1);
					    break;
					}
					h.freeHorseEndTime = -1;
				}
			}
		}catch(Exception e){
			
		}
	}
	
	@Transient
	protected int lastSystemSendPositionTime;
	
	@Override
	public void processMove(Player p){
		if(lastPosition!=null){
			if((Time.currTime-lastPosition.time)>15*1000&&(Time.currTime-lastSystemSendPositionTime)>15*1000){ //如果超过15秒没发position，系统帮发
				moveType |= GameObject.MOVE_POINT;
				lastSystemSendPositionTime = Time.currTime;
//				lastPosition = new Position(map.id,x,y,Time.currTime,Time.currTime);
			}
		}
		super.processMove(p);
	}
	
	protected void processActivePower(){
		if((Time.currTime-lastRestoreActivePowerTime)>30 * 60 *1000){ //半小时增加2点行动力
			setActivePower(Math.min(activePower + 2, 100));
			lastRestoreActivePowerTime = Time.currTime;
		}
	}
	
	public void setActivePower(int value) {
		if (value != activePower) {
			activePower = value;
			addIntPropertyChangedItem(ChangedItem.ACTIVEPOWER, activePower, false);
		}
	}
	
	public void checkActivePower(){
		int day = pool.getInt(PROPERTY_RESTORE_ACTIVEPOWER_DAY);
		if(day!=Time.day){
			setActivePower(100);
			pool.setInt(PROPERTY_RESTORE_ACTIVEPOWER_DAY, Time.day);
		}
	}

	// protected void processClientMove() {
	// if (isAlive() && systemState == SYSTEMSTATE_READY) {
	// if (moves.size() > 0) {
	// ClientMove m = moves.remove(0);
	// super.move(m.x, m.y);
	// this.direct = m.direct;
	// this.state = (short) (((this.state & 0xFFFE) | (m.state & 0x1)) &
	// 0xFFFF);
	// this.lastMoveTime = m.time;
	// if (pkInfo != null && pkInfo.state == PkInfo.STATE_STARTED) {
	// Server.server.eventManager.fireEvent(new ServiceEvent(
	// ServiceEvent.EVENT_PLAYER_PK_MOVE, this, x, y));
	// }
	// moveType |= MOVE_RUNNING_STATE;
	// }
	// }
	// }

	protected void processPvp() {
		if(warState!=null){
			warState.update(this);
		}
//		if (pvpTime != 0) {
//			if (pvpTime <= Time.currTime) {
//				unPvp();
//			}
//		}
//		if (pvpFactionTime != 0) {
//			if (pvpFactionTime <= Time.currTime) {
//				unPvpFaction();
//			}
//		}
	}

	@Override
	public void addThreatUnit(Unit u, float initThreat, boolean direct) {
		super.addThreatUnit(u, initThreat, direct);
		if (u.type == GameObject.TYPE_PLAYER) {
			if (u.faction != faction && initThreat != 0.0f) {
				Player p = (Player) u;
				p.warState.war(p);
//				if (!p.isPvpFaction()) {
//					p.pvpFaction(PVP_TIME);
//				} else {
//					if (pvpFactionTime != 0) {
//						pvpFactionTime = Time.currTime + PVP_TIME;
//					}
//				}
			}
			enemyPlayers.put(u.id, Time.currTime);
		}
	}

	protected void processMoveExt() {
		if ((moveExtended & MOVEEXT_CREDIT) != 0
				|| (moveExtended & MOVEEXT_GUILD) != 0
				|| (moveExtended & MOVEEXT_TITLE) != 0) {
			Packet pt = getInfoPacket();
			broadcast(pt, this, null, true, false, false);
		}
		if ((moveExtended & MOVEEXT_BUFFS) != 0) {
			Packet pt = getBuffsPacket();
			if(party!=null){
				party.broadcast(pt);
			}
			broadcast(pt, this, null, true, false, false);
		}
		moveExtended = 0;
	}

	@Override
	public boolean breakAttack() {
		Attack oldAttack = attack;
		super.breakAttack();
		if(battleType==TYPE_ASYNC_PLAYER && ai!=null){
			((PlayerBodyAi)ai).skill = null;
			((PlayerBodyAi)ai).nextSkill = null;
		}
		if(attendant!=null){
			Attack oldAttAttack = attendant.attack;
			attendant.breakAttack();
			if(oldAttAttack!=null){
				Packet pt = new Packet(OpCode.ATTACK_FAIL_SERVER);
				pt.put((byte) 15);
				pt.putInt(oldAttAttack.getSourceInstanceId());
				pt.putInt(oldAttAttack.getTargetInstanceId());
				pt.putInt(oldAttAttack.skill.getId());
				send(pt);
			}
		}
		if (oldAttack != null) {
			Packet pt = new Packet(OpCode.ATTACK_FAIL_SERVER);
			pt.put((byte) 15);
			pt.putInt(oldAttack.getSourceInstanceId());
			pt.putInt(oldAttack.getTargetInstanceId());
			pt.putInt(oldAttack.skill.getId());
			send(pt);
			return true;
		}
		return false;
	}

	protected void gatherStart(GatherUnit gu, int serial) {
		gather = new Gather(serial, gu.ref(), Time.currTime + gu.gatherTime);
		gu.isGathering = 1;
		if(gu.isPvp && warState==Player.PVPPVESTATE && gu.isPvp){
			setWarState(Player.PVPSTATE);
		}
	}

	protected void processGather() {
		if (gather != null) {
			if (gather.time <= Time.currTime) {
				GatherUnit gu = (GatherUnit) ObjectAccessor
						.getGameObject(gather.ref.instanceId);
				if (gu != null && gu.isAlive()) {
					gu.gatherEnd(this);
				} else {
					cancelGather(1);
				}
				if(gu!=null)
					gu.isGathering = 0;
				gather = null;
			}
		}
	}

	public void cancelGather(int code) {
		if (gather != null) {
			Packet pt = new Packet(OpCode.GATHER_CANCLED_SERVER);
			pt.putInt(gather.serial);
			pt.put(code);
			send(pt);
			gather = null;
		}
	}

	protected void processAttack(int diff) {
		if (attack != null) {
			recordLastAction();
			int attackRet = 0;
			if ((attackRet = attack.update(diff)) != -1) { // 如果返回-1，说明技能还在施放，返回其他的说明施放成功或者不能施放，全部都得设置成null
				if (attackRet == 0) { // 成功施放
					if (attack != null) {
						if ((attack.skill.getType() & Skill.TYPE_ATTACK) != 0
								&& attack.targetRef != null) {
							autoAttack.time = Time.currTime
									+ AUTO_ATTACK_INTERVAL
									- ObjectAccessor.getSkill(1).getActTime(
											this);
							autoAttack.fired = true;
							autoAttack.ref = attack.targetRef;
							autoAttack.attack = null;
							Packet pt = new Packet(
									OpCode.AUTOATTACK_START_SERVER);
							pt.putInt(autoAttack.ref.instanceId);
							send(pt);
						} else {
							if (autoAttack.fired) {
								autoAttack.time = Time.currTime
										+ AUTO_ATTACK_INTERVAL;
								autoAttack.attack = null;
							}
						}
						if(attack.skill != null)
							lastSkillId = attack.skill.getId();
					}
					broadcast(getMovePacket(moveType),null,this,false,false,false);
				} else {
        			Packet pt = new Packet(OpCode.ATTACK_FAIL_SERVER);
                    pt.put((byte)attackRet);
                    pt.putInt(this.instanceId);
                    pt.putInt(attack.getTargetInstanceId());
                    pt.putInt(attack.skill.getId());
                    send(pt);
				}
				// lastSkillId = attack.skill.getId();
				setAttack(null);
			}
		}
	}

	public void useItem(int gridId, int itemId, int instanceId, int targetId,
			int offsetTime) {
		if (itemUse != null) {
			sendUseItemFail(itemId, peony.Messages.STRING_01573);
			return;
		}
		GameItem item = bag.getGameItem(gridId, itemId, instanceId);
		if (item == null) {
			sendUseItemFail(itemId, peony.Messages.STRING_00588);
			return;
		}
		if (item.template.useType == null
				|| item.template.useType.effect == null) {
			sendUseItemFail(itemId, peony.Messages.STRING_00589);
			return;
		}
		if (item.template.useLevel > level) {
			sendUseItemFail(itemId, MessageFormat.format(peony.Messages.STRING_01574, item.template.useLevel));
			return;
		}
		if (coolDowns.atCoolDown(item.template.useType.coolDownId)) {
			int leaveTime = coolDowns.getLeaveTimeByCoolDownId(item.template.useType.coolDownId);
			sendUseItemFail(itemId, MessageFormat.format(peony.Messages.STRING_01575, TimeUtil.getStringH_M_S(leaveTime/1000)));
			return;
		}
		if(asyncMapInstanceId>0 && !AsyncBattleService.canUse(itemId)){
			sendUseItemFail(itemId, "擂台战中不允许使用此物品");
			return;
		}
		if (item.template.useType.occasion == UseType.OCCASION_BATTLE) {
			if (this.getThreatCount() == 0) {
				sendUseItemFail(itemId, peony.Messages.STRING_00590);
				return;
			}
		} else if (item.template.useType.occasion == UseType.OCCASION_NOBATTLE) {
			if (this.getThreatCount() > 0) {
				sendUseItemFail(itemId, peony.Messages.STRING_00591);
				return;
			}
		} else if (item.template.useType.useClazz != 4
				&& item.template.useType.useClazz != clazz) {
			sendUseItemFail(itemId, peony.Messages.STRING_01576);
			return;
		}
		GameObjectRef target = null;
		if (targetId != -1) {
			GameObject o = ObjectAccessor.getGameObject(targetId);
			if (o != null) {
				if (!o.isAlive()) {
					sendUseItemFail(itemId, peony.Messages.STRING_01577);
				} else {
					target = o.ref();
				}
			} else {
				sendUseItemFail(itemId, peony.Messages.STRING_01577);
				return;
			}
		}
		int time = Time.currTime
				+ Math.max(item.template.useType.spellTime - offsetTime, 0);
		itemUse = new ItemUse(target, gridId, itemId, instanceId, time);
		coolDowns.setCommonCD(500);
		int index = StatService.isInArray(StatService.foodIds, itemId);
		if(index != -1){
			ItemEffect effect = item.template.useType.effect;
			if(effect!=null && effect instanceof GetExpEffect){
				int expLock = this.pool.getInt(Player.PROPERTY_LOCK_EXP, Player.EXP_UNLOCK);  //如果玩家锁住经验，物品不能使用
				if(expLock == Player.EXP_LOCK){
					return;
				}
			}
		   Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_USEITEM,this,itemId,1));
		}
		if(asyncTargetId>0){
			AsyncPlayer battle_target=ObjectAccessor.asyncPlayers.get(AsyncPlayer.getSearchKey(asyncTargetId, map.map.asyncbattleInstanceId));
			if(battle_target!=null){
				battle_target.asyncPlayer.ai.processHpMp(itemId);
			}
		}
	}

	public void sendUseItemFail(int itemId, String msg) {
		Packet pt = new Packet(OpCode.USEITEM_FAIL_SERVER);
		pt.putInt(itemId);
		pt.putString(msg);
		send(pt);
	}

	protected void useItem() {
		int gridId = itemUse.gridId;
		int itemId = itemUse.itemId;
		int instanceId = itemUse.instanceId;
		PlayerTransaction tx = newTransaction("USE");
		TransactionBagGrid grid = bag.removeGridGameItem(gridId, itemId,
				instanceId, 1, tx, true);
		if (grid == null || grid.item == null
				|| grid.item.template.id != itemId) {
			sendUseItemFail(itemId, peony.Messages.STRING_00588);
			tx.rollback();
			return;
		}
		if (grid.item.template.useType == null
				|| grid.item.template.useType.effect == null) {
			sendUseItemFail(itemId, peony.Messages.STRING_00589);
			tx.rollback();
			return;
		}
		if (grid.item.template.useType.occasion == UseType.OCCASION_BATTLE) {
			if (this.getThreatCount() == 0) {
				sendUseItemFail(itemId, peony.Messages.STRING_00590);
				tx.rollback();
				return;
			}
		} else if (grid.item.template.useType.occasion == UseType.OCCASION_NOBATTLE) {
			if (this.getThreatCount() > 0) {
				sendUseItemFail(itemId, peony.Messages.STRING_00591);
				tx.rollback();
				return;
			}
		}
		GameObject target = null;
		if (itemUse.target != null) {
			target = ObjectAccessor.getGameObject(itemUse.target);
			if (target == null) {
				sendUseItemFail(itemId, peony.Messages.STRING_00592);
				tx.rollback();
				return;
			} else {
				if (!target.isAlive()) {
					sendUseItemFail(itemId, peony.Messages.STRING_00592);
					tx.rollback();
					return;
				}
				GameItem item = grid.item;
				ItemEffect effect = item.template.useType.effect;
				if (!effect.isAsync()) {  //不需要异步
					PlayerTransaction tx2 = newTransaction("ITE");
					try {
						effect.use(this, item, (Unit) target, tx2);
						tx2.commit();
						if (grid.item.template.useType.consume || effect.needRemove()) {
							tx.commit();
							if(Server.isStepServer){
								Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
								pt.putInt(StepServer.TYPE_BATTLE_BAGCHANGE);
								pt.putString(Integer.toString(itemId));
								send(pt);
							}
						} else {
							tx.rollback();
						}
						setCoolDown(item.template.useType.coolDownId,
								Time.currTime, Time.currTime
										+ item.template.useType.coolDownTime);

						// 使用成功
						lastItemId = itemId;
						
						// 发送通知
						List<GameItem> nitems = tx2.getNoticeItems();
						if (nitems != null) {
							AddItemEffect.sendItemNotice(nitems, this, item.template.name);
						}
						
						// 需要邮件发送的物品这里发送
						List<GainItem> mitems = tx2.getMailItems();
						if (mitems != null) {
							DBService dbs = Server.server.getServiceRegistry().getDbService();
					        this.message(-1, peony.Messages.STRING_00593, -1, -1);
					        for (GainItem gitem : mitems) {
					            GameItem addItem = gitem.getItem();
					            String itemTitle = addItem.template.name;
					            if (gitem.getCount() > 1) {
					                itemTitle += "x" + gitem.getCount();
					            }
					            Server.server.getServiceRegistry().getMailService().sendSystemMailAsync(this.id, peony.Messages.STRING_00004, itemTitle, "", 0,
					            		gitem.getItem(), gitem.getCount(), "ITE");
					        }
						}
						
						// 记录日志
						LogUtil.logUseItem(this, item, item.template.useType.consume);
					} catch (UseItemException e) {
						tx2.rollback();
						tx.rollback();
						sendUseItemFail(itemId, e.getMessage());
						return;
					}
				}else{
					tx.rollback(); //如果是异步的，那么就放入UseItemCall里去扣除
					UseItemCall useItemCall = new UseItemCall(session,ref(),itemUse);
					Server.server.getServiceRegistry().getDbService().schedule(useItemCall);
				}
			}
		} else {
			tx.rollback();
			sendUseItemFail(itemId, peony.Messages.STRING_01578);
		}
		if(asyncTargetId>0){
			AsyncPlayer battle_target=ObjectAccessor.asyncPlayers.get(AsyncPlayer.getSearchKey(asyncTargetId, map.map.asyncbattleInstanceId));
			if(battle_target!=null){
				battle_target.asyncPlayer.ai.processHpMp(itemId);
			}
		}
	}

	@Override
	public CoolDown setCoolDown(int id, int startTime, int time) {
		CoolDown cd = super.setCoolDown(id, startTime, time);
		Packet pt = new Packet(OpCode.COOLDOWN_SERVER);
		pt.putShort(id);
		pt.putInt(cd.startTime);
		pt.putInt(cd.endTime);
		send(pt);
		return cd;
	}

	protected void processAutoAttack(int diff) {
		if (autoAttack.fired) {
			if (autoAttack.attack != null) {
				recordLastAction();
				int attackResult = autoAttack.attack.update(diff);
				if (attackResult == 3 || attackResult == 4 || attackResult == 8) { // 目标死亡，目标不存在，不能对目标使用时停止自动攻击
					autoAttack.clear();
				}
				if (attackResult == 0 || attackResult == 12
						|| attackResult == 1) { // 如果成功施放 ,或者没有足够mana
					autoAttack.time = Time.currTime + AUTO_ATTACK_INTERVAL; // 两秒钟以后再出招
					autoAttack.attack = null;
				}
			} else {
				if (autoAttack.time <= Time.currTime && autoAttack.fired) {
					GameObject target = null;
					if(autoAttack.ref!=null && autoAttack.ref.battleType==Player.TYPE_ASYNC_PLAYER){
						target = ObjectAccessor.getAsyncGameObject(autoAttack.ref.instanceId, autoAttack.ref.mapInstanceId);
					}else{
						target = ObjectAccessor.getGameObject(autoAttack.ref);
					}
					Skill skill = ObjectAccessor.getSkill(1);
					if (target == null
							|| !target.inRange(this, skill.getDistance(this))) {
						autoAttack.time = Time.currTime + AUTO_ATTACK_INTERVAL;
						autoAttack.attack = null;
					} else {
						autoAttack.attack = new Attack(ObjectAccessor
								.getSkill(1), this, target, 0, map.map.asyncbattleInstanceId, true, true);
					}
				}
			}
		}
	}

	protected void processRelive() {
//		if(relive!=null){
//			reliveOptions = null;
//			relive.relive(this);
//			relive = null;
//		}
		if (reliveOptions != null) {
			ReliveOption option = reliveOptions.update();
			if (option != null) {
				relive(option);
			}
		}
	}

	// protected void syncBuffAndClear() {
	// // List<Buff> removedBuff = buffs.getRemovedBuffs();
	// // List<Buff> addedBuff = buffs.getAddedBuffs();
	// // List<Buff> mergedBuff = buffs.getMergedBuffs();
	// // if (removedBuff.size() > 0 || addedBuff.size() > 0
	// // || mergedBuff.size() > 0) {
	// //// Packet pt = new Packet(OpCode.SYNC_BUFF_SERVER);
	// //// pt.put(removedBuff.size());
	// //// for(Buff b:removedBuff){
	// //// pt.putInt(b.getInstanceID());
	// //// }
	// //// pt.put(addedBuff.size());
	// //// for(Buff b:addedBuff){
	// //// pt.putInt(b.getInstanceID());
	// //// pt.putInt(b.getIconID());
	// //// pt.putInt(b.getEndTime());
	// //// }
	// //// pt.put(mergedBuff.size());
	// //// for(Buff b:mergedBuff){
	// //// pt.putInt(b.getInstanceID());
	// //// pt.putInt(b.getIconID());
	// //// pt.putInt(b.getEndTime());
	// //// }
	// //// send(pt);
	// // buffs.clearHistory();
	// //
	// // }
	// }

	public void syncWithClient() {
		if (changed.isChanged()) {
			changed.sendAndClean(session, this);
		}
		if (cds != null && cds.size() > 0) {
			Packet pt = new Packet(OpCode.COOLDOWN_END_SERVER);
			pt.put(cds.size());
			for (CoolDown cd : cds) {
				pt.putShort(cd.id);
			}
			send(pt);
		}
	}

	/**
	 * 取得指定对象周围指定范围内允许被治疗的对象。玩家只能治疗本队伍的玩家。
	 */
	@Override
	public Unit[] getAidUnits(GameObject ref, int dist) {
		List<Unit> list = new ArrayList<Unit>();
//		if(attendant!=null)
//			list.add(attendant);
		if (this.party == null) {
			// 如果没有在队伍中，则只判断自己
			if (this.map.map == ref.map.map && this.inRange(ref, dist)) {
				list.add(this);
			}
		} else {
			// 检查所有队友
			List<Player> ps = this.party.getPlayerInRange(this.map.map, dist,
					ref.x, ref.y);
			for (Player p : ps) {
			    if (p != ref && p.isAlive() && canAid(p)) {
			        list.add(p);
			    }
			}
		}
		Unit[] ret = new Unit[list.size()];
		list.toArray(ret);
		return ret;
	}

	public void chat(int questId, int npcId, String message, int notifyId) {
		Packet pt = new Packet(OpCode.NPC_CHAT_SERVER);
		pt.putInt(questId);
		pt.putInt(npcId);
		pt.putString(message);
		pt.putInt(notifyId);
		send(pt);
	}

	public void message(int questId, String message, int timeout, int notifyId) {
		Packet pt = new Packet(OpCode.MESSAGE_SERVER);
		pt.putInt(questId);
		pt.putString(message);
		pt.putInt(timeout);
		pt.putInt(notifyId);
		send(pt);
	}
	
	public void addBubble(int npcId, String message, int time){
		Packet pt = new Packet(OpCode.NPC_BUBBLE_SERVER);
		pt.putInt(npcId);
		pt.putString(message);
		pt.putInt(time);
		send(pt);
	}

	public void question(int questId, String question, String options,
			int notifyId) {
		Packet pt = new Packet(OpCode.QUESTION_SERVER);
		pt.putInt(questId);
		pt.putString(question);
		pt.putString(options);
		pt.putInt(notifyId);
		send(pt);
	}

	public GameObject getVictim() {
		if (targetRef != null)
			return ObjectAccessor.getGameObject(targetRef);
		return null;
	}

	public void send(Packet packet) {
		if (session != null && battleType==0)
			session.send(packet);
	}
	
	public void schedule(Packet packet) {
		scheduledPacket.add(packet);
	}

	public void schedule(ChatMessage cm) {
		scheduledChat.add(cm);
	}
	
	@Override
	public void addToMap(VMap map, int x, int y) {
		addToMap(map, x, y, true);
	}

	public void addToMap(VMap map, int x, int y, boolean loading) {
		this.map.setMap(map);
		if (loading) {
			setSystemState(SYSTEMSTATE_LOGINED);
		}
		map.addPlayer(this, x, y);
	}

	public void removeFromMap() {
		if (this.map != null && map.map != null) {
			clearThreats();
			breakAttack();
			cancelGather(2);
			map.removeGameObject(this, true);
			// moves.clear();
			unMoving();
			// map.map =null;
		}
	}
	
	public int getTodayFactionChatCount(){
		int lastday = pool.getInt(PROPERTY_LAST_FATION_CHAT);
		if(lastday!=Time.day){
			pool.setInt(PROPERTY_LAST_FATION_CHAT, Time.day);
			pool.setInt(PROPERTY_TODAY_FACTION_CHAT_COUNT, 0);
			return 0;
		}else{
			return pool.getInt(PROPERTY_TODAY_FACTION_CHAT_COUNT);
		}
	}
	
	public void setTodayFactionChatCount(int value){
		pool.setInt(PROPERTY_TODAY_FACTION_CHAT_COUNT, value);
	}

	public void touchNpc(GameObject unit, int questId) {
		if (unit.type == TYPE_CREATURE) {
			// 特殊处理战役副本于吉BUFF
			if(unit.id==5963804 || unit.id==5963805 || unit.id==5963806 || unit.id==5963807 || 
					unit.id==5963808 || unit.id==5963809){
				if(buffs.getBuffByID(259)==null){
					buffs.addBuff(BuffUtil.createBuff(259, 1, this, this, 0));
					message(-1, peony.Messages.STRING_01579, -1, -1);
				}else{
					message(-1, peony.Messages.STRING_01580, -1, -1);
				}
			}
			if (questId <= -2) {
				Creature npc = (Creature) unit;
				TouchAction[] touchAction = npc.touchAction;
				if (touchAction != null)
					touchAction[-2 - questId].touch(this, npc);
			} else {
				TouchNpcInfo info = new TouchNpcInfo(unit.ref(), questId);
				touchedNpc.add(info);
			}
		}
	}

	public boolean hasTouchNpc(int npcId, int questId) {
		for (TouchNpcInfo info : touchedNpc) {
			if (info.questId == questId && info.npcRef.id == npcId)
				return true;
		}
		return false;
	}
	/***
	 * 获取玩家对应装备相同套效的权重值
	 * @return
	 */
	public int getEquipmentsWeight(SuiteEffects suiteEffects,GameItem[] items){
		int weight=0;
		for(GameItem gameItem:items){
			if (gameItem != null
					&& gameItem.template.isEquipment()) {
				if (gameItem != null
						&& gameItem.template.equipment.suiteEffects != null&&gameItem.template.equipment.suiteEffects==suiteEffects) {
					int equipWeight=gameItem.template.equipment.suiteEffects.weights.get(gameItem.template.id)==null?0:gameItem.template.equipment.suiteEffects.weights.get(gameItem.template.id);
					weight+=equipWeight;
				}
			}
		}
		return weight;
	}
	

	public void unSuiteEffect(EquipmentTemplate template, GameItem[] gameItems) {
//		GameItem[] gameItems = equipments.equs; // 得到player身上的装备
		SuiteEffects effects = template.suiteEffects;
		// 清除Buff
		if (template.suiteEffects != null
				&& template.suiteEffects.getEffects() != null) {
			if (effects != null && gameItems != null) {
				boolean hadUnEquip=false;
				for (SuiteEffect effect : template.suiteEffects.getEffects()) {
					Buff buff = effect.getBuff();
					int type=effect.type;
					if(type==SuiteEffect.TYPE_WEIGHT_CALC){
						if(!hadUnEquip){
							hadUnEquip=true;
							buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel, getEquipmentsWeight(template.suiteEffects,gameItems));
							buffs.removeBuff(buff.getId());
							if(getEquipmentsWeight(template.suiteEffects,gameItems)>0)
							buffs.addBuff(buff);
						}
					}else if(type==SuiteEffect.TYPE_NORMAL){
						if(buffs.isNoSingleSuiteBuff(buff.getId())){//如果要加单例的套装效果必须放到Buffs.noSingleSuiteBuffs里
							buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel);
						}
						int effectCount = effect.getCount();
						int count = 0;
						for (GameItem gameItem : gameItems) {
							if (gameItem != null
									&& template.suiteEffects != null
									&& gameItem.template.equipment.suiteEffects != null
									&& gameItem.template.equipment.suiteEffects == template.suiteEffects) {
								count++;
							}
						}
						if (count == effectCount - 1) { // 如果卸下装备后影响原来的套装效果，则清除此效果
							buffs.removeBuff(buff);
							if(buffs.isNoSingleSuiteBuff(buff.getId()))
								buffs.removeBuff(buff.getId());
						}
					}
				}
			}
		}
		
		//判断是否是高级覆盖低级buff
		if(template.suiteEffects!=null){
			SuiteEffect[] effects2 = template.suiteEffects.getEffects();
			ProjectData data = Server.server.getServiceRegistry().getDataService().data;
			if(effects2[0].type==SuiteEffect.TYPE_NORMAL){
				BuffConfig config = (BuffConfig)data.findObject(BuffConfig.class, effects2[0].buff.getId());
		        if(config.mergeStrategy==BuffConfig.MERGE_LEVEL){
		        	SuiteEffect[] suiteEffects = template.suiteEffects.getEffects(); // 添加新装备后可能影响的套装效果
					for (SuiteEffect effect : suiteEffects) {
						Buff buff = effect.buff;
						if(buffs.isNoSingleSuiteBuff(buff.getId())){//如果要加单例的套装效果必须放到Buffs.noSingleSuiteBuffs里
							buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel);
						}
						int effectCount = effect.getCount();
						int count = 0;
						if (gameItems != null) {
							for (GameItem gameItem : gameItems) {
								if (gameItem != null
										&& gameItem.template.isEquipment()) {
									if (gameItem != null
											&& gameItem.template.equipment.suiteEffects != null
											&& gameItem.template.equipment.suiteEffects == template.suiteEffects) {
										count++;
									}
								}
							}
						}
						if (count == effectCount) { // 添加新装备后如果正好构成一个套装效果，则添加此套装效果
							buffs.addBuff(buff);
						}
					}
		        }
			}else if(effects2[0].type==SuiteEffect.TYPE_WEIGHT_CALC){
				int equipWeight=getEquipmentsWeight(template.suiteEffects,gameItems);
				SuiteEffect[] suiteEffects = template.suiteEffects.getEffects(); // 添加新装备后可能影响的套装效果
				boolean hadAddBuff=false;
				for (SuiteEffect effect : suiteEffects) {
					if(effect.type==SuiteEffect.TYPE_WEIGHT_CALC){
						if(!hadAddBuff){
							hadAddBuff=true;
							Buff buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel,equipWeight);
							if(equipWeight>0){
								buffs.addBuff(buff);
							}
						}
					}
				}
			}
		}
	}

	public void horseUnequip(int itemId, int instanceId, int serial,
			int horseInstanceId) {
		Horse h = horseBag.getHorse(horseInstanceId);
		if (h == null) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.HORSE_EQUIP_CLIENT, peony.Messages.STRING_01581);
			return;
		}
		PlayerTransaction tx = newTransaction("HUE");
		GameItem item = h.equs.unequip(itemId, instanceId, this);
		if (item != null) {
			try {
				bag.addGameItemComplete(item, 1, tx, false);
				tx.commit();
				Packet pt = new Packet(OpCode.HORSE_UNEQU_SERVER);
				pt.putInt(serial);
				send(pt);
				if(h == horse){
					unSuiteEffect(item.template.equipment, h.equs.equs);
				}
				if (h == horse)
					refreshHorseAndPlayerProperty();
				else
					h.refreshPropertiesExcepPlayer(false, this);
				refreshHorseStarState();
			} catch (NoEnoughSpaceException e) {
				h.equs.equip(item, this);
				tx.rollback();
				ErrorHandler.sendErrorMessage(this.session, serial,
						OpCode.UNEQUIP_CLIENT, peony.Messages.STRING_01582);
			}
		} else {
			ErrorHandler.sendErrorMessage(this.session, serial,
					OpCode.UNEQUIP_CLIENT, peony.Messages.STRING_00132);
		}

	}

	public void unequip(int itemId, int instanceId, int serial) {
		PlayerTransaction tx = newTransaction("UEQ");
		GameItem item = equipments.unequip(itemId, instanceId);
		if (item != null) {
			try {
				bag.addGameItemComplete(item, 1, tx, false);
				if (item.template.isEquipment()
						&& item.template.equipment != null) {
					EquipmentTemplate template = item.template.equipment;
					if (template.specialEffect != null) {
						buffs.removeBuff(template.specialEffect);
					}
					unSuiteEffect(template, equipments.equs);
				}
				tx.commit();
				refreshStar7Buff();
				refreshProperties(false);
				processCardBuff();
			} catch (NoEnoughSpaceException e) {
				equipments.equip(item);
				tx.rollback();
				this.message(-1, peony.Messages.STRING_01582, -1, -1);
//				ErrorHandler.sendErrorMessage(this.session, serial,
//						OpCode.UNEQUIP_CLIENT, peony.Messages.STRING_01582);
			}
		} else {
//			ErrorHandler.sendErrorMessage(this.session, serial,
//					OpCode.UNEQUIP_CLIENT, peony.Messages.STRING_00132);
			this.message(-1, peony.Messages.STRING_00132, -1, -1);
		}
		refreshStarState();
	}

	public void horseEquip(int itemId, int instanceId, int serial,
			int horseInstanceId) {
		Horse h = horseBag.getHorse(horseInstanceId);
		if (h == null) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.HORSE_EQUIP_CLIENT, peony.Messages.STRING_01581);
			return;
		}
		PlayerTransaction tx = newTransaction("HEQ");
		TransactionBagGrid grid = bag.removeGameItemInstance(itemId,
				instanceId, tx, false);
		if (grid != null && grid.item.template.isHorseEquipment()) {
			GameItem equ = grid.item;
			EquipmentTemplate template = equ.template.equipment;
			if (equ.template.hasDuration() && equ.duration <= 0) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIP_CLIENT, peony.Messages.STRING_01583);
				tx.rollback();
				return;
			}
			if (h.level < template.useLevel) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIP_CLIENT, peony.Messages.STRING_01584);
				tx.rollback();
				return;
			}
			if ((template.agilityLimit > 0 && h.agility < template.agilityLimit)
					|| (template.strengthLimit > 0 && h.strength < template.strengthLimit)
					|| (template.intelligentLimit > 0 && h.intellect < template.intelligentLimit)
					|| (template.staminaLimit > 0 && h.stamina < template.staminaLimit)) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIP_CLIENT, peony.Messages.STRING_01585);
				tx.rollback();
				return;
			}

			if (equ.template.bindType == ItemTemplate.BIND_USED
					|| equ.template.bindType == ItemTemplate.BIND_REWARD) {
//				if (!equ.isBound() || equ.bindInstance == 0
//						|| equ.bindInstance == h.instanceId) {
//					if (equ.bindInstance != h.instanceId) { // 把绑定id设置成马的id
//						equ.bindInstance = h.instanceId;
//						BindChangedItem item = new BindChangedItem(equ);
//						changed.addChangedItem(item);
//					}
//				} else {
//					ErrorHandler.sendErrorMessage(session, serial,
//							OpCode.EQUIP_CLIENT, "装备已经被绑定");
//					tx.rollback();
//					return;
//				}
				if (!equ.isBound()){
					equ.bindInstance = 0;
					BindChangedItem item = new BindChangedItem(equ);
					changed.addChangedItem(item);
				}
			}
			tx.commit();
			GameItem old = h.equs.equip(equ, this);
			if (old != null) {
				tx = newTransaction("HUE");
				grid.addGameItem(old, 1, tx, false);
				tx.commit();
			}
			if (old != null && old.template.equipment.specialEffect != null) {
				buffs.removeBuff(old.template.equipment.specialEffect);
			}
			if (old != null
					&& (old.template.equipment.suiteEffects != null)
					&& (old.template.equipment.suiteEffects != equ.template.equipment.suiteEffects)) { // 如果套装效果一样就没必要去除了
				unSuiteEffect(old.template.equipment, h.equs.equs);
			}
			// 添加套装特效
			if(h == horse){
				if (template.specialEffect != null) {
					Buff specialBuff = buffs
							.getBuffByID(template.specialEffect.getId());
					if (specialBuff == null) {
						buffs.addBuff(template.specialEffect);
					}
				}
				int preHp=0;
				int preMp=0;
				// 添加新装备后添加Buff
				if(template.suiteEffects != null&&template.suiteEffects.type==SuiteEffects.TYPE_NORMAL){
					if (template.suiteEffects != null
							&& ((old == null) || (old != null && old.template.equipment.suiteEffects != template.suiteEffects))) { // 如果套装效果一样就没必要加了
						SuiteEffect[] suiteEffects = template.suiteEffects.getEffects(); // 添加新装备后可能影响的套装效果
						GameItem[] gameItems = h.equs.equs; // 得到player身上的装备
						for (SuiteEffect effect : suiteEffects) {
							Buff buff = effect.buff;
							if(buffs.isNoSingleSuiteBuff(buff.getId()))
								buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel);
							int effectCount = effect.getCount();
							int count = 0;
							if (gameItems != null) {
								for (GameItem gameItem : gameItems) {
									if (gameItem != null
											&& gameItem.template.isEquipment()) {
										if (gameItem != null
												&& gameItem.template.equipment.suiteEffects != null
												&& gameItem.template.equipment.suiteEffects == template.suiteEffects) {
											count++;
										}
									}
								}
							}
							if (count == effectCount) { // 添加新装备后如果正好构成一个套装效果，则添加此套装效果
								buffs.addBuff(buff);
							}
						}
					}
				}else if(template.suiteEffects != null&&template.suiteEffects.type==SuiteEffects.TYPE_WEIGHT){
					if (template.suiteEffects != null
							&& ((old == null) || (old != null && old.template.equipment.suiteEffects != template.suiteEffects))) { // 如果套装效果一样就没必要加了
						SuiteEffect[] suiteEffects = template.suiteEffects.getEffects(); // 添加新装备后可能影响的套装效果
						GameItem[] gameItems = h.equs.equs; // 得到player身上的装备
						boolean isChangeBuff=false;
						for (SuiteEffect effect : suiteEffects) {
							int suiteEffectType = effect.type;
							Buff buff = effect.buff;
							if(suiteEffectType==SuiteEffect.TYPE_WEIGHT_CALC){//套装效果有权重时
								int weight=0;
								for (GameItem gameItem : gameItems) {
									if (gameItem != null
											&& gameItem.template.isEquipment()) {
										if (gameItem != null
												&& gameItem.template.equipment.suiteEffects != null
												&& gameItem.template.equipment.suiteEffects == template.suiteEffects) {
											int equipWeight=gameItem.template.equipment.suiteEffects.weights.get(gameItem.template.id);
											weight+=(equipWeight);
										}
									}
								}
								if(!isChangeBuff){
									isChangeBuff=true;
									preHp=hp;
									preMp=mp;
									buffs.removeBuff(effect.buffId);
									buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel,weight);
									if(weight>0)
									buffs.addBuff(buff);
								}
							}
						}
					}
				
				
				}
			}
			if (h == horse){
				refreshHorseAndPlayerProperty();
			}else
				h.refreshPropertiesExcepPlayer(false, this);
			refreshHorseStarState();
			Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_HORSE_EQUIP, this));
			Packet pt = new Packet(OpCode.HORSE_EQUIP_SERVER);
			pt.putInt(serial);
			send(pt);
		}
	}
	
	public int horseEquipForEQLevelUp(GameItem newEquip,int horseInstanceId){
		Horse h = horseBag.getHorse(horseInstanceId);
		if (h == null) {
			return -1;
		}
		GameItem equ=newEquip;
		if (equ!=null) {
			EquipmentTemplate template = equ.template.equipment;
			GameItem old = h.equs.equip(equ, this);
			if (old != null && old.template.equipment.specialEffect != null) {
				buffs.removeBuff(old.template.equipment.specialEffect);
			}
//			if (old != null
//					&& (old.template.equipment.suiteEffects != null)
//					&& (old.template.equipment.suiteEffects != equ.template.equipment.suiteEffects)) { // 如果套装效果一样就没必要去除了
			if(h == horse){
				unSuiteEffect(old.template.equipment, h.equs.equs);
			}
//			}
			// 添加套装特效
			int preHp=0;
			int preMp=0;
			if(h == horse){
				if (template.specialEffect != null) {
					Buff specialBuff = buffs
							.getBuffByID(template.specialEffect.getId());
					if (specialBuff == null) {
						buffs.addBuff(template.specialEffect);
					}
				}
				// 添加新装备后添加Buff
				if(template.suiteEffects != null&&template.suiteEffects.type==SuiteEffects.TYPE_NORMAL){
					if (template.suiteEffects != null
							&& ((old == null) || (old != null && old.template.equipment.suiteEffects != template.suiteEffects))) { // 如果套装效果一样就没必要加了
						SuiteEffect[] suiteEffects = template.suiteEffects.getEffects(); // 添加新装备后可能影响的套装效果
						GameItem[] gameItems = h.equs.equs; // 得到player身上的装备
						for (SuiteEffect effect : suiteEffects) {
							Buff buff = effect.buff;
							if(buffs.isNoSingleSuiteBuff(buff.getId()))
								buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel);
							int effectCount = effect.getCount();
							int count = 0;
							if (gameItems != null) {
								for (GameItem gameItem : gameItems) {
									if (gameItem != null
											&& gameItem.template.isEquipment()) {
										if (gameItem != null
												&& gameItem.template.equipment.suiteEffects != null
												&& gameItem.template.equipment.suiteEffects == template.suiteEffects) {
											count++;
										}
									}
								}
							}
							if (count == effectCount) { // 添加新装备后如果正好构成一个套装效果，则添加此套装效果
								buffs.addBuff(buff);
							}
						}
					}
				}else if(template.suiteEffects != null&&template.suiteEffects.type==SuiteEffects.TYPE_WEIGHT){
					if (template.suiteEffects != null
							&& ((old == null) || (old != null && old.template.equipment.suiteEffects != template.suiteEffects))) { // 如果套装效果一样就没必要加了
						SuiteEffect[] suiteEffects = template.suiteEffects.getEffects(); // 添加新装备后可能影响的套装效果
						GameItem[] gameItems = h.equs.equs; // 得到player身上的装备
						boolean isChangeBuff=false;
						for (SuiteEffect effect : suiteEffects) {
							int suiteEffectType = effect.type;
							Buff buff = effect.buff;
							if(suiteEffectType==SuiteEffect.TYPE_WEIGHT_CALC){//套装效果有权重时
								int weight=0;
								for (GameItem gameItem : gameItems) {
									if (gameItem != null
											&& gameItem.template.isEquipment()) {
										if (gameItem != null
												&& gameItem.template.equipment.suiteEffects != null
												&& gameItem.template.equipment.suiteEffects == template.suiteEffects) {
											int equipWeight=gameItem.template.equipment.suiteEffects.weights.get(gameItem.template.id);
											weight+=(equipWeight);
										}
									}
								}
								if(!isChangeBuff){
									isChangeBuff=true;
									preHp=hp;
									preMp=mp;
									buffs.removeBuff(effect.buffId);
									buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel,weight);
									if(weight>0)
									buffs.addBuff(buff);
								}
							}
						}
					}
				
				}
			}
			if (h == horse)
				refreshHorseAndPlayerProperty();
			else
				h.refreshPropertiesExcepPlayer(false, this);
			refreshHorseStarState();
			if(preHp!=0){
				if(preHp>maxhp){
					setHp(maxhp, false);
				}else{
					setHp(preHp,false);
				}
			}
			if(preMp!=0){
				if(preMp>maxmp){
					setMp(maxmp, false);
				}else{
					setMp(preMp,false);
				}
			}
			Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_HORSE_EQUIP, this));
		}
		return 0;
	}
	
	public void equip(int itemId, int instanceId, int serial) {
		PlayerTransaction tx = newTransaction("EQU");
		TransactionBagGrid grid = bag.removeGameItemInstance(itemId,
				instanceId, tx, false);
		
		if (grid != null && grid.item.template.isEquipment()
				&& !grid.item.template.isHorseEquipment()) {
			GameItem equ = grid.item;
			EquipmentTemplate template = equ.template.equipment;
			if(equ.template.equipment.limitType>-1&&equ.template.equipment.limitType!=LIMITTYPE_PLAYER){//不是玩家可穿戴类型
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIP_CLIENT, MessageFormat.format("请装配{0}的装备！", CLASS_NAME[clazz]));
				tx.rollback();
				return;
			}
			if (equ.template.equipment.minorType == EquipmentTemplate.MINORTYPE_BOW
					&& clazz != CLASS_2) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIP_CLIENT, MessageFormat.format("请装配{0}的装备！", CLASS_NAME[clazz]));
				tx.rollback();
				return;
			} 
			if (equ.template.hasDuration() && equ.duration <= 0) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIP_CLIENT, peony.Messages.STRING_01583);
				tx.rollback();
				return;
			}
			if (level < template.useLevel) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIP_CLIENT, peony.Messages.STRING_01587);
				tx.rollback();
				return;
			}
			if (template.clazz != -1 && clazz != template.clazz) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIP_CLIENT, peony.Messages.STRING_01586);
				tx.rollback();
				return;
			}
			if ((template.agilityLimit > 0 && agility < template.agilityLimit)
					|| (template.strengthLimit > 0 && strength < template.strengthLimit)
					|| (template.intelligentLimit > 0 && intellect < template.intelligentLimit)
					|| (template.staminaLimit > 0 && stamina < template.staminaLimit)) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIP_CLIENT, peony.Messages.STRING_01585);
				tx.rollback();
				return;
			}
			tx.commit();
			if (equ.template.bindType == ItemTemplate.BIND_USED
					&& !equ.isBound()) {
				equ.bindInstance = 0;
				BindChangedItem item = new BindChangedItem(equ);
				changed.addChangedItem(item);
			}
			GameItem old = equipments.equip(equ);//换装备完成后返回旧装备
			if (old != null) {
				tx = newTransaction("UEQ");
				grid.addGameItem(old, 1, tx, false);
				tx.commit();
			}
			if (old != null && old.template.equipment.specialEffect != null) {
				buffs.removeBuff(old.template.equipment.specialEffect);
			}
			if (old != null
					&& (old.template.equipment.suiteEffects != null)
					&& (old.template.equipment.suiteEffects != equ.template.equipment.suiteEffects)) { // 如果套装效果一样就没必要去除了
				unSuiteEffect(old.template.equipment, equipments.equs);
			}
			// 添加套装特效
			if (template.specialEffect != null) {
				Buff specialBuff = buffs
						.getBuffByID(template.specialEffect.getId());
				if (specialBuff == null) {
					buffs.addBuff(template.specialEffect);
				}
			}
			int preHp=0;
			int preMp=0;
			// 添加新装备后添加Buff
			if(template.suiteEffects != null&&template.suiteEffects.type==SuiteEffects.TYPE_NORMAL){
				if (template.suiteEffects != null
						&& ((old == null) || (old != null && old.template.equipment.suiteEffects != template.suiteEffects))) { // 如果套装效果一样就没必要加了
					SuiteEffect[] suiteEffects = template.suiteEffects.getEffects(); // 添加新装备后可能影响的套装效果
					GameItem[] gameItems = equipments.equs; // 得到player身上的装备
					boolean isChangeBuff=false;
					for (SuiteEffect effect : suiteEffects) {
						int suiteEffectType = effect.type;
						Buff buff = effect.buff;
						if(suiteEffectType==SuiteEffect.TYPE_WEIGHT_CALC){//套装效果有权重时
							int weight=0;
							for (GameItem gameItem : gameItems) {
								if (gameItem != null
										&& gameItem.template.isEquipment()) {
									if (gameItem != null
											&& gameItem.template.equipment.suiteEffects != null
											&& gameItem.template.equipment.suiteEffects == template.suiteEffects) {
										int equipWeight=gameItem.template.equipment.suiteEffects.weights.get(gameItem.template.id);
										weight+=equipWeight;
									}
								}
							}
							if(!isChangeBuff){
								isChangeBuff=true;
								buffs.removeBuff(effect.buffId);
								buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel,weight);
								buffs.addBuff(buff);
							}
						}else if(suiteEffectType==SuiteEffect.TYPE_NORMAL){//套装效果无权重时按老套装执行
							if(buffs.isNoSingleSuiteBuff(buff.getId())){//如果要加单例的套装效果必须放到Buffs.noSingleSuiteBuffs里
								buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel);
							}
							int effectCount = effect.getCount();
							int count = 0;
							if (gameItems != null) {
								for (GameItem gameItem : gameItems) {
									if (gameItem != null
											&& gameItem.template.isEquipment()) {
										if (gameItem != null
												&& gameItem.template.equipment.suiteEffects != null
												&& gameItem.template.equipment.suiteEffects == template.suiteEffects) {
											count++;
										}
									}
								}
							}
							if (count == effectCount) { // 添加新装备后如果正好构成一个套装效果，则添加此套装效果
								buffs.addBuff(buff);
							}
						}
					}
				}
			}else if(template.suiteEffects != null&&template.suiteEffects.type==SuiteEffects.TYPE_WEIGHT){
				if (template.suiteEffects != null) { // 如果套装效果一样就没必要加了
					SuiteEffect[] suiteEffects = template.suiteEffects.getEffects(); // 添加新装备后可能影响的套装效果
					GameItem[] gameItems = equipments.equs; // 得到player身上的装备
					boolean isChangeBuff=false;
					for (SuiteEffect effect : suiteEffects) {
						int suiteEffectType = effect.type;
						Buff buff = effect.buff;
						if(suiteEffectType==SuiteEffect.TYPE_WEIGHT_CALC){//套装效果有权重时
							int weight=0;
							for (GameItem gameItem : gameItems) {
								if (gameItem != null
										&& gameItem.template.isEquipment()) {
									if (gameItem != null
											&& gameItem.template.equipment.suiteEffects != null
											&& gameItem.template.equipment.suiteEffects == template.suiteEffects) {
										int equipWeight=gameItem.template.equipment.suiteEffects.weights.get(gameItem.template.id);
										weight+=equipWeight;
									}
								}
							}
							if(!isChangeBuff){
								isChangeBuff=true;
								preHp=hp;
								preMp=mp;
								buffs.removeBuff(effect.buffId);
								buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel,weight);
								if(weight>0)
								buffs.addBuff(buff);
							}
						}
					}
				}
			}
			refreshStar7Buff();
			refreshProperties(false);
			if(preHp!=0){
				if(preHp>maxhp){
					setHp(maxhp, false);
				}else{
					setHp(preHp,false);
				}
			}
			if(preMp!=0){
				if(preMp>maxmp){
					setMp(maxmp, false);
				}else{
					setMp(preMp,false);
				}
			}
			// moveType |= MOVE_EQUIPMENT;
			Packet pt = new Packet(OpCode.EQUIP_SERVER);
			pt.putInt(serial);
			send(pt);
			if (clazz == Unit.CLASS_2
					&& Equipments.EQU_INDEXES[equ.template.equipment.minorType - 1] == Equipments.HAND) { // 如果是刺客并且更换的是武器，那么重发一遍自动攻击技能
				SkillChangedItem changedItem = new SkillChangedItem(skills
						.getSkill(1), false);
				changed.addChangedItem(changedItem);
			}
			
			// 刷新星辉状态
			refreshStarState();
			
			//玩家换装事件
			Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_CHANGE_EQUIP,this));
			
			// 记录玩家动作
			addAction(Action.EQUIP);
			processCardBuff();
		}else{
			tx.rollback();
		}
	}
	
	/** 刷新星辉状态 */
	public void refreshStarState(){
//		int oldValue = equipments.starState;
		int totalStar = getAveStar(equipments.equs,0);
		int level = 0;
		if(totalStar>=10){
			equipments.starState = (1<<3 | 7);
			level = 4;
		}else if(totalStar>=8){
			equipments.starState = (1<<2 | 3);
			level = 3;
		}else if(totalStar>=6){
			equipments.starState = (1<<1 | 1);
			level = 2;
		}else if(totalStar>=4){
			equipments.starState = 1;
			level = 1;
		}else{
			equipments.starState = 0;
			level = 0;
		}
//		if(oldValue!=equipments.starState){
			refreshStarBuff(level,0);
//			addIntPropertyChangedItem(ChangedItem.STAR_BUFF,oldValue,equipments.starState,false);
//		}
	}
	
	/** 刷新坐骑星辉状态 */
	public void refreshHorseStarState(){
		if(horse!=null){
			int totalStar = getAveStar(horse.equs.equs,1);
			int level = 0;
			if(totalStar>=10){
				level = 4;
			}else if(totalStar>=8){
				level = 3;
			}else if(totalStar>=6){
				level = 2;
			}else if(totalStar>=4){
				level = 1;
			}else{
				level = 0;
			}
			refreshStarBuff(level,1);
		}else{
			for(Buff buff : HORSE_STAR_BUFFS){
				if(buff!=null)
					buffs.removeBuff(buff);
			}
		}
	}
	
	public void refreshStar7Buff(){
		if(Server.server.revision.equals(Server.REVISION_TYPE_TW)){
			if(equipments.getFlashLevel()==6){
				addIntPropertyChangedItem(ChangedItem.STAR_7_BUFF,0,1,false);
			}else{
				addIntPropertyChangedItem(ChangedItem.STAR_7_BUFF,1,0,false);
			}
			if(equipments.getFlashLevel()==6 && buffs.getBuffByID(STAR_7_BUFF_ID)==null){
				buffs.addBuff(BuffUtil.createSuiteBuff(STAR_7_BUFF_ID, 1));
			}else if(equipments.getFlashLevel()!=6 && buffs.getBuffByID(STAR_7_BUFF_ID)!=null){
				buffs.removeBuff(STAR_7_BUFF_ID);
			}
		}
	}
	
	private void refreshStarBuff(int level, int type){
		if(type==0){
			for(Buff buff : STAR_BUFFS){
				if(buff!=null)
					buffs.removeBuff(buff);
			}
			for(int effectLevel=1;effectLevel<=level;effectLevel++){
				Buff buff = STAR_BUFFS[effectLevel];
				if(buff==null){
					buff = BuffUtil.createSuiteBuff(STAR_BUFF[effectLevel], 1);
					STAR_BUFFS[effectLevel] = buff;
				}
				buffs.addBuff(buff);
			}
		}else if(type==1){
			for(Buff buff : HORSE_STAR_BUFFS){
				if(buff!=null)
					buffs.removeBuff(buff);
			}
			for(int effectLevel=1;effectLevel<=level;effectLevel++){
				Buff buff = HORSE_STAR_BUFFS[effectLevel];
				if(buff==null){
					buff = BuffUtil.createSuiteBuff(HORSE_STAR_BUFF[effectLevel], 1);
					HORSE_STAR_BUFFS[effectLevel] = buff;
				}
				buffs.addBuff(buff);
			}
		}
	}
	
	public int getAveStar(GameItem[] items,int type){
		int total = 0;
		int four = 4;
		int six = 6;
		int eight = 8;
		int ten = 10;
		if(items==null || items.length==0)
			return total;
		int count = 0;
		for(GameItem item : items){
			if(item!=null && item.template!=null && item.template.isEquipment())
				count++;
		}
		if(type==0 && count<10)
			return 0;
		if(type==1 && count<7)
			return 0;
		for(GameItem item : items){
			if(item!=null && item.template!=null && item.template.isEquipment()){
				if(item.object!=null && item.object instanceof ItemEnhance){
					ItemEnhance ie = (ItemEnhance)item.object;
					if(ie.getStar()>=ten && ten == 10){
						total = ten;
					}else if(ie.getStar()>=eight && eight==8){
						total = eight;
						ten = 0;
					}else if(ie.getStar()>=six && six==6){
						total = six;
						eight = 0;
						ten = 0;
					}else if(ie.getStar()>=four && four==4){
						total = four;
						six = 0;
						eight = 0;
						ten = 0;
					}else{
						return 0;
					}
				}else{
					return 0;
				}
			}
		}
		return total;
	}

	/**
	 * 套装信息查询
	 */
	public void suiteIndex(int serial, int itemId, int itemInstanceId, int type,
			int instanceId, int horseInstanceId) {

		int white = 0;
		int total = 0;
		ItemTemplate item = ObjectAccessor.getItemTemplate(itemId);
		if (item == null) {
			ErrorHandler.sendErrorMessage(session, serial, instanceId,
					peony.Messages.STRING_01588);
			return;
		}
		EquipmentTemplate equipmentTemplate = item.equipment;
		if (equipmentTemplate == null
				|| (equipmentTemplate.suiteEffects == null && equipmentTemplate.specialEffect == null)) {
			ErrorHandler.sendErrorMessage(session, serial, instanceId,
					peony.Messages.STRING_01588);
			return;
		}

		GameItem[] gameItems = null;
		if(type==0){
			//其他玩家
			if(instanceId>0){
				if(ObjectAccessor.getPlayer(instanceId)==null){
					ErrorHandler.sendErrorMessage(session, serial, instanceId,peony.Messages.STRING_01589);
					return;
				}
				gameItems = ObjectAccessor.getPlayer(instanceId).equipments.equs; // 得到player身上的装备
			}else if(instanceId<0){
				//雕像
				if(Server.server.getServiceRegistry().getFameService().getStatue(instanceId)!=null){
					gameItems = Server.server.getServiceRegistry().getFameService()
					.getStatue(instanceId).equipments.equs;
				}else{
					gameItems = Server.server.getServiceRegistry().getDuelService()
					.getStatue(instanceId).equipments.equs;
				}
			}
		}else if(type==1){
			//玩家自己
			gameItems = equipments.equs;
		}else if(type==2){
			//随从
			if(attendantBag!=null && attendantBag.getAttendant(instanceId)!=null){
				gameItems = attendantBag.getAttendant(instanceId).equs;
			}
		}else if(type==3){
			//坐骑
			if(instanceId==id || instanceId==-1 || instanceId==0){
				Horse h = horseBag.getHorse(horseInstanceId);
				if(h!=null){
					gameItems = h.equs.equs;
				}
			}else{
				Player p = ObjectAccessor.getPlayer(instanceId);
				if(p==null)
					p = Server.server.getServiceRegistry().getFameService().getStatue(instanceId);
				if(p==null)
					p = Server.server.getServiceRegistry().getDuelService().getStatue(instanceId);
				if(p!=null){
					Horse h = null;
					if(p.id<0){
						if(p.horseBag!=null)
							h = p.horseBag.getHorse(horseInstanceId);
						if(h==null)
							h = p.horse;
					}else
						h = p.horseBag.getHorse(horseInstanceId);
					if(h!=null)
						gameItems = h.equs.equs;
				}
			}
		}else if(type == 4){
			Player player = ObjectAccessor.getPlayer(instanceId);
			if(player == null){
				ErrorHandler.sendErrorMessage(session, serial, instanceId,peony.Messages.STRING_01589);
				return;
			}
			if(player.attendantBag!=null && player.attendant!=null){
				gameItems = player.attendant.equs;
			}
		}else if(type == 5){//查看挑战擂台玩家套装信息
			Player p=ObjectAccessor.getPlayer(instanceId);
			if(p == null){
				AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
				p=service.getPlayerInfo(instanceId);
			}
			if(p!=null){
				gameItems = p.equipments.equs;
			}
		}else if(type == 7){
			Player p=ObjectAccessor.getPlayer(instanceId);
			if(p == null){
				AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
				p=service.getPlayerInfo(instanceId);
			}
			if(p != null&&p.attendantView!=null&&p.attendantView.equipments!=null){
				gameItems = p.attendantView.equs;
			}
			if(p!=null && p.attendant!=null && p.attendant.equs!=null){
				gameItems = p.attendant.equs;
			}
		}else if(type == 6){
			Player p=ObjectAccessor.getPlayer(instanceId);
			if(p == null){
				AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
				p=service.getPlayerInfo(instanceId);
			}
			if(p != null&&p.horse!=null&&p.horse.equs!=null){
				gameItems = p.horse.equs.equs;
			}
		}
		if (gameItems == null) {
			ErrorHandler.sendErrorMessage(session, serial, instanceId, peony.Messages.STRING_01588);
			return;
		}
		Packet pt = new Packet(OpCode.SUITE_SERVER);
		pt.putInt(serial);
		pt.putString(equipmentTemplate.specialEffect == null ? ""
				: equipmentTemplate.specialEffect.getDesc());
		if (equipmentTemplate.suiteEffects == null) {
			pt.putString("");
			pt.put(0);
			pt.putString("");
			pt.put(0);
			
		} else {
			if(equipmentTemplate.suiteEffects.type==SuiteEffects.TYPE_NORMAL){//老套装效果
				pt.putString(equipmentTemplate.suiteEffects.getName());
				List<Integer> equips = equipmentTemplate.suiteEffects.getEquips();
				pt.put(equips.size());
				if (equips.size() != 0) {
					for (int equipId : equips) {
						total++;
						int a = 0;
						ItemTemplate item1 = ObjectAccessor
								.getItemTemplate(equipId);
						pt.putString(item1.name);
						for (GameItem gameItem : gameItems) {
							if (gameItem != null
									&& gameItem.template.id == item1.id) {
								white++;
								pt.put(1);
								a++;
							}
						}
						if (a == 0) {
							pt.put(0);
						}
					}
				}
				pt.putString(white + "/" + total);
				SuiteEffect[] effects2 = equipmentTemplate.suiteEffects
						.getEffects();
				//判断是否是高级覆盖低级buff
				ProjectData data = Server.server.getServiceRegistry().getDataService().data;
		        BuffConfig config = (BuffConfig)data.findObject(BuffConfig.class, effects2[0].buff.getId());
		        if(config!=null&&config.mergeStrategy==BuffConfig.MERGE_LEVEL&&EquipLevelUpInfoCall.isLevelUpEquip(effects2[0].buff.getId())){
		        	pt.put(1);
		        	int count = 0;
					for (GameItem gameItem : gameItems) {
						if (gameItem != null
								&& gameItem.template.isEquipment()
								&& gameItem.template.equipment.suiteEffects != null
								&& gameItem.template.equipment.suiteEffects == item.equipment.suiteEffects) {
							count++;
						}
					}
					Buff bufTemp=BuffUtil.createSuiteBuff(effects2[0].buff.getId(), count==0?1:count);
					pt.putString(MessageFormat.format("套装({0}/{1}){2}", count==0?1:count,total>7?7:total,bufTemp.getDesc()));
					pt.put(1);
		        }else{
		        	pt.put(effects2.length);
		        	for (SuiteEffect effect : effects2) {
						int count = 0;
						for (GameItem gameItem : gameItems) {
							if (gameItem != null
									&& gameItem.template.isEquipment()
									&& gameItem.template.equipment.suiteEffects != null
									&& gameItem.template.equipment.suiteEffects == item.equipment.suiteEffects) {
								count++;
							}
						}
						int effectCount = effect.getCount();
						pt.putString(MessageFormat.format(peony.Messages.STRING_01110, effectCount,effect.buff.getDesc()));
						if (count >= effectCount) {
							pt.put(1);
						} else {
							pt.put(0);
						}
					}
		        }
			}else if(equipmentTemplate.suiteEffects.type==SuiteEffects.TYPE_WEIGHT){
				pt.putString(equipmentTemplate.suiteEffects.getName());
//				int weight=equipmentTemplate.suiteEffects.weights.get(item.id);//当前装备权重值
				List<Integer> sampleWeightEquips=getSamepleWeightEquipIds(equipmentTemplate.suiteEffects.weights);//最小权重装备ID列表
				List<Integer> needShowEffectEquips=new ArrayList<Integer>();//需要显示的装备
				//1.查找身上装备
				for (GameItem gameItem : gameItems) {
					if(gameItem==null){
						continue;
					}
					if(equipmentTemplate.suiteEffects.getEquips().contains(gameItem.template.id)){
						needShowEffectEquips.add(gameItem.template.id);
					}
				}
				for(int id:sampleWeightEquips){
					GameItem itemT0=ObjectAccessor.createGameItem(id);//身上的装备
					int index0=-1;
					if(type==3){
						index0=HorseEquipments.getIndex(itemT0.template.equipment.minorType);
					}else{
						if(itemT0.template.equipment.minorType<21){
							index0=Equipments.getIndex(itemT0.template.equipment.minorType);
						}else{
							index0=HorseEquipments.getIndex(itemT0.template.equipment.minorType);
						}
					}
					int count=0;
					for(int id1:needShowEffectEquips){
						GameItem itemT=ObjectAccessor.createGameItem(id1);
						int index=-1;
						if(type==3){
							index=HorseEquipments.getIndex(itemT.template.equipment.minorType);
						}else{
							if(itemT.template.equipment.minorType<21){
								index=Equipments.getIndex(itemT.template.equipment.minorType);
							}else{
								index=HorseEquipments.getIndex(itemT.template.equipment.minorType);
							}
						}
						if(index0!=-1&&index0==index&&itemT0.template.equipment.suiteEffects.getID()==itemT.template.equipment.suiteEffects.getID()){
							count++;
						}
					}
					if(equipmentTemplate.suiteEffects.weights.get(id)!=item.id&&count==0){
						needShowEffectEquips.add(id);
					}
				}
				if(type==1&&item.equipment.equ.job!=clazz){
					pt.put(sampleWeightEquips.size());
					if(sampleWeightEquips.size()!=0){
						for(int equipId:sampleWeightEquips){
							total++;
							int a=0;
							ItemTemplate item1 = ObjectAccessor
							.getItemTemplate(equipId);
							pt.putString(item1.name);
							if (a == 0) {
								pt.put(0);
							}
						}
					}
				}else{
					pt.put(needShowEffectEquips.size());
					if (needShowEffectEquips.size() != 0) {
						for (int equipId : needShowEffectEquips) {
							total++;
							int a = 0;
							ItemTemplate item1 = ObjectAccessor
									.getItemTemplate(equipId);
							pt.putString(item1.name);
							for (GameItem gameItem : gameItems) {
								if (gameItem != null
										&& gameItem.template.id == item1.id) {
									white++;
									pt.put(1);
									a++;
								}
							}
							if (a == 0) {
								pt.put(0);
							}
						}
					}
				}
				int buffId=equipmentTemplate.suiteEffects.getEffects()[0].buffId;
				Buff currentBuff=buffs.getBuffByID(buffId);//玩家自己
				if(type==0 ||type==5){//其他玩家
					int weight=0;
					for(GameItem  item1:gameItems){
						if(item1!=null&&item1.template!=null&&item1.template.equipment!=null&&item1.template.equipment.suiteEffects!=null&&item1.template.equipment.suiteEffects.type==SuiteEffects.TYPE_WEIGHT
						){
//							if(item1.template.equipment.suiteEffects.weights.get(item1.template.id)==item.equipment.suiteEffects.weights.get(item.id)){
								weight+=item1.template.equipment.suiteEffects.weights.get(item1.template.id);
//							}
						}
					}
					currentBuff=BuffUtil.createSuiteBuff(buffId, 1, weight);
					if(instanceId>0){
						Player p=ObjectAccessor.getPlayer(instanceId);
						if(p!=null){
							currentBuff=p.buffs.getBuffByID(buffId);
						}
					}
				}else if(type==4){//其他玩家的随从
					currentBuff=ObjectAccessor.getPlayer(instanceId).attendant.buffs.getBuffByID(buffId);
				}else if(type==1){//玩家背包里
					if(item.equipment.equ.job==clazz){
//						if(equipments.equs[Equipments.getIndex(item.equipment.minorType)]==null||
//								equipments.equs[Equipments.getIndex(item.equipment.minorType)].template.id!=item.id){
//							GameItem itemBag=bag.getGameItem(-1, itemId, itemInstanceId);
//							if(itemBag!=null){
//								int weight=item.equipment.suiteEffects.weights.get(itemId);
//								currentBuff=BuffUtil.createSuiteBuff(item.equipment.suiteEffects.getEffects()[0].buffId, 1, weight);
////								white=1;
//							}
//						}
						if(currentBuff==null){//身上没有套效只显示当前查看装备1件的套效
							int weight=item.equipment.suiteEffects.weights.get(itemId);
							currentBuff=BuffUtil.createSuiteBuff(item.equipment.suiteEffects.getEffects()[0].buffId, 1, weight);
							white=1;
						}
					}else{
						int weight=item.equipment.suiteEffects.weights.get(itemId);
						currentBuff=BuffUtil.createSuiteBuff(item.equipment.suiteEffects.getEffects()[0].buffId, 1, weight);
						white=1;
					}
					
				}else if(type==2){//自己随从
					if(attendantBag!=null && attendantBag.getAttendant(instanceId)!=null){
						Attendant att=attendantBag.getAttendant(instanceId);
						currentBuff=att.buffs.getBuffByID(item.equipment.suiteEffects.getEffects()[0].buffId);
					}
				}else if(type==3 || type == 6){//坐骑
					//其他玩家
					int weight=0;
					for(GameItem  item1:gameItems){
						if(item1!=null&&item1.template!=null&&item1.template.equipment!=null&&item1.template.equipment.suiteEffects!=null&&item1.template.equipment.suiteEffects.type==SuiteEffects.TYPE_WEIGHT
						){
							if(item1.template.equipment.suiteEffects==item.equipment.suiteEffects){
								weight+=(item1.template.equipment.suiteEffects.weights.get(item1.template.id));
							}
						}
					}
					if(weight==0){
						weight=100;
					}
					if(white==0){
						white=1;
					}
					currentBuff=BuffUtil.createSuiteBuff(buffId, 1, weight);
				}
				pt.putString(white + "/" + total);
				pt.put(1);
				String desc=currentBuff==null?"":currentBuff.getDesc();
				pt.putString(MessageFormat.format("套装({0}/{1}){2}", white,total,desc));
				pt.put(1);
			}
		}
		session.send(pt);
	}
	
	/***
	 * 获取套装最小权重的装备ID列表
	 * @param weights
	 * @param weight
	 * @return
	 */
	public static List<Integer> getSamepleWeightEquipIds(Map<Integer, Integer> weights){
		List<Integer> sampleWeightEquipIds=new ArrayList<Integer>();
		if(weights!=null){
			for (Map.Entry<Integer, Integer> entry : weights.entrySet()) {
				int itemId0=entry.getKey();
				int weight0=entry.getValue();
				int count=0;
				for (Map.Entry<Integer, Integer> entry1 : weights.entrySet()) {
					 int itemId1=entry1.getKey();
					 int weight1=entry1.getValue();
					 if(weight0>weight1){
						 count++;
					 }
				}
				if(count==0){
					if(!sampleWeightEquipIds.contains(itemId0)){
						sampleWeightEquipIds.add(itemId0);
					}
				}
			}
		}
		return sampleWeightEquipIds;
	}
	
	/***
	 * 获取相同权重的装备ID列表
	 * @param weights
	 * @param weight
	 * @return
	 */
	public static List<Integer> getSamepleWeightEquipIds(Map<Integer, Integer> weights,int weight ){
		List<Integer> sampleWeightEquipIds=new ArrayList<Integer>();
		if(weights!=null){
			for (Map.Entry<Integer, Integer> entry : weights.entrySet()) {
				   int itemId=entry.getKey();
				   int weight1=entry.getValue();
				   if(weight1==weight){
					   sampleWeightEquipIds.add(itemId);
				   }
			}
		}
		return sampleWeightEquipIds;
	}

	public void uiNotify(int questId, int id, int type, int answer) {
		UINotify notify = new UINotify(questId, id, type, answer);
		uiNotifies.add(notify);
	}

	public boolean hasUINotify(int questId, int id, int type) {
		for (UINotify notify : uiNotifies) {
			if (notify.questId == questId && notify.type == type
					&& notify.id == id)
				return true;
		}
		return false;
	}

	public boolean hasUINotify(int questId, int id, int type, int answer) {
		for (UINotify notify : uiNotifies) {
			if (notify.questId == questId && notify.type == type
					&& notify.id == id && notify.answer == answer)
				return true;
		}
		return false;
	}

	public void loadFinished() {
		if (systemState != SYSTEMSTATE_READY) {
			setSystemState(SYSTEMSTATE_READY);
			loadTimes++;
			Packet pt = new Packet(OpCode.VIEW_ACCEPT_SERVER);
			send(pt);
			if (loadTimes == 1 && serivce!=null) {
				serivce.firstLoad(this);
			}
			map.playerLoadingFinished(this);
		}
		acceptMoving = true;
		lastPosition = new Position(map.id, x, y, Time.elapseTime(System.currentTimeMillis()), -1, getSpeed());
		
		// 进入新场景，强制刷新速度
		float speed = this.speedRating;
		this.speedRating = 0;
		this.setSpeedRatio(speed);
		// lastPosition = new Position(map.id,x,y,Time.currTime,Time.currTime);
		if(map.getId()==AsyncBattleService.battleMap)
			asyncLoadFinish = true;
	}
	
	public void loadFinished1() {
		if(useNewLoadFinish){
			setSystemState(SYSTEMSTATE_READY);
			Packet pt = new Packet(OpCode.VIEW_ACCEPT_SERVER);
			send(pt);
			map.playerLoadingFinished(this);
		}
		loadFinished();
		if(!isInStep && id>0){
			try{processStepUiPacket();}catch(Exception e){}
		}
		if(Server.isStepServer)
			loadFinshed = true;
	} 

	public void clearCycle() {
		touchedNpc.clear();
		uiNotifies.clear();
		kills.clear();
		killPlayers.clear();
		cds = null;
		questToFinishQuestId = -1;
		lastSkillId = -1;
		lastItemId = -1;
		dieCause = 0;
	}

	public void sendBagInfo() {
		Packet pt = new Packet(OpCode.BAG_SERVER);
		pt.put(bag.getSize());
		for (TransactionBagGrid grid : bag.grids) {
			pt.put(grid.toClientByte());
		}
		send(pt);
	}

	public void sendSkillList() {
		// Packet pt = new Packet(OpCode.SKILL_LIST_SERVER);
		// Collection<Skill> ss = skills.getSkills();
		// pt.put(ss.size());
		// for (Skill skill : ss) {
		// pt.put(skill.toClientBytes(this));
		// }
		// send(pt);

		// bookskill
		Packet pt = new Packet(OpCode.SKILL_LIST_SERVER);
		if(skills.bookSkillSize!=Skills.DEFAULT_BOOKSKILL_SIZE){
			skills.bookSkillSize = Skills.DEFAULT_BOOKSKILL_SIZE;
		}
		pt.put(skills.toClientBytes(this));
		send(pt);
	}

	public void addPropertyPoint(int strength, int agility, int stamina,
			int intellect, int serial) {
		if (strength + agility + stamina + intellect > propertyPoint) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.PROPERTYPOINT_ADD_CLIENT, peony.Messages.STRING_01590);
		} else {
			setPropertyPoint(this.propertyPoint - strength - agility - stamina
					- intellect, false);
			strengthAdded += strength;
			agilityAdded += agility;
			staminaAdded += stamina;
			intellectAdded += intellect;

			refreshProperties(false);
			Packet pt = new Packet(OpCode.PROPERTYPOINT_ADD_SERVER);
			pt.put(serial);
			send(pt);
			
			// 记录玩家动作
			addAction(Action.ADD_PROPERTY_POINT);
		}
	}
	
	public void refreshPropertiesPoint(){
		setPropertyPoint(level*2,false);
		strengthAdded = 0;
		agilityAdded = 0;
		staminaAdded = 0;
		intellectAdded = 0;
		refreshProperties(false);
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_SKILL_REFRESH,this));
	}

	public void addSkill(Skill skill) {
		Skill oldSkill = skills.removeSkill(skill.getGroupId(), skill
				.getLevel() - 1);
		skills.addSkill(skill, changed, true);
		if (oldSkill != null) {
			buffs.removeBuff(oldSkill.getId());
		}
		if (skill.getLevel() > 0) {
			Buff b = skill.newBuff();
			if (b != null) {
				buffs.addBuff(b);
			}
			b = skill.getAreaBuff();
			if (b != null)
				buffs.addBuff(b);
		}
	}
	

	public void addBookSkill(Skill skill) {
		Skill oldSkill = skills.removeBookSkill(skill.getGroupId(), skill
				.getLevel() - 1);
		skills.addBookSkill(skill, changed, true);
		if (oldSkill != null) {
			buffs.removeBuff(oldSkill.getId());
		}
		if (skill.getLevel() > 0) {
			Buff b = skill.newBuff();
			if (b != null) {
				buffs.addBuff(b);
			}
			b = skill.getAreaBuff();
			if (b != null)
				buffs.addBuff(b);
		}
	}

	public void addSkillPoint(int skillGroupId, int level, boolean notify,
			int serial) {
		if (level <= 0)
			return;
		if(StatService.isInArray(MarriageService.MATE_SKILL_ID, skillGroupId)!=-1){
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.SKILL_ADDPOINT_CLIENT, "非法操作");
			return;
		}
		Skill oldSkill;
		if ((oldSkill = skills.getSkill(Skills.getSkillId(skillGroupId,
				level - 1))) != null) { // 必须要有前一级的技能
			Skill skill = ObjectAccessor.getSkill(Skills.getSkillId(
					skillGroupId, level));
			if (skill != null) {
				if (skill.getRequireLevel() > this.level) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.SKILL_ADDPOINT_CLIENT, 
							MessageFormat.format(peony.Messages.STRING_01591, skill.getRequireLevel()));
				} else {
					if (skillPoint < skill.getPoint()) {
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.SKILL_ADDPOINT_CLIENT, peony.Messages.STRING_01592);
					} else {
						setSkillPoint(skillPoint - skill.getPoint(), true);
						addSkill(skill);
						Packet pt = new Packet(OpCode.SKILL_ADDPOINT_SERVER);
						pt.putInt(serial);
						pt.put(skill.toClientBytes(this));
						send(pt);
						
						// 记录玩家动作
						addAction(Action.UPGRADE_SKILL);
						if ((skill.getType() & Skill.TYPE_VISIBLE) != 0) {
							addAction(Action.UPGRADE_ACTIVE_SKILL);
						} else {
							addAction(Action.UPGRADE_PASSIVE_SKILL);
						}
					}
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.SKILL_ADDPOINT_CLIENT, peony.Messages.STRING_01593);
			}
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.SKILL_ADDPOINT_CLIENT, peony.Messages.STRING_01594);
		}
	}
	
	public void refreshSkillPoint(int serial) {
		Collection<Skill> skills = super.skills.getSkills();
		super.skills.clear();
		setSkillPoint(PlayerUtil.getGrowSkillPoint(1, level), false);
		buffs.removeAreaBuffs();
		if (party != null) {
			List<Buff> l = party.getAreaBuffs(map.map);
			if (l.size() > 0) {
				for (Buff buff : l) {
					buffs.addBuff(buff);
				}
			}
		}
		for (Skill skill : skills) {
			Buff newBuff = skill.newBuff();
			if (newBuff != null) {
				buffs.removeBuff(newBuff.getId());
			}
		}
		List<Skill> newSkills = ObjectAccessor.getPlayerInitSkills(clazz);
		for (Skill skill : newSkills) {
			if(skill.getClazz() == 5){
				continue;
			}
			if (skill.getId() == 1) { // 如果是自动攻击技能加上
				addSkill(skill);
			} else {
				if (skill.isAutoLearn()
						&& level >= skill.getNextLevel().getRequireLevel()) { // 如果需要学习，并且达到学习条件，加上
					addSkill(skill.getNextLevel());
				} else {
					addSkill(skill);
				}
			}
		}
		refreshProperties(false);
		Packet pt = new Packet(OpCode.SKILL_REFRESH_SERVER);
		pt.putInt(serial);
		send(pt);
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_SKILL_REFRESH,this));
	}

	public void refreshSkillPointWithRule(int serial) {
		int times = pool.getInt(PROPERTY_REFRESH_SKILL);
		int money = PlayerUtil.getRefreshSkillMoney(times);
		PlayerTransaction tx = newTransaction("RSP");
		try {
			decMoney(money, tx, true);
			tx.commit();
			refreshSkillPoint(serial);
			pool.setInt(PROPERTY_REFRESH_SKILL, times + 1);
		} catch (NoEnoughValueException e) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.SKILL_REFRESH_CLIENT, peony.Messages.STRING_00020);
		}
	}

	public boolean isNewUI(){
		boolean ret = false;
		Account acc = this.getAccount();
		if(acc == null){
			return false;
		}
		
		String uimodel = acc.getUiModel();
		if(uimodel == null){
			return false;
		}
		ret = uimodel.startsWith("NewUI_");
		return ret;
	}
	
	public void sendQuestList() {
		boolean isNewUI = isNewUI();
		Packet pt = new Packet(OpCode.QUEST_LIST_SERVER);
		Collection<ASMQuest> quests = asmVm.getQuests();
		pt.put(quests.size());
		for (ASMQuest quest : quests) {
			pt.putInt(quest.getId());
			pt.putInt(quest.getGameQuest().getStartNpc());
			pt.putInt(quest.getGameQuest().getFinishNpc());
			pt.put(quest.getGameQuest().getLevel());
			pt.putString(quest.getGameQuest().getName());
			pt.put(quest.getGameQuest().getClientETF());
			pt.putInts(asmVm.getQuestStore(quest.getId()));
			List<QuestTarget> l = quest.getGameQuest().getTargets();
			pt.put((byte) l.size());
			for (QuestTarget target : l) {
				pt.putString(target.description);
				pt.putString(target.path);
			}
			pt.put(asmVm.isFail(quest.getId()) ? 1 : 0);
//			if(isNewUI){
				pt.putString(quest.getPreDesc(asmVm));
				pt.putString(quest.getUnFinishDesc(asmVm));
				pt.putString(quest.getPostDesc(asmVm));
				pt.putString(quest.getDesc(asmVm));
				PlayerPacketHandler.writeQuestRewardSets(pt,quest.getGameQuest(),this);
//			}
		}
		send(pt);
	}
	
	public Attack getAutoAttack(){
		if(autoAttack!=null){
			return autoAttack.attack;
		}
		return null;
	}
	
	protected void removeUnitEffectBuffState(){
		unFear();
		unDumb();
		unParalyze();
		unStay();
	}
	
	public byte[] toClientBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(id);
			dos.writeUTF(name);
			dos.write(sex);
			dos.write(level);
			dos.write(clazz);
			dos.write(faction);
			dos.writeShort(maxhp);
			dos.writeShort(maxmp);
			dos.writeShort(hp);
			dos.writeShort(mp);
			dos.writeShort(strength);
			dos.writeShort(agility);
			dos.writeShort(stamina);
			dos.writeShort(intellect);
			dos.writeShort(Math.round(attackpowerup));
			dos.writeShort(Math.round(attackpowerdown));
			dos.writeShort(Math.round(spellpower));
			dos.writeShort(Math.round(spellheal));
			dos.writeShort(Math.round(defense));
			dos.writeShort(Math.round(spelldefense));
			dos.writeShort(Math.round(critical * 100));
			dos.writeShort(Math.round(spellcritical * 100));
			dos.writeShort(Math.round(hit * 100));
			dos.writeShort(Math.round(spellhit * 100));
			dos.writeShort(Math.round(dodge * 100));
			dos.writeShort(Math.round(spelldodge * 100));
			dos.writeShort(Math.round(anticrit * 100));
			dos.writeShort(Math.round(defensePercent * 100));
			dos.writeShort(healthrestore);
			dos.writeShort(manarestore);
			dos.writeShort(skillPoint);
			dos.writeShort(propertyPoint);
			dos.writeInt(exp);
			dos.writeInt((int) PlayerUtil.getUpLevelExp(level, level + 1));
			dos.writeInt(money);
			dos.writeShort(map.id);
			if (getVMap() == null) {
				dos.writeInt(-1);
			} else {
				dos.writeInt(getVMap().getInstanceId());
			}
			dos.writeShort(x);
			dos.writeShort(y);
			dos.writeShort(direct);
			removeUnitEffectBuffState();
			dos.writeShort(state);
			dos.writeInt(credit);
			dos.writeUTF(getCreditString());
			dos.writeUTF(getGuildName());
			dos.write(equipments.toClientBytes());
			dos.writeInt(equipments.getHeadScore(level, clazz));
			dos.writeInt(equipments.getBodyScore(level, clazz));
			dos.writeInt(equipments.getWeaponScore(level, clazz));
			dos.write(equipments.getFlashLevel());
			dos.write(chatOptions.toClientBytes());
			dos.write(coolDowns.toClientBytes());
			dos.write(buffs.toClientBytes());
			dos.writeInt(honor);
			dos.writeUTF(titles.getCurrentTitle() == null ? "" : titles
					.getCurrentTitle().name);
			Nation nation = Server.server.getServiceRegistry()
			.getNationService().getNationByFaction(faction);
			Officer targetOfficer = nation.getOfficerByPlayerId(id);
			if(targetOfficer == null){
				dos.writeShort(-1);
			} else{
			    dos.writeShort(targetOfficer.level);
			}
			dos.write(pool.getInt(PROPERTY_LOCK_EXP, EXP_UNLOCK));
			MonthlyPayService monthPayService = Server.server.getServiceRegistry().getMonthlyPayService();
			AccountProperty ap = Server.server.getServiceRegistry().getVipPrivilegeService().getAccountProperty(this.accountId);
			vipLevel = ap.pool.getInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL,0);
			if(monthPayService.monthlyBuys!=null && monthPayService.monthlyBuys.size()>0){
				dos.writeInt(monthPayService.monthlyBuys.size());
				for(Integer key : monthPayService.monthlyBuys.keySet()){
					String pool = monthPayService.getPoolByType(key);
					dos.writeInt(key);
					if(pool != null){
//						dos.write(this.pool.getLong(pool, 0l)==0l?0:1);
						long value = this.pool.getLong(pool, 0l);
					    if(key == MonthlyPayService.MONTHPAY_TYPE_TELEPORT){
					    	if((value == 0 && ActivityItemEffect.hasTeleportEffect(this))||vipLevel>=1){
					    		value = 1;
					    	}
					    }
						dos.write(value==0?0:1);
					} else {
						dos.write(0);
					}
				}
			}else {
				dos.writeInt(0);
			}
			dos.writeInt(getSalary());
			dos.write(cards.toClientBytes());
			dos.write(this.pool.getInt(Player.PROPERTY_FRESH_ENTERMAP, 0));
			dos.writeInt(this.pool.getInt(AttendantFixService.PROPERTY_ATTENDANTEXP,0));
			ChargeActivityService chargeService = Server.server.getServiceRegistry().getChargeActivityService();
			dos.write(chargeService.getAccumulActLast(this));
			dos.write(vipLevel);
			dos.write(chargeService.getActivityState(this));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	
	public byte[] toClientBytesAdmin() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(id);
			dos.writeUTF(name);
			dos.write(sex);
			dos.write(level);
			dos.write(clazz);
			dos.write(faction);
			dos.writeShort(maxhp);
			dos.writeShort(maxmp);
			dos.writeShort(hp);
			dos.writeShort(mp);
			dos.writeShort(strength);
			dos.writeShort(agility);
			dos.writeShort(stamina);
			dos.writeShort(intellect);
			dos.writeShort(Math.round(attackpowerup));
			dos.writeShort(Math.round(attackpowerdown));
			dos.writeShort(Math.round(spellpower));
			dos.writeShort(Math.round(spellheal));
			dos.writeShort(Math.round(defense));
			dos.writeShort(Math.round(spelldefense));
			dos.writeShort(Math.round(critical * 100));
			dos.writeShort(Math.round(spellcritical * 100));
			dos.writeShort(Math.round(hit * 100));
			dos.writeShort(Math.round(spellhit * 100));
			dos.writeShort(Math.round(dodge * 100));
			dos.writeShort(Math.round(spelldodge * 100));
			dos.writeShort(Math.round(anticrit * 100));
			dos.writeShort(Math.round(defensePercent * 100));
			dos.writeShort(healthrestore);
			dos.writeShort(manarestore);
			dos.writeShort(skillPoint);
			dos.writeShort(propertyPoint);
			dos.writeInt(exp);
			dos.writeInt((int) PlayerUtil.getUpLevelExp(level, level + 1));
			dos.writeInt(money);
			dos.writeShort(map.id);
			if (getVMap() == null) {
				dos.writeInt(-1);
			} else {
				dos.writeInt(getVMap().getInstanceId());
			}
			dos.writeShort(x);
			dos.writeShort(y);
			dos.writeShort(direct);
			removeUnitEffectBuffState();
			dos.writeShort(state);
			dos.writeInt(credit);
			dos.writeUTF(getCreditString());
			dos.writeUTF(getGuildName());
			dos.write(equipments.toClientBytes());
			dos.writeInt(equipments.getHeadScore(level, clazz));
			dos.writeInt(equipments.getBodyScore(level, clazz));
			dos.writeInt(equipments.getWeaponScore(level, clazz));
			dos.write(equipments.getFlashLevel());
			dos.write(chatOptions.toClientBytes());
			dos.write(coolDowns.toClientBytes());
			dos.write(buffs.toClientBytes());
			dos.writeInt(honor);
			dos.writeUTF(titles.getCurrentTitle() == null ? "" : titles
					.getCurrentTitle().name);
			Nation nation = Server.server.getServiceRegistry()
			.getNationService().getNationByFaction(faction);
			Officer targetOfficer = nation.getOfficerByPlayerId(id);
			if(targetOfficer == null){
				dos.writeShort(-1);
			} else{
			    dos.writeShort(targetOfficer.level);
			}
			dos.write(pool.getInt(PROPERTY_LOCK_EXP, EXP_UNLOCK));
			MonthlyPayService monthPayService = Server.server.getServiceRegistry().getMonthlyPayService();
			AccountProperty ap = Server.server.getServiceRegistry().getVipPrivilegeService().getAccountProperty(this.accountId);
			vipLevel = ap.pool.getInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL,0);
			if(monthPayService.monthlyBuys!=null && monthPayService.monthlyBuys.size()>0){
				dos.writeInt(monthPayService.monthlyBuys.size());
				for(Integer key : monthPayService.monthlyBuys.keySet()){
					String pool = monthPayService.getPoolByType(key);
					dos.writeInt(key);
					if(pool != null){
//						dos.write(this.pool.getLong(pool, 0l)==0l?0:1);
						long value = this.pool.getLong(pool, 0l);
					    if(key == MonthlyPayService.MONTHPAY_TYPE_TELEPORT){
					    	if((value == 0 && ActivityItemEffect.hasTeleportEffect(this))||vipLevel>=1){
					    		value = 1;
					    	}
					    }
						dos.write(value==0?0:1);
					} else {
						dos.write(0);
					}
				}
			}else {
				dos.writeInt(0);
			}
			dos.writeInt(getSalary());
			dos.write(cards.toClientBytes());
			dos.write(this.pool.getInt(Player.PROPERTY_FRESH_ENTERMAP, 0));
			dos.writeInt(this.pool.getInt(AttendantFixService.PROPERTY_ATTENDANTEXP,0));
			ChargeActivityService chargeService = Server.server.getServiceRegistry().getChargeActivityService();
			dos.write(chargeService.getAccumulActLast(this));
			dos.write(vipLevel);
			int nextLevelValue = VipPrivilegeService.getVIPValue(vipLevel+1);
			dos.writeUTF(String.valueOf(chargeValue)+"/"+String.valueOf(nextLevelValue));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}

	public void setSkillPoint(int skillPoint, boolean notify) {
		if (this.skillPoint != skillPoint) {
			addIntPropertyChangedItem(ChangedItem.SKILLPOINT, this.skillPoint,
					skillPoint, notify);
			this.skillPoint = skillPoint;
		}
	}

	public void setPropertyPoint(int propertyPoint, boolean notify) {
		if (this.propertyPoint != propertyPoint) {
			addIntPropertyChangedItem(ChangedItem.PROPERTYPOINT,
					this.propertyPoint, propertyPoint, notify);
			this.propertyPoint = propertyPoint;
		}
	}

	public void addGainComplete(Gain gain, PlayerTransaction tx, boolean notify)
			throws NoEnoughSpaceException {
		bag.addGainComplete(gain, tx, notify);
		if (gain.getMoney() > 0){
			addMoney(gain.getMoney(), tx, notify);
		}
		//军团任务大师
		TongService ts = Server.server.getServiceRegistry().getTongService();
		Tong tong = ts.getPlayerTong(id,false);
		int exp = gain.getExp();
		int credit = gain.getCredit();
		int honor = gain.getHonor();
		if(tong!=null&&tong.ismaintain == TongService.MAINTAIN&&tong.skills!=null&&tong.skills.get(1)!=null){
			exp = (((TongSkill1)tong.skills.get(1)).getRatio() + 100) * exp / 100;
			credit = (((TongSkill1)tong.skills.get(1)).getRatio() + 100) * credit / 100;
			honor = (((TongSkill1)tong.skills.get(1)).getRatio() + 100) * honor / 100;
		}
		// levellimit
		// levellimit
		if (exp > 0 && level < MAX_LEVEL) {
			addExp(exp, tx, notify);
			// setExp(exp + gain.getExp(), notify);
		}
		if (gain.getCredit() > 0) {
			addCredit(gain.getCredit(), tx, notify);
			// setCredit(credit + gain.getCredit(), notify);
		}
		if (gain.getHonor() > 0) {
			addHonor(gain.getHonor(), tx, notify);
		}
		if(gain.getSalary() > 0){
			addSalary(gain.getSalary(), tx, notify);
		}
	}

	public void addGain(Gain gain, PlayerTransaction tx, boolean notify) {
		bag.addGain(gain, tx, notify);
		for(GainItem item :gain.getGainItems()){
			if(item.item.template.id == XUANWUSHI_ITEM){
				addNumOfXuanwushi(item.count,1);
			}
		}
		if (gain.getMoney() > 0) {
			addMoney(gain.getMoney(), tx, notify);
		}
		// levellimit
		if (gain.getExp() > 0 && level < MAX_LEVEL) {
			int gainExp = (int) (gain.getExp() * getExpRatio());
			addExp(gainExp, tx, notify);
			// setExp(exp + gain.getExp(), notify);
		}
//		if (horse != null&&horse.level<level) {
//			int gainExp = (int) (gain.getExp() * 0.12f * getHorseExpRatio());
//			if (gainExp > 0&&horse.level<MAX_LEVEL)
//				horse.setExp(horse.exp + gainExp, this, tx.getCause());
//		}
		if (gain.getCredit() > 0) {
			int addCredit = (int) (gain.getCredit() * getRewardRation());
			addCredit(addCredit, tx, notify);
			// setCredit(credit + gain.getCredit(), notify);
		}
		if (gain.getHonor() > 0) {
			addHonor(gain.getHonor(), tx, notify);
		}
		if(gain.getSalary() > 0){
			addSalary(gain.getSalary(), tx, notify);
		}
	}
	
	public float getHorseExpRatio(){
		Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(faction);
		NationSkill3 skill = (NationSkill3)nation.skills.get(3);
		if(skill!=null)
			return horseExpRatio + skill.getExpRatio();
		return horseExpRatio;
	}
	
	public float getExpRatio(){
		Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(faction);
		NationSkill6 skill = (NationSkill6)nation.skills.get(6);
		if(skill!=null)
			return 1 + skill.getExpRatio();
		return 1;
	}
	
	public float getRewardRation(){
		return rewardRation;
	}

	public void addExp(int value, PlayerTransaction tx, boolean notify) {
		if (value > 0) {
			if(level>=MAX_LEVEL)
				return;
//			log.info("[ADDEXP]" + LogUtil.getPlayerLogString(this) + "COUNT["
//					+ value + "]CURRENTEXP[" + exp + "]TRY");
			int expLock = pool.getInt(PROPERTY_LOCK_EXP, EXP_UNLOCK);  //如果玩家锁住经验，经验不增长
			if(expLock == EXP_UNLOCK){
			    expTx.add(value, tx, notify);
			    pool.setInt(AlchemyService.PLAYEREXP_TODAYADD, pool.getInt(AlchemyService.PLAYEREXP_TODAYADD)+value);//当天增加的经验
			}
		}
	}
	
	public void decExp(int value, PlayerTransaction tx, boolean notify) {
		if (value > 0) {
			int expLock = pool.getInt(PROPERTY_LOCK_EXP, EXP_UNLOCK);  //如果玩家锁住经验，经验不增长
			if(expLock == EXP_UNLOCK){
			    try {
					expTx.dec(value, tx, notify);
				} catch (NoEnoughValueException e) {
				}
			}
		}
	}

	public void addCredit(int value, PlayerTransaction tx, boolean notify) {
		if (value > 0) {
//			log.info("[ADDCREDIT]" + LogUtil.getPlayerLogString(this)
//					+ "COUNT[" + value + "]CURRENTCREDIT[" + credit + "]TRY");
			creditTx.add(value, tx, notify);
		}
	}

	public void decCredit(int value, PlayerTransaction tx, boolean notify)
			throws NoEnoughValueException {
		if (value > 0) {
//			log.info("[DECCREDIT]" + LogUtil.getPlayerLogString(this)
//					+ "COUNT[" + value + "]CURRENTCREDIT[" + credit + "]TRY");
			creditTx.dec(value, tx, notify);
		}
	}

	public void addHonor(int value, PlayerTransaction tx, boolean notify) {
		if (value > 0) {
//			log.info("[ADDHONOR]" + LogUtil.getPlayerLogString(this) + "COUNT["
//					+ value + "]CURRENTHONOR[" + honor + "]TRY");
			honorTx.add(value, tx, notify);
		}
	}

	public void decHonor(int value, PlayerTransaction tx, boolean notify)
			throws NoEnoughValueException {
		if (value > 0) {
//			log.info("[REMOVEHONOR]" + LogUtil.getPlayerLogString(this)
//					+ "COUNT[" + value + "]CURRENTHONOR[" + honor + "]TRY");
			honorTx.dec(value, tx, notify);
		}
	}

	public void addMoney(int value, PlayerTransaction tx, boolean notify) {
		int oldMoney = money;
		if (value > 0) {
//			log.info("[GETMONEY]" + LogUtil.getPlayerLogString(this) + "COUNT["
//					+ value + "]BALANCE[" + money + "]TRY");
			moneyTx.add(value, tx, notify);
			StatService service = Server.server.getServiceRegistry()
			.getStatService();
	        PvpInfo pvpInfo = service.getPvpInfo(this.id, this.faction);
	        if ((oldMoney + value >= 10000 && oldMoney < 10000 && pvpInfo.pool
			        .getLong(StatService.PROPERTY_MONEY_WAN, 0) != 1)
			        || (oldMoney < 1000000 && (oldMoney + value) >= 1000000 && pvpInfo.pool
					.getLong(StatService.PROPERTY_MONEY_MILLIONARE, 0) != 1)) {
		         fireEvent(money, value);
	        }
		}
	}
	
	protected void fireEvent(int oldMoney, int newMoney){
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent
				.EVENT_PLAYER_MONEY_UP,this,oldMoney,newMoney));
	}

	public void addActivePower(int value) {
		if (value > 0) {
			setActivePower(Math.min(this.activePower + value, 100));
		}
	}
	
	public void decMoney(int value, PlayerTransaction tx, boolean notify)
			throws NoEnoughValueException {
		if (value > 0) {
//			log.info("[REMOVEMONEY]" + LogUtil.getPlayerLogString(this)
//					+ "COUNT[" + value + "]BALANCE[" + money + "]TRY");
			moneyTx.dec(value, tx, notify);
		}
	}
	
	public void decSalary(int value, PlayerTransaction tx, boolean notify)
	throws NoEnoughValueException {
		if (value > 0) {
			salaryTx.dec(value, tx, notify);
		}
      }
	
	public int getSalary(){
		return pool.getInt(SalaryService.PROPERTY_SALARY, 0);
	}
	
	public void addSalary(int value, PlayerTransaction tx, boolean notify) {
		if (value > 0) {
			salaryTx.add(value, tx, notify);
		}
    }

	public void decActivePower(int value) throws NoEnoughValueException {
		if (value > 0) {
			if(value <= this.activePower){
				setActivePower(this.activePower - value);
			}else{
				throw new NoEnoughValueException();
			}
		}
	}
	
	public void setFlag(Flag flag) {
		if (this.flag != null) {
			this.flag.unbind(this);
		}
		this.flag = flag;
		if (this.flag == null) {
			state &= (~GameObject.STATE_FLAG);
		} else {
			flag.bind(this);
			state |= GameObject.STATE_FLAG;
		}
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}

	public void pvp(int time) {
		super.pvp();
		if (time == 0)
			this.pvpTime = 0;
		else
			this.pvpTime = Time.currTime + time;
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}

	public void unPvp() {
		super.unPvp();
		this.pvpTime = 0;
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}

	public void pvpFaction() {
		super.pvpFaction();
//		if (time == 0)
//			this.pvpFactionTime = 0;
//		else
//			this.pvpFactionTime = Time.currTime + time;
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}

	public void unPvpFaction() {
		super.unPvpFaction();
//		this.pvpFactionTime = 0;
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}

	public void dumb() {
		super.dumb();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
		breakAttack();
	}

	public void unDumb() {
		super.unDumb();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}

	public void fear() {
		super.fear();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
		breakAttack();
	}

	public void unFear() {
		super.unFear();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}
	
	public void setKing(){
		super.setKing();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE,state,false,true);
	}
	
	public void unKing(){
		super.unKing();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE,state,false,true);
	}

	public void paralyze() {
		super.paralyze();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
		breakAttack();
	}

	public void unParalyze() {
		super.unParalyze();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}

	public void stay() {
		super.stay();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}

	public void unStay() {
		super.unStay();
		moveType |= MOVE_POINT_STATE;
		addIntPropertyChangedItem(ChangedItem.STATE, state, false, true);
	}

	public void setExp(long exp, boolean notify, String cause) {
		if (this.exp != exp) {
			if(level>=MAX_LEVEL)
				return;
			LogUtil.logGetExp(this, this.exp, exp, cause);
			if(exp<0)
				exp &= 0xFFFFFFFFl;
			int upLevel = PlayerUtil.getUpLevel(level, exp);
			addIntPropertyChangedItem(ChangedItem.GAINEXP, (int)(exp - this.exp),
					notify, false);
			if (upLevel > 0) {
				int oldLevel = level;
				long oldExp = exp;
				int newLevel = level + upLevel;
				exp -= PlayerUtil.getUpLevelExp(level, newLevel);
				this.exp = (int)exp;
				addIntPropertyChangedItem(ChangedItem.EXP, this.exp, false,
						true);
				int skillPoint = PlayerUtil.getGrowSkillPoint(this.level,
						newLevel);
				setSkillPoint(this.skillPoint + skillPoint, notify);
				setLevel(newLevel, notify);
//				if (oldLevel <= MAX_PVE_LEVEL && newLevel > MAX_PVE_LEVEL) { // 设置pvp状态
//					if (pvpFactionTime != 0 || (!isPvpFaction())) {
//						pvpFaction(0);
//					}
//				}
				setPropertyPoint(propertyPoint + upLevel * 2, notify);
				long upExp = PlayerUtil
						.getUpLevelExp(this.level, this.level + 1);
				addIntPropertyChangedItem(ChangedItem.UPLEVELEXP, (int)upExp, false,
						true);
				int newBagSize = 27 + level / 5;
				if (newBagSize > bag.size) {
					bag.extend(newBagSize,false);
				}
				if(oldLevel<10 && newLevel>=10){
					PlayerTransaction tx = newTransaction("UPLEVEL");
					try {
						bag.addGameItemComplete(ObjectAccessor.createGameItem(4418), 1, tx, true);
						tx.commit();
					} catch (NoEnoughSpaceException e) {
						tx.rollback();
					}
				}
				refreshProperties(true);
				serivce.notifyPlayerUpLevel(oldLevel, this);
				pool.setLong(AlchemyService.PLAYEREXP_TODAYADD, this.exp);
				LogUtil.logLevelUp(this, oldLevel, oldExp, this.level, this.exp);
			} else {
				this.exp = (int)exp;
				addIntPropertyChangedItem(ChangedItem.EXP, this.exp, false,
						true);
			}
		}
	}
	
	public void setSalary(int salary, boolean notify, String cause){
		if (pool.getInt(SalaryService.PROPERTY_SALARY,0) != salary) {
			int oldValue = pool.getInt(SalaryService.PROPERTY_SALARY,0);
			pool.setInt(SalaryService.PROPERTY_SALARY, salary);
			addIntPropertyChangedItem(ChangedItem.SALARY, salary, false,true);
			if(oldValue<salary){
			   LogUtil.logAddSalary(this, oldValue, salary, cause);
			} else {
			   LogUtil.logRemoveSalary(this, oldValue, salary, cause);
			}
		}
	}

	public void abandonQuest(int questId, int serial) {
		GameQuest quest = ASMQuestUtil.getGameQuest(questId);
//		if(quest != null && quest.getCycleInfo() != null){
//			ErrorHandler.sendErrorMessage(session, serial,
//					OpCode.QUEST_ABANDON_CLIENT, "不能放弃环任务");
//			return;
//		}
		if (asmVm.abandonQuest(questId) == 0) {
			Packet pt = new Packet(OpCode.QUEST_ABANDON_SERVER);
			pt.putInt(serial);
			pt.putInt(questId);
			send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.QUEST_ABANDON_CLIENT, peony.Messages.STRING_01595);
		}
	}

	public void initBuffs() {
		addSkillBuffs();
		addTitleBuffs();
		addHorseBuffs();
		addPlayerSuiteEquipmentBuffs();
		addHorseSuiteEquipmentBuffs();
		refreshStarState();
		refreshHorseStarState();
		refreshStar7Buff();
		addCardBuff();
		
		if(isKing()==1 && buffs.getBuffByID(216)==null) {
			CandidateService candidateService = Server.server.getServiceRegistry().getCandidateService();
			buffs.addBuff(BuffUtil.createSuiteBuff(216, 1));
			if(ObjectAccessor.getSkill(Skills.getSkillId(candidateService.getKingSkillGroupId(this.clazz), 1))!=null)
				this.addSkill(ObjectAccessor.getSkill(Skills.getSkillId(candidateService.getKingSkillGroupId(this.clazz), 1)));
		}
		buffs.addBuff(Server.server.getServiceRegistry().getNationService().getNationByFaction(faction).buff);
	}
	public void addCardBuff(){//处理卡片技能buff
		CardService service = Server.server.getServiceRegistry().getCardService();
		for(CardInfo info : cards.horseEquipCards){
			if(info!=null){
				int cardId = info.cardId;
				int cardLevel = info.level;
				Card card = service.getCardByCardId(cardId);
				if(card!=null){
					int buff2Id=card.buff2Id;
					if(buff2Id!=-1&&buffs.getBuffByID(buff2Id)==null){
						Buff skillBuff=BuffUtil.createBuff(buff2Id, cardLevel, this, this, 0);
						buffs.addBuff(skillBuff);
					}
				}
			}
		
		}		
		for(CardInfo info : cards.equipCards){
			if(info!=null){
				int cardId = info.cardId;
				int cardLevel = info.level;
				Card card = service.getCardByCardId(cardId);
				if(card!=null){
//					int cardPropertyType = card.prorertyType;
//					int baseValue = card.propertyBaseValue;
//					int upLevelValue = card.propertyUpLevelValue;
//					int quality = ObjectAccessor.createGameItem(card.itemId).template.quality;
					int buff2Id=card.buff2Id;
					if(buff2Id!=-1&&buffs.getBuffByID(buff2Id)==null){
						Buff skillBuff=BuffUtil.createBuff(buff2Id, cardLevel, this, this, 0);
						buffs.addBuff(skillBuff);
					}
				}
			}
		}
	}
	
	public void initPlayerBooks(){
		if(books==null){
			books = new Books(this);
		}
		   BookUtil.createInitBook(this);
	}

	public void loadInit(boolean refreshBuffs) {
		pvpKilledTimes = 0;
		pvp2pveMapId =  0;
		lastPosition = null;
		loadTimes = 0;
		asmVm.clear();
		moveType = 0;
		if (level > MAX_PVE_LEVEL) {
			setWarState(Player.PVPSTATE);
		}else{
			setWarState(Player.PVESTATE);
		}
		logouted = false;
		if(refreshBuffs){
			long l = getLastLogoutElapseTime();
			int value = 0;
			if(l>Integer.MAX_VALUE){
				value = Integer.MAX_VALUE;
			}else{
				value = (int)l;
			}
			buffs.update2(value);
		}
		refreshProperties(false);
		changed.clean();
	}
	
	public void refreshTitles(){
		Title currentTitle = titles.getCurrentTitle();
		Title currentEquipTitle = titles.getCurrentEquipTitle();
		titles.removeCurrentTitle(false);
		titles.removeCurrentTitle(true);
		if(currentTitle!=null)
			titles.changeShowTitle(currentTitle.id);
		if(currentEquipTitle!=null)
			titles.changeEquipTitle(currentEquipTitle.id);
		if(currentTitle==null && currentEquipTitle==null)
			return;
		refreshProperties(false);
	}
	
	public void refreshAllAttendants(){
		attendantBag.addHpAndMp();
	}

	@Override
	public GameObjectRef ref() {
		if (instanceId == 0||type == 0) {
			instanceId = id;
			type = GameObject.TYPE_PLAYER;
//			log.info("[INSTANCEIDERROR]" + LogUtil.getPlayerLogString(this));
//			Thread.dumpStack();
		}
		if (lastRef == null) {
			lastRef = new GameObjectRef(type, id, instanceId);
		}
		if(battleType==TYPE_ASYNC_PLAYER)
			lastRef = new GameObjectRef(type, id, instanceId, map.map.asyncbattleInstanceId, battleType);
		return lastRef;
	}

	@Override
	public Packet getInfoPacket() {
		Packet pt = new Packet(OpCode.UNIT_INFO_SERVER);
		pt.putInt(instanceId);
		pt.putString(getGuildName());
		pt.putString(getCreditString());
		pt.putString(titles.getCurrentTitleString());
		return pt;
	}

	@Override
	public Packet getMovePacket(short moveType) {
		moveType &= ~MOVE_OWNER;
		return super.getMovePacket(moveType);
	}
	
	public int getPlayerExpTodayAdd(){
		return pool.getInt(AlchemyService.PLAYEREXP_TODAYADD);
	}
	
	public void setPlayerExpTodayAdd(int alchemyExp){
		pool.setInt(AlchemyService.PLAYEREXP_TODAYADD, pool.getInt(AlchemyService.PLAYEREXP_TODAYADD)+alchemyExp);
	}
	
	/**检测玩家在线时过天*/
	public void updateAlchemyByPlayerExpCount(){
		int lastDay=pool.getInt(AlchemyService.CURRENTDAY,0);
		if(lastDay!=0&&lastDay!=Time.day){
			pool.setInt(AlchemyService.CURRENTDAY,Time.day);
			pool.setInt(AlchemyService.PLAYEREXP_TODAYADD, 0);
			pool.setInt(AlchemyService.ALCHEMYCOUNT_PLAYEREXP, 4);
			alchemy.alchemyCount=pool.getInt(AlchemyService.ALCHEMYCOUNT_PLAYEREXP, 0);//每天第一次登录默认可以进行4次经验修炼
			if(alchemy!=null&&alchemy.practiceLevel==alchemy.pulseIndex&&alchemy.acupointLevel==10&&alchemy.acupointNum==8){
				pool.setInt(AlchemyService.ALCHEMY_HINT_TODAY, 0);
			}
			pool.setInt(ASMGameVM.SAFE_STATE, 0);
		}
		int mayDay_CurrentDay=pool.getInt(ASMGameVM.MAYDAYACTIVITY_DAY,0);
		if(mayDay_CurrentDay==0||mayDay_CurrentDay!=Time.day){
			pool.setInt(ASMGameVM.MAYDAYACTIVITY,0);
		}
	}
	

	public void logined() {
		setSystemState(SYSTEMSTATE_LOGINED);
		serivce.logined(this);
		
		lastLoginTime = Time.currDate;
		lastLoginTimeMills = Time.currDate.getTime();
//		lastLogoutTime = lastLoginTime;
		antiPlug = new AntiPlug(this);
		antiBot = new AntiBot(this);
		
		int currentDay=pool.getInt(AlchemyService.CURRENTDAY,0);
		if(currentDay==0||currentDay!=Time.day){//修炼开启第一次或每天第一次登录判断
			pool.setInt(AlchemyService.CURRENTDAY,Time.day);
			pool.setInt(AlchemyService.PLAYEREXP_TODAYADD, 0);
			pool.setInt(AlchemyService.ALCHEMYCOUNT_PLAYEREXP, 4);
			alchemy.alchemyCount=pool.getInt(AlchemyService.ALCHEMYCOUNT_PLAYEREXP, 0);//每天第一次登录默认可以进行4次经验修炼
			if(currentDay==0){
				pool.setInt(AlchemyService.ALCHEMY_HINT_TODAY, -1);
			}else 
			if(alchemy!=null&&alchemy.practiceLevel==alchemy.pulseIndex&&alchemy.acupointLevel==10&&alchemy.acupointNum==8){
				pool.setInt(AlchemyService.ALCHEMY_HINT_TODAY, 0);
			}
			pool.setInt(ASMGameVM.SAFE_STATE, 0);
		}
		int mayDay_CurrentDay=pool.getInt(ASMGameVM.MAYDAYACTIVITY_DAY,0);
		if(mayDay_CurrentDay==0||mayDay_CurrentDay!=Time.day){
			pool.setInt(ASMGameVM.MAYDAYACTIVITY,0);
		}
		alchemy.alchemyCount=pool.getInt(AlchemyService.ALCHEMYCOUNT_PLAYEREXP);//每次登录时赋值
		processCardNewByNewMethod();
		returnCardExp();
		// 记录日志
		LogUtil.logLoginOK(this);
		Server.server.getServiceRegistry().getRealtimeStatService().loginCounter++;
		notifyMate();
//		notifyEnemies();
	}
	
	/***
	 * 新的结构为："卡片1ID,卡片数量,2id,2数量,......"
	 * 数量大于0时为已获得
	 */
	public void processCardNewByNewMethod(){
		if(pool.getInt("CARDNEWPROPSAVE", 0)==0){
			pool.setInt("CARDNEWPROPSAVE", 1);
			CardService cs = Server.server.getServiceRegistry().getCardService();
			StringBuffer sb=new StringBuffer();
			for (CardGroup group : cs.cardGroupList) {
				for (Card cd : group.cards) {
					boolean hasMatch = cs.hasMatchOld(this, cd.id);
					if(hasMatch &&cd.itemId!=-1){
						String flag=cd.id+","+pool.getInt(CardService.getPropertyOfPlayerCard(cd.id),0);
						String splitStr="";
						if(sb.toString().length()>0){
							splitStr="|";
						}
						sb.append(splitStr+flag);
						log.info("[CARDNEWPROPSAVE]ACC["+accountId+"]ID["+id+"]CARDID["+cd.id+"]CARDCOUNT["+pool.getInt(CardService.getPropertyOfPlayerCard(cd.id),0)+"]");
					}
				}
			}
			pool.setString(CardService.CARD_NEW_PROPERTY, sb.toString());
			pool.deleteByPrefix("PROPERTY_HAVECARD");
		}
		//删除卡片充能过期数量
		if(pool.getInt("DELCARDPROP", 0)==0){
			pool.setInt("DELCARDPROP", 1);
			pool.deleteByPrefix("PROPERTY_CARDCOLLECT_TIME");
			pool.deleteByPrefix("PROPERTY_CARDENERGY");
			pool.deleteByPrefix("PROPERTY_SHOWCARDNAME");
			pool.deleteByPrefix("PROPERTY_HAVECARD_OFSUIT");
			pool.deleteByPrefix("PROPERTY_CARD_QUALITY");
		}
	}
	
	
	/***
	 * 返还0-1级卡片经验
	 */
	public void returnCardExp(){
		if(pool.getInt("NEWCARDLEVELRETURNEXP", 0)==0){
			pool.setInt("NEWCARDLEVELRETURNEXP", 1);
			CardService cs = Server.server.getServiceRegistry().getCardService();
			Map<Integer,Integer> allCardInfo=CardService.getAllCardsInfo(this);
			for (CardGroup group : cs.cardGroupList) {
				for (Card cd : group.cards) {
					boolean hasMatch = false;//cs.hasMatch(this, cd.id);
					if(allCardInfo.get(cd.id)!=null&&allCardInfo.get(cd.id)>0){
						hasMatch=true;
					}
					if(hasMatch && cd.itemId!=-1){
						CardInfo info = cs.getEquipCardInfo(this, cd.id);
						if(info==null){
							info = cards.getUnEquipCardInfo(cd.id);
						}
						if(info!=null&&info.level>=1){
							Card card = cs.getCardByCardId(info.cardId);
							GameItem cardItem = ObjectAccessor.createGameItem(card.itemId);
							int quality = cardItem.template.quality;
							int totalExp=0;
							try{
								totalExp =CardUpGradeCall.getUpGradeExp(quality,0);
								cards.addExp(totalExp);
							}catch(Exception e){};
							log.info("[RETURNCARDEXP]ACC["+accountId+"]ID["+id+"]CARDID["+cd.id+"]CARDLEVEL["+info.level+"]QUALITY["+quality+"]RETURNCARDEXP["+totalExp+"]");
						}
					}
					CardInfo info = cs.getEquipCardInfo(this, cd.id);
					if(hasMatch&&info==null&&!cards.cardInfos.containsKey(cd.id)){
						//升级原0级卡片，并加入到cardInfos中
						CardInfo cardInfo = new CardInfo(cd.id);
						cardInfo.level=1;
						cards.cardInfos.put(cd.id, cardInfo);
						log.info("[UPGRADEZEROCARD]ACC["+accountId+"]ID["+id+"]CARDID["+cd.id+"]CARDLEVEL["+cardInfo.level+"]");
					}
				}
			}
		}
		if(pool.getInt("NEWCARDLEVELRETURNEXP", 0)==1&&pool.getInt("NEWCARDLEVELRETURNEXPFIX", 0)==0){//修复0级卡片问题
			pool.setInt("NEWCARDLEVELRETURNEXPFIX", 1);
			Map<Integer,Integer> allCardInfo=CardService.getAllCardsInfo(this);
			CardService cs = Server.server.getServiceRegistry().getCardService();
			for (CardGroup group : cs.cardGroupList) {
				for (Card cd : group.cards) {
					boolean hasMatch = false;//cs.hasMatch(this, cd.id);
					if(allCardInfo.get(cd.id)!=null&&allCardInfo.get(cd.id)>0){
						hasMatch=true;
					}
					CardInfo info = cs.getEquipCardInfo(this, cd.id);
					if(hasMatch&&info==null&&!cards.cardInfos.containsKey(cd.id)){
						//升级原0级卡片，并加入到cardInfos中
						CardInfo cardInfo = new CardInfo(cd.id);
						cardInfo.level=1;
						cards.cardInfos.put(cd.id, cardInfo);
						log.info("[UPGRADEZEROCARDFIX]ACC["+accountId+"]ID["+id+"]CARDID["+cd.id+"]CARDLEVEL["+cardInfo.level+"]");
					}
				}
			}
		}
	}
	/**通知仇家我上线了*/
//	protected void notifyEnemies(){
//		RelationService service = Server.server.getServiceRegistry().getRelationService();
//		PlayerRelation relation = service.get(id);
//		if(relation!=null && relation.enemies!=null&&relation.enemies.getCount()>0){
//			for(int i=0;i<relation.enemies.getCount();i++){//所有仇人
//				int targetPlayerId=relation.enemies.getPlayerAt(i).id;
//				PlayerRelation targetPlayerRelation = service.get(targetPlayerId);//取每个仇人的关系列表
//				Player targetPlayer=ObjectAccessor.getPlayer(targetPlayerId);//对方在线才发私聊
//				if(targetPlayer!=null&&targetPlayerRelation.enemies.isLockedOfPlayer(id)){//如果在对方的锁定列表中就发私聊给对方提示我上线了
//					Server.server.getServiceRegistry().getChatService().sendPrivateMessage(
//							targetPlayerId, MessageFormat.format("{0}已经上线了，快去手刃了这个与你不共戴天的仇人吧！", name));
//				}
//			}
//		}
//	}
	
	protected void notifyMate(){
		RelationService service = Server.server.getServiceRegistry().getRelationService();
		PlayerRelation relation = service.get(id);
		if(relation!=null && relation.mateId>0){
			Player mate = ObjectAccessor.getPlayer(relation.mateId);
			if(mate!=null){
				if(mate.sex==0){
					Server.server.getServiceRegistry().getChatService().sendPrivateMessage(
							relation.mateId, MessageFormat.format(peony.Messages.STRING_01596, name));
				}else{
					Server.server.getServiceRegistry().getChatService().sendPrivateMessage(
							relation.mateId, MessageFormat.format(peony.Messages.STRING_01597, name));
				}
			}
		}
	}
	
	public void cancelExchange() {
		if (exchange != null) {
			if(exchange.state==Exchange.STATE_INIT){
				exchange.complete(this, false);
				exchange = null;
				return;
			}
			Gain gain = new Gain(this);
			exchange.restoreToGain(id, gain);
			PlayerTransaction tx = newTransaction("EXCC");
			addGain(gain, tx, false);
			tx.commit();
			int otherId = exchange.getOtherId(id);
			Player p = ObjectAccessor.getPlayer(otherId);
			if (p != null) {
				gain = new Gain(p);
				exchange.restoreToGain(p.id, gain);
				tx = p.newTransaction("EXCC");
				p.addGain(gain, tx, false);
				tx.commit();
			}
			exchange.complete(this, false);
			if(p != null)
				p.exchange = null;
			exchange = null;
		}
	}

	public void removeFromWorld() {
		Date date = new Date();
		lastLogoutTime = date;
		lastLogoutTimeMills = date.getTime();
		pool.setInt(PROPERTY_ONLINETIMETODY, onlineTimeToday);
		lastCheckOnlineTime = 0;
		autoAttack.clear();
		if (party != null) {
			party.leave(id);
		}
		cancelExchange();
		clearThreats();
		if (!isAlive()) {
			if(reliveOptions!=null && reliveOptions.getFirstOption()!=null)
				relive(reliveOptions.getFirstOption());
			else{
				relive(maxhp/2,maxmp/2);
				log.info("[REMOVEFROMWORLDERROR]"+LogUtil.getPlayerLogString(this));
			}
		}
		if(canRecordHpMp)
			recordLogOutHpMp();
		canRecordHpMp = true;
		//下马，但不改变PROPERTY_LAST_HORSE_INSTANCEID
		if (horse != null) {
			Horse h = horse;
			horse = null;
			h.unRide(this);
			unRide();
			Packet pt = new Packet(OpCode.HORSE_UNRIDE_SERVER);
			pt.putInt(-1);
			send(pt);
		}
		
		if(failHorseInst >= 0){
			horseBag.removeHorse(failHorseInst);
			failHorseInst = -1;
		}
		
		gather = null;
		removeFromMap();
		ObjectAccessor.removeGameObject(this);
		setSystemState(Player.SYSTEMSTATE_LOAD);
		unMoving();
		// acceptMoving = false;
		rollbackNotCommiteds();
		Server.server.eventManager.fireEvent(new ServiceEvent(
				ServiceEvent.EVENT_PLAYER_LOGOUTED, this));
		buffs.removeUnitEffectBuffState();
		DBService dbService = Server.server.getServiceRegistry().getDbService();
		dbService.schedule(new LogoutCall(dbService, session, this));
		OnlineTimeNotifyMessage msg = new OnlineTimeNotifyMessage(accountId,
				(int) (System.currentTimeMillis() - loginTime));
		Server.server.getServiceRegistry().getAccountService().postMessage(msg);
		if(antiPlug!=null){
			antiPlug.clear();
			antiPlug = null;
		}
		if(antiBot!=null)
			antiBot = null;
		//随从下线
		if(attendant!=null){
			pool.setInt(PROPERTY_LAST_ATTENDANT_INSTANCEID, attendant.getInstanceId());
			attendant.cancelFollow();
		}
		AskForGiftService aService = Server.server.getServiceRegistry().getAskForGiftService();
		aService.removeRequest(this);
		LogUtil.logLogouted(this, System.currentTimeMillis() - loginTime, lastSessionId);
		long time = this.pool.getLong(StatService.PROPERTY_ONLINE_TIME,0l);
		time += System.currentTimeMillis() - loginTime;
		this.pool.setLong(StatService.PROPERTY_ONLINE_TIME, time);
		//原来在线一小时工资
//		long salaryTime = pool.getLong(SalaryService.PROPERTY_SALARY_ONLLINE_TIME, 0l);
//		salaryTime +=System.currentTimeMillis() - pool.getLong(SalaryService.PROPERTY_SALARY_ONLINE_CHECKTIME,loginTime);
//		pool.setLong(SalaryService.PROPERTY_SALARY_ONLLINE_TIME, salaryTime);
		SalaryService salaryService = Server.server.getServiceRegistry().getSalaryService();
		salaryService.saveProperty(this);
		isFindPath = 0;
		Server.server.getServiceRegistry().getRealtimeStatService().logoutCounter++;
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_PLAYER_UNLOADED, this));
		this.notifyReparEquipment = false;
		this.notifyReparEquipment_1 = false;
		this.notifyReparHorseEquipment = false;
		this.notifyReparHorseEquipment_1 = false;
		if(!Server.isStepServer)
			this.isInStep = false;
		AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
		service.clearPlayerState(this);
	}
	
	/**
	 * 返回从上次退出游戏到现在的时间（以毫秒计算）
	 * @return
	 */
	public long getLastLogoutElapseTime(){
		 long ret = lastLogoutTime==null?0L:(System.currentTimeMillis() - lastLogoutTime.getTime());
		 if(ret<0) ret = 0;
		 return ret;
	}

	public void logout() {
		LogUtil.logDisconnected(this);
		session = null;
		this.loadTimes = 0;
		if (threats.count == 0) {
			removeFromWorld();
		} else {
			Date date = new Date();
			lastLogoutTime = date;
			lastLogoutTimeMills = date.getTime();
			setSystemState(Player.SYSTEMSTATE_DISCONNECTED);
		}
		buffs.removeUnitEffectBuffState();
		Server.server.getServiceRegistry().getDbService().schedule(new AccountDepotSaveCall(session, accountId));
	}

	public void attack(CombatContext cc) {
		if (cc.isDamage() && cc.hited()) {
			equipments.decWeaponDuration(1, 20);
		}
	}

	public void attacked(CombatContext cc) {
		if (cc.hited()) {
			if(cc.damageType == CombatContext.DAMAGE_PHYSICAL)
				equipments.decAllArmorDuration(1, 20);
			if(cc.damageType == CombatContext.DAMAGE_PHYSICAL||cc.damageType == CombatContext.DAMAGE_MAGIC)
				cancelGather(2);
		}
		if (pkInfo != null && pkInfo.state == PkInfo.STATE_STARTED) {
			if (this != cc.source && !pkInfo.in(cc.source.ref())) {
				Server.server.eventManager.fireEvent(new ServiceEvent(
						ServiceEvent.EVENT_PLAYER_PK_ATTACKED, this));
			}

		}
	}

	/**
	 * 把一个和本对象有关的信息包广播给周围玩家。
	 * @param pt 信息包
	 * @param p 生成此信息包的源玩家，如果此信息不是来自玩家，此参数传null
	 * @param target 此信息包作用的目标玩家，如果此目标不是玩家，此参数传null
	 * @param self 是否发送给玩家自己
	 * @param ingoreParty 是否不发送给队友（队友广播通过特殊接口发送）
	 * @param isAttack 是否是攻击包
	 */
	@Override
	public synchronized void broadcast(Packet pt, Player p, Player target, boolean self,
			boolean ingoreParty, boolean isAttack) {
		if (mapCell != null) {
			if (p == null)
				p = this;
			
			// 如果广播时忽略队友，则在这里强行向所有队友广播此消息
			if (ingoreParty && party != null) {
				party.broadcast(pt, p);
			}
			
			// 向CELL及相邻CELL其他玩家广播
			mapCell.broadcast(p, target, pt, self, ingoreParty, isAttack);
		}
	}

	public void unMoving() {
		breakAllActions();
		acceptMoving = false;
		lastPosition = null;
		if(battleType==0)
			nextDistance = 0;
	}

	@Override
	public void goMap(int mapId, int x, int y) throws VMapException {
		if (map.map.getId() != mapId) {
			ForceGoMapCall call = new ForceGoMapCall(this, mapId, x, y);
			Server.server.getWorld().schedule(call);
		} else {
			unMoving();
			// acceptMoving = false;
			move(x, y);
			if(getVMap().instance != null){
				getVMap().instance.loadingFinished(this);
			}
			Packet pt = new Packet(OpCode.FORCE_GOMAP_SERVER);
			pt.putInt(map.map.getId());
			pt.putInt(map.map.getInstanceId());
			pt.putInt(x);
			pt.putInt(y);
			pt.put(map.map.allowFollow() ? 1 : 0);
			send(pt);
		}
	}

	/**
	 * 脱离卡死
	 */
	public void outPrison() {
		if (map.map != null) {
			map.map.manager.outPrison(this);
//		    int[] pos = map.map.mapDef.mapInfo.getPathFinder().tryOutPrison(x, y);
//		    if (pos == null) {
//		        // 已彻底卡死，回复活点
//				int[] relivePoint = map.map.getRelivePoint(faction);
//				try{
//				int oldMapId = map.map.getId();
//				int oldX = x;
//				int oldY = y;
//				goMap(relivePoint[0], relivePoint[1], relivePoint[2]);
//					Server.server
//							.getEventManager()
//							.fireEvent(
//									new ServiceEvent(
//											ServiceEvent.EVENT_PLAYER_OUTPRISON_RELIVEPOINT,
//											this,oldMapId,oldX,oldY));
//				} catch (VMapException e) {
//					//不应该被执行到
//					log.error(e,e);
//				}
//			} else {
//			    try {
//					goMap(map.map.getId(), pos[0], pos[1]);
//				} catch (VMapException e) {
//					//不应该被执行到
//					log.error(e,e);
//				}
//			}
		}
	}

	public void exchangeGrid(int sourceId, int targetId, int serial) {
		boolean ret = bag.exchange(sourceId, targetId, true);
		if (ret) {
			Packet pt = new Packet(OpCode.GRID_EXCHANGE_SERVER);
			pt.putInt(serial);
			send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial, serial, peony.Messages.STRING_01598);
		}
	}

	public void exchangeRefuse(int serial, String msg) {
		Packet pt = new Packet(OpCode.EXCHANGE_INVIT_REFUSE_SERVER);
		pt.putInt(serial);
		pt.putString(msg);
		send(pt);
	}

	public void removeTitle(int serial, int titleId) {
		if (titleId > 0) {
			if(titles.currentTitle!=null && titles.currentTitle.id  == titleId){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TITLE_REMOVE_CLIENT, "正在展示的称号不能删除");
				return;
			}
			if(titles.currentEquipTitle!=null && titles.currentEquipTitle.id  == titleId){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TITLE_REMOVE_CLIENT, "正在装备的称号不能删除");
				return;
			}
			Title t = titles.removeTitle(titleId);
			if(t!=null){
				log.info("[TITLEREMOVE]"+LogUtil.getPlayerLogString(this)+"TITLE["+t.id+"]");
			}
			// if(t!=null){
			// super.addIntPropertyChangedItem(ChangedItem.REMOVE_TITLE, t.id,
			// false);
			// }
			Packet pt = new Packet(OpCode.TITLE_REMOVE_SERVER);
			pt.putInt(serial);
			pt.putShort(titleId);
			send(pt);
		}
	}

	/**
	 * 装备或卸下称号
	 * @param serial
	 * @param titleId
	 */
	public void changeTitle(int serial, int titleId) {
		if (titleId > 0) {
			titles.changeEquipTitle(titleId);
			Packet pt = new Packet(OpCode.TITLE_SET_SERVER);
			pt.putInt(serial);
			pt.putShort(titleId);
			send(pt);
			
			// 记录玩家动作
			addAction(Action.EQUIP_TITLE);
		} else {
			titles.removeCurrentTitle(false);
			Packet pt = new Packet(OpCode.TITLE_SET_SERVER);
			pt.putInt(serial);
			pt.putShort(titleId);
			send(pt);
		}
	}
	
	/**
	 * 展示或隐藏称号
	 * @param serial
	 * @param titleId
	 */
	public void changeShowTitle(int serial,int titleId){
		if(titleId>0){
			titles.changeShowTitle(titleId);
			Packet pt = new Packet(OpCode.TITLE_SHOW_SERVER);
			pt.putInt(serial);
			pt.putShort(titleId);
			send(pt);
			// 记录玩家动作
			addAction(Action.EQUIP_TITLE);
		} else {
			titles.removeCurrentTitle(true);
			Packet pt = new Packet(OpCode.TITLE_SHOW_SERVER);
			pt.putInt(serial);
			pt.putShort(titleId);
			send(pt);
		}
		
	}

	public boolean addTitle(Title t) {
		if (titles.hasTitle(t.id)) {
			return false;
		} else {
			titles.addTitle(t);
			log.info("[TITLEADD]"+LogUtil.getPlayerLogString(this)+"TITLE["+t.id+"]");
			AddTitleChangedItem c = new AddTitleChangedItem(t, true);
			changed.addChangedItem(c);
			return true;
		}
	}

	public void forgetSkill(int serial, int skillId) {
		Skill skill = skills.getBookSkill(skillId);
		if (skill == null) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.FORGET_SKILL_CLIENT, peony.Messages.STRING_01599);
			return;
		}
		if (skills.removeBookSkill(skillId) != null) {
			if (skill != null) {
				buffs.removeBuff(skill.getId());
			}
			Packet pt = new Packet(OpCode.FORGET_SKILL_SERVER);
			pt.putInt(serial);
			pt.putInt(skillId);
			send(pt);
		}
	}

	public int getShowOption(){
		if(config == null)
			return 0;
		if(config.length < 21)
			return 0;
		return config[20];
	}
	/**是否允许交易邀请**/
	public boolean isAllowTrade(){
		if(config == null || config.length < 89 || config[88] == 0){
			return true;
		}else{
			return false;
		}
	}
	/**是否允许PK邀请**/
	public boolean isAllowPK(){
		if(config == null || config.length < 93 || config[92] == 0){
			return true;
		}else{
			return false;
		}
	}
	/**是否允许军团邀请**/
	public boolean isAllowGuild(){
		if(config == null || config.length < 97 || config[96] == 0){
			return true;
		}else{
			return false;
		}
	}
	/**是否接收军团聊天**/
	public boolean isAllowGuildChat(){
		if(config == null || config.length < 101 || config[100] == 0){
			return true;
		}else{
			return false;
		}
	}
	/**是否接收国家聊天**/
	public boolean isAllowFactionChat(){
		if(config == null || config.length < 105 || config[104] == 0){
			return true;
		}else{
			return false;
		}
	}
	/**是否接收组队邀请**/
	public boolean isAllowParty(){
		if(config == null || config.length < 109 || config[108] == 0){
			return true;
		}else{
			return false;
		}
	}
	
	
//	public boolean onlyShowPartyAttack() {
//		if (config == null)
//			return false;
//		if (config.length < 5)
//			return false;
//		return config[20] == 1;
//	}
	
	public void setWarState(WarState state){
		if(warState!=null){
			warState.exit(this);
		}
		warState = state;
		warState.enter(this);
	}
	
	public int getTodayInstanceTimes(int instanceId){
		//优化Player的pool里的key值
		int day = pool.getInt(PROPERTY_INSTANCE_DAY_NEW+instanceId, 0);
		if(day==0){
			int oldDay = pool.getInt(PROPERTY_INSTANCE_DAY+instanceId, 0);
			if(oldDay!=0){
				pool.setInt(PROPERTY_INSTANCE_DAY_NEW+instanceId, oldDay);
				pool.remove(PROPERTY_INSTANCE_DAY+instanceId);
				day = oldDay;
				int oldTimes = pool.getInt(PROPERTY_INSTANCE_TIMES+instanceId,0);
				if(oldTimes!=0){
					pool.setInt(PROPERTY_INSTANCE_TIMES_NEW+instanceId, oldTimes);
					pool.remove(PROPERTY_INSTANCE_TIMES+instanceId);
				}
			}
		} 
		if(day==0||day!=Time.day){
			return 0;
		}else{
			return pool.getInt(PROPERTY_INSTANCE_TIMES_NEW+instanceId,0);
		}
	}
	
	public void setTodayInstanceTimes(int instanceId,int times){
		pool.setInt(PROPERTY_INSTANCE_DAY_NEW+instanceId, Time.day);
		pool.setInt(PROPERTY_INSTANCE_TIMES_NEW+instanceId, times);
	}
	
	public static int KEYBOARD_TYPE_NO_KEYS = 0;
	public static int KEYBOARD_TYPE_TRADITIONAL = 1;
	public static int KEYBOARD_TYPE_FULL_KEYS = 2;
	/**
     * 取得此帐号所用机型的键盘类型。
     * @return 0 - 无键盘，1 - 数字键盘，2 - 全键盘
     */
	public int getKeyboardType() {
	    if (session == null) {
	        return 1;
	    }
	    Account acc = (Account)session.getIdentity();
	    if (acc == null) {
	        return 1;
	    } else {
	        return acc.getKeyboardType();
	    }
	}
	
	/**
     * 取得此帐号所用机型的鼠标类型。
     * @return 0 - 无指点设备，1 - 触摸屏，2 - 鼠标
     */
	public int getMouseType() {
        if (session == null) {
            return 1;
        }
        Account acc = (Account)session.getIdentity();
        if (acc == null) {
            return 0;
        } else {
            return acc.getMouseType();
        }
	}
	
	/**
	 * 保存玩家当前位置到指定变量
	 * @param varName
	 */
	public void savePosition(String varName) {
		pool.setString(varName, map.id + "," + x + "," + y);
	}

	/**
	 * 取得玩家的伴侣ID，如果没有返回-1。
	 */
	public int getMateID() {
		RelationService rs = Server.server.getServiceRegistry().getRelationService();
		PlayerRelation pr = rs.get(id);
		return pr.mateId;
	}
	
	public int getTeacherId(){
		RelationService rs = Server.server.getServiceRegistry().getRelationService();
		PlayerRelation pr = rs.get(id);
		if(pr != null){
		   return pr.teacherId;
		}
		return -1;
	}
	
	/**
	 * 取得玩家的盟主，如果没有返回-1。
	 */
	public int getAssociationLeaderID() {
		AssociationService service = Server.server.getServiceRegistry().getAssociationService();
		Association association = service.getAssociationByPlayerId(id);
		if(association==null)
			return -1;
		return association.getLeader().playerId;
	}
	
	/**
	 * 为虚拟机特别制作，所以返回值设置成整形，偷懒偷懒^_^
	 * @return
	 */
	public int isKing(){
		return Server.server.getServiceRegistry().getNationService().isKing(this)?1:0;
	}
	
	public int attendantIsFollowing(){
		return (attendant==null ? 0 : 1);
	}
	
	public int isOfficer(){
		return Server.server.getServiceRegistry().getNationService().getNationByFaction(faction).getOfficerByPlayerId(id)!=null?1:0;
	}
	
	public void addAction(int type){
		actions.put(type,new Action(type));
	}
	
	public boolean hasAction(int type){
		return actions.containsKey(type);

	}
	
	public void clearReport(){
		report.clear();
	}
	
	public void recordLastAction(){
		report.lastActionTime = Time.currTime;
	}
	
	class MoneyIntProperty extends TransactionIntProperty {

		@Override
		public int getValue() {
			return money;
		}

		@Override
		protected void modifyValue(int value, boolean notify, String cause) {
			setMoney(money + value, notify, cause);
		}
	}

	class HonorIntProperty extends TransactionIntProperty {

		@Override
		public int getValue() {
			return honor;
		}

		@Override
		protected void modifyValue(int value, boolean notify, String cause) {
			setHonor(honor + value, notify, cause);
		}
	}

	class CreditIntProperty extends TransactionIntProperty {

		@Override
		public int getValue() {
			return credit;
		}

		@Override
		protected void modifyValue(int value, boolean notify, String cause) {
			setCredit(credit + value, notify, cause);
		}
	}

	class ExpIntProperty extends TransactionIntProperty {

		@Override
		public int getValue() {
			return exp;
		}

		@Override
		protected void modifyValue(int value, boolean notify, String cause) {
			setExp(exp + value, notify, cause);
		}
	}
	
	class SalaryIntProperty extends TransactionIntProperty {

		@Override
		public int getValue() {
			return pool.getInt(SalaryService.PROPERTY_SALARY,0);
		}

		@Override
		protected void modifyValue(int value, boolean notify, String cause) {
			setSalary(pool.getInt(SalaryService.PROPERTY_SALARY,0) + value, notify, cause);
		}
	}

	public int getItemUseGridId() {
		if(this.itemUse != null){
		    return this.itemUse.gridId;
		} else {
			return -1;
		}
	}
	
	public int getItemInstanceId() {
		if(this.itemUse != null){
		    return this.itemUse.instanceId;
		} else {
			return -1;
		}
	}
	
	public int hasReceiveAssoInv(){
		return associationInvite==null ? 0 : 1;
	}
	
	public void injoinAssociation(){
		if(associationInvite!=null && associationInvite.endTime>=System.currentTimeMillis()){
			AssociationService service = Server.server.getServiceRegistry().getAssociationService();
			Association association = service.getAssociationByPlayerId(id);
			if(association!=null && association.getMember(id).state==AssociationMember.STAT_WAIT){
				try {
					service.injoyAssociation(this, association.id, AssociationMember.STAT_WORK);
				} catch (AssociationException e) {
					
				}
			}
		}
	}
	
	public int hasAssociation(){
		AssociationService service = Server.server.getServiceRegistry().getAssociationService();
		Association asso = service.getAssociationByPlayerId(id);
		if(asso != null){
			AssociationMember mem = asso.getMember(id);
			if(mem != null && mem.state == AssociationMember.STAT_WORK){
				return 1;
			}
		}
		return 0;
	}
	
	/** 是否为外挂 */
	public boolean isBot(){
		if(antiPlug!=null && antiPlug.isBot && antiPlugModel==ANTIPLUG_MODEL_NONBENEFIT)
			return true;
		return false;
	}
	
	/**
	 * 增加总贡献度
	 * @param tongId  军团id
	 * @param addNum  增加数量
	 */
	public void addContribute(int addNum){
		contribute += addNum;
	}
	
	/**
	 * 减少总贡献度
	 * @param tongId  军团id
	 * @param addNum  增加数量
	 */
	public void decContribute(int decNum){
		int oldContribute = contribute;
		contribute -= decNum;
		log.info("[DECCONTRUIBUTE]" + LogUtil.getPlayerLogString(this)
		+ "V1[" + oldContribute + "]V2[" + contribute + "]");
	}
	
	/**
	 * 增加每日贡献度
	 * @param tongId  军团id
	 * @param addNum  增加数量
	 */
	public void addContributeDay(int addNum){
		contributeDay += addNum;
	}
	
	/**
	 * 每日贡献度清零
	 * @param tongId  军团id
	 * @param addNum  增加数量
	 */
	public void initContributeDay(int decNum){
		contributeDay = 0;
	}
	
	public void processBook(){
		Book b = this.book;
		if(b!=null){
			BookConfig bc = BookUtil.getBookConfig(b.id);
			if(bc !=null){
				BookChapter bookLevel = BookUtil.getBookChapter(b.chapter, bc);
				int leftTime = 0;
				if(b.onRead == Book.STATE_READ && bookLevel != null){
					long time = b.startReadTime - System.currentTimeMillis();
					if(bc.auto==1){
					    long lastTime = bookLevel.time*60*1000l;
					    time = (lastTime - (System.currentTimeMillis() - b.startReadTime + b.alreadyRead));
					}
					if(time<=0){
						bookLevel = BookUtil.getBookChapter(b.chapter+1, bc);
						b.alreadyRead = 0;
						if(bookLevel != null){
						    leftTime = bookLevel.time;
						}
						Server.server.getServiceRegistry().getChatService().sendPrivateMessage(id, MessageFormat.format("您当前《{0}》书籍可以升级了，赶快打开看看吧！", bc.getTitle()));
						b.onRead = Book.STATE_UNREAD;
						this.book = null;
						Packet pt = new Packet(OpCode.PLAYER_READBOOK_SERVER);
					    pt.putInt(-1);
					    pt.putInt(b.getId());
					    pt.putInt(b.chapter);
					    pt.putInt(leftTime);
					    pt.put(bookLevel==null?3:b.onRead);
					    pt.putString(b.getPropertyName(bc));
					    send(pt);  
					}
				}
			}
		}
		if(bookDay != Time.day){
			bookDay = Time.day;
			pool.setInt(Player.PROPERTY_PAYFORBOOK_DAYTIME,Time.day);
			pool.setInt(Player.PROPERTY_PAYFORBOOK_LASTTIME, 1);
			readBookCount = 0;
			pool.setInt(Player.PROPERTY_READBOOK_COUNT, 0);
		}
		if(level>=Books.LEVELLIMIT && this.send){
//			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(id, "您现在可以阅读书籍来提高自己的属性，赶快打开\"个人自传\"进行阅读吧！");
			if(this.isNewUI()){
				Server.server.getServiceRegistry().getMailService().sendSystemMail(id, "系统", "古书典藏", "您现在可以阅读书籍来提高自己的属性，赶快打开\"个人成就\"进行阅读吧！", 0, null, 0, "BOOK");
			}else{
			    Server.server.getServiceRegistry().getMailService().sendSystemMail(id, "系统", "古书典藏", "您现在可以阅读书籍来提高自己的属性，赶快打开\"个人自传\"进行阅读吧！", 0, null, 0, "BOOK");
			}
			this.send = false;
		}
//		books.processLimitBook(this);
	}
	
	/**
	 * 副本扫荡时间到期提示
	 */
	public void processInstanceSweep(){
		if(sweepList!=null && sweepList.size()>0){
			InstanceSweepService service = Server.server.getServiceRegistry().getInstanceSweepService();
			Iterator<Integer> it = sweepList.keySet().iterator();
			while(it.hasNext()){
				int key = it.next();
				InstanceSweep is = sweepList.get(key);
				InstanceSweep oriIns = service.getSweepInstance(key);
				if(Time.currentTimeMillis(Time.currTime) >= is.getEndTime()){
					service.getReward(this,is.id,oriIns.instanceName);
					int sweepTimes = this.pool.getInt(InstanceSweepService.getPropertyOfDayTimes(is.id), 1);
					sweepTimes++;
					this.pool.setInt(InstanceSweepService.getPropertyOfDayTimes(is.id), sweepTimes);
					LogUtil.logSweepEnd(this, oriIns.instanceName, sweepTimes-1);
					it.remove();
					if(freeSweep.contains(key)){
						int index = freeSweep.indexOf(key);
						freeSweep.remove(index);
					}
					Packet pt = new Packet(OpCode.INSTANCE_SWEEP_SERVER);
					pt.putInt(-1);
					pt.putInt(is.id);
					pt.put(sweepTimes>oriIns.dayTimes ? InstanceSweepService.TYPE_SWEEPED : InstanceSweepService.TYPE_UNSWEEP);
					pt.putInt(sweepTimes-1);
					ShopService shopService = Server.server.getServiceRegistry().getShopService();
					float price = (InstanceSweepService.PAY_OPENSWEEP[sweepTimes] * shopService.getItemPrice(NoItemShopBuy.YIYUANBAO))/36f;
					pt.putString(String.valueOf(price));
					pt.putInt(oriIns.time);
					pt.put(0);
					send(pt);
					//处理扫荡副本工资
					SalaryService salaryService = Server.server.getServiceRegistry().getSalaryService();
					salaryService.processSweepSalary(this);
				}
			}
		}
	}
	
	public int calculateBattleValue(){
//		等级战力=等级*12*50
		int levelVlaue = 0;
		levelVlaue += level * 12 * 50;
//		防具战力=0.35*(900+100*物品等级)*部位比重*7+int（0.35*(900+100*物品等级)*部位比重/200）*50
		int equipValue = 0;
		for(GameItem item : equipments.equs){
			Equipment equipment = null;
			try{equipment = item.template.equipment.equ;}catch(Exception e){}
			if(equipment!=null && equipment.equipmentType==Equipment.EQUI_TYPE_PROTECTOR){
				equipValue += (int)(0.35 * (900 + 100 * item.template.equipment.level) * AttributeCalculator.RATE_PLACE[equipment.place]) * 7 
				+ (int)((0.35f * (900 + 100 * item.template.equipment.level) * AttributeCalculator.RATE_PLACE[equipment.place] / 200) )* 50;
			}
		}
//		武器物攻战力=武器物品等级*6*部位比重*32
		for(GameItem item : equipments.equs){
			Equipment equipment = null;
			try{equipment = item.template.equipment.equ;}catch(Exception e){}
			if(equipment!=null && equipment.equipmentType==Equipment.EQUI_TYPE_WEAPON){
				equipValue += item.template.equipment.level * 6 * AttributeCalculator.RATE_PLACE[equipment.place] * 32;
			}
		}
//		装备所能够附加属性战力=sum（装备物品等级*720*部位比重*（品质系数+调节系数
		int equipmentAddtionValue = 0;
		for(GameItem item : equipments.equs){
			Equipment equipment = null;
			try{equipment = item.template.equipment.equ;}catch(Exception e){}
			if(equipment!=null){
				equipmentAddtionValue += equipment.getValue();
			}
		}
		if(horse!=null){
			for(GameItem item : horse.equs.equs){
				Equipment equipment = null;
				try{equipment = item.template.equipment.equ;}catch(Exception e){}
				if(equipment!=null){
					equipmentAddtionValue += equipment.getValue();
				}
			}
		}
//		星级鉴定提升的属性战力=sum(max{装备物品等级*6%，7}*720*星级*部位比重*（品质系数+调节系数）
		int starValue = 0;
		for(GameItem item : equipments.equs){
			Equipment equipment = null;
			try{equipment = item.template.equipment.equ;}catch(Exception e){}
			if(equipment!=null && item.object instanceof ItemEnhance){
				ItemEnhance enhance = (ItemEnhance)item.object;
				starValue += item.template.equipment.levelPromoteValue(item.template.equipment.level)*720*enhance.getStar()*AttributeCalculator.RATE_PLACE[equipment.place]*(AttributeCalculator.QUALITY_ADDITION[item.template.equipment.equ.quality]+item.template.equipment.equ.extraQuality);
			}
		}
		if(horse!=null){
			for(GameItem item : horse.equs.equs){
				Equipment equipment = null;
				try{equipment = item.template.equipment.equ;}catch(Exception e){}
				if(equipment!=null && item.object instanceof ItemEnhance){
					ItemEnhance enhance = (ItemEnhance)item.object;
					starValue += item.template.equipment.levelPromoteValue(item.template.equipment.level)*720*enhance.getStar()*AttributeCalculator.RATE_PLACE[equipment.place]*(AttributeCalculator.QUALITY_ADDITION[item.template.equipment.equ.quality]+item.template.equipment.equ.extraQuality);
				}
			}
		}
//		资质鉴定额外战力=装备价值*资质鉴定提高比例	
		int natualEnhanceValue = 0;
		for(GameItem item : equipments.equs){
			Equipment equipment = null;
			try{equipment = item.template.equipment.equ;}catch(Exception e){}
			if(equipment!=null && item.object instanceof ItemEnhance){
				ItemEnhance enhance = (ItemEnhance)item.object;
				if(enhance.getNaturals().length>0){
					for(NaturalEnhance h : enhance.getNaturals()){
						natualEnhanceValue += (int) ((int) ((((item.template.equipment.level + enhance.getStar() * 3) * 720 * .1f * (1.0f + AttributeCalculator.QUALITY_ADDITION[item.template.equipment.equ.quality] + item.template.equipment.equ.extraQuality))
								* (h.percent / 100.0f) * .2f)));
					}
				}
			}
		}
		if(horse!=null){
			for(GameItem item : horse.equs.equs){
				Equipment equipment = null;
				try{equipment = item.template.equipment.equ;}catch(Exception e){}
				if(equipment!=null && item.object instanceof ItemEnhance){
					ItemEnhance enhance = (ItemEnhance)item.object;
					if(enhance.getNaturals().length>0){
						for(NaturalEnhance h : enhance.getNaturals()){
							natualEnhanceValue += (int) ((((item.template.equipment.level + enhance.getStar() * 3) * 720 * .1f * (1.0f + AttributeCalculator.QUALITY_ADDITION[item.template.equipment.equ.quality] + item.template.equipment.equ.extraQuality))
									* (h.percent / 100.0f) * .2f));
						}
					}
				}
			}
		}
//		宝石战力=sum（宝石物品等级*36）
		int jewelValue = 0;
		for(GameItem item : equipments.equs){
			Equipment equipment = null;
			try{equipment = item.template.equipment.equ;}catch(Exception e){}
			if(equipment!=null && item.object instanceof ItemEnhance){
				ItemEnhance enhance = (ItemEnhance)item.object;
				int totalHoles = item.template.equipment.initHole + enhance.getAddHole();
				int jewelCount = enhance.getJewelCount();
				for(int i=0;i<totalHoles;i++){
					if(i>=jewelCount)
						break;
					int jewelItemId = enhance.getJewelID(i);
					int hole = enhance.getJewelHole(i);
					ItemTemplate temp = ObjectAccessor.createGameItem(jewelItemId).template;
//					int level = temp.level;
					int count = enhance.jewelUpgrades[2*hole+1];
					jewelValue += temp.level * 36;
					ItemTemplate nextJewelTemplate = null;
			        try {
			        	nextJewelTemplate = Server.server.getServiceRegistry().getJewelService().jewels[temp.jewelAttrType][temp.useLevel];
			            jewelValue += ((nextJewelTemplate.level-temp.level)*count)*36/4;
			        } catch (Exception e1) {}
					
				}
			}
		}
		if(horse!=null){
			for(GameItem item : horse.equs.equs){
				Equipment equipment = null;
				try{equipment = item.template.equipment.equ;}catch(Exception e){}
				if(equipment!=null && item.object instanceof ItemEnhance){
					ItemEnhance enhance = (ItemEnhance)item.object;
					int totalHoles = item.template.equipment.initHole + enhance.getAddHole();
					int jewelCount = enhance.getJewelCount();
					for(int i=0;i<totalHoles;i++){
						if(i>=jewelCount)
							break;
						int jewelItemId = enhance.getJewelID(i);
						int hole = enhance.getJewelHole(i);
						ItemTemplate temp = ObjectAccessor.createGameItem(jewelItemId).template;
//						int level = temp.level;
						int count = enhance.jewelUpgrades[2*hole+1];
						jewelValue += temp.level * 36;
						ItemTemplate nextJewelTemplate = null;
				        try {
				        	nextJewelTemplate = Server.server.getServiceRegistry().getJewelService().jewels[temp.jewelAttrType][temp.useLevel];
				            jewelValue += (nextJewelTemplate.level-temp.level)*count*36/4;
				        } catch (Exception e1) {}
						
					}
				}
			}
		}
//		强化战力= sum（装备属性价值*基础提升比例）+sum（装备星级属性*星级提升比例）+sum（资质鉴定价值*资质鉴定提升属性）+sum（宝石属性价值*宝石提升比例）															
		int equEnhanceValue = 0;
		for(GameItem item : equipments.equs){
			Equipment equipment = null;
			try{equipment = item.template.equipment.equ;}catch(Exception e){}
			if(equipment!=null && item.object instanceof ItemEnhance){
				ItemEnhance enhance = (ItemEnhance)item.object;
				//基础强化
				equEnhanceValue += equipment.getValue() * enhance.equipEnhanceData[0]/1000;
	            //星级强化
				equEnhanceValue += item.template.equipment.levelPromoteValue(item.template.equipment.level)*720*enhance.getStar()*AttributeCalculator.RATE_PLACE[equipment.place]*(AttributeCalculator.QUALITY_ADDITION[item.template.equipment.equ.quality]+item.template.equipment.equ.extraQuality) * enhance.equipEnhanceData[1]/1000;
				//资质强化
				if(enhance.getNaturals().length>0){
					for(NaturalEnhance h : enhance.getNaturals()){
						int natualEnhanceValue0 = (int) ((((item.template.equipment.level + enhance.getStar() * 3) * 720 * .1f * (1.0f + AttributeCalculator.QUALITY_ADDITION[item.template.equipment.equ.quality] + item.template.equipment.equ.extraQuality))
								* (h.percent / 100.0f) * .2f));
						equEnhanceValue += natualEnhanceValue0 *enhance.equipEnhanceData[2]/1000;
						
					}
				}
				//宝石强化
				int totalHoles = item.template.equipment.initHole + enhance.getAddHole();
				int jewelCount = enhance.getJewelCount();
                for(int i=0;i<totalHoles;i++){
					if(i>=jewelCount)
						break;
					int jewelItemId = enhance.getJewelID(i);
					int hole = enhance.getJewelHole(i);
					ItemTemplate temp = ObjectAccessor.createGameItem(jewelItemId).template;
					int level = temp.level;
					int count = enhance.jewelUpgrades[2*hole+1];
					int tmepEnhanceValue = temp.level * 36;
					ItemTemplate nextJewelTemplate = null;
			        try {
			        	nextJewelTemplate = Server.server.getServiceRegistry().getJewelService().jewels[temp.jewelAttrType][temp.useLevel];
			        	tmepEnhanceValue += (nextJewelTemplate.level-temp.level) * count*36/4;
			        } catch (Exception e1) {}
			        equEnhanceValue += tmepEnhanceValue*enhance.equipEnhanceData[3]/1000;
				}
			}
		}
		
		if(horse!=null){
			for(GameItem item : horse.equs.equs){
				Equipment equipment = null;
				try{equipment = item.template.equipment.equ;}catch(Exception e){}
				if(equipment!=null && item.object instanceof ItemEnhance){
					ItemEnhance enhance = (ItemEnhance)item.object;
					//基础强化
					equEnhanceValue += equipment.getValue() * enhance.equipEnhanceData[0]/1000;
		            //星级强化
					equEnhanceValue += item.template.equipment.levelPromoteValue(item.template.equipment.level)*720*enhance.getStar()*AttributeCalculator.RATE_PLACE[equipment.place]*(AttributeCalculator.QUALITY_ADDITION[item.template.equipment.equ.quality]+item.template.equipment.equ.extraQuality) * enhance.equipEnhanceData[1]/1000;
					//资质强化
					if(enhance.getNaturals().length>0){
						for(NaturalEnhance h : enhance.getNaturals()){
							int natualEnhanceValue0 = (int) ((((item.template.equipment.level + enhance.getStar() * 3) * 720 * .1f * (1.0f + AttributeCalculator.QUALITY_ADDITION[item.template.equipment.equ.quality] + item.template.equipment.equ.extraQuality))
									* (h.percent / 100.0f) * .2f));
							equEnhanceValue += natualEnhanceValue0 *enhance.equipEnhanceData[2]/1000;
							
						}
					}
					//宝石强化
					int totalHoles = item.template.equipment.initHole + enhance.getAddHole();
					int jewelCount = enhance.getJewelCount();
					for(int i=0;i<totalHoles;i++){
						if(i>=jewelCount)
							break;
						int jewelItemId = enhance.getJewelID(i);
						int hole = enhance.getJewelHole(i);
						ItemTemplate temp = ObjectAccessor.createGameItem(jewelItemId).template;
						int level = temp.level;
						int count = enhance.jewelUpgrades[2*hole+1];
						int tmepEnhanceValue = level * 36;
						ItemTemplate nextJewelTemplate = null;
				        try {
				        	nextJewelTemplate = Server.server.getServiceRegistry().getJewelService().jewels[temp.jewelAttrType][temp.useLevel];
				        	tmepEnhanceValue += (nextJewelTemplate.level-level)*count*36/4;
				        } catch (Exception e1) {}
				        equEnhanceValue += tmepEnhanceValue*enhance.equipEnhanceData[3]/1000;
					}
				}
			}
		}
		
//		卡片战力=sum（卡片提升属性值*50）	
		int cardValue = 0;
		CardService service = Server.server.getServiceRegistry().getCardService();
		for(CardInfo info : cards.equipCards){
			if(info!=null){
				int cardId = info.cardId;
				int cardLevel = info.level;
				Card card = service.getCardByCardId(cardId);
				if(card!=null){
					int quality = ObjectAccessor.createGameItem(card.itemId).template.quality;
					cardValue += service.getEnhanceValue(cardId, cardLevel, quality) * 50;
				}
			}
		}
		if(horse!=null){
			for(CardInfo info : cards.horseEquipCards){
				if(info!=null){
					int cardId = info.cardId;
					int cardLevel = info.level;
					Card card = service.getCardByCardId(cardId);
					if(card!=null){
						int quality = ObjectAccessor.createGameItem(card.itemId).template.quality;
						cardValue += service.getEnhanceValue(cardId, cardLevel, quality) * 50;
					}
				}
			}
		}
//		读书提供战力=sum（新书等级*50+（老书等级+1）*老书等级/2*50）	
		int bookValue = 0;
		if(books!=null && books.books!=null && books.books.size()>0){
			for(Book book:books.books.values()){
				BookConfig bc = BookUtil.getBookConfig(book.getId());
				if(bc.auto == 1){
					bookValue += (book.getLevel()+1)*book.getLevel()/2*50;
				}else{
					bookValue += book.getLevel()*50;
				}
			}
		}
		
//		坐骑的战力=坐骑初始价值+等级*成长价值+合成价值*合成等级		
		int horseValue = 0;
		if(horse!=null){
			horseValue += (horse.template.initValue + horse.level * horse.template.levelValue + horse.template.mergeValue * horse.fixCount);
		}
//		随从为人提供的战力=（光环提供的物攻*32+光环提供法功*50）	
		int attendantValue = 0;
		if(attendant!=null){
			for(Skill skill : attendant.specialSkills){
				if(skill.getLevel()<=0)
					continue;
				int buffId = ((AbstractSkill)skill).newBuff().getId();
				int attendantLevel = attendant.attLevel;
				int index = StatService.isInArray(attendant_buffs, buffId>>16);
				if(index!=-1){
					int[] values = buff_values[index];
					attendantValue += values[attendantLevel];
				}
			}
		}
//		星辉战力=星辉提升属性*属性价值	
		int starBuffValue = 0;
		if(buffs!=null){
			for(Buff buff : buffs.getBuffs()){
				int index = StatService.isInArray(star_buffs, buff.getId());
				if(index!=-1){
					starBuffValue += starbuff_values[index];
				}
			}
		}
		return levelVlaue + equipValue + equipmentAddtionValue + starValue + natualEnhanceValue + jewelValue + equEnhanceValue
		+ cardValue + bookValue + horseValue + attendantValue + starBuffValue;
	}
	
	/** 记录下线血蓝量 */
	public void recordLogOutHpMp(){
		pool.setInt("PROPERTY_LOGOUTHP", hp);
		pool.setInt("PROPERTY_LOGOUTMP", mp);
	}
	
//	class PvpState implements WarState{
//		
//		public void war(){
//			
//		}
//		
//		public void enter() {
//			pvpFaction(0);
//		}
//
//		public void exit() {
//			
//		}
//
//		public void update() {
//		}
//		
//	}
//	
//	class PvpPveState implements WarState{
//		
//		public int mapId,time;
//		
//		public PvpPveState(int mapId,int time){
//			this.mapId = mapId;
//			this.time = Time.currTime + time;
//		}
//
//		public void war(){
//			setWarState(new PvpState());
//		}
//		
//		public void enter() {
//			unPvpFaction();
//		}
//
//		public void exit() {
//			
//		}
//
//		public void update() {
//			if(Time.currTime>time){
//				setWarState(new PvpState());
//				return;
//			}
//			VMap map = getVMap();
//			if(map.getStageId()!=(mapId>>4)){
//				setWarState(new PvpState());
//				return;
//			}
//		}
//		
//	}
//	
//	class PveState implements WarState{
//		
//		public void war(){
//			setWarState(new PvePvpState(PVP_TIME));
//		}
//		
//		public void enter() {
//			unPvpFaction();
//		}
//
//		public void exit() {
//			
//		}
//
//		public void update() {
//			if(level>MAX_LEVEL){
//				setWarState(new PvpState());
//			}
//		}
//	}
//	
//	class PvePvpState implements WarState{
//		
//		public int time;
//		
//		public void war(){
//			this.time = Time.currTime + PVP_TIME;
//		}
//		
//		public PvePvpState(int time){
//			this.time = Time.currTime + time;
//		}
//		
//		public void enter() {
//			pvpFaction(0);
//		}
//
//		public void exit() {
//			
//		}
//
//		public void update() {
//			if(level>MAX_LEVEL){
//				setWarState(new PvpState());
//				return;
//			}
//			if(Time.currTime>time){
//				setWarState(new PveState());
//			}
//		}
//	}
}

class KillCreatureRecorder {
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger
			.getLogger(KillCreatureRecorder.class);

	protected List<Integer> killTemplateID = new ArrayList<Integer>(3);
	protected List<List> killOwners = new ArrayList<List>(3);

	public void add(int templateId, List<Player> owners) {
		killTemplateID.add(templateId);
		killOwners.add(owners);
	}
	
	public int get(int templateId) {
		int ret = 0;
		for (int i = killTemplateID.size() - 1; i >= 0; i--) {
			if (killTemplateID.get(i) == templateId) {
				ret++;
			}
		}
		return ret;
	}
	
	/**
	 * 取得和某个朋友共同杀死的怪物数量。
	 * @param templateId 怪物模板ID
	 * @param mate 朋友ID
	 * @return
	 */
	public int get(int templateId, int mate) {
		if (mate == -1) {
			return 0;
		}
		int ret = 0;
		for (int i = killTemplateID.size() - 1; i >= 0; i--) {
			if (killTemplateID.get(i) == templateId) {
				boolean found = false;
				List<Player> owners = (List<Player>) killOwners.get(i);
				for (Player p : owners) {
					if (p.id == mate) {
						found = true;
						break;
					}
				}
				if (found) {
					ret++;
				}
			}
		}
		return ret;
	}

	public void clear() {
		killTemplateID.clear();
		killOwners.clear();
	}
}

class TouchNpcInfo {
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(TouchNpcInfo.class);

	public GameObjectRef npcRef;
	public int questId;

	public TouchNpcInfo(GameObjectRef npcRef, int questId) {
		this.npcRef = npcRef;
		this.questId = questId;
	}
}

class UINotify {
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(UINotify.class);

	public int questId;
	public int id;
	public int type;
	public int answer;

	public UINotify(int questId, int id, int type, int answer) {
		this.questId = questId;
		this.id = id;
		this.type = type;
		this.answer = answer;
	}
}

class AutoAttack {
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(AutoAttack.class);

	public boolean fired;
	public int time;
	public GameObjectRef ref;
	public Attack attack;

	public void clear() {
		fired = false;
		time = 0;
		ref = null;
		attack = null;
	}

}

class ItemUse {
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(ItemUse.class);

	public GameObjectRef target;
	public int time;
	public int gridId;
	public int itemId;
	public int instanceId;

	public ItemUse(GameObjectRef target, int gridId, int itemId,
			int instanceId, int time) {
		this.target = target;
		this.gridId = gridId;
		this.itemId = itemId;
		this.instanceId = instanceId;
		this.time = time;
	}
}

class HorseRide {

	public HorseRide(int instanceId, int time, int serial) {
		this.instanceId = instanceId;
		this.time = time;
		this.serial = serial;
	}

	public int instanceId;
	public int time;
	public int serial;
}

class ClientMove {
	public int x, y;
	public byte direct;
	public short state;
	public int time;

	public ClientMove(int x, int y, byte direct, short state, int time) {
		this.x = x;
		this.y = y;
		this.state = state;
		this.direct = direct;
		this.time = time;
	}
}

class Position {
	public int mapId, x, y, time, clientTime, speed;

	public Position(int mapId, int x, int y, int time, int clientTime, int sp) {
		this.mapId = mapId;
		this.x = x;
		this.y = y;
		this.time = time;
		this.clientTime = clientTime;
		this.speed = sp;
	}
}

class Gather {

	public int serial;

	public GameObjectRef ref;
	public int time;

	public Gather(int serial, GameObjectRef ref, int time) {
		this.serial = serial;
		this.ref = ref;
		this.time = time;
	}
}

class Report{
	
	public int lastActionTime; //上次动作时间
	public int[] reportPlayerId; //举报人
	public int reportStartTime; //第一次举报时间
	public int effectReport; //有效举报数
	
	public void report(int playerId) throws Exception{
		if(reportPlayerId==null){
			reportPlayerId = new int[1];
			reportPlayerId[0] = playerId;
		}else{
			if(hasReported(playerId))
				throw new Exception(peony.Messages.STRING_01600);
			reportPlayerId = new int[reportPlayerId.length+1];
			reportPlayerId[reportPlayerId.length-1] = playerId;
		}
		if(reportStartTime==0)
			reportStartTime = Time.currTime;
		effectReport++;
	}
	
	private boolean hasReported(int playerId){
		for(int id : reportPlayerId){
			if(playerId==id)
				return true;
		}
		return false;
	}
	
	public void clear(){
		reportPlayerId = null;
		reportStartTime = 0;
		effectReport = 0;
	}
	
}

class AntiPlug{
	
	public static Random ran = new Random();
	public int lastSendATime;
	public int lastSendBTime;
	public int A = -1;
	public int B = -1;
	public int C = -1;
	public Integer D = null;
	public Integer D1 = null;
	public Player owner;
	public String uiModel;
	public boolean isBot; //是否已经判定为外挂
	public int score; //发包型外挂
	public int score1; //技能型外挂
	public boolean sendA;
	
	public AntiPlug(Player player){
		this.owner = player;
	}
	
	public void calc(){
		A = (ran.nextInt()<<29 | ran.nextInt()>>3);
		B = ran.nextInt();
	}
	
	public void calc1(){
		String name = "game_world.etf";
		try {
			DataService ds = Server.server.getServiceRegistry().getDataService();
			byte[] data = ds.data.downloadFile(name, uiModel);
			if(data != null){
				C = data.length;
			}
		} catch (Exception e) {
			clear();
		}
	}
	
	public void enCode(){
		int key = (A>>29 & 7);
		switch(key){
		case 0:
			D = A & B & C;
			break;
		case 1:
			D = (~A) & B | C;
			break;
		case 2:
			D = (A | (~B)) & C;
			break;
		case 3:
			D = (~A) & (~B) | C;
			break;
		case 4:
			D = (~A) | (~B) | (~C);
			break;
		case 5:
			D = A & (B) | (~C);
			break;
		case 6:
			D = ((~A) | (B)) & (~C);
			break;
		case 7:
			D = A & (~B) & (~C);
			break;
		}
	}
	
	public void clear(){
		A = -1;
		B = -1;
		C = -1;
		D = null;
		D1 = null;
		lastSendBTime = 0;
		score = 0;
		sendA = false;
	}
	
	public Packet getPacketA(){
		Packet pt = new Packet(OpCode.ANTI_PLUG_SERVER);
		pt.put(0);
		pt.putInt(A);
		return pt;
	}
	
	public Packet getPacketB(){
		Packet pt = new Packet(OpCode.ANTI_PLUG_SERVER);
		pt.put(1);
		pt.putInt(B);
		return pt;
	}
	
}

class AntiBot{
	
	public static Random ran = new Random();
	public Player owner;
	
	public int requestType = -1;
	public int firstReqTime;
	public int secondReqTime;
	
	public boolean hasSendFirstReq;
	public boolean hasReceive;
	
	public int lastCycleTime;
	public static int cycle = 300000;
	public static int calcTime = 60000;
	public static int bot_time = 120000;
	
	public int errCount;
	
	public boolean isBot;
	
	public AntiBot(Player owner){
		this.owner = owner;
	}
	
	public Packet buildFistRequestPacket(){
		Packet pt = new Packet(OpCode.ANTI_BOT_SERVER);
		pt.putInt(-1);
		requestType = -1;
		hasReceive = false;
		return pt;
	}
	
	public Packet buildSecondRequestPacket(){
		Packet pt = new Packet(OpCode.ANTI_BOT_SERVER);
		int generateEncodeKey = generateEncodeKey();
		pt.putInt(generateEncodeKey);
		requestType = generateEncodeKey;
		return pt;
	}
	
	public int enCodeValue(int A, int B, int C){
		int D = -1;
		int key = (requestType>>29 & 7);
		switch(key){
		case 0:
			D = A & B & C;
			break;
		case 1:
			D = (~A) & B | C;
			break;
		case 2:
			D = (A | (~B)) & C;
			break;
		case 3:
			D = (~A) & (~B) | C;
			break;
		case 4:
			D = (~A) | (~B) | (~C);
			break;
		case 5:
			D = A & (B) | (~C);
			break;
		case 6:
			D = ((~A) | (B)) & (~C);
			break;
		case 7:
			D = A & (~B) & (~C);
			break;
		}
		return D;
	}
	
	public int generateEncodeKey(){
		int value = (ran.nextInt()<<29 | ran.nextInt()>>3);
		if(value==-1){
			return ran.nextInt(Integer.MAX_VALUE);
		}
		return value;
	}
	
}

