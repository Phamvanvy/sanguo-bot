package com.pip.itimes.server.world.game;

public class CampbattlefieldWarriorPlaces {
	/**
	 * 战场等级
	 */
	int levelType;
	/**
	 * 光明玩家人数最大值
	 */
	int brightplayers;
	/**
	 * 黑暗玩家人数最大值
	 */
	int darkplayers;
	
	public CampbattlefieldWarriorPlaces (int levelType, int brightplayers, int darkplayers) {
		this.levelType = levelType;
		this.brightplayers = brightplayers;
		this.darkplayers = darkplayers;
	}
	
	public int getLevelType() {
		return levelType;
	}
	public void setLevelType(int levelType) {
		this.levelType = levelType;
	}
	public int getBrightplayers() {
		return brightplayers;
	}
	public void setBrightplayers(int brightplayers) {
		this.brightplayers = brightplayers;
	}
	public int getDarkplayers() {
		return darkplayers;
	}
	public void setDarkplayers(int darkplayers) {
		this.darkplayers = darkplayers;
	}
}
