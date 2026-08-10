package com.pip.server.account;

public class NumberStringValidator implements IStringValidator {

	public int valid(String value) {
		for(int i=0;i<value.length();i++){
			char c = value.charAt(i);
			if(c<'0'||c>'9')
				return IStringValidator.ILLEGAL_CHAR;
		}
		return IStringValidator.OK;
	}

}
