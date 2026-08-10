package com.pip.itimes.server.bean;


public class ArenaTeam2Player implements java.io.Serializable {


    private int arenaId;
    private int arenaLevel;
    private int playerId;
    private int type;
    private String playername;
    private int playerarenaLevel;
    private boolean isowner;

    public ArenaTeam2Player() {
    }


	public int getArenaId() {
		return arenaId;
	}


	public void setArenaId(int arenaId) {
		this.arenaId = arenaId;
	}


	public int getArenaLevel() {
		return arenaLevel;
	}


	public void setArenaLevel(int arenaLevel) {
		this.arenaLevel = arenaLevel;
	}


	public int getPlayerId() {
		return playerId;
	}


	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}


	public int getType() {
		return type;
	}


	public void setType(int type) {
		this.type = type;
	}


	public String getPlayername() {
		return playername;
	}


	public void setPlayername(String playername) {
		this.playername = playername;
	}


	public int getPlayerarenaLevel() {
		return playerarenaLevel;
	}


	public void setPlayerarenaLevel(int playerarenaLevel) {
		this.playerarenaLevel = playerarenaLevel;
	}


	public boolean isIsowner() {
		return isowner;
	}


	public void setIsowner(boolean isowner) {
		this.isowner = isowner;
	}


}
