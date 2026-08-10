package com.pip.dispatch;

import java.net.*;

import org.apache.commons.configuration.*;
import org.apache.mina.transport.socket.nio.*;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.log4j.Logger;
import java.io.File;

public class Main {

    private Configuration configuration = null;
    private Dispatcher dispatcher = null;
    private ChannelService channelService = null;
    
    private ChatService chatService = null;
    
    private TimeControlProcessor controlProcessor = null;
    private TrustIpService trustIpService = null;
    private IpdService ipdService = null;
    private ProxyManagingService proxyService = null;
    private static final Logger log = Logger.getLogger(Main.class);

    public static void main(String[] args) throws Throwable{
        Main main = new Main();
        main.launch();
    }

    private void launch() throws Exception {
        configuration = new PropertiesConfiguration("config.properties");
        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
        channelService = new ChannelService();
        controlProcessor = new TimeControlProcessor();
        trustIpService = new TrustIpService(new File(System.
                getProperty("user.dir") + "/trustip.txt"));
        controlProcessor.setChannelService(channelService);
        chatService = new ChatService();
        controlProcessor.setChatService(chatService);
        
        String serverType = configuration.getString("servertype");
        ipdService = new IpdService(serverType,configuration.getStringArray("ipd"),configuration);
        controlProcessor.setIpdService(ipdService);
        if ("socket".equals(serverType)) {
            dispatcher = new SocketDispatcher(controlProcessor,configuration);
            ((SocketDispatcher)dispatcher).setChannelService(channelService);
            ((SocketDispatcher)dispatcher).setTrustIpService(trustIpService);
            ((SocketDispatcher)dispatcher).setChatService(chatService);
            controlProcessor.setDispatcher(dispatcher);
            SocketAcceptorConfig sac = new SocketAcceptorConfig();
            sac.setDisconnectOnUnbind(true);
            SocketSessionConfig sc = (SocketSessionConfig)sac.getSessionConfig();
            sc.setReceiveBufferSize(configuration.getInt("clientreceivebuffsize"));
            sc.setSendBufferSize(configuration.getInt("clientwritebuffsize"));
            ( (SocketDispatcher) dispatcher).bind(new InetSocketAddress(
                configuration.getString("localip"),
                configuration.getInt("port")),
                                                  sac);
            log.info("binded");
            SocketConnectorConfig scc = new SocketConnectorConfig();
            SocketSessionConfig sc1 = (SocketSessionConfig)scc.getSessionConfig();
            sc1.setReceiveBufferSize(configuration.getInt("worldreceivebuffsize"));
            sc1.setSendBufferSize(configuration.getInt("worldwritebuffsize"));
            ConnectFuture future = ((SocketDispatcher)dispatcher).connect(new InetSocketAddress(
                configuration.getString("worldip"),
                configuration.getInt("worldport")), scc);
            future.join();
            log.info("Socket Dispatch Started");
        }
        else if("http".equals(serverType)){
            dispatcher = new HttpDispatcher(controlProcessor,configuration);
            ((HttpDispatcher)dispatcher).setChannelService(channelService);
            ((HttpDispatcher)dispatcher).setTrustIpService(trustIpService);
            ((HttpDispatcher)dispatcher).setChatService(chatService);
            controlProcessor.setDispatcher(dispatcher);
            ((HttpDispatcher)dispatcher).bind(configuration.getString("localip"),
                                                  configuration.getInt("port"));
            log.info("binded");
            ConnectFuture future = ((HttpDispatcher)dispatcher).connect(new InetSocketAddress(
                configuration.getString("worldip"),
                configuration.getInt("worldport")), new SocketConnectorConfig());
            future.join();
            log.info("Http Dispatch Started");
        }
        else if ("singlesocket".equals(serverType)) {
        	proxyService = new ProxyManagingService(configuration);
        	ipdService.setProxyService(proxyService);
            dispatcher = new SingleSocketDispatcher(controlProcessor, configuration, proxyService);
            ( (SingleSocketDispatcher) dispatcher).setChannelService(channelService);
            ( (SingleSocketDispatcher) dispatcher).setTrustIpService(trustIpService);
            ( (SingleSocketDispatcher) dispatcher).setChatService(chatService);
            controlProcessor.setDispatcher(dispatcher);
            ( (SingleSocketDispatcher) dispatcher).bind(new InetSocketAddress(
                configuration.getString("localip"),
                configuration.getInt("port")),
                new SocketAcceptorConfig());
            log.info("binded");
            ConnectFuture future = ( (SingleSocketDispatcher) dispatcher).connect(new
                InetSocketAddress(
                    configuration.getString("worldip"),
                    configuration.getInt("worldport")), new SocketConnectorConfig());
            future.join();
            log.info("SingleSocket Dispatch Started");
        }
        else
            throw new RuntimeException("Unknow Server Type");
    }
}
