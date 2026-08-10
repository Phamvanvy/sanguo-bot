package com.pip.gameaccount.qq;

import com.pip.net.message.AbstractMessage;

public class QQLoginMessage extends AbstractMessage {
	
	protected String uin;
	protected String sessionKey;
	protected int time;
	
	public QQLoginMessage(String uin,String sessionKey,int time){
		super(QQMessageType.QQ_LOGIN);
		this.uin = uin;
		this.sessionKey = sessionKey;
		this.time = time;
	}
	
	public String getUin(){
		return uin;
	}
	
	public String getSessionKey(){
		return sessionKey;
	}
	
	public int getTime(){
		return time;
	}
	
	@Override
	public String toString(){
		return "QQLoginMessage";
	}
}
