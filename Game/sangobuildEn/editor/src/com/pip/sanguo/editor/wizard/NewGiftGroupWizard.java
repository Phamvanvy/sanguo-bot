package com.pip.sanguo.editor.wizard;

import java.io.*;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.pip.sanguo.data.Faction;
import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.Animation;
import com.pip.sanguo.data.GiftGroup;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Title;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.EditorApplication;

/**
 * 创建新奖励组的向导。
 * @author lighthu
 */
public class NewGiftGroupWizard implements Runnable {
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        DataListView view = (DataListView)page.findView(DataListView.ID);
        
        // 询问新奖励组的名称
        InputDialog dlg = new InputDialog(shell, "新建奖励组", "请输入奖励组名称：", "新奖励组", new IInputValidator() {
            public String isValid(String newText) {
                if (newText.trim().length() == 0) {
                    return "奖励组名称不能为空。";
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
            // 创建新的GiftGroup对象
            ProjectData proj = EditorApplication.getInstance().getProjectData();
            GiftGroup newGroup = (GiftGroup)proj.newObject(GiftGroup.class);
            newGroup.title = newname;
            
            // 刷新列表并开始编辑新对象
            if (view != null) {
                view.refresh(GiftGroup.class);
                view.editObject(newGroup);
            }

            // 保存本类型数据列表
            proj.saveDataList(GiftGroup.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
