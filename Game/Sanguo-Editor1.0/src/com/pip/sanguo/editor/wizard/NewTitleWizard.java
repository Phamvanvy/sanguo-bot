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
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Title;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.EditorApplication;

/**
 * 创建新称号的向导。
 * @author lighthu
 */
public class NewTitleWizard implements Runnable {
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        DataListView view = (DataListView)page.findView(DataListView.ID);
        
        // 询问新动画的名称
        InputDialog dlg = new InputDialog(shell, "新建称号", "请输入称号名称：", "新称号", new IInputValidator() {
            public String isValid(String newText) {
                if (newText.trim().length() == 0) {
                    return "称号名称不能为空。";
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
            // 创建新的Title对象
            ProjectData proj = EditorApplication.getInstance().getProjectData();
            Title newTitle = (Title)proj.newObject(Title.class);
            newTitle.title = newname;
            newTitle.faction = (Faction)EditorApplication.getProj().findDictObject(Faction.class, 5);
            
            // 刷新列表并开始编辑新对象
            if (view != null) {
                view.refresh(Title.class);
                view.editObject(newTitle);
            }

            // 保存本类型数据列表
            proj.saveDataList(Title.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
