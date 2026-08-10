package peony.channel;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.mina.util.IdentityHashSet;

import peony.game.Server;
import peony.net.ClientSession;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

public class ChannelService implements Service,ServiceEventListener{
	
	protected Map<String,Channel> channels = new HashMap<String,Channel>();
	protected Map<ClientSession,Set<Channel>> session2channels = new IdentityHashMap<ClientSession,Set<Channel>>();
	
	public ChannelService() {
	}
	
	public void startup() {
		Server.server.getEventManager().registerListener(this);
	}
	
	public int[] getEventTypes() {
		return new int[] {
				ServiceEvent.EVENT_SESSION_REMOVED,
		};
	}
	
	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_SESSION_REMOVED:
			sessionRemoved((ClientSession)event.param1);
			break;
		}
	}
	
	protected synchronized void sessionRemoved(ClientSession session) {
		Set<Channel> channels = session2channels.remove(session);
		if (channels != null) {
			for (Channel channel : channels) {
				channel.removeSession(session);
			}
		}
	}
	
	public Channel getChannel(String name){
		return channels.get(name);
	}
	
	public synchronized Channel createChannel(String name,boolean destoryOnEmpty){
		if(!channels.containsKey(name)){
			Channel channel = new Channel(this,name,destoryOnEmpty);
			channels.put(name, channel);
			return channel;
		}
		return channels.get(name);
	}
	
	public synchronized void addSessionToChannel(String name,ClientSession session){
		Channel channel = channels.get(name);
		if(channel == null)
			return;
		if(name!=null){
			Set<Channel> cs = session2channels.get(session);
			if(cs==null){
				cs = new IdentityHashSet<Channel>();
				session2channels.put(session, cs);
			}
			cs.add(channel);
			channel.addSession(session);
		}
	}
	
	public synchronized void removeSessionFromChannel(String name,ClientSession session){
		Channel channel = channels.get(name);
		if(channel!=null){
			channel.removeSession(session);
			Set<Channel> cs = session2channels.get(session);
			if(cs!=null){
				cs.remove(session);
			}
			if(channel.isDestoryOnEmpty()&&channel.getCount()==0){
				channels.remove(name);
			}
		}
	}
	
	public synchronized void removeChannel(String name){
		channels.remove(name);
	}
	
	public void shutdown(){
		
	}
}
