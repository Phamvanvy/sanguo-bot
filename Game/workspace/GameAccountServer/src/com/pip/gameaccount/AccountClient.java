package com.pip.gameaccount;

import java.net.InetSocketAddress;

import org.apache.commons.configuration.Configuration;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;

import com.pip.net.uwap2.mina.UWAPDecoder;
import com.pip.net.uwap2.mina.UWAPEncoder;


public class AccountClient {
	
	private Configuration configuration;
	
	private SocketConnector connector;
	
	private int receiveBufferSize = 32767;
	private int sendBufferSize = 32767;
	
	public AccountClient(Configuration configuration){
		this.configuration = configuration;
		
	}
	
	public void start() {
		connector = new SocketConnector();
		SocketConnectorConfig cfg = new SocketConnectorConfig();
		cfg.setConnectTimeout(30);
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
		InetSocketAddress address = new InetSocketAddress(configuration
				.getString("accountip"), configuration.getInt("accountport"));
		ConnectFuture cf = connector
				.connect(address, new AccountHandler(), cfg);
		cf.join();
	}
	
	class AccountHandler extends IoHandlerAdapter{
		
	}
}
