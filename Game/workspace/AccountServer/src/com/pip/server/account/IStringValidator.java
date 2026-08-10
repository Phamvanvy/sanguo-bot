package com.pip.server.account;

public interface IStringValidator {
	
	public static final int OK = 0;
	public static final int MAX_LIMIT = 1;
	public static final int MIN_LIMIT = 2;
	public static final int ILLEGAL_CHAR = 3;
	public static final int NULL = 4;
	public static final int ERROR_PATTERN = 5;
	
	int valid(String value);
}
