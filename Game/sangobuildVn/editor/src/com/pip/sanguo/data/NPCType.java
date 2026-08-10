package com.pip.sanguo.data;

import org.jdom.*;

/**
 * NPC类型定义。
 * @author lighthu
 */
public class NPCType extends DataObject {
    public String toString() {
        return id + ": " + title;
    }
        
    public void update(DataObject obj) {}
    
    public DataObject duplicate() {
        return null;
    }
    
    @Override
    public boolean changed(DataObject obj) {
        return changed(this, obj);
    }
    
    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        title = elem.getAttributeValue("title");
        description = elem.getText();
    }
    
    public Element save() {
        Element ret = new Element("npctype");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("title", title);
        ret.setText(description);
        return ret;
    }
    
    public boolean depends(DataObject obj) {
        return false;
    }
}
