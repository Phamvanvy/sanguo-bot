package peony.service.welfare;

import peony.common.ClientSessionAsyncCall;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class WelfareListCall extends ClientSessionAsyncCall{
	
	ClientSession session;
	Packet packet;

	public WelfareListCall(ClientSession session,Packet packet) {
		super(session);
		this.session = session;
		this.packet = packet;
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		WelfareService service = Server.server.getServiceRegistry().getWelfareService();
		service.getWelfareList(packet, session);
	}

}
