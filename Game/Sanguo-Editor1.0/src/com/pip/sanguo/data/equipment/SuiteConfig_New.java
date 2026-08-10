package com.pip.sanguo.data.equipment;

import org.jdom.Element;

import java.util.*;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Faction;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Title;
import com.pip.sanguo.data.skill.BuffConfig;

/**
 * 新套装定义。
 * @author chunhui.shao
 */
public class SuiteConfig_New extends DataObject {
    /**
     * 所属项目。
     */
    public ProjectData owner;
    /**
     * 包含装备。
     */
    public List<Equipment> equipments = new ArrayList<Equipment>();
    public Map<Integer, Integer> weights = new HashMap<Integer, Integer>();

    /**
     * 新套装效果定义。
     * @author chunhui.shao
     */
    public static class SuiteEffect_New {
        public String equipName;
        public int buffID;
        
        public int weight;
        public int buff2ID;
        public int equipWeight;
        
        public SuiteEffect_New dup() {
            SuiteEffect_New ret = new SuiteEffect_New();
            ret.equipName=equipName;
            ret.buffID = buffID;
            ret.weight=weight;
            ret.equipWeight=equipWeight;
            ret.buff2ID=buff2ID;
            return ret;
        }
    }
    
    /**
     * 套装效果列表。
     */
    public List<SuiteEffect_New> effects = new ArrayList<SuiteEffect_New>();
    
    public SuiteConfig_New(ProjectData owner) {
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
        SuiteConfig_New oo = (SuiteConfig_New)obj;
        id = oo.id;
        title = oo.title;
        description = oo.description;
        categoryName = oo.categoryName;
        
        equipments.clear();
        equipments.addAll(oo.equipments);
        effects.clear();
        for (SuiteEffect_New eff : oo.effects) {
            effects.add(eff.dup());
        }
        
        if (owner != oo.owner) {
            List<Equipment> newEqus = new ArrayList<Equipment>();
            for (Equipment equ : equipments) {
                newEqus.add((Equipment)owner.findEquipment(equ.id));
            }
            equipments = newEqus;
        }
    }
    
    public DataObject duplicate() {
        SuiteConfig_New ret = new SuiteConfig_New(owner);
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
        
        equipments.clear();
        List list = elem.getChildren("equipment");
        for (int i = 0; i < list.size(); i++) {
            int eid = Integer.parseInt(((Element)list.get(i)).getAttributeValue("id"));
            int weight = Integer.parseInt(((Element)list.get(i)).getAttributeValue("equipweight"));
            weights.put(eid, weight);
            Equipment equ = owner.findEquipment(eid);
            if (equ != null) {
                equipments.add(equ);
            }
        }

        effects.clear();
        list = elem.getChildren("effect");
        for (int i = 0; i < list.size(); i++) {
            SuiteEffect_New eff = new SuiteEffect_New();
            Element elem2 = (Element)list.get(i);
            eff.equipName=elem2.getAttributeValue("equipname");
            eff.buffID = Integer.parseInt(elem2.getAttributeValue("buffid"));
            eff.buff2ID = Integer.parseInt(elem2.getAttributeValue("buff2id")==null?"0":elem2.getAttributeValue("buff2id"));
            eff.equipWeight=Integer.parseInt(elem2.getAttributeValue("equipweight"));
            String weightStr=elem2.getAttributeValue("weight")==null?"0":elem2.getAttributeValue("weight");
            eff.weight = Integer.parseInt(weightStr);
            if (owner.findObject(BuffConfig.class, eff.buffID) != null) {
                effects.add(eff);
            }
        }
    }
    
    public Element save() {
        Element ret = new Element("newsuite");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("title", title);
        ret.addAttribute("description", description);
        if (categoryName != null) {
            ret.addAttribute("category", categoryName);
        }
        
        for (int i=0;i<equipments.size();i++) {
            Equipment equ = equipments.get(i);
            Element elem = new Element("equipment");
            elem.addAttribute("id", String.valueOf(equ.id));
            elem.addAttribute("equipweight", String.valueOf(effects.get(i).equipWeight));
            elem.addAttribute("weight", String.valueOf(effects.get(i).weight));
            ret.addContent(elem);
        }
        for (SuiteEffect_New eff : effects) {
            Element elem = new Element("effect");
            elem.addAttribute("buffid", String.valueOf(eff.buffID));
            elem.addAttribute("buff2id", String.valueOf(eff.buff2ID));
            elem.addAttribute("equipname", String.valueOf(eff.equipName));
            elem.addAttribute("equipweight", String.valueOf(eff.equipWeight));
            elem.addAttribute("weight", String.valueOf(eff.weight));
            ret.addContent(elem);
        }
        return ret;
    }
    
    public boolean depends(DataObject obj) {
        if (obj instanceof BuffConfig) {
            int buffID = ((BuffConfig)obj).id;
            for (SuiteEffect_New eff : effects) {
                if (eff.buffID == buffID) {
                    return true;
                }
            }
        } else if (obj instanceof Equipment) {
            return equipments.contains(obj);
        }
        return false;
    }
}

