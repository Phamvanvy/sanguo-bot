package com.pip.net;

@SuppressWarnings("serial")
public class UnknownMessageTypeException extends Exception {
	public UnknownMessageTypeException(String msg){
		super(msg);
	}
}
