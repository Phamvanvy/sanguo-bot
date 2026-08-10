package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class RecommendRewardNotifyMessage extends AbstractMessage {
    protected int accountID;
    protected int playerID;
    protected int level;
    protected String gamecode;
    protected int guestRewardValue;
    protected int ownerID;
    protected int ownerRewardValue;
	
	public RecommendRewardNotifyMessage(int accountID, int playerID, int level, String gamecode, 
	        int guestRewardValue, int ownerID, int ownerRewardValue) {
		super(GameAccountMessageType.RECOMMEND_REWARD_NOTIFY);
		this.accountID = accountID;
		this.playerID = playerID;
		this.level = level;
		this.gamecode = gamecode;
		this.guestRewardValue = guestRewardValue;
		this.ownerID = ownerID;
		this.ownerRewardValue = ownerRewardValue;
	}

	public int getAccountID() {
	    return accountID;
	}
	
	public int getPlayerID() {
	    return playerID;
	}
	
	public int getLevel() {
	    return level;
	}
	
	public String getGameCode() {
	    return gamecode;
	}

    public int getGuestRewardValue() {
        return guestRewardValue;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public int getOwnerRewardValue() {
        return ownerRewardValue;
    }
}
