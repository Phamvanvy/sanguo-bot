package peony.game;

import java.io.IOException;
import org.apache.log4j.Logger;
import peony.common.ClientSessionAsyncCall;
import peony.net.ClientSession;
import peony.net.Packet;

public class GetFileNewCall extends ClientSessionAsyncCall {

	private static final Logger log = Logger.getLogger(GetFileNewCall.class);
	
	protected String model;
	protected String name;
	
	public GetFileNewCall(ClientSession session, String model, String name) {
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
				int DOWN_MAX = 10*1024;	//每个数据包的最大值 20KB
				int len = file.data.length / DOWN_MAX;
				if(file.data.length % DOWN_MAX != 0){
					len++;
				}
				if(file.data.length == 0){
					Packet pt = new Packet(OpCode.NEW_GETFILE_SERVER);
					pt.putString(name);
					pt.putInt(file.version);
					pt.putInt(0);
					pt.putInt(0);
					pt.put(file.data);
					session.send(pt);
				}else{
					int startIndex = 0;
					for(int i=0; i<len; i++){
						Packet pt = new Packet(OpCode.NEW_GETFILE_SERVER);
						pt.putString(name);
						pt.putInt(file.version);
						byte[] update;
						int downLen = file.data.length - startIndex;
						if(downLen > DOWN_MAX){
							downLen = DOWN_MAX;
						}
						update = new byte[downLen];
						System.arraycopy(file.data, startIndex, update, 0, downLen);
						pt.putInt(file.data.length);
						pt.putInt(startIndex);
						pt.put(update);
						session.send(pt);
						startIndex += downLen;
					}
				}
			}else{
				ErrorHandler.sendErrorMessage(session, -1, OpCode.NEW_GETFILE_CLIENT, "暂无文件");
			}
		} catch (IOException e) {
			log.error(e, e);
		}
	}

}
