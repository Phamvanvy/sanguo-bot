package com.pip.itimes.server.world.game;

public class OfflinePlayer {
	private int playerID;
	private long offlineTime;
	
	public OfflinePlayer (int playerID, long offlineTime) {
		this.playerID = playerID;
		this.offlineTime = offlineTime;
	}

	public int getPlayerID() {
		return playerID;
	}

	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}

	public long getOfflineTime() {
		return offlineTime;
	}

	public void setOfflineTime(long offlineTime) {
		this.offlineTime = offlineTime;
	}
	
}
