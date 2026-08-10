package com.pip.rcp.itimes.admin;


import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.pip.rcp.itimes.admin.views.CommandListView;
import com.pip.rcp.itimes.admin.views.ServerListView;


public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor{
    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer){
        super(configurer);
    }

    public void preWindowOpen(){
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(800, 600));
        configurer.setShowCoolBar(false);
        configurer.setShowStatusLine(true);
        configurer.setTitle("幻想i时代服务器管理");
    }

    public void postWindowOpen(){
        IViewReference[] views = getWindowConfigurer().getWindow().getActivePage().getViewReferences();

        for(int i = 0; i < views.length; i++){
            if(views[i].getId().equals(ServerListView.ID)){
                ServerListView serverListView = (ServerListView)views[i].getView(false);
                serverListView.loadServerData();
            }else if(views[i].getId().equals(CommandListView.ID)){
                CommandListView commandListView = (CommandListView)views[i].getView(false);
                commandListView.loadCommandData();
            }
        }
    }
}
