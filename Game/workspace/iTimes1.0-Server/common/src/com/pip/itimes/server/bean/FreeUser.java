package com.pip.itimes.server.bean;

import java.util.Date;




public class FreeUser{



     private int id;
     private int accountId;
     private int playerId;
     private Date freeTime;


    public FreeUser() {
    }




    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccountId() {
        return this.accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public Date getFreeTime() {
        return this.freeTime;
    }

    public void setFreeTime(Date freeTime) {
        this.freeTime = freeTime;
    }









}
