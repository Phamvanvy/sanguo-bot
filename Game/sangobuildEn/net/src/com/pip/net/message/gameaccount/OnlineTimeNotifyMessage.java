package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class OnlineTimeNotifyMessage extends AbstractMessage {
    protected int accountID;
    protected int duration;
	
	public OnlineTimeNotifyMessage(int accountID, int duration) {
		super(GameAccountMessageType.ONLINE_TIME_NOTIFY);
		this.accountID = accountID;
		this.duration = duration;
	}

	public int getAccountID() {
	    return accountID;
	}
	
	public int getDuration() {
	    return duration;
	}
}
