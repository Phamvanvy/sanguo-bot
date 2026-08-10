package com.pip.datatransfer.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;


@Entity
@Table(name = "tbl_auction")
public class Acution implements Serializable {
	@Id
	//@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	private int id;
	
	@Column(name="playerid",nullable=false)
	private int playerid;
	
	@Column(name="shopid",nullable=false)
    private int shopid;
	
	@Column(name="createtime",nullable=false)
    private Date createtime;
	
	@Column(name="startprice",nullable=false)
    private int startprice;
	
	@Column(name="currentprice",nullable=false)
    private int currentprice;
	
	@Column(name="endprice",nullable=false)
    private int endprice;
	
	@Column(name="item",nullable=false)
    private byte[] item;
	
	@Column(name="type",nullable=false)
    private int type;

	@Column(name="lastplayerid",nullable=false)
    private int lastplayerid;
	
	@Column(name="playername",nullable=false)
    private String playername;
	
	@Column(name="quality",nullable=false)
    private int quality;
	
	@Column(name="level",nullable=false)
    private int level;
	
	@Column(name="areaid",nullable=false)
    private short areaid;
	
	@Column(name="name",nullable=false)
    private String name;
	
	@Column(name="state",nullable=false)
    private byte state;
	
	@Column(name="validtime",nullable=false)
    private Date validtime;

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getPlayerid(){
        return playerid;
    }

    public void setPlayerid(int playerid){
        this.playerid = playerid;
    }

    public int getShopid(){
        return shopid;
    }

    public void setShopid(int shopid){
        this.shopid = shopid;
    }

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }

    public int getStartprice(){
        return startprice;
    }

    public void setStartprice(int startprice){
        this.startprice = startprice;
    }

    public int getCurrentprice(){
        return currentprice;
    }

    public void setCurrentprice(int currentprice){
        this.currentprice = currentprice;
    }

    public int getEndprice(){
        return endprice;
    }

    public void setEndprice(int endprice){
        this.endprice = endprice;
    }

    public byte[] getItem(){
        return item;
    }

    public void setItem(byte[] item){
        this.item = item;
    }

    public int getType(){
        return type;
    }

    public void setType(int type){
        this.type = type;
    }

    public int getLastplayerid(){
        return lastplayerid;
    }

    public void setLastplayerid(int lastplayerid){
        this.lastplayerid = lastplayerid;
    }

    public String getPlayername(){
        return playername;
    }

    public void setPlayername(String playername){
        this.playername = playername;
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

    public short getAreaid(){
        return areaid;
    }

    public void setAreaid(short areaid){
        this.areaid = areaid;
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

    public Date getValidtime(){
        return validtime;
    }

    public void setValidtime(Date validtime){
        this.validtime = validtime;
    }
}
