package peony.game.association;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.buff.BuffUtil;
import peony.util.IStringValidator;
import peony.util.StringUtil;

/**
 * 结义血盟
 * @author dchen
 */
@Entity
@AccessType("field")
@Table(name="association")
public class Association {

	/** 结盟最大人数 */
	public static int MAXPLAYER = 8;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	
	/** 血盟名称 */
	@Column(name="name",nullable=false)
	public String name = ""; 
	
	/** 创始人playerId */
	@Column(name="createplayerid",nullable=false)
	public int createPlayerId;
	
	/** 成员 */
	@Column(name="members")
	@Type(type="peony.game.association.AssociationMemberUserType")
	public AssociationMemberList memberList = new AssociationMemberList();
	
	/** 成员加入过期时间 */
	@Transient
	public static final int OUTTIME = 24 * 3600 * 1000;
	
	public Association(){
		
	}
	
	public Association(Player p, String name) throws AssociationException{
		if(p!=null){
			if(StringUtil.isValidText(name)!=IStringValidator.OK || StringUtil.hasBadWord(name))
				throw new AssociationException("血盟的名号十分重要，使用不雅词汇会有损各位壮士的威名，请换一个更响亮的名号吧。");
			this.name = name;
			this.createPlayerId = p.id;
			addMember(p.id, AssociationMember.DUTY_LEADER, AssociationMember.STAT_WORK);
		}
	}
	
	public boolean isFull(int addPlayerId){
		if(getMember(addPlayerId)!=null && getMember(addPlayerId).state==AssociationMember.STAT_WAIT)
			return false;
		return memberList.members.size()>=MAXPLAYER;
	}
	
	public int getMemberCount(){
		return memberList.members.size();
	}
	
	public void addMember(int playerId, int duty, int state) throws AssociationException{
		if(isFull(playerId))
			throw new AssociationException("兄弟贵在真情义，不在数量，当前的血盟中兄弟人数已经达到上限。");
		if(getMember(playerId)!=null && getMember(playerId).state==state)
			throw new AssociationException("您已加入了血盟，无法重复加入。");
		if(getMember(playerId)!=null && getMember(playerId).state==AssociationMember.STAT_WORK)
			throw new AssociationException("您已在当前血盟之中，无需重复加入。");
		AssociationMember member = new AssociationMember(duty, playerId, state);
		removeMember(playerId);
		memberList.members.add(member);
	}
	
	public void removeMember(int playerId) throws AssociationException{
		Iterator<AssociationMember> it = memberList.members.iterator();
		while(it.hasNext()){
			AssociationMember member = it.next();
			if(member.playerId==playerId){
				it.remove();
				Player p = ObjectAccessor.getPlayer(member.playerId);
				if(p!=null)
					p.associationInvite = null;
			}
		}
	}
	
	public AssociationMember getMember(int playerId){
		Iterator<AssociationMember> it = memberList.members.iterator();
		while(it.hasNext()){
			AssociationMember member = it.next();
			if(member.playerId==playerId)
				return member;
		}
		return null;
	}
	
	public AssociationMember getLeader(){
		for(AssociationMember mem : memberList.members){
			if(mem.duty==AssociationMember.DUTY_LEADER)
				return mem;
		}
		return null;
	}
	
	public void update(){
		processBuff();
		Iterator<AssociationMember> it = memberList.members.iterator();
		while(it.hasNext()){
			AssociationMember mem = it.next();
			int state = mem.state;
			long inviteTime = mem.inviteTime;
			if(state==AssociationMember.STAT_WAIT && inviteTime+OUTTIME<System.currentTimeMillis()){
				it.remove();
				LogUtil.logRemoveFromAssCauseTimeOut(mem.playerId);
				AssociationService service = Server.server.getServiceRegistry().getAssociationService();
				try {
					service.removeFromAssociation(mem.playerId);
					Player player = ObjectAccessor.getPlayer(mem.playerId);
					if(player!=null){
						player.associationInvite = null;
						player.message(-1, "壮士，您未能在24小时内完成结义任务，无法与兄弟们歃血为盟。", -1, -1);
					}
				} catch (AssociationException e) {
					
				}
			}
		}
	}
	
	protected void processBuff(){
		Map<Integer, List<Player>> map = new HashMap<Integer, List<Player>>();
		for(AssociationMember mem : memberList.members){
			Player p = ObjectAccessor.getPlayer(mem.playerId);
			if(mem.state==AssociationMember.STAT_WAIT){
				removeBuff(p);
				continue;
			}
			if(p!=null){
				if(p.party!=null){
					int partyId = p.party.id;
					if(map.get(partyId)==null){
						List<Player> l = new ArrayList<Player>();
						l.add(p);
						map.put(partyId, l);
					}else{
						Player m = map.get(partyId).get(0);
						if(m!=null && m.map.getId()==p.map.getId()){
							map.get(partyId).add(p);
						}
					}
				}
			}
		}
		Iterator<Integer> it = map.keySet().iterator();
		while(it.hasNext()){
			int partyId = it.next();
			List<Player> list = map.get(partyId);
			if(list==null || list.size()==0)
				continue;
			if(list.size()==1){
				removeBuff(list.get(0));
				continue;
			}
			int size = list.size();
			int buff = AssociationService.BUFFS[size];
			if(buff>0){
				for(Player p : list){
					if(p.buffs.getBuffByID(buff)!=null)
						continue;
					else{
						removeBuff(p);
					}
					p.buffs.addBuff(BuffUtil.createBuff(buff, 1, p, p, 0));
				}
			}
		}
	}
	
	private void removeBuff(Player p){
		if(p!=null){
			for(int buffId : AssociationService.BUFFS){
				if(buffId!=0 && p.buffs.getBuffByID(buffId)!=null)
					p.buffs.removeBuff(buffId);
			}
		}
	}
	
}
