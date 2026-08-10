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
import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.TeleportSet;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.util.EFSUtil;
import com.pipimage.utils.Utils;

/**
 * 创建新传送门组的向导。
 * @author lighthu
 */
public class NewTeleportSetWizard implements Runnable {
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        DataListView view = (DataListView)page.findView(DataListView.ID);

        // 询问新任务的名称
        InputDialog dlg = new InputDialog(shell, "新建驿站", "请输入驿站名称：", "新驿站", new IInputValidator() {
            public String isValid(String newText) {
                if (newText.trim().length() == 0) {
                    return "驿站名称不能为空。";
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
            // 创建新的TeleportSet对象
            ProjectData proj = EditorApplication.getInstance().getProjectData();
            TeleportSet newShop = (TeleportSet)proj.newObject(TeleportSet.class);
            newShop.title = newname;
            
            // 刷新商店列表并开始编辑新对象
            if (view != null) {
                view.refresh(TeleportSet.class);
                view.editObject(newShop);
            }
            
            // 保存本类型数据列表
            proj.saveDataList(TeleportSet.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
