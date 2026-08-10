package com.pip.itimes.server.auth;

public class CmccConsumeRecord {

    private int result;
    private String userId;
    private int startSequence;
    private CmccConsumeItem[] items;

    public CmccConsumeRecord(int result,String userId,int startSequence,CmccConsumeItem[] items) {
        this.result = result;
        this.userId = userId;
        this.startSequence = startSequence;
        this.items = items;
    }

    public String getUserId() {
        return userId;
    }

    public int getStartSequence() {
        return startSequence;
    }

    public int getResult() {
        return result;
    }

    public CmccConsumeItem[] getItems() {
        return items;
    }
}
