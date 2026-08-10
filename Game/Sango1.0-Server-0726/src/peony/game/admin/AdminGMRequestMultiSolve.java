package peony.game.admin;
import java.util.ArrayList;
import java.util.List;
import peony.channel.Channel;
import peony.common.ClientSessionAsyncCall;
import peony.db.DBService;
import peony.game.Admin;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
/**
 * 批量标注GM请求已被处理
 * @author pmeng
 */
public class AdminGMRequestMultiSolve extends ClientSessionAsyncCall{

	int[] ids;
	int serial;
	Admin admin = null;
	List<Integer> solveIds = null;
	
	public AdminGMRequestMultiSolve(ClientSession session,Packet packet) {
		super(session);
		admin = (Admin)session.getClient();
		serial = packet.getInt();
		ids = packet.getInts();
	}

	public void callFinish() throws Exception {
		if(success){
			if(solveIds != null && solveIds.size() > 0){
				Packet pt = new Packet(OpCode.ADMIN_MULTIGMREQUEST_SOLVE_SERVER);
				pt.putInt(0);
				int[] id = new int[solveIds.size()];
				for(int i = 0; i<solveIds.size(); i++){
					id[i] = solveIds.get(i);
				}
				pt.putInts(id);
				Channel channel = Server.server.getServiceRegistry().getChannelService().getChannel("gm");
				channel.broadcast(pt, null);
			}
		}
	}

	public void run() {
		if(admin != null){
			DBService dbService = Server.server.getServiceRegistry().getDbService();
			solveIds = new ArrayList<Integer>(ids.length);
			for(int i=0;i<ids.length;i++){
				GMRequest request = dbService.gmQuestDAO.getGMRequestById(ids[i]);
				if(request != null){
					request.setState(GMRequest.STATE_RESOLVED);
					request.setSolvent("multiSolve---" + admin.name);
					Server.server.getServiceRegistry().getDbService().gmQuestDAO.updateEntity(request);
					solveIds.add(ids[i]);
				}else{
					ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_MULTIGMREQUEST_SOLVE_SERVER, "id为" + ids[i] + "的'GM请求'不存在");
				}
			}
		}
		addToClientSession();
	}

}
