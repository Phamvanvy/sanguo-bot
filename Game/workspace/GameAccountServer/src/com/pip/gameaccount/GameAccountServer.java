package com.pip.gameaccount;

import java.io.File;
import java.net.InetSocketAddress;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import com.pip.gameaccount.dao.GameAccountDAO;
import com.pip.gameaccount.handler.client.AccountInfoHandler;
import com.pip.gameaccount.handler.client.AccountRegHandler;
import com.pip.gameaccount.handler.client.AddBalanceHandler;
import com.pip.gameaccount.handler.client.AddRecommendBalanceHandler;
import com.pip.gameaccount.handler.client.ChangeStatusHandler;
import com.pip.gameaccount.handler.client.CreateIMoneyCardHandler;
import com.pip.gameaccount.handler.client.GetAccountNameHandler;
import com.pip.gameaccount.handler.client.LegacyBuy1Handler;
import com.pip.gameaccount.handler.client.LegacyBuyHandler;
import com.pip.gameaccount.handler.client.LegacyFee1Handler;
import com.pip.gameaccount.handler.client.LegacyFeeHandler;
import com.pip.gameaccount.handler.client.LegacyLoginHandler;
import com.pip.gameaccount.handler.client.LegacyQuickRegHandler;
import com.pip.gameaccount.handler.client.LevelUpNotifyHandler;
import com.pip.gameaccount.handler.client.Logout1Handler;
import com.pip.gameaccount.handler.client.ModifyPasswordHandler;
import com.pip.gameaccount.handler.client.ModifyPhoneHandler;
import com.pip.gameaccount.handler.client.OnlineTimeNotifyHandler;
import com.pip.gameaccount.handler.client.PhoneNotifyHandler;
import com.pip.gameaccount.handler.client.RecommendRequestHandler;
import com.pip.gameaccount.handler.client.RenameHandler;
import com.pip.gameaccount.handler.client.ServerLoginHandler;
import com.pip.gameaccount.handler.client.UseIMoneyCardHandler;
import com.pip.gameaccount.handler.server.AccountInfoOkHandler;
import com.pip.gameaccount.handler.server.AccountRegOkHandler;
import com.pip.gameaccount.handler.server.AddBalanceOkHandler;
import com.pip.gameaccount.handler.server.AddRecommendBalanceOkHandler;
import com.pip.gameaccount.handler.server.CreateIMoneyCardOkHandler;
import com.pip.gameaccount.handler.server.CreditChangeNotifyHandler;
import com.pip.gameaccount.handler.server.ErrorHandler;
import com.pip.gameaccount.handler.server.ModifyPasswordOkHandler;
import com.pip.gameaccount.handler.server.ModifyPhoneOkHandler;
import com.pip.gameaccount.handler.server.RecommendRewardNotifyHandler;
import com.pip.gameaccount.handler.server.RenameOkHandler;
import com.pip.gameaccount.handler.server.SyncBalanceHandler;
import com.pip.gameaccount.handler.server.UseIMoneyCardOkHandler;
import com.pip.gameaccount.handler.server._BuyOkHandler;
import com.pip.gameaccount.handler.server._DecBalanceOkHandler;
import com.pip.gameaccount.handler.server._LegacyLoginOkHandler;
import com.pip.gameaccount.handler.server._LegacyQuickRegResultHandler;
import com.pip.net.DefaultRequestService;
import com.pip.net.DemuxingMessageHandler;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.message.ErrorMessage;
import com.pip.net.message.ServerLoginMessage;
import com.pip.net.message.gameaccount.AccountInfoMessage;
import com.pip.net.message.gameaccount.AccountInfoOkMessage;
import com.pip.net.message.gameaccount.AccountRegMessage;
import com.pip.net.message.gameaccount.AccountRegOkMessage;
import com.pip.net.message.gameaccount.AddBalanceMessage;
import com.pip.net.message.gameaccount.AddBalanceOkMessage;
import com.pip.net.message.gameaccount.AddRecommendBalanceMessage;
import com.pip.net.message.gameaccount.AddRecommendBalanceOkMessage;
import com.pip.net.message.gameaccount.ChangeStatusMessage;
import com.pip.net.message.gameaccount.CreateIMoneyCardMessage;
import com.pip.net.message.gameaccount.CreateIMoneyCardOkMessage;
import com.pip.net.message.gameaccount.CreditChangeNotifyMessage;
import com.pip.net.message.gameaccount.GetAccountNameMessage;
import com.pip.net.message.gameaccount.LegacyBuy1Message;
import com.pip.net.message.gameaccount.LegacyBuyMessage;
import com.pip.net.message.gameaccount.LegacyFee1Message;
import com.pip.net.message.gameaccount.LegacyFeeMessage;
import com.pip.net.message.gameaccount.LegacyLoginMessage;
import com.pip.net.message.gameaccount.LegacyQuickRegMessage;
import com.pip.net.message.gameaccount.LevelUpNotifyMessage;
import com.pip.net.message.gameaccount.Logout1Message;
import com.pip.net.message.gameaccount.ModifyPasswordMessage;
import com.pip.net.message.gameaccount.ModifyPasswordOkMessage;
import com.pip.net.message.gameaccount.ModifyPhoneMessage;
import com.pip.net.message.gameaccount.ModifyPhoneOkMessage;
import com.pip.net.message.gameaccount.OnlineTimeNotifyMessage;
import com.pip.net.message.gameaccount.PhoneNotifyMessage;
import com.pip.net.message.gameaccount.RecommendRequestMessage;
import com.pip.net.message.gameaccount.RecommendRewardNotifyMessage;
import com.pip.net.message.gameaccount.RenameMessage;
import com.pip.net.message.gameaccount.RenameOkMessage;
import com.pip.net.message.gameaccount.SyncBalanceMessage;
import com.pip.net.message.gameaccount.UseIMoneyCardMessage;
import com.pip.net.message.gameaccount.UseIMoneyCardOkMessage;
import com.pip.net.message.gameaccount._BuyOkMessage;
import com.pip.net.message.gameaccount._DecBalanceOkMessage;
import com.pip.net.message.gameaccount._LegacyLoginOkMessage;
import com.pip.net.message.gameaccount._LegacyQuickRegResultMessage;

public class GameAccountServer {
	
	private Configuration configuration;
	private WorldStub worldStub;
	private AccountSkeleton accountSkeleton;
	private IRequestService requestService;
	private ClientListManager clientListManager;
	private IGameAccountService gameAccountService;
	private ILoginService loginService;
	private ISessionService sessionService;
	
	private static final Logger log = Logger.getLogger(GameAccountServer.class);
	
	public GameAccountServer(Configuration configuration){
		this.configuration = configuration;
	}
	
	public void launch() throws Exception{
		requestService = new DefaultRequestService();
		gameAccountService = new GameAccountService(new GameAccountDAO());
		clientListManager = new ClientListManager(new File(System.getProperty("user.dir")+"/clients.txt"));
		loginService = new LoginService(gameAccountService);
		sessionService = new SessionService();
		worldStub = new WorldStub(configuration,sessionService);
		
		accountSkeleton = new AccountSkeleton("accountskeletons",new InetSocketAddress(
				configuration.getString("accountip"), configuration
						.getInt("accountport")));
		accountSkeleton.setUserName(configuration.getString("serverid"));
		accountSkeleton.setPassword(configuration.getString("password"));
		worldStub.setMessageHandler(getWorldStubMessageHandler());
		accountSkeleton.setMessageHandler(getAccountSkeletonMessageHandler());
		worldStub.start();
		log.info("Server Binded.");
		accountSkeleton.connect();
		log.info("AccountServer Connected.");
	}
	
	public IMessageHandler getWorldStubMessageHandler(){
		DemuxingMessageHandler handler = new DemuxingMessageHandler();
		handler.addHandler(ServerLoginMessage.class, new ServerLoginHandler(clientListManager,sessionService));
		handler.addHandler(LegacyLoginMessage.class, new LegacyLoginHandler(accountSkeleton,requestService));
		handler.addHandler(AccountRegMessage.class, new AccountRegHandler(accountSkeleton,requestService));
		handler.addHandler(LegacyBuyMessage.class, new LegacyBuyHandler(accountSkeleton,loginService,requestService));
		handler.addHandler(LegacyFeeMessage.class, new LegacyFeeHandler(loginService,accountSkeleton,requestService));
		handler.addHandler(LegacyBuy1Message.class, new LegacyBuy1Handler(accountSkeleton,loginService,requestService));
		handler.addHandler(LegacyFee1Message.class, new LegacyFee1Handler(loginService,accountSkeleton,requestService));
		handler.addHandler(LegacyQuickRegMessage.class, new LegacyQuickRegHandler(accountSkeleton,requestService));
		handler.addHandler(Logout1Message.class, new Logout1Handler(accountSkeleton,loginService));
		handler.addHandler(AccountInfoMessage.class, new AccountInfoHandler(loginService,accountSkeleton,requestService));
		handler.addHandler(AddRecommendBalanceMessage.class, new AddRecommendBalanceHandler(accountSkeleton,requestService));
		handler.addHandler(ModifyPasswordMessage.class, new ModifyPasswordHandler(accountSkeleton,loginService,requestService));
		handler.addHandler(ModifyPhoneMessage.class, new ModifyPhoneHandler(accountSkeleton,loginService,requestService));
		handler.addHandler(ChangeStatusMessage.class, new ChangeStatusHandler(loginService,accountSkeleton));
		handler.addHandler(GetAccountNameMessage.class, new GetAccountNameHandler(loginService));
		handler.addHandler(RenameMessage.class, new RenameHandler(accountSkeleton,loginService,requestService));
		handler.addHandler(OnlineTimeNotifyMessage.class, new OnlineTimeNotifyHandler(accountSkeleton));
		handler.addHandler(RecommendRequestMessage.class, new RecommendRequestHandler(accountSkeleton));
		handler.addHandler(LevelUpNotifyMessage.class, new LevelUpNotifyHandler(accountSkeleton));
		handler.addHandler(CreateIMoneyCardMessage.class, new CreateIMoneyCardHandler(accountSkeleton, requestService));
		handler.addHandler(UseIMoneyCardMessage.class, new UseIMoneyCardHandler(accountSkeleton, requestService));
		handler.addHandler(AddBalanceMessage.class, new AddBalanceHandler(accountSkeleton,requestService));
		handler.addHandler(PhoneNotifyMessage.class, new PhoneNotifyHandler(accountSkeleton));
		return handler;
	}
	
	public IMessageHandler getAccountSkeletonMessageHandler(){
		DemuxingMessageHandler handler = new DemuxingMessageHandler();
		handler.addHandler(AccountRegOkMessage.class, new AccountRegOkHandler(gameAccountService,requestService,sessionService));
		handler.addHandler(_LegacyLoginOkMessage.class, new _LegacyLoginOkHandler(loginService,requestService,sessionService));
		handler.addHandler(_BuyOkMessage.class, new _BuyOkHandler(loginService,requestService,gameAccountService,sessionService));
		handler.addHandler(_DecBalanceOkMessage.class, new _DecBalanceOkHandler(loginService,requestService,gameAccountService,accountSkeleton));
		handler.addHandler(ErrorMessage.class, new ErrorHandler(loginService,requestService,sessionService));
		handler.addHandler(_LegacyQuickRegResultMessage.class, new _LegacyQuickRegResultHandler(gameAccountService,requestService,sessionService));
		handler.addHandler(AccountInfoOkMessage.class, new AccountInfoOkHandler(sessionService,requestService));
		handler.addHandler(ModifyPasswordOkMessage.class, new ModifyPasswordOkHandler(requestService,sessionService));
		handler.addHandler(ModifyPhoneOkMessage.class, new ModifyPhoneOkHandler(requestService,sessionService));
		handler.addHandler(AccountInfoOkMessage.class, new AccountInfoOkHandler(sessionService,requestService));
		handler.addHandler(RenameOkMessage.class, new RenameOkHandler(requestService,sessionService,loginService));
		handler.addHandler(AddRecommendBalanceOkMessage.class, new AddRecommendBalanceOkHandler(requestService,sessionService));
		handler.addHandler(CreditChangeNotifyMessage.class, new CreditChangeNotifyHandler(sessionService));
        handler.addHandler(RecommendRewardNotifyMessage.class, new RecommendRewardNotifyHandler(sessionService));
        handler.addHandler(CreateIMoneyCardOkMessage.class, new CreateIMoneyCardOkHandler(requestService, sessionService));
        handler.addHandler(UseIMoneyCardOkMessage.class, new UseIMoneyCardOkHandler(requestService, sessionService));
        handler.addHandler(AddBalanceOkMessage.class, new AddBalanceOkHandler(requestService,sessionService));
        handler.addHandler(SyncBalanceMessage.class, new SyncBalanceHandler(sessionService, loginService));
        return handler;		
	}
	
	public static void main(String[] args) throws Exception{
		GameAccountServer server = new GameAccountServer(new PropertiesConfiguration("config.properties"));
		server.launch();
	}
}
