package com.pip.sanguo.editor.wizard;

import java.io.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.pip.mapeditor.data.MapFile;
import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.util.EFSUtil;
import com.pipimage.utils.Utils;

/**
 * 创建新BUFF的向导。
 * @author lighthu
 */
public class NewBuffWizard implements Runnable {
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        DataListView view = (DataListView)page.findView(DataListView.ID);

        // 询问新BUFF的名称和类型
        NewBuffDialog dlg = new NewBuffDialog(shell);
        if (dlg.open() != Dialog.OK) {
            return;
        }
        
        try {
            // 创建新的Buff对象
            ProjectData proj = EditorApplication.getInstance().getProjectData();
            BuffConfig newBuff = (BuffConfig)proj.newObject(BuffConfig.class);
            newBuff.title = dlg.getName();
            newBuff.setBuffType(dlg.getType());
            
            // 刷新BUFF列表并开始编辑新对象
            if (view != null) {
                view.refresh(BuffConfig.class);
                view.editObject(newBuff);
            }
            
            // 保存本类型数据列表
            proj.saveDataList(BuffConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
