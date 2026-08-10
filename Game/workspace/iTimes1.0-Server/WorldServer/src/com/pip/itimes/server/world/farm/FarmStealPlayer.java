package com.pip.itimes.server.world.farm;

public class FarmStealPlayer {
	
	private int stealplayerId;	//Íµ²ËÍæ¼Òid
	private String playername;
	private long stealTime;		//Ê±¼ä

	public FarmStealPlayer() {
    }
	
	public int getstealId(){
		return stealplayerId;
	}

	public void setstealId(int id){
		this.stealplayerId = id;
	}
	
	public long getstealTime(){
		return stealTime;
	}
	
	public void setstealTime(long time){
		this.stealTime = time;
	}

	public void setPlayerName(String playername){
		this.playername = playername;
	}
	
	public String getPlayerName(){
		return playername;
	}
	
}
