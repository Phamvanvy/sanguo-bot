package com.pip.image.workshop;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);
		
		layout.addView(DirectoryView.ID, IPageLayout.LEFT, 0.25f, editorArea);

		IFolderLayout rightPart = layout.createFolder("rightTop", IPageLayout.RIGHT, 0.7f, editorArea);
		rightPart.addView(ProjectView.ID);
		rightPart.addView(TileLibView.ID);
		
		IFolderLayout rightBottom = layout.createFolder("rightBottom", IPageLayout.BOTTOM, 0.5f, "rightTop");
		rightBottom.addView(TileView.ID);

	}

}
