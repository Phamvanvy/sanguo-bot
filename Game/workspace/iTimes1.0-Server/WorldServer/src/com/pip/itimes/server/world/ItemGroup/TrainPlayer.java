package com.pip.itimes.server.world.ItemGroup;

public class TrainPlayer {
	int playerid;
	String playername;
	int camp;//阵营
	int playertrainlevel;//玩家聚灵等级
	int playerusetrainpoint;//玩家使用的聚灵点
	
	public TrainPlayer(int id,String name,int playercamp,int trainlevel,int usepoint){
		this.playerid = id;
		this.playername = name;
		this.camp = playercamp;
		this.playertrainlevel = trainlevel;
		this.playerusetrainpoint = usepoint;
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

	public void setPlayerTrainLevel(int level){
		this.playertrainlevel = level;
	}
	
	public int getPlayerTrainLevel(){
		return this.playertrainlevel;
	}

	public void setPlayerCamp(int camp){
		this.camp = camp;
	}
	
	public int getPlayerCamp(){
		return this.camp;
	}
	
	public void setPlayerUseTrainPoint(int point){
		this.playerusetrainpoint = point;
	}
	
	public int getPlayerUseTrainPoint(){
		return this.playerusetrainpoint;
	}
	
}
