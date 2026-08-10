package com.pip.net.message.gameaccount;

import com.pip.net.message.AbstractMessage;

public class ModifyPhoneOkMessage extends AbstractMessage {
	public ModifyPhoneOkMessage(int serial){
		super(GameAccountMessageType.MODIFY_PHONE_OK,serial);
	}
	
	public ModifyPhoneOkMessage(){
		super(GameAccountMessageType.MODIFY_PHONE_OK);
	}
}
