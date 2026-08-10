package com.pip.itimes.server.stage;

public class BloodNpcType extends TaskNpcType{
	
	public int group;
	
	public BloodNpcType(int id, String name, int type) {
		super(id, name, type);
	}

	public int getGroup(){
		return group;
	}
	
}
