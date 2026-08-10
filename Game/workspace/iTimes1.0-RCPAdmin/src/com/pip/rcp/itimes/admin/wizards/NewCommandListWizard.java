package com.pip.rcp.itimes.admin.wizards;


import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;

import com.pip.rcp.itimes.admin.data.CommandData;
import com.pip.rcp.itimes.admin.views.CommandListView;


public class NewCommandListWizard extends Wizard implements INewWizard{
    public static String ID = "com.pip.rcp.itimes.admin.wizards.NewServerListWizard";
    private NewCommandListWizardPage page;
    private IWorkbench workbench;

    public NewCommandListWizard(){
        super();
        page = new NewCommandListWizardPage();
        this.addPage(page);
    }

    public boolean performFinish(){
        if(page.makeInputData()){
            CommandData commandData = page.getCommandList();

            IViewReference[] views = workbench.getActiveWorkbenchWindow().getActivePage().getViewReferences();

            for(int i = 0; i < views.length; i++){
                if(views[i].getId().equals(CommandListView.ID)){
                    CommandListView commandListView = (CommandListView)views[i].getView(false);

                    if(commandListView.commandExist(commandData)){
                        MessageDialog.openInformation(null, "Ìí¼ÓÃüÁîÊ§°Ü", "ÃüÁîÒÑ´æÔÚ");

                        return false;
                    }else{
                        commandListView.addCommand(commandData);
                    }
                }
            }

            return true;
        }else{
            MessageDialog.openInformation(null, "Ìí¼ÓÃüÁîÊ§°Ü", page.getErrorMessage());

            return false;
        }
    }

    public void init(IWorkbench workbench, IStructuredSelection selection){
        this.workbench = workbench;
    }
}
