package com.pip.sanguo.editor.wizard;

import java.io.*;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.pip.mapeditor.data.MapFile;
import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.item.DropGroup;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.util.EFSUtil;
import com.pipimage.utils.Utils;

/**
 * 创建新关卡的向导。
 * @author lighthu
 */
public class NewDropGroupWizard implements Runnable {
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        DataListView view = (DataListView)page.findView(DataListView.ID);

        // 询问新关卡的名称
        InputDialog dlg = new InputDialog(shell, "新建掉落组", "请输入掉落组名称：", "新掉落组", new IInputValidator() {
            public String isValid(String newText) {
                if (newText.trim().length() == 0) {
                    return "掉落组名称不能为空。";
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
            // 创建新的DropGroup对象
            ProjectData proj = EditorApplication.getInstance().getProjectData();
            DropGroup newArea = (DropGroup)proj.newObject(DropGroup.class);
            newArea.title = newname;
            
            if (view != null) {
                view.refresh(DropGroup.class);
                view.editObject(newArea);
            }
            
            // 保存本类型数据列表
            proj.saveDataList(DropGroup.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
