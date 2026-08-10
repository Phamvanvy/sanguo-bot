package com.pip.uieditor.parts;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import com.pip.uieditor.model.UIObject;

public abstract class UIObjectPart extends AbstractGraphicalEditPart implements PropertyChangeListener{
	
	public UIObjectPart(UIObject uiObject) {
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
	public EditPart getTargetEditPart(Request request) {
		EditPolicyIterator i = getEditPolicyIterator();
		EditPart editPart;
		while (i.hasNext()) {
			editPart = i.next().getTargetEditPart(request);
			if (editPart != null)
				return editPart;
		}

		if (RequestConstants.REQ_SELECTION == request.getType()) {
			if (isSelectable())
				return this;
		}

		return null;
	}

	public void setSelected(int value) {
		if (isSelectable()) { // 修改AbstractEditPart的一个Bug。当EditPart处于不能选中的状态下，如果设置选中，那么将会抛出一个运行时异常
			if(getSelected() == value)
				return;
			else {
				setSelectedValue(value);
				fireSelectionChanged();
			}
		}
	}
	
	protected void setSelectedValue(int value) { //这段代码太丑了，不过这是我能想到的最简单的解决问题的办法
		try {
			Field field = AbstractEditPart.class.getDeclaredField("selected");
			field.setAccessible(true);
			field.setInt(this, value);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}
