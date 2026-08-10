package com.pip.server.account.stub;

@SuppressWarnings("serial")
public class SequenceException extends Exception {
	
	
	public SequenceException(String message, Throwable cause) {
		super(message, cause);
	}

	public SequenceException(String msg){
		this(msg,null);
	}
}
