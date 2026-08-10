package com.pip.itimes.server.world.game;

/**
 * 阵营战场玩家类
 * @author hchen
 *
 */
public class CampBattlefieldPlayer {
	/**
	 * 战场名字
	 */
	String name;
	/**
	 * 阵营类型
	 */
	int campType;
	/**
	 * 战场等级类型
	 */
	int levelType;
	/**
	 * 玩家ID
	 */
	int playerID;
	/**
	 * 加入时间
	 */
	long joinTime;
	/**
	 * 是否随机加入战场
	 */
	boolean random;
	/**
	 * 缴纳资源个数
	 */
	int contributeCount;
	
	/**
	 * 阵营队伍 常量跟阵营一样 不过光明属性元素军团 黑暗属性黑龙军团
	 */
	int campTeam;
	
	public CampBattlefieldPlayer (String name, int levelType, int playerID, long joinTime, boolean random, int campType) {
		this.name = name;
		this.levelType = levelType;
		this.playerID = playerID;
		this.joinTime = joinTime;
		this.random = random;
		this.campType = campType;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getLevelType() {
		return levelType;
	}

	public void setLevelType(int levelType) {
		this.levelType = levelType;
	}

	public int getPlayerID() {
		return playerID;
	}

	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}

	public long getJoinTime() {
		return joinTime;
	}

	public void setJoinTime(long joinTime) {
		this.joinTime = joinTime;
	}

	public boolean isRandom() {
		return random;
	}

	public void setRandom(boolean random) {
		this.random = random;
	}

	public int getContributeCount() {
		return contributeCount;
	}

	public void setContributeCount(int contributeCount) {
		this.contributeCount = contributeCount;
	}

	public int getCampType() {
		return campType;
	}

	public void setCampType(int campType) {
		this.campType = campType;
	}
	
	public int getCampTeam(){
		return campTeam;
	}
	
	public void setCampTeam(int campTeam){
		this.campTeam = campTeam;
	}
}
