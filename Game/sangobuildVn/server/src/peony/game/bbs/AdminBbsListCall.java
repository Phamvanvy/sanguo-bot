package peony.game.bbs;

import java.util.List;

import peony.common.ClientSessionAsyncCall;
import peony.game.CommonUtil;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class AdminBbsListCall extends ClientSessionAsyncCall {

	protected int serial;
	
	public AdminBbsListCall(ClientSession session, Packet packet){
		super(session);
		this.serial = packet.getInt();
	}
	
	public void callFinish() throws Exception {

	}
	
	/**
	 * bbs列表
	 * serial								int
	 * 数量									short
	 * 循环n此
	 * 	id									int
	 * 	order								int
	 * 	title								string
	 * 	content								string
	 * 	time								string
	 */
	public void run() {
		List<Bbs> l = Server.server.getServiceRegistry().getDbService().bbsDAO.getBbs();
		Packet pt = new Packet(OpCode.ADMIN_BBS_LIST_SERVER);
		pt.putInt(serial);
		pt.putShort(l.size());
		for(Bbs b:l){
			pt.putInt(b.id);
			pt.putInt(b.order);
			pt.putString(b.title);
			pt.putString(b.message);
			pt.putString(CommonUtil.getDateString(b.createTime));
		}
		session.send(pt);
	}

}
