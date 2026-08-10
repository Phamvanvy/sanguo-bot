package com.pip.itimes.server.bean;

import java.util.Date;


public class Ibuy implements java.io.Serializable {
    private int id;
    private int accountid;
    private int playerid;
    private String itemname;
    private int itemid;
    private byte type;//物品种类
    private int imoney;//消费实际i币（单位i）
    private Date buytime;
    private int otherplayerid;//赠与角色id（-1为自用）
    private String otherplayername;//赠与角色名（空为自用）
    private int count;//购买数量
    private byte giftflag;//是否用券
    private int level;	// 购买时人物的等级

    public Ibuy() {
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

	public String getItemname() {
		return itemname;
	}

	public void setItemname(String itemname) {
		this.itemname = itemname;
	}

	public int getItemid() {
		return itemid;
	}

	public void setItemid(int itemid) {
		this.itemid = itemid;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public int getImoney() {
		return imoney;
	}

	public void setImoney(int imoney) {
		this.imoney = imoney;
	}

	public Date getBuytime() {
		return buytime;
	}

	public void setBuytime(Date buytime) {
		this.buytime = buytime;
	}

	public byte getGiftflag() {
		return giftflag;
	}

	public void setGiftflag(byte giftflag) {
		this.giftflag = giftflag;
	}

	public int getOtherplayerid() {
		return otherplayerid;
	}

	public void setOtherplayerid(int otherplayerid) {
		this.otherplayerid = otherplayerid;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getOtherplayername() {
		return otherplayername;
	}

	public void setOtherplayername(String otherplayername) {
		this.otherplayername = otherplayername;
	}
	
	public void setLevel (int level) {
		this.level = level;
	}
	
	public int getLevel () {
		return level;
	}
	
}
