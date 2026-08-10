package peony.net;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;

import peony.game.CommonUtil;
import peony.game.OpCode;
import peony.game.Server;

/**
 * 通过代理服务器连接的用户会话。
 * 
 * @author lighthu
 */
public class DispatchClientSession extends AbstractClientSession {
	
	private static final Logger log = Logger.getLogger(DispatchClientSession.class);
	/*
	 * 会话ID，高32位是代理服务器连接ID，低32位是代理服务器会话ID
	 */
	protected long id;
	/*
	 * 关闭时是否需要通知PROXY
	 */
	protected boolean notify;
	/*
	 * IP地址
	 */
	protected String ip = "";

	public DispatchClientSession(long id, DispatchClientSessionService service,
			IoSession session, PacketHandler handler) {
		super(service, session, handler);
		this.id = id;
		this.notify = true;
		service.addClientSession(this);
	}

	@Override
	public void send(Packet packet) {
		if (state == State.CONNECTED || state == State.AUTHENTICATED) {
			DispatchPacket dp = new DispatchPacket((int) id, packet);
			session.write(dp);
//			log.info("send:" + packet.opCode);
		}
	}

	@Override
	public void close() {
		super.close();
		setDisconnected();
		cleanMessageQueue();
		if (notify) {
			// 向proxy发送PROXY_SESSION_DISCONNECT包通知关闭连接
			Packet packet = new Packet(OpCode.PROXY_SESSION_DISCONNECT);
			session.write(new DispatchPacket((int)id, packet));
		}
	}
	
	public void silentClose() {
		notify = false;
	}
	
	public boolean isConnected() {
		return state==State.CONNECTED||state==State.AUTHENTICATED;
	}
	
	/**
	 * 取客户端IP地址
	 */
	public String getClientIP() {
		return ip;
	}
	
	/**
	 * 设置IP地址
	 * @param ip
	 */
	public void setIP(int ipnum) {
		ip = CommonUtil.ip2str(ipnum);
	}

    /**
     * 检查是否允许登录（如果在线数过多则不允许登录）
     */
    public boolean checkOnlineCount(int currentLoginedAccounts) {
        SyncInteger ati = (SyncInteger)session.getAttribute(DispatchClientSessionService.SESSION_COUNTER);
        int count = ati.get();
        ProxyManagingService pms = (ProxyManagingService)
            Server.server.getServiceRegistry().getService(ProxyManagingService.class);
        if (pms != null) {
            return count <= pms.getMaxPlayer(session);
        }
        return true;
    }
}
