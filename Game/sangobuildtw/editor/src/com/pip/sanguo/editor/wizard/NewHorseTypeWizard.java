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
import com.pip.sanguo.data.HorseType;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Title;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.EditorApplication;

/**
 * 创建新坐骑类型的向导。
 * @author lighthu
 */
public class NewHorseTypeWizard implements Runnable {
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        DataListView view = (DataListView)page.findView(DataListView.ID);
        
        // 询问新名称
        InputDialog dlg = new InputDialog(shell, "新建坐骑类型", "请输入名称：", "新坐骑", new IInputValidator() {
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
            // 创建新的HorseType对象
            ProjectData proj = EditorApplication.getInstance().getProjectData();
            HorseType newTitle = (HorseType)proj.newObject(HorseType.class);
            newTitle.title = newname;
            newTitle.showName = newname;
            
            // 刷新列表并开始编辑新对象
            if (view != null) {
                view.refresh(HorseType.class);
                view.editObject(newTitle);
            }

            // 保存本类型数据列表
            proj.saveDataList(Title.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
