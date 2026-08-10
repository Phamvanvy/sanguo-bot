package com.pip.gameaccount;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

import com.pip.net.AcceptSession;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.message.gameaccount.MessageDecoder;
import com.pip.net.message.gameaccount.MessageEncoder;
import com.pip.net.uwap2.mina.UWAP2MessageFilter;
import com.pip.net.uwap2.mina.UWAPDecoder;
import com.pip.net.uwap2.mina.UWAPEncoder;

public class WorldStub {

	private SocketAcceptor acceptor;
	private Configuration configuration;
	
	private int receiveBufferSize = 32767;
	private int sendBufferSize = 32767;
	
	private static final Logger log = Logger.getLogger(WorldStub.class);

	private IMessageHandler handler;
	private ISessionService sessionService;
	
	public WorldStub(Configuration configuration,ISessionService sessionService){
		this.configuration = configuration;
		this.sessionService = sessionService;
	}
	
	public void setMessageHandler(IMessageHandler handler){
		this.handler = handler;
	}
	
	
	public void start() throws IOException {
		ByteBuffer.setUseDirectBuffers(false);
		ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
		acceptor = new SocketAcceptor(1, Executors.newCachedThreadPool());
		SocketAcceptorConfig cfg = new SocketAcceptorConfig();
		cfg.setThreadModel(ThreadModel.MANUAL);
		cfg.setDisconnectOnUnbind(true);
		cfg.getSessionConfig().setTcpNoDelay(true);
		cfg.getSessionConfig().setReuseAddress(true);
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
		cfg.getFilterChain().addLast("uwap2message", new UWAP2MessageFilter(new MessageDecoder(),new MessageEncoder()));
		acceptor.bind(new InetSocketAddress(configuration.getString("serverip"),
				configuration.getInt("port")), new ClientSessionHandler(),
				cfg);
	}
	
	class ClientSessionHandler extends IoHandlerAdapter{

		@Override
		public void exceptionCaught(IoSession session, Throwable cause)
				throws Exception {
			log.info(cause,cause);
		}

		@Override
		public void messageReceived(IoSession session, Object message)
				throws Exception {
			IMessage msg = (IMessage)message;
			AcceptSession s = (AcceptSession)session.getAttribute("acceptsession");
			if(s!=null){
				msg.setSource(s);
				s.setLastMessageTime(System.currentTimeMillis());
				handler.handle(msg);
			}
		}

		@Override
		public void sessionClosed(IoSession session) throws Exception {
			AcceptSession s = (AcceptSession)session.removeAttribute("acceptsession");
			if(s!=null){
				s.setValid(false);
				if(s.getId()!=null){
					sessionService.removeSession(s);
					log.info("Session["+s.getId()+"]removed");
				}
			}
		}

		@Override
		public void sessionCreated(IoSession session) throws Exception {
			AcceptSession s = new AcceptSession(session);
			s.setValid(false);
			session.setAttribute("acceptsession",s);
		}
		
//		protected void serverLogin(IoSession session,UWAPData data) throws Exception{
//			String serverId = data.readString();
//			String password = data.readString();
//			ClientDetail cd = clientListManager.getClientDetail(serverId);
//			if(cd!=null&&cd.getPassWord().equals(password)){
//				PipWorldStub stub = (PipWorldStub)session.getAttribute("WorldStub");
//				stub.valid();
//				log.info("ClientServer["+session.getRemoteAddress()+"]["+serverId+"]Connected");
//			}else{
//				session.close();
//				log.info("ClientServer["+session.getRemoteAddress()+"]LoginError");
//			}
//		}
		
//		protected void accountReg(IoSession session,UWAPData data) throws Exception{
//			int requestId = data.readInt();
//			String name = data.readString();
//			String phone = data.readString();
//			String recommend = data.readString();
//			int recommendAccountId = data.readInt();
//			String model = data.readString();
//			String versionString = data.readString();
//			data.readStrings();  //charge
//			data.readString(); //feeplan
//			data.readBoolean(); //
//			String gamecode = data.readString();
//			PipWorldStub stub = (PipWorldStub)session.getAttribute("WorldStub");
//			if(stub.isValid()){
//				PipAccountRegRequest request = new PipAccountRegRequest(requestId,stub);
//				request.name = name;
//				request.phone = phone;
//				request.recommend = recommend;
//				request.recommendId = recommendAccountId;
//				request.model = model;
//				request.versionString = versionString;
//				request.gamecode = gamecode;
//				Dispatcher.dispatch(request);
//			}
//		}
	}
}
