package com.pip.uieditor.model.classic.type;

import com.pip.uieditor.model.ARGB;


public class ARGBPropertyType implements PropertyType {
	
	public static final ARGBPropertyType TYPE = new ARGBPropertyType();
	
	public String getId() {
		return "argb";
	}
	
	@Override
	public String to(Object value) {
		ARGB field = (ARGB)value;
		return String.format("[%d,%d,%d,%d]", field.alpha, field.red, field.green, field.blue);
	}

	@Override
	public ARGB from(String s) {
		if(!s.startsWith("[") || !s.endsWith("]"))
			throw new IllegalArgumentException();
		String[] ss = s.substring(1, s.length() - 1).split(",");
		if(ss.length != 4)
			throw new IllegalArgumentException();
		return new ARGB(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]), Integer.parseInt(ss[2]), Integer.parseInt(ss[4]));
	}
	
	
}
