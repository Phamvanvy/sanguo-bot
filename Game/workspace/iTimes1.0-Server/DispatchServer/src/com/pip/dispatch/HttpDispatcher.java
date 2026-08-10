package com.pip.dispatch;

import java.net.*;
import java.util.concurrent.*;

import org.apache.mina.common.*;
import org.apache.mina.filter.codec.*;
import org.apache.mina.transport.socket.nio.*;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

public class HttpDispatcher implements Dispatcher,Runnable{

//    private ConcurrentHashMap<Integer, HttpSession> sessions = new ConcurrentHashMap<Integer, HttpSession> ();
    private ControlProcessor processor = null;
    private ChannelService channelService = null;
    private HttpAcceptor acceptor = null;
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

    private static final Logger log = Logger.getLogger(HttpDispatcher.class);

    public HttpDispatcher(ControlProcessor processor,Configuration configuration) {
        this.processor = processor;
        this.configuration = configuration;
        new Thread(this,"OnlinePrinter").start();
//        new Thread(new Normal90Sender(),"Normal90Sender").start();
//        new Thread(new Fast90Sender(),"Fast90Sender").start();
    }

    public void setChannelService(ChannelService channelService){
        this.channelService = channelService;
    }

    public void setTrustIpService(TrustIpService trustIpService){
        this.trustIpService = trustIpService;
    }

    public void dispatchToServer(HttpSession session, Object object) {
        int sessionId = session.getSessionId();
        ByteBuffer buffer = (ByteBuffer) object;
        buffer.putInt(5, sessionId);
        serverSession.write(buffer.duplicate());
    }

    public HttpSession getSession(int sessionId){
        return acceptor.getSession(sessionId);
    }

    public void sendControlSegment(UWAPSegment seg){
        seg.setSessionId(-1);
        serverSession.write(ByteBuffer.wrap(seg.getPacketByteArray()));
    }

    public ConnectFuture connect(SocketAddress address,SocketConnectorConfig config){
        connector = new SocketConnector(1,Executors.newCachedThreadPool());
        config.setThreadModel(ThreadModel.MANUAL);
        config.getSessionConfig().setTcpNoDelay(true);
        config.getFilterChain().addLast("codec",new ProtocolCodecFilter(new ServerUWAPEncoder(),new ServerUWAPDecoder()));
        return connector.connect(address,new ServerSessionHandler(),config);
    }

    public void bind(String address,int port) throws Exception{
        acceptor = new HttpAcceptor();
        acceptor.bind(address,port,new ClientSessionHandler());
    }

    public void dispatchToClient(Packet packet) {
        dispatchToClient(packet.sessionId,packet.buffer);
    }

    public void dispatchToClient(int sessionId,ByteBuffer buffer){
        IoSession s = getSession(sessionId);
        if(s!=null){
            s.write(buffer);
        }
    }

//    public void registerClient(HttpSession session) {
//        int id = ids.incrementAndGet();
//
//        sessions.put(id, session);
//        channelService.getFast90Channel().join(session);
//    }

    protected void unRegisterClient(HttpSession session) {
        channelService.removeSessionFromAllChannel(session);
        chatService.removePlayerDataVersion(session);
        UWAPSegment seg = new UWAPSegment(SESSION_CLOSED);
        seg.writeInt(session.getSessionId());
        sendControlSegment(seg);
    }



    public void unRegisterClient(int sessionId){
        acceptor.closeSession(sessionId);
    }

    protected void processControl(Packet packet){
        processor.process(packet.data);
    }

    public void broadcast(ByteBuffer buffer){
        acceptor.broadcast(buffer);
    }

    public void shutdown(){
        acceptor.stop();
    }

    public void run(){
        while(true){
            try {
                Thread.sleep(60 * 1000L);
            }
            catch (InterruptedException ex) {

            }
            log.info("ONLINE[" + acceptor.size() + "]");
        }
    }

    class ClientSessionHandler
        extends IoHandlerAdapter {


        public void exceptionCaught(IoSession sesion, Throwable throwable) throws
            Exception {
            throwable.printStackTrace();
        }

        public void messageReceived(IoSession session, Object object) throws
            Exception {
            HttpDispatcher.this.dispatchToServer((HttpSession)session, object);
        }

        public void sessionClosed(IoSession session) throws Exception {
            HttpDispatcher.this.unRegisterClient((HttpSession)session);
        }

        public void sessionCreated(IoSession session) throws Exception {
            InetSocketAddress address = (InetSocketAddress)session.getRemoteAddress();
            if(!trustIpService.isTrustIp(address)){
                session.close();
                log.info("DISTRUST IP[" + address.toString() + "]");
            }
            
            UWAPSegment seg = new UWAPSegment(REPORT_CLIENT_IP);
            byte[] addr = address.getAddress().getAddress();
            int reportIp = ((addr[0] & 0xFF) << 24) | ((addr[1] & 0xFF) << 16) | ((addr[2] & 0xFF) << 8) | (addr[3] & 0xFF);
            seg.writeInt(reportIp);
            HttpDispatcher.this.dispatchToServer((HttpSession)session, ByteBuffer.wrap(seg.getPacketByteArray()));
        }

        public void sessionIdle(IoSession session, IdleStatus idleStatus) throws
            Exception {
            session.close();
        }
    }

    class ServerSessionHandler extends IoHandlerAdapter{
        public void exceptionCaught(IoSession sesion, Throwable throwable) throws
           Exception {
           throwable.printStackTrace();
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
