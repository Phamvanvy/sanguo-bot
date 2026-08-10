package peony.service.welfare;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
/**
 * ÇëÇó¸£Àû½±Àø
 * serial						int
 * welfareId 					int   ¸£Àûid
 */
public class WelfareRewardCall extends ClientSessionAsyncCall{
	int serial;
	int welfareId;
	Player player;
	
	public WelfareRewardCall(ClientSession session,Packet packet) {
		super(session);
		player = (Player)session.getClient();
		this.serial = packet.getInt();
		this.welfareId = packet.getInt();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.WELFARE_REWARD_SERVER);
			pt.putInt(serial);
			session.send(pt);
		}else{
			 ErrorHandler.sendErrorMessage(session, serial,
	                    OpCode.WELFARE_REWARD_CLIENT, errorMessage);
		}
	}

	public void run() {
		WelfareService service = Server.server.getServiceRegistry().getWelfareService();
		if(service != null){
			synchronized(service){
				int state = service.welfares2.get(welfareId).getWelfareState(player);
				if(state == Welfare.NOT_FINISH){
					error(peony.Messages.STRING_00702);
				}else if(state == Welfare.ALREADY_REWARD){
					error(peony.Messages.STRING_00703);
				}else if(state == Welfare.ALREADY_FINISH){
					service.getRewared(welfareId, player);
				}
			}
		}
		addToClientSession();
	}
}
