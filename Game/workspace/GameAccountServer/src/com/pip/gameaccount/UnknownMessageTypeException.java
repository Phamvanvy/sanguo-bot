package com.pip.gameaccount;

@SuppressWarnings("serial")
public class UnknownMessageTypeException extends Exception {
	
	public UnknownMessageTypeException(String msg){
		super(msg);
	}
}
