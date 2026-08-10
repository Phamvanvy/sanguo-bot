package peony.service.version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import peony.common.ClientSessionAsyncCall;
import peony.game.DataService;
import peony.game.ErrorHandler;
import peony.game.FileVersion;
import peony.game.GameFile;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.PlayerPacketHandler;
import peony.game.Server;
import peony.game.Version;
import peony.net.ClientSession;
import peony.net.Packet;

public class CompareVersionCall extends ClientSessionAsyncCall {

	private static final Logger log = Logger.getLogger(CompareVersionCall.class);
	
	protected Packet packet;
	
	public CompareVersionCall(ClientSession session, Packet packet) {
		super(session);
		this.packet = packet;
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		String uiModel = packet.getString();
		String clientVersion = packet.getString();
		String clientModel = packet.getString();
		int clientDataVersion = packet.getInt();
		String[] clientVersionStr = clientVersion.split("\\.");
		String[] clientVersionStr1 = clientVersion.split("-");
		if(clientVersionStr.length>=3 && !clientVersionStr[0].equals("0")){
			StringBuffer sb = new StringBuffer();
			int a = clientVersionStr1.length;
			if(a<=1){
				clientVersion = sb.append(clientVersionStr[0]).append(".").
				append(clientVersionStr[1]).toString();
			}else if(a==2){
				clientVersion = sb.append(clientVersionStr[0]).append(".").
				append(clientVersionStr[1]+"-").append(clientVersionStr1[1]).toString();
			}else if(a==3){
				clientVersion = sb.append(clientVersionStr[0]).append(".").
				append(clientVersionStr[1]+"-").append(clientVersionStr1[1]).
				append("-").append(clientVersionStr1[2]).toString();
			}
		}
		Version version = Server.server.getServiceRegistry()
				.getVersionService().getVersion(clientVersion);
		if (version == null) {
			ErrorHandler.sendErrorMessage(session, -1,
					OpCode.VERSION_COMPARE_CLIENT, peony.Messages.STRING_00308);
			return;
		} else {
			if (version.status == Version.STATUS_OBSOLETE) {
	            if(version.url != null){
	            	Packet pt = new Packet(OpCode.SYMBIAN_UPDATE_URL_SERVER);
	            	pt.putString(version.url);
	            	pt.putString(PlayerPacketHandler.cutSis(version.url));
	            	session.send(pt);
	            }
				ErrorHandler.sendErrorMessage(session, -1,
						OpCode.VERSION_COMPARE_CLIENT, version.message);

				return;
			} else {
				//java版<=2.7的封掉
				if(Server.server.getServiceRegistry()
						.getVersionService().isDeprivedVersion(uiModel, version.id)){
					ErrorHandler.sendErrorMessage(session, -1,
							OpCode.VERSION_COMPARE_CLIENT, "此版本已关闭，请下载最新版客户端，感谢您的支持!");
					return;
				}
			}
		}
		log.info("[COMPVERSIONTRY]IP["+session.getClientIP()+"]SESSIONID["+
				LogUtil.getSessionIdBySession(session)+"]VERSSION["+version.id+"]VERSIONSTAT["+version.status+"]");
//		int len = packet.getShort();
//		String[] files = new String[len];
//		int[] versions = new int[len];
//		for (int i = 0; i < len; i++) {
//			files[i] = packet.getString();
//			versions[i] = packet.getInt();
//		}
		int len = packet.getShort();
		List<String> fileList = new ArrayList<String>();
		List<Integer> versionList = new ArrayList<Integer>();
		while (true) {
			try {
				fileList.add(packet.getString());
				versionList.add(packet.getInt());
			} catch (Exception e) {
				break;
			}
		}
		String[] files = new String[fileList.size()];
		int[] versions = new int[versionList.size()];
		fileList.toArray(files);
		for(int i=0;i<versions.length;i++){
			versions[i] = versionList.get(i);
		}
		
		DataService stageService = Server.server.getServiceRegistry()
				.getDataService();

		byte[] newClientData = null;
		int newClientDataVersion = stageService.getClientDataVersion(uiModel);

		if (clientDataVersion != newClientDataVersion) {
			newClientData = stageService.getNewClientData(uiModel);
		}

		FileVersion[] remove = stageService.versionCompare(files, versions,
				uiModel);
		Packet pt = new Packet(OpCode.VERSION_COMPARE_SERVER);
		int kb = 0;
		int[] perSize = null;
		int index = 0;
		if (newClientData != null) {
			perSize = new int[remove.length + 1];
			pt.putShort(remove.length + 1);
			pt.putString("client.data");
			pt.putInt(newClientDataVersion);
			pt.put(newClientData);
			try {
				kb+=newClientData.length/1024;
				perSize[index] = kb;
				index++;
			} catch (Exception e) {
				
			}
		} else {
			perSize = new int[remove.length];
			pt.putShort(remove.length);
		}
        
		for (int i = 0; i < remove.length; i++) {
			pt.putString(remove[i].name);
			pt.putInt(remove[i].version);
			int k = 0;
			try {
				GameFile file = stageService.getGameFile(remove[i].name, uiModel);
		        byte[] content = file.data;
		        k = content.length/1024;
			} catch (IOException e) {
				
			}
			kb+=k;
			perSize[index] = k;
			index++;
		}
		pt.putInt(kb);
		for(int j=0;j<perSize.length;j++){
			pt.putInt(perSize[j]);
		}
		session.send(pt);
		log.info("[COMPVERSIONOK]IP["+session.getClientIP()+"]SESSIONID["+LogUtil.
				getSessionIdBySession(session)+"]VERSSION["+version.id+"]VERSIONSTAT["+version.status+"]");
	}

}
