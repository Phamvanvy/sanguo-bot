package com.pip.uieditor.model;

import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.uieditor.model.annotation.Property;


public class Button extends Widget {
	
	public static final Button PROTOTYPE = new Button();
	
	@Property(type=TextPropertyDescriptor.class)
	private String action = "";
	
	public Button() {
		super("Button");
	}
	
	@Override
	public void initFlags() {
		setClickable(true);
		setLongClickable(false);
		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(false);
		setScrollContainer(false);
		setFocusable(true);
	}
	
	@Override
	public Button clone() {
		Button ret = new Button();
		fillCloneWidget(ret);
		return ret;
	}
	
	@Override
	public String getDefaultName() {
		return "btn";
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
