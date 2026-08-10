package com.pip.sanguo.data.clientEvent;

import org.jdom.Element;

import com.pip.sanguo.data.DataObject;

/**
 * 完成事件的一个事件项。一个事件项可能是打开菜单，选定菜单项等。
 * @author bqzhang
 */
public class EventItem extends DataObject{
	public ClientEvent owner;
	
	/**
	 * 引导提示类型
	 */
	public static final String[] EVENT_PROMPT = {
	    "引导说明",
	    "无提示",
	    "菜单中的菜单项",
	    "包格中的物品",
	    "装备栏中的装备位",
	    "返回",
	    "坐骑列表中的坐骑",
	    "随从列表中的随从",
	    "任务引导",
	    "引导事件延迟",
	    "任务列表中的任务",
	    "菜单或确认(左软件按键图标)",
	    "游戏界面技能栏中的技能",
	    "属性加点",
	    "技能加点界面技能格",
	    "技能快捷栏",
	    "确认选项选择",
	    "包格中的物品或其他(0-物品1-技能)",
	    "使用血瓶引导",
	    "使用蓝瓶引导",
	    "属性设置(新UI)",
	    "配置快捷按钮(新UI)",
	    "技能快捷配置引导(新UI)",
	    "菜单界面顶部菜单项(新UI)",
	    "菜单界面左侧菜单项(新UI)"
	};
	
	/**
     * 玩家操作
     */
    public static final String[] EVENT_ACTIONS = {
        "无",
        "确定",
        "返回",
        "打开菜单",
        "关闭其他界面打开菜单",
        "寻路到目标",
        "寻路到目标并接受任务",
        "穿上装备",
        "放入快捷",
        "寻路到目标并完成任务",
        "拖拽(新UI)"
    };
	
	///////////////////////////////////////////////引导提示类型
     /** 
      * 引导说明(貂蝉)
      */
     public static final int PROMPT_TYPE_NOTICE = 0;
     /**
      * 简单文字提示
      */
    public static final int PROMPT_TYPE_NOTICE_SIMPLE = 1;
    
    /**
     * 菜单中的菜单项
     */
    public static final int PROMPT_TYPE_SELECT_MENU   = 2;
    /**
     * 包格中的物品
     */
	public static final int PROMPT_TYPE_SELECT_ITEM   = 3;
    /**
     * 装备栏中的装备位
     */
	public static final int PROMPT_TYPE_SELECT_EQUIP  = 4;
	/**
     * 返回
     */
    public static final int PROMPT_TYPE_BACK  = 5;
    
    /**
     * 坐骑列表中的坐骑
     */
    public static final int PROMPT_TYPE_SELECT_HORSE  = 6;
    
    /**
     * 随从列表中的随从
     */
    public static final int PROMPT_TYPE_SELECT_ATTENDANT  = 7;
    
    /**
     * 任务引导
     */
    public static final int PROMPT_TYPE_QUESTS  = 8;
    
    /**
     * 引导事件延迟
     */
    public static final int PROMPT_TYPE_DELAY_TIME  = 9;
    
    /**
     * 任务列表中的任务
     */
    public static final int PROMPT_TYPE_SELECT_QUEST  = 10;
    
    /**
     * 菜单或确认
     */
    public static final int PROMPT_TYPE_MENU  = 11;
    
    /**
     * 技能栏中的技能
     */
    public static final int PROMPT_TYPE_SELECT_SKILL  = 12;
    
    /**
     * 属性加点
     */
    public static final int PROMPT_ATT_ADD_POINT  = 13;
    
    /**
     * 技能加点界面技能格
     */
    public static final int PROMPT_SKILL_ADD_POINT  = 14;
    
    /**
     * 技能快捷栏
     */
    public static final int PROMPT_SKILL_QUICK_GRID  = 15;
    
    /**
     * 确认选项选择
     */
    public static final int PROMPT_CONFIRM_CHANGE  = 16;
    
    /**
     * 包格中的物品或其他(0-物品1-技能)
     */
    public static final int PROMPT_TYPE_ITEM_BYTYPE  = 17;
    
    /**
     * 使用血瓶引导
     */
    public static final int PROMPT_TYPE_ITEM_REDGRASS  = 18;
    
    /**
     * 使用蓝瓶引导
     */
    public static final int PROMPT_TYPE_ITEM_BLUEGRASS  = 19;
    
    /**
     * 属性设置(新UI)
     */
    public static final int PROMPT_ATT_POINT_OPENUI  = 20;
    
    /**
     * 配置快捷按钮(新UI)
     */
    public static final int PROMPT_SKILL_OPEN_SETQUICKUI  = 21;
    
    /**
     * 技能快捷配置引导(新UI)
     */
    public static final int PROMPT_SET_QUICK_SKILL = 22;
    
    /**
     * 菜单界面顶部菜单项(新UI)
     */
    public static final int PROMPT_MAINFRAME_TOP_MENU = 23;
    
    /**
     * 菜单界面左侧菜单项(新UI)
     */
    public static final int PROMPT_MAINFRAME_LEFT_MENU = 24;
	///////////////////////////////////////////////提示类型    end
	
    
	///////////////////////////////////////////////玩家动作
    /**
     * 无
     */
    public static final int ACTION_TYPE_NONE   = 0;
    
    /**
     * 确定
     */
    public static final int ACTION_TYPE_ENTER   = 1;
    
    /**
     * 返回
     */
    public static final int ACTION_TYPE_BACK   = 2;
    
    /**
     * 打开菜单
     */
    public static final int ACTION_TYPE_OPEN_MENU   = 3;
    /**
     * 在游戏界面打开菜单
     */
    public static final int ACTION_TYPE_OPEN_MAINMENU   = 4;
    
    /**
     * 寻路到目标
     */
    public static final int ACTION_TYPE_MOVE   = 5;
    
    /**
     * 寻路到目标并接受任务
     */
    public static final int ACTION_TYPE_CHANGE_TARGET   = 6;
    
    /**
     * 穿上装备
     */
    public static final int ACTION_TYPE_UP_EQUIP   = 7;
    
    /**
     * 放入快捷
     */
    public static final int ACTION_TYPE_SET_QUICK   = 8;
    
    /**
     * 寻路到目标并完成任务
     */
    public static final int ACTION_TYPE_TARGET_FINISH  = 9;
    
    /**
     * 拖拽
     */
    public static final int ACTION_TYPE_TARGET_DROPMOVE  = 10;
    ///////////////////////////////////////////////玩家动作 end
    
	/**
     * 引导提示类型。
     */
    public int promptType;
    /**
     * 提示参数
     */
    public String promptParam;
    
    /**
     * 玩家操作
     */
    public int actionType;
	
	/**
	 * 按键版文字描述
	 */
	public String eventDesKey;
	
	/**
     * 触摸版文字描述
     */
    public String eventDesTouch;
	
    /**
     * 操作指引提示文字--按键版
     */
    public String signDescKey;
    
    /**
     * 操作指引提示文字--触摸版
     */
    public String signDescTouch;
    
	public EventItem(ClientEvent own) {
		owner = own;
		this.promptParam = "";
		this.eventDesKey = "";
		this.eventDesTouch = "";
		this.signDescKey = "";
		this.signDescTouch = "";
	}

    public boolean equals(Object o) {
        return this == o;
    }
    
    public void load(Element elem) {
        //eventType = Integer.parseInt(elem.getAttributeValue("eventType"));
        try {
            promptType = Integer.parseInt(elem.getAttributeValue("promptType"));
        	promptParam = elem.getAttributeValue("promptParam");
        	actionType = Integer.parseInt(elem.getAttributeValue("actionType"));
        	eventDesKey = elem.getAttributeValue("desKey");
        	eventDesTouch = elem.getAttributeValue("desTouch");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try{
            signDescKey = elem.getAttributeValue("signDesc");
        }catch(Exception e){
        }
        if(signDescKey == null){
            signDescKey = "";
        }
        
        try{
            signDescTouch = elem.getAttributeValue("signDescTouch");
        }catch(Exception e){
        }
        if(signDescTouch == null){
            signDescTouch = "";
        }
    }
    
    public Element save() {
        Element ret = new Element("eventitem");
        //ret.addAttribute("eventType", String.valueOf(eventType));
        ret.addAttribute("promptType", String.valueOf(promptType));
        ret.addAttribute("promptParam", String.valueOf(promptParam));
        ret.addAttribute("actionType", String.valueOf(actionType));
        ret.addAttribute("desKey", eventDesKey);
        ret.addAttribute("desTouch", eventDesTouch);
        ret.addAttribute("signDesc", signDescKey);
        ret.addAttribute("signDescTouch", signDescTouch);
        return ret;
    }
    
    public EventItem duplicate() {
    	EventItem ret = new EventItem(owner);
    	//ret.eventType = eventType;
    	ret.promptType = promptType;
    	ret.promptParam = promptParam;
        ret.actionType = actionType;
    	ret.eventDesKey = eventDesKey;
    	ret.eventDesTouch = eventDesTouch;
    	ret.signDescKey = signDescKey;
    	ret.signDescTouch = signDescTouch;
    	return ret;
    }
    
    public void update(DataObject obj) {
        EventItem event = (EventItem)obj;
        //eventType = event.eventType;
        promptType = event.promptType;
        promptParam = event.promptParam;
        actionType = event.actionType;
        eventDesKey = event.eventDesKey;
        eventDesTouch = event.eventDesTouch;
        signDescKey = event.signDescKey;
        signDescTouch = event.signDescTouch;
    }
    
    public boolean depends(DataObject obj) {
        return false;
    }
    
    @Override
    public boolean changed(DataObject obj) {
        return changed(this, obj);
    }
}
