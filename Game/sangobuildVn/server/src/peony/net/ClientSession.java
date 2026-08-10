package peony.net;

import peony.common.AsyncCall;
import peony.game.Client;
import peony.game.Identity;

/*
 * 每个连接对应一个ClientSession，不论是由连接服务器转发的还是直接对外的连接
 */
public interface ClientSession {

	/**
	 * 是否处于连接状态
	 */
	public boolean isConnected();

	/**
	 * 发送数据包
	 * 
	 * @param packet
	 */
	public void send(Packet packet);

	/**
	 * 关闭连接
	 */
	public void close();

	/**
	 * 获得数据包处理对象
	 */
	public PacketHandler getHandler();

	/**
	 * 获取当前的Client，次方法可能返回null，比如在角色还没有登录的情况下
	 */
	public Client getClient();

	/**
	 * 设置当前的Client
	 */
	public void setClient(Client client);
	
	public boolean update(int diff);
	
	public void keepLive();
	
	public void addPacket(Packet packet);
	
	public void addAsyncCall(AsyncCall call);
	
	public void authenticate(Identity identity) throws SecurityException;
	
	public Identity getIdentity();
	
	
	/**
	 * 取客户端IP地址
	 */
	public String getClientIP();
	
	/**
	 * 检查是否允许登录（如果在线数过多则不允许登录）
	 */
	public boolean checkOnlineCount(int currentLoginedAccounts);
}
