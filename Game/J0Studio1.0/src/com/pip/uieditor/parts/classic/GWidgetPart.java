package com.pip.uieditor.parts.classic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import com.pip.uieditor.model.classic.GWidget;

public class GWidgetPart extends AbstractGraphicalEditPart implements PropertyChangeListener{

	public GWidgetPart(GWidget widget) {
		setModel(widget);
	}
	
	@Override
	public GWidget getModel() {
		return (GWidget)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return null;
	}

	@Override
	protected void createEditPolicies() {

	}

	@Override
	public void activate() {
		super.activate();
		getModel().addPropertyChangeListener(this);
	}

	@Override
	public void deactivate() {
		getModel().removePropertyChangeListener(this);
		super.deactivate();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("location")) {
			getFigure().setLocation((Point)evt.getNewValue());
		} else if(evt.getPropertyName().equals("size")) {
			getFigure().setSize((Dimension)evt.getNewValue());
		}
	}
	
	

}
