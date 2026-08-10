package com.pip.sanguo.editor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.item.DropGroup;
import com.pip.sanguo.data.item.DropNode;

public class NpcDropExportToExcel {

    private ProjectData projectData;

    private List<DataObject> npcTemplateList;

    private HashMap<String, List<NPCTemplate>> npcTemplateSheets;

    private static final String[] NPCDROP_TABLE_TITLE = { "NPC ID", "NPC名称", "掉落情况" };

    public NpcDropExportToExcel() {
        projectData = EditorApplication.getInstance().getProjectData();
        npcTemplateList = projectData.getDataListByType(NPCTemplate.class);
        npcTemplateSheets = new HashMap<String, List<NPCTemplate>>();

        readNpcTemplates();
    }

    private void readNpcTemplates() {
        for (int i = 0; i < npcTemplateList.size(); i++) {
            NPCTemplate npcTemplate = (NPCTemplate) npcTemplateList.get(i);
            String categoryName = npcTemplate.categoryName;

            if (categoryName.trim().length() == 0) {
                categoryName = "未分类";
            }

            List<NPCTemplate> sheetList = npcTemplateSheets.get(categoryName);

            if (sheetList == null) {
                sheetList = new ArrayList<NPCTemplate>();
                npcTemplateSheets.put(categoryName, sheetList);
            }

            sheetList.add(npcTemplate);
        }
    }

    private Object[] getNpcTemplateRow(NPCTemplate npcTemplate) {
        Object[] result = new Object[NPCDROP_TABLE_TITLE.length];

        result[0] = String.valueOf(npcTemplate.id);
        result[1] = npcTemplate.title;
        result[2] = new ArrayList<String>();
        
        List<DropNode> list = npcTemplate.dropGroups;
        
        for(DropNode dropNode : list){
            StringBuffer sb = new StringBuffer();
            
            switch(dropNode.type){
                case DropNode.TYPE_ITEM:
                    sb.append("物品");
                    sb.append(' ');
                    sb.append(EditorApplication.getProj().findItem(dropNode.id).toString());
                    sb.append(' ');
                    sb.append(String.valueOf(dropNode.getRateString()));
                    sb.append(' ');
                    sb.append(dropNode.quantityMin + "-" + dropNode.quantityMax);
                    sb.append(' ');
                    sb.append(dropNode.isTask ? "是" : "否");
                    break;
                case DropNode.TYPE_EQUIPMENT:
                    sb.append("装备");
                    sb.append(' ');
                    sb.append(EditorApplication.getProj().findEquipment(dropNode.id).toString());
                    sb.append(' ');
                    sb.append(String.valueOf(dropNode.getRateString()));
                    sb.append(' ');
                    sb.append(dropNode.quantityMin + "-" + dropNode.quantityMax);
                    sb.append(' ');
                    sb.append(dropNode.isTask ? "是" : "否");
                    break;
                case DropNode.TYPE_DROPGROUP:
                    sb.append("掉落组");
                    sb.append(' ');
                    sb.append(EditorApplication.getProj().findObject(DropGroup.class, dropNode.id).toString());
                    sb.append(' ');
                    sb.append(String.valueOf(dropNode.getRateString()));
                    sb.append(' ');
                    sb.append(dropNode.quantityMin + "-" + dropNode.quantityMax);
                    sb.append(' ');
                    sb.append(dropNode.isTask ? "是" : "否");
                    break;
            }
            
            ((List<String>)result[2]).add(sb.toString());
        }
        
        return result;
    }

    public void saveNpcTemplateToExcel(String fileName) {
        try {
            WritableWorkbook wwb = Workbook.createWorkbook(new File(fileName));

            Iterator<String> it = npcTemplateSheets.keySet().iterator();
            int c = 0;

            while (it.hasNext()) {
                String sheetName = it.next();
                List<NPCTemplate> sheetList = npcTemplateSheets.get(sheetName);

                WritableSheet ws = wwb.createSheet(sheetName, c++);

                for (int col = 0; col < NPCDROP_TABLE_TITLE.length; col++) {
                    Label label = new Label(col, 0, NPCDROP_TABLE_TITLE[col]);
                    ws.addCell(label);
                }

                int allRow = 1;
                
                for (int row = 0; row < sheetList.size(); row++) {
                    Object[] equLabel = getNpcTemplateRow(sheetList.get(row));
                    List<String> list = (List<String>)equLabel[2];
                    
                    for (int col = 0; col < equLabel.length; col++) {
                        if(col == equLabel.length - 1){
                            for(String s : list){
                                Label label = new Label(col, allRow++, s);
                                ws.addCell(label);
                            }
                        }else{
                            Label label = new Label(col, allRow, (String) equLabel[col]);
                            ws.addCell(label);
                        }
                    }
                    
                    allRow++;
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
}
