package com.pip.itimes.server.world;

import com.pip.itimes.server.bean.Mercenary;

public class MercenaryShop {
	private int id;
	private int profession;
	private int price;
	private String name;
	private byte sex;
	private int fire;
	private int playerid;
	private int face;
	private Mercenary mercenary;
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	public void setProfession(int profession){
		this.profession = profession;
	}
	
	public int getProfession(){
		return profession;
	}
	
	public void setPrice(int price){
		this.price = price;
	}
	
	public int getPrice(){
		return price;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setSex(byte sex){
		this.sex = sex;
	}
	
	public byte getSex(){
		return sex;
	}
	
	public void setFire(int fire){
		this.fire = fire;
	}
	
	public int getFire(){
		return fire;
	}
	
	public void setPlayerid(int playerid){
		this.playerid = playerid;
	}
	
	public int getPlayerid(){
		return playerid;
	}
	
	public void setFace(int face){
		this.face = face;
	}
	
	public int getFace(){
		return face;
	}
	
	public void setMercenary(Mercenary mercenary){
		this.mercenary = mercenary;
	}
	
	public Mercenary getMercenary(){
		return mercenary;
	}
}
