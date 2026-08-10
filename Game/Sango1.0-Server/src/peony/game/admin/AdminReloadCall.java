package peony.game.admin;

import com.pip.sanguo.data.ProjectData;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class AdminReloadCall extends ClientSessionAsyncCall {
	protected int serial;
	protected String type;
	protected ProjectData newPrj;
	
	public AdminReloadCall(Packet packet, ClientSession session) {
		super(session);
		this.serial = packet.getInt();
		this.type = packet.getString();
	}

	public void callFinish() throws Exception {
		try {
			Server.server.getServiceRegistry().getDataService().reload(newPrj, type);
			Packet pt = new Packet(OpCode.ADMIN_RELOAD_SERVER);
			pt.putInt(serial);
			session.send(pt);
		}  catch (Exception e) {
        	e.printStackTrace();
        	ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_RELOAD_CLIENT, peony.Messages.STRING_00673);
        	return;
        }
	}

	public void run() {
		boolean needProj = !(type.equals("file") || type.equals("version") || type.equals("config") || type.equals("hints") || type.equals("randomfaction"));
		if (needProj) {
			ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
	        newPrj = new ProjectData();
	        newPrj.serverMode = true;
	        newPrj.createPathFinder = false;
	        newPrj.branch = proj.branch;
	        try {
	        	newPrj.load(proj.baseDir);
	        } catch (Exception e) {
	        	e.printStackTrace();
	        	ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_RELOAD_CLIENT, peony.Messages.STRING_00673);
	        	return;
	        }
		}
		addToClientSession();
	}

}
