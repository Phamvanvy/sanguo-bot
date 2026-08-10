package peony.service.accountbinding;

import peony.common.ClientSessionAsyncCall;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class AccountBindStatusCall extends ClientSessionAsyncCall {
	
	int serial;
	int accountId;
	
	public AccountBindStatusCall(ClientSession session,Packet pt){
		super(session);
		serial = pt.getInt();
		accountId = session.getIdentity().getId();
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		byte code = Server.server.getServiceRegistry().getAccountBindingService().getAccountBindingStatus(accountId);
		Packet pt = new Packet(OpCode.ACCOUNTBINDING_STATUS_SERVER);
		pt.putInt(serial);
		pt.put(code);
		session.send(pt);
	}

}
