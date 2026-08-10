package peony.service.tong;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.tong.apply.TongBattleApplyService;
import peony.service.tong.apply.TongBattleException;

public class TagTongCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int playerId;
	protected Player p;
	
	public TagTongCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.playerId = packet.getInt();
		this.p = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.TONG_BATTLE_TAG_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_TAG_CLIENT, errorMessage);
		}
	}

	public void run() {
		synchronized (Server.server.getServiceRegistry().getTongService()) {
			if(p!=null){
				TongBattleApplyService applyService = Server.server.getServiceRegistry().getTongBattleApplyService();
				try {
					applyService.tagTongBattle(p, playerId);
				} catch (TongBattleException e) {
					error(e.getMessage());
				}
			}
			addToClientSession();
		}
	}

}
