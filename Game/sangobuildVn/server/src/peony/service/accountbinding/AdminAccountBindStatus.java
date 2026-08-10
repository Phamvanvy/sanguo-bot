package peony.service.accountbinding;

import peony.common.ClientSessionAsyncCall;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class AdminAccountBindStatus extends ClientSessionAsyncCall {

	int serial;
	int accountId;

	public AdminAccountBindStatus(ClientSession session, Packet packet) {
		super(session);
		serial = packet.getInt();
		accountId = packet.getInt();
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		String[] ss = Server.server.getServiceRegistry()
				.getAccountBindingService().getAccountBindingBlurStatus(
						accountId);
		Packet pt = new Packet(OpCode.ADMIN_ACCOUNTBINDING_STATUS_SERVER);
		pt.putInt(serial);
		pt.putString(ss[0]);
		pt.putString(ss[1]);
		pt.putString(ss[2]);
		pt.putString(ss[3]);
		session.send(pt);
	}

}
