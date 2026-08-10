package com.pip.sanguo.data.equipment;

import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.jdom.Element;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.editor.skill.DescriptionPattern;
import com.pip.util.Utils;

/**
 * 装备前缀（模板）
 */
public class EquipmentPrefix extends DataObject {
    public ProjectData owner;
    // 最低装备级别（含）
    public int minLevel;
    // 最高装备级别（含）
    public int maxLevel;
    // 最低装备品质（含）
    public int minQuality;
    // 最高装备品质（含）
    public int maxQuality;
    
    // 所有装备属性的权重，顺序和AttributeCalculator.ATTRIBUTES一样
    public float[] attributePrior = new float[AttributeCalculator.ATTRIBUTES.length];
    
    /**
     * 生成一个空的装备前缀（不能存储）。
     */
    public EquipmentPrefix(ProjectData owner) {
        this.owner = owner;
        id = -1;
        title = "特殊配置";
        description = "";
        minLevel = 0;
        maxLevel = 200;
        minQuality = Item.QUALITY_GREEN;
        maxQuality = Item.QUALITY_ORANGE;
    }
    
    /**
     * 根据指定的装备价值计算出所有的附加属性。
     * @param value 价值
     * @return 附加属性表，顺序和AttributeCalculator.ATTRIBUTES一样
     */
    public float[] generateAttributes(float value) {
        float[] ret= new float[AttributeCalculator.ATTRIBUTES.length];
        float totalPrior = 0.0f;
        for (int i = 0; i < attributePrior.length; i++) {
            totalPrior += attributePrior[i];
        }
        
        if (totalPrior == 0.0f) {
            return ret;
        }
        for (int i = 0; i < attributePrior.length; i++) {
            float thisValue = value * attributePrior[i] / totalPrior;
            ret[i] = thisValue / AttributeCalculator.ATTRIBUTES[i].value;
        }
        return ret;
    }
    
    /**
     * 通过修改过的配置表倒推出前缀配置。
     * @param attrs
     */
    public void updatePriors(float[] attrs) {
        float totalValue = 0.0f;
        for (int i = 0; i < attrs.length; i++) {
            attributePrior[i] = attrs[i] * AttributeCalculator.ATTRIBUTES[i].value;
            totalValue += attributePrior[i];
        }
        if (totalValue < 0.001f) {
            Arrays.fill(attributePrior, 0.0f);
        } else {
            for (int i = 0; i < attrs.length; i++) {
                attributePrior[i] = attributePrior[i] * 100 / totalValue;
            }
        }
    }
    
    /**
     * 调整配置表，使指定属性在总属性中的比例符合指定比例要求。
     * @param index
     * @param percent
     */
    public void setPriorByPercent(int index, float percent, boolean[] lockFlag) {
        // 计算锁定的列所占的百分比
        float[] pers = getPercents();
        float lockPer = 0.0f;
        for (int i = 0; i < pers.length; i++) {
            if (lockFlag[i]) {
                lockPer += pers[i];
            }
        }
        
        // 100%特殊处理
        if (percent + lockPer > 0.99999f) {
            for (int i = 0; i < attributePrior.length; i++) {
                if (!lockFlag[i] && i != index) {
                    attributePrior[index] += attributePrior[i];
                    attributePrior[i] = 0.0f;
                }
            }
            return;
        }
        
        // 重排新的权重值
        float unlockTotal = 1.0f - lockPer - pers[index];
        if (unlockTotal < 0.00001f) {
            // 加入除了拖动的剩下的都为0，则不允许拖动
            return;
        }
        for (int i = 0; i < pers.length; i++) {
            if (lockFlag[i]) {
                attributePrior[i] = 100.0f * pers[i];
            } else if (i == index) {
                attributePrior[i] = 100.0f * percent;
            } else {
                attributePrior[i] = 100.0f * pers[i] * (1.0f - lockPer - percent) / unlockTotal;
            }
        }
    }
    
    /**
     * 计算每个属性的百分比。
     */
    public float[] getPercents() {
        float totalPrior = 0.0f;
        for (int i = 0; i < attributePrior.length; i++) {
            totalPrior += attributePrior[i];
        }
        float[] ret = new float[attributePrior.length];
        if (totalPrior != 0.0f) {
            for (int i = 0; i < attributePrior.length; i++) {
                ret[i] = attributePrior[i] / totalPrior;
            }
        }
        return ret;
    }
    
    /**
     * 从prefix.xml中载入。
     */
    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        title = elem.getAttributeValue("title");
        description = elem.getAttributeValue("desc");
        
        minLevel = Integer.parseInt(elem.getAttributeValue("minlevel"));
        maxLevel = Integer.parseInt(elem.getAttributeValue("maxlevel"));
        minQuality = Integer.parseInt(elem.getAttributeValue("minquality"));
        maxQuality = Integer.parseInt(elem.getAttributeValue("maxquality"));
        
        Arrays.fill(attributePrior, 0.0f);
        
        List children = elem.getChildren("attribute");
        for (Object child : children) {
            Element childElem = (Element)child;
            
            String id = childElem.getAttributeValue("id");
            float value = Float.parseFloat(childElem.getAttributeValue("value"));
            int index = AttributeCalculator.findIndexOfAttribute(id);
            if (index >= 0) {
                attributePrior[index] = value;
            }
        }
    }

    /**
     * 保存到prefix.xml
     */
    public Element save() {
        Element ret = new Element("prefix");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("title", title);
        ret.addAttribute("desc", description);
        
        ret.addAttribute("minlevel", String.valueOf(minLevel));
        ret.addAttribute("maxlevel", String.valueOf(maxLevel));
        ret.addAttribute("minquality", String.valueOf(minQuality));
        ret.addAttribute("maxquality", String.valueOf(maxQuality));
        
        for (int i = 0; i < attributePrior.length; i++) {
            if (attributePrior[i] <= 0.0f) {
                continue;
            }
            Element childElem = new Element("attribute");
            childElem.addAttribute("id", AttributeCalculator.ATTRIBUTES[i].id);
            childElem.addAttribute("value", String.valueOf(attributePrior[i]));
            ret.addContent(childElem);
        }
        return ret;
    }

    public boolean depends(DataObject obj) {
        return false;
    }

    public DataObject duplicate() {
        EquipmentPrefix prefix = new EquipmentPrefix(owner);
        prefix.update(this);
        return prefix;
    }

    public void update(DataObject obj) {
        EquipmentPrefix target = (EquipmentPrefix)obj;
        
        id = target.id;
        title = target.title;
        description = target.description;
        
        minLevel = target.minLevel;
        maxLevel = target.maxLevel;
        minQuality = target.minQuality;
        maxQuality = target.maxQuality;
        
        System.arraycopy(target.attributePrior, 0, attributePrior, 0, attributePrior.length);
    }

    @Override
    public boolean changed(DataObject obj) {
        return changed(this, obj);
    }
    
    /**
     * 判断一个前缀的配置是否被修改过。
     * @param oo
     * @return
     */
    public boolean isChanged(EquipmentPrefix oo) {
        if (id != oo.id) {
            return true;
        }
        if (!title.equals(oo.title)) {
            return true;
        }
        if (minLevel != oo.minLevel || maxLevel != oo.maxLevel || minQuality != oo.minQuality || maxQuality != oo.maxQuality) {
            return true;
        }
        return !Arrays.equals(attributePrior, oo.attributePrior);
    }
    
    public String toString(){
        return title;
    }
    
    public String getHintString() {
        StringBuilder sb = new StringBuilder();
        float[] pers = getPercents();
        for (int i = 0; i < AttributeCalculator.ATTRIBUTES.length; i++) {
            if (pers[i] > 0.0001f) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(AttributeCalculator.ATTRIBUTES[i].shortName + " " + DescriptionPattern.formatPercent(pers[i] * 100.0f));
            }
        }
        return sb.toString();
    }
}
