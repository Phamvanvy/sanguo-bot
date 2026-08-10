package peony.service.tong.apply;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class TongBattleApplyCall extends ClientSessionAsyncCall {

	private Player p;
	private int serial;
	private int mapId;
	private int money;
	
	public TongBattleApplyCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.mapId = packet.getInt();
		this.money = packet.getInt();
		this.p = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.TONG_BATTLE_APPLY_SERVER);
			pt.putInt(serial);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_APPLY_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(p!=null){
			TongBattleApplyService applyService = Server.server.getServiceRegistry().getTongBattleApplyService();
			try {
				applyService.apply(p, mapId, money);
			} catch (TongBattleApplyException e) {
				error(e.getMessage());
			}
			addToClientSession();
		}
	}

}
