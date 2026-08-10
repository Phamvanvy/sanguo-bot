package com.pip.sanguo.editor.wizard;

import java.io.File;
import java.util.List;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.item.DropGroup;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.editor.EditorApplication;

public class AutoNewObjectWizard {
    public static void newItem(Shell shell) {
        ProjectData proj = EditorApplication.getInstance().getProjectData();
        int maxItemId = 0;

        List<DataObject> items = proj.getDataListByType(Item.class);

        for (int i = 0; i < items.size(); i++) {
            Item item = (Item) items.get(i);

            if (item.id > maxItemId) {
                maxItemId = item.id;
            }
        }

        String newArea = (maxItemId + 1) + "-" + (maxItemId + 101);

        InputDialog dlg = new InputDialog(shell, "自动生成物品", "请输入物品ID区间：", newArea, new IInputValidator() {
            public String isValid(String newText) {
                if (newText.trim().length() == 0) {
                    return "输入非法";
                }
                else {
                    return null;
                }
            }
        });

        if (dlg.open() != InputDialog.OK) {
            return;
        }

        String[] area = dlg.getValue().split("-");
        int min = Integer.parseInt(area[0].trim());
        int max = Integer.parseInt(area[1].trim());

        for (int i = min; i <= max; i++) {
            Item newItem = new Item(proj);
            newItem.id = i;
            newItem.title = "新物品" + i;
            newItem.categoryName = "";
            proj.addObjectToList(Item.class, newItem);
        }

        try {
            proj.saveDataList(Item.class);
            MessageDialog.openInformation(shell, "成功", "操作成功！");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void newQuest(Shell shell) {
        ProjectData proj = EditorApplication.getInstance().getProjectData();
        int maxQuestId = 0;

        List<DataObject> quests = proj.getDataListByType(Quest.class);

        for (int i = 0; i < quests.size(); i++) {
            Quest quest = (Quest) quests.get(i);

            if (quest.id > maxQuestId) {
                maxQuestId = quest.id;
            }
        }

        String newArea = (maxQuestId + 1) + "-" + (maxQuestId + 101);

        InputDialog dlg = new InputDialog(shell, "自动生成空任务", "请输入任务ID区间：", newArea, new IInputValidator() {
            public String isValid(String newText) {
                if (newText.trim().length() == 0) {
                    return "输入非法";
                }
                else {
                    return null;
                }
            }
        });

        if (dlg.open() != InputDialog.OK) {
            return;
        }

        String[] area = dlg.getValue().split("-");
        int min = Integer.parseInt(area[0].trim());
        int max = Integer.parseInt(area[1].trim());

        try {
            for (int i = min; i <= max; i++) {
                Quest newQuest = new Quest(proj);
                newQuest.id = i;
                newQuest.title = "新任务" + i;
                newQuest.categoryName = "";
                File questFile = new File(proj.baseDir, "Quests/" + (i + ".xml"));
                newQuest.source = questFile;
                QuestInfo info = new QuestInfo(newQuest);
                info.save();
                proj.addObjectToList(Quest.class, newQuest);
            }

            proj.saveDataList(Quest.class);
            MessageDialog.openInformation(shell, "成功", "操作成功！");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void newDropGroup(Shell shell) {
        ProjectData proj = EditorApplication.getInstance().getProjectData();
        int maxGroupId = 0;

        List<DataObject> dropgroups = proj.getDataListByType(DropGroup.class);

        for (int i = 0; i < dropgroups.size(); i++) {
            DropGroup dropGroup = (DropGroup) dropgroups.get(i);

            if (dropGroup.id > maxGroupId) {
                maxGroupId = dropGroup.id;
            }
        }

        String newArea = (maxGroupId + 1) + "-" + (maxGroupId + 101);

        InputDialog dlg = new InputDialog(shell, "自动生成掉落组", "请输入掉落组ID区间：", newArea, new IInputValidator() {
            public String isValid(String newText) {
                if (newText.trim().length() == 0) {
                    return "输入非法";
                }
                else {
                    return null;
                }
            }
        });

        if (dlg.open() != InputDialog.OK) {
            return;
        }

        String[] area = dlg.getValue().split("-");
        int min = Integer.parseInt(area[0].trim());
        int max = Integer.parseInt(area[1].trim());

        for (int i = min; i <= max; i++) {
            DropGroup newDropGroup = new DropGroup(proj);
            newDropGroup.id = i;
            newDropGroup.title = "新掉落组" + i;
            newDropGroup.categoryName = "";
            proj.addObjectToList(DropGroup.class, newDropGroup);
        }

        try {
            proj.saveDataList(DropGroup.class);
            MessageDialog.openInformation(shell, "成功", "操作成功！");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
