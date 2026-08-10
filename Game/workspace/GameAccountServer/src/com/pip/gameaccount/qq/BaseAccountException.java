package com.pip.gameaccount.qq;

public class BaseAccountException extends Exception {
	

	private int code;
	
	public BaseAccountException(int code){
		super();
		this.code = code;
	}
	
	public BaseAccountException(String msg,int code){
		super(msg);
		this.code = code;
	}
	
	public BaseAccountException(String msg,Throwable throwable,int code){
		super(msg,throwable);
		this.code = code;
	}
	
	public int getCode(){
		return code;
	}
}
