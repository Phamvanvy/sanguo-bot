package com.pip.itimes.server.bean;

import com.pip.itimes.server.util.PropertyPool;

public class Farm {
	private int id;
	private int playerid;
	private String playername;
	private byte landcount;
	private byte[] landinfo;
	/**
     * ²ÎÊý³Ø
     */
    private PropertyPool otherPool;
    
    public Farm(){
    }
    
    public void setId(int id){
    	this.id = id;
    }
    
    public int getId(){
    	return id;
    }
    
    public void setPlayerid(int playerid){
    	this.playerid = playerid;
    }
    
    public int getPlayerid(){
    	return playerid;
    }
    
    public void setPlayerName(String playername){
    	this.playername = playername;
    }
    
    public String getPlayerName(){
    	return playername;
    }
    
    public void setLandcount(byte landcount){
    	this.landcount = landcount;
    }
    
    public byte getLandcount(){
    	return landcount;
    }
    
    public void setLandinfo(byte[] landinfo){
    	this.landinfo = landinfo;
    }
    
    public byte[] getLandinfo(){
    	return landinfo;
    }
    
    public PropertyPool getOtherPool () {
    	return otherPool;
    }
    
    public void setOtherPool (PropertyPool otherPool) {
    	this.otherPool = otherPool;
    }
}
