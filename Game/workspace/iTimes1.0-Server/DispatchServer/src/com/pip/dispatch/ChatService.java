package com.pip.dispatch;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.common.IoSession;


/**
 * @author wpjiang
 *	根据玩家的客户端保留session并下发玩家的协议
 */
public class ChatService {
	  //sessionId,client下的dataversion
    protected Map<IoSession, Integer> sessionId2Clients = new ConcurrentHashMap<IoSession, Integer>();

	public Map<IoSession, Integer> getSessionId2Clients() {
		return sessionId2Clients;
	}
	
	/**
	 * @param session
	 * @param dataVesion
	 * 加入玩家的session，并保存里面的version。 在客户端下发的时候用
	 */
	public void addChatPlayerDataVersion(IoSession session, int dataVersion){
		sessionId2Clients.put(session, dataVersion);
	}
	
	/**
	 * @param session
	 * 去除玩家的dataVersion
	 */
	public void removePlayerDataVersion(IoSession session){
		sessionId2Clients.remove(session);
	}
	
	/**
	 * @param session
	 * @return获取玩家的版本号
	 */
	public int getPlayerDataVersion(IoSession session){
		return sessionId2Clients.get(session);
	}

}
