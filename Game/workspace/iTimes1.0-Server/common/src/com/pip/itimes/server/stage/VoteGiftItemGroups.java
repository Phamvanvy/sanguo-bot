package com.pip.itimes.server.stage;

import java.util.LinkedHashMap;
import java.util.Collection;

public class VoteGiftItemGroups {
	// 参加选美
	public static final int ENTRY = 0;
	// 投票大王
	public static final int VOTE = 1;
	
	/**
	 * 选美奖品
	 */
	private static LinkedHashMap <String, VoteGiftItemGroup> voteGiftItems = new LinkedHashMap<String, VoteGiftItemGroup>();
	
	/**
	 * 投票大王奖品
	 */
	private static LinkedHashMap <String, VoteGiftItemGroup> votesKingGiftItems = new LinkedHashMap<String, VoteGiftItemGroup>();
	
	public VoteGiftItemGroups() {
	}

	public static void addVoteGiftItem(VoteGiftItemGroup items){
		voteGiftItems.put(items.getName(), items);
	}
	
	public static void addVotesKingGiftItem (VoteGiftItemGroup items) {
		votesKingGiftItems.put(items.getName(), items);
	}

	public static void clearVoteGiftItemGroup () {
		voteGiftItems.clear();
		votesKingGiftItems.clear();
	}
	
    public static VoteGiftItem[] getGiftItem (int rank, int type) {
    	int i = 0;
		Collection<VoteGiftItemGroup> vgs = getVoteGfitItems(type);
		for (VoteGiftItemGroup group : vgs) {
			for (VoteGiftItem item : group.getItems()){
				if(item.rank == rank) {
					i ++;
				}
			}
		}
		if (i > 0) {
			int j = 0;
			VoteGiftItem[] voteGift = new VoteGiftItem [i];
			for (VoteGiftItemGroup group : vgs) {
				for (VoteGiftItem item : group.getItems()){
					if(item.rank == rank) {
						voteGift[j] = item;
						j ++;
					}
				}
			}
			return voteGift;
		} else {
			return null;
		}
    }
    
    public static Collection<VoteGiftItemGroup> getVoteGfitItems (int type) {
		if (type == ENTRY) {
			return voteGiftItems.values();
		} else if (type == VOTE) {
			return votesKingGiftItems.values();
		}
		return null;
    }
}
