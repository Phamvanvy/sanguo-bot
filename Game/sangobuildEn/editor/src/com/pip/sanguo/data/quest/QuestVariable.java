package com.pip.sanguo.data.quest;

import java.util.*;
import org.jdom.*;

/**
 * 任务中的一个变量。
 * @author lighthu
 */
public class QuestVariable {
    /**
     * 变量名称。
     */
    public String name = "";
    
    public Element save() {
        Element ret = new Element("variable");
        ret.addAttribute("name", name);
        return ret;
    }
    
    public void load(Element elem) {
        name = elem.getAttributeValue("name");
    }
    
    public String toString() {
        return name;
    }
}
