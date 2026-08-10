package peony.game;

import peony.net.ClientSession;
import peony.net.Packet;

public class ErrorHandler {
	public static final void sendErrorMessage(ClientSession session,
			int serial, int type, String message) {
		if (session != null) {
			Packet pt = new Packet(OpCode.ERROR);
			pt.putInt(serial);
			pt.putShort(type);
			pt.putUTF(message);
			session.send(pt);
		}
	}
	
	public static final void sendAdminErrorMessage(ClientSession session,int serial,int type,String message){
		if(session != null){
			Packet pt = new Packet(OpCode.ADMIN_ERROR);
			pt.putInt(serial);
			pt.putShort(type);
			pt.putString(message);
			session.send(pt);
		}
	}
	
}
