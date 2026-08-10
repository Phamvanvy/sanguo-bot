package peony.net;

import org.apache.commons.configuration.Configuration;
import org.apache.mina.transport.socket.nio.SocketAcceptor;

import peony.game.Server;
import peony.service.ServiceEvent;

public abstract class AbstractClientSessionService implements ClientSessionService {

	protected SocketAcceptor acceptor;
	
	protected static final String ADDRESS = "address";
	protected static final String PORT = "port";
	
	protected PacketHandler handler;
	protected Configuration config;
	
	public AbstractClientSessionService(Configuration config,PacketHandler handler){
		this.config = config;
		this.handler = handler;
	}

	protected void notifySessionRemoved(ClientSession session){
		Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_SESSION_REMOVED, session));
	}
	
	protected void notifySessionAdded(ClientSession session){
		Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_SESSION_ADDED, session));
	}

	public void shutdown() {
		if(acceptor!=null)
			acceptor.unbindAll();
	}

}
