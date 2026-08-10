package com.pip.itimes.server.stage;

public class TongShopNpcType extends TaskNpcType {
	
	private short islandID;
	private int group;
	
	public TongShopNpcType(int id, String name, int type) {
		super(id, name, type);
	}
	
	public short getIslandID(){
		return islandID;
	}
	
	public int getGroup(){
		return group;
	}
	
	public void setIslandID(short id){
		islandID = id;
	}
	
	public void setGroup(int tmpGroup){
		group = tmpGroup;
	}
}
