package com.pip.itimes.server.world.noahsark;

public class NoahsarkDonateMaterial {
	private int itemId;
	private int itemCount;
	
	public NoahsarkDonateMaterial() {
		
	}
	public NoahsarkDonateMaterial(int itemId, int itemCount) {
		
		this.itemId = itemId;
		this.itemCount = itemCount;
	}
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public int getItemCount() {
		return itemCount;
	}
	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}
	
}
