package com.pip.itimes.server.stage;

public class UpLevelEffect extends Effect{
	private int level;
	private int itemid;
	private int count;
	
	public UpLevelEffect(int level, int itemid, int count){
		this.level = level;
		this.itemid = itemid;
		this.count = count;
	}
	
	@Override
	public byte getType() {
		return 79;
	}
	
	public int getLevel(){
		return level;
	}
	
	public int getItemid(){
		return itemid;
	}
	
	public int getCount(){
		return count;
	}
}
