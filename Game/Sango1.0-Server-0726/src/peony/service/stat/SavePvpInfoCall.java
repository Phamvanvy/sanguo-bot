package peony.service.stat;

import peony.common.ClientSessionAsyncCall;
import peony.db.PvpInfoDAO;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;

public class SavePvpInfoCall extends ClientSessionAsyncCall {

	protected Player player;
	
	public SavePvpInfoCall(ClientSession session, Player player) {
		super(session);
		this.player = player;
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		if(player!=null){
			StatService service = Server.server.getServiceRegistry().getStatService();
			synchronized (service.pvpInfos) {
				PvpInfo pvpInfo = service.getPvpInfo(player.id, player.faction);
				if(pvpInfo!=null){
					PvpInfoDAO dao = Server.server.getServiceRegistry().getDbService().pvpInfoDAO;
					dao.updateEntity(pvpInfo);
				}
			}
		}
	}

}
