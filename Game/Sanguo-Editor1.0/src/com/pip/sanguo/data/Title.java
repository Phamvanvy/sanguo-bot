package com.pip.sanguo.data;

import org.jdom.Element;

import com.pip.mapeditor.data.MapFile;
import com.pip.sanguo.data.skill.BuffConfig;

/**
 * 称号。
 */
public class Title extends DataObject {
    /** 其他称号 */
    public static final int TYPE_OTHER = 0;
    /** 官职称号 */
    public static final int TYPE_OFFICIAL = 1;
    /** 国家称号 */
    public static final int TYPE_COUNTRY = 2;
    
    /**
     * 所属项目。
     */
    public ProjectData owner;
    /**
     * 称号类型。
     */
    public int type;
    /**
     * 需要级别。
     */
    public int level = 1;
    /**
     * 价格（用声望兑换）
     */
    public int price;
    /**
     * 俸禄（0表示没有）
     */
    public int salary;
    /**
     * 所属阵营（如果中立则所有阵营都可兑换）
     */
    public Faction faction;
    /**
     * 对应增益效果
     */
    public int buffID;
    /**
     * 对应效果级别。
     */
    public int buffLevel;
    /**
     * 对应力量增加
     */
    public int str = 0;
    /**
     * 对应敏捷增加
     */
    public int agi = 0;
    /**
     * 对应智力增加
     */
    public int wis = 0;
    /**
     * 对应体力增加
     */
    public int sta = 0;

    public Title(ProjectData owner) {
        this.owner = owner;
    }

    public int getID() {
        return id;
    }
    
    public String toString() {
        return id + ": " + title;
    }

    public boolean equals(Object o) {
        return this == o;
    }
    
    public void update(DataObject obj) {
        Title oo = (Title)obj;
        id = oo.id;
        title = oo.title;
        description = oo.description;
        categoryName = oo.categoryName;
        
        type = oo.type;
        level = oo.level;
        price = oo.price;
        salary = oo.salary;
        faction = oo.faction;
        buffID = oo.buffID;
        buffLevel = oo.buffLevel;
        
        str = oo.str;
        agi = oo.agi;
        wis = oo.wis;
        sta = oo.sta;
        
        if (owner != oo.owner) {
            faction = (Faction)owner.findDictObject(Faction.class, faction.id);
        }
    }
    
    public DataObject duplicate() {
        Title ret = new Title(owner);
        ret.update(this);
        return ret;
    }

    @Override
    public boolean changed(DataObject obj) {
        return changed(this, obj);
    }
    
    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        title = elem.getAttributeValue("title");
        description = elem.getAttributeValue("description");
        categoryName = elem.getAttributeValue("category");
        if (categoryName == null) {
            categoryName = "";
        }
        
        type = Integer.parseInt(elem.getAttributeValue("type"));
        level = Integer.parseInt(elem.getAttributeValue("level"));
        price = Integer.parseInt(elem.getAttributeValue("price"));
        salary = Integer.parseInt(elem.getAttributeValue("salary"));
        int factionID = Integer.parseInt(elem.getAttributeValue("faction"));
        faction = (Faction)owner.findDictObject(Faction.class, factionID);
        buffID = Integer.parseInt(elem.getAttributeValue("buff"));
        try {
            buffLevel = Integer.parseInt(elem.getAttributeValue("bufflevel"));
        } catch (Exception e) {
            buffLevel = 1;
        }
        
        try {
            str = Integer.parseInt(elem.getAttributeValue("str"));
        } catch (Exception e) {
            str = 0;
        }
        
        try {
            agi = Integer.parseInt(elem.getAttributeValue("agi"));
        } catch (Exception e) {
            agi = 0;
        }
        
        try {
            wis = Integer.parseInt(elem.getAttributeValue("wis"));
        } catch (Exception e) {
            wis = 0;
        }
        
        try {
            sta = Integer.parseInt(elem.getAttributeValue("sta"));
        } catch (Exception e) {
            sta = 0;
        }
    }
    
    public Element save() {
        Element ret = new Element("title");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("title", title);
        ret.addAttribute("description", description);
        if (categoryName != null) {
            ret.addAttribute("category", categoryName);
        }
        
        ret.addAttribute("type", String.valueOf(type));
        ret.addAttribute("level", String.valueOf(level));
        ret.addAttribute("price", String.valueOf(price));
        ret.addAttribute("salary", String.valueOf(salary));
        ret.addAttribute("faction", String.valueOf(faction.id));
        ret.addAttribute("buff", String.valueOf(buffID));
        ret.addAttribute("bufflevel", String.valueOf(buffLevel));
        
        ret.addAttribute("str", String.valueOf(str));
        ret.addAttribute("agi", String.valueOf(agi));
        ret.addAttribute("wis", String.valueOf(wis));
        ret.addAttribute("sta", String.valueOf(sta));
        return ret;
    }
    
    public boolean depends(DataObject obj) {
        return false;
    }
    
    /**
     * 查找一个称号的名字。
     * @param project
     * @param titleID
     * @return
     */
    public static String toString(ProjectData project, int titleID) {
        Title q = (Title)project.findObject(Title.class, titleID);
        if (q == null) {
            return "无效称号";
        } else {
            return q.toString();
        }
    }
}
