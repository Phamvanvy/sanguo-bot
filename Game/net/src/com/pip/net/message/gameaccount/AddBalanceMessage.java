package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class AddBalanceMessage extends AbstractMessage {
	protected int accountID;   // 添加i币的帐号ID
	protected int value;       // 添加金额（单位0.01i）
	protected String reason;   // 理由
	protected String partition = "";  // 分区ID
	protected int money;       // 实际金额（单位为最小货币单位）
	
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
	
	public AddBalanceMessage(int serial, int id, int v, String reason, String partition, int money) {
		super(GameAccountMessageType.ADD_BALANCE,serial);
		this.accountID = id;
		this.value = v;
		this.reason = reason;
		this.partition = partition;
		this.money = money;
	}
	
	public AddBalanceMessage(int id, int v, String reason, String partition, int money) {
        super(GameAccountMessageType.ADD_BALANCE);
        this.accountID = id;
        this.value = v;
        this.reason = reason;
        this.partition = partition;
        this.money = money;
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
	
	public String getPartition() {
		return partition;
	}
	
	public int getMoney() {
		return money;
	}
}
