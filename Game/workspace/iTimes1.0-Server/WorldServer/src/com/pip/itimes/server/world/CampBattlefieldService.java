package com.pip.itimes.server.world;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.bean.Battlefield;
import com.pip.itimes.server.dao.BattlefieldDao;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.util.PropertyPool;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.battle.BattleService2;
import com.pip.itimes.server.world.game.BattleForResourcesInstanceModel;
import com.pip.itimes.server.world.game.CampBattlefield;
import com.pip.itimes.server.world.game.CampBattlefieldConfig;
import com.pip.itimes.server.world.game.CampBattlefieldException;
import com.pip.itimes.server.world.game.CampBattlefieldInstance;
import com.pip.itimes.server.world.game.CampBattlefieldInstanceModel;
import com.pip.itimes.server.world.game.CampBattlefieldPlayer;
import com.pip.itimes.server.world.game.CampbattlefieldWarriorPlaces;
import com.pip.itimes.server.world.game.InstanceService;
import com.pip.itimes.server.world.game.WorldService;

/**
 * 阵营战场服务
 * @author hchen
 *
 */
public class CampBattlefieldService implements Runnable {
	private static final Logger log = Logger.getLogger(TongService.class);
	
	private PlayerService playerService;
    private ConnectService connectService;
    private StageService stageService;
    private ChatService chatService;
    private MailService mailService;
    private WorldService worldService;
    private InstanceService instanceService;
    private BattleService2 battleService;
    
    private BattlefieldDao dao;
    
    private static BattleForResourcesInstanceModel resourcesBattlefield;
    private static CampBattlefieldSchedule[] schedules;
    private long lastCheckBattlefieldProcessTime = Utils.getTodayStart();
    private long lastCheckBattlefieldOpenTime = Utils.getTodayStart();
    
    private Map<Integer, CampBattlefieldInstance> ID_instances = new HashMap<Integer, CampBattlefieldInstance>();
    /**
     * 所有开启战场的集合
     */
    private Map<Integer, CampBattlefieldInstanceModel> battlefields = new ConcurrentHashMap<Integer, CampBattlefieldInstanceModel>();
    /**
     * 所有需要移除的战场
     */
    private List<Integer> battlefieldRemoveds = new ArrayList<Integer>();
    
    /**
     * 资源争夺战：排队中的光明阵营玩家
     */
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, LinkedHashMap<Integer, CampBattlefieldPlayer>>> resources_BrightPlayer
    						= new ConcurrentHashMap<String, ConcurrentHashMap<Integer, LinkedHashMap<Integer, CampBattlefieldPlayer>>>();
    
    /**
     * 资源争夺张：排队中的黑暗阵营玩家
     */
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, LinkedHashMap<Integer, CampBattlefieldPlayer>>> resources_DarkPlayer
							= new ConcurrentHashMap<String, ConcurrentHashMap<Integer, LinkedHashMap<Integer, CampBattlefieldPlayer>>>();
    
    /**
     * KEY:战场类型
     * VALUES：KEY：战场等级类型
     * 		   VALUES：当前开启的战场个数
     */
    private ConcurrentHashMap<String, Map<Integer, Integer>> type_battlefieldCount
    						= new ConcurrentHashMap<String, Map<Integer, Integer>>();
    
    /**
     * KEY:战场类型
     * VALUES：KEY：战场等级类型
     * 		   VALUES：开启的战场个数上限
     */
    private static ConcurrentHashMap<String, Map<Integer, Integer>> type_battlefieldMaxCount
    						= new ConcurrentHashMap<String, Map<Integer, Integer>>();
	/**
	 * KEY：等级类型
	 * VALUES：待开启战场任务
	 */
	private static ConcurrentHashMap<String, ConcurrentHashMap<Integer, CampBattlefieldTask>> levelType_task = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, CampBattlefieldTask>>();
	/**
	 * KEY：玩家ID
	 * VALUES：再次排战场的冷卻時間
	 */
	private ConcurrentHashMap<Integer, Long> playerID_cooldown = new ConcurrentHashMap<Integer, Long>();
	/**
	 * KEY：玩家ID
	 * VALUES：拽入战场的玩家
	 */
	private LinkedHashMap<Integer, CampBattlefieldPlayer> draggedPlayers = new LinkedHashMap<Integer, CampBattlefieldPlayer>();
	
	public CampBattlefieldService (BattlefieldDao dao) {
		this.dao = dao;
		new Thread(this).start();
	}
	
	public void addBattlefield (Battlefield battlefield) throws DataAccessException {
    	synchronized (this) {
    		try {
    			dao.makePersistent(battlefield);
    		} catch (DataAccessException ex) {
    			throw new DataAccessException("添加阵营战场记录错误");
    		}
		}
    }
	
	/**
	 * 为数据库添加一条战场数据
	 * @param campBattlefieldType
	 * @param campBattleID
	 * @param playerid
	 * @param campType
	 * @param killPoint
	 * @param isRandom
	 * @param isSummon
	 * @throws DataAccessException
	 */
	public void addCampBattlefieldData (String campBattlefieldType, String campBattleID, int playerid, byte campType,
			int killPoint, boolean isRandom, boolean isSummon) throws DataAccessException {
		Battlefield battlefield = new Battlefield();
		battlefield.setCampbattlefieldtype(campBattlefieldType);
		battlefield.setCampbattleid(campBattleID);
		battlefield.setPlayerid(playerid);
		battlefield.setCamptype(campType);
		Date now = new Date();
		battlefield.setCreatetime(now);
		battlefield.setKillpoint(killPoint);
		battlefield.setIsrandom(isRandom);
		battlefield.setIssummon(isSummon);
		battlefield.setPool(new PropertyPool());
		try {
			addBattlefield(battlefield);
		} catch (DataAccessException e) {
			throw new DataAccessException("添加阵营战场记录错误");
		}
	}
	
	/**
	 * 获得所有玩家的冷却时间
	 * @return
	 */
	public Iterator<Long> getCooldownTimes () {
        return playerID_cooldown.values().iterator();
    }
	
	/**
	 * 添加一位玩家的冷却时间
	 * @param playerID
	 * @param cooldown
	 */
	public void putCooldown (int playerID, long cooldown) {
		playerID_cooldown.put(playerID, cooldown);
	}
	
	/**
	 * 获得玩家冷却时间MAP
	 * @return
	 */
	public Map<Integer, Long> getCooldownMap () {
		return playerID_cooldown;
	}
	
	/**
	 * 获得资源争夺战的光明玩家队列
	 * @return
	 */
	public ConcurrentHashMap<Integer, LinkedHashMap<Integer, CampBattlefieldPlayer>> getWaitingBrightPlayer (String name) {
		return resources_BrightPlayer.get(name);
	}
	
	/**
	 * 获得资源争夺战的黑暗玩家队列
	 * @return
	 */
	public ConcurrentHashMap<Integer, LinkedHashMap<Integer, CampBattlefieldPlayer>> getWaitingDarkPlayer (String name) {
		return resources_DarkPlayer.get(name);
	}
	
	/**
	 * 获得战场开始后，得到加入战场邀请的玩家
	 * @return
	 */
	public LinkedHashMap<Integer, CampBattlefieldPlayer> getDraggedPlayer () {
		return draggedPlayers;
	}
	
	/**
	 * 还原玩家的战场队列位置
	 * @param instance
	 * @param player
	 */
	public void restoreQueuePositionPlayer (CampBattlefieldInstance instance, CampBattlefieldPlayer player) {
		Map<Integer, LinkedHashMap<Integer, CampBattlefieldPlayer>> map = null;
		CampBattlefield campBattlefield = CampBattlefieldConfig.battlefields.get(instance.getName());
		if(campBattlefield == null){
			return;
		}
		if(campBattlefield.getModel() == CampBattlefield.MODEL_NORMOL){
			if (player.getCampType() == Utils.CAMP_BRIGHT) {
				map = resources_BrightPlayer.get(instance.getName());
			} else if (player.getCampType() == Utils.CAMP_DARK) {
				map = resources_DarkPlayer.get(instance.getName());
			}
		}else if(campBattlefield.getModel() == CampBattlefield.MODEL_CHAOS){
			map = resources_BrightPlayer.get(instance.getName());
		}
		if (map != null && map.get(instance.getLevelType()).containsKey(player.getPlayerID())) {
			map.get(instance.getLevelType()).put(player.getPlayerID(), player);
		}
	}
	
	/**
	 * 删除在等待队列的此玩家
	 * @param battlefieldName
	 * @param levelType
	 * @param playerID
	 * @param campType
	 */
	public void removeWaitingPlayer (String battlefieldName, int levelType, int playerID, int campType) {
		CampBattlefield campBattlefield = CampBattlefieldConfig.battlefields.get(battlefieldName);
		if(campBattlefield == null){
			return;
		}
		if(campBattlefield.getModel() == CampBattlefield.MODEL_NORMOL){
			if (campType == Utils.CAMP_BRIGHT) {
				if (resources_BrightPlayer.get(battlefieldName).get(levelType).containsKey(playerID)) {
					resources_BrightPlayer.get(battlefieldName).get(levelType).remove(playerID);
				}
			} else if (campType == Utils.CAMP_DARK) {
				if (resources_DarkPlayer.get(battlefieldName).get(levelType).containsKey(playerID)) {
					resources_DarkPlayer.get(battlefieldName).get(levelType).remove(playerID);
				}
			}
		}else if(campBattlefield.getModel() == CampBattlefield.MODEL_NORMOL){
			if (resources_BrightPlayer.get(battlefieldName).get(levelType).containsKey(playerID)) {
				resources_BrightPlayer.get(battlefieldName).get(levelType).remove(playerID);
			}
		}
	}
	
	/**
	 * 删除战场开始后，得到加入战场邀请的玩家
	 * @param playerID
	 */
	public void removeDraggedPlayer (int playerID) {
		draggedPlayers.remove(playerID);
	}
	
	/**
	 * 检查此玩家是否得到战场邀请
	 * @param playerID
	 * @return
	 */
	public boolean containsDraggedPlayers (int playerID) {
		return draggedPlayers.containsKey(playerID);
	}
	
	public static void setSchedules(CampBattlefieldSchedule[] s) {
        schedules = s;
    }
	
	public static void start() {
		for (int i = 0; i < schedules.length; i++) {
            schedule(schedules[i]);
        }
	}
	
	public static void schedule (CampBattlefieldSchedule schedule) {
		if (CampBattlefieldConfig.CAMP_BATTLEFIELD_TYPE_RESOURCES.equals(schedule.type)) {
			CampBattlefieldTask task = new CampBattlefieldTask(schedule.name, schedule.levelType, schedule.instanceID, schedule.type);
			if(!levelType_task.containsKey(schedule.name)){
				levelType_task.put(schedule.name, new ConcurrentHashMap<Integer, CampBattlefieldTask>());
			}
			levelType_task.get(schedule.name).put(schedule.levelType, task);
		}
	}
	
	/**
	 * 根据战场类型的不同，玩家等级类型的不同，添加已开启战场的个数。
	 * @param type
	 * @param levelType
	 * @param value
	 */
	public synchronized void addCurrentCount (String type, int levelType, int value) {
		Map<Integer, Integer> currentCountMap = getBattlefieldCount(type);
		if (currentCountMap != null) {
			if (currentCountMap.containsKey(levelType)) {
				Integer currentCount = currentCountMap.get(levelType);
				int c = currentCount.intValue() + value;
				currentCountMap.put(levelType, c);
			} else {
				currentCountMap.put(levelType, value);
			}
			modifyBattlefieldCount(type, currentCountMap);
		} else {
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			map.put(levelType, value);
			modifyBattlefieldCount(type, map);
		}
	}
	
	/**
	 * 根据战场类型的不同，玩家等级类型的不同，删除已开启战场的个数。
	 * @param type
	 * @param levelType
	 */
	public synchronized void reduceCurrentCount (String type, int levelType) {
		Map<Integer, Integer> currentCountMap = getBattlefieldCount(type);
		if (currentCountMap != null) {
			if (currentCountMap.containsKey(levelType)) {
				Integer count = currentCountMap.get(levelType);
				int c = count.intValue() - 1;
				if (c <= 0) {
					currentCountMap.remove(levelType);
				} else {
					currentCountMap.put(levelType, c);
				}
				modifyBattlefieldCount(type, currentCountMap);
			}
		}
	}
	
	/**
	 * 获得此类型战场的MAP
	 * @param type
	 * @return
	 */
	public synchronized Map<Integer, Integer> getBattlefieldCount (String type) {
		return type_battlefieldCount.get(type);
	}
	
	/**
	 * 根据此战场类型，修改战场MAP
	 * @param type
	 * @param battleCountMap
	 */
	public synchronized void modifyBattlefieldCount (String type, Map<Integer, Integer> battleCountMap) {
		type_battlefieldCount.put(type, battleCountMap);
	}
	
	/**
	 * 根据战场类型的不同，玩家等级类型的不同，获得已开启战场的个数。
	 * @param type
	 * @param levelType
	 */
	public synchronized int getCurrentCount (String type, int levelType) {
		Map<Integer, Integer> currentCountMap = getBattlefieldCount(type);
		if (currentCountMap == null) {
			return 0;
		} else {
			Integer count = currentCountMap.get(levelType);
			if (count == null) {
				return 0;
			} else {
				return count.intValue();
			}
		}
	}
	
	/**
	 * 检查资源争夺战玩家队列是否满足战场开启的条件
	 * @param name
	 * @param levelType
	 * @param playerService
	 * @return
	 */
	public synchronized CampBattlefieldPlayer[] checkWaitingEnoughToBattle (String name, int levelType, PlayerService playerService) {
//		if (resources_DarkPlayer == null || resources_BrightPlayer == null
//				|| resources_DarkPlayer.get(name) == null
//				|| resources_BrightPlayer.get(name) == null
//				|| resources_DarkPlayer.get(name).get(levelType) == null
//				|| resources_BrightPlayer.get(name).get(levelType) == null
//				) {
//			return null;
//		}
		
		int darkPlayers = 0;
		int brightPlayers = 0;
		LinkedHashMap<Integer, CampBattlefieldPlayer> mapDarkPlayers = null;
		LinkedHashMap<Integer, CampBattlefieldPlayer> mapBrightPlayers = null;
		if(resources_DarkPlayer.containsKey(name)){
			mapDarkPlayers = resources_DarkPlayer.get(name).get(levelType);
		}
		if(resources_BrightPlayer.containsKey(name)){
			mapBrightPlayers = resources_BrightPlayer.get(name).get(levelType);
		}
		if(mapDarkPlayers != null){
			darkPlayers = mapDarkPlayers.size();
		}
		if(mapBrightPlayers != null){
			brightPlayers = mapBrightPlayers.size();
		}
		
		CampBattlefield campBattlefield = CampBattlefieldConfig.battlefields.get(name);
		if(campBattlefield == null){
			return null;
		}
		
		if(campBattlefield.getModel() == CampBattlefield.MODEL_NORMOL){
			int maxDark = CampBattlefieldConfig.battlefields.get(name).getCampbattlefieldWarrior(levelType).getDarkplayers();
			int maxBright = CampBattlefieldConfig.battlefields.get(name).getCampbattlefieldWarrior(levelType).getBrightplayers();
			if (darkPlayers >= maxDark && brightPlayers >= maxBright) {
				processWaitingPlayer(levelType, playerService, maxDark, resources_DarkPlayer.get(name).get(levelType));
				processWaitingPlayer(levelType, playerService, maxBright, resources_BrightPlayer.get(name).get(levelType));
				List<CampBattlefieldPlayer> playerList = new ArrayList<CampBattlefieldPlayer>();
				List<CampBattlefieldPlayer> darkList = getOnlinePlayer(levelType, maxDark, resources_DarkPlayer.get(name).get(levelType));
				List<CampBattlefieldPlayer> brightList = getOnlinePlayer(levelType, maxBright, resources_BrightPlayer.get(name).get(levelType));
				playerList.addAll(brightList);
				playerList.addAll(darkList);
				if (playerList != null && playerList.size() >= maxDark + maxBright) {
					CampBattlefieldPlayer[] ret = new CampBattlefieldPlayer[maxDark + maxBright];
					playerList.toArray(ret);
					for (int i = 0; i < ret.length; i++) {
						CampBattlefieldPlayer tmp = playerList.get(i);
						ret[i] = tmp;
						if (tmp.getCampType() == Utils.CAMP_BRIGHT) {
							if (resources_BrightPlayer.get(name).get(levelType).containsKey(tmp.getPlayerID())) {
								resources_BrightPlayer.get(name).get(levelType).remove(tmp.getPlayerID());
							}
						} else if (tmp.getCampType() == Utils.CAMP_DARK) {
							if (resources_DarkPlayer.get(name).get(levelType).containsKey(tmp.getPlayerID())) {
								resources_DarkPlayer.get(name).get(levelType).remove(tmp.getPlayerID());
							}
						}
					}
					return ret;
				} else {
					return null;
				}
			} else {
				return null;
			}
		}else if(campBattlefield.getModel() == CampBattlefield.MODEL_CHAOS){
			int maxDark = CampBattlefieldConfig.battlefields.get(name).getCampbattlefieldWarrior(levelType).getDarkplayers();
			int maxBright = CampBattlefieldConfig.battlefields.get(name).getCampbattlefieldWarrior(levelType).getBrightplayers();
			if (brightPlayers >= maxDark + maxBright) {
				Random random = new Random();
				int darkCount = 0;
				int brightCount = 0;
				processWaitingPlayer(levelType, playerService, maxDark + maxBright, resources_BrightPlayer.get(name).get(levelType));
				List<CampBattlefieldPlayer> playerList = getOnlinePlayer(levelType, maxDark + maxBright, resources_BrightPlayer.get(name).get(levelType));
				if (playerList != null && playerList.size() >= maxDark + maxBright) {
					CampBattlefieldPlayer[] ret = new CampBattlefieldPlayer[maxDark + maxBright];
					playerList.toArray(ret);
					for (int i = 0; i < ret.length; i++) {
						int campTeam = Utils.hit(random, 50, 100) ? Utils.CAMP_BRIGHT : Utils.CAMP_DARK;
						if(campTeam == Utils.CAMP_BRIGHT){
							if(brightCount >= maxBright){
								campTeam = Utils.CAMP_DARK;
							}else{
								brightCount++;
							}
						}else if(campTeam == Utils.CAMP_DARK){
							if(darkCount >= maxDark){
								campTeam = Utils.CAMP_BRIGHT;
							}else{
								darkCount++;
							}
						}
						CampBattlefieldPlayer tmp = playerList.get(i);
						ret[i] = tmp;
						tmp.setCampTeam(campTeam);
						if (resources_BrightPlayer.get(name).get(levelType).containsKey(tmp.getPlayerID())) {
							resources_BrightPlayer.get(name).get(levelType).remove(tmp.getPlayerID());
						}
					}
					return ret;
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
		return null;
	}
	
	/**
	 * 处理玩家队列，删除离线玩家
	 * @param levelType
	 * @param playerService
	 * @param maxSize
	 * @param waitingPlayer
	 */
	public synchronized int processWaitingPlayer (int levelType, PlayerService playerService,
			int maxSize, LinkedHashMap<Integer, CampBattlefieldPlayer> waitingPlayer) {
		List<Integer> offlinePlayerID = new ArrayList<Integer>();
		int index = 0;
		for (CampBattlefieldPlayer cbfplayer : waitingPlayer.values()) {
			if (cbfplayer != null) {
				int playerID = cbfplayer.getPlayerID();
				WorldPlayer worldPlayer = playerService.getWorldPlayer(playerID);
				if (worldPlayer != null) {
					index ++;
					if (maxSize != -1 && index >= maxSize) {
						break;
					}
				} else {
					offlinePlayerID.add(playerID);
				}
			}
		}
		for (int i = 0; i < offlinePlayerID.size(); i++) {
			waitingPlayer.remove(offlinePlayerID.get(i));
		}
		return waitingPlayer.size();
	}
	
	/**
	 * 获得可以进入战场的在线玩家
	 * @param levelType
	 * @param maxSize
	 * @param waitingPlayer
	 * @return
	 */
	public synchronized List<CampBattlefieldPlayer> getOnlinePlayer (int levelType, int maxSize, LinkedHashMap<Integer, CampBattlefieldPlayer> waitingPlayer) {
		List<CampBattlefieldPlayer> playerList = new ArrayList<CampBattlefieldPlayer>();
		Iterator<Entry<Integer, CampBattlefieldPlayer>> iter = waitingPlayer.entrySet().iterator();
		while(iter.hasNext()){
			Entry<Integer, CampBattlefieldPlayer> entry = iter.next();
			if(entry != null){
				CampBattlefieldPlayer cbfplayer = entry.getValue();
				if (cbfplayer != null) {
					playerList.add(cbfplayer);
					if (playerList.size() >= maxSize) {
						break;
					}
				}
			}
		}
		return playerList;
	}
	
	/**
	 * 根据战场类型，设置此类战场的个数上限Map
	 * @param type
	 * @param maxCountMap
	 */
	public synchronized static void setBattlefieldMaxCount (String type, Map<Integer, Integer> maxCountMap) {
		type_battlefieldMaxCount.put(type, maxCountMap);
	}
	
	/**
	 * 根据战场类型，获得此类战场的个数上限Map
	 * @param type
	 * @return
	 */
	public synchronized Map<Integer, Integer> getBattlefieldMaxCount (String type) {
		return type_battlefieldMaxCount.get(type);
	}
	
	/**
	 * 根据战场类型，玩家等级类型，获得此类战场的最大个数
	 * @param type
	 * @param levelType
	 * @return
	 */
	public synchronized int getMaxCount (String type, int levelType) {
		Map<Integer, Integer> maxCountMap = getBattlefieldMaxCount(type);
		Integer count = maxCountMap.get(levelType);
		if (count == null) {
			return 0;
		}
		return count.intValue();
	}
	
	/**
	 * 设置战场的类型为资源争夺战
	 * @param model
	 */
	public void setBattleField(BattleForResourcesInstanceModel model) {
		resourcesBattlefield = model;
    }
	
	/**
	 * 检测玩家是否在队列中
	 * @param levelType
	 * @param playerid
	 * @return
	 */
	public boolean inBattlefieldWaitingQueue(int levelType, int playerid){
		Iterator<ConcurrentHashMap<Integer, LinkedHashMap<Integer, CampBattlefieldPlayer>>> iter = null;
		if(resources_BrightPlayer != null){
			iter = resources_BrightPlayer.values().iterator();
			while(iter.hasNext()){
				ConcurrentHashMap<Integer, LinkedHashMap<Integer, CampBattlefieldPlayer>> map = iter.next();
				if(map != null){
					LinkedHashMap<Integer, CampBattlefieldPlayer> link = map.get(levelType);
					if(link != null && link.containsKey(playerid)){
						CampBattlefieldPlayer cbp = link.get(playerid);
						if(cbp != null){
							return true;
						}
					}
				}
			}
		}
		if(resources_DarkPlayer != null){
			iter = resources_DarkPlayer.values().iterator();
			while(iter.hasNext()){
				ConcurrentHashMap<Integer, LinkedHashMap<Integer, CampBattlefieldPlayer>> map = iter.next();
				if(map != null){
					LinkedHashMap<Integer, CampBattlefieldPlayer> link = map.get(levelType);
					if(link != null && link.containsKey(playerid)){
						CampBattlefieldPlayer cbp = link.get(playerid);
						if(cbp != null){
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * 玩家加入资源争夺战队列
	 * @param name
	 * @param levelType
	 * @param player
	 * @param joinTime
	 * @param random
	 * @return
	 */
	public synchronized int addWaitingPlayers (String name, int levelType, WorldPlayer player, long joinTime, boolean random) {
		Map<Integer, LinkedHashMap<Integer, CampBattlefieldPlayer>> levelType_waitingPlayer = null;
		
		CampBattlefield campBattlefield = CampBattlefieldConfig.battlefields.get(name);
		if(campBattlefield == null){
			log.info("battlefield not found name[" + name + "]");
			return CampBattlefieldConfig.ERROR_WITHOUT_BATTLEFIELD;
		}
		
		if(campBattlefield.getModel() == CampBattlefield.MODEL_NORMOL){
			if (player.getCamp() == Utils.CAMP_DARK) {
				if(!resources_DarkPlayer.containsKey(name)){
					resources_DarkPlayer.put(name, new ConcurrentHashMap<Integer, LinkedHashMap<Integer, CampBattlefieldPlayer>>());
				}
				levelType_waitingPlayer = resources_DarkPlayer.get(name);
			} else if (player.getCamp() == Utils.CAMP_BRIGHT) {
				if(!resources_BrightPlayer.containsKey(name)){
					resources_BrightPlayer.put(name, new ConcurrentHashMap<Integer, LinkedHashMap<Integer, CampBattlefieldPlayer>>());
				}
				levelType_waitingPlayer = resources_BrightPlayer.get(name);
			}
		}else if(campBattlefield.getModel() == CampBattlefield.MODEL_CHAOS){
			if(!resources_BrightPlayer.containsKey(name)){
				resources_BrightPlayer.put(name, new ConcurrentHashMap<Integer, LinkedHashMap<Integer, CampBattlefieldPlayer>>());
			}
			levelType_waitingPlayer = resources_BrightPlayer.get(name);
		}
		
		LinkedHashMap<Integer, CampBattlefieldPlayer> playerID_battlefieldPlayer = levelType_waitingPlayer.get(levelType);
		if (playerID_battlefieldPlayer != null) {
			CampBattlefieldPlayer battlefieldPlayer = playerID_battlefieldPlayer.get(player.getId());
			if (battlefieldPlayer != null) {
				return CampBattlefieldConfig.ERROR_READY_IN_QUEUE;
			} else {
				if(inBattlefieldWaitingQueue(levelType, player.getId())){
					return CampBattlefieldConfig.ERROR_INOTHER_BATTLEFIELD_QUEUE;
				}else{
					CampBattlefieldPlayer cbp = new CampBattlefieldPlayer(name, levelType, player.getId(), joinTime, random, player.getCamp());
					playerID_battlefieldPlayer.put(player.getId(), cbp);
					log.info("success_queued battlefiled[" + name + "] levelType[" + levelType + "] playerID[" + player.getId() + "]");
					return CampBattlefieldConfig.SUCCESS_QUEUED;
				}
			}
		} else {
			if(inBattlefieldWaitingQueue(levelType, player.getId())){
				return CampBattlefieldConfig.ERROR_INOTHER_BATTLEFIELD_QUEUE;
			}else{
				CampBattlefieldPlayer cbp = new CampBattlefieldPlayer(name, levelType, player.getId(), joinTime, random, player.getCamp());
				LinkedHashMap<Integer, CampBattlefieldPlayer> createLHM = new LinkedHashMap<Integer, CampBattlefieldPlayer>();
				createLHM.put(player.getId(), cbp);
				levelType_waitingPlayer.put(levelType, createLHM);
				log.info("create success_queued battlefiled[" + name + "] levelType[" + levelType + "] playerID[" + player.getId() + "]");
				return CampBattlefieldConfig.SUCCESS_QUEUED;
			}
		}
	}
	
	/**
	 * 获得资源争夺战排队中的玩家
	 * @param name
	 * @param levelType
	 * @param player
	 * @return
	 */
	public synchronized CampBattlefieldPlayer getWaitingPlayer (String name, int levelType, WorldPlayer player) {
		CampBattlefield campBattlefield = CampBattlefieldConfig.battlefields.get(name);
		if(campBattlefield == null){
			return null;
		}
		Map<Integer, LinkedHashMap<Integer, CampBattlefieldPlayer>> levelType_waitingPlayer = null;
		if(campBattlefield.getModel() == CampBattlefield.MODEL_NORMOL){
			if (player.getCamp() == Utils.CAMP_DARK) {
				levelType_waitingPlayer = resources_DarkPlayer.get(name);
			} else if (player.getCamp() == Utils.CAMP_BRIGHT) {
				levelType_waitingPlayer = resources_BrightPlayer.get(name);
			}
		}else if(campBattlefield.getModel() == CampBattlefield.MODEL_CHAOS){
			levelType_waitingPlayer = resources_BrightPlayer.get(name);
		}
		LinkedHashMap<Integer, CampBattlefieldPlayer> playerID_battlefieldPlayer = levelType_waitingPlayer.get(levelType);
		if (playerID_battlefieldPlayer != null) {
			CampBattlefieldPlayer battlefieldPlayer = playerID_battlefieldPlayer.remove(player.getId());
			return battlefieldPlayer;
		} else {
			return null;
		}
	}
	
	
	/**
	 * 根据玩家等级和战场名称，设置阵营战场等级类型
	 * @param name
	 * @param playerLevel
	 * @return
	 */
	public static int setLevelTypeByBattleName (String name, int playerLevel) {
		for (int i = 0; i < schedules.length; i ++) {
			if (schedules[i].getName().equals(name) &&
					schedules[i].getMinLevel() <= playerLevel &&
						schedules[i].getMaxLevel() >= playerLevel) {
				return schedules[i].getLevelType();
			}
		}
		return CampBattlefieldConfig.ERROR_WITHOUT_BATTLEFIELD;
	}
	
	/**
	 * 玩家加入战场
	 * @param name
	 * @param player
	 * @param joinTime
	 * @param random
	 * @return
	 */
	public synchronized long addBattlefieldPlayer (String name, WorldPlayer player, long joinTime, boolean random) {
		if (player.getCamp() != Utils.CAMP_BRIGHT && player.getCamp() != Utils.CAMP_DARK) {
			return CampBattlefieldConfig.ERROR_NO_CAMP;
		}
		if (player.getLevel() < CampBattlefieldConfig.INTO_LIMIT_LEVEL) {
			return CampBattlefieldConfig.ERROR_LEVEL_TOO_LOW;
		}
		if (player.getMap() != null && player.getMap().getMapId() == CampBattlefieldConfig.WORLD_MAP) {
			return CampBattlefieldConfig.ERROR_ON_WORLD_MAP;
		}
		long coolTime = checkPlayerCooldown(player.getId(), joinTime);
		if (coolTime < 0) {
			
			if (player.getMap() != null && player.getMap().getInstance() != null) {
				return CampBattlefieldConfig.ERROR_READY_IN_INSTANCE;
			}
			int results = setLevelTypeByBattleName(name, player.getLevel());
			if (results >= CampBattlefieldConfig.BATTLEFIELD_TYPE) {
				return addWaitingPlayers(name, results, player, joinTime, random);
			} else {
				return results;
			}
		} else {
			coolTime = coolTime - joinTime;
			return coolTime;
		}
	}
	
	/**
	 * 为玩家发送允许进入战场的提示框
	 * @param players
	 * @param ID
	 * @param battlefieldName
	 */
	public synchronized void sendToBattlefield (CampBattlefieldPlayer[] players, int ID, String battlefieldName) {
		for (int i = 0; i < players.length; i++) {
			UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
			seg.writeShort(ClientConstants.EXTEND_INCAMP_BATTLEFIELD_BATTLEFIELD);
			seg.write(CampBattlefieldConfig.ACTION_GOTO_BATTLEFIELD);
			seg.writeString(battlefieldName);
			seg.writeInt(ID);
			connectService.writeTo(seg, players[i].getPlayerID());
		}
	}
	
	/**
	 * 删除玩家的CD时间
	 * @param currentTime
	 */
	public void cooldownEnd (long currentTime) {
		Iterator<Long> ite = getCooldownTimes();
		while (ite.hasNext()) {
			long cooldown = ite.next();
			if (currentTime >= cooldown) {
				ite.remove();
			}
		}
	}
	
	/**
	 * 检查玩家无法再次进入战场的CD时间
	 * @param playerID
	 * @param joinTime
	 * @return
	 */
	public long checkPlayerCooldown (int playerID, long joinTime) {
		Map<Integer, Long> map = getCooldownMap();
		if (map.containsKey(playerID)) {
			long playerTime = playerID_cooldown.get(playerID);
			if (joinTime >= playerTime) {
				playerID_cooldown.remove(playerID);
				return CampBattlefieldConfig.CAN_JOIN_QUEUE;
			} else {
				return map.get(playerID);
			}
		} else {
			return CampBattlefieldConfig.CAN_JOIN_QUEUE;
		}
	}
	
    public void setPlayerService (PlayerService playerService){
        this.playerService = playerService;
    }
    
    public PlayerService getPlayerService () {
    	return playerService;
    }

    public void setConnectService (ConnectService connectService){
        this.connectService = connectService;
    }
    
    public ConnectService getConnectService () {
    	return connectService;
    }
    
    public void setStageService (StageService stageService) {
    	this.stageService = stageService;
    }
    
    public void setChatService (ChatService chatService) {
    	this.chatService = chatService;
    }
    
    public ChatService getChatService(){
    	return chatService;
    }
    
    public void setMailService (MailService mailService) {
    	this.mailService = mailService;
    }
    
    public MailService getMailService(){
    	return mailService;
    }
    
    public void setWorldService (WorldService worldService) {
    	this.worldService = worldService;
    }
    
    public WorldService getWorldService(){
    	return worldService;
    }
    
    public void setInstanceService (InstanceService instanceService) {
    	this.instanceService = instanceService;
    }
    
    public InstanceService getInstanceService(){
    	return instanceService;
    }
    
    public void setBattleService (BattleService2 battleService) {
    	this.battleService = battleService;
    }
    
    public BattleService2 getBattleService(){
    	return battleService;
    }
    
    /**
     * 传送
     * @param playerId
     * @param mapId
     * @param x
     * @param y
     */
    public synchronized void sendGotoMap(int playerId, short mapId, short x, short y) {
        byte[] bytes = stageService.getTaskBytes((short) 31004,
                                                 new String[] {"" + mapId,
                                                 "" + x, "" + y});
        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                          GET_FILE_OK);
        seg.writeShort((short) 31004);
        seg.writeShort((short) 2);
        seg.write(bytes);
        connectService.writeTo(seg, playerId);
    }
    
    /**
     * 添加一个战场副本
     * @param instance
     */
    public void addInstance (CampBattlefieldInstance instance) {
		ID_instances.put(instance.getId(), instance);
    }
    
    /**
     * 删除一个战场副本
     * @param ID
     */
    public void removeInstance (int ID) {
    	ID_instances.remove(ID);
    }
    
    /**
     * 添加要移除的ID
     * @param id
     */
    public void addRemoveBattlefieldID(int id){
    	this.battlefieldRemoveds.add(id);
    }
    
    /**
     * 获取指定ID的战场
     * @param battlefieldID
     * @return
     */
    public CampBattlefieldInstanceModel getInstance(int battlefieldID){
    	if(battlefields.containsKey(battlefieldID)){
    		return battlefields.get(battlefieldID);
    	}
    	return null;
    }
    
    public CampBattlefieldInstance getPlayerInstance(IPlayerData player, int battlefieldID){
    	CampBattlefieldInstanceModel instance = getInstance(battlefieldID);
    	if(instance != null){
    		if(instance instanceof BattleForResourcesInstanceModel){
    			return ((BattleForResourcesInstanceModel)instance).getInstance(player, battlefieldID);
    		}
    	}
    	return null;
    }
    
    /**
     * 强制关闭
     */
    public void shutDown () {
    	for (CampBattlefieldInstanceModel cbi : battlefields.values()) {
    		try {
    			cbi.shutDown();
			} catch (Exception e) {
				log.error(e, e);
			}
		}
    }

    /**
     * 处理
     */
	public void run () {
		while (true) {
			long currentTime = System.currentTimeMillis();
			try {
				// 检查玩家加入战场冷却时间
				cooldownEnd(currentTime);
				if (currentTime - lastCheckBattlefieldOpenTime > CampBattlefieldConfig.TIME_OPEN_BATTLEFIELD) {
					synchronized (levelType_task) {
						for(ConcurrentHashMap<Integer, CampBattlefieldTask> map : levelType_task.values()){
							for (CampBattlefieldTask task : map.values()) {
								if (task != null) {
									int currentCount = getCurrentCount(task.getType(), task.getLevelType());
									int maxCount = getMaxCount(task.getType(), task.getLevelType());
									if (currentCount < maxCount) {
										CampBattlefieldPlayer[] players = checkWaitingEnoughToBattle(task.getName(),
												task.getLevelType(), playerService);
										if (players != null) {
											log.info("Task InstanceID[" + task.getInstanceID() + "]");
											CampBattlefieldInstanceModel instance = null;
											if(task.getType().equals(CampBattlefieldConfig.CAMP_BATTLEFIELD_TYPE_RESOURCES)){
												BattleForResourcesInstanceModel resourceInstance = new BattleForResourcesInstanceModel();
												resourceInstance.setChatService(chatService);
												resourceInstance.setBattleService(battleService);
												resourceInstance.setInstanceService(instanceService);
												resourceInstance.setMailService(mailService);
												resourceInstance.setWorldService(worldService);
												resourceInstance.setCampBattlefieldService(this);
												instance = resourceInstance;
											}
											if(instance != null){
												int ID = instance.start(task.getInstanceID(), task.getName(), task.getType(), task.getLevelType(), currentTime, players);
												log.info("Start ID[" + ID + "]");
												sendToBattlefield(players, ID, task.getName());
												log.info("CampBattlefield Started type[" + task.getType() + "]" +
														" levelType[" + task.getLevelType() + "] currentCount[" +
														currentCount + "] maxCount[" + maxCount + "] name[" + task.getName() + "]");
												battlefields.put(ID, instance);
											}
										}
									}
								}
							}
						}
					}
					lastCheckBattlefieldOpenTime = currentTime;
				}
				// 每1分钟
				if (currentTime - lastCheckBattlefieldProcessTime > Utils.UNIT_OF_MINUTE) {
					// 检查一次所有战场的时间
					Iterator<CampBattlefieldInstanceModel> iter = battlefields.values().iterator();
					while(iter.hasNext()){
						CampBattlefieldInstanceModel cbi = iter.next();
						cbi.process(currentTime);
					}
					
					//删除那些已经结束的战场
					if(battlefieldRemoveds.size() > 0){
						for(int i=battlefieldRemoveds.size() - 1; i >= 0; i--){
							int id = battlefieldRemoveds.get(i);
							if(battlefields.containsKey(id)){
								battlefields.remove(battlefieldRemoveds.get(i));
							}
						}
						battlefieldRemoveds.clear();
					}
					
					lastCheckBattlefieldProcessTime = currentTime;
				}
			} catch (CampBattlefieldException ex) {
				log.error(ex, ex);
			} catch (Exception e) {
				log.error(e, e);
			} finally {
				try {
					Thread.sleep(Utils.UNIT_OF_SECOND);
				} catch (Exception e) {
				}
			}
		}
	}
}

/**
 * 阵营战场需要开启的任务类
 * @author hchen
 *
 */
class CampBattlefieldTask {
	private String name;
	private int levelType;
	private int instanceID;
	private String type;

    public CampBattlefieldTask (String name, int levelType, int instanceID, String type) {
    	this.name = name;
        this.levelType = levelType;
    	this.instanceID = instanceID;
    	this.type = type;
    }
    
    public void setName (String name) {
    	this.name = name;
    }
    
    public String getName () {
    	return name;
    }
    
    public void setLevelType (int levelType) {
    	this.levelType = levelType;
    }
    
    public int getLevelType () {
    	return levelType;
    }
    
    public int getInstanceID () {
    	return instanceID;
    }
    
    public void setInstanceID (int instanceID) {
    	this.instanceID = instanceID;
    }

    public String getType () {
    	return type;
    }
    
    public void setType (String type) {
    	this.type = type;
    }
    
//    public void setCampBattlefield (CampBattlefieldInstanceModel campbattleField) {
//        this.campbattlefield = campbattleField;
//    }
//
//    public CampBattlefieldInstanceModel getCampBattlefield () {
//        return campbattlefield;
//    }
}
