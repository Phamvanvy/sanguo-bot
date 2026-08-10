package com.pip.itimes.server.bean;

public class Master {

    private int id;
    private int masterId;
    private String masterName;
    private int prenticeId;
    private String prenticeName;
    private int beginLevel;
    private int state;
    private int intimacy;
    private int fame;

    public static final int CURRENT = 1;
    public static final int FAIL = 2;
    public static final int SUCCESS = 3;

    public Master() {
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

    public void setBeginLevel(int beginLevel) {
        this.beginLevel = beginLevel;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public int getBeginLevel() {
        return beginLevel;
    }

    public int getState() {
        return state;
    }

    public int getIntimacy(){
    	return intimacy;
    }
    
    public void setIntimacy(int intimacy){
    	this.intimacy = intimacy;
    }
    
    public int getFame(){
    	return fame;
    }
    
    public void setFame(int fame){
    	this.fame = fame;
    }

}
