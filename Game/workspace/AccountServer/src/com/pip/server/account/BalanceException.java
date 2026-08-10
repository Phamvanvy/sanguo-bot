package com.pip.server.account;

public class BalanceException extends BaseAccountException {

	private static final long serialVersionUID = -845357612855709689L;

	public BalanceException(int code){
		super(code);
	}
}
