package peony.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedHashSet;

import org.apache.commons.configuration.Configuration;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

import peony.game.Server;


public class DirectClientSessionService extends AbstractClientSessionService {
	
	
	protected LinkedHashSet<DirectClientSession> sessions = new LinkedHashSet<DirectClientSession>();
	
	public DirectClientSessionService(Configuration config,PacketHandler handler){
		super(config,handler);
	}
	
	public void startup() throws Exception {
		bind();
	}

	public void bind() throws IOException{
		acceptor = new SocketAcceptor();
		SocketAcceptorConfig cfg = new SocketAcceptorConfig();
        cfg.getFilterChain().addLast( "codec", new ProtocolCodecFilter(MinaUAEncoder.class,MinaUADecoder.class));
        acceptor.bind( new InetSocketAddress(config.getString(ADDRESS),config.getInt(PORT)), new DirectClientSessionHandler(), cfg);
	}
	
	
	public void addClientSession(ClientSession session){
		sessions.add((DirectClientSession)session);
		notifySessionAdded(session);
	}
	
	public void removeClientSession(ClientSession session){
		sessions.remove(session);
		notifySessionRemoved(session);
	}
	
	class DirectClientSessionHandler extends IoHandlerAdapter{
		@Override
		public void exceptionCaught(IoSession session, Throwable t) throws Exception {
		    t.printStackTrace();
		}

		@Override
		public void messageReceived(IoSession session, Object msg) throws Exception {
			DirectClientSession s = (DirectClientSession)session.getAttachment();
			if(s!=null&&msg instanceof Packet){
				s.addPacket((Packet)msg);
			}
		}
		
		@Override
		public void sessionClosed(IoSession session) throws Exception{
			DirectClientSession ds = (DirectClientSession)session.getAttachment();
			if(ds!=null){
				session.setAttachment(null);
				ds.setDisconnected();
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
			
			DirectClientSession dSession = new DirectClientSession(DirectClientSessionService.this,session,handler);
			session.setAttachment(dSession);
			addClientSession(dSession);
		}
	}

}
