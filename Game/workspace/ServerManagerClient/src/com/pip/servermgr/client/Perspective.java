package com.pip.servermgr.client;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import com.pip.servermgr.client.ErrorView;
import com.pip.servermgr.client.ProductFileView;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);
		
		layout.addView(DirectoryView.ID, IPageLayout.LEFT, 0.3f, editorArea);
		layout.addView(ProductFileView.ID, IPageLayout.BOTTOM, 0.6f, editorArea);
		layout.addView(ErrorView.ID, IPageLayout.RIGHT, 0.5f, ProductFileView.ID);
		layout.getViewLayout(DirectoryView.ID).setCloseable(false);
		layout.getViewLayout(ProductFileView.ID).setCloseable(false);
		layout.getViewLayout(ErrorView.ID).setCloseable(false);
	}
}
