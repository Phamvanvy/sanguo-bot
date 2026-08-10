package com.pip.dispatch;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.mina.common.*;
import org.mortbay.jetty.*;
import org.mortbay.jetty.nio.*;
import org.mortbay.jetty.servlet.*;
import org.mortbay.thread.*;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class HttpAcceptor implements Runnable{
    private AtomicInteger ids = new AtomicInteger(1);
    private int port = 80;
    private IoHandler handler;
//    private SessionRegistry registry;
    private HttpUWAPDecoder decoder = new HttpUWAPDecoder();
    private HttpUWAPEncoder encoder = new HttpUWAPEncoder();
    private ConcurrentHashMap<Integer, HttpSession> sessions = new ConcurrentHashMap<Integer, HttpSession> ();
    private Server server;

    public HttpAcceptor() {
    }

    public void bind(String address,int port, IoHandler handler) throws Exception {
        this.port = port;
        this.handler = handler;
//        this.registry = handler.registry;
        server = new Server();
        BoundedThreadPool threadPool = new BoundedThreadPool();
        threadPool.setMinThreads(10);
        threadPool.setMaxThreads(50);
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort (port);
        connector.setHost (address);
        server.addConnector(connector);
        Context root = new Context(server, "/", Context.SESSIONS);
        root.addServlet(new ServletHolder(new HttpUWAPServlet()), "/*");
        server.start();
//        server.join();
        new Thread(this).start();
    }

    public void notifyClose(HttpSession session){
        try {
            synchronized(session){
                sessions.remove(session.getSessionId());
            }
            handler.sessionClosed(session);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int size(){
        return sessions.size();
    }

    class HttpUWAPServlet extends HttpServlet {
        protected void doPost(HttpServletRequest request,
                              HttpServletResponse response) throws
            ServletException,
            IOException {
//            log.info("Post");
            response.setContentType("application/octet-stream");
            response.setStatus(HttpServletResponse.SC_OK);
            try {
                Packet[] packets = decoder.decode(request.getInputStream());
                handle(packets,response.getOutputStream(),request);
//                log.info("Post Ok");
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        protected void doGet(HttpServletRequest request,
                              HttpServletResponse response) throws
            ServletException,
            IOException {
            response.setContentType("application/octet-stream");
            response.setStatus(HttpServletResponse.SC_OK);
//            response.getWriter().print("ok");
        }
    }


    private void handle(Packet[] packet, ServletOutputStream out,
                        HttpServletRequest request) throws Exception {
        HttpSession httpSession = null;
        for (int i = 0; i < packet.length; i++) {
            httpSession = handle(packet[i], out, request);
        }
        if (httpSession!= null) {

            ByteBuffer[] segments = httpSession.getSegments();
            if(segments==null||segments.length==0){
                try {
                    synchronized (httpSession) {
                        httpSession.wait(1000L);
                    }
                } catch (InterruptedException ex1) {
//                    System.out.println("notified");
                }
                segments = httpSession.getSegments();
            }
            if (segments == null || segments.length == 0) {

                segments = new ByteBuffer[1];
                UWAPSegment seg = new UWAPSegment((byte)92, -1,httpSession.getSessionId());
                segments[0] = ByteBuffer.wrap(seg.getPacketByteArray());
            }
            try {
                encoder.encode(out, segments);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private HttpSession handle(Packet packet, ServletOutputStream out,
                               HttpServletRequest request) throws Exception {
        if (packet.sessionId == -1) {
            InetSocketAddress address = new InetSocketAddress(request.
                getRemoteAddr(), request.getRemotePort());
            HttpSession session = new HttpSession(HttpAcceptor.this, address,
                                                  ids.incrementAndGet());
            sessions.put(session.getSessionId(), session);
            handler.sessionCreated(session);
            handler.messageReceived(session,packet.buffer);
            return session;
        }
        else {
            HttpSession session = sessions.get(packet.sessionId);
            if (session != null) {
                session.setLastReadTime(System.currentTimeMillis());
                handler.messageReceived(session, packet.buffer);
            }
            return session;
        }
    }

    public HttpSession getSession(int sessionId){
        return sessions.get(sessionId);
    }

    public void closeSession(int sessionId){
        HttpSession session = sessions.get(sessionId);
        if(session!=null){
            session.close();
        }
    }

    public void broadcast(ByteBuffer buffer){
        for(IoSession session:sessions.values()){
            session.write(buffer);
        }
    }

    public void stop(){
        try {
            server.stop();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
//        HandleResult ret = new HandleResult();
//        if(packet.datas.length>0){
//            if(packet.datas[0].getSessionId()==-1){
//                InetSocketAddress address = new InetSocketAddress(request.getRemoteAddr(),request.getRemotePort());
//                HttpSession session = new HttpSession(HttpAcceptor.this,address);
//                ret.session = handler.sessionCreated2(session);
//                synchronized(sessions){
//                    sessions.add(session);
//                }
//            }else{
//                ret.session = (Session)registry.getSession(packet.datas[0].getSessionId());
//            }
//            if(ret.session!=null){
//                ((HttpSession)ret.session.getIoSession()).setLastReadTime(System.currentTimeMillis());
//                ret.session.handle(packet);
//                ret.setIdleTime(IDLE_TIME[packet.datas[0].getAppType()&0xFF]);
//            }
//        return ret;
//        return ret;
//    }

    public void run(){
        while(true){
            try {
                Thread.sleep(60 * 1000L);
            } catch (InterruptedException ex) {
            }
            try {
                checkIdle();
            } catch (Throwable ex1) {
                ex1.printStackTrace();
            }
        }
    }

    private void checkIdle(){
        synchronized(sessions){
            Iterator ite = sessions.values().iterator();
            long currTime = System.currentTimeMillis();
            while (ite.hasNext()) {
                HttpSession session = (HttpSession)ite.next();
                if((currTime-session.getLastReadTime())>300000L){
                    session.close();
                }
            }
        }
    }

    private class HandleResult{
        HttpSession session;
        int idleTime;

        public void setIdleTime(int time){
            if(idleTime<time)
                idleTime = time;
        }
    }

}
