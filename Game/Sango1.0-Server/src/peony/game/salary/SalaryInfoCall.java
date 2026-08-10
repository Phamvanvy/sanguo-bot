package peony.game.salary;

import peony.common.ClientSessionAsyncCall;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class SalaryInfoCall extends ClientSessionAsyncCall{

	int serial;
	int playerId;
	int page;
	int count;
	Player player;
	SalaryService service = Server.server.getServiceRegistry().getSalaryService();
	
	public SalaryInfoCall(ClientSession session,Packet packet) {
		super(session);
		serial = packet.getInt();
		playerId = packet.getInt();
		page = packet.getInt();
		count = packet.getInt();
		player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(success){
//			service.checkOnlineOneHour(player);
			Packet pt = new Packet(OpCode.SALARY_INFO_SERVER);
			service.getSalaryList(player,serial,pt,page,count);
			player.send(pt);
		}
	}

	public void run() {
		addToClientSession();
	}
}
