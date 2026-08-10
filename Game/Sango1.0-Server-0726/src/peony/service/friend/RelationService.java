package peony.service.friend;

import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import peony.db.PlayerDAO;
import peony.db.PlayerRelationDAO;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.GameMapDefinition;
import peony.game.GameObject;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapUtil;
import peony.game.chat.ChatService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.player.ActorCacheService;

/**
 * 管理玩家关系的服务。玩家的关系包括：好友关系、黑名单、仇人关系、临时关系。 
 * @author lighthu
 */
public class RelationService implements Service, ServiceEventListener {
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(RelationService.class);

	// 所有在线玩家的关系数据
	public Map<Integer, PlayerRelation> relations = new ConcurrentHashMap<Integer, PlayerRelation>();
	
	public static int FRIEND_ONLINE_REMAIN_TIME = 10 * 60 * 1000;

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	/**
	 * 服务关闭时保存所有载入的玩家关系数据。
	 */
	public void shutdown() {
		log.info("SHUTDOWN SAVE RELATIONS");
		Server.server.getEventManager().unregisterListener(this);
		PlayerRelationDAO dao = Server.server.getServiceRegistry().getDbService().playerRelationDAO;
		for (PlayerRelation r : relations.values()) {
			dao.updateEntity(r);
		}
		log.info("SHUTDOWN SAVE RELATIONS OK");
	}

	public int[] getEventTypes() {
		return new int[] {
				ServiceEvent.EVENT_PLAYER_LOGINED,
				ServiceEvent.EVENT_PLAYER_LOGOUTED,
				ServiceEvent.EVENT_PLAYER_LOADED,
				ServiceEvent.EVENT_PLAYER_UNLOADED,
				ServiceEvent.EVENT_PLAYER_SAVED,
				ServiceEvent.EVENT_INTERACT,
				ServiceEvent.EVENT_UNIT_DIE,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_PLAYER_LOGINED:
			playerLogined((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_LOGOUTED:
			playerLogouted((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_LOADED:
			playerLoaded((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_UNLOADED:
			playerUnloaded((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_SAVED:
			playerSaved((Player)event.param1);
			break;
		case ServiceEvent.EVENT_INTERACT:
			interact((Player)event.param1, (Player)event.param2, ((Integer)event.param3).intValue());
			break;
		case ServiceEvent.EVENT_UNIT_DIE:
			unitDie((Unit)event.param1,(Unit)event.param2);
		}
	}
	
	private void unitDie(Unit u1, Unit u2){
		if(u1.type==GameObject.TYPE_PLAYER&&u2.type==GameObject.TYPE_PLAYER){
			if(u1.faction!=u2.faction){
				PlayerRelation r = relations.get(u1.id);
				Actor a2 = Server.server.getServiceRegistry().getActorCacheService().find(u2.id);
				if (r != null) {
					r.addEnemy(a2,(Player)u1);
				}
			}
		}
	}
	
	/*
	 * 当玩家登录时，通知所有加了此玩家为好友的其他玩家。
	 * @param player
	 */
	private void playerLogined(Player player) {
		Packet pt = new Packet(OpCode.FRIEND_ONLINE_SERVER);
		pt.putInt(player.id);
		pt.putString(player.name);
		pt.put(1);
		ConcurrentHashMap<Integer,Player> players = ObjectAccessor.players;
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		PlayerRelation rel = relations.get(player.id);	
		for (Player p : players.values()) {
			if (p.session != null && isAttention(p.id, player.id)) {
				if(rel.friends.exists(p.id)){
					if(p.lastRemindFriendTime.containsKey(p.id)){
						if((Time.currTime -p.lastRemindFriendTime.get(p.id)) > FRIEND_ONLINE_REMAIN_TIME){
							 p.lastRemindFriendTime.put(p.id, Time.currTime);
							 chatService.sendPrivateMessage(p.id, "您的好友" + player.name + "上线了");
						 }
					}else{
						p.lastRemindFriendTime.put(p.id, Time.currTime);
						chatService.sendPrivateMessage(p.id, "您的好友" + player.name + "上线了");
					}
				}
				p.session.send(pt);
			}
		}
	}
	
	/*
	 * 当玩家下线时，通知所有加了此玩家为好友的其他玩家。
	 * @param player
	 */
	private void playerLogouted(Player player) {
		Packet pt = new Packet(OpCode.FRIEND_ONLINE_SERVER);
		pt.putInt(player.id);
		pt.putString(player.name);
		pt.put(0);
		ConcurrentHashMap<Integer,Player> players = ObjectAccessor.players;
		for (Player p : players.values()) {
			if (p.session != null && isAttention(p.id, player.id)) {
				p.session.send(pt);
			}
		}
	}
	
	/*
	 * 当玩家数据被载入时，同步载入其关系信息。
	 * @param player
	 */
	private void playerLoaded(Player player) {
		if (!relations.containsKey(player.id)) {
			PlayerRelationDAO dao = Server.server.getServiceRegistry().getDbService().playerRelationDAO;
			PlayerRelation relation = dao.findPlayerRelation(player.id);
			relations.put(player.id, relation);
		}
		player.relations = relations.get(player.id);
	}
	
	/*
	 * 当玩家数据被卸载时，同步卸载其关系信息。
	 * @param player
	 */
	private void playerUnloaded(Player player) {
		playerSaved(player);
		relations.remove(player.id);
	}
	
	/*
	 * 当玩家数据被保存时，同步保存其关系信息。
	 * @param player
	 */
	private void playerSaved(Player player) { 
		PlayerRelation r = relations.get(player.id);
		if (r != null) {
			PlayerRelationDAO dao = Server.server.getServiceRegistry().getDbService().playerRelationDAO;
			dao.updateEntity(r);
		}
	}
	
	/*
	 * 当玩家之间交互时，更新双方的临时好友表。
	 */
	private void interact(Player player1, Player player2, int actType) {
		if(player2!=null && player1!=null){
			addTempList(player2.id, player1.id, actType);
			addTempList(player1.id, player2.id, actType);
		}
	}
	
	/*
	 * 更新一个玩家的临时好友表。
	 */
	private void addTempList(int targetID, int sourceID, int actType) {
		PlayerRelation r = relations.get(targetID);
		if (r != null) {
			Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(sourceID);
			r.addTempList(actor, actType);
		}
	}
	
	/**
	 * 获取一个玩家的关系信息。
	 * @param playerID
	 * @return
	 */
	public PlayerRelation get(int playerID) {
		return relations.get(playerID);
	}
	
	/**
	 * 检查一个玩家是否关注另外一个玩家的状态。
	 * @param playerID 
	 * @param otherID
	 * @return 如果玩家playerID加了otherID为好友或者仇人，返回true。
	 */
	public boolean isAttention(int playerID, int otherID) {
		PlayerRelation rel = relations.get(playerID);
		if (rel == null) {
			return false;
		}
		return rel.friends.exists(otherID) || rel.enemies.exists(otherID);
	}
	
	public boolean canSee(Player source,Player dest){
	    if (source == null) {
	        return false;
	    }
	    if (source.party != null && source.party.contains(dest.id)) {
	        return true;
	    }
		PlayerRelation rel = relations.get(source.id);
		if (rel == null) {
			return false;
		}
		return (rel.friends.exists(dest.id) || rel.enemies.exists(dest.id)||rel.tempList.exists(dest.id));
	}
	
	/**
	 * 检查一个玩家是否始终能看见另外一个玩家。如果在关系表，或者在同一队伍，应该始终能看到。
	 * @param playerID
	 * @param otherID
	 * @return
	 */
	public boolean canSee(int playerID, int otherID) {
	    // 首先检查是否队友
	    Player p = ObjectAccessor.getPlayer(playerID);
	    if (p == null) {
	        return false;
	    }
	    if (p.party != null && p.party.contains(otherID)) {
	        return true;
	    }
		PlayerRelation rel = relations.get(playerID);
		if (rel == null) {
			return false;
		}
		return (rel.friends.exists(otherID) || rel.enemies.exists(otherID)||rel.tempList.exists(otherID));
	}
	
	/**
	 * 清理所有关联的社会关系
	 */
	public void removeAllRelation(Player player){
		PlayerRelation relation = get(player.id);
		if(relation==null){
			PlayerRelationDAO dao = Server.server.getServiceRegistry().getDbService().playerRelationDAO;
			relation = dao.findPlayerRelation(player.id);
			relations.put(player.id, relation);
		}
		ActorCacheService service = Server.server.getServiceRegistry().getActorCacheService();
		PlayerDAO dao = Server.server.getServiceRegistry().getDbService().playerDAO;
		Iterator<Actor> it = relation.friends.players.iterator();
		while(it.hasNext()){
			Actor actor = it.next();
			if(dao.getActor(actor.id)==null || service.find(actor.id).exist==0){
				it.remove();
				relation.friends.degrees.remove(actor.id);
			}
		}
		Iterator<Actor> it1 = relation.blackList.players.iterator();
		while(it.hasNext()){
			Actor actor = it1.next();
			if(dao.getActor(actor.id)==null || service.find(actor.id).exist==0){
				it1.remove();
				relation.blackList.degrees.remove(actor.id);
			}
		}
		Iterator<Actor> it2 = relation.enemies.players.iterator();
		while(it.hasNext()){
			Actor actor = it2.next();
			if(dao.getActor(actor.id)==null || service.find(actor.id).exist==0){
				it2.remove();
				relation.enemies.degrees.remove(actor.id);
			}
		}
		Iterator<Actor> it3 = relation.tempList.players.iterator();
		while(it.hasNext()){
			Actor actor = it3.next();
			if(dao.getActor(actor.id)==null || service.find(actor.id).exist==0){
				it3.remove();
				relation.tempList.degrees.remove(actor.id);
			}
		}
		if(relation.mateId!=-1 && dao.getActor(relation.mateId)==null){
			relation.removeMate();
		}
	}
	
	
	/** 查看仇人的方位
	 * @throws RelationServiceException 
     */
	public void searchPosition(Packet pt,ClientSession session) 
	              throws RelationServiceException {
		Player p = (Player)session.getClient();
		int serial = pt.getInt();
		int id = pt.getInt();
		if(p != null){
			Player target = (Player) ObjectAccessor.getPlayer(id);
			if(target!=null){
				PlayerTransaction tx = p.newTransaction("ENPOSITION");
				try{
					p.decMoney(5000, tx, true);
					log.info("[ENEMYPOSITION]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]TRY");
					tx.commit();
				} catch(NoEnoughValueException e) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial,
								OpCode.ENEMY_POSITION_CLIENT, "您的金钱不足");
					return;
				}
				Packet packet = new Packet(OpCode.ENEMY_POSITION_SERVICE);
				packet.putInt(serial);
				GameMapDefinition def = VMapUtil.getDefinition(target.map.id);
				packet.putString(def.mapInfo.name);   //发送地图的名称
				String position = target.map.map.getDirection(target.x, target.y);
				packet.putString(position);  
				p.send(packet);
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.ENEMY_POSITION_CLIENT, "用户不在线");
			}
		}
	}
}
