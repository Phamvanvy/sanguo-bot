package com.pip.itimes.server.world.message;

public class ChannelGiftData {
	private long startTime;
	private long endTime;
	
	private int[] itemid;
	
	public void setStartTime(long startTime){
		this.startTime = startTime;
	}
	
	public long getStartTime(){
		return startTime;
	}
	
	public void setEndTime(long endTime){
		this.endTime = endTime;
	}
	
	public long getEndTime(){
		return endTime;
	}
	
	public void setItemId(int[] itemid){
		this.itemid = itemid;
	}
	
	public int[] getItemId(){
		return itemid;
	}
	
	public int getItemCount(){
		return itemid.length;
	}
}
