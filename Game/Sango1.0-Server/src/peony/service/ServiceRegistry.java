package peony.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import peony.alchemy.AlchemyService;
import peony.auction.AuctionService;
import peony.channel.ChannelService;
import peony.clientguid.ClientGuidService;
import peony.db.DBService;
import peony.db.SyncExecutorService;
import peony.db.WeiboExecutorService;
import peony.depot.DepotService;
import peony.game.AccountDepotService;
import peony.game.DataService;
import peony.game.PropertyService;
import peony.game.actlead.ActLeaderService;
import peony.game.admin.AdminService;
import peony.game.association.AssociationService;
import peony.game.asyncbattle.AsyncBattleService;
import peony.game.attendant.AttendantFixService;
import peony.game.battlefield.FlagBattleFieldVMapManager;
import peony.game.beautyparade.BeautyParadeService;
import peony.game.chat.ChatService;
import peony.game.clientbbs.ClientBbsService;
import peony.game.convoy.NationConvoyService;
import peony.game.directory.DirectoryService;
import peony.game.exchange.ExchangeService;
import peony.game.exp.ExpService;
import peony.game.file.FileService;
import peony.game.gift.FetchGiftService;
import peony.game.gift.GameChannelService;
import peony.game.gift.GameCityService;
import peony.game.gift.GiftService;
import peony.game.instance.BossScoreService;
import peony.game.instance.InstanceSweepService;
import peony.game.instance.NormalVMapManager;
import peony.game.instance.WomenDayInstanceService;
import peony.game.itemenhance.JewelService;
import peony.game.mail.MailService;
import peony.game.nation.CandidateService;
import peony.game.nation.NationService;
import peony.game.notification.NotificationService;
import peony.game.party.PartyService;
import peony.game.pk.PkService;
import peony.game.question.QuestionService;
import peony.game.roll.RollService;
import peony.game.salary.SalaryService;
import peony.game.stepserver.StepBattleService;
import peony.game.stepserver.StepClient;
import peony.game.stepserver.StepServer;
import peony.game.stepserver.StepSessionService;
import peony.game.weather.WeatherService;
import peony.gatecard.GateCardService;
import peony.marriage.AskForGiftService;
import peony.marriage.MarriageService;
import peony.marriage.WeddingService;
import peony.mobiphone.TelcoChargeService;
import peony.net.ClientSessionService;
import peony.net.IpdService;
import peony.net.TrustIpService;
import peony.npc.service.ExchangeNpcService;
import peony.npc.service.NpcService;
import peony.npc.service.PloyNpcService;
import peony.produce.ProduceService;
import peony.service.VIP.VipPrivilegeService;
import peony.service.account.AccountService;
import peony.service.account.AccountStatService;
import peony.service.account.ChargeActivityService;
import peony.service.account.ChargeInfoService;
import peony.service.account.RecordChargeService;
import peony.service.accountbinding.AccountBindingService;
import peony.service.activity.ActivityService;
import peony.service.activity.AnniversaryService;
import peony.service.activity.AwardActivityService;
import peony.service.activity.CardPunchActService;
import peony.service.activity.SendNewYearPrayService;
import peony.service.activity.TenthAnniversaryService;
import peony.service.apprentice.ApprenticeService;
import peony.service.award.AwardService;
import peony.service.cards.CardService;
import peony.service.chinajoy.ChinaJoyService;
import peony.service.duel.DuelService;
import peony.service.duelmetting.DuelMettingService;
import peony.service.enhance.EnhanceService;
import peony.service.exam.ExamService;
import peony.service.expansionbattle.ExpansionService;
import peony.service.fame.FameService;
import peony.service.feast.FeastInstanceService;
import peony.service.fiveelement.FiveElementService;
import peony.service.friend.RelationService;
import peony.service.gamble.GambleService;
import peony.service.jetty.JettyService;
import peony.service.levellimit.LevelLimitService;
import peony.service.nationDayActivity.NationDayService;
import peony.service.onlinetime.PlayerOnlineTimeService;
import peony.service.player.ActorCacheService;
import peony.service.player.KillPlayerService;
import peony.service.player.PlayerService;
import peony.service.pluginstance.AutoManInstanceService;
import peony.service.pluginstance.ChessInstanceService;
import peony.service.pluginstance.GoInstanceServcie;
import peony.service.pluginstance.LanternFestivalService;
import peony.service.pluginstance.MayDayFestivalService;
import peony.service.pluginstance.MouseInstanceService;
import peony.service.quest.EscortQuestService;
import peony.service.ranking.RankingService;
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
import peony.service.weibo.WeiboService;
import peony.service.welfare.WelfareService;
import peony.service.worldmap.WorldMapService;
import peony.stat.StatisticsService;
import peony.teleport.service.TeleportService;
import peony.vtc.charge.VtcCardChargeService;

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
	protected NationDayService nationDayService;
	protected DuelService duelService;
	protected TorchService torchService;
	protected CardService cardService;
	protected TowerDefendService towerDefendService;
	protected ActLeaderService actLeaderService;
	protected EnhanceService enhanceService;
	protected AssociationService associationService;
	protected ChargeInfoService chargeInfoService;
	protected ClearanceSaleService clearanceSaleService;
	protected FetchGiftService fetchGiftService;
	protected AccountDepotService accountDepotService;
	protected LogPlayeActionService logPlayerActionService;
	protected GateCardService gateCardService;
	protected QuestRewardService questRewardService;
	protected SyncExecutorService syncExecutorService;
	protected IpdService ipdService;
	protected TelcoChargeService telcoChargeService;
	protected ItemTrackService itemTrackService;
	protected WelfareService welfareService;
	protected MouseInstanceService mouseInstanceService;
	protected AutoManInstanceService autoManInstanceService;
	protected GoInstanceServcie goInstanceService;
	protected DuelMettingService duelMettingService;
	protected ApprenticeService apprenticeService;
	protected VtcCardChargeService vtcCardChargeService;
	protected BugProcessService bugProcessService;
	protected ClientGuidService clientGuidService;
	protected WeiboService weiboService;
	protected CycleInstanceMapManager cycleInstanceMapManager;
	protected AwardService awardService;
	protected PlayerOnlineTimeService playerOnlineTimeService;
	protected MonthlyPayService monthlyPayService;
	protected AskForGiftService askForGiftService;
	protected RankingService rankingService;
	protected WeiboExecutorService weiboExecutorService;
	protected DirectoryService directoryService;
	protected SalaryService salaryService;
	protected LanternFestivalService lanternFestivalService;
	protected FiveElementService fiveElementService;
	protected InstanceSweepService instanceSweepService;
	protected EscortQuestService escortQuestService;
	protected NotificationService notificationService;
	protected MayDayFestivalService mayDayFestivalService;
	protected GambleService gambleService;
	protected AnniversaryService anniversaryService;
	protected FeastInstanceService feastInstanceService;
	protected ChessInstanceService chessInstanceService;
	protected StepServer stepServer;
	protected StepClient stepClient;
	protected StepSessionService stepSessionService;
	protected StepBattleService stepBattleService;
	protected RewardBagService rewardBagService;
	protected AttendantFixService attendantFixService;
	protected ChargeActivityService chargeSystemService;
	protected CardPunchActService cardPunchActService;
	protected AwardActivityService awardActivityService;
	protected VipPrivilegeService vipPrivilegeService;
	protected SendNewYearPrayService sendNewYearPrayService;
	protected WomenDayInstanceService womenDayInstanceService;
	protected AlchemyService alchemyService;
//	protected TenthAnniversaryService tenthAnniversaryService;
	protected AsyncBattleService asyncBattleService;
	protected ExamService examService;
	
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
        }else if(service instanceof AccountBindingService){
        	accountBindingService = (AccountBindingService)service;
        }else if(service instanceof LevelLimitService){
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
        } else if(service instanceof NationDayService){
        	nationDayService = (NationDayService)service;
        } else if(service instanceof DuelService){
        	duelService = (DuelService)service;
        } else if(service instanceof TorchService){
        	torchService = (TorchService)service;
        } else if(service instanceof CardService){
        	cardService = (CardService)service;
        } else if(service instanceof TowerDefendService){
        	towerDefendService = (TowerDefendService)service;
        } else if(service instanceof ActLeaderService){
        	actLeaderService = (ActLeaderService)service;
        } else if(service instanceof EnhanceService){
        	enhanceService = (EnhanceService)service;
        } else if(service instanceof AssociationService){
        	associationService = (AssociationService)service;
        } else if(service instanceof ChargeInfoService){
        	chargeInfoService = (ChargeInfoService)service;
        } else if(service instanceof ClearanceSaleService){
        	clearanceSaleService = (ClearanceSaleService)service;
        } else if(service instanceof FetchGiftService){
        	fetchGiftService = (FetchGiftService)service;
        } else if(service instanceof AccountDepotService){
        	accountDepotService = (AccountDepotService)service;
        } else if(service instanceof LogPlayeActionService){
        	logPlayerActionService = (LogPlayeActionService)service;
        } else if(service instanceof GateCardService){
        	gateCardService = (GateCardService)service;
        } else if(service instanceof QuestRewardService){
        	questRewardService = (QuestRewardService)service;
        } else if(service instanceof SyncExecutorService){
        	syncExecutorService = (SyncExecutorService)service;
        } else if (service instanceof IpdService) {
        	ipdService = (IpdService)service;
        } else if(service instanceof TelcoChargeService){
        	telcoChargeService = (TelcoChargeService)service;
        } else if(service instanceof ItemTrackService){
        	itemTrackService = (ItemTrackService)service;
        } else if(service instanceof WelfareService){
        	welfareService = (WelfareService)service;
        } else if(service instanceof MouseInstanceService){
        	mouseInstanceService = (MouseInstanceService)service;
        } else if(service instanceof AutoManInstanceService){
        	autoManInstanceService = (AutoManInstanceService)service;
        } else if(service instanceof GoInstanceServcie){
        	goInstanceService = (GoInstanceServcie)service;
        } else if(service instanceof DuelMettingService){
        	duelMettingService = (DuelMettingService)service;
        } else if(service instanceof ApprenticeService){
        	apprenticeService = (ApprenticeService)service;
        } else if(service instanceof VtcCardChargeService){
        	vtcCardChargeService = (VtcCardChargeService)service;
        } else if(service instanceof BugProcessService){
        	bugProcessService = (BugProcessService)service;
        } else if(service instanceof ClientGuidService){
        	clientGuidService = (ClientGuidService)service;
        } else if(service instanceof WeiboService){
        	weiboService = (WeiboService)service;
        } else if(service instanceof CycleInstanceMapManager){
        	cycleInstanceMapManager = (CycleInstanceMapManager)service;
        } else if(service instanceof AwardService){
        	awardService = (AwardService)service;
        } else if(service instanceof PlayerOnlineTimeService){
        	playerOnlineTimeService = (PlayerOnlineTimeService)service;
        } else if(service instanceof MonthlyPayService){
        	monthlyPayService = (MonthlyPayService)service;
        } else if(service instanceof AskForGiftService){
        	askForGiftService = (AskForGiftService)service;
        } else if(service instanceof RankingService){
        	rankingService = (RankingService)service;
        } else if(service instanceof WeiboExecutorService){
        	weiboExecutorService = (WeiboExecutorService)service;
        } else if(service instanceof DirectoryService){
        	directoryService = (DirectoryService)service;
        } else if(service instanceof SalaryService){
        	salaryService = (SalaryService)service;
        } else if(service instanceof LanternFestivalService){
        	lanternFestivalService =(LanternFestivalService)service;
        } else if(service instanceof FiveElementService){
        	fiveElementService = (FiveElementService)service;
        } else if(service instanceof InstanceSweepService){
        	instanceSweepService = (InstanceSweepService)service;
        } else if(service instanceof EscortQuestService){
        	escortQuestService =(EscortQuestService)service;
        } else if(service instanceof NotificationService){
        	notificationService = (NotificationService)service;
        } else if(service instanceof MayDayFestivalService){
        	mayDayFestivalService = (MayDayFestivalService)service;
        } else if(service instanceof GambleService){
        	gambleService = (GambleService)service;
        } else if(service instanceof AnniversaryService){
        	anniversaryService = (AnniversaryService)service;
        } else if(service instanceof FeastInstanceService){
        	feastInstanceService = (FeastInstanceService)service;
        } else if(service instanceof ChessInstanceService){
        	chessInstanceService = (ChessInstanceService)service;
        } else if(service instanceof StepServer){
        	stepServer = (StepServer)service;
        } else if(service instanceof StepClient){
        	stepClient = (StepClient)service;
        } else if(service instanceof StepSessionService){
        	stepSessionService = (StepSessionService)service;
        } else if(service instanceof StepBattleService){
        	stepBattleService = (StepBattleService)service;
        } else if(service instanceof RewardBagService){
        	rewardBagService = (RewardBagService)service;
        } else if(service instanceof AttendantFixService){
        	attendantFixService = (AttendantFixService)service;
        } else if(service instanceof ChargeActivityService){
        	chargeSystemService = (ChargeActivityService)service;
        } else if(service instanceof CardPunchActService){
        	cardPunchActService = (CardPunchActService)service;
        } else if(service instanceof AwardActivityService){
        	awardActivityService = (AwardActivityService)service;
        } else if(service instanceof VipPrivilegeService){
        	vipPrivilegeService = (VipPrivilegeService)service;
        }else if(service instanceof SendNewYearPrayService){
        	sendNewYearPrayService =(SendNewYearPrayService)service;
        }else if(service instanceof WomenDayInstanceService){
        	womenDayInstanceService = (WomenDayInstanceService)service;
        }else if(service instanceof AlchemyService){
        	alchemyService = (AlchemyService)service;
//        }else if(service instanceof TenthAnniversaryService){
//       	tenthAnniversaryService=(TenthAnniversaryService)service;
        }else if(service instanceof AsyncBattleService){
        	asyncBattleService = (AsyncBattleService)service;
        } else if(service instanceof ExamService){
        	examService = (ExamService)service;
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
		try {
			playerService.shutdown();
		} catch (Exception e) {
			log.error(e, e);
		}
		try {
			relationService.shutdown();
		} catch (Exception e) {
			log.error(e, e);
		}
		try {
			tongService.shutdown();
		} catch (Exception e) {
			log.error(e, e);
		}
		try {
			nationService.shutdown();
		} catch (Exception e) {
			log.error(e, e);
		}
		try {
			bossScoreService.shutdown();
		} catch (Exception e) {
			log.error(e, e);
		}
		try {
			tongBattleApplyService.shutdown();
		} catch (Exception e) {
			log.error(e, e);
		}
		try {
			anniversaryService.shutdown();
		} catch (Exception e) {
			log.error(e, e);
		}
		for (Service service: services.values()) {
			try {
				if (service != sleepyCatService && service != playerService && service != relationService &&
						service != tongService && service != nationService && service != bossScoreService &&
						service != tongBattleApplyService && service != anniversaryService) {
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
	
	public NationDayService getNationDayService(){
		return nationDayService;
	}
	
	public DuelService getDuelService(){
		return duelService;
	}
	
	public TorchService getTorchService(){
		return torchService;
	}
	
	public CardService getCardService(){
		return cardService;
	}
	
	public TowerDefendService getTowerDefendService(){
		return towerDefendService;
	}
	
	public ActLeaderService getActLeaderService(){
		return actLeaderService;
	}
	
	public EnhanceService getEnhanceService(){
		return enhanceService;
	}
	
	public AssociationService getAssociationService(){
		return associationService;
	}
	
	public ChargeInfoService getChargeInfoService(){
		return chargeInfoService;
	}
	
	public ClearanceSaleService getClearanceSaleService(){
		return clearanceSaleService;
	}
	
	public FetchGiftService getFetchGiftService(){
		return fetchGiftService;
	}
	
	public AccountDepotService getAccountDepotService(){
		return accountDepotService;
	}
	
	public LogPlayeActionService getLogPlayerActionService(){
		return logPlayerActionService;
	}
	
	public GateCardService getGateCardService(){
		return gateCardService;
	}
	
	public QuestRewardService getQuestRewardService(){
		return questRewardService;
	}
	
	public SyncExecutorService getSyncExecutorService(){
		return syncExecutorService;
	}
	
	public IpdService getIpdService() {
		return ipdService;
	}
	
	public TelcoChargeService getTelcoChargeService(){
		return telcoChargeService;
	}
	
	public ItemTrackService getItemTrackService(){
		return itemTrackService;
	}
	
	public WelfareService getWelfareService() {
		return welfareService;
	}
	
	public MouseInstanceService getMouseInstanceService(){
		return mouseInstanceService;
	}
	
	public AutoManInstanceService getAutoManInstanceService(){
		return autoManInstanceService;
	}
	
	public GoInstanceServcie getGoInstanceServcie(){
		return goInstanceService;
	}
	
	public DuelMettingService getDuelMettingService() {
		return duelMettingService;
	}
	
	public ApprenticeService getApprenticeService(){
		return apprenticeService;
	}
	
	public VtcCardChargeService getVtcCardChargeService(){
		return vtcCardChargeService;
	}
	
	public BugProcessService getBugProcessService(){
		return bugProcessService;
	}
	
	public ClientGuidService getClientGuidService(){
		return clientGuidService;
	}
	
	public WeiboService getWeiboService(){
		return weiboService;
	}
	
	public CycleInstanceMapManager getCycleInstanceMapManager(){
		return cycleInstanceMapManager;
	}
	
	public AwardService getAwardService(){
		return awardService;
	}
	
	public PlayerOnlineTimeService getPlayerOnlineTimeService(){
		return playerOnlineTimeService;
	}
	
	public MonthlyPayService getMonthlyPayService(){
		return monthlyPayService;
	}
	
	public AskForGiftService getAskForGiftService(){
		return askForGiftService;
	}
	
	public RankingService getRankingService(){
		return rankingService;
	}
	
	public WeiboExecutorService getWeiboExecutorService(){
		return weiboExecutorService;
	}
	
	public DirectoryService getDirectoryService(){
		return directoryService;
	}
	
	public SalaryService getSalaryService(){
		return salaryService;
	}
	
	public LanternFestivalService getLanternFestivalService(){
		return lanternFestivalService;
	}
	
	public FiveElementService getFiveElementService(){
		return fiveElementService;
	}
	
	public InstanceSweepService getInstanceSweepService(){
		return instanceSweepService;
	}
	
	public EscortQuestService getEscortQuestService(){
		return escortQuestService;
	}
	
	public NotificationService getNotificationService(){
		return notificationService;
	}
	
	public MayDayFestivalService getMayDayFestivalService(){
		return mayDayFestivalService;
	}
	
	public GambleService getGambleService(){
		return gambleService;
	}
	
	public AnniversaryService getAnniversaryService(){
		return anniversaryService;
	}
	
	public FeastInstanceService getFeastInstanceService(){
		return feastInstanceService;
	}
	
	public ChessInstanceService getChessInstanceService(){
		return chessInstanceService;
	}
	
	public StepServer getStepServer(){
		return stepServer;
	}
	
	public StepClient getStepClient(){
		return stepClient;
	}
	
	public StepSessionService getStepSessionService(){
		return stepSessionService;
	}
	
	public StepBattleService getStepBattleService(){
		return stepBattleService;
	}
	
	public RewardBagService getRewardBagService(){
		return rewardBagService;
	}
	
	public AttendantFixService getAttendantFixService(){
		return attendantFixService;
	}
	
	public ChargeActivityService getChargeActivityService(){
		return chargeSystemService;
	}
	
	public CardPunchActService getCardPunchActService(){
		return cardPunchActService;
	}
	
	public AwardActivityService getAwardActivityService(){
		return awardActivityService;
	}
	
	public VipPrivilegeService getVipPrivilegeService(){
		return vipPrivilegeService;
	}
	
	public SendNewYearPrayService getSendNewYearPrayService(){
		return sendNewYearPrayService;
	}
	
	public WomenDayInstanceService getWomenDayInstanceService(){
		return womenDayInstanceService;
	}
	
	public AlchemyService getAlchemyService(){
		return alchemyService;
	}
//	public TenthAnniversaryService getTenthAnniversaryService(){
//		return tenthAnniversaryService;
//	}
	
	public AsyncBattleService getAsyncBattleService(){
		return asyncBattleService;
	}
	
	public ExamService getExamService(){
		return examService;
	}
	
}
