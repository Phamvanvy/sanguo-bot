package peony.service.tong.apply;

import java.util.List;

import peony.common.ClientSessionAsyncCall;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class TongBattleApplyListCall extends ClientSessionAsyncCall {

	private Player p;
	private int serial;
	private int mapId;
	
	public TongBattleApplyListCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.mapId = packet.getInt();
		this.p = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		if(p!=null){
			TongBattleApplyService applyService = Server.server.getServiceRegistry().getTongBattleApplyService();
			List<TongBattleApply> list = applyService.applyList(mapId);
			Packet pt = new Packet(OpCode.TONG_BATTLEAPPLY_LIST_SERVER);
			pt.putInt(serial);
			if(list==null || list.size()==0){
				pt.putInt(0);
			}else{
				pt.putInt(list.size());
				for(TongBattleApply apply : list){
					pt.putString(apply.tongName==null ? "" : apply.tongName);
				}
			}
			session.send(pt);
			addToClientSession();
		}
	}

}
