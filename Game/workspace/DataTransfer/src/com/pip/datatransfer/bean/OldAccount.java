package com.pip.datatransfer.bean;


import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;


@Entity
@Table(name = "tbl_account_old")
public class OldAccount implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    @Column(name = "username", nullable = false)
    private String userName;

    @Column(name = "msisdn")
    private String msisdn;

    @Column(name = "password", nullable = false)
    private String passWord;

    @Column(name = "guardpass")
    private String guardPass;

    @Column(name = "phone")
    private String phone;

    @Column(name = "balance")
    private int balance;

    @Column(name = "feeplan")
    private int feePlan;

    @Column(name = "createtime", nullable = false)
    private Date createTime;

    @Column(name = "lastbillingtime")
    private Date lastBillingTime;

    @Column(name = "valid", nullable = false)
    private int valid;

    @Column(name = "recommend")
    private String recommend; //推荐人

    @Column(name = "cause")
    private String cause;

    @Column(name = "imoney", nullable = false)
    private int iMoney;

    @Column(name = "monthfee", nullable = false)
    private int monthFee;

    @Column(name = "subscribestatus", nullable = false)
    private int subscribeStatus;

    @Column(name = "subscribephone")
    private String subscribePhone;

    @Column(name = "subscribebill", nullable = false)
    private int subscribeBill;

    @Column(name = "channel")
    private String channel;

    @Column(name = "modifynametimes", nullable = false)
    private int modifyNameTimes;

    @Column(name = "modifypasswordtimes", nullable = false)
    private int modifyPasswordTimes;

    @Column(name = "model")
    private String model;

    @Column(name = "gamecode")
    private String gameCode; //建立帐号的游戏码

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getUserName(){
        return userName;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public String getMsisdn(){
        return msisdn;
    }

    public void setMsisdn(String msisdn){
        this.msisdn = msisdn;
    }

    public String getPassWord(){
        return passWord;
    }

    public void setPassWord(String passWord){
        this.passWord = passWord;
    }

    public String getGuardPass(){
        return guardPass;
    }

    public void setGuardPass(String guardPass){
        this.guardPass = guardPass;
    }

    public String getPhone(){
        return phone;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }

    public int getBalance(){
        return balance;
    }

    public void setBalance(int balance){
        this.balance = balance;
    }

    public int getFeePlan(){
        return feePlan;
    }

    public void setFeePlan(int feePlan){
        this.feePlan = feePlan;
    }

    public Date getCreateTime(){
        return createTime;
    }

    public void setCreateTime(Date createTime){
        this.createTime = createTime;
    }

    public Date getLastBillingTime(){
        return lastBillingTime;
    }

    public void setLastBillingTime(Date lastBillingTime){
        this.lastBillingTime = lastBillingTime;
    }

    public int getValid(){
        return valid;
    }

    public void setValid(int valid){
        this.valid = valid;
    }

    public String getRecommend(){
        return recommend;
    }

    public void setRecommend(String recommend){
        this.recommend = recommend;
    }

    public String getCause(){
        return cause;
    }

    public void setCause(String cause){
        this.cause = cause;
    }

    public int getIMoney(){
        return iMoney;
    }

    public void setIMoney(int money){
        iMoney = money;
    }

    public int getMonthFee(){
        return monthFee;
    }

    public void setMonthFee(int monthFee){
        this.monthFee = monthFee;
    }

    public int getSubscribeStatus(){
        return subscribeStatus;
    }

    public void setSubscribeStatus(int subscribeStatus){
        this.subscribeStatus = subscribeStatus;
    }

    public String getSubscribePhone(){
        return subscribePhone;
    }

    public void setSubscribePhone(String subscribePhone){
        this.subscribePhone = subscribePhone;
    }

    public int getSubscribeBill(){
        return subscribeBill;
    }

    public void setSubscribeBill(int subscribeBill){
        this.subscribeBill = subscribeBill;
    }

    public String getChannel(){
        return channel;
    }

    public void setChannel(String channel){
        this.channel = channel;
    }

    public int getModifyNameTimes(){
        return modifyNameTimes;
    }

    public void setModifyNameTimes(int modifyNameTimes){
        this.modifyNameTimes = modifyNameTimes;
    }

    public int getModifyPasswordTimes(){
        return modifyPasswordTimes;
    }

    public void setModifyPasswordTimes(int modifyPasswordTimes){
        this.modifyPasswordTimes = modifyPasswordTimes;
    }

    public String getModel(){
        return model;
    }

    public void setModel(String model){
        this.model = model;
    }

    public String getGameCode(){
        return gameCode;
    }

    public void setGameCode(String gameCode){
        this.gameCode = gameCode;
    }
}
