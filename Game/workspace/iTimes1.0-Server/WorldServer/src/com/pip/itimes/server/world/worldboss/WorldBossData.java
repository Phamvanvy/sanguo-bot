package com.pip.itimes.server.world.worldboss;

/**
 * @file WorldBossData.java
 * @author zxyu
 * @version 1.0.0
 * @date 2012-9-19
 **/
public class WorldBossData {
	private int level;
	private int maxlevel;
	private int hp;
	private int mp;
	private int pa;
	private int mpa;
	private int def;
	private int mgId;
	private int roundsecond;
	private int roundhardsecond;
	private int leveluptime;
	
	public WorldBossData(int level, int maxlevel, int mgId){
		this.level = level;
		this.maxlevel = maxlevel;
		this.mgId = mgId;
	}
	
	public int getLevel(){
		return level;
	}
	
	public void setLevel(int level){
		this.level = level;
	}
	
	public void setMaxLevel(int maxlevel){
		this.maxlevel = maxlevel;
	}
	
	public int getMaxLevel(){
		return maxlevel;
	}
	
	public void setHp(int hp){
		this.hp = hp;
	}
	
	public int getHp(){
		return hp;
	}
	
	public void setMp(int mp){
		this.mp = mp;
	}
	
	public int getMp(){
		return mp;
	}
	
	public void setDef(int def){
		this.def = def;
	}
	
	public int getDef(){
		return def;
	}
	
	public int getMgId(){
		return mgId;
	}
	
	public void setPa(int pa){
		this.pa = pa;
	}
	
	public int getPa(){
		return pa;
	}
	
	public void setMPa(int mpa){
		this.mpa = mpa;
	}
	
	public int getMPa(){
		return mpa;
	}
	
	public void setRoundSecond(int roundsecond){
		this.roundsecond = roundsecond;
	}
	
	public int getRoundSecond(){
		return roundsecond;
	}
	
	public void setRoundHardSecond(int roundhardsecond){
		this.roundhardsecond = roundhardsecond;
	}
	
	public int getRoundHardSecond(){
		return roundhardsecond;
	}
	
	public void setLeveluptime(int leveluptime){
		this.leveluptime = leveluptime;
	}
	
	public int getLeveluptime(){
		return leveluptime;
	}
}
