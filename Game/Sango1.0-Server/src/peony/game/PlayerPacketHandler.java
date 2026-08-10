package peony.game;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import peony.alchemy.AlchemyCall;
import peony.alchemy.AlchemyIBugCall;
import peony.alchemy.AlchemyService;
import peony.auction.AuctionBuyCall;
import peony.auction.AuctionCreateCall;
import peony.auction.AuctionDeleteCall;
import peony.auction.AuctionListCall;
import peony.auction.PublishiedCall;
import peony.channel.LiMeiActivationCall;
import peony.db.ActivationCodeCall;
import peony.db.AlphaGiftGetCall;
import peony.db.DBService;
import peony.db.DeletePlayerCall;
import peony.db.ExchangeItemFromNpcCall;
import peony.db.GMCallCall;
import peony.db.GetAllMailAttachCall;
import peony.db.GetAppointItemDescCall;
import peony.db.GetItemFromNpcCall;
import peony.db.GiftGetCall;
import peony.db.LoadActorListCall;
import peony.db.MailAttachmentCall;
import peony.db.MailContentCall;
import peony.db.MailDeleteCall;
import peony.db.MailFavoriteCall;
import peony.db.MailListCall;
import peony.db.MailObsoleteDeleteCall;
import peony.db.MailPostCall;
import peony.db.MergeCardCall;
import peony.db.NpcDescCall;
import peony.db.PlayerCreateCall;
import peony.db.PlayerInfoCall;
import peony.db.PlayerLoadCall;
import peony.db.PlayerRateCall;
import peony.db.PlayerRenameCall;
import peony.db.RefreshPropertyPointCall;
import peony.db.StarPromoteCall;
import peony.db.SyncExecutorService;
import peony.depot.DepotException;
import peony.depot.DepotService;
import peony.game.actlead.ActLeaderListCall;
import peony.game.association.Association;
import peony.game.association.AssociationCreateCall;
import peony.game.association.AssociationException;
import peony.game.association.AssociationMember;
import peony.game.association.AssociationService;
import peony.game.asyncbattle.AsyncBattleChallengeRankCall;
import peony.game.asyncbattle.AsyncBattleCountIBugCall;
import peony.game.asyncbattle.AsyncBattleInfo;
import peony.game.asyncbattle.AsyncBattleService;
import peony.game.asyncbattle.AsyncNormalBoard;
import peony.game.asyncbattle.AsyncPlayer;
import peony.game.asyncbattle.AsyncSuiteIndexInfoCall;
import peony.game.asyncbattle.PlayerBodyAi;
import peony.game.attendant.Attendant;
import peony.game.attendant.AttendantAddSkillCall;
import peony.game.attendant.AttendantException;
import peony.game.attendant.AttendantFixService;
import peony.game.attendant.AttendantLearnSkillCall;
import peony.game.battlefield.FlagBattleFieldInstance;
import peony.game.battlefield.FlagBattleFieldVMapManager;
import peony.game.bbs.BbsContentCall;
import peony.game.bbs.BbsListCall;
import peony.game.beautyparade.BeautyListCall;
import peony.game.beautyparade.BeautyParadeService;
import peony.game.beautyparade.BeautySignUpCall;
import peony.game.beautyparade.BeautyVoteCall;
import peony.game.beautyparade.FindFriendListCall;
import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.game.buff.SlowDebuff;
import peony.game.changed.AttendantStringPropertyChangedItem;
import peony.game.changed.ChangedItem;
import peony.game.changed.DurationChangedItem;
import peony.game.chat.ChatMessage;
import peony.game.chat.ChatService;
import peony.game.chat.ItemChatAttachment;
import peony.game.chinarun.ChinarunCall;
import peony.game.clientbbs.ClientBbs;
import peony.game.clientbbs.ClientBbsService;
import peony.game.convoy.ConvoyException;
import peony.game.convoy.NationConvoyService;
import peony.game.coordinate.AccountLogin360Call;
import peony.game.coordinate.AccountLoginDuokuCall;
import peony.game.directory.ClientDirectory;
import peony.game.directory.ClientDirectory.Directory;
import peony.game.drop.GroupDrop;
import peony.game.exchange.Exchange;
import peony.game.exchange.ExchangeGrid;
import peony.game.exp.ExpException;
import peony.game.exp.ExpService;
import peony.game.file.FileData;
import peony.game.gift.FetchGiftService;
import peony.game.instance.BossScoreBoardCall;
import peony.game.instance.BossTimeScoreCall;
import peony.game.instance.DecInstanceTimeCall;
import peony.game.instance.InstanceSweepCall;
import peony.game.instance.InstanceSweepService;
import peony.game.instance.NormalInstance;
import peony.game.instance.WomenDayInstanceService;
import peony.game.itemeffect.ActivityItemEffect;
import peony.game.itemeffect.AddAlchemyExpEffect;
import peony.game.itemeffect.AddAttendantExpEffect;
import peony.game.itemeffect.AddCardExpItemEffect;
import peony.game.itemeffect.AddItemEffect;
import peony.game.itemeffect.AddVipExpEffect;
import peony.game.itemeffect.DropItemEffect;
import peony.game.itemeffect.GetExpEffect;
import peony.game.itemeffect.GetHonorEffect;
import peony.game.itemeffect.JewelsBagItemEffect;
import peony.game.itemeffect.KingItemEffect;
import peony.game.itemeffect.RideItemEffect;
import peony.game.itemeffect.ScriptEffect;
import peony.game.itemenhance.AddHoleCall;
import peony.game.itemenhance.AddJewelCall;
import peony.game.itemenhance.AddMaxHoleAllCall;
import peony.game.itemenhance.AddMaxHoleCall;
import peony.game.itemenhance.AutoAddHole;
import peony.game.itemenhance.AutoEquipEnhance;
import peony.game.itemenhance.AutoMergeJewelCall;
import peony.game.itemenhance.AutoMergeJewelCall1;
import peony.game.itemenhance.AutoNaturalEnhance;
import peony.game.itemenhance.GetJewelConfigCall;
import peony.game.itemenhance.ItemEnhance;
import peony.game.itemenhance.JewelService;
import peony.game.itemenhance.MergeJewelCall;
import peony.game.itemenhance.NaturalEnhance;
import peony.game.itemenhance.RemoveAllJewelCall;
import peony.game.itemenhance.RemoveAllJewelCall1;
import peony.game.itemenhance.RemoveJewelCall;
import peony.game.itemenhance.UpgradeJewelCall;
import peony.game.mail.MailRecoverCall;
import peony.game.mail.MailService;
import peony.game.map.TouchExitTransferCall;
import peony.game.nation.CandidateListCall;
import peony.game.nation.CandidateService;
import peony.game.nation.CandidateSignUpCall;
import peony.game.nation.CollectCall;
import peony.game.nation.ContributeCreditCall;
import peony.game.nation.Forbid;
import peony.game.nation.ForbidCall;
import peony.game.nation.Nation;
import peony.game.nation.NationBattleFieldDef;
import peony.game.nation.NationBattleFieldInstance;
import peony.game.nation.NationBattleFieldSignupException;
import peony.game.nation.NationDeclareException;
import peony.game.nation.NationDeclareListCall;
import peony.game.nation.NationQuestCall;
import peony.game.nation.NationQuestRequestCall;
import peony.game.nation.NationRel;
import peony.game.nation.NationService;
import peony.game.nation.NationSkill;
import peony.game.nation.NationSkillDescCall;
import peony.game.nation.NationSkillListCall;
import peony.game.nation.NationSkillStudyCall;
import peony.game.nation.NationSloganCall;
import peony.game.nation.NationSneakBattleFieldDef;
import peony.game.nation.NationSneakBattleFieldInstance;
import peony.game.nation.NationVoteException;
import peony.game.nation.Officer;
import peony.game.nation.OfficerCall;
import peony.game.nation.PunishCall;
import peony.game.nation.VoteCall;
import peony.game.notification.NotificationBindCall;
import peony.game.party.Party;
import peony.game.party.PartyFullException;
import peony.game.party.PartyMember;
import peony.game.party.PartyRequest;
import peony.game.party.PartyService;
import peony.game.party.TransLeaderException;
import peony.game.pk.PkInfo;
import peony.game.quest.ExpRewardEntry;
import peony.game.question.QuestionException;
import peony.game.question.QuestionService;
import peony.game.roll.Roll;
import peony.game.salary.SalaryInfoCall;
import peony.game.salary.SalaryService;
import peony.game.skill.Skill;
import peony.game.stepserver.StepClient;
import peony.game.stepserver.StepServer;
import peony.game.suite.SuiteEffect;
import peony.game.weather.Weather;
import peony.gatecard.GateCardChargeCall;
import peony.marriage.AskForGiftService;
import peony.marriage.DivorceCall;
import peony.marriage.MarriageCall;
import peony.marriage.MarriageException;
import peony.marriage.MarriageInfoCall;
import peony.marriage.MarriageService;
import peony.marriage.PayForOtherCall;
import peony.marriage.WeddingInstance;
import peony.marriage.WeddingService;
import peony.marriage.WeddingSignListCall;
import peony.mobiphone.TelcoChargeCall;
import peony.net.AbstractClientSession;
import peony.net.AdminDispatchClientSession;
import peony.net.ClientSession;
import peony.net.DispatchPacket;
import peony.net.Packet;
import peony.net.PacketHandler;
import peony.npc.service.PloyNpcService;
import peony.produce.ProduceService;
import peony.service.ClearanceSaleService;
import peony.service.CycleInstanceMapManager;
import peony.service.LogPlayeActionService;
import peony.service.MonthlyPayService;
import peony.service.QuestRewardService;
import peony.service.ServiceEvent;
import peony.service.VIP.VipPrivilegeService;
import peony.service.account.Account;
import peony.service.account.AccountChangePasswordCall;
import peony.service.account.AccountLogin91Call;
import peony.service.account.AccountLoginCall;
import peony.service.account.AccountLoginLenovoCall;
import peony.service.account.AccountLoginUCCall;
import peony.service.account.AccountLoginXiaomiCall;
import peony.service.account.AccountLoginYUNYOUAndroidCall;
import peony.service.account.AccountQuickRegisterCall;
import peony.service.account.AccountRegisterCall;
import peony.service.account.AccountRenameCall;
import peony.service.account.AccountService;
import peony.service.account.AlipayGetOrderCall;
import peony.service.account.Charge;
import peony.service.account.ChargeActivityService;
import peony.service.account.ChargeDownJoyCall;
import peony.service.account.ChargeInfoService;
import peony.service.account.ChargeRecordCall;
import peony.service.account.ChargeRegularCall;
import peony.service.account.HuaweiGetOrderCall;
import peony.service.account.IBuyHistoryCall;
import peony.service.account.IMoneyCardCall;
import peony.service.account.KTouchGetOrderCall;
import peony.service.account.PartnerGetOrderCall;
import peony.service.account.PhoneNotifyCall;
import peony.service.account.RecordChargeCall;
import peony.service.account.RecordChargeService;
import peony.service.account.XiaomiGetOrderCall;
import peony.service.account.ZhongXingGetOrderCall;
import peony.service.account.adapter.AppStoreService;
import peony.service.account.adapter.ClientChargeService;
import peony.service.account.adapter.HangameInviteFriendCall;
import peony.service.account.adapter.KTouchChargeService;
import peony.service.account.adapter.QmePayCall;
import peony.service.account.adapter.QmeQueryBalanceCall;
import peony.service.account.cmcc.CmccAccountService;
import peony.service.account.cmcc.CmccAndroidSmsBuyCall;
import peony.service.account.cmcc.CmccChargeCall;
import peony.service.account.cmcc.CmccChargeNewCall;
import peony.service.account.cmcc.CmccDownloadOkMessage;
import peony.service.accountbinding.AccountBindCall;
import peony.service.accountbinding.AccountBindStatusCall;
import peony.service.activity.Activity;
import peony.service.activity.ActivityService;
import peony.service.activity.AnniversaryService;
import peony.service.activity.AwardActivityService;
import peony.service.activity.CardPunchActService;
import peony.service.activity.FestivalConvoyActivity;
import peony.service.activity.MayDayActivity;
import peony.service.activity.NewYearActivity;
import peony.service.activity.SendNewYearPrayService;
import peony.service.activity.TenthAnniversaryService;
import peony.service.activity.ThreeYearsActivity1;
import peony.service.activity.ValentineListCall;
import peony.service.activity.VowActivity;
import peony.service.activity.VowIbuyCall;
import peony.service.apprentice.ApprenticeInvitOkCall;
import peony.service.apprentice.ApprenticeListCall;
import peony.service.apprentice.ApprenticeService;
import peony.service.apprentice.RemoveApprenticeCall;
import peony.service.award.AwardException;
import peony.service.award.AwardGetCall;
import peony.service.award.AwardService;
import peony.service.cards.AddCardCall;
import peony.service.cards.AddCardHoleCall;
import peony.service.cards.CardAddEnergyCall;
import peony.service.cards.CardAutoAddEnergyCall;
import peony.service.cards.CardCollectionCall;
import peony.service.cards.CardException;
import peony.service.cards.CardGroup;
import peony.service.cards.CardInfo;
import peony.service.cards.CardInfoCall;
import peony.service.cards.CardList4SheetCall;
import peony.service.cards.CardListCall;
import peony.service.cards.CardListDetailCall;
import peony.service.cards.CardRecollectionCall;
import peony.service.cards.CardRockCall;
import peony.service.cards.CardService;
import peony.service.cards.CardUnEquipCall;
import peony.service.cards.CardUpGradeCall;
import peony.service.cards.EquipCardCall;
import peony.service.cards.ImoneyRockCardCall;
import peony.service.duel.DuelException;
import peony.service.duel.DuelService;
import peony.service.duelmetting.DuelMettingException;
import peony.service.duelmetting.DuelMettingService;
import peony.service.enhance.EnhanceService;
import peony.service.enhance.EquipEnhanceCall;
import peony.service.enhance.EquipLevelUpInfoCall;
import peony.service.enhance.EquipLevelUp_ProcessCall;
import peony.service.exam.ExamAnswerCall;
import peony.service.exam.ExamBoardListCall;
import peony.service.exam.ExamChangeCall;
import peony.service.exam.ExamQuweiCall;
import peony.service.exam.ExamRedirectPassCall;
import peony.service.exam.ExamRequestCall;
import peony.service.exam.ExamResultCall;
import peony.service.fame.Fame;
import peony.service.fame.FameService;
import peony.service.feast.FeastInstance;
import peony.service.feast.FeastInstanceService;
import peony.service.fiveelement.FiveElementService;
import peony.service.friend.AddFriendCall;
import peony.service.friend.ChangeFriendLockCall;
import peony.service.friend.DelFriendCall;
import peony.service.friend.GetFriendListCall;
import peony.service.friend.PlayerRelation;
import peony.service.friend.RelationService;
import peony.service.friend.RelationServiceException;
import peony.service.gamble.GambleService;
import peony.service.levellimit.LevelLimitService;
import peony.service.nationDayActivity.NationDayService;
import peony.service.player.PlayerService;
import peony.service.pluginstance.ChessInstanceService;
import peony.service.quest.EscortException;
import peony.service.quest.EscortQuestService;
import peony.service.quest.ReFreshEscortCall;
import peony.service.quest.VipDemandEscortCall;
import peony.service.ranking.CycleInstanceRanking;
import peony.service.ranking.Ranking;
import peony.service.ranking.RankingService;
import peony.service.read.Book;
import peony.service.read.BookUtil;
import peony.service.read.Books;
import peony.service.read.PayForReadCall;
import peony.service.read.QuickDecBookTimeCall;
import peony.service.shop.CmccBuyCall;
import peony.service.shop.CmccShopListCall;
import peony.service.shop.IBuy;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.QuickBuyAndUseCall;
import peony.service.shop.QuickBuyCall;
import peony.service.shop.ShopBuyCall;
import peony.service.shop.ShopException;
import peony.service.shop.ShopListCall;
import peony.service.shop.ShopSellCall;
import peony.service.shop.ShopService;
import peony.service.shop.ShopTopListCall;
import peony.service.shop.YunyouImoneyBuyCall;
import peony.service.sleepycat.SleepyCatService;
import peony.service.stat.AchievementListCall;
import peony.service.stat.PvpInfo;
import peony.service.stat.StatService;
import peony.service.tong.CreateTongCall;
import peony.service.tong.JoinTongCall;
import peony.service.tong.ListTongMemberCall;
import peony.service.tong.QuitTongCall;
import peony.service.tong.RejectInvitationCall;
import peony.service.tong.RemoveTongCall;
import peony.service.tong.RenameTongCall;
import peony.service.tong.SetTongSloganCall;
import peony.service.tong.TagTongCall;
import peony.service.tong.Tong;
import peony.service.tong.TongContributeCall;
import peony.service.tong.TongException;
import peony.service.tong.TongExitRequestCall;
import peony.service.tong.TongForbidCall;
import peony.service.tong.TongInfoCall;
import peony.service.tong.TongInfoCall2;
import peony.service.tong.TongInviteCall;
import peony.service.tong.TongJoinApply;
import peony.service.tong.TongKickCall;
import peony.service.tong.TongMember;
import peony.service.tong.TongPromoteCall;
import peony.service.tong.TongQuestCall;
import peony.service.tong.TongQuestRequestCall;
import peony.service.tong.TongService;
import peony.service.tong.TongShopItemBuy;
import peony.service.tong.TongSkill4;
import peony.service.tong.TongSkillDescCall;
import peony.service.tong.TongSkillLevelUpCall;
import peony.service.tong.TongSkillListCall;
import peony.service.tong.TongSkillStudyCall;
import peony.service.tong.UnTagTongCall;
import peony.service.tong.apply.TongBattleApplyCall;
import peony.service.tong.apply.TongBattleApplyListCall;
import peony.service.tong.apply.TongBattleApplyService;
import peony.service.tong.apply.TongBattleBidCall;
import peony.service.tong.apply.TongBattleException;
import peony.service.tong.battle.TongBattleVMapManager;
import peony.service.towerdefend.TowerDefendException;
import peony.service.towerdefend.TowerDefendService;
import peony.service.version.CompareVersionCall;
import peony.service.version.GetMapNpcFilesCall;
import peony.service.weibo.BindWeiboCall;
import peony.service.weibo.SendWeiboCall;
import peony.service.weibo.TransformWeiboCall;
import peony.service.weibo.WeiboLoginCall;
import peony.service.weibo.WeiboQuickRegistrateCall;
import peony.service.weibo.WeiboService;
import peony.service.welfare.WelfareListCall;
import peony.service.welfare.WelfareRewardCall;
import peony.service.worldmap.WorldMapService;
import peony.teleport.service.TeleportService;
import peony.util.IStringValidator;
import peony.util.StringUtil;
import peony.vm.ASMQuest;
import peony.vm.ASMQuestUtil;
import peony.vtc.charge.ViettelCardChargeCall;
import peony.vtc.charge.VtcCardChargeCall;
import weibo4j.WeiboException;
import ch.javasoft.util.intcoll.IntHashMap;
import ch.javasoft.util.intcoll.IntMap.IntEntry;
import com.pip.sanguo.data.BookChapter;
import com.pip.sanguo.data.BookConfig;
import com.pip.sanguo.data.Card;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.DirectoryType;
import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.GiftGroup;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Rank;
import com.pip.sanguo.data.GiftGroup.GiftDef;
import com.pip.sanguo.data.item.Formula;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.map.GameMapExit;
import com.pip.sanguo.data.map.GameMapExitConstraints;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.quest.QuestRewardItem;
import com.pip.sanguo.data.quest.QuestRewardSet;
import com.pip.util.Utils;
import com.sleepycat.je.DatabaseException;

import edu.emory.mathcs.backport.java.util.Arrays;
public class PlayerPacketHandler implements PacketHandler {
	private static final Logger log = Logger.getLogger(PlayerPacketHandler.class);
	protected Random rnd = new Random();

	public static boolean asyncVersionCompare = true; //异步版本比较
	public static boolean asyncGetFile = true; //异步下载文件
	
	public PlayerPacketHandler() {
	}

	public void handle(Packet packet, ClientSession session, int diff)
			throws Exception {
		short opCode = packet.getOpCode();
		// log.debug("receive client message:"+opCode);
		Player player = (Player)session.getClient();
		if(player!=null && StatService.trackLogPlayer.contains(player.id)){
			log.info("[TRACKOPCODE]PLAYEID[" + player.id + "]OPCODE[" + opCode + "]");
		}
		// 本地服务器向跨服服务器的packet包处理
		if(player!=null && !Server.isStepServer){
			StepClient stepClient = Server.server.getServiceRegistry().getStepClient();
			if(packet.getOpCode()==OpCode.STEPSERVER_BATTLE_SIGNUP_CLIENT){
				Packet pat = packet.clone();
				pat.getInt();
				int type=pat.getByte();
				//跨服战报名协议
				try {
					Packet pt = new Packet(OpCode.STEPSERVER_BATTLE_SIGNUP_CLIENT);
					pt.putInt(0);
					pt.putShort(player.level);
					pt.put(type);//跨服战类型
					pt.putUTF(Server.server.gameCode);
					pt.data.flip();
					DispatchPacket dpt = new DispatchPacket(((AbstractClientSession)player.session).getId(), pt);
					dpt.accountId = player.accountId;
					dpt.playerId = player.id;
					player.stepType=type;//玩家的跨服类型也同上
					stepClient.send(dpt, player.accountId, player.id, player.session);
				} catch (Exception e) {
					player.message(-1, "网络中断,请稍后再试", -1, -1);
				}
				return;
			}else if(packet.getOpCode()==OpCode.STEPSERVER_BATTLE_SCORE_CLIENT){
				Packet pat = packet.clone();
				pat.getInt();
				int type=pat.getByte();
				//跨服排行榜协议
				Packet pt = new Packet(OpCode.STEPSERVER_BATTLE_SCORE_CLIENT);
				pt.put(0);
				pt.put(type);//跨服战类型
				pt.data.flip();
				DispatchPacket dpt = new DispatchPacket(((AbstractClientSession)player.session).getId(), pt);
				dpt.accountId = player.accountId;
				dpt.playerId = player.id;
				stepClient.send(dpt, player.accountId, player.id, player.session);
				return;
			}else if(packet.getOpCode()==OpCode.STEPSERVER_BATTLE_SCORE_FINALS_CLIENT){//争霸赛押注观战排行榜
				try {
					Packet pat=packet.clone();
					pat.getInt();
					Packet pt=new Packet(OpCode.STEPSERVER_BATTLE_SCORE_FINALS_CLIENT);
					pt.put(0);
					pt.putUTF(Server.server.gameCode);
					pt.data.flip();
					DispatchPacket dpt=new DispatchPacket(((AbstractClientSession)player.session).getId(), pt);
					dpt.accountId=player.accountId;
					dpt.playerId=player.id;
					stepClient.send(dpt, player.accountId, player.id, player.session);
				} catch (Exception e) {
					player.message(-1, "网络中断,请稍后再试", -1, -1);
				}
				return;
			}else if(packet.getOpCode()==OpCode.STEPBATTLE_FINALS_BETANDWATCH_CLIENT){//争霸赛押注观战协议
				Packet pat=packet.clone();
				pat.getInt();
				int type=pat.get();
				int targetPlayerId=pat.getInt();
				int targetPlayerAccountId=pat.getInt();
				int betCoins=pat.getInt();
				Packet pt=new Packet(OpCode.STEPBATTLE_FINALS_BETANDWATCH_CLIENT);
				pt.putInt(0);
				pt.put(type);
				pt.putInt(targetPlayerId);
				pt.putInt(targetPlayerAccountId);
				pt.putInt(betCoins);
				pt.putUTF(Server.server.gameCode);
				pt.putInt(player.money);
				pt.data.flip();
				DispatchPacket dpt=new DispatchPacket(((AbstractClientSession)player.session).getId(), pt);
				dpt.accountId=player.accountId;
				dpt.playerId=player.id;
				stepClient.send(dpt, player.accountId, player.id, player.session);
				return;
			}
			if(player.isInStep){
				//如果处于跨服战场状态，那么过滤协议包向跨服服务器发送请求
				if(StepClient.canSend(packet.getOpCode())){
					DispatchPacket dp = new DispatchPacket(session.getId(), packet.clone());
					dp.accountId = player.accountId;
					dp.playerId = player.id;
					stepClient.send(dp, player.accountId, player.id, session);
					return;
				}else{
					if(packet.getOpCode()!=OpCode.SYNC_TIME_CLIENT){
						int serial = 0;
						try{serial = packet.clone().getInt();}catch(Exception e){}
						if(packet.getOpCode()==OpCode.PARTY_TRANSFER_LEADER_CLIENT || packet.getOpCode()==OpCode.CHAT_CLIENT)
							serial = -1;
						ErrorHandler.sendErrorMessage(session, serial, packet.getOpCode(), "跨服战场期间不能使用此功能");
						return;
					}
				}
			}
		}
//		if(Server.isStepServer && player!=null && player.isInStep){
//			if(packet.getOpCode()==OpCode.CMCC_ISHOP_BUY_CLIENT){
//				ErrorHandler.sendErrorMessage(session, 0, packet.getOpCode(), "跨服战场期间不能使用此功能");
//				return;
//			}
//		}
//		if(Server.isStepServer && player!=null && !player.isInStep){
//			return;
//		}
		switch (opCode) {
		case OpCode.SYNC_TIME_CLIENT:
			syncTime(packet, session);
			break;
		case OpCode.ACTOR_LOGIN_CLIENT:
			login(packet, session);
			break;
		case OpCode.MOVE_CLIENT:
			move(packet, session, diff);
			break;
		case OpCode.RIDE_CLIENT:
			ride(packet, session);
			break;
		case OpCode.UNRIDE_CLIENT:
			unRide(packet, session);
			break;
		// case OpCode.ATTACK_CLIENT:
		// attack(packet,session);
		// break;
		case OpCode.LOGOUT_CLIENT:
			logout(packet, session);
			break;
		case OpCode.TOUCHEXIT_CLIENT:
			touchExit(packet, session, diff);
			break;
		// case OpCode.ANIMATEGET_CLIENT:
		// animateGet(packet,session);
		// break;
		case OpCode.TOUCHNPC_CLIENT:
			touchNpc(packet, session);
			break;
		case OpCode.QUEST_DESC_CLIENT:
			questDesc(packet, session);
			break;
		case OpCode.QUEST_ACCEPT_CLIENT:
			questAccept(packet, session);
			break;
		case OpCode.LOADING_FINISHED_CLIENT:
			loadingFinished(packet, session);
			break;
		case OpCode.QUEST_FINISH_CLIENT:
			questFinished(packet, session);
			break;
		case OpCode.CTNGET_CLIENT:
			ctnGet(packet, session);
			break;
		case OpCode.BAG_CLIENT:
			bag(packet, session);
			break;
		case OpCode.REMOVEITEM_CLIENT:
			removeItem(packet, session);
			break;
		case OpCode.QUEST_PREDESC_CLIENT:
			questPreDesc(packet, session);
			break;
		case OpCode.QUEST_POSTDESC_CLIENT:
			questPostDesc(packet, session);
			break;
		case OpCode.SKILL_LIST_CLIENT:
			skillList(packet, session);
			break;
		case OpCode.SKILL_NAMELIST_CLIENT:
			skillNameList(packet, session);
			break;
		case OpCode.SKILL_ADDPOINT_CLIENT:
			skillAddPoint(packet, session);
			break;
		case OpCode.SKILL_REFRESH_CLIENT:
			skillRefresh(packet, session);
			break;
		case OpCode.QUEST_LIST_CLIENT:
			questList(packet, session);
			break;
		case OpCode.VM_VARIABLE_SYNC_CLIENT:
			syncVariable(packet, session);
			break;
		case OpCode.ITEM_DESC_CLIENT:
			itemDesc(packet, session);
			break;
		case OpCode.ACCOUNT_LOGIN_CLIENT:
			accountLogin(packet, session);
			break;
		case OpCode.ACTOR_LIST_CLIENT:
			actorList(packet, session);
			break;
		case OpCode.ACTOR_CREATE_CLIENT:
			actorCreate(packet, session);
			break;
		case OpCode.QUEST_ABANDON_CLIENT:
			questAbandon(packet, session);
			break;
		case OpCode.NOTIFY_CLIENT:
			notify(packet, session);
			break;
		case OpCode.EQUIP_CLIENT:
			equip(packet, session);
			break;
		case OpCode.UNEQUIP_CLIENT:
			unequip(packet, session);
			break;
		case OpCode.USEITEM_CLIENT:
			useItem(packet, session);
			break;
		case OpCode.PROPERTYPOINT_ADD_CLIENT:
			propertyPointAdd(packet, session);
			break;
		case OpCode.SKILL_DESC_CLIENT:
			skillDesc(packet, session);
			break;
		case OpCode.SKILL_ATTACK_CLIENT:
			skillAttack(packet, session);
			break;
		case OpCode.VERSION_COMPARE_CLIENT:
			versionCompare(packet, session);
			break;
		case OpCode.GETFILE_CLIENT:
			getFile(packet, session);
			break;
		case OpCode.UNIT_INFO_CLIENT:
			unitInfo(packet, session);
			break;
		case OpCode.GATHER_START_CLIENT:
			gatherStart(packet, session);
			break;
		// case OpCode.GATHER_END_CLIENT:
		// gatherEnd(packet, session);
		// break;
		case OpCode.RELOAD_CLIENT:
			reload(packet, session);
			break;
		case OpCode.ADD_FRIEND_CLIENT:
			addFriend(packet, session);
			break;
		case OpCode.DEL_FRIEND_CLIENT:
			delFriend(packet, session);
			break;
		case OpCode.GET_FRIENDLIST_CLIENT:
			getFriendList(packet, session);
			break;
		case OpCode.CHANGE_FRIEND_LOCKSTATE_CLIENT:
			changeFriendLockState(packet, session);
			break;
		case OpCode.CHAT_CLIENT:
			chat(packet, session);
			break;
		case OpCode.CHAT_OPTION_CLIENT:
			chatOption(packet, session);
			break;
		case OpCode.CHAT_NATIVE_CHANGE_CLIENT:
			chatNativeChange(packet, session);
			break;
		case OpCode.PLAYER_INFO_CLIENT:
			playerInfo(packet, session);
			break;
		case OpCode.PARTY_CREATE_CLIENT:
			partyCreate(packet, session);
			break;
		case OpCode.PARTY_INVIT_CLIENT:
			partyInvit(packet, session);
			break;
		case OpCode.PARTY_JOIN_CLIENT:
			partyJoin(packet, session);
			break;
		case OpCode.PARTY_INVIT_OK_CLIENT:
			partyInvitOk(packet, session);
			break;
		case OpCode.PARTY_JOIN_ANSWER_CLIENT:
			partyJoinAnswer(packet, session);
			break;
		case OpCode.PARTY_INVIT_REJECT_CLIENT:
			partyInvitReject(packet, session);
			break;
		case OpCode.PARTY_KICK_CLIENT:
			partyKick(packet, session);
			break;
		case OpCode.PARTY_LEAVE_CLIENT:
			partyLeave(packet, session);
			break;
		case OpCode.MAIL_POST_CLIENT:
			mailPost(packet, session);
			break;
		case OpCode.MAIL_LIST_CLIENT:
			mailList(packet, session);
			break;
		case OpCode.MAIL_CONTENT_CLIENT:
			mailContent(packet, session);
			break;
		case OpCode.MAIL_ATTACHMENT_CLIENT:
			mailAttachment(packet, session);
			break;
		case OpCode.MAIL_DELETE_CLIENT:
			mailDelete(packet, session);
			break;
		case OpCode.TONG_CREATE_CLIENT:
			tongCreate(packet, session);
			break;
		case OpCode.TONG_LIST_CLIENT:
			tongList(packet, session);
			break;
		case OpCode.TONG_INVITE_CLIENT:
			tongInvite(packet, session);
			break;
		case OpCode.TONG_JOIN_CLIENT:
			tongJoin(packet, session);
			break;
		case OpCode.TONG_REJECT_CLIENT:
			tongReject(packet, session);
			break;
		case OpCode.TONG_QUIT_CLIENT:
			tongQuit(packet, session);
			break;
		case OpCode.TONG_SET_SLOGAN_CLIENT:
			tongSetSlogan(packet, session);
			break;
		case OpCode.TONG_PROMOTE_CLIENT:
			tongPromote(packet, session);
			break;
		case OpCode.TONG_KICK_CLIENT:
			tongKick(packet, session);
			break;
		case OpCode.TONG_FORBID_CLIENT:
			tongForbid(packet, session);
			break;
		case OpCode.MAIL_FAVORITE_CLIENT:
			mailFavorite(packet, session);
			break;
		case OpCode.ITEMINFO_CLIENT:
			itemInfo(packet, session);
			break;
		case OpCode.ACTIONBAR_OPTION_CLIENT:
			actionBar(packet, session);
			break;
		case OpCode.SET_ACTIONBAR_OPTION_CLIENT:
			setActionBar(packet, session);
			break;
		case OpCode.ROLL_CLIENT:
			roll(packet, session);
			break;
		case OpCode.PK_INVIT_CLIENT:
			pkInvit(packet, session);
			break;
		case OpCode.PK_REFUSE_CLIENT:
			pkRefuse(packet, session);
			break;
		case OpCode.PK_OK_CLIENT:
			pkOk(packet, session);
			break;
		case OpCode.RELIVE_CLIENT:
			relive(packet, session);
			break;
		case OpCode.SHOP_LIST_CLIENT:
			shopList(packet, session);
			break;
		case OpCode.SHOP_BUY_CLIENT:
			shopBuy(packet, session);
			break;
		case OpCode.SHOP_SELL_CLIENT:
			shopSell(packet, session);
			break;
		case OpCode.ACTOR_DELETE_CLIENT:
			actorDelete(packet, session);
			break;
		case OpCode.BUFF_DESC_CLIENT:
			buffDesc(packet, session);
			break;
		case OpCode.CANCEL_AUTOATTACK_CLIENT:
			cancelAutoAttack(packet, session);
			break;
		case OpCode.ACCOUNT_REG_CLIENT:
			accountReg(packet, session);
			break;
		case OpCode.CANCEL_ATTACK_CLIENT:
			cancelAttack(packet, session);
			break;
		case OpCode.CANCEL_USEITEM_CLIENT:
			cancelUseItem(packet, session);
			break;
		case OpCode.ACCOUNT_QUICK_REG_CLIENT:
			quickReg(packet, session);
			break;
		case OpCode.REPAIR_CLIENT:
			repair(packet, session);
			break;
		case OpCode.CHANGE_NAME_CLIENT:
			changeName(packet, session);
			break;
		case OpCode.CHANGE_SEX_CLIENT:
			changeSex(packet, session);
			break;
		case OpCode.CHANGE_CLASS_CLIENT:
			changeclass(packet, session);
			break;
		case OpCode.CHANGE_FACTION_CLIENT:
			changeFaction(packet, session);
			break;
		case OpCode.QUEST_UNFINISHDESC_CLIENT:
			questUnFinishDesc(packet, session);
			break;
		case OpCode.CHANGE_PASSWORD_CLIENT:
			changePassword(packet, session);
			break;
		case OpCode.OUT_PRISON_CLIENT:
			outPrison(packet, session);
			break;
		case OpCode.GRID_EXCHANGE_CLIENT:
			gridExchange(packet, session);
			break;
		case OpCode.GM_CALL_CLIENT:
			gmCall(packet, session);
			break;
		case OpCode.GET_MOVE_CLIENT:
			getMove(packet, session);
			break;
		case OpCode.FINDPATH_CLIENT:
			findPath(packet, session);
			break;
		case OpCode.EXCHANGE_INVIT_CLIENT:
			exchangeInvit(packet, session);
			break;
		case OpCode.EXCHANGE_INVIT_OK_CLIENT:
			exchangeInvitOk(packet, session);
			break;
		case OpCode.EXCHANGE_INVIT_REFUSE_CLIENT:
			exchangeInvitRefuse(packet, session);
			break;
		case OpCode.EXCHANGE_CANCEL_CLIENT:
			exchangeCancel(packet, session);
			break;
		case OpCode.EXCHANGE_ADDITEM_CLIENT:
			exchangeAddItem(packet, session);
			break;
		case OpCode.EXCHANGE_REMOVEITEM_CLIENT:
			exchangeRemoveItem(packet, session);
			break;
		case OpCode.EXCHANGE_ACCEPT_CLIENT:
			exchangeAccept(packet, session);
			break;
		case OpCode.TITLES_GET_CLIENT:
			titlesGet(packet, session);
			break;
		case OpCode.TITLE_SET_CLIENT:
			titleSet(packet, session);
			break;
		case OpCode.TITLE_REMOVE_CLIENT:
			titleRemove(packet, session);
			break;
		case OpCode.FORGET_SKILL_CLIENT:
			forgetSkill(packet, session);
			break;
		case OpCode.TITLE_LIST_CLIENT:
			titleList(packet, session);
			break;
		case OpCode.TITLE_BUY_CLIENT:
			titleBuy(packet, session);
			break;
		case OpCode.AUCTION_BUY_CLIENT:
			auctionBuy(packet, session);
			break;
		case OpCode.AUCTION_CREATEAUCTION_CLIENT:
			auctionCreate(packet, session);
			break;
		case OpCode.AUCTION_LIST_CLIENT:
			auctionList(packet, session);
			break;
		case OpCode.AUCTION_PUBLISHIED_CLIENT:
			publishiedAuctions(packet, session);
			break;
		case OpCode.GIFT_LIST_CLIENT:
			giftList(packet, session);
			break;
		case OpCode.GIFT_GET_CLIENT:
			giftGet(packet, session);
			break;
		case OpCode.ALPHA_GIFT_CLIENT:
			alphaGift(packet, session);
			break;
		case OpCode.CONFIG_CLIENT:
			config(packet, session);
			break;
		case OpCode.CONFIG_SAVE_CLIENT:
			configSave(packet, session);
			break;
		case OpCode.HORSE_EQUIP_CLIENT:
			horseEqu(packet, session);
			break;
		case OpCode.HORSE_UNEQU_CLIENT:
			horseUnequ(packet, session);
			break;
		case OpCode.HORSE_BAG_CLIENT:
			horseBag(packet, session);
			break;
		// case OpCode.HORSE_ADDPOINT_CLIENT:
		// horseAddPoint(packet, session);
		// break;
		case OpCode.HORSE_CHANGENAME_CLIENT:
			horseChangeName(packet, session);
			break;
		case OpCode.HORSE_THROW_CLIENT:
			horseThrow(packet, session);
			break;
		case OpCode.HORSE_RIDE_CLIENT:
			horseRide(packet, session);
			break;
		case OpCode.HORSE_FEED_CLIENT:
			horseFeed(packet, session);
			break;
		case OpCode.HORSE_FOOD_CLIENT:
			horseFood(packet, session);
			break;
		case OpCode.HORSE_UNRIDE_CLIENT:
			horseUnride(packet, session);
			break;
		case OpCode.GATHER_CANCEL_CLIENT:
			gatherCancel(packet, session);
			break;
		case OpCode.HORSE_PACK_CLIENT:
			horsePacket(packet, session);
			break;
		case OpCode.TITLE_SALARY_CLIENT:
			titleSalary(packet, session);
			break;
		case OpCode.SUITE_CLIENT:
			suiteIndex(packet, session);
			break;
		case OpCode.MARRIAGE_CLIENT:
			marriageRequest(packet, session);
			break;
		case OpCode.MARRIAGE_ANSWER_CLIENT:
			marriageAnswer(packet, session);
			break;
		case OpCode.MARRIAGE_DIVORCE_CLIENT:
			divorce(packet, session);
			break;
		case OpCode.MARRIAGE_DIVORCEANSWER_CLIENT:
			divorceAnswer(packet, session);
			break;
		case OpCode.BATTLEFIELD_SIGNUP_CLIENT:
			battleFieldSignUp(packet, session);
			break;
		case OpCode.BATTLEFIELD_TRAN_CLIENT:
			battleFieldTran(packet, session);
			break;
		case OpCode.HORSE_CHANGE_SKILL_CLIENT:
			horseChangeSkill(packet, session);
			break;
		case OpCode.SKILL_REFRESH_MONEY_CLIENT:
			skillRefreshMoney(packet, session);
			break;
		case OpCode.BATTLEFIELD_QUIT_CLIENT:
			battleFieldQuit(packet, session);
			break;
		case OpCode.DECORATE_ADD_JEWEL_CLIENT:
			addJewel(packet, session);
			break;
		case OpCode.DECORATE_REMOVE_JEWEL_CLIENT:
			removeJewel(packet, session);
			break;
		case OpCode.DECORATE_ADD_HOLE_CLIENT:
			addHole(packet, session);
			break;
//		case OpCode.DECORATE_ADD_MAX_HOLE_CLIENT:
//			addMaxHole(packet, session);
//			break;
		case OpCode.DECORATE_MERGE_JEWEL_CLIENT:
			mergeJewel(packet, session);
			break;
		case OpCode.DECORATE_GET_CONFIG_CLIENT:
			getJewelConfig(packet, session);
			break;
		case OpCode.CHINARUN_CLIENT:
			chinarun(packet, session);
			break;
		case OpCode.FORMULA_INFO_CLIENT:
			formulaIndex(packet, session);
			break;
		case OpCode.PRODUCE_CLIENT:
			produce(packet, session);
			break;
		case OpCode.FORMULA_LIST_CLIENT:
			formulaList(packet, session);
			break;
		case OpCode.ISHOP_LIST_CLIENT:
			ishopList(packet, session);
			break;
		case OpCode.FORMULA_DELETE_CLIENT:
			deleteFormula(packet, session);
			break;
		case OpCode.ACTIVATIONCODE_CLINET:
			activationCode(packet, session);
			break;
		case OpCode.BAG_ARRANGE_CLIENT:
			bagArrange(packet, session);
			break;
		case OpCode.TELEPORT_LIST_CLIENT:
			teleportList(packet, session);
			break;
		case OpCode.TELEPORT_CLIENT:
			teleport(packet, session);
			break;
		case OpCode.START_ENHANCE_MONEY_CLIENT:
			startEnhanceMoney(packet, session);
			break;
		case OpCode.NATURAL_ENHANCE_MONEY_CLIENT:
			naturalEnhanceMoney(packet, session);
			break;
		case OpCode.NATURAL_ENHANCE_CLIENT:
			naturalEnhance(packet, session);
			break;
		case OpCode.STAR_ENHANCE_CLIENT:
			startEnhance(packet, session);
			break;
		case OpCode.DEPOT_YESORNO_CLIENT:
			isOn(packet, session);
			break;
		case OpCode.DEPOT_REQUEST_CLIENT:
			depotrequesthandle(packet, session);
			break;
		case OpCode.DEPOT_ARRANGE_CLIENT:
			depotArrange(packet, session);
			break;
		case OpCode.DEPOT_GETFROMBAG_CLIENT:
			getItemFromBagToDepot(packet, session);
			break;
		case OpCode.DEPOT_GETFROMDEPOT_CIENT:
			getItemFromDepotToBag(packet, session);
			break;
		case OpCode.CHINAJOY_GIFT_CLIENT:
			chinaJoyGift(packet, session);
			break;
		case OpCode.CHINAJOY_COUNT_CLIENT:
			chinaJoyCount(packet, session);
			break;
		case OpCode.NATION_SLOGAN_CLIENT:
			nationSlogan(packet, session);
			break;
		case OpCode.NATION_INFO_CLIENT:
			nationInfo(packet, session);
			break;
		case OpCode.TOPLIST_KILLCOUNT_CLINET:
			topKillCount(packet, session);
			break;
		case OpCode.TOPLIST_WEEKRANK_CLIENT:
			topWeekRank(packet, session);
			break;
		case OpCode.INSTANCE_CLEAR_CLIENT:
			instanceClear(packet, session);
			break;
		case OpCode.CANDIDATE_SIGNUP_CLIENT:
			voteSignUp(packet, session);
			break;
		case OpCode.CONTRIBUTECREDIT_CLIENT:
			contributeCredit(packet, session);
			break;
		case OpCode.CANDIDATE_VOTE_CLIENT:
			vote(packet, session);
			break;
		case OpCode.CANDIDATE_LIST_CLIENT:
			candidateList(packet, session);
			break;
		case OpCode.COLLECT_LAUNCH_CLIENT:
			collectLaunch(packet, session);
			break;
		case OpCode.COLLECT_CLIENT:
			collect(packet, session);
			break;
		case OpCode.NATION_FORBID_CLINET:
			nationForbid(packet, session);
			break;
		case OpCode.NATION_PUNISH_CLIENT:
			nationPunish(packet, session);
			break;
		case OpCode.NATION_OFFICER_CLIENT:
			nationOfficer(packet, session);
			break;
		case OpCode.OFFICER_LIST_CLIENT:
			officerList(packet, session);
			break;
		case OpCode.ACCOUNTBINDING_STATUS_CLIENT:
			accountBindingStatus(packet, session);
			break;
		case OpCode.ACCOUNTBINDING_CLIENT:
			accountBinding(packet, session);
			break;
		case OpCode.NATION_BATTLE_TELE_CLIENT:
			nationBattleFieldTele(packet, session);
			break;
		case OpCode.NATION_DECLARE_LIST_CLIENT:
			nationDeclareList(packet, session);
			break;
		case OpCode.NATION_DECLARE_ACCEPT_CLIENT:
			nationDeclareAccept(packet, session);
			break;
		case OpCode.NATION_DECLARE_CLIENT:
			nationDeclare(packet, session);
			break;
		case OpCode.NATION_REL_CLIENT:
			nationRel(packet, session);
			break;
		case OpCode.BBS_LIST_CLIENT:
			bbsList(packet, session);
			break;
		case OpCode.BBS_CONTENT_CLIENT:
			bbsContent(packet, session);
			break;
		case OpCode.MARRIAGE_INFO_CLIENT:
			marriageInfo(packet, session);
			break;
		case OpCode.NATION_QUEST_REQUEST_CLIENT:
			nationQuestRequest(packet, session);
			break;
		case OpCode.NATION_QUEST_CLIENT:
			nationQuest(packet, session);
			break;
		case OpCode.NATION_SKILL_LIST_CLIENT:
			nationSkillList(packet, session);
			break;
		case OpCode.NATION_SKILL_STUDY_CLIENT:
			enhanceSkills(packet, session);
			break;
		case OpCode.NATION_SKILL_DESC_CLIENT:
			nationSkillDesc(packet,session);
			break;
		case OpCode.PLAYER_RATE_CLIENT:
			playerRate(packet,session);
			break;
		case OpCode.NPC_LIST_CLIENT:
			npcList(packet,session);
			break;
		case OpCode.NPC_DESC_CLIENT:
			npcDesc(packet,session);
			break;
		case OpCode.EXCHANGE_EXP_CLIENT:
			exchangeExp(packet, session);
			break;
		case OpCode.QUERY_OFFLINE_EXP_CIENT:
			queryOfflineExp(packet, session);
			break;
		case OpCode.HORSE_AGENT_CLIENT:
			addHorseAgent(packet, session);
			break;
		case OpCode.CANCEL_AGENTHORSE_CLIENT:
			cancelAgentHorse(packet, session);
			break;
		case OpCode.MAIL_OBSOLETE_DELETE_CLIENT:
			mailObsoleteDelete(packet,session);
			break;
		case OpCode.LOCK_HORSESKILL_CLIENT:
			lockHorseSkill(packet, session);
			break;
		case OpCode.UNLOCK_HORSESKILL_CLIENT:
			unlockHorseSkill(packet, session);
			break;
		case OpCode.PARTY_TRANSFER_LEADER_CLIENT:
			partyTransLeader(packet, session);
			break;
		case OpCode.WORLD_TELEPORT_CLIENT:
			worldTeleport(packet, session);
			break;
		case OpCode.GET_FILE_CLIENT:
			get_File(packet,session);
			break;
		case OpCode.NATIONBATTLE_MINLEVEL_CLIENT:
			setNationBattleMinLevel(packet, session);
			break;
		case OpCode.IMONEYCARD_CREATE_CLIENT:
			imoneyCard(packet, session);
			break;
		case OpCode.AUTO_NATURALENHANCE_CLIENT:
			autoNaturalEnhance(packet, session);
			break;
		case OpCode.KING_TAXRATE_CLIENT:
			kingTax(packet, session);
			break;
		case OpCode.NATION_CONVOY_CLIENT:
			nationConvoy(packet, session);
			break;
		case OpCode.EQUIPEMENT_MARK_CLIENT:
			equMark(packet, session);
			break;
		case OpCode.ANSWER_CLIENT:
			answer(packet, session);
			break;
		case OpCode.QME_PAY_CLIENT:
			qmePay(packet, session);
			break;
		case OpCode.QME_QUERY_BALANCE_CLIENT:
			qmeQueryBalance(packet, session);
			break;
		case OpCode.HORSEBAG_EXTEND_CLIENT:
			horsebagExtend(packet, session);
			break;
		case OpCode.BOSS_SCOREBOARD_CLIENT:
			bossScoreBoard(packet, session);
			break;
		case OpCode.BOSS_TIMEBOARD_CLIENT:
			bossTimeBoard(packet, session);
			break;
		case OpCode.HORSE_EQUIPMENT_UNBIND_CLIENT:
			horseequipUnbind(packet, session);
			break;
		case OpCode.NATION_SKILL_ITEM_CLIENT:
			getNationSkillItem(packet, session);
			break;
		case OpCode.WORLD_SHOUT_CLIENT:
			worldShout(packet, session);
			break;
		case OpCode.TONG_CONTRIBUTE_CLIENT:
			tongContribute(packet, session);
			break;
		case OpCode.TONG_SKILL_LIST_CLIENT:
			tongSkillList(packet, session);
			break;
		case OpCode.TONG_SKILL_DESC_CLIENT:
			tongSkillDesc(packet, session);
			break;
		case OpCode.TONG_SKILL_STUDY_CLIENT:
			tongSkillStudy(packet, session);
			break;
		case OpCode.APP_STORE_CHARGE2_CLIENT:
			appStoreCharge2(packet, session);
			break;
		case OpCode.APP_STORE_CHARGE3_CLIENT:
			appStoreCharge3(packet, session);
			break;
		case OpCode.APP_STORE_LIST_PRODUCT2_CLIENT:
			appStoreListProduct2(packet, session);
			break;
		case OpCode.CMCC_CHARGE_CLIENT:
			cmccCharge(packet, session);
			break;
		case OpCode.PHONE_NOTIFY_CLIENT:
			phoneNotify(packet, session);
			break;
		case OpCode.IBUY_HISTORY_CLIENT:
			ibuyHistory(packet, session);
			break;
		case OpCode.CMCC_CHARGE_TIME_CLIENT:
			cmccChargeTime(packet, session);
			break;
		case OpCode.GETITEM_FROM_NPC_CLIENT:
			getItemFromNpc(packet, session);
			break;
		case OpCode.CMCC_ISHOP_LIST_CLIENT:
			cmccIShop(packet, session);
			break;
		case OpCode.CMCC_PUSHDOWNLOADOK_CLINET:
			cmccPushDownloadOk(packet, session);
			break;
		case OpCode.CMCC_ISHOP_BUY_CLIENT:
			cmccShopBuy(packet, session);
			break;
		case OpCode.TONG_INFO_CLIENT:
			tongInfo(packet, session);
			break;
		case OpCode.TONG_BATTLE_APPLY_CLIENT:
			tongBattleApply(packet, session);
			break;
		case OpCode.TONG_BATTLEAPPLY_LIST_CLENT:
			tongBattleApplyList(packet, session);
			break;
		case OpCode.TONG_BATTLEBID_CLIENT:
			tongBattleBid(packet, session);
			break;
		case OpCode.TONG_BATTLETIME_CLIENT:
			tongBattleTime(packet, session);
			break;
		case OpCode.TONG_BATTLE_TRANSPORT_CLIENT:
			tongBattleTransport(packet, session);
			break;
		case OpCode.TONGBATTLE_BUY_CLIENT:
			tongBattleBuy(packet, session);
			break;
		case OpCode.TONG_QUEST_REQUEST_CLIENT:
			tongQuestRequest(packet, session);
			break;
		case OpCode.TONG_QUEST_CLIENT:
			tongQuest(packet, session);
			break;
		case OpCode.TONG_BATTLE_TAG_CLIENT:
			tongBattleTag(packet, session);
			break;
		case OpCode.TONGBATTLE_WINNER_INFO_CLIENT:
			tongBattleWinnerInfo(packet, session);
			break;
		case OpCode.TONG_BATTLE_TAX_CLIENT:
			tongBattleTax(packet, session);
			break;
		case OpCode.TONG_BATTLE_MAKETAX_CLIENT:
			tongBattleMakeTax(packet, session);
			break;
		case OpCode.TONG_BATTLE_UNTAG_CLIENT:
			tongBattleUnTag(packet, session);
			break;
		case OpCode.TONG_BATTLE_ABANDON_CLIENT:
			tongBattleAbandon(packet,session);
			break;
		case OpCode.TONG_BATTLE_GETEXP_CLIENT:
			tongBattleGetExp(packet, session);
			break;
		case OpCode.QME_CANPAY_CLIENT:
			qmeCanPay(packet, session);
			break;
		case OpCode.AUTO_ADDHOLE_CLIENT:
			autoAddHole(packet, session);
			break;
		case OpCode.SPLITITEM_CLIENT:
			splitItem(packet, session);
			break;
		case OpCode.GLOBAL_NPC_LIST_CLIENT:
			globalNpcList(packet,session);
			break;
		case OpCode.GLOBAL_NPC_DESC_CLIENT:
			globalNpcDesc(packet,session);
			break;
		case OpCode.DEPOT_EXCHANGE_CLIENT:
			depotExchange(packet, session);
			break;
		case OpCode.DEPOT_SPLITITEM_CLIENT:
			depotSplitItem(packet, session);
			break;
		case OpCode.EXTEND_BAG_CLIENT:
			extendBag(packet, session);
			break;
		case OpCode.EXTEND_BAG_PRICE_CLIENT:
			extendBagPrice(packet, session);
			break;
		case OpCode.EXTEND_DEPOT_PRICE_CLIENT:
			extendDepotPrice(packet, session);
			break;
		case OpCode.EXTEND_DEPOT_CLIENT:
			extendDepot(packet, session);
			break;
		case OpCode.GET_CONFIG_CLIENT:
			getConfig(packet, session);
			break;
		case OpCode.SET_CONFIG_CLIENT:
			setConfig(packet, session);
			break;
		case OpCode.TOPLIST_LEVEL_CLIENT:
			topLevelRank(packet, session);
			break;
		case OpCode.PERSONAL_ACHIEVEMENT_CLIENT:
			achievementList(packet,session);
			break;
		case OpCode.PERSONAL_ACHIEVEMENT_DETAIL_CLIENT:
			detailAchievementList(packet,session);
			break;
		case OpCode.CLIENTBBS_LOOK_OVER_CLIENT:
			lookOverBBS(packet,session);
			break;
		case OpCode.CLIENTBBS_SAVE_CLIENT:
			saveBBS(packet,session);
			break;
		case OpCode.ENEMY_POSITION_CLIENT:
			getPosition(packet,session);
			break;
		case OpCode.TONG_RENAME_CLIENT:
			renameTong(packet, session);
			break;
		case OpCode.BEAUTYPARADE_LIST_CLINET:
			beautyList(packet, session);
			break;
		case OpCode.BEAUTYPARADE_SIGNUP_CLIENT:
			beautySignUp(packet, session);
			break;
		case OpCode.BEAUTYPARADE_VOTE_CLIENT:
			beautyVote(packet, session);
			break;
		case OpCode.BEAUTY_FRIEND_LIST_CLIENT:
			beautyFriendList(packet, session);
			break;
		case OpCode.WEDDING_OPEN_CLIENT:
			weddingOpen(packet, session);
			break;
		case OpCode.WEDDING_LIST_CLIENT:
			weddingList(packet, session);
			break;
		case OpCode.WEDDING_JOINWEDDING_CLIENT:
			joinWedding(packet, session);
			break;
		case OpCode.WEDDING_SIGNIN_CLIENT:
			weddingSignIn(packet, session);
			break;
		case OpCode.WEDDING_SIGNINLIST_CLIENT:
			weddingSignList(packet,session);
			break;
		case OpCode.WEDDING_GIFT_CLINT:
			weddingSendGift(packet,session);
			break;
		case OpCode.WEDDING_KICK_CLINT:
			weddingKickGuest(packet,session);
			break;
		case OpCode.WEDDING_GETEXP_CLIENT:
			weddingGetExp(packet,session);
			break;
		case OpCode.FAME_ADDINFO_CLIENT:
			fameAddInfo(packet,session);
			break;
		case OpCode.CHANGE_PLAYER_INFO_CLIENT:
			changePlayerInfo(packet,session);
			break;
		case OpCode.DUEL_SIGNUP_CLIENT:
			duelSignUp(packet, session);
			break;
		case OpCode.HANDIN_LETTER_CLIENT:
			handInLetter(packet,session);
			break;
		case OpCode.DUEL_SCORE_CLIENT:
			duelScore(packet, session);
			break;
		case OpCode.DUEL_GETTITLE_CLIENT:
			duelGetTitle(packet, session);
			break;
		case OpCode.CHARGE_RECORD_CLIENT:
			chargeRecord(packet, session);
			break;
		case OpCode.CARD_LIST_CLIENT:
			cardList(packet,session);
			break;
		case OpCode.CARD_DETAILLIST_CLIENT:
			cardDetailList(packet,session);
			break;
		case OpCode.CARD_SHOWNAME_CLIENT:
			showCardName(packet,session);
			break;
		case OpCode.CARD_MERGE_CLINET:
			mergeCard(packet,session);
			break;
		case OpCode.TOWERDEFEND_SIGNUP_CLIENT:
			towerDefendSignUp(packet, session);
			break;
		case OpCode.CARD_COLLECTAGAIN_CLIENT:
			cardCollectAgain(packet,session);
			break;
		case OpCode.CARD_COLLECTION_CLIENT:
			cardCollect(packet,session);
			break;
		case OpCode.SHOP_QUICK_BUY_CLIENT:
			shopQuickBuy(packet, session);
			break;
		case OpCode.CARD_LIST_4SHEET_CLIENT:
			cardList4Sheet(packet, session);
			break;
		case OpCode.SHOP_TOPLIST_CLIENT:
			shopTopList(packet, session);
			break;
		case OpCode.INDICATOR_AREA_TASK_CLIENT:
			actLeadersQuery(packet, session);
			break;
		case OpCode.DELETE_WHITE_EQUIPMENT_CLIENT:
			sellWhiteEquips(packet,session);
			break;
		case OpCode.REPORT_CLIENT:
			reportPlayer(packet, session);
			break;
		case OpCode.CARD_INFO_CLIENT:
			cardInfo(packet, session);
			break;	
		case OpCode.NPC_EXCHANGE_CLIENT:
			exchangeItemFromNpc(packet, session);
			break;
		case OpCode.APPOINTITEM_DESC_CLIENT:
			getAppointItemDesc(packet, session);
			break;
		case OpCode.ENHANCE_EQUIP_REQUEST_CLIENT:
			enhanceRequest(packet,session);
			break;
		case OpCode.ENHANCE_EQUIP_CLIENT:
			enhanceEquip(packet,session);
			break;
		case OpCode.TITLE_SHOW_CLIENT:
			titleShow(packet,session);
			break;
		case OpCode.ASSOCIATION_CREATE_CLIENT:
			associationCreate(packet, session);
			break;
		case OpCode.ASSOCIATION_EXECISE_CLIENT:
			associationExcise(packet, session);
			break;
		case OpCode.ASSOCIATION_ANSWER_CLIENT:
			answerAssociationInvite(packet, session);
			break;
		case OpCode.ASSOCIATION_LIST_CLIENT:
			associationList(packet, session);
			break;
		case OpCode.BUFF_DESC_BYID_CLIENT:
			starBuffDesc(packet, session);
			break;
		case OpCode.ANTI_PLUG_CLIENT:
			antiPlug(packet, session);
			break;
		case OpCode.CHARGE_INFO_CLIENT:
			chargeInfo(packet,session);
			break;
		case OpCode.CLEARANCESALE_SIGN_CLIENT:
			clearanceSign(packet, session);
			break;
		case OpCode.CLEARSALE_LIST_CLIENT:
			clearanceList(packet, session);
			break;
		case OpCode.FETCH_GIFT_CLIENT:
			fetchGift(packet,session);
			break;
		case OpCode.ASSOCIATION_RENAME_CLIENT:
			associationReName(packet, session);
			break;
		case OpCode.ACHIEVEMENT_GIFT_CLIENT:
			getAchievementGift(packet,session);
			break;
		case OpCode.ACCOUNTDEPOT_CHECK_CLIENT:
			accountDepotCheck(packet,session);
			break;
		case OpCode.ACCOUNTDEPOT_REQUEST_CLIENT:
			accountDepotRequest(packet,session);
			break;
		case OpCode.ACCOUNTDEPOT_GETFROMBAG_CLIENT:
			getFromBagToAccDepot(packet,session);
			break;
		case OpCode.ACCOUNTDEPOT_GETFROMDEPOT_CIENT:
			getFromDepotToBag(packet,session);
			break;
		case OpCode.ACCOUNTDEPOT_ARRANGE_CLIENT:
			arrangeAccountDepot(packet,session);
			break;
		case OpCode.ATTENDANT_FOLLOW_CLIENT:
			attendantFollow(packet, session);
			break;
		case OpCode.ATTENDANT_CANCELFOLLOW_CLIENT:
			cancelAttendantFollow(packet, session);
			break;
		case OpCode.ATTENDANT_BAG_CLIENT:
			attendantBagInfo(packet, session);
			break;
		case OpCode.ATTENDANT_EQUIP_CLIENT:
			attendantEquip(packet, session);
			break;
		case OpCode.ATTENDANT_UNEQUIP_CLIENT:
			attendantUnequip(packet, session);
			break;
		case OpCode.ATTENDANT_RENAME_CLIENT:
			attendantReName(packet, session);
			break;
		case OpCode.ATTENDANT_LIGHTSKILL_CLIENT:
			attendantLightSkill(packet, session);
			break;
		case OpCode.ATTENDANT_DELETE_CLIENT:
			attendantDelete(packet, session);
			break;
		case OpCode.ATTENDANT_ADDLOYAL_CLIENT:
			attendantAddLoyal(packet, session);
			break;
		case OpCode.ATTENDANT_ADDSKILL_CLIENT:
			attendantAddSkill(packet, session);
			break;
		case OpCode.MERGE_JEWEL_REQUEST_CLIENT:
			mergeJewelRequest(packet, session);
			break;
		case OpCode.AUTO_MERGE_JEWEL_CLIENT:
			autoMergeJewel(packet, session);
			break;
		case OpCode.REMOVE_ALLJEWEL_RESOURCE_CLIENT:
			removeAllJewelRequest(packet, session);
			break;
		case OpCode.DECORATE_REMOVE_ALLJEWEL_CLIENT:
			removeAllJewel(packet, session);
			break;
		case OpCode.HORSE_UP_SKILL_CLIENT:
			horseUpSkillLevel(packet, session);
			break;
		case OpCode.FUXING_BAG_CLIENT:
			useFuXingBag(packet, session);
			break;
		case OpCode.PLAYER_ACTION_CLIENT:
			logPlayerAction(packet, session);
			break;
		case OpCode.VIRTNAM_CHARGE_CLIENT:
			vietNamCharge(packet, session);
			break;
		case OpCode.PLAYER_SET_FIND_PATH_CLIENT:
			setPlayerFindingPath(packet, session);
			break;
		case OpCode.HORSE_ACTIVE_CLIENT:
			horseActive(packet, session);
			break;
		case OpCode.AUCTION_DELETE_CLIENT:
			deleteAuction(packet,session);
			break;
		case OpCode.VIETNAM_TELCO_MOBIPHONE_CHARGE_CLIENT:
			vietNamTelcoCharge(packet, session);
			break;
		case OpCode.MAIL_RECOVER_CLIENT:
			mailRecover(packet,session);
			break;
		case OpCode.MAIL_GETALLATTACH_CLIENT:
			getAllMailAttach(packet, session);
			break;
		case OpCode.ATTENDANT_LEARNSKILL_CLIENT:
			attendantLearnSkill(packet, session);
			break;
		case OpCode.TONG_REMOVE_CLIENT:
			tongRemove(packet, session);
			break;
		case OpCode.START_7_BUFF_DESC_CLIENT:
			star_7_desc(packet, session);
			break;
		case OpCode.WELFARE_LIST_CLIENT:
			welfareList(packet, session);
			break;
		case OpCode.WELFARE_REWARD_CLIENT:
			welfareReward(packet,session);
			break;
		case OpCode.DECORATE_UPGRADE_JEWEL_CLIENT:
			upgradeJewel(packet, session);
			break;
		case OpCode.DUELMETTING_SIGNUP_CLIENT:
			duelMettingSign(packet, session);
			break;
		case OpCode.DUELMETTING_PLAYERINFO_ClIENT:
			duelMettingPalyerInfo(packet, session);
			break;
		case OpCode.OTHER_PLAYER_SKILL_LIST_CLIENT:
			otherPlayerSkillList(packet,session);
			break;
		case OpCode.PLAYER_LOCK_EXP_CLIENT:
			playerLockExp(packet,session);
			break;
//		case OpCode.PLAYER_APPRENTICE_CLIENT:
////			playerApprentice(packet,session);
//			break;
		case OpCode.PLAYER_APPRENTICELIST_CLIENT:
			playerApprenticeList(packet,session);
			break;
		case OpCode.PLAYER_REMOVEAPPRENTICE_CLIENT:
			removeApprenticeRelation(packet,session);
			break;
		case OpCode.MARRAY_BANLANG_CLIENT:
			applyBanLang(packet, session);
			break;
		case OpCode.MARRAY_BANLANG_REQUEST_CLIENT:
			applyBanLangRequest(packet, session);
			break;
		case OpCode.WEDDING_QUESTION_CLIENT:
			applyBeginQuestion(packet, session);
			break;
		case OpCode.WEDDING_QUESTION_RESPONSE_CLIENT:
			responBeginQuestion(packet, session);
			break;
		case OpCode.WEDDING_QUESTION1_CLIENT:
			weddingAnswerQuestion(packet, session);
			break;
		case OpCode.WEDDING_QUESTION_REFUSE_CLIENT:
			refuseQuestion(packet, session);
			break;
		case OpCode.CARD_ADDTOEQU_CLIENT:
			cardAddToEqup(packet, session);
			break;
		case OpCode.CARD_ADDHOLE_CLIENT:
			cardAddHole(packet, session);
			break;
		case OpCode.CARD_ALLLIST_CLIENT:
			allCardList(packet, session);
			break;
		case OpCode.APPRENTICE_INVIT_CLIENT:
			apprenticeInvit(packet,session);
			break;
		case OpCode.APPRENTICE_INVIT_REJECT_CLIENT:
			apprenticeReject(packet,session);
			break;
		case OpCode.APPRENTICE_INVIT_OK_CLIENT:
			apprenticeInvitOk(packet,session);
			break;
		case OpCode.CARD_ADDENERGY_CLIENT:
			cardAddEnergy(packet, session);
			break;
		case OpCode.VIETNAM_VTC_CHARGE_CLIENT:
			vteCharge(packet, session);
			break;
		case OpCode.ASK_TOGET_HORSEEXP_CLIENT:
			askToGetHorseExp(packet, session);
			break;
		case OpCode.USEWHOLE_TOGET_HORSEEXP_CLIENT:
			useToGetHorseExp(packet, session);
			break;
		case OpCode.DECORATE_ADD_MAXHOLE_ALL_CLIENT:
			addAllMaxHole(packet,session);
			break;
		case OpCode.CARD_PRORERTY_ALLLIST_CLIENT:
			cardPropertyType(packet,session);
			break;
		case OpCode.CLIENT_CHARGE_GET_ORDER_CLIENT:
		{
			ClientChargeService service = (ClientChargeService)Server.server.getServiceRegistry().getService(ClientChargeService.class);
			service.generateOrder(session, packet);
			break;
		}
		case OpCode.CLIENT_CHARGE_CHECK_RECEIPT_CLIENT:
		{
			ClientChargeService service = (ClientChargeService)Server.server.getServiceRegistry().getService(ClientChargeService.class);
			service.checkReceipt(session, packet);
			break;
		}
		case OpCode.CLIENT_GUID_RESULT_CLIENT:
			clientGuidResult(packet, session);
			break;
		case OpCode.CMCC_ANDROID_SMS_BUY_REQ_CLIENT:
			cmccAndroidSmsBuy(packet, session);
			break;
		case OpCode.CARD_AUTOADDENERGY_CLIENT:
		    cardAutoAddEnergy(packet,session);
		    break;
		case OpCode.EFFECT_JEWEL_GET_CLIENT:
			effectJewelGet(packet,session);
			break;
		case OpCode.CARD_PUNCH_INFO_CLIENT:
			cardPunchInfo(packet, session);
			break;
		case OpCode.CARD_PUNCH_CLIENT:
			cardPunch(packet, session);
			break;
		case OpCode.BIND_WEIBO_CLIENT:
			bindWeibo(packet,session);
			break;
		case OpCode.UNBIND_WEIBO_CLIENT:
			unBindWeibo(packet,session);
			break;
		case OpCode.LOGIN_WEIBO_CLIENT:
			loginWeibo(packet,session);
			break;
		case OpCode.SEND_WEIBO_CLIENT:
			sendWeibo(packet,session);
			break;
		case OpCode.BINDED_WEIBO_CLIENT:
			bindedWeibo(packet,session);
			break;
		case OpCode.QUICKREGISTRATE_WEIBO_CLIENT:
			weiboQuickRegistrate(packet,session);
			break;
		case OpCode.TRANSFORM_WEIBO_CLIENT:
			transformWeibo(packet,session);
			break;
		case OpCode.CYCLE_INSTANCE_GO_CLIENT:
			cycleInstanceGoMap(packet, session);
			break;
		case OpCode.MONTH_PAY_CLIENT:
			monthPay(packet,session);
			break;
		case OpCode.MONTH_PAY_LIST_CLIENT:
			monthPayList(packet,session);
			break;
		case OpCode.VOW_CLIENT:
			vow(packet, session);
			break;
		case OpCode.GET_AWARD_ITEMS_CLIENT:
			getAwardItems(packet,session);
			break;
		case OpCode.GET_AWARD_CLIENT:
			getAward(packet,session);
			break;
		case OpCode.GET_AWARDITEM_CLIENT:
			getAwardItem(packet,session);
			break;
		case OpCode.PAYFORME_INVIT_CLIENT:
			askForGift(packet,session);
			break;
		case OpCode.PAYFORME_INVIT_OK_CLIENT:
			payInviteOk(packet,session);
			break;
		case OpCode.PAYFORME_REJECT_CLIENT:
			payReject(packet,session);
			break;
		case OpCode.RANKING_RONGYUTA_CLIENT:
			cycleInstanceRanking(packet,session);
			break;
		case OpCode.HANGAME_INVITE_FRIENDS_CLIENT:
		    hangameInviteFriends(packet, session);
		    break;
		case OpCode.TONG_SEND_APPLY_CLIENT:
			requestTongList(packet,session);
			break;
		case OpCode.TONG_APPLY_JOIN_CLIENT:
			applyJoinTong(packet,session);
			break;
		case OpCode.TONG_AUTO_APPLY_STATUS_CLIENT:
			changeApplyStatus(packet,session);
			break;
		case OpCode.TONG_REQUEST_EXIT_CLIENT:
			requestExitTong(packet,session);
			break;
		case OpCode.TONG_USER_BAIBAO_BOX_CLIENT:
			tongUseBox(packet,session);
			break;
		case OpCode.TONG_GET_BAIBAO_CLIENT:
			tongGetBaiBaoItem(packet,session);
			break;
		case OpCode.TONG_LEVELUP_SKILL_CLIENT:
			tongLeveUpSkill(packet,session);
			break;
		case OpCode.TONG_SHOP_LIST_CLIENT:
			tongShopList(packet,session);
			break;
		case OpCode.TONG_SHOP_BUY_CLIENT:
			tongShopBuy(packet,session);
			break;
		case OpCode.VIETNAM_VIETTEL_CHARGE_CLIENT:
			viettelCharge(packet, session);
			break;
		case OpCode.SHOW_DECORADE_PRICE_CLIENT:
			showAddHole(packet,session);
			break;
		case OpCode.CLIENT_DIRECTORY_NORMAL_LIST_CLIENT:
			clientDirectoryNomalList(packet, session);
			break;
		case OpCode.CLIENT_DIRECTORY_TIME_LIST_CLIENT:
			clientDirectoryTimeList(packet, session);
			break;
		case OpCode.CLIENT_DIRECTORY_BUBBLE_LIST_CLIENT:
			clientDirectoryBubbleList(packet, session);
			break;
		case OpCode.SALARY_INFO_CLIENT:
			salaryInfo(packet,session);
			break;
		case OpCode.SHOPITEM_PRICE_CLIENT:
			getShopItemPrice(packet, session);
			break;
		case OpCode.HORSE_CHANGE_CLIENT:
			horseChange(packet, session);
			break;
		case OpCode.REMOVE_HORSE_CHANGE_CLIENT:
			removehorseChange(packet, session);
			break;
		case OpCode.HORSE_FIX_CLIENT:
			horseFix(packet, session);
			break;
		case OpCode.HORSE_FIXFAILURE_CLIENT:
			horseFixFail(packet, session);
			break;
		case OpCode.HORSE_SKILL_CONFIRE_CLIENT:
			horseSkillConfire(packet, session);
			break;
		case OpCode.EVALUATION_CONFIRE_CLIENT:
			creatNaturalEnhance(packet, session);
			break;
		case OpCode.VALENTINE_RANKING_CLIENT:
			valentineList(packet, session);
			break;
		case OpCode.PLAYER_REBUILDPROPERTY_CLIENT:
 			refreshPropertyPoint(packet,session);
			break;
		case OpCode.PLAYER_READBOOK_CLIENT:
			readBook(packet,session);
			break;
		case OpCode.PLAYER_BOOKLIST_CLIENT:
			getBookList(packet,session);
			break;
//		case OpCode.PLAYER_PAUSEREAD_CLIENT:
//			pauseReadBook(packet,session);
//			break;
		case OpCode.PLAYER_PAYFORREDA_CLIENT:
			payForRead(packet,session);
			break;
		case OpCode.FIVEELEMENT_TRANSFORM_CLIENT:
			fiveElementTran(packet,session);
			break;
		case OpCode.ATTENDANT_IS_DELCREDIT_ClIENT:
			delDelCredit(packet, session);
			break;
		case OpCode.ACCEPT_ESCORT_QUEST_CLIENT:
			acceptEscortQuest(packet, session);
			break;
		case OpCode.ACCEPT_REFRESH_ESCORT_CLIENT:
			reFreshEscort(packet, session);
			break; 
		case OpCode.START_ESCORT_CLIENT:
			acceptStartEscort(packet, session);
			break;
		case OpCode.REVIEW_BOOKDEC_ClIENT:
			reviewBookDec(packet,session);
			break;
		case OpCode.BOOKTIME_PAY_ClIENT:
			bookPay(packet,session);
			break;
		case OpCode.QUICKDEC_BOOKTIME_ClIENT:
			quickDecBookTime(packet,session);
			break;
		case OpCode.INSTANCE_SWEEP_CLIENT:
			instanceSweep(packet,session);
			break;
		case OpCode.INSTANCE_SWEEPLIST_CLIENT:
			instanceSweepList(packet,session);
			break;
		case OpCode.DEC_SWEEPTIME_CLIENT:
			decSweepTime(packet,session);
			break;
		case OpCode.STAR_PROMOTE_CLIENT:
			starPromote(packet,session);
			break;
		case OpCode.STARPROMOTE_APPLYRATE_CLIENT:
			starPromoteRate(packet,session);
			break;
		case OpCode.CARD_PRAY_CLIENT:
			prayCards(packet, session);
			break;
		case OpCode.CARD_UPGRADE_CLIENT:
			cardUpGrade(packet, session);
			break;
		case OpCode.CARD_ADDTOEQUINDEX_CLIENT:
			cardEquip(packet, session);
			break;
		case OpCode.CARD_REMOVEEXP_CLIENT:
			cardRemoveExp(packet, session);
			break;
		case OpCode.CARD_UNEQUIPCARD_CLIENT:
			cardUnEquip(packet, session);
			break;
		case OpCode.CARD_PRAY_INFO_CLIENT:
			cardPrayInfo(packet, session);
			break;
		case OpCode.NOTIFICATION_BIND_CLIENT:
			notificationBind(packet,session);
			break;
		case OpCode.LIMEI_ACTIVATION_CLIENT:
			liMeiActivation(packet, session);
			break;
		case OpCode.CARDEXP_ADD_CLIENT:
			cardExpBoard(packet,session);
			break;
		case OpCode.NEW_GETFILE_CLIENT:
			getFileNew(packet, session);
			break;
		case OpCode.ACCOUNT_LOGIN_UC_ANDROID_CLIENT:
			accountLoginUc(packet, session);
			break;
		case OpCode.PRAY_IMONEY_CLIENT:
			getPrayPrice(packet,session);
			break;
		case OpCode.ACCOUNT_LOGIN_YUNYOU_ANDROID_CLIENT:
			yunyouAccountLogin(packet, session);
			break;
		case OpCode.YUNYOU_BUYIMONEY_CLIENT:
			yunyouBuyImoney(packet, session);
			break;
		case OpCode.CHARGE_DOWNJOY_CLIENT:
			downJoyCharge(packet, session);
			break;
		case OpCode.CMCC_CHARGE_NEW_CLIENT:
			cmccChargeNew(packet, session);
			break;
		case OpCode.GAMBLE_LIST_CLIENT:
			gambleList(packet,session);
			break;
		case OpCode.GAMBLE_DETAILLIST_CLIENT:
			gambleDetailList(packet,session);
			break;
		case OpCode.GAMBLE_PROCESS_CLIENT:
			gambleProcess(packet,session);
			break;
		case OpCode.FEAST_SIGNANDENTER_CLIENT:
			feastSignAndEnter(packet,session);
			break;
		case OpCode.FEAST_NPCFUNCTION_CLIENT:
			feastFunction(packet,session);
			break;
		case OpCode.FEAST_VIEWMENU_CLIENT:
			viewMenu(packet,session);
			break;
		case OpCode.FEAST_MATERIALCOUNT_CLIENT:
			processMaterialCount(packet, session);
			break;
		case OpCode.KTOUCH_CHECK_RECEIPT_CLIENT:
		{
			KTouchChargeService service = (KTouchChargeService)Server.server.getServiceRegistry().getService(KTouchChargeService.class);
			service.checkReceipt(session, packet);
			break;
		}
		case OpCode.ANTI_BOT_CLIENT:
			processAntiBot(packet, session);
			break;
		case OpCode.USE_KINGITEM_CLIENT:
			processKingItem(packet,session);
			break;
		case OpCode.ACCOUNT_LOGIN_91_CLIENT:
			accountLogin91(packet,session);
			break;
		case OpCode.ACCOUNT_LOGIN_360_CLIENT:
			accountLogin360(packet,session);
			break;
		case OpCode.THREE_YEAR_HANDIN_CLIENT:
			handerinItem(packet,session);
			break;
		case OpCode.THREE_YEAR_REPUTE_CLIENT:
			reputeList(packet, session);
			break;
		case OpCode.CYCLE_INSTANCE_LEVEL_CLIENT:
			reqInstranceLevel(packet, session);
			break;
		case OpCode.ALIPAY_GETORDER_CLIENT:
			alilayGetOrder(packet, session);
			break;
		case OpCode.STARENHANCE_CONFIRE_CLIENT:
			createStarEnhance(packet,session);
			break;
		case OpCode.CHESSINSTANCE_BOARD_CLIENT:
			chessBoard(packet,session);
			break;
		case OpCode.ENTERMAP_CHESSINSTANCE_CLIENT:
			chessEnter(packet,session);
			break;
		case OpCode.ACTIVITY_ITEMEFFECT_CLIENT:
			changeActivityItem(packet,session);
			break;
		case OpCode.HUAWEI_GETORDER_CLIENT:
			huaweiGetOrder(packet, session);
			break;
		case OpCode.ACCOUNT_LOGIN_XIAOMI_CLIENT:
			accountLoginXiaomi(packet,session);
			break;
		case OpCode.XIAOMI_GETORDER_CLIENT:
			xiaomiGetOrder(packet, session);
			break;
		case OpCode.ACCOUNT_LOGIN_LENOVO_CLIENT:
			accountLoginLenovo(packet,session);
			break;
		case OpCode.MAP_NPC_CLINT:
			getMapNpcs(packet, session);
			break;
		case OpCode.ATTENDANT_CHANGETOEXP_CLIENT:
			attendantChangeToExp(packet,session);
			break;
		case OpCode.ATTENDANT_LEVELUP_CLIENT:
			attendantLevelUp(packet,session);
			break;
		case OpCode.THANKS_GIVING_GO_CLIENT:
			ThanksGivingGoMap(packet,session);
			break;
		case OpCode.BULK_USEITEM_CLIENT:
			bulkUseItem(packet,session);
			break;
		case OpCode.ZHONGXING_GET_ORDER_CLIENT:
			zhongxingGetOrder(packet, session);
			break;
		case OpCode.CHARGEACTIVITY_UIINFO_CLIENT:
			chargeActivityInfo(packet, session);
			break;
		case OpCode.LOADING_FINISHED1_CLIENT:
			loadingFinished1(packet, session);
			break;
		case OpCode.AWARDACTIVITY_RESULT_CLIENT:
			awardResult(packet, session);
			break;
		case OpCode.ATTENDANT_INFO_CLIENT:
			resAttendantInfo(packet, session);
			break;
		case OpCode.SHOP_QUICK_BUYANDUSE_CLIENT:
			shopQuickBuyAndUse(packet, session);
			break;
		case OpCode.GET_CMCC_YUANBAO_LIST_CLIENT:
			sendCMCCYuanbaoList(packet, session);
			break;
		case OpCode.AUTO_EQUIPENHANCE_CLIENT:
			autoEquipEnhance(packet,session);
			break;
		case OpCode.VIP_ESCORT_QUEST_CLIENT:
			reqVipDemandEscort(packet, session);
			break;
		case OpCode.VIP_CHARGE_VALUE_CLIENT:
			getVIPChargeValue(packet, session);
			break;
		case OpCode.VIP_NATURAL_PROPERTY_CLIENT:
			vipNaturalProperty(packet, session);
			break;
		case OpCode.SEND_NEWYEAR_PRAY_CLIENT:
			reqSendNewYearPray(packet, session);
			break;		
		case OpCode.NEWYEAR_ACTIVITY_CLIENT:
			newYearActivity(packet,session);
			break;
		case OpCode.KTOUCH_GET_ORDER_CLIENT:
			ktouchGetOrder(packet, session);
			break;
		case OpCode.BEAUTYPARADE_REWARD_CLIENT:
			beautyParadeRewards(packet, session);
			break;
		case OpCode.WOMEN_DAY_CLIENT:
			womenDaySignUp(packet, session);
			break;
		case OpCode.ALCHEMY_INFO_CLIENT:
			alchemyInfo(packet,session);
			break;
		case OpCode.ALCHEMY_BYPLAYEREXP_CLIENT:
			alchemyByPlayerExp(packet, session);
			break;
		case OpCode.ALCHEMY_BYIMONEY100_CLIENT:
			alchemyByIMoney(packet, session, 500);
			break;
		case OpCode.ALCHEMY_BYIMONEY_CLIENT:
			alchemyByIMoney(packet, session, 5);
			break;
		case OpCode.ALCHEMY_BREAKLEVEL_CLIENT:
			breakLevel(packet, session);
			break;
		case OpCode.CHARGEACTIVITY_GETREWARD_CLIENT:
			chargeActGetReward(packet, session);
			break;
		case OpCode.PARTNER_GETORDER_CLIENT:
			partnerGetOrder(packet, session);
			break;
		case OpCode.MAYDAY_HANDIN_CLIENT:
			mayDayHandIn(packet,session);
			break;
		case OpCode.EQUIPMENT_LEVELUP_ITEMINFO_CLIENT:
			getLevelUpItemInfo(packet, session);
			break;
		case OpCode.EQUIP_LEVELUP_CLIENT:
			equipLeveUp(packet, session);
			break;
		case OpCode.VIEWCANLEVELUPEQUIP_CLIENT:
			viewPlayerCanLevelUpEquips(packet,session);
			break;
		case OpCode.EQUIPMENT_SUITEITEMINFO_CLIENT:
			viewLevelUpEquipSuiteInfo(packet, session);
			break;
		case OpCode.APPSTORE_CHARGING_FAILED_CLIENT:
			appstoreChargeFailedReturn(packet, session);
			break;
		case OpCode.FESTIVAL_ESCORT_CLIENT:
			festivalConvoy(packet, session);
			break;
		case OpCode.FESTIVAL_ESCORTBOARD_CLIENT:
			festivalConvoyBoard(packet, session);
			break;
		case OpCode.CARD_INFO_NEW_CLIENT:
			getCardInfo(packet, session);
			break;
		case OpCode.GETPLAYERPROP_CARDS_CLIENT:
			getPlayerProp_Cards(packet, session);
			break;
		case OpCode.CARD_LIST_EQUIP_CLIENT:
			cardList_Equip(packet,session);
			break;
		case OpCode.ACCOUNT_LOGIN_DUOKU_CLIENT:
			accountLoginDuoku(packet, session);
			break;
		case OpCode.TENTHANNIVERSARY_GETEWARD_CLIENT:
			getTenthAnniversaryReWard(packet,session);
			break;
		case OpCode.TENTHANNIVERSARY_INFO_CLIENT:
			getTenthAnniversaryReWardInfo(packet,session);
			break;
		case OpCode.TONG_PLAYERINFO_CLIENT:
			playerTongInfo(packet,session);
			break;
		case OpCode.BAG_ENTRANCELOG_CLIENT:
			entranceLog(packet,session);
			break;
		case OpCode.ASYNCBATTLE_CHALLENGE_RANK_CLIENT:
			asyncBattleChallengeRank(packet, session);
			break;
		case OpCode.ASYNCBATTLE_HERO_RANK_CLIENT:
			asyncBattleHeroRank(packet, session);
			break;
		case OpCode.ASYNCBATTLE_HIGHSCORE_RANK_CLIENT://晋升最快的榜单
			asyncBattleHighScoreRank(packet,session);
			break;
		case OpCode.ASYNCBATTLE_CHALLENGE_CLIENT:
			asyncBattleChallenge(packet, session);
			break;
		case OpCode.ASYNCBATTLE_GETREWARDINFO_CLEINT://异步战场奖励信息
			asyncBattleGetRewardInfo(packet, session);
			break;
		case OpCode.ASYNCBATTLE_GETREWARD_CLIENT:
			asyncBattleGetReward(packet, session);
			break;
		case OpCode.ASYNCBATTLE_JOBSREWARDINFO_CLIENT://官职信息请求
			asyncBattleJobsRewardInfo(packet, session);
			break;
		case OpCode.ASYNCBATTLE_GETJOBSREWARD_CLIENT://领取官职奖励
			asyncBattleGetJobReward(packet, session);
			break;
		case OpCode.ASYNCBATTLE_JOBSUPGRADE_CLIENT://官职升级
			sayncBattleJobUpgrade(packet, session);
			break;
		case OpCode.ASYNCBATTLE_GETREWARDCOUNT_CLIENT://可领取奖励数量
			asyncBattleRewardCount(packet, session);
			break;
		case OpCode.ASYNC_LASTASYNCBATTLERESULT_CLIENT:
			asyncBattleResult(packet, session);
			break;
		case OpCode.EXAM_QUESTION_REQUEST_CLIENT:
			examRequest(packet, session);
			break;
		case OpCode.EXAM_ANSWER_CLIENT:
			examAmswer(packet, session);
			break;
		case OpCode.EXAM_CHANGE_QUESTION_CLIENT:
			examChage(packet, session);
			break;
		case OpCode.EXAM_REDICTPASS_CLIENT:
			examRedirectPass(packet, session);
			break;
		case OpCode.EXAM_REMOVE_CLIENT:
			examQuwei(packet, session);
			break;
		case OpCode.EXAM_RESULT_CLIENT:
			examResult(packet, session);
			break;
		case OpCode.EXAM_BOARD_CLIENT:
			examBoardList(packet, session);
			break;
		}
	}
	
	protected void examBoardList(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new ExamBoardListCall(session, packet));
	}
	
	protected void examResult(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new ExamResultCall(session, packet));
	}
	
	protected void examRequest(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new ExamRequestCall(session, packet));
	}
	
	protected void examAmswer(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new ExamAnswerCall(session, packet));
	}
	
	protected void examChage(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new ExamChangeCall(session, packet));
	}
	
	protected void examRedirectPass(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new ExamRedirectPassCall(session, packet));
	}
	
	protected void examQuwei(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new ExamQuweiCall(session, packet));
	}
	
	protected void asyncBattleResult(Packet packet,ClientSession session){
		/**
		 * 玩家最近一次擂台战结果返回
		 * serial			int
		 * result			byte 0失败 1胜利
		 * uprankNums		int 上升了多少名
		 * score			int 积分
		 * currentRank		int 当前名次
		 */
		int serial = packet.getInt();
		Player player=(Player)session.getClient();
		if(player!=null){
			Packet pt = new Packet(OpCode.ASYNC_LASTASYNCBATTLERESULT_SERVER);
			pt.putInt(serial);
			AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
			AsyncNormalBoard ab=service.getAsyncNormalBoardByPlayerId(player.id);
			pt.put(ab.lastBattleResult);
			pt.putInt(ab.oldRank-ab.rank);
			pt.putInt(ab.lastBattleResult==0?AsyncBattleService.SCORE_LOSE:AsyncBattleService.SCORE_WIN);
			pt.putInt(ab.rank);
			player.send(pt);
		}
	}
	
	protected void asyncBattleRewardCount(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player player=(Player)session.getClient();
		if(player!=null){
			Packet pt = new Packet(OpCode.ASYNCBATTLE_GETREWARDCOUNT_SERVER);
			pt.putInt(serial);
			int count=0;
			AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
			AsyncNormalBoard ab=service.getAsyncNormalBoardByPlayerId(player.id);
			for(int i=0;i<ab.achievementStateNew.length;i++){
				int state=ab.achievementStateNew[i];
				if(i<4){
					if(state>0){
						count++;
					}
				}else{
					if(state>=AsyncBattleService.TOTAL_REWARD_COUNT){
						count++;
					}
				}
			}
			if(ab.dayFlag!=ab.dayFlag_GetRewardTime&&ab.officerIndex!=AsyncBattleService.OFFICER_NAME.length-1){
				count++;
			}
			pt.putInt(count);
			player.send(pt);
		}
	}
	//	{4138，1，4787，1} 首次进入500
	//	{4140，1，4787，2} 首次进入200
	//	{4140，2，4787，3} 首次进入 50
	//	{4742，10，4893，5，4898，2} 累计5天11-50名
	//	{4742，20，4893，10，4898，5} 累计5天2-10名
	//	{4742，50，4893，15，4898，10} 累计5天第1名
	/**
	 * 官职升级
	 * @param packet
	 * @param session
	 */ 
	protected void sayncBattleJobUpgrade(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player player=(Player)session.getClient();
		if(player!=null){
			AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
			AsyncNormalBoard ab=service.getAsyncNormalBoardByPlayerId(player.id);
			int needScore=AsyncBattleService.OFFICERS_NEEDSCORE[ab.officerIndex];
			if(ab.officerIndex==0){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ASYNCBATTLE_JOBSUPGRADE_CLIENT, "您已经升到最高级！！");
				return;
			}
			if(needScore>ab.officerScore){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ASYNCBATTLE_JOBSUPGRADE_CLIENT, "积分不足升级官职！！");
				return;
			}
			if(ab.officerIndex>0){
				ab.officerIndex--;
			}
			Packet pt = new Packet(OpCode.ASYNCBATTLE_JOBSUPGRADE_SERVER);
			pt.putInt(serial);
			int success=1;
			pt.put(success);
			if(success==1){
				//下阶官职
				StringBuffer sb=new StringBuffer();
				int jobIndex=ab.officerIndex;
				int nextJob=jobIndex-1;
				if(nextJob<0){
					nextJob=0;
				}
				String job=AsyncBattleService.OFFICER_NAME[nextJob];
				pt.putUTF(job);
				pt.putUTF("");
				int itemTypes=AsyncBattleService.OFFICER_REWARD[nextJob].length/2;
				pt.putInt(itemTypes);
				for(int j=0;j<itemTypes;j++){
					int id=AsyncBattleService.OFFICER_REWARD[nextJob][j*2];
					int count=AsyncBattleService.OFFICER_REWARD[nextJob][j*2+1];
					GameItem item=ObjectAccessor.createGameItem(id);
					pt.putUTF(item!=null?item.template.name:"");
					String bindDesc="";
					if(item.template.bindType==ItemTemplate.BIND_REWARD){
						bindDesc="（拾取绑定）";
					}else if(item.template.bindType==ItemTemplate.BIND_USED){
						bindDesc="（使用绑定）";
					}
					pt.putUTF((item!=null?item.template.desc:"")+bindDesc);
					pt.putInt(item.template.id);
					pt.put((byte)item.template.showImage);
					pt.putShort(((item.template.showType<<2|item.template.bindType)&0xFFFF));
					pt.put(item.template.quality);
					pt.put(count);
				}
				int currScore=ab.officerScore;//当前积分
				pt.putInt(currScore);
				needScore=AsyncBattleService.OFFICERS_NEEDSCORE[jobIndex];
				pt.putInt(needScore);
				ab.dayFlag_GetRewardTime=Time.currTime;//重置领奖时间
				int canGetRewardFalg=ab.dayFlag==ab.dayFlag_GetRewardTime?1:0;
				pt.put(canGetRewardFalg);
			}
			log.info("[ASAYNCBATTLEJOBUPGRADE]PLAYERID["+player.id+"]OFFICERINDEX["+ab.officerIndex+"]");
			player.send(pt);
		}
	}
	
	/**
	 * 领取官职奖励
	 * @param packet
	 * @param session
	 */
	protected void asyncBattleGetJobReward(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player p=(Player)session.getClient();
		if(p!=null){
			AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
			AsyncNormalBoard ab=service.getAsyncNormalBoardByPlayerId(p.id);
			if(ab.dayFlag==ab.dayFlag_GetRewardTime){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ASYNCBATTLE_GETJOBSREWARD_CLIENT, "不能重复领取奖励");
				return;
			}
			ChatService chat=Server.server.getServiceRegistry().getChatService();
//			int sendcount1=0;
			int sendcount=0;
			int type=ab.officerIndex;
			if(p.bag.getFreeBagCount()<AsyncBattleService.OFFICER_REWARD[type].length/2){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ASYNCBATTLE_GETJOBSREWARD_CLIENT, "背包格数量不足，请尽快清理背包后再来领取。");
				return;
			}
			for(int i=0;i<AsyncBattleService.OFFICER_REWARD[type].length;i+=2){
				int id=AsyncBattleService.OFFICER_REWARD[type][i];
				int count=AsyncBattleService.OFFICER_REWARD[type][i+1];
				GameItem rewardItem=ObjectAccessor.createGameItem(id);
				if(rewardItem!=null){
					PlayerTransaction tx = p.newTransaction("ASYNCBATTLE_JOBSREWARD");
					try {
						p.bag.addGameItemComplete(rewardItem, count, tx, true);
						tx.commit();
						if(sendcount==0){
							sendcount=1;
							chat.sendPrivateMessage(p.id, "您领取的奖励已发送到背包，请及时查收。");
						}
					} catch (NoEnoughSpaceException e) {
						tx.rollback();
//						if(sendcount1==0){
//							sendcount1=1;
//							chat.sendPrivateMessage(p.id, "由于您的背包已满，您领取的周年庆活动奖励由飞鸽发送，请及时查收。");
//						}
//						MailService mailservice = Server.server.getServiceRegistry().getMailService();
//						mailservice.sendSystemMail(p.id, peony.Messages.STRING_00004, "周年庆活动奖励", "这是您领取的周年庆活动奖励，请及时收取附件。", 0, rewardItem, count, "TENTHANNIVERSARYREWARDTOMAIL_DAY");
					}
				}
			}
			ab.dayFlag_GetRewardTime=Time.currTime;
			ab.dayFlag=ab.dayFlag_GetRewardTime;
			
			Packet pt = new Packet(OpCode.ASYNCBATTLE_GETJOBSREWARD_SERVER);
			pt.putInt(serial);
			pt.put(1);
			p.send(pt);
		}
	}
	
	/***
	 * 官职信息请求
	 * @param packet
	 * @param session
	 */
	protected void asyncBattleJobsRewardInfo(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player player=(Player)session.getClient();
		AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
		if(player!=null){
			AsyncNormalBoard ab=service.getAsyncNormalBoardByPlayerId(player.id);
			if(ab!=null){
				int jobIndex=ab.officerIndex;
				Packet pt = new Packet(OpCode.ASYNCBATTLE_JOBSREWARDINFO_SERVER);
				pt.putInt(serial);
				String job=AsyncBattleService.OFFICER_NAME[jobIndex];
				pt.putUTF(job);
				int nextJobIndex=jobIndex;
				if(nextJobIndex<0){
					nextJobIndex=0;
				}
				int needScore=AsyncBattleService.OFFICERS_NEEDSCORE[nextJobIndex];//下级所需积分s
				if(nextJobIndex==0){
					needScore=0;
				}
				int currScore=ab.officerScore;//当前积分
//				StringBuffer sb=new StringBuffer();
				pt.putUTF("");
				int itemTypes=AsyncBattleService.OFFICER_REWARD[jobIndex].length/2;
				pt.putInt(itemTypes);
				for(int j=0;j<itemTypes;j++){
					int id=AsyncBattleService.OFFICER_REWARD[jobIndex][j*2];
					int count=AsyncBattleService.OFFICER_REWARD[jobIndex][j*2+1];
					GameItem item=ObjectAccessor.createGameItem(id);
					pt.putUTF(item!=null?item.template.name:"");
					String bindDesc="";
					if(item.template.bindType==ItemTemplate.BIND_REWARD){
						bindDesc="（拾取绑定）";
					}else if(item.template.bindType==ItemTemplate.BIND_USED){
						bindDesc="（使用绑定）";
					}
					pt.putUTF((item!=null?item.template.desc:"")+bindDesc);
					pt.putInt(item.template.id);
					pt.put((byte)item.template.showImage);
					pt.putShort(((item.template.showType<<2|item.template.bindType)&0xFFFF));
					pt.put(item.template.quality);
					pt.put(count);
				}
				//下阶官职
				if(nextJobIndex>0){
					--nextJobIndex;
				}
				job=AsyncBattleService.OFFICER_NAME[nextJobIndex];
				if(jobIndex==0){
					job="";
				}
				pt.putUTF(job);
//				sb=new StringBuffer();
				pt.putUTF("");
				itemTypes=AsyncBattleService.OFFICER_REWARD[nextJobIndex].length/2;
				pt.putInt(itemTypes);
				for(int j=0;j<itemTypes;j++){
					int id=AsyncBattleService.OFFICER_REWARD[nextJobIndex][j*2];
					int count=AsyncBattleService.OFFICER_REWARD[nextJobIndex][j*2+1];
					GameItem item=ObjectAccessor.createGameItem(id);
					pt.putUTF(item!=null?item.template.name:"");
					String bindDesc="";
					if(item.template.bindType==ItemTemplate.BIND_REWARD){
						bindDesc="（拾取绑定）";
					}else if(item.template.bindType==ItemTemplate.BIND_USED){
						bindDesc="（使用绑定）";
					}
					pt.putUTF((item!=null?item.template.desc:"")+bindDesc);
					pt.putInt(item.template.id);
					pt.put((byte)item.template.showImage);
					pt.putShort(((item.template.showType<<2|item.template.bindType)&0xFFFF));
					pt.put(item.template.quality);
					pt.put(count);
				}
				int flag=(ab.dayFlag_GetRewardTime==ab.dayFlag)?1:0;
				pt.put(flag);
				pt.putInt(currScore);
				pt.putInt(needScore);
				player.send(pt);
			}
		}
	}
	
	/**
	 * 领取异步战场奖励
	 * @param packet
	 * @param session
	 */
	protected void asyncBattleGetReward(Packet packet,ClientSession session){
		int items[][]=null;
		int serial = packet.getInt();
		int type = packet.get();//0-首进  1-累计
		int rewardType=packet.get();//type==0  0,1,2,3  type==1 0,1,2
		Player player=(Player)session.getClient();
		if(type!=0&&type!=1){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.ASYNCBATTLE_GETREWARD_CLIENT, "错误的请求");
			return;
		}
		if(type==0){
			items=AsyncBattleService.REWARD_FIRSTENTER;
			if(rewardType>=AsyncBattleService.REWARD_FIRSTENTER.length){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ASYNCBATTLE_GETREWARD_CLIENT, "错误的请求");
				return;
			}
		}else if(type==1){
			items=AsyncBattleService.REWARD_TOTAL;
			if(rewardType>=AsyncBattleService.REWARD_TOTAL.length){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ASYNCBATTLE_GETREWARD_CLIENT, "错误的请求");
				return;
			}
		}
		if(player!=null){
			AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
			AsyncNormalBoard board = service.getAsyncNormalBoardByPlayerId(player.id);
			Packet pt = new Packet(OpCode.ASYNCBATTLE_GETREWARD_SERVER);
			pt.putInt(serial);
			pt.put(type);
			pt.put(rewardType);
			int rewardTypeTemp=rewardType;
			if(type==1){
				rewardTypeTemp=AsyncBattleService.REWARD_FIRSTENTER.length+rewardType;
			}
			switch(rewardTypeTemp){
			case AsyncNormalBoard.ACHIEVEMENT_TYPE_500:
			case AsyncNormalBoard.ACHIEVEMENT_TYPE_200:
			case AsyncNormalBoard.ACHIEVEMENT_TYPE_100:
			case AsyncNormalBoard.ACHIEVEMENT_TYPE_50:
				if(board.achievementStateNew[rewardTypeTemp]==0){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ASYNCBATTLE_GETREWARD_CLIENT, "任务未完成");
					return;
				}else if(board.achievementStateNew[rewardTypeTemp]==-1){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ASYNCBATTLE_GETREWARD_CLIENT, "奖励已领取");
					return;
				}
				break;
			case AsyncNormalBoard.ACHIEVEMENT_TOTAL_1:
			case AsyncNormalBoard.ACHIEVEMENT_TOTAL_2_10:
			case AsyncNormalBoard.ACHIEVEMENT_TOTAL_11_50:
				if(board.achievementStateNew[rewardTypeTemp]<AsyncBattleService.TOTAL_REWARD_COUNT){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ASYNCBATTLE_GETREWARD_CLIENT, "任务未完成");
					return;
				}else if(board.achievementStateNew[rewardTypeTemp]==-1){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ASYNCBATTLE_GETREWARD_CLIENT, "奖励已领取");
					return;
				}
				break;
			default:
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ASYNCBATTLE_GETREWARD_CLIENT, "错误的请求");
				return;
			}
			
			//领取奖励
			ChatService chat=Server.server.getServiceRegistry().getChatService();
			int sendcount1=0;
			int sendcount=0;
			
			if(player.bag.getFreeBagCount()<items[rewardType].length/2){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ASYNCBATTLE_GETJOBSREWARD_CLIENT, "背包格数量不足，请尽快清理背包后再来领取。");
				return;
			}
			for(int i=0;i<items[rewardType].length;i+=2){
				int id=items[rewardType][i];
				int count=items[rewardType][i+1];
				GameItem rewardItem=ObjectAccessor.createGameItem(id);
				if(rewardItem!=null){
					PlayerTransaction tx = player.newTransaction("ASYNCBATTLEREWARDTOBAG_DAY");
					try {
						player.bag.addGameItemComplete(rewardItem, count, tx, true);
						tx.commit();
						if(sendcount1==0){
							sendcount1=1;
							chat.sendPrivateMessage(player.id, "您领取的奖励已发送到背包，请及时查收。");
						}
					} catch (NoEnoughSpaceException e) {
						tx.rollback();
//						if(sendcount1==0){
//							sendcount1=1;
//							chat.sendPrivateMessage(player.id, "由于您的背包已满，您领取的奖励由飞鸽发送，请及时查收。");
//						}
//						MailService mailservice = Server.server.getServiceRegistry().getMailService();
//						mailservice.sendSystemMail(player.id, peony.Messages.STRING_00004, "活动奖励", "这是您领取的活动奖励，请及时收取附件。", 0, rewardItem, count, "TENTHANNIVERSARYREWARDTOMAIL_DAY");
					}
				}
			}
			if(type==1){
				if(board.achievementStateNew[rewardTypeTemp]>=AsyncBattleService.TOTAL_REWARD_COUNT){
					board.achievementStateNew[rewardTypeTemp]-=AsyncBattleService.TOTAL_REWARD_COUNT;
				}
			}else if(type==0){
				board.achievementStateNew[rewardTypeTemp]=-1;
			}
			int flag=0;//未完成
			if(board.achievementStateNew[rewardTypeTemp]>=AsyncBattleService.TOTAL_REWARD_COUNT){
				flag=1;
			}
			if(rewardTypeTemp<AsyncBattleService.REWARD_FIRSTENTER.length){
				flag=2;
			}
			pt.put(flag);
			player.send(pt);
		}
	}
	/**
	 * 异步战场奖励信息
	 * @param packet
	 * @param session
	 */
	protected void asyncBattleGetRewardInfo(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int type = packet.get();//0-首进奖励 1-累计奖励
		if(type!=0&&type!=1){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.ASYNCBATTLE_GETREWARDINFO_CLEINT, "错误的请求");
			return;
		}
		Player player=(Player)session.getClient();
		if(player!=null){
			Packet pt = new Packet(OpCode.ASYNCBATTLE_GETREWARDINFO_SERVER);
			pt.putInt(serial);
			pt.put(type);
			int rewardItemIds_Day[][]=null;
			String descs[]=null;
			if(type==0){
				//由于首进要加入两个新奖励先处理一下，下周再打开
				rewardItemIds_Day=new int[AsyncBattleService.REWARD_FIRSTENTER.length-2][];
				for(int i=0;i<rewardItemIds_Day.length;i++){
					rewardItemIds_Day[i]=new int[AsyncBattleService.REWARD_FIRSTENTER[i].length];
					System.arraycopy(AsyncBattleService.REWARD_FIRSTENTER[i], 0, rewardItemIds_Day[i], 0,rewardItemIds_Day[i].length);
				}
//				rewardItemIds_Day=AsyncBattleService.REWARD_FIRSTENTER;
				descs=AsyncBattleService.rewardFirstEnter;
			}else{
				rewardItemIds_Day=AsyncBattleService.REWARD_TOTAL;
				descs=AsyncBattleService.rewardTotal;
			}
			pt.put(rewardItemIds_Day.length);
			for(int i=0;i<rewardItemIds_Day.length;i++){
				int itemTypes=rewardItemIds_Day[i].length/2;
				pt.putInt(itemTypes);
				StringBuffer sb=new StringBuffer();
				for(int j=0;j<itemTypes;j++){
					int id=rewardItemIds_Day[i][j*2];
					int count=rewardItemIds_Day[i][j*2+1];
					pt.put(count);
					GameItem item=ObjectAccessor.createGameItem(id);
					pt.putUTF(item!=null?item.template.name:"");
					String bindDesc="";
					if(item.template.bindType==ItemTemplate.BIND_REWARD){
						bindDesc="（拾取绑定）";
					}else if(item.template.bindType==ItemTemplate.BIND_USED){
						bindDesc="（使用绑定）";
					}
					pt.putUTF((item!=null?item.template.desc:"")+bindDesc);
					pt.putInt(item.template.id);
					pt.put((byte)item.template.showImage);
					pt.putShort(((item.template.showType<<2|item.template.bindType)&0xFFFF));
					pt.put(item.template.quality);
					String flag="";
					if(i<itemTypes-1){
						flag="，";
					}else{
						flag="。";
					}
					sb.append(item.template.name+"*"+count+flag);
				}
				AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
				AsyncNormalBoard board = service.getAsyncNormalBoardByPlayerId(player.id);
				pt.putUTF(descs[i]);
				String desc=MessageFormat.format(descs[descs.length/2+i],sb.toString());
				int rewardType=(type==1?AsyncBattleService.REWARD_FIRSTENTER.length+i:i);
				int hadReward=board.achievementStateNew[rewardType];
				pt.putUTF(desc);
				if(type==1){//累计
					if(hadReward>-1&&hadReward<AsyncBattleService.TOTAL_REWARD_COUNT){
						hadReward=0;
					}else if(hadReward>=AsyncBattleService.TOTAL_REWARD_COUNT){
						hadReward=1;
					}else if(hadReward==-1){//已领取
						hadReward=2;
					}
				}else if(type==0){
					if(hadReward==-1){
						hadReward=2;
					}
				}
				pt.put(hadReward);
				if(type==0){
					pt.put(0);
					pt.put(0);
				}else if(type==1){
					int alreadyCount=board.achievementStateNew[AsyncBattleService.REWARD_FIRSTENTER.length+i];
					int needCount=AsyncBattleService.TOTAL_REWARD_COUNT;
//					if(alreadyCount>AsyncBattleService.TOTAL_REWARD_COUNT || alreadyCount==-1){
//						alreadyCount=AsyncBattleService.TOTAL_REWARD_COUNT;
//					}
					pt.put(alreadyCount);
					pt.put(needCount);
				}
			}
			player.send(pt);
		}
	}
	
	protected void asyncBattleChallenge(Packet packet,ClientSession session){
		AsyncBattleCountIBugCall call=new AsyncBattleCountIBugCall(session,packet);
		Server.server.getServiceRegistry().getSyncExecutorService().schedule(call);
	}
	/**
	 * 晋升最快榜单请求
	 * @param packet
	 * @param session
	 */
	protected void asyncBattleHighScoreRank(Packet packet,ClientSession session){
		/**
		 * 异步战场：晋升最快榜单请求返回
		 * serial					int
		 * size						byte			
		 * 		id					int				玩家ID
		 * 		name				String			名字
		 * 		clazz				byte			职业
		 * 		level				byte			级别
		 * 		rank				int				晋升名次
		 */
		int serial = packet.getInt();
		Player player=(Player)session.getClient();
		if(player!=null){
			Packet pt = new Packet(OpCode.ASYNCBATTLE_HIGHSCORE_RANK_SERVER);
			pt.putInt(serial);
			AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
			int count=service.getTops().size();
			pt.put(count);
			for(int i=0;i<count;i++){
				AsyncNormalBoard anb = service.getTops().get(i);
				pt.putInt(anb.playerId);
				pt.putUTF(anb.name==null?"":anb.name);
				pt.put(anb.faction);
				pt.put(anb.clazz);
				pt.put(anb.level);
				pt.putInt(anb.upRank);
			}
			AsyncNormalBoard self=service.getAsyncNormalBoardByPlayerId(player.id);
			if(self!=null){
				pt.putInt(self.upRank);
			}else{
				pt.putInt(0);
			}
			player.send(pt);
		}
	}
	/**
	 * 英雄榜榜单请求
	 * @param packet
	 * @param session
	 */
	protected void asyncBattleHeroRank(Packet packet,ClientSession session){
		/**
		 * 异步战场：英雄榜榜单请求返回
		 * serial					int
		 * size						byte			
		 * 		id					int				玩家ID
		 * 		name				String			名字
		 * 		clazz				byte			职业
		 * 		level				byte			级别
		 * selfrank					int				本人排名
		 */
		AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
		int serial = packet.getInt();
		Player p=(Player)session.getClient();
		if(p!=null){
			Packet pt = new Packet(OpCode.ASYNCBATTLE_HERO_RANK_SERVER);
			pt.putInt(serial);
			int size=AsyncBattleService.HERO_RANK_TOP10;
			if(service.getRank2boards().size()<AsyncBattleService.HERO_RANK_TOP10){
				size=service.getRank2boards().size();
			}
			pt.put(size);
			for(int i=1;i<=size;i++){
				AsyncNormalBoard ab=service.getAsyncNormalBoardByRank(i);
				pt.putInt(ab.playerId);
				pt.putUTF(ab.name==null?"":ab.name);
				pt.put(ab.faction);
				pt.put(ab.clazz);
				pt.put(ab.level);
			}
			int selfRank=service.getAsyncNormalBoardByPlayerId(p.id)==null?0:service.getAsyncNormalBoardByPlayerId(p.id).rank;
			pt.putInt(selfRank);
			p.send(pt);
		}
	}
	
	protected void entranceLog(Packet packet,ClientSession session){
		int serial = packet.getInt();
		String type = packet.getString();
		Player player = (Player)session.getClient();
		if(player!=null){
			log.info("[BAGENTRANCE]ID["+player.id+"]ACC["+player.accountId+"]TYPE["+type+"]");
		}
	}	
	
	protected void asyncBattleChallengeRank(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AsyncBattleChallengeRankCall(session,packet));
	}
	protected void playerTongInfo(Packet packet,ClientSession session){
		TongInfoCall2 call=new TongInfoCall2(session,packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void getTenthAnniversaryReWard(Packet packet,ClientSession session){
//		TenthAnniversaryService service = Server.server.getServiceRegistry().getTenthAnniversaryService();
//		service.sendReWard(packet, session);
	}
	protected void getTenthAnniversaryReWardInfo(Packet packet,ClientSession session){
//		TenthAnniversaryService service = Server.server.getServiceRegistry().getTenthAnniversaryService();
//		service.getReWardInfo(packet, session);
	}
	
	/** 百度多酷平台联运 */
	protected void accountLoginDuoku(Packet packet,ClientSession session){
		int serial = packet.getInt();
		String uId = packet.getString();
		String sessionId = packet.getString();
		String model = packet.getString();
		String uiModel = packet.getString();
		String version = packet.getString();
		String realPhone = packet.getString();
		int playerId = 0;
		try{playerId = packet.getInt();}catch(Exception e){}
		Server.server.getServiceRegistry().getDbService().schedule(new AccountLoginDuokuCall(session, serial, uId, sessionId,
				model, uiModel, version, realPhone, playerId));
	}
	
	/** 活动押镖排行榜*/
	protected void festivalConvoyBoard(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player player = (Player)session.getClient();
		if(player!=null){
			ActivityService activityService = Server.server.getServiceRegistry().getActivityService();
			Activity activity = activityService.getActivityByImpClass(FestivalConvoyActivity.class.getSimpleName());
			if(activity!=null){
				Date dateNow = new Date();
				try{
					if(dateNow.after(activity.getSchedule().stopTime)){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.FESTIVAL_ESCORT_CLIENT, "活动已经结束");
						return;
					}
				}catch(Exception e){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.FESTIVAL_ESCORT_CLIENT, "活动已经结束");
					return;
				}
			}
			FestivalConvoyActivity.convoyBoard(serial, player);			
		}	
	}
	
	/** 活动押镖*/
	protected void festivalConvoy(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player player = (Player)session.getClient();
		if(player!=null){
			ActivityService activityService = Server.server.getServiceRegistry().getActivityService();
			Activity activity = activityService.getActivityByImpClass(FestivalConvoyActivity.class.getSimpleName());
			if(activity!=null){
				Date dateNow = new Date();
				try{
					if(dateNow.after(activity.getSchedule().stopTime)){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.FESTIVAL_ESCORT_CLIENT, "活动已经结束");
						return;
					}
				}catch(Exception e){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.FESTIVAL_ESCORT_CLIENT, "活动已经结束");
					return;
				}
				try {
					FestivalConvoyActivity.startFestivalEscort(player);
					Packet pt = new Packet(OpCode.FESTIVAL_ESCORT_SERVER);
					pt.putInt(serial);
					player.send(pt);
				} catch (Exception e) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.FESTIVAL_ESCORT_CLIENT, e.getMessage());
					return;
				}
			}
		}
	}
	protected void cardList_Equip(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		Player target=(Player)session.getClient();
		if(target!=null){
			CardService service = Server.server.getServiceRegistry().getCardService();
			Packet pt = new Packet(OpCode.CARD_LIST_EQUIP_SERVER);
			pt.putInt(serial);
			pt.putInt(target.cards.exp);
			pt.put(target.cards.equipCards.length);
			for(CardInfo info : target.cards.equipCards){
				if(info==null)
					pt.put(0);
				else{
					pt.put(1);
					Card card = service.getCardByCardId(info.cardId);
					if(card.buff2Id==-1){
						pt.put(0);//普通卡片
					}else
					{
						pt.put(1);//技能卡片
					}
					if(card.buff2Id==-1){//普通卡片
						pt.putInt(info.cardId);
						String desc=service.getEnhanceDesc(info.cardId, info.level+1);
						pt.put(card.star);
						int quality = -1;
						try {
							quality = ObjectAccessor.createGameItem(card.itemId).template.quality;
						} catch (Exception e) {
						}
						int exp=0;
						try {
							exp = CardUpGradeCall.getUpGradeExp(quality,info.level);
						} catch (Exception e) {
						}
						pt.putUTF(desc);
						pt.putInt(exp);
					}else{//技能卡片
						pt.putInt(info.cardId);
						String desc=service.getEnhanceDesc(info.cardId, info.level+1);
						pt.put(card.star);
						Buff skillBuffCurrent=BuffUtil.createBuff(card.buff2Id, info.level, target, target, 0);
						if(skillBuffCurrent!=null){
							desc=skillBuffCurrent.getDesc();
						}
						pt.putUTF(desc);
						Buff skillBuffNext=BuffUtil.createBuff(card.buff2Id, info.level+1, target, target, 0);
						if(skillBuffNext!=null){
							desc=skillBuffNext.getDesc();
						}
						pt.putUTF(desc);
						pt.putInt(CardUpGradeCall.getCardUpGradeNeedCount(info.level));
//						int totalCount=target.pool.getInt(CardService.getPropertyOfPlayerCard(card.id),0);
						int totalCount=CardService.getCardCount(target, card.id);
						if(totalCount>=1&&info.level>1){
							totalCount-=1;
							if(totalCount<=0){
								totalCount=0;
							}
						}
						pt.putInt(totalCount);
						int preLevel=1;
						if(info.level<3){
							preLevel=3;
						}else if(info.level<6){
							preLevel=6;
						}else if(info.level<9){
							preLevel=9;
						}else if(info.level<12){
							preLevel=12;
						}
						Buff skillBuffPre=BuffUtil.createBuff(card.buff2Id, preLevel, target, target, 0);
						if(skillBuffPre!=null&&info.level<12){
							desc=skillBuffPre.getDesc();
							pt.putUTF(desc);
						}else{
							pt.putUTF("");
						}
						int needExp=CardUpGradeCall.getSkillCardNeedExp(CardUpGradeCall.getCardUpGradeNeedCount(info.level));
						pt.putInt(needExp);
					}
				}
			}
			pt.put(target.cards.horseEquipCards.length);
			for(CardInfo info : target.cards.horseEquipCards){
				if(info==null){
					pt.put(0);
				}else{
					pt.put(1);
					Card card = service.getCardByCardId(info.cardId);
					if(card.buff2Id==-1){
						pt.put(0);//普通卡片
					}else
					{
						pt.put(1);//技能卡片
					}
					if(card.buff2Id==-1){//普通卡片						
						pt.putInt(info.cardId);
						String desc=service.getEnhanceDesc(info.cardId, info.level+1);
						pt.put(card.star);
						int quality = -1;
						try {
							quality = ObjectAccessor.createGameItem(card.itemId).template.quality;
						} catch (Exception e) {
						}
						int exp=0;
						try {
							exp = CardUpGradeCall.getUpGradeExp(quality,info.level);
						} catch (Exception e) {
						}
						pt.putUTF(desc);
						pt.putInt(exp);
					}else{//技能卡片
						pt.putInt(info.cardId);
						String desc=service.getEnhanceDesc(info.cardId, info.level+1);
						pt.put(card.star);
						Buff skillBuff=BuffUtil.createBuff(card.buff2Id, info.level, target, target, 0);
						if(skillBuff!=null){
							desc=skillBuff.getDesc();
						}
						pt.putUTF(desc);
						Buff skillBuffNext=BuffUtil.createBuff(card.buff2Id, info.level+1, target, target, 0);
						if(skillBuffNext!=null){
							desc=skillBuffNext.getDesc();
						}
						pt.putUTF(desc);
						pt.putInt(CardUpGradeCall.getCardUpGradeNeedCount(info.level));
//						int totalCount=target.pool.getInt(CardService.getPropertyOfPlayerCard(card.id),0);
						int totalCount=CardService.getCardCount(target, card.id);
						if(totalCount>=1&&info.level>1){
							totalCount-=1;
							if(totalCount<=0){
								totalCount=0;
							}
						}
						pt.putInt(totalCount);
						int preLevel=1;
						if(info.level<3){
							preLevel=3;
						}else if(info.level<6){
							preLevel=6;
						}else if(info.level<9){
							preLevel=9;
						}else if(info.level<12){
							preLevel=12;
						}
						Buff skillBuffPre=BuffUtil.createBuff(card.buff2Id, preLevel, target, target, 0);
						if(skillBuffPre!=null){
							desc=skillBuffPre.getDesc();
							pt.putUTF(desc);
						}else{
							pt.putUTF("");
						}
						int needExp=CardUpGradeCall.getSkillCardNeedExp(CardUpGradeCall.getCardUpGradeNeedCount(info.level));
						pt.putInt(needExp);
					}
				}
			}
			session.send(pt);
		}
	}

	protected void getPlayerProp_Cards(Packet packet,ClientSession session){
		Player player=(Player)session.getClient();
		int serial = packet.getInt();
		if(player!=null){
			Packet pt = new Packet(OpCode.GETPLAYERPROP_CARDS_SERVER);
			pt.putInt(serial);
			int strength=0;
			int agility=0;
			int intellect=0;
			int stamina=0;
			CardService service = Server.server.getServiceRegistry().getCardService();
			
			List<CardInfo> cards=new ArrayList<CardInfo>();
			cards.addAll(Arrays.asList(player.cards.equipCards));
			cards.addAll(Arrays.asList(player.cards.horseEquipCards));
			for(CardInfo info : cards){
				if(info!=null){
					int cardId = info.cardId;
					int cardLevel = info.level;
					Card card = service.getCardByCardId(cardId);
					if(card!=null){
						int cardPropertyType = card.prorertyType;
//						int baseValue = card.propertyBaseValue;
//						int upLevelValue = card.propertyUpLevelValue;
						int quality = ObjectAccessor.createGameItem(card.itemId).template.quality;
						int value = 0;
						if(quality==Item.QUALITY_WHITE)
							value = CardService.WHITE_ENHANCE_VALUES[cardLevel-1];
						else if(quality==Item.QUALITY_GREEN)
							value = CardService.GREEN_ENHANCE_VALUES[cardLevel-1];
						else if(quality==Item.QUALITY_BLUE)
							value = CardService.BLUE_ENHANCE_VALUES[cardLevel-1];
						else if(quality==Item.QUALITY_PURPLE)
							value = CardService.PURPLE_ENHANCE_VALUES[cardLevel-1];
						else if(quality==Item.QUALITY_ORANGE)
							value = CardService.ORANGE_ENHANCE_VALUES[cardLevel-1];
						if(cardPropertyType==0){
							strength += value;
						}else if(cardPropertyType==1){
							agility += value;
						}else if(cardPropertyType==2){
							intellect += value;
						}else if(cardPropertyType==7){
							stamina += value;
						}
					}
				}
			}
			pt.putShort(strength);
			pt.putShort(agility);
			pt.putShort(intellect);
			pt.putShort(stamina);
			session.send(pt);
		}
	}
	
	protected void getCardInfo(Packet packet,ClientSession session){
		Player player=(Player)session.getClient();
		int serial = packet.getInt();
		int cardId=packet.getInt();
		if(player!=null){
			Packet pt = new Packet(OpCode.CARD_INFO_NEW_SERVER);
			CardInfo cardInfo = player.cards.getEquipCardInfoByCardId(cardId);
			pt.putInt(serial);
			CardService service = Server.server.getServiceRegistry().getCardService();
			String desc=service.getEnhanceDesc(cardInfo.cardId, cardInfo.level+1);
			Card card = service.getCardByCardId(cardId);
			int quality = -1;
			try {
				quality = ObjectAccessor.createGameItem(card.itemId).template.quality;
			} catch (Exception e) {
			}
			int exp=0;
			try {
				exp = CardUpGradeCall.getUpGradeExp(quality,cardInfo.level);
			} catch (Exception e) {
			}
			pt.putUTF(desc);
			pt.putInt(exp);
			pt.putInt(player.cards.exp);
			session.send(pt);
		}
	}
	
	protected void appstoreChargeFailedReturn(Packet packet,ClientSession session){
		Player player=(Player)session.getClient();
		if(player!=null){
			player.pool.setInt(Player.ISSHOWPIPCHARGEFLAG, 1);//永久显示官网充值
			log.info("[APPSTORECHARGEFAILEDRETURN]");
		}
	}

	protected void viewLevelUpEquipSuiteInfo(Packet packet,ClientSession session){
		int serial=packet.getInt();
		Player player=(Player)session.getClient();
		int itemId=packet.getInt();
		int instanceId=packet.getInt();
		if(player!=null){
			Packet pt = new Packet(OpCode.EQUIPMENT_SUITEITEMINFO_SERVER);
			pt.putInt(serial);
			int[] equipIds=EquipLevelUpInfoCall.suiteIds[player.clazz];
			GameItem itemFlag=ObjectAccessor.createGameItem(itemId);
			if(itemFlag!=null&&itemFlag.template.equipment.minorType>=21){
				for(int id:itemFlag.template.equipment.suiteEffects.getEquips()){
					for(int j=0;j<EquipLevelUpInfoCall.suiteHorse.length;j++){
						if(id==EquipLevelUpInfoCall.suiteHorse[j][0]){
							equipIds=EquipLevelUpInfoCall.suiteHorse[j];
							break;
						}
					}
				}
			}
			pt.put(equipIds.length);
			for(int i=0;i<equipIds.length;i++){
				GameItem item=ObjectAccessor.createGameItem(equipIds[i]);
				EquipmentTemplate equipmentTemplate = item.template.equipment;
				String stepInfo=MessageFormat.format("({0}阶)", EquipLevelUpInfoCall.num_Chinese.charAt(i+1));
				pt.putString(equipmentTemplate.suiteEffects.getName()+stepInfo);
				//套装内装备列表
				List<Integer> equips = equipmentTemplate.suiteEffects.getEquips();
				pt.put(equips.size());
				int quality=0;
				if (equips.size() != 0) {
					for (int equipId : equips) {
						GameItem item1=ObjectAccessor.createGameItem(equipId);
						pt.putString(item1.template.name);
						if(quality==0){
							quality=item1.template.quality;
							boolean hasEffect = item1!=null&&item1.template.equipment.hasEffect();
							quality=(quality|((hasEffect?1:0)<<7));
						}
					}
				}
				pt.putInt(quality);
				//套装最高级Buff说明
				ItemTemplate itemTemp = ObjectAccessor.getItemTemplate(equipIds[i]);
				EquipmentTemplate equipmentTemplateTemp = itemTemp.equipment;
				if (equipmentTemplateTemp.suiteEffects == null) {
					pt.putString("");
				} else {
					SuiteEffect[] effects2 = equipmentTemplateTemp.suiteEffects.getEffects();
					if(effects2.length==0){
						pt.putString(MessageFormat.format("套装({0}/{1}){2}", 7,7,"没有套装效果!"));
					}else{
						int weight=item.template.equipment.suiteEffects.weights.get(item.template.id);
						Buff bufTemp=BuffUtil.createSuiteBuff(item.template.equipment.suiteEffects.getEffects()[0].buffId,1,weight*7);
						String sb = "";
						String[] desc=bufTemp.getDesc().split("；");
						int count=0;
						for(String str:desc){
							if(count!=0){
								sb+="；\n";
							}
							sb+=str;
							count++;
						}
						pt.putString(MessageFormat.format("{0}({1}/{2})\n{3}",equipmentTemplate.suiteEffects.getName(), 7,7,sb.toString()));
					}
				}
			}
			session.send(pt);
		}
	}
	
	protected void viewPlayerCanLevelUpEquips(Packet packet,ClientSession session){
		int serial=packet.getInt();
		Player player=(Player)session.getClient();
		if(player!=null){
			Packet pt = new Packet(OpCode.VIEWCANLEVELUPEQUIP_SERVER);
			pt.putInt(serial);
			//统计所有装备
			List<GameItem> equips = new ArrayList<GameItem>(); 
			//玩家身上所有装备
			int count=0;
			for(GameItem item : player.equipments.equs){
				if(item!=null && item.template!=null && item.template.isEquipment()
						&&item.template.equipment.equ.canLevelUp&&item.template.nextLevelEquipID>0){
					equips.add(item);
					count++;
				}
			}
//			pt.putInt(equips.size());
//			for(GameItem item:equips){
//				pt.put(item.toClientBytes());
//			}
//			equips.clear();
			//玩家背包中所有装备
			for(TransactionBagGrid grid : player.bag.getGrids()){
				GameItem item = grid.getItem();
				if(item!=null && item.template!=null && item.template.isEquipment()
						&&item.template.equipment.equ.canLevelUp&&item.template.nextLevelEquipID>0)
					equips.add(item);
			}
			pt.putInt(equips.size());
			int count1=0;
			for(GameItem item:equips){
				pt.put(item.toClientBytes());
				if(count1<count){
					pt.put(0);
				}else{
					pt.put(1);
				}
				count1++;
			}
			player.send(pt);
		}
	}
	
	
	protected void equipLeveUp(Packet packet,ClientSession session){
		EquipLevelUp_ProcessCall call=new EquipLevelUp_ProcessCall(session,packet);
		Server.server.getServiceRegistry().getSyncExecutorService().schedule(call);
	}
	
	protected void getLevelUpItemInfo(Packet packet, ClientSession session){
		EquipLevelUpInfoCall call=new EquipLevelUpInfoCall(session,packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void mayDayHandIn(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int type = packet.get();
		Player player = (Player)session.getClient();
		if(player!=null){
			ActivityService activityService = Server.server.getServiceRegistry().getActivityService();
			Activity activity = activityService.getActivityByImpClass(MayDayActivity.class.getSimpleName());
			if(activity!=null){
				Date dateNow = new Date();
				try{
					if(dateNow.after(activity.getSchedule().stopTime)){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.MAYDAY_HANDIN_CLIENT, "活动已经结束");
						return;
					}
				}catch(Exception e){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.MAYDAY_HANDIN_CLIENT, "活动已经结束");
					return;
				}
				if(type == 0){
					try {
						MayDayActivity.handInMatieral(player);
						Packet pt = new Packet(OpCode.MAYDAY_HANDIN_SERVER);
						pt.putInt(serial);
						player.send(pt);
					} catch (Exception e) {
						ErrorHandler.sendErrorMessage(session, serial, OpCode.MAYDAY_HANDIN_CLIENT, e.getMessage());
					}
				}else if(type == 1){
					int score = MayDayActivity.getScore(player.faction);
					String message = MessageFormat.format("本国烽火评分已累计：{0}", score);
					player.message(-1, message, -1, -1);
//				    ErrorHandler.sendErrorMessage(session, serial, OpCode.MAYDAY_HANDIN_CLIENT, message);
				}
			}
		}
	}
	
	/**
	 * 通用联运平台创建订单。
	 */
	protected void partnerGetOrder(Packet packet, ClientSession session){
		PartnerGetOrderCall call = new PartnerGetOrderCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}	
	
	//领取充值活动奖励
	protected void chargeActGetReward(Packet packet, ClientSession session){
		ChargeActivityService chargeService = Server.server.getServiceRegistry().getChargeActivityService();
		chargeService.getChargeActReward(session, packet);
	}
	
	/**
	 * 突破重天
	 * @param packet
	 * @param session
	 */
	public void breakLevel(Packet packet,ClientSession session){
		Player p=(Player)session.getClient();
		int serial=packet.getInt();
		if(p!=null){
			if(p.alchemy!=null&&p.alchemy.practiceLevel==4&&p.alchemy.pulseIndex==4&&p.alchemy.acupointNum==8&&p.alchemy.acupointLevel==10){//防止突破重天按钮出现
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ALCHEMY_BREAKLEVEL_CLIENT, "恭喜您已修炼完所有五重天，无需继续修炼。");
			}else{
				packet.data.flip();
				AlchemyCall call=new AlchemyCall(session,packet,AlchemyCall.ALCHEMY_BREAKLEVEL);
				Server.server.getServiceRegistry().getDbService().schedule(call);
			}
		}
	}
	
	
	/**
	 * 元宝修炼
	 * @param packet
	 * @param session
	 * @param type    5-元宝修炼一次5元宝  100-元宝百修   
	 */
	public void alchemyByIMoney(Packet packet,ClientSession session,int type){
		int serial=packet.getInt();
		Player p=(Player)session.getClient();
		if(p!=null){
			ShopService shopService=Server.server.getServiceRegistry().getShopService();
			int itemMoney=Math.round(shopService.getItemPrice(NoItemShopBuy.WUYUANBAO))/36;
			int useMoney=(type/5)*itemMoney;
			if(p.alchemy.restExp+25*(type/5)>160000){
				ErrorHandler.sendErrorMessage(session, serial, type==5?OpCode.ALCHEMY_BYIMONEY_CLIENT:OpCode.ALCHEMY_BYIMONEY100_CLIENT, "留存经验已达上限，请突破重天再进行修炼！");
			}else
			if(p.level<60){
				ErrorHandler.sendErrorMessage(session, serial, type==5?OpCode.ALCHEMY_BYIMONEY_CLIENT:OpCode.ALCHEMY_BYIMONEY100_CLIENT, "该功能将在60级开启，还请您继续努力升级！");
			}else
			if(p.alchemy!=null&&p.alchemy.practiceLevel==4&&p.alchemy.pulseIndex==4&&p.alchemy.acupointNum==8&&p.alchemy.acupointLevel==10){
				ErrorHandler.sendErrorMessage(session, serial, type==5?OpCode.ALCHEMY_BYIMONEY_CLIENT:OpCode.ALCHEMY_BYIMONEY100_CLIENT, "恭喜您已修炼完所有五重天，无需继续修炼。");
			}else
			if(p.getAccount().getLongIMoney()/3600<useMoney){//元宝不足
				ErrorHandler.sendErrorMessage(session, serial, type==5?OpCode.ALCHEMY_BYIMONEY_CLIENT:OpCode.ALCHEMY_BYIMONEY100_CLIENT, "您当前元宝不足以进行修炼，请充值后再来修炼！");
			}else{//可以修炼
				packet.data.flip();
				AlchemyIBugCall call=new AlchemyIBugCall(session,packet,type==5?AlchemyIBugCall.ALCHEMY_IMONEY:AlchemyIBugCall.ALCHEMY_IMONEY100);
				Server.server.getServiceRegistry().getSyncExecutorService().schedule(call);
			}
		}
	}
	
	
	/**人物经验修炼*/
	protected void alchemyByPlayerExp(Packet packet,ClientSession session){
		int serial=packet.getInt();
		Player p=(Player)session.getClient();
		if(p!=null){
			AlchemyService service=Server.server.getServiceRegistry().getAlchemyService();
			int playerAddExpToday=p.getPlayerExpTodayAdd();
			int expLock = p.pool.getInt(Player.PROPERTY_LOCK_EXP, Player.EXP_UNLOCK);  //如果玩家锁住经验，经验不增长
			if(p.alchemy.restExp+25>160000){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ALCHEMY_BYPLAYEREXP_CLIENT, "留存经验已达上限，请突破重天再进行修炼！");
			}else
			if(p.level<60){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ALCHEMY_BYPLAYEREXP_CLIENT, "该功能将在60级开启，还请您继续努力升级！");
			}else
			if(p.alchemy!=null&&p.alchemy.practiceLevel==4&&p.alchemy.pulseIndex==4&&p.alchemy.acupointNum==8&&p.alchemy.acupointLevel==10){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ALCHEMY_BYPLAYEREXP_CLIENT, "恭喜您已修炼完所有五重天，无需继续修炼。");
			}else
			if(expLock == Player.EXP_LOCK){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ALCHEMY_BYPLAYEREXP_CLIENT, "由于您已经进行了经验锁定的操作，所以暂时无法使用该功能，请解锁经验后再来进行修炼。");
			}
			else if(p.alchemy.alchemyCount<=0){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ALCHEMY_BYPLAYEREXP_CLIENT, "您今天的4次经验修炼次数已用完，请使用元宝继续修炼！");
			}
			else if(playerAddExpToday<service.getDecPlayerExp(p.level)){//经验不足
				String errorInfo="您今日所获经验不足以进行本次修炼，还请先努力积攒经验。";
				Account account = p.getAccount();
				if(account!=null){
					String mod = null;
					if(account.getUiModel()!=null)
						mod = account.getUiModel().trim();
					if(mod!=null){
						if(mod.equals("AndroidNew") || mod.equals("AndroidLargeNew") || mod.equals("iOSNewUI") || mod.equals("iOSNewUILarge")){
						}else if(mod.equals("NewUI_AndroidLarge") || mod.equals("NewUI_Android") || mod.equals("NewUI_iOS") || mod.equals("NewUI_iOSLarge")){
						}else{
							long needPlayerExp=0;//下一级需要人物经验
							long playerExpToday=p.pool.getLong(AlchemyService.PLAYEREXP_TODAYADD, 0);
							if(playerExpToday<service.getDecPlayerExp(p.level)){
								needPlayerExp=service.getDecPlayerExp(p.level)-playerExpToday;
							}
							errorInfo=MessageFormat.format("您还需要{0}经验进行下一次修炼.", needPlayerExp);
						}
					}else{
					}
				}
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ALCHEMY_BYPLAYEREXP_CLIENT, errorInfo);
			}else
			{//可以修炼
				packet.data.flip();
				AlchemyCall call=new AlchemyCall(session,packet,AlchemyCall.ALCHEMY_EXP);
				Server.server.getServiceRegistry().getDbService().schedule(call);
			}
		}
	}
	
	/**修炼信息*/
	protected void alchemyInfo(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player p=(Player)session.getClient();
		if(p!=null){
			int practiceLevel=p.alchemy.practiceLevel;
			int pulseIndex=p.alchemy.pulseIndex;
			int acupointNum=p.alchemy.acupointNum;
			int acupointLevel=p.alchemy.acupointLevel;
			
			switch(p.alchemy.practiceLevel){
			case 4:
				p.alchemy.jewelEnhance=AlchemyService.getProperties_Value(AlchemyService.JEWEL, practiceLevel, pulseIndex, acupointNum, acupointLevel);
			case 3:
				p.alchemy.spellDefense=AlchemyService.getProperties_Value(AlchemyService.SPELLDEF, practiceLevel, pulseIndex, acupointNum, acupointLevel);
			case 2:
				p.alchemy.defense=AlchemyService.getProperties_Value(AlchemyService.DEFENSE, practiceLevel, pulseIndex, acupointNum, acupointLevel);
			case 1:
				p.alchemy.hp=AlchemyService.getProperties_Value(AlchemyService.HP, practiceLevel, pulseIndex, acupointNum, acupointLevel);
			case 0:
				p.alchemy.attackPowerup=AlchemyService.getProperties_Value(AlchemyService.ATTACK, practiceLevel, pulseIndex, acupointNum, acupointLevel)
				+AlchemyService.BREAKLEVEL_ADDATTACK*practiceLevel;
				p.alchemy.spellPower=AlchemyService.getProperties_Value(AlchemyService.SPELLATTACK, practiceLevel, pulseIndex, acupointNum, acupointLevel)
				+AlchemyService.BREAKLEVEL_ADDSPELLPOWER*practiceLevel;
				break;
			}
			
			
			Packet pt = new Packet(OpCode.ALCHEMY_INFO_SERVER);
			pt.putInt(serial);
			pt.put(p.alchemy.practiceLevel);//重天数
			pt.put(p.alchemy.pulseIndex);//脉数
			pt.put(p.alchemy.acupointNum);//穴位
			pt.put(p.alchemy.acupointLevel);//穴位等级
			//修炼的经验值
			int alchemyExp=p.pool.getInt(AlchemyService.ALCHEMYEXP);
			if((!p.alchemy.levelBreak[p.alchemy.practiceLevel])&&
					p.alchemy.acupointNum==8&&p.alchemy.practiceLevel==p.alchemy.pulseIndex&&p.alchemy.acupointLevel==10){
				alchemyExp=0;
			}
			pt.putInt(alchemyExp);
			AlchemyService service = Server.server.getServiceRegistry().getAlchemyService();
			int needExp=service.getCurrentAlchemyNeedExp(p.alchemy.practiceLevel, p.alchemy.pulseIndex);
			pt.putInt(needExp);//当前需要总经验
			
			//字符串发送UI左面显示信息
			long needPlayerExp=0;//下一级需要人物经验
			long playerExpToday=p.pool.getLong(AlchemyService.PLAYEREXP_TODAYADD, 0);
			if(playerExpToday<service.getDecPlayerExp(p.level)){
				needPlayerExp=service.getDecPlayerExp(p.level)-playerExpToday;
			}
			if(p.alchemy.alchemyCount<=0){
				needPlayerExp=service.getDecPlayerExp(p.level);
			}
			int restExp=p.alchemy.restExp;//留存经验逻辑，先显示修炼总经验
			pt.putInt(restExp);
			int alchemyCount=p.alchemy.alchemyCount;//修炼剩余次数
			int attackPowerup=(int)p.alchemy.attackPowerup;//物攻
			int spellPower=(int)p.alchemy.spellPower;//法攻
			int hp=(int)p.alchemy.hp;//生命
			int defense=(int)p.alchemy.defense;//护甲
			int spellDefense=(int)p.alchemy.spellDefense;//法防
			NumberFormat numberFormat = NumberFormat.getInstance();
			numberFormat.setMaximumFractionDigits(3);
			String jewelEnghance=numberFormat.format(p.alchemy.jewelEnhance);//宝石光效
			
			String info0=AlchemyService.alchemyPropertyChangeInfo;
			if(!(p.alchemy.practiceLevel==4&&p.alchemy.pulseIndex==4&&p.alchemy.acupointNum==8&&p.alchemy.acupointLevel==10)){
				info0+=AlchemyService.alchemyByPlayerExpInfo;
			}
			ShopService shopService=Server.server.getServiceRegistry().getShopService();
			int itemMoney=Math.round(shopService.getItemPrice(NoItemShopBuy.WUYUANBAO))/36;
			String info=MessageFormat.format(info0,
											 attackPowerup+"",spellPower+"",
											 defense+"",spellDefense+"",
											 hp+"",jewelEnghance,
											 itemMoney+"",(itemMoney*100)+"",
											 needPlayerExp+"",alchemyCount+""
											 );
			pt.putUTF(info);
			pt.put((byte)(AlchemyService.PROPERTIES[0]*10));
			pt.put((byte)(AlchemyService.PROPERTIES[1]*10));
			pt.putShort((short)(AlchemyService.PROPERTIES[2]*10));
			pt.put((byte)(AlchemyService.PROPERTIES[3]*10));
			pt.put((byte)(AlchemyService.PROPERTIES[4]*10));
			pt.put((byte)(AlchemyService.PROPERTIES[5]*1000));
			pt.putInt(AlchemyService.BREAKLEVEL_ADDATTACK);
			pt.putInt(AlchemyService.BREAKLEVEL_ADDSPELLPOWER);
			byte todayHint=(byte)p.pool.getInt(AlchemyService.ALCHEMY_HINT_TODAY,0);
			if(!(p.alchemy.acupointNum==8&&p.alchemy.practiceLevel==p.alchemy.pulseIndex&&p.alchemy.acupointLevel==10)){//未修炼满也不提示
				todayHint=-1;
			}
			pt.put(todayHint);//是否提示
			pt.putShort(itemMoney*100);//元宝百修价格	
			p.send(pt);
		}
	}
	
	/** 女人节报名*/
	protected void womenDaySignUp(Packet packet,ClientSession session){
		WomenDayInstanceService service = Server.server.getServiceRegistry().getWomenDayInstanceService();
		service.signUp(session, packet);
	}
	
	/**选美奖励列表*/
	protected void beautyParadeRewards(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player p=(Player)session.getClient();
		if(p!=null){
			int count=BeautyParadeService.items2==null?0:BeautyParadeService.items2.length;
			Packet pt = new Packet(OpCode.BEAUTYPARADE_REWARD_SERVER);
			pt.putInt(serial);
			pt.putInt(count);
			for(int i=0;i<BeautyParadeService.items2.length;i++){
				if(BeautyParadeService.items2[i]!=null){
					String names="";
					String descs="";
					for(int j=0;j<BeautyParadeService.items2[i].length;j++){
						int itemId=BeautyParadeService.items2[i][j];
						GameItem item=ObjectAccessor.createGameItem(itemId);
						names+=(j==0?"":",")+item.template.name;
						descs+=(j==0?"":"|")+item.getDesc();
					}
					pt.putString(names);
					pt.putString(descs);
				}
			}
			p.send(pt);
		}
	}
	
	/** 神秘星史*/
	protected void newYearActivity(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int type = packet.get();
		Player p = (Player)session.getClient();
		if(p != null){
			ActivityService activityService = Server.server.getServiceRegistry().getActivityService();
			Activity activity = activityService.getActivityByImpClass(NewYearActivity.class.getSimpleName());
			Date dateNow = new Date();
			try{
				if(dateNow.after(activity.getSchedule().stopTime)){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.NEWYEAR_ACTIVITY_CLIENT, "活动已经结束");
					return;
				}
			}catch(Exception e){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.NEWYEAR_ACTIVITY_CLIENT, "活动已经结束");
				return;
			}
			if(type >3||type<0){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.NEWYEAR_ACTIVITY_CLIENT, "发生错误");
				return;
			}
			if(p.level<NewYearActivity.LEVEL_LIMIT){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.NEWYEAR_ACTIVITY_CLIENT, "您尚未达到70级。");
				return;
			}
			if(type < 2){
				try{
					NewYearActivity.getReward(p, type);
				}catch(Exception e){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.NEWYEAR_ACTIVITY_CLIENT, e.getMessage());
					return;
				}
			}else if(type == 2){
				try{
					int count = NewYearActivity.getJewerlCount(p);
					if(count<NewYearActivity.JEWERLBAG_NUM){
						int itemId = NewYearActivity.JEWERLBAG_THREE;
						GameItem rewardItem = ObjectAccessor.createGameItem(itemId);
						String message = MessageFormat.format("恭喜您得到{0}一个，祝您春节快乐", rewardItem.template.name);
						PlayerTransaction tx = p.newTransaction("NEWYEARACTIVITY");
						try {
							p.bag.addGameItemComplete(rewardItem, 1, tx, true);
							tx.commit();
						} catch (Exception e) {
							tx.rollback();
							Server.server.getServiceRegistry().getMailService()
									.sendSystemMail(p.id, peony.Messages.STRING_00004, "春节活动奖励", message, 0,
											rewardItem, 1, "NEWYEARACTIVITY");
						}
						NewYearActivity.recordJewerlReward(p);
						count++;
						Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, message);
						if(count == NewYearActivity.JEWERLBAG_NUM){
							Server.server.getServiceRegistry().getChatService().sendWorldMessage("禄星本次携带的3级宝石如意袋发放完了，下边发放3级瑕疵宝石。想要3级宝石如意袋的勇士还请及时赶到。");
						}
					}else {
						Packet pt = new Packet(OpCode.OPENUI_SERVER);
						pt.putString("ui_npc_dialog");
						pt.putString("NEWYEAR_JEWELS| ");
						p.send(pt);
						return;
					}
					
				}catch(Exception e){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.NEWYEAR_ACTIVITY_CLIENT, e.getMessage());
					return;
				}
			}else if(type == 3){
				int itemId = NewYearActivity.FLAW_JEWERLBAG;
				GameItem rewardItem = ObjectAccessor.createGameItem(itemId);
				String message = MessageFormat.format("恭喜您得到{0}一个，祝您春节快乐", rewardItem.template.name);
				PlayerTransaction tx = p.newTransaction("NEWYEARACTIVITY");
				try {
					p.bag.addGameItemComplete(rewardItem, 1, tx, true);
					tx.commit();
				} catch (Exception e) {
					tx.rollback();
					Server.server.getServiceRegistry().getMailService()
							.sendSystemMail(p.id, peony.Messages.STRING_00004, "春节活动奖励", message, 0,
									rewardItem, 1, "NEWYEARACTIVITY");
				}
				NewYearActivity.recordJewerlReward(p);
			}
			Packet pt = new Packet(OpCode.NEWYEAR_ACTIVITY_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}
	}
	
	//春节送祝福
	protected void reqSendNewYearPray(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int type = packet.getByte();
		int destId = packet.getInt();
		Player p = (Player)session.getClient();
		if(p != null){
			SendNewYearPrayService service = Server.server.getServiceRegistry().getSendNewYearPrayService();
			service.sendPray(p, session, serial, type, destId);
		}
	}
	
	protected void vipNaturalProperty(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int itemId = packet.getInt();
		Player p = (Player)session.getClient();
		if(p != null){
			GameItem item = ObjectAccessor.createGameItem(itemId);
			if(item!=null){
				int[] ps = item.template.equipment.getNaturalEnhanceAtts();
				if(ps==null){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.VIP_NATURAL_PROPERTY_CLIENT, "该装备不能进行鉴定");
					return;
				}
				Packet pt = new Packet(OpCode.VIP_NATURAL_PROPERTY_SERVER);
				pt.putInt(serial);
				int type = item.template.equipment.type;
				if(item.template.equipment.isHorseEquipment()){
					type = 3;
				}
				pt.putInt(type);
				pt.putInt(ps.length);
				for(int i=0;i<ps.length;i++){
					pt.putInt(ps[i]);
					pt.putString(VipPrivilegeService.AUTO_PERFECT_NATURANENHANCE[ps[i]]);
				}
				p.send(pt);
			}
		}
	}
	
	/**
	 * VIP充值额度请求
	 */
	protected void getVIPChargeValue(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player p = (Player)session.getClient();
		if(p != null){
			Packet pt = new Packet(OpCode.VIP_CHARGE_VALUE_SERVER);
			pt.putInt(serial);
			pt.putInt(p.chargeValue);
			int nextLevelValue = VipPrivilegeService.getVIPValue(p.vipLevel+1);
			pt.putInt(nextLevelValue);
			p.send(pt);
		}
	}
	
	/**
	 * @param VIP押镖需求
	 * @param session
	 */
	protected void reqVipDemandEscort(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int convoyType = packet.getByte();
		int vipDemand = 0;	//无用
		Player p = (Player)session.getClient();
		if(p != null){
			int isPayMoney = p.pool.getInt(Player.PROPERTY_ESCORTCAR_ISPANMONEY, 0);	//之前是否消费过元宝
			if(EscortQuestService.acceptCount >= EscortQuestService.ESCORT_QUEST_MAX && isPayMoney == 0){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.VIP_ESCORT_QUEST_CLIENT, MessageFormat.format("本时段的{0}次押镖任务，已被领取完毕!",EscortQuestService.ESCORT_QUEST_MAX));
			}else{
				Server.server.getServiceRegistry().getSyncExecutorService().schedule(
						new VipDemandEscortCall(p.session, p, serial, convoyType, vipDemand));
			}
			
		}
	}
	
	/**
	 * 下发cmcc元宝充值界面的商品列表
	 * @param packet
	 * @param session
	 */
	private void sendCMCCYuanbaoList(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if (player != null) {
			AccountService service = Server.server.getServiceRegistry().getAccountService();
			if(service instanceof CmccAccountService){
				CmccAccountService cservice = (CmccAccountService)service;
				Packet pt = cservice.getCMCCYuanbaoList();
				player.send(pt);
			}

		}
	}
	
	//快速购买并自动使用物品
	public void shopQuickBuyAndUse(Packet packet, ClientSession session) {
		QuickBuyAndUseCall call = new QuickBuyAndUseCall(session, packet);
		Server.server.getServiceRegistry().getSyncExecutorService().schedule(call);
	}
	
	/** 读取随从信息*/
	protected void resAttendantInfo(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int playId = packet.getInt();
		int instId = packet.getInt();
		Player player = (Player)session.getClient();
		if (player != null) {
			Player target = (Player)ObjectAccessor.getPlayer(playId);
			if(target != null){
				if(target.attendantBag != null){
					for(Attendant attendant : target.attendantBag.attendants){
						if(attendant.instanceId == instId){
							Packet pt = new Packet(OpCode.ATTENDANT_INFO_SERVER);
							pt.putInt(serial);
							pt.put(attendant.toClientBytes(target));
							player.send(pt);
							return;
						}
					}
				}
			}
		}
	}
	
	/** 末日狂欢抽奖结果*/
	protected void awardResult(Packet packet, ClientSession session){
		AwardActivityService awardActivityService = Server.server.getServiceRegistry().getAwardActivityService();
		awardActivityService.awardGet(session, packet);
	}
	protected void chargeActivityInfo(Packet packet, ClientSession session){
		ChargeActivityService chargeService = Server.server.getServiceRegistry().getChargeActivityService();
		chargeService.chargeActivityUI(session, packet);
	}
	
	/** 批量使用物品*/
	protected void bulkUseItem(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int itemId = packet.getInt();
		int count = packet.getInt();
		Player player = (Player)session.getClient();
		if(player!=null){
			GameItem item = ObjectAccessor.createGameItem(itemId);
			if(item!=null){
				if(player.bag.getGameItemCount(itemId) < count){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.BULK_USEITEM_CLIENT, "物品数量不足");
					return;
				}
				
				ItemEffect effect = item.template.useType.effect;
				if(effect!=null){
					PlayerTransaction tx = player.newTransaction("BULKUSE");
					GameItem removeItem = player.bag.removeGameItem(itemId, -1, count, tx, true);
					if(removeItem!=null){
						if(effect instanceof AddAttendantExpEffect){
							AddAttendantExpEffect attendantEffect = (AddAttendantExpEffect)effect;
							attendantEffect.bulkUseItem(player,count);
						}else if(effect instanceof AddVipExpEffect){
							AddVipExpEffect vipExpEffect = (AddVipExpEffect)effect;
							try {
								if(player.vipLevel>=VipPrivilegeService.UPVIPLEVEL){
									player.message(-1, "您的VIP已满级，敬请期待VIP更高级别", -1, -1);
									tx.rollback();
									return;
								}
								vipExpEffect.bulkUseItem(player,count);
							} catch (Exception e) {
								tx.rollback();
								ErrorHandler.sendErrorMessage(session, serial, OpCode.BULK_USEITEM_CLIENT, e.getMessage());
								return;
							}
						}else if(effect instanceof GetExpEffect){
							GetExpEffect expEffect = (GetExpEffect)effect;
							try {
								int expLock = player.pool.getInt(Player.PROPERTY_LOCK_EXP, Player.EXP_UNLOCK);  //如果玩家锁住经验，物品不能使用
								if(expLock == Player.EXP_LOCK){
									tx.rollback();
									player.message(-1, "你已锁定经验，无法再获得经验，如想获得经验请去主城官职管理员处解锁。", -1, -1);
//									ErrorHandler.sendErrorMessage(session, serial, OpCode.BULK_USEITEM_CLIENT, "你已锁定经验，无法再获得经验，如想获得经验请去主城官职管理员处解锁。");
									return;
								}
							    expEffect.getExp(player, count, removeItem);
							} catch (Exception e) {
								tx.rollback();
								ErrorHandler.sendErrorMessage(session, serial, OpCode.BULK_USEITEM_CLIENT, e.getMessage());
								return;
							}
						}else if(effect instanceof GetHonorEffect){	//得到战功
							GetHonorEffect ghEffect = (GetHonorEffect)effect;
							try {
								ghEffect.bulkUseItem(player, count, tx);
							} catch (Exception e) {
								tx.rollback();
								ErrorHandler.sendErrorMessage(session, serial, OpCode.BULK_USEITEM_CLIENT, e.getMessage());
								return;
							}
						}else if(effect instanceof AddItemEffect){	//得到物品
							AddItemEffect aiEffect = (AddItemEffect)effect;
							try {
								aiEffect.bulkUseItem(player, removeItem, count, tx);
							} catch (Exception e) {
								tx.rollback();
								ErrorHandler.sendErrorMessage(session, serial, OpCode.BULK_USEITEM_CLIENT, e.getMessage());
								return;
							}
						}else if(effect instanceof DropItemEffect){    //掉落物品
							DropItemEffect aiEffect = (DropItemEffect)effect;
                            try {
                                aiEffect.bulkUseItem(player, removeItem, count, tx);
                            } catch (Exception e) {
                                tx.rollback();
                                ErrorHandler.sendErrorMessage(session, serial, OpCode.BULK_USEITEM_CLIENT, e.getMessage());
                                return;
                            }
                        }else if(effect instanceof ScriptEffect){
                        	ScriptEffect scriptEff = (ScriptEffect)effect;
                        	try {
                        		scriptEff.use(player, removeItem, player, tx);
                            } catch (Exception e) {
                                tx.rollback();
                                ErrorHandler.sendErrorMessage(session, serial, OpCode.BULK_USEITEM_CLIENT, e.getMessage());
                                return;
                            }
                        }else if(effect instanceof AddCardExpItemEffect){//获得卡片经验
                        	AddCardExpItemEffect cardExpEffect = (AddCardExpItemEffect)effect;
                        	try {
								cardExpEffect.bulkUseItem(player, count);
							} catch (Exception e) {
								tx.rollback();
								ErrorHandler.sendErrorMessage(session, serial, OpCode.BULK_USEITEM_CLIENT, e.getMessage());
								return;
							}
                        }else if(effect instanceof AddAlchemyExpEffect){//获得修炼经验
                        	AddAlchemyExpEffect alchemyExpEffect = (AddAlchemyExpEffect)effect;
                        	try {
								alchemyExpEffect.useItems(player, removeItem, count, tx);
							} catch (Exception e) {
								tx.rollback();
								ErrorHandler.sendErrorMessage(session, serial, OpCode.BULK_USEITEM_CLIENT, e.getMessage());
								return;
							}
                        }
						
						// 发送通知
						List<GameItem> nitems = tx.getNoticeItems();
						if (nitems != null) {
							AddItemEffect.sendItemNotice(nitems, player, item.template.name);
						}
						
						// 需要邮件发送的物品这里发送
						List<GainItem> mitems = tx.getMailItems();
						if (mitems != null) {
							DBService dbs = Server.server.getServiceRegistry().getDbService();
							player.message(-1, peony.Messages.STRING_00593, -1, -1);
					        for (GainItem gitem : mitems) {
					            GameItem addItem = gitem.getItem();
					            String itemTitle = addItem.template.name;
					            if (gitem.getCount() > 1) {
					                itemTitle += "x" + gitem.getCount();
					            }
					            Server.server.getServiceRegistry().getMailService().sendSystemMailAsync(player.id, peony.Messages.STRING_00004, itemTitle, 
					            		MessageFormat.format("您批量使用{0}后由于背包格数不足，导致多出的物品暂时寄存飞鸽中，请及时提取。", removeItem.template.name), 0,
					            		gitem.getItem(), gitem.getCount(), "ITE");
					        }
						}
						tx.commit();
					}else{
						tx.rollback();
					}
				}
			}
			Packet pt = new Packet(OpCode.BULK_USEITEM_SERVER);
			pt.putInt(serial);
			player.send(pt);
		}
	}
	
	//感恩节活动传送
	protected void ThanksGivingGoMap(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int level = packet.getByte();
		Player player = (Player)session.getClient();
		if(player!=null){
			int useItemId = 0;	//需要的物品ID
			int mapId = 0;		//地图ID
			int mapX = 0;		//坐标X
			int mapY = 0;		//坐标Y
			
			if(level == 0){//普通
				useItemId = 4523;	//普通密室通行证
				mapId = 2224;
				mapX = 200;
				mapY = 168;
				if (player.party != null && player.party.getCount() > 0) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.THANKS_GIVING_GO_CLIENT, 
						"普通密室只允许单人进入");
					return;
				}
			}else if(level == 1){//困难
				useItemId = 4524;	//困难密室通行证
				mapId = 2225;
				mapX = 203;
				mapY = 176;
				
				//已经组队
//				if (player.party != null && player.party.getCount() > 0) {
//					for(PartyMember member:player.party.members){
//						if(member.player.id != player.id){
//							Player p = member.player;
//							if(p != null){
//								VMap map = member.player.getVMap();
//								if(map != null) {
//									if(map.getMapID() == mapId) {
//										NormalInstance instance = (NormalInstance) p.map.map.instance;
//										if(instance != null) {
//											int count = 0;	//副本中怪的数量
//											for(GameObject go : map.instanceid2objects.values()) {
//												if(go.type != GameObject.TYPE_PLAYER) {
//													count++;
//												}
//											}
//											if(count < 3){
//												ErrorHandler.sendErrorMessage(session, serial, OpCode.THANKS_GIVING_GO_CLIENT, 
//														"队长重置副本后方可进入");
//												return;
//											}
//											break;
//										}
//									}
//								}
//							}
//						}
//					}
//				}
			}else{
				return;
			}
			
			PlayerTransaction tx = player.newTransaction("GNJHD");
			if (player.bag.removeGameItem(useItemId, -1, 1, tx, true) == null) {
				tx.rollback();
				ItemTemplate it = ObjectAccessor.getItemTemplate(useItemId);
				ErrorHandler.sendErrorMessage(session, serial, OpCode.THANKS_GIVING_GO_CLIENT, 
						MessageFormat.format("您还没有{0}，无法传送。", it.name));
				return;
			}
			tx.commit();
			try {
				player.goMap(mapId, mapX, mapY);
			} catch (VMapException e) {
				e.printStackTrace();
			}
		}
	}

	/** 随从升级*/
    protected void attendantLevelUp(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getAttendantFixService().attendantUpLevel(packet, session);
	}
	
    /** 随从转换经验*/
	protected void attendantChangeToExp(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getAttendantFixService().attExchangeExp(packet, session);
	}
		
	
	protected void getMapNpcs(Packet packet, ClientSession session){
		GetMapNpcFilesCall call = new GetMapNpcFilesCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void changeActivityItem(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int itemId = packet.getInt();
		int gridId = packet.getInt();
		int type = packet.getInt();
		int value = packet.getInt();
		String addValue = packet.getString();
		Player p = (Player)session.getClient();
		if(p!=null){
			PlayerTransaction tx1 = p.newTransaction("ACTIVITYITEM");
			TransactionBagGrid grid = p.bag.removeGridGameItem(gridId, itemId, -1, 1, tx1, false);
			if(grid == null || grid.getItem()==null){
				tx1.rollback();
			}else{
				tx1.commit();
				ActivityItemEffect.checkAdd(p, itemId,gridId,type, value, addValue,true);
			}
			
		}
	} 
	
	protected void chessEnter(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int type = packet.get();
		Player player = (Player)session.getClient();
		if(player!=null){
			ChessInstanceService service = Server.server.getServiceRegistry().getChessInstanceService();
			try {
				service.enterInstance(player,type);
				Packet pt = new Packet(OpCode.ENTERMAP_CHESSINSTANCE_SERVER);
				pt.putInt(serial);
				player.send(pt);
			} catch (Exception e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ENTERMAP_CHESSINSTANCE_CLIENT, e.getMessage());
			}
		}
	}
	
	protected void chessBoard(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int type = packet.get();
		Player player = (Player)session.getClient();
		if(player!=null){
			Packet pt = new Packet(OpCode.CHESSINSTANCE_BOARD_SERVER);
			pt.putInt(serial);
			int size = 0;
			pt.putInt(size);
			if(size>0){
				for(int i=0;i<6;i++){
					pt.putString(String.valueOf(i));
					pt.putInt(1);
					pt.putString(String.valueOf(i));
					pt.put(1);
					pt.putInt(72);
					pt.put(0);
					pt.put(1);
				}
			}
			player.send(pt);
		}
	}
	
	protected void alilayGetOrder(Packet packet, ClientSession session){
		AlipayGetOrderCall call = new AlipayGetOrderCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	/**
	 * 华为SDK接入创建订单
	 */
	protected void huaweiGetOrder(Packet packet, ClientSession session){
		HuaweiGetOrderCall call = new HuaweiGetOrderCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	/**
	 * 中兴SDK请求创建订单
	 */
	protected void zhongxingGetOrder(Packet packet, ClientSession session){
		ZhongXingGetOrderCall call = new ZhongXingGetOrderCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	/**
	 * 天语SDK请求创建订单
	 */
	protected void ktouchGetOrder(Packet packet, ClientSession session){
		KTouchGetOrderCall call = new KTouchGetOrderCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	/**
	 * 小米账号登陆。
	 */
	protected void accountLoginXiaomi(Packet packet, ClientSession session){
		int serial = packet.getInt();
		String uid = packet.getString();
		String sessionId = packet.getString();
		String model = packet.getString();
		String uiModel = packet.getString();
		String version = packet.getString();
		String realPhone = packet.getString();
		int playerId = 0;
		try{playerId = packet.getInt();}catch(Exception e){}
		Server.server.getServiceRegistry().getDbService().schedule(new AccountLoginXiaomiCall(session, uid, 
				sessionId, version, model, uiModel,realPhone, playerId, serial));
	}

	/**
	 * 小米SDK接入创建订单
	 */
	protected void xiaomiGetOrder(Packet packet, ClientSession session){
		XiaomiGetOrderCall call = new XiaomiGetOrderCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	/**
	 * 联想乐逗用户登录。
	 */
	protected void accountLoginLenovo(Packet packet, ClientSession session){
		int serial = packet.getInt();
		String game_id = packet.getString();
		String openid = packet.getString();
		String sessionId = packet.getString();
		String model = packet.getString();
		String uiModel = packet.getString();
		String version = packet.getString();
		String realPhone = packet.getString();
		int playerId = 0;
		try{playerId = packet.getInt();}catch(Exception e){}
		Server.server.getServiceRegistry().getDbService().schedule(new AccountLoginLenovoCall(session, game_id, openid, 
				sessionId, version, model, uiModel, realPhone, playerId, serial));
	}
	
	/**
	 * 请求当前玩家荣誉塔闯关最高级别
	 */
	protected void reqInstranceLevel(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			Packet pt = new Packet(OpCode.CYCLE_INSTANCE_LEVEL_SERVER);
			int maxLevel = player.pool.getInt(CycleInstanceMapManager.propertyOfCycleMaxLevel, 0);
			pt.put(maxLevel);
			session.send(pt);
		}
	}
	
	protected void reputeList(Packet packet, ClientSession session){
		int serial = packet.getInt();
		Player player = (Player)session.getClient();
		if(player!=null){
			ActivityService service = Server.server.getServiceRegistry().getActivityService();
			List<Activity> acts = service.getActivitysByImpClass("ThreeYearsActivity1");
			if(acts==null || acts.size()==0)
				acts = service.getActivitysByImpClass("ThreeYearsActivity11");
			if(acts==null || acts.size()==0)
				acts = service.getActivitysByImpClass("ThreeYearsActivity12");
			if(acts==null || acts.size()==0){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.THREE_YEAR_HANDIN_CLIENT, "活动未开启");
				return;
			}
			for(Activity act : acts){
				if(act!=null){
					ThreeYearsActivity1 activity = (ThreeYearsActivity1) act.getImpl();
					int[] factions = activity.factionRecord;
					int count = 0;
					for(int i=0;i<factions.length;i++){
						if(factions[i]>0)
							count++;
					}
					Packet pt = new Packet(OpCode.THREE_YEAR_REPUTE_SERVER);
					pt.putInt(serial);
					pt.put(count);
					for(int i=0;i<factions.length;i++){
						if(factions[i]>0){
							int faction = factions[i];
							pt.put(activity.reputes[i]);
							pt.put(faction);
							pt.putInt(activity.seedsRecord[i]);
							NationService nationService = Server.server.getServiceRegistry().getNationService();
							Nation nation = nationService.getNationByFaction(faction);
							pt.putUTF(nation.getKingName());
						}
					}
					session.send(pt);
				}
			}
		}
	}
	
	protected void handerinItem(Packet packet, ClientSession session){
		int serial = packet.getInt();
		Player player = (Player)session.getClient();
		if(player!=null){
			ActivityService service = Server.server.getServiceRegistry().getActivityService();
			List<Activity> acts = service.getActivitysByImpClass("ThreeYearsActivity1");
			if(acts==null || acts.size()==0)
				acts = service.getActivitysByImpClass("ThreeYearsActivity11");
			if(acts==null || acts.size()==0)
				acts = service.getActivitysByImpClass("ThreeYearsActivity12");
			if(acts==null || acts.size()==0){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.THREE_YEAR_HANDIN_CLIENT, "活动未开启");
				return;
			}
			for(Activity act : acts){
				if(act!=null){
					ThreeYearsActivity1 activity = (ThreeYearsActivity1) act.getImpl();
					try {
						activity.handinSeed(player);
						Packet pt = new Packet(OpCode.THREE_YEAR_HANDIN_SERVER);
						pt.putInt(serial);
						session.send(pt);
					} catch (Exception e) {
						ErrorHandler.sendErrorMessage(session, serial, OpCode.THREE_YEAR_HANDIN_CLIENT, e.getMessage());
					}
				}
			}
		}
	}
	
	protected void accountLogin91(Packet packet, ClientSession session){
		int serial = packet.getInt();
		String uin = packet.getString();
		String sessionId = packet.getString();
		String model = packet.getString();
		String uiModel = packet.getString();
		String version = packet.getString();
		String realPhone = packet.getString();
		int playerId = 0;
		try{playerId = packet.getInt();}catch(Exception e){}
		Server.server.getServiceRegistry().getDbService().schedule(new AccountLogin91Call(session, uin, 
				sessionId, version, model, uiModel, realPhone, playerId, serial));
	}
	
	protected void accountLogin360(Packet packet, ClientSession session){
		int serial = packet.getInt();
		String authorityCode = packet.getString();
		String model = packet.getString();
		String uiModel = packet.getString();
		String version = packet.getString();
		String realPhone = packet.getString();
		int playerId = 0;
		try{playerId = packet.getInt();}catch(Exception e){}
		Server.server.getServiceRegistry().getDbService().schedule(new AccountLogin360Call(session, serial, authorityCode, 
				model, uiModel, version, realPhone, playerId));
	}
	
	protected void processKingItem(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int type = packet.get();
		Player player = (Player)session.getClient();
		if(player!=null){
			if(type == 0){
				player.buffs.removeBuff(KingItemEffect.KINGWEAPON_BUFFID);
				player.pool.setInt(KingItemEffect.PROPERTY_USEKINGWEAPEN, 0);
				player.message(-1, "效果取消", -1, -1);
			}else if(type == 1){
				KingItemEffect.horseResetImage(player);
				player.buffs.removeBuff(KingItemEffect.KINGWEAPON_BUFFID2);
				player.pool.setInt(KingItemEffect.PROPERTY_USEKINGHORSE, 0);
				player.message(-1, "效果取消", -1, -1);
			}
		}
	}
	
	protected void processMaterialCount(Packet packet, ClientSession session){
		int serail = packet.getInt();
		int type = packet.get();
		String name = packet.getString();
		Player player = (Player)session.getClient();
		if(player!=null){
			if(player.getVMap().getId()==FeastInstanceService.MAPID){
			FeastInstance instance = (FeastInstance)player.getVMap().instance;
			if(instance!=null){
				String msg = MessageFormat.format("{0}多了,大家快去打点别的食材吧。", name);
				if(type == 0){
					msg =MessageFormat.format("{0}少了,大家快去打点{1}吧。", name,name);
				}
				ChatService service = Server.server.getServiceRegistry().getChatService();
				service.sendAreaSystemMessage(msg, Integer.parseInt(player.getVMap().getId()+""+instance.getId()));
			}
			}
			
		}
	}
	
	protected void processAntiBot(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			player.checkAntiBot(packet);
		}
	}
	
	protected void viewMenu(Packet packet,ClientSession session){
		FeastInstanceService service = Server.server.getServiceRegistry().getFeastInstanceService();
		service.showMenu(session, packet);
	}
	
	protected void feastFunction(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int type = packet.get();
		Player player = (Player)session.getClient();
		if(player!=null){
			Packet pt = new Packet(OpCode.FEAST_NPCFUNCTION_SERVICE);
			pt.putInt(serial);
			FeastInstanceService feastService = Server.server.getServiceRegistry().getFeastInstanceService();
			if(type == 0){//确定报名
				feastService.signUp(player);
			}else if(type == 1){
				FeastInstance instance = (FeastInstance)player.map.map.instance;
				if(instance!=null){
//				     int score = feastService.checkMenu(instance);
				}
			}else if(type == 2){
				FeastInstance instance = (FeastInstance)player.map.map.instance;
				if(instance!=null){
//				   feastService.getMenuRandom(instance);
				}else{
				   ErrorHandler.sendErrorMessage(session, serial, OpCode.FEAST_NPCFUNCTION_CLIENT, "副本错误");
				   return;
				}
			}
			player.send(pt);
		}
	}
	
	/** 满汉全席报名,传进场景*/
	protected void feastSignAndEnter(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int type = packet.get();
		Player player = (Player)session.getClient();
		if(player!=null){
			FeastInstanceService feastService = Server.server.getServiceRegistry().getFeastInstanceService();
			if(type == 0){//确定报名
				feastService.signUp(player);
			}else if(type == 1){//确定传进场景
				feastService.checkEnter(player);
			}
		}
	}
	
	/** 抽奖*/
	protected void gambleProcess(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int gambleId = packet.get();
		int typeId = packet.get();
		Player player = (Player)session.getClient();
		if(player!=null){
			try{
				GambleService gambleService = Server.server.getServiceRegistry().getGambleService();
				gambleService.processGamble(player, gambleId, typeId);
				Packet pt = new Packet(OpCode.GAMBLE_PROCESS_SERVICE);
				pt.putInt(serial);
				player.send(pt);
			}catch(Exception e){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.GAMBLE_PROCESS_CLIENT, e.getMessage());
			}
		}
	}
	
	/** 抽奖类型列表*/
	protected void gambleDetailList(Packet packet, ClientSession session){
		GambleService gambleService = Server.server.getServiceRegistry().getGambleService();
		gambleService.gambleDetailList(session, packet);
	}
	
	
	/** 抽奖列表*/
	protected void gambleList(Packet packet, ClientSession session){
		GambleService gambleService = Server.server.getServiceRegistry().getGambleService();
		gambleService.gambleList(session, packet);
	}
	
	/** 移动区点数购买元宝 */
	protected void cmccChargeNew(Packet packet, ClientSession session){
		CmccChargeNewCall call = new CmccChargeNewCall(session, packet);
		Server.server.getServiceRegistry().getAccountService().schedule(call);
	}
	
	/** 当乐189hi平台充值 */
	protected void downJoyCharge(Packet packet, ClientSession session){
		ChargeDownJoyCall call = new ChargeDownJoyCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	/** 云游币兑换元宝 */
	protected void yunyouBuyImoney(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new YunyouImoneyBuyCall(session, packet));
	}
	
	/** 云游android登陆 */
	protected void yunyouAccountLogin(Packet packet, ClientSession session){
		int serial = packet.getInt();
		String sessionId = packet.getString();
		String userId = packet.getString();
		String mod = packet.getString();
		String uiMod = packet.getString();
		String version = packet.getString();
		String phone = packet.getString();
		int playerId = packet.getInt();
		AccountLoginYUNYOUAndroidCall call = new AccountLoginYUNYOUAndroidCall(session, sessionId, userId,version, mod, uiMod, phone, playerId, serial);
		Server.server.getServiceRegistry().getAccountService().schedule(call);
	}
	
	protected void getPrayPrice(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player player = (Player)session.getClient();
		if(player!=null){
			Packet pt = new Packet(OpCode.PRAY_IMONEY_SERVICE);
			pt.putInt(serial);
			int price = ImoneyRockCardCall.getItemPrice();
			pt.putString(String.valueOf(price));
			player.send(pt);
		}
	}
	
	/** 云游android、java、ios登陆 */
	protected void accountLoginUc(Packet packet, ClientSession session){
		int serial = packet.getInt();
		String sdk = packet.getString();
		String mod = packet.getString();
		String uiMod = packet.getString();
		String version = packet.getString();
		String phone = packet.getString();
		int playerId = packet.getInt();
		AccountLoginUCCall call = new AccountLoginUCCall(session, sdk, version, mod, uiMod, phone, playerId, serial);
		Server.server.getServiceRegistry().getAccountService().schedule(call);
	}
	
	/**
	 * 摇卡排行榜
	 * @param packet
	 * @param session
	 */
	protected void cardExpBoard(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int type = packet.get();
		Player player = (Player)session.getClient();
		if(player!=null){
			RankingService rankingService = Server.server.getServiceRegistry().getRankingService();
			Packet pt = new Packet(OpCode.CARDEXP_ADD_SERVICE);
			pt.putInt(serial);
			if(type == 1){
				List<Ranking> cardRanking = rankingService.getOldCardRanking();
				if(cardRanking!=null){
					pt.put(cardRanking.size());
					if(cardRanking.size()>0){
						for(int i=0;i<cardRanking.size();i++){
						    Ranking ranking = cardRanking.get(i);
						    pt.putString(ranking.playerName);
						    pt.put(ranking.faction);
						    pt.putInt(ranking.value);
						    pt.putInt(i+1);
						}
					}
				}else{
					pt.put(0);
				}
			}else if(type == 2){
				List<Ranking> prayRanking = rankingService.getOldPrayRanking();
				if(prayRanking!=null){
					pt.put(prayRanking.size());
					if(prayRanking.size()>0){
						for(int i=0;i<prayRanking.size();i++){
						    Ranking ranking = prayRanking.get(i);
						    pt.putString(ranking.playerName);
						    pt.put(ranking.faction);
						    pt.putInt(ranking.value/(30*36));
						    pt.putInt(i+1);
						}
					}
				}else{
					pt.put(0);
				}
			}else{
				pt.put(0);
			}
			player.send(pt);
		}
	}
	
	protected void liMeiActivation(Packet packet, ClientSession session){
//		Server.server.getServiceRegistry().getDbService().schedule(new LiMeiActivationCall(session, packet));
	}
	
	protected void notificationBind(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new NotificationBindCall(session,packet));
	}
	
	protected void cardPrayInfo(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int npcId = packet.getInt();
		Player player = (Player)session.getClient();
		if(player!=null){
			int freeCount = 0;
			try {
				int count=0;
				for(int i=0;i<CardRockCall.npcIds.length;i++){
					peony.util.IntHashMap<Integer> prayCountOfPlayer1=CardRockCall.npcToFreeMap.get(CardRockCall.npcIds[i]);
					count+=(prayCountOfPlayer1.get(player.id)==null?0:prayCountOfPlayer1.get(player.id));
				}
				freeCount=CardRockCall.FREEROCKCARDCOUNT-count;
				if(freeCount<=0){
					freeCount=0;
				}
//				freeCount = CardRockCall.npcToFreeMap.get(npcId).get(player.id);
			} catch (Exception e) {
				e.printStackTrace();
			}
			int creditCount = 0;
			try {
				creditCount = CardRockCall.npcToCreditMap.get(npcId).get(player.id);
			} catch (Exception e) {}
			Packet pt = new Packet(OpCode.CARD_PRAY_INFO_SERVER);
			pt.putInt(serial);
			pt.put(freeCount);
			pt.putInt(creditCount);
			pt.put(CardRockCall.decCreditOfRockCard);
			int itemPrice = ImoneyRockCardCall.getItemPrice();
			pt.putString(String.valueOf(itemPrice));
			pt.putInt(player.cards.exp);
			int total = CardRockCall.getTotalCreditRockCount(CardRockCall.ROCK_TYPE_CREDIT, player.id);
			int upLimit = CardRockCall.creditLimit;
			if(player.vipLevel>=5){
				upLimit = VipPrivilegeService.ROCKCARD_UPLIMIT;
			}
			int haveCount=upLimit-total;
			if(haveCount<=0){
				haveCount=0;
			}
			pt.putShort(haveCount);
			session.send(pt);
		}
	}
	
	protected void cardUnEquip(Packet packet, ClientSession session){
		CardUnEquipCall call = new CardUnEquipCall(session, packet);
		Server.server.getWorld().schedule(call);
	}
	
	protected void cardRemoveExp(Packet packet, ClientSession session){
		CardService service = Server.server.getServiceRegistry().getCardService();
		int serial = packet.getInt();
		int cardId = packet.getInt();
		Player player = (Player)session.getClient();
		if(player!=null){
			try {
				CardInfo cardInfo = player.cards.getUnEquipCardInfo(cardId);
				if(cardInfo==null){
					if(service.getEquipCardInfo(player, cardId)!=null)
						ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_REMOVEEXP_CLIENT, "卡片已镶嵌");
					else if(service.hasMatch(player, cardId))
						ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_REMOVEEXP_CLIENT, "卡片没有经验");
					else
						ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_REMOVEEXP_CLIENT, "卡片不存在");
					return;
				}
				Card card = service.getCardByCardId(cardId);
				if(card==null){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_REMOVEEXP_CLIENT, "未收藏指定卡片");
					return;
				}
				if(card!=null&&card.buff2Id!=-1){//技能卡片
					ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_REMOVEEXP_CLIENT, "此功能暂未开启");
					return;
				}
				service.removeCardExp(player, cardInfo);
				int quality = ObjectAccessor.createGameItem(card.itemId).template.quality;
				Packet pt = new Packet(OpCode.CARD_REMOVEEXP_SERVER);
				pt.putInt(serial);
				int type=card.buff2Id==-1?0:1;
				pt.put(type);
				if(type==0){
					try {
						pt.putInt(CardUpGradeCall.getUpGradeExp(quality, 1));
					} catch (Exception e) {
						pt.putInt(0);
					}
					pt.putInt(player.cards.exp);
					pt.putUTF(service.getEnhanceDesc(cardId, cardInfo.level));
//					pt.putUTF(MessageFormat.format("{0}{1}", "下级属性：", service.getEnhanceDesc(cardId, cardInfo.level+1)));
					pt.putUTF(MessageFormat.format("{0}",  service.getEnhanceDesc(cardId, cardInfo.level+1)));
				}else{
					pt.putInt(CardUpGradeCall.getCardUpGradeNeedCount(cardInfo.level));
//					int totalCount=player.pool.getInt(CardService.getPropertyOfPlayerCard(card.id),0);
					int totalCount=CardService.getCardCount(player, card.id);
					if(totalCount>=1&&cardInfo.level>1){
						totalCount-=1;
						if(totalCount<=0){
							totalCount=0;
						}
					}
					pt.putInt(totalCount);
					Buff skillBuff=BuffUtil.createBuff(card.buff2Id, cardInfo.level, player, player, 0);
//					Buff skillBuff=BuffUtil.createSuiteBuff(/*card.buff2Id*/633, cardInfo.level);
					String desc=service.getEnhanceDesc(cardId, cardInfo.level);
					if(skillBuff!=null){
						desc=skillBuff.getDesc();
					}
					pt.putUTF(desc);
					Buff skillBuffNext=BuffUtil.createBuff(card.buff2Id, cardInfo.level+1, player, player, 0);
					if(skillBuffNext!=null){
						desc=skillBuffNext.getDesc();
					}
					pt.putUTF(desc);
				}
				session.send(pt);
			} catch (CardException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_REMOVEEXP_CLIENT, e.getMessage());
			}
		}
	}
	
	protected void cardEquip(Packet packet, ClientSession session){
		EquipCardCall call = new EquipCardCall(session, packet);
		Server.server.getWorld().schedule(call);
	}
	
	protected void cardUpGrade(Packet packet, ClientSession session){
		CardUpGradeCall call = new CardUpGradeCall(session, packet);
		Server.server.getWorld().schedule(call);
	}
	
	protected void prayCards(Packet packet, ClientSession session){
		CardRockCall call = new CardRockCall(session, packet);
		Server.server.getWorld().schedule(call);
	}
	
	/** 星辉提升成功率*/
	protected void starPromoteRate(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int itemId = packet.getInt();
		int instanceId = packet.getInt();
		Player player = (Player)session.getClient();
		if(player!=null){
			Object[] os = ItemUtil.findPlayerEquipment(player, itemId,instanceId);
			if (os != null) {
				GameItem item = (GameItem) os[0];
				if (!item.template.isEquipment()) {
					return;
				}
				if (!item.template.equipment.canJudgeStar) {
					ErrorHandler.sendErrorMessage(session, serial,
								OpCode.STARPROMOTE_APPLYRATE_CLIENT, peony.Messages.STRING_00193);
						return;
				}
				if(item.object!=null && item.object instanceof ItemEnhance){
					ItemEnhance ie = (ItemEnhance)item.object;
					int star = ie.getStar();
					if(star >= 10){
						ErrorHandler.sendErrorMessage(session, serial, 
								OpCode.STARPROMOTE_APPLYRATE_CLIENT, "此件装备已经升级到10级，请勿重复升级");
						return;
					}
					if(star < 9){
						ErrorHandler.sendErrorMessage(session, serial, 
									OpCode.STARPROMOTE_APPLYRATE_CLIENT, "此件装备不为9星，无法进行星级提升");
						return;
					}
					LogUtil.logStarPromoteTry(player, item);
					Packet pt = new Packet(OpCode.STARPROMOTE_APPLYRATE_SERVER);
					pt.putInt(serial);
					pt.putInt(player.pool.getInt(StarPromoteCall.getProperty(instanceId),StarPromoteCall.SUCC_BASERATE));
					float rate = 1.0f;
					if(player.vipLevel>=7){
						rate = 1-VipPrivilegeService.STARPROMOTE_DECCREDIT_RATIO;
					}
					pt.putInt((int)(StarPromoteCall.getCredit((byte)1)*rate));
					pt.putInt((int)(StarPromoteCall.getCredit((byte)2)*rate));
					player.send(pt);
				} else {
					ErrorHandler.sendErrorMessage(session, serial, 
							OpCode.STARPROMOTE_APPLYRATE_CLIENT, "此件装备不为9星，无法进行星级提升");
				}
			}
		}
	}

	/** 星辉提升*/
	protected void starPromote(Packet packet,ClientSession session){
       Server.server.getServiceRegistry().getDbService().schedule(new StarPromoteCall(session,packet));
	}
	
	/** 缩短扫荡时间*/
	protected void decSweepTime(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new DecInstanceTimeCall(packet,session));
	}
	
	/** 副本扫荡列表*/
	protected void instanceSweepList(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player player = (Player)session.getClient();
		if(player!=null){
		//下发副本扫荡列表
		  InstanceSweepService service = Server.server.getServiceRegistry().getInstanceSweepService();
		  service.instanceSweepList(player,serial);
		}
	}
	
	/** 开始扫荡副本*/
	protected void instanceSweep(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new InstanceSweepCall(packet,session));
	}
	
	/** 立即读完书籍*/
	protected void quickDecBookTime(Packet packet,ClientSession session){
//		Server.server.serviceRegistry.getDbService().schedule(new QuickDecBookTimeCall(packet,session));
		Server.server.getServiceRegistry().getSyncExecutorService().schedule(new QuickDecBookTimeCall(packet,session));
	}
	
	/** 立即完成费用申请*/
	protected void bookPay(Packet packet,ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			p.books.decBookTimePay(session, packet);
		}
	}
	
	/** 查看书籍描述*/
	protected void reviewBookDec(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int bookId = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
			Packet pt = new Packet(OpCode.REVIEW_BOOKDEC_SERVER);
			pt.putInt(serial);
			BookConfig bc = BookUtil.getBookConfig(bookId);
			pt.putString(bc.dec);
			p.send(pt);
		}
	}
	
	/**
	 * @param 领取每日押镖任务
	 * @param session
	 */
	protected void acceptEscortQuest(Packet packet, ClientSession session){
		int serial = packet.getInt();
		Player player = (Player)session.getClient();
		if(player != null){
			try {
				EscortQuestService service = Server.server.getServiceRegistry().getEscortQuestService();
				service.acceptEscortQuest(player, session, serial);
			} catch (EscortException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_CONVOY_CLIENT, e.getMessage());
			}
		}
	}
	
	/**
	 * @param 刷新镖车品质
	 * @param session
	 */
	protected void reFreshEscort(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int isVip = packet.getByte();
		Player p = (Player)session.getClient();
		if(p != null){
			int isPayMoney = p.pool.getInt(Player.PROPERTY_ESCORTCAR_ISPANMONEY, 0);	//之前是否消费过元宝
			if(EscortQuestService.acceptCount >= EscortQuestService.ESCORT_QUEST_MAX && isPayMoney == 0){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCEPT_REFRESH_ESCORT_CLIENT, MessageFormat.format("本时段的{0}次押镖任务，已被领取完毕!",EscortQuestService.ESCORT_QUEST_MAX));
			}else{
				Server.server.getServiceRegistry().getSyncExecutorService().schedule(
						new ReFreshEscortCall(p.session, null, p, serial, isVip));
			}
			
		}
	}
	
	/**
	 * @param 开始押镖
	 * @param session
	 */
	protected void acceptStartEscort(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int convoyType = packet.getByte();
		Player player = (Player)session.getClient();
		if(player != null){
			try {
				EscortQuestService service = Server.server.getServiceRegistry().getEscortQuestService();
				service.startEscort(player, convoyType);
			} catch (EscortException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_CONVOY_CLIENT, e.getMessage());
			}
		}
	}
	
	protected void delDelCredit(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int instanceId = packet.getInt();
		int pointCredit = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
			if(pointCredit < 2){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_IS_DELCREDIT_ClIENT,"输入的战功数不能少于2点");
				return;
			}
			Attendant att = p.attendantBag.getAttendant(instanceId);
			if(att!= null){
				PlayerTransaction tx = p.newTransaction("ADDLOYAL");
				try {
					int surplus = pointCredit%2;	//2战功=1忠诚
					p.decCredit(pointCredit - surplus, tx, false);
					int curLoyal = att.loyal + pointCredit/2;
					att.addLoyal(curLoyal);
					tx.commit();
					if(surplus > 0){
						p.message(-1, MessageFormat.format("2战功激励1点忠诚！随从增加{0}忠诚度，返还1点战功", pointCredit/2), -1, -1);
					}else{
						p.message(-1, MessageFormat.format("随从增加{0}忠诚度", pointCredit/2), -1, -1);
					}
					Packet pt = new Packet(OpCode.ATTENDANT_IS_DELCREDIT_SERVER);
					pt.putInt(serial);
					session.send(pt);
					
				} catch (NoEnoughValueException e) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_IS_DELCREDIT_ClIENT,peony.Messages.STRING_01159);
					return;
				}
			}
		}
	}
	
	/** 袁绍副本NPC功能*/
	protected void fiveElementTran(Packet packet,ClientSession session){
		FiveElementService service= Server.server.getServiceRegistry().getFiveElementService();
		service.checkAccess(session, packet);
	}
	
	/** 缩短阅读时间 */
	protected void payForRead(Packet packet,ClientSession session){
//		Server.server.serviceRegistry.getDbService().schedule(new PayForReadCall(packet,session));
		Server.server.getServiceRegistry().getSyncExecutorService().schedule(new PayForReadCall(packet,session));
	}
	
	/** 暂停阅读*/
//	protected void pauseReadBook(Packet packet,ClientSession session){
//		int serial = packet.getInt();
//		int bookId = packet.getInt();
//		Player player = (Player)session.getClient();
//		if(player!=null){
//			try {
//				player.books.pauseReadBook(bookId);
//				Packet pt = new Packet(OpCode.PLAYER_PAUSEREAD_SERVER);
//				pt.putInt(serial);
//				player.send(pt);
//			} catch (Exception e) {
//				ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.PLAYER_PAUSEREAD_CLIENT, e.getMessage());
//			}
//		}
//	}
	
	/** 书籍列表*/
	protected void getBookList(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player player = (Player)session.getClient();
		if(player!=null){
			Packet pt = new Packet(OpCode.PLAYER_BOOKLIST_SERVER);
			pt.putInt(serial);
			pt.putShort(player.books.books.size());
			for(Book b : player.books.books.values()){
				BookConfig bc = BookUtil.getBookConfig(b.id);
				pt.putInt(b.getId());
				pt.putString(bc.getTitle());
				pt.putInt(b.chapter);
				BookChapter bookChapter = BookUtil.getBookChapter(b.chapter+1, bc);
				byte state = b.onRead;
				if(b.onRead != Book.STATE_READ && bookChapter==null){
					state = 3;
				}
				pt.put(state);
				int minute = 0;
				if(b.onRead == Book.STATE_READ){
					if(bc.auto==1){
						bookChapter = BookUtil.getBookChapter(b.chapter, bc);
					    minute = (int)((bookChapter.time*60*1000l -(Time.currentTimeMillis(Time.currTime)-b.startReadTime+b.alreadyRead))/(60*1000l));
					}else{
						minute = (int)(Math.ceil((b.startReadTime-System.currentTimeMillis())/(60*1000l)));
					}
				}else{
					if(bookChapter !=null){
						if(player.vipLevel>=2){
							 minute = (int) (bookChapter.time*(1-VipPrivilegeService.BOOK_DECTIME_RATIO));
						}else{
							minute = bookChapter.time;
						}
					}
				}
				pt.putInt(minute);
				int count = PayForReadCall.pow(b.payTimes);
				int price = (int)(Server.server.getServiceRegistry().getShopService().getItemPrice(NoItemShopBuy.LIUSHIYUANBAO)/36);
				int Yb = (int)price*count;
				if(Yb>=588888){
					count = (int)(588888/price);
				}
				pt.putString(String.valueOf(count * price));
				pt.putString(b.getPropertyName(bc));
				pt.put(bc.auto);
			}
			player.send(pt);
		}
	}
	
	/** 开始阅读*/
	protected void readBook(Packet packet,ClientSession session){
	   int serial = packet.getInt();
	   int bookId = packet.getInt();
	   Player player = (Player)session.getClient();
	   if(player!=null){
		   if(player.level<Books.LEVELLIMIT){
			   ErrorHandler.sendErrorMessage(session, serial, OpCode.PLAYER_READBOOK_CLIENT, "对不起您现在还不能阅读书籍，请到30级再来阅读");
			   return;
		   }
		   try {
			   BookConfig bc = BookUtil.getBookConfig(bookId);
			   Book b = player.books.getBookById(bookId);
			   if(b!=null){
				   player.books.readBook(bookId,bc);
				   LogUtil.logReadBook(player, b);
				   BookChapter level = BookUtil.getBookChapter(b.chapter, bc);
				   int leftTime =level.time;
				   if(player.vipLevel>=2){
					   leftTime = (int) (level.time*(1-VipPrivilegeService.BOOK_DECTIME_RATIO));
				   }
				   Packet pt = new Packet(OpCode.PLAYER_READBOOK_SERVER);
				   pt.putInt(serial);
				   pt.putInt(bookId);
				   pt.putInt(b.chapter);
				   pt.putInt(leftTime);
				   pt.put(b.onRead);
				   pt.putString(b.getPropertyName(bc));
				   player.send(pt);
			   }
		   } catch (Exception e) {
			   ErrorHandler.sendErrorMessage(session, serial, OpCode.PLAYER_READBOOK_CLIENT, e.getMessage());
		   }
	   }	
	}
	
	protected void refreshPropertyPoint(Packet packet,ClientSession session){
		Server.server.serviceRegistry.getDbService().schedule(new RefreshPropertyPointCall(packet,session));
	}
	
	protected void valentineList(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new ValentineListCall(session, packet));
	}
	
	protected void getShopItemPrice(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			int itemId = packet.getInt();
			int shopId = packet.getShort();
			int itemCount = packet.getShort();
			ShopService service = Server.server.getServiceRegistry().getShopService();
			float price = 0;
			try {
				price = service.getItemPriceInAppointShop(itemId, shopId);
			} catch (Exception e) {
				price = service.getFilterItemPrice(itemId);
			}
			price = price * itemCount;
			DecimalFormat df = new DecimalFormat("0.00");
			String showPrice = df.format(price);
			Packet pt = new Packet(OpCode.SHOPITEM_PRICE_SERVER);
			pt.putInt(serial);
			pt.putString(showPrice);
			session.send(pt);
		}
	}
	
	protected void salaryInfo(Packet packet,ClientSession session){
		Server.server.serviceRegistry.getDbService().schedule(new SalaryInfoCall(session, packet));
	}
	
	
	protected void clientDirectoryBubbleList(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			int type = packet.getByte();
			int page = packet.getByte();
			int count = packet.getByte();
			Packet pt = new Packet(OpCode.CLIENT_DIRECTORY_LIST_SERVER);
			writeDirectory(player, pt, serial, page, count, 0, false, type);
			player.send(pt);
		}
	}
	
	protected void clientDirectoryTimeList(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			int week = packet.getByte();
			int page = packet.getByte();
			int count = packet.getByte();
			Packet pt = new Packet(OpCode.CLIENT_DIRECTORY_LIST_SERVER);
			writeDirectory(player, pt, serial, page, count, week, true, -1);
			player.send(pt);
		}
	}
	
	protected void clientDirectoryNomalList(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null && player.directory!=null){
			int serial = packet.getInt();
			int page = packet.getByte();
			int count = packet.getByte();
			Packet pt = new Packet(OpCode.CLIENT_DIRECTORY_LIST_SERVER);
			writeDirectory(player, pt, serial, page, count, 0, false, -1);
			player.send(pt);
		}
	}
	
	private void writeDirectory(Player player, Packet pt, int serial, int page, int count, int weekDay, boolean week, int type){
		pt.putInt(serial);
		pt.put(ClientDirectory.getWeekDay());
		List<Directory> list = null;
		if(weekDay>0 && week)
			list = player.directory.getDirectorys(weekDay, false, false, false, false);
		else if(type==0)
			list = player.directory.getDirectorys(0, true, false, false, false);
		else if(type==1)
			list = player.directory.getDirectorys(0, false, true, false, false);
		else if(type==2)
			list = player.directory.getDirectorys(0, false, false, true, false);
		else if(type==3)
			list = player.directory.getDirectorys(0, false, false, false, true);
		else
			list = player.directory.getDirectorys(0, false, false, false, false);
		pt.putInt(list.size());
		List<Directory> temp = new ArrayList<Directory>();
		for(int i=page*count;i<page*count+count;i++){
			try {
				if(list.get(i)!=null)
					temp.add(list.get(i));
			} catch (Exception e) {
			}
		}
		pt.putInt(temp.size());
		for(Directory d : temp){
			pt.putString(d.name);
			pt.putString(DirectoryType.parseTime(d.timeScheduleHour, d.timeScheduleMin));
			pt.putString(d.description);
			pt.putString(d.directoryList);
			pt.put(d.difficulty);
			if(d.location==null || d.location.length==0){
				pt.putShort(0);
				pt.putShort(0);
				pt.putShort(0);
			}else{
				pt.putShort(d.location[0]);
				pt.putShort(d.location[1]/8);
				pt.putShort(d.location[2]/8);
			}
			pt.putString(GameMapInfo.locationToString1(Server.server.getServiceRegistry().
					getDataService().data, d.location, false));
			pt.put(d.getRewardCount());
			if(d.item1>0 && d.count1>0)
				writeDirecotyPacket(pt, d.item1, d.count1);
			if(d.item2>0 && d.count2>0)
				writeDirecotyPacket(pt, d.item2, d.count2);
			if(d.item3>0 && d.count3>0)
				writeDirecotyPacket(pt, d.item3, d.count3);
			if(d.item4>0 && d.count4>0)
				writeDirecotyPacket(pt, d.item4, d.count4);
			pt.putString(d.rewardDesc);
		}
	}
	
	private void writeDirecotyPacket(Packet pt, int itemId, int count){
		GameItem item = ObjectAccessor.createGameItem(itemId);
		pt.put(count);
		pt.put(item.toClientBytes(item.template));
	}
	
	/** 显示打孔价格 */
	protected void showAddHole(Packet packet,ClientSession session){
		JewelService js = Server.server.getServiceRegistry().getJewelService();
		js.showAddHolePrice(session, packet);
	}
	
	/** 越南Viettel卡充值 */
	protected void viettelCharge(Packet packet, ClientSession session) {
		Server.server.serviceRegistry.getDbService().schedule(new ViettelCardChargeCall(session, packet));
	}
	
	/**购买军团商店物品**/
	protected void tongShopBuy(Packet packet,ClientSession session){
		Server.server.serviceRegistry.getDbService().schedule(new TongShopItemBuy(session,packet));
	}
	
	/**军团商店物品列表**/
	protected void tongShopList(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
			TongService ts = Server.server.getServiceRegistry().getTongService();
			Packet pt = new Packet(OpCode.TONG_SHOP_LIST_SERVER);
			pt.putInt(serial);
			pt.putInt(p.contribute);
			pt.putInt(p.bag.getGameItemCount(TongService.suiPianIds[p.clazz]));
			pt.putInt(ts.shopItems.size());
			for(int i = 0;i < ts.shopItems.size();i++){
				ItemTemplate it = ts.shopItems.get(ts.shopItemIds[i]);
				pt.putInt(it.id);
				pt.putString(it.name);
				pt.putInt(it.showType);
				pt.putInt(ts.needContributes[i]);
				pt.putInt(ts.needSuiPian[i]);
			}
			session.send(pt);
		}
	}
	
	/**升级专属科技**/
	protected void tongLeveUpSkill(Packet packet,ClientSession session){
		Server.server.serviceRegistry.getDbService().schedule(new TongSkillLevelUpCall(session,packet));
	}
	
	/**领取百宝箱物品**/
	protected void tongGetBaiBaoItem(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int itemId = 0;
		try{itemId = packet.getInt();}catch(Exception e){}
		Player p = (Player)session.getClient();
		if(p==null)
			return;
		TongService ts = Server.server.getServiceRegistry().getTongService();
		try {
			if(itemId==0)
				ts.getBaiBaoItem(p, TongService.BAIBAOXIANG, serial);
			else
				ts.getBaiBaoItem(p, itemId);
		} catch (TongException e) {
			ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_GET_BAIBAO_CLIENT, e.getMessage());
		}
	}
	
	/**使用军团百宝箱**/
	protected void tongUseBox(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player p = (Player)session.getClient();
		if(p==null) 
			return;
		TongService ts = Server.server.getServiceRegistry().getTongService();
		try {
			ts.useBaibaoBox(p);
		} catch (TongException e) {
			ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_USER_BAIBAO_BOX_CLIENT, e.getMessage());
		}
	}
	
	/**请求退出军团**/
	protected void requestExitTong(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new TongExitRequestCall(session,packet));
//		int serial = packet.getInt();
//		Player p = (Player)session.getClient();
//		if(p != null){
//			TongService ts = Server.server.getServiceRegistry().getTongService();
//			int num = ts.getPlayerContributeInTong(p);
//			Packet pt = new Packet(OpCode.TONG_REQUEST_EXIT_SERVER);
//			pt.putInt(serial);
//			pt.putInt(num);
//			p.send(pt);
//		}
	}
	
	/**军团官员更改自动接收新人状态**/
	protected void changeApplyStatus(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int status = packet.getInt();
		TongService ts = Server.server.getServiceRegistry().getTongService();
		try {
			ts.setTongAutoAcceptStatus((Player)session.getClient(),status);
		} catch (TongException e) {
			ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_AUTO_APPLY_STATUS_CLIENT, e.getMessage());
		}
	}
	
	/**申请加入军团(通过自动提醒加入军团系统)**/
	protected void applyJoinTong(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new TongJoinApply(session,packet));
	} 
	
	/**请求开启自动接受新人的军团列表**/
	protected void requestTongList(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player p = (Player)session.getClient();
		TongService ts = Server.server.getServiceRegistry().getTongService();
		List<Tong> factionTong = new ArrayList<Tong>();
		for(int tongId : ts.autoAcceptTongs){
			Tong t = ts.getTong(tongId);
			if(t!=null && t.peoplenum < TongService.LEVEL_CONFIG[t.level][0] && t.getTongFaction()==p.faction)
				factionTong.add(t);
		}
//		Iterator<Tong> tongs = ts.autoAcceptTongs.values().iterator();
//		while(tongs.hasNext()){
//			Tong t = tongs.next();
//			if(t.peoplenum < TongService.LEVEL_CONFIG[t.level][0] && t.pool.getInt(TongService.TONG_FACTION, 0) == p.faction){
//				factionTong.add(t);
//			}
//		}
		if(factionTong!=null && factionTong.size()>0){
		     ts.convertSort(factionTong);
		}
		Packet pt = new Packet(OpCode.TONG_OPEN_APPLY_LIST_SERVER);
		pt.putInt(serial);
		pt.putInt(factionTong.size());
		Iterator<Tong> it =factionTong.iterator();
		while(it.hasNext()){
			Tong t = it.next();
			pt.putInt(t.id);
			pt.putString(t.name);
			pt.putInt(t.peoplenum);
			pt.putInt(TongService.LEVEL_CONFIG[t.level][0]);
		}
		session.send(pt);
	}
	
	/**
	 * 日文版hangame邀请好友通知billing服务器
	 * @param packet
	 * @param session
	 */
	protected void hangameInviteFriends(Packet packet, ClientSession session){
	    Player p = (Player)session.getClient();

	    if(p != null){
	        String inviteResult = packet.getString();
	        String userIds = inviteResult.replaceAll("::", ",");
	        Server.server.getServiceRegistry().getDbService().schedule(new HangameInviteFriendCall(session, p.getAccount().getName(), p.id, userIds));
	    }
	}
	
	protected void cycleInstanceRanking(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new CycleInstanceRanking(session,packet));
	}
	
	protected void askForGift(Packet packet, ClientSession session){
		AskForGiftService service = Server.server.getServiceRegistry().getAskForGiftService();
		service.askForGiftInvite(session, packet);
	}
	
	protected void payInviteOk(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new PayForOtherCall(session,packet));
	}
	
	protected void payReject(Packet packet, ClientSession session){
		AskForGiftService service = Server.server.getServiceRegistry().getAskForGiftService();
		service.askForGiftReject(packet, session);
	}
	
	protected void getAwardItem(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int type = packet.get();
		int itemId = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
			if(type == 0){
				AwardService as = Server.server.getServiceRegistry().getAwardService();
				int[] awardId = new int[2];
				try {
					awardId = as.getAwardByPlayerId(p.id);
					as.processGetAwardOver(p);
				} catch (AwardException e) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GET_AWARDITEM_CLIENT,e.getMessage());
					return;
				}
				
				if(itemId == awardId[0]){
					int count = p.pool.getInt(AwardService.PROPERTY_GETAWARD_NUM,0);
					LogUtil.logAward(p, count, awardId[0]);
					GameItem item = ObjectAccessor.createGameItem(itemId);
					PlayerTransaction tx = p.newTransaction("GETAWARD");
					if(!p.bag.addGameItem(item, awardId[1], tx, true)){
	//				Gain gain = new Gain(p);
	//				gain.addGainItem(item, awardId[1]);
	//				try {
	//					p.addGainComplete(gain, tx, true);
	//					tx.commit();
	//				} catch (NoEnoughSpaceException e) {
						tx.rollback();
						MailService mailService = Server.server.getServiceRegistry().getMailService();
						mailService.sendSystemMail(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_00050, peony.Messages.STRING_00050, 
								0, item, awardId[1], "JEWELBAG");
						Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id,peony.Messages.STRING_00051);
					}else{
					    tx.commit();
					}
					Packet pt = new Packet(OpCode.GET_AWARDITEM_SERVER);
					pt.putInt(serial);
					int totalTime = 1;
					int getAwardCount = p.pool.getInt(AwardService.PROPERTY_GETAWARD_NUM, 0);
					if(p.vipLevel>=3){
						totalTime = 2;
					}
					pt.putInt(totalTime-getAwardCount);
					p.send(pt);
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GET_AWARDITEM_CLIENT,peony.Messages.STRING_00052);
				}
			}else if(type == 1){
				AwardActivityService awardActivityService = Server.server.getServiceRegistry().getAwardActivityService();
				GameItem awardItem;
				try {
					awardItem = awardActivityService.getAwardByPlayerId(p.id);
					awardActivityService.processGetAwardOver(p);
				} catch (Exception e) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GET_AWARDITEM_CLIENT,e.getMessage());
					return;
				}
				
				if(itemId == awardItem.template.id){
//					GameItem item = ObjectAccessor.createGameItem(itemId);
					PlayerTransaction tx = p.newTransaction("GETAWARD");
					if(!p.bag.addGameItem(awardItem, 1, tx, true)){
						tx.rollback();
						MailService mailService = Server.server.getServiceRegistry().getMailService();
						mailService.sendSystemMail(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_00050, peony.Messages.STRING_00050, 
								0, awardItem, 1, "JEWELBAG");
						Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id,peony.Messages.STRING_00051);
					}else{
					    tx.commit();
					}
					Packet pt = new Packet(OpCode.GET_AWARDITEM_SERVER);
					pt.putInt(serial);
					p.send(pt);
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GET_AWARDITEM_CLIENT,peony.Messages.STRING_00052);
				}
			}else if(type == 2){
				AwardActivityService awardActivityService = Server.server.getServiceRegistry().getAwardActivityService();
				GameItem awardItem;
				try {
					awardItem = awardActivityService.getAwardByPlayerId(p.id);
					awardActivityService.processGetAwardOver(p);
				} catch (Exception e) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GET_AWARDITEM_CLIENT,e.getMessage());
					return;
				}
				
				if(itemId == awardItem.template.id){
//					GameItem item = ObjectAccessor.createGameItem(itemId);
					PlayerTransaction tx = p.newTransaction("GETAWARD");
					if(!p.bag.addGameItem(awardItem, 1, tx, true)){
						tx.rollback();
						MailService mailService = Server.server.getServiceRegistry().getMailService();
						mailService.sendSystemMail(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_00050, peony.Messages.STRING_00050, 
								0, awardItem, 1, "JEWELBAG");
						Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id,peony.Messages.STRING_00051);
					}else{
					    tx.commit();
					}
					Packet pt = new Packet(OpCode.GET_AWARDITEM_SERVER);
					pt.putInt(serial);
					p.send(pt);
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GET_AWARDITEM_CLIENT,peony.Messages.STRING_00052);
				}
			}
		}
	}
	
	protected void getAward(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AwardGetCall(session,packet));
	}
	
	protected void getAwardItems(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int type = packet.get();
		Player p = (Player)session.getClient();
		if(p!=null){
			if(type == 0){
				AwardService as = Server.server.getServiceRegistry().getAwardService();
				if(as.hasGetAward(p)){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GET_AWARDITEM_CLIENT,"今天抽奖次数已用完，请明天再来");
					return;
				}
				if(as.isGetAward(p.id)){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GET_AWARDITEM_CLIENT,peony.Messages.STRING_00053);
					return;
				}
				if(!as.isOnLineOneHour(p.id)){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GET_AWARDITEM_CLIENT,peony.Messages.STRING_00054);
					return;
				}
				try {
					as.requestAward(p);
				} catch (AwardException e) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GET_AWARD_ITEMS_CLIENT, e.getMessage());
				}
			}else if(type == 1){
				AwardActivityService awardActivityService = Server.server.getServiceRegistry().getAwardActivityService();
				try {
					awardActivityService.getAwardItems(p);
				} catch (Exception e) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GET_AWARD_ITEMS_CLIENT, e.getMessage());
				}
			}else if(type == 2){
				AwardActivityService awardActivityService = Server.server.getServiceRegistry().getAwardActivityService();
				try {
					awardActivityService.getAwardItems(p);
				} catch (Exception e) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GET_AWARD_ITEMS_CLIENT, e.getMessage());
				}
			}
		}
	}
	
	protected void vow(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int type = packet.getByte();
		int limit = 1;
		try {limit = packet.getByte();} catch (Exception e1) {}
		Player player = (Player)session.getClient();
		if(player!=null){
			ActivityService service = Server.server.getServiceRegistry().getActivityService();
			List<Activity> acts = service.getActivitysByImpClass("VowActivity");
			if(acts==null || acts.size()==0)
				acts = service.getActivitysByImpClass("VowActivity1");
			if(acts==null || acts.size()==0)
				acts = service.getActivitysByImpClass("VowActivity2");
			if(acts==null || acts.size()==0){
				player.message(-1, peony.Messages.STRING_00055, -1, -1);
//				ErrorHandler.sendErrorMessage(session, serial, OpCode.VOW_CLIENT, "活动暂未开启");
				return;
			}
//			boolean findCreature = false;
			if(type<4){
				for(Activity act : acts){
					if(act!=null && act.getImpl()!=null){
						VowActivity activity = (VowActivity)act.getImpl();
						if(activity.getMapId()==player.map.id){
//							findCreature = true;
							try {
								if(type==0){
									activity.vow(player);
								}else if(type==1){
									activity.pray(player, serial, limit);
								}else if(type==2){
									activity.flooding(player);
								}
								if(type!=1){
									Packet pt = new Packet(OpCode.VOW_SERVER);
									pt.putInt(serial);
									//wish tree.
									session.send(pt);
									return;
								}
							} catch (Exception e) {
								player.message(-1, e.getMessage(), -1, -1);
//							ErrorHandler.sendErrorMessage(session, serial, OpCode.VOW_CLIENT, e.getMessage());
								return;
							}
						}
					}
				}
			}else if(type==4){
//				if(!findCreature){
					//主界面祈福
					Activity act = acts.get(0);
					if(act!=null && act.getImpl()!=null){
						VowActivity activity = (VowActivity)act.getImpl();
						Server.server.getServiceRegistry().getSyncExecutorService().schedule(
								new VowIbuyCall(player.session, activity, player, serial, limit)); 
					}else{
						player.message(-1, peony.Messages.STRING_00055, -1, -1);
					}
//				}
			}
		}
	}
	
	protected void monthPay(Packet packet,ClientSession session){
		MonthlyPayService service = Server.server.getServiceRegistry().getMonthlyPayService();
		service.monthlyPay(session, packet);
	}
	
	protected void monthPayList(Packet packet,ClientSession session){
		MonthlyPayService service = Server.server.getServiceRegistry().getMonthlyPayService();
		service.monthPayList(session, packet);
	}
	
	protected void cycleInstanceGoMap(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int level = packet.getByte();
		Player player = (Player)session.getClient();
		if(player!=null){
			CycleInstanceMapManager manager = Server.server.getServiceRegistry().getCycleInstanceMapManager();
			int dieDay = player.pool.getInt(CycleInstanceMapManager.propertyOfCycleDieDay, 0);
			int lastCycleDay = player.pool.getInt(CycleInstanceMapManager.propertyOfCycleDay, 0);
			if(dieDay==CycleInstanceMapManager.currentDay || lastCycleDay==CycleInstanceMapManager.currentDay){
				player.message(-1, peony.Messages.STRING_00056, -1, -1);
				return;
			}
			int maxLevel = player.pool.getInt(CycleInstanceMapManager.propertyOfCycleMaxLevel, 0);
			if(maxLevel>0 && level/10>maxLevel/10 || maxLevel==0 && level>1){
				player.message(-1, peony.Messages.STRING_00057, -1, -1);
				return;
			}
			manager.setWannaLevel(player.id, level);
			try {
				player.goMap(CycleInstanceMapManager.mapId.get(new Integer(player.clazz)), CycleInstanceMapManager.x, CycleInstanceMapManager.y);
			} catch (VMapException e) {
				e.printStackTrace();
			}
		}
	}
	
	/** 聊天转发微博*/
	protected void transformWeibo(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getWeiboExecutorService().schedule(new TransformWeiboCall(session, packet));
//		int serial = packet.getInt();
//		int targetId = packet.getInt();
//		Player p = (Player)session.getClient();
//		Actor targetActor = Server.server.getServiceRegistry().getActorCacheService().find(targetId);
//		if(p!=null){
//			Packet pt = new Packet(OpCode.TRANSFORM_WEIBO_SERVER);
//			pt.putInt(serial);
//			if(targetActor!=null){
//				TongService tongService = Server.server.getServiceRegistry().getTongService();
//				TongMember tongMember = tongService.getPlayerInfo(p.id);
//				TongMember targetTongMember = tongService.getPlayerInfo(targetId);
//				if(p.faction != targetActor.faction){
//					pt.put(1);
//				} else {
//					if(tongMember!=null && targetTongMember == null){
//						pt.put(0);
//					} else {
//						pt.put(1);
//					}
//				}
//			} else {
//				pt.put(1);
//			}
//			p.send(pt);
//		}
	}
	
	/** 快速注册微博 */
	protected void weiboQuickRegistrate(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getWeiboExecutorService().schedule(new WeiboQuickRegistrateCall(session, packet));
	}
	
	/** 绑定微博显示页面*/
	protected void bindedWeibo(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
			Packet pt = new Packet(OpCode.BINDED_WEIBO_SERVER); 
			pt.putInt(serial);
			pt.put(p.pool.getString(Player.PROPERTY_WEIBO_TOKEN).equals("")?0:1);
			p.send(pt);
		}
	}                                                                                           
	
	/** 发送微博*/
	protected void sendWeibo(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getWeiboExecutorService().schedule(new SendWeiboCall(session, packet));
	}
	
	/** 登录微博*/
	protected void loginWeibo(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getWeiboExecutorService().schedule(new WeiboLoginCall(session, packet));
	}
	
	/** 解绑微博*/
	protected void unBindWeibo(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
			try {
				WeiboService service = Server.server.getServiceRegistry().getWeiboService();
				service.unBindWeibo(p);
				Packet pt = new Packet(OpCode.UNBIND_WEIBO_SERVER);
				pt.putInt(serial);
				p.send(pt);
			} catch (WeiboException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.UNBIND_WEIBO_CLIENT,e.getMessage());
				return;
			}
		}
	}
	
	/** 绑定微博*/
	protected void bindWeibo(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getWeiboExecutorService().schedule(new BindWeiboCall(session, packet));
	}
	
	/** 打卡 */
	protected void cardPunch(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int type = 0;
		try{type = packet.getByte();}catch(Exception e){}
		Player player = (Player)session.getClient();
		if(type==0 && player!=null && player.cardPunch!=null){
			try {
				player.cardPunch.updatePunch();
				player.cardPunch.punchCard(serial);
			} catch (Exception e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_PUNCH_CLIENT, e.getMessage());
			}
		}else if(type==2 && player!=null){
			AnniversaryService service = Server.server.getServiceRegistry().getAnniversaryService();
			service.updateAnniversaryData(player);
			try {
				service.getGift(serial, player);
			} catch (Exception e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_PUNCH_CLIENT, e.getMessage());
			}
		}else if(type==3 && player!=null){
			CardPunchActService service = Server.server.getServiceRegistry().getCardPunchActService();
			try{service.updateAnniversaryData(player);}catch(Exception e){}
			try {
				service.getGift(serial, player);
			} catch (Exception e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_PUNCH_CLIENT, e.getMessage());
			}
		}else if(type==4 && player!=null){
			CardPunchActService service = Server.server.getServiceRegistry().getCardPunchActService();
			try{service.updateAnniversaryData(player);}catch(Exception e){}
			try {
				service.getGift(serial, player);
			} catch (Exception e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_PUNCH_CLIENT, e.getMessage());
			}
		}
	}
	
	/** 请求打卡信息 */
	protected void cardPunchInfo(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int type = 0;
		try{type = packet.getByte();}catch(Exception e){}
		Player player = (Player)session.getClient();
		if(type==0 && player!=null && player.cardPunch!=null){
			player.cardPunch.updatePunch();
			Packet pt = new Packet(OpCode.CARD_PUNCH_INFO_SERVER);
			pt.putInt(serial);
			pt.put(player.cardPunch.hasPunch() ? 1 : 0);
			pt.put(player.cardPunch.getPlayerCurrentStar());
			String[] des = CardPunch.getCardPunchDes();
			pt.put(des.length);
			for(int i=0;i<des.length;i++){
				pt.putString(des[i]);
			}
			session.send(pt);
		}else if(type==2 && player!=null){
			AnniversaryService service = Server.server.getServiceRegistry().getAnniversaryService();
			service.updateAnniversaryData(player);
			Packet pt = new Packet(OpCode.CARD_PUNCH_INFO_SERVER);
			pt.putInt(serial);
			pt.put(service.hasGift(player) ? 1 : 0);
			pt.put(service.getPlayerCurrentStar(player));
			String[] des = AnniversaryService.getGiftsDes();
			pt.put(des.length);
			for(int i=0;i<des.length;i++){
				pt.putString(des[i]);
			}
			session.send(pt);
		}else if(type==3 && player!=null){
			CardPunchActService service = Server.server.getServiceRegistry().getCardPunchActService();
			service.updateAnniversaryData(player);
			Packet pt = new Packet(OpCode.CARD_PUNCH_INFO_SERVER);
			pt.putInt(serial);
			pt.put(service.hasGift(player) ? 1 : 0);
			pt.put(service.getPlayerCurrentStar(player));
			String[] des = CardPunchActService.getGiftsDes();
			pt.put(des.length);
			for(int i=0;i<des.length;i++){
				pt.putString(des[i]);
			}
			session.send(pt);
		}else if(type==4 && player!=null){
			CardPunchActService service = Server.server.getServiceRegistry().getCardPunchActService();
			service.updateAnniversaryData(player);
			Packet pt = new Packet(OpCode.CARD_PUNCH_INFO_SERVER);
			pt.putInt(serial);
			pt.put(service.hasGift(player) ? 1 : 0);
			pt.put(service.getPlayerCurrentStar(player));
			String[] des = CardPunchActService.getGiftsDes();
			pt.put(des.length);
			for(int i=0;i<des.length;i++){
				pt.putString(des[i]);
			}
			session.send(pt);
		}
	}
	
	/**宝石定向礼包领取奖励**/
	protected void effectJewelGet(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int itemId = packet.getInt();
		int jewelsId = packet.getInt();
		int jewelsNum = packet.getInt();
		if(!JewelsBagItemEffect.isJewelsItem(itemId, jewelsId)){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.EFFECT_JEWEL_GET_CLIENT,peony.Messages.STRING_00058);
			return;
		}
		Player p = (Player)session.getClient();
		int num = p.bag.getGameItemCount(itemId);
		if(num == 0||jewelsNum > num){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.EFFECT_JEWEL_GET_CLIENT,peony.Messages.STRING_00059);
			return;
		}
		GameItem jewel = ObjectAccessor.createGameItem(jewelsId);
		if(jewel==null){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.EFFECT_JEWEL_GET_CLIENT,peony.Messages.STRING_00060);
			return;
		}
		PlayerTransaction tx1 = p.newTransaction("JEWELBAG");
		try{
			p.bag.removeGameItemIngoreInstanceId(itemId, jewelsNum, tx1, true);
			tx1.commit();
		}catch(Exception e){
			tx1.rollback();
			return;
		}
		PlayerTransaction tx = p.newTransaction("JEWELBAG");
		Gain gain = new Gain(p);
		gain.addGainItem(jewel, jewelsNum);
		try {
			p.addGainComplete(gain, tx, true);
			tx.commit();
		} catch (NoEnoughSpaceException e) {
			tx.rollback();
			MailService mailService = Server.server.getServiceRegistry().getMailService();
			mailService.sendSystemMail(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_00061, peony.Messages.STRING_00061, 
					0, jewel, jewelsNum, "JEWELBAG");
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id,peony.Messages.STRING_00062);
		}
		Packet pt = new Packet(OpCode.EFFECT_JEWEL_GET_SERVER);
		pt.putInt(serial);
		p.send(pt);
	}
	
	protected void cardAutoAddEnergy(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new CardAutoAddEnergyCall(session, packet));
	}
	
	protected void cmccAndroidSmsBuy(Packet packet, ClientSession session){
		int requestId = packet.getInt();
		String consumeCode = packet.getString();
		
		Player player = (Player)session.getClient();
		if(player!=null){
			Server.server.getServiceRegistry().getDbService().schedule(new CmccAndroidSmsBuyCall(session,player,requestId,consumeCode));
		}
	}
	
	protected void clientGuidResult(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null && player.guid!=null){
			int guidId = packet.getInt();
			int type = packet.getByte();
			if(type==1){
				if(player.guid.guidRecords.get(guidId)==null)
					player.guid.guidRecords.put(guidId, 1);
				else
					player.guid.guidRecords.put(guidId, player.guid.guidRecords.get(guidId)+1);
			}
		}
	}
	
	/**卡片类型分类列表**/
	protected void cardPropertyType(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int playerId=packet.getInt();
		Player p = ObjectAccessor.getPlayer(playerId);
		//其他玩家
		if(playerId>0){
			if(p == null){
				ErrorHandler.sendErrorMessage(session, serial, playerId,peony.Messages.STRING_01589);
				return;
			}
		}else if(playerId<0){
			//雕像
			if(p==null){
				p=Server.server.getServiceRegistry().getFameService().getStatue(playerId);
			}
			if(p == null){
				ErrorHandler.sendErrorMessage(session, serial, playerId,"暂无此信息");
				return;
			}
		}
		
		Map<Integer,Integer> allCardInfo=CardService.getAllCardsInfo(p);
		Player p0=(Player)session.getClient();
		CardService cs = Server.server.getServiceRegistry().getCardService();
		Packet pt = new Packet(OpCode.CARD_PRORERTY_ALLLIST_SERVER);
		pt.putInt(serial);
		pt.putInt(Card.PROPERTY_TYPE_NAMES.length-Card.UNUSE_PROPERTY_TYPE.length());
		for(int i=0;i<Card.PROPERTY_TYPE_NAMES.length;i++){
			if(Card.UNUSE_PROPERTY_TYPE.indexOf(i+"")!=-1){
				continue;
			}
//			pt.putInt(Card.buffTypeId[i]);
			pt.putInt(i);
			pt.putString(CardService.PROPERTY_TYPE_NAMES[i]);
			int size = 0;
			for (CardGroup group : cs.cardGroupList) {
				for (Card cd : group.cards) {
					boolean hasMatch = false;//cs.hasMatch(p, cd.id);
					if(allCardInfo.get(cd.id)!=null&&allCardInfo.get(cd.id)>0){
						hasMatch=true;
					}
					if(hasMatch && cd.prorertyType==i){
						size ++;
					}
				}
			}
			pt.putInt(size);
		}
		pt.putInt(p.cards.exp);
		p0.send(pt);
	}
	
	/** 使用物品获取坐骑经验 */
	protected void useToGetHorseExp(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int itemId = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
			ExpService expService = Server.server.getServiceRegistry().getExpService();
			expService.useToGetHorseExp(p, itemId);
			Packet pt = new Packet(OpCode.USEWHOLE_TOGET_HORSEEXP_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}
	}
	
	/** 使用物品获取坐骑经验确认*/
	protected void askToGetHorseExp(Packet packet,ClientSession session){
		ExpService expService = Server.server.getServiceRegistry().getExpService();
		expService.askToGetHorseExp(packet, session);
	}
	
	/**越南充值**/
	protected void vteCharge(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new VtcCardChargeCall(session, packet));
	}
	
	/** 卡片充能 */
	protected void cardAddEnergy(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new CardAddEnergyCall(session, packet));
	}
	
	/** 所有已收集卡片基本信息列表 */
	protected void allCardList(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			int prorertyType = packet.getInt();
			int targetPlayerId=packet.getInt();
			Player targetPlayer=ObjectAccessor.getPlayer(targetPlayerId);
			//其他玩家
			if(targetPlayerId>0){
				if(targetPlayer == null){
					ErrorHandler.sendErrorMessage(session, serial, targetPlayerId,peony.Messages.STRING_01589);
					return;
				}
			}else if(targetPlayerId<0){
				//雕像
				if(targetPlayer==null){
					targetPlayer=Server.server.getServiceRegistry().getFameService().getStatue(targetPlayerId);
				}
				if(targetPlayer == null){
					ErrorHandler.sendErrorMessage(session, serial, targetPlayerId,"暂无此信息");
					return;
				}
			}
			CardService cs = Server.server.getServiceRegistry().getCardService();
			Packet pt = new Packet(OpCode.CARD_ALLLIST_SERVER);
			int size = 0;
			for (CardGroup group : cs.cardGroupList) {
				for (Card cd : group.cards) {
//					boolean hasMatch = cs.hasMatch(targetPlayer, cd.id);
					//if(hasMatch && cd.prorertyType == prorertyType && cs.getEquipCardInfo(player, cd.id)==null){
//					if(!player.name.equals("fdasdfas"))
//						hasMatch=true;
					if(prorertyType==Card.PROPERTY_TYPE_SKILL){
						int count=0;
						for(int i=0;i<3;i++){
							if(CardService.clazzCardId[targetPlayer.clazz*3+i]==cd.id){
								count++;
							}
						}
						if(count==0){
							continue;
						}
					}
					if(/*hasMatch && */cd.prorertyType == prorertyType&&cd.itemId!=-1){
						size ++;
					}
				}
			}
			pt.putInt(serial);
			pt.putInt(targetPlayer.cards.exp);
			pt.putInt(size);
			int count=0;
			Map<Integer,Integer> allCardInfo=CardService.getAllCardsInfo(targetPlayer);
			for (CardGroup group : cs.cardGroupList) {
				for (Card cd : group.cards) {
					boolean hasMatch = false;//cs.hasMatch(targetPlayer, cd.id);
					if(allCardInfo.get(cd.id)!=null&&allCardInfo.get(cd.id)>0){
						hasMatch=true;
					}
//					if(!player.name.equals("fdasdfas"))
//						hasMatch=true;
					if(prorertyType==Card.PROPERTY_TYPE_SKILL){
						int count1=0;
						for(int i=0;i<3;i++){
							if(CardService.clazzCardId[targetPlayer.clazz*3+i]==cd.id){
								count1++;
							}
						}
						if(count1==0){
							continue;
						}
					}
					if(/*hasMatch && */cd.prorertyType == prorertyType&&cd.itemId!=-1){
						int type=cd.buff2Id==-1?0:1;
						pt.put(type);
						pt.putInt(cd.id);
						pt.putUTF(cd.title);
						pt.putUTF(cd.description);
						int q = 0;
						try {
							q = ObjectAccessor.createGameItem(cd.itemId).template.quality;
						} catch (Exception e1) {
							
						}
//						byte quality = (byte)(Card.QUALITY_COMMON);
						pt.put(q);
						pt.putUTF(cd.res);
						boolean isPictureCard = cs.isPictureCard(cd);
						pt.put(isPictureCard ? 1 : 0);
//						pt.putShort(cs.getCardEnergy(player, cd.id, false));
//						pt.putShort(cs.getCardEnergy(player, cd.id, true));
						pt.put(cd.star);
//						try{
//							int buffLevel = cd.buffLevel1;
//							if(isFlash){
//								buffLevel = cd.buffLevel2;
//							}
//							String buffDec = cs.getBuffDesc(cd.id, buffLevel);
//							pt.putUTF(buffDec);
//						}catch(Exception e){
//							pt.putUTF(peony.Messages.STRING_00064);
//						}
						CardInfo info = cs.getEquipCardInfo(targetPlayer, cd.id);
						boolean hasEquip = false;
						if(info!=null)
							hasEquip = true;
						if(info==null){
							info = targetPlayer.cards.getUnEquipCardInfo(cd.id);
							if(info==null){
								info = new CardInfo(cd.id);
								info.level=1;
							}
						}
						if(info!=null){
							pt.put(hasEquip ? 1 : 0);
							if(info.level==0){
								info.level=1;
							}
							pt.put(info.level);
							if(type==0){//普通卡片
								try {
									q = ObjectAccessor.createGameItem(cd.itemId).template.quality;
									pt.putInt(CardUpGradeCall.getUpGradeExp(q, info.level));
								} catch (Exception e) {
									pt.putInt(0);
								}
								pt.putUTF(cs.getEnhanceDesc(info.cardId, info.level));
								pt.putUTF(/*"下级属性:"+*/cs.getEnhanceDesc(info.cardId, info.level+1));
								if(info.level==1)
									pt.putInt(0);
								else
									pt.putInt(CardUpGradeCall.getTotalExp(info.level, q));
							}else{//技能卡片
								int needCount=CardUpGradeCall.getCardUpGradeNeedCount(info.level);
								pt.putInt(needCount);
								String desc=cs.getEnhanceDesc(info.cardId, info.level);
								Buff skillBuff=BuffUtil.createBuff(cd.buff2Id, info.level, targetPlayer, targetPlayer, 0);
								if(skillBuff!=null){
									desc=skillBuff.getDesc();
								}
								pt.putUTF(desc);
								Buff skillBuffNext=BuffUtil.createBuff(cd.buff2Id, info.level+1, targetPlayer, targetPlayer, 0);
								if(skillBuffNext!=null){
									desc=skillBuffNext.getDesc();
								}else if(info.level==12){
									desc="";
								}
								pt.putUTF(desc);
//								int totalCount=targetPlayer.pool.getInt(CardService.getPropertyOfPlayerCard(cd.id),0);
								int totalCount=CardService.getCardCount(targetPlayer, cd.id);
								if(totalCount>=1&&info.level>1){
									totalCount-=1;
									if(totalCount<=0){
										totalCount=0;
									}
								}
								pt.putInt(totalCount);
								int preLevel=1;
								if(info.level<3){
									preLevel=3;
								}else if(info.level<6){
									preLevel=6;
								}else if(info.level<9){
									preLevel=9;
								}else if(info.level<12){
									preLevel=12;
								}
								Buff skillBuffPre=BuffUtil.createBuff(cd.buff2Id, preLevel, targetPlayer, targetPlayer, 0);
								if(skillBuffPre!=null){
									desc=skillBuffPre.getDesc();
									pt.putUTF(desc);
								}else{
									pt.putUTF("");
								}
								int needExp=CardUpGradeCall.getSkillCardNeedExp(needCount);
								pt.putInt(needExp);
							}
						}else{
							//旧的卡片
							pt.put(0);
							pt.put(1);
							try {
								q = ObjectAccessor.createGameItem(cd.itemId).template.quality;
								pt.putInt(CardUpGradeCall.getUpGradeExp(q, 1));
							} catch (Exception e) {
								pt.putInt(0);
							}
							pt.putUTF(cs.getEnhanceDesc(cd.id, 1));
							pt.putUTF(/*"下级属性:"+*/cs.getEnhanceDesc(cd.id, 2));
//							System.out.println("cs.getEnhanceDesc(cd.id, 0)"+cs.getEnhanceDesc(cd.id, 0));
//							System.out.println("\"下级属性:\":"+"下级属性:"+cs.getEnhanceDesc(cd.id, 1));
//							System.out.println();
							pt.putInt(0);
						}
						pt.put(hasMatch?1:0);
					}
				}
			}
			player.send(pt);
		}
	}
	
	/** 激活卡槽 */
	protected void cardAddHole(Packet packet, ClientSession session){
		AddCardHoleCall call = new AddCardHoleCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	/** 玩家镶嵌卡片 */
	protected void cardAddToEqup(Packet packet, ClientSession session){
		AddCardCall call = new AddCardCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void refuseQuestion(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player p = (Player)session.getClient();
		if(p==null)
			return;
		try {
			Server.server.getServiceRegistry().getWeddingService().refuseQuestion(p.id);
		} catch (MarriageException e) {
			ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_QUESTION_REFUSE_CLIENT, e.getMessage());
			return;
		}
		
	}
	
	/**答题**/
	protected void weddingAnswerQuestion(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int questionId = packet.getInt();
		int answer = packet.get();
		Player p = (Player)session.getClient();
		try{
			Server.server.getServiceRegistry().getWeddingService().answerQuestion(p.id,questionId,answer);
		}catch(MarriageException e){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_QUESTION1_CLIENT, e.getMessage());
			return;
		}
	}
	
	/**申请开始答题回复**/
	protected void responBeginQuestion(Packet packet,ClientSession session){
		int serial = packet.getInt();
		byte response = packet.get();
		int applyId = packet.getInt();
		Player apply = ObjectAccessor.getPlayer(applyId);
		Player beApply = (Player)session.getClient();
		if(apply == null){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.MARRAY_BANLANG_REQUEST_CLIENT, peony.Messages.STRING_00065);
			return;
		}
		if(response == 1){
			Packet pg = new Packet(OpCode.WEDDING_QUESTION_RESPONSE_SERVER);
			pg.putInt(serial);
			pg.put(response);
			apply.send(pg);
		}
		if(response == 0){
			WeddingInstance instance = Server.server.getServiceRegistry().getWeddingService().getInstance(applyId);
			if((instance.answerNum.get(applyId) != null && instance.answerNum.get(applyId) >= WeddingService.ANSWER_MAX_NUM)||(instance.answerNum.get(beApply.id) != null && instance.answerNum.get(beApply.id) >= WeddingService.ANSWER_MAX_NUM)){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.MARRAY_BANLANG_REQUEST_CLIENT, peony.Messages.STRING_00066);
				ErrorHandler.sendErrorMessage(apply.session, serial, OpCode.MARRAY_BANLANG_REQUEST_CLIENT, peony.Messages.STRING_00066);
				return;
			}
			if(instance != null){
				instance.addAnswerQuestion(apply,beApply);
			}
		}
	}
	
	/**申请开始答题**/
	protected void applyBeginQuestion(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player apply = (Player)session.getClient();
		Player beApply = null;
		if(apply != null){
			WeddingInstance instance = Server.server.getServiceRegistry().getWeddingService().getInstance(apply.id);
			if(instance == null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_QUESTION_CLIENT, peony.Messages.STRING_00067);
				return;
			}
			if(instance.stat != WeddingInstance.DATI){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_QUESTION_CLIENT, peony.Messages.STRING_00068);
				return;
			}
			if(apply.party.getCount() != 2){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_QUESTION_CLIENT, peony.Messages.STRING_00069);
				return;
			}
			int beApplyId = 0;
			for(PartyMember member:apply.party.members){
				if(member.player.id != apply.id){
					beApplyId = member.player.id;
				}
			}
			beApply = ObjectAccessor.getPlayer(beApplyId);
			if(beApply == null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_QUESTION_CLIENT, peony.Messages.STRING_00070);
				return;
			}
			if(instance.isLevelWedding(apply, beApply)){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_QUESTION_CLIENT, peony.Messages.STRING_00071);
				return;
			}
			if(System.currentTimeMillis() - apply.pool.getLong(WeddingService.QUESTION_REFUSE_TIME, 0L) < WeddingService.QUESTION_FORBID_TIME
					||System.currentTimeMillis() - beApply.pool.getLong(WeddingService.QUESTION_REFUSE_TIME, 0L) < WeddingService.QUESTION_FORBID_TIME){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_QUESTION_CLIENT, peony.Messages.STRING_00072);
				return;
			}
			Packet pt = new Packet(OpCode.WEDDING_QUESTION_SERVER);
			pt.putInt(serial);
			pt.putInt(apply.id);
			pt.putString(apply.name);
			beApply.send(pt);
		}
	}
	
	/**申请伴郎返回处理**/
	protected void applyBanLangRequest(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int type = packet.get();
		byte response = packet.get();
		int playerId = packet.getInt();
		Player beApplay = (Player)session.getClient();
		Player applay = ObjectAccessor.getPlayer(playerId);
		if(applay==null || beApplay==null){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.MARRAY_BANLANG_REQUEST_CLIENT, peony.Messages.STRING_00073);
			return;
		}
		String msg = "";
		String typeN = "";
		if(type == 0){
			typeN = peony.Messages.STRING_00074;
		}else if(type == 1){
			typeN = peony.Messages.STRING_00075;
		}
		if(response == 1){
			applay.pool.setInt(MarriageService.PROPERTY_WEDDING_BAN,beApplay.id);
			if(type == 0){
				msg = peony.Messages.STRING_00076;
			}else if(type == 1){
				msg =  peony.Messages.STRING_00077;
			}
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(beApplay.id,MessageFormat.format(peony.Messages.STRING_00078, applay.name,typeN));
		}else if(response == 0){
			if(type == 0){
				msg = peony.Messages.STRING_00079;
			}else if(type == 1){
				msg =  peony.Messages.STRING_00080;
			}
		}
		Server.server.getServiceRegistry().getChatService().sendPrivateMessage(applay.id, msg);
		Packet pt = new Packet(OpCode.MARRAY_BANLANG_REQUEST_SERVER);
		pt.putInt(serial);
		pt.put(type);
		pt.putInt(beApplay.id);
		pt.putString(beApplay.name);
		pt.putInt(applay.id);
		pt.put(response);
		applay.send(pt);
	}
	
	/**申请伴郎**/
	protected void applyBanLang(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int type = packet.get();
		int playerId = packet.getInt();
		Player applay = (Player)session.getClient();
		Player beApplay = ObjectAccessor.getPlayer(playerId);
		if(applay==null || beApplay==null){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.MARRAY_BANLANG_CLIENT, peony.Messages.STRING_00073);
			return;
		}
		String msg;
		String sex;
		if(type == 0){
			sex = peony.Messages.STRING_00081;
			msg = peony.Messages.STRING_00074;
		}else{
			sex = peony.Messages.STRING_00082;
			msg = peony.Messages.STRING_00075;
		}
		MarriageService service = Server.server.getServiceRegistry().getMarriageService();
		if(service.isMarried(applay.id)==-1){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.MARRAY_BANLANG_CLIENT, MessageFormat.format(peony.Messages.STRING_00083,msg));
			return;
		}
		if(applay.pool.getInt(MarriageService.PROPERTY_WEDDING_BAN, 0) != 0){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.MARRAY_BANLANG_CLIENT, MessageFormat.format(peony.Messages.STRING_00084,msg));
			return;
		}
		if(applay.sex != beApplay.sex){//0:男   1:女
			ErrorHandler.sendErrorMessage(session, serial, OpCode.MARRAY_BANLANG_CLIENT, MessageFormat.format(peony.Messages.STRING_00085, sex,msg));
			return;
		}
		Packet pt = new Packet(OpCode.MARRAY_BANLANG_SERVER);
		pt.putInt(serial);
		pt.put(type);
		pt.putInt(beApplay.id);
		pt.putInt(applay.id);
		pt.putString(applay.name);
		beApplay.send(pt);
	}
	
	/** 解除师徒关系 */
	protected void removeApprenticeRelation(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new RemoveApprenticeCall(session,packet));
	}
	
	/** 师徒列表 */
	protected void playerApprenticeList(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new ApprenticeListCall(session,packet));
	}
	
	/** 接受师徒请求 */
	protected void apprenticeInvitOk(Packet packet,ClientSession session){
//		ApprenticeService apprenticeService = Server.server.getServiceRegistry().getApprenticeService();
//		apprenticeService.apprenticeInvitOk(packet, session);
		Server.server.getServiceRegistry().getDbService().schedule(new ApprenticeInvitOkCall(packet,session));
	}
	
	/** 拒绝师徒请求 */
	protected void apprenticeReject(Packet packet,ClientSession session){
		ApprenticeService apprenticeService = Server.server.getServiceRegistry().getApprenticeService();
		apprenticeService.apprenticeInvitReject(packet,session);
	}
	
	/** 师徒请求*/
	protected void apprenticeInvit(Packet packet,ClientSession session){
		ApprenticeService apprenticeService = Server.server.getServiceRegistry().getApprenticeService();
		apprenticeService.apprenticeInvite(session, packet);
	}
	
//	/** 建立师徒关系 */
//	protected void playerApprentice(Packet packet,ClientSession session){
//		ApprenticeService apprenticeService = Server.server.getServiceRegistry().getApprenticeService();
//		apprenticeService.createTeaAndApp(session, packet);
//	}

	/** 玩家锁定或解除锁定经验 **/
	protected void playerLockExp(Packet packet,ClientSession session){
		Player p = (Player)session.getClient();
		int serial = packet.getInt();
		int type = packet.getByte();
		if(p!=null){
			if(type == Player.EXP_LOCK){
				p.pool.setInt(Player.PROPERTY_LOCK_EXP, Player.EXP_LOCK);
				//如果玩家有师傅，锁定经验时通知师傅
				if(p.getTeacherId() !=-1){
				   Player teacher = ObjectAccessor.getPlayer(p.getTeacherId());
				   if(teacher != null){
					   ChatService chatService = Server.server.getServiceRegistry().getChatService();
					   chatService.sendPrivateMessage(teacher.id, MessageFormat.format(peony.Messages.STRING_00086,
								p.name));
				   } else {
					   Server.server.getServiceRegistry().getMailService().sendSystemMail(p.getTeacherId(), peony.Messages.STRING_00004,peony.Messages.STRING_00087,MessageFormat.format(peony.Messages.STRING_00086,
								p.name), 0,null, 0,"APPRENTICELOCKEXP");
				   }
				}
			} else {
				p.pool.setInt(Player.PROPERTY_LOCK_EXP, Player.EXP_UNLOCK);
			}
			Packet pt = new Packet(OpCode.PLAYER_LOCK_EXP_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}
	}
	
	/** 查看其他玩家的技能点 **/
	protected void otherPlayerSkillList(Packet packet,ClientSession session){
		Player p = (Player)session.getClient();
		int serial = packet.getInt();
		int targetId = packet.getInt();
		if(p!=null){
			Packet pt = new Packet(OpCode.OTHER_PLAYER_SKILL_LIST_SERVER);
			Player target = (Player)ObjectAccessor.getPlayer(targetId);
			if(target == null){
				DuelService duelService = Server.server.getServiceRegistry().getDuelService();
				if(duelService != null){
					target = duelService.getStatue(targetId);
				}
			}
			if(target != null){
				pt.putInt(serial);
				pt.put(target.skills.toClientBytes(target));
				p.send(pt);
			} else {
				Fame fame = FameService.fames.get(targetId);
				if(fame!=null){
					pt.putInt(serial);
					pt.put(fame.skills.toClientBytes(null));
					p.send(pt);
				} else {
					ErrorHandler.sendErrorMessage(session, serial, 
							OpCode.OTHER_PLAYER_SKILL_LIST_CLIENT, peony.Messages.STRING_00088);
					return;
				}
			} 
		} else {
			ErrorHandler.sendErrorMessage(session, serial, 
					OpCode.OTHER_PLAYER_SKILL_LIST_CLIENT, peony.Messages.STRING_00088);
		}
	}
	
	/**比武大会查看对手信息**/
	protected void duelMettingPalyerInfo(Packet packet, ClientSession session) throws DuelMettingException{
		Player player = (Player)session.getClient();
		int serial = packet.getInt();
		DuelMettingService service = Server.server.getServiceRegistry().getDuelMettingService();
		int playerId = 0;
		try{
			playerId = service.getGroupPlayerId(player);
		}catch(DuelMettingException e){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.DUELMETTING_PLAYERINFO_ClIENT, e.getMessage());
			return;
		}
		Player p = ObjectAccessor.getPlayer(playerId);
		if(p!=null){
			Packet pac = new Packet(OpCode.DUELMETTING_PLAYERINFO_SERVER);
			pac.putInt(serial);
			pac.putInt(playerId);
			session.send(pac);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.DUELMETTING_PLAYERINFO_ClIENT, peony.Messages.STRING_00089);
		}
		
	}
	
	/**比武大会报名**/
	protected void duelMettingSign(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		int serial = packet.getInt();
		if(p!=null){
			DuelMettingService service = Server.server.getServiceRegistry().getDuelMettingService();
			try {
				service.signUp(p);
				Packet pt = new Packet(OpCode.DUELMETTING_SIGNUP_SERVER);
				pt.putInt(serial);
				p.send(pt);
			} catch (DuelMettingException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.DUELMETTING_SIGNUP_CLIENT, e.getMessage());
			}
		}
	}
	
	/**
	 * 镶嵌宝石升级
	 */
	protected void upgradeJewel(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getSyncExecutorService().schedule(new UpgradeJewelCall(session, packet));
	}
	
	/** 福利列表**/
	protected void welfareList(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new WelfareListCall(session, packet));
	}
	
	/** 领取福利**/
	protected void welfareReward(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new WelfareRewardCall(session, packet));
	}
	
	/** 台湾全七BUFF描述 */
	protected void star_7_desc(Packet packet, ClientSession session){
		if(Server.server.revision.equals(Server.REVISION_TYPE_TW)){
			Player player = (Player)session.getClient();
			if(player!=null){
				int serial = packet.getInt();
				Buff buff = Player.STAR_7_BUFF;
				Packet pt = new Packet(OpCode.START_7_BUFF_DESC_SERVER);
				pt.putInt(serial);
				pt.putUTF(buff.getName());
				pt.putUTF(buff.getDesc());
				session.send(pt);
			}
		}
	}
	
	/** 解散军团 */
	protected void tongRemove(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new RemoveTongCall(session, packet));
	}
	
	/** 随从技能栏学习技能**/
	protected void attendantLearnSkill(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AttendantLearnSkillCall(session, packet));
	}
	
	/** 一键提取邮件附件 */
	protected void getAllMailAttach(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new GetAllMailAttachCall(session, packet));
	}
	
	/**付费邮件索回**/
	protected void mailRecover(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new MailRecoverCall(session,packet));
	}
	
	/** 越南TELCO充值 */
	protected void vietNamTelcoCharge(Packet packet, ClientSession session){
		TelcoChargeCall call = new TelcoChargeCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	/** 拍卖行撤单*/
	protected void deleteAuction(Packet packet,ClientSession session){
		Player player = (Player)session.getClient();
		if(player != null){
			Server.server.getServiceRegistry().getDbService().schedule(new AuctionDeleteCall(session, packet,player));
		}
	}

	/** 坐骑激活 */
	protected void horseActive(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new HorseActiveCall(session, packet));
	}
	
	protected void setPlayerFindingPath(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			byte b = packet.getByte();
			player.isFindPath = b;
			Server.server.getServiceRegistry().getSyncExecutorService().schedule(
					new SetFindPathCall(player.session, null, player));
			//player.broadcast(player.getMovePacket(GameObject.MOVE_DETAIL),player, null, false, false, true);
//			player.mapCell.broadcast(player, player.getMovePacket(GameObject.MOVE_DETAIL), false, false);
		}
	}
	
	/** 越南充值 */
	protected void vietNamCharge(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new GateCardChargeCall(session, packet));
	}
	
	/** 记录玩家动作日志 */
	protected void logPlayerAction(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int type = packet.getInt();
			int subType = 0;
			try {
				subType = packet.getByte();
			} catch (Exception e) {
				
			}
			LogPlayeActionService service = Server.server.getServiceRegistry().getLogPlayerActionService();
			service.logAction(player, type, subType);
		}
	}
	
	protected void useFuXingBag(Packet packet, ClientSession session){
		UseFuXingBagCall call = new UseFuXingBagCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);		
//		int serial = packet.getInt();
//		int useIB = packet.get();
//		int itemId = packet.getInt();
//		Player player = (Player)session.getClient();
//		GameItem item = ObjectAccessor.createGameItem(itemId);
//		if(item != null){
//			PlayerTransaction tx = player.newTransaction("FUXINLIBAO");
//			try{
//				new GetClickExpEffect().useItem(player, item, player, useIB, tx ,session);
//			}catch(Exception e){
//				return;
//			}
//		}else{
//			return;
//		}
//		Packet pt = new Packet(OpCode.FUXING_BAG_SERVER);
//		pt.putInt(serial);
//		session.send(pt);
		
	}
	protected void horseUpSkillLevel(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			int horseInstanceId = packet.getInt();
			int skillId = packet.getInt();
			Horse horse = player.horseBag.getHorse(horseInstanceId);
			if(horse==null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_UP_SKILL_CLIENT, peony.Messages.STRING_00090);
				return;
			}
			int oldSkillIndex = -1;
			Skill oldSkill = null;
			try {
				oldSkillIndex = horse.skills.indexOf(ObjectAccessor.getSkill(skillId));
				oldSkill = horse.getSkill(skillId);
			} catch (Exception e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_UP_SKILL_CLIENT, peony.Messages.STRING_00091);
				return;
			}
			if(oldSkillIndex>-1 && oldSkill!=null){
				int oldSkillLevel = oldSkill.getLevel();
				int oldSkillGroupId = oldSkill.getGroupId();
				if(oldSkillLevel<2){
					int newSkillId = Skills.getSkillId(oldSkillGroupId, oldSkillLevel+1);
					Skill newSkill = ObjectAccessor.getSkill(newSkillId);
					if(newSkill==null){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_UP_SKILL_CLIENT, peony.Messages.STRING_00092);
						return;
					}
					PlayerTransaction tx = player.newTransaction("HORSEUPLEVEL");
					if(player.bag.removeGameItemIngoreInstanceId(ItemUtil.HORSE_UP_SKILLLEVEL_ITEM, 1, tx, false)!=null){
						horse.skills.set(oldSkillIndex, newSkill);
						tx.commit();
						Packet pt0 = new Packet(OpCode.HORSE_UP_SKILL_SERVER);
						pt0.putInt(serial);
						session.send(pt0);
						Packet pt = new Packet(OpCode.HORSE_SKILLS_SERVER);
						pt.putInt(horse.skills.size());
						for(Skill skill : horse.skills){
							pt.put(skill.toClientBytes(player));
						}
						session.send(pt);
						//统计坐骑升级成就
						StatService statService = Server.server.getServiceRegistry().getStatService();
						statService.playerHorseSkillLevel(player);
					}else{
						tx.rollback();
						String itemName = ObjectAccessor.getItemTemplate(ItemUtil.HORSE_UP_SKILLLEVEL_ITEM).name;
						ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_UP_SKILL_CLIENT, MessageFormat.format(peony.Messages.STRING_00093, itemName));
					}
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_UP_SKILL_CLIENT, peony.Messages.STRING_00092);
					return;
				}
			}
		}
	}
	
	protected void autoMergeJewel(Packet packet, ClientSession session){
		if(SyncExecutorService.async==1){
			Server.server.getServiceRegistry().getSyncExecutorService().schedule(new AutoMergeJewelCall1(session, packet));
		}else if(SyncExecutorService.async==0){
			Server.server.getServiceRegistry().getDbService().schedule(new AutoMergeJewelCall(session, packet));
		}
	}
	
	protected void removeAllJewel(Packet packet,ClientSession session){
		if(SyncExecutorService.async==1){
			Server.server.getServiceRegistry().getSyncExecutorService().schedule(new RemoveAllJewelCall1(session ,packet));
		}else if(SyncExecutorService.async==0){
			Server.server.getServiceRegistry().getDbService().schedule(new RemoveAllJewelCall(session ,packet));
		}
	}
	/**
	 * 一键摘除所需资源
	 * @param packet
	 * @param session
	 */
	protected void removeAllJewelRequest(Packet packet,ClientSession session) throws Exception{
		int serial = packet.getInt();
		int equItemID = packet.getInt();
		int equInstanceID = packet.getInt();
		Player player = (Player)session.getClient();
		if(player != null){
			//找到装备
			Object[] obj = ItemUtil.findPlayerEquipment(player, equItemID,
					equInstanceID);
			if (obj == null) {
				ErrorHandler.sendErrorMessage(session,  serial,OpCode.REMOVE_ALLJEWEL_RESOURCE_CLIENT, peony.Messages.STRING_00015);
				return;
			}
			GameItem gi = (GameItem) obj[0];
			if (gi.template.equipment == null) {
				ErrorHandler.sendErrorMessage(session,  serial,OpCode.REMOVE_ALLJEWEL_RESOURCE_CLIENT, peony.Messages.STRING_00094);
				return;
			}
			if (gi.object == null) {
				gi.object = new ItemEnhance();
			}
			if (!(gi.object instanceof ItemEnhance)) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.REMOVE_ALLJEWEL_RESOURCE_CLIENT,  peony.Messages.STRING_00017);
				return;
			}
			ItemEnhance itemEn = (ItemEnhance)gi.object;
			if(itemEn.getJewelCount() == 0){
				ErrorHandler.sendErrorMessage(session, serial ,OpCode.REMOVE_ALLJEWEL_RESOURCE_CLIENT, peony.Messages.STRING_00095);
				return;
			}
			Packet pt = null;
			if(SyncExecutorService.async==1)
				pt = RemoveAllJewelCall1.getRequest(itemEn, session ,serial);
			else if(SyncExecutorService.async==0)
				pt = RemoveAllJewelCall.getRequest(itemEn, session ,serial);
			session.send(pt);
		}
	}
	
	protected void mergeJewelRequest(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int jewelId = packet.getInt();
		Player player = (Player)session.getClient();
		if(player!=null){
			int jewelLevel;
			try {
				jewelLevel = ObjectAccessor.getItemTemplate(jewelId).useLevel;
			} catch (Exception e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.MERGE_JEWEL_REQUEST_CLIENT, peony.Messages.STRING_00060);
				return;
			}
			if(jewelLevel<=2){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.MERGE_JEWEL_REQUEST_CLIENT, peony.Messages.STRING_00096);
				return;
			}
			if(SyncExecutorService.async==1){
				List<Map<Integer, String>> list = AutoMergeJewelCall1.getRequest(player, jewelId);
				Packet pt = new Packet(OpCode.MERGE_JEWEL_REQUEST_SERVER);
				pt.putInt(serial);
				pt.putInt(player.bag.getGameItemCount(jewelId));
				if(jewelLevel>3){
					pt.putInt(player.bag.getGameItemCount(AutoMergeJewelCall1.lowItemId));
					pt.putInt(player.bag.getGameItemCount(AutoMergeJewelCall1.highItemId));
				}else{
					pt.putInt(player.bag.getGameItemCount(AutoMergeJewelCall1.lowItemId));
					pt.putInt(0);
				}
				if(list!=null && list.size()>0){
					pt.put(list.size());
					for(Map<Integer, String> m : list){
						int type = m.keySet().iterator().next();
						pt.put(type);
						pt.putUTF(m.get(type));
					}
				}else{
					pt.put(0);
				}
				session.send(pt);
			}else if(SyncExecutorService.async==0){
				List<Map<Integer, String>> list = AutoMergeJewelCall.getRequest(player, jewelId);
				Packet pt = new Packet(OpCode.MERGE_JEWEL_REQUEST_SERVER);
				pt.putInt(serial);
				pt.putInt(player.bag.getGameItemCount(jewelId));
				if(jewelLevel>3){
					pt.putInt(player.bag.getGameItemCount(AutoMergeJewelCall.lowItemId));
					pt.putInt(player.bag.getGameItemCount(AutoMergeJewelCall.highItemId));
				}else{
					pt.putInt(player.bag.getGameItemCount(AutoMergeJewelCall.lowItemId));
					pt.putInt(0);
				}
				if(list!=null && list.size()>0){
					pt.put(list.size());
					for(Map<Integer, String> m : list){
						int type = m.keySet().iterator().next();
						pt.put(type);
						pt.putUTF(m.get(type));
					}
				}else{
					pt.put(0);
				}
				session.send(pt);
			}
		}
	}
	
	protected void attendantAddSkill(Packet packet, ClientSession session){
//		Player player = (Player)session.getClient();
//		if(player!=null){
//			int serial = packet.getInt();
//			int skillGroupId = packet.getInt();
//			int skillLevel = packet.getInt();
//			int instanceId = packet.getInt();
//			int index = packet.getByte();
//			int itemId = packet.getInt();
//			int type = 0; //学习随从技能方式（0为普通学习，1为一键学习）
//			try {
//				type = packet.getByte();
//			} catch (Exception e) {}
//			Attendant attendant = player.attendantBag.getAttendant(instanceId);
//			if(attendant!=null){
//				if(type==0 && attendant.skillSwitchs[index]==false){
//					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_ADDSKILL_CLIENT, "此技能位尚未激活，您拥有技能位点化符后，便可以激活此技能位");
//					return;
//				}else if(type==1 && attendant.skillSwitchs[index]==false){
//					//
//				}
//				Skill skill = ObjectAccessor.getSkill(Skills.getSkillId(skillGroupId, skillLevel));
//				if(attendant.hasSkill(skill.getId())){
//					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_ADDSKILL_CLIENT, "您已经学习过此技能，无需重复学习");
//					return;
//				}
//				attendant.addSkill(skill, index);
//				PlayerTransaction tx = player.newTransaction("ATTSKILL");
//				GameItem item = player.bag.removeGameItemIngoreInstanceId(itemId, 1, tx, false);
//				if(item!=null)
//					tx.commit();
//				else
//					tx.rollback();
//				Packet pt = new Packet(OpCode.ATTENDANT_ADDSKILL_SERVER);
//				pt.putInt(serial);
//				session.send(pt);
//			}else{
//				ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_ADDSKILL_CLIENT, "没有发现你所要寻找的指定随从");
//			}
//		}
		Server.server.getServiceRegistry().getSyncExecutorService().schedule(new AttendantAddSkillCall(session, packet));
	}
	
	protected void attendantAddLoyal(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			int instanceId = packet.getInt();
			Attendant attendant = player.attendantBag.getAttendant(instanceId);
			if(attendant!=null){
				PlayerTransaction tx = player.newTransaction("ATTADDLOYAL");
				GameItem item = player.bag.removeGameItemIngoreInstanceId(ItemUtil.ATTENDANT_ADDLOYAL_ITEM, 1, tx, false);
				ItemTemplate item0 = ObjectAccessor.getItemTemplate(ItemUtil.ATTENDANT_ADDLOYAL_ITEM);
				if(item0==null)
					item0 = ObjectAccessor.createGameItem(ItemUtil.ATTENDANT_ADDLOYAL_ITEM).template;
				if(item==null){
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_ADDLOYAL_SERVER, MessageFormat.format(peony.Messages.STRING_00097, item0.name));
					return;
				}
//				if(attendant.loyal>=Attendant.maxLoyal){
//					tx.rollback();
//					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_ADDLOYAL_SERVER, "您的随从对您忠心耿耿，忠诚度已满");
//					return;
//				}
				int oldValue = attendant.loyal;
				int value = oldValue + 100;
				if(oldValue>0 && value<=0){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_EQUIP_CLIENT, peony.Messages.STRING_00098);
					return;
				}
				attendant.setLoyal(value);
				tx.commit();
				Packet pt = new Packet(OpCode.ATTENDANT_ADDLOYAL_SERVER);
				pt.putInt(serial);
				session.send(pt);
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_EQUIP_CLIENT, peony.Messages.STRING_00099);
			}
		}
	}
	
	protected void attendantDelete(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			int instanceId = packet.getInt();
			if(player.attendant!=null && player.attendant.getInstanceId()==instanceId){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_DELETE_CLIENT, peony.Messages.STRING_00100);
				return;
			}
			Attendant attendant = player.attendantBag.removeAttendant(instanceId);
			LogUtil.logAttendant(attendant, "[THROWATTENDANT]");
			if(attendant!=null){
				Packet pt = new Packet(OpCode.ATTENDANT_DELETE_SERVER);
				pt.putInt(serial);
				session.send(pt);
				LogUtil.logRemoveAttendant(player, attendant.id, instanceId);
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_DELETE_CLIENT, peony.Messages.STRING_00099);
			}
		}
	}
	
	protected void attendantLightSkill(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			int instanceId = packet.getInt();
			int skillIndex = packet.getByte();
			Attendant attendant = player.attendantBag.getAttendant(instanceId);
			if(attendant!=null){
				if(!attendant.canLight(skillIndex)){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_LIGHTSKILL_CLIENT, peony.Messages.STRING_00101);
					return;
				}
				if(attendant.skillSwitchs[skillIndex]==true){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_LIGHTSKILL_CLIENT, "该技能位已经激活");
					return;
				}
				PlayerTransaction tx = player.newTransaction("LIGHTSKILL");
				GameItem item = player.bag.removeGameItemIngoreInstanceId(ItemUtil.ATTENDANT_LIGHTSKILL_ITEM, 1, tx, false);
				ItemTemplate item0 = ObjectAccessor.getItemTemplate(ItemUtil.ATTENDANT_LIGHTSKILL_ITEM);
				if(item0==null)
					item0 = ObjectAccessor.createGameItem(ItemUtil.ATTENDANT_LIGHTSKILL_ITEM).template;
				if(item!=null){
					tx.commit();
				}else{
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_LIGHTSKILL_CLIENT, MessageFormat.format(peony.Messages.STRING_00097, item0.name));
					return;
				}
				attendant.lightSkill(skillIndex);
				Packet pt = new Packet(OpCode.ATTENDANT_LIGHTSKILL_SERVER);
				pt.putInt(serial);
				session.send(pt);
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_LIGHTSKILL_CLIENT, peony.Messages.STRING_00099);
			}
		}
	}
	
	protected void attendantReName(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			int instanceId = packet.getInt();
			String name = packet.getString();
			Attendant attendant = player.attendantBag.getAttendant(instanceId);
			if(attendant!=null){
				if(name.length()>Attendant.NAME_MAX_LENGTH){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_RENAME_CLIENT, peony.Messages.STRING_00102);
					return;
				}
				if(StringUtil.isValidText(name)!=IStringValidator.OK || StringUtil.hasBadWord(name)){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_RENAME_CLIENT, peony.Messages.STRING_00103);
					return;
				}
				PlayerTransaction tx = player.newTransaction("ATTRENAME");
				GameItem item = player.bag.removeGameItemIngoreInstanceId(ItemUtil.ATTENDANT_RENAME_ITEM, 1, tx, false);
				ItemTemplate item0 = ObjectAccessor.getItemTemplate(ItemUtil.ATTENDANT_RENAME_ITEM);
				if(item0==null)
					item0 = ObjectAccessor.createGameItem(ItemUtil.ATTENDANT_RENAME_ITEM).template;
				if(item==null){
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_RENAME_CLIENT, MessageFormat.format(peony.Messages.STRING_00104, item0.name));
					return;
				}
				tx.commit();
				attendant.name = name;
				Packet pt = new Packet(OpCode.ATTENDANT_RENAME_SERVER);
				pt.putInt(serial);
				session.send(pt);
				player.changed.addChangedItem(new AttendantStringPropertyChangedItem(attendant, name, instanceId, false));
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_RENAME_CLIENT, peony.Messages.STRING_00105);
			}
		}
	}
	
	protected void attendantUnequip(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			int itemId = packet.getInt();
			int itemInstanceId = packet.getInt();
			int attendantInstanceId = packet.getInt();
			Attendant attendant = player.attendantBag.getAttendant(attendantInstanceId);
			if(attendant!=null){
				try {
					attendant.unEquip(itemId, itemInstanceId);
					attendant.refreshProperties(false);
					Packet pt = new Packet(OpCode.ATTENDANT_UNEQUIP_SERVER);
					pt.putInt(serial);
					session.send(pt);
				} catch (AttendantException e) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_UNEQUIP_CLIENT, e.getMessage());
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_UNEQUIP_CLIENT, peony.Messages.STRING_00099);
			}
		}
	}
	
	protected void attendantEquip(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			int itemId = packet.getInt();
			int itemInstanceId = packet.getInt();
			int attendantInstanceId = packet.getInt();
			Attendant attendant = player.attendantBag.getAttendant(attendantInstanceId);
			if(attendant!=null){
				try {
					attendant.equip(itemId, itemInstanceId);
					attendant.refreshProperties(false);
					Packet pt = new Packet(OpCode.ATTENDANT_EQUIP_SERVER);
					pt.putInt(serial);
					session.send(pt);
				} catch (AttendantException e) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_EQUIP_CLIENT, e.getMessage());
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_EQUIP_CLIENT, peony.Messages.STRING_00099);
			}
		}
	}
	
	protected void attendantBagInfo(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			Packet pt = new Packet(OpCode.ATTENDANT_BAG_SERVER);
			pt.putInt(serial);
			pt.put(player.attendantBag.toClientBytes(player));
			session.send(pt);
		}
	}
	
	protected void cancelAttendantFollow(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			int instanceId = packet.getInt();
			Attendant attendant = player.attendantBag.getAttendant(instanceId);
			if(attendant!=null){
				attendant = player.attendant;
				if(attendant==null){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_CANCELFOLLOW_CLIENT, peony.Messages.STRING_00106);
					return;
				}
				attendant.cancelFollow();
				player.pool.setInt(Player.PROPERTY_LAST_ATTENDANT_INSTANCEID,0);
				//处理随从特殊技能
				AttendantFixService attFixService = Server.server.getServiceRegistry().getAttendantFixService();
				attFixService.removeBuffUnFollow(player);
				Packet pt = new Packet(OpCode.ATTENDANT_CANCELFOLLOW_SERVER);
				pt.putInt(serial);
				session.send(pt);
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_CANCELFOLLOW_CLIENT, peony.Messages.STRING_00099);
			}
		}
	}
	
	protected void attendantFollow(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			int instanceId = packet.getInt();
			int change = packet.getInt();
			Attendant oldAttendant = player.attendant;
			if(player.pkInfo!=null && player.pkInfo.state!=PkInfo.STATE_END){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_FOLLOW_CLIENT, "公平起见,不允许携带随从进行切磋");
				return;
			}
			Attendant attendant = player.attendantBag.getAttendant(instanceId);
			if(attendant!=null){
				if(player.lastAttendantFollowTime==0 || (System.currentTimeMillis()-player.lastAttendantFollowTime>Player.attendantFollowCD)){
					if(player.attendant!=null)
						player.attendant.cancelFollow();
					attendant.follow();
					try{
						player.attendantExchangeEquip(oldAttendant, attendant, change);
					} catch (Exception e) {
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.ATTENDANT_FOLLOW_CLIENT, e.getMessage());
					}
					player.lastAttendantFollowTime = System.currentTimeMillis();
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_FOLLOW_CLIENT, "随从未准备好");
					return;
				}
				//特殊处理随从的袁绍副本相关的buff
				FiveElementService service = Server.server.getServiceRegistry().getFiveElementService();
				service.removeAttendantBuff(attendant);
				//处理随从特殊技能
				AttendantFixService attFixService = Server.server.getServiceRegistry().getAttendantFixService();
				attFixService.addBuffOnFollow(player, attendant);
				Packet pt = new Packet(OpCode.ATTENDANT_FOLLOW_SERVER);
				pt.putInt(serial);
				session.send(pt);
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_FOLLOW_CLIENT, peony.Messages.STRING_00099);
			}
		}
	}
	
	/** 整理账号仓库 */
	public void arrangeAccountDepot(Packet packet,ClientSession session){
		AccountDepotService accDepotService = Server.server.getServiceRegistry().getAccountDepotService();
		accDepotService.accountDepotArrange(packet, session);
	}
	
	/** 从背包到账号仓库 */
	public void getFromBagToAccDepot(Packet packet,ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int gridId = packet.getInt();
			int itemId = packet.getInt();
			int instanceId = packet.getInt();
			int count = packet.getInt();
			AccountDepotService service = Server.server.getServiceRegistry().getAccountDepotService();
		    try {
				service.getItemFromBagToDepot(p, session, serial, gridId, itemId, instanceId, count);
			} catch (Exception e) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.ACCOUNTDEPOT_GETFROMBAG_CLIENT, e.getMessage());
			}  
		}
	}
	
	/** 从账号仓库到账号背包 */
	public void getFromDepotToBag(Packet packet,ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int gridId = packet.getInt();
			int itemId = packet.getInt();
			int instanceId = packet.getInt();
			int count = packet.getInt();
			AccountDepotService service = Server.server.getServiceRegistry().getAccountDepotService();
		    try {
				service.getItemFromDepotToBag(p, session, serial, gridId, itemId, instanceId, count);
			} catch (Exception e) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.ACCOUNTDEPOT_GETFROMDEPOT_CIENT, e.getMessage());
			}
		}
	}
	
	/** 判断是否开启账号仓库 */
	public void accountDepotCheck(Packet packet,ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			short id = packet.getShort();
			AccountDepotService service = Server.server.getServiceRegistry().getAccountDepotService();
		    service.hasAccountDepot(p, serial,id);
		}
	}
	
	/** 开启账号仓库 */
	public void accountDepotRequest(Packet packet,ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			short id = packet.getShort();
			AccountDepotService service = Server.server.getServiceRegistry().getAccountDepotService();
			try {
				service.turnOn(p, serial,id);
			} catch (Exception e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNTDEPOT_REQUEST_CLIENT, e.getMessage());
			}
		}
	}
	
	/** 完成成就领取奖励 */
	public void getAchievementGift(Packet packet,ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int type = packet.getInt();
			int achieveId = packet.getInt();
			StatService statService = Server.server.getServiceRegistry().getStatService();
			try{
				statService.getReward(p, type, achieveId);
			} catch (Exception e){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ACHIEVEMENT_GIFT_CLIENT, e.getMessage());
				return;
			}
			Packet pt = new Packet(OpCode.ACHIEVEMENT_GIFT_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}
	}
	
	/** 领取奖励 */
	protected void fetchGift(Packet packet,ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int type = packet.getInt();
			FetchGiftService fgService = Server.server.getServiceRegistry().getFetchGiftService();
			try {
				fgService.checkRuleAndSendGift(p, type);
			} catch (Exception e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.FETCH_GIFT_CLIENT, e.getMessage());
			}
			Packet pt = new Packet(OpCode.FETCH_GIFT_SERVER);
			pt.put(serial);
			p.send(pt);
		}
	}
	
	protected void clearanceList(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			ClearanceSaleService service = Server.server.getServiceRegistry().getClearanceSaleService();
			service.toPlayer(player, serial);
		}
	}
	
	protected void clearanceSign(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			ClearanceSaleService service = Server.server.getServiceRegistry().getClearanceSaleService();
			try {
				service.signUp(player);
				Packet pt = new Packet(OpCode.CLEARANCESALE_SIGN_SERVER);
				pt.putInt(serial);
				session.send(pt);
			} catch (Exception e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.CLEARANCESALE_SIGN_CLIENT, e.getMessage());
			}
		}
	}
	
	/** 充值额度查询 */
	protected void chargeInfo(Packet packet,ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			ChargeInfoService service = Server.server.getServiceRegistry().getChargeInfoService();
		    String str = service.getInfo();
			Packet pt = new Packet(OpCode.CHARGE_INFO_SERVER);
			pt.putInt(serial);
			pt.putString(str);
		    p.send(pt);
		}
	}
	
	protected void antiPlug(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			player.checkPlug(packet);
		}
	}
	
	/** 星辉效果BUFF查看 */
	protected void starBuffDesc(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int type = 0;
			try {
				type = packet.getByte();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Packet pt = new Packet(OpCode.BUFF_DESC_BYID_SERVER);
			pt.putInt(serial);
			if(type==0){
				pt.put(Player.STAR_BUFF.length-1);
				for(int i=1;i<Player.STAR_BUFF.length;i++){
					Buff buff = Player.STAR_BUFFS[i];
					if(buff==null){
						buff = BuffUtil.createSuiteBuff(Player.STAR_BUFF[i], 1);
						Player.STAR_BUFFS[i] = buff;
					}
					if(buff!=null){
						pt.putString(buff.getName());
						pt.putString(buff.getDesc());
					}else{
						pt.putString("");
						pt.putString("");
					}
				}
			}else{
				pt.put(Player.HORSE_STAR_BUFF.length-1);
				for(int i=1;i<Player.HORSE_STAR_BUFF.length;i++){
					Buff buff = Player.HORSE_STAR_BUFFS[i];
					if(buff==null){
						buff = BuffUtil.createSuiteBuff(Player.HORSE_STAR_BUFF[i], 1);
						Player.HORSE_STAR_BUFFS[i] = buff;
					}
					if(buff!=null){
						pt.putString(buff.getName());
						pt.putString(buff.getDesc());
					}else{
						pt.putString("");
						pt.putString("");
					}
				}
			}
			p.send(pt);
		}
	}
	
	/** 血盟成员列表 */
	protected void associationList(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			AssociationService service = Server.server.getServiceRegistry().getAssociationService();
			Association association = service.getAssociationByPlayerId(player.id);
			if(association!=null){
				List<AssociationMember> list = association.memberList.members;
				Packet pt = new Packet(OpCode.ASSOCIATION_LIST_SERVER);
				pt.putInt(serial);
				pt.putString(association.name);
				pt.put(list.size());
				for(AssociationMember mem : list){
					pt.putInt(mem.playerId);
					if(mem.actor==null)
						mem.actor = Server.server.getServiceRegistry().getActorCacheService().find(mem.playerId);
					pt.putString(mem.actor==null ? "" : mem.actor.name);
					pt.put(mem.state);
					pt.put(mem.duty);
					if(mem.actor==null)
						pt.put(0);
					else{
						pt.put(mem.actor.online ? 1 : 0);
					}
				}
				session.send(pt);
			} else {
				Packet pt = new Packet(OpCode.ASSOCIATION_LIST_SERVER);
				pt.putInt(serial);
				pt.putString(peony.Messages.STRING_00107);
				pt.put(0);
				session.send(pt);
			}
		}
	}
	
	/** 回复结盟请求 */
	protected void answerAssociationInvite(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			int sourceId = packet.getInt();
			int associationId = packet.getInt();
			int answer = packet.getByte();
			AssociationService service = Server.server.getServiceRegistry().getAssociationService();
			try {
				service.answerInvite(player, sourceId, associationId, answer);
			} catch (AssociationException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ASSOCIATION_ANSWER_CLIENT, e.getMessage());
			}
		}
	}
	
	/** 血盟权利 */
	protected void associationExcise(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		int serial = packet.getInt();
		int type = packet.getByte();
		if(player!=null){
			AssociationService service = Server.server.getServiceRegistry().getAssociationService();
			Packet pt = new Packet(OpCode.ASSOCIATION_EXECISE_SERVER);
			pt.putInt(serial);
			if(type==0){
				//邀请入盟
				int targetId = packet.getInt();
				try {
					service.invite(player, targetId);
					session.send(pt);
				} catch (AssociationException e) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ASSOCIATION_EXECISE_CLIENT, e.getMessage());
				}
			}else if(type==1){
				//转让血盟
				int targetId = packet.getInt();
				try {
					service.transferAssociation(player, targetId);
					session.send(pt);
				} catch (AssociationException e) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ASSOCIATION_EXECISE_CLIENT, e.getMessage());
				}
			}else if(type==2){
				//剔除盟友
				int targetId = packet.getInt();
				try {
					service.removeFromAssociation(targetId);
					session.send(pt);
				} catch (AssociationException e) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ASSOCIATION_EXECISE_CLIENT, e.getMessage());
				}
			}else if(type==3){
				//删除血盟
				Association association = service.getAssociationByPlayerId(player.id);
				if(association!=null){
					try {
						service.destroyAssociation(association.id);
						session.send(pt);
					} catch (AssociationException e) {
						ErrorHandler.sendErrorMessage(session, serial, OpCode.ASSOCIATION_EXECISE_CLIENT, e.getMessage());
					}
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ASSOCIATION_EXECISE_CLIENT, peony.Messages.STRING_00108);
				}
			}else if(type==4){
				//主动退出
				PlayerTransaction tx = player.newTransaction("ASS");
				try {
					Association association = service.getAssociationByPlayerId(player.id);
					if(association!=null && association.getMember(player.id)!=null && association.getMember(player.id).duty==AssociationMember.DUTY_LEADER){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.ASSOCIATION_EXECISE_CLIENT, peony.Messages.STRING_00109);
						return;
					}
					service.removeFromAssociation(player.id);
					player.pool.setLong(Player.PROPERTY_LAST_REMOVEFROMASSOCIATION_TIME, System.currentTimeMillis());
					if(player.honor>=AssociationService.DEC_HONOR){
						player.decHonor(AssociationService.DEC_HONOR, tx, true);
						tx.commit();
					}else if(player.honor>0){
						player.decHonor(player.honor, tx, true);
						tx.commit();
					}else{
						tx.rollback();
					}
					Server.server.getServiceRegistry().getChatService().sendWorldMessage(
							MessageFormat.format(peony.Messages.STRING_00110, player.name));
					session.send(pt);
				} catch (AssociationException e) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ASSOCIATION_EXECISE_CLIENT, e.getMessage());
				} catch (NoEnoughValueException e) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ASSOCIATION_EXECISE_CLIENT, peony.Messages.STRING_00111);
				}
			}
		}
	}
	
	/** 创建结义血盟 */
	protected void associationCreate(Packet packet, ClientSession session){
		AssociationCreateCall call = new AssociationCreateCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	/** 血盟修改名称 */
	protected void associationReName(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if(player!=null){
			int serial = packet.getInt();
			String name = packet.getString();
			AssociationService service = Server.server.getServiceRegistry().getAssociationService();
			PlayerTransaction tx = player.newTransaction("ASSORENAME");
			try {
				GameItem item = player.bag.removeGameItemIngoreInstanceId(ItemUtil.ASSOCIATION_RENAME_ITEM, 1, tx, true);
				if(item!=null){
					service.renameAssociationName(player, name);
					tx.commit();
					Packet pt = new Packet(OpCode.ASSOCIATION_RENAME_SERVER);
					pt.putInt(serial);
					session.send(pt);
				}else{
					throw new AssociationException(peony.Messages.STRING_00112);
				}
			} catch (AssociationException e) {
				tx.rollback();
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ASSOCIATION_RENAME_CLIENT, e.getMessage());
			}
		}
	}
	
	/** 称号展示或隐藏 */
	public void titleShow(Packet packet,ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int titleId = packet.getShort();
			p.changeShowTitle(serial, titleId);
		}
	}
	
	/** 强化装备 */
	public void enhanceEquip(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new EquipEnhanceCall(session,packet));
	}
	
	/** 强化装备请求 */
	public void enhanceRequest(Packet packet,ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			EnhanceService enhanceService = Server.server.getServiceRegistry().getEnhanceService();
			int enhanceTimes = enhanceService.getEnhanceTimes(p);
			int maxTimes = 20;
			int decMoney = 200;
			TongService ts = Server.server.getServiceRegistry().getTongService();
			TongMember tm = ts.getPlayerInfo(p.id);
			if(tm!=null && tm.skills != null && tm.skills.get(4)!=null){
				TongSkill4 tskill = (TongSkill4)tm.skills.get(4);
				if(tskill != null){
					maxTimes = tskill.getValue();
					if(tskill.level == 1){
						decMoney = 100;
					}else if(tskill.level == 2){
						decMoney = 0;
					}
				}
			}
			Packet pt = new Packet(OpCode.ENHANCE_EQUIP_REQUEST_SERVER);
			pt.putInt(serial);
			pt.putInt(enhanceTimes+1);
			pt.putInt(maxTimes);
			pt.putInt(decMoney);
			p.send(pt);
		}
	}
	
	protected void getAppointItemDesc(Packet packet, ClientSession session){
		GetAppointItemDescCall call = new GetAppointItemDescCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void exchangeItemFromNpc(Packet packet, ClientSession session){
		ExchangeItemFromNpcCall call = new ExchangeItemFromNpcCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	/** 举报挂机 */
	protected void reportPlayer(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int targetId = packet.getInt();
			Player tarPlayer = ObjectAccessor.getPlayer(targetId);
			if(tarPlayer!=null){
				if(p.faction!=tarPlayer.faction){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.REPORT_CLIENT, peony.Messages.STRING_00113);
					return;
				}
				if(p.getVMap()!=null && tarPlayer.getVMap()!=null && p.getVMap().getId()==tarPlayer.getVMap().getId()){
					try {
						tarPlayer.report.report(p.id);
					} catch (Exception e) {
						ErrorHandler.sendErrorMessage(session, serial, OpCode.REPORT_CLIENT, peony.Messages.STRING_00114);
						return;
					}
					Packet pt = new Packet(OpCode.REPORT_SERVER);
					pt.putInt(serial);
					session.send(pt);
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.REPORT_CLIENT, peony.Messages.STRING_00115);
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.REPORT_CLIENT, peony.Messages.STRING_00116);
			}
		}
	}
	
	/** 批量出售白装 */
	protected void sellWhiteEquips(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
			Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(p.faction);
			for(TransactionBagGrid grid : p.bag.getGrids()){
				GameItem item = grid.getItem();
				if(item != null){
					if(item.template.isEquipment() && item.template.quality == Item.QUALITY_WHITE){
						if(item.template.canSale){
							ItemEnhance ie = (ItemEnhance)item.object;
							if(ie != null && (ie.getStar() > 0 || ie.getNaturals() != null || ie.getJewelCount() > 0)){
								continue;
							}
							LogUtil.logShopSellTry(p, item.template.id, item.instanceId, grid.count);
							PlayerTransaction tx = p.newTransaction("SELL");
							TransactionBagGrid gd = p.bag.removeGridGameItem(grid.id, item.template.id,
									item.instanceId, grid.count, tx, true);
							if(gd != null){
								int sellMoney = item.template.price * grid.count;
								int tax = (int)(sellMoney * nation.taxRate);
								if (tax > 0) {
									nation.addMoney(tax);
								}
								sellMoney -= tax;
								p.addMoney(sellMoney, tx, true);
								tx.commit();
								LogUtil.logShopSellOK(p, item, grid.count, sellMoney, tax);
							} else {
								tx.rollback();
								continue;
							}
						}
					}
				}
			}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
			Packet pt = new Packet(OpCode.DELETE_WHITE_EQUIPMENT_SERVER);
			pt.putInt(serial);
			pt.put(p.bag.getSize());
			for (TransactionBagGrid grid : p.bag.grids) {
				pt.put(grid.toClientByte());
			}
			p.send(pt);
		}
	}
	
	/** 活动指引查询列表 */
	protected void actLeadersQuery(Packet packet, ClientSession session){
		ActLeaderListCall call = new ActLeaderListCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void shopTopList(Packet packet, ClientSession session){
		ShopTopListCall call = new ShopTopListCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	/** 卡片收藏 */
	protected void cardCollect(Packet packet,ClientSession session){
		CardCollectionCall call = new CardCollectionCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	/**
	 * 卡片信息
	 * @param packet
	 * @param session
	 */
	protected void cardInfo(Packet packet,ClientSession session){
		CardInfoCall call = new CardInfoCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	/** 卡片重复收藏 */
	protected void cardCollectAgain(Packet packet,ClientSession session){
		CardRecollectionCall call = new CardRecollectionCall(session,packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void towerDefendSignUp(Packet packet, ClientSession session){
		int serial = packet.getInt();
		Player p = (Player)session.getClient();
		TowerDefendService service = Server.server.getServiceRegistry().getTowerDefendService();
		try {
			service.signUp(p);
			Packet pt = new Packet(OpCode.TOWERDEFEND_SIGNUP_SERVER);
			pt.putInt(serial);
			session.send(pt);
		} catch (TowerDefendException e) {
			ErrorHandler.sendErrorMessage(session, serial, OpCode.TOWERDEFEND_SIGNUP_CLIENT, e.getMessage());
		}
	}
	
	/** 卡片合成 */
	protected void mergeCard(Packet packet,ClientSession session){
		MergeCardCall call = new MergeCardCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	/** 使用集卡名录 */
	protected void showCardName(Packet packet,ClientSession session){
		CardService service = Server.server.getServiceRegistry().getCardService();
		service.showCardName(packet, session);
	}
	
	/** 卡片详细列表 */
	protected void cardDetailList(Packet packet,ClientSession session){
		CardListDetailCall call = new CardListDetailCall(session,packet);
	    Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	/** 卡片列表 */
	protected void cardList(Packet packet,ClientSession session){
	    CardListCall call = new CardListCall(session,packet);
	    Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	/** 集名录卡片列表 */
	protected void cardList4Sheet(Packet packet,ClientSession session){
	    CardList4SheetCall call = new CardList4SheetCall(session,packet);
	    Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	/** 充值查询 */
	protected void chargeRecord(Packet packet, ClientSession session){
		ChargeRecordCall call = new ChargeRecordCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	/** 驸马领取称号 */
	protected void duelGetTitle(Packet packet, ClientSession session){
		int serial = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
			DuelService service = Server.server.getServiceRegistry().getDuelService();
			int index = service.getLastWinnerIndex(p.id);
			if(index==-1){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.DUEL_GETTITLE_CLIENT, peony.Messages.STRING_00117);
				return;
			}else{
				int itemId = service.itemIds[index];
				if(p.pool.getInt("DUELTILE",0)==1){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.DUEL_GETTITLE_CLIENT, peony.Messages.STRING_00118);
					return;
				}
				PlayerTransaction tx = p.newTransaction("DUEL");
				try {
					p.bag.addGameItemComplete(ObjectAccessor.createGameItem(itemId), 1, tx, true);
					tx.commit();
					p.pool.setInt("DUELTILE", 1);
					Packet pt = new Packet(OpCode.DUEL_GETTITLE_SERVER);
					pt.putInt(serial);
					p.send(pt);
				} catch (NoEnoughSpaceException e) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial, OpCode.DUEL_GETTITLE_CLIENT, peony.Messages.STRING_00119);
				}
			}
		}
	}
	
	/** 比武招亲排行榜 */
	protected void duelScore(Packet packet, ClientSession session){
		int serial = packet.getInt();
		Player p = (Player) session.getClient();
		if(p!=null){
			DuelService duelService = Server.server.getServiceRegistry().getDuelService();
			Packet pt = new Packet(OpCode.DUEL_SCORE_SERVER);
			pt.putInt(serial);
			pt.putInt(4);
			for(int i=0;i<4;i++){
				int id = duelService.ids[i];
				if(id==0){
					pt.putString(MessageFormat.format(peony.Messages.STRING_00120, duelService.npcNames[i]));
				}else{
					Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(id);
					if(actor!=null){
						pt.putString(duelService.npcNames[i]+":"+actor.name);
					}else{
						pt.putString(MessageFormat.format(peony.Messages.STRING_00120, duelService.npcNames[i]));
					}
				}
			}
			p.send(pt);
		}
	}
	
	/** 玩家提交密信 */
	protected void handInLetter(Packet packet,ClientSession session){
		NationDayService service = Server.server.getServiceRegistry().getNationDayService();
		service.handIn(packet, session);
	}
	
	/** 比武招亲报名 */
	protected void duelSignUp(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		int serial = packet.getInt();
		if(p!=null){
			DuelService service = Server.server.getServiceRegistry().getDuelService();
			try {
				service.signUp(p);
				Packet pt = new Packet(OpCode.DUEL_SIGNUP_SERVER);
				pt.putInt(serial);
				pt.putInt(service.getLeavingMinute());
				p.send(pt);
			} catch (DuelException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.DUEL_SIGNUP_CLIENT, e.getMessage());
			}
		}
	}
	
	/** 快速注册选择性别职业国籍 */
	protected void changePlayerInfo(Packet packet,ClientSession session){
		int serial = packet.getInt();
		short info = packet.getShort();
		int sex = (info & 0x1f) - 1;
		int clazz = ((info >> 5) & 0x1f) - 1;
		int faction = ((info >> 10) & 0x1f) - 1;
		if(faction == 0){
			faction = PlayerUtil.getRandomFaction();
		}
		Player player = (Player) session.getClient();
		if (player != null && player.level < 6) {
			//设置性别
			if(sex != -1){
				player.setSex(sex, false);
			}
			//设置职业
			if(clazz != -1){
				int oldClazz = player.clazz;
				player.setClazz(clazz, false);
				Gain gain = new Gain(player);
//				GameItem equ = ObjectAccessor
//						.createGameItem(PlayerUtil.INIT_EQUIPMENT[clazz]);
//				gain.addGainItem(equ, 1);
				PlayerTransaction tx = player.newTransaction("CCL");
				boolean ok = player.bag.addGain(gain, tx, false);
				tx.commit();
				if (ok) {
//					player.equip(equ.template.id, equ.instanceId, -1);
					GameItem item = player.bag
							.getGameItem(PlayerUtil.INIT_EQUIPMENT[oldClazz]);
					if (item != null) {
						tx = player.newTransaction("CCL");
						player.bag.removeGameItem(item.template.id, item.instanceId, 1,
								tx, true);
						tx.commit();
					}
				}
				player.skills.clear();
				List<Skill> initSkills = ObjectAccessor.getPlayerInitSkills(clazz);
				for (Skill skill : initSkills) {
					player.skills.addSkill(skill, null, false);
				}
				player.sendSkillList();
				player.refreshPropertiesWhenClazzChanged(false);
			}
			//设置阵营
			if(faction != -1){
				int oldFaction = player.faction;
				if (oldFaction != faction) {
					player.setFaction(faction, false);
					Server.server.getEventManager().fireEvent(
							new ServiceEvent(ServiceEvent.EVENT_PLAYER_CHANGE_FACTION,
									player, oldFaction));
				}
				player.pool.setString("leavecontry", PlayerUtil.CREATE_POINT[player.faction - 1][0] + "," + PlayerUtil.CREATE_POINT[player.faction - 1][1] + "," + PlayerUtil.CREATE_POINT[player.faction - 1][2]);
			}
			Packet pt = new Packet(OpCode.CHANGE_PLAYER_INFO_SERVER);
			pt.putInt(serial);
			player.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.CHANGE_PLAYER_INFO_CLIENT, peony.Messages.STRING_00121);
		}
	}
	
	/** 名人堂增加信息 */
	protected void fameAddInfo(Packet packet,ClientSession session){
		FameService service = Server.server.getServiceRegistry().getFameService();
		service.saveFameInfo(packet, session);
	}
	
	/** 婚礼领取经验 */
	protected void weddingGetExp(Packet packet,ClientSession session){
		WeddingService service = Server.server.getServiceRegistry().getWeddingService();
		service.getWeddingExp(packet, session);
	}
	
	/** 婚礼踢宾客 */
	protected void weddingKickGuest(Packet packet,ClientSession session){
		WeddingService service = Server.server.getServiceRegistry().getWeddingService();
		service.kickGuest(packet, session);
	}
	
	/** 婚礼发红包 */
	protected void weddingSendGift(Packet packet,ClientSession session){
		int serial = packet.getInt();
		int itemId = packet.getInt();
		int targetId = packet.getInt();
		int count = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
			WeddingService service = Server.server.getServiceRegistry().getWeddingService();
			WeddingInstance instance =(WeddingInstance) p.map.map.instance;;
			if(p.id!=instance.man.id){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_GIFT_CLINT, peony.Messages.STRING_00122);
				return;
			}
			Player targetPlayer = ObjectAccessor.getPlayer(targetId);
			if(targetPlayer == null || targetPlayer.map.map.instance==null || 
					targetPlayer.map.map.instance.getId() != instance.getId()){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_GIFT_CLINT, peony.Messages.STRING_00123);
				return;
			}
			try {
				service.sendGift(p, targetId, itemId,count);
			} catch (MarriageException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_GIFT_CLINT, e.getMessage());
				return;
			}
			Packet pt = new Packet(OpCode.WEDDING_GIFT_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}
	}
	
	/** 婚礼签到列表 */
	protected void weddingSignList(Packet packet,ClientSession session){
		WeddingSignListCall call = new WeddingSignListCall(packet, session);
		Server.server.getServiceRegistry().getDbService().schedule(call);
//	    int serial = packet.getInt();
//	    Player p = (Player)session.getClient();
//	    if(p!=null){
//	    	WeddingService service = Server.server.getServiceRegistry().getWeddingService();
//	    	List<Actor> actors = service.getSignIns(p);
//	    	RelationService rs = Server.server.getServiceRegistry().getRelationService();
//	    	TongService tongService = Server.server.getServiceRegistry().getTongService();
//	    	WeddingInstance instance = (WeddingInstance)p.map.map.instance;
//	    	PlayerRelation relationMan = rs.get(instance.man.id);
//	    	PlayerRelation relationWoman = rs.get(instance.woman.id);
//	    	Tong tmMan = tongService.getPlayerTong(instance.man.id);
//	    	Tong tmWoman = tongService.getPlayerTong(instance.woman.id);
//	    	byte type;
//	    	Packet pt = new Packet(OpCode.WEDDING_SIGNINLIST_SERVER);
//	    	pt.putInt(serial);
//	    	pt.putInt(actors == null?0:actors.size());
//	    	if(actors != null){
//		    	for(Actor actor:actors){
//		    		byte isFetch = 0;
//		    		if(instance.getgift.contains(new Integer(actor.id))){
//		    			isFetch = 1;
//		    		}
//		    		Tong tm2 = tongService.getPlayerTong(actor.id);
//					type = 0;
//					
//		    		if((relationMan != null && relationMan.friends.exists(actor.id)) || (relationWoman != null && relationWoman.friends.exists(actor.id))){
//		    			type |= 1;
//		    		}
//		    		if(tm2!=null && ((tmMan!=null && tmMan.id == tm2.id) || (tmWoman!=null && tmWoman.id == tm2.id))){
//		    			type |= 2;
//		    		} 
//		    		pt.putInt(actor.id);
//		    		pt.putString(actor.name);
//		    		pt.put(type);
//		    		pt.put(isFetch);
//		    	}
//	    	}
//	    	p.send(pt);
//	    }
	}
	
	/** 婚礼签到 */
	protected void weddingSignIn(Packet packet, ClientSession session){
		WeddingService service = Server.server.getServiceRegistry().getWeddingService();
		try {
			service.signIn(packet, session);
		} catch (VMapException e) {
			e.printStackTrace();
		}
	}
	
	/** 参加婚礼 */
	protected void joinWedding(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int manId = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
			WeddingService service = Server.server.getServiceRegistry().getWeddingService();
			try {
				service.joinWedding(p, manId);
				Packet pt = new Packet(OpCode.WEDDING_JOINWEDDING_SERVER);
				pt.putInt(serial);
				p.send(pt);
			} catch (VMapException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_JOINWEDDING_CLIENT, e.getMessage());
			}
		}
	}
	
	/** 婚礼列表 */
	protected void weddingList(Packet packet, ClientSession session){
		int serial = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
			WeddingService service = Server.server.getServiceRegistry().getWeddingService();
			List<Actor[]> list = service.getWeddingList(p);
			Packet pt = new Packet(OpCode.WEDDING_LIST_SERVER);
			pt.putInt(serial);
			pt.putInt(list.size());
			for(Actor[] ref : list){
				pt.putInt(ref[0].id);
				pt.putString(ref[0].name);
				pt.putInt(ref[1].id);
				pt.putString(ref[1].name);
			}
			p.send(pt);
		}
	}
	
	/** 婚礼开始 
	 * @throws VMapException */
	protected void weddingOpen(Packet packet, ClientSession session) throws VMapException{
		int serial = packet.getInt();
		int level = packet.getInt();
		int guestLevel = packet.getInt();
		int jewelTypeNum = packet.getInt();
		List<Integer> jewels = new ArrayList<Integer>();
		for(int i = 0;i < jewelTypeNum;i++){
			int itemId = packet.getInt();
			int itemNum = packet.getInt();
			for(int j = 0;j < itemNum;j++){
				jewels.add(itemId);
			}
		}
		Player p = (Player)session.getClient();
		if(p!=null){
			int mateId = Server.server.getServiceRegistry().getRelationService().get(p.id).mateId;
//			if(mateId<=0){
//				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_OPEN_CLIENT, peony.Messages.STRING_00124);
//				return;
//			}
			Player mate = ObjectAccessor.getPlayer(mateId);
//			if(mate==null){
//				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_OPEN_CLIENT, peony.Messages.STRING_00125);
//				return;
//			}
////			if(p.sex!=0){
////				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_OPEN_CLIENT, peony.Messages.STRING_00126);
////				return;
////			}
//			if(jewels.size() > WeddingService.jewelNum[level]){
//				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_OPEN_CLIENT, peony.Messages.STRING_00127);
//				return;
//			}
//			if(p.sex==0){
//				if(p.pool.getLong(WeddingService.MANOPENWEDDINGTIME,0L)>System.currentTimeMillis()-WeddingService.ONEWEEK
//						|| mate.pool.getLong(WeddingService.WOMANOPENWEDDINGTIME,0L)>System.currentTimeMillis()-WeddingService.ONEWEEK){
//					ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_OPEN_CLIENT, peony.Messages.STRING_00128);
//					return;
//				}
//			}else{
//				if(p.pool.getLong(WeddingService.WOMANOPENWEDDINGTIME,0L)>System.currentTimeMillis()-WeddingService.ONEWEEK
//						|| mate.pool.getLong(WeddingService.WOMANOPENWEDDINGTIME,0L)>System.currentTimeMillis()-WeddingService.ONEWEEK){
//					ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_OPEN_CLIENT, peony.Messages.STRING_00128);
//					return;
//				}
//			}
//			if(p.money < level*1200000){
//				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_OPEN_CLIENT, peony.Messages.STRING_00129);
//				return;
//			}
//			int num = p.bag.getGameItemCount(1311);
//			if(num < 20){
//				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_OPEN_CLIENT, "你的珍珠数不足不能开启婚礼");
//				return;
//			}
//			if(p.map.id != mate.map.id){
//				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_OPEN_CLIENT, peony.Messages.STRING_00130);
//				return;
//			}
//			if(p.party == null || mate.party == null || !p.party.contains(mate.id)){
//				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_OPEN_CLIENT, peony.Messages.STRING_00131);
//				return;
//			}
			
			WeddingService service = Server.server.getServiceRegistry().getWeddingService();
		    try {
				service.createInstance(p,p.sex==0 ? p : mate, mate.sex==1 ? mate : p,level,guestLevel,jewels.size(),jewels);
			} catch (Exception e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_OPEN_CLIENT, e.getMessage());
			    return;
			}
			Packet pt = new Packet(OpCode.WEDDING_OPEN_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}
	}


	protected void beautyFriendList(Packet packet, ClientSession session){
		FindFriendListCall call = new FindFriendListCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void beautyList(Packet packet, ClientSession session){
		BeautyListCall call = new BeautyListCall(packet,session);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void beautySignUp(Packet packet, ClientSession session){
		BeautySignUpCall call = new BeautySignUpCall(packet, session);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void beautyVote(Packet packet, ClientSession session){
		BeautyVoteCall call = new BeautyVoteCall(packet, session);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	/*
	 * 军团重命名。
	 */
	protected void renameTong(Packet packet, ClientSession session) {
		RenameTongCall call = new RenameTongCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void getPosition(Packet packet,ClientSession session){
		RelationService service = Server.server.getServiceRegistry().getRelationService();
		try {
			service.searchPosition(packet, session);
		} catch (RelationServiceException e) {
			e.printStackTrace();
		} 
	}
	
	protected void saveBBS(Packet packet,ClientSession session){
		ClientBbsService service=Server.server.getServiceRegistry().getClientBbsService();
		service.saveClientBbs(packet, session);
	}
	protected void lookOverBBS(Packet packet,ClientSession session){
		ClientBbsService service=Server.server.getServiceRegistry().getClientBbsService();
		service.sendBbs(packet, session);
	}
	protected void achievementList(Packet packet,ClientSession session){
//		StatService service=Server.server.getServiceRegistry().getStatService();
//		service.achievementList(packet, session);
		AchievementListCall call = new AchievementListCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);	}
	protected void detailAchievementList(Packet packet,ClientSession session){
		StatService service=Server.server.getServiceRegistry().getStatService();
		service.detailAchieveList(packet, session);
	}
	
	protected void autoAddHole(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int gameItemId = packet.getInt();
			int gameItemInstanceId = packet.getInt();
			int wantHole = packet.getByte();
			JewelService js = Server.server.getServiceRegistry().getJewelService();
			Object[] os = ItemUtil.findPlayerEquipment(p, gameItemId, gameItemInstanceId);
			if(os==null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.AUTO_ADDHOLE_CLIENT, peony.Messages.STRING_00132);
				return;
			}else{
				GameItem item = (GameItem) os[0];
				if(!item.template.isEquipment()){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.AUTO_ADDHOLE_CLIENT, peony.Messages.STRING_00133);
					return;
				}
				if (item.object == null) {
					item.object = new ItemEnhance();
				}
				if (!(item.object instanceof ItemEnhance)) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.AUTO_ADDHOLE_CLIENT, peony.Messages.STRING_00017);
					return;
				}
				ItemEnhance itemEnhance = (ItemEnhance) item.object;
				int initHole = item.template.equipment.initHole;
				int addHole = itemEnhance.addHole;
				int currentHoles = addHole + initHole;
				int maxHoles = itemEnhance.addMaxHole + item.template.equipment.maxHole;
				if (currentHoles >= maxHoles) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.AUTO_ADDHOLE_CLIENT,peony.Messages.STRING_00019);
					return;
				}
				if(maxHoles-currentHoles < wantHole || wantHole>maxHoles){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.AUTO_ADDHOLE_CLIENT, peony.Messages.STRING_00134);
					return;
				}
				List<ItemTemplate> l = js.getAddHoleItem(item.template.useLevel);
				if(l.size()==0){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.AUTO_ADDHOLE_CLIENT,peony.Messages.STRING_00135);
					return;
				}
				AutoAddHole autoAddHole = new AutoAddHole(item, wantHole, wantHole+addHole, serial);
				p.autoAddHole = autoAddHole;
			}
		}
	}

	/*
	 * 查询级别排行榜。
	 */
	protected void topLevelRank(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int faction = packet.get();
			List<Actor> l = Server.server.getServiceRegistry().getStatService().topLevelRanks(faction);
			Packet pt = new Packet(OpCode.TOPLIST_LEVEL_SERVER);
			pt.putInt(serial);
			pt.putShort(l.size());
			for (Actor a : l) {
				pt.putString(a.name);
				pt.put(a.faction);
				pt.putShort(a.level);
			}
			session.send(pt);
		}
	}

	/*
	 * 读取用户配置。
	 */
	protected void getConfig(Packet packet, ClientSession session) {
		Player player = (Player)session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			String name = packet.getString();
			String value = player.pool.getString("__CONFIG_" + name);
			Packet pt = new Packet(OpCode.GET_CONFIG_SERVER);
			pt.putInt(serial);
			pt.putString(name);
			pt.putString(value);
			session.send(pt);
		}
	}
	
	/*
	 * 保存用户配置。
	 */
	protected void setConfig(Packet packet, ClientSession session) {
		Player player = (Player)session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			String name = packet.getString();
			String value = packet.getString();
			player.pool.setString("__CONFIG_" + name, value);
			Packet pt = new Packet(OpCode.SET_CONFIG_SERVER);
			pt.putInt(serial);
			pt.putString(name);
			session.send(pt);
		}
	}
	
	/*
	 * 新扩展仓库协议。
	 */
	protected void extendDepot(Packet packet, ClientSession session) {
		Player player = (Player)session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			Server.server.getServiceRegistry().getDepotService().extendDepot(player, session, serial);
		}
	}
	
	/*
	 * 新扩展仓库协议，取价格。
	 */
	protected void extendDepotPrice(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			Server.server.getServiceRegistry().getDepotService().getExtendDepotPrice(player, session, serial);
		}
	}
	
	/*
	 * 取得玩家扩展包格需要的价格。
	 */
	private int getExtendBagPrice(Player p) {
		int addSize = p.bag.getAddedSize();
		return (addSize + 1) * 100;
	}
	
	/*
	 * 新扩展包格协议，取价格。
	 */
	protected void extendBagPrice(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			Packet pt = new Packet(OpCode.EXTEND_BAG_PRICE_SERVER);
			pt.putInt(serial);
			pt.putInt(getExtendBagPrice(player));
			session.send(pt);
		}
	}
	
	/*
	 * 新扩展包格协议，直接i币收费。
	 */
	protected void extendBag(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			if (player.bag.isSizeLocked()) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.EXTEND_BAG_CLIENT, peony.Messages.STRING_00136);
				return;
			}
			
			int price = getExtendBagPrice(player);
			
			// 通过ShopService完成购买
			ShopService shopService = Server.server.getServiceRegistry().getShopService();
			try {
				player.bag.lockSize();
				shopService.buy(player, new ExtendBagBuy(player, serial, price));
			} catch (ShopException e) {
			}
		}
	}
	
	/*
	 * 拆分仓库物品。
	 */
	protected void depotSplitItem(Packet packet, ClientSession session) {
		DepotService depotService = Server.server.getServiceRegistry().getDepotService();
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int gridId = packet.get();
			int itemId = packet.getInt();
			int count = packet.get();
			try {
				depotService.splitItem(player, session, serial, gridId, itemId, count);
			} catch (DepotException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.DEPOT_SPLITITEM_CLIENT, e.getMessage());
			}
		}
	}
	
	/*
	 * 交换仓库包格。
	 */
	protected void depotExchange(Packet packet, ClientSession session) {
		DepotService depotService = Server.server.getServiceRegistry().getDepotService();
		Player p = (Player) session.getClient();
		if (p != null) {
			int serial = packet.getInt();
			short grid1 = packet.getShort();
			short grid2 = packet.getShort();
			try {
				depotService.exchangeGrid(p, session, serial, grid1, grid2);
			} catch (DepotException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.DEPOT_EXCHANGE_CLIENT, e.getMessage());
			}
		}
	}

	/*
	 * 全局取NPC描述，通过instanceID查找。
	 */
	protected void globalNpcDesc(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		if (p != null) {
			int serial = packet.getInt();
			int id = packet.getInt();
			int instanceID = packet.getInt();
			GameObject gobj = ObjectAccessor.getGameObject(instanceID);
			String desc = null;
			if (gobj == null || gobj.id != id || gobj.type != GameObject.TYPE_CREATURE) {
				desc = peony.Messages.STRING_00137;
			} else {
				Creature c = (Creature)gobj;
				desc = c.searchName == null ? "" : c.searchName;
			}
			Packet pt = new Packet(OpCode.GLOBAL_NPC_DESC_SERVER);
			pt.putInt(serial);
			pt.putInt(id);
			pt.putInt(instanceID);
			pt.putString(desc);
			p.send(pt);
		}
	}
	
	/*
	 * 查找任意地区的NPC列表。
	 */
	protected void globalNpcList(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		if (p != null) {
			int serial = packet.getInt();
			int areaID = packet.getInt();
			List<Creature> l = new ArrayList<Creature>();
			List<Integer> ml = new ArrayList<Integer>();

			// 场景ID=地区ID << 4 + 地区内场景编号
			for (int i = 0; i < 16; i++) {
				// 查找地图，只有非副本地图才能列出NPC列表
				int mapID = (areaID << 4) | i;
				VMapManager mgr = Server.server.getWorld().getVMapManager(mapID);
				if (mgr == null || !(mgr instanceof NoInstanceVMapManager)) {
					continue;
				}
				VMap[] maps = ((NoInstanceVMapManager)mgr).getVMaps(mapID);
				if (maps == null) {
					continue;
				}
				
				// 列出地图中所有的NPC
				for (GameObject o : maps[0].instanceid2objects.values()) {
					if(o.type==GameObject.TYPE_CREATURE&&(o.faction==p.faction||o.faction==GameObject.FACTION_NEUTRAL)){
						Creature c = (Creature)o;
						if(c.searchName!=null||c.touchAction!=null||c.subTitle!=null){ //如果serarchName不为空或者是功能npc
							l.add((Creature)o);
							ml.add(mapID);
						}
					}
				}
			}
			
			// 发送返回包
			Packet pt = new Packet(OpCode.GLOBAL_NPC_LIST_SERVER);
			pt.putInt(serial);
			pt.putInt(areaID);
			int size = l.size();
			pt.putShort(size);
			for (int i = 0; i < size; i++) {
				Creature c = l.get(i);
				pt.putInt(c.id);
				pt.putInt(c.instanceId);
				pt.putString(c.title);
				pt.putString(c.subTitle==null?"":c.subTitle);
				pt.putInt(ml.get(i));
				pt.putShort(c.x / 8);
				pt.putShort(c.y / 8);
			}
			p.send(pt);
		}
	}
		
	
	protected void qmeCanPay(Packet packet, ClientSession session){
		int b = Server.server.getConfig().getInt("qmepay",0);
		Packet pt = new Packet(OpCode.QME_CANPAY_SERVER);
		pt.put(b);
		session.send(pt);
	}
	
	protected void buyModifyTowerBanner(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			TongService tongService = Server.server.getServiceRegistry().getTongService();
			Tong tong = tongService.getPlayerTong(p.id,false);
			if(tong!=null && tong.getChairmanName().equals(p.name)){
				GameItem item = ObjectAccessor.createGameItem(1106);
				PlayerTransaction tx = p.newTransaction("BUYTOWERBANNER");
				try {
					p.decMoney(100000, tx, true);
					p.bag.addGameItemComplete(item, 1, tx, true);
					tx.commit();
				} catch (NoEnoughValueException e) {
					tx.rollback();
				} catch (NoEnoughSpaceException e) {
					tx.rollback();
				}
			}
		}
	}
	
	protected void tongBattleGetExp(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			TongService tongService = Server.server.getServiceRegistry().getTongService();
			Tong tong = tongService.getPlayerTong(p.id,false);
			TongBattleApplyService applyService = Server.server.getServiceRegistry().getTongBattleApplyService();
			if(tong!=null && applyService.isWinner(tong.id)){
				if(p.map.getId()!=applyService.getWinnerMapId(tong.id)){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_GETEXP_CLIENT, peony.Messages.STRING_00138);
					return;
				}
				if(p.pool.getInt(Player.PROPERTY_TONGBATTLE_EXPDAY,0)==Time.day){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_GETEXP_CLIENT, peony.Messages.STRING_00139);
					return;
				}
				PlayerTransaction tx = p.newTransaction("TONGBATTLEEXP");
				int gainExp = 100 * (p.level);
				p.addExp(gainExp, tx, true);
				tx.commit();
				p.pool.setInt(Player.PROPERTY_TONGBATTLE_EXPDAY, Time.day);
				Packet pt = new Packet(OpCode.TONG_BATTLE_GETEXP_SERVER);
				pt.putInt(serial);
				p.send(pt);
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_GETEXP_CLIENT, peony.Messages.STRING_00140);
			}
		}
	}
	
	protected void tongBattleAbandon(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			TongService tongService = Server.server.getServiceRegistry().getTongService();
			Tong tong = tongService.getPlayerTong(p.id,false);
			if(tong!=null){
				TongBattleApplyService applyService = Server.server.getServiceRegistry().getTongBattleApplyService();
				try {
					if(!tong.getChairmanName().equals(p.name)){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_ABANDON_CLIENT, peony.Messages.STRING_00141);
						return;
					}
					applyService.abandon(p, tong.id);
					Packet pt = new Packet(OpCode.TONG_BATTLE_ABANDON_SERVER);
					pt.putInt(serial);
					p.send(pt);
				} catch (TongBattleException e) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_ABANDON_CLIENT, e.getMessage());
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_ABANDON_CLIENT, peony.Messages.STRING_00142);
			}
		}
	}
	
	protected void tongBattleUnTag(Packet packet, ClientSession session){
//		Player p = (Player)session.getClient();
//		if(p!=null){
//			int serial = packet.getInt();
//			int playerId = packet.getInt();
//			TongService tongService = Server.server.getServiceRegistry().getTongService();
//			Tong tong = tongService.getPlayerTong(p.id,false);
//			TongMember tmPlayer = tongService.getPlayerInfo(p.id);
//			if(tong!=null && tmPlayer!=null && tmPlayer.duty>TongService.NORMAL){
//				TongMember tm = tongService.getPlayerInfo(playerId);
//				if(tm == null){
//					for(TongMember t : tong.members){
//						if(t.id == playerId){
//							tm = t;
//						}
//					}
//				}
//				tm.battleTag = 0;
//				Packet pt = new Packet(OpCode.TONG_BATTLE_UNTAG_SERVER);
//				pt.putInt(serial);
//				p.send(pt);
//			}else{
//				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_UNTAG_CLIENT, peony.Messages.STRING_00144);
//			}
//		}
		Server.server.getServiceRegistry().getDbService().schedule(new UnTagTongCall(session, packet));
	}
	
	protected void tongBattleMakeTax(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int tax = packet.getByte();
			TongService tongService = Server.server.getServiceRegistry().getTongService();
			TongBattleApplyService service = Server.server.getServiceRegistry().getTongBattleApplyService();
			Tong tong = tongService.getPlayerTong(p.id,false);
			if(tong!=null && tong.getChairmanName().equals(p.name) && service.isWinner(tong.id)){
				if(p.map.getId()!=service.getWinnerMapId(tong.id)){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_MAKETAX_CLIENT, peony.Messages.STRING_00144);
					return;
				}
				if(tax>10 || tax<5){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_MAKETAX_CLIENT, peony.Messages.STRING_00145);
				}else{
					tong.taxRate = tax / 100f;
					tong.modify = true;
					Packet pt = new Packet(OpCode.TONG_BATTLE_MAKETAX_SERVER);
					pt.putInt(serial);
					p.send(pt);
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_MAKETAX_CLIENT, peony.Messages.STRING_00144);
			}
		}
	}
	
	protected void tongBattleTax(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int signMapId = packet.getInt();
			TongBattleApplyService applyService = Server.server.getServiceRegistry().getTongBattleApplyService();
			Tong tong = applyService.getWinnerTong(signMapId);
			int tax = 0;
			if(tong!=null){
				tax = (int) (tong.taxRate * 100);
			}
			Packet pt = new Packet(OpCode.TONG_BATTLE_TAX_SERVER);
			pt.putInt(serial);
			pt.put(tax);
			p.send(pt);
		}
	}
	
	protected void tongBattleWinnerInfo(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int signMapId = packet.getInt();
			TongBattleApplyService service = Server.server.getServiceRegistry().getTongBattleApplyService();
			String winTongName = service.getWinner(signMapId);
			Tong tong = service.getWinnerTong(signMapId);
			TongBattleApplyService appService = Server.server.getServiceRegistry().getTongBattleApplyService();
			Packet pt = new Packet(OpCode.TONGBATTLE_WINNER_INFO_SERVER);
			pt.putInt(serial);
			if(tong!=null){
				pt.putString(MessageFormat.format(peony.Messages.STRING_00146,
						winTongName, tong.getChairmanName(), tong.level, tong.pool.getInt(Tong.PROPERTY_TONGBATTLE_WIN, 0), tong.members.size(), tong.money, appService.getApplyByTongId(tong.id).money));
			}else{
				pt.putString(peony.Messages.STRING_00147);
			}
			p.send(pt);
		}
	}
	
	protected void tongBattleTag(Packet packet, ClientSession session){
//		Player p = (Player)session.getClient();
//		if(p!=null){
//			int serial = packet.getInt();
//			int playerId = packet.getInt();
//			TongBattleApplyService applyService = Server.server.getServiceRegistry().getTongBattleApplyService();
//			try {
//				applyService.tagTongBattle(p, playerId);
//				Packet pt = new Packet(OpCode.TONG_BATTLE_TAG_SERVER);
//				pt.putInt(serial);
//				p.send(pt);
//			} catch (TongBattleException e) {
//				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_TAG_CLIENT, e.getMessage());
//			}
//		}
		Server.server.getServiceRegistry().getDbService().schedule(new TagTongCall(session, packet));
	}
	
	protected void cmccShopBuy(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getAccountService().schedule(new CmccBuyCall(session, packet));
	}
	
	protected void cmccPushDownloadOk(Packet packet, ClientSession session){
		Account a = (Account)session.getIdentity();
		if(a.getCmccUserId()!=null&&a.getCmccUserKey()!=null){
			Server.server.getServiceRegistry().getSlaveAccountService().postMessage(new CmccDownloadOkMessage(a.getCmccUserId()));
		}
	}
	
	
	protected void cmccIShop(Packet packet, ClientSession session){
		CmccShopListCall call = new CmccShopListCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void tongQuest(Packet packet, ClientSession session){
		TongQuestCall call = new TongQuestCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void tongQuestRequest(Packet packet, ClientSession session){
		TongQuestRequestCall call = new TongQuestRequestCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	protected void getItemFromNpc(Packet packet, ClientSession session){
		GetItemFromNpcCall call = new GetItemFromNpcCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	protected void tongBattleBuy(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int type = packet.getByte();
		Player p = (Player)session.getClient();
		TongBattleVMapManager tongBattleService = Server.server.getServiceRegistry().getTongBattleVMapManager();
		try {
			if(type==0){
				tongBattleService.buyWarCarriage(p);
			}else if(type==1){
				tongBattleService.buyTower(p);
			}
			Packet pt = new Packet(OpCode.TONGBATTLE_BUY_SERVER);
			pt.putInt(serial);
			session.send(pt);
		} catch (TongBattleException e) {
			ErrorHandler.sendErrorMessage(session, serial, OpCode.TONGBATTLE_BUY_CLIENT, e.getMessage());
		}
	}
	
	protected void tongBattleTransport(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			try {
				Server.server.getServiceRegistry().getTongBattleVMapManager().tran(p);
				Packet pt = new Packet(OpCode.TONG_BATTLE_TRANSPORT_SERVER);
				pt.putInt(serial);
				p.send(pt);
			} catch (VMapException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_TRANSPORT_CLIENT, e.getMessage());
			}
		}
	}
	
	protected void tongBattleTime(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int mapId = packet.getInt();
			TongBattleApplyService applyService = Server.server.getServiceRegistry().getTongBattleApplyService();
			String time = applyService.getCanApplyTime(mapId);
			Packet pt = new Packet(OpCode.TONG_BATTLETIME_SERVER);
			pt.putInt(serial);
			pt.putString(time);
			p.send(pt);
		}
	}
	
	protected void tongBattleBid(Packet packet, ClientSession session){
		TongBattleBidCall call = new TongBattleBidCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void tongBattleApply(Packet packet, ClientSession session){
		TongBattleApplyCall call = new TongBattleApplyCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void tongBattleApplyList(Packet packet, ClientSession session){
		TongBattleApplyListCall call = new TongBattleApplyListCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}	
	protected void cmccChargeTime(Packet packet, ClientSession session){
		int serial = packet.getInt();
   		Calendar c = Calendar.getInstance();
   		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd"); 
        String end = sf.format(c.getTime());
   		c.add(Calendar.DATE,-7);
   		String start = sf.format(c.getTime());
   		Packet pt = new Packet(OpCode.CMCC_CHARGE_TIME_SERVER);
   		pt.putInt(serial);
   		pt.putString(start);
   		pt.putString(end);
   		session.send(pt);
	}
	
	protected void ibuyHistory(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getAccountService().schedule(new IBuyHistoryCall(session, packet));
	}
	
	protected void phoneNotify(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getAccountService().schedule(new PhoneNotifyCall(session, packet.getString()));
	}
	
	protected void cmccCharge(Packet packet, ClientSession session){
		CmccChargeCall call = new CmccChargeCall(session, packet);
		Server.server.getServiceRegistry().getAccountService().schedule(call);
	}

	/*
	 * 客户端通过AppStore购买元宝后，通知服务器验证订单。
	 */
	protected void appStoreCharge2(Packet packet, ClientSession session) {
		AppStoreService service = (AppStoreService)Server.server.getServiceRegistry().getService(AppStoreService.class);
		if (service != null) {
			Player p = (Player)session.getClient();
			Account a = (Account)session.getIdentity();
			if (a != null) {
				String bid = packet.getString();
				byte[] receipt = packet.getBytes();
				String clientID;
				try {
					clientID = packet.getString();
				} catch (Exception e) {
					clientID = "";
				}
				service.checkReceipt2(session, a, p, bid, receipt, clientID);
			}
		}
	}
	
	/*
	 * 客户端通过AppStore购买元宝后，通知服务器验证订单。
	 */
	protected void appStoreCharge3(Packet packet, ClientSession session) {
		AppStoreService service = (AppStoreService)Server.server.getServiceRegistry().getService(AppStoreService.class);
		if (service != null) {
			Player p = (Player)session.getClient();
			Account a = (Account)session.getIdentity();
			if (a != null) {
				int serial = packet.getInt();
				String bid = packet.getString();
				byte[] receipt = packet.getBytes();
				String clientID;
				try {
					clientID = packet.getString();
				} catch (Exception e) {
					clientID = "";
				}
				service.checkReceipt3(session, a, p, serial, bid, receipt, clientID);
				//Server.server.getServiceRegistry().getDbService().schedule(new LiMeiActivationCall(session, serial, clientID));
			}
		}
	}

	/*
	 * 客户端查询AppStore可用商品列表。
	 */
	protected void appStoreListProduct2(Packet packet, ClientSession session) {
		AppStoreService service = (AppStoreService)Server.server.getServiceRegistry().getService(AppStoreService.class);
		if (service != null) {
			Player p = (Player)session.getClient();
			int serial = packet.getInt();
			String bid = packet.getString();
			String clientID;
			try {
				clientID = packet.getString();
			} catch (Exception e) {
				clientID = "";
			}
			if (p != null) {
				service.listProduct2(session, serial, bid, clientID);
			}
		}
	}
	
	protected void tongSkillStudy(Packet packet, ClientSession session){
		TongSkillStudyCall call = new TongSkillStudyCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void tongSkillDesc(Packet packet, ClientSession session){
		TongSkillDescCall call = new TongSkillDescCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void tongSkillList(Packet packet, ClientSession session){
		TongSkillListCall call = new TongSkillListCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void tongContribute(Packet packet, ClientSession session){
		TongContributeCall call = new TongContributeCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void tongInfo(Packet packet, ClientSession session){
		TongInfoCall call = new TongInfoCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	/** 世界喊话 **/
	protected void worldShout(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			String content = packet.getString();
			content = StringUtil.filterBadWords(content);
			if(p.level<20){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WORLD_SHOUT_CLIENT, peony.Messages.STRING_00148);
				return;
			}
			PlayerTransaction tx = p.newTransaction("CHAT");
			GameItem gameItem = p.bag.removeGameItem(499, -1, 1, tx, true);
			if(gameItem!=null){
				tx.commit();
				ChatService chatService = Server.server.getServiceRegistry().getChatService();
				chatService.sendWorldShout(p.name,p.id,p.faction, content, 0xff4700, 11000,p.vipLevel);//p.vipLevel>0?"{#V.pip,0}"+p.name:
				Packet pt = new Packet(OpCode.WORLD_SHOUT_SERVER);
				pt.putInt(serial);
				p.send(pt);
				Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_USEITEM,p,499,1));
			}else{
				tx.rollback();
			}
		}
	}
	
	/** 领取国家科技道具 **/
	protected void getNationSkillItem(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int skillId = packet.getInt();
			int skillLevel = packet.get();
			Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(p.faction);
			NationSkill nationSkill = nation.skills.get(skillId);
			if(nationSkill!=null && nationSkill.level==skillLevel){
				if(nationSkill.type==NationSkill.TYPE_ITEM){
					nationSkill.fire(p);
					Packet pt = new Packet(OpCode.NATION_SKILL_ITEM_SERVER);
					pt.putInt(serial);
					p.send(pt);
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_SKILL_ITEM_CLIENT, peony.Messages.STRING_00149);
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_SKILL_ITEM_CLIENT, peony.Messages.STRING_00150);
			}
		}
	}
	
	/**
	 * 马装备解绑
	 */
	protected void horseequipUnbind(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int gameItemId = packet.getInt();
			int instanceId = packet.getInt();
			Object[] o = ItemUtil.findPlayerEquipment(p, gameItemId,instanceId);
			if(o==null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_EQUIPMENT_UNBIND_CLIENT
						, peony.Messages.STRING_00151);
				return;
			}
			GameItem gameItem = (GameItem)o[0];
			if(!gameItem.isBound()){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_EQUIPMENT_UNBIND_CLIENT
						, peony.Messages.STRING_00152);
			}else if(gameItem.bindInstance==0){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_EQUIPMENT_UNBIND_CLIENT
						, peony.Messages.STRING_00153);
			}else{
				PlayerTransaction tx = p.newTransaction("HEU");
				GameItem item = p.bag.removeGameItem(491, -1, 1, tx, true);
				if(item!=null){
					tx.commit();
					gameItem.bindInstance=0;
					Packet pt = new Packet(OpCode.HORSE_EQUIPMENT_UNBIND_SERVER);
					pt.putInt(serial);
					pt.put(gameItem.toClientBytes());
					p.send(pt);
					log.info("[UNBINDHORSEEQUP]"+LogUtil.getPlayerLogString(p)+LogUtil.getGameItemString(item, 1));
				}else{
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_EQUIPMENT_UNBIND_CLIENT
							, MessageFormat.format(peony.Messages.STRING_00154, ObjectAccessor.getItemTemplate(491).name));
				}
			}
		}
	}
	
	
	/**
	 * 坐骑幻化
	 */
	protected void horseChange(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int destInstId = packet.getInt();
		int resInstId = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
			//ExpService expService = Server.server.getServiceRegistry().getExpService();
			//expService.isAgentHorse(p, horseInstanceId);
			Horse destHorse = p.horseBag.getHorse(destInstId);
			Horse resHorse = p.horseBag.getHorse(resInstId);
			if(destHorse == null || resHorse == null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, "您没有该坐骑");
				return;
			}else{
				//国公坐骑不能幻化
				if(CandidateService.isKingHorse(destHorse.itemId) || CandidateService.isKingHorse(resHorse.itemId)){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, "对不起,国公坐骑不允许幻化");
					return;
				}
				if(destHorse.state != 1 || resHorse.state != 1){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, "未激活的坐骑不允许幻化");
					return;
				}
				if(destHorse.level < Horse.minChangeImgLevel || resHorse.level < Horse.minChangeImgLevel){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, MessageFormat.format("坐骑等级不能小于{0}级", Horse.minChangeImgLevel));
					return;
				}
				if(resHorse.imageIdChange >= 0){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, "已幻化的坐骑不允许被幻化");
					return;
				}
				if(destHorse.imageIdChange >= 0){
					if(destHorse.imageIdChange == resHorse.imageId){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, "相同外表下的坐骑不允许幻化");
						return;
					}
				}else{
					if(destHorse.imageId == resHorse.imageId){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, "相同外表下的坐骑不允许幻化");
						return;
					}
				}
				if(p.horse != null && (p.horse.instanceId == destInstId || p.horse.instanceId == resInstId)){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, "请先下坐骑后再进行幻化");
					return;
				}
				if(!resHorse.equs.isEmpty()){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, "装配装备的坐骑不允许幻化");
					return;
				}
			}
			PlayerTransaction tx = p.newTransaction("HORSEIMGCHANGE");
			try {
//				p.decCredit(Horse.horseChangeCredit, tx, false);
				int credit = ActivityItemEffect.processHorseChange(p, Horse.horseChangeCredit);
				p.decCredit(credit, tx, false);
				tx.commit();
			} catch (NoEnoughValueException e) {
				tx.rollback();
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, peony.Messages.STRING_00432);
				return;
			}
			
			p.horseBag.horseImageChange(p, destInstId, resInstId, serial);
			ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, "幻化成功");
		}
	}
	
	/**
	 * 解除坐骑幻化
	 */
	protected void removehorseChange(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int instId = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
			Horse horse = p.horseBag.getHorse(instId);
			if(horse == null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.REMOVE_HORSE_CHANGE_CLIENT, "您没有该坐骑");
				return;
			}
			if(p.horse != null && p.horse.instanceId == instId){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.REMOVE_HORSE_CHANGE_CLIENT, "请先下骑后再进行坐骑还原");
				return;
			}
			PlayerTransaction tx = p.newTransaction("HORSEREMOVECHANGE");
			try {
				p.decCredit(Horse.removeHorseChangeCredit, tx, false);
				tx.commit();
			} catch (NoEnoughValueException e) {
				tx.rollback();
				ErrorHandler.sendErrorMessage(session, serial, OpCode.REMOVE_HORSE_CHANGE_CLIENT, peony.Messages.STRING_00432);
				return;
			}
			p.horseBag.removehorseImageChange(p, instId, serial);
			ErrorHandler.sendErrorMessage(session, serial, OpCode.REMOVE_HORSE_CHANGE_CLIENT, "幻化解除成功");
		}
	}
	
	/**
	 * 坐骑合成
	 */
	protected void horseFix(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int destInstId = packet.getInt();
		int resInstId = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
			Horse destHorse = p.horseBag.getHorse(destInstId);
			Horse resHorse = p.horseBag.getHorse(resInstId);
			if(destHorse == null || resHorse == null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, "您没有该坐骑");
				return;
			}
			if(p.horse!=null && (destHorse.instanceId==p.horse.instanceId || resHorse.instanceId==p.horse.instanceId)){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, "请先下坐骑后再进行合成");
				return;
			}
			//青聪马和枣红马不能合成
//			if(!Horse.canFixOrChangeImg(destHorse.itemId) || !Horse.canFixOrChangeImg(resHorse.itemId)){
//				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, "此种坐骑不允许合成");
//				return;
//			}
			//国公坐骑不能合成
			if(CandidateService.isKingHorse(destHorse.itemId)  || CandidateService.isKingHorse(resHorse.itemId)){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, "对不起,国公坐骑不允许合成");
				return;
			}
			if(destHorse.state != 1 || resHorse.state != 1){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, "未激活的坐骑不允许合成");
				return;
			}
			if(destHorse.level < Horse.minFixLevel || resHorse.level < Horse.minFixLevel){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, MessageFormat.format("坐骑等级不能小于{0}级", Horse.minFixLevel));
				return;
			}
			if(destHorse.fixCount >= Horse.maxFixCount){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, MessageFormat.format("坐骑最多只能合成{0}次", Horse.maxFixCount));
				return;
			}
			
			int destImageId = destHorse.imageId;
			int resImageId = resHorse.imageId;
			if(destHorse.imageIdChange >= 0){
				destImageId = destHorse.imageIdChange;
			}
			if(resHorse.imageIdChange >= 0){
				resImageId = resHorse.imageIdChange;
			}
			if(destImageId != resImageId){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, "同种类的坐骑才能合成");
				return;
			}
			
			if(!resHorse.equs.isEmpty()){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, "装配装备的坐骑不允许合成");
				return;
			}
			if(p.failHorseInst != -1){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_CHANGE_CLIENT, "合成失败未确认！");
				return;
			}
			
			PlayerTransaction tx = p.newTransaction("HORSEFIX");
			try {
//				int credit = (destHorse.fixCount+1) * Horse.fixCreditParam;
				int credit = (destHorse.fixCount+1) * Horse.fixCreditParam;
				int newCreidt = ActivityItemEffect.processHorseFix(p, credit);
				newCreidt = VipPrivilegeService.decHorseFixCredit(p, newCreidt);
				p.decCredit(newCreidt, tx, false);
				tx.commit();
			} catch (NoEnoughValueException e) {
				tx.rollback();
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_FIX_CLIENT, peony.Messages.STRING_00432);
				return;
			}
			int price = Math.round(Server.server.getServiceRegistry().getShopService().getItemPrice(HorseBag.decIMoneyItem));
			boolean success = p.horseBag.horseFix(p, destInstId, resInstId, price, serial);
			if(success){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_FIX_CLIENT, "合成成功");
				//统计坐骑合成成就
				StatService statService = Server.server.getServiceRegistry().getStatService();
				statService.playerMergeHorse(p, destHorse);
			}
		}
	}
	
	/**
	 * 坐骑合成失败，是否保留副坐骑确认
	 */
	protected void horseFixFail(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int type = packet.get();
		int instId = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
			if(type == 0){	//不保留
				p.horseBag.removeHorse(instId);
				Packet pt = new Packet(OpCode.HORSE_FIXFAILURE_SERVER);
				pt.putInt(serial);
				p.send(pt);
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_FIXFAILURE_CLIENT, "合成失败,副坐骑消失");
				if(instId == p.failHorseInst){
					p.failHorseInst = -1;
				}
				// 记录日志
				LogUtil.logHorseFixFail(p, instId, 3);
			}else if(type == 1){
				Server.server.getServiceRegistry().getSyncExecutorService().schedule(
						new FixHorseIbuyCall(p.session, null, p, serial, instId));
				if(instId == p.failHorseInst){
					p.failHorseInst = -1;
				}
			}
		}
	}
	
	/**
	 * 获取最快击杀Boss的排名榜
	 */
	protected void bossTimeBoard(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(
				new BossTimeScoreCall(session, packet));
	}
	
	/**
	 * 获取最早击杀Boss的排名榜
	 */
	protected void bossScoreBoard(Packet packet ,ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(
				new BossScoreBoardCall(session, packet));
	}
	
	/**
	 * 坐骑栏扩展
	 */
	protected void horsebagExtend(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		int serial = packet.getInt();
		if(p!=null){
			if(p.horseBag.maxSize>=15){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSEBAG_EXTEND_CLIENT
						, peony.Messages.STRING_00155);
				return;
			}
			PlayerTransaction tx = p.newTransaction("HBE");
			try {
				p.decMoney(p.horseBag.getExtendHorsebagMoney(), tx, true);
				p.horseBag.maxSize++;
				tx.commit();
				if(p.horseBag.maxSize==15){
					Server.server.getServiceRegistry().getChatService()
					.sendPrivateMessage(p.id, peony.Messages.STRING_00156);
				}else{
					Server.server.getServiceRegistry().getChatService()
					.sendPrivateMessage(p.id, 
							MessageFormat.format(peony.Messages.STRING_00157, 
									p.horseBag.maxSize,(25-p.horseBag.maxSize)));
				}
				Packet pt = new Packet(OpCode.HORSEBAG_EXTEND_SERVER);
				pt.putInt(serial);
				p.send(pt);
				LogUtil.logExtendHorseBag(p);
			} catch (NoEnoughValueException e) {
				tx.rollback();
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSEBAG_EXTEND_CLIENT, peony.Messages.STRING_00158);
			}
		}
	}
	
	/**
	 * 繁体版本支付。
	 * @param packet
	 * @param session
	 */
	protected void qmePay(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			Account a = (Account) session.getIdentity();
			Server.server.getServiceRegistry().getDbService().schedule(
					new QmePayCall(session, packet, player, a.getChannel()));
		}
	}

	/**
	 * 繁体版本查询余额。
	 * @param packet
	 * @param session
	 */
	protected void qmeQueryBalance(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			Account a = (Account) session.getIdentity();
			Server.server.getServiceRegistry().getDbService().schedule(
					new QmeQueryBalanceCall(session, packet));
		}
	}

	/**
	 * 答题
	 */
	protected void answer(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int questionId = packet.getInt();
			String answer = packet.getString();
			answer = answer.replace(" ", "").replace("　", "");
			QuestionService questionService = Server.server.getServiceRegistry().getQuestionService();
			try {
				questionService.anwser(p, questionId, answer);
				Packet pt = new Packet(OpCode.ANSWER_SERVER);
				pt.putInt(serial);
				p.send(pt);
			} catch (QuestionException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ANSWER_CLIENT, e.getMessage());
			}
			questionService.questionMap.remove(p.id);
			questionService.questionMap1.remove(p.id);
		}
	}
	
	protected void equMark(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int itemId = packet.getInt();
		int instanceId = packet.getInt();
		String msg = packet.getString();
		Player player = (Player)session.getClient();
		if(player != null){
			Object[] os = ItemUtil.findPlayerEquipment(player, itemId,
					instanceId);
			if (os != null) {
				GameItem item = (GameItem) os[0];
				if (!item.template.isEquipment()) {
					return;
				}
				if(item.template.equipment.markCharCount <= 0){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.EQUIPEMENT_MARK_CLIENT, peony.Messages.STRING_00159);
					return;
				}
				if(item.template.equipment.markCharCount < msg.length()){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.EQUIPEMENT_MARK_CLIENT, MessageFormat.format(peony.Messages.STRING_00160, item.template.equipment.markCharCount));
					return;					
				}
				ItemEnhance ie = (ItemEnhance)item.object;
				if(ie != null){
					if(ie.getMarkString().length()>0){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.EQUIPEMENT_MARK_CLIENT, peony.Messages.STRING_00161);
						return;	
					}
				}
				if(StringUtil.isValidText(msg) != IStringValidator.OK || StringUtil.hasBadWord(msg)){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.EQUIPEMENT_MARK_CLIENT, peony.Messages.STRING_00162);
					return;	
				}
				if(ie == null){
					ie = new ItemEnhance();
					item.object = ie;
					
				}
				ie.setMarkString(MessageFormat.format(peony.Messages.STRING_00163, msg,player.name));
				Packet pt = new Packet(OpCode.EQUIPEMENT_MARK_SERVER);
				pt.putInt(serial);
				pt.put(item.toClientBytes());
				player.send(pt);
				
				LogUtil.logEquMarkOK(player, item);
			}
		}
	}
	
	protected void nationConvoy(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		int serial = packet.getInt();
		int type = packet.get();
		int index = packet.get();
		if(player != null){
			if(player.isKing()==1){
				Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(player.faction);
				NationConvoyService convoyService = Server.server.getServiceRegistry().getNationConvoyService();
				if(type == 0){//开启押运
					if(nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_STATE,0)==0){
						try {
							Server.server.getServiceRegistry().getNationConvoyService().startConvoy(nation,index);
							Packet pt = new Packet(OpCode.NATION_CONVOY_SERVER);
							pt.putInt(serial);
							int t = 0;
							if(nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_MODIFYTIME,0)==Time.day){
								t=1;
							}else{
								if(convoyService.isInTime()){
									t=2;
								}
							}
							pt.put(t);
							player.send(pt);
						} catch (ConvoyException e) {
							ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_CONVOY_CLIENT, e.getMessage());
						}
					}
				}else if(type == 1){//关闭押运
					if(convoyService.isInTime()){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_CONVOY_CLIENT, "每天00:00-20:00才可以关闭押运任务");
						return;
					}
					nation.pool.remove(Nation.PROPERTY_NATIONCONVOY_STATE);
					nation.pool.remove(Nation.PROPERTY_NATIONCONVOY_DATE);
					Packet pt = new Packet(OpCode.NATION_CONVOY_SERVER);
					pt.putInt(serial);
					int t = 0;
					if(nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_MODIFYTIME,0)==Time.day){
						t=1;
					}else{
						if(convoyService.isInTime()){
							t=2;
						}
					}
					pt.put(t);
					player.send(pt);
				} else if(type == 2){//修改押运时间
					if(index<0 || index>=NationConvoyService.BEGIN_HOUR.length){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_CONVOY_CLIENT, "数据错误");
						return;
					}
					
					if(nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_MODIFYTIME,0)==Time.day){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_CONVOY_CLIENT, "每天只能修改一次押运时间哦");
						return;
					}
					
					if(convoyService.isInTime()){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_CONVOY_CLIENT, "每天00:00-20:00才可以修改押运时间");
						return;
					}
					
					if(nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_DATE,0)== convoyService.getStartTime(index)){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_CONVOY_CLIENT, "已经在这个时段了");
						return;
					}
					nation.pool.setInt(Nation.PROPERTY_NATIONCONVOY_DATE, convoyService.getStartTime(index));
					nation.pool.setInt(Nation.PROPERTY_NATIONCONVOY_MODIFYTIME,Time.day);
				    String kingName = "";
				    try{
				    	kingName = nation.getKingName();
				    }catch(Exception e){
				    	
				    }
				    if(!kingName.equals("")){
						String message = MessageFormat.format("国公{0}将国家押运设定在{1}:{2}{3}，请广大英雄提前做好准备！", kingName,NationConvoyService.BEGIN_HOUR[index],NationConvoyService.BEGIN_MINUTE[index],NationConvoyService.BEGIN_MINUTE[index]==0?"0":"");
						Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(nation.faction, message);
				    }
					Packet pt = new Packet(OpCode.NATION_CONVOY_SERVER);
					pt.putInt(serial);
					int t = 0;
					if(nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_MODIFYTIME,0)==Time.day){
						t=1;
					}else{
						if(convoyService.isInTime()){
							t=2;
						}
					}
					pt.put(t);
					player.send(pt);
				}
			}else{
				String msg = peony.Messages.STRING_00164;
				if(type == 1){
					msg = "只有国公能够关闭国家押运";
				}else if(type == 2){
					msg = "只有国公能够修改国家押运时间";
				}
				ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_CONVOY_CLIENT, msg);
			}
		}
	}
	
	protected void kingTax(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			if (player.isKing() == 1) {
				int rate = packet.get();
				Nation nation = Server.server.getServiceRegistry()
						.getNationService().getNationByFaction(player.faction);
				if (nation.pool.getInt(Nation.PROPERTY_TAX_DAY, 0) != Time.day) {
					if (rate >= 5 && rate <= 15) {
						float oldRate = nation.taxRate;
						nation.taxRate = rate / 100.0f;
						if(oldRate>nation.taxRate){
							Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(player.faction, 
									MessageFormat.format(peony.Messages.STRING_00165, player.name));
							nation.pool.setInt(Nation.PROPERTY_TAX_DAY, Time.day);
						}else if(oldRate<nation.taxRate){
							Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(player.faction,
									MessageFormat.format(peony.Messages.STRING_00166, player.name));
							nation.pool.setInt(Nation.PROPERTY_TAX_DAY, Time.day);
						}
						Packet pt = new Packet(OpCode.KING_TAXRATE_SERVER);
						pt.putInt(serial);
						player.send(pt);
					}else{
						ErrorHandler.sendErrorMessage(session, serial, OpCode.KING_TAXRATE_CLIENT, peony.Messages.STRING_00167);
					}
				} else {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.KING_TAXRATE_CLIENT, peony.Messages.STRING_00168);
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.KING_TAXRATE_CLIENT, peony.Messages.STRING_00169);
			}
		}
	}
	
	/**
	 * 装备自动资质鉴定
	 */
	protected void autoNaturalEnhance(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int itemId = packet.getInt();
			int instanceId = packet.getInt();
			int level = packet.getByte();
			int specialAtt = packet.getByte();
			if(level<0 || level>4)
				return;
			Object[] os = ItemUtil.findPlayerEquipment(player, itemId, instanceId);
			if (os != null) {
				GameItem item = (GameItem) os[0];
				if (!item.template.isEquipment()) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.AUTO_NATURALENHANCE_CLIENT, peony.Messages.STRING_00170);
					return;
				}
				if (!item.template.equipment.canJudgePotential) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.AUTO_NATURALENHANCE_CLIENT, peony.Messages.STRING_00171);
					return;
				}
				if(player.bag.getGameItemCount(ItemUtil.ITEM_NATURAL_ENHANCE)==0){
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.AUTO_NATURALENHANCE_CLIENT, peony.Messages.STRING_00172);
					return;
				}
				if(specialAtt>=0){
				    int[] atts = item.template.equipment.getNaturalEnhanceAtts();
				    boolean canEnhance = false;
				    for(int att : atts){
				        if(att == specialAtt)
				            canEnhance = true;
				    }
				    if(!canEnhance){
				        ErrorHandler.sendErrorMessage(session, serial,
	                            OpCode.AUTO_NATURALENHANCE_CLIENT, "该装备不能鉴定此属性");
	                    return;
				    }
				}
				Object owner = os[1]; // 装备拥有者
				int price = item.template.level * item.template.level / 8;
				AutoNaturalEnhance autoEnhance = new AutoNaturalEnhance(serial, item, level
						, price, owner, itemId, instanceId);
				autoEnhance.specialAtt = specialAtt;
				player.autoNaturalEnhance = autoEnhance;
				Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_ENHANCE,player,0));
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.AUTO_NATURALENHANCE_CLIENT, peony.Messages.STRING_00173);
			}
		}
	}
	
	/**
	 * 装备自动装备强化
	 */
	protected void autoEquipEnhance(Packet packet, ClientSession session){
		Player player = (Player)session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int itemId = packet.getInt();
			int instanceId = packet.getInt();
			int percent = packet.getInt();
			int specialAttIndex = packet.getInt();
			Object[] os = ItemUtil.findPlayerEquipment(player, itemId, instanceId);
			if (os != null) {
				GameItem item = (GameItem) os[0];
				Object owner = os[1];
				if (item == null) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.AUTO_EQUIPENHANCE_CLIENT, peony.Messages.STRING_01352);
					return;
				}
				if (!item.template.isEquipment()) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.AUTO_EQUIPENHANCE_CLIENT, peony.Messages.STRING_01353);
					return;
				}
				if(EquipEnhanceCall.canIntensify(itemId)){
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.AUTO_EQUIPENHANCE_CLIENT, "此装备不允许强化");
					return;
				}
				EnhanceService enhService = Server.server.getServiceRegistry().getEnhanceService();
				int enhanceTimes = enhService.getEnhanceTimes(player);
				//军团专属科技  装备强化福利 
				int maxTimes = 20;
				int decMoney = EquipEnhanceCall.MONEY;
				TongService ts = Server.server.getServiceRegistry().getTongService();
				TongMember tm = ts.getPlayerInfo(player.id);
				if(tm!=null && tm.skills != null && tm.skills.get(4)!=null){
					TongSkill4 tskill = (TongSkill4)tm.skills.get(4);
					if(tskill != null){
						maxTimes = tskill.getValue();
						if(tskill.level == 1){
							decMoney = 100;
						}else if(tskill.level == 2){
							decMoney = 0;
						}
					}
				}
				if(enhanceTimes>=maxTimes){
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.AUTO_EQUIPENHANCE_CLIENT, "次数已用完");
					return;
				}
				int leftTimes = maxTimes - enhanceTimes;
				if(specialAttIndex<0){
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.AUTO_EQUIPENHANCE_CLIENT, peony.Messages.STRING_00172);
					return;
				}
	            // 装备拥有者
				AutoEquipEnhance autoEnhance = new AutoEquipEnhance(serial, item, percent
						, decMoney, leftTimes,owner, itemId, instanceId);
				autoEnhance.specialAtt = specialAttIndex;
				player.autoEquipEnhance = autoEnhance;
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.AUTO_EQUIPENHANCE_CLIENT, peony.Messages.STRING_00173);
			}
		}
	}

	protected void imoneyCard(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getAccountService().schedule(new IMoneyCardCall(session,packet));
	}
	
	protected void setNationBattleMinLevel(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int setLevel = packet.getShort();
		NationService nationService = Server.server.getServiceRegistry().getNationService();
		Player p = (Player)session.getClient();
		try {
			nationService.setBattleFieldMinLevel(p, setLevel);
			Packet pt = new Packet(OpCode.NATIONBATTLE_MINLEVEL_SERVER);
			pt.putInt(serial);
			session.send(pt);
		} catch (NationBattleFieldSignupException e) {
			ErrorHandler.sendErrorMessage(session, serial, OpCode.NATIONBATTLE_MINLEVEL_CLIENT, e.getMessage());
		}
	}
	
//	/**
//	 * 获取文件
//	 * serial						int
//	 * 文件名						string
//	 * 版本							int
//	 */
//	public static final short GET_FILE_CLIENT = 621;
//	
//	/**
//	 * 获取文件成功
//	 * serial						int
//	 * 文件名						string
//	 * 是否存在数据					byte(0 不存在，版本相同 1存在)
//	 * 数据							byte[](如果是否存在数据字段是0，这个字段不存在)
	
	protected void get_File(Packet packet, ClientSession session){
		int serial = packet.getInt();
		String name = packet.getString();
		int version = packet.getInt();
		FileData fd = Server.server.getServiceRegistry().getFileService().getFileData(name);
		if(fd == null){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.GET_FILE_CLIENT, peony.Messages.STRING_00174);
		}else{
			if(fd.version == version){
				Packet pt = new Packet(OpCode.GET_FILE_SERVER);
				pt.putInt(serial);
				pt.putString(name);
				pt.putInt(version);
				pt.put(0);
				session.send(pt);
			}else{
				Packet pt = new Packet(OpCode.GET_FILE_SERVER);
				pt.putInt(serial);
				pt.putString(name);
				pt.putInt(fd.version);
				pt.put(1);
				pt.put(fd.data);
				session.send(pt);
			}
		}
	}
	
	protected void worldTeleport(Packet packet, ClientSession session){
		WorldMapService service = Server.server.getServiceRegistry().getWorldMapService();
		service.worldTeleport(packet, session);
	}
	
	protected void cancelAgentHorse(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int horseInstanceId = packet.getInt();
			ExpService expService = Server.server.getServiceRegistry().getExpService();
			try {
				expService.removeAgentHorse(p, horseInstanceId);
				Packet packet1 = new Packet(OpCode.CANCEL_AGENTHORSE_SERVER);
				packet1.putInt(serial);
				p.send(packet1);
			} catch (ExpException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.CANCEL_AGENTHORSE_CLIENT, e.getMessage());
			}
		}
	}
	
	protected void exchangeExp(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		int serial = packet.getInt();
		int count = packet.getInt();
		ExpService expService = Server.server.getServiceRegistry().getExpService();
		expService.exchaneExp(p, packet, session, serial,count);
	}
	
	protected void queryOfflineExp(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		int serial = packet.getInt();
		int limitcount = packet.getInt();//玩家输入的一合酥数量
		ExpService expService = Server.server.getServiceRegistry().getExpService();
		int exps = expService.getNotOnineExps(p);
		int hasCount = p.bag.getGameItemCount(1183);
		int count = hasCount;
		if(count > limitcount){
			count = limitcount;
		}
		int needExp = expService.getExp(p.level, 30*60*1000L, ExpService.TYPE_PLAYER);
		if(exps < needExp){
			String msg = MessageFormat.format(peony.Messages.STRING_00175, exps);
			ErrorHandler.sendErrorMessage(session, serial, OpCode.QUERY_OFFLINE_EXP_CIENT, msg);
			return;
		}
//		if(count == 0){
//			String msg = MessageFormat.format("<cff0000>提示：</c>您有<cff0000>{0}</c>离线经验，您没有<cff0000>{1}</c>，请进入充值商店购买。", exps,ObjectAccessor.getItemTemplate(1183).name);
//			ErrorHandler.sendErrorMessage(session, serial, OpCode.AGENTHORSE_LIST_CIENT, msg);
//			return;
//		}
		int[] result = expService.calculateOfflineExp(p, count);
		int canChangeExp = 0;
		canChangeExp = result[0];
		int count1 = result[1];
		result = null;
		String msg;
		if(count < count1){
			msg = MessageFormat.format(peony.Messages.STRING_00176, String.valueOf(count),String.valueOf(canChangeExp));
		} else {
		    msg = MessageFormat.format(peony.Messages.STRING_00177, String.valueOf(count1));
		}
		Packet packet1 = new Packet(OpCode.QUERY_OFFLINE_EXP_SERVER);
		packet1.putInt(serial);
		packet1.putInt(exps);
		packet1.putString(msg);
		packet1.putInt(Math.min(count1, count));
		
		if(p!=null){
			p.send(packet1);
		}
	}
	
	protected void addHorseAgent(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int horseId = packet.getInt();
			ExpService expService = Server.server.getServiceRegistry().getExpService();
			try {
				expService.addAgentHorse(p, horseId);
				Packet packet1 = new Packet(OpCode.HORSE_AGENT_SERVER);
				packet1.putInt(serial);
				p.send(packet1);
			} catch (ExpException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_AGENT_CLIENT, e.getMessage());
			}
		}
	}
	
	protected void npcDesc(Packet packet, ClientSession session){
		new NpcDescCall(session,packet).run();
//		Server.server.getServiceRegistry().getDbService().schedule(new NpcDescCall(session,packet));

	}
	
	
	protected void npcList(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		Player p = (Player) session.getClient();
		if (p != null) {
			VMap map = p.getVMap();
			if (map != null) {
				List<Creature> l = new ArrayList<Creature>();
				for (GameObject o : map.instanceid2objects.values()) {
					if(o.type==GameObject.TYPE_CREATURE&&(o.faction==p.faction||o.faction==GameObject.FACTION_NEUTRAL)){
						if(VMap.isSpecialNpc(o.id)){
							continue;
						}
						Creature c = (Creature)o;
						if(c.searchName!=null||c.touchAction!=null||c.subTitle!=null){ //如果serarchName不为空或者是功能npc
							if(c.isVisible())
								l.add((Creature)o);
						}
					}
				}
				Packet pt = new Packet(OpCode.NPC_LIST_SERVER);
				pt.putInt(serial);
				pt.putShort(l.size());
				for(Creature c:l){
					pt.putInt(c.id);
					pt.putString(c.title);
					pt.putString(c.subTitle==null?"":c.subTitle);
					pt.putShort(c.x);
					pt.putShort(c.y);
				}
				p.send(pt);
			}
		}
	}
	
	protected void playerRate(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new PlayerRateCall(session,packet));
	}
	
	protected void nationSkillDesc(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new NationSkillDescCall(session,packet));
	}

	public void nationSkillList(Packet packet, ClientSession session) {
		Server.server.getServiceRegistry().getDbService().schedule(new NationSkillListCall(session,packet));
	}

	protected void enhanceSkills(Packet packet, ClientSession session) {
		Server.server.getServiceRegistry().getDbService().schedule(new NationSkillStudyCall(session,packet));
//		Player p = (Player) session.getClient();
//		int serial = packet.getInt();
//		if (p != null) {
//			int id = packet.getInt();
//			byte type = packet.get();
//			Packet pt = new Packet(OpCode.NATION_SKILL_STUDY_SERVER);
//			pt.putInt(serial);
//			List<NationSkill> skills = new ArrayList<NationSkill>();
//			try {
//				skills = new NationSkills().enhanceSkill(p, id, type);
//			} catch (NationSkillsException e) {
//				e.printStackTrace();
//			}
//			pt.putInt(serial);
//			for (NationSkill ski : skills) {
//				pt.putInt(id);
//				pt.putInt(ski.getLevel());
//				pt.putString(ski.getName());
//			}
//			p.send(pt);
//		} else {
//			ErrorHandler.sendErrorMessage(session, serial,
//					OpCode.NATION_SKILL_STUDY_CLIENT, "不是官员，没有权限");
//		}
	}

	protected void nationQuest(Packet packet, ClientSession session) {
		Server.server.getServiceRegistry().getDbService().schedule(
				new NationQuestCall(session, packet));
	}

	protected void nationQuestRequest(Packet packet, ClientSession session) {
		Server.server.getServiceRegistry().getDbService().schedule(
				new NationQuestRequestCall(session, packet));
	}

	protected void marriageInfo(Packet packet, ClientSession session) {
		Server.server.getServiceRegistry().getDbService().schedule(
				new MarriageInfoCall(session, packet));
	}

	protected void bbsList(Packet packet, ClientSession session) {
		Server.server.getServiceRegistry().getDbService().schedule(
				new BbsListCall(session, packet));
	}

	protected void bbsContent(Packet packet, ClientSession session) {
		Server.server.getServiceRegistry().getDbService().schedule(
				new BbsContentCall(session, packet));
	}

	protected void nationRel(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		if (p != null) {
			int serial = packet.getInt();
			NationService service = Server.server.getServiceRegistry()
					.getNationService();
			if (service.isKing(p)) {
				Packet pt = new Packet(OpCode.NATION_REL_SERVER);
				pt.putInt(serial);
				for (int i = 1; i < 4; i++) {
					Nation nation = service.getNationByFaction(i);
					pt.putInt(i);
					Officer king = nation.getOfficer(Officer.KING);
					pt.putInt(king == null ? 0 : king.actor.id);
					pt.putString(king == null ? "" : king.actor.name);
					pt.putInt(0);
				}
				p.send(pt);
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.NATION_REL_CLIENT, peony.Messages.STRING_00178);
			}
		}
	}

	protected void nationDeclare(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		if (p != null) {
			int serial = packet.getInt();
			int faction = packet.getInt();
			int type = packet.get();
			if (faction == p.faction) {
				return;
			}
			try {
				Date now = new Date();
				Date end = Time.getDateNextDay(now);
				NationService service = Server.server.getServiceRegistry()
						.getNationService();
				if (type == NationRel.TYPE_WAR_REQUEST) {
					if (!service.isKing(p)) {
						throw new NationDeclareException(peony.Messages.STRING_00179);
					}
					log.info("[NATIONBATTLEDECLARE]"
							+ LogUtil.getPlayerLogString(p) + "TRY");
					service.declareWar(p.faction, faction, now, end);
					log.info("[NATIONBATTLEDECLARE]"
							+ LogUtil.getPlayerLogString(p) + "OK");
					Packet pt = new Packet(OpCode.NATION_DECLARE_SERVER);
					pt.putInt(serial);
					session.send(pt);
					Nation nation = service.getNationByFaction(faction);
					int kingId = nation.getKingId();
					if (kingId != -1) {
						Player destKing = ObjectAccessor.getPlayer(kingId);
						if (destKing != null) {
							destKing.message(-1, 
									MessageFormat.format(peony.Messages.STRING_00180, 
											GameObject.getFactionName(p.faction)), -1, -1);
						}
					}
					Server.server.getServiceRegistry().getChatService()
							.sendFactionSystemMessage(
									p.faction,
									MessageFormat.format(peony.Messages.STRING_00181, 
											GameObject.getFactionName(faction)));
				} else if (type == NationRel.TYPE_SNEAK_REQUEST) {
					if (!service.isKing(p)){
						throw new NationDeclareException(peony.Messages.STRING_00179);
					}
					log.info("[SNEAKDECLARE]"
							+ LogUtil.getPlayerLogString(p) + "TRY");
					service.declareSneak(p.faction, faction);
					log.info("[SNEAKDECLARE]"
							+ LogUtil.getPlayerLogString(p) + "OK");
					Packet pt = new Packet(OpCode.NATION_DECLARE_SERVER);
					pt.putInt(serial);
					session.send(pt);
					Nation nation = service.getNationByFaction(faction);
					int kingId = nation.getKingId();
					if (kingId != -1) {
						Player destKing = ObjectAccessor.getPlayer(kingId);
						if (destKing != null) {
							destKing.message(-1, 
									MessageFormat.format(peony.Messages.STRING_00182,
									GameObject.getFactionName(p.faction)), 
									-1, -1);
						}
					}
					Server.server
							.getServiceRegistry()
							.getChatService()
							.sendFactionSystemMessage(
									p.faction,
									MessageFormat.format(peony.Messages.STRING_00183, 
											GameObject.getFactionName(faction)));
					Server.server
							.getServiceRegistry()
							.getChatService()
							.sendFactionSystemMessage(
									faction,
									MessageFormat.format(peony.Messages.STRING_00184, 
											GameObject.getFactionName(p.faction)));
				} else {
					throw new NationDeclareException(peony.Messages.STRING_00185);
				}
			} catch (NationDeclareException e) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.NATION_DECLARE_CLIENT, e.getMessage());
			}
		}
	}

	protected void nationDeclareAccept(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		if (p != null) {
			int serial = packet.getInt();
			int faction = packet.getInt();
			int type = packet.get();
			int v = packet.get();
			boolean accepted = false;
			if (v == 1)
				accepted = true;
			NationService service = Server.server.getServiceRegistry()
					.getNationService();
			if (service.isKing(p)) {
				if (type == 3) {
					if (accepted) {
						try {
							log.info("[NATIONBATTLEACCEPT]"
									+ LogUtil.getPlayerLogString(p) + "TRY");
							service.acceptWar(p.faction, faction);
							log.info("[NATIONBATTLEACCEPT]"
									+ LogUtil.getPlayerLogString(p) + "OK");
							Packet pt = new Packet(
									OpCode.NATION_DECLARE_ACCEPT_SERVER);
							pt.putInt(serial);
							p.send(pt);
						} catch (NationDeclareException e) {
							ErrorHandler.sendErrorMessage(session, serial,
									OpCode.NATION_DECLARE_ACCEPT_CLIENT, e
											.getMessage());
						}
					} else {
						try {
							log.info("[NATIONBATTLEREFUSE]"
									+ LogUtil.getPlayerLogString(p) + "TRY");
							service.refuseWar(p.faction, faction);
							log.info("[NATIONBATTLEREFUSE]"
									+ LogUtil.getPlayerLogString(p) + "TRY");
							Packet pt = new Packet(
									OpCode.NATION_DECLARE_ACCEPT_SERVER);
							pt.putInt(serial);
							p.send(pt);
						} catch (NationDeclareException e) {
							ErrorHandler.sendErrorMessage(session, serial,
									OpCode.NATION_DECLARE_ACCEPT_CLIENT, e
											.getMessage());
						}
					}
				} else {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.NATION_DECLARE_ACCEPT_CLIENT, peony.Messages.STRING_00186);
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.NATION_DECLARE_ACCEPT_CLIENT, peony.Messages.STRING_00187);
			}
		}
	}

	protected void nationDeclareList(Packet packet, ClientSession session) {
		Server.server.getServiceRegistry().getDbService().schedule(new NationDeclareListCall(session,packet));
	}

	protected void nationBattleFieldTele(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		// packet.getInt();
		int type = packet.getInt();
		int direction = packet.get();
		if (type == 1) {
			if (direction >= 1 && direction <= 4) { //国战
				if (player != null) {
					try {
						NationBattleFieldInstance instance = Server.server
								.getServiceRegistry().getNationService()
								.battleFieldSignup(player,
										type == 1 ? true : false);
						NationBattleFieldDef def = instance.getDef();
						player.goMap(def.mapId, def.doors[direction - 1][0],
								def.doors[direction - 1][1]);
					} catch (NationBattleFieldSignupException e) {
						ErrorHandler.sendErrorMessage(session, -1,
								OpCode.NATION_BATTLE_TELE_CLIENT, e
										.getMessage());
					} catch (VMapException e) {
						ErrorHandler.sendErrorMessage(session, -1,
								OpCode.NATION_BATTLE_TELE_CLIENT, e
										.getMessage());
					}
				}
			}
		}else if(type == 2){//反击战
			if (player != null) {
				try {
					NationSneakBattleFieldInstance instance = Server.server
							.getServiceRegistry().getNationService()
							.sneakSignup(player,
									type == 3 ? true : false);
					NationSneakBattleFieldDef def = instance.getDef();
					int[] in = def.getInPoint(player.faction);
					player.goMap(def.mapId, in[0], in[1]);
				} catch (NationBattleFieldSignupException e) {
					ErrorHandler.sendErrorMessage(session, -1,
							OpCode.NATION_BATTLE_TELE_CLIENT, e
									.getMessage());
				} catch (VMapException e) {
					ErrorHandler.sendErrorMessage(session, -1,
							OpCode.NATION_BATTLE_TELE_CLIENT, e
									.getMessage());
				}
			}
		}
	}

	protected void accountBinding(Packet packet, ClientSession session) {
		Server.server.getServiceRegistry().getDbService().schedule(
				new AccountBindCall(session, packet));
	}

	protected void accountBindingStatus(Packet packet, ClientSession session) {
		Server.server.getServiceRegistry().getDbService().schedule(
				new AccountBindStatusCall(session, packet));
	}

	/**
	 * 大臣一览表
	 */
	protected void officerList(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		NationService nationService = Server.server.getServiceRegistry()
				.getNationService();
		nationService.officerList(p, packet.getInt());
	}

	protected void nationOfficer(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		if (p != null) {
			OfficerCall call = new OfficerCall(packet, session);
			Server.server.getServiceRegistry().getDbService().schedule(call);
		}
	}

	protected void nationForbid(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		if (p != null) {
			Server.server.getServiceRegistry().getDbService().schedule(
					new ForbidCall(packet, session));
		}
	}

	protected void nationPunish(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		if (p != null) {
			Server.server.getServiceRegistry().getDbService().schedule(
					new PunishCall(packet, session));
		}
	}

	/**
	 * 捐献战功
	 */
	public void contributeCredit(Packet packet, ClientSession session) {
		ContributeCreditCall call = new ContributeCreditCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	/**
	 * 国王发起募捐
	 */
	protected void collectLaunch(Packet packet, ClientSession session) {
		CandidateService candidateService = Server.server.getServiceRegistry()
				.getCandidateService();
		Player p = (Player) session.getClient();
		if (p == null)
			return;
		try {
			candidateService.collect(p);
		} catch (NationVoteException e) {
			p.message(-1, e.getMessage(), -1, -1);
		}
	}

	/**
	 * 公民募捐
	 */
	protected void collect(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		if (p == null)
			return;
		CollectCall call = new CollectCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	/**
	 * 候选人列表
	 */
	protected void candidateList(Packet packet, ClientSession session) {
		Server.server.getServiceRegistry().getDbService().schedule(
				new CandidateListCall(session, packet));
	}

	/**
	 * 选举国王投票
	 */
	protected void vote(Packet packet, ClientSession session) {
		VoteCall call = new VoteCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	/**
	 * 竞选国王报名
	 */
	protected void voteSignUp(Packet packet, ClientSession session) {
		CandidateSignUpCall candidateSignUpCall = new CandidateSignUpCall(
				session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(
				candidateSignUpCall);
	}

	protected void instanceClear(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			if (player.getVMap().instance == null) {
				Party p = player.party;
				List<Player> list = new ArrayList<Player>();
				list.add(player);
				int count = 0;
				if(p!=null && p.leader.player.id == player.id){
					List<PartyMember> members = p.members;
					if(members!=null){
						List<NormalInstance> l = Server.server.getServiceRegistry().getNormalVMapManager().getNormalInstance(player.id);
						if(l!=null){
							for(NormalInstance n : l){
								for(PartyMember pm : members){
									list.add(pm.player);
									if(pm.player.getVMap().instance == n){
										pm.player.message(-1, "队长正在重置副本，请离开副本。", -1, -1);
										count++;
									}
								}
							}
						}						
					}
				} else {
					
				}
				if(count == 0){
					try{
						List<NormalInstance> l = Server.server.getServiceRegistry().getNormalVMapManager().getNormalInstance(player.id);
						if(l!=null && l.size()>0){
							for(NormalInstance n : l){
								for(VMap map : n.maps.values()){
									if(map.getId() == FiveElementService.MAPS[1]){
										Server.server.getServiceRegistry().getFiveElementService().removeFiveElement(n);
									}
								}
							}
						}
					}catch(Exception e){
						
					}
					Server.server.getServiceRegistry().getNormalVMapManager()
					.clear(player.id);
					if(player.party!=null && player.party.members!=null){
						for(PartyMember pm : player.party.members){
							Server.server.getServiceRegistry().getNormalVMapManager()
							.clear(pm.player.id);
						}
					}
				}else{
					player.message(-1, "有队员在副本中，暂无法清除副本。", -1, -1);
					return;
				}
				Packet pt = new Packet(OpCode.INSTANCE_CLEAR_SERVER);
				pt.putInt(serial);
				pt.put(list.size());
				for(Player pm : list){
					pm.send(pt);
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.INSTANCE_CLEAR_CLIENT, peony.Messages.STRING_00188);
			}
		}
	}

	protected void topKillCount(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			List<PvpInfo> l = Server.server.getServiceRegistry()
					.getStatService().topPvpInfos(player.faction);
			Packet pt = new Packet(OpCode.TOPLIST_KILLCOUNT_SERVER);
			pt.putInt(serial);
			pt.putShort(l.size());
			for (PvpInfo pvpInfo : l) {
				pt.putString(pvpInfo.actor.name);
				pt.putInt(pvpInfo.yesterdayKillCount);
			}
			session.send(pt);
		}
	}

	protected void topWeekRank(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			List<Actor> l = Server.server.getServiceRegistry().getStatService()
					.topWeekRanks(player.faction);
			Packet pt = new Packet(OpCode.TOPLIST_WEEKRANK_SERVER);
			pt.putInt(serial);
			pt.putShort(l.size());
			for (Actor a : l) {
				pt.putString(a.name);
				pt.putString(CreditUtil.getCreditString(a.rank));
			}
			session.send(pt);
		}
	}

	protected void nationInfo(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			int faction = packet.getInt();
			Nation nation = Server.server.getServiceRegistry()
					.getNationService().getNationByFaction(faction);
			Packet pt = new Packet(OpCode.NATION_INFO_SERVER);
			pt.putInt(serial);
			pt.putString(nation.getName());
			if (nation.slogan == null) {
				pt.putString(peony.Messages.STRING_00189);
			} else {
				pt.putString(nation.slogan);
			}
			pt.putInt(nation.getWinTimes());
			pt.putInt(nation.getFailTimes());
			pt.putInt(nation.getKingId());
			try {
				pt.putString(nation.getKingName());
			} catch (Exception e) {
				pt.putString("");
			}
			String moneyStr=getLongString(nation.money);
			pt.putUTF(moneyStr);
			pt.putInt(nation.power);
			int taxRate = 0;
			Nation winNation = Server.server.getServiceRegistry()
					.getNationService().getWinNation(player.faction);
			if (winNation == null) {
				taxRate = (int) (100 * nation.taxRate);
			} else {
				taxRate = (int) ((100 * nation.taxRate) + (100 * Nation.FAILURE_TAX)); //战败国被多征5%的税
			}
			pt.putInt(taxRate);
			//国家押运
			NationConvoyService convoyService = Server.server.getServiceRegistry().getNationConvoyService();
			pt.put(nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_STATE,0)!=0?1:0);
			pt.putInt(convoyService.getIndex(nation.pool.getInt(Nation.PROPERTY_NATIONCONVOY_DATE,0)));
			player.send(pt);
		}
	}
	
	public String getLongString(long src){
		StringBuffer sb=new StringBuffer();
		if(src>=0&&src<10000){
			sb.append(src);
		}else if(src>=10000&&src<100000000){
			sb.append(src/10000).append("万");
		}else if(src>=100000000){
			sb.append(src/100000000).append("亿");
		}
		return sb.toString();
	}

	protected void nationSlogan(Packet packet, ClientSession session) {
		NationSloganCall call = new NationSloganCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	protected void chinaJoyCount(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		Player p = (Player) session.getClient();
		if (p != null) {
			Packet pt = new Packet(OpCode.CHINAJOY_COUNT_SERVER);
			pt.putInt(serial);
			pt.putInt(Server.server.getServiceRegistry().getChinaJoyService()
					.getCount(p.accountId));
			p.send(pt);
		}
	}

	protected void chinaJoyGift(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		Player p = (Player) session.getClient();
		if (p != null) {
			log.info("[CHINAJOY]" + LogUtil.getPlayerLogString(p) + "TRY");
			if (Server.server.getServiceRegistry().getChinaJoyService()
					.addVote(p.accountId)) {
				GameItem item = ObjectAccessor
						.createGameItem(ItemUtil.ITEM_CHINAJOY);
				PlayerTransaction tx = p.newTransaction("GFT");
				Gain gain = new Gain(p);
				gain.addGainItem(item, 1);
				try {
					p.addGainComplete(gain, tx, true);
					tx.commit();
					Packet pt = new Packet(OpCode.ACTIVATIONCODE_SERVER);
					pt.put(serial);
					p.send(pt);
					log.info("[CHINAJOY]" + LogUtil.getPlayerLogString(p)
							+ "BAG");
				} catch (Exception e) {
					tx.rollback();
					Server.server.getServiceRegistry().getMailService()
							.sendSystemMailAsync(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_00190, "", 0, item,
									1, "CHINAJOY");
					log.info("[CHINAJOY]" + LogUtil.getPlayerLogString(p)
							+ "MAIL");
				}
				Packet pt = new Packet(OpCode.CHINAJOY_GIFT_SERVER);
				pt.putInt(serial);
				p.send(pt);
			} else {
				log.info("[CHINAJOY]" + LogUtil.getPlayerLogString(p) + "FAIL");
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.CHINAJOY_GIFT_CLIENT, peony.Messages.STRING_00191);
			}
		}
	}

	/**
	 * 判断是否已经申请过仓库
	 */
	public void isOn(Packet packet, ClientSession session) {
		DepotService depotService = Server.server.getServiceRegistry()
				.getDepotService();
		Player p = (Player) session.getClient();
		if (p != null) {
			Packet pt = new Packet(OpCode.DEPOT_YESORNO_SERVER);
			pt.putInt(packet.getInt());
			if (p.depot != null && p.depot.getGrids().size() == 0 
					|| p.depot==null) {
				pt.putInt(0);
			} else {
				pt.putInt(1);
				pt.put(p.depot.getSize());
				for (TransactionBagGrid grid : p.depot.getGrids()) {
					pt.put(grid.toClientByte());
				}
				depotService.flushDepot(p);
			}
			p.send(pt);
		}
	}

	/**
	 * 仓库申请
	 */
	protected void depotrequesthandle(Packet packet, ClientSession session) {
		DepotService depotService = Server.server.getServiceRegistry()
				.getDepotService();
		int serial = packet.getInt();
		Player p = (Player) session.getClient();
		try {
			depotService.turnOn(p, serial);
		} catch (DepotException e) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.DEPOT_REQUEST_CLIENT, e.getMessage());
		}
	}

	/**
	 * 从背包中取出物品放入仓库
	 */
	public void getItemFromBagToDepot(Packet packet, ClientSession session) {
		DepotService depotService = Server.server.getServiceRegistry()
				.getDepotService();
		Player p = (Player) session.getClient();
		int serial = packet.getInt();
		int gridId = packet.getInt();
		int itemId = packet.getInt();
		int instanceId = packet.getInt();
		int count = packet.getInt();
		try {
			depotService.getItemFromBagToDepot(p, session, serial, gridId,
					itemId, instanceId, count);
		} catch (DepotException e) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.DEPOT_GETFROMBAG_CLIENT, e.getMessage());
		}
	}

	/**
	 * 从仓库中取出物品放入背包
	 */
	public void getItemFromDepotToBag(Packet packet, ClientSession session) {
		DepotService depotService = Server.server.getServiceRegistry()
				.getDepotService();
		Player p = (Player) session.getClient();
		int serial = packet.getInt();
		int gridId = packet.getInt();
		int itemId = packet.getInt();
		int instanceId = packet.getInt();
		int count = packet.getInt();
		try {
			depotService.getItemFromDepotToBag(p, session, serial, gridId,
					itemId, instanceId, count);
		} catch (DepotException e) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.DEPOT_GETFROMDEPOT_CIENT, e.getMessage());
		}
	}

	/**
	 * 仓库整理
	 */
	protected void depotArrange(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			if (player.depot.arrange()) {
				Packet pt = new Packet(OpCode.DEPOT_ARRANGE_SERVER);
				pt.putInt(serial);
				pt.put(player.depot.getSize());
				for (TransactionBagGrid grid : player.depot.grids) {
					pt.put(grid.toClientByte());
				}
				player.send(pt);
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.DEPOT_ARRANGE_SERVER, peony.Messages.STRING_00192);
			}
		}
	}
	
	/**
	 * 生成新的装备星级（等待玩家确认是否替换）
	 */
	protected void createStarEnhance(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int itemId = packet.getInt();
			int instanceId = packet.getInt();
			int type = packet.get();
			Object[] os = ItemUtil.findPlayerEquipment(player, itemId,
					instanceId);
			if (os != null) {
				GameItem item = (GameItem) os[0];
				if (!item.template.isEquipment()) {
					return;
				}
				if (!item.template.equipment.canJudgeStar) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.STARENHANCE_CONFIRE_CLIENT, peony.Messages.STRING_00193);
					return;
				}
				if (item.object != null) {//已经鉴定过
					ItemEnhance ie = (ItemEnhance) item.object;
					if(type != 4){
						if (ie != null && ie.getStar() != 0) {
							ErrorHandler.sendErrorMessage(session, serial,
									OpCode.STARENHANCE_CONFIRE_CLIENT, peony.Messages.STRING_00194);
							return;
						}
					}
				}
				int fuId = -1;
				if (type == 1) {
					fuId = ItemUtil.ITEM_STAR_ENHANCE_LEVEL1;
				} else if (type == 2) {
					fuId = ItemUtil.ITEM_STAR_ENHANCE_LEVEL2;
				} else if (type == 3) {
					fuId = ItemUtil.ITEM_STAR_ENHANCE_LEVEL3;
				} else if( type == 4){
					fuId = ItemUtil.ITEM_STAR_ENHANCE_LEVEL4;
				}
				PlayerTransaction tx = player.newTransaction("STE");
				if (fuId != -1) {
					if (player.bag.removeGameItem(fuId, -1, 1, tx, true) == null) {
						tx.rollback();
						ItemTemplate it = ObjectAccessor.getItemTemplate(fuId);
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.STARENHANCE_CONFIRE_CLIENT, 
								MessageFormat.format(peony.Messages.STRING_00195, it.name));
						return;
					}
				}
				// 记录日志
				LogUtil.logStarTry(player, item, type);
				int price = item.template.level * item.template.level;
				if (type != 0)
					price /= 2;
				try {
					player.decMoney(price, tx, true);
					tx.commit();
                    GameItem _item = ObjectAccessor.createGameItem(item.template, -1);
                    if (item.object == null)
        		        item.object = new ItemEnhance();
					if(item.object!=null){
						_item.object = ((ItemEnhance)item.object).clone();
					}
					int star = ItemUtil.startEnhance(_item, type, player);
					Packet pt = new Packet(OpCode.STARENHANCE_CONFIRE_SERVER);
					pt.putInt(serial);
					pt.put(_item.toClientBytes());
					player.send(pt);
					
					if(_item.object!=null){
						item.object = ((ItemEnhance)_item.object).clone();
						Object owner = os[1];
						if (owner instanceof Player) {
							player.refreshProperties(false);
						} else if (owner instanceof Horse) {
							Horse h = (Horse) owner;
							h.refreshProperties(false, player);
							if (h == player.horse) {
								player.refreshProperties(false);
							}
						} else if(owner instanceof Attendant){
							((Attendant) owner).refreshProperties(false);
						}
						player.addAction(Action.START);
						if(star == 8){
	                    	String msg2 = MessageFormat.format("八星/-1出世！{0}将之拿在手中，睥睨了一下四周人群。",player.name);
	                		ItemChatAttachment attItem = new ItemChatAttachment(item);
	                		ChatMessage cm = new ChatMessage(ChatOption.FACTION,player.id,player.faction,peony.Messages.STRING_00004,player.faction,
	                				msg2,attItem);
	                		Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
	                    }else if(star == 9){
	                    	String msg2 = MessageFormat.format("九星/-1出世！{0}淡定的数着星星，数到九后放声狂笑，顿时惊动世界。", player.name);
	                		ItemChatAttachment attItem = new ItemChatAttachment(item);
	                    	ChatMessage cm = new ChatMessage(ChatOption.WORLD, player.id, -1,peony.Messages.STRING_00004, msg2, attItem);
	                		Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
	                    }
						// 记录日志
						LogUtil.logStarOK(player, item, type);
						
						Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_ENHANCE,player,1));
						player.refreshStarState();
						player.refreshHorseStarState();
					}
				}catch (NoEnoughValueException e) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.STARENHANCE_CONFIRE_CLIENT, peony.Messages.STRING_00196);
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.STARENHANCE_CONFIRE_CLIENT, peony.Messages.STRING_00173);
			}
		}
	}

	/**
	 * 对装备进行星级鉴定 serial int itemId int instanceId int type byte(0 无星级鉴定符 1
	 * 低级星级鉴定符 2 高级星级鉴定符 3 顶级星级鉴定符)
	 */
	protected void startEnhance(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int itemId = packet.getInt();
			int instanceId = packet.getInt();
			int type = packet.get();
			Object[] os = ItemUtil.findPlayerEquipment(player, itemId,
					instanceId);
			if (os != null) {
				GameItem item = (GameItem) os[0];
//				if (!item.template.isEquipment()) {
//					return;
//				}
//				if (!item.template.equipment.canJudgeStar) {
//					ErrorHandler.sendErrorMessage(session, serial,
//							OpCode.NATURAL_ENHANCE_CLIENT, peony.Messages.STRING_00193);
//					return;
//				}
//				boolean check = false;
//				if (item.object != null) {//已经鉴定过
//					ItemEnhance ie = (ItemEnhance) item.object;
//					if(type != 4){
//						if (ie != null && ie.getStar() != 0) {
//							ErrorHandler.sendErrorMessage(session, serial,
//									OpCode.NATURAL_ENHANCE_CLIENT, peony.Messages.STRING_00194);
//							return;
//						}
//					}else{
//						if (ie != null && ie.getStar() != 0) {
//							check = true;
//						}
//					}
//				}
//				int fuId = -1;
//				if (type == 1) {
//					fuId = ItemUtil.ITEM_STAR_ENHANCE_LEVEL1;
//				} else if (type == 2) {
//					fuId = ItemUtil.ITEM_STAR_ENHANCE_LEVEL2;
//				} else if (type == 3) {
//					fuId = ItemUtil.ITEM_STAR_ENHANCE_LEVEL3;
//				} else if( type == 4){
//					fuId = ItemUtil.ITEM_STAR_ENHANCE_LEVEL4;
//				}
//				if(!check){
//					PlayerTransaction tx = player.newTransaction("STE");
//					if (fuId != -1) {
//						if (player.bag.removeGameItem(fuId, -1, 1, tx, true) == null) {
//							tx.rollback();
//							ItemTemplate it = ObjectAccessor.getItemTemplate(fuId);
//							ErrorHandler.sendErrorMessage(session, serial,
//									OpCode.NATURAL_ENHANCE_CLIENT, 
//									MessageFormat.format(peony.Messages.STRING_00195, it.name));
//							return;
//						}
//					}
//					
//					// 记录日志
//					LogUtil.logStarTry(player, item, type);
//					
//					int price = item.template.level * item.template.level;
//					if (type != 0)
//						price /= 2;
//					try {
//						player.decMoney(price, tx, true);
//						tx.commit();
//						int star = ItemUtil.startEnhance(item, type, player);
//						Object owner = os[1];
//						if (owner instanceof Player) {
//							player.refreshProperties(false);
//						} else if (owner instanceof Horse) {
//							Horse h = (Horse) owner;
//							h.refreshProperties(false, player);
//							if (h == player.horse) {
//								player.refreshProperties(false);
//							}
//						} else if(owner instanceof Attendant){
//							((Attendant) owner).refreshProperties(false);
//						}
//						player.addAction(Action.START);
//	                    if(star == 8){
//	                    	String msg2 = MessageFormat.format("八星/-1出世！{0}将之拿在手中，睥睨了一下四周人群。",player.name);
//	                		ItemChatAttachment attItem = new ItemChatAttachment(item);
//	                		ChatMessage cm = new ChatMessage(ChatOption.FACTION,player.id,player.faction,peony.Messages.STRING_00004,player.faction,
//	                				msg2,attItem);
//	                		Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
//	                    }else if(star == 9){
//	                    	String msg2 = MessageFormat.format("九星/-1出世！{0}淡定的数着星星，数到九后放声狂笑，顿时惊动世界。", player.name);
//	                		ItemChatAttachment attItem = new ItemChatAttachment(item);
//	                    	ChatMessage cm = new ChatMessage(ChatOption.WORLD, player.id, -1,peony.Messages.STRING_00004, msg2, attItem);
//	                		Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
//	                    }
//						// 记录日志
//						LogUtil.logStarOK(player, item, type);
						
						Packet pt = new Packet(OpCode.STAR_ENHANCE_SERVER);
						pt.putInt(serial);
						pt.put(item.toClientBytes());
						player.send(pt);
//						Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_ENHANCE,player,1));
//						player.refreshStarState();
//						player.refreshHorseStarState();
//					} catch (NoEnoughValueException e) {
//						tx.rollback();
//						ErrorHandler.sendErrorMessage(session, serial,
//								OpCode.NATURAL_ENHANCE_CLIENT, peony.Messages.STRING_00196);
//					}
//				}else{
//					Packet pt = new Packet(OpCode.STAR_ENHANCE_SERVER);
//					pt.putInt(serial);
//					pt.put(item.toClientBytes());
//					player.send(pt);
//				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.STAR_ENHANCE_CLIENT, peony.Messages.STRING_00173);
			}
		}
	}

	
	/**
	 * 生成新的装备资质（等待玩家确认是否替换）
	 */
	protected void creatNaturalEnhance(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int itemId = packet.getInt();
			int instanceId = packet.getInt();
			Object[] os = ItemUtil.findPlayerEquipment(player, itemId,
					instanceId);
			if (os != null) {
				GameItem item = (GameItem) os[0];
				if (!item.template.isEquipment()) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.EVALUATION_CONFIRE_CLIENT, peony.Messages.STRING_00170);
					return;
				}
				if (!item.template.equipment.canJudgePotential) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.EVALUATION_CONFIRE_CLIENT, peony.Messages.STRING_00171);
					return;
				}
				PlayerTransaction tx = player.newTransaction("MNE");
				GameItem gameItem = player.bag.removeGameItem(ItemUtil.ITEM_NATURAL_ENHANCE,
						-1, 1, tx, true);
				if (gameItem == null) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.EVALUATION_CONFIRE_CLIENT,
							peony.Messages.STRING_00197);
					return;
				}
				
				// 记录日志
				LogUtil.logNaturalEnhanceTry(player, item);
				
				int price = item.template.level * item.template.level / 8;
				try {
					player.decMoney(price, tx, true);
					tx.commit();
					GameItem _item = ObjectAccessor.createGameItem(item.template, -1);
					
					if(item.object!=null){
						_item.object = ((ItemEnhance)item.object).clone();
					}
					ItemUtil.naturalEnhance(_item,player);
					
					player.tempEnHance = ((ItemEnhance)_item.object).getNaturals();
//					Object owner = os[1];
//					if (owner instanceof Player) {
//						player.refreshProperties(false);
//					} else if (owner instanceof Horse) {
//						Horse h = (Horse) owner;
//						h.refreshProperties(false, player);
//						if (h == player.horse) {
//							player.refreshProperties(false);
//						}
//					} else if(owner instanceof Attendant){
//						((Attendant) owner).refreshProperties(false);
//					}
					//player.addAction(Action.NATURAL_ENHANCE);
					Packet pt = new Packet(OpCode.EVALUATION_CONFIRE_SERVER);
					pt.putInt(serial);
					pt.put(_item.toClientBytes());
					player.send(pt);
					//Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_ENHANCE,player,0));
					// 记录日志
					//LogUtil.logNaturalEnhanceOK(player, item);
				} catch (NoEnoughValueException e) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.NATURAL_ENHANCE_CLIENT, peony.Messages.STRING_00198);
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.NATURAL_ENHANCE_CLIENT, peony.Messages.STRING_00173);
			}
		}
	}
	
	/**
	 * 确认用新的资质，替换原来的装备资质
	 */
	protected void naturalEnhance(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int itemId = packet.getInt();
			int instanceId = packet.getInt();
			int isNatural = packet.getByte();
			Object[] os = ItemUtil.findPlayerEquipment(player, itemId,
					instanceId);
			if (os != null) {
				GameItem item = (GameItem) os[0];
				if (!item.template.isEquipment()) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.NATURAL_ENHANCE_CLIENT, peony.Messages.STRING_00170);
					return;
				}
				if (!item.template.equipment.canJudgePotential) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.NATURAL_ENHANCE_CLIENT, peony.Messages.STRING_00171);
					return;
				}
				
				if(isNatural == 1){	//重新鉴定
					PlayerTransaction tx = player.newTransaction("MNE");
					GameItem gameItem = player.bag.removeGameItem(ItemUtil.ITEM_NATURAL_ENHANCE,
							-1, 1, tx, true);
					if (gameItem == null) {
						tx.rollback();
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.NATURAL_ENHANCE_CLIENT,
								peony.Messages.STRING_00197);
						return;
					}
					// 记录日志
					LogUtil.logNaturalEnhanceTry(player, item);
					
					int price = item.template.level * item.template.level / 8;
					try {
						player.decMoney(price, tx, true);
						tx.commit();
						ItemUtil.naturalEnhance(item,player);
						
						Object owner = os[1];
						if (owner instanceof Player) {
							player.refreshProperties(false);
						} else if (owner instanceof Horse) {
							Horse h = (Horse) owner;
							h.refreshProperties(false, player);
							if (h == player.horse) {
								player.refreshProperties(false);
							}
						} else if(owner instanceof Attendant){
							((Attendant) owner).refreshProperties(false);
						}
						player.addAction(Action.NATURAL_ENHANCE);
						Packet pt = new Packet(OpCode.NATURAL_ENHANCE_SERVER);
						pt.putInt(serial);
						pt.put(item.toClientBytes());
						player.send(pt);
						Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_ENHANCE,player,0));
						// 记录日志
						LogUtil.logNaturalEnhanceOK(player, item);
					} catch (NoEnoughValueException e) {
						tx.rollback();
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.NATURAL_ENHANCE_CLIENT, peony.Messages.STRING_00198);
					}
					
				}else{
					if(player.tempEnHance == null){
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.NATURAL_ENHANCE_CLIENT, "您还没有进行资质鉴定");
						return;
					}
					
					ItemEnhance ie = null;
					if (item.object != null) {
						ie = (ItemEnhance) item.object;
					}else {
						ie = new ItemEnhance();
						item.object = ie;
					}
					ie.setNaturals(player.tempEnHance);
					player.tempEnHance = null;
					
					Object owner = os[1];
					if (owner instanceof Player) {
						player.refreshProperties(false);
					} else if (owner instanceof Horse) {
						Horse h = (Horse) owner;
						h.refreshProperties(false, player);
						if (h == player.horse) {
							player.refreshProperties(false);
						}
					} else if(owner instanceof Attendant){
						((Attendant) owner).refreshProperties(false);
					}
					player.addAction(Action.NATURAL_ENHANCE);
					Packet pt = new Packet(OpCode.NATURAL_ENHANCE_SERVER);
					pt.putInt(serial);
					pt.put(item.toClientBytes());
					player.send(pt);
					Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_ENHANCE,player,0));
					// 记录日志
					LogUtil.logNaturalEnhanceOK(player, item);
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.NATURAL_ENHANCE_CLIENT, peony.Messages.STRING_00173);
			}
		}
	}

	protected void naturalEnhanceMoney(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int itemId = packet.getInt();
			int instanceId = packet.getInt();
			Object[] os = ItemUtil.findPlayerEquipment(player, itemId,
					instanceId);
			if (os != null) {
				GameItem item = (GameItem) os[0];
				int price = -1;
				if (item.template.isEquipment()
						&& item.template.equipment.canJudgePotential) {
					price = item.template.level * item.template.level / 8;
				}
				Packet pt = new Packet(OpCode.NATURAL_ENHANCE_MONEY_SERVER);
				pt.putInt(serial);
				pt.putInt(price);
				player.send(pt);
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.NATURAL_ENHANCE_MONEY_CLIENT, peony.Messages.STRING_00199);
			}
		}
	}

	protected void startEnhanceMoney(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int itemId = packet.getInt();
			int instanceId = packet.getInt();
			Object[] os = ItemUtil.findPlayerEquipment(player, itemId,
					instanceId);
			int price1 = -1;
			int price2 = -1;
			int price3 = -1;
			int price4 = -1;
			int price5 = -1;
			if (os != null) {
				GameItem item = (GameItem) os[0];
				//星级鉴定信息
				if (item.template.isEquipment()
						&& item.template.equipment.canJudgeStar) {
					price1 = item.template.level * item.template.level;
					price2 = price1 / 2;
					price3 = price1 / 2;
					price4 = price1 / 2;
					price5 = price1 / 2;
				}
				//资质鉴定信息
				int price = -1;
				if (item.template.isEquipment()
						&& item.template.equipment.canJudgePotential) {
					price = item.template.level * item.template.level / 8;
				}
				//查询商品价格(资质鉴定符，星级鉴定符等)
				if(GetJewelConfigCall.ITEM_NATURAL_ENHANCE_PRICE == 0){
					GetJewelConfigCall.checkConfigPacket();
				}
				//鉴定符价格
				Packet pt = new Packet(OpCode.START_ENHANCE_MONEY_SERVER);
				pt.putInt(serial);
				pt.putInt(price1);
				pt.putInt(price2);
				pt.putInt(price3);
				pt.putInt(price4);
				pt.putInt(price5);
				pt.putInt(price);
				pt.putInt(GetJewelConfigCall.ITEM_STAR_ENHANCE_LEVEL1_PRICE);
				pt.putInt(GetJewelConfigCall.ITEM_STAR_ENHANCE_LEVEL2_PRICE);
				pt.putInt(GetJewelConfigCall.ITEM_STAR_ENHANCE_LEVEL3_PRICE);
				pt.putInt(GetJewelConfigCall.ITEM_NATURAL_ENHANCE_PRICE);
				player.send(pt);
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.START_ENHANCE_MONEY_CLIENT, peony.Messages.STRING_00199);
			}
		}
	}

	/**
	 * 驿站传送
	 */
	protected void teleport(Packet packet, ClientSession session) {
		TeleportService teleportService = Server.server.getServiceRegistry()
				.getTeleportService();
		teleportService.teleport(packet, session);
	}

	/**
	 * 驿站列表
	 */
	protected void teleportList(Packet packet, ClientSession session) {
		TeleportService teleportService = Server.server.getServiceRegistry()
				.getTeleportService();
		teleportService.getTeleportList(packet, session);
	}

	protected void bagArrange(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			if (player.bag.arrange()) {
				Packet pt = new Packet(OpCode.BAG_ARRANGE_SERVER);
				pt.putInt(serial);
				pt.put(player.bag.getSize());
				for (TransactionBagGrid grid : player.bag.grids) {
					pt.put(grid.toClientByte());
				}
				player.send(pt);
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.BAG_ARRANGE_CLIENT, peony.Messages.STRING_00200);
			}
		}
	}

	protected void activationCode(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			Server.server.getServiceRegistry().getDbService().schedule(
					new ActivationCodeCall(session, packet, player));
		}
	}

	protected void ishopList(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		Packet pt = new Packet(OpCode.ISHOP_LIST_SERVER);
		pt.putInt(serial);
		if (Server.REVISION_TYPE_CMCC.equals(Server.server.revision)|| Server.REVISION_TYPE_TEL.equals(Server.server.revision)) {
			pt.putString("47,49,48,50,51");
		}else if(Server.REVISION_TYPE_TW.equals(Server.server.revision)){
			pt.putString("56,249,54,52,168,53,55");
		}else if(Server.REVISION_TYPE_KO.equals(Server.server.revision)){
			pt.putString("186,139,140,141,142,187,189");
		}else if(Server.REVISION_TYPE_JAPAN.equals(Server.server.revision)){
			pt.putString("184,180,181,182,183,188,190");
		}else {
			pt.putString("21,248,20,18,167,19,17");
		}
		session.send(pt);
	}

	/**
	 * 配方书详细信息
	 */
	protected void formulaIndex(Packet packet, ClientSession session) {
		ProduceService produceService = Server.server.getServiceRegistry()
				.getProduceService();
		produceService.formulaIndex(packet, session);
	}

	/**
	 * 打造
	 */
	protected void produce(Packet packet, ClientSession session) {
		ProduceService produceService = Server.server.getServiceRegistry()
				.getProduceService();
		Player p = (Player) session.getClient();
		int serial = packet.getInt();
		int formulaId = packet.getInt();
		if (p != null) {
			produceService.produce(formulaId, p, session, serial);
		}
	}

	/**
	 * 配方列表
	 */
	protected void formulaList(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		ProduceService produceService = Server.server.getServiceRegistry().getProduceService();
		if (p != null) {
			int serial = packet.getInt();
			Packet packet1 = new Packet(OpCode.FORMULA_LIST_SERVER);
			packet1.putInt(serial);
			int playerGatherLevel = ProduceService.getPracticeLevel(p.level,
					p.pool.getInt(Player.PROPERTY_GATHER_ABILITY));
			int playerProduceLevel = ProduceService.getPracticeLevel(p.level,
					p.pool.getInt(Player.PROPERTY_PRODUCE_ABILITY));
			packet1.putShort(p.pool.getInt(Player.PROPERTY_GATHER_ABILITY, 1));
			packet1.putString(ProduceService
					.getLevelToString(playerGatherLevel));
			packet1.putShort(p.pool.getInt(Player.PROPERTY_PRODUCE_ABILITY, 1));
			packet1.putString(ProduceService
					.getLevelToString(playerProduceLevel));
			List<List> tmp = new ArrayList<List>();
			for(int i=0;i<5;i++){
				tmp.add(new ArrayList<Formula>());
			}
			ProjectData pd = Server.server.getServiceRegistry().getDataService().data;
			for(int formulaId : p.formulaList.ids){
				Formula fml = (Formula) pd.findObject(Formula.class, formulaId);
				int type = produceService.getTypeOfFormula(fml.itemID);
				List<Formula> list = tmp.get(type);
				produceService.binaryAdd(list, fml);
			}
			for (List<Formula> list : tmp) {
				packet1.putShort(list.size());
				for (Formula fml : list) {
					packet1.putInt(fml.id);
					packet1.putString(fml.title);
					packet1.putString(ProduceService
							.getLevelToString(fml.level));
				}
			}
			p.send(packet1);
		}
	}

	/**
	 * 删除配方
	 */
	protected void deleteFormula(Packet packet, ClientSession session) {
		ProduceService.deleteFormula(packet, session);
	}

	protected void chinarun(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			Account a = (Account) session.getIdentity();
			Server.server.getServiceRegistry().getDbService().schedule(
					new ChinarunCall(session, packet, player, a.getChannel()));
		}
	}

	protected void mergeJewel(Packet packet, ClientSession session) {
		MergeJewelCall call = new MergeJewelCall(session, packet);
		call.callFinish();
	}

	protected void getJewelConfig(Packet packet, ClientSession session) {
		GetJewelConfigCall call = new GetJewelConfigCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	protected void addJewel(Packet packet, ClientSession session) {
		AddJewelCall call = new AddJewelCall(session, packet);
		call.callFinish();
	}

	protected void removeJewel(Packet packet, ClientSession session) {
		RemoveJewelCall call = new RemoveJewelCall(session, packet);
		call.callFinish();
	}

	protected void addHole(Packet packet, ClientSession session) {
		AddHoleCall call = new AddHoleCall(session, packet);
		call.callFinish();
	}

	/**
	 * 原来极限打孔逻辑(废弃)
	 * @param packet
	 * @param session
	 */
	protected void addMaxHole(Packet packet, ClientSession session) {
		AddMaxHoleCall call = new AddMaxHoleCall(session, packet);
		call.callFinish();
	}
	
	/**
	 * 极限打孔
	 * @param packet
	 * @param session
	 */
	protected void addAllMaxHole(Packet packet, ClientSession session) {
		AddMaxHoleAllCall call = new AddMaxHoleAllCall(session, packet);
		call.callFinish();
	}

	protected void skillRefreshMoney(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int times = player.pool.getInt(Player.PROPERTY_REFRESH_SKILL);
			int money = PlayerUtil.getRefreshSkillMoney(times);
			Packet pt = new Packet(OpCode.SKILL_REFRESH_MONEY_SERVER);
			pt.putInt(serial);
			pt.putInt(money);
			player.send(pt);
		}
	}

	/**
	 * 坐骑领悟的技能，新技能信息
	 */
	protected void horseSkillConfire(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int horseInstanceId = packet.getInt();
			Horse h = player.horseBag.getHorse(horseInstanceId);
			if (h == null) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_CHANGE_SKILL_CLIENT, peony.Messages.STRING_00201);
				return;
			}
			
			PlayerTransaction tx = player.newTransaction("HSK");
			if (player.bag.removeGameItem(HorseUtil.CHANGE_SKILL_ITEMID, -1, 1,
					tx, true) != null) {
				int[] s = new int[h.skills.size()];
				for (int j = 0; j < s.length; j++) {
					s[j] = h.skills.get(j).getGroupId();
				}
				Skill newSkill = HorseUtil.getSkill(player.clazz, s);
				h.tempNewSkill = newSkill;
				h.removeSkillIndex = h.getRemoveSkillId(player, newSkill);
				
				if (h.removeSkillIndex == -1 || newSkill == null) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.HORSE_CHANGE_SKILL_CLIENT, peony.Messages.STRING_00202);
				} else {
					tx.commit();
					//出发使用坐骑遗忘书事件
					Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_USEITEM,player,HorseUtil.CHANGE_SKILL_ITEMID,1));
					Packet pt = new Packet(OpCode.HORSE_SKILL_CONFIRE_SERVER);
					pt.putInt(serial);
					pt.put(h.removeSkillIndex);
					pt.put(h.tempNewSkill.toClientBytes(player));
					pt.putString(SkillUtil.getSkillDesc(h.tempNewSkill, player));
					player.send(pt);
				}
			}else {
				tx.rollback();
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_CHANGE_SKILL_CLIENT, peony.Messages.STRING_00203);
			}
		}
	}
	
	protected void horseChangeSkill(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int horseInstanceId = packet.getInt();
			// int skillId = packet.getInt();
			Horse h = player.horseBag.getHorse(horseInstanceId);
			if (h == null) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_CHANGE_SKILL_CLIENT, peony.Messages.STRING_00201);
				return;
			}
			Skill skill = h.removeSkill(player, h.tempNewSkill, h.removeSkillIndex);
			h.removeSkillIndex = -1;
			h.tempNewSkill = null;
			if (skill != null) {
				Packet pt = new Packet(OpCode.HORSE_CHANGE_SKILL_SERVER);
				pt.putInt(serial);
				player.send(pt);
				String str = LogUtil.getHorseString(h);
				// 记录日志
				LogUtil.logHorseChangeSkill(player, str, h);
				
				// 记录玩家动作
				player.addAction(Action.REFRESH_HORSE_SKILL);
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_CHANGE_SKILL_CLIENT, peony.Messages.STRING_00202);
				return;
			}
		}
	}
	
	protected void lockHorseSkill(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int horseInstanceId = packet.getInt();
			int skillIndex = packet.getInt();
			Horse h = p.horseBag.getHorse(horseInstanceId);
			if(h==null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.LOCK_HORSESKILL_CLIENT, peony.Messages.STRING_00201);
				return;
			}
			int lockSkillCount = HorseUtil.getLockSkillCount(h);
			if(lockSkillCount>=HorseUtil.HORSE_LOCKSKILL_COUNT){
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_CHANGE_SKILL_CLIENT, peony.Messages.STRING_00204);
				return;
			}
			PlayerTransaction tx = p.newTransaction("HLK");
			GameItem item = p.bag.removeGameItem(HorseUtil.HORSE_SKILL_LOCK, -1, 1, tx, false);
			if (item!= null) {
				if(((h.lockSkillId>>skillIndex)&1)==1){
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.HORSE_CHANGE_SKILL_CLIENT, peony.Messages.STRING_00205);
					return;
				}
				tx.commit();
				h.lockSkillId |= (1 << skillIndex);
				h.addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_AGENT, (h.agentHorse<<7) | h.lockSkillId, false);
				//统计锁定坐骑技能成就
				StatService statService = Server.server.getServiceRegistry().getStatService();
				statService.playerLockHorseSkill(p);
				LogUtil.logHorseLockSkillOK(p, h);
			} else {
				tx.rollback();
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_CHANGE_SKILL_CLIENT, 
						MessageFormat.format(peony.Messages.STRING_00097, 
								ObjectAccessor.getItemTemplate(HorseUtil.HORSE_SKILL_LOCK).name));
				return;
			}
			Packet pt = new Packet(OpCode.LOCK_HORSESKILL_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}
	}
	
	protected void unlockHorseSkill(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int horseInstanceId = packet.getInt();
			int skillIndex = packet.getInt();
			Horse h = p.horseBag.getHorse(horseInstanceId);
			if(h==null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.UNLOCK_HORSESKILL_CLIENT, peony.Messages.STRING_00201);
				return;
			}
			if(((h.lockSkillId>>skillIndex)&1)==0)
				return;
			PlayerTransaction tx = p.newTransaction("UHS");
			try {
				p.decMoney(2000, tx, true);
				tx.commit();
			} catch (NoEnoughValueException e) {
				tx.rollback();
				ErrorHandler.sendErrorMessage(session, serial, OpCode.UNLOCK_HORSESKILL_CLIENT, peony.Messages.STRING_00129);
				return;
			}
			h.lockSkillId = h.lockSkillId & ~(1<<skillIndex);
			h.addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_AGENT, (h.agentHorse<<7) | h.lockSkillId, false);
			Packet pt = new Packet(OpCode.UNLOCK_HORSESKILL_SERVER);
			pt.putInt(serial);
			p.send(pt);
			
			LogUtil.logHorseUnlockSkillOK(p, h);
		}
	}

	protected void battleFieldQuit(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			FlagBattleFieldInstance instance = (FlagBattleFieldInstance) player.map.map.instance;
			if (instance != null) {
				instance.quit(player);
			}
		}
	}

	protected void battleFieldTran(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			byte type = packet.get();
			FlagBattleFieldVMapManager manager = Server.server
			.getServiceRegistry().getFlagBattleFieldVMapManager();
			if(type == 0){
				manager.tran(player);
			} else if(type == 1) {
				manager.unTran(player);
			}
		}
	}

	protected void battleFieldSignUp(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int battleFieldId = packet.getInt();
			FlagBattleFieldVMapManager manager = Server.server
					.getServiceRegistry().getFlagBattleFieldVMapManager();
			manager.signup(player, serial);
		}
	}

	/**
	 * 发起结婚申请
	 */
	protected void marriageRequest(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		if (p != null) {
			MarriageService marriageService = Server.server
					.getServiceRegistry().getMarriageService();
			int annotherPersonId = packet.getInt();
			Packet packet1 = new Packet(OpCode.MARRIAGE_SERVER);
			String message = "";
			if (marriageService.isCouple(p.id, annotherPersonId) == p.id) {
				message = peony.Messages.STRING_00206;
			} else {
				if (annotherPersonId == -1) {
					if (p.party == null) {
						message = peony.Messages.STRING_00207;
					} else {
						synchronized (p.party) {
							List<PartyMember> list = p.party.members;
							if (list.size() == 2) {
								for (PartyMember member : list) {
									if (member != null && member.getId() != p.id) {
										annotherPersonId = member.getId();
									}
								}
								if (marriageService
										.isCouple(p.id, annotherPersonId) == annotherPersonId) {
									message = peony.Messages.STRING_00208;
								} else {
									Player annotherPerson = ObjectAccessor
											.getPlayer(annotherPersonId);
									if (annotherPerson != null) {
										if (p.sex != annotherPerson.sex) {
											if (p.level >= 10) {
												if (annotherPerson.level >= 10) {
													if (marriageService.isFriend(
															p.id, annotherPersonId) == 2) {
														if (p.money >= MarriageService.MARRY_MONEY) {
															if (p.map.id == annotherPerson.map.id) {
																Packet packet2 = new Packet(
																		OpCode.MARRIAGE_REQUEST_SERVER);
																packet2
																		.putInt(p.id);
																packet2
																		.putString(p.name);
																annotherPerson
																		.send(packet2);
															} else {
																message = peony.Messages.STRING_00209;
															}
														} else {
															message = MessageFormat.format(peony.Messages.STRING_00210, MarriageService.MARRY_MONEY);
														}
													} else if (marriageService
															.isFriend(p.id,
																	annotherPersonId) == 0) {
														message = peony.Messages.STRING_00211;
													} else if (marriageService
															.isFriend(p.id,
																	annotherPersonId) == 1) {
														message = peony.Messages.STRING_00212;
													} else if (marriageService
															.isFriend(p.id,
																	annotherPersonId) == -1) {
														message = peony.Messages.STRING_00213;
													}
												} else {
													message = peony.Messages.STRING_00214;
												}
											} else {
												message = peony.Messages.STRING_00215;
											}
										} else {
											message = peony.Messages.STRING_00216;
										}
									}
								}
							} else {
								message = peony.Messages.STRING_00217;
							}
						}
					}
				}
			}
			if (message != "") {
				packet1.putString(message);
				p.send(packet1);
			}
		}
	}

	/**
	 * 结婚邀请答复
	 */
	protected void marriageAnswer(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		if (p != null) {
			int personId = packet.getInt();
			int answer = packet.getInt();
			Player person = ObjectAccessor.getPlayer(personId);
			if (answer == 0) {
				Packet packet1 = new Packet(OpCode.MARRIAGE_SERVER);
				packet1.putString( MessageFormat.format(peony.Messages.STRING_00218, p.name));
				person.send(packet1);
			} else if (answer == 1) {
				if (person != null) {
					MarriageCall marriageCall = new MarriageCall(session,
							personId, p.id);
					Server.server.getServiceRegistry().getDbService().schedule(
							marriageCall);
				}
			}
		}
	}

	/**
	 * 离婚申请
	 */
	public void divorce(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		if (p != null) {
			RelationService relationService = Server.server
					.getServiceRegistry().getRelationService();
			int type = packet.getInt();
			int annotherPersonId = -1;
			Packet packet1 = new Packet(OpCode.MARRIAGE_DIVORCE_SERVER);
			String message = "";
			PlayerRelation relation = null;
			if (relationService.get(p.id) != null) {
				relation = relationService.get(p.id);
			} else {
				relation = Server.server.getServiceRegistry().getDbService().playerRelationDAO
						.findPlayerRelation(p.id);
			}
			if (relation.mateId != -1) {
				if (type == 0) {
					if (p.party == null) {
						message = peony.Messages.STRING_00219;
					} else {
						synchronized (p.party) {
							List<PartyMember> list = p.party.members;
							if (list.size() == 2) {
								for (PartyMember member : list) {
									if (member != null && member.getId() != p.id) {
										annotherPersonId = member.getId();
									}
								}
							} else {
								message = peony.Messages.STRING_00220;
							}
						}
						Player annotherPerson = ObjectAccessor
								.getPlayer(annotherPersonId);
						if (annotherPerson != null) {
							int mate = -1;
							if (relationService.get(annotherPersonId) != null) {
								mate = relationService.get(annotherPersonId).mateId;
							} else {
								mate = Server.server.getServiceRegistry()
										.getDbService().playerRelationDAO
										.findPlayerRelation(annotherPersonId).mateId;
							}
							if (mate != -1 && mate == p.id) {
								if (p.map.id == annotherPerson.map.id) {
									if(p.money>=20000){
										if(p.honor>=500){
											if(annotherPerson.money >= 20000){
												if(annotherPerson.honor >= 500){
													Packet packet2 = new Packet(
															OpCode.MARRIAGE_DIVORCEREQUEST_SERVER);
													packet2.putInt(p.id);
													packet2.putString(p.name);
													annotherPerson.send(packet2);
												    return;
												} else {
													message = peony.Messages.STRING_00221;
											    }
											} else {
												message = peony.Messages.STRING_00222;
											}
										} else {
											message = peony.Messages.STRING_00223;
										}
								    } else {
									    message = peony.Messages.STRING_00224;
								}
							}else {
									message = peony.Messages.STRING_00225;
								}
							} else {
								message = peony.Messages.STRING_00226;
							}
						}
					}
				} else if (type == 1) {
					annotherPersonId = relation.mateId;
					GameItem gameItem = p.bag.getGameItem(644);
					if (gameItem == null) {
						message = peony.Messages.STRING_00227;
					} else {
						int instanceId = gameItem.instanceId;
						DivorceCall divorceCall = new DivorceCall(session,
								p.id, annotherPersonId, 0, instanceId, type);
						Server.server.getServiceRegistry().getDbService()
								.schedule(divorceCall);
						return;
					}
				}
			} else if (relation.mateId == -1) {
				message = peony.Messages.STRING_00228;
			}
			packet1.putString(message);
			p.send(packet1);
		}
	}

	/**
	 * 离婚协商回复
	 */
	public void divorceAnswer(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		if (p != null) {
			int personId = packet.getInt();
			int answer = packet.getInt();
			if (answer == 1) {
				DivorceCall divorceCall = new DivorceCall(session, personId,
						p.id, 0, 0, 0);
				Server.server.getServiceRegistry().getDbService().schedule(
						divorceCall);
			} else if (answer == 0) {
				Player player = ObjectAccessor.getPlayer(personId);
				if (player != null) {
					Packet packet1 = new Packet(OpCode.MARRIAGE_DIVORCE_SERVER);
					packet1.putString(peony.Messages.STRING_00229);
					player.send(packet1);
				}
			}
		}
	}

	protected void suiteIndex(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		if (p != null) {
			int serial = packet.getInt();
			int itemId = packet.getInt();
			int itemInstanceId = packet.getInt();
			int type = packet.getByte();
			int instanceId = packet.getInt();
			int horseInstanceId = 0;
			try {
				horseInstanceId = packet.getInt();
			} catch (Exception e) {
				
			}
			if(type>=5&&type<=7){
				Server.server.getServiceRegistry().getDbService().schedule(new AsyncSuiteIndexInfoCall(session, 
						serial, itemId, itemInstanceId, type, instanceId, horseInstanceId));
			}else{
				p.suiteIndex(serial, itemId, itemInstanceId, type, instanceId, horseInstanceId);
			}
		}
	}

	protected void titleSalary(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			if (player.titles.getCurrentTitle() != null) {
				if (player.titles.getCurrentTitle().type != Title.TYPE_OFFICIAL) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.TITLE_SALARY_CLIENT, peony.Messages.STRING_00230);
					return;
				}
				if (player.titles.lastSalaryTime == 0
						|| !DateUtils.isSameDay(new Date(
								player.titles.lastSalaryTime), new Date())) {
					player.titles.setLastSalaryTime(System.currentTimeMillis());
					PlayerTransaction tx = player.newTransaction("SAL");
					player.addMoney(player.titles.getCurrentTitle().salary, tx,
							true);
					tx.commit();
					Packet pt = new Packet(OpCode.TITLE_REMOVE_SERVER);
					pt.putInt(serial);
					player.send(pt);
				} else {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.TITLE_SALARY_CLIENT, peony.Messages.STRING_00231);
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.TITLE_SALARY_CLIENT, peony.Messages.STRING_00232);
			}
		}
	}

	protected void horsePacket(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
 			int serial = packet.getInt();
			int horseInstanceId = packet.getInt();
			if (player.horse != null
					&& player.horse.instanceId == horseInstanceId) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_PACK_CLIENT, peony.Messages.STRING_00233);
				return;
			}
			Horse h = player.horseBag.getHorse(horseInstanceId);
			if (h == null) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_PACK_CLIENT, peony.Messages.STRING_00235);
				return;
			}
			
			if(HorseActiveCall.getActiveItemByHorseId(h.itemId)!=-1 || HorseActiveCall.isIn(Horse.autoRideHorse, h.itemId)){
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_PACK_CLIENT, peony.Messages.STRING_00234);
				return;
			}
			
			if (h.agentHorse==1){
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_PACK_CLIENT, peony.Messages.STRING_00236);
				return;
			}
			if (!h.equs.isEmpty()) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_PACK_CLIENT, peony.Messages.STRING_00237);
				return;
			}
			
			if(h.instanceId == player.failHorseInst){
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_PACK_CLIENT, "合成失败的坐骑不能放入背包");
				return;
			}
			
			//ItemTemplate t = ObjectAccessor.getItemTemplate(h.itemId);
//			if (t.bindType == ItemTemplate.BIND_REWARD
//					|| t.bindType == ItemTemplate.BIND_USED) {
//				ErrorHandler.sendErrorMessage(session, serial,
//						OpCode.HORSE_PACK_CLIENT, "坐骑已经被绑定，不能交易");
//				return;
//			}
			GameItem item = ObjectAccessor.createGameItem(h.itemId);
			
			if(h.imageIdChange >= 0 && h.itemIdChange >= 0){
				GameItem itemChange = ObjectAccessor.createGameItem(h.itemIdChange);
				if(itemChange.bindInstance >= 0 || item.bindInstance >= 0 ){
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.HORSE_PACK_CLIENT, peony.Messages.STRING_00234);
					return;
				}
			}
//			if (h.imageIdChange >= 0 && h.itemIdChange >= 0){
//				item = ObjectAccessor.createGameItem2(h.itemId);
//				GameItem itemChange = ObjectAccessor.createGameItem(h.itemIdChange);
//				if(itemChange.bindInstance >= 0){
//					item.bindInstance = itemChange.bindInstance;
//					item.template.bindType = itemChange.template.bindType;
//				}
//				item.template.name = MessageFormat.format("{0}\n[幻化]{1}", item.template.name, itemChange.template.name);
//			}else{
//				item = ObjectAccessor.createGameItem(h.itemId);
//			}
			item.object = h;
			
			PlayerTransaction tx = player.newTransaction("HPK");
			Gain gain = new Gain(player);
			gain.addGainItem(item, 1);
			try {
				player.addGainComplete(gain, tx, true);
				player.horseBag.removeHorse(horseInstanceId);
				tx.commit();
				
				// 记录日志
				LogUtil.logThrowHorse(player, h, "HPK");
				LogUtil.logHorsePack(player, h);

				Packet pt = new Packet(OpCode.HORSE_PACK_SERVER);
				pt.putInt(serial);
				player.send(pt);
			} catch (NoEnoughSpaceException e) {
				tx.rollback();
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_PACK_CLIENT, peony.Messages.STRING_00238);
				return;
			}
		}

	}

	protected void gatherCancel(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			player.gather = null;
		}
	}

	protected void horseUnride(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			player.horseUnride(serial);
		}
	}

	protected void horseFood(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int itemId = packet.getInt();
			int horseInstanceId = packet.getInt();
			if (itemId != ItemUtil.ITEM_HORSEFOOD
					&& itemId != ItemUtil.ITEM_HORSEFOOD_ADDBUFF
					&& itemId != ItemUtil.ITEM_HORSEFOODS
					&& itemId != -1)
				return;
			Horse h = player.horseBag.getHorse(horseInstanceId);
			if (h != null) {
				h.food(itemId, player, serial);
			}
		}
	}

	protected void horseFeed(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int gridId = packet.getInt();
			int itemId = packet.getInt();
			int instanceId = packet.getInt();
			int horseInstanceId = packet.getInt();
			player.horseFeed(gridId, itemId, instanceId, horseInstanceId,
					serial);
		}
	}

	protected void horseRide(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int horseInstanceId = packet.getInt();
			int change = packet.getInt();
			player.horseRide(horseInstanceId, serial,change);
		}
	}

	protected void horseThrow(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int horseInstanceId = packet.getInt();
			ExpService expService = Server.server.getServiceRegistry().getExpService();
			boolean isAgent = expService.isAgentHorse(player, horseInstanceId);
			if(isAgent){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_THROW_CLIENT, peony.Messages.STRING_00239);
				return;
			}
			try {
				player.horseBag.throwHorse(horseInstanceId, serial);
			} catch (Exception e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_THROW_CLIENT, e.getMessage());
			}
		}
	}

	protected void horseChangeName(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int horseInstanceId = packet.getInt();
			String name = packet.getString().trim();
			if (name.getBytes().length > 8 || name.getBytes().length < 1) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_CHANGENAME_CLIENT, peony.Messages.STRING_00240);
				return;
			}
			if (StringUtil.isValidText(name) != IStringValidator.OK) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_CHANGENAME_CLIENT, peony.Messages.STRING_00241);
				return;
			}
			if (StringUtil.isValidName(name) != IStringValidator.OK) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_CHANGENAME_CLIENT, peony.Messages.STRING_00241);
				return;
			}
			Horse h = player.horseBag.getHorse(horseInstanceId);
			if (h != null) {
				h.changeName(name, player, serial);
			}
		}
	}

	// protected void horseAddPoint(Packet packet, ClientSession session) {
	// Player player = (Player) session.getClient();
	// if (player != null) {
	// int serial = packet.getInt();
	// int horseInstanceId = packet.getInt();
	// int strength = packet.getShort();
	// int agility = packet.getShort();
	// int stamina = packet.getShort();
	// int intellect = packet.getShort();
	// int speed = packet.getShort();
	// if (strength < 0 || agility < 0 || stamina < 0 || intellect < 0
	// || speed < 0) {
	// log.info("[ADDHORSEPOINTERROR]"
	// + LogUtil.getPlayerLogString(player));
	// return;
	// }
	// Horse h = player.horseBag.getHorse(horseInstanceId);
	// if (h == null) {
	// ErrorHandler.sendErrorMessage(session, serial,
	// OpCode.HORSE_ADDPOINT_CLIENT, "没知道指定坐骑");
	// return;
	// }
	// if ((strength + agility + stamina + intellect + speed) > h.point) {
	// log.info("[ADDHORSEPOINTERROR]"
	// + LogUtil.getPlayerLogString(player));
	// return;
	//
	// }
	// h.strengthAdded += strength;
	// h.agilityAdded += agility;
	// h.staminaAdded += stamina;
	// h.intellectAdded += intellect;
	// h.speedAdded += speed;
	// h.refreshProperties(false, player);
	// if (player.horse == h) {
	// player.refreshProperties(false);
	// }
	// Packet pt = new Packet(OpCode.HORSE_ADDPOINT_SERVER);
	// pt.putInt(serial);
	// session.send(pt);
	// }
	// }

	protected void horseBag(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			Packet pt = new Packet(OpCode.HORSE_BAG_SERVER);
			pt.putInt(serial);
			pt.put(player.horseBag.toClientBytes());
			session.send(pt);
		}
	}

	protected void horseUnequ(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int itemId = packet.getInt();
			int instanceId = packet.getInt();
			int horseInstanceId = packet.getInt();
			player.horseUnequip(itemId, instanceId, serial, horseInstanceId);
		}
	}

	protected void horseEqu(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int itemId = packet.getInt();
			int instanceId = packet.getInt();
			int horseInstanceId = packet.getInt();
			player.horseEquip(itemId, instanceId, serial, horseInstanceId);
		}
	}

	protected void config(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			Packet pt = new Packet(OpCode.CONFIG_SERVER);
			pt.putInt(serial);
			pt.put(player.config == null ? new byte[0] : player.config);
			session.send(pt);
		}
	}

	protected void configSave(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			byte[] data = packet.getBytes();
			player.config = data;
			Packet pt = new Packet(OpCode.CONFIG_SAVE_SERVER);
			pt.putInt(serial);
			session.send(pt);
			Account acc = player.getAccount();
			if(acc != null){
				String uimodel = acc.getUiModel();
				if(uimodel != null && uimodel.startsWith("NewUI_")){//新界面
					LogUtil.logSystemConfig(player, data);
				}
			}
			
		}
	}

	protected void alphaGift(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			Server.server.getServiceRegistry().getDbService().schedule(
					new AlphaGiftGetCall(session, packet, player));
		}
	}

	protected void giftGet(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			Server.server.getServiceRegistry().getDbService().schedule(
					new GiftGetCall(session, packet, player));
		}
	}

	protected void giftList(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int groupId = packet.getInt();
			GiftGroup g = Server.server.getServiceRegistry().getGiftService()
					.getGiftGroup(groupId);
			if (g == null) {
				log.info("[GIFTERROR]" + LogUtil.getPlayerLogString(player)
						+ "GROUP[" + groupId + "]");
				return;
			}
			if (g.isValid(new Date())) {
				GiftDef[] defs = g.findGifts(player.level);
				Packet pt = new Packet(OpCode.GIFT_LIST_SERVER);
				pt.putInt(serial);
				pt.putInt(groupId);
				pt.putShort(defs.length);
				for (GiftDef def : defs) {
					pt.putInt(def.id);
					pt.putString(g.translateText(g.giftMessage, def, 0, 0));
				}
				session.send(pt);
			}
		}
	}

	/**
	 * auctionBuy(Packet packet, ClientSession session)
	 * 
	 * @param packet
	 * @param session
	 */
	protected void auctionBuy(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		// player.getAuctionPrice();
		if (player != null) {
			AuctionBuyCall call = new AuctionBuyCall(session, packet, player);
			Server.server.getServiceRegistry().getDbService().schedule(call);
		}
	}

	/**
	 * auctionCreate(Packet packet,ClientSession session)
	 * 
	 * @param packet
	 * @param session
	 */
	protected void auctionCreate(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			AuctionCreateCall call = new AuctionCreateCall(session, packet,
					player);
			Server.server.getServiceRegistry().getDbService().schedule(call);
		}
	}

	/**
	 * auctionList(packet,session);
	 * 
	 * @param packet
	 * @param session
	 */
	protected void auctionList(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			AuctionListCall call = new AuctionListCall(session, packet, player);
			Server.server.getServiceRegistry().getDbService().schedule(call);
		}
	}

	protected void publishiedAuctions(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			PublishiedCall call = new PublishiedCall

			(session, packet, player);
			Server.server.getServiceRegistry

			().getDbService().schedule(call);
		}
	}

	public void close(ClientSession session) {
		session.close();
	}

	protected void logout(Packet packet, ClientSession session) {
		logout0(session);
		Player player = (Player) session.getClient();
		if (player != null) {
			player.logouted = true;
		}
	}

	public void logout0(ClientSession session) {
		close(session);
	}

	protected void titleList(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int type = packet.get();
			List<Title> titles = null;
			if (type == Title.TYPE_OFFICIAL) {
				titles = TitleUtil.getNeturalTitles();
			} else if (type == Title.TYPE_OTHER) {
				titles = TitleUtil.getOtherTitles();
			} else if (type == Title.TYPE_COUNTRY) {
				titles = TitleUtil.getCountryTitles(player.faction);
			}
			if (titles != null) {
				Packet pt = new Packet(OpCode.TITLE_LIST_SERVER);
				pt.putInt(serial);
				pt.putShort(titles.size());
				for (Title t : titles) {
					pt.put(t.toClientBytes());
					if (player.titles.hasTitle(t.id)) {
						pt.put(1);
					} else {
						pt.put(0);
					}
				}
				player.send(pt);
			}
		}
	}

	protected void titleBuy(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int titleId = packet.getShort();
			
			LogUtil.logTitleBuyTry(player, titleId);
			
			if (player.titles.hasTitle(titleId)) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.TITLE_BUY_CLIENT, peony.Messages.STRING_00242);
				return;
			}
			Title t = TitleUtil.getTitle(titleId);
			if (t.type == Title.TYPE_COUNTRY && t.faction != player.faction)
				return;
			if (player.level < t.level) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.TITLE_BUY_CLIENT, 
						MessageFormat.format(peony.Messages.STRING_00243, t.level));
				return;
			}
			if (t.price > 0) {
				PlayerTransaction tx = player.newTransaction("TBY");
				try {
					player.decHonor(t.price, tx, true);
					tx.commit();
				} catch (NoEnoughValueException e) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.TITLE_BUY_CLIENT, MessageFormat.format(peony.Messages.STRING_00244, t.price));
					return;
				}
			}
			player.addTitle(t);
			
			//增加title事件
			Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_ADD_TITLE,player));
			
			LogUtil.logTitleBuyOK(player, titleId);
			
			Packet pt = new Packet(OpCode.TITLE_BUY_SERVER);
			pt.putInt(serial);
			player.send(pt);
		}
	}

	protected void forgetSkill(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int skillId = packet.getInt();
			player.forgetSkill(serial, skillId);
		}
	}

	protected void titleRemove(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int titleId = packet.getShort();
			player.removeTitle(serial, titleId);
		}
	}

	protected void titleSet(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int titleId = packet.getShort();
			player.changeTitle(serial, titleId);
		}
	}

	protected void titlesGet(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			Packet pt = new Packet(OpCode.TITLES_SERVER);
			pt.putInt(serial);
			pt.put(player.titles.toClientBytes());
			session.send(pt);
		}
	}

	protected void exchangeAccept(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		Exchange ex = player.exchange;
		if (player != null && ex != null && ex.state == Exchange.STATE_STARTED) {
			packet.getInt();
			packet.getInt();
			int exchangeId = packet.getInt();
			if (ex.accept(player.id, exchangeId)) {
				int otherId = ex.getOtherId(player.id);
				Player p = ObjectAccessor.getPlayer(otherId);
				Gain g1 = new Gain(player);
				Gain g2 = new Gain(p);
				ex.restoreToGain(otherId, g1);
				ex.restoreToGain(player.id, g2);
				PlayerTransaction t1 = player.newTransaction("EXC");
				PlayerTransaction t2 = p.newTransaction("EXC");
				try {
					player.addGainComplete(g1, t1, false);
					p.addGainComplete(g2, t2, false);
					t1.commit();
					t2.commit();
					ex.complete(player, true);
					
					// 记录日志
					LogUtil.logExchangeOK(ex.id, player, g1, p, g2);
				} catch (NoEnoughSpaceException e) {
					t1.rollback();
					t2.rollback();
					ErrorHandler.sendErrorMessage(player.session, -1,
							OpCode.EXCHANGE_ACCEPT_CLIENT, peony.Messages.STRING_00245);
					ErrorHandler.sendErrorMessage(p.session, -1,
							OpCode.EXCHANGE_ACCEPT_CLIENT, peony.Messages.STRING_00245);
					ex.changeAllStatus();
				}
			}
		}
	}

	protected void exchangeRemoveItem(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		Exchange ex = player.exchange;
		if (player != null && ex != null && ex.state == Exchange.STATE_STARTED) {
			int targetId = (ex.targetRef.id==player.id ? ex.sourceRef.id : ex.targetRef.id);
			int targetState = (ex.targetRef.id==player.id ? ex.sourceStatus : ex.targetStatus);
			Player target = ObjectAccessor.getPlayer(targetId);
			int serial = packet.getInt();
			int gridId = packet.getInt();
			int count = packet.getInt();
			if (count <= 0) {
				log.error("[EXCHANGEERROR]OP[REMOVEITEM]"
						+ LogUtil.getPlayerLogString(player) + "COUNT[" + count
						+ "]");
				return;
			}
			if (gridId == -1) {
				if (ex.decMoney(player.id, count)) {
					PlayerTransaction tx = player.newTransaction("EXCC");
					player.addMoney(count, tx, false);
					tx.commit();
					
					if(target!=null && targetState==1){
						target.message(-1, peony.Messages.STRING_00246, -1, -1);
					}
					// 记录日志
					LogUtil.logExchangeRemoveItem(ex.id, player, count, null, 0);
				} else {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.EXCHANGE_REMOVEITEM_CLIENT, peony.Messages.STRING_00247);
				}
			} else {
				ExchangeGrid grid = ex.remove(player.id, gridId);
				if (grid != null && !grid.isEmpty()) {
					PlayerTransaction tx = player.newTransaction("EXCC");
					player.bag.addGameItem(grid.item, grid.count, tx, false);
					tx.commit();

					if(target!=null && targetState==1){
						target.message(-1, peony.Messages.STRING_00246, -1, -1);
					}
					// 记录日志
					LogUtil.logExchangeRemoveItem(ex.id, player, 0, grid.item, grid.count);
				} else {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.EXCHANGE_REMOVEITEM_CLIENT, peony.Messages.STRING_00248);
				}
			}
		}
	}

	protected void exchangeAddItem(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		Exchange ex = player.exchange;
		LevelLimitService service = Server.server
		.getServiceRegistry().getLevelLimitService();
		if (player != null && ex != null && ex.state == Exchange.STATE_STARTED) {
			int targetId = (ex.targetRef.id==player.id ? ex.sourceRef.id : ex.targetRef.id);
			int targetState = (ex.targetRef.id==player.id ? ex.sourceStatus : ex.targetStatus);
			Player target = ObjectAccessor.getPlayer(targetId);
			int serial = packet.getInt();
			int itemId = packet.getInt();
			int instanceId = packet.getInt();
			int count = packet.getInt();
			if (count <= 0) {
				log.error("[EXCHANGEERROR]OP[ADDITEM]"
						+ LogUtil.getPlayerLogString(player) + "COUNT[" + count
						+ "]");
				return;
			}
			if (itemId == -1) { // 金钱
				PlayerTransaction tx = player.newTransaction("EXC");
				try {
					player.decMoney(count, tx, false);
					if (ex.addMoney(player.id, count)) {
						tx.commit();
						
						if(target!=null && targetState==1){
							target.message(-1, peony.Messages.STRING_00246, -1, -1);
						}
						// 记录日志
						LogUtil.logExchangeAddItem(ex.id, player, count, null, 0);
					} else {
						tx.rollback();
					}
				} catch (NoEnoughValueException e) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.EXCHANGE_ADDITEM_CLIENT, peony.Messages.STRING_00249);
				}
			} else {
				PlayerTransaction tx = player.newTransaction("EXC");
				try {
					GameItem item = player.bag.removeGameItem(itemId,
							instanceId, count, tx, false);
					if (item == null) {
						tx.rollback();
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.EXCHANGE_ADDITEM_CLIENT,
								peony.Messages.STRING_00250);
						return;
					}
					if (player.level < 30 && service.check(itemId)) {
						tx.rollback();
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.EXCHANGE_ADDITEM_CLIENT,
								peony.Messages.STRING_00251);
						return;
					}
					if (item.isBound()) {
						tx.rollback();
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.EXCHANGE_ADDITEM_CLIENT, peony.Messages.STRING_00252);
						return;
					}
					if (ex.addGameItem(player.id, item, count) == false) {
						tx.rollback();
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.EXCHANGE_ADDITEM_CLIENT, peony.Messages.STRING_00253);
						return;
					}
					tx.commit();

					if(target!=null && targetState==1){
						target.message(-1, peony.Messages.STRING_00246, -1, -1);
					}
					// 记录日志
					LogUtil.logExchangeAddItem(ex.id, player, 0, item, count);
				} catch (Exception e) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.EXCHANGE_ADDITEM_CLIENT, peony.Messages.STRING_00254);
				}
			}
		}
	}

	/**
	 * 交易完成 交易Id 原因 byte(0 正常完成 1 取消)
	 */
	protected void exchangeCancel(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			Exchange exchange = player.exchange;
			if (exchange != null) {
				player.cancelExchange();
				
				// 记录日志
				LogUtil.logExchangeCancel(exchange.id, player);
			}
		}
	}

	protected void exchangeInvitRefuse(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int exchangeId = packet.getInt();
			Exchange ex = Server.server.getServiceRegistry()
					.getExchangeService().getExchange(exchangeId);
			if (ex != null && ex.state == Exchange.STATE_INIT) {
				if (ex.targetRef.id == player.id) {
					Server.server.getServiceRegistry().getExchangeService()
							.removeExchange(exchangeId);
					Player p = ObjectAccessor.getPlayer(ex.sourceRef.id);
					if (p != null) {
						p.exchange = null;
						p.exchangeRefuse(ex.serial, peony.Messages.STRING_00255);
					}
					
					// 记录日志
					LogUtil.logExchangeRefuse(ex.id, player);
				}
			}
		}
	}

	protected void exchangeInvitOk(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int exchangeId = packet.getInt();
			Exchange ex = Server.server.getServiceRegistry()
					.getExchangeService().getExchange(exchangeId);
			if (ex != null && ex.state == Exchange.STATE_INIT) {
				if (ex.targetRef.id == player.id) {
					player.exchange = ex;
					ex.state = Exchange.STATE_STARTED;
					Player p = ObjectAccessor.getPlayer(ex.sourceRef.id);
					if (p != null) {
						Packet pt = ex.getInfoPacket();
						player.send(pt);
						p.send(pt);
					}
					
					// 记录日志
					LogUtil.logExchangeAccept(ex.id, player);
				}
			}
		}
	}

	protected void exchangeInvit(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int targetId = packet.getInt();
			if(targetId == player.id){
				Server.server.getServiceRegistry().getPlayerService().mute(player.id, System.currentTimeMillis()+15*60*1000);
				return;
			}
			if (player.exchange != null) {
				player.exchangeRefuse(serial, peony.Messages.STRING_00256);
				return;
			}
			Player p = ObjectAccessor.getPlayer(targetId);
			if (p == null) {
				player.exchangeRefuse(serial, peony.Messages.STRING_00257);
				return;
			}
			PlayerRelation rel = Server.server.getServiceRegistry()
					.getRelationService().get(p.id);
			if (rel != null && rel.blackList.exists(player.id)) {
				player.exchangeRefuse(serial, peony.Messages.STRING_00258);
				return;
			}
			if (p.faction != player.faction) {
				player.exchangeRefuse(serial, peony.Messages.STRING_00259);
				return;
			}
			if (p.exchange != null) {
				player.exchangeRefuse(serial, peony.Messages.STRING_00260);
				return;
			}
			if (p.threats.getCount() != 0
					|| (p.pkInfo != null && p.pkInfo.state == PkInfo.STATE_STARTED)) {
				if (player.party == null || !player.party.contains(p.id)) { // 如果是队员，那么可以交易
					player.exchangeRefuse(serial, peony.Messages.STRING_00260);
					return;
				}
			}
			if(!p.isAllowTrade()){
				player.exchangeRefuse(serial, peony.Messages.STRING_00261);
				return;
			}
			Exchange ex = new Exchange(Server.server.getServiceRegistry()
					.getExchangeService(), serial, player.ref(), p.ref());
			player.exchange = ex;
			
			// 记录日志
			LogUtil.logExchangeCreate(ex.id, player, p);
			Packet pt = new Packet(OpCode.EXCHANGE_INVIT_SERVER);
			pt.putInt(ex.id);
			pt.putString(player.name);
			p.send(pt);
			
			// 记录临时关系
			Server.server.getEventManager().addEvent(
					new ServiceEvent(ServiceEvent.EVENT_INTERACT, player,
							p, PlayerRelation.INTERACT_TRADE));
		}
	}

	protected void findPath(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int mapId = packet.getShort();
			int x = packet.getShort();
			int y = packet.getShort();
			GameMapExit[] exites = Server.server.getServiceRegistry()
					.getDataService().data.getPathFinder().findPath(
					player.faction, player.map.id, player.x, player.y, mapId,
					x * 8, y * 8);
			if (exites == null || exites.length == 0) {
				log.error("[FINDPATHERROR]FROM[" + player.map.id + "," + player.x
						+ "," + player.y + "]TO[" + mapId + "," + x * 8 + ","
						+ y * 8 + "]");
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.FINDPATH_CLIENT, peony.Messages.STRING_00262);
			} else {
				Packet pt = new Packet(OpCode.FINDPATH_SERVER);
				pt.putInt(serial);
				pt.put(exites.length);
				for (GameMapExit exit : exites) {
					pt.putShort(exit.owner.getGlobalID());
					pt.putShort(exit.x);
					pt.putShort(exit.y);
				}
				player.send(pt);
			}
		}
	}

	protected void getMove(Packet packet, ClientSession session) {
		int instanceId = packet.getInt();
		Player p = (Player) ObjectAccessor.getPlayer(instanceId);
		if (p != null && p.systemState == Player.SYSTEMSTATE_READY) {
			Packet pt = p.getMovePacket(GameObject.MOVE_ALL);
			session.send(pt);
		}
	}

	protected void gmCall(Packet packet, ClientSession session) {
		Server.server.getServiceRegistry().getDbService().schedule(
				new GMCallCall(session, packet));
	}

	protected void gridExchange(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int sourceId = packet.getShort();
			int targetId = packet.getShort();
			player.exchangeGrid(sourceId, targetId, serial);
		}
	}

	protected void outPrison(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			player.outPrison();
		}
	}

	protected void changePassword(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		String oldPassword = packet.getString();
		String password = packet.getString();
		if (password.length() <= 0) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.CHANGE_PASSWORD_CLIENT, peony.Messages.STRING_00263);
		} else {
				Server.server.getServiceRegistry().getAccountService()
						.schedule(
								new AccountChangePasswordCall(session, serial,
										oldPassword, password));
		}
	}

	protected void changeFaction(Packet packet, ClientSession session)
			throws VMapException {
		int serial = packet.getInt();
		int faction = packet.getInt();
		if (faction >= GameObject.FACTION_WEI
				&& faction <= GameObject.FACTION_WU) {
			Player player = (Player) session.getClient();
			if (player != null && player.level < 6) {
				player.setFaction(faction, false, serial);
				if (player.faction == GameObject.FACTION_WEI) {
					player.pool.setString("leavecontry", PlayerUtil.CREATE_POINT[0][0] + "," + PlayerUtil.CREATE_POINT[0][1] + "," + PlayerUtil.CREATE_POINT[0][2]);
//					player.goMap(PlayerUtil.CREATE_POINT[0][0],
//							PlayerUtil.CREATE_POINT[0][1],
//							PlayerUtil.CREATE_POINT[0][2]);
//					// try {
//					// Server.server.getWorld().addPlayerToMap(player,
//					// PlayerUtil.CREATE_POINT[0][0],
//					// PlayerUtil.CREATE_POINT[0][1],
//					// PlayerUtil.CREATE_POINT[0][2], false);
//					// } catch (VMapException e) { // 不应该出现
//					// log.error(e, e);
//					// }
				} else if (player.faction == GameObject.FACTION_SHU) {
					player.pool.setString("leavecontry", PlayerUtil.CREATE_POINT[1][0] + "," + PlayerUtil.CREATE_POINT[1][1] + "," + PlayerUtil.CREATE_POINT[1][2]);
//					player.goMap(PlayerUtil.CREATE_POINT[1][0],
//							PlayerUtil.CREATE_POINT[1][1],
//							PlayerUtil.CREATE_POINT[1][2]);
//					// try {
//					// Server.server.getWorld().addPlayerToMap(player,
//					// PlayerUtil.CREATE_POINT[1][0],
//					// PlayerUtil.CREATE_POINT[1][1],
//					// PlayerUtil.CREATE_POINT[1][2], false);
//					// } catch (VMapException e) { // 不应该出现
//					// log.error(e, e);
//					// }
				} else if (player.faction == GameObject.FACTION_WU) {
					player.pool.setString("leavecontry", PlayerUtil.CREATE_POINT[2][0] + "," + PlayerUtil.CREATE_POINT[2][1] + "," + PlayerUtil.CREATE_POINT[2][2]);
//					player.goMap(PlayerUtil.CREATE_POINT[2][0],
//							PlayerUtil.CREATE_POINT[2][1],
//							PlayerUtil.CREATE_POINT[2][2]);
//					// try {
//					// Server.server.getWorld().addPlayerToMap(player,
//					// PlayerUtil.CREATE_POINT[2][0],
//					// PlayerUtil.CREATE_POINT[2][1],
//					// PlayerUtil.CREATE_POINT[2][2], false);
//					// } catch (VMapException e) { // 不应该出现
//					// log.error(e, e);
//					// }
				}
				// Packet pt = new Packet(OpCode.FORCE_GOMAP_SERVER);
				// pt.putInt(player.map.id);
				// pt.putInt(player.x);
				// pt.putInt(player.y);
				// player.send(pt);
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.CHANGE_CLASS_CLIENT, peony.Messages.STRING_00264);
			}

		} else {
			if (faction == GameObject.FACTION_WU) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.CHANGE_CLASS_CLIENT, peony.Messages.STRING_00265);
			}
		}
	}

	protected void changeclass(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int clazz = packet.getInt();
		if (clazz >= 0 && clazz <= 3) {
			Player player = (Player) session.getClient();
			if (player != null && player.level < 6) {
				player.setClazz(clazz, false, serial);
			}
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.CHANGE_CLASS_CLIENT, peony.Messages.STRING_00266);
		}
	}

	protected void changeSex(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int sex = packet.getInt();
		if (sex == 0 || sex == 1) {
			Player player = (Player) session.getClient();
			if (player != null && player.level < 6) {
				player.setSex(sex, false, serial);
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.CHANGE_SEX_CLIENT, peony.Messages.STRING_00267);
			}
		}

	}

	protected void changeName(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int type = packet.get();
		String name = packet.getString();
		Player player = (Player) session.getClient();
		Account a = (Account)session.getIdentity();
		if (player != null) {
			if(type == 7 && !a.getName().substring(0, 2).equals("游客")){
				ErrorHandler.sendErrorMessage(session, serial, 
						OpCode.CHANGE_NAME_CLIENT, "你已经拥有自己的帐号了哦,不需要修改啦");
			}else if(StringUtil.isValidName(name) != 0){
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.CHANGE_NAME_CLIENT, "这个名字恐怕有损英雄的威名，请您重新换一个名字吧！");
			}else{
				if(type == 1){
					if(player.bag.getGameItem(ItemUtil.ITEM_CHANGE_NAME)!=null){
						Server.server.getServiceRegistry().getDbService().schedule(
								new PlayerRenameCall(session, player.id, name,serial,true));
					}else {
						if(!player.name.substring(0, 2).equals("游客")){
							ErrorHandler.sendErrorMessage(session, serial, 
									OpCode.CHANGE_NAME_CLIENT, "你已经拥有自己的呢称了哦,不需要修改啦");
							return;
						}
						Server.server.getServiceRegistry().getDbService().schedule(
								new PlayerRenameCall(session, player.id, name,serial,false));
					}
				}else if(type == 7){
//				}else{
//					if(player.level < 20){ //如果是小于6级的人改名，那么需要改账号名
						Server.server.getServiceRegistry().getDbService().schedule(
								new AccountRenameCall(session, serial, name));
//					}else{
//						ErrorHandler.sendErrorMessage(session, serial,
//								OpCode.CHANGE_NAME_CLIENT, peony.Messages.STRING_00269);
//					}
				}
			}
		}
	}

	protected void repair(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int type = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			player.repair(serial, type);
		}
	}

	protected void quickReg(Packet packet, ClientSession session) {
		AccountService accountService = Server.server.getServiceRegistry()
				.getAccountService();
		AccountQuickRegisterCall call = new AccountQuickRegisterCall(session,
				packet);
		accountService.schedule(call);
	}

	protected void accountReg(Packet packet, ClientSession session) {
		AccountService accountService = Server.server.getServiceRegistry()
				.getAccountService();
		AccountRegisterCall call = new AccountRegisterCall(session, packet);
		accountService.schedule(call);
	}

	protected void cancelAttack(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			player.cancelAttack();
		}
	}

	protected void cancelUseItem(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			player.cancelUseItem();
		}
	}

	public void cancelAutoAttack(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			player.cancelAutoAttack();
		}
	}

	public void buffDesc(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int instanceId = packet.getInt();
			int len = packet.getByte();
			Unit unit = (Unit) ObjectAccessor.getGameObject(instanceId);
			if (unit != null) {
				List<Buff> buffs = new ArrayList<Buff>(len);
				for (int i = 0; i < len; i++) {
					int buffId = packet.getInt();
					Buff buff = unit.buffs.getBuffByInstanceID(buffId);
					if (buff != null) {
						buffs.add(buff);
					}
				}
				Packet pt = new Packet(OpCode.BUFF_DESC_SERVER);
				pt.putInt(instanceId);
				pt.put(buffs.size());
				for (int i = 0; i < buffs.size(); i++) {
					Buff bf = buffs.get(i);
					pt.putInt(bf.getInstanceID());
					pt.putString(bf.getName());
					pt.putString(bf.getDesc());
				}
				session.send(pt);
			} else {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.BUFF_DESC_CLIENT, peony.Messages.STRING_00270);
			}
		}
	}

	public void actorDelete(Packet packet, ClientSession session) {
		if (session.getIdentity() != null && session.getClient() == null) {
			int id = packet.getInt();
			DeletePlayerCall call = new DeletePlayerCall(session, id);
			Server.server.getServiceRegistry().getDbService().schedule(call);
		}
	}

	public void shopSell(Packet packet, ClientSession session) {
		ShopSellCall call = new ShopSellCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	public void shopBuy(Packet packet, ClientSession session) {
		ShopBuyCall call = new ShopBuyCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	public void shopQuickBuy(Packet packet, ClientSession session) {
		QuickBuyCall call = new QuickBuyCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	public void shopList(Packet packet, ClientSession session) {
		ShopListCall call = new ShopListCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	public void relive(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if ((player != null) && (Server.isStepServer)) {
		      Packet pt = new Packet(OpCode.RELIVE_SERVER);
		      pt.putInt(player.instanceId);
		      pt.putInt(player.map.map.getId());
		      pt.putInt(player.getVMap().getInstanceId());
		      pt.putInt(player.x);
		      pt.putInt(player.y);
		      pt.putInt(0);
		      player.broadcast(pt, player, null, false, true, false);
		      player.send(pt);
		      return;
		    }
		if (player != null && !player.isAlive()) {
			int id = packet.getInt();
			player.relive(id);
		}
	}

	public void pkOk(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null && player.pkInfo == null) {
			int id = packet.getInt();
			PkInfo pkInfo = Server.server.getServiceRegistry().getPkService()
					.getPkInfo(id);
			if (pkInfo == null) {
				player.pkInfo = null;
				Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
				pt.putString(peony.Messages.STRING_00271);
				player.send(pt);
				return;
			} else if (pkInfo != null && pkInfo.target.id == player.id
					&& pkInfo.state == PkInfo.STATE_INIT) {
				Player source = (Player) ObjectAccessor
						.getGameObject(pkInfo.source);
				if (source.systemState != Player.SYSTEMSTATE_READY) {
					pkInfo.state = PkInfo.STATE_END;
					source.pkInfo = null;
					return;
				}
				if (source.getVMap() != player.getVMap()
						|| !source.inRange(player, 120)) {
					pkInfo.state = PkInfo.STATE_END;
					source.pkInfo = null;
					Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
					pt.putString(peony.Messages.STRING_00272);
					source.send(pt);
					player.send(pt);
					return;
				}
				if (!source.getVMap().isAllowDuel()) {
					Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
					pt.putString(peony.Messages.STRING_00273);
					source.send(pt);
					player.send(pt);
					return;
				}
				if (source.pkInfo != pkInfo) {
					source.pkInfo = null;
					pkInfo.state = PkInfo.STATE_END;
					return;
				}
				if (source.getMoney() < pkInfo.wager
						|| player.getMoney() < pkInfo.wager) {
					pkInfo.state = PkInfo.STATE_END;
					source.pkInfo = null;
					Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
					pt.putString(peony.Messages.STRING_00249);
					source.send(pt);
					player.send(pt);
					return;
				}
				if (pkInfo.wager > 0) {
					PlayerTransaction tx1 = source.newTransaction("PKE");
					PlayerTransaction tx2 = player.newTransaction("PKE");
					try {
						source.decMoney(pkInfo.wager, tx1, true);
						player.decMoney(pkInfo.wager, tx2, true);
						tx1.commit();
						tx2.commit();
					} catch (NoEnoughValueException e) {
						tx1.rollback();
						tx2.rollback();
						Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
						pt.putString(peony.Messages.STRING_00249);
						source.send(pt);
						player.send(pt);
						return;
					}
				}
				if(source.attendant!=null || player.attendant!=null){
					Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
					pt.putString("公平起见,不允许携带随从进行切磋");
					source.send(pt);
					player.send(pt);
					return;
				}
				int x = source.x + (player.x - source.x) / 2;
				int y = source.y + (player.y - source.y) / 2;
				player.pkInfo = pkInfo;
				pkInfo.x = x;
				pkInfo.y = y;
				pkInfo.map = player.getVMap();
				pkInfo.state = PkInfo.STATE_STARTED;
				Packet pt = new Packet(OpCode.PK_OK_SERVER);
				pt.putInt(source.instanceId);
				pt.putInt(player.instanceId);
				pt.putInt(pkInfo.wager);
				pt.putInt(x);
				pt.putInt(y);
				pt.putInt(pkInfo.r);
				source.send(pt);
				player.send(pt);
				source.mapCell.broadcast(source, source, pt, false, false, false);
			}
		}
	}

	public void pkRefuse(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int id = packet.getInt();
			PkInfo pkInfo = Server.server.getServiceRegistry().getPkService()
					.getPkInfo(id);
			if (pkInfo != null && pkInfo.target.id == player.id
					&& pkInfo.state == PkInfo.STATE_INIT) {
				pkInfo.state = PkInfo.STATE_END;
				Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
				pt.putString(peony.Messages.STRING_00274);
				Player source = (Player) ObjectAccessor
						.getGameObject(pkInfo.source);
				Player target=(Player) ObjectAccessor
				.getGameObject(pkInfo.target);
				if(target!=null){
					target.canPK=0;
				}
				if (source != null) {
					source.canPK=0;
					source.pkInfo = null;
					source.send(pt);
				}
			}
		}
	}

	public void pkInvit(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			if (player.pkInfo == null) {
				int targetId = packet.getInt();
				int wager = packet.getInt();
				if (wager < 0) {
					log.error("[PKERROR]" + LogUtil.getPlayerLogString(player) + "WAGER[" + wager + "]");
					return;
				}
				if (player.getMoney() >= wager) {
					if (!player.getVMap().isAllowDuel()) {
						Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
						pt.putString(peony.Messages.STRING_00273);
						player.send(pt);
						return;
					}
					if(player.attendant!=null){
						Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
						pt.putString("公平起见,不允许携带随从进行切磋");
						player.send(pt);
						return;
					}
					if(player.attendantWaitRelive){
						Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
						pt.putString("随从死亡30秒内不允许切磋");
						player.send(pt);
						return;
					}
					Player target = (Player) ObjectAccessor.getPlayer(targetId);
					if (target != null
							&& target.systemState == Player.SYSTEMSTATE_READY) {
						if(target.attendant!=null){
							Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
							pt.putString("公平起见,不允许携带随从进行切磋");
							player.send(pt);
							return;
						}
						if(target.attendantWaitRelive){
							Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
							pt.putString("随从死亡30秒内不允许切磋");
							player.send(pt);
							return;
						}
						PlayerRelation rel = Server.server.getServiceRegistry()
								.getRelationService().get(target.id);
						if (rel != null && rel.blackList.exists(player.id)) {
							Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
							pt.putString(peony.Messages.STRING_00275);
							player.send(pt);
							return;
						}
						if(!target.isAllowPK()){
							Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
							pt.putString(peony.Messages.STRING_00261);
							player.send(pt);
							return;
						}
						if (target.pkInfo == null&&(target.canPK==0)) {
							if (target.faction != player.faction) {
								Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
								pt.putString(peony.Messages.STRING_00276);
								player.send(pt);
								return;
							}
							PkInfo pkInfo = new PkInfo(Server.server
									.getServiceRegistry().getPkService(),
									player.ref(), target.ref(), wager,
									Time.currTime, 20 * 8);
							target.canPK=1;//1是说明目标pk状态正在进行中，但过期时必须设置为0
							player.pkInfo = pkInfo;
							Packet pt = new Packet(OpCode.PK_INVIT_SERVER);
							pt.putInt(pkInfo.id);
							pt.putInt(player.id);
							pt.putString(player.name);
							pt.put(player.level);
							pt.put(player.clazz);
							pt.putInt(wager);
							target.send(pt);
							
							// 发送PK通知消息
							Server.server.getEventManager().addEvent(
									new ServiceEvent(ServiceEvent.EVENT_INTERACT, player,
											target, PlayerRelation.INTERACT_PK));
						} else {
							Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
							pt.putString(peony.Messages.STRING_00277);
							player.send(pt);
						}
					}
				}
			} else {
				String info = "";
				if (player.pkInfo.state == PkInfo.STATE_END) {
					return;
				} else if (player.pkInfo.state == PkInfo.STATE_STARTED) {
					info = peony.Messages.STRING_00278;
					Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
					pt.putString(info);
					player.send(pt);
				} else if (player.pkInfo.state == PkInfo.STATE_INIT) {
					info = peony.Messages.STRING_00279;
					ErrorHandler.sendErrorMessage(session, -1,
							OpCode.PK_INVIT_CLIENT, info);
				}

				return;
			}
		}
	}

	public void roll(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int rollId = packet.getInt();
			int type = packet.get();
			Roll roll = Server.server.getServiceRegistry().getRollService()
					.getRoll(rollId);
			if (roll != null) {
				if (type == 1) {
					LogUtil.logRollAdd(player, rollId);
					roll.addRollPoint(player.ref());
				} else {
					LogUtil.logRollCancel(player, rollId);
					roll.discard(player.ref());
				}
			}
		}
	}

	public void setActionBar(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			byte[] bytes = packet.getBytes();
			player.actionBarOptions = bytes;
			Packet pt = new Packet(OpCode.SET_ACTIONBAR_OPTION_SERVER);
			pt.putInt(serial);
			player.send(pt);
		}
	}

	public void actionBar(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			Packet pt = new Packet(OpCode.ACTIONBAR_OPTION_SERVER);
//			pt.put(player.actionBarOptions != null ? player.actionBarOptions
//					: new byte[0]);
			if(player.actionBarOptions!=null)
				pt.put(player.actionBarOptions);
			else
				player.actionBarOptions = PlayerUtil.ACTIONBAR_BYTES;
			player.send(pt);
		}
	}

	public void itemInfo(Packet packet, ClientSession session) {
		// * 物品Id int
		// * 物品实例Id int -1 表示请求一般信息
		// * 类型 byte 0位为1说明需要ITEM信息，1位为1说明需要物品描述
		int itemId = packet.getInt();
		int instanceId = packet.getInt();
		byte type = packet.get();
		int questId = 0;
		try {
			questId = Integer.parseInt(packet.getString());
		} catch (Exception e) {
		}
		if (instanceId == -1) {
			ItemTemplate template = ObjectAccessor.getItemTemplate(itemId);
			if (template != null) {
				Packet pt = new Packet(OpCode.ITEMINFO_SERVER);
				pt.put(type);
				pt.putInt(itemId);
				pt.putInt(instanceId);
				if ((type & 1) != 0) {
					pt.put(GameItem.toClientBytes(template));
				}
				if ((type & 2) != 0) {
					pt.putString(ItemUtil.parseUseConfirm(template.desc));
				}
				session.send(pt);
			} else {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.ITEMINFO_CLIENT, peony.Messages.STRING_00280);
			}
		} else {
			GameItem item = ObjectAccessor
					.getCachedGameItem(itemId, instanceId);
			if (item == null) {
				// 在背包里查找
				Player player = (Player) session.getClient();
				if (player != null) {
					Object[] arr = ItemUtil.findPlayerEquipment(player, itemId, instanceId);
					if (arr != null) {
						item = (GameItem)arr[0];
					} else if (player.depot != null) {
						item = player.depot.getGameItem(-1, itemId, instanceId);
					}
				}
			}
			if(item==null && questId!=0){
				QuestRewardService service = Server.server.getServiceRegistry().getQuestRewardService();
				item = service.getGameItem(questId, itemId);
			}
			if (item != null) {
				Packet pt = new Packet(OpCode.ITEMINFO_SERVER);
				pt.put(type);
				pt.putInt(itemId);
				pt.putInt(instanceId);
				if ((type & 1) != 0) {
					pt.put(item.toClientBytes());
				}
				if ((type & 2) != 0) {
					pt.putString(item.getDesc());
				}
				session.send(pt);
			} else {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.ITEMINFO_CLIENT, peony.Messages.STRING_00280);
			}
		}
	}

	public void mailFavorite(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int mailId = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			Server.server.getServiceRegistry().getDbService()
					.schedule(
							new MailFavoriteCall(session, serial, player.ref(),
									mailId));
		}
	}

	public void mailDelete(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int mailId = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			Server.server.getServiceRegistry().getDbService().schedule(
					new MailDeleteCall(session, serial, player.ref(), mailId));
		}
	}
	
	public void mailObsoleteDelete(Packet packet,ClientSession session){
		int serial= packet.getInt();
		int type=packet.getInt();
		Player player =(Player)session.getClient();
		if(player!=null){
			Server.server.getServiceRegistry().getDbService().schedule(
					new MailObsoleteDeleteCall(session,serial,player.ref(),player.id,type));
		}
	}
	


	public void mailAttachment(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int mailId = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			Server.server.getServiceRegistry().getDbService().schedule(
					new MailAttachmentCall(session, serial, player.ref(),
							mailId));
		}
	}

	public void mailContent(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int mailId = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			Server.server.getServiceRegistry().getDbService().schedule(
					new MailContentCall(session, serial, mailId, player.id));
		}
	}

	public void mailList(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int type = packet.getShort();
		int pageSize = packet.getShort();
		int pageNo = packet.getShort();
		Player player = (Player) session.getClient();
		if (player != null) {
			Server.server.getServiceRegistry().getDbService().schedule(
					new MailListCall(session, serial,type, player.id, pageSize,
							pageNo));
		}
	}

	public void mailPost(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		String destName = packet.getString();
		String title = packet.getString();
		String content = packet.getString();
		int price = packet.getInt();
		byte[] attachment = packet.getBytes();
		Player player = (Player) session.getClient();
		if (player != null) {
			if (destName.equals(player.name)) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.MAIL_POST_CLIENT, peony.Messages.STRING_00281);
				return;
			}
			Server.server.getServiceRegistry().getDbService().schedule(
					new MailPostCall(session, serial, player.ref(), destName,
							title, MessageFormat.format("{0}{1}", "(来自玩家的信件\r)",content), price, attachment));
		}
	}

	public void partyLeave(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			if (player.party != null) {
				player.party.leave(player.id);
			}
		}
	}

	public void partyKick(Packet packet, ClientSession session) {
		int targetId = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			if (player.party != null
					&& player.party.leader.getId() == player.id) {
				player.party.kick(targetId);
			}
		}
	}
	public void partyTransLeader(Packet packet, ClientSession session) {
		int targetId = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			if (player.party != null
					&& player.party.leader.getId() == player.id) {
				try {
					player.party.transLeader(player.id, targetId);
				} catch (TransLeaderException e) {
				}
			} else {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_TRANSFER_LEADER_CLIENT, peony.Messages.STRING_00282);
			}
		}
	}


	public void partyInvitReject(Packet packet, ClientSession session) {
		int requestId = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			PartyService partyService = Server.server.getServiceRegistry()
					.getPartyService();
			PartyRequest request = partyService.getAndRemoveRequest(requestId);
			if (request != null) {
				Player source = (Player) ObjectAccessor
						.getGameObject(request.ref);
				if (source != null) {
					Packet pt = new Packet(OpCode.PARTY_INVIT_REJECT_SERVER);
					pt.putString(player.name);
					pt.putString(peony.Messages.STRING_00283);
					source.send(pt);
				}
			}
		}
	}

	public void partyInvitOk(Packet packet, ClientSession session) {
		int requestId = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null && player.party == null) {
			PartyService partyService = Server.server.getServiceRegistry()
					.getPartyService();
			PartyRequest request = partyService.getAndRemoveRequest(requestId);
			if (request != null && (Time.currTime - request.time) < 60000) { // 一分钟之内有效
				Player source = (Player) ObjectAccessor
						.getGameObject(request.ref);
				if (source != null) {
				    if((source.map!=null && source.map.getId() == ChessInstanceService.MAPID) || (player.map!=null && player.map.getId() == ChessInstanceService.MAPID)){
				    	ErrorHandler.sendErrorMessage(session, -1,
								OpCode.PARTY_INVIT_OK_CLIENT, "五子连珠场景里不允许组队");
				    	return;
				    }
					if (source.party != null) {
						if (!source.party.isFull()
								&& source.party.leader.getId() == source.id) {
							try {
								source.party.addMember(player);
								player.addAction(Action.JOIN_PARTY);
								LogUtil.joinPartySuccess(player.name, source.party.id);
							} catch (PartyFullException e) {
							}
						}
					} else {
						Party party = new Party(partyService, source, -1);
						source.addAction(Action.JOIN_PARTY);
						try {
							party.addMember(player);
							player.addAction(Action.JOIN_PARTY);
							LogUtil.joinPartySuccess(player.name, party.id);
						} catch (PartyFullException e) {
						}
					}
				}
			} else {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_OK_CLIENT, peony.Messages.STRING_00284);
			}
		}
	}
	
	public void partyJoinAnswer(Packet packet, ClientSession session){
		int joinerId = packet.getInt();
		int answer = packet.getUnsignedByte();
		Player player = (Player) session.getClient();
		if (player != null) {
			Player target = (Player) ObjectAccessor.getPlayer(joinerId);
			if(target == null){
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_JOIN_ANSWER_CLIENT, peony.Messages.STRING_00285);
				return;
			}
			if(Server.server.getServiceRegistry().getPartyService().getParty(joinerId)!=null){
				ErrorHandler.sendErrorMessage(session, -1, OpCode.PARTY_INVIT_CLIENT, peony.Messages.STRING_00286);
				return;
			}
			if(answer == 1){//拒绝此人加入队伍
				Packet pt = new Packet(OpCode.PARTY_INVIT_REJECT_SERVER);
				pt.putString(player.name);
				pt.putString(peony.Messages.STRING_00287);
				target.send(pt);
				return;
			}
			PartyService partyService = Server.server.getServiceRegistry()
				.getPartyService();
			if (player.party != null) {
				if (!player.party.isFull()
						&& player.party.leader.getId() == player.id) {
					try {
						player.party.addMember(target);
						target.addAction(Action.JOIN_PARTY);
					} catch (PartyFullException e) {
					}
				} else {
					ErrorHandler.sendErrorMessage(session, -1,
							OpCode.PARTY_INVIT_CLIENT, peony.Messages.STRING_00288);
					return;
				}
			} else {
				Party party = new Party(partyService, player, -1);
				player.addAction(Action.JOIN_PARTY);
				try {
					party.addMember(target);
					target.addAction(Action.JOIN_PARTY);
				} catch (PartyFullException e) {
				}
			}
			
		}
	}

	public void partyInvit(Packet packet, ClientSession session) {
		int targetId = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			// if (player.party == null) {
			// ErrorHandler.sendErrorMessage(session, -1,
			// OpCode.PARTY_INVIT_CLIENT, "当前属于非组队状态");
			// return;
			// }
			if (player.party != null && player.party.isFull()) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_CLIENT, peony.Messages.STRING_00288);
				return;
			}
			if (player.party != null
					&& player.party.leader.getId() != player.id) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_CLIENT, peony.Messages.STRING_00289);
				return;
			}
			Player target = (Player) ObjectAccessor.getPlayer(targetId);
			if (target == null) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_CLIENT, peony.Messages.STRING_00270);
				return;
			}
			if (target.faction != player.faction) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_CLIENT, peony.Messages.STRING_00290);
				return;
			}
			if (player.minorFaction!=target.minorFaction) {
				String hint="您的好友正在忙碌中，暂时无法与你组队。";
				if(target.map!=null&&target.map.map!=null&&target.map.map.getId()==AsyncBattleService.battleMap){//擂台
					hint="您的好友正在擂台奋战，暂时无法与你组队。";
				}
				if(target.isInStep){//跨服
					hint="您的好友正在跨服竞技场奋战，暂时无法与你组队。";
				}
				if(target.map.map.getMapID()==FeastInstanceService.MAPID){//满汉
					hint="您的好友正在满汉全席奋战，暂时无法与你组队。";
				}
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_CLIENT, hint);
				return;
			}
			if (target.party != null) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_CLIENT, peony.Messages.STRING_00291);
				return;
			}
			if(!target.isAllowParty()){
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_CLIENT, peony.Messages.STRING_00261);
				return;
			}
			if(player.getVMap().getId() == CycleInstanceMapManager.mapId.get(new Integer(player.clazz))
					|| target.getVMap().getId() == CycleInstanceMapManager.mapId.get(new Integer(target.clazz))){
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_CLIENT, "您或您的邀请目标正在荣誉塔当中，请稍后再邀请入队");
				return;
			}
			if(target.isInStep){
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_CLIENT, "目标正在跨服竞技，请稍后再邀请入队！");
				return;
			}
			RelationService relService = Server.server.getServiceRegistry()
					.getRelationService();
			PlayerRelation rel = relService.get(target.id);
			if (rel.blackList.exists(player.id)) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_CLIENT, peony.Messages.STRING_00292);
				return;
			}
			PartyService partyService = Server.server.getServiceRegistry()
					.getPartyService();
			PartyRequest request = partyService.newPartyRequest(player, target);
			Packet pt = new Packet(OpCode.PARTY_INVIT_SERVER);
			pt.putInt(request.id);
			pt.putInt(player.id);
			pt.putString(player.name);
			pt.put(player.level);
			pt.put(player.clazz);
			pt.put(player.sex);
			target.send(pt);

		}
	}
	
	public void partyJoin(Packet packet, ClientSession session) {
		int targetId = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			// if (player.party == null) {
			// ErrorHandler.sendErrorMessage(session, -1,
			// OpCode.PARTY_INVIT_CLIENT, "当前属于非组队状态");
			// return;
			// }
			//自己已经组队
			if (player.party != null && player.party.getCount() > 0) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_JOIN_CLIENT, peony.Messages.STRING_00293);
				return;
			}
			
			Player target = (Player) ObjectAccessor.getPlayer(targetId);
			if(target == null){
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_JOIN_CLIENT, peony.Messages.STRING_00270);
				return;
			}
			if (target.faction != player.faction) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_JOIN_CLIENT, peony.Messages.STRING_00290);
				return;
			}
			if (player.minorFaction!=target.minorFaction) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_JOIN_CLIENT, peony.Messages.STRING_00290);
				return;
			}
			if(target.party == null || target.party.getCount() == 0){
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_JOIN_CLIENT, peony.Messages.STRING_00294);
				return;
			}
			
			//TODO:距上次请求是否超过20秒
			
			if(target.party.isFull()){
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_JOIN_CLIENT, peony.Messages.STRING_00288);
				return;
			}
			
			RelationService relService = Server.server.getServiceRegistry()
					.getRelationService();
			PlayerRelation rel = relService.get(target.id);
			if (rel.blackList.exists(player.id)) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_JOIN_CLIENT, peony.Messages.STRING_00292);
				return;
			}
			
			if(target.party.leader == null){
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_JOIN_CLIENT, peony.Messages.STRING_00295);
				return;
			}
			
			
//			PartyService partyService = Server.server.getServiceRegistry()
//					.getPartyService();
//			PartyRequest request = partyService.newPartyRequest(player, target);
			
			Packet pt = new Packet(OpCode.PARTY_JOIN_SERVER);
			pt.putInt(player.id);
			pt.putString(player.name);
			pt.put(player.level);
			pt.put(player.clazz);
			pt.put(player.sex);
			target.party.leader.player.send(pt);

		}
	}

	public void partyCreate(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			if (player.party == null) {
				PartyService partyService = Server.server.getServiceRegistry()
						.getPartyService();
				new Party(partyService, player, serial);
			}
		}
	}

	public void playerInfo(Packet packet, ClientSession session) {
		Server.server.getServiceRegistry().getDbService().schedule(
				new PlayerInfoCall(packet, session));
		// RelationService relationService =
		// Server.server.getServiceRegistry().getRelationService();
		// ActorCacheService actorCacheService =
		// Server.server.getServiceRegistry().getActorCacheService();
		// int serial = packet.getInt();
		// int id = packet.getInt();
		// Player player = (Player) session.getClient();
		// if (player != null) {
		// Player target = (Player) ObjectAccessor.getGameObject(
		// GameObject.TYPE_PLAYER, id, 0);
		// if (target != null) {
		// Packet pt = new Packet(OpCode.PLAYER_INFO_SERVER);
		// pt.putInt(serial);
		// pt.putString(target.name);
		// pt.put(target.level);
		// pt.put(target.clazz);
		// pt.put(target.faction);
		// pt.putString(target.getGuildName());
		// pt.putString(target.chatOptions.nativeName);
		// Title title = target.titles.getCurrentTitle();
		// pt.putString(title==null?"":title.name);
		// pt.putString("");
		// // 夫妻信息
		// int mateId = relationService.get(target.id).mateId;
		// if(mateId != -1){
		// pt.putString(actorCacheService.find(relationService.get(target.id).mateId).name);
		// }else{
		// pt.putString("");
		// }
		// pt.putString(target.getCreditString());
		// pt.put(target.equipments.toClientBytes());
		// pt.putInt(target.equipments.getHeadScore(target.level,
		// target.clazz));
		// pt.putInt(target.equipments.getBodyScore(target.level,
		// target.clazz));
		// pt.putInt(target.equipments.getWeaponScore(target.level,
		// target.clazz));
		// pt.put(target.sex);
		// session.send(pt);
		// } else {
		// ErrorHandler.sendErrorMessage(session, serial,
		// OpCode.PLAYER_INFO_CLIENT, "该用户已下线");
		// }
		// }
	}

	public void chatNativeChange(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		String nativeName = packet.getString();
		Player player = (Player) session.getClient();
		if (player != null) {
			String oldNative = player.chatOptions.nativeName;
			player.chatOptions.nativeName = nativeName;
			ChatService chatService = Server.server.getServiceRegistry()
					.getChatService();
			chatService.nativeChange(oldNative, nativeName, player);
			Packet pt = new Packet(OpCode.CHAT_NATIVE_CHANGE_SERVER);
			pt.putInt(serial);
			player.send(pt);
		}
	}

	public void chatOption(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int len = packet.get();
		Player player = (Player) session.getClient();
		if (player != null) {
			ChatService chatService = Server.server.getServiceRegistry()
					.getChatService();
			if (len > 0) {
				for (int i = 0; i < len; i++) {
					byte ch = packet.get();
					byte op = packet.get();
					ChatOption option = player.chatOptions.options[ch];
					boolean inChannel = (op & 0x10) != 0 ? true : false;
					boolean notify = (op & 0x20) != 0 ? true : false;
					int index = (op & 0xF);
					if (option.inChannel != inChannel
							|| option.notify != inChannel
							|| option.color != index) {
						player.chatOptions.options[ch] = new ChatOption(
								inChannel, notify, index);
						if (option.inChannel != inChannel) {
							chatService.changeOption(ch, option.inChannel,
									inChannel, player);
						}
					}
				}
				Packet pt = new Packet(OpCode.CHAT_OPTION_SERVER);
				pt.putInt(serial);
				player.send(pt);
			}
		}
	}

	public void chat(Packet packet, ClientSession session) {
		byte ch = packet.get();
		int destId = packet.getInt();
		String message = packet.getString();
		byte[] attachment = packet.getBytes();
		Player player = (Player) session.getClient();
		if (player != null) {
			// 记录日志
			LogUtil.logChat(player, ch, destId, "[CHAT]", message);
			
			// 统计
			Server.server.getServiceRegistry().getRealtimeStatService().chatCounter++;

			/** 密码 */
			if (Server.cheatOn && message.equals(Server.server.cheat)) {
				player.cheat = true;
				LogUtil.logChat(player, ch, destId, "[CHEAT]", message);
				return;
			}
			if (player.cheat && message.startsWith("/")) {
				String[] cmds = message.split("\\s+");
				if (cmds[0].equals("/go")) {
					if (cmds.length == 4) {
						int mapId = Integer.parseInt(cmds[1]);
						int x = Integer.parseInt(cmds[2]);
						int y = Integer.parseInt(cmds[3]);
						try {
							player.goMap(mapId, x, y);
						} catch (Exception e) {
						}
					}
				} else if (cmds[0].equals("/exp")) {
					if (cmds.length == 2) {
						int exp = Integer.parseInt(cmds[1]);
						PlayerTransaction tx = player.newTransaction("GM");
						player.addExp(exp, tx, true);
						tx.commit();
					}
				} else if (cmds[0].equals("/item")) {
					if (cmds.length == 3) {
						int itemId = Integer.parseInt(cmds[1]);
						int count = Integer.parseInt(cmds[2]);
						ItemTemplate template = ObjectAccessor
								.getItemTemplate(itemId);
						if (template != null) {
							if (template.newInstance || template.isEquipment()) {
								for (int i = 0; i < count; i++) {
									GameItem item = ObjectAccessor
											.createGameItem(template, -1);
									PlayerTransaction tx = player
											.newTransaction("GM");
									player.bag.addGameItem(item, 1, tx, true);
									tx.commit();
								}
							} else {
								GameItem item = ObjectAccessor
										.createGameItem(template, -1);
								PlayerTransaction tx = player.newTransaction("GM");
								player.bag.addGameItem(item, count, tx, true);
								tx.commit();
							}
						}

					}
				} else if(cmds[0].equals("/body")){
					try {
//						AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
//						int count=0;
//						for(int i=0;i<1000;i++){
//							int targetId = Integer.parseInt(cmds[1]);
//							player.goMap(AsyncBattleService.battleMap, 330, 280);
//							player.asyncTargetId = targetId;
//							System.out.println("===============:"+i);
//						}
						int targetId = Integer.parseInt(cmds[1]);
						player.goMap(AsyncBattleService.battleMap, 350, 500);
						player.asyncTargetId = targetId;
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if(cmds[0].equals("/bodyr")){
					try {
						AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
						AsyncNormalBoard ab=service.getAsyncNormalBoardByRank(Integer.parseInt(cmds[1]));
						int targetId = ab.playerId;
						player.goMap(AsyncBattleService.battleMap, 330, 280);
						player.asyncTargetId = targetId;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else if(cmds[0].equals("/clearscore")){
					AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
					AsyncNormalBoard board=service.getAsyncNormalBoardByPlayerId(player.id);
					board.achievementStateNew=new int[20];
				}else if(cmds[0].equals("/clearbody")){
					AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
					service.getTops().clear();
					service.getId2boards().clear();
					service.getRank2boards().clear();
//					for(AsyncNormalBoard board:service.getId2boards().values()){
//						if(board!=null){
//							board.achievementStateNew[6]=0;
//							board.achievementStateNew[7]=0;
//							board.achievementStateNew[8]=0;
//						}
//					}
				}else if(cmds[0].equals("/clearInfo")){
					AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
					AsyncNormalBoard ab=service.getAsyncNormalBoardByPlayerId(player.id);
					ab.battleInfos.clear();		
				}else if(cmds[0].equals("/bodys")){//挑战积分
					AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
					AsyncNormalBoard ab=service.getAsyncNormalBoardByPlayerId(player.id);
					ab.officerScore=Integer.parseInt(cmds[1]);
				}else if(cmds[0].equals("/pool")){
					for(String s : player.pool.properties.keySet()){
						System.out.println("@@@@@@@@@@"+s+" = "+player.pool.properties.get(s));
					}
				}else if(cmds[0].equals("/bbs")){
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					ClientBbsService service = Server.server.getServiceRegistry().getClientBbsService();
					String publish = "2013-06-25 16:43:00";
					Date publishDate = null;
					try {
						publishDate = df.parse(publish);
					} catch (ParseException e) {
					}
					int minLevel = 70;
					int maxLevel = 100;
					if(!service.isInLevel(minLevel,maxLevel)){
						return;
					}
					ClientBbs newbbs = new ClientBbs(minLevel,maxLevel);
					if(publishDate!=null)
						newbbs.isschedule = true;
					newbbs.pulishTime = publishDate;
					Iterator<ClientBbs> it = service.bbs.iterator();
					if(!newbbs.isschedule){
						while(it.hasNext()){
							ClientBbs b = it.next();
							if(b.minLevel == newbbs.minLevel && b.maxLevel == newbbs.maxLevel && b.enable == true){
								b.obsoleteTime = new Date();
								b.enable = false;
								Server.server.getServiceRegistry().getDbService().clientBbsDao.updateEntity(b);
								it.remove();
							}
						}
					}
					String explaination = "eeeeeeee";
					newbbs.explaination = explaination;
					short size = 1;
					StringBuilder sb = new StringBuilder();
					for(int i=0;i<size;i++){
						String activeName = "eeeeeeeeeee";
						sb.append(activeName);
						sb.append("/");
						String detailContent = "eeeeeeeeeeee";
						sb.append(detailContent);
						if(i!=size-1){
							sb.append("/");
							}
						}
					String str = sb.toString();
					newbbs.active = str;
					if(publishDate==null)
						newbbs.pulishTime = new Date();
					newbbs.enable = true;
					if(!newbbs.isschedule)
						service.bbs.add(newbbs);
					else{
						Iterator<ClientBbs> iterator = service.bbsLater.iterator();
						while(iterator.hasNext()){
							ClientBbs bb = iterator.next();
							if(bb.minLevel == newbbs.minLevel && bb.maxLevel == newbbs.maxLevel && bb.enable == true){
								Server.server.getServiceRegistry().getDbService().clientBbsDao.makeTransient(bb);
								iterator.remove();
							}
						}
						service.bbsLater.add(newbbs);
					}
					Server.server.getServiceRegistry().getDbService().clientBbsDao.makePersistent(newbbs);
				}else if (cmds[0].equals("/money")) {
					if (cmds.length == 2) {
						int money = Integer.parseInt(cmds[1]);
						PlayerTransaction tx = player.newTransaction("GM");
						player.addMoney(money, tx, true);
						tx.commit();
					}
				} else if (cmds[0].equals("/credit")) {
					if (cmds.length == 2) {
						int credit = Integer.parseInt(cmds[1]);
						player.setCredit(credit, true, "GM");
					}
				} else if (cmds[0].equals("/title")) {
					if (cmds.length == 2) {
						int titleId = Integer.parseInt(cmds[1]);
						Title t = TitleUtil.getTitle(titleId);
						if (t != null) {
							player.addTitle(t);
						}
					}
				} else if (cmds[0].equals("/pvp")) {
					if (player.isPvp()) {
						player.unPvp();
					} else {
						player.pvp(Player.PVP_TIME);
					}
				} else if (cmds[0].equals("/pvpfaction")) {
					if (player.isPvpFaction()) {
						player.unPvpFaction();
					} else {
						player.pvpFaction();
					}
				} else if (cmds[0].equals("/honor")) {
					if (cmds.length == 2) {
						int honor = Integer.parseInt(cmds[1]);
						player.setHonor(player.honor + honor, true, "GM");
					}
				} else if (cmds[0].equals("/horseexp")) {
					if (cmds.length == 2) {
						int exp = Integer.parseInt(cmds[1]);
						if (player.horse != null) {
							player.horse.setExp(player.horse.exp + exp, player, "GM");
						}
					}
				} else if (cmds[0].equals("/horsedegree")) {
					if (cmds.length == 2) {
						int d=Integer.parseInt(cmds[1]);
						if (player.horse != null) {
							player.horse.setDegree(d, player);
						}
					}
				} else if (cmds[0].equals("/rank")) {
					if (cmds.length == 2) {
						int rank = Integer.parseInt(cmds[1]);
						if (rank >= 0 && rank <= 16) {
							player.setRank(rank);
						}
					}
				} else if (cmds[0].equals("/reload")) {
					try {
						boolean needProj = !(cmds[1].equals("file") || cmds[1].equals("version") || cmds[1].equals("config") || cmds[1].equals("hints") || cmds[1].equals("randomfaction"));
						ProjectData newPrj = null;
						if (needProj) {
							ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
							newPrj = new ProjectData();
					        newPrj.serverMode = true;
					        newPrj.createPathFinder = false;
					        newPrj.branch = proj.branch;
					        try {
					        	newPrj.load(proj.baseDir);
					        } catch (Exception e) {
					        	e.printStackTrace();
					        	return;
					        }
						}
						Server.server.getServiceRegistry().getDataService().reload(newPrj, cmds[1]);
					} catch (Exception e) {
						log.error(e, e);
					}
				} else if (cmds[0].equals("/horseskill")) {
					if (cmds.length == 2) {
						int skillId = Integer.parseInt(cmds[1]);
						if (player.horse != null) {
							Skill skill = ObjectAccessor
									.getSkill((skillId << 16) + 1);
							if (player.horse.skills.size() < player.horse.skillSize) {
								player.horse.addSkill(skill, player);
								player.refreshProperties(false);
							}
						}
					}
				} else if (cmds[0].equals("/activepower")) {
					player.setActivePower(100);
				} else if (cmds[0].equals("/producepractice")) {
					if (cmds.length == 2) {
						int producepractice = Integer.parseInt(cmds[1]);
						player.pool.setInt(Player.PROPERTY_PRODUCE_ABILITY,
								producepractice);
					}
				} else if (cmds[0].equals("/gatherpractice")) {
					if (cmds.length == 2) {
						int gatherpractice = Integer.parseInt(cmds[1]);
						player.pool.setInt(Player.PROPERTY_GATHER_ABILITY,
								gatherpractice);
					}
				} else if (cmds[0].equals("/armorpoint")) {
					if (cmds.length == 2) {
						int armorpoint = Integer.parseInt(cmds[1]);
						player.setHeadScore((armorpoint / 10) << 16
								| (armorpoint % 10));
						player.setBodyScore((armorpoint / 10) << 16
								| (armorpoint % 10));
					}
				} else if (cmds[0].equals("/instance")) {
					if (cmds.length == 2) {
						int id = Integer.parseInt(cmds[1]);
						player.setTodayInstanceTimes(id, 0);
					}
				} else if (cmds[0].equals("/rebuildpvplist")) {
					Server.server.getServiceRegistry().getStatService()
							.rebuildPvpInfos();
				} else if (cmds[0].equals("/rebuildcreditlist")) {
					Server.server.getServiceRegistry().getStatService()
							.rebuildWeekCredits();
				} else if (cmds[0].equals("/rebuildlevellist")) {
					Server.server.getServiceRegistry().getStatService()
							.rebuildLevelRanks();
				} else if (cmds[0].equals("/clearrecord")) {
					Server.server.getServiceRegistry().getNormalVMapManager()
							.clear(player.id);
				} else if (cmds[0].equals("/refreshpoint")) {
					player.refreshPropertiesPoint();
				} else if (cmds[0].equals("/openSignup")) {
					CandidateService candidateService = Server.server
							.getServiceRegistry().getCandidateService();
					candidateService.openSignup();
				} else if (cmds[0].equals("/closeSignup")) {
					CandidateService candidateService = Server.server
							.getServiceRegistry().getCandidateService();
					candidateService.closeSignup();
				} else if (cmds[0].equals("/openVote")) {
					CandidateService candidateService = Server.server
							.getServiceRegistry().getCandidateService();
					candidateService.openVote();
				} else if (cmds[0].equals("/closeVote")) {
					CandidateService candidateService = Server.server
							.getServiceRegistry().getCandidateService();
					candidateService.closeVote();
				} else if (cmds[0].equals("/birthKing")) {
					CandidateService candidateService = Server.server
							.getServiceRegistry().getCandidateService();
					candidateService.birthKing();
				} else if (cmds[0].equals("/king")) {
					if (player != null) {
						Nation nation = Server.server.getServiceRegistry()
								.getNationService().getNationByFaction(
										player.faction);
						Officer king = new Officer(player.id, Officer.KING,
								player.faction, Server.server
										.getServiceRegistry()
										.getActorCacheService().find(player.id));
						nation.addOfficer(king);
						player.setKing();
						player.buffs.addBuff(BuffUtil.createSuiteBuff(216, 1));
					}
				} else if (cmds[0].equals("/setkingmoney") && cmds.length == 2) {
					if (player != null) {
						int v = Integer.parseInt(cmds[1]);
						if (Server.server.getServiceRegistry()
								.getNationService().isKing(player)) {
							Nation nation = Server.server.getServiceRegistry()
									.getNationService().getNationByFaction(
											player.faction);
							nation.money = v;
						}
					}
				} else if (cmds[0].equals("/maintain")){
					if (player != null) {
						if (Server.server.getServiceRegistry()
								.getNationService().isKing(player)) {
							Nation nation = Server.server.getServiceRegistry()
									.getNationService().getNationByFaction(
											player.faction);
							nation.maintainAll();
						}
						TongService tongService = Server.server.getServiceRegistry().getTongService();
						Tong tong = tongService.getPlayerTong(player.id,false);
						if(tong!=null && tong.getChairmanName().equals(player.name)){
							tong.maintainAll();
							int[] questIds = TongService.getTongQuestIds(player.faction);
							for(int questId : questIds){
								tong.pool.setInt(Tong.PROPERTY_TONG_QUEST + questId, 0);
							}
						}
					}
				} else if (cmds[0].equals("/sneak")){
					if (player != null) {
						if (Server.server.getServiceRegistry()
								.getNationService().isKing(player)) {
							int destFaction = Integer.parseInt(cmds[1]);
							if(destFaction!=player.faction){
								Server.server.getServiceRegistry().getNationService().addRequest(player.faction, destFaction, new Date(System.currentTimeMillis()+10*1000L));
							}
						}
					}
				}else if(cmds[0].equals("/online") && cmds.length==2){
					int dis = Integer.parseInt(cmds[1]);
					ExpService.onlineDis = dis*60*1000L;
				}else if(cmds[0].equals("/notonline") && cmds.length==2){
					int dis = Integer.parseInt(cmds[1]);
					ExpService.notonlineDis = dis*60*1000L;
				}else if(cmds[0].equals("/weather") && cmds.length==7){
					int mapId = Integer.parseInt(cmds[1]);
					int type = Integer.parseInt(cmds[2]);
					int size = Integer.parseInt(cmds[3]);
					int count = Integer.parseInt(cmds[4]);
					int speed = Integer.parseInt(cmds[5]);
					int wind = Integer.parseInt(cmds[6]);
					Weather weather = new Weather(type,size,count,speed,wind,0xE8C888);
					Server.server.getServiceRegistry().getWeatherService().changeWeatherAndAddBuff(mapId, weather);
				}else if(cmds[0].equals("/nationconvoy")){
					Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(player.faction);
					try {
						Server.server.getServiceRegistry().getNationConvoyService().startConvoy(nation,0);
					} catch (ConvoyException e) {
						player.message(-1, e.getMessage(), -1, -1);
					}
				}else if(cmds[0].equals("/removedata")){
					Server.server.getServiceRegistry().getBossScoreService().removeAllData();
				} else if (cmds[0].equals("/finishquest")) {
					player.asmVm.forceAddFinished(Integer.parseInt(cmds[1]));
				}else if(cmds.length==2 && cmds[0].equals("/tongmoney")){
					int tongMoney = Integer.parseInt(cmds[1]);
					TongService tongService = Server.server.getServiceRegistry().getTongService();
					Tong tong = tongService.getPlayerTong(player.id,false);
					tong.money = tongMoney;
				} else if (cmds[0].equals("/slow")) {
					player.buffs.addBuff(new SlowDebuff(player, 70, 300000));
				} else if (cmds[0].equals("/unslow")) {
					player.buffs.removeBuff(10004);
				} else if (cmds[0].equals("/clearbag")) {
					PlayerTransaction tx = player.newTransaction("GM");
					player.bag.clear(tx, true);
					tx.commit();
				} else if(cmds[0].equals("/charge") && cmds.length==2){
					int money = Integer.parseInt(cmds[1]);
					Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_CHARGE_SUCCESS, player, money));
				} else if(cmds[0].equals("/star") && cmds.length==3){
					int id = Integer.parseInt(cmds[1]);
					int star = Integer.parseInt(cmds[2]);
					for(GameItem item : player.equipments.equs){
						if(item!=null && item.template!=null && item.template.isEquipment() && (id==-1 ? true : (item.template.id==id))){
							if(item.object!=null && item.object instanceof ItemEnhance){
								ItemEnhance en = (ItemEnhance)item.object;
								en.setStar(star);
								if(en.getNaturals()!=null){
									for(NaturalEnhance h : en.getNaturals()){
										h.value = item.getNatureEnhanceAttribute(h.attType
												, h.percent);
										if(h.value==0)
											h.value = 1;
									}
								}
							}
						}
					}
					player.refreshProperties(false);
				}else if(cmds[0].equals("/horsestar") && cmds.length==3){
					int id = Integer.parseInt(cmds[1]);
					int star = Integer.parseInt(cmds[2]);
					for(GameItem item : player.horse.equs.equs){
						if(item!=null && item.template!=null && item.template.isEquipment() && (id==-1 ? true : (item.template.id==id))){
							if(item.object!=null && item.object instanceof ItemEnhance){
								ItemEnhance en = (ItemEnhance)item.object;
								en.setStar(star);
								if(en.getNaturals()!=null){
									for(NaturalEnhance h : en.getNaturals()){
										h.value = item.getNatureEnhanceAttribute(h.attType
												, h.percent);
										if(h.value==0)
											h.value = 1;
									}
								}
							}
						}
					}
					player.refreshProperties(false);
				}else if(cmds[0].equals("/load") && cmds.length==2){
					String className = cmds[1];
					try {
						Class clazz = Class.forName("peony.patchs."+className);
						Runnable r = (Runnable) clazz.newInstance();
						r.run();
					} catch (Exception e) {
						log.error(e, e);
					}
				}else if (cmds[0].equals("/bot")) {
					player.antiPlug.isBot = true;
				}else if(cmds[0].equals("/shut")){
					System.exit(0);
				}else if(cmds[0].equals("/find")){
					String name = cmds[1];
					for(ItemTemplate tmp : ObjectAccessor.itemTemplates.values()){
						if(tmp.name.contains(name)){
							player.message(-1, tmp.name+":"+tmp.id, -1, -1);
						}
					}
				}else if(cmds[0].equals("/mapid")){
					String name = cmds[1];
					ProjectData data = Server.server.getServiceRegistry().getDataService().data;
					List<DataObject> maps = data.getDataListByType(GameArea.class);
					for(DataObject o : maps){
						if(o instanceof GameArea){
							GameArea m = (GameArea)o;
							List<GameMapInfo> infos = m.getAreaInfo().maps;
							for(GameMapInfo info : infos){
								if(info.name.contains(name))
									player.message(-1, info.name + ": " + (m.id<<4 | info.id), -1, -1);
							}
						}
					}
				}else if(cmds[0].equals("/power")){
					int power = player.calculateBattleValue();
					player.message(-1, "您的战力评估为："+power, -1, -1);
				}else if(cmds[0].equals("/fquest")){
					player.asmVm.forceAddFinished(Integer.parseInt(cmds[1]));
					Packet pt = new Packet(OpCode.QUEST_FINISHED_SERVER);
					pt.putInt(Integer.parseInt(cmds[1]));
					player.send(pt);
				}else if(cmds[0].equals("/addquest")){
					player.asmVm.preConditionOk.add(Integer.parseInt(cmds[1]));
					player.asmVm.pending(Integer.parseInt(cmds[1]));
				}else if(cmds[0].equals("/jewelconfig")){
					GetJewelConfigCall.loadPacket();
				}else if(cmds[0].equals("/throwhorse")){
					player.horseBag.horses.clear();
					player.message(-1, peony.Messages.STRING_00296, -1, -1);
				}else if(cmds[0].equals("/closequest")){
					String quests = cmds[1];
					String[] questsArr = quests.split(",");
					for(String quest : questsArr){
						try {
							int questId = Integer.parseInt(quest);
							GameQuest gq = ASMQuestUtil.getGameQuest(questId);
							if(gq!=null)
								gq.closeQuest();
						} catch (NumberFormatException e) {
							
						}
					}
				}else if(cmds[0].equals("/openquest")){
					String quests = cmds[1];
					String[] questsArr = quests.split(",");
					for(String quest : questsArr){
						try {
							int questId = Integer.parseInt(quest);
							GameQuest gq = ASMQuestUtil.getGameQuest(questId);
							if(gq!=null)
								gq.openQuest();
						} catch (NumberFormatException e) {
							
						}
					}
				}else if(cmds[0].equals("/opendrop")){
					String drops = cmds[1];
					String[] dropIds = drops.split(",");
					for(String k:dropIds){
						try {
							GroupDrop gd = ObjectAccessor.getGroupDrop(Integer.parseInt(k));
							if(!gd.isValid()){
								gd.setValid(true);
							}
						} catch (NumberFormatException e) {
						}
					}
				}else if(cmds[0].equals("/closedrop")){
					String drops = cmds[1];
					String[] dropIds = drops.split(",");
					for(String k:dropIds){
						try {
							GroupDrop gd = ObjectAccessor.getGroupDrop(Integer.parseInt(k));
							if(!gd.isValid()){
								gd.setValid(false);
							}
						} catch (NumberFormatException e) {
						}
					}
				}else if(cmds[0].equals("/changeday")){
					CycleInstanceMapManager.currentDay++;
					Time.day --;
//					Server.server.getServiceRegistry().getCycleInstanceMapManager().clearAllPlayers();
//					Server.server.getServiceRegistry().getCycleInstanceMapManager().player2maps.clear();
					Time.currentDayOfYear_test++;
					if(Time.currentWeekDay_test==7)
						Time.currentWeekDay_test = 1;
					else
						Time.currentWeekDay_test++;
				}else if(cmds[0].equals("/contribute")){
					int type = Integer.parseInt(cmds[1]);//1 增加的贡献度算在每日贡献度中   其他不算入
					int con = Integer.parseInt(cmds[2]);
					TongService ts = Server.server.serviceRegistry.getTongService();
					if(type == 1){
						ts.addContribute(player, con,true);
					}else{
						ts.addContribute(player, con,false);
					}
				}else if(cmds[0].equals("/tongContribute")){
					int con = Integer.parseInt(cmds[1]);
					TongService ts = Server.server.serviceRegistry.getTongService();
					ts.addTongContributeMiji(player,con);
				}else if(cmds[0].equals("/enaidu")){
					int num = Integer.parseInt(cmds[1]);
					int value = player.pool.getInt(WeddingService.PROPERTY_ENAIDU,0) + num;
					player.pool.setInt(WeddingService.PROPERTY_ENAIDU, value);
				}else if(cmds[0].equals("/tongContribute2")){
					int con = Integer.parseInt(cmds[1]);
					TongService ts = Server.server.serviceRegistry.getTongService();
					ts.setTongContribute(player,con);
				}else if (cmds[0].equals("/salary")) {
					if (cmds.length == 2) {
						int salary = Integer.parseInt(cmds[1]);
						PlayerTransaction tx = player.newTransaction("GM");
						player.addSalary(salary, tx, true);
						tx.commit();
						int needAddSalary = salary;
						int oldDayValue = player.pool.getInt(SalaryService.PPOPERTY_SALARY_DAY,0);
						if((player.getPlayerSalary() + needAddSalary) > SalaryService.SALARY_LIMIT){
							needAddSalary = SalaryService.SALARY_LIMIT - player.getPlayerSalary();
							if((player.daySalary+needAddSalary)>SalaryService.SALARY_DAYLIMIT){
								needAddSalary = SalaryService.SALARY_DAYLIMIT - player.daySalary;
							}
						}else{
							if((player.daySalary+needAddSalary)>SalaryService.SALARY_DAYLIMIT){
								needAddSalary = SalaryService.SALARY_DAYLIMIT-player.daySalary;
							}
						}
						int newDayValue = oldDayValue + needAddSalary;
						player.pool.setInt(SalaryService.PPOPERTY_SALARY_DAY, newDayValue);
					}
				}else if(cmds[0].equals("/getdaysalary")){
					int salary = Integer.parseInt(cmds[1]);
					player.pool.setInt(SalaryService.PPOPERTY_SALARY_DAY, player.pool.getInt(SalaryService.PPOPERTY_SALARY_DAY,0)+salary);
					player.daySalary = player.pool.getInt(SalaryService.PPOPERTY_SALARY_DAY,0);
					Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_SALARY_ADD,player));
					PlayerTransaction tx = player.newTransaction("GM");
					player.addSalary(salary, tx, true);
					tx.commit();
				}else if(cmds[0].equals("/horsefix")){
					int con = Integer.parseInt(cmds[1]);
					if(player.horse!=null){
						player.horse.fixCount = con;
					}else{
						player.message(-1, "请选择一匹坐骑", -1, -1);
					}
				}else if(cmds[0].equals("/cardexp")){
					int exp = Integer.parseInt(cmds[1]);
					if(player.cards!=null){
						try {
							player.cards.addExp(exp);
						} catch (Exception e) {
							
						}
					}
				}else if(cmds[0].equals("/cardtime0")){
					int count = Integer.parseInt(cmds[1]);
					for(peony.util.IntHashMap<Integer> map : CardRockCall.npcToFreeMap.values()){
						map.put(player.id, count);
					}
				}else if(cmds[0].equals("/cardtime1")){
					int count = Integer.parseInt(cmds[1]);
					for(peony.util.IntHashMap<Integer> map : CardRockCall.npcToCreditMap.values()){
						map.put(player.id, count);
					}
				}else if(cmds[0].equals("/card")){
					int itemId=Integer.parseInt(cmds[1]);
					int count=Integer.parseInt(cmds[2]);
					PlayerTransaction tx2 = player.newTransaction("EQUIPLEVELUPADDJEWELTOBAG");
					GameItem item=ObjectAccessor.createGameItem(itemId);
					try {
						player.bag.addGameItemComplete(item, count, tx2, true);
					} catch (NoEnoughSpaceException e) {
					}
				}else if(cmds[0].equals("/carddel")){
					int itemId=Integer.parseInt(cmds[1]);
					CardService cardService = Server.server.getServiceRegistry().getCardService();
					Card card = cardService.getCardByItemId(itemId);
					player.cards.cardInfos.remove(card.id);
					player.pool.setInt(CardService.getPropertyOfPlayerCard(card.id), 0);
				}else if(cmds[0].equals("/clearbooks")){
					if(player.books!=null){
					   player.books.clearBooks();
					}
				} else if(cmds[0].equals("/enaidu")){
					int value = Integer.parseInt(cmds[1]);
					player.pool.setInt(WeddingService.PROPERTY_ENAIDU, value);
					
				} else if(cmds[0].equals("/fulivalue")){
					int value = Integer.parseInt(cmds[1]);
					player.pool.setInt(GambleService.PROPERTY_VALUE_FULI, value);
				} else if(cmds[0].equals("/marriagequest")){
					WeddingService service = Server.server.getServiceRegistry().getWeddingService();
					service.playerFinishQuest(player, 2392, 0);
				} else if(cmds[0].equals("/time")){
					Date date = new Date();
					SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
					String dateString = format.format(date);
					player.message(-1, MessageFormat.format("当前时间：{0}", dateString), -1, -1);
				} else if(cmds[0].equals("/boss")){
					String npcName = cmds[1].trim();
					PloyNpcService service = Server.server.getServiceRegistry().getPloyNpcService();
					service.refreshPointNpc(npcName);
				}else if(cmds[0].equals("/attlevel")){
					int value = Integer.parseInt(cmds[1]);
					player.pool.setInt(AttendantFixService.PROPERTY_ATTENDANTEXP, value);
				}else if(cmds[0].equals("/duration")){
					int value = Integer.parseInt(cmds[1]);
					for(GameItem item : player.equipments.equs){
						if(item!=null){
							item.duration = Math.min(value, item.template.equipment.duration);
							if(item.duration==0){
								player.refreshProperties(false);
							}
							DurationChangedItem changedItem = new DurationChangedItem(item);
							player.changed.addChangedItem(changedItem);
						}
					}
				}else if(cmds[0].equals("/firstcharge")&& cmds.length==2){
					int ammount = Integer.parseInt(cmds[1]);
					int iamount = ammount ;
					RecordChargeCall call = new RecordChargeCall(null, player.accountId, iamount);
					Server.server.getServiceRegistry().getDbService()
							.schedule(call);
					ChargeRegularCall call2 = new ChargeRegularCall(player.session,player.accountId, iamount);
					Server.server.getServiceRegistry().getDbService()
					.schedule(call2);
					if (player != null) {
						Server.server.getEventManager().fireEvent(
								new ServiceEvent(ServiceEvent.EVENT_CHARGE_SUCCESS, player, iamount));
		            }
				}else if(cmds[0].equals("/clearcharge")&& cmds.length==1){
					player.pool.remove(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL);
					RecordChargeService service = Server.server.getServiceRegistry().getRecordChargeService();
					synchronized(service){
						List<Charge> list = Server.server.getServiceRegistry().getDbService().chargeDao.getChargesAfter(VipPrivilegeService.START_TIME);
						for(Charge l : list){
							if(l.accountId == player.accountId){
								Server.server.getServiceRegistry().getDbService().chargeDao.makeTransient(l);
							}
						}
						List<Charge> charges = service.getChargesAfter(VipPrivilegeService.START_TIME);
						Iterator<Charge> it = charges.iterator();
						while(it.hasNext()){
							Charge charge =it.next();
							if(charge.accountId == player.accountId){
								it.remove();
							}
						}
					}
					player.pool.remove(VipPrivilegeService.PROPERTY_VIP_IBUYVALUE);
					List<IBuy> ib = Server.server.getServiceRegistry().getDbService().ibuyDAO.getIbuyAfter(player.accountId,VipPrivilegeService.START_TIME);
					for(IBuy i : ib){
						Server.server.getServiceRegistry().getDbService().ibuyDAO.makeTransient(i);
					}
					
				}else if(cmds[0].equals("/chargeforother")&& cmds.length==3){
					int accountId = Integer.parseInt(cmds[1]);
					int ammount = Integer.parseInt(cmds[2]);
					RecordChargeCall call = new RecordChargeCall(null, accountId, ammount);
					Server.server.getServiceRegistry().getDbService()
							.schedule(call);
					ChargeRegularCall call2 = new ChargeRegularCall(player.session,accountId, ammount);
					Server.server.getServiceRegistry().getDbService()
					.schedule(call2);
				}else if(cmds[0].equals("/account")){
					player.message(-1, String.valueOf(player.accountId), -1, -1);
				}else if(cmds[0].equals("/alchemy")){
					player.pool.setInt(AlchemyService.ALCHEMYEXP, 0);
					player.pool.setInt(AlchemyService.ALCHEMYEXP_USECALCULATE, 0);
					player.pool.setInt(AlchemyService.PLAYEREXP_TODAYADD, 0);
					player.alchemy.practiceLevel=0;
					player.alchemy.pulseIndex=0;
					player.alchemy.acupointNum=0;
					player.alchemy.acupointLevel=0;
					player.pool.setInt(AlchemyService.ALCHEMYCOUNT_PLAYEREXP, 4);
					player.alchemy.alchemyCount=4;
					player.alchemy.levelBreak=new boolean[]{false,false,false,false,false};
					player.alchemy.restExp=0;
				}else if(cmds[0].equals("/clearalchemyexp")){
					player.pool.setInt(AlchemyService.PLAYEREXP_TODAYADD, 0);
				}else if(cmds[0].equals("/alchemycount")){
					int count = Integer.parseInt(cmds[1]);
					player.alchemy.alchemyCount=count;
				}else if(cmds[0].equals("/getsuite")){
					if(cmds[1]!=null){
						if(cmds[1].equals("tmw")){//获取勇魂全套
							int id=1008282;
							for(int i=0;i<7;i++){
								PlayerTransaction tx2 = player.newTransaction("EQUIPLEVELUPADDJEWELTOBAG");
								try {
									GameItem  item=ObjectAccessor.createGameItem(id+i);
									player.bag.addGameItemComplete(item, 1, tx2, true);
									tx2.commit();
								}catch(Exception e){
									
								}
							}
						}else if(cmds[1].equals("tmc")){//获取天命全套
							int id=1008289;
							for(int i=0;i<7;i++){
								PlayerTransaction tx2 = player.newTransaction("EQUIPLEVELUPADDJEWELTOBAG");
								try {
									GameItem  item=ObjectAccessor.createGameItem(id+i);
									player.bag.addGameItemComplete(item, 1, tx2, true);
									tx2.commit();
								}catch(Exception e){
									
								}
							}
						}else if(cmds[1].equals("tmm")){//获取天命全套
							int id=1008296;
							for(int i=0;i<7;i++){
								PlayerTransaction tx2 = player.newTransaction("EQUIPLEVELUPADDJEWELTOBAG");
								try {
									GameItem  item=ObjectAccessor.createGameItem(id+i);
									player.bag.addGameItemComplete(item, 1, tx2, true);
									tx2.commit();
								}catch(Exception e){
									
								}
							}
						}else if(cmds[1].equals("tmf")){//获取天命全套
							int id=1008303;
							for(int i=0;i<10;i++){
								if(i==6||i==7){
									continue;
								}
								PlayerTransaction tx2 = player.newTransaction("EQUIPLEVELUPADDJEWELTOBAG");
								try {
									GameItem  item=ObjectAccessor.createGameItem(id+i);
									player.bag.addGameItemComplete(item, 1, tx2, true);
									tx2.commit();
								}catch(Exception e){
									
								}
							}
						}else if(cmds[1].equals("yhw")){
							int id=1008167;
							for(int i=0;i<7;i++){
								PlayerTransaction tx2 = player.newTransaction("EQUIPLEVELUPADDJEWELTOBAG");
								try {
									GameItem  item=ObjectAccessor.createGameItem(id+i);
									player.bag.addGameItemComplete(item, 1, tx2, true);
									tx2.commit();
								}catch(Exception e){
								}
							}
							PlayerTransaction tx2 = player.newTransaction("EQUIPLEVELUPADDJEWELTOBAG");
							try {
								GameItem  item=ObjectAccessor.createGameItem(1008191);
								player.bag.addGameItemComplete(item, 1, tx2, true);
								tx2.commit();
							}catch(Exception e){
							}
						}else if(cmds[1].equals("yhc")){
							int id=1008173;
							for(int i=0;i<7;i++){
								PlayerTransaction tx2 = player.newTransaction("EQUIPLEVELUPADDJEWELTOBAG");
								try {
									GameItem  item=ObjectAccessor.createGameItem(id+i);
									player.bag.addGameItemComplete(item, 1, tx2, true);
									tx2.commit();
								}catch(Exception e){
								}
							}
							PlayerTransaction tx2 = player.newTransaction("EQUIPLEVELUPADDJEWELTOBAG");
							try {
								GameItem  item=ObjectAccessor.createGameItem(1008194);
								player.bag.addGameItemComplete(item, 1, tx2, true);
								tx2.commit();
							}catch(Exception e){
							}
						}else if(cmds[1].equals("yhm")){
							int id=1008179;
							for(int i=0;i<7;i++){
								PlayerTransaction tx2 = player.newTransaction("EQUIPLEVELUPADDJEWELTOBAG");
								try {
									GameItem  item=ObjectAccessor.createGameItem(id+i);
									player.bag.addGameItemComplete(item, 1, tx2, true);
									tx2.commit();
								}catch(Exception e){
								}
							}
							PlayerTransaction tx2 = player.newTransaction("EQUIPLEVELUPADDJEWELTOBAG");
							try {
								GameItem  item=ObjectAccessor.createGameItem(1008192);
								player.bag.addGameItemComplete(item, 1, tx2, true);
								tx2.commit();
							}catch(Exception e){
							}
						}else if(cmds[1].equals("yhf")){
							int id=1008185;
							for(int i=0;i<7;i++){
								PlayerTransaction tx2 = player.newTransaction("EQUIPLEVELUPADDJEWELTOBAG");
								try {
									GameItem  item=ObjectAccessor.createGameItem(id+i);
									player.bag.addGameItemComplete(item, 1, tx2, true);
									tx2.commit();
								}catch(Exception e){
								}
							}
							PlayerTransaction tx2 = player.newTransaction("EQUIPLEVELUPADDJEWELTOBAG");
							try {
								GameItem  item=ObjectAccessor.createGameItem(1008193);
								player.bag.addGameItemComplete(item, 1, tx2, true);
							}catch(Exception e){
							}
							try {
								GameItem  item=ObjectAccessor.createGameItem(1008195);
								player.bag.addGameItemComplete(item, 1, tx2, true);
								tx2.commit();
							}catch(Exception e){
							}
						}else if(cmds[1].equals("tmbx")){
							int id=1008889;
							for(int i=0;i<7;i++){
								int itemId=id+i;
								if(i==0){
									itemId=1008868;
								}
								PlayerTransaction tx2 = player.newTransaction("EQUIPLEVELUPADDJEWELTOBAG");
								try {
									GameItem  item=ObjectAccessor.createGameItem(itemId);
									player.bag.addGameItemComplete(item, 1, tx2, true);
									tx2.commit();
								}catch(Exception e){
								}
							}
							PlayerTransaction tx2 = player.newTransaction("EQUIPLEVELUPADDJEWELTOBAG");
							try {
								GameItem  item=ObjectAccessor.createGameItem(1008192);
								player.bag.addGameItemComplete(item, 1, tx2, true);
								tx2.commit();
							}catch(Exception e){
							}
						
						}
					}
				}
				return;
			}
			if((!(session instanceof AdminDispatchClientSession)) && message.length()>30){
				player.message(-1, peony.Messages.STRING_00297, -1, -1);
				return;
			}
			ChatService chatService = Server.server.getServiceRegistry()
					.getChatService();
			ChatMessage cm = ChatMessage.parse(message, attachment, player, ch,
					destId);
			if (cm != null) {
				if(ch == ChatOption.AREA || ch == ChatOption.NATIVE || ch == ChatOption.GUILD || ch == ChatOption.PARTY){
					if(player.lastChatTime != 0){
						long a = Time.currTime - player.lastChatTime;
						if(Time.currTime - player.lastChatTime < 3000){
							player.message(-1, peony.Messages.STRING_00298, -1, -1);
							return;
						}
					}
					player.lastChatTime = Time.currTime;
				}
				if (ch == ChatOption.NATIVE) {
					if (player.chatOptions.nativeName == null
							|| player.chatOptions.nativeName.length() == 0) {
						cm = new ChatMessage(ChatOption.PRIVATE, -1, -1, peony.Messages.STRING_00004,
								player.id, peony.Messages.STRING_00299, null);
					} else {
						cm.destName = player.chatOptions.nativeName;
					}
				} else if (ch == ChatOption.PARTY) {
					if (player.party != null) {
						cm.sessions = player.party.getSessions();
					} else {
						cm = new ChatMessage(ChatOption.PRIVATE, -1, -1, peony.Messages.STRING_00004,
								player.id, peony.Messages.STRING_00300, null);
					}
				} else if (ch == ChatOption.AREA) {
					if(ChatService.isSpecialMap(player.map.getId()) && player.map.map.instance!=null){
						cm.destId = Integer.parseInt(player.map.id + "" +player.map.map.instance.getId());
					}else{
						cm.destId = player.map.id;
					}
				} else if (ch == ChatOption.GUILD) {
					TongMember tm = Server.server.getServiceRegistry()
							.getTongService().getPlayerInfo(player.id);
					if (tm != null) {
						if (tm.forbid) {
							cm = new ChatMessage(ChatOption.PRIVATE, -1, -1,
									peony.Messages.STRING_00004, player.id, peony.Messages.STRING_00301, null);
						} else
							cm.destId = tm.tongID;
					} else {
						cm = new ChatMessage(ChatOption.PRIVATE, -1, -1, peony.Messages.STRING_00004,
								player.id, peony.Messages.STRING_00302, null);
					}
				} else if (ch == ChatOption.FACTION) {
					cm.destId = player.faction;
					NationService nationService = Server.server.getServiceRegistry().getNationService();
					if (!nationService.isKing(player)) {
						// 如果是国王那么不限制国家聊数量
						synchronized (nationService.forbids) {
							for(Forbid forbid : nationService.forbids){
								if(forbid.targetId==player.id){
									player.message(-1, peony.Messages.STRING_00303, -1, -1);
									return;
								}
							}
						}
						int count = player.getTodayFactionChatCount();
						int limitCount = PlayerUtil.getFactionChatCount(player.level);
						if(limitCount!=0 && player.vipLevel>=1){//vip玩家提高国聊上限
							limitCount += VipPrivilegeService.NATIONCHAT_UPLIMIT;
						}
						if (count >= limitCount) {
							PlayerTransaction tx = player.newTransaction("CHT");
							if (player.bag
									.removeGameItem(ItemUtil.ITEM_FACTION_CHAT,
											-1, 1, tx, true) != null) {
								tx.commit();
							} else {
								tx.rollback();
								if (player.level > 10) {
									Packet pt = new Packet(OpCode.QUICK_BUY_ITEM_SERVER);
									pt.putInt(ItemUtil.ITEM_FACTION_CHAT);
									player.send(pt);
									
//									player.message(
//													-1,
//													peony.Messages.STRING_00304,
//													-1, -1);
									return;
								} else {
									player.message(-1, peony.Messages.STRING_00305, -1, -1);
									return;
								}
							}

						} else {
							player.setTodayFactionChatCount(count + 1);
						}
						if(nationService.getNationByFaction(player.faction).getOfficerByPlayerId(player.id)!=null){ //如果是官员
							cm.isOfficer = true;
						}
					} else {
						cm.isKing = true;
					}
				} else if (ch == ChatOption.WORLD) {
					PlayerTransaction tx = player.newTransaction("CHT");
					if (player.bag.removeGameItem(ItemUtil.ITEM_WORLD_CHAT, -1,
							1, tx, true) != null) {
						tx.commit();
					} else {
						tx.rollback();
						player.message(-1, peony.Messages.STRING_00306,
								-1, -1);
						return;
					}
				} else if(ch == ChatOption.PRIVATE){
					Player destPlayer = ObjectAccessor.getPlayer(cm.destId);
					if(destPlayer != null){
						cm.destName = destPlayer.getName();
					}else{
						cm.destName = "";
					}
				}
				cm.vipLevel = player.vipLevel;
				cm.playerLevel=player.level;

				chatService.addChatMessage(cm);
				Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_CHAT,player, new Byte(ch).intValue()));
			}
		}
	}

	public void reload(Packet packet, ClientSession session) {
		// try {
		// StageService stageService = Server.server.getServiceRegistry()
		// .getStageService();
		// stageService.reloadProjectData();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	// public void gatherEnd(Packet packet, ClientSession session) {
	// int instanceId = packet.getInt();
	// Player p = (Player) session.getClient();
	// if (p != null) {
	// GatherUnit gu = (GatherUnit) ObjectAccessor
	// .getGameObject(instanceId);
	// if (gu != null && gu.isAlive()) {
	// if (p.ref().equals(gu.ref)) {
	// Gain gain = new Gain(p);
	// Gain[] gains = new Gain[1];
	// gains[0] = gain;
	// gu.fall.gain(rnd, gains);
	// if (gain.completeAddToPlayer(true)) {
	// gu.gatherEnd();
	// // p.send(gu.getDetailPacket());
	// } else {
	// gu.ref = null;
	// gu.startTime = 0;
	// }
	// }
	// }
	// }
	// }

	public void gatherStart(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int instanceId = packet.getInt();
		Player p = (Player) session.getClient();
		if (p != null && p.isAlive()) {
			GatherUnit gu = (GatherUnit) ObjectAccessor
					.getGameObject(instanceId);
			if (gu != null && gu.isAlive()) {
				p.gatherStart(gu, serial);
				if(gu.level>p.level){
					p.message(-1, MessageFormat.format(peony.Messages.STRING_00307, gu.level), -1, -1);
					p.cancelGather(4);
					return;
				}
				if(p.map==null || gu.map==null || p.map.id!=gu.map.id){
					// 防止外挂刷材料
					p.cancelGather(3);
					return;
				}
				if(p.distance(gu)>100*100){
					// 防止外挂刷材料
					p.cancelGather(3);
					return;
				}
			} else {
				Packet pt = new Packet(OpCode.GATHER_CANCLED_SERVER);
				pt.putInt(serial);
				pt.put(1);
				p.send(pt);
			}
		}
	}

	public void unitInfo(Packet packet, ClientSession session) {
		int instanceId = packet.getInt();
		GameObject u = ObjectAccessor.getGameObject(instanceId);
		if (u != null) {
			session.send(u.getInfoPacket());
		}
	}

	public void getFile(Packet packet, ClientSession session) {
		String model = packet.getString();
		String name = packet.getString();
		if(asyncGetFile){
			Server.server.getServiceRegistry().getDbService().schedule(new GetFileCall(session, model, name));
		}else{
			try {
				DataService stageService = Server.server.getServiceRegistry()
						.getDataService();
				GameFile file = stageService.getGameFile(name, model);
				if (file != null) {
					Packet pt = new Packet(OpCode.GETFILE_SERVER);
					pt.putString(name);
					pt.putInt(file.version);
					pt.put(file.data);
					session.send(pt);
				}
			} catch (IOException e) {
				log.error(e, e);
			}
		}
	}
	
	/**
	 * 新版下载更新客户端文件(分包下载，减小每一个包得大小以便获得精确的更新速度)
	 */
	public void getFileNew(Packet packet, ClientSession session) {
		String model = packet.getString();
		String name = packet.getString();
		if(asyncGetFile){
			Server.server.getServiceRegistry().getDbService().schedule(new GetFileNewCall(session, model, name));
		}else{
			try {
				DataService stageService = Server.server.getServiceRegistry()
						.getDataService();
				GameFile file = stageService.getGameFile(name, model);
				if (file != null) {
					int DOWN_MAX = 10*1024;	//每个数据包的最大值 20KB
					int len = file.data.length / DOWN_MAX;
					if(file.data.length % DOWN_MAX != 0){
						len++;
					}
					if(file.data.length == 0){
						Packet pt = new Packet(OpCode.NEW_GETFILE_SERVER);
						pt.putString(name);
						pt.putInt(file.version);
						pt.putInt(0);
						pt.putInt(0);
						pt.put(file.data);
						session.send(pt);
					}else{
						int startIndex = 0;
						for(int i=0; i<len; i++){
							Packet pt = new Packet(OpCode.NEW_GETFILE_SERVER);
							pt.putString(name);
							pt.putInt(file.version);
							byte[] update;
							int downLen = file.data.length - startIndex;
							if(downLen > DOWN_MAX){
								downLen = DOWN_MAX;
							}
							update = new byte[downLen];
							System.arraycopy(file.data, startIndex, update, 0, downLen);
							pt.putInt(file.data.length);
							pt.putInt(startIndex);
							pt.put(update);
							session.send(pt);
							startIndex += downLen;
						}
					}
				}else{
					ErrorHandler.sendErrorMessage(session, -1, OpCode.NEW_GETFILE_CLIENT, "暂无文件");
				}
			} catch (IOException e) {
				log.error(e, e);
			}
		}
	}

	public void versionCompare(Packet packet, ClientSession session) {
		if(asyncVersionCompare){
			Server.server.getServiceRegistry().getDbService().schedule(new CompareVersionCall(session, packet));
		}else{
			String uiModel = packet.getString();
			String clientVersion = packet.getString();
			String clientModel = packet.getString();
			int clientDataVersion = packet.getInt();
			String[] clientVersionStr = clientVersion.split("\\.");
			String[] clientVersionStr1 = clientVersion.split("-");
			if(clientVersionStr.length>=3 && !clientVersionStr[0].equals("0")){
				StringBuffer sb = new StringBuffer();
				int a = clientVersionStr1.length;
				if(a<=1){
					clientVersion = sb.append(clientVersionStr[0]).append(".").
					append(clientVersionStr[1]).toString();
				}else if(a==2){
					clientVersion = sb.append(clientVersionStr[0]).append(".").
					append(clientVersionStr[1]+"-").append(clientVersionStr1[1]).toString();
				}else if(a==3){
					clientVersion = sb.append(clientVersionStr[0]).append(".").
					append(clientVersionStr[1]+"-").append(clientVersionStr1[1]).
					append("-").append(clientVersionStr1[2]).toString();
				}
			}
			Version version = Server.server.getServiceRegistry()
					.getVersionService().getVersion(clientVersion);
			if (version == null) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.VERSION_COMPARE_CLIENT, peony.Messages.STRING_00308);
				return;
			} else {
				if (version.status == Version.STATUS_OBSOLETE) {
		            if(version.url != null){
		            	Packet pt = new Packet(OpCode.SYMBIAN_UPDATE_URL_SERVER);
		            	pt.putString(version.url);
		            	pt.putString(cutSis(version.url));
		            	session.send(pt);
		            }
					ErrorHandler.sendErrorMessage(session, -1,
							OpCode.VERSION_COMPARE_CLIENT, version.message);
	
					return;
				}
			}
			log.info("[COMPVERSIONTRY]IP["+session.getClientIP()+"]SESSIONID["+
					LogUtil.getSessionIdBySession(session)+"]VERSSION["+version.id+"]VERSIONSTAT["+version.status+"]");
	//		int len = packet.getShort();
	//		String[] files = new String[len];
	//		int[] versions = new int[len];
	//		for (int i = 0; i < len; i++) {
	//			files[i] = packet.getString();
	//			versions[i] = packet.getInt();
	//		}
			int len = packet.getShort();
			List<String> fileList = new ArrayList<String>();
			List<Integer> versionList = new ArrayList<Integer>();
			while (true) {
				try {
					fileList.add(packet.getString());
					versionList.add(packet.getInt());
				} catch (Exception e) {
					break;
				}
			}
			String[] files = new String[fileList.size()];
			int[] versions = new int[versionList.size()];
			fileList.toArray(files);
			for(int i=0;i<versions.length;i++){
				versions[i] = versionList.get(i);
			}
			
			DataService stageService = Server.server.getServiceRegistry()
					.getDataService();
	
			byte[] newClientData = null;
			int newClientDataVersion = stageService.getClientDataVersion(uiModel);
	
			if (clientDataVersion != newClientDataVersion) {
				newClientData = stageService.getNewClientData(uiModel);
			}
	
			FileVersion[] remove = stageService.versionCompare(files, versions,
					uiModel);
			Packet pt = new Packet(OpCode.VERSION_COMPARE_SERVER);
			int kb = 0;
			int[] perSize = null;
			int index = 0;
			if (newClientData != null) {
				perSize = new int[remove.length + 1];
				pt.putShort(remove.length + 1);
				pt.putString("client.data");
				pt.putInt(newClientDataVersion);
				pt.put(newClientData);
				try {
					kb+=newClientData.length/1024;
					perSize[index] = kb;
					index++;
				} catch (Exception e) {
					
				}
			} else {
				perSize = new int[remove.length];
				pt.putShort(remove.length);
			}
	        
			for (int i = 0; i < remove.length; i++) {
				pt.putString(remove[i].name);
				pt.putInt(remove[i].version);
				int k = 0;
				try {
					GameFile file = stageService.getGameFile(remove[i].name, uiModel);
			        byte[] content = file.data;
			        k = content.length/1024;
				} catch (IOException e) {
					
				}
				kb+=k;
				perSize[index] = k;
				index++;
			}
			pt.putInt(kb);
			for(int j=0;j<perSize.length;j++){
				pt.putInt(perSize[j]);
			}
			session.send(pt);
			log.info("[COMPVERSIONOK]IP["+session.getClientIP()+"]SESSIONID["+LogUtil.
					getSessionIdBySession(session)+"]VERSSION["+version.id+"]VERSIONSTAT["+version.status+"]");
		
			}
	}
    public synchronized static String cutSis(String url){
    	int i = url.lastIndexOf('/');
    	return url.substring(i);
    }

	public void skillDesc(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int groupId = packet.getShort();
		int level = packet.get();
		Skill skill = ObjectAccessor
				.getSkill(Skills.getSkillId(groupId, level));
		Player player = (Player) session.getClient();
		if (skill != null && player != null) {
			Packet pt = new Packet(OpCode.SKILL_DESC_SERVER);
			pt.putInt(serial);
			pt.putShort(groupId);
			pt.put(level);
			if (!skill.isPlayerSkill()) {
				pt.put(0);
			} else {
				pt.put(skill.getPoint());
			}
			pt.putString(SkillUtil.getSkillDesc(skill, player));
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.SKILL_DESC_CLIENT, peony.Messages.STRING_00309);
		}
	}

	public void propertyPointAdd(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int strength = packet.getShort();
		int agility = packet.getShort();
		int stamina = packet.getShort();
		int intellect = packet.getShort();
		Player player = (Player) session.getClient();
		if (player != null) {
			if (strength < 0 || agility < 0 || stamina < 0 || intellect < 0) {
				log.error("[PROPERTYPOINTERROR]" + LogUtil.getPlayerLogString(player) + "STR[" + strength + "]AGI[" +
						agility + "]STA[" + stamina + "]INT[" + intellect + "]");
				return;
			}
			if (strength + agility + stamina + intellect > player.propertyPoint) {
				log.error("[PROPERTYPOINTERROR]" + LogUtil.getPlayerLogString(player) + "STR[" + strength + "]AGI[" +
						agility + "]STA[" + stamina + "]INT[" + intellect + "]");
				return;
			}
			player.addPropertyPoint(strength, agility, stamina, intellect,
					serial);
		}
	}

	public void skillRefresh(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			player.refreshSkillPointWithRule(serial);
		}
	}

	public void useItem(Packet packet, ClientSession session) {
		byte gridIdByte = packet.get();
		int gridId = gridIdByte;
		if(gridIdByte!=-1 && gridIdByte<0)
			gridId = gridIdByte & 0xFF;
		int itemId = packet.getInt();
		int instanceId = packet.getInt();
		int targetId = packet.getInt();
		int time = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			if(Server.isStepServer){
				if(!StepServer.canUse(itemId)){
					player.sendUseItemFail(itemId, "跨服战场期间不能使用此物品");
					return;
				}
			}
			GameItem item = player.bag.getGameItem(gridId, itemId, instanceId);
			if(item!=null && item.validTime<0){
				player.sendUseItemFail(itemId, "物品已经过期");
				return;
			}
			
			if(ActivityItemEffect.confirm(player, itemId,gridId)){
				return;
			}
			int t = Time.currTime - time;
			if (t < 0)
				t = 0;
			player.useItem(gridId, itemId, instanceId, targetId, t * 2);
		}
	}

	public void unequip(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int itemId = packet.getInt();
		int instanceId = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			player.unequip(itemId, instanceId, serial);
		}
	}

	public void equip(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int itemId = packet.getInt();
		int instanceId = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			player.equip(itemId, instanceId, serial);
		}
	}

	public void notify(Packet packet, ClientSession session) {
		int questId = packet.getInt();
		byte id = packet.get();
		byte type = packet.get();
		byte answer = packet.get();
		Player player = (Player) session.getClient();
		if (player != null) {
			player.uiNotify(questId, id, type, answer);
		}
	}

	public void questAbandon(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int questId = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			player.abandonQuest(questId, serial);
		}
	}

	public void actorCreate(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		String name = packet.getString().trim();
		int sex = packet.get();
		int clazz = packet.get();
		int faction = packet.get();
		byte type = 0;
		try{type = packet.get();}catch(Exception e){}
		int randomFaction = faction;
		if(faction == 0)
			randomFaction = PlayerUtil.getRandomFaction();
		if(type==PlayerCreateCall.TYPE_RANDOM_FACTION_SEX_NAME 
				&& Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_TW) && name.equals("")){
			clazz = PlayerUtil.getRandomClazz();
			sex = PlayerUtil.getRandomSex();
		}
		Player player = PlayerUtil.createPlayer(name, sex, clazz, randomFaction, session.getIdentity().getId());
		if(faction==0 && PlayerUtil.rewardItem>0 && PlayerUtil.rewardCount>0){
			GameItem item = ObjectAccessor.createGameItem(PlayerUtil.rewardItem);
			if(item!=null){
				PlayerTransaction tx = player.newTransaction("RANDOMFACTION");
				try {
					player.bag.addGameItemComplete(item, PlayerUtil.rewardCount, tx, false);
					tx.commit();
				} catch (NoEnoughSpaceException e) {
					tx.rollback();
				}
			}
		}
		DBService dbService = Server.server.getServiceRegistry().getDbService();
		PlayerService playerService = Server.server.getServiceRegistry().getPlayerService();
		dbService.schedule(new PlayerCreateCall(playerService, session, player, serial, type));
	}

	public void actorList(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		DBService dbService = Server.server.getServiceRegistry().getDbService();
		LoadActorListCall call = new LoadActorListCall(serial, dbService,
				session);
		dbService.schedule(call);
	}

	public void accountLogin(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		String name = packet.getString();
		String password = packet.getString();
		String model = packet.getString();
		String uiModel = packet.getString();
		String version = packet.getString();
		String realPhone = "";
		try {
			realPhone = packet.getString();
			if (realPhone == null) {
				realPhone = "";
			}
		} catch (Exception e) {
		}
		int playerID = -1;
		try {
			playerID = packet.getInt();
		} catch (Exception e) {
		}
		String IMEI = "";
		if(!(Server.server.REVISION_TYPE_CMCC.equals(Server.server.revision)|| Server.server.REVISION_TYPE_TEL.equals(Server.server.revision))){
			try {
				IMEI = packet.getString();
			} catch (Exception e) {
			}
		}
		// if (!StringUtil.isAccountNameValid(name)) {
		// ErrorHandler.sendErrorMessage(session, serial,
		// OpCode.ACCOUNT_LOGIN_CLIENT, "内测期间，您的账号暂时不能登陆游戏");
		// return;
		// }
		String qmeId = "0";
		if(Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_TW)){
			try{qmeId = packet.getString();}catch(Exception e){}
		}
		AccountService accountService = Server.server.getServiceRegistry()
				.getAccountService();
		AccountLoginCall call = new AccountLoginCall(session, name, password,
				model, uiModel, version, serial, realPhone, playerID, IMEI, qmeId);
		if("CMCC".equals(Server.server.revision)||"CHINATEL".equals(Server.server.revision)){
			String cmccUserId = packet.getString();
			String cmccUserKey = packet.getString();
			if("CMCC".equals(Server.server.revision) && cmccUserId.equals(""))
				cmccUserId = CmccAccountService.CMCC_ANDROID_USERID;
			if("CMCC".equals(Server.server.revision) && cmccUserKey.equals(""))
				cmccUserKey = CmccAccountService.CMCC_ANDROID_USERKEY;
			call.setCmccUserId(cmccUserId);
			call.setCmccUserKey(cmccUserKey);
		}
		accountService.schedule(call);
	}

	public void itemDesc(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int itemId = packet.getInt();
		int instanceId = packet.getInt();
		if (instanceId == -1) {
			ItemTemplate template = ObjectAccessor.getItemTemplate(itemId);
			if (template != null) {
				Packet pt = new Packet(OpCode.ITEM_DESC_SERVER);
				pt.putInt(serial);
				pt.putInt(itemId);
				pt.putInt(instanceId);
				pt.putString(ItemUtil.parseUseConfirm(template.desc));
				session.send(pt);
			}
		} else {
			Player p = (Player) session.getClient();
			if (p != null) {
				GameItem item = ObjectAccessor.getCachedGameItem(itemId,
						instanceId);
				if (item == null) {
					Object[] os = ItemUtil.findPlayerEquipment(p, itemId,
							instanceId);
					if (os != null) {
						item = (GameItem) os[0];
					}else if(p.depot!=null && p.depot.getGrids().size()>0){
						item = p.depot.getGameItem(-1, itemId, instanceId);
					}
				}
				if (item == null) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.ITEM_DESC_CLIENT, peony.Messages.STRING_00310);
				} else {
					Packet pt = new Packet(OpCode.ITEM_DESC_SERVER);
					pt.putInt(serial);
					pt.putInt(itemId);
					pt.putInt(instanceId);
					pt.putString(item.getDesc());
					session.send(pt);
				}
			}
		}
	}

	public void syncVariable(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int questId = packet.getInt();
			int index = packet.getInt();
			int value = packet.getInt();
			player.asmVm.clientSyncVariable(questId, index, value);
		}
	}

	public void questList(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			player.sendQuestList();
		}
	}

	public void skillAddPoint(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int groupId = packet.getShort();
		int level = packet.getByte();
		Player player = (Player) session.getClient();
		if (player != null) {
			player.addSkillPoint(groupId, level, true, serial);
		}

	}

	public void skillList(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
//			if(player.relations!=null && player.relations.mateId!=-1){
				MarriageService mService = Server.server.getServiceRegistry().getMarriageService();
				mService.refreshMateSkill(player);
				Player mate = ObjectAccessor.getPlayer(player.relations.id);
				if(mate!=null){
					mService.refreshMateSkill(mate);
				}
//			}
			player.sendSkillList();
		}
	}

	public void skillNameList(Packet packet, ClientSession session) {
		IntHashMap<String> names = ObjectAccessor.getPlayerSkillName();
		Packet pt = new Packet(OpCode.SKILL_NAMELIST_SERVER);
		pt.put(names.size());
		Iterator<IntEntry<String>> ite = names.intEntrySet().iterator();
		while (ite.hasNext()) {
			IntEntry<String> entry = ite.next();
			pt.putShort(entry.getIntKey());
			pt.putString(entry.getValue());
		}
		session.send(pt);
	}

	public void removeItem(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			byte gridIdByte = packet.get();
			int gridId = gridIdByte;
			if(gridIdByte!=-1 && gridIdByte<0)
				gridId = gridIdByte & 0xFF;
			int itemId = packet.getInt();
			int instanceId = packet.getInt();
			int count = packet.get();
			GameItem gameItem = player.bag.getGameItem(gridId,itemId,instanceId);
			if(gameItem != null && gameItem.template.isEquipment()){
				ItemEnhance itemEnhance = (ItemEnhance)gameItem.object;
				if(itemEnhance != null && itemEnhance.getJewelCount() > 0 && itemEnhance.findJewelByLevel(ItemEnhance.CANT_REMOVE_LEVEL)){
					ErrorHandler.sendErrorMessage(session, -1,
							OpCode.REMOVEITEM_CLIENT, peony.Messages.STRING_00311);
					return;
				}
			}
			PlayerTransaction tx = player.newTransaction("DEL");
			TransactionBagGrid grid = null;
			if ((grid = player.bag.removeGridGameItem(gridId, itemId,
					instanceId, count, tx, false)) != null) {
				ItemEffect itemEffect = grid.item.template.useType.effect;
				if (itemEffect != null
						&& (itemEffect instanceof RideItemEffect)) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, -1,
							OpCode.REMOVEITEM_CLIENT, peony.Messages.STRING_00312);
				} else if(grid.item.template.itemType==Item.TYPE_JEWEL && grid.item.template.useLevel>=6){
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, -1,
							OpCode.REMOVEITEM_CLIENT, peony.Messages.STRING_00312);
				} else if(grid.item.template.name.equals("6级宝石如意袋") || grid.item.template.name.equals("7级宝石如意袋")){
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, -1,
							OpCode.REMOVEITEM_CLIENT, peony.Messages.STRING_00312);
				} else {
					tx.commit();
				}
			} else {
				tx.rollback();
			}
			// player.bag.removeItemByGrid(gridId, count,false);
		}
	}

	public void splitItem(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int gridId = packet.get() & 0xFF;
			int itemId = packet.getInt();
			int count = packet.get();
			try {
				ChangedItem[] changes = player.bag.splitGridGameItem(gridId, itemId, count);
				for (ChangedItem citem : changes) {
					player.changed.addChangedItem(citem);
				}
			} catch (NoEnoughSpaceException ne) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.SPLITITEM_CLIENT, peony.Messages.STRING_00313);
			} catch (Exception e) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.SPLITITEM_CLIENT, peony.Messages.STRING_00314);
			}
		}
	}

	public void bag(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			player.sendBagInfo();
		}
	}

	public void ctnGet(Packet packet, ClientSession session) {
		int instanceId = packet.getInt();
		Player player = (Player) session.getClient();
		Account account = (Account) session.getIdentity();
		if (player != null) {
			GameObject object = ObjectAccessor.getGameObject(instanceId);
			if (object != null && object.type == GameObject.TYPE_CREATURE) {
				Creature c = (Creature) object;
				byte[] data = c.npcImage.getCTNData(account.getModel());
				Packet pt = new Packet(OpCode.CTNGET_SERVER);
				pt.putInt(c.instanceId);
				pt.putInt(c.npcImage.getID());
				pt.put(data);
				session.send(pt);
			}
		}
	}

	public void questFinished(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int questId = packet.getInt();
		int branchId = packet.getInt();
		Player player = (Player) session.getClient();
		int ret = player.asmVm.finishQuest(questId, branchId);
		
		// 记录日志
		LogUtil.logFinishQuest(player, questId, branchId, ret);
		
		//记录夫妻任务日志
		LogUtil.logFinishMarriageQuest(player, questId, branchId, ret);
		
		//记录师徒任务日志
		LogUtil.logFinishApprenticeQuest(player, questId, branchId, ret);
		
		//记录结义任务
		LogUtil.logFinishAssociationQuest(player, questId, branchId, ret);
		
		//记录奇遇任务
		LogUtil.logFinishQiYuQuest(player, questId, branchId, ret);
		
		//记录跑环任务
		LogUtil.logFinishCycleQuest(player, questId, branchId, ret);
		
		//记录驯兽任务
		LogUtil.logFinishXunshouQuest(player, questId, branchId, ret);
		
		//记录斗酒大会任务
		LogUtil.logFinishDoujiuQuest(player, questId, branchId, ret);
		
		if (ret != 0) {// 0 成功 1 包格不够 2 没有指定分支 3 没有指定任务 4 不能完成任务
			String message = "";
			if (ret == 1) {
				message = peony.Messages.STRING_00315;
			} else if (ret == 2) {
				message = peony.Messages.STRING_00316;
			} else if (ret == 3) {
				message = peony.Messages.STRING_00317;
			} else if (ret == 4) {
				message = peony.Messages.STRING_00318;
			}
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.QUEST_FINISH_CLIENT, message);
		} else {
			// 统计
			Server.server.getServiceRegistry().getRealtimeStatService().questCounter++;
		}
	}

	public void loadingFinished(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if(player!=null){
			player.loadFinished();
			processLazyData(player);
			player.refreshStar7Buff();
		}
	}
	
	public void loadingFinished1(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if(player!=null){
			player.loadFinished1();
			loadLogoutHPMP(player);
		}
	}
	
	/**恢复下线时的血蓝*/
	public void loadLogoutHPMP(Player player){
		player.logoutHp = player.pool.getInt("PROPERTY_LOGOUTHP", 0);
		player.logoutMp = player.pool.getInt("PROPERTY_LOGOUTMP", 0);
		if(player.hp<=player.logoutHp){
			player.setHp(player.logoutHp, false);
		}
		if(player.mp<=player.logoutMp){
			player.setMp(player.logoutMp, false);
		}
		player.pool.setInt("PROPERTY_LOGOUTHP", 0);
		player.pool.setInt("PROPERTY_LOGOUTMP", 0);
	}
	
	protected void processLazyData(Player player){
		if(player!=null){
			int lastHorseInstId = player.pool.getInt(Player.PROPERTY_LAST_HORSE_INSTANCEID);
			if(lastHorseInstId != 0){
				if(player.horse == null || (player.horse != null && player.horse.instanceId != lastHorseInstId)){
					player.horse = null;
					player.horseRide(lastHorseInstId, 0,-1);
					try {
						boolean notify = false;
						boolean notify_1 = false;
						for(GameItem item : player.horse.equs.equs){
							try {
								int duration = item.duration;
								int maxDuration = 0;
								try{maxDuration = item.template.equipment.duration;}catch(Exception e){}
								if(maxDuration>0 && duration==0){
									notify_1 = true;
									break;
								} else if(maxDuration>0 && duration<maxDuration*0.1){
									notify = true;
									break;
								}
							} catch (Exception e) {
							}
						}
						if(notify_1){
							Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, "您有坐骑装备耐久已经变成0了，实力大幅削减，请赶快修理。");
						}else if(notify){
							Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, "您有坐骑装备该修理了，还请英雄尽快修理，若耐久为0后您的实力可是会大打折扣的呢。");
						}
					} catch (Exception e) {
					}
				}
			}
			int lastAttendantId = player.pool.getInt(Player.PROPERTY_LAST_ATTENDANT_INSTANCEID);
			if(lastAttendantId != 0){
				if(player.attendant == null){
					Attendant attendant = player.attendantBag.getAttendant(lastAttendantId);
					if(attendant!=null){
						attendant.follow();
						//上线时处理玩家身上的随从相关特殊buff
						Server.server.getServiceRegistry().getAttendantFixService()
				        .playerAddSpecialBuff(player);
					}
				}
			}
			if(player!=null){
				for(Buff buff : player.buffs.getBuffs()){
					if(buff!=null && buff instanceof UnitEffect){
						((UnitEffect)buff).effect(player);
					}
				}
			}
			loadLogoutHPMP(player);
		}
	}

	public void questAccept(Packet packet, ClientSession session) {
		int id = packet.getInt();
		Player player = (Player) session.getClient();
		player.asmVm.pending(id);
	}

	public void questPreDesc(Packet packet, ClientSession session) {
		int id = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			ASMQuest quest = ASMQuestUtil.getQuest(id);
			String preDesc = quest.getPreDesc(player.asmVm);
			Packet pt = new Packet(OpCode.QUEST_PREDESC_SERVER);
			pt.putInt(id);
			pt.putString(preDesc);
			writeQuestRewardSets(pt, quest.getGameQuest(), player);
			session.send(pt);
		}
	}

	public void questDesc(Packet packet, ClientSession session) {
		int id = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			ASMQuest quest = ASMQuestUtil.getQuest(id);
			if (quest != null) {
				Packet pt = new Packet(OpCode.QUEST_DESC_SERVER);
				pt.putInt(id);
				pt.putString(quest.getDesc(player.asmVm));
				writeQuestRewardSets(pt, quest.getGameQuest(), player);
				session.send(pt);
			} else {
				// todo 非法请求
			}
		}
	}

	public static void writeQuestRewardSets(Packet pt, GameQuest quest, Player player) {
		List<QuestRewardSet> ls = quest.getRewardSets();
		int size = ls.size();
		boolean cycleTail = false;
		if(quest.getCycleInfo() != null && quest.getCycleInfo().type==GameQuest.CycleInfo.TYPE_TAIL){
			size ++;
			cycleTail = true;
		}
		pt.put(size);
		for (QuestRewardSet rewardSet : ls) {
			pt.put((byte) rewardSet.getID());
			List<QuestRewardItem> items = rewardSet.rewardItems;
			pt.put((byte) items.size());
			for (QuestRewardItem item : items) {
				pt.put((byte) item.rewardType);
				if (item.rewardType == QuestRewardItem.REWARD_ITEM) {
					pt.putInt(item.itemCount);
					ItemTemplate template = ObjectAccessor
							.getItemTemplate(item.rewardValue);
//					pt.put(GameItem.toClientBytes(template));
					pt.put(Server.server.getServiceRegistry().getQuestRewardService().toClientBytes(quest.getId(), template));
				} else if (item.rewardType == QuestRewardItem.REWARD_EXP) {
					// 任务奖励经验要考虑级别惩罚
					int exp = ExpRewardEntry.calcExp(quest, item.rewardValue,
							player);
					pt.putInt(exp);
				} else {
					pt.putInt(item.rewardValue);
				}
			}
		}
		if(cycleTail){
			ItemTemplate template = player.asmVm.getCycleReward();
			pt.put(size);
			pt.put(1);
			pt.put(QuestRewardItem.REWARD_ITEM);
			pt.putInt(1);
//			pt.put(GameItem.toClientBytes(template));
			pt.put(Server.server.getServiceRegistry().getQuestRewardService().toClientBytes(quest.getId(), template));
		}
	}

	public void questUnFinishDesc(Packet packet, ClientSession session) {
		int id = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			ASMQuest quest = ASMQuestUtil.getQuest(id);
			if (quest != null) {
				Packet pt = new Packet(OpCode.QUEST_UNFINISHDESC_SERVER);
				pt.putInt(id);
				pt.putString(quest.getUnFinishDesc(player.asmVm));
				writeQuestRewardSets(pt, quest.getGameQuest(), player);
				session.send(pt);
			} else {
				// todo 非法请求
			}
		}
	}

	public void questPostDesc(Packet packet, ClientSession session) {
		int id = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			ASMQuest quest = ASMQuestUtil.getQuest(id);
			Packet pt = new Packet(OpCode.QUEST_POSTDESC_SERVER);
			pt.putInt(id);
			pt.putString(quest.getPostDesc(player.asmVm));
			writeQuestRewardSets(pt, quest.getGameQuest(), player);
			session.send(pt);
		}
	}

	public void touchNpc(Packet packet, ClientSession session) {
		int instanceId = packet.getInt();
		int questId = packet.getInt();
		Player player = (Player) session.getClient();
		GameObject npc = ObjectAccessor.getGameObject(instanceId);
		if (player != null&&npc != null) {
			if (npc.type == GameObject.TYPE_CREATURE
					&& npc.map.map == player.getVMap()) {
				if (player.distance(npc.x, npc.y) < 15000) {
					if(npc.faction==GameObject.FACTION_NEUTRAL || npc.faction==player.faction)
						player.touchNpc(npc, questId);
				} else {
					ErrorHandler.sendErrorMessage(session, -1,
							OpCode.TOUCHNPC_CLIENT, peony.Messages.STRING_00319);
					return;
				}
			}
			Server.server.getServiceRegistry().getClientGuidService().recordTouchNpc(npc.id, player);
			Server.server.getServiceRegistry().getDirectoryService().recordTouchNpc(npc.id, player);
		}
	}

	// public void animateGet(Packet packet,ClientSession session) throws
	// Exception {
	// String animateName = packet.getString();
	// byte[] bytes = stageService.findImageFile(animateName);
	// if(bytes!=null){
	// Packet pt = new Packet(OpCode.ANIMATEGET_SERVER);
	// pt.putString(animateName);
	// pt.put(bytes);
	// session.send(pt);
	// }
	// }

	public void touchExit(Packet packet, ClientSession session, int diff)
			throws IOException {
		int serial = packet.getInt();
		int time = packet.getInt();
		int exitId = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null && player.isAlive()) {
			if (!player.acceptMoving) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.TOUCHEXIT_CLIENT, peony.Messages.STRING_00319);
				return;
			}
			// if((Time.currTime-player.lastPosition.time)>10000&&diff<=5000){
			// ErrorHandler.sendErrorMessage(session, serial,
			// OpCode.TOUCHEXIT_CLIENT, "距离太远");
			// return;
			// }
			GameMapExit exit = player.getVMap().getExit(exitId);
			if (exit != null && exit.exitType < GameMapExit.TYPE_INTERNAL) {
				if (player.distance(exit.x, exit.y) >= 10000) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.TOUCHEXIT_CLIENT, peony.Messages.STRING_00319);
					return;
				}
				VMap oldVMap = player.getVMap();
				if (player.party != null
						&& player.party.leader.player == player) {
					synchronized (player.party) {
						for (PartyMember member : player.party.members) { // 告诉所有队员企图切换关卡，用来处理跟随
							Player p = member.player;
							if (p.id != player.id && p.getVMap() == oldVMap
									&& p.systemState == Player.SYSTEMSTATE_READY) {
								Packet pt1 = new Packet(
										OpCode.PARTY_TOUCHED_EXIT_SERVER);
								pt1.putInt(exitId);
								p.send(pt1);
							}
						}
					}
				}
				try {
					// 检查是否有过地图的权限
					GameMapExitConstraints c = (GameMapExitConstraints) exit.constraints;
					if (!c.checkFaction(player.faction)) {
						throw new VMapException(peony.Messages.STRING_00320);
					}
					if (player.level < c.minLevel || player.level > c.maxLevel) {
						throw new VMapException(peony.Messages.STRING_00321);
					}
					ProjectData prj = Server.server.getServiceRegistry()
							.getDataService().data;
					if (c.minRank != -1) {
						if (player.getRank() < c.minRank) {
							Rank rank = (Rank) prj.findDictObject(Rank.class,
									c.minRank);
							throw new VMapException(MessageFormat.format(peony.Messages.STRING_00322, rank.title));
						}
					}
					if (!c.allowBattle && player.getThreatCount() > 0) {
						throw new VMapException(peony.Messages.STRING_00323);
					}
					if (c.requireQuest != -1
							&& player.asmVm.hasTask(c.requireQuest) == 0) {
						Quest quest = (Quest) prj.findObject(Quest.class,
								c.requireQuest);
						throw new VMapException(MessageFormat.format(peony.Messages.STRING_00324, quest.title));
					}
					if (c.requireFinishQuest != -1
							&& player.asmVm.taskFinished(c.requireFinishQuest) == 0) {
						Quest quest = (Quest) prj.findObject(Quest.class,
								c.requireFinishQuest);
						throw new VMapException(MessageFormat.format(peony.Messages.STRING_00325, quest.title));
					}
					if (c.requireProperty.length() > 0) {
						if (player.asmVm.getGlobalValue(c.requireProperty) >= c.requirePropertyValue) {
							throw new VMapException(peony.Messages.STRING_00326);
						}
					}

					// 处理哪儿来哪儿去的出口
					int nextmap = exit.targetMap;
					int nextx = exit.targetX;
					int nexty = exit.targetY;
					if (exit.exitType == GameMapExit.TYPE_RECORD) {
						if (exit.positionVarName != null
								&& exit.positionVarName.length() > 0) {
							// 至少保证玩家位置和出口位置距离有32，否则按玩家位置和出口位置的相对角度，反向移动像素，
							// 以避免循环传送
							int enterX = player.x;
							int enterY = player.y;
							int exitX = exit.x;
							int exitY = exit.y;
							double dist = Math.sqrt((exitX - enterX)
									* (exitX - enterX) + (exitY - enterY)
									* (exitY - enterY));
							double ratio = (32 - dist) / dist;
							if (ratio > 0) {
								enterX += (player.x - exit.x) * ratio;
								enterY += (player.y - exit.y) * ratio;
							}
							player.pool
									.setString(exit.positionVarName,
											player.map.id + "," + enterX + ","
													+ enterY);
						}
					} else if (exit.exitType == GameMapExit.TYPE_RECALL) {
						if (exit.positionVarName != null
								&& exit.positionVarName.length() > 0) {
							String saveValue = player.pool
									.getString(exit.positionVarName);
							String[] secs = Utils.splitString(saveValue, ',');
							if (secs.length == 3) {
								try {
									nextmap = Integer.parseInt(secs[0]);
									nextx = Integer.parseInt(secs[1]);
									nexty = Integer.parseInt(secs[2]);
								} catch (Exception e) {
									nextmap = exit.targetMap;
									nextx = exit.targetX;
									nexty = exit.targetY;
								}
							}
						}
					}

					// 下发小提示
					String hint = Server.server.getServiceRegistry()
							.getDataService().getHint(player);
					String model = (player.getAccount()==null ? "" : player.getAccount().getModel());
					if (hint != null) {
						if(!"Lenovo".equals(model) && !"LenovoU1".equals(model)){
							Packet pt1 = new Packet(OpCode.PUSH_HINT_SERVER);
						    pt1.putString(hint);
						    session.send(pt1);
						}
					}
					
					//玩家出新手村地图
					LogUtil.logTouchExitToOutVMap(player);
					player.enterMap = System.currentTimeMillis();
					
					// 请求加入新场景
					player.unMoving();
					TouchExitTransferCall call = new TouchExitTransferCall(player, session, serial, exitId, nextmap, nextx, nexty);
					Server.server.getWorld().schedule(call);
				} catch (VMapException e) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.TOUCHEXIT_CLIENT, e.getMessage());
					log.error("[TOUCHEXITERROR]ID[" + player.id + "]EXIT[" + exitId + "]");
				}
			}
		}
	}

	protected void skillAttack(Packet packet, ClientSession session) {
		int time = packet.getInt();
		int x = packet.getShort();
		int y = packet.getShort();
		byte direct = packet.get();
		int instanceId = packet.getInt();
		int skillId = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			LogUtil.logTrackPlayerLog(player, x, y, skillId);
			if(Server.isStepServer){
				log.info("[SKILLATTACK]ID["+player.id+"]SKILL["+skillId+"]TRY");
			}
			if (player.isAlive()) {
				if (!player.acceptMoving) {
					Packet pt = new Packet(OpCode.ATTACK_FAIL_SERVER);
					pt.put(1); // 如果不能接收move包，那么就返回距离太远
					pt.putInt(player.instanceId);
					pt.putInt(instanceId);
					pt.putInt(skillId);
					session.send(pt);
					if(Server.isStepServer){
						log.info("[SKILLATTACK]ID["+player.id+"]SKILL["+skillId+"]FAIL-1");
					}
					return;
				}
				player.lastAttackTime = time;
				player.lastMoveTime = time;
				// todo
				// player.move
				// player.x = x;
				// player.y = y;
				player.direct = direct;
				int t = Time.currTime - time;
				if (t < 0)
					t = 0;
				// log.debug("delayTime:" + t);
				GameObject target = ObjectAccessor.getGameObject(instanceId);
				if(target!=null && target instanceof Player){
					Player targetPlayer = (Player)target;
					if(!Attack.canAttack(player.map.id, Server.server.revision) && targetPlayer.id != player.id){
						Packet pt = new Packet(OpCode.ATTACK_FAIL_SERVER);
						pt.put(11);
						pt.putInt(player.instanceId);
						pt.putInt(instanceId);
						pt.putInt(skillId);
						session.send(pt);
						if(Server.isStepServer){
							log.info("[SKILLATTACK]ID["+player.id+"]SKILL["+skillId+"]FAIL-11");
						}
						return;
					}
				}
				if(player.asyncMapInstanceId!=0)
					player.prepareSkillAttack(instanceId, skillId, t * 2, Attack.ATTACK_TYPE_ASYNC_TARGET);
				else
					player.prepareSkillAttack(instanceId, skillId, t * 2, 0);
				if(Server.isStepServer){
					log.info("[SKILLATTACK]ID["+player.id+"]SKILL["+skillId+"]OK");
				}
			} else { // 死亡后不能使用技能
				Packet pt = new Packet(OpCode.ATTACK_FAIL_SERVER);
				pt.put(11);
				pt.putInt(player.instanceId);
				pt.putInt(instanceId);
				pt.putInt(skillId);
				session.send(pt);
				if(Server.isStepServer){
					log.info("[SKILLATTACK]ID["+player.id+"]SKILL["+skillId+"]FAIL-11");
				}
			}
		}
	}

	protected void unRide(Packet packet, ClientSession session) {
		int time = packet.getInt();
		int x = packet.getInt();
		int y = packet.getInt();
		byte direct = packet.get();
		Player player = (Player) session.getClient();
		if (player != null) {
			player.move(x, y, direct, time);
			// player.lastMoveTime = time;
			// player.move(x, y);
			// player.x = x;
			// player.y = y;
			// player.direct = direct;
			player.unRide();
		}
	}

	protected void ride(Packet packet, ClientSession session) {
		int time = packet.getInt();
		int x = packet.getInt();
		int y = packet.getInt();
		byte direct = packet.get();
		Player player = (Player) session.getClient();
		if (player != null) {
			player.move(x, y, direct, time);
			// player.lastMoveTime = time;
			// player.move(x, y);
			// player.x = x;
			// player.y = y;
			// player.direct = direct;
			player.ride();
			// world.broadcastMove(player, this);
		}
		if (player != null) {

		}
	}

	protected void move(Packet packet, ClientSession session, int diff) {
		int time = packet.getInt();
		int x = packet.getShort();
		int y = packet.getShort();
		byte direct = packet.get();
		if (direct < 0 || direct > 3) {
			direct = 0;
		}
		short state = packet.getShort();
		int nextx = -1, nexty = -1;
		// try {
			// nextx = packet.getShort();
			// nexty = packet.getShort();
		// } catch (Exception e) {
		// }
		Player player = (Player) session.getClient();
		if (player != null && player.systemState == Player.SYSTEMSTATE_READY
				&& player.acceptMoving && player.isAlive()) {
			// 如果客户端传上来的时间比服务器时间还晚，视为异常
			int tt = Time.elapseTime(System.currentTimeMillis());
			if (time > tt + 6000) {
				log.error("[TIMEERROR]" + LogUtil.getPlayerLogString(player)
						+ "SERVER[" + tt + "]CLIENT[" + time + "]");
				player.addForbidScore(Player.TIME_ERROR_SCORE_3);
			} else if (time > tt + 4000) {
				log.error("[TIMEERROR]" + LogUtil.getPlayerLogString(player)
						+ "SERVER[" + tt + "]CLIENT[" + time + "]");
				player.addForbidScore(Player.TIME_ERROR_SCORE_2);
			} else if (time > tt + 2000) {
				log.error("[TIMEERROR]" + LogUtil.getPlayerLogString(player)
						+ "SERVER[" + tt + "]CLIENT[" + time + "]");
				player.addForbidScore(Player.TIME_ERROR_SCORE_1);
			}
			player.lastX = player.x;
			player.lastY = player.y;
			player.move(x, y, direct, state, time, diff, nextx, nexty);
            LogUtil.logMove(player);
			//处理随从跟随
			processAttendantFollow(player, direct);
		}
	}
	
	protected void processAttendantFollow(Player player, int direct){
		if(player!=null && player.attendant!=null){
			player.attendant.speed = player.getSpeed() * Attendant.SPEEDRATIO;
			int[] po = VMap.getAttendantPositon(direct, player);
			player.attendant.move(po[0], po[1]);
			player.attendant.moveType |= GameObject.MOVE_ALL;
		}
	}

	protected void syncTime(Packet packet, ClientSession session) {
		int clientTime = packet.getInt();
		Packet pt = new Packet(OpCode.SYNC_TIME_SERVER);
		pt.putInt(clientTime);
		pt.putInt(Time.elapseTime(System.currentTimeMillis()));
		session.send(pt);
	}

	protected void login(Packet packet, ClientSession session) throws Exception {
		int serial = packet.getInt();
		int id = packet.getInt();
		String MIEI = "";
		try {
			MIEI = packet.getString();
		} catch (Exception e) {
			
		}
		DBService dbService = Server.server.getServiceRegistry().getDbService();
		PlayerService playerService = Server.server.getServiceRegistry()
				.getPlayerService();
		World world = Server.server.getWorld();
		dbService.schedule(new PlayerLoadCall(playerService, session, id,
				world, serial, MIEI));
	}

	/*
	 * 请求添加好友/黑名单/仇人。
	 */
	private void addFriend(Packet packet, ClientSession session) {
		AddFriendCall call = new AddFriendCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	/**
	 * 请求更改关系人锁定状态
	 * @param packet
	 * @param session
	 */
	public void changeFriendLockState(Packet packet, ClientSession session){
		ChangeFriendLockCall call = new ChangeFriendLockCall(session,packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	/*
	 * 请求删除好友/黑名单/仇人。
	 */
	private void delFriend(Packet packet, ClientSession session) {
		DelFriendCall call = new DelFriendCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	/*
	 * 请求下载关联玩家列表。
	 */
	private void getFriendList(Packet packet, ClientSession session) {
		GetFriendListCall call = new GetFriendListCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	/*
	 * 请求创建军团。
	 */
	private void tongCreate(Packet packet, ClientSession session) {
		CreateTongCall call = new CreateTongCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	/*
	 * 请求军团成员列表。
	 */
	private void tongList(Packet packet, ClientSession session) {
		ListTongMemberCall call = new ListTongMemberCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	/*
	 * 邀请新成员。
	 */
	private void tongInvite(Packet packet, ClientSession session) {
		TongInviteCall call = new TongInviteCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	/*
	 * 请求加入军团。
	 */
	private void tongJoin(Packet packet, ClientSession session) {
		JoinTongCall call = new JoinTongCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	/*
	 * 拒绝加入邀请。
	 */
	private void tongReject(Packet packet, ClientSession session) {
		RejectInvitationCall call = new RejectInvitationCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	/*
	 * 请求退出军团。
	 */
	private void tongQuit(Packet packet, ClientSession session) {
		QuitTongCall call = new QuitTongCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	/*
	 * 请求修改军团公告。
	 */
	private void tongSetSlogan(Packet packet, ClientSession session) {
		SetTongSloganCall call = new SetTongSloganCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	/*
	 * 请求提升/降职/转让。
	 */
	private void tongPromote(Packet packet, ClientSession session) {
		TongPromoteCall call = new TongPromoteCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	/*
	 * 请求踢人出军团。
	 */
	private void tongKick(Packet packet, ClientSession session) {
		TongKickCall call = new TongKickCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	/*
	 * 请求禁言/取消禁言。
	 */
	private void tongForbid(Packet packet, ClientSession session) {
		TongForbidCall call = new TongForbidCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	public void disconnected(ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			player.logout();
			session.setClient(null);
		}
	}
}
