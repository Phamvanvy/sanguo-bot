package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class PhoneNotifyMessage extends AbstractMessage {
    protected int accountID;
    protected String phone;
	
	public PhoneNotifyMessage(int accountID, String phone) {
		super(GameAccountMessageType.PHONE_NOTIFY);
		this.accountID = accountID;
		this.phone = phone;
	}

	public int getAccountID() {
	    return accountID;
	}
	
	public String getPhone() {
		return phone;
	}
}
