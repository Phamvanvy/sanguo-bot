package com.pip.itimes.server.stage;

public class PetSetupEffect extends Effect{
	private int perceptionLevel;
	private int spiritualLevel;
	
	public PetSetupEffect(int perceptionLevel, int spiritualLevel){
		this.perceptionLevel = perceptionLevel;
		this.spiritualLevel = spiritualLevel;
	}
	
	@Override
	public byte getType() {
		return 80;
	}
	
	public int getPerceptionLevel(){
		return perceptionLevel;
	}
	
	public int getSpiritualLevel(){
		return spiritualLevel;
	}
}
