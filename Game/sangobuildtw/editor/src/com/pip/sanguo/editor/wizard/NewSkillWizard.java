package com.pip.sanguo.editor.wizard;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.EditorApplication;

public class NewSkillWizard implements Runnable{
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        DataListView view = (DataListView) page.findView(DataListView.ID);

        // 询问新技能的名称和类型
        NewSkillDialog dlg = new NewSkillDialog(shell);
        if (dlg.open() != Dialog.OK) {
            return;
        }
        
        try {
            // 创建新的Buff对象
            ProjectData proj = EditorApplication.getInstance().getProjectData();
            SkillConfig newSkill = (SkillConfig)proj.newObject(SkillConfig.class);
            newSkill.title = dlg.getName();
            newSkill.setType(dlg.getType());
            
            // 刷新BUFF列表并开始编辑新对象
            if (view != null) {
                view.refresh(SkillConfig.class);
                view.editObject(newSkill);
            }
            
            // 保存本类型数据列表
            proj.saveDataList(SkillConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
