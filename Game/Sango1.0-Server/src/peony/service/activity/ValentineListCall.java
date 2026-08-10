package peony.service.activity;

import java.util.ArrayList;
import java.util.List;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class ValentineListCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int type;
	protected Player player;
	
	protected List<IncompletePlayer> list = new ArrayList<IncompletePlayer>();
	protected int ownerCount = 0;
	
	public ValentineListCall(ClientSession session, Packet packet) {
		super(session);
		serial = packet.getInt();
		type = packet.getByte();
		player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.VALENTINE_RANKING_SERVER);
			pt.putInt(serial);
			pt.put(list.size());
			for(IncompletePlayer p : list){
				pt.putUTF(p.name);
				pt.put(p.faction);
				pt.putInt(p.count);
			}
			pt.putInt(ownerCount);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.VALENTINE_RANKING_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(player!=null){
			ActivityService service = Server.server.getServiceRegistry().getActivityService();
			synchronized (service) {
				Activity act = service.getActivityByImpClass("ValentineActivity");
				if (act == null || !act.isActive() || !act.isEnabled() /*|| !act.isVisible()*/) {
					error("活动尚未开始");
					addToClientSession();
					return;
				}
				ValentineActivity actImp = (ValentineActivity) act.getImpl();
				list = actImp.getTopList(player, type == 0 ? ValentineActivity.RANKTYPE_SEND : ValentineActivity.RANKTYPE_RECEIVE);
				IncompletePlayer owner = null;
				owner = actImp.getIncompletePlayerInfo(player.id, type == 0 ? ValentineActivity.RANKTYPE_SEND : ValentineActivity.RANKTYPE_RECEIVE);
				if (owner != null)
					ownerCount = owner.count;
			}
		}
		addToClientSession();
	}

}
