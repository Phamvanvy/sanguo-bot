package com.pip.uieditor.model.classic.type;

import org.eclipse.draw2d.geometry.Dimension;

public class DimensionPropertyType implements PropertyType {
	
	public static final DimensionPropertyType TYPE = new DimensionPropertyType();

	@Override
	public String getId() {
		return "dim";
	}

	@Override
	public String to(Object value) {
		Dimension dim = (Dimension)value;
		return String.format("%d,%d", dim.width, dim.height);
	}

	@Override
	public Object from(String s) {
		String[] ss = s.split(",");
		return new Dimension(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
	}

}
