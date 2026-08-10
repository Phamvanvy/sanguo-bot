package peony.db;

import java.util.List;

import org.apache.log4j.Logger;

import peony.game.ActorListActor;
import peony.game.Equipments;
import peony.game.ErrorHandler;
import peony.game.GameMapDefinition;
import peony.game.ItemUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.VMapUtil;
import peony.net.ClientSession;
import peony.net.Packet;

public class LoadActorListCall extends DBAsyncCall {
	
	private static final Logger log = Logger.getLogger(LoadActorListCall.class);
	
	protected int serial;
	protected List<ActorListActor> actorList;
	
	public LoadActorListCall(int serial,DBService dbService,ClientSession session){
		super(dbService,session);
		this.serial = serial;
	}
	
	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.ACTOR_LIST_SERVER);
			pt.putInt(serial);
			pt.put(actorList.size());
			for(ActorListActor a:actorList){
				pt.putInt(a.id);
				pt.putString(a.name);
				pt.put(a.sex);
				pt.put(a.level);
				pt.put(a.clazz);
				pt.put(a.faction);
				pt.putInt(a.equipmentScores[0]);
				pt.putInt(a.equipmentScores[1]);
				pt.putInt(a.equipmentScores[2]);
				pt.put(a.equipmentScores[3]);
				GameMapDefinition def = VMapUtil.getDefinition(a.mapId);
				String mapName = null;
				if(def==null){
					mapName = "未知地圖";
				}else{
					mapName = def.mapInfo.name;
				}
				pt.putString(mapName);
			}
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.ACTOR_LIST_CLIENT, "查詢角色列表錯誤");
		}
	}

	public void run() {
		actorList = dbService.playerDAO.getActorList(session.getIdentity()
				.getId());
		for (ActorListActor a : actorList) {
			if (!ensureActor(a)) {
				if (a.equipments == null) {
					a.equipmentScores[0] = 0;
					a.equipmentScores[1] = 0;
					a.equipmentScores[2] = 0;
					a.equipmentScores[3] = 0;
				} else {
					Equipments es = ItemUtil.getEquipmentsFromDB(a.equipments,
							null);
					a.equipmentScores[0] = es.getHeadScore(a.level, a.clazz);
					a.equipmentScores[1] = es.getBodyScore(a.level, a.clazz);
					a.equipmentScores[2] = es.getWeaponScore(a.level, a.clazz);
					a.equipmentScores[3] = es.getFlashLevel();
				}
			}
		}
		success = true;
		addToClientSession();
	}
	
	protected boolean ensureActor(ActorListActor actor){
		Player p = Server.server.getServiceRegistry().getPlayerService().getFromCache(actor.id);
		if(p!=null){
			actor.clazz = p.clazz;
			actor.faction = p.faction;
			actor.level = p.level;
			actor.mapId = p.map.id;
			actor.name = p.name;
			actor.sex = p.sex;
			actor.equipmentScores[0] = p.equipments.getHeadScore(p.level, p.clazz);
			actor.equipmentScores[1] = p.equipments.getBodyScore(p.level, p.clazz);
			actor.equipmentScores[2] = p.equipments.getWeaponScore(p.level, p.clazz);
			actor.equipmentScores[3] = p.equipments.getFlashLevel();
			return true;
		}
		return false;
	}
}
