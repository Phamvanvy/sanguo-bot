package peony.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import com.pip.sanguo.data.DataChangeHandler;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.GameAreaInfo;
import com.pip.sanguo.data.map.GameMapInfo;

import peony.common.AsyncCall;
import peony.game.map.VMapUpdater;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.stat.WorldStatistics;
import ch.javasoft.util.intcoll.IntHashMap;

public class World implements Service, ServiceEventListener, DataChangeHandler {
	
	private static final Logger log = Logger.getLogger(World.class);

	ConcurrentLinkedQueue<ClientSession> sessions = new ConcurrentLinkedQueue<ClientSession>();
	
	List<VMapManager> managers = new ArrayList<VMapManager>();
	List<VMapUpdater> managerUpdaters = new ArrayList<VMapUpdater>();
	IntHashMap<VMapManager> mapid2managers = new IntHashMap<VMapManager>();
	protected ConcurrentLinkedQueue<AsyncCall> calls = new ConcurrentLinkedQueue<AsyncCall>();

	public DieCallback dieCallback;
	
	public World(){
		dieCallback = new NormalDieCallback();
	}
	
	public void addVMapManager(VMapManager manager){
		managers.add(manager);
		managerUpdaters.add(new VMapUpdater(this, manager));
	}
	
	public void registerVMapManager(int mapId,VMapManager manager){
		mapid2managers.put(mapId, manager);
	}
	
	public VMapManager getVMapManager(int mapId){
		return mapid2managers.get(mapId);
	}
	
	public void startup() {
		Server.server.getEventManager().registerListener(this);
	}
	
	public void shutdown() {
		
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

	void notifyPlayerAddedToMap(VMap map,Player player) {
		Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_PLAYER_ADDED, map, player));
	}
	
	void notifyPlayerRemoveFromMap(VMap map,Player player){
		Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_PLAYER_REMOVED, map, player));
	}
	
	void notifyPlayerLoadingFinish(VMap map,Player player){
		Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_PLAYER_LOADED, map, player));
	}
	
	public void notifyMapAdded(VMap map){
		Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_ADDED, map));
	}
	
	public void notifyMapRemoved(VMap map){
		Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_REMOVED, map));
	}
	
	
	public void update(int diff){
		WorldStatistics stat = new WorldStatistics();
		int sessionCount = 0;
		long t = System.nanoTime();
		
		// 处理所有未登录角色（未加入场景）的会话上的请求，已登录角色的会话请求处理交给场景处理线程完成
		Iterator<ClientSession> ite = sessions.iterator();
		while (ite.hasNext()) {
			sessionCount++;
			ClientSession s = ite.next();
			if (s.getClient() != null && s.getClient() instanceof Player) {
				continue;
			}
			if (s.update(diff)) {
				ite.remove();
			}
		}
		stat.sessionCount = sessionCount;
		long t1 = System.nanoTime();
		stat.sessionCycleTime = t1 -t;
		
		// 启动所有地图管理器的处理线程，同步处理所有对象的循环
		int size = managerUpdaters.size();
		for (int i = 0; i < size; i++) {
			managerUpdaters.get(i).update(diff);
		}
		
		// 等待所有地图处理线程完成执行
		while (true) {
			synchronized (this) {
				try {
					wait(20);
				} catch (InterruptedException e) {
				}
			}
			boolean over = true;
			for (int i = 0; i < size; i++) {
				if (managerUpdaters.get(i).isRunning()) {
					over = false;
					break;
				}
			}
			if (over) {
				break;
			}
		}

		// 执行异步调用（主要是切换地图的调用）
		Iterator<AsyncCall> iteCalls = calls.iterator();
		while (iteCalls.hasNext()) {
			try {
				iteCalls.next().callFinish();
			} catch(Exception e) {
				log.error(e, e);
			}
			iteCalls.remove();
		}
		
		stat.worldManagerCycleTime = System.nanoTime() - t1;
		Server.server.getServiceRegistry().getStatisticsService().addWorldStatistics(stat);
	}
	
	/**
	 * 把一个玩家加入新场景。由于场景管理器的多线程属性。所以，此方法只允许通过World的异步调用(World.schedule)、
	 * 在PlayerLoadCall中或VMapManager.addToMap中调用。
	 * @param player
	 * @param mapId
	 * @param x
	 * @param y
	 * @param check
	 * @return
	 * @throws VMapException
	 */
	public VMap addPlayerToMap(Player player,int mapId,int x,int y,boolean check) throws VMapException{
		VMapManager manager = mapid2managers.get(mapId);
		if(manager!=null){
			return manager.addToMap(player, mapId,x,y,check);
		}
		return null;
	}
	
	
	public void sessionAdded(ClientSession session) {
		sessions.add(session);
	}

	public void sessionRemoved(ClientSession session) {
//		log.debug("session removed");
		sessions.remove(session);
//		Player player = (Player)session.getClient();
//		if(player!=null){
//			player.systemState = Player.SYSTEMSTATE_DISCONNECTED;
//			player.removeFromMap();
//			
//		}
	}
	
	private NoInstanceVMapManager getDefaultManager(int faction) {
	    NoInstanceVMapManager mgr = null;
        for (VMapManager vmgr : managers) {
            if (vmgr instanceof NoInstanceVMapManager) {
            	NoInstanceVMapManager thismgr = (NoInstanceVMapManager)vmgr;
            	if (thismgr.getFaction() == faction) {
            		return thismgr;
            	} else if (thismgr.getFaction() == GameObject.FACTION_NEUTRAL) {
            		mgr = thismgr;
            	}
            }
        }
        return mgr;
	}
	
	/**
	 * 添加一个异步调用请求，这个请求将会在每个cycle的最后执行。
	 */
	public void schedule(AsyncCall call) {
		calls.add(call);
	}

	/*
	 * 载入所有关卡地图，并为所有地图创建VMap对象。
	 * @throws Exception
	 */
	protected void loadStages() throws Exception {
		List gas = Server.server.getServiceRegistry().getDataService().data
				.getDataListByType(GameArea.class);
		Iterator ite = gas.iterator();
		while (ite.hasNext()) {
			GameArea area = (GameArea) ite.next();
			addGameArea(area);
		}
	}
	
	/*
	 * 为一个关卡的所有地图创建VMap对象。
	 */
	protected void addGameArea(GameArea area) {
		GameAreaInfo info = area.getAreaInfo();
        for (GameMapInfo mi : info.maps) {
        	NoInstanceVMapManager mgr = getDefaultManager(mi.faction == null ? GameObject.FACTION_NEUTRAL : mi.faction.id);
        	mgr.createNewMap(mi);
        }
	}
	
    /**
     * 添加新对象通知。
     * @param obj 新添加的对象
     */
    public void dataObjectAdded(DataObject obj) {
        if (obj instanceof GameArea) {
            // 添加新的关卡，总是作为非副本处理
            VMapUtil.registerGameArea((GameArea)obj);
            addGameArea((GameArea)obj);
        }
    }
    
    /**
     * 对象被删除通知。
     * @param obj 被删除的老对象
     */
    public void dataObjectRemoved(DataObject obj) {
        if (obj instanceof GameArea) {
            // 无法处理关卡被删除的情况，忽略
        }
    }
    
    /**
     * 对象即将被修改通知。
     * @param obj 修改前的对象
     */
    public void dataObjectChanging(DataObject obj) {
    }
    
    /**
     * 对象被修改通知。
     * @param newobj 修改后的新对象
     */
    public void dataObjectChanged(DataObject newobj) {
        if (newobj instanceof GameArea) {
            // 关卡内容修改，需要做的处理包括：
            // 1. 更新对应的GameMapDefinition里的GameMap和GameMapInfo引用
            // 2. 找出所有属于此关卡的VMap对象，检查其中的GameMapObject是否修改、删除或添加
            List<GameMapDefinition> added = new ArrayList<GameMapDefinition>();
            List<GameMapDefinition> changed = new ArrayList<GameMapDefinition>();
            List<GameMapDefinition> removed = new ArrayList<GameMapDefinition>();
            VMapUtil.updateGameArea((GameArea)newobj, added, changed, removed);
            
            // 创建新的地图（副本不处理）
            for (GameMapDefinition def : added) {
                getDefaultManager(def.mapInfo.faction.id).createNewMap(def.mapInfo);
            }
            
            // 删除地图（暂时没有好的处理办法，不处理）
            
            // 修改地图
            for (GameMapDefinition def : changed) {
                for (VMapManager mgr : managers) {
                    mgr.mapChanged(def);
                }
            }
        }
    }
}
