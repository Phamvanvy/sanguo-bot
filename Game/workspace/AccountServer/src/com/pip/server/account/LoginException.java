package com.pip.server.account;

public class LoginException extends BaseAccountException {
	

	private static final long serialVersionUID = 6915994878005760739L;

	public LoginException(int code){
		super(code);
	}
	
}
