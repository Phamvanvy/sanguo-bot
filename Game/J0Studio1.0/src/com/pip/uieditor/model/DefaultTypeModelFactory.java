package com.pip.uieditor.model;

import org.eclipse.swt.graphics.RGB;

import com.pip.uieditor.model.classic.type.ARGBPropertyType;
import com.pip.uieditor.model.classic.type.BooleanPropertyType;
import com.pip.uieditor.model.classic.type.EnumPropertyType;
import com.pip.uieditor.model.classic.type.IntegerPropertyType;
import com.pip.uieditor.model.classic.type.PropertyDef;
import com.pip.uieditor.model.classic.type.StringPropertyType;
import com.pip.uieditor.model.classic.type.Type;

public class DefaultTypeModelFactory {

//	@Override
//	public TypeModel create() {
//		DefaultTypeModel model = new DefaultTypeModel();
//		addContainerTypeModel(model);
//		addLabelTypeModel(model);
//		addTextFieldTypeModel(model);
//		addButtonTypeModel(model);
//		return model;
//	}
//	
//	protected void addContainerTypeModel(DefaultTypeModel model) {
//		Type type = new Type("Container", true);
//		initWidgetTypeModel(type);
//		model.addType(type);
//	}
//	
//	protected void initWidgetTypeModel(Type type) {
//		new PropertyDef(type, new BooleanPropertyType(), "Normal.visible", Boolean.TRUE);
//		new PropertyDef(type, new BooleanPropertyType(), "Normal.focusable", Boolean.TRUE);
//		new PropertyDef(type, new BooleanPropertyType(), "Normal.enable", Boolean.TRUE);
//		new PropertyDef(type, new BooleanPropertyType(), "Border.painted", Boolean.TRUE);
//		new PropertyDef(type, new IntegerPropertyType(), "Border.x", Integer.valueOf(0));
//		new PropertyDef(type, new IntegerPropertyType(), "Border.y", Integer.valueOf(0));
//		new PropertyDef(type, new IntegerPropertyType(), "Border.width", Integer.valueOf(0));
//		new PropertyDef(type, new IntegerPropertyType(), "Border.height", Integer.valueOf(0));
//		new PropertyDef(type, new BooleanPropertyType(), "Backgroud.painted", Boolean.TRUE);
//		new PropertyDef(type, new ARGBPropertyType(), "Backgroud.color", new RGB(0xFF, 0xFF, 0xFF));
//		new PropertyDef(type, new ARGBPropertyType(), "Foregroud.color", new RGB(0xFF, 0xFF, 0xFF));
//	}
//	
//	protected void addLabelTypeModel(DefaultTypeModel model) {
//		Type type = new Type("Label");
//		initWidgetTypeModel(type);
//		type.getPropertyDef("Normal.focusable").setDefaultValue(Boolean.FALSE);
//		new PropertyDef(type, new StringPropertyType(), "Text.text", "");
//		new PropertyDef(type, new BooleanPropertyType(), "Text.3d",
//				Boolean.FALSE);
//		new PropertyDef(type, new ARGBPropertyType(), "Text.color", new RGB(
//				0x00, 0x00, 0x00));
//		new PropertyDef(type, new EnumPropertyType(new String[] { "LEFT",
//				"HCENTER", "RIGHT" }), "Text.h_align", Integer.valueOf(1));
//		new PropertyDef(type, new EnumPropertyType(new String[] { "TOP",
//				"MIDDLE", "BOTTOM" }), "Text.v_align", Integer.valueOf(1));
//		new PropertyDef(type, new ARGBPropertyType(), "Text.bordercolor",
//				new RGB(0xFF, 0xFF, 0xFF));
//		model.addType(type);
//	}
//	
//	protected void addTextFieldTypeModel(DefaultTypeModel model) {
//		Type type = new Type("TextField");
//		initWidgetTypeModel(type);
//		new PropertyDef(type, new StringPropertyType(), "Text.text", "");
//		new PropertyDef(type, new IntegerPropertyType(), "Text.maxlen", Integer.valueOf(11));
//		model.addType(type);
//	}
//	
//	protected void addButtonTypeModel(DefaultTypeModel model) {
//		Type type = new Type("Button");
//		initWidgetTypeModel(type);
//		new PropertyDef(type, new StringPropertyType(), "Text.text", "");
//		new PropertyDef(type, new BooleanPropertyType(), "Text.3d",
//				Boolean.FALSE);
//		new PropertyDef(type, new ARGBPropertyType(), "Text.color", new RGB(
//				0x00, 0x00, 0x00));
//		new PropertyDef(type, new EnumPropertyType(new String[] { "LEFT",
//				"HCENTER", "RIGHT" }), "Text.h_align", Integer.valueOf(1));
//		new PropertyDef(type, new EnumPropertyType(new String[] { "TOP",
//				"MIDDLE", "BOTTOM" }), "Text.v_align", Integer.valueOf(1));
//		new PropertyDef(type, new ARGBPropertyType(), "Text.bordercolor",
//				new RGB(0xFF, 0xFF, 0xFF));
//		new PropertyDef(type, new StringPropertyType(), "Image.file", "");
//		new PropertyDef(type, new IntegerPropertyType(), "Image.index",
//				Integer.valueOf(0));
//		new PropertyDef(type, new EnumPropertyType(new String[] { "none",
//				"mirror_rot180", "mirror", "rot180", "mirror_rot270", "rot90",
//				"rot270", "mirror_rot90" }), "Image.rotate", Integer.valueOf(0));
//		model.addType(type);
//	}
	
}
