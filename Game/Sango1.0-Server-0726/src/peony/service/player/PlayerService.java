package peony.service.player;

import java.util.Date;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

import peony.db.DBService;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.version.ModelService;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class PlayerService implements Service {
	protected static final Logger log = Logger.getLogger(PlayerService.class); 
	
	
	public CacheManager cacheManager = CacheManager.create();
	
	public final Cache cache = new Cache("recycle",3000,false,false,5*60*1000L,10*60*1000L);
	
	public PlayerService(){
		cacheManager.addCache(cache);
	}
	
	public void startup() {
	}

	public void notifyPlayerUpLevel(int oldLevel,Player player){
		Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_PLAYER_LEVELUP, player,oldLevel));
	}
	
	public void notifyPlayerLoaded(Player player,int type){
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_PLAYER_LOADED, player, type));
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
		log.info("SHUTDOWN SAVE PLAYERS");
		DBService dbService = Server.server.getServiceRegistry().getDbService();
		Date time = new Date();
		for (Player p : ObjectAccessor.players.values()) {
			try {
				p.lastLogoutTime = time;
				dbService.playerDAO.updateEntity(p);
				Server.server.getServiceRegistry().getAccountDepotService().saveAccountDepot(p.accountId);
				LogUtil.logSavePlayer(p);
			} catch (Exception ex) {
				log.warn("shutdown() - exception ignored", ex); //$NON-NLS-1$
			}
		}
		log.info("SHUTDOWN SAVE PLAYERS OK");
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
	
	public void savePlayer(Player p){
		Server.server.getServiceRegistry().getDbService().playerDAO.updateEntity(p);
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
				p.loadInit(false);
				p.serivce = this;
				p.setSystemState(Player.SYSTEMSTATE_LOAD);
				cache.put(new Element(p.id,p));
				notifyPlayerLoaded(p,ServiceEvent.PLAYER_LOAD_SILENT);
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
			p.instanceId = p.id;
			p.skillsPatch();
			p.initBuffs();
			p.loadInit(true);
			if(p!=null&&p.accountId==accountId){
				p.serivce = this;
				p.setSystemState(Player.SYSTEMSTATE_LOAD);
				notifyPlayerLoaded(p,ServiceEvent.PLAYER_LOAD_DB);
				p.changed.clean();
				cache.put(new Element(p.id,p));
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
			dbService.playerDAO.newEntity(player);
			player.instanceId = player.id;
			player.serivce = this;
			player.setSystemState(Player.SYSTEMSTATE_LOAD);
			cache.put(new Element(player.id,player));
			notifyPlayerCreated(player);
			notifyPlayerLoaded(player,ServiceEvent.PLAYER_LOAD_DB);
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
}
