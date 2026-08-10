package com.pip.itimes.server.bean;

public class Mater {

    private int id;
    private int masterId;
    private String masterName;
    private int prenticeId;
    private String prenticeName;

    public Mater() {
    }

    public String getPrenticeName() {
        return prenticeName;
    }

    public int getPrenticeId() {
        return prenticeId;
    }

    public String getMasterName() {
        return masterName;
    }

    public int getMasterId() {
        return masterId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPrenticeName(String prenticeName) {
        this.prenticeName = prenticeName;
    }

    public void setPrenticeId(int prenticeId) {
        this.prenticeId = prenticeId;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public void setMasterId(int masterId) {
        this.masterId = masterId;
    }

    public int getId() {
        return id;
    }
}
