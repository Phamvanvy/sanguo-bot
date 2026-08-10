package com.pip.itimes.server.bean;

import java.util.Date;

public class Mate {

    private int id;
    private int husbandId;
    private String husbandName;
    private int wifeId;
    private String wifeName;
    private Date createTime;

    public Mate() {
    }

    public String getWifeName() {
        return wifeName;
    }

    public int getWifeId() {
        return wifeId;
    }

    public int getId() {
        return id;
    }

    public String getHusbandName() {
        return husbandName;
    }

    public void setHusbandId(int husbandId) {
        this.husbandId = husbandId;
    }

    public void setWifeName(String wifeName) {
        this.wifeName = wifeName;
    }

    public void setWifeId(int wifeId) {
        this.wifeId = wifeId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setHusbandName(String husbandName) {
        this.husbandName = husbandName;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getHusbandId() {
        return husbandId;
    }

    public Date getCreateTime() {
        return createTime;
    }
}
