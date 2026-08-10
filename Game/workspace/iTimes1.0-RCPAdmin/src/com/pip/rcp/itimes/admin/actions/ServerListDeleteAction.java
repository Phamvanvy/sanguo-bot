package com.pip.rcp.itimes.admin.actions;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.pip.rcp.itimes.admin.data.ServerData;
import com.pip.rcp.itimes.admin.views.ServerListView;


public class ServerListDeleteAction implements IObjectActionDelegate{
    private ServerListView serverListView;
    private ServerData selectedServer;

    public void setActivePart(IAction action, IWorkbenchPart targetPart){
        serverListView = (ServerListView)targetPart;
    }

    public void run(IAction action){
        if(selectedServer != null){
            if(MessageDialog.openConfirm(null, "删除服务器", "确实要删除服务器：" + selectedServer.toString() + "吗？")){
                serverListView.removeServer(selectedServer);
                selectedServer = null;
            }
        }
    }

    public void selectionChanged(IAction action, ISelection selection){
        if(!selection.isEmpty()){
            selectedServer = ((ServerData)((IStructuredSelection)selection).getFirstElement()).clone();
        }else{
            selectedServer = null;
        }
    }
}
