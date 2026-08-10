package com.pip.gameaccount.qq;

import com.pip.net.message.AbstractMessage;

public class QQBuy2ResultMessage extends AbstractMessage {
	protected byte version;
	protected int seqNo;
	protected String uin;
	protected String linkId;
	protected int bId;
	protected short objectId;
	protected int count;
	protected byte result;
	
	public QQBuy2ResultMessage(short cmd, byte version, int seqNo, String uin, String linkId, int bId, short objectId, int count, byte result) {
		super(cmd);
		this.version = version;
		this.seqNo = seqNo;
		this.uin = uin;
		this.linkId = linkId;
		this.bId = bId;
		this.objectId = objectId;
		this.count = count;
		this.result = result;
	}
	
	public byte getVersion() {
		return version;
	}
	
	public int getSeqNo() {
		return seqNo;
	}

	public String getLinkId() {
		return linkId;
	}

	public int getBId() {
		return bId;
	}

	public String getUin() {
		return uin;
	}

	public short getObjectId() {
		return objectId;
	}

	public byte getResult() {
		return result;
	}

	public int getCount() {
		return count;
	}
}
