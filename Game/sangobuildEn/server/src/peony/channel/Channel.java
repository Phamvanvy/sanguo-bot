package peony.channel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import peony.net.ClientSession;
import peony.net.Packet;

public class Channel {
	
	protected Set<ClientSession> sessions = new HashSet<ClientSession>();
	protected String name;
	protected ChannelService service;
	protected boolean destoryOnEmpty;
	
	public Channel(ChannelService service,String name,boolean destoryOnEmpty){
		this.service = service;
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	/**
	 * 如果filter为空，那么object必须是Packet类型
	 * @param object
	 * @param filter
	 */
	public synchronized void broadcast(Object object,ChannelFilter filter){
		Iterator<ClientSession> ite = sessions.iterator();
		while(ite.hasNext()){
			ClientSession session = ite.next();
			if (session == null) {
			    continue;
			}
			if(filter!=null){
				filter.filter(session, object, this);
			}else{
				session.send((Packet)object);
			}
		}
	}
	
	public synchronized void broadcast(Packet packet){
		Iterator<ClientSession> ite = sessions.iterator();
		while(ite.hasNext()){
			ClientSession session = ite.next();
			if (session == null) {
			    continue;
			}else{
				session.send(packet);
			}
		}
	}
	
	synchronized void addSession(ClientSession session){
		sessions.add(session);
	}
	
	synchronized void removeSession(ClientSession session){
		sessions.remove(session);
	}
	
	public int getCount(){
		return sessions.size();
	}
	
	public boolean isDestoryOnEmpty(){
		return destoryOnEmpty;
	}
}
