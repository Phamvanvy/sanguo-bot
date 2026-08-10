package com.pip.server.account;

public class ChangeStatusException extends BaseAccountException {

	private static final long serialVersionUID = 8354844896491230009L;

	public ChangeStatusException(int code){
		super(code);
	}
}
