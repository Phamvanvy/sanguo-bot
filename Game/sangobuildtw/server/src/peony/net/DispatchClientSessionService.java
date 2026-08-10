package peony.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

import peony.game.CommonUtil;
import peony.game.OpCode;
import peony.game.Server;

/**
 * 代理服务器连接服务。
 * 
 * @author lighthu
 */
public class DispatchClientSessionService extends AbstractClientSessionService {
	private static final Logger log = Logger.getLogger(DispatchClientSessionService.class);
	/*
	 * 所有用户会话，id的高32位是代理ID，低32位是会话ID
	 */
	protected Map<Long, DispatchClientSession> sessions = new ConcurrentHashMap<Long, DispatchClientSession>();
	/*
	 * 代理ID生成器
	 */
	protected SyncInteger ids = new SyncInteger(0);
	/*
	 * 本地IP
	 */
	protected String localIP;
	/*
	 * 本地监听端口
	 */
	protected int localPort;

	protected static final String SESSION_ID = "SESSION_ID";
	public static final String SESSION_COUNTER = "SessionCounter";

	public DispatchClientSessionService(Configuration config, PacketHandler handler) {
		super(config, handler);
	}

	public void startup() throws Exception {
		bind();
	}

	public void bind() throws IOException {
		acceptor = new SocketAcceptor();
		SocketAcceptorConfig cfg = new SocketAcceptorConfig();
		cfg.getFilterChain().addLast(
				"codec",
				new ProtocolCodecFilter(DispatchUAEncoder.class,
						DispatchUADecoder.class));
		localIP = config.getString(ADDRESS);
		localPort = config.getInt(PORT);
		acceptor.bind(new InetSocketAddress(localIP, localPort), 
				new DispatchClientSessionHandler(), cfg);
	}

	@Override
	public void shutdown() {
		sessions.clear();
		super.shutdown();
	}
	
	public String getLocalIP() {
		return localIP;
	}
	
	public int getLocalPort() {
		return localPort;
	}

	public void addClientSession(ClientSession session) {
		
		sessions.put(((DispatchClientSession) session).id,
				(DispatchClientSession) session);
		notifySessionAdded(session);

		// 修改连接计数器
		try {
			AbstractClientSession cs = (AbstractClientSession)session;
			SyncInteger ati = (SyncInteger)cs.getIoSession().getAttribute(SESSION_COUNTER);
			ati.incrementAndGet();
		} catch (Exception e) {
		}
	}

	public void removeClientSession(ClientSession session) {
		ClientSession s = sessions.remove(((DispatchClientSession) session).id);
		if (s != null) {
			notifySessionRemoved(session);

			// 修改连接计数器
			try {

				AbstractClientSession cs = (AbstractClientSession) session;
				SyncInteger ati = (SyncInteger) cs.getIoSession().getAttribute(SESSION_COUNTER);
				ati.decrementAndGet();
			} catch (Exception e) {
			}
		}
	}

	protected DispatchClientSession getSession(long id) {
		return sessions.get(id);
	}

	class DispatchClientSessionHandler extends IoHandlerAdapter {
		@Override
		public void exceptionCaught(IoSession session, Throwable t)
				throws Exception {
			log.debug(t, t);
		}

		@Override
		public void messageReceived(IoSession session, Object msg)
				throws Exception {
//			log.debug("receive msg");
			if (msg instanceof DispatchPacket) {
				DispatchPacket dp = (DispatchPacket) msg;
				if (dp.id == -1) {
					// 如果ID是-1，则只可能是一种包：PROXY_LOGIN
					if (dp.packet.opCode == OpCode.PROXY_LOGIN) {
						int ip = dp.packet.getInt();
						int port = dp.packet.getShort();
						log.info("proxy login: " + CommonUtil.ip2str(ip) + ":" + port);

						// 通知Proxy服务
						ProxyManagingService pms = (ProxyManagingService)
							Server.server.getServiceRegistry().getService(ProxyManagingService.class);
						if (pms != null) {
							pms.registerConnection(session, CommonUtil.ip2str(ip), port);
						}
					}
				} else {
					int sessionId = (Integer) session.getAttribute(SESSION_ID);
					long fullId = (((long) sessionId) << 32) | dp.id;
					DispatchClientSession ds = getSession(fullId);
					if (ds == null) {
						ds = new DispatchClientSession(fullId, 
								DispatchClientSessionService.this, session, handler);
//						addClientSession(ds); 
					}
					switch (dp.packet.opCode) {
					case OpCode.PROXY_SYNC_IP: // 同步IP
						ds.setIP(dp.packet.getInt());
						break;
					case OpCode.PROXY_SESSION_DISCONNECT: // 断开连接
//						log.info("session disconnect");
						ds.silentClose();
						ds.setDisconnected();
						break;
					default:
//						System.out.println("recv: " + dp.packet.opCode);
						ds.addPacket(dp.packet);
						break;
					}
				}
			}
		}

		@Override
		public void sessionClosed(IoSession session) throws Exception {
			// proxy连接断开，关闭所有通过此proxy连接的会话
			for (DispatchClientSession ds : sessions.values()) {
				if (ds.session == this) {
					ds.silentClose();
					ds.setDisconnected();;
				}
			}
			
			// 通知Proxy服务
			ProxyManagingService pms = (ProxyManagingService)
				Server.server.getServiceRegistry().getService(ProxyManagingService.class);
			if (pms != null) {
				pms.unregisterConnection(session);
			}
		}

		@Override
		public void sessionCreated(IoSession session) throws Exception {
			// 检查IP地址
			TrustIpService ts = Server.server.getServiceRegistry().getTrustIpService();
			if (!ts.isTrustIp((InetSocketAddress)session.getRemoteAddress())) {
				session.close();
				return;
			}
			
			session.setAttribute(SESSION_ID, new Integer(ids.incrementAndGet()));
			session.setAttribute(SESSION_COUNTER, new SyncInteger(0));
		}
	}
}
