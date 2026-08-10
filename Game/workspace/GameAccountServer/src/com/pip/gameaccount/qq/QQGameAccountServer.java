package com.pip.gameaccount.qq;

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import com.pip.gameaccount.ClientListManager;
import com.pip.gameaccount.GameAccountServer;
import com.pip.gameaccount.ISessionService;
import com.pip.gameaccount.SessionService;
import com.pip.gameaccount.WorldStub;
import com.pip.gameaccount.handler.client.ServerLoginHandler;
import com.pip.net.DemuxingMessageHandler;
import com.pip.net.IMessageHandler;
import com.pip.net.http.JettyServer;
import com.pip.net.message.ServerLoginMessage;
import com.pip.net.message.gameaccount.AccountInfoMessage;
import com.pip.net.message.gameaccount.ChangeStatusMessage;
import com.pip.net.message.gameaccount.LegacyBuy1Message;
import com.pip.net.message.gameaccount.LegacyLoginMessage;
import com.pip.net.message.gameaccount.Logout1Message;
import com.pip.net.message.gameaccount.QQBillingMessage;

public class QQGameAccountServer {
	private final Configuration configuration;
	private WorldStub worldStub;
	private QQStub qqStub;
	private ClientListManager clientListManager;
	private QQGameAccountService gameAccountService;
	private QQLoginService loginService;
	private ISessionService sessionService;
	private QQBillingService qqBillingService;
	private SuperQQService superQQService;
	private FeeDAO feeDao;
	private JettyServer jettyServer;

	private static final Logger log = Logger.getLogger(GameAccountServer.class);

	public QQGameAccountServer(Configuration configuration) {
		this.configuration = configuration;
	}

	public void launch() throws Exception {
		superQQService = new SuperQQService();
		gameAccountService = new QQGameAccountService(new QQGameAccountDAO());
		clientListManager = new ClientListManager(new File(System
				.getProperty("user.dir")
				+ "/clients.txt"));
		loginService = new QQLoginService(gameAccountService);
		sessionService = new SessionService();
		feeDao = new FeeDAO();
		qqBillingService = new QQBillingService(feeDao, configuration.getString("dbdir"));
		worldStub = new WorldStub(configuration, sessionService);

		qqStub = new QQStub(configuration.getString("serverip"), configuration
				.getInt("qqport"), "919101307282901971910015");

		worldStub.setMessageHandler(getWorldStubMessageHandler());
		qqStub.setMessageHandler(getQQStubMessageHandler());
		worldStub.start();
		qqStub.start();
		log.info("Server Binded.");
		jettyServer = new JettyServer("localhost",7200,1,5);
		jettyServer.addServlet("/backdoor", new ModifyAccountServlet(loginService, superQQService, feeDao));
		jettyServer.start();
		log.info("JettyServerStarted.");
		
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
	}

	public IMessageHandler getWorldStubMessageHandler() {
		DemuxingMessageHandler handler = new DemuxingMessageHandler();
		handler.addHandler(ServerLoginMessage.class, new ServerLoginHandler(
				clientListManager, sessionService));
		handler.addHandler(LegacyLoginMessage.class, new LegacyLoginHandler(
				loginService,superQQService));
		handler.addHandler(LegacyBuy1Message.class, new LegacyBuy1Handler(
				loginService));
		handler.addHandler(QQBillingMessage.class, new QQBillingHandler(qqBillingService));
		handler.addHandler(Logout1Message.class, new LogoutHandler());
		handler.addHandler(AccountInfoMessage.class, new AccountInfoHandler(loginService));
		handler.addHandler(ChangeStatusMessage.class, new ChangeStatusHandler(loginService));
		return handler;
	}

	public IMessageHandler getQQStubMessageHandler() {
		DemuxingMessageHandler handler = new DemuxingMessageHandler();
		handler.addHandler(QQBuyMessage.class,
						new QQBuyOkHandler(loginService,qqBillingService,sessionService));
		handler.addHandler(QQBuy2Message.class,
				new QQBuy2OkHandler(loginService,qqBillingService,sessionService));
		handler.addHandler(QQLoginMessage.class, new QQLoginHandler(
				loginService, superQQService, sessionService));
		handler.addHandler(QQLogin2Message.class, new QQLogin2Handler(
				loginService, superQQService, sessionService));
		return handler;
	}

	public static void main(String[] args) throws Exception {
		QQGameAccountServer server = new QQGameAccountServer(
				new PropertiesConfiguration("config.properties"));
		server.launch();
	}
	
	class ShutdownHook extends Thread{
        public void run(){
            qqBillingService.close();
        }
    }
}
