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
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.util.EFSUtil;
import com.pipimage.utils.Utils;

/**
 * 创建新任务的向导。
 * @author lighthu
 */
public class NewQuestWizard implements Runnable {
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        DataListView view = (DataListView)page.findView(DataListView.ID);

        // 询问新任务的名称
        InputDialog dlg = new InputDialog(shell, "新建任务", "请输入任务名称：", "新任务", new IInputValidator() {
            public String isValid(String newText) {
                if (newText.trim().length() == 0) {
                    return "任务名称不能为空。";
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
            // 创建新的Quest对象
            ProjectData proj = EditorApplication.getInstance().getProjectData();
            Quest newQuest = (Quest)proj.newObject(Quest.class);
            newQuest.title = newname;
            int aid = 1;
            String fname = newQuest.id + ".xml";
            while (new File(proj.baseDir, "Quests/" + fname).exists()) {
                fname = newQuest.id + "_" + aid + ".xml";
                aid++;
            }
            File questFile = new File(proj.baseDir, "Quests/" + fname);
            newQuest.source = questFile;
            
            // 刷新任务列表并开始编辑新任务
            if (view != null) {
                view.refresh(Quest.class);
                view.editObject(newQuest);
            }
            
            // 保存本类型数据列表
            proj.saveDataList(Quest.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
