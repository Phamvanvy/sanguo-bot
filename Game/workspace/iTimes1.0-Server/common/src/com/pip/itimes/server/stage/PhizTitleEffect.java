package com.pip.itimes.server.stage;

public class PhizTitleEffect extends Effect {
	
	private byte phizType;
	private short phizIndex;
	private String phizName;
	
	public PhizTitleEffect(byte type,short index,String name){
		this.phizType = type;
		this.phizIndex = index;
		this.phizName = name;
	}
	@Override
	public byte getType() {
		return 76;
	}
	
	public byte getPhizType(){
		return phizType;
	}
	
	public short getPhizIndex(){
		return phizIndex;
	}
	
	public String getPhizName(){
		return phizName;
	}
}
