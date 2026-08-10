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
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.QuestTrigger;
import com.pip.sanguo.editor.EditorApplication;

public class AreaQuestChatExportToExcel {

    private ProjectData projectData;

    private List<DataObject> questList;

    private List<DataObject> itemList;

    public Hashtable<Integer, String> itemTable;

    private Vector<String> questSheet;

    private Vector<Vector<String[]>> questData;

    private static final String[] QUEST_TABLE_TITLE = { "NPCID", "NPC名称", "对话内容" };

    public AreaQuestChatExportToExcel() {
        projectData = EditorApplication.getInstance().getProjectData();
        questList = projectData.getDataListByType(Quest.class);
        itemList = projectData.getDataListByType(Item.class);
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
        questData = new Vector<Vector<String[]>>();

        for (int i = 0; i < questList.size(); i++) {
            Quest quest = (Quest) questList.get(i);

            if (quest.type != 1) {
                continue; // 跳过非场景任务
            }

            String _sheet = quest.title;

            if (!existSheet(_sheet)) {
                questSheet.addElement(_sheet);
                Vector<String[]> qData = new Vector<String[]>();

                QuestInfo info = getQuestInfo(quest);
                List<QuestTrigger> triggers = info.triggers;

                for (QuestTrigger trigger : triggers) {
                    String[] tmp = getTriggerChat(trigger);

                    if (tmp != null) {
                        qData.addElement(tmp);
                    }
                }

                questData.addElement(qData);
            }
        }
    }

    public void saveQuestToExcel(String fileName) {
        try {
            WritableWorkbook wwb = Workbook.createWorkbook(new File(fileName));
            for (int i = 0; i < questData.size(); i++) {
                WritableSheet ws = wwb.createSheet(questSheet.elementAt(i), i);
                for (int row = 0; row <= questData.get(i).size(); row++) {
                    if (row == 0) {
                        for (int col = 0; col < QUEST_TABLE_TITLE.length; col++) {
                            Label label = new Label(col, 0, QUEST_TABLE_TITLE[col]);
                            ws.addCell(label);
                        }
                    }
                    else {
                        String[] qbLabel = questData.get(i).elementAt(row - 1);
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

    public String[] getTriggerChat(QuestTrigger trigger) {
        if (trigger.action.startsWith("Chat(")) {
            String[] result = new String[3];

            String tmp = trigger.action;

            tmp = tmp.substring("Chat(".length());
            int idx1 = tmp.indexOf(",");
            String npcId = tmp.substring(0, idx1);
            tmp = tmp.substring(idx1 + 1);
            int idx2 = tmp.indexOf("\"");
            int idx3 = tmp.indexOf("\",");
            String msg = tmp.substring(idx2 + 1, idx3);
            
            result[0] = npcId;
            result[1] = GameMapNPC.toStringShort(EditorApplication.getProj(), Integer.parseInt(npcId));
            result[2] = msg;

            return result;
        }

        return null;
    }

    private QuestInfo getQuestInfo(Quest quest) {
        QuestInfo info = new QuestInfo(quest);

        try {
            info.load();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return info;
    }

    /**
     * 是否允许添加新标签 true -- 不存在，允许添加 false-- 存在或非法状态，不允许添加
     * */
    private boolean existSheet(String sheetName) {
        if (questSheet != null) {
            for (int i = 0; i < questSheet.size(); i++) {
                if (((String) questSheet.elementAt(i)).equals(sheetName)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public void exportQuest() {

    }
}
