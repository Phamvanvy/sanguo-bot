package com.pip.itimes.server.auth;

public class CmccChargeItem {

    private String date;
    private String type;
    private int point;

    public CmccChargeItem(String date,String type,int point) {
        this.date = date;
        this.type = type;
        this.point = point;
    }

    public String getType() {
        return type;
    }

    public int getPoint() {
        return point;
    }

    public String getDate() {
        return date;
    }
}
