package peony.game.admin;



import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class AdminPlayerInfoCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int playerId;
	protected String playerName;
	protected Player p;
	
	public AdminPlayerInfoCall(ClientSession session,Packet pt){
		super(session);
		this.serial = pt.getInt();
		this.playerId = pt.getInt();
		this.playerName = pt.getString();
	}
	
	public void callFinish() throws Exception {
		if (p != null) {
			byte[] bytes = p.toClientBytes();
			byte[] bytes1 = p.bag.toClientBytes();
			byte[] bytes2 = p.skills.toClientBytes(p);
			byte[] bytes3 = p.horseBag.toClientBytes();
			byte[] bytes4 = p.depot == null ? new byte[0] : p.depot.toClientBytes(); 
			Packet pt = new Packet(OpCode.ADMIN_PLAYER_INFO_SERVER);
			pt.putInt(serial);
			pt.put(bytes);
			pt.put(bytes1);
			pt.put(bytes2);
			pt.put(bytes3);
			pt.putInt(p.getWeekCredit());
			pt.put(bytes4);
			session.send(pt);
		} else {
			ErrorHandler.sendAdminErrorMessage(session, serial,
					OpCode.ADMIN_PLAYER_INFO_CLIENT, "Không tìm thấy nhân vật chỉ định");
		}
	}

	public void run() {
		if(playerName.length()!=0){
			Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(playerName);
			if(actor==null){
				ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_PLAYER_INFO_CLIENT, "Không tìm thấy nhân vật chỉ định");
				return;
			}
			playerId = actor.id;
		}
		p = Server.server.getServiceRegistry().getPlayerService().loadPlayerSilent(playerId);
		addToClientSession();
	}

}
