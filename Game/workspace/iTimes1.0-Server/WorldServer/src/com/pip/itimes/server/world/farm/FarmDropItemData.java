package com.pip.itimes.server.world.farm;

public class FarmDropItemData {
	private int itemid;
	private int count;
	
	public FarmDropItemData(int itemid, int count){
		this.itemid = itemid;
		this.count = count;
	}
	
	public int getItemid(){
		return itemid;
	}
	
	public int getCount(){
		return count;
	}
}
