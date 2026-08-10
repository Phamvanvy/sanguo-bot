/**
 * 
 */
package com.pip.sanguo.editor.wizard;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.pip.sanguo.data.DataObjectCategory;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.clientEvent.ClientEvent;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.EditorApplication;

/**
 * @author zlguo
 *
 */
public class NewClientEventWizard implements Runnable {

    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        DataListView view = (DataListView)page.findView(DataListView.ID);
        
        // 缺省分类
        Object[] obj = view.getSelectedObjects();
        DataObjectCategory type = null;
        //String cataName = "";
        if (obj.length > 0) {
            if (obj[0] instanceof DataObjectCategory) {
                type = (DataObjectCategory)obj[0];
            } else if (obj[0] instanceof ClientEvent) {
                ClientEvent item = (ClientEvent)obj[0];
                //cataName = item.categoryName;
                type = item.owner.findCategory(ClientEvent.class, item.categoryName);
            }
        }
        
        // 询问新任务的名称
        InputDialog dlg = new InputDialog(shell, "新建事件", "请输入名称：", "", new IInputValidator() {
            public String isValid(String newText) {
                if (newText.trim().length() == 0) {
                    return "名称不能为空。";
                } else {
                    return null;
                }
            }
        });
        if (dlg.open() != InputDialog.OK) {
            return;
        }
        
        String newname = dlg.getValue();
        try {
            // 创建新的ClientEvent对象
            ProjectData proj = EditorApplication.getInstance().getProjectData();
            ClientEvent newEvent = new ClientEvent(proj);
            newEvent.id = 0;
            while (proj.findObject(ClientEvent.class, newEvent.id) != null) {
                newEvent.id++;
            }
            
            newEvent.title = newname;
            newEvent.description = "";
            if(type != null){
                newEvent.categoryName = type.name;
            } else {
                newEvent.categoryName = "";
            }
            
            proj.addObjectToList(ClientEvent.class, newEvent);
            // 保存本类型数据列表
            proj.saveDataList(ClientEvent.class);
            
            
            // 刷新商店列表并开始编辑新对象
            if (view != null) {
                view.refresh(ClientEvent.class);
                view.editObject(newEvent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
}
