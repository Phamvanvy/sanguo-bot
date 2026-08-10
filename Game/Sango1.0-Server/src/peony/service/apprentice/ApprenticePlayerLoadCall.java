package peony.service.apprentice;

import peony.common.ClientSessionAsyncCall;
import peony.game.Player;
import peony.net.ClientSession;
import peony.game.Server;

public class ApprenticePlayerLoadCall extends ClientSessionAsyncCall{

	Player p;
	public ApprenticePlayerLoadCall(ClientSession session,Player player) {
		super(session);
		this.p = player;
	}

	public void callFinish() throws Exception {
		
		
	}

	public void run() {
		 if(p!=null){
			 ApprenticeService service = Server.server.getServiceRegistry().getApprenticeService();
			 service.playerLoad(p);
		 }
	}

}
