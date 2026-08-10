package com.pip.itimes.server.bean;

import java.util.Date;

public class Mercenary implements java.io.Serializable {
	
	public static final byte STATE_SHOP = 0;
	public static final byte STATE_BUY = 1;
	public static final byte STATE_SLEEP = 2;
	public static final byte STATE_USE = 3;
	public static final byte STATE_LAYOFF = 4;
	public static final byte STATE_AUTOREMOVE = 5;		//自动解除
	public static final byte STATE_PLAYERREMOVE = 6;		//玩家自己移除
	
	private int id;
	private int masterid;
	private int accountid;
	private int buyplayerid;
	private byte profession;
	private String playername;
	private int level;
	private byte sex;
	private byte camp;
	private int price;
	private Date createtime;
	private Date buytime;
	private Date leavetime;
	private int usetime;
	private int battletime;
	private int face;
	private int viany;
	private int strength;
	private int agility;
	private int vitality;
	private int intelligence;
	private byte[] abilities;
	private byte[] usedequipments;
	private byte state;
	private boolean valid;
	
	public Mercenary(){
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	public void setMasterid(int masterid){
		this.masterid = masterid;
	}
	
	public int getMasterid(){
		return masterid;
	}
	
	public void setAccountid(int accountid){
		this.accountid = accountid;
	}
	
	public int getAccountid(){
		return accountid;
	}
	
	public void setBuyplayerid(int buyplayerid){
		this.buyplayerid = buyplayerid;
	}
	
	public int getBuyplayerid(){
		return buyplayerid;
	}
	
	public void setProfession(byte profession){
		this.profession = profession;
	}
	
	public byte getProfession(){
		return profession;
	}
	
	public void setPlayername(String playername){
		this.playername = playername;
	}
	
	public String getPlayername(){
		return playername;
	}
	
	public void setLevel(int level){
		this.level = level;
	}
	
	public int getLevel(){
		return level;
	}
	
	public void setSex(byte sex){
		this.sex = sex;
	}
	
	public byte getSex(){
		return sex;
	}
	
	public void setCamp(byte camp){
		this.camp = camp;
	}
	
	public byte getCamp(){
		return camp;
	}
	
	public void setPrice(int price){
		this.price = price;
	}
	
	public int getPrice(){
		return price;
	}
	
	public void setCreatetime(Date createtime){
		this.createtime = createtime;
	}
	
	public Date getCreatetime(){
		return createtime;
	}
	
	public void setBuytime(Date buytime){
		this.buytime = buytime;
	}
	
	public Date getBuytime(){
		return buytime;
	}
	
	public void setLeavetime(Date leavetime){
		this.leavetime = leavetime;
	}
	
	public Date getLeavetime(){
		return leavetime;
	}
	
	public void setUsetime(int usetime){
		this.usetime = usetime;
	}
	
	public int getUsetime(){
		return usetime;
	}
	
	public void setBattletime(int battletime){
		this.battletime = battletime;
	}
	
	public int getBattletime(){
		return battletime;
	}
	
	public void setFace(int face){
		this.face = face;
	}
	
	public int getFace(){
		return face;
	}
	
	public void setViany(int viany){
		this.viany = viany;
	}
	
	public int getViany(){
		return viany;
	}
	
	public void setStrength(int strength){
		this.strength = strength;
	}
	
	public int getStrength(){
		return strength;
	}
	
	public void setAgility(int agility){
		this.agility = agility;
	}
	
	public int getAgility(){
		return agility;
	}
	
	public void setVitality(int vitality){
		this.vitality = vitality;
	}
	
	public int getVitality(){
		return vitality;
	}
	
	public void setIntelligence(int intelligence){
		this.intelligence = intelligence;
	}
	
	public int getIntelligence(){
		return intelligence;
	}
	
	public void setAbilities(byte[] abilities){
		this.abilities = abilities;
	}
	
	public byte[] getAbilities(){
		return abilities;
	}
	
	public void setUsedequipments(byte[] usedquipments){
		this.usedequipments = usedquipments;
	}
	
	public byte[] getUsedequipments(){
		return usedequipments;
	}
	
	public void setState(byte state){
		this.state = state;
	}
	
	public byte getState(){
		return state;
	}
	
	public void setValid(boolean valid){
		this.valid = valid;
	}
	
	public boolean getValid(){
		return valid;
	}
	
}
