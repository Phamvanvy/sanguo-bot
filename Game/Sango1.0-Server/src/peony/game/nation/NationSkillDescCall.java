package peony.game.nation;


import peony.common.ClientSessionAsyncCall;
import peony.game.OpCode;
import peony.game.Player;
import peony.net.ClientSession;
import peony.net.Packet;

public class NationSkillDescCall extends ClientSessionAsyncCall {
	
	protected int serial;
	protected int[] skillIds;
	protected int[] levels;

//	 * serial							int
//	 * 数量								short
//	 * 循环n次
//	 * 	科技ID							int
//	 * 	科技等级							byte
	public NationSkillDescCall(ClientSession session, Packet packet){
		super(session);
		this.serial = packet.getInt();
		int len = packet.getShort();
		skillIds = new int[len];
		levels = new int[len];
		for(int i=0;i<len;i++){
			skillIds[i] = packet.getInt();
			levels[i] = packet.get();
		}
	}
	
	public void callFinish() throws Exception {

	}

//	 * 国家科技描述
//	 * serial							int
//	 * 数量								short
//	 * 循环n次
//	 * 	科技ID							int
//	 * 	科技等级							byte
//	 * 	科技升级费用						int
//	 *  科技维护费用						int
//	 *  科技描述							string
	public void run() {
		Player p = (Player)session.getClient();
		if(p!=null){
			NationSkill[] skills = new NationSkill[skillIds.length];
			for(int i=0;i<skillIds.length;i++){
				skills[i] = NationService.getNationSkill(skillIds[i]);
			}
			Packet pt = new Packet(OpCode.NATION_SKILL_DESC_SERVER);
			pt.putInt(serial);
			pt.putShort(skillIds.length);
			for(int i=0;i<skills.length;i++){
				pt.putInt(skills[i].id);
				pt.put(levels[i]);
				pt.putInt(skills[i].getUpgradeMoney(levels[i]));
				pt.putInt(skills[i].getMaintainMoney(levels[i]));
				pt.putString(skills[i].getDesc(levels[i]));
			}
			session.send(pt);
		}
	}

}
