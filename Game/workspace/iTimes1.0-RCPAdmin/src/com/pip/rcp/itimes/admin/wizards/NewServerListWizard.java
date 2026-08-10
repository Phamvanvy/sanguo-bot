package com.pip.rcp.itimes.admin.wizards;


import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;

import com.pip.rcp.itimes.admin.data.ServerData;
import com.pip.rcp.itimes.admin.views.ServerListView;


public class NewServerListWizard extends Wizard implements INewWizard{
    public static String ID = "com.pip.rcp.itimes.admin.wizards.NewServerListWizard";
    private NewServerListWizardPage page;
    private IWorkbench workbench;

    public NewServerListWizard(){
        super();
        page = new NewServerListWizardPage();
        this.addPage(page);
    }

    public boolean performFinish(){
        if(page.makeInputData()){
            ServerData serverList = page.getServerList();

            IViewReference[] views = workbench.getActiveWorkbenchWindow().getActivePage().getViewReferences();

            for(int i = 0; i < views.length; i++){
                if(views[i].getId().equals(ServerListView.ID)){
                    ServerListView serverListView = (ServerListView)views[i].getView(false);

                    if(serverListView.serverExist(serverList)){
                        MessageDialog.openInformation(null, "添加服务器失败", "服务器已存在");

                        return false;
                    }else{
                        serverListView.addServer(serverList);
                    }
                }
            }

            return true;
        }else{
            MessageDialog.openInformation(null, "添加服务器失败", page.getErrorMessage());

            return false;
        }
    }

    public void init(IWorkbench workbench, IStructuredSelection selection){
        this.workbench = workbench;
    }
}
