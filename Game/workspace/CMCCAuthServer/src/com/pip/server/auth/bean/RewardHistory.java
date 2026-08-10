package com.pip.server.auth.bean;

import java.util.Date;

/**
 * ÕÊºÅ½±Àø¼ÇÂ¼¡£
 * @author lighthu
 */
public class RewardHistory implements java.io.Serializable {
    private int id;
    private String userID;
    private int cause;
    private String sourceUser;
    private int accountID;
    private int playerID;
    private int level;
    private Date rewardTime;
    private int rewardMoney;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public int getCause() {
        return cause;
    }
    public void setCause(int cause) {
        this.cause = cause;
    }
    public String getSourceUser() {
        return sourceUser;
    }
    public void setSourceUser(String sourceUser) {
        this.sourceUser = sourceUser;
    }
    public int getAccountID() {
        return accountID;
    }
    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }
    public int getPlayerID() {
        return playerID;
    }
    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public Date getRewardTime() {
        return rewardTime;
    }
    public void setRewardTime(Date rewardTime) {
        this.rewardTime = rewardTime;
    }
    public int getRewardMoney() {
        return rewardMoney;
    }
    public void setRewardMoney(int rewardMoney) {
        this.rewardMoney = rewardMoney;
    }

}
