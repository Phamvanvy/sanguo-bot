package com.pip.uieditor.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.gef.EditPolicy;

import com.pip.uieditor.figures.WidgetFigure;
import com.pip.uieditor.model.Container;
import com.pip.uieditor.policies.ContainerModelEditPolicy;
import com.pip.uieditor.policies.DeleteWidgetPolicy;

public class ContainerPart extends WidgetPart {
	
	public ContainerPart(Container container) {
		super(container);
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
	
	
	public Container getModel() {
		return (Container)super.getModel();
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ContainerModelEditPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new DeleteWidgetPolicy());
	}
	
	@Override
	protected IFigure createFigure() {
		Container container = (Container)getModel();
		Figure figure = new WidgetFigure(getModel());
		figure.setBounds(container.getBoundsWithBorder());
		figure.setLayoutManager(new XYLayout());
		return figure;
	}
}
