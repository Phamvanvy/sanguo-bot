package com.pip.sanguo.editor.wizard;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.recast.Recast;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.EditorApplication;

/**
 * 创建新装备性格的向导。
 * @author jxu
 */
public class NewRecastWizard implements Runnable {
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        DataListView view = (DataListView)page.findView(DataListView.ID);

        // 询问新任务的名称
        InputDialog dlg = new InputDialog(shell, "新建重铸性格", "请输入名称：", "新性格", new IInputValidator() {
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
            // 创建新的性格对象
            ProjectData proj = EditorApplication.getInstance().getProjectData();
            Recast newRecast = (Recast)proj.newObject(Recast.class);
            newRecast.title = newname;
            
            // 刷新性格列表并开始编辑新对象
            if (view != null) {
                view.refresh(Recast.class);
                view.editObject(newRecast);
            }
            
            // 保存本类型数据列表
            proj.saveDataList(Recast.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
