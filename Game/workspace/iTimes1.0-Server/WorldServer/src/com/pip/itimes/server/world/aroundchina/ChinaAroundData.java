package com.pip.itimes.server.world.aroundchina;



/**
 * @author mengjie
 * @version 1.0
 */
public class ChinaAroundData {
	//id->msg用
	private int ID = 0;
	//Userid
    private int UserID = 0;
    //用户登录名
    private String Username = "";
    //金额
    private int Amount = 0;
    //序列号
    private String Serialnum = "";
    //密码
    private String Password = "";
    //标志位
    private int type = 0;
    //用户渠道号
    private String channel = "";

    public int getID() {
		return ID;
	}
	public void setID(int id) {
		ID = id;
	}
	public int getUserID() {
		return UserID;
	}
	public void setUserID(int Userid) {
		UserID = Userid;
	}
	public String getUsername() {
		return Username;
	}
	public void setUsername(String username) {
		Username = username;
	}
	public int getAmount() {
		return Amount;
	}
	public void setAmount(int amount) {
		Amount = amount;
	}
	public String getSerialnum() {
		return Serialnum;
	}
	public void setSerialnum(String serialnum) {
		Serialnum = serialnum;
	}
	public String getPassword() {
		return Password;
	}
	public void setPassword(String password) {
		Password = password;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
    public String getChannel() {
        return channel;
    }
    
    public void setChannel(String c) {
        channel = c;
    }
}
