package com.pip.itimes.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.*;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.BoundedThreadPool;
import org.apache.log4j.Logger;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class HttpAcceptor implements Runnable{

    private static final Logger log = Logger.getLogger(HttpAcceptor.class);


    private static final int[] IDLE_TIME = {0,0,0,0,1000,0,4000,0,2000,0,
    2000,0,0,2000,0,2000,2000,0,0,0,
    0,0,2000,2000,2000,2000,2000,0,0,0,
    0,0,0,2000,2000,2000,0,2000,0,0,

    0,0,0,2000,2000,2000,2000,0,0,0,
    2000,0,0,0,2000,0,0,0,0,2000,
    0,0,0,0,0,2000,0,0,0,2000,
    0,2000,0,0,0,0,2000,0,0,0,

    2000,0,2000,0,0,2000,2000,2000,2000,0,
    0,0,0,0,2000,0,2000,2000,0,0,
    2000,0,2000,0,0,2000,0,2000,0,0,

    0,2000,2000,2000,2000,2000,0,2000,2000,2000,
    2000,0,2000,2000,0,2000,2000,0,0,0,
    2000,2000,0,0,2000,2000,2000,2000,0,2000,
    0,2000,2000,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,};

    private int port = 80;
    private SessionHandler handler;
    private SessionRegistry registry;
    private HttpUWAPDecoder decoder = new HttpUWAPDecoder();
    private HttpUWAPEncoder encoder = new HttpUWAPEncoder();
    private Set sessions = new HashSet();

    public HttpAcceptor() {
    }

    public void bind(String address,int port, SessionHandler handler) throws Exception {
        this.port = port;
        this.handler = handler;
        this.registry = handler.registry;
        Server server = new Server();
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
                sessions.remove(session);
            }
            handler.sessionClosed(session);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    class HttpUWAPServlet extends HttpServlet {
        protected void doPost(HttpServletRequest request,
                              HttpServletResponse response) throws
            ServletException,
            IOException {
            log.info("Post");
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
        HandleResult s = null;
        for (int i = 0; i < packet.length; i++) {
            s = handle(packet[i], out, request);
        }
        if (s.session != null) {
            HttpSession httpSession = (HttpSession) s.session.getIoSession();
            if(s.idleTime>0){
                try {
                    synchronized (httpSession) {
                        httpSession.wait(s.idleTime);
                    }
                } catch (InterruptedException ex1) {
//                    System.out.println("notified");
                }
            }
            UWAPSegment[] segments = httpSession.getSegments();
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
                segments = new UWAPSegment[1];
                UWAPSegment seg = new UWAPSegment(ClientConstants.NOP, -1,
                                                  s.session.getSessionId());
                segments[0] = seg;
            }
            try {
                encoder.encode(out, segments);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private HandleResult handle(Packet packet,ServletOutputStream out,HttpServletRequest request) throws Exception{
        HandleResult ret = new HandleResult();
        if(packet.datas.length>0){
            if(packet.datas[0].getSessionId()==-1){
                InetSocketAddress address = new InetSocketAddress(request.getRemoteAddr(),request.getRemotePort());
                HttpSession session = new HttpSession(HttpAcceptor.this,address);
                ret.session = handler.sessionCreated2(session);
                synchronized(sessions){
                    sessions.add(session);
                }
            }else{
                ret.session = (Session)registry.getSession(packet.datas[0].getSessionId());
            }
            if(ret.session!=null){
                ((HttpSession)ret.session.getIoSession()).setLastReadTime(System.currentTimeMillis());
                ret.session.handle(packet);
                ret.setIdleTime(IDLE_TIME[packet.datas[0].getAppType()&0xFF]);
            }
        }
        return ret;

    }

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
            Iterator ite = sessions.iterator();
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
        Session session;
        int idleTime;

        public void setIdleTime(int time){
            if(idleTime<time)
                idleTime = time;
        }
    }

}
