package com.pip.gameaccount.qq;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

import com.pip.net.AcceptSession;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.uwap2.mina.TripleDESFilter;

public class QQStub {
	private SocketAcceptor acceptor;

	private int receiveBufferSize = 32767;
	private int sendBufferSize = 32767;

	private static final Logger log = Logger.getLogger(QQStub.class);

	private IMessageHandler handler;

	private final String ip;
	private final int port;
	private final String key;

	public QQStub(String ip, int port, String key) {
		this.ip = ip;
		this.port = port;
		this.key = key;
	}

	public void setReceiveBufferSize(int receiveBufferSize) {
		this.receiveBufferSize = receiveBufferSize;
	}

	public void setSendBufferSize(int sendBufferSize) {
		this.sendBufferSize = sendBufferSize;
	}

	public void setMessageHandler(IMessageHandler handler) {
		this.handler = handler;
	}

	public void start() throws Exception {
		acceptor = new SocketAcceptor(1, Executors.newCachedThreadPool());
		SocketAcceptorConfig cfg = new SocketAcceptorConfig();
		cfg.setThreadModel(ThreadModel.MANUAL);
		cfg.setDisconnectOnUnbind(true);
		cfg.getSessionConfig().setTcpNoDelay(true);
		cfg.getSessionConfig().setReuseAddress(true);
		cfg.getSessionConfig().setReceiveBufferSize(receiveBufferSize);
		cfg.getSessionConfig().setSendBufferSize(sendBufferSize);
		cfg.getFilterChain().addLast("triple-des", new TripleDESFilter(key));
		cfg.getFilterChain().addLast(
				"codec",
				new ProtocolCodecFilter(new QQMessageEncoder(),
						new QQMessageDecoder()));
		cfg.getFilterChain().addLast(
				"threadcache",
				new ExecutorFilter(
						new ThreadPoolExecutor(2, 8, 20L,
								TimeUnit.SECONDS,
								new LinkedBlockingQueue<Runnable>())));
		acceptor.bind(new InetSocketAddress(ip, port),
				new ClientSessionHandler(), cfg);
	}

	class ClientSessionHandler extends IoHandlerAdapter {

		@Override
		public void exceptionCaught(IoSession session, Throwable cause)
				throws Exception {
			log.info(cause, cause);
		}

		@Override
		public void messageReceived(IoSession session, Object message)
				throws Exception {
			log.info("receive:"+message.hashCode());
			IMessage msg = (IMessage) message;
			AcceptSession s = (AcceptSession) session
					.getAttribute("acceptsession");
			if (s != null) {
				msg.setSource(s);
				handler.handle(msg);
			}else{
				log.info("session not found:"+message.hashCode());
			}
		}

		@Override
		public void sessionClosed(IoSession session) throws Exception {
			log.info("QQSession closed");
//			System.out.println("QQSession closed");
			AcceptSession s = (AcceptSession) session
					.removeAttribute("acceptsession");
			if (s != null) {
				s.setValid(false);
			}
		}

		@Override
		public void sessionCreated(IoSession session) throws Exception {
			log.info("QQSession created");
//			System.out.println("QQSession created");
			AcceptSession s = new AcceptSession(session);
			s.setValid(true);
			session.setAttribute("acceptsession", s);
		}
	}
}
