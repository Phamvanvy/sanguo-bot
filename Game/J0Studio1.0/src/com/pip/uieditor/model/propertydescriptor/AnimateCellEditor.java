package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.pip.uieditor.editor.ChooseAnimateDialog;
import com.pip.uieditor.model.AnimateData;
import com.pip.uieditor.model.ImageData;

public class AnimateCellEditor extends DialogCellEditor {
	
	public AnimateCellEditor(Composite parent) {
		super(parent, SWT.NONE);
	}
	
	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		ChooseAnimateDialog dlg = new ChooseAnimateDialog(cellEditorWindow.getShell());
		AnimateData data = (AnimateData)getValue();
		dlg.setData(data);
		if(dlg.open() == Window.OK) {
			String macro = dlg.getMacro();
			if(macro != null) {
				return new ImageData(macro, dlg.getSelectedIndex());
			} else {
				return new AnimateData(dlg.getSelectedFile().getName(), dlg.getSelectedIndex());
			}
		} else {
			return data;
		}
		
	}

}
