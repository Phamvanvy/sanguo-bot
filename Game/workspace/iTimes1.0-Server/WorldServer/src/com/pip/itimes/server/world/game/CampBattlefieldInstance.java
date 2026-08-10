package com.pip.itimes.server.world.game;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.InstanceDefinition;
import com.pip.itimes.server.world.WorldPlayer;

/**
 * 阵营战场副本
 * @author hchen
 *
 */
public class CampBattlefieldInstance extends Instance {
	/**
	 * 时
	 */
	private int hour;
	/**
	 * 分
	 */
	private int minute;
	/**
	 * 黑暗缴纳物品个数
	 */
	private int darkItemCount;
	/**
	 * 光明缴纳物品个数
	 */
	private int brightItemCount;
	/**
	 * 战场名称
	 */
	private String name;
	/**
	 * 战场类型
	 */
	private String type;
	/**
	 * 战场创建时间
	 */
	private long createTime;
	/**
	 * 战场等级类型
	 */
	private int levelType;
	/**
	 * 战场生命周期
	 */
	private long endTime;
	/**
	 * 战场当前时间
	 */
	private long currentTime;
	/**
	 * 发送广播的次数
	 */
	private byte times;
	/**
	 * 战场关闭，但没有销毁
	 */
	private boolean isClosed;
	/**
	 * KEY：阵营类型
	 * VALUE：玩家个数
	 */
	private Map<Integer, Integer> campType_playerCount = new HashMap<Integer, Integer>();
	/**
	 * KEY:玩家ID
	 * VALUE:战场玩家对象
	 */
	private Map<Integer, CampBattlefieldPlayer> playerID_battlePlayer = new HashMap<Integer, CampBattlefieldPlayer>();
	/**
	 * KEY:玩家ID
	 * VALUE:玩家离线时的时间
	 */
	private HashMap<Integer, OfflinePlayer> playerID_OfflinePlayer = new HashMap<Integer, OfflinePlayer>();
	
	public CampBattlefieldInstance(int id, String name, String type, int levelType, InstanceDefinition idf, InstanceService service) {
		super(id, idf, service);
		this.createTime = System.currentTimeMillis();
		this.endTime = this.createTime + Utils.UNIT_OF_SECOND * idf.getRefreshSecond();
		this.levelType = levelType;
		this.name = name;
		this.type = type;
		this.brightItemCount = 0;
		this.darkItemCount = 0;
		this.isClosed = false;
		Calendar calendar = Calendar.getInstance();
		this.hour = calendar.get(Calendar.HOUR_OF_DAY);
		this.minute = calendar.get(Calendar.MINUTE);
	}
	
	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getDarkItemCount() {
		return darkItemCount;
	}

	public void setDarkItemCount(int darkItemCount) {
		this.darkItemCount = darkItemCount;
	}

	public int getBrightItemCount() {
		return brightItemCount;
	}

	public void setBrightItemCount(int brightItemCount) {
		this.brightItemCount = brightItemCount;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public String getType () {
		return type;
	}
	
	public int getLevelType () {
		return levelType;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}

	public boolean setActive(int id) {
        return super.setActive(id);
    }

	public synchronized void preAdd(WorldPlayer[] players, byte model, byte campTeam) throws InstanceException {
		for (int i = 0; i < players.length; i ++) {
			if(model == CampBattlefield.MODEL_NORMOL){
				addCampPlayer(players[i].getCamp());
			}else if(model == CampBattlefield.MODEL_CHAOS){
				addCampPlayer(campTeam);
			}
		}
		super.preAdd(players);
	}
	 
	/**
	 * 通过阵营类型获得当前阵营玩家个数
	 * @param campType
	 * @return
	 */
	public int getPlayerCountByCampType (int campType) {
		Integer count = campType_playerCount.get(campType);
		if (count == null) {
			return 0;
		}
		return count.intValue();
	}
	
	/**
	 * 获得玩家的个数MAP
	 * @return
	 */
	public Map<Integer, Integer> getPlayerCountMap () {
		return campType_playerCount;
	}
	 
	/**
	 * 根据阵营添加玩家
	 * @param campType
	 */
	private void addCampPlayer (int campType) {
		Integer playerCount = getPlayerCountByCampType(campType);
		if (playerCount == 0) {
			campType_playerCount.put(campType, 1);
		} else {
			int c = playerCount.intValue() + 1;
			campType_playerCount.put(campType, c);
		}
	}
	 
	/**
	 * 根据阵营类型减少玩家并查看此方是否还有玩家
	 * @param campType
	 * @return
	 */
	public boolean reducePlayer (int campType) {
		if (campType_playerCount.containsKey(campType)) {
			Integer count = campType_playerCount.get(campType);
			int c = count.intValue() - 1;
			if (c <= 0) {
				campType_playerCount.remove(campType);
				// 此阵营放玩家个数为0，宣告失败，失败方不给奖励，胜利方颁发奖励
				return false;
			} else {
				campType_playerCount.put(campType, c);
				return true;
			}
		} else {
			return false;
		}
	}
	 
	/**
	 * 战场关闭，删除此战场的玩家进度
	 * PS:	由于每一个玩家成功获得奖励后无论输赢都
	 * 	需要等待clearance秒的时间才可以再次排战
	 * 	场故只有没有战场进度的玩家才可以再排战场
	 */
	public synchronized void removePlayer (int playerID) {
		super.removePlayer(playerID);
	}
	 
	/**
	 * 战斗结束，把战场中的玩家T出
	 */
	public synchronized boolean removeActive (int playerID) {
		return super.removeActive(playerID);
	}
	 
	public void removeBattlefieldPlayer (int playerID) {
		playerID_battlePlayer.remove(playerID);
	}
	
	public void setBattlefieldPlayer (CampBattlefieldPlayer[] players) {
		for (int i = 0; i < players.length; i++) {
			playerID_battlePlayer.put(players[i].getPlayerID(), players[i]);
		}
	}
	
	public boolean battlefieldPlayerContains (int playerID) {
		return playerID_battlePlayer.containsKey(playerID);
	}
	 
	public CampBattlefieldPlayer getBattlefieldPlayer (int playerID) {
		return playerID_battlePlayer.get(playerID);
	}
	 
	public boolean isTimeOut (long now) {
		return createTime != 0 && ((now - createTime) / 1000 > getRefreshSecond());
	}
	
	public synchronized void addItemCount (int campType, int totalCount){
		if (campType == Utils.CAMP_DARK) {
			darkItemCount += totalCount;
		} else if (campType == Utils.CAMP_BRIGHT) {
			brightItemCount += totalCount;
		}
	}
	
	public synchronized int getItemCount (int campType) {
		if(campType == Utils.CAMP_DARK) {
			return darkItemCount;
		} else if (campType == Utils.CAMP_BRIGHT) {
			return brightItemCount;
		}
		return 0;
	}
	
	public int getMessageTimes (long currentTime) {
		if (currentTime >= this.currentTime + CampBattlefieldConfig.SEND_BROADCAST_TIME) {
			times ++;
			this.currentTime = currentTime;
			return times;
		} else {
			return -1;
		}
	}
	
	public boolean hasOfflinePlayer (int playerID) {
    	return playerID_OfflinePlayer.containsKey(playerID);
    }
    
    public void removeOfflinePlayer (int playerID) {
    	this.playerID_OfflinePlayer.remove(playerID);
    }
    
    public Iterator<OfflinePlayer> getOfflinePlayers () {
    	return playerID_OfflinePlayer.values().iterator();
    }
    
    public void addOfflinePlayer (int playerID, OfflinePlayer offlinePlayer) {
    	this.playerID_OfflinePlayer.put(playerID, offlinePlayer);
    }
}
