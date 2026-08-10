package peony.game.nation;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class CandidateSignUpCall extends ClientSessionAsyncCall {

	private Player p;
	private Packet pt;
	int serial;
	public CandidateSignUpCall(ClientSession session, Packet packet) {
		super(session);
		Player p = (Player)session.getClient();
		this.p = p;
		this.pt = packet;
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.CANDIDATE_SIGNUP_SERVER);
			pt.putInt(serial);
			if(p!=null)
				p.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.CANDIDATE_SIGNUP_CLIENT, errorMessage);
		}
	}

	public void run() {
		CandidateService candidateService = Server.server.getServiceRegistry().getCandidateService();
		serial = pt.getInt();
		try {
			candidateService.signUp(p, serial);
		} catch (NationVoteException e) {
			error(e, e.getMessage());
		}
		addToClientSession();
	}

}
