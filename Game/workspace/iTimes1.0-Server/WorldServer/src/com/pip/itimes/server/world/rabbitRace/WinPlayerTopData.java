package com.pip.itimes.server.world.rabbitRace;

public class WinPlayerTopData {
	private int playerMoney;
	private int playerId;
	private String playerName;
	
	public WinPlayerTopData(int playerId, String playerName, int playerMoney) {
		setPlayerId(playerId);
		setPlayerName(playerName);
		setPlayerMoney(playerMoney);
	}
	
	public int getPlayerMoney() {
		return playerMoney;
	}
	public void setPlayerMoney(int money) {
		this.playerMoney = money;
	}
	public int getPlayerId() {
		return playerId;
	}
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
}
