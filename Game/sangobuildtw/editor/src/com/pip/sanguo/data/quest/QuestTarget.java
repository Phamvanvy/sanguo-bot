package com.pip.sanguo.data.quest;

import org.jdom.*;

/**
 * 任务的一个目标。
 * @author lighthu
 */
public class QuestTarget {
	public Quest owner;
	/**
	 * 目标完成条件。
	 */
	public String condition = "";
	/**
	 * 目标描述文本。
	 */
	public String description = "";
	/**
	 * 完成目标提示。
	 */
	public String hint = "";
	
	public QuestTarget(Quest own) {
		owner = own;
	}

    public boolean equals(Object o) {
        return this == o;
    }
    
    public void load(Element elem) {
    	condition = elem.getAttributeValue("condition");
//        description = elem.getText();
    	try {
            description = elem.getAttributeValue("description");
        }
        catch (Exception e) {
        }
        if(description == null){
            description = "";
        }
        hint = elem.getAttributeValue("hint");
        if (hint == null) {
            hint = "";
        }
    }
    
    public Element save() {
        Element ret = new Element("target");
        ret.addAttribute("condition", condition);
        ret.addAttribute("description", description);
        if (hint != null && hint.length() > 0) {
            ret.addAttribute("hint", hint);
        }
        //ret.setText(description);
        return ret;
    }
    
    public QuestTarget duplicate() {
    	QuestTarget ret = new QuestTarget(owner);
    	ret.condition = condition;
    	ret.description = description;
    	ret.hint = hint;
    	return ret;
    }
}
