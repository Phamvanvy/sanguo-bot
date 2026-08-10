package com.pip.itimes.server.bean;

import java.util.Date;



public class Oem  implements java.io.Serializable {



     private int id;
     private int shopId;
     private int itemId;
     private int total;
     private int current;
     private int pay;
     private int workPoint;
     private Date createTime;
     private String name;
     private byte type;
     public short areaId;
     private byte state;
     private byte quality;


    public Oem() {
    }

    public void setType(byte type){
        this.type = type;
    }

    public byte getType(){
        return type;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getShopId() {
        return this.shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public int getItemId() {
        return this.itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getTotal() {
        return this.total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCurrent() {
        return this.current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getPay() {
        return this.pay;
    }

    public void setPay(int pay) {
        this.pay = pay;
    }

    public int getWorkPoint() {
        return this.workPoint;
    }

    public void setWorkPoint(int workPoint) {
        this.workPoint = workPoint;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setAreaId(short areaId){
        this.areaId = areaId;
    }

    public short getAreaId(){
        return areaId;
    }

    public byte getState(){
        return state;
    }

    public byte getQuality() {
        return quality;
    }

    public void setState(byte state){
        this.state = state;
    }

    public void setQuality(byte quality) {
        this.quality = quality;
    }
}
