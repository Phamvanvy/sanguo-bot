package peony.game.stepserver;

import java.util.HashMap;
import java.util.Map;
import org.apache.mina.common.IoSession;
import peony.game.Server;
import peony.net.DispatchClientSession;
import peony.net.PacketHandler;
import peony.service.Service;

/**
 * 远端(本地)服务器需要记录通信IoSession,方便通信返回
 * @author dchen
 */
public class StepSessionService implements Service {

	/** 键:accountId 	值:IoSession */
	protected Map<Long, IoSession> sessions = new HashMap<Long, IoSession>();
	protected Map<Long, DispatchClientSession> dispatchSessions = new HashMap<Long, DispatchClientSession>();
	
	protected PacketHandler handler;
	
	public void startup() throws Exception {
		handler = Server.server.getServiceRegistry().getPacketHandlerService().getPlayerHandler();
	}
	
	public void addSession(int accountId, int playerId, IoSession session){
		if(sessions.containsKey(StepServer.getStepBattleSessionId(accountId, playerId)))
			return;
		sessions.put(StepServer.getStepBattleSessionId(accountId, playerId), session);
	}
	
	public IoSession getSession(int accountId, int playerId){
		return sessions.get(StepServer.getStepBattleSessionId(accountId, playerId));
	}
	
	public void removeSession(int accountId, int playerId){
		sessions.remove(StepServer.getStepBattleSessionId(accountId, playerId));
	}
	
	public void addDispatchSession(int accountId, int playerId, DispatchClientSession session){
		dispatchSessions.put(StepServer.getStepBattleSessionId(accountId, playerId), session);
	}
	
	public DispatchClientSession getDispatchClientSession(int accountId, int playerId){
		return dispatchSessions.get(StepServer.getStepBattleSessionId(accountId, playerId));
	}
	
	public void removeDiapatchClientSession(int accountId, int playerId){
		dispatchSessions.remove(StepServer.getStepBattleSessionId(accountId, playerId));
	}
	
	public void removeAllCachSession(int accountId, int playerId){
		removeDiapatchClientSession(accountId, playerId);
		removeSession(accountId, playerId);
	}
	
	public void clearAllCache(){
		sessions.clear();
		dispatchSessions.clear();
	}

	public void shutdown() {
		
	}

}
