package com.pip.net;

import java.net.ConnectException;
import java.net.SocketAddress;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;

import com.pip.net.message.ServerLoginMessage;
import com.pip.net.message.ServerMessageType;

public abstract class Connector implements ISession {
	

	protected boolean connected = false;
	protected boolean valid = false;
	protected SocketAddress address;
	protected boolean needSecureAuth;
	protected String userName = "";
	protected String password = "";
	protected boolean needRetry = true;
	protected SocketConnector connector;
	protected SocketConnectorConfig config;
	protected int receiveBufferSize = 32767;
	protected int sendBufferSize = 32767;
	protected IoSession session;

	protected IMessageHandler loginHandler;
	protected IMessageHandler customHandler;
	protected IMessageHandler currentHandler;
	protected IoHandler minaHandler = new OriginalSessionHandler();

	protected static final Logger log = Logger.getLogger(Connector.class);

	protected Object lock = new Object();
	
	protected String id;

	public Connector(String id,SocketAddress address, boolean needSecureAuth) {
		this.id = id;
		this.address = address;
		this.needSecureAuth = needSecureAuth;
		
		initConnector();
	}
	
	public String getId(){
		return id;
	}
	
	private void initConnector(){
		connector = new SocketConnector(1, Executors.newCachedThreadPool());
		config = new SocketConnectorConfig();
		config.setThreadModel(ThreadModel.MANUAL);
		config.getSessionConfig().setTcpNoDelay(true);
		config.getSessionConfig().setSendBufferSize(sendBufferSize);
		config.getSessionConfig().setReceiveBufferSize(receiveBufferSize);
		loginHandler = new LoginMessageHandler();
		init();
	}
	
	public abstract void init();

	public void connect() throws ConnectException {
		if (connected || valid) {
			throw new IllegalStateException("connection is connected");
		}
		if (needSecureAuth) {
			currentHandler = loginHandler;
		} else {
			currentHandler = customHandler;
		}
		connector.connect(address, minaHandler, config);
		synchronized (lock) {
			try {
				lock.wait(10000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (!valid) {
				if (session != null) {
					session.close();
				}
				connected = false;
				throw new ConnectException("connect [" + address
								+ "] error");
			}
		}

	}

	public void setMessageHandler(IMessageHandler handler) {
		customHandler = handler;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setNeedRetry(boolean needRetry) {
		this.needRetry = needRetry;
	}

	public boolean isNeedRetry() {
		return needRetry;
	}

	public boolean isConnected() {
		return connected;
	}

	public boolean isValid() {
		return valid;
	}

	public void send(IMessage message) {
		if (valid) {
			send0(message);
		}
	}
	
	public void close(){
		if(session!=null&&session.isConnected()){
			session.close();
		}
	}
	
	public SocketAddress getRemoteAddress(){
		return session.getRemoteAddress();
	}
	
	public boolean equals(ISession s){
		if(this==s)
			return true;
		return id.equals(s.getId());
	}

	protected void sendSecureAuthMessage() {
		ServerLoginMessage msg = new ServerLoginMessage(userName, password);
		send0(msg);
	}

	protected void send0(IMessage msg) {
		session.write(msg);
	}

	class OriginalSessionHandler extends IoHandlerAdapter {

		@Override
		public void exceptionCaught(IoSession session, Throwable cause)
				throws Exception {
			log.info(cause, cause);
		}

		@Override
		public void messageReceived(IoSession session, Object message)
				throws Exception {
			if (currentHandler != null) {
				IMessage msg = (IMessage) message;
				if (msg != null) {
					msg.setSource(Connector.this);
					currentHandler.handle(msg);
				}
			}
		}

		@Override
		public void sessionClosed(IoSession session) throws Exception {
			connected = false;
			if (needRetry && valid) {
				valid = false;
				while (!connected) {
					try {
						Thread.sleep(3000L);
					} catch (Exception ex) {
					}
					try {
						initConnector();
						connect();
						log.info("breaked");
						break;
					} catch (Exception e) {
					}
				}
			} else {
				valid = false;
			}
		}

		@Override
		public void sessionCreated(IoSession session) throws Exception {
			Connector.this.session = session;
			connected = true;
			if (!needSecureAuth) {
				valid = true;
				currentHandler = customHandler;
				synchronized (lock) {
					lock.notify();
				}
			} else {
				sendSecureAuthMessage();
			}
		}

		@Override
		public void sessionIdle(IoSession session, IdleStatus status)
				throws Exception {
			// TODO Auto-generated method stub
			super.sessionIdle(session, status);
		}

	}

	class LoginMessageHandler implements IMessageHandler {

		public void handle(IMessage message) throws Exception {
			short type = message.getCmd();
			if (type == ServerMessageType.LOGINOK) {
				synchronized (lock) {
					currentHandler = customHandler;
					valid = true;
					lock.notify();
					log.info("ServerLoginOk");
				}
			} else if (type == ServerMessageType.LOGINERROR) {
				synchronized (lock) {
					lock.notify();
				}
			}
		}

	}
}
