package com.pip.itimes.server.bean;

import java.util.Date;


public class Shop  implements java.io.Serializable {

     private int id;
     private String name;
     private int money;
     private int playerId;
     private int level;
     private Date createTime;
     private int areaId;
     private byte[] items;
     private short gridSize;
     private byte state;
     private int buyPlayerId;
     private int price;
     private Date sellTime;
     private Date levelupTime;

     public static final byte STATE_NORMAL = 0;
     public static final byte STATE_CLOSED = 1;
     public static final byte STATE_SELL = 2;

     public static final String[] LEVEL = {"迷你店铺","中型店铺","大型店铺","超级店铺"};

    public Shop() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMoney() {
        return this.money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setAreaId(int areaId){
        this.areaId = areaId;
    }

    public int getAreaId(){
        return areaId;
    }

    public byte[] getItems() {
        return this.items;
    }

    public void setItems(byte[] items) {
        this.items = items;
    }

    public void setGridSize(short gridSize){
        this.gridSize = gridSize;
    }

    public short getGridSize(){
        return gridSize;
    }

    public byte getState(){
        return state;
    }

    public void setState(byte state){
        this.state = state;
    }

    public int getBuyPlayerId(){
        return buyPlayerId;
    }

    public void setBuyPlayerId(int playerId){
        this.buyPlayerId = playerId;
    }

    public int getPrice(){
        return this.price;
    }

    public void setPrice(int price){
        this.price = price;
    }

    public void setSellTime(Date time){
        this.sellTime = sellTime;
    }

    public Date getSellTime(){
        return sellTime;
    }

    public void setLevelupTime(Date time){
        this.levelupTime = time;
    }

    public Date getLevelupTime(){
        return levelupTime;
    }

    public String toString(){
        return "店名:"+name+",等级:"+level+",仓库位:"+gridSize+"状态:"+getStateString();
    }

    public String getStateString(){
        if(state==STATE_NORMAL)
            return "正常";
        else if(state==STATE_CLOSED)
            return "关闭";
        else if(state==STATE_SELL)
            return "转让";
        return "";
    }
}
