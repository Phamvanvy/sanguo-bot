package com.pip.gameaccount.qq;

import com.pip.net.message.AbstractMessage;

public class QQLoginResultMessage extends AbstractMessage {
	
	protected String result;
	
	public QQLoginResultMessage(String result){
		super(QQMessageType.QQ_LOGIN_RESULT);
		this.result = result;
	}
	
	public String getResult(){
		return result;
	}
}
