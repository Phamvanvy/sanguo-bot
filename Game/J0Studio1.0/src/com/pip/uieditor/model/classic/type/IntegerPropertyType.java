package com.pip.uieditor.model.classic.type;



public class IntegerPropertyType implements PropertyType{
	
	public static final IntegerPropertyType TYPE = new IntegerPropertyType();
	
	public String getId() {
		return "int";
	}
	
	@Override
	public String to(Object field) {
		if(!(field instanceof Integer)) 
			throw new IllegalArgumentException();
		return String.valueOf(field);
	}

	@Override
	public Integer from(String s) {
		return Integer.decode(s);
	}
}
