package com.pip.dispatch.tomcat;

import java.net.*;
import java.util.concurrent.*;

import org.apache.mina.common.*;
import org.apache.mina.filter.codec.*;
import org.apache.mina.transport.socket.nio.*;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import com.pip.dispatch.*;

public class TomcatHttpDispatcher implements com.pip.dispatch.Dispatcher, Runnable {
    private ControlProcessor processor = null;
    private ChannelService channelService = null;
    private TomcatHttpAcceptor acceptor = null;
    private IoConnector connector = null;
    private IoSession serverSession = null;
    private TrustIpService trustIpService = null;
    private Configuration configuration = null;
    
    private ChatService chatService = null;
    public void setChatService(ChatService chatService) {
		this.chatService = chatService;
	}
    
    private Thread workingThread;
    private boolean stopped = false;

    private static final byte REPORT_CLIENT_IP = (byte)220;
    private static final byte SESSION_CLOSED = (byte)186;
    private static final byte SERVER_LOGIN = (byte)180;
    public static final String SERVERID = "serverid";
    public static final String SERVERNAME = "servername";
    public static final String SERVERPASSWORD = "serverpassword";

    private static final Logger log = Logger.getLogger(TomcatHttpDispatcher.class);

    public TomcatHttpDispatcher(ControlProcessor processor, Configuration configuration) {
        this.processor = processor;
        this.configuration = configuration;
        workingThread = new Thread(this);
        workingThread.start();
    }

    public void setAcceptor(TomcatHttpAcceptor o) {
        acceptor = o;
    }
    
    public void setChannelService(ChannelService channelService){
        this.channelService = channelService;
    }

    public void setTrustIpService(TrustIpService trustIpService){
        this.trustIpService = trustIpService;
    }

    public void dispatchToServer(TomcatHttpSession session, Object object) {
        int sessionId = session.getSessionId();
        ByteBuffer buffer = (ByteBuffer) object;
        buffer.putInt(5, sessionId);
        serverSession.write(buffer.duplicate());
    }

    public TomcatHttpSession getSession(int sessionId) {
        return acceptor.getSession(sessionId);
    }

    public void sendControlSegment(UWAPSegment seg){
        seg.setSessionId(-1);
        serverSession.write(ByteBuffer.wrap(seg.getPacketByteArray()));
    }

    public ConnectFuture connect(SocketAddress address, SocketConnectorConfig config) {
        connector = new SocketConnector(1,Executors.newCachedThreadPool());
        config.setThreadModel(ThreadModel.MANUAL);
        config.getSessionConfig().setTcpNoDelay(true);
        config.getFilterChain().addLast("codec",new ProtocolCodecFilter(new ServerUWAPEncoder(), new ServerUWAPDecoder()));
        return connector.connect(address, new ServerSessionHandler(), config);
    }

    public void dispatchToClient(Packet packet) {
        dispatchToClient(packet.sessionId, packet.buffer);
    }

    public void dispatchToClient(int sessionId,ByteBuffer buffer) {
        IoSession s = getSession(sessionId);
        if (s != null) {
            s.write(buffer);
        }
    }

    protected void unRegisterClient(TomcatHttpSession session) {
        channelService.removeSessionFromAllChannel(session);
        chatService.removePlayerDataVersion(session);
        UWAPSegment seg = new UWAPSegment(SESSION_CLOSED);
        seg.writeInt(session.getSessionId());
        sendControlSegment(seg);
        session.close();
    }

    public void unRegisterClient(int sessionId) {
        TomcatHttpSession session = acceptor.getSession(sessionId);
        if (session != null) {
            unRegisterClient(session);
        }
    }

    protected void processControl(Packet packet) {
        processor.process(packet.data);
    }

    public void broadcast(ByteBuffer buffer) {
        acceptor.broadcast(buffer);
    }

    public void shutdown(){
        acceptor.stop();
        stopped = true;
        workingThread.interrupt();
    }

    public void run() {
        while (!stopped) {
            try {
                Thread.sleep(60 * 1000L);
            } catch (InterruptedException ex) {
            }
            log.info("ONLINE[" + acceptor.size() + "]");
        }
    }


    public void messageReceived(IoSession session, Object object) throws Exception {
        dispatchToServer((TomcatHttpSession)session, object);
    }

    public void sessionClosed(IoSession session) throws Exception {
        unRegisterClient((TomcatHttpSession)session);
    }

    public void sessionCreated(IoSession session) throws Exception {
        InetSocketAddress address = (InetSocketAddress)session.getRemoteAddress();
        if (!trustIpService.isTrustIp(address)) {
            session.close();
            log.info("DISTRUST IP[" + address.toString() + "]");
        }
        
        UWAPSegment seg = new UWAPSegment(REPORT_CLIENT_IP);
        byte[] addr = address.getAddress().getAddress();
        int reportIp = ((addr[0] & 0xFF) << 24) | ((addr[1] & 0xFF) << 16) | ((addr[2] & 0xFF) << 8) | (addr[3] & 0xFF);
        seg.writeInt(reportIp);
        dispatchToServer((TomcatHttpSession)session, ByteBuffer.wrap(seg.getPacketByteArray()));
    }

    class ServerSessionHandler extends IoHandlerAdapter {
        public void exceptionCaught(IoSession sesion, Throwable throwable) throws Exception {
            log.error(throwable, throwable);
        }

        public void messageReceived(IoSession session, Object object) throws Exception {
            Packet packet = (Packet) object;
            if (packet.type == Packet.TYPE.BUFFER) {
                dispatchToClient(packet);
            } else {
                processControl(packet);
            }
        }

        public void sessionClosed(IoSession session) throws Exception {
            serverSession = null;
        }

        public void sessionCreated(IoSession session) throws Exception {
            serverSession = session;
            UWAPSegment seg = new UWAPSegment(SERVER_LOGIN);
            seg.writeString((String) configuration.getProperty(SERVERID));
            seg.writeString((String) configuration.getProperty(SERVERPASSWORD));
            seg.writeInt(configuration.getInt("maxplayer"));
            seg.writeString(configuration.getString("servertype"));
            sendControlSegment(seg);
        }

        public void sessionIdle(IoSession session, IdleStatus idleStatus) throws Exception {
        }
    }
}
