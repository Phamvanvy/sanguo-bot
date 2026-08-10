package com.pip.sanguo.editor.item;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.item.DropGroup;
import com.pip.sanguo.data.item.DropItem;
import com.pip.sanguo.data.item.DropNode;
import com.pip.sanguo.data.item.SubDropGroup;
import com.pip.sanguo.editor.EditorApplication;

public class DropGroupExportToExcel {

    private ProjectData projectData;

    private List<DataObject> dropgroupList;

    private HashMap<String, List<DropGroup>> dropgroupSheets;

    private static final String[] DROPGROUP_TABLE_TITLE = { "掉落组ID", "掉落组名称", "掉落情况" };

    public DropGroupExportToExcel() {
        projectData = EditorApplication.getInstance().getProjectData();
        dropgroupList = projectData.getDataListByType(DropGroup.class);
        dropgroupSheets = new HashMap<String, List<DropGroup>>();

        readDropGroups();
    }

    private void readDropGroups() {
        for (int i = 0; i < dropgroupList.size(); i++) {
            DropGroup dropgroup = (DropGroup) dropgroupList.get(i);
            String categoryName = dropgroup.categoryName;

            if (categoryName.trim().length() == 0) {
                categoryName = "未分类";
            }

            List<DropGroup> sheetList = dropgroupSheets.get(categoryName);

            if (sheetList == null) {
                sheetList = new ArrayList<DropGroup>();
                dropgroupSheets.put(categoryName, sheetList);
            }

            sheetList.add(dropgroup);
        }
    }

    private Object[] getDropGroupRow(DropGroup dropgroup) {
        Object[] result = new Object[DROPGROUP_TABLE_TITLE.length];

        result[0] = String.valueOf(dropgroup.id);
        result[1] = dropgroup.title;
        result[2] = new ArrayList<String>();

        List<SubDropGroup> list = dropgroup.subGroup;
        
        for(SubDropGroup subDropGroup : list){
            ((List<String>)result[2]).add(subDropGroup.toString());
            
            List<DropItem> list1 = subDropGroup.dropGroup;
            
            for(DropItem dropItem : list1){
                ((List<String>)result[2]).add("    " + dropItem.toString() + " " + (Math.round(dropItem.dropRate * 10000) / 100f) + "%");
            }
        }
        
        return result;
    }

    public void saveDropGroupToExcel(String fileName) {
        try {
            WritableWorkbook wwb = Workbook.createWorkbook(new File(fileName));

            Iterator<String> it = dropgroupSheets.keySet().iterator();
            int c = 0;

            while (it.hasNext()) {
                String sheetName = it.next();
                List<DropGroup> sheetList = dropgroupSheets.get(sheetName);

                WritableSheet ws = wwb.createSheet(sheetName, c++);

                for (int col = 0; col < DROPGROUP_TABLE_TITLE.length; col++) {
                    Label label = new Label(col, 0, DROPGROUP_TABLE_TITLE[col]);
                    ws.addCell(label);
                }

                int allRow = 1;
                
                for (int row = 0; row < sheetList.size(); row++) {
                    Object[] equLabel = getDropGroupRow(sheetList.get(row));
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
