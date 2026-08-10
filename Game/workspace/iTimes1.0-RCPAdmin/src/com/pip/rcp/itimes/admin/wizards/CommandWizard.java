package com.pip.rcp.itimes.admin.wizards;


import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.pip.rcp.itimes.admin.data.CommandData;
import com.pip.rcp.itimes.admin.editors.ServerWindowEditor;


public class CommandWizard extends Wizard implements INewWizard{
    public static String ID = "com.pip.rcp.itimes.admin.wizards.NewServerListWizard";
    private CommandWizardPage page;
    private IWorkbench workbench;
    private ServerWindowEditor editor;
    private CommandData selectedCommand;

    public CommandWizard(CommandData commandData, ServerWindowEditor editor){
        super();
        page = new CommandWizardPage();
        page.setCommand(commandData);
        this.selectedCommand = commandData;
        this.editor = editor;
        this.addPage(page);
    }

    public boolean performFinish(){
        if(page.makeInputData()){
            if(workbench.getActiveWorkbenchWindow().getActivePage().getActiveEditor() instanceof ServerWindowEditor){
                ServerWindowEditor editor = (ServerWindowEditor)workbench.getActiveWorkbenchWindow().getActivePage().getActiveEditor();

                boolean exec = true;

                if(selectedCommand.isNeedConfirm()){
                    if(!MessageDialog.openConfirm(null, "执行命令", "确实要在服务器 [" + editor.getTitle() + "] 上执行 [" + selectedCommand.getName() + " : " + page.getCommand() + "] 吗？")){
                        exec = false;
                    }
                }

                if(exec){
                    editor.fireCommand(page.getCommand(), false);
                }
            }

            return true;
        }else{
            MessageDialog.openInformation(null, "填写命令失败", page.getErrorMessage());

            return false;
        }
    }

    public void init(IWorkbench workbench, IStructuredSelection selection){
        this.workbench = workbench;
    }
}
