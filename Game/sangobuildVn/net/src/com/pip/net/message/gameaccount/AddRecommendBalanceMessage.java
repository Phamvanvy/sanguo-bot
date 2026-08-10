package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class AddRecommendBalanceMessage extends AbstractMessage {
	protected int accountID;   // 被推荐人帐号ID
	protected int value;       // 被推荐人奖励金额（单位0.01i）
	protected int value2;      // 推荐人奖励金额（单位0.01i）
	
	public AddRecommendBalanceMessage(int serial, int id, int v1, int v2) {
		super(GameAccountMessageType.ADD_RECOMMEND_BALANCE,serial);
		this.accountID = id;
		this.value = v1;
		this.value2 = v2;
	}
	
	public AddRecommendBalanceMessage(int id, int v1, int v2) {
        super(GameAccountMessageType.ADD_RECOMMEND_BALANCE);
        this.accountID = id;
        this.value = v1;
        this.value2 = v2;
    }
	
	public int getAccountID() {
		return accountID;
	}

	public int getValue() {
		return value;
	}
	
	public int getValue2() {
	    return value2;
	}
}
