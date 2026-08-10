package com.pip.rcp.itimes.admin.wizards;


import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;

import com.pip.rcp.itimes.admin.data.ServerData;
import com.pip.rcp.itimes.admin.views.ServerListView;


public class ModifyServerListWizard extends Wizard implements INewWizard{
    public static String ID = "com.pip.rcp.itimes.admin.wizards.NewServerListWizard";
    private ModifyServerListWizardPage page;
    private IWorkbench workbench;
    private ServerData selectedServer;

    public ModifyServerListWizard(ServerData selectedServer){
        super();
        this.selectedServer = selectedServer.clone();

        page = new ModifyServerListWizardPage();
        page.setServerData(selectedServer);
        this.addPage(page);
    }

    public boolean performFinish(){
        if(page.makeInputData()){
            ServerData serverData = page.getServerData();

            IViewReference[] views = workbench.getActiveWorkbenchWindow().getActivePage().getViewReferences();

            for(int i = 0; i < views.length; i++){
                if(views[i].getId().equals(ServerListView.ID)){
                    ServerListView serverListView = (ServerListView)views[i].getView(false);

                    if(serverListView.serverExist(serverData)){
                        MessageDialog.openInformation(null, "更新服务器失败", "服务器已存在");

                        return false;
                    }else{
                        serverListView.modifyServer(selectedServer, serverData);
                    }
                }
            }

            return true;
        }else{
            MessageDialog.openInformation(null, "更新服务器失败", page.getErrorMessage());

            return false;
        }
    }

    public void init(IWorkbench workbench, IStructuredSelection selection){
        this.workbench = workbench;
    }
}
