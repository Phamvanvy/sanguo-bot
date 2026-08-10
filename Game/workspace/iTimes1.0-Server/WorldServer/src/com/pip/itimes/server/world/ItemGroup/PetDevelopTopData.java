package com.pip.itimes.server.world.ItemGroup;

public class PetDevelopTopData {
	private int playerid;
	private String playername;
	private int camp;//阵营
	private int attrValue;
	private String petName;
	private int petID;
	private int type;
	
	public PetDevelopTopData(int type, int id, String playername, int camp, int attrValue, String petName, int petID){
		this.type = type;
		this.playerid = id;
		this.playername = playername;
		this.camp = camp;
		this.attrValue = attrValue;
		this.petName = petName;
		this.petID = petID;
	}
	
	public void setType(int type){
		this.type = type;
	}
	
	public int getType(){
		return type;
	}

	public void setPlayerID(int playerid){
		this.playerid = playerid;
	}
	
	public int getPlayerID(){
		return playerid;
	} 

	public void setPlayername(String name){
		this.playername = name;
	}
	
	public String getPlayername(){
		return playername;
	}
	
	public void setPlayerCamp(int camp){
		this.camp = camp;
	}
	
	public int getPlayerCamp(){
		return this.camp;
	}

	public void setAttrValue(int attrValue){
		this.attrValue = attrValue;
	}
	
	public int getAttrValue(){
		return attrValue;
	}
	
	public void setPetName(String petName){
		this.petName = petName;
	}
	
	public String getPetName(){
		return petName;
	}
	
	public void setPetID(int petID){
		this.petID = petID;
	}
	
	public int getPetID(){
		return petID;
	}
	
	public String toString(){
		return petName + "(" + playername + "的宠物)  培养值:" + attrValue;
	}
}
