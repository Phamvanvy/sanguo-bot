package com.pip.sanguo.data;

import java.io.File;
import java.util.*;

import org.jdom.Document;
import org.jdom.Element;

import com.pip.sanguo.editor.util.Constants;
import com.pip.util.Utils;

public class NPCTemplateConfig {
    private HashMap<Integer, Element> levelConfigs = new HashMap<Integer, Element>();
    private HashMap<Integer, Element> dropConfigs = new HashMap<Integer, Element>();
    private HashMap<Integer, Element> powerfulDropConfigs = new HashMap<Integer, Element>();

    public NPCTemplateConfig(File configFile) {
        if (!configFile.exists()) {
            return;
        }
        try {
            loadConfig(Utils.loadDOM(configFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /*
     * 从配置文件中载入NPC缺省设置。
     */
    private void loadConfig(Document doc) throws Exception {
        Iterator itor = doc.getRootElement().getChildren("levelconfig").iterator();
        while (itor.hasNext()) {
            Element elem = (Element)itor.next();
            int clazz = Integer.parseInt(elem.getAttributeValue("class"));
            int level = Integer.parseInt(elem.getAttributeValue("level"));
            levelConfigs.put(clazz * 1000 + level, elem);
        }
        
        itor = doc.getRootElement().getChildren("standarddrop").iterator();
        while (itor.hasNext()) {
            Element elem = (Element)itor.next();
            int level = Integer.parseInt(elem.getAttributeValue("level"));
            boolean powerful = "1".equals(elem.getAttributeValue("level"));
            if (powerful) {
                powerfulDropConfigs.put(level, elem);
            } else {
                dropConfigs.put(level, elem);
            }
        }
    }
 
    /**
     * 取得某个级别怪物的标准掉落配置。
     * @param level
     * @param powerful
     * @return 数组中依次是掉落组ID和万分掉率。如果没有配置，返回null。
     */
    public int[] getDropConfig(int level, boolean powerful) {
        Element elem;
        if (powerful) {
            elem = powerfulDropConfigs.get(level);
        } else {
            elem = dropConfigs.get(level);
        }
        if (elem != null) {
            String[] arr = elem.getTextTrim().split(",");
            int[] ret = new int[arr.length];
            for (int i = 0; i < arr.length; i++) {
                ret[i] = Integer.parseInt(arr[i]);
            }
            return ret;
        } else {
            return null;
        }
    }
    
    private int getStandardValue(int clazz, int level, String name) {
        try {
            Element elem = levelConfigs.get(clazz * 1000 + level);
            if (elem == null) {
                return 0;
            }
            return Integer.parseInt(elem.getAttributeValue(name));
        } catch (Exception e) {
            return 0;
        }
    }

    /*
     * 下面是根据级别和职业计算标准属性的方法。
     */
    
    public int getStandardHP(int clazz, int level) {
        return getStandardValue(clazz, level, "hp");
    }
    
    public int getStandardMP(int clazz, int level) {
        return getStandardValue(clazz, level, "mp");
    }
    
    public int getStandardArmor(int clazz, int level) {
        return getStandardValue(clazz, level, "armor");
    }
    
    public int getStandardMagicArmor(int clazz, int level) {
        return getStandardValue(clazz, level, "magicarmor");
    }

    public int getStandardSTA(int clazz, int level) {
        return getStandardValue(clazz, level, "sta");
    }
    
    public int getStandardSTR(int clazz, int level) {
        return getStandardValue(clazz, level, "str");
    }
    
    public int getStandardAGI(int clazz, int level) {
        return getStandardValue(clazz, level, "agi");
    }
    
    public int getStandardINT(int clazz, int level) {
        return getStandardValue(clazz, level, "int");
    }
    
    public int getStandardWeaponAP1(int clazz, int level) {
        return getStandardValue(clazz, level, "ap1");
    }
    
    public int getStandardWeaponAP2(int clazz, int level) {
        return getStandardValue(clazz, level, "ap2");
    }
    
    public int getStandardWeaponMagicAP(int clazz, int level) {
        return getStandardValue(clazz, level, "magicap");
    }
    
    public int getStandardExp(int clazz, int level) {
        return getStandardValue(clazz, level, "exp");
    }
    
    public int getStandardMoney(int clazz, int level) {
        return getStandardValue(clazz, level, "money");
    }
}
