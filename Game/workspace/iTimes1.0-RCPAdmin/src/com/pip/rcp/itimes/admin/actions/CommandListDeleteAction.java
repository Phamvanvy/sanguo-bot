package com.pip.rcp.itimes.admin.actions;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.pip.rcp.itimes.admin.data.CommandData;
import com.pip.rcp.itimes.admin.views.CommandListView;


public class CommandListDeleteAction implements IObjectActionDelegate{
    private CommandListView commandListView;
    private CommandData selectedCommand;

    public void setActivePart(IAction action, IWorkbenchPart targetPart){
        commandListView = (CommandListView)targetPart;
    }

    public void run(IAction action){
        if(selectedCommand != null){
            if(MessageDialog.openConfirm(null, "É¾³ýÃüÁî", "È·ÊµÒªÉ¾³ýÃüÁî£º" + selectedCommand.toString() + "Âð£¿")){
                commandListView.removeCommand(selectedCommand);
                selectedCommand = null;
            }
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
