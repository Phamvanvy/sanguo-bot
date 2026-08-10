package com.pip.itimes.server.bean;


import java.util.Date;


public class SmsFee implements java.io.Serializable{
    private int id;
    private boolean charged;
    private Date createTime;
    private int accountId;
    private String phone;
    private int amount;
    private String consumeCode;
    private String smsCode;

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public boolean isCharged(){
        return charged;
    }

    public void setCharged(boolean charged){
        this.charged = charged;
    }

    public Date getCreateTime(){
        return createTime;
    }

    public void setCreateTime(Date createTime){
        this.createTime = createTime;
    }

    public int getAccountId(){
        return accountId;
    }

    public void setAccountId(int accountId){
        this.accountId = accountId;
    }

    public String getPhone(){
        return phone;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }

    public int getAmount(){
        return amount;
    }

    public void setAmount(int amount){
        this.amount = amount;
    }

    public String getConsumeCode(){
        return consumeCode;
    }

    public void setConsumeCode(String consumeCode){
        this.consumeCode = consumeCode;
    }

    public String getSmsCode(){
        return smsCode;
    }

    public void setSmsCode(String smsCode){
        this.smsCode = smsCode;
    }
}