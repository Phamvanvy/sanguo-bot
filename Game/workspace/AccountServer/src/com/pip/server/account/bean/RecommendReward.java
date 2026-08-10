package com.pip.server.account.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "tbl_recommendreward")
public class RecommendReward implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="id")
    private int id;
    @Column(name="rewardtime")
    private Date rewardTime;            // 奖励时间
    @Column(name="guestid")
    private int guestID;                // 被推荐人帐号ID
    @Column(name="guestphone")
    private String guestPhone;          // 被推荐人注册手机号
    @Column(name="guestgamecode")
    private String guestGameCode;       // 被推荐人游戏区代码
    @Column(name="roleid")
    private int roleID;                 // 被推荐人角色ID
    @Column(name="guestlevel")
    private int guestLevel;             // 被推荐人达到级别
    @Column(name="guestrewardvalue")
    private int guestRewardValue;       // 被推荐人奖励i币(单位1/100i)
    @Column(name="ownerid")
    private int ownerID;                // 推荐人帐号ID
    @Column(name="ownerrewardvalue")
    private int ownerRewardValue;       // 推荐人奖励i币(单位1/100i)
    @Column(name="rewardcode")
    private int rewardCode;             // 奖励代码
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public Date getRewardTime() {
        return rewardTime;
    }
    public void setRewardTime(Date rewardTime) {
        this.rewardTime = rewardTime;
    }
    public int getGuestID() {
        return guestID;
    }
    public void setGuestID(int guestID) {
        this.guestID = guestID;
    }
    public String getGuestGameCode() {
        return guestGameCode;
    }
    public void setGuestGameCode(String guestGameCode) {
        this.guestGameCode = guestGameCode;
    }
    public int getGuestLevel() {
        return guestLevel;
    }
    public void setGuestLevel(int guestLevel) {
        this.guestLevel = guestLevel;
    }
    public int getGuestRewardValue() {
        return guestRewardValue;
    }
    public void setGuestRewardValue(int guestRewardValue) {
        this.guestRewardValue = guestRewardValue;
    }
    public int getOwnerID() {
        return ownerID;
    }
    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }
    public int getOwnerRewardValue() {
        return ownerRewardValue;
    }
    public void setOwnerRewardValue(int ownerRewardValue) {
        this.ownerRewardValue = ownerRewardValue;
    }
    public int getRewardCode() {
        return rewardCode;
    }
    public void setRewardCode(int rewardCode) {
        this.rewardCode = rewardCode;
    }
    public int getRoleID() {
        return roleID;
    }
    public void setRoleID(int roleID) {
        this.roleID = roleID;
    }
    public String getGuestPhone() {
        return guestPhone;
    }
    public void setGuestPhone(String guestPhone) {
        this.guestPhone = guestPhone;
    }
    
}
