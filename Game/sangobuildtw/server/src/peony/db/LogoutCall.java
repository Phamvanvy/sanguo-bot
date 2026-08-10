package peony.db;

import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;

public class LogoutCall extends DBAsyncCall {

	Player player;
	
	public LogoutCall(DBService dbService,ClientSession session,Player player){
		super(dbService,session);
		this.player = player;
	}
	
	public void callFinish() throws Exception {
		
	}

	public void run() {
//		player.systemState = Player.SYSTEMSTATE_DISCONNECTED;
		Server.server.getServiceRegistry().getPlayerService().savePlayer(player);
	}

}
