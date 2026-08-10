package com.pip.itimes.server.stage;

public class AddIMoneyQuan extends Effect{
	private int value;
	private int server;
	
	public AddIMoneyQuan(int value, int server){
		this.value = value;
		this.server = server;
	}
	
	public int getValue(){
		return value;
	}
	
	public int getServer(){
		return server;
	}
	
	public byte getType() {
		return 72;
	}
}
