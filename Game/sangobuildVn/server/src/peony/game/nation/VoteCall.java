package peony.game.nation;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class VoteCall extends ClientSessionAsyncCall {

	Player p;
	int serial;
	int kingId;
	public VoteCall(ClientSession session, Packet packet) {
		super(session);
		this.p = (Player)session.getClient();
		this.serial = packet.getInt();
		this.kingId = packet.getInt();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.CANDIDATE_VOTE_SERVER);
			pt.putInt(serial);
			if(p!=null)
				p.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.CANDIDATE_VOTE_CLIENT, errorMessage);
		}
	}

	public void run() {
		CandidateService candidateService = Server.server.getServiceRegistry().getCandidateService();
		try {
			candidateService.vote(p, p.faction, kingId);
		} catch (NationVoteException e) {
			error(e, e.getMessage());
		}
		addToClientSession();
	}

}
