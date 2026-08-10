package peony.net;

import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;

import peony.common.AsyncCall;
import peony.game.Client;
import peony.game.Identity;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.stepserver.StepClient;
import peony.util.TimeUtil;

public abstract class AbstractClientSession implements ClientSession{
	
	protected static final long DISCONNECTING_TIME = 4 * 60 * 1000L;
	
	private static final Logger log = Logger.getLogger(AbstractClientSession.class);

	public IoSession session;
	
	protected enum State{CONNECTED,DISCONNECTING,DISCONNECTED,AUTHENTICATED,DISCHARGED};
	
	protected volatile State state = State.CONNECTED;
	
	protected PacketHandler handler;

	protected Client client;
	
	/**
	 * 为了不生成ConcurrentLinkedQueue$Node对象，将ConcurrentLinkedQueue换成ArrayBlockingQueue。
	 * Queue最大1024个元素。
	 */
	protected ArrayBlockingQueue<Packet> queue = new ArrayBlockingQueue<Packet>(1024);
//	protected ConcurrentLinkedQueue<Packet> queue = new ConcurrentLinkedQueue<Packet>();
	
	protected ArrayBlockingQueue<AsyncCall> calls = new ArrayBlockingQueue<AsyncCall>(256);
//	protected ConcurrentLinkedQueue<AsyncCall> calls = new ConcurrentLinkedQueue<AsyncCall>();
	
	protected ClientSessionService service;
	
	protected Identity identity;
	
	protected long lastReceiveStamp = System.currentTimeMillis();
	
	protected String initChannel;
	
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
		queue.offer(packet);
//		queue.add(packet);
		lastReceiveStamp = System.currentTimeMillis();
	}
	
	public long getIdlePacketTime(){
		return System.currentTimeMillis() - lastReceiveStamp;
	}
	
	public boolean update(int diff){
		if (state == State.DISCHARGED) {
			return true;
		}
		if ((state != State.DISCONNECTING && state != State.DISCONNECTED)
				&& (System.currentTimeMillis() - lastReceiveStamp) > DISCONNECTING_TIME) {
			if(!Server.isStepServer)
				state = State.DISCONNECTED;
		}
//		if(state == State.DISCONNECTING)
//			close();
//		if(queue.size()>0)
//			log.info(String.format("queue[%d]", queue.size()));
//		Iterator<Packet> ite = queue.iterator();
		Packet packet = null;
		while((packet = queue.poll())!=null) {
//		while(ite.hasNext()){
//			Packet packet = ite.next();
			try {
				long startTime = 0;
				if (TimeUtil.monitorPerformace) {
					startTime = System.nanoTime();
				}
				getHandler().handle(packet, this, diff);
				if (TimeUtil.monitorPerformace) {
					long used = System.nanoTime() - startTime;
					if (used > 10000000L) {
						// 如果每个包的处理时间超过10毫秒，报警
						log.warn("[OPCODETOOLONG][" + packet.opCode + "," + (used / 1000000) + "]");
					}
				}
			} catch (Exception e) {
				log.error(e, e);
			} finally{
//				ite.remove();  //无论是否出异常都移除消息，防止每次都抛Exception
			}
			
		}
		AsyncCall call = null;
		while((call = calls.poll()) != null) {
//		Iterator<AsyncCall> iteCalls = calls.iterator();
//		while(iteCalls.hasNext()){
			try{
				call.callFinish();
//				iteCalls.next().callFinish();
			}catch(Exception e){
				log.error(e, e);
			}
//			iteCalls.remove();
		}
		if(state == State.DISCONNECTED){
			try {
				processStepSessionDisconnected();
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
	
	protected void processStepSessionDisconnected(){
		//如果正在进行跨服战场，则通知跨服服务器玩家断连
		try {
			if(client instanceof Player && !Server.isStepServer){
				Player player = (Player)client;
				int accountId = player.accountId;
				int playerId = player.id;
				Packet pt = new Packet(OpCode.CLIENTSERVER_STEPSERVER_INFO_CLIENT);
				pt.put(StepClient.DISCONNECTED);
				DispatchPacket dpt = new DispatchPacket(getId(), pt);
				dpt.accountId = player.accountId;
				dpt.playerId = player.id;
				Server.server.getServiceRegistry().getStepClient().send(dpt, accountId, playerId, this);
				
				Server.server.getServiceRegistry().getStepSessionService().removeAllCachSession(accountId, playerId);
			}
		} catch (Exception e) {
		}
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
	
	public String getInitChannel(){
		return initChannel;
	}
	
	public void setInitChannel(String channel){
		this.initChannel = channel;
	}
	
}
