package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class CreditChangeNotifyMessage extends AbstractMessage {
    protected int accountID;
    protected int credit;
	
	public CreditChangeNotifyMessage(int accountID, int credit) {
		super(GameAccountMessageType.CREDIT_CHANGE_NOTIFY);
		this.accountID = accountID;
		this.credit = credit;
	}

	public int getAccountID() {
	    return accountID;
	}
	
	public int getCredit() {
	    return credit;
	}
}
