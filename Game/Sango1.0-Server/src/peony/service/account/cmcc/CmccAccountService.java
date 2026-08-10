package peony.service.account.cmcc;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
import peony.game.GameItem;
import peony.game.Identity;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.account.Account;
import peony.service.account.AccountAsyncCall;
import peony.service.account.AccountLoginCall;
import peony.service.account.AccountService;
import peony.service.account.ChargeRegularCall;
import peony.service.account.ErrorMessages;
import peony.service.account.RecordChargeCall;
import peony.service.shop.CmccIMoneyBuyCall;
import peony.service.shop.ShopService;
import ch.javasoft.util.intcoll.IntHashMap;
import com.pip.net.IMessage;
import com.pip.net.message.ServerMessageType;
import com.pip.net.message.gameaccount.ForceLogoutMessage;
import com.pip.net.message.gameaccount.GameAccountMessageType;
import com.pip.net.message.gameaccount.Logout1Message;
import com.pip.net.uwap2.mina.Packet;
import com.pip.net.uwap2.mina.UWAPData;
import com.pip.net.uwap2.mina.UWAPDecoder;
import com.pip.net.uwap2.mina.UWAPEncoder;
import com.pip.net.uwap2.mina.UWAPSegment;

public class CmccAccountService implements AccountService ,Service ,ServiceEventListener, IoHandler{
	
	private Logger log = Logger.getLogger(CmccAccountService.class);
	
	protected ConcurrentHashMap<Integer,AccountAsyncCall> calls = new ConcurrentHashMap<Integer,AccountAsyncCall>();
	protected ExecutorService executor = Executors.newCachedThreadPool();
	
	protected ConcurrentHashMap<Integer,Account> accounts = new ConcurrentHashMap<Integer,Account>();
	protected IntHashMap<ClientSession> sessions = new IntHashMap<ClientSession>();
	protected HashMap<Integer,Integer> playerId2serial = new HashMap<Integer,Integer>();
	
	private SocketConnector connector;
	private IoSession session;
	
	protected SocketConnectorConfig socketConfig;
	
	protected int receiveBufferSize = 32767;
	protected int sendBufferSize = 32767;
	
	protected MessageToUWAPTranslator message2uwap = new MessageToUWAPTranslator();
	protected UWAPToMessageTranslator uwap2message = new UWAPToMessageTranslator();
	
	protected Configuration config;
	
	protected volatile boolean isValid;
	
	protected Object startupLock = new Object();
	
	protected Thread thread;
	
	public static String CMCC_ANDROID_USERID = "auto:160121244000";
	
	public static String CMCC_ANDROID_USERKEY = "2222";
	
	/**
	 * 10元宝消费代码
	 */
	public static final String CONSUME_CODE_10YUANBAO = "160121244070";
	/**
	 * 60元宝消费代码
	 */
	public static final String CONSUME_CODE_60YUANBAO = "160121244069";
	/**
	 * 300元宝消费代码
	 */
	public static final String CONSUME_CODE_300YUANBAO = "160121244071";
	
	/**
	 * 10元宝标题
	 */
	public static final String TITLE_10YUANBAO = "100点(1元)———>10元宝";
	/**
	 * 60元宝标题
	 */
	public static final String TITLE_60YUANBAO = "600点(6元)———>60元宝";
	/**
	 * 300元宝标题
	 */
	public static final String TITLE_300YUANBAO = "3000点(30元)———>300元宝";
	
	/**
	 * 消费代码对应的ib数（单位：i）
	 */
	public static HashMap<String,Float> consumecode2yuanbao = new HashMap<String,Float>();
	
	private peony.net.Packet pkt;
	
	public CmccAccountService(Configuration config){
		this.config = config;
		initConnector();

	}
	
	protected void initConnector(){
		connector = new SocketConnector(1, Executors.newCachedThreadPool());
		socketConfig = new SocketConnectorConfig();
		socketConfig.setThreadModel(ThreadModel.MANUAL);
		socketConfig.getSessionConfig().setTcpNoDelay(true);
		socketConfig.getSessionConfig().setSendBufferSize(sendBufferSize);
		socketConfig.getSessionConfig().setReceiveBufferSize(receiveBufferSize);
		socketConfig.getFilterChain().addLast("uwap2codec", new ProtocolCodecFilter(new UWAPEncoder(), new UWAPDecoder()));
		consumecode2yuanbao.put(CONSUME_CODE_10YUANBAO, new Float(360f));
		consumecode2yuanbao.put(CONSUME_CODE_60YUANBAO, new Float(2160f));
		consumecode2yuanbao.put(CONSUME_CODE_300YUANBAO, new Float(10800f));
		pkt = new peony.net.Packet(OpCode.GET_CMCC_YUANBAO_LIST_SERVER);
		pkt.put(consumecode2yuanbao.size());
		pkt.putString(TITLE_10YUANBAO);
		pkt.putString(CONSUME_CODE_10YUANBAO);
		pkt.putString(TITLE_60YUANBAO);
		pkt.putString(CONSUME_CODE_60YUANBAO);
		pkt.putString(TITLE_300YUANBAO);
		pkt.putString(CONSUME_CODE_300YUANBAO);
	}
	
	/**
	 * 事先组装好的商品列表包
	 * @return
	 */
	public peony.net.Packet getCMCCYuanbaoList(){
		return pkt;
	}

	public boolean allowLogin(ClientSession session) {
		return false;
	}

	public Account getAccount(int accountId) {
		return accounts.get(accountId);
	}

	public ClientSession getClientSession(int accountId) {
		return sessions.get(accountId);
	}

	public void postMessage(IMessage message) {
		UWAPSegment seg = translateMessageToUWAP(message);
		if(seg != null){
			session.write(seg);
		}
		if(message instanceof CmccAccountRenameMessage){
			CmccAccountRenameMessage msg = (CmccAccountRenameMessage)message;
			playerId2serial.put(msg.getPlayerId(), message.getSerial());
		}
		else if(message instanceof CmccModifyPasswordMessage){
			CmccModifyPasswordMessage msg = (CmccModifyPasswordMessage)message;
			playerId2serial.put(msg.getPlayerId(), message.getSerial());
		}
	}
	
	protected UWAPSegment translateMessageToUWAP(IMessage message){
		return message2uwap.translate(message);
	}

	
	
	public void registerClientSession(ClientSession session) {
		Identity identity = session.getIdentity();
		if (identity instanceof Account) {
			accounts.put(identity.getId(), (Account) identity);
			sessions.put(identity.getId(), session);
		}
	}

	public void schedule(AccountAsyncCall call) {
		executor.execute(call);
	}

	public boolean scheduleLogin(AccountLoginCall call) {
		return false;
	}

	public void sendAndRegister(IMessage message, AccountAsyncCall call) {
		postMessage(message);
		register(message, call);
	}
	
	protected  void  register(IMessage message,AccountAsyncCall call){
		calls.put(message.getSerial(), call);
	}

	public int[] getEventTypes() {
		return new int[] {
				ServiceEvent.EVENT_SESSION_ADDED,
				ServiceEvent.EVENT_SESSION_REMOVED
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
				postMessage(msg);
				sessions.remove(identity.getId());
			}
		}
	}

	public void handleMessage(IMessage message) throws Exception{
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
					if (call instanceof CmccIMoneyBuyCall) {
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
				CmccRenameResultMessage msg = (CmccRenameResultMessage) message;
				Integer o = playerId2serial.remove(msg.getPlayerId());
				if(o != null){
					AccountAsyncCall call = calls.remove(o);
					if(call != null){
						if (msg.isSuccess()) {
							call.setMessage(message);
							call.addToClientSession();
						}else{
							call.setSuccess(false);
							call.setMessage(new CmccErrorMessage(o, ErrorMessages.UNKNOW, msg.getMsg()));
							call.addToClientSession();
						}
					}

				}
			}
				break;
			case GameAccountMessageType.MODIFY_PASSWORD_OK:{
				CmccModifyPasswordResultMessage msg = (CmccModifyPasswordResultMessage) message;
				Integer o = playerId2serial.remove(msg.getPlayerId());
				if(o != null){
					AccountAsyncCall call = calls.remove(o);
					if(call != null){
						if (msg.isSuccess()) {
							call.setMessage(message);
							call.addToClientSession();
						}else{
							call.setSuccess(false);
							call.setMessage(new CmccErrorMessage(o, ErrorMessages.UNKNOW, msg.getMsg()));
							call.addToClientSession();
						}
					}

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
			case 510:{
				AccountAsyncCall call = calls.remove(message.getSerial());
				if(call != null){
					call.setMessage(message);
					call.callFinish();
				}
			}
				break;
			case 602: {
				AccountAsyncCall call = calls.remove(message.getSerial());
				if (call != null) {
					call.setMessage(message);
					call.callFinish();
				}
			}
				break;
			case CmccMessageType.PUSH_DOWNLOAD: {
				CmccPushDownloadMessage msg = (CmccPushDownloadMessage)message;
				Player p = (Player)ObjectAccessor.getPlayer(msg.getPlayerId());
				if(p != null){
					peony.net.Packet pt = new peony.net.Packet(OpCode.CMCC_PUSHDOWNLOAD_SERVER);
					pt.putString(msg.getUrl());
					p.send(pt);
				}
			}
				break;
			case CmccMessageType.ANDROIDBUYREQRESULT: {
				AccountAsyncCall call = calls.remove(message.getSerial());
				if (call != null) {
					call.setMessage(message);
					call.callFinish();
				}
			}
				break;
			case CmccMessageType.ANDROIDBUYOK: {
				CmccAndroidSmsBuyReqOkMessage mess = (CmccAndroidSmsBuyReqOkMessage)message;
				int playerId = mess.getPlayerId();
//				int itemId = mess.getItemId();
//				ShopService shopService = Server.server.getServiceRegistry().getShopService();
//				float money = shopService.getItemPrice(itemId);
				Float moneyObj = consumecode2yuanbao.get(mess.consumeCode);
				float money = moneyObj.floatValue();
//				GameItem item = ObjectAccessor.createGameItem(itemId);
				Player player = ObjectAccessor.getPlayer(playerId);
				if(player!=null){
//					log.info("[CMCCANDROIDBUY]"+LogUtil.getPlayerLogString(player)+"]ITEM["+itemId+"]TRY");
//					PlayerTransaction tx = player.newTransaction("CMCCANDROIDBUY");
//					try {
//						player.bag.addGameItemComplete(item, 1, tx, false);
//						tx.commit();
//						log.info("[CMCCANDROIDBUY]"+LogUtil.getPlayerLogString(player)+"]ITEM["+itemId+"]OK");
//					} catch (Exception e) {
//						tx.rollback();
//						Server.server.getServiceRegistry().getMailService().sendSystemMailAsync(
//								playerId, peony.Messages.STRING_00004, "", "", 0, item, 1, "CMCCANDROIDBUY");
//						log.info("[CMCCANDROIDBUY]"+LogUtil.getPlayerLogString(player)+"]ITEM["+itemId+"][MAIL]OK");
//					}
					long oldIMoney = player.getAccount().getLongIMoney();
					player.getAccount().setLongIMoney((long) (oldIMoney + money*100));
					String showPrice = player.ibToYuanbao(player.getAccount().getLongIMoney());
		 			player.addStringPropertyChangedItem(ChangedItem.YUANBAO, showPrice, true);
		 			int ammount = (int)money/360;
		 			RecordChargeCall call = new RecordChargeCall(null, player.accountId, ammount);
					Server.server.getServiceRegistry().getDbService().schedule(call);
					ChargeRegularCall call2 = new ChargeRegularCall(player.session,player.accountId, ammount);
					Server.server.getServiceRegistry().getDbService().schedule(call2);
					Server.server.getEventManager().fireEvent(
							new ServiceEvent(ServiceEvent.EVENT_CHARGE_SUCCESS, player, ammount));
		 			log.info("[CMCCANDROIDBUY]"+LogUtil.getPlayerLogString(player)+"]PRICE["+money+"]");
				}else{
//					log.info("[CMCCANDROIDBUY]PLAYER["+playerId+"]ITEM["+itemId+"]TRY");
//					Server.server.getServiceRegistry().getMailService().sendSystemMailAsync(
//							playerId, peony.Messages.STRING_00004, "", "", 0, item, 1, "CMCCANDROIDBUY");
//					log.info("[CMCCANDROIDBUY]PLAYER["+playerId+"]ITEM["+itemId+"][MAIL]OK");
				}
			}
				break;
			case GameAccountMessageType.MODIFY_PHONE_OK:{
				AccountAsyncCall call = calls.remove(message.getSerial());
				if(call != null){
					call.setMessage(message);
					call.addToClientSession();
				}
			}
			case CmccMessageType.CMCC_BUY2_RESULT:{
				AccountAsyncCall call = calls.remove(message.getSerial());
				if (call != null) {
					call.setMessage(message);
					call.callFinish();
				}
			}
				break;	
		}
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	protected void connect() throws Exception{
		connector.connect(new InetSocketAddress(config.getString("address"), config.getInt("port")),this, socketConfig);
		synchronized(startupLock){
			startupLock.wait(20*1000L);
		}
		if(!isValid){
			throw new Exception("connect timeout");
		}
	}
	
	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
		connect();
		thread = new Thread(new LiveNotifier());
		thread.start();
	}

	public void exceptionCaught(IoSession arg0, Throwable arg1)
			throws Exception {
		log.info(arg1,arg1);
	}

	public void messageReceived(IoSession session, Object msg) throws Exception {
		if(msg instanceof Packet){
			UWAPData data  = ((Packet)msg).datas[0];
			if(data.getAppType() == SERVER_LOGIN_OK){
				isValid = true;
				log.info("Account Server LoginOK");
				synchronized(startupLock){
					startupLock.notify();
				}
			}else{
				IMessage message = uwap2message.translate(data);
				if(message != null){
					handleMessage(message);
				}
			}
		}
	}

	public void messageSent(IoSession session, Object msg) throws Exception {
	}

	public void sessionClosed(IoSession session) throws Exception {
		isValid = false;
		log.info("AccountServer Disconnected");
		while(!isValid){
			try {
				Thread.sleep(3000L);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			try {
				initConnector();
				connect();
			} catch (Exception e) {
				log.info("Retry Error");
			}
		}
	}

    /**
     * 世界服务器登录
     * id				String			服务器ID
     * password			String			密码
     */
	public static final byte SERVER_LOGIN = (byte)180;
	/**
	 * 世界服务器登录成功
	 * 无参数
	 */
	public static final byte SERVER_LOGIN_OK = (byte)181;
	
	public void sessionCreated(IoSession session) throws Exception {
		this.session = session;
		UWAPSegment seg = new UWAPSegment(SERVER_LOGIN);
		seg.writeString(config.getString("name"));
		seg.writeString(config.getString("password"));
		session.write(seg);
	}

	public void sessionIdle(IoSession session, IdleStatus arg1) throws Exception {
		
	}

	public void sessionOpened(IoSession session) throws Exception {
		
	}
	
	public static final byte CMCC_LIVE_NOTIFY = (byte)219;
	class LiveNotifier implements Runnable {
		public void run() {
			while (true) {
				if (isValid&&session!=null) {
					for (Account a : accounts.values()) {
						if(a.getCmccUserId()!=null&&a.getCmccUserKey()!=null){
							UWAPSegment seg = new UWAPSegment(CMCC_LIVE_NOTIFY);
							seg.writeString(a.getCmccUserId());
							session.write(seg);
						}
					}
				}
				try {
					Thread.sleep(180 * 1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
