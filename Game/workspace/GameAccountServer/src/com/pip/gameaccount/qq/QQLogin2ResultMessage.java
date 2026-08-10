package com.pip.gameaccount.qq;

import com.pip.net.message.AbstractMessage;

/**
 * 新版本QQ登录同步返回协议。
 * @author lighthu
 */
public class QQLogin2ResultMessage extends AbstractMessage {
	protected byte version;
	protected int seqNo;
	protected boolean result;
	
	public QQLogin2ResultMessage(byte version, int seqNo, boolean result){
		super(QQMessageType.QQ_LOGIN2_RESULT);
		this.version = version;
		this.seqNo = seqNo;
		this.result = result;
	}
	
	public byte getVersion() {
		return version;
	}
	
	public int getSeqNo() {
		return seqNo;
	}
	
	public boolean getResult(){
		return result;
	}
}
