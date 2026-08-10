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
		Player p = (Player)session.getClient();
		if(p != null){
			Tong tong = Server.server.getServiceRegistry().getTongService().getPlayerTong(p.id);
			if(tong != null){
				Packet pt = new Packet(OpCode.TONG_SKILL_LIST_SERVER);
				pt.putInt(serial);
				pt.putShort(tong.skills.size());
				for(TongSkill skill:tong.skills.skills.values()){
					pt.putInt(skill.id);
					pt.putString(skill.name);
					pt.put(skill.level);
					pt.put(skill.maxLevel);
					pt.put(skill.type);
				}
				p.send(pt);
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_SKILL_LIST_CLIENT, "没有相应的军团");
			}
		}
	}

}
