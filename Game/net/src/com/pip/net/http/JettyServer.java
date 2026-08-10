package com.pip.net.http;

import javax.servlet.http.HttpServlet;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.BoundedThreadPool;
import org.mortbay.thread.QueuedThreadPool;

public class JettyServer {
    private Server server = null;
    private Context root = null;

    public JettyServer(String host,int port,int minThread,int maxThread) {
        server = new Server();
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(minThread);
        threadPool.setMaxThreads(maxThread);
        server.setThreadPool(threadPool);
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort (port);
        connector.setHost (host);
        connector.setMaxIdleTime(3000);
        root = new Context(server, "/", Context.SESSIONS);
        server.addConnector(connector);
    }


    public void addServlet(String url,HttpServlet servlet){
        root.addServlet(new ServletHolder(servlet), url);
    }

    public void start() throws Exception{
        server.start();
    }
}
