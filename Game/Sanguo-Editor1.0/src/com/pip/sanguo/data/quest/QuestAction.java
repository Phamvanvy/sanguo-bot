package com.pip.sanguo.data.quest;

import java.util.*;
import org.jdom.*;

/**
 * 触发器触发的一个动作。
 * @author lighthu
 */
public class QuestAction {
    /**
     * 动作类型。
     */
    public String command = "";
    /**
     * 动作参数。
     */
    public List<String> params = new ArrayList<String>();

    public Element save() {
        Element ret = new Element("action");
        ret.addAttribute("command", command);
        for (String param : params) {
            Element elem = new Element("param");
            elem.addAttribute("value", param);
            ret.getMixedContent().add(elem);
        }
        return ret;
    }
    
    public void load(Element elem) {
        command = elem.getAttributeValue("command");
        List list = elem.getChildren("param");
        for (Object obj : list) {
            Element elem2 = (Element)obj;
            params.add(elem2.getAttributeValue("value"));
        }
    }
}
