package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.pip.uieditor.editor.WidgetStateDialog;

public class WidgetStateCellEditor extends DialogCellEditor {
	
	public WidgetStateCellEditor(Composite parent) {
		super(parent, SWT.NONE);
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		WidgetStateDialog dlg = new WidgetStateDialog(cellEditorWindow.getShell());
		Integer data = (Integer)getValue();
		dlg.setState(data.intValue());
		if(dlg.open() == Window.OK) {
			return dlg.getState();
		} else {
			return data;
		}
	}

}
