package com.pip.sanguo.editor.quest;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.quest.QuestBean;
import com.pip.sanguo.editor.EditorApplication;

public class QuestExportToExcel {

    private ProjectData projectData;

    private List<DataObject> questList;

    private List<DataObject> itemList;

    public Hashtable<Integer, String> itemTable;

    public Hashtable<Integer, String> beforeQuest;

    private Vector<String> questSheet;

    private Vector<QuestBean>[] questData;

    private static final String[] QUEST_TABLE_TITLE = { "任务ID", "任务名称", "任务级别", "任务类型", "起始NPC", "领取提示", "任务描述",
            "完成提示", "未完提示", "任务目标", "任务奖励", "前置任务" };

    public QuestExportToExcel() {
        projectData = EditorApplication.getInstance().getProjectData();
        questList = projectData.getDataListByType(Quest.class);
        // System.out.println("列表项总数："+questList.size());
        itemList = projectData.getDataListByType(Item.class);
        // System.out.println("物品总数："+itemList.size());
        readQuest();
        readItem();
    }

    private void readItem() {
        if (itemList.size() == 0) {
            return;
        }
        itemTable = new Hashtable<Integer, String>();
        for (int i = 0; i < itemList.size(); i++) {
            Item item = (Item) itemList.get(i);
            itemTable.put(item.id, item.title);
        }
    }

    private void readQuest() {
        if (questList.size() == 0) {
            return;
        }
        questSheet = new Vector<String>();
        beforeQuest = new Hashtable<Integer, String>();
        for (int i = 0; i < questList.size(); i++) {
            Quest quest = (Quest) questList.get(i);
            String _sheet = quest.categoryName;
            if (_sheet.equals(""))
                _sheet = "未分类";
            if (existSheet(_sheet)) {
                questSheet.addElement(_sheet);
            }

            beforeQuest.put(quest.getID(), quest.getTitle());
        }
        // System.out.println("总标签数：" + questSheet.size());
        questData = new Vector[questSheet.size()];
        for (int i = 0; i < questData.length; i++) {
            questData[i] = new Vector<QuestBean>();
            for (int j = 0; j < questList.size(); j++) {
                Quest quest = (Quest) questList.get(j);
                String _sheet = quest.categoryName;
                if (_sheet.equals(""))
                    _sheet = "未分类";
                if (_sheet.equals(questSheet.elementAt(i))) {
                    questData[i].addElement(getQuestBean(quest));
                }
            }
            // System.out.println("标签" + i +"questBean数量：" +
            // questData[i].size());
        }
    }

    private String[] getQuestBeanLabel(QuestBean qb) {
        return new String[] { "" + qb.getQuestID(), qb.getQuestTitle(), "" + qb.getQuestLevel(), qb.getQuestType(),
                qb.getQuestStartNpc(), qb.getQuestPreDescription(), qb.getQuestDescription(),
                qb.getQuestFinishDescription(), qb.getQuestUnfinishDescription(), qb.getQuestTargets(this),
                qb.getQuestRewardsItem(), qb.getQuestCondition(this) };
    }

    public void saveQuestToExcel(String fileName) {
        try {
            WritableWorkbook wwb = Workbook.createWorkbook(new File(fileName));
            for (int i = 0; i < questData.length; i++) {
                WritableSheet ws = wwb.createSheet(questSheet.elementAt(i), i);
                for (int row = 0; row <= questData[i].size(); row++) {
                    if (row == 0) {
                        for (int col = 0; col < QUEST_TABLE_TITLE.length; col++) {
                            Label label = new Label(col, 0, QUEST_TABLE_TITLE[col]);
                            ws.addCell(label);
                        }
                    }
                    else {
                        QuestBean qb = questData[i].elementAt(row - 1);
                        String[] qbLabel = getQuestBeanLabel(qb);
                        for (int col = 0; col < qbLabel.length; col++) {
                            Label label = new Label(col, row, qbLabel[col]);
                            ws.addCell(label);
                        }
                    }
                }
            }
            wwb.write();
            wwb.close();
        }
        catch (RowsExceededException e) {
            e.printStackTrace();
        }
        catch (WriteException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private QuestBean getQuestBean(Quest quest) {
        QuestBean qb = new QuestBean();
        qb.setQuestID(quest.id);
        qb.setQuestTitle(quest.title);
        qb.setQuestLevel(quest.level);
        qb.setQuestType(quest.type == 0 ? "普通" : "场景");
        // System.out.println("任务ID："+ qb.getQuestID() + "| 任务标题：" +
        // qb.getQuestTitle() + "| 任务类型：" + qb.getQuestType());
        qb.setQuestStartNpc(quest.getStartNpcName());
        // System.out.println("起始NPC：" + qb.getQuestStartNpc());
        qb.setQuestPreDescription(quest.preDescription);
        qb.setQuestDescription(quest.description);
        qb.setQuestFinishDescription(quest.postDescription);
        qb.setQuestUnfinishDescription(quest.unfinishDescription);
        // System.out.println("任务描述：" + qb.getQuestPreDescription());
        qb.setQuestTargets(quest.getTargetsCondition());
        // System.out.println("任务目标：" + qb.getQuestTargets());
        qb.setQuestRewardsItem(quest.getRewardsItem());
        // System.out.println("任务奖励：" + qb.getQuestRewardsItem());
        if (quest.condition.matches(".*TaskFinished\\(\\d+\\).*")) {
            qb.setQuestCondition(quest.condition);
        }
        else {
            qb.setQuestCondition("无");
        }
        return qb;
    }

    /**
     * 是否允许添加新标签 true -- 不存在，允许添加 false-- 存在或非法状态，不允许添加
     * */
    private boolean existSheet(String sheetName) {
        if (questSheet != null) {
            for (int i = 0; i < questSheet.size(); i++) {
                if (((String) questSheet.elementAt(i)).equals(sheetName)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void exportQuest() {

    }
}
