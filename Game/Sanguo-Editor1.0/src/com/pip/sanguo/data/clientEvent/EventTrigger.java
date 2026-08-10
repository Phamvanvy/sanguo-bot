package com.pip.sanguo.data.clientEvent;

import org.jdom.Element;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.DirectoryType;
import com.pip.sanguo.editor.clientevent.trigger.IExprEvent;
import com.pip.sanguo.editor.clientevent.trigger.TempManagerEvent;

/**
 * 任务的一个目标。
 * @author bqzhang
 */
public class EventTrigger extends DataObject{
    public ClientEvent owner;
    /**
     * 目标完成条件。
     */
    public int type;                 //触发条件索引
    public String condition = "";    //条件方法名
    public String paramType = "";    //参数基本类型
    public String paramValue = "";   //参数值
    
    public IExprEvent exprEvent;   //触发条件
    
    public EventTrigger(ClientEvent own) {
        owner = own;
    }

    public boolean equals(Object o) {
        return this == o;
    }
    
    public void load(Element elem) {
        type = Integer.parseInt(elem.getAttributeValue("type"));
        condition = elem.getAttributeValue("condition");
        paramType = elem.getAttributeValue("paramType");
        paramValue = elem.getAttributeValue("paramValue");
    }
    
    public Element save() {
        Element ret = new Element("eventtrigger");
        ret.addAttribute("type", String.valueOf(type));
        if(exprEvent != null){
            condition = exprEvent.getExpression();
            paramType = exprEvent.getParamsType();
            paramValue = exprEvent.getParamsValue();
        }
        ret.addAttribute("condition", condition);
        ret.addAttribute("paramType", paramType);
        ret.addAttribute("paramValue", paramValue);
        return ret;
    }
    
    public Element save2() {
        Element ret = new Element("eventtrigger2");
        ret.addAttribute("type", String.valueOf(type));
        if(exprEvent != null){
            condition = exprEvent.getExpression();
            paramType = exprEvent.getParamsType();
            paramValue = exprEvent.getParamsValue();
        }
        ret.addAttribute("condition", condition);
        ret.addAttribute("paramType", paramType);
        ret.addAttribute("paramValue", paramValue);
        return ret;
    }
    
    public EventTrigger duplicate() {
        EventTrigger ret = new EventTrigger(owner);
        ret.type = type;
        if(exprEvent == null){
            if(owner instanceof DirectoryType)
                exprEvent = TempManagerEvent.getExprEvent1(type);
            else
                exprEvent = TempManagerEvent.getExprEvent(type);
            try {
                exprEvent.initParamsValue(paramValue);
            }
            catch (Exception e) {
            }
        }
        ret.exprEvent = exprEvent;
        return ret;
    }
    
    public void update(DataObject obj) {
        EventTrigger trigger = (EventTrigger)obj;
        type = trigger.type;
        exprEvent = trigger.exprEvent;
    }
    
    public boolean depends(DataObject obj) {
        return false;
    }
    
    public boolean changed(DataObject obj) {
        return changed(this, obj);
    }
}
