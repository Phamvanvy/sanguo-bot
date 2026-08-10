package com.pip.sanguo.performancetest;

import org.eclipse.ui.IViewReference;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.pip.sanguo.performancetest.client.ClientManager;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor{

    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer){
        super(configurer);
    }

    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer){
        return new ApplicationActionBarAdvisor(configurer);
    }

    public void preWindowOpen(){
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setShowCoolBar(false);
        configurer.setShowStatusLine(false);
        configurer.setTitle("三国压力测试(请选择一个测试数据文件)");
    }

    public void postWindowOpen(){
        IViewReference[] views = getWindowConfigurer().getWindow().getActivePage().getViewReferences();

        for(int i = 0; i < views.length; i++){
            if(views[i].getId().equals(View.ID)){
                View view = (View) views[i].getView(false);
                ClientManager.view = view;
            }
        }
    }
}
