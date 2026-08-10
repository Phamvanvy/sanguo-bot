package peony.marriage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import peony.db.PlayerRelationDAO;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.VMap;
import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.game.mail.MailService;
import peony.game.party.Party;
import peony.game.party.PartyMember;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.friend.PlayerRelation;
import peony.service.friend.RelationService;

public class MarriageService implements Service,ServiceEventListener {

	protected final Logger log = Logger.getLogger(MarriageService.class);
	private RelationService relationService = Server.server.getServiceRegistry().getRelationService();
	private PlayerRelationDAO dao = Server.server.getServiceRegistry().getDbService().playerRelationDAO;
	
	public static int MARRY_LECENCE_ID = 1007886;//结婚证
	public static int DIVORCE_LECENCE_ID = 1007887;//离婚证
	public static int MARRY_MONEY = 1314;
	
	/**
	 * 建立婚姻关系
	 */
	public void createMarriage(int manId, int womanId,int playerId) throws MarriageException {
		synchronized(this){
			if(isCouple(manId, womanId)!=-1){
				throw new MarriageException("您已经结过婚了");
			}
			PlayerRelation entity;
			Player man = ObjectAccessor.getPlayer(manId);
			Player woman = ObjectAccessor.getPlayer(womanId);
			Player p = ObjectAccessor.getPlayer(playerId);
			if(man != null && woman != null){
				log.info("[MARRIAGE]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]TRY");
				PlayerTransaction tx = p.newTransaction("MGE");
				try {
					p.decMoney(2000, tx, false);
					if (Server.server.getServiceRegistry().getRelationService().get(manId) == null) {
						entity = dao.findPlayerRelation(manId);
						entity.addMate(womanId);
						dao.updateEntity(entity);
					}else{
						entity = Server.server.getServiceRegistry().getRelationService().get(manId);
						entity.addMate(woman.id);
						dao.updateEntity(entity);
					}
					if(Server.server.getServiceRegistry().getRelationService().get(womanId) == null){
						entity = dao.findPlayerRelation(womanId);
						entity.addMate(manId);
						dao.updateEntity(entity);
					}else{
						entity = Server.server.getServiceRegistry().getRelationService().get(womanId);
						entity.addMate(man.id);
						dao.updateEntity(entity);
					}
					if(man.party != null && man.party.contains(womanId) && woman.party != null && woman.party.contains(manId) && man.map.id == woman.map.id){
						Buff buff = BuffUtil.createSuiteBuff(121, 1);
						man.buffs.addBuff(buff);
						woman.buffs.addBuff(buff);
					}
					tx.commit();
					sendLecence(man,0);
					sendLecence(woman,0);
					Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_PLAYER_MARRIAGE, man, woman));
					log.info("[MARRIAGE]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]");
				} catch (NoEnoughValueException e) {
					tx.rollback();
					throw new MarriageException("需要2000注册费，您的金钱不足");
				}
			}
		}
	}
	
	/**
	 * 发证
	 * @param type  0：结婚证    1：离婚证
	 */
	private void sendLecence(Player player,int type){
		GameItem lecence = null;
		PlayerTransaction tx = null;
		String info = "";
		if(type == 0){//结婚
			tx = player.newTransaction("MAEEYLE");
			lecence = ObjectAccessor.createGameItem(MARRY_LECENCE_ID);
			info = "结婚证书";
		}else if(type == 1){
			tx = player.newTransaction("DIVORCELE");
			lecence = ObjectAccessor.createGameItem(DIVORCE_LECENCE_ID);
			info = "离婚证书";
		}
		if(lecence != null){
			if(!player.bag.addGameItem(lecence, 1, tx, true)){
				MailService mailService = Server.server.getServiceRegistry().getMailService();
				mailService.sendSystemMailAsync(player.id, "系统", info, "", 0, 
						lecence, 1, "MAEEYLE");
			}
			tx.commit();
		}else{
			tx.rollback();
		}
	}
	
	/**
	 * 离婚
	 */
	public void divorce(int manId, int womanId, int itemId, int instanceId, int playerId, int type) throws MarriageException {
		synchronized (this) {
			if(isCouple(manId, womanId)==-1){
				throw new MarriageException("您已经离过婚了");
			}
			PlayerRelation entity;
			Player man = ObjectAccessor.getPlayer(manId);
			Player woman = ObjectAccessor.getPlayer(womanId);
			Player player = ObjectAccessor.getPlayer(playerId); // 携带休书的人
			if(man != null && woman != null){
				if(type == 0){
					PlayerTransaction tx1 = man.newTransaction("DVC");
					PlayerTransaction tx2 = woman.newTransaction("DVC");
					try {
						man.decHonor(500, tx1, false);
						man.decMoney(20000, tx1, false);
						woman.decHonor(500, tx2, false);
						woman.decMoney(20000, tx2, false);
					} catch (NoEnoughValueException e) {
						tx1.rollback();
						tx2.rollback();
						throw new MarriageException("您或者对方的金钱或声望不足，不能离婚");
					}
					tx1.commit();
					tx2.commit();
					log.info("[MARRIAGEDIVORCE]"+LogUtil.getPlayerLogString(man)+LogUtil.getPlayerLogString(woman)+"PROTOCALDIVORCE TRY");
				}else if(type == 1){
					log.info("[MARRIAGEDIVORCE]"+LogUtil.getPlayerLogString(man)+LogUtil.getPlayerLogString(woman)+"FORCEDIVORCE TRY");
				}
				sendLecence(man,1);
				sendLecence(woman,1);
			}
			if(man == null || woman == null){//使用休书时另一方不在线的情况
				GameItem lecence = ObjectAccessor.createGameItem(DIVORCE_LECENCE_ID);
				MailService mailService = Server.server.getServiceRegistry().getMailService();
				if(man == null){
					mailService.sendSystemMailAsync(manId, "系统", "离婚证书", "", 0, 
							lecence, 1, "MAEEYLE");
				}else{
					sendLecence(man,1);
				}
				if(woman == null){
					mailService.sendSystemMailAsync(womanId, "系统", "离婚证书", "", 0, 
							lecence, 1, "MAEEYLE");
				}else{
					sendLecence(woman,1);
				}
			}
			if(Server.server.getServiceRegistry().getRelationService().get(manId) == null){
				entity = dao.findPlayerRelation(manId);
				entity.removeMate();
				dao.updateEntity(entity);
			}else{
				entity = Server.server.getServiceRegistry().getRelationService().get(manId);
				entity.removeMate();
				dao.updateEntity(entity);
			}
			if(Server.server.getServiceRegistry().getRelationService().get(womanId) == null){
				entity = dao.findPlayerRelation(womanId);
				entity.removeMate();
				dao.updateEntity(entity);
			}else{
				entity = Server.server.getServiceRegistry().getRelationService().get(womanId);
				entity.removeMate();
				dao.updateEntity(entity);
			}
			if(player != null){
				PlayerTransaction tx = player.newTransaction("DVC");
				GameItem item = player.bag.removeGameItem(itemId, instanceId, 1, tx, false);
				tx.commit();
				if(item != null){
					log.info("[MARRIAGEDIVORCE]"+LogUtil.getPlayerLogString(player)+"DECITEM["+LogUtil.getGameItemString(item, 1)+"]");
				}
			}
			if(man != null && woman != null && man.map.id == woman.map.id && man.party != null && man.party.contains(womanId)){
				man.buffs.removeBuff(121);
			    woman.buffs.removeBuff(121);
			}
		}
	}

	/**
	 * 判断双方是否已和别人结过婚
	 */
	public int isCouple(int manId, int womanId) {
		if(relationService.get(manId) != null){
			if(relationService.get(manId).mateId != -1){
				return manId;
			}
		}else{
			if(dao.findPlayerRelation(manId).mateId != -1){
				return manId;
			}
		}
		if(relationService.get(womanId) != null){
			if(relationService.get(womanId).mateId != -1){
				return womanId;
			}
		}else{
			if(dao.findPlayerRelation(womanId).mateId != -1){
				return womanId;
			}
		}
		return -1;
	} 

	/**
	 * 判断是否为朋友关系
	 */
	public int isFriend(int playerId, int otherId){
		RelationService rs = Server.server.getServiceRegistry().getRelationService();
		PlayerRelation relationList = null;
		PlayerRelation relationList1 = null;
		if(rs != null){
			relationList = rs.get(playerId);
			relationList1 = rs.get(otherId);
		}else{
			relationList = dao.findPlayerRelation(playerId);
			relationList1 = dao.findPlayerRelation(otherId);
		}
		if(relationList.friends.exists(otherId) && !relationList1.friends.exists(playerId)){
			return 0; // 对方未加你为好友
		}else if(relationList1.friends.exists(playerId) && !relationList.friends.exists(otherId)){
			return 1; // 未加对方为好友
		}else if(relationList.friends.exists(otherId) && relationList1.friends.exists(playerId)){
			return 2; // 互为好友
		}
		return -1; // 互相不是好友
	}

	public void shutdown() {

	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	public int[] getEventTypes() {
		return new int[]{
			ServiceEvent.EVENT_MEMBER_ADDED, // 玩家进入队伍事件
			ServiceEvent.EVENT_MEMBER_LEAVED, // 玩家离开队伍事件
			ServiceEvent.EVENT_PARTY_DESTROIED, // 队伍解散事件
			ServiceEvent.EVENT_MAP_PLAYER_ADDED, // 玩家进入场景事件
			ServiceEvent.EVENT_MAP_PLAYER_REMOVED // 玩家离开场景事件
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
			case ServiceEvent.EVENT_MEMBER_ADDED :
				enterParty((Party)event.param1,(PartyMember)event.param2);
				break;
			case ServiceEvent.EVENT_MEMBER_LEAVED :
				leaveParty((Party)event.param1,(PartyMember)event.param2);
				break;
			case ServiceEvent.EVENT_PARTY_DESTROIED :
				destroyParty((Party)event.param1);
				break;
			case ServiceEvent.EVENT_MAP_PLAYER_ADDED :
				addMap((VMap) event.param1, (Player) event.param2);
				break;
			case ServiceEvent.EVENT_MAP_PLAYER_REMOVED :
				removeMap((VMap) event.param1, (Player) event.param2);
				break;
		}
	}

	private void removeMap(VMap map, Player p) {
		if(p.party != null){
			int mateId = -1;
			if(relationService.get(p.id) != null){
				mateId = relationService.get(p.id).mateId;
			}else{
				mateId = dao.findPlayerRelation(p.id).mateId;
			}
			if(mateId == -1){
				return;
			}
			synchronized (p.party) {
				List<PartyMember> list = p.party.members;
				for(PartyMember member2 : list){
					if(member2.player.id == mateId && map.getId() == member2.player.map.id){
						p.buffs.removeBuff(121);
						member2.player.buffs.removeBuff(121);
					}
				}
			}
		}
	}

	private void addMap(VMap map, Player p) {
		if(p.party != null){
			int mateId = -1;
			if(relationService.get(p.id) != null){
				mateId = relationService.get(p.id).mateId;
			}else{
				mateId = dao.findPlayerRelation(p.id).mateId;
			}
			if(mateId == -1){
				return;
			}
			synchronized (p.party) {
				List<PartyMember> list = p.party.members;
				for(PartyMember member2 : list){
					if(member2.player.id == mateId && map.getId() == member2.player.map.id){
						Buff buff = BuffUtil.createSuiteBuff(121, 1);
						p.buffs.addBuff(buff);
						member2.player.buffs.addBuff(buff);
					}
				}
			}
		}
	}

	private void destroyParty(Party party) {
		Set<PartyMember> set = new HashSet<PartyMember>();
		synchronized (party) {
			List<PartyMember> list = party.members;
			for(PartyMember member : list){
				int mateId = -1;
				if(relationService.get(member.player.id) != null){
					mateId = relationService.get(member.player.id).mateId;
				}else{
					mateId = dao.findPlayerRelation(member.player.id).mateId;
				}
				if(mateId != -1){
					for(PartyMember member2 : list){
						if(member2.player.id == mateId && member.player.map.id == member2.player.map.id){
							if(!set.contains(member2)){
								member.player.buffs.removeBuff(121);
								member2.player.buffs.removeBuff(121);
							}
							//记录删除历史,防止重复删除Buff
							set.add(member2);
							set.add(member);
						}
					}
				}
			}
		}
	}

	private void leaveParty(Party party, PartyMember member) {
		Player p = member.player;
		int mateId = -1;
		if(relationService.get(p.id) != null){
			mateId = relationService.get(p.id).mateId;
		}else{
			mateId = dao.findPlayerRelation(p.id).mateId;
		}
		synchronized (party) {
			List<PartyMember> list = party.members;
			for(PartyMember member2 : list){
				if(member2.player.id == mateId && p.map.id == member2.player.map.id){
					p.buffs.removeBuff(121);
					member2.player.buffs.removeBuff(121);
				}
			}
		}
	}

	private void enterParty(Party party, PartyMember member) {
		Player p = member.player;
		int mateId = -1;
		if(relationService.get(p.id) != null){
			mateId = relationService.get(p.id).mateId;
		}else{
			mateId = dao.findPlayerRelation(p.id).mateId;
		}
		if(mateId == -1){
			return;
		}
		synchronized (party) {
			List<PartyMember> list = party.members;
			for(PartyMember member2 : list){
				if(member2.player.id == mateId && p.map.id == member2.player.map.id){
					Buff buff = BuffUtil.createSuiteBuff(121, 1);
					p.buffs.addBuff(buff);
					member2.player.buffs.addBuff(buff);
				}
			}
		}
	}

}
