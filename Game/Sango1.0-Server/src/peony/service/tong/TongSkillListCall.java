package peony.service.tong;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class TongSkillListCall extends ClientSessionAsyncCall {
	
	int serial;
	
	public TongSkillListCall(ClientSession session, Packet packet){
		super(session);
		this.serial = packet.getInt();
	}
	
	public void callFinish() throws Exception {

	}

	public void run() {
		synchronized (Server.server.getServiceRegistry().getTongService()) {
			Player p = (Player)session.getClient();
			if(p != null){
				Tong tong = Server.server.getServiceRegistry().getTongService().getPlayerTong(p.id,true);
				TongMember  tm = Server.server.getServiceRegistry().getTongService().getPlayerInfo(p.id);
				if(tong != null && tm != null){
					Packet pt = new Packet(OpCode.TONG_SKILL_LIST_SERVER);
					pt.putInt(serial);
					int num = tong.skills.size() + tm.skills.size();
					pt.putShort(num);
					for(TongSkill skill:tong.skills.skills.values()){
						pt.putInt(skill.id);
						pt.putString(skill.name);
						pt.put(skill.level);
						pt.put(skill.maxLevel);
						pt.put(skill.getSkillType());
						pt.putInt(skill.getMaintainContribute(skill.level));
					}
					for(TongSkill skill:tm.skills.skills.values()){
						pt.putInt(skill.id);
						pt.putString(skill.name);
						pt.put(skill.level);
						pt.put(skill.maxLevel);
						pt.put(skill.getSkillType());
						if(skill.level == skill.maxLevel){
							pt.putInt(skill.getMaintainContribute(skill.level - 1));
						}else{
							pt.putInt(skill.getMaintainContribute(skill.level));
						}
					}
					pt.putInt(p.contribute);
					p.send(pt);
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_SKILL_LIST_CLIENT, peony.Messages.STRING_01858);
				}
			}
		}
	}

}
