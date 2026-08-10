package com.pip.server.auth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.pip.server.auth.cmcc.CmccBackdoorServlet;
import com.pip.server.auth.cmcc.CmccConnectSessionFactory;
import com.pip.server.auth.cmcc.CmccRecommendNotifyServlet;
import com.pip.server.auth.cmcc.CmccSmsBuyNotifyServlet;
import com.pip.server.auth.cmcc.CmccSubscribeNotifyServlet;
import com.pip.server.auth.cmcc.CmccUserNotifyServlet;
import com.pip.server.auth.cmcc.billing.BillingService;
import com.pip.server.auth.cmcc.billing.CmccBillingServlet;
import com.pip.server.auth.dao.AccountDao;
import com.pip.server.auth.dao.FeeDao;
import com.pip.server.auth.net.*;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.mina.common.*;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;

/**
 * 服务器启动类。
 */
public class Server {
    private static final Logger log = Logger.getLogger(Server.class);

    private AccountService accountService = null;
    private AccountDao accountDao = null;
    private Configuration configuration = null;
    private ConnectService connectService = null;
    private FeeService feeService = null;
    private BillingService billingService = null;

    private ConnectSessionFactory sessionFactory;

    private JettyServer jettyServer = null;
    public static Server instance;

    public Server() {
        super();
        instance = this;
        accountDao = new AccountDao();
        accountService = new AccountService(accountDao);
        connectService = new ConnectService();
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }

    public void launch() throws Exception {
        configuration = new PropertiesConfiguration("config.properties");
        jettyServer = new JettyServer(configuration.getString("feehttpurl"), configuration.getInt("feehttpport"), 3, 50);
        feeService = new FeeService(new FeeDao(), configuration, accountService, jettyServer);
        feeService.setConnectService(connectService);
        if (!configuration.getBoolean("poolbuffer")) {
            ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
        }
        if (configuration.getBoolean("directbuffer")) {
            ByteBuffer.setUseDirectBuffers(true);
        } else {
            ByteBuffer.setUseDirectBuffers(false);
        }
        sessionFactory = ConnectSessionFactory.getFactory(configuration.getString("connectsessionfactory"),
                configuration);
        if (sessionFactory == null)
            throw new Exception("Init ConnectSessionFactory Error");
        SessionRegistry registry = new SessionRegistry();
        SessionHandler connectSessionHandler = new ConnectSessionHandler(registry);
        bind(registry, connectSessionHandler);
        if ("cmcc".equals(configuration.getString("connectsessionfactory"))) {
            CmccConnectSessionFactory factory = (CmccConnectSessionFactory)sessionFactory;
            String furl = configuration.getString("fowardnotifyurl");
            jettyServer.addServlet("/UserNotify", new CmccUserNotifyServlet(factory.getUserCache(), furl));
            jettyServer.addServlet("/SMSBuyNotify", new CmccSmsBuyNotifyServlet(
                    factory.getUserService(), factory.getUserCache(), accountService, feeService));
            jettyServer.addServlet("/RecommendNotify", new CmccRecommendNotifyServlet());
            jettyServer.addServlet("/SubscribeNotify", new CmccSubscribeNotifyServlet(connectService));
            jettyServer.addServlet("/backdoor", new CmccBackdoorServlet(connectService));

            // 添加代计费服务
            billingService = new BillingService(connectService, factory.getUserService(), factory.getUserCache(), feeService);
            jettyServer.addServlet("/charge", new CmccBillingServlet(billingService));
            
            // 测试发送短信
            // testSendSMS();
        }
        jettyServer.start();
        
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }

    private void bind(SessionRegistry registry, SessionHandler sessionHandler) throws Exception {
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
        IoAcceptor acceptor = new UWAPAcceptor(2, Executors.newCachedThreadPool());
        SocketAcceptorConfig sconfig = (SocketAcceptorConfig) acceptor.getDefaultConfig();
        sconfig.setThreadModel(ThreadModel.MANUAL);
        SocketSessionConfig sc = (SocketSessionConfig) sconfig.getSessionConfig();
        sc.setReceiveBufferSize(configuration.getInt("receivebuffsize"));
        sc.setSendBufferSize(configuration.getInt("writebuffsize"));
        sc.setTcpNoDelay(configuration.getBoolean("tcpnodelay"));
        acceptor.bind(new InetSocketAddress(configuration.getString("localip"), configuration.getInt("port")),
                sessionHandler, sconfig);
        accountService.setAcceptor(acceptor);
    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.launch();
            log.info("AuthServer started");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
    
    class ShutdownHook extends Thread{
        public void run(){
            sessionFactory.shutdown();
        }
    }
    
    private void testSendSMS() {
    	File listFile = new File("smslist.txt");
    	if (listFile.exists()) {
    		try {
    			FileInputStream fis = new FileInputStream(listFile);
    			BufferedReader br = new BufferedReader(new InputStreamReader(fis, "GBK"));
    			int lineCount = Integer.parseInt(br.readLine().trim());
    			String[] msgs = new String[lineCount];
    			for (int i = 0; i < lineCount; i++) {
    				msgs[i] = br.readLine().trim();
    			}
    			log.info("发送消息：");
    			for (String msg : msgs) {
    				log.info(msg);
    			}
    			String line;
    			CmccConnectSessionFactory fac = (CmccConnectSessionFactory)sessionFactory;
    			while ((line = br.readLine()) != null) {
    				line = line.trim();
    				if (line.length() == 0) {
    					continue;
    				}
    				try {
    					for (String msg : msgs) {
    						fac.getUserService().sendRewardMessage(line, msg);
    					}
    					log.info(line + "...OK");
    				} catch (Exception e) {
    					log.info(line + "..." + e.toString());
    				}
    			}
    			fis.close();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		System.exit(0);
    	}
    }
}
