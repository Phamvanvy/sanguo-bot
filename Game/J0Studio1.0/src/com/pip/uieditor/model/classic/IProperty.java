package com.pip.uieditor.model.classic;

public interface IProperty {
	
	public String getName();
	public String getCategory();
	
	public Object getDefaultValue();
	
	public void setValue(Object value);
	
	public Object getValue();
	
	public boolean isWritable();
	
	public GWidget getOwner();
	
	public void setOwner(GWidget owner);
	
}
