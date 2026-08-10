package com.pip.itimes.server.world;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;
import org.apache.mina.util.NewThreadExecutor;

import com.pip.accountskeleton.AccountSkeleton;
import com.pip.battleskeleton.BattleSkeleton;
import com.pip.itimes.net.JettyServer;
import com.pip.itimes.net.Session;
import com.pip.itimes.net.SessionHandler;
import com.pip.itimes.net.SessionRegistry;
import com.pip.itimes.net.UWAPAcceptor;
import com.pip.itimes.net.UWAPConnector;
import com.pip.itimes.server.dao.AdminDao;
import com.pip.itimes.server.dao.ArenaTeamDao;
import com.pip.itimes.server.dao.AuctionDao;
import com.pip.itimes.server.dao.BattlefieldDao;
import com.pip.itimes.server.dao.BbsDao;
import com.pip.itimes.server.dao.BlogDao;
import com.pip.itimes.server.dao.BuyDao;
import com.pip.itimes.server.dao.FriendsDao;
import com.pip.itimes.server.dao.GiftDao;
import com.pip.itimes.server.dao.HopeGrassDao;
import com.pip.itimes.server.dao.IbuyDao;
import com.pip.itimes.server.dao.IrechargeDao;
import com.pip.itimes.server.dao.LeaveMessageDao;
import com.pip.itimes.server.dao.MailDao;
import com.pip.itimes.server.dao.MasterDao;
import com.pip.itimes.server.dao.MateDao;
import com.pip.itimes.server.dao.MercenaryDao;
import com.pip.itimes.server.dao.OemDao;
import com.pip.itimes.server.dao.PetmanagerDao;
import com.pip.itimes.server.dao.PlayerDao;
import com.pip.itimes.server.dao.QuestionDao;
import com.pip.itimes.server.dao.ShopDao;
import com.pip.itimes.server.dao.SmsFeeDao;
import com.pip.itimes.server.dao.TongDao;
import com.pip.itimes.server.dao.TongIslandDao;
import com.pip.itimes.server.dao.TreasureDao;
import com.pip.itimes.server.dao.VoteContentDao;
import com.pip.itimes.server.dao.VoteDao;
import com.pip.itimes.server.stage.TwelfthLunarConfig;
import com.pip.itimes.server.stage.voteGiftGroups;
import com.pip.itimes.server.world.accountbinging.AccountBingingService;
import com.pip.itimes.server.world.activationcode.ActivationCodeService;
import com.pip.itimes.server.world.activityService.ActivityServer;
import com.pip.itimes.server.world.activityService.ActivityService;
import com.pip.itimes.server.world.aroundchina.ChinaService;
import com.pip.itimes.server.world.aroundchina.ChinaServlet;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleReplayerService;
import com.pip.itimes.server.world.battle.BattleService2;
import com.pip.itimes.server.world.battle.arena.ArenaService;
import com.pip.itimes.server.world.battle.arena.client.ArenaSession;
import com.pip.itimes.server.world.boss.BossService;
import com.pip.itimes.server.world.camp.CampMainService;
import com.pip.itimes.server.world.chr.ChristmasConfig;
import com.pip.itimes.server.world.fee.FeeService;
import com.pip.itimes.server.world.game.BattleFieldInstanceModel;
import com.pip.itimes.server.world.game.BattleForResourcesInstanceModel;
import com.pip.itimes.server.world.game.FallCalculator;
import com.pip.itimes.server.world.game.FallService2;
import com.pip.itimes.server.world.game.FarmInstanceModel;
import com.pip.itimes.server.world.game.GuildBattleFieldInstanceModel;
import com.pip.itimes.server.world.game.HouseInstanceModel;
import com.pip.itimes.server.world.game.InstanceForbid;
import com.pip.itimes.server.world.game.InstanceService;
import com.pip.itimes.server.world.game.WorldService;
import com.pip.itimes.server.world.lyrics.LoveLyricsConfig;
import com.pip.itimes.server.world.lyrics.LyricsConfig;
import com.pip.itimes.server.world.noahsark.NoahsarkConfig;
import com.pip.itimes.server.world.rabbitRace.RabbitRaceConfig;
import com.pip.itimes.server.world.riddles.RiddlesConfig;
import com.pip.itimes.server.world.riddles.RiddlesConfig2;
import com.pip.itimes.server.world.sports.SportsService;
import com.pip.itimes.server.world.sports.SportsTimer;
import com.pip.itimes.server.world.stat.RealtimeStatService;
import com.pip.itimes.server.world.toplist.TopListService;
import com.pip.net.DefaultRequestService;
import com.pip.net.IRequestService;

public class Server{
    private static final Logger log = Logger.getLogger(Server.class);
    public ConnectService connectService = null;
    public StageService stageService = null;
    public ChatService chatService = null;
    public AddLogService addLogService = null;
    public LockService lockService = null;
    public PlayerService playerService = null;
    public PositionService positionService = null;
//    public BattleService battleService = null;
    public TeamService teamService = null;
    public BufService bufService = null;
    public PhizService phizService = null;
    public MailService mailService = null;
    public AuctionService auctionService = null;
    public ShopService shopService = null;
    public BuyService buyService = null;
    public OemService oemService = null;
    public TongService tongService = null;
    public FriendService friendService = null;
    public Configuration configuration = null;
    public AdminService adminService = null;
    volatile public AuthSession authSession = null;
    public TimeService timeService = null;
    public PhoneService phoneService = null;
    public WorldService worldService = null;
//    public FallService fallService = null;
    public FallService2 fallService2 = null;
    public FeeService feeService = null;
    public PetSellService petSellService = null;
    public TreasureService treasureService = null;
    public HopeGrassService hopeGrassService = null;
    public BattleService2 battleService = null;
    
    public BattleReplayerService battleReplayerService = null;
    public BattleFieldInstanceModel battleField = null;
    public BattleForResourcesInstanceModel resourcesModel = null;
    public GuildBattleFieldInstanceModel guildBattleField = null;
    public HouseInstanceModel houseModel = null;
    public FarmInstanceModel farmInstanceModel = null;
    public InstanceService instanceService = null;
    public BbsService bbsService = null;
    public MateService mateService = null;
    public MasterService masterService = null;
    public StoreService storeService = null;
    public BlogService blogService = null;
    public LeaveMessageService leaveMessageService = null;
    public RobotService robotService = null;
    public VersionService versionService = null;
    public ModelService modelService = null;

    public QuestionService questionService = null;

    public AccountService loginService = null;

    public BossService bossService = null;
    public TopListService topListService = null;
    public SportsService sportsService = null;

    public GiftService giftService = null;
    public VoteService voteService = null;
    public static Server instance = null;
    public boolean isFake = false;

    public static boolean isMaintance = true;

    public ConnectSessionFactory connectSessionFactory = null;

    public Configuration consumeConfiguration = null;
    public CampBattleService campBattleService = null;
    public ShoutService shoutService = null;
    public TwelfthLunarService twelfthLunarService = null;
    public NoahsarkConfig noahsarkConfig = null;
    public IrechargeService irechargeService = null;
    public CampBattlefieldService campBattlefieldService = null;
    public MercenaryService mercenaryService = null;
    

    public IRequestService requestService;
    public AccountSkeleton accountSkeleton;
    public AutoTraceService autoTraceService;
    public static String revisionName = "PIP";
    public static String iMoneyChar = "i";
    public static String iMoneyString = "i币";
    public static String iMoneyStoreString = "i币卖场";
    public static String Cmcc_msg1 = "5月1日至7月30日，吉林移动用户登陆幻想当月升级到20级，就获赠话费10元，再升级到30级，还可获赠话费20元，活动结束还有100元充值卡大抽奖等你拿。";
    public static String Cmcc_msg2 = "幻想推广员招募，推荐友玩幻想得话费，推荐越多话费越多！有意请找吉林移动专区“推荐人联合会”或加入飞信群8533384";
    
    public static final int IMONEY_TYPE_PIP = 0;
    public static final int IMONEY_TYPE_CMCC = 1;
    public static final int IMONEY_TYPE_QQ = 2;
    public static int iMoneyType = IMONEY_TYPE_PIP;
    
    public static final int RMB_TO_IMONEY = 360;
    public static final int RMB_TO_CMONEY = 100;
    public static final int RMB_TO_QMONEY = 100;
    public static int consumerType = RMB_TO_IMONEY;

    //mengjie add
    public static HashSet CMCC_jilin_cityname = new HashSet();
    public static HashSet CMCC_jiangsu_cityname = new HashSet();
    public static HashSet CMCC_zhejiang_cityname = new HashSet();
    public static HashSet CMCC_guangdong_cityname = new HashSet();
    public static HashSet CMCC_fujian_cityname = new HashSet();
	public static Map<Integer,Date> player_lastlogout_time = new HashMap<Integer,Date>();
	public static Map<Integer,Integer> player_mg_time = new HashMap<Integer,Integer>();
	public static Map<Integer,Integer> player_questions_time = new HashMap<Integer,Integer>();
	
	public static Map<Integer, WorldBossEquipInfo> player_Delay= new HashMap<Integer,WorldBossEquipInfo>();
	//用于记录player_Delay中一次要删除的key
	public static List delay= new ArrayList();
	// 用于控制副本次数限制
	public static Map<Integer, InstanceForbid> player_InstanceForbid = new HashMap<Integer,InstanceForbid>();
	
    public BattleSkeleton battleSkeleton;
    public ArenaService arenaService;
    
    public ChinaService chinaService = null;
    private JettyServer jettyServer = null;
    private IbuyService ibuyService = null;
    private PetmanagerService petmanagerService = null;
    private FriendsService friendsService = null;
    private AccountBingingService accountbingingService = null;
    private ActivationCodeService activationcodeService = null;
    
    public static Map<String,Integer> cmcc_jilin_playerid = new HashMap<String,Integer>();
    public static Map<String,Integer> cmcc_fujian_playerid = new HashMap<String,Integer>();
    public static Map<Integer,Integer> playerid_point_info = new HashMap<Integer,Integer>();
    public static int cmcc_jilin_count = 0;
    public static Date cmcc_jilin_lasttime = new Date();
    public static List cmcc_jilin_list = new ArrayList();
    public static int cmcc_fujian_totalmoney = 0;
    
    //mengjie add end
    
    //leo add
    private SmsFeeService smsFeeService = null;
    public ArenaSession arenaSession = null;
    public CampMainService campMainService = null;
    //leo add end
    
    //leo add
    public static RealtimeStatService realtimeStatService;
    //leo add end
    
    
    public static ActivityServer activityServer;
    
    public static ActivityService activityService;
    
    public static boolean demoForVersion2;
    
    public IMoneyCardService iMoneyCardService;
    
    SessionRegistry registry = new SessionRegistry();

    public Server() {
    }

    /**
     * 为了统计而做的入口，只载入解析数据所需要的服务。
     * @param datadir
     */
    public Server(String datadir) throws Exception {
    	instance = this;
    	isFake = true;
    	stageService = new StageService(new File(datadir));
    }

    public void launch() throws Exception {
        configuration = new PropertiesConfiguration(
                "config.properties");
        demoForVersion2 = "1".equals(configuration.getString("version2demo"));
        connectSessionFactory = ConnectSessionFactory.getFactory(configuration.getString("connectsessionfactory"));
        if("cmcc".equals(configuration.getString("connectsessionfactory"))){
        	revisionName = "CMCC";
            iMoneyChar = "点";
            iMoneyString = "点数";
            iMoneyStoreString = "道具卖场";
            iMoneyType = IMONEY_TYPE_CMCC;
            consumerType = RMB_TO_CMONEY;
            CMCC_jilin_cityname.add("长春");
            CMCC_jilin_cityname.add("吉林");
            CMCC_jilin_cityname.add("延吉");
            CMCC_jilin_cityname.add("四平");
            CMCC_jilin_cityname.add("通化");
            CMCC_jilin_cityname.add("白城");
            CMCC_jilin_cityname.add("辽源");
            CMCC_jilin_cityname.add("松原");
            CMCC_jilin_cityname.add("白山");
            CMCC_jilin_cityname.add("梅河口");
            CMCC_jilin_cityname.add("珲春");

            CMCC_guangdong_cityname.add("广州");
            CMCC_guangdong_cityname.add("深圳");
            CMCC_guangdong_cityname.add("珠海");
            CMCC_guangdong_cityname.add("汕头");
            CMCC_guangdong_cityname.add("韶关");
            CMCC_guangdong_cityname.add("佛山");
            CMCC_guangdong_cityname.add("江门");
            CMCC_guangdong_cityname.add("湛江");
            CMCC_guangdong_cityname.add("茂名");
            CMCC_guangdong_cityname.add("肇庆");
            CMCC_guangdong_cityname.add("惠州");
            CMCC_guangdong_cityname.add("梅州");
            CMCC_guangdong_cityname.add("汕尾");
            CMCC_guangdong_cityname.add("河源");
            CMCC_guangdong_cityname.add("阳江");
            CMCC_guangdong_cityname.add("清远");
            CMCC_guangdong_cityname.add("东莞");
            CMCC_guangdong_cityname.add("中山");
            CMCC_guangdong_cityname.add("潮州");
            CMCC_guangdong_cityname.add("揭阳");
            CMCC_guangdong_cityname.add("云浮");
            
            CMCC_jiangsu_cityname.add("南京");
            CMCC_jiangsu_cityname.add("苏州");
            CMCC_jiangsu_cityname.add("无锡");
            CMCC_jiangsu_cityname.add("常州");
            CMCC_jiangsu_cityname.add("镇江");
            CMCC_jiangsu_cityname.add("南通");
            CMCC_jiangsu_cityname.add("泰州");
            CMCC_jiangsu_cityname.add("扬州");
            CMCC_jiangsu_cityname.add("淮安");
            CMCC_jiangsu_cityname.add("盐城");
            CMCC_jiangsu_cityname.add("徐州");
            CMCC_jiangsu_cityname.add("连云港");
            CMCC_jiangsu_cityname.add("宿迁");
            
            CMCC_zhejiang_cityname.add("杭州");
            CMCC_zhejiang_cityname.add("宁波");
            CMCC_zhejiang_cityname.add("温州");
            CMCC_zhejiang_cityname.add("嘉兴");
            CMCC_zhejiang_cityname.add("湖州");
            CMCC_zhejiang_cityname.add("绍兴");
            CMCC_zhejiang_cityname.add("金华");
            CMCC_zhejiang_cityname.add("衢州");            
            CMCC_zhejiang_cityname.add("舟山");
            CMCC_zhejiang_cityname.add("台州");
            CMCC_zhejiang_cityname.add("丽水");
            
            CMCC_fujian_cityname.add("福州");
            CMCC_fujian_cityname.add("厦门");
            CMCC_fujian_cityname.add("宁德");
            CMCC_fujian_cityname.add("莆田");
            CMCC_fujian_cityname.add("泉州");
            CMCC_fujian_cityname.add("漳州");
            CMCC_fujian_cityname.add("龙岩");
            CMCC_fujian_cityname.add("三明");
            CMCC_fujian_cityname.add("南平");
            
        }else if("qq".equals(configuration.getString("connectsessionfactory"))){
        	revisionName = "QQ";
            iMoneyChar = "元宝";
            iMoneyString = "元宝";
            iMoneyStoreString = "道具卖场";
            iMoneyType = IMONEY_TYPE_QQ;
            consumerType = RMB_TO_QMONEY;
        }
        requestService = new DefaultRequestService();
        bbsService = new BbsService(new BbsDao());//公告板和家园公告
        versionService = new VersionService();
        modelService = new ModelService();
        modelService.startup();
        connectService = new ConnectService();
        stageService = new StageService(new File(configuration.getString("datadir")));
        stageService.setConnectService(connectService);
        connectService.setStageService(stageService);
        playerService = new PlayerService(new PlayerDao());//用户信息
        BaseMonsterAI.playerService = playerService;
        connectService.setPlayerService(playerService);
        LoveLyricsConfig.playerService = playerService;
        
        RabbitRaceConfig.playerService = playerService;	//兔子赛跑
        RabbitRaceConfig.connectService = connectService;
        
        RiddlesConfig.playerService = playerService;
        RiddlesConfig2.playerService = playerService;
        LyricsConfig.playerService = playerService;
//        chatService = new ChatService();
//        chatService.setStageService(stageService);
//        chatService.setConnectService(connectService);
        lockService = new LockService();//锁住用户（采集和打服务器怪等等时用）
        positionService = new PositionService();//用户位置信息
        positionService.setConnectService(connectService);
        instanceService = new InstanceService();//副本信息
        worldService = new WorldService(stageService,positionService,instanceService);//场景
        worldService.setConnectService(connectService);
        stageService.setWorldService(worldService);
        chatService = new ChatService();//聊天功能
        chatService.setStageService(stageService);
        chatService.setConnectService(connectService);
        chatService.setInstanceService(instanceService);
        chatService.setPlayerService(playerService);
        battleService = new BattleService2();//战斗
        battleService.setConnectService(connectService);
        battleService.setStageService(stageService);
        battleService.setChatService(chatService);
        battleService.setPlayerService(playerService);
        battleService.setPositionService(positionService);
        ChristmasConfig.setChatService(chatService);

        loginService = new AccountService();//登陆
//        battleFieldService = new BattleFieldService();
//        battleFieldService.setBattleService(battleService);
//        battleFieldService.setPlayerService(playerService);
//        worldService.setBattleFieldService(battleFieldService);
        teamService = new TeamService();//组队功能
        teamService.setChatService(chatService);
//        battleService.setTeamService(teamService);
        treasureService = new TreasureService(new TreasureDao());//挖宝
        hopeGrassService = new HopeGrassService(new HopeGrassDao());//漂流瓶
        bufService = new BufService();//使用物品
        bufService.setConnectService(connectService);
        bufService.setStageService(stageService);
        bufService.setTreasureService(treasureService);
        bufService.setHopeGrassService(hopeGrassService);
        bufService.setChatService(chatService);
        bufService.setPositionService(positionService);
        bufService.setWorldService(worldService);
        playerService.setBufService(bufService);
        mailService = new MailService(new MailDao());//精灵速递
        mailService.setConnectService(connectService);
        mailService.setPlayerService(playerService);
        mailService.setChatService(chatService);
        playerService.setMailService(mailService);
        mailService.start();
        bufService.setMailService(mailService);
        auctionService = new AuctionService(new AuctionDao());//拍卖
        auctionService.setMailService(mailService);
        auctionService.start();
        shopService = new ShopService(new ShopDao());//店铺
        auctionService.setShopService(shopService);
        buyService = new BuyService(new BuyDao());//收购
        oemService = new OemService(new OemDao());//求做
        tongService = new TongService(new TongDao(),new TongIslandDao());//公会
        tongService.setPlayerService(playerService);
        tongService.setStageService(stageService);
        tongService.setConnectService(connectService);
        tongService.setChatService(chatService);
        tongService.setWorldService(worldService);
        tongService.setBbsService(bbsService);
        tongService.initIslands();
        bufService.setTongService(tongService);
        chatService.setTongService(tongService);
        battleService.setTongService(tongService);
        friendService = new FriendService();//好友
        friendService.setConnectService(connectService);
        friendService.setPlayerService(playerService);
        adminService = new AdminService(new AdminDao());//GM
        chatService.setAdminService(adminService);
        mailService.setAdminService(adminService);

        questionService = new QuestionService(new QuestionDao());//答题
        questionService.setChatService(chatService);
        timeService = new TimeService();//定时器
        timeService.setConnectService(connectService);
        phoneService = new PhoneService();
        stageService.setPhoneService(phoneService);
//        fallService = new FallService();
//        fallService.setChatService(chatService);
//        fallService.setConnectService(connectService);
//        fallService.setStageService(stageService);
//        fallService.setPlayerService(playerService);
//        battleService.setFallService(fallService);
        fallService2 = new FallService2();//掉落
        fallService2.setChatService(chatService);
        fallService2.setConnectService(connectService);
        fallService2.setStageService(stageService);
        fallService2.setPlayerService(playerService);
        battleService.setFallService(fallService2);
        shopService.setAuctionService(auctionService);
        shopService.setOemService(oemService);
        shopService.setBuyService(buyService);
        log.info("load shops");
//        shopService.loadAllShops();
        log.info("shops loaded");
        shopService.start();
        playerService.setShopService(shopService);
        playerService.start();
        feeService = new FeeService();//计费
        feeService.setPlayerService(playerService);
        feeService.setConnectService(connectService);
        feeService.setStageService(stageService);
        feeService.setChatService(chatService);
        petSellService = new PetSellService();//宠物买卖
        mateService = new MateService(new MateDao());//夫妻
        mateService.setPlayerService(playerService);
        mateService.setMailService(mailService);
        battleField = new BattleFieldInstanceModel();//个人战场
        battleField.setBattleService(battleService);
        battleField.setInstanceService(instanceService);
        battleField.setPlayerService(playerService);
        battleField.setWorldService(worldService);
        battleField.setChatService(chatService);
        battleField.setBbsService(bbsService);
        battleField.setMailService(mailService);

        houseModel = new HouseInstanceModel();//房屋
        houseModel.setInstanceService(instanceService);
        houseModel.setWorldService(worldService);
        houseModel.setPlayerService(playerService);
        houseModel.setMateService(mateService);
        playerService.setHouserModel(houseModel);
        chatService.setHouseModel(houseModel);
        
        farmInstanceModel = new FarmInstanceModel();	//庄园
        farmInstanceModel.setInstanceService(instanceService);
        farmInstanceModel.setWorldService(worldService);
        farmInstanceModel.setPlayerService(playerService);
        farmInstanceModel.setMateService(mateService);
        farmInstanceModel.setHouseInstanceModel(houseModel);
        farmInstanceModel.setMailService(mailService);

        worldService.setBattleField(battleField);
        guildBattleField = new GuildBattleFieldInstanceModel();//公会战场
        guildBattleField.setBattleService(battleService);
        guildBattleField.setInstanceService(instanceService);
        guildBattleField.setPlayerService(playerService);
        guildBattleField.setWorldService(worldService);
        guildBattleField.setChatService(chatService);
        guildBattleField.setBbsService(bbsService);
        guildBattleField.setMailService(mailService);
        guildBattleField.setTongService(tongService);
        worldService.setGuildBattleField(guildBattleField);
        worldService.setHouseModel(houseModel);
        worldService.setFarmInstanceModel(farmInstanceModel);
        farmInstanceModel.start();
        
        resourcesModel = new BattleForResourcesInstanceModel();// 阵营战场
        resourcesModel.setInstanceService(instanceService);
        resourcesModel.setWorldService(worldService);
        resourcesModel.setChatService(chatService);
        resourcesModel.setMailService(mailService);
        worldService.setResourcesModel(resourcesModel);
        
        worldService.load();
        
//        houseModel.loadAllHouse();
        BattleFieldTimer.setBattleField(battleField);
        BattleFieldTimer.setGuildBattleField(guildBattleField);
        BattleFieldTimer.start();
        
        campBattlefieldService = new CampBattlefieldService(new BattlefieldDao());
        campBattlefieldService.setBattleField(resourcesModel);
        campBattlefieldService.setPlayerService(playerService);
        campBattlefieldService.setConnectService(connectService);
        campBattlefieldService.setStageService(stageService);
        campBattlefieldService.setInstanceService(instanceService);
        campBattlefieldService.setWorldService(worldService);
        campBattlefieldService.setChatService(chatService);
        campBattlefieldService.setMailService(mailService);
        campBattlefieldService.setBattleService(battleService);
        CampBattlefieldService.start();
        battleService.setCampBattlefieldService(campBattlefieldService);
        resourcesModel.setCampBattlefieldService(campBattlefieldService);
        resourcesModel.setBattleService(battleService);

        masterService = new MasterService(new MasterDao());//师徒
        masterService.setPlayerService(playerService);
        mateService.loadMates();
        masterService.loadMasters();
        bufService.setMasterService(masterService);
        bufService.setMateService(mateService);
        playerService.setMasterService(masterService);
        bufService.setPlayerService(playerService);
        bufService.setHouseModel(houseModel);
        battleService.setBufService(bufService);
        if("cmcc".equals(configuration.getString("connectsessionfactory"))){
            storeService = new StoreService(false);//商店
        }else{
            storeService = new StoreService(false);
        }
        FallCalculator.setMasterService(masterService);
        FallCalculator.setMateService(mateService);
        blogService = new BlogService(new BlogDao());//家园blog
        leaveMessageService = new LeaveMessageService(new LeaveMessageDao());//家园留言
        robotService = new RobotService();//机器人
        robotService.setPlayerService(playerService);
        robotService.setPositionService(positionService);
        robotService.setWorldService(worldService);
        
        campBattleService = new CampBattleService();
        campBattleService.setConnectService(connectService);
        campBattleService.setMailService(mailService);
        campBattleService.setPlayerService(playerService);
        campBattleService.setChatService(chatService);
        campBattleService.setBufService(bufService);

        topListService = new TopListService();//排行榜
        topListService.setTongService(tongService);
        topListService.setPlayerService(playerService);
        topListService.setHouseInstanceModel(houseModel);
        topListService.setMailService(mailService);
        topListService.setChatService(chatService);
        topListService.setCampBattleService(campBattleService);
        topListService.start();
        
        bossService = new BossService(); //世界boss
        bossService.setPlayerService(playerService);
        bossService.setStageService(stageService);
        bossService.setConnectService(connectService);
        bossService.setPositionService(positionService);
        bossService.setChatService(chatService);
        bossService.start();
        playerService.setBossService(bossService);
        battleService.setBossService(bossService);
        fallService2.setBossService(bossService);
        
        shoutService = new ShoutService();
        chatService.setShoutService(shoutService);
        topListService.setShoutService(shoutService);
        shoutService.setMailService(mailService);
        shoutService.setChatService(chatService);
        
        twelfthLunarService = new TwelfthLunarService();
        topListService.setTwelfthLunarService(twelfthLunarService);
        twelfthLunarService.setMailService(mailService);
        twelfthLunarService.setChatService(chatService);
        twelfthLunarService.setPlayerService(playerService);
        TwelfthLunarService.currentSegment = TwelfthLunarService.STAGE_NOT_STARTED;
        twelfthLunarService.setAllStage();
        twelfthLunarService.setCurrentSegment(twelfthLunarService.checkEffectivePeriod());
        //诺亚方舟
        noahsarkConfig = new NoahsarkConfig();
        noahsarkConfig.setMailService(mailService);
        noahsarkConfig.setChatService(chatService);
        noahsarkConfig.setPlayerService(playerService);
        NoahsarkConfig.setNoahsarkStage();
   
        
        mercenaryService = new MercenaryService(new MercenaryDao());
        mercenaryService.setPositionService(positionService);
        mercenaryService.setPlayerService(playerService);
        mercenaryService.setTeamService(teamService);
        mercenaryService.setBattleService(battleService);
        mercenaryService.setConnectService(connectService);
        mercenaryService.init();
        mercenaryService.start();
        teamService.setMercenaryService(mercenaryService);
        
        battleService.setMercenaryService(mercenaryService);
        
        //初始化当前状态&设置活动日期
        ShoutService.currentSegment = ShoutService.STAGE_NOT_STARTED;
        
        battleService.setTopListService(topListService);
        battleService.setHouseModel(houseModel);
        sportsService = new SportsService();//运动会
        sportsService.setBbsService(bbsService);
        sportsService.setChatService(chatService);
        sportsService.setMailService(mailService);
        sportsService.setTongService(tongService);
        SportsTimer.setSportsService(sportsService);
        SportsTimer.start();
        log.info("Top List Service OK");
        
        iMoneyCardService = new IMoneyCardService();
        
        irechargeService = new IrechargeService(new IrechargeDao());	//i币充值记录
        
        //mengjie moved
        chatService.start();
        timeService.start();
        playerService.setConnectService(connectService);
        if("pip".equals(configuration.getString("connectsessionfactory"))){
            //mengjie add
            chinaService = new ChinaService();//神州行付费
            chinaService.launch();
            chinaService.setConnectService(connectService);

            accountbingingService = new AccountBingingService();//等待处理的付费请求
            accountbingingService.launch();
            accountbingingService.setConnectService(connectService);
            accountbingingService.setStageService(stageService);
            accountbingingService.setMailService(mailService);
            activationcodeService = new ActivationCodeService();//等待处理的付费请求
            activationcodeService.launch();
            activationcodeService.setConnectService(connectService);
            activationcodeService.setStageService(stageService);
            activationcodeService.setMailService(mailService);
            //回调http服务
            jettyServer = new JettyServer(configuration.getString("localip"),
                                          configuration.getInt("webport"), 3, 10);//神州行回调
            jettyServer.addServlet("/chinarun", new ChinaServlet(connectService, playerService, irechargeService));
            jettyServer.start();
        }
        ibuyService = new IbuyService(new IbuyDao());//i币卖场消费记录
        smsFeeService = new SmsFeeService(new SmsFeeDao()); //短信购买消费记录
        petmanagerService = new PetmanagerService(new PetmanagerDao());	// 宠物修炼
        playerService.setPetmanagerService(petmanagerService);
        friendsService = new FriendsService(new FriendsDao());//知己
        playerService.setFriendsService(friendsService);
        CreditShopTimer.setAuctionService(auctionService);//荣誉拍卖自动登陆物品计时器
        auctionService.setChatService(chatService);
        auctionService.setPlayerService(playerService);
        playerService.setChatService(chatService);
        CreditShopTimer.start();
        worldService.setChatService(chatService);
        bufService.setFriendsService(friendsService);
        bufService.setAddLogService(addLogService);
        //mengjie add end
        
        phizService = new PhizService();
        phizService.setConnectService(connectService);
        phizService.setPlayerService(playerService);
        phizService.setPositionService(positionService);
        battleService.setPhizService(phizService);
        positionService.setPhizService(phizService);
        bufService.setPhizService(phizService);
        giftService = new GiftService(new GiftDao());//自动发奖
        giftService.setStageService(stageService);
        
        autoTraceService = new AutoTraceService(new File(configuration.getString("datadir")));
        autoTraceService.setChatService(chatService);
        autoTraceService.setConnectService(connectService);
        
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        
        voteService = new VoteService(new VoteDao(), new VoteContentDao(), new PlayerDao());
        voteService.loadVote(new File(configuration.getString("datadir")));
        Enumeration<Integer> enumer = voteGiftGroups.getEnumeration();
		while (enumer.hasMoreElements()) {
			int voteType = (Integer) enumer.nextElement();
			voteService.loadVoteContentData(voteType);
	        voteService.loadVoteData(voteType);
		}
        voteService.setPlayerService(playerService);
        voteService.setMailService(mailService);
        voteService.setConnectService(connectService);
        voteService.setChatService(chatService);
        topListService.setVoteService(voteService);
        stageService.setVoteService(voteService);
        
        arenaService = new ArenaService(new ArenaTeamDao(),new PlayerDao());//跨服竞技场
        arenaService.setPlayerService(playerService);
        
        //战斗回放服务
        battleReplayerService = new BattleReplayerService();
        battleReplayerService.setChatService(chatService);
        battleReplayerService.setConnectService(connectService);
        battleReplayerService.setPlayerService(playerService);
        battleReplayerService.setStageService(stageService);
        
        //新阵营
        campMainService = new CampMainService(connectService, playerService, chatService,mailService);
        auctionService.setCampMainService(campMainService);
        mailService.setCampMainService(campMainService);
        battleService.setCampMainService(campMainService);
        battleService.setInstanceService(instanceService);
        bufService.setCampMainService(campMainService);
        log.info("Camp Main Service Started");
        
        activityService = new ActivityService ();
        activityService.startup();
        activityService.setPlayerService(playerService);
        
        activityServer = new ActivityServer (activityService);
        activityServer.setMailService(mailService);
        activityServer.setIrechargeService(irechargeService);
        activityServer.setConnectService(connectService);
        activityServer.setPlayerService(playerService);
        log.info("Activity Server Started");
        
        //统计平台实时报告服务
        realtimeStatService = new RealtimeStatService();
        realtimeStatService.setPlayerService(playerService);
        realtimeStatService.start();
        
        //mengjie moved ok
//        long current = System.currentTimeMillis();
//        battleField.start(current+20*60*1000L,current+30*60*1000L,current+35*60*1000L);
        if (!configuration.getBoolean("poolbuffer")) {
            ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
        }
        if (configuration.getBoolean("directbuffer")) {
            ByteBuffer.setUseDirectBuffers(true);
        } else {
            ByteBuffer.setUseDirectBuffers(false);
        }

        SessionHandler connectSessionHandler = new ConnectSessionHandler(
                registry);
        AuthSessionHandler authSessionHandler = new AuthSessionHandler(registry);
        log.info("connect auth");
        connectAuth(authSessionHandler);
        playerService.setAccountSkeleton(accountSkeleton);
        feeService.setAccountSkeleton(accountSkeleton);
        battleService.setAccountSkeleton(accountSkeleton);
        bufService.setAccountSkeleton(accountSkeleton);
//        while(authSession==null);
//        log.info("Auth connected");
        bind(registry,connectSessionHandler);
        AdminSessionHandler adminSessionHandler = new AdminSessionHandler(registry);
        bindAdmin(adminSessionHandler);
        log.info("WorldServer started");
        //连接竞技场服务器
        arenaSessionTryConnect();
    }

    private void connectAuthServer(AuthSessionHandler handler) throws Exception {
        String factory = configuration.getString("connectsessionfactory");
        if ("cmcc".equals(factory) || "pip".equals(factory)) {
            // CMCC版本和PIP版本都需要连接卓望认证服务器
            IoConnector connector = new UWAPConnector(2, Executors.newCachedThreadPool());
            SocketConnectorConfig sconfig = (SocketConnectorConfig) connector.getDefaultConfig();
            sconfig.setThreadModel(ThreadModel.MANUAL);
            SocketSessionConfig sc = (SocketSessionConfig) sconfig.getSessionConfig();
            sc.setReceiveBufferSize(configuration.getInt("authreceivebuffsize"));
            sc.setSendBufferSize(configuration.getInt("authwritebuffsize"));
            sc.setTcpNoDelay(configuration.getBoolean("tcpnodelay"));
    //        handler.setConnector(connector);
            ConnectFuture future = connector.connect(new InetSocketAddress(
                    configuration.getString("cmccauthip"),
                    configuration.getInt("cmccauthport")), handler, sconfig);
            
            long timeout = System.currentTimeMillis() + 30000L;
            log.info("cmcc auth connecting");
            future.join(30000L);
            while (authSession == null && System.currentTimeMillis() < timeout) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
            }
            if (authSession == null) {
                log.info("Auth connect fail");
            } else {
                log.info("Auth connected");
            }
        }
    }
    
    private void connectAccountServer() throws Exception {
         String factory = configuration.getString("connectsessionfactory");
         if (!"cmcc".equals(factory)) {
            // PIP版本和QQ版本连接新版gameaccount
            accountSkeleton = new AccountSkeleton("accountskeleton",
                                                  new InetSocketAddress(configuration.getString("authip"),
                    configuration.getInt("authport")), requestService, connectService, feeService);
            accountSkeleton.setUserName(configuration.getString("serverid"));
            accountSkeleton.setPassword(configuration.getString("serverpassword"));
            accountSkeleton.connect();
            log.info("auth connected");
        }
    }
    
    //mengjie add battleserver
//    private void connectBattleServer() throws Exception {
//        String factory = configuration.getString("connectsessionfactory");
//        if ("pip".equals(factory)) {
//           // PIP版本连接
//           battleSkeleton = new BattleSkeleton("battleskeleton",
//                                                 new InetSocketAddress(configuration.getString("battleip"),
//                   configuration.getInt("battleport")), requestService, connectService, feeService);
//           battleSkeleton.setUserName(configuration.getString("serverid"));
//           battleSkeleton.setPassword(configuration.getString("serverpassword"));
//           battleSkeleton.connect();
//           log.info("battle connected");
//       }
//   }
    //mengjie add end
    private void connectAuth(AuthSessionHandler handler) throws Exception {
        connectAuthServer(handler);
        connectAccountServer();
    }
    
    /*
     * 卓望版本，认证连接断开处理，反复尝试重连。
     */
    public void authSessionClosed(AuthSession authSess) {
        if (authSess != authSession) {
            return;
        }
        authSession = null;
        new Thread() {
            public void run() {
                while (authSession == null) {
                    try {
//                        SessionRegistry registry = new SessionRegistry();
                        AuthSessionHandler authSessionHandler = new AuthSessionHandler(registry);
                        log.info("connect cmcc auth");
                        connectAuthServer(authSessionHandler);
                        Thread.sleep(10000L);
                    } catch (Exception e) {
                    }
                }
            }
        }.start();
    }
    private void bindAdmin(SessionHandler sessionHandler) throws Exception{
        IoAcceptor acceptor = new UWAPAcceptor(1,new NewThreadExecutor());
        acceptor.bind(new InetSocketAddress(configuration.getString("localip"),configuration.getInt("adminport")),sessionHandler);
    }

    private void bind(SessionRegistry registry,SessionHandler sessionHandler) throws Exception{
//        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
//        ByteBuffer.setUseDirectBuffers(false);
        IoAcceptor acceptor = new UWAPAcceptor(2,Executors.newCachedThreadPool());
        DefaultIoFilterChainBuilder filterChainBuilder = acceptor.getDefaultConfig().getFilterChain();
        filterChainBuilder.addLast("threadPool", new ExecutorFilter());
        SocketAcceptorConfig sconfig = (SocketAcceptorConfig) acceptor.getDefaultConfig();
        sconfig.setThreadModel(ThreadModel.MANUAL);
        SocketSessionConfig sc = (SocketSessionConfig) sconfig.getSessionConfig();
        sc.setReceiveBufferSize(configuration.getInt("receivebuffsize"));
        sc.setSendBufferSize(configuration.getInt("writebuffsize"));
        sc.setTcpNoDelay(configuration.getBoolean("tcpnodelay"));
        acceptor.bind(new InetSocketAddress(configuration.getString("localip"),configuration.getInt("port")),sessionHandler,sconfig);
        connectService.setAcceptor(acceptor);
    }
    
    private void connectArenaServer(ArenaSessionHandler handler) throws Exception{
        IoConnector connector = new UWAPConnector(2, Executors.newCachedThreadPool());
        SocketConnectorConfig sconfig = (SocketConnectorConfig) connector.getDefaultConfig();
        sconfig.setThreadModel(ThreadModel.MANUAL);
        SocketSessionConfig sc = (SocketSessionConfig) sconfig.getSessionConfig();
        sc.setReceiveBufferSize(configuration.getInt("arenareceivebuffersize"));
        sc.setSendBufferSize(configuration.getInt("arenawritebuffersize"));
        sc.setTcpNoDelay(configuration.getBoolean("tcpnodelay"));
        ConnectFuture future = connector.connect(new InetSocketAddress(configuration.getString("arenaip"), configuration.getInt("arenaport")), handler, sconfig);

        long timeout = System.currentTimeMillis() + 30000L;
        log.info("Arena connecting");
        future.join(30000L);

        while(arenaSession == null && System.currentTimeMillis() < timeout){
            try{
                Thread.sleep(100);
            }catch(Exception e){
            }
        }

        if(arenaSession == null){
            log.info("Arena connect fail");
        }else{
            log.info("Arena connected");
        }
    }
    
    public void arenaSessionClosed(ArenaSession arenaSess) {
        if (arenaSess != arenaSession) {
            return;
        }
        
        arenaSession = null;
        arenaSessionTryConnect();
    }
    
    public void arenaSessionTryConnect(){
        new Thread() {
            public void run() {
                while (arenaSession == null) {
                    try {
                        ArenaSessionHandler arenaSessionHandler = new ArenaSessionHandler(registry);
                        log.info("connect arena server");
                        connectArenaServer(arenaSessionHandler);
                        Thread.sleep(10000L);
                    } catch (Exception e) {
                    }
                }
            }
        }.start();
    }
    
    public static String getGameCode() {
        return instance.configuration.getString("gamecode");
    }

    public static void main(String[] args) {
        try {
            instance = new Server();
            instance.launch();
//            instance.createPlayers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void createPlayers() throws Exception{
//        for(int i=0;i<10000;i++){
//            Player p = playerService.createDefaultPlayer(i+1,"test"+i,(byte)0,0,20,null);
//            playerService.savePlayer(p);
//            System.out.println(i+"created");
//        }
//    }

    class ConnectSessionHandler extends SessionHandler {

        public void exceptionCaught(IoSession session, Throwable ex) throws
                Exception {
            log.error(ex,ex);
        }

        public Session createSession(IoSession session) {
            ConnectSession ret = connectSessionFactory.createSession(session);
            ret.setConnectService(connectService);
            ret.setStageService(stageService);
            ret.setLockService(lockService);
            ret.setPlayerService(playerService);
            ret.setRobotService(robotService);
            ret.setPositionService(positionService);
            ret.setBattleService(battleService);
            ret.setTeamService(teamService);
            ret.setBufService(bufService);
            ret.setMailService(mailService);
            ret.setShopService(shopService);
            ret.setAuctionService(auctionService);
            ret.setBuyService(buyService);
            ret.setOemService(oemService);
            ret.setTongService(tongService);
            ret.setFriendService(friendService);
            ret.setConfiguration(configuration);
            ret.setChatService(chatService);
            ret.setAddLogService(addLogService);
            ret.setAdminService(adminService);
            ret.setWorldService(worldService);
//            ret.setFallService(fallService);
            ret.setFallService2(fallService2);
            ret.setPetSellService(petSellService);
            ret.setBattleField(battleField);
            ret.setMateService(mateService);
            ret.setMasterService(masterService);
            ret.setStoreService(storeService);
            ret.setGuildBattleField(guildBattleField);
            ret.setBbsService(bbsService);
            ret.setBlogService(blogService);
            ret.setHouseModel(houseModel);
            ret.setFarmInstanceModel(farmInstanceModel);
            ret.setAccountService(loginService);
            ret.setLeaveMessageService(leaveMessageService);
            ret.setVersionService(versionService);
            ret.setTopListService(topListService);
            ret.setSportsService(sportsService);
            ret.setAccountSkeleton(accountSkeleton);
            ret.setRequestService(requestService);
            //mengjie add
            ret.setChinaService(chinaService);
            ret.setIbuyService(ibuyService);
            ret.setPetmanagerService(petmanagerService);
            ret.setFriendsService(friendsService);
            ret.setAccountBingingService(accountbingingService);
            ret.setActivationCodeService(activationcodeService);
            ret.setArenaService(arenaService);
            ret.setGiftService(giftService);
            ret.setAutoTraceService(autoTraceService);
            ret.setSmsFeeService(smsFeeService);
            ret.setBossService(bossService);
            //jwp add
            ret.setVoteService(voteService);
            ret.setBattleReplayerService(battleReplayerService);
            
            ret.setCampMainService(campMainService);
            ret.setCampBattleService(campBattleService);
            ret.setCampBattlefieldService(campBattlefieldService);
            ret.setIMoneyCardService(iMoneyCardService);
            
            ret.setTwelfthLunarService(twelfthLunarService);
            ret.setPhizService(phizService);
            
            ret.setMercenaryService(mercenaryService);
            
            return ret;
        }

        public ConnectSessionHandler(SessionRegistry registry) {
            super(registry);
        }
    }

    class AdminSessionHandler extends SessionHandler{
        public Session createSession(IoSession session){
            AdminSession ret = new AdminSession(session,configuration.getString("connectsessionfactory"));
            ret.setAdminService(adminService);
            ret.setChatService(chatService);
            ret.setConnectService(connectService);
            ret.setPlayerService(playerService);
            ret.setStageService(stageService);
            ret.setShopService(shopService);
            ret.setAuctionService(auctionService);
            ret.setMailService(mailService);
            ret.setBattleField(battleField);
            ret.setGuildBattleField(guildBattleField);
            ret.setBbsService(bbsService);
            ret.setRobotService(robotService);
            ret.setHouseModel(houseModel);
            ret.setFarmInstanceModel(farmInstanceModel);
            ret.setVersionService(versionService);
            ret.setSportsService(sportsService);
            ret.setAccountSkeleton(accountSkeleton);
            ret.setReuqestService(requestService);
            ret.setTongService(tongService);
            ret.setAccountbingingService(accountbingingService);
            ret.setTopListService(topListService);
            ret.setMasterService(masterService);
            ret.setMateService(mateService);
            ret.setFriendsService(friendsService);
            ret.setCampMainService(campMainService);
            ret.setActivityService(activityService);
            ret.setCampBattlefieldService(campBattlefieldService);
            return ret;
        }

        public AdminSessionHandler(SessionRegistry registry){
            super(registry);
        }
    }
    class AuthSessionHandler extends SessionHandler{
        public AuthSessionHandler(SessionRegistry registry){
            super(registry);
        }

        public Session createSession(IoSession session){
            authSession = new AuthSession(session);
            authSession.setClientRegistry(registry);
            authSession.setConfiguration(configuration);
            authSession.setFeeService(feeService);
            authSession.setConnectService(connectService);
            authSession.setStoreService(storeService);
            authSession.setPlayerService(playerService);
            authSession.setHouseModel(houseModel);
            authSession.setChatService(chatService);
            authSession.setMailService(mailService);
            authSession.setLoginService(loginService);
            authSession.setHouseModel(houseModel);
            authSession.setStageService(stageService);
            return authSession;
        }
    }
    
    class ArenaSessionHandler extends SessionHandler{
        public ArenaSessionHandler(SessionRegistry registry){
            super(registry);
        }
        
        public Session createSession(IoSession session){
            arenaSession = new ArenaSession(session);
            arenaSession.setStageService(stageService);
            arenaSession.setConfiguration(configuration);
            arenaSession.setConnectService(connectService);
            arenaSession.setPlayerService(playerService);
            arenaSession.setChatService(chatService);
            arenaSession.setBattleService(battleService);
            arenaSession.setArenaService(arenaService);
            arenaSession.setTopListService(topListService);
            arenaSession.setPhizService(phizService);
            return arenaSession;
        }
    }

    class ShutdownHook extends Thread{
        public void run(){
        	modelService.shutdown();
            playerService.saveAll();
            shopService.saveAll();
            campMainService.shutdown();
            activityService.shutdown();
            campBattlefieldService.shutDown();
            houseModel.saveAll();
            farmInstanceModel.saveAll();
            if (Server.iMoneyType == Server.IMONEY_TYPE_CMCC){
            	topListService.saveCmccJILINxml(Server.cmcc_jilin_count, Server.cmcc_jilin_playerid);
            	topListService.saveCmccFUJIANxml(Server.cmcc_fujian_playerid,Server.cmcc_fujian_totalmoney);
            }
            TwelfthLunarService.saveIronChefActivityXml(TwelfthLunarConfig.playerDonateMap);
        }
    }
}
