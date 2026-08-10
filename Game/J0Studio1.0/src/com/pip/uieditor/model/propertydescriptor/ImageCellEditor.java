package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.pip.uieditor.editor.ChooseImageDialog;
import com.pip.uieditor.model.ImageData;

public class ImageCellEditor extends DialogCellEditor {
	
	public ImageCellEditor(Composite parent) {
		super(parent, SWT.NONE);
	}
	
	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		ChooseImageDialog dlg = new ChooseImageDialog(cellEditorWindow.getShell());
		ImageData data = (ImageData)getValue();
		dlg.setData(data);
		if(dlg.open() == Window.OK) {
			String macro = dlg.getMacro();
			if(macro != null) {
				return new ImageData(macro, dlg.getSelectedFrame());
			} else {
				return new ImageData(dlg.getSelectedFile().getName(), dlg.getSelectedFrame());
			}
			
		} else {
			return data;
		}
		
	}

}
