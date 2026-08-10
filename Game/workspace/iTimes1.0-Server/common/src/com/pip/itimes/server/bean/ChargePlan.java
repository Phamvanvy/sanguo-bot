package com.pip.itimes.server.bean;

public class ChargePlan {
    private int id;
    private String serviceNo;
    private String smsContent;
    private float fee;
    private int addPoints;
    private String message;
    private float monthmax;
    public ChargePlan() {
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getServiceNo() {
        return serviceNo;
    }
    public void setServiceNo(String serviceNo) {
        this.serviceNo = serviceNo;
    }
    public String getSmsContent() {
        return smsContent;
    }
    public void setSmsContent(String smsContent) {
        this.smsContent = smsContent;
    }
    public float getFee() {
        return fee;
    }
    public void setFee(float fee) {
        this.fee = fee;
    }
    public int getAddPoints() {
        return addPoints;
    }
    public void setAddPoints(int addPoints) {
        this.addPoints = addPoints;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public float getMonthmax() {
        return monthmax;
    }
    public void setMonthmax(float monthmax) {
        this.monthmax = monthmax;
    }
}
