package com.pip.itimes.server.stage;

public class Love7Effect extends Effect{
	private int groupID;
	
	public Love7Effect(int groupID){
		this.groupID = groupID;
	}
	
	public byte getType() {
		return 82;
	}
	
	public int getGroupID(){
		return groupID;
	}

}