package com.pip.itimes.server.connect;

import java.io.File;
import java.net.InetSocketAddress;

import com.pip.itimes.net.*;
import com.pip.itimes.server.dao.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.mina.common.*;
import org.apache.mina.transport.socket.nio.*;
import org.apache.mina.util.NewThreadExecutor;


public class Server{

    private static final Logger log = Logger.getLogger(Server.class);

    private BbsService bbsService = null;

    private AuthSession authSession = null;

    private WorldSession worldSession = null;

    private PlayerService playerService = null;

    private StageService stageService = null;

    private ClientService clientService = null;

    private ChatService chatService = null;

    private MailService mailService = null;

//    private BufService bufService = null;

    private AuctionService auctionService = null;

    private BuyService buyService = null;

    private OemService oemService = null;

    private ShopService shopService = null;

    private VersionService versionService = null;

    private TrustIpService trustIpService = null;

    private Configuration configuration = null;

    private IpdService ipdService = null;

    public volatile static  boolean  isMaintance = true;

    SessionRegistry registry = new SessionRegistry();

    public ClientSessionFactory clientSessionFactory = null;

    public Server() throws Exception {
        super();

    }


    public void launch() throws Exception {
        Runtime.getRuntime().addShutdownHook(new ShutDownHook());
        configuration = new PropertiesConfiguration(
                "config.properties");
        String s = System.getProperty("startup");
        if(s!=null){
            isMaintance = false;
        }
        clientSessionFactory = ClientSessionFactory.getFactory(configuration.getString("clientsessionfactory"));
        if(clientSessionFactory==null)
            throw new Exception("Init ClientSessionFactory Error");
        trustIpService = new TrustIpService();
        bbsService = new BbsService(new BbsDao());
        stageService = new StageService(new File(configuration.getString("datadir")),bbsService);
        clientService = new ClientService();
        clientService.setMaxPlayer(configuration.getInt("maxplayer"));
        stageService.setClientService(clientService);
        playerService = new PlayerService(new PlayerDao());
        mailService = new MailService(new MailDao(), playerService);
        chatService = new ChatService();
        chatService.setStageService(stageService);
        chatService.init();
        auctionService = new AuctionService(new AuctionDao());
        buyService = new BuyService(new BuyDao());
        oemService = new OemService(new OemDao());
        shopService = new ShopService(new ShopDao());
        versionService = new VersionService();
        playerService.start();
        ipdService = new IpdService(configuration.getString("servertype"));
        ipdService.setClientService(clientService);
        ipdService.setConfiguration(configuration);
        ipdService.start();
        if(!configuration.getBoolean("poolbuffer")){
            ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
        }
        if(configuration.getBoolean("directbuffer")){
            ByteBuffer.setUseDirectBuffers(true);
        }else{
            ByteBuffer.setUseDirectBuffers(false);
        }

        WorldSessionHandler worldSessionHandler = new WorldSessionHandler(registry);
        connectWorld(worldSessionHandler);

        AuthSessionHandler authSessionHandler = new AuthSessionHandler(registry);

        connectAuth(authSessionHandler);

        ClientSessionHandler clientSessionHandler = new ClientSessionHandler(registry);

        if("http".equals(configuration.getString("servertype"))){
            bindHttp(clientSessionHandler);
        }
        else{
            bind(clientSessionHandler);
        }

    }

    private void connectWorld(WorldSessionHandler handler) throws Exception{
        IoConnector connector = new UWAPConnector(2,new NewThreadExecutor());
        SocketConnectorConfig sconfig = (SocketConnectorConfig)connector.getDefaultConfig();
        SocketSessionConfig sc = (SocketSessionConfig)sconfig.getSessionConfig();
        sc.setReceiveBufferSize(configuration.getInt("worldreceivebuffsize"));
        sc.setSendBufferSize(configuration.getInt("worldwritebuffsize"));
        sc.setTcpNoDelay(configuration.getBoolean("tcpnodelay"));
//        handler.setConnector(connector);
        connector.connect(new InetSocketAddress(configuration.getString("worldip"), configuration.getInt("worldport")),handler,sconfig);
    }

    private void connectAuth(AuthSessionHandler handler) throws Exception{
        IoConnector connector = new UWAPConnector(2,new NewThreadExecutor());
        SocketConnectorConfig sconfig = (SocketConnectorConfig)connector.getDefaultConfig();
        SocketSessionConfig sc = (SocketSessionConfig)sconfig.getSessionConfig();
        sc.setReceiveBufferSize(configuration.getInt("authreceivebuffsize"));
        sc.setSendBufferSize(configuration.getInt("authwritebuffsize"));
        sc.setTcpNoDelay(configuration.getBoolean("tcpnodelay"));
//        handler.setConnector(connector);
        connector.connect(new InetSocketAddress(configuration.getString("authip"), configuration.getInt("authport")),handler,sconfig);
    }

    private void bind(ClientSessionHandler handler) throws Exception{
        IoAcceptor acceptor = new UWAPAcceptor(configuration.getInt("clientacceptornum"),new NewThreadExecutor());
        SocketAcceptorConfig sconfig = (SocketAcceptorConfig) acceptor.getDefaultConfig();
        SocketSessionConfig sc = (SocketSessionConfig) sconfig.getSessionConfig();
        sc.setReceiveBufferSize(configuration.getInt("clientreceivebuffsize"));
        sc.setSendBufferSize(configuration.getInt("clientwritebuffsize"));
        sc.setTcpNoDelay(true);
//        sc.setTcpNoDelay(configuration.getBoolean("tcpnodelay"));
        acceptor.bind(new InetSocketAddress(configuration.getString("localip"),configuration.getInt("port")),handler,sconfig);
        clientService.setAcceptor(acceptor);
    }

    private void bindHttp(ClientSessionHandler handler) throws Exception{
        HttpAcceptor acceptor = new HttpAcceptor();
        acceptor.bind(configuration.getString("localip"),configuration.getInt("port"),handler);
    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.launch();
        } catch (Exception ex) {
            log.error(ex, ex);
        }
    }


    class ClientSessionHandler extends SessionHandler {
        public ClientSessionHandler(SessionRegistry registry) {
            super(registry);
        }

        public Session createSession(IoSession session) {
            InetSocketAddress address = (InetSocketAddress)session.getRemoteAddress();
            if(!trustIpService.isTrustIp(address)){
                session.close();
                return null;
            }
            ClientSession ret = clientSessionFactory.createSession(session);
//            ret.setStageService(stageService);
            ret.setClientService(clientService);
            ret.setBbsService(bbsService);
            ret.setWorldSession(worldSession);
            ret.setAuthSession(authSession);
            ret.setPlayerService(playerService);
            ret.setChatService(chatService);
            ret.setMailService(mailService);
            ret.setAuctionService(auctionService);
            ret.setBuyService(buyService);
            ret.setOemService(oemService);
//            ret.setShopService(shopService);
            ret.setVersionService(versionService);
            ret.setStageService(stageService);
//            ret.setTrustIpService(trustIpService);
            return ret;
        }
    }

    class WorldSessionHandler extends SessionHandler{
        public WorldSessionHandler(SessionRegistry registry){
            super(registry);
        }

        public Session createSession(IoSession session){
            worldSession = new WorldSession(session);
            worldSession.setClientRegistry(registry);
            worldSession.setStageService(stageService);
            worldSession.setClientService(clientService);
            worldSession.setPlayerService(playerService);
            worldSession.setChatService(chatService);
            worldSession.setConfiguration(configuration);
            worldSession.setTrustIpService(trustIpService);
            worldSession.setVersionService(versionService);
            return worldSession;
        }
    }

    class AuthSessionHandler extends SessionHandler{
        public AuthSessionHandler(SessionRegistry registry){
            super(registry);
        }

        public Session createSession(IoSession session){
            authSession = new AuthSession(session);
            authSession.setClientRegistry(registry);
            authSession.setConfiguration(configuration);
            authSession.setClientService(clientService);
            return authSession;
        }
    }
}
