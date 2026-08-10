package com.pip.uieditor.editor.action;

import java.util.List;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import com.pip.uieditor.editor.clipboard.Clipboard;
import com.pip.uieditor.parts.WidgetPart;

public class CopyAction extends SelectionAction {
	
	public CopyAction(IWorkbenchPart part) {
		super(part);
		setId(ActionFactory.COPY.getId());
		setText("Copy");
		setToolTipText("Copy");
	}

	@Override
	protected boolean calculateEnabled() {
		List l = getSelectedObjects();
		if(l.size() == 1 && getSelectedObjects().get(0) instanceof WidgetPart) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void run() {
		WidgetPart widgetPart = (WidgetPart)getSelectedObjects().get(0);
		Clipboard.instance().setObject(widgetPart.getModel().clone());
	}
	
	

}
