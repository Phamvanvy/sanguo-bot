/**
 * 
 */
package com.pip.sanguo.editor.wizard;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.pip.sanguo.data.Card;
import com.pip.sanguo.data.DataObjectCategory;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.EditorApplication;

/**
 * @author zlguo
 *
 */
public class NewCardWizard implements Runnable {

    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        DataListView view = (DataListView)page.findView(DataListView.ID);
        
        // 缺省分类
        Object[] obj = view.getSelectedObjects();
        DataObjectCategory type = null;
        //String cataName = "";
        if (obj.length > 0) {
            if (obj[0] instanceof DataObjectCategory) {
                type = (DataObjectCategory)obj[0];
            } else if (obj[0] instanceof Card) {
                Card item = (Card)obj[0];
                //cataName = item.categoryName;
                type = item.owner.findCategory(Card.class, item.categoryName);
            }
        }
        
        // 询问新任务的名称
        InputDialog dlg = new InputDialog(shell, "新建卡片", "请输入名称：", "", new IInputValidator() {
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
            // 创建新的Card对象
            ProjectData proj = EditorApplication.getInstance().getProjectData();
            //Card newCard = (Card)proj.newObject(Card.class);
            Card newCard = new Card(proj);
            newCard.id = 0;
            while (proj.findObject(Card.class, newCard.id) != null) {
                newCard.id++;
            }
            
            newCard.title = newname;
            newCard.canUse = false;
            newCard.description = "";
            newCard.dropObjects = new Card.DropObject[0];
            newCard.materials = new Card.Material[0];
            newCard.itemId = -1;
            newCard.quality = Card.QUALITY_COMMON;
            newCard.rate = 100;
            newCard.star = 1;
            if(type != null){
                newCard.categoryName = type.name;
            } else {
                newCard.categoryName = "";
            }
            newCard.holeId = 0;
            if(type != null){
                int defaultId = 0;
                Hashtable<Integer,String> t = new Hashtable<Integer,String>();
                //设置holeId.
                boolean hasSameHole = false;
                for (int i = 0; i < type.objects.size(); i++) {
                    Card c = (Card)type.objects.get(i);
                    if(c.title.equals(newname)){
                        newCard.holeId = c.holeId;
                        hasSameHole = true;
                        t.put(new Integer(c.holeId), "");
                    }
                }
                if(hasSameHole == false){
                    while(t.containsKey(defaultId)){
                        defaultId++;
                    }
                    newCard.holeId = defaultId;
                }
                //设置suiteId
                List<DataObjectCategory> catas = proj.getCategoryListByType(Card.class);
                int index = catas.lastIndexOf(type);
                if(index != -1){
                    newCard.suiteId = index - 1;
                } else {
                    newCard.suiteId = -1;
                }
            }
            newCard.type = Card.TYPE_HERO;
            proj.addObjectToList(Card.class, newCard);
            // 保存本类型数据列表
            proj.saveDataList(Card.class);
            
            
            // 刷新商店列表并开始编辑新对象
            if (view != null) {
                view.refresh(Card.class);
                view.editObject(newCard);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
}
