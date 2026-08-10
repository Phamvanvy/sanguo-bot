package peony.game.nation;

import peony.common.ClientSessionAsyncCall;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class NationSkillListCall extends ClientSessionAsyncCall {

	protected int serial;
	
	public NationSkillListCall(ClientSession session, Packet packet){
		super(session);
		this.serial = packet.getInt();
	}
	
	public void callFinish() throws Exception {

	}

//	ID							int
//	名字							string
//	等级							byte
//	最高等级						byte
//	科技类型						byte(第0位为1表示可以领取道具)
	public void run() {
		Player p = (Player)session.getClient();
		if(p!=null){
			Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(p.faction);
			Packet pt = new Packet(OpCode.NATION_SKILL_LIST_SERVER);
			pt.putInt(serial);
			pt.putShort(nation.skills.size());
			for(NationSkill skill:nation.skills.skills.values()){
				pt.putInt(skill.id);
				pt.putString(skill.name);
				pt.put(skill.level);
				pt.put(skill.maxLevel);
				pt.put(skill.type);
			}
			session.send(pt);
		}
	}

}
