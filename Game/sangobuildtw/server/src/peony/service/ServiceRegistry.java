package peony.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import peony.auction.AuctionService;
import peony.channel.ChannelService;
import peony.db.DBService;
import peony.depot.DepotService;
import peony.game.DataService;
import peony.game.PropertyService;
import peony.game.admin.AdminService;
import peony.game.battlefield.FlagBattleFieldVMapManager;
import peony.game.beautyparade.BeautyParadeService;
import peony.game.chat.ChatService;
import peony.game.clientbbs.ClientBbsService;
import peony.game.convoy.NationConvoyService;
import peony.game.exchange.ExchangeService;
import peony.game.exp.ExpService;
import peony.game.file.FileService;
import peony.game.gift.GameChannelService;
import peony.game.gift.GameCityService;
import peony.game.gift.GiftService;
import peony.game.instance.BossScoreService;
import peony.game.instance.NormalVMapManager;
import peony.game.itemenhance.JewelService;
import peony.game.mail.MailService;
import peony.game.nation.CandidateService;
import peony.game.nation.NationService;
import peony.game.party.PartyService;
import peony.game.pk.PkService;
import peony.game.question.QuestionService;
import peony.game.roll.RollService;
import peony.game.weather.WeatherService;
import peony.marriage.MarriageService;
import peony.marriage.WeddingService;
import peony.net.ClientSessionService;
import peony.net.TrustIpService;
import peony.npc.service.ExchangeNpcService;
import peony.npc.service.NpcService;
import peony.npc.service.PloyNpcService;
import peony.produce.ProduceService;
import peony.service.account.AccountService;
import peony.service.account.AccountStatService;
import peony.service.account.RecordChargeService;
import peony.service.accountbinding.AccountBindingService;
import peony.service.activity.ActivityService;
import peony.service.chinajoy.ChinaJoyService;
import peony.service.expansionbattle.ExpansionService;
import peony.service.fame.FameService;
import peony.service.friend.RelationService;
import peony.service.jetty.JettyService;
import peony.service.levellimit.LevelLimitService;
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
import peony.service.version.ModelService;
import peony.service.version.VersionService;
import peony.service.worldmap.WorldMapService;
import peony.stat.StatisticsService;
import peony.teleport.service.TeleportService;

/**
 * 管理所有的服务，并允许通过类名索引服务对象。
 * @author lighthu
 */
public class ServiceRegistry {
	// 日志
	protected static Logger log = Logger.getLogger(ServiceRegistry.class);
	
	public static ServiceRegistry reg = null;
	
	// 类名->服务对象的映射表
	protected Map<String, Service> services = new HashMap<String, Service>();
	
	/*
	 * 下面的这些成员变量是为了提高访问效率而设置的。
	 */
	protected LevelLimitService levelLimitService;
	protected PacketHandlerService packetHandlerService;
	protected AccountService accountService;
	protected AuctionService auctionService;
	protected DBService dbService;
	protected PlayerService playerService;
	protected ChatService chatService;
	protected ChannelService channelService;
	protected PartyService partyService;
	protected ClientSessionService clientSessionService;
	protected ActorCacheService actorCacheService;
	protected RelationService relationService;
	protected TongService tongService;
	protected DataService dataService;
	protected PkService pkService;
	protected RollService rollService;
	protected ShopService shopService;
	protected MailService mailService;
	protected TrustIpService trustIpService;
	protected SleepyCatService sleepyCatService;
	protected VersionService versionService;
	protected AdminService adminService;
	protected ExchangeService exchangeService;
	protected StatisticsService statisticsService;
	protected GiftService giftService;
	protected PropertyService propertyService;
	protected MarriageService marriageService;
	protected FlagBattleFieldVMapManager flagVMapManager;
	protected JettyService jettyService;
	protected ProduceService produceService;
	protected JewelService jewelService;
	protected ModelService modelService;
	protected TeleportService teleportService;
	protected DepotService depotService;
	protected ChinaJoyService chinaJoyService;
	protected NationService nationService;
	protected StatService statService;
	protected NormalVMapManager normalVMapManager;
	protected CandidateService candidateService;
	protected AccountBindingService accountBindingService;
	protected ExpService expService;
	protected WeatherService weatherService;
	protected WorldMapService worldMapService;
	protected FileService fileService;
	protected NationConvoyService nationConvoyService;
	protected QuestionService questionService;
	protected BossScoreService bossScoreService;
	protected GameChannelService gameChannelService;
	protected NpcService npcService;
	protected PloyNpcService ployNpcService;
	protected ExchangeNpcService exchangeNpcService;
	protected AccountService slaveAccountService;
	protected TongBattleApplyService tongBattleApplyService;
	protected TongBattleVMapManager tongBattleVMapManager;
	protected RealtimeStatService realtimeStatService;
	protected AccountStatService accountStatService;
	protected ActivityService activityService;
	protected ClientBbsService clientBbsService;
	protected KillPlayerService killPlayerService;
	protected GameCityService gameCityService;
	protected BeautyParadeService beautyParadeService;
	protected RecordChargeService recordChargeService;
	protected WeddingService weddingService; 
	protected ExpansionService expansionService;
	protected FameService fameService;
	
	public ServiceRegistry(){
		reg = this;
	}

	/**
	 * 注册一个服务。一个类型的服务同时只能有一个。
	 * @param service
	 */
	public void addService(Service service) {
		// 如果有旧的服务，关闭之。
		String name = service.getClass().getName();
		Service oldService = services.get(name);
		if (oldService != null) {
			try {
				oldService.shutdown();
			} catch (Exception e) {
				log.error(e, e);
			}
		}
		
		// 保存新的服务
		services.put(name, service);
		
		// 设置快捷访问变量
		if (service instanceof PacketHandlerService) {
			packetHandlerService = (PacketHandlerService)service;
		} else if (service instanceof AccountService) {
			accountService = (AccountService)service;
		} else if (service instanceof DBService) {
			dbService = (DBService)service;
		} else if (service instanceof PlayerService) {
			playerService = (PlayerService)service;
		} else if (service instanceof ChatService) {
			chatService = (ChatService)service;
		} else if (service instanceof ChannelService) {
			channelService = (ChannelService)service;
		} else if (service instanceof PartyService) {
			partyService = (PartyService)service;
		} else if (service instanceof ActorCacheService) {
			actorCacheService = (ActorCacheService)service;
		} else if (service instanceof RelationService) {
			relationService = (RelationService)service;
		} else if (service instanceof TongService) {
			tongService = (TongService)service;
		}	else if (service instanceof DataService) {
			dataService = (DataService)service;
		} else if (service instanceof PkService){
			pkService = (PkService)service;
		} else if (service instanceof RollService){
			rollService = (RollService)service;
		} else if (service instanceof ShopService) {
			shopService = (ShopService)service;
		} else if (service instanceof MailService) {
			mailService = (MailService)service;
		} else if (service instanceof TrustIpService) {
			trustIpService = (TrustIpService)service;
		} else if (service instanceof SleepyCatService){
			sleepyCatService = (SleepyCatService)service;
		} else if (service instanceof VersionService){
			versionService = (VersionService)service;
		} else if (service instanceof AdminService){
			adminService = (AdminService)service;
		} else if (service instanceof ExchangeService){
			exchangeService = (ExchangeService)service;
		} else if (service instanceof StatisticsService){
			statisticsService = (StatisticsService)service;
		}else if(service instanceof AuctionService){
			auctionService = (AuctionService)service;
		}else if(service instanceof GiftService){
			giftService = (GiftService)service;
		}else if(service instanceof PropertyService){
			propertyService = (PropertyService)service;
		}else if(service instanceof MarriageService){
			marriageService = (MarriageService)service;
		}else if(service instanceof FlagBattleFieldVMapManager){
			flagVMapManager = (FlagBattleFieldVMapManager)service;
		}else if(service instanceof JettyService){
			jettyService = (JettyService)service;
		}else if(service instanceof ProduceService){
			produceService = (ProduceService)service;
		}else if(service instanceof JewelService) {
		    jewelService = (JewelService)service;
		}else if(service instanceof ModelService) {
            modelService = (ModelService)service;
        }else if(service instanceof TeleportService) {
            teleportService = (TeleportService)service;
        }else if(service instanceof DepotService) {
            depotService = (DepotService)service;
        }else if(service instanceof ChinaJoyService){
        	chinaJoyService = (ChinaJoyService)service;
        }else if(service instanceof NationService){
        	nationService = (NationService)service;
        }else if(service instanceof StatService){
        	statService = (StatService)service;
        }else if(service instanceof NormalVMapManager){
        	normalVMapManager = (NormalVMapManager)service;
        }else if(service instanceof CandidateService){
        	candidateService = (CandidateService)service;
        } else if(service instanceof AccountBindingService){
        	accountBindingService = (AccountBindingService)service;
        }
        else if(service instanceof LevelLimitService){
        	levelLimitService=(LevelLimitService)service;
        }else if(service instanceof ExpService){
        	expService = (ExpService)service;
        }else if(service instanceof WeatherService){
        	weatherService = (WeatherService)service;
        }else if(service instanceof WorldMapService){
        	worldMapService = (WorldMapService)service;
        }else if(service instanceof FileService){
        	fileService = (FileService)service;
        }else if(service instanceof NationConvoyService){
        	nationConvoyService = (NationConvoyService)service;
        }else if(service instanceof QuestionService){
        	questionService = (QuestionService)service;
        }else if(service instanceof BossScoreService){
        	bossScoreService = (BossScoreService)service;
        }else if(service instanceof GameChannelService){
        	gameChannelService = (GameChannelService)service;
        }else if(service instanceof NpcService){
        	npcService = (NpcService)service;
        }else if(service instanceof PloyNpcService){
        	ployNpcService = (PloyNpcService)service;
        }else if(service instanceof ExchangeNpcService){
        	exchangeNpcService = (ExchangeNpcService)service;
        }else if(service instanceof TongBattleApplyService){
        	tongBattleApplyService = (TongBattleApplyService)service;
        }else if(service instanceof TongBattleVMapManager){
        	tongBattleVMapManager = (TongBattleVMapManager)service;
        } else if (service instanceof RealtimeStatService) {
        	realtimeStatService = (RealtimeStatService)service;
        } else if (service instanceof AccountStatService) {
        	accountStatService = (AccountStatService)service;
        } else if (service instanceof ActivityService) {
        	activityService = (ActivityService)service;
        } else if (service instanceof ClientBbsService) {
        	clientBbsService = (ClientBbsService)service;
        } else if (service instanceof KillPlayerService){
        	killPlayerService = (KillPlayerService)service;
        } else if (service instanceof GameCityService){
        	gameCityService = (GameCityService)service;
        } else if (service instanceof BeautyParadeService) {
        	beautyParadeService = (BeautyParadeService)service;
        } else if (service instanceof RecordChargeService) {
        	recordChargeService = (RecordChargeService)service;
        } else if (service instanceof WeddingService) {
        	weddingService = (WeddingService)service;
        } else if (service instanceof ExpansionService) {
        	expansionService = (ExpansionService)service;
        } else if(service instanceof FameService) {
        	fameService = (FameService)service;
        }
	}

	/**
	 * 遍历所有当前载入的服务。
	 * @return
	 */
	public Iterator<Service> iterator() {
		return services.values().iterator();
	}
	
	/**
	 * 关闭所有服务。
	 */
	public void shutdown() {
		for (Service service: services.values()) {
			try {
				if (service != sleepyCatService) {
					service.shutdown();
				}
			} catch (Exception e) {
				log.error(e, e);
			}
		}
		try {
			sleepyCatService.shutdown();
		} catch (Exception e) {
			log.error(e, e);
		}
		services.clear();
	}
	
	public void setSlaveAccountService(AccountService slaveAccountService){
		this.slaveAccountService = slaveAccountService;
	}
	
	/**
	 * 根据类查找服务。
	 * @param cls
	 * @return
	 */
	public Service getService(Class cls) {
		return services.get(cls.getName());
	}
   
	public PacketHandlerService getPacketHandlerService() {
		return packetHandlerService;
	}

	public AccountService getAccountService() {
		return accountService;
	}
	public LevelLimitService getLevelLimitService() {
		return levelLimitService;
	}
	
	public AuctionService getAuctionService() {
		return auctionService;
	}
	
	public MarriageService getMarriageService() {
		return marriageService;
	}

	public DBService getDbService() {
		return dbService;
	}

	public PlayerService getPlayerService() {
		return playerService;
	}

	public ChatService getChatService() {
		return chatService;
	}

	public ChannelService getChannelService() {
		return channelService;
	}

	public PartyService getPartyService() {
		return partyService;
	}
	
	public ActorCacheService getActorCacheService() {
		return actorCacheService;
	}
	
	public RelationService getRelationService() {
		return relationService;
	}
	
	public TongService getTongService() {
		return tongService;
	}
	
	public DataService getDataService(){
		return dataService;
	}
	
	public PkService getPkService(){
		return pkService;
	}
	
	public RollService getRollService(){
		return rollService;
	}
	
	public ShopService getShopService() {
		return shopService;
	}
	
	public MailService getMailService() {
		return mailService;
	}
	
	public TrustIpService getTrustIpService() {
		return trustIpService;
	}
	
	public SleepyCatService getSleepyCatService(){
		return sleepyCatService;
	}
	
	public VersionService getVersionService(){
		return versionService;
	}
	
	public AdminService getAdminService(){
		return adminService;
	}
	
	public ExchangeService getExchangeService(){
		return exchangeService;
	}
	
	public StatisticsService getStatisticsService(){
		return statisticsService;
	}
	
	public GiftService getGiftService(){
		return giftService;
	}
	
	public PropertyService getPropertyService(){
		return propertyService;
	}
	
	public FlagBattleFieldVMapManager getFlagBattleFieldVMapManager(){
		return flagVMapManager;
	}
	
	public JettyService getJettyService(){
		return jettyService;
	}
	
	public ProduceService getProduceService(){
		return produceService;
	}
	
	public JewelService getJewelService() {
	    return jewelService;
	}
	
	public ModelService getModelService() {
	    return modelService;
	}
	
	public TeleportService getTeleportService() {
	    return teleportService;
	}
	
	public DepotService getDepotService() {
	    return depotService;
	}
	
	public ChinaJoyService getChinaJoyService(){
		return chinaJoyService;
	}
	
	public NationService getNationService(){
		return nationService;
	}
	
	public StatService getStatService(){
		return statService;
	}
	
	public NormalVMapManager getNormalVMapManager(){
		return normalVMapManager;
	}
	
	public CandidateService getCandidateService(){
		return candidateService;
	}
	
	public AccountBindingService getAccountBindingService(){
		return accountBindingService;
	}
	
	public ExpService getExpService(){
		return expService;
	}
	
	public WeatherService getWeatherService(){
		return weatherService;
	}
	
	public WorldMapService getWorldMapService(){
		return worldMapService;
	}
	
	public FileService getFileService(){
		return fileService;
	}
	
	public NationConvoyService getNationConvoyService(){
		return nationConvoyService;
	}
	
	public QuestionService getQuestionService(){
		return questionService;
	}
	
	public BossScoreService getBossScoreService(){
		return bossScoreService;
	}
	
	public GameChannelService getGameChannelService(){
		return gameChannelService;
	}
	
	public NpcService getNpcService(){
		return npcService;
	}
	
	public PloyNpcService getPloyNpcService(){
		return ployNpcService;
	}
	
	public ExchangeNpcService getExchangeNpcService(){
		return exchangeNpcService;
	}
	
	public AccountService getSlaveAccountService(){
		return slaveAccountService;
	}
	
	public TongBattleApplyService getTongBattleApplyService(){
		return tongBattleApplyService;
	}
	
	public TongBattleVMapManager getTongBattleVMapManager(){
		return tongBattleVMapManager;

	}
	public RealtimeStatService getRealtimeStatService() {
		return realtimeStatService;
	}
	
	public AccountStatService getAccountStatService() {
		return accountStatService;
	}
	
	public ActivityService getActivityService() {
		return activityService;
	}
	
	public ClientBbsService getClientBbsService() {
		return clientBbsService;
	}
	
	public KillPlayerService getKillPlayerService(){
		return killPlayerService;
	}
	
	public GameCityService getGameCityService(){
		return gameCityService;
	}
	
	public BeautyParadeService getBeautyParadeService(){
		return beautyParadeService;
	}
	
	public RecordChargeService getRecordChargeService(){
		return recordChargeService;
	}
	
	public WeddingService getWeddingService(){
		return weddingService;
	}
	
	public ExpansionService getExpansionService(){
		return expansionService;
	}
	
	public FameService getFameService(){
		return fameService;
	}
	
}
