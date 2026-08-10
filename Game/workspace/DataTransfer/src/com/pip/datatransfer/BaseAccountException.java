package com.pip.datatransfer;

public class BaseAccountException extends Exception {
	

	private static final long serialVersionUID = -6218418402759010762L;

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
