package com.pip.datatransfer.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;


@Entity
@Table(name = "tbl_shop")
public class Shop implements Serializable {
	@Id
	//@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	private int id;
	
	@Column(name="name",nullable=false)
	private String name;
	
	@Column(name="money",nullable=false)
    private int money;
	
	@Column(name="playerid",nullable=false)
    private int playerid;
	
	@Column(name="level",nullable=false)
    private int level;

	@Column(name="createtime",nullable=false)
    private Date createtime;
	
	@Column(name="items",nullable=false)
    private byte[] items;
	
	@Column(name="areaid",nullable=false)
    private int areaid;
	
	@Column(name="gridsize",nullable=false)
    private short gridsize;
	
	@Column(name="state",nullable=false)
    private byte state;
	
	@Column(name="buyplayerid",nullable=false)
    private int buyplayerid;
	
	@Column(name="sellTime",nullable=true)
    private Date sellTime;
	
	@Column(name="leveluptime",nullable=false)
    private Date leveluptime;

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getMoney(){
        return money;
    }

    public void setMoney(int money){
        this.money = money;
    }

    public int getPlayerid(){
        return playerid;
    }

    public void setPlayerid(int playerid){
        this.playerid = playerid;
    }

    public int getLevel(){
        return level;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }

    public byte[] getItems(){
        return items;
    }

    public void setItems(byte[] items){
        this.items = items;
    }

    public int getAreaid(){
        return areaid;
    }

    public void setAreaid(int areaid){
        this.areaid = areaid;
    }

    public short getGridsize(){
        return gridsize;
    }

    public void setGridsize(short gridsize){
        this.gridsize = gridsize;
    }

    public byte getState(){
        return state;
    }

    public void setState(byte state){
        this.state = state;
    }

    public int getBuyplayerid(){
        return buyplayerid;
    }

    public void setBuyplayerid(int buyplayerid){
        this.buyplayerid = buyplayerid;
    }

    public Date getSellTime(){
        return sellTime;
    }

    public void setSellTime(Date sellTime){
        this.sellTime = sellTime;
    }

    public Date getLeveluptime(){
        return leveluptime;
    }

    public void setLeveluptime(Date leveluptime){
        this.leveluptime = leveluptime;
    }
}
