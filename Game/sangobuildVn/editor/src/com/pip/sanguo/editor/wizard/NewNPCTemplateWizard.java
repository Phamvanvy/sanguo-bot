package com.pip.sanguo.editor.wizard;

import java.io.*;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.Animation;
import com.pip.sanguo.data.NPCType;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.EditorApplication;

/**
 * 创建新NPC模板的向导。
 * @author lighthu
 */
public class NewNPCTemplateWizard implements Runnable {
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        DataListView view = (DataListView)page.findView(DataListView.ID);

        // 询问新关卡的名称
        InputDialog dlg = new InputDialog(shell, "新建NPC模板", "请输入NPC模板名称：", "新NPC模板", new IInputValidator() {
            public String isValid(String newText) {
                if (newText.trim().length() == 0) {
                    return "NPC模板名称不能为空。";
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
            // 创建新的NPCDef对象
            ProjectData proj = EditorApplication.getInstance().getProjectData();
            NPCTemplate newNPC = (NPCTemplate)proj.newObject(NPCTemplate.class);
            newNPC.title = newname;
            newNPC.type = (NPCType)proj.getDictDataListByType(NPCType.class).get(0);
            
            // 刷新NPC模板列表并开始编辑新对象
            if (view != null) {
                view.refresh(NPCTemplate.class);
                view.editObject(newNPC);
            }

            // 保存本类型数据列表
            proj.saveDataList(NPCTemplate.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
