package peony.game.nation;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class CollectCall extends ClientSessionAsyncCall {

	protected Player p;
	protected int serial;
	protected int money;
	public CollectCall(ClientSession session, Packet packet) {
		super(session);
		this.p = (Player)session.getClient();
		this.serial = packet.getInt();
		this.money = packet.getInt();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.COLLECT_SERVER);
			pt.putInt(serial);
			if(p!=null)
			p.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.COLLECT_CLIENT, errorMessage);
		}
	}

	public void run() {
		NationService nationService = Server.server.getServiceRegistry().getNationService();
		try {
			nationService.collect(p, money);
		} catch (NationVoteException e) {
			error(e, e.getMessage());
		}
		addToClientSession();
	}

}
