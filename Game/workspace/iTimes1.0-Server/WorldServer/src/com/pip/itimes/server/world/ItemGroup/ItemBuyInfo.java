package com.pip.itimes.server.world.ItemGroup;

public class ItemBuyInfo {
	private int count;
	private long startTimer;
	private long lastTimer;
	private int itemid;
	
	public void setCount(int count){
		this.count = count;
	}
	
	public int getCount(){
		return count;
	}
	
	public void setLastTimer(long lastTimer){
		this.lastTimer = lastTimer;
	}
	
	public long getLastTimer(){
		return lastTimer;
	}
	
	public void setStartTimer(long startTimer){
		this.startTimer = startTimer;
	}
	
	public long getStartTimer(){
		return startTimer;
	}
	
	public void setItemID(int itemid){
		this.itemid = itemid;
	}
	
	public int getItemID(){
		return itemid;
	}
}
