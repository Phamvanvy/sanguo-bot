package com.pip.itimes.server.stage;

public class SeedEffect extends Effect{

	private int seedID = 0;
	
	public byte getType() {
		return 85;
	}
	
	public SeedEffect(int seedID){
		this.seedID = seedID;
	}
	
	public int getSeedID(){
		return seedID;
	}
}
