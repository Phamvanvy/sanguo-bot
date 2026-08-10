package com.pip.sanguo.editor.wizard;

import java.io.*;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.Animation;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.EditorApplication;

/**
 * 创建新动画的向导。
 * @author lighthu
 */
public class NewAnimationWizard implements Runnable {
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        DataListView view = (DataListView)page.findView(DataListView.ID);
        
        // 询问新动画的名称
        InputDialog dlg = new InputDialog(shell, "新建动画", "请输入动画名称：", "新动画", new IInputValidator() {
            public String isValid(String newText) {
                if (newText.trim().length() == 0) {
                    return "动画名称不能为空。";
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
            // 创建新的Animation对象
            ProjectData proj = EditorApplication.getInstance().getProjectData();
            Animation newAni = (Animation)proj.newObject(Animation.class);
            newAni.title = newname;
            
            // 刷新动画列表并开始编辑新对象
            if (view != null) {
                view.refresh(Animation.class);
                view.editObject(newAni);
            }

            // 保存本类型数据列表
            proj.saveDataList(Animation.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
