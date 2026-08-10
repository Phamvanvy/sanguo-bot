package com.pip.rcp.itimes.admin.actions;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.pip.rcp.itimes.admin.data.ServerData;
import com.pip.rcp.itimes.admin.views.ServerListView;
import com.pip.rcp.itimes.admin.wizards.ModifyServerListWizard;


public class ServerListModifyAction implements IObjectActionDelegate{
    private ServerListView serverListView;
    private ServerData selectedServer;

    public void setActivePart(IAction action, IWorkbenchPart targetPart){
        serverListView = (ServerListView)targetPart;
    }

    public void run(IAction action){
        if(selectedServer != null){
            ModifyServerListWizard wizard = new ModifyServerListWizard(selectedServer);

            wizard.init(serverListView.getSite().getWorkbenchWindow().getWorkbench(), null);
            WizardDialog dialog = new WizardDialog(serverListView.getSite().getShell(), wizard);
            dialog.open();
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
