package com.pip.sanguo.editor.shop;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import jxl.Workbook;
import jxl.format.CellFormat;
import jxl.write.Blank;
import jxl.write.Label;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.Shop.ShopItem;
import com.pip.sanguo.editor.EditorApplication;
import com.pipimage.image.PipImage;

public class IshopExportToExcel {

    private ProjectData projectData;

    private List<DataObject> shopList;

    private HashMap<String, List<ShopItem>> shopSheets;

    private static final String[] ITEM_TABLE_TITLE = { "物品ID", "物品名称", "物品描述", "物品售价(人民币：分)", "物品图标" };

    public IshopExportToExcel() {
        projectData = EditorApplication.getInstance().getProjectData();
        shopList = projectData.getDataListByType(Shop.class);
        shopSheets = new HashMap<String, List<ShopItem>>();

        readItems();
    }

    private void readItems() {
        for (int i = 0; i < shopList.size(); i++) {
            Shop shop = (Shop) shopList.get(i);
            String categoryName = shop.categoryName;

            if (!categoryName.trim().equals("卖场")) {
                continue;
            }

            String shopTitle = shop.title;
            List<ShopItem> sheetList = shopSheets.get(shopTitle);

            if (sheetList == null) {
                sheetList = new ArrayList<ShopItem>();
                shopSheets.put(shopTitle, sheetList);
            }

            for (int j = 0; j < shop.items.size(); j++) {
                ShopItem item = shop.items.get(j);
                sheetList.add(item);
            }
        }
    }

    private Object[] getItemRow(ShopItem item) {
        Object[] result = new Object[ITEM_TABLE_TITLE.length];

        result[0] = String.valueOf(item.item.id);
        result[1] = item.item.title;
        result[2] = item.item.description;
        result[3] = String.valueOf(item.requirements.get(0).amount);

        PipImage pimg = projectData.skillIcon;
        Image img = pimg.getImageDraw(item.item.iconIndex).createSWTImage(Display.getCurrent().getActiveShell().getDisplay(), 0);
        ImageLoader imageLoader = new ImageLoader();
        imageLoader.data = new ImageData[] { img.getImageData() };
        imageLoader.save("c:\\tmp.png", SWT.IMAGE_PNG);

        try {
            BufferedImage bufferImg = null;
            ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
            bufferImg = ImageIO.read(new File("c:\\tmp.png"));
            ImageIO.write(bufferImg, "png", byteArrayOut);
            result[4] = byteArrayOut.toByteArray();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void saveIshopToExcel(String fileName) {
        try {
            WritableWorkbook wwb = Workbook.createWorkbook(new File(fileName));

            Iterator<String> it = shopSheets.keySet().iterator();
            int c = 0;

            while (it.hasNext()) {
                String sheetName = it.next();
                List<ShopItem> sheetList = shopSheets.get(sheetName);

                WritableSheet ws = wwb.createSheet(sheetName, c++);

                for (int col = 0; col < ITEM_TABLE_TITLE.length; col++) {
                    Label label = new Label(col, 0, ITEM_TABLE_TITLE[col]);
                    ws.addCell(label);
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
