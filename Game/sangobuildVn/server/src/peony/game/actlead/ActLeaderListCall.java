package peony.game.actlead;

import java.util.List;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class ActLeaderListCall extends ClientSessionAsyncCall {

	private int serial;
	private int levelType;
	private int timeType;
	private int type;
	private int rewardType;
	private int startPage;
	private int pageCount;
	private Player p;
	
	public ActLeaderListCall(ClientSession session, Packet packet) {
		super(session);
		serial = packet.getInt();
		levelType = packet.getByte();
		timeType = packet.getByte();
		type = packet.getByte();
		rewardType = packet.getByte();
		startPage = packet.getShort();
		pageCount = packet.getShort();
		p = (Player) session.getClient();
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		if(p!=null){
			ActLeaderService service = Server.server.getServiceRegistry().getActLeaderService();
			Packet pt = new Packet(OpCode.INDICATOR_AREA_TASK_SERVER);
			pt.putInt(serial);
			if(timeType<=0 && type<=0 && rewardType<=0){
				List<ActLeaderMap> list = service.getActLeaderMaps(service.getLevelByLevelType(
						levelType, p.level), p.faction, startPage, pageCount);
				if(startPage!=0 && list.size()==0){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.INDICATOR_AREA_TASK_CLIENT, "Không phù hợp với dữ liệu yêu cầu");
					return;
				}
				pt.putShort(list.size());
				for(ActLeaderMap map : list){
					pt.putString(map.mapName);
					pt.putInt(map.mapId);
					pt.putString(map.mapName);
					pt.putInt(map.x/8);
					pt.putInt(map.y/8);
				}
			}else{
				List<ActLeader> list = service.getActLeadersBy(timeType, 0, type, rewardType, 
						service.getLevelByLevelType(levelType, p.level), p.faction, startPage, pageCount);		
				if(startPage!=0 && list.size()==0){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.INDICATOR_AREA_TASK_CLIENT, "Không phù hợp với dữ liệu yêu cầu");
					return;
				}
				pt.putShort(list.size());
				for(ActLeader al : list){
					pt.putString(al.name);
					pt.putInt(al.mapId);
					pt.putString(al.mapName);
					pt.putInt(al.x/8);
					pt.putInt(al.y/8);
				}
			}
			session.send(pt);
		}
		addToClientSession();
	}

}
