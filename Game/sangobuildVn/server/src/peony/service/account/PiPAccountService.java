package peony.service.account;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.Configuration;
import org.apache.mina.filter.codec.ProtocolCodecFilter;

import peony.game.Identity;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.shop.IMoneyBuyCall;
import ch.javasoft.util.intcoll.IntHashMap;

import com.pip.net.Connector;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.message.ServerMessageType;
import com.pip.net.message.gameaccount.ForceLogoutMessage;
import com.pip.net.message.gameaccount.GameAccountMessageType;
import com.pip.net.message.gameaccount.Logout1Message;
import com.pip.net.message.gameaccount.MessageDecoder;
import com.pip.net.message.gameaccount.MessageEncoder;
import com.pip.net.message.gameaccount.SyncBalanceMessage;
import com.pip.net.uwap2.mina.UWAP2MessageFilter;
import com.pip.net.uwap2.mina.UWAPDecoder;
import com.pip.net.uwap2.mina.UWAPEncoder;

public class PiPAccountService extends Connector implements Service,IMessageHandler,ServiceEventListener,AccountService {
	private static final String ADDRESS = "address";
	private static final String PORT = "port";
	private static final String NAME = "name";
	private static final String PASSWORD = "password";
	
	protected ConcurrentHashMap<Integer,AccountAsyncCall> calls = new ConcurrentHashMap<Integer,AccountAsyncCall>();
	protected ExecutorService executor = Executors.newCachedThreadPool();
	
	protected IntHashMap<Account> accounts = new IntHashMap<Account>();
	protected IntHashMap<ClientSession> sessions = new IntHashMap<ClientSession>();
	
	
	public PiPAccountService(Configuration config){
		super("sangoaccountservice",new InetSocketAddress(config.getString(ADDRESS),config.getInt(PORT)),true);
		setUserName(config.getString(NAME));
		setPassword(config.getString(PASSWORD));
		setMessageHandler(this);
	}

	@Override
	public void init(){
        config.getFilterChain().addLast("uwap2codec", new ProtocolCodecFilter(new UWAPEncoder(), new UWAPDecoder()));
        config.getFilterChain().addLast("uwap2message",new UWAP2MessageFilter(new MessageDecoder(),new MessageEncoder()));		
	}
	
	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
		connect();
	}
	
	
	public void shutdown() {
		close();
		Server.server.getEventManager().unregisterListener(this);
	}
	
	public int[] getEventTypes() {
		return new int[] {
				ServiceEvent.EVENT_SESSION_ADDED,
				ServiceEvent.EVENT_SESSION_REMOVED,
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_SESSION_ADDED:
			this.sessionAdded((ClientSession)event.param1);
			break;
		case ServiceEvent.EVENT_SESSION_REMOVED:
			this.sessionRemoved((ClientSession)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			processPlayerLoaded((Player)event.param1);
			break;
		}
	}
	
	protected void processPlayerLoaded(Player p){
		if(p!=null && p.session!=null && p.accountId>0 && accounts.get(p.accountId)==null){
			Identity identity = p.session.getIdentity();
			if(identity!=null && (identity instanceof Account))
				accounts.put(p.accountId, (Account)identity);
			sessions.put(p.accountId, p.session);
		}
	}
	
	public void handle(IMessage message) throws Exception {
		switch (message.getCmd()) {
		case GameAccountMessageType.LEGACY_LOGIN_OK: {
			AccountAsyncCall call = calls.remove(message.getSerial());
			if (call != null) {
				call.setMessage(message);
				call.addToClientSession();
			}
		}
			break;
		case ServerMessageType.ERROR: {
			AccountAsyncCall call = calls.remove(message.getSerial());
			if (call != null) {
				if (call instanceof IMoneyBuyCall) {
					call.setSuccess(false);
					call.setMessage(message);
					call.callFinish();
				} else {
					call.setSuccess(false);
					call.setMessage(message);
					call.addToClientSession();
				}
			}
		}
			break;
		case GameAccountMessageType.FORCE_LOGOUT: {
			ForceLogoutMessage msg = (ForceLogoutMessage) message;
			ClientSession session = sessions.get(msg.getId());
			if (session != null) {
				session.close();
			}
		}
			break;
		case GameAccountMessageType.ACCOUNT_REG_OK: {
			AccountAsyncCall call = calls.remove(message.getSerial());
			if(call != null){
				call.setMessage(message);
				call.addToClientSession();
			}
			
		}
			break;
		case GameAccountMessageType.LEGACY_QUICKREG_RESULT:{
			AccountAsyncCall call = calls.remove(message.getSerial());
			if(call != null){
				call.setMessage(message);
				call.addToClientSession();
			}
		}
			break;
		case GameAccountMessageType.RENAME_OK:{
			AccountAsyncCall call = calls.remove(message.getSerial());
			if(call != null){
				call.setMessage(message);
				call.addToClientSession();
			}
		}
			break;
		case GameAccountMessageType.MODIFY_PASSWORD_OK:{
			AccountAsyncCall call = calls.remove(message.getSerial());
			if(call != null){
				call.setMessage(message);
				call.addToClientSession();
			}
		}
			break;
		case GameAccountMessageType.ACCOUNT_INFO_OK:{
			AccountAsyncCall call = calls.remove(message.getSerial());
			if(call != null){
				call.setMessage(message);
				call.addToClientSession();
			}
		}
			break;
		case GameAccountMessageType.LEGACY_BUY_RESULT:{
			AccountAsyncCall call = calls.remove(message.getSerial());
			if(call != null){
				call.setMessage(message);
				call.callFinish();
			}
		}
			break;
		case GameAccountMessageType.CREATE_IMONEY_CARD_OK:{
			AccountAsyncCall call = calls.remove(message.getSerial());
			if(call != null){
				call.setMessage(message);
				call.callFinish();
			}
		}
			break;
		case GameAccountMessageType.USE_IMONEY_CARD_OK:{
			AccountAsyncCall call = calls.remove(message.getSerial());
			if(call != null){
				call.setMessage(message);
				call.callFinish();
			}
		}
			break;
		case GameAccountMessageType.ADD_BALANCE_OK:{
			AccountAsyncCall call = calls.remove(message.getSerial());
			if(call != null){
				call.setMessage(message);
				call.callFinish();
			}
		}
			break;
		case GameAccountMessageType.SYNC_BALANCE: {
			SyncBalanceMessage sbmsg = (SyncBalanceMessage)message;
			int accountID = sbmsg.getAccountId();
			int balance = sbmsg.getBalance();
			Account account = getAccount(accountID);
			if (account != null) {
				account.setIMoney(balance);
			}
			ClientSession session = getClientSession(accountID);
			if (session != null) {
				Player p = (Player)session.getClient();
				if (p != null) {
					p.addIntPropertyChangedItem(ChangedItem.IMONEY, account.getIMoney() / 100, true, true);
				}
			}
		}
		break;
		}

	}
	
	
	protected  void  register(IMessage message,AccountAsyncCall call){
		calls.put(message.getSerial(), call);
	}
	
	public void postMessage(IMessage message) {
		send(message);
	}
	
	public void sendAndRegister(IMessage message,AccountAsyncCall call){
		send(message);
		register(message, call);
//		executor.execute(call);
	}
	
	public void schedule(AccountAsyncCall call){
		executor.execute(call);
	}
	
	public void registerClientSession(ClientSession session){
		Identity identity = session.getIdentity();
		if(identity instanceof Account){
			accounts.put(identity.getId(), (Account)identity);
			sessions.put(identity.getId(), session);
		}
	}
	
	public void sessionAdded(ClientSession session) {
		
	}

	public void sessionRemoved(ClientSession session) {
		Identity identity = session.getIdentity();
		if(identity!=null&&identity instanceof Account){
			Account a = accounts.remove(identity.getId());
			Account a1 = (Account)identity;
			if(a!=null&&a.getKey().equals(a1.getKey())){
				Logout1Message msg = new Logout1Message(a.getId(),a.getKey());
				send(msg);	
				sessions.remove(identity.getId());
			}
		}
	}
	
	public Account getAccount(int accountId){
		return accounts.get(accountId);
	}
	
	public ClientSession getClientSession(int accountId){
		return sessions.get(accountId);
	}

	/**
	 * 检查现在是否允许登录。只有没有到达服务器上限，并且排队队列中没有请求时，才可以登录。
	 */
	public boolean allowLogin(ClientSession session) {
		return session.checkOnlineCount(accounts.size());
	}
	
	/**
	 * 服务器满时把一个登录请求加入到排队队列中去。
	 * @param call
	 * @return
	 */
	public boolean scheduleLogin(AccountLoginCall call) {
		return false;
	}
}

