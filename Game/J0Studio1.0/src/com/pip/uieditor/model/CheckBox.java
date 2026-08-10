package com.pip.uieditor.model;

import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.uieditor.model.annotation.Property;

public class CheckBox extends Widget {
	public static final Button PROTOTYPE = new Button();
	
	@Property(type=TextPropertyDescriptor.class)
	private String action = "";
	
	public CheckBox() {
		super("CheckBox");
	}
	
	@Override
	public void initFlags() {
		setFocusable(true);
		setClickable(true);
		setLongClickable(false);
		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(false);
		setScrollContainer(false);
	}
	
	@Override
	public CheckBox clone() {
		CheckBox ret = new CheckBox();
		fillCloneWidget(ret);
		return ret;
	}
	
	@Override
	public String getDefaultName() {
		return "chk";
	}
	
	public void setAction(String action) {
		String old = this.action;
		this.action = action;
		firePropertyChange("action", old, this.action);
	}
	
	public String getAction() {
		return this.action;
	}
	
	@Override
	public String[] getEvents() {
		return new String[]{action};
	}
}
