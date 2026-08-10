package peony.game.beautyparade;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class BeautyVoteCall extends ClientSessionAsyncCall {

	private int serial;
	private int targetPlayerId;
	private int voteType;
	private int count;
	private Player p;
	
	public BeautyVoteCall(Packet packet, ClientSession session) {
		super(session);
		this.serial = packet.getInt();
		this.targetPlayerId = packet.getInt();
		this.voteType = packet.getByte();
		this.count = packet.getInt();
		this.p = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.BEAUTYPARADE_VOTE_SERVER);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.BEAUTYPARADE_VOTE_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(p!=null){
			BeautyParadeService service = Server.server.getServiceRegistry().getBeautyParadeService();
			try {
				service.vote(p, targetPlayerId, voteType, count);
			} catch (BeautyParadeException e) {
				error(e.getMessage());
			}
		}
		addToClientSession();
	}

}
