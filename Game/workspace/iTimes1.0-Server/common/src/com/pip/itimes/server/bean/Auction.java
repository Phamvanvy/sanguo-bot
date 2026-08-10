package com.pip.itimes.server.bean;

import java.util.Date;


public class Auction implements java.io.Serializable {

    private int id;
    private int playerId;
    private int shopId;
    private Date createTime;
    private int startPrice;
    private int currentPrice;
    private int endPrice;
    private byte[] item;
    private int type;
    private int lastPlayerId;
    private String playerName;
    private int quality;
    private int level;
    private short areaId;
    private String name;
    private byte state;
    private Date validTime;

    public Auction() {
    }


    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getShopId() {
        return this.shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getStartPrice() {
        return this.startPrice;
    }

    public void setStartPrice(int startPrice) {
        this.startPrice = startPrice;
    }

    public int getCurrentPrice() {
        return this.currentPrice;
    }

    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }

    public int getEndPrice() {
        return this.endPrice;
    }

    public void setEndPrice(int endPrice) {
        this.endPrice = endPrice;
    }

    public byte[] getItem() {
        return this.item;
    }

    public void setItem(byte[] item) {
        this.item = item;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLastPlayerId() {
        return this.lastPlayerId;
    }

    public void setLastPlayerId(int lastPlayerId) {
        this.lastPlayerId = lastPlayerId;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getQuality(){
        return quality;
    }

    public void setQuality(int quality){
        this.quality = quality;
    }

    public int getLevel(){
        return level;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public short getAreaId(){
        return areaId;
    }

    public void setAreaId(short areaId){
        this.areaId = areaId;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public byte getState(){
        return state;
    }

    public void setState(byte state){
        this.state = state;
    }

    public void setValidTime(Date validTime){
        this.validTime = validTime;
    }

    public Date getValidTime(){
        return validTime;
    }
}
