package com.pip.uieditor.model.classic;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import com.pip.uieditor.model.ARGB;
import com.pip.uieditor.model.classic.type.ARGBPropertyType;
import com.pip.uieditor.model.classic.type.BooleanPropertyType;
import com.pip.uieditor.model.classic.type.DimensionPropertyType;
import com.pip.uieditor.model.classic.type.PointPropertyType;
import com.pip.uieditor.model.classic.type.PropertyDef;
import com.pip.uieditor.model.classic.type.RectanglePropertyType;
import com.pip.uieditor.model.classic.type.Type;

public class Model {
	
	private static Model instance;
	
	private Map<String, Type> types;
	
	public Model() {
		types = new HashMap<String, Type>();
	}
	
	public Type getType(String name) {
		return types.get(name);
	}

	public Type[] getTypes() {
		return types.values().toArray(new Type[types.size()]);
	}
	
	public void addType(Type type) {
		types.put(type.getName(), type);
	}
	
	
	public static Model getCurrent() {
		if(instance == null) {
			instance = createModel();
		}
		return instance;
	}
	
	public static Model createModel() {
		Model model = new Model();
		
		return model;
	}
	
	public static void fillWidgetProperty(Type type) {
		new PropertyDef(type, PointPropertyType.TYPE, "" , "location", new Point(0,0));
		new PropertyDef(type, DimensionPropertyType.TYPE, "", "size", new Dimension(0,0));
		new PropertyDef(type, DimensionPropertyType.TYPE, "", "preferedSize", new Dimension(-1, -1));
		new PropertyDef(type, BooleanPropertyType.TYPE, "", "borderPainted", Boolean.FALSE);
		new PropertyDef(type, ARGBPropertyType.TYPE, "", "borderColor", new ARGB(0, 0, 0, 0));
		new PropertyDef(type, BooleanPropertyType.TYPE, "", "backgroundPainted", Boolean.FALSE);
		new PropertyDef(type, ARGBPropertyType.TYPE, "", "backgroundColor", new ARGB(0, 0, 0, 0));
		new PropertyDef(type, ARGBPropertyType.TYPE, "", "foregroundColor", new ARGB(0, 0, 0, 0));
		new PropertyDef(type, BooleanPropertyType.TYPE, "", "selectionPainted", Boolean.FALSE);
		new PropertyDef(type, ARGBPropertyType.TYPE, "", "selectionColor", new ARGB(0, 0, 0, 0));
		new PropertyDef(type, BooleanPropertyType.TYPE, "", "visible", Boolean.TRUE);
		new PropertyDef(type, BooleanPropertyType.TYPE, "", "focusable", Boolean.TRUE);
		new PropertyDef(type, BooleanPropertyType.TYPE, "", "enabled", Boolean.TRUE);
		new PropertyDef(type, BooleanPropertyType.TYPE, "", "supportSelect", Boolean.FALSE);
		new PropertyDef(type, RectanglePropertyType.TYPE, "", "border", new Rectangle());
		new PropertyDef(type, RectanglePropertyType.TYPE, "", "inset", new Rectangle());
	}
}	
