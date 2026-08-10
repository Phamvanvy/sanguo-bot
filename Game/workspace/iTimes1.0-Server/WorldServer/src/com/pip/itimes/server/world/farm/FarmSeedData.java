package com.pip.itimes.server.world.farm;

public class FarmSeedData {
	private int id = 0;
	private String name = null;
	private int hp = 0;
	private int ap = 0;
	private int growCycle = 60;
	private int landLevel = 0;
	private int fruitCount = 0;
	private int resultsid = 0;

	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setHP(int hp){
		this.hp = hp;
	}
	
	public int getHP(){
		return hp;
	}
	
	public void setAP(int ap){
		this.ap = ap;
	}
	
	public int getAP(){
		return ap;
	}
	
	public void setGrowCycle(int growCycle){
		this.growCycle = growCycle;
	}
	
	public int getGrowCycle(){
		return growCycle;
	}
	
	public void setLandLevel(int landLevel){
		this.landLevel = landLevel;
	}
	
	public int getLandLevel(){
		return landLevel;
	}
	
	public void setFruitCount(int fruitCount){
		this.fruitCount = fruitCount;
	}
	
	public int getFruitCount(){
		return fruitCount;
	}
	
	public void setResultsid(int resultsid){
		this.resultsid = resultsid;
	}
	
	public int getResultsid(){
		return resultsid;
	}
}
