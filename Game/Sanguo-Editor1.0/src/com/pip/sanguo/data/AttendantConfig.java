package com.pip.sanguo.data;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import org.jdom.Document;
import org.jdom.Element;
import com.pip.util.Utils;

/**
 * 随从基础数据配置信息
 * @author dchen
 */
public class AttendantConfig {
    
    private HashMap<Integer, Element> qulityConfigs = new HashMap<Integer, Element>();

    public AttendantConfig(File configFile) {
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
     * 从配置文件中载入随从缺省设置。
     */
    private void loadConfig(Document doc) throws Exception {
        Iterator itor = doc.getRootElement().getChildren("qualityconfig").iterator();
        while (itor.hasNext()) {
            Element elem = (Element)itor.next();
            int quality = Integer.parseInt(elem.getAttributeValue("quality"));
            qulityConfigs.put(quality, elem);
        }
    }
    
    private int getStandardValue(int quality, String name) {
        try {
            Element elem = qulityConfigs.get(quality);
            if (elem == null) {
                return 0;
            }
            return Integer.parseInt(elem.getAttributeValue(name));
        } catch (Exception e) {
            return 0;
        }
    }
    
    public int getStandardHP(int qulity) {
        return getStandardValue(qulity, "hp");
    }
    
    public int getStandardMP(int qulity) {
        return getStandardValue(qulity, "mp");
    }
    
    public int getStandardArmor(int qulity) {
        return getStandardValue(qulity, "armor");
    }
    
    public int getStandardMagicArmor(int qulity) {
        return getStandardValue(qulity, "magicarmor");
    }

    public int getStandardSTA(int qulity) {
        return getStandardValue(qulity, "sta");
    }
    
    public int getStandardSTR(int qulity) {
        return getStandardValue(qulity, "str");
    }
    
    public int getStandardAGI(int qulity) {
        return getStandardValue(qulity, "agi");
    }
    
    public int getStandardINT(int qulity) {
        return getStandardValue(qulity, "int");
    }
    
    public int getStandardWeaponAP1(int qulity) {
        return getStandardValue(qulity, "ap1");
    }
    
    public int getStandardWeaponAP2(int qulity) {
        return getStandardValue(qulity, "ap2");
    }
    
    public int getStandardWeaponMagicAP(int qulity) {
        return getStandardValue(qulity, "magicap");
    }
    
    public int getStandardCritical(int qulity){
        return getStandardValue(qulity, "critical");
    }
    
    public int getStandardSpellcritical(int qulity){
        return getStandardValue(qulity, "spellcritical");
    }
    
    public int getStandardDecritical(int qulity){
        return getStandardValue(qulity, "decritical");
    }
}
