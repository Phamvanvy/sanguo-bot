package com.pip.sanguo.data;

import org.jdom.Element;

public class BookChapter {
    
    public BookConfig owner;
    /**
     * 等级
     */
    public int level;
    /**
     * 取值
     */
    public int value;
    /**
     * 时间
     */
    public int time;
    
    /**
     * 书籍的基础属性类
     * @param owner 所属对象
     */
    public BookChapter(BookConfig owner) {
        this.owner = owner;
    }
    
    public boolean equals(Object o) {
        return this == o;
    }
    
    public void load(Element elem) {
        try {
            level = Integer.parseInt(elem.getAttributeValue("level"));
        } catch (Exception e) {
            level = 0;
        }
        try {
            value = Integer.parseInt(elem.getAttributeValue("value"));
        } catch (Exception e) {
            value = 0;
        }
        try {
            time = Integer.parseInt(elem.getAttributeValue("time"));
        } catch (Exception e) {
            time = 0;
        }
    }
    
    public Element save() {
        Element elem = new Element("attr");
        elem.addAttribute("level", String.valueOf(level));
        if (value != 0) {
            elem.addAttribute("value", String.valueOf(value));
        }
        if (time != 0) {
            elem.addAttribute("time", String.valueOf(time));
        }
        return elem;
    }
    
    public BookChapter duplicate() {
        BookChapter ret = new BookChapter(owner);
        ret.level = level;
        ret.value = value;
        ret.time = time;
        return ret;
    }
}
