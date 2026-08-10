package com.pip.itimes.server.bean;

import java.util.Date;


public class Tong {

    public static final byte OWNER = 100;
    public static final byte VICE_OWNER = 99;
    public static final byte MUTE_MEMBER = 13;
    public static final byte ADVANCED_MEMEBER = 2;
    public static final byte MEMBER = 1;
    public static final byte NONE = -1;

    private int id;
    private String tongName;
    private Date createTime;
    private int owner;
    private String slogan;
    private int level;
    private int money;
    private int resource;
    private int health;
    private Date lastRepairTime;
    private int credit;
    private int topListHot;
    private int topListOnline;
    private int leastCredit;
    private boolean valid;

    public Tong() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTongName() {
        return this.tongName;
    }

    public void setTongName(String tongName) {
        this.tongName = tongName;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getOwner() {
        return this.owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public String getSlogan() {
        return this.slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMoney() {
        return this.money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getResource() {
        return this.resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }

    public int getHealth() {
        return this.health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public Date getLastRepairTime() {
        return this.lastRepairTime;
    }

    public int getCredit() {
        return credit;
    }

    public void setLastRepairTime(Date lastRepairTime) {
        this.lastRepairTime = lastRepairTime;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public int getTopListHot(){
        return this.topListHot;
    }

    public void setTopListHot(int topListHot){
        this.topListHot = topListHot;
    }

    public int getTopListOnline(){
        return this.topListOnline;
    }

    public int getLeastCredit() {
        return leastCredit;
    }

    public void setTopListOnline(int topListOnline){
        this.topListOnline = topListOnline;
    }

    public void setLeastCredit(int leastCredit) {
        this.leastCredit = leastCredit;
    }
    
    public void setValid(boolean valid){
    	this.valid = valid;
    }
    
    public boolean getValid(){
    	return valid;
    }
}
