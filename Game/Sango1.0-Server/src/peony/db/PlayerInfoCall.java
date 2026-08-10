package peony.db;

import java.text.MessageFormat;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.Horse;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Title;
import peony.game.association.Association;
import peony.game.asyncbattle.AsyncBattleService;
import peony.game.attendant.Attendant;
import peony.game.attendant.AttendantFixService;
import peony.marriage.WeddingService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.cards.CardInfo;
import peony.service.cards.CardService;
import peony.service.duel.DuelService;
import peony.service.fame.Fame;
import peony.service.fame.FameService;
import peony.service.friend.PlayerRelation;
import peony.service.friend.RelationService;
import peony.service.player.ActorCacheService;
import peony.service.tong.Tong;
import peony.service.tong.TongService;

public class PlayerInfoCall extends ClientSessionAsyncCall {
	
	protected int serial;
	protected int id;
	protected String manteName;
	protected byte type;
	public static final int TYPE_NORMAL=0;
	public static final int TYPE_ASYNCBATTLE=1;//异步战场
	
	protected int[] capital = {272, 240, 352}; // 名人堂地图ID
	
	protected String[] creditStrings = {peony.Messages.STRING_01218,peony.Messages.STRING_01219,peony.Messages.STRING_01220,peony.Messages.STRING_01221,peony.Messages.STRING_01222,peony.Messages.STRING_01223,peony.Messages.STRING_01224,
			peony.Messages.STRING_01225,peony.Messages.STRING_01226,peony.Messages.STRING_01227,peony.Messages.STRING_01228,peony.Messages.STRING_01229,peony.Messages.STRING_01230,
			peony.Messages.STRING_01231,peony.Messages.STRING_01232,peony.Messages.STRING_01233,peony.Messages.STRING_01234}; //军衔名称
	
	protected String[] classes = {peony.Messages.STRING_01235,peony.Messages.STRING_01236,peony.Messages.STRING_01237,peony.Messages.STRING_01238,peony.Messages.STRING_01239,peony.Messages.STRING_01240,peony.Messages.STRING_01241,peony.Messages.STRING_01242,peony.Messages.STRING_01243,
			peony.Messages.STRING_01244,peony.Messages.STRING_01245,peony.Messages.STRING_01246,peony.Messages.STRING_01247,peony.Messages.STRING_01248,peony.Messages.STRING_01249,peony.Messages.STRING_01250,peony.Messages.STRING_01251}; //军衔对应的等级

	public PlayerInfoCall(Packet pt,ClientSession session){
		super(session);
		this.serial = pt.getInt();
		this.type=pt.get();
		this.id = pt.getInt();
	}
	
	public void callFinish() throws Exception {
		// TODO Auto-generated method stub

	}

	public void run() {
		RelationService relationService = Server.server.getServiceRegistry().getRelationService();
		ActorCacheService actorCacheService = Server.server.getServiceRegistry().getActorCacheService();
		FameService fameService = Server.server.getServiceRegistry().getFameService();
		DuelService duelService = Server.server.getServiceRegistry().getDuelService();
		CardService service = Server.server.getServiceRegistry().getCardService();
		Player player = (Player) session.getClient();
		boolean isStatue = false;
		if (player != null) {
			Player target = (Player) ObjectAccessor.getPlayer(id);
			if(type==TYPE_NORMAL){
				if(target==null && fameService!=null && isCapital(player.getVMap().getId()))
					target = fameService.getStatue(id);
				if(target==null && duelService!=null)
					target = duelService.getStatue(id);
			}else if(type==TYPE_ASYNCBATTLE){
				if(target==null){
					AsyncBattleService battleService=Server.server.getServiceRegistry().getAsyncBattleService();
					target=battleService.getPlayerInfo(id);
				}
			}
			if (target != null) {
				Packet pt = new Packet(OpCode.PLAYER_INFO_SERVER);
				pt.putInt(serial);
				pt.putString(target.name);
				pt.put(target.level);
				pt.put(target.clazz);
				pt.put(target.faction);
				pt.putString(target.getGuildName());
				pt.putString(target.chatOptions.nativeName);
				Title title = target.titles.getCurrentTitle();
				pt.putString(title==null?"":title.name);
				//师徒信息
				int teacherId = target.getTeacherId();
				if(target.id < 0){
					int targetId = -target.id;
					Player t = (Player) ObjectAccessor.getPlayer(targetId);
					if(t == null){
					   t = Server.server.getServiceRegistry().getDbService().playerDAO.getPlayerById(targetId);
					}
					if(t!=null)
					   teacherId= t.getTeacherId();
				}
				int graduateTeacherId = -1;
				if(teacherId == -1){
					graduateTeacherId = target.pool.getInt(Player.PROPERTY_GRADUATE_TEACHER,-1);
				}
				if(teacherId!=-1){
					Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(teacherId);
					if(actor != null){
					  pt.putString(actor.name);
					}
				} else{
					if(graduateTeacherId != -1){
						Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(graduateTeacherId);
						if(actor != null){
						  pt.putString(MessageFormat.format(peony.Messages.STRING_01252, actor.name));
						} else {
							 pt.putString("");
						}
					} else {
			          pt.putString("");
					}
				}
				// 夫妻信息
				if(target.id<0){
					Fame fame = FameService.fames.get(target.id);
					if(fame!=null){
						if(fame.mateName!=null){
						  pt.putString(fame.mateName);
						} else {
							pt.putString("");
						}
						isStatue = true;
						target.id = -target.id;
					} else {
						pt.putString("");
					}
				} else {
				    PlayerRelation rel =  relationService.get(target.id);
					if (rel == null) {
						pt.putString("");
					} else {
						int mateId = rel.mateId;
						if (mateId != -1) {
							if(actorCacheService.find(mateId)==null){
								pt.putString(peony.Messages.STRING_01253);
							}else{
								pt.putString(actorCacheService.find(mateId).name);
							}
						} else {
							pt.putString("");
						}
					}
				}
				pt.putInt(target.pool.getInt(WeddingService.PROPERTY_ENAIDU,0));
				String rankString = getRankString(target.getCreditString());
				pt.putString(rankString);
				pt.put(target.equipments.toClientBytes());
				pt.putInt(target.equipments.getHeadScore(target.level,
						target.clazz));
				pt.putInt(target.equipments.getBodyScore(target.level,
						target.clazz));
				pt.putInt(target.equipments.getWeaponScore(target.level,
						target.clazz));
				pt.put(target.sex);
				pt.putInt(target.activePower);
				pt.putInt(target.getCredit());
				pt.putInt(target.getWeekCredit());
				if(type==TYPE_ASYNCBATTLE&&target.horse==null){
					int horseInstanceId=target.pool.getInt(Player.PROPERTY_LAST_HORSE_INSTANCEID);
					if(horseInstanceId!=0&&target.horseBag!=null&&target.horseBag.getHorse(horseInstanceId)!=null){
						Horse horse=target.horseBag.getHorse(horseInstanceId).clone();
						target.horse=horse;
						target.horse.ride(target);
						target.ride();
					}
				}
				if(type==TYPE_ASYNCBATTLE&&target.attendantView==null){
					int attendantInstanceId = target.pool.getInt(Player.PROPERTY_LAST_ATTENDANT_INSTANCEID);
					if(attendantInstanceId!=0&&target.attendantBag!=null&&target.attendantBag.getAttendant(attendantInstanceId)!=null){
						Attendant attendant = target.attendantBag.getAttendant(attendantInstanceId).clone();
						if(attendant!=null){
							attendant.owner=target;
							target.attendantView=attendant;
						}
					}
				}
				pt.put(target.horse==null ? 0 : 1);
				if(target.horse!=null){
					pt.put(target.horse.toClientBytes(target));
				}
				Association association = Server.server.getServiceRegistry().getAssociationService().getAssociationByPlayerId(target.id);
				if(association!=null)
					pt.putString(association.name);
				else
					pt.putString(peony.Messages.STRING_00147);
				if(isStatue){
					target.id = -target.id;
				}
				//下发随从信息
				if(type==TYPE_ASYNCBATTLE){
					pt.put(target.attendantView==null ? 0 : 1);
					if(target.attendantView != null){
						pt.put(target.attendantView.toClientBytes(target));
					}
				}else if(type==TYPE_NORMAL){
					pt.put(target.attendant==null ? 0 : 1);
					if(target.attendant != null){
						pt.put(target.attendant.toClientBytes(target));
					}
				}
				//是否七彩光效
				if(Server.server.revision.equals(Server.REVISION_TYPE_TW)){
					pt.put(target.equipments.getFlashLevel()== 6?1:0);
				}
				//贡献度
				int dayConMax = 0;
				int dayCon = 0;
				int allCon = 0;
				TongService ts = Server.server.getServiceRegistry().getTongService();
				Tong tong = ts.getPlayerTong(target.id,true);
				allCon = target.contribute;
				dayCon = target.contributeDay;
				if(tong != null){
					dayConMax = ts.getContributeTop(tong);
				}
				pt.putInt(dayConMax);
				pt.putInt(dayCon);
				pt.putInt(allCon);
				pt.put(target.cards.equipCards.length);
				for(CardInfo info : target.cards.equipCards){
					if(info==null)
						pt.put(0);
					else{
						pt.put(1);
						pt.putInt(info.cardId);
						pt.putUTF(service.getCardByCardId(info.cardId).title);
						pt.put(info.level);
						pt.putUTF(service.getEnhanceDesc(info.cardId, info.level));
					}
				}
				pt.put(target.cards.horseEquipCards.length);
				for(CardInfo info : target.cards.horseEquipCards){
					if(info==null)
						pt.put(0);
					else{
						pt.put(1);
						pt.putInt(info.cardId);
						pt.putUTF(service.getCardByCardId(info.cardId).title);
						pt.put(info.level);
						pt.putUTF(service.getEnhanceDesc(info.cardId, info.level));
					}
				}
				session.send(pt);
			} else {
				if(type==1){
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.PLAYER_INFO_CLIENT, "此玩家已离线，无法查询相关信息 。");
				}else{
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.PLAYER_INFO_CLIENT, peony.Messages.STRING_00497);
				}
			}
		}

	}
	
	protected boolean isCapital(int mapId){
		for(int id : capital){
			if(mapId==id)
				return true;
		}
		return false;
	}
	
	protected String getRankString(String creditString){
		if(getIndex(creditString)!=-1){
			return creditString+"("+classes[getIndex(creditString)]+")";
		}
		return creditString;
	}
	
	protected int getIndex(String creditString){
		for(int i=0;i<creditStrings.length;i++){
			if(creditString.equals(creditStrings[i])){
				return i;
			}
		}
		return -1;
	}

}
