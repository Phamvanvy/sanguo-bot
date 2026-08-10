package com.pip.itimes.server.stage;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

public class voteGiftGroups {
    private static ConcurrentHashMap<Integer, VotePlayerGift> votegiftGroupReference = new ConcurrentHashMap<Integer, VotePlayerGift>();
    public static ConcurrentHashMap<Integer, VotePlayerGift> getGiftGroupReference() {
		return votegiftGroupReference;
	}

	public static void clearVoteGiftGroups(){
		votegiftGroupReference.clear();
    }
    
    public static void addVoteGiftGroup(VotePlayerGift votePlayerGift){
    	votegiftGroupReference.put(votePlayerGift.getId(), votePlayerGift);
    }
    
    public static VotePlayerGift getGiftGroup(int voteGroupId){
        return votegiftGroupReference.get(voteGroupId);
    }
    
    public static Enumeration<Integer> getEnumeration () {
    	return votegiftGroupReference.keys();
    }
}
