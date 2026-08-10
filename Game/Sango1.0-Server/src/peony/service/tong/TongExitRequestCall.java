package peony.service.tong;

import peony.common.ClientSessionAsyncCall;
import peony.db.DBService;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.tong.apply.TongBattleApplyService;

public class TongExitRequestCall extends ClientSessionAsyncCall {
	
	protected int serial;
	protected int type;
	protected Player player;
	TongService service = Server.server.getServiceRegistry().getTongService();

	public TongExitRequestCall(ClientSession session,Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.type = packet.get();
		player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		int num = service.getPlayerContributeInTong(player);
		Packet pt = new Packet(OpCode.TONG_REQUEST_EXIT_SERVER);
		pt.putInt(serial);
		pt.putInt(num);
		player.send(pt);
		
	}

	public void run() {
		if(player != null){
			if(type == 0){
				TongBattleApplyService applyService = Server.server.getServiceRegistry().getTongBattleApplyService();
				Tong tong = service.getPlayerTong(player.id,true);
				TongMember member = service.getPlayerInfo(player.id);
				if(tong==null || member==null){
					player.message(-1, peony.Messages.STRING_00676, -1, -1);
					return;
				}
				if(applyService.getApplyByTongId(tong.id)!=null){
					player.message(-1, peony.Messages.STRING_00675, -1, -1);
					return;
				}
				if(tong.members!=null && tong.members.size()>1){
					player.message(-1, peony.Messages.STRING_00674, -1, -1);
					return;
				}
			}
			addToClientSession();
		}
	}

}
