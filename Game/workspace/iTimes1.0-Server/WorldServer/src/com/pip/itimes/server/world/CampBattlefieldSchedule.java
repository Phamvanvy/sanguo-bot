package com.pip.itimes.server.world;

/**
 * 阵营战场流程
 * @author hchen
 *
 */
public class CampBattlefieldSchedule {
	public String name;
	public String type;
	public int instanceID;
	public int levelType;
	public int minLevel;
	public int maxLevel;
	
	public CampBattlefieldSchedule (String name, String type, int instanceID, int levelType, int maxLevel, int minLevel) {
		this.name = name;
		this.type = type;
		this.instanceID = instanceID;
		this.levelType = levelType;
		this.maxLevel = maxLevel;
		this.minLevel = minLevel;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getInstanceID() {
		return instanceID;
	}

	public void setInstanceID(int instanceID) {
		this.instanceID = instanceID;
	}

	public int getLevelType() {
		return levelType;
	}

	public void setLevelType(int levelType) {
		this.levelType = levelType;
	}

	public int getMinLevel() {
		return minLevel;
	}

	public void setMinLevel(int minLevel) {
		this.minLevel = minLevel;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}
}
