package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class _ChangeStatusMessage extends AbstractMessage {
	protected String name;
	protected int status;
	protected String message;
	
	public _ChangeStatusMessage(int serial,String name,int status,String message){
		super(GameAccountMessageType._CHANGE_STATUS,serial);
		this.name = name;
		this.status = status;
		this.message = message;
	}
	
	public _ChangeStatusMessage(String name,int status,String message){
		super(GameAccountMessageType._CHANGE_STATUS);
		this.name = name;
		this.status = status;
		this.message = message;
	}

	public String getName() {
		return name;
	}


	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}
}
