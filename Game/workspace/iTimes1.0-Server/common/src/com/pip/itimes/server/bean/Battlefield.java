package com.pip.itimes.server.bean;

import java.util.Date;

import com.pip.itimes.server.util.PropertyPool;

public class Battlefield implements java.io.Serializable {
	
	private int id;
	private String campbattleid;
	private int playerid;
	private boolean israndom;
	private boolean issummon;
	private byte camptype;
	private String campbattlefieldtype;
	private int killpoint;
	private Date createtime;
	private PropertyPool pool;
	
	public Battlefield () {
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCampbattleid() {
		return campbattleid;
	}

	public void setCampbattleid(String campbattleid) {
		this.campbattleid = campbattleid;
	}

	public int getPlayerid() {
		return playerid;
	}

	public void setPlayerid(int playerid) {
		this.playerid = playerid;
	}

	public boolean isIsrandom() {
		return israndom;
	}

	public void setIsrandom(boolean israndom) {
		this.israndom = israndom;
	}

	public boolean isIssummon() {
		return issummon;
	}

	public void setIssummon(boolean issummon) {
		this.issummon = issummon;
	}

	public byte getCamptype() {
		return camptype;
	}

	public void setCamptype(byte camptype) {
		this.camptype = camptype;
	}

	public String getCampbattlefieldtype() {
		return campbattlefieldtype;
	}

	public void setCampbattlefieldtype(String campbattlefieldtype) {
		this.campbattlefieldtype = campbattlefieldtype;
	}

	public int getKillpoint() {
		return killpoint;
	}

	public void setKillpoint(int killpoint) {
		this.killpoint = killpoint;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public PropertyPool getPool() {
		return pool;
	}

	public void setPool(PropertyPool pool) {
		this.pool = pool;
	}
	
}
