package com.pip.itimes.server.bean;

import java.util.Date;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Fee implements java.io.Serializable {

    private int id;
    private int accountId;
    private Date createTime;
    private Date finishTime;
    private boolean charged;
    private int amount;
    private String channel;

	public Fee() {
    }

    public int getId() {
        return id;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public boolean isCharged() {
        return charged;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setCharged(boolean charged) {
        this.charged = charged;
    }

    public int getAccountId() {
        return accountId;
    }
    
    public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	public String getChannel() {
		return channel;
	}
	
	public void setChannel(String c) {
		channel = c;
	}
}
