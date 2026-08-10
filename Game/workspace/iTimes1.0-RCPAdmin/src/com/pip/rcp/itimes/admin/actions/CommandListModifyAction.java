package com.pip.rcp.itimes.admin.actions;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.pip.rcp.itimes.admin.data.CommandData;
import com.pip.rcp.itimes.admin.views.CommandListView;
import com.pip.rcp.itimes.admin.wizards.ModifyCommandListWizard;


public class CommandListModifyAction implements IObjectActionDelegate{
    private CommandListView commandListView;
    private CommandData selectedCommand;

    public void setActivePart(IAction action, IWorkbenchPart targetPart){
        commandListView = (CommandListView)targetPart;
    }

    public void run(IAction action){
        if(selectedCommand != null){
            ModifyCommandListWizard wizard = new ModifyCommandListWizard(selectedCommand);

            wizard.init(commandListView.getSite().getWorkbenchWindow().getWorkbench(), null);
            WizardDialog dialog = new WizardDialog(commandListView.getSite().getShell(), wizard);
            dialog.open();
        }
    }

    public void selectionChanged(IAction action, ISelection selection){
        if(!selection.isEmpty()){
            selectedCommand = ((CommandData)((IStructuredSelection)selection).getFirstElement()).clone();
        }else{
            selectedCommand = null;
        }
    }
}
