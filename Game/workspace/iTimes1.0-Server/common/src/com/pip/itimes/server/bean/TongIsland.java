package com.pip.itimes.server.bean;

import java.util.Date;

public class TongIsland {

    private int id;
    private Date beginTime;
    private Date endTime;
    private int tongId;

    public TongIsland() {

    }

    public int getTongId() {
        return tongId;
    }

    public int getId() {
        return id;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public void setTongId(int tongId) {
        this.tongId = tongId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getBeginTime() {
        return beginTime;
    }
}
