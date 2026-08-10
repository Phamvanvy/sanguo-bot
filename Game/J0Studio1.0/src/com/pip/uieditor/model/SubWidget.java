package com.pip.uieditor.model;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;


public abstract class SubWidget extends UIObject {

	private Widget parent;
	
	protected Point location;
	
	protected Dimension size;
	
	public SubWidget() {
		this.location =  new Point(0, 0);
		this.size = new Dimension(0, 0);
	}
	
	public void setParent(Widget widget) {
		this.parent = widget;
	}
	
	public Widget getParent() {
		return this.parent;
	}
	
}
