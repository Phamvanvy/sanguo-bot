package peony.service.player;

import java.text.MessageFormat;
import java.util.Date;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.log4j.Logger;
import peony.db.DBService;
import peony.db.GetUnreadMailCall;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.PlayerUtil;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;
import peony.game.changed.ChangedItem;
import peony.game.chat.ChatService;
import peony.game.itemeffect.ActivityItemEffect;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.account.Account;
import peony.service.account.AccountProperty;
import peony.service.account.ChargeActivityService;
import peony.service.account.FirstCharge;
import peony.service.account.RecordChargeService;
import peony.service.activity.NewServerAreaActivity;
import peony.service.friend.RelationList;
import peony.service.stat.StatService;
import peony.service.version.ModelService;
import peony.service.welfare.WelfareService;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class PlayerService implements Service, ServiceEventListener {
	protected static final Logger log = Logger.getLogger(PlayerService.class); 
	
	
	public CacheManager cacheManager = CacheManager.create();
	
	public final Cache cache = new Cache("recycle",2000,false,false,5*60*1000L,10*60*1000L);
	
	public static int[] newComerMap = {1442,1410,1426,1552,2176};//新手村地图
	
	public static int[] freshMap = {401,529,496};
	
	public static long baseMuteAccountTime = 3600 * 1000L;
	
	public static String[] oldAndroidMods = {"AndroidNew","AndroidLargeNew","iOSNewUI",
		"iOSNewUILarge","Nokia5800New"};
	
	public static String newAndroidUIVersion = "4.0";
	
	public static boolean isOldAndroidMod(String model){
		if(Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_CMCC) 
				|| Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_UNICOM))
			return false;
		for(String m : oldAndroidMods){
			if(model.equalsIgnoreCase(m))
				return true;
		}
		return false;
	}
	
	public PlayerService(){
		cacheManager.addCache(cache);
	}
	
	public void startup() {
		Server.server.getEventManager().registerListener(this);
	}

	public void notifyPlayerUpLevel(int oldLevel,Player player){
		Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_PLAYER_LEVELUP, player,oldLevel));
	}
	
	public void notifyPlayerLoaded(Player player,int type){
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_PLAYER_LOADED, player, type));
	}
	
	public void notifyAccountPropertyLoaded(Player player){
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_ACCOUNTPROPERTY_LOADED, player));
	}
	
	
	public void notifyPlayerCreated(Player player){
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_PLAYER_CREATED, player));
	}
	
	public void notifyPlayerLogined(Player player){
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_PLAYER_LOGINED, player));
	}
	
	public void notifyPlayerLogout(Player player){
		Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_PLAYER_LOGOUTED, player));
	}
	
	public void notifyPlayerFirstLoad(Player player){
		Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_PLAYER_FIRSTLOAD, player));
	}
	
	public void notifyPlayerSaved(Player player){
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_PLAYER_SAVED, player));
	}
	
	public void logined(Player player){
		notifyPlayerLogined(player);
	}
	
	public void firstLoad(Player player){
		notifyPlayerFirstLoad(player);
	}
	
	public void shutdown() {
		if(Server.isStepServer)
			return;
		log.info("SHUTDOWN SAVE PLAYERS");
		DBService dbService = Server.server.getServiceRegistry().getDbService();
		Date time = new Date();
		for (Player p : ObjectAccessor.players.values()) {
			try {
				p.lastLogoutTime = time;
				p.lastLogoutTimeMills = time.getTime();
				p.pool.setInt(Player.PROPERTY_ONLINETIMETODY, p.onlineTimeToday);
				if(p.relations!=null && p.relations.apprenticeList == null){
					p.relations.apprenticeList = new RelationList();
				}
				p.recordLogOutHpMp();
				dbService.playerDAO.updateEntity(p);
				Server.server.getServiceRegistry().getAccountDepotService().saveAccountDepot(p.accountId);
				FirstCharge firstCharge = Server.server.getServiceRegistry().getChargeActivityService().getFirstCharge(p.accountId, false);
				if(firstCharge!=null){
					Server.server.getServiceRegistry().getDbService().firstChargeDao.updateEntity(firstCharge);
				}
				AccountProperty ap = Server.server.getServiceRegistry().getVipPrivilegeService().getAccountProperty(p.accountId);
				if(ap!=null){
					Server.server.getServiceRegistry().getDbService().accountPropertyDao.updateEntity(ap);
				}
				LogUtil.logSavePlayer(p);
			} catch (Exception ex) {
				log.warn("shutdown() - exception ignored", ex); //$NON-NLS-1$
			}
		}
		log.info("SHUTDOWN SAVE PLAYERS OK");
		Server.server.getEventManager().unregisterListener(this);
	}
	
	public Player getFromCache(int id){
		Element el = cache.get(id);
		if(el!=null){
			return (Player)el.getObjectValue();
		}
		return null;
	}
	
	public void mute(int playerId, long time) {
		Player player = loadPlayerSilent(playerId);
		if (player != null) {
			LogUtil.logMute(player, time);
			
			if(player.session!=null){
				player.session.close();
			}
			Database db = Server.server.getServiceRegistry()
					.getSleepyCatService().kickedPlayersDB;
			DatabaseEntry key = new DatabaseEntry();
			IntegerBinding.intToEntry(playerId, key);
			DatabaseEntry data = new DatabaseEntry();
			LongBinding.longToEntry(time, data);
			try {
				db.put(null, key, data);
			} catch (DatabaseException e) {
				log.warn("mute", e);
			}
		}
	}
	
	public void muteAccount(Player player, long time, int currentCount) {
		if(player!=null){
			int accountId = player.accountId;
			LogUtil.logAccountMute(player, time);
			if(player.session!=null){
				player.session.close();
			}
			Database db = Server.server.getServiceRegistry().getSleepyCatService().kickAccountDB;
			DatabaseEntry key = new DatabaseEntry();
			IntegerBinding.intToEntry(accountId, key);
			DatabaseEntry data = new DatabaseEntry();
			LongBinding.longToEntry(time, data);
			try {
				db.put(null, key, data);
			} catch (DatabaseException e) {
				log.warn("muteaccount", e);
			}
			key = new DatabaseEntry();
			StringBinding.stringToEntry("COUNT-" + accountId, key);
			data = new DatabaseEntry();
			IntegerBinding.intToEntry(currentCount, data);
			try {
				db.put(null, key, data);
			} catch (DatabaseException e) {
				log.warn("muteaccount", e);
			}
		}
	}
	
	public void removeMute(int playerId){
		Database db = Server.server.getServiceRegistry().getSleepyCatService().kickedPlayersDB;
		DatabaseEntry key = new DatabaseEntry();
		IntegerBinding.intToEntry(playerId, key);
		try {
			db.delete(null, key);
		} catch (DatabaseException e) {
			log.warn("removemute",e);
		}
	}
	
	public void removeAccountMute(int accountId){
		Database db = Server.server.getServiceRegistry().getSleepyCatService().kickAccountDB;
		DatabaseEntry key = new DatabaseEntry();
		IntegerBinding.intToEntry(accountId, key);
		try {
			db.delete(null, key);
		} catch (DatabaseException e) {
			log.warn("removeaccountmute",e);
		}
		key = new DatabaseEntry();
		StringBinding.stringToEntry("COUNT-" + accountId, key);
		try {
			db.delete(null, key);
		} catch (DatabaseException e) {
			log.warn("removeaccountmute",e);
		}
	}
	
	public boolean isMuted(int playerId){
		Database db = Server.server.getServiceRegistry().getSleepyCatService().kickedPlayersDB;
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		IntegerBinding.intToEntry(playerId, key);
		try {
			if(db.get(null,key,data,LockMode.DEFAULT)==OperationStatus.SUCCESS){
				long time = LongBinding.entryToLong(data);
				if(time<System.currentTimeMillis()){
					db.delete(null, key);
					return false;
				}
				return true;
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean isAccountMuted(int accountId){
		Database db = Server.server.getServiceRegistry().getSleepyCatService().kickAccountDB;
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		IntegerBinding.intToEntry(accountId, key);
		try {
			if(db.get(null,key,data,LockMode.DEFAULT)==OperationStatus.SUCCESS){
				long time = LongBinding.entryToLong(data);
				if(time<System.currentTimeMillis()){
					db.delete(null, key);
					return false;
				}
				return true;
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public int getAccountMuteCount(int accountId){
		Database db = Server.server.getServiceRegistry().getSleepyCatService().kickAccountDB;
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		StringBinding.stringToEntry("COUNT-" + accountId, key);
		try {
			if(db.get(null,key,data,LockMode.DEFAULT)==OperationStatus.SUCCESS){
				int count = IntegerBinding.entryToInt(data);
				return count;
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public void savePlayer(Player p){
		if(Server.isStepServer)
			return;
		if(p.battleType==Player.TYPE_ASYNC_PLAYER || p.battleIngoPlayer)
			return;
		log.info("[DEBUGSAVEPLAYER]ID["+p.id+"]");
		Server.server.getServiceRegistry().getDbService().playerDAO.updateEntity(p);
		FirstCharge firstCharge = Server.server.getServiceRegistry().getChargeActivityService().getFirstCharge(p.accountId, false);
		if(firstCharge!=null){
			Server.server.getServiceRegistry().getDbService().firstChargeDao.updateEntity(firstCharge);
		}
		AccountProperty ap = Server.server.getServiceRegistry().getVipPrivilegeService().getAccountProperty(p.accountId);
		if(ap!=null){
			Server.server.getServiceRegistry().getDbService().accountPropertyDao.updateEntity(ap);
		}
		LogUtil.logSavePlayer(p);
		notifyPlayerSaved(p);
	}
	
	public Player loadPlayerSilent(int actorId){
		Player p = ObjectAccessor.getPlayer(actorId);
		if(p==null){
			p = getFromCache(actorId);
			if(p==null){
				DBService dbService = Server.server.getServiceRegistry().getDbService();
				p = dbService.playerDAO.getPlayerById(actorId);
				p.instanceId = p.id;
				p.initBuffs();
				p.initPlayerBooks();
				p.loadInit(false);
				p.serivce = this;
				p.setSystemState(Player.SYSTEMSTATE_LOAD);
				cache.put(new Element(p.id,p));
				notifyPlayerLoaded(p,ServiceEvent.PLAYER_LOAD_SILENT);
			}
		}
		return p;
	}
	
	public Player loadAsyncPlayerSilent(int actorId){
		Player p = ObjectAccessor.getPlayer(actorId);
		if(p==null){
			p = getFromCache(actorId);
			if(p==null){
				DBService dbService = Server.server.getServiceRegistry().getDbService();
				p = dbService.playerDAO.getPlayerById(actorId);
				p.instanceId = p.id;
				p.initBuffs();
				p.initPlayerBooks();
				p.loadInit(false);
				p.serivce = this;
				p.setSystemState(Player.SYSTEMSTATE_LOAD);
			}
		}
		return p;
	}
	
	public Player loadPlayer(int accountId,int actorId) {
		Player p = ObjectAccessor.getPlayer(actorId);
		if(p!=null){
			if (p.accountId == accountId) {
				p.loadInit(false);
				notifyPlayerLoaded(p, ServiceEvent.PLAYER_LOAD_ACCESSOR);
				return p;
			} else {
				return null;
			}
		}
		p = getFromCache(actorId);
		if(p!=null){
			if(p.exist==0){
				return null;
			}
			if (p.accountId == accountId) {
				p.loadInit(true);
				notifyPlayerLoaded(p, ServiceEvent.PLAYER_LOAD_CACHE);
				return p;
			} else {
				return null;
			}
		}
		DBService dbService = Server.server.getServiceRegistry().getDbService();
		try {
			p = dbService.playerDAO.getPlayerById(actorId);
			if(p==null){
				log.info("[NOPLAYER]ID["+actorId+"]");
			}
			p.instanceId = p.id;
			p.skillsPatch();
			p.initBuffs();
			p.initPlayerBooks();
			p.loadInit(true);
			p.asmVm.optimizeVarStores();
			if(p!=null&&p.accountId==accountId){
				p.serivce = this;
				p.setSystemState(Player.SYSTEMSTATE_LOAD);
				notifyPlayerLoaded(p,ServiceEvent.PLAYER_LOAD_DB);
				p.changed.clean();
				cache.put(new Element(p.id,p));
				
//				BindImoneyDao bindImoneyDao = Server.server.getServiceRegistry().getDbService().bindImoneyDao;
//				BindImoney bindImoney = bindImoneyDao.getBindImoneyByAccountId(accountId);
//				p.getAccount().setBindImoney(bindImoney);

				return p;
			}
			return null;
		} catch (Exception ex) {
			log.error(ex, ex);
			return null;
		}
	}
	
	public boolean createPlayer(Player player){
		DBService dbService = Server.server.getServiceRegistry().getDbService();
		try {
			player.refreshProperties(false);
			player.lastLoginTime = new Date();
			player.lastLogoutTime = new Date();
			player.lastLoginTimeMills = new Date().getTime();
			player.lastLogoutTimeMills = new Date().getTime();
			player.initPlayerBooks();
			dbService.playerDAO.newEntity(player);
			player.instanceId = player.id;
			player.serivce = this;
			player.setSystemState(Player.SYSTEMSTATE_LOAD);
			cache.put(new Element(player.id,player));
			notifyPlayerCreated(player);
			notifyPlayerLoaded(player,ServiceEvent.PLAYER_LOAD_DB);
			player.enterMap = System.currentTimeMillis();//创建角色时记录新手村地图load开始时间
			player.salaryDay = Time.day;
			return true;
		} catch (Exception e) {
			log.error(e, e);
			return false;
		}
	}
	
	public void setDefault(Player player, String jvmCode) {
		player.createTime = new Date();
		
		// 根据机型设置缺省配置
		ModelService ms = Server.server.getServiceRegistry().getModelService();
		player.config = ms.getModelConfig(jvmCode).toBytes();
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD,
				ServiceEvent.EVENT_MAP_PLAYER_LOADED,
				ServiceEvent.EVENT_PLAYER_LEVELUP,
				ServiceEvent.EVENT_MAP_PLAYER_ADDED
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			processPlayerFirstLoad((Player)event.param1);
			break;
		case ServiceEvent.EVENT_MAP_PLAYER_LOADED:
			processPlayerMapLoad((VMap)event.param1,(Player)event.param2);
			break;
		case ServiceEvent.EVENT_PLAYER_LEVELUP:
			notifyPlayerChangeName((Player)event.param1);
			break;
		case ServiceEvent.EVENT_MAP_PLAYER_ADDED:
			notifyClientFreshEnter((VMap)event.param1,(Player)event.param2);
			break;
		}
	}
	
	protected void notifyClientFreshEnter(VMap map,Player player){
		if(player!=null && map!=null){
			if(StatService.isInArray(freshMap, map.getId())!=-1 &&
					player.pool.getInt(Player.PROPERTY_FRESH_ENTERMAP, 0)==0){
				if(player.changed!=null){
					player.addIntPropertyChangedItem(ChangedItem.FRESHENTERMAP, 1, false,true);
					player.pool.setInt(Player.PROPERTY_FRESH_ENTERMAP, 1);
				}
			}
		}
	}
	
	protected void notifyPlayerChangeName(Player player){
		if(player!=null){
			if(!Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_TW) && player.level==19 && player.name.contains("游客"))
				player.message(-1, "恭喜你到达19级，不过我看你的名字还是游客，要不要起一个拉风一点的名字呢，快去系统服务中的其他设置中修改吧。", -1, -1);
		}
	}
	
	protected void processPlayerMapLoad(VMap map,Player p){
		if(p!=null){
			if(StatService.isInArray(newComerMap, map.getId())!=-1){
				//新手村玩家进入地图打印日志
				LogUtil.logEnterVMap(p, System.currentTimeMillis()-p.enterMap);
			}
		}
	}
	
	protected void processPlayerFirstLoad(Player player){
		if(player!=null){
			//Android新界面和iPhone新界面特殊处理装备
			try {
				Account account = player.getAccount();
				if(account!=null && player.level==1 && player.pool.getInt("FIRSTLOAD_EQU", 0)==0){
					String mod = account.getModel().trim();
					String version = account.getVersion().id.trim();
					if(((mod.equals("Android") || mod.equals("AndroidLarge")) 
							&& version.equals("3.0")) || mod.equals("iOSNewUI") || mod.equals("iOSNewUILarge")){
						GameItem equ = ObjectAccessor.createGameItem(PlayerUtil.INIT_EQUIPMENT[player.clazz]);
						PlayerTransaction tx = player.newTransaction("ANDROIDIPHONENEW");
						try {
							player.bag.addGameItemComplete(equ, 1, tx, false);
							tx.commit();
						} catch (NoEnoughSpaceException e) {
							tx.rollback();
						}
						player.equip(equ.template.id, equ.instanceId, -1);
						player.pool.setInt("FIRSTLOAD_EQU", 1);
					}
				}
				if(!Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_TW) && account!=null){
					String mod = account.getModel().trim();
					if(isOldAndroidMod(mod)){
						ChatService chatService = Server.server.getServiceRegistry().getChatService();
						chatService.sendPrivateMessage(player.id, MessageFormat.format("您当前的版本过旧,为保证您获得更好的游戏体验,请更新客户端,最新版本号为{0}", newAndroidUIVersion));
					}
				}
			} catch (Exception e) {
			}
			if(player.cardPunch!=null){
				player.cardPunch.updatePunch();
			}
			if(Server.isAppSection){
				Packet pt = new Packet(OpCode.APP_SECTION_SERVER);
				player.send(pt);
			}
			if(player.books!=null){
				player.books.processBookRead();
			}
			
			ActivityItemEffect.removeProperty(player);
			
			if(player.isReachLimitTotal()){
				Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id,  "您的总工资已经达到了上限，如果不及时消耗将无法继续获得工资,请尽快去主城工资商人处兑换奖励吧。");
			}
			
			//玩家登陆未读邮件提醒
			Server.server.getServiceRegistry().getDbService().
			                           schedule(new GetUnreadMailCall(player==null ? null : player.session, player));
			WelfareService welfareService = Server.server.getServiceRegistry().getWelfareService();
			if(welfareService.hasWelfareReady(player)){
				String message = "您尚有未领取的每日福利，请及时领取，否则过了今天将不能再领取。在人物信息菜单里选择个人自传再选择每日福利，就可以领取到奖励。";
				boolean isNewUI = player.isNewUI();
				if(isNewUI){
					message = "您尚有未领取的每日福利，请及时领取，否则过了今天将不能再领取。点击菜单，选择并点击个人成就，在界面右下角点击每日福利，就可以领取到奖励。";
				}
				Account account = player.getAccount();
				if(account!=null){
					String mod = null;
					if(account.getUiModel()!=null)
						mod = account.getUiModel().trim();
					   if(mod !=null){
				            if(mod.equals("AndroidNew") || mod.equals("AndroidLargeNew") || mod.equals("iOSNewUI") || mod.equals("iOSNewUILarge") || mod.equals("Nokia5800New") || mod.equals("Nokia5800NewC")){
					             message = "您尚有未领取的每日福利，请及时领取，否则过了今天将不能再领取。在人物信息菜单里选择个人成就再选择每日福利，就可以领取到奖励。";
				            }
					   }
				}
				Server.server.getServiceRegistry().getChatService().
				                    sendPrivateMessage(player.id, message);
			}
			
			if(player.isNewUI() && player.propertyPoint>0){
				String msg = "您尚有未分配的属性点";
				Server.server.getServiceRegistry().getChatService().
                sendPrivateMessage(player.id, msg);
			}
			
			notifyPlayerChangeName(player);
			Server.server.getServiceRegistry().getAnniversaryService().updateAnniversaryData(player);
			
			boolean isSendPrivate = false;
			ChargeActivityService service = Server.server.getServiceRegistry().getChargeActivityService();
			FirstCharge firstCharge = service.getFirstCharge(player.accountId,false);
			if(firstCharge!=null){
				if(!firstCharge.hasGetFirstGift(ChargeActivityService.PROPERTY_FIRSTCHARGE_CHARGEANDREWARD)){
					int ammount = firstCharge.pool.getInt(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATEED,0);
					if(ammount>0){
						isSendPrivate = true;
						Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, "您有充值奖励可领取，快去充值活动界面领取奖励吧。");
				       //service.getFirstChargeReward(firstCharge, player, ammount);
					}
				}
				
				long firstChargeTime = firstCharge.pool.getLong(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATETIME, 0);
				if(firstChargeTime>0 ){
					RecordChargeService recordService = Server.server.getServiceRegistry().getRecordChargeService();
					synchronized(recordService){
						Date startTime = new Date();
						startTime.setTime(firstChargeTime);
						Date endTime = new Date();
						endTime.setTime(firstChargeTime+ChargeActivityService.FIFTEEN_DAY);
						int chargeMoney = Server.server.getServiceRegistry().getDbService().chargeDao.getAccumulateCharge(player.accountId, startTime, endTime);
						int firstMoney = firstCharge.pool.getInt(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATEED,0);
						chargeMoney += firstMoney;
						firstCharge.pool.setInt(ChargeActivityService.PROPERTY_CHARGE_TOTAL, chargeMoney);
					}
				}
				if(!service.hasGetMulGift(player.accountId)){
					if(firstChargeTime>0 ){
						if(System.currentTimeMillis() - firstChargeTime>ChargeActivityService.FIFTEEN_DAY){
							if(!isSendPrivate && service.getMulChargeMax(player.accountId) > 0){
								Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, "您有充值奖励可领取，快去充值活动界面领取奖励吧。");
							}
			    			firstCharge.pool.remove(ChargeActivityService.PROPERTY_ACCUMULATECHARGE_LASTTIME);
			    			service.addFirstCharge(player.accountId, firstCharge);
						}
					}
				}
			}
			//新区活动奖励
			NewServerAreaActivity.accountPlayReward(player);
		}
	}
	
}
