package com.pip.net;

import java.net.SocketAddress;
import java.util.concurrent.Executors;

import org.apache.mina.common.ThreadModel;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

public class Acceptor {
	
	protected SocketAddress address = null;
	protected SocketAcceptor acceptor = null;
	protected SocketAcceptorConfig config = null;
	
	protected int receiveBufferSize = 32767;
	protected int sendBufferSize = 32767;
	
	protected IMessageHandler currentHandler;
	protected IMessageHandler customHandler;
	protected IMessageHandler serverHandler;
	
	public Acceptor(SocketAddress address){
		this.address = address;
		this.config = new SocketAcceptorConfig();
		this.acceptor = new SocketAcceptor(1,Executors.newCachedThreadPool());
	}
	
	public void bind(){
		config.setThreadModel(ThreadModel.MANUAL);
		config.getSessionConfig().setReceiveBufferSize(receiveBufferSize);
		config.getSessionConfig().setSendBufferSize(sendBufferSize);
		config.getSessionConfig().setTcpNoDelay(true);
		config.setReuseAddress(true);
		
	}
	
	public void setReceiveBufferSize(int size){
		this.receiveBufferSize = size;
	}
	
	public void setSendBufferSize(int size){
		this.sendBufferSize = size;
	}
	
	
	

}
