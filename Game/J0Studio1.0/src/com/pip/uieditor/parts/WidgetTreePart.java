package com.pip.uieditor.parts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import com.pip.uieditor.model.Widget;

public class WidgetTreePart extends UIObjectTreePart {
	
	public WidgetTreePart(Widget widget) {
		super(widget);
	}
	
	public Widget getModel() {
		return (Widget)super.getModel();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("child")) {
			refreshChildren();
		}
	}

	@Override
	protected String getText() {
		return getModel().getType();
	}
	
	@Override
	public List getModelChildren() {
		List subWidgets = getModel().getSubWidgets();
		List regions = getModel().getRegions();
		List widgets = getModel().getChildren();
		List ret = new ArrayList(subWidgets.size() + regions.size() + widgets.size());
		for(Object o : regions) {
			ret.add(o);
		}
		for(Object o : subWidgets) {
			ret.add(o);
		}
		for(Object o : widgets) {
			ret.add(o);
		}
		return ret;
	}

}
