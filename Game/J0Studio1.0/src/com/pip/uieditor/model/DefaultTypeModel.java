package com.pip.uieditor.model;

import java.util.HashMap;
import java.util.Map;

import com.pip.uieditor.model.classic.type.Type;

public class DefaultTypeModel implements TypeModel {

	private Map<String, Type> types;
	
	public DefaultTypeModel() {
		types = new HashMap<String, Type>();
	}
	
	@Override
	public Type getType(String name) {
		return types.get(name);
	}

	@Override
	public Type[] getTypes() {
		return types.values().toArray(new Type[types.size()]);
	}
	
	public void addType(Type type) {
		types.put(type.getName(), type);
	}

}
