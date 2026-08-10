package com.pip.itimes.server.bean;

import java.util.Date;


public class Account implements java.io.Serializable {


    private int id;
    private String userName;
    private String msisdn;
    private String password;
    private String guardpass;
    private String phone;
    private int balance;
    private int feeplan;
    private Date createTime;
    private Date lastBillingTime;
    private boolean valid;
    private String recommend;
    private String cause;
    private int iMoney;
    private int monthFee;
    private int subscribeStatus;
    private String subscribePhone;
    private int subscribeBill;
    private String channel;
    private int modifyNameTimes;
    private int modifyPasswordTimes;
    private String model;
    private String gameCode;

    public static final int NO_SUBSCRIBE = 0; // Î´¶©¹º×´Ì¬
    public static final int SUBSCRIBED = 1; // ÒÑ¶©¹º


    public int getSubscribeBill() {
        return subscribeBill;
    }


    public void setSubscribeBill(int subscribeBill) {
        this.subscribeBill = subscribeBill;
    }


    public String getSubscribePhone() {
        return subscribePhone;
    }


    public void setSubscribePhone(String subscribePhone) {
        this.subscribePhone = subscribePhone;
    }


    public int getSubscribeStatus() {
        return subscribeStatus;
    }


    public void setSubscribeStatus(int subscribeStatus) {
        this.subscribeStatus = subscribeStatus;
    }


    public Account() {
    }


    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMsisdn() {
        return this.msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGuardpass() {
        return this.guardpass;
    }

    public void setGuardpass(String guardpass) {
        this.guardpass = guardpass;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getBalance() {
        return this.balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int getFeeplan() {
        return this.feeplan;
    }

    public void setFeeplan(int feeplan) {
        this.feeplan = feeplan;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastBillingTime() {
        return this.lastBillingTime;
    }

    public void setLastBillingTime(Date lastBillingTime) {
        this.lastBillingTime = lastBillingTime;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean getValid() {
        return valid;
    }


    public String getRecommend() {
        return recommend;
    }

    public String getCause() {
        return cause;
    }

    public void setRecommend(String recommend) {
        this.recommend = recommend;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public void setiMoney(int iMoney) {
        this.iMoney = iMoney;
    }

    public int getiMoney() {
        return iMoney;
    }


    public void setMonthFee(int monthFee) {
        this.monthFee = monthFee;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setModifyNameTimes(int modifyNameTimes) {
        this.modifyNameTimes = modifyNameTimes;
    }

    public void setModifyPasswordTimes(int modifyPasswordTimes) {
        this.modifyPasswordTimes = modifyPasswordTimes;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public int getMonthFee() {
        return monthFee;
    }

    public String getChannel() {
        return channel;
    }

    public int getModifyNameTimes() {
        return modifyNameTimes;
    }

    public int getModifyPasswordTimes() {
        return modifyPasswordTimes;
    }

    public String getModel() {
        return model;
    }

    public String getGameCode() {
        return gameCode;
    }
}
