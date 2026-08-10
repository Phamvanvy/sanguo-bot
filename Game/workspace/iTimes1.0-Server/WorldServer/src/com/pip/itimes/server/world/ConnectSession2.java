package com.pip.itimes.server.world;

import java.text.MessageFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.common.IoSession;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.ServerConstants;
import com.pip.itimes.net.UWAPData;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.server.bean.ArenaTeam;
import com.pip.itimes.server.bean.ArenaTeam2Player;
import com.pip.itimes.server.bean.CampCandidate;
import com.pip.itimes.server.bean.CampQualification;
import com.pip.itimes.server.bean.Friends;
import com.pip.itimes.server.bean.Gift;
import com.pip.itimes.server.bean.House;
import com.pip.itimes.server.bean.Ibuy;
import com.pip.itimes.server.bean.LeaveMessage;
import com.pip.itimes.server.bean.Mail;
import com.pip.itimes.server.bean.Master;
import com.pip.itimes.server.bean.Mate;
import com.pip.itimes.server.bean.Petmanager;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.bean.Shop;
import com.pip.itimes.server.bean.Tong;
import com.pip.itimes.server.bean.TongIsland;
import com.pip.itimes.server.bean.Vote;
import com.pip.itimes.server.camp.CampConfig;
import com.pip.itimes.server.camp.CampData;
import com.pip.itimes.server.camp.CampOfficial;
import com.pip.itimes.server.camp.CampSkillData;
import com.pip.itimes.server.camp.CampSkillLevel;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.PlayerDao;
import com.pip.itimes.server.gift.ExchangeData;
import com.pip.itimes.server.gift.ExchangeDefine;
import com.pip.itimes.server.gift.ExchangeGroup;
import com.pip.itimes.server.gift.GiftData;
import com.pip.itimes.server.gift.GiftDefine;
import com.pip.itimes.server.gift.GiftGroup;
import com.pip.itimes.server.gift.GiftGroupAllCount;
import com.pip.itimes.server.gift.GiftGroupData;
import com.pip.itimes.server.gift.GiftGroups;
import com.pip.itimes.server.gift.OnlyGiftDefine;
import com.pip.itimes.server.gift.OnlyGiftGroup;
import com.pip.itimes.server.gift.OnlyGiftGroups;
import com.pip.itimes.server.gift.OnlyGiftNeedItem;
import com.pip.itimes.server.stage.AddFriendFavoriteEffect;
import com.pip.itimes.server.stage.AnniversaryEnhance;
import com.pip.itimes.server.stage.Buf;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.Command;
import com.pip.itimes.server.stage.DropGroup;
import com.pip.itimes.server.stage.DropGroupListEffect;
import com.pip.itimes.server.stage.DropGroups;
import com.pip.itimes.server.stage.DropItem;
import com.pip.itimes.server.stage.Effect;
import com.pip.itimes.server.stage.Enemy;
import com.pip.itimes.server.stage.Enhance;
import com.pip.itimes.server.stage.EquipmentTemplate;
import com.pip.itimes.server.stage.ExtendedItem;
import com.pip.itimes.server.stage.Friend;
import com.pip.itimes.server.stage.GiftItemAutoUseEffect;
import com.pip.itimes.server.stage.Grid;
import com.pip.itimes.server.stage.HouseData;
import com.pip.itimes.server.stage.HousePart;
import com.pip.itimes.server.stage.HouseTemplate;
import com.pip.itimes.server.stage.HouseWaiter;
import com.pip.itimes.server.stage.Houses;
import com.pip.itimes.server.stage.Houses.Styles;
import com.pip.itimes.server.stage.IEffectItem;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.IShopTimeItem;
import com.pip.itimes.server.stage.IStoreGroups;
import com.pip.itimes.server.stage.IStoreItem;
import com.pip.itimes.server.stage.IValuableItem;
import com.pip.itimes.server.stage.Instanceadd;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.NoDoor;
import com.pip.itimes.server.stage.Pet;
import com.pip.itimes.server.stage.RandomQuestion;
import com.pip.itimes.server.stage.RandomQuestionManager;
import com.pip.itimes.server.stage.RoarEffect;
import com.pip.itimes.server.stage.RoleFaceData;
import com.pip.itimes.server.stage.RoleFaces;
import com.pip.itimes.server.stage.Scene;
import com.pip.itimes.server.stage.ShopData;
import com.pip.itimes.server.stage.TalkEffect;
import com.pip.itimes.server.stage.TemplateGrid;
import com.pip.itimes.server.stage.TransferDoor;
import com.pip.itimes.server.stage.TreasureEffect;
import com.pip.itimes.server.stage.TwelfthLunarConfig;
import com.pip.itimes.server.stage.TwelfthLunarShowInfo;
import com.pip.itimes.server.stage.VoteGiftDefine;
import com.pip.itimes.server.stage.VoteInfo;
import com.pip.itimes.server.stage.VoteInfoHelp;
import com.pip.itimes.server.stage.VoteKingInfo;
import com.pip.itimes.server.stage.VotePlayerGift;
import com.pip.itimes.server.stage.VoteShowInfo;
import com.pip.itimes.server.stage.VotesKing;
import com.pip.itimes.server.stage.voteGiftGroups;
import com.pip.itimes.server.suit.Suits;
import com.pip.itimes.server.util.KeywordsUtil;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.StoreService.CmccHistoryRequest;
import com.pip.itimes.server.world.accountbinging.AccountBingingData;
import com.pip.itimes.server.world.activationcode.ActivationCodeData;
import com.pip.itimes.server.world.aroundchina.ChinaAroundData;
import com.pip.itimes.server.world.aroundchina.ChinaService;
import com.pip.itimes.server.world.battle.arena.ArenaConstants;
import com.pip.itimes.server.world.battle.arena.ArenaException;
import com.pip.itimes.server.world.battle.arena.client.ArenaBattleClient;
import com.pip.itimes.server.world.camp.CampAuctionService;
import com.pip.itimes.server.world.camp.CampMainService;
import com.pip.itimes.server.world.camp.CampVoteService;
import com.pip.itimes.server.world.game.BattleFieldException;
import com.pip.itimes.server.world.game.CampBattlefieldConfig;
import com.pip.itimes.server.world.game.CampBattlefieldInstance;
import com.pip.itimes.server.world.game.CampBattlefieldPlayer;
import com.pip.itimes.server.world.game.GameMap;
import com.pip.itimes.server.world.game.HouseException;
import com.pip.itimes.server.world.game.HouseInstance;
import com.pip.itimes.server.world.game.Instance;
import com.pip.itimes.server.world.game.InstanceException;
import com.pip.itimes.server.world.lyrics.LoveLyricsConfig;
import com.pip.itimes.server.world.lyrics.LyricsConfig;
import com.pip.itimes.server.world.noahsark.NoahsarkConfig;
import com.pip.itimes.server.world.noahsark.NoahsarkPlayer;
import com.pip.itimes.server.world.question.QuestionControl;
import com.pip.itimes.server.world.riddles.RiddlesConfig;
import com.pip.itimes.server.world.riddles.RiddlesConfig2;
import com.pip.itimes.server.world.sports.SportException;
import com.pip.itimes.server.world.sports.SportRecord;
import com.pip.itimes.server.world.suggest.Suggest;
import com.pip.itimes.server.world.taskHelp.TaskHelp;
import com.pip.itimes.server.world.taskHelp.TaskHelpManager;
import com.pip.itimes.server.world.top.GemTop;
import com.pip.itimes.server.world.toplist.TopListService;
import com.pip.net.message.gameaccount.RecommendRequestMessage;

public abstract class ConnectSession2 extends ConnectSession{

	private Map commandmap = new HashMap();
	
	
	protected Object[][] commands = new Object[][] {
			{ "shop_create", new ShopCreateProcessor() },
			{ "shop_sell", new ShopSellProcessor() },
			{ "shop_buy", new ShopBuyProcessor() },
			{ "shop_buy_cancel", new ShopBuyCancelProcessor() },
			{ "learn_skill", new LearSkillProcessor() },
			{ "property", new PropertyProcessor() },
			{ "tong_create", new TongCreateProcessor() },
			{ "tong_join", new TongJoinProcessor() },
			{ "pet_rename", new PetRenameProcessor() },
			{ "pet_request_point", new PetRequestPointProcessor() },
			{ "pet_request_rename", new PetRequestRenameProcessor() },
			{ "refresh_ability", new RefreshAbilityProcessor() },
			{ "refresh_ability_commit", new RefreshAbilityCommitProcessor() },
			{ "refresh_skill", new RefreshSkillProcessor() },
			{ "refresh_skill_commit", new RefreshSkillCommitProcessor() },
			{ "goto_instance", new GotoInstanceProcessor() },
			{ "roll", new RollProcessor() },
			{ "roll_cancel", new RollCancelProcessor() },
			{ "change_tong_owner", new ChangeTongOwnerProcessor() },
			{ "modifypassword", new ModifyPasswordProcessor() },
			{ "addpropertypoint", new AddPropertyPointProcessor() },
			{ "title", new TitleProcessor() },
			{ "pet_sell", new PetSellProcessor() },
			{ "pet_buy", new PetBuyProcessor() },
			{ "send_message", new SendMessageProcessor() },
			{ "look_package", new LookPackageProcessor() },
			{ "freemove", new FreeMoveProcessor() },
			{ "touch", new TouchProcessor() },
			{ "hello", new HelloProcessor() },
			{ "refresh", new RefreshProcessor() },
			{ "color", new ColorProcessor() },
			{ "battlestatus", new BattleStatusProcessor() },
			{ "rename", new RenameProcessor() },
			{ "renamemerger", new RenameMergerProcessor() },
			{ "bindphone", new BindPhoneProcessor() },
			{ "enterfor", new EnterForProcessor() },
			{ "marry_request", new RequestMarryProcessor() },
			{ "lawunmarry_request", new LawUnMarryRequestProcessor() },
			{ "illegalunmarry_request", new IllegalUnMarryRequestprocessor() },
			{ "marry", new MarrayProcessor() },
			{ "marry_cancel", new CancelMarryProcessor() },
			{ "unmarry", new UnMarryProcessor() },
			{ "unmarry_cancel", new CancelUnMarryProcessor() },
			{ "illegalunmarry", new IllegalUnMarryProcessor() },
			{ "lol_request", new GetLolProcessor() },
			{ "tol_request", new GetTolProcessor() },
			{ "top_request", new GetTopProcessor() },
			{ "tom_request", new GetTomProcessor() },
			{ "master_accept", new MasterAcceptProcessor() },
			{ "master_cancel", new MasterCancelAcceptProcessor() },
			{ "master_request", new RequestMasterProcessor() },
			{ "unmaster", new MasterUnRelationProcessor() },
			{ "single_unmaster_request",
					new MasterRequestIllegalUnRelationProcessor() },
			{ "single_unmaster", new MasterIllegalUnRelationProcessor() },
			{ "guildenterfor", new GuildEnterForProcessor() },
			{ "pre_single_unmaster", new PreSingleUnMasterProcessor() },
			{ "buyface", new BuyFaceProcessor() },
			{ "cmcccharge", new CmccChargeProcessor() },
			{ "itemunmarry", new ItemUnMarryProcessor() },
			{ "item_single_unmaster", new ItemSingleUnMasterProcessor() },
			{ "investigation", new InvestigationProcessor() },
			{ "addfriend", new AddFriendProcessor() },
			{ "house", new HouseProcessor() },
			{ "buyhouse", new BuyHouseProcessor() },
			{ "changestyle", new ChangeStyleProcessor() },
			{ "movehouse", new MoveHouseProcessor() },
			{ "requestbuyhouse", new RequestBuyHouseProcessor() },
			{ "requestchangestyle", new RequestChangeStyleProcessor() },
			{ "requestbuyhouse2", new RequestBuyHouse2Processor() },
			{ "buypart", new BuyPartProcessor() },
			{ "requestpart", new RequestPartProcessor() },
			{ "outhouse", new OutHouseProcessor() },
			{ "leavemessage", new LeaveMessageProcessor() },
			{ "requestchangerule", new RequestChangeRuleProcessor() },
			{ "housegetitem", new HouseGetItemProcessor() },
			{ "changerule", new ChangeRuleProcessor() },
			{ "requestchangehousetitle", new RequestChangeHouseTitleProcessor() },
			{ "changehousetitle", new ChangeHouseTitleProcessor() },
			{ "question_begin_item", new QuestionProcessor("itembegin") },
			{ "question_begin", new QuestionProcessor("begin") },
			{ "question_again", new QuestionProcessor("again") },
			{ "question_answer", new QuestionProcessor("answer") },
			{ "question_pause", new QuestionProcessor("pause") },
			{ "question_next", new QuestionProcessor("next") },
			{ "buywaiter", new BuyWaiterProcessor() },
			{ "toplist_tong", new TongTopListProcessor() },
			{ "toplist_house", new HouseTopListProcessor() },
			{ "waiterhelp", new WaiterHelpProcessor() },
			{ "waiterface", new WaiterFaceProcessor() },
			{ "tongbathhousehelp", new TongBathHouseHelp() },
			{ "tongbathhouse", new TongBathHouse() },
			{ "housepush", new HousePushProcessor() },
			{ "privatehousepush", new PrivateHousePushProcessor() },
			{ "changewaiterface", new ChangeWaiterFaceProcessor() },
			{ "requestmovehouse", new RequestMoveHouseProcessor() },
			{ "ensurebuyhouse", new EnsureBuyHouseProcessor() },
			{ "chargefor", new ChargeForProcessor() },
			{ "log", new LogProcessor() },
			{ "timelimit",new timelimit()},        //  zjl modify
			{ "enemylist", new EnemyListProcessor() },
                        { "sportplay", new SportPlayProcessor()},
                        { "sportover", new SportOverProcessor()},
                        { "house_sp_room", new HouseSpRoomProcessor()},
                        { "requestselectequ", new RequestSelectEquProcessor()},
                        { "requestunenhance", new RequestUnEnhanceProcessor()},
                        { "enhance", new EnhanceProcessor()},
                        { "unenhance", new UnEnhanceProcessor()},
                        { "selectequ", new SelectEquProcessor()},
                        { "selectunenhance", new SelectUnEnhanceProcessor()},
                        { "preenhance", new PreEnhanceProcessor()},
                        { "goisland", new GoIslandProcessor()},
                        { "auctionisland", new AuctionIslandProcessor()},
                        { "leastcredit", new LeastCreditProcessor()},
                        { "auctionislandmessage",new AuctionIslandMessageProcessor()},
                        { "confirmauctionisland", new ConfirmAuctionIslandProcessor()},
                        { "preauctionisland", new PreAuctionIslandProcessor()},
                        { "auctionislandstate", new AuctionIslandStateProcessor()},
                        { "pretongcreate", new PreTongCreateProcessor()},
                        { "preleastcredit", new PreLeastCreditProcessor()},
                        { "islanditem", new IslandItemProcessor()},
                        { "message", new MessageProcessor()},
                        { "cmcc_history",new CmccHistoryProcessor()},
						//mengjie add
                        { "chinaaround", new ChinaAroundProcessor()},
                        { "ibuy10record", new IbuyProcessor()},
                        { "findfriend", new FindfriendProcessor()},
                        { "searchfriend", new SearchfriendProcessor()},
                        { "quittongmenu", new QuittongmenuProcessor()},
                        { "chinagesexy", new ChangesexyProcessor()},
                        { "accountbinding", new AccountBingingProcessor()},
                        { "accountbindingrepeat", new AccountBingingRepeatProcessor()},
                        { "ibuyforother", new IbuyForOtherProcessor()},
                        { "ibuyforotherreturn", new IbuyForOtherReturnProcessor()},
                        { "autobuywaitercancel", new AutoBuyWaiterCancelProcessor()},
                        { "exchangeselectequ", new ExchangeSelectEquProcessor()},
                        { "exchangeokequ", new ExchangeOkEquProcessor()},
                        { "exchangeendequ", new ExchangeEndEquProcessor()},
                        { "showibuytop10", new IbuyShowTop10Processor()},
                        { "ibuytop10", new IbuyTop10Processor()},
                        
                        /**
                         * petversion >= 4将宠物寄养修改为修炼
                         */
//                        { "petmanager", new PetmanagerProcessor()},	// 宠物寄养
//                        { "petmanagerinput", new PetmanagerinputProcessor()},
//                        { "petmanagerout", new PetmanageroutProcessor()},
//                        { "petmanagereat", new PetmanagereatProcessor()},
                        { "enhanceanniversary", new EnhanceAnniversaryProcessor()},
                        { "recommended", new RecommendedProcessor()},
                        { "recommendedresult", new RecommendedResultProcessor()},
                        { "CmccBusinessresult", new CmccBusinessresultProcessor()},
                        { "CmccBusinessok", new CmccBusinessokProcessor()},
                        { "Cmccinfogift", new CmccinfogiftProcessor()},
                        { "ActivationCoderesult", new ActivationCoderesultProcessor()},
                        
                        { "create_arenateam", new ArenaTeamCreateProcessor()},
                        { "view_arenateam", new ArenaTeamViewProcessor()},
                        { "dissolve_arenateam", new ArenaTeamDissolveProcessor()},
                        { "create_arenateam_return", new ArenaTeamCreateReturnProcessor()},
                        { "dissolve_arenateam_return", new ArenaTeamDissolveReturnProcessor()},
                        { "view_arenateaminfo", new ArenaTeamViewInfoProcessor()},
                        { "view_arenateamtop", new ArenaTeamViewTopProcessor()},
                        { "join_arenateam", new ArenaTeamJoinProcessor()},
//                        { "dissolveteam_arenateam", new ArenaTeamDissolveTeamProcessor()},
                        { "lastlogout_addexp", new LastlogoutAddexpProcessor()},
                        { "CMCC_go_recommend", new CMCCgoRecommendProcessor()},
                        { "open_all", new OpenAllProcessor()},
                        { "changeequenhance", new ChangeEquEnhancesProcessor()},
                        { "giftItem", new GiftItemProcessor()},
                        { "updateclientweb", new UpdateClientWebProcessor()},
                        { "worldmap_go", new WorldMapGoProcessor()},
                        { "camp_add", new AddCampProcessor()},
                        { "usePackage", new AddItemProcessor()}, //使用宝石包
                        //mengjie add end
                        //wpjiang add 
                        /**
                         * petversion >= 4 取消炼化功能
                         */
                        /*{ "preEnhancePet", new PreEnhancePetProcessor()},*///精炼宠物
                        /*{ "enhancePet", new EnahancePetProcessor()},*/
                        /*{ "preEnhacePetReady",new PreEnhancePetReadyProcessor()},*/
                        /*{ "enhancePetStart",new EnahancePetStartProcessor()},*/
                        //退化宠物
                        /**
                         * petversion >= 4 取消炼化功能
                         */
                        /*{ "preUnhancePet",new PreUnhancePetProcessor()}, */
                        /*{ "unhancePet",new UnhancePetProcessor()},*/
                        /*{"unhancePetStart",new UnhancePetStartProcessor()},*/
                        {"addFriendFavorite",new AddFriendFavoriteProcessor()},
                        //星装分解
                        {"unhenceequip",new unhenceequipProcessor()},  
                        {"unhanceYearEquip",new unhenceYearEquipProcessor()},
                        {"unhancePlainEquip",new unhencePlainEquipProcessor()},
                        //一次性礼品发放
                        {"giveonlyreword",new GiveOnlyRewordProcessor()},
                        {"givereword",new GiveReworldProcessor()},
                        //邮件里清理白装和绿装
                        {"selectclear",new SelectClearProcessor()},
                        {"sellattachementequip", new sellAttachementEquipPreocessor() },
                        //追踪仇家
                        {"fllowenemys",new FollowEnemysPreocessor()},
                        //1.5版本客户端新加，取仇人列表，同使用高级仇人录一样的效果
                        {"enemyslist",new GetEnemysListPreocessor()},
                        //名人堂装备展示
                        {"ibuytop10equip", new IbuyTop10EquipPreocessor()},
                        //指路宝典
                        {"directway",new DirectWayPreocessor()},
                        //单属性复生石领取
                        {"getOnlyReborn",new GetOnlyRebornPreocessor()},
                        //选举投票
                        {"vote", new VotePreocessor()},
                        //星装属性精炼替换
                        {"insteadenhance", new RequestInsteadEnhancePreocessor()},
                        //战斗放刷答案
                        {"battleQuestion", new BattleQuestionPreocessor()},
                        
                        {"ExchangeGroup", new ExchangeGroupPreocessor()},
                        {"showUIHelp", new ShowUIHelpPreocessor()},
                        //wpjiang add end
                        //leo add
                        {"autouseitem", new AutoUseItemProcessor()},
                        {"get_gift_define", new GiftDefineProcessor()},
                        {"arenasignup", new ArenaSignupProcessor()}, //1v1竞技场排队
                        {"arenacancel", new ArenaCancelProcessor()}, //1v1竞技场排队
                        //leo add end
                        //yfchen add
                        {"voteCamp",new VoteCampProcessor()},		//阵营选举
                        //yfchen add end
                        
                        {"treasure", new TreasureProcessor()},				// 替换藏宝图
                        
                        /**
                         * petversion >= 4 新加宠物重铸大师
                         */
                        // 宠物重铸
                        { "petRecastMaster", new PetRecastMasterProcessor()},
                        { "petRecast", new PetRecastProcessor()},
                        { "petRecastingProperty", new PetRecastingPropertyProcessor()},
                        // 宠物修炼
                        { "petPractice", new PetPracticeProcessor()},
                        // 取走修炼中的宠物
                        { "petPracticeOut", new PetPracticeOutProcessor()},
                        // 选择修炼时间
                        { "petPracticeTime", new PetPracticeTimeProcessor()},
                        
                        /**
                         * Christmas
                         */
                        { "christmas", new ChristmasProcessor(this)},
                        { "unline_exp", new UnlineExpProcessor(this)},
                        { "master", new MasterProcessor(this)},
                        { "campRollcall", new CampRollcallProcessor()},		// 阵营点名活动
                        { "ironChef", new IronChefProcessor()},				// 食神活动(腊八活动)
                        { "openUI", new OpenUIProcessor()},	// 通知客户打开UI
                        { "getTask", new GetTaskProcessor()},	//获取任务\
                        { "battlefieldResources", new BattlefieldResourcesProcessor()},	// 阵营战场：资源争夺战
                        { "exitCampBattlefield", new exitBattlefieldProcessor()},		// 阵营战场：退出战场
                        //追踪仇家
                        {"deleteenemys",new DeleteEnemysPreocessor()},
                        //庄园交互
                        {"farmInteractive", new FarmInteractive(this)},
                        //多层BOSS挑战
                        {"bossRush", new BossRushProcessor()},
                        //丘比特之箭
                        {"TheArrowOfLove",new TheArrowOfLoveProcessor()},
                        {"TheArrowOfLoveOk", new TheArrowOfLoveOkProcessor()},
                        //随缘物语
                        {"LetItBe",new LetItBeProcessor()},
                        {"ChristmasWishing", new ChristmasWishingProcessor()},//圣诞许愿
                        {"PieceReplaceDiamond",new PieceReplaceDiamond()},
                        {"NBShow",new NBShowProcessor()},
                        {"OpenSkyLoveAll",new OpenSkyLoveProcessor()},
                        {"OpenWarBlessingAll",new OpenWarBlessingProcessor()},
                        {"OpenTrainsSignAll",new OpenTrainsSignAll()},
                        {"OpenBlueHeartAll",new OpenBlueHeartAll()},
                        {"FruitRepalceExp",new FruitRepalceExpProcessor()},
                        {"DragonBoatFestivalRepalce",new DragonBoatFestivalReplaceProcessor()},
                        {"gem_top",new GemTopProcessor()},
                        {"noahsark",new NoahsarkProcessor()},
                        {"ChristmasFestivalReplace" ,new ChristmasFestivalReplaceProcessor()}
	};
	
	public ConnectSession2(IoSession session) {
		super(session);
		for (int i = 0; i < commands.length; i++) {
            commandmap.put(commands[i][0], commands[i][1]);
        }
	}

	public void command(UWAPData data) throws Exception {
        WorldPlayer player = getPlayer(data.getSessionId());
        if (player != null) {
            String s = data.readString();
            if(s == null || s.equals("")){
            	log.info("Send Command ID[" + player.getId() + "] command is null");
            	return;
            }
            Command command = new Command(s);
            command.setAppType(data.getAppType());
            command.setSerial(data.getSerial());
            command.setSessionId(data.getSessionId());
            CommandProcessor processor = (CommandProcessor) commandmap.get(
                    command.getCommand());
            if (processor != null) {
                processor.process(player, command);
                log.info("Send Command ID[" + player.getId() + "]" + s);
            } else {
                log.info("Error Command ID[" + player.getId() + "]" + s);
            }
        }
    }
	
	class ShopCreateProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            short areaId = Short.parseShort(command.getParam(0));
            String name = command.getParam(1);
            int money = shopService.getCreateShopMoney(player, areaId);
            try {
                Shop shop = shopService.createShop(player, areaId, name);
                Utils.log(log, player.getId(), command.getAppType(),
                          "SubType[shop_create]ID" + shop.getId() +
                          "]Money[" + player.getMoeny() + "]TRY");
                player.setMoeny(player.getMoeny() - money);
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  SHOP_CREATE_OK,
                                                  command.getSerial(),
                                                  command.getSessionId());
                seg.writeInt(shop.getId());
                seg.writeString(shop.getName());
                seg.writeInt(money);
                write(seg);
                Utils.log(log, player.getId(), command.getAppType(),
                          "SubType[shop_create]ID" + shop.getId() +
                          "]Money[" + player.getMoeny() + "]");

            } catch (ShopException ex) {
                sendMessage(ex.getMessage(), command.getSerial(),
                            command.getSessionId());
            }
        }
    }
	
	class ShopSellProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            if (command.getParamCount() == 3) {
                int shopId = Integer.parseInt(command.getParam(0));
                ShopData shop = shopService.getShopData(shopId);
                String playerName = command.getParam(1);
                int price = Integer.parseInt(command.getParam(2));
                if (shop.getState() != Shop.STATE_SELL &&
                    shop.getBuyPlayerId() == -1) {
                    WorldPlayer target = playerService.getWorldPlayer(
                            playerName);
                    if (target == null) {
                        throw new ITimesException("玩家不在线", command.getSerial(),
                                                  command.getSessionId(),
                                                  command.getAppType());
                    }
                    if (price <= 0) {
                        throw new ITimesException("价钱不对", command.getSerial(),
                                                  command.getSessionId(),
                                                  command.getAppType());
                    }
                    shop.setState(Shop.STATE_SELL);
                    shop.setBuyPlayerId(target.getId());
                    shop.setPrice(price);
                    shop.setSellTime(new Date());
                    shopService.saveShop(shop);
                    Utils.log(log, player.getId(), command.getAppType(),
                              "SubType[shop_sell]ID[" + shop.getId() + "]");
                } else {
                    throw new ITimesException("不能出售", command.getSerial(),
                                              command.getSessionId(),
                                              command.getAppType());
                }
            }
        }
    }
	
	class ShopBuyCancelProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            int shopId = Integer.parseInt(command.getParam(0));
            ShopData shop = shopService.getShopData(shopId);
            if (shop.getState() == Shop.STATE_SELL &&
                shop.getBuyPlayerId() == player.getId()) {
                shop.setBuyPlayerId( -1);
                shop.setPrice( -1);
                shop.setSellTime(null);
                shopService.saveShop(shop);
                sendMessage(shop.getPlayerId(),
                            player.getPlayerName() + "拒绝购买你的店铺" +
                            shop.getName());
            }

        }
    }
	
	class LearSkillProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            if (command.getParamCount() == 1) {
                byte clazz = Byte.parseByte(command.getParam(0));
                synchronized (player) {
                    if (player.canLearnSkill(clazz)) {
                        player.setSkillPoint(clazz, (short) 1);
                        Changed changed = new Changed();
                        changed.setProperty((byte) (29 + clazz), 1);
                        sendGetItem(changed, command.getSerial(),
                                    command.getSessionId(), (byte) 10);
                    }
                }
            }
        }
    }
	
	class TreasureProcessor implements CommandProcessor {
    	public void process (WorldPlayer player, Command command) throws Exception {
    		int usetype = Integer.parseInt(command.getParam(0));
    		int itemId = Integer.parseInt(command.getParam(1));
    		if (usetype == 0){
    			synchronized (player) {
                    Changed changed = new Changed();
                    IItem item = player.completeRemoveItem(itemId, 1, changed);
                    if (item != null) {
                    	if (item.getType() == IItem.TYPE_EXTENDED) {
                    		Effect[] effects = ((ExtendedItem) item).getEffects();
                    		if (effects.length > 0 && effects[0].getType() == 10) {
                    			TreasureEffect effect = (TreasureEffect) effects[0];
                				bufService.getTreasureService().createTreasure(player.getId(),
	                                    effect.getMapId(), effect.getMinX(), effect.getMaxX(),
	                                    effect.getMinY(), effect.getMaxY(), effect.getItemGroupId(), effect.getShovelId());
                    			Scene scene = stageService.getScene(effect.getMapId());
	                            sendMessage(player.getId(), "宝藏在" + scene.getName() + "快去寻找吧!");
	                            sendGetItem(changed, command.getSerial(),
	                                     command.getSessionId(), (byte) 33);
                    		}
                    	} else {
                            log.info("ID" + player.getId() +
                                    " UseItem Error ItemID[" + item.getItemId() +
                                    "]");
                       }
                    } else {
                        log.info("ID" + player.getId() +
                                " UseItem Error ItemID[" + item.getItemId() +
                                "]");
                   }
                }
    		}else if (usetype == GiftItemAutoUseEffect.USETYPE_MARRIAGE){//夫妻关系
    			int mateid = mateService.getMateId(player);//配偶playerid
        		if (mateid != -1){
        			synchronized (player) {
                        Changed changed = new Changed();
                        IItem item = player.completeRemoveItem(itemId, 1, changed);
                        if (item != null) {//原物品已扣除
                        	Effect[] effects = ((IEffectItem) item).getEffects();
                        	IItem giftitem = null;
                        	GiftItemAutoUseEffect effect = null;
                			for (int i = 0; i < effects.length; i++) {
                				if (effects[i].getType() == 68){//68=为他人
                					effect = (GiftItemAutoUseEffect)effects[i];
                					giftitem = Items.getTemplate(effect.getItemid()).newInstance();
                				}
                			}
                			TreasureEffect effect_item = null;
                			if (giftitem != null){
                				Effect[] effects_item = ((IEffectItem) giftitem).getEffects();
                    			for (int effects_i = 0; effects_i < effects_item.length; effects_i++) {
                    				if (effects_item[effects_i].getType() == effect.getParamtype()){//与配置的Paramtype相符
                    					effect_item = (TreasureEffect) effects_item[effects_i];
                    					bufService.getTreasureService().createTreasure(mateid,
        	                            		effect_item.getMapId(), effect_item.getMinX(), effect_item.getMaxX(),
        	                            		effect_item.getMinY(), effect_item.getMaxY(), effect_item.getItemGroupId(), effect_item.getShovelId());
        	                            Scene scene_tmp = stageService.getScene(effect_item.getMapId());
        	                            sendMessage(player.getId(), "已经将宝藏偷偷埋在" + scene_tmp.getName() + "了。并发了一封精灵速递给他(她)哦!");
        	                            
        	                            //发信
        	                            WorldPlayer mateplayer = playerService.getWorldPlayerAndCatch(mateid);
        	                            try {
//											WorldPlayer mateplayer = playerService.loadWorldPlayer(mateid);
											mailService.sendMail(mateid, mateplayer.getPlayerName(), -1, "系统","感恩节的温暖", "你是风儿我是沙，我来藏宝你去挖！" +
                                            		"您的爱人"+player.getPlayerName() +"使用“夫妻感恩魔盒”为您在" + scene_tmp.getName() + "埋下了一个充满爱的感恩宝藏，赶快拿着“夫妻感恩挖宝铲”去开启吧！" +
                                            		"（夫妻感恩挖宝铲可以在夫妻感恩使者处领取，也可以在"+Server.iMoneyStoreString+"购买。）", null, 0, true);
										} catch (Exception e) {
											sendMessage(player.getId(), "精灵速递忙碌中，发信失败了，麻烦您亲自告诉他（她）吧。。。在" + scene_tmp.getName() + "哦。");
            	                            
										}
        	                            playerService.releasePlayer(mateplayer);
										//发私聊
										mateplayer = playerService.getWorldPlayer(mateid);
										if (mateplayer != null){
											sendMessage(mateid, "您的爱人"+player.getPlayerName() +"使用“夫妻感恩魔盒”为您在" + scene_tmp.getName() + "埋下了一个充满爱的感恩宝藏，赶快拿着“夫妻感恩挖宝铲”去开启吧！");
										}
										//自己获得物品
										if(effect.getAddgroupid() > 0){
											DropGroup group = DropGroups.getDropGroup(effect.getAddgroupid(),player.getLevel());
					                        if(group != null){
						                        int rate = rnd.nextInt(group.getRate());
						                        DropItem dropItem = group.calcDropItem(rate);
						                        int count = getCount(rnd, dropItem.getMin(), dropItem.getMax());
						                        IItem di = dropItem.getItem().newInstance();
						                        if(player.completeAddItem(di,count,changed, player.getClientDataVersion())==null){
					                        		byte[] att = ItemUtils.item2dbAttachment(di, count);
					                                mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", di.getName(), "", att, 0, true);
					                                sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到你的邮箱!");
						                        }
					                        }
										}
										sendGetItem(changed, command.getSerial(),
	       	                                     command.getSessionId(), (byte) 33);
                    				}
                    			}
                			}
                        } else {
                            log.info("ID" + player.getId() +
                                    " UseItem Error ItemID[" + item.getItemId() +
                                    "]");
                       }
                    }
        		}else{
        			log.info("ID" + player.getId() +
                            " UseItem Error ItemID[" + itemId +
                            "] USETYPE_MARRIAGE");
        		}
    		}
                
   		}
    }
	
	class PetRenameProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            if (command.getParamCount() < 1) {
                sendMessage("参数错误", command.getSerial(), command.getSessionId());
                return;
            }
            synchronized (player) {
//                Pet pet = player.getPet();
            	int petID = 0;
            	if(command.getParamCount() > 1){
            		petID = Integer.parseInt(command.getParam(1));
            	}
            	Pet pet = null;
            	if(petID > 0){
            		pet = player.getPet(petID);
            	}else{
            		pet = player.getPet();
            	}
                if (pet != null) {
                    if (player.getMoeny() < Utils.getPetRenameMoney(pet)) {
                        sendMessage("没有足够金钱", command.getSerial(),
                                    command.getSessionId());
                        return;
                    }
                    String name = command.getParam(0);
                    if (name.getBytes().length > 10) {
                        sendMessage("名字超出长度", command.getSerial(),
                                    command.getSessionId());
                        return;
                    }
                    if (!Utils.checkString(name, false)) {
                        sendMessage("名字存在非法字符", command.getSerial(),
                                    command.getSessionId());
                        return;
                    }
                    Changed changed = new Changed();
                    String oldname = pet.getName();
                    pet.setName(name);
                    String newName = pet.getName();
	        		if(pet.getBindType() > 0){
						newName = newName.concat("(" + (pet.getBindType() + 1) + "代)");
					}
                    if(pet.getEnhanceName().equals("")&& pet.getEnhanceName().length()==0){
    					changed.addPetProperty(pet, Changed.PET_NAME,
    							newName);
					}else{
						changed.addPetProperty(pet, Changed.PET_NAME,
								newName+pet.getEnhanceName());
					}
                    //changed.addPetProperty(pet, Changed.PET_NAME,
                    //                       command.getParam(0));
                    player.setMoeny(player.getMoeny() -
                                    Utils.getPetRenameMoney(pet));
                    changed.addProperty(Changed.MONEY,
                                        -Utils.getPetRenameMoney(pet));
                    player.resetPets();
                    Utils.log(log, player.getId(), command.getAppType(),
                            "PetRename[" + name + "]--OLDname["+oldname+"] Success");
//                        playerService.savePlayer(player);
                    sendGetItem(changed, command.getSerial(),
                                command.getSessionId(), (byte) 20);
                    if(petID > 0){
                    	//需要给UI发送新的宠物列表
                    	UWAPSegment seg = new UWAPSegment(ClientConstants.COMMAND,
                                command.getSerial(),
                                command.getSessionId());
						write(seg);
                    }
                } else {
                    sendMessage("找不到指定宠物", command.getSerial(),
                                command.getSessionId());
                }
            }
            playerService.checkPlayer(player);
        }
    }
	
	class RefreshAbilityCommitProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            synchronized (player) {
                int money = Utils.getRefreshAbilityMoney(player);
                if(player.hasBuf(Buf.HOPEOBJECT)){
                	Buf buf = player.getBuf(Buf.HOPEOBJECT);
                	int rate = buf.getValue();
                	money = money * (100 - rate)/100;
                }
                if (player.getMoeny() < money) {
                    sendMessage("没有足够的金钱遗忘技能", command.getSerial(),
                                command.getSessionId());
                } else {

//                    int points = player.getUsedAbilityPoint();
                    player.setMoeny(player.getMoeny() - money);
                    player.setPoint(player.getLevel() / 2);
                    player.clearAbilities();
                    player.setAbilityPoints(0);
                    player.setAbilityTimes(player.getAbilityTimes() + 1);
//                        player.reset();
//                        playerService.savePlayer(player);
                    Changed changed = new Changed();
                    changed.addProperty(Changed.MONEY, -money);
                    changed.addProperty(Changed.POINT, player.getPoint());
                    changed.addProperty(Changed.REFRESH_ABILITY, 1);
                    sendGetItem(changed, command.getSerial(),
                                command.getSessionId(), (byte) 0);
                    
                    //重置技能使用
                    UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL, command.getSerial(), command.getSessionId());
                	seg.writeShort(ClientConstants.EXTEND_PROTOCOL_USESKILL);
                	seg.writeShort((short)0);
                	write(seg);
                	
                	player.setUseskill(null);
                }
            }
        }
    }
	
	class RefreshSkillCommitProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            synchronized (player) {
                int type = Integer.parseInt(command.getParam(0));
                int money = Utils.getRefreshSkillMoney(player, type);
                if(player.hasBuf(Buf.HOPEOBJECT)){
                	Buf buf = player.getBuf(Buf.HOPEOBJECT);
                	int rate = buf.getValue();
                	money = money * (100 - rate)/100;
                }
                if (player.getMoeny() < money) {
                    sendMessage("没有足够的金钱遗忘技能", command.getSerial(),
                                command.getSessionId());
                } else {

                    player.setMoeny(player.getMoeny() - money);
                    player.setSkillPoint(type, (short) - 1);
                    player.clearSkills(type);
                    player.reset();
//                        playerService.savePlayer(player);
                    Changed changed = new Changed();
                    changed.addProperty(Changed.MONEY, -money);
                    changed.addProperty((byte) (Changed.SKILL_BLACKSMITHING +
                                                type), -1);
                    sendGetItem(changed, command.getSerial(),
                                command.getSessionId(), (byte) 0);
                }
            }
            playerService.checkPlayer(player);
        }
    }
	
	class GotoInstanceProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            int instanceId = Integer.parseInt(command.getParam(0));
//                Instance ins = null;
            try {
                Instance instance = worldService.gotoInstance(instanceId,
                        player);
                if (instance != null) {
                    InstanceDefinition idf = instance.getDefinition();
                    sendGotoMap(player.getId(), idf.getMap(), idf.getX(),
                                idf.getY());
                    Utils.log(log, player.getId(), command.getAppType(),
                              "GotoInstance[" + idf.getId() + "]");
                }
            } catch (InstanceException ex5) {
                sendMessage(player.getId(), ex5.getMessage());
            }
//                if (ins == null) {
//                    sendMessage(player.getId(), "不能进入副本");
//                } else {
//                    InstanceDefinition idf = ins.getDefinition();
//                    sendGotoMap(player.getId(), idf.getMap(), idf.getX(),
//                                idf.getY());
//                    Utils.log(log, playerId, data.getAppType(),
//                              "GotoInstance[" + idf.getId() + "]");
//                }
        }
    }
	
	class RollProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            int rollId = Integer.parseInt(command.getParam(0));
            log.info("ID[" + player.getId() + "]Fall[" + rollId +
                     "]Roll TRY");
            fallService2.addRoll(player, rollId);
        }
    }


    class RollCancelProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            int rollId = Integer.parseInt(command.getParam(0));
            log.info("ID[" + player.getId() + "]Fall[" + rollId +
                     "]Cancel TRY");
            fallService2.cancelRoll(player, rollId);
        }
    }


    class ChangeTongOwnerProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            int id = Integer.parseInt(command.getParam(0));
            try {
                tongService.changeOwner(player, id, command.getSerial());
            } catch (TongException ex3) {
                throw new ITimesException(ex3.getMessage(), command.getSerial(),
                                          command.getSessionId(),
                                          command.getAppType());
            }
        }
    }


    class ModifyPasswordProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            String old = command.getParam(0);
            String new1 = command.getParam(1);
            String new2 = command.getParam(2);
            if (old == null || new1 == null || new2 == null) {
                sendMessage("修改密码错误", command.getSerial(), command.getSessionId());
                return;
            }
//                    throw new ITimesException("修改密码错误",data.getSerial(),data.getSessionId(),data.getAppType());
            new1.trim();
            new2.trim();
            if (!new1.equals(new2)) {
                sendMessage("新密码不匹配", command.getSerial(), command.getSessionId());
                return;
//                    throw new ITimesException("新密码不匹配",data.getSerial(),data.getSessionId(),data.getAppType());
            }
            //jwp add 游客首次免输入密码，客户段首次发送垃圾密码123，需要从服务器获得
            if(player.getModifyPasswordTimes()==0&&player.getAccountName().startsWith("游客")){
            	old  = player.getPassword();
            }
            modifyPassword(player,command.getSessionId(),command.getSerial(),old,new1);


        }
    }


    class AddPropertyPointProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            int pro = Integer.parseInt(command.getParam(0));
            int point = Integer.parseInt(command.getParam(1));

            if (point > player.getLeavePoints()) {
                throw new ITimesException("加属性点错误", command.getSerial(),
                                          command.getSessionId(),
                                          command.getAppType());
            }

            byte[] pros = new byte[4];

            synchronized (player) {
                switch (pro) {
                    case 1:
                        pros[1] = (byte) point;

                        break;
                    case 2:
                        pros[2] = (byte) point;

                        break;
                    case 3:
                        pros[0] = (byte) point;

                        break;
                    case 4:
                        pros[3] = (byte) point;

                        break;
                }

                player.setLeavePoints(player.getLeavePoints() - point);
                player.setVitality(player.getVitality() + pros[0]);
                player.setStrength(player.getStrength() + pros[1]);
                player.setAgility(player.getAgility() + pros[2]);
                player.setIntelligence(player.getIntelligence() + pros[3]);

                player.adjustProperty();
//                    playerService.savePlayer(player);
            }

            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                              SEG_402_RESULT,
                                              command.getSerial(),
                                              command.getSessionId());
            seg.write((byte) 0);
            seg.write((byte) pro);
            seg.writeShort((short) point);
            write(seg);
            Utils.log(log, player.getId(), command.getAppType(),
                      "PropertyPoint[" + Utils.getHexdump(pros) + "]");
        }
    }


    class TitleProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            int itemId = Integer.parseInt(command.getParam(0));
            String title = command.getParam(1);
            if (title.length() == 0) {
                sendMessage("称号不能为空", command.getSerial(), command.getSessionId());
                return;
            }
            if (title.getBytes("GBK").length > 12) {
                sendMessage("称号太长", command.getSerial(), command.getSessionId());
                return;
            }
            if (!Utils.checkString(title, false)) {
                sendMessage("称号存在非法字符", command.getSerial(),
                            command.getSessionId());
                return;
            }
            if (!KeywordsUtil.filterKeywords(title).equals(title)) {
                sendMessage("称号存在非法字符", command.getSerial(),
                            command.getSessionId());
                return;
            }
            synchronized (player) {
                Changed changed = new Changed();
                IItem item = player.completeRemoveItem(itemId, 1, changed);
                if (item != null) {
                    player.setTitle(title);
                    log.info("ID[" + player.getId() + "]setTitle[" + title +
                             "]ItemID[" + Utils.getHexdump(item.toDbBytes()) +
                             "]");
                    changed.setProperty(Changed.TITLE_STRING, title);
                    sendGetItem(changed, command.getSerial(),
                                command.getSessionId(), (byte) 16);
                }
            }
        }
    }


    class PetSellProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            synchronized (player) {		
                WorldPlayer dest = playerService.getWorldPlayer(command.
                        getParam(0));
                if (dest == null || !dest.online()) {
                    sendMessage("对方不在线", command.getSerial(),
                                command.getSessionId());
                    return;
                }
                if(Math.abs(player.getLastPetTradeTime() - System.currentTimeMillis()) < 15000){
                	sendMessage("请求交易太频繁，请稍候再交易", command.getSerial(),
                            command.getSessionId());
                	return;
                }else{
                	player.setLastPetTradeTime(System.currentTimeMillis());
                }
                //int ret = dest.addBlackList(player.getId(),player.getAccountName());	// zjl modify
                boolean flag = dest.inBlackList(player.getId());
                if(flag){
                	 sendMessage("你已经在对方的黑名单中，无法与对方进行宠物交易!", command.getSerial(),
                			command.getSessionId());
                	 return;
                }
                int petId = Integer.parseInt(command.getParam(1));
                int money = Integer.parseInt(command.getParam(2));
                Pet p = player.getPet(petId);
                if (p == null) {
                    sendMessage("没有找到指定宠物", command.getSerial(),
                                command.getSessionId());
                    return;
                }
                if (p == player.getPet()) {
                    sendMessage("不能买卖已携带宠物", command.getSerial(),
                                command.getSessionId());
                    return;
                }
                if (p.getFavor() < 30) {
                    sendMessage("不能买卖忠诚度小于30的宠物", command.getSerial(),
                                command.getSessionId());
                    return;
                }
                if (p.isBinded()){
                	sendMessage("不能买卖灵魂绑定的宠物", command.getSerial(),
                            command.getSessionId());
                	return;
                }
                
                if(p.getBindType() > 0){
                	sendMessage("不能买卖2代宠物", command.getSerial(),command.getSessionId());
                	return;
                }
                
                if (money < 0) {
                    sendMessage("出卖价格不对", command.getSerial(),
                                command.getSessionId());
                    return;
                }
                
                /*if(p.getCurrentEnchancePoint()>20){
                	sendMessage("宠物炼化超过20星级不允许交易", command.getSerial(),
                            command.getSessionId());
                	return;
                }*/
                try {
                    PetSell sell = petSellService.createPetSell(player.
                            getId(), p.getId(), dest.getId(), money, p.getName());
                    byte[] bytes = stageService.getTaskBytes((short) 31011,
                            new String[] {Utils.getPetSellDesc(p, player,
                            money),
                            "pet_buy " + sell.getId()});
                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                            GET_FILE_OK, command.getSerial(),
                            command.getSessionId());
                    seg.writeShort((short) 31011);
                    seg.writeShort((short) 2);
                    seg.write(bytes);
                    connectService.writeTo(seg, dest.getId());
                } catch (PetSellException ex4) {
                    sendMessage(ex4.getMessage(), command.getSerial(),
                                command.getSessionId());
                }
            }
        }
    }
    
    class PetBuyProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            synchronized (player) {
                int sellId = Integer.parseInt(command.getParam(0));
                Pet pettmp = null; 
                if(command.getParamCount() != 2){
                	PetSell tmpSell = petSellService.getSellPet(sellId, player.getId());
                	if(tmpSell != null){
                		byte[] bytes = stageService.getTaskBytes((short) 31011,
                                new String[] {"你确定花“" + tmpSell.getMoney() + "J”购买宠物“" + tmpSell.getPetName() + "”吗？\n7.确定\n9.取消",
                                "pet_buy " + tmpSell.getId() + " 1"});
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                GET_FILE_OK, command.getSerial(),
                                command.getSessionId());
                        seg.writeShort((short) 31011);
                        seg.writeShort((short) 2);
                        seg.write(bytes);
                        write(seg);
                	}else{
                		sendMessage("交易已经不存在", command.getSerial(),
                                command.getSessionId());
                	}
                	return;
                }
                PetSell sell = petSellService.release(sellId, player.getId());
                if (sell != null) {
                    WorldPlayer src = playerService.getWorldPlayer(sell.
                            getSrcId());
                    if (src != null) {
                        synchronized (src) {
                            if (player.getId() == src.getId()) {
                                return;
                            }
                            Pet p = src.getPet(sell.getPetId());
                            pettmp = p;
                            if (p == null || p == src.getPet() ||
                                p.getFavor() < 30) {
                                sendMessage("交易错误", command.getSerial(),
                                            command.getSessionId());
                                return;
                            }
                            if (player.getPetCount() >= player.getPetSize()) {
                                sendMessage("宠物包格不够", command.getSerial(),
                                            command.getSessionId());
                                return;
                            }
                            if (player.getMoeny() < sell.getMoney()) {
                                sendMessage("钱不够", command.getSerial(),
                                            command.getSessionId());
                                return;
                            }
                            if(p.getBindType() == 0 && (p.getPerceptionLevel() >= 4 || p.getSpiritualityLevel()>=7)){
                            	//1代宠并且悟性或灵性其中一项符合标准则绑定
                            	p.setBinded(true);
                            }
                            
                            /*if(p.getCurrentEnchancePoint()>20){
                            	sendMessage("宠物炼化超过20星级不允许交易", command.getSerial(),
                                        command.getSessionId());
                            	return;
                            }*/
                            //宠物脱掉装备		
                            Changed changed = new Changed();
                            for (int i = 0; i < p.getUsedEquipments().length ; i++){
                            	if (p.getUsedEquipments()[i] != null){
                            		IEquipment e = (IEquipment)p.getUsedEquipments()[i].item;
                            		if(src.completeAddItem(e,e.getId(),changed, src.getClientDataVersion())==null){
                            			if (changed != null){
                            				connectService.sendGetItem(changed,src.getId(), (byte) 26);
                            			}
                            			sendMessage(src.getId(),"无法交易。背包满，宠物装备无法卸下。");
                            			UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
                                    	seg.writeShort(ClientConstants.EXTEND_PROTOCOL_PETEQU_LOGIN);
                                    	seg.writeInt(pettmp.getId());
                                    	Grid[] usedEquipmentsTemp = pettmp.getUsedEquipments();
                                		if (usedEquipmentsTemp != null){
                                			for (int jj = 0;jj<pettmp.getUsedEquipmentinfo().length;jj++){
                                				seg.write((byte) pettmp.getUsedEquipmentinfo()[jj]);
                                				if (usedEquipmentsTemp[jj] != null){
                                					if (pettmp.getUsedEquipmentinfo()[jj] == 1){
                                						IEquipment equtmp = (IEquipment) usedEquipmentsTemp[jj].item;
                                						equtmp.setDataVersion(player.getClientDataVersion());
                                						seg.write(equtmp.toClientBytesWithLevel(pettmp.getLevel()));
                                					}
                                				}
                                			}
                                		}else{
                                			for (int jj = 0;jj<pettmp.getUsedEquipmentinfo().length;jj++){
                                				seg.write((byte) pettmp.getUsedEquipmentinfo()[jj]);
                                			}
                                		}
                                		// 发送宠物升级所需升级经验
                                		seg.writeInt(Utils.getPetUpLevelExp(pettmp.getLevel()));
                                		
                                		//发送宠物阵营宝石效果
                                		CampData campData = getCampMainService().getCampData(player.getCamp());
                                		int value = 0;
                                		if(campData != null){
                            		    	List<CampSkillData> list = campData.getSkillDataList();
                            		    	for(int a = 0; a < list.size(); a++){
                            		    		CampSkillData temp = (CampSkillData) list.get(a);
                            		    		CampSkillLevel temp1 = CampConfig.campSkills.get(temp.getEffect()).getLevel(temp.getLevel());
                            		    		
                            		    		if(temp1 == null || temp1.getParm1() == 0){
                            		    			continue;
                            		    		}else{
                            		    			if(temp.getEffect() == Buf.CAMP_STONE){//阵营科技中只有阵营宝石buff取值下发
                            		    				value = temp1.getParm1();
                            		    				break;
                            		    			}
                            		    		}
                            		    	}
                            		    }
                                		seg.writeInt(value);
                                		
                                		write(seg,src.getId());
                            			
                            			return;
                            		}
                            		e = null;
                            		p.setUsedEquipmentsinfo(i, (byte) 0);
                            		p.setUsedEquipments(i, null);
                            	}
                            }
                            connectService.sendGetItem(changed,src.getId(), (byte) 26);
                            
                            p.setFavor(30);
                            Changed changedSrc = new Changed();
                            Changed changedDest = new Changed();

                            boolean removed = src.removePet(p);
                            int count = player.addPet(p, changedDest);
                          
                            if (!removed) {
                                log.info("ID[" + src.getId() +
                                         "remove Pet[" + p.getId() +
                                         "]Error");
                                return;
                            } else {
                                changedSrc.addItem(p, -1);
                            }
                            if (count == 0) {
                                log.info("ID[" + player.getId() +
                                         "add Pet[" + p.getId() + "]Error");
                                src.addPet(p, null);
                                return;
                            }
                            player.decMoney(sell.getMoney(), changedDest);
                            src.addMoney(sell.getMoney(), changedSrc);
                            sendGetItem(changedDest, command.getSerial(),
                                        command.getSessionId(), (byte) 26);
                            connectService.sendGetItem(changedSrc,
                                    src.getId(), (byte) 26);
                            
                            mailService.sendMail(src.getId(), src.getPlayerName(), -1, 
                            		"系统", "宠物交易成功", "您与<cff0000>" + player.getPlayerName() +
                            		"</c>交易宠物<cff0000>" + p.getName() + "</c>成功，从他那收到<cff0000>" + sell.getMoney() + "</c>j币。", null, 0, true);
                            
                            log.info("ID[" + player.getId() +
                                     "]BuyPet,Price[" + sell.getMoney() +
                                     "]Money[" + player.getMoeny() +
                                     "]Changed[" +
                                     Utils.getHexdump(changedDest.toBytes()) +
                                     "]Source[" + src.getId() + "]");
                            log.info("ID[" + src.getId() +
                                     "]SellPet,Price[" + sell.getMoney() +
                                     "]Money[" + src.getMoeny() +
                                     "]Changed[" +
                                     Utils.getHexdump(changedSrc.toBytes()) +
                                     "]Dest[" + player.getId() + "]");
                        }
                        playerService.checkPlayer(player);
                        playerService.checkPlayer(src);
                        
                        UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL, command.getSerial(), command.getSessionId());
                    	seg.writeShort(ClientConstants.EXTEND_PROTOCOL_PETEQU_LOGIN);
                    	seg.writeInt(pettmp.getId());
                    	Grid[] usedEquipmentsTemp = pettmp.getUsedEquipments();
                		if (usedEquipmentsTemp != null){
                			for (int jj = 0;jj<pettmp.getUsedEquipmentinfo().length;jj++){
                				seg.write((byte) pettmp.getUsedEquipmentinfo()[jj]);
                				if (usedEquipmentsTemp[jj] != null){
                					if (pettmp.getUsedEquipmentinfo()[jj] == 1){
                						IEquipment equtmp = (IEquipment) usedEquipmentsTemp[jj].item;
                						seg.write(equtmp.toClientBytesWithLevel(pettmp.getLevel()));
                					}
                				}
                			}
                		}else{
                			for (int jj = 0;jj<pettmp.getUsedEquipmentinfo().length;jj++){
                				seg.write((byte) pettmp.getUsedEquipmentinfo()[jj]);
                			}
                		}
                		// 发送宠物升级所需升级经验
                		seg.writeInt(Utils.getPetUpLevelExp(pettmp.getLevel()));
                		
                		//发送宠物阵营宝石效果
                		CampData campData = getCampMainService().getCampData(player.getCamp());
            		    List<CampSkillData> list = campData.getSkillDataList();
            		    int value = 0;
            		    for(int tt = 0; tt < list.size(); tt++){
            		    	CampSkillData temp = (CampSkillData) list.get(tt);
            		    	CampSkillLevel temp1 = CampConfig.campSkills.get(temp.getEffect()).getLevel(temp.getLevel());
            		    	
            		    	if(temp1 == null || temp1.getParm1() == 0){
            		    		continue;
            		    	}else{
            		    		if(temp.getEffect() == Buf.CAMP_STONE){//阵营科技中只有阵营宝石buff取值下发
            		    			value = temp1.getParm1();
            		    			break;
            		    		}
            		    	}
            		    }
        				seg.writeInt(value);
                		
                		write(seg);
                    }
                } else {
                    sendMessage("交易已经不存在", command.getSerial(),
                                command.getSessionId());
                }
            }
        }
    }


    class SendMessageProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            int itemId = Integer.parseInt(command.getParam(0));
            String message = command.getParam(1);
            Changed changed = new Changed();
            IItem item = player.completeRemoveItem(itemId, 1, changed);
            if (item != null) {
                if (item.getType() == IItem.TYPE_EXTENDED) {
                    Effect[] effects = ((ExtendedItem) item).getEffects();
                    if (effects.length > 0 && effects[0].getType() == 14) { //14是说话的效果
                        TalkEffect effect = (TalkEffect) effects[0];
                        //情歌对唱时 进行检测
                    	if("world".equals(effect.getChannel()) && LoveLyricsConfig.state == LoveLyricsConfig.ACTIONSTART){
                    		LoveLyricsConfig.playerChat(message.trim(), player);
                    	}
                    	//歌词活动时 进行检测
                    	if("world".equals(effect.getChannel()) && LyricsConfig.state == LyricsConfig.ACTIONSTART){
                    		LyricsConfig.playerChat(message.trim(), player);
                    	}
                    	//猜灯谜活动时 进行检测
                    	if("world".equals(effect.getChannel()) && RiddlesConfig.state == RiddlesConfig.ACTIONSTART){
                    		RiddlesConfig.playerChat(message.trim(), player);
                    	}
                    	//咏春诗歌活动时 进行检测
                    	if("world".equals(effect.getChannel()) && RiddlesConfig2.state == RiddlesConfig2.ACTIONSTART){
                    		RiddlesConfig2.playerChat(message.trim(), player);
                    	}
                        String msg = StringUtils.replace(effect.getMessage(),
                                "{name}", player.getPlayerName());
                        msg = StringUtils.replace(msg, "{message}", message);
                        if (msg.length() > 40) {
                            msg = msg.substring(0, 39);
                        }
                        msg = KeywordsUtil.filterKeywords(msg);
                        msg = msg.replace('\n', ' ');
                        if ("world".equals(effect.getChannel())) { //世界
                            chatService.sendWorldMessage( -1, "系统", msg);
                            sendGetItem(changed, command.getSerial(),
                                        command.getSessionId(), (byte) 33);
                        } else if ("private".equals(effect.getChannel())) {
                            WorldPlayer dest = playerService.
                                               getOnlinePlayerWithSex(player.
                                    getSex() ==
                                    0 ? 1 : 0);
                            if (dest == null) {
                                player.completeAddItem(item, 1, null, player.getClientDataVersion());
                                sendMessage("没有合适目标", command.getSessionId(),
                                            command.getSessionId());
                            } else {
                                chatService.sendPrivateMessage( -1, "系统",
                                        dest.getId(), msg);
                                sendGetItem(changed, command.getSerial(),
                                            command.getSessionId(), (byte) 33);
                                sendMessage("消息已经发送给" + dest.getPlayerName(),
                                            command.getSessionId(),
                                            command.getSessionId());
                            }
                        }
                    } else if (effects.length > 0 && effects[0].getType() == 63) {	// 狮子吼
                    	RoarEffect effect = (RoarEffect) effects[0];
                        String msg = StringUtils.replace(effect.getMessage(),
                                "{name}", player.getPlayerName());
                        if (message.length() > 40) {
                        	message = message.substring(0, 40);
                        }
                        msg = StringUtils.replace(msg, "{message}", message);
                        msg = KeywordsUtil.filterKeywords(msg);
                        msg = msg.replace('\n', ' ');
                    	if ("roar".equals(effect.getChannel())) {// 狮子吼
                    		chatService.sendRoarMessage( -1, "狮子吼", msg, true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
                    		sendGetItem(changed, command.getSerial(),
                                     command.getSessionId(), (byte) 33);
                        } 
                    } else {
                        log.info("ID" + player.getId() +
                                 " UseItem Error ItemID[" + item.getItemId() +
                                 "]");
                    }
                } else {
                    log.info("ID" + player.getId() +
                             " UseItem Error ItemID[" + item.getItemId() +
                             "]");
                }
            }
        }
    }
    


    class LookPackageProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            int itemId = Integer.parseInt(command.getParam(0));
            WorldPlayer dest = playerService.getWorldPlayer(command.
                    getParam(1));
            if (dest == null || !dest.online()) {
                sendMessage("目标不在线", command.getSerial(), command.getSessionId());
                return;
            }
            Changed changed = new Changed();
            IItem item = player.completeRemoveItem(itemId, 1, changed);
            if (item != null && ItemUtils.hasEffect(item, (byte) 17)) {

                mailService.sendMail(player.getId(),
                                     player.getPlayerName(),
                                     -1,
                                     "系统",
                                     "神秘的背包内物品清单"
                                     /*dest.getPlayerName() + "背包状态"*/
                                     ,
                                     Utils.getPlayerItemsString(dest), null,
                                     0, true);
                sendGetItem(changed, command.getSerial(),
                            command.getSessionId(), (byte) 34);
            } else {
                log.info("ID" + player.getId() + " UseItem Error ItemID[" +
                         item.getItemId() + "]");
            }
        }
    }


    class FreeMoveProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            int itemId = Integer.parseInt(command.getParam(0));
        	GameMap playerMap = player.getMap();
        	if(playerMap != null){
				NoDoor door = NoDoor.getNoTransfer(playerMap.getMapId());
				if(door != null){
					sendMessage(player.getId(), door.getMessage());
					return;
				}
			}
            TransferDoor door = TransferDoor.getTransferDoor(command.
                    getParam(1));
            if (door == null) {
                sendMessage("目的地不存在", command.getSerial(), command.getSessionId());
                return;
            }
            //                Stage stage = stageService.getStage(command.getParam(1));
            //                if(stage==null){
            //                    sendMessage("目的地不存在", data.getSerial(), data.getSessionId());
            //                    return;
            //                }
            if (player.getLevel() + Utils.DOOR_LEVEL_RESTRICTIONS < door.getLevel()) {
            	sendMessage("您的等级过低，去那里太危险了。", command.getSerial(), command.getSessionId());
                return;
            }
            
            Changed changed = new Changed();
            IItem item = player.completeRemoveItem(itemId, 1, changed);
            if (item != null && ItemUtils.hasEffect(item, (byte) 18)) {
                sendGetItem(changed, command.getSerial(), command.getSessionId(),
                            (byte) 33);
                if(player.getClientDataVersion() > 0){
                	sendGotoMap(player.getId(), (short) door.getNewMap(),
                            (short) door.getNewX(), (short) door.getNewY());
                }else{
                	sendGotoMap(player.getId(), (short) door.getMapId(),
                            (short) door.getX(), (short) door.getY());
                }
            }
        }
    }


    class TouchProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            int destId = Integer.parseInt(command.getParam(0));
            WorldPlayer dest = playerService.getWorldPlayer(destId);
            if (dest != null) {
                byte[] bytes = stageService.getTaskBytes((short) 31015,
                        new String[] {"" + destId, dest.getPlayerName()});
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  GET_FILE_OK,
                                                  command.getSerial(),
                                                  command.getSessionId());
                seg.writeShort((short) 31011);
                seg.writeShort((short) 2);
                seg.write(bytes);
                write(seg);
            }
        }
    }


    class HelloProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            int destId = Integer.parseInt(command.getParam(0));
            String msg = "" + command.getParam(1);
            chatService.sendPrivateMessage(player.getId(),
                                           player.getPlayerName(), destId,
                                           msg);
        }
    }
    
    class RefreshProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            int id = Integer.parseInt(command.getParam(0));
            byte type = Byte.parseByte(command.getParam(1)); //0 怪物  1 资源 2 npc
            int x = Integer.parseInt(command.getParam(2));
            int y = Integer.parseInt(command.getParam(3));
            UWAPSegment seg = new UWAPSegment(ClientConstants.REFRESH,
                                              command.getSerial(),
                                              command.getSessionId());
            seg.writeShort((short) 1);
            seg.write((byte) 1);
            seg.writeInt(id);
            seg.writeInt((x & 0xFFFF) << 16 | (y & 0xFFFF));
            write(seg);
        }
    }


    class ColorProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            int color = Integer.parseInt(command.getParam(0));
            player.setColor(color);
        }
    }


    class BattleStatusProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            byte status = Byte.parseByte(command.getParam(0));
            player.setInBattle(status != 0);
        }
    }


    class RenameProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            String name = command.getParam(0).trim();
            synchronized (player) {
                if (player.getModifyNameTimes() <= 0) {
                    sendMessage("您已经不能修改角色名了", command.getSerial(),
                                command.getSessionId());
                    return;
                }
                if (name.length() == 0) {
                    sendMessage("角色名不能为空", command.getSerial(),
                                command.getSessionId());
                    return;
                }
                if (name.getBytes("GBK").length > 16) {
                    sendMessage("角色名太长", command.getSerial(),
                                command.getSessionId());
                    return;
                }

                if (KeywordsUtil.isInvalidName(name.toLowerCase())) {
                    sendMessage("角色名出现非法字符", command.getSerial(),
                                command.getSessionId());
                    return;
                }
                if (!Utils.checkString(name, false)) {
                    sendMessage("角色名出现非法字符", command.getSerial(),
                                command.getSessionId());
                    return;
                }
                String newName = KeywordsUtil.filterKeywords(name);
                if (!newName.equals(name)) {
                    sendMessage("角色名出现非法字符", command.getSerial(),
                                command.getSessionId());
                    return;
                }
                Player p = playerService.getPlayerByName(name);
                if (p != null) {
                    sendMessage("存在同名角色", command.getSerial(),
                                command.getSessionId());
                    return;
                }
                player.setModifyNameTimes(player.getModifyNameTimes() - 1);
                String oldName = player.getPlayerName();
                player.setPlayerName(name);
                playerService.savePlayer(player);
                if (player.getTongId() > 0) {
                    tongService.nameModified(oldName, player);
                }
                log.info("ID[" + player.getId() + "]RENAME OLD[" + oldName +
                         "] NEW[" + name + "]");
                sendMessage("角色名修改成功", command.getSerial(),
                            command.getSessionId());
                Changed changed = new Changed();
                changed.setProperty(Changed.PLAYERNAME, name);
                sendGetItem(changed, command.getSerial(), command.getSessionId(),
                            (byte) 10);
            }
        }
    }

    class RenameMergerProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
        	int itemId = Integer.parseInt(command.getParam(0));
            String name = command.getParam(1).trim();
            synchronized (player) {
                //if (player.getModifyNameTimes() <= 0) {
                //    sendMessage("您已经不能修改角色名了", command.getSerial(),
                //                command.getSessionId());
                //    return;
                //}
                if (name.length() == 0) {
                    sendMessage("角色名不能为空", command.getSerial(),
                                command.getSessionId());
                    return;
                }
                if (name.getBytes("GBK").length > 16) {
                    sendMessage("角色名太长", command.getSerial(),
                                command.getSessionId());
                    return;
                }

                if (KeywordsUtil.isInvalidName(name.toLowerCase())) {
                    sendMessage("角色名出现非法字符", command.getSerial(),
                                command.getSessionId());
                    return;
                }
                if (!Utils.checkString(name, false)) {
                    sendMessage("角色名出现非法字符", command.getSerial(),
                                command.getSessionId());
                    return;
                }
                String newName = KeywordsUtil.filterKeywords(name);
                if (!newName.equals(name)) {
                    sendMessage("角色名出现非法字符", command.getSerial(),
                                command.getSessionId());
                    return;
                }
                Player p = playerService.getPlayerByName(name);
                if (p != null) {
                    sendMessage("存在同名角色", command.getSerial(),
                                command.getSessionId());
                    return;
                }
                //player.setModifyNameTimes(player.getModifyNameTimes() - 1);
                
                String oldName = player.getPlayerName();
                player.setPlayerName(name);
                playerService.savePlayer(player);
                if (player.getTongId() > 0) {
                    tongService.nameModified(oldName, player);
                }
                mateService.nameModified(oldName, player);
                masterService.nameModified(oldName, player);
                friendsService.nameModified(oldName, player);
                playerService.resetPlayerName(oldName, name);
                Changed changed = new Changed();
                IItem item = player.completeRemoveItem(itemId, 1, changed);
                if (item != null) {
	                log.info("ID[" + player.getId() + "]RENAME OLD[" + oldName +
	                         "] NEW[" + name + "]ItemID[" + Utils.getHexdump(item.toDbBytes()) +
	                             "]");
	                sendMessage("角色名修改成功", command.getSerial(),
	                            command.getSessionId());
	                changed.setProperty(Changed.PLAYERNAME, name);
	                sendGetItem(changed, command.getSerial(), command.getSessionId(),
                            (byte) 10);
	                Friend[] friends = player.getFriends();
	                String mailmsg = "";
	                if (oldName.length()>3){
		                if ("*4区".equalsIgnoreCase(oldName.substring(oldName.length()-3, oldName.length()))){
		                	mailmsg = "您的朋友因合服后改名为“" + oldName + "”。现在他已改名为“" + name
                            + "”。请您把好友列表中原有的旧名字删掉，再添加一次。给您带来的不便，敬请谅解。";
		                }else{
		                	mailmsg = "由于你朋友的名字重复或违反了规定，名字将从“" + oldName + "”改名为“" + name
                            + "”。请您把好友列表中原有的旧名字删掉，再添加一次。给您带来的不便，敬请谅解。";
		                }
	                }else{
	                	mailmsg = "由于你朋友的名字重复或违反了规定，名字将从“" + oldName + "”改名为“" + name
                        + "”。请您把好友列表中原有的旧名字删掉，再添加一次。给您带来的不便，敬请谅解。";
	                }
	                for(int i=0;i<friends.length;i++){
	                	Friend friend = friends[i];
	                	mailService.sendMail(friend.getId(), "", -1, "系统",
	                            "更名通知", 
	                            mailmsg, null, 0, true);
	                }
                }
            }
        }
    }

    class BindPhoneProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            String phone = command.getParam(0);
            if (phone == null || phone.length() != 11) {
                sendMessage("手机号码不正确", command.getSerial(),
                            command.getSessionId());
                return;
            }
            modifyPhone(player,command.getSessionId(),command.getSerial(),phone);


        }
    }


    class EnterForProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            try {
                battleField.enterfor(player);
                sendMessage("报名成功", command.getSerial(), command.getSessionId());
            } catch (BattleFieldException ex) {
                sendMessage(ex.getMessage(),
                            command.getSerial(),
                            command.getSessionId());
            }
        }
    }
	
    static IItemTemplate lol = Items.getTemplate(550001);

    class GetLolProcessor implements CommandProcessor {

        public void process(WorldPlayer player, Command command) throws Exception {
            synchronized (player) {
                if (player.hasItem(550001)) {
                    sendMessage("你不是已经有爱之神光了么!", command.getSerial(), command.getSessionId());
                } else {
                    Mate mate = mateService.getMate(player);
                    if (mate != null) {

                        Changed changed = new Changed();
                        player.addItem(lol, 1, changed, player.getClientDataVersion());
                        sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 100);
                    	Client client = player.getClient();
                    	if(client != null && client.getDataVersion() > 0){
                    		if(player.getAllGridSize() <= player.getCurrentGridSize()){
                    			return;
                    		}
                    	}
                        sendMessage("收好你的爱之神光!", command.getSerial(), command.getSessionId());
                    } else {
                        sendMessage("你不能领取爱之神光!", command.getSerial(), command.getSessionId());
                    }
                }
            }
        }
    }
    
    static IItemTemplate tol = Items.getTemplate(550002);

    class GetTolProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            synchronized (player) {
                if (player.hasItem(550002)) {
                    sendMessage("你不是已经有爱之称谓了么!", command.getSerial(), command.getSessionId());
                } else {
                    Mate mate = mateService.getMate(player);
                    if (mate != null) {
                        Changed changed = new Changed();
                        player.addItem(tol, 1, changed, player.getClientDataVersion());
                        sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 100);
                        sendMessage("收好你的爱之称谓!", command.getSerial(), command.getSessionId());
                    } else {
                        sendMessage("你不能领取爱之称谓!", command.getSerial(), command.getSessionId());
                    }
                }
            }
        }
    }
    
    static IItemTemplate top = Items.getTemplate(550003);
    static IItemTemplate tom = Items.getTemplate(550004);

    class GetTopProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            synchronized (player) {
                if (player.hasItem(550003)) {
                    sendMessage("你不是已经有师徒称谓了么!", command.getSerial(), command.getSessionId());
                } else {
                    if (masterService.isPrentice(player)) {
                        Changed changed = new Changed();
                        player.addItem(top, 1, changed, player.getClientDataVersion());
                        sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 100);
                    } else {
                        sendMessage("你不能领取师徒称谓!", command.getSerial(), command.getSessionId());
                    }
                }
            }
        }
    }


    class GetTomProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            synchronized (player) {
                if (player.hasItem(550004)) {
                    sendMessage("你不是已经有师傅称谓了么!", command.getSerial(), command.getSessionId());
                } else {
                    Changed changed = new Changed();
                    player.addItem(tom, 1, changed, player.getClientDataVersion());
                    sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 100);
                }
            }
        }
    }


    class CancelUnMarryProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            int id = Integer.parseInt(command.getParam(0));
            try {
                MateService.UnMarryRequest request = mateService.cancelUnMarry(player, id);
                sendMessage(request.sourceId, "对方拒绝了你的离婚,你们没有商量好么?");
            } catch (MateException ex) {
                sendMessage(ex.getMessage(), command.getSerial(),
                            command.getSessionId());
            }
        }
    }


    class UnMarryProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            int id = Integer.parseInt(command.getParam(0));
            try {
                Changed changed1 = new Changed();
                Changed changed2 = new Changed();
                Mate mate = mateService.unMarry(player, id, changed1, changed2);
                log.info("NormalUnMarry Husband[" + mate.getHusbandId() + "] Changed[" +
                        Utils.getHexdump(changed1.toBytes()) + "]Wife[" + mate.getWifeId() + "] Changed[" +
                        Utils.getHexdump(changed2.toBytes()) + "]");
                sendMessage(mate.getHusbandId(), "又是人生一个新的开始");
                sendMessage(mate.getWifeId(), "又是人生一个新的开始");
                connectService.sendGetItem(changed1, mate.getHusbandId(), (byte) 22);
                connectService.sendGetItem(changed2, mate.getWifeId(), (byte) 22);

                if (player.getSex() == 0){//男
                	WorldPlayer p = playerService.getWorldPlayer(mate.getWifeId());
    				if (p != null && p.getState() == WorldPlayer.ONLINE) {//在线
    					//通知本人
       				 	UWAPSegment seg = new UWAPSegment(ClientConstants.FRIENDS_STATUS);
       			        seg.write((byte)1);
       			        seg.writeInt(p.getId());
       			        seg.writeBoolean(true);
       			        seg.writeShort((short)player.getFriendFavorite(p));
       			        seg.writeInt(0);
       			        connectService.writeTo(seg,player.getId());
       			        //通知妻子
       			        seg = new UWAPSegment(ClientConstants.FRIENDS_STATUS);
    			        seg.write((byte)1);
    			        seg.writeInt(player.getId());
    			        seg.writeBoolean(player != null&&player.getState()==WorldPlayer.ONLINE);
    			        seg.writeShort((short)p.getFriendFavorite(player));
    			        seg.writeInt(0);
    			        connectService.writeTo(seg,p.getId());
    				}
                }else{//女
                	WorldPlayer p = playerService.getWorldPlayer(mate.getHusbandId());
    				if (p != null && p.getState() == WorldPlayer.ONLINE) {//在线
    					//通知本人
       				 	UWAPSegment seg = new UWAPSegment(ClientConstants.FRIENDS_STATUS);
       			        seg.write((byte)1);
       			        seg.writeInt(p.getId());
       			        seg.writeBoolean(true);
       			        seg.writeShort((short)player.getFriendFavorite(p));
       			        seg.writeInt(0);
       			        connectService.writeTo(seg,player.getId());
       			        //通知丈夫
       			        seg = new UWAPSegment(ClientConstants.FRIENDS_STATUS);
    			        seg.write((byte)1);
    			        seg.writeInt(player.getId());
    			        seg.writeBoolean(player != null&&player.getState()==WorldPlayer.ONLINE);
    			        seg.writeShort((short)p.getFriendFavorite(player));
    			        seg.writeInt(0);
    			        connectService.writeTo(seg,p.getId());
    				}
                }
            } catch (MateException ex) {
                sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
            }
        }
    }
    
    class IllegalUnMarryProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            try {
                Changed changed1 = new Changed();
                Changed changed2 = new Changed();
                log.info("ID[" + player.getId() + "] SingleUnMarry Money[" + player.getMoeny() + "] Credit[" +
                         player.getCredit() + "] TRY");
                Mate mate = mateService.unMarry(player, changed1, changed2);
                connectService.sendGetItem(changed1, mate.getHusbandId(), (byte) 22);
                connectService.sendGetItem(changed2, mate.getWifeId(), (byte) 22);
                log.info("SingleUnMarry Husband[" + mate.getHusbandId() + "] Changed[" +
                         Utils.getHexdump(changed1.toBytes()) + "]Wife[" + mate.getWifeId() + "] Changed[" +
                         Utils.getHexdump(changed2.toBytes()) + "]");
                sendMessage(player.getId(), "又是人生一个新的开始");
                

                if (player.getSex() == 0){//男
                	WorldPlayer p = playerService.getWorldPlayer(mate.getWifeId());
    				if (p != null && p.getState() == WorldPlayer.ONLINE) {//在线
    					//通知本人
       				 	UWAPSegment seg = new UWAPSegment(ClientConstants.FRIENDS_STATUS);
       			        seg.write((byte)1);
       			        seg.writeInt(p.getId());
       			        seg.writeBoolean(true);
       			        seg.writeShort((short)player.getFriendFavorite(p));
       			        seg.writeInt(0);
       			        connectService.writeTo(seg,player.getId());
       			        //通知妻子
       			        seg = new UWAPSegment(ClientConstants.FRIENDS_STATUS);
    			        seg.write((byte)1);
    			        seg.writeInt(player.getId());
    			        seg.writeBoolean(player != null&&player.getState()==WorldPlayer.ONLINE);
    			        seg.writeShort((short)p.getFriendFavorite(player));
    			        seg.writeInt(0);
    			        connectService.writeTo(seg,p.getId());
    				}
                }else{//女
                	WorldPlayer p = playerService.getWorldPlayer(mate.getHusbandId());
    				if (p != null && p.getState() == WorldPlayer.ONLINE) {//在线
    					//通知本人
       				 	UWAPSegment seg = new UWAPSegment(ClientConstants.FRIENDS_STATUS);
       			        seg.write((byte)1);
       			        seg.writeInt(p.getId());
       			        seg.writeBoolean(true);
       			        seg.writeShort((short)player.getFriendFavorite(p));
       			        seg.writeInt(0);
       			        connectService.writeTo(seg,player.getId());
       			        //通知丈夫
       			        seg = new UWAPSegment(ClientConstants.FRIENDS_STATUS);
    			        seg.write((byte)1);
    			        seg.writeInt(player.getId());
    			        seg.writeBoolean(player != null&&player.getState()==WorldPlayer.ONLINE);
    			        seg.writeShort((short)p.getFriendFavorite(player));
    			        seg.writeInt(0);
    			        connectService.writeTo(seg,p.getId());
    				}
                }
            } catch (MateException ex) {
                sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
            }
        }
    }


    class IllegalUnMarryRequestprocessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            byte[] bytes = stageService.getTaskBytes((short) 31002,
                    new String[] {"你要强制解除你们的婚姻么?\n1.没错\n2.没有的事",
                    "illegalunmarry"});
            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                              GET_FILE_OK,
                                              command.getSerial(),
                                              command.getSessionId());
            seg.writeShort((short) 31002);
            seg.writeShort((short) 2);
            seg.write(bytes);
            write(seg);
        }
    }


    class LawUnMarryRequestProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            Team team = player.getTeam();
            if (team == null) {
                sendMessage("协议离婚需要两个人组队跟随后来申请", command.getSerial(),
                            command.getSessionId());
                return;
            }
            if (team.getLeader() != player) {
                sendMessage("必须由队长发起离婚申请", command.getSerial(),
                            command.getSessionId());
                return;
            }
            if (team.getCount() != 2) {
                sendMessage("队伍必须为2人并且在跟随状态", command.getSerial(),
                            command.getSessionId());
                return;
            }
            IPlayerData ps[] = team.getMembers(WorldPlayer.TEAM_FOLLOW);
            if (ps.length != 2) {
            	sendMessage("队伍必须为2人并且在跟随状态", command.getSerial(),
            			command.getSessionId());
            	return;
            }
            
            ArrayList<WorldPlayer> lstPlayer = new ArrayList<WorldPlayer>();
            for(int i=0; i<ps.length; i++){
            	if(!(ps[i] instanceof WorldPlayer)){
            		sendMessage("队伍必须为2人并且在跟随状态，不能有佣兵。", command.getSerial(),
                			command.getSessionId());
                	return;
            	}
            	lstPlayer.add((WorldPlayer)ps[i]);
            }
            WorldPlayer[] players = new WorldPlayer[lstPlayer.size()];
            lstPlayer.toArray(players);
            try {
                MateService.UnMarryRequest request = mateService.unMarryRequest(players[0], players[1]);
                sendMessage("希望你们今后各自的生活能幸福,正在等待对方的同意.", command.getSerial(), command.getSessionId());
               /* byte[] bytes = stageService.getTaskBytes((short) 31003,
                        new String[] {player.getPlayerName() +
                        "要和你协议离婚,你同意么?\n1.同意\n2.拒绝",
                        "unmarry " + request.id,
                        "unmarry_cancel " + request.id});
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  GET_FILE_OK);
                seg.writeShort((short) 31003);*/
                byte[] bytes = stageService.getTaskBytes((short) 31050,
                        new String[] {player.getPlayerName() +
                        "要和你协议离婚,你同意么?\n1.同意\n2.拒绝",
                        "unmarry " + request.id,
                        "unmarry_cancel " + request.id});
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  GET_FILE_OK);
                seg.writeShort((short) 31050);
                seg.writeShort((short) 2);
                seg.write(bytes);
                connectService.writeTo(seg, players[1].getId());
            } catch (MateException ex) {
                sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
            }
        }
    }


    class MarrayProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            int id = Integer.parseInt(command.getParam(0));
            try {
                Changed changed1 = new Changed();
                Changed changed2 = new Changed();
                Mate mate = mateService.marry(player, id, changed1, changed2);
                connectService.sendGetItem(changed1,
                                           mate.getHusbandId() == player.getId() ? mate.getWifeId() : mate.getHusbandId(),
                                           (byte) 20);
                connectService.sendGetItem(changed2, player.getId(), (byte) 20);

                log.info("ID["+mate.getHusbandId()+"] Mate["+mate.getWifeId()+"]Marry");
                chatService.sendSystemMessage(mate.getHusbandName() + "和" +
                                              mate.getWifeName() +
                                              "于今日在幻想世界爱神的爱之光环下结为夫妻，祝福他们永远幸福。");
            } catch (MateException ex) {
                sendMessage(ex.getMessage(), command.getSerial(),
                            command.getSessionId());
            }
        }
    }


    class CancelMarryProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            int id = Integer.parseInt(command.getParam(0));
            try {
                MateService.MarryRequest request = mateService.cancelMarry(player, id);
                sendMessage(request.sourceId, "对方拒绝了你的求婚,你们没有商量好么?");
            } catch (MateException ex) {
                sendMessage(ex.getMessage(), command.getSerial(),
                            command.getSessionId());
            }
        }
    }


    class RequestMarryProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            Team team = player.getTeam();
            if (team == null) {
                sendMessage("结婚需要2个人组队跟随后来申请", command.getSerial(),
                            command.getSessionId());
                return;
            }
            if (team.getLeader() != player) {
                sendMessage("必须由队长发起结婚申请", command.getSerial(),
                            command.getSessionId());
                return;
            }
            if (team.getCount() != 2) {
                sendMessage("队伍必须为2人并且在跟随状态", command.getSerial(),
                            command.getSessionId());
                return;
            }
            IPlayerData[] ps = team.getMembers(WorldPlayer.TEAM_FOLLOW);
            if (ps.length != 2) {
            	sendMessage("队伍必须为2人并且在跟随状态", command.getSerial(),
            			command.getSessionId());
            	return;
            }
            ArrayList<WorldPlayer> lstPlayer = new ArrayList<WorldPlayer>();
            for(int i=0; i<ps.length; i++){
            	if(ps[i] instanceof WorldPlayer){
            		lstPlayer.add((WorldPlayer)ps[i]);
            	}
            }
            WorldPlayer[] players = new WorldPlayer[lstPlayer.size()];
            lstPlayer.toArray(players);
            if (players.length != 2) {
            	sendMessage("队伍必须为2人并且在跟随状态，不能带佣兵。", command.getSerial(),
            			command.getSessionId());
            	return;
            }
            try {
                if (players[0].getFriendFavorite(players[1]) < 99)
                    throw new MateException("互为好友并好友度都到达99之后才能结婚");
                if (players[1].getFriendFavorite(players[0]) < 99)
                    throw new MateException("互为好友并好友度都到达99之后才能结婚");
                if (players[0].getLevel() < 6 || players[1].getLevel() < 6)
                    throw new MateException("需要双方等级超过6级才能结婚");
                if (Math.abs(players[1].getSex() - players[0].getSex()) != 1)
                    throw new MateException("目前只有男女才允许结为夫妻哦.");
                if (players[0].getCredit() < 10 || players[1].getCredit() < 10)
                    throw new MateException("荣誉没有达到要求");
                if (player.getMoeny() < (players[0].getLevel() + players[1].getLevel()) * 99)
                    throw new MateException("没钱还想结婚呀");
                if (mateService.hasMate(players[0]) || mateService.hasMate(players[1]))
                    throw new MateException("不能同时拥有两份婚姻哦");

                MateService.MarryRequest request = mateService.requestMarry(players[0], players[1]);
                sendMessage("恭喜你们即将走入婚姻的殿堂,正在等待对方的同意.", command.getSerial(), command.getSessionId());
              /*  byte[] bytes = stageService.getTaskBytes((short) 31003,
                        new String[] {player.getPlayerName() +
                        "要和你结为夫妻\n1.同意\n2.拒绝",
                        "marry " + request.id,
                        "marry_cancel " + request.id});
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  GET_FILE_OK);
                seg.writeShort((short) 31003);*/
                byte[] bytes = stageService.getTaskBytes((short) 31050,
                        new String[] {player.getPlayerName() +
                        "要和你结为夫妻\n1.同意\n2.拒绝",
                        "marry " + request.id,
                        "marry_cancel " + request.id});
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  GET_FILE_OK);
                seg.writeShort((short) 31050);
                seg.writeShort((short) 2);
                seg.write(bytes);
                connectService.writeTo(seg, players[1].getId());
            } catch (MateException ex) {
                sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
            }
        }
    }

    // 由于师徒改造目前屏蔽原拜师代码
    class RequestMasterProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {/*
            Team team = player.getTeam();
            if (team == null) {
                sendMessage("必须组队才能拜师", command.getSerial(),
                            command.getSessionId());
                return;
            }
            if (team.getLeader() != player) {
                sendMessage("必须由队长发起拜师请求", command.getSerial(),
                            command.getSessionId());
                return;
            }
            if (team.getCount() != 2) {
                sendMessage("队伍必须为2人并且在跟随状态", command.getSerial(),
                            command.getSessionId());
                return;
            }
            WorldPlayer[] players = team.getMembers(WorldPlayer.TEAM_FOLLOW);
            if (players.length != 2) {
                sendMessage("队伍必须为2人并且在跟随状态", command.getSerial(),
                            command.getSessionId());
                return;
            }
            try {
                MasterService.Request request = masterService.requestRelation(players[0], players[1]);
                byte[] bytes = stageService.getTaskBytes((short) 31003,
                        new String[] {player.getPlayerName() +
                        "要收你为徒\n1.同意\n2.拒绝",
                        "master_accept " + request.id,
                        "master_cancel " + request.id});
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  GET_FILE_OK);
                seg.writeShort((short) 31003);
                byte[] bytes = stageService.getTaskBytes((short) 31050,
                        new String[] {player.getPlayerName() +
                        "要收你为徒\n1.同意\n2.拒绝",
                        "master_accept " + request.id,
                        "master_cancel " + request.id});
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  GET_FILE_OK);
                seg.writeShort((short) 31050);
                seg.writeShort((short) 2);
                seg.write(bytes);
                connectService.writeTo(seg, players[1].getId());
            } catch (MasterException ex) {
                sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
            }
        */}
    }


    class MasterAcceptProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            int id = Integer.parseInt(command.getParam(0));
            try {
                Changed changed1 = new Changed();
                Changed changed2 = new Changed();
                Master master = masterService.makeRelation(player, id, changed1, changed2);
                log.info("MasterID["+master.getMasterId()+"] PrenticeId["+master.getPrenticeId()+"]Master");
                connectService.sendGetItem(changed1, master.getMasterId(), (byte) 20);
                connectService.sendGetItem(changed2, master.getPrenticeId(), (byte) 20);
                sendMessage(master.getMasterId(), master.getPrenticeName() + "已经成为你的徒弟");
                sendMessage(master.getPrenticeId(), master.getMasterName() + "已经成为你的师傅");
            } catch (MasterException ex) {
                sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
            }
        }
    }


    class MasterCancelAcceptProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            int id = Integer.parseInt(command.getParam(0));
            try {
                MasterService.Request request = masterService.cancelRelation(player, id);
                sendMessage(request.sourceId, "对方拒绝成为你的徒弟,你们没有商量好么?");
            } catch (MasterException ex) {
                sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
            }
        }
    }

    // 由于师徒改造目前屏蔽原拜师代码
    class MasterUnRelationProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {/*
            try {
                Changed changed1 = new Changed();
                Changed changed2 = new Changed();
                Master master = masterService.unRelation(player, changed1, changed2);
                sendGetItem(changed1, command.getSerial(), command.getSessionId(), (byte) 20);
                connectService.sendGetItem(changed2, master.getMasterId(), (byte) 20);
                sendMessage("你已经光荣出师了", command.getSerial(), command.getSessionId());
                log.info("LowUnMaster MasterID[" + master.getMasterId() + "] changed[" +
                         Utils.getHexdump(changed1.toBytes()) + "]PrenticeID[" + master.getPrenticeId() + "] changed[" +
                         Utils.getHexdump(changed2.toBytes()) + "]");
            } catch (MasterException ex) {
                sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
            }
        */}
    }


    class PreSingleUnMasterProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() == 1) {
                int prenticeId = Integer.parseInt(command.getParam(0));
                Master master = masterService.getRelation(player, prenticeId);
                if (master == null) {
                    sendMessage("不存在此师徒关系", command.getSerial(), command.getSessionId());
                }
//                WorldPlayer prentice = playerService.loadWorldPlayer(prenticeId);
//                boolean acquire = false;
//                if(prentice!=null){
//                    playerService.acquire(prentice);
//                    acquire = true;
//                }
                WorldPlayer prentice = playerService.getWorldPlayerAndCatch(prenticeId);
                if (prentice.getLevel() > 13) {
                    if (prentice.getLastLoginTime().getTime() + 15 * 3600 * 24L > System.currentTimeMillis()) {
                      /*  byte[] bytes = stageService.getTaskBytes((short) 31003,
                                new String[] {
                                master.getPrenticeName() + "在最近15天内有上线记录，你将会受到荣誉惩罚，是否继续?\n1.是\n2.否",
                                "single_unmaster " + master.getPrenticeId()});
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                GET_FILE_OK, command.getSerial(), command.getSessionId());
                        seg.writeShort((short) 31003);*/
                    	byte[] bytes = stageService.getTaskBytes((short) 31050,
                                new String[] {
                                master.getPrenticeName() + "在最近15天内有上线记录，你将会受到荣誉惩罚，是否继续?\n1.是\n2.否",
                                "single_unmaster " + master.getPrenticeId()});
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                GET_FILE_OK, command.getSerial(), command.getSessionId());
                        seg.writeShort((short) 31050);
                        seg.writeShort((short) 2);
                        seg.write(bytes);
                        write(seg);
                    } else {
                       /* byte[] bytes = stageService.getTaskBytes((short) 31003,
                                new String[] {
                                master.getPrenticeName() + "在最近15天没有上线记录，你将不会受到荣誉惩罚，是否继续?\n1.是\n2.否",
                                "single_unmaster " + master.getPrenticeId()});
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                GET_FILE_OK, command.getSerial(), command.getSessionId());
                        seg.writeShort((short) 31003);*/
	                	byte[] bytes = stageService.getTaskBytes((short) 31050,
	                             new String[] {
	                             master.getPrenticeName() + "在最近15天没有上线记录，你将不会受到荣誉惩罚，是否继续?\n1.是\n2.否",
	                             "single_unmaster " + master.getPrenticeId()});
	                    UWAPSegment seg = new UWAPSegment(ClientConstants.
	                             GET_FILE_OK, command.getSerial(), command.getSessionId());
	                    seg.writeShort((short) 31050);
	                    seg.writeShort((short) 2);
                        seg.write(bytes);
                        write(seg);
                    }
                } else {
                 /*   byte[] bytes = stageService.getTaskBytes((short) 31003,
                            new String[] {
                            master.getPrenticeName() + "的等级小于14级，你将不会受到荣誉惩罚，是否继续?\n1.是\n2.否",
                            "single_unmaster " + master.getPrenticeId()});
                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                            GET_FILE_OK, command.getSerial(), command.getSessionId());
                    seg.writeShort((short) 31003);*/
            	   byte[] bytes = stageService.getTaskBytes((short) 31050,
                           new String[] {
                           master.getPrenticeName() + "的等级小于14级，你将不会受到荣誉惩罚，是否继续?\n1.是\n2.否",
                           "single_unmaster " + master.getPrenticeId()});
                   UWAPSegment seg = new UWAPSegment(ClientConstants.
                           GET_FILE_OK, command.getSerial(), command.getSessionId());
                   seg.writeShort((short) 31050);
                    seg.writeShort((short) 2);
                    seg.write(bytes);
                    write(seg);
                }
//                if(acquire){
//                    playerService.savePlayer(prentice);
//                }
                playerService.releasePlayer(prentice);
            }
        }
    }


    class MasterRequestIllegalUnRelationProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            Master[] masters = masterService.getRelation(player);
            if (masters != null) {
                byte[] bytes = stageService.getTaskBytes((short) 31010, getUnRelationString(masters));
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  GET_FILE_OK,
                                                  command.getSerial(),
                                                  command.getSessionId());
                seg.writeShort((short) 31010);
                seg.writeShort((short) 2);
                seg.write(bytes);
                write(seg);
            } else {
                Master master = masterService.getMasterRelation(player);
                if (master != null) {
                   /* byte[] bytes = stageService.getTaskBytes((short) 31003,
                            new String[] {
                            "你要强制解除师徒关系吗?\n1.是\n2.否",
                            "single_unmaster"});
                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                            GET_FILE_OK, command.getSerial(), command.getSessionId());
                    seg.writeShort((short) 31003);*/
                	byte[] bytes = stageService.getTaskBytes((short) 31050,
                             new String[] {
                             "你要强制解除师徒关系吗?\n1.是\n2.否",
                             "single_unmaster"});
                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                             GET_FILE_OK, command.getSerial(), command.getSessionId());
                    seg.writeShort((short) 31050);
                    seg.writeShort((short) 2);
                    seg.write(bytes);
                    write(seg);
                } else {
                    sendMessage("你没有师徒关系存在", command.getSerial(), command.getSessionId());
                }
            }
        }

        private String[] getUnRelationString(Master[] masters) {
            String[] ret = new String[masters.length + 4];
            ret[0] = (masters.length + 1) + "";
            ret[1] = "1";
            StringBuilder sb = new StringBuilder();
            sb.append("你要强制解除师徒关系吗?");
            int i = 0;
            for (; i < masters.length; i++) {
                sb.append("\n");
                sb.append(i + 1);
                sb.append(".");
                sb.append(masters[i].getPrenticeName());
            }
            sb.append("\n");
            sb.append(i + 1);
            sb.append(".");
            sb.append("取消");
            ret[2] = sb.toString();
            i = 0;
            for (; i < masters.length; i++) {
                ret[i + 3] = "pre_single_unmaster " + masters[i].getPrenticeId();
            }
            ret[i + 3] = "ok";
            return ret;
        }
    }


    class MasterIllegalUnRelationProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() >= 1) { //师傅的请求
                int prenticeId = Integer.parseInt(command.getParam(0));
                try {
                    Changed changed = new Changed();
                    log.info("ID[" + player.getId() + "] SingleUnMaster PrenticeID[" + prenticeId + "]Money[" +
                             player.getMoeny() + "] Credit[" + player.getCredit() + "] TRY");
                    Master master = masterService.IllegalUnRelation(player, prenticeId, changed);
                    sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 20);
                    sendMessage(player.getId(), "与" + master.getPrenticeName() + "的师徒关系已经解除");
                    log.info("ID[" + player.getId() + "] SingleUnMaster SUCCESS");
                } catch (MasterException ex) {
                    log.info("ID[" + player.getId() + "] SingleUnMaster FAIL");
                    sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
                }
            } else { //徒弟的请求
                try {
                    Changed changed = new Changed();
                    log.info("ID[" + player.getId() + "] SingleUnMaster Money[" + player.getMoeny() + "] Credit[" +
                             player.getCredit() + "] TRY");
                    Master master = masterService.IllegalUnRelation(player, changed);
                    sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 20);
                    sendMessage(player.getId(), "与" + master.getMasterName() + "的师徒关系已经解除");
                    log.info("ID[" + player.getId() + "] SingleUnMaster Money[" + player.getMoeny() + "] Credit[" +
                             player.getCredit() + "] SUCCESS");
                } catch (MasterException ex) {
                    log.info("ID[" + player.getId() + "] SingleUnMaster Money[" + player.getMoeny() + "] Credit[" +
                             player.getCredit() + "] FAIL");
                    sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
                }
            }
        }
    }


    class GuildEnterForProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            try {
                guildBattleField.enterfor(player);
                sendMessage("报名成功", command.getSerial(), command.getSessionId());
            } catch (BattleFieldException ex) {
                sendMessage(ex.getMessage(),
                            command.getSerial(),
                            command.getSessionId());
            }
        }
    }
    
    class CmccChargeProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if(command.getParamCount()==1){
                int value = Integer.parseInt(command.getParam(0));
                if(value<1||value>100){
                    sendMessage("充值数额错误！", command.getSerial(), command.getSessionId());
                    return;
                }
                cmccCharge(player,command.getSessionId(),command.getSerial(),value);
            }
        }
    }


    class ItemUnMarryProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            Changed changed1 = new Changed();
            Changed changed2 = new Changed();
            IItem item = null;
            log.info("ID[" + player.getId() + "] ItemSingleUnMarry TRY");
            if (player.getSex() == 0)
                item = player.completeRemoveItem(550006, 1, changed1);
            else
                item = player.completeRemoveItem(550006, 1, changed2);
            if (item != null) {
                Mate mate = mateService.itemUnMarry(player, changed1, changed2);
                connectService.sendGetItem(changed1, mate.getHusbandId(), (byte) 22);
                connectService.sendGetItem(changed2, mate.getWifeId(), (byte) 22);
                sendMessage(player.getId(), "你已经成功解除了婚姻关系。");
                log.info("ID[" + player.getId() + "] ItemSingleUnMarry Husband[" + mate.getHusbandId() + "] Wife[" +
                         mate.getWifeId() + "]");

                
                if (player.getSex() == 0){//男
                	WorldPlayer p = playerService.getWorldPlayer(mate.getWifeId());
    				if (p != null && p.getState() == WorldPlayer.ONLINE) {//在线
    					//通知本人
       				 	UWAPSegment seg = new UWAPSegment(ClientConstants.FRIENDS_STATUS);
       			        seg.write((byte)1);
       			        seg.writeInt(player.getId());
       			        seg.writeBoolean(false);
       			        seg.writeShort((short)player.getFriendFavorite(p));
       			        seg.writeInt(0);
       			        connectService.writeTo(seg,player.getId());
       			        //通知妻子
       			        seg = new UWAPSegment(ClientConstants.FRIENDS_STATUS);
    			        seg.write((byte)1);
    			        seg.writeInt(p.getId());
    			        seg.writeBoolean(false);
    			        seg.writeShort((short)p.getFriendFavorite(player));
    			        seg.writeInt(0);
    			        connectService.writeTo(seg,p.getId());
    				}
                }else{//女
                	WorldPlayer p = playerService.getWorldPlayer(mate.getHusbandId());
    				if (p != null && p.getState() == WorldPlayer.ONLINE) {//在线
    					//通知本人
       				 	UWAPSegment seg = new UWAPSegment(ClientConstants.FRIENDS_STATUS);
       			        seg.write((byte)1);
       			        seg.writeInt(player.getId());
       			        seg.writeBoolean(false);
       			        seg.writeShort((short)player.getFriendFavorite(p));
       			        seg.writeInt(0);
       			        connectService.writeTo(seg,player.getId());
       			        //通知丈夫
       			        seg = new UWAPSegment(ClientConstants.FRIENDS_STATUS);
    			        seg.write((byte)1);
    			        seg.writeInt(p.getId());
    			        seg.writeBoolean(false);
    			        seg.writeShort((short)p.getFriendFavorite(player));
    			        seg.writeInt(0);
    			        connectService.writeTo(seg,p.getId());
    				}
                }
            } else {
                log.info("ID[" + player.getId() + "] ItemSingleUnMarry FAIL Item Not Found");
            }
        }
    }


    class ItemSingleUnMasterProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() >= 1) { //师傅的请求
                int prenticeId = Integer.parseInt(command.getParam(0));
                try {
                    log.info("ID[" + player.getId() + "] ItemSingleUnMaster PrenticeID[" + prenticeId + "] TRY");
                    Changed changed = new Changed();
                    IItem item = player.completeRemoveItem(550007, 1, changed);
                    if (item != null) {
                        Master master = masterService.itemUnRelation(player, prenticeId, changed);
                        sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 20);
                        sendMessage(player.getId(), "与" + master.getPrenticeName() + "的师徒关系已经解除");
                        //解除关系之后需要加入师傅列表
                        playerService.addMasterPlayer(player, changed);
                        log.info("ID[" + player.getId() + "] ItemSingleUnMaster SUCCESS");
                        WorldPlayer apprentice = playerService.getWorldPlayer(master.getPrenticeId());
                        if(apprentice == null || !apprentice.online()){
                        	mailService.sendMail(master.getPrenticeId(), master.getPrenticeName(), -1, "系统", "师徒关系解除", "您的师傅<cff0000>" + player.getPlayerName() + "</c>已经跟您解除了师徒关系。", null, 0, true);
                        }else{
                        	sendMessage(apprentice.getId(), "您的师傅<cff0000>" + player.getPlayerName() + "</c>已经跟您解除了师徒关系。");
                        }
                    } else {
                        log.info("ID[" + player.getId() + "] ItemSingleUnMaster FAIL Item Not Found");
                    }
                } catch (MasterException ex) {
                    log.info("ID[" + player.getId() + "] ItemSingleUnMaster FAIL");
                    sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
                }
            } else { //徒弟的请求
                try {
                    Changed changed = new Changed();
                    log.info("ID[" + player.getId() + "] ItemSingleUnMaster TRY");
                    IItem item = player.completeRemoveItem(550007, 1, changed);
                    if (item != null) {
                        Master master = masterService.itemUnRelation(player, changed);
                        sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 20);
                        sendMessage(player.getId(), "与" + master.getMasterName() + "的师徒关系已经解除");
                        //解除关系后师傅需要把师傅加入师傅列表 
                        WorldPlayer masterPlayer = playerService.getWorldPlayer(master.getMasterId());
                        if(masterPlayer == null || !masterPlayer.online()){
                        	mailService.sendMail(master.getMasterId(), master.getMasterName(), -1, "系统", "师徒关系解除", "您的徒弟<cff0000>" + player.getPlayerName() + "</c>已经跟您解除了师徒关系。", null, 0, true);
                        }else{
                        	sendMessage(masterPlayer.getId(), "您的徒弟<cff0000>" + player.getPlayerName() + "</c>已经跟您解除了师徒关系。");
                        }
                        playerService.addMasterPlayer(masterPlayer, changed);
                        log.info("ID[" + player.getId() + "] ItemSingleUnMaster SUCCESS");
                    } else {
                        log.info("ID[" + player.getId() + "] ItemSingleUnMaster FAIL Item Not Found");
                    }
                } catch (MasterException ex) {
                    log.info("ID[" + player.getId() + "] ItemSingleUnMaster FAIL");
                    sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
                }
            }
        }
    }


    class InvestigationProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() >= 2) {
                String answer = command.getParamString(1);
                log.info("ID[" + player.getId() + "] Name[" + player.getPlayerName() + "] InvestigationID[" +
                         command.getParam(0) + "] Answer[" + answer +
                         "]");
                sendMessage(player.getId(), "感谢您的参与，您的答案已经被记录。");
            }
        }
    }


    class AddFriendProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() == 1) {
                String name = command.getParam(0);
                if (name.length() == 0) {
                    sendMessage("未找到此用户", command.getSerial(), command.getSessionId());
                    return;
                }
                int id = playerService.getPlayerId(name);
                if (id == -1) {
                    sendMessage("未找到此用户", command.getSerial(), command.getSessionId());
                    return;
                }
                boolean isLoad = false;
//                WorldPlayer friend = playerService.getWorldPlayer(id);
//                if(friend == null){
//                	friend = playerService.loadWorldPlayer(id);
//                	isLoad = true;
//                }
                WorldPlayer friend = playerService.getWorldPlayerAndCatch(id);
                long loginTime = 0;
                if(friend.getLastLoginTime() != null){
                	loginTime = friend.getLastLoginTime().getTime();
                }
//                if(isLoad){
//                	playerService.unRegistry(friend);
//                }
                playerService.releasePlayer(friend);
                int ret = player.addFriend(id, name, loginTime);
                if (ret == 1) {
                    sendMessage("好友已经达到最大数量", command.getSerial(), command.getSessionId());
                    return;
                } else if (ret == 2) {
                    sendMessage("已经存在此好友", command.getSerial(), command.getSessionId());
                    return;
                } else if (ret == 0) {
                    friendService.addedFriend(player, id, name);

                    byte[] bytes = stageService.getTaskBytes((short) 31020,
                            new String[] {id + "", name});
                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                            GET_FILE_OK, command.getSerial(),
                            command.getSessionId());
                    seg.writeShort((short) 31020);
                    seg.writeShort((short) 2);
                    seg.writeInt((int)(loginTime / 1000));
                    seg.write(bytes);
                    write(seg);
                    Friend[] friends = player.getFriends();
                    seg = new UWAPSegment(ClientConstants.FRIENDS_STATUS,
                                          command.getSerial(),
                                          command.getSessionId());
                    seg.write((byte) friends.length);
                    long now = new Date().getTime();
                    for (int i = 0; i < friends.length; i++) {
                        WorldPlayer p = playerService.getWorldPlayer(friends[i].getId());
                        boolean online = p != null && p.getState() == WorldPlayer.ONLINE;
                        seg.writeInt(friends[i].getId());
                        seg.writeBoolean(online);
                        seg.writeShort((short) friends[i].getFavorite());
                        seg.writeInt(online ? 0 : Utils.getLoginTimeSecond(now, friends[i].getLoginTime()));
                    }
                    write(seg);
                    sendMessage("好友添加成功", command.getSerial(), command.getSessionId());
                }
            }
        }
    }


    class HouseProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() == 1) {
                int destId = Integer.parseInt(command.getParam(0));
                try {
                    HouseInstance instance = houseModel.preTry(player, destId);
                    HouseInstance hi = houseModel.tryGotoInstance(instance.getId(), player, -1);
                    if (hi != null) {
                        InstanceDefinition idf = instance.getDefinition();
                        sendGotoMap(player.getId(), idf.getMap(), idf.getX(),
                                    idf.getY());
                        Utils.log(log, player.getId(), command.getAppType(),
                                  "GotoHouse[" + idf.getId() + "]");
                    }
                } catch (HouseException ex) {
                    sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
                } catch (InstanceException ex) {
                    sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
                }
            }
        }
    }


    


    class MoveHouseProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() == 1) {
                short areaId = Short.parseShort(command.getParam(0));
                HouseData hd = houseModel.getHouseByPlayerId(player.getId());
                if (hd != null) {
                    if (hd.getAreaId() != areaId) {
                        int money = hd.getLevel() * hd.getLevel() * 500;
                        if (player.getMoeny() >= money) {
                            Changed changed = new Changed();
                            player.decMoney(money, changed);
                            hd.setAreaId(areaId);
                            sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 66);
                            sendMessage("你的家已经设在此地", command.getSerial(), command.getSessionId());
                        } else {
                            sendMessage("你的搬家费用不够", command.getSerial(), command.getSessionId());
                        }
                    } else {
                        sendMessage("你的房屋已经在此地", command.getSerial(), command.getSessionId());
                    }
                } else {
                    sendMessage("你还没有房产", command.getSerial(), command.getSessionId());
                }
            }
        }
    }


    class RequestBuyHouseProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() == 1) {
                if (player.getLevel() < 12) {
                    sendMessage("12级以后才能购买房产", command.getSerial(), command.getSessionId());
                } else {
                    short areaId = Short.parseShort(command.getParam(0));
                    //                1.	简约独居（1i）
                    //                2.	温馨家园（1080i）
                    //                3.	豪华大宅（2880i）
                    //                4.	花园豪宅（5760i）
                    //                5.	家族城堡（11520i）
                    //                6.	取消购买
                    Collection<Styles> houses = Houses.getStyles();
                    String[] question = new String[4 + houses.size()];
                    question[0] = "" + (houses.size() + 1);
                    question[1] = "1";
                    question[2] = getQuestionString(houses, player);
                    String[] an = getAnswerString(houses, areaId);
                    System.arraycopy(an, 0, question, 3, an.length);
                    byte[] bytes = stageService.getTaskBytes((short) 31010,
                            question);
                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                            GET_FILE_OK,
                            command.getSerial(),
                            command.getSessionId());
                    seg.writeShort((short) 31010);
                    seg.writeShort((short) 2);
                    seg.write(bytes);
                    write(seg);
                }
            }
        }

        private String getQuestionString(Collection<Styles> houses, WorldPlayer player) {
            StringBuilder sb = new StringBuilder(300);
            int i = 1;
            for (Styles style : houses) {
                sb.append(i);
                sb.append(".");
                sb.append(style.getDesc());
                sb.append("(");
                int price = style.getPrice();
                if (player.isSubscribe()) {
                    price = Utils.getDiscountPrice(price,Discount.MDISCOUNT);
//                    price = price * Discount.MDISCOUNT / 100;
                } else {
                    price = Utils.getDiscountPrice(price,Discount.DISCOUNT);
//                    price = price * Discount.DISCOUNT / 100;
                }
                sb.append(price);
                sb.append(Server.iMoneyChar);
                // 卓望版本显示RMB价格
                if (Server.iMoneyType == Server.IMONEY_TYPE_CMCC) {
                    sb.append(")(");
                    sb.append(Utils.getRMBPrice(price));
                }
                sb.append(")\n");
                i++;
            }
            sb.append(i);
            sb.append(".取消购买");
            return sb.toString();
        }

        private String[] getAnswerString(Collection<Styles> houses, short areadId) {
            String[] ret = new String[houses.size() + 1];
            int i = 0;
            for (Styles style : houses) {
                ret[i] = "requestbuyhouse2 " + style.getLevel() + " " + areadId;
                i++;
            }
            ret[ret.length - 1] = "";
            return ret;
        }
    }
    
    class DeleteEnemysPreocessor implements CommandProcessor{
      	public void process(WorldPlayer player, Command command) throws Exception {
      		int enemyId = Integer.parseInt(command.getParam(0));
      		if(enemyId != -1){
      			synchronized (player) {
					if(player.deleteEnemy(enemyId)){
						sendMessage("删除仇人成功。", command.getSerial(), command.getSessionId());
					}else{
						sendMessage("没找到相应的仇人，请刷新后重试。", command.getSerial(), command.getSessionId());
					}
				}
      		}else{
      			sendMessage("没找到相应的仇人。", command.getSerial(), command.getSessionId());
      		}
      	}
    }
    class LetItBeProcessor implements CommandProcessor{
		public void process(WorldPlayer player, Command command)
				throws Exception {
			int destid = Integer.valueOf(command.getParam(0));
			synchronized (player) {
				WorldPlayer dest = playerService.getWorldPlayerAndCatch(destid);
				sendMessage("你已经和" + dest.getPlayerName() + "打招呼了，去私聊记录里查看吧",command.getSerial(),command.getSessionId());
				String msg = "相遇的几率是几十亿分之一，既然如此，我们何不好好珍惜？愿佛祖保佑我们的友谊天长地久，阿门！";
				String msg2 = "相遇的几率是几十亿分之一，既然如此，我们何不好好珍惜？愿佛祖保佑我们的友谊天长地久，阿门！";
//				chatService.sendPrivateMessage(dest.getId(), dest.getPlayerName(), player.getId(), msg);//自身
				chatService.sendPrivateMessage(player.getId(), player.getPlayerName(), dest.getId(), msg2);//对方
				
				int dropGroupId = 402;	//随缘物语掉落组
				Changed changed = new Changed();
				DropGroup group = DropGroups.getDropGroup(dropGroupId, player.getLevel());
				if(group == null){
					playerService.releasePlayer(dest);
					throw new ITimesException("物品出错！",command.getSerial(),command.getSessionId(),command.getAppType());
				}
				int rate = rnd.nextInt(group.getRate());
				DropItem dropItem = group.calcDropItem(rate);
				int count = getCount(rnd, dropItem.getMin(), dropItem.getMax());
				int itemCount = player.getItemCount(dropItem.getItem().getItemId());
				boolean isFull = false;
				if(itemCount > 0 && player.isFull() && itemCount + count < 100 && dropItem.getItem().getType() != IItem.TYPE_EQU){
				}else if(player.isFull() || itemCount + count > 99){
					isFull = true;
				}
				//自己
				if(player.isFull() || player.getItemCount(dropItem.getItem().getItemId()) + count > 99){
					byte[] att = ItemUtils.item2dbAttachment(dropItem.getItem().newInstance(), count);
					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", dropItem.getItem().getName() + "*" + count, "光棍节礼品", att, 0, true);
					if(player.getItemCount(dropItem.getItem().getItemId()) + count > 99){
						sendMessage("您的物品个数已达上限，奖励物品已经发送到邮箱，请及时查收。", command.getSerial(), command.getSessionId());
					}else{
						sendMessage("您的背包已满，奖励物品已经发送到邮箱，请及时查收。", command.getSerial(), command.getSessionId());
					}
				}else{
					player.addItem(dropItem.getItem(), count, changed, player.getClientDataVersion());
				}
				sendGetItem(changed, command.getSerial(), command.getSessionId(), command.getAppType());
                playerService.releasePlayer(dest);
			}
		}
    	
    }
    
    class QuestionProcessor implements CommandProcessor {
        String cmd;

        public QuestionProcessor(String c) {
            cmd = c;
        }

        public void process(WorldPlayer player, Command command) throws Exception {
            QuestionControl control = new QuestionControl(player);

            if (cmd.equals("begin") || cmd.equals("itembegin")) {
                control.initQuestionState();

                write(QuestionControl.getQuestionGoonSegment(control, player, stageService, command.getSerial(), command.getSessionId()));

                if(cmd.equals("itembegin")) {
                	int itemId = 200171;
                	IItem item = Items.getTemplate(itemId).newInstance();
                    if (item.getType() == IItem.TYPE_BASIC) { // 基本物品在客户端就已经扣除了
                        player.completeRemoveItem(item, 1, null);
                    } else {
                    	Changed changed = new Changed();
                        player.completeRemoveItem(item, 1, changed);
                        sendGetItem(changed, command.getSerial(),command.getSessionId(),(byte) 4);
                    Utils.log(log, player.getId(), command.getAppType(),
                              "SubType[" + 20 + "]Item[" +
                              Utils.getHexdump(item.toDbBytes()) +
                              "]Money[" +
                              player.getMoeny() + "]");
                    }
                }
            } else if (cmd.equals("again")) {
                if(control.getQuestionState() != QuestionControl.Question_Goon){
                    sendMessage("答题数据异常", command.getSerial(), command.getSessionId());
                }else{
                    write(QuestionControl.getQuestionGoonSegment(control, player, stageService, command.getSerial(), command.getSessionId()));
                }
            } else if (cmd.equals("answer")) {
                if(control.getQuestionState() != QuestionControl.Question_Goon){
                    sendMessage("答题数据异常", command.getSerial(), command.getSessionId());
                }else{
                    int answer = Integer.parseInt(command.getParam(0));

                    control.answerQuestion(answer);

                    boolean right = false;

                    if(control.getQuestionState() == QuestionControl.Question_Goon || control.getQuestionState() == QuestionControl.Question_Succeed){
                        right = true;
                    }

                    com.pip.itimes.server.bean.Question q = Server.instance.questionService.getQuestion(player.
                            getQuestionId());
                    if (q == null) {
                        com.pip.itimes.server.bean.Question qd = new com.pip.itimes.server.bean.Question();
                        qd.setQuestionId(player.getQuestionId());
                        qd.setSucceed(right? 1: 0);
                        qd.setFail(right? 0: 1);
                        Server.instance.questionService.addQuestion(qd);
                    } else {
                        if(right){
                            q.setSucceed(q.getSucceed() + 1);
                        }else{
                            q.setFail(q.getFail() + 1);
                        }

                        Server.instance.questionService.setQuestion(q);
                    }

                    if(control.getQuestionState() == QuestionControl.Question_Goon){
                        write(QuestionControl.getQuestionGoonSegment(control, player, stageService, command.getSerial(), command.getSessionId()));
                    }else if(control.getQuestionState() == QuestionControl.Question_Succeed){
                        write(QuestionControl.getQuestionSucceedSegment(control, player, stageService, command.getSerial(), command.getSessionId()));

                        int exp = control.getPriceExp();
                        int money = control.getPriceMoney();

                        log.info("ID[" + player.getId() + "]Question Meed EXP[" + exp + "]MONEY[" + money + "]");

                        Changed changed = new Changed();
                        int level_tmp = player.getLevel();
                        player.addExp(exp, changed);
                        
                        if (level_tmp<player.getLevel()){
                        	//推荐人通用函数
	                    	playerService.recommendBalance(player, "Question");
	                    	//尝试加到师傅的列表中
                        	playerService.addMasterPlayer(player, changed);
                        }
                        
                        player.addMoney(money, changed);
                        sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 22);
                    }else if(control.getQuestionState() == QuestionControl.Question_Error){
                        write(QuestionControl.getQuestionErrorSegment(control, player, stageService, command.getSerial(), command.getSessionId()));

                        int exp = control.getPriceExp();
                        int money = control.getPriceMoney();

                        log.info("ID[" + player.getId() + "]Question Wrong Meed EXP[" + exp + "]MONEY[" + money + "]");

                        Changed changed = new Changed();
                        int level_tmp = player.getLevel();
                        player.addExp(exp, changed);
                        
                        if (level_tmp<player.getLevel()){
                        	//推荐人通用函数
	                    	playerService.recommendBalance(player, "Question");
	                    	//尝试加到师傅的列表中
                        	playerService.addMasterPlayer(player, changed);
                        }
                        player.addMoney(money, changed);
                        sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 22);
                    }
                }
            } else if (cmd.equals("pause")) {
                write(QuestionControl.getQuestionPauseSegment(control, player, stageService, command.getSerial(), command.getSessionId()));
            } else if (cmd.equals("next")) {
                int itemId = 200175; // <= 物品ID
                IItem item = Items.getTemplate(itemId).newInstance();
                if (item == null) {
                    Utils.log(log, player.getId(), command.getAppType(),
                              "SubType[" + 1 + "]ItemId[" +
                              itemId + "] Error");
                } else {
                	int petId = -1;
                    Changed changed = new Changed();
                    Utils.log(log, player.getId(), command.getAppType(),
                              "SubType[" + 1 + "]Item[" +
                              Utils.getHexdump(item.toDbBytes()) +
                              "]Money[" +
                              player.getMoeny() + "]TRY");

                    if (item instanceof IEffectItem) {
                        boolean successed = false;
                        try {
                            int[] result = bufService.playerUseItem(player,
                                    (IEffectItem) item,
                                    changed,true,playerId2Clients.get(player.getId()));
                            successed = result[0] == 1 ? true : false;
                            petId = result[1];
                        } catch (UseItemException ex) {
                            sendMessage(ex.getMessage(), command.getSerial(),
                                        command.getSessionId());
                            return;
                        }

                        if(control.getQuestionState() != QuestionControl.Question_Goon){
                            sendMessage("答题数据异常", command.getSerial(), command.getSessionId());

                            Utils.log(log, player.getId(), command.getAppType(),
                                            "SubType[" + 1 + "]Item[" +
                                            Utils.getHexdump(item.toDbBytes()) +
                                            "]Money[" +
                                            player.getMoeny() + "]" + "Error");
                        }else{
                            if (successed) {
                                control.passQuestion();

                                boolean right = false;

                                if(control.getQuestionState() == QuestionControl.Question_Goon || control.getQuestionState() == QuestionControl.Question_Succeed){
                                    right = true;
                                }

                                com.pip.itimes.server.bean.Question q = Server.instance.questionService.getQuestion(player.
                                        getQuestionId());
                                if (q == null) {
                                    com.pip.itimes.server.bean.Question qd = new com.pip.itimes.server.bean.Question();
                                    qd.setQuestionId(player.getQuestionId());
                                    qd.setSucceed(right? 1: 0);
                                    qd.setFail(right? 0: 1);
                                    Server.instance.questionService.addQuestion(qd);
                                } else {
                                    if(right){
                                        q.setSucceed(q.getSucceed() + 1);
                                    }else{
                                        q.setFail(q.getFail() + 1);
                                    }

                                    Server.instance.questionService.setQuestion(q);
                                }

                                if(control.getQuestionState() == QuestionControl.Question_Goon){
                                    write(QuestionControl.getQuestionGoonSegment(control, player, stageService, command.getSerial(), command.getSessionId()));
                                }else if(control.getQuestionState() == QuestionControl.Question_Succeed){
                                    write(QuestionControl.getQuestionSucceedSegment(control, player, stageService, command.getSerial(), command.getSessionId()));

                                    int exp = control.getPriceExp();
                                    int money = control.getPriceMoney();

                                    log.info("ID[" + player.getId() + "]Question Meed EXP[" + exp + "]MONEY[" + money + "]");

                                    Changed changed1 = new Changed();
                                    int level_tmp = player.getLevel();
                                    player.addExp(exp, changed1);
                                    if (level_tmp<player.getLevel()){
                                    	//推荐人通用函数
            	                    	playerService.recommendBalance(player, "Question");
            	                    	//尝试加到师傅的列表中
                                    	playerService.addMasterPlayer(player, changed1);
                                    }
                                    player.addMoney(money, changed1);
                                    sendGetItem(changed1, command.getSerial(), command.getSessionId(), (byte) 22);
                                    //使用召唤宠物物品后，自动装备
                                    if (changed != null) {
                                    	Pet pet = changed.getPeton(IItem.TYPE_PET);
                                    	if (pet != null){
                                    		//mengjie add 自动装备宠物
                                    		if (player.getPet() == null){
                                    			if (pet.getFavor() <= 15 || 
                                    					pet.getLevel() > player.getLevel() ||
                                    					player.getLevel() < 8){
                                    				
                                    			}else{
                                    				player.setPet(pet);
                                    				byte[] bytes = stageService.getTaskBytes((short) 31033,
                                    						new String[] {String.valueOf(pet.getId())});
                                    				UWAPSegment seg = new UWAPSegment(ClientConstants.
                                    						GET_FILE_OK);
                                    				seg.writeShort((short) 31033);
                                    				seg.writeShort((short) 2);
                                    				seg.write(bytes);
                                    				connectService.writeTo(seg, player.getId());
                                    			}
                                    		}
                                    	}
                                    }
                                    //判断新版本客户端，同步宠物装备信息
                                    if (petId > 0) {
                                		Pet pet = player.getPet(petId);
                                		if (pet != null) {
                                			try{
                                    			UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
                                    			seg.writeShort(ClientConstants.EXTEND_PROTOCOL_PETEQU_LOGIN);
                                    			seg.writeInt(petId);
                                    			Grid[] usedEquipmentsTemp = pet.getUsedEquipments();
                                    			if (usedEquipmentsTemp != null){
                                    				for (int jj = 0;jj<pet.getUsedEquipmentinfo().length;jj++){
                                    					seg.write((byte) pet.getUsedEquipmentinfo()[jj]);
                                    					if (usedEquipmentsTemp[jj] != null){
                                    						if (pet.getUsedEquipmentinfo()[jj] == 1){
                                    							IEquipment equtmp = (IEquipment) usedEquipmentsTemp[jj].item;
                                    							equtmp.setDataVersion(player.getClientDataVersion());
                                    							seg.write(equtmp.toClientBytesWithLevel(pet.getLevel()));
                                    						}
                                    					}
                                    				}
                                    			} else {
                                    				for (int jj = 0;jj<pet.getUsedEquipmentinfo().length;jj++){
                                    					seg.write((byte) pet.getUsedEquipmentinfo()[jj]);
                                    				}
                                    			}
                                    			// 发送宠物升级所需升级经验
                                    			seg.writeInt(Utils.getPetUpLevelExp(pet.getLevel()));
                                    			
                                    			//发送宠物阵营宝石效果
                                        		CampData campData = getCampMainService().getCampData(player.getCamp());
                                        		int value = 0;
                                        		if(campData != null){
                                    		    	List<CampSkillData> list = campData.getSkillDataList();
                                    		    	for(int a = 0; a < list.size(); a++){
                                    		    		CampSkillData temp = (CampSkillData) list.get(a);
                                    		    		CampSkillLevel temp1 = CampConfig.campSkills.get(temp.getEffect()).getLevel(temp.getLevel());
                                    		    		
                                    		    		if(temp1 == null || temp1.getParm1() == 0){
                                    		    			continue;
                                    		    		}else{
                                    		    			if(temp.getEffect() == Buf.CAMP_STONE){//阵营科技中只有阵营宝石buff取值下发
                                    		    				value = temp1.getParm1();
                                    		    				break;
                                    		    			}
                                    		    		}
                                    		    	}
                                    		    }
                                        		seg.writeInt(value);
                                    			
                                    			connectService.writeTo(seg, player.getId());
                                    		} catch (Exception e) {
                                    			log.debug(e, e);
                                    		}
                                    	}
                                    }
                                }
                            }else {
                                write(QuestionControl.getQuestionItemSegment(control, player, stageService, command.getSerial(), command.getSessionId()));
                            }
                        }
                    }

                    sendGetItem(changed, command.getSerial(),
                                command.getSessionId(),
                                (byte) 4);
                    Utils.log(log, player.getId(), command.getAppType(),
                              "SubType[" + 1 + "]Item[" +
                              Utils.getHexdump(item.toDbBytes()) +
                              "]Money[" +
                              player.getMoeny() + "]");
                }
            }
        }
    }
    
	public Map getCommandmap(){
		return commandmap;
	}
	
	class TheArrowOfLoveOkProcessor implements CommandProcessor{
    	public void process(WorldPlayer dest, Command command) throws Exception {
    		int type = Integer.parseInt(command.getParam(0));
    		if(type == 1){	//接受
    			int id = Integer.parseInt(command.getParam(1));
    			WorldPlayer player = playerService.getWorldPlayerAndCatch(id);
    			String msg = player.getPlayerName() + "向" + dest.getPlayerName() + "射出了丘比特之箭，希望与他共坠爱河，携手到老，" + dest.getPlayerName() + "很欣然的接受了他的爱。";
    			//chatService.sendWorldMessage(-1, "系统", msg);
    			int dropGroupId = 403;	//丘比特掉落组
				DropGroup group = DropGroups.getDropGroup(dropGroupId, player.getLevel());
				if(group == null){
					playerService.releasePlayer(player);
					throw new ITimesException("物品出错！",command.getSerial(),command.getSessionId(),command.getAppType());
				}
				int rate = rnd.nextInt(group.getRate());
				DropItem dropItem = group.calcDropItem(rate);
				int count = getCount(rnd, dropItem.getMin(), dropItem.getMax());
				//对方
				byte[] att = ItemUtils.item2dbAttachment(dropItem.getItem().newInstance(), count);
				mailService.sendMail(dest.getId(), dest.getPlayerName(), -1, "系统", dropItem.getItem().getName() + "*" + count, "光棍节礼品", att, 0, true);
				playerService.releasePlayer(player);
    		}else if(type == 2){	//拒绝
    			int id = Integer.parseInt(command.getParam(1));
    			WorldPlayer player = playerService.getWorldPlayerAndCatch(id);
    			if(player != null){
	    			String msg = player.getPlayerName() + "向" + dest.getPlayerName() + "射出了丘比特之箭，希望与他共坠爱河，携手到老，" + dest.getPlayerName() + "摇了摇头：“对不起，你是个好人”";
	    			//chatService.sendWorldMessage(-1, "系统", msg);
					playerService.releasePlayer(player);
    			}
    		}
    	}
    }
	
    class TheArrowOfLoveProcessor implements CommandProcessor{
    	public void process(WorldPlayer player, Command command) throws Exception {
    			
    			int friendId = Integer.parseInt(command.getParam(0));
	    		WorldPlayer friend = playerService.getWorldPlayerAndCatch(friendId);
	    		if(friend != null){
	    			if(friend.online() == false){
	    				sendMessage("您的好友不在线,请选择在线好友",command.getSerial(), command.getSessionId());
	    			}else{
	    				synchronized (player) {
		    				Changed changed = new Changed();
		    				IItem item = player.completeRemoveItem(201470, 1, changed);
		    				if(item != null){
			    				byte[] bytes = stageService.getTaskBytes((short) 31010, new String[] {
			                            "2", "1", "形象" + "孤单寂寞冷的" + player.getPlayerName() + "，在这个寒风来袭的日子里，" +
					    						"极度渴望得到您的温暖和关爱，您愿意把这个受折磨的灵魂从苦逼的光棍军团里拯救出来吗?" +
					    						"\n1.接受\n2.拒绝",
					    						"TheArrowOfLoveOk 1 " + player.getId(),
					    						"TheArrowOfLoveOk 2 " + player.getId()});
			                    UWAPSegment seg = new UWAPSegment(ClientConstants.
			                                                      GET_FILE_OK,
			                                                      command.getSerial(),
			                                                      command.getSessionId());
			                    seg.writeShort((short) 31010);
			                    seg.writeShort((short) 2);
			                    seg.write(bytes);
			    				connectService.writeTo(seg, friendId);
			    				sendGetItem(changed, command.getSerial(), command.getSessionId(), command.getAppType());
			    				int dropGroupId = 403;	//丘比特掉落组
			    				DropGroup group = DropGroups.getDropGroup(dropGroupId, player.getLevel());
			    				if(group != null){
				    				int rate = rnd.nextInt(group.getRate());
				    				DropItem dropItem = group.calcDropItem(rate);
				    				int count = getCount(rnd, dropItem.getMin(), dropItem.getMax());
				    				int itemCount = player.getItemCount(dropItem.getItem().getItemId());
				    				byte[] att = ItemUtils.item2dbAttachment(dropItem.getItem().newInstance(), count);
				    				mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", dropItem.getItem().getName() + "*" + count, "光棍节礼品", att, 0, true);
			    				}
		    				}else{
		    					sendMessage("您没有物品。", command.getSerial(), command.getSessionId());
		    				}
	    				}
	    		  }
	    		playerService.releasePlayer(friend);
	    	}
	    }
    }
    
    class ShopBuyProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            int shopId = Integer.parseInt(command.getParam(0));
            ShopData shop = shopService.getShopData(shopId);
            if (shop == null || shop.getState() != Shop.STATE_SELL ||
                shop.getBuyPlayerId() != player.getId()) {
                throw new ITimesException("购买店铺错误", command.getSerial(),
                                          command.getSessionId(),
                                          command.getAppType());
            }
            synchronized (player) {
                if (player.getMoeny() < shop.getPrice()) {
                    throw new ITimesException("没有足够的钱", command.getSerial(),
                                              command.getSessionId(),
                                              command.getAppType());
                }
                Utils.log(log, player.getId(), command.getAppType(),
                          "SubType[shop_buy]ID[" + shop.getId() + "]Price[" +
                          shop.getPrice() + "]Money[" + player.getMoeny() +
                          "]TRY");
                player.setMoeny(player.getMoeny() - shop.getPrice());
                shop.setBuyPlayerId( -1);
                shop.setPrice( -1);
                shop.setState(Shop.STATE_NORMAL);
                shop.setSellTime(null);
                shopService.saveShop(shop);
                auctionService.setOwner(shop.getId(), player);
                oemService.setState(shop.getId(), Shop.STATE_NORMAL);
                buyService.setState(shop.getId(), Shop.STATE_NORMAL);
                sendMessage(player.getId(), "购买店铺" + shop.getName() + "成功");
                Utils.log(log, player.getId(), command.getAppType(),
                          "SubType[shop_buy]ID[" + shop.getId() + "]Price[" +
                          shop.getPrice() + "]Money[" + player.getMoeny() +
                          "]");
            }
        }
    }
    
    class TongCreateProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            String p0 = command.getParam(0);
            try {
            	if (p0.equalsIgnoreCase("c")){
            		sendMessage("公会名中含有非法字符。", command.getSerial(),
                            command.getSessionId());
            	}else{
	                Tong tong = tongService.creatTong(player, p0);
	                UWAPSegment seg = new UWAPSegment(ClientConstants.
	                                                  TONG_CREATE_OK,
	                                                  command.getSerial(),
	                                                  command.getSessionId());
	                seg.writeString(tong.getTongName());
	                seg.write(Tong.OWNER);
	                seg.writeInt(tongService.getCreateTongMoney());
	                write(seg);
	                Utils.log(log, player.getId(), command.getAppType(),
	                          "SubType[tong_create]ID[" + tong.getId() + "]");
            	}
            } catch (TongException ex1) {
                sendMessage(ex1.getMessage(), command.getSerial(),
                            command.getSessionId());
                return;
            }
        }
    }


    class TongJoinProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            String p0 = command.getParam(0);
            int tongId = Integer.parseInt(p0);
            try {
                tongService.join(player, tongId);
                chatService.sendTongMessage(tongId, -1, "系统",
                                            player.getPlayerName() + "加入公会");
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  TONG_GRANT_OK,
                                                  command.getSerial());
                seg.writeInt(player.getId());
                seg.writeString(player.getTongName());
                seg.write((byte) player.getTongDuty());
                connectService.writeTo(seg, player.getId());
                //mengjie add 添加加入时间
                player.setTonginTime(new Date());

            } catch (TongException ex2) {
                throw new ITimesException(ex2.getMessage(), command.getSerial(),
                                          command.getSessionId(),
                                          command.getAppType());

            }
        }
    }



    class PetRequestPointProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
        	Client client = player.getClient();
        	if(client != null && client.getDataVersion() > 0){
        		UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL,
        				command.getSerial(),
                           command.getSessionId());
        		seg.writeShort((short)ClientConstants.CONN_EXTEND_PET_TRADE);
        		write(seg);

        	}else{
	            byte[] bytes = stageService.getTaskBytes((short) 30020,player.getLevel());
	            UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
	                                              command.getSerial(),
	                                              command.getSessionId());
	            seg.writeShort((short) 31001);
	            seg.writeShort((short) 2);
	            seg.write(bytes);
	            write(seg);
        	}
        }
    }


    class PetRequestRenameProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            Pet p = player.getPet();
            if (p == null) {
                sendMessage("必须携带宠物才能改名", command.getSerial(),
                            command.getSessionId());
            } else {
                int money = Utils.getPetRenameMoney(p);
                byte[] bytes = stageService.getTaskBytes((short) 31001,
                        new String[] {"改名需要" + money + "金钱.是否改名?\n1.是\n2.否",
                        "输入宠物名字:", "pet_rename "});
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  GET_FILE_OK,
                                                  command.getSerial(),
                                                  command.getSessionId());
                seg.writeShort((short) 31001);
                seg.writeShort((short) 2);
                seg.write(bytes);
                write(seg);
            }
        }
    }


    class RefreshAbilityProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            int money = Utils.getRefreshAbilityMoney(player);
            if (player.getMap() != null && player.getMap().getInstance() != null
            		&& player.getMap().getInstance() instanceof CampBattlefieldInstance) {
            	sendMessage("战场中禁止遗忘技能。", command.getSerial(), command.getSessionId());
        		return;
        	}
            if(player.hasBuf(Buf.HOPEOBJECT)){
            	Buf buf = player.getBuf(Buf.HOPEOBJECT);
            	int rate = buf.getValue();
            	money = money * (100 - rate)/100;
            }
            byte[] bytes = stageService.getTaskBytes((short) 31002,
                    new String[] {"遗忘战斗技能需要" + money + "J,是否遗忘?\n1.是\n2.否",
                    "refresh_ability_commit"});
            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                              GET_FILE_OK, command.getSerial(),
                                              command.getSessionId());
            seg.writeShort((short) 31002);
            seg.writeShort((short) 2);
            seg.write(bytes);
            write(seg);
        }
    }

    


    class RefreshSkillProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            int type = Integer.parseInt(command.getParam(0));
            if (type < Player.SKILL_BLACKSMITHING ||
                type > Player.SKILL_MINING ||
                player.getSkillPoint(type) == -1) {
                sendMessage("不能遗忘此生活技能", command.getSerial(),
                            command.getSessionId());
            } else {
                int money = Utils.getRefreshSkillMoney(player, type);
                if(player.hasBuf(Buf.HOPEOBJECT)){
                	Buf buf = player.getBuf(Buf.HOPEOBJECT);
                	int rate = buf.getValue();
                	money = money * (100 - rate)/100;
                }
                byte[] bytes = stageService.getTaskBytes((short) 31002,
                        new String[] {"遗忘生活技能需要" + money +
                        "J,是否遗忘?\n1.是\n2.否",
                        "refresh_skill_commit " + type});
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  GET_FILE_OK,
                                                  command.getSerial(),
                                                  command.getSessionId());
                seg.writeShort((short) 31002);
                seg.writeShort((short) 2);
                seg.write(bytes);
                write(seg);
            }
        }
    }

    


    class RequestBuyHouse2Processor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() == 2) {
                if (player.getLevel() < 12) {
                    sendMessage("12级以后才能购买房产", command.getSerial(), command.getSessionId());
                } else {
                    int level = Integer.parseInt(command.getParam(0));
                    short areaId = Short.parseShort(command.getParam(1));
                    HouseData hd = houseModel.getHouseByPlayerId(player.getId());
                    if (hd != null && hd.getLevel() == level) {
                        sendMessage("已经拥有此级别房产，不用再次购买！", command.getSerial(), command.getSessionId());
                    } else {
                        Collection<HouseTemplate> styles = Houses.getHouseTemplates(level);
                        byte[] bytes = stageService.getTaskBytes((short) 31010,
                                getQuestionString(styles, areaId));
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                GET_FILE_OK,
                                command.getSerial(),
                                command.getSessionId());
                        seg.writeShort((short) 31010);
                        seg.writeShort((short) 2);
                        seg.write(bytes);
                        write(seg);
                    }
                }
            }
        }

        public String[] getQuestionString(Collection<HouseTemplate> styles, short areaId) {
            String[] ret = new String[styles.size() + 4];
            ret[0] = "" + (styles.size() + 1);
            ret[1] = "1";
            int i = 1;
            StringBuilder sb = new StringBuilder();
            for (HouseTemplate style : styles) {
                sb.append(i);
                sb.append(".");
                sb.append(style.getStyleDesc());
                sb.append("\n");
//                sb.append("(");
//                sb.append(style.getStylePrice());
//                sb.append(")");
                ret[i + 2] = "buyhouse " + style.getLevel() + " " + style.getStyle() + " " + areaId;
                i++;
            }
            sb.append(i);
            sb.append(".取消购买");
            ret[2] = sb.toString();
            ret[ret.length - 1] = "";
            return ret;
        }
    }
    
    class BuyWaiterProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            HouseData hd = houseModel.getHouseByPlayerId(player.getId());
            if (hd!=null){
	            HouseTemplate ht = Houses.getHouseTemplate(hd.getLevel(), hd.getStyle());
	            if (hd.isUsedWaiter()) {
	                //sendMessage("你已经雇佣了管家", command.getSerial(), command.getSessionId());
	            	if (Server.iMoneyType == Server.IMONEY_TYPE_CMCC){
                    	connectService.sendMessage(player.getId(), "你已经成功雇佣了管家.(系统将扣除相应" + Server.iMoneyString + "，如" + Server.iMoneyString + "不足系统将自动扣除您的话费。)");
                    }else{
                    	connectService.sendMessage(player.getId(), "你已经成功雇佣了管家.(如无需自动续费请您到家院内找管家NPC修改设置)");
                    }
	            	hd.setCanUseWaiterTime(hd.getCanUseWaiterTime());
	            } else {
	                StoreService.Request request = storeService.request(player, ht.getWaiterPrice()*100,
	                        ht.getWaiterConsumeCode(), command.getSerial(), StoreService.Request.WAITER,ConnectSession2.this);
	                if (request != null) {
	                    sendRequestToAuth(request, player.getAccountId(), player.getkey(), command.getSerial(), command.getSessionId(), true);
	                } else {
	                    throw new ITimesException("不能购买此物品", command.getSerial(), command.getSessionId(), (byte) 20);
	                }
	                int iMoney = (int)(player.getLongIMoney() / 100);
	                if(iMoney < ht.getWaiterPrice()){
	                	sendMessage("您的i币不足无法雇佣管家", command.getSerial(), command.getSessionId());
	                }
	                
	            }
             }
        }
    }

    class AutoBuyWaiterCancelProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            HouseData hd = houseModel.getHouseByPlayerId(player.getId());
            if (hd!=null){
	            if (hd.getAutoBuyWaiter() == 1){
	            	hd.setAutoBuyWaiter(0);
	            	sendMessage("以后您将自动给管家支付薪水，无须您任何操作，管家就会为您服务到底！", command.getSerial(), command.getSessionId());
	            }else{
	            	hd.setAutoBuyWaiter(1);
	            	sendMessage("您的管家将由您手动雇佣使用了，如果忘记了发工资很多精彩服务将与您擦身而过哦。", command.getSerial(), command.getSessionId());
	            }
	            
	            houseModel.saveHouse(hd);
            }
        }
    }
    class WaiterFaceProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            Collection<HouseWaiter> l = Houses.getWaiters();
            byte[] bytes = stageService.getTaskBytes((short) 31010, getQuestion(l));
            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                              GET_FILE_OK,
                                              command.getSerial(),
                                              command.getSessionId());
            seg.writeShort((short) 31010);
            seg.writeShort((short) 2);
            seg.write(bytes);
            write(seg);
        }

        private String[] getQuestion(Collection<HouseWaiter> waiters) {
            String[] ret = new String[waiters.size() + 4];
            ret[0] = (waiters.size() + 1) + "";
            ret[1] = "1";
            StringBuilder sb = new StringBuilder();
            sb.append("哦！我的样子由你决定，你喜欢那种?");
            int i = 1;
            for (HouseWaiter waiter : waiters) {
                sb.append("\n");
                sb.append(i);
                sb.append(".");
                sb.append(waiter.getName());
                i++;
            }
            sb.append("\n");
            sb.append(i);
            sb.append(".");
            sb.append("暂时不选");
            ret[2] = sb.toString();
            i = 3;
            for (HouseWaiter waiter : waiters) {
                ret[i] = "changewaiterface " + waiter.getId();
                i++;
            }
            ret[i] = "ok";
            return ret;
        }
    }


    class ChangeWaiterFaceProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() == 1) {
                int id = Integer.parseInt(command.getParam(0));
                HouseData hd = houseModel.getHouseByPlayerId(player.getId());
                if (hd != null) {
                    HouseWaiter waiter = Houses.getWaiter(id);
                    if (waiter != null) {
                        hd.setWaiterId(id);
                        houseModel.saveHouse(hd);
                        sendMessage("更改管家形象成功,您需要重新进入家园才能看到效果。", command.getSerial(), command.getSessionId());
                    }
                }
            }
        }
    }


    class TongTopListProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() > 0) {
                String parm = command.getParamString(0);

                if (parm.startsWith("1")) {
                    List<String> list = topListService.tongTopList.getTongTopListHot(player, 10);

                    if (list.size() > 0) {
                        UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                        seg.writeShort((short) 9001);
                        seg.writeString("公会热度排行");
                        seg.write((byte) 0);
                        seg.writeShort((short) list.size());

                        for (int i = 0; i < list.size(); i++) {
                            seg.writeInt(i);
                            seg.writeString(list.get(i));
                            seg.writeInt(Utils.CLR_WHITE);
                        }

                        connectService.writeTo(seg, player.getId());
                    } else {
                        sendMessage(TopListService.TOP_LIST_NO_DATA_MESSAGE, command.getSerial(), command.getSessionId());
                    }
                } else if (parm.startsWith("2")) {
                    List<String> list = topListService.tongTopList.getTongTopListOnline(player, 10);

                    if (list.size() > 0) {
                        UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                        seg.writeShort((short) 9002);
                        seg.writeString("公会在线人数排行");
                        seg.write((byte) 0);
                        seg.writeShort((short) list.size());

                        for (int i = 0; i < list.size(); i++) {
                            seg.writeInt(i);
                            seg.writeString(list.get(i));
                            seg.writeInt(Utils.CLR_WHITE);
                        }

                        connectService.writeTo(seg, player.getId());
                    } else {
                        sendMessage(TopListService.TOP_LIST_NO_DATA_MESSAGE, command.getSerial(), command.getSessionId());
                    }
                } else if (parm.startsWith("3")) {
                    List<String> list = topListService.tongTopList.getTongTopListMaxOnline(player, 10);

                    if (list.size() > 0) {
                        UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                        seg.writeShort((short) 9003);
                        seg.writeString("公会当前在线排行榜");
                        seg.write((byte) 0);
                        seg.writeShort((short) list.size());

                        for (int i = 0; i < list.size(); i++) {
                            seg.writeInt(i);
                            seg.writeString(list.get(i));
                            seg.writeInt(Utils.CLR_WHITE);
                        }

                        connectService.writeTo(seg, player.getId());
                    } else {
                        sendMessage(TopListService.TOP_LIST_NO_DATA_MESSAGE, command.getSerial(), command.getSessionId());
                    }
                }
            }
        }
    }


    class HouseTopListProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() > 0) {
                String parm = command.getParamString(0);

                if (parm.startsWith("1")) {
                    List<String> list = topListService.houseTopList.getHouseTopListVisited(player, 10);

                    if (list.size() > 0) {
                        UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                        seg.writeShort((short) 9011);
                        seg.writeString("家园人气榜");
                        seg.write((byte) 0);
                        seg.writeShort((short) list.size());

                        for (int i = 0; i < list.size(); i++) {
                            seg.writeInt(i);
                            seg.writeString(list.get(i));
                            seg.writeInt(Utils.CLR_WHITE);
                        }

                        connectService.writeTo(seg, player.getId());
                    } else {
                        sendMessage(TopListService.TOP_LIST_NO_DATA_MESSAGE, command.getSerial(), command.getSessionId());
                    }
                } else if (parm.startsWith("2")) {
                    List<String> list = topListService.houseTopList.getHouseTopListUsediMoney(player, 10);

                    if (list.size() > 0) {
                        UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                        seg.writeShort((short) 9012);
                        seg.writeString("家园财富榜");
                        seg.write((byte) 0);
                        seg.writeShort((short) list.size());

                        for (int i = 0; i < list.size(); i++) {
                            seg.writeInt(i);
                            seg.writeString(list.get(i));
                            seg.writeInt(Utils.CLR_WHITE);
                        }

                        connectService.writeTo(seg, player.getId());
                    } else {
                        sendMessage(TopListService.TOP_LIST_NO_DATA_MESSAGE, command.getSerial(), command.getSessionId());
                    }
                } else if (parm.startsWith("3")) {
                    List<String> list = topListService.houseTopList.getHouseTopListLeaveMessageList(player, 10);

                    if (list.size() > 0) {
                        UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                        seg.writeShort((short) 9013);
                        seg.writeString("家园交流榜");
                        seg.write((byte) 0);
                        seg.writeShort((short) list.size());

                        for (int i = 0; i < list.size(); i++) {
                            seg.writeInt(i);
                            seg.writeString(list.get(i));
                            seg.writeInt(Utils.CLR_WHITE);
                        }

                        connectService.writeTo(seg, player.getId());
                    } else {
                        sendMessage(TopListService.TOP_LIST_NO_DATA_MESSAGE, command.getSerial(), command.getSessionId());
                    }
                }
            }
        }
    }


    private static final String TONG_BATHHOUSE_HELP =
            "如果你是公会会长，可以召集您的公会成员集体来浴场，然后选择“开启公会浴”，同时您公会的公会荣誉点将被扣除500点。从开启后开始计时，10分钟后所有在此浴场的本公会成员将获得非常诱人的经验奖励，赶快行动吧。";

    class TongBathHouseHelp implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            sendMessage(TONG_BATHHOUSE_HELP, command.getSerial(), command.getSessionId());
        }
    }


    class TongBathHouse implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() == 1) {
                short mapId = Short.parseShort(command.getParam(0));
                BathHouse bath = BathHouse.getBathHouseByMapId(mapId);
                if (bath != null) {
                    try {
                        tongService.addTongBathHouseRequest(player, bath);
                        sendMessage("你已经开启了公会集体浴。", command.getSerial(), command.getSessionId());
                    } catch (TongException ex) {
                        sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
                    }
                }
            }
        }
    }


    class HousePushProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            GameMap map = player.getMap();
            if (map != null && map.getInstance() == null) {
                Changed changed = new Changed();
                if (player.completeRemoveItem(550018, 1, changed) != null) {
                    HouseData hd = houseModel.getHouseByPlayerId(player.getId());
                    if (hd != null) {
                        if (hd.getRule() != House.RULE_FREE) {
                            hd.setRule(House.RULE_FREE);
                            houseModel.saveHouse(hd);
                        }
                    }
                    sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 20);
                    WorldPlayer[] players = map.getPlayers();
                    byte[] bytes = stageService.getTaskBytes((short) 31002,
                            new String[] {player.getPlayerName() + "邀请你到家里做客!\n1.接受\n2.拒绝",
                            "house " + player.getId()});
                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                            GET_FILE_OK);
                    seg.writeShort((short) 31002);
                    seg.writeShort((short) 2);
                    seg.write(bytes);
                    for (int i = 0; i < players.length; i++) {
                        if (players[i].getId() != player.getId())
                            connectService.writeTo(seg, players[i].getId());
                    }
                }

            }else{
            	sendMessage(player.getId(), "您不能在此使用!");
            }
        }
    }


    class PrivateHousePushProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() == 1) {
                String name = command.getParam(0);
                WorldPlayer dest = playerService.getWorldPlayer(name);
                if (player == null || !player.online()) {
                    sendMessage("你的密友目前不在线", command.getSerial(), command.getSessionId());
                    return;
                }
                GameMap map = player.getMap();
                if (map == null || map.getInstance() != null) {
                    sendMessage("对方暂时无法接受邀请", command.getSerial(), command.getSessionId());
                    return;
                }
                Changed changed = new Changed();
                if (player.completeRemoveItem(550019, 1, changed) != null) {
                    sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 20);
                    byte[] bytes = stageService.getTaskBytes((short) 31002,
                            new String[] {player.getPlayerName() + "邀请你到家里做客!\n1.接受\n2.拒绝",
                            "house " + player.getId()});
                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                            GET_FILE_OK);
                    seg.writeShort((short) 31002);
                    seg.writeShort((short) 2);
                    seg.write(bytes);
                    connectService.writeTo(seg, dest.getId());
                }
            }
        }
    }


    class RequestMoveHouseProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() == 1) {
                Short areaId = Short.parseShort(command.getParam(0));
                HouseData hd = houseModel.getHouseByPlayerId(player.getId());
                if (hd != null) {
                    byte[] bytes = stageService.getTaskBytes((short) 31002,
                            new String[] {player.getPlayerName() + "你要将家搬到此地吗？\n1.搬入此地(" +
                            hd.getLevel() * hd.getLevel() * 500 + "J)\n2.不想搬入",
                            "movehouse " + areaId});
                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                            GET_FILE_OK);
                    seg.writeShort((short) 31002);
                    seg.writeShort((short) 2);
                    seg.write(bytes);
                    connectService.writeTo(seg, player.getId());
                }
            }
        }
    }


    class ChargeForProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() == 1) {
                String playerName = command.getParam(0);
                WorldPlayer target = playerService.getWorldPlayer(playerName);
                if (target != null) {
                    Client client = getClient(command.getSessionId());
                    String channel = client == null ? "" : client.channel;
                    byte[] bytes = stageService.getTaskBytes((short) 31012,
                            new String[] {target.getAccountName(), String.valueOf(target.getAccountId()),"1",target.getPlayerName(), "", channel,
                    		ChinaService.IMONEY_ADDITIONAL[0], ChinaService.IMONEY_ADDITIONAL[1], ChinaService.IMONEY_ADDITIONAL[2],
                    		ChinaService.IMONEY_ADDITIONAL[3], ChinaService.IMONEY_ADDITIONAL[4], ChinaService.IMONEY_ADDITIONAL[5],
                    		ChinaService.IMONEY_ADDITIONAL[6]
                    	});
                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                            GET_FILE_OK, command.getSerial(),
                            command.getSessionId());
                    seg.writeShort((short) 31012);
                    seg.writeShort((short) 2);
                    seg.write(bytes);
                    write(seg);
                    log.info("ID["+player.getId()+"] ChrageForPre["+playerName+"]");
                } else {
                    int accountId = playerService.getAccountIdByPlayerName(playerName);
                    if(accountId==-1){
                        sendMessage("找不到指定用户",command.getSerial(),command.getSessionId());
                    }else{
                        getAccountName(command.getSessionId(),command.getSerial(),accountId,playerName);


                        log.info("ID["+player.getId()+"] ChrageForPre["+playerName+"]");
                    }
                }
            }
        }
    }
    
    class PropertyProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws
                Exception {
            String p0 = command.getParam(0);
            if ("renew".equals(p0)) {
            	short mapId = Short.parseShort(command.getParam(1));
                synchronized (player) {
                	if (mapId == player.getMapId()){
                		int hp = player.getHp();
                        int mp = player.getMp();
                        player.adjustProperty();
                        player.repairOldVersionHpMp(player.getClientDataVersion());
                        int maxHp = player.getMaxHp();
                        int maxMp = player.getMaxMp();
                        player.setHp(maxHp);
                        player.setMp(maxMp);
                        Changed changed = new Changed();
//                        changed.addProperty(Changed.HP, maxHp - hp);
                        changed.addProperty(Changed.HP, maxHp);
//                        changed.addProperty(Changed.MP, maxMp - mp);
                        changed.addProperty(Changed.MP, maxMp);
                        Pet p = player.getPet();
                        if (p != null) {
                            int petHp = p.getHp();
                            int petMp = p.getMp();
                            int petMaxHp = p.getMaxHp();
                            int petMaxMp = p.getMaxMp();
                            p.setHp(petMaxHp);
                            p.setMp(petMaxMp);
                            changed.addPetProperty(p, Changed.PET_HP, petMaxHp);
//                                                   petMaxHp - petHp);
                            changed.addPetProperty(p, Changed.PET_MP, petMaxMp);
//                                                   petMaxMp - petMp);
                        }
                        sendGetItem(changed, command.getSerial(),
                                    command.getSessionId(), (byte) 5);
                	}else{
                		//刷恢复包，不作处理
                	}                    
                }
            }
        }
    }


    


    class RequestChangeStyleProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            HouseData hd = houseModel.getHouseByPlayerId(player.getId());
            if (hd == null) {
                sendMessage("你还没有房产，可以通过购买拥有自己的房屋。", command.getSerial(), command.getSessionId());
            } else {
                Collection<HouseTemplate> styles = Houses.getHouseTemplates(hd.getLevel());
                byte[] bytes = stageService.getTaskBytes((short) 31010,
                        getQuestionString(styles, player));
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  GET_FILE_OK,
                                                  command.getSerial(),
                                                  command.getSessionId());
                seg.writeShort((short) 31010);
                seg.writeShort((short) 2);
                seg.write(bytes);
                write(seg);
            }
        }

        private String[] getQuestionString(Collection<HouseTemplate> styles, WorldPlayer player) {
            String[] ret = new String[styles.size() + 4];
            ret[0] = "" + (styles.size() + 1);
            ret[1] = "1";
            int i = 1;
            StringBuilder sb = new StringBuilder();
            for (HouseTemplate style : styles) {
                sb.append(i);
                sb.append(".");
                sb.append(style.getStyleDesc());
                sb.append("(");
                int price = style.getStylePrice();
                if (player.isSubscribe()) {
                    price = Utils.getDiscountPrice(price,Discount.MDISCOUNT);
//                    price = price * Discount.MDISCOUNT / 100;
                } else {
                    price = Utils.getDiscountPrice(price,Discount.DISCOUNT);
//                    price = price * Discount.DISCOUNT / 100;
                }
                sb.append(price);
                sb.append(Server.iMoneyChar);
                // 卓望版本显示RMB价格
                if (Server.iMoneyType == Server.IMONEY_TYPE_CMCC) {
                    sb.append(")(");
                    sb.append(Utils.getRMBPrice(price));
                }
                sb.append(")\n");
                ret[i + 2] = "changestyle " + style.getStyle();
                i++;
            }
            sb.append(i);
            sb.append(".取消购买");
            ret[2] = sb.toString();
            ret[ret.length - 1] = "";
            return ret;
        }
    }


    class RequestPartProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            HouseData hd = houseModel.getHouseByPlayerId(player.getId());
            if (hd == null) {
                sendMessage("你还没有房产，可以通过购买拥有自己的房屋。", command.getSerial(), command.getSessionId());
            } else {
                HouseTemplate ht = Houses.getHouseTemplate(hd.getLevel(), hd.getStyle());
                Collection<HousePart> parts = ht.getParts();
                byte[] bytes = stageService.getTaskBytes((short) 31010,
                        getQuestionString(parts, player));
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  GET_FILE_OK,
                                                  command.getSerial(),
                                                  command.getSessionId());
                seg.writeShort((short) 31010);
                seg.writeShort((short) 2);
                seg.write(bytes);
                write(seg);
            }
        }

        private String[] getQuestionString(Collection<HousePart> parts, WorldPlayer player) {
            String[] ret = new String[parts.size() + 4];
            ret[0] = "" + (parts.size() + 1);
            ret[1] = "1";
            int i = 1;
            StringBuilder sb = new StringBuilder();
            for (HousePart part : parts) {
                sb.append(i);
                sb.append(".");
                sb.append(part.getDesc());
                sb.append("(");
                int price = part.getPrice();
                if (player.isSubscribe()) {
                    price = Utils.getDiscountPrice(price,Discount.MDISCOUNT);
//                    price = price * Discount.MDISCOUNT / 100;
                } else {
                    price = Utils.getDiscountPrice(price,Discount.DISCOUNT);
//                    price = price * Discount.DISCOUNT / 100;
                }
                sb.append(price);
                sb.append(Server.iMoneyChar);
                // 卓望版本显示RMB价格
                if (Server.iMoneyType == Server.IMONEY_TYPE_CMCC) {
                    sb.append(")(");
                    sb.append(Utils.getRMBPrice(price));
                }
                sb.append(")\n");
                ret[i + 2] = "buypart " + part.getId();
                i++;
            }
            sb.append(i);
            sb.append(".取消购买");
            ret[2] = sb.toString();
            ret[ret.length - 1] = "";
            return ret;
        }
    }


    class BuyPartProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() == 1) {
                Client client = getClient(command.getSessionId());
                if (client != null && "MotoV300".equals(client.model)) {
                    sendMessage("你的机型暂不支持此功能", command.getSerial(), command.getSessionId());
                    return;
                }
                byte part = Byte.parseByte(command.getParam(0));
                HouseData hd = houseModel.getHouseByPlayerId(player.getId());
                if (hd != null){
	                HouseTemplate ht = Houses.getHouseTemplate(hd.getLevel(), hd.getStyle());
	                if (hd.hasVisiblePart(part)) {
	                    sendMessage("你已经拥有了此家具", command.getSerial(), command.getSessionId());
	                } else {
	                    HousePart hp = ht.getPart(part);
	                    int price = hp.getPrice();
	                    if (player.isSubscribe()) {
	                        price = Utils.getDiscountPrice(price,Discount.MDISCOUNT);
	//                        price = price * Discount.MDISCOUNT / 100;
	                    } else {
	                        price = Utils.getDiscountPrice(price,Discount.DISCOUNT);
	//                        price = price * Discount.DISCOUNT / 100;
	                    }
	                    StoreService.Request request = storeService.request(player, hp, price * 100,
	                            command.getSerial(),ConnectSession2.this);
	                    if (request != null) {
	                        sendRequestToAuth(request, player.getAccountId(), player.getkey(), command.getSerial(), command.getSessionId(), true);
	                    } else {
	                        throw new ITimesException("不能购买此物品", command.getSerial(), command.getSessionId(), (byte) 20);
	                    }
	                    int iMoney = (int)(player.getLongIMoney() / 100);
                        if(iMoney < price){
                        	sendMessage("您的i币不足无法购买", command.getSerial(),command.getSessionId());
                        }
	                
	                }
                }
            }
        }
    }


    class OutHouseProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            GameMap map = player.getMap();
            if (map != null && map.getInstance() != null) {
                if (player.getJumpMapId() != 0) {
                    GameMap toMap = worldService.getNoInstanceMap(player.getJumpMapId());
                    if (toMap != null) {
                        sendGotoMap(player.getId(), player.getJumpMapId(),
                                    (short) (player.getJumpX() / toMap.getTileWidth()),
                                    (short) (player.getJumpY() / toMap.getTileHeight()));
//                        player.setJumpMapId((short) 0);
//                        player.setJumpX((short)0);
//                        player.setJumpY((short)0);
                    }
                } else {
                    sendGotoMap(player.getId(), (short) 353, (short) 4, (short) 41);
                }
            }
        }
    }


    class LeaveMessageProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() > 0) {
                String msg = command.getParamString(0);
                GameMap map = player.getMap();
                if (map != null && map.getInstance() != null) {
                    HouseInstance hi = (HouseInstance) map.getInstance();
                    HouseData hd = houseModel.getHouseByPlayerId(hi.getOwnerId());
                    if (hd != null) {
                        LeaveMessage m = new LeaveMessage();
                        m.setContent(msg);
                        m.setCreateTime(new Date());
                        m.setOwnerId(hi.getOwnerId());
                        m.setSourceId(player.getId());
                        m.setSourceName(player.getPlayerName());
                        m.setTitle("");
                        try {
                            leaveMessageService.addLeaveMessage(m);
                            hd.incLeaveMessageTimes();
                            sendMessage("添加留言成功", command.getSerial(), command.getSessionId());
                        } catch (DataAccessException ex) {
                        }
                    }
                }
            }

        }
    }


    class RequestChangeRuleProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
//            public static final int RULE_PRIVATE = 0;
//            public static final int RULE_FRIENDS = 1;
//            public static final int RULE_TEAM = 2;
//            public static final int RULE_GUILD = 3;
//            public static final int RULE_FREE = 4;
//            \uF06C	随意参观：玩家允许所有其他玩家进入自己的房屋。
//            \uF06C	好友参观：玩家允许自己的好友列表中的玩家好友进入游戏。
//            \uF06C	队友参观：玩家允许同一队内的成员自由进入自己的房屋。
//            \uF06C	公会参观：玩家允许同一公会的成员自由进入自己的房屋。
//            \uF06C	拒绝参观：玩家可以拒绝除夫妻成员以外的其他玩家。
            byte[] bytes = stageService.getTaskBytes((short) 31010, new String[] {
                    "6", "1", "你需要设置参观权限吗?\n1.拒绝参观\n2.好友参观\n3.队友参观\n4.公会参观\n5.随意参观\n6.取消",
                    "changerule 0", "changerule 1", "changerule 2", "changerule 3", "changerule 4", "ok"});
            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                              GET_FILE_OK,
                                              command.getSerial(),
                                              command.getSessionId());
            seg.writeShort((short) 31010);
            seg.writeShort((short) 2);
            seg.write(bytes);
            write(seg);

        }
    }


    class ChangeRuleProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() == 1) {
                int rule = Integer.parseInt(command.getParam(0));
                if (rule >= 0 && rule <= 4) {
                    GameMap map = player.getMap();
                    if (map != null && map.getInstance() != null) {
                        HouseInstance hi = (HouseInstance) map.getInstance();
                        if (hi.getOwnerId() == player.getId()) {
                            HouseData hd = houseModel.getHouseByPlayerId(player.getId());
                            if (hd != null){
	                            hd.setRule(rule);
	                            try {
	                                houseModel.saveHouse(hd);
	                                sendMessage("你已经将参观权限设置为" + House.RULE_STRING[rule] + "。", command.getSerial(),
	                                            command.getSessionId());
	                            } catch (Exception ex) {
	                                log.error(ex, ex);
	                            }
                            }
                        }
                    }
                }
            }
        }
    }


    class HouseGetItemProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            GameMap map = player.getMap();
            if (map != null && map.getInstance() != null) {
                HouseInstance hi = (HouseInstance) map.getInstance();
                if (hi.getOwnerId() == player.getId()) {
                    HouseData hd = houseModel.getHouseByPlayerId(player.getId());
                    if (!hd.isUsedWaiter()) {
                        sendMessage("没有雇佣管家，不能进行此操作", command.getSerial(), command.getSessionId());
                        return;
                    }
                    Date now = new Date();
                    Date last = hd.getLastTime();
                    synchronized (hi) {
                        if (last == null || !Utils.isSameDay(last, now)) {
                            HouseTemplate ht = Houses.getHouseTemplate(hd.getLevel(), hd.getStyle());
                            DropGroup group = DropGroups.getDropGroup(
                                    ht.getDropGroup(),player.getLevel());
                            if(group != null){
	                            Random rnd = new Random();
	                            int rate = rnd.nextInt(group.getRate());
	                            DropItem dropItem = group.calcDropItem(
	                                    rate);
	                            int count = Utils.getCount(rnd,
	                                    dropItem.getMin(),
	                                    dropItem.getMax());
	                            Changed changed = new Changed();
	                            int c = player.addItem(dropItem.getItem(), count,
	                                    changed, player.getClientDataVersion());
	                            if (c == 0) {
	                                hd.setLastTime(now);
	                                byte[] att = ItemUtils.item2dbAttachment(dropItem.getItem().newInstance(),
	                                        count);
	                                mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	                                        dropItem.getItem().getName() + "*" + count, "", att, 0, true);
	                                sendMessage("由于背包满，物品已经邮寄到邮箱中，请注意查收。", command.getSerial(), command.getSessionId());
	//                                connectService.sendError(player.getId(),
	//                                        "你购买的" + request.item.item.getName() + "由于背包满，已经邮寄到邮箱中，请注意查收。");
	
	//                                sendMessage("你的包已经满了", command.getSerial(), command.getSessionId());
	                              //mengjie add
	                                int item_id = 0;
	                                item_id = dropItem.getItem().getItemId();
	                                String item_msg = Items.getMessage(item_id,5,player.getPlayerName(),dropItem.getItem().getName(),"管家那里");
	                                if (item_msg != null){
	                                	chatService.sendWorldMessage(-1, "系统", item_msg);
	                                }
	                                //mengjie add end
	                            } else {
	                                hd.setLastTime(now);
	                                sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 22);
	                                //mengjie add
	                                int item_id = 0;
	                                item_id = dropItem.getItem().getItemId();
	                                String item_msg = Items.getMessage(item_id,5,player.getPlayerName(),dropItem.getItem().getName(),"管家那里");
	                                if (item_msg != null){
	                                	chatService.sendWorldMessage(-1, "系统", item_msg);
	                                }
	                                //mengjie add end
	                            }
                            }
                        } else {
                            sendMessage("每天只有一次领取物品的机会", command.getSerial(), command.getSessionId());
                        }
                    }
                }
            }
        }
    }


    class IslandItemProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            int islandId = Integer.parseInt(command.getParam(0));
            TongIsland ti = tongService.getTongIsland(islandId);
            if (ti == null) {
                sendMessage("现在不能领取物品", command.getSerial(), command.getSessionId());
                return;
            }
            if (ti.getTongId() != player.getTongId()) {
                sendMessage("你不能在此领取物品", command.getSerial(), command.getSessionId());
                return;
            }
            //mengjie add
            if (player.getLevel()<20) {
                sendMessage("您还没到20级，加油练级到20级来领取丰富礼物吧！", command.getSerial(), command.getSessionId());
                return;
            }
            //mengjie add end
            Date now = new Date();
            Date last = player.getIslandItemTime();
            if (last == null || !Utils.isSameDay(last, now)) {
                TongIslandDef def = tongService.getTongIslandDef(islandId);
                DropGroup group = DropGroups.getDropGroup(
                        def.getDropGroup(), player.getLevel());
                Random rnd = new Random();
                int rate = rnd.nextInt(group.getRate());
                DropItem dropItem = group.calcDropItem(
                        rate);
                int count = Utils.getCount(rnd,
                                           dropItem.getMin(),
                                           dropItem.getMax());
                Changed changed = new Changed();
                int c = player.addItem(dropItem.getItem(), count,
                                       changed, player.getClientDataVersion());
                if (c == 0) {
                    player.setIslandItemTime(now);
                    byte[] att = ItemUtils.item2dbAttachment(dropItem.getItem().newInstance(),
                            count);
                    mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                            dropItem.getItem().getName() + "*" + count, "", att, 0, true);
                                sendMessage("由于背包满，物品已经邮寄到邮箱中，请注意查收。", command.getSerial(), command.getSessionId());
                } else {
                    player.setIslandItemTime(now);
                    sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 22);
                }
                //mengjie add
                int item_id = 0;
                item_id = dropItem.getItem().getItemId();
                String item_msg = Items.getMessage(item_id,6,player.getPlayerName(),dropItem.getItem().getName(),"公会小岛上");
                if (item_msg != null){
                	chatService.sendWorldMessage(-1, "系统", item_msg);
                }

                //mengjie add end
            } else {
                sendMessage("每天只有一次领取物品的机会", command.getSerial(), command.getSessionId());
            }

        }
    }

    class BuyFaceProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            int id = Integer.parseInt(command.getParam(0));
            int serial = command.getSerial();
            try{
            	serial = Integer.parseInt(command.getParam(1));
            }catch(Exception e){
            	serial = command.getSerial();
            }
            boolean cancel = false;
            try{
            	if ("cancel".equalsIgnoreCase(command.getParam(2)))
            		cancel = true;	
            }catch(Exception e){
            }
            if (cancel){
            	UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL, serial);
				seg.writeShort(ClientConstants.EXTEND_ROLEFACE);
				seg.write((byte)4);
				seg.write((byte)1);
				connectService.writeTo(seg,player.getId());
            } else {
	            RoleFaceData roleFace = RoleFaces.getRoleFace(id);
	            if (roleFace == null) {
	                sendMessage("没有此形象", command.getSerial(), command.getSessionId());
	            } else {
	            	int error = player.isCanBuyFace(id);
	            	if(error == 1){
	            		sendMessage("您已经购买过"+roleFace.getName()+"的形象了,不能再购买了。", command.getSerial(), command.getSessionId());
	            	}else{
	            		int price = roleFace.getPrice();
	                    if (player.isSubscribe()) {
	                        price = Utils.getDiscountPrice(price, Discount.MDISCOUNT);
	//                        price = price * Discount.MDISCOUNT / 100;
	                    } else {
	                        price = Utils.getDiscountPrice(price, Discount.DISCOUNT);
	//                        price = price * Discount.DISCOUNT / 100;
	                    }
	                    //相同形象不再购买
	                    if (player.getFace() == roleFace.getFace()){
	                    	sendMessage("您已经是"+roleFace.getName()+"的形象了,无需购买。", command.getSerial(), command.getSessionId());
	                    }else{
	                    	if(player.getLongIMoney() < price * 100){
	                    		sendMessage("您的" + Server.iMoneyString + "不足。", command.getSerial(), command.getSessionId());
	                    		return;
	                    	}
	                    	log.info("ID["+player.getId()+"]BuyFace["+id+"]Price["+price+"]");
	                        StoreService.Request request = storeService.request(player, roleFace, price * 100,
	                        		serial,ConnectSession2.this);
	                        if (request != null) {
	                            sendRequestToAuth(request, player.getAccountId(), player.getkey(), command.getSerial(), command.getSessionId(), true);
	                        } else {
	                            sendMessage("不能购买此物品", command.getSerial(), command.getSessionId());
	                        }
	                    }
	            	}
	            }
            }
        }
    }
    
    class BuyHouseProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() == 3) {
                int level = Integer.parseInt(command.getParam(0));
                int style = Integer.parseInt(command.getParam(1));
                short areaId = Short.parseShort(command.getParam(2));
                int addGridSize = 0;	//扩展的格子数
                if (player.getLevel() < 12) {
                    sendMessage("12级以后才能购买房产", command.getSerial(), command.getSessionId());
                } else {
                    HouseData hd = houseModel.getHouseByPlayerId(player.getId());
                    if (hd != null) {
                    	addGridSize = hd.getAddGridSize();			// 家园扩展了的格子数
                        if (hd.getLevel() == level) {
                            sendMessage("已经拥有此级别房产，不用再次购买！", command.getSerial(), command.getSessionId());
                            return;
                        }
//                        if (areaId != hd.getAreaId()) {
//                            sendMessage("你在异地拥有房屋，只能先搬入此地才能购买新的房屋。", command.getSerial(), command.getSessionId());
//                            return;
//                        }
                        if (hd.getLevel() > level) {
                            byte[] bytes = stageService.getTaskBytes((short) 31002,
                                    new String[] {"你已拥有高级别房屋，请考虑清楚再进行购买低级别房屋。\n1.是\n2.否",
                                    "ensurebuyhouse " + level + " " + style + " " + areaId});
                            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                    GET_FILE_OK, command.getSerial(),
                                    command.getSessionId());
                            seg.writeShort((short) 31002);
                            seg.writeShort((short) 2);
                            seg.write(bytes);
                            write(seg);
                            return;
                        }
                    }
                    HouseTemplate ht = Houses.getHouseTemplate(level, style);
                    if (ht != null) {
                        int price = ht.getPrice();
                        if (player.isSubscribe()) {
                            price = Utils.getDiscountPrice(price,Discount.MDISCOUNT);
//                            price = price * Discount.MDISCOUNT / 100;
                        } else {
                            price = Utils.getDiscountPrice(price,Discount.DISCOUNT);
//                            price = price * Discount.DISCOUNT / 100;
                        }
                        ht.setAddGridSize(addGridSize);
                        StoreService.Request request = storeService.request(player, ht, price * 100, areaId,
                                command.getSerial(),ConnectSession2.this);
                      //0点数跳过购买 && 卓望区
                        if ((price == 0) &&(Server.iMoneyType == Server.IMONEY_TYPE_CMCC)){
                        	BuyResult resulttemp = new BuyResult();
	                		resulttemp.success = true;
	                		resulttemp.iMoney = 0;
	                		resulttemp.bBalance = 0;
	                		resulttemp.cost = 0;
	                		resulttemp.realCost = 0;
	                		resulttemp.cause = "";
	                		resulttemp.sessionId = command.getSessionId();
	                		resulttemp.serial = command.getSerial();
	                		ConnectSession2.this.buyResult(resulttemp,request);
                        	return;
                        }
                        if(request!=null){
                            sendRequestToAuth(request, player.getAccountId(), player.getkey(), command.getSerial(),
                                              command.getSessionId(), true);
                        }else{
                            sendMessage("不能购买此物品",command.getSerial(),command.getSessionId());
                        }
                        
                        int iMoney = (int)(player.getLongIMoney() / 100);
                        if(iMoney < price){
                        	sendMessage("您的i币不足无法购买", command.getSerial(),command.getSessionId());
                        }
//                        UWAPSegment seg = new UWAPSegment(ServerConstants.BUY);
//                        seg.writeInt(player.getAccountId());
//                        if(request.consumeCode==null){
//                            seg.writeInt(price * 100);
//                        }else{
//                            seg.writeString(request.consumeCode);
//                        }
//                        seg.writeInt(request.id);
//                        authSession.write(seg);
                    }
                }
            }
        }
    }


    class EnsureBuyHouseProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() == 3) {
                int level = Integer.parseInt(command.getParam(0));
                int style = Integer.parseInt(command.getParam(1));
                short areaId = Short.parseShort(command.getParam(2));
                int addGridSize = 0;	//扩展的格子数
                if (player.getLevel() < 12) {
                    sendMessage("12级以后才能购买房产", command.getSerial(), command.getSessionId());
                } else {
                    HouseData hd = houseModel.getHouseByPlayerId(player.getId());
                    if (hd != null) {
                    	addGridSize = hd.getAddGridSize();		//老家园扩展了的格子数
                        if (hd.getLevel() == level) {
                            sendMessage("已经拥有此级别房产，不用再次购买！", command.getSerial(), command.getSessionId());
                            return;
                        }
//                        if (areaId != hd.getAreaId()) {
//                            sendMessage("你已经在别的地方拥有房产，如果需要在此处购买，需要先将原有家园搬入此地！", command.getSerial(),
//                                        command.getSessionId());
//                            return;
//                        }
                        if (hd.getLevel() > level) {
                            HouseTemplate ht = Houses.getHouseTemplate(level, style);
                            if (hd.getCurrentGridSize() >= (ht.getGridSize() + hd.getAddGridSize()) ) {
                                sendMessage("你现在仓库中的物品数量多于将要购买房屋能够存储的物品数量，请移除部分物品再次尝试！", command.getSerial(),
                                            command.getSessionId());
                                return;
                            }
                        }
                    }
                    HouseTemplate ht = Houses.getHouseTemplate(level, style);
                    if (ht != null) {
                        int price = ht.getPrice();
                        if (player.isSubscribe()) {
                            price = Utils.getDiscountPrice(price,Discount.MDISCOUNT);
//                            price = price * Discount.MDISCOUNT / 100;
                        } else {
                            price = Utils.getDiscountPrice(price,Discount.DISCOUNT);
//                            price = price * Discount.DISCOUNT / 100;
                        }
                        ht.setAddGridSize(addGridSize);			// 将老家园的扩展的格子数添加到新家园中去
                        StoreService.Request request = storeService.request(player, ht, price * 100, areaId,
                                command.getSerial(),ConnectSession2.this);
                        if(request!=null){
                            sendRequestToAuth(request, player.getAccountId(), player.getkey(), command.getSerial(),
                                              command.getSessionId(), true);
                        }else{
                            throw new ITimesException("不能购买次商品",command.getSerial(),command.getSessionId(),(byte)20);
                        }
                        int iMoney = (int)(player.getLongIMoney() / 100);
                        if(iMoney < price){
                        	sendMessage("您的i币不足无法购买", command.getSerial(),command.getSessionId());
                        }
                    }
                }
            }
        }
    }


    class ChangeStyleProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() == 1) {
                int style = Integer.parseInt(command.getParam(0));
                HouseData hd = houseModel.getHouseByPlayerId(player.getId());
                if (hd != null) {
                    if (hd.getStyle() != style) {
                        HouseTemplate ht = Houses.getHouseTemplate(hd.getLevel(), style);
                        if (ht != null) {
                            int price = ht.getStylePrice();
                            if (player.isSubscribe()) {
                                price = Utils.getDiscountPrice(price,Discount.MDISCOUNT);
//                                price = price * Discount.MDISCOUNT / 100;
                            } else {
                                price = Utils.getDiscountPrice(price,Discount.DISCOUNT);
//                                price = price * Discount.DISCOUNT / 100;
                            }

                            StoreService.Request request = storeService.request(player, ht, price * 100,
                                    command.getSerial(),ConnectSession2.this);
                            if (request != null) {
                                sendRequestToAuth(request, player.getAccountId(), player.getkey(), command.getSerial(),
                                                  command.getSessionId(), true);
                            } else {
                                throw new ITimesException("不能购买此物品", command.getSerial(), command.getSessionId(),
                                        (byte) 20);
                            }
                        } else {
                            sendMessage("不存在此种样式", command.getSerial(), command.getSessionId());
                        }
                    } else {
                        sendMessage("不能更换相同的样式", command.getSerial(), command.getSessionId());
                    }
                } else {
                    sendMessage("你还没有房产", command.getSerial(), command.getSessionId());
                }
            }
        }
    }

    class RequestChangeHouseTitleProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            HouseData hd = houseModel.getHouseByPlayerId(player.getId());
            if (hd != null) {
                byte[] bytes = stageService.getTaskBytes((short) 31001,
                        new String[] {"是否修改欢迎词?\n1.是\n2.否",
                        "请输入欢迎词",
                        "changehousetitle "});
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  GET_FILE_OK,
                                                  command.getSerial(),
                                                  command.getSessionId());
                seg.writeShort((short) 31001);
                seg.writeShort((short) 2);
                seg.write(bytes);
                write(seg);

            } else {
                sendMessage("你还没有房产，可以通过购买拥有自己的房屋。", command.getSerial(), command.getSessionId());
            }
        }
    }


    class ChangeHouseTitleProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() >= 1) {
                HouseData hd = houseModel.getHouseByPlayerId(player.getId());
                if (hd != null) {
                    String s = command.getParamString(0);
                    if (s.length() > 16) {
                        sendMessage("欢迎词不能大于16个字符。", command.getSerial(), command.getSessionId());
                        return;
                    }
                    hd.setTitle(s);
                    houseModel.saveHouse(hd);
                    sendMessage("欢迎词已经修改完成。", command.getSerial(), command.getSessionId());
                }
            }
        }
    }
    
    public static final String WAITER_MESSAGE = "如果你雇佣了我，你可以获得可以存放绑定物品的仓库、可以发其他人可以看到的BLOG、还可以每天免费领取很超值的物品哦。另外，如果你在浴场消费在线得到经验时，你当前装备的宠物也能获得经验啊！";
    public static final String WAITER_MESSAGE_part2 = "想想看，你将得到丰富多彩的生活，我每领一次薪水，会忠诚地为您服务30天，如果你不主动来退定，雇佣到期后会自动继续支付雇佣薪水，我将为您服务到底。";
    class WaiterHelpProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            sendMessage(WAITER_MESSAGE, command.getSerial(), command.getSessionId());
            sendMessage(WAITER_MESSAGE_part2, command.getSerial(), command.getSessionId());
        }
    }

    // zjl add
    class timelimit implements CommandProcessor {
    	public void process(WorldPlayer player, Command command) throws Exception {
    		HouseData hd = houseModel.getHouseByPlayerId(player.getId());
    		if(hd!=null){
    			if(hd.isUsedWaiter()){	//有管家的情况
    				Date time = hd.getCanUseWaiterTime();
    				SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒 ");//时:分:秒:毫秒 
    				//String strlimit  = time.toString();
    				//sendMessage("您管家的有效期截止到" + strlimit, command.getSerial(), command.getSessionId());
    				sendMessage(player.getId(), "您管家的有效期截止到"+ sdf.format(time));
    			}else{
    				sendMessage("您还没有雇佣管家！",command.getSerial(), command.getSessionId());
    			}
    		}
    	}
    }

    class LogProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
            log.info("ID["+player.getId()+"]Log["+command.getParamString(0)+"]");
        }
    }


    class EnemyListProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            HouseData hd = houseModel.getHouseByPlayerId(player.getId());
            if (hd != null && hd.isUsedWaiter()) {
                Enemy[] enemys = player.getEnemys();
                Arrays.sort(enemys);
                UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST,command.getSerial(),command.getSessionId());
                seg.writeShort((short) 9);
                seg.writeString("仇人榜");
                seg.write((byte) 0);
                seg.writeShort((short) enemys.length);
                for (int j = 0; j < enemys.length; j++) {
                    WorldPlayer p = playerService.getWorldPlayer(enemys[j].id);
                    String onlineString = "";
                    if (p != null && p.online()) {
                        onlineString = "[在线]";
                    }else{
                        onlineString = "[离线]";
                    }
                    seg.writeInt(j);
                    seg.writeString(enemys[j].name + "[仇恨值:" + enemys[j].times + "]" + onlineString);
                    seg.writeInt(Utils.CLR_WHITE);
                }
                write(seg);
            }else{
                sendMessage("你没有雇佣管家",command.getSerial(),command.getSessionId());
            }
        }
    }

    class SportPlayProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
            if(command.getParamCount()==1){
                try {
                    Changed changed = new Changed();
                    String s = command.getParam(0);
                    sportsService.play(player, command.getParam(0),changed);
                    sendGetItem(changed,command.getSerial(),command.getSessionId(),(byte)22);
                    if(s.startsWith("p-")){
                        sendMessage("运动起来吧，别让自己落后了哦！", command.getSerial(), command.getSessionId());
                    }else if(s.startsWith("g-")){
                        sendMessage("为公会的荣誉而冲刺吧！", command.getSerial(), command.getSessionId());
                    }else if(s.startsWith("c-")){
                        sendMessage("快点儿去登高祈福吧！登得越高，福气越大！", command.getSerial(), command.getSessionId());
                    }
                } catch (SportException ex) {
                    sendMessage(ex.getMessage(),command.getSerial(),command.getSessionId());
                }
            }
        }
    }


    class SportOverProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            try {
                SportRecord sr = sportsService.over(player, command.getParam(0));
                sendMessage("哦！已经到终点啦，你的成绩是" + Utils.getTimeString(sr.overTime - sr.startTime), command.getSerial(),
                            command.getSessionId());
            } catch (SportException ex) {
                sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
            }
        }
    }

    class HouseSpRoomProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
            GameMap map = player.getMap();
            if (map == null || map.getInstance() == null)
                throw new ITimesException("状态错误", command.getSerial(), command.getSessionId(),
                                          command.getAppType());
            HouseInstance hi = (HouseInstance) map.getInstance();
            if (hi.getOwnerId() == player.getId()) {
                HouseData hd = houseModel.getHouseByPlayerId(player.getId());
                if (hd!=null){
	                if (!hd.isUsedWaiter()) {
	                    sendMessage("你没有雇佣管家，不能操作", command.getSerial(), command.getSessionId());
	                    return;
	                } else {
	                    HouseTemplate ht = Houses.getHouseTemplate(hd.getLevel(), hd.getStyle());
	                    if (ht.getSpMapId() != 0) {
	                        sendGotoMap(player.getId(),(short)ht.getSpMapId(),(short)ht.getSpX(),(short)ht.getSpY());
	                    } else {
	                        return;
	                    }
	                }
                }else{
                	sendMessage("你没有雇佣管家，不能操作", command.getSerial(), command.getSessionId());
                	return;
                }
            }else{
                sendMessage("你不是房间主人，不能操作", command.getSerial(), command.getSessionId());
            }
        }
    }

    class RequestSelectEquProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
            int serial = Integer.parseInt(command.getParam(0));
            UWAPSegment seg = new UWAPSegment(ClientConstants.REPAIRE_LIST, serial, command.getSessionId());
            seg.write((byte) 2);
            write(seg);
        }
    }

    class RequestUnEnhanceProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
            int serial = Integer.parseInt(command.getParam(0));
            UWAPSegment seg = new UWAPSegment(ClientConstants.REPAIRE_LIST, serial, command.getSessionId());
            seg.write((byte) 3);
            write(seg);
        }
    }

    class SelectEquProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
            int equItemId = Integer.parseInt(command.getParam(0));
            int id = Integer.parseInt(command.getParam(1));
            IEquipment equ = player.getEquipment(equItemId,id);
            if(equ!=null){
                if (equ.getTimes() >= 9) {
                    sendMessage("此装备不能再进行精炼。", command.getSerial(), command.getSessionId());
                    return;
                }
                byte[] bytes = stageService.getTaskBytes((short) 31024,
                        new String[] {"preenhance " + equItemId + " " + id});
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  GET_FILE_OK);
                seg.writeShort((short) 31024);
                seg.writeShort((short) 2);
                seg.write(bytes);
                connectService.writeTo(seg, player.getId());
            }else{
                sendMessage("选择精炼的装备不存在。", command.getSerial(), command.getSessionId());
            }
        }
    }


    class SelectUnEnhanceProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            int equItemId = Integer.parseInt(command.getParam(0));
            int id = Integer.parseInt(command.getParam(1));
            IEquipment equ = player.getEquipment(equItemId, id);
            if (equ != null) {
                if (equ.getTimes() <= 0) {
                    sendMessage("此装备不能再进行退化。", command.getSerial(), command.getSessionId());
                    return;
                }
                byte[] bytes = stageService.getTaskBytes((short) 31002,
                        new String[] {"是否退化此装备?\n1.是\n2.否",
                        "unenhance " + equItemId + " " + id});
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                        GET_FILE_OK, command.getSerial(),
                        command.getSessionId());
                seg.writeShort((short) 31002);
                seg.writeShort((short) 2);
                seg.write(bytes);
                write(seg);
            } else {
                sendMessage("选择精炼的装备不存在。", command.getSerial(), command.getSessionId());
            }
        }
    }

    class PreEnhanceProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            int equItemId = Integer.parseInt(command.getParam(0));
            int id = Integer.parseInt(command.getParam(1));
            int pro = Integer.parseInt(command.getParam(2));
            IEquipment equ = player.getEquipment(equItemId, id);
            if (equ != null) {
                if (equ.getTimes() >= 9) {
                    sendMessage("此装备不能再进行精炼。", command.getSerial(), command.getSessionId());
                    return;
                }
                //取物品的等级
                IItemTemplate template = Items.getTemplate(equItemId);
                Enhance enhance = Enhance.getEnhance(pro,template.getLevel());
                if (enhance != null) {
                    IItemTemplate item = Items.getTemplate(enhance.getItemId());
                    int[] count = Utils.getEnhanceItemCount(equ, enhance);
                  //mengjie add 精炼精华与精炼石数量相同
                    IEquipment itemtmp1 = player.getEquipment(equItemId, id);
                    List<Enhance> enhancestmp  = itemtmp1.getEnhances();
                    
                    String msg = "精炼的属性" + enhance.getName() + "将提高" + enhance.getPoint(enhancestmp.size()+1) + "点,需要精炼石" + count[1] + "个," +
                                 item.getName() + "(或者更高级的精华)" +
                                 count[1] + "个,继续精炼么?\n1.开始精炼\n2.放弃精炼";
                    byte[] bytes = stageService.getTaskBytes((short) 31025,
                            new String[] {msg, "enhance " + equItemId + " " + id + " " + pro});
                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                            GET_FILE_OK);
                    seg.writeShort((short) 31025);
                    seg.writeShort((short) 2);
                    seg.write(bytes);
                    connectService.writeTo(seg, player.getId());
                }
            } else {
                sendMessage("选择精炼的装备不存在。", command.getSerial(), command.getSessionId());
            }
        }
    }

    class UnEnhanceProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
            int equItemId = Integer.parseInt(command.getParam(0));
            int id = Integer.parseInt(command.getParam(1));
            IEquipment equ = player.getEquipment(equItemId, id);
            if(equ!=null){
                if(equ.getTimes()<=0){
                    sendMessage("此装备不能再进行退化。", command.getSerial(), command.getSessionId());
                    return;
                }
                Changed changed = new Changed();
                if(player.completeRemoveItem(211015,1,changed)!=null){
                    equ.unEnhance();
                    changed.addEquipment(equ,-1);
                    changed.addEquipment(equ,1);
                    sendMessage("退化成功。", command.getSerial(), command.getSessionId());
                    sendGetItem(changed,command.getSerial(),command.getSessionId(),(byte)20);
                }else{
                    sendMessage("精炼退化石不存在。", command.getSerial(), command.getSessionId());
                }
            }else{
                sendMessage("选择精炼的装备不存在。", command.getSerial(), command.getSessionId());
            }

        }
    }

    class EnhanceProcessor implements CommandProcessor{

        private Random rnd = new Random();

        public void process(WorldPlayer player, Command command) throws Exception {
            int equItemId = Integer.parseInt(command.getParam(0));
            int id = Integer.parseInt(command.getParam(1));
            int pro = Integer.parseInt(command.getParam(2));
            synchronized (player) {
                IEquipment equ = player.getEquipment(equItemId, id);
                if (equ != null) {
                    if (equ.getTimes() >= 9) {
                        sendMessage("此装备不能再进行精炼。", command.getSerial(), command.getSessionId());
                        return;
                    }
                    if(!equ.canEnhance()){
                        sendMessage("此装备不能进行精炼。", command.getSerial(), command.getSessionId());
                        return;
                    }
                    //取物品的等级
                    IItemTemplate template = Items.getTemplate(equItemId);
                    //所需的精华蛋物品等级
                    Enhance enhance = Enhance.getEnhance(pro,template.getLevel());
                    if (enhance != null) {
                        log.info("ID["+player.getId()+"]equId["+equItemId+"]instanceId["+id+"]pro["+pro+"]times["+equ.getTimes()+"]Enhance Try");
                        //jwp add此时进行免费物品10级神秘装的精炼id检查
                       //if(ItemUtils.isFreeEhanceItem(equItemId, equ.getTimes())){//判断是否免费打造的物品
                        if(false){
                        	//sendMessage(player.getId(),"免费精炼装备了哦");
                        	Changed changed = new Changed();
                        	equ.enhance(enhance);
                            Utils.resetEnhanceStatus(equ, true);
                            changed.addEquipment(equ, -1);
                            changed.addEquipment(equ, 1);

                            sendMessage("恭喜你精炼成功！", command.getSerial(), command.getSessionId());
                            sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 30);

                            log.info("ID[" + player.getId() + "]equId[" + equItemId + "]instanceId[" + id + "]pro[" +
                                 pro + "]times[" + equ.getTimes() + "]EnhanceOk");
                        }else{
	                        int[] itemCount = Utils.getEnhanceItemCount(equ, enhance);
	                        //mengjie add 精炼精华与精炼石数量相同
	                        //jwp add 查找高级精华蛋精炼
	                        boolean needItemFindFlag = false;
	                        int itemLevel = Enhance.level_quality[template.getLevel()];
	                       
	                        int needItemCount = 0;
	                        while(!needItemFindFlag && itemLevel <= Enhance.getEnhanceMaxPointProLevel()){
	                        	/*if(needItemCount >= itemCount[1]){
	                        		needItemFindFlag = true;
	                        	}else{*/
	                        		Enhance tempEnhance = Enhance.getUpEnhance(pro,itemLevel);
	                        		needItemCount = needItemCount + player.getItemCount(tempEnhance.getItemId());
	                        		itemLevel++;
	                        		
	                        		if(needItemCount >= itemCount[1]){
		                        		needItemFindFlag = true;
		                        	}
	                        	//}
	                        }
	                       //jwp add end
	                  
	                        if (/*player.hasItem(enhance.getItemId(), itemCount[1]) */ needItemFindFlag && player.hasItem(211002, itemCount[1])) {
	                            int probability = Utils.getEnhanceItemProbability(equ, player.getLevel(), enhance);
	                            //添加地区精炼成功率 jwp add start
	                            int upProbability =Enhance.getUpMapPercent(player.getMapId());
	                            probability += upProbability;
	                            //jwp add end
	                            Buf buf = player.getBuf(Buf.ENHANCE);
	                            if(buf!=null){
	                                probability += buf.getValue();
	                            }
	                            //星辉套装 3、4星加成功率
	                            int[] diamondShineLevel = Suits.getActualPointSuitEffect2(player.getUsedEquipments());
	                            if(diamondShineLevel[0] >= 4){
	                            	probability = probability + probability * 4 / 100;
	                            } else if(diamondShineLevel[0] == 3){
	                            	probability = probability + probability * 2 / 100;
	                            }
	                            player.removeBuf(Buf.ENHANCE,null);
	                            //添加阵营精炼成功率
	                            buf = player.getBuf(Buf.CAMP_REFINE);
	                            if(buf != null){
	                            	probability += buf.getValue();
	                            }
	                            
	                            //vip增加成功率
		                        if(player.getVipLevel() > 0){
		                        	probability += 50; 
		                        }
	                            
	                            log.info("ID[" + player.getId() + "]equId[" + equItemId + "]instanceId[" + id + "]pro[" +
	                                     pro + "]times[" + equ.getTimes() + "]probability[" + probability + "]Enhance");
	                            if (probability >= 100 || Utils.hit(rnd, probability, 100)) {
	                                Changed changed = new Changed();
	                                player.completeRemoveItem(211002, itemCount[1], changed);
	                                //player.completeRemoveItem(enhance.getItemId(), itemCount[1], changed);
	                                //jwp add减去精华，因为查询过，所以肯定物品走够，这里只需要从低级开始到高级逐步减去就行
	                                int temp = itemCount[1];
	                                //属性计数器
	                                int tempLevel = Enhance.level_quality[template.getLevel()];
	                                while(temp >0 && tempLevel <= Enhance.getEnhanceMaxPointProLevel()){
	                                	Enhance enhance2 = Enhance.getUpEnhance(pro,tempLevel);
	                                	int count = player.getItemCount(enhance2.getItemId());
	                                	if(count >= temp){//结束了
	                                		player.completeRemoveItem(enhance2.getItemId(), temp, changed);
	                                		break;
	                                	}else {
	                                		player.completeRemoveItem(enhance2.getItemId(), count, changed);
	                                		temp = temp - count;
	                                	}
	                                	tempLevel++;
	                                }
	                                //jwp add end
	                                equ.enhance(enhance);
	                                Utils.resetEnhanceStatus(equ, true);
	                                changed.addEquipment(equ, -1);
	                                changed.addEquipment(equ, 1);
	                                if (equ.getTimes() >= 3) {
	                                    equ.setBinded(true);
	                                }
	                                sendMessage("恭喜你精炼成功！", command.getSerial(), command.getSessionId());
	                                sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 30);
	
	                                log.info("ID[" + player.getId() + "]equId[" + equItemId + "]instanceId[" + id + "]pro[" +
	                                     pro + "]times[" + equ.getTimes() + "]probability[" + probability + "]EnhanceOk");
	                                if (equ.getTimes() >= 5){
	                                	chatService.sendWorldMessage( -1, "系统",
		                            			"恭喜" + player.getPlayerName() + "顺利精炼出"+Utils.getClientItemColor(equ.getQuality())
		                            					+ equ.getName() +  "</c>");
	                                }
	                            } else {
	                                Changed changed = new Changed();
	                                player.completeRemoveItem(211002, itemCount[1], changed);
	                                //player.completeRemoveItem(enhance.getItemId(), itemCount[1], changed);
	                                //jwp add减去精华，因为查询过，所以肯定物品走够，这里只需要从低级开始到高级逐步减去就行
	                                int temp = itemCount[1];
	                                //属性计数器
	                                int tempLevel = Enhance.level_quality[template.getLevel()];
	                                while(temp >0 && tempLevel <= Enhance.getEnhanceMaxPointProLevel()){
	                                	Enhance enhance2 = Enhance.getUpEnhance(pro,tempLevel);
	                                	int count = player.getItemCount(enhance2.getItemId());
	                                	if(count >= temp){//结束了
	                                		player.completeRemoveItem(enhance2.getItemId(), temp, changed);
	                                		break;
	                                	}else {
	                                		player.completeRemoveItem(enhance2.getItemId(), count, changed);
	                                		temp = temp - count;
	                                	}
	                                	tempLevel++;
	                                }
	                                //jwp add end
	                                //一周的取消精炼守护石
/*	                                if (player.completeRemoveItem(211001, 1, changed) == null) { //如果不存在装备精炼守护石就扣装备
	                                    if(equ.getTimes()>0){
	                                        changed.addItem(equ,-1);
	                                        equ.unEnhance();
	                                        changed.addItem(equ,1);
	                                    }
	//                                    player.completeRemoveItem(equ.getItemId(), equ.getId(), changed);
	                                    log.info("ID[" + player.getId() + "]equId[" + equItemId + "]instanceId[" + id + "]pro[" +
	                                     pro + "]times[" + equ.getTimes() + "]probability[" + probability + "]EnhanceFail1");
	                                    sendMessage("此次精炼失败，因无装备精炼守护石，装备退化一个星级！", command.getSerial(), command.getSessionId());
	                                } else {
	                                    Utils.resetEnhanceStatus(equ, false);
	                                    log.info("ID[" + player.getId() + "]equId[" + equItemId + "]instanceId[" + id + "]pro[" +
	                                     pro + "]times[" + equ.getTimes() + "]probability[" + probability + "]EnhanceFail2");
	                                    sendMessage("此次精炼失败，因有装备精炼守护石，装备完好无损，守护石消耗1个。", command.getSerial(), command.getSessionId());
	                                }*/
	                                Utils.resetEnhanceStatus(equ, false);
	                                
	                                log.info("ID[" + player.getId() + "]equId[" + equItemId + "]instanceId[" + id + "]pro[" +
		                                     pro + "]times[" + equ.getTimes() + "]probability[" + probability + "]Enhance failed");
	                                
	                                sendMessage("此次精炼失败。请下次再来", command.getSerial(), command.getSessionId());
	                                sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 30);
	                            }
	                        } else {
	                            sendMessage("你没有足够的材料精炼。", command.getSerial(), command.getSessionId());
	                        }
                        }
                    }
                } else {
                    sendMessage("选择精炼的装备不存在。", command.getSerial(), command.getSessionId());
                }
            }
        }
    }


    class GoIslandProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            int islandId = Integer.parseInt(command.getParam(0));
            TongIslandDef def = tongService.getTongIslandDef(islandId);
            if (def != null) {
                TongIsland ti = tongService.getTongIsland(islandId);
                if (ti == null) {
                    sendMessage("岛屿处于拍卖或者不可用的状态。", command.getSerial(), command.getSessionId());
                    return;
                } else {
                    if (ti.getTongId() != player.getTongId()) {
                        TongData td = tongService.getTongData(ti.getTongId());
                        sendMessage("此岛屿已被" + td.getTongName() + "公会占领，目前无法进入！", command.getSerial(),
                                    command.getSessionId());
                        return;
                    } else {
                        TongData td = tongService.getTongData(ti.getTongId());
                        if(td.getLeastCredit()<=player.getContribution()){
                            sendGotoMap(player.getId(), def.getEntrance(), def.getEntrancex(), def.getEntrancey());
                        }else{
                            sendMessage("你的帮会贡献没有达到要求。", command.getSerial(), command.getSessionId());
                            return;
                        }
                    }
                }
            }
        }
    }
    
    class AuctionIslandProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (player.getTongId() == -1 || player.getTongDuty() != Tong.OWNER) {
                sendMessage("只有公会会长才能通过公会荣誉来竞价争夺岛屿。", command.getSerial(),
                            command.getSessionId());
                return;
            } else {
                int islandId = Integer.parseInt(command.getParam(0));
                int price = 0;
                try {
                    price = Integer.parseInt(command.getParam(1));
                } catch (NumberFormatException ex1) {
                    sendMessage("出价错误！", command.getSerial(), command.getSessionId());
                    return;
                }
                log.info("ID["+player.getId()+"]tongId["+player.getTongId()+"]islandId["+islandId+"]price["+price+"]");
                try {
                    tongService.addAuction(player.getTongId(), islandId, price);
                    log.info("ID["+player.getId()+"]tongId["+player.getTongId()+"]islandId["+islandId+"]price["+price+"]Ok");
                    sendMessage("您的出价已成功，请随时通过竞价状态来查看当前竞价情况！", command.getSerial(), command.getSessionId());
                } catch (TongException ex) {
                    log.info("ID["+player.getId()+"]tongId["+player.getTongId()+"]islandId["+islandId+"]price["+price+"]Fail");
                    sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
                }
            }
        }
    }

    class PreTongCreateProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
            byte[] bytes = stageService.getTaskBytes((short) 31001,
                    new String[] {"是否建立公会?\n1.是\n2.否", "请输入公会名称",
                    "tong_create "});
            UWAPSegment seg = new UWAPSegment(ClientConstants.
                    GET_FILE_OK, command.getSerial(),
                    command.getSessionId());
            seg.writeShort((short) 31001);
            seg.writeShort((short) 2);
            seg.write(bytes);
            write(seg);
        }
    }

    class PreLeastCreditProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (player.getTongId() == -1 || player.getTongDuty() != Tong.OWNER) {
                sendMessage("只有公会会长才能设置最贡献值。", command.getSerial(),
                            command.getSessionId());
                return;
            } else {

                byte[] bytes = stageService.getTaskBytes((short) 31001,
                        new String[] {"是否设置最低贡献值?\n1.是\n2.否", "请输入最低贡献值",
                        "leastcredit "});
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                  GET_FILE_OK, command.getSerial(),
                                                  command.getSessionId());
                seg.writeShort((short) 31001);
                seg.writeShort((short) 2);
                seg.write(bytes);
                write(seg);
            }
        }
    }

    class LeastCreditProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
            if (player.getTongId() == -1 || player.getTongDuty() != Tong.OWNER) {
                sendMessage("只有公会会长才能设置最低贡献值。", command.getSerial(),
                            command.getSessionId());
                return;
            }else{
                int value = 0;
                try {
                    value = Integer.parseInt(command.getParam(0));
                } catch (NumberFormatException ex) {
                    sendMessage("设置错误！", command.getSerial(),
                            command.getSessionId());
                    return;
                }
                TongData td = tongService.getTongData(player.getTongId());
                if(value>=0){
                    td.setLeastCredit(value);
                    sendMessage("设置最低贡献值成功。", command.getSerial(),
                            command.getSessionId());
                }
            }
        }
    }


    class AuctionIslandMessageProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            sendMessage("每周二19：30公会小岛全部恢复为无占领状态，当日20：00－23：00为公会会长使用公会荣誉点竞价的时段；竞价结束后，竞价点数最高的将拥有竞价岛屿一周的占领权，其余出价者的荣誉点数将被返回，但需要扣除一定的竞拍费用。",
                        command.getSerial(),
                        command.getSessionId());
        }
    }


    class ConfirmAuctionIslandProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            if (player.getTongId() == -1 || player.getTongDuty() != Tong.OWNER) {
                sendMessage("只有公会会长才能通过公会荣誉来竞价争夺岛屿。", command.getSerial(), command.getSessionId());
                return;
            } else {
                int islandId = Integer.parseInt(command.getParam(0));
                if (tongService.isAuction()) {
                    TongData td = tongService.getTongData(player.getTongId());
                    if (td != null) {
                        byte[] bytes = stageService.getTaskBytes((short) 31001,
                                new String[] {"拥有公会荣誉" + td.getCredit() +
                                "。竞拍费：1%*与上次出价差点/次，最低1点。是否竞拍?\n1.是\n2.否",
                                "请输入竞价公会荣誉点数",
                                "auctionisland " + islandId +
                                " "});
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                GET_FILE_OK, command.getSerial(),
                                command.getSessionId());
                        seg.writeShort((short) 31001);
                        seg.writeShort((short) 2);
                        seg.write(bytes);
                        write(seg);
                    }
                } else {
                    TongIsland ti = tongService.getTongIsland(islandId);
                    if (ti != null) {
                        TongData td = tongService.getTongData(ti.getTongId());
                        sendMessage("此岛屿已被" + td.getTongName() + "公会占领，目前无法进入！", command.getSerial(),
                                    command.getSessionId());
                        return;
                    } else {
                        sendMessage("岛屿处于不可用的状态。", command.getSerial(), command.getSessionId());
                        return;
                    }
                }
            }
        }
    }


    class PreAuctionIslandProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
            int islandId = Integer.parseInt(command.getParam(0));
            if (tongService.isAuction()) {
                TongIslandDef def = tongService.getTongIslandDef(islandId);
                if (def != null) {
                    byte[] bytes = stageService.getTaskBytes((short) 31010,
                            new String[] {"3","1","欢迎竞价争夺\n1.竞价状态\n2.我要出价\n3.退出竞价", "auctionislandstate " + islandId,
                            "confirmauctionisland " + islandId, "message 1"
                    });
                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                            GET_FILE_OK, command.getSerial(), command.getSessionId());
                    seg.writeShort((short) 31002);
                    seg.writeShort((short) 2);
                    seg.write(bytes);
                    write(seg);
                }
            } else {
                TongIsland ti = tongService.getTongIsland(islandId);
                if (ti != null) {
                    TongData td = tongService.getTongData(ti.getTongId());
                    sendMessage("此岛屿已被" + td.getTongName() + "公会占领，目前无法进入！", command.getSerial(),
                                command.getSessionId());
                    return;
                } else {
                    sendMessage("岛屿处于不可用的状态。", command.getSerial(), command.getSessionId());
                    return;
                }
            }
        }
    }

    class AuctionIslandStateProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
            int islandId = Integer.parseInt(command.getParam(0));
            if(tongService.isAuction()){
                TongAuction[] auctions = tongService.getTop9List(islandId);
                UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST, command.getSerial(), command.getSessionId());
                seg.writeShort((short) 7);
                seg.writeString("竞价状态");
                seg.write((byte) 0);
                seg.writeShort((short) auctions.length);
                for (int i = 0; i < auctions.length; i++) {
                    String ds = i==0?"领先":"出局";
                    seg.writeInt(i);
                    seg.writeString((i + 1) + ". " + auctions[i].getTongName() + " " + auctions[i].getPrice()+" "+ds);
                    seg.writeInt(Utils.CLR_WHITE);
                }
                write(seg);
            }
        }
    }


    private static final String[] CMD_MESSAGES = {
    "公会会长通过岛屿竞价获得岛屿占领权，所有会员将享受很多额外好处！",
    "占领岛屿，全公会成员将享受特殊待遇，有机会就来争夺吧！",
    "参加跨服战斗可以提示个人竞技场等级和战队等级哦，快快参与吧！",
    "不同的岛屿会提供全公会成员不同的福利待遇，例如泡澡获取经验的时间不同、可获得的物品不同等。更多的福利信息请上岛了解。",
    "装备精炼转移就是将旧装备的精炼星级原样转移到新的装备上。\n能进行装备精炼转移的两件装备，必须是您背包中同等级、同部位的两件普通装备；\n精炼转移完成后<cff0000>来源装备还原成未精炼过的状态</c>，目标装备的星级和精炼属性，都转变为来源装备原来的状态；装备精炼转移需要消耗一颗“移星水晶”和50，000J币。\n周年纪念装等特殊装备暂时不能进行精炼转移。",
    "装备精炼转移真是个实惠的功能啊！",
    };

    class MessageProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
            int id = Integer.parseInt(command.getParam(0));
            if(id>=0||id<CMD_MESSAGES.length){
                sendMessage(CMD_MESSAGES[id], command.getSerial(), command.getSessionId());
            }
        }
    }
	//mengjie add
    class FindfriendProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	//知己的角色名
        	try{
        		if (player.getLevel() > 40){
        			sendMessage("您已经超过40级了。不能再找知己了。", command.getSerial(),command.getSessionId());
        		}else{
	        		int friendplayerid = friendsService.getfriendplayerid(player.getId());
	            	if (friendplayerid > -1 ){
	            		sendMessage("您已经有知己了。不能再找知己了。", command.getSerial(),command.getSessionId());
	            	}else{
	            		friendplayerid = friendsService.getallplayerid(player.getId());
	            		if (friendplayerid > -1 ){
	            			sendMessage("您曾经有过知己了。不能再找知己了。", command.getSerial(),command.getSessionId());
	            		}else{
	            			String playerName = command.getParam(0);
	                        WorldPlayer target = playerService.getWorldPlayer(playerName);
	                        if (target != null) {
	                        	if (target.getAccountId() == player.getAccountId()){
	                        		sendMessage("抱歉！同一个账号的两个角色无法成为知己！", command.getSerial(),command.getSessionId());
	                        	}else{
		                        	if (target.getLevel()>player.getLevel()){
		                        		int backfriendplayerid = friendsService.getallplayerid(target.getId());
		                        		if(backfriendplayerid == player.getId()){
		                        			sendMessage("抱歉！两个角色无法互为知己！", command.getSerial(),command.getSessionId());
		                        		}else{
		                        			Friends friends = new Friends();
		                        			friends.setPlayerid(player.getId());
		                        			friends.setFriendplayerid(target.getId());
		                        			friends.setPlayername(player.getPlayerName());
		                        			friends.setLevel(player.getLevel());
		                        			friends.setImoney(0);
		                        			friends.setValid((byte)0);
		                        			friendsService.addFriend(friends,target.getLevel(),target.getPlayerName());
		                        			sendMessage("恭喜！" + target.getPlayerName() + "成为了你的知己！", command.getSerial(),command.getSessionId());
		                        		}
		                        	}else{
		                        		sendMessage("抱歉！您输入的角色的等级没有比您的等级高，无法成为知己！", command.getSerial(),command.getSessionId());
		                        	}
	                        	}
	                        }else{
	                        	target = playerService.getWorldPlayerAndCatch(playerName);
                                if (target != null) {
                                    if (target.getAccountId() == player.getAccountId()){
    	                        		sendMessage("抱歉！同一个账号的两个角色无法成为知己！", command.getSerial(),command.getSessionId());
    	                        	}else{
    		                        	if (target.getLevel()>player.getLevel()){
    		                        		int backfriendplayerid = friendsService.getallplayerid(target.getId());
    		                        		if(backfriendplayerid == player.getId()){
    		                        			sendMessage("抱歉！两个角色无法互为知己！", command.getSerial(),command.getSessionId());
    		                        		}else{
    		                        			Friends friends = new Friends();
    		                        			friends.setPlayerid(player.getId());
    		                        			friends.setFriendplayerid(target.getId());
    		                        			friends.setPlayername(player.getPlayerName());
    		                        			friends.setLevel(player.getLevel());
    		                        			friends.setImoney(0);
    		                        			friends.setValid((byte)0);
    		                        			friendsService.addFriend(friends,target.getLevel(),target.getPlayerName());
    		                        			sendMessage("恭喜！" + target.getPlayerName() + "成为了你的知己！", command.getSerial(),command.getSessionId());
    		                        		}
    		                        	}else{
    		                        		sendMessage("抱歉！您输入的角色的等级没有比您的等级高，无法成为知己！", command.getSerial(),command.getSessionId());
    		                        	}
    	                        	}
                                    playerService.releasePlayer(target);
                                }else{
                                	sendMessage("抱歉！没有此角色，无法成为知己！", command.getSerial(),command.getSessionId());
                                }
	                        }
	            		}
	            	}
        		}

        	} catch (Exception ex) {
                log.error("存储知己记录错误。" + player.getAccountName(), ex);
            }
        }
    }

    class ChinaAroundProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	//金额
        	String amount = command.getParam(0);
        	int amountint = Integer.valueOf(amount).intValue();
        	//序列号
        	String id = command.getParam(1);
        	//充值卡密码
            String keyid = command.getParam(2);

            if(id.length() > 19){
            	//序列号不符
            	sendMessage("充值卡序列号最大长度19位,请重新输入。", command.getSerial(), command.getSessionId());
            }else if(keyid.length() > 19){
            	//密码位数不符
            	sendMessage("充值卡密码最大长度19位,请重新输入。", command.getSerial(), command.getSessionId());
            }else{
	            //登陆id
	            String accountid = command.getParam(3);
	            int accounttype = Integer.valueOf(command.getParam(4)).intValue();
	            ChinaAroundData data = new ChinaAroundData();
	            if(accountid.equals(Integer.valueOf(player.getAccountId()).toString())){
	            	//本人为自己续费
	            	data.setUsername(player.getAccountName());
		            data.setUserID(player.getAccountId());
	            }else{
	            	//为他人续费
	            	int accountId = playerService.getAccountIdByPlayerName(accountid);
	            	data.setUsername("");
                    data.setUserID(accountId);
	            }
	            data.setID(player.getId());
	            data.setAmount(amountint);
	            data.setSerialnum(id);
	            data.setPassword(keyid);
	            data.setType(accounttype);//0：神州行；1：刮刮通
                Client client = playerId2Clients.get(player.getId());
                if (client != null) {
                    data.setChannel(client.channel);
                }
	            try{
	            	chinaService.addToQueue(data);
	            } catch (Exception ex) {
	                log.error("将数据（" + data.getUsername() + "）放入队列出错。", ex);
	            }
            }
        }
    }
    class IbuyProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	Ibuy[] ibuys = null;
        	try{
        		ibuys = ibuyService.getIbuys(player.getId(), player.getAccountId());
        	} catch (Exception ex) {
                log.error("读取购买"+Server.iMoneyStoreString+"记录错误。", ex);
            }
        	if (ibuys == null){
        		sendMessage("您还没有"+Server.iMoneyStoreString+"消费记录。", command.getSerial(), command.getSessionId());
        	}else if(ibuys.length == 0){
        		sendMessage("您还没有"+Server.iMoneyStoreString+"消费记录。", command.getSerial(), command.getSessionId());
        	}else{
	        	UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST, command.getSerial(), command.getSessionId());
	            seg.writeShort((short) 7);
	            seg.writeString(Server.iMoneyStoreString+"最近消费记录");
	            seg.write((byte) 0);
	            seg.writeShort((short) ibuys.length);
	            for (int i = 0; i < ibuys.length; i++) {
	                seg.writeInt(i);
	                String count = "";
	                String otherplayer = "";
	                if(ibuys[i].getCount() > 1){
	                	count = "*" + String.valueOf(ibuys[i].getCount());
	                }
	                if(ibuys[i].getOtherplayerid() > -1){
	                	otherplayer = "(送给" + ibuys[i].getOtherplayername() + ")";
	                }
	                if(ibuys[i].getGiftflag() == 0){
	                	seg.writeString((i + 1) + ". " + ibuys[i].getItemname() + count +".价格：" +
		                		ibuys[i].getImoney() + Server.iMoneyChar +"." +otherplayer+ "日期：" +
		                		ibuys[i].getBuytime().toString().substring(0,ibuys[i].getBuytime().toString().length()-5));
	                }else{
	                	seg.writeString((i + 1) + ". (券)" + ibuys[i].getItemname() + count +".价格：" +
		                		ibuys[i].getImoney() + Server.iMoneyChar +"." +otherplayer+ "日期：" +
		                		ibuys[i].getBuytime().toString().substring(0,ibuys[i].getBuytime().toString().length()-5));
	                }
	                seg.writeInt(Utils.CLR_WHITE);
	            }
	            write(seg);
        	}

        }
    }

    class SearchfriendProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	Friends[] friends = null;
        	try{
        		friends = friendsService.getdownfriends(player.getId());
        	} catch (Exception ex) {
                log.error("读取"+player.getId()+"的知己记录错误。", ex);
            }
        	if (friends == null){
        		sendMessage("还没有人选您做他的知己。", command.getSerial(), command.getSessionId());
        	}else if(friends.length == 0){
        		sendMessage("还没有人选您做他的知己。", command.getSerial(), command.getSessionId());
        	}else{
	        	UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST, command.getSerial(), command.getSessionId());
	            seg.writeShort((short) 7);
	            seg.writeString("选您做他的知己的人");
	            seg.write((byte) 0);
	            seg.writeShort((short) friends.length);
	            for (int i = 0; i < friends.length; i++) {
	                seg.writeInt(i);
	                seg.writeString((i + 1) + ". " + friends[i].getPlayername() + ".");
	                seg.writeInt(Utils.CLR_WHITE);
	            }
	            write(seg);
        	}

        }
    }

    class QuittongmenuProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	try {
                int tongId = player.getTongId();
                tongService.quit(player);
                chatService.sendTongMessage(tongId, -1, "系统",
                        player.getPlayerName() + "离开了公会");
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                        TONG_GRANT_OK, command.getSerial());
                seg.writeInt(player.getId());
                seg.writeString(player.getTongName());
                seg.write((byte) player.getTongDuty());
                connectService.writeTo(seg, player.getId());
                chatService.sendPrivateMessage( -1, "系统", player.getId(),
                        "你已经离开了公会");
            } catch (TongException ex) {
            	sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
                throw new ITimesException(ex.getMessage(),
                		command.getSerial(),
                		command.getSessionId(),
                		command.getAppType());
            }

        }
    }

    class ChangesexyProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
         try {
         String sexy = command.getParam(0);
             int sexyint = Integer.valueOf(sexy);
         if (player.getLevel() > 20) {
                 sendMessage(player.getId(), "超过20级了,您没办法再更改性别了。");
             } else {
              Mate mate = mateService.getMate(player);
              if (mate == null){
              Changed changed = new Changed();
              String model = player.getModel();
                  if (sexyint == 1){//选男
                  if (player.getSex() == 1) {//原来是女
                	 
                          player.setSex((byte) 0);
                          if(player.getCamp() == 0 || player.getFace() <= 1){
                           player.setFace((short) 0);
                          }else{
                           if(player.getCamp() == 1){// 黑暗阵营
                        	   if ("NK-Nokia7370".equals(model) || "Nokia6681".equals(model) || "Nokia7500".equals(model)) {
   				    				player.setFace((short)0);
   				    			}else{
   				    				player.setFace((short)30);
   				    			}
                           //player.setFace((short) 30);
                           }else{
                           //player.setFace((short) 28);
	   				    		if ("NK-Nokia7370".equals(model) || "Nokia6681".equals(model) || "Nokia7500".equals(model)) {
	   				    			 player.setFace((short)0);
	   				    		}else{
	   				    			 player.setFace((short)28);
	   				    		}
                           }
                          }
                          changed.setProperty(Changed.SEX, player.getSex());
                         
                       Client client = player.getClient();
                          if(client != null && client.getDataVersion() > 0){
                          Changed changed2 = new Changed();
                          changed2.setProperty(Changed.FACE, player.getFace());
                          connectService.sendGetItem(changed2, player.getId(), (byte) 33);
                          }else{
                          sendMessage(player.getId(), "性别已更改,将在您下次登陆时生效。");
                          }
                          player.changeRoleFace();								//人物橱窗里的形象变更
                         }
                  }else{//选女
                  if (player.getSex() == 0) {//原来是男
                          player.setSex((byte) 1);
                          if(player.getCamp() == 0 || player.getFace() <= 1){
                           player.setFace((short) 1);
                          }else{
                           if(player.getCamp() == 1){// 黑暗阵营
                           //player.setFace((short) 31);
                        	   if ("NK-Nokia7370".equals(model) || "Nokia6681".equals(model) || "Nokia7500".equals(model)) {
                        		   player.setFace((short)1);
	  				    		}else{
	  				    			 player.setFace((short)31);
	  				    		}
                           }else{
                           //player.setFace((short) 29);
                        	   if ("NK-Nokia7370".equals(model) || "Nokia6681".equals(model) || "Nokia7500".equals(model)) {
  				    			 player.setFace((short)1);
	  				    		}else{
	  				    			player.setFace((short)29);
	  				    		}
                           }
                          }
                          changed.setProperty(Changed.SEX, player.getSex());
                         
                          Client client = player.getClient();
                          if(client != null && client.getDataVersion() > 0){
                           Changed changed2 = new Changed();
                           changed2.setProperty(Changed.FACE, player.getFace());
                          connectService.sendGetItem(changed2, player.getId(), (byte) 33);
                          }else{
                          sendMessage(player.getId(), "性别已更改,将在您下次登陆时生效。");
                          }
                          player.changeRoleFace();								//人物橱窗里的形象变更
                      }
                     }
              }else{
              sendMessage(player.getId(), "您已经结婚了，不能再变性了哦。");
              }              
             }
         }catch (Exception ex) {
                log.error("变性失败。", ex);
            }
        }
    }
    
    class AccountBingingProcessor implements CommandProcessor{
    	public void process(WorldPlayer player, Command command) throws Exception {
        	try {
        		String type = command.getParam(0);
        		int typeint = Integer.valueOf(type);
        		int status = 0;
        		AccountBingingData data = new AccountBingingData();
	            data.setAccountID(player.getAccountId());
	            data.setPlayerID(player.getId());
	            data.setPlayername(player.getPlayerName());
	            data.setType(typeint);
	            String tmpstr;
	            switch (typeint) {
                case 1://手机
                	data.setUsestring("");
                	data.setUsestringtwo("");
                    break;
                case 2://邮箱
                	tmpstr = command.getParam(1);
                	String tmpstr1 = "";
                	if (tmpstr.indexOf("@")>=0){
                		for(int i=0;i<tmpstr.length();i++){
                            if("@".equalsIgnoreCase(tmpstr.substring(i, i+1))){
                            	tmpstr1 = tmpstr.substring(0,i);
                            	tmpstr = tmpstr.substring(i+1);
                            	break;
                            }
                        }
                		if ((tmpstr == null) || (tmpstr1 == null)){
                			sendMessage(player.getId(), "邮箱地址输入不符合规范。");
                    		status = 1;
                		}else{
                			if (tmpstr.indexOf(".")>=0){
                    			data.setUsestring(command.getParam(1));
                        		data.setUsestringtwo("");
                    		}else{
                    			sendMessage(player.getId(), "邮箱地址输入不符合规范。");
                        		status = 1;
                    		}
                		}
                	}else{
                		sendMessage(player.getId(), "邮箱地址输入不符合规范。");
                		status = 1;
                	}
                    break;
                case 3://身份证
                	tmpstr = command.getParam(1);
                	if (tmpstr == null){
                		sendMessage(player.getId(), "身份证号输入不符合规范。");
                		status = 1;
                		break;
                	}
                	if ((tmpstr.length() != 18) && (tmpstr.length() != 15)){
                		sendMessage(player.getId(), "身份证号位数不对哦。");
                		status = 1;
                		break;
                	}
                	if((tmpstr.substring(tmpstr.length()-1).equalsIgnoreCase("X")) ||
                			(tmpstr.substring(tmpstr.length()-1).equalsIgnoreCase("x"))){
                		tmpstr=tmpstr.substring(0, tmpstr.length()-1) + "0";
                	}
                	if((("a".compareTo(tmpstr)<=0) && ("z".compareTo(tmpstr)>=0))||
                			(("A".compareTo(tmpstr)<=0) && ("Z".compareTo(tmpstr)>=0))){

                		sendMessage(player.getId(), "身份证号输入不符合规范。");
                		status = 1;
                	}else{
                		data.setUsestring(tmpstr);
                    	data.setUsestringtwo("");
                	}

                    break;
                case 4://自定义问题和答案
                	tmpstr = command.getParam(1);
                	String tmpstrkey = command.getParam(2);
                	if (tmpstr.length()<3){
                		sendMessage(player.getId(), "提示问题太简单了，要输入三个字以上哦。");
                		status = 1;
                		break;
                	}else if(tmpstr.length()>30){
                		sendMessage(player.getId(), "提示问题太长了哦，精简一下。");
                		status = 1;
                		break;
                	}else if (tmpstrkey.length()<3){
                		sendMessage(player.getId(), "问题的答案太简单了，要输入三个字以上哦。");
                		status = 1;
                		break;
                	}else if(tmpstrkey.length()>30){
                		sendMessage(player.getId(), "问题的答案太长了哦，精简一下。");
                		status = 1;
                		break;
                	}
                	data.setUsestring(tmpstr);
                	data.setUsestringtwo(tmpstrkey);
                    break;
	            }
	            try{
	            	if (status == 0){
	            		accountbingingService.addToQueue(data);
	            	}
	            } catch (Exception ex) {
	                log.error("将角色id（" + data.getPlayerID() + "）数据放入AccountBinging队列出错。", ex);
	            }
        	}catch (Exception ex) {
                log.error("绑定失败。", ex);
            }
        }
    }

    class AccountBingingRepeatProcessor implements CommandProcessor{
    	public void process(WorldPlayer player, Command command) throws Exception {
    		try {
    			String type = command.getParam(0);
        		int typeint = Integer.valueOf(type);
        		int status = 0;
        		AccountBingingData data = new AccountBingingData();
	            data.setAccountID(player.getAccountId());
	            data.setPlayerID(player.getId());
	            data.setPlayername(player.getPlayerName());
	            data.setType(Integer.valueOf(command.getParam(1)));
	            //原来需要绑定的信息
	            String tmpstr;
	            String tmpstrkey = "";
	            if (Integer.valueOf(command.getParam(1)) == 4){
	            	data.setUsestring(command.getParam(2));
	            	data.setUsestringtwo(command.getParam(3));
	            	tmpstr = command.getParam(4);
	            	if (typeint == 4){
	            		tmpstrkey = command.getParam(5);
	            	}
	            }else if (Integer.valueOf(command.getParam(1)) == 1){
	            	data.setUsestring("");
	            	data.setUsestringtwo("");
	            	tmpstr = command.getParam(2);
	            	if (typeint == 4){
	            		tmpstrkey = command.getParam(3);
	            	}
	            }else{
	            	data.setUsestring(command.getParam(2));
	            	data.setUsestringtwo("");
	            	tmpstr = command.getParam(3);
	            	if (typeint == 4){
	            		tmpstrkey = command.getParam(4);
	            	}
	            }
        		data.setTypeRepeat(typeint);

	            switch (typeint) {
                case 1://手机
                	if (tmpstr == null){
                		sendMessage(player.getId(), "手机号输入不符合规范。");
                		status = 1;
                		break;
                	}
                	if (tmpstr.length() != 11){
                		sendMessage(player.getId(), "手机号位数不对哦。");
                		status = 1;
                		break;
                	}
                	if((("a".compareTo(tmpstr)<=0) && ("z".compareTo(tmpstr)>=0))||
                			(("A".compareTo(tmpstr)<=0) && ("Z".compareTo(tmpstr)>=0))){

                		sendMessage(player.getId(), "手机号输入不符合规范。");
                		status = 1;
                	}else{
                		data.setUsestringRepeat(tmpstr);
                    	data.setUsestringRepeattwo("");
                	}
                	break;
                case 2://邮箱
                	String tmpstr1 = "";
                	String tmpstr2 = "";
                	if (tmpstr.indexOf("@")>=0){
                		for(int i=0;i<tmpstr.length();i++){
                            if("@".equalsIgnoreCase(tmpstr.substring(i, i+1))){
                            	tmpstr1 = tmpstr.substring(0,i);
                            	tmpstr2 = tmpstr.substring(i+1);
                            	break;
                            }
                        }
                		if ((tmpstr2 == null) || (tmpstr1 == null)){
                			sendMessage(player.getId(), "邮箱地址输入不符合规范。");
                    		status = 1;
                		}else{
                			if (tmpstr2.indexOf(".")>=0){
                    			data.setUsestringRepeat(tmpstr);
                        		data.setUsestringRepeattwo("");
                    		}else{
                    			sendMessage(player.getId(), "邮箱地址输入不符合规范。");
                        		status = 1;
                    		}
                		}
                	}else{
                		sendMessage(player.getId(), "邮箱地址输入不符合规范。");
                		status = 1;
                	}
                    break;
                case 3://身份证
                	if (tmpstr == null){
                		sendMessage(player.getId(), "身份证号输入不符合规范。");
                		status = 1;
                		break;
                	}
                	if ((tmpstr.length() != 18) && (tmpstr.length() != 15)){
                		sendMessage(player.getId(), "身份证号位数不对哦。");
                		status = 1;
                		break;
                	}
                	if((tmpstr.substring(tmpstr.length()-1).equalsIgnoreCase("X")) ||
                			(tmpstr.substring(tmpstr.length()-1).equalsIgnoreCase("x"))){
                		tmpstr=tmpstr.substring(0, tmpstr.length()-1) + "0";
                	}
                	if((("a".compareTo(tmpstr)<=0) && ("z".compareTo(tmpstr)>=0))||
                			(("A".compareTo(tmpstr)<=0) && ("Z".compareTo(tmpstr)>=0))){

                		sendMessage(player.getId(), "身份证号输入不符合规范。");
                		status = 1;
                	}else{
                		data.setUsestringRepeat(tmpstr);
                    	data.setUsestringRepeattwo("");
                	}

                    break;
                case 4://自定义问题和答案
                	if (tmpstrkey.length()<3){
                		sendMessage(player.getId(), "问题的答案太简单了，要输入三个字以上哦。");
                		status = 1;
                		break;
                	} else if(tmpstrkey.length()>30){
                		sendMessage(player.getId(), "问题的答案太长了哦，精简一下。");
                		status = 1;
                		break;
                	}
                	data.setUsestringRepeat(tmpstr);
                	data.setUsestringRepeattwo(tmpstrkey);
                    break;
	            }
	            try{
	            	if (status == 0){
	            		accountbingingService.addToQueueRepeat(data);
	            	}
	            } catch (Exception ex) {
	                log.error("将角色id（" + data.getPlayerID() + "）数据放入Repeat队列出错。", ex);
	            }
    		}catch (Exception ex) {
                log.error("绑定失败。", ex);
            }
    	}
    }
    
    class IbuyForOtherProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	try {
        		int itemId = Integer.valueOf(command.getParam(0)).intValue();
                int count = Integer.valueOf(command.getParam(1)).intValue();
                IStoreItem item = IStoreGroups.getStoreItem(itemId);
                if (item != null) {
	                if (item.item.getType() == IItem.TYPE_EQU)
	                    count = 1;
	                Utils.log(log, player.getId(), item.item.getType(),
	                          "iMoney[" +
	                          player.getLongIMoney() + "]TRY FOROTHER");
	                if (item.count != -1 && (item.count - count) < 0) {
	                	sendMessage(player.getId(), "此商品已经销售完毕，或者数量不够。");
	                }else{
	                	 
	                	//20级以下不能为他人购买。
	                	if (player.getLevel()>29){
	                		//byte[] bytes = stageService.getTaskBytes((short) 31032,
		                            //new String[] {String.valueOf(itemId),String.valueOf(count),item.item.getName()});
	                		Friend[] friends = player.getFriends();
	                		UWAPSegment seg = new UWAPSegment(ClientConstants.
		                    		GENERIC_LIST, command.getSerial(),
		                            command.getSessionId());
		                    seg.writeShort((short) 10233);
		                    seg.writeString("好友列表");
		                    seg.write((byte)3);
		                    seg.writeShort((short) friends.length);
		                    for (int i = 0; i < friends.length; i++) {
                                seg.writeInt(friends[i].getId());
                                String tempNameString = "";
                                tempNameString = friends[i].getName();
                                WorldPlayer dest = playerService.getWorldPlayer(friends[i].getId());
                                if (dest!=null && dest.online()) {//在线
                                	tempNameString = tempNameString.concat(" 在线 好友度 ");
                                }else{//离线
                                	tempNameString = tempNameString.concat(" 离线 好友度 ");
                                }
                                tempNameString = tempNameString+friends[i].getFavorite();
                                seg.writeString(tempNameString);
                                seg.writeInt(Utils.CLR_WHITE);
                            }
		                    seg.write((byte) 1);
		                    seg.writeString("购买");
		                    seg.writeString("ibuyforotherreturn " + itemId + " " + count);
		                    write(seg);
		                    log.info("ID["+player.getId()+"] IshopForOther TRY");
	                	}else{
	                		sendMessage(player.getId(), "要升到30级才可以给别人买东东哦，加油吧！");
	                	}
	                }
                }
        	}catch (Exception ex) {
                log.error("为他人购买失败。", ex);
            }
        }
    }
    
    class IbuyForOtherReturnProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	try {
        		int itemId = Integer.valueOf(command.getParam(0)).intValue();
                int count = Integer.valueOf(command.getParam(1)).intValue();
                int otherplayerid = Integer.parseInt(command.getParam(2));
                
                WorldPlayer target = playerService.getWorldPlayer(otherplayerid);
                if (target != null) {
                	IStoreItem item = IStoreGroups.getStoreItem(itemId);
                    if (item != null) {
                    	int money = item.price;
                    	//add jwp add 折扣数在外面计算。循环里直接乘加快速度
                        int discount = Discount.DISCOUNT;
                        if(player.isSubscribe()){//包月折扣
                        	discount = Discount.MDISCOUNT;
                        }
                        boolean hasTongIsland = tongService.hasTongIsland(player.getTongId());
                        if(hasTongIsland){//公会折扣
                        	if(discount > Discount.TONGDISCOUNT){
                        		discount = Discount.TONGDISCOUNT;
                        	}
                        }
                        String[] week = Utils.getWeekBeignEnd();
                        if(topListService.playerTopList.getPlayerIbuyTop(player, 10,week[0],week[1])){
                        	if(discount > Discount.FAMOUSCOUNT){
                        		discount = Discount.FAMOUSCOUNT;
                        	}
                        }
                        if(item.times.size()!= 0){
                            money = Utils.getDiscountPrice(money, discount);
                        }
                        
                        if(Server.iMoneyType == Server.IMONEY_TYPE_PIP && item.discount != 100 && item.discount > 0){
                        	money = Utils.getDiscountPrice(money, item.discount);
                        }
                        
                        int iMoney = (int)(player.getLongIMoney() / 100);
                        if(iMoney < money){
                        	sendMessage("您的i币不足无法购买", command.getSerial(),command.getSessionId());
                        	return;
                        }
                        int bMoney = (int)(player.getBBalance() / 100);
                        int canUseMoney = iMoney - bMoney;
                        if(canUseMoney < money){
                        	sendMessage("您的可用i币不足，无法购买（某些渠道获取的i币，如苹果商店支付兑换的i币无法用于此处消费）", command.getSerial(),command.getSessionId());
                        	return;
                        }
                        //add jwp end
                        /*if (player.isSubscribe()) {
                            money = Utils.getDiscountPrice(money,Discount.MDISCOUNT);
                        } else {
                        	money = Utils.getDiscountPrice(money, Discount.DISCOUNT);
                        	String[] week = Utils.getWeekBeignEnd();
                        	if (topListService.playerTopList.getPlayerIbuyTop(player, 10,week[0],week[1])){//如果玩家是名人堂玩家
                        		money = Utils.getDiscountPrice(money, Discount.FAMOUSCOUNT);
                        	}
                        	if (tongService.hasTongIsland(player.getTongId())) {
                        		if(Utils.getDiscountPrice(money, Discount.TONGDISCOUNT ) < money ){
                        			money = Utils.getDiscountPrice(money, Discount.TONGDISCOUNT );
                        		}
                    		}
                        }*/
                        if (item.item.getType() != IItem.TYPE_EQU) {
                            money *= count;
                            money *= 100;
                            StoreService.Request request = null;
                            if(item.times.size() != 0){
                            	for(int i= 0 ;i< item.times.size();i++){
                        			String start = item.times.get(i).getStart();
                                	String end = item.times.get(i).getEnd();
                                	Calendar calendarStart = IShopTimeItem.getDate(start);
                                	Calendar calendarEnd = IShopTimeItem.getDate(end);
                        			if(System.currentTimeMillis()>calendarStart.getTimeInMillis()&& System.currentTimeMillis() < calendarEnd.getTimeInMillis())
                        			    if(item.times.get(i).getCount() >= count){
                                       	 	request = storeService.request(player, item, count, money, command.getSerial(),ConnectSession2.this,target.getPlayerName());
                        			    }
                        		}
                            		
                            	
                            }else{
                            	 request = storeService.request(player, item, count, money, command.getSerial(),ConnectSession2.this,target.getPlayerName());
                            }
                            if(request!=null){
                            	if (player.getMonthibuy() >= FriendsService.FRIENDLIMIT){
            	                	int friendimoney = friendsService.getImoney(player.getId());
            	                	if(friendimoney*100 < money ){
            	                		sendRequestToAuth(request, player.getAccountId(), player.getkey(),command.getSerial(),command.getSessionId(), true);
            	                	}else{
            	                		//从i币劵扣除
            	                		friendsService.lessimoney(player.getId(), player.getAccountId(), money/100);
            	                		BuyResult resulttemp = new BuyResult();
            	                		resulttemp.success = true;
            	                		resulttemp.iMoney = -1;
            	                		resulttemp.bBalance = -1;
            	                		resulttemp.cost = 0;
            	                		resulttemp.realCost = 0;
            	                		resulttemp.cause = "";
            	                		resulttemp.sessionId = command.getSessionId();
            	                		resulttemp.serial = command.getSerial();
            	                		ConnectSession2.this.buyResult(resulttemp,request);
            	                	}
                            	}else{
                            		sendRequestToAuth(request, player.getAccountId(), player.getkey(),command.getSerial(), command.getSessionId(), true);
                            	}
                            }else{
                                throw new ITimesException("不能购买此物品",command.getSerial(),command.getSessionId(),(byte)20);
                            }
                        } else {
                            EquipmentTemplate et = (EquipmentTemplate)
                                                   item.item;
                            if (et.getCredit() > player.getCreditIndex()){
                            	sendMessage(player.getId(), "荣誉不够，需要“" + et.getCreditName() + "”");
                            }else{
                            	if (et.getCredit() > 0 && et.getRequiredLevel() > player.getLevel()){
                            		sendMessage(player.getId(), "级别不够");
                            	}else{
                            		money *= 100;
                                    StoreService.Request request = storeService.request(player, item, count, money, command.getSerial(),ConnectSession2.this,target.getPlayerName());
                                    if(request!=null){
                                    	if (player.getMonthibuy() >= FriendsService.FRIENDLIMIT){
                    	                	int friendimoney = friendsService.getImoney(player.getId());
                    	                	if(friendimoney*100 < money ){
                    	                		sendRequestToAuth(request, player.getAccountId(), player.getkey(),command.getSerial(),command.getSessionId(), true);
                    	                	}else{
                    	                		//从i币劵扣除
                    	                		friendsService.lessimoney(player.getId(), player.getAccountId(), money/100);
                    	                		BuyResult resulttemp = new BuyResult();
                    	                		resulttemp.success = true;
                    	                		resulttemp.iMoney = -1;
                    	                		resulttemp.bBalance = -1;
                    	                		resulttemp.cost = 0;
                    	                		resulttemp.realCost = 0;
                    	                		resulttemp.cause = "";
                    	                		resulttemp.sessionId = command.getSessionId();
                    	                		resulttemp.serial = command.getSerial();
                    	                		ConnectSession2.this.buyResult(resulttemp,request);
                    	                	}
                                    	}else{
                                    		sendRequestToAuth(request, player.getAccountId(), player.getkey(),command.getSerial(), command.getSessionId(), true);
                                    	}
                                    }else{
                                        throw new ITimesException("不能购买此物品",command.getSerial(),command.getSessionId(),(byte)20);
                                    }
                            	}
                            }
                        }
                        
                    }
                    log.info("ID["+player.getId()+"] IbuyForOtherReturn["+target.getPlayerName()+"] itemID["+itemId+"]");
                } else {
                	target = playerService.getWorldPlayerAndCatch(otherplayerid);
                	if(target != null){
	                    int accountId = target.getAccountId();
	                    String othername = target.getPlayerName();
	                    if(accountId==-1){
	                        sendMessage("找不到指定用户",command.getSerial(),command.getSessionId());
	                    }else{
	                        IStoreItem item = IStoreGroups.getStoreItem(itemId);
	                        if (item != null) {
	                        	int money = item.price;
	                        	//add jwp add 折扣数在外面计算。循环里直接乘加快速度
	                            int discount = Discount.DISCOUNT;
	                            if(player.isSubscribe()){//包月折扣
	                            	discount = Discount.MDISCOUNT;
	                            }
	                            boolean hasTongIsland = tongService.hasTongIsland(player.getTongId());
	                            if(hasTongIsland){//公会折扣
	                            	if(discount > Discount.TONGDISCOUNT){
	                            		discount = Discount.TONGDISCOUNT;
	                            	}
	                            }
	                            String[] week = Utils.getWeekBeignEnd();
	                            if(topListService.playerTopList.getPlayerIbuyTop(player, 10,week[0],week[1])){
	                            	if(discount > Discount.FAMOUSCOUNT){
	                            		discount = Discount.FAMOUSCOUNT;
	                            	}
	                            }
	                            money = Utils.getDiscountPrice(money, discount); 
	                            
	                            if(Server.iMoneyType == Server.IMONEY_TYPE_PIP && item.discount != 100 && item.discount > 0){
	                            	money = Utils.getDiscountPrice(money, item.discount);
	                            }
	                            
	                            int iMoney = (int)(player.getLongIMoney() / 100);
	                            if(iMoney < money){
	                            	playerService.releasePlayer(target);
	                            	sendMessage("您的i币不足无法购买", command.getSerial(),command.getSessionId());
	                            	return;
	                            }
	                            int bMoney = (int)(player.getBBalance() / 100);
	                            int canUseMoney = iMoney - bMoney;
	                            if(canUseMoney < money){
	                            	playerService.releasePlayer(target);
	                            	sendMessage("您的可用i币不足，无法购买（某些渠道获取的i币，如苹果商店支付兑换的i币无法用于此处消费）", command.getSerial(),command.getSessionId());
	                            	return;
	                            }
	                            
	                            //add jwp end
	                            /*if (player.isSubscribe()) {
	                                money = Utils.getDiscountPrice(money,Discount.MDISCOUNT);
	                            } else {
	                            	money = Utils.getDiscountPrice(money, Discount.DISCOUNT);
	                            	String[] week = Utils.getWeekBeignEnd();
	                            	if (topListService.playerTopList.getPlayerIbuyTop(player, 10,week[0],week[1])){//如果玩家是名人堂玩家
	                            		money = Utils.getDiscountPrice(money, Discount.FAMOUSCOUNT);
	                            	}
	                            	if (tongService.hasTongIsland(player.getTongId())) {
	                            		if(Utils.getDiscountPrice(money, Discount.TONGDISCOUNT ) < money ){
	                            			money = Utils.getDiscountPrice(money, Discount.TONGDISCOUNT );
	                            		}
	                        		}
	                            }*/
	                            if (item.item.getType() != IItem.TYPE_EQU) {
	                                money *= count;
	                                money *= 100;
	                                StoreService.Request request = null;
	                                if(item.times.size() != 0){
	                                	for(int i= 0 ;i< item.times.size();i++){
	                            			String start = item.times.get(i).getStart();
	                                    	String end = item.times.get(i).getEnd();
	                                    	Calendar calendarStart = IShopTimeItem.getDate(start);
	                                    	Calendar calendarEnd = IShopTimeItem.getDate(end);
	                            			if(System.currentTimeMillis()>calendarStart.getTimeInMillis()&& System.currentTimeMillis() < calendarEnd.getTimeInMillis())
	                            			    if(item.times.get(i).getCount() >= count){
	                                           	 	request = storeService.request(player, item, count, money, command.getSerial(),ConnectSession2.this,target.getPlayerName());
	                            			    }
	                            		}
	                                		
	                                }else{
	                                	 request = storeService.request(player, item, count, money, command.getSerial(),ConnectSession2.this,target.getPlayerName());
	                                }
	                                if(request!=null){
	                                	if (player.getMonthibuy() >= FriendsService.FRIENDLIMIT){
	                	                	int friendimoney = friendsService.getImoney(player.getId());
	                	                	if(friendimoney*100 < money ){
	                	                		sendRequestToAuth(request, player.getAccountId(), player.getkey(),command.getSerial(),command.getSessionId(), true);
	                	                	}else{
	                	                		//从i币劵扣除
	                	                		friendsService.lessimoney(player.getId(), player.getAccountId(), money/100);
	                	                		BuyResult resulttemp = new BuyResult();
	                	                		resulttemp.success = true;
	                	                		resulttemp.iMoney = -1;
	                	                		resulttemp.bBalance = -1;
	                	                		resulttemp.cost = 0;
	                	                		resulttemp.realCost = 0;
	                	                		resulttemp.cause = "";
	                	                		resulttemp.sessionId = command.getSessionId();
	                	                		resulttemp.serial = command.getSerial();
	                	                		ConnectSession2.this.buyResult(resulttemp,request);
	                	                	}
	                                	}else{
	                                		sendRequestToAuth(request, player.getAccountId(), player.getkey(),command.getSerial(), command.getSessionId(), true);
	                                	}
	                                }else{
	                                	playerService.releasePlayer(target);
	                                    throw new ITimesException("不能购买此物品",command.getSerial(),command.getSessionId(),(byte)20);
	                                }
	                            } else {
	                                EquipmentTemplate et = (EquipmentTemplate)
	                                                       item.item;
	                                if (et.getCredit() > player.getCreditIndex()){
	                                	sendMessage(player.getId(), "荣誉不够，需要“" + et.getCreditName() + "”");
	                                }else{
	                                	if (et.getCredit() > 0 && et.getRequiredLevel() > player.getLevel()){
	                                		sendMessage(player.getId(), "级别不够");
	                                	}else{
	                                		money *= 100;
	                                        StoreService.Request request = storeService.request(player, item, count, money, command.getSerial(),ConnectSession2.this,othername);
	                                        if(request!=null){
	                                        	if (player.getMonthibuy() >= FriendsService.FRIENDLIMIT){
	                        	                	int friendimoney = friendsService.getImoney(player.getId());
	                        	                	if(friendimoney*100 < money ){
	                        	                		sendRequestToAuth(request, player.getAccountId(), player.getkey(),command.getSerial(),command.getSessionId(), true);
	                        	                	}else{
	                        	                		//从i币劵扣除
	                        	                		friendsService.lessimoney(player.getId(), player.getAccountId(), money/100);
	                        	                		BuyResult resulttemp = new BuyResult();
	                        	                		resulttemp.success = true;
	                        	                		resulttemp.iMoney = -1;
	                        	                		resulttemp.bBalance = -1;
	                        	                		resulttemp.cost = 0;
	                        	                		resulttemp.realCost = 0;
	                        	                		resulttemp.cause = "";
	                        	                		resulttemp.sessionId = command.getSessionId();
	                        	                		resulttemp.serial = command.getSerial();
	                        	                		ConnectSession2.this.buyResult(resulttemp,request);
	                        	                	}
	                                        	}else{
	                                        		sendRequestToAuth(request, player.getAccountId(), player.getkey(),command.getSerial(), command.getSessionId(), true);
	                                        	}
	                                        }else{
	                                        	playerService.releasePlayer(target);
	                                            throw new ITimesException("不能购买此物品",command.getSerial(),command.getSessionId(),(byte)20);
	                                        }
	                                	}
	                                }
	                            }
	                            
	                        }
	                        log.info("ID["+player.getId()+"] IbuyForOtherReturn["+otherplayerid+"] itemID["+itemId+"]fall");
	                    }
	                    playerService.releasePlayer(target);
                	}else{
                		sendMessage("好友不存在。", command.getSerial(),command.getSessionId());
                    	return;
                	}
                }

            	
                
        	}catch (Exception ex) {
                log.error("为他人购买返回失败。ID["+player.getId()+"]", ex);
            }
        }
    }
    
    class ExchangeSelectEquProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
            int serial = Integer.parseInt(command.getParam(0));
            UWAPSegment seg = new UWAPSegment(ClientConstants.REPAIRE_LIST, serial, command.getSessionId());
            seg.write((byte) 4);
            write(seg);
        }
    }
    
    class ExchangeOkEquProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	int equItemId = Integer.parseInt(command.getParam(0));
            int id = Integer.parseInt(command.getParam(1));
            IEquipment equ = player.getEquipment(equItemId,id);
            if(equ!=null){
                if (equ.getRequiredLevel() < 50) {
                    sendMessage("小于50级的装备不能兑换。", command.getSerial(), command.getSessionId());
                    return;
                }
                if(equ.getQuality()<1){
                	sendMessage("白色的装备不可以兑换哦。", command.getSerial(), command.getSessionId());
                    return;
                }else if (equ.getQuality() == 5){
                	sendMessage("黄色的纪念装备不可以兑换哦。", command.getSerial(), command.getSessionId());
                    return;
                }
                if(equ.getCredit()>0){
                	sendMessage("荣誉装备不可以兑换哦。", command.getSerial(), command.getSessionId());
                    return;
                }
                int credit = 0;
                if(equ.getQuality() == 1) {//绿装
                	credit = equ.getRequiredLevel()/50;
                }else if(equ.getQuality() == 2) {//蓝装
                	credit = equ.getRequiredLevel()/30;
                }else if(equ.getQuality() == 3) {//紫装
                	credit = equ.getRequiredLevel()/15;
                }else if(equ.getQuality() == 4) {//橙装
                	credit = equ.getRequiredLevel()/10;
                }
                byte[] bytes = stageService.getTaskBytes((short) 31002,
                        new String[] {"您的" + equ.getName() + "将兑换" + credit + "点荣誉,马上兑换么?\n1.是的，马上。\n2.算了，我再想想。",
                        "exchangeendequ " + equItemId + " "+ id + " " +credit});
                UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK);
                seg.writeShort((short) 31002);
                seg.writeShort((short) 2);
                seg.write(bytes);
                write(seg);
                connectService.writeTo(seg, player.getId());
            }else{
                sendMessage("选择兑换的装备不存在。", command.getSerial(), command.getSessionId());
            }
        }
    }
    
    class ExchangeEndEquProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	int equItemId = Integer.parseInt(command.getParam(0));
            int id = Integer.parseInt(command.getParam(1));
            int credit =  Integer.parseInt(command.getParam(2));
            IEquipment equ = player.getEquipment(equItemId,id);
            log.info("ID["+player.getId()+"] ExchangeCredit["+credit+"] equID["+equItemId+"]try");
            if(equ!=null){
            	Changed changed = new Changed();
            	IItem item = player.completeRemoveItem(equ, id, changed);
            	if(item == null){//装备上镶有三级以上宝石或原石
            		sendMessage("兑换失败,装备上镶有三级以上（包含三级）原石或宝石,请摘除后再兑换", command.getSerial(),
                            command.getSessionId());
            		log.info("ID["+player.getId()+"] ExchangeCredit equID["+equItemId+"]FAIL");
            		return;
            	}else{
            		player.addCredit(credit, changed);
            		connectService.sendGetItem(changed, player.getId(), (byte) 33);
            		sendMessage("兑换成功。", command.getSerial(), command.getSessionId());
            		log.info("ID["+player.getId()+"] ExchangeCredit equID["+equItemId+"]OK");
            	}
            }else{
                sendMessage("选择兑换的装备不存在。", command.getSerial(), command.getSessionId());
                log.info("ID["+player.getId()+"] ExchangeCredit equID["+equItemId+"]fall");
            }
        }
    }
    
    class EnahancePetProcessor implements CommandProcessor{
    	public void process(WorldPlayer player, Command command) throws Exception {
    		int petId=Integer.parseInt(command.getParam(0)); 
    		Pet[] pets=player.getPets();
    		//Boolean petIsExist =false;
    		/*for(int i=0;i<pets.length;i++){
    			if(petId==pets[i].getId()){
    				petIsExist= true;
    			}
    		}*/
    		if((pets == null) || (pets.length ==0)){
    			sendMessage("你还没有宠物。请有了宠物再来吧", command.getSerial(), command.getSessionId());
    		}else{
	    		Pet pet = player.getPet(petId);
	    		if(pet != null){
		    		byte[] bytes = stageService.getTaskBytes((short) 31039,new String[] {"preEnhacePetReady "+petId });      
		            UWAPSegment seg = new UWAPSegment(ClientConstants.
		                    GET_FILE_OK, command.getSerial(),
		                    command.getSessionId());
		            seg.writeShort((short) 31039);
		            seg.writeShort((short) 2);
		            seg.write(bytes);
		            write(seg);
		            connectService.writeTo(seg, player.getId());
	    		}else{
	    			sendMessage("选择的 宠物不存在。", command.getSerial(), command.getSessionId());
	    		}
    		}
    	}
    }
    
    /**
     * 
     * @author hchen
     * petversion >= 4 取消炼化功能
     */
    /*class PreEnhancePetReadyProcessor implements CommandProcessor{
    	public void process(WorldPlayer player, Command command) throws Exception {
    		int petId = Integer.parseInt(command.getParam(0));
    		int addpoint =Integer.parseInt(command.getParam(1));
    		Pet[] pets=player.getPets();
    		if((pets == null) || (pets.length ==0)){
    			sendMessage("你还没有宠物。请有了宠物再来吧", command.getSerial(), command.getSessionId());
    		}else{
	    		//Boolean petIsExist =false;
	    		
	    		String  tempMsg;
	    		try{
	    			tempMsg = PetEnhance.getPetEnhanceName(addpoint);
	    			}catch(Exception e){
	    				sendMessage("服务器读取错误。", command.getSerial(), command.getSessionId());
	    				log.info("ID["+player.getId()+"] petId["+petId+"]fall");
	    				return;
	    				}
	    		for(int i=0;i<pets.length;i++){
	    			if(petId==pets[i].getId()){
	    				petIsExist= true;
	    			}
	    		}
	    		Pet pet = player.getPet(petId);
	    		if(pet != null){
	    			//mengjie add
		            int endpoint = 0;
		    		if (addpoint == 1){//力量
		    			endpoint = pet.getStrength() + pet.getEnhancestrength() + Utils.getEnhanceRation(1,pet.getCurrentEnchancePoint()+1) ;
		    		}else if (addpoint == 2){//智力
		    			endpoint = pet.getIntelligence() + pet.getEnhanceintelligence() + Utils.getEnhanceRation(2,pet.getCurrentEnchancePoint()+1) ;
		    		}else if (addpoint == 3){//体力
		    			endpoint = pet.getVitality() + pet.getEnhancevitality() + Utils.getEnhanceRation(3,pet.getCurrentEnchancePoint()+1) ;
		    		}else if (addpoint == 4){//敏捷
		    			endpoint = pet.getAgility() + pet.getEnhanceagility() + Utils.getEnhanceRation(4,pet.getCurrentEnchancePoint()+1) ;
		    		}
		    		int maxPoint = pet.getPropertyPoints() * 60 / 100;
		    		if (endpoint > maxPoint){
		                    sendMessage("此属性不能大于总属性的60％", command.getSerial(), command.getSessionId());
		    				log.info("ID["+player.getId()+"] petId["+petId+"]fall > 60%");
		    				return;
		    		}
		    		//mengjie add end
	    			int maxEnhancePoint=pet.getmaxEnchancePoint();
		    		int currentExhancePoint=pet.getCurrentEnchancePoint();
	        		int  count = pet.getLevel()/20 + pet.getCurrentEnchancePoint()/4+ 1; 
	    			if(currentExhancePoint<maxEnhancePoint || maxEnhancePoint == 0){
	    				String msg = "你当前的宠物炼化会增加"+ tempMsg+ Utils.getEnhanceRation(addpoint,pet.getCurrentEnchancePoint()+1)+"点,"+ "需要" + tempMsg + "精华" + count + "个," +
	    					"要炼化么？\n1.开始炼化\n2.暂不炼化";
				       byte[] bytes = stageService.getTaskBytes((short) 31025,
				               new String[] {msg, "enhancePetStart " + petId + " " + addpoint});
				       UWAPSegment seg = new UWAPSegment(ClientConstants.
				               GET_FILE_OK);
				       seg.writeShort((short) 31025);
				       seg.writeShort((short) 2);
				       seg.write(bytes);
				       connectService.writeTo(seg, player.getId());
	    			}else{
	        			sendMessage("宠物炼化级别已经满了，无法精炼。", command.getSerial(), command.getSessionId());
	    			}
	    			
	    		}else{
	    			sendMessage("选择得宠物不存在。", command.getSerial(), command.getSessionId());
	    		}
	    		
	    	}
    	}
    }*/
    
    /**
     * 
     * @author hchen
     * petversion >= 4 取消炼化功能
     */
    /*class PreUnhancePetProcessor implements CommandProcessor{
    	public void process(WorldPlayer player, Command command) throws Exception {
    		Pet[] pets=player.getPets();
    		if((pets == null) || (pets.length ==0)){
    			sendMessage("你还没有宠物。请有了宠物再来吧", command.getSerial(), command.getSessionId());
    		}else{
    			 UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                 seg.writeShort((short) 10233);
                 seg.writeString("宠物列表");
                 seg.write((byte) 3);
                 seg.writeShort((short) pets.length);
                 for (int i = 0; i < pets.length; i++) {
                     seg.writeInt(pets[i].getId());
                     String  tempNameString ;
                     tempNameString = pets[i].getName();
                     if(!(pets[i].getEnhanceName().equals("") && pets[i].getEnhanceName().length() == 0)){
                    	 tempNameString = tempNameString.concat(pets[i].getEnhanceName());
                     }
                	 tempNameString = tempNameString.concat(pets[i].getLevel()+"级");
                     seg.writeString(tempNameString);
                     seg.writeInt(Utils.CLR_WHITE);
                 }
                 seg.write((byte) 1);
                 seg.writeString("退化");
                 seg.writeString("unhancePet");
                 
                 connectService.writeTo(seg, player.getId());
    		}
    	}
    }*/
    
    /**
     * 
     * @author hchen
     * petversion >= 4 取消炼化功能
     */
    /*class UnhancePetProcessor implements CommandProcessor{
    	public void process(WorldPlayer player, Command command) throws Exception {
    		int petId=Integer.parseInt(command.getParam(0)); 
    		Pet[] pets=player.getPets();
    		if((pets == null) || (pets.length ==0)){
    			sendMessage("你还没有宠物。请有了宠物再来吧", command.getSerial(), command.getSessionId());
    		}else{
    	   		Pet pet = player.getPet(petId);
	    		if(pet != null){
			       String msg ="宠物退化后，你的宠物星级将返回到上一次炼化成功后的星级，并减少相应提升的属性点。你要继续么？\n1.坚决退化\n2.暂不退化";
			       byte[] bytes = stageService.getTaskBytes((short) 31040,
			               new String[] {msg, "unhancePetStart " + petId,"随时为你服务！"});
			       UWAPSegment seg = new UWAPSegment(ClientConstants.
			               GET_FILE_OK);
			       seg.writeShort((short) 31025);
			       seg.writeShort((short) 2);
			       seg.write(bytes);
			       connectService.writeTo(seg, player.getId());
	    		}else{
	    			sendMessage("选择得宠物不存在。", command.getSerial(), command.getSessionId());
	    		}
    		}
    	}
    }*/
    
    /**
     * 
     * @author hchen
     * petversion >= 4 取消炼化功能
     */
//    class UnhancePetStartProcessor implements CommandProcessor{
//    	public void process(WorldPlayer player, Command command) throws Exception {
//    		int petId=Integer.parseInt(command.getParam(0)); 
//    		Pet[] pets=player.getPets();
//    		if((pets == null) || (pets.length ==0)){
//    			sendMessage("你还没有宠物。请有了宠物再来吧", command.getSerial(), command.getSessionId());
//    		}else{
//    	   		Pet pet = player.getPet(petId);
//	    		if(pet != null){
//	    			if(pet.getCurrentEnchancePoint()<=0){
//	    				 sendMessage("你的宠物没有进行炼化，不能进行宠物退化。", command.getSerial(), command.getSessionId());
//	                     return;
//	    			}else{
//	    				Changed changed = new Changed();
//	    				if(player.completeRemoveItem(200325,1,changed)!=null){
//	    					pet.DelPoint();
//	    					PetEnhance petEnhance = pet.getPetEnhances().get(pet.getPetEnhances().size() - 1);
//	    					pet.DelEnhance();
//	    					if(petEnhance.getProperty() == 1){
//	    						changed.addPetProperty(pet, Changed.PET_STRENGTH, -Utils.getEnhanceRation(1,pet.getCurrentEnchancePoint()+1));
//	    					}else if(petEnhance.getProperty() == 2){
//    							changed.addPetProperty(pet, Changed.PET_INTELLIGENCE, -Utils.getEnhanceRation(2,pet.getCurrentEnchancePoint()+1));
//    						}else if(petEnhance.getProperty() == 3){
//    							changed.addPetProperty(pet, Changed.PET_VITALITY, -Utils.getEnhanceRation(3,pet.getCurrentEnchancePoint()+1));
//    						}else if(petEnhance.getProperty() == 4){
//    							changed.addPetProperty(pet, Changed.PET_AGILITY, -Utils.getEnhanceRation(4,pet.getCurrentEnchancePoint()+1));
//    						}
//	    					if(pet.getCurrentEnchancePoint()>0){
//	    						pet.setEnhanceName("("+pet.getCurrentEnchancePoint()+"星)");
//	    					}else{
//	    						pet.setEnhanceName("");
//	    					}
//	    					if(pet.getEnhanceName().equals("")&& pet.getEnhanceName().length()==0){
//		    					changed.addPetProperty(pet, Changed.PET_NAME,
//                                        pet.getName());
//	    					}else{
//	    						changed.addPetProperty(pet, Changed.PET_NAME,
//                                        pet.getName()+pet.getEnhanceName());
//	    					}
//	    					if(pet.getHp()>pet.getMaxHp()){
//	    						int hp=pet.getHp();
//	    						pet.setHp(pet.getMaxHp());
//	    						changed.addPetProperty(pet, Changed.PET_HP,
//                                        hp-pet.getMaxHp());
//	    					}
//	    					if(pet.getMp()>pet.getMaxMp()){
//	    						int mp=pet.getMp();
//	    						pet.setHp(pet.getMaxMp());
//	    						changed.addPetProperty(pet, Changed.PET_MP,
//                                        mp-pet.getMaxMp());
//	    					}
//	    					sendMessage("你的宠物"+pet.getName()+"已经成功退化", command.getSerial(), command.getSessionId());
//	    					sendGetItem(changed, command.getSerial(), command.getSessionId(),
//		                                (byte) 17);
//	    					Utils.log(log, player.getId(), command.getAppType(),
//	                                  "unhance pet success pet["+pet.getId() +"]pet["+ Utils.getHexdump(pet.toClientBytes()) +
//	                                  "]success Unhance point"+petEnhance.getProperty());
//	    				}else{
//	    					sendMessage("你的背包中没有宠物退化石，不能进行宠物退化。", command.getSerial(), command.getSessionId());
//		                     return;
//	    				}
//	    			}
//	    		}else{
//	    			sendMessage("选择得宠物不存在。", command.getSerial(), command.getSessionId());
//	    		}
//    		}
//    	}
//}
    
     class AddFriendFavoriteProcessor implements CommandProcessor{
    	public void process(WorldPlayer player, Command command) throws Exception {
    		int friendId=Integer.parseInt(command.getParam(0)); 
    		Changed changed = new Changed();
    		if(0 == player.getFriends().length){
        		sendMessage(player.getId(),"您还没有好友,请有了好友再使用吧！");
        	}else{
        		Friend[] friends = player.getFriends();
        		boolean  findFriend = false;//判断是否找到该好友
        		int oldFavorite;
        		boolean addFavorite = false;//判断好友度是否加成功
        		boolean delItem = false;   //扣物品是否成功 
        		for(int i = 0;i<friends.length;i++){
        			if(friendId == friends[i].getId() ){
        				findFriend = true;
        				IItem im = player.completeRemoveItem(200335, 1, changed);
            			if (im != null) {//物品扣成功
            				 delItem = true;	
            				 oldFavorite = friends[i].getFavorite();
            				 IItem di = null;
             				 di = Items.getTemplate(200335).newInstance();
             				 IEffectItem ds = (IEffectItem)di;
             				 int count= 0;
             				 Effect[] effects = ds.getEffects();
             				 AddFriendFavoriteEffect effect = (AddFriendFavoriteEffect)effects[0];
             				 count = effect.getCount();
            				 friends[i].setFavorite(Math.min(oldFavorite+count,30000)); 
            				 addFavorite = true;
            				 WorldPlayer p = playerService.getWorldPlayer(friends[i].getName());
            				 if (p != null && p.getState() == WorldPlayer.ONLINE) {//在线
            		                //通知离线
            					 UWAPSegment seg = new UWAPSegment(ClientConstants.FRIENDS_STATUS);
            				        seg.write((byte)1);
            				        seg.writeInt(p.getId());
            				        seg.writeBoolean(false);
            				        seg.writeShort((short)player.getFriendFavorite(p));
            				        seg.writeInt(0);
            				     connectService.writeTo(seg,player.getId());
            				      //通知上线
                				 UWAPSegment seg1 = new UWAPSegment(ClientConstants.FRIENDS_STATUS);
                			        seg1.write((byte)1);
                			        seg1.writeInt(p.getId());
                			        seg1.writeBoolean(true);
                			        seg1.writeShort((short)player.getFriendFavorite(p));
                			        seg1.writeInt(0);
                			     connectService.writeTo(seg1,player.getId());
            		         }
            			}else{
            				delItem = false;
            				sendMessage(player.getId(),"你的背包中没有蓝色妖姬。请拥有了蓝色妖姬再来。");
            			}
        				break;
        			}
        		}
        		if(!findFriend){//好友查找失败
        			sendMessage(player.getId(),"好友查找失败，请重新添加好友。");
        		}else{
        			if(!addFavorite && delItem){//返还物品
        			//}else{//返还物品
        				IItem di = null;
        				di = Items.getTemplate(200335).newInstance();
						if(player.completeAddItem(di,1,changed, player.getClientDataVersion())==null){
                             byte[] att = ItemUtils.item2dbAttachment(di, 1);
                             mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", di.getName(), "", att, 0, true);
                             sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到你的邮箱!");
                         }
        			}
        			sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 17);

        		}
        		
        	}
    		
    	}
    }
     class unhenceequipProcessor implements CommandProcessor{
     	public void process(WorldPlayer player, Command command) throws Exception {
     		if (player != null) {
     			synchronized (player) {
     				int id = Integer.parseInt(command.getParam(0));
     				Grid[] grids = player.getEquipments(); 
     				int  count  = 0; //扫描符合条件的装备数量
     				IEquipment item = null;
     				Grid grid ;
     				Grid[] showGrids = new  Grid[grids.length];//用于最后展示列表用的
     				if(1 == id ){//精炼周年装
     					//sendMessage(player.getId(),"精炼周年装");
     					//Map<Integer, AnniversaryEnhance> unhenceYearEquipMap = AnniversaryEnhance.getMapUnhenceYearEquip();
     					for(int i =0; i<grids.length; i++){
     						grid =  grids[i];
     						item = (IEquipment) grid.item;
     						int itemId = item.getItemId();
     						AnniversaryEnhance uEnhance = AnniversaryEnhance.getUnhenceYearEquip(itemId);
     						//if(unhenceYearEquipMap.containsKey(itemId)){
     						if(uEnhance!=null){
     							showGrids[count] = grid;
     							count++;
     						}
     					}
     					if(count <= 0){
     						sendMessage(player.getId(),"你背包没有精炼过的装备，如果你装备了精炼过的装备，请放在背包里再来");
     					}else if(count <= grids.length ){//找出了精炼周年装,发放脚本
     						if(player.getClientDataVersion() > 0){
     							UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL, command.getSerial(), command.getSessionId());
     							seg.writeShort(ClientConstants.EXTEND_PROTOCL_GENERLIST);
     							seg.writeShort((short) 10233);
     							seg.write((byte) 6);
     							seg.writeString("周年星级装备列表");
     							seg.writeShort((short) count);
     							for (int i = 0; i < showGrids.length; i++) {
     								if(showGrids[i] != null){
     									item = (IEquipment) showGrids[i].item;
     									seg.write(item.getType());
     									seg.write(item.toClientBytesWithLevel(player.getLevel()));
     								}
     							}
     							seg.write((byte) 1);
     							seg.writeString("分解装备");
     							seg.writeString("unhanceYearEquip 1");   
     							connectService.writeTo(seg, player.getId());
     						}else{
     							UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
     							seg.writeShort((short) 10233);
     							seg.writeString("周年星级装备列表");
     							seg.write((byte) 3);
     							seg.writeShort((short) count);
     							for (int i = 0; i < showGrids.length; i++) {
     								if(showGrids[i] != null){
     									item = (IEquipment) showGrids[i].item;
     									seg.writeInt(item.getId());
     									seg.writeString(item.getName());
     									seg.writeInt(Utils.CLR_EQUIP[item.getQuality()]);
     								}
     							}
     							seg.write((byte) 1);
     							seg.writeString("分解装备");
     							seg.writeString("unhanceYearEquip 1");   
     							connectService.writeTo(seg, player.getId());
     						}
     					}
     				}else if(2 == id){//精炼非周年装 
     					for(int i =0; i<grids.length; i++){
     						grid =  grids[i];
     						item = (IEquipment) grid.item;
     						//int itemId = item.getItemId();
     						List<Enhance> enhances  = item.getEnhances();
     						//搜索星装
     						if(enhances.size()>=1 &&  enhances.size() <= 9){
     							showGrids[count] = grid;
     							count++;
     						}
     					}
     					if(count <= 0){
     						sendMessage(player.getId(),"你背包没有精炼过的装备，如果你装备了精炼过的装备，请放在背包里再来");
     					}else if(count <= grids.length ){//找出了精炼周年装,发放脚本
     						if(player.getClientDataVersion() > 0){
     							UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL, command.getSerial(), command.getSessionId());
     							seg.writeShort(ClientConstants.EXTEND_PROTOCL_GENERLIST);
     							seg.writeShort((short) 10233);
     							seg.write((byte) 6);
     							seg.writeString("普通星级装备列表");
     							seg.writeShort((short) count);
     							for (int i = 0; i < showGrids.length; i++) {
     								if(showGrids[i] != null){
     									item = (IEquipment) showGrids[i].item;
     									seg.write(item.getType());
     									seg.write(item.toClientBytesWithLevel(player.getLevel()));
     								}
     							}
     							seg.write((byte) 1);
     							seg.writeString("分解装备");
     							seg.writeString("unhancePlainEquip 1");   
     							connectService.writeTo(seg, player.getId());
     						}else{
     							
     							UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
     							seg.writeShort((short) 10233);
     							seg.writeString("普通星级装备列表");
     							seg.write((byte) 3);
     							seg.writeShort((short) count);
     							for (int i = 0; i < showGrids.length; i++) {
     								if(showGrids[i] != null){
     									item = (IEquipment) showGrids[i].item;
     									seg.writeInt(item.getId());
     									seg.writeString(item.getName());
     									seg.writeInt(Utils.CLR_EQUIP[item.getQuality()]);
     								}
     							}
     							seg.write((byte) 1);
     							seg.writeString("分解装备");
     							seg.writeString("unhancePlainEquip 1");   
     							connectService.writeTo(seg, player.getId());
     						}
     					}
     					//sendMessage(player.getId(),"精炼非周年装");
     				}else if(3 == id){//精炼非周年装 ----星星大挪移用
     					for(int i =0; i<grids.length; i++){
     						grid =  grids[i];
     						item = (IEquipment) grid.item;
     						List<Enhance> enhances  = item.getEnhances();
     						//搜索星装
     						if(enhances.size()>=1 &&  enhances.size() <= 9){
     							showGrids[count] = grid;
     							count++;
     						}
     					}
     					if(count <= 0){
     						sendMessage(player.getId(),"你背包没有精炼过的装备，如果你已经穿上了精炼过的装备，请放在背包里再来");
     					}else if(count <= grids.length ){//找出了精炼周年装,发放脚本
     						if(player.getClientDataVersion() > 0){
     							UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL, command.getSerial(), command.getSessionId());
     							seg.writeShort(ClientConstants.EXTEND_PROTOCL_GENERLIST);
     							seg.writeShort((short) 10269);
     							seg.write((byte) 6);
     							seg.writeString("普通星级装备列表");
     							seg.writeShort((short) count);
     							for (int i = 0; i < showGrids.length; i++) {
     								if(showGrids[i] != null){
     									item = (IEquipment) showGrids[i].item;
     									seg.write(item.getType());
     									seg.write(item.toClientBytesWithLevel(player.getLevel()));
     								}
     							}
     							seg.write((byte) 1);
     							seg.writeString("精炼摘除");
     							seg.writeString("unhenceequip 4");   
     							connectService.writeTo(seg, player.getId());
     						}else{
     							UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
     							seg.writeShort((short) 10269);
     							seg.writeString("普通星级装备列表");
     							seg.write((byte) 4);
     							seg.writeShort((short) count);
     							for (int i = 0; i < showGrids.length; i++) {
     								if(showGrids[i] != null){
     									item = (IEquipment) showGrids[i].item;
     									seg.writeInt(item.getId());
     									seg.writeString(item.getName());
     									seg.writeInt(Utils.CLR_EQUIP[item.getQuality()]);
     								}
     							}
     							seg.write((byte) 1);
     							seg.writeString("精炼摘除");
     							seg.writeString("unhenceequip 4");   
     							connectService.writeTo(seg, player.getId());
     						}
     					}
     					//sendMessage(player.getId(),"精炼非周年装");
     				}else if(4 == id){//精炼非周年装 ----星星大挪移用 2次选中装备
     					Thread.sleep(100);
     					int instanceid = Integer.parseInt(command.getParam(1));
     					boolean findSucess= false; //检索所选中的装备是否成功；
     					grid  = new Grid();
     					for(int i=0; i<grids.length ;i++){
     						grid =  grids[i];
     						item = (IEquipment) grid.item;
     						if(instanceid == item.getId()){
     							findSucess = true;
     							break;
     						}
     					}
     					if(findSucess){
     						int itemid  = item.getItemId(); 
     						IEquipment item_old = item;
     						for(int i =0; i<grids.length; i++){
     							grid =  grids[i];
     							item = (IEquipment) grid.item;
     							//搜索同等级同部位的装备
     							if((item_old.getLevel() == item.getLevel()) 
     									&&  (item_old.getPart() == item.getPart())
     									&&  (item_old.getId() != item.getId())
     									&& (item.canEnhance())){
     								showGrids[count] = grid;
     								count++;
     							}
     						}
     						if(count <= 0){
     							sendMessage(player.getId(),"你背包里没有与"+item_old.getName()+"装备，同等级同部位的装备。");
     						}else if(count <= grids.length ){ //找出了精炼周年装,发放脚本
     							if(player.getClientDataVersion() > 0){
     								UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL, command.getSerial(), command.getSessionId());
     								seg.writeShort(ClientConstants.EXTEND_PROTOCL_GENERLIST);
     								seg.writeShort((short) 10269);
     								seg.write((byte) 6);
     								seg.writeString(item_old.getName()+" 可转移的装备");
     								seg.writeShort((short) count);
     								for (int i = 0; i < showGrids.length; i++) {
     									if(showGrids[i] != null){
     										item = (IEquipment) showGrids[i].item;
     										seg.write(item.getType());
     										seg.write(item.toClientBytesWithLevel(player.getLevel()));  
     									}
     								}
     								seg.write((byte) 1);
     								seg.writeString("精炼转移");
     								seg.writeString("changeequenhance 1 " + item_old.getId() + " ");   
     								connectService.writeTo(seg, player.getId());
     							}else{
     								UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
     								seg.writeShort((short) 10269);
     								seg.writeString(item_old.getName()+" 可转移的装备");
     								seg.write((byte) 4);
     								seg.writeShort((short) count);
     								for (int i = 0; i < showGrids.length; i++) {
     									if(showGrids[i] != null){
     										item = (IEquipment) showGrids[i].item;
     										seg.writeInt(item.getId());
     										seg.writeString(item.getName());
     										seg.writeInt(Utils.CLR_EQUIP[item.getQuality()]);
     									}
     								}
     								seg.write((byte) 1);
     								seg.writeString("精炼转移");
     								seg.writeString("changeequenhance 1 " + item_old.getId() + " ");   
     								try{
     									//TODO must delete by sky
     									Thread.sleep(100);
     								}catch(Exception e){
     								}
     								connectService.writeTo(seg, player.getId());
     							}
     						}
     					}else{
     						sendMessage(player.getId(),"未找到选中装备，请重新选择");
     					}
     				}else{
     					sendMessage(player.getId(),"系统错误，请重新选择");
     				}
     			}
     		}
     	}
     }

     class ChangeEquEnhancesProcessor implements CommandProcessor{
       	public void process(WorldPlayer player, Command command) throws Exception {
       		if (player != null) {
       			synchronized (player) {
	       			int type = Integer.parseInt(command.getParam(0));
	       			
	       			if (type == -1){
	   					//初期
	   					if (player.getMoeny() < 50000){
	   						sendMessage(player.getId(),"要进行装备精炼转移，需要消耗50,000J币，详情请查看《装备星移必读》");
	   					}else{
	   						if (!player.hasItem(200530)){
	   							sendMessage(player.getId(),"要进行装备精炼转移，需要消耗“移星水晶”1个，详情请查看《装备星移必读》");
	   						}else{
	   							byte[] bytes = stageService.getTaskBytes((short) 31002,
	   									new String[] {"请在背包中选中精炼星级要被摘除的装备。\n1.继续\n2.取消",
	   							"unhenceequip 3"});
	   							UWAPSegment seg = new UWAPSegment(ClientConstants.
	   									GET_FILE_OK, command.getSerial(),
	   									command.getSessionId());
	   							seg.writeShort((short) 31002);
	   							seg.writeShort((short) 2);
	   							seg.write(bytes);
	   							write(seg);
	   						}
	   					}
	       				
	       			}else if (type == 1){
	       				
	   					//星星大挪移最后处理阶段.
	   					int oldEQUinstanceid = Integer.parseInt(command.getParam(1));
	   					int newEQUinstanceid = Integer.parseInt(command.getParam(2));
	   					Grid[] grids = player.getEquipments(); 
	   					IEquipment item = null;
	   					IEquipment olditem = null;
	   					IEquipment newitem = null;
	   					Grid grid ;
	   					for(int i=0; i<grids.length ;i++){
	   						grid =  grids[i];
	   						item = (IEquipment) grid.item;
	   						if(oldEQUinstanceid == item.getId()){
	   							olditem = item;
	   						}
	   						if(newEQUinstanceid == item.getId()){
	   							newitem = item;
	   						}
	   					}
	   					if ((olditem == null) || (olditem == null)){
	   						sendMessage(player.getId(),"未找到选中装备，请重新选择");
	   					}else{
	   						//精炼保留
	   						List<Enhance> enhances_old  = olditem.getEnhances();
	   						List<Enhance> enhances_new  = newitem.getEnhances();
	   						if (enhances_old.size() < enhances_new.size()){
	   							//由于被替换星级比替换后高，需要2次确认
	   							byte[] bytes = stageService.getTaskBytes((short) 31002,
	   									new String[] {"您的"+ newitem.getName() +"比"+ olditem.getName() +"的星级还要高，确定要转移吗？\n1.我要转移\n2.没准备好",
	   									"changeequenhance 2 " + olditem.getId() + " " + newitem.getId()});
	   							UWAPSegment seg = new UWAPSegment(ClientConstants.
	   									GET_FILE_OK,
	   									command.getSerial(),
	   									command.getSessionId());
	   							seg.writeShort((short) 31002);
	   							seg.writeShort((short) 2);
	   							seg.write(bytes);
	   							write(seg);
	   						}else{
	   							log.info("ID["+player.getId()+"] ChangeEquEnhances EquOldId["+olditem.getItemId()+ "] EqunewId["+newitem.getItemId()+ "] try");
	   							String enhanceold = "";
	   							String enhancenew = "";
	   							
	   							for(int i= 0; i < enhances_new.size(); i++){
	   								Enhance enhance = enhances_new.get(i);
	   								enhancenew = enhancenew + " " + enhance.getProperty();
	   							}
	   							Changed changed = new Changed();
	   							changed.addEquipment(olditem,-1);
	   							changed.addEquipment(newitem,-1);
	   							sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 73);
	   							newitem.unEnhanceAll();
	   							Changed newChanged = new Changed();
	   							for(int i= 0; i < enhances_old.size(); i++){
	   								Enhance enhance = enhances_old.get(i);
	   								newitem.enhance(enhance);
	   								enhanceold = enhanceold + " " + enhance.getProperty();
	   							}
	   							if (newitem.getTimes() >= 3) {
	   								newitem.setBinded(true);
	   							}
	   							olditem.unEnhanceAll();
	   							newChanged.addEquipment(olditem,1);
	   							newChanged.addEquipment(newitem,1);
	   							
	   							player.completeRemoveItem(200530,1,newChanged);
	   							player.decMoney(50000, newChanged);
	   							sendGetItem(newChanged, command.getSerial(), command.getSessionId(), (byte) 73);
	   							log.info("ID["+player.getId()+"] ChangeEquEnhances EquOld["+enhanceold+ "] Equnew["+enhancenew+"] Sucess");
	   						}
	   						
	   					}
	       			}else if (type == 2){//由于被替换星级比替换后高，需要2次确认
	   					int oldEQUinstanceid = Integer.parseInt(command.getParam(1));
	   					int newEQUinstanceid = Integer.parseInt(command.getParam(2));
	   					Grid[] grids = player.getEquipments(); 
	   					IEquipment item = null;
	   					IEquipment olditem = null;
	   					IEquipment newitem = null;
	   					Grid grid ;
	   					for(int i=0; i<grids.length ;i++){
	   						grid =  grids[i];
	   						item = (IEquipment) grid.item;
	   						if(oldEQUinstanceid == item.getId()){
	   							olditem = item;
	   						}
	   						if(newEQUinstanceid == item.getId()){
	   							newitem = item;
	   						}
	   					}
	   					
	   					//精炼保留
	   					List<Enhance> enhances_old  = olditem.getEnhances();
	   					List<Enhance> enhances_new  = newitem.getEnhances();
	   					log.info("ID["+player.getId()+"] ChangeEquEnhances EquOldId["+olditem.getItemId()+ "] EqunewId["+newitem.getItemId()+ "] try");
	   					String enhanceold = "";
	   					String enhancenew = "";
	   					
	   					for(int i= 0; i < enhances_new.size(); i++){
	   						Enhance enhance = enhances_new.get(i);
	   						enhancenew = enhancenew + " " + enhance.getProperty();
	   					}
	   					Changed changed = new Changed();
	   					changed.addEquipment(olditem,-1);
	   					changed.addEquipment(newitem,-1);
	   					sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 73);
	   					newitem.unEnhanceAll();
	   					Changed newChanged = new Changed();
	   					for(int i= 0; i < enhances_old.size(); i++){
	   						Enhance enhance = enhances_old.get(i);
	   						newitem.enhance(enhance);
	   						enhanceold = enhanceold + " " + enhance.getProperty();
	   					}
	   					
	   					if (newitem.getTimes() >= 3) {
	   						newitem.setBinded(true);
	   					}
	   					olditem.unEnhanceAll();
	   					newChanged.addEquipment(olditem,1);
	   					newChanged.addEquipment(newitem,1);
	   					
	   					player.completeRemoveItem(200530,1,newChanged);
	   					player.decMoney(50000, newChanged);
	   					sendGetItem(newChanged, command.getSerial(), command.getSessionId(), (byte) 73);
	   					log.info("ID["+player.getId()+"] ChangeEquEnhances EquOld["+enhanceold+ "] Equnew["+enhancenew+"] Sucess2");
	       			}
	       		}
       		}
       	}
     }
     class    unhenceYearEquipProcessor implements CommandProcessor{
      	public void process(WorldPlayer player, Command command) throws Exception {
      		if (player != null) {
      			synchronized (player) {
      				//二次确认的标志
      				int flag = Integer.parseInt(command.getParam(0));
      				int id = Integer.parseInt(command.getParam(1));
      				Grid[] grids = player.getEquipments(); 
      				boolean findSucess= false; //周年装是否成功；
      				Grid grid  = new Grid();
      				
      				IEquipment item = null;
      				//log.info("ID["+player.getId()+"]equId["+id+"]unhenceYearEquip Try");
      				//mengjie add 精炼精华与精炼石数量相同
      				for(int i=0; i<grids.length ;i++){
      					grid =  grids[i];
      					item = (IEquipment) grid.item;
      					if(id == item.getId()){
      						findSucess = true;
      						break;
      					}
      				}
      				if(findSucess){
      					if(flag == 1){//确认
      						String msg =item.getName() + "分解后将会消失。你要继续么？\n1.分解\n2.暂不分解";
      						byte[] bytes = stageService.getTaskBytes((short) 31040,
      								new String[] {msg, "unhanceYearEquip 2 " + id,"随时为你服务！"});
      						UWAPSegment seg = new UWAPSegment(ClientConstants.
      								GET_FILE_OK);
      						seg.writeShort((short) 31025);
      						seg.writeShort((short) 2);
      						seg.write(bytes);
      						connectService.writeTo(seg, player.getId());
      					}else if(flag == 2){
      						int enhanceYearEquipCount =0; //判断精炼星数；
      						int itemid  = item.getItemId();  //二次判断物品
      						//Map<Integer, AnniversaryEnhance> unhenceYearEquipMap = AnniversaryEnhance.getMapUnhenceYearEquip();
      						AnniversaryEnhance uEnhance = AnniversaryEnhance.getUnhenceYearEquip(itemid);
      						if(uEnhance != null){
      							enhanceYearEquipCount = uEnhance.getCount();
      						}else {
      							sendMessage(player.getId(),"未找到装备，请确认背包内含有次装备再来");
      							return ;
      						}
      						log.info("ID["+player.getId()+"]equId["+id+"]itemsid["+itemid+"],count["+enhanceYearEquipCount+"]unhenceYearEquip Try");
      						if(enhanceYearEquipCount >= 1 && enhanceYearEquipCount <= 9){//获得正确id和正确精炼数
      							if (player.getMoeny() < enhanceYearEquipCount * 1000){
      								sendMessage(player.getId(),"您没有那么多钱来分解！");
      								return ;
      							}else{
      								Changed changed = new Changed();
      								player.decMoney(enhanceYearEquipCount * 1000, changed);
      								if(player.completeRemoveItem(itemid,id,changed)!=null){//物品扣成功了
      									int mailcount = 0;
      									IItem di = Items.getTemplate(200233).newInstance();
      									for(int k = 0;k<enhanceYearEquipCount;k++){//循环加明珠
      										if(player.completeAddItem(di,1,changed, player.getClientDataVersion())==null){
      											mailcount++;
      										}
      									}
      									if (mailcount > 0){
      										byte[] att = ItemUtils.item2dbAttachment(di, mailcount);
      										mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
      												di.getName() + "*" + mailcount, "", att, 0, true);
      										sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到你的邮箱!");
      									}
      									sendMessage(player.getId(),"装备"+item.getName()+"已分解！");
      									sendGetItem(changed, command.getSerial(), command.getSessionId(),
      											(byte) 17);
      									log.info("ID["+player.getId()+"]equId["+id+"]itemsid["+itemid+"]unhenceYearEquip Ok");
      								}else{
      									sendMessage(player.getId(),"分解失败。请重新选择分解");
      								}
      							}      				
      						}else{
      							sendMessage(player.getId(),"精炼次数不正确 ，请确认该物品精炼过");
      							return ;
      						}
      					}
      				}else{
      					sendMessage(player.getId(),"您选择的装备不存在，请确认背包内含有此装备再来");
      				}
      			}
      		}
      	}
    }

     class GiveOnlyRewordProcessor implements CommandProcessor{
        	public void process(WorldPlayer player, Command command) throws Exception {
        		//按照礼品发放搜寻周年装，非周年装，宠物
        		ConcurrentHashMap<Integer, OnlyGiftGroup> onlyGiftGroups= OnlyGiftGroups.getAllOnlyGiftGroup();
        		//遍历map
        		if(onlyGiftGroups == null && onlyGiftGroups.size() == 0){
        			sendMessage(player.getId(),"没有可以兑换的奖励");
        			return;
        		}
        		UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                seg.writeShort((short) 10246);
                seg.writeString("普通星级装备列表");
                seg.write((byte) 4);
                seg.writeShort((short) 3);
        	/*	for(Map.Entry<Integer,OnlyGiftGroup> onlyGiftGroupValue: onlyGiftGroups.entrySet()){ 
        			//输入所有可以兑换的奖励，因为如果在这里读取数据库的话会很耗时间。发送的列表中根据玩家选中的奖励可以减少对数据库的操作
        			OnlyGiftGroup onlyGiftGroup = onlyGiftGroupValue.getValue();*/
                for(int i = 0; i < 3; i++){
        			seg.writeInt(300000+i);
        			OnlyGiftGroup onlyGiftGroup = onlyGiftGroups.get(300000+i);
        			seg.writeString(onlyGiftGroup.getMessage_title());
        			seg.writeInt(Utils.CLR_WHITE);
        		/*	//sendMessage(player.getId(),"日你大爷");
        		 } */
                }
        		 seg.write((byte) 1);
                 seg.writeString("领取奖品");
                 seg.writeString("givereword");   
                 connectService.writeTo(seg, player.getId());
        		}
        	}
     
     
     class GiveReworldProcessor implements CommandProcessor{
     	public void process(WorldPlayer player, Command command) throws Exception {
     		int onlyGiftGroupId = Integer.parseInt(command.getParam(0));
     		if(onlyGiftGroupId < 300000 || onlyGiftGroupId > 300002){
     			sendMessage(player.getId(),"无法为你提供奖品，很抱歉");
    			return ;
     		}
     		OnlyGiftGroup onlyGiftGroup = OnlyGiftGroups.getOnlyGiftGroup(onlyGiftGroupId);
        	if(onlyGiftGroup != null){
        		if(player.getLevel() < onlyGiftGroup.getBeginLevel() || player.getLevel() > onlyGiftGroup.getEndLevel()){
        			sendMessage(player.getId(),"你的等级不符合赠送要求,请满足了奖励条件再来吧");
        			return ;
        		}
        		Vector<OnlyGiftDefine> onlyGiftDefine = onlyGiftGroup.getGifts();
        		if(onlyGiftDefine == null && onlyGiftDefine.size() == 0){
        			sendMessage(player.getId(),onlyGiftGroup.getMessage_error());
        		}else{//获得可以领取的必须物品和奖励物品成功
        			Vector<OnlyGiftDefine> onlyCanUsedGiftDefine = new Vector<OnlyGiftDefine>();//可以分配的多个兑奖规则
        			int index = -1;//最后 满足条件的OnlyGiftDefine的id号
        			boolean findFlag = false;
        			
        			for(int i = 0; i < onlyGiftDefine.size(); i++){//
        				OnlyGiftDefine onlyNeedGiftDefine = onlyGiftDefine.get(i);
        				Vector<OnlyGiftNeedItem> onlyGiftNeedItemVector = onlyNeedGiftDefine.getAllNeedItem();
        				//获取兑换的必须物品
        				if(onlyGiftNeedItemVector == null && onlyGiftNeedItemVector.size() == 0){
        					sendMessage(player.getId(),"无法找到奖励的必须物品，请稍候再试");
    						break;
        				}
        				int count = 0;
        				for(int k =0; k < onlyGiftNeedItemVector.size(); k++){
        					OnlyGiftNeedItem onlyGiftNeedItem = (OnlyGiftNeedItem)onlyGiftNeedItemVector.get(k);
        					if(0 == onlyGiftNeedItem.getId()){//非周年装
        						if(Utils.findUnyearEquip(player,onlyGiftNeedItem.getEnhanceCount())){//找到非周年装
        							count++;
        							//sendMessage(player.getId(),"已经找到兑换的非周年装");
        						}
        					}else if(1 == onlyGiftNeedItem.getId()){//周年装
        						if(Utils.findYearEquip(player, onlyGiftNeedItem.getEnhanceCount(), onlyGiftNeedItem.getType(), onlyGiftNeedItem.getYeartype())){//找到链或者戒指
        							count++;
        						}
        					}else if(2 == onlyGiftNeedItem.getId()){//宠物
        						/*if(Utils.findEnhancePetLimited(player,onlyGiftNeedItem.getEnhanceCount())){//找到精炼宠物{
        							count ++;
        							
        						}*/
        						
        						//sendMessage(player.getId(),"兑换宠物");
        					}else{
        						sendMessage(player.getId(),onlyGiftGroup.getMessage_error());
        						break;
        					}
        				}
        				if(count == onlyGiftNeedItemVector.size()){
							index = i;
							findFlag = true;
							break;
						}
        			}
        			if(findFlag){//找到兑奖物品
        				//判断是否领过
        				GiftData giftData = giftService.getPlayerOnlyGift(onlyGiftGroup.getId(), player); 
        				if(giftData != null ){
        					Gift gift = giftData.getGift();
        					OnlyGiftDefine onlyNeedGiftDefine = onlyGiftDefine.get(index);
        					if(gift.getCount() >= onlyGiftGroup.getMaxCount()){
        						sendMessage(player.getId(),OnlyGiftGroup.getReplaceMessage(onlyGiftGroup.getMessage_maxcount(), gift, onlyNeedGiftDefine,onlyGiftGroup, player));
        					}
        				//未领过奖励物品
	        				else {
	        					TemplateGrid[] onlyGiftGiveItems = onlyNeedGiftDefine.getAllGiveItem();
		            			if(onlyGiftGiveItems == null && onlyGiftGiveItems.length == 0){
		            				sendMessage(player.getId(),"无法找到奖励物品，请稍候再试");
		            				return;
		            			}else{//开始发放礼品
			            			if (player.isOver(onlyGiftGiveItems)) {
			            				sendMessage(player.getId(),OnlyGiftGroup.getReplaceMessage(onlyGiftGroup.getMessage_bag(), gift, onlyNeedGiftDefine,onlyGiftGroup, player));
			                            return ;
			                        }
			            			Changed changed = new Changed();
			            			player.addItems(onlyGiftGiveItems, changed, player.getClientDataVersion());
			            			sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);
			            			//数据库添加天赋
			            			//GiftData giftData = giftService.getPlayerOnlyGift(onlyGiftGroup.getId(), player);
			            			//if(giftData != null){
			            					//OnlyGiftGroup group = giftData.getOnlyGiftGroup();
			                        if(gift.getCount() == 0){//第一次领取
			                        	gift.setCount(onlyGiftGroup.getMaxCount());
			                            gift.setRcount(1);
			                            giftService.savePlayerGift(gift);
			                        }
			                                //GiftDefine giftDefine = group.getAvailableGift(player.getLevel());
			            			sendMessage(player.getId(),OnlyGiftGroup.getReplaceMessage(onlyGiftGroup.getMessage_give(), gift, onlyNeedGiftDefine,onlyGiftGroup, player));
			            			}
		            				log.info("ID["+player.getId()+"]get onlyGiftGroup["+onlyGiftGroup.getId()+"]giftId["+gift.getId()+"] Ok");
		            			}
	        					//sendMessage(player.getId(),"回馈礼品哦");
	        				}
        			}else{
        				sendMessage(player.getId(),"未找到奖励必须物品");
        			}
        		}
        	}else{
        		sendMessage(player.getId(),"没有此奖励");
        	}
     	}

     }
     class SelectClearProcessor implements CommandProcessor{
      	public void process(WorldPlayer player, Command command) throws Exception {
      		int clearId = Integer.parseInt(command.getParam(0));
      		if(1 == clearId){
      			Grid[] grids = player.getEquipments(); 
    			int  count  = 0; //扫描符合条件的装备数量
    			IEquipment item;
    			Grid grid ;
    			Grid[] showGrids = new  Grid[grids.length];//用于最后展示列表用的
   				for(int i =0; i<grids.length; i++){
   					grid =  grids[i];
   					item = (IEquipment) grid.item;
   					if(Utils.CLR_EQUIP[item.getQuality()] == Utils.CLR_WHITE || Utils.CLR_EQUIP[item.getQuality()] == Utils.CLR_GREEN){
   						showGrids[count] = grid;
   						count++;
   					}
   				}
        		if(count <= 0){
        			sendMessage(player.getId(),"你背包里面没有白色和绿色的装备，请确认后再来");
        		}else{//发放背包装备类表
        			if(player.getClientDataVersion() > 0){
        				UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL, command.getSerial(), command.getSessionId());
	            		seg.writeShort(ClientConstants.EXTEND_PROTOCL_GENERLIST);
	            		seg.writeShort((short) 10235);
	            		seg.write((byte) 5);
	                    seg.writeString("白色和绿色装备列表");
	                    seg.writeShort((short) count);
	                    for (int i = 0; i < showGrids.length; i++) {
	                    	if(showGrids[i] != null){
	                    		item = (IEquipment) showGrids[i].item;
	                    	  	seg.write(item.getType());
				            	seg.write(item.toClientBytesWithLevel(player.getLevel()));
	                    	}
	                    }
	                    seg.write((byte) 1);
	                    seg.writeString("售出");
	                    seg.writeString("sellattachementequip 1");   
	                    connectService.writeTo(seg, player.getId());
        			}else{
	        			UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
	                    seg.writeShort((short) 10235);
	                    seg.writeString("白色和绿色装备列表");
	                    seg.write((byte) 5);
	                    seg.writeShort((short) count);
	                    for (int i = 0; i < showGrids.length; i++) {
	                    	if(showGrids[i] != null){
	                    		item = (IEquipment) showGrids[i].item;
		                        seg.writeInt(item.getId());
		                        seg.writeString(item.getName());
		                        seg.writeInt(Utils.CLR_EQUIP[item.getQuality()]);
	                    	}
	                    }
	                    seg.write((byte) 1);
	                    seg.writeString("售出");
	                    seg.writeString("sellattachementequip 1");   
	                    connectService.writeTo(seg, player.getId());
        			}
        		}
      			//sendMessage(player.getId(),"清理背包");
      		}else if(2 == clearId){//清空邮件命令
      			int mailCount = mailService.getMailCount(player);
                //每次查十个邮件
      			if(mailCount == 0){
      				sendMessage(player.getId(),"你没有邮件，请确认后再来");
      				return ;
      			}
                int maxPageNo = mailCount/50 ;
                Mail mail = new Mail();
                Vector<Mail> attchmentList = new Vector<Mail>();
                //if(0 == attchmentList.size()){//判断是否已经清空过。。加速读取数据库。
	                for( int pageNo = 0; pageNo < (maxPageNo+1); pageNo++){
	                    MailList allMailList = mailService.getPageMail(player,(short) 50,pageNo,-1);
		                    for (int i = 0; i < allMailList.getList().size(); i++) {
		                  	  mail = (Mail) allMailList.getList().get(i);
		                  	  if (mail != null){//信件非空 
			                    	  //if (mail.getSourceId() == -1){//标示是系统来信
			                          	//查找附件物品
			                    		  byte[] item = mail.getAttachment();
			                    		  //if (item != null) {
			                    		  if(item.length > 0){//附件物品非空，提取附件
			                    			  int itemId = ItemUtils.getAttachementEquId(item);
			                    			  if(-1 != itemId){
			                    				  attchmentList.add(mail);
			                    			  }
			                    			 // }
			                             // }
			                    		  }
		                  	  }
	                    }
	                }
                //}
                
                //找到绿色和白色装备id,写系统邮件到通用列表
                if(attchmentList.size()==0){
                	sendMessage(player.getId(),"你的系统邮件附件里面没有白色和绿色装备，请确认后再来");
                }else{
                    UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                    seg.writeShort((short) 10244);
                    seg.writeString("清理装备邮件列表");
                    seg.write((byte) 6);
                    seg.writeShort((short)attchmentList.size());
                    for(int k =0; k< attchmentList.size(); k++){
                  	  mail = attchmentList.get(k);
                  	  seg.writeInt(mail.getId());
      				  seg.writeString("[系统]"+mail.getTitle());
      				  if(mail.getReaded()){
      					  seg.writeInt(Utils.CLR_RED);
      				  }else{
      					  seg.writeInt(Utils.CLR_GREEN);
      				  }
                    }
                    seg.write((byte) 1);
                    seg.writeString("卖掉附件并删除");
                    seg.writeString("sellattachementequip 2");
                    connectService.writeTo(seg, player.getId());
                }
      		}
      	}
      }
     class FollowEnemysPreocessor implements CommandProcessor{
      	public void process(WorldPlayer player, Command command) throws Exception {
      		int itemId = Integer.parseInt(command.getParam(0));
      		int enemyId = Integer.parseInt(command.getParam(1));
      		if(enemyId != -1){
      			WorldPlayer enemy = playerService.getWorldPlayer(enemyId);
      			if(enemy ==null){
      				sendMessage(player.getId(),"你的仇人已经下线了，无法进行追踪。");
      			}else{
      				GameMap map = enemy.getMap();
                    if (map == null) {
                        sendMessage(player.getId(), " 仇人位置信息错误");
                    } else {
                    	 if (map.getInstance() != null) {//副本家园等地区
                             sendMessage(player.getId(), " 仇人目前所在地区不允许追踪 ");
                         } else {
							boolean canTransfer = true;
							GameMap playerMap = player.getMap();
							if(playerMap != null){
								NoDoor door = NoDoor.getNoTransfer(playerMap.getMapId());
								if(door != null){
									sendMessage(player.getId(), door.getMessage());
									canTransfer = false;
								}
							}
							if(canTransfer){
								 NoDoor door = NoDoor.getNoDoor(map.getMapId());//非副本等限制地区
							     if (door != null) {
							         sendMessage(player.getId(), door.getMessage());
							     }else{//符合条件飞过去
							    	 Changed changed = new Changed();
							    	 if(player.completeRemoveItem(itemId, 1, changed)!=null){//物品扣成功，飞向敌人
							    		 sendGotoMap(player.getId(),
							                     map.getMapId(), (short) (enemy.getX() / map.getTileWidth()),
							                     (short) (enemy.getY() / map.getTileHeight()));
							    		 sendGetItem(changed, command.getSerial(), command.getSessionId(),
							                   (byte) 17);
							    		 log.info("ID["+player.getId()+"] followEnemy["+enemyId+"] at map"+ map.getMapId() + "at x[" + (short) (enemy.getX() / map.getTileWidth())
							    				 +"] at y["+ (short) (enemy.getY() / map.getTileHeight())+"]");
							    	 }else{//没有此物品
							    		 sendMessage(player.getId(), "你没有仇人录，不能跟踪敌人");
							    	 }
							     }
							}
                         }
                    }
      			}
      		}else{
      			sendMessage(player.getId(),"你选择的仇人不在线。无法传送 ");
      		}
      	}
     }
     /**
     * @author wpjiang
     *	用于ui的模拟展示
     */
    class ShowUIHelpPreocessor implements CommandProcessor {

 		public void process(WorldPlayer player, Command command)
 				throws Exception {
 			int  taskId= Integer.parseInt(command.getParam(0));
 			log.info("ID["+player.getId()+"] ShowUI["+taskId+ "]");
 			Client client = player.getClient();
 			if(client != null && client.getDataVersion() > 0){
	 			if(TaskHelpManager.taskHelpMap.containsKey((short)taskId)){
	 				TaskHelp taskHelp = (TaskHelp) TaskHelpManager.taskHelpMap.get((short)taskId);
	 				//下发停止监听
	 				if(player.getLevel() >= taskHelp.getHelpLevel()){
		 				UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
		          		seg.writeShort(ClientConstants.EXTEND_PROTOCOL_UIHELP);
		          		seg.writeString(taskHelp.getUiName());
		          		//seg.writeInt(taskHelp.getFastKey());
		          		Vector showKeyVector = taskHelp.getKeyVector();
		          		Vector showKeyMessageVector = taskHelp.getUiWaitMessage();
		          		Vector showKeyTimeVector = taskHelp.getKeyTimeVector();
		          		seg.writeInt(showKeyVector.size());
		          		for(int i = 0; i < showKeyVector.size(); i++){
		          			seg.writeInt((Integer) showKeyVector.get(i));
		          			seg.writeString((String)showKeyMessageVector.get(i));
		          			seg.writeInt((Integer)showKeyTimeVector.get(i));
		          		}
		          	    connectService.writeTo(seg, player.getId());
	 				}else{
	 					sendMessage(player.getId(), "你的等级小于" + taskHelp.getHelpLevel() + "不能演示此任务");
	 				}
	 			}else{
	 				sendMessage(player.getId(), "没有此演示任务");
	 			}
 			}else{
 				sendMessage(player.getId(), "请使用新版本客户端");
 			}
 		}
 	}
    
    /**
     * 
     * @author hchen
     * petversion >= 4 取消炼化功能
     */
    /*class PreEnhancePetProcessor implements CommandProcessor{
    	public void process(WorldPlayer player, Command command) throws Exception {
    		Pet[] pets=player.getPets();
    		if((pets == null) || (pets.length ==0)){
    			sendMessage("你还没有宠物。请有了宠物再来吧", command.getSerial(), command.getSessionId());
    		}else{
    			 UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                 seg.writeShort((short) 10233);
                 seg.writeString("宠物列表");
                 seg.write((byte) 3);
                 seg.writeShort((short) pets.length);
                 for (int i = 0; i < pets.length; i++) {
                     seg.writeInt(pets[i].getId());
                     String  tempNameString ;
                     tempNameString = pets[i].getName();
                     if(!(pets[i].getEnhanceName().equals("") && pets[i].getEnhanceName().length() == 0)){
                    	 tempNameString = tempNameString.concat(pets[i].getEnhanceName());
                     }
                	 tempNameString = tempNameString.concat(pets[i].getLevel()+"级");
                     seg.writeString(tempNameString);
                     seg.writeInt(Utils.CLR_WHITE);
                 }
                 seg.write((byte) 1);
                 seg.writeString("炼化");
                 seg.writeString("enhancePet");
                 
                 connectService.writeTo(seg, player.getId());
    		}
    	}
    }*/
   
	class ExchangeGroupPreocessor implements CommandProcessor {

		public void process(WorldPlayer player, Command command)
				throws Exception {
			// TODO Auto-generated method stub
			// type = 1 选择 group层某一个
			// type = 2选择背包中装备
			// type = 3如果group层的目标物品为多个才会显示列表
			int type = Integer.parseInt(command.getParam(0));
			ExchangeDefine exchangeDefine = new ExchangeDefine();
			if (type == 1){//选择 group层某一个
				int exchangeId = Integer.parseInt(command.getParam(1));
				ExchangeData exchangeData = exchangeDefine.getExchangeData(exchangeId);
				int exchangeGroupId = Integer.parseInt(command.getParam(2));
				ExchangeGroup exchangeGroup = exchangeData.getExchangeGroup(exchangeGroupId);
				
				if(player.getLevel() < exchangeGroup.getBeginlevel() || player.getLevel() > exchangeGroup.getEndlevel()){
					sendMessage(player.getId(), "你的等级需要在"+ exchangeGroup.getBeginlevel() + "到" +exchangeGroup.getEndlevel() + "之间");
					return;
				}
				if(!player.hasItem(exchangeGroup.getNeeditem(), exchangeGroup.getNeeditemcount())){
					IItem item = Items.getTemplate(exchangeGroup.getNeeditem()).newInstance();
					sendMessage(player.getId(), "兑换需要物品【"+item.getName()+"】："+exchangeGroup.getNeeditemcount()+"个。请您到"+Server.iMoneyStoreString+"购买。");
					return;
				}
				Vector<Grid> equsVector = new Vector<Grid>();//显示装备列表
				Grid[] equs = player.getEquipments();//角色背包中所有装备
				String equnames = "";
				for(Entry<Integer, Integer> entry :  exchangeGroup.getNeedEquMap().entrySet()){//group中所有装备遍历
					for (int i = 0; i < equs.length; i++) {//背包中遍历
						IEquipment iEquipment = (IEquipment) equs[i].item;
						if (entry.getKey() == iEquipment.getItemId()) {
							equsVector.add(equs[i]);
						}
					}
					IItem item = Items.getTemplate(entry.getKey()).newInstance();
					equnames = equnames + "\n"+item.getName();
				}
				if(equsVector.size() == 0){
					sendMessage(player.getId(), "兑换所需："+equnames+"中一件。请您获得后再来兑换。");
					return;
				}
				// 发送背包内列表让其选择
				// 为了让每个人都看见奖励则主要使用菜单命令去进行控制，不上传装备id
				if(player.getClientDataVersion() > 0){
					UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL, command.getSerial(), command.getSessionId());
            		seg.writeShort(ClientConstants.EXTEND_PROTOCL_GENERLIST);
					seg.writeShort((short) 10271);
					seg.write((byte) 6);
					seg.writeString("背包中可兑换的装备列表");
					seg.writeShort((short) equsVector.size());
					for (int i = 0; i < equsVector.size(); i++) {
						IItem item = equsVector.get(i).item;
						seg.write(item.getType());
		            	seg.write(item.toClientBytesWithLevel(player.getLevel()));
					}
					seg.write((byte) 1);
					seg.writeString("兑换");
					seg.writeString("ExchangeGroup 2 "+ exchangeId+ " "+ exchangeGroupId);
					connectService.writeTo(seg, player.getId());
				}else{
					UWAPSegment seg = new UWAPSegment(
							ClientConstants.GENERIC_LIST);
					seg.writeShort((short) 10271);
					seg.writeString("背包中可兑换的装备列表");
					seg.write((byte) 3);
					seg.writeShort((short) equsVector.size());

					for (int i = 0; i < equsVector.size(); i++) {
						seg.writeInt(equsVector.get(i).item.getId());
						seg.writeString(equsVector.get(i).item.getName());
						seg.writeInt(Utils.CLR_EQUIP[equsVector.get(i).item.getQuality()]);
					}
					seg.write((byte) 1);
					seg.writeString("兑换");
					seg.writeString("ExchangeGroup 2 "+ exchangeId+ " "+ exchangeGroupId);
					connectService.writeTo(seg, player.getId());
				}
			}else if (type == 2){//选择背包中装备
				int exchangeId = Integer.parseInt(command.getParam(1));
				ExchangeData exchangeData = exchangeDefine.getExchangeData(exchangeId);
				int exchangeGroupId = Integer.parseInt(command.getParam(2));
				ExchangeGroup exchangeGroup = exchangeData.getExchangeGroup(exchangeGroupId);
				if(player.getLevel() < exchangeGroup.getBeginlevel() || player.getLevel() > exchangeGroup.getEndlevel()){
					sendMessage(player.getId(), "你的等级需要在"+ exchangeGroup.getBeginlevel() + "到" +exchangeGroup.getEndlevel() + "之间");
					return;
				}
				if(!player.hasItem(exchangeGroup.getNeeditem(), exchangeGroup.getNeeditemcount())){
					IItem item = Items.getTemplate(exchangeGroup.getNeeditem()).newInstance();
					sendMessage(player.getId(), "兑换需要物品【"+item.getName()+"】："+exchangeGroup.getNeeditemcount()+"个");
					return;
				}
				int instanceId = Integer.parseInt(command.getParam(3));
				if (exchangeGroup.getGiveItemMap().size() > 1){
					//多个目标物品。需要进入第三层选择
					Vector<Grid> equsVector = new Vector<Grid>();//显示装备列表
					
					for(Entry<Integer, Integer> entry :  exchangeGroup.getNeedEquMap().entrySet()){//group中所有装备遍历
						IEquipment item = (IEquipment) Items.getTemplate(entry.getKey()).newInstance();
						equsVector.add((Grid) item);
					}
					if(player.getClientDataVersion() > 0){
						UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL, command.getSerial(), command.getSessionId());
	            		seg.writeShort(ClientConstants.EXTEND_PROTOCL_GENERLIST);
						seg.writeShort((short) 10271);
						seg.write((byte) 6);
						seg.writeString("请选择您想兑换的装备物品");
						seg.writeShort((short) equsVector.size());

						for (int i = 0; i < equsVector.size(); i++) {
							IItem item = equsVector.get(i).item;
							seg.write(item.getType());
			            	seg.write(item.toClientBytesWithLevel(player.getLevel()));
						}
						seg.write((byte) 1);
						seg.writeString("兑换");
						seg.writeString("ExchangeGroup 3 "+ exchangeId+ " "+ exchangeGroupId+" "+ instanceId);
						connectService.writeTo(seg, player.getId());
					}else{
						UWAPSegment seg = new UWAPSegment(
								ClientConstants.GENERIC_LIST);
						seg.writeShort((short) 10271);
						seg.writeString("请选择您想兑换的装备物品");
						seg.write((byte) 3);
						seg.writeShort((short) equsVector.size());

						for (int i = 0; i < equsVector.size(); i++) {
							seg.writeInt(equsVector.get(i).item.getId());
							seg.writeString(equsVector.get(i).item.getName());
							seg.writeInt(Utils.CLR_EQUIP[equsVector.get(i).item.getQuality()]);
						}
						seg.write((byte) 1);
						seg.writeString("兑换");
						seg.writeString("ExchangeGroup 3 "+ exchangeId+ " "+ exchangeGroupId+" "+ instanceId);
						connectService.writeTo(seg, player.getId());
					}
				}else{//当兑换的目标物品数量 = 1时，直接兑换
					int giveItemId = 0;
					int givecount = 0;
					for(Entry<Integer, Integer> entry :  exchangeGroup.getGiveItemMap().entrySet()){//group中所有装备遍历
						giveItemId = entry.getKey();
						givecount = entry.getValue();
					}
					if (player.isFull()) {
						sendMessage(player.getId(), "包裹满了，请稍后再来吧");
						return;
					}
					IEquipment equ = (IEquipment) player.getEquipmentByInstanceid(instanceId).item;
					int exchangetype = exchangeGroup.getType();
					if (exchangetype == 1){//带有星级的装备不可兑换
						
					}else if (exchangetype == 2){//带有宝石的装备不可兑换
						byte[] roleInfo = equ.getDiamondMosiacRoleInfo();
						for(int i = 0; i < roleInfo.length; i++){
							if(roleInfo[i] > IEquipment.CURRENT_EQU_CANDIAMOND){
								sendMessage(player.getId(), "装备上已经打宝石了,请摘下后再兑换");
								return;
							}
						}
					}
					
					log.info("ID["+player.getId()+"] exchangegroup oldequId[" + equ.getItemId()+ "] newItemId["+giveItemId+"] up start");
					Changed changed = new Changed();
					if(player.completeRemoveItem(exchangeGroup.getNeeditem(), exchangeGroup.getNeeditemcount(), changed) != null){
						log.info("ID["+player.getId()+"] exchangegroup oldequId[" + equ.getItemId()+ "] newItemId["+giveItemId+"] " +
								"delete itemid["+exchangeGroup.getNeeditem()+"] count ["+exchangeGroup.getNeeditemcount()+"] OK");
						if (player.completeRemoveItem(equ.getItemId(),instanceId, changed) != null) {
							log.info("ID["+player.getId()+"] exchangegroup oldequId[" + equ.getItemId()+ "] newItemId["+giveItemId+"] " +
									"delete Equid["+equ.getItemId()+"] instanceId ["+instanceId+"] OK");
							IItem item = Items.getTemplate(giveItemId).newInstance();
							if (player.completeAddItem(item, givecount, changed, player.getClientDataVersion()) == null){
								byte[] att = ItemUtils.item2dbAttachment(item, givecount);
								mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
										item.getName() + "*" + givecount, "", att, 0, true);
								sendMessage(player.getId(), "你的背包满了，已经把您兑换的物品发送到您的邮箱!");
							}
							
							sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 73);
							log.info("ID["+player.getId()+"] exchangegroup oldequId[" + equ.getItemId()+ "] newItemId["+giveItemId+"]OK");
						}	
					}
					
					
				}
				
			}else if (type == 3){//如果group层的目标物品为多个才会显示列表
				
				int exchangeId = Integer.parseInt(command.getParam(1));
				ExchangeData exchangeData = exchangeDefine.getExchangeData(exchangeId);
				int exchangeGroupId = Integer.parseInt(command.getParam(2));
				ExchangeGroup exchangeGroup = exchangeData.getExchangeGroup(exchangeGroupId);
				if(player.getLevel() < exchangeGroup.getBeginlevel() || player.getLevel() > exchangeGroup.getEndlevel()){
					sendMessage(player.getId(), "你的等级需要在"+ exchangeGroup.getBeginlevel() + "到" +exchangeGroup.getEndlevel() + "之间");
					return;
				}
				int instanceId = Integer.parseInt(command.getParam(3));
				
				int giftItemId = Integer.parseInt(command.getParam(4));
				int giftCount = 1;
				
				if (player.isFull()) {
					sendMessage(player.getId(), "包裹满了，请稍后再来吧");
					return;
				}
				IEquipment equ = (IEquipment) player.getEquipmentByInstanceid(instanceId).item;
				
				byte[] roleInfo = equ.getDiamondMosiacRoleInfo();
				for(int i = 0; i < roleInfo.length; i++){
					if(roleInfo[i] > IEquipment.CURRENT_EQU_CANDIAMOND){
						sendMessage(player.getId(), "装备上已经打宝石了,请摘下后再兑换");
						return;
					}
				}
				
				log.info("ID["+player.getId()+"] exchangegroup oldequId[" + equ.getItemId()+ "] newItemId["+giftItemId+"] up start2");
				Changed changed = new Changed();
				if(player.completeRemoveItem(exchangeGroup.getNeeditem(), exchangeGroup.getNeeditemcount(), changed) != null){
					log.info("ID["+player.getId()+"] exchangegroup oldequId[" + equ.getItemId()+ "] newItemId["+giftItemId+"] " +
							"delete itemid["+exchangeGroup.getNeeditem()+"] count ["+exchangeGroup.getNeeditemcount()+"] OK2");
					if (player.completeRemoveItem(equ.getItemId(),instanceId, changed) != null) {
						log.info("ID["+player.getId()+"] exchangegroup oldequId[" + equ.getItemId()+ "] newItemId["+giftItemId+"] " +
								"delete Equid["+equ.getItemId()+"] instanceId ["+instanceId+"] OK2");
						IItem item = Items.getTemplate(giftItemId).newInstance();
						if (player.completeAddItem(item, giftCount, changed, player.getClientDataVersion()) == null){
							byte[] att = ItemUtils.item2dbAttachment(item, giftCount);
							mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
									item.getName() + "*" + giftCount, "", att, 0, true);
							sendMessage(player.getId(), "你的背包满了，已经把您兑换的物品发送到您的邮箱!");
						}
						sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 73);
						log.info("ID["+player.getId()+"] exchangegroup oldequId[" + equ.getItemId()+ "] newItemId["+giftItemId+"]OK2");
					}	
				}
			}
		}

	}
	
	
	/**
	 * @author 战斗答案回答
	 *
	 */
	class BattleQuestionPreocessor implements CommandProcessor{

		public void process(WorldPlayer player, Command command)
				throws Exception {
			// TODO Auto-generated method stub
			int id  = Integer.parseInt(command.getParam(0));
			//int exp = Integer.parseInt(command.getParam(1));
			//int petExp = Integer.parseInt(command.getParam(2));
			int answer = Integer.parseInt(command.getParam(1));
			
			RandomQuestion randomQuestion = player.getRandomQuestion(id);
			if(randomQuestion != null){
				int questtionAnswer = randomQuestion.getAnswer();
				if(questtionAnswer != answer){
					log.info("ID[" + player.getId() +
		                    "] RANDOMQUESTION ANSWER + ID[" + id + "]MGCOUNT[" + Server.player_questions_time.get(player.getId()) + "] FAIL");
					if(player.getRandomQuestionIndex(id) >= player.battleRandomQuestionSize){
						//打错2次t下线
						playerService.addForbiden(player.getId(), 60);
    			        connectService.kick(player.getId());
    			        //player.randomQuestionClear();
    			        player.randmoQuestionClear();
    			        Server.player_questions_time.put(player.getId(), 100);
    			        log.info("FORBID ID[" + player.getId() +
    	                        "]ERROR BATTLE REPEAT ID[" + id + "]MGCOUNT[" + Server.player_questions_time.get(player.getId()) + "]");
						
					}else{//重新下发选项
						Random rand = new Random();
                		int targetQuestId =  player.getRandomQuestionLegal();
            			RandomQuestion newRandomQuestion = RandomQuestionManager.makeRandomQuestion(targetQuestId);
            			String showString;
            			if(randomQuestion != null){
            				showString = newRandomQuestion.getRandomQuestionShow();
            			}else{
            				log.info("ID[" + player.getId() +
        	                        "]ERROR RANDOMQUESTION ANSWER ID[" + id + "]MGCOUNT[" + Server.player_questions_time.get(player.getId()) + "] NO GAIN");
            				return;
            			}
            			newRandomQuestion.setExp(randomQuestion.getExp());
            			/*byte[] bytes = stageService.getTaskBytes((short) 31010, new String[] {
                                "2", "1", showString,
                                "battleQuestion " + targetQuestId + " " + exp + " " + petExp+ " 0", "battleQuestion "+ targetQuestId + " " + exp + " " + petExp + " 1"});*/
            			byte[] bytes = stageService.getTaskBytes((short) 31010, new String[] {
                                "2", "1", showString,
                                "battleQuestion " + targetQuestId /*+ " " + exp*/ + " 0", "battleQuestion "+ targetQuestId /*+ " " + exp*/  + " 1"});
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                GET_FILE_OK, command.getSerial(), command.getSessionId());
                        seg.writeShort((short) 31010);
                        seg.writeShort((short) 2);
                        seg.write(bytes);
                        write(seg);
                        
                        player.addRandQuestion(newRandomQuestion);
            			log.info("ID[" + player.getId() +
    	                        "] RANDOMQUESTION ANSWER REPEAT ID" + id + "]MGCOUNT[" + Server.player_questions_time.get(player.getId()) + "]");
						
					}
					
				}else{//清空玩家打怪数据
					Server.player_questions_time.put(player.getId(), 1);
					//player.randomQuestionClear();
					player.randmoQuestionClear();
					Changed changed = new Changed();
					player.addExp(randomQuestion.getExp(), changed);
					int level_tmp = player.getLevel();
					sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 30);
					if (level_tmp<player.getLevel()){
                    	//推荐人通用函数
                    	playerService.recommendBalance(player, "Question");
                    	//尝试加到师傅的列表中
                    	playerService.addMasterPlayer(player, changed);
                    }
					log.info("ID[" + player.getId() +
                    "] RANDOMQUESTION ANSWER + MGCOUNT[" + Server.player_questions_time.get(player.getId()) + "] DELETE MGCOUNT");
				}
			}else{
				log.info("ID[" + player.getId() +
                        "] NO RANDOMQUESTION ERROR");
				
			}
		}
	
		
	}
     /**
     * @author wpjiang
     *	星装属性精炼替换
     */
    class RequestInsteadEnhancePreocessor implements CommandProcessor{
    	private Random rnd = new Random();
       	public void process(WorldPlayer player, Command command) throws Exception {
       		if (player != null) {
       			synchronized (player) {
       				int serial = Integer.parseInt(command.getParam(0));
       				if(serial == 0){//选择星级装备列表
       					Grid[] grids = player.getEquipments(); 
       					if(grids.length == 0 ){
       						sendMessage(player.getId(),"你背包内没有装备稍候再来吧 ");
       					}else{
       						Vector<IEquipment> showGrids = new Vector<IEquipment>();
       						for(int i = 0; i < grids.length ; i++){
       							IEquipment item = (IEquipment) grids[i].item;
       							if(0 < item.getEnhances().size()){
       								showGrids.add(item);
       							}
       						}
       						if(player.getClientDataVersion() > 0){
       							UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL, command.getSerial(), command.getSessionId());
       							seg.writeShort(ClientConstants.EXTEND_PROTOCL_GENERLIST);
       							seg.writeShort((short) 10248);
       							seg.write((byte) 6);
       							seg.writeString("星装列表");
       							seg.writeShort((short) (showGrids.size()));
       							for(int k =0; k<showGrids.size(); k++){
       								IEquipment iequipment = showGrids.get(k);
       								seg.write(iequipment.getType());
       								seg.write(iequipment.toClientBytesWithLevel(player.getLevel()));
       							}
       							
       							seg.write((byte) 1);
       							seg.writeString("精炼属性替换");
       							seg.writeString("insteadenhance 1");
       							connectService.writeTo(seg, player.getId());
       						}else{
       							UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
       							seg.writeShort((short) 10269);
       							seg.writeString("星装列表");
       							seg.write((byte) 4);
       							seg.writeShort((short) (showGrids.size()));
       							for(int k =0; k<showGrids.size(); k++){
       								
       								IEquipment iequipment = showGrids.get(k);
       								seg.writeInt(iequipment.getId());
       								seg.writeString(iequipment.getName());
       								seg.writeInt(Utils.CLR_EQUIP[iequipment.getQuality()]);
       							}
       							seg.write((byte) 1);
       							seg.writeString("精炼属性替换");
       							seg.writeString("insteadenhance 1");
       							connectService.writeTo(seg, player.getId());
       						}
       					}
       				}else if(serial == 1){//已经选择好了装备，准备取星数
       					int itemId = Integer.parseInt(command.getParam(1));
       					Grid[] grids = player.getEquipments();
       					if(grids.length != 0){
       						for(int i = 0; i < grids.length; i++){
       							IEquipment iequipment = (IEquipment) grids[i].item;
       							if(iequipment.getId() == itemId){
       								//下发星数
       								List<Enhance> enhanceList =iequipment.getEnhances();
       								Vector<String> commandString = new Vector<String>();
       								String title = "选择要替换的精炼星数";
       								
       								for(int k = 0; k < enhanceList.size(); k++){
       									Enhance enhance = enhanceList.get(k);
       									title = title + "\n" + (k+1) +"." + (k+1) + "星" + enhance.getName();
       									String insteadCommand = "insteadenhance 2 " + itemId + " " + (k+1);
       									commandString.add(insteadCommand);
       								}
       								String[] sendStrings = new String[commandString.size() + 3];
       								sendStrings[0] = Integer.toString(enhanceList.size());
       								sendStrings[1] = "1";
       								sendStrings[2] = title;
       								for(int z = 0; z < commandString.size(); z++){
       									sendStrings[z + 3] = commandString.get(z);
       								}
       								byte[] bytes = stageService.getTaskBytes((short) 31010, sendStrings);
       								UWAPSegment seg = new UWAPSegment(ClientConstants.
       										GET_FILE_OK,
       										command.getSerial(),
       										command.getSessionId());
       								seg.writeShort((short) 31010);
       								seg.writeShort((short) 2);
       								seg.write(bytes);
       								write(seg);
       								
       								break;
       							}
       						}
       					}else{
       						sendMessage(player.getId(),"你背包内没有装备稍候再来吧 ");
       					}
       				}else if(serial == 2){//已经选好了装备。准备选择替换的属性
       					int itemId = Integer.parseInt(command.getParam(1));
       					int insteadEnhance = Integer.parseInt(command.getParam(2));
       					Grid[] grids = player.getEquipments();
       					if(grids.length != 0){
       						for(int i = 0; i < grids.length; i++){
       							IEquipment iequipment = (IEquipment) grids[i].item;
       							if(iequipment.getId() == itemId){//找到该物品
       								List<Enhance> enhanceList =iequipment.getEnhances();
       								if(enhanceList.size() < insteadEnhance){
       									sendMessage(player.getId(),"装备所选择代替的星数不存在，请稍候再来");
       								}else{//发送准备选择替换的属性
       									
       									byte[] bytes = stageService.getTaskBytes((short) 31024,
       											new String[] {"insteadenhance 3 " + itemId + " " + insteadEnhance});
       									UWAPSegment seg = new UWAPSegment(ClientConstants.
       											GET_FILE_OK);
       									seg.writeShort((short) 31024);
       									seg.writeShort((short) 2);
       									seg.write(bytes);
       									connectService.writeTo(seg, player.getId());
       								}
       								break;
       							}
       						}
       					}else{
       						sendMessage(player.getId(),"你背包内没有装备稍候再来吧 ");
       					}
       				}else if(serial == 3){
       					int itemId = Integer.parseInt(command.getParam(1));
       					int insteadEnhance = Integer.parseInt(command.getParam(2));
       					int enhanceAtrri = Integer.parseInt(command.getParam(3));
       					Grid[] grids = player.getEquipments();
       					if(grids.length != 0){
       						for(int i = 0; i < grids.length; i++){
       							IEquipment iequipment = (IEquipment) grids[i].item;
       							if(iequipment.getId() == itemId){//找到该物品
       								List<Enhance> enhanceList =iequipment.getEnhances();
       								if(enhanceList.size() < insteadEnhance){
       									sendMessage(player.getId(),"装备所选择代替的星数不存在，请稍候再来");
       								}else{//发送精炼需要的材料数目选择
       									IItemTemplate template = Items.getTemplate(iequipment.getItemId());
       									Enhance enhance = Enhance.getEnhance(enhanceAtrri,template.getLevel());
       									if (enhance != null) {
       										IItemTemplate item = Items.getTemplate(enhance.getItemId());
       										int[] count = Utils.getInsteadEnhanceItemCount(iequipment, enhance, insteadEnhance);
       										//mengjie add 精炼精华与精炼石数量相同
       										
       										String msg = "属性" + enhance.getName() + "将提高" + enhance.getPoint(insteadEnhance) + "点,需要精炼石" + count[1] + "个," +
       										item.getName() + "(或者更高级的精华)" + 
       										count[1] + "个,新的精炼属性将替换原来的精炼属性,要继续么?\n1.开始精炼\n2.放弃精炼";
       										byte[] bytes = stageService.getTaskBytes((short) 31025,
       												new String[] {msg, "insteadenhance 4 " + itemId + " " + insteadEnhance + " " + enhanceAtrri/*"enhance " + equItemId + " " + id + " " + pro*/});
       										UWAPSegment seg = new UWAPSegment(ClientConstants.
       												GET_FILE_OK);
       										seg.writeShort((short) 31025);
       										seg.writeShort((short) 2);
       										seg.write(bytes);
       										connectService.writeTo(seg, player.getId());
       									}else{
       										sendMessage(player.getId(),"没有该精炼属性");
       									}
       								}
       								break;
       							}
       							
       						}
       					}else{
       						sendMessage(player.getId(),"你背包内没有装备稍候再来吧 ");
       					}
       				}else if(serial == 4){
       					int itemId = Integer.parseInt(command.getParam(1));
       					int insteadEnhance = Integer.parseInt(command.getParam(2));
       					int enhanceAtrri = Integer.parseInt(command.getParam(3));
       					Grid[] grids = player.getEquipments();
       					if(grids.length != 0){
       						for(int i = 0; i < grids.length; i++){
       							IEquipment iequipment = (IEquipment) grids[i].item;
       							if(iequipment.getId() == itemId){//找到该物品
       								List<Enhance> enhanceList =iequipment.getEnhances();
       								if(enhanceList.size() < insteadEnhance){
       									sendMessage(player.getId(),"装备所选择代替的星数不存在，请稍候再来");
       								}else{//发送精炼需要的材料数目选择
       									IItemTemplate template = Items.getTemplate(iequipment.getItemId());
       									Enhance enhance = Enhance.getEnhance(enhanceAtrri,template.getLevel());
       									if (enhance != null) {//属性替换
       										IItemTemplate item = Items.getTemplate(enhance.getItemId());
       										int[] count = Utils.getInsteadEnhanceItemCount(iequipment, enhance, insteadEnhance);
       										//足够的精炼材料
       										//jwp add 查找高级精华蛋精炼
       										boolean needItemFindFlag = false;
       										int itemLevel = Enhance.level_quality[template.getLevel()];
       										
       										int needItemCount = 0;
       										int replaceItemId = 200885;		//顶级精华定向包
       										
       										//while(!needItemFindFlag && itemLevel <= Enhance.getEnhanceMaxPointProLevel()){
       											/*if(needItemCount >= itemCount[1]){
	    	                        		needItemFindFlag = true;
	    	                        	}else{*/
       											Enhance tempEnhance = Enhance.getUpEnhance(enhanceAtrri,3);//不考虑初级和中级精华(tempLevel写3为顶级)
       											needItemCount = needItemCount + player.getItemCount(tempEnhance.getItemId());
       											//itemLevel++;
       											
       											if(needItemCount >= count[1]){
       												needItemFindFlag = true;
       											}
       											
       											//背包里有定向包并且数量足够
       				                    		if(player.hasItem(replaceItemId) && player.getItemCount(replaceItemId) + needItemCount >= count[1]){
       				                    			needItemFindFlag = true;
       				                    		}
       											//}
       										//}
       										/* while(!needItemFindFlag && itemLevel <= Enhance.getEnhanceMaxPointProLevel()){
	    	                        	if(needItemCount >= count[1]){
	    	                        		needItemFindFlag = true;
	    	                        	}else{
	    	                        		Enhance tempEnhance = Enhance.getUpEnhance(enhanceAtrri,itemLevel);
	    	                        		needItemCount = needItemCount + player.getItemCount(tempEnhance.getItemId());
	    	                        		itemLevel++;
	    	                        	}
	    	                        }*/
       										//jwp add end
       										if (needItemFindFlag/*player.hasItem(enhance.getItemId(), count[1]) */&& player.hasItem(211002, count[1])) {
       											int probability = Utils.getInsteadEnhanceItemProbability(iequipment, player.getLevel(), enhance,insteadEnhance);
       											//添加地区精炼成功率 jwp add start
       											int upProbability =Enhance.getUpMapPercent(player.getMapId());
       											probability += upProbability;
       											//jwp add end
       											Buf buf = player.getBuf(Buf.ENHANCE);
       											if(buf!=null){
       												probability += buf.getValue();
       											}
       											player.removeBuf(Buf.ENHANCE,null);
       											//星辉套装 3、4星加成功率
       											int[] diamondShineLevel = Suits.getActualPointSuitEffect2(player.getUsedEquipments());
       											if(diamondShineLevel[0] >= 4){
       												probability = probability + probability * 4 / 100;
       											} else if(diamondShineLevel[0] == 3){
       												probability = probability + probability * 2 / 100;
       											}
       											log.info("ID[" + player.getId() + "]equId[" + template.getItemId() + "]instanceId[" + itemId + "]pro[" +
       													enhance.getName() + "]insteadtime[" + insteadEnhance+ "]probability[" + probability + "] insteadEnhance start");
       											if (probability >= 100 || Utils.hit(rnd, probability, 100)) {
       												Changed changed = new Changed();
       												player.completeRemoveItem(211002, count[1], changed);
       												//player.completeRemoveItem(enhance.getItemId(), count[1], changed);
       												int temp = count[1];
       												//属性计数器
       												int tempLevel = Enhance.level_quality[template.getLevel()];
       												//while(temp >0 && tempLevel <= Enhance.getEnhanceMaxPointProLevel()){
       													Enhance enhance2 = Enhance.getUpEnhance(enhanceAtrri,3);//不考虑初级和中级精华(tempLevel写3为顶级)
       													int counts = player.getItemCount(enhance2.getItemId());
       													if(counts >= temp){////结束了 顶级精华足够 
       														player.completeRemoveItem(enhance2.getItemId(), temp, changed);
       														//break;
       													}else {
       														player.completeRemoveItem(enhance2.getItemId(), counts, changed);
       														player.completeRemoveItem(replaceItemId, temp - counts, changed);
       													}
       													//tempLevel++;
       												//}
       												//jwp add end
       												iequipment.insteadEnhance(enhance,insteadEnhance);
       												Utils.resetEnhanceStatus(iequipment, true);
       												changed.addEquipment(iequipment, -1);
       												changed.addEquipment(iequipment, 1);
       												sendMessage("恭喜你，属性替换成功 ！", command.getSerial(), command.getSessionId());
       												sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 30);
       												
       												log.info("ID[" + player.getId() + "]equId[" + template.getItemId() + "]instanceId[" + itemId + "]pro[" +
       														enhance.getName() + "]insteadtime[" + insteadEnhance+ "]probability[" + probability + "] insteadEnhance success");
       											}else{
       												Changed changed = new Changed();
       												player.completeRemoveItem(211002, count[1], changed);
       												//player.completeRemoveItem(enhance.getItemId(), count[1], changed);
       												int temp = count[1];
       												//属性计数器
       												int tempLevel = Enhance.level_quality[template.getLevel()];
       												//while(temp >0 && tempLevel <= Enhance.getEnhanceMaxPointProLevel()){
       													Enhance enhance2 = Enhance.getUpEnhance(enhanceAtrri,3);
       													int counts = player.getItemCount(enhance2.getItemId());
       													if(counts >= temp){//结束了
       														player.completeRemoveItem(enhance2.getItemId(), temp, changed);
       														//break;
       													}else {
       														player.completeRemoveItem(enhance2.getItemId(), counts, changed);
       														player.completeRemoveItem(replaceItemId, temp - counts, changed);
       													}
       													//tempLevel++;
       												//}
       												Utils.resetEnhanceStatus(iequipment, false);
       												sendMessage("很遗憾，祝您下次成功 ", command.getSerial(), command.getSessionId());
       												sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 30);
       												
       												log.info("ID[" + player.getId() + "]equId[" + template.getItemId() + "]instanceId[" + itemId + "]pro[" +
       														enhance.getName() + "]insteadtime[" + insteadEnhance+ "]probability[" + probability + "] insteadEnhance fail");
       											}
       										}else{
       											sendMessage("您的材料不够，准备好了再来吧！", command.getSerial(), command.getSessionId());
       										}
       									}else{
       										sendMessage(player.getId(),"没有该精炼属性");
       									}
       								}
       								break;
       							}
       							
       						}
       					}else{
       						sendMessage(player.getId(),"你背包内没有装备稍候再来吧 ");
       					}
       				}
				}
       		}
       		//log.info("获得serial" + serial);
       		//sendMessage(player.getId(),"你选择的仇人不在线。无法传送 ");
       	}
       	
     }
     
     /**
     * @author wpjiang
     *选举投票
     */
    class VotePreocessor implements CommandProcessor{//选举投票
      	/* (non-Javadoc)
      	 * @see com.pip.itimes.server.world.CommandProcessor#process(com.pip.itimes.server.world.WorldPlayer, com.pip.itimes.server.stage.Command)
      	 */
      	public void process(WorldPlayer player, Command command) throws Exception {
      		int voteType = Integer.parseInt(command.getParam(0));
      		int type = Integer.parseInt(command.getParam(1));
        	//voteType = 1001;2
        	VotePlayerGift votePlayerGift = voteGiftGroups.getGiftGroup(voteType);
        	if(votePlayerGift == null/* || !votePlayerGift.isAvlib()*/){
        		sendMessage(player.getId(), "暂时没有竞选活动，请稍后再来");
        	}else{
	      		if(type %10 == 0){//1级菜单        投票第一，参加第2，排行第3，收费道具投票大王，介绍第4，奖励第5
	      			//log.info("选举类型id" + voteType + "选择"+ type);
	      			if(type/10 == 5){//介绍
	      				byte[] bytes = stageService.getTaskBytes((short) 31019, new String[]{votePlayerGift.getVoteContent()});
                        UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,command.getSerial(), command.getSessionId());
                        seg.writeShort((short) 31019);
                        seg.writeShort((short) 2);
                        seg.write(bytes);
                        write(seg);
	      				//sendMessage(player.getId(), votePlayerGift.getVoteContent());
	      			}else if(type/10 == 6){//奖励介绍
	      				if(player.getSex() == 0){
	      					byte[] bytes = stageService.getTaskBytes((short) 31019, new String[]{votePlayerGift.getManAwardIntroduction()});
	                        UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,command.getSerial(), command.getSessionId());
	                        seg.writeShort((short) 31019);
	                        seg.writeShort((short) 2);
	                        seg.write(bytes);
	                        write(seg);
	      					//sendMessage(player.getId(), votePlayerGift.getManAwardIntroduction());
	      				}else if(player.getSex() == 1){
	      					byte[] bytes = stageService.getTaskBytes((short) 31019, new String[]{votePlayerGift.getWomanAwardIntroduction()});
	                        UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,command.getSerial(), command.getSessionId());
	                        seg.writeShort((short) 31019);
	                        seg.writeShort((short) 2);
	                        seg.write(bytes);
	                        write(seg);
	      						//sendMessage(player.getId(), votePlayerGift.getWomanAwardIntroduction());
	      				}
	      			}else if(type/10 == 2){//参加比赛
	      				if(!votePlayerGift.isAvlib()){
	      	        		sendMessage(player.getId(), "暂时没有竞选活动，请稍后再来");
	      	        		return;
	      	        	}
	      				if(votePlayerGift.getMainType() == 3 || votePlayerGift.getMainType() == (player.getSex()+ 1)){
	      					if(player.getLevel() >= votePlayerGift.getVoteLevel()){	
	      						Map<Integer,VoteShowInfo> voteContentMap= voteService.getVoteContentMap(voteType);
	      						if(!voteContentMap.containsKey(player.getId())){//没有参加过比赛
			      					byte[] bytes = stageService.getTaskBytes((short) 31001, new String[]{"你确定要参赛并填写参赛宣言吗？\n1.确定\n2.退出", "参赛宣言", "vote "+ votePlayerGift.getId() + " 22 " });
			                        UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,command.getSerial(), command.getSessionId());
			                        seg.writeShort((short) 31001);
			                        seg.writeShort((short) 2);
			                        seg.write(bytes);
			                        write(seg);
	      						}else{
	      							byte[] bytes = stageService.getTaskBytes((short) 31001, new String[]{"你确定要修改参赛宣言吗？\n1.确定\n2.退出", "参赛宣言", "vote "+ votePlayerGift.getId() + " 22 " });
			                        UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,command.getSerial(), command.getSessionId());
			                        seg.writeShort((short) 31001);
			                        seg.writeShort((short) 2);
			                        seg.write(bytes);
			                        write(seg);
	      						}
	      					}else{
	      						sendMessage(player.getId(), "对不起，你参加比赛等级不够。请仔细阅读" + votePlayerGift.getVoteTitle() + "活动内容介绍");
	      					}
	      				}else {
	      					sendMessage(player.getId(), "对不起，你不能参加比赛。请仔细阅读" + votePlayerGift.getVoteTitle() + "活动内容介绍");
	      				}
	      				
	      			}else if(type/10 == 1){//好友投票
	      				if(!votePlayerGift.isAvlib()){
	      	        		sendMessage(player.getId(), "暂时没有竞选活动，请稍后再来");
	      	        		return;
	      	        	}
	      				if(player.getLevel() >= votePlayerGift.getVoteplayerlevel()){
	      					if((votePlayerGift.getVoteType() != (player.getSex()+1) ) && 
	      							(votePlayerGift.getVoteType() != 3)){//3为男女都可以投票
		      					sendMessage(player.getId(), "对不起，你无法投票.请仔细阅读" + votePlayerGift.getVoteTitle() + "活动内容介绍");
		      					return;
		      				}

	      					
		      				Map<Friend, Integer> friendMap = new HashMap<Friend, Integer>();
		      				Map<Integer,VoteShowInfo> voteContentMap= voteService.getVoteContentMap(voteType);
		      				if(voteContentMap == null || voteContentMap.size() == 0){
		      					sendMessage(player.getId(), "你的好友没有参加竞选，不可以在此快捷投票，可以到"+votePlayerGift.getVoteTitle()+"排行榜进行选择投票");
		      					return;
		      				}
		      				Friend[] friends = player.getFriends();
		      				Set<Integer> keySet = voteContentMap.keySet();
		      				int i;
		      				for(i = 0; i < friends.length; i++){
		      				     //遍历key集合
		      					Friend friend = friends[i];
		      					for(int key : keySet) {
		      				    	if(friend.getId() == key){
		      				    		friendMap.put(friend, 0);
		      				    	}
		      				    }
		      				}
		      				ArrayList<VoteInfo> voteTreeSet = voteService.getVoteSet(voteType);
		      				for(Entry<Friend, Integer> temp: friendMap.entrySet()){
		      					Iterator it =voteTreeSet.iterator();
		      					while(it.hasNext())
		      					{
		      		        	   VoteInfo voteInfo = (VoteInfo)it.next(); 
		      		        	   Friend friend = temp.getKey();
		      		        	   if(friend.getId() == voteInfo.getId()){
		      		        		   temp.setValue(voteInfo.getVotePoint());
		      		        	   }
		      					}
		      				}
		      				if(friendMap.size() == 0){
		      					sendMessage(player.getId(), "你的好友没有参加竞选，不可以在此快捷投票，可以到"+votePlayerGift.getVoteTitle()+"排行榜进行选择投票");
		      					return;
		      				}else{//下发可以投票的好友程度
		      					UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
		      	                seg.writeShort((short) 10266);
		      	                seg.writeString("好友" + votePlayerGift.getVoteTitle() + "列表");
		      	                seg.write((byte) 4);
		      	                seg.writeShort((short) friendMap.size());
		      	                for(Map.Entry<Friend, Integer> temp : friendMap.entrySet()){
		      	                	Friend friend = temp.getKey();
		      	                	
		      	                	//为了查看明细将选举类型放在short最高5位
		      	                	int friendId = voteType << 26 | friend.getId();
		      	                	
		      	                	//VoteShowInfo voteShowInfo = voteContentMap.get(friendId);
		      	                	seg.writeInt(friendId);
		      	                	seg.writeString(friend.getName() + "    " + temp.getValue()+ "票");
		      	                	seg.writeInt(Utils.CLR_WHITE);
		      	                }
			      	                //获得需要的物品并发送命令
			      	            Vector<String> voteComandString = new Vector<String>();
			      	            Vector<TemplateGrid> voteItem = new Vector<TemplateGrid>();
			      	            Vector<VoteGiftDefine> voteGiftDefines = new Vector<VoteGiftDefine>();
			      	            voteGiftDefines	= votePlayerGift.getVoteGiftDefines();
			      	            
			      	            for(i = 0; i < voteGiftDefines.size(); i++){
			      	                if(!voteGiftDefines.get(i).isLevelOK(player.getLevel())){
			      	                	continue;
			      	                }
			      	                TemplateGrid[] needItems = voteGiftDefines.get(i).getNeedItems();
			      	                	//为了安全起见 需要的物品只要一个
			      	                for(int k = 0; k < needItems.length; k++){
			      	                	String commandString = new String();
			      	                	IItemTemplate template = needItems[k].template;
			      	                	commandString = commandString + needItems[k].count + "个" + template.getName();
			      	               		int point = voteGiftDefines.get(i).getItemsVotePoint(template.getItemId());
			      	               		commandString = commandString + point * needItems[k].count + "票";
			      	               		
			      	               		voteComandString.add(commandString);
			      	               		voteItem.add(needItems[k]);
			      	               	}
			      	            }
			      	            seg.write((byte)voteComandString.size());
			      	            for(i = 0; i < voteComandString.size(); i++ ){
			      	            	seg.writeString(voteComandString.get(i));
			      	            	String sendCommand = new String();
			      	            	sendCommand = "vote " + voteType+" 11 " + voteItem.get(i).template.getItemId() + " " + voteItem.get(i).count;
				      	            seg.writeString(sendCommand);   
			      	            }    
		      	                connectService.writeTo(seg, player.getId());
		      				}
		      			}else{
		      				sendMessage(player.getId(), "对不起，你不能参加投票。请仔细阅读" + votePlayerGift.getVoteTitle() + "活动内容介绍");
		      			}
	      			}else if(type /10 == 3){//排行榜
	      				byte[] bytes = stageService.getTaskBytes((short) 31010, new String[] {
                                "4", "1", votePlayerGift.getVoteTitle() + "\n1.查看票数排行榜"+
                                			"\n2.查看自己的票数 "+
                                			"\n3.查看自己的投票明细"+
                                			"\n4.我一会再来参加",
                                "vote " + voteType + " 31",
                                "vote " + voteType + " 32",
                                "vote " + voteType + " 33",
                                "ok"});
                		UWAPSegment seg = new UWAPSegment(ClientConstants.
                                GET_FILE_OK,
                                command.getSerial(),
                                command.getSessionId());
                        seg.writeShort((short) 31010);
                        seg.writeShort((short) 2);
                        seg.write(bytes);
                        write(seg);
	      			} else if (type / 10 == 4) {	// 收费道具投票大王
	      				if (votePlayerGift.notShowVotesKing()) {
	      					sendMessage(player.getId(), "请活动结束后再来查看");
	      				} else {
	      					// 获得投票大王ID;
	      					ArrayList <VotesKing> votesKingSet = voteService.getVotesKingSet(voteType);
	      					if (votesKingSet == null) {
	      						voteService.loadVotesKing(voteType);
	      						votesKingSet = voteService.getVotesKingSet(voteType);
	      					}
	      					if (votesKingSet.isEmpty() || votesKingSet.size() == 0) {
	      						sendMessage("暂时没有排行请稍候再来", command.getSerial(), command.getSessionId());
	      					} else {
	      						Vector <VotesKing> kingId = new Vector <VotesKing> ();
	      						Iterator it = votesKingSet.iterator();
	      						int i = 0;
	      						while (it.hasNext()) {
	      							VotesKing votesKing = (VotesKing) it.next(); 
	      							kingId.add(votesKing);
	      							i++;
	      						}
	      						// 校检数据
	      						Map <Integer, VoteKingInfo> votesKingInfo = voteService.getVotesKingInfo(voteType);
	      						if (votesKingInfo.isEmpty() || votesKingInfo.size() == 0) {
	      							sendMessage("暂时没有排行请稍候再来", command.getSerial(), command.getSessionId());
	      							return;
	      						} else {
	      							for (i = 0; i < Math.min(kingId.size(), votesKingInfo.size()); i++) {
	      								VoteKingInfo kingInfo = votesKingInfo.get(kingId.get(i).getId());
	      								if (kingInfo == null || kingInfo.getPlayerName()== null || ((kingInfo.getPlayerName().equals("") && kingInfo.getPlayerName().length() ==0))) {
	      									kingId.remove(i);
	      									i--;
	      									continue;
	      								}
	      							}
	      							if (kingId.size() == 0) {
	      								sendMessage("暂时没有排行请稍候再来", command.getSerial(), command.getSessionId());
	      								return;
	      							}
	      						}
	      						
	      						//下发收费道具票数大王
	      						UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
	      						seg.writeShort((short) 10272);
	      						seg.writeString("收费道具票数大王");
	      						seg.write((byte) 4);
	      						seg.writeShort((short) (Math.min(Math.min(kingId.size(), votesKingInfo.size()), votePlayerGift.getWinnersNumEntry())));
	      						for (i = 0; i < Math.min(kingId.size(), votesKingInfo.size()) && i < votePlayerGift.getWinnersNumVote(); i++) {
	      							VoteKingInfo votesKing = votesKingInfo.get(kingId.get(i).getId());
	      							if (votesKing == null || votesKing.getPlayerName() == null || ((votesKing.getPlayerName().equals("") && votesKing.getPlayerName().length() == 0))) {
	      								continue;
	      							}
	      							int playerId =  voteType << 26 | kingId.get(i).getId();
	      							seg.writeInt(playerId);
	      							seg.writeString(i + 1 + "." + votesKing.getPlayerName() + " " +  kingId.get(i).getvotes() + "票数");
	      							seg.writeInt(Utils.CLR_WHITE);
	      						}
	      						seg.write((byte)0);
	      						connectService.writeTo(seg, player.getId());
	      					}
	      				}
	      			}else if(type/10 == 7){//为自己投票
	      				//mengjie add 20110428
	      				ArrayList<VoteInfo> voteTreeSet = voteService.getVoteSet(voteType);
	      				Iterator it =voteTreeSet.iterator();
	      				int mine_count_tmp = 0;
	      				boolean vote_valid = false;//自己是否参选
	      				Map<Integer,VoteShowInfo> voteContentMap= voteService.getVoteContentMap(voteType);
	      				if(voteContentMap == null || voteContentMap.size() == 0){
	      					sendMessage("您还没有参选哦，不能给自己投票呢。", command.getSerial(), command.getSessionId());
	      					return;
	      				}
	      				Set<Integer> keySet = voteContentMap.keySet();
      					for(int key : keySet) {
      				    	if(player.getId() == key){
      				    		vote_valid = true;
      				    	}
      				    }
	      				if (vote_valid){
	      					while(it.hasNext())
	      					{
	      		        	   VoteInfo voteInfo = (VoteInfo)it.next();
	      		        	   if(player.getId() == voteInfo.getId()){
	      		        		 mine_count_tmp = voteInfo.getVotePoint();
	      		        	   }
	      					}
	      					UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
	      	                seg.writeShort((short) 10266);
	      	                seg.writeString(votePlayerGift.getVoteTitle());
	      	                seg.write((byte) 3);
	      	                seg.writeShort((short) 1);
	      	                
	      	                seg.writeInt(player.getId());
		                	seg.writeString(player.getPlayerName() + "(自己)    " + mine_count_tmp + "票");
		                	seg.writeInt(Utils.CLR_WHITE);
	      	                
		      	            //获得需要的物品并发送命令
		      	            Vector<String> voteComandString = new Vector<String>();
		      	            Vector<TemplateGrid> voteItem = new Vector<TemplateGrid>();
		      	            Vector<VoteGiftDefine> voteGiftDefines = new Vector<VoteGiftDefine>();
		      	            voteGiftDefines	= votePlayerGift.getVoteGiftDefines();
		      	            
		      	            for(int i = 0; i < voteGiftDefines.size(); i++){
		      	                if(!voteGiftDefines.get(i).isLevelOK(player.getLevel())){
		      	                	continue;
		      	                }
		      	                TemplateGrid[] needItems = voteGiftDefines.get(i).getNeedItems();
		      	                	//为了安全起见 需要的物品只要一个
		      	                for(int k = 0; k < needItems.length; k++){
		      	                	String commandString = new String();
		      	                	IItemTemplate template = needItems[k].template;
		      	                	commandString = commandString + needItems[k].count + "个" + template.getName();
		      	               		int point = voteGiftDefines.get(i).getItemsVotePoint(template.getItemId());
		      	               		commandString = commandString + point * needItems[k].count + "票";
		      	               		
		      	               		voteComandString.add(commandString);
		      	               		voteItem.add(needItems[k]);
		      	               	}
		      	            }
		      	            seg.write((byte)voteComandString.size());
		      	            for(int i = 0; i < voteComandString.size(); i++ ){
		      	            	seg.writeString(voteComandString.get(i));
		      	            	String sendCommand = new String();
		      	            	sendCommand = "vote " + voteType+" 71 " + voteItem.get(i).template.getItemId() + " " + voteItem.get(i).count;
			      	            seg.writeString(sendCommand);   
		      	            }    
	      	                connectService.writeTo(seg, player.getId());
	      				}else{
	      					sendMessage("您还没有参选哦，不能给自己投票呢。", command.getSerial(), command.getSessionId());
	      					return;
	      				}
	      			}
	      		}else{//二级菜单
	      			if(type /10  == 2){
		      			if(type == 22){//已经输入内容并参赛
		      				String voteContentString = command.getParam(2);
		      				voteContentString = voteContentString.replace('\n',' ');
		      				//log.info("选举宣言" + voteContentString);
		      				//字符串进行过滤
		      				if(voteContentString.length()==0){
		      					sendMessage("角色名出现非法字符", command.getSerial(), command.getSessionId());
		      					return;
		      				}
		      				if(!votePlayerGift.isAvlib()){
		      	        		sendMessage(player.getId(), "暂时没有竞选活动，请稍后再来");
		      	        		return;
		      	        	}
		      	          /*  if(KeywordsUtil.isInvalidName(voteContentString.toLowerCase()))
		      	                throw new CreatePlayerException("角色名出现非法字符");*/
		      	          /*  if(!Utils.checkString(voteContentString,false)){
		      	            	sendMessage("角色名出现非法字符", command.getSerial(), command.getSessionId());
      							return;
		      	            }*/
/*		      	            String newName = KeywordsUtil.filterKeywords(name);
		      	            if(!newName.equals(name))
		      	                throw new CreatePlayerException("角色名出现非法字符");*/
		      				if(!KeywordsUtil.isLegitimate(voteContentString)){//输入不合法
		      					byte[] bytes = stageService.getTaskBytes((short) 31001, new String[]{"你刚才的输入不合法，请重新填写参赛宣言。\n1.确定\n2.退出", "参赛宣言", "vote "+ votePlayerGift.getId() + " 22 " });
		                        UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,command.getSerial(), command.getSessionId());
		                        seg.writeShort((short) 31001);
		                        seg.writeShort((short) 2);
		                        seg.write(bytes);
		                        write(seg);
		      				}else{//参赛成功
		      					Map<Integer,VoteShowInfo> voteContentMap= voteService.getVoteContentMap(voteType);
		      					if (player.getMoeny() < 999) {
		      						sendMessage("发表或者修改参赛宣言需要999J,请稍候再来", command.getSerial(), command.getSessionId());
		      						return;
		      					}
		      					player.setMoeny(player.getMoeny() - 999);
		      					Changed changed = new Changed();
		      					changed.addProperty(Changed.MONEY, -999);
		      				    sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);
		      					if(!voteContentMap.containsKey(player.getId())){//没有参加过比赛
			      					//log.info("参赛成功");
			      					//放入一个map里面
			      					voteService.setVoteContent(voteType,player, voteContentString, true);
			      					//数据库插入
			      					voteService.saveVoteContent(voteType,player.getId(), voteContentString);
			      					sendMessage(player.getId(), "发表参赛宣言成功。赶快拉拢你的朋友为你投票吧");
	      						}else{//修改宣言
	      							//需要修改数据库，内容里面的 showInfo
	      							voteService.upDataVoteContent(voteType, player, voteContentString);
	      							sendMessage(player.getId(), "修改参赛宣言成功。赶快拉拢你的朋友为你投票吧");
	      						}
		      				}
		      			}
	      			}else if(type /10 == 1){
	      				if(!votePlayerGift.isAvlib()){
	      	        		sendMessage(player.getId(), "暂时没有竞选活动，请稍后再来");
	      	        		return;
	      	        	}
	      				if(type == 11){
	      				int itemId = Integer.parseInt(command.getParam(2));
	      				int count = Integer.parseInt(command.getParam(3));
	      				int playId = Integer.parseInt(command.getParam(4));
	      				log.info("vote start voteId[" + player.getId() + "]vote playId[" + playId + "useItemid["+itemId+"]voteCount[" + count );
	      			
	      				//抛弃其中的选局类型
	      				playId = playId & 0x3FFFFFF;
	      				
	      				if(playId == player.getId()){
	      					
	      					sendMessage(player.getId(), "不能给自己投票，请为别人投票吧");
	      					return;
	      				}
	      				//log.info("投票成功" + itemId + "玩家id" + playId);
	      				//根据物品查找相应的奖励并加入内存投票榜和数据库
	      				 Vector<VoteGiftDefine> vectorVoteGiftDefine = votePlayerGift.getVoteGiftDefines();
	      				 
	      				 int flag = -1;//用于查找物品是否找到的标志
	      				 for(int i = 0; i < vectorVoteGiftDefine.size() && flag == -1; i++){
	      					TemplateGrid[] needItems = vectorVoteGiftDefine.get(i).getNeedItems();
	      					if(!vectorVoteGiftDefine.get(i).isLevelOK(player.getLevel())){
	      						continue;
	      					}
	      					for(int k = 0; k < needItems.length ; k++){
	      						if(itemId == needItems[k].template.getItemId() && count == needItems[k].count){
	      							flag = i;
	      							break;
	      						}
	      					}
	      				 }
	      				 if(flag == -1){//查找未成功
	      					sendMessage(player.getId(), "没有足够的物品，请稍候再来");
	      				 }else{//获得奖励物品，并记录数据
	      					 TemplateGrid[] needGrid = vectorVoteGiftDefine.get(flag).getNeedItems();
                             TemplateGrid[] giveGrid = vectorVoteGiftDefine.get(flag).getGiveItems();
                             if (needGrid.length > 0 && !player.contains(needGrid)) {
                                 sendMessage("没有足够的物品", command.getSerial(), command.getSessionId());
                                 return;
                             }
                             if (player.isOver(giveGrid)) {
                                 sendMessage(votePlayerGift.getVoteBag(), command.getSerial(), command.getSessionId());
                                 return;
                             }
                             Changed changed = new Changed();
                             synchronized (player) {
                                 player.completeRemoveItem(needGrid, changed);
                                 player.addItems(giveGrid, changed, player.getClientDataVersion());
                             }

                             playerService.checkPlayer(player);
                             sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);
                             byte isImoneyItem = vectorVoteGiftDefine.get(flag).getIsImoneyItem();;
                             int addPoint = 0;
                             for(int w = 0; w < needGrid.length; w++){
                            	 addPoint = addPoint + vectorVoteGiftDefine.get(flag).getItemsVotePoint(needGrid[w].template.getItemId()) * needGrid[w].count;
                             }
                             
                             //记录内存
                             ArrayList<VoteInfo> voteTreeSet = voteService.getVoteSet(voteType);
                             Iterator it =voteTreeSet.iterator();
                             boolean insertFlag = true;
                             while(it.hasNext())
                             {
                            	 VoteInfo voteInfo = (VoteInfo)it.next(); 
                            	 if(voteInfo.getId() == playId){//已经存在
                            		 insertFlag = false;
                            		 voteInfo.setVotePoint(voteInfo.getVotePoint() + addPoint);
                            	 }
                             }
                             if(insertFlag){
                            	 VoteInfo voteInfo = new VoteInfo(playId, addPoint);
                            	 voteTreeSet.add(voteInfo);
                             }
                             
                             final int maxCount = 5;
                             int length = voteTreeSet.size();
                             if(length > maxCount){
                            	 length = maxCount;
                             }
                             VoteInfo[] firstVoteInfo = new VoteInfo[length];
                             for(int i=0; i<length; i++){
                            	 firstVoteInfo[i] = voteTreeSet.get(i);
                             }
                             //记录内存后排序
                             Collections.sort(voteTreeSet);
                             
                             for(int i=0; i<length; i++){
                            	 VoteInfo info = voteTreeSet.get(i);
                            	 int index = -1;
                            	 for(int j=0; j<length; j++){
                            		 if(firstVoteInfo[j].getId() == info.getId()){
                            			 //原先的排名
                            			 index = j;
                            			 break;
                            		 }
                            	 }
                            	 //在原先的排名中没有该玩家 则给他后面的所有玩家都发消息
                            	 if(index == -1){
                            		 for(int n=i; n<length; n++){
	                            		 WorldPlayer playerTemp = playerService.getWorldPlayer(firstVoteInfo[n].getId());
	                            		 if(playerTemp != null && playerTemp.online()){
	                            			 chatService.sendPrivateMessage(-1, "系统", playerTemp.getId(), "您的投票被人超过了，这能忍吗？赶快去抢回来吧！");
	                            		 }
                            		 }
                            		 break;
                            	 }
                            	 //跟原来不同名次时
                            	 else if(i != index){
	                            	 //被超过时 给被超过的在线玩家发送私聊
                            		 for(int n=i; n<length; n++){
                            			 if(firstVoteInfo[n].getId() != info.getId()){
		                            		 WorldPlayer playerTemp = playerService.getWorldPlayer(firstVoteInfo[n].getId());
		                            		 if(playerTemp != null && playerTemp.online()){
		                            			 chatService.sendPrivateMessage(-1, "系统", playerTemp.getId(), "您的投票被人超过了，这能忍吗？赶快去抢回来吧！");
		                            		 }
                            			 }
                            		 }
                            		 break;
                            	 }
                             }
                             
                             //插入到vote表里
                             Vote vote = new Vote();
                             vote.setCreatetime(new Date());
                             vote.setPlayeridvoters(playId);
                             vote.setVotersid(player.getId());
                             vote.setValid(true);
                             vote.setVotepoint(addPoint);
                             vote.setType(voteType);
                             vote.setIsImoneyItem(isImoneyItem);
                             voteService.saveVote(vote);
                             log.info("vote end voteId [" + player.getId() + "] vote playId [" + playId + " ] useItemid [" + itemId + "] voteCount [" + count + "] isImoneyItem [" + (isImoneyItem == 1 ? true : false) + "]");
	      				 
                             // 投票后获得奖励装备：1个蓝色妖姬中奖几率1/200, 10个蓝色妖姬中奖几率11/200
                             if (isImoneyItem == 1 && count == 1 && Utils.hit(1, 200)) {
                            	 IItem iit = Items.getTemplate(201245).newInstance();
                            	 if (iit != null) {
                            		 changed = new Changed();
                            		 IItem nItem = player.completeAddItem(iit, 1, changed, player.getClientDataVersion());
                            		 if (nItem == null) {
                            			 byte[] att = ItemUtils.item2dbAttachment(iit, count);
                            			 mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                            					 iit.getName() + "*" + 1, "", att, 0, true);
                            			 sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到您的邮箱!");
                            		 }
                            		 sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);
                            	 }
                             } else if (isImoneyItem == 1 && count == 10 && Utils.hit(11, 200)) {
                            	 IItem iit = Items.getTemplate(201245).newInstance();
                            	 if (iit != null) {
                            		 changed = new Changed();
                            		 IItem nItem = player.completeAddItem(iit, 1, changed, player.getClientDataVersion());
                            		 if (nItem == null) {
                            			 byte[] att = ItemUtils.item2dbAttachment(iit, count);
                            			 mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                            					 iit.getName() + "*" + 1, "", att, 0, true);
                            			 sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到您的邮箱!");
                            		 }
                            		 sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);
                            	 }
                             }
	      				 }
	      				}
	      			}else if(type / 10 == 3){//排行榜菜单 1，查看排行榜，查看自己选票数量，查看明细
	      				if(type == 31){
	      					ArrayList<VoteInfo> voteTreeSet = voteService.getVoteSet(voteType);
	      					if(voteTreeSet.isEmpty() || voteTreeSet.size() == 0){
	      						 sendMessage("暂时没有排行请稍候再来", command.getSerial(), command.getSessionId());
	      					}else{
	      						Vector<VoteInfo> showPlayerIdVector = new Vector<VoteInfo>();
	                            Iterator it =voteTreeSet.iterator();
	                            int i = 0;
	                            while(it.hasNext() && i < 36)
	                            {
	                            	VoteInfo voteInfo = (VoteInfo)it.next(); 
	                           	 	showPlayerIdVector.add(voteInfo);
	                           	 	i++;
	                            }
	                            //根据id获取展示信息
	                            Map<Integer,VoteShowInfo> showVoteInfoMap = voteService.getVoteContentMap(voteType);
	                            if(showVoteInfoMap.size() == 0){
	                            	sendMessage("暂时没有排行请稍候再来", command.getSerial(), command.getSessionId());
	                            	return;
	                            }else{//去除不同步的id和名称
	                            	int f = 0;
	 		      	                for(i = 0; i < Math.min(showPlayerIdVector.size(), showVoteInfoMap.size()) && f < 30; i++){
	 		      	                	VoteShowInfo voteShowInfo = showVoteInfoMap.get(showPlayerIdVector.get(i).getId());
			      	                	if(voteShowInfo == null || voteShowInfo.getPlayerName()== null ||((voteShowInfo.getPlayerName().equals("") && voteShowInfo.getPlayerName().length() ==0))){
			      	                		showPlayerIdVector.remove(i);
			      	                		i--;
			      	                		continue;
			      	                		
			      	                	}
			      	                	f++;
	 		      	                }
	 		      	                if(showPlayerIdVector.size() == 0){
	 		      	                	sendMessage("暂时没有排行请稍候再来", command.getSerial(), command.getSessionId());
		                            	return;
	 		      	                }
	                            }
	                            //下发排行榜列表
	                            UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
		      	                seg.writeShort((short) 10267);
		      	                seg.writeString(votePlayerGift.getVoteTitle() + "票数排行榜");
		      	                seg.write((byte) 4);
		      	                seg.writeShort((short) Math.min(Math.min(showPlayerIdVector.size(), showVoteInfoMap.size()), 30));
		      	                //seg.writeShort((short) Math.min(showPlayerIdVector.size(), showVoteInfoMap.size()));
		      	                int f = 0;
		      	                for(i = 0; i < Math.min(showPlayerIdVector.size(), showVoteInfoMap.size()) && f < 30; i++){
		      	                	VoteShowInfo voteShowInfo = showVoteInfoMap.get(showPlayerIdVector.get(i).getId());
		      	                	if(voteShowInfo == null || voteShowInfo.getPlayerName()== null ||((voteShowInfo.getPlayerName().equals("") && voteShowInfo.getPlayerName().length() ==0))){
		      	                		continue;
		      	                	}
		      	                	int playerId =  voteType << 26 | showPlayerIdVector.get(i).getId();
		      	                	seg.writeInt(playerId);
		      	                	
		      	                	seg.writeString(i+1 + "." + voteShowInfo.getPlayerName()/* + " " +  showPlayerIdVector.get(i).getVotePoint()+ "票数"*/);
		      	                	seg.writeInt(Utils.CLR_WHITE);
		      	                	f++;
		      	                }//获得需要的物品并发送命令
		      	                if(votePlayerGift.isAvlib() && player.getLevel() >= votePlayerGift.getVoteplayerlevel()){
			      	                if((votePlayerGift.getVoteType() != player.getSex()) || (votePlayerGift.getVoteType() == 3)){
			      	                	Vector<String> voteComandString = new Vector<String>();
				      	                Vector<TemplateGrid> voteItem = new Vector<TemplateGrid>();
				      	                Vector<VoteGiftDefine> voteGiftDefines = votePlayerGift.getVoteGiftDefines();
				      	          
				      	                for(i = 0; i < voteGiftDefines.size(); i++){
					      	              	if(!voteGiftDefines.get(i).isLevelOK(player.getLevel())){
					      						continue;
					      					}
				      	                	TemplateGrid[] needItems = voteGiftDefines.get(i).getNeedItems();
				      	                	//为了安全起见 需要的物品只要一个
				      	                	for(int k = 0; k < needItems.length; k++){
				      	                		String commandString = new String();
				      	                		IItemTemplate template = needItems[k].template;
				      	                		commandString = commandString + needItems[k].count + "个" + template.getName();
				      	                		int point = voteGiftDefines.get(i).getItemsVotePoint(template.getItemId());
				      	                		commandString = commandString + point * needItems[k].count + "票";
				      	                		voteComandString.add(commandString);
				      	                		voteItem.add(needItems[k]);
				      	                	}
				      	                }
			      	                	seg.write((byte)voteComandString.size());
				      	                for(i = 0; i < voteComandString.size(); i++ ){
				      	                	 seg.writeString(voteComandString.get(i));
				      	                	 String sendCommand = new String();
				      	                	 sendCommand = "vote " + voteType+" 11 " + voteItem.get(i).template.getItemId() + " " + voteItem.get(i).count;
				      	                	 seg.writeString(sendCommand);   
				      	                }
			      	                }else{
			      	                	seg.write((byte)0);
			      	                }
		      	                }else{
		      	                	seg.write((byte)0);
		      	                }
		      	                connectService.writeTo(seg, player.getId());
	      					}
	      				}else if(type == 32){//自己的投票数量
	      					if(!votePlayerGift.isAvlib()){
		      	        		sendMessage(player.getId(), "暂时没有竞选活动，请稍后再来");
		      	        		return;
		      	        	}
	      					if(votePlayerGift.getMainType() == 3 || votePlayerGift.getMainType() == (player.getSex()+1)){
	      						ArrayList<VoteInfo> voteTreeSets = voteService.getVoteSet(voteType);
		      					//Vector<VoteInfo> showPlayerIdVector = new Vector<VoteInfo>();
		                        
		                     /*   while(it.hasNext() )
		                        {
		                            VoteInfo voteInfo = (VoteInfo)it.next(); 
		                           	showPlayerIdVector.add(voteInfo);
		                        }*/
		                            //根据id获取展示信息
		                        Map<Integer,VoteShowInfo> showVoteInfoMap = voteService.getVoteContentMap(voteType);
		 		      	        for(int i = 0; i < Math.min(voteTreeSets.size(), showVoteInfoMap.size()); i++){
		 		      	           VoteShowInfo voteShowInfo = showVoteInfoMap.get(voteTreeSets.get(i).getId());
				      	           if(voteShowInfo == null || voteShowInfo.getPlayerName()== null ||((voteShowInfo.getPlayerName().equals("") && voteShowInfo.getPlayerName().length() ==0))){
				      	        	   	voteTreeSets.remove(i);
				      	                i--;
				      	                continue;		      	                		
				      	                }
		 		      	        }
	      						ArrayList<VoteInfo> voteTreeSet = voteTreeSets;
	      						Iterator it =voteTreeSets.iterator();
	                            int point = 0;
	                            int order = 0;
	                            while(it.hasNext()){
	                            	VoteInfo voteInfo = (VoteInfo)it.next(); 
	                            	order++;
	                            	if(voteInfo.getId() == player.getId()){
	                            		point = voteInfo.getVotePoint();
	                            		break;
	                            	}
	                            	
	                            }
	                            sendMessage("当前的票数为" + point+ (point == 0?"没有排行":"排在第" + order+"名"), command.getSerial(), command.getSessionId());
	      					}else{
	      						sendMessage("你没有参加比赛，不能查看自己的比赛票数", command.getSerial(), command.getSessionId());
	      					}
	      				}else if(type == 33){
	      					if(!votePlayerGift.isAvlib()){
		      	        		sendMessage(player.getId(), "暂时没有竞选活动，请稍后再来");
		      	        		return;
		      	        	}
	      					if(votePlayerGift.getMainType() == 3 || votePlayerGift.getMainType() == (player.getSex()+1)){
		      					List voteShowList = voteService.getVotePlayers(voteType, player.getId());
		      					ArrayList<VoteInfo> voteList = new ArrayList<VoteInfo>();
		      					for(int i = 0; i< voteShowList.size(); i++){
		      						Object[] tempObjects = (Object[]) voteShowList.get(i);
	      							int playerId = (Integer)tempObjects[0];
	      							int point =  ((Long)tempObjects[1]).intValue();
	      							VoteInfo voteInfo = new VoteInfo(playerId, point);
	      							voteList.add(voteInfo);
		      					}
		      					Collections.sort(voteList);
		      					//用于展示的id和字符串
		      					ArrayList<VoteInfoHelp> showMap = new ArrayList<VoteInfoHelp>();
		      					//Map<String, Integer>  showMap = new TreeMap<String, Integer>();
		      					if(voteList == null || voteList.size() == 0 ){
		      						sendMessage("没有人给你投票，赶快拉拢你的朋友给你投票吧", command.getSerial(), command.getSessionId());
		      					}else{
		      						//从数据库中读取id并根据id读取玩家名称。显示明细
		      						int t = 0;
		      						for(int k = 0; k < voteList.size() && t < 10; k++){
		      							VoteInfo voteInfo = (VoteInfo) voteList.get(k);
		      							int playerId = voteInfo.getId();
		      							int point =   voteInfo.getVotePoint();
		      							/*Object[] tempObjects = (Object[]) voteList.get(k);
		      							int playerId = (Integer)tempObjects[0];
		      							int point =  ((Long)tempObjects[1]).intValue();*/
		      							String playNameString = playerService.getPlayerName(playerId);
		      							if(playNameString == null ||(playNameString.equals("") && playNameString.length() == 0)){
		      								continue;
		      							}else{
		      								VoteInfoHelp voteInfoHelp = new VoteInfoHelp(playNameString, point);
		      								showMap.add(voteInfoHelp);
		      							}
		      							t++;
		      						}
		      						Collections.sort(showMap);
		      						//处理因为封号而导致没有查询结果的问题
		      						if(showMap.size() == 0){
		      							sendMessage("很抱歉，无法为你提供明细", command.getSerial(), command.getSessionId());
		      						}else{
			                            //下发排行榜列表
			                            UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
				      	                seg.writeShort((short) 10268);
				      	                seg.writeString(votePlayerGift.getVoteTitle() + "明细排行榜");
				      	                seg.write((byte) 0);
				      	                seg.writeShort((short) Math.min(showMap.size(), 10));
				      	                //seg.writeShort((short)showMap.size());
					      	          	for ( int i= 0; i< showMap.size() && i < 10; i++){
					      	          		VoteInfoHelp voteInfoHelp = showMap.get(i);
						      	          	seg.writeInt(6);
				      	                	seg.writeString(i+1 + "." + voteInfoHelp.getName() + "    " +  voteInfoHelp.getVotePoint()+ "票");
				      	                	seg.writeInt(Utils.CLR_WHITE);
					      	          	}
					      	          	connectService.writeTo(seg, player.getId());
		      						}
		      					}
	      					}else{
	      						sendMessage("你没有参加比赛，不能查看自己的投票明细", command.getSerial(), command.getSessionId());
      						}
      					}
      				}else if(type /10 == 7){//给自己投票，mengjie add 20110428
	      				if(!votePlayerGift.isAvlib()){
	      	        		sendMessage(player.getId(), "暂时没有竞选活动，请稍后再来");
	      	        		return;
	      	        	}
	      				if(type == 71){
		      				int itemId = Integer.parseInt(command.getParam(2));
		      				int count = Integer.parseInt(command.getParam(3));
		      				int playId = Integer.parseInt(command.getParam(4));
		      				log.info("vote start voteId[" + player.getId() + "]vote playId[" + playId + "useItemid["+itemId+"]voteCount[" + count );
		      			
		      				//根据物品查找相应的奖励并加入内存投票榜和数据库
		      				 Vector<VoteGiftDefine> vectorVoteGiftDefine = votePlayerGift.getVoteGiftDefines();
		      				 
		      				 int flag = -1;//用于查找物品是否找到的标志
		      				 for(int i = 0; i < vectorVoteGiftDefine.size() && flag == -1; i++){
		      					TemplateGrid[] needItems = vectorVoteGiftDefine.get(i).getNeedItems();
		      					if(!vectorVoteGiftDefine.get(i).isLevelOK(player.getLevel())){
		      						continue;
		      					}
		      					for(int k = 0; k < needItems.length ; k++){
		      						if(itemId == needItems[k].template.getItemId() && count == needItems[k].count){
		      							flag = i;
		      							break;
		      						}
		      					}
		      				 }
		      				 if(flag == -1){//查找未成功
		      					sendMessage(player.getId(), "没有足够的物品，请稍候再来");
		      				 }else{//获得奖励物品，并记录数据
		      					 TemplateGrid[] needGrid = vectorVoteGiftDefine.get(flag).getNeedItems();
	                             TemplateGrid[] giveGrid = vectorVoteGiftDefine.get(flag).getGiveItems();
	                             if (needGrid.length > 0 && !player.contains(needGrid)) {
	                                 sendMessage("没有足够的物品", command.getSerial(), command.getSessionId());
	                                 return;
	                             }
	                             if (player.isOver(giveGrid)) {
	                                 sendMessage(votePlayerGift.getVoteBag(), command.getSerial(), command.getSessionId());
	                                 return;
	                             }
	                             Changed changed = new Changed();
	                             synchronized (player) {
	                                 player.completeRemoveItem(needGrid, changed);
	                                 player.addItems(giveGrid, changed, player.getClientDataVersion());
	                             }
	
	                             playerService.checkPlayer(player);
	                             sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);
	                             byte isImoneyItem = vectorVoteGiftDefine.get(flag).getIsImoneyItem();;
	                             int addPoint = 0;
	                             for(int w = 0; w < needGrid.length; w++){
	                            	 addPoint = addPoint + vectorVoteGiftDefine.get(flag).getItemsVotePoint(needGrid[w].template.getItemId()) * needGrid[w].count;
	                             }
	                             
	                             //记录内存
	                             ArrayList<VoteInfo> voteTreeSet = voteService.getVoteSet(voteType);
	                             Iterator it =voteTreeSet.iterator();
	                             boolean insertFlag = true;
	                             while(it.hasNext())
	                             {
	                            	 VoteInfo voteInfo = (VoteInfo)it.next(); 
	                            	 if(voteInfo.getId() == playId){//已经存在
	                            		 insertFlag = false;
	                            		 voteInfo.setVotePoint(voteInfo.getVotePoint() + addPoint);
	                            	 }
	                             }
	                             if(insertFlag){
	                            	 VoteInfo voteInfo = new VoteInfo(playId, addPoint);
	                            	 voteTreeSet.add(voteInfo);
	                             }
	                             
	                             final int maxCount = 5;
	                             int length = voteTreeSet.size();
	                             if(length > maxCount){
	                            	 length = maxCount;
	                             }
	                             VoteInfo[] firstVoteInfo = new VoteInfo[length];
	                             for(int i=0; i<length; i++){
	                            	 firstVoteInfo[i] = voteTreeSet.get(i);
	                             }
	                             //记录内存后排序
	                             Collections.sort(voteTreeSet);
	                             
	                             for(int i=0; i<length; i++){
	                            	 VoteInfo info = voteTreeSet.get(i);
	                            	 int index = -1;
	                            	 for(int j=0; j<length; j++){
	                            		 if(firstVoteInfo[j].getId() == info.getId()){
	                            			 //原先的排名
	                            			 index = j;
	                            			 break;
	                            		 }
	                            	 }
	                            	 //在原先的排名中没有该玩家 则给他后面的所有玩家都发消息
	                            	 if(index == -1){
	                            		 for(int n=i; n<length; n++){
		                            		 WorldPlayer playerTemp = playerService.getWorldPlayer(firstVoteInfo[n].getId());
		                            		 if(playerTemp != null && playerTemp.online()){
		                            			 chatService.sendPrivateMessage(-1, "系统", playerTemp.getId(), "您的投票被人超过了，这能忍吗？赶快去抢回来吧！");
		                            		 }
	                            		 }
	                            		 break;
	                            	 }
	                            	 //跟原来不同名次时
	                            	 else if(i != index){
		                            	 //被超过时 给被超过的在线玩家发送私聊
	                            		 for(int n=i; n<length; n++){
	                            			 if(firstVoteInfo[n].getId() != info.getId()){
			                            		 WorldPlayer playerTemp = playerService.getWorldPlayer(firstVoteInfo[n].getId());
			                            		 if(playerTemp != null && playerTemp.online()){
			                            			 chatService.sendPrivateMessage(-1, "系统", playerTemp.getId(), "您的投票被人超过了，这能忍吗？赶快去抢回来吧！");
			                            		 }
	                            			 }
	                            		 }
	                            		 break;
	                            	 }
	                             }
	                             
	                             //插入到vote表里
	                             Vote vote = new Vote();
	                             vote.setCreatetime(new Date());
	                             vote.setPlayeridvoters(playId);
	                             vote.setVotersid(player.getId());
	                             vote.setValid(true);
	                             vote.setVotepoint(addPoint);
	                             vote.setType(voteType);
	                             vote.setIsImoneyItem(isImoneyItem);
	                             voteService.saveVote(vote);
	                             log.info("vote end voteId [" + player.getId() + "] vote playId [" + playId + " ] useItemid [" + itemId + "] voteCount [" + count + "] isImoneyItem [" + (isImoneyItem == 1 ? true : false) + "]");
		      				 
	                             // 投票后获得奖励装备：1个蓝色妖姬中奖几率1/100, 10个蓝色妖姬中奖几率11/100
	                             if (isImoneyItem == 1 && count == 1 && Utils.hit(1, 200)) {
	                            	 IItem iit = Items.getTemplate(201245).newInstance();
	                            	 if (iit != null) {
	                            		 changed = new Changed();
	                            		 IItem nItem = player.completeAddItem(iit, 1, changed, player.getClientDataVersion());
	                            		 if (nItem == null) {
	                            			 byte[] att = ItemUtils.item2dbAttachment(iit, count);
	                            			 mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	                            					 iit.getName() + "*" + 1, "", att, 0, true);
	                            			 sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到您的邮箱!");
	                            		 }
	                            		 sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);
	                            	 }
	                             } else if (isImoneyItem == 1 && count == 10 && Utils.hit(11, 200)) {
	                            	 IItem iit = Items.getTemplate(201245).newInstance();
	                            	 if (iit != null) {
	                            		 changed = new Changed();
	                            		 IItem nItem = player.completeAddItem(iit, 1, changed, player.getClientDataVersion());
	                            		 if (nItem == null) {
	                            			 byte[] att = ItemUtils.item2dbAttachment(iit, count);
	                            			 mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	                            					 iit.getName() + "*" + 1, "", att, 0, true);
	                            			 sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到您的邮箱!");
	                            		 }
	                            		 sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);
	                            	 }
	                             }
		      				 }
	      				}
	      			}
      			}
      		}
      	}
    }
     class GetOnlyRebornPreocessor implements CommandProcessor{//名人堂装备展示
     	public void process(WorldPlayer player, Command command) throws Exception {
     		int type = Integer.parseInt(command.getParam(0));
     		if(1 == type){
     			byte[] bytes = stageService.getTaskBytes((short) 31046, player.getLevel());
                UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
                		 command.getSerial(),
                         command.getSessionId());
                seg.writeShort((short) 31046);
                seg.writeShort((short) 2);
                seg.write(bytes);
                write(seg);
     			//sendMessage(player.getId(),"成功领取属性复生石 ");
     		}else{//领取复生石任务开始
     			//判断领取条件
     			if(friendsService.canGetReborn(player.getId())){
     				//使用天赋组判断是否领取过
     				
     				GiftData giftData = giftService.getPlayerOnlyGiftbyaccountid(300003, player,player.getAccountId()); 
    				OnlyGiftGroup onlyGiftGroup = OnlyGiftGroups.getOnlyGiftGroup(300003);
    				if(onlyGiftGroup != null ){
    					Gift gift = giftData.getGift();
    					Vector<OnlyGiftDefine> onlyGiftDefine = onlyGiftGroup.getGifts();
    					OnlyGiftDefine onlyNeedGiftDefine = onlyGiftDefine.get(0);
    					if(gift.getCount() >= onlyGiftGroup.getMaxCount()){
    						//已经领过
    						sendMessage(player.getId(),"你已经领过一次复生石，一个账户只能领一次");    						
    					}else {//未领过奖励物品
        					//开始发放礼品
            				if(giftData != null ){
            					int  id = type;//或获取属性石
            					if(2 == id){
            						id = 550026;
            					}else if(3 == id){
            						id = 550025;
            					}else if(4 == id){
            						id = 550027;
            					}else if(5 == id){
            						id = 550024;
            					}
            					IItem di = Items.getTemplate(id).newInstance();
            					Changed changed = new Changed();
            					if(player.completeAddItem(di,1,changed, player.getClientDataVersion())!=null){//添加物品
            						sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);	
            					}else{
            						byte[] att = ItemUtils.item2dbAttachment(di, 1);
    	                            mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
    	                                                di.getName() + "*" + 1, "", att, 0, true);
    	                            sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到你的邮箱!");
            					}
            					//添加天赋记录
            					gift.setCount(onlyGiftGroup.getMaxCount());
	                            gift.setRcount(1);
	                            giftService.savePlayerGift(gift);
            				}
        				}
    				}
     				
     			}else{
     				sendMessage(player.getId(),"你不符合领取条件，你符合领取条件后再来吧");
     			}
     		}
     	}
     }
     class DirectWayPreocessor implements CommandProcessor{//名人堂装备展示
        	public void process(WorldPlayer player, Command command) throws Exception {
        		int type = Integer.parseInt(command.getParam(0));
        		if(type/10 == 0){
	        		if(1 == type){//领取每日奖励
		        			//遍历天赋组。查找每日奖励  查找条件为repeat = 1
		        			ConcurrentHashMap<Integer, GiftGroup> giftGroupReference = GiftGroups.getGiftGroupReference();
		        			Vector<GiftGroup> giftVector = new Vector<GiftGroup>();
		        			for(Map.Entry<Integer, GiftGroup> giftGroupsEntry : giftGroupReference.entrySet()){
		        				GiftGroup giftGroup = giftGroupsEntry.getValue();
		        				if(giftGroup.isValid() && giftGroup.isDirectwayCanSee()){//天赋组是否有效及指路宝典是否可见
			        				GiftDefine[] giftDefines = giftGroup.getAvailableGifts(player.getLevel());
			        				if(giftDefines != null && giftDefines.length != 0){
			        					for(int g = 0; g < giftDefines.length; g++){
			        						GiftDefine giftDefine = giftDefines[g];
			        						if(giftDefine != null){//条件搜索
			        							if(giftDefine.getMaxRepeat() == 1 && player.getLevel() >= giftDefine.getBeginLevel() && player.getLevel() <= giftDefine.getEndLevel()){
			        								giftVector.add(giftGroup);
			        							}
			        						}
			        					}
			        				}
		        				}
		        			}
		        			//下发每日奖励
		        			if(giftVector.size() > 0){
		        				UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
		                        seg.writeShort((short) 10251);
		                        seg.writeString("每日奖励列表");
		                        seg.write((byte) 3);
		                        seg.writeShort((short) giftVector.size());
		                        for (int i = 0; i < giftVector.size(); i++) {
		                        	GiftGroup giftGroup = giftVector.get(i);
		                        	GiftDefine[] giftDefines = giftGroup.getAvailableGifts(player.getLevel());
		    	                    seg.writeInt(giftGroup.getId());
		    	                    Gift gift = new Gift();
		    	                    gift.setGroupid(giftGroup.getId());
		    	                    seg.writeString(giftGroup.getMessage_group(gift, giftDefines[0], player));
		    	                    seg.writeInt(Utils.CLR_WHITE);
		                        }
		                        seg.write((byte) 1);
		                        seg.writeString("领取奖励");
		                        seg.writeString("directway 21");   
		                        connectService.writeTo(seg, player.getId());
		        			}else{
		        				sendMessage(player.getId(), "你暂时还不能领取奖励");
		        			}
		        			
		        			
		        		}else if(2 == type){//当前我能做什么
		        			if (player.getMap() == null) {
		                        sendMessage(player.getId(), "请10秒后再试");
		                    } else {
		                        Suggest suggest = null;
		                        boolean find = false;
		                        for (int s = 0; s < Suggest.suggest.size(); s++) {
		                            suggest = (Suggest) Suggest.suggest.elementAt(s);
		                            for (int m = 0; m < suggest.map.length; m++) {
		                                if (suggest.map[m] == player.getMapId()) {
		                                    find = true;
		                                    break;
		                                }
		                            }
		                            if (find)
		                            	break;
		                        }
		                        if (!find) {
		                            sendMessage(player.getId(), "本地指南暂无内容,请联系GM!");
		                            return;
		                        }
		                        Object[] o;
		                        String su = null;
		                        for (int l = 0; l < suggest.level.size(); l++) {
		                            o = (Object[]) suggest.level.elementAt(l);
		                            if (player.getLevel() >= ((Integer) (o[0])).intValue() &&
		                                player.getLevel() <= ((Integer) (o[1])).intValue()) {
		                                su = (String) o[2];
		                                break;
		                            }
		                        }
		                        if (su == null) {
		                            sendMessage(player.getId(), "本地指南暂无内容,请联系GM!");
		                            return;
		                        };
		
		                        String str = MessageFormat.format(su, new Object[] {player.getLevel(),player.getMap().getName()});
		                        //加入可接任务
		                     /*   String str2 = "\n 适合你练级地区";
		                        //返回自己适合的地区id和名称
		                        String taskString ="";
		                        Vector levelArea = Suggest.getLevelAreaId(player.getLevel());
		                        if(levelArea != null && levelArea.size() != 0){
		                        	for(int i=0 ; i < levelArea.size(); i++){
		                        		Object[] levelAreaId = (Object[]) levelArea.get(i);
		                        		//获取场景并获得任务号
		                        		short areaId = (Short)levelAreaId[0];
		                        		short[] taskIds = TaskDefinitions.getDefinitions((short) (areaId * 100));
		                        		//根据任务号获取任务标题；
		                        		if(taskIds != null && taskIds.length != 0){
		                        			String[] names = stageService.getTasksName(taskIds,player.getLevel());
		                        			for(int  k= 0; k < names.length; k++ ){
		                        				taskString = taskString+names[k]+"\n ";
		                        			}
		                        		}
		                        		String areaName= (String) levelAreaId[1];
		                        		str2 = str2 + areaName +":";
		                        	}
		                        }
		                        str2 = str2+ "的任务列表\n";
		                        str2 = str2 + taskString;*/
		                        //末尾加入广告
		                        String str3 = "\n \n升级累了可以到瓦伊特镇嘉宾海滨浴场休闲升级";
		                        UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
		                        seg.writeShort((short) 10250);
		                        seg.writeString("指路宝典");
		                        seg.write((byte) 2);
		                        seg.writeShort((short)1);
		                        seg.writeInt(1);
		                        seg.writeString(str + str3);
//		                        seg.writeString(str + str2 + str3);
		                        seg.writeInt(Utils.CLR_WHITE);
		                        connectService.writeTo(seg, player.getId());
		        			//sendMessage(player.getId(), "当前我能做什么");
		                    }
		        		}else if(3 == type){//幻想活动手册
		        			 byte[] bytes = stageService.getTaskBytes((short) 31003,
		                             new String[] {"1.每日活动\n2.每周活动\n3.下次查看",
		                             "directway 31", "directway 32"});
		                     UWAPSegment seg = new UWAPSegment(ClientConstants.
		                             GET_FILE_OK, command.getSessionId(), player.getId());
		                     seg.writeShort((short) 31003);
		                     seg.writeShort((short) 2);
		                     seg.write(bytes);
		                     connectService.writeTo(seg, player.getId());
		        			//sendMessage(player.getId(), "幻想活动手册");
		        		}else if(4 == type){//幻想精彩玩点介绍
		        			Vector<String> gamePlayVector= Suggest.getGamePlay();
		        			String strings="我们将隆重向你介绍我们的游戏各种特色";
		        			if(gamePlayVector != null && gamePlayVector.size() > 0){		
		        				for (int k = 0; k < gamePlayVector.size(); k++ ){
					        		strings = strings + "\n" +(k+1)+"."+gamePlayVector.get(k);
		        				}
		        			}else{
		        				sendMessage(player.getId(), "很抱歉，数据错误请稍后在查看");
		        				return;
		        			}
		        			String[] sendCommandString = new String[3 + gamePlayVector.size()];
		        			sendCommandString[0] =((Integer)(gamePlayVector.size())).toString();
				        	sendCommandString[1] = "1";
				        	sendCommandString[2] = strings;
				        	for(int k= 0;k < gamePlayVector.size(); k++){
				        		sendCommandString[3+k] = "directway "+(k+41);
				        	}
				        	byte[] bytes = stageService.getTaskBytes((short) 31010, sendCommandString);
				            UWAPSegment seg = new UWAPSegment(ClientConstants.
				                    GET_FILE_OK);
				            seg.writeShort((short) 31010);
				            seg.writeShort((short) 2);
				            seg.write(bytes);
				            connectService.writeTo(seg, player.getId());
		        			//sendMessage(player.getId(), "幻想精彩玩点介绍");
		        		}else if(5 == type){//账号安全
		                	String string="请不要与他人共享你的游戏账号，否则你有可能会失去你的账号；\n1.立刻绑定密码保护\n2.如何修改密码\n3.如何找回账号.\n4.下次查看";
				        	String[] sendCommandString = new String[7];
				        	sendCommandString[0] = "4";
				        	sendCommandString[1] = "1";
				        	sendCommandString[2] = string;
				        	for(int k= 0;k < 4; k++){
				        		sendCommandString[3+k] = "directway "+(k+51);
				        	}
				        	byte[] bytes = stageService.getTaskBytes((short) 31010, sendCommandString);
				            UWAPSegment seg = new UWAPSegment(ClientConstants.
				                    GET_FILE_OK);
				            seg.writeShort((short) 31010);
				            seg.writeShort((short) 2);
				            seg.write(bytes);
				            connectService.writeTo(seg, player.getId());
		        		}else if(6 == type ){//近期游戏公告
		        			Vector<String> gameNoticeVector  = Suggest.getGameNotice();
		        			
		        			if(gameNoticeVector != null && gameNoticeVector.size() > 0){
		        				String strings="在游戏中你可以尽情体验我们能带给你的各种活动";
		        				for (int k = 0; k < gameNoticeVector.size(); k++ ){
					        		strings = strings + "\n" +gameNoticeVector.get(k);
		        				}
		        				sendMessage(player.getId(), strings);
		        			}else{
		        				sendMessage(player.getId(), "游戏中暂时没有活动");
		        			}
		        		}else if(7 == type){//游戏中的一些小窍门
		        			Vector<String> gameTipsVector  = Suggest.getGameTip();
		        			
		        			if(gameTipsVector != null && gameTipsVector.size() > 0){
		        				String strings="在游戏中你可以尽情体验我们能带给你的便利的操作";
		        				for (int k = 0; k < gameTipsVector.size(); k++ ){
					        		strings = strings + "\n" +gameTipsVector.get(k);
		        				}
		        				sendMessage(player.getId(), strings);
		        			}else{
		        				sendMessage(player.getId(), "游戏中暂时没有小窍门");
		        			}
		        		}else if(8 == type){//当前游戏时间	
		        			Date d = new Date(); 
		        			SimpleDateFormat sdf = new SimpleDateFormat("HH时mm分ss秒 yyyy年MM月dd日");//时:分:秒:毫秒 
		        			sendMessage(player.getId(), "当前游戏时间为"+ sdf.format(d));
		        		}
        		}else{//二级菜单
        			if(3 == type/10){
	        			if(31 == type){//每日活动
	        				 byte[] bytes = stageService.getTaskBytes((short) 31003,
		                             new String[] {"1.个人试炼场于每日12、16、19、22点开放；" +
		        						"（建议到达20级以后再去参与试炼场，通过瓦伊特镇战场NPC传送）;\n2. "+
			        					"智力问答于每日12、20点开放；（答对就给经验哦，建议任意级别参与，到海滨浴场于智力水晶对话开始答题）\n3.下次查看"  ,
		                             "directway 91", "directway 92"});
		                     UWAPSegment seg = new UWAPSegment(ClientConstants.
		                             GET_FILE_OK, command.getSessionId(), player.getId());
		                     seg.writeShort((short) 31003);
		                     seg.writeShort((short) 2);
		                     seg.write(bytes);
		                     connectService.writeTo(seg, player.getId());
	        				/*sendMessage(player.getId(), "1.个人试炼场于每日12、16、19、22点开放；" +
	        						"（建议到达20级以后再去参与试炼场，通过瓦伊特镇战场NPC传送）;\n2. "+
	        					"智力问答于每日12、20点开放；（答对就给经验哦，建议任意级别参与，到海滨浴场于智力水晶对话开始答题）");*/
	        			}else if( 32 ==type){//每周活动
	        				 byte[] bytes = stageService.getTaskBytes((short) 31003,
		                             new String[] {"1.公会试炼场于每周2、4、6、中午13点，晚上20点开放；" +
	        						"（公会试炼场可以获得公会荣誉，公会荣誉可以用于岛屿拍卖，拍卖成功可是全公会都可以享受优惠哦）;\n2. " +
	        						"岛屿争夺于每周2晚上7.30-11点开放，（获得岛屿争夺权的公会将会享受购买道具0."+Discount.TONGDISCOUNT+"的折扣、击杀岛屿BOSS、高级浴场等诸多特殊优惠哦）\n3.下次查看"  ,
		                             "directway 91", "directway 92"});
		                     UWAPSegment seg = new UWAPSegment(ClientConstants.
		                             GET_FILE_OK, command.getSessionId(), player.getId());
		                     seg.writeShort((short) 31003);
		                     seg.writeShort((short) 2);
		                     seg.write(bytes);
		                     connectService.writeTo(seg, player.getId());
	        				/*sendMessage(player.getId(), "1.公会试炼场于每周2、4、6、中午12点，晚上20点开放；" +
	        						"（公会试炼场可以获得公会荣誉，公会荣誉可以用于岛屿拍卖，拍卖成功可是全公会都可以享受优惠哦）;\n2. " +
	        						"岛屿争夺于每周2晚上7.30-11点开放，（获得岛屿争夺权的公会将会享受购买道具0."+Discount.TONGDISCOUNT+"的折扣、击杀岛屿BOSS、高级浴场等诸多特殊优惠哦）");*/
	        			}
        			}else if(2 == type/10){
	        				if(21 == type){//领取每日奖励
		        				int giftGroupId = Integer.parseInt(command.getParam(1));;
		        				GiftData gDatas =  giftService.getPlayerGift(giftGroupId, player);
		
		                        if(gDatas == null){
		                        	sendMessage(player.getId(), "数据异常请稍候再试" );
		                            return;
		                        }
		                        UWAPSegment seg = giftService.getGiftGroupSegemntOnly(gDatas, command.getSerial(), command.getSessionId());
		                        write(seg);
	        				}//sendMessage(player.getId(), "领取了每日奖励哦");
        			}else if(5 ==type/10){
	        				if(51 == type){//密码账户绑定
	        				byte[] bytes = stageService.getTaskBytes((short) 31030,player.getAccountId());
	                        UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
	                        		command.getSerial(),
	                        		command.getSessionId());
	                        seg.writeShort((short) 31030);
	                        seg.writeShort((short) 2);
	                        seg.write(bytes);
	                        write(seg);
		        			}else if(52 == type || 53 == type ){//修改密码提示
		        				int typeSelect = type % 10;
		        				Vector<String> gameSafe= Suggest.getGameSafeInfo();
		        				String gameSafeInfo = gameSafe.get((typeSelect-2));
		        				if(gameSafeInfo != null && gameSafeInfo.length() >0){
		        					sendMessage(player.getId(), gameSafeInfo);
		        				}else{
		        					sendMessage(player.getId(), "对不起，暂时没有内容，请稍后再查看");
		        				}
		        			}
        			}else if( 4 == type/10){
        				int typeSelect = type % 10;
        				Vector<String> gamePlayContent = Suggest.getGameContents();
        				String content = gamePlayContent.get((typeSelect-1));
        				if(content != null && content.length() >0){
        					sendMessage(player.getId(), content);
        				}else{
        					sendMessage(player.getId(), "对不起，暂时没有介绍，请稍后再查看");
        				}
        			}
        			
        		}
        		//sendMessage(player.getId(),"指路宝典 ");
        	}
     }
     class IbuyTop10EquipPreocessor implements CommandProcessor{//名人堂装备展示
       	public void process(WorldPlayer player, Command command) throws Exception {
       		//sendMessage(player.getId(),"猪猪，我很想你 ");
       		int type = Integer.parseInt(command.getParam(0));
       		if(1 == type){//用于上传名人装备
       			//1穿在身上自动上传。2在列表中选中进行上传
       		 byte[] bytes = stageService.getTaskBytes((short) 31003,
                     new String[] {"1.身上的装备自动上传\n2.装备手动上传\n3.取消上传",
                     "ibuytop10equip 3", "ibuytop10equip 4"});
             UWAPSegment seg = new UWAPSegment(ClientConstants.
                     GET_FILE_OK, command.getSessionId(), player.getId());
             seg.writeShort((short) 31003);
             seg.writeShort((short) 2);
             seg.write(bytes);
             connectService.writeTo(seg, player.getId());
       		//sendMessage(player.getId(),"猪猪1，我很想你 ");
       		}else if(2 == type){//用于瞻仰名人装备
       			//手动输入名人的次序（1-10名）
       			byte[] bytes = stageService.getTaskBytes((short) 31001,
                        new String[] {"输入名人堂中的排名号即可查看对应排名的名人装备（排名分别为1-10）\n1.输入排名\n2.下次查看", "请输入1—10之间的数字",
                        "ibuytop10equip 6 "});
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                        GET_FILE_OK, command.getSessionId(), player.getId());
               seg.writeShort((short) 31001);
                seg.writeShort((short) 2);
                seg.write(bytes);
                connectService.writeTo(seg, player.getId());
       		}else if(3 == type){
       			if(!topListService.playerTopList.isFamuous(player.getId())){
       				sendMessage(player.getId(), "请确定自己是名人后稍候再试");
       				return;
       			}
       			//自动获取身上装备
       			IEquipment[] usedEquipmentsTemp= player.getUsedEquipments();
       			IEquipment[] usedEquipments = new IEquipment[9];
	            for(int i = 0; i< usedEquipmentsTemp.length; i++){
	            	if(null !=usedEquipmentsTemp[i]){
	            		usedEquipments[i] = usedEquipmentsTemp[i];
	            	}
	            }
	            if(topListService.playerTopList.setPlayerIbuyTopPlayerEquip(player.getId(), usedEquipments)){
	            	sendMessage(player.getId(),"自动上传已经成功");
	            }else{
	            	sendMessage(player.getId(),"自动上传失败，请确定自己是名人后再试");
	            }
       		}else if(4 == type){
       			//检查是否是名人
       			if(!topListService.playerTopList.isFamuous(player.getId())){
       				sendMessage(player.getId(), "请确定自己是名人后稍候再试");
       				return;
       			}
       			//装备在身上的装备查找发放
       			IEquipment[] usedEquipmentsTemp= player.getUsedEquipments();
       			IEquipment[] usedEquipments = new IEquipment[9];
       			int count = 0;
	            for(int i = 0; i< usedEquipmentsTemp.length; i++){
	            	if(null !=usedEquipmentsTemp[i]){
	            		usedEquipments[count] = usedEquipmentsTemp[i];
	            		count++;
	            	}
	            }
	            //背包内背包上传发放
	        	Grid[] grids = player.getEquipments(); 
	            if( 0 != (count + grids.length )){//背包和身上都有装备
	            	if(player.getClientDataVersion() > 0){
	            		UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL, command.getSerial(), command.getSessionId());
	            		seg.writeShort(ClientConstants.EXTEND_PROTOCL_GENERLIST);
	            		seg.writeShort((short) 10248);
	            		seg.write((byte) 3);
	            		seg.writeString("装备上传列表");
	            		 seg.writeShort((short) (count +  grids.length));
				            for(int k =0; k< count; k++){
				            	if(null !=usedEquipments[k]){
					            	IEquipment iequipment = usedEquipments[k];
					            	iequipment.setDataVersion(player.getClientDataVersion());
					            	seg.write(iequipment.getType());
					            	seg.write(iequipment.toClientBytesWithLevel(player.getLevel()));
				            	}
				            }
				            if(grids.length > 0){
				            	IEquipment item;
				    			Grid grid ;
					            for(int p = 0; p < grids.length ; p++){
					            	grid =  grids[p];
				   					item = (IEquipment) grid.item;
				   					item.setDataVersion(player.getClientDataVersion());
					            	seg.write(item.getType());
					            	seg.write(item.toClientBytesWithLevel(player.getLevel()));
					            }
					            
				            }
				            seg.write((byte) 1);
		                    seg.writeString("上传装备");
		                    seg.writeString("ibuytop10equip 5");
	            		connectService.writeTo(seg, player.getId());
	            	}else{
	            		UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
			       		seg.writeShort((short) 10248);
		                seg.writeString("装备上传列表");
		                seg.write((byte) 3);
			            seg.writeShort((short) (count +  grids.length));
			            for(int k =0; k< count; k++){
			            	if(null !=usedEquipments[k]){
				            	IEquipment iequipment = usedEquipments[k];
				            	seg.writeInt(iequipment.getId());
								seg.writeString(iequipment.getName());
								seg.writeInt(Utils.CLR_EQUIP[iequipment.getQuality()]);
			            	}
			            }
			            if(grids.length > 0){
			            	IEquipment item;
			    			Grid grid ;
				            for(int p = 0; p < grids.length ; p++){
				            	grid =  grids[p];
			   					item = (IEquipment) grid.item;
			   					seg.writeInt(item.getId());
								seg.writeString(item.getName());
								seg.writeInt(Utils.CLR_EQUIP[item.getQuality()]);
				            }
				            
			            }
			            seg.write((byte) 1);
	                    seg.writeString("上传装备");
	                    seg.writeString("ibuytop10equip 5");
			            connectService.writeTo(seg, player.getId());
	            	}
	            }else{//装备异常
	            	sendMessage(player.getId(),"选择错误，没有装备，请重新选择");
	            }
       			//sendMessage(player.getId(),"手动上传装备");
       		}else if(5 == type){//装备已经上传，开始做限制
       	   		int id = Integer.parseInt(command.getParam(1));
          		Grid[] grids = player.getEquipments(); 
          		boolean findSucess= false; //周年装是否成功；
          		Grid grid  = new Grid();
          		IEquipment item = null;
          		//log.info("ID["+player.getId()+"]equId["+id+"]unhenceYearEquip Try");
                //mengjie add 精炼精华与精炼石数量相同
          		for(int i=0; i<grids.length ;i++){
          			grid =  grids[i];
    			 	item = (IEquipment) grid.item;
    			 	if(id == item.getId()){
    			 		findSucess = true;
    			 		break;
    			 	}
          		}
          		if(!findSucess){//身上装备查找
          			IEquipment[] iEquipments = player.getUsedEquipments();
          			for(int t=0; t < iEquipments.length; t++){
          				if(null !=iEquipments[t]){
          					item = iEquipments[t];
			            	if(item.getId() == id){
			            		findSucess = true;
			            		break;
			            	}
		            	}
          			}
          		}
          		if(!findSucess){
          			sendMessage(player.getId(),"没有找到该装备，请稍候再试");
          		}else{
          			if(item.getLevel() > player.getLevel()){
          				sendMessage(player.getId(),"不能上传大于自己级别的装备");
          			}else{//生成临时的用于展示的装备
          				IEquipment[] tempEquipments = player.getUsedEquipments();
          				IEquipment ieEquipment;
          				boolean flag = false;//用于查找装备部位是否有该物品
          				for(int j = 0; j < tempEquipments.length; j++){
          					ieEquipment = tempEquipments[j];
          					if(null !=ieEquipment){
	          					if(ieEquipment.getPart() == item.getPart()){//找到并取代该部位
	          						tempEquipments[j] = item;
	          						flag = true;
	          					}
          					}
          				}
          				if(!flag){
          					tempEquipments[item.getPart()] = item;
          				}
          				if(topListService.playerTopList.setPlayerIbuyTopPlayerEquip(player.getId(), tempEquipments)){
        	            	sendMessage(player.getId(),"手动上传已经成功");
        	            }else{
        	            	sendMessage(player.getId(),"手动上传失败，请确定自己是名人后稍候再试");
        	            }
          			}
          		
          		}
          	
       			//sendMessage(player.getId(),"手动上传装备成功");
       		}else if(6 == type){//确定瞻仰名人顺序。并瞻仰
       			int id;
       			try {
       				id = Integer.parseInt(command.getParam(1));
				} catch (Exception e) {
					// TODO: handle exception
					sendMessage(player.getId(),"输入的数字有误，请重新输入");
					return;
				}
       			id = id - 1;//输入是1到10需要减去1
       			if(id >= 0 && id <= 9){//获取装备
       				IEquipment[] euqips = topListService.playerTopList.getPlayerIbuyTopPlayerEquip(id);
           			if(euqips == null || euqips.length == 0){
           				sendMessage(player.getId(),"名人装备展示错误 ");
           			}else{//下发装备列表，进行展示 
               			IEquipment[] usedEquipments = new IEquipment[9];
               			int count = 0;
        	            for(int i = 0; i< euqips.length; i++){
        	            	if(null !=euqips[i]){
        	            		usedEquipments[count] = euqips[i];
        	            		count++;
        	            	}
        	            }
        	            if( 0 != count){//身上穿着装备
        	            	String sringPlayerNameString = topListService.playerTopList.getPlayerName(id);
        		       		UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
        		       		seg.writeShort((short) 10249);
        	                seg.writeString("名人"+sringPlayerNameString+"装备列表");
        	                seg.write((byte) 0);
        		            seg.writeShort((short) count);
        		            for(int k =0; k< count; k++){
        		            	if(null !=usedEquipments[k]){
        			            	IEquipment iequipment = usedEquipments[k];
        			            	seg.writeInt(iequipment.getId());
        							seg.writeString(iequipment.EQUIP_TYPE_NAME[iequipment.getPart()]+" : "+iequipment.getName());
        							seg.writeInt(Utils.CLR_EQUIP[iequipment.getQuality()]);
        		            	}
        		            }
        		            try{
                                //TODO must delete by sky
                                Thread.sleep(100);
                            }catch(Exception e){
                            }
        		            connectService.writeTo(seg, player.getId());
        	            }else{//装备异常
        	            	sendMessage(player.getId(),"该名人没有装备，请重新选择");
        	            }
           			}
       			}
       			else{
           			sendMessage(player.getId(),"输入错误，稍候请重新输入");
           		}
       		}else{
       			sendMessage(player.getId(),"选择错误，请重新选择");
       		}
       	}
     }
     
     class sellAttachementEquipPreocessor implements CommandProcessor{
     	public void process(WorldPlayer player, Command command) throws Exception {
     		int clearId = Integer.parseInt(command.getParam(0));
     		if(1 == clearId){//清理指定的背包
     			int itemId = Integer.parseInt(command.getParam(1));
     			Grid[] grids = player.getEquipments();
     			Grid grid, grid2 = null;
     			for(int i = 0; i < grids.length; i++){
     				grid = grids[i];
     				if(grid.item.getId() == itemId){
     					grid2 = grid;
     					break;
     				}
     			}
     			if( grid2 == null){
     				sendMessage(player.getId(),"你背包里没有该物品，请确认背包里面有再来 ");
     			}else{
     				IEquipment item;
     				item = (IEquipment) grid2.item;
     				if(Utils.CLR_EQUIP[item.getQuality()] == Utils.CLR_WHITE || Utils.CLR_EQUIP[item.getQuality()] == Utils.CLR_GREEN){
     					Changed changed = new Changed();
     					if(player.completeRemoveItem(item.getItemId(), item.getId(), changed)!=null){//物品扣成功了
     						int price = ((IValuableItem) item).getPrice();
                    		player.addMoney(price, changed);
                    		sendGetItem(changed, command.getSerial(), command.getSessionId(),
     	                           (byte) 17);
                    		log.info("ID["+player.getId()+"] sellBagEquId["+itemId+"] Ok");
     					}
     				}else{
     					sendMessage(player.getId(),"你背包里 的装备不是白装和绿装，请确认后再来 ");
     				}
     			}
     		}else if(2 == clearId){//清理指定的邮件
	     		int mailId = Integer.parseInt(command.getParam(1));
	     		if (player != null) {
	                Mail mail = mailService.getMail(mailId);           
	                //删除邮件
	                if (mail != null) {
	                	 byte[] item = mail.getAttachment();//提取附件
	                     if (item == null || item.length == 0) {
	                    	sendMessage(player.getId(),"你的邮件没有附件，请确认附件没有提取，然后再来卖出附件");
	                         //throw new MailException("没有附件");
	                     }else{
		                    if (player.getId() != mail.getDestId()) {//确认是发给玩家的
		                        log.info("DeleteMail Error ID[" + player.getId() + "] DestId[" + mail.getDestId() + "]");
		                        return;
		                    } else {
		                    	int itemId = ItemUtils.getAttachementEquId(item);
		                    	if(-1 == itemId ){
		                    		sendMessage(player.getId(),"你邮件里面没有白色和绿色的装备，请确认后再来");
		                    	}else {
		                    		log.info("ID["+player.getId()+"] sellMailAttachmentId["+mail.getId()+"] Ok");
		                    		//player.getAttchmentList().remove(mail);
		                    		mailService.deleteMail(mail);
		                    		
		                    		IItem item2 = Items.getTemplate(itemId).newInstance();//获得装备名称。获取价格，返还
		                    		int price = ((IValuableItem) item2).getPrice();
		                    		Changed changed = new Changed();
		                    		player.addMoney(price, changed);
		                    		sendGetItem(changed, command.getSerial(), command.getSessionId(),
		     	                           (byte) 17);
		                    	}
		                        /*Utils.log(log, player.getId(), data.getAppType(),
		                                  "Money[" + player.getMoeny() + "]OK");*/
		                    }
	                     }
	                }
	            }
     		}
     		}
     	}
     class unhencePlainEquipProcessor implements CommandProcessor{
       	public void process(WorldPlayer player, Command command) throws Exception {
       		if (player != null) {
       			synchronized (player) {
       				int flag = Integer.parseInt(command.getParam(0));
       				int id = Integer.parseInt(command.getParam(1));
       				Grid[] grids = player.getEquipments(); 
       				boolean findSucess= false; //周年装是否成功；
       				Grid grid  = new Grid();
       				
       				IEquipment item = null;
       				for(int i=0; i<grids.length ;i++){
       					grid =  grids[i];
       					item = (IEquipment) grid.item;
       					if(id == item.getId()){
       						findSucess = true;
       						break;
       					}
       				}
       				if(findSucess){
       					int itemid  = item.getItemId(); 
       					Changed changed = new Changed();
       					List<Enhance> enhances  = item.getEnhances();
       					if(enhances.size() >= 1 && enhances.size() <= 9){
       						log.info("ID["+player.getId()+"]equId["+id+"]itemsid["+itemid+"],count["+enhances.size()+"]unhencePlainEquip Try");
       						if (player.getMoeny() < enhances.size() * 1000){
       							sendMessage(player.getId(),"您没有那么多钱来分解！");
       							return ;
       						}else{
       							if(flag == 1){
       								String msg =item.getName() + "分解后将会消失。你要继续么？\n1.分解\n2.暂不分解";
       								byte[] bytes = stageService.getTaskBytes((short) 31040,
       										new String[] {msg, "unhancePlainEquip 2 " + id,"随时为你服务！"});
       								UWAPSegment seg = new UWAPSegment(ClientConstants.
       										GET_FILE_OK);
       								seg.writeShort((short) 31025);
       								seg.writeShort((short) 2);
       								seg.write(bytes);
       								connectService.writeTo(seg, player.getId());
       							}else if(flag == 2){
       								if(player.completeRemoveItem(itemid,id,changed)!=null){//物品扣成功了
       									player.decMoney(enhances.size() * 1000, changed);
       									int mailcount = 0;
       									//IItem di = Items.getTemplate(211002).newInstance();
       									IItem di = Items.getTemplate(550034).newInstance();
       									
       									String str_count = "";
       									for(int k = 0; k<enhances.size(); k++){//循环加精炼石
       										str_count = str_count + " " + String.valueOf(enhances.get(k).getProperty());
       										if(player.completeAddItem(di,3,changed, player.getClientDataVersion())==null){
       											mailcount++;
       										}
       									}
       									if (mailcount>0){
       										byte[] att = ItemUtils.item2dbAttachment(di, mailcount * 3);
       										mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
       												di.getName() + "*" + mailcount, "", att, 0, true);
       										sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到你的邮箱!");
       									}
       									sendMessage(player.getId(),"装备"+item.getName()+"已分解！");
       									sendGetItem(changed, command.getSerial(), command.getSessionId(),
       											(byte) 17);
       									log.info("ID["+player.getId()+"]equId["+id+"]itemsid["+itemid+"],count["+enhances.size()+"],["+str_count+"]unhencePlainEquip Ok");
       								}
       							}else{
       								sendMessage(player.getId(),"装备分解失败。请重新选择分解");
       							}
       						}
       					}else{
       						sendMessage(player.getId(),"未找到装备，请确认背包内含有此装备再来");
       					}
       				}else{
       					sendMessage(player.getId(),"您选择的装备不存在，请确认背包内含有此装备再来");
       				}
       				//sendMessage(player.getId(),"非周年星装分解成功");
       			}
       		}
       	}
     }
     
     /**
      * 
      * @author hchen
      *	petversion >= 4 取消炼化功能
      *
      */
    /*class EnahancePetStartProcessor implements CommandProcessor{
    	 private Random rnd = new Random();
    	 
    	public void process(WorldPlayer player, Command command) throws Exception {
    		int petId = Integer.parseInt(command.getParam(0));
    		int addpoint =Integer.parseInt(command.getParam(1));
    		Pet[] pets=player.getPets();
    		if((pets == null) || (pets.length ==0)){
    			sendMessage("你还没有宠物。请有了宠物再来吧", command.getSerial(), command.getSessionId());
    		}else{
	    		Pet pet = player.getPet(petId);
	    		if(pet != null){
	    			PetEnhance[] petEnhanceTemaPlate = PetEnhance.getPetEnhance(addpoint);
	    			int[] itemId=new int[petEnhanceTemaPlate.length];		
	    			for(int i=0;i<petEnhanceTemaPlate.length;i++){
	    				itemId[i] = petEnhanceTemaPlate[i].getItemId();
	    			}
	    			int[] itemCount = new int[itemId.length];
	    			for(int i=0;i<itemId.length;i++){
	    				itemCount[i]=player.getItemCount(itemId[i]);
	    			}
	    			int needCount = pet.getLevel()/20 + pet.getCurrentEnchancePoint()/4+ 1;
	    			int currentCount = 0;
	    			for(int i=0;i<itemCount.length;i++){
	    				currentCount=currentCount+itemCount[i];
	    			}
	    			if(currentCount>=needCount){//精华数量够精炼
	    				int probability = Utils.getEnhancePetProbability(pet);
		    			if(probability >= 100 || Utils.hit(rnd, probability, 100)){//命中，可以精炼宠物了
		    				if(pet.getmaxEnchancePoint()==0){//第一次精炼
		    					//player.removePet(pet);
		    					int tempCount =needCount;
		    					Changed changed = new Changed();
		    					boolean removeFlag =false;
		    					if(itemCount[0]>=tempCount){//减物品
		    						 player.completeRemoveItem(itemId[0], tempCount, changed);
		    						 removeFlag = true;
		    					}else{
		    						if(itemCount[0]!=0){
		    							player.completeRemoveItem(itemId[0], itemCount[0], changed);
		    						}
		    						tempCount =tempCount-itemCount[0];
		    						if(itemCount[1]>=tempCount){
		    							player.completeRemoveItem(itemId[1], tempCount, changed);
		    							removeFlag = true;
		    						}else{
		    							if(itemCount[1]!=0){
			    							player.completeRemoveItem(itemId[1], itemCount[1], changed);
			    						}
			    						tempCount =tempCount-itemCount[1];
			    						if(itemCount[2]>=tempCount){
			    							player.completeRemoveItem(itemId[2], tempCount, changed);
			    							removeFlag = true;
			    						}else{
			    							if(itemCount[2]!=0){
				    							player.completeRemoveItem(itemId[2], itemCount[2], changed);
				    						}
			    							tempCount =tempCount-itemCount[2];
			    							player.completeRemoveItem(itemId[3], tempCount, changed);
			    							removeFlag = true;
			    							
			    						}
		    						}
		    					}
		            			if(removeFlag){//减物品成功
		            				pet.addEnhance(petEnhanceTemaPlate[0]);
			    					pet.addPoint(addpoint);
			    					//int tempMaxEnchancePoint=Utils.getCount(rnd,20,40);
			    					//tempMaxEnchancePoint = tempMaxEnchancePoint + rnd.nextInt(20);
			    					pet.setmaxEnchancePoint(Utils.getCount(rnd,21,40));
			    					pet.setEnhanceName("(1星)");
			    					if(addpoint==1){
			    						changed.addPetProperty(pet, Changed.PET_STRENGTH, Utils.getEnhanceRation(1,pet.getCurrentEnchancePoint()));
			    			    	}else if(addpoint==2){
			    			    		changed.addPetProperty(pet, Changed.PET_INTELLIGENCE, Utils.getEnhanceRation(2,pet.getCurrentEnchancePoint()));
			    			    	}else if(addpoint==3){
			    			    		changed.addPetProperty(pet, Changed.PET_VITALITY, Utils.getEnhanceRation(3,pet.getCurrentEnchancePoint()));
			    			    	}else if(addpoint == 4){
			    			    		changed.addPetProperty(pet, Changed.PET_AGILITY, Utils.getEnhanceRation(4,pet.getCurrentEnchancePoint()));
			    			    	}   
			    					changed.addPetProperty(pet, Changed.PET_NAME,
	                                           pet.getName()+pet.getEnhanceName());
			    					Utils.log(log, player.getId(), command.getAppType(),
			                                  "enhance pet success pet["+pet.getId() +"]pet["+ Utils.getHexdump(pet.toClientBytes()) +
			                                  "]success enhance point"+addpoint);
			    					sendMessage("恭喜你顺利炼化出"+pet.getCurrentEnchancePoint()+"星级 宠物。", command.getSerial(), command.getSessionId());
			    					//sendMessage("恭喜你顺利炼化出"+
	                                //            pet.getCurrentEnchancePoint()+"星级 宠物。为了奖励你，送你2周年纪念装备哦~", command.getSerial(), command.getSessionId());
				                    //sendGetItem(changed, command.getSerial(), command.getSessionId(),
				                    //            (byte) 17);
				                    //chatService.sendPrivateMessage(-1,"系统",player.getId(),
	        						//			"恭喜您，炼化宠物成功，"+pet.getName()+"已打造为"+pet.getCurrentEnchancePoint()+"星！请查收送给您的2周年纪念装备~");
			    					//sendMessage("已经初次炼化", command.getSerial(), command.getSessionId());
				                    //活动，送周年链 mengjie add
				                    //IItem item = player.completeAddItem(Items.getTemplate(1001234).newInstance(), 1, changed);
		                            //if (item == null) {
		                            //    byte[] att = ItemUtils.item2dbAttachment(Items.getTemplate(1001234).newInstance(),1);
		                            //    mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
		                            //    		Items.getTemplate(1001234).newInstance().getName() + "*" + 1, "", att, 0, true);
		                            //    sendMessage("由于背包满，物品已经邮寄到邮箱中，请注意查收。", command.getSerial(), command.getSessionId());
		                            //}
		                            sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 17);
		            			}
		    				}else{//非第一次精炼
		    					if(pet.getCurrentEnchancePoint() < pet.getmaxEnchancePoint()){
		    						
			    					//player.removePet(pet);
			    					Changed changed = new Changed();
			            			boolean removeFlag =false;
			            			int tempCount =needCount;
			    					if(itemCount[0]>=tempCount){//减物品
			    						 player.completeRemoveItem(itemId[0], tempCount, changed);
			    						 removeFlag = true;
			    					}else{
			    						if(itemCount[0]!=0){
			    							player.completeRemoveItem(itemId[0], itemCount[0], changed);
			    						}
			    						tempCount =tempCount-itemCount[0];
			    						if(itemCount[1]>=tempCount){
			    							player.completeRemoveItem(itemId[1], tempCount, changed);
			    							removeFlag = true;
			    						}else{
			    							if(itemCount[1]!=0){
				    							player.completeRemoveItem(itemId[1], itemCount[1], changed);
				    						}
				    						tempCount =tempCount-itemCount[1];
				    						if(itemCount[2]>=tempCount){
				    							player.completeRemoveItem(itemId[2], tempCount, changed);
				    							removeFlag = true;
				    						}else{
				    							if(itemCount[2]!=0){
					    							player.completeRemoveItem(itemId[2], itemCount[2], changed);
					    						}
				    							tempCount =tempCount-itemCount[2];
				    							player.completeRemoveItem(itemId[3], tempCount, changed);
				    							removeFlag = true;
				    							
				    						}
			    						}
			    					}
			    					if(removeFlag){//减物品成功
			    						pet.addEnhance(petEnhanceTemaPlate[0]);
				    					pet.addPoint(addpoint);
				    					if(pet.getCurrentEnchancePoint() < pet.getmaxEnchancePoint()){
				    						pet.setEnhanceName("("+pet.getCurrentEnchancePoint()+"星)");
				    					}else{
				    						pet.setEnhanceName("("+pet.getCurrentEnchancePoint()+"星满)");
				    					}
				            			//changed.addItem(pet, 1);
				    					if(addpoint==1){
				    						changed.addPetProperty(pet, Changed.PET_STRENGTH, Utils.getEnhanceRation(1,pet.getCurrentEnchancePoint()));
				    			    	}else if(addpoint==2){
				    			    		changed.addPetProperty(pet, Changed.PET_INTELLIGENCE, Utils.getEnhanceRation(2,pet.getCurrentEnchancePoint()));
				    			    	}else if(addpoint==3){
				    			    		changed.addPetProperty(pet, Changed.PET_VITALITY, Utils.getEnhanceRation(3,pet.getCurrentEnchancePoint()));
				    			    	}else if(addpoint == 4){
				    			    		changed.addPetProperty(pet, Changed.PET_AGILITY, Utils.getEnhanceRation(4,pet.getCurrentEnchancePoint()));
				    			    	} 
				    					changed.addPetProperty(pet, Changed.PET_NAME,
		                                           pet.getName()+pet.getEnhanceName());
				                        Utils.log(log, player.getId(), command.getAppType(),
				                                  "enhance pet success pet["+pet.getId() +"]pet["+ Utils.getHexdump(pet.toClientBytes()) +
				                                  "]success enhance point"+addpoint);
				                        if(pet.getCurrentEnchancePoint() == 10 || pet.getCurrentEnchancePoint() == 20 || pet.getCurrentEnchancePoint() == 30
				                        		|| pet.getCurrentEnchancePoint() == 40){
				                        	chatService.sendWorldMessage( -1, "系统",
		                                            "恭喜" + player.getPlayerName() + "顺利炼化出"+
		                                            pet.getCurrentEnchancePoint()+"星级 宠物");
				                        }
				                        //活动，送周年戒 mengjie add
				                        //if (pet.getCurrentEnchancePoint() == 5){
				                        //	IItem item = player.completeAddItem(Items.getTemplate(1001233).newInstance(), 1, changed);
				                        //    if (item == null) {
				                        //        byte[] att = ItemUtils.item2dbAttachment(Items.getTemplate(1001233).newInstance(),1);
				                        //        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
				                        //        		Items.getTemplate(1001233).newInstance().getName() + "*" + 1, "", att, 0, true);
				                        //        sendMessage("由于背包满，物品已经邮寄到邮箱中，请注意查收。", command.getSerial(), command.getSessionId());
				                        //    }
				                        //    sendMessage("恭喜你顺利炼化出"+
		                                //            pet.getCurrentEnchancePoint()+"星级 宠物。为了奖励你，送你2周年纪念装备哦~", command.getSerial(), command.getSessionId());
					                    //    chatService.sendPrivateMessage(-1,"系统",player.getId(),
		        						//			"恭喜您，炼化宠物成功，"+pet.getName()+"已打造为"+pet.getCurrentEnchancePoint()+"星！请查收送给您的2周年纪念装备~");
				                        //}else{
				                        //	sendMessage("恭喜你顺利炼化出"+
		                                //            pet.getCurrentEnchancePoint()+"星级 宠物", command.getSerial(), command.getSessionId());
					                    //    //sendGetItem(changed, command.getSerial(), command.getSessionId(),
					                    //    //        (byte) 17);
					                    //    chatService.sendPrivateMessage(-1,"系统",player.getId(),
		        						//			"恭喜您，炼化宠物成功，"+pet.getName()+"已打造为"+pet.getCurrentEnchancePoint()+"星！");
				                        //}
				                        sendMessage("恭喜你顺利炼化出"+
	                                            pet.getCurrentEnchancePoint()+"星级 宠物", command.getSerial(), command.getSessionId());
			                            sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 17);
			    					}
		    					}else{
		    						sendMessage("宠物炼化级别已经满了，无法炼化。", command.getSerial(), command.getSessionId());
		    					}
		    				}
		    			}else{//未命中 
		    				sendMessage("此次炼化失败，精炼材料消失，宠物星级没有增长！", command.getSerial(), command.getSessionId());
    						Changed changed = new Changed();
	            			boolean removeFlag =false;
	            			int tempCount =needCount;
	    					if(itemCount[0]>=tempCount){//减物品
	    						 player.completeRemoveItem(itemId[0], tempCount, changed);
	    						 removeFlag = true;
	    					}else{
	    						if(itemCount[0]!=0){
	    							player.completeRemoveItem(itemId[0], itemCount[0], changed);
	    						}
	    						tempCount =tempCount-itemCount[0];
	    						if(itemCount[1]>=tempCount){
	    							player.completeRemoveItem(itemId[1], tempCount, changed);
	    							removeFlag = true;
	    						}else{
	    							if(itemCount[1]!=0){
		    							player.completeRemoveItem(itemId[1], itemCount[1], changed);
		    						}
		    						tempCount =tempCount-itemCount[1];
		    						if(itemCount[2]>=tempCount){
		    							player.completeRemoveItem(itemId[2], tempCount, changed);
		    							removeFlag = true;
		    						}else{
		    							if(itemCount[2]!=0){
			    							player.completeRemoveItem(itemId[2], itemCount[2], changed);
			    						}
		    							tempCount =tempCount-itemCount[2];
		    							player.completeRemoveItem(itemId[3], tempCount, changed);
		    							removeFlag = true;
		    							
		    						}
	    						}
	    					}
	    					if(removeFlag){//减物品成功
	    						 Utils.log(log, player.getId(), command.getAppType(),
		                                  "enhance pet fail pet["+pet.getId() +"]pet["+ Utils.getHexdump(pet.toClientBytes()) +
		                                  "]fail enhance point"+addpoint);
		                        sendGetItem(changed, command.getSerial(), command.getSessionId(),
		                                (byte) 17);
	    					}
		    			}
	    			}else{
	    				sendMessage("你没有足够数量的"+PetEnhance.getPetEnhanceName(addpoint)+"型精华。", command.getSerial(), command.getSessionId());
	    			}
	    		}else{
	    			sendMessage("选择宠物不存在。", command.getSerial(), command.getSessionId());
	    		}
	    		//sendMessage("宠物"+petId+"加"+addpoint, command.getSerial(), command.getSessionId());
	    	}
    	}
    }*/

    class IbuyTop10Processor implements CommandProcessor{
    	private static final long MILLS_OF_DAY = 3600 * 24 * 1000;
        public void process(WorldPlayer player, Command command) throws Exception {
        	List<String> list = null;
        	Calendar cal = Calendar.getInstance();
            
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            String end = sf.format(cal.getTime());
            Date tmp_date = new Date(cal.getTime().getTime() - MILLS_OF_DAY * 7);
            Calendar calendar=Calendar.getInstance(); 
            calendar.setTime(tmp_date);
            String begin = sf.format(calendar.getTime());
        	try{
        		if (topListService.playerTopList.getPlayerIbuyTop(player, 10,begin,end)){
        			byte[] bytes = stageService.getTaskBytes((short) 31002,
                            new String[] {"1.进入名人堂\n2.取消",
                            "goto_instance " +
                            Instanceadd.getInstanceaddbytype(1).getInstanceid()});
                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                            GET_FILE_OK, command.getSerial(),
                            command.getSessionId());
                    seg.writeShort((short) 31002);
                    seg.writeShort((short) 2);
                    seg.write(bytes);
                    write(seg);
        			//sendGotoMap(player.getId(), (short)Instanceadd.getInstanceaddbytype(1).getMapid(), 
        			//		(short)Instanceadd.getInstanceaddbytype(1).getX(), 
        			//		(short)Instanceadd.getInstanceaddbytype(1).getY());
        		}else{
        			sendMessage("你还不是名人哦，继续努力吧。", command.getSerial(), command.getSessionId());
        		}
        	} catch (Exception ex) {
                log.error("读取名人堂记录错误。", ex);
            }
        }
    }
    
    class IbuyShowTop10Processor implements CommandProcessor{
    	private static final long MILLS_OF_DAY = 3600 * 24 * 1000;
        public void process(WorldPlayer player, Command command) throws Exception {
        	List<String> list = null;
//        	Calendar cal = Calendar.getInstance();
//        	if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){//星期日
//				cal.setTime(new Date(cal.getTime().getTime() - MILLS_OF_DAY));//取昨天，再取得周一的日期
//			}
//            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
//            String end = sf.format(cal.getTime());
//            //cal.roll(Calendar.WEEK_OF_MONTH, -1); 
//            //cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
//            Date tmp_date = new Date(cal.getTime().getTime() - MILLS_OF_DAY * 7);
//            Calendar calendar=Calendar.getInstance(); 
//            calendar.setTime(tmp_date);
//            String begin = sf.format(calendar.getTime());
        	String week[] = Utils.getWeekBeignEnd();
        	try{
        		list = topListService.playerTopList.getPlayerTopListIbuy(player, 10,week[0], week[1]);
        	} catch (Exception ex) {
                log.error("读取名人堂"+Server.iMoneyStoreString+"记录错误。", ex);
            }
            if (list.size() > 0) {
                UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                seg.writeShort((short) 9022);
                seg.writeString("名人堂");
                seg.write((byte) 3);
                seg.writeShort((short) list.size());

                for (int i = 0; i < list.size(); i++) {
                    seg.writeInt(i+1);
                    seg.writeString(list.get(i));
                    seg.writeInt(Utils.CLR_WHITE);
                }
                seg.write((byte) 1);
                seg.writeString("查看名人装备");
                seg.writeString("ibuytop10equip 6");
                connectService.writeTo(seg, player.getId());
            } else {
                sendMessage(TopListService.TOP_LIST_NO_DATA_MESSAGE, command.getSerial(), command.getSessionId());
            }
        }
        private Date getDay7Begin(Date day){
            Calendar cal = Calendar.getInstance();

            cal.setTimeInMillis(day.getTime() - MILLS_OF_DAY * 7);

            return cal.getTime();
        }
    }
    
    /**
     * petversion >= 4将宠物寄养修改为修炼
     */
    /*class PetmanagerProcessor implements CommandProcessor{
    	private static final long MILLS_OF_HOUR = 3600 * 1000;
    	private static final long MILLS_OF_HALFHOUR = 1800 * 1000;
    	private static final long MILLS_OF_TWENTYMINUTES = 1200 * 1000;
    	private static final long MILLS_OF_TENMINUTES = 600 * 1000;
        public void process(WorldPlayer player, Command command) throws Exception {
        	int petmanagertpye = Integer.parseInt(command.getParam(0));
        	Petmanager[] petmanager = null;
        	try{
	        	if ((petmanagertpye > 0) && (petmanagertpye < 4)){
	        		//取幼儿园内该人的信息
	        		petmanager = petmanagerService.getPets(player.getId());
	        	}
	        	if (petmanagertpye == 1){//寄养宠物
	        		if (petmanager == null){
	        			Pet[] pets = player.getPets();
	        			if((pets == null) || (pets.length ==0)){
	        				sendMessage("您没有宠物哦，去捉一只再来吧。", command.getSerial(), command.getSessionId());
	        			}else{
	        				String petslist = "你要寄养谁呀？";
		        			String [] questions = new String[pets.length + 3];
		        			for(int i=0;i<pets.length;i++){
		        				petslist += "\n" + String.valueOf(i+1) + "." + pets[i].getName() + "(" + pets[i].getLevel() + "级)";
		        				questions[3+i] = "petmanagerinput " + pets[i].getId();
		        				
		        			}
		        			petslist += "\n" + String.valueOf(pets.length+1) + ".算了，先不寄养了。";
		        			questions[0] = String.valueOf(pets.length+1);
	        				questions[1] = "1";
		        			questions[2] = petslist;
		        			byte[] bytes = stageService.getTaskBytes((short) 31010, questions);
		        			UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
		        					command.getSerial(),
		        					command.getSessionId());
							      seg.writeShort((short) 31010);
							      seg.writeShort((short) 2);
							      seg.write(bytes);
							      write(seg);
	        			}
	        		}else{
		        		if (petmanager.length >= Petmanager.petcount){
		        			sendMessage("您已经寄养了" + petmanager.length + "只宠物了，不能再寄养了，请领走一只再来吧。", command.getSerial(), command.getSessionId());
		        		}else{
		        			Pet[] pets = player.getPets();
		        			if((pets == null) || (pets.length ==0)){
		        				sendMessage("您没有宠物哦，去捉一只再来吧。", command.getSerial(), command.getSessionId());
		        			}else{
		        				String petslist = "你要寄养谁呀？";
			        			String [] questions = new String[pets.length + 3];
			        			for(int i=0;i<pets.length;i++){
			        				petslist += "\n" + String.valueOf(i+1) + "." + pets[i].getName() + "(" + pets[i].getLevel() + "级)";
			        				questions[3+i] = "petmanagerinput " + pets[i].getId();
			        				
			        			}
			        			petslist += "\n" + String.valueOf(pets.length+1) + ".算了，先不寄养了。";
			        			questions[0] = String.valueOf(pets.length+1);
		        				questions[1] = "1";
			        			questions[2] = petslist;
			        			byte[] bytes = stageService.getTaskBytes((short) 31010, questions);
			        			UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
			        					command.getSerial(),
			        					command.getSessionId());
								      seg.writeShort((short) 31010);
								      seg.writeShort((short) 2);
								      seg.write(bytes);
								      write(seg);
		        			}
		        		}
	        		}
	        	}else if (petmanagertpye == 2){//喂宠物
	        		if (petmanager == null){
	        			sendMessage("您还没有寄养宠物呢。", command.getSerial(), command.getSessionId());
	        		}else if(petmanager.length == 0){
	        			sendMessage("您还没有寄养宠物呢。", command.getSerial(), command.getSessionId());
	        		}else{
	        			String petslist = "你来喂谁呀？";
	        			String [] questions = new String[petmanager.length + 3];
	        			for(int i=0;i<petmanager.length;i++){
	        				String msg = "";
	        				Pet pet = Pet.getPetFromDb(petmanager[i].getPet());
	        				if (pet != null){
	        					if (petmanager[i].getStone() == 0){
	        						//没有闭关
	        					}else{
	        						//Long time_tmp = petmanager[i].getEattime().getTime()+MILLS_OF_HOUR*petmanager[i].getStone()
    								//				-(new Date()).getTime();
	        						Long time_tmp = petmanager[i].getEattime().getTime()+MILLS_OF_TENMINUTES*petmanager[i].getStone()
													-(new Date()).getTime();
	        						if (time_tmp>0){
	        							pet = addpetexp(pet,petmanager[i].getStone());//宠物加经验
	        							int minute_all = (int) (time_tmp/60/1000);
	        							int minute = minute_all % 60;
	        							int house_all = minute_all/60;
	        							if (minute == 59){
	        								minute = 0;
	        								house_all = house_all + 1;
	        							}
	        							msg = "闭关中，剩余时间";
	        							if (house_all > 0){
	        								msg = msg + house_all + "小时";
	        							}
	        							if (minute > 0){
	        								msg = msg + minute + "分钟";
	        							}
	        						}else{
	        							//闭关结束，存档
	        							pet = addpetexp(pet,petmanager[i].getStone());//宠物加经验
	        							Petmanager petmanagernew = new Petmanager();
	        							petmanagernew.setId(petmanager[i].getId());
	        							petmanagernew.setpetdata(pet);
	        							petmanagernew.setPlayerId(player.getId());
	        							petmanagernew.setEattime(new Date());
	        							petmanagernew.setStone(0);
	        							petmanagerService.addPet(petmanagernew);
	        						}
	        					}
	        					petslist += "\n" + String.valueOf(i+1) + "." + pet.getName() + "(" + pet.getLevel() + "级)" + msg;
		        				questions[3+i] = "petmanagereat " + pet.getId();
	        				}
	        			}
	        			petslist += "\n" + String.valueOf(petmanager.length+1) + ".算了，先不喂了。";
	        			questions[0] = String.valueOf(petmanager.length+1);
        				questions[1] = "1";
	        			questions[2] = petslist;
	        			byte[] bytes = stageService.getTaskBytes((short) 31010, questions);
	        			UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
	        					command.getSerial(),
	        					command.getSessionId());
						      seg.writeShort((short) 31010);
						      seg.writeShort((short) 2);
						      seg.write(bytes);
						      write(seg);
	        		}
	        	}else if (petmanagertpye == 3){//领回宠物
	        		if (petmanager == null){
	        			sendMessage("您还没有寄养宠物呢。", command.getSerial(), command.getSessionId());
	        		}else if(petmanager.length == 0){
	        			sendMessage("您还没有寄养宠物呢。", command.getSerial(), command.getSessionId());
	        		}else{
	        			String petslist = "你来接谁呀？";
	        			String [] questions = new String[petmanager.length + 3];
	        			for(int i=0;i<petmanager.length;i++){
	        				String msg = "";
	        				Pet pet = Pet.getPetFromDb(petmanager[i].getPet());
	        				if (pet != null){
	        					if (petmanager[i].getStone() == 0){
	        						//没有闭关
	        					}else{
	        						//Long time_tmp = petmanager[i].getEattime().getTime()+MILLS_OF_HOUR*petmanager[i].getStone()
    								//				-(new Date()).getTime();
	        						Long time_tmp = petmanager[i].getEattime().getTime()+MILLS_OF_TENMINUTES*petmanager[i].getStone()
													-(new Date()).getTime();
	        						if (time_tmp>0){
	        							pet = addpetexp(pet,petmanager[i].getStone());
	        							int minute_all = (int) (time_tmp/60/1000);
	        							int minute = minute_all % 60;
	        							int house_all = minute_all/60;
	        							if (minute == 59){
	        								minute = 0;
	        								house_all = house_all + 1;
	        							}
	        							msg = "闭关中，剩余时间";
	        							if (house_all > 0){
	        								msg = msg + house_all + "小时";
	        							}
	        							if (minute > 0){
	        								msg = msg + minute + "分钟";
	        							}
	        						}else{
	        							//闭关结束，存档
	        							pet = addpetexp(pet,petmanager[i].getStone());
	        							Petmanager petmanagernew = new Petmanager();
	        							petmanagernew.setId(petmanager[i].getId());
	        							petmanagernew.setpetdata(pet);
	        							petmanagernew.setPlayerId(player.getId());
	        							petmanagernew.setEattime(new Date());
	        							petmanagernew.setStone(0);
	        							petmanagerService.addPet(petmanagernew);
	        						}
	        					}
	        					petslist += "\n" + String.valueOf(i+1) + "." + pet.getName() + "(" + pet.getLevel() + "级)" + msg;
		        				questions[3+i] = "petmanagerout " + pet.getId();
	        				}
	        			}
	        			petslist += "\n" + String.valueOf(petmanager.length+1) + ".算了，先不领了。";
	        			questions[0] = String.valueOf(petmanager.length+1);
        				questions[1] = "1";
	        			questions[2] = petslist;
	        			byte[] bytes = stageService.getTaskBytes((short) 31010, questions);
	        			UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
	        					command.getSerial(),
	        					command.getSessionId());
						      seg.writeShort((short) 31010);
						      seg.writeShort((short) 2);
						      seg.write(bytes);
						      write(seg);
	        		}
	        	}
        	} catch (Exception ex) {
                log.error("宠物寄养园信息错误。", ex);
            }
        }
        private Pet addpetexp(Pet pet,int stone){
//        	int exp = Utils.PET_VIP_EXP[pet.getLevel()] * stone;//BathHouse.PET_EXP[pet.getLevel()]*Petmanager.expforbathhouse*stone;
        	//活动期间，宠物寄养喂精炼石获得经验双倍
        	int exp = Utils.PET_VIP_EXP[pet.getLevel()] * stone * 2;
        	int nExp = pet.getExp() + exp;
            int upExp = Utils.getPetUpLevelExp(pet.getLevel());
            if (nExp >= upExp) {
            	while(nExp >= upExp){
            		pet.setLevel(pet.getLevel() + 1);
                    pet.setExp(nExp - upExp);
                    int oldHp = pet.getHp();
                    int oldMp = pet.getMp();
                    pet.setMp(pet.getMaxMp());
                    pet.setHp(pet.getMaxHp());
//                    pet.setPoint(pet.getPoint() + 4);
                    pet.setCurrentPoint(pet.getCurrentPoint()+4);
                    nExp = nExp - upExp;
                    upExp = Utils.getPetUpLevelExp(pet.getLevel());
            	}
                return pet;
            } else {
                int oldExp = pet.getExp();
                nExp = Math.min(upExp-1,nExp);  //不能超过最高升级点数
                pet.setExp(nExp);
                if(nExp>oldExp)
                    return pet;
                return pet;
            }
        }
        
    }*/
    
    /**
     * petversion >= 4将宠物寄养修改为修炼
     */
    /*class PetmanagerinputProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	int petId = Integer.parseInt(command.getParam(0));
        	try{
        		Pet pet = player.getPet(petId);
        		//宠物脱掉装备
                Changed changed = new Changed();
                for (int i = 0; i < pet.getUsedEquipments().length ; i++){
                	if (pet.getUsedEquipments()[i] != null){
                		IEquipment e = (IEquipment)pet.getUsedEquipments()[i].item;
                		if(player.completeAddItem(e,e.getId(),changed, player.getClientDataVersion())==null){
                			if (changed != null){
                				connectService.sendGetItem(changed,player.getId(), (byte) 26);
                			}
                			sendMessage(player.getId(),"操作失败。背包满，宠物装备无法卸下。");
                			UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
                        	seg.writeShort(ClientConstants.EXTEND_PROTOCOL_PETEQU_LOGIN);
                        	seg.writeInt(pet.getId());
                        	Grid[] usedEquipmentsTemp = pet.getUsedEquipments();
                    		if (usedEquipmentsTemp != null){
                    			for (int jj = 0;jj<pet.getUsedEquipmentinfo().length;jj++){
                    				seg.write((byte) pet.getUsedEquipmentinfo()[jj]);
                    				if (usedEquipmentsTemp[jj] != null){
                    					if (pet.getUsedEquipmentinfo()[jj] == 1){
                    						IEquipment equtmp = (IEquipment) usedEquipmentsTemp[jj].item;
                    						equtmp.setDataVersion(player.getClientDataVersion());
                    						seg.write(equtmp.toClientBytes());
                    					}
                    				}
                    			}
                    		}else{
                    			for (int jj = 0;jj<pet.getUsedEquipmentinfo().length;jj++){
                    				seg.write((byte) pet.getUsedEquipmentinfo()[jj]);
                    			}
                    		}
                    		// 发送宠物升级所需升级经验
                    		seg.writeInt(Utils.getPetUpLevelExp(pet.getLevel()));
                    		write(seg,player.getId());
                			
                			return;
                		}
                		e = null;
                		pet.setUsedEquipmentsinfo(i, (byte) 0);
                		pet.setUsedEquipments(i, null);
                	}
                }
                connectService.sendGetItem(changed,player.getId(), (byte) 26);
        		if (pet != null){
        			Petmanager petmanager = new Petmanager();
        			petmanager.setpetdata(pet);
        			petmanager.setPlayerId(player.getId());
        			petmanager.setEattime(new Date());
        			petmanager.setStone(0);
        			petmanagerService.addPet(petmanager);
        			player.removePet(pet);
        			changed = new Changed();
        			changed.addItem(pet, -1);
                    Utils.log(log, player.getId(), command.getAppType(),
                              "Petmanagerinput Pet[" + Utils.getHexdump(pet.toClientBytes()) +
                              "]");
                    sendGetItem(changed, command.getSerial(), command.getSessionId(),
                            (byte) 17);
        		}
        	} catch (Exception ex) {
                log.error("宠物寄养园寄养信息错误。", ex);
            }
        }
    }*/
    
    /**
     * petversion >= 4将宠物寄养修改为修炼
     */
    /*class PetmanageroutProcessor implements CommandProcessor{
    	private static final long MILLS_OF_HOUR = 3600 * 1000;
    	private static final long MILLS_OF_HALFHOUR = 1200 * 1000;
        public void process(WorldPlayer player, Command command) throws Exception {
        	int petId = Integer.parseInt(command.getParam(0));
        	try{
        		if (petId == -1){
        			//强行取走
        			petId = Integer.parseInt(command.getParam(1));
        			Petmanager petmanager = petmanagerService.getPet(petId);
        			if (petmanager != null){
        				Pet pet = Pet.getPetFromDb(petmanager.getPet());
            			if (pet != null){
            				//int stone_tmp = (int) (((new Date()).getTime()-petmanager.getEattime().getTime())/MILLS_OF_HOUR);
            				int stone_tmp = (int) (((new Date()).getTime()-petmanager.getEattime().getTime())/MILLS_OF_HALFHOUR);
            				//存档
							pet = addpetexp(pet,stone_tmp);//宠物只加到现在的经验
							//判断宠物栏是否满
        					if (player.getPetCount() >= player.getPetSize()) {
                                sendMessage("宠物栏满了哦，不能领走他了。", command.getSerial(),
                                            command.getSessionId());
                            }else{
                            	Petmanager petmanagernew = new Petmanager();
                            	petmanagernew.setId(petmanager.getId());
    							petmanagernew.setpetdata(pet);
    							petmanagernew.setPlayerId(player.getId());
    							petmanagernew.setEattime(new Date());
    							petmanagernew.setStone(0);
    							petmanagerService.addPet(petmanagernew);
	        					Changed changed = new Changed();
	        					//超过100
	        					if (pet.getLevel()>100){
	        						Utils.log(log, player.getId(), command.getAppType(),
		                                    "Petmanagerout timein Petlevle[ " + pet.getLevel() +"] error");
	        						int leveltmp= pet.getLevel()-100;
	        						pet.setLevel(100);
	        						if (pet.getPoint()-leveltmp*4 <0){
	        							pet.setPoint(0);
	        						}else{
	        							pet.setPoint(pet.getPoint()-leveltmp*4);
	        						}
	        					}
	            				player.addPet(pet, changed);
	            				
	            				Utils.log(log, player.getId(), command.getAppType(),
	                                    "Petmanagerout timein Pet[" + Utils.getHexdump(pet.toClientBytes()) +
	                                    "]");
	            				petmanagerService.leavepet(petmanager);
	                            sendGetItem(changed, command.getSerial(), command.getSessionId(),
	                                    (byte) 17);
	                          //判断新版本客户端，同步宠物装备信息
                                try{
                                	UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
                                	seg.writeShort(ClientConstants.EXTEND_PROTOCOL_PETEQU_LOGIN);
                                	seg.writeInt(pet.getId());
                                	Grid[] usedEquipmentsTemp = pet.getUsedEquipments();
                            		if (usedEquipmentsTemp != null){
                            			for (int jj = 0;jj<pet.getUsedEquipmentinfo().length;jj++){
                            				seg.write((byte) pet.getUsedEquipmentinfo()[jj]);
                            				if (usedEquipmentsTemp[jj] != null){
                            					if (pet.getUsedEquipmentinfo()[jj] == 1){
                            						IEquipment equtmp = (IEquipment) usedEquipmentsTemp[jj].item;
                            						equtmp.setDataVersion(player.getClientDataVersion());
                            						seg.write(equtmp.toClientBytes());
                            					}
                            				}
                            			}
                            		}else{
                            			for (int jj = 0;jj<pet.getUsedEquipmentinfo().length;jj++){
                            				seg.write((byte) pet.getUsedEquipmentinfo()[jj]);
                            			}
                            		}
                            		// 发送宠物升级所需升级经验
                            		seg.writeInt(Utils.getPetUpLevelExp(pet.getLevel()));
                            		write(seg, player.getId());
                                }catch (Exception e) {
                                	log.debug(e, e);
                                }
                            }
            			}
        			}
        		}else{
        			Petmanager petmanager = petmanagerService.getPet(petId);
            		if (petmanager != null){
            			Pet pet = Pet.getPetFromDb(petmanager.getPet());
            			if (pet != null){
            				//判断时间
            				if (petmanager.getStone() == 0){
        						//没有闭关
            					//判断宠物栏是否满
            					if (player.getPetCount() >= player.getPetSize()) {
                                    sendMessage("宠物栏满了哦，不能领走他了。", command.getSerial(),
                                                command.getSessionId());
                                }else{
                                	//超过100
    	        					if (pet.getLevel()>100){
    	        						Utils.log(log, player.getId(), command.getAppType(),
    		                                    "Petmanagerout timein Petlevle[ " + pet.getLevel() +"] error");
    	        						int leveltmp= pet.getLevel()-100;
    	        						pet.setLevel(100);
    	        						if (pet.getPoint()-leveltmp*4 <0){
    	        							pet.setPoint(0);
    	        						}else{
    	        							pet.setPoint(pet.getPoint()-leveltmp*4);
    	        						}
    	        					}
    	        					Changed changed = new Changed();
    	            				player.addPet(pet, changed);
    	            				//sendGetItem(changed, command.getSerial(),
    	                            //        command.getSessionId(), (byte) 26);
    	            				Utils.log(log, player.getId(), command.getAppType(),
    	                                    "Petmanagerout Pet[" + Utils.getHexdump(pet.toClientBytes()) +
    	                                    "]");
    	            				petmanagerService.leavepet(petmanager);
    	                            sendGetItem(changed, command.getSerial(), command.getSessionId(),
    	                                    (byte) 17);
    	                            // 领会宠物时，需要同步宠物可装装备标识。
    	                            UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
    	                        	seg.writeShort(ClientConstants.EXTEND_PROTOCOL_PETEQU_LOGIN);
    	                        	seg.writeInt(pet.getId());
    	                        	Grid[] usedEquipmentsTemp = pet.getUsedEquipments();
    	                    		if (usedEquipmentsTemp != null){
    	                    			for (int j = 0; j < pet.getUsedEquipmentinfo().length; j++){
    	                    				seg.write((byte) pet.getUsedEquipmentinfo()[j]);
    	                				}
    	                    			// 发送宠物升级所需升级经验
    	                        		seg.writeInt(Utils.getPetUpLevelExp(pet.getLevel()));
    	                    			connectService.writeTo(seg, player.getId());
    	                    		} else {
    	                    			sendMessage(player.getId(), "数据库错误。");
    	                    		}
                                }
        					}else{
        						//Long time_tmp = petmanager.getEattime().getTime()+MILLS_OF_HOUR*petmanager.getStone()
    							//				-(new Date()).getTime();
        						Long time_tmp = petmanager.getEattime().getTime()+MILLS_OF_HALFHOUR*petmanager.getStone()
												-(new Date()).getTime();
        						if (time_tmp>0){
        							byte[] bytes = stageService.getTaskBytes((short) 31002,
                                            new String[] {"您的宠物" + pet.getName() + "正在修炼哦，如果领走了就修炼失败了，损失惨重哦，你想好了吗？\n1.坚持领走\n2.还是算了吧",
                                            "petmanagerout -1 " +
                                            pet.getId()});
                                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                                            GET_FILE_OK, command.getSerial(),
                                            command.getSessionId());
                                    seg.writeShort((short) 31002);
                                    seg.writeShort((short) 2);
                                    seg.write(bytes);
                                    write(seg);
        						}else{
        							//闭关结束，存档
        							pet = addpetexp(pet,petmanager.getStone());
        							Petmanager petmanagernew = new Petmanager();
        							petmanagernew.setpetdata(pet);
        							petmanagernew.setPlayerId(player.getId());
        							petmanagernew.setEattime(new Date());
        							petmanagernew.setStone(0);
        							petmanagerService.addPet(petmanagernew);
        							//判断宠物栏是否满
                					if (player.getPetCount() >= player.getPetSize()) {
                                        sendMessage("宠物栏满了哦，不能领走他了。", command.getSerial(),
                                                    command.getSessionId());
                                    }else{
                                    	//超过100
        	        					if (pet.getLevel()>100){
        	        						Utils.log(log, player.getId(), command.getAppType(),
        		                                    "Petmanagerout timein Petlevle[ " + pet.getLevel() +"] error");
        	        						int leveltmp= pet.getLevel()-100;
        	        						pet.setLevel(100);
        	        						if (pet.getPoint()-leveltmp*4 <0){
        	        							pet.setPoint(0);
        	        						}else{
        	        							pet.setPoint(pet.getPoint()-leveltmp*4);
        	        						}
        	        					}
        	        					Changed changed = new Changed();
        	            				player.addPet(pet, changed);
        	            				sendGetItem(changed, command.getSerial(),
        	                                    command.getSessionId(), (byte) 26);
        	            				Utils.log(log, player.getId(), command.getAppType(),
        	                                    "Petmanagerout Pet[" + Utils.getHexdump(pet.toClientBytes()) +
        	                                    "]");
        	            				petmanagerService.leavepet(petmanager);
        	                            sendGetItem(changed, command.getSerial(), command.getSessionId(),
        	                                    (byte) 17);
                                    }
        						}
        					}
        				}
            		}
        		} 			
        	} catch (Exception ex) {
                log.error("宠物寄养园领取信息错误。", ex);
            }
        }
	    private Pet addpetexp(Pet pet,int stone){
//	    	int exp = Utils.PET_VIP_EXP[pet.getLevel()] * stone;//BathHouse.PET_EXP[pet.getLevel()]*Petmanager.expforbathhouse*stone;
	    	//活动期间，宠物寄养喂精炼石获得经验双倍
        	int exp = Utils.PET_VIP_EXP[pet.getLevel()] * stone * 2;
	    	int nExp = pet.getExp() + exp;
	        int upExp = Utils.getPetUpLevelExp(pet.getLevel());
	        if (nExp >= upExp) {
	        	while(nExp >= upExp){
            		pet.setLevel(pet.getLevel() + 1);
                    pet.setExp(nExp - upExp);
                    int oldHp = pet.getHp();
                    int oldMp = pet.getMp();
                    pet.setMp(pet.getMaxMp());
                    pet.setHp(pet.getMaxHp());
//                    pet.setPoint(pet.getPoint() + 4);
                    pet.setCurrentPoint(pet.getCurrentPoint()+4);
                    nExp = nExp - upExp;
                    upExp = Utils.getPetUpLevelExp(pet.getLevel());
            	}
                return pet;
	        } else {
	            int oldExp = pet.getExp();
	            nExp = Math.min(upExp-1,nExp);  //不能超过最高升级点数
	            pet.setExp(nExp);
	            if(nExp>oldExp)
	                return pet;
	            return pet;
	        }
	    }
    }*/
    
    /**
     * petversion >= 4将宠物寄养修改为修炼
     */
    /*class PetmanagereatProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	int petId = Integer.parseInt(command.getParam(0));
        	try{
        		if (player.hasItem(Petmanager.stoneid, Petmanager.stonecount)) {
        			//有精炼石
        			Petmanager petmanager = petmanagerService.getPet(petId);
            		if (petmanager != null){
            			Pet pet = Pet.getPetFromDb(petmanager.getPet());
            			if (pet != null){
            				Pet pet_tmp = new Pet();
            				pet_tmp.setAbilities(pet.getAbilities());
            				pet_tmp.setAgility(pet.getAgility());
            				pet_tmp.setBaby(pet.isBinded());
            				pet_tmp.setCurrentPoint(pet.getCurrentPoint());
            				pet_tmp.setDesc(pet.getDesc());
            				pet_tmp.setExp(pet.getExp());
            				pet_tmp.setFavor(pet.getFavor());
            				pet_tmp.setHp(pet.getHp());
            				pet_tmp.setId(pet.getId());
            				pet_tmp.setIntelligence(pet.getIntelligence());
            				pet_tmp.setItemId(pet.getItemId());
            				pet_tmp.setLevel(pet.getLevel());
            				pet_tmp.setMp(pet.getMp());
            				pet_tmp.setName(pet.getName());
            				pet_tmp.setPetType(pet.getPetType());
            				pet_tmp.setPoint(pet.getPoint());
            				pet_tmp.setStrength(pet.getStrength());
            				pet_tmp.setVitality(pet.getVitality());
            				pet_tmp = addpetexp(pet_tmp,petmanager.getStone());
            				if (pet_tmp.getLevel()>99){
            					sendMessage("宠物100级了哦，别再喂了。", command.getSerial(),
                                        command.getSessionId());
        					}else{
        						Petmanager petmanagernew = new Petmanager();
                				petmanagernew.setId(petmanager.getId());
            					petmanagernew.setpetdata(pet);
            					petmanagernew.setPlayerId(player.getId());
            					if (petmanager.getStone() > 0){
            						petmanagernew.setStone(petmanager.getStone() + 1);
            						petmanagernew.setEattime(petmanager.getEattime());
            					}else{
            						petmanagernew.setStone(1);
            						petmanagernew.setEattime(new Date());
            					}
            					//存档
            					petmanagerService.addPet(petmanagernew);
            					Changed changed = new Changed();
            					player.completeRemoveItem(Petmanager.stoneid, Petmanager.stonecount, changed);
            					sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 30);
            					Utils.log(log, player.getId(), command.getAppType(),
	                                    "Petmanager-eat Pet[" + Utils.getHexdump(pet.toClientBytes()) +
	                                    "] stone[1]");
        					}
            				
            			}
            		}
					
        		}else{
        			sendMessage("您没有 " + Petmanager.stonename + "哦，没法喂了。", command.getSerial(),
                            command.getSessionId());
        		}
        	} catch (Exception ex) {
                log.error("宠物寄养园喂食信息错误。", ex);
            }
        }
        private Pet addpetexp(Pet pet,int stone){
//	    	int exp = Utils.PET_VIP_EXP[pet.getLevel()] * stone;//BathHouse.PET_EXP[pet.getLevel()]*Petmanager.expforbathhouse*stone;
        	//活动期间，宠物寄养喂精炼石获得经验双倍
        	int exp = Utils.PET_VIP_EXP[pet.getLevel()] * stone * 2;
	    	int nExp = pet.getExp() + exp;
	        int upExp = Utils.getPetUpLevelExp(pet.getLevel());
	        if (nExp >= upExp) {
	        	while(nExp >= upExp){
	        		if(pet.getLevel() > 100){
	        			break;
	                }
            		pet.setLevel(pet.getLevel() + 1);
                    pet.setExp(nExp - upExp);
                    int oldHp = pet.getHp();
                    int oldMp = pet.getMp();
                    pet.setMp(pet.getMaxMp());
                    pet.setHp(pet.getMaxHp());
//                    pet.setPoint(pet.getPoint() + 4);
                    pet.setCurrentPoint(pet.getCurrentPoint()+4);
                    nExp = nExp - upExp;
                  
                    upExp = Utils.getPetUpLevelExp(pet.getLevel());
            	}
                return pet;
	        } else {
	            int oldExp = pet.getExp();
	            nExp = Math.min(upExp-1,nExp);  //不能超过最高升级点数
	            pet.setExp(nExp);
	            if(nExp>oldExp)
	                return pet;
	            return pet;
	        }
	    }
    }*/
    
    class EnhanceAnniversaryProcessor implements CommandProcessor{

        private Random rnd = new Random();

        public void process(WorldPlayer player, Command command) throws Exception {
            int equItemId = Integer.parseInt(command.getParam(0));//原装备id
            AnniversaryEnhance anniversary = AnniversaryEnhance.getAnniversaryEnhance(equItemId);
            if (anniversary != null){
            	int newequItemId = anniversary.getNewequItemId();//精炼后装备id
                int count = anniversary.getCount();//星级
                int probability = anniversary.getProbability();//概率
                synchronized (player) {
                    log.info("ID["+player.getId()+"]equId["+equItemId+"]equedId["+newequItemId+"]instanceId["+id+"]pro["+count+"]EnhanceAnniversary Try");
                    if (player.hasItem(200233, count) && (player.getItem(equItemId, -1) != null)) {
                    	Changed changed = new Changed();
                    	if (probability >= 100 || Utils.hit(rnd, probability, 100)){
                    		IItem deletedequ = player.completeRemoveItem(equItemId, -1, changed);
                            if (deletedequ == null){
                            	sendMessage("你没有足够的材料精炼。", command.getSerial(), command.getSessionId());
                            } else {
                            	IItem deleted = player.completeRemoveItem(200233, count, changed);
                            	IItem item = player.completeAddItem(Items.getTemplate(newequItemId).newInstance(), 1, changed, player.getClientDataVersion());
                                if (item == null) {
                                    byte[] att = ItemUtils.item2dbAttachment(Items.getTemplate(newequItemId).newInstance(),1);
                                    mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                                    		Items.getTemplate(newequItemId).newInstance().getName() + "*" + 1, "", att, 0, true);
                                }
                                sendMessage("恭喜你，你的装备精炼成功。", command.getSerial(), command.getSessionId());
                                sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 30);
                                log.info("ID["+player.getId()+"]equId["+equItemId+"]equedId["+newequItemId+"]instanceId["+id+"]pro["+count+"]EnhanceAnniversary EnhanceOk");
                                if (count >= 5){
                                    chatService.sendWorldMessage( -1, "系统",
                                           "恭喜" + player.getPlayerName() + "顺利精炼出"+
                                           item.getName());
                                }
                            }
                            
                    	}else{
                    		//精炼失败
                    		IItem deleted = player.completeRemoveItem(200233, count, changed);
                    		if (deleted == null){
                            	sendMessage("你没有足够的材料精炼。", command.getSerial(), command.getSessionId());
                            } else {
                            	log.info("ID["+player.getId()+"]equId["+equItemId+"]equedId["+newequItemId+"]instanceId["+id+"]pro["+count+"]EnhanceAnniversary EnhanceFail");
                                sendMessage("哦！很遗憾！本次精炼失败。不过，为了精品装备，集齐明珠再来试试吧！", command.getSerial(), command.getSessionId());
                            }
                    		sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 30);
                    	}
                    } else {
                        sendMessage("你没有足够的材料精炼。", command.getSerial(), command.getSessionId());
                    }   	
                }
            }
        }
    }
    
    class RecommendedProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	try {
        		int type = Integer.parseInt(command.getParam(0));
        		String tel = command.getParam(1);
        		if (tel != null) {
        			if (tel.length() == 11){
        				if(type  == 0){//cmcc
        					String msg = "您的朋友“"+player.getPlayerName()+"”邀您一起玩《明珠幻想》";
        					String playeruserid = getClient(command.getSessionId()).cmccUserId;
    	                	//调发短信的脚本
    	                	log.info("CMCC Send sms to[" + tel + "],playername["+ player.getPlayerName() + "userid["
    	                			+ playeruserid + "] try");
    	                	/**
    	                     * 卓望版本，用户推荐好友。
    					     * userId           String          登录平台ID
    					     * accountId        int             帐号ID
    					     * playerId         int             角色ID
    					     * targetPhone      String          目标用户手机号
    					     * message          String          邀请标题
    					     * requestId        int             请求ID
    					     * public static final byte CMCC_RECOMMEND_REQUEST = (byte)220;
    	                     */
    	                	UWAPSegment seg = new UWAPSegment(ServerConstants.CMCC_RECOMMEND_REQUEST);
    	                    seg.writeString(getClient(command.getSessionId()).cmccUserId);
    	                    seg.writeInt(player.getAccountId()); 
    	                    seg.writeInt(player.getId()); 
    	                    seg.writeString(tel);
    	                    seg.writeString(msg);
    	                    seg.writeInt(0); 
    	                    Server.instance.authSession.write(seg);
    	                    Changed changed = new Changed();
    	                    IItem item = player.completeRemoveItem(200409, 1, changed);
    	                    if (item != null) {
    	    	                sendGetItem(changed, command.getSerial(), command.getSessionId(),
    	                                (byte) 10);
    	                    }
    	                    log.info("CMCC RecommendedResult ID[" + player.getId() + "]Accountid[" +
    	                    		player.getAccountId() +"]userid["+getClient(command.getSessionId()).cmccUserId+"]TEL[" + tel +"]");
                		}else if (type  == 1){//pip
        					String playeruserid = getClient(command.getSessionId()).cmccUserId;
    	                	//调发短信的脚本
                        	byte[] bytes = stageService.getTaskBytes((short) 31031,new String[] {"2",tel,
			                			player.getPlayerName(),playeruserid,(String) configuration.getProperty("arenashowname")});
								UWAPSegment seg = new UWAPSegment(ClientConstants.
								                                 GET_FILE_OK, command.getSerial(),
								                                 command.getSessionId());
								seg.writeShort((short) 31031);
								seg.writeShort((short) 2);
								seg.write(bytes);
								write(seg);
    	                	log.info("PIP Send sms to[" + tel + "],playername["+ player.getPlayerName() + "try");
                		}
        			}else{
                    	sendMessage(player.getId(), "您输入的不是手机号吧。11位的哦。");
                    }
        		}else{
                	sendMessage(player.getId(), "手机号码不能是空的。");
                }
        	}catch (Exception ex) {
                log.error("推荐玩家失败。", ex);
            }
        }
    }

    class RecommendedResultProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	try {
        		String tel = command.getParam(0);
                if (tel != null) {
                	sendMessage(player.getId(), "您已经成功的邀请了您的朋友，等待他一起享受快乐的游戏吧。");
                	RecommendRequestMessage msg = new RecommendRequestMessage(player.getAccountId(), tel, Server.getGameCode());
            	    accountSkeleton.send(msg);
                    log.info("PIP RecommendedResult ID[" + player.getId() + "]Accountid[" +
                    		player.getAccountId() +"]userid["+getClient(command.getSessionId()).cmccUserId+"]TEL[" + tel +"]");
                }else{
                	sendMessage(player.getId(), "手机号码不能是空的。");
                }
        	}catch (Exception ex) {
                log.error("推荐返回向认证发送确认信息失败。", ex);
            }
        }
    }

    class CmccBusinessresultProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	try {
        		int type = Integer.parseInt(command.getParam(0));
        		log.info("CmccBusiness result ID[" + player.getId() + "]Accountid[" +
                    	player.getAccountId() +"]TYPE[" + type +"]Success");
        		//判断是否领过
				GiftData giftData = giftService.getPlayerOnlyGift(299999, player); 
				if(giftData != null ){
					Gift gift = giftData.getGift();
					if(gift.getCount() < 1){
						//未领过奖励物品
						Changed changed = new Changed();
						IItem item = player.completeAddItem(Items.getTemplate(2).newInstance(), 50, changed, player.getClientDataVersion());
                        if (item == null) {
                            byte[] att = ItemUtils.item2dbAttachment(Items.getTemplate(2).newInstance(),50);
                            mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                            		Items.getTemplate(2).newInstance().getName() + "*" + 50, "", att, 0, true);
                            sendMessage("由于背包满，物品已经邮寄到邮箱中，请注意查收。", command.getSerial(), command.getSessionId());
                        }
                        sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 30);
                        sendMessage("恭喜你，首次定制成功后得到了礼物哦~", command.getSerial(), command.getSessionId());
						gift.setCount(1);
                        gift.setRcount(1);
                        giftService.savePlayerGift(gift);
    				}
				}
        	}catch (Exception ex) {
                log.error("定制移动业务后发放奖品错误。", ex);
            }
        }
    }
    class CmccBusinessokProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	try {
        		int type = Integer.parseInt(command.getParam(0));
        		String Businesscode = command.getParam(1);
        		String Businessmsg = command.getParam(2);
        		log.info("CmccBusiness ID[" + player.getId() + "]Accountid[" +
                    	player.getAccountId() +"]TYPE[" + type +"]TRY " +
                    	"号码：" + Businesscode +"内容：" + Businessmsg);
    			//调发短信的脚本
    			String tmp = "";
    			int subtype = 0;
    			if (type == 1){
    				tmp = "飞信";
    				subtype = 2;
    			}else if (type == 2){
    				tmp = "G+游戏包定制";
    				subtype = 5;
    			}else if (type == 3){
    				tmp = "彩铃定制";
//    				subtype = 1;
    			}else if (type == 4){
    				tmp = "GPRS20元";
    			}else if (type == 5){
    				tmp = "139邮箱";
    				subtype = 3;
    			}else if (type == 6){
    				tmp = "手机报";
    				subtype = 4;
    			}else if (type == 7){
    				tmp = "5元GPRS套餐";
    				subtype = -1;
    			}else if (type == 8){
    				tmp = "20元GPRS套餐";
    				subtype = -1;
    			}else if (type == 9){
    				tmp = "神州行GPRS套餐";
    				subtype = -1;
    			}else if (type == 10){
    				tmp = "动感地带GPRS套餐";
    				subtype = -1;
    			}else if (type == 11){
    				tmp = "全球通GPRS套餐";
    				subtype = -1;
    			}
    			
    			if (subtype == 0){
    				log.info("CmccBusiness ID[" + player.getId() + "]Accountid[" +
                        	player.getAccountId() +"]TYPE[" + type +"]TRY " +
                        	"号码：" + Businesscode +"内容：" + Businessmsg);
        			sendMessage("您将定制移动" + tmp + "业务，请确认发送短信来保障定制成功，非常感谢。",command.getSerial(),command.getSessionId());
        			
                	byte[] bytes = stageService.getTaskBytes((short) 31031,new String[] {"3",
                			Businesscode,
                			Businessmsg,
                			String.valueOf(type)});
    				UWAPSegment seg = new UWAPSegment(ClientConstants.
    				                                 GET_FILE_OK, command.getSerial(),
    				                                 command.getSessionId());
    				seg.writeShort((short) 31031);
    				seg.writeShort((short) 2);
    				seg.write(bytes);
    				write(seg);
    				log.info("CmccBusiness ID[" + player.getId() + "]Accountid[" +
                        	player.getAccountId() +"]TYPE[" + type +"]SEND RETURN");
    			}else if (subtype>0){
    				/**
        		     * 卓望版本，订购移动服务。
        		     * requestId        int             请求ID
        		     * userId           String          登录平台ID
        		     * accountId        int             帐号ID
        		     * playerId         int             角色ID
        		     * subType          int             订购类型：1 开通彩铃，2 开通飞信，3 开通邮箱，4 开通手机报，5 开通G+游戏包
        		     */
                	UWAPSegment seg = new UWAPSegment(ServerConstants.CMCC_SUBSCRIBE);
                	seg.writeInt(-1); 
                    seg.writeString(getClient(command.getSessionId()).cmccUserId);
                    seg.writeInt(player.getAccountId()); 
                    seg.writeInt(player.getId()); 
                    seg.writeInt(subtype); 
                    Server.instance.authSession.write(seg);
                    log.info("CmccBusiness to Account ID[" + player.getId() + "]Accountid[" +
                    		player.getAccountId() +"]userid["+getClient(command.getSessionId()).cmccUserId+"]");
                 }else if (subtype==-1){
                	 if (type == 7){
                		 byte[] bytes = stageService.getTaskBytes((short) 31022,
                                 new String[] {
                                 "http://211.139.201.153:8898/wap/dsissfinally/whiteList.jsp?serid=00730301&sid=feixun","0"});
                         UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
                        		 command.getSerial(),
                        		 command.getSessionId());
                         seg.writeShort((short) 31022);
                         seg.writeShort((short) 2);
                         seg.write(bytes);
                         write(seg);
                	 }else if (type == 8){
                		 byte[] bytes = stageService.getTaskBytes((short) 31022,
                                 new String[] {
                                 "http://211.139.201.153:8898/wap/dsissfinally/whiteList.jsp?serid=00730401&sid=feixun","0"});
                         UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
                        		 command.getSerial(),
                        		 command.getSessionId());
                         seg.writeShort((short) 31022);
                         seg.writeShort((short) 2);
                         seg.write(bytes);
                         write(seg);
                	 }else if (type == 9){
                		 byte[] bytes = stageService.getTaskBytes((short) 31022,
                                 new String[] {
                                 "http://wap.dg1860.com/139.ipi?id=31B1998CCDC907CF4292D75B1D8069AC29ED04AFD14CF5EFD703A30F5494B0761EEA262C7C6C510657A0DED9235DADE02284458B20EFC181CBD91F33B35D5CD2AE6ECE482B2940E4C6E8D2B6F8A86B53","0"});
                         UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
                        		 command.getSerial(),
                        		 command.getSessionId());
                         seg.writeShort((short) 31022);
                         seg.writeShort((short) 2);
                         seg.write(bytes);
                         write(seg);
                	 }else if (type == 10){
                		 byte[] bytes = stageService.getTaskBytes((short) 31022,
                                 new String[] {
                                 "http://wap.dg1860.com/139.ipi?id=31B1998CCDC907CF4292D75B1D8069AC29ED04AFD14CF5EFD703A30F5494B0761EEA262C7C6C510657A0DED9235DADE02284458B20EFC181013C2FB1A77F1A97AE6ECE482B2940E4C6E8D2B6F8A86B53","0"});
                         UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
                        		 command.getSerial(),
                        		 command.getSessionId());
                         seg.writeShort((short) 31022);
                         seg.writeShort((short) 2);
                         seg.write(bytes);
                         write(seg);
                	 }else if (type == 11){
                		 byte[] bytes = stageService.getTaskBytes((short) 31022,
                                 new String[] {
                                 "http://wap.dg1860.com/139.ipi?id=31B1998CCDC907CF4292D75B1D8069AC29ED04AFD14CF5EFD703A30F5494B0761EEA262C7C6C510657A0DED9235DADE02284458B20EFC181BB3FDBA2C5A27334AE6ECE482B2940E4C6E8D2B6F8A86B53","0"});
                         UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
                        		 command.getSerial(),
                        		 command.getSessionId());
                         seg.writeShort((short) 31022);
                         seg.writeShort((short) 2);
                         seg.write(bytes);
                         write(seg);
                	 }
                	 
                	 
                	 log.info("CmccBusiness to wap ID[" + player.getId() + "]Accountid[" +
                     		player.getAccountId() +"]userid["+getClient(command.getSessionId()).cmccUserId+"] type[" + type +"]");
                 }
        	}catch (Exception ex) {
                log.error("定制移动业务后发放奖品错误。", ex);
            }
        }
    }
    
    class CmccinfogiftProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	try {
        		int type = Integer.parseInt(command.getParam(0));
        		//判断是否领过
        		GiftData giftData;
        		OnlyGiftGroup onlyGiftGroup;
        		if (type == 5){
        			giftData = giftService.getPlayerOnlyGift(299993, player); 
    				onlyGiftGroup = OnlyGiftGroups.getOnlyGiftGroup(299993);
        		}else{
        			giftData = giftService.getPlayerOnlyGift(299999-type, player); 
    				onlyGiftGroup = OnlyGiftGroups.getOnlyGiftGroup(299999-type);
        		}
				
				if(onlyGiftGroup != null ){
					Gift gift = giftData.getGift();
					Vector<OnlyGiftDefine> onlyGiftDefine = onlyGiftGroup.getGifts();
					OnlyGiftDefine onlyNeedGiftDefine = onlyGiftDefine.get(0);
					if(gift.getCount() >= onlyGiftGroup.getMaxCount()){
						String msgtmp = OnlyGiftGroup.getReplaceMessage(onlyGiftGroup.getMessage_maxcount(), gift, onlyNeedGiftDefine,onlyGiftGroup, player);
						if (!"".equals(msgtmp)){
							sendMessage(player.getId(),msgtmp);
						}
					}else {//未领过奖励物品
    					TemplateGrid[] onlyGiftGiveItems = onlyNeedGiftDefine.getAllGiveItem();
            			if(onlyGiftGiveItems == null && onlyGiftGiveItems.length == 0){
            				sendMessage(player.getId(),"无法找到奖励物品，请稍候再试");
            				return;
            			}else{//开始发放礼品
	            			if (player.isOver(onlyGiftGiveItems)) {
	            				String msgtmp = OnlyGiftGroup.getReplaceMessage(onlyGiftGroup.getMessage_bag(), gift, onlyNeedGiftDefine,onlyGiftGroup, player);
	    						if (!"".equals(msgtmp)){
	    							sendMessage(player.getId(),msgtmp);
	    						}
	                            return ;
	                        }
	            			Changed changed = new Changed();
	            			player.addItems(onlyGiftGiveItems, changed, player.getClientDataVersion());
	            			sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);
	                        if(gift.getCount() == 0){//第一次领取
	                        	gift.setCount(onlyGiftGroup.getMaxCount());
	                            gift.setRcount(1);
	                            giftService.savePlayerGift(gift);
	                        }
	            			sendMessage(player.getId(),OnlyGiftGroup.getReplaceMessage(onlyGiftGroup.getMessage_give(), gift, onlyNeedGiftDefine,onlyGiftGroup, player));
	            			}
            				log.info("ID["+player.getId()+"]get CMCCGiftGroup["+onlyGiftGroup.getId()+"]giftId["+gift.getId()+"] Ok");
            			}
    				}
        	}catch (Exception ex) {
                log.error("温州移动业务介绍后发放奖品错误。", ex);
            }
        }
    }
    
    
    
    class ActivationCoderesultProcessor implements CommandProcessor{
    	public void process(WorldPlayer player, Command command) throws Exception {
    		ActivationCodeData data = new ActivationCodeData();
    		try {
    			int type = Integer.parseInt(command.getParam(0));
    			String code = command.getParam(1);
	            data.setAccountID(player.getAccountId());
	            data.setPlayerID(player.getId());
	            data.setActivationcode(code);
	            data.setType(type);
	            activationcodeService.addToQueue(data);
    		}catch (Exception ex) {
    			log.error("将角色id（" + data.getPlayerID() + "）数据放入ActivationCode队列出错。", ex);
    		}
    	}
    }
    
    class CMCCgoRecommendProcessor implements CommandProcessor{
    	public void process(WorldPlayer player, Command command) throws Exception {
    		ActivationCodeData data = new ActivationCodeData();
    		try {
    			int type = Integer.parseInt(command.getParam(0));
    			if (type == 1){
    				sendGotoMap(player.getId(), (short)1,(short)13,(short)16);
    			}else if (type == 2){
            		String rawversion = getClient(command.getSessionId()).rawVersion;
            		if (rawversion == null){
            			rawversion = "";
            		}
            		String cityname = getClient(command.getSessionId()).cityname;
            		if (cityname!=null && Server.CMCC_jilin_cityname.contains(cityname)){//吉林移动
            			if (Server.iMoneyType == Server.IMONEY_TYPE_CMCC){
                    		byte[] bytes = stageService.getTaskBytes((short) 31001,
                                    new String[] {"推荐自己的吉林移动号码的好友玩明珠幻想吧,一起游戏才更有意思！在我这里直接输入他的手机号，就可以喽~~\n1.现在就输入\n2.等等，我先查查电话簿",
                                    "朋友的手机号:", "recommended "});
                            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                    GET_FILE_OK,
                                    command.getSerial(),
                                    command.getSessionId());
                            seg.writeShort((short) 31001);
                            seg.writeShort((short) 2);
                            seg.write(bytes);
                            write(seg);
                            log.info("Recommended jilin ID[" + player.getId() + "]Accountid[" +
                            		player.getAccountId() +"]TRY");
                    	}
            		}else if (cityname!=null && Server.CMCC_zhejiang_cityname.contains(cityname)){//浙江移动
            			if (Server.iMoneyType == Server.IMONEY_TYPE_CMCC){
                    		byte[] bytes = stageService.getTaskBytes((short) 31001,
                                    new String[] {"推荐自己的浙江移动号码的好友玩明珠幻想吧,一起游戏才更有意思！在我这里直接输入他的手机号，就可以喽~~\n1.现在就输入\n2.等等，我先查查电话簿",
                                    "朋友的手机号:", "recommended "});
                            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                    GET_FILE_OK,
                                    command.getSerial(),
                                    command.getSessionId());
                            seg.writeShort((short) 31001);
                            seg.writeShort((short) 2);
                            seg.write(bytes);
                            write(seg);
                            log.info("Recommended jiangsu ID[" + player.getId() + "]Accountid[" +
                            		player.getAccountId() +"]TRY");
                    	}
            		}else{
            			sendMessage("这是吉林移动、浙江移动与明珠幻想合作的推广活动，新注册的吉林、浙江移动用户才能使用。",command.getSerial(),command.getSessionId());
            		}
    				
    			}else if (type == 3){
    				String rawversion = getClient(command.getSessionId()).rawVersion;
            		if (rawversion == null){
            			rawversion = "";
            		}
            		String cityname = getClient(command.getSessionId()).cityname;
            		if (cityname!=null && Server.CMCC_jilin_cityname.contains(cityname)){//吉林移动
            			if (Server.iMoneyType == Server.IMONEY_TYPE_CMCC){
            				/**
            			     * 查询成功推荐的玩家信息。
            			     * requestId        int             请求ID
            			     * userId           String          用户平台ID
            			     */
                        	UWAPSegment seg = new UWAPSegment(ServerConstants.CMCC_QUERY_RECOMMEND);
                        	seg.writeInt(-1); 
                            seg.writeString(getClient(command.getSessionId()).cmccUserId);
                            Server.instance.authSession.write(seg);
                            log.info("CMCC_QUERY_RECOMMEND jilin to Account ID[" + player.getId() + "]Accountid[" +
                            		player.getAccountId() +"]userid["+getClient(command.getSessionId()).cmccUserId+"]");
                    	}
            		}else if (cityname!=null && Server.CMCC_zhejiang_cityname.contains(cityname)){//浙江移动 
            			if (Server.iMoneyType == Server.IMONEY_TYPE_CMCC){
            				/**
            			     * 查询成功推荐的玩家信息。
            			     * requestId        int             请求ID
            			     * userId           String          用户平台ID
            			     */
                        	UWAPSegment seg = new UWAPSegment(ServerConstants.CMCC_QUERY_RECOMMEND);
                        	seg.writeInt(-1); 
                            seg.writeString(getClient(command.getSessionId()).cmccUserId);
                            Server.instance.authSession.write(seg);
                            log.info("CMCC_QUERY_RECOMMEND jiangsu to Account ID[" + player.getId() + "]Accountid[" +
                            		player.getAccountId() +"]userid["+getClient(command.getSessionId()).cmccUserId+"]");
                    	}
            		}else{
            			sendMessage("这是吉林、浙江移动与明珠幻想合作的推广活动，新注册的吉林、浙江移动用户才能使用。",command.getSerial(),command.getSessionId());
            		}
    			}
    		}catch (Exception ex) {
    			log.error("吉林推荐出错。", ex);
    		}
    	}
    }
    
    //mengjie add end

    //leo add
    class AutoUseItemProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
            try {
                int itemId = Integer.valueOf(command.getParam(0));

                IItem item = Items.getTemplate(itemId).newInstance();
                if (item == null) {
                    Utils.log(log, player.getId(), command.getAppType(),
                              "AutoUse[" + itemId + "]ItemId[" +
                              itemId + "] Error");
                } else {
                	int petId = -1;
                    Changed changed = new Changed();
                    int oldLevel = player.getLevel();
                    Utils.log(log, player.getId(), command.getAppType(),
                              "AutoUse[" + itemId + "]Item[" +
                              Utils.getHexdump(item.toDbBytes()) +
                              "]Money[" +
                              player.getMoeny() + "]TRY");
                    if (item instanceof IEffectItem) {
                        boolean successed = false;
                        try {
                        	int[] result = bufService.playerUseItem(player,
                                    (IEffectItem) item,
                                    changed,true,playerId2Clients.get(player.getId()));
                            successed = result[0] == 1 ? true : false;
                            petId = result[1];
                            //mengjie add
                            checkLevelChangedAndSendTips(player, changed, command.getSerial(), command.getSessionId(), oldLevel);
                        } catch (UseItemException ex) {
                            sendMessage(ex.getMessage(), command.getSerial(),
                                            command.getSessionId());
                            return;
                        }
                        if (!successed) {
                            sendMessage("现在还不能使用此物品", command.getSerial(),
                                            command.getSessionId());
                        }
                    }
                    sendGetItem(changed, command.getSerial(),
                                    command.getSessionId(),
                                (byte) 4);
                    //使用召唤宠物物品后，自动装备
                    if (changed != null) {
                    	Pet pet = changed.getPeton(IItem.TYPE_PET);
                    	if (pet != null){
                    		//mengjie add 自动装备宠物
                    		if (player.getPet() == null){
                    			if (pet.getFavor() <= 15 || 
                    					pet.getLevel() > player.getLevel() ||
                    					player.getLevel() < 8){
                    				
                    			}else{
                    				player.setPet(pet);
                    				byte[] bytes = stageService.getTaskBytes((short) 31033,
                    						new String[] {String.valueOf(pet.getId())});
                    				UWAPSegment seg = new UWAPSegment(ClientConstants.
                    						GET_FILE_OK);
                    				seg.writeShort((short) 31033);
                    				seg.writeShort((short) 2);
                    				seg.write(bytes);
                    				connectService.writeTo(seg, player.getId());
                    			}
                    		}
                    	}
                    }
                    //判断新版本客户端，同步宠物装备信息
                    if (petId > 0) {
                		Pet pet = player.getPet(petId);
                		if (pet != null) {
                			try{
                    			UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
                    			seg.writeShort(ClientConstants.EXTEND_PROTOCOL_PETEQU_LOGIN);
                    			seg.writeInt(petId);
                    			Grid[] usedEquipmentsTemp = pet.getUsedEquipments();
                    			if (usedEquipmentsTemp != null){
                    				for (int jj = 0;jj<pet.getUsedEquipmentinfo().length;jj++){
                    					seg.write((byte) pet.getUsedEquipmentinfo()[jj]);
                    					if (usedEquipmentsTemp[jj] != null){
                    						if (pet.getUsedEquipmentinfo()[jj] == 1){
                    							IEquipment equtmp = (IEquipment) usedEquipmentsTemp[jj].item;
                    							equtmp.setDataVersion(player.getClientDataVersion());
                    							seg.write(equtmp.toClientBytesWithLevel(pet.getLevel()));
                    						}
                    					}
                    				}
                    			} else {
                    				for (int jj = 0;jj<pet.getUsedEquipmentinfo().length;jj++){
                    					seg.write((byte) pet.getUsedEquipmentinfo()[jj]);
                    				}
                    			}
                    			// 发送宠物升级所需升级经验
                    			seg.writeInt(Utils.getPetUpLevelExp(pet.getLevel()));
                    			
                    			//发送宠物阵营宝石效果
                        		CampData campData = getCampMainService().getCampData(player.getCamp());
                        		int value = 0;
                        		if(campData != null){
                    		    	List<CampSkillData> list = campData.getSkillDataList();
                    		    	for(int a = 0; a < list.size(); a++){
                    		    		CampSkillData temp = (CampSkillData) list.get(a);
                    		    		CampSkillLevel temp1 = CampConfig.campSkills.get(temp.getEffect()).getLevel(temp.getLevel());
                    		    		
                    		    		if(temp1 == null || temp1.getParm1() == 0){
                    		    			continue;
                    		    		}else{
                    		    			if(temp.getEffect() == Buf.CAMP_STONE){//阵营科技中只有阵营宝石buff取值下发
                    		    				value = temp1.getParm1();
                    		    				break;
                    		    			}
                    		    		}
                    		    	}
                    		    }
                        		seg.writeInt(value);
                    			
                    			connectService.writeTo(seg, player.getId());
                    		} catch (Exception e) {
                    			log.debug(e, e);
                    		}
                    	}
                    }
                    Utils.log(log, player.getId(), command.getAppType(),
                              "itemId[" + itemId + "]Item[" +
                              Utils.getHexdump(item.toDbBytes()) +
                              "]Money[" +
                              player.getMoeny() + "]");
                }
            }catch (Exception ex) {
                log.error("自动使用失败", ex);
            }
        }
    }

    class GiftDefineProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
            try {
                int groupId = Integer.valueOf(command.getParam(0));

                GiftData giftData = giftService.getPlayerGift(groupId, player);

                if(giftData != null){
                    GiftGroup group = giftData.getGiftGroup();
                    Gift gift = giftData.getGift();
                    GiftDefine giftDefine = group.getAvailableGift(player.getLevel());
                    if(command.getParamCount() > 1){
                    	sendMessage(group.getMessage_aboutmsg(gift, giftDefine, player), command.getSerial(), command.getSessionId());
                    	return;
                    }

                    if(giftDefine == null){
                        sendMessage("您的级别不符合领取条件~", command.getSerial(), command.getSessionId());
                    }else{
                    	if(giftDefine.getAllCount() != -1){
                    		if(!GiftGroupAllCount.hasCount(group.getId(), giftDefine.getId(), giftDefine.getAllCount())){
                    			sendMessage(group.getMessage_allcount(gift, giftDefine, player), command.getSerial(), command.getSessionId());
                    			return;
                    		}
                    	}
                        int giftStatus = giftDefine.isAvailable(gift, player, group);

                        switch(giftStatus){
                            case GiftGroup.GIFT_AVAILABLE:
                            case GiftGroup.GIFT_ERROR_REPEAT:
                                boolean newTime = giftDefine.isNewTime(gift, player);

                                if(!newTime && giftStatus == GiftGroup.GIFT_ERROR_REPEAT){
                                    sendMessage(group.getMessage_repeat(gift, giftDefine, player), command.getSerial(), command.getSessionId());
                                }else{
                                    List<TemplateGrid[]> needGrid = giftDefine.getNeedItems();
                                    TemplateGrid[] giveGrid = giftDefine.getGiveItems();
                                   
                                    if (needGrid.size() > 0 && !player.containsOR(needGrid)) {
                                        sendMessage("没有足够的物品", command.getSerial(), command.getSessionId());

                                        throw new ITimesException("没有足够的物品", command.getSerial(), command.getSessionId(), command.getAppType());
                                    }
                                    if (player.isOver(giveGrid)) {
                                        sendMessage("您背包空余的位置不够", command.getSerial(), command.getSessionId());

                                        throw new ITimesException("您背包空余的位置不够", command.getSerial(), command.getSessionId(), command.getAppType());
                                    }

                                    Changed changed = new Changed();

                                    synchronized (player) {
                                        int needcount = player.completeRemoveItemOR(needGrid, changed);//需要删的个数
                                        if(needcount < 0){
                                        	 sendMessage("需要物品数量不足", command.getSerial(), command.getSessionId());
                                             throw new ITimesException("需要物品数量不足", command.getSerial(), command.getSessionId(), command.getAppType());
                                        }
                                        boolean flag = player.addItems(giveGrid, changed, player.getClientDataVersion());//是否添加
                                        if(flag == false){
                                        	IItemTemplate tempitem = null;
                                        	for(TemplateGrid grids : giveGrid){
                                        		tempitem = grids.template;
                                        	}
                                        	if(tempitem != null){
                                        		if(player.isFull() || player.getItemCount(tempitem.getItemId()) + needcount > 99){//背包满或者堆叠数大于99,发邮件
                                        			int itemid = tempitem.getItemId();
                                        			IItem iit = Items.getTemplate(itemid).newInstance();
                									byte[] att = ItemUtils.item2dbAttachment(iit,needcount);
                									mailService.sendMail(player.getId(),
                											player.getPlayerName(), -1, "系统",
                											iit.getName() + "*" + needcount, "获得的礼物", att, 0,
                											true);
                									//player.completeRemoveItem(cutid, cutcount * count, changed);
                									sendMessage(player.getId(),
                											"你的背包满了，已经把物品邮寄到您的邮箱!");
                                        		}else{
                                        			player.addItem(tempitem, needcount, changed, player.getClientDataVersion());
                                        		}
                                        	}
                                        }
                                    }

                                    playerService.checkPlayer(player);
                                    sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);

                                    if(newTime){
                                        gift.setRcount(0);
                                    }

                                    gift.setRcount(gift.getRcount() + 1);
                                    gift.setCount(gift.getCount() + 1);
                                    gift.setModifytime(new Date(System.currentTimeMillis()));
                                    giftService.savePlayerGift(gift);

                                    sendMessage(group.getMessage_give(gift, giftDefine, player), command.getSerial(), command.getSessionId());


                                    Utils.log(log, player.getId(), command.getAppType(),
                                              "GiftGroup[" + group.getId() + "]GifeDefine[" + giftDefine.getId() +
                                              "]Changed[" + Utils.getHexdump(changed.toBytes()) +
                                              "]");
                                    if(giftDefine.getAllCount() != -1){
                                		GiftGroupAllCount.addCount(group.getId(), giftDefine.getId());
                                	}
                                }

                                break;
                            case GiftGroup.GIFT_ERROR_COUNT:
                                sendMessage(group.getMessage_count(gift, giftDefine, player), command.getSerial(), command.getSessionId());

                                break;
                            case GiftGroup.GIFT_ERROR_TIME:
                                sendMessage(group.getMessage_time(gift, giftDefine, player), command.getSerial(), command.getSessionId());

                                break;
                            case GiftGroup.GIFT_ERROR_ITEM:
                                sendMessage(group.getMessage_item(gift, giftDefine, player), command.getSerial(), command.getSessionId());

                                break;
                            case GiftGroup.GIFT_ERROR_BAG:
                                sendMessage(group.getMessage_bag(gift, giftDefine, player), command.getSerial(), command.getSessionId());

                                break;
                            case GiftGroup.GIFT_INFO_SUPERQ://SUPERQ
                            	SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMddHHmmss");
                            	String str="20090115000000";
                            	ParsePosition pos = new ParsePosition(0);
                            	Date dt=formatter.parse(str,pos);
                            	long time_tmp = player.getCreateTime().getTime() - dt.getTime();
                                if (time_tmp>0){
                                	sendMessage(group.getMessage_repeat(gift, giftDefine, player), command.getSerial(), command.getSessionId());
                                	break;
                                }
                            	if ((!player.isSubscribe()) || (Server.iMoneyType != Server.IMONEY_TYPE_QQ)){
                            		sendMessage(group.getMessage_item(gift, giftDefine, player), command.getSerial(), command.getSessionId());
                            		break;
                            	}
                            	boolean newTime2 = giftDefine.isNewTime(gift, player);
                            	//if(newTime2){
                        		List<TemplateGrid[]> needGrid = giftDefine.getNeedItems();
                                TemplateGrid[] giveGrid = giftDefine.getGiveItems();

                                if (needGrid.size() > 0 && !player.containsOR(needGrid)) {
                                    sendMessage("没有足够的物品", command.getSerial(), command.getSessionId());

                                    throw new ITimesException("没有足够的物品", command.getSerial(), command.getSessionId(), command.getAppType());
                                }

                                if (player.isOver(giveGrid)) {
                                    sendMessage("您背包空余的位置不够", command.getSerial(), command.getSessionId());

                                    throw new ITimesException("您背包空余的位置不够", command.getSerial(), command.getSessionId(), command.getAppType());
                                }

                                Changed changed = new Changed();

                                synchronized (player) {
                                    player.completeRemoveItemOR(needGrid, changed);
                                    player.addItems(giveGrid, changed, player.getClientDataVersion());
                                }

                                playerService.checkPlayer(player);
                                sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);

                                if(newTime2){
                                    gift.setRcount(0);
                                }

                                gift.setRcount(gift.getRcount() + 1);
                                gift.setCount(gift.getCount() + 1);
                                gift.setModifytime(new Date(System.currentTimeMillis()));
                                giftService.savePlayerGift(gift);

                                sendMessage(group.getMessage_give(gift, giftDefine, player), command.getSerial(), command.getSessionId());


                                Utils.log(log, player.getId(), command.getAppType(),
                                          "GiftGroup[" + group.getId() + "]GifeDefine[" + giftDefine.getId() +
                                          "]Changed[" + Utils.getHexdump(changed.toBytes()) +
                                          "]");
                                if(giftDefine.getAllCount() != -1){
                            		GiftGroupAllCount.addCount(group.getId(), giftDefine.getId());
                            	}
                                break;
                            case GiftGroup.GIFT_INFO_SUPERQ_nomal://SUPERQ
                            	if ((!player.isSubscribe()) || (Server.iMoneyType != Server.IMONEY_TYPE_QQ)){
                            		sendMessage(group.getMessage_item(gift, giftDefine, player), command.getSerial(), command.getSessionId());
                            		break;
                            	}
                            	boolean newTime3 = giftDefine.isNewTime(gift, player);
                            	//if(newTime2){
                        		List<TemplateGrid[]> needGrid1 = giftDefine.getNeedItems();
                                TemplateGrid[] giveGrid1 = giftDefine.getGiveItems();

                                if (needGrid1.size() > 0 && !player.containsOR(needGrid1)) {
                                    sendMessage("没有足够的物品", command.getSerial(), command.getSessionId());

                                    throw new ITimesException("没有足够的物品", command.getSerial(), command.getSessionId(), command.getAppType());
                                }

                                if (player.isOver(giveGrid1)) {
                                    sendMessage("您背包空余的位置不够", command.getSerial(), command.getSessionId());

                                    throw new ITimesException("您背包空余的位置不够", command.getSerial(), command.getSessionId(), command.getAppType());
                                }

                                Changed changed1 = new Changed();

                                synchronized (player) {
                                    player.completeRemoveItemOR(needGrid1, changed1);
                                    player.addItems(giveGrid1, changed1, player.getClientDataVersion());
                                }

                                playerService.checkPlayer(player);
                                sendGetItem(changed1, command.getSerial(), command.getSessionId(), (byte)73);

                                if(newTime3){
                                    gift.setRcount(0);
                                }

                                gift.setRcount(gift.getRcount() + 1);
                                gift.setCount(gift.getCount() + 1);
                                gift.setModifytime(new Date(System.currentTimeMillis()));
                                giftService.savePlayerGift(gift);

                                sendMessage(group.getMessage_give(gift, giftDefine, player), command.getSerial(), command.getSessionId());


                                Utils.log(log, player.getId(), command.getAppType(),
                                          "GiftGroup[" + group.getId() + "]GifeDefine[" + giftDefine.getId() +
                                          "]Changed[" + Utils.getHexdump(changed1.toBytes()) +
                                          "]");
                                if(giftDefine.getAllCount() != -1){
                            		GiftGroupAllCount.addCount(group.getId(), giftDefine.getId());
                            	}
                        }
                    }
                }else{
                    sendMessage("取得礼物数据失败", command.getSerial(), command.getSessionId());
                }
            }catch (Exception ex) {
//                log.error("取得礼物数据失败", ex);
            	log.info("取得礼物数据失败" + ex.getMessage());
            }
        }
    }
    class GiftItemProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	/*
        	int type = Integer.valueOf(command.getParam(0));
        	int giftcount = Integer.valueOf(command.getParam(1));
        	int Itemid = Integer.valueOf(command.getParam(2));
        	int GiftItemid = Integer.valueOf(command.getParam(3));
        	int AddItemid = Integer.valueOf(command.getParam(4));
        	int Mailflag = Integer.valueOf(command.getParam(5));
        	String Mailtitle = command.getParam(6);
        	
        	IItem item = Items.getTemplate(Itemid).newInstance();
            if (item == null) {
                Utils.log(log, player.getId(), command.getAppType(),"gift item ItemId[" + Itemid + "] Error");
            } else {
            	if (!player.hasItem(Itemid,giftcount)){
        			sendMessage("您没有" + giftcount + "个" + item.getName() + "。", command.getSerial(), command.getSessionId());
        		}else{
        			if (type == 1){
        				//选择好友
	        			if(0 == player.getFriends().length){
                    		sendMessage(player.getId(),"您还没有好友,请有了好友再使用吧！");
                    	}else{
                    		Friend[] friends = player.getFriends();
                    		UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                            seg.writeShort((short) 10233);
                            seg.writeString("好友列表(选择赠送对象)");
                            seg.write((byte) 3);
                            seg.writeShort((short) friends.length);
                            for ( int j = 0; j < friends.length; j++) {
                                seg.writeInt(friends[j].getId());
                                String  tempNameString ;
                                tempNameString = friends[j].getName();
                                WorldPlayer dest = playerService.getWorldPlayer(friends[j].getId());
                                if (dest!=null && dest.online()) {//在线
                                	tempNameString = tempNameString.concat(" 在线 好友度 ");
                                }else{//离线
                                	tempNameString = tempNameString.concat(" 离线 好友度 ");
                                }
                                tempNameString = tempNameString+friends[j].getFavorite();
                                seg.writeString(tempNameString);
                                seg.writeInt(Utils.CLR_WHITE);
                            }
                            seg.write((byte) 1);
                            seg.writeString("选择");
                            seg.writeString("giftItem 2 " + giftcount + " " + Itemid + " " + 
    	                            GiftItemid + " " + AddItemid + " " + 
    	                            Mailflag + " " + Mailtitle);
                            connectService.writeTo(seg, player.getId());
                    	}
                	}else{
                		//2次返回，返回好友
                		int otherplayerId = Integer.valueOf(command.getParam(7));
                		Changed changed = new Changed();
            			if (player.completeRemoveItem(item, giftcount, changed) == null){
            				sendMessage("您没有" + giftcount + "个" + item.getName() + "。", command.getSerial(), command.getSessionId());
    	        		}else{
    	        			IItem itemgift = Items.getTemplate(GiftItemid).newInstance();
    	        			if (itemgift != null){
    	        				if (Mailflag == 1){
        	        				//发送速递给对方
    	        					Mailtitle = Mailtitle.replaceAll("player", player.getPlayerName());
        	        				mailService.sendMail(otherplayerId, "", -1, "系统", "“" + player.getPlayerName()+"”送给您的礼物", Mailtitle,
        	                                 ItemUtils.item2dbAttachment(itemgift, giftcount), 0, true);
        	        				IItem itemmine = Items.getTemplate(AddItemid).newInstance();
        	        				if (player.completeAddItem(itemmine, giftcount, changed) == null){
        	        					connectService.sendMessage(player.getId(),"由于背包满，您的奖励品已经邮寄到邮箱中，请注意查收。");
        	        					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
        	        							itemmine.getName() + "*" + giftcount, "请注意查收您的奖励品",
        	        							ItemUtils.item2dbAttachment(itemmine, giftcount), 0, true);
        	        				}
    	        				}
    	        			}
    	        		}
            			sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);
                	}
        			
        		}
            }
            */
        }
    }
    
    class UpdateClientWebProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	if (Server.iMoneyType == Server.IMONEY_TYPE_PIP) {
        		byte[] bytes = stageService.getTaskBytes((short) 31022,
                        new String[] {
                        "http://hx.pipgame.cn","1"});
                UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
               		 command.getSerial(),
               		 command.getSessionId());
                seg.writeShort((short) 31022);
                seg.writeShort((short) 2);
                seg.write(bytes);
                write(seg);
        	} else if (Server.iMoneyType == Server.IMONEY_TYPE_CMCC) {
        		byte[] bytes = stageService.getTaskBytes((short) 31022,
                        new String[] {
                        "http://218.206.80.188/wpg/dia.do?id=4","1"});
                UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
               		 command.getSerial(),
               		 command.getSessionId());
                seg.writeShort((short) 31022);
                seg.writeShort((short) 2);
                seg.write(bytes);
                write(seg);
        	} else if (Server.iMoneyType == Server.IMONEY_TYPE_QQ) {
        		byte[] bytes = stageService.getTaskBytes((short) 31022,
                        new String[] {
                        "http://119.147.16.18:8080/qqitimesipd/itimes_update.jsp","1"});
                UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
               		 command.getSerial(),
               		 command.getSessionId());
                seg.writeShort((short) 31022);
                seg.writeShort((short) 2);
                seg.write(bytes);
                write(seg);
        	}
        	 
        }
    }
    class WorldMapGoProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	int mapId = Integer.valueOf(command.getParam(0));
        	int mapx = Integer.valueOf(command.getParam(1));
        	int mapy = Integer.valueOf(command.getParam(2));
        	Changed changed = new Changed();
        	//if(player.completeRemoveItem(210025,1, changed) != null){
//        		player.setJumpMapId(player.getMapId());
    		GameMap map = player.getMap();
    		if(map != null){
//        			player.setJumpX((short)(player.getX() / map.getTileWidth()));
//            		player.setJumpY((short)(player.getY() / map.getTileHeight()));
        		sendGotoMap(player.getId(), (short)mapId, (short)mapx, (short)mapy);
    		}
    		sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);
			/*}else{
				sendMessage("去往目的地需要一个世界地图卷轴，请您到卖场购买或回到原地。", command.getSerial(), command.getSessionId());
			}*/
        }
    }
    //加入阵营
    class AddCampProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	try {
        		int Type = Integer.valueOf(command.getParam(0));
                int campType = Integer.valueOf(command.getParam(1));
                if (Type == 1){//阵营传送
                	if (campType == 0){//瓦伊特镇传送去阵营地图
                		if (player.getCamp()>0){//已有阵营
                    		sendGotoMap(player.getId(), (short)17, (short)1, (short)35);
                    	}else{//无阵营
                    		sendGotoMap(player.getId(), (short)18, (short)1, (short)35);
                    	}
                	}else if (campType == 1){//2010五一活动场景限制 50级 有阵营
                		/*if(player.getLevel() < 50 ){
                			sendMessage("您的等级低于活动参与最低标准，请抓紧升级吧", command.getSerial(), command.getSessionId());
                        	return;
                		}else*/ 
                		if (player.getCamp() == 0){//无阵营
                			if (Server.iMoneyType == Server.IMONEY_TYPE_PIP){
	                			sendMessage("请先去拜访瓦伊特的阵营地图传送师, 选择自己的信仰吧", command.getSerial(), command.getSessionId());
	                        	return;
                			}else{
                				sendGotoMap(player.getId(), (short)833, (short)52, (short)7);
                			}
                		}else {
                			sendGotoMap(player.getId(), (short)833, (short)52, (short)7);
                		}
                	}else if(campType == 2){//这个是2010六一足球场的传送
                		
                		/*if(player.getLevel() <50 ){
                			sendMessage("您的等级低于活动参与最低标准，请抓紧升级吧", command.getSerial(), command.getSessionId());
                        	return;
                		}else*/ 
                		if (player.getCamp() == 0){//无阵营
                			if (Server.iMoneyType == Server.IMONEY_TYPE_PIP){
	                			sendMessage("请先去拜访瓦伊特的阵营地图传送师, 选择自己的信仰吧", command.getSerial(), command.getSessionId());
	                        	return;
                			}else{
                				if(player.getTeam()!=null){
                					sendMessage("组队不能进入活动区域哦，请解散队伍再进入吧。", command.getSerial(), command.getSessionId());
    	                        	return;
                				}else{
                					sendGotoMap(player.getId(), (short)3745, (short)28, (short)19);
                				}
                			}
                		}else {
                			if(player.getTeam()!=null){
            					sendMessage("组队不能进入活动区域哦，请解散队伍再进入吧。", command.getSerial(), command.getSessionId());
	                        	return;
            				}else{
            					sendGotoMap(player.getId(), (short)3745, (short)28, (short)19);
            				}
                		}
                	}
                }else if (Type == 2){//加入阵营
                	/*if(player.getLevel() < 50){
                    	sendMessage("请升级到50级以后，再加入阵营吧", command.getSerial(), command.getSessionId());
                    	return;
                    }*/
                    if (player.getCamp()>0){
                    	sendMessage("您已经有阵营了哦，不要叛变啦~", command.getSerial(), command.getSessionId());
                    }else{
                    	if(campType != 1 && campType != 2){
                    		int taskgetid = Integer.valueOf(command.getParam(2));
                    		log.info("join campType[" + campType + "] taskgetid[" + taskgetid + "]");
                    		if(taskgetid > 1000){
                    			switch(taskgetid){
                    			case 1001:
                    			case 1002:
                    			case 1003:
                    				campType = 1;
                    				break;
                    			case 1004:
                    			case 1005:
                    			case 1006:
                    				campType = 2;
                    				break;
                    			}
                    		}
                    	}
                    	if(campType != 1 && campType != 2){
                    		int newCampType = campMainService.getMinCampType();
                    		log.info("join campType[" + campType + "] error changeType[" + newCampType);
                    		campType = newCampType;
                    	}
                    	if(!campMainService.testCanAddCamp(campType)){
                    		sendMessage("该阵营过于强大,无法加入了", command.getSerial(), command.getSessionId());
                    		return;
                    	}
                    	
                    	if (campType == 1){
                    		player.setCamp((byte) campType);
                    		
                    		sendMessage("您已成功加入黑暗阵营，赶紧去找光明阵营的玩家宣战吧~", command.getSerial(), command.getSessionId());
                    		
                    		if (player.hasTask((short)29801)){//有阵营任务，结束任务给与奖励
                    			int itemid = Integer.valueOf(command.getParam(2));//奖励分之
                    			byte[] bytes = stageService.getTaskBytes((short) 31051,
                                        new String[] {"29801",String.valueOf(itemid)});
                                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                        GET_FILE_OK, command.getSerial(),
                                        command.getSessionId());
                                seg.writeShort((short) 31051);
                                seg.writeShort((short) 2);
                                seg.write(bytes);
                                write(seg);
                    		}
                    		Changed change = new Changed();
                    		change.addProperty(change.REFRESH_CAMPTYPE, campType);
                    		connectService.sendGetItem(change, player.getId(), (byte) 26);
                    		sendGotoMap(player.getId(), (short)17, (short)2, (short)30);
                    	}else if (campType == 2){
                    		player.setCamp((byte) campType);
                    		sendMessage("您已成功加入光明阵营，赶紧去找黑暗阵营的玩家宣战吧~", command.getSerial(), command.getSessionId());
                    		if (player.hasTask((short)29801)){//有阵营任务，结束任务给与奖励
                    			int itemid = Integer.valueOf(command.getParam(2));//奖励分之
                    			byte[] bytes = stageService.getTaskBytes((short) 31051,
                                        new String[] {"29801",String.valueOf(itemid)});
                                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                        GET_FILE_OK, command.getSerial(),
                                        command.getSessionId());
                                seg.writeShort((short) 31051);
                                seg.writeShort((short) 2);
                                seg.write(bytes);
                                write(seg);
                    		}
                    		Changed change = new Changed();
                    		change.addProperty(change.REFRESH_CAMPTYPE, campType);
                    		connectService.sendGetItem(change, player.getId(), (byte) 26);
                    		sendGotoMap(player.getId(), (short)17, (short)2, (short)41);
                    	}
                    	
                    	//刷新阵营即时数据
                    	campMainService.refreshCampPlayerCount(campType);
                    	//同步玩家频道
                    	addToChannels(command.getSessionId(), getPlayerChannels(player));
                    }
                }else if(Type==3){//感恩节活动场景 等级限制  
                	if(player.getTeam()!=null){
    					sendMessage("组队不能进入活动区域哦，请解散队伍再进入吧。", command.getSerial(), command.getSessionId());
                    	return;
    				}else{
//    					sendGotoMap(player.getId(), (short)4449, (short)25, (short)25);
    					//菊花台活动传送
    					sendGotoMap(player.getId(), (short)2850, (short)16, (short)96);
    				}
                }
        	}catch (Exception ex) {
                log.error("添加阵营信息错误。", ex);
            }
        }
        
//        private void taskCompleted(WorldPlayer player, Command command,short taskId,short id) throws Exception {
//        	Utils.log(log, player.getId(), command.getAppType(),
//                    "TaskId[" + taskId + "]Money[" + player.getMoeny() +
//                    "]TRY");
//          if (id < 1000 || player.hasTask(taskId)) {
//              TaskAward award = TaskAwards.getTaskAward(taskId);
//              if (award != null) {
//                  TemplateGrid[] remove = award.getRemoveItems(id);
//                  if (remove.length > 0 && !player.contains(remove)) {
//                      sendMessage("没有足够的物品", command.getSerial(),
//                    		  command.getSessionId());
////                      return;
//                      throw new ITimesException("没有足够的物品", command.getSerial(),
//                    		  command.getSessionId(),
//                    		  command.getAppType());
//                  }
//                  //mengjie add 防刷处理20090603
//                  if ((taskId % 100 == 0) && (remove.length <= 0) && (award.getMoney(id) > 0)){
//                  	int taskidtmp = taskId*1000+id;
//                  	if (player.playercompletedTask.contains(new Integer(taskidtmp))){
//                  		throw new ITimesException("不能重复领取", command.getSerial(),
//                  				command.getSessionId(),
//                  				command.getAppType());
//                  	}else{
//                  		player.playercompletedTask.add(new Integer(taskidtmp));
//                  	}
//                  }              	
//                  if (award.getMoney(id) < 0 &&
//                      player.getMoeny() < -award.getMoney(id)) {
//                      sendMessage("没有足够的钱", command.getSerial(),
//                    		  command.getSessionId());
////                      return;
//                      throw new ITimesException("没有足够的钱", command.getSerial(),
//                    		  command.getSessionId(),
//                    		  command.getAppType());
//                  }
//                  TemplateGrid[] add = award.getAddItems(id);
//                  if (player.isOver(add)) {
//                      if(player.isReallyOver(add, remove)){
//                          sendMessage("您背包空余的位置不够", command.getSerial(),
//                        		  command.getSessionId());
//                          throw new ITimesException("您背包空余的位置不够", command.getSerial(),
//                        		  command.getSessionId(),
//                        		  command.getAppType());
//                      }
//                  }
//                  Changed changed = new Changed();
//                  synchronized (player) {
//                      player.completeRemoveItem(remove, changed);
//                      player.addItems(add, changed, player.getClientDataVersion());
//                      int money = award.getMoney(id);
//                      if (money > 0)
//                          player.addMoney(money, changed);
//                      else
//                          player.decMoney( -money, changed);
//                      if (player.getMaxLevel() > player.getLevel()) {
//                          int exp = award.getExp(id);
//                        //20091201 活动期间完成任务所得经验增加 20%
//                          exp = (int)(exp * 1.25);
//                          int level_tmp = player.getLevel();
//                          player.addExp(exp, changed);
//                          //推荐人
//                          if ((level_tmp<player.getLevel()) && ((player.getLevel()==50)||(player.getLevel()==65)||(player.getLevel()==80)||(player.getLevel()==90)||(player.getLevel()==100))){
//                          	level_tmp = player.getLevel();
//                          	if (Server.iMoneyType == Server.IMONEY_TYPE_PIP){
//                          		LevelUpNotifyMessage msg = new LevelUpNotifyMessage(player.getAccountId(), player.getId(), player.getLevel(), Server.getGameCode());
//                          	    accountSkeleton.send(msg);
//                                  Utils.log(log, player.getId(), -1,
//                                          "RecommendBalance--accountid[" + player.getAccountId() + "]--" +
//                                          		"accountname[" + player.getAccountName() + "]" +
//                                          				"LEVEL["+player.getLevel()+"]Question");
//                              }
//                          }
//                      }
//                      int credit = award.getCredit(id);
//                      if (credit > 0) {
//                          player.addCredit(credit, changed);
//                      }
//                      tongService.modifyPlayer(player);
//                      if (id >= 1000)
//                          player.taskCompleted(taskId);
//                      
//                      //广东移动 特殊处理 毒瘤！
//                      String cityname = player.getcityname();
//                      if ((Server.iMoneyType == Server.IMONEY_TYPE_CMCC) && 
//                      		Server.CMCC_guangdong_cityname.contains(cityname)){
////                      if ((player.Cmcc_list.equals("124328141") || player.Cmcc_list.equals("138046130") || player.Cmcc_list.equals("94034796"))
////                      		&& (Server.iMoneyType == Server.IMONEY_TYPE_CMCC)){
//                      	IItemTemplate itemtemplate = Items.getTemplate(200626);
//                  		if (itemtemplate != null) {
//                  			IItem item_tmp = player.completeAddItem(itemtemplate.
//      	                            newInstance(), 1, changed, player.getClientDataVersion());
//                  		}
//                      }
//                      
//                  }
//                  playerService.checkPlayer(player);
//                
//                  UWAPSegment seg = new UWAPSegment(ClientConstants.
//                          TASK_COMPLETED, command.getSerial(), command.getSessionId());
//                  seg.writeShort(taskId);
//                  seg.writeShort(id);
//                  seg.writeBoolean(id >= 1000);
//                  write(seg);
//               
//                   
//                  sendGetItem(changed, command.getSerial(), command.getSessionId(),
//                          (byte) 5);
//                  checkLevelChangedAndSendTips(player, changed, command.getSerial(), command.getSessionId());
//                  Utils.log(log, player.getId(), command.getAppType(),
//                            "TaskId[" + taskId + "]Money[" + player.getMoeny() +
//                            "]Changed[" + Utils.getHexdump(changed.toBytes()) +
//                            "]");
//                 
//              }
//              
//          } else {
//              throw new ITimesException("没有此任务", command.getSerial(),
//            		  command.getSerial(), command.getAppType());
//          }
//        }
    }
        //leo add
    class ArenaSignupProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
            try {
                int arenaType = Integer.valueOf(command.getParam(0));

                switch(arenaType){
                    case ArenaConstants.ARENA_TYPE_ONE:{ //1v1竞技场报名
                        if(Server.instance.arenaSession.arenaValid()){
                        	if (player.getArenaV1Id() < 0){
                        		sendMessage("您还没有创建战队", command.getSerial(), command.getSessionId());
                        	}else{
                        		if (arenaService.findArenaTeamByarenaId(player.getArenaV1Id()) == null){
                        			player.setArenaV1Id(-1);
                        			sendMessage("您还没有创建战队", command.getSerial(), command.getSessionId());
                        		}else{
                        			ArenaTeam arenateam = arenaService.findArenaTeamByarenaId(player.getArenaV1Id());
                            		
                            		if (arenateam!=null){
                            			if(player.getArenaLevel() == 0){
                            				player.setArenaLevel(1000);
                            			}
                            			switch(Server.instance.arenaSession.addArenaQueue(arenaType, player, new WorldPlayer[]{
                            			                player
                            			}, arenateam)){
                                            case ArenaConstants.ARENA_QUEUE_DUPLICATE:
                                            case ArenaConstants.ARENA_QUEUE_DUPLICATE_PLAYER:
                                            case ArenaConstants.ARENA_QUEUE_OTHER:
                                                sendMessage("不要重复排队哦！", command.getSerial(), command.getSessionId());
                                                break;
                                            case ArenaConstants.ARENA_QUEUE_DUPLICATE_OTHER:
                                                sendMessage("您已在别的类型竞技场拍过队，不要重复排队哦！", command.getSerial(), command.getSessionId());
                                                break;
                                            case ArenaConstants.ARENA_QUEUE_ERROR:
                                                sendMessage("排队失败,每位参赛者在同一时间只能在一个队列中。", command.getSerial(), command.getSessionId());
                                                break;
                            			}
                            		}
                        		}
                        	}
                        }else{
                            sendMessage("竞技场还未开放", command.getSerial(), command.getSessionId());
                        }
                    }
                        break;
                    case ArenaConstants.ARENA_TYPE_TWO:{ //2v2竞技场报名
                        if(Server.instance.arenaSession.arenaValid()){
                            if (player.getArenaV2Id() < 0){
                                sendMessage("您还没有创建战队", command.getSerial(), command.getSessionId());
                            }else{
                            	if (arenaService.findArenaTeamByarenaId(player.getArenaV2Id()) == null){
                        			player.setArenaV2Id(-1);
                        			sendMessage("您还没有创建战队", command.getSerial(), command.getSessionId());
                        		}else{
                        			Team team = player.getTeam();
                                    
                                    if(team == null){
                                        sendMessage("您需要和您战队中的一个队友组队才能报名！", command.getSerial(), command.getSessionId());
                                    }else{
                                        ArenaTeam arenateam = arenaService.findArenaTeamByarenaId(player.getArenaV2Id());
                                        
                                        if(arenateam != null){
                                        	if(player.getArenaLevel() == 0){
                                				player.setArenaLevel(1000);
                                			}
                                        	PositionSprite[] ps = team.getPlayers();
                                        	for(int i=0; i<ps.length; i++){
                                        		if(!(ps[i] instanceof WorldPlayer)){
                                        			sendMessage("您不能带着佣兵报名!", command.getSerial(), command.getSessionId());
                                        			return;
                                        		}
                                        	}
                                        	WorldPlayer[] teamPlayers = new WorldPlayer[ps.length];
                                        	for(int i=0; i<ps.length; i++){
                                        		teamPlayers[i] = (WorldPlayer)ps[i];
                                        	}
                                            int arenaCount = 0;
                                            Vector<WorldPlayer> arenaPlayerList = new Vector<WorldPlayer>();
                                            
                                            for(int i = 0; i < teamPlayers.length; i++){
                                                if(teamPlayers[i].getArenaV2Id() == player.getArenaV2Id()){
                                                    arenaCount++;
                                                    arenaPlayerList.add(teamPlayers[i]);
                                                }
                                            }
                                            
                                            if(arenaCount != 2){
                                                sendMessage("您需要和您战队中的一个队友组队才能报名！", command.getSerial(), command.getSessionId());
                                            }else{
                                                WorldPlayer[] queuePlayes = new WorldPlayer[arenaPlayerList.size()];
                                                arenaPlayerList.copyInto(queuePlayes);
                                                
                                                switch(Server.instance.arenaSession.addArenaQueue(arenaType, player, queuePlayes, arenateam)){
                                                    case ArenaConstants.ARENA_QUEUE_DUPLICATE:
                                                        sendMessage("不要重复排队哦！", command.getSerial(), command.getSessionId());
                                                        break;
                                                    case ArenaConstants.ARENA_QUEUE_OTHER:
                                                        sendMessage("您战队的其他成员已经排过队了，不能重复排队", command.getSerial(), command.getSessionId());
                                                        break;
                                                    case ArenaConstants.ARENA_QUEUE_DUPLICATE_PLAYER:
                                                        sendMessage("您队伍中的队友已排过其他类型的竞技场，不要重复排队哦！", command.getSerial(), command.getSessionId());
                                                        break;
                                                    case ArenaConstants.ARENA_QUEUE_DUPLICATE_OTHER:
                                                        sendMessage("您已在别的类型竞技场排过队，不要重复排队哦！", command.getSerial(), command.getSessionId());
                                                        break;
                                                    case ArenaConstants.ARENA_QUEUE_ERROR:
                                                        sendMessage("排队失败,每位参赛者在同一时间只能在一个队列中。", command.getSerial(), command.getSessionId());
                                                        break;
                                                }
                                            }
                                        }
                                    }
                        		}
                            }
                        }else{
                            sendMessage("竞技场还未开放", command.getSerial(), command.getSessionId());
                        }
                    }
                        break;
                    case ArenaConstants.ARENA_TYPE_THREE:{ //3v3竞技场报名
                        if(Server.instance.arenaSession.arenaValid()){
                            if (player.getArenaV3Id() < 0){
                                sendMessage("您还没有创建战队", command.getSerial(), command.getSessionId());
                            }else{
                            	if (arenaService.findArenaTeamByarenaId(player.getArenaV3Id()) == null){
                        			player.setArenaV3Id(-1);
                        			sendMessage("您还没有创建战队", command.getSerial(), command.getSessionId());
                        		}else{
                        			Team team = player.getTeam();
                                    
                                    if(team == null){
                                        sendMessage("您需要和您战队中的两个队友组队才能报名！", command.getSerial(), command.getSessionId());
                                    }else{
                                        ArenaTeam arenateam = arenaService.findArenaTeamByarenaId(player.getArenaV3Id());
                                        
                                        if(arenateam != null){
                                        	if(player.getArenaLevel() == 0){
                                				player.setArenaLevel(1000);
                                			}
                                        	PositionSprite[] ps = team.getPlayers();
                                        	for(int i=0; i<ps.length; i++){
                                        		if(!(ps[i] instanceof WorldPlayer)){
                                        			sendMessage("您的队伍中不能有佣兵！", command.getSerial(), command.getSessionId());
                                        			return;
                                        		}
                                        	}
                                            WorldPlayer[] teamPlayers = new WorldPlayer[ps.length];
                                            for(int i=0; i<ps.length; i++){
                                            	teamPlayers[i] = (WorldPlayer)ps[i];
                                            }
                                            int arenaCount = 0;
                                            Vector<WorldPlayer> arenaPlayerList = new Vector<WorldPlayer>();
                                            
                                            for(int i = 0; i < teamPlayers.length; i++){
                                                if(teamPlayers[i].getArenaV3Id() == player.getArenaV3Id()){
                                                    arenaCount++;
                                                    arenaPlayerList.add(teamPlayers[i]);
                                                }
                                            }
                                            
                                            if(arenaCount != 3){
                                                sendMessage("您需要和您战队中的两个队友组队才能报名！", command.getSerial(), command.getSessionId());
                                            }else{
                                                WorldPlayer[] queuePlayes = new WorldPlayer[arenaPlayerList.size()];
                                                arenaPlayerList.copyInto(queuePlayes);
                                                
                                                switch(Server.instance.arenaSession.addArenaQueue(arenaType, player, queuePlayes, arenateam)){
                                                    case ArenaConstants.ARENA_QUEUE_DUPLICATE:
                                                        sendMessage("不要重复排队哦！", command.getSerial(), command.getSessionId());
                                                        break;
                                                    case ArenaConstants.ARENA_QUEUE_OTHER:
                                                        sendMessage("您战队的其他成员已经排过队了，不能重复排队", command.getSerial(), command.getSessionId());
                                                        break;
                                                    case ArenaConstants.ARENA_QUEUE_DUPLICATE_PLAYER:
                                                        sendMessage("您队伍中的队友已排过其他类型的竞技场，不要重复排队哦！", command.getSerial(), command.getSessionId());
                                                        break;
                                                    case ArenaConstants.ARENA_QUEUE_DUPLICATE_OTHER:
                                                        sendMessage("您已在别的类型竞技场排过队，不要重复排队哦！", command.getSerial(), command.getSessionId());
                                                        break;
                                                    case ArenaConstants.ARENA_QUEUE_ERROR:
                                                        sendMessage("排队失败,每位参赛者在同一时间只能在一个队列中。", command.getSerial(), command.getSessionId());
                                                        break;
                                                }
                                            }
                                        }
                                    }
                        		}
                            }
                        }else{
                            sendMessage("竞技场还未开放", command.getSerial(), command.getSessionId());
                        }
                    }
                        break;
                }
            }catch (Exception ex) {
                log.error("竞技场报名失败", ex);
            }
        }
    }
    
    class ArenaCancelProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception{
            try{
                int arenaType = Integer.valueOf(command.getParam(0));

                switch(arenaType){
                    case ArenaConstants.ARENA_TYPE_ONE:
                        if(Server.instance.arenaSession.arenaValid()){
                            if (player.getArenaV1Id() < 0){
                                sendMessage("您还没有创建战队", command.getSerial(), command.getSessionId());
                            }else{
                            	if (arenaService.findArenaTeamByarenaId(player.getArenaV1Id()) == null){
                        			player.setArenaV1Id(-1);
                        			sendMessage("您还没有创建战队", command.getSerial(), command.getSessionId());
                        		}else{
                        			ArenaTeam arenateam = arenaService.findArenaTeamByarenaId(player.getArenaV1Id());
                                    
                                    if(arenateam != null){
                                        switch(Server.instance.arenaSession.cancelArenaQueue(arenaType, player, arenateam)){
                                            case ArenaConstants.ARENA_CANCEL_QUEUE_BATTLE:
                                                sendMessage("竞技场战斗已开始，不能取消", command.getSerial(), command.getSessionId());
                                                break;
                                            case ArenaConstants.ARENA_CANCEL_QUEUE_ERROR:
                                                sendMessage("您还未曾排队，不能取消！", command.getSerial(), command.getSessionId());
                                                break;
                                            case ArenaConstants.ARENA_CANCEL_QUEUE_OTHER:
                                                sendMessage("您战队的其他成员已经排过队了，不能由您取消", command.getSerial(), command.getSessionId());
                                                break;
                                            case ArenaConstants.ARENA_CANCEL_QUEUE_BATTLE_OTHER:
                                                sendMessage("您刚才排的并不是1v1竞技场，不能在这取消", command.getSerial(), command.getSessionId());
                                                break;
                                        }
                                    }
                        		}
                            }
                        }else{
                            sendMessage("竞技场还未开放", command.getSerial(), command.getSessionId());
                        }
                        
                        break;
                    case ArenaConstants.ARENA_TYPE_TWO:
                        if(Server.instance.arenaSession.arenaValid()){
                            if (player.getArenaV2Id() < 0){
                                sendMessage("您还没有创建战队", command.getSerial(), command.getSessionId());
                            }else{
                            	if (arenaService.findArenaTeamByarenaId(player.getArenaV2Id()) == null){
                        			player.setArenaV2Id(-1);
                        			sendMessage("您还没有创建战队", command.getSerial(), command.getSessionId());
                        		}else{
                        			ArenaTeam arenateam = arenaService.findArenaTeamByarenaId(player.getArenaV2Id());
                                    
                                    if(arenateam != null){
                                        switch(Server.instance.arenaSession.cancelArenaQueue(arenaType, player, arenateam)){
                                            case ArenaConstants.ARENA_CANCEL_QUEUE_BATTLE:
                                                sendMessage("竞技场战斗已开始，不能取消", command.getSerial(), command.getSessionId());
                                                break;
                                            case ArenaConstants.ARENA_CANCEL_QUEUE_ERROR:
                                                sendMessage("您还未曾排队，不能取消！", command.getSerial(), command.getSessionId());
                                                break;
                                            case ArenaConstants.ARENA_CANCEL_QUEUE_OTHER:
                                                sendMessage("您战队的其他成员已经排过队了，不能由您取消", command.getSerial(), command.getSessionId());
                                                break;
                                            case ArenaConstants.ARENA_CANCEL_QUEUE_BATTLE_OTHER:
                                                sendMessage("您刚才排的并不是2v2竞技场，不能在这取消", command.getSerial(), command.getSessionId());
                                                break;
                                        }
                                    }
                        		}
                            }
                        }else{
                            sendMessage("竞技场还未开放", command.getSerial(), command.getSessionId());
                        }
                        
                        break;
                    case ArenaConstants.ARENA_TYPE_THREE:
                        if(Server.instance.arenaSession.arenaValid()){
                            if (player.getArenaV3Id() < 0){
                                sendMessage("您还没有创建战队", command.getSerial(), command.getSessionId());
                            }else{
                            	if (arenaService.findArenaTeamByarenaId(player.getArenaV3Id()) == null){
                        			player.setArenaV3Id(-1);
                        			sendMessage("您还没有创建战队", command.getSerial(), command.getSessionId());
                        		}else{
                        			ArenaTeam arenateam = arenaService.findArenaTeamByarenaId(player.getArenaV3Id());
                                    
                                    if(arenateam != null){
                                        switch(Server.instance.arenaSession.cancelArenaQueue(arenaType, player, arenateam)){
                                            case ArenaConstants.ARENA_CANCEL_QUEUE_BATTLE:
                                                sendMessage("竞技场战斗已开始，不能取消", command.getSerial(), command.getSessionId());
                                                break;
                                            case ArenaConstants.ARENA_CANCEL_QUEUE_ERROR:
                                                sendMessage("您还未曾排队，不能取消！", command.getSerial(), command.getSessionId());
                                                break;
                                            case ArenaConstants.ARENA_CANCEL_QUEUE_OTHER:
                                                sendMessage("您战队的其他成员已经排过队了，不能由您取消", command.getSerial(), command.getSessionId());
                                                break;
                                            case ArenaConstants.ARENA_CANCEL_QUEUE_BATTLE_OTHER:
                                                sendMessage("您刚才排的并不是3v3竞技场，不能在这取消", command.getSerial(), command.getSessionId());
                                                break;
                                            
                                        }
                                    }
                        		}
                            }
                        }else{
                            sendMessage("竞技场还未开放", command.getSerial(), command.getSessionId());
                        }
                        
                        break;
                }
            }catch(Exception ex){
                log.error("1v1竞技场取消排队失败", ex);
            }
        }
    }

    //leo add end
    //mengjie add arena
    class ArenaTeamCreateProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws ArenaException{
            try{
                int arenaType = Integer.valueOf(command.getParam(0));

                switch(arenaType){
                    case ArenaConstants.ARENA_TYPE_ONE: { //1v1战队创建
                    	if(player.getArenaV1Id() > 0){
                    		if (arenaService.findArenaTeamByarenaId(player.getArenaV1Id()) == null){
                    			player.setArenaV1Id(-1);
                    			byte[] bytes = stageService.getTaskBytes((short) 31001,
                                        new String[] {"是否要创建1v1战队(160000J)?\n1.是\n2.否", "请输入战队名字",
                                        "create_arenateam_return 1 "});
                                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                                  GET_FILE_OK, command.getSerial(),
                                                                  command.getSessionId());
                                seg.writeShort((short) 31001);
                                seg.writeShort((short) 2);
                                seg.write(bytes);
                                write(seg);
                    		}else{
                    			sendMessage("您已经有战队了，需要重建请先把目前的战队解散。", command.getSerial(), command.getSessionId());
                    		}
                    	}else{
                    		byte[] bytes = stageService.getTaskBytes((short) 31001,
                                    new String[] {"是否要创建1v1战队(160000J)?\n1.是\n2.否", "请输入战队名字",
                                    "create_arenateam_return 1 "});
                            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                              GET_FILE_OK, command.getSerial(),
                                                              command.getSessionId());
                            seg.writeShort((short) 31001);
                            seg.writeShort((short) 2);
                            seg.write(bytes);
                            write(seg);
                    	}
                    }
                        break;
                    case ArenaConstants.ARENA_TYPE_TWO: { //2v2战队创建
                    	if(player.getArenaV2Id() > 0){
                    		if (arenaService.findArenaTeamByarenaId(player.getArenaV2Id()) == null){
                    			player.setArenaV2Id(-1);
                    			byte[] bytes = stageService.getTaskBytes((short) 31001,
                                        new String[] {"是否要创建2v2战队(320000J)?\n1.是\n2.否", "请输入战队名字",
                                        "create_arenateam_return 2 "});
                                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                                  GET_FILE_OK, command.getSerial(),
                                                                  command.getSessionId());
                                seg.writeShort((short) 31001);
                                seg.writeShort((short) 2);
                                seg.write(bytes);
                                write(seg);
                    		}else{
                    			sendMessage("您已经有战队了，需要重建请先把目前的战队解散或离开战队。", command.getSerial(), command.getSessionId());
                    		}
                    	}else{
                    		byte[] bytes = stageService.getTaskBytes((short) 31001,
                                    new String[] {"是否要创建2v2战队(320000J)?\n1.是\n2.否", "请输入战队名字",
                                    "create_arenateam_return 2 "});
                            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                              GET_FILE_OK, command.getSerial(),
                                                              command.getSessionId());
                            seg.writeShort((short) 31001);
                            seg.writeShort((short) 2);
                            seg.write(bytes);
                            write(seg);
                    	}
                    }
                        break;
                    case ArenaConstants.ARENA_TYPE_THREE: { //3v3战队创建
                    	if(player.getArenaV3Id() > 0){
                    		if (arenaService.findArenaTeamByarenaId(player.getArenaV3Id()) == null){
                    			player.setArenaV3Id(-1);
                    			byte[] bytes = stageService.getTaskBytes((short) 31001,
                                        new String[] {"是否要创建3v3战队(480000J)?\n1.是\n2.否", "请输入战队名字",
                                        "create_arenateam_return 3 "});
                                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                                  GET_FILE_OK, command.getSerial(),
                                                                  command.getSessionId());
                                seg.writeShort((short) 31001);
                                seg.writeShort((short) 2);
                                seg.write(bytes);
                                write(seg);
                    		}else{
                    			sendMessage("您已经有战队了，需要重建请先把目前的战队解散或离开战队。", command.getSerial(), command.getSessionId());
                    		}
                    	}else{
                    		byte[] bytes = stageService.getTaskBytes((short) 31001,
                                    new String[] {"是否要创建3v3战队(480000J)?\n1.是\n2.否", "请输入战队名字",
                                    "create_arenateam_return 3 "});
                            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                              GET_FILE_OK, command.getSerial(),
                                                              command.getSessionId());
                            seg.writeShort((short) 31001);
                            seg.writeShort((short) 2);
                            seg.write(bytes);
                            write(seg);
                    	}
                    }
                        break;
                }
            }catch(Exception ex){
                log.error("1v1战队创建失败", ex);
            }
        }
    }
    
    class ArenaTeamCreateReturnProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws ArenaException{
            try{
                int arenaType = Integer.valueOf(command.getParam(0));

                switch(arenaType){
                    case ArenaConstants.ARENA_TYPE_ONE: { //1v1战队创建
                    	if(player.getArenaV1Id() > 0){
                    		sendMessage("您已经有战队了，需要重建请先把目前的战队解散。", command.getSerial(), command.getSessionId());
                    	}else{
                    		String p0 = command.getParam(1);
                            try {
                            	Changed changed = new Changed();
                            	ArenaTeam arenateam = arenaService.createArena(player, p0,arenaType,changed);
            	                if (arenateam != null){
            	                	sendMessage("创建战队成功，请详细阅读竞技场介绍后排队等候战斗。", command.getSerial(), command.getSessionId());
            	                	sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);
            	                	Utils.log(log, player.getId(), command.getAppType(),
              	                          "SubType[arena_create_1v1]ID[" + player.getArenaV1Id() + "]");
            	                }
                            } catch (ArenaException ex1) {
                                sendMessage(ex1.getMessage(), command.getSerial(),
                                            command.getSessionId());
                                return;
                            }
                    	}
                    }
                        break;
                    case ArenaConstants.ARENA_TYPE_TWO: { //2v2战队创建
                    	if(player.getArenaV2Id() > 0){
                    		sendMessage("您已经有战队了，需要重建请先把目前的战队解散或离开战队。", command.getSerial(), command.getSessionId());
                    	}else{
                    		String p0 = command.getParam(1);
                            try {
                            	Changed changed = new Changed();
                            	ArenaTeam arenateam = arenaService.createArena(player, p0,arenaType,changed);
            	                if (arenateam != null){
            	                	sendMessage("创建战队成功，请详细阅读竞技场介绍后排队等候战斗。", command.getSerial(), command.getSessionId());
            	                	sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);
            	                	Utils.log(log, player.getId(), command.getAppType(),
              	                          "SubType[arena_create_2v2]ID[" + player.getArenaV2Id() + "]");
            	                }
                            } catch (ArenaException ex1) {
                                sendMessage(ex1.getMessage(), command.getSerial(),
                                            command.getSessionId());
                                return;
                            }
                    	}
                    }
                        break;
                    case ArenaConstants.ARENA_TYPE_THREE: { //3v3战队创建
                    	if(player.getArenaV3Id() > 0){
                    		sendMessage("您已经有战队了，需要重建请先把目前的战队解散或离开战队。", command.getSerial(), command.getSessionId());
                    	}else{
                    		String p0 = command.getParam(1);
                            try {
                            	Changed changed = new Changed();
                            	ArenaTeam arenateam = arenaService.createArena(player, p0,arenaType,changed);
            	                if (arenateam != null){
            	                	sendMessage("创建战队成功，请详细阅读竞技场介绍后排队等候战斗。", command.getSerial(), command.getSessionId());
            	                	sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);
            	                	Utils.log(log, player.getId(), command.getAppType(),
              	                          "SubType[arena_create_3v3]ID[" + player.getArenaV3Id() + "]");
            	                }
                            } catch (ArenaException ex1) {
                                sendMessage(ex1.getMessage(), command.getSerial(),
                                            command.getSessionId());
                                return;
                            }
                    	}
                    }
                        break;
                }
            }catch(Exception ex){
                log.error("1v1战队创建失败", ex);
            }
        }
    }
    class ArenaTeamViewProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws ArenaException{
            try{
                int arenaType = Integer.valueOf(command.getParam(0));

                switch(arenaType){
                    case ArenaConstants.ARENA_TYPE_ONE: { //1v1战队查看
                    	if(player.getArenaV1Id() > 0){
                    		ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                    		if (arenateam!=null){
                    			sendMessage("["+arenateam.getArenaname()+"]战队成员:" + player.getPlayerName() + "(个人竞技场等级:" + player.getArenaLevel() + ")", command.getSerial(), command.getSessionId());
                    		}else{
                    			sendMessage("您还没有战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                    		}
                    	}else{
                    		sendMessage("您还没有战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                    	}
                    }
                        break;
                    case ArenaConstants.ARENA_TYPE_TWO: { //2v2战队查看
                    	if(player.getArenaV2Id() > 0){
                    		ArenaTeam arenateam = arenaService.findArenaTeamByarenaId(player.getArenaV2Id());
                    		if (arenateam!=null){
                    			//取N v N成员
                    			ArrayList arenalist = arenaService.findArenaTeamPlayer(arenateam.getId(),arenaType,arenateam.getOwner(),arenateam.getArenalevel());
                    			String tmp_msg = "["+arenateam.getArenaname()+"]战队成员:(按确定键翻页)\n";
								if ((arenalist!=null) && (arenalist.size()>0)){
									ArenaTeam2Player arenaTeam2Player = new ArenaTeam2Player();
									for(int i = 0; i < arenalist.size(); i++){
	                    				arenaTeam2Player = new ArenaTeam2Player();
	                    				arenaTeam2Player = (ArenaTeam2Player) arenalist.get(i);
	                    				if (arenaTeam2Player.isIsowner()){
	                    					tmp_msg = tmp_msg+arenaTeam2Player.getPlayername()+"(个人竞技场等级:" + arenaTeam2Player.getPlayerarenaLevel() + ")【队长】\n";
	                    				}else{
	                    					tmp_msg = tmp_msg+arenaTeam2Player.getPlayername()+"(个人竞技场等级:" + arenaTeam2Player.getPlayerarenaLevel() + ")\n";
	                    				}
	                    				
	                    			}
								}else{
									tmp_msg = "暂无信息";
								}
                    			sendMessage(tmp_msg, command.getSerial(), command.getSessionId());
                    		}else{
                    			sendMessage("您还没有战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                    		}
                    	}else{
                    		sendMessage("您还没有战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                    	}
                    }
                        break;
                    case ArenaConstants.ARENA_TYPE_THREE: { //3v3战队查看
                    	if(player.getArenaV3Id() > 0){
                    		ArenaTeam arenateam = arenaService.findArenaTeamByarenaId(player.getArenaV3Id());
                    		if (arenateam!=null){
                    			//取N v N成员
                    			ArrayList arenalist = arenaService.findArenaTeamPlayer(arenateam.getId(),arenaType,arenateam.getOwner(),arenateam.getArenalevel());
                    			String tmp_msg = "["+arenateam.getArenaname()+"]战队成员:(按确定键翻页)\n";
								if ((arenalist!=null) && (arenalist.size()>0)){
									ArenaTeam2Player arenaTeam2Player = new ArenaTeam2Player();
									for(int i = 0; i < arenalist.size(); i++){
	                    				arenaTeam2Player = new ArenaTeam2Player();
	                    				arenaTeam2Player = (ArenaTeam2Player) arenalist.get(i);
	                    				if (arenaTeam2Player.isIsowner()){
	                    					tmp_msg = tmp_msg + "["+arenaTeam2Player.getPlayername()+"]队长(个人竞技场等级:" + arenaTeam2Player.getPlayerarenaLevel() + ")\n";
	                    				}else{
	                    					tmp_msg = tmp_msg + "["+arenaTeam2Player.getPlayername()+"](个人竞技场等级:" + arenaTeam2Player.getPlayerarenaLevel() + ")\n";
	                    				}
	                    				
	                    			}
								}else{
									tmp_msg = "暂无信息";
								}
                    			sendMessage(tmp_msg, command.getSerial(), command.getSessionId());
                    		}else{
                    			sendMessage("您还没有战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                    		}
                    	}else{
                    		sendMessage("您还没有战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                    	}
                    }
                        break;
                }
            }catch(Exception ex){
                log.error("战队查看失败", ex);
            }
        }
    }
    
    class ArenaTeamDissolveProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws ArenaException{
            try{
                int arenaType = Integer.valueOf(command.getParam(0));
                switch(arenaType){
                    case ArenaConstants.ARENA_TYPE_ONE: { //1v1战队解散
                    	if(player.getArenaV1Id() > 0){
                    		ArenaBattleClient arenaBattle = Server.instance.arenaSession.getArenaBattle(player);
                            if(arenaBattle != null){
                            	sendMessage("您正在排队中，请先取消排队再解散战队。", command.getSerial(), command.getSessionId());
                            }else{
                            	ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                        		if (arenateam!=null){
                        			byte[] bytes = stageService.getTaskBytes((short) 31002,
                                            new String[] {"不属于任何战队将不能参加比赛且个人竞技场等级将会被重置,您真的要解散1v1战队["+arenateam.getArenaname()+"]吗?\n1.是\n2.否",
                                            "dissolve_arenateam_return " + arenaType + " " + arenateam.getId()});
                                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                                            GET_FILE_OK, command.getSerial(),
                                            command.getSessionId());
                                    seg.writeShort((short) 31002);
                                    seg.writeShort((short) 2);
                                    seg.write(bytes);
                                    write(seg);
                        		}else{
                        			sendMessage("您还没有战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                        		}
                            }
                    	}else{
                    		sendMessage("您还没有战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                    	}
                    }
                        break;
                    case ArenaConstants.ARENA_TYPE_TWO: { //2v2战队解散,踢人，离队
                    	if(player.getArenaV2Id() > 0){
                    		int type = Integer.valueOf(command.getParam(1));
                        	if(type == 1){//4.解散2v2战队(队长操作)
                        		ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                        		if (arenateam!=null){
                        			ArenaBattleClient arenaBattle = Server.instance.arenaSession.getArenaBattleByowner(player);
                                    if(arenaBattle != null){
                                    	sendMessage("您的战队正在排队中，请先取消排队再解散战队。", command.getSerial(), command.getSessionId());
                                    }else{
                            			byte[] bytes = stageService.getTaskBytes((short) 31002,
                                                new String[] {"不属于任何战队将不能参加比赛且个人竞技场等级将会被重置,您真的要解散2v2战队["+arenateam.getArenaname()+"]吗?\n1.是\n2.否",
                                                "dissolve_arenateam_return " + arenaType + " " + arenateam.getId()});
                                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                GET_FILE_OK, command.getSerial(),
                                                command.getSessionId());
                                        seg.writeShort((short) 31002);
                                        seg.writeShort((short) 2);
                                        seg.write(bytes);
                                        write(seg);
                                		
                                    }
                        		}else{
                        			sendMessage("只有战队的队长才能解散战队。", command.getSerial(), command.getSessionId());
                        		}
                        	}else if (type == 2){//5.踢出队员(队长操作)
                        		ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                        		if (arenateam!=null){
                        			ArenaBattleClient arenaBattle = Server.instance.arenaSession.getArenaBattleByowner(player);
                                    if(arenaBattle != null){
                                    	sendMessage("您的战队正在排队中，请先取消排队再对战队成员进行操作。", command.getSerial(), command.getSessionId());
                                    }else{
                                    	int times = Integer.valueOf(command.getParam(2));
                                    	if (times == 1){//第一次操作时
                                    		String tmp_msg = "每一位战队成员都应为战队的成长做出贡献，如果您对某位战队成员不满意，可以将他请出战队。" +
                                    				"\n请输入要踢出战队的队员所在的序号";
                                    		//取N v N成员
                                			ArrayList arenalist = arenaService.findArenaTeamPlayer(arenateam.getId(),arenaType,arenateam.getOwner(),arenateam.getArenalevel());
            								if ((arenalist!=null) && (arenalist.size()>0)){
            									ArenaTeam2Player arenaTeam2Player = new ArenaTeam2Player();
            									int count_tmp = 0;
            									String [] questions = new String[arenalist.size() - 1 + 3];
            									for(int i = 0; i < arenalist.size(); i++){
            	                    				arenaTeam2Player = new ArenaTeam2Player();
            	                    				arenaTeam2Player = (ArenaTeam2Player) arenalist.get(i);
            	                    				
            	                    				if (!arenaTeam2Player.isIsowner()){
            	                    					count_tmp++;
            	                    					tmp_msg = tmp_msg + "\n"+count_tmp+"."+arenaTeam2Player.getPlayername()+"(个人竞技场等级:" + arenaTeam2Player.getPlayerarenaLevel() + ")";
            	                    					questions[2+count_tmp] = "dissolve_arenateam 2 2 2 " + arenaTeam2Player.getPlayerId();
            	                    				}
            	                    				
            	                    			}
            									if(count_tmp == 0){
            										tmp_msg = "您的战队只有您一个人哦，赶紧去招募队员吧。";
                									sendMessage(tmp_msg, command.getSerial(), command.getSessionId());
            									}else{
            										count_tmp++;
            										tmp_msg = tmp_msg + "\n"+count_tmp+".我再想想。";
                            	        			questions[0] = String.valueOf(count_tmp);
                                    				questions[1] = "1";
                            	        			questions[2] = tmp_msg;
                            	        			byte[] bytes = stageService.getTaskBytes((short) 31010, questions);
                            	        			UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
                            	        					command.getSerial(),
                            	        					command.getSessionId());
                            						      seg.writeShort((short) 31010);
                            						      seg.writeShort((short) 2);
                            						      seg.write(bytes);
                            						      write(seg);
            									}
            								}else{
            									tmp_msg = "暂时无法操作，请稍后再试。";
            									sendMessage(tmp_msg, command.getSerial(), command.getSessionId());
            								}
                                    	}else if (times == 2){
                                    		//确认踢出某队员
                                    		int playerId = Integer.valueOf(command.getParam(3));
                                    		WorldPlayer playertmp = playerService.getWorldPlayer(playerId);
                                    		if (playertmp != null){
                                    			if ((playertmp.getArenaV1Id() == -1) &&(playertmp.getArenaV3Id() == -1)){
                                    				playertmp.setArenaLevel(0);
	                    						}
                                    			playertmp.setArenaV2Id(-1);
                    							sendMessage(playertmp.getId(),"您已被2v2战队【"+arenateam.getArenaname()+"】的队长踢出战队。");
                                    		}else{
                                    			arenaService.killArenaTeamPlayer(arenateam.getId(),arenaType,playerId);
                                    		}
                                    		//内存中删除
                                    		arenaService.killArenaTeamPlayercatch(arenateam.getId(),arenaType,arenateam.getOwner(),arenateam.getArenalevel(),playerId);
                                    		sendMessage("已将指定队员踢出战队。", command.getSerial(), command.getSessionId());
                                    		Utils.log(log, player.getId(), command.getAppType(),
                                                    "Dissolve playerID[" + playerId + "]ArenaTeamID[" +
                                                    arenateam.getId() + "] ArenaTeamDissolve-2");
                                    	}
                                    }
                        		}else{
                        			sendMessage("只有战队的队长才能踢出战队成员。", command.getSerial(), command.getSessionId());
                        		}
                        	}else if (type == 3){//6.离开2v2战队(队员操作)
                        		int times = Integer.valueOf(command.getParam(2));
                            	if (times == 1){//第一次操作时
                            		ArenaBattleClient arenaBattle = Server.instance.arenaSession.getArenaBattle(player);
                                    if(arenaBattle != null){
                                    	sendMessage("您正在排队中，请先取消排队再离开战队。", command.getSerial(), command.getSessionId());
                                    }else{
                                    	ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                                		if (arenateam!=null){
                                			sendMessage("队长不能离开战队，如果想解散战队请选择“解散2v2战队”。", command.getSerial(), command.getSessionId());
                                		}else{
                                			byte[] bytes = stageService.getTaskBytes((short) 31002,
                                                    new String[] {"您真的要离开2v2战队吗?\n1.是\n2.否",
                                                    "dissolve_arenateam 2 3 2"});
                                            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                    GET_FILE_OK, command.getSerial(),
                                                    command.getSessionId());
                                            seg.writeShort((short) 31002);
                                            seg.writeShort((short) 2);
                                            seg.write(bytes);
                                            write(seg);
                                		}
                                    }
                            	}else if (times == 2){
                            		if ((player.getArenaV1Id() == -1) &&(player.getArenaV3Id() == -1)){
                            			player.setArenaLevel(0);
            						}
                            		//内存中删除
                            		ArenaTeam arenateam = arenaService.findArenaTeamByarenaId(player.getArenaV2Id());
                            		arenaService.killArenaTeamPlayercatch(arenateam.getId(),arenaType,arenateam.getOwner(),arenateam.getArenalevel(),player.getId());
                            		player.setArenaV2Id(-1);
                            		sendMessage(arenateam.getOwner(),"您2v2战队队员“"+player.getPlayerName()+"”离开了战队。");
        							sendMessage("您已经离开2v2战队。",command.getSerial(), command.getSessionId());
        							Utils.log(log, player.getId(), command.getAppType(),
                                            "Dissolve playerID[" + player.getId() + "]ArenaTeamID[" +
                                            arenateam.getId() + "] ArenaTeamDissolve-owner-2");
                            	}
                        	}
                    	}else{
                    		sendMessage("您还没有2v2战队呢，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                    	}
                    }
                    break;
                    case ArenaConstants.ARENA_TYPE_THREE: { //3v3战队解散,踢人，离队
                    	if(player.getArenaV3Id() > 0){
                    		int type = Integer.valueOf(command.getParam(1));
                        	if(type == 1){//4.解散3v3战队(队长操作)
                        		ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                        		if (arenateam!=null){
                        			ArenaBattleClient arenaBattle = Server.instance.arenaSession.getArenaBattleByowner(player);
                                    if(arenaBattle != null){
                                    	sendMessage("您的战队正在排队中，请先取消排队再解散战队。", command.getSerial(), command.getSessionId());
                                    }else{
                            			byte[] bytes = stageService.getTaskBytes((short) 31002,
                                                new String[] {"不属于任何战队将不能参加比赛且个人竞技场等级将会被重置,您真的要解散3v3战队["+arenateam.getArenaname()+"]吗?\n1.是\n2.否",
                                                "dissolve_arenateam_return " + arenaType + " " + arenateam.getId()});
                                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                GET_FILE_OK, command.getSerial(),
                                                command.getSessionId());
                                        seg.writeShort((short) 31002);
                                        seg.writeShort((short) 2);
                                        seg.write(bytes);
                                        write(seg);
                                		
                                    }
                        		}else{
                        			sendMessage("只有战队的队长才能解散战队。", command.getSerial(), command.getSessionId());
                        		}
                        	}else if (type == 2){//5.踢出队员(队长操作)
                        		ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                        		if (arenateam!=null){
                        			ArenaBattleClient arenaBattle = Server.instance.arenaSession.getArenaBattleByowner(player);
                                    if(arenaBattle != null){
                                    	sendMessage("您的战队正在排队中，请先取消排队再对战队成员进行操作。", command.getSerial(), command.getSessionId());
                                    }else{
                                    	int times = Integer.valueOf(command.getParam(2));
                                    	if (times == 1){//第一次操作时
                                    		String tmp_msg = "每一位战队成员都应为战队的成长做出贡献，如果您对某位战队成员不满意，可以将他请出战队。" +
                                    				"\n请输入要踢出战队的队员所在的序号";
                                    		//取N v N成员
                                			ArrayList arenalist = arenaService.findArenaTeamPlayer(arenateam.getId(),arenaType,arenateam.getOwner(),arenateam.getArenalevel());
            								if ((arenalist!=null) && (arenalist.size()>0)){
            									ArenaTeam2Player arenaTeam2Player = new ArenaTeam2Player();
            									int count_tmp = 0;
            									String [] questions = new String[arenalist.size() - 1 + 3];
            									for(int i = 0; i < arenalist.size(); i++){
            	                    				arenaTeam2Player = new ArenaTeam2Player();
            	                    				arenaTeam2Player = (ArenaTeam2Player) arenalist.get(i);
            	                    				
            	                    				if (!arenaTeam2Player.isIsowner()){
            	                    					count_tmp++;
            	                    					tmp_msg = tmp_msg + "\n"+count_tmp+"."+arenaTeam2Player.getPlayername()+"(个人竞技场等级:" + arenaTeam2Player.getPlayerarenaLevel() + ")";
            	                    					questions[2+count_tmp] = "dissolve_arenateam 3 2 2 " + arenaTeam2Player.getPlayerId();
            	                    				}
            	                    				
            	                    			}
            									if(count_tmp == 0){
            										tmp_msg = "您的战队只有您一个人哦，赶紧去招募队员吧。";
                									sendMessage(tmp_msg, command.getSerial(), command.getSessionId());
            									}else{
            										count_tmp++;
            										tmp_msg = tmp_msg + "\n"+count_tmp+".我再想想。";
                            	        			questions[0] = String.valueOf(count_tmp);
                                    				questions[1] = "1";
                            	        			questions[2] = tmp_msg;
                            	        			byte[] bytes = stageService.getTaskBytes((short) 31010, questions);
                            	        			UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
                            	        					command.getSerial(),
                            	        					command.getSessionId());
                            						      seg.writeShort((short) 31010);
                            						      seg.writeShort((short) 2);
                            						      seg.write(bytes);
                            						      write(seg);
            									}
            								}else{
            									tmp_msg = "暂时无法操作，请稍后再试。";
            									sendMessage(tmp_msg, command.getSerial(), command.getSessionId());
            								}
                                    	}else if (times == 2){
                                    		//确认踢出某队员
                                    		int playerId = Integer.valueOf(command.getParam(3));
                                    		WorldPlayer playertmp = playerService.getWorldPlayer(playerId);
                                    		if (playertmp != null){
                                    			if ((playertmp.getArenaV1Id() == -1) &&(playertmp.getArenaV2Id() == -1)){
                                    				playertmp.setArenaLevel(0);
	                    						}
                                    			playertmp.setArenaV3Id(-1);
                    							sendMessage(playertmp.getId(),"您已被3v3战队【"+arenateam.getArenaname()+"】的队长踢出战队。");
                                    		}else{
                                    			arenaService.killArenaTeamPlayer(arenateam.getId(),arenaType,playerId);
                                    		}
                                    		arenaService.killArenaTeamPlayercatch(arenateam.getId(),arenaType,arenateam.getOwner(),arenateam.getArenalevel(),playerId);
                                    		sendMessage("已将指定队员踢出战队。", command.getSerial(), command.getSessionId());
                                    		Utils.log(log, player.getId(), command.getAppType(),
                                                    "Dissolve playerID[" + playertmp.getId() + "]ArenaTeamID[" +
                                                    arenateam.getId() + "] ArenaTeamDissolve-3");
                                    	}
                                    }
                        		}else{
                        			sendMessage("只有战队的队长才能踢出战队成员。", command.getSerial(), command.getSessionId());
                        		}
                        	}else if (type == 3){//6.离开3v3战队(队员操作)
                        		int times = Integer.valueOf(command.getParam(2));
                            	if (times == 1){//第一次操作时
                            		ArenaBattleClient arenaBattle = Server.instance.arenaSession.getArenaBattle(player);
                                    if(arenaBattle != null){
                                    	sendMessage("您正在排队中，请先取消排队再离开战队。", command.getSerial(), command.getSessionId());
                                    }else{
                                    	ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                                		if (arenateam!=null){
                                			sendMessage("队长不能离开战队，如果想解散战队请选择“解散3v3战队”。", command.getSerial(), command.getSessionId());
                                		}else{
                                			byte[] bytes = stageService.getTaskBytes((short) 31002,
                                                    new String[] {"您真的要离开3v3战队吗?\n1.是\n2.否",
                                                    "dissolve_arenateam 3 3 2"});
                                            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                    GET_FILE_OK, command.getSerial(),
                                                    command.getSessionId());
                                            seg.writeShort((short) 31002);
                                            seg.writeShort((short) 2);
                                            seg.write(bytes);
                                            write(seg);
                                		}
                                    }
                            	}else if (times == 2){
                            		if ((player.getArenaV1Id() == -1) &&(player.getArenaV2Id() == -1)){
                            			player.setArenaLevel(0);
            						}
                            		//内存中删除
                            		ArenaTeam arenateam = arenaService.findArenaTeamByarenaId(player.getArenaV3Id());
                            		arenaService.killArenaTeamPlayercatch(arenateam.getId(),arenaType,arenateam.getOwner(),arenateam.getArenalevel(),player.getId());
                            		player.setArenaV3Id(-1);
                            		sendMessage(arenateam.getOwner(),"您3v3战队队员“"+player.getPlayerName()+"”离开了战队。");
        							sendMessage("您已经离开3v3战队。",command.getSerial(), command.getSessionId());
        							Utils.log(log, player.getId(), command.getAppType(),
                                            "Dissolve playerID[" + player.getId() + "]ArenaTeamID[" +
                                            arenateam.getId() + "] ArenaTeamDissolve-owner-3");
                            	}
                        	}
                    	}else{
                    		sendMessage("您还没有3v3战队呢，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                    	}
                    }
                    break;
                }
            }catch(Exception ex){
                log.error("战队创建失败", ex);
            }
        }
    }
    
    class ArenaTeamDissolveReturnProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws ArenaException{
            try{
                int arenaType = Integer.valueOf(command.getParam(0));
                switch(arenaType){
                    case ArenaConstants.ARENA_TYPE_ONE: { //1v1战队解散
                    	if(player.getArenaV1Id() > 0){
                    		ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                    		if (arenateam!=null){
                    			int arenaId = Integer.valueOf(command.getParam(1));
                    			if (arenaId == arenateam.getId()){
                    				arenaService.quit(arenateam,player,arenaType);
                    				sendMessage("解散战队成功", command.getSerial(), command.getSessionId());
                    			}else{
                    				sendMessage("解散1v1战队失败。", command.getSerial(), command.getSessionId());
                    			}
                    		}else{
                    			sendMessage("您还没有战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                    		}
                    	}else{
                    		sendMessage("您还没有战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                    	}
                    }
                    break;
                    case ArenaConstants.ARENA_TYPE_TWO: { //2v2战队解散
                    	if(player.getArenaV2Id() > 0){
                    		ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                    		if (arenateam!=null){
                    			int arenaId = Integer.valueOf(command.getParam(1));
                    			if (arenaId == arenateam.getId()){
                    				//排查队员
                    				ArrayList arenalist = arenaService.findArenaTeamPlayer(arenateam.getId(),arenaType,arenateam.getOwner(),arenateam.getArenalevel());
    								if ((arenalist!=null) && (arenalist.size()>0)){
    									ArenaTeam2Player arenaTeam2Player = new ArenaTeam2Player();
    									for(int i = 0; i < arenalist.size(); i++){
    	                    				arenaTeam2Player = new ArenaTeam2Player();
    	                    				arenaTeam2Player = (ArenaTeam2Player) arenalist.get(i);
    	                    				
    	                    				if (!arenaTeam2Player.isIsowner()){
    	                    					WorldPlayer p2 = playerService.getWorldPlayer(arenaTeam2Player.getPlayerId());
    	                    					if (p2 != null){
    	                    						if ((p2.getArenaV1Id() == -1) &&(p2.getArenaV3Id() == -1)){
    	                    							p2.setArenaLevel(0);
    	                    						}
    	                    						p2.setArenaV2Id(-1);
	                    							sendMessage(p2.getId(),"您的2v2战队【"+arenateam.getArenaname()+"】已被队长解散。");
    	                    					}else{
    	                    						arenaService.killArenaTeamPlayer(arenateam.getId(),arenaType,arenaTeam2Player.getPlayerId());
    	                    					}
    	                    				}
    	                    			}
    								}
                    				arenaService.quit(arenateam,player,arenaType);
                    				sendMessage("解散战队成功", command.getSerial(), command.getSessionId());
                    			}else{
                    				sendMessage("解散2v2战队失败。", command.getSerial(), command.getSessionId());
                    			}
                    		}else{
                    			sendMessage("您还没有战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                    		}
                    	}else{
                    		sendMessage("您还没有战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                    	}
                    }
                    break;
                    case ArenaConstants.ARENA_TYPE_THREE: { //3v3战队解散
                    	if(player.getArenaV3Id() > 0){
                    		ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                    		if (arenateam!=null){
                    			int arenaId = Integer.valueOf(command.getParam(1));
                    			if (arenaId == arenateam.getId()){
                    				//排查队员
                    				ArrayList arenalist = arenaService.findArenaTeamPlayer(arenateam.getId(),arenaType,arenateam.getOwner(),arenateam.getArenalevel());
    								if ((arenalist!=null) && (arenalist.size()>0)){
    									ArenaTeam2Player arenaTeam2Player = new ArenaTeam2Player();
    									for(int i = 0; i < arenalist.size(); i++){
    	                    				arenaTeam2Player = new ArenaTeam2Player();
    	                    				arenaTeam2Player = (ArenaTeam2Player) arenalist.get(i);
    	                    				
    	                    				if (!arenaTeam2Player.isIsowner()){
    	                    					WorldPlayer p2 = playerService.getWorldPlayer(arenaTeam2Player.getPlayerId());
    	                    					if (p2 != null){
    	                    						if ((p2.getArenaV1Id() == -1) &&(p2.getArenaV2Id() == -1)){
    	                    							p2.setArenaLevel(0);
    	                    						}
    	                    						p2.setArenaV3Id(-1);
	                    							sendMessage(p2.getId(),"您的3v3战队【"+arenateam.getArenaname()+"】已被队长解散。");
    	                    					}else{
    	                    						arenaService.killArenaTeamPlayer(arenateam.getId(),arenaType,arenaTeam2Player.getPlayerId());
    	                    					}
    	                    				}
    	                    			}
    								}
                    				arenaService.quit(arenateam,player,arenaType);
                    				sendMessage("解散战队成功", command.getSerial(), command.getSessionId());
                    			}else{
                    				sendMessage("解散3v3战队失败。", command.getSerial(), command.getSessionId());
                    			}
                    		}else{
                    			sendMessage("您还没有战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                    		}
                    	}else{
                    		sendMessage("您还没有战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                    	}
                    }
                        break;
                }
            }catch(Exception ex){
                log.error("战队解散失败", ex);
            }
        }
    }
    class ArenaTeamViewInfoProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws ArenaException{
            try{
                int arenaType = Integer.valueOf(command.getParam(0));

                switch(arenaType){
                	case 0: { //其他查看
                		int arenainfoType = Integer.valueOf(command.getParam(1));
                		switch(arenainfoType){
	            			case 1: { //1、	查看个人竞技点数
	            				sendMessage("["+player.getPlayerName()+"]个人竞技点数：" + player.getArenaPoint(), command.getSerial(), command.getSessionId());	
	            			}
	            			break;
	            			case 2: { //2、	查看个人等级
	            				sendMessage("["+player.getPlayerName()+"]个人竞技场等级：" + player.getArenaLevel(), command.getSerial(), command.getSessionId());	
	            			}
	            			break;
                		}
                	}
                	break;
                    case ArenaConstants.ARENA_TYPE_ONE: { //1v1战队查看
                    	int arenainfoType = Integer.valueOf(command.getParam(1));
                    	if(player.getArenaV1Id() > 0){
                    		switch(arenainfoType){
                    			case 3: { //3、	查看战队等级
                    				ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                            		if (arenateam!=null){
                            			sendMessage("["+arenateam.getArenaname()+"]1v1战队等级：" + arenateam.getArenalevel(), command.getSerial(), command.getSessionId());
                            		}else{
                            			sendMessage("您还没有1v1战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                            		}
                    			}
                    			break;
                    		}
                    	}else{
                    		sendMessage("您还没有1v1战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                    	}
                    }
                    break;
                    case ArenaConstants.ARENA_TYPE_TWO: { //2v2战队查看
                    	int arenainfoType = Integer.valueOf(command.getParam(1));
                    	if(player.getArenaV2Id() > 0){
                    		switch(arenainfoType){
                    			case 3: { //3、	查看战队等级
                    				ArenaTeam arenateam = arenaService.findArenaTeamByarenaId(player.getArenaV2Id());
                            		if (arenateam!=null){
                            			sendMessage("["+arenateam.getArenaname()+"]2v2战队等级：" + arenateam.getArenalevel(), command.getSerial(), command.getSessionId());
//                            			
                            		}else{
                            			sendMessage("您还没有2v2战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                            		}
                    			}
                    			break;
                    		}
                    		
                    	}else{
                    		sendMessage("您还没有2v2战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                    	}
                    }
                        break;
                    case ArenaConstants.ARENA_TYPE_THREE: { //3v3战队查看
                    	int arenainfoType = Integer.valueOf(command.getParam(1));
                    	if(player.getArenaV3Id() > 0){
                    		switch(arenainfoType){
                    			case 3: { //3、	查看战队等级
                    				ArenaTeam arenateam = arenaService.findArenaTeamByarenaId(player.getArenaV3Id());
                            		if (arenateam!=null){
                            			sendMessage("["+arenateam.getArenaname()+"]3v3战队等级：" + arenateam.getArenalevel(), command.getSerial(), command.getSessionId());
//                            			
                            		}else{
                            			sendMessage("您还没有3v3战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                            		}
                    			}
                    			break;
                    		}
                    		
                    	}else{
                    		sendMessage("您还没有3v3战队，先建个超炫的战队吧。", command.getSerial(), command.getSessionId());
                    	}
                    }
                        break;
                }
            }catch(Exception ex){
                log.error("战队查看异常", ex);
            }
        }
    }
    class ArenaTeamViewTopProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws ArenaException{
            try{
            	int type = Integer.valueOf(command.getParam(0));
            	if (type == 1){
            		//本服排行
            		int arenaType = Integer.valueOf(command.getParam(1));
            		switch(arenaType){
            			case ArenaConstants.ARENA_TYPE_ONE: { //1v1排行榜查看
            				ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                			List<String> list = topListService.playerTopList.getArenaTopListLevel(arenateam,player, 10,arenaType);

                            if (list.size() > 0) {
                                UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                                seg.writeShort((short) 9021);
                                seg.writeString("1v1战队等级排名");
                                seg.write((byte) 0);
                                seg.writeShort((short) list.size());

                                for (int i = 0; i < list.size(); i++) {
                                    seg.writeInt(i);
                                    seg.writeString(list.get(i));
                                    seg.writeInt(Utils.CLR_WHITE);
                                }

                                connectService.writeTo(seg, player.getId());
                            } else {
                                sendMessage("暂时没有1v1战队排名，请稍候再试。", command.getSerial(), command.getSessionId());
                            }
            			}
            			break;
            			case ArenaConstants.ARENA_TYPE_TWO: { //2v2排行榜查看
            				ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                			List<String> list = topListService.playerTopList.getArenaTopListLevel(arenateam,player, 10,arenaType);

                            if (list.size() > 0) {
                                UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                                seg.writeShort((short) 9021);
                                seg.writeString("2v2战队等级排名");
                                seg.write((byte) 0);
                                seg.writeShort((short) list.size());

                                for (int i = 0; i < list.size(); i++) {
                                    seg.writeInt(i);
                                    seg.writeString(list.get(i));
                                    seg.writeInt(Utils.CLR_WHITE);
                                }

                                connectService.writeTo(seg, player.getId());
                            } else {
                                sendMessage("暂时没有2v2战队排名，请稍候再试。", command.getSerial(), command.getSessionId());
                            }
            			}
            			break;
						case ArenaConstants.ARENA_TYPE_THREE: { //3v3排行榜查看
							ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                			List<String> list = topListService.playerTopList.getArenaTopListLevel(arenateam,player, 10,arenaType);

                            if (list.size() > 0) {
                                UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                                seg.writeShort((short) 9021);
                                seg.writeString("3v3战队等级排名");
                                seg.write((byte) 0);
                                seg.writeShort((short) list.size());

                                for (int i = 0; i < list.size(); i++) {
                                    seg.writeInt(i);
                                    seg.writeString(list.get(i));
                                    seg.writeInt(Utils.CLR_WHITE);
                                }

                                connectService.writeTo(seg, player.getId());
                            } else {
                                sendMessage("暂时没有3v3战队排名，请稍候再试。", command.getSerial(), command.getSessionId());
                            }
						}
						break;
            		}
            	}else if (type == 2){
            		//个人竞技场等级排行
            		List<String> list = topListService.playerTopList.getPlayerarenaLevelTopList(player, 10,1);

                    if (list.size() > 0) {
                        UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                        seg.writeShort((short) 9021);
                        seg.writeString("个人竞技场等级排名");
                        seg.write((byte) 0);
                        seg.writeShort((short) list.size());

                        for (int i = 0; i < list.size(); i++) {
                            seg.writeInt(i);
                            seg.writeString(list.get(i));
                            seg.writeInt(Utils.CLR_WHITE);
                        }

                        connectService.writeTo(seg, player.getId());
                    } else {
                        sendMessage("个人竞技场等级暂时无法查询，请稍候再试。", command.getSerial(), command.getSessionId());
                    }
            	}else if (type == 3){
            		//全服排行
            		int arenaType = Integer.valueOf(command.getParam(1));
            		switch(arenaType){
            			case ArenaConstants.ARENA_TYPE_ONE: { //1v1排行榜查看
            				List<String> list = topListService.playerTopList.getAllServerArenaLevelTopList(player, 10,arenaType);
                            if ((list != null ) && (list.size() > 0)) {
                                UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                                seg.writeShort((short) 9021);
                                seg.writeString("全服1v1战队等级排名");
                                seg.write((byte) 0);
                                seg.writeShort((short) list.size());

                                for (int i = 0; i < list.size(); i++) {
                                    seg.writeInt(i);
                                    seg.writeString(list.get(i));
                                    seg.writeInt(Utils.CLR_WHITE);
                                }

                                connectService.writeTo(seg, player.getId());
                            } else {
                                sendMessage("竞技场暂时无排名数据，请参加竞技场战斗后再试。", command.getSerial(), command.getSessionId());
                            }
            			}
            			break;
            			case ArenaConstants.ARENA_TYPE_TWO: { //2v2排行榜查看
            				List<String> list = topListService.playerTopList.getAllServerArenaLevelTopList(player, 10,arenaType);
                            if ((list != null ) && (list.size() > 0)) {
                                UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                                seg.writeShort((short) 9021);
                                seg.writeString("全服2v2战队等级排名");
                                seg.write((byte) 0);
                                seg.writeShort((short) list.size());

                                for (int i = 0; i < list.size(); i++) {
                                    seg.writeInt(i);
                                    seg.writeString(list.get(i));
                                    seg.writeInt(Utils.CLR_WHITE);
                                }

                                connectService.writeTo(seg, player.getId());
                            } else {
                                sendMessage("竞技场暂时无排名数据，请参加竞技场战斗后再试。", command.getSerial(), command.getSessionId());
                            }
            			}
            			break;
						case ArenaConstants.ARENA_TYPE_THREE: { //3v3排行榜查看
							List<String> list = topListService.playerTopList.getAllServerArenaLevelTopList(player, 10,arenaType);
                            if ((list != null ) && (list.size() > 0)) {
                                UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                                seg.writeShort((short) 9021);
                                seg.writeString("全服3v3战队等级排名");
                                seg.write((byte) 0);
                                seg.writeShort((short) list.size());

                                for (int i = 0; i < list.size(); i++) {
                                    seg.writeInt(i);
                                    seg.writeString(list.get(i));
                                    seg.writeInt(Utils.CLR_WHITE);
                                }

                                connectService.writeTo(seg, player.getId());
                            } else {
                                sendMessage("竞技场暂时无排名数据，请参加竞技场战斗后再试。", command.getSerial(), command.getSessionId());
                            }
						}
            		}
            	}else if (type == 4){//跨服PK大奖赛
            		List<String> list = topListService.playerTopList.getAllServerArenaLevelTopListWorldWar(player, 10,1);
                    if ((list != null ) && (list.size() > 0)) {
                        UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                        seg.writeShort((short) 9021);
                        seg.writeString("跨服PK大奖赛最终排名(第一季)");
                        seg.write((byte) 0);
                        seg.writeShort((short) list.size());

                        for (int i = 0; i < list.size(); i++) {
                            seg.writeInt(i);
                            seg.writeString(list.get(i));
                            seg.writeInt(Utils.CLR_WHITE);
                        }

                        connectService.writeTo(seg, player.getId());
                    } else {
                        sendMessage("竞技场大奖赛暂时无排名数据，请稍候再试。", command.getSerial(), command.getSessionId());
                    }
            	}
            }catch(Exception ex){
                log.error("排行榜读取失败", ex);
            }
        }
    }
    class ArenaTeamJoinProcessor implements CommandProcessor{
    	public void process(WorldPlayer player, Command command) throws Exception {
    		try{
                int arenaType = Integer.valueOf(command.getParam(0));
                int inputType = Integer.valueOf(command.getParam(1));
                switch(arenaType){
                case ArenaConstants.ARENA_TYPE_ONE: { //1v1
                	
                	}
                	break;
                case ArenaConstants.ARENA_TYPE_TWO: { //2v2
                	
                	if (inputType == 1){//input
                		if(player.getArenaV2Id() > 0){
                    		ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                    		if (arenateam!=null){
                    			ArrayList arenalist = arenaService.findArenaTeamPlayer(arenateam.getId(),arenaType,arenateam.getOwner(),arenateam.getArenalevel());
                    			if ((arenalist!=null) && (arenalist.size()>0)){
									if (arenalist.size() >= arenaService.getCreateArena2count()){
										sendMessage("您的2V2战队已经满了哦~", command.getSerial(), command.getSessionId());
									}else{
										byte[] bytes = stageService.getTaskBytes((short) 31001,
		                                        new String[] {"请输入您要邀请加入战队的角色名字。\n1.现在就邀请\n2.等下，我先查查",
		                                        "加入战队的角色名:", "join_arenateam 2 2 "});
		                                UWAPSegment seg = new UWAPSegment(ClientConstants.
		                                                                  GET_FILE_OK,
		                                                                  command.getSerial(),
		                                                                  command.getSessionId());
		                                seg.writeShort((short) 31001);
		                                seg.writeShort((short) 2);
		                                seg.write(bytes);
		                                write(seg);
									}
								}
                    			
                    		}else{
                    			sendMessage("只有队长可以邀请他人入队。", command.getSerial(), command.getSessionId());
                    		}
                        	
                    	}else{
                    		sendMessage("您还没有2v2战队，只有队长可以邀请他人入队。", command.getSerial(), command.getSessionId());
                    	}
                		
                	}else if(inputType == 2){
                		if(player.getArenaV2Id() > 0){
                    		ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                    		if (arenateam!=null){
                    			String joinplayername = command.getParam(2);
                        		if (joinplayername.length() == 0){
                        			sendMessage("未找到此用户。", command.getSerial(), command.getSessionId());
                        		}else{
                        			WorldPlayer joinplayer = playerService.getWorldPlayer(joinplayername);	 //zjl modify
                        			
                        			if (joinplayer == null ){
                        				sendMessage("因为需要对方确认，请在玩家在线时加入战队。谢谢合作。", command.getSerial(), command.getSessionId());
                        			}else{
                        				//int id = playerService.getPlayerId(joinplayername);
                            			//int ret = joinplayer.addBlackList(player.getId(), player.getAccountName());
                            			boolean flag = joinplayer.inBlackList(player.getId());
                            			if(flag){
                            				sendMessage("你已经在对方的黑名单中，无法邀请对方加入战队!", command.getSerial(), command.getSessionId());
                            				return;
                            			}
                        				if(joinplayer.getArenaV2Id() > 0){
                        					if (joinplayer.getArenaV2Id() == player.getArenaV2Id()){
                        						sendMessage("“" + joinplayername + "”已经在您的战队中了。", command.getSerial(), command.getSessionId());
                        					}else{
                        						sendMessage("“" + joinplayername + "”已经加入了一个2v2战队，不能同时加入两个2v2战队。", command.getSerial(), command.getSessionId());
                        					}
                                		}else{
                                			byte[] bytes = stageService.getTaskBytes((short) 31002,
                                                    new String[] {"您的朋友“"+ player.getPlayerName() +"”邀请您加入2v2战队["+arenateam.getArenaname()+"]?\n1.同意加入\n2.还不想加入",
                                                    "join_arenateam 2 3 " + arenateam.getId()+" "+ arenateam.getArenalevel()
                                                    +" "+ arenateam.getOwner()});
                                            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                    GET_FILE_OK);
                                            seg.writeShort((short) 31002);
                                            seg.writeShort((short) 2);
                                            seg.write(bytes);
                                            write(seg,joinplayer.getId());
                                		}
                        				
                        			}
                        		}
                    		}else{
                    			sendMessage("只有队长可以邀请他人入队。", command.getSerial(), command.getSessionId());
                    		}
                        	
                    	}else{
                    		sendMessage("您还没有2v2战队，只有队长可以邀请他人入队。", command.getSerial(), command.getSessionId());
                    	}
                	}else if(inputType == 3){
                		int arenaid = Integer.valueOf(command.getParam(2));
                		int arenalevel = Integer.valueOf(command.getParam(3));
                		int arenaownerId = Integer.valueOf(command.getParam(4));
                		if(player.getArenaV2Id() > 0){
                			sendMessage("“" + player.getPlayerName() + "”邀请您加入他的2v2战队，因为不能同时加入两个2v2战队，所以请您先离开当前战队或者婉言谢绝他。", command.getSerial(), command.getSessionId());
                			sendMessage(arenaownerId,"“" + player.getPlayerName() + "”已经加入了一个2v2战队，不能同时加入两个2v2战队。");
                		}else{
                			//加入2v2战队
                			arenaService.addArenaplayer(player,arenaid,arenalevel,arenaownerId,arenaType);
                			sendMessage("您已经成功加入2v2战队。", command.getSerial(), command.getSessionId());
                			sendMessage(arenaownerId,"“" + player.getPlayerName() + "”已经同意加入您的2v2战队。");
                			log.info("ID[" + player.getId() + "] ArenaID[" + arenaid +"] ArenaLevel[" + arenalevel + "] ArenaOwnerID[" + arenaownerId +"] ArenaType[" + arenaType + "] Arena Team Join 2v2");
                		}
                	}
                	
                }
                	break;
                case ArenaConstants.ARENA_TYPE_THREE: { //3v3
                	if (inputType == 1){//input
                		if(player.getArenaV3Id() > 0){
                    		ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                    		if (arenateam!=null){
                    			ArrayList arenalist = arenaService.findArenaTeamPlayer(arenateam.getId(),arenaType,arenateam.getOwner(),arenateam.getArenalevel());
                    			if ((arenalist!=null) && (arenalist.size()>0)){
									if (arenalist.size() >= arenaService.getCreateArena3count()){
										sendMessage("您的3V3战队已经满了哦~", command.getSerial(), command.getSessionId());
									}else{
										byte[] bytes = stageService.getTaskBytes((short) 31001,
		                                        new String[] {"请输入您要邀请加入战队的角色名字。\n1.现在就邀请\n2.等下，我先查查",
		                                        "加入战队的角色名:", "join_arenateam 3 2 "});
		                                UWAPSegment seg = new UWAPSegment(ClientConstants.
		                                                                  GET_FILE_OK,
		                                                                  command.getSerial(),
		                                                                  command.getSessionId());
		                                seg.writeShort((short) 31001);
		                                seg.writeShort((short) 2);
		                                seg.write(bytes);
		                                write(seg);
									}
								}
                    			
                    		}else{
                    			sendMessage("只有队长可以邀请他人入队。", command.getSerial(), command.getSessionId());
                    		}
                        	
                    	}else{
                    		sendMessage("您还没有3v3战队，只有队长可以邀请他人入队。", command.getSerial(), command.getSessionId());
                    	}
                		
                	}else if(inputType == 2){
                		if(player.getArenaV3Id() > 0){
                    		ArenaTeam arenateam = arenaService.findArenaTeam(player.getId(),arenaType);
                    		if (arenateam!=null){
                    			String joinplayername = command.getParam(2);
                        		if (joinplayername.length() == 0){
                        			sendMessage("未找到此用户。", command.getSerial(), command.getSessionId());
                        		}else{
                        			WorldPlayer joinplayer = playerService.getWorldPlayer(joinplayername);	// zjl modify
                        			//int id = playerService.getPlayerId(joinplayername);
                        			//int ret = joinplayer.addBlackList(id, joinplayername);
                        			boolean flag = joinplayer.inBlackList(player.getId());
                        			if(flag){
                        				sendMessage("你已经在对方的黑名单中，无法邀请对方加入战队!", command.getSerial(), command.getSessionId());
                        				return;
                        			}
                        			if (joinplayer == null ){
                        				sendMessage("因为需要对方确认，请在玩家在线时加入战队。谢谢合作。", command.getSerial(), command.getSessionId());
                        			}else{
                        				if(joinplayer.getArenaV3Id() > 0){
                        					if (joinplayer.getArenaV3Id() == player.getArenaV3Id()){
                        						sendMessage("“" + joinplayername + "”已经在您的战队中了。", command.getSerial(), command.getSessionId());
                        					}else{
                        						sendMessage("“" + joinplayername + "”已经加入了一个3v3战队，不能同时加入两个3v3战队。", command.getSerial(), command.getSessionId());
                        					}
                                		}else{
	                        				byte[] bytes = stageService.getTaskBytes((short) 31002,
	                                                new String[] {"您的朋友“"+ player.getPlayerName() +"”邀请您加入3v3战队["+arenateam.getArenaname()+"]?\n1.同意加入\n2.还不想加入",
	                                                "join_arenateam 3 3 " + arenateam.getId()+" "+ arenateam.getArenalevel()
	                                                +" "+ arenateam.getOwner()});
	                                        UWAPSegment seg = new UWAPSegment(ClientConstants.
	                                                GET_FILE_OK);
	                                        seg.writeShort((short) 31002);
	                                        seg.writeShort((short) 2);
	                                        seg.write(bytes);
	                                        write(seg,joinplayer.getId());
                                		}
                        			}
                        		}
                    		}else{
                    			sendMessage("只有队长可以邀请他人入队。", command.getSerial(), command.getSessionId());
                    		}
                        	
                    	}else{
                    		sendMessage("您还没有3v3战队，只有队长可以邀请他人入队。", command.getSerial(), command.getSessionId());
                    	}
                	}else if(inputType == 3){
                		int arenaid = Integer.valueOf(command.getParam(2));
                		int arenalevel = Integer.valueOf(command.getParam(3));
                		int arenaownerId = Integer.valueOf(command.getParam(4));
                		if(player.getArenaV3Id() > 0){
                			sendMessage("“" + player.getPlayerName() + "”邀请您加入他的3v3战队，因为不能同时加入两个3v3战队，所以请您先离开当前战队或者婉言谢绝他。", command.getSerial(), command.getSessionId());
                			sendMessage(arenaownerId,"“" + player.getPlayerName() + "”已经加入了一个3v3战队，不能同时加入两个3v3战队。");
                		}else{
                			//加入3v3战队
                			arenaService.addArenaplayer(player,arenaid,arenalevel,arenaownerId,arenaType);
                			sendMessage("您已经成功加入3v3战队。", command.getSerial(), command.getSessionId());
                			sendMessage(arenaownerId,"“" + player.getPlayerName() + "”已经同意加入您的3v3战队。");
                			log.info("ID[" + player.getId() + "] ArenaID[" + arenaid +"] ArenaLevel[" + arenalevel + "] ArenaOwnerID[" + arenaownerId +"] ArenaType[" + arenaType + "] Arena Team Join 3v3");
                		}
                	}
                	}
                	break;
                }
    		}catch(Exception ex){
                log.error("加入战队失败", ex);
            }
    	}
    }

    class LastlogoutAddexpProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	int r_tmp = 0;
			BathHouse bathHouse_tmp = BathHouse.getBathHouseByMapId((short) 1521);
			r_tmp += bathHouse_tmp.getRatio();
			int exp_tmp = (BathHouse.EXP[player.getLevel()] * r_tmp) / 100;
			int time_tmp = (int) (((new Date()).getTime() - (Server.player_lastlogout_time.get(player.getId())).getTime())/(1000 * 60 * 30));
			if (time_tmp > 0){
				long exp_tmp_long = exp_tmp * time_tmp;
				log.info("ID[" + player.getId() +"],Add EXP["+exp_tmp_long+"] try lastlogout["+Server.player_lastlogout_time.get(player.getId())+"]");
				Changed changed = new Changed();
				tryAddBathHouseExp(player, System.currentTimeMillis(), bathHouse_tmp, changed,Server.player_lastlogout_time.get(player.getId()));
		        connectService.sendGetItem(changed, player.getId(), (byte) 20);
			}
			log.info("ID[" + player.getId() +
                    "] addEXP[" + time_tmp + "] remove");
			Server.player_lastlogout_time.remove(player.getId());
        }
    }
    class OpenAllProcessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	int type = Integer.parseInt(command.getParam(0));
        	int count_tmp = 0;
        	if (type == 1){//砸蛋
        		if (player.hasItem(550034) || player.hasItem(200423) || player.hasItem(200424)){
    				//包内有精华金蛋，物理精华金蛋，魔法精华金蛋
        			while(player.hasItem(550034)){
                		if (!useitems(player,550034,command,true)){
                			break;
                		}else{
                			if (count_tmp>300){
                				break;
                			}else{
                				count_tmp++;
                			}
                		}
                	}
        			while(player.hasItem(200423)){
        				if (!useitems(player,200423,command,true)){
                			break;
                		}else{
                			if (count_tmp>300){
                				break;
                			}else{
                				count_tmp++;
                			}
                		}
                	}
        			while(player.hasItem(200424)){
        				if (!useitems(player,200424,command,true)){
                			break;
                		}else{
                			if (count_tmp>300){
                				break;
                			}else{
                				count_tmp++;
                			}
                		}
                	}
    			}else{
    				sendMessage("您背包里没有精华蛋哦，是不是忘了解开礼包了呢？", command.getSerial(), command.getSessionId());
    			}
        	}else if (type == 2){//开宝箱
        		if (player.hasItem(200224) || player.hasItem(210023)) {
    				//包内有宝箱钥匙或者钥匙串
//        			int box_flag = 0;
        			while (player.hasItem(200224)) {
//        				if (player.hasItem(200223)){
        					if (!useitems(player, 200224, command, false)) {
                    			break;
                    		} else {
                    			if (count_tmp>200) {
                    				break;
                    			} else {
                    				count_tmp++;
                    			}
                    		}
//        				}else{
//        					box_flag = 1;
//        					break;
//        				}        				
                	}
//        			if(box_flag<1){
        				while(player.hasItem(210023)){
//            				if (player.hasItem(200223,5)){
            					if (!useitems(player,210023,command,false)){
                        			break;
                        		}else{
                        			if (count_tmp>200){
                        				break;
                        			}else{
                        				count_tmp++;
                        			}
                        		}
//            				}else{
//            					box_flag = 2;
//            					break;
//            				}
                    	}
//        			}
//        			if(box_flag == 1){
//        				sendMessage("您背包里没有宝箱了哦", command.getSerial(), command.getSessionId());
//        			}else if (box_flag == 2){
//        				sendMessage("您背包里没有5个宝箱了哦", command.getSerial(), command.getSessionId());
//        			}
    			}else{
    				sendMessage("您背包里没有宝箱钥匙或者钥匙串哦，是不是忘了解开礼包了呢？", command.getSerial(), command.getSessionId());
    			}
        	}
        }
        private boolean useitems(WorldPlayer player, int itemId,Command command,boolean mailflag) throws Exception {
        	IItem item = Items.getTemplate(itemId).newInstance();
            if (item == null) {
                Utils.log(log, player.getId(), command.getAppType(),
                          "UseAll[" + itemId + "]ItemId[" +
                          itemId + "] Error");
                return false;
            } else {
            	int petId = -1;
                Changed changed = new Changed();
                int oldLevel = player.getLevel();
                Utils.log(log, player.getId(), command.getAppType(),
                          "UseAll[" + itemId + "]Item[" +
                          Utils.getHexdump(item.toDbBytes()) +
                          "]Money[" +
                          player.getMoeny() + "]TRY");
                if (item instanceof IEffectItem) {
                    boolean successed = false;
                    try {
                        int[] result = bufService.playerUseItem(player,
                                (IEffectItem) item,
                                changed,mailflag,playerId2Clients.get(player.getId()));
                        successed = result[0] == 1 ? true : false;
                        petId = result[1];
                        //mengjie add
                        checkLevelChangedAndSendTips(player, changed, command.getSerial(), command.getSessionId(), oldLevel);
                    } catch (UseItemException ex) {
                        sendMessage(ex.getMessage(), command.getSerial(),
                                        command.getSessionId());
                        return false;
                    }
                    sendGetItem(changed, command.getSerial(),
                            command.getSessionId(),
                        (byte) 4);
                    //使用召唤宠物物品后，自动装备
                    if (changed != null) {
                    	Pet pet = changed.getPeton(IItem.TYPE_PET);
                    	if (pet != null){
                    		//mengjie add 自动装备宠物
                    		if (player.getPet() == null){
                    			if (pet.getFavor() <= 15 || 
                    					pet.getLevel() > player.getLevel() ||
                    					player.getLevel() < 8){
                    				
                    			}else{
                    				player.setPet(pet);
                    				byte[] bytes = stageService.getTaskBytes((short) 31033,
                    						new String[] {String.valueOf(pet.getId())});
                    				UWAPSegment seg = new UWAPSegment(ClientConstants.
                    						GET_FILE_OK);
                    				seg.writeShort((short) 31033);
                    				seg.writeShort((short) 2);
                    				seg.write(bytes);
                    				connectService.writeTo(seg, player.getId());
                    			}
                    		}
                    	}
                    }
                    //判断新版本客户端，同步宠物装备信息
                    if (petId > 0) {
                		Pet pet = player.getPet(petId);
                		if (pet != null) {
                			try{
                    			UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
                    			seg.writeShort(ClientConstants.EXTEND_PROTOCOL_PETEQU_LOGIN);
                    			seg.writeInt(petId);
                    			Grid[] usedEquipmentsTemp = pet.getUsedEquipments();
                    			if (usedEquipmentsTemp != null){
                    				for (int jj = 0;jj<pet.getUsedEquipmentinfo().length;jj++){
                    					seg.write((byte) pet.getUsedEquipmentinfo()[jj]);
                    					if (usedEquipmentsTemp[jj] != null){
                    						if (pet.getUsedEquipmentinfo()[jj] == 1){
                    							IEquipment equtmp = (IEquipment) usedEquipmentsTemp[jj].item;
                    							equtmp.setDataVersion(player.getClientDataVersion());
                    							seg.write(equtmp.toClientBytesWithLevel(pet.getLevel()));
                    						}
                    					}
                    				}
                    			} else {
                    				for (int jj = 0;jj<pet.getUsedEquipmentinfo().length;jj++){
                    					seg.write((byte) pet.getUsedEquipmentinfo()[jj]);
                    				}
                    			}
                    			// 发送宠物升级所需升级经验
                    			seg.writeInt(Utils.getPetUpLevelExp(pet.getLevel()));
                    			
                    			//发送宠物阵营宝石效果
                        		CampData campData = getCampMainService().getCampData(player.getCamp());
                        		int value = 0;
                        		if(campData != null){
                    		    	List<CampSkillData> list = campData.getSkillDataList();
                    		    	for(int a = 0; a < list.size(); a++){
                    		    		CampSkillData temp = (CampSkillData) list.get(a);
                    		    		CampSkillLevel temp1 = CampConfig.campSkills.get(temp.getEffect()).getLevel(temp.getLevel());
                    		    		
                    		    		if(temp1 == null || temp1.getParm1() == 0){
                    		    			continue;
                    		    		}else{
                    		    			if(temp.getEffect() == Buf.CAMP_STONE){//阵营科技中只有阵营宝石buff取值下发
                    		    				value = temp1.getParm1();
                    		    				break;
                    		    			}
                    		    		}
                    		    	}
                    		    }
                        		seg.writeInt(value);
                    			
                    			connectService.writeTo(seg, player.getId());
                    		} catch (Exception e) {
                    			log.debug(e, e);
                    		}
                    	}
                    }
                    if (!successed) {
//                        sendMessage("现在还不能使用此物品", command.getSerial(),
//                                        command.getSessionId());
                        return false;
                    }
                }
                Utils.log(log, player.getId(), command.getAppType(),
                          "itemId[" + itemId + "]Item[" +
                          Utils.getHexdump(item.toDbBytes()) +
                          "]Money[" +
                          player.getMoeny() + "]");
                return true;
            }
			
        }
    }
    
    class GetEnemysListPreocessor implements CommandProcessor{
        public void process(WorldPlayer player, Command command) throws Exception {
        	int type = Integer.parseInt(command.getParam(0));
        	Enemy[] enemys = player.getEnemys();
            Arrays.sort(enemys);
            UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
            seg.writeShort((short) 10245);
            seg.writeString("仇人榜");
            seg.write((byte) 3);
            seg.writeShort((short) enemys.length);
            for (int j = 0; j < enemys.length; j++) {
                WorldPlayer p = playerService.getWorldPlayer(enemys[j].id);
                String onlineString = "";
                if(p!=null&&p.online()){
                    GameMap m = p.getMap();
                    if(m!=null)
                        onlineString = "["+m.getName()+"]";
                }else{
                    onlineString = "[离线]";
                }
                seg.writeInt(enemys[j].id);
                seg.writeString(enemys[j].name + "[仇恨值:" + enemys[j].times+"]"+onlineString);
                seg.writeInt(Utils.CLR_WHITE);
            }
            seg.write((byte) 2);
            seg.writeString("追踪仇人");
            if(player.hasItem(550023)){
            	seg.writeString("fllowenemys 550023");//高级仇人录
            }else if(player.hasItem(550022)){
            	seg.writeString("fllowenemys 550022");
            }else if(player.hasItem(550021)){
            	seg.writeString("fllowenemys 550021");
            }else{
            	seg.writeString("fllowenemys 550023");
            }
            seg.writeString("删除仇人");
            seg.writeString("deleteenemys");
            
//            switch (type) {
//            case 1:
//            	seg.writeString("fllowenemys 550023");//高级仇人录
//            	break;
//            case 2:
//            	seg.writeString("fllowenemys 550022");
//            	break;
//            case 3:
//            	seg.writeString("fllowenemys 550021");
//            	break;
//            }
            connectService.writeTo(seg,player.getId());
        }
    }
    //mengjie add end
    class CmccHistoryProcessor implements CommandProcessor{

    	SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMdd");

        public void process(WorldPlayer player, Command command) throws Exception {
            if (command.getParamCount() == 1) {
                int type = Integer.parseInt(command.getParam(0));
                Calendar cal = Calendar.getInstance();
                String end = sf1.format(cal.getTime());
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                String begin = sf1.format(cal.getTime());
                CmccHistoryRequest request = storeService.history(type, begin, end, 1, 10, ConnectSession2.this, command.getSessionId(), command.getSerial());
                UWAPSegment seg = new UWAPSegment(ServerConstants.CMCC_GET_HISTORY);
                seg.writeInt(request.id);
                seg.write((byte) type);
                seg.writeInt(player.getAccountId());
                seg.writeString(begin);
                seg.writeString(end);
                seg.writeInt(1); //startSequence
                seg.writeInt(10); //count
                Server.instance.authSession.write(seg);
            }
        }
    }
    class AddItemProcessor implements CommandProcessor{
    	
		public void process(WorldPlayer player, Command command)
				throws Exception {
			try {
				int itemID_old = Integer.valueOf(command.getParam(0));//扣除物品
				int DropItemID = Integer.valueOf(command.getParam(1));//选择掉落组中的物品id
				IItem item_old = Items.getTemplate(itemID_old).newInstance();
				Effect[] effects = ((IEffectItem)item_old).getEffects();
				for (int i = 0; i < effects.length; i++) {
					if (effects[i].getType() == 61){//物品效果为开启定向包
						DropGroupListEffect effect = (DropGroupListEffect)effects[i]; 
	                    DropGroup group = DropGroups.getDropGroup(effect.getGroup(),player.getLevel());
	                    ArrayList list = (ArrayList)group.getDropItems();				// 获得掉落组的列表
	                    int count = effect.getCount();
	                    boolean isDropItem = false;
	                    for (int j = 0; j < list.size(); j++) {
	                    	 DropItem dropItem = (DropItem)list.get(j);
	                         if(dropItem.getItem().getItemId() == DropItemID){
	                        	 isDropItem = true;
	                         }
	                    }
	                    if (isDropItem){
	                    	IItemTemplate item = Items.getTemplate(DropItemID);//选择后的掉落物品
	                    	Changed changed = new Changed();
		                    synchronized (player) {
			                    IItem isRemove = player.completeRemoveItem(itemID_old, 1, changed);
			                    if (isRemove != null){//使用物品已扣除
			                    	log.info("completeRemoveItem ID[" + player.getId() + "]GetItem[" + item.getItemId() +
			                                "]Count[" + 1 + "]");
			                    	if (item != null){
			                    		boolean sendMailMessage = false;
			                    		switch(effect.getParamType()){
			                    		case 1:	//选中的物品为装备时鉴定钻数为指定的钻数
			                    			IItem iitem = item.newInstance();
		                            		IEquipment tmpEqu = (IEquipment)iitem;
		                            		tmpEqu.setDataVersion(player.getClientDataVersion());
		                            		tmpEqu.setDiamond((byte)effect.getParam());
		                            		if (player.completeAddItem(tmpEqu, 1, changed, player.getClientDataVersion()) == null){
		                                    	byte[] att = ItemUtils.item2dbAttachment(tmpEqu, 1);
		                                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
		                                                            tmpEqu.getName(), "", att, 0, true);
		                                        sendMailMessage = true;
		                                    }
		                            		String item_msg = Items.getMessage(iitem.getItemId(),7,player.getPlayerName(),iitem.getName(),item_old.getName(), iitem);
		                                    if (item_msg != null){
		                                    	chatService.sendWorldMessage(-1, "系统", item_msg, iitem);
		                                    }
			                    			break;
			                    		default:
			                    			int c = player.addItem(item, count, changed, player.getClientDataVersion());
			                    			if (c == 0) {
			                    				byte[] att = ItemUtils.item2dbAttachment(item.newInstance(),count);
			                    				mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
			                    						item.getName() + "*" + count, "", att, 0, true);
			                    				sendMailMessage = true;
			                    			}
			                    			if(item instanceof EquipmentTemplate){
			                    				IItem iitem2 = item.newInstance();
			                            		IEquipment tmpEqu2 = (IEquipment)iitem2;
			                            		tmpEqu2.setDataVersion(player.getClientDataVersion());
			                            		String item_msg2 = Items.getMessage(tmpEqu2.getItemId(),7,player.getPlayerName(),tmpEqu2.getName(),item_old.getName(), tmpEqu2);
			                            		if (item_msg2 != null){
			                            			chatService.sendWorldMessage(-1, "系统", item_msg2, tmpEqu2);
			                            		}
			                    			}else{
			                    				String item_msg2 = Items.getMessage(item.getItemId(),7,player.getPlayerName(),item.getName(),item_old.getName());
			                            		if (item_msg2 != null){
			                            			chatService.sendWorldMessage(-1, "系统", item_msg2);
			                            		}
			                    			}
			                    		}
			                    		if(sendMailMessage){
			                    			sendMessage("由于背包满，物品已经邮寄到邮箱中，请注意查收。", command.getSerial(), command.getSessionId());
			                    		}
			                    		sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 22);
			                    		log.info("GetDropGroup ID[" + player.getId() + "]RemoveItem ID["+itemID_old+"]GetItem[" + item.getItemId() +
			                    				"]Count[" + count + "] paramType[" + effect.getParamType() + "]");
			                    	}else{
			                    		sendMessage("获得物品失败", command.getSerial(), command.getSessionId());
			                    	}
			                    }else{
			                    	sendMessage("你没有此物品", command.getSerial(), command.getSessionId());
			                    }
		                    }
	                    }else{
	                    	log.info("GetDropGroup ID[" + player.getId() + "]RemoveItem ID["+itemID_old+"]GetItem[" + DropItemID +
	                                "]Count[" + count + "]ERROR BUG");
	                    }
					}
				}                
            }catch (Exception ex) {
                log.error("获得物品失败", ex);
            }
		}
    	
    }
    class VoteCampProcessor implements CommandProcessor{
    	public void process(WorldPlayer player, Command command)
		throws Exception {
    		/*
    		 *  "voteCamp 1",
                "voteCamp 2",
                "voteCamp 3",
                "voteCamp 4",
    		 */
    		int itemType = Integer.valueOf(command.getParam(0));		//菜单类型
    		if(itemType == 1){			//1.	领袖报名
    			if(campMainService.getCampAuctionService().getState() == CampVoteService.STATE_STARTED){
    				byte[] bytes = stageService.getTaskBytes((short) 31002,
                            new String[] {"领袖是本阵营的灵魂人物；参选需要以荣誉点数竞拍候选人资格，" +
                            		"出价前五名者成功入围竞选，竞拍结束后返还失败者的荣誉点数；" +
                            		"你要查看阵营领袖入围名单吗？\n1.确认\n2.取消",
                            "voteCamp " + 5});
                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                            GET_FILE_OK,
                            command.getSerial(),
                            command.getSessionId());
                    seg.writeShort((short) 31002);
                    seg.writeShort((short) 2);
                    seg.write(bytes);
                    write(seg);
    			}else{
    				sendMessage("阵营领袖的报名时间是每周的周三早10点到周四晚12点，要记住哦。", command.getSerial(), command.getSessionId());
    			}
    		}else if(itemType == 2){		//2.	阵营投票
    			if(campMainService.getCampVoteService().getState() == CampVoteService.STATE_STARTED){
    				//投票开始
    				if(player.getLevel() < 80 ){
                		//低于50级的玩家
    					sendMessage("请您升到80级以后再来参与吧！", command.getSerial(), command.getSessionId());
                	}else{
                	    CampCandidate[] campCandidates = campMainService.getCampVoteService().getCandidateList(player.getCamp());
                        
                        UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL,command.getSerial(),command.getSessionId());
                        seg.writeShort(ClientConstants.EXTEND_VOTE);
                        seg.write((byte)1);         //刷新新的界面
                        
                        int[] temp = new int[6];
                        temp[0] = CampMainService.VOTE_ITEM_ID;
                        temp[1] = player.getItemCount2(CampMainService.VOTE_ITEM_ID);
                        temp[2] = CampMainService.VOTE_ISHOP_ITEM_ID;
                        temp[3] = player.getItemCount2(CampMainService.VOTE_ISHOP_ITEM_ID);
                        temp[4] = CampMainService.VOTE_EGG_ITEM_ID;
                        temp[5] = player.getItemCount2(CampMainService.VOTE_EGG_ITEM_ID);
                        seg.writeInts(temp);
                        seg.write((byte)campCandidates.length);
                        
                        PlayerDao playerDao = new PlayerDao();
                        
                        for(int i = 0; i < campCandidates.length; i++){
                            //已经有人出价了
                            seg.writeInt(campCandidates[i].getPlayerid());
                            seg.writeString(playerDao.getPlayerName(campCandidates[i].getPlayerid()));
                            seg.writeInt(campCandidates[i].getTotalvote());     //票数
                            if(campCandidates[i].getMagicremain() <= 0){
                                seg.write((byte)0);         //判断是否使用了魔力分享0无1有
                            }else{
                                seg.write((byte)1);
                            }
                            seg.writeString(campCandidates[i].getSlogan());    //竞选宣言
                        }
                        
                        write(seg);
                	}
    			}else{
    				sendMessage("新的阵营领袖候选人名单还没最终决定，请在周五0点到周一10点之间再来投票。", command.getSerial(), command.getSessionId());
    			}
    			
    		}else if(itemType == 3){			//3.	竞选指南
    			sendMessage("新任阵营领袖在每周一上午10点产生，由领袖候选者中获得投票最多的一位当选；" +
    					"候选报名的有效时间是周三早10点到周四24点；荣誉竞拍前5名获得候选人资格，" +
    					"当前的阵营领袖自动入围下一届候选人名单；阵营中的每个90级以上玩家都可以报名，每个80级以上的玩家都可以投票！", 
    					command.getSerial(), command.getSessionId());
    			
    		}else if(itemType == 4){			//4.	领袖的权利
    			sendMessage("阵营领袖地位尊崇，万人瞩目，他将会获得特别的尊号、特别的形象、" +
    					"……还会掌控着阵营金库的钥匙，能给他的人民布施福泽，也能带来祸患。", 
    					command.getSerial(), command.getSessionId());  			
    		}else if(itemType == 5){			//5.	领袖报名下级菜单
    			//当自然时间不属于报名时间
    			if(campMainService.getCampAuctionService().getState() != CampAuctionService.STATE_STARTED){
    				sendMessage("阵营领袖的报名时间是每周的周三早10点到周四晚12点，要记住哦。", command.getSerial(), command.getSessionId());
    			}else{
    				int name = campMainService.getKingId(player.getCamp());	//获得当前的阵营的国王
    				if(name != 0 && name == player.getId()){
    					
    					List<CampQualification> list = campMainService.getCampAuctionService().getQualificationList(player.getCamp());
    					// 下发协议，客户端显示
    					UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL,command.getSerial(),command.getSessionId());
						seg.writeShort(ClientConstants.EXTEND_VOTECAMP);
						seg.write((byte)1);	
						seg.write((byte)0);
						seg.write(player.getCamp());	// 
						seg.writeInt(player.getCredit());	//
    					if(list == null || list.size() == 0){
    						seg.write((byte)0);
    						write(seg);
    					}else{
                            PlayerDao playerDao = new PlayerDao();
                            
    						seg.write((byte)list.size());
    						
    						for(CampQualification tmp : list){
                                seg.writeString(playerDao.getPlayerName(tmp.getPlayerid()));
                            }
    						
    						write(seg);
    					}
    				}else if(player.getLevel() < 90 ){
                		//低于50级的玩家
    					sendMessage("请您升到90级以后再来参与吧！", command.getSerial(), command.getSessionId());
                	}else{
                	  //下发协议，重画界面
                        List<CampQualification> list = campMainService.getCampAuctionService().getQualificationList(player.getCamp());
                        CampQualification campQualification = campMainService.getCampAuctionService().getCampQualification(player.getId());
                        UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL,command.getSerial(),command.getSessionId());
                        seg.writeShort(ClientConstants.EXTEND_VOTECAMP);
                        seg.write((byte)1);             //重画界面
                        
                        if(campQualification == null){
                            seg.write((byte)2); 
                            seg.write(player.getCamp()); 
                            seg.writeInt(player.getCredit());
                        }else{
                            seg.write((byte)1); 
                            seg.write(player.getCamp()); 
                            seg.writeInt(campQualification.getRemain());
                            seg.writeInt(campQualification.getTotal());
                        }
                        
                        if(list == null || list.size() == 0){
                            seg.write((byte)0);
                            write(seg);
                        }else{
                            seg.write((byte)list.size());
                            PlayerDao playerDao = new PlayerDao();
                            
                            for(CampQualification tmp : list){
                                seg.writeString(playerDao.getPlayerName(tmp.getPlayerid()));
                            }
                            
                            write(seg);
                        }
    				}
    			}
    		}
    	}
    }
    
 // 食神活动
    class IronChefProcessor implements CommandProcessor{
    	public void process(WorldPlayer player, Command command) throws Exception {
    		if (player != null) {
    			synchronized (player) {
    				int itemType = Integer.valueOf(command.getParam(0));
    				if (itemType == 1) {	// 捐献腊八豆
    					if (twelfthLunarService.getStage() == TwelfthLunarService.STAGE_NOT_STARTED) {
    						sendMessage("食神正在休息，请稍后再来捐献豆子吧~", 
    								command.getSerial(), command.getSessionId());
    					} else if (twelfthLunarService.getStage() == TwelfthLunarService.STAGE_DONATE_STARTED) {
    						int count = player.getItemCount(TwelfthLunarConfig.donateItemId);
    						if (count > 0) {
    							Changed changed = new Changed();
    							IItem item = player.completeRemoveItem(TwelfthLunarConfig.donateItemId, count, changed);
    							if (item != null) {
    								Utils.log(log, player.getId(), command.getAppType(),
    										"]Item[" + Utils.getHexdump(item.toDbBytes()) + "]count[" + count + "]");
    							} else {
    								Utils.log(log, player.getId(), command.getAppType(),
    										"]ItemId[" + TwelfthLunarConfig.donateItemId + "] Error");
    							}
    							
    							IItem iit = Items.getTemplate(TwelfthLunarConfig.donateGiftId).newInstance();
    							if (iit != null) {
    								IItem nItem = player.completeAddItem(iit, count, changed, player.getClientDataVersion());
    								if (nItem == null) {
    									byte[] att = ItemUtils.item2dbAttachment(iit, count);
    									mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
    											iit.getName() + "*" + count, "这是食神的礼物，您的背包已满，请整理后再提取", att, 0, true);
    									sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到您的邮箱!");
    								} else {
    									Utils.log(log, player.getId(), command.getAppType(),
    											"] Add ItemId[" + TwelfthLunarConfig.donateGiftId + "] Success");
    								}
    							} else {
    								Utils.log(log, player.getId(), command.getAppType(),
    										"]ItemId[" + TwelfthLunarConfig.donateGiftId + "] Error");
    							}
    							player.setTwelfthLunarBeansCount(player.getTwelfthLunarBeansCount() + count);
    							twelfthLunarService.playerDonateItem(player, count);
    							twelfthLunarService.setGruelCount(twelfthLunarService.getGruelCount() + count);
    							sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);
    						} else {
    							sendMessage("如果你有腊八豆，我愿意用传家宝和你交换。", 
    									command.getSerial(), command.getSessionId());
    						}
    					} else {
    						sendMessage("不好意思，捐豆活动已经结束了，不能再捐献了。", 
    								command.getSerial(), command.getSessionId());
    					}
    				} else if(itemType == 2) {	// 领取腊八粥
    					if (twelfthLunarService.getStage() == TwelfthLunarService.STAGE_DONATE_END) {
    						sendMessage("不好意思，施粥活动已经结束了，不能再领粥了。",
    								command.getSerial(), command.getSessionId());
    						return;
    					}
    					if (twelfthLunarService.checkStarted()) {
    						if (player.getLevel() < twelfthLunarService.getActivityLevel()) {
    							sendMessage("20级以上的玩家才能得到我施舍的粥，快去升级吧~", 
    									command.getSerial(), command.getSessionId());
    							return;
    						}
    						if (twelfthLunarService.getGruelCount() > 0) {
    							long todayStart = Utils.getTodayStart();
    							long lastGetGiftTime = player.getTwelfthLunarLastReceiveTime().getTime();
    							if (lastGetGiftTime < todayStart) {
    								twelfthLunarService.setGruelCount(twelfthLunarService.getGruelCount() - 1);
    								Changed changed = new Changed();
    								IItem iit = Items.getTemplate(twelfthLunarService.getActivityGiftId()).newInstance();
    								if (iit != null) {
    									IItem nItem = player.completeAddItem(iit, twelfthLunarService.getActivityGiftCount(), changed, player.getClientDataVersion());
    									if (nItem == null) {
    										byte[] att = ItemUtils.item2dbAttachment(iit, twelfthLunarService.getActivityGiftCount());
    										mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
    												iit.getName() + "*" + twelfthLunarService.getActivityGiftCount(),
    												"这是食神的礼物，您的背包已满，请整理后再提取", att, 0, true);
    										sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到您的邮箱!");
    									} else {
    										Utils.log(log, player.getId(), command.getAppType(),
    												"] Add ItemId[" + TwelfthLunarConfig.donateGiftId + "] Success");
    									}
    								} else {
    									Utils.log(log, player.getId(), command.getAppType(),
    											"]ItemId[" + twelfthLunarService.getActivityGiftId() + "] Error");
    								}
    								player.setTwelfthLunarLastReceiveTime(new Date());
    								sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);
    							} else {
    								sendMessage("一人一碗，不要太贪呀~，觉得味美请明天再来吧！", 
    										command.getSerial(), command.getSessionId());
    							}
    						} else {
    							sendMessage("食神难做无豆之粥，想要腊八粥，快请朋友们送点腊八豆来吧~", 
    									command.getSerial(), command.getSessionId());
    						}
    					} else {
    						sendMessage("腊八粥还没有熬好，请19:00再来吧！", 
    								command.getSerial(), command.getSessionId());
    					}
    				} else if (itemType == 3) {	// 察看捐献腊八豆排行榜
    					if (twelfthLunarService.getTopStage() == TwelfthLunarService.STAGE_NOT_STARTED) {
    						sendMessage("还不到揭榜的时间，请稍等片刻。", 
    								command.getSerial(), command.getSessionId());
    					} else if (twelfthLunarService.getTopStage() == TwelfthLunarService.STAGE_TOP_STARTED) {
    						twelfthLunarService.setTopList();
    						List list = twelfthLunarService.getTopList();
    						if (list == null || list.isEmpty() || list.size() == 0) {
    							sendMessage("还没有玩家登上排行榜，要把握机会啊。", command.getSerial(), command.getSessionId());
    						} else {
    							UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
    							seg.writeShort((short) 10267);
    							seg.writeString("排行榜");
    							seg.write((byte) 0);
    							seg.writeShort((short) Math.min(list.size(), TwelfthLunarConfig.topCount));
    							for(int i = 0; i < Math.min(list.size(), TwelfthLunarConfig.topCount); i++) {
    								Map.Entry<Integer, TwelfthLunarShowInfo> infoMap = (Entry<Integer, TwelfthLunarShowInfo>) list.get(i);
    								TwelfthLunarShowInfo showInfo = infoMap.getValue();
    								if(showInfo == null || showInfo.getPlayerName()== null ||((showInfo.getPlayerName().equals("") && showInfo.getPlayerName().length() == 0))){
    									continue;
    								}
    								seg.writeInt(showInfo.getId());
    								seg.writeString(i + 1 + "." + showInfo.getPlayerName() + " " + showInfo.getCount() + "个");
    								seg.writeInt(Utils.CLR_WHITE);
    							}
    							connectService.writeTo(seg, player.getId());
    						}
    					} else {
    						sendMessage("活动结束了，不能查看了~", 
    								command.getSerial(), command.getSessionId());
    					}
    				} else if (itemType == 4) {
    					if (twelfthLunarService.getTopStage() == TwelfthLunarService.STAGE_NOT_STARTED) {
    						sendMessage("食神正在休息，稍后再来问他吧~", 
    								command.getSerial(), command.getSessionId());
    					} else if (twelfthLunarService.getTopStage() == TwelfthLunarService.STAGE_TOP_STARTED) {
    						int ownCount = player.getTwelfthLunarBeansCount();
    						int serverCount = twelfthLunarService.getPlyaerDonateCount(player.getId());
    						if (ownCount == serverCount) {
    							sendMessage("你一共捐献了" + serverCount + "袋腊八豆。", 
    									command.getSerial(), command.getSessionId());
    						} else if (ownCount > serverCount) {
    							sendMessage("你一共捐献了" + ownCount + "袋腊八豆。", 
    									command.getSerial(), command.getSessionId());
    							twelfthLunarService.playerDonateItem (player, ownCount - serverCount);
    						} else {
    							sendMessage("你一共捐献了" + serverCount + "袋腊八豆。", 
    									command.getSerial(), command.getSessionId());
    							twelfthLunarService.playerDonateItem (player, serverCount - ownCount);
    						}
    					} else {
    						sendMessage("活动结束了，不能查看了~", 
    								command.getSerial(), command.getSessionId());
    					}
    				} else if (itemType == 5) {	// 详情
    					sendMessage("每捐献一袋腊八豆，食神将制造一碗腊八粥，（捐献无时间和次数限制）每日19：00食神开始免费施粥，到23：30结束，数量有限（数量等于当时捐献的腊八豆的数量），先到先得，每人每日只能领一次（等级20级以上）。", 
    							command.getSerial(), command.getSessionId());
    				} else if (itemType == 6) {	// 一会再说
    					sendMessage("来看看嘛。", 
    							command.getSerial(), command.getSessionId());
    				}
    			}
    		}
    	}
    }
    
    // 宠物重铸大师
    class PetRecastMasterProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
        	if (player != null) {
        		synchronized (player) {
        			int petmanagertpye = Integer.parseInt(command.getParam(0));
        			Petmanager[] petmanager = null;
        			try{
        				if ((petmanagertpye > 0) && (petmanagertpye < 4)){
        					petmanager = petmanagerService.getPets(player.getId());
        				}
        				if (petmanagertpye == 1) {	// 宠物修炼（第一页，前5个）
        					if (petmanager == null) {
        						Pet[] pets = player.getPets();
        						if ((pets == null) || (pets.length ==0)) {
        							sendMessage("您没有宠物哦，去捉一只再来吧。", command.getSerial(), command.getSessionId());
        						} else {
        							String petslist = "你要让谁修炼呀？";
        							int tmpNum = 0;
        							int tmpLen = pets.length;
        							if(tmpLen > 5){
        								tmpLen = 5;
        								tmpNum = 1;
        							}
    								String [] questions = new String[tmpLen + 3 + tmpNum];
        							for (int i=0; i < tmpLen; i ++) {
        								petslist += "\n" + String.valueOf(i + 1) + "." + pets[i].getName() + "(" + pets[i].getLevel() + "级)";
        								questions[3 + i] = "petPracticeTime " + pets[i].getId();
        							}
        							if(tmpNum == 1){
        								petslist += "\n" + String.valueOf(tmpLen + 1) + ".下一页";
										questions[3 + tmpLen] = "petRecastMaster 4";
        							}
    								petslist += "\n" + String.valueOf(tmpLen + 1 + tmpNum) + ".算了，先不修炼了。";
        							questions[0] = String.valueOf(tmpLen + 1 + tmpNum);
        							questions[1] = "1";
        							questions[2] = petslist;
        							byte[] bytes = stageService.getTaskBytes((short) 31010, questions);
        							UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
        									command.getSerial(),
        									command.getSessionId());
        							seg.writeShort((short) 31010);
        							seg.writeShort((short) 2);
        							seg.write(bytes);
        							write(seg);
        						}
        					} else {
        						if (petmanager.length >= Petmanager.petcount) {
        							sendMessage("您已经有" + petmanager.length + "只宠物在修炼了，不能再增加了，请领走一只再来吧。", command.getSerial(), command.getSessionId());
        						}else{
        							Pet[] pets = player.getPets();
        							if ((pets == null) || (pets.length == 0)) {
        								sendMessage("您没有宠物哦，去捉一只再来吧。", command.getSerial(), command.getSessionId());
        							} else {
        								String petslist = "你要让谁修炼呀？";
        								int tmpNum = 0;
            							int tmpLen = pets.length;
            							if(tmpLen > 5){
            								tmpLen = 5;
            								tmpNum = 1;
            							}
        								String [] questions = new String[tmpLen + 3 + tmpNum];
        								for(int i=0;i<tmpLen;i++){
        									petslist += "\n" + String.valueOf(i+1) + "." + pets[i].getName() + "(" + pets[i].getLevel() + "级)";
        									questions[3 + i] = "petPracticeTime " + pets[i].getId();
        								}
        								if(tmpNum == 1){
            								petslist += "\n" + String.valueOf(tmpLen + 1) + ".下一页";
    										questions[3 + tmpLen] = "petRecastMaster 4";
            							}
        								petslist += "\n" + String.valueOf(tmpLen + 1 + tmpNum) + ".算了，先不修炼了。";
        								questions[0] = String.valueOf(tmpLen + 1 + tmpNum);
        								questions[1] = "1";
        								questions[2] = petslist;
        								byte[] bytes = stageService.getTaskBytes((short) 31010, questions);
        								UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
        										command.getSerial(),
        										command.getSessionId());
        								seg.writeShort((short) 31010);
        								seg.writeShort((short) 2);
        								seg.write(bytes);
        								write(seg);
        							}
        						}
        					}
        				} else if (petmanagertpye == 2) {	// 宠物重铸（第一页，前5个）
        					Pet[] pets = player.getPets();
        					if ((pets == null) || (pets.length == 0)) {
        						sendMessage("您没有宠物哦，去捉一只再来吧。", command.getSerial(), command.getSessionId());
        					} else {
        						String petslist = "你要重铸哪只宠物？";
        						int tmpNum = 0;
    							int tmpLen = pets.length;
    							if(tmpLen > 5){
    								tmpLen = 5;
    								tmpNum = 1;
    							}
        						String [] questions = new String[tmpLen + 3 + tmpNum];
        						for (int i = 0; i < tmpLen; i++) {
        							petslist += "\n" + String.valueOf(i + 1) + "." + pets[i].getName() + "(" + pets[i].getLevel() + "级)";
        							questions[3 + i] = "petRecast " + pets[i].getId();
        						}
        						if(tmpNum == 1){
    								petslist += "\n" + String.valueOf(tmpLen + 1) + ".下一页";
									questions[3 + tmpLen] = "petRecastMaster 5";
    							}
        						petslist += "\n" + String.valueOf(tmpLen + 1 + tmpNum) + ".算了，先不重铸了。";
        						questions[0] = String.valueOf(tmpLen + 1 + tmpNum);
        						questions[1] = "1";
        						questions[2] = petslist;
        						byte[] bytes = stageService.getTaskBytes((short) 31010, questions);
        						UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
        								command.getSerial(),
        								command.getSessionId());
        						seg.writeShort((short) 31010);
        						seg.writeShort((short) 2);
        						seg.write(bytes);
        						write(seg);
        					}
        				} else if (petmanagertpye == 3) {	// 领回宠物
        					if (petmanager == null){
        						sendMessage("您还没有宠物在此修炼。", command.getSerial(), command.getSessionId());
        					} else if(petmanager.length == 0 ){
        						sendMessage("您还没有宠物在此修炼。", command.getSerial(), command.getSessionId());
        					} else {
        						String petslist = "你来接谁呀？";
        						String [] questions = new String[petmanager.length + 3];
        						for (int i = 0; i < petmanager.length; i++) {
        							String msg = "";
        							Pet pet = Pet.getPetFromDb(petmanager[i].getPet());
        							if (pet != null) {
        								// 检查修炼的时间与玩家上线的时间
        								long playerLoginTime = player.getLastLoginTime().getTime();
        								long petBeganPracticeTime = petmanager[i].getEattime().getTime();
        								long practiceTime;
        								if (petBeganPracticeTime >= playerLoginTime) {
        									practiceTime = (new Date ()).getTime() - petBeganPracticeTime;
        								} else {
        									practiceTime = petmanager[i].getPracticeTime();
        									long playerGameTime = (new Date ()).getTime() - playerLoginTime;
        									practiceTime += playerGameTime;
        								}
        								int minute_all = (int) (practiceTime / Utils.UNIT_OF_MINUTE);
        								int minute = minute_all % 60;
        								int house_all = minute_all / 60;
        								if (minute > 59) {
        									minute = 0;
        									house_all = house_all + 1;
        								}
        								msg = "已修炼";
        								if (house_all > 0) {
        									msg = msg + house_all + "小时";
        								}
        								if (minute > 0 || (minute == 0 && house_all == 0)) {
        									msg = msg + minute + "分钟";
        								}
        								petslist += "\n" + String.valueOf(i+1) + "." + pet.getName() + "(" + pet.getLevel() + "级) " + msg;
        								questions[3 + i] = "petPracticeOut " + pet.getId();
        							}
        						}
        						petslist += "\n" + String.valueOf(petmanager.length + 1) + ".算了，先不领了。";
        						questions[0] = String.valueOf(petmanager.length + 1);
        						questions[1] = "1";
        						questions[2] = petslist;
        						byte[] bytes = stageService.getTaskBytes((short) 31010, questions);
        						UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
        								command.getSerial(),
        								command.getSessionId());
        						seg.writeShort((short) 31010);
        						seg.writeShort((short) 2);
        						seg.write(bytes);
        						write(seg);
        					}
        				}else if(petmanagertpye == 4){//宠物修炼 (第二页)
        					if (petmanager == null) {
        						Pet[] pets = player.getPets();
        						if ((pets == null) || (pets.length ==0)) {
        							sendMessage("您没有宠物哦，去捉一只再来吧。", command.getSerial(), command.getSessionId());
        						} else if(pets.length <=5){
        							sendMessage("您没有那么多宠物。", command.getSerial(), command.getSessionId());
        						} else {
        							String petslist = "你要让谁修炼呀？";
        							int tmpNum = 5;
        							int tmpLen = pets.length - tmpNum;
    								String [] questions = new String[tmpLen + 4];
        							for (int i=0; i < tmpLen; i ++) {
        								petslist += "\n" + String.valueOf(i + 1) + "." + pets[i + tmpNum].getName() + "(" + pets[i + tmpNum].getLevel() + "级)";
        								questions[3 + i] = "petPracticeTime " + pets[i + tmpNum].getId();
        							}
        							petslist += "\n" + String.valueOf(tmpLen + 1) + ".上一页";
        							questions[3 + tmpLen] = "petRecastMaster 1";
    								petslist += "\n" + String.valueOf(tmpLen + 2) + ".算了，先不修炼了。";
        							questions[0] = String.valueOf(tmpLen + 2);
        							questions[1] = "1";
        							questions[2] = petslist;
        							byte[] bytes = stageService.getTaskBytes((short) 31010, questions);
        							UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
        									command.getSerial(),
        									command.getSessionId());
        							seg.writeShort((short) 31010);
        							seg.writeShort((short) 2);
        							seg.write(bytes);
        							write(seg);
        						}
        					} else {
        						if (petmanager.length >= Petmanager.petcount) {
        							sendMessage("您已经有" + petmanager.length + "只宠物在修炼了，不能再增加了，请领走一只再来吧。", command.getSerial(), command.getSessionId());
        						}else{
        							Pet[] pets = player.getPets();
        							if ((pets == null) || (pets.length == 0)) {
        								sendMessage("您没有宠物哦，去捉一只再来吧。", command.getSerial(), command.getSessionId());
        							} else if(pets.length <=5){
            							sendMessage("您没有那么多宠物。", command.getSerial(), command.getSessionId());
        							} else {
        								String petslist = "你要让谁修炼呀？";
        								int tmpNum = 5;
            							int tmpLen = pets.length - tmpNum;
        								String [] questions = new String[tmpLen + 4];
            							for (int i=0; i < tmpLen; i ++) {
            								petslist += "\n" + String.valueOf(i + 1) + "." + pets[i + tmpNum].getName() + "(" + pets[i + tmpNum].getLevel() + "级)";
            								questions[3 + i] = "petPracticeTime " + pets[i + tmpNum].getId();
            							}
            							petslist += "\n" + String.valueOf(tmpLen + 1) + ".上一页";
            							questions[3 + tmpLen] = "petRecastMaster 1";
        								petslist += "\n" + String.valueOf(tmpLen + 2) + ".算了，先不修炼了。";
            							questions[0] = String.valueOf(tmpLen + 2);
        								questions[1] = "1";
        								questions[2] = petslist;
        								byte[] bytes = stageService.getTaskBytes((short) 31010, questions);
        								UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
        										command.getSerial(),
        										command.getSessionId());
        								seg.writeShort((short) 31010);
        								seg.writeShort((short) 2);
        								seg.write(bytes);
        								write(seg);
        							}
        						}
        					}
        				}else if(petmanagertpye == 5){ //宠物重铸 (第二页)
        					Pet[] pets = player.getPets();
        					if ((pets == null) || (pets.length == 0)) {
        						sendMessage("您没有宠物哦，去捉一只再来吧。", command.getSerial(), command.getSessionId());
        					} else if(pets.length <=5){
    							sendMessage("您没有那么多宠物。", command.getSerial(), command.getSessionId());
        					} else {
        						String petslist = "你要重铸哪只宠物？";
        						int tmpNum = 5;
    							int tmpLen = pets.length - tmpNum;
        						String [] questions = new String[tmpLen + 4];
        						for (int i = 0; i < tmpLen; i++) {
        							petslist += "\n" + String.valueOf(i + 1) + "." + pets[i + tmpNum].getName() + "(" + pets[i + tmpNum].getLevel() + "级)";
        							questions[3 + i] = "petRecast " + pets[i + tmpNum].getId();
        						}
        						
    							petslist += "\n" + String.valueOf(tmpLen + 1) + ".上一页";
								questions[3 + tmpLen] = "petRecastMaster 2";
        						petslist += "\n" + String.valueOf(tmpLen + 2) + ".算了，先不重铸了。";
        						questions[0] = String.valueOf(tmpLen + 2);
        						questions[1] = "1";
        						questions[2] = petslist;
        						byte[] bytes = stageService.getTaskBytes((short) 31010, questions);
        						UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
        								command.getSerial(),
        								command.getSessionId());
        						seg.writeShort((short) 31010);
        						seg.writeShort((short) 2);
        						seg.write(bytes);
        						write(seg);
        					}
        				}
        			} catch (Exception ex) {
        				log.error("宠物重铸大师信息错误。", ex);
        			}
        		}
        	}
    	}
    }
    

    // 宠物修炼的时间
    class PetPracticeTimeProcessor implements CommandProcessor {
    	public void process (WorldPlayer player, Command command) throws Exception {
    		if (player != null) {
    			synchronized (player) {
    				int petId = Integer.parseInt(command.getParam(0));
    				try{
    					Pet pet = player.getPet(petId);
    					if (pet != null) {
							String[] capacityList = new String[] {"一", "二", "三", "四", "五"};
							int [] practiceTime = new int[] {1, 2, 3, 4, 5};
    						String titlelist = "你要修炼多长时间？";
    						String [] questions = new String[practiceTime.length + 3];
    						for (int i = 0; i < practiceTime.length; i++) {
    							titlelist += "\n" + String.valueOf(i + 1) + "." + capacityList[i] + "小时";
    							questions[3 + i] = "petPractice " + petId + " " + practiceTime[i];
    						}
    						titlelist += "\n" + String.valueOf(practiceTime.length + 1) + ".算了，先不修炼了。";
    						questions[0] = String.valueOf(practiceTime.length + 1);
    						questions[1] = "1";
    						questions[2] = titlelist;
    						byte[] bytes = stageService.getTaskBytes((short) 31010, questions);
    						UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
    								command.getSerial(),
    								command.getSessionId());
    						seg.writeShort((short) 31010);
    						seg.writeShort((short) 2);
    						seg.write(bytes);
    						write(seg);
    					}
    				} catch (Exception ex) {
    					log.error("宠物修炼时间选择错误。", ex);
    				}
    			}
    		}
    	}
    }
    
    // 宠物修炼
    class PetPracticeProcessor implements CommandProcessor {
    	private static final int CONSUME_HONOR_OF_HOUR = 5;
        public void process(WorldPlayer player, Command command) throws Exception {
        	if (player != null) {
        		synchronized (player) {
        			int petId = Integer.parseInt(command.getParam(0));
        			int time = Integer.parseInt(command.getParam(1));
        			log.info("ID[" + player.getId() + "] PetPractice PetID[" + petId + "] TRY");
        			int consumeHonor = time * CONSUME_HONOR_OF_HOUR;
        			if (consumeHonor > player.getCredit()) {
        				sendMessage("您的荣誉不够，需要" + consumeHonor + "点荣誉。", command.getSerial(),
        						command.getSessionId());
        			} else {
        				try{
        					Pet pet = player.getPet(petId);
        					
        					//宠物脱掉装备
        					if (pet != null) {
        						Changed changed = new Changed();
        						for (int i = 0; i < pet.getUsedEquipments().length ; i++) {
        							if (pet.getUsedEquipments()[i] != null) {
        								IEquipment e = (IEquipment)pet.getUsedEquipments()[i].item;
        								if (player.completeAddItem(e, e.getId(), changed, player.getClientDataVersion()) == null) {
        									if (changed != null){
        										connectService.sendGetItem(changed, player.getId(), (byte) 26);
        									}
        									sendMessage(player.getId(),"操作失败。背包满，宠物装备无法卸下。");
        									UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
        									seg.writeShort(ClientConstants.EXTEND_PROTOCOL_PETEQU_LOGIN);
        									seg.writeInt(pet.getId());
        									Grid[] usedEquipmentsTemp = pet.getUsedEquipments();
        									if (usedEquipmentsTemp != null) {
        										for (int jj = 0; jj < pet.getUsedEquipmentinfo().length; jj++) {
        											seg.write((byte) pet.getUsedEquipmentinfo()[jj]);
        											if (usedEquipmentsTemp[jj] != null) {
        												if (pet.getUsedEquipmentinfo()[jj] == 1) {
        													IEquipment equtmp = (IEquipment) usedEquipmentsTemp[jj].item;
        													equtmp.setDataVersion(player.getClientDataVersion());
        													seg.write(equtmp.toClientBytesWithLevel(pet.getLevel()));
        												}
        											}
        										}
        									} else {
        										for (int jj = 0; jj < pet.getUsedEquipmentinfo().length; jj++) {
        											seg.write((byte) pet.getUsedEquipmentinfo()[jj]);
        										}
        									}
        									// 发送宠物升级所需升级经验
        									seg.writeInt(Utils.getPetUpLevelExp(pet.getLevel()));
        									
        									//发送宠物阵营宝石效果
        			                		CampData campData = getCampMainService().getCampData(player.getCamp());
        			                		int value = 0;
        			                		if(campData != null){
        			            		    	List<CampSkillData> list = campData.getSkillDataList();
        			            		    	for(int a = 0; a < list.size(); a++){
        			            		    		CampSkillData temp = (CampSkillData) list.get(a);
        			            		    		CampSkillLevel temp1 = CampConfig.campSkills.get(temp.getEffect()).getLevel(temp.getLevel());
        			            		    		
        			            		    		if(temp1 == null || temp1.getParm1() == 0){
        			            		    			continue;
        			            		    		}else{
        			            		    			if(temp.getEffect() == Buf.CAMP_STONE){//阵营科技中只有阵营宝石buff取值下发
        			            		    				value = temp1.getParm1();
        			            		    				break;
        			            		    			}
        			            		    		}
        			            		    	}
        			            		    }
        			                		seg.writeInt(value);
        									
        									write(seg, player.getId());
        									return;
        								}
        								e = null;
        								pet.setUsedEquipmentsinfo(i, (byte) 0);
        								pet.setUsedEquipments(i, null);
        							}
        						}
        						if (petmanagerService.checkPet(pet.getId(), player.getId())) {
        							petmanagerService.deletePet(pet.getId(), player.getId());
        							Utils.log(log, player.getId(), command.getAppType(),
        									"PetPractice Error and Delete data of Database! Database and the playerId["
        									+ player.getId() + "] have the same petId[" + pet.getId() + "]");
        						}
        						Petmanager petmanager = new Petmanager();
        						petmanager.setpetdata(pet);
        						petmanager.setPlayerId(player.getId());
        						// petversion >= 4开始修炼的时间
        						petmanager.setEattime(new Date());
        						// petversion >= 4需要修炼的时间
        						petmanager.setStone(time);
        						// 修炼持续的时间
        						petmanager.setPracticeTime(0);
        						
        						petmanagerService.addPet(petmanager);
        						player.addCredit(-consumeHonor, changed);
        						player.removePet(pet);
        						changed.addItem(pet, -1);
        						sendGetItem(changed, command.getSerial(), command.getSessionId(),
        								(byte) 17);
        						Utils.log(log, player.getId(), command.getAppType(),
        								"PetPractice Pet[" + Utils.getHexdump(pet.toClientBytesWithLevel(-1)) +
        						"]");
        						log.info("ID[" + player.getId() + "] PetPractice PetID[" + pet.getId() + "] PracticeTime[" + time + "] SUCCESS");
        					}
        				} catch (Exception ex) {
        					log.error("宠物重铸大师信息错误。", ex);
        				}
        			}
        		}
        	}
        }
    }
    
    // 带走正在修炼的宠物
    class PetPracticeOutProcessor implements CommandProcessor {
        public void process(WorldPlayer player, Command command) throws Exception {
        	if (player != null) {
        		synchronized (player) {
        			int petId = Integer.parseInt(command.getParam(0));
        			try{
        				if (petId == -1) {
        					//强行取走
        					petId = Integer.parseInt(command.getParam(1));
        					Petmanager petmanager = petmanagerService.getPet(petId);
        					if (petmanager != null) {
        						Pet pet = Pet.getPetFromDb(petmanager.getPet());
        						if (pet != null){
        							if (player.getPetCount() >= player.getPetSize()) {
        								sendMessage("宠物栏满了哦，不能领走他了。", command.getSerial(),
        										command.getSessionId());
        							} else {
        								if (player.getPet(pet.getId()) == null) {
        									Changed changed = new Changed();
        									player.addPet(pet, changed);
        									Utils.log(log, player.getId(), command.getAppType(),
        											"PetPracticeOut Pet[" + Utils.getHexdump(pet.toClientBytesWithLevel(-1)) + "]");
        									petmanagerService.leavepet(petmanager);
        									sendGetItem(changed, command.getSerial(), command.getSessionId(),
        											(byte) 17);
        									UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
        									seg.writeShort(ClientConstants.EXTEND_PROTOCOL_PETEQU_LOGIN);
        									seg.writeInt(pet.getId());
        									Grid[] usedEquipmentsTemp = pet.getUsedEquipments();
        									if (usedEquipmentsTemp != null) {
        										for (int j = 0; j < pet.getUsedEquipmentinfo().length; j++) {
        											seg.write((byte) pet.getUsedEquipmentinfo()[j]);
        											if (usedEquipmentsTemp[j] != null){
        			                					if (pet.getUsedEquipmentinfo()[j] == 1){
        			                						IEquipment equtmp = (IEquipment) usedEquipmentsTemp[j].item;
        			                						equtmp.setDataVersion(player.getClientDataVersion());
        			                						seg.write(equtmp.toClientBytesWithLevel(pet.getLevel()));
        			                					}
        			                				}
        										}
        										seg.writeInt(Utils.getPetUpLevelExp(pet.getLevel()));
        										
        										//发送宠物阵营宝石效果
        				                		CampData campData = getCampMainService().getCampData(player.getCamp());
        				                		int value = 0;
        				                		if(campData != null){
        				            		    	List<CampSkillData> list = campData.getSkillDataList();
        				            		    	for(int a = 0; a < list.size(); a++){
        				            		    		CampSkillData temp = (CampSkillData) list.get(a);
        				            		    		CampSkillLevel temp1 = CampConfig.campSkills.get(temp.getEffect()).getLevel(temp.getLevel());
        				            		    		
        				            		    		if(temp1 == null || temp1.getParm1() == 0){
        				            		    			continue;
        				            		    		}else{
        				            		    			if(temp.getEffect() == Buf.CAMP_STONE){//阵营科技中只有阵营宝石buff取值下发
        				            		    				value = temp1.getParm1();
        				            		    				break;
        				            		    			}
        				            		    		}
        				            		    	}
        				            		    }
        				                		seg.writeInt(value);
        										
        										connectService.writeTo(seg, player.getId());
        									} else {
        										sendMessage(player.getId(), "数据库错误。");
        									}
        									log.info("ID[" + player.getId() + "] getPetPractice PetID[" + pet.getId() + "] RealPracticeTime Less Than 1 hour. Do not hava any changed.");
        								} else {
        									if (petmanagerService.checkPet(pet.getId(), player.getId())) {
        										petmanagerService.deletePet(pet.getId(), player.getId());
        										Utils.log(log, player.getId(), command.getAppType(),
        												"PetPracticeOut Error and Delete data of Database! Database and the playerId["
        												+ player.getId() + "] have the same pet[" + Utils.getHexdump(pet.toClientBytesWithLevel(-1)) + "]");
        									}
        								}
        							}
        						}
        					}
        				} else {
        					Petmanager[] petmanager = petmanagerService.getPetData(petId);
        					if (petmanager.length > 0) {
        						for (int i = 1; i < petmanager.length; i++) {
        							petmanagerService.deletePetmanager(petmanager[i].getId());
        						}
        						Pet pet = Pet.getPetFromDb(petmanager[0].getPet());
        						if (pet != null) {
        							// 首先检查修炼的时间与玩家上线的时间
        							long playerLoginTime = player.getLastLoginTime().getTime();
        							long petBeganPracticeTime = petmanager[0].getEattime().getTime();
        							long practiceTime;
        							if (petBeganPracticeTime >= playerLoginTime) {
        								practiceTime = (new Date ()).getTime() - petBeganPracticeTime;
        							} else {
        								practiceTime = petmanager[0].getPracticeTime();
        								long playerGameTime = (new Date ()).getTime() - playerLoginTime;
        								practiceTime += playerGameTime;
        							}
        							getPetPractice(practiceTime, pet, player, command, petmanager[0]);
        						}
        					}
        				} 			
        			} catch (Exception ex) {
        				log.error("宠物修炼领取信息错误。", ex);
        			}
        		}
        	}
        }
        
        private void getPetPractice (long practiceTime, Pet pet, WorldPlayer player, Command command, Petmanager petmanager) throws Exception {
        	if (practiceTime < Utils.PET_PRACTICE_TIME_REWARDED) {
				byte[] bytes = stageService.getTaskBytes((short) 31002,
                        new String[] {"您的宠物" + pet.getName() + "修炼还不到1小时哦，如果领走了就修炼失败了，损失惨重哦，你想好了吗？\n1.坚持领走\n2.还是算了吧",
                        "petPracticeOut -1 " +
                        pet.getId()});
                UWAPSegment seg = new UWAPSegment(ClientConstants.
                        GET_FILE_OK, command.getSerial(),
                        command.getSessionId());
                seg.writeShort((short) 31002);
                seg.writeShort((short) 2);
                seg.write(bytes);
                write(seg);
			} else {
				int level  = pet.getLevel();
				if (player.getPetCount() >= player.getPetSize()) {
					sendMessage("宠物栏满了哦，不能领走他了。", command.getSerial(),
							command.getSessionId());
				} else {
					if (player.getPet(pet.getId()) == null) {
						if (practiceTime > petmanager.getStone() * Utils.PET_PRACTICE_TIME_REWARDED) {
							practiceTime = petmanager.getStone() * Utils.PET_PRACTICE_TIME_REWARDED;
						}
						
						int practiceHour = (int) (practiceTime / Utils.PET_PRACTICE_TIME_REWARDED);
						//int getPlayerExp = getPlayerExp(pet, player, practiceHour);
						int getPetExp = getPetExp(pet, practiceHour);
						int getPlayerMoney = getPlayerMoney(pet, player, practiceHour);
						int getPetPerceptionPoint = getPetPerceptionPoint(pet, practiceHour);
						
						Changed changed = new Changed();
//						if (player.getMaxLevel() > player.getLevel()) {
//							player.addExp(getPlayerExp, changed);
//						}
						if(pet.getLevel() < 100){
							player.tryAddPetExp(pet,getPetExp,null);
						}
						player.addMoney(getPlayerMoney, changed);
						player.addPet(pet, changed);
						int lastPerceptionLevel = pet.getPerceptionLevel();
						log.info("ID[" + player.getId() + "] getPetPractice PetID[" + pet.getId() + "] curPetPercetionPoint[" + pet.getPerceptionPoint() + "] PetPercetionLevel["
								+ pet.getPerceptionLevel() + "] RealPracticeTime[" + practiceHour + "] Add PetPerceptionPoint[" + getPetPerceptionPoint + "] TRY");
						String msg = "";
						if(getPetExp > 0){
							msg = getPetExp + "点经验";
						}
						if (pet.getPerceptionLevel() < Utils.PET_MAX_PERCEPTION_LEVEL) {
							if (player.addPetPerceptionPoint(pet.getId(), getPetPerceptionPoint, changed)) {
								player.setPetSkillAndEnhanceName(pet.getId(), lastPerceptionLevel, changed);
								if(msg.length() > 0){
									msg = msg + "、" + getPetPerceptionPoint + "点悟性。";
								}else{
									msg = getPetPerceptionPoint + "点悟性。";
								}
							}
						}
						if(msg.length()>0){
							sendMessage("您的宠物获得了" + msg, command.getSerial(),
									command.getSessionId());
						}
						Utils.log(log, player.getId(), command.getAppType(),
								"PetPracticeOut Pet[" + Utils.getHexdump(pet.toClientBytesWithLevel(-1)) +
						"]");
						petmanagerService.leavepet(petmanager);
						sendGetItem(changed, command.getSerial(), command.getSessionId(),
								(byte) 17);
						int updatalevel = pet.getLevel();
						if(updatalevel > level){
							sendMessage("您的宠物升级了，可以到宠物配点菜单中去分配属性点！",  command.getSerial(), command.getSessionId());
						}
						int noticLevel = Utils.judgePerceptionSendNotice(pet.getPerceptionLevel(), lastPerceptionLevel);
						if (noticLevel > 0) {
							chatService.sendWorldMessage(player.getId(), "系统", "恭喜“" + player.getPlayerName()
									+ "”将宠物“<c6A5ACD>" + pet.getName() + "</c>”的悟性提升到了"
									+ noticLevel + "星！" + Utils.petIdToProtocol(pet));
						}
						UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
						seg.writeShort(ClientConstants.EXTEND_PROTOCOL_PETEQU_LOGIN);
						seg.writeInt(pet.getId());
						Grid[] usedEquipmentsTemp = pet.getUsedEquipments();
						if (usedEquipmentsTemp != null) {
							for (int j = 0; j < pet.getUsedEquipmentinfo().length; j++) {
								seg.write((byte) pet.getUsedEquipmentinfo()[j]);
                				if (usedEquipmentsTemp[j] != null){
                					if (pet.getUsedEquipmentinfo()[j] == 1){
                						IEquipment equtmp = (IEquipment) usedEquipmentsTemp[j].item;
                						equtmp.setDataVersion(player.getClientDataVersion());
                						seg.write(equtmp.toClientBytesWithLevel(pet.getLevel()));
                					}
                				}
							}
							seg.writeInt(Utils.getPetUpLevelExp(pet.getLevel()));
							
							//发送宠物阵营宝石效果
	                		CampData campData = getCampMainService().getCampData(player.getCamp());
	                		int value = 0;
	                		if(campData != null){
	            		    	List<CampSkillData> list = campData.getSkillDataList();
	            		    	for(int a = 0; a < list.size(); a++){
	            		    		CampSkillData temp = (CampSkillData) list.get(a);
	            		    		CampSkillLevel temp1 = CampConfig.campSkills.get(temp.getEffect()).getLevel(temp.getLevel());
	            		    		
	            		    		if(temp1 == null || temp1.getParm1() == 0){
	            		    			continue;
	            		    		}else{
	            		    			if(temp.getEffect() == Buf.CAMP_STONE){//阵营科技中只有阵营宝石buff取值下发
	            		    				value = temp1.getParm1();
	            		    				break;
	            		    			}
	            		    		}
	            		    	}
	            		    }
	                		seg.writeInt(value);
							
							connectService.writeTo(seg, player.getId());
						} else {
							sendMessage(player.getId(), "数据库错误。");
						}
						log.info("ID[" + player.getId() + "] getPetPractice PetID[" + pet.getId() + "] curPetPercetionPoint[" + pet.getPerceptionPoint() + "] PetPercetionLevel["
								+ pet.getPerceptionLevel() + "] RealPracticeTime["+ practiceHour + "] Add PetPerceptionPoint[" + getPetPerceptionPoint + "] SUCCESS");
					} else {
						if (petmanagerService.checkPet(pet.getId(), player.getId())) {
							petmanagerService.deletePet(pet.getId(), player.getId());
							Utils.log(log, player.getId(), command.getAppType(),
									"PetPracticeOut Error and Delete data of Database! Database and the playerId["
									+ player.getId() + "] have the same petId[" + pet.getId() + "]");
						}
					}
				}
			}
        }
        
        // 宠物修炼人物获得的经验
        private int getPlayerExp (Pet pet, WorldPlayer player, int time) {
    		return ((pet.getPerceptionLevel() * pet.getPerceptionLevel() * 5 + 100) * player.getLevel() + 100) * time;
    	}
        
        //宠物修炼宠物获得的经验   
        private int getPetExp(Pet pet, int time){
        	return pet.getPerceptionLevel() * 2 * time;
        }
        
        // 宠物修炼人物获得J币
    	private int getPlayerMoney (Pet pet, WorldPlayer player, int time) {
    		return (pet.getPerceptionLevel() * pet.getPerceptionLevel() * 150 + 100 + pet.getLevel() * 50) * time;
    	}
    	// 宠物修炼人物获得的悟性点
    	private int getPetPerceptionPoint (Pet pet, int time) {
    		return (pet.getPerceptionLevel() * pet.getPerceptionLevel() + 10 - pet.getPerceptionLevel()) * time;
    	}
    }

    // 宠物属性重铸
    class PetRecastProcessor implements CommandProcessor {
    	public void process (WorldPlayer player, Command command) throws Exception {
    		if (player != null) {
    			synchronized (player) {
    				int petId = Integer.parseInt(command.getParam(0));
    				try{
    					Pet pet = player.getPet(petId);
    					if (pet != null) {
    						String [] capacityList = new String[] {"力量1点", "敏捷1点", "体力1点", "智力1点", "力量10点", "敏捷10点", "体力10点", "智力10点"};
    						String titlelist = "你要重铸什么属性？";
    						String [] questions = new String[capacityList.length + 3];
    						for (int i = 0; i < capacityList.length; i++) {
    							titlelist += "\n" + String.valueOf(i + 1) + "." + capacityList[i];
    							questions[3 + i] = "petRecastingProperty " + petId + " " + capacityList[i];
    						}
    						titlelist += "\n" + String.valueOf(capacityList.length + 1) + ".算了，先不重铸了。";
    						questions[0] = String.valueOf(capacityList.length + 1);
    						questions[1] = "1";
    						questions[2] = titlelist;
    						byte[] bytes = stageService.getTaskBytes((short) 31010, questions);
    						UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK,
    								command.getSerial(),
    								command.getSessionId());
    						seg.writeShort((short) 31010);
    						seg.writeShort((short) 2);
    						seg.write(bytes);
    						write(seg);
    					}
    				} catch (Exception ex) {
    					log.error("宠物重铸大师信息错误。", ex);
    				}
    			}
    		}
    	}
    }
    
    // 宠物属性重铸
    class PetRecastingPropertyProcessor implements CommandProcessor {
    	public void process (WorldPlayer player, Command command) throws Exception {
    		if (player != null) {
    			synchronized (player) {
    				int petId = Integer.parseInt(command.getParam(0));
    				String capacity = command.getParam(1);
    				try{
    					Pet pet = player.getPet(petId);
    					if (pet != null){
    						log.info("ID[" + player.getId() + "] PetRecastingProperty PetID[" + pet.getId() + "] strength["
    								+ pet.getStrength() + "] agility[" + pet.getAgility() + "] vitality["
    								+ pet.getVitality() + "] intelligence[" + pet.getIntelligence() + "] currentPoint["
    								+ pet.getCurrentPoint() + "] TRY");
    						int[] recastingPoints = new int [4];
    						int minPoint = pet.getPropertyPoints() * 7 / 100;
    						if (minPoint <= 0) {
    							sendMessage("宠物属性点错误。", command.getSerial(),
    									command.getSessionId());
    						} else {
    							if (capacity.equals("力量1点")) {
    								recastingPoints[0] = 1;
    							} else if (capacity.equals("敏捷1点")) {
    								recastingPoints[1] = 1;
    							} else if (capacity.equals("体力1点")) {
    								recastingPoints[2] = 1;
    							} else if (capacity.equals("智力1点")) {
    								recastingPoints[3] = 1;
    							} else if (capacity.equals("力量10点")) {
    								recastingPoints[0] = 10;
    							} else if (capacity.equals("敏捷10点")) {
    								recastingPoints[1] = 10;
    							} else if (capacity.equals("体力10点")) {
    								recastingPoints[2] = 10;
    							} else if (capacity.equals("智力10点")) {
    								recastingPoints[3] = 10;
    							} else {
    								sendMessage("属性点选择错误。", command.getSerial(),
    										command.getSessionId());
    							}
    							int allPoint = 0;
    							for (int i = 0; i < recastingPoints.length; i++) {
    								allPoint += recastingPoints[i];
    							}
    							int money = allPoint * 10000;
    							int strength = pet.getStrength() + pet.getEnhancestrength() - recastingPoints[0];
    							int agility = pet.getAgility() + pet.getEnhanceagility() - recastingPoints[1];
    							int vitality = pet.getVitality() + pet.getEnhancevitality() - recastingPoints[2];
    							int intelligence = pet.getIntelligence() + pet.getEnhanceintelligence() - recastingPoints[3];
    							if (player.getMoeny() < money) {
    								sendMessage("需要<cFF0000>" + money + "J</c>，您没有这么多J币。", command.getSerial(),
    										command.getSessionId());
    							} else if ((recastingPoints[2] != 0 && vitality <= minPoint) ||
    									(recastingPoints[1] != 0 && agility <= minPoint) ||
    									(recastingPoints[0] != 0 && strength <= minPoint) ||
    									(recastingPoints[3] != 0 && intelligence <= minPoint)) {
    								sendMessage("此属性不能小于等于总属性的7％。", command.getSerial(),
    										command.getSessionId());
    							} else {
    								pet.setStrength(pet.getStrength() - recastingPoints[0]);
    								pet.setAgility(pet.getAgility() - recastingPoints[1]);
    								pet.setVitality(pet.getVitality() - recastingPoints[2]);
    								pet.setIntelligence(pet.getIntelligence() - recastingPoints[3]);
    								pet.setCurrentPoint(pet.getCurrentPoint() + allPoint);
    								Changed changed = new Changed();
    								player.setMoeny(player.getMoeny() - money);
    								changed.addProperty(Changed.MONEY, - money);
    								changed.addPetProperty(pet, Changed.PET_STRENGTH, - recastingPoints[0]);
    								changed.addPetProperty(pet, Changed.PET_AGILITY, - recastingPoints[1]);
    								changed.addPetProperty(pet, Changed.PET_VITALITY, - recastingPoints[2]);
    								changed.addPetProperty(pet, Changed.PET_INTELLIGENCE, - recastingPoints[3]);
    								changed.addPetProperty(pet, Changed.PET_CURRENTPOINT, + allPoint);
    								sendMessage("恭喜您重铸宠物的" + capacity, command.getSerial(), command.getSessionId());
    								sendGetItem(changed, command.getSerial(), command.getSessionId(),
    										(byte) 20);
    								log.info("ID[" + player.getId() + "] PetRecastingProperty PetID[" + pet.getId() + "] strength["
    										+ pet.getStrength() + "] agility[" + pet.getAgility() + "] vitality["
    										+ pet.getVitality() + "] intelligence[" + pet.getIntelligence() + "] currentPoint["
    										+ pet.getCurrentPoint() + "] SUCCESS");
    							}
    						}
    					} else {
    						sendMessage("不存在这个宠物。", command.getSerial(),
    								command.getSessionId());
    					}
    				} catch (Exception ex) {
    					log.error("宠物重铸大师信息错误。", ex);
    				}
    			}
    		}
    	}
    }
    
    // 通知客户端打开UI
    class OpenUIProcessor implements CommandProcessor {
    	public void process (WorldPlayer player, Command command) throws Exception {
    		String uiName = command.getParam(0);
    		if (player != null) {
    			synchronized (player) {
    				UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL, command.getSerial(), command.getSessionId());
					seg.writeShort(ClientConstants.EXTEND_OPEN_UI);
					seg.writeString(uiName);
					int count = command.getParamCount() - 1;
					seg.writeInt(count);
					if(count > 0){
						for(int i=0; i<count; i++){
							seg.writeString(command.getParam(i + 1));
						}
					}
					write(seg);
				}
    		}
    	}
    }
    class GetTaskProcessor implements CommandProcessor {
    	public void process (WorldPlayer player, Command command) throws Exception {
    		int touchNpcCamp = Integer.parseInt(command.getParam(0));
    		int taskNpcID = Integer.parseInt(command.getParam(1));
    		if (player != null) {
    			synchronized (player) {
    				int camp = player.getCamp();
    				if(camp != 1 && camp != 2){
    					sendMessage("您没有阵营。", command.getSerial(), command.getSessionId());
    					return;
    				}
    				if(camp != touchNpcCamp){
    					sendMessage("您不是" + (camp == 2 ? "黑暗" : "光明") + "的人。", command.getSerial(), command.getSessionId());
    					return;
    				}
    				if(player.getLevel() < 20){
                		sendMessage("您的等级不够，请提升后再来。", command.getSerial(), command.getSessionId());
                	}else{
                		CampData campData = campMainService.getCampData(camp);
                		CampOfficial financial = (CampOfficial)campData.getOfficial().get(new Integer(CampOfficial.POST_FINANCIAL));
                		CampOfficial duke = (CampOfficial)campData.getOfficial().get(new Integer(CampOfficial.POST_DUKE));
                		int getFinancialTask = 1;
                		int getDukeTask = 1;
                		long now = System.currentTimeMillis();
                		long todayStart = Utils.getTodayStart();
//                		if(financial != null){	 	zjl modify 把内政任务限制去掉
//	                		long taskStart = financial.getTaskTime();
//	                		//long taskEnd = taskStart + 2 * 60 * 60 * 1000L;
//	                		if(financial.getTaskTime() < todayStart){
//	                			getFinancialTask = 0;
//	                		}
////	                		else if(now < taskStart || now > taskEnd){
////	                			getFinancialTask = 2;
////	                		}
//                		}else{
//                			getFinancialTask = 0;
//                		}
//                		if(duke != null){
//	                		long taskStart = duke.getTaskTime();
//	                		//long taskEnd = taskStart + 2 * 60 * 60 * 1000L;
//	                		if(taskStart < todayStart){
//	                			getDukeTask = 0;
//	                		}
////	                		else if(now < taskStart || now > taskEnd){
////	                			getDukeTask = 2;
////	                		}
//                		}else{
//                			getDukeTask = 0;
//                		}
	                	byte[] bytes = stageService.getTaskBytes((short) 31055, new String[] {
	                            "" + taskNpcID, "" + camp, "" + getDukeTask, "" + getFinancialTask,
	                            Server.iMoneyType == Server.IMONEY_TYPE_PIP ? "1" : "0"});
	                    UWAPSegment seg = new UWAPSegment(ClientConstants.
	                            GET_FILE_OK, command.getSerial(), command.getSessionId());
	                    seg.writeShort((short) 31055);
	                    seg.writeShort((short) 2);
	                    seg.write(bytes);
	                    write(seg);
                	}
    			}
    		}
    	}
    }
    
    
    
    
    class BattlefieldResourcesProcessor implements CommandProcessor {
    	public void process (WorldPlayer player, Command command) throws Exception {
    		byte actionType = Byte.parseByte(command.getParam(0));
    		synchronized (player) {
    			int campType = player.getCamp();
    			GameMap map = player.getMap();
    			if (map != null) {
    				Instance instance = map.getInstance();
    				if (instance != null) {
    					int battlefieldID = instance.getId();
    					
	    				CampBattlefieldInstance battlefieldInstance = campBattlefieldService.getPlayerInstance(player, battlefieldID);
	    				if (battlefieldInstance != null) {
	    					CampBattlefieldPlayer selfcbp = battlefieldInstance.getBattlefieldPlayer(player.getId());
	    					if(selfcbp != null && selfcbp.getCampTeam() != Utils.NO_CAMP){
	    						campType = selfcbp.getCampTeam();
	    					}
	    					switch (actionType) {
	    					case CampBattlefieldConfig.ACTION_CONTRIBUTE_RESOURCES:
	    						int itemID = battlefieldInstance.getDefinition().getCompetingGoodsID();
	    						if (itemID > 0) {
	    							int count = player.getItemCount(itemID);
	    							if (count > 0) {
	    								Changed changed = new Changed();
	    								IItem item = Items.getTemplate(itemID).newInstance();
	    								if (player.completeRemoveItem(item, count, changed) != null) {
	    									battlefieldInstance.addItemCount(campType, count);
	    									sendMessage("很不错，照这个速度我们能赢！", command.getSerial(), command.getSessionId());
	    								} else {
	    									sendMessage("物品扣除失败", command.getSerial(), command.getSessionId());
	    								}
	    								sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 22);
	    							} else {
	    								sendMessage("别在这傻站着，快去收集更多的资源！", command.getSerial(), command.getSessionId());
	    							}
	    						}
	    						break;
	    					case CampBattlefieldConfig.ACTION_VIEW_VICTORIES:
	    						int count = battlefieldInstance.getItemCount(campType);
	    						sendMessage("我们目前已经征收到了" + count + "，不知道对方情况怎么样了...", command.getSerial(), command.getSessionId());
	    						break;
	    					case CampBattlefieldConfig.ACTION_EXIT_BATTLEFIELD:
	    						byte[] bytes = stageService.getTaskBytes((short) 31010, new String[] {
	                                    "2", "1", "逃离战场副本需要扣除你当前等级的荣誉，确认退出吗？" +
	                                    			"\n1.是的，我要当逃兵" +
	                                    			"\n2.不，为了荣誉我要奋战到死",
	                                    "exitCampBattlefield " + CampBattlefieldConfig.ACTION_EXIT_BATTLEFIELD + " " + battlefieldID,
	                                    "ok"});
	                            UWAPSegment seg = new UWAPSegment(ClientConstants.
	                                    GET_FILE_OK, command.getSerial(), command.getSessionId());
	                            seg.writeShort((short) 31010);
	                            seg.writeShort((short) 2);
	                            seg.write(bytes);
	                            write(seg);
	                            break;
	    					case CampBattlefieldConfig.ACTION_VIEW_RULES:
	    						String rules = instance.getDefinition().getRules();
	    						sendMessage(rules, command.getSerial(), command.getSessionId());
	    						break;
	    					}
	    				} else {
	    					sendMessage("你不在战场中无法执行。", command.getSerial(), command.getSessionId());
	    				}
    				}
    			}
			}
    	}
    }
    
    class exitBattlefieldProcessor implements CommandProcessor {
    	public void process (WorldPlayer player, Command command) throws Exception {
    		int actionType = Integer.parseInt(command.getParam(0));
    		int battlefieldID = Integer.parseInt(command.getParam(1));
    		if (actionType == CampBattlefieldConfig.ACTION_EXIT_BATTLEFIELD) {
				CampBattlefieldInstance instance = campBattlefieldService.getPlayerInstance(player, battlefieldID);
				if (instance != null) {
					campBattlefieldService.getInstance(battlefieldID).exitCampBattlefield(instance, player.getId(), System.currentTimeMillis());
				}
    		}
    	}
    }
    
    class PieceReplaceDiamond implements CommandProcessor{

		public void process(WorldPlayer player, Command command)
				throws Exception {
			int replaceType = Integer.parseInt(command.getParam(0));
			int piece = 201530;//宝石碎块id
	    	int fourneedcount = 14;//四级需要碎块数
	    	int fiveneedcount = 70;//五级
	    	long start = Utils.getTodayStart();
	    	long time = 60 * 60 * 24 * 14 * 1000L;//14天
	    	long end = start + time;
	    	long useTime = player.getPieceReplaceTime().getTime();
			if(replaceType == 1){//兑换四级
				int itemid = 200883;	//四级定向包
				if(!player.hasItem(piece)){
					sendMessage("您还没有宝石碎块", command.getSerial(), command.getSessionId());
					return;
				}
				if(player.getItemCount(piece) < fourneedcount){
					sendMessage("您兑换的数量不足,兑换需要14个宝石碎块", command.getSerial(), command.getSessionId());
					return;
				}
				if(useTime < end){//活动期间内
					Changed changed = new Changed();
					int playercount = player.getItemCount(piece);//玩家背包内碎块数量
					int percent = playercount / fourneedcount;	//能兑换多少个定向包
					int delcount = percent * fourneedcount;
					if(percent >= 1){
						synchronized(player){
							if(player.hasItem(itemid) && player.getItemCount(itemid) < 99){
								player.completeRemoveItem(piece, delcount, changed);//删碎块
	    						player.addItem(itemid, percent, changed,
		    							player.getClientDataVersion());
							}else if(!player.isFull() && !player.hasItem(itemid)){
								player.completeRemoveItem(piece, delcount, changed);//删碎块
	    						player.addItem(itemid, percent, changed,
		    							player.getClientDataVersion());
							}else{
		    					IItem iit = Items.getTemplate(itemid).newInstance();
		    					byte[] att = ItemUtils.item2dbAttachment(iit,1);
		    					mailService.sendMail(player.getId(),
		    							player.getPlayerName(), -1, "系统",
		    							iit.getName() + "*" + percent, "宝石碎块兑换", att, 0,
		    							true);
		    					player.completeRemoveItem(piece, delcount, changed);//删碎块
		    					sendMessage(player.getId(),
		    					"你的背包满了，已经把物品邮寄到您的邮箱!已经扣除需要交换的物品");
		    				}
							if(changed != null){
	    						sendGetItem(changed, command.getSerial(), command.getSessionId(), command.getAppType());
		    					sendMessage(player.getId(), "兑换成功!已经扣除需要交换的物品");
		    					log.info("PieceReplace ID[" + player.getId() + "] giveitemid[" + itemid + "] removeItemID[" 
		    							+ piece + "] removeItemcount[" + delcount +"] givecount[" + percent +"]");
							}
						
						}
					}
				}else{
					sendMessage("活动已经结束", command.getSerial(), command.getSessionId());
				}
			}else if(replaceType == 2){//兑换五级
				int itemid = 201532;//五级定向包
				if(!player.hasItem(piece)){
					sendMessage("您还没有宝石碎块", command.getSerial(), command.getSessionId());
					return;
				}
				if(player.getItemCount(piece) < fiveneedcount){
					sendMessage("您兑换的数量不足,兑换需要70个宝石碎块", command.getSerial(), command.getSessionId());
					return;
				}
				if(useTime < end){//活动期间内
					Changed changed = new Changed();
					int playercount = player.getItemCount(piece);//玩家背包内碎块数量
					int percent = playercount / fiveneedcount;	//能兑换多少个定向包
					int delcount = percent * fiveneedcount;
					if(percent >= 1){
						synchronized(player){
							if(player.hasItem(itemid) && player.getItemCount(itemid) < 99){
								player.completeRemoveItem(piece, delcount, changed);//删碎块
	    						player.addItem(itemid, percent, changed,
		    							player.getClientDataVersion());
							}else if(!player.isFull() && !player.hasItem(itemid)){
								player.completeRemoveItem(piece, delcount, changed);//删碎块
	    						player.addItem(itemid, percent, changed,
		    							player.getClientDataVersion());
							}else{
		    					IItem iit = Items.getTemplate(itemid).newInstance();
		    					byte[] att = ItemUtils.item2dbAttachment(iit,1);
		    					mailService.sendMail(player.getId(),
		    							player.getPlayerName(), -1, "系统",
		    							iit.getName() + "*" + percent, "宝石碎块兑换", att, 0,
		    							true);
		    					player.completeRemoveItem(piece, delcount, changed);//删碎块
		    					sendMessage(player.getId(),
		    					"你的背包满了，已经把物品邮寄到您的邮箱!已经扣除需要交换的物品");
		    				}
							if(changed != null){
	    						sendGetItem(changed, command.getSerial(), command.getSessionId(), command.getAppType());
		    					sendMessage(player.getId(), "兑换成功!已经扣除需要交换的物品");
		    					log.info("PieceReplace ID[" + player.getId() + "] giveitemid[" + itemid + "] removeItemID[" 
		    							+ piece + "] removeItemcount[" + delcount +"] givecount[" + percent +"]");
	    					}
						
						}
					}
					
				}else{
					sendMessage("活动已经结束", command.getSerial(), command.getSessionId());
				}
			}else if(replaceType == 3){//兑换一个
				int itemid = 200883;	//四级定向包
				if(!player.hasItem(piece)){
					sendMessage("您还没有宝石碎块", command.getSerial(), command.getSessionId());
					return;
				}
				if(player.getItemCount(piece) < fourneedcount){
					sendMessage("您兑换的数量不足,兑换需要14个宝石碎块", command.getSerial(), command.getSessionId());
					return;
				}
				if(useTime < end){
					Changed changed = new Changed();
					synchronized(player){
						if(player.hasItem(itemid) && player.getItemCount(itemid) < 99){
							player.completeRemoveItem(piece, fourneedcount, changed);//删碎块
    						player.addItem(itemid, 1, changed,
	    							player.getClientDataVersion());
						}else if(!player.isFull() && !player.hasItem(itemid)){
							player.completeRemoveItem(piece, fourneedcount, changed);//删碎块
    						player.addItem(itemid, 1, changed,
	    							player.getClientDataVersion());
						}else{
	    					IItem iit = Items.getTemplate(itemid).newInstance();
	    					byte[] att = ItemUtils.item2dbAttachment(iit,1);
	    					mailService.sendMail(player.getId(),
	    							player.getPlayerName(), -1, "系统",
	    							iit.getName() + "*" + 1, "宝石碎块兑换", att, 0,
	    							true);
	    					player.completeRemoveItem(piece, fourneedcount, changed);//删碎块
	    					sendMessage(player.getId(),
	    					"你的背包满了，已经把物品邮寄到您的邮箱!已经扣除需要交换的物品");
	    				}
						if(changed != null){
    						sendGetItem(changed, command.getSerial(), command.getSessionId(), command.getAppType());
	    					sendMessage(player.getId(), "兑换成功!已经扣除需要交换的物品");
	    					log.info("PieceReplace ID[" + player.getId() + "] giveitemid[" + itemid + "] removeItemID[" 
	    							+ piece + "] removeItemcount[" + fourneedcount +"] givecount[" + 1 +"]");
						}
					}
				}else{
					sendMessage("活动已经结束", command.getSerial(), command.getSessionId());
				}
			}else if(replaceType == 4){//兑换1个
				int itemid = 201532;//五级定向包
				if(!player.hasItem(piece)){
					sendMessage("您还没有宝石碎块", command.getSerial(), command.getSessionId());
					return;
				}
				if(player.getItemCount(piece) < fiveneedcount){
					sendMessage("您兑换的数量不足,兑换需要70个宝石碎块", command.getSerial(), command.getSessionId());
					return;
				}
				if(useTime < end){
					Changed changed = new Changed();
					synchronized(player){
						if(player.hasItem(itemid) && player.getItemCount(itemid) < 99){
							player.completeRemoveItem(piece, fiveneedcount, changed);//删碎块
    						player.addItem(itemid, 1, changed,
	    							player.getClientDataVersion());
						}else if(!player.isFull() && !player.hasItem(itemid)){
							player.completeRemoveItem(piece, fiveneedcount, changed);//删碎块
    						player.addItem(itemid, 1, changed,
	    							player.getClientDataVersion());
						}else{
	    					IItem iit = Items.getTemplate(itemid).newInstance();
	    					byte[] att = ItemUtils.item2dbAttachment(iit,1);
	    					mailService.sendMail(player.getId(),
	    							player.getPlayerName(), -1, "系统",
	    							iit.getName() + "*" + 1, "宝石碎块兑换", att, 0,
	    							true);
	    					player.completeRemoveItem(piece, fiveneedcount, changed);//删碎块
	    					sendMessage(player.getId(),
	    					"你的背包满了，已经把物品邮寄到您的邮箱!已经扣除需要交换的物品");
	    				}
						if(changed != null){
    						sendGetItem(changed, command.getSerial(), command.getSessionId(), command.getAppType());
	    					sendMessage(player.getId(), "兑换成功!已经扣除需要交换的物品");
	    					log.info("PieceReplace ID[" + player.getId() + "] giveitemid[" + itemid + "] removeItemID[" 
	    							+ piece + "] removeItemcount[" + fiveneedcount +"] givecount[" + 1 +"]");
    					}
					}
				}else{
					sendMessage("活动已经结束", command.getSerial(), command.getSessionId());
				}
			
			}
		}
    	
    }
    
    
    class ChristmasWishingProcessor implements CommandProcessor{
		public void process(WorldPlayer player, Command command)
				throws Exception {
			int wishingType = Integer.parseInt(command.getParam(0));
			if(player!=null){
//				if(player.getLevel()< player.getMaxLevel()){
//	        		sendMessage("只有100级的玩家才可以敲钟，你的等级不够，加油练级哦！", command.getSerial(), command.getSessionId());
//	        	}else{
				if(wishingType == 1){
					throw new ITimesException("物品出错！",command.getSerial(),command.getSessionId(),command.getAppType());
				}
					switch(wishingType){
					case 1://普通许愿
						log.info("ID[" + player.getId() + "] ChristmasWishing wishingType[" + wishingType + "] wishingCount[" + player.getChristmasWishingNormal_Count() +"] try");
						if(player.addChristmasWishingNormalCount()){
							Changed dropchanged = new Changed();
				    		int dropGroupId = 438;	//普通许愿掉落组（敲钟一次）
				    		DropGroup group = DropGroups.getDropGroup(dropGroupId, player.getLevel());
				    		if(group == null){
				    			throw new ITimesException("物品出错！",command.getSerial(),command.getSessionId(),command.getAppType());
				    		}
				    		int rate = rnd.nextInt(group.getRate());
				    		DropItem dropItem = group.calcDropItem(rate);
				    		int dropcount = getCount(rnd, dropItem.getMin(), dropItem.getMax());
				    		if(player.isFull() || player.getItemCount(dropItem.getItem().getItemId()) + dropcount > 99){
				    			byte[] att = ItemUtils.item2dbAttachment(dropItem.getItem().newInstance(), dropcount);
				    			mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", dropItem.getItem().getName() + "*" + dropcount, "许愿礼品", att, 0, true);
				    			if(player.getItemCount(dropItem.getItem().getItemId()) + dropcount > 99){
				    				sendMessage("您的物品个数已达上限，奖励物品已经发送到邮箱，请及时查收。", command.getSerial(), command.getSessionId());
				    			}else{
				    				sendMessage("您的背包已满，奖励物品已经发送到邮箱，请及时查收。", command.getSerial(), command.getSessionId());
				    			}
				    		}else{
				    			player.addItem(dropItem.getItem(), dropcount, dropchanged, player.getClientDataVersion());
				    		}
				    		String tmpStr = "许愿成功。";
				    		int tmpCount = player.checkChristmasWishingNormalCount();
				    		if(tmpCount>0){
				    			tmpStr = tmpStr + "今日的许愿次数还剩" + tmpCount + "次。";
				    		}else{
				    			tmpStr = "您今日的普通许愿次数已经用完，可以明日再来或者进行文艺许愿";
				    		}
				    		sendMessage(tmpStr , command.getSerial(), command.getSessionId());
				    		connectService.sendGetItem(dropchanged, player.getId(),(byte) 22);
				    		log.info("ID[" + player.getId() + "] ChristmasWishing wishingType[" + wishingType + "] wishingCount[" + player.getChristmasWishingNormal_Count() +"] getItemID[" + dropItem.getItem().getItemId() + "] success");
						}else{
							sendMessage("今日的敲钟次数已满，请明日再来！", command.getSerial(), command.getSessionId());
							log.info("ID[" + player.getId() + "] ChristmasWishing wishingType[" + wishingType + "] wishingCount[" + player.getChristmasWishingNormal_Count() +"] normal count is Max. failed!");
						}
						break;
					case 2://文艺许愿(敲钟10次)
						log.info("ID[" + player.getId() + "] ChristmasWishing wishingType[" + wishingType + "] try");
						int itemID = 201538;//祝福礼锤
						if(iShopHide(player, itemID, command.getSerial(), command.getSessionId(), 1, (byte)5, -1, 0) != ISHOP_OK){
							log.info("ID[" + player.getId() + "] ChristmasWishing wishingType[" + wishingType + "] iMoney is not enough. failed!");
							sendMessage("您的" + Server.iMoneyString + "不足，请充值后再敲钟", command.getSerial(), command.getSessionId());
						}else{
							log.info("ID[" + player.getId() + "] ChristmasWishing wishingType[" + wishingType + "] success!");
							//sendMessage("敲钟成功。", command.getSerial(), command.getSessionId());
						}
						break;
					case 3:// 文艺许愿二次确认
						int price = Integer.parseInt(command.getParam(1));
						byte[] bytes = stageService.getTaskBytes((short)31010, new String[] {
	                            "2", "1", "敲钟可以有几率得到悟性水晶簇哦，但是需要消耗" + price +Server.iMoneyString +" ，你确认要敲钟吗？\n1.是的，我要敲钟\n2.一会儿再说",
	                            "ChristmasWishing 2",
	                            "ok"});
	                	UWAPSegment seg = new UWAPSegment(ClientConstants.
	                            GET_FILE_OK, command.getSerial(), command.getSessionId());
	                    seg.writeShort((short) 31010);
	                    seg.writeShort((short) 2);
	                    seg.write(bytes);
	                    write(seg);
						break;
					case 4:// 敲钟10次
						price = Integer.parseInt(command.getParam(1));
						bytes = stageService.getTaskBytes((short)31010, new String[] {
	                            "2", "1", "敲钟可以有几率得到悟性水晶簇哦，但是需要消耗" + price +Server.iMoneyString +" ，你确认要敲10次钟吗？\n1.是的，我要敲钟\n2.一会儿再说",
	                            "ChristmasWishing 5",
	                            "ok"});
	                	seg = new UWAPSegment(ClientConstants.
	                            GET_FILE_OK, command.getSerial(), command.getSessionId());
	                    seg.writeShort((short) 31010);
	                    seg.writeShort((short) 2);
	                    seg.write(bytes);
	                    write(seg);
						break;
					case 5://文艺许愿(敲钟10次)
						log.info("ID[" + player.getId() + "] ChristmasWishing wishingType[" + wishingType + "] try");
						itemID = 201538;//祝福礼锤
						if(iShopHide(player, itemID, command.getSerial(), command.getSessionId(), 10, (byte)5, -1, 0) != ISHOP_OK){
							log.info("ID[" + player.getId() + "] ChristmasWishing wishingType[" + wishingType + "] iMoney is not enough. failed!");
							sendMessage("您的" + Server.iMoneyString + "不足，请充值后再敲钟", command.getSerial(), command.getSessionId());
						}else{
							log.info("ID[" + player.getId() + "] ChristmasWishing wishingType[" + wishingType + "] success!");
							//sendMessage("敲钟成功。", command.getSerial(), command.getSessionId());
						}
						break;	
					}
//	        	}
			}
		}
    	
    	
    }
    
    class NBShowProcessor implements CommandProcessor{
		public void process(WorldPlayer player, Command command)
				throws Exception {
			if(player != null && player.hasItem(201540)){
				int dropId = Integer.parseInt(command.getParam(0));
				int count = Integer.parseInt(command.getParam(1));
				DropGroup group = DropGroups.getDropGroup(dropId, player.getLevel());
				HashMap<Integer, Integer> items = new HashMap<Integer, Integer>();
				if (group != null) {
					for(int i=0; i<count; i++){
						int rate = rnd.nextInt(group.getRate());
						DropItem dropItem = group.calcDropItem(rate);
						int c1 = getCount(rnd, dropItem.getMin(), dropItem.getMax());
						if(items.containsKey(dropItem.getItem().getItemId())){
							int c = items.get(dropItem.getItem().getItemId());
							c += c1;
							items.put(dropItem.getItem().getItemId(), c);
						}else{
							items.put(dropItem.getItem().getItemId(), c1);
						}
					}
					UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
					seg.writeShort((short) 10267);
					seg.writeString("物品列表");
					seg.write((byte) 0);
					seg.writeShort((short)items.size());
					for(Integer item : items.keySet()){
						IItemTemplate t = Items.getTemplate(item);
						seg.writeInt(item);
						seg.writeString(t.getName() + " " + items.get(item).intValue());
						seg.writeInt(Utils.CLR_WHITE);
					}
					connectService.writeTo(seg, player.getId());
				}
			}
		}
    }
    
    class OpenSkyLoveProcessor implements CommandProcessor{

		@Override
		public void process(WorldPlayer player, Command command)
				throws Exception {
			if(player != null){
				if(player.hasItem(201545)){
            		int itemcount = player.getItemCount(201545);
            		int	groupId = 444;
            		DropGroup group = DropGroups.getDropGroup(groupId, player.getLevel());
		    		Changed ch = new Changed();
            		boolean flag = false;
            		if(group != null){
		    			for(int i = 0;i<itemcount;i++){
		    				int rate = rnd.nextInt(group.getRate());
				    		DropItem dropItem = group.calcDropItem(rate);
				    		int dropcount = getCount(rnd, dropItem.getMin(), dropItem.getMax());
				    		if(player.isFull() || player.getItemCount(dropItem.getItem().getItemId()) + dropcount > 99){
				    			byte[] att = ItemUtils.item2dbAttachment(dropItem.getItem().newInstance(), dropcount);
				    			mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", dropItem.getItem().getName() + "*" + dropcount, "紫罗兰的星空之恋", att, 0, true);
				    			if(!flag){
				    				if(player.getItemCount(dropItem.getItem().getItemId()) + dropcount > 99){
				    					sendMessage(player.getId() ,"您的物品个数已达上限，奖励物品已经发送到邮箱，请及时查收。");
				    					flag = true;
				    				}else{
				    					sendMessage(player.getId() ,"您的背包已满，奖励物品已经发送到邮箱，请及时查收。");
				    					flag = true;
				    				}
				    			}
				    		}else{
				    			player.addItem(dropItem.getItem(), dropcount, ch, player.getClientDataVersion());
				    		}
				    		String item_msg = Items.getMessage(dropItem.getItem().getItemId(), 7, player.getPlayerName(), dropItem.getItem().getName(), "紫罗兰的星空之恋");
				    		if (item_msg != null){
				    				chatService.sendWorldMessage(-1, "系统", item_msg);
				    		}
				    		log.info("ID[" + player.getId() + "] sky_love_getItemID[" + dropItem.getItem().getItemId()+"] add!");
                		}
		    			player.completeRemoveItem(201545, itemcount, ch);
		    			log.info("ID[" + player.getId() + "] sky_love_removeItemID[" + 201545 +"] remove!");
		    			connectService.sendGetItem(ch, player.getId(), command.getAppType());
            		}else{
		    			log.info("ID[" + player.getId() + "] sky_love dropGroup is null. failed!");
		    			return;
		    		}
            	}else{
            		sendMessage("你还没有该物品哦", command.getSerial(), command.getSessionId());
            	}
			}
			
		}
    	
    }
    
    class OpenWarBlessingProcessor implements CommandProcessor{

		@Override
		public void process(WorldPlayer player, Command command)
				throws Exception {
			if(player != null){
				if(player.hasItem(201577)){
            		int itemcount = player.getItemCount(201577);
            		int	groupId = 447;
            		DropGroup group = DropGroups.getDropGroup(groupId, player.getLevel());
		    		Changed ch = new Changed();
            		boolean flag = false;
            		if(group != null){
		    			for(int i = 0;i<itemcount;i++){
		    				int rate = rnd.nextInt(group.getRate());
				    		DropItem dropItem = group.calcDropItem(rate);
				    		int dropcount = getCount(rnd, dropItem.getMin(), dropItem.getMax());
				    		if(player.isFull() || player.getItemCount(dropItem.getItem().getItemId()) + dropcount > 99){
				    			byte[] att = ItemUtils.item2dbAttachment(dropItem.getItem().newInstance(), dropcount);
				    			mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", dropItem.getItem().getName() + "*" + dropcount, "战神的祝福", att, 0, true);
				    			if(!flag){
				    				if(player.getItemCount(dropItem.getItem().getItemId()) + dropcount > 99){
				    					sendMessage(player.getId() ,"您的物品个数已达上限，奖励物品已经发送到邮箱，请及时查收。");
				    					flag = true;
				    				}else{
				    					sendMessage(player.getId() ,"您的背包已满，奖励物品已经发送到邮箱，请及时查收。");
				    					flag = true;
				    				}
				    			}
				    		}else{
				    			player.addItem(dropItem.getItem(), dropcount, ch, player.getClientDataVersion());
				    		}
				    		String item_msg = Items.getMessage(dropItem.getItem().getItemId(), 9, player.getPlayerName(), dropItem.getItem().getName(), "战神的祝福");
				    		if (item_msg != null){
				    				chatService.sendWorldMessage(-1, "系统", item_msg);
				    		}
				    		log.info("ID[" + player.getId() + "] war_bless_getItemID[" + dropItem.getItem().getItemId()+"] add!");
                		}
		    			player.completeRemoveItem(201577, itemcount, ch);
		    			log.info("ID[" + player.getId() + "] war_bless_removeItemID[" + 201577 +"] remove!");
		    			connectService.sendGetItem(ch, player.getId(), command.getAppType());
            		}else{
		    			log.info("ID[" + player.getId() + "] war_bless dropGroup is null. failed!");
		    			return;
		    		}
            	}else{
            		sendMessage("你还没有该物品哦", command.getSerial(), command.getSessionId());
            	}
			}
			
		}
    	
    }
    
    class OpenTrainsSignAll implements CommandProcessor{

		@Override
		public void process(WorldPlayer player, Command command)
				throws Exception {
			if(player != null){
				if(player.hasItem(201582)){
            		int itemcount = player.getItemCount(201582);
            		int	groupId = 452;
            		DropGroup group = DropGroups.getDropGroup(groupId, player.getLevel());
		    		Changed ch = new Changed();
            		boolean flag = false;
            		if(group != null){
		    			for(int i = 0;i<itemcount;i++){
		    				int rate = rnd.nextInt(group.getRate());
				    		DropItem dropItem = group.calcDropItem(rate);
				    		int dropcount = getCount(rnd, dropItem.getMin(), dropItem.getMax());
				    		if(player.isFull() || player.getItemCount(dropItem.getItem().getItemId()) + dropcount > 99){
				    			byte[] att = ItemUtils.item2dbAttachment(dropItem.getItem().newInstance(), dropcount);
				    			mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", dropItem.getItem().getName() + "*" + dropcount, "聚灵神迹", att, 0, true);
				    			if(!flag){
				    				if(player.getItemCount(dropItem.getItem().getItemId()) + dropcount > 99){
				    					sendMessage(player.getId() ,"您的物品个数已达上限，奖励物品已经发送到邮箱，请及时查收。");
				    					flag = true;
				    				}else{
				    					sendMessage(player.getId() ,"您的背包已满，奖励物品已经发送到邮箱，请及时查收。");
				    					flag = true;
				    				}
				    			}
				    		}else{
				    			player.addItem(dropItem.getItem(), dropcount, ch, player.getClientDataVersion());
				    		}
				    		String item_msg = Items.getMessage(dropItem.getItem().getItemId(), 9, player.getPlayerName(), dropItem.getItem().getName(), "聚灵神迹");
				    		if (item_msg != null){
				    				chatService.sendWorldMessage(-1, "系统", item_msg);
				    		}
				    		log.info("ID[" + player.getId() + "] TrainSign_getItemID[" + dropItem.getItem().getItemId()+"] add!");
                		}
		    			player.completeRemoveItem(201582, itemcount, ch);
		    			log.info("ID[" + player.getId() + "] TrainSign_removeItemID[" + 201582 +"] remove!");
		    			connectService.sendGetItem(ch, player.getId(), command.getAppType());
            		}else{
		    			log.info("ID[" + player.getId() + "] TrainSign dropGroup is null. failed!");
		    			return;
		    		}
            	}else{
            		sendMessage("你还没有该物品哦", command.getSerial(), command.getSessionId());
            	}
			}
			
		}
    	
    }
    
    class OpenBlueHeartAll implements CommandProcessor{

		@Override
		public void process(WorldPlayer player, Command command)
				throws Exception {
			if(player != null){
				if(player.hasItem(201621)){
            		int itemcount = player.getItemCount(201621);
            		int	groupId = 460;
            		DropGroup group = DropGroups.getDropGroup(groupId, player.getLevel());
		    		Changed ch = new Changed();
            		boolean flag = false;
            		if(group != null){
		    			for(int i = 0;i<itemcount;i++){
		    				int rate = rnd.nextInt(group.getRate());
				    		DropItem dropItem = group.calcDropItem(rate);
				    		int dropcount = getCount(rnd, dropItem.getMin(), dropItem.getMax());
				    		if(player.isFull() || player.getItemCount(dropItem.getItem().getItemId()) + dropcount > 99){
				    			byte[] att = ItemUtils.item2dbAttachment(dropItem.getItem().newInstance(), dropcount);
				    			mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", dropItem.getItem().getName() + "*" + dropcount, "月神的祝福", att, 0, true);
				    			if(!flag){
				    				if(player.getItemCount(dropItem.getItem().getItemId()) + dropcount > 99){
				    					sendMessage(player.getId() ,"您的物品个数已达上限，奖励物品已经发送到邮箱，请及时查收。");
				    					flag = true;
				    				}else{
				    					sendMessage(player.getId() ,"您的背包已满，奖励物品已经发送到邮箱，请及时查收。");
				    					flag = true;
				    				}
				    			}
				    		}else{
				    			player.addItem(dropItem.getItem(), dropcount, ch, player.getClientDataVersion());
				    		}
				    		String item_msg = Items.getMessage(dropItem.getItem().getItemId(), 9, player.getPlayerName(), dropItem.getItem().getName(), "月神的祝福");
				    		if (item_msg != null){
				    				chatService.sendWorldMessage(-1, "系统", item_msg);
				    		}
				    		log.info("ID[" + player.getId() + "] BlueHeart_getItemID[" + dropItem.getItem().getItemId()+"] add!");
                		}
		    			player.completeRemoveItem(201621, itemcount, ch);
		    			log.info("ID[" + player.getId() + "] BlueHeart_removeItemID[" + 201621 +"] remove!");
		    			connectService.sendGetItem(ch, player.getId(), command.getAppType());
            		}else{
		    			log.info("ID[" + player.getId() + "] BlueHeart dropGroup is null. failed!");
		    			return;
		    		}
            	}else{
            		sendMessage("你还没有该物品哦", command.getSerial(), command.getSessionId());
            	}
			}
		}
    	
    }
    
    
    class FruitRepalceExpProcessor implements CommandProcessor{

		@Override
		public void process(WorldPlayer player, Command command)
				throws Exception {
			int type = Integer.parseInt(command.getParam(0));
			if(player != null){
				if(type == 0){//领取种子
					long today = Utils.getTodayStart();    //一天起始时间
					if(player.getSeedclock().getTime() == today){
						sendMessage("今天已经领过了！", command.getSerial(), command.getSessionId());
						return;
					}else{
						if(player.isFull() || player.getItemCount(201589) + 5 > 99){
							IItemTemplate tmpitem = Items.getTemplate(201589);
        					byte [] att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 5);
        					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 5, "劳动节礼物", att, 0, true);
        					sendMessage("你的背包已满或者物品数量已大于最大堆叠数，物品已经发送至邮箱!", command.getSerial(), command.getSessionId());
						}else{
							Changed changed = new Changed();
							player.addItem(201589, 5, changed, player.getClientDataVersion());//领5个种子
							log.info("ID[" + player.getId() + "] addItemID[" + 201589 +"] add!");
							connectService.sendGetItem(changed, player.getId(), command.getAppType());
						}
						player.setSeedclock(new Date(today));
					}
				}else if(type == 1){//普通
					if(!player.hasItem(201590)){
						sendMessage("你还没有劳动果实哦", command.getSerial(), command.getSessionId());
						return;
					}
					Changed changed = new Changed();
					int exp = 100000;
					int itemcount = player.getItemCount(201590);//背包中劳动果实数
					player.completeRemoveItem(201590, itemcount, changed);
					log.info("ID[" + player.getId() + "] removeItemID[" + 201590 +"] remove!");
					player.addExp(itemcount * exp, changed);
					log.info("ID[" + player.getId() + "] addExp[" + itemcount * exp +"] add!");
					connectService.sendGetItem(changed, player.getId(), command.getAppType());
				}else if(type == 2){//高级
					int itemcount = player.getItemCount(201590);
					if(itemcount <= 0){
	                	sendMessage(player.getId() ,"当前还没有劳动节果实!");
	                	return;
					}
					int price = Integer.parseInt(command.getParam(1));
					byte[] bytes = stageService.getTaskBytes((short)31010, new String[] {
                            "2", "1", "5倍奖励兑换每个果实需要消耗" + price +Server.iMoneyString +"本次需花费" + price * itemcount + Server.iMoneyString +"，确定兑换吗？\n1.是的，我要兑换\n2.一会儿再说",
                            "FruitRepalceExp 3",
                            "ok"});
                	UWAPSegment seg = new UWAPSegment(ClientConstants.
                            GET_FILE_OK, command.getSerial(), command.getSessionId());
                    seg.writeShort((short) 31010);
                    seg.writeShort((short) 2);
                    seg.write(bytes);
                    write(seg);
				}else if(type == 3){
					log.info("ID[" + player.getId() + "] FruitRepalce Type try");
					int itemID = 201591;//高级果实
					int playeritemcount = player.getItemCount(201590);//普通果实数量 
					if(iShopHide(player, itemID, command.getSerial(), command.getSessionId(), playeritemcount, (byte)6, -1, 0) != ISHOP_OK){
						log.info("ID[" + player.getId() + "] iMoney is not enough. failed!");
						sendMessage("您的" + Server.iMoneyString + "不足，请充值再兑换", command.getSerial(), command.getSessionId());
					}else{
						log.info("ID[" + player.getId() + "] FruitRepalce Type success!");
					}
				}
			}
			
		}
    	
    }
    
    class GemTopProcessor implements CommandProcessor {

		@Override
		public void process(WorldPlayer player, Command command)
				throws Exception {
			int type = Integer.parseInt(command.getParam(0));
			if(type == 1){
				UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST, command.getSerial(), command.getSessionId());
	            seg.writeShort((short) 7);
	            seg.writeString("宝石分数排行榜");
	            seg.write((byte) 0);
	            GemTop.writeTop(seg);
	            write(seg);
			}else if(type == 2){
				sendMessage("角色装备和战斗宠装备所有宝石的积分排行榜，宝石等级越高数量越多，分数就越高(只计算4级以上宝石)。", command.getSerial(), command.getSessionId());
				return;
			}
		}
    }
    class ChristmasFestivalReplaceProcessor implements CommandProcessor {
    	public void process(WorldPlayer player, Command command)
		throws Exception {
    		int replaceType = Integer.parseInt(command.getParam(0));
			if (player != null) {
				int itemBuyID = 201701;//
				int itemID = 201700;
				int[] itemData = getIshopEasyPrice(player,itemBuyID,1);
				int itemPrice = itemData[2];
				
				if(!player.hasItem(itemID)){
					sendMessage("你还没有哦", command.getSerial(), command.getSessionId());
					return;
				}
				switch (replaceType) {
				case 1:// 惊喜兑换
					byte[] bytesSec = stageService.getTaskBytes((short) 31010,
							new String[] {
									"3",
									"1",
									"兑换每个泡泡龙的私房柜（已锁）需要消耗您" + itemPrice
											+ Server.iMoneyString
											+ " ，你确认要兑换吗？\n1.兑换1个\n2.兑换全部\n3.一会儿再说",
									"ChristmasFestivalReplace 2","ChristmasFestivalReplace 3", "ok" });
					UWAPSegment segSec = new UWAPSegment(
							ClientConstants.GET_FILE_OK, command.getSerial(),
							command.getSessionId());
					segSec.writeShort((short) 31010);
					segSec.writeShort((short) 2);
					segSec.write(bytesSec);
					write(segSec);
					break;
				case 2:// 花钱兑换一个
					log.info("ID[" + player.getId()
							+ "] ChristmasWishing wishingType[" + replaceType
							+ "] try");
					if (iShopHide(player, itemBuyID, command.getSerial(),
							command.getSessionId(), 1, (byte) 18, -1, 0) != ISHOP_OK) {
						log.info("ID[" + player.getId()
								+ "] ChristmasFestivalReplace replaceType:["
								+ replaceType
								+ "] iMoney is not enough. failed!");
						sendMessage("您的" + Server.iMoneyString + "不足，请充值后再兑换",
								command.getSerial(), command.getSessionId());
					}
					break;			
				case 3:// 花钱兑换全部
					log.info("ID[" + player.getId()
							+ "] ChristmasWishing wishingType[" + replaceType
							+ "] try");
					int allNumOfItem = player.getItemCount(itemID);

					if (iShopHide(player, itemBuyID, command.getSerial(),
							command.getSessionId(), allNumOfItem, (byte) 18,
							-1, 0) != ISHOP_OK) {
						log.info("ID[" + player.getId()
								+ "] ChristmasFestivalReplace replaceType:["
								+ replaceType
								+ "] iMoney is not enough. failed!");
						sendMessage("您的" + Server.iMoneyString + "不足，请充值后再兑换",
								command.getSerial(), command.getSessionId());
					}
					break;
				}
			}
				
    	}
    }
	class DragonBoatFestivalReplaceProcessor implements CommandProcessor {

		@Override
		public void process(WorldPlayer player, Command command)
				throws Exception {
			int replaceType = Integer.parseInt(command.getParam(0));
			if (player != null) {
				int itemBuyID = 201520;//隐藏道具-文艺许愿符-1.5元
				int itemID = 201618;
				int[] itemData = getIshopEasyPrice(player,itemBuyID,1);
				int itemPrice = itemData[2];
				
				if(!player.hasItem(itemID)){
					sendMessage("你还没有端午节粽子哦", command.getSerial(), command.getSessionId());
					return;
				}
				
				switch (replaceType) {
				case 1:// 免费兑换
					log.info("ID[" + player.getId()
							+ "] DragonBoatFestivalReplace replaceType:["
							+ replaceType
							+ "] ReplageCount["
							+ player.getDragonBoatFestivalReplaceCount()
							+ "] try");
					if (player.addDragonBoatFestivalReplaceCount()) {
						Changed dropchanged = new Changed();
						int expNum = 400000;
						player.addExp(expNum, dropchanged);
						player.completeRemoveItem(itemID, 1, dropchanged);
						
						String tmpStr = "兑换成功。";
						int tmpCount = player.checkDragonBoatFestivalReplaceCount();
						if (tmpCount > 0) {
							tmpStr = tmpStr + "今日的免费兑换次数还剩" + tmpCount + "次。";
						} else {
							tmpStr = tmpStr + "您今日的免费兑换次数已经用完，可以明日再来或者使用i币兑换";
						}
						sendMessage(tmpStr, command.getSerial(),
								command.getSessionId());
						sendGetItem(dropchanged, command.getSerial(), command.getSessionId(), (byte) 22);
					} else {
						sendMessage("今日的免费兑换次数已满，请明日再来或使用i币兑换！", command.getSerial(),
								command.getSessionId());
					}
					break;
				case 2:// 花钱兑换一个
					log.info("ID[" + player.getId()
							+ "] ChristmasWishing wishingType[" + replaceType
							+ "] try");
					if (iShopHide(player, itemBuyID, command.getSerial(),
							command.getSessionId(), 1, (byte) 10, -1, 0) != ISHOP_OK) {
						log.info("ID[" + player.getId()
								+ "] DragonBoatFestivalReplace replaceType:["
								+ replaceType
								+ "] iMoney is not enough. failed!");
						sendMessage("您的" + Server.iMoneyString + "不足，请充值后再兑换",
								command.getSerial(), command.getSessionId());
					}
					break;
				case 3:// 花钱兑换提示
//					int price = Integer.parseInt(command.getParam(1));
					byte[] bytes = stageService.getTaskBytes((short) 31010,
							new String[] {
									"2",
									"1",
									"惊喜兑换有机会获得意想不到的物品哦，顶级鉴定宝钻、4级宝石随机包、8钻地狱装随机包、紫罗兰的星空钻戒等等大奖等着你。 你要兑换吗？\n1.是的，我要兑换\n2.一会儿再说",
									"DragonBoatFestivalRepalce 4", "ok" });
//					new String[] {
//							"2",
//							"1",
//							"付费兑换有机会获得意想不到的物品哦，但是需要消耗" + price
//							+ Server.iMoneyString
//							+ " ，你确认要兑换吗？\n1.是的，我要兑换\n2.一会儿再说",
//							"DragonBoatFestivalRepalce 2", "ok" });
					UWAPSegment seg = new UWAPSegment(
							ClientConstants.GET_FILE_OK, command.getSerial(),
							command.getSessionId());
					seg.writeShort((short) 31010);
					seg.writeShort((short) 2);
					seg.write(bytes);
					write(seg);
					break;
				case 4:// 花钱兑换
					//int price = Integer.parseInt(command.getParam(1));
					byte[] bytesSec = stageService.getTaskBytes((short) 31010,
							new String[] {
									"3",
									"1",
									"惊喜兑换每个粽子需要消耗您" + itemPrice
											+ Server.iMoneyString
											+ " ，你确认要兑换吗？\n1.兑换1个\n2.兑换全部\n3.一会儿再说",
									"DragonBoatFestivalRepalce 2","DragonBoatFestivalRepalce 5", "ok" });
					UWAPSegment segSec = new UWAPSegment(
							ClientConstants.GET_FILE_OK, command.getSerial(),
							command.getSessionId());
					segSec.writeShort((short) 31010);
					segSec.writeShort((short) 2);
					segSec.write(bytesSec);
					write(segSec);
					break;
				case 5:// 花钱兑换全部
					log.info("ID[" + player.getId()
							+ "] ChristmasWishing wishingType[" + replaceType
							+ "] try");
					// int itemID = 201520;// 隐藏道具-文艺许愿符-1.5元
					int allNumOfItem = player.getItemCount(itemID);

					if (iShopHide(player, itemBuyID, command.getSerial(),
							command.getSessionId(), allNumOfItem, (byte) 10,
							-1, 0) != ISHOP_OK) {
						log.info("ID[" + player.getId()
								+ "] DragonBoatFestivalReplace replaceType:["
								+ replaceType
								+ "] iMoney is not enough. failed!");
						sendMessage("您的" + Server.iMoneyString + "不足，请充值后再兑换",
								command.getSerial(), command.getSessionId());
					}
					break;
				}
			}
		}

	}
	class NoahsarkProcessor implements CommandProcessor{
    	public void process(WorldPlayer player, Command command) throws Exception {
    		int donateCount;
    		int totalScore;
    		if(NoahsarkConfig.noahsarkPlayer.size() == 0 ) {
    			donateCount = 0;
    			totalScore = 0;
    			NoahsarkConfig.setDonateNumber(player.getId(), player.getPlayerName(), 0, 0);

    		}else if(!NoahsarkConfig.noahsarkPlayer.containsKey(player.getId())){
    			donateCount = 0;
    			totalScore = 0;
    			NoahsarkConfig.setDonateNumber(player.getId(), player.getPlayerName(), 0, 0);
    		}{
    		NoahsarkPlayer nplayer = NoahsarkConfig.noahsarkPlayer.get(player.getId());
    		donateCount = nplayer.getTotalCount();
    		totalScore = nplayer.getTotalScore();
    		}
    		//int donateCount = 0;
    		if (player != null) {
    			synchronized (player) {
    				int itemType = Integer.valueOf(command.getParam(0));
    				if(itemType <= 4 && donateCount < 20){
	    					// 捐献银矿石，帆布，上等木料，杂鱼
	    					if(NoahsarkConfig.getStage() == NoahsarkConfig.STAGE_NOT_STARTED){
	    						sendMessage(NoahsarkConfig.donate.getMessage()[0].toString(), 
	    								command.getSerial(), command.getSessionId());
	    					}else if(NoahsarkConfig.getStage() == NoahsarkConfig.STAGE_DONATE_END){
	    						sendMessage(NoahsarkConfig.donate.getMessage()[1].toString(), 
	    								command.getSerial(), command.getSessionId());
	    					}
	    					else if (NoahsarkConfig.getStage() == NoahsarkConfig.STAGE_DONATE_NOT_STARTED) {
	    						sendMessage(NoahsarkConfig.donate.getMessage()[2].toString(), 
	    								command.getSerial(), command.getSessionId());
	    					} else if (NoahsarkConfig.getStage() == NoahsarkConfig.STAGE_DONATE_STARTED) {
	    						int itemId = NoahsarkConfig.donate.getMaterial()[itemType-1].getItemId();
	    						int itemCount = NoahsarkConfig.donate.getMaterial()[itemType-1].getItemCount();
	    						IItem iitem = Items.getTemplate(itemId).newInstance();
	    						int count = player.getItemCount(itemId);
	    						if (count >= itemCount) {
	    							Changed changed = new Changed();
	    							IItem item = player.completeRemoveItem(itemId, itemCount, changed);
	    							if (item != null) {
	    								Utils.log(log, player.getId(), command.getAppType(),
	    										"]Item[" + Utils.getHexdump(item.toDbBytes()) + "]count[" + count + "] noahsark remove Success");
	    							} else {
	    								Utils.log(log, player.getId(), command.getAppType(),
	    										"]ItemId[" + itemId + "] noahsark remove Error");
	    							}
	    							int giftId = NoahsarkConfig.donate.getAward().getItemId();
	    							int giftCount = NoahsarkConfig.donate.getAward().getItemCount();
	    							IItem iit = Items.getTemplate(giftId).newInstance();
	    							if (iit != null) {
	    								IItem nItem = player.completeAddItem(iit, giftCount, changed, player.getClientDataVersion());
	    								if (nItem == null) {
	    									byte[] att = ItemUtils.item2dbAttachment(iit, count);
	    									mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	    											iit.getName() + "*" + count, "您的背包已满，请整理后再提取", att, 0, true);
	    									sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到您的邮箱!");
	    								} else {
	    									Utils.log(log, player.getId(), command.getAppType(),
	    											"] Add ItemId[" + giftId + "] noahsark donate gift Success");
	    									sendMessage("你已经捐献了" + itemCount + "个" + iitem.getName() +"，获得一个经验包，同时获得一点捐献积分。",command.getSerial(), command.getSessionId());
	    									
	    								}
	    								NoahsarkConfig.setDonateNumber(player.getId(), player.getPlayerName(), 1, 1);
	    							} else {
	    								Utils.log(log, player.getId(), command.getAppType(),
	    										"]ItemId[" + giftId  + "] noahsark donate gift Error");
	    							}
	    							sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte)73);
	    						} else {
	    							sendMessage("你的" + iitem.getName() + "数量还不够，你可以通过采集或者杀死怪物来获得。", 
	    									command.getSerial(), command.getSessionId());
	    						}
	    					} 
	    				}
	    				
	    				
    				else if(itemType <=4 && donateCount >= 20){
    					sendMessage(NoahsarkConfig.donate.getMessage()[3].toString(),command.getSerial(),command.getSessionId());
    					
    				}else if(itemType >4){
    					if (itemType == 5) {
    						// 查看捐献积分排行榜
    						NoahsarkConfig.setSortTop();
    						List list = NoahsarkConfig.getSortTop();
    						switch(NoahsarkConfig.getStage()){
    							case NoahsarkConfig.STAGE_NOT_STARTED:
    							case NoahsarkConfig.STAGE_DONATE_NOT_STARTED:
    							case NoahsarkConfig.STAGE_DONATE_END:
									UWAPSegment seg1 = new UWAPSegment(ClientConstants.GENERIC_LIST);
	    							seg1.writeShort((short) 10267);
	    							seg1.writeString("你本周的捐献积分为"+ totalScore + ",积分排行榜的前十名为：");
	    							seg1.write((byte) 0);
	    							seg1.writeShort((short)0);
	    							seg1.writeInt(0);
	    							seg1.writeString("");
	    							seg1.writeInt(0);
	    							connectService.writeTo(seg1, player.getId());
    								break;
    							case NoahsarkConfig.STAGE_DONATE_STARTED:
			    					/*if (NoahsarkConfig.getStage() == NoahsarkConfig.STAGE_TOP_STARTED) {
			    						sendMessage("捐献积分榜正在兑换奖励，请稍等片刻。", 
			    								command.getSerial(), command.getSessionId());
			    					} else if(NoahsarkConfig.getStage() == NoahsarkConfig.STAGE_TOP_END) {
			    						sendMessage("捐献积分榜正在兑换奖励，请稍等片刻。", 
			    								command.getSerial(), command.getSessionId());
			    					}else{*/
			    						
		    						if (list == null || list.isEmpty() || list.size() == 0) {
		    							sendMessage("还没有玩家登上排行榜，要把握机会啊。", command.getSerial(), command.getSessionId());
		    						} else {
		    							UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
		    							seg.writeShort((short) 10267);
		    							seg.writeString("你本周的捐献积分为"+ totalScore + ",积分排行榜的前十名为：");
		    							seg.write((byte) 0);
		    							seg.writeShort((short) Math.min(list.size(), NoahsarkConfig.MAX_TOP));
		    							for(int i = 0; i < Math.min(list.size(), NoahsarkConfig.MAX_TOP); i++) {				 
		    								NoahsarkPlayer playerInfo = (NoahsarkPlayer)list.get(i);;
		    								if(playerInfo == null || playerInfo.getName()== null ||((playerInfo.getName().equals("") && playerInfo.getName().length() == 0))){
		    									continue;
		    								}if(playerInfo.getTotalScore() == 0){
		    									seg.writeInt(0);
			    								seg.writeString("");
			    								seg.writeInt(0);
		    								}else{
			    								seg.writeInt(playerInfo.getId());
			    								seg.writeString(i + 1 + "." + playerInfo.getName() + "  (" + playerInfo.getTotalScore() + ")分");
			    								seg.writeInt(Utils.CLR_WHITE);
		    								}
		    								
		    							}
		    							connectService.writeTo(seg, player.getId());
		    						}
		    						break;
	    	
	    				}
    				}else if(itemType == 6){	// 活动详情
	    					sendMessage(NoahsarkConfig.donate.getMessage()[4].toString(), 
	    							command.getSerial(), command.getSessionId());
    				 }
    			   }
	    				
    			}
    		}
    	}
    }
}
