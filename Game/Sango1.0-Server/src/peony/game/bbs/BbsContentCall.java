package peony.game.bbs;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class BbsContentCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int id;
	
	public BbsContentCall(ClientSession session, Packet packet){
		super(session);
		this.serial = packet.getInt();
		this.id = packet.getInt();
	}
	
	public void callFinish() throws Exception {

	}

	public void run() {
		Bbs bbs = Server.server.getServiceRegistry().getDbService().bbsDAO.find(id);
		if(bbs!=null){
			Packet pt = new Packet(OpCode.BBS_CONTENT_SERVER);
			pt.putInt(serial);
			pt.putString(bbs.message);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.BBS_CONTENT_CLIENT, peony.Messages.STRING_00871);
		}
	}

}
