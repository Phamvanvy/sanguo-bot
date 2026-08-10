package peony.db;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Title;
import peony.game.association.Association;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.duel.DuelService;
import peony.service.fame.Fame;
import peony.service.fame.FameService;
import peony.service.friend.PlayerRelation;
import peony.service.friend.RelationService;
import peony.service.player.ActorCacheService;

public class PlayerInfoCall extends ClientSessionAsyncCall {
	
	protected int serial;
	protected int id;
	protected String manteName;
	protected int[] capital = {272, 240, 352}; // 名人堂地图ID
	
	protected String[] creditStrings = {"新兵","精兵","军侯","校尉","都尉","裨将","偏将",
			"荡寇中郎将","羽林中郎将","讨虏将军","伏波将军","鹰扬将军","虎翼将军",
			"定国将军","安邦将军","上将军","大将军"}; //军衔名称
	
	protected String[] classes = {"Vô giá","Từ bậc một","Chính nhất giai","Từ giai đoạn 2","Chính Nhị Giai","Từ giai đoạn 3","Bậc 3 chính","Từ Tứ Giai","Chính tứ giai",
			"Từ giai đoạn  5","Giai đoạn chính ngũ","Từ bậc sáu","Chính lục đoạn","从七阶","Chính thất giai","Từ bậc 8","Chính bát giai"}; //军衔对应的等级

	public PlayerInfoCall(Packet pt,ClientSession session){
		super(session);
		this.serial = pt.getInt();
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
		Player player = (Player) session.getClient();
		if (player != null) {
			Player target = (Player) ObjectAccessor.getPlayer(id);
			if(target==null && fameService!=null && isCapital(player.getVMap().getId()))
				target = fameService.getStatue(id);
			if(target==null && duelService!=null)
				target = duelService.getStatue(id);
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
				pt.putString("");
				// 夫妻信息
				if(target.id<0){
					Fame fame = FameService.fames.get(target.id);
					if(fame!=null){
						if(fame.mateName!=null){
						  pt.putString(fame.mateName);
						} else {
							pt.putString("");
						}
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
								pt.putString("Ko biết");
							}else{
								pt.putString(actorCacheService.find(mateId).name);
							}
						} else {
							pt.putString("");
						}
					}
				}
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
				pt.put(target.horse==null ? 0 : 1);
				if(target.horse!=null){
					pt.put(target.horse.toClientBytes(target));
				}
				Association association = Server.server.getServiceRegistry().getAssociationService().getAssociationByPlayerId(target.id);
				if(association!=null)
					pt.putString(association.name);
				else
					pt.putString("Tạm thời không có ");
				session.send(pt);
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.PLAYER_INFO_CLIENT, "Người sử dụng đã rời mạng");
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
