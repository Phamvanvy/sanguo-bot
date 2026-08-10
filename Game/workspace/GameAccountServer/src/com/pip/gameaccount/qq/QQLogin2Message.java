package com.pip.gameaccount.qq;

import com.pip.net.message.AbstractMessage;

/**
 * 新版本QQ登录同步协议。
 * @author lighthu
 */
public class QQLogin2Message extends AbstractMessage {
	protected byte version;
	protected int seqNo;
	protected String uin;
	protected String sessionKey;
	protected int time;
	
	public QQLogin2Message(byte version, int seqNo, String uin,String sessionKey,int time){
		super(QQMessageType.QQ_LOGIN2);
		this.version = version;
		this.seqNo = seqNo;
		this.uin = uin;
		this.sessionKey = sessionKey;
		this.time = time;
	}
	
	public byte getVersion() {
		return version;
	}
	
	public int getSeqNo() {
		return seqNo;
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
