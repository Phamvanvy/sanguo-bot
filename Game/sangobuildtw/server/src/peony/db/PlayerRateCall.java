package peony.db;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.net.ClientSession;
import peony.net.Packet;

public class PlayerRateCall extends ClientSessionAsyncCall {

	int serial;
	int level;
	
	public PlayerRateCall(ClientSession session, Packet packet){
		super(session);
		this.serial = packet.getInt();
		this.level = packet.getInt();
	}
	
	public void callFinish() throws Exception {

	}

	public void run() {
		if(level<1||level>100){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.PLAYER_RATE_CLIENT, "請輸入1~100之間的等級");
			return;
		}
		Player p = (Player)session.getClient();
		if(p!=null){
			int v1 = (int)((p.critical + Math.min(p.level - level, 10)/100f)*100);
			v1 = Math.max(v1, 0);
			int v2 = (int)((p.spellcritical + Math.min(p.level - level, 10)/100f)*100);
			v2 = Math.max(v2, 0);
			int v3 = (int)((p.hit + (p.level - level)/100f)*100);
			v3 = Math.max(v3, 0);
			int v4 = (int)((p.spellhit + (p.level - level)/100f)*100);
			v4 = Math.max(v4, 0);
			int v5 = (int)(((1 - (1 - p.dodge)*(100 - (p.level - level))/100f)) * 100);
			v5 = Math.max(v5, 0);
			v5 = Math.min(v5, 100);
			int v6 = (int)(((1 - (1 - p.spelldodge)*(100 - (p.level - level))/100f)) * 100);
			v6 = Math.max(v6, 0);
			v6 = Math.min(v6, 100);
			Packet pt = new Packet(OpCode.PLAYER_RATE_SERVER);
			pt.putInt(serial);
			pt.putInt(level);
			pt.putInt(v1);
			pt.putInt(v2);
			pt.putInt(v3);
			pt.putInt(v4);
			pt.putInt(v5);
			pt.putInt(v6);
			session.send(pt);
		}
	}

}
