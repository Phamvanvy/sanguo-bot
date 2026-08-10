package com.pip.itimes.server.stage;

public class UseChangeItemEffect extends Effect {
	private int needItemID;
	private int needItemCount;
	private int changeItemID;
	private int changeItemCount;
	
	public UseChangeItemEffect(int needItemID, int needItemCount, int changeItemID, int changeItemCount){
		this.needItemID = needItemID;
		this.needItemCount = needItemCount;
		this.changeItemID = changeItemID;
		this.changeItemCount = changeItemCount;
	}
	
	@Override
	public byte getType() {
		return 104;
	}
	
	public int getNeedItemID(){
		return needItemID;
	}
	
	public int getNeedItemCount(){
		return needItemCount;
	}
	
	public int getChangeItemID(){
		return changeItemID;
	}
	
	public int getChangeItemCount(){
		return changeItemCount;
	}
}
