package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.pip.uieditor.editor.RegionMaskDialog;

public class RegionMaskCellEditor extends DialogCellEditor {
	
	public RegionMaskCellEditor(Composite parent) {
		super(parent, SWT.NONE);
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		RegionMaskDialog dlg = new RegionMaskDialog(cellEditorWindow.getShell());
		long data = (Long)getValue();
		dlg.setFlag((int)(data & 0xFFFFFFFF));
		dlg.setMask((int)((data >> 32) & 0xFFFFFFFF));
		if(dlg.open() == Window.OK) {
			long ret = 0;
			ret |= dlg.getMask();
			ret <<= 32;
			ret |= dlg.getFlag();
			return ret;
		} else {
			return data;
		}
	}

}
