package com.pip.rcp.itimes.admin.actions;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import com.pip.rcp.itimes.admin.wizards.NewServerListWizard;


public class ServerListAddAction implements IViewActionDelegate{
    IViewPart view;

    public void init(IViewPart view){
        this.view = view;
    }

    public void run(IAction action){
        NewServerListWizard wizard = new NewServerListWizard();

        wizard.init(view.getViewSite().getWorkbenchWindow().getWorkbench(), null);
        WizardDialog dialog = new WizardDialog(view.getViewSite().getShell(), wizard);
        dialog.open();
    }

    public void selectionChanged(IAction action, ISelection selection){
    }
}
