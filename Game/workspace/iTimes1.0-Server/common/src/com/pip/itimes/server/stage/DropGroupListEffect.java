package com.pip.itimes.server.stage;

public class DropGroupListEffect extends Effect {

	private int group;
	private int count;
	private int paramType;
	private int param;

	public DropGroupListEffect(int group, int count, int paramType, int param) {
		this.group = group;
		this.count = count;
		this.paramType = paramType;
		this.param = param;
	}

	public byte getType() {
		return 61;
	}

	public int getGroup() {
		return group;
	}

	public int getCount() {
		return count;
	}
	
	public int getParamType(){
		return paramType;
	}
	
	public int getParam(){
		return param;
	}
}
