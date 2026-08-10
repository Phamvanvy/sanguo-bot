package com.pip.sanguo.data.quest;

import java.util.*;
import org.jdom.*;

/**
 * 任务中的一个触发器。触发器包括2个部分：触发条件和触发动作。
 * @author lighthu
 */
public class QuestTrigger {
    /**
     * 触发条件。
     */
    public String condition;
    /**
     * 触发动作。
     */
    public String action;
    /**
     * 触发条件（替换变量名后的版本）
     */
    public String serverCondition;
    /**
     * 触发动作（替换变量名后的版本）
     */
    public String serverAction;

    public Element save() {
        Element ret = new Element("trigger");
        ret.addAttribute("condition", condition);
        ret.addAttribute("action", action);
        if (serverCondition != null) {
            ret.addAttribute("servercondition", serverCondition);
        }
        if (serverAction != null) {
            ret.addAttribute("serveraction", serverAction);
        }
        return ret;
    }
    
    public void load(Element elem) {
        condition = elem.getAttributeValue("condition");
        action = elem.getAttributeValue("action");
        condition = condition.replaceAll("\\\\r\\\\n", "\\\\n");
        action = action.replaceAll("\\\\r\\\\n", "\\\\n");
        
        serverCondition = elem.getAttributeValue("servercondition");
        serverAction = elem.getAttributeValue("serveraction");
        if (serverCondition != null) {
            serverCondition = serverCondition.replaceAll("\\\\r\\\\n", "\\\\n");
        }
        if (serverAction != null) {
            serverAction = serverAction.replaceAll("\\\\r\\\\n", "\\\\n");
        }
    }
}
