package com.pip.itimes.server.stage;

import java.util.LinkedHashMap;
import java.util.Collection;

public class CStoreGroups {

	private static LinkedHashMap<Integer, LinkedHashMap<String,CStoreGroup>> groups = new LinkedHashMap<Integer, LinkedHashMap<String,CStoreGroup>>();

    public CStoreGroups() {
    }

    public static void addTypeGroup (CStoreGroup group, LinkedHashMap<String, CStoreGroup> typeGroups) {
        typeGroups.put(group.getName(), group);
    }
    
    public static void addGroup (Integer typeId, LinkedHashMap<String, CStoreGroup> cstoreGroup) {
    	groups.put(typeId, cstoreGroup);
    }
    
    public static Collection<CStoreGroup> getGroups (int typeId) {
    	LinkedHashMap<String, CStoreGroup> gs = groups.get(typeId);
		return gs.values();
    }

    public static CStoreItem getStoreItem (int itemId, int typeId){
    	Collection<CStoreGroup> gs = getGroups(typeId);
        for (CStoreGroup group : gs) {
            for (CStoreItem item : group.getItems()){
                if(item.item.getItemId() == itemId)
                    return item;
            }
        }
        return null;
    }

    public static CStoreGroup getGroup (String name, int typeId) {
    	Collection<CStoreGroup> gs = getGroups(typeId);
        for (CStoreGroup group : gs) {
            if (group.getName().equals(name)) {
            	return group;
            }
        }
        return null;
    }
}
