package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class LevelUpNotifyMessage extends AbstractMessage {
    protected int accountID;
    protected int playerID;
    protected int level;
    protected String gamecode;
	
	public LevelUpNotifyMessage(int accountID, int playerID, int level, String gamecode) {
		super(GameAccountMessageType.LEVEL_UP_NOTIFY);
		this.accountID = accountID;
		this.playerID = playerID;
		this.level = level;
		this.gamecode = gamecode;
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
}
