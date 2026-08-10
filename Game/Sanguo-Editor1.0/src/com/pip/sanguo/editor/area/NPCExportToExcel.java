package com.pip.sanguo.editor.area;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.GameAreaInfo;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.quest.QuestBean;
import com.pip.sanguo.editor.EditorApplication;

public class NPCExportToExcel {
    private ProjectData projectData;
    private GameArea[] gameAreas;

    public NPCExportToExcel() {
        projectData = EditorApplication.getInstance().getProjectData();
        List<DataObject> objs = projectData.getDataListByType(GameArea.class);
        gameAreas = new GameArea[objs.size()];
        objs.toArray(gameAreas);
    }

    public void saveToExcel(String fileName) {
        try {
            WritableWorkbook wwb = Workbook.createWorkbook(new File(fileName));
            WritableSheet ws = wwb.createSheet("NPC", 0);
            ws.addCell(new Label(0, 0, "Scene"));
            ws.addCell(new Label(1, 0, "ID"));
            ws.addCell(new Label(2, 0, "Name"));
            int row = 1;
            for (int i = 0; i < gameAreas.length; i++) {
                GameAreaInfo gai;
                try {
                    gai = new GameAreaInfo(gameAreas[i]);
                    gai.load();
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                for (int j = 0; j < gai.maps.size(); j++) {
                    GameMapInfo gmi = gai.maps.get(j);
                    for (int k = 0; k < gmi.objects.size(); k++) {
                        GameMapObject gmo = gmi.objects.get(k);
                        if (gmo instanceof GameMapNPC) {
                            ws.addCell(new Label(0, row, gmi.name));
                            ws.addCell(new Label(1, row, String.valueOf(gmo.getGlobalID())));
                            ws.addCell(new Label(2, row, ((GameMapNPC) gmo).name));
                            row++;
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
}
