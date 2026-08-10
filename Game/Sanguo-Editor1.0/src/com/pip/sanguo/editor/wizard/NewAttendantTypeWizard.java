package com.pip.sanguo.editor.wizard;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import com.pip.sanguo.data.AttendantType;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.EditorApplication;

/**
 * 创建新随从的向导
 * @author dchen
 */
public class NewAttendantTypeWizard implements Runnable {

    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        DataListView view = (DataListView)page.findView(DataListView.ID);

        // 询问新任务的名称
        InputDialog dlg = new InputDialog(shell, "新建随从", "请输入名称：", "新随从", new IInputValidator() {
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
            AttendantType newAttendant = (AttendantType)proj.newObject(AttendantType.class);
            newAttendant.title = newname;
            
            if (view != null) {
                view.refresh(AttendantType.class);
                view.editObject(newAttendant);
            }
            
            // 保存本类型数据列表
            proj.saveDataList(AttendantType.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
