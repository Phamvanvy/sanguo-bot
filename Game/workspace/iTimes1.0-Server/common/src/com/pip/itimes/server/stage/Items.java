package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.HashMap;

import com.pip.itimes.server.util.Utils;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Items {

    private static final Map templates = new HashMap();
    //name,item
    private static final Map taskTemplates = new HashMap();

    private static final Map messages1 = new HashMap();
    private static final Map messages2 = new HashMap();
    private static final Map messages3 = new HashMap();
    private static final Map messages4 = new HashMap();
    private static final Map messages5 = new HashMap();
    private static final Map messages6 = new HashMap();
    private static final Map messages7 = new HashMap();
    private static final Map messages8 = new HashMap();
    private static final Map messages9 = new HashMap();
    private static final Map messages10 = new HashMap();
    private static final Map messages11 = new HashMap();
    
    public static void addTemplate(IItemTemplate template){
        templates.put(new Integer(template.getItemId()),template);
        if(template.getType()==IItem.TYPE_TASK){
            taskTemplates.put(template.getName(),template);
        }
    }

    public static IItemTemplate getTemplate(int id){
        return (IItemTemplate)templates.get(new Integer(id));
    }

//    public static Equipment getEquipment(int id){
//        return (Equipment)equs.get(new Integer(id));
//    }

    public static TaskItemTemplate getTaskTemplate(String name){
        return (TaskItemTemplate)taskTemplates.get(name);
    }


    public static String getMessage(int itemId , int msgtype , String playname , String itemname,String boxname){
    	return getMessage(itemId, msgtype, playname, itemname, boxname, null);
    }
    
    public static String getMessage(int itemId , int msgtype , String playname , String itemname,String boxname, IItem item){
    	String tempstr = null;
    	switch (msgtype) {
        case 1://宝箱
        	tempstr = (String)messages1.get(itemId);
        	break;
        case 2://世界掉落
        	tempstr = (String)messages2.get(itemId);
        	break;
        case 3://商店购买
        	tempstr = (String)messages3.get(itemId);
        	break;
        case 4://i币商店购买
        	tempstr = (String)messages4.get(itemId);
        	break;
        case 5://家园领取
        	tempstr = (String)messages5.get(itemId);
        	break;
        case 6://小岛领取
        	tempstr = (String)messages6.get(itemId);
        	break;
        case 7://使用召唤符（type=47）
        	tempstr = (String)messages7.get(itemId);
        	break;
        case 8://使用寻缘镜（type=13）
        	tempstr = (String)messages8.get(itemId);
        	break;
        case 9:
        	tempstr = (String)messages9.get(itemId);
        	break;
        case 10:
        	tempstr = (String)messages10.get(itemId);
        	break;
        case 11:
        	tempstr = (String)messages11.get(itemId);
        	break;
    	}
    	if (tempstr != null){
    		//add jwp start
    		tempstr = tempstr.replaceAll("player", playname);
    		itemname = Utils.getClientItemColor(getTemplate(itemId).getQuality()) + itemname + "</c>";
    		tempstr = tempstr.replaceAll("item", itemname);
    		tempstr = tempstr.replaceAll("box",boxname);
    		//末尾加上/s
    		//获取物品id的位数
    		int id = itemId;
    		//当传进来实例物品时,使用该实例物品的InstanceID,这样可以在公告上查看到物品的各种信息
    		if(item != null){
    			id = item.getId();
    		}
    		int length = ((Integer)id).toString().length();
    		String appendString = "/s 1#" + length + " " + id;
    		tempstr = tempstr + appendString;
    		
        }
		return tempstr;

    }

    public static void addMessage(int itemId, int msgtype, String message){
    	switch (msgtype) {
        case 1://宝箱
        	messages1.put(itemId, message);
        	break;
        case 2://世界掉落
        	messages2.put(itemId, message);
        	break;
        case 3://商店购买
        	messages3.put(itemId, message);
        	break;
        case 4://i币商店购买
        	messages4.put(itemId, message);
        	break;
        case 5://家园领取
        	messages5.put(itemId, message);
        	break;
        case 6://小岛领取
        	messages6.put(itemId, message);
        	break;
        case 7://使用召唤符（type=47）
        	messages7.put(itemId, message);
        	break;
        case 8://使用寻缘镜（type=13）
        	messages8.put(itemId, message);
        	break;
        case 9://活动公告
        	messages9.put(itemId, message);
        	break;
        case 10://奥德赛之旅
        	messages10.put(itemId, message);
        	break;
    	}

    }

}
