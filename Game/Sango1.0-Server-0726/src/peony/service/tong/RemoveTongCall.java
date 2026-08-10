package peony.service.tong;

import peony.common.ClientSessionAsyncCall;
import peony.db.DBService;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class RemoveTongCall extends ClientSessionAsyncCall {

	protected Player player;
	protected int serial;
	
	public RemoveTongCall(ClientSession session, Packet packet) {
		super(session);
		player = (Player)session.getClient();
		serial = packet.getInt();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.TONG_REMOVE_SERVER);
			pt.putInt(serial);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_REMOVE_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(player!=null){
			try {
				TongService service = Server.server.getServiceRegistry().getTongService();
				Tong tong = service.getPlayerTong(player.id);
				TongMember member = service.getPlayerInfo(player.id);
				if(tong!=null && member!=null){
					if(tong.members!=null && tong.members.size()==1){
						synchronized (tong) {
							tong.members.remove(member);
							service.removeTongMember(player.id);
							DBService dbs = Server.server.getServiceRegistry().getDbService();
							dbs.tongMemberDAO.makeTransient(member);
							service.removeTong(tong.id);
							player.setGuildName("");
						}
					}else{
						error("只有军团内没有其他成员时，才可解散军团");
					}
				}else{
					error("你还没有自己的军团");
				}
			} catch (Exception e) {
				error("解散不成功");
			}
		}
		addToClientSession();
	}

}
