package com.pip.itimes.server.world;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.pip.gtl.etf.ETFFile;
import com.pip.gtl.etf.ETFUtil;
import com.pip.itimes.server.camp.CampLoader;
import com.pip.itimes.server.gift.ExchangeDataLoader;
import com.pip.itimes.server.gift.GiftGroupLoader;
import com.pip.itimes.server.gift.OnlyGiftGruopLoader;
import com.pip.itimes.server.stage.AbilitiesLoader;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.stage.ActivationCode;
import com.pip.itimes.server.stage.Animate;
import com.pip.itimes.server.stage.AnniversaryEnhance;
import com.pip.itimes.server.stage.BloodStoreGroupLoader;
import com.pip.itimes.server.stage.BossLocalTips;
import com.pip.itimes.server.stage.BossRushLoader;
import com.pip.itimes.server.stage.BossTips;
import com.pip.itimes.server.stage.CStoreGroupLoader;
import com.pip.itimes.server.stage.CampBuffLoader;
import com.pip.itimes.server.stage.ChatFavoriteLoader;
import com.pip.itimes.server.stage.CreditShop;
import com.pip.itimes.server.stage.DiamondMosaic;
import com.pip.itimes.server.stage.Diamonds;
import com.pip.itimes.server.stage.DiscountShopLoader;
import com.pip.itimes.server.stage.DownloadManager;
import com.pip.itimes.server.stage.DownloadPointShopLoader;
import com.pip.itimes.server.stage.DropGroupLoader;
import com.pip.itimes.server.stage.Enhance;
import com.pip.itimes.server.stage.EvolutionLoader;
import com.pip.itimes.server.stage.HousePart;
import com.pip.itimes.server.stage.HouseTemplate;
import com.pip.itimes.server.stage.HouseWaiter;
import com.pip.itimes.server.stage.Houses;
import com.pip.itimes.server.stage.IStoreGroupLoader;
import com.pip.itimes.server.stage.IStoreGroupLoader2;
import com.pip.itimes.server.stage.IbuyGift;
import com.pip.itimes.server.stage.InPkgFile;
import com.pip.itimes.server.stage.Instanceadd;
import com.pip.itimes.server.stage.ItemLoader;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.LevelTips;
import com.pip.itimes.server.stage.LoopTasks;
import com.pip.itimes.server.stage.MagicPosLoader;
import com.pip.itimes.server.stage.MaterialTypeLoader;
import com.pip.itimes.server.stage.Monster;
import com.pip.itimes.server.stage.MonsterConstants;
import com.pip.itimes.server.stage.MonsterGroup;
import com.pip.itimes.server.stage.Npc;
import com.pip.itimes.server.stage.Pet;
import com.pip.itimes.server.stage.PetColorLoader;
import com.pip.itimes.server.stage.PetEnhance;
import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.stage.PngResourceData;
import com.pip.itimes.server.stage.PngResources;
import com.pip.itimes.server.stage.PrescriptionsLoader;
import com.pip.itimes.server.stage.RandomMessageLoader;
import com.pip.itimes.server.stage.RecipesLoader;
import com.pip.itimes.server.stage.Resource;
import com.pip.itimes.server.stage.RoleFaceLoader;
import com.pip.itimes.server.stage.Scene;
import com.pip.itimes.server.stage.ShoutLoader;
import com.pip.itimes.server.stage.Stage;
import com.pip.itimes.server.stage.StageBuilder;
import com.pip.itimes.server.stage.StageLoader;
import com.pip.itimes.server.stage.StoreGroupLoader;
import com.pip.itimes.server.stage.TaskAwardLoader;
import com.pip.itimes.server.stage.TaskDefinitionLoader;
import com.pip.itimes.server.stage.TaskNpcLoader;
import com.pip.itimes.server.stage.TaskService;
import com.pip.itimes.server.stage.TipsLoader;
import com.pip.itimes.server.stage.TongShopLoader;
import com.pip.itimes.server.stage.TrainGiftMessageLoader;
import com.pip.itimes.server.stage.TransferLoader;
import com.pip.itimes.server.stage.TwelfthLunarConfig;
import com.pip.itimes.server.stage.TwelfthLunarLoader;
import com.pip.itimes.server.stage.TwelfthLunarShowInfo;
import com.pip.itimes.server.stage.WorldMap;
import com.pip.itimes.server.suit.PointSuitLoader;
import com.pip.itimes.server.suit.SuitLoader;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.ItemGroup.BossBattleTop;
import com.pip.itimes.server.world.ItemGroup.ItemGroupLoader;
import com.pip.itimes.server.world.ItemGroup.ItemTop;
import com.pip.itimes.server.world.ItemGroup.MagicPositionTop;
import com.pip.itimes.server.world.ItemGroup.PetDevelopTop;
import com.pip.itimes.server.world.ItemGroup.PetEvolutionTop;
import com.pip.itimes.server.world.ItemGroup.TrainLevelTop;
import com.pip.itimes.server.world.awardbox.AwardBoxConfig;
import com.pip.itimes.server.world.battle.BattleIntervene;
import com.pip.itimes.server.world.battle.Skill;
import com.pip.itimes.server.world.book.BookLoader;
import com.pip.itimes.server.world.boss.BossDefineLoader;
import com.pip.itimes.server.world.chr.ChristmasLoader;
import com.pip.itimes.server.world.equmodle.EquModleConfig;
import com.pip.itimes.server.world.farm.FarmConfig;
import com.pip.itimes.server.world.fee.ChargePlan;
import com.pip.itimes.server.world.fee.FeePlan;
import com.pip.itimes.server.world.game.CampBattlefield;
import com.pip.itimes.server.world.game.CampBattlefieldAward;
import com.pip.itimes.server.world.game.CampBattlefieldConfig;
import com.pip.itimes.server.world.game.CampbattlefieldWarriorPlaces;
import com.pip.itimes.server.world.game.WorldService;
import com.pip.itimes.server.world.love.LoveLoader;
import com.pip.itimes.server.world.love7.Love7Loader;
import com.pip.itimes.server.world.lyrics.LoveLyricsLoader;
import com.pip.itimes.server.world.lyrics.LyricsLoader;
import com.pip.itimes.server.world.lyricsSystem.LyricsSystemLoader;
import com.pip.itimes.server.world.message.WelcomeMessageLoader;
import com.pip.itimes.server.world.noahsark.noahsarkLoader;
import com.pip.itimes.server.world.question.QuestionLoader;
import com.pip.itimes.server.world.rabbitRace.RabbitRaceLoader;
import com.pip.itimes.server.world.rabbitRace.RabbitRaceTop;
import com.pip.itimes.server.world.refresh.IRefreshCallback;
import com.pip.itimes.server.world.refresh.IRefreshObject;
import com.pip.itimes.server.world.refresh.Lock;
import com.pip.itimes.server.world.refresh.LockException;
import com.pip.itimes.server.world.refresh.MGPool;
import com.pip.itimes.server.world.refresh.NpcPool;
import com.pip.itimes.server.world.refresh.ResourcePool;
import com.pip.itimes.server.world.riddles.RiddlesLoader;
import com.pip.itimes.server.world.riddles.RiddlesLoader2;
import com.pip.itimes.server.world.sports.SportSchedule;
import com.pip.itimes.server.world.sports.SportsTimer;
import com.pip.itimes.server.world.suggest.SuggestLoader;
import com.pip.itimes.server.world.taskHelp.TaskHelpManager;
import com.pip.itimes.server.world.taskRequest.TaskRequestLoader;
import com.pip.itimes.server.world.top.GemTop;
import com.pip.itimes.server.world.unline.UnlineExpLoader;
import com.pip.itimes.server.world.worldboss.WorldBossLoader;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class StageService {

    private static final Logger log = Logger.getLogger(StageService.class);

    private File pkgDir;

    private PngResources npcPngs;
    private PngResources mgPngs;
    private PngResources monsterPngs;
    
    private Animate roleAnimate; //人物形象动画
    private Animate attachRoleAnimate; //关卡附件人物形象动画
    private Animate UIAnimate; //ui附加动画扩展

//    private RefreshPool npcPool;
//    private RefreshPool mgPool;
//    private RefreshPool resourcePool;
    private PoolCallBack callback = new PoolCallBack();

    private NpcPool fNpcPool = new NpcPool();
    private MGPool fMgPool = new MGPool();
    private ResourcePool fResourcePool = new ResourcePool();

    private ConnectService connectService = null;
    private PhoneService phoneService = null;

    private Map stages = new HashMap();
    private Map pkgems = new HashMap();
    private Map pkgss = new HashMap();
    private Map name2scenes = new HashMap();
    private Set teamForbidens = new HashSet();

    private ChatFavoriteLoader chatFavoriteLoader;
    private AbilitiesLoader abilitiesLoader;
    private ItemLoader itemLoader;
    private RecipesLoader recipesLoader;
    private PrescriptionsLoader prescriptionsLoader;
    private TaskNpcLoader taskNpcLoader;
    private TaskAwardLoader taskAwardLoader;
    private TaskDefinitionLoader taskDefinitionLoader;
    private MaterialTypeLoader materialTypeLoader;
    private TaskService taskService = null;
    private StageBuilder stageBuilder = new StageBuilder();
    private StoreGroupLoader storeGroupLoader;
    private	CStoreGroupLoader cstoreGroupLoader;
    private BloodStoreGroupLoader bloodstoreGroupLoader;
    private IStoreGroupLoader istoreGroupLoader;
    private IStoreGroupLoader2 istoreGroupLoader2;
    private TongShopLoader tongShopLoader;
    private DiscountShopLoader discountShopLoader;
    private ItemGroupLoader itemGroupLoader;
    private DropGroupLoader dropGroupLoader;
    private TransferLoader transferLoader;
    private TipsLoader tipsLoader;
    private RoleFaceLoader faceLoader;
    private SuitLoader suitLoader;
    private PointSuitLoader pointSuitLoader;
    private QuestionLoader questionLoader;
    private BossDefineLoader bossDefineLoader;
    //private RandomQuestionManager randomQuestionManager;
    private SuggestLoader suggestLoader;
    private GiftGroupLoader giftGroupLoader;
    private OnlyGiftGruopLoader onlyGiftGruopLoader;
    private CampLoader campLoader;
    private ChristmasLoader christmasLoader;
    private BookLoader bookLoader;
    private FarmConfig farmLoader;
    private AwardBoxConfig awardBoxLoader;
    private TaskRequestLoader taskRequestLoader;
    private LyricsLoader lyricsLoader;
    private LoveLyricsLoader loveLyricsLoader;
    private RabbitRaceLoader rabbitRaceLoader;
    private LyricsSystemLoader lyricsSystemLoader;
    private RiddlesLoader riddlesLoader;
    private RiddlesLoader2 riddlesLoader2;
    private WelcomeMessageLoader welecomMessageLoader;
    private TrainGiftMessageLoader TrainGiftLoader;
    private MagicPosLoader magicPosLoader;
    private LoveLoader loveLoader;
    private Love7Loader love7Loader;
    private UnlineExpLoader unlineExpLoader;
    private CampBuffLoader campBuffLoader;
    private ShoutLoader shoutLoader;
    private TwelfthLunarLoader twelfthLunarLoader;
    private BattleIntervene battleInterveneLoader;
    private PetColorLoader petColorLoader;
    private VoteService voteService;
    private ExchangeDataLoader exchangeDataLoader;
    private TaskHelpManager taskHelpManager;
    private BossRushLoader bossRushLoader;
    private RandomMessageLoader randomMessageLoader;
    private DownloadPointShopLoader downloadPointShopLoader;
    private WorldBossLoader worldBossLoader;
    private EvolutionLoader evolutionLoader;
    private noahsarkLoader noahsarkLoader;
    
    public void setVoteService(VoteService voteService) {
		this.voteService = voteService;
	}

	private List instances = new ArrayList();
	
	/**
	 * 定义内置关卡的关卡号
	 */
	private List innerpackagefile = new ArrayList();
	
    private List<TongIslandDef> tongIslandDefs = new ArrayList<TongIslandDef>();

    private WorldService worldService = null;
    //mengjie add
    public static int BBSLEVEL = 0;
    public static int BBSJMONEY = 0;
    //mengjie add end
    
    public boolean reload = false;
    public StageService(File pkgDir) throws Exception{
        this.pkgDir = pkgDir;
        load(pkgDir);
        fNpcPool.setCallback(callback);
        fMgPool.setCallback(callback);
        fResourcePool.setCallback(callback);
        fNpcPool.start();
        fMgPool.start();
        fResourcePool.start();
    }

    public void setWorldService(WorldService worldService){
        this.worldService = worldService;
    }


    public void load(File file) throws Exception{
    	//changePetColor();
        loadInstanceDefinition();

        loadCampBattleInstanceDefinition();
        
        loadImages();

        loadAbilities();

        loadMaterialType();
        loadPetColor();
        //2013年4月1日 因为需要Load带宝石 有镶嵌的装备，将loadEnhances提前
        loadEnhances();
        
        loadItems();

        loadRecipes();
        
        loadPrescription();
        
        loadNpcs();
        
        //怪物世界年落常量 重置年率机率
        loadMonsterConstants();

        loadStages();

        boolean isFake = false;
        
        if(Server.instance != null){
        	isFake = Server.instance.isFake;
        }
        if (!isFake) {
        	loadTasks();
        }

        loadChatFavorites();

        loadCommodityGroups();

        loadDropGroup();

        if (!isFake) {
	        loadChargePlan();
	
	        loadFeePlan();
        }

        loadTransfer();

        loadTips();

        loadBattleFieldTimer();
        
        loadCampBattlefields();

        loadLevelTips();

        loadCStoreGroups();
        
        loadIStoreGroups();
        
        loadIStoreGroups2();
        
        loadTongShopGroups();
        
        loadDiscountShopGroups();
        
        loadDownloadPointShopGroups();
        
        loadBloodsuckerStoreGroups();
        
        loadItemGroup();

        loadFaces();

        loadHouses();

        loadSuits();
//        toTransfer();

        loadQuestions();

        loadSuggests();

        loadBathHouses();

        if (!isFake) {
        	loadDiscount();
        }

        loadSportsTimer();

        loadForbidens();

        loadConsumeCodes();
        
        loadCmccSMSCodes();

        loadTongIslandDef();

        loaditemmsg();

        loadWorldMap();

        loadcreditshop();
        
        loadGiftGroups();
        
        loadinstancesadd();
        
        loadibuygift();
        
        loadLooptasks();
        //jwp add
        loadPetEnhance();
        
        //jwp end
        //cmcc mengjie add
        if (Server.iMoneyType == Server.IMONEY_TYPE_CMCC){
        	loadcmccjilincount();
        	loadcmccfujiancount();
        }
        //只load1次
        
        // 初始化下载文件管理器
        initDownloadManager();
        
        //ui脚本的演示和脚本的关联关系加载
        loadTaskUIHlep();
        
        loadDiamond(pkgDir);
        
        //阵营
        loadCamp();
        
        //圣诞节活动
        loadChristmas();
        
        // 战场胜利后阵营BUFF
        loadCampBuff();
        
        // 喊话活动
        loadShout();
        
        //离线经验
        loadUnlineExp();
        
        // 腊八活动
        loadTwelfthLunar();
        loadIronChefActivityCount();
        
        //记歌词活动
        loadLyrics();
        
        //情歌对唱
        loadLoveLyrics();
        
        //兔子赛跑
        rabbitRace();
        
        //情人节情话
        loadLove();
        
        //指路宝典
        loadBook();
        //元宵猜灯谜活动
        loadRiddles();
        //咏春诗歌大赛
        loadRiddles2();
        
        //任务请求
        loadTaskRequest();
        
        ItemTop.load4File();
        
        //重构装备兑换 mengjie
        loadExchangeData();
        
        //加载援护设置(宠物乱入)
        loadIntervene();
        
        //加载欢渠道欢迎语句
        loadWelcomeMessage();
        
        //加载佣兵数据
        loadMercenaryConstants();
        
        //加载装备模板
        loadEquModle();
        
        //加载点歌系统歌库
        loadLyricsSystem();
        
        //加载七夕情人节
        loadLove7();
        
        //加载庄园相关
        loadFarm();
        
        //加载宝箱相关
        loadAwardBox();
        
        //加载下100层的BOSS
        loadBossRush();
        
        //加载BOSS数据后才能够加载排行榜
        BossBattleTop.loadfile();
        
        //加载聚灵等级排行
        TrainLevelTop.loadfile();
        
        //加载每周使用聚灵点排行
        TrainLevelTop.load2File();
        
        //加载随机喊话
        loadRandomMessage();
    
        //加载聚灵点排行发奖
        loadTrainGift();
        
        //加载封印法阵数据
        loadMagicPos();
        
        //加载各法阵等级排行榜
        MagicPositionTop.loadWaterInfo();
        MagicPositionTop.loadSoilInfo();
        MagicPositionTop.loadFireInfo();
        MagicPositionTop.loadWindInfo();
        MagicPositionTop.loadMindInfo();
        
        //加载宠物培养文件
        loadPetDevelopData();
        //加载宠物培养排行榜
        PetDevelopTop.loadfile();
        
        //世界BOSS加载
        loadWorldBoss(this);
        
        //宝石排行榜加载
        GemTop.loadfile();
        GemTop.init();
        
        //加载宠物进化数据
        loadEvolution();
        
        //诺亚方舟活动
        loadNoahsark();
      //加载宠物进化排行榜
        PetEvolutionTop.loadEvolutionFile();
    }

    /**
     * 加载鉴定
     * @param file
     * @throws Exception
     */
    public void loadDiamond(File file) throws Exception {
    	Diamonds diamonds = new Diamonds();
    	diamonds.loadEnhances(file);
    }
    private void loadTaskUIHlep() throws Exception {
    	taskHelpManager = new TaskHelpManager();
    	taskHelpManager.loadTaskHelp(pkgDir);
    }
    
    private void initDownloadManager() throws Exception {
    	new DownloadManager(pkgDir);
    }
    
    public void loaditemmsg() throws Exception {
    	String stageDirName = pkgDir.getAbsolutePath();
        String instanceDirName = FilenameUtils.concat(stageDirName,"Areas/itemmsg.xml");
        File file = new File(instanceDirName);
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);

        Element root = doc.getRootElement();
		for(Iterator item_i = root.elementIterator("vmessage");item_i.hasNext();){
            Element e = (Element)item_i.next();
            int id = Integer.parseInt(e.attributeValue("itemid"));
            int msgtype = Integer.parseInt(e.attributeValue("msgtype"));
            String message = e.attributeValue("message");
            Items.addMessage(id, msgtype, message);
		}
	}

    private void loadWorldMap() throws Exception{
    	WorldMap worldmap;
        String stageDirName = pkgDir.getAbsolutePath();
        String dirName = FilenameUtils.concat(stageDirName,"Areas/worldmap.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(dirName);
        Element root = doc.getRootElement();
        int Id = 1;
        for(Iterator i=root.elementIterator("world");i.hasNext();){
            Element el = (Element)i.next();
            String name = el.attributeValue("name");
            int[] pointid = parseIntArrayString(el.attributeValue("pointid"));
            int npcid = Integer.parseInt(el.attributeValue("npcid"));
            int levelmin = Integer.parseInt(el.attributeValue("levelmin"));
            int levelmax = Integer.parseInt(el.attributeValue("levelmax"));
            int endmapid = Integer.parseInt(el.attributeValue("endmapid"));
            int endx = Integer.parseInt(el.attributeValue("endx"));
            int endy = Integer.parseInt(el.attributeValue("endy"));
            int x = Integer.parseInt(el.attributeValue("x"));
            int y = Integer.parseInt(el.attributeValue("y"));
            
            int newMapid = Integer.parseInt(el.attributeValue("newmap"));
            int newEndx = Integer.parseInt(el.attributeValue("newx"));
            int newEndy = Integer.parseInt(el.attributeValue("newy"));
            
            String info = el.attributeValue("info");
            for (int j = 0; j < pointid.length; j++){
            	worldmap = new WorldMap(Id,name,pointid[j],npcid,levelmin,levelmax,endmapid,
            			endx,endy,x,y,info, newMapid, newEndx, newEndy);
            	WorldMap.addWorldMap(worldmap);
            }
            Id++;
        }
        WorldMap.addCountNPC(Id-1);
    }
    private void loadLooptasks() throws Exception{
    	LoopTasks loopTasks;
        String stageDirName = pkgDir.getAbsolutePath();
        String dirName = FilenameUtils.concat(stageDirName,"Tasks/looptasks.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(dirName);
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("Task");i.hasNext();){
            Element el = (Element)i.next();
            short taskid = Short.parseShort(el.attributeValue("id"));
            int loops = Integer.parseInt(el.attributeValue("loops"));
            int time = Integer.parseInt(el.attributeValue("time"));
            int groupid = Integer.parseInt(el.attributeValue("group"));
            int campId = Integer.parseInt(el.attributeValue("campId"));
            int preTask = Integer.parseInt(el.attributeValue("preTask"));
            loopTasks = new LoopTasks(taskid,loops,time,groupid, campId);
            loopTasks.setPreTask(preTask);
            LoopTasks.addLoopTasks(loopTasks);
        }
    }
    public void loadcreditshop() throws Exception {
    	String stageDirName = pkgDir.getAbsolutePath();
        String instanceDirName = FilenameUtils.concat(stageDirName,"Items/creditshop.xml");
        File file = new File(instanceDirName);
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        Element root = doc.getRootElement();
        CreditShop creditshop;
        int Id = 0;
		for(Iterator item_i = root.elementIterator("creditshop");item_i.hasNext();){
            Element e = (Element)item_i.next();
            String cron = e.attributeValue("cron");
            int type = Integer.parseInt(e.attributeValue("type"));
            int itemID = Integer.parseInt(e.attributeValue("itemID"));//物品ID
            int groupID = 0;
            if ((e.attributeValue("groupID")!=null) && (e.attributeValue("groupID")!=""))
            	groupID = Integer.parseInt(e.attributeValue("groupID"));//掉落组ID
            int price = Integer.parseInt(e.attributeValue("price"));//起始价格
            String title = e.attributeValue("title");//物品名称
            int time = Integer.parseInt(e.attributeValue("time"));//拍卖时长（分钟）
            int areaId = Integer.parseInt(e.attributeValue("areaId"));//地区id
            String desc = e.attributeValue("desc");//描述
            creditshop = new CreditShop(Id,type,itemID,groupID,price,title,time,areaId,desc,cron);
            CreditShop.addCreditShop(Id,creditshop);
            Id++;
		}
		CreditShop.addCount(Id);
		//CreditShopTimer.cancel();
                if(reload)
                    CreditShopTimer.start();
	}
    
    public void loadinstancesadd() throws Exception {
    	String stageDirName = pkgDir.getAbsolutePath();
        String instanceDirName = FilenameUtils.concat(stageDirName,"Areas/instanceadd.xml");
        File file = new File(instanceDirName);
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        Element root = doc.getRootElement();
        Instanceadd instanceadd;
        int Id = 0;
		for(Iterator item_i = root.elementIterator("instanceadd");item_i.hasNext();){
            Element e = (Element)item_i.next();
            int mapid = Integer.parseInt(e.attributeValue("mapid"));
            int x = Integer.parseInt(e.attributeValue("x"));
            int y = Integer.parseInt(e.attributeValue("y"));
            int type = Integer.parseInt(e.attributeValue("type"));
            String name = e.attributeValue("name");
            int npccount = Integer.parseInt(e.attributeValue("npccount"));
            int instanceid = Integer.parseInt(e.attributeValue("instanceid"));
            instanceadd = new Instanceadd(mapid,x,y,name,type,npccount,instanceid);
            Instanceadd.addInstanceadd(instanceadd);
		}
	}
    private void loadExchangeData() throws Exception{
  	  	String stageDirName = pkgDir.getAbsolutePath();
        String DirName = FilenameUtils.concat(stageDirName,"Items/exchangeequ.xml");
        exchangeDataLoader = new ExchangeDataLoader(new File(DirName));
    }
    
    private void loadibuygift() throws Exception{
    	IbuyGift ibuygift;
    	ActivationCode activationCode;
        String stageDirName = pkgDir.getAbsolutePath();
        String dirName = FilenameUtils.concat(stageDirName,"Items/ibuygift.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(dirName);
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("ibuygift");i.hasNext();){
            Element el = (Element)i.next();
            int id  = Integer.parseInt(el.attributeValue("id"));
            String name = el.attributeValue("name");
            int useitemsid  = Integer.parseInt(el.attributeValue("itemid"));
            int buyitemsid  = Integer.parseInt(el.attributeValue("buyid"));
            int buycount  = Integer.parseInt(el.attributeValue("buycount"));
            int giftitemsid  = Integer.parseInt(el.attributeValue("giftid"));
            int giftcount  = Integer.parseInt(el.attributeValue("giftcount"));
            int useitemslevel  = Integer.parseInt(el.attributeValue("level"));
            int giftrate = Integer.parseInt(el.attributeValue("giftrate"));
            boolean eachone = Boolean.parseBoolean(el.attributeValue("eachone"));
            ibuygift = new IbuyGift(id, name, buyitemsid, buycount, giftitemsid, giftcount, useitemslevel, useitemsid, giftrate, eachone);
            IbuyGift.addIbuyGift(ibuygift);
        }
        for(Iterator i=root.elementIterator("ActivationCode");i.hasNext();){
            Element el = (Element)i.next();
            int id  = Integer.parseInt(el.attributeValue("id"));
            int itemsid  = Integer.parseInt(el.attributeValue("itemid"));
            int count  = Integer.parseInt(el.attributeValue("count"));
            int level  = Integer.parseInt(el.attributeValue("level"));
            activationCode = new ActivationCode(id,itemsid,count,level);
            ActivationCode.addActivationCode(activationCode);
        }
    }
    private void loadcmccjilincount() throws Exception{
    	File file = new File(System.getProperty("user.dir") + "/cmcc_jilin.xml");
    	SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        Element root = doc.getRootElement();
        
        for(Iterator i=root.elementIterator("jilin");i.hasNext();){
            Element el = (Element)i.next();
            String cmcc_userid  = el.attributeValue("id");
            if (!Server.cmcc_jilin_playerid.containsKey(cmcc_userid)){
            	Server.cmcc_jilin_playerid.put(cmcc_userid, 0);
            }
        }
        for(Iterator i=root.elementIterator("count");i.hasNext();){
            Element el = (Element)i.next();
            int count  = Integer.parseInt(el.attributeValue("id"));
            Server.cmcc_jilin_count = count;
        }
    }
    private void loadcmccfujiancount() throws Exception{
    	File file = new File(System.getProperty("user.dir") + "/cmcc_fujian.xml");
    	SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        Element root = doc.getRootElement();
        
        for(Iterator i=root.elementIterator("fujian");i.hasNext();){
            Element el = (Element)i.next();
            String cmcc_userid  = el.attributeValue("userid");
            String money = el.attributeValue("money");
            if (!Server.cmcc_fujian_playerid.containsKey(cmcc_userid)){
            	Server.cmcc_fujian_playerid.put(cmcc_userid, Integer.valueOf(money));
            }
        }
        for(Iterator i=root.elementIterator("totalmoney");i.hasNext();){
            Element el = (Element)i.next();
            int count  = Integer.parseInt(el.attributeValue("money"));
            Server.cmcc_fujian_totalmoney = count;
        }
    }
    //mengjie add end

	public TongIslandDef[] getTongIslandDefs(){
        TongIslandDef[] ret = new TongIslandDef[tongIslandDefs.size()];
        tongIslandDefs.toArray(ret);
        return ret;
    }

    private void loadTongIslandDef() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String dirName = FilenameUtils.concat(stageDirName,"Areas/tongisland.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(dirName);
        Element root = doc.getRootElement();
        tongIslandDefs.clear();
        for(Iterator i=root.elementIterator("island");i.hasNext();){
            Element el = (Element)i.next();
            int id = Integer.parseInt(el.attributeValue("id"));
            String[] s = el.attributeValue("maps").split(",");
            short[] mapIds = new short[s.length];
            for(int j=0;j<s.length;j++){
                mapIds[j] = Short.parseShort(s[j]);
            }
            int bbsId = Integer.parseInt(el.attributeValue("bbsid"));
            String name = el.attributeValue("name");
            short entrance = Short.parseShort(el.attributeValue("entrance"));
            short entrancex = Short.parseShort(el.attributeValue("entrancex"));
            short entrancey = Short.parseShort(el.attributeValue("entrancey"));
            int dropGroup = Integer.parseInt(el.attributeValue("dropgroup"));
            TongIslandDef def = new TongIslandDef(id,name,mapIds,bbsId,entrance,entrancex,entrancey,dropGroup);
            tongIslandDefs.add(def);
        }
    }

    private void loadEnhances() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String dirName = FilenameUtils.concat(stageDirName,"Items/enhances.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(dirName);
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("enhance");i.hasNext();){
            Element el = (Element)i.next();
            String name = el.attributeValue("name");
            int property = Integer.parseInt(el.attributeValue("property"));
            int[] point = parseIntArrayString(el.attributeValue("point"));
            //int point = Integer.parseInt(el.attributeValue("point"));
            int ratio = Integer.parseInt(el.attributeValue("ratio"));
            //mengjie add
            int quality = Integer.parseInt(el.attributeValue("quality"));
            int itemId = Integer.parseInt(el.attributeValue("itemid"));
            int percentage = Integer.parseInt(el.attributeValue("Percentage"));
            int additional = Integer.parseInt(el.attributeValue("additional"));
            int bottom = Integer.parseInt(el.attributeValue("bottom"));
            Enhance enhance = new Enhance(name,property,point,ratio,itemId,
            		quality,percentage,additional,bottom);
            //mengjie add end
            Enhance.addEnhance(enhance);
        }
        for(Iterator i=root.elementIterator("anniversary");i.hasNext();){
            Element el = (Element)i.next();
            int equItemId = Integer.parseInt(el.attributeValue("equItemId"));
            int newequItemId = Integer.parseInt(el.attributeValue("newequItemId"));
            int count = Integer.parseInt(el.attributeValue("count"));
            int probability = Integer.parseInt(el.attributeValue("probability"));
            int anniversary = Integer.parseInt(el.attributeValue("anniversary"));
            int equipType = Integer.parseInt(el.attributeValue("equiptype"));;
            AnniversaryEnhance anniversaryEnhance = new AnniversaryEnhance(equItemId,newequItemId,count,probability,anniversary,equipType);
            AnniversaryEnhance.addAnniversaryEnhance(anniversaryEnhance);
            //jwp add
            AnniversaryEnhance.addUnhenceYearEquip(anniversaryEnhance);//分解周年装
            //jwp add end
        }
        //添加精炼成功率
        for(Iterator i=root.elementIterator("enhancesucessrate");i.hasNext();){
        	 Element el = (Element)i.next();
        	 int stageId = Integer.parseInt(el.attributeValue("stageId"));
        	 int upPercent = Integer.parseInt(el.attributeValue("upPercent"));
        	 Enhance.addUpEnhancePercent(stageId, upPercent);
        }
        onlyGiftGruopLoader = new OnlyGiftGruopLoader(new File(dirName));
        /*for(Iterator i=root.elementIterator("gift");i.hasNext();){
            Element el = (Element)i.next();
            int equiptype = Integer.parseInt(el.attributeValue("equiptype"));
            int equcount = Integer.parseInt(el.attributeValue("equcount"));
            int giftitemsid = Integer.parseInt(el.attributeValue("giftitemsid"));
            int count = Integer.parseInt(el.attributeValue("count"));
            int anniversary = Integer.parseInt(el.attributeValue("anniversary"));
            AnniversaryEnhance.addAnniversaryYearEquip(equiptype, equcount, anniversary);
            //jwp add end
        }*/
        
        //添加刻字
        Enhance.letteringString.clear();
        for(Iterator i=root.elementIterator("letteringItem");i.hasNext();){
        	Element el = (Element)i.next();
	       	int itemId = Integer.parseInt(el.attributeValue("itemid"));
	       	String itemDesc = el.attributeValue("desc");
	       	
	       	Enhance.addLettering(itemId, itemDesc);
       	 //Enhance.addUpEnhancePercent(stageId, upPercent);
       }
      
       //加载宝石镶嵌类
       DiamondMosaic.clearDiamondMosaicMap();
       for(Iterator i=root.elementIterator("diamondmosaic");i.hasNext();){
    	   	Element el = (Element)i.next();
	       	int itemId = Integer.parseInt(el.attributeValue("itemId"));
	       	byte itemLevel = Byte.parseByte(el.attributeValue("itemlevel"));
	       	byte property = Byte.parseByte(el.attributeValue("property"));
	       	short point = Short.parseShort(el.attributeValue("point"));
	       	boolean canUse = Boolean.parseBoolean(el.attributeValue("user"));
	       	
	       	DiamondMosaic diamondMosaic = new DiamondMosaic(itemId, property, itemLevel, point, canUse);
	       	DiamondMosaic.addDiamondMosaicMap(diamondMosaic);
       }
       
       //记载宝石合成必须物品
       DiamondMosaic.clearDiamondMosaicNeedItemMap();
       for(Iterator i=root.elementIterator("diamondmosaicneeditem");i.hasNext();){
    	   Element el = (Element)i.next();
    	   Integer[] needItem = new Integer[2];
    	   needItem[0] = Integer.parseInt(el.attributeValue("max"));
	       needItem[1] = Integer.parseInt(el.attributeValue("itemId"));
	       DiamondMosaic.addDiamondMosaicNeedItem(needItem);
      }
      
       //记载打孔符
       DiamondMosaic.clearDiamondMosaicRoleNeedItemMap();
       for(Iterator i=root.elementIterator("diamondmosaicneedroleitem");i.hasNext();){
    	   Element el = (Element)i.next();
    	   Integer[] needItem = new Integer[2];
    	   needItem[0] = Integer.parseInt(el.attributeValue("max"));
	       needItem[1] = Integer.parseInt(el.attributeValue("itemId"));
	       DiamondMosaic.addDiamondMosaicRoleNeedItem(needItem);
      }
       
      DiamondMosaic.clearDiamondMosaicThrowItemMap();
      for(Iterator i=root.elementIterator("diamondmosaicneedthrowitem");i.hasNext();){
   	   	   Element el = (Element)i.next();
   	   	   int itemId = Integer.parseInt(el.attributeValue("itemId"));
	       int level = Integer.parseInt(el.attributeValue("level"));
	       DiamondMosaic.addDiamondMosaicThrowNeedItem(itemId, level);
     }
      
       
    }
    private void loadPetEnhance() throws Exception{
    	
    	String stageDirName = pkgDir.getAbsolutePath();
        String dirName = FilenameUtils.concat(stageDirName,"Items/petEnhance.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(dirName);
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("petEnhances");i.hasNext();){
            Element el = (Element)i.next();
            String name = el.attributeValue("name");
            int property = Integer.parseInt(el.attributeValue("property"));
            int point = Integer.parseInt(el.attributeValue("point"));
            int quality = Integer.parseInt(el.attributeValue("quality"));
            int itemId = Integer.parseInt(el.attributeValue("itemid"));
            PetEnhance petEnhance = new PetEnhance(name,property,point,quality,itemId);
            //mengjie add end
            PetEnhance.addPetEnhance(petEnhance);
        }
    }
    

    private void loadConsumeCodes() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String consumeFile = FilenameUtils.concat(stageDirName,"Areas/consumecode.properties");
        File f = new File(consumeFile);
        if(f.exists()){
            Configuration conf = new PropertiesConfiguration(new File(consumeFile));
            Iterator ite = conf.getKeys();
            while(ite.hasNext()){
                String v = (String)ite.next();
                String[] codes = conf.getStringArray(v);
                ConsumeCodes.addConsumeCode(Integer.parseInt(v)*100,codes);
            }
        }
    }
    
    private void loadCmccSMSCodes() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String cmccsmsFile = FilenameUtils.concat(stageDirName,"Areas/cmccsmscode.properties");
        File f = new File(cmccsmsFile);
        if(f.exists()){
            Configuration conf = new PropertiesConfiguration(new File(cmccsmsFile));
            Iterator ite = conf.getKeys();
            while(ite.hasNext()){
                String v = (String)ite.next();
                String[] codes = conf.getStringArray(v);
                CmccSMSCodes.addSMSCode(v, codes);
            }
        }
    }

    private void loadBathHouses() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String housesDir = FilenameUtils.concat(stageDirName,"Areas/bathhouse.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(housesDir);
        Element root = doc.getRootElement();
        BathHouse.clearBathHouses();
        for(Iterator i=root.elementIterator("bathhouse");i.hasNext();){
            Element el = (Element)i.next();
            short mapId = Short.parseShort(el.attributeValue("mapid"));
            int itemId = Integer.parseInt(el.attributeValue("itemid"));
            long time = Integer.parseInt(el.attributeValue("time"));
            boolean isVip = Boolean.parseBoolean(el.attributeValue("vip"));
            int ratio = Integer.parseInt(el.attributeValue("ratio"));
            //mengjie add
            String msg = el.attributeValue("msg");
            short mapid = 0;
            int x= 0;
            int y = 0;
            if(isVip){
                mapid = Short.parseShort(el.attributeValue("destmapid"));
                x = Integer.parseInt(el.attributeValue("destx"));
                y = Integer.parseInt(el.attributeValue("desty"));
            }
            time = time * 60*1000L;
            BathHouse bathHouse = new BathHouse(mapId,itemId,time,isVip,ratio,msg);
            if(isVip){
                bathHouse.setDestMapId(mapid);
                bathHouse.setDestX(x);
                bathHouse.setDestY(y);
            }
            BathHouse.addBathHouse(bathHouse);
        }
    }

    public Set getTeamForbidens(){
        return teamForbidens;
    }


    private void loadForbidens() throws Exception{
        String dirName = FilenameUtils.concat(pkgDir.getAbsolutePath(),
                                              "Areas/bbs.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(dirName);
        Element root = doc.getRootElement();
        Set ids = new HashSet();
        for(Iterator i=root.elementIterator("bbs");i.hasNext();){
            Element node = (Element)i.next();
            int id = Integer.parseInt(node.attributeValue("id"));
            ids.add(new Integer(id));
        }
        BbsService.setForbidenBbs(ids);
        for(Iterator i=root.elementIterator("map");i.hasNext();){
            Element node = (Element)i.next();
            int id = Integer.parseInt(node.attributeValue("id"));
            teamForbidens.add(new Integer(id));
        }
        //mengjie add
        for(Iterator i=root.elementIterator("limit");i.hasNext();){
            Element node = (Element)i.next();
            int level = Integer.parseInt(node.attributeValue("level"));
            BBSLEVEL = level;
            int jmoney = Integer.parseInt(node.attributeValue("jmoney"));
            BBSJMONEY = jmoney;
        }
        //mengjie add end
    }

    private void loadSuits() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String suitDirName = FilenameUtils.concat(stageDirName,"Items/suit.xml");
        suitLoader = new SuitLoader(new File(suitDirName));
        //mengjie add
        stageDirName = pkgDir.getAbsolutePath();
        suitDirName = FilenameUtils.concat(stageDirName,"Items/enhancesuit.xml");
        pointSuitLoader = new PointSuitLoader(new File(suitDirName));
    }
    
    private void loadGiftGroups() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String giftGroupDirName = FilenameUtils.concat(stageDirName,"Items/giftgroups.xml");
        giftGroupLoader = new GiftGroupLoader(new File(giftGroupDirName));
    }

    private void loadQuestions() throws Exception{
//    	String stageDirName = pkgDir.getAbsolutePath();
//    	String questionDirName = FilenameUtils.concat(stageDirName,"Areas/questions.xml");
    	questionLoader = new QuestionLoader(pkgDir);
    	
    	//randomQuestionManager = new RandomQuestionManager();
    	
    }

    private void loadSuggests() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
        String suggestDirName = FilenameUtils.concat(stageDirName,"Areas/suggests.xml");
        suggestLoader = new SuggestLoader(new File(suggestDirName));
    }

    public void reloadBattleFieldTimer() throws Exception{
        loadBattleFieldTimer();
    }
    
    public void reloadCampBattlefieldTimer() throws Exception {
    	loadCampBattlefields();
    }

    private void loadHouses() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String housesDir = FilenameUtils.concat(stageDirName,"Areas/houses.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(housesDir);
        Element root = doc.getRootElement();
        Houses.clear();
        for(Iterator i=root.elementIterator("house");i.hasNext();){
            Element el = (Element)i.next();
            int level = Integer.parseInt(el.attributeValue("level"));
            int price = Integer.parseInt(Utils.getWholeDataPrice(el.attributeValue("price")));
            String consumeCode = el.attributeValue("consumecode");
            int gridSize = Integer.parseInt(el.attributeValue("grid"));
            int dropGroup = Integer.parseInt(el.attributeValue("dropgroup"));
            int waiterPrice = Integer.parseInt(Utils.getWholeDataPrice(el.attributeValue("waiterprice")));
            String waiterConsumeCode = el.attributeValue("waiterconsumecode");
            String desc = el.attributeValue("desc");
            for(Iterator j=el.elementIterator("style");j.hasNext();){
                Element node = (Element)j.next();
                int style = Integer.parseInt(node.attributeValue("id"));
                String styleDesc = node.attributeValue("desc");
                int stylePrice = Integer.parseInt(Utils.getWholeDataPrice(node.attributeValue("price")));
                String styleConsumeCode = node.attributeValue("consumecode");
                int instance = Integer.parseInt(node.attributeValue("instance"));
                int waiterNpcId = Integer.parseInt(node.attributeValue("waiternpcid"), 16);
                HouseTemplate house = new HouseTemplate(instance,level,style,gridSize,price,stylePrice,desc,styleDesc,dropGroup);
                house.setWaiterNpcId(waiterNpcId);
                house.setWaiterPrice(waiterPrice);
                house.setConsumeCode(consumeCode);
                house.setStyleConsumeCode(styleConsumeCode);
                house.setWaiterConsumeCode(waiterConsumeCode);
                int spMapId = 0;
                int spX = 0;
                int spY = 0;
                Element elSp = node.element("sproom");
                if (elSp != null) {
                    spMapId = Integer.parseInt(elSp.attributeValue("mapid"));
                    spX = Integer.parseInt(elSp.attributeValue("x"));
                    spY = Integer.parseInt(elSp.attributeValue("y"));
                }
                house.setSpMapId(spMapId);
                house.setSpX(spX);
                house.setSpY(spY);
                List l = new ArrayList(10);
                for(Iterator k=node.elementIterator("part");k.hasNext();){
                    Element p = (Element)k.next();
                    int id = Integer.parseInt(p.attributeValue("id"));
                    int[] indexes = parseIntArrayString(p.attributeValue("npcindex"));
                    int partPrice = Integer.parseInt(Utils.getWholeDataPrice(p.attributeValue("price")));
                    String partConsumeCode = p.attributeValue("consumecode");
                    String partDesc = p.attributeValue("desc");
                    HousePart part = new HousePart(id,indexes,partPrice,partDesc,partConsumeCode);
                    if(id==-1){
                        house.setDefaultPart(part);
                    }else{
                        l.add(part);
                    }
                }
                HousePart[] parts = new HousePart[l.size()];
                l.toArray(parts);
                house.setParts(parts);
                Houses.addHouseTemplate(house);
            }
        }
        for(Iterator i=root.elementIterator("waiter");i.hasNext();){
            Element el = (Element)i.next();
            int id = Integer.parseInt(el.attributeValue("id"));
            String name = el.attributeValue("name");
            int imageId = Integer.parseInt(el.attributeValue("imageid"));
            HouseWaiter waiter = new HouseWaiter(id,name,imageId);
            Houses.addWaiter(waiter);
        }
    }

    private int[] parseIntArrayString(String s){
        String[] ss = s.split(",");
        int[] ret = new int[ss.length];
        for(int i=0;i<ss.length;i++){
            ret[i] = Integer.parseInt(ss[i]);
        }
        return ret;
    }

    private void loadFaces() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String roleFaceDir = FilenameUtils.concat(stageDirName,"RoleImages/index.xml");
        faceLoader = new RoleFaceLoader(new File(roleFaceDir));
        String animateDir = FilenameUtils.concat(stageDirName, "RoleImages/Animate");
        roleAnimate = new Animate(new File(animateDir));
        String attachRoleAnimateDir= FilenameUtils.concat(stageDirName, "AttachImg/Animate");
        attachRoleAnimate = new Animate(new File(attachRoleAnimateDir));
        
        //扩展的ui动画
//        String UIAnimateDir= FilenameUtils.concat(stageDirName, "ExtendAnimte");
//        UIAnimate = new Animate(new File(UIAnimateDir));
    }

    private void loadLevelTips() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String battlefieldtimerDir = FilenameUtils.concat(stageDirName,"Areas/leveltips.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(battlefieldtimerDir);
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("tip");i.hasNext();){
            Element el = (Element)i.next();
            int worldmsg  = Integer.parseInt(el.attributeValue("worldMsg"));
            int level = Integer.parseInt(el.attributeValue("level"));
            String message = null;
            if(el.attribute("message")!=null)
                message = el.attributeValue("message");
            String shout = null;
            if(el.attribute("shout")!=null)
                shout = el.attributeValue("shout");
            //mengjie add
            List l = new ArrayList();
            List v = new ArrayList();
            List c = new ArrayList();
            for(Iterator j=el.elementIterator("item");j.hasNext();){
            	Element el_ = (Element)j.next();
            	int id = Integer.parseInt(el_.attributeValue("id"));
            	int count = Integer.parseInt(el_.attributeValue("count"));
            	String msglevel = el_.attributeValue("msg");
            	l.add(id);
            	v.add(count);
            	c.add(msglevel);
            }
            if (l.size() >0 ){
            	int[] l_ = new int[l.size()];
            	int[] v_ = new int[v.size()];
            	String[] c_= new String[c.size()];
            	for(int j = 0; j < l.size(); j++){
            		l_[j]= Integer.valueOf(l.get(j).toString()).intValue();
            		v_[j]= Integer.valueOf(v.get(j).toString()).intValue();
            		if (c.get(j) == null){
            			c_[j] = "";
            		}else{
            			c_[j]= c.get(j).toString();
            		}
            	}
            	LevelTips.addTip(level,message,shout,l_,v_,c_,worldmsg);
            }else{
            	LevelTips.addTip(level,message,shout,null,null,null,worldmsg);
            }
        }
    }

    private void loadBattleFieldTimer() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String battlefieldtimerDir = FilenameUtils.concat(stageDirName,"Areas/battlefieldtimer.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(battlefieldtimerDir);
        Element root = doc.getRootElement();
        List l = new ArrayList();
        for(Iterator i=root.elementIterator("battlefield");i.hasNext();){
            Element el = (Element)i.next();
            String cron = el.attributeValue("cron");
            int enter = Integer.parseInt(el.attributeValue("enter"));
            int enterfor = Integer.parseInt(el.attributeValue("enterfor"));
            int end = Integer.parseInt(el.attributeValue("end"));
            String type = el.attributeValue("type");
            BattleFieldSchedule schedule = new BattleFieldSchedule(cron,enter,enterfor,end,type);
            l.add(schedule);
        }
        BattleFieldSchedule[] schedules = new BattleFieldSchedule[l.size()];
        l.toArray(schedules);
//        BattleFieldTimer.cancel();
        BattleFieldTimer.setSchedules(schedules);
        if(reload)
            BattleFieldTimer.start();
    }

    private void loadSportsTimer() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String battlefieldtimerDir = FilenameUtils.concat(stageDirName,"Areas/sports.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(battlefieldtimerDir);
        Element root = doc.getRootElement();
        List l = new ArrayList();
        for(Iterator i=root.elementIterator("sport");i.hasNext();){
            Element el = (Element)i.next();
            String cron = el.attributeValue("cron");
            int start = Integer.parseInt(el.attributeValue("start"));
            int end = Integer.parseInt(el.attributeValue("end"));
            int interval = Integer.parseInt(el.attributeValue("interval"));
            String type = el.attributeValue("type");
            int bbsId = Integer.parseInt(el.attributeValue("bbsid"));
            SportSchedule schedule = new SportSchedule(cron,start,end,interval,type,bbsId);
            l.add(schedule);
        }
        SportSchedule[] schedules = new SportSchedule[l.size()];
        l.toArray(schedules);
//        SportsTimer.cancel();
        SportsTimer.setSchedules(schedules);
        if(reload)
            SportsTimer.start();
    }

    private void loadTips() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String tipsDirName = FilenameUtils.concat(stageDirName,"Areas/tips.txt");
        tipsLoader = new TipsLoader(new File(tipsDirName));
    }

    private void toTransfer() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String transferDirName = FilenameUtils.concat(stageDirName,"Areas/doors.xml");
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("GBK");
        XMLWriter writer = new XMLWriter(new FileOutputStream(new File(transferDirName)),format);
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("doors");
        Iterator ite = stages.values().iterator();
        while(ite.hasNext()){
            Stage stage = (Stage)ite.next();
            Scene[] scene = stage.getScenes();
            for(int i=0;i<scene.length;i++){
                Element node = root.addElement("door");
                node.addAttribute("name",scene[i].getName());
                node.addAttribute("mapid",""+((stage.getId()<<4)|stage.getDefaultMapId()));
                node.addAttribute("x",""+stage.getDefaultX());
                node.addAttribute("y",""+stage.getDefaultY());
            }
        }
        writer.write(doc);
        writer.close();
    }

    private void loadTransfer() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String transferDirName = FilenameUtils.concat(stageDirName,"Areas/doors.xml");
        transferLoader = new TransferLoader(new File(transferDirName));
    }

    private void loadChargePlan() throws Exception{
        String dir = FilenameUtils.concat(System.getProperty("user.dir"),"chargeplans.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(dir);
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("chargeplan");i.hasNext();){
            Element node = (Element)i.next();
            String id = node.attributeValue("id");
            String serviceno = node.attributeValue("serviceno");
            String content = node.attributeValue("content");
            ChargePlan plan = new ChargePlan();
            plan.setId(id);
            plan.setServiceNo(serviceno);
            plan.setContent(content);
            ChargePlan.addChargePlan(plan);
        }
    }

    public void loadFeePlan() throws Exception{
        String dir = FilenameUtils.concat(System.getProperty("user.dir"),"feeplans.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(dir);
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("feeplan");i.hasNext();){
            Element node = (Element)i.next();
            String id = node.attributeValue("id");
            int fee = Integer.parseInt(node.attributeValue("fee"));
            int max = Integer.parseInt(node.attributeValue("max"));
            int beginLevel = Integer.parseInt(node.attributeValue("beginlevel"));
            String content = node.getText();
            FeePlan plan = new FeePlan();
            plan.setId(id);
            plan.setFee(fee);
            plan.setMax(max);
            plan.setBeginLevel(beginLevel);
            plan.setContent(content);
            FeePlan.addFeePlan(plan);
        }
    }

    public void loadDiscount() throws Exception{
        String dir = FilenameUtils.concat(System.getProperty("user.dir"),"discount.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(dir);
        Element root = doc.getRootElement();
        int discount = Integer.parseInt(root.attributeValue("discount"));
        int mdiscount = Integer.parseInt(root.attributeValue("mdiscount"));
        int tongdiscount = Integer.parseInt(root.attributeValue("tongdiscount"));
        int famouscount = Integer.parseInt(root.attributeValue("famouscount"));
        int expaddpercent = Integer.parseInt(root.attributeValue("percent"));
        Discount.DISCOUNT = discount;
        Discount.MDISCOUNT = mdiscount;
        Discount.TONGDISCOUNT = tongdiscount;
        Discount.FAMOUSCOUNT = famouscount;
        Discount.EXPADDPERCENT = expaddpercent;
        //mengjie add
        int FRIENDLIMIT = Integer.parseInt(root.attributeValue("limit"));
        int FRIENDLEVEL1 = Integer.parseInt(root.attributeValue("level1"));
        int FRIENDLEVEL2 = Integer.parseInt(root.attributeValue("level2"));
        int FRIENDLEVEL3 = Integer.parseInt(root.attributeValue("level3"));
        FriendsService.FRIENDLIMIT = FRIENDLIMIT;
        FriendsService.FRIENDLEVEL1 = FRIENDLEVEL1;
        FriendsService.FRIENDLEVEL2 = FRIENDLEVEL2;
        FriendsService.FRIENDLEVEL3 = FRIENDLEVEL3;
        //mengjie add end
    }

    private void loadAbilities() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String abilitiesDirName = FilenameUtils.concat(stageDirName,
                "Skill/index.xml");
        abilitiesLoader = new AbilitiesLoader(new File(abilitiesDirName));
        Skill.addSkills();
    }

    private void loadDropGroup() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String dropGroupDirName = FilenameUtils.concat(stageDirName,"Items/dropGroup.xml");
        dropGroupLoader = new DropGroupLoader(new File(dropGroupDirName));
    }

    private void loadCommodityGroups() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String storeGroupDirName = FilenameUtils.concat(stageDirName,"Areas/CommodityGroups.xml");
        storeGroupLoader = new StoreGroupLoader(new File(storeGroupDirName));
    }

    private void loadCStoreGroups() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String storeGroupDirName = FilenameUtils.concat(stageDirName,"Areas/CreditGroups.xml");
        cstoreGroupLoader = new CStoreGroupLoader(new File(storeGroupDirName));
    }
    
    private void loadBloodsuckerStoreGroups() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String storeGroupDirName = FilenameUtils.concat(stageDirName,"Areas/BloodsuckerShop.xml");
    	bloodstoreGroupLoader = new BloodStoreGroupLoader(new File(storeGroupDirName));
    }
    
    private void loadIStoreGroups() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String storeGroupDirName = FilenameUtils.concat(stageDirName,"Areas/ishop.xml");
        istoreGroupLoader = new IStoreGroupLoader(new File(storeGroupDirName),Server.iMoneyType,Server.IMONEY_TYPE_CMCC);//增加参数：servertype和cmcctype
    }
    
    private void loadIStoreGroups2() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String storeGroupDirName = FilenameUtils.concat(stageDirName, "Areas/magicshop.xml");
    	istoreGroupLoader2 = new IStoreGroupLoader2(new File(storeGroupDirName),Server.iMoneyType,Server.IMONEY_TYPE_CMCC);
    }
    
    private void loadTongShopGroups() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String dirName = FilenameUtils.concat(stageDirName, "Areas/tongshop.xml");
    	tongShopLoader = new TongShopLoader(new File(dirName),Server.iMoneyType,Server.IMONEY_TYPE_CMCC);
    }
    
    private void loadDiscountShopGroups() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String dirName = FilenameUtils.concat(stageDirName, "Areas/discountshop.xml");
    	discountShopLoader = new DiscountShopLoader(new File(dirName),Server.iMoneyType,Server.IMONEY_TYPE_CMCC);
    }
    
    private void loadDownloadPointShopGroups() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String dirName = FilenameUtils.concat(stageDirName, "Areas/downloadPointShop.xml");
    	downloadPointShopLoader = new DownloadPointShopLoader(new File(dirName),Server.iMoneyType, Server.IMONEY_TYPE_CMCC);
    }
    
    private void loadPetColor() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String dirName = FilenameUtils.concat(stageDirName, "Items/petcolor.xml");
    	petColorLoader = new PetColorLoader(new File(dirName));
    }
    private void loadItemGroup() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String dirName = FilenameUtils.concat(stageDirName,"Items/itemgroups.xml");
        itemGroupLoader = new ItemGroupLoader(new File(dirName));
    }

    private void loadNpcs() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String taskNpcDirName = FilenameUtils.concat(stageDirName, "Areas/npc.xml");
        taskNpcLoader = new TaskNpcLoader(new File(taskNpcDirName));
    }

    private void loadRecipes() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String recipesDirName = FilenameUtils.concat(stageDirName,"Skill/Recipes.xml");
        recipesLoader = new RecipesLoader(new File(recipesDirName));
    }
    
    private void loadPrescription() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String recipesDirName = FilenameUtils.concat(stageDirName,
                "Skill/RecipesNew.xml");
        prescriptionsLoader = new PrescriptionsLoader(new File(recipesDirName));
    }

    private void loadItems() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String equDirName = FilenameUtils.concat(stageDirName,
                "Items/equ.xml");
        String itemDirName = FilenameUtils.concat(stageDirName,
                "Items/item.xml");
        itemLoader = new ItemLoader(new File(equDirName),new File(itemDirName));
    }
    
    /**
     * 载入阵营数据
     * @throws Exception
     */
    private void loadCamp() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String campDirName = FilenameUtils.concat(stageDirName, "Items/camp.xml");
        campLoader = new CampLoader(new File(campDirName));
    }
    
    private void loadChristmas() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
        String chrDirName = FilenameUtils.concat(stageDirName, "Items/chr.xml");
        christmasLoader = new ChristmasLoader(new File(chrDirName));
    }
    
    private void loadBook() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
        String bookFile = FilenameUtils.concat(stageDirName, "Items/book.xml");
        bookLoader = new BookLoader(new File(bookFile));
    }
    
    private void loadFarm() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
        String dir = FilenameUtils.concat(stageDirName, "Items/farm.xml");
        farmLoader = new FarmConfig(new File(dir));
    }
    
    private void loadAwardBox() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String dir = FilenameUtils.concat(stageDirName, "Items/awardBox.xml");
    	awardBoxLoader = new AwardBoxConfig(new File(dir));
    }
    
    private void loadTaskRequest() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
        String file = FilenameUtils.concat(stageDirName, "Tasks/taskRequest.xml");
        taskRequestLoader = new TaskRequestLoader(new File(file));
    }
    
	private void loadLyrics() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
        String lyricsName = FilenameUtils.concat(stageDirName, "Items/lyrics.xml");
        lyricsLoader = new LyricsLoader(new File(lyricsName));
    }
    
    private void loadLoveLyrics() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String lyricsName = FilenameUtils.concat(stageDirName, "Items/lovelyrics.xml");
    	loveLyricsLoader = new LoveLyricsLoader(new File(lyricsName));
    }
    
    //兔子赛跑
    private void rabbitRace() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String rabbitRaceName = FilenameUtils.concat(stageDirName, "Items/RabbitRace.xml");
    	rabbitRaceLoader = new RabbitRaceLoader(new File(rabbitRaceName));
    }
    
    private void loadLyricsSystem() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String lyricsName = FilenameUtils.concat(stageDirName, "Items/lyricsSystem.xml");
    	lyricsSystemLoader = new LyricsSystemLoader(new File(lyricsName));
    }
    
    private void loadRiddles() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
        String riddlesName = FilenameUtils.concat(stageDirName, "Items/riddles.xml");
        riddlesLoader = new RiddlesLoader(new File(riddlesName));
    }
    
    private void loadMonsterConstants() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String file = FilenameUtils.concat(stageDirName, "Items/monsterConstants.xml");
    	MonsterConstants.loadMonsterConstants(new File(file));
    }
    
    private void loadMercenaryConstants() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String file = FilenameUtils.concat(stageDirName, "Items/mercenary.xml");
    	MercenaryConstants.loadMercenaryConstants(new File(file));
    }
    
    private void loadPetDevelopData() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String file = FilenameUtils.concat(stageDirName, "Items/petDevelop.xml");
    	PetDevelopData.loadData(new File(file));
    }
    
    private void loadEquModle() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String file = FilenameUtils.concat(stageDirName, "Items/equmodle.xml");
    	EquModleConfig.load(new File(file));
    }
    
    private void loadIntervene() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String file = FilenameUtils.concat(stageDirName, "Items/intervene.xml");
    	battleInterveneLoader = new BattleIntervene(new File(file));
    }
    
    private void loadRiddles2() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String riddlesName = FilenameUtils.concat(stageDirName, "Items/riddles2.xml");
    	riddlesLoader2 = new RiddlesLoader2(new File(riddlesName));
    }
    
    private void loadWelcomeMessage() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String fileName = FilenameUtils.concat(stageDirName, "Items/welcome.xml");
    	welecomMessageLoader = new WelcomeMessageLoader(new File(fileName));
    }
    
    private void loadTrainGift()throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String fileName = FilenameUtils.concat(stageDirName, "Items/traingift.xml");
    	TrainGiftLoader = new TrainGiftMessageLoader(new File(fileName));
    }
    
    private void loadMagicPos()throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String fileName = FilenameUtils.concat(stageDirName, "Items/MagicPosition.xml");
    	magicPosLoader = new MagicPosLoader(new File(fileName));
    }
    
    
    private void loadLove() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
        String loveName = FilenameUtils.concat(stageDirName, "Items/love.xml");
        loveLoader = new LoveLoader(new File(loveName));
    }
    
    private void loadLove7() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
        String loveName = FilenameUtils.concat(stageDirName, "Items/love7.xml");
        love7Loader = new Love7Loader(new File(loveName));
    }
    
    private void loadUnlineExp() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
        String unlineDirName = FilenameUtils.concat(stageDirName, "Items/unlineExp.xml");
        unlineExpLoader = new UnlineExpLoader(new File(unlineDirName));
    }
    
    private void loadCampBuff () throws Exception {
    	String stageDirName = pkgDir.getAbsolutePath();
    	String campBuffDirName = FilenameUtils.concat(stageDirName, "Items/CampBuff.xml");
    	campBuffLoader = new CampBuffLoader(new File(campBuffDirName));
    }
    
    private void loadShout () throws Exception {
    	String stageDirName = pkgDir.getAbsolutePath();
    	String shoutDirName = FilenameUtils.concat(stageDirName, "Items/Shout.xml");
    	shoutLoader = new ShoutLoader(new File(shoutDirName));
    }
    
    private void loadTwelfthLunar () throws Exception {
    	String stageDirName = pkgDir.getAbsolutePath();
    	String TwelfthLunarDirName = FilenameUtils.concat(stageDirName, "Items/TwelfthLunar.xml");
    	twelfthLunarLoader = new TwelfthLunarLoader(new File(TwelfthLunarDirName));
    }
    
    private void loadBossRush() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String bossRushDirName = FilenameUtils.concat(stageDirName, "Areas/BossRush.xml");
    	bossRushLoader = new BossRushLoader(new File(bossRushDirName));
    }
    
    private void loadWorldBoss(StageService service) throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String fileName = FilenameUtils.concat(stageDirName, "Items/worldBoss.xml");
    	worldBossLoader = new WorldBossLoader(new File(fileName), this);
    }
    private void loadNoahsark() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String fileName = FilenameUtils.concat(stageDirName, "Items/noahsark.xml");
    	noahsarkLoader = new noahsarkLoader(new File(fileName), this);
    }
    
    private void loadEvolution() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String fileName = FilenameUtils.concat(stageDirName, "Items/petEvolution.xml");
    	evolutionLoader = new EvolutionLoader(new File(fileName));
    }
    
    private void loadRandomMessage() throws Exception{
    	String stageDirName = pkgDir.getAbsolutePath();
    	String rmDirName = FilenameUtils.concat(stageDirName, "Areas/randomMessage.xml");
    	randomMessageLoader = new RandomMessageLoader(new File(rmDirName));
    }
    private void loadIronChefActivityCount() throws Exception{
    	File file = new File(System.getProperty("user.dir") + "/IronChefActivity.xml");
    	try {
    		SAXReader reader = new SAXReader();
    		Document doc = reader.read(file);
    		Element root = doc.getRootElement();
    		if (root.elementIterator("IronChefActivity") != null) {
    			for (Iterator i = root.elementIterator("IronChefActivity"); i.hasNext();) {
    				Element el = (Element)i.next();
    				String playerId = el.attributeValue("id");
    				int id = Integer.parseInt(playerId);
    				String playerLevel = el.attributeValue("level");
    				int level = Integer.parseInt(playerLevel);
    				String playerName = el.attributeValue("playerName");
    				String tmpCount = el.attributeValue("count");
    				int count = Integer.parseInt(tmpCount);
    				TwelfthLunarShowInfo tsi = new TwelfthLunarShowInfo (id, level, playerName, count);
    				if (!TwelfthLunarConfig.playerDonateMap.containsKey(id)) {
    					TwelfthLunarConfig.playerDonateMap.put(id, tsi);
    				}
    			}
    		}
    		if (root.elementIterator("Gruel") != null) {
    			for (Iterator i = root.elementIterator("Gruel"); i.hasNext();) {
    				Element el = (Element)i.next();
    				String count = el.attributeValue("GruelCount");
    				TwelfthLunarConfig.gruelCount = Integer.parseInt(count);
    			}
    		}
    	} catch (Exception e) {
    	}
    }
    
    private void loadItemGroupInFile() throws Exception{
    	File file = new File(System.getProperty("user.dir") + "/IronChefActivity.xml");
    	try {
    		SAXReader reader = new SAXReader();
    		Document doc = reader.read(file);
    		Element root = doc.getRootElement();
    		if (root.elementIterator("IronChefActivity") != null) {
    			for (Iterator i = root.elementIterator("IronChefActivity"); i.hasNext();) {
    				Element el = (Element)i.next();
    				String playerId = el.attributeValue("id");
    				int id = Integer.parseInt(playerId);
    				String playerLevel = el.attributeValue("level");
    				int level = Integer.parseInt(playerLevel);
    				String playerName = el.attributeValue("playerName");
    				String tmpCount = el.attributeValue("count");
    				int count = Integer.parseInt(tmpCount);
    				TwelfthLunarShowInfo tsi = new TwelfthLunarShowInfo (id, level, playerName, count);
    				if (!TwelfthLunarConfig.playerDonateMap.containsKey(id)) {
    					TwelfthLunarConfig.playerDonateMap.put(id, tsi);
    				}
    			}
    		}
    		if (root.elementIterator("Gruel") != null) {
    			for (Iterator i = root.elementIterator("Gruel"); i.hasNext();) {
    				Element el = (Element)i.next();
    				String count = el.attributeValue("GruelCount");
    				TwelfthLunarConfig.gruelCount = Integer.parseInt(count);
    			}
    		}
    	} catch (Exception e) {
    	}
    }

    private void loadMaterialType() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String materialDirName = FilenameUtils.concat(stageDirName,"Areas/MaterialType.xml");
        materialTypeLoader = new MaterialTypeLoader(new File(materialDirName));
    }

    private void loadImages() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String npcPngsDirName = FilenameUtils.concat(stageDirName, "NpcImages");
        PngResources newNpcPngs = new PngResources(new File(npcPngsDirName));
        npcPngs = newNpcPngs;
        String mgPngsDirName = FilenameUtils.concat(stageDirName,
                "MonsterIcons");
        PngResources newMgPngs = new PngResources(new File(mgPngsDirName));
        mgPngs = newMgPngs;
        String monsterPngsDirName = FilenameUtils.concat(stageDirName,
                "MonsterImages");
        PngResources newMonsterPngs = new PngResources(new File(monsterPngsDirName));
        monsterPngs = newMonsterPngs;
    }

    public void loadTasks() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String taskDirName = FilenameUtils.concat(stageDirName, "Tasks");
        String tipsDirName = FilenameUtils.concat(stageDirName, "Tasks/tasktips.xml");
        taskService = new TaskService(new File(taskDirName),new File(tipsDirName));
        String awardDirName = FilenameUtils.concat(stageDirName,"Tasks/items.xml");
        taskAwardLoader = new TaskAwardLoader(new File(awardDirName));
        String relationDirName = FilenameUtils.concat(stageDirName,"Tasks/index.xml");
        taskDefinitionLoader = new TaskDefinitionLoader(new File(relationDirName));
        stageBuilder.setTaskService(taskService);
    }



    private void loadInstanceDefinition() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String instanceDirName = FilenameUtils.concat(stageDirName,"Areas/instance.xml");
        File file = new File(instanceDirName);
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        instances.clear();
        innerpackagefile.clear();
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("instance");i.hasNext();){
            Element e = (Element)i.next();
            int id = Integer.parseInt(e.attributeValue("id"));
            short map = Short.parseShort(e.attributeValue("map"));
            short x = Short.parseShort(e.attributeValue("x"));
            short y = Short.parseShort(e.attributeValue("y"));
            int maxPlayer = Integer.parseInt(e.attributeValue("maxplayer"));
            int refreshSecond = Integer.parseInt(e.attributeValue("refreshsecond"));
            int bbsId = Integer.parseInt(e.attributeValue("bbsid"));
            int minLevel = 0;
            String type = e.attributeValue("type");
            if("normal".equals(type)){
                minLevel = Integer.parseInt(e.attributeValue("minlevel"));
            }
            InstanceDefinition idf = new InstanceDefinition(id,map,x,y);
            idf.setMaxPlayer(maxPlayer);
            idf.setRefreshSecond(refreshSecond);
            idf.setBbsId(bbsId);
            idf.setType(type);
            idf.setMinLevel(minLevel);
            for(Iterator j = e.elementIterator("map");j.hasNext();){
                Element m = (Element)j.next();
                short mapId = Short.parseShort(m.attributeValue("id"));
                idf.addMap(mapId);
            }
            Element entrance = e.element("entrance");
            short entranceMapId = Short.parseShort(entrance.attributeValue("map"));
            short entranceX = Short.parseShort(entrance.attributeValue("x"));
            short entranceY = Short.parseShort(entrance.attributeValue("y"));
            short pixelX = Short.parseShort(entrance.attributeValue("pixel_x"));
            short pixelY = Short.parseShort(entrance.attributeValue("pixel_y"));
            
            int maxTime = Integer.parseInt(e.attributeValue("maxTime"));

            idf.setEntrance(entranceMapId);
            idf.setEntranceX(entranceX);
            idf.setEntranceY(entranceY);
            idf.setEntrancePixelX(pixelX);
            idf.setEntrancePixelY(pixelY);
            idf.setMaxTime(maxTime);
            instances.add(idf);
        }
        BossTips.clear();
        for(Iterator i = root.elementIterator("vmessage");i.hasNext();){
            Element e = (Element)i.next();
            int id = Integer.parseInt(e.attributeValue("mgid"),16);
            String message = e.attributeValue("message");
            BossTips.addTip(id,message);
        }
        bossDefineLoader = new BossDefineLoader(file);
        
        //mengjie add服务器怪出现消失发地区聊
        BossLocalTips.clear();
        for(Iterator i = root.elementIterator("localmessage");i.hasNext();){
            String localmessage[] = new String[3];
            Element e = (Element)i.next();
            int id = Integer.parseInt(e.attributeValue("mgid"),16);
            localmessage[0] = e.attributeValue("mapid");
            localmessage[1] = e.attributeValue("comemessage");
            localmessage[2] = e.attributeValue("endmessage");
            BossLocalTips.addTip(id,localmessage);
        }
        for(Iterator i = root.elementIterator("innerpackagefile");i.hasNext();){
            Element e = (Element)i.next();
            int innerPackageId = Integer.parseInt(e.attributeValue("stageId"));
            innerpackagefile.add(innerPackageId);
        }
        
    }
    
    /**
     * 加载阵营战场中的相关信息
     * @throws Exception
     */
    private void loadCampBattleInstanceDefinition() throws Exception {
        String stageDirName = pkgDir.getAbsolutePath();
        String instanceDirName = FilenameUtils.concat(stageDirName,"Areas/CampBattlefieldInstances.xml");
        File file = new File(instanceDirName);
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        Element root = doc.getRootElement();
        List<Integer> tempID = new ArrayList();
        for (Iterator i = root.elementIterator("Instance"); i.hasNext();) {
            Element e = (Element)i.next();
            int id = Integer.parseInt(e.attributeValue("id"));
            short map = Short.parseShort(e.attributeValue("map"));
            short x = Short.parseShort(e.attributeValue("x"));
            short y = Short.parseShort(e.attributeValue("y"));
            int refreshSecond = Integer.parseInt(e.attributeValue("refreshsecond"));
            int minLevel = 0;
            String type = e.attributeValue("type");
            int maxPlayer = Integer.parseInt(e.attributeValue("maxplayer"));
            int timeout = Integer.parseInt(e.attributeValue("timeout"));
            int clearance = Integer.parseInt(e.attributeValue("clearance"));
            int goodsLoseRate = Integer.parseInt(e.attributeValue("goodsloserate"));
            int competingGoodsID = Integer.parseInt(e.attributeValue("competinggoodsid"));
            if (competingGoodsID > 0) {
            	tempID.add(competingGoodsID);
            }
            String rules = e.attributeValue("rules");
            InstanceDefinition idf = new InstanceDefinition(id, map, x, y);
            idf.setRefreshSecond(refreshSecond);
            idf.setType(type);
            idf.setMinLevel(minLevel);
            idf.setMaxPlayer(maxPlayer);
            idf.setTimeout(timeout);
            idf.setClearance(clearance);
            idf.setCompetingGoodsID(competingGoodsID);
            idf.setGoodsLoseRate(goodsLoseRate);
            idf.setRules(rules);
            CampBattlefield campBattlefield = new CampBattlefield();
            for (Iterator j = e.elementIterator("Battlefield"); j.hasNext();) {
                Element m = (Element)j.next();
                int leveltype = Integer.parseInt(m.attributeValue("leveltype"));
                int winnerexprate = Integer.parseInt(m.attributeValue("winnerexprate"));
                int loserexprate = Integer.parseInt(m.attributeValue("loserexprate"));
                int winnerpoint = Integer.parseInt(m.attributeValue("winnerpoint"));
                int loserpoint = Integer.parseInt(m.attributeValue("loserpoint"));
                int rate = Integer.parseInt(m.attributeValue("rate"));
                int summonwinnerexprate = Integer.parseInt(m.attributeValue("summonwinnerexprate"));
                int summonloserexprate = Integer.parseInt(m.attributeValue("summonloserexprate"));
                int summonwinnerpoint = Integer.parseInt(m.attributeValue("summonwinnerpoint"));
                int summonloserpoint = Integer.parseInt(m.attributeValue("summonloserpoint"));
                int summonrate = Integer.parseInt(m.attributeValue("summonrate"));
                int giftID = Integer.parseInt(m.attributeValue("giftID"));
                List<Integer> temp = new ArrayList();
                for (Iterator k = m.elementIterator("Summon"); k.hasNext();) {
                	Element n = (Element)k.next();
                	int starthour = Integer.parseInt(n.attributeValue("starthour"));
                    int startminute = Integer.parseInt(n.attributeValue("startminute"));
                    int endhour = Integer.parseInt(n.attributeValue("endhour"));
                    int endminute = Integer.parseInt(n.attributeValue("endminute"));
                    temp.add(starthour);
                    temp.add(startminute);
                    temp.add(endhour);
                    temp.add(endminute);
                }
                temp.toArray();
                int[] timeperiods = new int[temp.size()];
                for (int k = 0; k < temp.size(); k += 4) {
        	        timeperiods[k] = temp.get(k);
        	        timeperiods[k + 1] = temp.get(k + 1);
        	        timeperiods[k + 2] = temp.get(k + 2);
        	        timeperiods[k + 3] = temp.get(k + 3);
                }
                CampBattlefieldAward campBattlefieldAward = new CampBattlefieldAward(leveltype, summonloserexprate,
                							summonloserpoint, summonwinnerexprate, summonwinnerpoint, winnerexprate,
                									winnerpoint, loserexprate, loserpoint);
                campBattlefieldAward.setRate(rate);
                campBattlefieldAward.setSummonRate(summonrate);
                campBattlefieldAward.setTimePeriods(timeperiods);
                campBattlefieldAward.setGiftID(giftID);
                campBattlefield.putCampBattlefieldAward(campBattlefieldAward);
            }
            idf.setCampBattlefield(campBattlefield);
            for (Iterator j = e.elementIterator("Map"); j.hasNext();) {
                Element m = (Element)j.next();
                short mapId = Short.parseShort(m.attributeValue("id"));
                idf.addMap(mapId);
            }
            Element entrance = e.element("DarkEntrance");
            short entranceMapId = Short.parseShort(entrance.attributeValue("map"));
            short entranceX = Short.parseShort(entrance.attributeValue("x"));
            short entranceY = Short.parseShort(entrance.attributeValue("y"));
            short pixelX = Short.parseShort(entrance.attributeValue("pixel_x"));
            short pixelY = Short.parseShort(entrance.attributeValue("pixel_y"));
            idf.setDarkEntrance(entranceMapId);
            idf.setDarkEntranceX(entranceX);
            idf.setDarkEntranceY(entranceY);
            idf.setDarkEntrancePixelX(pixelX);
            idf.setDarkEntrancePixelY(pixelY);
            entrance = e.element("BrightEntrance");
            entranceMapId = Short.parseShort(entrance.attributeValue("map"));
            entranceX = Short.parseShort(entrance.attributeValue("x"));
            entranceY = Short.parseShort(entrance.attributeValue("y"));
            pixelX = Short.parseShort(entrance.attributeValue("pixel_x"));
            pixelY = Short.parseShort(entrance.attributeValue("pixel_y"));
            idf.setBrightEntrance(entranceMapId);
            idf.setBrightEntranceX(entranceX);
            idf.setBrightEntranceY(entranceY);
            idf.setBrightEntrancePixelX(pixelX);
            idf.setBrightEntrancePixelY(pixelY);
            instances.add(idf);
        }
    }
    
    /**
     * 加载开启阵营战场前等配置信息
     * @throws Exception
     */
    private void loadCampBattlefields() throws Exception{
        String stageDirName = pkgDir.getAbsolutePath();
        String battlefieldtimerDir = FilenameUtils.concat(stageDirName,"Areas/CampBattlefields.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(battlefieldtimerDir);
        Element root = doc.getRootElement();
        List l = new ArrayList();
        ArrayList<String> nameList = new ArrayList();
        CampBattlefieldConfig.battlefields.clear();
        CampBattlefieldConfig.battlefieldName.clear();
        for (Iterator i = root.elementIterator("CampBattlefieldType"); i.hasNext();) {
            Element el = (Element)i.next();
            CampBattlefield battlefield = new CampBattlefield();
            String name = el.attributeValue("name");
            String type = el.attributeValue("type");
            String desc = el.attributeValue("desc");
            String target = el.attributeValue("target");
            int model = Integer.parseInt(el.attributeValue("model"));
            Map<Integer, Integer> battlefieldCountMap = new HashMap<Integer, Integer>();
            for (Iterator j = el.elementIterator("CampBattlefield"); j.hasNext();) {
            	Element els = (Element)j.next();
            	int minlevel = Integer.parseInt(els.attributeValue("minlevel"));
            	int maxlevel = Integer.parseInt(els.attributeValue("maxlevel"));
                int leveltype = Integer.parseInt(els.attributeValue("leveltype"));
                int maxcount = Integer.parseInt(els.attributeValue("maxcount"));
                int instanceid = Integer.parseInt(els.attributeValue("instanceid"));
                int brightplayers = Integer.parseInt(els.attributeValue("brightplayers"));
                int darkplayers = Integer.parseInt(els.attributeValue("darkplayers"));
            	Element elt = els.element("Award");
            	int winnerexprate = Integer.parseInt(elt.attributeValue("winnerexprate"));
            	int loserexprate = Integer.parseInt(elt.attributeValue("loserexprate"));
            	int winnerpoint = Integer.parseInt(elt.attributeValue("winnerpoint"));
                int loserpoint = Integer.parseInt(elt.attributeValue("loserpoint"));
                int summonwinnerexprate = Integer.parseInt(elt.attributeValue("summonwinnerexprate"));
                int summonloserexprate = Integer.parseInt(elt.attributeValue("summonloserexprate"));
                int summonwinnerpoint = Integer.parseInt(elt.attributeValue("summonwinnerpoint"));
                int summonloserpoint = Integer.parseInt(elt.attributeValue("summonloserpoint"));
                CampBattlefieldAward battlefieldAward = new CampBattlefieldAward(leveltype,
                		summonloserexprate, summonloserpoint, summonwinnerexprate, summonwinnerpoint,
                		winnerexprate, winnerpoint, loserexprate, loserpoint);
                battlefield.putCampBattlefieldAward(battlefieldAward);
                CampBattlefieldSchedule schedule = new CampBattlefieldSchedule(name, type, instanceid, leveltype, maxlevel, minlevel);
                l.add(schedule);
                battlefieldCountMap.put(leveltype, maxcount);
                CampbattlefieldWarriorPlaces warriorPlaces = new CampbattlefieldWarriorPlaces(leveltype, brightplayers, darkplayers);
                battlefield.putCampBattlefieldWarrior(warriorPlaces);
            }
            CampBattlefieldService.setBattlefieldMaxCount(type, battlefieldCountMap);
            battlefield.setName(name);
            battlefield.setDesc(desc);
            battlefield.setTarget(target);
            battlefield.setModel(model);
            CampBattlefieldConfig.battlefields.put(name, battlefield);
            nameList.add(name);
        }
        CampBattlefieldSchedule[] schedules = new CampBattlefieldSchedule[l.size()];
        CampBattlefieldConfig.battlefieldName = new ArrayList(nameList.size());
        l.toArray(schedules);
        nameList.toArray();
        CampBattlefieldService.setSchedules(schedules);
        CampBattlefieldConfig.battlefieldName = nameList;
        if (reload) {
        	CampBattlefieldService.start();
        }
    }
    
    /**
     * @param stageId
     * @return是否是内置关卡，4.4有效
     */
    public boolean isInnnerStage(int stageId){
    	boolean innerPackageFlag = false;
    	for(int i = 0; i < innerpackagefile.size(); i++){
    		int innerPackageId = (Integer)innerpackagefile.get(i);
    		if(innerPackageId == stageId){
    			innerPackageFlag = true;
    			break;
    		}
    	}
    	return innerPackageFlag;
    }
    public InstanceDefinition[] getInstance(){
        InstanceDefinition[] ret = new InstanceDefinition[instances.size()];
        instances.toArray(ret);
        return ret;
    }

    public void reload() throws Exception{

        reload = true;

        loadInstanceDefinition();
        
        loadCampBattleInstanceDefinition();

        loadImages();

        loadAbilities();

        loadMaterialType();
        loadPetColor();
        loadItems();

        loadRecipes();
        
        loadPrescription();

        loadNpcs();
        
        //怪物世界年落常量 重置年率机率
        loadMonsterConstants();

        loadStages();

        boolean isFake = false;
        
        if(Server.instance != null){
        	isFake = Server.instance.isFake;
        }
        if (!isFake) {
        	loadTasks();
        }

        loadChatFavorites();

        loadCommodityGroups();

        loadDropGroup();

        loadTransfer();

        loadLevelTips();

        loadCStoreGroups();
        
        loadIStoreGroups();
        
        loadIStoreGroups2();
        
        loadTongShopGroups();
        
        loadDiscountShopGroups();
        
        loadDownloadPointShopGroups();
        
        loadBloodsuckerStoreGroups();
        
        loadItemGroup();

        loadFaces();

        loadHouses();

        loadSuits();

        loadEnhances();

//        loadBattleFieldTimer();
        loadCampBattlefields();

        if (!isFake) {
        	loadDiscount();
        }

        loadQuestions();

        loadSuggests();

        if(worldService != null){
            worldService.reload();
        }

        loadBathHouses();

        loadSportsTimer();

        //mengjie add
        loadWorldMap();

        loaditemmsg();

        loadcreditshop();
        
        loadGiftGroups();
        
        loadinstancesadd();
        
        loadPetEnhance();
        
        loadibuygift();
        
        loadLooptasks();

        voteService.reload();
        //jwp add end
        
        // 初始化下载文件管理器
        initDownloadManager();
        
        //ui脚本的演示和脚本的关联关系加载
        loadTaskUIHlep();
        
        loadDiamond(pkgDir);
        
        //阵营
        loadCamp();
        
        //圣诞节活动
        loadChristmas();
        
        // 战场胜利后阵营BUFF
        loadCampBuff();
        
        // 喊话活动
        loadShout();
        
        //离线经验
        loadUnlineExp();
        
        // 腊八活动
        loadTwelfthLunar();
        
        //记歌词活动
        loadLyrics();
        
        //情歌对唱
        loadLoveLyrics();
        
        //兔子赛跑
        rabbitRace();
        
        //情人节情话
        loadLove();
        
        //指路宝典
        loadBook();
        
        //元宵猜灯谜活动
        loadRiddles();
        //咏春诗歌大赛
        loadRiddles2();
        
        //任务请求
        loadTaskRequest();
        
        ItemTop.load4File();
        
      //重构装备兑换 mengjie
        loadExchangeData();
        
        //加载援护设置(宠物乱入)
        loadIntervene();
        
        //加载欢渠道欢迎语句
        loadWelcomeMessage();
        
        //加载佣兵数据
        loadMercenaryConstants();
        
        //加载装备模板
        loadEquModle();
        
        //加载点歌系统歌库
        loadLyricsSystem();
        
        //加载七夕情人节
        loadLove7();
        
        //加载庄园相关
        loadFarm();
        
        //加载宝箱相关
        loadAwardBox();
        
        //加载下100层的BOSS
        loadBossRush();
        
        //加载BOSS数据后才能够加载排行榜
        BossBattleTop.loadfile();
        
        //加载聚灵等级排行
        TrainLevelTop.loadfile();
        
        //加载每周使用聚灵点排行
        TrainLevelTop.load2File();
        
        //加载随机喊话
        loadRandomMessage();
        
        //加载聚灵点排行发奖
        loadTrainGift();
        
        //加载封印法阵数据
        loadMagicPos();
        
        //加载各法阵等级排行榜
        MagicPositionTop.loadWaterInfo();
        MagicPositionTop.loadSoilInfo();
        MagicPositionTop.loadFireInfo();
        MagicPositionTop.loadWindInfo();
        MagicPositionTop.loadMindInfo();
        
        RabbitRaceTop.loadWinPlayerInfo();
        
        //加载宠物培养文件
        loadPetDevelopData();
        //加载宠物培养排行榜
        PetDevelopTop.loadfile();
        
        //世界BOSS加载
        loadWorldBoss(this);
        
        //宝石排行榜加载
        GemTop.loadfile();
        GemTop.init();
        
        //加载宠物进化数据
        loadEvolution();
        //诺亚方舟
        loadNoahsark();
       //加载宠物进化排行榜
        PetEvolutionTop.loadEvolutionFile();
    }

    public int[] getVisibleObjectIds(){
        IRefreshObject[] npc = fNpcPool.getVisibleObjects();
        IRefreshObject[] mg = fMgPool.getVisibleObjects();
        IRefreshObject[] resource = fResourcePool.getVisibleObjects();
//        IRefresh[] npc = npcPool.getVisibleObjects();
//        IRefresh[] mg = mgPool.getVisibleObjects();
//        IRefresh[] resource = resourcePool.getVisibleObjects();
        IntList l = new ArrayIntList(npc.length+mg.length+resource.length);
        for(int i=0;i<npc.length;i++){
            l.add(npc[i].getId());
        }
        for(int i=0;i<mg.length;i++){
            l.add(mg[i].getId());
        }
        for(int i=0;i<resource.length;i++){
            l.add(resource[i].getId());
        }
        return l.toArray();
    }

    public void setConnectService(ConnectService connectService){
        this.connectService = connectService;
    }

    public void setPhoneService(PhoneService phoneService){
        this.phoneService = phoneService;
    }

    private void loadStages() throws Exception{
        String areasDirName = FilenameUtils.concat(pkgDir.getAbsolutePath(),"Areas/Export");
        File areasDir = new File(areasDirName);
        File[] files = areasDir.listFiles();
        Map newStages = new HashMap();
        Map newPkgems = new HashMap();
        Map newPkgss = new HashMap();
        Map newname2scenes = new HashMap();
        List pkgFiles = new ArrayList();
        List pkgemFiles = new ArrayList();
        List pkgsFiles = new ArrayList();
        for(int i=0;i<files.length;i++){
            String name = files[i].getName();
            String ext = FilenameUtils.getExtension(name);
            if("pkg".equals(ext)){
                pkgFiles.add(files[i]);
//                log.info("load "+name);
//                Stage stage = StageLoader.getStage(files[i]);
////                loadDynamicObjects(stage);
//                packMonsters(stage);
//                newStages.put(new Short(stage.getId()), stage);
//                Scene[] scenes = stage.getScenes();
//                for(int j=0;j<scenes.length;j++){
//                    newname2scenes.put(scenes[j].getName(),scenes[j]);
//                }
//                log.info("load "+name+" ok");
            }
            else if("pkgem".equals(ext)){
                pkgemFiles.add(files[i]);
//                log.info("load "+name);
//                Stage stage = StageLoader.getStage(files[i]);
////                loadDynamicObjects(stage);
//                packMonsters(stage);
//                newPkgems.put(new Short(stage.getId()), stage);
//                log.info("load "+name+" ok");
            }else if("pkgs".equals(ext)){
                pkgsFiles.add(files[i]);
//                log.info("load "+name);
//                Stage stage = StageLoader.getStage(files[i]);
////                loadDynamicObjects(stage);
//                packMonsters(stage);
//                newPkgss.put(new Short(stage.getId()), stage);
//                log.info("load "+name+" ok");
            }
        }
        for(int i=0;i<pkgsFiles.size();i++){
            String name = ((File)pkgsFiles.get(i)).getName();

//            log.info("load "+name);
            Stage stage = StageLoader.getStage((File)pkgsFiles.get(i));
//                loadDynamicObjects(stage);
            packMonsters(stage);
            newPkgss.put(new Short(stage.getId()), stage);
            log.info("load "+name+" ok");
        }
        for(int i=0;i<pkgemFiles.size();i++){
            String name = ((File)pkgemFiles.get(i)).getName();
//            log.info("load "+name);
            Stage stage = StageLoader.getStage((File)pkgemFiles.get(i));
//                loadDynamicObjects(stage);
            packMonsters(stage);
            newPkgems.put(new Short(stage.getId()), stage);
            log.info("load "+name+" ok");
        }
        for(int i=0;i<pkgFiles.size();i++){
            String name = ((File)pkgFiles.get(i)).getName();
//            log.info("load "+name);
            Stage stage = StageLoader.getStage((File)pkgFiles.get(i));
//                loadDynamicObjects(stage);
            packMonsters(stage);
            newStages.put(new Short(stage.getId()), stage);
            Scene[] scenes = stage.getScenes();
            for(int j=0;j<scenes.length;j++){
                newname2scenes.put(scenes[j].getName(),scenes[j]);
            }
            log.info("load "+name+" ok");
        }


        stages = newStages;
        pkgems = newPkgems;
        pkgss = newPkgss;
        name2scenes = newname2scenes;
    }

    private void loadChatFavorites() throws Exception{
        String dirName = FilenameUtils.concat(pkgDir.getAbsolutePath(),"Areas/chatfavorites.xml");
        chatFavoriteLoader = new ChatFavoriteLoader(new File(dirName));
    }

    public Stage[] getStages(){
        Stage[] ret = new Stage[stages.size()];
        stages.values().toArray(ret);
        return ret;
    }

    private void packMonsters(Stage stage) throws IOException {
        Monster[] monsters = stage.getMonsters();
        List l = new ArrayList(monsters.length);
        for (int i = 0; i < monsters.length; i++) {
            byte type = monsters[i].getType();
            if ((type & 2) == 0) {
                l.add(monsters[i]);
            } else {
                break;
            }
        }
        Set abilities = new HashSet();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dis = new DataOutputStream(bos);
        dis.writeByte((byte) l.size());
        for (int i = 0; i < l.size(); i++) {
            Monster monster = (Monster) l.get(i);
            dis.writeShort(monster.getPngId());
            dis.writeUTF(monster.getName());
            dis.writeByte(monster.getType());
            dis.writeShort(monster.getLevel());
            dis.writeShort(monster.getVit());
            dis.writeShort(monster.getStr());
            dis.writeShort(monster.getInt());
            dis.writeShort(monster.getAgi());
            dis.writeShort(monster.getPMinAttack());
            dis.writeShort(monster.getPMaxAttack());
            dis.writeShort(monster.getPDef());
            dis.writeShort(monster.getMMinAttack());
            dis.writeShort(monster.getMMaxAttack());
            dis.writeShort(monster.getMDef());
            dis.writeShort(monster.getParry());
            dis.writeShort(monster.getHit());
            dis.writeShort(monster.getPCritial());
            dis.writeShort(monster.getMCritial());
            dis.writeInt(monster.getHp());
            dis.writeInt(monster.getMp());
            dis.writeByte(monster.getPetType());
            int[] abis = monster.getAbilities();
            dis.writeByte(abis.length);
            for(int j = 0;j<abis.length;j++){
                dis.writeShort(abis[j]);
            }
            for (int j = 0; j < abis.length; j++) {
                abilities.add(Ability.getAbility(abis[j]));
            }
        }
        InPkgFile pkgFile = new InPkgFile();
        pkgFile.setData(bos.toByteArray());
        pkgFile.setName("m.d");
        stage.addInPkgFile(pkgFile);
        pkgFile = new InPkgFile();
        pkgFile.setData(getAbilitiesBytes(abilities));
        pkgFile.setName("ms.d");
        stage.addInPkgFile(pkgFile);
    }

    private byte[] getAbilitiesBytes(Set abilities) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeShort((short) abilities.size());
        Iterator ite = abilities.iterator();
        while (ite.hasNext()) {
            Ability ability = (Ability) ite.next();
            dos.writeByte(ability.getType());
            dos.writeUTF(ability.getName());
            dos.writeByte(ability.getEffect());
            dos.writeByte(ability.getStatus());
            dos.writeByte(ability.getPosition());
            dos.writeByte(ability.getCD());
            dos.writeByte(ability.getCDTime());
            dos.writeByte(1);
            dos.writeShort(ability.getId());
            dos.writeByte(ability.getLevel());
            dos.writeInt(ability.getValue1());
            dos.writeInt(ability.getValue2());
            dos.writeByte(ability.getEffectTime());
            dos.writeShort(ability.getMana());
            dos.writeByte(ability.getArithmetic());
            dos.writeByte(ability.getHit());
        }
        return bos.toByteArray();
    }

    public MonsterGroup getMonsterGroup(int mgId){
        Scene scene = getScene((short)(mgId>>16));
        if(scene!=null){
            return scene.getMonsterGroup(mgId);
        }
        return null;
    }

    public Stage getStage(short stageId){
        return (Stage)stages.get(new Short(stageId));
    }

    public Stage getStage(String sceneName){
        Scene scene = (Scene)name2scenes.get(sceneName);
        if(scene!=null){
            return getStage((short)((scene.getMapId()>>4)&0xFFFF));
        }
        return null;
    }

    public Stage getStage(short stageId,String model){
        PhoneType phone = phoneService.getPhoneType(model);
        if(phone==null||phone.getFiltType()==PhoneType.PKG)
            return (Stage)stages.get(new Short(stageId));
        else if(phone==null||phone.getFiltType()==PhoneType.PKGEM)
            return (Stage)pkgems.get(new Short(stageId));
        else
            return (Stage)pkgss.get(new Short(stageId));
    }

    public Monster[] getMonsters(int mgId){
        short mapId = (short)(mgId>>16);
        short stageId = Utils.getStageId(mapId);
        Stage stage = getStage(stageId);
        if(stage!=null){
            Scene scene = stage.getScene(mapId&0xF);
            if(scene!=null){
                MonsterGroup mg = scene.getMonsterGroup(mgId);
                byte mIds[] = mg.getMonstersId();
                short pros[] = mg.getProbabilities();
                Monster[] ret = new Monster[mIds.length];
                for(int i=0;i<ret.length;i++){
                    ret[i] = stage.getMonster(mIds[i]);
                }
                return ret;
            }
        }
        return new Monster[0];
    }
    
    public Scene getScene(short mapId){
        short stageId = Utils.getStageId(mapId);
        Stage stage = getStage(stageId);
        if(stage!=null){
            return stage.getScene(mapId&0xF);
        }
        return null;
    }

    public Resource getResource(short mapId,int resourceId){
        Scene scene = getScene(mapId);
        if(scene==null)
            return null;
        return scene.getResource(resourceId);
    }

    public Lock lockResource(int id,PlayerData player) throws LockException{
        return fResourcePool.lock(id,player);
    }

    public Lock lockMG(int id,PlayerData player) throws LockException{
        return fMgPool.lock(id,player);
    }

    public void realese(Lock lock,boolean complete) throws LockException{
        byte type = lock.getObject().getType();
        if(type==IRefreshObject.RESOURCE){
            fResourcePool.release(lock,complete);
        }
        else if(type==IRefreshObject.MG){
            fMgPool.release(lock,complete);
        }
        else if(type==IRefreshObject.NPC){
            fNpcPool.release(lock,complete);
        }
    }

    private void loadDynamicObjects(Stage stage){
        Scene[] scenes = stage.getScenes();
        for(int i=0;i<scenes.length;i++){
            Npc[] npcs = scenes[i].getDynNpcs();
            for(int j=0;j<npcs.length;j++){
                fNpcPool.addNpc(npcs[j]);
            }
            MonsterGroup[] mgs = scenes[i].getDynMonsterGroups();
            for(int j=0;j<mgs.length;j++){
                fMgPool.addMG(mgs[j]);
            }
            Resource[] resources = scenes[i].getResources();
            for(int j=0;j<resources.length;j++){
                fResourcePool.addResource(resources[j]);
            }
        }

    }

    public byte[] getTaskBytes(short id,int level) {
        try {
            ETFFile etfFile = taskService.findETF(id,level);
            if (etfFile != null) {
                etfFile = ETFUtil.clone(etfFile);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ETFUtil.save(etfFile, bos);
                return bos.toByteArray();
            }
            return null;
        } catch (IOException ex) {
            return null;
        }
    }

    public byte[] getTaskBytes(short id, String[] args) {
        ETFFile etfFile = taskService.fineETF(id, args);
        try {
            if (etfFile != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ETFUtil.save(etfFile, bos);
                return bos.toByteArray();
            }
            return null;
        } catch (IOException ex) {
            return null;
        }
    }

    public String[] getTasksName(short[] id,int level){
        try {
            String[] ret = new String[id.length];
            for(int i=0;i<id.length;i++){
                ETFFile etfFile = taskService.findETF(id[i],level);
                if (etfFile != null) {
                    ret[i] = etfFile.taskName;
                }else{
                    ret[i] = ""; //leo Added
                }
            }
            return ret;
        } catch (Exception ex) {
            return null;
        }
    }

    public ETFFile[] getTaskFiles(short[] taskIds,int level){
        return taskService.findETFs(taskIds,level);
    }

    public int getTaskLevel(int taskId){
        return taskService.getTaskLevel(taskId);
    }
    
    public TaskService getTaskService() {
    	return taskService;
    }

    public PngResourceData getPng(short type, short id) {
        String prefix = "";
        PngResources resource = null;
        if (type == 3) {
            resource = mgPngs;
        } else if (type == 4) {
            prefix = "n";
            resource = npcPngs;
        } else if (type == 5) {
            prefix = "m";
            resource = monsterPngs;
        }
        String name = prefix + id;
        PngResourceData data = resource.getPngResourceData(name);
        return data;
    }
    /**
     * @param type
     * @param name
     * @return 获得动画ctn字节
     */
    public byte[] getCtn(short type , String name){
    	
    	byte[] bytes = null;
    	if(type == 1 || type == 2 ){//人物形象， 战斗形象
    		bytes = roleAnimate.getCtn(name);
    	}else if(type  == 6){  //请求ui动画
    		bytes = UIAnimate.getCtn(name);
    	}
    	return bytes;
    }
    
    public byte[] getPip(short type, String name){
    
    	byte[] bytes = null;
    	if(type == 1 || type == 2){
    		bytes = roleAnimate.getPip(name);
    	}else if(type == 3 || type == 4 || type == 5){
    		bytes = attachRoleAnimate.getPip(name);
    	}else if(type == 6){
    		bytes = UIAnimate.getPip(name);
    	}
    	return bytes;
    	
    }
/*    public byte[] getStageBytes(short id, Map parameters,String model) throws Exception {
        Stage stage = getStage(id,model);
        if (stage == null)
            return null;
        return stageBuilder.toBytes(stage, parameters);
    }*/
    
    public byte[] getStageBytesClientDateVersion(short id, Map parameters,String model, int dataVersion, short crc) throws Exception {
        Stage stage = getStage(id,model);
        if (stage == null)
            return null;
        return stageBuilder.toBytesClientDataVersion(stage, parameters, dataVersion, crc);
    }
    
    /**
     * @param id
     * @param parameters
     * @param model
     * @return获得内置关卡里面的数据， 只包含脚本任务数据
     * @throws Exception
     */
    public byte[] getInnerPkgBytes(short id, Map parameters,String model) throws Exception {
    	  Stage stage = getStage(id,model);
          if (stage == null)
              return null;
          return stageBuilder.toInnerBytes(stage, parameters);
    }
    
    public void changePetColor() throws IOException{
    	int PET_COUNT = 6;
    	for(int i = 0;i<2;i++){
    		for(int j = 0; j < PET_COUNT; j++){
    			String path = "client_res/4.0/Common/pet" + (i + 1) + "_" +j +"_0.ctn";
    			byte[] ret = loadFileData(new File(pkgDir, path));
    			byte[] pipimg = loadFileData(new File(pkgDir, path.replace("_0.ctn", ".pip")));
    			int[] imgData = readPaletteAndFrame(pipimg);
//    			int version = (byte)((ret[0] >> 6) & 0x03);
    			for(int k = 1;k < imgData[0];k++){
    				byte[] copy = new byte[ret.length];
    				System.arraycopy(ret, 0, copy, 0, ret.length);
    				writeFile(changePetColor(copy,k,imgData[1]),path.replace("0.ctn", k + ".ctn"));
    			}
    		}
    	}
    	
    }
    
    public void writeFile(byte[] data,String path) throws IOException{
    	File ctn = new File(pkgDir,path);
    	FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(ctn);
			fos.write(data);
		}finally {
			if (fos != null) {
				fos.close();
			}
		}
    }
    /**
     * 载入文件内容到字符数组。
     */
    public static byte[] loadFileData(File src) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(src);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ByteArrayOutputStream bos = new ByteArrayOutputStream((int)src.length());
			byte[] data = new byte[256];
			int len;
			while ((len = bis.read(data)) >= 0) {
				if (len == 0) {
					continue;
				}
				bos.write(data, 0, len);
			}
			return bos.toByteArray();
		} catch (IOException e) {
			throw e;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
	}
    
    private int[] readPaletteAndFrame(byte[] data) throws IOException {
    	int[] value = new int[2];
    	DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
    	byte[] head = new byte[3];
        dis.read(head);
        // 读取调色板数据
        int c = dis.readByte() & 0xff;
        int[][] palette = new int[c][];
        for(int i = 0; i < c; i++){
            palette[i] = readPalette(dis);
        }
        // 读取图块数据
        int size = dis.readByte() & 0xff;
        value[0] = c;
        value[1] = size;
    	return value;
    }
    
    /**
     * 读取一个调色板。
     */
    private int[] readPalette(DataInputStream dis) throws IOException{
        int len = dis.readInt();
        int[] ret = new int[len];
        dis.skip(4);
        for(int i = 0; i < ret.length; i++){
            ret[i] = dis.readInt();
        }
        return ret;
    }
    
    private static int[] readIntArray(DataInputStream dis, short[] start, byte[] len) throws IOException {
        int count = start.length;
        int[][] ret = new int[count][];
        int pos = 0;
        for (int i = 0; i < count; i++) {
            int count2 = dis.readByte() & 0xFF;
            start[i] = (short)pos;
            len[i] = (byte)count2;
            ret[i] = new int[count2];
            for (int j = 0; j < count2; j++) {
                ret[i][j] = dis.readInt();
            }
            pos += count2;
        }
        int[] ret2 = new int[pos];
        pos = 0;
        for (int i = 0; i < count; i++) {
            System.arraycopy(ret[i], 0, ret2, pos, ret[i].length);
            pos += ret[i].length;
        }
        return ret2;
    }
    
    private static String[] readStringArray(DataInputStream dis, boolean byteLength) throws IOException{
        int count;
        if(byteLength){
            count = dis.readByte() & 0xFF;
        }else{
            count = dis.readShort() & 0xFFFF;
        }
        String[] ret = new String[count];
        for(int i = 0; i < count; i++){
            ret[i] = dis.readUTF();
        }
        return ret;
    }
    
    private static void writeIntArray(DataOutputStream dos,byte[] len, int[] data, int count)throws IOException{
    	int tmpCount = count & 0xFFFF;
	    int pos = 0;
	    for(int i =0;i<tmpCount;i++){
	    	dos.writeByte(len[i]);
	    	int count2 = len[i];
	    	while(count2 > 0){
	    		dos.writeInt(data[pos]);
	    		pos++;
	    		count2--;
	    	}
	    }
    }
    
    private static void writeStringArray(DataOutputStream dos,String[] str,boolean byteLength)throws IOException{
    	int count = str.length;
    	if(byteLength){
    		dos.writeByte((byte)count);
    	}else{
    		dos.writeShort((short)count);
    	}
    	for(int i = 0;i<count;i++){
    		dos.writeUTF(str[i]);
    	}
    }
    
    private static byte[] changePetColor(byte[] data, int index,int frameCount)throws IOException{
    	DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
    	short tmpLen = dis.readShort();
    	short[] framePos = new short[tmpLen & 0xFFFF];
        byte[] frameLen = new byte[framePos.length];
        int[] frameData = readIntArray(dis, framePos, frameLen);
        byte tmpPosLen = dis.readByte();
        short[] animatePos = new short[tmpPosLen & 0xFF];
        byte[] animateLen = new byte[animatePos.length];
        int[] animateData = readIntArray(dis, animatePos, animateLen);
        String[] sourceImageNames = readStringArray(dis, true);
        
     	for(int i = 0;i<frameData.length; i++){
    		int iframe = (frameData[i] >> 21) & 0xFF;
    		int tmp = frameData[i] & 0x1FFFFF;
    		frameData[i] = ((iframe + index * frameCount)<<21) | tmp;   
    	}
     	
     	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    DataOutputStream dos = new DataOutputStream(bos);
	    dos.writeShort(tmpLen);
	    writeIntArray(dos,frameLen,frameData,tmpLen);
	    dos.writeByte(tmpPosLen);
	    writeIntArray(dos, animateLen,animateData,tmpPosLen);
	    writeStringArray(dos,sourceImageNames,true);
	    dos.flush();
    	return bos.toByteArray();
    }
    
    class PoolCallBack implements IRefreshCallback {
        public void objectCreated(IRefreshObject obj) {
//            UWAPSegment seg = new UWAPSegment(ServerConstants.RESOURCE_ADD);
//            seg.writeInt(obj.getId());
//            if(connectService!=null)
//                connectService.broadcast(seg);
        }


        public void objectDisappeared(IRefreshObject obj) {
//            UWAPSegment seg = new UWAPSegment(ServerConstants.RESOURCE_DELETE);
//            seg.writeInt(obj.getId());
//            if(connectService!=null)
//                connectService.broadcast(seg);
        }

        public void objectLocked(IRefreshObject obj){

        }

        public void objectReleased(IRefreshObject obj){

        }

    }
}
