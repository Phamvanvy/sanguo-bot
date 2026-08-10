package peony.service.tong;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class UnTagTongCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int playerId;
	protected Player p;
	
	public UnTagTongCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.playerId = packet.getInt();
		this.p = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.TONG_BATTLE_UNTAG_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_BATTLE_UNTAG_CLIENT, peony.Messages.STRING_00144);
		}
	}

	public void run() {
		synchronized (Server.server.getServiceRegistry().getTongService()) {
			if(p!=null){
				TongService tongService = Server.server.getServiceRegistry().getTongService();
				Tong tong = tongService.getPlayerTong(p.id,false);
				TongMember tmPlayer = tongService.getPlayerInfo(p.id);
				if(tong!=null && tmPlayer!=null && tmPlayer.duty>TongService.NORMAL){
					TongMember tm = tongService.getPlayerInfo(playerId);
					if(tm == null){
						for(TongMember t : tong.members){
							if(t.id == playerId){
								tm = t;
							}
						}
					}
					tm.battleTag = 0;
					for(TongMember t : tong.members){
						if(t.id == playerId){
							t.battleTag = 0;
						}
					}
					Server.server.getServiceRegistry().getDbService().tongMemberDAO.updateEntity(tm);
				}else{
					error(peony.Messages.STRING_00144);
				}
			}
			addToClientSession();
		}
	}

}
