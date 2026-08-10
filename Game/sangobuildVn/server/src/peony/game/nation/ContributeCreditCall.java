package peony.game.nation;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class ContributeCreditCall extends ClientSessionAsyncCall {

	protected Player p;
	protected int serial;
	protected int credit;
	public ContributeCreditCall(ClientSession session, Packet packet) {
		super(session);
		this.p = (Player)session.getClient();
		this.serial = packet.getInt();
		this.credit = packet.getInt();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.CONTRIBUTECREDIT_SERVER);
			pt.putInt(serial);
			if(p!=null){
				p.send(pt);
			}
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.CONTRIBUTECREDIT_CLIENT, errorMessage);
		}
	}

	public void run() {
		CandidateService candidateService = Server.server.getServiceRegistry().getCandidateService();
		try {
			candidateService.contributeCredit(p, credit);
		} catch (NationVoteException e) {
			error(e, e.getMessage());
		}
		addToClientSession();
	}

}
