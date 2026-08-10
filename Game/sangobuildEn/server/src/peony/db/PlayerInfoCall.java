package peony.db;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Title;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.fame.Fame;
import peony.service.fame.FameService;
import peony.service.friend.PlayerRelation;
import peony.service.friend.RelationService;
import peony.service.player.ActorCacheService;

public class PlayerInfoCall extends ClientSessionAsyncCall {
	
	protected int serial;
	protected int id;
	protected String manteName;

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
		Player player = (Player) session.getClient();
		if (player != null) {
			Player target = (Player) ObjectAccessor.getPlayer(id);
			if(target==null && fameService!=null)
				target = fameService.getStatue(id);
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
								pt.putString("未知");
							}else{
								pt.putString(actorCacheService.find(mateId).name);
							}
						} else {
							pt.putString("");
						}
					}
				}
				pt.putString(target.getCreditString());
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
				session.send(pt);
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.PLAYER_INFO_CLIENT, "該用戶已下線");
			}
		}

	}

}
