package peony.game;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.SimpleByteBufferAllocator;
import peony.auction.AuctionService;
import peony.channel.ChannelService;
import peony.db.DBService;
import peony.db.SyncExecutorService;
import peony.depot.DepotService;
import peony.game.actlead.ActLeaderService;
import peony.game.admin.AdminService;
import peony.game.association.AssociationService;
import peony.game.battlefield.FlagBattleFieldVMapManager;
import peony.game.beautyparade.BeautyParadeService;
import peony.game.buff.BuffService;
import peony.game.buff.BuffUtil;
import peony.game.chat.ChatService;
import peony.game.clickexp.ClickExpService;
import peony.game.clientbbs.ClientBbsService;
import peony.game.convoy.NationConvoyService;
import peony.game.drop.DropGroupUtil;
import peony.game.exchange.ExchangeService;
import peony.game.exp.ExpService;
import peony.game.gift.FetchGiftService;
import peony.game.gift.GameChannelService;
import peony.game.gift.GameCityService;
import peony.game.gift.GiftService;
import peony.game.instance.BossScoreService;
import peony.game.instance.NormalVMapManager;
import peony.game.itemenhance.ItemEnhancePersistence;
import peony.game.itemenhance.JewelService;
import peony.game.mail.KillKingService;
import peony.game.mail.MailService;
import peony.game.maintain.HorseNewInstance;
import peony.game.music.MusicService;
import peony.game.nation.CandidateService;
import peony.game.nation.NationService;
import peony.game.party.PartyService;
import peony.game.pk.PkService;
import peony.game.question.QuestionService;
import peony.game.roll.RollService;
import peony.game.weather.WeatherService;
import peony.gatecard.GateCardService;
import peony.marriage.MarriageService;
import peony.marriage.WeddingService;
import peony.mobiphone.TelcoChargeService;
import peony.net.AdminClientSessionService;
import peony.net.AdminDispatchClientSessionService;
import peony.net.DirectClientSessionService;
import peony.net.DispatchClientSessionService;
import peony.net.FlashClientSessionService;
import peony.net.IpdService;
import peony.net.PacketHandler;
import peony.net.ProxyManagingService;
import peony.net.TrustIpService;
import peony.npc.service.ExchangeNpcService;
import peony.npc.service.NpcService;
import peony.npc.service.PloyNpcService;
import peony.produce.ProduceService;
import peony.service.ClearanceSaleService;
import peony.service.EventManager;
import peony.service.ItemTrackService;
import peony.service.LogPlayeActionService;
import peony.service.PacketHandlerService;
import peony.service.QuestRewardService;
import peony.service.Service;
import peony.service.ServiceRegistry;
import peony.service.TorchService;
import peony.service.account.AccountStatService;
import peony.service.account.ChargeInfoService;
import peony.service.account.PiPAccountService;
import peony.service.account.RecordChargeService;
import peony.service.account.adapter.AppStoreService;
import peony.service.account.cmcc.CmccAccountService;
import peony.service.accountbinding.AccountBindingService;
import peony.service.activity.ActivityService;
import peony.service.cards.CardService;
import peony.service.duel.DuelService;
import peony.service.enhance.EnhanceService;
import peony.service.expansionbattle.ExpansionService;
import peony.service.fame.FameService;
import peony.service.friend.RelationService;
import peony.service.jetty.JettyService;
import peony.service.levellimit.LevelLimitService;
import peony.service.nationDayActivity.NationDayService;
import peony.service.player.ActorCacheService;
import peony.service.player.KillPlayerService;
import peony.service.player.PlayerService;
import peony.service.shop.ShopService;
import peony.service.sleepycat.SleepyCatService;
import peony.service.stat.RealtimeStatService;
import peony.service.stat.StatService;
import peony.service.tong.TongService;
import peony.service.tong.apply.TongBattleApplyService;
import peony.service.tong.battle.TongBattleVMapManager;
import peony.service.towerdefend.TowerDefendService;
import peony.service.version.ModelService;
import peony.service.version.VersionService;
import peony.service.worldmap.WorldMapService;
import peony.stat.StatisticsService;
import peony.teleport.service.TeleportService;
import peony.util.StringUtil;
import peony.vm.ASMGameVM;
import peony.vm.ASMQuestUtil;
import peony.vtc.charge.VtcCardChargeService;

public class Server implements Runnable{
	
	private static final Logger log = Logger.getLogger(Server.class);
	
	public static Server server;
	
	// 是否是一个只为了解析玩家数据而启动的”假“服务器
	public boolean isFake = false;

	IoAcceptor acceptor;
	World world;
	ServiceRegistry serviceRegistry;
	EventManager eventManager;
	public SyncRunner syncRunner = new SyncRunner();
	
	XMLConfiguration config;
	
	public int maxPlayer = 5000;
	long preTime;
	long currentTime;
	
	long CYCLE_TIME = 80;
	volatile public boolean running = true;
	
    public static String iMoneyChar = "i";
    public static String iMoneyString = "i币";
    public static String iMoneyStoreString = "i币卖场";

    public static final int IMONEY_TYPE_PIP = 0;
    public static final int IMONEY_TYPE_CMCC = 1;
    public static final int IMONEY_TYPE_QQ = 2;
    public static int iMoneyType = IMONEY_TYPE_PIP;
	
    public String gameCode = "";
    
    public String chinarunhttp = "";
    
    public String cheat = "timeismoney@@!!";
    
    public static final String REVISION_TYPE_PIP = "PIP";
    public static final String REVISION_TYPE_CMCC = "CMCC";
    public static final String REVISION_TYPE_TEL = "CHINATEL";
    public static final String REVISION_TYPE_TW = "TAIWAN";
    public static final String REVISION_TYPE_VN = "VN";
    public static final String REVISION_TYPE_KO = "KO";
    public static final String REVISION_TYPE_JAPAN = "JAPAN";
    public String revision = REVISION_TYPE_PIP;
    
    public String billingURL = "http://218.206.80.185:8102/";
    
    public float expRatio = 1.0f;
    
    public float moneyRatio = 1.0f;
    
    public float creditRatio = 1.0f;
    
    public float onlineExpRatio = 1.0f;
    
    public float agentHorseExp = 1.0f;
    
    public static String[] options;
    
    public static boolean supportRealtimeStatistics;
    
    public Thread mainLoop;
    
    public ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(20); 
    
	public static void main(String[] args) throws Exception{
		options = args;
		new Server();
		for(String s:args){
			System.out.println("option:"+s);
		}
		if (!containsOption("hack")) {
			server.launch();
		}
		if(containsOption("horse")){
			HorseNewInstance hni = new HorseNewInstance();
			int start = 0;
			if(options.length>=3){
				start = Integer.parseInt(options[2]);
			}
			int step = 10;
			if(options.length>=4){
				step = Integer.parseInt(options[3]);
			}
			hni.launch(start,step);
		}
		log.info("World Started");
	}
	
	public static boolean containsOption(String s){
		for(String opt:options){
			if(opt.equals(s))
				return true;
		}
		return false;
	}
	
	/**
	 * 注意：这个构造函数用来创建一个“假”的Server，以便可以利用服务器的数据解析函数。这个“假”Server只启动数据解析相关的必要的服务。
	 * @param dataDir 数据目录
	 * @throws Exception
	 */
	public Server(String dataDir) throws Exception {
		server = this;
		isFake = true;
		serviceRegistry = new ServiceRegistry();
		eventManager = new EventManager();
		Time.init();
		
		// 下面注册装备附属对象的解析器
		HorsePersistence horsePersist = new HorsePersistence();
		PersistenceManager.registerMarshaller(horsePersist);
		PersistenceManager.registerSerializer(horsePersist);
		ItemEnhancePersistence itemEnhPersist = new ItemEnhancePersistence();
        PersistenceManager.registerMarshaller(itemEnhPersist);
        PersistenceManager.registerSerializer(itemEnhPersist);
        IMoneyCardPersistence imoneyCardPersist = new IMoneyCardPersistence();
        PersistenceManager.registerMarshaller(imoneyCardPersist);
        PersistenceManager.registerSerializer(imoneyCardPersist);
		
        // 数据服务加载数据目录，是必须的
		Service service = new DataService(new File(dataDir), revision);
		serviceRegistry.addService(service);
		
		// 下面是一些字典工具类
        BuffUtil.initBuffs();
		HorseUtil.load();
		TitleUtil.load();
		SkillUtil.initSkills();
		ItemUtil.load();
	}
	
	public Server() throws Exception {
		server = this;
		config = new XMLConfiguration("peony.xml");
		gameCode = config.getString("gamecode");
		cheat = config.getString("cheat");
		chinarunhttp = config.getString("chinarun");
		revision = config.getString("revision");
		if (revision == null) {
			revision = REVISION_TYPE_VN;
		}
		if (config.getString("billing") != null) {
			billingURL = config.getString("billing");
		}
		supportRealtimeStatistics = new File("promptbubble.properties").exists();
		
		serviceRegistry = new ServiceRegistry();
		eventManager = new EventManager();
		Time.init();
		HorsePersistence horsePersist = new HorsePersistence();
		PersistenceManager.registerMarshaller(horsePersist);
		PersistenceManager.registerSerializer(horsePersist);
		ItemEnhancePersistence itemEnhPersist = new ItemEnhancePersistence();
        PersistenceManager.registerMarshaller(itemEnhPersist);
        PersistenceManager.registerSerializer(itemEnhPersist);
        IMoneyCardPersistence imoneyCardPersist = new IMoneyCardPersistence();
        PersistenceManager.registerMarshaller(imoneyCardPersist);
        PersistenceManager.registerSerializer(imoneyCardPersist);
		
		Service service = new DataService(new File(config.getString("datadir")),revision);
		serviceRegistry.addService(service);
//		createService(FileService.class);
		ObjectAccessor.startRefreshThread();
        BuffUtil.initBuffs();
		HorseUtil.load();
		TitleUtil.load();
		SkillUtil.initSkills();
		ItemUtil.load();
		DropGroupUtil.load();
		ASMQuestUtil.load();
		VMapUtil.load();
		List<String> names = VMapUtil.getAllNpcNames();
		String dir = System.getProperty("user.dir");
		StringUtil.init(new File(dir, "invalidname.txt"), new File(dir, "keywords.xml"), names);
		world = new World();
		world.startup();
		serviceRegistry.addService(world);
		
		serviceRegistry.addService(new SleepyCatService(config));
		createService(DBService.class);
		createService(SyncExecutorService.class);
		createService(ActorCacheService.class);
		createService(CandidateService.class);//创建CandidateService
		createService(NationService.class);//创建NationService
		createService(LevelLimitService.class);
		createService(ExpService.class);
		createService(WorldMapService.class);
		createService(BossScoreService.class);
		//serviceRegistry.addService(auctionService);
		createService(ChannelService.class);
		createService(ChatService.class);
		
		// 创建副本地图管理器
		NormalVMapManager normalVMapManager = new NormalVMapManager(world);
		serviceRegistry.addService(normalVMapManager);
		normalVMapManager.startup();
		
		// 创建夺旗战场地图管理器
		FlagBattleFieldVMapManager flagBattleFieldVMapManager = new FlagBattleFieldVMapManager(world);
		flagBattleFieldVMapManager.startup();
		serviceRegistry.addService(flagBattleFieldVMapManager);
		
		// 创建普通地图管理器（分阵营）
		new NoInstanceVMapManager(world, GameObject.FACTION_NEUTRAL);
		new NoInstanceVMapManager(world, GameObject.FACTION_WEI);
		new NoInstanceVMapManager(world, GameObject.FACTION_SHU);
		new NoInstanceVMapManager(world, GameObject.FACTION_WU);
		
		// 载入所有地图，并创建VMap
		world.loadStages();
		
		createService(PkService.class);
		createService(RollService.class);
		createService(PartyService.class);
		createService(PacketHandlerService.class);
		
		createService(RelationService.class);
		createService(TongService.class);
		createService(ShopService.class);
		createService(MailService.class);
		createService(BuffService.class);
		createService(VersionService.class);
		createService(AdminService.class);
//		createService(PloyMapVMapManager.class);
		createService(ExchangeService.class);
		createService(PropertyService.class);
		createService(StatService.class);
		createService(AuctionService.class);//创建AuctionService
		createService(MarriageService.class);//创建MarriageService
		createService(ProduceService.class);//创建ProduceService
		createService(TeleportService.class);//创建TeleportService
		createService(DepotService.class);//创建DepotService
		createService(QuestionService.class);//创建QuestionService
		
		createService(JewelService.class);
		createService(ModelService.class);

		createService(StatisticsService.class);
		createService(GiftService.class);
//		createService(ChinaJoyService.class);
		createService(AccountBindingService.class);
		createService(KillKingService.class);
		createService(WeatherService.class);
		createService(NationConvoyService.class);
		createService(AppStoreService.class);
		createService(ClickExpService.class);
		
		createService(GameChannelService.class);
		createService(GameCityService.class);
		createService(ClearanceSaleService.class);
		createService(ActivityService.class);
		createService(ClientBbsService.class);
		createService(KillPlayerService.class);
		createService(ExpansionService.class);
		createService(WeddingService.class);
		createService(NationDayService.class);
		createService(CardService.class);
		
		service = new TrustIpService(config.getString("trustip"));
		service.startup();
		serviceRegistry.addService(service);

		// 根据认证服务器类型创建不同的AccountService
		String accountType = config.configurationAt("account").getString("type");
		if(Server.server.REVISION_TYPE_CMCC.equals(Server.server.revision)|| Server.server.REVISION_TYPE_TEL.equals(Server.server.revision)) {
			service = new CmccAccountService(config.configurationAt("account"));
		} else {
			service = new PiPAccountService(config.configurationAt("account"));
			if (Server.server.REVISION_TYPE_PIP.equals(Server.server.revision)) {
				if (config.configurationsAt("slaveaccount").size() > 0) {
					CmccAccountService slaveAccountService = new CmccAccountService(
							config.configurationAt("slaveaccount"));
					slaveAccountService.startup();
					serviceRegistry.setSlaveAccountService(slaveAccountService);
				}
			}
		}
		service.startup();
		serviceRegistry.addService(service);
		
		Configuration jettyConfig = null;
		try{
			jettyConfig = config.configurationAt("jetty");
		}catch(Exception ex){
			
		}
		
		service = new JettyService(jettyConfig);
		service.startup();
		serviceRegistry.addService(service);
		
		createService(PlayerService.class);
		createService(MusicService.class);
		Time.addDayListener(new ActivePowerDayListener());
//		Time.addDayListener(Server.server.getServiceRegistry().getChinaJoyService());
		Time.addDayListener(Server.server.getServiceRegistry().getStatService());
		createService(RealtimeStatService.class);
		Time.addDayListener(Server.server.getServiceRegistry().getRealtimeStatService());
		createService(AccountStatService.class);
		
		ObjectAccessor.startAutoSaveThread();
		createService(NpcService.class);
		createService(PloyNpcService.class);
		createService(ExchangeNpcService.class);
		createService(TongBattleApplyService.class);
		createService(TongBattleVMapManager.class);
		createService(BeautyParadeService.class);
		createService(RecordChargeService.class);
		createService(FameService.class);
		createService(DuelService.class);
		createService(TorchService.class);
		createService(TowerDefendService.class);
		createService(ActLeaderService.class);
		createService(EnhanceService.class);
		createService(AssociationService.class);
		createService(ChargeInfoService.class);
		createService(FetchGiftService.class);
		createService(AccountDepotService.class);
		createService(LogPlayeActionService.class);
		createService(GateCardService.class);
		createService(QuestRewardService.class);
		createService(TelcoChargeService.class);
		createService(ItemTrackService.class);
		createService(VtcCardChargeService.class);
		// 测试掉落组
//		testDrop(3162115, 53, 1000);
//		testDrop(790564, 30, 1000);
//
//		testDrop(659482, 22, 1000);
//        testDrop(659482, 40, 1000);
//        
//        testDrop(2232373, 30, 1000);
//        testDrop(2232373, 50, 1000);
//        
//        testDrop(2756719, 40, 1000);
//        testDrop(2756719, 60, 1000);
//        
//        testDrop(3342404, 50, 1000);
//        testDrop(3342404, 70, 1000);
//        
//        testDrop(3604519, 60, 1000);
//        testDrop(3604519, 70, 1000);
		// 测试所有任务脚本
//		for (GameQuest quest : ASMQuestUtil.getAllQuests()) {
//			quest.getClientETF();
//		}
	}
	
	private void testDrop(int monsterID, int playerLevel, int times) {
	    long time = System.currentTimeMillis();
	    Creature c = findCreature(monsterID);
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i = 0; i < times; i++) {
            Gain[] gains = new Gain[1];
            Player p = new Player();
            p.level = playerLevel;
            p.asmVm = new ASMGameVM(p);
            gains[0] = new Gain(p, null, true);
            c.fall.gain(Unit.RND, gains);
            for (GainItem gi : gains[0].getGainItems()) {
                int id = gi.item.template.id;
                if (map.containsKey(id)) {
                    map.put(id, map.get(id) + 1);
                } else {
                    map.put(id, 1);
                }
            }
        }
        System.out.println("怪物级别" + c.level + ", 玩家级别" + playerLevel);
        System.out.println("用时: " + (System.currentTimeMillis() - time));
        Iterator<Integer> itor = map.keySet().iterator();
        while (itor.hasNext()) {
            int id = itor.next();
            ItemTemplate it = ObjectAccessor.getItemTemplate(id);
            System.out.println(id + "\t" + it.name + "\t" + map.get(id));
        }
	}
	
	private Creature findCreature(int id) {
	    for (GameObject gobj : ObjectAccessor.instanceid2objects.values()) {
            if (!(gobj instanceof Creature)) {
                continue;
            }
            Creature c = (Creature)gobj;
            if (c.id == id) {
                return c;
            }
        }
	    return null;
	}
	
	private Service createService(Class cls) throws Exception {
		Service service = (Service)cls.newInstance();
		service.startup();
		serviceRegistry.addService(service);
		return service;
	}
	
	
	public void launch() throws Exception{
		ByteBuffer.setUseDirectBuffers(false);
		ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
//		accountService.connect();
		buildConnections(config.configurationsAt("connections.connection"));
		ProxyManagingService pms = new ProxyManagingService(config);
		pms.startup();
		serviceRegistry.addService(pms);
		createService(IpdService.class);
//		acceptor = new SocketAcceptor();
//		SocketAcceptorConfig cfg = new SocketAcceptorConfig();
//        cfg.getFilterChain().addLast( "codec", new ProtocolCodecFilter(MinaUAEncoder.class,MinaUADecoder.class));
//        acceptor.bind( new InetSocketAddress(PORT), new WorldSessionHandler(), cfg);
		mainLoop = new Thread(this);
		mainLoop.setPriority(Thread.MAX_PRIORITY - 1);
		mainLoop.start();
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
	}
	
	public void reloadConfig() throws Exception {
	    config = new XMLConfiguration("peony.xml");
	    ProxyManagingService pms = (ProxyManagingService)
                Server.server.getServiceRegistry().getService(ProxyManagingService.class);
        pms.reload(config);
        IpdService ipds = (IpdService)
            Server.server.getServiceRegistry().getService(IpdService.class);
        ipds.reload();
        cheat = config.getString("cheat");
	}
	
	public void buildConnections(List<SubnodeConfiguration> l) throws Exception{
		for(Configuration cfg:l){
			try {
				if("direct".equals(cfg.getString("type"))){
					PacketHandler handler = getServiceRegistry().getPacketHandlerService().getPlayerHandler();
					DirectClientSessionService service = new DirectClientSessionService(cfg,handler);
					service.startup();
					serviceRegistry.addService(service);
				} else if("dispatch".equals(cfg.getString("type"))){
					PacketHandler handler = getServiceRegistry().getPacketHandlerService().getPlayerHandler();
					DispatchClientSessionService service = new DispatchClientSessionService(cfg,handler);
					service.startup();
					serviceRegistry.addService(service);
				} else if("admin".equals(cfg.getString("type"))){
					PacketHandler handler = getServiceRegistry().getPacketHandlerService().getAdminHandler();
					AdminClientSessionService service = new AdminClientSessionService(cfg,handler);
					service.startup();
					serviceRegistry.addService(service);
				} else if ("admindispatch".equals(cfg.getString("type"))) {
					PacketHandler handler = getServiceRegistry().getPacketHandlerService().getAdminHandler();
					AdminDispatchClientSessionService service = new AdminDispatchClientSessionService(cfg,handler);
					service.startup();
					serviceRegistry.addService(service);
				} else if("flash".equals(cfg.getString("type"))){
					PacketHandler handler = getServiceRegistry().getPacketHandlerService().getPlayerHandler();
					FlashClientSessionService service = new FlashClientSessionService(cfg,handler);
					service.startup();
					serviceRegistry.addService(service);
				}
			} catch (Exception e) {
				log.error(e, e);
			}
		}
	}
	
	public void run() {  //main loop
		long preTime = Time.currTime;
		while (running) {
			Time.update();
			currentTime = Time.currTime;
			int diff = (int) (currentTime - preTime);
			if(diff>100){
				log.warn("Cycle too long:" + diff);
			}
//			CycleStatistics stat = new CycleStatistics();
			try {
				long t = System.nanoTime();
				world.update(diff);
				long t1 = System.nanoTime();
//				stat.cycleWorldTime = t1 - t;
				eventManager.dispatchEvents();
				long t2 = System.nanoTime();
//				stat.cycleEventTime = t2 - t1;
				server.getServiceRegistry().getPkService().update();
				long t3 = System.nanoTime();
//				stat.cyclePkTime = t3 - t2;
				server.getServiceRegistry().getRollService().update();
				long t4 = System.nanoTime();
//				stat.cycleRollTime = t4 - t3;
//				stat.cycleTime = t4 - t;
				server.serviceRegistry.getExchangeService().update();
//				server.getServiceRegistry().getStatisticsService().addCycleStatistics(stat);
//				server.getServiceRegistry().getFlagBattleFieldVMapManager().update(diff);
				server.getServiceRegistry().getCandidateService().update(diff);
				server.getServiceRegistry().getTongService().update();
				server.getServiceRegistry().getRealtimeStatService().update(diff);
				server.getServiceRegistry().getActivityService().update(diff);
				server.getServiceRegistry().getAssociationService().update();
				syncRunner.update();
			} catch (Throwable e1) {
				log.error(e1,e1);
			}
			preTime = currentTime;
			long t = Time.update();
			if(t<CYCLE_TIME){
				try {
					Thread.sleep(CYCLE_TIME-t);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Time.tick++;
		}
		
	}
	
	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}
	
	public EventManager getEventManager() {
		return eventManager;
	}
	
	public World getWorld() {
		return world;
	}
	
	public XMLConfiguration getConfig() {
		return config;
	}
	
	public String getName() {
		return config.getString("name");
	}

    class ShutdownHook extends Thread{
        @Override
		public void run(){
        	running = false;
        	try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	serviceRegistry.shutdown();
        }
    }
}
