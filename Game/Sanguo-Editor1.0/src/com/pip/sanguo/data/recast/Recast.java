package com.pip.sanguo.data.recast;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import com.pip.mapeditor.data.MapFile;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Faction;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Title;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.skill.BuffConfig;

/**
 * 称号。
 */
public class Recast extends DataObject {
    /**
     * 所属项目。
     */
    public ProjectData owner;
    /**
     * 重铸品质。
     */
    public int type;
    /**
     * 提升的物品等级。
     */
    public int level = 5;
    /**
     * 重铸的价格
     */
    public int price;
    /**
     * 重铸需要消耗的物品ID
     */
    public int itemId;
    /**
     * 重铸需要消耗的物品数量
     */
    public int itemNum;
    /**
     * 属性增加区间的各区间比率
     */
    public int[] areaRatio;
    /**
     * 重铸性格
     */
    public List<RecastProperty> propertys = new ArrayList<RecastProperty>();
    
    /**
     * Recast的构造函数
     * @param owner
     */
    public Recast(ProjectData owner) {
        this.owner = owner;
    }
    
    /**
     * 获取索引
     * @return  索引
     */
    public int getId() {
        return id;
    }
    
    /**
     * 获取重铸类型
     * @return 类型
     */
    public int getType() {
        return type;
    }
    
    /**
     * 获取重铸名称
     */
    public String getName() {
        return title;
    }
    
    /**
     * 获取提升的物品等级
     * @return 等级
     */
    public int getUpLevel() {
        return level;
    }
    
    /**
     * 获取基础价格
     * @return  价格
     */
    public int getBasePrice() {
        return price;
    }
    
    /**
     * 获取属性加成区间比率值
     */
    public int[] getAreaRatio() {
        return areaRatio;
    }
    
    /**
     * 获取重铸性格
     * @return  性格
     */
    public List<RecastProperty> getPropertys() {
        return propertys;
    }
    
    public String toString() {
        return id + ": " + title;
    }

    public boolean equals(Object o) {
        return this == o;
    }
    
    public DataObject duplicate() {
        Recast ret = new Recast(owner);
        ret.update(this);
        return ret;
    }
    
    public void update(DataObject obj) {
        Recast oo = (Recast)obj;
        id = oo.id;
        title = oo.title;
        level = oo.level;
        price = oo.price;
        itemId = oo.itemId;
        itemNum = oo.itemNum;
        areaRatio = oo.areaRatio;
        propertys.clear();
        for (RecastProperty target : oo.propertys) {
            RecastProperty newProperty = target.duplicate();
            newProperty.owner = this;
            propertys.add(newProperty);
        }
    }
    
    @Override
    public boolean changed(DataObject obj) {
        return changed(this, obj);
    }
    
    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        title = elem.getAttributeValue("title");
        level = Integer.parseInt(elem.getAttributeValue("level"));
        price = Integer.parseInt(elem.getAttributeValue("cost"));
        try {
            itemId = Integer.parseInt(elem.getAttributeValue("itemId"));
        } catch (Exception e) {
            itemId = -1;
        }
        try {
            itemNum = Integer.parseInt(elem.getAttributeValue("itemNum"));
        } catch (Exception e) {
            itemNum = 0;
        }
        String area = elem.getAttributeValue("areaRatio");
        if (area != null && area.length() > 0) {
            setAreaRatio(area);
        }
        
        List propertys = elem.getChildren("property");
        for (int i = 0; i < propertys.size(); i++) {
            RecastProperty target = new RecastProperty(this);
            target.load((Element)propertys.get(i));
            this.propertys.add(target);
        }
    }
    
    public Element save() {
        Element ret = new Element("recast");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("title", title);
        ret.addAttribute("level", String.valueOf(level));
        ret.addAttribute("cost", String.valueOf(price));
        ret.addAttribute("itemId", String.valueOf(itemId));
        ret.addAttribute("itemNum", String.valueOf(itemNum));
        if (areaRatio != null && areaRatio.length > 0) {
            String area = getAreaRatioText();
            if (area != null && area.length() > 0) {
                ret.addAttribute("areaRatio", area);
            }
        }
        for (RecastProperty property : propertys) {
            ret.addContent(property.save());
        }
        return ret;
    }
    
    public boolean depends(DataObject obj) {
        return false;
    }
    
    /**
     * 查找一个重铸的名称。
     * @param project   
     * @param recastID 重铸的索引
     * @return  重铸名称
     */
    public static String toString(ProjectData project, int recastID) {
        Recast q = (Recast)project.findObject(Title.class, recastID);
        if (q == null) {
            return "无效重铸";
        } else {
            return q.toString();
        }
    }
    
    public void setAreaRatio(String areaStr) {
        String[] area = areaStr.split(",");
        if (area != null && area.length > 0) {
            areaRatio = new int[area.length];
            for (int i = 0; i < area.length; i++) {
                try {
                    areaRatio[i] = Integer.parseInt(area[i]);
                } catch (Exception e) {
                    areaRatio[i] = 0;
                }
            }
        }
    }
    
    public String getAreaRatioText() {
        String area = "";
        if (areaRatio != null && areaRatio.length > 0) {
            for (int i = 0; i < areaRatio.length; i++) {
                area += areaRatio[i];
                if (i < areaRatio.length - 1) {
                    area += ",";
                }
            }
        }
        return area;
    }
}
