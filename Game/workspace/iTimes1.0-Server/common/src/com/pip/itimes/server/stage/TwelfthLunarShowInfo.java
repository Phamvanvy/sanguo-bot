package com.pip.itimes.server.stage;
/**
 * 食神活动显示排行榜细节
 */
public class TwelfthLunarShowInfo {
	int id;
	int level;
	int count;
	String playerName;
	
	public TwelfthLunarShowInfo (int id, int level, String playerName, int count) {
		this.id = id;
		this.level = level;
		this.playerName = playerName;
		this.count = count;
	}
	
	public String getPlayerName () {
		return playerName;
	}

	public void setPlayerName (String playerName) {
		this.playerName = playerName;
	}

	public int getLevel () {
		return level;
	}

	public void setLevel (int level) {
		this.level = level;
	}
	
	public void setCount (int count) {
		this.count = count;
	}
	
	public int getCount () {
		return count;
	}
	
	public void setId (int id) {
		this.id = id;
	}
	
	public int getId () {
		return id;
	}

}
