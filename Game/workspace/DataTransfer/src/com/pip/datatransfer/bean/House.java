package com.pip.datatransfer.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;


@Entity
@Table(name = "tbl_house")
public class House implements Serializable {
	@Id
	//@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	private int id;
	
	@Column(name="playerid",nullable=false)
	private int playerid;

	@Column(name="playername",nullable=false)
    private String playername;
	
	@Column(name="createtime",nullable=false)
    private Date createtime;
	
	@Column(name="level",nullable=false)
    private int level;
	
	@Column(name="style",nullable=false)
    private int style;
	
	@Column(name="rule",nullable=false)
    private int rule;
	
	@Column(name="areaid",nullable=false)
    private short areaid;
	
	@Column(name="gridsize",nullable=false)
    private int gridsize;
	
	@Column(name="items",nullable=true)
    private byte[] items;
	
	@Column(name="parts",nullable=true)
    private byte[] parts;
	
	@Column(name="lasttime",nullable=true)
    private Date lasttime;
	
	@Column(name="title",nullable=false)
    private String title;
	
	@Column(name="waiterid",nullable=false)
    private int waiterid;
	
	@Column(name="visitedtimes",nullable=false)
    private int visitedtimes;
	
	@Column(name="usedimoney",nullable=false)
    private int usedimoney;
	
	@Column(name="leavemessagetimes",nullable=false)
    private int leavemessagetimes;
	
	@Column(name="canusewaitertime",nullable=true)
    private Date canusewaitertime;

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

    public String getPlayername(){
        return playername;
    }

    public void setPlayername(String playername){
        this.playername = playername;
    }

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }

    public int getLevel(){
        return level;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public int getStyle(){
        return style;
    }

    public void setStyle(int style){
        this.style = style;
    }

    public int getRule(){
        return rule;
    }

    public void setRule(int rule){
        this.rule = rule;
    }

    public short getAreaid(){
        return areaid;
    }

    public void setAreaid(short areaid){
        this.areaid = areaid;
    }

    public int getGridsize(){
        return gridsize;
    }

    public void setGridsize(int gridsize){
        this.gridsize = gridsize;
    }

    public byte[] getItems(){
        return items;
    }

    public void setItems(byte[] items){
        this.items = items;
    }

    public byte[] getParts(){
        return parts;
    }

    public void setParts(byte[] parts){
        this.parts = parts;
    }

    public Date getLasttime(){
        return lasttime;
    }

    public void setLasttime(Date lasttime){
        this.lasttime = lasttime;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public int getWaiterid(){
        return waiterid;
    }

    public void setWaiterid(int waiterid){
        this.waiterid = waiterid;
    }

    public int getVisitedtimes(){
        return visitedtimes;
    }

    public void setVisitedtimes(int visitedtimes){
        this.visitedtimes = visitedtimes;
    }

    public int getUsedimoney(){
        return usedimoney;
    }

    public void setUsedimoney(int usedimoney){
        this.usedimoney = usedimoney;
    }

    public int getLeavemessagetimes(){
        return leavemessagetimes;
    }

    public void setLeavemessagetimes(int leavemessagetimes){
        this.leavemessagetimes = leavemessagetimes;
    }

    public Date getCanusewaitertime(){
        return canusewaitertime;
    }

    public void setCanusewaitertime(Date canusewaitertime){
        this.canusewaitertime = canusewaitertime;
    }
}
