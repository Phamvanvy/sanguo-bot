package com.pip.gameaccount.qq;

import com.pip.net.message.AbstractMessage;

public class QQBuy2Message extends AbstractMessage {
	protected byte version;
	protected int seqNo;
	protected String uin;
	protected String linkId;
	protected int bId;
	protected short objectId;
	protected int count;
	protected int time;
	
	public QQBuy2Message(short cmd, byte version, int seqNo, String uin, String linkId, int bId, short objectId, int count, int time) {
		super(cmd);
		this.version = version;
		this.seqNo = seqNo;
		this.linkId = linkId;
		this.bId = bId;
		this.uin = uin;
		this.objectId = objectId;
		this.count = count;
		this.time = time;
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

	public int getCount() {
		return count;
	}

	public int getTime() {
		return time;
	}
	
	@Override
	public String toString(){
		return "QQBuyMessage";
	}
}
