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
	
	protected String[] classes = {"无阶","从一阶","正一阶","从二阶","正二阶","从三阶","正三阶","从四阶","正四阶",
			"从五阶","正五阶","从六阶","正六阶","从七阶","正七阶","从八阶","正八阶"}; //军衔对应的等级

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
		boolean isStatue = false;
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
								pt.putString("未知");
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
					pt.putString("暂无");
				if(isStatue){
					target.id = -target.id;
				}
				//下发随从信息
				pt.put(target.attendant==null ? 0 : 1);
				if(target.attendant != null){
					pt.put(target.attendant.toClientBytes(target));
				}
				//是否七彩光效
				if(Server.server.revision.equals(Server.REVISION_TYPE_TW)){
					pt.put(target.equipments.getFlashLevel()== 6?1:0);
				}
				session.send(pt);
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.PLAYER_INFO_CLIENT, "该用户已下线");
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
