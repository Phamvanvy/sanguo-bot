package com.pip.itimes.server.bean;

import java.util.Date;

public class ArenaPoints {

    private int id;
    private int serverId;
    private int playerId;
    private int arenaId;
    private int arenaType;
    private int playerPoint;
    private Date lastrecordtime;
    public ArenaPoints() {
    }
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getServerId() {
		return serverId;
	}
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
	public int getPlayerId() {
		return playerId;
	}
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}
	public int getArenaId() {
		return arenaId;
	}
	public void setArenaId(int arenaId) {
		this.arenaId = arenaId;
	}
	public int getArenaType() {
		return arenaType;
	}
	public void setArenaType(int arenaType) {
		this.arenaType = arenaType;
	}
	public int getPlayerPoint() {
		return playerPoint;
	}
	public void setPlayerPoint(int playerPoint) {
		this.playerPoint = playerPoint;
	}
	public Date getLastrecordtime() {
		return lastrecordtime;
	}
	public void setLastrecordtime(Date lastrecordtime) {
		this.lastrecordtime = lastrecordtime;
	}


}
