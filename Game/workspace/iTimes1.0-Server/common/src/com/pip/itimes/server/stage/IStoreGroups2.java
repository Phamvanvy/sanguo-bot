package com.pip.itimes.server.stage;

import java.util.LinkedHashMap;
import java.util.Collection;

public class IStoreGroups2 {

	private static LinkedHashMap<String,IStoreGroup> groups = new LinkedHashMap<String,IStoreGroup>();
	
	public IStoreGroups2(){
		
	}
	
	public static void addGroup(IStoreGroup group){
        groups.put(group.getName(),group);
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
    
    
    public static IStoreGroup getGroup2(String name){
        Collection<IStoreGroup> gs = getGroups();
        for(IStoreGroup group:gs){
            if(group.getName().equals(name))
                return group;
        }
        return null;
    }
}
