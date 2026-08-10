package com.pip.itimes.server.world.ItemGroup;

public class MagicPositionPlayer {
	int playerid;
	String playername;
	int camp;//阵营
	int playermagiclevel;//玩家法阵等级
	int playermagicfloor;//玩家法阵阶层
	int playermagicexp;//玩家法阵经验
	
	public MagicPositionPlayer(int id,String name,int playercamp,int level,int floor,int exp){
		this.playerid = id;
		this.playername = name;
		this.camp = playercamp;
		this.playermagiclevel = level;
		this.playermagicfloor = floor;
		this.playermagicexp = exp;
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

	public void setPlayerMagicLevel(int level){
		this.playermagiclevel = level;
	}
	
	public int getPlayerMagicFloor(){
		return this.playermagicfloor;
	}

	public void setPlayerMagicFloor(int floor){
		this.playermagicfloor = floor;
	}
	
	public int getPlayerMagicLevel(){
		return this.playermagiclevel;
	}
	
	public void setPlayerMagicExp(int exp){
		this.playermagicexp = exp;
	}
	
	public int getPlayerMagicExp(){
		return this.playermagicexp;
	}
	
	public void setPlayerCamp(int camp){
		this.camp = camp;
	}
	
	public int getPlayerCamp(){
		return this.camp;
	}

}
