package com.pip.sanguo.editor.equipment;

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
import com.pip.sanguo.data.equipment.AttributeCalculator;
import com.pip.sanguo.data.equipment.Equipment;
import com.pip.sanguo.data.equipment.EquipmentAttribute;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.skill.DescriptionPattern;
import com.pip.sanguo.editor.util.Constants;
import com.pipimage.image.PipImage;

public class EquipmentExportToExcel {

    private ProjectData projectData;

    private List<DataObject> equipmentList;

    private HashMap<String, List<Equipment>> equipmentSheets;

    private static final String[] EQUIPMENT_TABLE_TITLE = { "装备ID", "装备名称", "需求级别", "最大耐久", "装备部位", "附加属性", "物品售价(游戏币)" ,"绑定信息", "物品图标"};
    
    private boolean needIcon = false;

    public EquipmentExportToExcel(boolean needIcon) {
        projectData = EditorApplication.getInstance().getProjectData();
        equipmentList = projectData.getDataListByType(Equipment.class);
        equipmentSheets = new HashMap<String, List<Equipment>>();
        this.needIcon = needIcon;
        
        readEquipments();
    }

    private void readEquipments() {
        for (int i = 0; i < equipmentList.size(); i++) {
            Equipment equipment = (Equipment) equipmentList.get(i);
            String categoryName = equipment.categoryName;
            
            if(categoryName.trim().length() == 0){
                categoryName = "未分类";
            }
            
            List<Equipment> sheetList = equipmentSheets.get(categoryName);

            if (sheetList == null) {
                sheetList = new ArrayList<Equipment>();
                equipmentSheets.put(categoryName, sheetList);
            }

            sheetList.add(equipment);
        }
    }

    private Object[] getEquipmentRow(Equipment equipment) {
        Object[] result = null;
        
        if(needIcon){
            result = new Object[EQUIPMENT_TABLE_TITLE.length];
        }else{
            result = new Object[EQUIPMENT_TABLE_TITLE.length - 1];
        }

        result[0] = String.valueOf(equipment.id);
        result[1] = equipment.title;
        result[2] = String.valueOf(equipment.playerLevel);
        result[3] = String.valueOf(equipment.durability);
        result[4] = Constants.PLACE_NAMES[equipment.place];

        StringBuffer sb = new StringBuffer();

        // 武器：物理攻击力，法术攻击力
        boolean isWeapon = equipment.equipmentType == Equipment.EQUI_TYPE_WEAPON;
        if (isWeapon) {
            sb.append("攻击：");
            sb.append(equipment.getAttribute(AttributeCalculator.ATTRIBUTE_MINATTACK));
            sb.append("~");
            sb.append(equipment.getAttribute(AttributeCalculator.ATTRIBUTE_MAXATTACK));
            sb.append(" ");
            int matk = equipment.getAttribute(AttributeCalculator.ATTRIBUTE_MAGICPOWER);
            if (matk > 0) {
                sb.append("法攻：" + matk + " ");
            }
        }

        // 防具：护甲
        if (!isWeapon) {
            int armor = equipment.getAttribute(AttributeCalculator.ATTRIBUTE_ARMOR);
            if (armor > 0) {
                sb.append("护甲：" + armor + " ");
            }
            armor = equipment.getAttribute(AttributeCalculator.ATTRIBUTE_MAGICARMOR);
            if (armor > 0) {
                sb.append("法防：" + armor + " ");
            }
        }

        // 其他属性
        if (equipment.showRandom) {
            sb.append("\n ");
            sb.append("随机属性 ");
        }
        else {
            sb.append("  ");
            for (int i = 0; i < AttributeCalculator.ATTRIBUTES.length; i++) {
                EquipmentAttribute attr = AttributeCalculator.ATTRIBUTES[i];
                if (isWeapon) {
                    // 武器的攻击和法攻这里不再显示
                    if (i == AttributeCalculator.ATTRIBUTE_ATTACKPOWER || i == AttributeCalculator.ATTRIBUTE_MAGICPOWER) {
                        continue;
                    }
                }
                else {
                    // 防具的护甲这里不再显示
                    if (i == AttributeCalculator.ATTRIBUTE_ARMOR || i == AttributeCalculator.ATTRIBUTE_MAGICARMOR) {
                        continue;
                    }
                }
                int value = equipment.getAttribute(i);
                if (value > 0) {
                    sb.append(attr.shortName);
                    sb.append("+");
                    sb.append(value);
                    sb.append(" ");
                }
            }
        }

        // 特殊效果
        if (equipment.buffID != -1) {
            BuffConfig bc = (BuffConfig) equipment.owner.findObject(BuffConfig.class, equipment.buffID);
            if (bc == null) {
                sb.append("特效配置无效 ");
            }
            else if (equipment.buffLevel < 1 || equipment.buffLevel > bc.maxLevel) {
                sb.append("特效配置无效 ");
            }
            else {
                DescriptionPattern pat = new DescriptionPattern(bc);
                String desc = pat.generate(equipment.buffLevel);
                sb.append("" + desc + " ");
            }
        }

        result[5] = sb.toString();
        
        result[6] = String.valueOf(equipment.price * 2);
        
        switch (equipment.bind) {
            case 0:
                result[7] = "不绑定";
                break;
            case 1:
                result[7] = "装备绑定";
                break;
            case 2:
                result[7] = "拾取绑定";
                break;
        }
        
        if(needIcon){
            PipImage pimg;
            if(equipment.iconImage == Item.ICON_IMAGE_ABILITY2){ //是否是新增的技能图片
                pimg = projectData.skillIcon2;
            }else{
                pimg = projectData.skillIcon;
            }
            Image img = pimg.getImageDraw(equipment.iconIndex).createSWTImage(Display.getCurrent().getActiveShell().getDisplay(), 0);
            ImageLoader imageLoader = new ImageLoader();
            imageLoader.data = new ImageData[] { img.getImageData() };
            imageLoader.save("c:\\tmp.png", SWT.IMAGE_PNG);
    
            try {
                BufferedImage bufferImg = null;
                ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                bufferImg = ImageIO.read(new File("c:\\tmp.png"));
                ImageIO.write(bufferImg, "png", byteArrayOut);
                result[8] = byteArrayOut.toByteArray();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return result;
    }

    public void saveEquipmentToExcel(String fileName) {
        try {
            WritableWorkbook wwb = Workbook.createWorkbook(new File(fileName));

            Iterator<String> it = equipmentSheets.keySet().iterator();
            int c = 0;

            while (it.hasNext()) {
                String sheetName = it.next();
                List<Equipment> sheetList = equipmentSheets.get(sheetName);

                WritableSheet ws = wwb.createSheet(sheetName, c++);

                if(needIcon){
                    for (int col = 0; col < EQUIPMENT_TABLE_TITLE.length; col++) {
                        Label label = new Label(col, 0, EQUIPMENT_TABLE_TITLE[col]);
                        ws.addCell(label);
                    }
                }else{
                    for (int col = 0; col < EQUIPMENT_TABLE_TITLE.length - 1; col++) {
                        Label label = new Label(col, 0, EQUIPMENT_TABLE_TITLE[col]);
                        ws.addCell(label);
                    }
                }

                for (int row = 0; row < sheetList.size(); row++) {
                    Object[] equLabel = getEquipmentRow(sheetList.get(row));

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
