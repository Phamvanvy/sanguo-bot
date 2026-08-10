package com.pip.uieditor.parts;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

import com.pip.uieditor.model.TableColumn;

public class TableColumnPart extends UIObjectPart {

	public TableColumnPart(TableColumn tableColumn) {
		super(tableColumn);
	}
	

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("location")) {
			getFigure().setLocation((Point)evt.getNewValue());
			getFigure().repaint();
		} else if(evt.getPropertyName().equals("size")) {
			getFigure().setSize((Dimension)evt.getNewValue());
			getFigure().repaint();
		} else if(evt.getPropertyName().equals("preferredWidth")) {
			getParent().refresh();
		} else if(evt.getPropertyName().equals("flexible")) {
			getParent().refresh();
		}
	}

	@Override
	public TableColumn getModel() {
		return (TableColumn)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		RectangleFigure figure = new RectangleFigure() {
			protected boolean useLocalCoordinates() {
				return true;
			}
		};
		figure.setBounds(getModel().getBounds());
		figure.setBackgroundColor(ColorConstants.lightGray);
		figure.setAlpha(100);
		return figure;
	}

	@Override
	protected void createEditPolicies() {

	}
	
}
