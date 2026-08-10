package peony.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import peony.service.ServiceEvent;
import peony.stat.NIMapManagerStatistics;
import ch.javasoft.util.intcoll.IntHashMap;

import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.GameAreaInfo;
import com.pip.sanguo.data.map.GameMapInfo;

/**
 * 普通非副本的地图管理器。
 * @author Jeffrey
 * 
 * Light: 多线程模型下，非副本地图管理器按阵营分成4个：中立、魏国、蜀国、吴国。
 * Light: 加入每个场景的最大容量功能，可以为一个场景自动分线建立多个副本，以保证一个场景中玩家不至于太多，避免抢怪问题。
 */
public class NoInstanceVMapManager implements VMapManager{
	private static Logger log = Logger.getLogger(NoInstanceVMapManager.class);
	
	/*
	 * 场景ID到场景对象的映射表。一个场景ID可能对应多个场景副本。
	 */
	protected IntHashMap<VMap[]> maps = new IntHashMap<VMap[]>();
	/*
	 * 全部场景的列表，用于遍历。
	 */
	protected List<VMap> mapList = new ArrayList<VMap>();
	/*
	 * 记录玩家和场景实例的关系，让一个玩家不会在进入同一个场景时被分配到不同的副本。key是一个long，高32位是playerid，
	 * 低32位是场景ID，value是VMap实例。
	 */
	protected Map<Long, VMap> playerMap = new HashMap<Long, VMap>();
	/*
	 * 实例ID生成器，对于有人数限制的场景需要设置副本ID。
	 */
	protected static final AtomicInteger ids = new AtomicInteger(0);
	
	protected World world;
	protected DieCallback dieCallback;
	protected int faction;
	
	/**
	 * 为一个阵营创建非副本地图管理器。
	 * @param world
	 * @param faction
	 */
	public NoInstanceVMapManager(World world, int faction) throws Exception {
		this.world = world;
		this.faction = faction;
		world.addVMapManager(this);
		dieCallback = new NormalDieCallback();
	}
	
	/**
	 * 取得本地图管理器对应的阵营。
	 * @return 0 - 中立，1 -魏国，2 - 蜀国，3 - 吴国
	 */
	public int getFaction() {
		return faction;
	}
	
	/**
	 * 创建一个地图。初始化或reload时调用。
	 * @param mi
	 */
	public void createNewMap(GameMapInfo mi) {
        if (world.getVMapManager(mi.getGlobalID()) == null) { //没有MapManager说明不是副本
            VMap map = VMapUtil.create(this,world, mi.getGlobalID(),Server.server.revision);
            if (mi.maxPlayer > 0) {
            	map.forceInstanceID = ids.incrementAndGet();
            }
            maps.put(map.getId(), new VMap[] { map });
            mapList.add(map);
            world.registerVMapManager(map.getId(), this);
            Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_ADDED, map));
        }
	}
	
	/**
	 * 把一个玩家加入到一个场景中。
	 * 如果要加入的场景已经满员，则需要创建新的场景来放入此用户。
	 */
	public VMap addToMap(Player player, int mapId, int x, int y, boolean check) throws VMapException {
		// 处理没有更换场景的特殊情况
		if (player.getVMap() != null && player.map.id == mapId) {
			player.unMoving();
			player.move(x, y);
			return player.getVMap();
		}
		
		// 查找可选择的目标场景副本
		VMap[] instances = maps.get(mapId);
		if (instances == null) {
			throw new VMapException("目標場景不存在");
		}
		
		// 玩家可以移动如此场景之前，先从旧场景中移除
		player.removeFromMap();
		
		// 如果目标场景不限制玩家数上限，则直接加入第一个副本
		int limit = instances[0].mapDef.mapInfo.maxPlayer;
		if (limit == 0) {
			player.addToMap(instances[0], x, y);
			return instances[0];
		}
		
		// 如果有队友在目标场景中，则直接进入队友的副本
		if (player.party != null) {
			VMap foundMap = null;
			synchronized (player.party) {
				int size = player.party.members.size();
				for (int i = 0; i < size; i++) {
					Player tp = player.party.members.get(i).player;
					if (tp == player) {
						continue;
					}
					if (tp.map.id == mapId) {
						foundMap = tp.getVMap();
						break;
					}
				}
			}
			if (foundMap != null) {
				player.addToMap(foundMap, x, y);
				playerMap.put(makeKey(player.id, mapId), foundMap);
				return foundMap;
			}
		}
		
		// 如果玩家有曾经进入过此场景的记录，则进入上次的副本
		long key = makeKey(player.id, mapId);
		if (playerMap.containsKey(key)) {
			VMap map = playerMap.get(key);
			player.addToMap(map, x, y);
			return map;
		}
		
		// 新进用户，如果第一个副本没有满，则加入第一个副本
		if (instances[0].playerCount < limit) {
			player.addToMap(instances[0], x, y);
			playerMap.put(key, instances[0]);
			return instances[0];
		}
		
		// 如果第一个副本满了，则尝试查找或者创建一个新的副本来容纳此用户
		VMap map = getNextInstance(mapId);
		player.addToMap(map, x, y);
		playerMap.put(key, map);
		return map;
	}
	
	/*
	 * 查找或创建一个副本来容纳新用户。
	 * @param mapId
	 * @return
	 */
	private VMap getNextInstance(int mapId) {
		VMap[] instances = maps.get(mapId);
		
		// 如果第一个副本没有满，返回第一个副本
		int limit = instances[0].mapDef.mapInfo.maxPlayer;
		if (instances[0].playerCount < limit) {
			return instances[0];
		}
		
		// 查找第一个没有满且不为空的副本
		int nextInd;
		for (nextInd = 1; nextInd < instances.length; nextInd++) {
			if (instances[nextInd].playerCount > 0 && instances[nextInd].playerCount < limit) {
				break;
			}
		}
		if (nextInd >= instances.length) {
			// 如果没有未满且不空的副本，则选择一个空副本
			for (nextInd = 1; nextInd < instances.length; nextInd++) {
				if (instances[nextInd].playerCount < limit) {
					break;
				}
			}
		}
		if (nextInd >= instances.length) {
			// 如果还找不到，说明所有副本都满了，创建一个新副本，放到数组第一位，把新用户加入这个新副本中
			VMap[] newArr = new VMap[instances.length + 1];
			newArr[0] = VMapUtil.create(this,world, mapId, Server.server.revision);
			newArr[0].forceInstanceID = ids.incrementAndGet();
			System.arraycopy(instances, 0, newArr, 1, instances.length);
			maps.put(mapId, newArr);
			mapList.add(newArr[0]);
			Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_ADDED, newArr[0]));
            return newArr[0];
		} else {
			// 如果找到一个没满的副本，则循环移动数组，把这个新副本挪到数组第一位，并把新用户加入此副本中
			VMap[] newArr = new VMap[instances.length];
			System.arraycopy(instances, nextInd, newArr, 0, instances.length - nextInd);
			System.arraycopy(instances, 0, newArr, instances.length - nextInd, nextInd);
			maps.put(mapId, newArr);
            return newArr[0];
		}
	}
	
	private long makeKey(int playerId, int mapId) {
		return (((long)playerId) << 32) | (long)mapId;
	}
	
	/**
	 * 查询某个场景对应的所有场景副本。
	 * @param mapId
	 * @return
	 */
	public VMap[] getVMaps(int mapId) {
		return maps.get(mapId);
	}
	
	/**
	 * 玩家离开场景前的通知调用。
	 */
	public void removeFromMap(Player player) {
	}
	
	/*
	 * 关闭一个空副本。如果此副本是本场景副本队列中的第一个，则不会关闭。
	 * 如果副本被关闭，返回true。
	 */
	private boolean deleteInstance(VMap map) {
		VMap[] instances = maps.get(map.getMapID());
		if (instances == null || instances[0] == map) {
			return false;
		}
		int ind = -1;
		for (int i = 0; i < instances.length; i++) {
			if (instances[i] == map) {
				ind = i;
				break;
			}
		}
		
		// 如果此地图在列表中存在（正常情况），则将其从列表中移除
		if (ind != -1) {
			VMap[] newArr = new VMap[instances.length - 1];
			System.arraycopy(instances, 0, newArr, 0, ind);
			System.arraycopy(instances, ind + 1, newArr, ind, instances.length - ind - 1);
			maps.put(map.getMapID(), newArr);
		}
		
		// 清理此副本中的所有对象
		for (GameObject o : map.instanceid2objects.values()) {
			if (o.type == GameObject.TYPE_PLAYER) {
				log.info("[NOINSTANCEDESTORYERROR]");
			} else {
				ObjectAccessor.removeGameObject(o);
			}
		}
		
		// 清理所有玩家进入此副本的记录
		Iterator<Long> itor = playerMap.keySet().iterator();
		while (itor.hasNext()) {
			Long key = itor.next();
			if (playerMap.get(key) == map) {
				itor.remove();
			}
		}
		
		// 广播地图移除事件
		Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_REMOVED, map));
		return true;
	}

	/**
	 * 更新所有场景中的所有对象。
	 */
	public void update(int diff) {
		NIMapManagerStatistics stat = new NIMapManagerStatistics();
		stat.mapCount = mapList.size();
		long t = System.nanoTime();
		for (int i = 0; i < mapList.size(); i++) {
			VMap map = mapList.get(i);
			map.update(diff);
			
			// 如果一个副本已经空了超过5分钟，并且它不是本场景副本队列中的第一个，则关闭它
			if (map.playerCount == 0 && map.lastActiveTime < Time.currTime - 300000) {
				if (deleteInstance(map)) {
					mapList.remove(i);
					i--;
				}
			}
		}
		stat.mapCycleTime = System.nanoTime()-t;
		Server.server.getServiceRegistry().getStatisticsService().addNIMapManagerStatistics(stat);
	}
	
	public DieCallback dieCallback(){
		return dieCallback;
	}
	
	public CreatureDieCallback creatureDieCallback(){
		return null;
	}
	
	public MoveCallback moveCallback(){
		return null;
	}
	
	/**
     * 处理地图数据变化，尽可能地更新已有对象的属性。
     */
    public void mapChanged(GameMapDefinition mapDef) {
    	for (int i = 0; i < mapList.size(); i++) {
    		VMap map = mapList.get(i);
            if (map.mapDef.mapInfo.getGlobalID() == mapDef.mapInfo.getGlobalID()) {
                map.mapDef = mapDef;
                map.mapChanged();
            }
        }
    }
    
    public void outPrison(Player p){
    	OutPrisonUtil.outPrison(p);
    }
}
