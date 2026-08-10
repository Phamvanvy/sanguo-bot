package com.pip.net.message;

public class ErrorMessage extends AbstractMessage {
	
	protected int code;
	
	public ErrorMessage(int serial,int code){
		super(ServerMessageType.ERROR,serial,true);
		this.code = code;
	}
	
	public int getCode(){
		return code;
	}
}
