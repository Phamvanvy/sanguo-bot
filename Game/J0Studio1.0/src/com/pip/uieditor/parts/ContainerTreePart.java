package com.pip.uieditor.parts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import com.pip.uieditor.model.Container;

public class ContainerTreePart extends WidgetTreePart {

	public ContainerTreePart(Container model) {
		super(model);
	}
	
	public Container getModel() {
		return (Container)super.getModel();
	}
	
	@Override
	public List getModelChildren() {
		Container con = getModel();
		List ret = new ArrayList(con.getChildCount() + con.getRegionCount());
		for(int i = 0; i < con.getRegionCount(); i++) {
			ret.add(con.getRegion(i));
		}
		for(int i = 0; i < con.getChildCount(); i++) {
			ret.add(con.getChild(i));
		}
		return ret;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals(Container.PROPERTY_CHILD)) {
			if(evt.getOldValue() == null) { //新加了child
				refreshChildren();
			}
			if(evt.getNewValue() == null) { //删除了child
				refreshChildren();
			}
			return;
		}
	}
	
	@Override
	protected String getText() {
		return "Container"; 
	}
}
