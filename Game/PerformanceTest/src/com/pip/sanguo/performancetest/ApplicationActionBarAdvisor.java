package com.pip.sanguo.performancetest;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import com.pip.sanguo.performancetest.client.ClientManager;
import com.pip.sanguo.performancetest.client.NewClientDataWizard;
import com.pip.sanguo.performancetest.client.SanguoClient;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor{

    // Actions - important to allocate these only in makeActions, and then use
    // them
    // in the fill methods. This ensures that the actions aren't recreated
    // when fillActionBars is called with FILL_PROXY.
    private IWorkbenchAction exitAction;
    private Action chooseFileAction;
    private Action createAction;
    private Action runAction;

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer){
        super(configurer);
    }

    protected void makeActions(final IWorkbenchWindow window){
        // Creates the actions and registers them.
        // Registering is needed to ensure that key bindings work.
        // The corresponding commands keybindings are defined in the plugin.xml
        // file.
        // Registering also provides automatic disposal of the actions when
        // the window is closed.

        exitAction = ActionFactory.QUIT.create(window);
        exitAction.setText("退出");
        register(exitAction);

        chooseFileAction = new Action("选择测试数据文件"){
            public void run(){
                try{
                    FileDialog dlg = new FileDialog(window.getShell());
                    dlg.setFilterPath( FileLocator.toFileURL(Platform.getBundle("PerformanceTest").getEntry("")).getPath());
                    dlg.setText("选择文件");
                    String newFile = dlg.open();
                    if(newFile != null){
                        ClientManager.datafle = newFile;
                        ClientManager.loadClients();
                        ClientManager.view.loadClients();
    
                        window.getShell().setText("三国压力测试" + "(" + newFile.toString() + ")");
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };

        createAction = new Action("初始化测试数据"){
            public void run(){
                WizardDialog dialog = new WizardDialog(window.getShell(), new NewClientDataWizard());
                dialog.open();
                
                if(dialog.getReturnCode() == WizardDialog.OK){
                    ClientManager.saveClients();
                    ClientManager.view.loadClients();
                }
            }
        };
        
        runAction = new Action("启动测试"){
            public void run(){
                if(runAction.getText().equals("启动测试")){
                	InputDialog dlg = new InputDialog(window.getShell(), "地址", "请输入服务器地址：", "socket://s01.sg.5ding.com:8080#7F0000016D61", null);
                	if (dlg.open() == InputDialog.OK) {
                		SanguoClient.SERVER_URL = dlg.getValue();
                    	ClientManager.start();
                        runAction.setText("停止测试");
                	}
                }else{
                    ClientManager.stop();
                    runAction.setText("启动测试");
                }
            }
        };
    }

    protected void fillMenuBar(IMenuManager menuBar){
        MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
        menuBar.add(fileMenu);
        fileMenu.add(chooseFileAction);
        fileMenu.add(createAction);
        fileMenu.add(runAction);
        fileMenu.add(exitAction);
    }
}
