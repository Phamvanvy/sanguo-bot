package com.pip.sanguo.editor.item;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.editor.EditorApplication;
import com.pipimage.image.PipImage;

public class ItemExportToExcel {

    private ProjectData projectData;

    private List<DataObject> itemList;

    private HashMap<String, List<Item>> itemSheets;

    private static final String[] ITEM_TABLE_TITLE = { "物品ID", "物品名称", "物品描述", "物品售价(游戏币)" ,"绑定信息", "物品图标"};

    private boolean needIcon = false;
    
    public ItemExportToExcel(boolean needIcon) {
        projectData = EditorApplication.getInstance().getProjectData();
        itemList = projectData.getDataListByType(Item.class);
        itemSheets = new HashMap<String, List<Item>>();
        this.needIcon = needIcon;
        
        readItems();
    }

    private void readItems() {
        for (int i = 0; i < itemList.size(); i++) {
            Item item = (Item) itemList.get(i);
            String categoryName = item.categoryName;
            
            if(categoryName.trim().length() == 0){
                categoryName = "未分类";
            }
            
            List<Item> sheetList = itemSheets.get(categoryName);

            if (sheetList == null) {
                sheetList = new ArrayList<Item>();
                itemSheets.put(categoryName, sheetList);
            }

            sheetList.add(item);
        }
    }

    private Object[] getItemRow(Item item) {
        Object[] result = null;
        
        if(needIcon){
            result = new Object[ITEM_TABLE_TITLE.length];
        }else{
            result = new Object[ITEM_TABLE_TITLE.length - 1];
        }

        result[0] = String.valueOf(item.id);
        result[1] = item.title;
        result[2] = item.description;
        result[3] = String.valueOf(item.price * 2);
        switch (item.bind) {
            case 0:
                result[4] = "不绑定";
                break;
            case 1:
                result[4] = "装备绑定";
                break;
            case 2:
                result[4] = "拾取绑定";
                break;
        }
        
        if(needIcon){
            PipImage pimg = projectData.skillIcon;
            Image img = pimg.getImageDraw(item.iconIndex).createSWTImage(Display.getCurrent().getActiveShell().getDisplay(), 0);
            ImageLoader imageLoader = new ImageLoader();
            imageLoader.data = new ImageData[] { img.getImageData() };
            imageLoader.save("c:\\tmp.png", SWT.IMAGE_PNG);
    
            try {
                BufferedImage bufferImg = null;
                ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                bufferImg = ImageIO.read(new File("c:\\tmp.png"));
                ImageIO.write(bufferImg, "png", byteArrayOut);
                result[5] = byteArrayOut.toByteArray();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return result;
    }

    public void saveItemToExcel(String fileName) {
        try {
            WritableWorkbook wwb = Workbook.createWorkbook(new File(fileName));

            Iterator<String> it = itemSheets.keySet().iterator();
            int c = 0;

            while (it.hasNext()) {
                String sheetName = it.next();
                List<Item> sheetList = itemSheets.get(sheetName);

                WritableSheet ws = wwb.createSheet(sheetName, c++);

                if(needIcon){
                    for (int col = 0; col < ITEM_TABLE_TITLE.length; col++) {
                        Label label = new Label(col, 0, ITEM_TABLE_TITLE[col]);
                        ws.addCell(label);
                    }
                }else{
                    for (int col = 0; col < ITEM_TABLE_TITLE.length - 1; col++) {
                        Label label = new Label(col, 0, ITEM_TABLE_TITLE[col]);
                        ws.addCell(label);
                    }
                }

                for (int row = 0; row < sheetList.size(); row++) {
                    Object[] equLabel = getItemRow(sheetList.get(row));

                    for (int col = 0; col < equLabel.length; col++) {
                        if (equLabel[col] instanceof String) {
                            Label label = new Label(col, row + 1, (String) equLabel[col]);
                            ws.addCell(label);
                        }
                        else {
                            WritableImage image = new WritableImage((double) col, (double) (row + 1), (double) 0.6,
                                    (double) 2, (byte[]) equLabel[col]);
                            ws.addImage(image);
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
