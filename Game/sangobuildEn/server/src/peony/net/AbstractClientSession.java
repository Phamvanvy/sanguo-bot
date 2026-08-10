package peony.net;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;

import peony.common.AsyncCall;
import peony.game.Client;
import peony.game.Identity;

public abstract class AbstractClientSession implements ClientSession{
	
	protected static final long DISCONNECTING_TIME = 4 * 60 * 1000L;
	
	private static final Logger log = Logger.getLogger(AbstractClientSession.class);

	protected IoSession session;
	
	protected enum State{CONNECTED,DISCONNECTING,DISCONNECTED,AUTHENTICATED,DISCHARGED};
	
	protected volatile State state = State.CONNECTED;
	
	protected PacketHandler handler;

	protected Client client;
	
	protected ConcurrentLinkedQueue<Packet> queue = new ConcurrentLinkedQueue<Packet>();
	
	protected ConcurrentLinkedQueue<AsyncCall> calls = new ConcurrentLinkedQueue<AsyncCall>();
	
	protected ClientSessionService service;
	
	protected Identity identity;
	
	protected long lastReceiveStamp = System.currentTimeMillis();
	
	public AbstractClientSession(ClientSessionService service,IoSession session,PacketHandler handler){
		this.service = service;
		this.session = session;
		this.handler = handler;
	}
	
	public void send(Packet packet){
		if(state==State.CONNECTED||state==State.AUTHENTICATED)
			session.write(packet);
	}
	
	public PacketHandler getHandler(){
		return handler;
	}
	
	public IoSession getIoSession(){
		return session;
	}

	
	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
		if (client != null) {
			client.setSession(this);
		}
	}
	
	public void keepLive() {
		lastReceiveStamp = System.currentTimeMillis();
	}
	
	public void addPacket(Packet packet){
		queue.add(packet);
		lastReceiveStamp = System.currentTimeMillis();
	}
	
	
	public boolean update(int diff){
		if (state == State.DISCHARGED) {
			return true;
		}
		if ((state != State.DISCONNECTING && state != State.DISCONNECTED)
				&& (System.currentTimeMillis() - lastReceiveStamp) > DISCONNECTING_TIME) {
			state = State.DISCONNECTED;
		}
//		if(state == State.DISCONNECTING)
//			close();
//		if(queue.size()>0)
//			log.info(String.format("queue[%d]", queue.size()));
		Iterator<Packet> ite = queue.iterator();
		while(ite.hasNext()){
			Packet packet = ite.next();
			try {
				getHandler().handle(packet, this, diff);
			} catch (Exception e) {
				log.error(e, e);
			} finally{
				ite.remove();  //无论是否出异常都移除消息，防止每次都抛Exception
			}
			
		}
		Iterator<AsyncCall> iteCalls = calls.iterator();
		while(iteCalls.hasNext()){
			try{
				iteCalls.next().callFinish();
			}catch(Exception e){
				log.error(e, e);
			}
			iteCalls.remove();
		}
		if(state == State.DISCONNECTED){
			try {
				getHandler().disconnected(this);
			} catch (Exception e) {
				log.error(e,e);
				e.printStackTrace();
			}
			close();
			state = State.DISCHARGED;
			return true;
		}
		return false;
	}
	
	public void cleanMessageQueue(){
		queue.clear();
	}
	
	public void addAsyncCall(AsyncCall call){
		calls.add(call);
	}
	
	public void close(){
		service.removeClientSession(this);
	}
	
	public void setDisconnecting(){
		state = State.DISCONNECTING;
	}
	
	public void setDisconnected(){
		state = State.DISCONNECTED;
	}
	
	public void authenticate(Identity identity) throws SecurityException{
		this.identity = identity;
		state = State.AUTHENTICATED;
	}
	
	public Identity getIdentity(){
		return identity;
	}
}
