package com.pip.uieditor.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.TreeEditPart;
import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import com.pip.uieditor.model.UIObject;

public abstract class UIObjectTreePart extends AbstractTreeEditPart implements PropertyChangeListener{
	
	public UIObjectTreePart(UIObject uiObject) {
		setModel(uiObject);
	}
	
	@Override
	public UIObject getModel() {
		return (UIObject)super.getModel();
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
	protected void addChildVisual(EditPart childEditPart, int index) {
		Widget widget = getWidget();
		TreeItem item;
		if (widget instanceof Tree)
			item = new TreeItem((Tree) widget, 0, index);
		else
			item = new TreeItem((TreeItem) widget, 0, index);
		item.setChecked(true);
		((TreeEditPart) childEditPart).setWidget(item);
	}
	
	
}
