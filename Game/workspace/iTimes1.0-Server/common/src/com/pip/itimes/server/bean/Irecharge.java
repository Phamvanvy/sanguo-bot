package com.pip.itimes.server.bean;

import java.util.Date;

public class Irecharge implements java.io.Serializable {

    private int id;
    private int accountid;
    private int playerid;
    private int money;
    private Date chargetime;
    private int playerlevel;

    public Irecharge() {
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAccountid() {
		return accountid;
	}

	public void setAccountid(int accountid) {
		this.accountid = accountid;
	}

	public int getPlayerid() {
		return playerid;
	}

	public void setPlayerid(int playerid) {
		this.playerid = playerid;
	}

	public int getMoney() {
		return money;
	}

	public int getPlayerlevel() {
		return playerlevel;
	}

	public void setPlayerlevel(int playerlevel) {
		this.playerlevel = playerlevel;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public Date getChargetime() {
		return chargetime;
	}

	public void setChargetime(Date chargetime) {
		this.chargetime = chargetime;
	}
}
