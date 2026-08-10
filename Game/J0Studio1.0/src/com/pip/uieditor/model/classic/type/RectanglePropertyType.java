package com.pip.uieditor.model.classic.type;

import org.eclipse.draw2d.geometry.Rectangle;

public class RectanglePropertyType implements PropertyType {

	public static final RectanglePropertyType TYPE = new RectanglePropertyType();
	
	@Override
	public String getId() {
		return "rect";
	}

	@Override
	public String to(Object value) {
		Rectangle rect = (Rectangle)value;
		return String.format("%d,%d,%d,%d", rect.x, rect.y, rect.width, rect.height);
	}

	@Override
	public Object from(String s) {
		String[] ss = s.split(",");
		return new Rectangle(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]), Integer.parseInt(ss[2]), Integer.parseInt(ss[3]));
	}

}
