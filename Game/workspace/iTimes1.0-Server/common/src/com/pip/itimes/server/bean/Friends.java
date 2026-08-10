package com.pip.itimes.server.bean;


public class Friends implements java.io.Serializable {
    private int id;
    //private int accountid;
    private int playerid;
    private int level;
    private int friendplayerid;
    private String playername;
    private int imoney;
    private byte valid;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
//	public int getAccountid() {
//		return accountid;
//	}
//	public void setAccountid(int accountid) {
//		this.accountid = accountid;
//	}
	public int getPlayerid() {
		return playerid;
	}
	public void setPlayerid(int playerid) {
		this.playerid = playerid;
	}
	
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getFriendplayerid() {
		return friendplayerid;
	}
	public void setFriendplayerid(int friendplayerid) {
		this.friendplayerid = friendplayerid;
	}
	public String getPlayername() {
		return playername;
	}
	public void setPlayername(String playername) {
		this.playername = playername;
	}
	public int getImoney() {
		return imoney;
	}
	public void setImoney(int imoney) {
		this.imoney = imoney;
	}
	public byte getValid() {
		return valid;
	}
	public void setValid(byte valid) {
		this.valid = valid;
	}
	
}
