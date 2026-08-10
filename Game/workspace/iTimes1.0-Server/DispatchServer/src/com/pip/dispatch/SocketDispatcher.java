package com.pip.dispatch;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;  //mina1.1.x
import java.util.concurrent.atomic.*;

import org.apache.mina.common.*;
import org.apache.mina.filter.codec.*;
import org.apache.mina.transport.socket.nio.*;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
//import edu.emory.mathcs.backport.java.util.concurrent.Executors;  //mina1.0.x

public class SocketDispatcher implements Dispatcher,Runnable{

    private AtomicInteger ids = new AtomicInteger(1);
    private ConcurrentHashMap<Integer, IoSession> sessions = new ConcurrentHashMap<Integer, IoSession> ();
    private static final String ATTRIBUTE_STRING = "UWAPSESSIONID";
    private ControlProcessor processor = null;
    private ChannelService channelService = null;
    private IoAcceptor acceptor = null;
    private IoConnector connector = null;
    private IoSession serverSession = null;
    private TrustIpService trustIpService = null;
    private Configuration configuration = null;
    
    private ChatService chatService = null;
    public void setChatService(ChatService chatService) {
		this.chatService = chatService;
	}

	private static final byte REPORT_CLIENT_IP = (byte)220;
    private static final byte SESSION_CLOSED = (byte)186;
    private static final byte SERVER_LOGIN = (byte)180;
    public static final String SERVERID = "serverid";
    public static final String SERVERNAME = "servername";
    public static final String SERVERPASSWORD = "serverpassword";

    private static final Logger log = Logger.getLogger(SocketDispatcher.class);

    private boolean shutdown = false;

    public SocketDispatcher(ControlProcessor processor,Configuration configuration) {
        this.processor = processor;
        this.configuration = configuration;
        new Thread(this,"OnlinePrinter").start();
        new Thread(new Normal90Sender(),"Normal90Sender").start();
        new Thread(new Fast90Sender(),"Fast90Sender").start();
    }

    public void setChannelService(ChannelService channelService){
        this.channelService = channelService;
    }

    public void setTrustIpService(TrustIpService trustIpService){
        this.trustIpService = trustIpService;
    }

    public void dispatchToServer(IoSession session, Object object) {
        Integer id = (Integer)session.getAttribute(ATTRIBUTE_STRING);
        if(id!=null){
            ByteBuffer buffer = (ByteBuffer)object;
            buffer.putInt(5,id.intValue());
            serverSession.write(buffer.duplicate());
        }
    }

    public IoSession getSession(int sessionId){
        return sessions.get(sessionId);
    }
    
    public void sendControlSegment(UWAPSegment seg){
        seg.setSessionId(-1);
        serverSession.write(ByteBuffer.wrap(seg.getPacketByteArray()));
    }

    public ConnectFuture connect(SocketAddress address,SocketConnectorConfig config){
        connector = new SocketConnector(4,Executors.newCachedThreadPool());
        config.setThreadModel(ThreadModel.MANUAL);
//        ((SocketSessionConfig)config.getSessionConfig()).setTcpNoDelay(true); //mina1.0.x
        config.getSessionConfig().setTcpNoDelay(true);  //mina1.1.x
        config.getFilterChain().addLast("codec",new ProtocolCodecFilter(new ServerUWAPEncoder(),new ServerUWAPDecoder()));
        return connector.connect(address,new ServerSessionHandler(),config);
    }

    public void bind(SocketAddress address,SocketAcceptorConfig config) throws IOException{
        acceptor = new SocketAcceptor(4,Executors.newCachedThreadPool());
        config.setThreadModel(ThreadModel.MANUAL);
//        ((SocketSessionConfig)config.getSessionConfig()).setTcpNoDelay(true); //mina1.0.x
        config.getSessionConfig().setTcpNoDelay(true); //mina1.1.x
        config.getFilterChain().addLast("codec",new ProtocolCodecFilter(new SimpleUWAPEncoder(),new SimpleUWAPDecoder1()));
        acceptor.bind(address,new ClientSessionHandler(),config);
    }

    public void dispatchToClient(Packet packet) {
        dispatchToClient(packet.sessionId,packet.buffer);
    }

    public void dispatchToClient(int sessionId,ByteBuffer buffer){
        IoSession s = sessions.get(sessionId);
        if(s!=null){
        	if (buffer.remaining() > 32000) {
        		byte[] arr = new byte[buffer.remaining()];
        		buffer.get(arr);
        		for (int start = 0; start < arr.length; start += 32000) {
        			int thisLen = 32000;
        			if (start + thisLen > arr.length) {
        				thisLen = arr.length - start;
        			}
        			s.write(ByteBuffer.wrap(arr, start, thisLen));
        			try {
        				Thread.sleep(300);
        			} catch (Exception e) {
        			}
        		}
        	} else {
        		s.write(buffer);
        	}
        }
    }

    public void registerClient(IoSession session) {
        int id = ids.incrementAndGet();
        if(id<0){
            log.info("SessionId[-1]");
        }
        session.setIdleTime(IdleStatus.READER_IDLE,300);
        session.setAttribute(ATTRIBUTE_STRING, id);
        sessions.put(id, session);
        channelService.getFast90Channel().join(session);
    }

    protected void unRegisterClient(IoSession session) {
        channelService.removeSessionFromAllChannel(session);
        chatService.removePlayerDataVersion(session);
        Integer sessionId = (Integer)session.getAttribute(ATTRIBUTE_STRING);
        if(sessionId!=null){
            UWAPSegment seg = new UWAPSegment(SESSION_CLOSED);
            seg.writeInt(sessionId);
            sendControlSegment(seg);
            sessions.remove(sessionId);
        }
    }



    public void unRegisterClient(int sessionId){
        IoSession session = sessions.remove(sessionId);
        if(session!=null&&session.isConnected()){
            session.close();
        }
    }

    protected void processControl(Packet packet){
        processor.process(packet.data);
    }

    public void broadcast(ByteBuffer buffer){
        for(IoSession session:sessions.values()){
            session.write(buffer.duplicate());
        }
        buffer.release();
    }

    public void shutdown(){
        acceptor.unbindAll();
        shutdown = true;
    }

    public void run(){
        while(true){
            try {
                Thread.sleep(60 * 1000L);
            }
            catch (InterruptedException ex) {
            }
            log.info("ONLINE[" + sessions.size() + "]");
        }
    }

    class ClientSessionHandler
        extends IoHandlerAdapter {


        public void exceptionCaught(IoSession sesion, Throwable throwable) throws
            Exception {
            log.error(throwable,throwable);
        }

        public void messageReceived(IoSession session, Object object) throws
            Exception {
            SocketDispatcher.this.dispatchToServer(session, object);
        }

        public void sessionClosed(IoSession session) throws Exception {
            if(!shutdown)
                SocketDispatcher.this.unRegisterClient(session);
        }

        public void sessionCreated(IoSession session) throws Exception {
            InetSocketAddress address = (InetSocketAddress)session.getRemoteAddress();
            if(!trustIpService.isTrustIp(address)){
                session.close();
                log.info("DISTRUST IP[" + address.toString() + "]");
            }else{
                SocketDispatcher.this.registerClient(session);
            }
            
            UWAPSegment seg = new UWAPSegment(REPORT_CLIENT_IP);
            byte[] addr = address.getAddress().getAddress();
            int reportIp = ((addr[0] & 0xFF) << 24) | ((addr[1] & 0xFF) << 16) | ((addr[2] & 0xFF) << 8) | (addr[3] & 0xFF);
            seg.writeInt(reportIp);
            SocketDispatcher.this.dispatchToServer(session, ByteBuffer.wrap(seg.getPacketByteArray()));
        }

        public void sessionIdle(IoSession session, IdleStatus idleStatus) throws
            Exception {
            session.close();
        }
    }

    class ServerSessionHandler extends IoHandlerAdapter{
        public void exceptionCaught(IoSession sesion, Throwable throwable) throws
           Exception {
           log.error(throwable,throwable);
       }

       public void messageReceived(IoSession session, Object object) throws
           Exception {
           Packet packet = (Packet)object;
           if(packet.type==Packet.TYPE.BUFFER){
               dispatchToClient(packet);
           }else{
               processControl(packet);
           }
       }

//       public void messageSent(IoSession session, Object object) throws Exception{
//           System.out.println(object.toString());
//       }

       public void sessionClosed(IoSession session) throws Exception {
           serverSession = null;
       }

       public void sessionCreated(IoSession session) throws Exception {
           serverSession = session;
           UWAPSegment seg = new UWAPSegment(SERVER_LOGIN);
           seg.writeString( (String) configuration.getProperty(
               SERVERID));
           seg.writeString( (String) configuration.getProperty(
               SERVERPASSWORD));
           seg.writeInt(configuration.getInt("maxplayer"));
           seg.writeString(configuration.getString("servertype"));
           sendControlSegment(seg);

       }

       public void sessionIdle(IoSession session, IdleStatus idleStatus) throws
           Exception {
       }

    }

    class Fast90Sender
        implements Runnable {
        public void run() {
            while(true){
                try {
                    try {
                        Thread.sleep(3 * 1000L);
                    }
                    catch (InterruptedException ex) {
                    }
                    if (channelService != null) {
                        int time = (int)((System.currentTimeMillis()+8*3600*1000)/1000);
                        UWAPSegment seg = new UWAPSegment( (byte) 90);
                        seg.writeInt(time);
                        channelService.getFast90Channel().broadcast(ByteBuffer.
                            wrap(
                                seg.getPacketByteArray()));
                    }
                }
                catch (Exception ex1) {
                }
            }
        }
    }

    class Normal90Sender
        implements Runnable {
        public void run() {
            while (true) {
                try {
                    try {
                        Thread.sleep(30 * 1000L);
                    }
                    catch (InterruptedException ex) {
                    }
                    if (channelService != null) {
                        int time = (int)((System.currentTimeMillis()+8*3600*1000)/1000);
                        UWAPSegment seg = new UWAPSegment( (byte) 90);
                        seg.writeInt(time);
                        channelService.getNormal90Channel().broadcast(
                            ByteBuffer.
                            wrap(
                                seg.getPacketByteArray()));
                    }
                }
                catch (Exception ex1) {
                }
            }
        }
    }
}
