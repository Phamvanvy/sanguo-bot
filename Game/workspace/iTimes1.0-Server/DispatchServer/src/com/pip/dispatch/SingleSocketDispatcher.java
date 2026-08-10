package com.pip.dispatch;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.apache.mina.common.*;
import org.apache.mina.filter.codec.*;
import org.apache.mina.transport.socket.nio.*;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

/**
 * 用于和proxy接口的转发服务器。原来的版本只能支持一个proxy连接，新修订的版本可以支持多个proxy连接。
 * 每个用户在世界服务器上必须有一个唯一的sessionid。原来的版本直接用proxy生成的sessionid作为用户的
 * sessionid，新的版本中，把sessionid改为由2部分组成，高8位是proxy连接的ID，低24位是proxy传过来 的sessionid。0.
 * 
 * @author lighthu
 */
public class SingleSocketDispatcher implements Dispatcher, Runnable {
	private static final Logger log = Logger
			.getLogger(SingleSocketDispatcher.class);

	// proxy连接ID生成器，从0开始递增，按256取模
	private AtomicInteger proxyIDs = new AtomicInteger(0);
	// 已经建立的连接，每个proxy连接必须有唯一的ID。当一个proxy的IOSession建立时，我们会为它分配一个ID，然后
	// 把这个ID设置为IoSession的一个属性，属性名为"ProxyID"。
	private ConcurrentHashMap<Integer, IoSession> proxySessions = new ConcurrentHashMap<Integer, IoSession>();
	// 当前所有的客户端连接对象，KEY是用户的sessionid（带连接ID的)，VALUE是用户的session对象
	private ConcurrentHashMap<Integer, SingleConnectSession> sessions = new ConcurrentHashMap<Integer, SingleConnectSession>();

	// 管理接口
	private ControlProcessor processor = null;
	// 客户端90通知服务
	private ChannelService channelService = null;
	// 信任IP服务
	private TrustIpService trustIpService = null;
	// 代理管理服务
	private ProxyManagingService proxyService = null;
	// 服务器配置
	private Configuration configuration = null;

	// MINA
	private IoAcceptor acceptor = null;
	private IoConnector connector = null;

	// 世界服务器连接
	private IoSession serverSession = null;
	
	private ChatService chatService = null;
	public void setChatService(ChatService chatService) {
		this.chatService = chatService;
	}
	    
	    
	// 网络和配置相关的常量
	private static final byte SESSION_CLOSED = (byte)186;
	private static final byte SERVER_LOGIN = (byte)180;
	public static final String SERVERID = "serverid";
	public static final String SERVERNAME = "servername";
	public static final String SERVERPASSWORD = "serverpassword";
	public static final String PROXYID = "ProxyID";
	public static final String SESSION_COUNTER = "SessionCounter";

	// 向proxy发送空包表示请求断开连接
	private static final byte[] EMTPY_PACKET = { 'U', 'W', 'A', 'P', '1', 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0 };

	/**
	 * 创建一个proxy连接模式的转发器。
	 * 
	 * @param processor
	 *            管理控制器
	 * @param configuration
	 *            服务器配置
	 */
	public SingleSocketDispatcher(ControlProcessor processor,
			Configuration configuration, ProxyManagingService proxyService) {
		this.processor = processor;
		this.configuration = configuration;
		this.proxyService = proxyService;

		// 启动一个线程定时在日志输入当前在线数
		new Thread(this, "OnlinePrinter").start();

		// 启动普通模式90包发送线程
		new Thread(new Normal90Sender(), "Normal90Sender").start();

		// 启动特殊模式(NGage) 90包发送线程
		new Thread(new Fast90Sender(), "Fast90Sender").start();
	}

	/**
	 * 配置客户端90通知服务
	 * 
	 * @param channelService
	 */
	public void setChannelService(ChannelService channelService) {
		this.channelService = channelService;
	}

	/**
	 * 配置信任IP服务
	 * 
	 * @param trustIpService
	 */
	public void setTrustIpService(TrustIpService trustIpService) {
		this.trustIpService = trustIpService;
	}

	/**
	 * 向服务器转发一个包。
	 * 
	 * @param session
	 * @param object
	 */
	public void dispatchToServer(IoSession session, Object object) {
		Packet1 packet = (Packet1) object;

		// 计算这个包的实际sessionid
		int clientSessionID = packet.sessionId; // proxy传过来的sessionid
		int proxySessionID = ((Integer) session.getAttribute(PROXYID))
				.intValue(); // proxy本身ID
		int sessionID = (proxySessionID << 24) | clientSessionID; // 按8+24的方式拼起来

		if (packet.type == Packet1.TYPE.CONTROL) {
			// CONTROL类型的包表示客户端断开连接通知
			unRegisterClient(sessionID);
		} else {
			// 0xFFFD类型的包用来发送代理服务器服务地址
			byte ptype = packet.buffer.get(19);
			if (ptype == (byte)0xFD) {
				int ip = packet.buffer.getInt(26);
				int port = packet.buffer.getShort(31) & 0xFFFF;
				String ipStr = ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) +
					"." + (ip & 0xFF);
				proxyService.registerConnection(session, ipStr, port);
				return;
			}
			
			// 其他类型的包，修改包里的sessionid后转发给世界服务器
			if (!sessions.containsKey(sessionID)) {
				SingleConnectSession s = new SingleConnectSession(session,
						packet.sessionId);
				registerClient(sessionID, s);
			}
			packet.buffer.putInt(5, sessionID);
			serverSession.write(packet.buffer.duplicate());
		}
	}

	/**
	 * 取得指定会话ID对应的客户端连接（这里实际返回的是一个虚拟连接对象）。
	 */
	public IoSession getSession(int sessionId) {
		return sessions.get(sessionId);
	}

	/**
	 * 向服务器发送一个控制包。
	 * 
	 * @param seg
	 */
	public void sendControlSegment(UWAPSegment seg) {
		seg.setSessionId(-1);
		serverSession.write(ByteBuffer.wrap(seg.getPacketByteArray()));
	}

	/**
	 * 连接世界服务器。
	 * 
	 * @param address
	 * @param config
	 * @return
	 */
	public ConnectFuture connect(SocketAddress address,
			SocketConnectorConfig config) {
		connector = new SocketConnector(1, Executors.newCachedThreadPool());
		config.setThreadModel(ThreadModel.MANUAL);
		config.getSessionConfig().setTcpNoDelay(true);
		config.getFilterChain().addLast(
				"codec",
				new ProtocolCodecFilter(new ServerUWAPEncoder(),
						new ServerUWAPDecoder()));
		return connector.connect(address, new ServerSessionHandler(), config);
	}

	/**
	 * 绑定分配器监听端口。
	 * 
	 * @param address
	 * @param config
	 * @throws IOException
	 */
	public void bind(SocketAddress address, SocketAcceptorConfig config)
			throws IOException {
		acceptor = new SocketAcceptor(1, Executors.newCachedThreadPool());
		config.setThreadModel(ThreadModel.MANUAL);
		config.getSessionConfig().setTcpNoDelay(true);
		config.getFilterChain().addLast(
				"codec",
				new ProtocolCodecFilter(new SimpleUWAPEncoder(),
						new SimpleUWAPDecoder2()));
		acceptor.bind(address, new ClientSessionHandler(), config);
	}

	/**
	 * 把一个世界服务器下发的包转发给客户端。
	 * 
	 * @param packet
	 */
	public void dispatchToClient(Packet packet) {
		IoSession s = sessions.get(packet.sessionId);
		if (s != null) {
			s.write(packet.buffer);
		}
	}

	/**
	 * 注册一个客户端新连接（实际是一个虚拟连接）。
	 * 
	 * @param sessionID
	 *            连接sessionid(proxyid + clientsessionid)
	 * @param session
	 *            虚拟连接对象
	 */
	public void registerClient(int sessionID, SingleConnectSession session) {
		sessions.put(sessionID, session);
		channelService.getFast90Channel().join(session);
		
		// 修改连接计数器
		AtomicInteger ati = (AtomicInteger)session.getUnderlyingSession().getAttribute(SESSION_COUNTER);
		ati.incrementAndGet();
	}

	/**
	 * 注销一个客户端连接。
	 * 
	 * @param session
	 */
	protected void unRegisterClient(SingleConnectSession session) {
		channelService.removeSessionFromAllChannel(session);
		chatService.removePlayerDataVersion(session);
		// 通知世界服务器连接关闭了
		UWAPSegment seg = new UWAPSegment(SESSION_CLOSED);
		seg.writeInt(session.getFullSessionID());
		sendControlSegment(seg);
		
		// 修改连接计数器
		AtomicInteger ati = (AtomicInteger)session.getUnderlyingSession().getAttribute(SESSION_COUNTER);
		ati.decrementAndGet();
	}

	/*
	 * 创建一个空的包（用来通知proxy关闭客户端连接）。
	 */
	private ByteBuffer getEmtpySegment(int sessionId) {
		ByteBuffer buf = ByteBuffer.wrap(EMTPY_PACKET);
		buf.putInt(5, sessionId);
		return buf;
	}

	/**
	 * 注销一个客户端连接。
	 * 
	 * @param sessionId
	 *            连接的完整sessionid
	 */
	public void unRegisterClient(int sessionId) {
		SingleConnectSession session = (SingleConnectSession) sessions
				.remove(sessionId);
		if (session != null) {
			unRegisterClient(session);
			session.write(getEmtpySegment(session.getSessionId()));
		}
	}

	/**
	 * 处理控制指令。
	 * 
	 * @param packet
	 */
	protected void processControl(Packet packet) {
		processor.process(packet.data);
	}

	/**
	 * 向所有连接广播包。
	 */
	public void broadcast(ByteBuffer buffer) {
		for (IoSession session : sessions.values()) {
			session.write(buffer.duplicate());
		}
		buffer.release();
	}

	/**
	 * 关闭监听连接。
	 */
	public void shutdown() {
		acceptor.unbindAll();
	}

	/**
	 * 定时打印在线用户数的线程。
	 */
	public void run() {
		while (true) {
			try {
				Thread.sleep(60 * 1000L);
			} catch (InterruptedException ex) {
			}
			log.info("ONLINE[" + sessions.size() + "]");
		}
	}

	/**
	 * 客户端（proxy服务器）连接处理器。
	 * @author lighthu
	 */
	class ClientSessionHandler extends IoHandlerAdapter {
		/**
		 * 连接异常。
		 */
		public void exceptionCaught(IoSession sesion, Throwable throwable)
				throws Exception {
			log.error(throwable, throwable);
		}

		/**
		 * 收到消息处理。调用dispatchToServer把包转发给世界服务器。
		 */
		public void messageReceived(IoSession session, Object object)
				throws Exception {
			dispatchToServer(session, object);
		}

		/**
		 * 连接关闭处理。通知世界服务器所有此连接上的用户掉线。
		 */
		public void sessionClosed(IoSession session) throws Exception {
			log.info("C Dispatcher Closed");
			proxyService.unregisterConnection(session);
			int proxyID = ((Integer)session.getAttribute(PROXYID)).intValue();
			proxySessions.remove(proxyID);
			synchronized (sessions) {
				Object[] sessionKeys = sessions.keySet().toArray();
				for (Object key : sessionKeys) {
					SingleConnectSession s = sessions.get(key);
					if (s.getUnderlyingSession() == session) {
						unRegisterClient(s);
						sessions.remove(key);
					}
				}
			}
		}

		/**
		 * 连接创建。为新的连接分配ID。
		 */
		public void sessionCreated(IoSession session) throws Exception {
			synchronized (sessions) {
				InetSocketAddress address = (InetSocketAddress) session
						.getRemoteAddress();
				if (!trustIpService.isTrustIp(address)) {
					session.close();
				} else {
					log.info("C Dispatcher Created");
					int nextID = proxyIDs.getAndIncrement();
					while (proxySessions.containsKey(nextID)) {
						nextID = proxyIDs.getAndIncrement();
					}
					session.setAttribute(PROXYID, new Integer(nextID));
					session.setAttribute(SESSION_COUNTER, new AtomicInteger(0));
					proxySessions.put(nextID, session);
				}
			}
		}

		/**
		 * 连接空闲处理。
		 */
		public void sessionIdle(IoSession session, IdleStatus idleStatus)
				throws Exception {
		}
	}

	/**
	 * 世界服务器连接处理器。
	 * @author lighthu
	 */
	class ServerSessionHandler extends IoHandlerAdapter {
		/**
		 * 连接异常处理。
		 */
		public void exceptionCaught(IoSession sesion, Throwable throwable)
				throws Exception {
			log.error(throwable, throwable);
		}

		/**
		 * 收到消息处理。对于大部分的包，都调用dispatchToClient转发给proxy服务器。
		 */
		public void messageReceived(IoSession session, Object object)
				throws Exception {
			Packet packet = (Packet) object;
			if (packet.type == Packet.TYPE.BUFFER) {
				dispatchToClient(packet);
			} else {
				processControl(packet);
			}
		}

		/**
		 * 连接关闭，目前没有处理这种情况。世界服务器断开后转发服务器也必须重启。
		 */
		public void sessionClosed(IoSession session) throws Exception {
		}

		/**
		 * 连接建立成功。发送LOGIN请求。
		 */
		public void sessionCreated(IoSession session) throws Exception {
			serverSession = session;
			UWAPSegment seg = new UWAPSegment(SERVER_LOGIN);
			seg.writeString((String) configuration.getProperty(SERVERID));
			seg.writeString((String) configuration.getProperty(SERVERPASSWORD));
			seg.writeInt(configuration.getInt("maxplayer"));
			seg.writeString(configuration.getString("servertype"));
            sendControlSegment(seg);
		}

		/**
		 * 连接空闲。
		 */
		public void sessionIdle(IoSession session, IdleStatus idleStatus)
				throws Exception {
		}
	}

	/**
	 * 每3秒下发90包的线程。客户端刚登录时，或者NGage手机用户需要这个90包。
	 * @author lighthu
	 */
	class Fast90Sender implements Runnable {
		public void run() {
			while (true) {
				try {
					try {
						Thread.sleep(3 * 1000L);
					} catch (InterruptedException ex) {
					}
					if (channelService != null) {
						int time = (int) ((System.currentTimeMillis() + 8 * 3600 * 1000) / 1000);
						UWAPSegment seg = new UWAPSegment((byte) 90);
						seg.writeInt(time);
						channelService.getFast90Channel().broadcast(
								ByteBuffer.wrap(seg.getPacketByteArray()));
					}
				} catch (Exception ex1) {
				}
			}
		}
	}

	/**
	 * 普通机型登录后，每30秒下发一个90包维持连接。
	 * @author lighthu
	 */
	class Normal90Sender implements Runnable {
		public void run() {
			while (true) {
				try {
					try {
						Thread.sleep(30 * 1000L);
					} catch (InterruptedException ex) {
					}
					if (channelService != null) {
						int time = (int) ((System.currentTimeMillis() + 8 * 3600 * 1000) / 1000);
						UWAPSegment seg = new UWAPSegment((byte) 90);
						seg.writeInt(time);
						channelService.getNormal90Channel().broadcast(
								ByteBuffer.wrap(seg.getPacketByteArray()));
					}
				} catch (Exception ex1) {
				}
			}
		}
	}
}
