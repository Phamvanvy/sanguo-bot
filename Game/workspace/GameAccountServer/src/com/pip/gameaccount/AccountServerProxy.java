package com.pip.gameaccount;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;

import com.pip.net.message.ServerLoginMessage;
import com.pip.net.uwap2.mina.UWAPDecoder;
import com.pip.net.uwap2.mina.UWAPEncoder;

public class AccountServerProxy {
	
	private SocketConnector connector;
	private Configuration configuration;
	
	private int receiveBufferSize = 32767;
	private int sendBufferSize = 32767;
	
	
	
	private static final Logger log = Logger.getLogger(AccountServerProxy.class);
	
	public AccountServerProxy(Configuration configuration){
		this.configuration = configuration;
	}
	
	public void start(){
		connector = new SocketConnector(1,Executors.newCachedThreadPool());
		SocketConnectorConfig cfg = new SocketConnectorConfig();
		cfg.setThreadModel(ThreadModel.MANUAL);
		cfg.getSessionConfig().setTcpNoDelay(true);
		if (configuration.containsKey("receivebuffersize")) {
			receiveBufferSize = configuration.getInt("receivebuffersize");
		}
		if (configuration.containsKey("sendbuffersize")) {
			sendBufferSize = configuration.getInt("sendbuffersize");
		}
		cfg.getSessionConfig().setReceiveBufferSize(receiveBufferSize);
		cfg.getSessionConfig().setSendBufferSize(sendBufferSize);
		cfg.getFilterChain().addLast("codec",
				new ProtocolCodecFilter(new UWAPEncoder(), new UWAPDecoder()));
		ConnectFuture future = connector.connect(new InetSocketAddress(configuration
				.getString("accountip"), configuration.getInt("accountport")),
				new AccountServiceSessionHandler(), cfg);
		future.join();
//		if(!future.isConnected()){
//			throw new IllegalStateException("account server can't connect");
//		}
	}
	
	
	class AccountServiceSessionHandler extends IoHandlerAdapter{
		@Override
		public void exceptionCaught(IoSession session, Throwable cause)
				throws Exception {
			log.info(cause,cause);
		}

		@Override
		public void messageReceived(IoSession session, Object message)
				throws Exception {
		}

		@Override
		public void sessionClosed(IoSession session) throws Exception {
			
		}

		@Override
		public void sessionCreated(IoSession session) throws Exception {
			String serverId = configuration.getString("serverid");
			String password = configuration.getString("password");
			if(serverId!=null&&password!=null){
				ServerLoginMessage msg = new ServerLoginMessage(serverId,password);
				session.write(msg);
			}else{
				log.info("configuration error:serverid or password is null");
			}
		}
	}
}
