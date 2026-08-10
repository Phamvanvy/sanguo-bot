package peony.db;

import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.service.stat.SavePvpInfoCall;

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
		Server.server.getServiceRegistry().getAccountDepotService().saveAccountDepot(player.accountId);
		player.buffs.removeUnitEffectBuffState();
		Server.server.getServiceRegistry().getDbService().schedule(new SavePvpInfoCall(player==null ? null : player.session, player));
	}

}
