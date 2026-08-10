package peony.game.buff;

import java.util.HashMap;
import java.util.Map;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.VMap;
import peony.game.party.Party;
import peony.game.party.PartyMember;
import peony.game.skill.Skill;
import peony.marriage.MarriageService;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

public class BuffService implements Service,ServiceEventListener{



	public void shutdown() {
		
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}
	
	public int[] getEventTypes() {
		return new int[] {
				ServiceEvent.EVENT_MAP_PLAYER_LOADED,
				ServiceEvent.EVENT_MEMBER_LEAVED,
				ServiceEvent.EVENT_MEMBER_ADDED,
				ServiceEvent.EVENT_PARTY_DESTROIED,
		};
	}
	
	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_MAP_PLAYER_LOADED:
			addToMap((VMap)event.param1,(Player)event.param2);
			break;
		case ServiceEvent.EVENT_MEMBER_LEAVED:
			memberLeaved((Party)event.param1,(PartyMember)event.param2);
			break;
		case ServiceEvent.EVENT_MEMBER_ADDED:
			memberAdded((Party)event.param1,(PartyMember)event.param2);
			break;
		case ServiceEvent.EVENT_PARTY_DESTROIED:
			partyDestoried((Party)event.param1);
			break;
		}
	}
	
	protected void partyDestoried(Party party){
		synchronized (party) {
			for (PartyMember member : party.members) {
				Player p = member.player;
				int tempHp = p.hp;//记录移除buff之前的血量
				int tempMp = p.mp;
				p.buffs.removeAreaBuffs();
				for(Buff buff:p.skills.getAreaBuffs()){
					p.buffs.addBuff(buff);
					p.setHp(tempHp, false);
					p.setMp(tempMp, false);
					Skill s = ObjectAccessor.getSkill(buff.getId());
					if(s!=null && s.getClazz() == 5)
					if(!checkMate(p)){
						p.buffs.removeBuff(buff);
					}
				}
			}
		}
	}
	
	protected void memberLeaved(Party party,PartyMember member){
		Player p = member.player;
		p.buffs.removeAreaBuffs();
		for(Buff buff:p.skills.getAreaBuffs()){
			p.buffs.addBuff(buff);
			Skill s = ObjectAccessor.getSkill(buff.getId());
			if(s!=null && s.getClazz() == 5)
			if(!checkMate(p)){
				p.buffs.removeBuff(buff);
			}
		}
		partyAreaBuffRefresh(party);
	}
	
	protected void memberAdded(Party party,PartyMember member){
		partyAreaBuffRefresh(party);
	}
	
	protected Map<VMap,Buffs> getAreaBuffs(Party party){
		Map<VMap,Buffs> ret = new HashMap<VMap,Buffs>();
		synchronized (party) {
			for(PartyMember member:party.members){
				Player p = member.player;
				if(p.systemState==Player.SYSTEMSTATE_READY&&p.isAlive()&&p.getVMap()!=null){
					Buffs buffs = ret.get(p.getVMap());
					if(buffs==null){
						buffs = new Buffs(p);
						ret.put(p.getVMap(), buffs);
					}
					for(Buff buff:p.skills.getAreaBuffs()){
						buffs.addBuff(buff);
					}
				}
			}
		}
		return ret;
	}
	
	
	protected void addToMap(VMap map, Player p) {
		if (p.party != null && p.party.getCount() > 0) {
			partyAreaBuffRefresh(p.party);
		}
	}
	
	protected void partyAreaBuffRefresh(Party party){
		Map<VMap, Buffs> mapBuffs = getAreaBuffs(party);
		synchronized (party) {
			for (PartyMember member : party.members) {
				Player mp = member.player;
				if (mp.isAlive() && mp.systemState == Player.SYSTEMSTATE_READY
						&& mp.getVMap() != null) {
					int tempHp = mp.hp;//记录移除buff之前的血量
					int tempMp = mp.mp;
					mp.buffs.removeAreaBuffs();
					Buffs buffs = mapBuffs.get(mp.getVMap());
					if(buffs!=null){
						mp.buffs.addBuffs(buffs);
						mp.setHp(tempHp, false);
						mp.setMp(tempMp, false);
						//特殊处理夫妻技能buff
						processMateBuff(mp);
					}
				}
			}
		}
	}
	public void processMateBuff(Player mp){
		synchronized(mp.buffs){
			for(int i=0;i<mp.buffs.buffs.size();i++){
				Buff b = mp.buffs.buffs.get(i);
				Skill s = ObjectAccessor.getSkill(b.getId());
				if(s!=null && s.getClazz() == 5){
					Buff b0 = mp.buffs.getBuffByID(b.getId());
					if(b0!=null){
					   mp.buffs.removeBuff(b0);
					   i--;
					}
				}
			}
			if(checkMate(mp)){
				MarriageService service = Server.server.getServiceRegistry().getMarriageService();
				Skill skill = service.isIn(mp);
				if(skill!=null){
					Buff b = skill.getAreaBuff();
					if(b!=null){
						mp.buffs.addBuff(b);
					}
				}
			}
		}
	}
	public static boolean checkMate(Player p){
		if(p!=null && p.relations!=null && p.party!=null){
			Player mate = ObjectAccessor.getPlayer(p.relations.mateId);
			if(mate!=null&&mate.party!=null && mate.party.id == p.party.id && p.map.id == mate.map.id){
				return true;
			}
		}
		return false;
	}
}
