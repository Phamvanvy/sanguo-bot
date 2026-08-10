package com.pip.net.http;

import javax.servlet.http.HttpServlet;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.BoundedThreadPool;

public class JettyServer {
    private Server server = null;
    private Context root = null;

    public JettyServer(String host,int port,int minThread,int maxThread) {
        server = new Server();
        BoundedThreadPool threadPool = new BoundedThreadPool();
        threadPool.setMinThreads(minThread);
        threadPool.setMaxThreads(maxThread);
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort (port);
        connector.setHost (host);
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
