package com.pip.itimes.server.stage;

public class LeadershipEffect extends Effect {
	
	private int value;
	
	public LeadershipEffect(int value){
		this.value = value;
	}
	
	@Override
	public byte getType() {
		return 77;
	}
	
	public int getValue(){
		return value;
	}
}
