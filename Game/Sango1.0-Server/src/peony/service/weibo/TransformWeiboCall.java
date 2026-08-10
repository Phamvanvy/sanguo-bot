package peony.service.weibo;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.tong.TongMember;
import peony.service.tong.TongService;
import weibo4j.WeiboException;

public class TransformWeiboCall extends ClientSessionAsyncCall{
	
	int serial;
	int targetId;
	Player p = null;

	public TransformWeiboCall(ClientSession session,Packet packet) {
		super(session);
		serial = packet.getInt();
		targetId = packet.getInt();
		p = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(success){
			if(p!=null){
				Actor targetActor = Server.server.getServiceRegistry().getActorCacheService().find(targetId);
				Packet pt = new Packet(OpCode.TRANSFORM_WEIBO_SERVER);
				pt.putInt(serial);
				if(targetActor!=null){
					TongService tongService = Server.server.getServiceRegistry().getTongService();
					TongMember tongMember = tongService.getPlayerInfo(p.id);
					TongMember targetTongMember = tongService.getPlayerInfo(targetId);
					if(p.faction != targetActor.faction){
						pt.put(1);
					} else {
						if(tongMember!=null && targetTongMember == null){
							pt.put(0);
						} else {
							pt.put(1);
						}
					}
				} else {
					pt.put(1);
				}
				p.send(pt);
			}
		}
	}

	public void run() {
		addToClientSession();
	}
}
