package com.pip.itimes.server.auth;

public class CmccChargeRecord {

    private int result;
    private String userId;
    private int startSequence;
    private CmccChargeItem[] items;

    public CmccChargeRecord(int result,String userId,int startSequence,CmccChargeItem[] items) {
        this.result = result;
        this.userId = userId;
        this.startSequence = startSequence;
        this.items = items;
    }

    public String getUserId() {
        return userId;
    }

    public int getResult() {
        return result;
    }

    public CmccChargeItem[] getItems() {
        return items;
    }

    public int getStartSequence() {
        return startSequence;
    }


}
