package peony.db;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.joda.time.MutableDateTime;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameMapDefinition;
import peony.game.Horse;
import peony.game.Identity;
import peony.game.ItemTemplate;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Skills;
import peony.game.Time;
import peony.game.TransactionBagGrid;
import peony.game.VMapUtil;
import peony.game.World;
import peony.game.attendant.Attendant;
import peony.game.buff.BuffUtil;
import peony.game.itemeffect.KingItemEffect;
import peony.game.itemenhance.ItemEnhance;
import peony.game.nation.CandidateService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.VIP.VipPrivilegeService;
import peony.service.account.Account;
import peony.service.account.AccountProperty;
import peony.service.activity.Activity;
import peony.service.activity.ActivityService;
import peony.service.activity.OldPlayerReturnActivity;
import peony.service.cards.CardService;
import peony.service.friend.PlayerRelation;
import peony.service.player.PlayerService;
import peony.service.pluginstance.MayDayFestivalService;

public class PlayerLoadCall extends ClientSessionAsyncCall {
	protected static final Logger log = Logger.getLogger(PlayerLoadCall.class); 
	protected World world;
	protected int actorId;
	protected Player player;
	protected int serial;
	protected PlayerService playerService;
	protected String MIEI = "";
	protected int equipScore;
	
	protected MutableDateTime cachedCal = new MutableDateTime();
	
	protected static int[] EQUIP_SCORE = {
		0,90,90,90,90,90,90,0,57,50,39,25,39,16,32,16,32
	};
	protected static int HORSEEQUIP_SCORE = 18;
	protected static int[] JEWEL_SCORE = {
		0,150,250,450,850,1500,2750,4900
	};
	
	protected static int[] oldMaps = {864,1456,2016,1552,128,97,144,160,544,672,656,64,32,16,176,480,608,624,336,304,288,416,497,592,
		688,1808,1809};
	
	public PlayerLoadCall(PlayerService playerService, ClientSession session,
			int actorId, World world, int serial, String MIEI) {
		super(session);
		this.playerService = playerService;
		this.actorId = actorId;
		this.world = world;
		this.serial = serial;
		this.MIEI = MIEI;
		LogUtil.logLoginTry((Account)session.getIdentity(), actorId, MIEI);
	}
	
	public void callFinish() throws Exception {
		if (success) {
			if (session.isConnected()) {
				session.setClient(player);
				Server.server.getServiceRegistry().getSalaryService().processPlayerLoaded(player);
				if(player.systemState!=Player.SYSTEMSTATE_LOAD){
					player.canRecordHpMp = false;
					player.removeFromWorld();
				}
				player.loginTime = System.currentTimeMillis();
				player.guid.setEventUiType();
				CardService cardService = Server.server.getServiceRegistry().getCardService();
				try{cardService.processRemoveCardExp(player);}catch(Exception e){}
				ObjectAccessor.addGameObject(player);
				Map<Integer, PlayerRelation> relations = Server.server.getServiceRegistry().getRelationService().relations;
				if (!relations.containsKey(player.id)) {
					PlayerRelationDAO dao = Server.server.getServiceRegistry().getDbService().playerRelationDAO;
					PlayerRelation relation = dao.findPlayerRelation(player.id);
					relations.put(player.id, relation);
				}
				GameMapDefinition def = VMapUtil.getDefinition(player.map.id);
				if(isOldMap(player.map.id) 
						&& def!=null && def.mapInfo!=null && def.mapInfo.oldMap){
					int[] outs = getRenascence(def, player.faction);
					world.addPlayerToMap(player, outs[0], outs[1], outs[2],true);
				}else{
					if(player.map.id == MayDayFestivalService.MAYDAY_MAP){
						MayDayFestivalService.isTimeOut(player);
					}else{
					    world.addPlayerToMap(player, player.map.id, player.x, player.y,true);
					}
				}
				player.logined();
				player.moveType = 0;
				player.moveExtended = 0;
				Packet pt = new Packet(OpCode.ACTOR_LOGIN_SERVER);
				pt.putInt(serial);
				pt.put(player.toClientBytes());
				Account a = (Account)session.getIdentity();
				pt.putString(a.getName());
				session.send(pt);
				
				// 下发小提示
				String hint = Server.server.getServiceRegistry().getDataService().getHint(player);
				String model = (player.getAccount()==null ? "" : player.getAccount().getModel());
				if (hint != null) {
					if(!"Lenovo".equals(model) && !"LenovoU1".equals(model)){
						Packet pt1 = new Packet(OpCode.PUSH_HINT_SERVER);
					    pt1.putString(hint);
					    session.send(pt1);
					}
				}
				
				Packet pt1 = new Packet(OpCode.GOMAP_ALLOW_SERVER);
				pt1.putInt(player.map.id);
				pt1.putInt(player.getVMap().getInstanceId());
				pt1.putInt(player.x);
				pt1.putInt(player.y);
				pt1.put(player.getVMap().allowFollow()?1:0);
				session.send(pt1);
				player.refreshTitles();
				player.refreshStar7Buff();
				player.refreshStarState();
				player.refreshHorseStarState();
				player.refreshAllAttendants();
				player.directory.totalEqupScore = equipScore;
				checkRepaireEquipments(player);
			}
		} else {
			if(errorMessage==null){
				errorMessage = peony.Messages.STRING_00497;
			}
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.ACTOR_LOGIN_CLIENT,errorMessage);
		}

	}

	public void run() {
		Identity identity = session.getIdentity();
		if (identity == null) {
			error(null, peony.Messages.STRING_00498);
		} else {
			if (playerService.isMuted(actorId)) {
				error(null, peony.Messages.STRING_00499);
			} else {
				CandidateService candidateService = Server.server.getServiceRegistry().getCandidateService();
				player = playerService.loadPlayer(identity.getId(), actorId);
				if (player != null) {
					player.checkActivePower();
					if (player.isKing() == 1) {
						player.buffs.addBuff(BuffUtil.createSuiteBuff(216, 1));
						if(ObjectAccessor.getSkill(Skills.getSkillId(candidateService.getKingSkillGroupId(player.clazz), 1))!=null)
								player.addSkill(ObjectAccessor.getSkill(Skills.getSkillId(candidateService.getKingSkillGroupId(player.clazz), 1)));
						 KingItemEffect.isKing(player);
					} else {
						player.buffs.removeBuff(216);
						player.skills.removeSkill(candidateService.getKingSkillGroupId(player.clazz), 1);
						player.skills.removeSkill(candidateService.getKingSkillGroupId(player.clazz), 0);
						player.buffs.removeBuff(Skills.getSkillId(candidateService.getKingSkillGroupId(player.clazz), 1));
						Server.server.getServiceRegistry().getCandidateService().processKingItem(player);
					    KingItemEffect.notKing(player);
					}
					ActivityService activityService = Server.server.getServiceRegistry().getActivityService();
					Activity activity = activityService.getActivityByImpClass(OldPlayerReturnActivity.class.getSimpleName());
					if(player.skills.getSkill(Skills.getSkillId(OldPlayerReturnActivity.SKILLS[player.clazz], 1))!=null && 
							(player.pool.getInt(OldPlayerReturnActivity.oldPlayerPool, 0)==0 || activity==null || !activity.isActive() || !activity.isEnabled())){
						player.skills.removeSkill(OldPlayerReturnActivity.SKILLS[player.clazz],0);
						player.skills.removeSkill(OldPlayerReturnActivity.SKILLS[player.clazz],1);
						player.buffs.removeBuff(Skills.getSkillId(OldPlayerReturnActivity.SKILLS[player.clazz], 1));
						player.buffs.removeBuff(OldPlayerReturnActivity.BUFF);
					}else if(activity!=null && activity.isActive() && activity.isEnabled() && player.pool.getInt(OldPlayerReturnActivity.oldPlayerPool, 0)>0){
						player.addSkill(ObjectAccessor.getSkill(Skills.getSkillId(OldPlayerReturnActivity.SKILLS[player.clazz], 1)));
					}
					Server.server.getServiceRegistry().getRelationService()
							.removeAllRelation(player);
					Server.server.getServiceRegistry().getAccountDepotService()
					        .loadAccountDepot(player);
					playerEquipmentLog(player);
					player.lastLoginTime = Time.currDate;
					player.lastLoginTimeMills = Time.currDate.getTime();
					cachedCal.setMillis(player.lastLogoutTime.getTime());
					if(cachedCal.getDayOfYear()==Time.currentDayOfYear){
						player.onlineTimeToday = player.pool.getInt(Player.PROPERTY_ONLINETIMETODY, 0);
					}
					player.isInStep = false;
					Server.server.getServiceRegistry().getChargeActivityService()
			               .getFirstCharge(player.accountId, true);
					VipPrivilegeService vipService = Server.server.getServiceRegistry().getVipPrivilegeService();
					AccountProperty ap = vipService.getAccountProperty(player.accountId);
					if(ap==null){
						synchronized (Server.server.getServiceRegistry().getDbService().accountPropertyDao) {
							ap = vipService.getAccountPropertyFromDB(player.accountId);
						}
					}
					if(ap!=null){
						vipService.addAccountProperty(player.accountId, ap);
						vipService.processPlayerFirstLoad(player, ap);
					}
					Server.server.getServiceRegistry().getPlayerService().notifyAccountPropertyLoaded(player);
				}else{
					log.info("[LOADPLAYERCHEAT]ACCOUNT["+identity.getId()+"]ID["+actorId+"]");
					error(null, peony.Messages.STRING_00500);
				}
			}
		}
		addToClientSession();
	}
	
	protected void playerEquipmentLog(Player player){
		try {
			if(player!=null){
				//统计所有装备
				List<GameItem> equips = new ArrayList<GameItem>(); 
				//玩家背包中所有装备
				for(TransactionBagGrid grid : player.bag.getGrids()){
					GameItem item = grid.getItem();
					if(item!=null && item.template!=null && item.template.isEquipment())
						equips.add(item);
				}
				//玩家身上所有装备
				for(GameItem item : player.equipments.equs){
					equips.add(item);
				}
				//随从身上所有装备
				if(player.attendantBag!=null){
					for(Attendant attendant : player.attendantBag.attendants){
						for(GameItem item : attendant.equs){
							if(item!=null && item.template!=null && item.template.isEquipment()){
								equips.add(item);
							}
						}
					}
				}
				//玩家仓库中所有装备
				for(TransactionBagGrid grid : player.depot.getGrids()){
					GameItem item = grid.getItem();
					if(item!=null && item.template!=null && item.template.isEquipment())
						equips.add(item);
				}
				//坐骑身上所有装备
				for(Horse horse : player.horseBag.horses){
					for(GameItem item : horse.equs.equs){
						equips.add(item);
					}
				}
				int levelScore = 0; //统计用级别积分
				int starScore = 0; //统计用星级积分
				int jewelScore = 0; //统计用宝石积分
				int equipValue = 0; //统计装备价值
				int totalScore = 0; //总分
				StringBuilder sb = new StringBuilder();
				sb.append("[EQUIPSCORE]");
				sb.append(LogUtil.getPlayerLogString(player));
				//计算玩家等级分数
				levelScore += player.level * 600;
				for(GameItem item : equips){
					if(item!=null && item.template!=null && item.template.isEquipment()){
						if(item.object!=null && item.object instanceof ItemEnhance){
							ItemEnhance ie = (ItemEnhance)item.object;
							int star = ie.getStar();
							if(star>0){
								//计算玩家星级分数
								if(item.template.isHorseEquipment()){
									starScore += HORSEEQUIP_SCORE * 7 * star;
								}else{
									int minorType = item.template.equipment.minorType;
									int equipScore = EQUIP_SCORE[minorType];
									starScore += equipScore * 7 * star;
								}
								//计算装备基础分
								equipValue += item.template.equipment.value;
								//计算玩家宝石分数
								int[] jewels = ie.getJewelIDs();
								for(int id:jewels){
									ItemTemplate template = ObjectAccessor.getItemTemplate(id);
									if(template != null){
										jewelScore += JEWEL_SCORE[template.useLevel];
									}
								}
							}
						}
					}
				}
				totalScore += (equipValue + starScore + jewelScore + levelScore);
				equipScore = totalScore;
				sb.append("EQUIPVALUE["+equipValue+"]");
				sb.append("STARSCORE["+starScore+"]");
				sb.append("JEWELSCORE["+jewelScore+"]");
				sb.append("LEVELSCORE["+levelScore+"]");
				sb.append("TOTALSCORE["+totalScore+"]");
				log.info(sb.toString());
			}
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	protected int[] getRenascence(GameMapDefinition def, int faction){
		if(faction==1){
			return def.mapInfo.renascenceWei;
		}else if(faction==2){
			return def.mapInfo.renascenceShu;
		}else if(faction==3){
			return def.mapInfo.renascenceWu;
		}
		return null;
	}
	
	protected void checkRepaireEquipments(Player player){
		//玩家身上所有装备
		boolean notify = false;
		boolean notify_1 = false;
		for(GameItem item : player.equipments.equs){
			try {
				int duration = item.duration;
				int maxDuration = 0;
				try{maxDuration = item.template.equipment.duration;}catch(Exception e){}
				if(maxDuration>0 && duration==0){
					notify_1 = true;
					break;
				} else if(maxDuration>0 && duration<maxDuration*0.1){
					notify = true;
				}
			} catch (Exception e) {
			}
		}
		if(notify_1){
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, "您有装备耐久已经变成0了，实力大幅削减，请赶快修理。");
			return;
		}
		if(notify){
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, "您有装备该修理了，还请英雄尽快修理，若耐久为0后您的实力可是会大打折扣的呢。");
		}
	}
	
	protected boolean isOldMap(int mapId){
		for(int mId : oldMaps){
			if(mId==mapId)
				return true;
		}
		return false;
	}
	
}
