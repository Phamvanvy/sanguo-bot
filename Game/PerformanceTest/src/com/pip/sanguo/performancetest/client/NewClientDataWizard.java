package com.pip.sanguo.performancetest.client;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;

public class NewClientDataWizard extends Wizard{
    private NewClientDataWizardPage page;

    public NewClientDataWizard(){
        page = new NewClientDataWizardPage();
        this.addPage(page);
    }

    public boolean performFinish(){
        if(page.makeInputData()){
            ClientManager.createClients(page.getNamePrefix(), page.getNameBegin(), page.getNameEnd());
            
            return true;
        }else{
            MessageDialog.openInformation(null, "数据输入有误", page.getErrorMessage());

            return false;
        }
    }
}