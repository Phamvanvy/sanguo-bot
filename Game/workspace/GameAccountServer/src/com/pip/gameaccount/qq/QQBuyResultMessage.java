package com.pip.gameaccount.qq;

import com.pip.net.message.AbstractMessage;

public class QQBuyResultMessage extends AbstractMessage {
	
	protected String linkId;
	protected String bId;
	protected String uin;
	protected String objectId;
	protected short count;
	protected short result;
	
	public QQBuyResultMessage(short cmd,String linkId,String bId,String uin,String objectId,short count,short result){
		super(cmd);
		this.linkId = linkId;
		this.bId = bId;
		this.uin = uin;
		this.objectId = objectId;
		this.result = result;
	}

	public String getLinkId() {
		return linkId;
	}

	public String getBId() {
		return bId;
	}

	public String getUin() {
		return uin;
	}

	public String getObjectId() {
		return objectId;
	}

	public short getResult() {
		return result;
	}

	public short getCount() {
		return count;
	}
}
