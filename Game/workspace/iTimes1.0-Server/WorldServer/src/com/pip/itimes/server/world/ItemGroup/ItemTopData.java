package com.pip.itimes.server.world.ItemGroup;

public class ItemTopData {
	private int playerid;
	private int consume;
	
	public ItemTopData(int playerid, int consume){
		this.playerid = playerid;
		this.consume = consume;
	}
	
	public void setPlayerID(int playerid){
		this.playerid = playerid;
	}
	
	public int getPlayerID(){
		return playerid;
	}
	
	public void setConsume(int consume){
		this.consume = consume;
	}
	
	public int getConsume(){
		return consume;
	}
	
	
}
