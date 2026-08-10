package peony.service.tong;

import peony.common.ClientSessionAsyncCall;
import peony.game.OpCode;
import peony.game.Player;
import peony.net.ClientSession;
import peony.net.Packet;

public class TongSkillDescCall extends ClientSessionAsyncCall {

	private int serial;
	private int[] skillids;
	private int[] skilllevel;
	
	public TongSkillDescCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		int len = packet.getShort();
		skillids = new int[len];
		skilllevel = new int[len];
		for(int i=0;i<len;i++){
			skillids[i] = packet.getInt();
			skilllevel[i] = packet.getByte();
		}
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		Player p = (Player)session.getClient();
		if(p!=null){
			TongSkill[] skills = new TongSkill[skillids.length];
			Packet pt = new Packet(OpCode.TONG_SKILL_DESC_SERVER);
			pt.putInt(serial);
			pt.putShort(skills.length);
			for(int i=0;i<skills.length;i++){
				skills[i] = TongService.skills.get(skillids[i]);
			}
			for(int i=0;i<skillids.length;i++){
				pt.putInt(skills[i].id);
				pt.put(skilllevel[i]);
				pt.putInt(skills[i].getUpgradeMoney(skilllevel[i]));
				pt.putInt(skills[i].getMaintainMoney(skilllevel[i]));
				pt.putString(skills[i].getDesc(skilllevel[i]));
			}
			session.send(pt);
		}
	}

}
