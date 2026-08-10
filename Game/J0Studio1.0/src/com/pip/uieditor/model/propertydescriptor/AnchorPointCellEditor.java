package com.pip.uieditor.model.propertydescriptor;

import java.util.List;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.pip.uieditor.editor.AnchorDialog;
import com.pip.uieditor.model.AnchorPoint;
import com.pip.uieditor.util.AnchorUtil;

public class AnchorPointCellEditor extends DialogCellEditor{
	
	public AnchorPointCellEditor(Composite parent) {
		super(parent, SWT.NONE);
	}
	
	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		AnchorDialog dlg = new AnchorDialog(cellEditorWindow.getShell());
		List<AnchorPoint> data = (List<AnchorPoint>)getValue();
		dlg.setAnchorPoints(data);
		if(dlg.open() == Window.OK) {
			return dlg.getAnchorPoints();
		} else {
			return data;
		}
	}
	
    protected void updateContents(Object value) {
        if (getDefaultLabel() == null) {
			return;
		}

        String text = "";
        if (value != null) {
			text = AnchorUtil.anchorPointListToText((List<AnchorPoint>)value);
		}
        getDefaultLabel().setText(text);
    }
}
