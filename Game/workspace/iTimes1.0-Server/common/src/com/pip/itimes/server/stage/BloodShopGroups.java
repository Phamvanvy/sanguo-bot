package com.pip.itimes.server.stage;

import java.util.Collection;
import java.util.LinkedHashMap;

public class BloodShopGroups {
	private static LinkedHashMap<String,BloodStoreGroup> groups = new LinkedHashMap<String,BloodStoreGroup>();
	
	public BloodShopGroups(){
		
	}
	
	public static void addGroup(BloodStoreGroup group){
		groups.put(group.getName(), group);
	}
	
	public static Collection<BloodStoreGroup> getGroups(){
        return groups.values();
    }
	
    public static BloodStoreItem getStoreItem(int itemId){
        Collection<BloodStoreGroup> gs = getGroups();
        if (itemId < 0) {
            itemId = -itemId;
            for(BloodStoreGroup group:gs){
                if (!group.getName().contains("精品")) {
                    continue;
                }
                for(BloodStoreItem item:group.getItems()){
                    if(item.item.getItemId()==itemId)
                        return item;
                }
            }
        } else {
            for(BloodStoreGroup group:gs){
                if (group.getName().contains("属性")) {
                    continue;
                }
                for(BloodStoreItem item:group.getItems()){
                    if(item.item.getItemId()==itemId)
                        return item;
                }
            }
        }
        return null;
    }
    
    public static BloodStoreItem getStoreItem(int itemId, String groupName,int index){
    	BloodStoreGroup group =getBloodStoreGroup(groupName);
    	if(group!=null){
	    	if (itemId < 0) {
	            itemId = -itemId;
	            if (group.getName().contains("精品")) {
	                for(BloodStoreItem item:group.getItems()){
	                	if(item.item.getItemId()==itemId)
	                		return item;
	                }
	            }
	        } else {
                if (!group.getName().contains("属性")) {
                	for(int i = 0; i< group.getItems().length;i++){
                		BloodStoreItem allitem[] = group.getItems();
                		BloodStoreItem item = allitem[i];
                		if((item.item.getItemId()==itemId) && (i == index))
                			return item;
                	}
//                	for(BloodStoreItem item:group.getItems()){
//                		if(item.item.getItemId()==itemId)
//                			return item;
//                	}
                }
	        }
    	}
        return null;
    }
    
    public static BloodStoreGroup getBloodStoreGroup(String name){
        Collection<BloodStoreGroup> gs = getGroups();
        for(BloodStoreGroup group:gs){
            if(group.getName().equals(name))
                return group;
        }
        return null;
    }
}
