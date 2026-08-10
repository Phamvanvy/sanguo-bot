package com.pip.server.auth.bean;

import java.util.Date;

/**
 * ÍÆ¼öÇëÇó¡£
 * @author lighthu
 */
public class RecommendRequest implements java.io.Serializable {
    private int id;
    private String userId;
    private int accountId;
    private String serverId;
    private int playerId;
    private String targetPhone;
    private Date createTime;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public int getAccountId() {
        return accountId;
    }
    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
    public String getServerId() {
        return serverId;
    }
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
    public int getPlayerId() {
        return playerId;
    }
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
    public String getTargetPhone() {
        return targetPhone;
    }
    public void setTargetPhone(String targetPhone) {
        this.targetPhone = targetPhone;
    }
    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
