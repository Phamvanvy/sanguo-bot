package com.pip.itimes.server.gift;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GiftGroups{
    private static ConcurrentHashMap<Integer, GiftGroup> giftGroupReference = new ConcurrentHashMap<Integer, GiftGroup>();
    
    public static ConcurrentHashMap<Integer, GiftGroup> getGiftGroupReference() {
		return giftGroupReference;
	}

	public static void clearGiftGroups(){
        giftGroupReference.clear();
    }
    
    public static void addGiftGroup(GiftGroup giftGroup){
        giftGroupReference.put(giftGroup.getId(), giftGroup);
    }
    
    public static GiftGroup getGiftGroup(int groupId){
        return giftGroupReference.get(groupId);
    }
}
