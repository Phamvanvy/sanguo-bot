package com.pip.uieditor.model;

import org.eclipse.draw2d.geometry.Dimension;



public class TabButton extends Widget {
	public static final TabButton PROTOTYPE = new TabButton();
	
	public TabButton() {
		super("TabButton");
	}
	
	@Override
	public TabButton clone() {
		TabButton ret = new TabButton();
		fillCloneWidget(ret);
		return ret;
	}
	
	@Override
	public String getDefaultName() {
		return "tbtn";
	}
	
	@Override
	public void setSize(Dimension size) {
		super.setSize(size);
		if(getParent() != null) {
			getParent().layoutWidgets();
		}
	}
}
