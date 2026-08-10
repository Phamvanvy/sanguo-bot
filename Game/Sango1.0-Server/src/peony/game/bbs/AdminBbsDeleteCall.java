package peony.game.bbs;

import java.util.List;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class AdminBbsDeleteCall extends ClientSessionAsyncCall {

	int serial;
//	int id;
	String title;
	
	public AdminBbsDeleteCall(ClientSession session, Packet packet){
		super(session);
		this.serial = packet.getInt();
//		this.id = packet.getInt();
		this.title = packet.getString();
	}
	
	public void callFinish() throws Exception {

	}

	/**
	 * 删除bbs条目
	 * serial								int
	 * id									int
	 */
	
	/**
	 * 删除bbs条目成功
	 * serial								int
	 */
	
	public void run() {
//		Bbs bbs = (Bbs) Server.server.getServiceRegistry().getDbService().bbsDAO.uniqueResult("from Bbs where title=?", title);
//		if(bbs!=null){
//			Server.server.getServiceRegistry().getDbService().bbsDAO.makeTransient(bbs);
//			Packet pt = new Packet(OpCode.ADMIN_BBS_ADD_SERVER);
//			pt.putInt(serial);
//			session.send(pt);
//		}else{
//			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_BBS_DELETE_CLIENT, peony.Messages.STRING_01125);
//		}
//		
		List<Bbs> bbss = (List<Bbs>) Server.server.getServiceRegistry().getDbService().bbsDAO.list("from Bbs where title=?", title);
		if(bbss!=null){
			Server.server.getServiceRegistry().getDbService().bbsDAO.delete("delete from Bbs where title=?", title);
			Packet pt = new Packet(OpCode.ADMIN_BBS_ADD_SERVER);
			pt.putInt(serial);
			session.send(pt);
		}else{
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_BBS_DELETE_CLIENT, peony.Messages.STRING_01125);
		}
	}

}
