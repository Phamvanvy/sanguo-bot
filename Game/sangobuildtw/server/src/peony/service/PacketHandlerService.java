package peony.service;

import peony.game.AdminPacketHandler;
import peony.game.PlayerPacketHandler;
import peony.net.PacketHandler;

/**
 * 管理包处理器实例。
 * @author lighthu
 */
public class PacketHandlerService implements Service {
	/*
	 * 用户连接的包处理器。
	 */
	protected PacketHandler playerHandler;
	protected PacketHandler adminHandler;

	public void startup() {
		playerHandler = new PlayerPacketHandler();
		adminHandler = new AdminPacketHandler();
	}

	public void shutdown() {}
	
	public PacketHandler getAdminHandler(){
		return adminHandler;
	}
	
	/**
	 * 获得用户连接的包处理器。
	 * @return
	 */
	public PacketHandler getPlayerHandler() {
		return playerHandler;
	}
}
