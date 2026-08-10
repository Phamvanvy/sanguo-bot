package peony.game.notification;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import peony.game.DayListener;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.sleepycat.SleepyCatService;
import peony.util.TimeUtil;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class NotificationService implements Service,ServiceEventListener,DayListener{
	
	private static final Logger log = Logger.getLogger(NotificationService.class);
	
	public static final String DATABASE_NAME = "Notification";
	
	public static final String APP_ID = "Sanguo";
	
	public static final String PROVIDER = "Apple";
	
//	
//	String msg = "明珠三国全新资料片《天命之战》火爆上线，置身于战火弥漫的三国时代，亲身体验刀光剑影折戟沉沙的爽快感觉，天下大势任你操控，一切精彩尽在明珠三国！";
	
	String msg = "喜迎新年，宝石、随从、坐骑免费送啦！";
	
	public boolean push = false;
	
	private long ONEDAY = 24 * 3600 * 1000L; // 一天
	
	// 上次推送时间
	protected int lastPushTime;
	
	protected int lastNum = 0;
	
//	public List<String> tokens = new ArrayList<String>();
	
	public List<String> pushedTokens = new ArrayList<String>();  //当天已推送过的token
	
	public Map<String,List<NotificationToken>> tokenToAcc = new HashMap<String,List<NotificationToken>>();
	
//	public Map<String,List<Player>> accToPlayers = new HashMap<String,List<Player>>();
	
	private Database db = null;
	
	private String bindUrl = "http://127.0.0.1:7100/bind";
	private String pushUrl = "http://127.0.0.1:7100/push";
	
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private BindCallback bindCallback;
	private PushCallback pushCallback;

	
	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		Database db = null;
		db = dbservice.notificationDB;
		if (db != null) {
			try {
				db.close();
			} catch (Exception e) {
			}
		}
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
		try{
			this.db = Server.server.getServiceRegistry().getSleepyCatService().notificationDB;
			this.bindUrl = Server.server.getConfig().getString("notification_bind_url");
			this.pushUrl = Server.server.getConfig().getString("notification_push_url");
			if(this.bindUrl == null || this.bindUrl.length() == 0 || this.pushUrl == null || this.pushUrl.length() == 0)
				throw new IllegalArgumentException();
			load();
			processNotify();
		}catch(Exception e){
			
		}
	}
	
	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_PLAYER_LOGOUTED
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
			case ServiceEvent.EVENT_PLAYER_LOGOUTED:
				processPlayerLogOut((Player)event.param1);
				break;
		}
	}

	public void bind(String appId, String provider, int accountId, String token) {
		try {
 			NotificationToken nf = fetch(accountId);
			if(nf != null) {
				List<NotificationToken> list = tokenToAcc.get(token);
				nf.lastLogoutTime = new Date().getTime();
				if(list==null)
					list = new ArrayList<NotificationToken>();
				if(!hasNotification(nf,list)){
					list.add(nf);
				    tokenToAcc.put(token, list);
				}
				if(nf.provider.equals(provider) && nf.deviceToken.equals(token)){ //如果能够从本地数据库获得相同的token，说明已经绑定过了
					return;
				}
			}
			nf = new NotificationToken(accountId,provider,token,new Date().getTime());
			addToken(nf);
			executor.execute(new BindWorker(appId, provider, accountId, token));
		} catch (DatabaseException e) {
			log.error(e.toString(), e);
		}
	}
	
	public boolean hasNotification(NotificationToken nf,List<NotificationToken> list){
		if(list.size()>0){
			for(NotificationToken nl : list){
				if(nl.accountId == nf.accountId){
					return true;
				}
			}
		}
		return false;
	}
	
	public void addToken(NotificationToken nf){
		if(nf!=null){
//			if(!tokens.contains(nf.deviceToken)){
//				tokens.add(nf.deviceToken);
//			}
			List<NotificationToken> nfToken = tokenToAcc.get(nf.deviceToken);
			if(nfToken == null){
				nfToken = new ArrayList<NotificationToken>();
			}
			if(!nfToken.contains(nf)){
				nfToken.add(nf);
				tokenToAcc.put(nf.deviceToken, nfToken);
			}
		}
	}
	
//	public void processPlayerLoad(Player player){
//		if(player!=null){
//			String token = player.pushToken;
//			List<Player> playerList = accToPlayers.get(token);
//			if(playerList==null){
//				playerList = new ArrayList<Player>();
//			}
//			if(!playerList.contains(player)){
//				playerList.add(player);
//				accToPlayers.put(token, playerList);
//			}
//		}   
//	}
	
	
	
	public void processPlayerLogOut(Player player){
		if(player!=null){
			String pushToken = player.pushToken;
			if(!pushToken.equals("")){
				List<NotificationToken> list = tokenToAcc.get(pushToken);
				if(list!=null && list.size()>0){
					for(NotificationToken nt : list){
						if(nt.accountId == player.accountId){
							nt.lastLogoutTime = new Date().getTime();
							break;
						}
					}
				}
//				if(accToPlayers.containsKey(pushToken)){
//					List<Player> playerList = accToPlayers.get(pushToken);
//					if(playerList==null){
//						playerList = new ArrayList<Player>();
//					}
//					for(Player p : playerList){
//						if(p.id == player.id){
//							p.lastLogoutTime = player.lastLogoutTime;
//							accToPlayers.put(pushToken,playerList);
//							break;
//						}
//					}
//				}
			}
		}
	}
	
	public void setBindCallback(BindCallback bindCallback) {
		this.bindCallback = bindCallback;
	}
	
	public void setPushCallback(PushCallback pushCallback) {
		this.pushCallback = pushCallback;
	}
	
	/**
	 * 跟Push服务器请求绑定
	 * @param appId
	 * @param provider
	 * @param accountId
	 * @param token
	 * @return
	 */
	private boolean bindRemote(String appId, String provider, int accountId, String token) {
		PostMethod method = new PostMethod(bindUrl);
		method.getParams().setContentCharset("utf-8");
		method.addRequestHeader("Connection", "close");
		method.setParameter("appid", appId);
		method.setParameter("provider", provider);
		method.setParameter("accountid", String.valueOf(accountId));
		method.setParameter("token", token);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
		int code = 0;
		BufferedReader br = null;
		try {	
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams()
					.setConnectionTimeout(30000);
			httpclient.getParams().setSoTimeout(30000);
			code = httpclient.executeMethod(method);
			br = new BufferedReader(new InputStreamReader(method
					.getResponseBodyAsStream(), "UTF-8"));
			if (code == 200) {
				String ret = br.readLine();
				if("true".equals(ret)) {
					return true;
				} 
			}
			return false;
		} catch (Exception e) {
			log.error(e.toString(), e);
			return false;
		} finally {
			method.releaseConnection();
		}
	}
	
	/**
	 * 跟push服务器请求发送push信息
	 * @param appId
	 * @param provider
	 * @param accountId
	 * @param token
	 * @param msg
	 * @return
	 */
	private boolean pushRemote(String appId, String provider, int accountId, String token, String msg) {
		PostMethod method = new PostMethod(pushUrl);
		method.getParams().setContentCharset("utf-8");
		method.addRequestHeader("Connection", "close");
		method.setParameter("appid", appId);
		method.setParameter("provider", provider);
		method.setParameter("accountid", String.valueOf(accountId));
		method.setParameter("sound", "default");
		if(token != null) //token是可选参数
			method.setParameter("token", token);
		method.setParameter("message", msg);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
		int code = 0;
		BufferedReader br = null;
		try {
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams()
					.setConnectionTimeout(30000);
			httpclient.getParams().setSoTimeout(30000);
			code = httpclient.executeMethod(method);
			br = new BufferedReader(new InputStreamReader(method
					.getResponseBodyAsStream(), "UTF-8"));
			if (code == 200) {
				String ret = br.readLine();
				if("true".equals(ret)) {
					return true;
				} 
			}
			return false;
		} catch (Exception e) {
			log.error(e.toString(), e);
			return false;
		} finally {
			method.releaseConnection();
		}
	}

	public NotificationToken find(int accountId) {
		try {
			return fetch(accountId);
		} catch (DatabaseException e) {
			log.error(e.toString(), e);
			return null;
		}
	}

	public void push(String appId, int accountId, String msg) {
		push(appId, null, accountId, null ,msg);
	}

	public void push(String appId, String provider, int accountId,
			String token, String msg) {
		executor.execute(new PushWorker(appId, provider, accountId, token, msg));
	}
	
	protected void bindSuccess(String appId, String provider, int accountId,
			String token) {
		log.info("[NOTIFICATIONBINDOK]PROVIDER["+provider+"]ACC["+accountId+"]TOKEN["+token+"]");
		try {
			save(new NotificationToken(accountId, provider, token,new Date().getTime()));
			if(bindCallback !=  null) {
				bindCallback.bindSuccess(appId, provider, accountId, token);
			}
		} catch (DatabaseException e) {
			log.error(e.toString() ,e);
		}
	}
	
	protected void bindFail(String appId, String provider, int accountId,
			String token) {
		log.info("[NOTIFICATIONBINDFAIL]PROVIDER["+provider+"]ACC["+accountId+"]TOKEN["+token+"]");
		if(bindCallback != null) {
			bindCallback.bindFail(appId, provider, accountId, token);
		}
	}
	
	protected void pushSuccess(String appId, String provider, int accountId,
			String token, String message) {
		log.info("[NOTIFICATIONPUSHOK]PROVIDER["+provider+"]ACC["+accountId+"]TOKEN["+token+"]MSG["+message+"]");
		if(pushCallback != null) {
			this.pushCallback.pushSuccess(appId, provider, accountId, token, message);
		}
	}
	
	protected void pushFail(String appId, String provider, int accountId,
			String token, String message) {
		log.info("[NOTIFICATIONPUSHFAIL]PROVIDER["+provider+"]ACC["+accountId+"]TOKEN["+token+"]MSG["+message+"]");
		if(pushCallback != null) {
			pushCallback.pushFail(appId, provider, accountId, token, message);
		}
	}
	
	protected void save(NotificationToken token) throws DatabaseException {
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		IntegerBinding.intToEntry(token.accountId, key);
		NotificationTokenTupleBinding valueBinding = new NotificationTokenTupleBinding();
		valueBinding.objectToEntry(token, value);
		OperationStatus status = db.put(null, key, value);
	}
	
	protected NotificationToken fetch(int accountId) throws DatabaseException {
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		IntegerBinding.intToEntry(accountId, key);
		OperationStatus status = db.get(null, key, value, LockMode.DEFAULT);
		if(status == OperationStatus.SUCCESS) {
			NotificationTokenTupleBinding valueBinding = new NotificationTokenTupleBinding();
			return valueBinding.entryToObject(value);
		}
		return null;
	}
	
	public String getDBName(){
		return DATABASE_NAME;
	}
	
	protected void load(){
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		Database db = null;
		try {
			db = dbservice.notificationDB;
			Cursor cursor = null;
            DatabaseEntry key = new DatabaseEntry();
            DatabaseEntry value = new DatabaseEntry();
            try {
	            cursor = db.openCursor(null, new CursorConfig());
	            while (cursor.getNext(key, value, null) != OperationStatus.NOTFOUND) {
	            	OperationStatus status = db.get(null, key, value, LockMode.DEFAULT);
	            	NotificationToken token = null;
	            	
	        		if(status == OperationStatus.SUCCESS) {
		            	NotificationTokenTupleBinding valueBinding = new NotificationTokenTupleBinding();
		            	token =  valueBinding.entryToObject(value);
		            	List<NotificationToken> list = tokenToAcc.get(token.deviceToken);
		            	if(list==null){
		            		list = new ArrayList<NotificationToken>();
		            	}
		            	list.add(token);
		            	tokenToAcc.put(token.deviceToken, list);
	        		}
//	        		if(token != null){
//	        			try{
//		        			List<Player> players = Server.server.getServiceRegistry().getDbService().playerDAO.getPlayerByAcc(token.accountId);
//		        			if(players!=null){
//		        				if(!tokens.contains(token.deviceToken)){
//		        				   tokens.add(token.deviceToken);
//		        				}
//		        				List<Player> ps = accToPlayers.get(token.deviceToken);
//		        				if(ps == null){
//		        					ps = new ArrayList<Player>();
//		        				}
//		        				for(Player p : players){
//		        					ps.add(p);
//		        				}
//			            	    accToPlayers.put(token.deviceToken, ps);
//			            	    addToken(token);
//		        			}
//	        			}catch(Exception e){
//	        				
//	        			}
//	        		}
	            }
	        } finally {
	            if (cursor != null) {
	                try {
	                    cursor.close();
	                } catch (Exception e) {
	                }
	            }
	        }
		} catch (Exception e) {
		} 
//		finally {
//			if (db != null) {
//				try {
//					db.close();
//				} catch (Exception e) {
//				}
//			}
//		}
	}
	
	public void update(){
		if(push){
			if(Time.currTime>lastPushTime+5*60000){
		        if(tokenToAcc!=null && tokenToAcc.size()>0){
					for(String token : tokenToAcc.keySet()){
						if(checkRule(token) && !pushedTokens.contains(token)){
							List<NotificationToken> allToken = tokenToAcc.get(token);
							if(allToken != null){
								NotificationToken nf = allToken.get(0);
							    if(nf!=null){
						    	    push(APP_ID,nf.provider, nf.accountId,
						    					nf.deviceToken, msg);
						    	    pushedTokens.add(token);
							    }
							}
						}
					}
				}
		        lastPushTime = Time.currTime;
		    }
		}
	}
	
	public boolean checkRule(String devToken){
		List<NotificationToken> list = tokenToAcc.get(devToken);
		if(list!=null){
			for(NotificationToken nt : list){
				if(Time.currentTimeMillis(Time.currTime)-nt.lastLogoutTime<3*24*3600*1000l){
				   return false;
				}
			}
		}
		return true;
	}
	
	private void processNotify(){
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				push = true;
				lastPushTime = Time.currTime;
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), 12, 0), ONEDAY, TimeUnit.MILLISECONDS);
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				push = false;
			}
		}, TimeUtil.getScheduleTimeMills(new Date(), 18, 0), ONEDAY, TimeUnit.MILLISECONDS);
	}
	
    
	
	
	public static class NotificationTokenTupleBinding extends TupleBinding<NotificationToken> {

		@Override
		public NotificationToken entryToObject(TupleInput in) {
			int accountId = in.readInt();
			String provider = in.readString();
			String deviceToken = in.readString();
			long lastLogoutTime = 0;
			try {
				lastLogoutTime = in.readLong();
			} catch (IndexOutOfBoundsException e) {
				
			}
			return new NotificationToken(accountId, provider, deviceToken,lastLogoutTime);
		}

		@Override
		public void objectToEntry(NotificationToken token, TupleOutput out) {
			out.writeInt(token.accountId);
			out.writeString(token.provider);
			out.writeString(token.deviceToken);
			out.writeLong(token.lastLogoutTime);
		}
		
	}
	
	/**
	 * 实际的绑定行为
	 * @author Jeffrey
	 *
	 */
	class BindWorker implements Runnable {
		
		String appId, provider, token;
		int accountId;
		
		public BindWorker(String appId, String provider, int accountId,String token) {
			this.appId = appId;
			this.accountId = accountId;
			this.provider = provider;
			this.token = token;
		}

		public void run() {
			if(bindRemote(appId, provider, accountId, token)) {
				bindSuccess(appId, provider, accountId, token);
			} else {
				bindFail(appId, provider, accountId, token);
			}
		}
		
	}
	
	class PushWorker implements Runnable {
		
		String appId, provider, token, message;
		int accountId;
		
		public PushWorker(String appId, String  provider, int accountId, String token, String message) {
			this.appId = appId;
			this.accountId = accountId;
			this.provider = provider;
			this.token = token;
			this.message = message;
		}

		public void run() {
			if(provider == null) { //如果provider为null，说明得到本地数据库获取token信息，如果取不到，那么直接返回
				try {
					NotificationToken nt = fetch(accountId);
					if(nt != null) { 
						doPush(appId, nt.provider, accountId, nt.deviceToken, message);
					}
				} catch (DatabaseException e) {
					log.error(e.toString(), e);
				}
				
			} else {
				doPush(appId, provider, accountId, token, message);
			}

		}
		
		
		
		private void doPush(String appId, String provider, int accountId, String token, String message) {
			if(pushRemote(appId, provider, accountId, token, message)) {
				pushSuccess(appId, provider, accountId, token, message);
			} else {
				pushFail(appId, provider, accountId, token, message);
			}
		}
		
	}

	public void dayChanged() {
		pushedTokens.clear();
	}
}