package com.pip.uieditor.model.classic;


import org.apache.commons.beanutils.PropertyUtils;

import com.pip.uieditor.model.classic.type.PropertyDef;

public class DefaultProperty implements IProperty {
	
	private PropertyDef def;
	private Object value;
	
	private GWidget owner;
	
	public DefaultProperty(PropertyDef def, Object value) {
		this.def = def;
		this.value = value;
	}
	
	public DefaultProperty(PropertyDef def) {
		this(def, null);
	}
	
	@Override
	public String getName() {
		return def.getName();
	}

	@Override
	public String getCategory() {
		return def.getCategory();
	}

	@Override
	public Object getDefaultValue() {
		return def.getDefaultValue();
	}

	@Override
	public void setValue(Object value) {
		if(def.isNative()) {
			try {
				setNativeValue(value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Object old = getValue();
			if(old != null && old.equals(value)) {
				return;
			} 
			this.value = value;
			if(owner != null) {
				owner.firePropertyChange(getName(), old, this.value);
			}
		}
	}

	@Override
	public Object getValue() {
		if(def.isNative()) {
			try {
				return getNativeValue();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return value == null ? def.getDefaultValue() : value;
		}
	}

	@Override
	public boolean isWritable() {
		return true;
	}
	
	@Override
	public GWidget getOwner() {
		return owner;
	}

	@Override
	public void setOwner(GWidget owner) {
		this.owner = owner;
	}
	
	protected void setNativeValue(Object value) throws Exception {
		PropertyUtils.setProperty(getOwner(), def.getFieldName(), value);
	}
	
	protected Object getNativeValue() throws Exception{
		return PropertyUtils.getProperty(getOwner(), def.getFieldName());
	}
}
