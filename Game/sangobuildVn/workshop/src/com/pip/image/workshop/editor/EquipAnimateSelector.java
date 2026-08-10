package com.pip.image.workshop.editor;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;

public class EquipAnimateSelector extends TileLibSelector {
	private EquipEditor editor;
	
	public EquipAnimateSelector(Composite parent, int style, EquipEditor editor) {
		super(parent, style);
		this.editor = editor;
	}
	
	protected void prepareMenu() {
		MenuManager mgr = new MenuManager();
        
        mgr.add(new Action("±à¼­´Ë¶¯»­") {
            public void run() {
            	onEditAnimate();
            }
        });
        setMenu(mgr.createContextMenu(this));
	}
	
	protected void onEditAnimate() {
		int[] sels = getSelectedFrames();
		if (sels.length == 0) {
			return;
		}
		editor.editAnimate(sels[0]);
	}
}
