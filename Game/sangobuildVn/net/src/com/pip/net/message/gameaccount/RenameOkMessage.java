package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class RenameOkMessage extends AbstractMessage {
	public RenameOkMessage(int serial){
		super(GameAccountMessageType.RENAME_OK,serial);
	}
	
	public RenameOkMessage(){
		super(GameAccountMessageType.RENAME_OK);
	}
}
