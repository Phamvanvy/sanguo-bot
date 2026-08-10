package com.pip.itimes.server.stage;

public class PetColorEffect extends Effect{
	private short colorIndex;
	private byte changeWay; //0:指定 ；1：随机
	private byte petType; //宠物类型
	private byte petBindType;//几代宠   0:1代，1:2代
	
	public PetColorEffect(short colorIndex, byte changeWay, byte type, byte bindType){
		this.colorIndex = colorIndex;
		this.changeWay = changeWay;
		this.petType = type;
		this.petBindType = bindType;
	}
	
	public byte getType() {
		return 84;
	}
	
	public int getColorIndex(){
		return colorIndex;
	}
	
	public byte getChangeWay(){
		return changeWay;
	}
	
	public byte getPetType(){
		return petType;
	}
	
	public byte getBindType(){
		return petBindType;
	}
}
