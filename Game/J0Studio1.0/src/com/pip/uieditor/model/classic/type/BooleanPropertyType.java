package com.pip.uieditor.model.classic.type;

public class BooleanPropertyType extends EnumPropertyType{
	
	public static final BooleanPropertyType TYPE = new BooleanPropertyType();
	
	public BooleanPropertyType() {
		super(new String[]{"NO", "YES"});
	}
	
	public String getId() {
		return "boolean";
	}
	
	@Override
	public String to(Object value) {
		if(Boolean.TRUE.equals(value)) {
			return "YES";
		} else {
			return "NO";
		}
	}

	@Override
	public Object from(String s) {
		if(s.equals("NO")) {
			return Boolean.FALSE;
		} else if(s.equals("YES")) {
			return Boolean.TRUE;
		}
		throw new IllegalArgumentException();
	}
}
