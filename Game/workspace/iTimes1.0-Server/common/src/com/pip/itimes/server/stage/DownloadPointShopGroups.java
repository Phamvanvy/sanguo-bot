package com.pip.itimes.server.stage;

import java.util.Collection;
import java.util.LinkedHashMap;

public class DownloadPointShopGroups {
	private static int DS_POINT_USE_MAX ;//每天消费积分的上限
	private static LinkedHashMap<String,IStoreGroup> groups = new LinkedHashMap<String,IStoreGroup>();
	public DownloadPointShopGroups(){
		
	}
	
	public static void setUseMax(int value){
		DS_POINT_USE_MAX = value;
	}
	
	public static int getPointUseMax(){
		return DS_POINT_USE_MAX;
	}
	
	public static void addGroup(IStoreGroup group){
		groups.put(group.getName(), group);
	}
	
	public static Collection<IStoreGroup> getGroups(){
        return groups.values();
    }
	
    public static IStoreItem getStoreItem(int itemId){
        Collection<IStoreGroup> gs = getGroups();
        if (itemId < 0) {
            itemId = -itemId;
            for(IStoreGroup group:gs){
                if (!group.getName().contains("工资")) {
                    continue;
                }
                for(IStoreItem item:group.getItems()){
                    if(item.item.getItemId()==itemId)
                        return item;
                }
            }
        } else {
            for(IStoreGroup group:gs){
                if (group.getName().contains("工资")) {
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
    
    public static IStoreItem getStoreItem(int itemId, String groupName){
    	IStoreGroup group =getIStoreGroup(groupName);
    	if(group!=null){
	    	if (itemId < 0) {
	            itemId = -itemId;
	            if (group.getName().contains("工资")) {
	                for(IStoreItem item:group.getItems()){
	                	if(item.item.getItemId()==itemId)
	                		return item;
	                }
	            }
	        } else {
                if (!group.getName().contains("工资")) {
                	for(IStoreItem item:group.getItems()){
                		if(item.item.getItemId()==itemId)
                			return item;
                	}
                }
	        }
    	}
        return null;
    }
    
    public static IStoreGroup getIStoreGroup(String name){
        Collection<IStoreGroup> gs = getGroups();
        for(IStoreGroup group:gs){
            if(group.getName().equals(name))
                return group;
        }
        return null;
    }
}
