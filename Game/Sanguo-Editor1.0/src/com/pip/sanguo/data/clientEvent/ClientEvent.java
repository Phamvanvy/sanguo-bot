package com.pip.sanguo.data.clientEvent;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;

public class ClientEvent extends DataObject {
    //------------------UI类型-------------------
    /**
     * 第一版新UI-蓝色(待废弃)
     */
    public static final int UI_TYPE_NEWBLUE = 0;
    /**
     * java旧UI
     */
   public static final int UI_TYPE_JAVA_OLD = 1;
   
   /**
    * 第二版新UI-黄色
    */
   public static final int UI_TYPE_NEW2_YEELOW = 2;
   /**
    * 通用(不包括第一版新UI)
    */
   public static final int UI_TYPE_COMMAND = 3;
   /**
    * 废弃事件
    */
   public static final int UI_TYPE_NONE  = 4;
   
    /**
     * 事件类型。
     */
    public int type;
    
    /**
     * 机型UI类型。
     */
    public int uiType;
    
    /**
     * 阵营。
     */
    public int faction;
    
    /**
     * 事件重启间隔时间(秒)。
     */
    public int restartTime;
    
    /**
     * 适合级别--下限
     */
    public int suitLvlMin = 1;
    
    /**
     * 适合级别--上限
     */
    public int suitLvlMax = 15;
    
    /**
     * 接受任务的条件。
     */
    //public EventTrigger trigger = new EventTrigger(this);
    /**
     * 所属项目。
     * */
    public ProjectData owner;
    
    /**
     * 第一种事件条件。
     */
    public List<EventTrigger> triggers = new ArrayList<EventTrigger>();
    
    /**
     * 第二种事件条件。
     */
    public List<EventTrigger> triggers2 = new ArrayList<EventTrigger>();
    
    /**
     * 事件分支。
     */
    public List<EventItem> eventItems = new ArrayList<EventItem>();
    
    public ClientEvent(ProjectData owner) {
        this.owner = owner;
    }
    
    public int getID() {
        return id;
    }
    
    @Override
    public boolean changed(DataObject obj) {
        ClientEvent ce = (ClientEvent) obj;
        
        if (ce.id != this.id) {
            return true;
        }
        
        if (ce.type != this.type) {
            return true;
        }
        
        if (ce.uiType != this.uiType) {
            return true;
        }
        
        if (ce.faction != this.faction) {
            return true;
        }
        
        if (ce.restartTime != this.restartTime) {
            return true;
        }
        
        if (ce.suitLvlMin != this.suitLvlMin) {
            return true;
        }
        
        if (ce.suitLvlMax != this.suitLvlMax) {
            return true;
        }
        
        if (ce.categoryName != this.categoryName) {
            return true;
        }
        
        if (ce.description != this.description) {
            return true;
        }
        return false;
    }

    @Override
    public boolean depends(DataObject obj) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public DataObject duplicate() {
        ClientEvent ret = new ClientEvent(owner);
        ret.update(this);
        return ret;
    }

    @Override
    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        title = elem.getAttributeValue("title");
        type = Integer.parseInt(elem.getAttributeValue("type"));
        
        try {
            uiType = Integer.parseInt(elem.getAttributeValue("uiType"));
        } catch (Exception e) {
        }
        
        try {
            faction = Integer.parseInt(elem.getAttributeValue("faction"));
        } catch (Exception e) {
        }
        
        try {
            restartTime = Integer.parseInt(elem.getAttributeValue("restartTime"));
            suitLvlMin = Integer.parseInt(elem.getAttributeValue("suitLvlMin"));
            suitLvlMax = Integer.parseInt(elem.getAttributeValue("suitLvlMax"));
        } catch (Exception e) {
        }
        
        categoryName = elem.getAttributeValue("category");
        description = elem.getAttributeValue("description");
        
        List eventElems = elem.getChildren("eventtrigger");
        for (int i = 0; i < eventElems.size(); i++) {
            EventTrigger trigger = new EventTrigger(this);
            trigger.load((Element) eventElems.get(i));
            triggers.add(trigger);
        }
        eventElems.clear();
        
        eventElems = elem.getChildren("eventtrigger2");
        for (int i = 0; i < eventElems.size(); i++) {
            EventTrigger trigger = new EventTrigger(this);
            trigger.load((Element) eventElems.get(i));
            triggers2.add(trigger);
        }
        eventElems.clear();
        
        eventElems = elem.getChildren("eventitem");
        for (int i = 0; i < eventElems.size(); i++) {
            EventItem eventItem = new EventItem(this);
            eventItem.load((Element) eventElems.get(i));
            eventItems.add(eventItem);
        }
    }

    @Override
    public Element save() {
        Element ret = new Element("event");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("title", title);
        ret.addAttribute("type", String.valueOf(type));
        ret.addAttribute("uiType", String.valueOf(uiType));
        ret.addAttribute("faction", String.valueOf(faction));
        ret.addAttribute("restartTime", String.valueOf(restartTime));
        ret.addAttribute("suitLvlMin", String.valueOf(suitLvlMin));
        ret.addAttribute("suitLvlMax", String.valueOf(suitLvlMax));
        ret.addAttribute("description", description);
        if (categoryName != null) {
            ret.addAttribute("category", categoryName);
        } else {
            ret.addAttribute("category", "");
        }
        
        for (EventTrigger trigger : triggers) {
            ret.addContent(trigger.save());
        }
        
        for (EventTrigger trigger : triggers2) {
            ret.addContent(trigger.save2());
        }

        for (EventItem eventItem : eventItems) {
            ret.addContent(eventItem.save());
        }
        return ret;
    }

    @Override
    public void update(DataObject obj) {
        ClientEvent oo = (ClientEvent) obj;
        id = oo.id;
        title = oo.title;
        type = oo.type;
        uiType = oo.uiType;
        faction = oo.faction;
        restartTime = oo.restartTime;
        suitLvlMin = oo.suitLvlMin;
        suitLvlMax = oo.suitLvlMax;
        description = oo.description;
        categoryName = oo.categoryName;
        
        triggers.clear();
        for (EventTrigger trigger : oo.triggers) {
            EventTrigger newReward = trigger.duplicate();
            newReward.owner = this;
            triggers.add(newReward);
        }
        
        triggers2.clear();
        for (EventTrigger trigger : oo.triggers2) {
            EventTrigger newReward = trigger.duplicate();
            newReward.owner = this;
            triggers2.add(newReward);
        }
        
        eventItems.clear();
        for (EventItem eventItem : oo.eventItems) {
            EventItem newReward = eventItem.duplicate();
            newReward.owner = this;
            eventItems.add(newReward);
        }
        System.gc();
    }

}
