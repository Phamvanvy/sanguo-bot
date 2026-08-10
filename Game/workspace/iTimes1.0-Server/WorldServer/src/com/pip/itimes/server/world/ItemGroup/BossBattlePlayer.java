package com.pip.itimes.server.world.ItemGroup;

public class BossBattlePlayer{
	int playerid;	//玩家id
	String playername;//玩家名
	int playerlevel;//等级
	int playercamp;//阵营
	int totalfloor;//玩家达到的关卡
	int roundnum;//回合数
	
	public BossBattlePlayer(int id, String name,int level,int camp,int floor,int number){
		this.playerid = id;
		this.playername = name;
		this.playerlevel = level;
		this.playercamp = camp;
		this.totalfloor = floor;
		this.roundnum = number;
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
	
	public void setPlayerLevel(int level){
		this.playerlevel = level;
	}
	
	public int getPlayerLevel(){
		return playerlevel;
	}
	
	public void setPlayerCamp(int camp){
		this.playercamp = camp;
	}
	
	public int getPlayerCamp(){
		return playercamp;
	}
	
	public void setPlayerTotalfloor(int floor){
		this.totalfloor = floor;
	}
	
	public int getPlayerTotalfloor(){
		return totalfloor;
	}
	
	public void setPlayerRound(int num){
		this.roundnum = num;
	}
	
	public int getPlayerRound(){
		return roundnum;
	}
	
}
