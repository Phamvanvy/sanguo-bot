package com.pip.uieditor.model.classic.type;

import org.eclipse.draw2d.geometry.Point;

public class PointPropertyType implements PropertyType {

	public static final PointPropertyType TYPE = new PointPropertyType();
	
	@Override
	public String getId() {
		return "point";
	}

	@Override
	public String to(Object value) {
		Point point = (Point)value;
		return String.format("%d,%d", point.x, point.y);
	}

	@Override
	public Object from(String s) {
		String[] ss = s.split(",");
		return new Point(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
	}

}
