package com.pip.net;

public class AbstractRequest implements IRequest {
	
	protected int id;
	protected int type;

	public AbstractRequest(int id,int type){
		this.id = id;
		this.type = type;
	}
	
	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

}
