package com.pip.j0ide;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import com.pip.gtl.remotedebugger.ui.DebugSessionView;
import com.pip.gtl.remotedebugger.ui.MemoryView;
import com.pip.gtl.remotedebugger.ui.VariableView;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);
		
		layout.addView(DirectoryView.ID, IPageLayout.LEFT, 0.25f, editorArea);
		layout.addView(ConsoleView.ID, IPageLayout.BOTTOM, 0.8f, editorArea);
		layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, 0.75f, editorArea);
		layout.addView(MemoryView.ID, IPageLayout.LEFT, 0.5f, IPageLayout.ID_OUTLINE);
		layout.addView(DebugSessionView.ID, IPageLayout.BOTTOM, 0.5f, editorArea);
		layout.addView(VariableView.ID, IPageLayout.BOTTOM, 0.5f, DebugSessionView.ID);
	
	}
}
