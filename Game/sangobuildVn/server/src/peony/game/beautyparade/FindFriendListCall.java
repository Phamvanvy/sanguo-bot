package peony.game.beautyparade;

import java.util.List;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class FindFriendListCall extends ClientSessionAsyncCall {

	private int serial;
	private Player p;
	private List<Beauty> list;
	
	public FindFriendListCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.p = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(success){
			BeautyParadeService service = Server.server.getServiceRegistry().getBeautyParadeService();
			Packet pt = new Packet(OpCode.BEAUTY_FRIEND_LIST_SERVER);
			pt.putInt(serial);
			if(list==null || list.size()==0){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.BEAUTY_FRIEND_LIST_CLIENT, "<cff0000>没有好友！</c>\n<cff0000>Không có bạn bè！</c>");
				return;
			}else{
				pt.putInt(list.size());
				for(Beauty b : list){
					pt.putInt(service.getPositionInBeautys(b.playerId)+1);
					pt.putInt(b.playerId);
					pt.putString(b.name);
					pt.put(b.sex);
					pt.putString(b.slogan);
					pt.putInt(b.votes);
					pt.put(b.faction);
				}
				session.send(pt);
			}
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.BEAUTY_FRIEND_LIST_CLIENT, errorMessage);
		}
	}

	public void run() {
		BeautyParadeService service = Server.server.getServiceRegistry().getBeautyParadeService();
		try {
			this.list = service.getBeautysInFriend(p);
		} catch (BeautyParadeException e) {
			error(e.getMessage());
		}
		addToClientSession();
	}

}
