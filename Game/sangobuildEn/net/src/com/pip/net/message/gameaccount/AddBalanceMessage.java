package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class AddBalanceMessage extends AbstractMessage {
	protected int accountID;   // 添加i币的帐号ID
	protected int value;       // 添加金额（单位0.01i）
	protected String reason;   // 理由
	
	public AddBalanceMessage(int serial, int id, int v, String reason) {
		super(GameAccountMessageType.ADD_BALANCE,serial);
		this.accountID = id;
		this.value = v;
		this.reason = reason;
	}
	
	public AddBalanceMessage(int id, int v, String reason) {
        super(GameAccountMessageType.ADD_BALANCE);
        this.accountID = id;
        this.value = v;
        this.reason = reason;
    }
	
	public int getAccountID() {
		return accountID;
	}

	public int getValue() {
		return value;
	}
	
	public String getReason() {
		return reason;
	}
}
