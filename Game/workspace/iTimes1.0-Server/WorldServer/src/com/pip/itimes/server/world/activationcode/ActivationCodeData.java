package com.pip.itimes.server.world.activationcode;

import com.pip.itimes.server.stage.ActivationCode;

/**
 * @author mengjie
 * @version 1.0
 */
public class ActivationCodeData {
	public static final int gamecode = 1;//游戏代码，1 - 幻想，2 - 武林，3 - 乐园，4 - 三国
	//AccountID
	private int AccountID = 0;
	//PlayerID
	private int PlayerID = 0;
	private String activationcode;
	//类型：1）
    private int type = 1001;
    //内容1
    private int itemsid = ActivationCode.getactivationcode(type).getItemsid();;
    //内容2
    private int count = ActivationCode.getactivationcode(type).getCount();
    private int level = ActivationCode.getactivationcode(type).getLevel();
	public int getAccountID() {
		return AccountID;
	}
	public void setAccountID(int accountId) {
		AccountID = accountId;
	}
	public int getPlayerID() {
		return PlayerID;
	}
	public void setPlayerID(int playerId) {
		PlayerID = playerId;
	}
	public static int getGamecode() {
		return gamecode;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	public int getType() {
		return type;
	}
	public int getItemsid() {
		return itemsid;
	}
	public int getCount() {
		return count;
	}
	public int getLevel() {
		return level;
	}
	public String getActivationcode() {
		return activationcode;
	}
	public void setActivationcode(String activationcode) {
		this.activationcode = activationcode;
	}
	
}
