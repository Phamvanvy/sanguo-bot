package com.pip.server.account;

import java.util.regex.Pattern;

public class PatternStringValidator implements IStringValidator {

	protected Pattern pattern;
	
	public PatternStringValidator(String regex){
		pattern = Pattern.compile(regex);
	}
	
	public int valid(String value) {
		if(pattern.matcher(value).matches()){
			return IStringValidator.OK;
		}
		return IStringValidator.ERROR_PATTERN;
	}

}
