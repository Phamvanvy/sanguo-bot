package peony.service.tong;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * 专属科技升级
 * @author pmeng
 */
public class TongSkillLevelUpCall extends ClientSessionAsyncCall {

	private int serial;
	private int tongSkillId;
	private int currentLevel;
	private int contribute;
	
	public TongSkillLevelUpCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.tongSkillId = packet.getInt();
		this.currentLevel = packet.getInt();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.TONG_LEVELUP_SKILL_SERVER);
			pt.putInt(serial);
			pt.putInt(contribute);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_LEVELUP_SKILL_CLIENT,errorMessage);
		}
	}

	public void run() {
		Player p = (Player)session.getClient();
		if(p!=null){
			TongService ts = Server.server.getServiceRegistry().getTongService();
			try {
				ts.levelUpSkill(p, tongSkillId, currentLevel);
				contribute = p.contribute;
			} catch (TongException e) {
				error(e.getMessage());
			}
			addToClientSession();
		}
	}
}
