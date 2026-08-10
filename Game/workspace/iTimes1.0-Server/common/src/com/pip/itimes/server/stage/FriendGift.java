package com.pip.itimes.server.stage;

public class FriendGift extends Effect{
	private byte sex;
	private int groupID;
	
	public FriendGift(byte sex, int groupID){
		this.sex = sex;
		this.groupID = groupID;
	}
	
	public int getSex(){
		return sex;
	}
	
	public int getGroupID(){
		return groupID;
	}
	
	public byte getType() {
		return 71;
	}
}
