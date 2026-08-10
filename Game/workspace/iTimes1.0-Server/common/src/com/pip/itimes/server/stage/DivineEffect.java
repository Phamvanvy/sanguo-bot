package com.pip.itimes.server.stage;

public class DivineEffect extends Effect {
	private int value;
	
	public DivineEffect(int value){
		this.value = value;
	}
	
	@Override
	public byte getType() {
		return 105;
	}
	
	public int getValue(){
		return value;
	}
}
