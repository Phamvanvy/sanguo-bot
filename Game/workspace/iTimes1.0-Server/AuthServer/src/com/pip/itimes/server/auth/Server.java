package com.pip.itimes.server.auth;


import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.pip.itimes.net.*;
import com.pip.itimes.server.dao.AccountDao;
import com.pip.itimes.server.dao.FeeDao;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.mina.common.*;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;


public class Server{

    private static final Logger log = Logger.getLogger(Server.class);

    private AccountService accountService = null;
    private AccountDao accountDao = null;
//    private FreeUserService freeUserService = null;
//    private FreeUserDao freeUserDao = null;
    private Configuration configuration = null;
    private ConnectService connectService = null;
    private FeeService feeService = null;

    private ConnectSessionFactory sessionFactory;

    private JettyServer jettyServer = null;

    public Server() {
        super();
        accountDao = new AccountDao();
//        freeUserDao = new FreeUserDao();
        accountService = new AccountService(accountDao);
//        freeUserService = new FreeUserService(freeUserDao);
        connectService = new ConnectService();
    }

    public void launch() throws Exception {
        configuration = new PropertiesConfiguration("config.properties");
        jettyServer = new JettyServer(configuration.getString("feehttpurl"),
                                       configuration.getInt("feehttpport"), 3, 50);
        feeService = new FeeService(new FeeDao(), configuration,accountService,jettyServer);
        feeService.setConnectService(connectService);
        if (!configuration.getBoolean("poolbuffer")) {
            ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
        }
        if (configuration.getBoolean("directbuffer")) {
            ByteBuffer.setUseDirectBuffers(true);
        } else {
            ByteBuffer.setUseDirectBuffers(false);
        }
        sessionFactory = ConnectSessionFactory.getFactory(configuration.getString("connectsessionfactory"),configuration);
        if(sessionFactory==null)
            throw new Exception("Init ConnectSessionFactory Error");
        SessionRegistry registry = new SessionRegistry();
        SessionHandler connectSessionHandler = new ConnectSessionHandler(registry);
        bind(registry,connectSessionHandler);
        if("cmcc".equals(configuration.getString("connectsessionfactory"))){
            jettyServer.addServlet("/UserNotify",new CmccUserNotifyServlet(((CmccConnectSessionFactory)sessionFactory).getUserCache()));
        }
        jettyServer.addServlet("/reg",new AccountServlet(accountService));
        jettyServer.addServlet("/VerifyAccount",new VerifyAccountServlet(accountService));
        jettyServer.addServlet("/ModifyPassword",new ModifyPasswordServlet(accountService));
        jettyServer.start();
//        SessionHandler worldSessionHandler = new WorldSessionHandler(registry);
//        bindWorld(registry,worldSessionHandler);
    }

    private void bind(SessionRegistry registry,SessionHandler sessionHandler) throws Exception{
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
        IoAcceptor acceptor = new UWAPAcceptor(2,Executors.newCachedThreadPool());
        SocketAcceptorConfig sconfig = (SocketAcceptorConfig) acceptor.getDefaultConfig();
        sconfig.setThreadModel(ThreadModel.MANUAL);
        SocketSessionConfig sc = (SocketSessionConfig) sconfig.getSessionConfig();
        sc.setReceiveBufferSize(configuration.getInt("receivebuffsize"));
        sc.setSendBufferSize(configuration.getInt("writebuffsize"));
        sc.setTcpNoDelay(configuration.getBoolean("tcpnodelay"));
        acceptor.bind(new InetSocketAddress(configuration.getString("localip"),configuration.getInt("port")),sessionHandler,sconfig);
        accountService.setAcceptor(acceptor);
    }

//    private void bindWorld(SessionRegistry registry,SessionHandler sessionHandler) throws Exception{
//        IoAcceptor acceptor = new UWAPAcceptor(2,new NewThreadExecutor());
//       SocketAcceptorConfig sconfig = (SocketAcceptorConfig) acceptor.getDefaultConfig();
//       SocketSessionConfig sc = (SocketSessionConfig) sconfig.getSessionConfig();
//       sc.setReceiveBufferSize(configuration.getInt("receivebuffsize"));
//       sc.setSendBufferSize(configuration.getInt("writebuffsize"));
//       sc.setTcpNoDelay(configuration.getBoolean("tcpnodelay"));
//       acceptor.bind(new InetSocketAddress(configuration.getString("localip"),configuration.getInt("port1")),sessionHandler,sconfig);
//
//    }


    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.launch();
            log.info("AuthServer started");
//            server.createAccounts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void createAccounts(){
//        for(int i=0;i<20000;i++){
//            accountService.createNewAccount("test"+i,"2008","","","",0,"",true,"",0,0,"","");
//            System.out.println(i+"created");
//        }
//        System.out.println("account created");
//    }

    class ConnectSessionHandler extends SessionHandler {

        public Session createSession(IoSession session) {
            ConnectSession ret = sessionFactory.createSession(session);
            ret.setConnectService(connectService);
            ret.setConfiguration(configuration);
            ret.setAccountService(accountService);
            ret.setFeeService(feeService);
            return ret;
        }

        public ConnectSessionHandler(SessionRegistry registry) {
            super(registry);
        }

    }

    class WorldSessionHandler extends SessionHandler{
        public Session createSession(IoSession session){
            WorldSession ret = new WorldSession(session);
            ret.setAccountService(accountService);
            return ret;
        }

        public WorldSessionHandler(SessionRegistry registry){
            super(registry);
        }
    }
}
