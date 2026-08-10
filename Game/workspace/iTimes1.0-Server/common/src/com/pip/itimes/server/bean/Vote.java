package com.pip.itimes.server.bean;

import java.util.Date;


public class Vote implements java.io.Serializable {
    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getVotersid() {
		return votersid;
	}

	public void setVotersid(int votersid) {
		this.votersid = votersid;
	}

	public int getPlayeridvoters() {
		return playeridvoters;
	}

	public void setPlayeridvoters(int playeridvoters) {
		this.playeridvoters = playeridvoters;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public int getVotepoint() {
		return votepoint;
	}

	public void setVotepoint(int votepoint) {
		this.votepoint = votepoint;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	private int id;
    /**
     * 选举别人着
     */
    private int votersid;
    /**
     * 被别人选举
     */
    private int playeridvoters;
    private Date createtime;
    private int votepoint;
    private int type;
    private boolean valid;
    private byte isImoneyItem;	// 0是J币道具，1是I币道具
    
    public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public Vote() {
    }

	public byte getIsImoneyItem() {
		return isImoneyItem;
	}

	public void setIsImoneyItem(byte isImoneyItem) {
		this.isImoneyItem = isImoneyItem;
	}

}

