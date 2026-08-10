package com.pip.rcp.itimes.admin;


import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.pip.rcp.itimes.admin.views.CommandListView;
import com.pip.rcp.itimes.admin.views.ServerListView;


public class Perspective implements IPerspectiveFactory{
    public void createInitialLayout(IPageLayout layout){
        String editorArea = layout.getEditorArea();
        layout.setFixed(true);

        layout.addView(ServerListView.ID, IPageLayout.LEFT, 0.5f, editorArea);
        layout.addView(CommandListView.ID, IPageLayout.BOTTOM, 0.5f, ServerListView.ID);
    }
}
