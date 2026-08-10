package com.pip.itimes.server.gift;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OnlyGiftGroups{
    private static ConcurrentHashMap<Integer, OnlyGiftGroup> giftGroupReference = new ConcurrentHashMap<Integer, OnlyGiftGroup>();
    
    public static void clearOnlyGiftGroups(){
        giftGroupReference.clear();
    }
    
    public static void addOnlyGiftGroup(OnlyGiftGroup onlyGiftGroup){
        giftGroupReference.put(onlyGiftGroup.getId(), onlyGiftGroup);
    }
    
    public static OnlyGiftGroup getOnlyGiftGroup(int onlyGiftGroupId){
        return giftGroupReference.get(onlyGiftGroupId);
    }
    
    public static ConcurrentHashMap<Integer, OnlyGiftGroup> getAllOnlyGiftGroup(){
    	return giftGroupReference;
    }
}