package com.pip.itimes.server.world.game;

import com.pip.itimes.server.world.InstanceDefinition;

public class FarmInstance extends Instance{

	private int ownerid;
	
	public FarmInstance(int id, InstanceDefinition idf, InstanceService service) {
		super(id, idf, service);
	}
	
	public void setOwnerId(int ownerid){
		this.ownerid = ownerid;
	}
	
	public int getOwnerId(){
		return ownerid;
	}

}
