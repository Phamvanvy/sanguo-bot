package peony.game.admin;

import java.util.ArrayList;
import java.util.List;

import peony.channel.Channel;
import peony.common.ClientSessionAsyncCall;
import peony.db.DBService;
import peony.game.Admin;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class AdminGMRequestDeleteCall extends ClientSessionAsyncCall {

	protected int[] ids;
	
	
	public AdminGMRequestDeleteCall(ClientSession session,Packet packet){
		super(session);
		int len = packet.getShort();
		ids = new int[len];
		for(int i=0;i<len;i++){
			ids[i] = packet.getInt();
		}
	}
	
	public void callFinish() throws Exception {

	}

	public void run() {
		Admin admin = (Admin)session.getClient();
		DBService dbService = Server.server.getServiceRegistry().getDbService();
		List<Integer> l = new ArrayList<Integer>(ids.length);
		for(int i=0;i<ids.length;i++){
			GMRequest g = dbService.gmQuestDAO.getGMRequestById(ids[i]);
			if(g!=null){
				dbService.gmQuestDAO.makeTransient(g);
				l.add(g.getId());
			}
		}
		Channel channel = Server.server.getServiceRegistry().getChannelService().getChannel("gm");
		Packet pt = new Packet(OpCode.ADMIN_GMREQUEST_DELETE_SERVER);
		pt.putShort(l.size());
		for(int i:l){
			pt.putInt(i);
		}
		pt.putString(admin.name);
		channel.broadcast(pt, null);
	}

}
