package com.pip.sanguo.editor.wizard;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.pip.sanguo.data.DataObjectCategory;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.equipment.Equipment;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.EditorApplication;

/**
 * 创建新关卡的向导。
 * 
 * @author Joy Yan
 */
public class NewItemWizard implements Runnable {

    
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        DataListView view = (DataListView) page.findView(DataListView.ID);
        
        // 缺省分类
        Object[] obj = view.getSelectedObjects();
        DataObjectCategory type = null;
        if (obj.length > 0) {
            if (obj[0] instanceof DataObjectCategory) {
                type = (DataObjectCategory)obj[0];
            } else if (obj[0] instanceof Item) {
                Item item = (Item)obj[0];
                type = item.owner.findCategory(Item.class, item.categoryName);
            }
        }
        
        // 询问新物品的名称
        InputDialog dlg = new InputDialog(shell, "新建物品", "请输入物品名称：", "新物品", new IInputValidator() {
            public String isValid(String newText) {
                if (newText.trim().length() == 0) {
                    return "物品名称不能为空。";
                } else {
                    return null;
                }
            }
        });
        if (dlg.open() != InputDialog.OK) {
            return;
        }
        
        try {
            ProjectData proj = EditorApplication.getInstance().getProjectData();
            Item newItem = proj.newItem(type);
            newItem.title = dlg.getValue();

            // 刷新物品列表并开始编辑新物品
            if (view != null) {
                view.refresh(Item.class);
                view.editObject(newItem);
            }

            // 保存本类型数据列表
            proj.saveDataList(Item.class);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
