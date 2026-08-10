package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class AddBalanceOkMessage extends AbstractMessage {
    protected int accountID;   // 帐号ID
	protected int value;       // 添加的金额（单位0.01i）
	
	public AddBalanceOkMessage(int serial, int id, int v) {
		super(GameAccountMessageType.ADD_BALANCE_OK,serial);
		this.accountID = id;
		this.value = v;
	}
	
	public int getAccountID() {
		return accountID;
	}

	public int getValue() {
		return value;
	}
}
