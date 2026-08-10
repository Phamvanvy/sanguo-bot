package com.pip.itimes.server.stage;

import java.util.LinkedHashMap;
import java.util.Collection;

public class IStoreGroups {

    private static LinkedHashMap<String,IStoreGroup> groups = new LinkedHashMap<String,IStoreGroup>();
    private static String message = "";		//打折信息

    public IStoreGroups() {
    }

    public static void addGroup(IStoreGroup group){
        groups.put(group.getName(),group);
    }

    public static Collection<IStoreGroup> getGroups(){
        return groups.values();
    }

    public static IStoreItem getStoreItem(int itemId){
        Collection<IStoreGroup> gs = getGroups();
        // Lighthu: 特殊分类(短信专区/话费专区)，传给客户端的物品ID负数
        if (itemId < 0) {
            itemId = -itemId;
            for(IStoreGroup group:gs){
                if (!group.getName().contains("短信") && !group.getName().contains("话费")) {
                    continue;
                }
                for(IStoreItem item:group.getItems()){
                    if(item.item.getItemId()==itemId)
                        return item;
                }
            }
        } else {
            for(IStoreGroup group:gs){
                if (group.getName().contains("短信") || group.getName().contains("话费")) {
                    continue;
                }
                for(IStoreItem item:group.getItems()){
                    if(item.item.getItemId()==itemId)
                        return item;
                }
            }
        }
        return null;
    }

    public static IStoreGroup getGroup(String name){
        Collection<IStoreGroup> gs = getGroups();
        for(IStoreGroup group:gs){
            if(group.getName().equals(name))
                return group;
        }
        return null;
    }
    
    public static void setMessage(String msg){
    	message = msg;
    }
    
    public static String getMessage(){
    	return message;
    }
}
