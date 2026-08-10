package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class AddRecommendBalanceOkMessage extends AbstractMessage {
	protected boolean result;  // 是否成功
    protected int accountID;   // 被推荐人帐号ID
	protected int value;       // 给被推荐人添加的金额
	protected int recommendID; // 推荐人帐号ID
	protected int value2;      // 给推荐人添加的金额
	
	public AddRecommendBalanceOkMessage(int serial, boolean res, int id, int v1, int rid, int v2) {
		super(GameAccountMessageType.ADD_RECOMMEND_BALANCE_OK,serial);
		this.result = res;
		this.accountID = id;
		this.value = v1;
		this.recommendID = rid;
		this.value2 = v2;
	}
	
	public boolean getResult() {
	    return result;
	}
	
	public int getAccountID() {
		return accountID;
	}

	public int getValue() {
		return value;
	}
	
	public int getRecommendID(){ 
	    return recommendID;
	}
	
	public int getValue2() {
	    return value2;
	}
}
