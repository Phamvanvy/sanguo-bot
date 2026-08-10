package peony.game;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import peony.auction.AuctionBuyCall;
import peony.auction.AuctionCreateCall;
import peony.auction.AuctionListCall;
import peony.auction.PublishiedCall;
import peony.db.ActivationCodeCall;
import peony.db.AlphaGiftGetCall;
import peony.db.DBService;
import peony.db.DeletePlayerCall;
import peony.db.GMCallCall;
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
import peony.db.NpcDescCall;
import peony.db.PlayerCreateCall;
import peony.db.PlayerInfoCall;
import peony.db.PlayerLoadCall;
import peony.db.PlayerRateCall;
import peony.db.PlayerRenameCall;
import peony.depot.DepotException;
import peony.depot.DepotService;
import peony.game.battlefield.FlagBattleFieldInstance;
import peony.game.battlefield.FlagBattleFieldVMapManager;
import peony.game.bbs.BbsContentCall;
import peony.game.bbs.BbsListCall;
import peony.game.beautyparade.BeautyListCall;
import peony.game.beautyparade.BeautySignUpCall;
import peony.game.beautyparade.BeautyVoteCall;
import peony.game.beautyparade.FindFriendListCall;
import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.game.buff.SlowDebuff;
import peony.game.changed.ChangedItem;
import peony.game.chat.ChatMessage;
import peony.game.chat.ChatService;
import peony.game.chinarun.ChinarunCall;
import peony.game.clientbbs.ClientBbsService;
import peony.game.convoy.ConvoyException;
import peony.game.exchange.Exchange;
import peony.game.exchange.ExchangeGrid;
import peony.game.exp.ExpException;
import peony.game.exp.ExpService;
import peony.game.file.FileData;
import peony.game.instance.BossScoreBoardCall;
import peony.game.instance.BossTimeScoreCall;
import peony.game.itemeffect.RideItemEffect;
import peony.game.itemenhance.AddHoleCall;
import peony.game.itemenhance.AddJewelCall;
import peony.game.itemenhance.AddMaxHoleCall;
import peony.game.itemenhance.AutoAddHole;
import peony.game.itemenhance.AutoNaturalEnhance;
import peony.game.itemenhance.GetJewelConfigCall;
import peony.game.itemenhance.ItemEnhance;
import peony.game.itemenhance.JewelService;
import peony.game.itemenhance.MergeJewelCall;
import peony.game.itemenhance.RemoveJewelCall;
import peony.game.map.TouchExitTransferCall;
import peony.game.nation.CandidateListCall;
import peony.game.nation.CandidateService;
import peony.game.nation.CandidateSignUpCall;
import peony.game.nation.CollectCall;
import peony.game.nation.ContributeCreditCall;
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
import peony.game.skill.Skill;
import peony.game.weather.Weather;
import peony.marriage.DivorceCall;
import peony.marriage.MarriageCall;
import peony.marriage.MarriageException;
import peony.marriage.MarriageInfoCall;
import peony.marriage.MarriageService;
import peony.marriage.WeddingInstance;
import peony.marriage.WeddingService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.net.PacketHandler;
import peony.produce.ProduceService;
import peony.service.ServiceEvent;
import peony.service.account.Account;
import peony.service.account.AccountChangePasswordCall;
import peony.service.account.AccountLoginCall;
import peony.service.account.AccountQuickRegisterCall;
import peony.service.account.AccountRegisterCall;
import peony.service.account.AccountRenameCall;
import peony.service.account.AccountService;
import peony.service.account.IBuyHistoryCall;
import peony.service.account.IMoneyCardCall;
import peony.service.account.PhoneNotifyCall;
import peony.service.account.adapter.AppStoreService;
import peony.service.account.adapter.QmePayCall;
import peony.service.account.adapter.QmeQueryBalanceCall;
import peony.service.account.cmcc.CmccChargeCall;
import peony.service.account.cmcc.CmccDownloadOkMessage;
import peony.service.accountbinding.AccountBindCall;
import peony.service.accountbinding.AccountBindStatusCall;
import peony.service.fame.FameService;
import peony.service.friend.AddFriendCall;
import peony.service.friend.DelFriendCall;
import peony.service.friend.GetFriendListCall;
import peony.service.friend.PlayerRelation;
import peony.service.friend.RelationService;
import peony.service.friend.RelationServiceException;
import peony.service.levellimit.LevelLimitService;
import peony.service.player.PlayerService;
import peony.service.shop.CmccBuyCall;
import peony.service.shop.CmccShopListCall;
import peony.service.shop.ShopBuyCall;
import peony.service.shop.ShopException;
import peony.service.shop.ShopListCall;
import peony.service.shop.ShopSellCall;
import peony.service.shop.ShopService;
import peony.service.stat.PvpInfo;
import peony.service.stat.StatService;
import peony.service.tong.CreateTongCall;
import peony.service.tong.JoinTongCall;
import peony.service.tong.ListTongMemberCall;
import peony.service.tong.QuitTongCall;
import peony.service.tong.RejectInvitationCall;
import peony.service.tong.RenameTongCall;
import peony.service.tong.SetTongSloganCall;
import peony.service.tong.Tong;
import peony.service.tong.TongContributeCall;
import peony.service.tong.TongForbidCall;
import peony.service.tong.TongInfoCall;
import peony.service.tong.TongInviteCall;
import peony.service.tong.TongKickCall;
import peony.service.tong.TongMember;
import peony.service.tong.TongPromoteCall;
import peony.service.tong.TongQuestCall;
import peony.service.tong.TongQuestRequestCall;
import peony.service.tong.TongService;
import peony.service.tong.TongSkillDescCall;
import peony.service.tong.TongSkillListCall;
import peony.service.tong.TongSkillStudyCall;
import peony.service.tong.apply.TongBattleApplyCall;
import peony.service.tong.apply.TongBattleApplyListCall;
import peony.service.tong.apply.TongBattleApplyService;
import peony.service.tong.apply.TongBattleBidCall;
import peony.service.tong.apply.TongBattleException;
import peony.service.tong.battle.TongBattleVMapManager;
import peony.service.worldmap.WorldMapService;
import peony.teleport.service.TeleportService;
import peony.util.IStringValidator;
import peony.util.StringUtil;
import peony.vm.ASMQuest;
import peony.vm.ASMQuestUtil;
import ch.javasoft.util.intcoll.IntHashMap;
import ch.javasoft.util.intcoll.IntMap.IntEntry;

import com.pip.sanguo.data.GiftGroup;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Rank;
import com.pip.sanguo.data.GiftGroup.GiftDef;
import com.pip.sanguo.data.item.Formula;
import com.pip.sanguo.data.map.GameMapExit;
import com.pip.sanguo.data.map.GameMapExitConstraints;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.quest.QuestRewardItem;
import com.pip.sanguo.data.quest.QuestRewardSet;
import com.pip.util.Utils;

public class PlayerPacketHandler implements PacketHandler {
	private static final Logger log = Logger
			.getLogger(PlayerPacketHandler.class);
	protected Random rnd = new Random();

	public PlayerPacketHandler() {
	}

	public void handle(Packet packet, ClientSession session, int diff)
			throws Exception {
		short opCode = packet.getOpCode();
		// log.debug("receive client message:"+opCode);
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
		case OpCode.DECORATE_ADD_MAX_HOLE_CLIENT:
			addMaxHole(packet, session);
			break;
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
		case OpCode.AGENTHORSE_LIST_CIENT:
			agentHorseList(packet, session);
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
		case OpCode.APP_STORE_CHARGE_CLIENT:
			appStoreCharge(packet, session);
			break;
		case OpCode.APP_STORE_LIST_PRODUCT_CLIENT:
			appStoreListProduct(packet, session);
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
		}
	}
	
	/** 快速注册选择性别职业国籍 */
	protected void changePlayerInfo(Packet packet,ClientSession session){
		int serial = packet.getInt();
		short info = packet.getShort();
		int sex = (info & 0x1f) - 1;
		int clazz = ((info >> 5) & 0x1f) - 1;
		int faction = ((info >> 10) & 0x1f) - 1;
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
				GameItem equ = ObjectAccessor
						.createGameItem(PlayerUtil.INIT_EQUIPMENT[clazz]);
				gain.addGainItem(equ, 1);
				PlayerTransaction tx = player.newTransaction("CCL");
				boolean ok = player.bag.addGain(gain, tx, false);
				tx.commit();
				if (ok) {
					player.equip(equ.template.id, equ.instanceId, -1);
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
				player.pool.setString("離開新手村", PlayerUtil.CREATE_POINT[player.faction - 1][0] + "," + PlayerUtil.CREATE_POINT[player.faction - 1][1] + "," + PlayerUtil.CREATE_POINT[player.faction - 1][2]);
			}
			Packet pt = new Packet(OpCode.CHANGE_PLAYER_INFO_SERVER);
			pt.putInt(serial);
			player.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.CHANGE_PLAYER_INFO_CLIENT, "您不能修改基本信息");
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
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_GIFT_CLINT, "只有新郎才可以發放紅包");
				return;
			}
			Player targetPlayer = ObjectAccessor.getPlayer(targetId);
			if(targetPlayer == null || targetPlayer.map.map.instance==null || 
					targetPlayer.map.map.instance.getId() != instance.getId()){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_GIFT_CLINT, "對方不在線或者不在本地圖");
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
	    int serial = packet.getInt();
	    Player p = (Player)session.getClient();
	    if(p!=null){
	    	WeddingService service = Server.server.getServiceRegistry().getWeddingService();
	    	List<Actor> actors = service.getSignIns(p);
	    	RelationService rs = Server.server.getServiceRegistry().getRelationService();
	    	TongService tongService = Server.server.getServiceRegistry().getTongService();
	    	WeddingInstance instance = (WeddingInstance)p.map.map.instance;
	    	PlayerRelation relationMan = rs.get(instance.man.id);
	    	PlayerRelation relationWoman = rs.get(instance.woman.id);
	    	Tong tmMan = tongService.getPlayerTong(instance.man.id);
	    	Tong tmWoman = tongService.getPlayerTong(instance.woman.id);
	    	byte type;
	    	Packet pt = new Packet(OpCode.WEDDING_SIGNINLIST_SERVER);
	    	pt.putInt(serial);
	    	pt.putInt(actors == null?0:actors.size());
	    	if(actors != null){
		    	for(Actor actor:actors){
		    		byte isFetch = 0;
		    		if(instance.getgift.contains(new Integer(actor.id))){
		    			isFetch = 1;
		    		}
		    		Tong tm2 = tongService.getPlayerTong(actor.id);
					type = 0;
		    		if(relationMan.friends.exists(actor.id) || relationWoman.friends.exists(actor.id)){
		    			type |= 1;
		    		}
		    		if(tm2!=null && ((tmMan!=null && tmMan.id == tm2.id) || (tmWoman!=null && tmWoman.id == tm2.id))){
		    			type |= 2;
		    		} 
		    		pt.putInt(actor.id);
		    		pt.putString(actor.name);
		    		pt.put(type);
		    		pt.put(isFetch);
		    	}
	    	}
	    	p.send(pt);
	    }
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
		int level = packet.getShort();
		int guestLevel = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
			int mateId = Server.server.getServiceRegistry().getRelationService().get(p.id).mateId;
			if(mateId<=0){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_OPEN_CLIENT, "請先結婚");
				return;
			}
			Player mate = ObjectAccessor.getPlayer(mateId);
			if(mate==null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_OPEN_CLIENT, "配偶不在線");
				return;
			}
			if(p.sex!=0){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_OPEN_CLIENT, "只有新郎才可以開啟婚禮");
				return;
			}
			if(p.pool.getLong(WeddingService.MANOPENWEDDINGTIME,0L)>System.currentTimeMillis()-WeddingService.ONEWEEK
					|| mate.pool.getLong(WeddingService.WOMANOPENWEDDINGTIME,0L)>System.currentTimeMillis()-WeddingService.ONEWEEK){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_OPEN_CLIENT, "一周以后才可以再次開啟婚禮");
				return;
			}
			if(p.money < (2*level - 1)*1000000){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_OPEN_CLIENT, "您的金錢不足");
				return;
			}
			if(p.map.id != mate.map.id){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_OPEN_CLIENT, "新郎新娘在同一場景才可以開啟婚禮");
				return;
			}
			if(p.party == null || mate.party == null || !p.party.contains(mate.id)){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WEDDING_OPEN_CLIENT, "新郎新娘組隊后才可以開啟婚禮");
				return;
			}
			
			WeddingService service = Server.server.getServiceRegistry().getWeddingService();
		    try {
				service.createInstance(p.sex==0 ? p : mate, mate.sex==1 ? mate : p,level,guestLevel);
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
		service.getClientBbs(packet, session);
	}
	protected void lookOverBBS(Packet packet,ClientSession session){
		ClientBbsService service=Server.server.getServiceRegistry().getClientBbsService();
		service.sendBbs(packet, session);
	}
	protected void achievementList(Packet packet,ClientSession session){
		StatService service=Server.server.getServiceRegistry().getStatService();
		service.achievementList(packet, session);
	}
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
				ErrorHandler.sendErrorMessage(session, serial, OpCode.AUTO_ADDHOLE_CLIENT, "沒有找到裝備");
				return;
			}else{
				GameItem item = (GameItem) os[0];
				if(!item.template.isEquipment()){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.AUTO_ADDHOLE_CLIENT, "打孔目標必須是一件裝備");
					return;
				}
				if (item.object == null) {
					item.object = new ItemEnhance();
				}
				if (!(item.object instanceof ItemEnhance)) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.AUTO_ADDHOLE_CLIENT, "裝備數据錯誤");
					return;
				}
				ItemEnhance itemEnhance = (ItemEnhance) item.object;
				int initHole = item.template.equipment.initHole;
				int addHole = itemEnhance.addHole;
				int currentHoles = addHole + initHole;
				int maxHoles = itemEnhance.addMaxHole + item.template.equipment.maxHole;
				if (currentHoles >= maxHoles) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.AUTO_ADDHOLE_CLIENT,"已達到最大孔數");
					return;
				}
				if(maxHoles-currentHoles < wantHole || wantHole>maxHoles){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.AUTO_ADDHOLE_CLIENT, "請選擇正确的孔數");
					return;
				}
				List<ItemTemplate> l = js.getAddHoleItem(item.template.useLevel);
				if(l.size()==0){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.AUTO_ADDHOLE_CLIENT,"沒有找到對應的打孔符");
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
				ErrorHandler.sendErrorMessage(session, serial, OpCode.EXTEND_BAG_CLIENT, "操作正在進行中");
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
				desc = "人物不存在";
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
			Tong tong = tongService.getPlayerTong(p.id);
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
			Tong tong = tongService.getPlayerTong(p.id);
			TongBattleApplyService applyService = Server.server.getServiceRegistry().getTongBattleApplyService();
			if(tong!=null && applyService.isWinner(tong.id)){
				if(p.map.getId()!=applyService.getWinnerMapId(tong.id)){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_GETEXP_CLIENT, "不屬于此城池胜利軍團,不能領取經驗");
					return;
				}
				if(p.pool.getInt(Player.PROPERTY_TONGBATTLE_EXPDAY,0)==Time.day){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_GETEXP_CLIENT, "每天只可領取一次");
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
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_GETEXP_CLIENT, "不屬于胜利軍團,不能領取經驗");
			}
		}
	}
	
	protected void tongBattleAbandon(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			TongService tongService = Server.server.getServiceRegistry().getTongService();
			Tong tong = tongService.getPlayerTong(p.id);
			if(tong!=null){
				TongBattleApplyService applyService = Server.server.getServiceRegistry().getTongBattleApplyService();
				try {
					if(!tong.getChairmanName().equals(p.name)){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_ABANDON_CLIENT, "您沒有權限");
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
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_ABANDON_CLIENT, "你還沒有加入軍團");
			}
		}
	}
	
	protected void tongBattleUnTag(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int playerId = packet.getInt();
			TongService tongService = Server.server.getServiceRegistry().getTongService();
			Tong tong = tongService.getPlayerTong(p.id);
			if(tong!=null && tong.getChairmanName().equals(p.name)){
				TongMember tm = tongService.getPlayerInfo(playerId);
				tm.battleTag = 0;
				Packet pt = new Packet(OpCode.TONG_BATTLE_UNTAG_SERVER);
				pt.putInt(serial);
				p.send(pt);
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_UNTAG_CLIENT, "您不是都督不能行使此權利");
			}
		}
	}
	
	protected void tongBattleMakeTax(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int tax = packet.getByte();
			TongService tongService = Server.server.getServiceRegistry().getTongService();
			TongBattleApplyService service = Server.server.getServiceRegistry().getTongBattleApplyService();
			Tong tong = tongService.getPlayerTong(p.id);
			if(tong!=null && tong.getChairmanName().equals(p.name) && service.isWinner(tong.id)){
				if(p.map.getId()!=service.getWinnerMapId(tong.id)){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_MAKETAX_CLIENT, "沒有權利操作此項");
					return;
				}
				if(tax>10 || tax<5){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_MAKETAX_CLIENT, "稅率必須在5-10之間");
				}else{
					tong.taxRate = tax / 100f;
					Packet pt = new Packet(OpCode.TONG_BATTLE_MAKETAX_SERVER);
					pt.putInt(serial);
					p.send(pt);
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_MAKETAX_CLIENT, "沒有權利操作此項");
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
			pt.putString(winTongName+"\t軍團都督:<cFF0000>"+tong.getChairmanName()+"</c>\t軍團等級:<cFC00FF>"+tong.level+"</c>\t城戰紀錄:<cFC00FF>"+tong.pool.getInt(Tong.PROPERTY_TONGBATTLE_WIN, 0)
					+"</c>\t軍團人數:<cFF0000>"+tong.members.size()+"</c>\t軍團資金:<cFF0000>"+tong.money+"</c>\t競价价格:<cFC00FF>"+appService.getApplyByTongId(tong.id).money+"</c>");
			p.send(pt);
		}
	}
	
	protected void tongBattleTag(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int playerId = packet.getInt();
			TongBattleApplyService applyService = Server.server.getServiceRegistry().getTongBattleApplyService();
			try {
				applyService.tagTongBattle(p, playerId);
				Packet pt = new Packet(OpCode.TONG_BATTLE_TAG_SERVER);
				pt.putInt(serial);
				p.send(pt);
			} catch (TongBattleException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_TAG_CLIENT, e.getMessage());
			}
		}
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
	protected void appStoreCharge(Packet packet, ClientSession session) {
		AppStoreService service = (AppStoreService)Server.server.getServiceRegistry().getService(AppStoreService.class);
		if (service != null) {
			Player p = (Player)session.getClient();
			if (p != null) {
				service.checkReceipt(session, p.id, p.accountId, packet.getBytes());
			}
		}
	}
	
	/*
	 * 客户端查询AppStore可用商品列表。
	 */
	protected void appStoreListProduct(Packet packet, ClientSession session) {
		AppStoreService service = (AppStoreService)Server.server.getServiceRegistry().getService(AppStoreService.class);
		if (service != null) {
			Player p = (Player)session.getClient();
			if (p != null) {
				service.listProduct(session);
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
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WORLD_SHOUT_CLIENT, "不滿20級不能世界喊話");
				return;
			}
			PlayerTransaction tx = p.newTransaction("CHAT");
			GameItem gameItem = p.bag.removeGameItem(499, -1, 1, tx, true);
			if(gameItem!=null){
				tx.commit();
				ChatService chatService = Server.server.getServiceRegistry().getChatService();
				chatService.sendWorldShout(p.name,p.id,p.faction, content, 0xff4700, 11000);
				Packet pt = new Packet(OpCode.WORLD_SHOUT_SERVER);
				pt.putInt(serial);
				p.send(pt);
				Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_USEITEM,p,499));
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
					ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_SKILL_ITEM_CLIENT, "沒有可領取的物品");
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_SKILL_ITEM_CLIENT, "沒有此科技");
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
						, "沒有找到該裝備");
				return;
			}
			GameItem gameItem = (GameItem)o[0];
			if(!gameItem.isBound()){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_EQUIPMENT_UNBIND_CLIENT
						, "此裝備處于非綁定狀態,不可解綁");
			}else if(gameItem.bindInstance==0){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_EQUIPMENT_UNBIND_CLIENT
						, "此裝備已經解綁");
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
							, MessageFormat.format("您沒有{0},不能解綁", ObjectAccessor.getItemTemplate(491).name));
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
			if(p.horseBag.maxSize>=10){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSEBAG_EXTEND_CLIENT
						, "您的坐騎欄達到了最大數量了,不能再擴大了.");
				return;
			}
			PlayerTransaction tx = p.newTransaction("HBE");
			try {
				p.decMoney(p.horseBag.getExtendHorsebagMoney(), tx, true);
				p.horseBag.maxSize++;
				tx.commit();
				if(p.horseBag.maxSize==10){
					Server.server.getServiceRegistry().getChatService()
					.sendPrivateMessage(p.id, "恭喜您擁有了10個坐騎欄.");
				}else{
					Server.server.getServiceRegistry().getChatService()
					.sendPrivateMessage(p.id, 
							MessageFormat.format("您成功擴大了坐騎欄能容納更多坐騎,目前擁有{0}個還有{1}個可擴展.", 
									p.horseBag.maxSize,(10-p.horseBag.maxSize)));
				}
				Packet pt = new Packet(OpCode.HORSEBAG_EXTEND_SERVER);
				pt.putInt(serial);
				p.send(pt);
				LogUtil.logExtendHorseBag(p);
			} catch (NoEnoughValueException e) {
				tx.rollback();
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSEBAG_EXTEND_CLIENT, "金錢不足");
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
					ErrorHandler.sendErrorMessage(session, serial, OpCode.EQUIPEMENT_MARK_CLIENT, "此裝備不能刻字");
					return;
				}
				if(item.template.equipment.markCharCount < msg.length()){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.EQUIPEMENT_MARK_CLIENT, MessageFormat.format("此裝備最多可刻{0}個字", item.template.equipment.markCharCount));
					return;					
				}
				ItemEnhance ie = (ItemEnhance)item.object;
				if(ie != null){
					if(ie.getMarkString().length()>0){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.EQUIPEMENT_MARK_CLIENT, "裝備只能刻字一次");
						return;	
					}
				}
				if(StringUtil.isValidText(msg) != IStringValidator.OK || StringUtil.hasBadWord(msg)){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.EQUIPEMENT_MARK_CLIENT, "刻字中帶有非法字符!");
					return;	
				}
				if(ie == null){
					ie = new ItemEnhance();
					item.object = ie;
					
				}
				ie.setMarkString(MessageFormat.format("{0}\n{1}鑄", msg,player.name));
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
		if(player != null){
			if(player.isKing()==1){
				Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(player.faction);
				try {
					Server.server.getServiceRegistry().getNationConvoyService().startConvoy(nation);
					Packet pt = new Packet(OpCode.NATION_CONVOY_SERVER);
					pt.putInt(serial);
					player.send(pt);
				} catch (ConvoyException e) {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_CONVOY_CLIENT, e.getMessage());
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_CONVOY_CLIENT, "只有國公能夠發起國家押鏢");
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
									MessageFormat.format("國公{0}施仁政,下調稅率,可去國家信息菜單查詢", player.name));
							nation.pool.setInt(Nation.PROPERTY_TAX_DAY, Time.day);
						}else if(oldRate<nation.taxRate){
							Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(player.faction,
									MessageFormat.format("因國庫吃緊,國公{0}發布新的稅率,可去國家信息菜單查詢", player.name));
							nation.pool.setInt(Nation.PROPERTY_TAX_DAY, Time.day);
						}
						Packet pt = new Packet(OpCode.KING_TAXRATE_SERVER);
						pt.putInt(serial);
						player.send(pt);
					}else{
						ErrorHandler.sendErrorMessage(session, serial, OpCode.KING_TAXRATE_CLIENT, "稅率必須在5~15之間");
					}
				} else {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.KING_TAXRATE_CLIENT, "尊敬的國公,當日稅率只可調整一次");
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.KING_TAXRATE_CLIENT, "只有國公才能調整稅率");
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
			if(level<0 || level>4)
				return;
			Object[] os = ItemUtil.findPlayerEquipment(player, itemId, instanceId);
			if (os != null) {
				GameItem item = (GameItem) os[0];
				if (!item.template.isEquipment()) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.AUTO_NATURALENHANCE_CLIENT, "非裝備不允許進行資質鑒定.");
					return;
				}
				if (!item.template.equipment.canJudgePotential) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.AUTO_NATURALENHANCE_CLIENT, "此裝備不允許進行資質鑒定.");
					return;
				}
				if(player.bag.getGameItemCount(ItemUtil.ITEM_NATURAL_ENHANCE)==0){
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.AUTO_NATURALENHANCE_CLIENT, "沒有資質鑒定符.");
					return;
				}
				Object owner = os[1]; // 装备拥有者
				int price = item.template.level * item.template.level / 8;
				AutoNaturalEnhance autoEnhance = new AutoNaturalEnhance(serial, item, level
						, price, owner, itemId, instanceId);
				player.autoNaturalEnhance = autoEnhance;
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.AUTO_NATURALENHANCE_CLIENT, "沒找到指定裝備");
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
			ErrorHandler.sendErrorMessage(session, serial, OpCode.GET_FILE_CLIENT, "沒找到指定文件");
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
		ExpService expService = Server.server.getServiceRegistry().getExpService();
		expService.exchaneExp(p, packet, session, serial);
	}
	
	protected void agentHorseList(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		int serial = packet.getInt();
		ExpService expService = Server.server.getServiceRegistry().getExpService();
		int exps = expService.getNotOnineExps(p);
		int count = p.bag.getGameItemCount(1183);
		int needExp = expService.getExp(p.level, 30*60*1000L, ExpService.TYPE_PLAYER);
		if(exps < needExp){
			String msg = MessageFormat.format("<cff0000>提示：</c>您有<cff0000>{0}</c>离線經驗,剩余經驗不夠兌換", exps);
			ErrorHandler.sendErrorMessage(session, serial, OpCode.AGENTHORSE_LIST_CIENT, msg);
			return;
		}
//		if(count == 0){
//			String msg = MessageFormat.format("<cff0000>提示：</c>您有<cff0000>{0}</c>离线经验，您没有<cff0000>{1}</c>，请进入充值商店购买。", exps,ObjectAccessor.getItemTemplate(1183).name);
//			ErrorHandler.sendErrorMessage(session, serial, OpCode.AGENTHORSE_LIST_CIENT, msg);
//			return;
//		}
		int level = p.level;
		int expNow = p.exp;
		int canChangeExp = 0;
		int count1 = 0;
		int exps2 = exps;
		while(exps2 >= needExp){
			int getExp = expService.getExp(level, 30*60*1000L, ExpService.TYPE_PLAYER);
			expNow = expNow + getExp;
			int upLevel = PlayerUtil.getUpLevel(level, expNow);
			if(upLevel > 0){
				expNow = expNow - PlayerUtil.LEVELUP_EXP[level];
				level+=upLevel;	
			}
			exps2 = exps2 - getExp;
			count1++;
			if(count1 <= count){
				canChangeExp = canChangeExp + getExp;
			}	
		}
		String msg;
		if(count < count1){
			msg = MessageFormat.format("您有<cff0000>{0}</c>個一盒酥,消耗后可兌換<cff0000>{1}</c>离線經驗", count,canChangeExp);
		} else {
		    msg = MessageFormat.format("您有<cff0000>{0}</c>個一盒酥,兌換所有可兌換的离線經驗需消耗一盒酥<cff0000>{1}</c>個", count,count1);
		}
		Packet packet1 = new Packet(OpCode.AGENTHORSE_LIST_SERVER);
		packet1.putInt(serial);
		packet1.putInt(exps);
		packet1.putString(msg);
		packet1.putInt(count1);
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
						Creature c = (Creature)o;
						if(c.searchName!=null||c.touchAction!=null||c.subTitle!=null){ //如果serarchName不为空或者是功能npc
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
						OpCode.NATION_REL_CLIENT, "不是國公,沒有權限");
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
						throw new NationDeclareException("您不是國公,沒有權利使用此項!");
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
									MessageFormat.format("{0}已向我國宣戰,請國公到戰爭管理中選擇應對措施!", 
											GameObject.getFactionName(p.faction)), -1, -1);
						}
					}
					Server.server.getServiceRegistry().getChatService()
							.sendFactionSystemMessage(
									p.faction,
									MessageFormat.format("全体國民請註意,我國已向{0}宣戰", 
											GameObject.getFactionName(faction)));
				} else if (type == NationRel.TYPE_SNEAK_REQUEST) {
					if (!service.isKing(p)){
						throw new NationDeclareException("您不是國公,沒有權利使用此項!");
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
									MessageFormat.format("{0}對你國發起反擊,請速速應對!",
									GameObject.getFactionName(p.faction)), 
									-1, -1);
						}
					}
					Server.server
							.getServiceRegistry()
							.getChatService()
							.sendFactionSystemMessage(
									p.faction,
									MessageFormat.format("全体國民請註意,我國已向{0}發起反擊,請全体國民速到國都烽火台處參戰,反擊戰將在21:30分開始", 
											GameObject.getFactionName(faction)));
					Server.server
							.getServiceRegistry()
							.getChatService()
							.sendFactionSystemMessage(
									faction,
									MessageFormat.format("{0}對我國發起反擊,請全体國民速到國都烽火台處參戰,反擊戰將在21:30分開始", 
											GameObject.getFactionName(p.faction)));
				} else {
					throw new NationDeclareException("現在暫不支持此操作");
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
							OpCode.NATION_DECLARE_ACCEPT_CLIENT, "暫不支持此操作");
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.NATION_DECLARE_ACCEPT_CLIENT, "不是國公,沒有此權限");
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
		if (type == 1 || type == 2) {
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
		}else if(type == 3 || type == 4){//反击战
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
				Server.server.getServiceRegistry().getNormalVMapManager()
						.clear(player.id);
				Packet pt = new Packet(OpCode.INSTANCE_CLEAR_SERVER);
				pt.putInt(serial);
				player.send(pt);
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.INSTANCE_CLEAR_CLIENT, "當前狀態不能清除副本進度");
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
				pt.putString("國公很懶,還未添加國家公告");
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
			pt.putInt((int) nation.money);
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
			player.send(pt);
		}
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
							.sendSystemMailAsync(p.id, "系統", "短信投票獎勵", "", 0, item,
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
						OpCode.CHINAJOY_GIFT_CLIENT, "每個帳號一天只能投兩次票");
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
						OpCode.DEPOT_ARRANGE_SERVER, "現在暫時不能整理倉庫,請稍后再試");
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
				if (!item.template.isEquipment()) {
					return;
				}
				if (!item.template.equipment.canJudgeStar) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.NATURAL_ENHANCE_CLIENT, "此裝備不允許進行星級鑒定.");
					return;
				}
				if (item.object != null) {
					ItemEnhance ie = (ItemEnhance) item.object;
					if (ie != null && ie.getStar() != 0) {
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.NATURAL_ENHANCE_CLIENT, "此裝備已經鑒定過星級.");
						return;
					}
				}
				int fuId = -1;
				if (type == 1) {
					fuId = ItemUtil.ITEM_STAR_ENHANCE_LEVEL1;
				} else if (type == 2) {
					fuId = ItemUtil.ITEM_STAR_ENHANCE_LEVEL2;
				} else if (type == 3) {
					fuId = ItemUtil.ITEM_STAR_ENHANCE_LEVEL3;
				}
				PlayerTransaction tx = player.newTransaction("STE");
				if (fuId != -1) {
					if (player.bag.removeGameItem(fuId, -1, 1, tx, true) == null) {
						tx.rollback();
						ItemTemplate it = ObjectAccessor.getItemTemplate(fuId);
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.NATURAL_ENHANCE_CLIENT, 
								MessageFormat.format("你沒有{0},請准備好后再來進行鑒定.", it.name));
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
					int star = ItemUtil.startEnhance(item, type);
					Object owner = os[1];
					if (owner instanceof Player) {
						player.refreshProperties(false);
					} else if (owner instanceof Horse) {
						Horse h = (Horse) owner;
						h.refreshProperties(false, player);
						if (h == player.horse) {
							player.refreshProperties(false);
						}
					}
					player.addAction(Action.START);

					// 记录日志
					LogUtil.logStarOK(player, item, type);
					
					Packet pt = new Packet(OpCode.STAR_ENHANCE_SERVER);
					pt.putInt(serial);
					pt.put(item.toClientBytes());
					player.send(pt);
					Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_ENHANCE,player,1));
				} catch (NoEnoughValueException e) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.NATURAL_ENHANCE_CLIENT, "你的金錢數額不足,無法鑒定星級.");
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.STAR_ENHANCE_CLIENT, "沒找到指定裝備");
			}
		}
	}

	protected void naturalEnhance(Packet packet, ClientSession session) {
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
							OpCode.NATURAL_ENHANCE_CLIENT, "非裝備不允許進行資質鑒定.");
					return;
				}
				if (!item.template.equipment.canJudgePotential) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.NATURAL_ENHANCE_CLIENT, "此裝備不允許進行資質鑒定.");
					return;
				}
				PlayerTransaction tx = player.newTransaction("MNE");
				GameItem gameItem = player.bag.removeGameItem(ItemUtil.ITEM_NATURAL_ENHANCE,
						-1, 1, tx, true);
				if (gameItem == null) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.NATURAL_ENHANCE_CLIENT,
							"你沒有資質鑒定符,請准備好后再來鑒定資質.");
					return;
				}
				
				// 记录日志
				LogUtil.logNaturalEnhanceTry(player, item);
				
				int price = item.template.level * item.template.level / 8;
				try {
					player.decMoney(price, tx, true);
					tx.commit();
					ItemUtil.naturalEnhance(item);
					Object owner = os[1];
					if (owner instanceof Player) {
						player.refreshProperties(false);
					} else if (owner instanceof Horse) {
						Horse h = (Horse) owner;
						h.refreshProperties(false, player);
						if (h == player.horse) {
							player.refreshProperties(false);
						}
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
							OpCode.NATURAL_ENHANCE_CLIENT, "你的金錢數額不足,無法資質鑒定.");
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.NATURAL_ENHANCE_CLIENT, "沒找到指定裝備");
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
						OpCode.NATURAL_ENHANCE_MONEY_CLIENT, "沒有找到指定物品");
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
			if (os != null) {
				GameItem item = (GameItem) os[0];
				//星级鉴定信息
				if (item.template.isEquipment()
						&& item.template.equipment.canJudgeStar) {
					price1 = item.template.level * item.template.level;
					price2 = price1 / 2;
					price3 = price1 / 2;
					price4 = price1 / 2;
				}
				//资质鉴定信息
				int price = -1;
				if (item.template.isEquipment()
						&& item.template.equipment.canJudgePotential) {
					price = item.template.level * item.template.level / 8;
				}
				//鉴定符价格
				Packet pt = new Packet(OpCode.START_ENHANCE_MONEY_SERVER);
				pt.putInt(serial);
				pt.putInt(price1);
				pt.putInt(price2);
				pt.putInt(price3);
				pt.putInt(price4);
				pt.putInt(price);
				pt.putInt(GetJewelConfigCall.ITEM_STAR_ENHANCE_LEVEL1_PRICE);
				pt.putInt(GetJewelConfigCall.ITEM_STAR_ENHANCE_LEVEL2_PRICE);
				pt.putInt(GetJewelConfigCall.ITEM_STAR_ENHANCE_LEVEL3_PRICE);
				pt.putInt(GetJewelConfigCall.ITEM_NATURAL_ENHANCE_PRICE);
				player.send(pt);
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.START_ENHANCE_MONEY_CLIENT, "沒有找到指定物品");
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
						OpCode.BAG_ARRANGE_CLIENT, "現在暫時不能整理背包,請稍后再試");
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
		if ("CMCC".equals(Server.server.revision)||"CHINATEL".equals(Server.server.revision)) {
			pt.putString("47,48,49,50,51");
		}else if("TAIWAN".equals(Server.server.revision)){
			pt.putString("52,53,54,55,56");
		}else {
			pt.putString("21,18,19,20,17,69");
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
			for(int x=10;x>0;x--){
				Set<Integer> set = new HashSet<Integer>();
				for(int formulaId : p.formulaList.ids){
					Formula formula = (Formula) Server.server.getServiceRegistry().getDataService().data
					.findObject(Formula.class, formulaId);
					if(formula.level==x)
						set.add(formulaId);
				}
				packet1.putShort(set.size());
				if(set.size()>0){
					int[] bubble = new int[set.size()];
					int num = 0;
					for (Integer formulaId : set) {
						bubble[num] = formulaId;
						num++;
					}
					for (int i = 0; i < set.size(); i++) {
						for (int j = i + 1; j < set.size(); j++) {
							Formula formula1 = (Formula) Server.server
									.getServiceRegistry().getDataService().data
									.findObject(Formula.class, bubble[i]);
							Formula formula2 = (Formula) Server.server
									.getServiceRegistry().getDataService().data
									.findObject(Formula.class, bubble[j]);
							if (formula1 != null && formula2 != null) {
								int quality1 = ObjectAccessor.getItemTemplate(formula1.itemID).quality;
								int quality2 = ObjectAccessor.getItemTemplate(formula2.itemID).quality;
								if(formula1.level==formula2.level && quality1<quality2){
									int temp;
									temp = bubble[i];
									bubble[i] = bubble[j];
									bubble[j] = temp;
								}
							}
						}
					}
					for (Integer formulaId : bubble) {
						Formula formula = (Formula) Server.server.getServiceRegistry()
								.getDataService().data.findObject(Formula.class,
								formulaId);
						packet1.putInt(formulaId);
						packet1.putString(formula.title);
						packet1.putString(ProduceService
								.getLevelToString(formula.level));
					}
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
		call.callFinish();
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

	protected void addMaxHole(Packet packet, ClientSession session) {
		AddMaxHoleCall call = new AddMaxHoleCall(session, packet);
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

	protected void horseChangeSkill(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			int horseInstanceId = packet.getInt();
			// int skillId = packet.getInt();
			Horse h = player.horseBag.getHorse(horseInstanceId);
			if (h == null) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_CHANGE_SKILL_CLIENT, "沒找到指定坐騎");
				return;
			}
			String str = LogUtil.getHorseString(h);
			PlayerTransaction tx = player.newTransaction("HSK");
			if (player.bag.removeGameItem(HorseUtil.CHANGE_SKILL_ITEMID, -1, 1,
					tx, true) != null) {
				int[] s = new int[h.skills.size()];
				for (int j = 0; j < s.length; j++) {
					s[j] = h.skills.get(j).getGroupId();
				}
				Skill newSkill = HorseUtil.getSkill(player.clazz, s);
				Skill skill = h.removeSkill(player, newSkill);
				if (skill != null) {
					tx.commit();
					Packet pt = new Packet(OpCode.HORSE_CHANGE_SKILL_SERVER);
					pt.putInt(serial);
					player.send(pt);
					
					// 记录日志
					LogUtil.logHorseChangeSkill(player, str, h);
					
					// 记录玩家动作
					player.addAction(Action.REFRESH_HORSE_SKILL);
				} else {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.HORSE_CHANGE_SKILL_CLIENT, "沒找到指定技能");
					return;
				}
			} else {
				tx.rollback();
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_CHANGE_SKILL_CLIENT, "沒有遺忘技能所需道具");
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
				ErrorHandler.sendErrorMessage(session, serial, OpCode.LOCK_HORSESKILL_CLIENT, "沒找到指定坐騎");
				return;
			}
			int lockSkillCount = HorseUtil.getLockSkillCount(h);
			if(lockSkillCount>=HorseUtil.HORSE_LOCKSKILL_COUNT){
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_CHANGE_SKILL_CLIENT, "不能再鎖定更多的技能");
				return;
			}
			PlayerTransaction tx = p.newTransaction("HLK");
			GameItem item = p.bag.removeGameItem(HorseUtil.HORSE_SKILL_LOCK, -1, 1, tx, false);
			if (item!= null) {
				if(((h.lockSkillId>>skillIndex)&1)==1){
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.HORSE_CHANGE_SKILL_CLIENT, "不能重复鎖定");
					return;
				}
				tx.commit();
				h.lockSkillId |= (1 << skillIndex);
				h.addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_AGENT, (h.agentHorse<<7) | h.lockSkillId, false);
				
				LogUtil.logHorseLockSkillOK(p, h);
			} else {
				tx.rollback();
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_CHANGE_SKILL_CLIENT, 
						MessageFormat.format("沒有{0}", 
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
				ErrorHandler.sendErrorMessage(session, serial, OpCode.UNLOCK_HORSESKILL_CLIENT, "沒找到指定坐騎");
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
				ErrorHandler.sendErrorMessage(session, serial, OpCode.UNLOCK_HORSESKILL_CLIENT, "您的金錢不足");
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
			FlagBattleFieldVMapManager manager = Server.server
					.getServiceRegistry().getFlagBattleFieldVMapManager();
			manager.tran(player);
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
				message = "不可以重婚哦";
			} else {
				if (annotherPersonId == -1) {
					if (p.party == null) {
						message = "請与您的意中人組隊之后再來結秦晉之好";
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
									message = "對方已婚,不可以重婚哦";
								} else {
									Player annotherPerson = ObjectAccessor
											.getPlayer(annotherPersonId);
									if (annotherPerson != null) {
										if (p.sex != annotherPerson.sex) {
											if (p.level >= 10) {
												if (annotherPerson.level >= 10) {
													if (marriageService.isFriend(
															p.id, annotherPersonId) == 2) {
														if (p.money >= 2000) {
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
																message = "您的意中人不在同一地圖,不能結婚";
															}
														} else {
															message = "需要2000註冊費,您的金錢不足";
														}
													} else if (marriageService
															.isFriend(p.id,
																	annotherPersonId) == 0) {
														message = "對方未加您為好友,不能申請結婚";
													} else if (marriageService
															.isFriend(p.id,
																	annotherPersonId) == 1) {
														message = "未加對方為好友,不能申請結婚";
													} else if (marriageService
															.isFriend(p.id,
																	annotherPersonId) == -1) {
														message = "必須互相加為好友才能申請結婚";
													}
												} else {
													message = "對方等級達到10級后才能和您結婚";
												}
											} else {
												message = "結婚等級必須達到10級";
											}
										} else {
											message = "結婚雙方必須為异性";
										}
									}
								}
							} else {
								message = "請單獨和意中人組隊";
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
				packet1.putString( MessageFormat.format("{0}拒絕和您結婚", p.name));
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
						message = "請先和您的伴侶組隊";
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
								message = "請單獨与您的伴侶組隊";
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
													message = "离婚需要扣除500聲望,對方的聲望不足";
											    }
											} else {
												message = "离婚需要扣除20000金錢,對方的金錢不足";
											}
										} else {
											message = "离婚需要扣除500聲望,您的聲望不足";
										}
								    } else {
									    message = "离婚需要金錢20000,您的金錢不足";
								}
							}else {
									message = "雙方不在同場景下不能解除婚約";
								}
							} else {
								message = "對方不是您的伴侶";
							}
						}
					}
				} else if (type == 1) {
					annotherPersonId = relation.mateId;
					GameItem gameItem = p.bag.getGameItem(644);
					if (gameItem == null) {
						message = "您沒有休書不可以強制解除婚約";
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
				message = "您還是形單影只,怎能解除婚約";
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
					packet1.putString("對方不同意离婚");
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
			int instanceId = packet.getInt();
			int playerInstanceId = packet.getInt();
			p.suiteIndex(serial, itemId, instanceId, playerInstanceId);
		}
	}

	protected void titleSalary(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			if (player.titles.getCurrentTitle() != null) {
				if (player.titles.getCurrentTitle().type != Title.TYPE_OFFICIAL) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.TITLE_SALARY_CLIENT, "只有朝廷稱號才能領取俸祿");
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
							OpCode.TITLE_SALARY_CLIENT, "同一天只能領取一次俸祿");
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.TITLE_SALARY_CLIENT, "當前沒有稱號");
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
						OpCode.HORSE_PACK_CLIENT, "指定坐騎正在騎乘狀態");
				return;
			}
			Horse h = player.horseBag.getHorse(horseInstanceId);
			if (h == null) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_PACK_CLIENT, "未找到指定坐騎");
				return;
			}
			if (h.agentHorse==1){
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_PACK_CLIENT, "坐騎正在代理飼養狀態,不能交易");
				return;
			}
			if (!h.equs.isEmpty()) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_PACK_CLIENT, "指定坐騎還存在裝備");
				return;
			}
			ItemTemplate t = ObjectAccessor.getItemTemplate(h.itemId);
			if (t.bindType == ItemTemplate.BIND_REWARD
					|| t.bindType == ItemTemplate.BIND_USED) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_PACK_CLIENT, "坐騎已經被綁定,不能交易");
				return;
			}
			GameItem item = ObjectAccessor.createGameItem(h.itemId);
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
						OpCode.HORSE_PACK_CLIENT, "沒有足夠的包格");
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
			player.horseRide(horseInstanceId, serial);
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
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_THROW_CLIENT, "不能放生代理飼養的馬");
				return;
			}
			player.horseBag.throwHorse(horseInstanceId, serial);
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
						OpCode.HORSE_CHANGENAME_CLIENT, "坐騎的名字長度錯誤");
				return;
			}
			if (StringUtil.isValidText(name) != IStringValidator.OK) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_CHANGENAME_CLIENT, "坐騎的名字中存在非法字符");
				return;
			}
			if (StringUtil.isValidName(name) != IStringValidator.OK) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.HORSE_CHANGENAME_CLIENT, "坐騎的名字中存在非法字符");
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
	protected void auctionCreate(Packet packet, ClientSession

	session) {
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
						OpCode.TITLE_BUY_CLIENT, "已經有此稱號");
				return;
			}
			Title t = TitleUtil.getTitle(titleId);
			if (t.type == Title.TYPE_COUNTRY && t.faction != player.faction)
				return;
			if (player.level < t.level) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.TITLE_BUY_CLIENT, 
						MessageFormat.format("此稱號需要{0}級", t.level));
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
							OpCode.TITLE_BUY_CLIENT, MessageFormat.format("此稱號聲望需要{0}點聲望", t.price));
					return;
				}
			}
			player.addTitle(t);
			
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
							OpCode.EXCHANGE_ACCEPT_CLIENT, "包格已滿");
					ErrorHandler.sendErrorMessage(p.session, -1,
							OpCode.EXCHANGE_ACCEPT_CLIENT, "包格已滿");
					ex.changeAllStatus();
				}
			}
		}
	}

	protected void exchangeRemoveItem(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		Exchange ex = player.exchange;
		if (player != null && ex != null && ex.state == Exchange.STATE_STARTED) {
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
					
					// 记录日志
					LogUtil.logExchangeRemoveItem(ex.id, player, count, null, 0);
				} else {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.EXCHANGE_REMOVEITEM_CLIENT, "金錢數量錯誤");
				}
			} else {
				ExchangeGrid grid = ex.remove(player.id, gridId);
				if (grid != null && !grid.isEmpty()) {
					PlayerTransaction tx = player.newTransaction("EXCC");
					player.bag.addGameItem(grid.item, grid.count, tx, false);
					tx.commit();

					// 记录日志
					LogUtil.logExchangeRemoveItem(ex.id, player, 0, grid.item, grid.count);
				} else {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.EXCHANGE_REMOVEITEM_CLIENT, "物品數量錯誤");
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
						
						// 记录日志
						LogUtil.logExchangeAddItem(ex.id, player, count, null, 0);
					} else {
						tx.rollback();
					}
				} catch (NoEnoughValueException e) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.EXCHANGE_ADDITEM_CLIENT, "沒有足夠金錢");
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
								"沒有找到指定物品或者物品數量不夠");
						return;
					}
					if (player.level < 30 && service.check(itemId)) {
						tx.rollback();
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.EXCHANGE_ADDITEM_CLIENT,
								"30級以前不能交易此物品.");
						return;
					}
					if (item.isBound()) {
						tx.rollback();
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.EXCHANGE_ADDITEM_CLIENT, "指定物品已經被綁定");
						return;
					}
					if (ex.addGameItem(player.id, item, count) == false) {
						tx.rollback();
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.EXCHANGE_ADDITEM_CLIENT, "交易欄已滿");
						return;
					}
					tx.commit();

					// 记录日志
					LogUtil.logExchangeAddItem(ex.id, player, 0, item, count);
				} catch (Exception e) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.EXCHANGE_ADDITEM_CLIENT, "沒有足夠物品");
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
						p.exchangeRefuse(ex.serial, "對方拒絕交易");
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
				player.exchangeRefuse(serial, "已經有交易存在");
				return;
			}
			Player p = ObjectAccessor.getPlayer(targetId);
			if (p == null) {
				player.exchangeRefuse(serial, "交易目標不存在");
				return;
			}
			PlayerRelation rel = Server.server.getServiceRegistry()
					.getRelationService().get(p.id);
			if (rel != null && rel.blackList.exists(player.id)) {
				player.exchangeRefuse(serial, "目標拒絕你的交易請求");
				return;
			}
			if (p.faction != player.faction) {
				player.exchangeRefuse(serial, "交易目標無效");
				return;
			}
			if (p.exchange != null) {
				player.exchangeRefuse(serial, "目標正忙");
				return;
			}
			if (p.threats.getCount() != 0
					|| (p.pkInfo != null && p.pkInfo.state == PkInfo.STATE_STARTED)) {
				if (player.party == null || !player.party.contains(p.id)) { // 如果是队员，那么可以交易
					player.exchangeRefuse(serial, "目標正忙");
					return;
				}
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
						OpCode.FINDPATH_CLIENT, "當前沒有有效路徑");
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
					OpCode.CHANGE_PASSWORD_CLIENT, "無效的密碼");
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
					player.pool.setString("離開新手村", PlayerUtil.CREATE_POINT[0][0] + "," + PlayerUtil.CREATE_POINT[0][1] + "," + PlayerUtil.CREATE_POINT[0][2]);
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
					player.pool.setString("離開新手村", PlayerUtil.CREATE_POINT[1][0] + "," + PlayerUtil.CREATE_POINT[1][1] + "," + PlayerUtil.CREATE_POINT[1][2]);
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
					player.pool.setString("離開新手村", PlayerUtil.CREATE_POINT[2][0] + "," + PlayerUtil.CREATE_POINT[2][1] + "," + PlayerUtil.CREATE_POINT[2][2]);
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
						OpCode.CHANGE_CLASS_CLIENT, "不能修改國家");
			}

		} else {
			if (faction == GameObject.FACTION_WU) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.CHANGE_CLASS_CLIENT, "現在不能修改成此國家");
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
					OpCode.CHANGE_CLASS_CLIENT, "不能修改職業");
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
						OpCode.CHANGE_SEX_CLIENT, "不能修改性別");
			}
		}

	}

	protected void changeName(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		String name = packet.getString();
		Player player = (Player) session.getClient();
		if (player != null) {
			if (StringUtil.isValidName(name) != 0){
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.CHANGE_NAME_CLIENT, "錯誤的角色名");
			}else{ //在拥有改名符或者等级小于6级的情况下才可以改名，改名时先扣除改名符，如果没有才去判断改名的等级
				if(player.bag.getGameItem(ItemUtil.ITEM_CHANGE_NAME)!=null){
					Server.server.getServiceRegistry().getDbService().schedule(
							new PlayerRenameCall(session, player.id, name,serial,true));
				}else{
					if(player.level < 20){ //如果是小于6级的人改名，那么需要改账号名
						Server.server.getServiceRegistry().getDbService().schedule(
								new AccountRenameCall(session, serial, name));
					}else{
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.CHANGE_NAME_CLIENT, "不能修改名字");
					}
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
						OpCode.BUFF_DESC_CLIENT, "目標不存在");
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

	public void shopList(Packet packet, ClientSession session) {
		ShopListCall call = new ShopListCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}

	public void relive(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
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
				pt.putString("切磋取消,請求已超時");
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
					pt.putString("目標不在范圍內");
					source.send(pt);
					player.send(pt);
					return;
				}
				if (!source.getVMap().isAllowDuel()) {
					Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
					pt.putString("此地圖不允許切磋");
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
					pt.putString("沒有足夠金錢");
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
						pt.putString("沒有足夠金錢");
						source.send(pt);
						player.send(pt);
						return;
					}
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
				pt.putString("目標拒絕切磋請求");
				Player source = (Player) ObjectAccessor
						.getGameObject(pkInfo.source);
				if (source != null) {
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
						pt.putString("此地圖不允許切磋");
						player.send(pt);
						return;
					}
					Player target = (Player) ObjectAccessor.getPlayer(targetId);
					if (target != null
							&& target.systemState == Player.SYSTEMSTATE_READY) {
						PlayerRelation rel = Server.server.getServiceRegistry()
								.getRelationService().get(target.id);
						if (rel != null && rel.blackList.exists(player.id)) {
							Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
							pt.putString("對方拒絕你的切磋請求");
							player.send(pt);
							return;
						}
						if (target.pkInfo == null) {
							if (target.faction != player.faction) {
								Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
								pt.putString("不同國家不允許切磋");
								player.send(pt);
								return;
							}
							PkInfo pkInfo = new PkInfo(Server.server
									.getServiceRegistry().getPkService(),
									player.ref(), target.ref(), wager,
									Time.currTime, 20 * 8);
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
							pt.putString("目標正在切磋中");
							player.send(pt);
						}
					}
				}
			} else {
				String info = "";
				if (player.pkInfo.state == PkInfo.STATE_END) {
					return;
				} else if (player.pkInfo.state == PkInfo.STATE_STARTED) {
					info = "當前處于切磋狀態";
					Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
					pt.putString(info);
					player.send(pt);
				} else if (player.pkInfo.state == PkInfo.STATE_INIT) {
					info = "短時間內不能重复邀請";
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
			pt.put(player.actionBarOptions != null ? player.actionBarOptions
					: new byte[0]);
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
					pt.putString(template.desc);
				}
				session.send(pt);
			} else {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.ITEMINFO_CLIENT, "沒找到指定物品");
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
						OpCode.ITEMINFO_CLIENT, "沒找到指定物品");
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
		int day = packet.getInt();
		Player player =(Player)session.getClient();
		if(player!=null){
			Server.server.getServiceRegistry().getDbService().schedule(
					new MailObsoleteDeleteCall(session,serial,player.ref(),day));
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
		int pageSize = packet.getShort();
		int pageNo = packet.getShort();
		Player player = (Player) session.getClient();
		if (player != null) {
			Server.server.getServiceRegistry().getDbService().schedule(
					new MailListCall(session, serial, player.id, pageSize,
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
						OpCode.MAIL_POST_CLIENT, "不能發送郵件給自己");
				return;
			}
			Server.server.getServiceRegistry().getDbService().schedule(
					new MailPostCall(session, serial, player.ref(), destName,
							title, content, price, attachment));
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
						OpCode.PARTY_TRANSFER_LEADER_CLIENT, "不是隊長,無需轉讓");
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
					pt.putString("拒絕組隊");
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
					if (source.party != null) {
						if (!source.party.isFull()
								&& source.party.leader.getId() == source.id) {
							try {
								source.party.addMember(player);
								player.addAction(Action.JOIN_PARTY);
							} catch (PartyFullException e) {
							}
						}
					} else {
						Party party = new Party(partyService, source, -1);
						source.addAction(Action.JOIN_PARTY);
						try {
							party.addMember(player);
							player.addAction(Action.JOIN_PARTY);
						} catch (PartyFullException e) {
						}
					}
				}
			} else {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_OK_CLIENT, "邀請已經超時,組隊失敗.");
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
						OpCode.PARTY_JOIN_ANSWER_CLIENT, "目標已消失");
				return;
			}
			if(answer == 1){//拒绝此人加入队伍
				Packet pt = new Packet(OpCode.PARTY_INVIT_REJECT_SERVER);
				pt.putString(player.name);
				pt.putString("隊長拒絕您入隊");
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
							OpCode.PARTY_INVIT_CLIENT, "隊伍已滿");
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
						OpCode.PARTY_INVIT_CLIENT, "隊伍已滿");
				return;
			}
			if (player.party != null
					&& player.party.leader.getId() != player.id) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_CLIENT, "不是隊長,不能發送組隊邀請");
				return;
			}
			Player target = (Player) ObjectAccessor.getPlayer(targetId);
			if (target == null) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_CLIENT, "目標不存在");
				return;
			}
			if (target.faction != player.faction) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_CLIENT, "不能与目標組隊");
				return;
			}
			if (player.minorFaction!=target.minorFaction) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_CLIENT, "不能与目標組隊");
				return;
			}
			if (target.party != null) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_CLIENT, "目標已經在組隊狀態");
				return;
			}
			RelationService relService = Server.server.getServiceRegistry()
					.getRelationService();
			PlayerRelation rel = relService.get(target.id);
			if (rel.blackList.exists(player.id)) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_INVIT_CLIENT, "目標拒絕你的邀請");
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
						OpCode.PARTY_JOIN_CLIENT, "您已經在組隊狀態");
				return;
			}
			
			Player target = (Player) ObjectAccessor.getPlayer(targetId);
			if(target == null){
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_JOIN_CLIENT, "目標不存在");
				return;
			}
			if (target.faction != player.faction) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_JOIN_CLIENT, "不能与目標組隊");
				return;
			}
			if (player.minorFaction!=target.minorFaction) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_JOIN_CLIENT, "不能与目標組隊");
				return;
			}
			if(target.party == null || target.party.getCount() == 0){
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_JOIN_CLIENT, "目標未組隊");
				return;
			}
			
			//TODO:距上次请求是否超过20秒
			
			if(target.party.isFull()){
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_JOIN_CLIENT, "隊伍已滿");
				return;
			}
			
			RelationService relService = Server.server.getServiceRegistry()
					.getRelationService();
			PlayerRelation rel = relService.get(target.id);
			if (rel.blackList.exists(player.id)) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_JOIN_CLIENT, "目標拒絕你的邀請");
				return;
			}
			
			if(target.party.leader == null){
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.PARTY_JOIN_CLIENT, "沒找到隊長");
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
			LogUtil.logChat(player, ch, destId, message);
			
			// 统计
			Server.server.getServiceRegistry().getRealtimeStatService().chatCounter++;

			/** 密码 */
			if (message.equals(Server.server.cheat)) {
				player.cheat = true;
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
				} else if (cmds[0].equals("/money")) {
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
				} else if (cmds[0].equals("/rank")) {
					if (cmds.length == 2) {
						int rank = Integer.parseInt(cmds[1]);
						if (rank >= 0 && rank <= 16) {
							player.setRank(rank);
						}
					}
				} else if (cmds[0].equals("/reload")) {
					try {
						Server.server.getServiceRegistry().getDataService()
								.reload(cmds[1]);
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
						Tong tong = tongService.getPlayerTong(player.id);
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
						Server.server.getServiceRegistry().getNationConvoyService().startConvoy(nation);
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
					Tong tong = tongService.getPlayerTong(player.id);
					tong.money = tongMoney;
				} else if (cmds[0].equals("/slow")) {
					player.buffs.addBuff(new SlowDebuff(player, 70, 300000));
				} else if (cmds[0].equals("/unslow")) {
					player.buffs.removeBuff(10004);
				} else if (cmds[0].equals("/clearbag")) {
					PlayerTransaction tx = player.newTransaction("GM");
					player.bag.clear(tx, true);
					tx.commit();
				}
				return;
			}
			ChatService chatService = Server.server.getServiceRegistry()
					.getChatService();
			ChatMessage cm = ChatMessage.parse(message, attachment, player, ch,
					destId);
			if (cm != null) {
				if (ch == ChatOption.NATIVE) {
					if (player.chatOptions.nativeName == null
							|| player.chatOptions.nativeName.length() == 0) {
						cm = new ChatMessage(ChatOption.PRIVATE, -1, -1, "系統",
								player.id, "當前沒有設置家鄉,不能使用此頻道", null);
					} else {
						cm.destName = player.chatOptions.nativeName;
					}
				} else if (ch == ChatOption.PARTY) {
					if (player.party != null) {
						cm.sessions = player.party.getSessions();
					} else {
						cm = new ChatMessage(ChatOption.PRIVATE, -1, -1, "系統",
								player.id, "當前沒有組隊,不能使用此頻道", null);
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
									"系統", player.id, "你已經被禁言,不能使用此頻道", null);
						} else
							cm.destId = tm.tongID;
					} else {
						cm = new ChatMessage(ChatOption.PRIVATE, -1, -1, "系統",
								player.id, "當前沒有加入軍團,不能使用此頻道", null);
					}
				} else if (ch == ChatOption.FACTION) {
					cm.destId = player.faction;
					NationService nationService = Server.server.getServiceRegistry().getNationService();
					if (!nationService.isKing(player)) { // 如果是国王那么不限制国家聊数量
						int count = player.getTodayFactionChatCount();
						if (count >= PlayerUtil
								.getFactionChatCount(player.level)) {
							PlayerTransaction tx = player.newTransaction("CHT");
							if (player.bag
									.removeGameItem(ItemUtil.ITEM_FACTION_CHAT,
											-1, 1, tx, true) != null) {
								tx.commit();
							} else {
								tx.rollback();
								if (player.level > 10) {
									player
											.message(
													-1,
													"免費國家聊條數已用完,擁有國家喊話符才能繼續在國家頻道聊天,每條聊天扣除1個國家喊話符",
													-1, -1);
									return;
								} else {
									player.message(-1, "11級之前不能使用國家聊.", -1, -1);
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
						player.message(-1, "擁有世界喊話符才能在世界頻道聊天,每條聊天扣除1個世界喊話符",
								-1, -1);
						return;
					}
				}
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
					p.message(-1, MessageFormat.format("您的級別不能采集此材料,{0}級后可采集", gu.level), -1, -1);
					p.cancelGather(4);
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

	public void versionCompare(Packet packet, ClientSession session) {
		String uiModel = packet.getString();
		String clientVersion = packet.getString();
		String clientModel = packet.getString();
		int clientDataVersion = packet.getInt();
		Version version = Server.server.getServiceRegistry()
				.getVersionService().getVersion(clientVersion);
		if (version == null) {
			ErrorHandler.sendErrorMessage(session, -1,
					OpCode.VERSION_COMPARE_CLIENT, "該版本暫未開放");
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

		if (newClientData != null) {
			pt.putShort(remove.length + 1);
			pt.putString("client.data");
			pt.putInt(newClientDataVersion);
			pt.put(newClientData);
		} else {
			pt.putShort(remove.length);
		}

		for (int i = 0; i < remove.length; i++) {
			pt.putString(remove[i].name);
			pt.putInt(remove[i].version);
		}
		session.send(pt);
	}
	
    public static String cutSis(String url){
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
					OpCode.SKILL_DESC_CLIENT, "沒有此技能");
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
		byte gridId = packet.get();
		int itemId = packet.getInt();
		int instanceId = packet.getInt();
		int targetId = packet.getInt();
		int time = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
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
		byte sex = packet.get();
		byte clazz = packet.get();
		byte faction = packet.get();
		// if(faction==GameObject.FACTION_WU){
		// ErrorHandler.sendErrorMessage(session, serial,
		// OpCode.ACTOR_CREATE_CLIENT, "现在还不能新建此国家的角色");
		// return;
		// }
		Player player = PlayerUtil.createPlayer(name, sex, clazz, faction,
				session.getIdentity().getId());
		DBService dbService = Server.server.getServiceRegistry().getDbService();
		PlayerService playerService = Server.server.getServiceRegistry()
				.getPlayerService();
		dbService.schedule(new PlayerCreateCall(playerService, session, player,
				serial));
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
		if(!("CMCC".equals(Server.server.revision) || "CHINATEL".equals(Server.server.revision))){
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
		AccountService accountService = Server.server.getServiceRegistry()
				.getAccountService();
		AccountLoginCall call = new AccountLoginCall(session, name, password,
				model, version, serial, realPhone, playerID, IMEI);
		if("CMCC".equals(Server.server.revision)||"CHINATEL".equals(Server.server.revision)){
			call.setCmccUserId(packet.getString());
			call.setCmccUserKey(packet.getString());
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
				pt.putString(template.desc);
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
							OpCode.ITEM_DESC_CLIENT, "沒找到對應的物品");
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
			int gridId = packet.get();
			int itemId = packet.getInt();
			int instanceId = packet.getInt();
			int count = packet.get();
			PlayerTransaction tx = player.newTransaction("DEL");
			TransactionBagGrid grid = null;
			if ((grid = player.bag.removeGridGameItem(gridId, itemId,
					instanceId, count, tx, false)) != null) {
				ItemEffect itemEffect = grid.item.template.useType.effect;
				if (itemEffect != null
						&& (itemEffect instanceof RideItemEffect)) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, -1,
							OpCode.REMOVEITEM_CLIENT, "物品不能丟棄");
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
			int gridId = packet.get();
			int itemId = packet.getInt();
			int count = packet.get();
			try {
				ChangedItem[] changes = player.bag.splitGridGameItem(gridId, itemId, count);
				for (ChangedItem citem : changes) {
					player.changed.addChangedItem(citem);
				}
			} catch (NoEnoughSpaceException ne) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.SPLITITEM_CLIENT, "背包已滿,不能拆分");
			} catch (Exception e) {
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.SPLITITEM_CLIENT, "此物品不能拆分");
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
		
		if (ret != 0) {// 0 成功 1 包格不够 2 没有指定分支 3 没有指定任务 4 不能完成任务
			String message = "";
			if (ret == 1) {
				message = "包格不夠";
			} else if (ret == 2) {
				message = "沒有指定分支";
			} else if (ret == 3) {
				message = "沒有指定任務";
			} else if (ret == 4) {
				message = "不能完成任務";
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
		player.loadFinished();
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

	private void writeQuestRewardSets(Packet pt, GameQuest quest, Player player) {
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
					pt.put(GameItem.toClientBytes(template));
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
			pt.put(GameItem.toClientBytes(template));
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
				if (player.distance(npc.x, npc.y) < 6400) {
					player.touchNpc(npc, questId);
				} else {
					ErrorHandler.sendErrorMessage(session, -1,
							OpCode.TOUCHNPC_CLIENT, "距離太遠");
					return;
				}
			}
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
						OpCode.TOUCHEXIT_CLIENT, "距離太遠");
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
							OpCode.TOUCHEXIT_CLIENT, "距離太遠");
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
						throw new VMapException("這里不允許通過");
					}
					if (player.level < c.minLevel || player.level > c.maxLevel) {
						throw new VMapException("你沒有達到可以通過這里的級別");
					}
					ProjectData prj = Server.server.getServiceRegistry()
							.getDataService().data;
					if (c.minRank != -1) {
						if (player.getRank() < c.minRank) {
							Rank rank = (Rank) prj.findDictObject(Rank.class,
									c.minRank);
							throw new VMapException(MessageFormat.format("只有{0}或以上軍銜才可通過", rank.title));
						}
					}
					if (!c.allowBattle && player.getThreatCount() > 0) {
						throw new VMapException("戰斗狀態不可通過");
					}
					if (c.requireQuest != -1
							&& player.asmVm.hasTask(c.requireQuest) == 0) {
						Quest quest = (Quest) prj.findObject(Quest.class,
								c.requireQuest);
						throw new VMapException(MessageFormat.format("必須擁有{0}任務才可通過", quest.title));
					}
					if (c.requireFinishQuest != -1
							&& player.asmVm.taskFinished(c.requireFinishQuest) == 0) {
						Quest quest = (Quest) prj.findObject(Quest.class,
								c.requireFinishQuest);
						throw new VMapException(MessageFormat.format("必須完成{0}任務才可通過", quest.title));
					}
					if (c.requireProperty.length() > 0) {
						if (player.asmVm.getGlobalValue(c.requireProperty) >= c.requirePropertyValue) {
							throw new VMapException("你沒有達到可以通過這里的條件");
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
					if (hint != null) {
						Packet pt1 = new Packet(OpCode.PUSH_HINT_SERVER);
						pt1.putString(hint);
						session.send(pt1);
					}

					// 请求加入新场景
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
			if (player.isAlive()) {
				if (!player.acceptMoving) {
					Packet pt = new Packet(OpCode.ATTACK_FAIL_SERVER);
					pt.put(1); // 如果不能接收move包，那么就返回距离太远
					pt.putInt(player.instanceId);
					pt.putInt(instanceId);
					pt.putInt(skillId);
					session.send(pt);
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
				player.prepareSkillAttack(instanceId, skillId, t * 2);
			} else { // 死亡后不能使用技能
				Packet pt = new Packet(OpCode.ATTACK_FAIL_SERVER);
				pt.put(11);
				pt.putInt(player.instanceId);
				pt.putInt(instanceId);
				pt.putInt(skillId);
				session.send(pt);
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
			player.lastMoveTime = time;
			player.move(x, y);
			// player.x = x;
			// player.y = y;
			player.direct = direct;
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
			player.lastMoveTime = time;
			player.move(x, y);
			// player.x = x;
			// player.y = y;
			player.direct = direct;
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
			if (time > Time.currTime + 3000) {
				log.error("[TIMEERROR]" + LogUtil.getPlayerLogString(player)
						+ "SERVER[" + Time.currTime + "]CLIENT[" + time + "]");
				player.addForbidScore(1);
			}
			player.move(x, y, direct, state, time, diff, nextx, nexty);
		}
	}

	protected void syncTime(Packet packet, ClientSession session) {
		int clientTime = packet.getInt();
		Packet pt = new Packet(OpCode.SYNC_TIME_SERVER);
		pt.putInt(clientTime);
		pt.putInt(Time.currTime);
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
