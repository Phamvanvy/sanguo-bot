package com.pip.uieditor.model.classic.type;

import java.util.LinkedHashMap;
import java.util.Map;

public class Type {
	private String name;
	private Map<String, PropertyDef> defs;
	private boolean container;
	
	public Type(String name, boolean container) {
		this.name = name;
		this.defs = new LinkedHashMap<String, PropertyDef>();
		this.container = container;
	}
	
	public Type(String name) {
		this(name, false);
	}
	
	public String getName() {
		return this.name;
	}
	
	public void addPropertyDef(PropertyDef def) {
		defs.put(def.getId(), def);
	}
	
	public PropertyDef[] getPropertyDefs() {
		return defs.values().toArray(new PropertyDef[defs.size()]);
	}
	
	public PropertyDef getPropertyDef(String id) {
		return defs.get(id);
	}
	
	public int size() {
		return defs.size();
	}
	
	public boolean isContainer() {
		return this.container;
	}
}
