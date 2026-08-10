package com.pip.uieditor.model.classic.type;



public class StringPropertyType implements PropertyType {

	public static final StringPropertyType TYPE  = new StringPropertyType();
	
	public String getId() {
		return "string";
	}
	
	@Override
	public String to(Object value) {
		return (String)value;
	}

	@Override
	public String from(String s) {
		return s;
	}
}
