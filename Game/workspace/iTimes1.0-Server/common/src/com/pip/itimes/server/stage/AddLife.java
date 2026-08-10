package com.pip.itimes.server.stage;

public class AddLife extends Effect{

	private int value;
	
	public AddLife(int value){
		this.value = value;
	}
	
	public int getValue(){
		return value;
	}
	
	public byte getType() {
		return 70;
	}

}
