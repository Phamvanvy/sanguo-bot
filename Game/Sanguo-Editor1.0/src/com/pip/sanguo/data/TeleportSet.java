package com.pip.sanguo.data;

import java.io.File;
import java.util.*;

import org.jdom.*;

import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.editor.EditorApplication;
import com.pipimage.utils.Utils;

/**
 * 传送门组。一个传送NPC对应一个传送门组。
 * @author lighthu
 */
public class TeleportSet extends DataObject {
    /**
     * 传送门类。
     * @author lighthu
     */
    public static class Teleport {
        /** 传送名称 */
        public String name = "";
        /** 目标地图ID */
        public int mapID;
        /** 目标X位置 */
        public int x;
        /** 目标Y位置 */
        public int y;
        /** 购买需求 */
        public List<Shop.BuyRequirement> requirements = new ArrayList<Shop.BuyRequirement>();
        
        public Teleport dup() {
            Teleport ret = new Teleport();
            ret.name = name;
            ret.mapID = mapID;
            ret.x = x;
            ret.y = y;
            for (Shop.BuyRequirement br : requirements) {
                ret.requirements.add(br.dup());
            }
            return ret;
        }
    };
    
    /**
     * 所属项目。
     */
    public ProjectData owner;
    /**
     * 传送目标列表
     */
    public List<Teleport> items = new ArrayList<Teleport>();
    
    public TeleportSet(ProjectData owner) {
        this.owner = owner;
    }

    public int getID() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }

    public boolean equals(Object o){
        return this == o;
    }
    
    public String toString() {
        return id + ": " + title;
    }

    /**
     * 从另外一个对象复制内容。
     */
    public void update(DataObject obj) {
        TeleportSet oo = (TeleportSet)obj;
        id = oo.id;
        title = oo.title;
        description = oo.description;
        categoryName = oo.categoryName;
        
        items.clear();
        for (Teleport item : oo.items) {
            Teleport newItem = item.dup();
            items.add(newItem);
        }
    }
    
    /**
     * 复制出一个对象来。
     */
    public DataObject duplicate() {
        TeleportSet ret = new TeleportSet(owner);
        ret.update(this);
        return ret;
    }

    @Override
    public boolean changed(DataObject obj) {
        return changed(this, obj);
    }
    
    /**
     * 从一个teleportset元素中载入传送点集合。
     */
    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        title = elem.getAttributeValue("title");
        description = elem.getAttributeValue("description");
        if(description == null){
            description = "";
        }
        categoryName = elem.getAttributeValue("category");
        if (categoryName == null) {
            categoryName = "";
        }
        
        items.clear();
        List itemElems = elem.getChildren("teleport");
        for (int i = 0; i < itemElems.size(); i++) {
            Teleport item = loadItem((Element)itemElems.get(i));
            if (item != null) {
                items.add(item);
            }
        }
    }
    
    /*
     * 从一个teleport元素中载入传送点。
     */
    private Teleport loadItem(Element elem) {
        Teleport ret = new Teleport();
        ret.name = elem.getAttributeValue("name");
        ret.mapID = Integer.parseInt(elem.getAttributeValue("mapid"));
        ret.x = Integer.parseInt(elem.getAttributeValue("x"));
        ret.y = Integer.parseInt(elem.getAttributeValue("y"));
        List reqElems = elem.getChildren("requirement");
        for (int i = 0; i < reqElems.size(); i++) {
            Shop.BuyRequirement req = Shop.BuyRequirement.load(owner, (Element)reqElems.get(i));
            if (req != null ){
                ret.requirements.add(req);
            }
        }
        return ret;
    }
    
    /**
     * 把对象保存到一个teleportset元素中。
     */
    public Element save() {
        Element ret = new Element("teleportset");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("title", title);
        ret.addAttribute("description", description);
        if (categoryName != null) {
            ret.addAttribute("category", categoryName);
        }
        
        for (Teleport item : items) {
            ret.addContent(saveItem(item));
        }
        return ret;
    }
    
    /*
     * 把一个商品保存到teleport元素中。
     */
    private Element saveItem(Teleport item) {
        Element ret = new Element("teleport");
        ret.addAttribute("name", item.name);
        ret.addAttribute("mapid", String.valueOf(item.mapID));
        ret.addAttribute("x", String.valueOf(item.x));
        ret.addAttribute("y", String.valueOf(item.y));
        for (Shop.BuyRequirement req : item.requirements) {
            Element elem = req.save();
            if (elem != null) {
                ret.addContent(elem);
            }
        }
        return ret;
    }
    
    public boolean depends(DataObject obj) {
        return false;
    }
}
