package com.pip.itimes.server.stage;

public class AddItemAnimate extends Effect {
	private byte nameIndex;
	private byte lifeCycle;
	public AddItemAnimate(byte index,byte cycle){
		nameIndex=index;
		lifeCycle=cycle;
	}
	public byte getIndex(){
		return nameIndex;
	}
	
	public byte getLifeCycle(){
		return lifeCycle;
	}
	
	public byte getType() {
		return 73;
	}

}
