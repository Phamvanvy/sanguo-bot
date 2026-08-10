package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;


/**
 * @author Jeffery
 * @version 1.0
 */
public class StoreGroups {

    public static Map groups = new HashMap();
    //用于跨服积分商店的物品组
    public static Map<Integer, StoreGroup> fightLevelGroupsMap = new TreeMap<Integer, StoreGroup>();
    
    public static void addStoreGroup(StoreGroup storeGroup){
        groups.put(new Integer(storeGroup.getId()),storeGroup);
    }

    public static Map getFightLevelStoreGroup(){
        return fightLevelGroupsMap;
    }
    public static void addFightLevelStoreGroup(StoreGroup storeGroup){
    	fightLevelGroupsMap.put(new Integer(storeGroup.getId()),storeGroup);
    }
    public static StoreGroup getStoreGroup(int id){
        return (StoreGroup)groups.get(new Integer(id));
    }
}
