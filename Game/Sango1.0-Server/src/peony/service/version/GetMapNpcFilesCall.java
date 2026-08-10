package peony.service.version;

import java.util.Set;
import peony.common.ClientSessionAsyncCall;
import peony.game.DataService;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class GetMapNpcFilesCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int mapId;
	protected Player player;
	
	public GetMapNpcFilesCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.mapId = packet.getShort();
		this.player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		if(player!=null){
			DataService dataService = Server.server.getServiceRegistry().getDataService();
			synchronized (dataService) {
				String model = "";
				try {
					model = player.getAccount().getModel();
				} catch (Exception e) {
					model = player.accountModel;
				}
				Set<String> npcs = dataService.getNpcs(mapId, model);
				Packet pt = new Packet(OpCode.MAP_NPC_SERVER);
				pt.putInt(serial);
				if(npcs==null){
					pt.putInt(0);
				}else{
					pt.putInt(npcs.size());
					for(String name : npcs){
						pt.putString(name);
					}
				}
				player.send(pt);
			}
		}
	}

}
