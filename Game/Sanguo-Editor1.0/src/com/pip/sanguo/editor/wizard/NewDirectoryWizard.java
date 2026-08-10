package com.pip.sanguo.editor.wizard;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import com.pip.sanguo.data.DirectoryType;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.EditorApplication;

/**
 * 活动指引引导模板
 * @author dchen
 */
public class NewDirectoryWizard implements Runnable {

    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        DataListView view = (DataListView)page.findView(DataListView.ID);
        
        InputDialog dlg = new InputDialog(shell, "新建引导", "请输入名称：", "活动名称", new IInputValidator() {
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
            ProjectData proj = EditorApplication.getInstance().getProjectData();
            DirectoryType newDirectory = (DirectoryType)proj.newObject(DirectoryType.class);
            newDirectory.title = newname;
            
            if (view != null) {
                view.refresh(DirectoryType.class);
                view.editObject(newDirectory);
            }
            
            // 保存本类型数据列表
            proj.saveDataList(DirectoryType.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
