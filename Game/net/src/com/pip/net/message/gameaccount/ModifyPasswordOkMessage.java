package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class ModifyPasswordOkMessage extends AbstractMessage {
	
	
	public ModifyPasswordOkMessage(int serial){
		super(GameAccountMessageType.MODIFY_PASSWORD_OK,serial);
	}
	
	public ModifyPasswordOkMessage(){
		super(GameAccountMessageType.MODIFY_PASSWORD_OK);
	}
}
