package com.pip.sanguo.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

public class BookConfig extends DataObject{
    
    /**
     * 所属项目
     */
    public ProjectData owner;
    
    /**
     * 属性
     */
    public int property = 0;
    /**
     * 描述
     */
    public String dec="";
    /**
     * 升级上限
     */
    public int upLimit=0;
    /**
     *等级
     */
    public int level=0;
    
    /**
     *取值
     */
    public int value = 0;
    /**
     * 时间
     */
    public int time = 0;
    
    /**
     * 是否预装
     */
    public int auto=0;

    /**
     * 书籍等级提升对应的属性(力量,敏捷,体力,智力)数值
     */
    public List<BookChapter> basicAttrAdvances = new ArrayList<BookChapter>();
    
    public Map<Integer, BookChapter> attrAdvances = new HashMap<Integer,BookChapter>();
    
    /**
     * 书籍类
     * @param owner 所属项目
     */
    public BookConfig(ProjectData owner) {
        this.owner = owner;
    }
    
    public String toString() {
        return id + ": " + title;
    }
    
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public boolean changed(DataObject obj) {
        return changed(this, obj);
    }

    @Override
    public boolean depends(DataObject obj) {
        return false;
    }

    @Override
    public DataObject duplicate() {
        BookConfig ret = new BookConfig(this.owner);
        ret.update(this);
        return ret;
    }

    @Override
    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        title = elem.getAttributeValue("title");
        property = Integer.parseInt(elem.getAttributeValue("property"));
        dec = elem.getAttributeValue("dec");
        upLimit = Integer.parseInt(elem.getAttributeValue("uplimit"));
        try {
            auto = Integer.parseInt(elem.getAttributeValue("auto"));
        }
        catch (NumberFormatException e) {
            auto = 0;
        }
        List attrs = elem.getChildren("attr");
        if (attrs != null) {
            for (int i = 0; i < attrs.size(); i++) {
                BookChapter attr = new BookChapter(this);
                attr.load((Element)attrs.get(i));
                basicAttrAdvances.add(attr);
                attrAdvances.put(attr.level, attr);
                if (attr.level == 1) {
                    value = attr.value;
                    time = attr.time;
                }
            }
        }
    }

    @Override
    public Element save() {
        Element ret = new Element("book");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("title", title);
        ret.addAttribute("property", String.valueOf(property));
        ret.addAttribute("uplimit", String.valueOf(upLimit));
        ret.addAttribute("dec", dec);
        ret.addAttribute("auto",String.valueOf(auto));
        for (BookChapter attr : basicAttrAdvances) {
            ret.addContent(attr.save());
        }
        return ret;
    }

    @Override
    public void update(DataObject obj) {
        BookConfig oo = (BookConfig)obj;
        id = oo.id;
        title = oo.title;
        property = oo.property;
        upLimit = oo.upLimit;
        dec = oo.dec;
        value = oo.value;
        time = oo.time;
        auto = oo.auto;
        basicAttrAdvances.clear();
        for (BookChapter attr : oo.basicAttrAdvances) {
            BookChapter newAttr = attr.duplicate();
            newAttr.owner = this;
            basicAttrAdvances.add(newAttr);
        }
    }
    
    public BookConfig(int id){
        this.id = id;
    }

}
