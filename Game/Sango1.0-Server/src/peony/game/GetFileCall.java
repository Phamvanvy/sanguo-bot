package peony.game;

import java.io.IOException;
import org.apache.log4j.Logger;
import peony.common.ClientSessionAsyncCall;
import peony.net.ClientSession;
import peony.net.Packet;

public class GetFileCall extends ClientSessionAsyncCall {

	private static final Logger log = Logger.getLogger(GetFileCall.class);
	
	protected String model;
	protected String name;
	
	public GetFileCall(ClientSession session, String model, String name) {
		super(session);
		this.model = model;
		this.name = name;
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		try {
			DataService stageService = Server.server.getServiceRegistry().getDataService();
			GameFile file = stageService.getGameFile(name, model);
			if (file != null) {
				Packet pt = new Packet(OpCode.GETFILE_SERVER);
				pt.putString(name);
				pt.putInt(file.version);
				pt.put(file.data);
				session.send(pt);
			}
		} catch (IOException e) {
			log.error(e, e);
		}
	}

}
