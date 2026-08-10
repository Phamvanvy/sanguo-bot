package peony.game.admin;

import java.util.List;

import peony.common.ClientSessionAsyncCall;
import peony.db.DBService;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class AdminGMRequestCall extends ClientSessionAsyncCall {
	
	protected int serial;
	protected int pageNo;
	protected int pageSize;
	protected int count;
	protected List<GMRequest> mails;
	
	public AdminGMRequestCall(ClientSession session,Packet packet){
		super(session);
		this.serial = packet.getInt();
		this.pageNo = packet.getShort();
		this.pageSize = packet.getShort();
	}
//	 * 	请求Id						int
//	 *  请求类型						byte(暂时都为0，以后分各种问题组)
//	 * 	请求玩家Id					int
//	 * 	请求玩家名					string
//	 * 	请求内容						string
//	 * 	请求状态						byte(0 未解决 1 解决)
//	 * 	解决方案						string
//	 * 	提交时间						long
//	 *  玩家机型						string
//	 *  玩家mapId					short
//	 *  玩家x坐标					short
//	 *  玩家y坐标					short
	public void callFinish() throws Exception {
		if (success) {
			int pageCount = count / pageSize;
			if (count % pageSize != 0)
				pageCount++;
			Packet pt = new Packet(OpCode.ADMIN_GMREQUEST_LIST_SERVER);
			pt.putInt(serial);
			pt.putShort(pageSize);
			pt.putShort(pageNo);
			pt.putInt(count);
			pt.putShort(mails.size());
			for(GMRequest m:mails){
				pt.putInt(m.getId());
				pt.put(m.getType());
				pt.putInt(m.getPlayerId());
				pt.putString(m.getPlayerName());
				pt.putString(m.getCause());
				pt.put(m.getState());
				pt.putString(m.getSolvent());
				pt.putLong(m.getCreateTime().getTime());
				pt.putString(m.getModel());
				pt.putShort(m.getMapId());
				pt.putShort(m.getX());
				pt.putShort(m.getY());
			}
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.ADMIN_GMREQUEST_LIST_SERVER, errorMessage);
		}
	}

	public void run() {
		DBService dbService = Server.server.getServiceRegistry().getDbService();
		mails = dbService.gmQuestDAO.getGMRequest(pageSize*pageNo, pageSize);
		count = dbService.gmQuestDAO.getGMRequestCount();
		addToClientSession();
	}

}
