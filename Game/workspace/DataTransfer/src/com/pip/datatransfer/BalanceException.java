package com.pip.datatransfer;

public class BalanceException extends BaseAccountException {

	private static final long serialVersionUID = -845357612855709689L;

	public BalanceException(int code){
		super(code);
	}
}
