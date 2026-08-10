package peony.alchemy;

import peony.common.ClientSessionAsyncCall;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.player.PlayerService;

public class AdminDecAlchemyExpCall extends ClientSessionAsyncCall {

	public int serial;
	public int accountId;
	public int playerId;
	public int alchemyExp;
	public AdminDecAlchemyExpCall(ClientSession session,Packet packet) {
		super(session);
		serial=packet.getInt();
		accountId=packet.getInt();
		playerId=packet.getInt();
		alchemyExp=packet.getInt();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.ADMIN_DEC_ALCHEMYEXP_SERVER);
		   pt.putInt(serial);
		   session.send(pt);
		}
	}

	public void run() {
		PlayerService service=Server.server.getServiceRegistry().getPlayerService();
		Player player=null;
		synchronized (service) {
			player=service.loadPlayer(accountId, playerId);
		}
		if(player!=null){
			int oldValue=player.alchemy.restExp;
			if(alchemyExp<0){
				if(-alchemyExp<oldValue){
					player.alchemy.restExp=oldValue+alchemyExp;
				}
			}else{
				player.alchemy.restExp=oldValue+alchemyExp;
			}
			int newValue=player.alchemy.restExp;
			player.alchemy.restExp=player.pool.getInt(AlchemyService.ALCHEMYEXP);
			LogUtil.logAlchemyExpDec(player, oldValue, newValue);
		}
		addToClientSession();
	}

}
