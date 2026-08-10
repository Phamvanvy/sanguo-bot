package peony.game.bbs;

import java.util.Date;

import peony.common.ClientSessionAsyncCall;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class AdminBbsAddCall extends ClientSessionAsyncCall {
	
	int serial;
	int order;
	String title,message;

	public AdminBbsAddCall(ClientSession session, Packet packet){
		super(session);
		this.serial = packet.getInt();
		this.order = packet.getInt();
		this.title = packet.getString();
		this.message = packet.getString();
	}
	
	public void callFinish() throws Exception {

	}

	/**
	 * 添加bbs条目
	 * serial								int
	 * order								int
	 * title								string
	 * message								string
	 */
	
	/**
	 * 添加bbs条目成功
	 */
	
	public void run() {
		Bbs b = new Bbs();
		b.order = order;
		b.title = title;
		b.message = message;
		b.createTime = new Date();
		Server.server.getServiceRegistry().getDbService().bbsDAO.makePersistent(b);
		Packet pt = new Packet(OpCode.ADMIN_BBS_ADD_SERVER);
		pt.putInt(serial);
		session.send(pt);
	}

}
