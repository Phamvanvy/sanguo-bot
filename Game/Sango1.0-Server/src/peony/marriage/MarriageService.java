package peony.marriage;

import java.util.ArrayList;
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
import peony.game.Skills;
import peony.game.VMap;
import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.game.changed.RemoveSkillChangeItem;
import peony.game.mail.MailService;
import peony.game.party.Party;
import peony.game.party.PartyMember;
import peony.game.skill.Skill;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.friend.PlayerRelation;
import peony.service.friend.RelationList;
import peony.service.friend.RelationService;
import peony.service.stat.StatService;

public class MarriageService implements Service,ServiceEventListener {

	protected final Logger log = Logger.getLogger(MarriageService.class);
	
	private RelationService relationService = Server.server.getServiceRegistry().getRelationService();
	
	private PlayerRelationDAO dao = Server.server.getServiceRegistry().getDbService().playerRelationDAO;
	
	public static int MARRY_LECENCE_ID = 1007886;//结婚证
	
	public static int DIVORCE_LECENCE_ID = 1007887;//离婚证
	
	public static int MARRY_MONEY = 1314;
	
	public static String PROPERTY_WEDDING_BAN = "PROPERTY_WEDDING_BAN";//  伴郎/伴娘id
	
	public static int[] MATE_SKILL_ID = {420,421,422,423,424};//夫妻技能
	
	public static int ACTIVE_SKILL420 = 90;
	
	public static int[] ACTIVE_SKILL421 = {60,90,500,800};
	
	public static int[] ACTIRVE_BUFF = {0,571,572,573,574};
	
	
	
	/**
	 * 建立婚姻关系
	 */
	public void createMarriage(int manId, int womanId,int playerId) throws MarriageException {
		synchronized(this){
			if(isCouple(manId, womanId)!=-1){
				throw new MarriageException(peony.Messages.STRING_00794);
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
						if(entity.apprenticeList == null){
							entity.apprenticeList = new RelationList();
						}
						dao.updateEntity(entity);
					}else{
						entity = Server.server.getServiceRegistry().getRelationService().get(manId);
						entity.addMate(woman.id);
						if(entity.apprenticeList == null){
							entity.apprenticeList = new RelationList();
						}
						dao.updateEntity(entity);
					}
					if(Server.server.getServiceRegistry().getRelationService().get(womanId) == null){
						entity = dao.findPlayerRelation(womanId);
						entity.addMate(manId);
						if(entity.apprenticeList == null){
							entity.apprenticeList = new RelationList();
						}
						dao.updateEntity(entity);
					}else{
						entity = Server.server.getServiceRegistry().getRelationService().get(womanId);
						entity.addMate(man.id);
						if(entity.apprenticeList == null){
							entity.apprenticeList = new RelationList();
						}
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
					int v = Math.min(man.pool.getInt(WeddingService.PROPERTY_ENAIDU, 0), woman.pool.getInt(WeddingService.PROPERTY_ENAIDU, 0));
					refreshSkill(man,v);
					refreshSkill(woman,v);
					Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_PLAYER_MARRIAGE, man, woman));
					log.info("[MARRIAGE]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]");
				} catch (NoEnoughValueException e) {
					tx.rollback();
					throw new MarriageException(peony.Messages.STRING_00795);
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
			info = peony.Messages.STRING_00796;
		}else if(type == 1){
			tx = player.newTransaction("DIVORCELE");
			lecence = ObjectAccessor.createGameItem(DIVORCE_LECENCE_ID);
			info = peony.Messages.STRING_00797;
		}
		if(lecence != null){
			if(!player.bag.addGameItem(lecence, 1, tx, true)){
				MailService mailService = Server.server.getServiceRegistry().getMailService();
				mailService.sendSystemMailAsync(player.id, peony.Messages.STRING_00004, info, "", 0, 
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
				throw new MarriageException(peony.Messages.STRING_00798);
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
						throw new MarriageException(peony.Messages.STRING_00799);
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
					mailService.sendSystemMailAsync(manId, peony.Messages.STRING_00004, peony.Messages.STRING_00797, "", 0, 
							lecence, 1, "MAEEYLE");
				}else{
					sendLecence(man,1);
				}
				if(woman == null){
					mailService.sendSystemMailAsync(womanId, peony.Messages.STRING_00004, peony.Messages.STRING_00797, "", 0, 
							lecence, 1, "MAEEYLE");
				}else{
					sendLecence(woman,1);
				}
			}
			if(Server.server.getServiceRegistry().getRelationService().get(manId) == null){
				entity = dao.findPlayerRelation(manId);
				entity.removeMate();
				if(entity != null && entity.apprenticeList == null){
					entity.apprenticeList = new RelationList();
				}
				dao.updateEntity(entity);
			}else{
				entity = Server.server.getServiceRegistry().getRelationService().get(manId);
				if(entity != null && entity.apprenticeList == null){
					entity.apprenticeList = new RelationList();
				}
				entity.removeMate();
				dao.updateEntity(entity);
			}
			if(Server.server.getServiceRegistry().getRelationService().get(womanId) == null){
				entity = dao.findPlayerRelation(womanId);
				if(entity != null && entity.apprenticeList == null){
					entity.apprenticeList = new RelationList();
				}
				entity.removeMate();
				dao.updateEntity(entity);
			}else{
				entity = Server.server.getServiceRegistry().getRelationService().get(womanId);
				if(entity != null && entity.apprenticeList == null){
					entity.apprenticeList = new RelationList();
				}
				entity.removeMate();
				dao.updateEntity(entity);
			}
//			if(player != null){
//				PlayerTransaction tx = player.newTransaction("DVC");
//				GameItem item = player.bag.removeGameItem(itemId, instanceId, 1, tx, false);
//				tx.commit();
//				if(item != null){
//					log.info("[MARRIAGEDIVORCE]"+LogUtil.getPlayerLogString(player)+"DECITEM["+LogUtil.getGameItemString(item, 1)+"]");
//				}
//			}
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
			ServiceEvent.EVENT_MAP_PLAYER_REMOVED// 玩家离开场景事件
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
	
	public void refreshMateSkill(Player p){
		if(p!=null){
			int value = Math.min(p.mateenaidu, p.pool.getInt(WeddingService.PROPERTY_ENAIDU,0));
			Skill skill = p.skills.getSkillByGroupId(MATE_SKILL_ID[0]);
			if(skill == null){
				skill = p.skills.getSkillByGroup(MATE_SKILL_ID[0]);
			}
			if(value>=ACTIVE_SKILL420){
				if(skill == null){
					if(p.relations!=null && p.relations.mateId!=-1){
						skill = ObjectAccessor.getSkill(Skills.getSkillId(MATE_SKILL_ID[0], 1));
						p.skills.addSkillSlient(skill);
					}
				}else{
					if(p.relations!=null && p.relations.mateId!=-1){
						if(skill.getLevel()<1){
							p.skills.removeSkillByGroupId(skill.getGroupId());
							skill = ObjectAccessor.getSkill(Skills.getSkillId(MATE_SKILL_ID[0], 1));
							p.skills.addSkillSlient(skill);
						}
					}else{
						p.skills.removeSkillByGroupId(skill.getGroupId());
					}
				}
			}else{
				if(p.relations!=null && p.relations.mateId!=-1){
					if(skill == null){
						skill = ObjectAccessor.getSkill(Skills.getSkillId(MATE_SKILL_ID[0], 0));
						p.skills.addSkillSlient(skill);
					}else{
						if(skill.getLevel()>0){
							p.skills.removeSkillByGroupId(skill.getGroupId());
							skill = ObjectAccessor.getSkill(Skills.getSkillId(MATE_SKILL_ID[0], 0));
							p.skills.addSkillSlient(skill);
						}
					}
				}else{
					if(skill != null){
						p.skills.removeSkillByGroupId(MATE_SKILL_ID[0]);
					}
				}
			}
			skill = isIn(p);
			int index = getIndex(value);
			if(index>0){
				if(skill == null){
					if(p.relations!=null && p.relations.mateId!=-1){
						skill = ObjectAccessor.getSkill(Skills.getSkillId(MATE_SKILL_ID[index], 1));
						p.skills.addSkillSlient(skill);
					}
				}else{
					if(p.relations!=null && p.relations.mateId!=-1){
						if (skill.getGroupId()!=MATE_SKILL_ID[index]||skill.getLevel()!=1){
							p.skills.removeSkillByGroupId(skill.getGroupId());
							skill = ObjectAccessor.getSkill(Skills.getSkillId(MATE_SKILL_ID[index], 1));
							p.skills.addSkillSlient(skill);
						}
					}else{
						p.skills.removeSkillByGroupId(skill.getGroupId());
					}
				}
			}else{
				if(p.relations!=null && p.relations.mateId!=-1){
					if(skill == null){
						skill = ObjectAccessor.getSkill(Skills.getSkillId(MATE_SKILL_ID[1], 0));
						p.skills.addSkillSlient(skill);
					}else{
						if(skill.getLevel()>0){
							p.skills.removeSkillByGroupId(skill.getGroupId());
							skill = ObjectAccessor.getSkill(Skills.getSkillId(MATE_SKILL_ID[1], 0));
							p.skills.addSkillSlient(skill);
						}
					}
				}else{
					if(skill != null){
						p.skills.removeSkillByGroupId(skill.getGroupId());
					}
				}
			}
			for(Skill s :p.skills.getSkills()){
				if(s.getClazz()==5){
					Buff b = s.getAreaBuff();
					if(b!=null){
						Buff b0 = p.buffs.getBuffByID(b.getId());
						if(b0!= null){
							if(p.relations!=null){
								Player m = ObjectAccessor.getPlayer(p.relations.mateId);
								if(m==null || p.party==null || m.party==null|| p.party.id != m.party.id || p.map.getId() != m.map.getId()){
									p.buffs.removeBuff(b0);
								}
							}
						}
					}
				}
			}
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
	
	/**
	 * 判断某人是否已将结果婚
	 */
	public int isMarried(int playerId){
		if(relationService.get(playerId) != null){
			if(relationService.get(playerId).mateId != -1){
				return playerId;
			}
		}else{
			if(dao.findPlayerRelation(playerId).mateId != -1){
				return playerId;
			}
		}
		return -1;
	}
	
	
	public void refreshSkill(Player player,int value){
		List<Integer> list = new ArrayList<Integer>();
		Skill skill = player.skills.getSkillByGroupId(MATE_SKILL_ID[0]);
		if(skill == null){
			skill = player.skills.getSkillByGroup(MATE_SKILL_ID[0]);
		}
		if(player.relations!=null && player.relations.mateId!=-1){
			if(value>=ACTIVE_SKILL420){
				if(skill!=null){
					if(skill.getLevel()<1){
						player.skills.removeSkillByGroupId(skill.getGroupId());
//						list.add(skill.getGroupId());
						Skill sk = ObjectAccessor.getSkill(Skills.getSkillId(skill.getGroupId(), 1));
						player.addSkill(sk);
					}
				}else{
					Skill sk = ObjectAccessor.getSkill(Skills.getSkillId(MATE_SKILL_ID[0], 1));
					player.addSkill(sk);
				}
			}else{
				if(skill!=null){
					if(skill.getLevel()>0){
						player.skills.removeSkillByGroupId(skill.getGroupId());
//						list.add(skill.getGroupId());
						Skill sk = ObjectAccessor.getSkill(Skills.getSkillId(skill.getGroupId(), 0));
						player.addSkill(sk);
					}
				}else{
					Skill sk = ObjectAccessor.getSkill(Skills.getSkillId(MATE_SKILL_ID[0], 0));
					player.addSkill(sk);
				}
			}
		}else{
			if(skill!=null){
				player.skills.removeSkillByGroupId(skill.getGroupId());
				list.add(skill.getGroupId());
			}
		}
		
		skill = isIn(player);
		int index = getIndex(value);
		if(player.relations!=null && player.relations.mateId!=-1){
			if(skill!=null){
				if(index>0){
					if(skill.getGroupId()!=MATE_SKILL_ID[index] || skill.getLevel()<1){
						player.skills.removeSkillByGroupId(skill.getGroupId());
						if(skill.getGroupId()!=MATE_SKILL_ID[index]){
						    list.add(skill.getGroupId());
						}
						Buff b = skill.getAreaBuff();
						if(b!=null){
							Buff b0 = player.buffs.getBuffByID(b.getId());
							if(b0!= null){
								player.buffs.removeBuff(b0);
							}
						}
						Skill sk = ObjectAccessor.getSkill(Skills.getSkillId(MATE_SKILL_ID[index], 1));
						player.addSkill(sk);
						Buff b1 = sk.getAreaBuff();
						if(!checkMate(player) && b1!=null){
							Buff b00 = player.buffs.getBuffByID(b1.getId());
							if(b00!= null){
								player.buffs.removeBuff(b00);
							}
						}
						
					}
				}else{
					if(skill.getLevel()>0){
						player.skills.removeSkillByGroupId(skill.getGroupId());
						if(skill.getGroupId()!=MATE_SKILL_ID[1]){
						    list.add(skill.getGroupId());
						}
						Buff b = skill.getAreaBuff();
						if(b!=null){
							Buff b0 = player.buffs.getBuffByID(b.getId());
							if(b0!= null){
								player.buffs.removeBuff(b0);
							}
						}
						Skill sk = ObjectAccessor.getSkill(Skills.getSkillId(MATE_SKILL_ID[1], 0));
						player.addSkill(sk);
						Buff b1 = sk.getAreaBuff();
						if(b1!=null&&!checkMate(player)){
							Buff b00 = player.buffs.getBuffByID(b1.getId());
							if(b00!= null){
								player.buffs.removeBuff(b00);
							}
						}
					}
				}
				
			}else{
				Skill sk = ObjectAccessor.getSkill(Skills.getSkillId(MATE_SKILL_ID[1], 0));
				player.addSkill(sk);
				Buff b1 = sk.getAreaBuff();
				if(b1!=null){
					Buff b00 = player.buffs.getBuffByID(b1.getId());
					if(b00!= null){
						player.buffs.removeBuff(b00);
					}
				}
			}
		}else{
			if(skill!=null){
				player.skills.removeSkillByGroupId(skill.getGroupId());
				list.add(skill.getGroupId());
				Buff b = skill.getAreaBuff();
				if(b!=null){
					Buff b0 = player.buffs.getBuffByID(b.getId());
					if(b0!= null){
						player.buffs.removeBuff(b0);
					}
				}
			}
		}
		if(list!=null && list.size()>0){
		    int[] a = new int[list.size()];
		    for(int i=0;i<list.size();i++){
		    	a[i] = list.get(i);
		    }
			RemoveSkillChangeItem changedItem = new RemoveSkillChangeItem(list.size(),a, true);
			player.changed.addChangedItem(changedItem);
		}
		
//		if(player.relations!=null && player.relations.mateId!=-1){
//			if(value>=ACTIVE_SKILL420){
//				Skill sk = ObjectAccessor.getSkill(Skills.getSkillId(MATE_SKILL_ID[0], 1));
//				player.addSkill(sk);
//			}else{
//				Skill sk = ObjectAccessor.getSkill(Skills.getSkillId(MATE_SKILL_ID[0], 0));
//				player.addSkill(sk);
//			}
//			
//			int index = getIndex(value);
//			if(index>0){
//				Skill sk = ObjectAccessor.getSkill(Skills.getSkillId(MATE_SKILL_ID[index], 1));
//				player.addSkill(sk);
//				Buff b = sk.getAreaBuff();
//				Buff b0 = player.buffs.getBuffByID(b.getId());
//				if(b0!= null && !checkMate(player)){
//					player.buffs.removeBuff(b0);
//				}
//			}else{
//				Skill sk = ObjectAccessor.getSkill(Skills.getSkillId(MATE_SKILL_ID[1], 0));
//				player.addSkill(sk);
//			}
//		}
	}
	
	
	public Skill isIn(Player p){
		for(int i=1;i<MATE_SKILL_ID.length;i++){
			Skill skill = p.skills.getSkillByGroupId(MATE_SKILL_ID[i]);
			if(skill == null){
				skill = p.skills.getSkillByGroup(MATE_SKILL_ID[i]);
			}
			if(skill!=null){
				return skill;
			}
		}
		return null;
	}
	
	public int getIndex(int value){
		if(value>=ACTIVE_SKILL421[0] && value<ACTIVE_SKILL421[1]){
			return 1;
		}else if(value>=ACTIVE_SKILL421[1] && value<ACTIVE_SKILL421[2]){
			return 2;
		}else if(value>=ACTIVE_SKILL421[2] && value<ACTIVE_SKILL421[3]){
			return 3;
		}else if(value>=ACTIVE_SKILL421[3]){
			return 4;
		}else{
			return -1;
		}
	}

	public boolean checkMate(Player p){
		if(p!=null && p.relations!=null && p.party!=null){
			Player mate = ObjectAccessor.getPlayer(p.relations.mateId);
			if(mate!=null&&mate.party!=null && mate.party.id == p.party.id && p.map.id == mate.map.id){
				return true;
			}
		}
		return false;
	}

}
