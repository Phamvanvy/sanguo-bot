package com.pip.itimes.server.bean;

public class FeePlan {
    private int id;
    private String name;
    private int chargeRate;
    private int chargeInterval;
    public FeePlan() {
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getChargeRate() {
        return chargeRate;
    }
    public void setChargeRate(int chargeRate) {
        this.chargeRate = chargeRate;
    }
    public int getChargeInterval() {
        return chargeInterval;
    }
    public void setChargeInterval(int chargeInterval) {
        this.chargeInterval = chargeInterval;
    }
}
