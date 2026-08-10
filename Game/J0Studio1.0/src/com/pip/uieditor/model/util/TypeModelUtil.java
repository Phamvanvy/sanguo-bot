package com.pip.uieditor.model.util;

import com.pip.uieditor.model.TypeModel;

public class TypeModelUtil {
	
	static TypeModel typeModel;
	
//	static {
//		typeModel = new DefaultTypeModelFactory().create();
//	}
	
	public static void setTypeModel(TypeModel typeModel) {
		typeModel = typeModel;
	}
	
	public static TypeModel getTypeModel() {
		return typeModel;
	}
}
