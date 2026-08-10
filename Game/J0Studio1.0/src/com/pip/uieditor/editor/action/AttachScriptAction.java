package com.pip.uieditor.editor.action;

import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;

import com.pip.uieditor.editor.AttachScriptDialog;
import com.pip.uieditor.editor.FrameEditor;
import com.pip.uieditor.model.Screen;

public class AttachScriptAction extends WorkbenchPartAction {
	public static final String ID = "com.pip.uieditor.editor.action.attachscript";


	public AttachScriptAction(IWorkbenchPart part) {
		super(part);
		initUI();
	}

	protected void initUI() {
		setId(ID);
		setText("Attach");
		setToolTipText("Attach");
	}

	@Override
	protected boolean calculateEnabled() {
		return true;
	}

	@Override
	public void run() {
		Screen screen  = ((FrameEditor)getWorkbenchPart()).getScreen();
		AttachScriptDialog dlg = new AttachScriptDialog(getWorkbenchPart().getSite().getShell());
		dlg.setScript(screen.getScript());
		if(dlg.open() == Window.OK) {
			screen.setScript(dlg.getScript());
		}
	}
}
