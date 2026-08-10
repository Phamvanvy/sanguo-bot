package com.pip.dispatch.tomcat;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.mina.common.*;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;
import org.mortbay.jetty.*;
import org.mortbay.jetty.nio.*;
import org.mortbay.jetty.servlet.*;
import org.mortbay.thread.*;

import com.pip.dispatch.*;

/**
 * 寄生于Tomcat的HTTP分配器主接口。
 */
public class TomcatHttpAcceptor extends HttpServlet implements Runnable {
    private static Logger log;

    private AtomicInteger ids = new AtomicInteger(1);
    private HttpUWAPDecoder decoder;
    private HttpUWAPEncoder encoder;
    private ConcurrentHashMap<Integer, TomcatHttpSession> sessions;

    private Configuration configuration = null;
    private TomcatHttpDispatcher dispatcher = null;
    private ChannelService channelService = null;
    private ChatService chatService = null;
    private TimeControlProcessor controlProcessor = null;
    private TrustIpService trustIpService = null;
    private IpdService ipdService = null;
    
    private Thread workingThread;
    private boolean stopped = false;

    /**
     * 初始化分配器。
     */
    public void init() throws ServletException {
        // 初始化日志文件
        String file = getInitParameter("log4j-init-file");
        if (file != null) {
            try {
                String confFile = getServletContext().getRealPath(file);
                Properties prop = new Properties();
                FileInputStream is = new FileInputStream(confFile);
                prop.load(is);
                is.close();

                String logPath = prop.getProperty("log4j.appender.daylylog.file");
                logPath = getServletContext().getRealPath("WEB-INF/logs") + File.separator + logPath;
                prop.setProperty("log4j.appender.daylylog.file", logPath);

                PropertyConfigurator.configure(prop);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log = Logger.getLogger(TomcatHttpAcceptor.class);
        
        String rootPath = getServletContext().getRealPath("WEB-INF");
        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());

        decoder = new HttpUWAPDecoder();
        encoder = new HttpUWAPEncoder();
        sessions = new ConcurrentHashMap<Integer, TomcatHttpSession>();

        try {
            // 载入配置创建服务
            configuration = new PropertiesConfiguration(new File(rootPath, "config.properties"));
            channelService = new ChannelService();
            controlProcessor = new TimeControlProcessor();
            trustIpService = new TrustIpService(new File(rootPath, "trustip.txt"));
            controlProcessor.setChannelService(channelService);
            ipdService = new IpdService("embedhttp", configuration.getStringArray("ipd"), configuration);
            
            controlProcessor.setIpdService(ipdService);
            chatService = new ChatService();
            controlProcessor.setChatService(chatService);
            
            // 创建转发管理器
            dispatcher = new TomcatHttpDispatcher(controlProcessor, configuration);
            dispatcher.setAcceptor(this);
            dispatcher.setChannelService(channelService);
            dispatcher.setTrustIpService(trustIpService);
            dispatcher.setChatService(chatService);
            controlProcessor.setDispatcher(dispatcher);
            
            // 连接世界服务器
            ConnectFuture future = dispatcher.connect(new InetSocketAddress(
                configuration.getString("worldip"),
                configuration.getInt("worldport")), new SocketConnectorConfig());
            future.join();
            log.info("Http Dispatch Started");
        } catch (Exception e) {
            throw new ServletException(e);
        }
        
        // 启动自己的线程
        workingThread = new Thread(this);
        workingThread.start();
    }

    /**
     * 关闭分配器。
     */
    public void destroy() {
        dispatcher.shutdown();
    }

    /**
     * 通知会话关闭。
     * @param session
     */
    public void notifyClose(TomcatHttpSession session){
        try {
            synchronized(session) {
                sessions.remove(session.getSessionId());
            }
        } catch (Exception ex) {
            log.error(ex, ex);
        }
    }

    /**
     * 取得当前会话数。
     * @return
     */
    public int size(){
        return sessions.size();
    }

    /*
     * 客户端通过POST方法访问。
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/octet-stream");
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            Packet[] packets = decoder.decode(request.getInputStream());
            handle(packets, response.getOutputStream(), request);
        } catch (Exception ex) {
            log.error(ex, ex);
        }
    }

    /*
     * GET方法访问时，读取服务器地址通知分配器。服务器启动后需要通过一次GET访问来设置此信息（init中
     * 无法获得服务器地址）。
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 刷新访问地址给分配器
        String path = request.getRequestURL().toString();
        ipdService.setEmbedURL(path);
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("OK");
    }

    /*
     * 处理客户端一次请求发送的多个UWAP包。
     * @param packet 本次请求的所有UWAP包
     * @param out 输出流
     * @param request 请求对象
     */
    private void handle(Packet[] packet, ServletOutputStream out, HttpServletRequest request) throws Exception {
        // 处理请求
        TomcatHttpSession httpSession = null;
        for (int i = 0; i < packet.length; i++) {
            httpSession = handle(packet[i], out, request);
        }
        
        // 检查是否有需要返回客户端的包
        if (httpSession!= null) {
            ByteBuffer[] segments = httpSession.getSegments();
            
            // 如果暂时没有包，等待1秒钟
            if (segments == null || segments.length==0) {
                try {
                    synchronized (httpSession) {
                        httpSession.wait(1000L);
                    }
                } catch (InterruptedException ex1) {
                }
                segments = httpSession.getSegments();
            }
            
            // 如果等待1秒还是没有包，则生成一个占位包给客户端
            if (segments == null || segments.length == 0) {
                segments = new ByteBuffer[1];
                UWAPSegment seg = new UWAPSegment((byte)92, -1, httpSession.getSessionId());
                segments[0] = ByteBuffer.wrap(seg.getPacketByteArray());
            }
            
            // 返回给客户端
            try {
                encoder.encode(out, segments);
            } catch (Exception ex) {
                log.error(ex, ex);
            }
        }
    }
    
    /*
     * 处理客户端发上来的一个请求包。
     * @param packet 请求UWAP包
     * @param out 输出流
     * @param request 请求对象
     * @return 如果成功找到或创建了一个session，返回。
     * @throws Exception
     */
    private TomcatHttpSession handle(Packet packet, ServletOutputStream out, HttpServletRequest request) throws Exception {
        if (packet.sessionId == -1) {
            // 如果sessionid为-1，表示客户端的第一次请求，创建一个新的session
            InetSocketAddress address = new InetSocketAddress(request.getRemoteAddr(), request.getRemotePort());
            TomcatHttpSession session = new TomcatHttpSession(this, address, ids.incrementAndGet());
            sessions.put(session.getSessionId(), session);
            dispatcher.sessionCreated(session);
            dispatcher.messageReceived(session, packet.buffer);
            return session;
        } else {
            TomcatHttpSession session = sessions.get(packet.sessionId);
            if (session != null) {
                session.setLastReadTime(System.currentTimeMillis());
                dispatcher.messageReceived(session, packet.buffer);
            }
            return session;
        }
    }

    public TomcatHttpSession getSession(int sessionId) {
        return sessions.get(sessionId);
    }

    public void closeSession(int sessionId) {
        TomcatHttpSession session = sessions.get(sessionId);
        if (session != null) {
            session.close();
        }
    }

    public void broadcast(ByteBuffer buffer) {
        for (IoSession session : sessions.values()) {
            session.write(buffer);
        }
    }

    public void stop(){
        controlProcessor.shutdown();
        trustIpService.shutdown();
        ipdService.shutdown();
        sessions.clear();
        stopped = true;
        workingThread.interrupt();
    }

    public void run() {
        while (!stopped) {
            try {
                checkIdle();
            } catch (Throwable ex1) {
                log.error(ex1, ex1);
            }
            try {
                Thread.sleep(60 * 1000L);
            } catch (InterruptedException ex) {
            }
        }
    }

    /*
     * 检查是否有idle的连接，删除。
     */
    private void checkIdle(){
        synchronized(sessions) {
            Iterator ite = sessions.values().iterator();
            long currTime = System.currentTimeMillis();
            while (ite.hasNext()) {
                TomcatHttpSession session = (TomcatHttpSession)ite.next();
                if ((currTime - session.getLastReadTime()) > 300000L) {
                    try {
                        dispatcher.sessionClosed(session);
                    } catch (Exception e) {
                        log.error(e, e);
                    }
                }
            }
        }
    }
}
